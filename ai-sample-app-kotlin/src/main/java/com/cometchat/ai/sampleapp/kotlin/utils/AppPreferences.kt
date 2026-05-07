package com.cometchat.ai.sampleapp.kotlin.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing application preferences using SharedPreferences.
 * Stores CometChat credentials (App ID, Region, Auth Key) and user session (UID).
 */
class AppPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "cometchat_ai_sample_app_prefs"
    }

    object PreferenceKeys {
        const val APP_ID = "app_id"
        const val REGION = "region"
        const val AUTH_KEY = "auth_key"
        const val LOGGED_IN_USER_UID = "logged_in_user_uid"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCredentials(appId: String, region: String, authKey: String) {
        sharedPreferences.edit().apply {
            putString(PreferenceKeys.APP_ID, appId)
            putString(PreferenceKeys.REGION, region)
            putString(PreferenceKeys.AUTH_KEY, authKey)
            apply()
        }
    }

    fun getAppId(): String? = sharedPreferences.getString(PreferenceKeys.APP_ID, null)
    fun getRegion(): String? = sharedPreferences.getString(PreferenceKeys.REGION, null)
    fun getAuthKey(): String? = sharedPreferences.getString(PreferenceKeys.AUTH_KEY, null)

    fun saveLoggedInUserUid(uid: String) {
        sharedPreferences.edit().apply {
            putString(PreferenceKeys.LOGGED_IN_USER_UID, uid)
            apply()
        }
    }

    fun getLoggedInUserUid(): String? =
        sharedPreferences.getString(PreferenceKeys.LOGGED_IN_USER_UID, null)

    fun clearSession() {
        sharedPreferences.edit().apply {
            remove(PreferenceKeys.LOGGED_IN_USER_UID)
            apply()
        }
    }

    fun clearAll() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    fun hasCredentials(): Boolean =
        !getAppId().isNullOrBlank() &&
            !getRegion().isNullOrBlank() &&
            !getAuthKey().isNullOrBlank()

    fun hasSession(): Boolean = !getLoggedInUserUid().isNullOrBlank()
}
