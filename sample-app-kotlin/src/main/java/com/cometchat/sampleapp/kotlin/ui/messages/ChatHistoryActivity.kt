package com.cometchat.sampleapp.kotlin.ui.messages

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.ActivityChatHistoryBinding

/**
 * Activity for displaying AI assistant chat history.
 *
 * This activity displays the CometChatAIAssistantChatHistory component,
 * which shows a scrollable list of past AI assistant conversations grouped by date.
 *
 * ## Features:
 * - View past AI assistant conversations
 * - Navigate to a specific conversation
 * - Start a new AI chat
 * - Delete conversation history items
 *
 * ## Usage:
 * ```kotlin
 * ChatHistoryActivity.start(context, user)
 * ```
 *
 * @see com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory
 */
class ChatHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChatHistoryActivity"
        private const val EXTRA_USER = "extra_user"

        /**
         * Starts ChatHistoryActivity with a User object.
         *
         * @param context The context to start the activity from
         * @param user The user whose AI chat history to display
         */
        fun start(context: Context, user: User) {
            val intent = Intent(context, ChatHistoryActivity::class.java).apply {
                putExtra(EXTRA_USER, user)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityChatHistoryBinding
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()

        // Extract the Parcelable User directly from the intent
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_USER, User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_USER)
        }

        if (user == null) {
            Log.e(TAG, "onCreate: User is null, finishing activity")
            finish()
            return
        }

        Log.d(TAG, "onCreate: User loaded from intent: uid=${user!!.uid}, name=${user!!.name}")
        setupChatHistory(user!!)
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
     * Sets up the CometChatAIAssistantChatHistory component with callbacks.
     *
     * @param user The user whose chat history to display
     */
    private fun setupChatHistory(user: User) {
        Log.d(TAG, "setupChatHistory: user=${user.uid}, name=${user.name}")
        binding.chatHistory.apply {
            setOnCloseClickListener {
                Log.d(TAG, "onCloseClick")
                finish()
            }

            setOnNewChatClickListener {
                Log.d(TAG, "onNewChatClick: starting fresh AI conversation")
                this@ChatHistoryActivity.user?.let { u ->
                    MessagesActivity.start(this@ChatHistoryActivity, u)
                    finish()
                }
            }

            setOnItemClickListener { _: View, position: Int, message: com.cometchat.chat.models.BaseMessage? ->
                Log.d(TAG, "onItemClick: position=$position, message=${message?.id}, messageType=${message?.type}, text=${message}")
                this@ChatHistoryActivity.user?.let { u ->
                    val parentMessageId = message?.id?.toLong() ?: 0L
                    Log.d(TAG, "Navigating to MessagesActivity: userId=${u.uid}, parentMessageId=$parentMessageId")
                    MessagesActivity.start(this@ChatHistoryActivity, u, parentMessageId = parentMessageId)
                    finish()
                }
            }

            setUser(user)
            Log.d(TAG, "setUser called on chatHistory component")
        }
    }
}
