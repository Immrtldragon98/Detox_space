package com.immrtldragon.detoxspace.data.remote

data class RegisterRequest(val username: String, val email: String, val password: String, val deviceName: String)
data class LoginRequest(val login: String, val password: String, val deviceName: String)
data class AuthResponse(val accessToken: String, val refreshToken: String, val userId: String, val sessionId: String)
data class ApiConnection(val id: String, val user_id: String, val username: String, val allow_invitations: Boolean)
data class ConnectionsResponse(val connections: List<ApiConnection>)
data class CreateInvitationRequest(
    val id: String,
    val recipientId: String,
    val signalType: String,
    val note: String?,
    val proposedAt: String,
    val expiresAt: String,
)
data class ApiInvitation(
    val id: String,
    val sender_id: String,
    val recipient_id: String,
    val signal_type: String,
    val note: String?,
    val proposed_at: String,
    val expires_at: String,
    val state: String,
    val sequence_id: Long,
)
data class InvitationResponse(val invitation: ApiInvitation)
data class InvitationsResponse(val invitations: List<ApiInvitation>, val nextCursor: Long)
data class ErrorResponse(val error: String)
