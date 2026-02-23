package com.club.poc.core.auth

import com.club.poc.core.common.AppResult
import com.club.poc.core.common.DefaultDispatcherProvider
import com.club.poc.core.common.DispatcherProvider
import com.club.poc.core.network.AuthApi
import com.club.poc.core.network.InviteAcceptRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NetworkInviteAuthRepository(
    private val authApi: AuthApi,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider,
) : InviteAuthRepository {
    override fun acceptInvite(inviteCode: String, googleIdToken: String): Flow<AppResult<String>> = flow {
        try {
            val session = authApi.acceptInvite(
                InviteAcceptRequestDto(
                    inviteCode = inviteCode,
                    googleIdToken = googleIdToken,
                ),
            )
            emit(AppResult.Success(session.memberId))
        } catch (throwable: Throwable) {
            emit(AppResult.Error(message = throwable.toRepositoryMessage("accept invite"), cause = throwable))
        }
    }.flowOn(dispatcherProvider.io)

    private fun Throwable.toRepositoryMessage(action: String): String {
        return message?.takeIf { it.isNotBlank() } ?: "Unable to $action"
    }
}
