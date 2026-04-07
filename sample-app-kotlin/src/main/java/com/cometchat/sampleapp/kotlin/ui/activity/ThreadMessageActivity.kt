package com.cometchat.sampleapp.kotlin.ui.activity

import android.content.Context
import android.content.Intent
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
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityThreadMessageBinding
import com.cometchat.sampleapp.kotlin.viewmodels.ThreadMessageViewModel
import com.cometchat.sampleapp.kotlin.utils.AppConstants
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class ThreadMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThreadMessageBinding
    private var user: User? = null
    private var group: Group? = null
    private var goToMessage: BaseMessage? = null
    private var isBlockedByMe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTheme()
        adjustWindowSettings()
        windowInsetsListener()

        val viewModel: ThreadMessageViewModel = ViewModelProvider.NewInstanceFactory().create(ThreadMessageViewModel::class.java)
        val goToMessageJson = intent.getStringExtra(getString(R.string.app_go_to_message))
        val rawMessage = intent.getStringExtra(AppConstants.JSONConstants.RAW_JSON)
        val replyCount = intent.getIntExtra(AppConstants.JSONConstants.REPLY_COUNT, 0)
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
                group = Group.fromJson(groupJson)
            }
        } catch (e: JSONException) {
            CometChatLogger.e("ThreadMessageActivity", "onCreate: $e")
        }
        viewModel.addUserListener()
        viewModel.parentMessage.observe(this, this::setParentMessage)
        viewModel.userBlockStatus.observe(this, this::setUserBlockedStatus)
        viewModel.unblockButtonState.observe(this, this::setUnblockButtonState)

        if (user != null)
            viewModel.setUser(user!!)

        binding.unblockBtn.setOnClickListener { _ -> viewModel.unblockUser() }
        initClickListeners()
        setupUI()
    }

    private fun setUserBlockedStatus(user: User) {
        if (this.user != null && this.user!!.uid == user.uid) {
            isBlockedByMe = user.isBlockedByMe
            updateUserBlockStatus()
        }
    }

    private fun initClickListeners() {
        binding.messageList.mentionsFormatter.setOnMentionClick { context: Context, user: User? ->
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra(context.getString(R.string.app_user), Gson().toJson(user))
            context.startActivity(intent)
        }

        binding.threadHeader.cometchatMentionsFormatter.setOnMentionClick { context: Context, user: User? ->
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra(context.getString(R.string.app_user), Gson().toJson(user))
            context.startActivity(intent)
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
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            if (binding.singleLineComposer.isFocused && isImeVisible) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom()
                }
            }
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
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun setupUI() {
        // Set up back button behavior
        binding.backIcon.setOnClickListener { _: View? ->
            Utils.hideKeyBoard(this, binding.root)
            finish()
        }

        // Get the screen height
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Calculate 25% of the screen height
        val requiredHeight = (screenHeight * 0.35).toInt()
        binding.threadHeader.maxHeight = requiredHeight

        updateUserBlockStatus()
    }

    private fun setParentMessage(parentMessage: BaseMessage) {
        if (UIKitConstants.ReceiverType.USER.equals(parentMessage.receiverType, ignoreCase = true)) {
            user = if (parentMessage.sender.uid.equals(
                    CometChatUIKit.getLoggedInUser().uid,
                    ignoreCase = true
                )
            ) parentMessage.receiver as User else parentMessage.sender
        } else if (UIKitConstants.ReceiverType.GROUP.equals(parentMessage.receiverType, ignoreCase = true)) {
            group = parentMessage.receiver as Group
        }

        if (goToMessage != null) binding.messageList.gotoMessage(goToMessage!!.id)
        binding.tvSubtitle.text = if (user != null) user!!.name else if (group != null) group!!.name else ""
        binding.tvSubtitle.visibility = if (binding.tvSubtitle.text.toString().isEmpty()) View.GONE else View.VISIBLE
        binding.messageList.setParentMessage(parentMessage.id)
        binding.threadHeader.parentMessage = parentMessage
        binding.threadHeader.reactionVisibility = View.GONE

        binding.singleLineComposer.setParentMessageId(parentMessage.id)

        // Set user or group data to the message list and composer
        if (user != null) {
            binding.messageList.user = user
            binding.singleLineComposer.setUser(user)
        } else if (group != null) {
            binding.messageList.group = group
            binding.singleLineComposer.setGroup(group)
        }
    }

    private fun updateUserBlockStatus() {
        if (isBlockedByMe) {
            binding.singleLineComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.singleLineComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }

    private fun setUnblockButtonState(dialogState: DialogState) {
        if (dialogState == DialogState.INITIATED) {
            binding.unblockText.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
        } else if (dialogState == DialogState.SUCCESS || dialogState == DialogState.FAILURE) {
            binding.unblockText.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
        }
    }
}
