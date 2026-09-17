import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../auth.js";
import { pool } from "../db/pool.js";
import type { InvitationState } from "../invitation-policy.js";
import { publishDelivery } from "../delivery.js";

const router = Router();
router.use(requireAuth);

router.get("/", async (req, res) => {
  const parsedCursor = z.coerce.number().int().nonnegative().default(0).safeParse(req.query.cursor);
  if (!parsedCursor.success) return res.status(400).json({ error: "invalid_cursor" });
  const cursor = parsedCursor.data;
  await pool.query(
    "UPDATE invitations SET state='EXPIRED',updated_at=now() WHERE expires_at<=now() AND state=ANY($1::text[]) AND (sender_id=$2 OR recipient_id=$2)",
    [["SENT", "LATER"], req.auth!.userId],
  );
  const result = await pool.query(
    `SELECT * FROM invitations WHERE (sender_id=$1 OR recipient_id=$1) AND sequence_id>$2
     ORDER BY sequence_id ASC LIMIT 200`, [req.auth!.userId, cursor],
  );
  res.json({ invitations: result.rows, nextCursor: result.rows.at(-1)?.sequence_id ?? cursor });
});

router.post("/", async (req, res) => {
  const input = z.object({
    id: z.string().uuid(), recipientId: z.string().uuid(),
    signalType: z.enum(["WALK", "COFFEE", "TALK", "FREE"]),
    note: z.string().trim().max(80).optional(), proposedAt: z.iso.datetime(), expiresAt: z.iso.datetime(),
  }).safeParse(req.body);
  if (!input.success) return res.status(400).json({ error: "invalid_input", fields: input.error.flatten().fieldErrors });
  if (input.data.recipientId === req.auth!.userId) return res.status(400).json({ error: "cannot_invite_self" });
  const proposedAt = new Date(input.data.proposedAt);
  const expiresAt = new Date(input.data.expiresAt);
  if (expiresAt <= new Date() || expiresAt <= proposedAt) return res.status(400).json({ error: "invalid_expiry" });
  const connection = await pool.query<{ id: string; allowed: boolean }>(
    `SELECT id, CASE WHEN user_low=$2 THEN low_allows_invitations ELSE high_allows_invitations END AS allowed
     FROM connections WHERE (user_low=LEAST($1::uuid,$2::uuid) AND user_high=GREATEST($1::uuid,$2::uuid))
     AND NOT EXISTS (SELECT 1 FROM blocks WHERE (blocker_id=$1 AND blocked_id=$2) OR (blocker_id=$2 AND blocked_id=$1))`,
    [req.auth!.userId, input.data.recipientId],
  );
  if (!connection.rows[0]?.allowed) return res.status(403).json({ error: "invitation_not_allowed" });
  const result = await pool.query(
    `WITH inserted AS (
       INSERT INTO invitations(id,sender_id,recipient_id,connection_id,signal_type,note,proposed_at,expires_at)
       VALUES($1,$2,$3,$4,$5,$6,$7,$8) ON CONFLICT(id) DO NOTHING RETURNING *
     ), owned AS (
       SELECT * FROM invitations WHERE id=$1 AND sender_id=$2 AND recipient_id=$3
     ) SELECT * FROM inserted UNION ALL SELECT * FROM owned LIMIT 1`,
    [input.data.id, req.auth!.userId, input.data.recipientId, connection.rows[0].id,
      input.data.signalType, input.data.note ?? null, proposedAt, expiresAt],
  );
  if (!result.rows[0]) return res.status(409).json({ error: "idempotency_key_conflict" });
  publishDelivery([input.data.recipientId]);
  res.status(201).json({ invitation: result.rows[0] });
});

router.post("/:id/respond", async (req, res) => {
  const state = z.enum(["ACCEPTED", "LATER", "DECLINED"]).safeParse(req.body?.state);
  if (!state.success) return res.status(400).json({ error: "invalid_response" });
  const allowedFrom: Record<"ACCEPTED" | "LATER" | "DECLINED", InvitationState[]> = {
    ACCEPTED: ["SENT", "LATER"], LATER: ["SENT"], DECLINED: ["SENT", "LATER"],
  };
  const result = await pool.query(
    "UPDATE invitations SET state=$1,updated_at=now() WHERE id=$2 AND recipient_id=$3 AND state=ANY($4::text[]) RETURNING *",
    [state.data, req.params.id, req.auth!.userId, allowedFrom[state.data]],
  );
  if (!result.rows[0]) return res.status(409).json({ error: "not_found_or_invalid_transition" });
  publishDelivery([result.rows[0].sender_id]);
  res.json({ invitation: result.rows[0] });
});

router.post("/:id/cancel", async (req, res) => {
  const result = await pool.query(
    "UPDATE invitations SET state='CANCELLED',updated_at=now() WHERE id=$1 AND sender_id=$2 AND state=ANY($3::text[]) RETURNING *",
    [req.params.id, req.auth!.userId, ["SENT", "ACCEPTED"]],
  );
  if (!result.rows[0]) return res.status(409).json({ error: "not_found_or_invalid_transition" });
  publishDelivery([result.rows[0].recipient_id]);
  res.json({ invitation: result.rows[0] });
});

router.post("/:id/complete", async (req, res) => {
  const result = await pool.query(
    "UPDATE invitations SET state='COMPLETED',updated_at=now() WHERE id=$1 AND (sender_id=$2 OR recipient_id=$2) AND state='ACCEPTED' RETURNING *",
    [req.params.id, req.auth!.userId],
  );
  if (!result.rows[0]) return res.status(409).json({ error: "not_found_or_invalid_transition" });
  const row = result.rows[0];
  publishDelivery([row.sender_id === req.auth!.userId ? row.recipient_id : row.sender_id]);
  res.json({ invitation: result.rows[0] });
});

export default router;
