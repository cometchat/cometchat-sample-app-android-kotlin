package com.cometchat.sampleapp.compose.push.shared

import android.content.Context
import android.content.SharedPreferences
import com.cometchat.sampleapp.compose.push.R

/**
 * Utility class for managing app credentials in SharedPreferences.
 * Handles storage and retrieval of CometChat App ID, Region, and Auth Key.
 * Returns default credentials from AppCredentials when none are stored.
 */
object AppPreferences {

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(R.string.app_shared_pref),
            Context.MODE_PRIVATE
        )
    }

    /**
     * Returns the stored CometChat App ID, or default if not set.
     */
    fun getAppId(context: Context): String? {
        return getSharedPreferences(context).getString(
            context.getString(R.string.app_cred_id),
            AppCredentials.APP_ID
        )
    }

    /**
     * Returns the stored CometChat Region (us, eu, in), or default if not set.
     */
    fun getRegion(context: Context): String? {
        return getSharedPreferences(context).getString(
            context.getString(R.string.app_cred_region),
            AppCredentials.REGION
        )
    }

    /**
     * Returns the stored CometChat Auth Key, or default if not set.
     */
    fun getAuthKey(context: Context): String? {
        return getSharedPreferences(context).getString(
            context.getString(R.string.app_cred_auth),
            AppCredentials.AUTH_KEY
        )
    }

    /**
     * Saves the CometChat credentials to SharedPreferences.
     * @param appId The CometChat App ID
     * @param region The region (us, eu, in)
     * @param authKey The Auth Key
     */
    fun saveCredentials(context: Context, appId: String, region: String, authKey: String) {
        getSharedPreferences(context).edit().apply {
            putString(context.getString(R.string.app_cred_id), appId)
            putString(context.getString(R.string.app_cred_region), region)
            putString(context.getString(R.string.app_cred_auth), authKey)
            apply()
        }
    }

    /**
     * Clears all stored credentials from SharedPreferences.
     */
    fun clearCredentials(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    /**
     * Checks if app credentials are configured.
     * Always returns true since we have default credentials.
     * @return true (always has credentials due to defaults)
     */
    fun hasCredentials(context: Context): Boolean {
        return true
    }
}
