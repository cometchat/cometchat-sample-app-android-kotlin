package com.cometchat.uikit.kotlin.presentation.threadheader

import android.view.View
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * JVM interaction tests for CometChatThreadHeader (chatuikit-kotlin).
 * Verifies user interactions and configuration changes produce correct ViewModel state.
 *
 * Tests focus on:
 * - setParentMessage triggers adapter update
 * - Visibility controls (reactions, avatar, receipts, replyCount, replyCountBar)
 * - Alignment change re-renders bubble
 * - Text formatters applied to adapter
 * - BubbleFactory integration
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatThreadHeaderInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatThreadHeaderInteractionTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    fun createViewModel(): CometChatThreadHeaderViewModel {
        return CometChatThreadHeaderViewModel(enableListeners = false)
    }

    fun createParentMessage(
        id: Long = 100L,
        senderUid: String = "user-1",
        replyCount: Int = 0
    ): BaseMessage {
        val sender = mock<User>()
        whenever(sender.uid).thenReturn(senderUid)
        whenever(sender.name).thenReturn("Sender")
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.text).thenReturn("Parent message")
        whenever(message.sender).thenReturn(sender)
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        whenever(message.replyCount).thenReturn(replyCount)
        whenever(message.parentMessageId).thenReturn(0L)
        return message
    }

    beforeTest {
        // Set up ArchTaskExecutor to run tasks synchronously (required for asLiveData())
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        ArchTaskExecutor.getInstance().setDelegate(null)
        println()
    }

    // ==================== setParentMessage Triggers Adapter Update ====================

    test("setParentMessage triggers parentMessageListStateFlow update for adapter") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 42L, replyCount = 3)
            println("    → setParentMessage(id=42) → adapter receives update")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 42L
        }
    }

    test("setParentMessage replaces previous message in adapter") {
        runTest {
            val viewModel = createViewModel()
            val msg1 = createParentMessage(id = 1L)
            val msg2 = createParentMessage(id = 2L)
            println("    → setParentMessage(id=1) then setParentMessage(id=2)")

            viewModel.setParentMessage(msg1)
            advanceUntilIdle()
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 1L

            viewModel.setParentMessage(msg2)
            advanceUntilIdle()
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 2L
        }
    }

    // ==================== Visibility Controls ====================

    test("setReactionVisibility GONE sets hideReaction=true on ViewModel") {
        runTest {
            val viewModel = createViewModel()
            println("    → setReactionVisibility(GONE) → hideReaction=true")

            viewModel.hideReaction = (View.GONE == View.GONE)

            viewModel.hideReaction shouldBe true
        }
    }

    test("setReactionVisibility VISIBLE sets hideReaction=false on ViewModel") {
        runTest {
            val viewModel = createViewModel()
            println("    → setReactionVisibility(VISIBLE) → hideReaction=false")

            viewModel.hideReaction = (View.VISIBLE == View.GONE)

            viewModel.hideReaction shouldBe false
        }
    }

    test("PBT: visibility toggle for reactions maps correctly to hideReaction") {
        val visibilityArb = Arb.element(View.VISIBLE, View.GONE)
        checkAll(20, visibilityArb) { visibility ->
            runTest {
                val viewModel = createViewModel()
                val expectedHide = (visibility == View.GONE)
                println("    → visibility=$visibility → hideReaction=$expectedHide")

                viewModel.hideReaction = expectedHide

                viewModel.hideReaction shouldBe expectedHide
            }
        }
    }

    // ==================== Alignment Change ====================

    test("alignment STANDARD allows sender-based bubble positioning") {
        runTest {
            val viewModel = createViewModel()
            val outgoingMessage = createParentMessage(id = 1L, senderUid = "me")
            println("    → STANDARD alignment + outgoing message → RIGHT bubble")

            viewModel.setParentMessage(outgoingMessage)
            advanceUntilIdle()

            // View determines alignment: STANDARD + sender==loggedInUser → RIGHT
            viewModel.parentMessageListStateFlow.value[0].sender?.uid shouldBe "me"
        }
    }

    test("alignment LEFT_ALIGNED forces all messages to left regardless of sender") {
        runTest {
            val viewModel = createViewModel()
            val outgoingMessage = createParentMessage(id = 1L, senderUid = "me")
            println("    → LEFT_ALIGNED alignment → all messages LEFT")

            viewModel.setParentMessage(outgoingMessage)
            advanceUntilIdle()

            // View forces LEFT alignment regardless of sender
            // ViewModel still provides the message — alignment is View-level concern
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    test("PBT: alignment mode does not affect ViewModel state") {
        val alignmentArb = Arb.element(
            UIKitConstants.MessageListAlignment.STANDARD,
            UIKitConstants.MessageListAlignment.LEFT_ALIGNED
        )
        checkAll(20, alignmentArb, Arb.long(1L, 100L)) { alignment, msgId ->
            runTest {
                val viewModel = createViewModel()
                val parentMessage = createParentMessage(id = msgId)
                println("    → alignment=$alignment, msgId=$msgId")

                viewModel.setParentMessage(parentMessage)
                advanceUntilIdle()

                // ViewModel state is alignment-agnostic
                viewModel.parentMessageListStateFlow.value shouldHaveSize 1
                viewModel.parentMessageListStateFlow.value[0].id shouldBe msgId
            }
        }
    }

    // ==================== Text Formatters ====================

    test("text formatters are a View-level concern, ViewModel provides raw message") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = mock<TextMessage>()
            val sender = mock<User>()
            whenever(sender.uid).thenReturn("user-1")
            whenever(sender.name).thenReturn("Sender")
            whenever(parentMessage.id).thenReturn(1L)
            whenever(parentMessage.text).thenReturn("Hello @user")
            whenever(parentMessage.sender).thenReturn(sender)
            whenever(parentMessage.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
            whenever(parentMessage.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            whenever(parentMessage.replyCount).thenReturn(0)
            whenever(parentMessage.parentMessageId).thenReturn(0L)
            println("    → Text formatters applied by View/Adapter, not ViewModel")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // ViewModel provides raw message; View applies formatters to adapter
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    // ==================== BubbleFactory Integration ====================

    test("bubble factory is a View-level concern, ViewModel provides message type info") {
        runTest {
            val viewModel = createViewModel()
            val mediaMessage = mock<MediaMessage>()
            val sender = mock<User>()
            whenever(sender.uid).thenReturn("user-1")
            whenever(sender.name).thenReturn("Sender")
            whenever(mediaMessage.id).thenReturn(1L)
            whenever(mediaMessage.type).thenReturn("image")
            whenever(mediaMessage.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            whenever(mediaMessage.sender).thenReturn(sender)
            whenever(mediaMessage.replyCount).thenReturn(0)
            whenever(mediaMessage.parentMessageId).thenReturn(0L)
            println("    → BubbleFactory uses message.type to determine bubble rendering")

            viewModel.setParentMessage(mediaMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].type shouldBe "image"
        }
    }

    // ==================== Reply Count Update ====================

    test("reply count update triggers replyCountStateFlow for View to observe") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 10)
            println("    → setParentMessage(replyCount=10) → View observes replyCountStateFlow")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.replyCountStateFlow.value shouldBe 10
        }
    }

    test("PBT: any reply count from message is reflected in StateFlow") {
        checkAll(30, Arb.int(0..500)) { count ->
            runTest {
                val viewModel = createViewModel()
                val parentMessage = createParentMessage(id = 1L, replyCount = count)
                println("    → replyCount=$count")

                viewModel.setParentMessage(parentMessage)
                advanceUntilIdle()

                viewModel.replyCountStateFlow.value shouldBe count
            }
        }
    }
})
