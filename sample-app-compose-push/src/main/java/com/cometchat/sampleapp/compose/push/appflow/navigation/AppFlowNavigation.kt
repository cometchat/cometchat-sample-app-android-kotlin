package com.cometchat.sampleapp.compose.push.appflow.navigation

/**
 * Bottom navigation tabs for the app flow home screen.
 * 
 * Note: The AppFlowDestination sealed class has been replaced by type-safe routes
 * defined in AppFlowRoutes.kt using @Serializable annotations for Navigation Compose.
 */
enum class AppFlowTab {
    Chats,
    Calls,
    Users,
    Groups,
    Notifications
}
