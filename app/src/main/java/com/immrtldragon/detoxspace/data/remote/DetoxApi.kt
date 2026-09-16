package com.immrtldragon.detoxspace.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DetoxApi {
    @POST("v1/auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @POST("v1/auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("v1/auth/logout") suspend fun logout()
    @GET("v1/connections") suspend fun connections(): ConnectionsResponse
    @POST("v1/connections/invites") suspend fun createConnectionInvite(): ConnectionInviteResponse
    @POST("v1/connections/invites/{code}/accept")
    suspend fun acceptConnectionInvite(@Path("code") code: String): AcceptConnectionInviteResponse
    @GET("v1/invitations") suspend fun invitations(@Query("cursor") cursor: Long): InvitationsResponse
    @POST("v1/invitations") suspend fun createInvitation(@Body request: CreateInvitationRequest): InvitationResponse
    @POST("v1/invitations/{id}/respond")
    suspend fun respondToInvitation(@Path("id") id: String, @Body request: RespondInvitationRequest): InvitationResponse
    @POST("v1/invitations/{id}/cancel") suspend fun cancelInvitation(@Path("id") id: String): InvitationResponse
    @POST("v1/invitations/{id}/complete") suspend fun completeInvitation(@Path("id") id: String): InvitationResponse
}
