package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.adapters.CallDetailsTabAdapter
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.CallDetailsParticipantsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.CallDetailsRecordingsFragment
import com.cometchat.sampleapp.kotlin.push.appflow.fragments.CallDetailsHistoryFragment
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityCallDetailsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import java.util.Locale

class CallDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCallDetailsBinding
    private lateinit var callLog: CallLog
    
    private val receiverUser = MutableLiveData<User>()
    private val callDuration = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        
        // Extract call log from intent
        callLog = Gson().fromJson(intent.getStringExtra("callLog"), CallLog::class.java)
        callLog.initiator = Gson().fromJson(intent.getStringExtra("initiator"), CallUser::class.java)
        callLog.receiver = Gson().fromJson(intent.getStringExtra("receiver"), CallUser::class.java)
        
        initViewModel()
        initTabFragment()
        initClickListeners()
        applyThemeColors()
    }
    
    private fun initViewModel() {
        setCallDuration()
        
        val initiator = callLog.initiator as CallUser
        val isLoggedInUser = isLoggedInUser(initiator)
        val user = if (isLoggedInUser) callLog.receiver as CallUser else initiator
        
        CometChat.getUser(user.uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                receiverUser.value = user
            }
            
            override fun onError(e: CometChatException) {}
        })
        
        receiverUser.observe(this) { user ->
            if (user != null) {
                binding.messageHeader.setVideoCallButtonVisibility(
                    if (user.isHasBlockedMe || user.isBlockedByMe) View.GONE else View.VISIBLE
                )
                binding.messageHeader.setVoiceCallButtonVisibility(
                    if (user.isHasBlockedMe || user.isBlockedByMe) View.GONE else View.VISIBLE
                )
                binding.messageHeader.setUser(user)
            }
            binding.messageHeader.setBackButtonVisibility(View.GONE)
        }
        
        callDuration.observe(this) { duration ->
            binding.tvInfoCallDuration.text = duration
        }
        
        updateInfoView()
    }
    
    private fun setCallDuration() {
        var minutes = 0
        var seconds = 0
        val decimalValue = callLog.totalDurationInMinutes
        minutes = decimalValue.toInt()
        seconds = ((decimalValue - minutes) * 60).toInt()
        callDuration.value = String.format(Locale.US, "%dm %ds", minutes, seconds)
    }
    
    private fun initClickListeners() {
        binding.toolbarBackIcon.setOnClickListener { finish() }
    }

    private fun initTabFragment() {
        val adapter = CallDetailsTabAdapter(this)
        adapter.addFragment(
            CallDetailsParticipantsFragment(),
            getString(R.string.app_call_details_participants),
            callLog
        )
        adapter.addFragment(
            CallDetailsRecordingsFragment(),
            getString(R.string.app_call_details_recordings),
            callLog
        )
        adapter.addFragment(
            CallDetailsHistoryFragment(),
            getString(R.string.app_call_details_history),
            callLog
        )
        binding.viewPager.adapter = adapter
        
        binding.tabLayout.setSelectedTabIndicatorColor(CometChatTheme.getTextColorHighlight(this))
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = capitalizeFirstLetter(adapter.getTabTitle(position))
        }.attach()
        
        styleTabs(binding.tabLayout)
    }
    
    private fun updateInfoView() {
        val isLoggedInUser = isLoggedInUser(callLog.initiator as CallUser)
        val isMissedOrUnanswered = callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
                callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED
        
        binding.tvInfoDate.setDateText(Utils.callLogsTimeStamp(callLog.initiatedAt, null))
        
        if (callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO) {
            
            when {
                isLoggedInUser -> {
                    binding.tvInfoTitle.setText(R.string.app_call_outgoing)
                    binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                    setupCallIcon(
                        AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_outgoing_call),
                        CometChatTheme.getSuccessColor(this)
                    )
                }
                isMissedOrUnanswered -> {
                    binding.tvInfoTitle.setText(R.string.app_call_missed)
                    binding.tvInfoTitle.setTextColor(CometChatTheme.getErrorColor(this))
                    setupCallIcon(
                        AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_missed_call),
                        CometChatTheme.getErrorColor(this)
                    )
                }
                else -> {
                    binding.tvInfoTitle.setText(R.string.app_call_incoming)
                    binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                    setupCallIcon(
                        AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_incoming_call),
                        CometChatTheme.getSuccessColor(this)
                    )
                }
            }
        }
    }
    
    private fun setupCallIcon(icon: Drawable?, @ColorInt iconTint: Int) {
        binding.ivInfoIcon.background = icon
        binding.ivInfoIcon.backgroundTintList = ColorStateList.valueOf(iconTint)
    }

    private fun applyThemeColors() {
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
    
    private fun capitalizeFirstLetter(text: String?): String? {
        if (text.isNullOrEmpty()) return text
        return text.substring(0, 1).uppercase(Locale.getDefault()) + 
               text.substring(1).lowercase(Locale.getDefault())
    }
    
    private fun styleTabs(tabLayout: TabLayout) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab != null) {
                val tabTextView = TextView(this)
                tabTextView.text = tab.text
                tabTextView.gravity = Gravity.CENTER
                tab.customView = tabTextView
                setTabTextStyle(tabTextView, i == tabLayout.selectedTabPosition)
            }
        }
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.customView as? TextView)?.let { setTabTextStyle(it, true) }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.customView as? TextView)?.let { setTabTextStyle(it, false) }
            }
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun setTabTextStyle(tabTextView: TextView, isActive: Boolean) {
        tabTextView.setTypeface(tabTextView.typeface, Typeface.BOLD)
        if (isActive) {
            tabTextView.setTextColor(CometChatTheme.getTextColorHighlight(this))
        } else {
            tabTextView.setTextColor(CometChatTheme.getTextColorSecondary(this))
        }
    }
    
    /**
     * Checks if the given CallUser is the logged-in user.
     * This is a local implementation of CallUtils.isLoggedInUser to avoid
     * dependency issues with the calls SDK compileOnly configuration.
     */
    private fun isLoggedInUser(user: CallUser?): Boolean {
        return CometChatUIKit.getLoggedInUser()?.uid == (user?.uid ?: "")
    }
}
