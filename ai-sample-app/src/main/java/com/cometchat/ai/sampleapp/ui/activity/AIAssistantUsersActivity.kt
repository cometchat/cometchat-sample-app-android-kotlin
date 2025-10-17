package com.cometchat.ai.sampleapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.ai.sampleapp.BuildConfig
import com.cometchat.ai.sampleapp.R
import com.cometchat.ai.sampleapp.data.repository.Repository
import com.cometchat.ai.sampleapp.databinding.ActivityAiassistantUsersBinding
import com.cometchat.ai.sampleapp.databinding.UserProfilePopupMenuLayoutBinding
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest.UsersRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar

class AIAssistantUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiassistantUsersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAiassistantUsersBinding.inflate(layoutInflater)
        binding.users.setUsersRequestBuilder(UsersRequestBuilder().setRoles(listOf<String?>(UIKitConstants.AIConstants.AGENTIC_USER)))
        binding.users.setSearchRequestBuilder(UsersRequestBuilder().setRoles(listOf<String?>(UIKitConstants.AIConstants.AGENTIC_USER)))
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot()) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // binding.users.setToolbarVisibility(View.GONE)
        binding.users.setOnItemClick { view1, position, user ->
            val intent = Intent(this, AIAssistantChatActivity::class.java)
            intent.putExtra(getString(R.string.app_user), user.toJson().toString())
            startActivity(intent)
        }

        binding.users.setTitleText(getString(R.string.app_ai_agents))
        binding.users.overflowMenu = getLogoutView()
    }

    /**
     * Creates a logout view that displays a logout icon and handles logout clicks.
     *
     * @return A View representing the logout option.
     */
    private fun getLogoutView(): View? {
        if (!CometChatUIKit.isSDKInitialized()) return null
        val user = CometChatUIKit.getLoggedInUser()
        if (user != null) {
            val cometchatAvatar = CometChatAvatar(this)
            cometchatAvatar.setAvatar(user.getName(), user.getAvatar())
            val layoutParams = LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_40dp),
                getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_40dp)
            )
            layoutParams.setLayoutDirection(Gravity.CENTER_VERTICAL)
            cometchatAvatar.setLayoutParams(layoutParams)
            cometchatAvatar.setOnClickListener { v: View? ->
                showCustomMenu(binding.users.binding.toolbar)
            }
            return cometchatAvatar
        }
        return null
    }

    private fun showCustomMenu(anchorView: View) {
        val popupMenuBinding: UserProfilePopupMenuLayoutBinding =
            UserProfilePopupMenuLayoutBinding.inflate(LayoutInflater.from(this))
        val popupWindow = PopupWindow(
            popupMenuBinding.getRoot(),
            getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_250dp),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupMenuBinding.tvUserName.text = CometChatUIKit.getLoggedInUser().getName()
        val version = "V" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")"
        popupMenuBinding.tvVersion.text = version
        popupMenuBinding.tvUserName.setOnClickListener { view -> popupWindow.dismiss() }
        popupMenuBinding.tvLogout.setOnClickListener { view ->
            Repository.logout(object : CometChat.CallbackListener<String>() {
                override fun onSuccess(s: String?) {
                    startActivity(Intent(this@AIAssistantUsersActivity, SplashActivity::class.java))
                    finish()
                }

                override fun onError(e: CometChatException?) {}
            })
            popupWindow.dismiss()
        }

        popupMenuBinding.tvUserName.setTextColor(CometChatTheme.getTextColorPrimary(this))
        popupMenuBinding.tvVersion.setTextColor(CometChatTheme.getTextColorPrimary(this))

        popupWindow.elevation = 5f

        val endMargin =
            getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_margin_2)
        val anchorWidth = anchorView.width
        val offsetX = anchorWidth - popupWindow.width - endMargin
        val offsetY = 0
        popupWindow.showAsDropDown(anchorView, offsetX, offsetY)
    }
}