package com.club.poc.core.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubConfigDao {
    @Query("SELECT * FROM club_config WHERE club_id = :clubId")
    fun observe(clubId: String): Flow<ClubConfigEntity?>

    @Query("SELECT * FROM club_config WHERE club_id = :clubId")
    suspend fun get(clubId: String): ClubConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ClubConfigEntity)

    @Query(
        "UPDATE club_config " +
            "SET docs_enabled = :docsEnabled, assets_enabled = :assetsEnabled, lodges_enabled = :lodgesEnabled " +
            "WHERE club_id = :clubId",
    )
    suspend fun updateFeatureFlags(
        clubId: String,
        docsEnabled: Boolean,
        assetsEnabled: Boolean,
        lodgesEnabled: Boolean,
    )
}

@Dao
interface MembershipDao {
    @Query("SELECT * FROM member_profiles WHERE member_id = :memberId")
    fun observe(memberId: String): Flow<MemberProfileEntity?>

    @Query("SELECT * FROM member_profiles WHERE member_id = :memberId")
    suspend fun get(memberId: String): MemberProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemberProfileEntity)

    @Query("UPDATE member_profiles SET membership_status = :membershipStatus WHERE member_id = :memberId")
    suspend fun updateMembershipStatus(memberId: String, membershipStatus: String): Int
}

@Dao
interface MemberRoleDao {
    @Query("SELECT * FROM member_roles WHERE member_id = :memberId ORDER BY role_key")
    fun observe(memberId: String): Flow<List<MemberRoleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemberRoleEntity)

    @Query("DELETE FROM member_roles WHERE member_id = :memberId AND role_key = :roleKey")
    suspend fun delete(memberId: String, roleKey: String): Int
}

@Dao
interface LodgeDao {
    @Query("SELECT * FROM lodges WHERE club_id = :clubId ORDER BY sort_order, name")
    fun observe(clubId: String): Flow<List<LodgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LodgeEntity>)

    @Query("DELETE FROM lodges WHERE club_id = :clubId")
    suspend fun deleteForClub(clubId: String)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE club_id = :clubId ORDER BY category, title")
    fun observe(clubId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DocumentEntity>)

    @Query("DELETE FROM documents WHERE club_id = :clubId")
    suspend fun deleteForClub(clubId: String)
}

@Dao
interface BedNightDao {
    @Query(
        "SELECT * FROM bed_nights " +
            "WHERE night_date BETWEEN :startDate AND :endDate " +
            "ORDER BY night_date, bed_id",
    )
    fun observeRange(startDate: String, endDate: String): Flow<List<BedNightEntity>>

    @Query("SELECT * FROM bed_nights WHERE night_date BETWEEN :startDate AND :endDate ORDER BY night_date, bed_id")
    suspend fun listRange(startDate: String, endDate: String): List<BedNightEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<BedNightEntity>)

    @Query(
        "UPDATE bed_nights " +
            "SET status = :nextStatus, hold_id = NULL, booking_id = NULL, guest_member_id = NULL " +
            "WHERE hold_id = :holdId",
    )
    suspend fun releaseHold(
        holdId: String,
        nextStatus: String = BedNightRecordStatus.AVAILABLE.name,
    )

    @Query("SELECT * FROM bed_nights WHERE hold_id = :holdId ORDER BY night_date, bed_id")
    suspend fun listByHoldId(holdId: String): List<BedNightEntity>

    @Query("SELECT * FROM bed_nights WHERE booking_id = :bookingId ORDER BY night_date, bed_id")
    suspend fun listByBookingId(bookingId: String): List<BedNightEntity>
}

data class HoldSelection(
    val bedId: String,
    val nightDate: String,
    val guestMemberId: String,
)

data class CreateHoldCommand(
    val holdId: String,
    val clubId: String,
    val memberId: String,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val selections: List<HoldSelection>,
)

data class ConfirmHoldCommand(
    val holdId: String,
    val bookingId: String,
    val totalCents: Int,
    val confirmedAtEpochMillis: Long,
)

enum class CreateHoldFailureReason {
    EMPTY_SELECTION,
    INVALID_GUEST_ASSIGNMENT,
    BED_NIGHT_UNAVAILABLE,
}

sealed interface CreateHoldResult {
    data class Success(val holdId: String, val heldCount: Int) : CreateHoldResult
    data class Failure(val reason: CreateHoldFailureReason) : CreateHoldResult
}

enum class ConfirmHoldFailureReason {
    HOLD_NOT_FOUND,
    HOLD_NOT_ACTIVE,
    HOLD_EXPIRED,
    NO_HELD_NIGHTS,
}

sealed interface ConfirmHoldResult {
    data class Success(val bookingId: String, val bookedNights: Int) : ConfirmHoldResult
    data class Failure(val reason: ConfirmHoldFailureReason) : ConfirmHoldResult
}

