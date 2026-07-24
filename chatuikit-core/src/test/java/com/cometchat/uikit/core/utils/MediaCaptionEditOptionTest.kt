package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for the EDIT message option on media messages (ENG-37005).
 *
 * Media messages (image/video/audio/file) carry their editable text in
 * [MediaMessage.getCaption]; the EDIT option must be offered exactly when a
 * caption exists on the user's own message, and never otherwise.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MediaCaptionEditOptionTest"
 */
class MediaCaptionEditOptionTest : FunSpec({

    val mediaTypes = listOf(
        UIKitConstants.MessageType.IMAGE,
        UIKitConstants.MessageType.VIDEO,
        UIKitConstants.MessageType.AUDIO,
        UIKitConstants.MessageType.FILE
    )

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Default Option Map ====================

    context("default option maps include EDIT for media types") {
        mediaTypes.forEach { type ->
            test("message_$type default options include EDIT") {
                val options = MessageOptionsUtils.getDefaultOptionIds(
                    UIKitConstants.MessageCategory.MESSAGE, type
                )
                options shouldContain UIKitConstants.MessageOption.EDIT
            }
        }
    }

    // ==================== Caption Gating ====================

    context("caption gating in getDefaultMessageOptions") {
        lateinit var cometChatMock: MockedStatic<CometChat>
        val context = mock<Context>()
        val me = User().apply { uid = "me" }
        val someoneElse = User().apply { uid = "other" }

        beforeTest {
            whenever(context.getString(any<Int>())).thenReturn("option")
            cometChatMock = Mockito.mockStatic(CometChat::class.java)
            cometChatMock.`when`<User> { CometChat.getLoggedInUser() }.thenReturn(me)
        }

        afterTest {
            cometChatMock.close()
        }

        fun mediaMessage(type: String, sender: User, caption: String?): MediaMessage =
            MediaMessage("receiver-1", type, UIKitConstants.ReceiverType.USER).apply {
                this.sender = sender
                caption?.let { this.caption = it }
            }

        fun optionIdsFor(message: com.cometchat.chat.models.BaseMessage): List<String> =
            MessageOptionsUtils.getDefaultMessageOptions(
                context = context,
                message = message,
                user = someoneElse,
                group = null
            ).map { it.id }

        mediaTypes.forEach { type ->
            test("my $type message WITH caption offers EDIT") {
                val options = optionIdsFor(mediaMessage(type, me, caption = "hello"))
                println("    → options: $options")
                options shouldContain UIKitConstants.MessageOption.EDIT
            }

            test("my $type message WITHOUT caption does not offer EDIT") {
                val options = optionIdsFor(mediaMessage(type, me, caption = null))
                println("    → options: $options")
                options shouldNotContain UIKitConstants.MessageOption.EDIT
            }
        }

        test("someone else's media message with caption does not offer EDIT") {
            val message = mediaMessage(UIKitConstants.MessageType.IMAGE, someoneElse, caption = "hello")
            optionIdsFor(message) shouldNotContain UIKitConstants.MessageOption.EDIT
        }

        test("my text message still offers EDIT without any caption") {
            val message = TextMessage("receiver-1", "hi", UIKitConstants.ReceiverType.USER).apply {
                sender = me
            }
            optionIdsFor(message) shouldContain UIKitConstants.MessageOption.EDIT
        }
    }
})
