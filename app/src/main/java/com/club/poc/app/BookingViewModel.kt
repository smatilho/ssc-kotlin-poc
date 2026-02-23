package com.club.poc.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.club.poc.app.data.BookingExperienceRepository
import com.club.poc.app.data.CheckoutMode
import com.club.poc.core.database.BedNightEntity
import com.club.poc.core.database.BedNightRecordStatus
import com.club.poc.core.database.ConfirmHoldFailureReason
import com.club.poc.core.database.ConfirmHoldResult
import com.club.poc.core.database.CreateHoldFailureReason
import com.club.poc.core.database.CreateHoldResult
import com.club.poc.core.database.HoldSelection
import com.club.poc.feature.booking.BookingBedNightOptionUi
import com.club.poc.feature.booking.BookingBedNightStatusUi
import com.club.poc.feature.booking.BookingScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val BED_NIGHT_RATE_CENTS = 9500
private const val INITIAL_STRIPE_MODE_LABEL = "Stripe checkout mode: pending"

private data class ActiveHoldState(
    val holdId: String,
    val expiresAtEpochMillis: Long,
    val heldCount: Int,
)

private data class BookingUiDraft(
    val availability: List<BedNightEntity>,
    val selections: Map<String, String>,
    val hold: ActiveHoldState?,
    val booking: String?,
    val paymentIntentId: String?,
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingExperienceRepository,
) : ViewModel() {
    private val clubId = BuildConfig.BOOTSTRAP_CLUB_ID
    private val memberId = BuildConfig.BOOTSTRAP_MEMBER_ID

    private val windowStartDate = LocalDate.now().plusDays(10)
    private val windowEndDate = windowStartDate.plusDays(2)
    private val dateDisplayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val expiryDisplayFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    private val selectedByNight = MutableStateFlow<Map<String, String>>(emptyMap())
    private val activeHold = MutableStateFlow<ActiveHoldState?>(null)
    private val bookingId = MutableStateFlow<String?>(null)
    private val stripePaymentIntentId = MutableStateFlow<String?>(null)
    private val stripeModeLabel = MutableStateFlow(INITIAL_STRIPE_MODE_LABEL)
    private val isBusy = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(
        "Select one bed per night, then create hold.",
    )
    private val errorMessage = MutableStateFlow<String?>(null)

    private val bedNights = repository.observeBedNights(
        startDate = windowStartDate.format(DateTimeFormatter.ISO_DATE),
        endDate = windowEndDate.format(DateTimeFormatter.ISO_DATE),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val bookingDraft = combine(
        bedNights,
        selectedByNight,
        activeHold,
        bookingId,
        stripePaymentIntentId,
    ) { availability, selections, hold, booking, paymentIntentId ->
        BookingUiDraft(
            availability = availability,
            selections = selections,
            hold = hold,
            booking = booking,
            paymentIntentId = paymentIntentId,
        )
    }

    val uiState: StateFlow<BookingScreenUiState> = combine(
        bookingDraft,
        isBusy,
        stripeModeLabel,
        statusMessage,
        errorMessage,
    ) { draft, busy, paymentModeLabel, status, error ->
        val availability = draft.availability
        val selections = draft.selections
        val hold = draft.hold
        val booking = draft.booking
        val paymentIntentId = draft.paymentIntentId

        val options = availability.map { bedNight ->
            val mappedStatus = bedNight.status.toUiStatus()
            BookingBedNightOptionUi(
                bedId = bedNight.bedId,
                lodgeLabel = lodgeLabelForBed(bedNight.bedId),
                nightDate = bedNight.nightDate,
                displayNightDate = runCatching {
                    LocalDate.parse(bedNight.nightDate).format(dateDisplayFormatter)
                }.getOrDefault(bedNight.nightDate),
                status = mappedStatus,
                isSelected = selections[bedNight.nightDate] == bedNight.bedId,
                canSelect = hold == null && booking == null && mappedStatus == BookingBedNightStatusUi.AVAILABLE,
            )
        }

        val holdExpiresAtText = hold?.let {
            val instant = Instant.ofEpochMilli(it.expiresAtEpochMillis)
            instant.atZone(ZoneId.systemDefault()).format(expiryDisplayFormatter)
        }

        BookingScreenUiState(
            clubName = "North Ridge Alpine Club",
            windowLabel = "Inventory window: ${windowStartDate.format(dateDisplayFormatter)} -> ${windowEndDate.format(dateDisplayFormatter)}",
            bedNightOptions = options,
            selectedCount = selections.size,
            holdId = hold?.holdId,
            holdExpiresAt = holdExpiresAtText,
            bookingId = booking,
            stripePaymentIntentId = paymentIntentId,
            statusMessage = status,
            errorMessage = error,
            isBusy = busy,
            canCreateHold = hold == null && booking == null && !busy && selections.isNotEmpty(),
            canConfirmPayment = hold != null && booking == null && !busy,
            canCancelHold = hold != null && booking == null && !busy,
            stripeModeLabel = paymentModeLabel,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BookingScreenUiState.Empty,
    )

    fun toggleBedSelection(
        bedId: String,
        nightDate: String,
    ) {
        if (activeHold.value != null || bookingId.value != null) return

        val selectedOption = bedNights.value.firstOrNull {
            it.bedId == bedId && it.nightDate == nightDate
        } ?: return
        if (selectedOption.status != BedNightRecordStatus.AVAILABLE.name) return

        selectedByNight.value = selectedByNight.value.toMutableMap().apply {
            if (this[nightDate] == bedId) {
                remove(nightDate)
            } else {
                this[nightDate] = bedId
            }
        }
        statusMessage.value = "Selected ${selectedByNight.value.size} bed-night(s)."
        errorMessage.value = null
    }

    fun createHold() {
        if (activeHold.value != null || bookingId.value != null || isBusy.value) return
        val selectionMap = selectedByNight.value
        if (selectionMap.isEmpty()) {
            errorMessage.value = "Select at least one bed-night before creating hold."
            return
        }

        viewModelScope.launch {
            isBusy.value = true
            errorMessage.value = null

            val holdId = "hold-${System.currentTimeMillis()}"
            val createdAtEpochMillis = System.currentTimeMillis()
            val selections = selectionMap.map { (nightDate, bedId) ->
                HoldSelection(
                    bedId = bedId,
                    nightDate = nightDate,
                    guestMemberId = memberId,
                )
            }

            val result = repository.createHold(
                holdId = holdId,
                clubId = clubId,
                memberId = memberId,
                selections = selections,
                createdAtEpochMillis = createdAtEpochMillis,
            )

            when (val holdResult = result.result) {
                is CreateHoldResult.Success -> {
                    activeHold.value = ActiveHoldState(
                        holdId = holdResult.holdId,
                        expiresAtEpochMillis = result.expiresAtEpochMillis,
                        heldCount = holdResult.heldCount,
                    )
                    selectedByNight.value = emptyMap()
                    stripePaymentIntentId.value = null
                    stripeModeLabel.value = INITIAL_STRIPE_MODE_LABEL
                    statusMessage.value = "Hold created (${holdResult.heldCount} nights). Continue to Stripe payment step."
                }

                is CreateHoldResult.Failure -> {
                    errorMessage.value = holdResult.reason.toUserMessage()
                }
            }
            isBusy.value = false
        }
    }

    fun processStripeAndConfirm() {
        val hold = activeHold.value ?: return
        if (bookingId.value != null || isBusy.value) return

        viewModelScope.launch {
            isBusy.value = true
            errorMessage.value = null
            statusMessage.value = "Preparing Stripe checkout..."

            val totalCents = hold.heldCount * BED_NIGHT_RATE_CENTS
            val checkout = repository.createCheckout(
                clubId = clubId,
                memberId = memberId,
                holdId = hold.holdId,
                duesRequired = false,
                expectedBookingAmountCents = totalCents,
            )
            val paymentIntentId = checkout.intents.bookingIntent.id
            stripePaymentIntentId.value = paymentIntentId
            stripeModeLabel.value = checkout.mode.toUserLabel()
            statusMessage.value = checkout.note ?: "Stripe checkout intent ready. Confirming booking..."

            val generatedBookingId = "booking-${System.currentTimeMillis()}"
            val result = repository.confirmHold(
                holdId = hold.holdId,
                bookingId = generatedBookingId,
                totalCents = totalCents,
                confirmedAtEpochMillis = System.currentTimeMillis(),
            )

            when (result) {
                is ConfirmHoldResult.Success -> {
                    bookingId.value = result.bookingId
                    statusMessage.value = "Stripe intent $paymentIntentId confirmed. Booking complete (${result.bookedNights} nights)."
                }

                is ConfirmHoldResult.Failure -> {
                    errorMessage.value = result.reason.toUserMessage()
                }
            }

            isBusy.value = false
        }
    }

    fun cancelHold() {
        val hold = activeHold.value ?: return
        if (bookingId.value != null || isBusy.value) return

        viewModelScope.launch {
            isBusy.value = true
            errorMessage.value = null

            val cancelled = repository.cancelHold(hold.holdId)
            if (cancelled) {
                activeHold.value = null
                stripePaymentIntentId.value = null
                stripeModeLabel.value = INITIAL_STRIPE_MODE_LABEL
                statusMessage.value = "Hold cancelled. Inventory released."
            } else {
                errorMessage.value = "Unable to cancel hold. It may already be expired/confirmed."
            }

            isBusy.value = false
        }
    }

    private fun String.toUiStatus(): BookingBedNightStatusUi {
        return when (this) {
            BedNightRecordStatus.AVAILABLE.name -> BookingBedNightStatusUi.AVAILABLE
            BedNightRecordStatus.HELD.name -> BookingBedNightStatusUi.HELD
            BedNightRecordStatus.BOOKED.name -> BookingBedNightStatusUi.BOOKED
            else -> BookingBedNightStatusUi.BOOKED
        }
    }

    private fun CreateHoldFailureReason.toUserMessage(): String {
        return when (this) {
            CreateHoldFailureReason.EMPTY_SELECTION -> "No selection sent for hold creation."
            CreateHoldFailureReason.INVALID_GUEST_ASSIGNMENT ->
                "Invalid assignment. Rule: one bed per person per night."

            CreateHoldFailureReason.BED_NIGHT_UNAVAILABLE ->
                "One or more bed-nights are no longer available. Refresh and retry."
        }
    }

    private fun ConfirmHoldFailureReason.toUserMessage(): String {
        return when (this) {
            ConfirmHoldFailureReason.HOLD_NOT_FOUND -> "Hold not found. Create a new hold."
            ConfirmHoldFailureReason.HOLD_NOT_ACTIVE -> "Hold is not active. Create a new hold."
            ConfirmHoldFailureReason.HOLD_EXPIRED -> "Hold expired before confirmation. Create a new hold."
            ConfirmHoldFailureReason.NO_HELD_NIGHTS -> "Hold contains no held bed-nights."
        }
    }

    private fun lodgeLabelForBed(bedId: String): String {
        return when {
            bedId.startsWith("ridge-") -> "Ridge House"
            bedId.startsWith("powder-") -> "Powder Lodge"
            else -> "Club Lodge"
        }
    }

    private fun CheckoutMode.toUserLabel(): String {
        return when (this) {
            CheckoutMode.NETWORK -> "Stripe checkout mode: network"
            CheckoutMode.DEMO_FALLBACK -> "Stripe checkout mode: demo fallback"
        }
    }
}
