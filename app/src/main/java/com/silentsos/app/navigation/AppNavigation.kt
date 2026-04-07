package com.silentsos.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.silentsos.app.presentation.ui.screens.contacts.AddContactScreen
import com.silentsos.app.presentation.ui.screens.contacts.SafetyNetworkScreen
import com.silentsos.app.presentation.ui.screens.dashboard.HiddenDashboardScreen
import com.silentsos.app.presentation.ui.screens.decoy.FakeFineScreen
import com.silentsos.app.presentation.ui.screens.decoy.SystemDeactivatedDecoyScreen
import com.silentsos.app.presentation.ui.screens.disguise.CalculatorScreen
import com.silentsos.app.presentation.ui.screens.history.IncidentHistoryScreen
import com.silentsos.app.presentation.ui.screens.settings.SettingsScreen
import com.silentsos.app.presentation.ui.screens.settings.TriggerConfigScreen
import com.silentsos.app.presentation.ui.screens.sos.ActiveSOSScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route
    ) {
        // ── Entry Point: Calculator Disguise ──
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onAccessDashboard = {
                    navController.navigate(Screen.HiddenDashboard.route) {
                        popUpTo(Screen.Calculator.route) { inclusive = false }
                    }
                },
                onDuressTriggered = {
                    navController.navigate(Screen.FakeFine.route) {
                        popUpTo(Screen.Calculator.route) { inclusive = false }
                    }
                }
            )
        }

        // ── Hidden Dashboard ──
        composable(Screen.HiddenDashboard.route) {
            HiddenDashboardScreen(
                onNavigateToSetup = {
                    navController.navigate(Screen.SafetyNetwork.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.IncidentHistory.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onSOSTriggered = {
                    navController.navigate(Screen.ActiveSOS.route)
                }
            )
        }

        // ── Active SOS ──
        composable(Screen.ActiveSOS.route) {
            ActiveSOSScreen(
                onEndProtocol = {
                    navController.popBackStack(Screen.HiddenDashboard.route, inclusive = false)
                }
            )
        }

        // ── Safety Network ──
        composable(Screen.SafetyNetwork.route) {
            SafetyNetworkScreen(
                onBackClick = { navController.popBackStack() },
                onAddContact = { navController.navigate(Screen.AddContact.route) }
            )
        }

        // ── Add Contact ──
        composable(Screen.AddContact.route) {
            AddContactScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Settings ──
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Trigger Config ──
        composable(Screen.TriggerConfig.route) {
            TriggerConfigScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Incident History ──
        composable(Screen.IncidentHistory.route) {
            IncidentHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Decoy: Fake Fine ──
        composable(Screen.FakeFine.route) {
            FakeFineScreen(
                onDismiss = {
                    navController.popBackStack(Screen.Calculator.route, inclusive = false)
                }
            )
        }

        // ── Decoy: System Deactivated ──
        composable(Screen.SystemDeactivated.route) {
            SystemDeactivatedDecoyScreen(
                onDismiss = {
                    navController.popBackStack(Screen.Calculator.route, inclusive = false)
                }
            )
        }
    }
}
