package com.club.poc.core.model

import java.time.LocalDate

data class Club(
    val id: String,
    val slug: String,
    val name: String,
    val timezone: String,
    val currency: String,
)

data class ClubConfig(
    val clubId: String,
    val membershipStartMonth: Int,
    val membershipStartDay: Int,
    val holdMinutes: Int,
    val duesCents: Int,
)

data class MembershipYear(
    val startDate: String,
    val endDate: String,
)

class MembershipYearPolicy(
    private val startMonth: Int,
    private val startDay: Int,
) {
    fun forDate(year: Int, month: Int, dayOfMonth: Int): MembershipYear {
        return forDate(LocalDate.of(year, month, dayOfMonth))
    }

    fun forDate(date: LocalDate): MembershipYear {
        val startsThisYear =
            date.monthValue > startMonth || (date.monthValue == startMonth && date.dayOfMonth >= startDay)
        val startYear = if (startsThisYear) date.year else date.year - 1
        val start = LocalDate.of(startYear, startMonth, startDay)
        val endInclusive = start.plusYears(1).minusDays(1)
        return MembershipYear(startDate = start.toString(), endDate = endInclusive.toString())
    }
}
