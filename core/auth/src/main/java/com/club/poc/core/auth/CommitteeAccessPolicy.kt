package com.club.poc.core.auth

import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.Permission

class CommitteeAccessPolicy {
    fun permissionsFor(roles: Set<CommitteeRole>): Set<Permission> {
        val permissions = mutableSetOf<Permission>()
        roles.forEach { role ->
            when (role) {
                CommitteeRole.ADMIN -> {
                    permissions += Permission.BOOKING_OVERRIDE
                    permissions += Permission.DOC_UPLOAD_WRITE
                }
                CommitteeRole.RESERVATIONIST -> permissions += Permission.BOOKING_OVERRIDE
                CommitteeRole.DOCS_COMMITTEE -> permissions += Permission.DOC_UPLOAD_WRITE
                CommitteeRole.MEMBER -> Unit
                CommitteeRole.TREASURER -> Unit
            }
        }
        return permissions
    }

    fun canOverrideBookings(roles: Set<CommitteeRole>): Boolean {
        return Permission.BOOKING_OVERRIDE in permissionsFor(roles)
    }

    fun canManageDocuments(roles: Set<CommitteeRole>): Boolean {
        return Permission.DOC_UPLOAD_WRITE in permissionsFor(roles)
    }
}
