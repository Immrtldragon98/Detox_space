import { createHash, randomBytes } from "node:crypto";
import jwt from "jsonwebtoken";
import type { RequestHandler } from "express";
import { config } from "./config.js";
import { pool } from "./db/pool.js";

export type AuthUser = { userId: string; sessionId: string };
declare global { namespace Express { interface Request { auth?: AuthUser } } }

export function signAccessToken(auth: AuthUser): string {
  return jwt.sign({ sub: auth.userId, sid: auth.sessionId }, config.JWT_SECRET, {
    expiresIn: config.ACCESS_TOKEN_TTL as jwt.SignOptions["expiresIn"],
    issuer: "detox-space",
    audience: "detox-space-android",
  });
}

export function newRefreshToken(): { token: string; hash: string } {
  const token = randomBytes(48).toString("base64url");
  return { token, hash: hashToken(token) };
}

export function hashToken(value: string): string {
  return createHash("sha256").update(value).digest("hex");
}

export const requireAuth: RequestHandler = async (req, res, next) => {
  try {
    const raw = req.header("authorization")?.replace(/^Bearer\s+/i, "");
    if (!raw) return res.status(401).json({ error: "authentication_required" });
    const claims = jwt.verify(raw, config.JWT_SECRET, {
      issuer: "detox-space", audience: "detox-space-android",
    }) as jwt.JwtPayload;
    const userId = String(claims.sub ?? "");
    const sessionId = String(claims.sid ?? "");
    const session = await pool.query(
      "SELECT 1 FROM device_sessions WHERE id=$1 AND user_id=$2 AND revoked_at IS NULL",
      [sessionId, userId],
    );
    if (!session.rowCount) return res.status(401).json({ error: "session_revoked" });
    req.auth = { userId, sessionId };
    next();
  } catch { return res.status(401).json({ error: "invalid_access_token" }); }
};
