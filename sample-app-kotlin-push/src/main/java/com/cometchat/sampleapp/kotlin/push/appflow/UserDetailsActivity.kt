package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.UserDetailsViewModel
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityUserDetailsBinding
import com.google.gson.Gson

class UserDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserDetailsBinding
    private var alertDialog: CometChatConfirmDialog? = null
    private lateinit var viewModel: UserDetailsViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        
        initViewModel()
        initClickListeners()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
    
    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(UserDetailsViewModel::class.java)
        viewModel.addListeners()
        
        // Extract user from intent
        val userJson = intent.getStringExtra(getString(R.string.app_user))
        if (userJson != null) {
            viewModel.setUser(Gson().fromJson(userJson, User::class.java))
        }
        
        // Extract baseMessage from intent
        val baseMessageJson = intent.getStringExtra(getString(R.string.app_base_message))
        if (baseMessageJson != null) {
            viewModel.setBaseMessage(Gson().fromJson(baseMessageJson, BaseMessage::class.java))
        }
        
        // Observe LiveData
        viewModel.user.observe(this, setUserHeader())
        viewModel.isUserBlockedByMe.observe(this, isUserBlockedByMeObserver)
        viewModel.isUserBlocked.observe(this, blockUserStateObserver())
        viewModel.isUserUnblocked.observe(this, unblockUserStateObserver())
        viewModel.isChatDeleted.observe(this, deleteChatStateObserver())
        viewModel.onCallStart().observe(this, onCallStart())
        viewModel.onCallStartError().observe(this, onCallStartError())
    }
    
    private fun initClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        
        binding.tvBlock.setOnClickListener {
            if (binding.tvBlock.text.toString() == getString(R.string.app_block)) {
                blockUser()
            } else {
                unblockUser()
            }
        }
        
        // Hide delete chat if no baseMessage was passed via intent
        val baseMessageJson = intent.getStringExtra(getString(R.string.app_base_message))
        if (baseMessageJson.isNullOrEmpty()) {
            binding.tvDeleteChat.visibility = View.GONE
        }
        
        binding.tvDeleteChat.setOnClickListener { deleteChat() }
        
        binding.cardVoiceCall.setOnClickListener {
            viewModel.startCall(CometChatConstants.CALL_TYPE_AUDIO)
        }
        
        binding.cardVideoCall.setOnClickListener {
            viewModel.startCall(CometChatConstants.CALL_TYPE_VIDEO)
        }
    }
    
    private fun deleteChatStateObserver(): Observer<Boolean> {
        return Observer { isChatDeleted ->
            if (isChatDeleted) {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_delete_chat_success), CometChatTheme.getColorBlack(this))
                finish()
            } else {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_delete_chat_error), CometChatTheme.getErrorColor(this))
            }
        }
    }
    
    private val isUserBlockedByMeObserver: Observer<Boolean> = Observer { isUserBlockedByMe ->
        if (isUserBlockedByMe) {
            binding.tvBlock.text = getString(R.string.app_unblock)
        } else {
            binding.tvBlock.text = getString(R.string.app_block)
        }
    }
    
    private fun blockUserStateObserver(): Observer<Boolean> {
        return Observer { isUserBlocked ->
            if (isUserBlocked) {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_block_user_success), CometChatTheme.getColorBlack(this))
            } else {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_block_user_error), CometChatTheme.getErrorColor(this))
            }
        }
    }
    
    private fun unblockUserStateObserver(): Observer<Boolean> {
        return Observer { isUserUnblocked ->
            if (isUserUnblocked) {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_unblock_user_success), CometChatTheme.getColorBlack(this))
            } else {
                alertDialog?.dismiss()
                showToast(getString(R.string.app_unblock_user_error), CometChatTheme.getErrorColor(this))
            }
        }
    }
    
    private fun setUserHeader(): Observer<User?> {
        return Observer { user ->
            if (user != null) {
                // Apply theme colors
                binding.tvVideoCall.compoundDrawableTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this))
                binding.tvVoiceCall.compoundDrawableTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this))
                binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                binding.toolbarTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
                binding.tvSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(this))
                binding.tvVoiceCall.setTextColor(CometChatTheme.getTextColorSecondary(this))
                binding.tvVideoCall.setTextColor(CometChatTheme.getTextColorSecondary(this))
                
                // Set user info
                binding.avatar.setAvatar(user.name, user.avatar)
                binding.tvTitle.text = user.name
                
                if (!Utils.isBlocked(user)) {
                    binding.infoMessage.visibility = View.GONE
                    binding.tvSubtitle.visibility = View.VISIBLE
                    binding.cardVideoCall.visibility = View.VISIBLE
                    binding.cardVoiceCall.visibility = View.VISIBLE
                    
                    if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                        binding.tvSubtitle.text = getString(com.cometchat.uikit.kotlin.R.string.cometchat_online)
                    } else {
                        if (user.lastActiveAt == 0L) {
                            binding.tvSubtitle.text = getString(com.cometchat.uikit.kotlin.R.string.cometchat_offline)
                        } else {
                            val lastSeen = Utils.getLastSeenTime(this, user.lastActiveAt)
                            binding.tvSubtitle.text = lastSeen
                            binding.tvSubtitle.isSelected = true
                        }
                    }
                } else {
                    binding.tvSubtitle.visibility = View.GONE
                    binding.cardVideoCall.visibility = View.GONE
                    binding.cardVoiceCall.visibility = View.GONE
                    
                    if (user.isBlockedByMe) {
                        binding.infoMessage.visibility = View.VISIBLE
                        binding.tvInfoMessage.text = String.format(
                            "%s %s",
                            getString(R.string.app_you_have_blocked_this_user),
                            user.name
                        )
                    } else {
                        binding.infoMessage.visibility = View.VISIBLE
                        binding.tvInfoMessage.text = String.format(
                            "%s %s",
                            user.name,
                            getString(R.string.app_has_blocked_you)
                        )
                    }
                }
            }
        }
    }
    
    private fun onCallStart(): Observer<Call> {
        return Observer { call ->
            com.cometchat.uikit.kotlin.calls.CometChatCallActivity.launchOutgoingCallScreen(this, call, null)
        }
    }
    
    private fun onCallStartError(): Observer<String?> {
        return Observer {
            showToast(
                getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong),
                CometChatTheme.getErrorColor(this)
            )
        }
    }
    
    private fun deleteChat() {
        showCometChatConfirmDialog(
            icon = ResourcesCompat.getDrawable(resources, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete, null),
            iconTint = CometChatTheme.getErrorColor(this),
            title = getString(R.string.app_delete_chat_title),
            subtitle = getString(R.string.app_delete_chat_subtitle),
            positiveButtonText = getString(R.string.app_delete_chat_positive_button),
            negativeButtonText = getString(R.string.app_delete_chat_negative_button),
            onPositiveButtonClick = {
                alertDialog?.showPositiveButtonProgress(true)
                viewModel.deleteChat()
            },
            onNegativeButtonClick = { alertDialog?.dismiss() }
        )
    }
    
    private fun blockUser() {
        showCometChatConfirmDialog(
            icon = ResourcesCompat.getDrawable(resources, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_block, null),
            iconTint = CometChatTheme.getErrorColor(this),
            title = getString(R.string.app_block_user_title),
            subtitle = getString(R.string.app_block_user_subtitle),
            positiveButtonText = getString(R.string.app_block_user_positive_button),
            negativeButtonText = getString(R.string.app_block_user_negative_button),
            onPositiveButtonClick = {
                alertDialog?.showPositiveButtonProgress(true)
                viewModel.blockUser()
            },
            onNegativeButtonClick = { alertDialog?.dismiss() }
        )
    }
    
    private fun unblockUser() {
        showCometChatConfirmDialog(
            icon = ResourcesCompat.getDrawable(resources, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_block, null),
            iconTint = CometChatTheme.getErrorColor(this),
            title = getString(R.string.app_unblock_user_title),
            subtitle = getString(R.string.app_unblock_user_subtitle),
            positiveButtonText = getString(R.string.app_unblock_user_positive_button),
            negativeButtonText = getString(R.string.app_unblock_user_negative_button),
            onPositiveButtonClick = {
                alertDialog?.showPositiveButtonProgress(true)
                viewModel.unblockUser()
            },
            onNegativeButtonClick = { alertDialog?.dismiss() }
        )
    }
    
    private fun showCometChatConfirmDialog(
        icon: Drawable?,
        @ColorInt iconTint: Int,
        title: String,
        subtitle: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClick: () -> Unit,
        onNegativeButtonClick: () -> Unit
    ) {
        alertDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        alertDialog?.apply {
            setConfirmDialogIcon(icon)
            setConfirmDialogIconTint(iconTint)
            setTitleText(title)
            setSubtitleText(subtitle)
            setPositiveButtonText(positiveButtonText)
            setNegativeButtonText(negativeButtonText)
            setOnPositiveButtonClick(View.OnClickListener { onPositiveButtonClick() })
            setOnNegativeButtonClick(View.OnClickListener { onNegativeButtonClick() })
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }
    
    private fun showToast(message: String, @ColorInt color: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
