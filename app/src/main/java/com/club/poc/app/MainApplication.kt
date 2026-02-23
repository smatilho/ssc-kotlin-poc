package com.club.poc.app

import android.app.Application
import com.club.poc.app.bootstrap.DemoSeedBootstrapper
import com.club.poc.core.work.ClubSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MainApplication : Application() {
    private val bootstrapScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        bootstrapScope.launch {
            DemoSeedBootstrapper.seedIfEmpty(
                context = this@MainApplication,
                clubId = BuildConfig.BOOTSTRAP_CLUB_ID,
                memberId = BuildConfig.BOOTSTRAP_MEMBER_ID,
            )
        }
        ClubSyncScheduler.schedule(
            context = this,
            clubId = BuildConfig.BOOTSTRAP_CLUB_ID,
            apiBaseUrl = BuildConfig.API_BASE_URL,
        )
    }
}
