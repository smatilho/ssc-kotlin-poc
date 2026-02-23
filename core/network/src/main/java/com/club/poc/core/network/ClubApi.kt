package com.club.poc.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClubApi {
    @GET("/v1/clubs/{clubId}/config")
    suspend fun getClubConfig(@Path("clubId") clubId: String): ClubConfigDto

    @GET("/v1/clubs/{clubId}/availability")
    suspend fun getAvailability(
        @Path("clubId") clubId: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
    ): AvailabilityResponseDto

    @POST("/v1/clubs/{clubId}/booking/holds")
    suspend fun createHold(
        @Path("clubId") clubId: String,
        @Body body: CreateHoldRequestDto,
    ): BookingHoldDto
}

data class ClubConfigDto(
    val clubId: String,
    val membershipStartMonth: Int,
    val membershipStartDay: Int,
    val duesCents: Int,
    val holdMinutes: Int,
)

data class AvailabilityResponseDto(
    val items: List<BedNightDto>,
)

data class BedNightDto(
    val bedId: String,
    val nightDate: String,
    val status: String,
)

data class CreateHoldRequestDto(
    val memberId: String,
    val bedIds: List<String>,
    val dates: List<String>,
)

data class BookingHoldDto(
    val holdId: String,
    val expiresAt: String,
)
