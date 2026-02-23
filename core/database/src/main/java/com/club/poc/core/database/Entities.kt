package com.club.poc.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BedNightRecordStatus {
    AVAILABLE,
    HELD,
    BOOKED,
}

enum class BookingHoldRecordStatus {
    ACTIVE,
    EXPIRED,
    CONFIRMED,
    CANCELLED,
}

enum class BookingRecordStatus {
    CONFIRMED,
    CANCELLED,
}

@Entity(tableName = "club_config")
data class ClubConfigEntity(
    @PrimaryKey @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "club_name") val clubName: String,
    @ColumnInfo(name = "membership_start_month") val membershipStartMonth: Int,
    @ColumnInfo(name = "membership_start_day") val membershipStartDay: Int,
    @ColumnInfo(name = "dues_cents") val duesCents: Int,
    @ColumnInfo(name = "hold_minutes") val holdMinutes: Int,
    @ColumnInfo(name = "docs_enabled") val docsEnabled: Boolean,
    @ColumnInfo(name = "assets_enabled") val assetsEnabled: Boolean,
    @ColumnInfo(name = "lodges_enabled") val lodgesEnabled: Boolean,
)

@Entity(tableName = "member_profiles")
data class MemberProfileEntity(
    @PrimaryKey @ColumnInfo(name = "member_id") val memberId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "membership_status") val membershipStatus: String,
    @ColumnInfo(name = "dues_paid_at") val duesPaidAt: String?,
)

@Entity(
    tableName = "member_roles",
    primaryKeys = ["member_id", "role_key"],
)
data class MemberRoleEntity(
    @ColumnInfo(name = "member_id") val memberId: String,
    @ColumnInfo(name = "role_key") val roleKey: String,
)

@Entity(
    tableName = "lodges",
    indices = [Index(value = ["club_id"])],
)
data class LodgeEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "bed_count") val bedCount: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(
    tableName = "documents",
    indices = [Index(value = ["club_id"])],
)
data class DocumentEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "url") val url: String,
)

@Entity(
    tableName = "bed_nights",
    indices = [
        Index(value = ["bed_id", "night_date"], unique = true),
        Index(value = ["guest_member_id", "night_date"], unique = true),
    ],
)
data class BedNightEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "bed_id") val bedId: String,
    @ColumnInfo(name = "night_date") val nightDate: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "hold_id") val holdId: String?,
    @ColumnInfo(name = "booking_id") val bookingId: String?,
    @ColumnInfo(name = "guest_member_id") val guestMemberId: String?,
)

@Entity(tableName = "booking_holds")
data class BookingHoldEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "member_id") val memberId: String,
    @ColumnInfo(name = "expires_at_epoch_millis") val expiresAtEpochMillis: Long,
    @ColumnInfo(name = "created_at_epoch_millis") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "status") val status: String,
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "club_id") val clubId: String,
    @ColumnInfo(name = "member_id") val memberId: String,
    @ColumnInfo(name = "total_cents") val totalCents: Int,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "confirmed_at_epoch_millis") val confirmedAtEpochMillis: Long,
)

@Entity(
    tableName = "booking_nights",
    indices = [
        Index(value = ["bed_id", "night_date"], unique = true),
        Index(value = ["guest_member_id", "night_date"], unique = true),
    ],
)
data class BookingNightEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "booking_id") val bookingId: String,
    @ColumnInfo(name = "bed_id") val bedId: String,
    @ColumnInfo(name = "night_date") val nightDate: String,
    @ColumnInfo(name = "guest_member_id") val guestMemberId: String,
)
