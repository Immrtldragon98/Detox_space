import cors from "cors";
import express from "express";
import helmet from "helmet";
import { config } from "./config.js";
import authRoutes from "./routes/auth.js";
import connectionRoutes from "./routes/connections.js";
import invitationRoutes from "./routes/invitations.js";

export function createApp() {
  const app = express();
  app.disable("x-powered-by");
  app.use(helmet());
  app.use(cors({ origin: config.CORS_ORIGIN, credentials: false }));
  app.use(express.json({ limit: "32kb" }));
  app.get("/health", (_req, res) => res.json({ status: "ok" }));
  app.use("/v1/auth", authRoutes);
  app.use("/v1/connections", connectionRoutes);
  app.use("/v1/invitations", invitationRoutes);
  app.use((_req, res) => res.status(404).json({ error: "not_found" }));
  app.use((error: unknown, _req: express.Request, res: express.Response, _next: express.NextFunction) => {
    console.error(error instanceof Error ? error.message : "unknown_error");
    res.status(500).json({ error: "internal_error" });
  });
  return app;
}
