package com.cometchat.sampleapp.kotlin.ui.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.ActivityThreadMessagesBinding

/**
 * Activity for displaying thread replies to a message.
 *
 * This activity displays thread replies using CometChat UI Kit components:
 * - CometChatThreadHeader: Shows the parent message
 * - CometChatMessageList: Displays thread replies
 * - CometChatMessageComposer: Allows composing thread replies
 *
 * ## Features:
 * - Display parent message context
 * - Real-time thread reply updates
 * - Support for all message types in replies
 *
 * ## Usage:
 * ```kotlin
 * ThreadMessagesActivity.start(context, parentMessage)
 * ```
 *
 * @see com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList
 * @see com.cometchat.uikit.kotlin.presentation.messagecomposer.ui.CometChatMessageComposer
 *
 * Validates: Requirements 6.9
 */
class ThreadMessagesActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PARENT_MESSAGE_ID = "extra_parent_message_id"
        private const val EXTRA_RECEIVER_ID = "extra_receiver_id"
        private const val EXTRA_RECEIVER_TYPE = "extra_receiver_type"

        /**
         * Starts ThreadMessagesActivity with a parent message.
         *
         * @param context The context to start the activity from
         * @param parentMessage The parent message to show thread for
         */
        fun start(context: Context, parentMessage: BaseMessage) {
            val intent = Intent(context, ThreadMessagesActivity::class.java).apply {
                putExtra(EXTRA_PARENT_MESSAGE_ID, parentMessage.id.toLong())
                putExtra(EXTRA_RECEIVER_ID, parentMessage.receiverUid)
                putExtra(EXTRA_RECEIVER_TYPE, parentMessage.receiverType)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityThreadMessagesBinding
    private var parentMessageId: Long = -1
    private var receiverId: String? = null
    private var receiverType: String? = null
    private var user: User? = null
    private var group: Group? = null
    private var parentMessage: BaseMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityThreadMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        extractIntentData()
        setupToolbar()
        loadData()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    /**
     * Extracts data from intent extras.
     */
    private fun extractIntentData() {
        parentMessageId = intent.getLongExtra(EXTRA_PARENT_MESSAGE_ID, -1)
        receiverId = intent.getStringExtra(EXTRA_RECEIVER_ID)
        receiverType = intent.getStringExtra(EXTRA_RECEIVER_TYPE)
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Loads the parent message and receiver data.
     */
    private fun loadData() {
        if (parentMessageId == -1L || receiverId == null || receiverType == null) {
            finish()
            return
        }

        // Load receiver (user or group)
        when (receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> loadUser(receiverId!!)
            CometChatConstants.RECEIVER_TYPE_GROUP -> loadGroup(receiverId!!)
            else -> finish()
        }
    }

    /**
     * Loads user data from CometChat.
     */
    private fun loadUser(userId: String) {
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                user = fetchedUser
                loadParentMessage()
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
                group = fetchedGroup
                loadParentMessage()
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }

    /**
     * Loads the parent message from CometChat.
     */
    private fun loadParentMessage() {
        CometChat.getMessageDetails(parentMessageId, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                parentMessage = message
                setupThreadUI()
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }

    /**
     * Sets up the thread UI components after data is loaded.
     */
    private fun setupThreadUI() {
        setupThreadHeader()
        setupMessageList()
        setupMessageComposer()
    }

    /**
     * Sets up the thread header component.
     */
    private fun setupThreadHeader() {
        binding.threadHeader.apply {
            parentMessage?.let { setParentMessage(it) }
        }
    }

    /**
     * Sets up the message list component for thread replies.
     */
    private fun setupMessageList() {
        binding.messageList.apply {
            // Set user or group
            user?.let { setUser(it) }
            group?.let { setGroup(it) }

            // Set parent message ID for thread context
            setParentMessageId(parentMessageId)

            // Enable real-time updates
            setScrollToBottomOnNewMessage(true)
        }
    }

    /**
     * Sets up the message composer component for thread replies.
     */
    private fun setupMessageComposer() {
        binding.messageComposer.apply {
            // Set user or group
            user?.let { setUser(it) }
            group?.let { setGroup(it) }

            // Set parent message ID for thread context
            setParentMessageId(parentMessageId)

            // Enable all attachment types
            setHideAttachmentButton(false)
            setHideVoiceRecordingButton(false)
        }
    }
}
