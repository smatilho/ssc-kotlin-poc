package com.club.poc.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingLifecycleDaoTest {
    private lateinit var database: ClubPocDatabase
    private lateinit var bedNightDao: BedNightDao
    private lateinit var bookingLifecycleDao: BookingLifecycleDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, ClubPocDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bedNightDao = database.bedNightDao()
        bookingLifecycleDao = database.bookingLifecycleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createHold_marksRequestedBedNightsHeld() = runTest {
        seedAvailableBedNights(
            listOf(
                availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10"),
                availableBedNight(id = "bn-2", bedId = "bed-b", nightDate = "2026-01-10"),
            ),
        )

        val result = bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-1",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1000L,
                expiresAtEpochMillis = 10_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                    HoldSelection(bedId = "bed-b", nightDate = "2026-01-10", guestMemberId = "guest-2"),
                ),
            ),
        )

        assertEquals(CreateHoldResult.Success(holdId = "hold-1", heldCount = 2), result)
        val heldNights = bedNightDao.listByHoldId("hold-1")
        assertEquals(2, heldNights.size)
        assertTrue(heldNights.all { it.status == BedNightRecordStatus.HELD.name })
        assertTrue(heldNights.all { it.guestMemberId != null })

        val hold = bookingLifecycleDao.fetchHoldById("hold-1")
        assertNotNull(hold)
        assertEquals(BookingHoldRecordStatus.ACTIVE.name, hold?.status)
    }

    @Test
    fun createHold_rejectsDuplicateGuestNightSelection() = runTest {
        seedAvailableBedNights(
            listOf(
                availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10"),
                availableBedNight(id = "bn-2", bedId = "bed-b", nightDate = "2026-01-10"),
            ),
        )

        val result = bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-dup",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1000L,
                expiresAtEpochMillis = 10_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                    HoldSelection(bedId = "bed-b", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                ),
            ),
        )

        assertEquals(
            CreateHoldResult.Failure(CreateHoldFailureReason.INVALID_GUEST_ASSIGNMENT),
            result,
        )
        assertNull(bookingLifecycleDao.fetchHoldById("hold-dup"))
        assertEquals(0, bedNightDao.listByHoldId("hold-dup").size)
    }

    @Test
    fun expireHoldIfPastDue_releasesHeldBedNights() = runTest {
        seedAvailableBedNights(
            listOf(
                availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10"),
            ),
        )
        bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-expire",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1000L,
                expiresAtEpochMillis = 5_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                ),
            ),
        )

        val expired = bookingLifecycleDao.expireHoldIfPastDue(
            holdId = "hold-expire",
            asOfEpochMillis = 6_000L,
        )

        assertTrue(expired)
        val hold = bookingLifecycleDao.fetchHoldById("hold-expire")
        assertEquals(BookingHoldRecordStatus.EXPIRED.name, hold?.status)

        val items = bedNightDao.listRange("2026-01-10", "2026-01-10")
        assertEquals(1, items.size)
        assertEquals(BedNightRecordStatus.AVAILABLE.name, items.first().status)
        assertNull(items.first().holdId)
        assertNull(items.first().guestMemberId)
    }

    @Test
    fun confirmHold_movesHeldNightsToBooking() = runTest {
        seedAvailableBedNights(
            listOf(
                availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10"),
                availableBedNight(id = "bn-2", bedId = "bed-b", nightDate = "2026-01-11"),
            ),
        )
        val holdResult = bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-confirm",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1_000L,
                expiresAtEpochMillis = 20_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                    HoldSelection(bedId = "bed-b", nightDate = "2026-01-11", guestMemberId = "guest-1"),
                ),
            ),
        )
        assertTrue(holdResult is CreateHoldResult.Success)

        val confirmResult = bookingLifecycleDao.confirmHold(
            ConfirmHoldCommand(
                holdId = "hold-confirm",
                bookingId = "booking-1",
                totalCents = 42_000,
                confirmedAtEpochMillis = 15_000L,
            ),
        )

        assertEquals(
            ConfirmHoldResult.Success(bookingId = "booking-1", bookedNights = 2),
            confirmResult,
        )
        assertEquals(BookingHoldRecordStatus.CONFIRMED.name, bookingLifecycleDao.fetchHoldById("hold-confirm")?.status)
        assertEquals(BookingRecordStatus.CONFIRMED.name, bookingLifecycleDao.fetchBookingById("booking-1")?.status)
        assertEquals(2, bookingLifecycleDao.fetchBookingNights("booking-1").size)

        val bookedNights = bedNightDao.listByBookingId("booking-1")
        assertEquals(2, bookedNights.size)
        assertTrue(bookedNights.all { it.status == BedNightRecordStatus.BOOKED.name })
        assertTrue(bookedNights.all { it.holdId == null })
        assertTrue(bookedNights.all { it.guestMemberId == "guest-1" })
    }

    @Test
    fun confirmHold_afterExpiryFailsAndExpiresHold() = runTest {
        seedAvailableBedNights(
            listOf(availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10")),
        )
        bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-expired",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1_000L,
                expiresAtEpochMillis = 2_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                ),
            ),
        )

        val confirmResult = bookingLifecycleDao.confirmHold(
            ConfirmHoldCommand(
                holdId = "hold-expired",
                bookingId = "booking-never",
                totalCents = 10_000,
                confirmedAtEpochMillis = 3_000L,
            ),
        )

        assertEquals(
            ConfirmHoldResult.Failure(ConfirmHoldFailureReason.HOLD_EXPIRED),
            confirmResult,
        )
        assertEquals(BookingHoldRecordStatus.EXPIRED.name, bookingLifecycleDao.fetchHoldById("hold-expired")?.status)
        assertNull(bookingLifecycleDao.fetchBookingById("booking-never"))

        val items = bedNightDao.listRange("2026-01-10", "2026-01-10")
        assertEquals(BedNightRecordStatus.AVAILABLE.name, items.single().status)
        assertNull(items.single().guestMemberId)
    }

    @Test
    fun cancelHold_releasesHeldBedNightsAndMarksCancelled() = runTest {
        seedAvailableBedNights(
            listOf(availableBedNight(id = "bn-1", bedId = "bed-a", nightDate = "2026-01-10")),
        )
        bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = "hold-cancel",
                clubId = "club-1",
                memberId = "member-1",
                createdAtEpochMillis = 1_000L,
                expiresAtEpochMillis = 5_000L,
                selections = listOf(
                    HoldSelection(bedId = "bed-a", nightDate = "2026-01-10", guestMemberId = "guest-1"),
                ),
            ),
        )

        val cancelled = bookingLifecycleDao.cancelHold("hold-cancel")

        assertTrue(cancelled)
        assertEquals(BookingHoldRecordStatus.CANCELLED.name, bookingLifecycleDao.fetchHoldById("hold-cancel")?.status)

        val items = bedNightDao.listRange("2026-01-10", "2026-01-10")
        assertEquals(BedNightRecordStatus.AVAILABLE.name, items.single().status)
        assertNull(items.single().holdId)
        assertNull(items.single().guestMemberId)
    }

    private suspend fun seedAvailableBedNights(items: List<BedNightEntity>) {
        bedNightDao.insertAll(items)
    }

    private fun availableBedNight(
        id: String,
        bedId: String,
        nightDate: String,
    ): BedNightEntity {
        return BedNightEntity(
            id = id,
            bedId = bedId,
            nightDate = nightDate,
            status = BedNightRecordStatus.AVAILABLE.name,
            holdId = null,
            bookingId = null,
            guestMemberId = null,
        )
    }
}
