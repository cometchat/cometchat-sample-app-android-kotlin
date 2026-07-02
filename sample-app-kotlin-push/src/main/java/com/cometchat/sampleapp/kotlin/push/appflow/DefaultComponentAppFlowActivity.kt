package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cometchat.pushnotification.helpers.CometChatPNHelper
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.CallsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.ChatsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.GroupsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.NotificationsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.UsersFragment
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityDefaultComponentAppFlowBinding

/**
 * Main activity for the App Flow, containing BottomNavigationView and fragment container.
 */
class DefaultComponentAppFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDefaultComponentAppFlowBinding
    private var currentFragment = R.id.nav_chats
    private val TAG = "DefaultComponentAppFlow"

    companion object {
        private const val SELECTED_FRAGMENT_KEY = "selected_fragment"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityDefaultComponentAppFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request the POST_NOTIFICATIONS runtime permission (Android 13+) on the main app screen.
        if (!CometChatPNHelper.hasNotificationPermission(this)) {
            CometChatPNHelper.requestNotificationPermission(this, NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, 0)
            windowInsets
        }

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY, R.id.nav_chats)
        } else if (intent?.getStringExtra("navigate_to") == "notifications") {
            currentFragment = R.id.nav_notifications
        }
        
        // If SDK is not initialized (cold start from deep link), initialize first
        if (!CometChatUIKit.isSDKInitialized()) {
            Log.w(TAG, "onCreate: SDK not initialized on cold start, initializing...")
            val appCredentials = com.cometchat.sampleapp.kotlin.push.shared.AppCredentials
            val uiKitSettings = com.cometchat.uikit.core.UIKitSettings.UIKitSettingsBuilder()
                .setAppId(appCredentials.APP_ID)
                .setRegion(appCredentials.REGION)
                .setAuthKey(appCredentials.AUTH_KEY)
                .subscribePresenceForAllUsers()
                .build()
            CometChatUIKit.init(this, uiKitSettings, object : com.cometchat.chat.core.CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "onCreate: SDK initialized, now loading UI")
                    initializeUI()
                }
                override fun onError(e: com.cometchat.chat.exceptions.CometChatException) {
                    Log.e(TAG, "onCreate: SDK init failed: ${e.message}")
                    finish()
                }
            })
        } else {
            initializeUI()
        }
    }
    
    private fun initializeUI() {
        binding.bottomNavigationView.selectedItemId = currentFragment
        configureBottomNavigation()
        setupBackPressHandler()
        loadFragment(getFragment(currentFragment))
    }

    private fun configureBottomNavigation() {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                CometChatTheme.getIconTintHighlight(this),
                CometChatTheme.getIconTintSecondary(this)
            )
        )

        binding.bottomNavigationView.itemIconTintList = colorStateList
        binding.bottomNavigationView.itemTextColor = colorStateList

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (currentFragment == item.itemId) {
                return@setOnItemSelectedListener true
            }
            currentFragment = item.itemId
            loadFragment(getFragment(currentFragment))
            true
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun getFragment(itemId: Int): Fragment {
        return when (itemId) {
            R.id.nav_chats -> ChatsFragment()
            R.id.nav_calls -> CallsFragment()
            R.id.nav_users -> UsersFragment()
            R.id.nav_groups -> GroupsFragment()
            R.id.nav_notifications -> NotificationsFragment()
            else -> ChatsFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_FRAGMENT_KEY, currentFragment)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("navigate_to") == "notifications") {
            currentFragment = R.id.nav_notifications
            binding.bottomNavigationView.selectedItemId = R.id.nav_notifications
            loadFragment(getFragment(R.id.nav_notifications))
        }
    }
}
