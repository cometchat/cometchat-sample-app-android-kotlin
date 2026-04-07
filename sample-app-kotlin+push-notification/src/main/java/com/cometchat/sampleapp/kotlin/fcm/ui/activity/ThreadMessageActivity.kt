package com.cometchat.sampleapp.kotlin.fcm.ui.activity

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.keyboard_utils.KeyBoardUtils
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.databinding.ActivityThreadMessageBinding
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants
import com.cometchat.sampleapp.kotlin.fcm.viewmodels.ThreadMessageViewModel
import org.json.JSONException
import org.json.JSONObject

class ThreadMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThreadMessageBinding
    private var user: User? = null
    private var group: Group? = null
    private var goToMessage: BaseMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTheme()
        adjustWindowSettings()
        windowInsetsListener()

        val viewModel: ThreadMessageViewModel = ViewModelProvider.NewInstanceFactory().create(ThreadMessageViewModel::class.java)
        val goToMessageJson = intent.getStringExtra(getString(R.string.app_go_to_message))
        val rawMessage = intent.getStringExtra(AppConstants.JSONConstants.RAW_JSON)
        val replyCount = intent.getIntExtra(AppConstants.JSONConstants.REPLY_COUNT, 0)
        try {
            if (goToMessageJson != null) {
                goToMessage = BaseMessage.processMessage(JSONObject(goToMessageJson))
            }
            if (rawMessage != null) {
                val parentMessage = BaseMessage.processMessage(JSONObject(rawMessage))
                parentMessage.replyCount = replyCount
                viewModel.setParentMessage(parentMessage)
            }
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        viewModel.addUserListener()
        viewModel.parentMessage.observe(this, this::setParentMessage)
        viewModel.userBlockStatus.observe(this, this::updateUserBlockStatus)
        viewModel.unblockButtonState.observe(this, this::setUnblockButtonState)

        if (user != null)
            viewModel.setUser(user!!)

        binding.unblockBtn.setOnClickListener { _ -> viewModel.unblockUser() }

        setupUI()
    }

    private fun setUpTheme() {
        binding.backIcon.setColorFilter(CometChatTheme.getIconTintPrimary(this))
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.tvSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this))
        binding.unblockBtn.strokeColor = CometChatTheme.getStrokeColorDark(this)
        binding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
    }

    private fun windowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val bottomPadding = maxOf(imeInsets.bottom, navBarInsets.bottom)
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            if (binding.singleLineComposer.isFocused && isImeVisible) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom()
                }
            }
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                bottomPadding
            )
            insets
        }
    }

    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun setupUI() {
        // Set up back button behavior
        binding.backIcon.setOnClickListener { _: View? ->
            Utils.hideKeyBoard(this, binding.root)
            finish()
        }

        // Get the screen height
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Calculate 25% of the screen height
        val requiredHeight = (screenHeight * 0.35).toInt()
        binding.threadHeader.maxHeight = requiredHeight

        if (user != null) {
            updateUserBlockStatus(user!!)
        }
    }

    private fun setParentMessage(parentMessage: BaseMessage) {
        if (UIKitConstants.ReceiverType.USER.equals(parentMessage.receiverType, ignoreCase = true)) {
            user = if (parentMessage.sender.uid.equals(
                    CometChatUIKit.getLoggedInUser().uid,
                    ignoreCase = true
                )
            ) parentMessage.receiver as User else parentMessage.sender
        } else if (UIKitConstants.ReceiverType.GROUP.equals(parentMessage.receiverType, ignoreCase = true)) {
            group = parentMessage.receiver as Group
        }

        if (goToMessage != null) binding.messageList.gotoMessage(goToMessage!!.id)
        binding.tvSubtitle.text = if (user != null) user!!.name else if (group != null) group!!.name else ""
        binding.tvSubtitle.visibility = if (binding.tvSubtitle.text.toString().isEmpty()) View.GONE else View.VISIBLE
        binding.messageList.setParentMessage(parentMessage.id)
        binding.singleLineComposer.setParentMessageId(parentMessage.id)
        binding.threadHeader.parentMessage = parentMessage
        binding.threadHeader.reactionVisibility = View.GONE

        if (user != null) {
            binding.messageList.user = user
            binding.singleLineComposer.setUser(user)
        } else if (group != null) {
            binding.messageList.group = group
            binding.singleLineComposer.setGroup(group)
        }
    }

    private fun updateUserBlockStatus(user: User) {
        if (user.isBlockedByMe) {
            binding.singleLineComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.singleLineComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }

    private fun setUnblockButtonState(dialogState: UIKitConstants.DialogState) {
        if (dialogState == UIKitConstants.DialogState.INITIATED) {
            binding.unblockText.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
        } else if (dialogState == UIKitConstants.DialogState.SUCCESS || dialogState == UIKitConstants.DialogState.FAILURE) {
            binding.unblockText.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
        }
    }
}
