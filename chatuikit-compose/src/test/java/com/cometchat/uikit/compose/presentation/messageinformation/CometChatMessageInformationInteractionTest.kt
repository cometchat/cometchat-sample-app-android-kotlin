package com.cometchat.uikit.compose.presentation.messageinformation

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
import io.kotest.property.arbitrary.element
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
 * Interaction tests for CometChatMessageInformation Compose component.
 *
 * Verifies ViewModel operations triggered by composable interactions:
 * - setMessage triggers correct behavior per conversation type
 * - getMessage returns the set message
 * - getConversationType returns correct type
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.messageinformation.CometChatMessageInformationInteractionTest"
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

    fun createRepository(): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long) = Result.success(emptyList<MessageReceipt>())
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

    fun createViewModel(): CometChatMessageInformationViewModel {
        return CometChatMessageInformationViewModel(
            repository = createRepository(),
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )
    }

    fun createMessage(receiverType: String, messageId: Long, deliveredAt: Long = 1000L): BaseMessage {
        val receiverId = if (receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) "group-1" else "user-1"
        return TextMessage(receiverId, "Test", receiverType).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = deliveredAt + 500
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                receiver = User().apply { uid = receiverId; name = "Receiver" }
            }
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    // ==================== Tests ====================

    test("setMessage stores message accessible via getMessage") {
        println("=== TEST: setMessage → getMessage ===")
        runTest {
            val viewModel = createViewModel()
            val message = createMessage(CometChatConstants.RECEIVER_TYPE_GROUP, 42L)

            viewModel.setMessage(message)
            advanceUntilIdle()

            viewModel.getMessage() shouldNotBe null
            viewModel.getMessage()?.id shouldBe 42L
            println("RESULT: getMessage() returns id=42 ✅")
        }
    }

    test("PBT: getConversationType matches message receiverType") {
        println("=== PBT: conversationType matches receiverType ===")
        checkAll(
            20,
            Arb.element(CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.RECEIVER_TYPE_GROUP),
            Arb.long(1L, 10000L)
        ) { receiverType, messageId ->
            runTest {
                val viewModel = createViewModel()
                val message = createMessage(receiverType, messageId)

                viewModel.setMessage(message)
                advanceUntilIdle()

                viewModel.getConversationType() shouldBe receiverType
                println("  [Iteration] receiverType=$receiverType, id=$messageId ✅")
            }
        }
    }

    test("setMessage with null does nothing") {
        println("=== TEST: setMessage(null) no-op ===")
        runTest {
            val viewModel = createViewModel()

            viewModel.setMessage(null)
            advanceUntilIdle()

            viewModel.getMessage() shouldBe null
            viewModel.getConversationType() shouldBe null
            println("RESULT: null message → no state change ✅")
        }
    }

    test("USER message creates receipt with correct messageId") {
        println("=== TEST: USER receipt has correct messageId ===")
        runTest {
            val viewModel = createViewModel()
            val message = createMessage(CometChatConstants.RECEIVER_TYPE_USER, 777L, deliveredAt = 1000L)

            viewModel.setMessage(message)
            advanceUntilIdle()

            viewModel.state.value shouldBe MessageInformationUIState.Loaded
            viewModel.listData.value.size shouldBe 1
            viewModel.listData.value[0].messageId shouldBe 777L
            println("RESULT: Receipt messageId=777 ✅")
        }
    }

    test("GROUP message fetches receipts and populates listData") {
        println("=== TEST: GROUP fetches and populates ===")
        runTest {
            val receipts = (1..3).map { i ->
                MessageReceipt().apply {
                    messageId = 100L
                    deliveredAt = 1000L + i * 100
                    sender = User().apply { uid = "user-$i"; name = "User $i" }
                }
            }
            val repository = object : MessageInformationRepository {
                override suspend fun fetchReceipts(messageId: Long) = Result.success(receipts)
                override fun createReceiptFromMessage(message: BaseMessage) = MessageReceipt()
            }
            val viewModel = CometChatMessageInformationViewModel(
                repository = repository,
                eventListener = MessageReceiptEventListener(),
                enableListeners = false
            )

            viewModel.setMessage(createMessage(CometChatConstants.RECEIVER_TYPE_GROUP, 100L))
            advanceUntilIdle()

            viewModel.listData.value.size shouldBe 3
            viewModel.listData.value[0].sender?.uid shouldBe "user-1"
            println("RESULT: 3 receipts fetched and populated ✅")
        }
    }
})