@Dao
abstract class BookingLifecycleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertHold(entity: BookingHoldEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertBooking(entity: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertBookingNights(entities: List<BookingNightEntity>)

    @Query("SELECT * FROM booking_holds WHERE id = :holdId")
    protected abstract suspend fun getHoldById(holdId: String): BookingHoldEntity?

    @Query("SELECT * FROM booking_holds WHERE id = :holdId")
    abstract suspend fun fetchHoldById(holdId: String): BookingHoldEntity?

    @Query(
        "SELECT id FROM booking_holds " +
            "WHERE status = :activeStatus AND expires_at_epoch_millis <= :asOfEpochMillis " +
            "ORDER BY expires_at_epoch_millis ASC",
    )
    abstract suspend fun listActiveHoldIdsDueForExpiry(
        asOfEpochMillis: Long,
        activeStatus: String = BookingHoldRecordStatus.ACTIVE.name,
    ): List<String>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    abstract suspend fun fetchBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM booking_nights WHERE booking_id = :bookingId ORDER BY night_date, bed_id")
    abstract suspend fun fetchBookingNights(bookingId: String): List<BookingNightEntity>

    @Query(
        "SELECT * FROM bed_nights " +
            "WHERE hold_id = :holdId AND status = :heldStatus " +
            "ORDER BY night_date, bed_id",
    )
    protected abstract suspend fun getHeldBedNightsForHold(
        holdId: String,
        heldStatus: String = BedNightRecordStatus.HELD.name,
    ): List<BedNightEntity>

    @Query(
        "UPDATE bed_nights " +
            "SET status = :heldStatus, hold_id = :holdId, booking_id = NULL, guest_member_id = :guestMemberId " +
            "WHERE bed_id = :bedId AND night_date = :nightDate " +
            "AND status = :availableStatus AND hold_id IS NULL AND booking_id IS NULL",
    )
    protected abstract suspend fun moveBedNightToHeld(
        holdId: String,
        bedId: String,
        nightDate: String,
        guestMemberId: String,
        heldStatus: String = BedNightRecordStatus.HELD.name,
        availableStatus: String = BedNightRecordStatus.AVAILABLE.name,
    ): Int

    @Query(
        "UPDATE bed_nights " +
            "SET status = :availableStatus, hold_id = NULL, booking_id = NULL, guest_member_id = NULL " +
            "WHERE hold_id = :holdId AND status = :heldStatus",
    )
    protected abstract suspend fun releaseHeldBedNights(
        holdId: String,
        availableStatus: String = BedNightRecordStatus.AVAILABLE.name,
        heldStatus: String = BedNightRecordStatus.HELD.name,
    ): Int

    @Query(
        "UPDATE bed_nights " +
            "SET status = :bookedStatus, booking_id = :bookingId, hold_id = NULL " +
            "WHERE hold_id = :holdId AND status = :heldStatus",
    )
    protected abstract suspend fun moveHeldBedNightsToBooked(
        holdId: String,
        bookingId: String,
        bookedStatus: String = BedNightRecordStatus.BOOKED.name,
        heldStatus: String = BedNightRecordStatus.HELD.name,
    ): Int

    @Query("DELETE FROM booking_holds WHERE id = :holdId")
    protected abstract suspend fun deleteHoldById(holdId: String)

    @Query(
        "UPDATE booking_holds SET status = :newStatus " +
            "WHERE id = :holdId AND status = :expectedStatus",
    )
    protected abstract suspend fun transitionHoldStatus(
        holdId: String,
        expectedStatus: String,
        newStatus: String,
    ): Int

    @Transaction
    open suspend fun createHold(command: CreateHoldCommand): CreateHoldResult {
        if (command.selections.isEmpty()) {
            return CreateHoldResult.Failure(CreateHoldFailureReason.EMPTY_SELECTION)
        }

        val duplicateGuestNight = command.selections
            .groupBy { selection -> selection.guestMemberId to selection.nightDate }
            .values
            .any { group -> group.size > 1 }
        if (duplicateGuestNight) {
            return CreateHoldResult.Failure(CreateHoldFailureReason.INVALID_GUEST_ASSIGNMENT)
        }

        try {
            insertHold(
                BookingHoldEntity(
                    id = command.holdId,
                    clubId = command.clubId,
                    memberId = command.memberId,
                    createdAtEpochMillis = command.createdAtEpochMillis,
                    expiresAtEpochMillis = command.expiresAtEpochMillis,
                    status = BookingHoldRecordStatus.ACTIVE.name,
                ),
            )

            command.selections.forEach { selection ->
                val updated = moveBedNightToHeld(
                    holdId = command.holdId,
                    bedId = selection.bedId,
                    nightDate = selection.nightDate,
                    guestMemberId = selection.guestMemberId,
                )
                if (updated != 1) {
                    throw IllegalStateException("Bed night unavailable")
                }
            }
        } catch (constraintException: SQLiteConstraintException) {
            cleanupFailedHold(command.holdId)
            return CreateHoldResult.Failure(CreateHoldFailureReason.INVALID_GUEST_ASSIGNMENT)
        } catch (stateException: IllegalStateException) {
            cleanupFailedHold(command.holdId)
            return CreateHoldResult.Failure(CreateHoldFailureReason.BED_NIGHT_UNAVAILABLE)
        }

        return CreateHoldResult.Success(
            holdId = command.holdId,
            heldCount = command.selections.size,
        )
    }

    @Transaction
    open suspend fun expireHoldIfPastDue(holdId: String, asOfEpochMillis: Long): Boolean {
        val hold = getHoldById(holdId) ?: return false
        if (hold.status != BookingHoldRecordStatus.ACTIVE.name) return false
        if (asOfEpochMillis < hold.expiresAtEpochMillis) return false

        val transitioned = transitionHoldStatus(
            holdId = holdId,
            expectedStatus = BookingHoldRecordStatus.ACTIVE.name,
            newStatus = BookingHoldRecordStatus.EXPIRED.name,
        )
        if (transitioned == 0) return false

        releaseHeldBedNights(holdId = holdId)
        return true
    }

    @Transaction
    open suspend fun cancelHold(holdId: String): Boolean {
        val hold = getHoldById(holdId) ?: return false
        if (hold.status != BookingHoldRecordStatus.ACTIVE.name) return false

        val transitioned = transitionHoldStatus(
            holdId = holdId,
            expectedStatus = BookingHoldRecordStatus.ACTIVE.name,
            newStatus = BookingHoldRecordStatus.CANCELLED.name,
        )
        if (transitioned == 0) return false

        releaseHeldBedNights(holdId = holdId)
        return true
    }

    @Transaction
    open suspend fun confirmHold(command: ConfirmHoldCommand): ConfirmHoldResult {
        val hold = getHoldById(command.holdId)
            ?: return ConfirmHoldResult.Failure(ConfirmHoldFailureReason.HOLD_NOT_FOUND)
        if (hold.status != BookingHoldRecordStatus.ACTIVE.name) {
            return ConfirmHoldResult.Failure(ConfirmHoldFailureReason.HOLD_NOT_ACTIVE)
        }
        if (command.confirmedAtEpochMillis > hold.expiresAtEpochMillis) {
            expireHoldIfPastDue(command.holdId, command.confirmedAtEpochMillis)
            return ConfirmHoldResult.Failure(ConfirmHoldFailureReason.HOLD_EXPIRED)
        }

        val heldBedNights = getHeldBedNightsForHold(command.holdId)
        if (heldBedNights.isEmpty()) {
            return ConfirmHoldResult.Failure(ConfirmHoldFailureReason.NO_HELD_NIGHTS)
        }

        insertBooking(
            BookingEntity(
                id = command.bookingId,
                clubId = hold.clubId,
                memberId = hold.memberId,
                totalCents = command.totalCents,
                status = BookingRecordStatus.CONFIRMED.name,
                confirmedAtEpochMillis = command.confirmedAtEpochMillis,
            ),
        )

        insertBookingNights(
            heldBedNights.map { bedNight ->
                val guestMemberId = bedNight.guestMemberId ?: hold.memberId
                BookingNightEntity(
                    id = "${command.bookingId}-${bedNight.bedId}-${bedNight.nightDate}-$guestMemberId",
                    bookingId = command.bookingId,
                    bedId = bedNight.bedId,
                    nightDate = bedNight.nightDate,
                    guestMemberId = guestMemberId,
                )
            },
        )

        val movedCount = moveHeldBedNightsToBooked(
            holdId = command.holdId,
            bookingId = command.bookingId,
        )
        if (movedCount != heldBedNights.size) {
            throw IllegalStateException("Held bed nights changed during confirmation")
        }

        val transitioned = transitionHoldStatus(
            holdId = command.holdId,
            expectedStatus = BookingHoldRecordStatus.ACTIVE.name,
            newStatus = BookingHoldRecordStatus.CONFIRMED.name,
        )
        if (transitioned == 0) {
            throw IllegalStateException("Unable to transition hold to confirmed")
        }

        return ConfirmHoldResult.Success(
            bookingId = command.bookingId,
            bookedNights = heldBedNights.size,
        )
    }

    private suspend fun cleanupFailedHold(holdId: String) {
        releaseHeldBedNights(holdId = holdId)
        deleteHoldById(holdId)
    }
}
