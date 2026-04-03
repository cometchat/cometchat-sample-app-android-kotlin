package com.cometchat.sampleapp.kotlin.ui.messages

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityMessagesBinding
import com.cometchat.sampleapp.kotlin.ui.groups.GroupDetailsActivity
import com.cometchat.sampleapp.kotlin.ui.users.UserDetailsActivity
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu

/**
 * Activity for displaying one-on-one and group messaging.
 *
 * This activity displays the complete chat interface using CometChat UI Kit components:
 * - CometChatMessageHeader: Shows user/group info, typing indicators, and navigation
 * - CometChatMessageList: Displays message history with real-time updates
 * - CometChatMessageComposer: Allows composing and sending messages
 *
 * ## Features:
 * - Real-time message sending and receiving
 * - Support for text, images, videos, audio, and files
 * - Message delivery and read receipts
 * - Message reactions
 * - Thread replies
 * - Typing indicators
 * - Blocked user handling with unblock option
 * - Group membership status handling
 *
 * ## Usage:
 * ```kotlin
 * // Start with a user
 * MessagesActivity.start(context, user = user)
 *
 * // Start with a group
 * MessagesActivity.start(context, group = group)
 *
 * // Start with user ID
 * MessagesActivity.startWithUserId(context, userId = "user123")
 *
 * // Start with group ID
 * MessagesActivity.startWithGroupId(context, groupId = "group123")
 * ```
 *
 * @see com.cometchat.uikit.kotlin.presentation.messageheader.ui.CometChatMessageHeader
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList
 * @see com.cometchat.uikit.kotlin.presentation.messagecomposer.ui.CometChatMessageComposer
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7.1, 7.2, 7.3, 2.27, 2.28
 */
class MessagesActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_GROUP_ID = "extra_group_id"

        /**
         * Starts MessagesActivity with a User object.
         *
         * @param context The context to start the activity from
         * @param user The user to chat with
         */
        fun start(context: Context, user: User) {
            val intent = Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, user.uid)
            }
            context.startActivity(intent)
        }

        /**
         * Starts MessagesActivity with a Group object.
         *
         * @param context The context to start the activity from
         * @param group The group to chat in
         */
        fun start(context: Context, group: Group) {
            val intent = Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, group.guid)
            }
            context.startActivity(intent)
        }

        /**
         * Starts MessagesActivity with a user ID.
         *
         * @param context The context to start the activity from
         * @param userId The UID of the user to chat with
         */
        fun startWithUserId(context: Context, userId: String) {
            val intent = Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }

        /**
         * Starts MessagesActivity with a group ID.
         *
         * @param context The context to start the activity from
         * @param groupId The GUID of the group to chat in
         */
        fun startWithGroupId(context: Context, groupId: String) {
            val intent = Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMessagesBinding
    private var user: User? = null
    private var group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adjustWindowSettings()
        applyWindowInsets()
        setupUnblockButton()
        loadConversationData()
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
     * Adjusts window settings for proper keyboard handling.
     */
    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    /**
     * Sets up the unblock button click listener.
     * Validates: Requirement 2.28
     */
    private fun setupUnblockButton() {
        binding.unblockBtn.setOnClickListener {
            user?.let { unblockUser(it) }
        }
    }

    /**
     * Unblocks the specified user.
     *
     * @param userToUnblock The user to unblock
     */
    private fun unblockUser(userToUnblock: User) {
        // Show progress
        binding.unblockText.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE

        CometChat.unblockUsers(
            listOf(userToUnblock.uid),
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(result: HashMap<String, String>) {
                    // Update user block status
                    userToUnblock.isBlockedByMe = false
                    user = userToUnblock
                    
                    // Hide progress and update UI
                    binding.unblockText.visibility = View.VISIBLE
                    binding.progress.visibility = View.GONE
                    
                    updateUserBlockStatus(userToUnblock)
                }

                override fun onError(e: CometChatException) {
                    // Hide progress on error
                    binding.unblockText.visibility = View.VISIBLE
                    binding.progress.visibility = View.GONE
                }
            }
        )
    }

    /**
     * Loads the user or group data based on intent extras.
     */
    private fun loadConversationData() {
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        val groupId = intent.getStringExtra(EXTRA_GROUP_ID)

        when {
            userId != null -> loadUser(userId)
            groupId != null -> loadGroup(groupId)
            else -> {
                // No user or group ID provided, finish activity
                finish()
            }
        }
    }

    /**
     * Loads user data from CometChat.
     *
     * @param userId The UID of the user to load
     */
    private fun loadUser(userId: String) {
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                user = fetchedUser
                setupMessagesUI()
                updateUserBlockStatus(fetchedUser)
            }

            override fun onError(e: CometChatException) {
                // Handle error - could show error message or finish
                finish()
            }
        })
    }

    /**
     * Loads group data from CometChat.
     *
     * @param groupId The GUID of the group to load
     */
    private fun loadGroup(groupId: String) {
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                group = fetchedGroup
                setupMessagesUI()
                updateGroupJoinedStatus(fetchedGroup)
            }

            override fun onError(e: CometChatException) {
                // Handle error - could show error message or finish
                finish()
            }
        })
    }

    /**
     * Updates the UI based on the user's block status.
     * Shows unblock layout if user is blocked, otherwise shows composer.
     *
     * @param user The user to check block status for
     *
     * Validates: Requirement 2.28
     */
    private fun updateUserBlockStatus(user: User) {
        if (user.isBlockedByMe) {
            binding.messageComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.GONE
        } else {
            binding.messageComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
            binding.infoLayout.visibility = View.GONE
        }
    }

    /**
     * Updates the UI based on the group's joined status.
     * Shows info layout if user is no longer a member, otherwise shows composer.
     *
     * @param group The group to check membership for
     *
     * Validates: Requirement 2.27
     */
    private fun updateGroupJoinedStatus(group: Group) {
        if (!group.isJoined) {
            binding.unblockLayout.visibility = View.GONE
            binding.messageComposer.visibility = View.GONE
            binding.infoLayout.visibility = View.VISIBLE
        } else {
            binding.unblockLayout.visibility = View.GONE
            binding.messageComposer.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.GONE
        }
    }

    /**
     * Sets up the messages UI components after user/group is loaded.
     *
     * Configures:
     * - Message header with back button and user/group info
     * - Message list with real-time updates
     * - Message composer with all attachment options
     *
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7.1, 7.2, 7.3
     */
    private fun setupMessagesUI() {
        setupMessageHeader()
        setupMessageList()
        setupMessageComposer()
    }

    /**
     * Sets up the message header component.
     *
     * Validates: Requirements 6.3, 7.3, 10.8
     */
    private fun setupMessageHeader() {
        binding.messageHeader.apply {
            // Set user or group
            user?.let { setUser(it) }
            group?.let { setGroup(it) }

            // Show back button for navigation
            setBackButtonVisibility(View.VISIBLE)
            setOnBackPress { finish() }

            // Hide call buttons - no VoIP in sample apps
            setVideoCallButtonVisibility(View.GONE)
            setVoiceCallButtonVisibility(View.GONE)
        }
        
        // Set up header menu with Details option
        setUpMessageHeaderMenu()
    }
    
    /**
     * Sets up the message header overflow menu with Details option.
     * Validates: Requirements 7.3, 10.8
     */
    private fun setUpMessageHeaderMenu() {
        val options = getHeaderMenuOptions()
        binding.messageHeader.setOptions(options)
    }
    
    /**
     * Gets the header menu options.
     * @return List of menu items for the header popup menu
     */
    private fun getHeaderMenuOptions(): List<CometChatPopupMenu.MenuItem> {
        val options = mutableListOf<CometChatPopupMenu.MenuItem>()
        
        // Add Details option for navigating to user/group details
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.DETAILS,
                getString(com.cometchat.uikit.kotlin.R.string.cometchat_details),
                AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_info),
                null
            ) {
                openDetailScreen()
            }
        )
        
        return options
    }
    
    /**
     * Opens the detail screen for the selected user or group.
     * Validates: Requirements 7.3, 10.8
     */
    private fun openDetailScreen() {
        user?.let { navigateToUserDetails(it) }
        group?.let { navigateToGroupDetails(it) }
    }

    /**
     * Sets up the message list component.
     *
     * Validates: Requirements 6.1, 6.4, 6.5, 6.7, 6.8, 6.9, 7.1, 7.2
     */
    private fun setupMessageList() {
        binding.messageList.apply {
            // Set user or group
            user?.let { setUser(it) }
            group?.let { setGroup(it) }

            // Enable real-time updates (Requirement 6.4, 6.5)
            setScrollToBottomOnNewMessage(true)

            // Enable swipe to reply
            setSwipeToReplyEnabled(true)

            // Show receipts for delivery/read status (Requirement 6.7)
            setHideReceipts(false)

            // Handle thread click for navigation
            setOnThreadRepliesClick { message ->
                navigateToThread(message)
            }
        }
    }

    /**
     * Sets up the message composer component.
     *
     * Validates: Requirements 6.2, 6.6
     */
    private fun setupMessageComposer() {
        binding.messageComposer.apply {
            // Set user or group
            user?.let { setUser(it) }
            group?.let { setGroup(it) }

            // Enable all attachment types (Requirement 6.6)
            setHideAttachmentButton(false)
            setHideVoiceRecordingButton(false)
        }
    }

    /**
     * Navigates to the thread messages screen.
     *
     * @param parentMessage The parent message to show thread for
     *
     * Validates: Requirements 6.9
     */
    private fun navigateToThread(parentMessage: BaseMessage) {
        ThreadMessagesActivity.start(this, parentMessage)
    }

    /**
     * Navigates to the group details screen.
     *
     * @param group The group to show details for
     *
     * Validates: Requirements 7.3
     */
    private fun navigateToGroupDetails(group: Group) {
        GroupDetailsActivity.start(this, group)
    }

    /**
     * Navigates to the user details screen.
     *
     * @param user The user to show details for
     *
     * Validates: Requirements 10.8
     */
    private fun navigateToUserDetails(user: User) {
        UserDetailsActivity.start(this, user)
    }
}
