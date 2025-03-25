package com.cometchat.sampleapp.kotlin.fcm.ui.activity

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.calls.utils.CallUtils
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.databinding.ActivityCallDetailsBinding
import com.cometchat.sampleapp.kotlin.fcm.ui.adapters.CallDetailsTabFragmentAdapter
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.CallDetailsTabHistoryFragment
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.CallDetailsTabParticipantsFragment
import com.cometchat.sampleapp.kotlin.fcm.ui.fragments.CallDetailsTabRecordingFragment
import com.cometchat.sampleapp.kotlin.fcm.viewmodels.CallDetailsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import java.util.Locale

class CallDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallDetailsBinding
    private lateinit var viewModel: CallDetailsViewModel
    private lateinit var callLog: CallLog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallDetailsBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        callLog = Gson().fromJson(
            intent.getStringExtra("callLog"), CallLog::class.java
        )
        callLog.initiator = Gson().fromJson(
            intent.getStringExtra("initiator"), CallUser::class.java
        )
        callLog.receiver = Gson().fromJson(
            intent.getStringExtra("receiver"), CallUser::class.java
        )

        initViewModel()

        initTabFragment()

        initClickListeners()
    }

    private fun initViewModel() {
        viewModel = CallDetailsViewModel()
        viewModel.setCallLog(callLog)

        viewModel.callLog.observe(this) { callLog: CallLog? ->
            if (callLog != null) {
                this.callLog = callLog
            }
            updateInfoView()
        }

        viewModel.callDuration.observe(
            this
        ) { callDuration: String? ->
            binding.tvInfoCallDuration.text = callDuration
        }

        viewModel.receiverUser.observe(this) { user: User? ->
            binding.messageHeader.user = user!!
            binding.messageHeader.userStatusVisibility = View.GONE
            binding.messageHeader.setBackIconVisibility(View.GONE)
        }
    }

    private fun initClickListeners() {
        binding.toolbarBackIcon.setOnClickListener { view: View? -> finish() }
    }

    private fun initTabFragment() {
        val adapter = CallDetailsTabFragmentAdapter(this)
        adapter.addFragment(
            CallDetailsTabParticipantsFragment(), getString(R.string.app_call_details_participants), callLog
        )
        adapter.addFragment(
            CallDetailsTabRecordingFragment(), getString(R.string.app_call_details_recordings), callLog
        )
        adapter.addFragment(
            CallDetailsTabHistoryFragment(), getString(R.string.app_call_details_history), callLog
        )
        binding.viewPager.adapter = adapter

        binding.tabLayout.setSelectedTabIndicatorColor(CometChatTheme.getTextColorHighlight(this))

        // Attach TabLayout with ViewPager2
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.setText(
                capitalizeFirstLetter(
                    adapter.getTabTitle(
                        position
                    )
                )
            )
        }.attach()

        styleTabs(binding.tabLayout)
    }

    private fun updateInfoView() {
        val isLoggedInUser = CallUtils.isLoggedInUser(callLog.initiator as CallUser)
        val isMissedOrUnanswered =
            callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED || callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED
        binding.tvInfoDate.dateText = Utils.callLogsTimeStamp(
            callLog.initiatedAt, null
        )
        if (callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO || callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO || callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO) {
            if (isLoggedInUser) {
                binding.tvInfoTitle.setText(R.string.app_call_outgoing)
                binding.tvInfoTitle.setTextAppearance(
                    CometChatTheme.getTextAppearanceHeading4Medium(
                        this
                    )
                )
                binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                setupCallIcon(
                    binding.ivInfoIcon, AppCompatResources.getDrawable(
                        this, com.cometchat.chatuikit.R.drawable.cometchat_ic_outgoing_call
                    ), CometChatTheme.getSuccessColor(
                        this
                    )
                )
            } else if (isMissedOrUnanswered) {
                binding.tvInfoTitle.setText(R.string.app_call_missed)
                binding.tvInfoTitle.setTextAppearance(
                    CometChatTheme.getTextAppearanceHeading4Medium(
                        this
                    )
                )
                binding.tvInfoTitle.setTextColor(CometChatTheme.getErrorColor(this))
                setupCallIcon(
                    binding.ivInfoIcon, AppCompatResources.getDrawable(
                        this, com.cometchat.chatuikit.R.drawable.cometchat_ic_missed_call
                    ), CometChatTheme.getErrorColor(
                        this
                    )
                )
            } else {
                binding.tvInfoTitle.setText(R.string.app_call_incoming)
                binding.tvInfoTitle.setTextAppearance(
                    CometChatTheme.getTextAppearanceHeading4Medium(
                        this
                    )
                )
                binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                setupCallIcon(
                    binding.ivInfoIcon, AppCompatResources.getDrawable(
                        this, com.cometchat.chatuikit.R.drawable.cometchat_ic_incoming_call
                    ), CometChatTheme.getSuccessColor(
                        this
                    )
                )
            }
        }
    }

    private fun setupCallIcon(
        imageView: ImageView,
        icon: Drawable?,
        @ColorInt iconTint: Int
    ) {
        imageView.background = icon
        imageView.backgroundTintList = ColorStateList.valueOf(iconTint)
    }

    /**
     * Capitalizes only the first letter of a string.
     */
    private fun capitalizeFirstLetter(text: String?): String? {
        if (text == null || text.isEmpty()) {
            return text
        }
        return text
            .substring(0, 1)
            .uppercase(Locale.getDefault()) + text
            .substring(1)
            .lowercase(
                Locale.getDefault()
            )
    }

    /**
     * Styles tabs to set active and inactive text color programmatically.
     */
    private fun styleTabs(tabLayout: TabLayout) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab != null) {
                val tabTextView = TextView(this)
                tabTextView.text = tab.text
                tabTextView.gravity = Gravity.CENTER
                tab.setCustomView(tabTextView)

                // Set initial colors
                setTabTextStyle(tabTextView, i == tabLayout.selectedTabPosition)
            }
        }

        // Listen for tab selection changes to update styles
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabTextView = tab.customView as TextView?
                if (tabTextView != null) {
                    setTabTextStyle(tabTextView, true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabTextView = tab.customView as TextView?
                if (tabTextView != null) {
                    setTabTextStyle(tabTextView, false)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) { // Optional: Handle reselection if needed
            }
        })
    }

    private fun setTabTextStyle(
        tabTextView: TextView,
        isActive: Boolean
    ) {
        tabTextView.setTextAppearance(CometChatTheme.getTextAppearanceHeading4Medium(this))
        tabTextView.setTypeface(tabTextView.typeface, Typeface.BOLD)
        if (isActive) {
            tabTextView.setTextColor(CometChatTheme.getTextColorHighlight(this))
        } else {
            tabTextView.setTextColor(CometChatTheme.getTextColorSecondary(this))
        }
    }
}
