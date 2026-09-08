package com.cometchat.uikit.kotlin.presentation.savedmessages

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
 * Roborazzi screenshot tests for CometChatSavedMessages (chatuikit-kotlin View).
 *
 * Same harness as CometChatPinnedMessagesScreenshotTest: the view's internal ViewModel fetches
 * through a scripted MessagesRequest (mockConstruction on the builder); CometChat statics are
 * mocked for getLoggedInUser (drives the 1-1 row peer resolution). The saved list reloads on
 * attach, so the scripted queue is consumed when the view enters the window.
 *
 * States captured:
 * - Content (1-1 rows + group row)
 * - Content dark theme
 * - Empty state (light + dark)
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatSavedMessagesScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatSavedMessagesScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/savedmessages"
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

    /** A saved 1-1 message from [sender]; receiver is the logged-in user. */
    private fun createSavedUserMessage(id: Long, text: String, sender: User): TextMessage {
        return TextMessage(LOGGED_IN_UID, text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = FIXED_TIMESTAMP
            this.sender = sender
            this.receiver = createRealUser(LOGGED_IN_UID, "Logged In User")
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
        val alice = createRealUser("alice", "Alice Smith")
        val bob = createRealUser("bob", "Bob Johnson")
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

    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatSavedMessages) {
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
        currentRequest = requestServing(savedContentPage())
        launchAndCapture { activity -> CometChatSavedMessages(activity) }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        currentRequest = requestServing(savedContentPage())
        launchAndCapture { activity -> CometChatSavedMessages(activity) }
    }

    @Test
    fun stateEmpty() {
        currentRequest = requestServing()
        launchAndCapture { activity -> CometChatSavedMessages(activity) }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        currentRequest = requestServing()
        launchAndCapture { activity -> CometChatSavedMessages(activity) }
    }
}
