package com.immrtldragon.detoxspace.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvitationPolicyTest {
    @Test fun sentInvitationCanBeAcceptedDeclinedOrCancelled() {
        assertTrue(InvitationPolicy.canTransition(InvitationState.SENT, InvitationState.ACCEPTED))
        assertTrue(InvitationPolicy.canTransition(InvitationState.SENT, InvitationState.DECLINED))
        assertTrue(InvitationPolicy.canTransition(InvitationState.SENT, InvitationState.CANCELLED))
    }

    @Test fun completedInvitationIsTerminal() {
        assertFalse(InvitationPolicy.canTransition(InvitationState.COMPLETED, InvitationState.SENT))
        assertFalse(InvitationPolicy.canTransition(InvitationState.COMPLETED, InvitationState.CANCELLED))
    }

    @Test fun acceptedInvitationCanBeCompleted() {
        assertTrue(InvitationPolicy.canTransition(InvitationState.ACCEPTED, InvitationState.COMPLETED))
    }
}
