package com.cometchat.ai.sampleapp.kotlin.ui.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.ai.sampleapp.kotlin.R
import com.cometchat.ai.sampleapp.kotlin.app.AIAssistantApplication
import com.cometchat.ai.sampleapp.kotlin.databinding.ActivityAiAssistantChatBinding
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.max

/**
 * AI Assistant Chat screen — one-on-one chat with an AI agent.
 *
 * Layout:
 * - DrawerLayout with chat content on main pane and
 *   [com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory]
 *   as the end drawer.
 * - Header has Back + New Chat + Chat History buttons wired up.
 * - Composer auto-detects agent chat (via [com.cometchat.uikit.core.utils.AgentChatDetector])
 *   and hides attachment/voice/sticker/rich-text toolbar. We additionally call
 *   `setEnableRichTextFormatting(false)` to disable in-text rich formatting.
 */
class AIAssistantChatActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AIAssistantChatActivity"
    }

    private lateinit var binding: ActivityAiAssistantChatBinding
    private val viewModel: AIAssistantChatViewModel by viewModels()

    private var user: User? = null
    private var parentMessage: BaseMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        parseIntentExtras()

        AIAssistantApplication.currentOpenChatId = user?.uid

        adjustWindowSettings()
        applyWindowInsets()

        viewModel.addListener()
        observeViewModel()

        setupHeader()
        setupMessageListAndComposer()
        setupChatHistoryDrawer()

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
        AIAssistantApplication.currentOpenChatId = null
    }

    // ================== Init ==================

    private fun parseIntentExtras() {
        try {
            val userJson = intent.getStringExtra(getString(R.string.app_user))
            if (!userJson.isNullOrEmpty()) {
                user = User.fromJson(userJson)
            }

            val messageJson = intent.getStringExtra(getString(R.string.app_base_message))
            if (!messageJson.isNullOrEmpty()) {
                parentMessage = BaseMessage.processMessage(JSONObject(messageJson))
            }
        } catch (e: JSONException) {
            Log.e(TAG, "parseIntentExtras: ${e.message}")
        }
    }

    private fun adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomInset = max(ime.bottom, nav.bottom)

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                binding.messageList.scrollToBottom()
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }
    }

    // ================== Header ==================

    private fun setupHeader() {
        val currentUser = user ?: return
        binding.messageHeader.setUser(currentUser)

        // Show the back button (kotlin UIKit defaults this to GONE, unlike the
        // Compose version where the back button is visible by default).
        binding.messageHeader.setBackButtonVisibility(View.VISIBLE)

        binding.messageHeader.setOnBackPress {
            hideKeyboard()
            finish()
        }

        binding.messageHeader.setOnNewChatClick {
            hideKeyboard()
            val intent = Intent(this, AIAssistantChatActivity::class.java).apply {
                putExtra(getString(R.string.app_user), currentUser.toJson().toString())
            }
            startActivity(intent)
            finish()
        }

        binding.messageHeader.setOnChatHistoryClick {
            binding.drawerLayout.refreshDrawableState()
            binding.aiAssistantChatHistory.setUser(currentUser)
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    // ================== Message list + composer ==================

    private fun setupMessageListAndComposer() {
        val currentUser = user ?: return

        parentMessage?.let {
            binding.messageList.setParentMessageId(it.id)
            binding.messageComposer.setParentMessageId(it.id)
        }

        binding.messageList.setUser(currentUser)
        binding.messageComposer.setUser(currentUser)
        // Agent-chat UX: keep the composer compact (no rich-text, no attachments).
        // AgentChatDetector already hides attachment/voice/sticker for agents inside setUser;
        // we additionally disable rich-text formatting for a cleaner single-line feel.
        binding.messageComposer.setEnableRichTextFormatting(false)
        binding.messageComposer.setRichTextToolbarVisibility(View.GONE)
    }

    // ================== Chat history drawer ==================

    private fun setupChatHistoryDrawer() {
        binding.aiAssistantChatHistory.setOnCloseClickListener {
            closeChatHistoryDrawer()
        }

        binding.aiAssistantChatHistory.setOnNewChatClickListener {
            val currentUser = user ?: return@setOnNewChatClickListener
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            }
            val intent = Intent(this, AIAssistantChatActivity::class.java).apply {
                putExtra(getString(R.string.app_user), currentUser.toJson().toString())
            }
            startActivity(intent)
            finish()
        }

        binding.aiAssistantChatHistory.setOnItemClickListener { _, _, message ->
            if (message == null) return@setOnItemClickListener
            val receiver = message.receiver
            if (receiver is User) {
                val intent = Intent(this, AIAssistantChatActivity::class.java).apply {
                    putExtra(getString(R.string.app_user), receiver.toJson().toString())
                    putExtra(getString(R.string.app_base_message), message.rawMessage.toString())
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun closeChatHistoryDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    // ================== ViewModel observers ==================

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sentMessage.collect { sent ->
                        if (sent) hideKeyboard()
                    }
                }
                launch {
                    viewModel.openUserChat.collect { targetUser ->
                        if (targetUser != null) openUserChat(targetUser)
                    }
                }
                launch {
                    viewModel.isExitActivity.collect { exit ->
                        if (exit) finish()
                    }
                }
            }
        }
    }

    private fun openUserChat(targetUser: User) {
        val intent = Intent(this, AIAssistantChatActivity::class.java).apply {
            putExtra(getString(R.string.app_user), targetUser.toJson().toString())
        }
        startActivity(intent)
        finish()
    }

    // ================== Back handling ==================

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            hideKeyboard()
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                finish()
            }
        }
    }

    // ================== Helpers ==================

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        val focused = currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(focused.windowToken, 0)
    }
}
