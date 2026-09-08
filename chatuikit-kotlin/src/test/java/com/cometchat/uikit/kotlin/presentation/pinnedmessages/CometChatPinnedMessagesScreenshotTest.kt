package com.cometchat.uikit.kotlin.presentation.pinnedmessages

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatPinnedMessages (chatuikit-kotlin View).
 *
 * The view builds its ViewModel internally, so the fetch path is scripted at the SDK boundary:
 * mockConstruction(MessagesRequestBuilder) makes every builder chain return a mocked
 * MessagesRequest serving a fixed queue of pages (same harness as the core ViewModel unit tests).
 * CometChat statics are mocked for getLoggedInUser (drives "You" + the outgoing bubble styling).
 *
 * States captured:
 * - Content (own + other pinned messages)
 * - Content dark theme
 * - Empty state (light + dark)
 * - Group conversation content
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatPinnedMessagesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatPinnedMessagesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/pinnedmessages"
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

    private fun createRealUser(uid: String, name: String): User = User().apply {
        this.uid = uid
        this.name = name
    }

    private fun createPinnedMessage(
        id: Long,
        text: String,
        sender: User,
        receiverId: String = "peer-1",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): TextMessage {
        return TextMessage(receiverId, text, receiverType).apply {
            this.id = id
            this.sentAt = FIXED_TIMESTAMP
            this.sender = sender
            this.pinnedAt = FIXED_TIMESTAMP
            this.pinnedBy = LOGGED_IN_UID
        }
    }

    /** Own + other pinned messages — exercises "You" + outgoing style vs incoming. */
    private fun pinnedContentPage(): List<BaseMessage> {
        val me = createRealUser(LOGGED_IN_UID, "Logged In User")
        val alice = createRealUser("alice", "Alice Smith")
        return listOf(
            createPinnedMessage(1L, "Team standup moved to 10am tomorrow.", alice),
            createPinnedMessage(2L, "Sharing the final launch checklist here.", me),
            createPinnedMessage(3L, "Wifi password for the office: cometchat123", alice)
        )
    }

    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatPinnedMessages) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            ShadowLooper.idleMainLooper()
        }
        ShadowLooper.idleMainLooper()
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    /** Robolectric's software canvas can't clip avatar outlines; force full rounding instead. */
    private fun fixAvatarCircularRendering(root: View) {
        if (root is CometChatAvatar) {
            root.radius = Float.MAX_VALUE
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                fixAvatarCircularRendering(root.getChildAt(i))
            }
        }
    }

    // ==================== UI States ====================

    @Test
    fun stateContent() {
        currentRequest = requestServing(pinnedContentPage())
        launchAndCapture { activity ->
            CometChatPinnedMessages(activity).apply {
                setUser(createRealUser("peer-1", "Alice Smith"))
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        currentRequest = requestServing(pinnedContentPage())
        launchAndCapture { activity ->
            CometChatPinnedMessages(activity).apply {
                setUser(createRealUser("peer-1", "Alice Smith"))
            }
        }
    }

    @Test
    fun stateEmpty() {
        currentRequest = requestServing()
        launchAndCapture { activity ->
            CometChatPinnedMessages(activity).apply {
                setUser(createRealUser("peer-1", "Alice Smith"))
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        currentRequest = requestServing()
        launchAndCapture { activity ->
            CometChatPinnedMessages(activity).apply {
                setUser(createRealUser("peer-1", "Alice Smith"))
            }
        }
    }

    @Test
    fun stateGroupContent() {
        val bob = createRealUser("bob", "Bob Johnson")
        val me = createRealUser(LOGGED_IN_UID, "Logged In User")
        currentRequest = requestServing(
            listOf(
                createPinnedMessage(
                    1L, "Launch is a go for Monday!", bob,
                    receiverId = "team-launch", receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                ),
                createPinnedMessage(
                    2L, "Design review notes pinned for reference.", me,
                    receiverId = "team-launch", receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
            )
        )
        launchAndCapture { activity ->
            CometChatPinnedMessages(activity).apply {
                setGroup(Group().apply {
                    guid = "team-launch"
                    name = "Launch Team"
                    groupType = CometChatConstants.GROUP_TYPE_PUBLIC
                })
            }
        }
    }
}
