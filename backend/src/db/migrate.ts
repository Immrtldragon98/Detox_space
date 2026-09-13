import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { pool } from "./pool.js";

const migration = fileURLToPath(new URL("../../migrations/001_initial.sql", import.meta.url));
await pool.query(await readFile(migration, "utf8"));
await pool.end();
console.log("Migration 001 applied");
