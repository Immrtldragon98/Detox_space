import { Router } from "express";
import { hash, verify } from "@node-rs/argon2";
import { z } from "zod";
import { pool } from "../db/pool.js";
import { hashToken, newRefreshToken, requireAuth, signAccessToken } from "../auth.js";

const router = Router();
const credentials = z.object({
  login: z.string().min(3).max(254),
  password: z.string().min(10).max(128),
  deviceName: z.string().min(1).max(80),
});

router.post("/register", async (req, res) => {
  const input = z.object({
    username: z.string().regex(/^[a-zA-Z0-9_]{3,24}$/),
    email: z.string().email().max(254),
    password: z.string().min(10).max(128),
    deviceName: z.string().min(1).max(80),
  }).safeParse(req.body);
  if (!input.success) return res.status(400).json({ error: "invalid_input", fields: input.error.flatten().fieldErrors });
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const user = await client.query<{ id: string }>(
      "INSERT INTO users(username,email,password_hash) VALUES(lower($1),lower($2),$3) RETURNING id",
      [input.data.username, input.data.email, await hash(input.data.password)],
    );
    const refresh = newRefreshToken();
    const session = await client.query<{ id: string }>(
      "INSERT INTO device_sessions(user_id,device_name,refresh_token_hash) VALUES($1,$2,$3) RETURNING id",
      [user.rows[0]!.id, input.data.deviceName, refresh.hash],
    );
    await client.query("COMMIT");
    const auth = { userId: user.rows[0]!.id, sessionId: session.rows[0]!.id };
    return res.status(201).json({ accessToken: signAccessToken(auth), refreshToken: refresh.token, ...auth });
  } catch (error: unknown) {
    await client.query("ROLLBACK");
    if ((error as { code?: string }).code === "23505") return res.status(409).json({ error: "account_exists" });
    throw error;
  } finally { client.release(); }
});

router.post("/login", async (req, res) => {
  const input = credentials.safeParse(req.body);
  if (!input.success) return res.status(400).json({ error: "invalid_input" });
  const user = await pool.query<{ id: string; password_hash: string }>(
    "SELECT id,password_hash FROM users WHERE deleted_at IS NULL AND (username=lower($1) OR email=lower($1))",
    [input.data.login],
  );
  const row = user.rows[0];
  if (!row || !(await verify(row.password_hash, input.data.password))) return res.status(401).json({ error: "invalid_credentials" });
  const refresh = newRefreshToken();
  const session = await pool.query<{ id: string }>(
    "INSERT INTO device_sessions(user_id,device_name,refresh_token_hash) VALUES($1,$2,$3) RETURNING id",
    [row.id, input.data.deviceName, refresh.hash],
  );
  const auth = { userId: row.id, sessionId: session.rows[0]!.id };
  return res.json({ accessToken: signAccessToken(auth), refreshToken: refresh.token, ...auth });
});

router.post("/refresh", async (req, res) => {
  const input = z.object({ refreshToken: z.string().min(32).max(256) }).safeParse(req.body);
  if (!input.success) return res.status(400).json({ error: "invalid_input" });
  const replacement = newRefreshToken();
  const session = await pool.query<{ id: string; user_id: string }>(
    `UPDATE device_sessions
     SET refresh_token_hash=$1,last_seen_at=now()
     WHERE refresh_token_hash=$2 AND revoked_at IS NULL
     RETURNING id,user_id`,
    [replacement.hash, hashToken(input.data.refreshToken)],
  );
  const row = session.rows[0];
  if (!row) return res.status(401).json({ error: "invalid_refresh_token" });
  const auth = { userId: row.user_id, sessionId: row.id };
  return res.json({ accessToken: signAccessToken(auth), refreshToken: replacement.token, ...auth });
});

router.post("/logout", requireAuth, async (req, res) => {
  await pool.query(
    "UPDATE device_sessions SET revoked_at=now() WHERE id=$1 AND user_id=$2 AND revoked_at IS NULL",
    [req.auth!.sessionId, req.auth!.userId],
  );
  return res.status(204).end();
});

export default router;
