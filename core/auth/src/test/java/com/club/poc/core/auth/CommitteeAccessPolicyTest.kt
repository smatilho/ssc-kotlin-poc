package com.club.poc.core.auth

import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.Permission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommitteeAccessPolicyTest {
    private val policy = CommitteeAccessPolicy()

    @Test
    fun reservationist_canOverrideBookingsOnly() {
        val permissions = policy.permissionsFor(setOf(CommitteeRole.RESERVATIONIST))

        assertEquals(setOf(Permission.BOOKING_OVERRIDE), permissions)
        assertTrue(policy.canOverrideBookings(setOf(CommitteeRole.RESERVATIONIST)))
        assertFalse(policy.canManageDocuments(setOf(CommitteeRole.RESERVATIONIST)))
    }

    @Test
    fun docsCommittee_canManageDocumentsOnly() {
        val permissions = policy.permissionsFor(setOf(CommitteeRole.DOCS_COMMITTEE))

        assertEquals(setOf(Permission.DOC_UPLOAD_WRITE), permissions)
        assertFalse(policy.canOverrideBookings(setOf(CommitteeRole.DOCS_COMMITTEE)))
        assertTrue(policy.canManageDocuments(setOf(CommitteeRole.DOCS_COMMITTEE)))
    }

    @Test
    fun admin_hasAllCommitteePermissions() {
        val permissions = policy.permissionsFor(setOf(CommitteeRole.ADMIN))

        assertEquals(
            setOf(
                Permission.BOOKING_OVERRIDE,
                Permission.DOC_UPLOAD_WRITE,
            ),
            permissions,
        )
    }
}
