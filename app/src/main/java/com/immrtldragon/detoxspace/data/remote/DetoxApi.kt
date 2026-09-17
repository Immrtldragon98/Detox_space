package com.immrtldragon.detoxspace.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DetoxApi {
    @POST("v1/auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @POST("v1/auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("v1/auth/logout") suspend fun logout()
    @DELETE("v1/auth/account") suspend fun deleteAccount()
    @POST("v1/devices/push-token") suspend fun registerPushToken(@Body request: PushTokenRequest)
    @GET("v1/devices") suspend fun devices(): DevicesResponse
    @DELETE("v1/devices/{id}") suspend fun revokeDevice(@Path("id") id: String)
    @GET("v1/connections") suspend fun connections(): ConnectionsResponse
    @POST("v1/connections/invites") suspend fun createConnectionInvite(): ConnectionInviteResponse
    @POST("v1/connections/invites/{code}/accept")
    suspend fun acceptConnectionInvite(@Path("code") code: String): AcceptConnectionInviteResponse
    @DELETE("v1/connections/{userId}") suspend fun removeConnection(@Path("userId") userId: String)
    @POST("v1/connections/{userId}/block") suspend fun blockConnection(@Path("userId") userId: String)
    @GET("v1/invitations") suspend fun invitations(@Query("cursor") cursor: Long): InvitationsResponse
    @POST("v1/invitations") suspend fun createInvitation(@Body request: CreateInvitationRequest): InvitationResponse
    @POST("v1/invitations/{id}/respond")
    suspend fun respondToInvitation(@Path("id") id: String, @Body request: RespondInvitationRequest): InvitationResponse
    @POST("v1/invitations/{id}/cancel") suspend fun cancelInvitation(@Path("id") id: String): InvitationResponse
    @POST("v1/invitations/{id}/complete") suspend fun completeInvitation(@Path("id") id: String): InvitationResponse
}

interface RefreshApi {
    @POST("v1/auth/refresh")
    fun refresh(@Body request: RefreshRequest): retrofit2.Call<AuthResponse>
}
