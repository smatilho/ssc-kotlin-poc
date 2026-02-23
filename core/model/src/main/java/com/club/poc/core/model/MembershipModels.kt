package com.club.poc.core.model

enum class MembershipStatus {
    ACTIVE,
    LAPSED,
    UNPAID,
}

data class MemberProfile(
    val memberId: String,
    val userId: String,
    val clubId: String,
    val membershipStatus: MembershipStatus,
    val duesPaidAt: String? = null,
)

enum class CommitteeRole {
    MEMBER,
    RESERVATIONIST,
    DOCS_COMMITTEE,
    TREASURER,
    ADMIN,
}

enum class Permission {
    BOOKING_OVERRIDE,
    DOC_UPLOAD_WRITE,
}
