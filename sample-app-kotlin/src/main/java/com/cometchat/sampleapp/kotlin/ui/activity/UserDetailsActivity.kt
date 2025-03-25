package com.cometchat.sampleapp.kotlin.ui.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
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
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.calls.CometChatCallActivity
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityUserDetailsBinding
import com.cometchat.sampleapp.kotlin.utils.AppUtils.customToast
import com.cometchat.sampleapp.kotlin.viewmodels.UserDetailsViewModel
import com.google.gson.Gson

class UserDetailsActivity : AppCompatActivity() {
    private var binding: ActivityUserDetailsBinding? = null
    private var alertDialog: CometChatConfirmDialog? = null
    private var viewModel: UserDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initViewModel()

        initClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.removeListeners()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(
            UserDetailsViewModel::class.java
        )
        viewModel!!.addListeners()
        viewModel!!.setUser(
            Gson().fromJson(
                intent.getStringExtra(getString(R.string.app_user)), User::class.java
            )
        )
        viewModel!!.setBaseMessage(
            Gson().fromJson(
                intent.getStringExtra(getString(R.string.app_base_message)), BaseMessage::class.java
            )
        )
        viewModel!!.user.observe(this, setUserHeader())
        viewModel!!.isUserBlockedByMe.observe(this, isUserBlockedByMe)
        viewModel!!.isUserBlocked.observe(this, blockUserStateObserver())
        viewModel!!.isUserUnblocked.observe(this, unblockUserStateObserver())
        viewModel!!.isChatDeleted.observe(this, daleteChatStateObserver())
        viewModel!!.onCallStart().observe(this, onCallStart())
        viewModel!!.onCallStartError().observe(this, onCallStartError())
    }

    private fun initClickListeners() {
        binding!!.ivBack.setOnClickListener { v: View? -> finish() }

        binding!!.tvBlock.setOnClickListener { v: View? ->
            if (binding!!.tvBlock.text.toString() == getString(R.string.app_block)) {
                blockUser()
            } else {
                unblockUser()
            }
        }

        if (viewModel?.getBaseMessage()?.getValue() == null) binding!!.tvDeleteChat.visibility = View.GONE

        binding!!.tvDeleteChat.setOnClickListener { v: View? -> deleteChat() }

        binding!!.cardVoiceCall.setOnClickListener { v: View? ->
            viewModel!!.startCall(
                CometChatConstants.CALL_TYPE_AUDIO
            )
        }

        binding!!.cardVideoCall.setOnClickListener { v: View? ->
            viewModel!!.startCall(
                CometChatConstants.CALL_TYPE_VIDEO
            )
        }
    }

    private fun daleteChatStateObserver(): Observer<Boolean> {
        return Observer { isChatDeleted: Boolean ->
            if (isChatDeleted) {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_delete_chat_success), CometChatTheme.getColorBlack(
                        this
                    )
                )
                finish()
            } else {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_delete_chat_error), CometChatTheme.getErrorColor(
                        this
                    )
                )
            }
        }
    }

    private val isUserBlockedByMe: Observer<Boolean>
        get() = Observer { isUserBlockedByMe: Boolean ->
            if (isUserBlockedByMe) {
                binding!!.tvBlock.text = getString(R.string.app_unblock)
            } else {
                binding!!.tvBlock.text = getString(R.string.app_block)
            }
        }

    private fun blockUserStateObserver(): Observer<Boolean> {
        return Observer { isUserBlocked: Boolean ->
            if (isUserBlocked) {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_block_user_success), CometChatTheme.getColorBlack(
                        this
                    )
                )
            } else {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_block_user_error), CometChatTheme.getErrorColor(
                        this
                    )
                )
            }
        }
    }

    private fun unblockUserStateObserver(): Observer<Boolean> {
        return Observer { isUserUnblocked: Boolean ->
            if (isUserUnblocked) {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_unblock_user_success), CometChatTheme.getColorBlack(
                        this
                    )
                )
            } else {
                alertDialog!!.dismiss()
                customToast(
                    this, getString(R.string.app_unblock_user_error), CometChatTheme.getErrorColor(
                        this
                    )
                )
            }
        }
    }

    private fun setUserHeader(): Observer<in User?> {
        return Observer { user: User? ->
            if (user != null) {
                binding!!.avatar.setAvatar(user.name, user.avatar)
                binding!!.tvTitle.text = user.name
                if (!Utils.isBlocked(user)) {
                    binding!!.infoMessage.visibility = View.GONE
                    binding!!.tvSubtitle.visibility = View.VISIBLE
                    binding!!.cardVideoCall.visibility = View.VISIBLE
                    binding!!.cardVoiceCall.visibility = View.VISIBLE
                    if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                        binding!!.tvSubtitle.text = resources.getString(com.cometchat.chatuikit.R.string.cometchat_online)
                    } else {
                        if (user.lastActiveAt == 0L) {
                            binding!!.tvSubtitle.text = getString(com.cometchat.chatuikit.R.string.cometchat_offline)
                        } else {
                            val lastSeen = Utils.getLastSeenTime(this, user.lastActiveAt)
                            binding!!.tvSubtitle.text = lastSeen
                            binding!!.tvSubtitle.isSelected = true
                        }
                    }
                } else {
                    binding!!.tvSubtitle.visibility = View.GONE
                    binding!!.cardVideoCall.visibility = View.GONE
                    binding!!.cardVoiceCall.visibility = View.GONE
                    if (user.isBlockedByMe) {
                        binding!!.infoMessage.visibility = View.VISIBLE
                        binding!!.tvInfoMessage.text = String.format("%s %s", getString(R.string.app_you_have_blocked_this_user), user.name)
                    } else {
                        binding!!.infoMessage.visibility = View.VISIBLE
                        binding!!.tvInfoMessage.text = String.format("%s %s", user.name, getString(R.string.app_has_blocked_you))
                    }
                }
            }
        }
    }


    private fun onCallStart(): Observer<Call> {
        return Observer { call: Call? ->
            CometChatCallActivity.launchOutgoingCallScreen(
                this, call!!, null
            )
        }
    }

    private fun onCallStartError(): Observer<in String?> {
        return Observer {
            customToast(
                this, getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong), CometChatTheme.getErrorColor(this)
            )
        }
    }

    private fun deleteChat() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(
                resources, com.cometchat.chatuikit.R.drawable.cometchat_ic_delete, null
            ),
            CometChatTheme.getErrorColor(
                this
            ),
            getString(R.string.app_delete_chat_title),
            getString(R.string.app_delete_chat_subtitle),
            getString(R.string.app_delete_chat_positive_button),
            getString(R.string.app_delete_chat_negative_button),
            { view: View? ->
                alertDialog!!.hidePositiveButtonProgressBar(false)
                viewModel!!.deleteChat()
            },
            { view: View? -> alertDialog!!.dismiss() },
            0,
            false
        )
    }

    private fun blockUser() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(
                resources, com.cometchat.chatuikit.R.drawable.cometchat_ic_block, null
            ),
            CometChatTheme.getErrorColor(
                this
            ),
            getString(R.string.app_block_user_title),
            getString(R.string.app_block_user_subtitle),
            getString(R.string.app_block_user_positive_button),
            getString(R.string.app_block_user_negative_button),
            { view: View? ->
                alertDialog!!.hidePositiveButtonProgressBar(false)
                viewModel!!.blockUser()
            },
            { view: View? -> alertDialog!!.dismiss() },
            0,
            false
        )
    }

    private fun unblockUser() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(
                resources, com.cometchat.chatuikit.R.drawable.cometchat_ic_block, null
            ),
            CometChatTheme.getErrorColor(
                this
            ),
            getString(R.string.app_unblock_user_title),
            getString(R.string.app_unblock_user_subtitle),
            getString(R.string.app_unblock_user_positive_button),
            getString(R.string.app_unblock_user_negative_button),
            { view: View? ->
                alertDialog!!.hidePositiveButtonProgressBar(false)
                viewModel!!.unblockUser()
            },
            { view: View? -> alertDialog!!.dismiss() },
            0,
            false
        )
    }

    private fun showCometChatConfirmDialog(
        @StyleRes style: Int,
        icon: Drawable?,
        @ColorInt iconTint: Int,
        title: String,
        subtitle: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClick: View.OnClickListener,
        onNegativeButtonClick: View.OnClickListener,
        elevation: Int,
        cancelable: Boolean
    ) {
        alertDialog = CometChatConfirmDialog(this, style)
        alertDialog!!.confirmDialogIcon = icon
        alertDialog!!.setConfirmDialogIconTint(iconTint)
        alertDialog!!.titleText = title
        alertDialog!!.subtitleText = subtitle
        alertDialog!!.positiveButtonText = positiveButtonText
        alertDialog!!.negativeButtonText = negativeButtonText
        alertDialog!!.onPositiveButtonClick = onPositiveButtonClick
        alertDialog!!.onNegativeButtonClick = onNegativeButtonClick
        alertDialog!!.confirmDialogElevation = elevation
        alertDialog!!.setCancelable(cancelable)
        alertDialog!!.show()
    }
}
