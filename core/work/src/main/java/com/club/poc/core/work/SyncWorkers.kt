package com.club.poc.core.work

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.club.poc.core.database.CLUB_POC_DATABASE_NAME
import com.club.poc.core.database.ClubConfigEntity
import com.club.poc.core.database.ClubPocDatabase
import com.club.poc.core.network.ClubApi
import com.club.poc.core.network.NetworkFactory
import java.io.IOException

const val WORK_INPUT_CLUB_ID = "work_input_club_id"
const val WORK_INPUT_BASE_URL = "work_input_base_url"

private const val WORK_OUTPUT_SYNCED_AT_EPOCH_MILLIS = "work_output_synced_at_epoch_millis"
private const val WORK_OUTPUT_EXPIRED_HOLD_COUNT = "work_output_expired_hold_count"
private const val WORK_OUTPUT_RECONCILED_AT_EPOCH_MILLIS = "work_output_reconciled_at_epoch_millis"
private const val WORK_OUTPUT_CACHED_AT_EPOCH_MILLIS = "work_output_cached_at_epoch_millis"

class ConfigSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val clubId = inputData.getRequiredString(WORK_INPUT_CLUB_ID) ?: return Result.failure()
        val baseUrl = inputData.getRequiredString(WORK_INPUT_BASE_URL) ?: return Result.failure()

        val database = openDatabase(applicationContext)
        return try {
            val api = createClubApi(baseUrl)
            val config = api.getClubConfig(clubId)
            database.clubConfigDao().upsert(
                ClubConfigEntity(
                    clubId = config.clubId,
                    clubName = config.clubId,
                    membershipStartMonth = config.membershipStartMonth,
                    membershipStartDay = config.membershipStartDay,
                    duesCents = config.duesCents,
                    holdMinutes = config.holdMinutes,
                    docsEnabled = true,
                    assetsEnabled = true,
                    lodgesEnabled = true,
                ),
            )
            Result.success(workDataOf(WORK_OUTPUT_SYNCED_AT_EPOCH_MILLIS to System.currentTimeMillis()))
        } catch (throwable: Throwable) {
            throwable.toWorkerResult()
        } finally {
            database.close()
        }
    }
}

class HoldExpirySyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        inputData.getRequiredString(WORK_INPUT_CLUB_ID) ?: return Result.failure()

        val database = openDatabase(applicationContext)
        return try {
            val nowEpochMillis = System.currentTimeMillis()
            val lifecycleDao = database.bookingLifecycleDao()
            val activeHoldIds = lifecycleDao.listActiveHoldIdsDueForExpiry(nowEpochMillis)
            var expiredCount = 0
            activeHoldIds.forEach { holdId ->
                if (lifecycleDao.expireHoldIfPastDue(holdId, nowEpochMillis)) {
                    expiredCount += 1
                }
            }
            Result.success(workDataOf(WORK_OUTPUT_EXPIRED_HOLD_COUNT to expiredCount))
        } catch (throwable: Throwable) {
            throwable.toWorkerResult()
        } finally {
            database.close()
        }
    }
}

class PaymentReconcileWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        inputData.getRequiredString(WORK_INPUT_CLUB_ID) ?: return Result.failure()

        return Result.success(
            workDataOf(
                WORK_OUTPUT_RECONCILED_AT_EPOCH_MILLIS to System.currentTimeMillis(),
            ),
        )
    }
}

class DocumentCacheWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        inputData.getRequiredString(WORK_INPUT_CLUB_ID) ?: return Result.failure()

        return Result.success(
            workDataOf(
                WORK_OUTPUT_CACHED_AT_EPOCH_MILLIS to System.currentTimeMillis(),
            ),
        )
    }
}

private fun openDatabase(context: Context): ClubPocDatabase {
    return Room.databaseBuilder(
        context,
        ClubPocDatabase::class.java,
        CLUB_POC_DATABASE_NAME,
    ).fallbackToDestructiveMigration().build()
}

private fun createClubApi(baseUrl: String): ClubApi {
    return NetworkFactory.create(baseUrl).create(ClubApi::class.java)
}

private fun Data.getRequiredString(key: String): String? {
    return getString(key)?.takeIf { it.isNotBlank() }
}

private fun Throwable.toWorkerResult(): ListenableWorker.Result {
    return if (this is IOException) {
        ListenableWorker.Result.retry()
    } else {
        ListenableWorker.Result.failure()
    }
}
