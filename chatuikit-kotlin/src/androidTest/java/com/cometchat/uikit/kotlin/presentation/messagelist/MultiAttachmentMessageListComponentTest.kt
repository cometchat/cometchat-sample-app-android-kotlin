package com.cometchat.uikit.kotlin.presentation.messagelist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filesbubble.CometChatFilesBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagesbubble.CometChatImagesBubble
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ENG-36737 — Layer 1 (instrumented) component tests for multi-attachment rendering in the Views
 * [com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList]:
 * fake repository → real ViewModel → real message list on device, per TEST-SETUP.md.
 *
 * Covers what the JVM layers can't: that a multi-attachment [MediaMessage] actually renders the
 * NEW per-type bubble inside the RecyclerView when `enableMultipleAttachments` is on (and the
 * DEPRECATED single bubble when off), and that the files card stack expands on the real
 * "+N more ▾" tap.
 *
 * Run: ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.MultiAttachmentMessageListComponentTest
 */
@RunWith(AndroidJUnit4::class)
class MultiAttachmentMessageListComponentTest {

    @Before
    fun setup() {
        // MessageAdapter resolves alignment via CometChat.getLoggedInUser().
        MessageListTestSdkHelper.ensureInitialized()
        MessageListHostFragment.reset()
    }

    @After
    fun tearDown() {
        MessageListHostFragment.reset()
    }

    // ==================== Helpers ====================

    private fun createUser(uid: String, name: String): User = User().apply {
        this.uid = uid
        this.name = name
        this.status = CometChatConstants.USER_STATUS_ONLINE
    }

    private fun attachment(name: String, mime: String) = Attachment().apply {
        fileUrl = "https://cdn.example.com/$name"
        fileName = name
        fileMimeType = mime
        fileExtension = name.substringAfterLast('.', "")
        fileSize = 3_200_000
    }

    private fun mediaMessage(
        id: Long,
        type: String,
        attachments: List<Attachment>,
        batchId: String? = null,
        sentAt: Long = 1_700_000_000L + id
    ): MediaMessage {
        val sender = createUser("user-1", "Alice")
        return MediaMessage("user-1", type, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sender = sender
            this.receiverUid = MessageListTestSdkHelper.LOGGED_IN_USER_UID
            this.sentAt = sentAt
            this.category = CometChatConstants.CATEGORY_MESSAGE
            this.attachment = attachments.first()
            this.attachments = attachments
            batchId?.let {
                this.metadata = JSONObject().put(UIKitConstants.JSONKeys.BATCH_ID, it)
            }
        }
    }

    private fun launchWithMessages(
        messages: List<BaseMessage>,
        enableMultipleAttachments: Boolean
    ): FragmentScenario<MessageListHostFragment> {
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser("user-1", "Alice")
        MessageListHostFragment.enableMultipleAttachments = enableMultipleAttachments
        return launchFragmentInContainer(themeResId = R.style.CometChatTheme_DayNight)
    }

    // ==================== Flag routing in the real list ====================

    @Test
    fun multiImageMessage_flagOn_rendersImagesBubbleInList() {
        val message = mediaMessage(
            id = 1L,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE,
            attachments = listOf(
                attachment("a.jpg", "image/jpeg"),
                attachment("b.jpg", "image/jpeg"),
                attachment("c.jpg", "image/jpeg")
            )
        )

        val scenario = launchWithMessages(listOf(message), enableMultipleAttachments = true)

        onView(isAssignableFrom(CometChatImagesBubble::class.java)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun multiImageMessage_flagOff_rendersDeprecatedImageBubble() {
        val message = mediaMessage(
            id = 1L,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE,
            attachments = listOf(
                attachment("a.jpg", "image/jpeg"),
                attachment("b.jpg", "image/jpeg")
            )
        )

        val scenario = launchWithMessages(listOf(message), enableMultipleAttachments = false)

        onView(isAssignableFrom(CometChatImageBubble::class.java)).check(matches(isDisplayed()))
        onView(isAssignableFrom(CometChatImagesBubble::class.java)).check(doesNotExist())
        scenario.close()
    }

    @Test
    fun fileMessage_flagOff_rendersDeprecatedFileBubble() {
        val message = mediaMessage(
            id = 1L,
            type = CometChatConstants.MESSAGE_TYPE_FILE,
            attachments = listOf(attachment("report.pdf", "application/pdf"))
        )

        val scenario = launchWithMessages(listOf(message), enableMultipleAttachments = false)

        onView(isAssignableFrom(CometChatFileBubble::class.java)).check(matches(isDisplayed()))
        onView(isAssignableFrom(CometChatFilesBubble::class.java)).check(doesNotExist())
        scenario.close()
    }

    // ==================== Files expand/collapse interaction ====================

    @Test
    fun filesMessage_collapsesToThree_andExpandsOnMoreTap() {
        val message = mediaMessage(
            id = 1L,
            type = CometChatConstants.MESSAGE_TYPE_FILE,
            attachments = listOf(
                attachment("report.pdf", "application/pdf"),
                attachment("notes.docx", "application/msword"),
                attachment("data.xlsx", "application/vnd.ms-excel"),
                attachment("slides.pptx", "application/vnd.ms-powerpoint"),
                attachment("archive.zip", "application/zip")
            )
        )

        val scenario = launchWithMessages(listOf(message), enableMultipleAttachments = true)

        // Collapsed: first three cards visible, the rest hidden behind the toggle.
        onView(withText("report.pdf")).check(matches(isDisplayed()))
        onView(withText("data.xlsx")).check(matches(isDisplayed()))
        onView(withText("archive.zip")).check(doesNotExist())

        onView(withSubstring("+2 more")).perform(click())

        onView(withText("slides.pptx")).check(matches(isDisplayed()))
        onView(withText("archive.zip")).check(matches(isDisplayed()))

        // And back.
        onView(withSubstring("Show less")).perform(click())
        onView(withText("archive.zip")).check(doesNotExist())

        scenario.close()
    }

    // ==================== Batched (split) send rendering ====================

    @Test
    fun batchedMessages_renderOnePerTypeBubbleEach() {
        val batchId = "batch-e2e-1"
        val images = mediaMessage(
            id = 1L,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE,
            attachments = listOf(attachment("a.jpg", "image/jpeg"), attachment("b.jpg", "image/jpeg")),
            batchId = batchId,
            sentAt = 1_700_000_001L
        )
        val files = mediaMessage(
            id = 2L,
            type = CometChatConstants.MESSAGE_TYPE_FILE,
            attachments = listOf(attachment("report.pdf", "application/pdf")),
            batchId = batchId,
            sentAt = 1_700_000_002L
        )

        val scenario = launchWithMessages(listOf(images, files), enableMultipleAttachments = true)

        onView(isAssignableFrom(CometChatImagesBubble::class.java)).check(matches(isDisplayed()))
        onView(isAssignableFrom(CometChatFilesBubble::class.java)).check(matches(isDisplayed()))
        onView(withText("report.pdf")).check(matches(isDisplayed()))
        scenario.close()
    }
}
