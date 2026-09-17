import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../auth.js";
import { pool } from "../db/pool.js";

const router = Router();
router.use(requireAuth);

router.get("/", async (req, res) => {
  const result = await pool.query(
    `SELECT id,device_name,created_at,last_seen_at,(id=$2) AS is_current
     FROM device_sessions
     WHERE user_id=$1 AND revoked_at IS NULL
     ORDER BY last_seen_at DESC`,
    [req.auth!.userId, req.auth!.sessionId],
  );
  res.json({ devices: result.rows });
});

router.delete("/:id", async (req, res) => {
  const id = z.string().uuid().safeParse(req.params.id);
  if (!id.success) return res.status(400).json({ error: "invalid_device" });
  const result = await pool.query(
    "UPDATE device_sessions SET revoked_at=now(),fcm_token=NULL WHERE id=$1 AND user_id=$2 AND revoked_at IS NULL RETURNING id",
    [id.data, req.auth!.userId],
  );
  if (!result.rowCount) return res.status(404).json({ error: "device_not_found" });
  res.status(204).end();
});

router.post("/push-token", async (req, res) => {
  const token = z.string().min(20).max(4096).safeParse(req.body?.token);
  if (!token.success) return res.status(400).json({ error: "invalid_push_token" });
  await pool.query(
    "UPDATE device_sessions SET fcm_token=$1,last_seen_at=now() WHERE id=$2 AND user_id=$3 AND revoked_at IS NULL",
    [token.data, req.auth!.sessionId, req.auth!.userId],
  );
  res.status(204).end();
});

export default router;
