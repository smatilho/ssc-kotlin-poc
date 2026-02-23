package com.club.poc.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentsApi {
    @POST("/v1/clubs/{clubId}/membership/dues/payment-intent")
    suspend fun createMembershipDuesPaymentIntent(
        @Path("clubId") clubId: String,
        @Body request: MembershipDuesPaymentIntentRequestDto,
    ): PaymentIntentDto

    @POST("/v1/clubs/{clubId}/booking/holds/{holdId}/payment-intent")
    suspend fun createBookingPaymentIntent(
        @Path("clubId") clubId: String,
        @Path("holdId") holdId: String,
    ): PaymentIntentDto
}

data class MembershipDuesPaymentIntentRequestDto(
    val memberId: String,
)

data class PaymentIntentDto(
    val id: String,
    val clientSecret: String,
    val amountCents: Int,
)
