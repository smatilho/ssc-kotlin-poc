package com.club.poc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.club.poc.app.ui.theme.ClubPocTheme
import com.club.poc.feature.booking.BOOKING_ROUTE
import com.club.poc.feature.booking.BookingScreen
import com.club.poc.feature.committeeadmin.COMMITTEE_ADMIN_ROUTE
import com.club.poc.feature.committeeadmin.CommitteeAdminScreen
import com.club.poc.feature.documents.DOCUMENTS_ROUTE
import com.club.poc.feature.documents.DocumentsScreen
import com.club.poc.feature.inviteauth.INVITE_AUTH_ROUTE
import com.club.poc.feature.inviteauth.InviteAuthScreen
import com.club.poc.feature.lodgecatalog.LODGE_CATALOG_ROUTE
import com.club.poc.feature.lodgecatalog.LodgeCatalogScreen
import com.club.poc.feature.membership.MEMBERSHIP_ROUTE
import com.club.poc.feature.membership.MembershipScreen
import com.club.poc.feature.profile.PROFILE_ROUTE
import com.club.poc.feature.profile.ProfileScreen

@Composable
fun ClubPocApp() {
    val navController = rememberNavController()
    val viewModel: ClubExperienceViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    ClubPocTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFEAF1FF),
                        ),
                    ),
                ),
        ) {
            Surface(color = Color.Transparent) {
            NavHost(navController = navController, startDestination = INVITE_AUTH_ROUTE) {
                composable(INVITE_AUTH_ROUTE) {
                    InviteAuthScreen(
                        onInviteAccepted = {
                            navController.navigate(MEMBERSHIP_ROUTE) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(MEMBERSHIP_ROUTE) {
                    MembershipScreen(
                        membershipStatus = uiState.membershipStatus,
                        onCycleMembershipStatus = viewModel::cycleMembershipStatus,
                        onContinue = {
                            navController.navigate(LODGE_CATALOG_ROUTE) {
                                popUpTo(INVITE_AUTH_ROUTE) { inclusive = true }
                            }
                        },
                    )
                }
                composable(LODGE_CATALOG_ROUTE) {
                    val bookingBlockedMessage = when (val eligibility = uiState.bookingEligibility) {
                        is com.club.poc.core.payments.BookingEligibilityResult.Eligible -> null
                        is com.club.poc.core.payments.BookingEligibilityResult.Ineligible -> when (eligibility.reason) {
                            com.club.poc.core.payments.BookingEligibilityFailureReason.DUES_UNPAID ->
                                "Booking blocked: dues unpaid"
                            com.club.poc.core.payments.BookingEligibilityFailureReason.MEMBERSHIP_LAPSED ->
                                "Booking blocked: membership lapsed"
                        }
                    }
                    LodgeCatalogScreen(
                        clubName = uiState.clubName,
                        lodgesCount = uiState.lodges.size,
                        documentsCount = uiState.documents.size,
                        bookingEnabled = uiState.bookingEligibility is com.club.poc.core.payments.BookingEligibilityResult.Eligible,
                        bookingBlockedMessage = bookingBlockedMessage,
                        docsEnabled = uiState.docsEnabled,
                        lodgesEnabled = uiState.lodgesEnabled,
                        canManageBookings = uiState.canManageBookings,
                        canManageDocuments = uiState.canManageDocuments,
                        onBooking = {
                            navController.navigate(BOOKING_ROUTE) {
                                launchSingleTop = true
                            }
                        },
                        onDocuments = {
                            navController.navigate(DOCUMENTS_ROUTE) {
                                launchSingleTop = true
                            }
                        },
                        onCommittee = {
                            navController.navigate(COMMITTEE_ADMIN_ROUTE) {
                                launchSingleTop = true
                            }
                        },
                        onProfile = {
                            navController.navigate(PROFILE_ROUTE) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(BOOKING_ROUTE) {
                    val bookingViewModel: BookingViewModel = hiltViewModel()
                    val bookingUiState by bookingViewModel.uiState.collectAsState()

                    BookingScreen(
                        uiState = bookingUiState,
                        onToggleSelection = bookingViewModel::toggleBedSelection,
                        onCreateHold = bookingViewModel::createHold,
                        onConfirmPayment = bookingViewModel::processStripeAndConfirm,
                        onCancelHold = bookingViewModel::cancelHold,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(DOCUMENTS_ROUTE) {
                    DocumentsScreen(
                        docsEnabled = uiState.docsEnabled,
                        canManageDocuments = uiState.canManageDocuments,
                        documents = uiState.documents,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(COMMITTEE_ADMIN_ROUTE) {
                    CommitteeAdminScreen(
                        roles = uiState.roles,
                        canManageBookings = uiState.canManageBookings,
                        canManageDocuments = uiState.canManageDocuments,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(PROFILE_ROUTE) {
                    ProfileScreen(
                        membershipStatus = uiState.membershipStatus,
                        membershipYear = uiState.membershipYear,
                        roles = uiState.roles,
                        docsEnabled = uiState.docsEnabled,
                        lodgesEnabled = uiState.lodgesEnabled,
                        onCycleMembershipStatus = viewModel::cycleMembershipStatus,
                        onToggleReservationist = { enabled ->
                            viewModel.setRoleEnabled(
                                role = com.club.poc.core.model.CommitteeRole.RESERVATIONIST,
                                enabled = enabled,
                            )
                        },
                        onToggleDocsCommittee = { enabled ->
                            viewModel.setRoleEnabled(
                                role = com.club.poc.core.model.CommitteeRole.DOCS_COMMITTEE,
                                enabled = enabled,
                            )
                        },
                        onToggleDocsEnabled = viewModel::toggleDocsEnabled,
                        onToggleLodgesEnabled = viewModel::toggleLodgesEnabled,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
        }
    }
}
