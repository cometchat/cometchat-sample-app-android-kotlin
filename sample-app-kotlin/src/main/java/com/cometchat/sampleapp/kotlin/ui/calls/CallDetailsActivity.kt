package com.cometchat.sampleapp.kotlin.ui.calls

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityCallDetailsBinding
import com.cometchat.uikit.core.CometChatUIKit
import com.google.gson.Gson
import java.util.Locale

/**
 * Activity displaying call log details.
 *
 * Shows:
 * - Call recipient information via CometChatMessageHeader
 * - Call type (incoming/outgoing/missed)
 * - Call date and duration
 *
 * ## Navigation:
 * - Receives call log data as JSON extras from CallsFragment
 * - Back button returns to previous screen
 *
 * Validates: Requirement 9.2
 */
class CallDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallDetailsBinding
    private lateinit var callLog: CallLog

    companion object {
        private const val TAG = "CallDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Parse call log from intent extras
        parseCallLogFromIntent()

        // Setup UI
        setupTheme()
        setupClickListeners()
        loadReceiverUser()
        updateInfoView()
    }

    /**
     * Parses the call log data from intent extras.
     */
    private fun parseCallLogFromIntent() {
        callLog = Gson().fromJson(
            intent.getStringExtra("callLog"),
            CallLog::class.java
        )
        callLog.initiator = Gson().fromJson(
            intent.getStringExtra("initiator"),
            CallUser::class.java
        )
        callLog.receiver = Gson().fromJson(
            intent.getStringExtra("receiver"),
            CallUser::class.java
        )
    }

    /**
     * Applies CometChat theme colors to UI elements.
     */
    private fun setupTheme() {
        binding.parentLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        binding.toolbarBackIcon.setColorFilter(CometChatTheme.getIconTintPrimary(this))
        binding.toolbarTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.toolbarDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this))
        binding.messageHeaderDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this))
        binding.infoLayout.setBackgroundColor(CometChatTheme.getBackgroundColor2(this))
        binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.tvInfoCallDuration.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.infoLayoutDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this))
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.toolbarBackIcon.setOnClickListener { finish() }
    }

    /**
     * Loads the receiver user and updates the message header.
     */
    private fun loadReceiverUser() {
        val initiator = callLog.initiator as? CallUser ?: return
        val isLoggedInUser = isLoggedInUser(initiator)
        
        val targetUser = if (isLoggedInUser) {
            callLog.receiver as? CallUser
        } else {
            initiator
        }

        if (targetUser == null) return

        CometChat.getUser(targetUser.uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                binding.messageHeader.apply {
                    // Configure message header
                    setVideoCallButtonVisibility(if (user.isHasBlockedMe || user.isBlockedByMe) View.GONE else View.VISIBLE)
                    setVoiceCallButtonVisibility(if (user.isHasBlockedMe || user.isBlockedByMe) View.GONE else View.VISIBLE)
                    setUser(user)
                    setUserStatusVisibility(View.GONE)
                    setBackButtonVisibility(View.GONE)
                }
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "Error fetching user: ${e.message}")
            }
        })
    }
    
    /**
     * Checks if the given CallUser is the logged-in user.
     */
    private fun isLoggedInUser(user: CallUser?): Boolean {
        return CometChatUIKit.getLoggedInUser()?.uid == (user?.uid ?: "")
    }

    /**
     * Updates the call info section based on call log data.
     */
    private fun updateInfoView() {
        val initiator = callLog.initiator as? CallUser
        val isLoggedInUser = initiator?.let { isLoggedInUser(it) } ?: false
        val isMissedOrUnanswered = callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
                callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED

        // Set call date
        binding.tvInfoDate.setDateText(Utils.callLogsTimeStamp(callLog.initiatedAt, null))

        // Set call duration
        val decimalValue = callLog.totalDurationInMinutes
        val minutes = decimalValue.toInt()
        val seconds = ((decimalValue - minutes) * 60).toInt()
        binding.tvInfoCallDuration.text = String.format(Locale.US, "%dm %ds", minutes, seconds)

        // Set call type info (title and icon)
        when {
            callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO -> {
                when {
                    isLoggedInUser -> {
                        // Outgoing call
                        binding.tvInfoTitle.setText(R.string.call_outgoing)
                        binding.tvInfoTitle.setTextAppearance(
                            CometChatTheme.getTextAppearanceHeading4Medium(this)
                        )
                        binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                        setupCallIcon(
                            AppCompatResources.getDrawable(
                                this,
                                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_outgoing_call
                            ),
                            CometChatTheme.getSuccessColor(this)
                        )
                    }
                    isMissedOrUnanswered -> {
                        // Missed call
                        binding.tvInfoTitle.setText(R.string.call_missed)
                        binding.tvInfoTitle.setTextAppearance(
                            CometChatTheme.getTextAppearanceHeading4Medium(this)
                        )
                        binding.tvInfoTitle.setTextColor(CometChatTheme.getErrorColor(this))
                        setupCallIcon(
                            AppCompatResources.getDrawable(
                                this,
                                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_missed_call
                            ),
                            CometChatTheme.getErrorColor(this)
                        )
                    }
                    else -> {
                        // Incoming call
                        binding.tvInfoTitle.setText(R.string.call_incoming)
                        binding.tvInfoTitle.setTextAppearance(
                            CometChatTheme.getTextAppearanceHeading4Medium(this)
                        )
                        binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                        setupCallIcon(
                            AppCompatResources.getDrawable(
                                this,
                                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_incoming_call
                            ),
                            CometChatTheme.getSuccessColor(this)
                        )
                    }
                }
            }
        }
    }

    /**
     * Sets up the call type icon with the specified drawable and tint.
     */
    private fun setupCallIcon(icon: Drawable?, @ColorInt iconTint: Int) {
        binding.ivInfoIcon.background = icon
        binding.ivInfoIcon.backgroundTintList = ColorStateList.valueOf(iconTint)
    }
}
