package com.silentsos.app.navigation

sealed class Screen(val route: String) {
    // Initialization
    data object Splash : Screen("splash")

    // Authentication
    data object PhoneAuth : Screen("phone_auth")
    data object Permissions : Screen("permissions")
    
    // Disguise (Entry Point)
    data object Calculator : Screen("calculator")

    // Core Dashboard
    data object HiddenDashboard : Screen("hidden_dashboard")

    // SOS
    data object ActiveSOS : Screen("active_sos")

    // Contacts / Safety Network
    data object SafetyNetwork : Screen("safety_network")
    data object AddContact : Screen("add_contact")

    // Settings
    data object Settings : Screen("settings")
    data object TriggerConfig : Screen("trigger_config")

    // History
    data object IncidentHistory : Screen("incident_history")

    // Decoys
    data object FakeFine : Screen("fake_fine")
    data object SystemDeactivated : Screen("system_deactivated")
}
