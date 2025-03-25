package com.cometchat.sampleapp.kotlin.fcm.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.data.interfaces.OnItemClickListener
import com.cometchat.sampleapp.kotlin.fcm.databinding.ActivityHomeBinding
import com.cometchat.sampleapp.kotlin.fcm.fcm.FCMMessageDTO
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.CallsFragment
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.ChatsFragment
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.GroupsFragment
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.UsersFragment
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants
import com.cometchat.sampleapp.kotlin.fcm.utils.AppUtils
import com.cometchat.sampleapp.kotlin.fcm.voip.CometChatVoIP
import com.cometchat.sampleapp.kotlin.fcm.voip.interfaces.VoIPPermissionListener
import com.cometchat.sampleapp.kotlin.fcm.voip.model.CometChatVoIPError
import com.cometchat.sampleapp.kotlin.fcm.voip.utils.CometChatVoIPConstant
import com.google.gson.Gson

class HomeActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var binding: ActivityHomeBinding
    private var currentFragment = R.id.nav_chats // Default to the Chats fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY, R.id.nav_chats)
        } // Set the selected item in the bottom navigation to match the current fragment
        binding.bottomNavigationView.selectedItemId = currentFragment

        AppUtils.requestNotificationPermission(this)

        handleDeepLinking()
        configureBottomNavigation()
        configureVoIP()
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment
     * The fragment to be loaded.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    /**
     * Configures the bottom navigation view and its item selection listener.
     * Updates the displayed fragment based on user selection.
     */
    private fun configureBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (currentFragment == item.itemId) {
                return@setOnItemSelectedListener true // No action needed if the fragment is already selected
            }
            currentFragment = item.itemId
            loadFragment(
                getFragment(
                    currentFragment
                )
            )
            true
        } // Create a ColorStateList for icon and text color based on the checked state
        val colorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                CometChatTheme.getIconTintHighlight(this), CometChatTheme.getIconTintSecondary(
                    this
                )
            )
        )
        binding.bottomNavigationView.itemPaddingBottom = 0
        binding.bottomNavigationView.itemIconTintList = colorStateList
        binding.bottomNavigationView.itemTextColor = colorStateList
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_FRAGMENT_KEY, currentFragment) // Save the selected fragment ID
    }

    override fun onItemClick() {
        val intent = Intent(this, NewChatActivity::class.java)
        startActivity(intent)
    }

    private fun handleDeepLinking() {
        val notificationType = intent.getStringExtra(AppConstants.FCMConstants.NOTIFICATION_TYPE)
        val notificationPayload = intent.getStringExtra(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD)
        if (notificationType == null || notificationPayload == null) {
            loadFragment(getFragment(currentFragment))
        } else {
            if (AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE == notificationType) {
                val fcmMessageDTO = Gson().fromJson(
                    notificationPayload, FCMMessageDTO::class.java
                )
                if (fcmMessageDTO != null) {
                    if ("chat".equals(fcmMessageDTO.type, ignoreCase = true)) {
                        val chatFragment = getFragment(currentFragment)
                        val args = Bundle()
                        args.putString(AppConstants.FCMConstants.NOTIFICATION_TYPE, notificationType)
                        args.putString(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD, notificationPayload)
                        chatFragment.arguments = args
                        loadFragment(chatFragment)
                        currentFragment = R.id.nav_chats
                        binding.bottomNavigationView.selectedItemId = currentFragment
                    }
                }
            }
        }
    }

    companion object {
        private val TAG: String = HomeActivity::class.java.simpleName
        private const val SELECTED_FRAGMENT_KEY = "selected_fragment"

        /**
         * Returns the appropriate fragment based on the selected menu itemId.
         *
         * @param itemId
         * The selected menu itemId ID.
         * @return The corresponding fragment, or null if no match is found.
         */
        private fun getFragment(itemId: Int): Fragment {
            val selectedFragment = when (itemId) {
                R.id.nav_chats -> {
                    ChatsFragment()
                }

                R.id.nav_calls -> {
                    CallsFragment()
                }

                R.id.nav_users -> {
                    UsersFragment()
                }

                R.id.nav_groups -> {
                    GroupsFragment()
                }

                else -> {
                    ChatsFragment()
                }
            }
            return selectedFragment
        }
    }

    private fun configureVoIP() {
        CometChatVoIP.init(this, applicationInfo.loadLabel(packageManager).toString())
        launchVoIP()
    }

    private fun launchVoIP() {
        if (!CometChatVoIP.hasReadPhoneStatePermission(this)) {
            CometChatVoIP.requestReadPhoneStatePermission(this, CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE)
            return
        }

        if (!CometChatVoIP.hasManageOwnCallsPermission(this)) {
            CometChatVoIP.requestManageOwnCallsPermission(this, CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS)
            return
        }

        if (!CometChatVoIP.hasAnswerPhoneCallsPermission(this)) {
            CometChatVoIP.requestAnswerPhoneCallsPermission(this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS)
            return
        }

        if (CometChatVoIP.hasReadPhoneStatePermission(this) && CometChatVoIP.hasManageOwnCallsPermission(
                this
            ) && CometChatVoIP.hasAnswerPhoneCallsPermission(
                this
            )
        ) {
            CometChatVoIP.hasEnabledPhoneAccountForVoIP(this, object : VoIPPermissionListener {
                override fun onPermissionsGranted() {
                    CometChatLogger.e(TAG, "VoIP Permissions granted")
                }

                override fun onPermissionsDenied(error: CometChatVoIPError?) {
                    CometChatLogger.e(TAG, "VoIP Permissions denied: " + error?.message)
                    CometChatVoIP.alertDialogForVoIP(this@HomeActivity)
                }
            })
        } else {
            CometChatLogger.e(TAG, "All VoIP Permissions denied.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppUtils.PushNotificationPermissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CometChatLogger.e(TAG, "Push Notification permission granted.")
                CometChatVoIP.requestPhoneStatePermissions(
                    this, CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE
                )
            } else {
                CometChatLogger.e(TAG, "Push Notification permission denied.")
            }

            CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CometChatLogger.e(TAG, "Read Phone State permission granted.")
                if (CometChatVoIP.hasManageOwnCallsPermission(this)) {
                    CometChatVoIP.requestAnswerPhoneCallsPermissions(
                        this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS
                    )
                } else {
                    CometChatVoIP.requestManageOwnCallsPermissions(
                        this, CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS
                    )
                }
            } else {
                CometChatLogger.e(TAG, "Read Phone State permission denied.")
            }

            CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CometChatLogger.e(TAG, "Manage Own Calls permission granted.")
                CometChatVoIP.requestAnswerPhoneCallsPermissions(
                    this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS
                )
            } else {
                CometChatLogger.e(TAG, "Manage Own Calls permission denied.")
            }

            CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CometChatLogger.e(TAG, "Answer Phone Calls permission granted.")
                launchVoIP()
            } else {
                CometChatLogger.e(TAG, "Answer Phone Calls permission denied.")
            }
        }
    }

}