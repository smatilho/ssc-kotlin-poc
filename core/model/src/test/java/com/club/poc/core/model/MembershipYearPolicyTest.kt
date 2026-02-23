package com.club.poc.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MembershipYearPolicyTest {
    private val policy = MembershipYearPolicy(startMonth = 11, startDay = 1)

    @Test
    fun dateBeforeBoundaryFallsInPreviousYearWindow() {
        val year = policy.forDate(year = 2026, month = 10, dayOfMonth = 31)

        assertEquals("2025-11-01", year.startDate)
        assertEquals("2026-10-31", year.endDate)
    }

    @Test
    fun dateOnBoundaryStartsNewMembershipYear() {
        val year = policy.forDate(year = 2026, month = 11, dayOfMonth = 1)

        assertEquals("2026-11-01", year.startDate)
        assertEquals("2027-10-31", year.endDate)
    }

    @Test
    fun adjustableBoundarySupportsNonNovemberPolicy() {
        val customPolicy = MembershipYearPolicy(startMonth = 7, startDay = 1)
        val year = customPolicy.forDate(year = 2026, month = 6, dayOfMonth = 30)

        assertEquals("2025-07-01", year.startDate)
        assertEquals("2026-06-30", year.endDate)
    }
}
