package com.cometchat.ai.sampleapp.kotlin.ui.agents

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
import com.cometchat.ai.sampleapp.kotlin.BuildConfig
import com.cometchat.ai.sampleapp.kotlin.R
import com.cometchat.ai.sampleapp.kotlin.databinding.ActivityAiAssistantUsersBinding
import com.cometchat.ai.sampleapp.kotlin.databinding.PopupUserProfileMenuBinding
import com.cometchat.ai.sampleapp.kotlin.ui.chat.AIAssistantChatActivity
import com.cometchat.ai.sampleapp.kotlin.ui.splash.SplashActivity
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Activity listing all AI Agents (users with role = "@agentic").
 *
 * Uses the v6 [com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers] component
 * with a [UsersRequest.UsersRequestBuilder] filtered by the AGENTIC_USER role.
 */
class AIAssistantUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAiAssistantUsersBinding.inflate(layoutInflater)

        // Filter: only show AI Agents (role = AGENTIC_USER).
        // The ViewModel defers its initial fetch until the view is attached to the
        // window, so setting the builder here (pre-attach) ensures the first fetch
        // already uses the filtered request.
        val requestBuilder = UsersRequest.UsersRequestBuilder()
            .setRoles(listOf(UIKitConstants.AIConstants.AGENTIC_USER))
        binding.users.setUsersRequestBuilder(requestBuilder)
        binding.users.setSearchRequestBuilder(requestBuilder)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.users.setTitle(getString(R.string.app_ai_agents))

        binding.users.setOnItemClick { user ->
            val intent = Intent(this, AIAssistantChatActivity::class.java)
            intent.putExtra(getString(R.string.app_user), user.toJson().toString())
            startActivity(intent)
        }

        setupOverflowMenu()

    }

    /**
     * Sets the logged-in user's avatar as the overflow-menu icon.
     * Tapping it opens a profile popup (username, logout, version).
     */
    private fun setupOverflowMenu() {
        if (!CometChatUIKit.isSDKInitialized()) return

        val user = CometChatUIKit.getLoggedInUser() ?: return

        val avatar = CometChatAvatar(this).apply {
            setAvatar(user.name, user.avatar)
            val layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_40dp),
                resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_40dp)
            )
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            this.layoutParams = layoutParams
            setOnClickListener {
                showUserProfilePopup(this)
            }
        }

        binding.users.setOverflowMenu(avatar)
    }

    private fun showUserProfilePopup(anchorView: View) {
        val popupBinding = PopupUserProfileMenuBinding.inflate(LayoutInflater.from(this))
        val popupWindow = PopupWindow(
            popupBinding.root,
            resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_250dp),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val user = CometChatUIKit.getLoggedInUser()
        popupBinding.tvUserName.text = user?.name ?: ""
        popupBinding.tvVersion.text = "V${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"

        popupBinding.tvUserName.setTextColor(CometChatTheme.getTextColorPrimary(this))
        popupBinding.tvVersion.setTextColor(CometChatTheme.getTextColorPrimary(this))

        popupBinding.tvUserName.setOnClickListener {
            popupWindow.dismiss()
        }

        popupBinding.tvLogout.setOnClickListener {
            CometChat.logout(object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    popupWindow.dismiss()
                    startActivity(Intent(this@AIAssistantUsersActivity, SplashActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }

                override fun onError(exception: CometChatException?) {
                    popupWindow.dismiss()
                }
            })
        }

        popupWindow.elevation = 5f
        val endMargin =
            resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_margin_2)
        val offsetX = anchorView.width - popupWindow.width - endMargin
        popupWindow.showAsDropDown(anchorView, offsetX, 0)
    }
}
