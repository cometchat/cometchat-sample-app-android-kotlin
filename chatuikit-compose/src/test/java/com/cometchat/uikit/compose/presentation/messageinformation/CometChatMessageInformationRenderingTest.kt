package com.cometchat.uikit.compose.presentation.messageinformation

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.state.MessageInformationUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
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

/**
 * Rendering tests for CometChatMessageInformation Compose component.
 *
 * Verifies ViewModel state that drives composable rendering:
 * - Loading → shimmer composable
 * - Loaded → receipt list LazyColumn
 * - Empty → empty state composable
 * - Error → error state composable
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.messageinformation.CometChatMessageInformationRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageInformationRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Helpers ====================

    fun createRepository(
        receipts: List<MessageReceipt> = emptyList(),
        error: CometChatException? = null
    ): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return if (error != null) Result.failure(error) else Result.success(receipts)
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt().apply {
                    sender = if (message.receiver is User) message.receiver as User else null
                    readAt = message.readAt
                    deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
                    messageId = message.id
                }
            }
        }
    }

    fun createViewModel(repository: MessageInformationRepository): CometChatMessageInformationViewModel {
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )
    }

    fun createGroupMessage(messageId: Long): BaseMessage {
        return TextMessage("group-1", "Test", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            id = messageId
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    fun createUserMessage(messageId: Long, deliveredAt: Long = 1000L, readAt: Long = 2000L): BaseMessage {
        val receiver = User().apply { uid = "receiver-1"; name = "Receiver" }
        return TextMessage(receiver.uid, "Test", CometChatConstants.RECEIVER_TYPE_USER).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.receiver = receiver
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    fun createMockReceipts(count: Int, messageId: Long = 100L): List<MessageReceipt> {
        return (1..count).map { i ->
            MessageReceipt().apply {
                this.messageId = messageId
                this.deliveredAt = 1000L + i * 100
                this.readAt = 2000L + i * 100
                sender = User().apply { uid = "user-$i"; name = "User $i" }
            }
        }
    }

    // ==================== Tests ====================

    test("GROUP message with receipts → Loaded state (drives receipt list composable)") {
        println("=== TEST: GROUP receipts → Loaded ===")
        runTest {
            val receipts = createMockReceipts(3)
            val viewModel = createViewModel(createRepository(receipts = receipts))
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            viewModel.state.value shouldBe MessageInformationUIState.Loaded
            viewModel.listData.value.size shouldBe 3
            println("RESULT: Loaded with 3 receipts ✅")
        }
    }

    test("GROUP message with empty receipts → Empty state (drives empty composable)") {
        println("=== TEST: GROUP empty → Empty ===")
        runTest {
            val viewModel = createViewModel(createRepository(receipts = emptyList()))
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            viewModel.state.value shouldBe MessageInformationUIState.Empty
            println("RESULT: Empty state ✅")
        }
    }

    test("GROUP message with error → Error state (drives error composable)") {
        println("=== TEST: GROUP error → Error ===")
        runTest {
            val exception = CometChatException("FETCH_ERR", "Failed")
            val viewModel = createViewModel(createRepository(error = exception))
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            viewModel.state.value.shouldBeInstanceOf<MessageInformationUIState.Error>()
            println("RESULT: Error state ✅")
        }
    }

    test("USER message with delivery → Loaded state with 1 receipt") {
        println("=== TEST: USER delivered → Loaded ===")
        runTest {
            val viewModel = createViewModel(createRepository())
            viewModel.setMessage(createUserMessage(100L, deliveredAt = 500L, readAt = 1000L))
            advanceUntilIdle()

            viewModel.state.value shouldBe MessageInformationUIState.Loaded
            viewModel.listData.value.size shouldBe 1
            println("RESULT: Loaded with 1 receipt ✅")
        }
    }

    test("USER message without delivery → Empty state") {
        println("=== TEST: USER not delivered → Empty ===")
        runTest {
            val viewModel = createViewModel(createRepository())
            viewModel.setMessage(createUserMessage(100L, deliveredAt = 0L, readAt = 0L))
            advanceUntilIdle()

            viewModel.state.value shouldBe MessageInformationUIState.Empty
            println("RESULT: Empty state ✅")
        }
    }

    test("PBT: Receipt timestamp preservation — non-zero values preserved in list") {
        println("=== PBT: Timestamp preservation ===")
        checkAll(20, Arb.long(1L, 50000L), Arb.long(1L, 50000L)) { delivered, read ->
            runTest {
                val receipt = MessageReceipt().apply {
                    messageId = 100L
                    deliveredAt = delivered
                    readAt = read
                    sender = User().apply { uid = "user-1"; name = "User 1" }
                }
                val viewModel = createViewModel(createRepository(receipts = listOf(receipt)))
                viewModel.setMessage(createGroupMessage(100L))
                advanceUntilIdle()

                viewModel.listData.value[0].deliveredAt shouldBe delivered
                viewModel.listData.value[0].readAt shouldBe read
                println("  [Iteration] delivered=$delivered, read=$read ✅")
            }
        }
    }
})
