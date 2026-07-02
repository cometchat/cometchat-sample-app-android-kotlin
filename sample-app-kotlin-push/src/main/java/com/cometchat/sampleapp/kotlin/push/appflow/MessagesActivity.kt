package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.uikit.core.formatter.RichTextConfiguration
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.MessagesViewModel
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityAppFlowMessagesBinding
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.log
import kotlin.math.max

/**
 * Activity for displaying chat messages with a user or group.
 * Contains MessageHeader, MessageList, and MessageComposer components.
 *
 * **Requirements:**
 * - 7.1: Layout with MessageHeader, MessageList, MessageComposer
 * - 7.2: Extract User from intent extras
 * - 7.3: Extract Group from intent extras
 * - 7.5: Configure MessageHeader with back button
 * - 7.6: MessageHeader overflow menu with Search, Conversation Summary, Details
 * - 7.10: Show unblock layout when user is blocked
 * - 7.11: Show info layout when not a group member
 * - 7.12: MessageList with isStartFromUnreadMessages and markAsUnreadOptionVisibility
 * - 7.13: Handle thread replies click
 * - 7.14: Handle mention clicks
 */
class MessagesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MessagesActivity"
    }

    private lateinit var binding: ActivityAppFlowMessagesBinding
    private lateinit var viewModel: MessagesViewModel
    
    private var user: User? = null
    private var group: Group? = null
    private var goToMessage: BaseMessage? = null
    private var goToMessageIdFromSearch: Long = 0
    private var baseMessage: BaseMessage? = null
    private var parentMessageId: Long = -1

    private val searchActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val shouldNavigateToDifferentChat = data.getBooleanExtra("navigateToDifferentChat", false)

            if (shouldNavigateToDifferentChat) {
                handleDifferentChatNavigation(data)
            } else {
                handleSameChatNavigation(data)
            }
        }
    }

    private fun handleDifferentChatNavigation(data: Intent) {
        val selectedUserJson = data.getStringExtra(getString(R.string.app_user))
        val selectedGroupJson = data.getStringExtra(getString(R.string.app_group))
        val goToMessageId = data.getLongExtra("goToMessageId", 0)
        val parentMessageJson = data.getStringExtra(getString(R.string.app_base_message))

        if (parentMessageJson != null) {
            val intent = Intent(this, ThreadMessageActivity::class.java).apply {
                putExtra("rawJson", parentMessageJson)
                selectedUserJson?.let { putExtra(getString(R.string.app_user), it) }
                selectedGroupJson?.let { putExtra(getString(R.string.app_group), it) }
            }
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, MessagesActivity::class.java).apply {
                selectedUserJson?.let { putExtra(getString(R.string.app_user), it) }
                selectedGroupJson?.let { putExtra(getString(R.string.app_group), it) }
                if (goToMessageId > 0) {
                    putExtra("goToMessageId", goToMessageId)
                }
            }
            startActivity(intent)
            finish()
        }
    }

    private fun handleSameChatNavigation(data: Intent) {
        val goToMessageId = data.getLongExtra("goToMessageId", 0)
        if (goToMessageId > 0) {
            binding.messageList.gotoMessage(goToMessageId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppFlowMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTheme()
        adjustWindowSettings()
        applyWindowInsets()
        extractIntentData(intent)
        initViewModel()
        addViews()
        initClickListeners()
        setUpMessageHeaderMenu()
    }

    override fun onResume() {
        super.onResume()
        // Suppress push notifications for the conversation currently on screen.
        (user?.uid ?: group?.guid)?.let { CometChatPushNotifications.setCurrentOpenChatId(it) }
    }

    override fun onPause() {
        super.onPause()
        CometChatPushNotifications.setCurrentOpenChatId(null)
    }

    /**
     * Extracts user, group, and goToMessage from intent extras.
     */
    private fun extractIntentData(intent: Intent) {
        // Extract group
        val groupJson = intent.getStringExtra(getString(R.string.app_group))
        if (groupJson != null) {
            group = Gson().fromJson(groupJson, Group::class.java)
        }

        // Extract parent message ID (from chat history navigation)
        parentMessageId = intent.getLongExtra("parent_message_id", -1)
        try {
            // Extract goToMessage — support both Long ID extra and legacy JSON extra
            val goToMessageId = intent.getLongExtra("goToMessageId", 0)
            val rawGoToMessage = intent.getStringExtra(getString(R.string.app_go_to_message))
            if (goToMessageId > 0) {
                goToMessage = null
                this.goToMessageIdFromSearch = goToMessageId
            } else if (rawGoToMessage != null) {
                goToMessage = BaseMessage.processMessage(JSONObject(rawGoToMessage))
            }
            
            // Extract user
            val userJson = intent.getStringExtra(getString(R.string.app_user))
            if (userJson != null) {
                user = User.fromJson(JSONObject(userJson).toString())
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing intent data: ${e.message}")
        }
    }

    /**
     * Initializes the ViewModel and sets up observers.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(MessagesViewModel::class.java)
        viewModel.setUser(user)
        viewModel.setGroup(group)
        viewModel.addListener()

        viewModel.updatedGroup.observe(this) { group ->
            updateGroupJoinedStatus(group)
        }

        viewModel.updateUser.observe(this) { user ->
            this.user = user
            updateUserBlockStatus(user)
        }

        viewModel.isExitActivity.observe(this) { exit ->
            if (exit) finish()
        }

        viewModel.unblockButtonState.observe(this) { state ->
            setUnblockButtonState(state)
        }

        viewModel.baseMessage.observe(this) { message ->
            if (message != null) {
                this.baseMessage = message
            }
        }

        viewModel.openUserChat.observe(this) { user ->
            openUserChat(user)
        }
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun initClickListeners() {
        binding.unblockBtn.setOnClickListener {
            viewModel.unblockUser()
        }

        // Note: setOnThreadRepliesClick signature in chatuikit-kotlin is (BaseMessage) -> Unit
        binding.messageList.setOnThreadRepliesClick { parentMessage ->
            val intent = Intent(this, ThreadMessageActivity::class.java)
            if (user != null) {
                intent.putExtra(getString(R.string.app_user), user?.toJson().toString())
            } else if (group != null) {
                intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            }
            intent.putExtra("isBlockedByMe", user?.isBlockedByMe)
            intent.putExtra("replyCount", parentMessage.replyCount)
            intent.putExtra("rawJson", parentMessage.rawMessage.toString())
            startActivity(intent)
        }
    }

    /**
     * Sets up the MessageHeader overflow menu with Search and Details options.
     */
    private fun setUpMessageHeaderMenu() {
        // setOptions IS available in chatuikit-kotlin CometChatMessageHeader
        val options = getHeaderMenuOptions()
        binding.messageHeader.setOptions(options)
    }

    /**
     * Gets the header menu options.
     */
    private fun getHeaderMenuOptions(): List<CometChatPopupMenu.MenuItem> {
        val options = mutableListOf<CometChatPopupMenu.MenuItem>()
        
        // Search option
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.SEARCH,
                getString(com.cometchat.uikit.kotlin.R.string.cometchat_menu_search),
                AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_search),
                null
            ) { navigateToSearchActivity() }
        )
        
        // Details option
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.DETAILS,
                getString(com.cometchat.uikit.kotlin.R.string.cometchat_details),
                AppCompatResources.getDrawable(this, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_info),
                null
            ) { openDetailScreen() }
        )

        return options
    }

    /**
     * Navigates to SearchActivity.
     */
    private fun navigateToSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        if (user != null) {
            intent.putExtra(getString(R.string.app_user), user?.toJson().toString())
        } else {
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
        }
        searchActivityLauncher.launch(intent)
    }

    /**
     * Opens the detail screen for the selected user or group.
     */
    private fun openDetailScreen() {
        val intent: Intent
        if (user != null) {
            intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            intent.putExtra(getString(R.string.app_base_message), Gson().toJson(baseMessage))
        } else {
            intent = Intent(this, GroupDetailsActivity::class.java)
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            intent.putExtra(getString(R.string.app_base_message), Gson().toJson(baseMessage))
        }
        startActivity(intent)
    }

    /**
     * Opens the chat interface for the specified user.
     */
    private fun openUserChat(user: User?) {
        if (user != null) {
            val intent = Intent(this, MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            startActivity(intent)
        }
    }

    /**
     * Applies theme colors to UI elements.
     */
    private fun setUpTheme() {
        binding.parentView.setBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        binding.infoLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        binding.separator.setBackgroundColor(CometChatTheme.getStrokeColorLight(this))
        binding.infoText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockTitle.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this))
        binding.unblockBtn.strokeColor = CometChatTheme.getStrokeColorDark(this)
        binding.progress.indeterminateTintList = ColorStateList.valueOf(
            CometChatTheme.getIconTintSecondary(this)
        )
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
     * Applies window insets for proper padding.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentView) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomInset = max(ime.bottom.toDouble(), nav.bottom.toDouble()).toInt()

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }
    }

    /**
     * Updates the unblock button state.
     */
    private fun setUnblockButtonState(dialogState: UIKitConstants.DialogState) {
        when (dialogState) {
            UIKitConstants.DialogState.INITIATED -> {
                binding.unblockText.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            }
            UIKitConstants.DialogState.SUCCESS, UIKitConstants.DialogState.FAILURE -> {
                binding.unblockText.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
            }
        }
    }

    /**
     * Updates UI based on group joined status.
     */
    private fun updateGroupJoinedStatus(group: Group) {
        if (!group.isJoined) {
            binding.unblockBtn.visibility = View.GONE
            binding.messageComposer.visibility = View.GONE
            binding.infoLayout.visibility = View.VISIBLE
        } else {
            binding.unblockBtn.visibility = View.GONE
            binding.messageComposer.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.GONE
        }
    }

    /**
     * Updates UI based on user block status.
     */
    private fun updateUserBlockStatus(user: User) {
        val isBlocked = user.isBlockedByMe || user.isHasBlockedMe
        val callButtonVisibility = if (isBlocked) View.GONE else View.VISIBLE
        binding.messageHeader.setVideoCallButtonVisibility(callButtonVisibility)
        binding.messageHeader.setVoiceCallButtonVisibility(callButtonVisibility)
        if (user.isBlockedByMe) {
            binding.messageComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.messageComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }

    /**
     * Initializes UI components with user/group data.
     */
    private fun addViews() {
        // Show back button
        binding.messageHeader.setBackButtonVisibility(View.VISIBLE)
        
        // Show call buttons

        binding.messageHeader.setVideoCallButtonVisibility(View.VISIBLE)
        binding.messageHeader.setVoiceCallButtonVisibility(View.VISIBLE)
        // Show or hide call buttons based on block status
        val callButtonVisibility = if (user?.isBlockedByMe == true || user?.isHasBlockedMe == true) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.messageHeader.setVideoCallButtonVisibility(callButtonVisibility)
        binding.messageHeader.setVoiceCallButtonVisibility(callButtonVisibility)

        // Note: Use setOnBackPress (setOnBackButtonPressed is not available in chatuikit-kotlin)
        binding.messageHeader.setOnBackPress {
            // Hide keyboard using InputMethodManager directly
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
            finish()
        }

        // Configure message list to start from unread messages BEFORE setting user/group
        binding.messageList.isStartFromUnreadMessages = true
        binding.messageList.setMarkAsUnreadOptionVisibility(View.VISIBLE)

        // Set parent message ID for threaded conversations (from chat history)
        if (parentMessageId > 0) {
            Log.d(TAG, "addViews: setting parentMessageId=$parentMessageId on messageList and messageComposer")
            binding.messageList.setParentMessageId(parentMessageId)
            binding.messageComposer.setParentMessageId(parentMessageId)
        }

        // Handle goToMessage navigation
        if (goToMessage != null) {
            binding.messageList.gotoMessage(goToMessage!!.id)
        } else if (goToMessageIdFromSearch > 0) {
            binding.messageList.gotoMessage(goToMessageIdFromSearch)
            goToMessageIdFromSearch = 0
        }

        binding.messageComposer.setEnableRichTextFormatting(true)
        binding.messageComposer.setShowFormattingToolbar(true)
        binding.messageComposer.setRichTextToolbarVisibility(View.VISIBLE)
        // Note: Use setUser/setGroup methods (properties are not directly accessible in chatuikit-kotlin)
        if (user != null) {
            binding.messageHeader.setUser(user!!)
            Log.e(TAG, "User : ${user}")
            binding.messageList.setUser(user!!)
            binding.messageComposer.setUser(user!!)
            updateUserBlockStatus(user!!)
        } else if (group != null) {
            binding.messageHeader.setGroup(group!!)
            binding.messageList.setGroup(group!!)
            binding.messageComposer.setGroup(group!!)
            binding.messageComposer.setLayoutMode(ComposerLayoutMode.SINGLE_LINE)
            updateGroupJoinedStatus(group!!)
        }
        
        // Wire up chat history button
        binding.messageHeader.setOnChatHistoryClick {
            user?.let { u ->
                val intent = Intent(this, ChatHistoryActivity::class.java)
                intent.putExtra("extra_user", u)
                startActivity(intent)
            }
        }

        // Wire up new chat button — start a fresh AI conversation
        binding.messageHeader.setOnNewChatClick {
            user?.let { u ->
                val intent = Intent(this, MessagesActivity::class.java)
                intent.putExtra(getString(R.string.app_user), u.toJson().toString())
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }

        // Enable rich text toolbar only for non-agent chats
        // (Agent chats hide the toolbar automatically in setUser, but this call would override it)
        if (user == null || !com.cometchat.uikit.core.utils.AgentChatDetector.isAgentChat(user!!)) {
//            binding.messageComposer.setRichTextToolbarVisibility(View.VISIBLE)
        }
        
        // Show all attachment options (only effective for non-agent chats)
        binding.messageComposer.setPollOptionVisibility(View.VISIBLE)
        binding.messageComposer.setCollaborativeDocumentOptionVisibility(View.VISIBLE)
        binding.messageComposer.setCollaborativeWhiteboardOptionVisibility(View.VISIBLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }
}
