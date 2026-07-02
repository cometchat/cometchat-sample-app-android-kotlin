package com.cometchat.sampleapp.kotlin.utils

/**
 * Application-wide constants for the CometChat Sample App.
 *
 * This object contains default configuration values and sample data
 * used throughout the application. These values serve as placeholders
 * and should be replaced with actual CometChat credentials in production.
 *
 * ## CometChat Credentials:
 * To use this sample app, you need to:
 * 1. Create a CometChat account at https://app.cometchat.com
 * 2. Create a new app in the CometChat dashboard
 * 3. Replace the placeholder values below with your actual credentials
 *
 * ## Sample Users:
 * The sample users (superhero1-5) are pre-created in CometChat's sample apps.
 * These users are available for testing purposes when using CometChat's
 * sample app credentials.
 *
 * @see AppPreferences for credential storage
 */
object AppConstants {

    /**
     * Default CometChat App ID.
     *
     * This is a placeholder value. Replace with your actual App ID
     * from the CometChat dashboard (API & Auth Keys section).
     *
     * Format: Alphanumeric string (e.g., "123456abcdef")
     */
    const val APP_ID = "XXXXXXXXXXXXXXXXXX"

    /**
     * Default CometChat Region.
     *
     * The region where your CometChat app is hosted.
     * Common values: "us", "eu", "in"
     *
     * This should match the region selected when creating your
     * CometChat app in the dashboard.
     */
    const val REGION = "XXXXXXXXXXXXXXXX"

    /**
     * Default CometChat Auth Key.
     *
     * This is a placeholder value. Replace with your actual Auth Key
     * from the CometChat dashboard (API & Auth Keys section).
     *
     * **Security Note**: In production apps, consider using a more
     * secure authentication method such as Auth Tokens generated
     * from your backend server.
     */
    const val AUTH_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX"


}
