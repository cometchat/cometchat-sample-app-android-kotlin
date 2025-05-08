package com.cometchat.sampleapp.kotlin.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.keyboard_utils.KeyBoardUtils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityThreadMessageBinding
import com.cometchat.sampleapp.kotlin.viewmodels.ThreadMessageViewModel
import com.google.gson.Gson

class ThreadMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThreadMessageBinding
    private var user: User? = null
    private var group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWindowSettings()
        windowInsetsListener()

        // Create an instance of the MessagesViewModel
        val viewModel: ThreadMessageViewModel = ViewModelProvider.NewInstanceFactory().create(ThreadMessageViewModel::class.java)
        viewModel.fetchMessageDetails(intent.getIntExtra(getString(R.string.app_message_id), -1))
        user = Gson().fromJson(intent.getStringExtra("user"), User::class.java)

        viewModel.addUserListener()
        viewModel.parentMessage.observe(this, this::setParentMessage)
        viewModel.userBlockStatus.observe(this, this::updateUserBlockStatus)
        viewModel.unblockButtonState.observe(this, this::setUnblockButtonState)
        viewModel.parentMessage.observe(this) { parentMessage: BaseMessage -> this.setParentMessage(parentMessage) }

        if (user != null)
            viewModel.setUser(user!!)

        binding.unblockBtn.setOnClickListener { _ -> viewModel.unblockUser() }

        setupUI()
    }

    private fun windowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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
            user = if (parentMessage.sender.uid.equals(CometChatUIKit.getLoggedInUser().uid, ignoreCase = true)) parentMessage.receiver as User else parentMessage.sender
        } else if (UIKitConstants.ReceiverType.GROUP.equals(parentMessage.receiverType, ignoreCase = true)) {
            group = parentMessage.receiver as Group
        }

        KeyBoardUtils.setKeyboardVisibilityListener(this, binding.root) { keyboardVisible: Boolean ->
            if (binding.messageComposer.messageInput.composeBox.isFocused && keyboardVisible) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom()
                }
            }
        }

        binding.tvSubtitle.text = if (user != null) user!!.name else if (group != null) group!!.name else ""
        binding.tvSubtitle.visibility = if (binding.tvSubtitle.text.toString().isEmpty()) View.GONE else View.VISIBLE

        binding.messageList.setParentMessage(parentMessage.id)
        binding.messageComposer.parentMessageId = parentMessage.id
        binding.threadHeader.parentMessage = parentMessage

        // Set user or group data to the message header and composer
        if (user != null) {
            binding.messageList.user = user
            binding.messageComposer.user = user
        } else if (group != null) {
            binding.messageList.group = group
            binding.messageComposer.group = group
        }
    }

    private fun updateUserBlockStatus(user: User) {
        if (user.isBlockedByMe) {
            binding.messageComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.messageComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }

    private fun setUnblockButtonState(dialogState: DialogState) {
        if (dialogState == DialogState.INITIATED) {
            binding.unblockText.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
        } else if (dialogState == DialogState.SUCCESS || dialogState == DialogState.FAILURE) {
            binding.unblockText.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
        }
    }
}
