package com.cometchat.uikit.compose.screenshots

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.pinnedmessages.ui.CometChatPinnedMessages
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatPinnedMessages (chatuikit-compose).
 *
 * The screen's ViewModel fetches through MessagesRequest, so the harness scripts the request:
 * mockConstruction(MessagesRequestBuilder) makes every builder chain return a mocked
 * MessagesRequest that serves a fixed queue of pages (same approach as the ViewModel unit tests).
 * CometChat statics are mocked for getLoggedInUser (drives the "You" + outgoing styling).
 *
 * States captured:
 * - Content (own + other messages, "You" header + outgoing bubble for own)
 * - Content dark theme
 * - Empty state (light + dark)
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatPinnedMessagesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatPinnedMessagesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/pinnedmessages"
        )
    )

    private companion object {
        // Fixed epoch (Jan 1, 2025 UTC) so the "name • date" header is deterministic.
        private const val FIXED_TIMESTAMP = 1735689600L
        private const val LOGGED_IN_UID = "logged-in-user"
    }

    private lateinit var cometChatMock: MockedStatic<CometChat>
    private lateinit var builderConstruction: MockedConstruction<MessagesRequest.MessagesRequestBuilder>
    private lateinit var currentRequest: MessagesRequest

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(android.graphics.Color.TRANSPARENT),
                    request = chain.request,
                    dataSource = DataSource.MEMORY_CACHE
                )
            }
        }
        Coil.setImageLoader(
            ImageLoader.Builder(context).components { add(interceptor) }.crossfade(false).build()
        )

        currentRequest = requestServing()
        builderConstruction = Mockito.mockConstruction(
            MessagesRequest.MessagesRequestBuilder::class.java
        ) { builder, _ ->
            whenever(builder.setPinned(any())).thenReturn(builder)
            whenever(builder.setSaved(any())).thenReturn(builder)
            whenever(builder.setLimit(any())).thenReturn(builder)
            whenever(builder.setTypes(any())).thenReturn(builder)
            whenever(builder.setCategories(any())).thenReturn(builder)
            whenever(builder.setUID(any())).thenReturn(builder)
            whenever(builder.setGUID(any())).thenReturn(builder)
            whenever(builder.build()).thenAnswer { currentRequest }
        }

        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        val loggedInUser = User().apply {
            uid = LOGGED_IN_UID
            name = "Logged In User"
        }
        cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)
    }

    @After
    fun tearDown() {
        cometChatMock.close()
        builderConstruction.close()
    }

    // ==================== Helpers ====================

    /** MessagesRequest mock serving the given pages in order, then empty pages forever. */
    private fun requestServing(vararg pages: List<BaseMessage>): MessagesRequest {
        val queue = ArrayDeque(pages.toList())
        val req = mock<MessagesRequest>()
        doAnswer { invocation ->
            val cb = invocation.getArgument<CometChat.CallbackListener<List<BaseMessage>>>(0)
            cb.onSuccess(queue.removeFirstOrNull() ?: emptyList())
            null
        }.whenever(req).fetchNext(any())
        return req
    }

    private fun createUser(uid: String, name: String): User = User().apply {
        this.uid = uid
        this.name = name
    }

    private fun createPinnedMessage(
        id: Long,
        text: String,
        sender: User,
        sentAt: Long = FIXED_TIMESTAMP
    ): TextMessage {
        return TextMessage("peer-1", text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = sentAt
            this.sender = sender
            this.pinnedAt = FIXED_TIMESTAMP
            this.pinnedBy = LOGGED_IN_UID
        }
    }

    /** Own + other pinned messages — exercises "You" + outgoing style vs incoming. */
    private fun pinnedContentPage(): List<BaseMessage> {
        val me = createUser(LOGGED_IN_UID, "Logged In User")
        val alice = createUser("alice", "Alice Smith")
        return listOf(
            createPinnedMessage(1L, "Team standup moved to 10am tomorrow.", alice),
            createPinnedMessage(2L, "Sharing the final launch checklist here.", me),
            createPinnedMessage(3L, "Wifi password for the office: cometchat123", alice)
        )
    }

    private fun createViewModelWithPages(vararg pages: List<BaseMessage>): CometChatPinnedMessagesViewModel {
        // Two reload cycles are served: one for the manual pre-configure below (state is Content
        // before composition) and one for the composable's own LaunchedEffect configure.
        val repeated = pages.toList() + listOf(emptyList()) + pages.toList()
        currentRequest = requestServing(*repeated.toTypedArray())
        val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
        vm.configure(uid = "peer-1", guid = null)
        return vm
    }

    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        ShadowLooper.idleMainLooper()
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== UI States ====================

    @Test
    fun stateContent() {
        val vm = createViewModelWithPages(pinnedContentPage())
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatPinnedMessages(
                    user = createUser("peer-1", "Alice Smith"),
                    viewModel = vm
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        val vm = createViewModelWithPages(pinnedContentPage())
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatPinnedMessages(
                    user = createUser("peer-1", "Alice Smith"),
                    viewModel = vm
                )
            }
        }
    }

    @Test
    fun stateEmpty() {
        val vm = createViewModelWithPages()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatPinnedMessages(
                    user = createUser("peer-1", "Alice Smith"),
                    viewModel = vm
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        val vm = createViewModelWithPages()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatPinnedMessages(
                    user = createUser("peer-1", "Alice Smith"),
                    viewModel = vm
                )
            }
        }
    }

    @Test
    fun stateSingleOwnMessage() {
        val me = createUser(LOGGED_IN_UID, "Logged In User")
        val vm = createViewModelWithPages(
            listOf(createPinnedMessage(1L, "Pinned by me — should render outgoing with \"You\".", me))
        )
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatPinnedMessages(
                    user = createUser("peer-1", "Alice Smith"),
                    viewModel = vm
                )
            }
        }
    }
}
