package com.club.poc.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.club.poc.app.data.ClubExperienceRepository
import com.club.poc.core.auth.CommitteeAccessPolicy
import com.club.poc.core.database.MemberRoleEntity
import com.club.poc.core.model.ClubDocumentSummary
import com.club.poc.core.model.CommitteeRole
import com.club.poc.core.model.LodgeSummary
import com.club.poc.core.model.MembershipStatus
import com.club.poc.core.model.MembershipYear
import com.club.poc.core.model.MembershipYearPolicy
import com.club.poc.core.payments.BookingEligibilityResult
import com.club.poc.core.payments.MembershipDuesGate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClubExperienceUiState(
    val clubId: String,
    val clubName: String,
    val memberId: String,
    val membershipStatus: MembershipStatus,
    val membershipYear: MembershipYear,
    val bookingEligibility: BookingEligibilityResult,
    val roles: Set<CommitteeRole>,
    val canManageBookings: Boolean,
    val canManageDocuments: Boolean,
    val docsEnabled: Boolean,
    val lodgesEnabled: Boolean,
    val lodges: List<LodgeSummary>,
    val documents: List<ClubDocumentSummary>,
) {
    companion object {
        val Empty = ClubExperienceUiState(
            clubId = BuildConfig.BOOTSTRAP_CLUB_ID,
            clubName = "Club",
            memberId = BuildConfig.BOOTSTRAP_MEMBER_ID,
            membershipStatus = MembershipStatus.UNPAID,
            membershipYear = MembershipYear(startDate = "", endDate = ""),
            bookingEligibility = BookingEligibilityResult.Ineligible(
                reason = com.club.poc.core.payments.BookingEligibilityFailureReason.DUES_UNPAID,
            ),
            roles = emptySet(),
            canManageBookings = false,
            canManageDocuments = false,
            docsEnabled = true,
            lodgesEnabled = true,
            lodges = emptyList(),
            documents = emptyList(),
        )
    }
}

@HiltViewModel
class ClubExperienceViewModel @Inject constructor(
    private val repository: ClubExperienceRepository,
    private val membershipDuesGate: MembershipDuesGate,
    private val committeeAccessPolicy: CommitteeAccessPolicy,
) : ViewModel() {
    private val clubId = BuildConfig.BOOTSTRAP_CLUB_ID
    private val memberId = BuildConfig.BOOTSTRAP_MEMBER_ID

    val uiState: StateFlow<ClubExperienceUiState> = combine(
        repository.observeClubConfig(clubId),
        repository.observeMemberProfile(memberId),
        repository.observeMemberRoles(memberId),
        repository.observeLodges(clubId),
        repository.observeDocuments(clubId),
    ) { config, profile, roleEntities, lodgeEntities, documentEntities ->
        val startMonth = config?.membershipStartMonth ?: 11
        val startDay = config?.membershipStartDay ?: 1
        val membershipYear = MembershipYearPolicy(startMonth, startDay).forDate(LocalDate.now())
        val membershipStatus = profile?.membershipStatus.toMembershipStatus()
        val roles = roleEntities.toCommitteeRoles()
        val bookingEligibility = membershipDuesGate.evaluate(membershipStatus)

        ClubExperienceUiState(
            clubId = clubId,
            clubName = config?.clubName ?: "Club",
            memberId = memberId,
            membershipStatus = membershipStatus,
            membershipYear = membershipYear,
            bookingEligibility = bookingEligibility,
            roles = roles,
            canManageBookings = committeeAccessPolicy.canOverrideBookings(roles),
            canManageDocuments = committeeAccessPolicy.canManageDocuments(roles),
            docsEnabled = config?.docsEnabled ?: true,
            lodgesEnabled = config?.lodgesEnabled ?: true,
            lodges = lodgeEntities.map { entity ->
                LodgeSummary(
                    id = entity.id,
                    name = entity.name,
                    bedCount = entity.bedCount,
                )
            },
            documents = documentEntities.map { entity ->
                ClubDocumentSummary(
                    id = entity.id,
                    title = entity.title,
                    category = entity.category,
                    url = entity.url,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClubExperienceUiState.Empty,
    )

    fun cycleMembershipStatus() {
        viewModelScope.launch {
            repository.cycleMembershipStatus(memberId)
        }
    }

    fun setRoleEnabled(role: CommitteeRole, enabled: Boolean) {
        viewModelScope.launch {
            repository.setRoleEnabled(
                memberId = memberId,
                role = role,
                enabled = enabled,
            )
        }
    }

    fun toggleDocsEnabled() {
        viewModelScope.launch {
            repository.toggleDocsEnabled(clubId)
        }
    }

    fun toggleLodgesEnabled() {
        viewModelScope.launch {
            repository.toggleLodgesEnabled(clubId)
        }
    }

    private fun String?.toMembershipStatus(): MembershipStatus {
        return MembershipStatus.entries.firstOrNull { it.name == this } ?: MembershipStatus.UNPAID
    }

    private fun List<MemberRoleEntity>.toCommitteeRoles(): Set<CommitteeRole> {
        if (isEmpty()) return emptySet()
        return mapNotNull { roleEntity ->
            CommitteeRole.entries.firstOrNull { it.name == roleEntity.roleKey }
        }.toSet()
    }
}
