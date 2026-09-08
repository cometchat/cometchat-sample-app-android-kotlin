package com.cometchat.sampleapp.kotlin.push.appflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.presentation.pinnedmessages.CometChatPinnedMessages
import com.cometchat.sampleapp.kotlin.push.R
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Full-screen list of a conversation's pinned messages, opened from the message-header ⋮ menu.
 * Hosts [CometChatPinnedMessages], configured for the current user or group.
 */
class PinnedMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pinned_messages)
        applyWindowInsets()

        val pinnedMessages = findViewById<CometChatPinnedMessages>(R.id.pinned_messages)

        val userJson = intent.getStringExtra(getString(R.string.app_user))
        val groupJson = intent.getStringExtra(getString(R.string.app_group))
        when {
            userJson != null -> pinnedMessages.setUser(User.fromJson(JSONObject(userJson).toString()))
            groupJson != null -> pinnedMessages.setGroup(Gson().fromJson(groupJson, Group::class.java))
        }

        pinnedMessages.setOnBackClickListener { finish() }
        // Tapping a row jumps to that message in the conversation — same as goto-message from search.
        pinnedMessages.setOnMessageClickListener { message ->
            setResult(Activity.RESULT_OK, Intent().putExtra("goToMessageId", message.id))
            finish()
        }
    }

    /** Pads the parent layout for the system bars so the toolbar clears the status bar. */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
