package com.cometchat.sampleapp.kotlin.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing application preferences using SharedPreferences.
 *
 * This class provides a secure and convenient way to store and retrieve
 * CometChat credentials and user session data. All data is stored using
 * [android.content.Context.MODE_PRIVATE] to ensure it's only accessible by this application.
 *
 * ## Stored Data:
 * - **CometChat Credentials**: App ID, Region, and Auth Key required for SDK initialization
 * - **User Session**: Logged-in user's UID for session persistence across app restarts
 *
 * ## Usage:
 * ```kotlin
 * val prefs = AppPreferences(context)
 *
 * // Save credentials
 * prefs.saveCredentials(
 *     appId = "YOUR_APP_ID",
 *     region = "us",
 *     authKey = "YOUR_AUTH_KEY"
 * )
 *
 * // Save logged-in user
 * prefs.saveLoggedInUserUid("user123")
 *
 * // Retrieve credentials
 * val appId = prefs.getAppId()
 * val region = prefs.getRegion()
 * val authKey = prefs.getAuthKey()
 *
 * // Retrieve logged-in user
 * val uid = prefs.getLoggedInUserUid()
 *
 * // Clear session on logout (keeps credentials)
 * prefs.clearSession()
 *
 * // Clear everything (credentials + session)
 * prefs.clearAll()
 * ```
 *
 * ## Thread Safety:
 * SharedPreferences operations are thread-safe. However, for best practices,
 * consider performing write operations on a background thread in production apps.
 *
 * @param context The application context used to access SharedPreferences.
 *
 * @see android.content.SharedPreferences
 */
class AppPreferences(context: Context) {

    companion object {
        /**
         * Name of the SharedPreferences file.
         */
        private const val PREFS_NAME = "cometchat_sample_app_prefs"
    }

    /**
     * Keys used for storing preference values.
     *
     * These constants define the keys under which various app data is stored
     * in SharedPreferences. Using an object ensures type safety and prevents
     * typos when accessing preferences.
     */
    object PreferenceKeys {
        /** Key for storing the CometChat App ID */
        const val APP_ID = "app_id"

        /** Key for storing the CometChat Region */
        const val REGION = "region"

        /** Key for storing the CometChat Auth Key */
        const val AUTH_KEY = "auth_key"

        /** Key for storing the logged-in user's UID */
        const val LOGGED_IN_USER_UID = "logged_in_user_uid"
    }

    /**
     * The SharedPreferences instance used for storing app data.
     * Uses MODE_PRIVATE to ensure data is only accessible by this app.
     */
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Saves CometChat credentials to SharedPreferences.
     *
     * This method stores the App ID, Region, and Auth Key required for
     * initializing the CometChat SDK. These credentials should be obtained
     * from the CometChat dashboard.
     *
     * ## Note:
     * Calling this method will overwrite any previously stored credentials.
     *
     * @param appId The CometChat App ID that uniquely identifies your application.
     * @param region The region where your CometChat app is hosted (e.g., "us", "eu", "in").
     * @param authKey The Auth Key used for user authentication.
     *
     * @see getAppId
     * @see getRegion
     * @see getAuthKey
     */
    fun saveCredentials(appId: String, region: String, authKey: String) {
        sharedPreferences.edit().apply {
            putString(PreferenceKeys.APP_ID, appId)
            putString(PreferenceKeys.REGION, region)
            putString(PreferenceKeys.AUTH_KEY, authKey)
            apply()
        }
    }

    /**
     * Retrieves the stored CometChat App ID.
     *
     * @return The stored App ID, or `null` if no App ID has been saved.
     *
     * @see saveCredentials
     */
    fun getAppId(): String? {
        return sharedPreferences.getString(PreferenceKeys.APP_ID, null)
    }

    /**
     * Retrieves the stored CometChat Region.
     *
     * @return The stored Region (e.g., "us", "eu", "in"), or `null` if no Region has been saved.
     *
     * @see saveCredentials
     */
    fun getRegion(): String? {
        return sharedPreferences.getString(PreferenceKeys.REGION, null)
    }

    /**
     * Retrieves the stored CometChat Auth Key.
     *
     * @return The stored Auth Key, or `null` if no Auth Key has been saved.
     *
     * @see saveCredentials
     */
    fun getAuthKey(): String? {
        return sharedPreferences.getString(PreferenceKeys.AUTH_KEY, null)
    }

    /**
     * Saves the logged-in user's UID for session persistence.
     *
     * This method stores the UID of the currently logged-in user, allowing
     * the app to restore the session when the app is restarted. The stored
     * UID can be used to automatically log in the user without requiring
     * them to enter credentials again.
     *
     * @param uid The unique identifier of the logged-in user.
     *
     * @see getLoggedInUserUid
     * @see clearSession
     */
    fun saveLoggedInUserUid(uid: String) {
        sharedPreferences.edit().apply {
            putString(PreferenceKeys.LOGGED_IN_USER_UID, uid)
            apply()
        }
    }

    /**
     * Retrieves the stored logged-in user's UID.
     *
     * This can be used to check if a user session exists and to restore
     * the session on app restart.
     *
     * @return The stored user UID, or `null` if no user is logged in.
     *
     * @see saveLoggedInUserUid
     * @see clearSession
     */
    fun getLoggedInUserUid(): String? {
        return sharedPreferences.getString(PreferenceKeys.LOGGED_IN_USER_UID, null)
    }

    /**
     * Clears the user session data while preserving credentials.
     *
     * This method should be called when the user logs out. It removes
     * the stored user UID but keeps the CometChat credentials (App ID,
     * Region, Auth Key) so the user doesn't need to re-enter them.
     *
     * ## Use Case:
     * Call this method when implementing logout functionality to clear
     * the session while allowing quick re-login with the same credentials.
     *
     * @see clearAll
     * @see saveLoggedInUserUid
     */
    fun clearSession() {
        sharedPreferences.edit().apply {
            remove(PreferenceKeys.LOGGED_IN_USER_UID)
            apply()
        }
    }

    /**
     * Clears all stored data including credentials and session.
     *
     * This method removes all data stored by this application, including:
     * - CometChat credentials (App ID, Region, Auth Key)
     * - User session data (logged-in user UID)
     *
     * ## Use Case:
     * Call this method when you want to completely reset the app state,
     * such as when switching to a different CometChat application or
     * performing a complete sign-out that requires re-entering credentials.
     *
     * @see clearSession
     */
    fun clearAll() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    /**
     * Checks if CometChat credentials are stored.
     *
     * This is a convenience method to quickly check if the app has
     * stored credentials that can be used for SDK initialization.
     *
     * @return `true` if App ID, Region, and Auth Key are all stored, `false` otherwise.
     */
    fun hasCredentials(): Boolean {
        return !getAppId().isNullOrBlank() &&
                !getRegion().isNullOrBlank() &&
                !getAuthKey().isNullOrBlank()
    }

    /**
     * Checks if a user session exists.
     *
     * This is a convenience method to quickly check if a user is
     * logged in (i.e., if a user UID is stored).
     *
     * @return `true` if a logged-in user UID is stored, `false` otherwise.
     */
    fun hasSession(): Boolean {
        return !getLoggedInUserUid().isNullOrBlank()
    }
}