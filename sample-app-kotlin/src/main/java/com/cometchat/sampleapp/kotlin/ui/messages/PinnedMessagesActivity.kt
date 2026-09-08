package com.cometchat.sampleapp.kotlin.ui.messages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.ActivityPinnedMessagesBinding

/**
 * Full-screen list of a conversation's pinned messages, opened from the message-header
 * overflow menu and from the user/group details screens.
 * Hosts [com.cometchat.uikit.kotlin.presentation.pinnedmessages.CometChatPinnedMessages],
 * configured for the current user or group.
 *
 * ## Usage:
 * ```kotlin
 * // Fire-and-forget (details screens)
 * PinnedMessagesActivity.start(context, user)
 *
 * // For a result (MessagesActivity) — a tapped row returns the message id so the
 * // conversation can jump to it in place
 * launcher.launch(PinnedMessagesActivity.newIntent(context, group))
 * ```
 */
class PinnedMessagesActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_GROUP_ID = "extra_group_id"

        /** Result extra: id of the tapped pinned message, for in-place navigation. */
        const val EXTRA_GO_TO_MESSAGE_ID = "extra_go_to_message_id"

        /**
         * Creates an intent for a one-on-one conversation's pinned messages.
         */
        fun newIntent(context: Context, user: User): Intent =
            Intent(context, PinnedMessagesActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, user.uid)
            }

        /**
         * Creates an intent for a group conversation's pinned messages.
         */
        fun newIntent(context: Context, group: Group): Intent =
            Intent(context, PinnedMessagesActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, group.guid)
            }

        /**
         * Starts PinnedMessagesActivity for a one-on-one conversation.
         */
        fun start(context: Context, user: User) {
            context.startActivity(newIntent(context, user))
        }

        /**
         * Starts PinnedMessagesActivity for a group conversation.
         */
        fun start(context: Context, group: Group) {
            context.startActivity(newIntent(context, group))
        }
    }

    private lateinit var binding: ActivityPinnedMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPinnedMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        binding.pinnedMessages.setOnBackClickListener { finish() }
        // Tapping a row jumps to that message in the conversation — same as goto-message
        // navigation from search. The message id is handed back to the caller as a result.
        binding.pinnedMessages.setOnMessageClickListener { message ->
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_GO_TO_MESSAGE_ID, message.id.toLong())
            )
            finish()
        }

        loadConversationData()
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
     * Loads the user or group data based on intent extras and configures the pinned list.
     */
    private fun loadConversationData() {
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        val groupId = intent.getStringExtra(EXTRA_GROUP_ID)

        when {
            userId != null -> loadUser(userId)
            groupId != null -> loadGroup(groupId)
            else -> finish()
        }
    }

    /**
     * Loads user data from CometChat.
     */
    private fun loadUser(userId: String) {
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                binding.pinnedMessages.setUser(fetchedUser)
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }

    /**
     * Loads group data from CometChat.
     */
    private fun loadGroup(groupId: String) {
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                binding.pinnedMessages.setGroup(fetchedGroup)
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }
}
