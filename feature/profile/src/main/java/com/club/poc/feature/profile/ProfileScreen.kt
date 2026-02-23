package com.club.poc.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.MembershipStatus
import com.club.poc.core.model.MembershipYear

const val PROFILE_ROUTE = "profile"

@Composable
fun ProfileScreen(
    membershipStatus: MembershipStatus,
    membershipYear: MembershipYear,
    roles: Set<CommitteeRole>,
    docsEnabled: Boolean,
    lodgesEnabled: Boolean,
    onCycleMembershipStatus: () -> Unit,
    onToggleReservationist: (Boolean) -> Unit,
    onToggleDocsCommittee: (Boolean) -> Unit,
    onToggleDocsEnabled: () -> Unit,
    onToggleLodgesEnabled: () -> Unit,
    onBack: () -> Unit,
) {
    val hasReservationist = CommitteeRole.RESERVATIONIST in roles
    val hasDocsCommittee = CommitteeRole.DOCS_COMMITTEE in roles

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Role & Feature Controls", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Membership status: ${membershipStatus.name}")
        Text(text = "Membership year: ${membershipYear.startDate} -> ${membershipYear.endDate}")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Committee Roles", style = MaterialTheme.typography.titleMedium)
                RoleSwitchRow(
                    label = "Reservationist",
                    checked = hasReservationist,
                    onCheckedChange = onToggleReservationist,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                RoleSwitchRow(
                    label = "Docs Committee",
                    checked = hasDocsCommittee,
                    onCheckedChange = onToggleDocsCommittee,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Club Feature Flags", style = MaterialTheme.typography.titleMedium)
                RoleSwitchRow(
                    label = "Documents",
                    checked = docsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled != docsEnabled) {
                            onToggleDocsEnabled()
                        }
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                RoleSwitchRow(
                    label = "Lodges",
                    checked = lodgesEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled != lodgesEnabled) {
                            onToggleLodgesEnabled()
                        }
                    },
                )
            }
        }

        Button(onClick = onCycleMembershipStatus, modifier = Modifier.fillMaxWidth()) {
            Text("Cycle Membership Status")
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
    }
}

@Composable
private fun RoleSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
