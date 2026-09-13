package com.immrtldragon.detoxspace.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DetoxApi {
    @POST("v1/auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @POST("v1/auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("v1/auth/logout") suspend fun logout()
    @GET("v1/connections") suspend fun connections(): ConnectionsResponse
    @GET("v1/invitations") suspend fun invitations(@Query("cursor") cursor: Long): InvitationsResponse
    @POST("v1/invitations") suspend fun createInvitation(@Body request: CreateInvitationRequest): InvitationResponse
}
