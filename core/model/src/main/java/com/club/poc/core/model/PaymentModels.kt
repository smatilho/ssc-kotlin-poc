package com.club.poc.core.model

data class PaymentIntent(
    val id: String,
    val clientSecret: String,
    val amountCents: Int,
)
