package com.cometchat.pro.androiduikit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.androiduikit.constants.AppConfig
import com.cometchat.pro.core.AppSettings.AppSettingsBuilder
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_settings.UIKitSettings

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val appSettings = AppSettingsBuilder().subscribePresenceForAllUsers()
            .setRegion(AppConfig.AppDetails.REGION).build()
        CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings,
            object : CallbackListener<String?>() {
                override fun onSuccess(s: String?) {
                    Handler().postDelayed({
                        if (CometChat.getLoggedInUser() != null) startActivity(
                            Intent(
                                this@SplashActivity,
                                SelectActivity::class.java
                            )
                        ) else startActivity(
                            Intent(
                                this@SplashActivity,
                                MainActivity::class.java
                            )
                        )
                        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
                    }, 2000)
                    UIKitSettings.setAppID(AppConfig.AppDetails.APP_ID)
                    UIKitSettings.setAuthKey(AppConfig.AppDetails.AUTH_KEY)
                    CometChat.setSource("ui-kit", "android", "kotlin")
                    UIKitApplication.initListener(this@SplashActivity)
                }

                override fun onError(e: CometChatException) {
                    Toast.makeText(this@SplashActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}