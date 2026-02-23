package com.club.poc.core.payments

import com.club.poc.core.common.AppResult
import com.club.poc.core.model.PaymentIntent
import kotlinx.coroutines.flow.Flow

interface StripePaymentRepository {
    fun createMembershipDuesIntent(clubId: String, memberId: String): Flow<AppResult<PaymentIntent>>
    fun createBookingIntent(clubId: String, holdId: String): Flow<AppResult<PaymentIntent>>
}
