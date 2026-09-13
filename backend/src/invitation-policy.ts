export type InvitationState = "SENT" | "ACCEPTED" | "LATER" | "DECLINED" | "EXPIRED" | "CANCELLED" | "COMPLETED";

const transitions: Record<InvitationState, readonly InvitationState[]> = {
  SENT: ["ACCEPTED", "LATER", "DECLINED", "EXPIRED", "CANCELLED"],
  ACCEPTED: ["COMPLETED", "CANCELLED"],
  LATER: ["ACCEPTED", "DECLINED", "EXPIRED"],
  DECLINED: [], EXPIRED: [], CANCELLED: [], COMPLETED: [],
};

export const canTransition = (from: InvitationState, to: InvitationState) => transitions[from].includes(to);
