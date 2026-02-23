package com.club.poc.feature.inviteauth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val INVITE_AUTH_ROUTE = "invite_auth"

@Composable
fun InviteAuthScreen(onInviteAccepted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Club Access",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Invite-only authentication and distribution",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Gate Requirements", style = MaterialTheme.typography.titleLarge)
                RequirementLine("Google Play closed track install")
                RequirementLine("Valid invite token accepted by backend")
                RequirementLine("Google identity token exchange")
            }
        }

        Button(
            onClick = onInviteAccepted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text("Accept Invite and Continue")
        }
    }
}

@Composable
private fun RequirementLine(text: String) {
    Row(modifier = Modifier.padding(top = 10.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 8.dp)
                .size(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                ),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
