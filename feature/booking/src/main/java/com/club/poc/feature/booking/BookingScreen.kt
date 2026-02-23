package com.club.poc.feature.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

const val BOOKING_ROUTE = "booking"

enum class BookingBedNightStatusUi {
    AVAILABLE,
    HELD,
    BOOKED,
}

data class BookingBedNightOptionUi(
    val bedId: String,
    val lodgeLabel: String,
    val nightDate: String,
    val displayNightDate: String,
    val status: BookingBedNightStatusUi,
    val isSelected: Boolean,
    val canSelect: Boolean,
)

data class BookingScreenUiState(
    val clubName: String,
    val windowLabel: String,
    val bedNightOptions: List<BookingBedNightOptionUi>,
    val selectedCount: Int,
    val holdId: String?,
    val holdExpiresAt: String?,
    val bookingId: String?,
    val stripePaymentIntentId: String?,
    val statusMessage: String?,
    val errorMessage: String?,
    val isBusy: Boolean,
    val canCreateHold: Boolean,
    val canConfirmPayment: Boolean,
    val canCancelHold: Boolean,
    val stripeModeLabel: String,
) {
    companion object {
        val Empty = BookingScreenUiState(
            clubName = "Club",
            windowLabel = "Inventory window",
            bedNightOptions = emptyList(),
            selectedCount = 0,
            holdId = null,
            holdExpiresAt = null,
            bookingId = null,
            stripePaymentIntentId = null,
            statusMessage = "Loading booking inventory...",
            errorMessage = null,
            isBusy = false,
            canCreateHold = false,
            canConfirmPayment = false,
            canCancelHold = false,
            stripeModeLabel = "Stripe booking payment",
        )
    }
}

@Composable
fun BookingScreen(
    uiState: BookingScreenUiState,
    onToggleSelection: (bedId: String, nightDate: String) -> Unit,
    onCreateHold: () -> Unit,
    onConfirmPayment: () -> Unit,
    onCancelHold: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = "Bed Booking Checkout", style = MaterialTheme.typography.headlineMedium)
            Text(text = uiState.clubName, style = MaterialTheme.typography.titleMedium)
            Text(text = uiState.windowLabel)
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uiState.stripeModeLabel,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        uiState.errorMessage?.let { error ->
            item {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        uiState.statusMessage?.let { status ->
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Selection Rules", style = MaterialTheme.typography.titleMedium)
                    Text(text = "One bed per person per night")
                    Text(text = "Selected nights: ${uiState.selectedCount}")
                    uiState.holdId?.let { holdId ->
                        Text(text = "Active hold: $holdId")
                        uiState.holdExpiresAt?.let { expires ->
                            Text(text = "Hold expires: $expires")
                        }
                    }
                    uiState.stripePaymentIntentId?.let { intent ->
                        Text(text = "Stripe intent: $intent")
                    }
                    uiState.bookingId?.let { booking ->
                        Text(text = "Confirmed booking: $booking")
                    }
                }
            }
        }

        if (uiState.bedNightOptions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No bed inventory seeded for this window yet.",
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        } else {
            val grouped = uiState.bedNightOptions.groupBy { it.displayNightDate }
            grouped.forEach { (nightLabel, options) ->
                item {
                    Text(
                        text = nightLabel,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(options, key = { option -> "${option.nightDate}-${option.bedId}" }) { option ->
                    BedNightOptionRow(
                        option = option,
                        onToggleSelection = onToggleSelection,
                    )
                }
            }
        }

        item {
            Button(
                onClick = onCreateHold,
                enabled = uiState.canCreateHold,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isBusy) "Working..." else "Create Hold")
            }
        }

        item {
            Button(
                onClick = onConfirmPayment,
                enabled = uiState.canConfirmPayment,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.bookingId == null) "Process Stripe Payment + Confirm" else "Booking Confirmed")
            }
        }

        item {
            OutlinedButton(
                onClick = onCancelHold,
                enabled = uiState.canCancelHold,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel Active Hold")
            }
        }

        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun BedNightOptionRow(
    option: BookingBedNightOptionUi,
    onToggleSelection: (bedId: String, nightDate: String) -> Unit,
) {
    val colors = when (option.status) {
        BookingBedNightStatusUi.AVAILABLE -> CardDefaults.outlinedCardColors()
        BookingBedNightStatusUi.HELD -> CardDefaults.outlinedCardColors(containerColor = Color(0xFFFDF3DC))
        BookingBedNightStatusUi.BOOKED -> CardDefaults.outlinedCardColors(containerColor = Color(0xFFF4F4F7))
    }

    OutlinedCard(
        colors = colors,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = option.lodgeLabel, style = MaterialTheme.typography.labelLarge)
                Text(text = option.bedId.uppercase(), style = MaterialTheme.typography.titleMedium)
            }

            StatusPill(option.status)

            Spacer(modifier = Modifier.weight(0.1f))

            OutlinedButton(
                onClick = {
                    onToggleSelection(option.bedId, option.nightDate)
                },
                enabled = option.canSelect || option.isSelected,
            ) {
                Text(
                    when {
                        option.isSelected -> "Selected"
                        option.status == BookingBedNightStatusUi.AVAILABLE -> "Select"
                        option.status == BookingBedNightStatusUi.HELD -> "Held"
                        else -> "Booked"
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: BookingBedNightStatusUi) {
    val (label, color, textColor) = when (status) {
        BookingBedNightStatusUi.AVAILABLE -> Triple("AVAILABLE", Color(0xFFDFF4E7), Color(0xFF0E5B2A))
        BookingBedNightStatusUi.HELD -> Triple("HELD", Color(0xFFFFF0D9), Color(0xFF7D5100))
        BookingBedNightStatusUi.BOOKED -> Triple("BOOKED", Color(0xFFEAEAF0), Color(0xFF3D3D4A))
    }

    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text = label,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
