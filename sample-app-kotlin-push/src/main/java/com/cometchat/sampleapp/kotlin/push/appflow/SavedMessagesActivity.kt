package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.presentation.savedmessages.CometChatSavedMessages
import com.cometchat.sampleapp.kotlin.push.R
import com.google.gson.Gson

/**
 * User-level, cross-conversation list of the current user's saved messages, opened from the
 * message-header ⋮ menu. Hosts [CometChatSavedMessages]. Because saved messages span all
 * conversations, tapping a row opens that message's conversation (jumping to the message) — the
 * cross-chat equivalent of goto-message from search.
 */
class SavedMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_messages)
        applyWindowInsets()

        val savedMessages = findViewById<CometChatSavedMessages>(R.id.saved_messages)
        savedMessages.setOnBackClickListener { finish() }
        savedMessages.setOnMessageClickListener { message -> openConversationAtMessage(message) }
    }

    /** Pads the parent layout for the system bars so the toolbar clears the status bar. */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /** Opens the conversation the saved message belongs to, scrolled to that message. */
    private fun openConversationAtMessage(message: BaseMessage) {
        val intent = Intent(this, MessagesActivity::class.java)
        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            val group = message.receiver as? Group ?: return
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
        } else {
            // 1-1: the peer is the other party — the receiver if I sent it, else the sender.
            val myUid = CometChat.getLoggedInUser()?.uid
            val peer: User? = if (message.sender?.uid == myUid) message.receiver as? User else message.sender
            peer ?: return
            intent.putExtra(getString(R.string.app_user), peer.toJson().toString())
        }
        intent.putExtra("goToMessageId", message.id)
        startActivity(intent)
        finish()
    }
}
