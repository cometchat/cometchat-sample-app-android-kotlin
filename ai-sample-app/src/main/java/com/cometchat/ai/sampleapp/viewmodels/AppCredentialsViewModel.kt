package com.cometchat.ai.sampleapp.viewmodels

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.ai.sampleapp.R
import com.cometchat.ai.sampleapp.ui.activity.LoginActivity
import com.cometchat.ai.sampleapp.utils.AppUtils
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings.UIKitSettingsBuilder

/**
 * ViewModel for the SplashActivity to handle UI-related data and business
 * logic.
 */
class AppCredentialsViewModel : ViewModel() {
    private val loginStatus = MutableLiveData<Boolean>()
    val selectedRegion: MutableLiveData<String> = MutableLiveData()

    fun setSelectedRegion(selectedRegion: String) {
        this.selectedRegion.value = selectedRegion
    }

    fun initUIKit(activity: Activity, appId: String, authKey: String) {
        AppUtils.clearSharePref(activity)
        AppUtils.saveDataInSharedPref(activity, activity.getString(R.string.app_cred_id), appId)
        AppUtils.saveDataInSharedPref(
            activity, activity.getString(R.string.app_cred_region), selectedRegion.value
        )
        AppUtils.saveDataInSharedPref(activity, activity.getString(R.string.app_cred_auth), authKey)
        val uiKitSettings = UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(true)
            .setRegion(selectedRegion.value)
            .setAppId(appId)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .build()

        CometChatUIKit.init(activity, uiKitSettings, object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                activity.finish()
            }

            override fun onError(e: CometChatException) { // Show error message
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Checks if the user is logged in and updates the login status.  */
    private fun checkUserIsNotLoggedIn() {
        if (CometChatUIKit.getLoggedInUser() != null) {
            loginStatus.setValue(true) // User is logged in
        } else {
            loginStatus.setValue(false) // User is not logged in
        }
    }

    /**
     * Returns the login status LiveData.
     *
     * @return LiveData containing the user's login status.
     */
    fun getLoginStatus(): LiveData<Boolean> {
        return loginStatus
    }
}
