package com.cometchat.sampleapp.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityMessagesBinding
import com.cometchat.sampleapp.kotlin.databinding.OverflowMenuLayoutBinding
import com.cometchat.sampleapp.kotlin.utils.AppConstants
import com.cometchat.sampleapp.kotlin.utils.MyApplication
import com.cometchat.sampleapp.kotlin.viewmodels.MessagesViewModel
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.max

class MessagesActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MessagesActivity"
    }

    private var user: User? = null
    private var group: Group? = null
    private var baseMessage: BaseMessage? = null
    private lateinit var viewModel: MessagesViewModel
    private lateinit var binding: ActivityMessagesBinding
    private var goToMessage: BaseMessage? = null

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
        val goToMessageJson = data.getStringExtra(getString(R.string.app_go_to_message))
        val parentMessageJson = data.getStringExtra(getString(R.string.app_base_message))

        if (parentMessageJson != null) {
            val intent = Intent(this, ThreadMessageActivity::class.java).apply {
                putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessageJson)
                selectedUserJson?.let { putExtra(getString(R.string.app_user), it) }
                selectedGroupJson?.let { putExtra(getString(R.string.app_group), it) }
            }
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, MessagesActivity::class.java).apply {
                selectedUserJson?.let { putExtra(getString(R.string.app_user), it) }
                selectedGroupJson?.let { putExtra(getString(R.string.app_group), it) }
                goToMessageJson?.let { putExtra(getString(R.string.app_go_to_message), it) }
            }
            startActivity(intent)
            finish()
        }
    }

    private fun handleSameChatNavigation(data: Intent) {
        val goToMessageJson = data.getStringExtra(getString(R.string.app_go_to_message))
        goToMessageJson?.let {
            try {
                this.goToMessage = BaseMessage.processMessage(JSONObject(it))
                addViews()
            } catch (e: JSONException) {
                CometChatLogger.e(TAG, "Error processing goto message: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTheme()
        adjustWindowSettings()
        applyWindowInsets()
        extractIntentData(intent)
        initViewModel()
        addViews()
        initClickListeners()
        if (!Utils.isAgentChat(user))
            setUpMessageHeaderMenu()
        MyApplication.currentOpenChatId = if (group != null) group!!.guid else user?.uid
    }

    private fun extractIntentData(intent: Intent) {
        group = Gson().fromJson(intent.getStringExtra(getString(R.string.app_group)), Group::class.java)
        try {
            val rawGoToMessage = intent.getStringExtra(getString(R.string.app_go_to_message))
            val userJson = intent.getStringExtra(getString(R.string.app_user))
            if (rawGoToMessage != null)
                goToMessage = BaseMessage.processMessage(JSONObject(rawGoToMessage))
            if (userJson != null)
                user = User.fromJson(JSONObject(userJson).toString())
        } catch (e: JSONException) {
            CometChatLogger.e(TAG, e.message)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(MessagesViewModel::class.java)
        // Set the user and group in the ViewModel
        viewModel.setUser(user)
        viewModel.setGroup(group)

        // Add listeners for ViewModel updates
        viewModel.addListener()

        viewModel.updatedGroup.observe(this) { group: Group -> this.updateGroupJoinedStatus(group) }
        viewModel.baseMessage.observe(this) { baseMessage: BaseMessage? ->
            if (baseMessage != null) {
                this.setBaseMessage(baseMessage)
            }
        }
        viewModel.updateUser.observe(this) { user: User ->
            this.user = user
            this.updateUserBlockStatus(user)
        }
        viewModel.openUserChat().observe(this) { user: User? -> this.openUserChat(user) }
        viewModel.isExitActivity.observe(this) { exit: Boolean -> this.exitActivity(exit) }
        viewModel.unblockButtonState.observe(this) { dialogState: DialogState -> this.setUnblockButtonState(dialogState) }
    }

    private fun initClickListeners() {
        binding.unblockBtn.setOnClickListener { view: View? -> viewModel.unblockUser() }
        binding.messageList.mentionsFormatter.setOnMentionClick { context: Context, user: User? ->
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra(context.getString(R.string.app_user), Gson().toJson(user))
            context.startActivity(intent)
        }
        binding.messageList.setOnThreadRepliesClick { context: Context, baseMessage: BaseMessage, cometchatMessageTemplate: CometChatMessageTemplate? ->
            val intent = Intent(context, ThreadMessageActivity::class.java)
            if (user != null) {
                intent.putExtra(context.getString(R.string.app_user), user?.toJson().toString())
            } else if (group != null) {
                intent.putExtra(context.getString(R.string.app_group), Gson().toJson(group))
            }
            intent.putExtra("isBlockedByMe", user?.isBlockedByMe)
            intent.putExtra(AppConstants.JSONConstants.REPLY_COUNT, baseMessage.replyCount)
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, baseMessage.getRawMessage().toString())
            context.startActivity(intent)
        }
    }

    private fun setUpMessageHeaderMenu() {
        val options = getHeaderMenuOptions()
        binding.messageHeader.options = options
        binding.messageHeader.setPopupMenuStyle(R.style.CustomHeaderPopUpMenuStyle)
    }

    private fun getHeaderMenuOptions(): List<CometChatPopupMenu.MenuItem> {
        val options = mutableListOf<CometChatPopupMenu.MenuItem>()
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.SEARCH,
                getString(com.cometchat.chatuikit.R.string.cometchat_menu_search),
                AppCompatResources.getDrawable(this@MessagesActivity, com.cometchat.chatuikit.R.drawable.cometchat_ic_search),
                null,
            ) { navigateToSearchActivity() }
        )
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.CONVERSATION_SUMMARY,
                getString(com.cometchat.chatuikit.R.string.cometchat_menu_conversation_summary),
                AppCompatResources.getDrawable(
                    this@MessagesActivity,
                    com.cometchat.chatuikit.R.drawable.cometchat_ic_menu_conversation_summary
                ),
                null
            ) { generateConversationSummary() }
        )
        options.add(
            CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.DETAILS,
                getString(com.cometchat.chatuikit.R.string.cometchat_details),
                AppCompatResources.getDrawable(this@MessagesActivity, R.drawable.ic_info),
                null
            ) {
                openDetailScreen()
            }
        )

        return options
    }

    private fun generateConversationSummary() {
        binding.messageList.generateConversationSummary()
    }

    private fun navigateToSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        if (user != null)
            intent.putExtra(getString(R.string.app_user), user?.toJson().toString())
        else
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
        searchActivityLauncher.launch(intent)
    }

    private fun setUpTheme() {
        binding.parentView.setBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        binding.infoLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        binding.separator.setBackgroundColor(CometChatTheme.getStrokeColorLight(this))
        binding.infoText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockTitle.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this))
        binding.unblockBtn.strokeColor = CometChatTheme.getStrokeColorDark(this)
        binding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
    }

    /**
     * Adjusts the window settings for the activity.
     */

    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentView) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val bottomInset = max(ime.bottom.toDouble(), nav.bottom.toDouble()).toInt()

            if (isImeVisible && binding.singleLineComposer.isFocused) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom()
                }
            }

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomInset
            )
            insets
        }
    }

    /**
     * Updates the visibility of the unblock button based on the dialog state.
     *
     * @param dialogState
     * The current state of the unblock dialog.
     */
    private fun setUnblockButtonState(dialogState: DialogState) {
        if (dialogState == DialogState.INITIATED) {
            binding.unblockText.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
        } else if (dialogState == DialogState.SUCCESS || dialogState == DialogState.FAILURE) {
            binding.unblockText.visibility = View.VISIBLE
            binding.progress.visibility = View.GONE
        }
    }

    /**
     * Updates the UI based on the group's joined status.
     *
     * @param group
     * The updated group object.
     */
    private fun updateGroupJoinedStatus(group: Group) {
        if (!group.isJoined) {
            binding.unblockBtn.visibility = View.GONE
            binding.singleLineComposer.visibility = View.GONE
            binding.infoLayout.visibility = View.VISIBLE
        } else {
            binding.unblockBtn.visibility = View.GONE
            binding.singleLineComposer.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.GONE
        }
    }

    private fun setBaseMessage(baseMessage: BaseMessage) {
        this.baseMessage = baseMessage
    }

    /**
     * Updates the UI based on the user's block status.
     *
     * @param user
     * The updated user object.
     */
    private fun updateUserBlockStatus(user: User) {
        if (user.isBlockedByMe) {
            binding.singleLineComposer.visibility = View.GONE
            binding.unblockLayout.visibility = View.VISIBLE
        } else {
            binding.singleLineComposer.visibility = View.VISIBLE
            binding.unblockLayout.visibility = View.GONE
        }
    }

    /**
     * Opens the chat interface for the specified user.
     *
     * @param user
     * The user object representing the chat participant. Must not be null.
     */
    private fun openUserChat(user: User?) {
        if (user != null) {
            val intent = Intent(this, MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            startActivity(intent)
        }
    }

    /**
     * Exits the activity if the exit flag is true.
     *
     * @param exit
     * Indicates whether to exit the activity.
     */
    private fun exitActivity(exit: Boolean) {
        if (exit) {
            finish()
        }
    }

    /** Configures the overflow menu for additional actions.  */
    private fun setOverFlowMenu() {
        binding.messageHeader.setTrailingView { context, user, group ->
            val linearLayout = LinearLayout(context)
            val overflowMenuLayoutBinding = OverflowMenuLayoutBinding.inflate(layoutInflater)
            overflowMenuLayoutBinding.ivMenu.setImageResource(R.drawable.ic_info)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.gravity = Gravity.CENTER_VERTICAL

            if ((group != null && group.isJoined) || (user != null && !Utils.isBlocked(user))) {
                linearLayout.addView(overflowMenuLayoutBinding.getRoot())
            }

            overflowMenuLayoutBinding.ivMenu.setOnClickListener { view1 -> openDetailScreen() }
            linearLayout
        }
    }

    /** Opens the detail screen for the selected user or group.  */
    private fun openDetailScreen() {
        var intent: Intent? = null
        if (user != null) {
            intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            intent.putExtra(getString(R.string.app_base_message), Gson().toJson(binding.messageList.viewModel.lastMessage))
        } else if (group != null) {
            intent = Intent(this, GroupDetailsActivity::class.java)
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            intent.putExtra(getString(R.string.app_base_message), Gson().toJson(binding.messageList.viewModel.lastMessage))
        }
        startActivity(intent)
    }

    /** Initializes UI components */
    private fun addViews() {
        // Set user or group data to the message header and composer
        if (goToMessage != null) binding.messageList.gotoMessage(goToMessage!!.id)
        if (user != null) {
            binding.messageHeader.user = user!!
            binding.messageList.user = user
            binding.singleLineComposer.setUser(user)
            updateUserBlockStatus(user!!)
        } else if (group != null) {
            binding.messageHeader.group = group!!
            binding.messageList.group = group
            binding.singleLineComposer.setGroup(group)
            updateGroupJoinedStatus(group!!)
        }

        binding.messageList.isStartFromUnreadMessages = true
        binding.messageList.markAsUnreadOptionVisibility = View.VISIBLE

        // Set up back button behavior
        binding.messageHeader.setOnBackButtonPressed {
            Utils.hideKeyBoard(this, binding.root)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy() // Remove listener from the ViewModel to prevent memory leaks
        viewModel.removeListener()
        MyApplication.currentOpenChatId = null
    }
}
