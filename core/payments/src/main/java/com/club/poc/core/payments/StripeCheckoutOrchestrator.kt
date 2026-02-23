package com.club.poc.core.payments

import com.club.poc.core.common.AppResult
import com.club.poc.core.common.DefaultDispatcherProvider
import com.club.poc.core.common.DispatcherProvider
import com.club.poc.core.model.PaymentIntent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class CheckoutIntents(
    val bookingIntent: PaymentIntent,
    val duesIntent: PaymentIntent?,
)

class StripeCheckoutOrchestrator(
    private val stripePaymentRepository: StripePaymentRepository,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider,
) {
    fun createCheckout(
        clubId: String,
        memberId: String,
        holdId: String,
        duesRequired: Boolean,
    ): Flow<AppResult<CheckoutIntents>> = flow {
        val duesIntent = if (duesRequired) {
            when (val duesResult = stripePaymentRepository.createMembershipDuesIntent(clubId, memberId).first()) {
                is AppResult.Success -> duesResult.data
                is AppResult.Error -> {
                    emit(duesResult)
                    return@flow
                }
            }
        } else {
            null
        }

        when (val bookingResult = stripePaymentRepository.createBookingIntent(clubId, holdId).first()) {
            is AppResult.Success -> {
                emit(
                    AppResult.Success(
                        CheckoutIntents(
                            bookingIntent = bookingResult.data,
                            duesIntent = duesIntent,
                        ),
                    ),
                )
            }
            is AppResult.Error -> emit(bookingResult)
        }
    }.flowOn(dispatcherProvider.io)
}
