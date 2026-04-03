package com.cometchat.sampleapp.kotlin.ui.users

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityUserDetailsBinding
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Activity for displaying user details.
 *
 * This activity displays detailed information about a user:
 * - Custom toolbar with back icon and title
 * - User avatar, name, and online/offline status
 * - Voice and Video call cards
 * - Block/unblock user functionality
 * - Delete chat option
 *
 * ## Features:
 * - Display user profile information
 * - Show online/offline presence status with last seen
 * - Voice and Video call cards (matching master-app-kotlin2)
 * - Block/unblock user with CometChatConfirmDialog
 * - Delete chat option
 * - Real-time user status updates
 * - Info banner for blocked user warning
 *
 * ## Usage:
 * ```kotlin
 * UserDetailsActivity.start(context, user)
 * UserDetailsActivity.start(context, user, lastMessage)
 * ```
 *
 * Matches master-app-kotlin2 UI parity.
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.6, 10.7, 10.8
 */
class UserDetailsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_HAS_LAST_MESSAGE = "extra_has_last_message"
        private const val USER_LISTENER_ID = "UserDetailsActivity_UserListener"

        /**
         * Starts UserDetailsActivity with a User object.
         *
         * @param context The context to start the activity from
         * @param user The user to display details for
         * @param lastMessage Optional last message (determines delete chat visibility)
         */
        fun start(context: Context, user: User, lastMessage: BaseMessage? = null) {
            val intent = Intent(context, UserDetailsActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, user.uid)
                putExtra(EXTRA_HAS_LAST_MESSAGE, lastMessage != null)
            }
            context.startActivity(intent)
        }

        /**
         * Starts UserDetailsActivity with a user ID.
         *
         * @param context The context to start the activity from
         * @param userId The UID of the user to display
         * @param hasLastMessage Whether there's a last message (determines delete chat visibility)
         */
        fun startWithUserId(context: Context, userId: String, hasLastMessage: Boolean = false) {
            val intent = Intent(context, UserDetailsActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_HAS_LAST_MESSAGE, hasLastMessage)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityUserDetailsBinding
    private var user: User? = null
    private var isBlocked: Boolean = false
    private var hasLastMessage: Boolean = false
    private var confirmDialog: CometChatConfirmDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hasLastMessage = intent.getBooleanExtra(EXTRA_HAS_LAST_MESSAGE, false)

        applyWindowInsets()
        applyThemeColors()
        setupToolbar()
        setupClickListeners()
        loadUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeUserListener()
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
     * Applies theme colors to UI elements (matching master-app-kotlin2).
     */
    private fun applyThemeColors() {
        binding.tvVideoCall.compoundDrawableTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this))
        binding.tvVoiceCall.compoundDrawableTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this))
        binding.toolbarTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.userName.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.userStatus.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.tvVoiceCall.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.tvVideoCall.setTextColor(CometChatTheme.getTextColorSecondary(this))
    }

    /**
     * Sets up the custom toolbar with back navigation.
     */
    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Sets up click listeners for action buttons.
     */
    private fun setupClickListeners() {
        // Voice Call card
        binding.cardVoiceCall.setOnClickListener {
            startCall(CometChatConstants.CALL_TYPE_AUDIO)
        }

        // Video Call card
        binding.cardVideoCall.setOnClickListener {
            startCall(CometChatConstants.CALL_TYPE_VIDEO)
        }

        // Block/Unblock button
        binding.tvBlock.setOnClickListener {
            if (isBlocked) {
                showUnblockConfirmation()
            } else {
                showBlockConfirmation()
            }
        }

        // Delete Chat button
        binding.tvDeleteChat.setOnClickListener {
            showDeleteChatConfirmation()
        }
    }

    /**
     * Loads the user data from CometChat.
     */
    private fun loadUserData() {
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        if (userId == null) {
            finish()
            return
        }

        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                user = fetchedUser
                isBlocked = fetchedUser.isBlockedByMe
                setupUserDetails()
                addUserListener()
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }

    /**
     * Sets up the user details UI (matching master-app-kotlin2).
     */
    private fun setupUserDetails() {
        user?.let { u ->
            // User Avatar
            binding.userAvatar.setAvatar(u.name, u.avatar)

            // User Name
            binding.userName.text = u.name

            // Update status and blocked state
            updateUserStatus(u)
            updateBlockedState()
            
            // Delete chat visibility
            binding.tvDeleteChat.visibility = if (hasLastMessage) View.VISIBLE else View.GONE
        }
    }

    /**
     * Updates the user status display (matching master-app-kotlin2 logic).
     *
     * @param user The user to display status for
     */
    private fun updateUserStatus(user: User) {
        if (!Utils.isBlocked(user)) {
            binding.userStatus.visibility = View.VISIBLE
            
            if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                binding.userStatus.text = getString(com.cometchat.uikit.kotlin.R.string.cometchat_online)
            } else {
                if (user.lastActiveAt == 0L) {
                    binding.userStatus.text = getString(com.cometchat.uikit.kotlin.R.string.cometchat_offline)
                } else {
                    val lastSeen = Utils.getLastSeenTime(this, user.lastActiveAt)
                    binding.userStatus.text = lastSeen
                    binding.userStatus.isSelected = true
                }
            }
        } else {
            binding.userStatus.visibility = View.GONE
        }
    }

    /**
     * Updates the UI based on blocked state (matching master-app-kotlin2).
     */
    private fun updateBlockedState() {
        user?.let { u ->
            if (Utils.isBlocked(u)) {
                // Show info message banner
                binding.infoMessage.visibility = View.VISIBLE
                
                if (u.isBlockedByMe) {
                    binding.tvInfoMessage.text = String.format(
                        "%s %s",
                        getString(R.string.you_have_blocked_this_user),
                        u.name
                    )
                    binding.tvBlock.text = getString(R.string.unblock_user)
                } else {
                    binding.tvInfoMessage.text = String.format(
                        "%s %s",
                        u.name,
                        getString(R.string.has_blocked_you)
                    )
                }
                
                // Hide call cards when blocked
                binding.cardVoiceCall.visibility = View.GONE
                binding.cardVideoCall.visibility = View.GONE
                binding.userStatus.visibility = View.GONE
            } else {
                // Hide info message banner
                binding.infoMessage.visibility = View.GONE
                // Show call cards
                binding.cardVoiceCall.visibility = View.VISIBLE
                binding.cardVideoCall.visibility = View.VISIBLE
                // Update block button text
                binding.tvBlock.text = getString(R.string.block_user)
            }
        }
    }

    /**
     * Starts a call with the user.
     *
     * @param callType The type of call (audio or video)
     */
    private fun startCall(callType: String) {
        user?.let { u ->
            val call = Call(u.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
            CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(initiatedCall: Call) {
                    // Launch outgoing call screen
                    com.cometchat.uikit.kotlin.calls.CometChatCallActivity.launchOutgoingCallScreen(
                        this@UserDetailsActivity,
                        initiatedCall,
                        null
                    )
                }

                override fun onError(e: CometChatException) {
                    // Handle error
                }
            })
        }
    }

    /**
     * Shows a confirmation dialog before blocking the user.
     */
    private fun showBlockConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_block, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setTitleText(getString(R.string.block_user_title))
            setSubtitleText(getString(R.string.block_user_message))
            setPositiveButtonText(getString(R.string.block))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                blockUser()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Shows a confirmation dialog before unblocking the user.
     */
    private fun showUnblockConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_block, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setTitleText(getString(R.string.unblock_user_title))
            setSubtitleText(getString(R.string.unblock_user_message))
            setPositiveButtonText(getString(R.string.unblock))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                unblockUser()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Shows a confirmation dialog before deleting the chat.
     */
    private fun showDeleteChatConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setTitleText(getString(R.string.delete_chat_title))
            setSubtitleText(getString(R.string.delete_chat_message))
            setPositiveButtonText(getString(R.string.delete))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@UserDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                deleteChat()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Deletes the chat with the current user.
     */
    private fun deleteChat() {
        user?.let { u ->
            CometChat.deleteConversation(
                u.uid,
                CometChatConstants.CONVERSATION_TYPE_USER,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(result: String) {
                        confirmDialog?.dismiss()
                        finish()
                    }

                    override fun onError(e: CometChatException) {
                        confirmDialog?.dismiss()
                    }
                }
            )
        }
    }

    /**
     * Blocks the current user.
     */
    private fun blockUser() {
        user?.let { u ->
            CometChat.blockUsers(
                listOf(u.uid),
                object : CometChat.CallbackListener<HashMap<String, String>>() {
                    override fun onSuccess(result: HashMap<String, String>) {
                        confirmDialog?.dismiss()
                        isBlocked = true
                        u.isBlockedByMe = true
                        updateBlockedState()
                        updateUserStatus(u)
                    }

                    override fun onError(e: CometChatException) {
                        confirmDialog?.dismiss()
                    }
                }
            )
        }
    }

    /**
     * Unblocks the current user.
     */
    private fun unblockUser() {
        user?.let { u ->
            CometChat.unblockUsers(
                listOf(u.uid),
                object : CometChat.CallbackListener<HashMap<String, String>>() {
                    override fun onSuccess(result: HashMap<String, String>) {
                        confirmDialog?.dismiss()
                        isBlocked = false
                        u.isBlockedByMe = false
                        updateBlockedState()
                        updateUserStatus(u)
                    }

                    override fun onError(e: CometChatException) {
                        confirmDialog?.dismiss()
                    }
                }
            )
        }
    }

    /**
     * Adds a user listener for real-time status updates.
     */
    private fun addUserListener() {
        CometChat.addUserListener(USER_LISTENER_ID, object : CometChat.UserListener() {
            override fun onUserOnline(onlineUser: User) {
                if (onlineUser.uid == user?.uid) {
                    user = onlineUser
                    runOnUiThread {
                        updateUserStatus(onlineUser)
                    }
                }
            }

            override fun onUserOffline(offlineUser: User) {
                if (offlineUser.uid == user?.uid) {
                    user = offlineUser
                    runOnUiThread {
                        updateUserStatus(offlineUser)
                    }
                }
            }
        })
    }

    /**
     * Removes the user listener.
     */
    private fun removeUserListener() {
        CometChat.removeUserListener(USER_LISTENER_ID)
    }
}
