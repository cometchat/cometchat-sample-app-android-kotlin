package com.cometchat.sampleapp.kotlin.push.appflow

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityChatHistoryBinding

/**
 * Activity for displaying AI assistant chat history.
 *
 * Displays the CometChatAIAssistantChatHistory component,
 * which shows a scrollable list of past AI assistant conversations grouped by date.
 *
 * ## Features:
 * - View past AI assistant conversations
 * - Navigate to a specific conversation via MessagesActivity
 * - Start a new AI chat
 *
 * ## Usage:
 * Launch via intent with a Parcelable User extra:
 * ```kotlin
 * val intent = Intent(context, ChatHistoryActivity::class.java)
 * intent.putExtra("extra_user", user)
 * context.startActivity(intent)
 * ```
 */
class ChatHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChatHistoryActivity"
        private const val EXTRA_USER = "extra_user"
    }

    private lateinit var binding: ActivityChatHistoryBinding
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()

        // Extract the Parcelable User from the intent
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
                    // Navigate to MessagesActivity with no parentMessageId — fresh agent conversation
                    val intent = Intent(this@ChatHistoryActivity, MessagesActivity::class.java)
                    intent.putExtra(getString(R.string.app_user), u.toJson().toString())
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }

            setOnItemClickListener { _: View, position: Int, message: com.cometchat.chat.models.BaseMessage? ->
                Log.d(TAG, "onItemClick: position=$position, messageId=${message?.id}, messageType=${message?.type}")
                this@ChatHistoryActivity.user?.let { u ->
                    val intent = Intent(this@ChatHistoryActivity, MessagesActivity::class.java)
                    intent.putExtra(getString(R.string.app_user), u.toJson().toString())
                    message?.let {
                        Log.d(TAG, "Passing parent_message_id: ${it.id}")
                        intent.putExtra("parent_message_id", it.id.toLong())
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    Log.d(TAG, "Starting MessagesActivity with userId=${u.uid}, parentMessageId=${message?.id}")
                    startActivity(intent)
                    finish()
                }
            }

            setUser(user)
            Log.d(TAG, "setUser called on chatHistory component")
        }
    }
}
