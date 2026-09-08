package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A meeting message (category `custom`, type `meeting`) long-presses like any other custom message.
 *
 * v6 briefly gave the meet bubble a single affordance — the Join button — and suppressed its
 * options and its long press entirely. That dropped behaviour v5 shipped: there the conference-call
 * template took `getImageMessageOptions`, i.e. the common option set (reply, reply in thread,
 * delete, mark unread, info, message privately). This restores that set and adds v6's own
 * additions — Pin/Save and the thread-subscription toggle.
 *
 * Report is deliberately excluded: v5 added it for category `message` only, so a meeting message
 * never offered it, and strict parity keeps it that way. That is the single difference from the
 * custom-message list stickers and polls use.
 *
 * [MessageOptionsUtils.isMeetingMessage] still routes the type to that list and still drives bubble
 * layout (minimal slots) — what it no longer does is gate the long press in either kit.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MeetingMessageOptionsTest"
 */
class MeetingMessageOptionsTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    lateinit var mockContext: Context
    lateinit var cometChatMock: MockedStatic<CometChat>

    fun customMessage(type: String): CustomMessage {
        // Build every nested mock BEFORE the stubbing calls — creating one inside `whenever(...)`
        // trips Mockito's UnfinishedStubbingException.
        val sender = MockFactory.createUser(uid = "other-user")
        val message = mock<CustomMessage>()
        whenever(message.id).thenReturn(42L)
        whenever(message.category).thenReturn(UIKitConstants.MessageCategory.CUSTOM)
        whenever(message.type).thenReturn(type)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        return message
    }

    beforeTest {
        mockContext = mock()
        whenever(mockContext.getString(org.mockito.kotlin.any())).thenReturn("label")
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
    }

    afterTest { cometChatMock.close() }

    val meetingOptionIds = {
        MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CUSTOM,
            UIKitConstants.MessageType.MEETING
        )
    }

    test("a meeting message carries the v5 option set again") {
        meetingOptionIds().shouldContainAll(
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.REPLY_IN_THREAD,
            UIKitConstants.MessageOption.DELETE,
            UIKitConstants.MessageOption.MARK_AS_UNREAD,
            UIKitConstants.MessageOption.MESSAGE_INFORMATION,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY
        )
    }

    test("a meeting message also carries v6's pin, save and thread-subscription options") {
        meetingOptionIds().shouldContainAll(
            UIKitConstants.MessageOption.PIN,
            UIKitConstants.MessageOption.UNPIN,
            UIKitConstants.MessageOption.SAVE,
            UIKitConstants.MessageOption.UNSAVE,
            UIKitConstants.MessageOption.THREAD_SUBSCRIPTION
        )
    }

    test("a meeting message offers no copy or edit — it is a custom message, not text") {
        meetingOptionIds() shouldNotContain UIKitConstants.MessageOption.COPY
        meetingOptionIds() shouldNotContain UIKitConstants.MessageOption.EDIT
    }

    test("it resolves to a non-empty action sheet") {
        val options = MessageOptionsUtils.getDefaultMessageOptions(
            mockContext,
            customMessage(UIKitConstants.MessageType.MEETING),
            user = null,
            group = null
        )
        options.shouldNotBeEmpty()
        options.map { it.id } shouldContain UIKitConstants.MessageOption.REPLY
    }

    test("it offers no Report — strict v5 parity, where Report was category `message` only") {
        meetingOptionIds() shouldNotContain UIKitConstants.MessageOption.REPORT
    }

    test("Report is the ONLY thing separating it from any other custom type") {
        val sticker = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CUSTOM,
            UIKitConstants.MessageType.EXTENSION_STICKER
        )
        sticker shouldContain UIKitConstants.MessageOption.REPORT
        meetingOptionIds() shouldBe sticker.filterNot { it == UIKitConstants.MessageOption.REPORT }
    }

    test("isMeetingMessage still identifies the type — it now drives bubble layout only") {
        MessageOptionsUtils.isMeetingMessage("CUSTOM", "MEETING") shouldBe true
        MessageOptionsUtils.isMeetingMessage(customMessage(UIKitConstants.MessageType.MEETING)) shouldBe true
        MessageOptionsUtils.isMeetingMessage(customMessage(UIKitConstants.MessageType.EXTENSION_STICKER)) shouldBe false
        MessageOptionsUtils.isMeetingMessage(
            UIKitConstants.MessageCategory.MESSAGE,
            UIKitConstants.MessageType.MEETING
        ) shouldBe false
        MessageOptionsUtils.isMeetingMessage(null, null) shouldBe false
        MessageOptionsUtils.isMeetingMessage(null) shouldBe false
    }
})
