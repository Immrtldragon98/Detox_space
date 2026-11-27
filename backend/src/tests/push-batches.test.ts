import test from "node:test";
import assert from "node:assert/strict";
import type { BatchResponse } from "firebase-admin/messaging";
import { sendPushBatches } from "../push-batches.js";

test("pushes are data-only and chunked at 500 tokens", async () => {
  const sizes: number[] = [];
  await sendPushBatches(Array.from({ length: 1001 }, (_, i) => String(i)), async (message) => {
    sizes.push(message.tokens.length);
    assert.equal(message.notification, undefined);
    assert.equal(message.android?.priority, "high");
    assert.deepEqual(message.data, { type: "sync_required" });
    return { responses: [], successCount: message.tokens.length, failureCount: 0 };
  }, async () => assert.fail("No token should be cleared"));
  assert.deepEqual(sizes, [500, 500, 1]);
});

test("clear only confirmed dead tokens, preserving payload and transient errors", async () => {
  const codes = ["messaging/registration-token-not-registered", "messaging/invalid-registration-token",
    "messaging/invalid-argument", "messaging/server-unavailable"];
  const cleared: string[][] = [];
  await sendPushBatches(["gone", "invalid", "payload", "retry"], async () => ({
    responses: codes.map((code) => ({ success: false, error: { code } })),
    successCount: 0, failureCount: 4,
  } as BatchResponse), async (tokens) => { cleared.push(tokens); });
  assert.deepEqual(cleared, [["gone", "invalid"]]);
});

test("send and cleanup failures propagate to the guarded delivery boundary", async () => {
  await assert.rejects(sendPushBatches(["token"], async () => { throw new Error("offline"); }, async () => {}), /offline/);
  await assert.rejects(sendPushBatches(["token"], async () => ({ responses: [
    { success: false, error: { code: "messaging/registration-token-not-registered" } },
  ], successCount: 0, failureCount: 1 } as BatchResponse), async () => { throw new Error("db unavailable"); }), /db unavailable/);
});
