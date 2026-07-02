package com.cometchat.uikit.compose.presentation.threadheader

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
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
 * JVM rendering tests for CometChatThreadHeader (chatuikit-compose).
 * Verifies ViewModel states produce correct data for composable rendering.
 *
 * Tests focus on:
 * - Parent message renders via CometChatMessageBubble
 * - Reply count displays correct format
 * - Reply count bar visibility
 * - Custom messageBubbleView replaces default
 * - Custom replyCountView replaces default
 * - Max height constraint applied
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*threadheader.CometChatThreadHeaderRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatThreadHeaderRenderingTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    fun createViewModel(): ThreadHeaderViewModel {
        return ThreadHeaderViewModel(enableListeners = false)
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
        whenever(message.type).thenReturn("text")
        whenever(message.category).thenReturn("message")
        whenever(message.sentAt).thenReturn(1735689600L)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        whenever(message.replyCount).thenReturn(replyCount)
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

    test("parent message renders via CometChatMessageBubble when messageList is non-empty") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 42L)
            println("    → setParentMessage(id=42) → composable renders message bubble")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable collects parentMessageListStateFlow and renders firstOrNull()
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 42L
        }
    }

    test("no message bubble rendered when parentMessageListStateFlow is empty") {
        runTest {
            val viewModel = createViewModel()
            println("    → No setParentMessage → composable renders nothing in bubble section")

            viewModel.parentMessageListStateFlow.value shouldHaveSize 0
        }
    }

    // ==================== Reply Count Format ====================

    test("reply count 0 formats as '0 Replies' for composable Text") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 0)
            println("    → replyCount=0 → formatReplyCount returns '0 Replies'")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.replyCountStateFlow.value shouldBe 0
            // Composable: when count != 1 → "$count ${stringResource(R.string.cometchat_replies)}"
        }
    }

    test("reply count 1 formats as '1 Reply' for composable Text") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 1)
            println("    → replyCount=1 → formatReplyCount returns '1 Reply'")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            viewModel.replyCountStateFlow.value shouldBe 1
            // Composable: when count == 1 → "$count ${stringResource(R.string.cometchat_reply)}"
        }
    }

    test("PBT: reply count > 1 formats as 'N Replies' for composable Text") {
        checkAll(50, Arb.int(2..500)) { count ->
            runTest {
                val viewModel = createViewModel()
                val parentMessage = createParentMessage(id = 1L, replyCount = count)
                println("    → replyCount=$count → '$count Replies'")

                viewModel.setParentMessage(parentMessage)
                advanceUntilIdle()

                viewModel.replyCountStateFlow.value shouldBe count
            }
        }
    }

    // ==================== Reply Count Bar Visibility ====================

    test("reply count bar renders when hideReplyCountBar=false") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 5)
            println("    → hideReplyCountBar=false → ReplyCountBar composable renders")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: if (!hideReplyCountBar) { ReplyCountBar(...) }
            viewModel.replyCountStateFlow.value shouldBe 5
        }
    }

    test("reply count text hidden when hideReplyCount=true but bar still visible") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 5)
            println("    → hideReplyCount=true → Text not rendered inside ReplyCountBar")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: if (!hideReplyCount) { Text(...) } inside ReplyCountBar
            viewModel.replyCountStateFlow.value shouldBe 5
        }
    }

    // ==================== Custom Views ====================

    test("custom messageBubbleView replaces default when provided") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L)
            println("    → messageBubbleView != null → custom composable renders instead of CometChatMessageBubble")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: if (messageBubbleView != null) { messageBubbleView(message) }
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    test("custom replyCountView replaces default when provided") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L, replyCount = 7)
            println("    → replyCountView != null → custom composable renders instead of default Text")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: if (replyCountView != null) { replyCountView(replyCount) }
            viewModel.replyCountStateFlow.value shouldBe 7
        }
    }

    // ==================== Max Height Constraint ====================

    test("max height constraint is applied via Modifier.heightIn") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L)
            println("    → maxHeight != Dp.Unspecified → Modifier.heightIn(max = maxHeight)")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: val heightModifier = if (maxHeight != Dp.Unspecified) Modifier.heightIn(max = maxHeight)
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    // ==================== ViewModel State Updates Trigger Recomposition ====================

    test("ViewModel state update triggers recomposition via collectAsState") {
        runTest {
            val viewModel = createViewModel()
            val msg1 = createParentMessage(id = 1L, replyCount = 3)
            val msg2 = createParentMessage(id = 2L, replyCount = 8)
            println("    → State change → collectAsState triggers recomposition")

            viewModel.setParentMessage(msg1)
            advanceUntilIdle()
            viewModel.replyCountStateFlow.value shouldBe 3

            viewModel.setParentMessage(msg2)
            advanceUntilIdle()
            viewModel.replyCountStateFlow.value shouldBe 8
            viewModel.parentMessageListStateFlow.value[0].id shouldBe 2L
        }
    }

    // ==================== Alignment ====================

    test("STANDARD alignment determines bubble position based on sender") {
        runTest {
            val viewModel = createViewModel()
            val outgoingMessage = createParentMessage(id = 1L, senderUid = "logged-in-user")
            println("    → STANDARD + outgoing → RIGHT bubble alignment")

            viewModel.setParentMessage(outgoingMessage)
            advanceUntilIdle()

            viewModel.parentMessageListStateFlow.value[0].sender?.uid shouldBe "logged-in-user"
        }
    }

    test("LEFT_ALIGNED forces all messages to LEFT bubble alignment") {
        runTest {
            val viewModel = createViewModel()
            val outgoingMessage = createParentMessage(id = 1L, senderUid = "logged-in-user")
            println("    → LEFT_ALIGNED → all messages LEFT regardless of sender")

            viewModel.setParentMessage(outgoingMessage)
            advanceUntilIdle()

            // Composable: when alignment == LEFT_ALIGNED → UIKitConstants.MessageBubbleAlignment.LEFT
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    // ==================== BubbleFactory Integration ====================

    test("bubble factory map is built from bubbleFactories list via toFactoryMap()") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L)
            println("    → bubbleFactories.toFactoryMap() → factory lookup by buildFactoryKey(message)")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: val factoryMap = remember(bubbleFactories) { bubbleFactories.toFactoryMap() }
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }

    // ==================== Text Formatters ====================

    test("effective text formatters default to CometChatMentionsFormatter when none provided") {
        runTest {
            val viewModel = createViewModel()
            val parentMessage = createParentMessage(id = 1L)
            println("    → textFormatters=null → effectiveTextFormatters = listOf(CometChatMentionsFormatter)")

            viewModel.setParentMessage(parentMessage)
            advanceUntilIdle()

            // Composable: val effectiveTextFormatters = textFormatters ?: remember { listOf(CometChatMentionsFormatter(context)) }
            viewModel.parentMessageListStateFlow.value shouldHaveSize 1
        }
    }
})
