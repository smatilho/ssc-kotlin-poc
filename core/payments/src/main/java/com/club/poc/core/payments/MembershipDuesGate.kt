package com.club.poc.core.payments

import com.club.poc.core.model.MembershipStatus

enum class BookingEligibilityFailureReason {
    DUES_UNPAID,
    MEMBERSHIP_LAPSED,
}

sealed interface BookingEligibilityResult {
    data object Eligible : BookingEligibilityResult
    data class Ineligible(val reason: BookingEligibilityFailureReason) : BookingEligibilityResult
}

class MembershipDuesGate {
    fun evaluate(membershipStatus: MembershipStatus): BookingEligibilityResult {
        return when (membershipStatus) {
            MembershipStatus.ACTIVE -> BookingEligibilityResult.Eligible
            MembershipStatus.UNPAID -> BookingEligibilityResult.Ineligible(
                BookingEligibilityFailureReason.DUES_UNPAID,
            )
            MembershipStatus.LAPSED -> BookingEligibilityResult.Ineligible(
                BookingEligibilityFailureReason.MEMBERSHIP_LAPSED,
            )
        }
    }

    fun canCreateBooking(membershipStatus: MembershipStatus): Boolean {
        return evaluate(membershipStatus) is BookingEligibilityResult.Eligible
    }
}
