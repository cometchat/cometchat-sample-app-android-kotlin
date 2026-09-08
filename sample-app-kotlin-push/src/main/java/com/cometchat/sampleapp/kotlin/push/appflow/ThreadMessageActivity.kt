package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatThreadEvent
import com.cometchat.uikit.core.utils.CometChatThreadSubscription
import android.widget.Toast
import com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityThreadMessageBinding
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.ThreadMessageViewModel
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Activity for displaying threaded message replies.
 * Shows the parent message in a ThreadHeader and replies in a MessageList.
 */
class ThreadMessageActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "ThreadMessageActivity"
        const val EXTRA_RAW_JSON = "rawJson"
        const val EXTRA_REPLY_COUNT = "replyCount"

        /**
         * The parent as a [BaseMessage] Parcelable — the preferred way to hand it over. The SDK's
         * pin/save and thread-subscription state lives on the parsed object, not in `rawMessage`
         * (that JSON is the server payload as fetched and is never rewritten by realtime updates),
         * so re-parsing [EXTRA_RAW_JSON] dropped a save/pin made moments earlier.
         * Passing the live object mirrors the React kit, which hands the thread the message
         * object itself. [EXTRA_RAW_JSON] remains as a fallback for callers that only hold JSON.
         */
        const val EXTRA_PARENT_MESSAGE = "parentMessage"
    }
    
    private lateinit var binding: ActivityThreadMessageBinding
    private lateinit var viewModel: ThreadMessageViewModel
    
    private var user: User? = null
    private var group: Group? = null
    private var goToMessage: BaseMessage? = null
    private var goToMessageId: Long = 0
    private var isBlockedByMe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[ThreadMessageViewModel::class.java]
        
        setUpTheme()
        adjustWindowSettings()
        windowInsetsListener()
        
        parseIntentData()
        setupObservers()
        setupUI()
    }
    
    private fun parseIntentData() {
        val goToMessageJson = intent.getStringExtra(getString(R.string.app_go_to_message))
        val rawMessage = intent.getStringExtra(EXTRA_RAW_JSON)
        val replyCount = intent.getIntExtra(EXTRA_REPLY_COUNT, 0)
        val userJson = intent.getStringExtra(getString(R.string.app_user))
        val groupJson = intent.getStringExtra(getString(R.string.app_group))
        
        try {
            isBlockedByMe = intent.getBooleanExtra("isBlockedByMe", false)
            
            goToMessageId = intent.getLongExtra("goToMessageId", 0)
            if (goToMessageJson != null) {
                goToMessage = BaseMessage.processMessage(JSONObject(goToMessageJson))
            }
            
            val parcelledParent: BaseMessage? =
                androidx.core.content.IntentCompat.getParcelableExtra(intent, EXTRA_PARENT_MESSAGE, BaseMessage::class.java)
            if (parcelledParent != null) {
                // Live object: carries pinnedAt/pinnedBy/savedAt exactly as the list showed them.
                if (replyCount > 0) parcelledParent.replyCount = replyCount
                viewModel.setParentMessage(parcelledParent)
            } else if (rawMessage != null) {
                val parentMessage = BaseMessage.processMessage(JSONObject(rawMessage))
                parentMessage.replyCount = replyCount
                viewModel.setParentMessage(parentMessage)
            }
            
            if (userJson != null) {
                user = User.fromJson(userJson)
            }
            
            if (groupJson != null) {
                group = Gson().fromJson(groupJson, Group::class.java)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error parsing intent data: $e")
        }
        
        viewModel.addUserListener()
        
        if (user != null) {
            viewModel.setUser(user!!)
        }
    }
    
    private fun setupObservers() {
        viewModel.parentMessage.observe(this) { parentMessage ->
            setParentMessage(parentMessage)
        }
        
        viewModel.userBlockStatus.observe(this) { user ->
            setUserBlockedStatus(user)
        }
        
        viewModel.unblockButtonState.observe(this) { state ->
            setUnblockButtonState(state)
        }
    }
    
    private fun setUpTheme() {
        binding.backIcon.setColorFilter(CometChatTheme.getIconTintPrimary(this))
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.tvSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this))
        binding.unblockBtn.strokeColor = CometChatTheme.getStrokeColorDark(this)
        binding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
    }
    
    private fun windowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val bottomPadding = maxOf(imeInsets.bottom, navBarInsets.bottom)
            
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                bottomPadding
            )
            insets
        }
    }
    
    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }
    
    private fun setupUI() {
        // Set up back button behavior
        binding.backIcon.setOnClickListener {
            // Hide keyboard using InputMethodManager directly
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
            finish()
        }
        
        // Get the screen height
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        
        // Calculate 35% of the screen height for thread header max height
        val requiredHeight = (screenHeight * 0.35).toInt()
        binding.threadHeader.setMaxHeight(requiredHeight)
        
        // Unblock button click
        binding.unblockBtn.setOnClickListener {
            viewModel.unblockUser()
        }
        
        updateUserBlockStatus()
    }
    
    private fun setParentMessage(parentMessage: BaseMessage) {
        // Determine user or group from parent message
        if (UIKitConstants.ReceiverType.USER.equals(parentMessage.receiverType, ignoreCase = true)) {
            user = if (parentMessage.sender.uid.equals(
                    CometChatUIKit.getLoggedInUser()?.uid,
                    ignoreCase = true
                )
            ) parentMessage.receiver as User else parentMessage.sender
        } else if (UIKitConstants.ReceiverType.GROUP.equals(parentMessage.receiverType, ignoreCase = true)) {
            group = parentMessage.receiver as Group
        }
        
        // Navigate to message if provided
        if (goToMessageId > 0) {
            binding.messageList.gotoMessage(goToMessageId)
        } else if (goToMessage != null) {
            binding.messageList.gotoMessage(goToMessage!!.id)
        }
        
        // Set subtitle
        binding.tvSubtitle.text = when {
            user != null -> user!!.name
            group != null -> group!!.name
            else -> ""
        }
        binding.tvSubtitle.visibility = if (binding.tvSubtitle.text.toString().isEmpty()) View.GONE else View.VISIBLE
        
        // Configure ThreadHeader with parent message
        binding.threadHeader.setParentMessage(parentMessage)
        binding.threadHeader.setReactionVisibility(View.GONE)
        // Thread subscription bell lives in the title bar (Figma / Flutter parity); hide the kit
        // header's own control so only one bell shows.
        binding.threadHeader.setThreadSubscriptionVisibility(View.GONE)
        setupThreadSubscriptionBell(parentMessage)

        // Configure MessageList and MessageComposer with parent message ID
        binding.messageList.setParentMessage(parentMessage)
        binding.messageComposer.setParentMessageId(parentMessage.id)
        
        // Set user or group data to the message list and composer
        if (user != null) {
            binding.messageList.setUser(user!!)
            binding.messageComposer.setUser(user!!)
        } else if (group != null) {
            binding.messageList.setGroup(group!!)
            binding.messageComposer.setGroup(group!!)
        }
        
        // Show all attachment options (extension options are filtered out in threaded context by ViewModel)
        binding.messageComposer.setPollOptionVisibility(View.VISIBLE)
        binding.messageComposer.setCollaborativeDocumentOptionVisibility(View.VISIBLE)
        binding.messageComposer.setCollaborativeWhiteboardOptionVisibility(View.VISIBLE)
    }
    
    private fun setUserBlockedStatus(user: User) {
        if (this.user != null && this.user!!.uid == user.uid) {
            isBlockedByMe = user.isBlockedByMe
            updateUserBlockStatus()
        }
    }
    
    private fun updateUserBlockStatus() {
        if (isBlockedByMe) {
            binding.messageComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.messageComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }
    
    private fun setUnblockButtonState(dialogState: DialogState) {
        when (dialogState) {
            DialogState.INITIATED -> {
                binding.unblockText.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            }
            DialogState.SUCCESS, DialogState.FAILURE -> {
                binding.unblockText.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
            }
            else -> {}
        }
    }

    /**
     * Wires the thread-subscription (mute/unmute) bell in the title bar — the Figma / cross-platform
     * (Flutter) placement. Renders only when the feature gate is on and the thread has a valid root.
     * Optimistic flip on tap with a single in-flight guard; reverts with a snackbar on error; stays in
     * sync with changes from any surface via the kit event bus.
     */
    private fun setupThreadSubscriptionBell(parentMessage: BaseMessage) {
        val bell = binding.ivThreadSubscription
        val rootId = parentMessage.id
        if (!CometChatThreadSubscription.isAvailableForThread(parentMessage)) {
            bell.visibility = View.GONE
            return
        }
        bell.visibility = View.VISIBLE
        // State is read off the parent message — the server's per-viewer flag — never from a cache.
        renderThreadSubscriptionBell(parentMessage.isThreadSubscribed())

        bell.setOnClickListener {
            // The controller owns the debounce, in-flight lock, optimistic publish and revert; the
            // optimistic flip reaches this bell through the bus collector below.
            CometChatThreadSubscription.toggle(parentMessage, parentMessage.isThreadSubscribed()) { result ->
                val toast = when (result) {
                    is CometChatThreadSubscription.ToggleResult.Success -> getString(
                        if (result.subscribed) com.cometchat.uikit.kotlin.R.string.cometchat_thread_subscribed_toast
                        else com.cometchat.uikit.kotlin.R.string.cometchat_thread_unsubscribed_toast
                    )

                    is CometChatThreadSubscription.ToggleResult.Failure ->
                        getString(com.cometchat.uikit.kotlin.R.string.cometchat_thread_subscription_failed)
                }
                Toast.makeText(this@ThreadMessageActivity, toast, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            CometChatEvents.threadEvents.collect { event ->
                if (event is CometChatThreadEvent.SubscriptionChanged &&
                    event.parentMessageId == rootId
                ) {
                    // Stamp the held parent too, so a direct read stays coherent.
                    parentMessage.setThreadSubscribed(event.subscribed)
                    renderThreadSubscriptionBell(event.subscribed)
                }
            }
        }
    }

    private fun renderThreadSubscriptionBell(subscribed: Boolean) {
        binding.ivThreadSubscription.setImageResource(
            if (subscribed) com.cometchat.uikit.core.R.drawable.cometchat_ic_notifications
            else com.cometchat.uikit.core.R.drawable.cometchat_ic_notifications_off
        )
        binding.ivThreadSubscription.contentDescription = getString(
            if (subscribed) com.cometchat.uikit.kotlin.R.string.cometchat_thread_mute
            else com.cometchat.uikit.kotlin.R.string.cometchat_thread_unmute
        )
    }
}
