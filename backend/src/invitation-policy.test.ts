import assert from "node:assert/strict";
import test from "node:test";
import { canTransition } from "./invitation-policy.js";

test("sent invitation can be accepted", () => assert.equal(canTransition("SENT", "ACCEPTED"), true));
test("completed invitation is terminal", () => assert.equal(canTransition("COMPLETED", "SENT"), false));
test("recipient cannot move declined invitation", () => assert.equal(canTransition("DECLINED", "ACCEPTED"), false));
