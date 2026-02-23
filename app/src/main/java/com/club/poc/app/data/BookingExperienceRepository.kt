package com.club.poc.app.data

import com.club.poc.app.BuildConfig
import com.club.poc.core.common.AppResult
import com.club.poc.core.database.BedNightDao
import com.club.poc.core.database.BedNightEntity
import com.club.poc.core.database.BookingLifecycleDao
import com.club.poc.core.database.ClubConfigDao
import com.club.poc.core.database.ConfirmHoldCommand
import com.club.poc.core.database.ConfirmHoldResult
import com.club.poc.core.database.CreateHoldCommand
import com.club.poc.core.database.CreateHoldResult
import com.club.poc.core.database.HoldSelection
import com.club.poc.core.model.PaymentIntent
import com.club.poc.core.payments.CheckoutIntents
import com.club.poc.core.payments.StripeCheckoutOrchestrator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

private const val DEFAULT_HOLD_MINUTES = 15

@Singleton
class BookingExperienceRepository @Inject constructor(
    private val clubConfigDao: ClubConfigDao,
    private val bedNightDao: BedNightDao,
    private val bookingLifecycleDao: BookingLifecycleDao,
    private val stripeCheckoutOrchestrator: StripeCheckoutOrchestrator,
) {
    fun observeBedNights(startDate: String, endDate: String): Flow<List<BedNightEntity>> {
        return bedNightDao.observeRange(startDate, endDate)
    }

    suspend fun createHold(
        holdId: String,
        clubId: String,
        memberId: String,
        selections: List<HoldSelection>,
        createdAtEpochMillis: Long,
    ): HoldCreationResult {
        val holdMinutes = clubConfigDao.get(clubId)?.holdMinutes ?: DEFAULT_HOLD_MINUTES
        val expiresAtEpochMillis = createdAtEpochMillis + holdMinutes * 60_000L

        val result = bookingLifecycleDao.createHold(
            CreateHoldCommand(
                holdId = holdId,
                clubId = clubId,
                memberId = memberId,
                createdAtEpochMillis = createdAtEpochMillis,
                expiresAtEpochMillis = expiresAtEpochMillis,
                selections = selections,
            ),
        )

        return HoldCreationResult(
            result = result,
            expiresAtEpochMillis = expiresAtEpochMillis,
            holdMinutes = holdMinutes,
        )
    }

    suspend fun confirmHold(
        holdId: String,
        bookingId: String,
        totalCents: Int,
        confirmedAtEpochMillis: Long,
    ): ConfirmHoldResult {
        return bookingLifecycleDao.confirmHold(
            ConfirmHoldCommand(
                holdId = holdId,
                bookingId = bookingId,
                totalCents = totalCents,
                confirmedAtEpochMillis = confirmedAtEpochMillis,
            ),
        )
    }

    suspend fun cancelHold(holdId: String): Boolean {
        return bookingLifecycleDao.cancelHold(holdId)
    }

    suspend fun createCheckout(
        clubId: String,
        memberId: String,
        holdId: String,
        duesRequired: Boolean,
        expectedBookingAmountCents: Int,
    ): CheckoutResult {
        if (isPlaceholderApiBaseUrl()) {
            return CheckoutResult(
                intents = createDemoCheckoutIntents(
                    clubId = clubId,
                    memberId = memberId,
                    holdId = holdId,
                    duesRequired = duesRequired,
                    expectedBookingAmountCents = expectedBookingAmountCents,
                ),
                mode = CheckoutMode.DEMO_FALLBACK,
                note = "Using demo Stripe mode (placeholder API base URL).",
            )
        }

        return when (
            val result = stripeCheckoutOrchestrator.createCheckout(
                clubId = clubId,
                memberId = memberId,
                holdId = holdId,
                duesRequired = duesRequired,
            ).first()
        ) {
            is AppResult.Success -> CheckoutResult(
                intents = result.data,
                mode = CheckoutMode.NETWORK,
                note = "Using network Stripe mode.",
            )

            is AppResult.Error -> CheckoutResult(
                intents = createDemoCheckoutIntents(
                    clubId = clubId,
                    memberId = memberId,
                    holdId = holdId,
                    duesRequired = duesRequired,
                    expectedBookingAmountCents = expectedBookingAmountCents,
                ),
                mode = CheckoutMode.DEMO_FALLBACK,
                note = "Network Stripe unavailable (${result.message}). Fallback demo intent created.",
            )
        }
    }

    private suspend fun createDemoCheckoutIntents(
        clubId: String,
        memberId: String,
        holdId: String,
        duesRequired: Boolean,
        expectedBookingAmountCents: Int,
    ): CheckoutIntents {
        val now = System.currentTimeMillis()
        val duesAmount = clubConfigDao.get(clubId)?.duesCents ?: 0

        val bookingIntent = PaymentIntent(
            id = "pi_demo_booking_$now",
            clientSecret = "demo_secret_booking_$holdId",
            amountCents = expectedBookingAmountCents,
        )
        val duesIntent = if (duesRequired) {
            PaymentIntent(
                id = "pi_demo_dues_$now",
                clientSecret = "demo_secret_dues_$memberId",
                amountCents = duesAmount,
            )
        } else {
            null
        }

        return CheckoutIntents(
            bookingIntent = bookingIntent,
            duesIntent = duesIntent,
        )
    }

    private fun isPlaceholderApiBaseUrl(): Boolean {
        return BuildConfig.API_BASE_URL.contains("example.com")
    }
}

data class HoldCreationResult(
    val result: CreateHoldResult,
    val expiresAtEpochMillis: Long,
    val holdMinutes: Int,
)

data class CheckoutResult(
    val intents: CheckoutIntents,
    val mode: CheckoutMode,
    val note: String?,
)

enum class CheckoutMode {
    NETWORK,
    DEMO_FALLBACK,
}
