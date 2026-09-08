package com.cometchat.sampleapp.kotlin.push

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.push.appflow.DefaultComponentAppFlowActivity
import com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity
import com.cometchat.sampleapp.kotlin.push.databinding.ActivitySplashBinding
import com.cometchat.sampleapp.kotlin.push.shared.SplashNavigationState
import com.cometchat.sampleapp.kotlin.push.shared.SplashViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * Splash screen activity that displays the CometChat logo and handles
 * SDK initialization and navigation based on credentials and login state.
 * 
 * Navigation Logic:
 * - No credentials configured → AppCredentialsActivity
 * - Credentials exist, user logged in → the App Flow screen
 * - Credentials exist, user not logged in → LoginActivity (with "Not logged in" toast)
 * - SDK initialization error → Show error toast
 * 
 * Validates: Requirements 1.1, 1.3, 1.7, 1.8
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashViewModel: SplashViewModel by viewModels()

    /** A tapped chat notification, opened once the SDK is up and the user is known to be logged in. */
    private var pushDeepLink: PushDeepLink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pushDeepLink = parsePushDeepLink(intent)

        // Apply system window insets padding to avoid overlap with system bars
        applyWindowInsets()

        // Observe navigation state from ViewModel
        observeNavigationState()

        // Start credentials check and SDK initialization
        splashViewModel.checkCredentialsAndInitialize(applicationContext)
    }

    /**
     * Applies system window insets padding to the main container
     * to avoid overlap with status bar and navigation bar.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Observes the SplashViewModel navigation state and handles navigation
     * to the appropriate screen based on credentials and login status.
     */
    private fun observeNavigationState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.navigationState.collect { state ->
                    when (state) {
                        is SplashNavigationState.Loading -> {
                            // Show loading state - logo is already displayed
                        }
                        is SplashNavigationState.NavigateToAppCredentials -> {
                            navigateToAppCredentials()
                        }
                        is SplashNavigationState.NavigateToLogin -> {
                            Toast.makeText(this@SplashActivity, "Not logged in", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        }
                        is SplashNavigationState.NavigateToHome -> {
                            navigateToHome()
                        }
                        is SplashNavigationState.Error -> {
                            Toast.makeText(this@SplashActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Navigates to AppCredentialsActivity when no credentials are configured.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToAppCredentials() {
        val intent = Intent(this, AppCredentialsActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to LoginActivity when credentials exist but user is not logged in.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to the App Flow screen when user is already logged in.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToHome() {
        val link = pushDeepLink
        if (link == null) {
            startActivity(Intent(this, DefaultComponentAppFlowActivity::class.java))
            finish()
            return
        }
        pushDeepLink = null
        openConversationFromPush(link)
    }

    /**
     * A tapped chat notification, as delivered by Firebase's own notification display.
     *
     * The push carries a `notification` block, so with the app killed FCM posts the notification
     * itself: [com.cometchat.sampleapp.kotlin.push.fcm.AppFCMService] never sees it,
     * `CometChatPushNotifications.handlePushNotification` never runs, and the tap listener in
     * [KotlinApplication] that would have set the app's own extras never fires. Firebase launches
     * this Activity with the raw push data instead, which is what [parsePushDeepLink] reads.
     */
    private data class PushDeepLink(
        val userId: String?,
        val groupId: String?,
        val messageId: Long,
        /** Greater than zero for a thread reply — the thread opens rather than the conversation. */
        val parentMessageId: Long
    )

    private fun parsePushDeepLink(intent: Intent?): PushDeepLink? {
        if (intent?.getStringExtra("type") != "chat") return null

        val isGroup = intent.getStringExtra("receiverType") == "group"
        val sender = intent.getStringExtra("sender")
        val receiver = intent.getStringExtra("receiver")
        // In a 1-1 push the receiver is the logged-in user, so the conversation is the sender.
        val userId = if (isGroup) null else sender
        val groupId = if (isGroup) receiver else null
        if (userId == null && groupId == null) return null

        return PushDeepLink(
            userId = userId,
            groupId = groupId,
            messageId = intent.getStringExtra("tag")?.toLongOrNull() ?: 0L,
            parentMessageId = intent.getStringExtra("parentId")?.toLongOrNull() ?: 0L
        )
    }

    /**
     * Opens the tapped conversation. The payload carries only ids, so the entity is fetched first —
     * safe here because this runs after the SDK is initialized. The App Flow screen is started
     * underneath so Back lands in the app rather than leaving it.
     */
    private fun openConversationFromPush(link: PushDeepLink) {
        startActivity(Intent(this, DefaultComponentAppFlowActivity::class.java))

        if (link.groupId != null) {
            CometChat.getGroup(link.groupId, object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(group: Group) {
                    startMessagesForPush(userJson = null, groupJson = Gson().toJson(group), link = link)
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "Could not open group ${link.groupId} from a notification: ${e.message}")
                    finish()
                }
            })
        } else {
            val userId = link.userId ?: return finish()
            CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(user: User) {
                    startMessagesForPush(userJson = user.toJson().toString(), groupJson = null, link = link)
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "Could not open user $userId from a notification: ${e.message}")
                    finish()
                }
            })
        }
    }

    /**
     * Hands off to [MessagesActivity], which stacks the thread on top when the tapped message is a
     * reply (see its notification extras).
     */
    private fun startMessagesForPush(userJson: String?, groupJson: String?, link: PushDeepLink) {
        val intent = Intent(this, MessagesActivity::class.java).apply {
            userJson?.let { putExtra(getString(R.string.app_user), it) }
            groupJson?.let { putExtra(getString(R.string.app_group), it) }
            if (link.parentMessageId > 0) {
                putExtra(MessagesActivity.EXTRA_NOTIFICATION_PARENT_ID, link.parentMessageId)
                putExtra(MessagesActivity.EXTRA_NOTIFICATION_REPLY_ID, link.messageId)
            } else if (link.messageId > 0) {
                putExtra("goToMessageId", link.messageId)
            }
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}
