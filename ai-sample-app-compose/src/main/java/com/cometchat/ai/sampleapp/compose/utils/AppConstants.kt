package com.cometchat.ai.sampleapp.compose.utils

/**
 * Application-wide constants for the AI Assistant Sample App (Compose).
 *
 * The App ID / Region / Auth Key are placeholders; users enter real credentials
 * via the [com.cometchat.ai.sampleapp.compose.ui.credentials.AppCredentialsScreen].
 */
object AppConstants {

    /** CometChat App ID (placeholder — replace with your actual App ID). */
    const val APP_ID = "XXXXXXXXX"

    /** CometChat Region ("us", "eu", or "in"). */
    const val REGION = "XXXXXXXXX"

    /** CometChat Auth Key (placeholder — replace with your actual Auth Key). */
    const val AUTH_KEY = "XXXXXXXXX"

    /** URL for fetching sample users (CometChat demo users). */
    const val SAMPLE_USERS_URL = "https://assets.cometchat.io/sampleapp/sampledata.json"
}
