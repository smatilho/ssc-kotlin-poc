package com.club.poc.core.testing

import com.club.poc.core.model.MembershipStatus
import com.club.poc.core.model.MemberProfile

object Fixtures {
    fun activeMember(memberId: String = "member-1", clubId: String = "club-1"): MemberProfile {
        return MemberProfile(
            memberId = memberId,
            userId = "user-1",
            clubId = clubId,
            membershipStatus = MembershipStatus.ACTIVE,
            duesPaidAt = "2025-11-01T00:00:00Z",
        )
    }
}
