package com.immrtldragon.detoxspace.domain

enum class Presence { AVAILABLE, QUIET, AWAY }

enum class InvitationState { SENDING, SENT, ACCEPTED, LATER, DECLINED, EXPIRED, CANCELLED, COMPLETED }

enum class TimeWindow(val label: String, val durationMinutes: Long) {
    NOW("Now", 60),
    LATER_TODAY("Later today", 360),
    TOMORROW("Tomorrow", 1_440),
}

data class Connection(
    val id: String,
    val name: String,
    val initials: String,
    val presence: Presence,
    val status: String,
    val allowSignals: Boolean = true,
)

data class SignalType(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
)

data class SentSignal(
    val id: String,
    val connectionName: String,
    val signalTitle: String,
    val sentAt: String,
    val state: InvitationState = InvitationState.SENT,
    val timeWindow: String = TimeWindow.NOW.label,
)
