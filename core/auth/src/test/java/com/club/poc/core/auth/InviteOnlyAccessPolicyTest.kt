package com.club.poc.core.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteOnlyAccessPolicyTest {
    private val policy = InviteOnlyAccessPolicy()

    @Test
    fun accessAllowedOnlyWhenInviteAndSessionAreBothValid() {
        assertTrue(policy.canAccessApp(hasValidInvite = true, hasActiveSession = true))
        assertFalse(policy.canAccessApp(hasValidInvite = true, hasActiveSession = false))
        assertFalse(policy.canAccessApp(hasValidInvite = false, hasActiveSession = true))
        assertFalse(policy.canAccessApp(hasValidInvite = false, hasActiveSession = false))
    }
}
