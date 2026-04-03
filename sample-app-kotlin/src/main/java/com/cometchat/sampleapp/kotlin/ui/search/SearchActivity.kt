package com.cometchat.sampleapp.kotlin.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.sampleapp.kotlin.databinding.ActivitySearchBinding

/**
 * Activity for global and contextual search across conversations and messages.
 *
 * Note: CometChatSearch component is not yet available in chatuikit-kotlin.
 * This activity serves as a placeholder until the component is implemented.
 *
 * ## Usage:
 * ```kotlin
 * // Global search
 * SearchActivity.start(context)
 *
 * // Contextual search for a user
 * SearchActivity.startWithUserId(context, userId = "user123")
 *
 * // Contextual search for a group
 * SearchActivity.startWithGroupId(context, groupId = "group123")
 * ```
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_GROUP_ID = "group_id"

        /**
         * Starts SearchActivity for global search.
         *
         * @param context The context to start the activity from
         */
        fun start(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * Starts SearchActivity with a user ID for contextual search.
         *
         * @param context The context to start the activity from
         * @param userId The UID of the user to search within
         */
        fun startWithUserId(context: Context, userId: String) {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }

        /**
         * Starts SearchActivity with a group ID for contextual search.
         *
         * @param context The context to start the activity from
         * @param groupId The GUID of the group to search within
         */
        fun startWithGroupId(context: Context, groupId: String) {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupUI()
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
     * Sets up the UI with back button functionality.
     */
    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
