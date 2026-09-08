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
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.savedmessages.ui.CometChatSavedMessages
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.viewmodel.CometChatSavedMessagesViewModel
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
 * Roborazzi screenshot tests for CometChatSavedMessages (chatuikit-compose).
 *
 * Same harness as CometChatPinnedMessagesScreenshotTest: mockConstruction(MessagesRequestBuilder)
 * scripts the saved-messages fetch, CometChat statics are mocked for getLoggedInUser (drives the
 * peer resolution of 1-1 rows).
 *
 * States captured:
 * - Content (1-1 rows + group row)
 * - Content dark theme
 * - Empty state (light + dark)
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatSavedMessagesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatSavedMessagesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/savedmessages"
        )
    )

    private companion object {
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

    /** A saved 1-1 message from [sender]; receiver is the logged-in user. */
    private fun createSavedUserMessage(id: Long, text: String, sender: User): TextMessage {
        return TextMessage(LOGGED_IN_UID, text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = FIXED_TIMESTAMP
            this.sender = sender
            this.receiver = createUser(LOGGED_IN_UID, "Logged In User")
            this.savedAt = FIXED_TIMESTAMP
        }
    }

    /** A saved group message; the row shows the group as the conversation context. */
    private fun createSavedGroupMessage(id: Long, text: String, sender: User, group: Group): TextMessage {
        return TextMessage(group.guid, text, CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            this.id = id
            this.sentAt = FIXED_TIMESTAMP
            this.sender = sender
            this.receiver = group
            this.savedAt = FIXED_TIMESTAMP
        }
    }

    private fun savedContentPage(): List<BaseMessage> {
        val alice = createUser("alice", "Alice Smith")
        val bob = createUser("bob", "Bob Johnson")
        val group = Group().apply {
            guid = "team-launch"
            name = "Launch Team"
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC
        }
        return listOf(
            createSavedUserMessage(1L, "Here is the address for Friday's dinner.", alice),
            createSavedGroupMessage(2L, "Release notes draft is in the shared doc.", bob, group),
            createSavedUserMessage(3L, "Flight lands at 6:45pm, terminal 2.", bob)
        )
    }

    private fun createViewModelWithPages(vararg pages: List<BaseMessage>): CometChatSavedMessagesViewModel {
        // Two reload cycles: one for the manual pre-reload below, one for the composable's own
        // LaunchedEffect reload.
        val repeated = pages.toList() + listOf(emptyList()) + pages.toList()
        currentRequest = requestServing(*repeated.toTypedArray())
        val vm = CometChatSavedMessagesViewModel(enableListeners = false)
        vm.reload()
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
        val vm = createViewModelWithPages(savedContentPage())
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSavedMessages(viewModel = vm)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        val vm = createViewModelWithPages(savedContentPage())
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatSavedMessages(viewModel = vm)
            }
        }
    }

    @Test
    fun stateEmpty() {
        val vm = createViewModelWithPages()
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSavedMessages(viewModel = vm)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        val vm = createViewModelWithPages()
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatSavedMessages(viewModel = vm)
            }
        }
    }
}
