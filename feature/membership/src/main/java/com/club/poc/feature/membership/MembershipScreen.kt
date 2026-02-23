package com.club.poc.feature.membership

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.club.poc.core.model.MembershipStatus

const val MEMBERSHIP_ROUTE = "membership"

@Composable
fun MembershipScreen(
    membershipStatus: MembershipStatus,
    onCycleMembershipStatus: () -> Unit,
    onContinue: () -> Unit,
) {
    val statusLabel = when (membershipStatus) {
        MembershipStatus.ACTIVE -> "ACTIVE - booking allowed"
        MembershipStatus.UNPAID -> "UNPAID - dues required"
        MembershipStatus.LAPSED -> "LAPSED - renewal required"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = "Membership Dues Gate", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Current membership year policy: Nov 1 -> Oct 31")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Eligibility Status", style = MaterialTheme.typography.titleMedium)
                AssistChip(
                    onClick = {},
                    label = { Text(statusLabel) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        OutlinedButton(onClick = onCycleMembershipStatus, modifier = Modifier.fillMaxWidth()) {
            Text("Cycle Status (test)")
        }
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue to Club Home")
        }
    }
}
