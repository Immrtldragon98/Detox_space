import { EventEmitter } from "node:events";
import { cert, getApps, initializeApp } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";
import { config } from "./config.js";
import { pool } from "./db/pool.js";

import { sendPushBatches } from "./push-batches.js";

export const deliveryEvents = new EventEmitter();

export function publishDelivery(userIds: string[]): void {
  for (const userId of new Set(userIds)) deliveryEvents.emit("sync_required", userId);
}

const firebaseConfigured = Boolean(
  config.FIREBASE_PROJECT_ID && config.FIREBASE_CLIENT_EMAIL && config.FIREBASE_PRIVATE_KEY,
);

if (firebaseConfigured && !getApps().length) {
  initializeApp({
    credential: cert({
      projectId: config.FIREBASE_PROJECT_ID!,
      clientEmail: config.FIREBASE_CLIENT_EMAIL!,
      privateKey: config.FIREBASE_PRIVATE_KEY!.replace(/\\n/g, "\n"),
    }),
  });
}

export async function sendBackgroundSync(userId: string): Promise<void> {
  if (!firebaseConfigured) return;
  try {
    const result = await pool.query<{ fcm_token: string }>(
      "SELECT DISTINCT fcm_token FROM device_sessions WHERE user_id=$1 AND revoked_at IS NULL AND fcm_token IS NOT NULL",
      [userId],
    );
    const tokens = result.rows.map((row) => row.fcm_token);
    await sendPushBatches(tokens,
      (message) => getMessaging().sendEachForMulticast(message),
      (deadTokens) => pool.query(
        "UPDATE device_sessions SET fcm_token=NULL WHERE user_id=$1 AND fcm_token=ANY($2::text[])",
        [userId, deadTokens],
      ),
    );
  } catch {
    // EventEmitter does not await listeners. Periodic Android sync provides recovery.
    console.error("Background sync push failed; periodic sync will retry");
  }
}
