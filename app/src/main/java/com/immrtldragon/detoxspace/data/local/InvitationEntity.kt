package com.immrtldragon.detoxspace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invitations")
data class InvitationEntity(
    @PrimaryKey val id: String,
    val connectionId: String,
    val connectionName: String,
    val signalId: String,
    val signalTitle: String,
    val note: String?,
    val timeWindow: String,
    val state: String,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
)
