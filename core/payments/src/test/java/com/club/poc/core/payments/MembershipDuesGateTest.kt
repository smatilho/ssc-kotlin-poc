package com.club.poc.core.payments

import com.club.poc.core.model.MembershipStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MembershipDuesGateTest {
    private val gate = MembershipDuesGate()

    @Test
    fun bookingAllowedOnlyForActiveMembership() {
        assertTrue(gate.canCreateBooking(MembershipStatus.ACTIVE))
        assertFalse(gate.canCreateBooking(MembershipStatus.UNPAID))
        assertFalse(gate.canCreateBooking(MembershipStatus.LAPSED))
    }

    @Test
    fun evaluateReturnsSpecificFailureReasons() {
        val unpaidResult = gate.evaluate(MembershipStatus.UNPAID)
        val lapsedResult = gate.evaluate(MembershipStatus.LAPSED)

        assertEquals(
            BookingEligibilityResult.Ineligible(BookingEligibilityFailureReason.DUES_UNPAID),
            unpaidResult,
        )
        assertEquals(
            BookingEligibilityResult.Ineligible(BookingEligibilityFailureReason.MEMBERSHIP_LAPSED),
            lapsedResult,
        )
    }
}
