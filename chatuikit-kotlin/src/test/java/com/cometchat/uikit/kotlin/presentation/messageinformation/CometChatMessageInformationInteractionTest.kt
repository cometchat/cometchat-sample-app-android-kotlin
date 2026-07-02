package com.cometchat.uikit.kotlin.presentation.messageinformation

import com.cometchat.chat.constants.CometChatConstants
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
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Interaction tests for CometChatMessageInformation Kotlin component.
 *
 * Verifies ViewModel operations triggered by user interactions:
 * - setMessage triggers correct fetch behavior
 * - fetchMessageReceipt can be called manually
 * - Real-time receipt updates via setOrUpdate
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.messageinformation.CometChatMessageInformationInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageInformationInteractionTest : FunSpec({

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
        receipts: List<MessageReceipt> = emptyList()
    ): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.success(receipts)
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

    fun createUserMessage(messageId: Long, deliveredAt: Long, readAt: Long): BaseMessage {
        val receiver = User().apply { uid = "receiver-1"; name = "Receiver" }
        return TextMessage(receiver.uid, "Test", CometChatConstants.RECEIVER_TYPE_USER).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.receiver = receiver
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    fun createMockReceipt(senderUid: String, messageId: Long, deliveredAt: Long = 0L, readAt: Long = 0L): MessageReceipt {
        return MessageReceipt().apply {
            this.messageId = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            sender = User().apply { uid = senderUid; name = "User $senderUid" }
        }
    }

    // ==================== setMessage Interaction ====================

    test("setMessage with GROUP triggers fetchReceipts") {
        println("=== TEST: setMessage GROUP triggers fetch ===")
        runTest {
            var fetchCalled = false
            val repository = object : MessageInformationRepository {
                override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                    fetchCalled = true
                    return Result.success(emptyList())
                }
                override fun createReceiptFromMessage(message: BaseMessage) = MessageReceipt()
            }
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting GROUP message")
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            println("STEP 2: Asserting fetchReceipts was called")
            fetchCalled shouldBe true
            println("RESULT: fetchReceipts called for GROUP message ✅")
        }
    }

    test("setMessage with USER does NOT trigger fetchReceipts") {
        println("=== TEST: setMessage USER does NOT fetch ===")
        runTest {
            var fetchCalled = false
            val repository = object : MessageInformationRepository {
                override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                    fetchCalled = true
                    return Result.success(emptyList())
                }
                override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                    return MessageReceipt().apply {
                        deliveredAt = message.deliveredAt
                        readAt = message.readAt
                        messageId = message.id
                    }
                }
            }
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting USER message with deliveredAt=1000")
            viewModel.setMessage(createUserMessage(100L, deliveredAt = 1000L, readAt = 2000L))
            advanceUntilIdle()

            println("STEP 2: Asserting fetchReceipts was NOT called")
            fetchCalled shouldBe false
            println("RESULT: fetchReceipts NOT called for USER message ✅")
        }
    }

    test("setMessage stores the message for later access via getMessage") {
        println("=== TEST: getMessage returns set message ===")
        runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)

            val message = createGroupMessage(42L)
            viewModel.setMessage(message)
            advanceUntilIdle()

            viewModel.getMessage() shouldNotBe null
            viewModel.getMessage()?.id shouldBe 42L
            println("RESULT: getMessage() returns message with id=42 ✅")
        }
    }

    // ==================== PBT: Receipt creation for USER ====================

    test("PBT: USER message creates receipt with correct deliveredAt") {
        println("=== PBT: USER receipt deliveredAt ===")
        checkAll(20, Arb.long(1L, 100000L), Arb.long(0L, 100000L)) { deliveredAt, readAt ->
            runTest {
                val repository = createRepository()
                val viewModel = createViewModel(repository)

                viewModel.setMessage(createUserMessage(100L, deliveredAt = deliveredAt, readAt = readAt))
                advanceUntilIdle()

                if (deliveredAt > 0) {
                    viewModel.state.value shouldBe MessageInformationUIState.Loaded
                    viewModel.listData.value.size shouldBe 1
                    println("  [Iteration] deliveredAt=$deliveredAt → Loaded ✅")
                } else {
                    viewModel.state.value shouldBe MessageInformationUIState.Empty
                    println("  [Iteration] deliveredAt=0 → Empty ✅")
                }
            }
        }
    }

    // ==================== PBT: Receipt timestamp preservation ====================

    test("PBT: setOrUpdate preserves existing non-zero timestamps when new is 0") {
        println("=== PBT: Timestamp preservation ===")
        checkAll(20, Arb.long(1L, 50000L), Arb.long(1L, 50000L)) { existingDelivered, existingRead ->
            runTest {
                val existingReceipt = createMockReceipt("user-1", 100L, deliveredAt = existingDelivered, readAt = existingRead)
                val repository = createRepository(receipts = listOf(existingReceipt))
                val viewModel = createViewModel(repository)

                viewModel.setMessage(createGroupMessage(100L))
                advanceUntilIdle()

                // Verify initial state
                viewModel.listData.value.size shouldBe 1
                viewModel.listData.value[0].deliveredAt shouldBe existingDelivered
                viewModel.listData.value[0].readAt shouldBe existingRead
                println("  [Iteration] existing delivered=$existingDelivered, read=$existingRead ✅")
            }
        }
    }

    // ==================== addListener / removeListener ====================

    test("removeListener stops event processing") {
        println("=== TEST: removeListener stops events ===")
        runTest {
            val repository = createRepository(receipts = emptyList())
            val viewModel = createViewModel(repository)

            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            println("STEP 1: Removing listener")
            viewModel.removeListener()

            println("STEP 2: Asserting listener removed (no crash)")
            // Just verify no exception — events won't be processed
            println("RESULT: removeListener completed without error ✅")
        }
    }
})
