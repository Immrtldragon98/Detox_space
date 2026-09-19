import cors from "cors";
import express from "express";
import helmet from "helmet";
import { rateLimit } from "express-rate-limit";
import { config } from "./config.js";
import authRoutes from "./routes/auth.js";
import connectionRoutes from "./routes/connections.js";
import invitationRoutes from "./routes/invitations.js";
import deviceRoutes from "./routes/devices.js";
import { pool } from "./db/pool.js";

export function createApp() {
  const app = express();
  app.set("trust proxy", 1);
  app.disable("x-powered-by");
  app.use(helmet());
  app.use(cors({ origin: config.CORS_ORIGIN, credentials: false }));
  app.use(express.json({ limit: "32kb" }));
  app.get("/health", (_req, res) => res.json({ status: "ok", service: "detox-space-api" }));
  app.get("/health/ready", async (_req, res) => {
    try {
      await pool.query("SELECT 1");
      res.json({ status: "ready", service: "detox-space-api", database: "connected" });
    } catch (error) {
      console.error("readiness_check_failed", error instanceof Error ? error.message : "unknown_error");
      res.status(503).json({ status: "unavailable", service: "detox-space-api", database: "unavailable" });
    }
  });
  app.use("/v1", rateLimit({
    windowMs: 60_000,
    limit: 120,
    standardHeaders: "draft-8",
    legacyHeaders: false,
  }));
  app.use("/v1/auth", rateLimit({
    windowMs: 15 * 60_000,
    limit: 30,
    standardHeaders: "draft-8",
    legacyHeaders: false,
  }), authRoutes);
  app.use("/v1/connections", connectionRoutes);
  app.use("/v1/invitations", invitationRoutes);
  app.use("/v1/devices", deviceRoutes);
  app.use((_req, res) => res.status(404).json({ error: "not_found" }));
  app.use((error: unknown, _req: express.Request, res: express.Response, _next: express.NextFunction) => {
    console.error(error instanceof Error ? error.message : "unknown_error");
    res.status(500).json({ error: "internal_error" });
  });
  return app;
}
