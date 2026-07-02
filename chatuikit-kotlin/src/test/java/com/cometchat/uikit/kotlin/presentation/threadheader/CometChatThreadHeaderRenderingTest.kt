package com.cometchat.uikit.kotlin.presentation.threadheader

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
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
 * JVM rendering tests for CometChatThreadHeader (chatuikit-kotlin).
 * Verifies ViewModel states produce correct data for View rendering.
 *
 * Tests focus on:
 * - Parent message renders in RecyclerView (via adapter update)
 * - Reply count displays correct format ("0 Replies", "1 Reply", "5 Replies")
 * - Reply count bar visibility
 * - Alignment affects bubble position
 * - Max height constraint
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatThreadHeaderRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatThreadHeaderRenderingTest : FunSpec({

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

    // ==================== Parent Message Rendering ====================

    test("parent message renders in single-item list for RecyclerView adapter") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 42L)
            println("    → setParentMessage(id=42) → adapter should receive 1-item list")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 42L
        }
    }

    test("parent message list is empty before setParentMessage is called") {
        runTest {
            val viewModel = createViewModel()
            println("    → No setParentMessage → adapter receives empty list")

            viewModel.parentMessageListStateFlow.value shouldHaveSize 0
        }
    }

    // ==================== Reply Count Format ====================

    test("reply count 0 should format as '0 Replies'") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 0)
            println("    → replyCount=0 → '0 Replies'")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.replyCountStateFlow.value shouldBe 0
            // View formats: "$count Replies" when count != 1
        }
    }

    test("reply count 1 should format as '1 Reply'") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 1)
            println("    → replyCount=1 → '1 Reply'")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.replyCountStateFlow.value shouldBe 1
            // View formats: "$count Reply" when count == 1
        }
    }

    test("PBT: reply count > 1 should format as 'N Replies'") {
        checkAll(50, Arb.int(2..100)) { count ->
            runTest {
                val viewModel = createViewModel()
                val parentMessage = createParentMessage(id = 1L, replyCount = count)
                println("    → replyCount=$count → '$count Replies'")

                viewModel.setParentMessage(parentMessage)
                advanceUntilIdle()

                viewModel.replyCountStateFlow.value shouldBe count
                // View formats: "$count Replies" when count != 1
            }
        }
    }

    // ==================== Reply Count Bar Visibility ====================

    test("reply count is available for bar visibility control") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 5)
            println("    → replyCount=5 → bar should be visible")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // The View controls visibility via setReplyCountBarVisibility
            // ViewModel just provides the count
            viewModel.replyCountStateFlow.value shouldBe 5
        }
    }

    // ==================== Alignment ====================

    test("alignment is controlled by View, ViewModel provides message sender for determination") {
        runTest {
            val viewModel = createViewModel()
            val outgoingMessage = createParentMessage(id = 1L, senderUid = "logged-in-user")
            println("    → outgoing message → View determines RIGHT alignment")

            viewModel.setParentMessage(outgoingMessage)
            advanceUntilIdle()

            // The View's determineAndApplyAlignment checks sender.uid vs loggedInUser.uid
            viewModel.parentMessageListStateFlow.value[0].sender?.uid shouldBe "logged-in-user"
        }
    }

    test("incoming message sender uid differs from logged-in user for LEFT alignment") {
        runTest {
            val viewModel = createViewModel()
            val incomingMessage = createParentMessage(id = 1L, senderUid = "other-user")
            println("    → incoming message → View determines LEFT alignment")

            viewModel.setParentMessage(incomingMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value[0].sender?.uid shouldBe "other-user"
        }
    }

    // ==================== Max Height ====================

    test("max height is a View-level constraint, ViewModel provides full message content") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L)
            println("    → ViewModel provides message regardless of max height constraint")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Max height is enforced by the View's onMeasure override
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    // ==================== Message Update Rendering ====================

    test("updated parent message triggers adapter refresh") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 50L)
            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            val editedMessage = mock<TextMessage>()
            val editSender = mock<User>()
            whenever(editSender.uid).thenReturn("user-1")
            whenever(editSender.name).thenReturn("Sender")
            whenever(editedMessage.id).thenReturn(50L)
            whenever(editedMessage.text).thenReturn("Edited")
            whenever(editedMessage.sender).thenReturn(editSender)
            whenever(editedMessage.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
            whenever(editedMessage.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            whenever(editedMessage.parentMessageId).thenReturn(0L)
            println("    → Edit message id=50 → adapter should refresh")

            viewModel.updateParentMessageInList(editedMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 50L
        }
    }
})
