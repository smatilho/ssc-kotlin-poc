package com.club.poc.feature.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.club.poc.core.model.ClubDocumentSummary

const val DOCUMENTS_ROUTE = "documents"

@Composable
fun DocumentsScreen(
    docsEnabled: Boolean,
    canManageDocuments: Boolean,
    documents: List<ClubDocumentSummary>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = "Documents", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Club-configured document catalog")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Docs enabled: ${if (docsEnabled) "yes" else "no"}")
                Text(text = "Committee write access: ${if (canManageDocuments) "yes" else "no"}")
            }
        }

        if (docsEnabled) {
            if (documents.isEmpty()) {
                Text(text = "No documents configured for this club.")
            } else {
                documents.forEach { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = doc.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${doc.category}  |  ${doc.url}")
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Documents feature disabled by club config",
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Back") }
    }
}
