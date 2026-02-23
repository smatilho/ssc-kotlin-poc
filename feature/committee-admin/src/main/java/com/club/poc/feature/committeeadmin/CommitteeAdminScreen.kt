package com.club.poc.feature.committeeadmin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.club.poc.core.model.CommitteeRole

const val COMMITTEE_ADMIN_ROUTE = "committee_admin"

@Composable
fun CommitteeAdminScreen(
    roles: Set<CommitteeRole>,
    canManageBookings: Boolean,
    canManageDocuments: Boolean,
    onBack: () -> Unit,
) {
    val sortedRoles = roles.map { it.name }.sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Committee Admin", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Role-driven capability matrix")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Roles")
                if (sortedRoles.isEmpty()) {
                    Text(text = "none")
                } else {
                    sortedRoles.forEach { role ->
                        AssistChip(
                            onClick = {},
                            label = { Text(role) },
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Reservation Overrides")
                Text(
                    text = if (canManageBookings) "Allowed" else "Blocked",
                    color = if (canManageBookings) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Document Writes")
                Text(
                    text = if (canManageDocuments) "Allowed" else "Blocked",
                    color = if (canManageDocuments) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                )
            }
        }

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}
