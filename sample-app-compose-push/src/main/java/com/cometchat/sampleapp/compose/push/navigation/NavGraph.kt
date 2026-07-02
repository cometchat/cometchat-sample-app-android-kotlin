package com.cometchat.sampleapp.compose.push.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the login flow.
 * These routes use Kotlin Serialization for Navigation Compose type-safe navigation.
 * 
 * Navigation Flow:
 * - SplashRoute → AppCredentialsRoute (no credentials)
 * - SplashRoute → AppFlowHomeRoute (user logged in)
 * - SplashRoute → LoginRoute (not logged in)
 * - AppCredentialsRoute → LoginRoute (credentials saved)
 * - LoginRoute → AppFlowHomeRoute (login success)
 * - LoginRoute → AppCredentialsRoute (change credentials)
 * - AppFlowHomeRoute → LoginRoute (Logout)
 */

// ============================================================================
// Login Flow Routes
// ============================================================================

/**
 * Splash screen route - initial screen displayed on app launch.
 * Handles SDK initialization and login state checking.
 * 
 * Navigation destinations:
 * - AppCredentialsRoute: No credentials configured
 * - AppFlowHomeRoute: User is logged in
 * - LoginRoute: Credentials exist but user not logged in
 */
@Serializable
object SplashRoute

/**
 * Login screen route - allows users to authenticate via sample user selection
 * or manual UID entry.
 * 
 * Navigation destinations:
 * - AppFlowHomeRoute: Login success
 * - AppCredentialsRoute: Change credentials
 */
@Serializable
object LoginRoute

/**
 * App credentials screen route - allows users to configure CometChat app credentials
 * (App ID, Region, Auth Key).
 * 
 * Navigation destinations:
 * - LoginRoute: Credentials saved and SDK initialized
 */
@Serializable
object AppCredentialsRoute
