package com.cometchat.uikit.kotlin.presentation.messagelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Helper object to initialize CometChat SDK and log in for instrumented tests.
 *
 * The MessageAdapter calls `CometChat.getLoggedInUser()` to determine message alignment,
 * which requires the SDK to be initialized and a user to be logged in. This helper
 * performs initialization with real credentials and logs in as `cometchat-uid-2` so that
 * message alignment (incoming vs outgoing) is correctly determined.
 *
 * Call [ensureInitialized] once in `@BeforeClass` or `@Before` of any test that renders
 * messages in the RecyclerView.
 */
object MessageListTestSdkHelper {

    private const val TAG = "MessageListTestHelper"

    private const val APP_ID = "278059f315a564b4"
    private const val AUTH_KEY = "5bb2416b7eb003c1f94234c26178a4b053c66b97"
    private const val REGION = "in"
    private const val LOGIN_UID = "cometchat-uid-2"

    @Volatile
    private var initialized = false

    /**
     * The UID of the logged-in test user. Use this when creating outgoing messages
     * so that the message alignment logic correctly identifies them as outgoing.
     */
    const val LOGGED_IN_USER_UID = LOGIN_UID

    /**
     * Initializes CometChat SDK with real credentials and logs in as [LOGIN_UID].
     * Safe to call multiple times — only initializes once.
     */
    fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            val context = ApplicationProvider.getApplicationContext<android.app.Application>()
            val appSettings = AppSettings.AppSettingsBuilder()
                .setRegion(REGION)
                .autoEstablishSocketConnection(false)
                .build()

            // Step 1: Initialize the SDK
            val initLatch = CountDownLatch(1)
            var initSuccess = false

            CometChat.init(
                context,
                APP_ID,
                appSettings,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(result: String?) {
                        android.util.Log.d(TAG, "CometChat.init() succeeded")
                        initSuccess = true
                        initLatch.countDown()
                    }

                    override fun onError(e: CometChatException?) {
                        android.util.Log.e(TAG, "CometChat.init() failed: ${e?.message}")
                        initLatch.countDown()
                    }
                }
            )

            if (!initLatch.await(30, TimeUnit.SECONDS)) {
                android.util.Log.e(TAG, "CometChat.init() timed out after 30s")
                initialized = true
                return
            }

            if (!initSuccess) {
                android.util.Log.e(TAG, "CometChat init failed, tests may not work correctly")
                initialized = true
                return
            }

            // Step 2: Check if already logged in
            val loggedInUser = CometChat.getLoggedInUser()
            if (loggedInUser != null) {
                android.util.Log.d(TAG, "Already logged in as ${loggedInUser.uid}")
                initialized = true
                return
            }

            // Step 3: Log in with the test user
            val loginLatch = CountDownLatch(1)
            var loginSuccess = false

            CometChat.login(
                LOGIN_UID,
                AUTH_KEY,
                object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                    override fun onSuccess(user: com.cometchat.chat.models.User?) {
                        android.util.Log.d(TAG, "CometChat.login() succeeded for uid=${user?.uid}")
                        loginSuccess = true
                        loginLatch.countDown()
                    }

                    override fun onError(e: CometChatException?) {
                        android.util.Log.e(TAG, "CometChat.login() failed: code=${e?.code}, message=${e?.message}")
                        loginLatch.countDown()
                    }
                }
            )

            if (!loginLatch.await(30, TimeUnit.SECONDS)) {
                android.util.Log.e(TAG, "CometChat.login() timed out after 30s")
            }

            if (loginSuccess) {
                android.util.Log.d(TAG, "Init + Login complete. LoggedInUser uid=${CometChat.getLoggedInUser()?.uid}")
            } else {
                android.util.Log.e(TAG, "Login failed. Tests requiring outgoing message detection may fail.")
            }

            initialized = true
        }
    }
}

