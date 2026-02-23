package com.club.poc.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ClubSyncScheduler {
    fun schedule(
        context: Context,
        clubId: String,
        apiBaseUrl: String,
    ) {
        val input = workDataOf(
            WORK_INPUT_CLUB_ID to clubId,
            WORK_INPUT_BASE_URL to apiBaseUrl,
        )
        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName("config-sync", clubId),
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ConfigSyncWorker>(6, TimeUnit.HOURS)
                .setInputData(input)
                .setConstraints(networkConnectedConstraint())
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName("hold-expiry-sync", clubId),
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HoldExpirySyncWorker>(15, TimeUnit.MINUTES)
                .setInputData(input)
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName("payment-reconcile", clubId),
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PaymentReconcileWorker>(1, TimeUnit.HOURS)
                .setInputData(input)
                .setConstraints(networkConnectedConstraint())
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName("document-cache", clubId),
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DocumentCacheWorker>(12, TimeUnit.HOURS)
                .setInputData(input)
                .setConstraints(networkConnectedConstraint())
                .build(),
        )
    }

    private fun uniqueWorkName(prefix: String, clubId: String): String {
        return "club-$clubId-$prefix"
    }

    private fun networkConnectedConstraint(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
