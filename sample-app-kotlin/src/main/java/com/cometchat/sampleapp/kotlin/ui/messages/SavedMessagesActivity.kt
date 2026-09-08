package com.cometchat.sampleapp.kotlin.ui.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.ActivitySavedMessagesBinding

/**
 * User-level, cross-conversation list of the current user's saved messages, opened from the
 * user-avatar popup menu on the Chats tab.
 * Hosts [com.cometchat.uikit.kotlin.presentation.savedmessages.CometChatSavedMessages].
 * Because saved messages span all conversations, tapping a row opens that message's
 * conversation (jumping to the message) — the cross-chat equivalent of goto-message
 * navigation from search.
 *
 * ## Usage:
 * ```kotlin
 * SavedMessagesActivity.start(context)
 * ```
 */
class SavedMessagesActivity : AppCompatActivity() {

    companion object {
        /**
         * Starts SavedMessagesActivity.
         */
        fun start(context: Context) {
            context.startActivity(Intent(context, SavedMessagesActivity::class.java))
        }
    }

    private lateinit var binding: ActivitySavedMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySavedMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        binding.savedMessages.setOnBackClickListener { finish() }
        binding.savedMessages.setOnMessageClickListener { message ->
            openConversationAtMessage(message)
        }
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Opens the conversation the saved message belongs to, scrolled to that message.
     */
    private fun openConversationAtMessage(message: BaseMessage) {
        val goToMessageId = message.id.toLong()
        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            val group = message.receiver as? Group ?: return
            MessagesActivity.start(this, group = group, goToMessageId = goToMessageId)
        } else {
            // 1-1: the peer is the other party — the receiver if I sent it, else the sender.
            val myUid = CometChat.getLoggedInUser()?.uid
            val peer: User? =
                if (message.sender?.uid == myUid) message.receiver as? User else message.sender
            peer ?: return
            MessagesActivity.start(this, user = peer, goToMessageId = goToMessageId)
        }
        finish()
    }
}
