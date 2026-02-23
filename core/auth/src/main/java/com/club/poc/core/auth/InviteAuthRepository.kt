package com.club.poc.core.auth

import com.club.poc.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface InviteAuthRepository {
    fun acceptInvite(inviteCode: String, googleIdToken: String): Flow<AppResult<String>>
}
