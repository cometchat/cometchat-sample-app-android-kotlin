package com.cometchat.sampleapp.kotlin.fcm.ui.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.keyboard_utils.KeyBoardUtils
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.databinding.ActivityThreadMessageBinding
import com.cometchat.sampleapp.kotlin.fcm.viewmodels.ThreadMessageViewModel

class ThreadMessageActivity : AppCompatActivity() {
    private var binding: ActivityThreadMessageBinding? = null
    private var user: User? = null
    private var group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = ActivityThreadMessageBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        // Create an instance of the MessagesViewModel
        val viewModel: ThreadMessageViewModel = ViewModelProvider.NewInstanceFactory().create(
            ThreadMessageViewModel::class.java
        )
        viewModel.parentMessage.observe(
            this
        ) { parentMessage: BaseMessage -> this.setParentMessage(parentMessage) }
        val messageId = intent.getLongExtra(getString(R.string.app_message_id), -1)
        viewModel.fetchMessageDetails(messageId)

        // Set up back button behavior
        binding!!.backIcon.setOnClickListener { v: View? ->
            Utils.hideKeyBoard(
                this, binding!!.root
            )
            finish()
        }

        // Get the screen height
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Calculate 25% of the screen height
        val requiredHeight = (screenHeight * 0.35).toInt()
        binding!!.threadHeader.setMaxHeight(requiredHeight)
    }

    private fun setParentMessage(parentMessage: BaseMessage) {
        if (UIKitConstants.ReceiverType.USER.equals(
                parentMessage.receiverType, ignoreCase = true
            )
        ) {
            user = if (parentMessage.sender.uid.equals(
                    CometChatUIKit.getLoggedInUser().uid, ignoreCase = true
                )
            ) parentMessage.receiver as User else parentMessage.sender
        } else if (UIKitConstants.ReceiverType.GROUP.equals(
                parentMessage.receiverType, ignoreCase = true
            )
        ) {
            group = parentMessage.receiver as Group
        }

        KeyBoardUtils.setKeyboardVisibilityListener(
            this, binding!!.root
        ) { keyboardVisible: Boolean ->
            if (binding!!.messageComposer.messageInput.composeBox.isFocused && keyboardVisible) {
                if (binding!!.messageList.atBottom()) {
                    binding!!.messageList.scrollToBottom()
                }
            }
        }

        binding!!.messageList.setParentMessage(parentMessage.id)
        binding!!.messageComposer.parentMessageId = parentMessage.id
        binding!!.threadHeader.parentMessage = parentMessage

        // Set user or group data to the message header and composer
        if (user != null) {
            binding!!.messageList.user = user
            binding!!.messageComposer.user = user
        } else if (group != null) {
            binding!!.messageList.group = group
            binding!!.messageComposer.group = group
        }
    }
}
