import type { BatchResponse, MulticastMessage } from "firebase-admin/messaging";

export async function sendPushBatches(
  tokens: string[],
  send: (message: MulticastMessage) => Promise<BatchResponse>,
  clear: (tokens: string[]) => Promise<unknown>,
): Promise<void> {
  for (let offset = 0; offset < tokens.length; offset += 500) {
    const batch = tokens.slice(offset, offset + 500);
    const response = await send({ tokens: batch, data: { type: "sync_required" }, android: { priority: "high" } });
    const deadTokens = response.responses.flatMap((item, index) =>
      item.error?.code === "messaging/registration-token-not-registered" ||
      item.error?.code === "messaging/invalid-registration-token" ? [batch[index]!] : [],
    );
    // invalid-argument can mean a bad payload; it does not prove a token is dead.
    if (deadTokens.length) await clear(deadTokens);
    if (response.failureCount) console.warn("Background sync push failures", response.failureCount);
  }
}
