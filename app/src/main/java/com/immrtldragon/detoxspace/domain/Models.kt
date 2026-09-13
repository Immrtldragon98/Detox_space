package com.immrtldragon.detoxspace.domain

enum class Presence { AVAILABLE, QUIET, AWAY }

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
)

