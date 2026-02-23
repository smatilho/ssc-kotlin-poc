package com.club.poc.core.payments

import com.club.poc.core.common.AppResult
import com.club.poc.core.model.PaymentIntent
import com.club.poc.core.network.MembershipDuesPaymentIntentRequestDto
import com.club.poc.core.network.PaymentIntentDto
import com.club.poc.core.network.PaymentsApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkStripePaymentRepositoryTest {
    @Test
    fun createMembershipDuesIntent_mapsDtoToDomain() = runBlocking {
        val repository = NetworkStripePaymentRepository(
            paymentsApi = object : PaymentsApi {
                override suspend fun createMembershipDuesPaymentIntent(
                    clubId: String,
                    request: MembershipDuesPaymentIntentRequestDto,
                ): PaymentIntentDto {
                    return PaymentIntentDto(
                        id = "pi_dues",
                        clientSecret = "sec_dues",
                        amountCents = 25000,
                    )
                }

                override suspend fun createBookingPaymentIntent(clubId: String, holdId: String): PaymentIntentDto {
                    return PaymentIntentDto(
                        id = "pi_booking",
                        clientSecret = "sec_booking",
                        amountCents = 42000,
                    )
                }
            },
        )

        val result = repository.createMembershipDuesIntent(clubId = "club-a", memberId = "member-a").first()

        assertEquals(
            AppResult.Success(
                PaymentIntent(
                    id = "pi_dues",
                    clientSecret = "sec_dues",
                    amountCents = 25000,
                ),
            ),
            result,
        )
    }

    @Test
    fun createBookingIntent_emitsErrorOnFailure() = runBlocking {
        val repository = NetworkStripePaymentRepository(
            paymentsApi = object : PaymentsApi {
                override suspend fun createMembershipDuesPaymentIntent(
                    clubId: String,
                    request: MembershipDuesPaymentIntentRequestDto,
                ): PaymentIntentDto {
                    error("not used")
                }

                override suspend fun createBookingPaymentIntent(clubId: String, holdId: String): PaymentIntentDto {
                    error("booking unavailable")
                }
            },
        )

        val result = repository.createBookingIntent(clubId = "club-a", holdId = "hold-a").first()

        assertTrue(result is AppResult.Error)
        val error = result as AppResult.Error
        assertEquals("booking unavailable", error.message)
    }
}

class StripeCheckoutOrchestratorTest {
    @Test
    fun createCheckout_returnsDuesAndBookingIntentsWhenDuesRequired() = runBlocking {
        val fakeRepository = object : StripePaymentRepository {
            override fun createMembershipDuesIntent(clubId: String, memberId: String) = flowOf(
                AppResult.Success(
                    PaymentIntent(
                        id = "pi_dues",
                        clientSecret = "sec_dues",
                        amountCents = 20000,
                    ),
                ),
            )

            override fun createBookingIntent(clubId: String, holdId: String) = flowOf(
                AppResult.Success(
                    PaymentIntent(
                        id = "pi_booking",
                        clientSecret = "sec_booking",
                        amountCents = 60000,
                    ),
                ),
            )
        }

        val orchestrator = StripeCheckoutOrchestrator(fakeRepository)
        val result = orchestrator.createCheckout(
            clubId = "club-a",
            memberId = "member-a",
            holdId = "hold-a",
            duesRequired = true,
        ).first()

        assertEquals(
            AppResult.Success(
                CheckoutIntents(
                    bookingIntent = PaymentIntent(
                        id = "pi_booking",
                        clientSecret = "sec_booking",
                        amountCents = 60000,
                    ),
                    duesIntent = PaymentIntent(
                        id = "pi_dues",
                        clientSecret = "sec_dues",
                        amountCents = 20000,
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun createCheckout_shortCircuitsWhenDuesIntentFails() = runBlocking {
        var bookingCallCount = 0
        val fakeRepository = object : StripePaymentRepository {
            override fun createMembershipDuesIntent(clubId: String, memberId: String) = flowOf(
                AppResult.Error(message = "dues required failure"),
            )

            override fun createBookingIntent(clubId: String, holdId: String) = flowOf(
                AppResult.Success(
                    PaymentIntent(
                        id = "pi_booking",
                        clientSecret = "sec_booking",
                        amountCents = 60000,
                    ),
                ),
            ).also { bookingCallCount++ }
        }

        val orchestrator = StripeCheckoutOrchestrator(fakeRepository)
        val result = orchestrator.createCheckout(
            clubId = "club-a",
            memberId = "member-a",
            holdId = "hold-a",
            duesRequired = true,
        ).first()

        assertEquals(AppResult.Error(message = "dues required failure"), result)
        assertEquals(0, bookingCallCount)
    }
}
