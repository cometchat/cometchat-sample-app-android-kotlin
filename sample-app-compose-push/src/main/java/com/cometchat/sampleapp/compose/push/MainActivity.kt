package com.cometchat.sampleapp.compose.push

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.os.Build
import com.cometchat.pushnotification.helpers.CometChatPNHelper
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.PushNotification
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCall
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.navigation.AppNavigation
import com.cometchat.sampleapp.compose.push.ui.AppTheme

class MainActivity : ComponentActivity() {
    
    private val TAG = "MainActivity"
    
    // Holds navigation state for push notification deep link
    companion object {
        /** Compose-observable state: true when we need to auto-navigate to Notifications tab */
        private val _shouldNavigateToNotifications = androidx.compose.runtime.mutableStateOf(false)
        val shouldNavigateToNotifications: Boolean get() = _shouldNavigateToNotifications.value
        
        /** Compose-observable trigger: incremented each time a notification deep link arrives */
        private val _notificationDeepLinkTrigger = androidx.compose.runtime.mutableStateOf(0)
        val notificationDeepLinkTrigger: Int get() = _notificationDeepLinkTrigger.value
        
        fun consumeNavigateToNotifications(): Boolean {
            val should = _shouldNavigateToNotifications.value
            _shouldNavigateToNotifications.value = false
            return should
        }
        
        internal fun triggerNotificationNavigation() {
            _shouldNavigateToNotifications.value = true
            _notificationDeepLinkTrigger.value++
        }

        /** A tapped chat notification, waiting for the NavHost to be ready to route it. */
        private val _pendingChatDeepLink =
            androidx.compose.runtime.mutableStateOf<ChatDeepLink?>(null)
        val pendingChatDeepLink: ChatDeepLink? get() = _pendingChatDeepLink.value

        /** Compose-observable trigger: incremented each time a chat deep link arrives. */
        private val _chatDeepLinkTrigger = androidx.compose.runtime.mutableStateOf(0)
        val chatDeepLinkTrigger: Int get() = _chatDeepLinkTrigger.value

        /** Returns the pending link and clears it, so it is routed exactly once. */
        fun consumeChatDeepLink(): ChatDeepLink? {
            val link = _pendingChatDeepLink.value
            _pendingChatDeepLink.value = null
            return link
        }

        internal fun triggerChatDeepLink(link: ChatDeepLink) {
            _pendingChatDeepLink.value = link
            _chatDeepLinkTrigger.value++
        }
    }

