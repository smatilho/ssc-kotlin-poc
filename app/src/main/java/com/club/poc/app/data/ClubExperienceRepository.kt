package com.club.poc.app.data

import com.club.poc.core.database.ClubConfigDao
import com.club.poc.core.database.DocumentDao
import com.club.poc.core.database.LodgeDao
import com.club.poc.core.database.MemberRoleDao
import com.club.poc.core.database.MemberRoleEntity
import com.club.poc.core.database.MembershipDao
import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.MembershipStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubExperienceRepository @Inject constructor(
    private val clubConfigDao: ClubConfigDao,
    private val membershipDao: MembershipDao,
    private val memberRoleDao: MemberRoleDao,
    private val lodgeDao: LodgeDao,
    private val documentDao: DocumentDao,
) {
    fun observeClubConfig(clubId: String) = clubConfigDao.observe(clubId)

    fun observeMemberProfile(memberId: String) = membershipDao.observe(memberId)

    fun observeMemberRoles(memberId: String): Flow<List<MemberRoleEntity>> = memberRoleDao.observe(memberId)

    fun observeLodges(clubId: String) = lodgeDao.observe(clubId)

    fun observeDocuments(clubId: String) = documentDao.observe(clubId)

    suspend fun cycleMembershipStatus(memberId: String) {
        val profile = membershipDao.get(memberId) ?: return
        val current = profile.membershipStatus.toMembershipStatus()
        val next = when (current) {
            MembershipStatus.ACTIVE -> MembershipStatus.UNPAID
            MembershipStatus.UNPAID -> MembershipStatus.LAPSED
            MembershipStatus.LAPSED -> MembershipStatus.ACTIVE
        }
        membershipDao.updateMembershipStatus(memberId, next.name)
    }

    suspend fun setRoleEnabled(
        memberId: String,
        role: CommitteeRole,
        enabled: Boolean,
    ) {
        if (enabled) {
            memberRoleDao.upsert(MemberRoleEntity(memberId = memberId, roleKey = role.name))
        } else {
            memberRoleDao.delete(memberId = memberId, roleKey = role.name)
        }
    }

    suspend fun toggleDocsEnabled(clubId: String) {
        val config = clubConfigDao.get(clubId) ?: return
        clubConfigDao.updateFeatureFlags(
            clubId = clubId,
            docsEnabled = !config.docsEnabled,
            assetsEnabled = config.assetsEnabled,
            lodgesEnabled = config.lodgesEnabled,
        )
    }

    suspend fun toggleLodgesEnabled(clubId: String) {
        val config = clubConfigDao.get(clubId) ?: return
        clubConfigDao.updateFeatureFlags(
            clubId = clubId,
            docsEnabled = config.docsEnabled,
            assetsEnabled = config.assetsEnabled,
            lodgesEnabled = !config.lodgesEnabled,
        )
    }

    private fun String.toMembershipStatus(): MembershipStatus {
        return MembershipStatus.entries.firstOrNull { it.name == this } ?: MembershipStatus.UNPAID
    }
}
