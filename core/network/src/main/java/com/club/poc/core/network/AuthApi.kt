package com.club.poc.core.network

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/v1/auth/invite/accept")
    suspend fun acceptInvite(@Body request: InviteAcceptRequestDto): AuthSessionDto
}

data class InviteAcceptRequestDto(
    val inviteCode: String,
    val googleIdToken: String,
)

data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val memberId: String,
)
