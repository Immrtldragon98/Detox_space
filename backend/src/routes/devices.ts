import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../auth.js";
import { pool } from "../db/pool.js";

const router = Router();
router.use(requireAuth);

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
