package com.cometchat.ai.sampleapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cometchat.ai.sampleapp.AppCredentials
import com.cometchat.ai.sampleapp.R
import com.cometchat.ai.sampleapp.databinding.ActivitySplashBinding
import com.cometchat.ai.sampleapp.utils.AppUtils
import com.cometchat.ai.sampleapp.viewmodels.SplashViewModel
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAppCredentials()
    }

    private fun checkAppCredentials() {
        val appId = AppUtils.getDataFromSharedPref(this, String::class.java, R.string.app_cred_id, AppCredentials.APP_ID)

        if (appId.isNullOrEmpty()) {
            startActivity(Intent(this, AppCredentialsActivity::class.java))
            finish()
        } else {
            initViewModel()
        }
    }

    private fun initViewModel() { // Initialize ViewModel
        val viewModel: SplashViewModel = ViewModelProvider(this)[SplashViewModel::class.java] // Initialize CometChat UIKit
        if (!CometChatUIKit.isSDKInitialized()) {
            viewModel.initUIKit(this)
        } else {
            viewModel.checkUserIsNotLoggedIn()
        } // Observe login status
        viewModel.getLoginStatus().observe(this) { isLoggedIn ->
            if (isLoggedIn != null) {
                if (isLoggedIn) {
                    Handler(mainLooper).postDelayed({
                        val intent = Intent(this, AIAssistantUsersActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1500) // 1500ms delay
                } else {
                    Handler(mainLooper).postDelayed({
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                        Toast.makeText(this@SplashActivity, R.string.app_not_logged_in, Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    }, 1500) // 1500ms delay
                }
            }
        }
    }
}
