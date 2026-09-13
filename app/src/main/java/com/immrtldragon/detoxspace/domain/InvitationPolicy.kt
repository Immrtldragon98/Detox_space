package com.immrtldragon.detoxspace.domain

object InvitationPolicy {
    private val allowed = mapOf(
        InvitationState.SENDING to setOf(InvitationState.SENT, InvitationState.CANCELLED),
        InvitationState.SENT to setOf(
            InvitationState.ACCEPTED, InvitationState.LATER, InvitationState.DECLINED,
            InvitationState.EXPIRED, InvitationState.CANCELLED,
        ),
        InvitationState.ACCEPTED to setOf(InvitationState.COMPLETED, InvitationState.CANCELLED),
        InvitationState.LATER to setOf(InvitationState.ACCEPTED, InvitationState.DECLINED, InvitationState.EXPIRED),
    )

    fun canTransition(from: InvitationState, to: InvitationState): Boolean =
        to in (allowed[from] ?: emptySet())
}
