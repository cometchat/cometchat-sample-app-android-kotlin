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
import com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
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
    }
    
    private lateinit var binding: ActivityThreadMessageBinding
    private lateinit var viewModel: ThreadMessageViewModel
    
    private var user: User? = null
    private var group: Group? = null
    private var goToMessage: BaseMessage? = null
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
            
            if (goToMessageJson != null) {
                goToMessage = BaseMessage.processMessage(JSONObject(goToMessageJson))
            }
            
            if (rawMessage != null) {
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
        if (goToMessage != null) {
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
        
        // Configure MessageList and MessageComposer with parent message ID
        binding.messageList.setParentMessageId(parentMessage.id)
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
}
