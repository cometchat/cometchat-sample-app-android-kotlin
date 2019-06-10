package com.inscripts.cometchatpulse

import android.app.Application
import android.content.Context
import android.util.Log
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.inscripts.cometchatpulse.Utils.Appearance


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

        CometChat.init(applicationContext, StringContract.AppDetails.APP_ID.trim(), object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d("INIT", "Initialization completed successfully");
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG,"onError: "+p0?.message)
            }
        })
    }
}