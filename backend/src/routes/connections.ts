import { createHash, randomBytes } from "node:crypto";
import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../auth.js";
import { pool } from "../db/pool.js";
import { publishDelivery } from "../delivery.js";

const router = Router();
router.use(requireAuth);
const digest = (value: string) => createHash("sha256").update(value).digest("hex");

router.get("/", async (req, res) => {
  const result = await pool.query(
    `SELECT c.id, u.id AS user_id, u.username,
      CASE WHEN c.user_low=$1 THEN c.high_allows_invitations ELSE c.low_allows_invitations END AS allow_invitations
     FROM connections c JOIN users u ON u.id=CASE WHEN c.user_low=$1 THEN c.user_high ELSE c.user_low END
     WHERE (c.user_low=$1 OR c.user_high=$1)
       AND NOT EXISTS (SELECT 1 FROM blocks b WHERE (b.blocker_id=$1 AND b.blocked_id=u.id) OR (b.blocker_id=u.id AND b.blocked_id=$1))
     ORDER BY u.username`, [req.auth!.userId],
  );
  res.json({ connections: result.rows });
});

router.post("/invites", async (req, res) => {
  const code = randomBytes(18).toString("base64url");
  await pool.query(
    "INSERT INTO connection_invites(code_hash,created_by,expires_at) VALUES($1,$2,now()+interval '24 hours')",
    [digest(code), req.auth!.userId],
  );
  res.status(201).json({ code, expiresInSeconds: 86_400 });
});

router.post("/invites/:code/accept", async (req, res) => {
  const code = z.string().min(12).safeParse(req.params.code);
  if (!code.success) return res.status(400).json({ error: "invalid_code" });
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const invite = await client.query<{ id: string; created_by: string }>(
      "SELECT id,created_by FROM connection_invites WHERE code_hash=$1 AND accepted_at IS NULL AND expires_at>now() FOR UPDATE",
      [digest(code.data)],
    );
    const row = invite.rows[0];
    if (!row) { await client.query("ROLLBACK"); return res.status(404).json({ error: "invite_unavailable" }); }
    if (row.created_by === req.auth!.userId) { await client.query("ROLLBACK"); return res.status(400).json({ error: "cannot_connect_to_self" }); }
    const [low, high] = [row.created_by, req.auth!.userId].sort();
    const connection = await client.query<{ id: string }>(
      "INSERT INTO connections(user_low,user_high) VALUES($1,$2) ON CONFLICT(user_low,user_high) DO UPDATE SET user_low=EXCLUDED.user_low RETURNING id",
      [low, high],
    );
    await client.query("UPDATE connection_invites SET accepted_by=$1,accepted_at=now() WHERE id=$2", [req.auth!.userId, row.id]);
    await client.query("COMMIT");
    publishDelivery([row.created_by]);
    return res.json({ connectionId: connection.rows[0]!.id });
  } catch (error) { await client.query("ROLLBACK"); throw error; }
  finally { client.release(); }
});

router.post("/:userId/block", async (req, res) => {
  if (req.params.userId === req.auth!.userId) return res.status(400).json({ error: "cannot_block_self" });
  await pool.query("INSERT INTO blocks(blocker_id,blocked_id) VALUES($1,$2) ON CONFLICT DO NOTHING", [req.auth!.userId, req.params.userId]);
  res.status(204).end();
});

router.delete("/:userId", async (req, res) => {
  if (req.params.userId === req.auth!.userId) return res.status(400).json({ error: "cannot_remove_self" });
  const result = await pool.query(
    `DELETE FROM connections
     WHERE user_low=LEAST($1::uuid,$2::uuid) AND user_high=GREATEST($1::uuid,$2::uuid)
     RETURNING id`,
    [req.auth!.userId, req.params.userId],
  );
  if (!result.rowCount) return res.status(404).json({ error: "connection_not_found" });
  publishDelivery([req.params.userId]);
  res.status(204).end();
});

export default router;
