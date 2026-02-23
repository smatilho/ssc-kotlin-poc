package com.club.poc.core.model

enum class BedNightStatus {
    AVAILABLE,
    HELD,
    BOOKED,
}

enum class BookingHoldStatus {
    ACTIVE,
    EXPIRED,
    CONFIRMED,
    CANCELLED,
}

enum class BookingStatus {
    CONFIRMED,
    CANCELLED,
}

data class BedNight(
    val bedId: String,
    val nightDate: String,
    val status: BedNightStatus,
    val guestMemberId: String? = null,
)

data class BookingHold(
    val holdId: String,
    val clubId: String,
    val memberId: String,
    val expiresAtEpochMillis: Long,
    val status: BookingHoldStatus,
)

data class BedNightSelection(
    val bedId: String,
    val nightDate: String,
    val guestMemberId: String,
)

data class Booking(
    val bookingId: String,
    val clubId: String,
    val memberId: String,
    val status: BookingStatus,
    val totalCents: Int,
    val confirmedAtEpochMillis: Long,
)
