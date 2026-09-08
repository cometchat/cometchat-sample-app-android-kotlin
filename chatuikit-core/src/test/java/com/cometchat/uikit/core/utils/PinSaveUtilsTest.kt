package com.cometchat.uikit.core.utils

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.mockito.MockedStatic
import org.mockito.Mockito

/**
 * [PinSaveUtils] — the pin/save rules shared by the message list, thread header and panels.
 *
 * A. carryPinSaveForward: a non-pin/save replacement keeps the loaded 📌/🔖 state (ENG-38915/38916)
 * B. classifyFailure: a limit error is reported as one even without `errorParams.limit`
 *    (ENG-38914/38918), resolving the cap from the error, then app settings, then `null`
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*PinSaveUtilsTest"
 */
class PinSaveUtilsTest : FunSpec({

    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    fun message(id: Long, text: String = "msg-$id", pinnedAt: Long = 0L, pinnedBy: String? = null, savedAt: Long = 0L) =
        TextMessage("peer-1", text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = 1735689600L
            this.sender = User().apply { uid = "peer-1"; name = "Peer" }
            this.pinnedAt = pinnedAt
            this.pinnedBy = pinnedBy
            this.savedAt = savedAt
        }

    fun exception(code: String, params: Map<String, Any>? = null) =
        CometChatException(code, "message for $code", null, params)

    beforeTest {
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
    }

    afterTest {
        cometChatMock.close()
        logMock.close()
    }

    // ==================== A. carryPinSaveForward ====================

    test("an edit payload without savedAt keeps the loaded savedAt") {
        val loaded = message(1L, savedAt = 555L)
        val edited = message(1L, text = "edited")

        val merged = PinSaveUtils.carryPinSaveForward(loaded, edited)

        merged.savedAt shouldBe 555L
        merged.isSaved shouldBe true
        (merged as TextMessage).text shouldBe "edited"
    }

    test("an edit payload without pin fields keeps pinnedAt AND pinnedBy") {
        val loaded = message(1L, pinnedAt = 111L, pinnedBy = "admin")
        val edited = message(1L, text = "edited")

        val merged = PinSaveUtils.carryPinSaveForward(loaded, edited)

        merged.pinnedAt shouldBe 111L
        merged.pinnedBy shouldBe "admin"
    }

    test("the incoming object is never mutated — a clone carries the state") {
        val loaded = message(1L, savedAt = 555L)
        val edited = message(1L, text = "edited")

        val merged = PinSaveUtils.carryPinSaveForward(loaded, edited)

        merged shouldNotBeSameInstanceAs edited
        edited.isSaved shouldBe false
    }

    test("nothing to carry returns the incoming instance untouched") {
        val loaded = message(1L)
        val edited = message(1L, text = "edited")

        PinSaveUtils.carryPinSaveForward(loaded, edited) shouldBeSameInstanceAs edited
    }

    test("an incoming payload that already carries the state is trusted (server value wins)") {
        val loaded = message(1L, pinnedAt = 111L, pinnedBy = "admin", savedAt = 555L)
        val fresh = message(1L, pinnedAt = 222L, pinnedBy = "peer-1", savedAt = 777L)

        val merged = PinSaveUtils.carryPinSaveForward(loaded, fresh)

        merged shouldBeSameInstanceAs fresh
        merged.pinnedAt shouldBe 222L
        merged.savedAt shouldBe 777L
    }

    test("a different message or no loaded copy is a no-op") {
        val other = message(2L, savedAt = 555L)
        val incoming = message(1L)

        PinSaveUtils.carryPinSaveForward(other, incoming) shouldBeSameInstanceAs incoming
        PinSaveUtils.carryPinSaveForward(null, incoming) shouldBeSameInstanceAs incoming
    }

    // ==================== B. classifyFailure ====================

    test("pin limit with errorParams.limit → LimitReached(PINNED, limit from error)") {
        val e = exception(UIKitConstants.PinSaveErrorCodes.PINNED_MESSAGES_LIMIT_EXCEEDED, mapOf("limit" to 5))

        PinSaveUtils.classifyFailure(e) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.PINNED_MESSAGES, 5)
    }

    test("pin limit WITHOUT errorParams → cap resolved from app settings") {
        cometChatMock.`when`<Int> { CometChat.getPinMessageLimit() }.thenReturn(10)
        val e = exception(UIKitConstants.PinSaveErrorCodes.PINNED_MESSAGES_LIMIT_EXCEEDED)

        PinSaveUtils.classifyFailure(e) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.PINNED_MESSAGES, 10)
    }

    test("save limit with neither errorParams nor a served setting → LimitReached(SAVED, null), never Other") {
        cometChatMock.`when`<Int> { CometChat.getSaveMessageLimit() }.thenReturn(-1)
        val e = exception(UIKitConstants.PinSaveErrorCodes.SAVED_MESSAGES_LIMIT_EXCEEDED)

        PinSaveUtils.classifyFailure(e) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.SAVED_MESSAGES, null)
    }

    test("a non-numeric or non-positive errorParams.limit is ignored in favour of settings") {
        cometChatMock.`when`<Int> { CometChat.getSaveMessageLimit() }.thenReturn(7)
        val bogus = exception(UIKitConstants.PinSaveErrorCodes.SAVED_MESSAGES_LIMIT_EXCEEDED, mapOf("limit" to "five"))
        val zero = exception(UIKitConstants.PinSaveErrorCodes.SAVED_MESSAGES_LIMIT_EXCEEDED, mapOf("limit" to 0))

        PinSaveUtils.classifyFailure(bogus) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.SAVED_MESSAGES, 7)
        PinSaveUtils.classifyFailure(zero) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.SAVED_MESSAGES, 7)
    }

    test("pin limit without errorParams but phrased in the message → cap from text, before settings (React parity)") {
        cometChatMock.`when`<Int> { CometChat.getPinMessageLimit() }.thenReturn(10)
        val e = CometChatException(
            UIKitConstants.PinSaveErrorCodes.PINNED_MESSAGES_LIMIT_EXCEEDED,
            "Pinned messages limit of 5 reached for this conversation"
        )

        PinSaveUtils.classifyFailure(e) shouldBe
            PinSaveUtils.Failure.LimitReached(PinSaveUtils.LimitScope.PINNED_MESSAGES, 5)
    }

    test("limitFromErrorText: trailing number only when plausible; ids/timestamps are never a cap") {
        PinSaveUtils.limitFromErrorText(CometChatException("X", "You can save up to 3.")) shouldBe 3
        PinSaveUtils.limitFromErrorText(CometChatException("X", "Message 4009 not found")) shouldBe null
        PinSaveUtils.limitFromErrorText(CometChatException("X", "failed at 1786350074")) shouldBe null
        PinSaveUtils.limitFromErrorText(CometChatException("X", "Limit exceeded", "limit of 8")) shouldBe 8
        PinSaveUtils.limitFromErrorText(null) shouldBe null
    }

    test("permission denial → PermissionDenied") {
        PinSaveUtils.classifyFailure(exception(UIKitConstants.PinSaveErrorCodes.PERMISSION_DENIED)) shouldBe
            PinSaveUtils.Failure.PermissionDenied
    }

    test("anything else (or no exception) → Other carrying the code") {
        PinSaveUtils.classifyFailure(exception("ERR_SOMETHING")) shouldBe PinSaveUtils.Failure.Other("ERR_SOMETHING")
        PinSaveUtils.classifyFailure(null) shouldBe PinSaveUtils.Failure.Other(null)
    }
})
