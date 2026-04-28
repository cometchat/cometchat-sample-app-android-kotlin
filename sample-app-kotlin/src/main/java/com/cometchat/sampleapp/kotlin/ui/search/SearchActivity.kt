package com.cometchat.sampleapp.kotlin.ui.search

import android.util.Log
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.ActivitySearchBinding
import com.cometchat.sampleapp.kotlin.ui.messages.MessagesActivity
import com.cometchat.sampleapp.kotlin.ui.messages.ThreadMessagesActivity
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle

/**
 * Activity for global and contextual search across conversations and messages.
 *
 * This activity integrates the CometChatSearch component from chatuikit-kotlin
 * to provide search functionality across conversations and messages.
 *
 * ## Features:
 * - Global search across all conversations and messages
 * - Contextual search within a specific user or group conversation
 * - Filter chips for Photos, Videos, Documents, Links, Audio, Groups, Unread
 * - Debounced search with 450ms delay
 * - Pagination support for large result sets
 * - Custom styling support
 *
 * ## Usage:
 * ```kotlin
 * // Global search
 * SearchActivity.start(context)
 *
 * // Contextual search for a user
 * SearchActivity.startWithUserId(context, userId = "user123")
 *
 * // Contextual search for a group
 * SearchActivity.startWithGroupId(context, groupId = "group123")
 * ```
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_GROUP_ID = "group_id"

        /**
         * Starts SearchActivity for global search.
         *
         * @param context The context to start the activity from
         */
        fun start(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * Starts SearchActivity with a user ID for contextual search.
         *
         * @param context The context to start the activity from
         * @param userId The UID of the user to search within
         */
        fun startWithUserId(context: Context, userId: String) {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }

        /**
         * Starts SearchActivity with a group ID for contextual search.
         *
         * @param context The context to start the activity from
         * @param groupId The GUID of the group to search within
         */
        fun startWithGroupId(context: Context, groupId: String) {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchBinding

    // Contextual search parameters
    private var userId: String? = null
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract intent extras
        userId = intent.getStringExtra(EXTRA_USER_ID)
        groupId = intent.getStringExtra(EXTRA_GROUP_ID)

        applyWindowInsets()
        setupSearchComponent()
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
     * Sets up the CometChatSearch component with configuration and callbacks.
     */
    private fun setupSearchComponent() {
        binding.searchComponent.apply {
            // Configure contextual search if UID or GUID is provided
            userId?.let { setUid(it) }
            groupId?.let { setGuid(it) }

            // Set up back button callback
            setOnBackPress {
                finish()
            }

            // Set up conversation click callback
            setOnConversationClick { conversation ->
                navigateToMessages(conversation)
            }

            // Set up message click callback
            setOnMessageClick { message ->
                navigateToMessageContext(message)
            }

            // Set up error callback
            setOnError { exception ->
                // Log error or show toast
                android.util.Log.e("SearchActivity", "Search error: ${exception.message}")
            }
        }
    }

    /**
     * Navigates to the Messages screen for the selected conversation.
     *
     * @param conversation The selected conversation
     */
    private fun navigateToMessages(conversation: Conversation) {
        when (conversation.conversationType) {
            CometChatConstants.CONVERSATION_TYPE_USER -> {
                val user = conversation.conversationWith as? User
                user?.let {
                    MessagesActivity.start(this, user = it)
                    finish()
                }
            }
            CometChatConstants.CONVERSATION_TYPE_GROUP -> {
                val group = conversation.conversationWith as? Group
                group?.let {
                    MessagesActivity.start(this, group = it)
                    finish()
                }
            }
        }
    }

    /**
     * Handles message click matching Java SearchActivity.handleMessageClick().
     * Fetches User/Group, checks for parent message (thread), then navigates.
     */
    private fun navigateToMessageContext(message: BaseMessage) {
        when (message.receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> {
                val uid = if (message.sender.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                    message.receiverUid
                } else {
                    message.sender.uid
                }
                CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        fetchParentAndNavigate(user, null, message)
                    }
                    override fun onError(e: CometChatException) {
                        Log.e("SearchActivity", "getUser error: ${e.message}")
                    }
                })
            }
            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                CometChat.getGroup(message.receiverUid, object : CometChat.CallbackListener<Group>() {
                    override fun onSuccess(group: Group) {
                        fetchParentAndNavigate(null, group, message)
                    }
                    override fun onError(e: CometChatException) {
                        Log.e("SearchActivity", "getGroup error: ${e.message}")
                    }
                })
            }
        }
    }

    /**
     * Fetches the parent message if the tapped message is a thread reply,
     * then navigates to the appropriate screen.
     */
    private fun fetchParentAndNavigate(user: User?, group: Group?, message: BaseMessage) {
        val parentMessageId = message.parentMessageId
        if (parentMessageId > 0) {
            CometChat.getMessageDetails(parentMessageId, object : CometChat.CallbackListener<BaseMessage>() {
                override fun onSuccess(parentMessage: BaseMessage) {
                    navigateToThread(parentMessage, message.id.toLong())
                }
                override fun onError(e: CometChatException) {
                    // Parent fetch failed — navigate to messages without thread
                    navigateToChat(user, group, message.id.toLong())
                }
            })
        } else {
            navigateToChat(user, group, message.id.toLong())
        }
    }

    /**
     * Navigates to ThreadMessagesActivity for thread replies.
     */
    private fun navigateToThread(parentMessage: BaseMessage, goToMessageId: Long = 0) {
        ThreadMessagesActivity.start(this, parentMessage, goToMessageId)
        finish()
    }

    /**
     * Navigates to MessagesActivity with goToMessageId to scroll to the tapped message.
     */
    private fun navigateToChat(user: User?, group: Group?, goToMessageId: Long) {
        when {
            user != null -> MessagesActivity.start(this, user = user, goToMessageId = goToMessageId)
            group != null -> MessagesActivity.start(this, group = group, goToMessageId = goToMessageId)
        }
        finish()
    }
}
