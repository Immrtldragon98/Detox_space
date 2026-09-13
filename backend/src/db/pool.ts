import pg from "pg";
import { config } from "../config.js";

export const pool = new pg.Pool({
  connectionString: config.DATABASE_URL,
  ...(config.NODE_ENV === "production" ? { ssl: { rejectUnauthorized: false } } : {}),
  max: 10,
  idleTimeoutMillis: 30_000,
});
