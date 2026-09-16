import type { Server } from "node:http";
import jwt from "jsonwebtoken";
import { Server as SocketServer } from "socket.io";
import { config } from "./config.js";
import { deliveryEvents, sendBackgroundSync } from "./delivery.js";
import { pool } from "./db/pool.js";

export function createRealtime(server: Server): SocketServer {
  const io = new SocketServer(server, { cors: { origin: config.CORS_ORIGIN } });
  io.use(async (socket, next) => {
    try {
      const raw = socket.handshake.auth.token;
      const claims = jwt.verify(raw, config.JWT_SECRET, {
        issuer: "detox-space", audience: "detox-space-android",
      }) as jwt.JwtPayload;
      const userId = String(claims.sub ?? "");
      const sessionId = String(claims.sid ?? "");
      const active = await pool.query(
        "SELECT 1 FROM device_sessions WHERE id=$1 AND user_id=$2 AND revoked_at IS NULL",
        [sessionId, userId],
      );
      if (!active.rowCount) return next(new Error("session_revoked"));
      socket.data.userId = userId;
      next();
    } catch { next(new Error("unauthorized")); }
  });
  io.on("connection", (socket) => socket.join(`user:${String(socket.data.userId)}`));
  deliveryEvents.on("sync_required", (userId: string) => {
    io.to(`user:${userId}`).emit("sync_required");
    void sendBackgroundSync(userId).catch((error) => console.error("push delivery failed", error));
  });
  return io;
}
