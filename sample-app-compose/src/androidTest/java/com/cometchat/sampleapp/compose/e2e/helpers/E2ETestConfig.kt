package com.cometchat.sampleapp.compose.e2e.helpers

import androidx.test.platform.app.InstrumentationRegistry

/**
 * Single source of truth for all E2E test configuration.
 *
 * If you are running these tests against your OWN CometChat app, replace the values below.
 * Any value can also be overridden at runtime via a runner argument (no code edit), e.g.:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.appId=YOUR_APP_ID \
 *       -Pandroid.testInstrumentationRunnerArguments.restApiKey=YOUR_FULLACCESS_REST_KEY
 *
 * The fullAccess REST API Key is the only real secret here.
 *
 * NOTE on display names: user display names are intentionally NOT stored here. Fetch them from
 * the UID at runtime via [E2ETestHelper.getUserName] so tests don't break if dashboard data
 * (a user's name) changes. The names shown in comments are just the current dashboard values
 * for reference.
 */
object E2ETestConfig {

    private fun arg(name: String): String? =
        InstrumentationRegistry.getArguments().getString(name)?.takeIf { it.isNotBlank() }

    // ─── CometChat app credentials (REPLACE with your own) ───────────────────────
    val APP_ID: String get() = arg("appId") ?: "YOUR_APP_ID"
    val REGION: String get() = arg("region") ?: "YOUR_REGION" // e.g. "us", "eu", "in"

    /** The app's Auth Key (auth-only scope) — used to initialize the UIKit SDK. */
    val AUTH_KEY: String get() = arg("authKey") ?: "YOUR_AUTH_KEY"

    /**
     * fullAccess REST API Key (NOT the Auth Key). Required by [RestApiHelper] to drive partner
     * users over REST. Override at runtime via -Pandroid.testInstrumentationRunnerArguments.restApiKey.
     */
    val REST_API_KEY: String get() = arg("restApiKey") ?: "YOUR_REST_API_KEY"

    // ─── Test users (UIDs) (REPLACE with UIDs that exist in your app) ─────────────
    /** The user the app logs in as. */
    val LOGGED_IN_UID: String get() = arg("testUid") ?: arg("myUid") ?: "YOUR_LOGGED_IN_UID"

    /** Primary "other" user — the 1:1 partner and the main other group member. */
    val ONE_TO_ONE_UID: String get() = arg("partnerUid") ?: "YOUR_PARTNER_UID"

    /** Secondary partner for "multiple members" realtime scenarios. */
    val MULTI_MEMBER_UID: String get() = arg("partner2Uid") ?: "YOUR_SECOND_PARTNER_UID"

    /** Group member / "other user" for SDK- and REST-based group setup. */
    const val GROUP_MEMBER_1_UID = "YOUR_GROUP_MEMBER_1_UID"

    /** Second group member / moderator in member-management tests. */
    const val GROUP_MEMBER_2_UID = "YOUR_GROUP_MEMBER_2_UID"

    /** Third group member, promoted to admin in member-management tests. */
    const val GROUP_MEMBER_3_UID = "YOUR_GROUP_MEMBER_3_UID"

    // ─── Groups / data (REPLACE with public groups owned by LOGGED_IN_UID) ────────
    /** Public group owned by [LOGGED_IN_UID] for the realtime group tests. */
    const val REALTIME_GROUP_GUID = "YOUR_REALTIME_GROUP_GUID"

    /** Public group owned by [LOGGED_IN_UID] for the realtime group-receive tests. */
    const val GROUP_RECEIVE_GUID = "YOUR_GROUP_RECEIVE_GUID"

    /** Password used for the password-protected group tests. */
    const val GROUP_PASSWORD = "YOUR_GROUP_PASSWORD"

    /** A reachable hosted file used as a media attachment in realtime media tests. */
    const val MEDIA_FILE_URL =
        "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
}