/**
 * A minimal Fragment that hosts CometChatMessageList for instrumented testing.
 *
 * The DataSource/Repository is injected via the companion object before launch.
 * The ViewModel is built inside onViewCreated (on the main thread) to ensure
 * proper coroutine dispatching with viewModelScope.
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList View]
 *       → [Espresso assertions on rendered UI]
 *
 * The ONLY fake is the Repository at the data boundary. Everything else is real
 * production code — ViewModel, View, Adapter, ViewHolder.
 *
 * Usage:
 *   MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
 *   MessageListHostFragment.userToSet = createUser(...)
 *   launchFragmentInContainer<MessageListHostFragment>(themeResId = R.style.CometChatTheme_DayNight)
 */
class MessageListHostFragment : Fragment() {

    companion object {
        /** Injected before fragment launch — cleared after each test */
        var injectedRepository: MessageListRepository? = null

        /** User to configure the message list for (mutually exclusive with groupToSet) */
        var userToSet: User? = null

        /** Group to configure the message list for (mutually exclusive with userToSet) */
        var groupToSet: Group? = null

        /** Callback results for assertion */
        var onItemClickMessage: BaseMessage? = null
        var onItemClickPosition: Int = -1
        var onItemLongClickMessage: BaseMessage? = null
        var onItemLongClickPosition: Int = -1
        var onThreadRepliesClickMessage: BaseMessage? = null
        var onErrorThrowable: Throwable? = null
        var onLoadInvoked: Boolean = false
        var onEmptyInvoked: Boolean = false

        /** Custom view factories */
        var customHeaderView: ((android.content.Context) -> View)? = null
        var customFooterView: ((android.content.Context) -> View)? = null
        var customLoadingView: ((android.content.Context) -> View)? = null
        var customEmptyView: ((android.content.Context) -> View)? = null
        var customErrorView: ((android.content.Context) -> View)? = null

        /** Configuration flags */
        var hideLoadingState: Boolean = false
        var hideErrorState: Boolean = false

        /** ENG-36737: per-type multi-attachment bubbles vs deprecated single bubbles (null = view default) */
        var enableMultipleAttachments: Boolean? = null

        /** Options callbacks */
        var optionsCallback: ((BaseMessage) -> List<com.cometchat.uikit.core.domain.model.CometChatMessageOption>?)? = null
        var addOptionsCallback: ((BaseMessage) -> List<com.cometchat.uikit.core.domain.model.CometChatMessageOption>)? = null

        /** BubbleViewProvider for custom bubble slots */
        var leadingViewProvider: com.cometchat.uikit.kotlin.presentation.messagelist.BubbleViewProvider? = null

        fun reset() {
            injectedRepository = null
            userToSet = null
            groupToSet = null
            onItemClickMessage = null
            onItemClickPosition = -1
            onItemLongClickMessage = null
            onItemLongClickPosition = -1
            onThreadRepliesClickMessage = null
            onErrorThrowable = null
            onLoadInvoked = false
            onEmptyInvoked = false
            customHeaderView = null
            customFooterView = null
            customLoadingView = null
            customEmptyView = null
            customErrorView = null
            hideLoadingState = false
            hideErrorState = false
            enableMultipleAttachments = null
            optionsCallback = null
            addOptionsCallback = null
            leadingViewProvider = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val messageListView = CometChatMessageList(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        return FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(messageListView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frameLayout = view as FrameLayout
        val messageListView = frameLayout.getChildAt(0) as CometChatMessageList

        // Configure custom views
        customHeaderView?.let { factory ->
            messageListView.setHeaderView(factory(requireContext()))
        }
        customFooterView?.let { factory ->
            messageListView.setFooterView(factory(requireContext()))
        }
        customLoadingView?.let { factory ->
            messageListView.setLoadingStateView(factory(requireContext()))
        }
        customEmptyView?.let { factory ->
            messageListView.setEmptyStateView(factory(requireContext()))
        }
        customErrorView?.let { factory ->
            messageListView.setErrorStateView(factory(requireContext()))
        }

        // Configure visibility flags
        if (hideLoadingState) {
            messageListView.setHideLoadingState(true)
        }
        enableMultipleAttachments?.let { enable ->
            messageListView.setEnableMultipleAttachments(enable)
        }
        if (hideErrorState) {
            messageListView.setHideErrorState(true)
        }

        // Configure options
        optionsCallback?.let { callback ->
            messageListView.setOptions(callback)
        }
        addOptionsCallback?.let { callback ->
            messageListView.addOptions(callback)
        }

        // Configure BubbleViewProvider
        leadingViewProvider?.let { provider ->
            messageListView.setLeadingViewProvider(provider)
        }

        // Wire callbacks for assertion
        messageListView.setOnItemClick { message, position ->
            onItemClickMessage = message
            onItemClickPosition = position
        }
        messageListView.setOnItemLongClick { message, position ->
            onItemLongClickMessage = message
            onItemLongClickPosition = position
            true
        }
        messageListView.setOnThreadRepliesClick { message ->
            onThreadRepliesClickMessage = message
        }
        messageListView.setOnError { throwable ->
            onErrorThrowable = throwable
        }
        messageListView.setOnLoad {
            onLoadInvoked = true
        }
        messageListView.setOnEmpty {
            onEmptyInvoked = true
        }

        // Build ViewModel with injected Repository
        injectedRepository?.let { repo ->
            val viewModel = CometChatMessageListViewModel(
                repository = repo,
                enableListeners = false
            )
            messageListView.setViewModel(viewModel)

            // Configure for user or group
            userToSet?.let { user ->
                messageListView.setUser(user)
            }
            groupToSet?.let { group ->
                messageListView.setGroup(group)
            }
        }
    }
}

/**
 * A fake [MessageListRepository] for instrumented tests.
 * Returns pre-configured messages or errors without any SDK dependency.
 */
class FakeMessageListRepository(
    private val messages: List<BaseMessage> = emptyList(),
    private val shouldFail: Boolean = false,
    private val errorCode: String = "ERR",
    private val errorMessage: String = "Error",
    private val paginationMessages: List<BaseMessage> = emptyList()
) : MessageListRepository {

    private var fetchCount = 0

    override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
        fetchCount++
        return if (shouldFail) {
            Result.failure(CometChatException(errorCode, errorMessage))
        } else {
            Result.success(messages)
        }
    }

    override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        return Result.success(paginationMessages)
    }

    override suspend fun getConversation(id: String, type: String): Result<Conversation> {
        return Result.failure(CometChatException("NOT_IMPL", "Not implemented in test"))
    }

    override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
        val msg = messages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(CometChatException("NOT_FOUND", "Message not found"))
    }

