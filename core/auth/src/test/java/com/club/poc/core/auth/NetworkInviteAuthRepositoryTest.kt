package com.club.poc.core.auth

import com.club.poc.core.common.AppResult
import com.club.poc.core.network.AuthApi
import com.club.poc.core.network.AuthSessionDto
import com.club.poc.core.network.InviteAcceptRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkInviteAuthRepositoryTest {
    @Test
    fun acceptInvite_emitsMemberIdOnSuccess() = runBlocking {
        val repository = NetworkInviteAuthRepository(
            authApi = object : AuthApi {
                override suspend fun acceptInvite(request: InviteAcceptRequestDto): AuthSessionDto {
                    return AuthSessionDto(
                        accessToken = "access",
                        refreshToken = "refresh",
                        memberId = "member-123",
                    )
                }
            },
        )

        val result = repository.acceptInvite(inviteCode = "code", googleIdToken = "id-token").first()

        assertEquals(AppResult.Success("member-123"), result)
    }

    @Test
    fun acceptInvite_emitsErrorOnFailure() = runBlocking {
        val repository = NetworkInviteAuthRepository(
            authApi = object : AuthApi {
                override suspend fun acceptInvite(request: InviteAcceptRequestDto): AuthSessionDto {
                    error("invite invalid")
                }
            },
        )

        val result = repository.acceptInvite(inviteCode = "bad", googleIdToken = "id-token").first()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("invite invalid", error.message)
    }
}
