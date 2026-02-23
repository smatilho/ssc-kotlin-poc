package com.club.poc.core.auth

class InviteOnlyAccessPolicy {
    fun canAccessApp(hasValidInvite: Boolean, hasActiveSession: Boolean): Boolean {
        return hasValidInvite && hasActiveSession
    }
}
