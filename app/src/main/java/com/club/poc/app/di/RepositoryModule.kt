package com.club.poc.app.di

import com.club.poc.core.auth.InviteAuthRepository
import com.club.poc.core.auth.NetworkInviteAuthRepository
import com.club.poc.core.auth.CommitteeAccessPolicy
import com.club.poc.core.common.DefaultDispatcherProvider
import com.club.poc.core.common.DispatcherProvider
import com.club.poc.core.network.AuthApi
import com.club.poc.core.network.PaymentsApi
import com.club.poc.core.payments.MembershipDuesGate
import com.club.poc.core.payments.NetworkStripePaymentRepository
import com.club.poc.core.payments.StripeCheckoutOrchestrator
import com.club.poc.core.payments.StripePaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider

    @Provides
    @Singleton
    fun provideCommitteeAccessPolicy(): CommitteeAccessPolicy = CommitteeAccessPolicy()

    @Provides
    @Singleton
    fun provideMembershipDuesGate(): MembershipDuesGate = MembershipDuesGate()

    @Provides
    @Singleton
    fun provideInviteAuthRepository(
        authApi: AuthApi,
        dispatcherProvider: DispatcherProvider,
    ): InviteAuthRepository {
        return NetworkInviteAuthRepository(
            authApi = authApi,
            dispatcherProvider = dispatcherProvider,
        )
    }

    @Provides
    @Singleton
    fun provideStripePaymentRepository(
        paymentsApi: PaymentsApi,
        dispatcherProvider: DispatcherProvider,
    ): StripePaymentRepository {
        return NetworkStripePaymentRepository(
            paymentsApi = paymentsApi,
            dispatcherProvider = dispatcherProvider,
        )
    }

    @Provides
    @Singleton
    fun provideStripeCheckoutOrchestrator(
        stripePaymentRepository: StripePaymentRepository,
        dispatcherProvider: DispatcherProvider,
    ): StripeCheckoutOrchestrator {
        return StripeCheckoutOrchestrator(
            stripePaymentRepository = stripePaymentRepository,
            dispatcherProvider = dispatcherProvider,
        )
    }
}
