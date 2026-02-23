package com.club.poc.feature.lodgecatalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val LODGE_CATALOG_ROUTE = "club_home"

@Composable
fun LodgeCatalogScreen(
    clubName: String,
    lodgesCount: Int,
    documentsCount: Int,
    bookingEnabled: Boolean,
    bookingBlockedMessage: String?,
    docsEnabled: Boolean,
    lodgesEnabled: Boolean,
    canManageBookings: Boolean,
    canManageDocuments: Boolean,
    onBooking: () -> Unit,
    onDocuments: () -> Unit,
    onCommittee: () -> Unit,
    onProfile: () -> Unit,
) {
    val isBookingEnabled = bookingEnabled && lodgesEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = clubName, style = MaterialTheme.typography.headlineSmall)
                Text(text = "Data-driven config + committee capability gating")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Lodges")
                    Text(text = lodgesCount.toString(), style = MaterialTheme.typography.headlineSmall)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Documents")
                    Text(text = documentsCount.toString(), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        AnimatedVisibility(
            visible = bookingBlockedMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = bookingBlockedMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = bookingBlockedMessage == null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Booking eligible",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Button(
            onClick = onBooking,
            enabled = isBookingEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) { Text("Booking") }
        Button(
            onClick = onDocuments,
            enabled = docsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) { Text("Documents") }
        OutlinedButton(
            onClick = onCommittee,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = "Committee  |  bookings:${if (canManageBookings) "Y" else "N"} docs:${if (canManageDocuments) "Y" else "N"}",
            )
        }
        OutlinedButton(
            onClick = onProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) { Text("Profile / Role Toggles") }
    }
}
