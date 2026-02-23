package com.club.poc.app.di

import com.club.poc.app.BuildConfig
import com.club.poc.core.network.AuthApi
import com.club.poc.core.network.ClubApi
import com.club.poc.core.network.NetworkFactory
import com.club.poc.core.network.PaymentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return NetworkFactory.create(baseUrl = BuildConfig.API_BASE_URL)
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideClubApi(retrofit: Retrofit): ClubApi {
        return retrofit.create(ClubApi::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentsApi(retrofit: Retrofit): PaymentsApi {
        return retrofit.create(PaymentsApi::class.java)
    }
}
