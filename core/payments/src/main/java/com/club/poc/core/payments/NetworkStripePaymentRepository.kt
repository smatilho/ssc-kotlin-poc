package com.club.poc.core.payments

import com.club.poc.core.common.AppResult
import com.club.poc.core.common.DefaultDispatcherProvider
import com.club.poc.core.common.DispatcherProvider
import com.club.poc.core.model.PaymentIntent
import com.club.poc.core.network.MembershipDuesPaymentIntentRequestDto
import com.club.poc.core.network.PaymentsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NetworkStripePaymentRepository(
    private val paymentsApi: PaymentsApi,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider,
) : StripePaymentRepository {
    override fun createMembershipDuesIntent(clubId: String, memberId: String): Flow<AppResult<PaymentIntent>> = flow {
        try {
            val dto = paymentsApi.createMembershipDuesPaymentIntent(
                clubId = clubId,
                request = MembershipDuesPaymentIntentRequestDto(memberId = memberId),
            )
            emit(AppResult.Success(dto.toDomain()))
        } catch (throwable: Throwable) {
            emit(AppResult.Error(message = throwable.toRepositoryMessage("create dues payment intent"), cause = throwable))
        }
    }.flowOn(dispatcherProvider.io)

    override fun createBookingIntent(clubId: String, holdId: String): Flow<AppResult<PaymentIntent>> = flow {
        try {
            val dto = paymentsApi.createBookingPaymentIntent(clubId = clubId, holdId = holdId)
            emit(AppResult.Success(dto.toDomain()))
        } catch (throwable: Throwable) {
            emit(AppResult.Error(message = throwable.toRepositoryMessage("create booking payment intent"), cause = throwable))
        }
    }.flowOn(dispatcherProvider.io)

    private fun Throwable.toRepositoryMessage(action: String): String {
        return message?.takeIf { it.isNotBlank() } ?: "Unable to $action"
    }
}

private fun com.club.poc.core.network.PaymentIntentDto.toDomain(): PaymentIntent {
    return PaymentIntent(
        id = id,
        clientSecret = clientSecret,
        amountCents = amountCents,
    )
}
