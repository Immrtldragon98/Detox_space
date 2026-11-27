import test from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import pg from "pg";

test("migration supports pages beyond 200 and advances updated invitations", {
  skip: !process.env.CURSOR_TEST_DATABASE_URL,
}, async () => {
  const client = new pg.Client({ connectionString: process.env.CURSOR_TEST_DATABASE_URL });
  await client.connect();
  try {
    await client.query("BEGIN");
    await client.query("CREATE SCHEMA cursor_regression; SET LOCAL search_path TO cursor_regression, public");
    for (const name of ["001_initial.sql", "002_production_hardening.sql", "003_invitation_change_cursor.sql"]) {
      await client.query(await readFile(new URL(`../../migrations/${name}`, import.meta.url), "utf8"));
    }
    const users = await client.query(`INSERT INTO users(username,email,password_hash)
      VALUES ('first','first@example.com','hash'),('second','second@example.com','hash') RETURNING id`);
    const [a, b] = users.rows.map((row) => row.id);
    const connection = await client.query(`INSERT INTO connections(user_low,user_high)
      VALUES (LEAST($1::uuid,$2::uuid),GREATEST($1::uuid,$2::uuid)) RETURNING id`, [a, b]);
    await client.query(`INSERT INTO invitations(id,sender_id,recipient_id,connection_id,signal_type,proposed_at,expires_at)
      SELECT gen_random_uuid(),$1,$2,$3,'WALK',now(),now()+interval '1 day' FROM generate_series(1,451)`,
    [a, b, connection.rows[0].id]);
    let cursor = "0";
    let count = 0;
    while (true) {
      const page = await client.query("SELECT * FROM invitations WHERE sequence_id>$1 ORDER BY sequence_id LIMIT 200", [cursor]);
      if (!page.rowCount) break;
      count += page.rowCount;
      cursor = page.rows.at(-1).sequence_id;
    }
    assert.equal(count, 451);
    const first = await client.query("SELECT id FROM invitations ORDER BY sequence_id LIMIT 1");
    for (const state of ["ACCEPTED", "DECLINED", "CANCELLED", "COMPLETED", "EXPIRED", "LATER"]) {
      const update = await client.query("UPDATE invitations SET state=$1 WHERE id=$2 RETURNING sequence_id", [state, first.rows[0].id]);
      assert.ok(BigInt(update.rows[0].sequence_id) > BigInt(cursor));
      cursor = update.rows[0].sequence_id;
    }
    await client.query("ROLLBACK");
  } finally {
    await client.end();
  }
});
