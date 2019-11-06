package com.inscripts.cometchatpulse

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.Utils.Appearance
import timber.log.Timber


class CometChatPro : Application() {

    private val TAG="CometChatPro"
    init {
        instance = this
    }

    companion object {
        private var instance: CometChatPro? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        Appearance(Appearance.AppTheme.PERSIAN_BLUE)

        val appSettings =AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(StringContract.AppDetails.REGION).build();
//        Logger.switchToDev("4ddd5d736cf33ca31a0b4c72ae64b6d5")
//        Logger.enableLogs("4ddd5d736cf33ca31a0b4c72ae64b6d5")
        CometChat.init(applicationContext, StringContract.AppDetails.APP_ID.trim(),appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Timber.d("Initialization completed successfully")
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG,"onError: ${p0?.message}")
            }
        })
    }
}