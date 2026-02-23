package com.club.poc.app.bootstrap

import android.content.Context
import androidx.room.Room
import com.club.poc.core.database.BedNightDao
import com.club.poc.core.database.BedNightEntity
import com.club.poc.core.database.BedNightRecordStatus
import com.club.poc.core.database.CLUB_POC_DATABASE_NAME
import com.club.poc.core.database.ClubConfigEntity
import com.club.poc.core.database.ClubPocDatabase
import com.club.poc.core.database.DocumentEntity
import com.club.poc.core.database.LodgeEntity
import com.club.poc.core.database.MemberProfileEntity
import com.club.poc.core.database.MemberRoleEntity
import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.MembershipStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DemoSeedBootstrapper {
    suspend fun seedIfEmpty(
        context: Context,
        clubId: String,
        memberId: String,
    ) {
        val database = Room.databaseBuilder(
            context,
            ClubPocDatabase::class.java,
            CLUB_POC_DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()

        try {
            val clubConfigDao = database.clubConfigDao()
            val membershipDao = database.membershipDao()
            val lodgeDao = database.lodgeDao()
            val documentDao = database.documentDao()
            val memberRoleDao = database.memberRoleDao()
            val bedNightDao = database.bedNightDao()

            if (clubConfigDao.get(clubId) == null) {
                clubConfigDao.upsert(
                    ClubConfigEntity(
                        clubId = clubId,
                        clubName = "North Ridge Alpine Club",
                        membershipStartMonth = 11,
                        membershipStartDay = 1,
                        duesCents = 35000,
                        holdMinutes = 15,
                        docsEnabled = true,
                        assetsEnabled = true,
                        lodgesEnabled = true,
                    ),
                )
                lodgeDao.deleteForClub(clubId)
                lodgeDao.upsertAll(
                    listOf(
                        LodgeEntity(
                            id = "lodge-ridge-house",
                            clubId = clubId,
                            name = "Ridge House",
                            bedCount = 18,
                            sortOrder = 1,
                        ),
                        LodgeEntity(
                            id = "lodge-powder-lodge",
                            clubId = clubId,
                            name = "Powder Lodge",
                            bedCount = 24,
                            sortOrder = 2,
                        ),
                    ),
                )
                documentDao.deleteForClub(clubId)
                documentDao.upsertAll(
                    listOf(
                        DocumentEntity(
                            id = "doc-waiver",
                            clubId = clubId,
                            title = "Season Liability Waiver",
                            category = "Policy",
                            url = "https://example.com/docs/waiver",
                        ),
                        DocumentEntity(
                            id = "doc-checkin",
                            clubId = clubId,
                            title = "Lodge Check-in Guide",
                            category = "Ops",
                            url = "https://example.com/docs/checkin",
                        ),
                        DocumentEntity(
                            id = "doc-bed-assign",
                            clubId = clubId,
                            title = "Bed Assignment Rules",
                            category = "Booking",
                            url = "https://example.com/docs/bed-rules",
                        ),
                    ),
                )
            }

            if (membershipDao.get(memberId) == null) {
                membershipDao.upsert(
                    MemberProfileEntity(
                        memberId = memberId,
                        userId = "demo-user",
                        clubId = clubId,
                        membershipStatus = MembershipStatus.ACTIVE.name,
                        duesPaidAt = "2026-01-05",
                    ),
                )
            }

            memberRoleDao.upsert(
                MemberRoleEntity(
                    memberId = memberId,
                    roleKey = CommitteeRole.MEMBER.name,
                ),
            )

            seedBedInventoryIfEmpty(bedNightDao)
        } finally {
            database.close()
        }
    }

    private suspend fun seedBedInventoryIfEmpty(
        bedNightDao: BedNightDao,
    ) {
        val start = LocalDate.now().plusDays(10)
        val nights = (0..2).map { index ->
            start.plusDays(index.toLong()).format(DateTimeFormatter.ISO_DATE)
        }
        val existing = bedNightDao.listRange(nights.first(), nights.last())
        if (existing.isNotEmpty()) return

        val bedIds = listOf(
            "ridge-b01",
            "ridge-b02",
            "ridge-b03",
            "ridge-b04",
            "powder-b01",
            "powder-b02",
            "powder-b03",
            "powder-b04",
        )

        val seedRows = bedIds.flatMap { bedId ->
            nights.map { nightDate ->
                BedNightEntity(
                    id = "$bedId-$nightDate",
                    bedId = bedId,
                    nightDate = nightDate,
                    status = BedNightRecordStatus.AVAILABLE.name,
                    holdId = null,
                    bookingId = null,
                    guestMemberId = null,
                )
            }
        }
        bedNightDao.insertAll(seedRows)
    }
}