    /**
     * A tapped chat notification. [parentMessageId] is greater than zero when the tapped message is
     * a thread reply, in which case the thread opens instead of the main conversation.
     */
    data class ChatDeepLink(
        val userId: String?,
        val groupId: String?,
        val messageId: Long,
        val parentMessageId: Long
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Handle notification tap intent
        handleNotificationIntent(intent)

        setContent {
            MainNavigation()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    
    private fun handleNotificationIntent(intent: Intent?) {
        val notificationType = intent?.getStringExtra(com.cometchat.sampleapp.compose.push.utils.AppConstants.FCMConstants.NOTIFICATION_TYPE)
        if (notificationType == com.cometchat.sampleapp.compose.push.utils.AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE) {
            val keys = com.cometchat.sampleapp.compose.push.utils.AppConstants.FCMConstants
            val userId = intent.getStringExtra(keys.KEY_UID)
            val groupId = intent.getStringExtra(keys.KEY_GUID)
            if (userId == null && groupId == null) {
                Log.w(TAG, "Chat notification tapped without a conversation id — ignoring")
                return
            }
            // Parked rather than navigated: the NavHost may still be on Splash, and on a cold start
            // the SDK is not initialized yet. AppNavigation routes it once it can.
            triggerChatDeepLink(
                ChatDeepLink(
                    userId = userId,
                    groupId = groupId,
                    messageId = intent.getLongExtra(keys.KEY_MESSAGE_ID, 0L),
                    parentMessageId = intent.getLongExtra(keys.KEY_PARENT_MESSAGE_ID, 0L)
                )
            )
            return
        }
        if (notificationType == "business_messaging") {
            val feedItemId = intent.getStringExtra("notification_feed_item_id")
            val pushNotificationId = intent.getStringExtra("push_notification_id")
            
            // Mark push as clicked
            if (!pushNotificationId.isNullOrEmpty() && com.cometchat.uikit.core.CometChatUIKit.isSDKInitialized()) {
                val pushNotification = PushNotification(pushNotificationId, feedItemId, null, "campaign")
                CometChat.markPushNotificationClicked(pushNotification, object : CometChat.CallbackListener<Void>() {
                    override fun onSuccess(result: Void?) {
                        Log.d(TAG, "Push notification marked as clicked")
                    }
                    override fun onError(e: com.cometchat.chat.exceptions.CometChatException) {
                        Log.e(TAG, "Failed to mark push clicked: ${e.message}")
                    }
                })
            }
            
            // Navigate to Notifications tab when app opens
            triggerNotificationNavigation()
            return
        }
        parkDeepLinkFromPushPayload(intent)
    }

    /**
     * Routes a tap on a chat notification that Firebase displayed itself.
     *
     * The push carries a `notification` block, so when the app is backgrounded FCM posts the
     * notification directly: [com.cometchat.sampleapp.compose.push.fcm.AppFCMService.onMessageReceived]
     * never runs, `CometChatPushNotifications.handlePushNotification` never runs, and the tap
     * listener in [ComposeApplication] that would have set our own extras never fires. What the
     * launcher Intent does carry is the raw push data, so the conversation and thread are read from
     * those keys.
     */
    private fun parkDeepLinkFromPushPayload(intent: Intent?) {
        if (intent?.getStringExtra("type") != "chat") return

        val isGroup = intent.getStringExtra("receiverType") == "group"
        val sender = intent.getStringExtra("sender")
        val receiver = intent.getStringExtra("receiver")
        // In a 1-1 push the receiver is the logged-in user, so the conversation is the sender.
        val userId = if (isGroup) null else sender
        val groupId = if (isGroup) receiver else null
        if (userId == null && groupId == null) {
            Log.w(TAG, "Chat push tapped without a conversation id — ignoring")
            return
        }

        triggerChatDeepLink(
            ChatDeepLink(
                userId = userId,
                groupId = groupId,
                messageId = intent.getStringExtra("tag")?.toLongOrNull() ?: 0L,
                // Present only for a thread reply — routes to the thread instead of the conversation.
                parentMessageId = intent.getStringExtra("parentId")?.toLongOrNull() ?: 0L
            )
        )
    }
}

/**
 * Main navigation composable that uses NavHost-based navigation.
 * Delegates all navigation handling to AppNavigation composable.
 * Also observes incoming call state and shows incoming call UI overlay.
 */
/**
 * Requests the POST_NOTIFICATIONS runtime permission on Android 13+ using a Compose launcher.
 * Relies on the CometChat SDK helper to read the current permission state.
 */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("MainActivity", "POST_NOTIFICATIONS granted=$granted")
    }
    LaunchedEffect(Unit) {
        if (!CometChatPNHelper.hasNotificationPermission(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun MainNavigation() {
    val systemDarkMode = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkMode) }
    val navController = rememberNavController()

    // Request the POST_NOTIFICATIONS runtime permission (Android 13+).
    RequestNotificationPermission()

    // Observe incoming call state from Application
    val application = ComposeApplication.getInstance()
    val incomingCall by application?.incomingCall?.collectAsState() ?: remember { mutableStateOf(null) }
    
    AppTheme(isDarkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main app navigation
            AppNavigation(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                isDarkMode = isDarkMode,
                onDarkModeChange = { isDarkMode = it }
            )
            
            // Incoming call overlay at the top
            incomingCall?.let { call ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    CometChatIncomingCall(
                        call = call,
                        modifier = Modifier.fillMaxWidth(),
                        disableSoundForCalls = true, // Sound is managed by Application
                        // Don't provide onAcceptClick - let the component handle accept via viewModel.acceptCall()
                        // and auto-launch CometChatOngoingCallActivity
                        onAcceptClick = null,
                        // Don't provide onRejectClick - let the component handle rejection via viewModel.rejectCall()
                        // The CallRejected event will trigger dismissIncomingCall() via addCallEventsListener()
                        onRejectClick = null,
                        onError = { exception ->
                            Log.e("MainActivity", "IncomingCall error: code=${exception.code}, message=${exception.message}", exception)
                            application?.dismissIncomingCall()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val colors = CometChatTheme.colorScheme

    androidx.compose.material3.Text(
        text = "Hello $name!",
        color = colors.primary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        Greeting("Android")
    }
}
