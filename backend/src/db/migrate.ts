import { readdir, readFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { pool } from "./pool.js";

const migrationsDir = dirname(fileURLToPath(new URL("../../migrations/placeholder", import.meta.url)));

await pool.query(`CREATE TABLE IF NOT EXISTS schema_migrations (
  name text PRIMARY KEY,
  applied_at timestamptz NOT NULL DEFAULT now()
)`);

const files = (await readdir(migrationsDir)).filter((name) => name.endsWith(".sql")).sort();
for (const name of files) {
  const applied = await pool.query("SELECT 1 FROM schema_migrations WHERE name=$1", [name]);
  if (applied.rowCount) continue;
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    await client.query(await readFile(join(migrationsDir, name), "utf8"));
    await client.query("INSERT INTO schema_migrations(name) VALUES($1)", [name]);
    await client.query("COMMIT");
    console.log(`Migration ${name} applied`);
  } catch (error) {
    await client.query("ROLLBACK");
    throw error;
  } finally {
    client.release();
  }
}

await pool.end();