    override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> {
        return Result.success(message)
    }

    override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        val msg = messages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(CometChatException("NOT_FOUND", "Not found"))
    }

    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        val msg = messages.find { it.id == messageId }
        return if (msg != null) Result.success(msg)
        else Result.failure(CometChatException("NOT_FOUND", "Not found"))
    }

    override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
    override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
    override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> {
        return Result.failure(CometChatException("NOT_IMPL", "Not implemented in test"))
    }

    override fun hasMorePreviousMessages(): Boolean = false
    override fun resetRequest() {}

    override fun configureForUser(
        user: User,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {}

    override fun configureForGroup(
        group: Group,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {}

    override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
        return Result.failure(CometChatException("NOT_IMPL", "Not implemented in test"))
    }

    override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        return Result.success(emptyList())
    }

    override fun rebuildRequestFromMessageId(messageId: Long) {}
    override fun getLatestMessageId(): Long = messages.lastOrNull()?.id ?: -1
    override fun setLatestMessageId(messageId: Long) {}
}

/**
 * A fake [MessageListRepository] that never completes — keeps the ViewModel in Loading state.
 */
class LoadingMessageListRepository : MessageListRepository {
    override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun getConversation(id: String, type: String): Result<Conversation> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun getMessage(messageId: Long): Result<BaseMessage> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
    override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
    override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> {
        kotlinx.coroutines.awaitCancellation()
    }
    override fun hasMorePreviousMessages(): Boolean = true
    override fun resetRequest() {}
    override fun configureForUser(
        user: User,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {}
    override fun configureForGroup(
        group: Group,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    ) {}
    override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> {
        kotlinx.coroutines.awaitCancellation()
    }
    override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> {
        kotlinx.coroutines.awaitCancellation()
    }
    override fun rebuildRequestFromMessageId(messageId: Long) {}
    override fun getLatestMessageId(): Long = -1
    override fun setLatestMessageId(messageId: Long) {}
}
