package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * ENG-37017 — [CometChatMessageList.gotoMessage] navigation (Advanced Search media results).
 *
 * The bug: `gotoMessage` only stored the target id, which is consumed exclusively during
 * `setUser`/`setGroup` initialization. Any call made *after* the list was initialized — exactly
 * what tapping a search result for the already-open conversation does — was a silent no-op.
 * The fix adds `navigateToMessage`: post-init calls now either scroll directly (message already
 * loaded) or ask the ViewModel to fetch a message window around the target.
 *
 * The ViewModel is real (mocked repository, listeners off) and wired through the public
 * `setViewModel` seam; assertions are on the VM's list content — a successful navigation
 * replaces the list with the window around the target, a pre-fix no-op leaves it untouched.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*MessageListGoToMessageViewTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MessageListGoToMessageViewTest {

    // Row binding resolves message alignment via CometChat.getLoggedInUser() — mocked so the
    // test never needs CometChat.init (null user ⇒ everything renders incoming/LEFT).
    private lateinit var cometChatMock: MockedStatic<CometChat>

    @Before
    fun setUp() {
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
    }

    @After
    fun tearDown() {
        cometChatMock.close()
    }

    private fun user(uid: String = "test-user"): User = User().apply {
        this.uid = uid
        name = "Test User"
    }

    private fun message(id: Long, text: String = "msg-$id"): BaseMessage =
        TextMessage("test-user", text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            sender = user()
            sentAt = 1_700_000_000L + id
        }

    /** Repository stubbed so the initial fetch returns [initial] and a goToMessage(42) succeeds. */
    private fun repositoryWithWindowAround42(initial: List<BaseMessage>): MessageListRepository {
        val target = message(42L)
        val repository = mock<MessageListRepository> {
            onBlocking { fetchPreviousMessages() }.thenReturn(Result.success(initial))
            on { hasMorePreviousMessages() }.thenReturn(true)
            onBlocking { markAsRead(any()) }.thenReturn(Result.success(Unit))
            onBlocking { markAsDelivered(any()) }.thenReturn(Result.success(Unit))
            onBlocking { getMessage(42L) }.thenReturn(Result.success(target))
            onBlocking { fetchSurroundingMessages(42L) }.thenReturn(
                Result.success(
                    SurroundingMessagesResult(
                        olderMessages = listOf(message(40L), message(41L)),
                        targetMessage = target,
                        newerMessages = listOf(message(43L)),
                        hasMorePrevious = true,
                        hasMoreNext = false
                    )
                )
            )
        }
        return repository
    }

    private fun withList(
        repository: MessageListRepository,
        block: (CometChatMessageList, CometChatMessageListViewModel) -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val viewModel = CometChatMessageListViewModel(repository = repository, enableListeners = false)
            val list = CometChatMessageList(activity)
            activity.setContentView(list)
            list.setViewModel(viewModel)
            block(list, viewModel)
        }
        scenario.close()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun `gotoMessage AFTER initialization fetches the window around the target`() {
        val repository = repositoryWithWindowAround42(initial = listOf(message(1L), message(2L), message(3L)))
        withList(repository) { list, vm ->
            list.setUser(user())
            idle()
            // Sanity: initialized on the regular fetch, target not loaded
            assertTrue(vm.getItems().none { it.id == 42L })

            // The QA repro: tapping a search result for the already-open conversation.
            // Pre-fix this only wrote goToMessageId (read nowhere after init) — a silent no-op.
            list.gotoMessage(42L)
            idle()

            assertEquals(listOf(40L, 41L, 42L, 43L), vm.getItems().map { it.id })
            assertTrue(vm.uiState.value is MessageListUIState.Loaded)
        }
    }

    @Test
    fun `gotoMessage BEFORE setUser is honored during initialization`() {
        val repository = repositoryWithWindowAround42(initial = emptyList())
        withList(repository) { list, vm ->
            // The Advanced Search entry path: the activity sets the target, then the user
            list.gotoMessage(42L)
            list.setUser(user())
            idle()

            assertEquals(listOf(40L, 41L, 42L, 43L), vm.getItems().map { it.id })
            assertTrue(vm.uiState.value is MessageListUIState.Loaded)
        }
    }

    @Test
    fun `gotoMessage with id 0 stays a no-op`() {
        val repository = repositoryWithWindowAround42(initial = listOf(message(1L)))
        withList(repository) { list, vm ->
            list.setUser(user())
            idle()

            list.gotoMessage(0L)
            idle()

            verifyBlocking(repository, never()) { getMessage(anyOrNull()) }
            assertEquals(listOf(1L), vm.getItems().map { it.id })
        }
    }
}
