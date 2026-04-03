package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.state.MessageInformationUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Property-based tests for CometChatMessageInformationViewModel.
 * 
 * Feature: message-information-compose
 * **Validates: Requirements 1.1, 12.2, 12.3, 12.4, 12.5, 13.7**
 * 
 * ## Test Coverage
 * 
 * | Property | Description | Requirements |
 * |----------|-------------|--------------|
 * | Property 1 | Conversation Type Determination | 1.1 |
 * | Property 10 | Real-Time Receipt Event Handling | 12.2, 12.3 |
 * | Property 11 | Receipt Update Timestamp Preservation | 12.4 |
 * | Property 12 | New Receipt Insertion Position | 12.5 |
 * | Property 13 | Receipt Creation from USER Message | 13.7 |
 * | Property 14 | Receipt Sender Matching | 12.2, 12.3, 12.4 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageInformationViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // ==================== Mock Classes ====================

    /**
     * Mock repository for testing ViewModel behavior.
     * Tracks calls and allows configuring responses.
     */
    class MockMessageInformationRepository(
        private val loggedInUserUid: String = "test_logged_in_user"
    ) : MessageInformationRepository {
        var fetchReceiptsResult: Result<List<MessageReceipt>> = Result.success(emptyList())
        var lastFetchedMessageId: Long? = null
        var fetchReceiptsCallCount: Int = 0

        override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
            fetchReceiptsCallCount++
            lastFetchedMessageId = messageId
            return fetchReceiptsResult
        }

        override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
            return MessageReceipt().apply {
                sender = if (message.receiver is User) message.receiver as User else null
                readAt = message.readAt
                timestamp = message.readAt
                deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
                messageId = message.id
                receiverType = message.receiverType
                receiverId = loggedInUserUid
                messageSender = message.sender?.toString() ?: ""
            }
        }
    }

    /**
     * Testable ViewModel that accepts a Flow for receipt events instead of MessageReceiptEventListener.
     * This allows us to inject test events without extending the final class.
     */
    class TestableMessageInformationViewModel(
        repository: MessageInformationRepository,
        private val receiptEventsFlow: MutableSharedFlow<MessageReceipt>,
        enableListeners: Boolean = true
    ) : CometChatMessageInformationViewModel(
        repository,
        MessageReceiptEventListener(), // Dummy, won't be used
        enableListeners = false // Disable real listeners
    ) {
        private var testReceiptEventsJob: Job? = null

        init {
            if (enableListeners) {
                startTestListener()
            }
        }

        private fun startTestListener() {
            testReceiptEventsJob = CoroutineScope(Dispatchers.Main).launch {
                receiptEventsFlow.collect { messageReceipt ->
                    val currentMessage = getMessage()
                    if (currentMessage != null && messageReceipt.messageId == currentMessage.id) {
                        // Use reflection to call private setOrUpdate method
                        val method = CometChatMessageInformationViewModel::class.java
                            .getDeclaredMethod("setOrUpdate", MessageReceipt::class.java)
                        method.isAccessible = true
                        method.invoke(this@TestableMessageInformationViewModel, messageReceipt)
                    }
                }
            }
        }

        fun stopTestListener() {
            testReceiptEventsJob?.cancel()
            testReceiptEventsJob = null
        }
    }

    // ==================== Helper Functions ====================

    /**
     * Creates a mock BaseMessage for testing.
     */
    fun createMockMessage(
        messageId: Long,
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        readAt: Long = 0L,
        deliveredAt: Long = 0L,
        receiverUser: User? = null
    ): BaseMessage {
        return TextMessage(
            receiverUser?.uid ?: "receiver_id",
            "Test message content",
            receiverType
        ).apply {
            id = messageId
            this.readAt = readAt
            this.deliveredAt = deliveredAt
            if (receiverUser != null) {
                receiver = receiverUser
            }
            sender = User().apply {
                uid = "sender_user"
                name = "Sender"
            }
        }
    }

    /**
     * Creates a mock MessageReceipt for testing.
     */
    fun createMockReceipt(
        senderUid: String,
        messageId: Long = 100L,
        readAt: Long = 0L,
        deliveredAt: Long = 0L
    ): MessageReceipt {
        return MessageReceipt().apply {
            this.messageId = messageId
            this.readAt = readAt
            this.deliveredAt = deliveredAt
            sender = User().apply {
                uid = senderUid
                name = "User $senderUid"
            }
        }
    }

    // ========================================
    // Property 1: Conversation Type Determination
    // ========================================

    context("Property 1: Conversation Type Determination") {

        /**
         * **Property 1: Conversation Type Determination**
         * 
         * *For any* BaseMessage with a valid receiverType, the component SHALL correctly 
         * determine the conversation type as USER when receiverType equals "user" and 
         * GROUP when receiverType equals "group".
         * 
         * **Validates: Requirements 1.1**
         */

        test("Property 1: receiverType 'user' should set conversation type to USER") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        deliveredAt = 1000L
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    viewModel.getConversationType() shouldBe CometChatConstants.RECEIVER_TYPE_USER
                }
            }
        }

        test("Property 1: receiverType 'group' should set conversation type to GROUP") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    viewModel.getConversationType() shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
                }
            }
        }

        test("Property 1: For any valid receiverType, conversation type is correctly determined") {
            checkAll(
                100,
                Arb.element(CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.RECEIVER_TYPE_GROUP),
                Arb.long(1L, 10000L)
            ) { receiverType, messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = receiverType,
                        deliveredAt = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) 1000L else 0L
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    viewModel.getConversationType() shouldBe receiverType
                }
            }
        }

        test("Property 1: USER conversation should NOT call fetchReceipts") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        deliveredAt = 1000L
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    repository.fetchReceiptsCallCount shouldBe 0
                }
            }
        }

        test("Property 1: GROUP conversation should call fetchReceipts") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    repository.fetchReceiptsCallCount shouldBe 1
                    repository.lastFetchedMessageId shouldBe messageId
                }
            }
        }
    }

    // ========================================
    // Property 10: Real-Time Receipt Event Handling
    // ========================================

    context("Property 10: Real-Time Receipt Event Handling") {

        /**
         * **Property 10: Real-Time Receipt Event Handling**
         * 
         * *For any* MessagesDelivered or MessagesRead event where the messageId matches 
         * the current message's id, the receipt SHALL be either updated (if sender already 
         * exists) or added to the list.
         * 
         * **Validates: Requirements 12.2, 12.3**
         */

        test("Property 10: Receipt event with matching messageId should be added to list") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 20)) { messageId, senderUid ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    repository.fetchReceiptsResult = Result.success(emptyList())
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit receipt event with matching messageId
                    val receipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(receipt)
                    advanceUntilIdle()

                    // Verify receipt was added
                    val listData = viewModel.listData.value
                    listData.any { it.sender?.uid == senderUid } shouldBe true
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 10: Receipt event with non-matching messageId should be ignored") {
            checkAll(100, Arb.long(1L, 5000L), Arb.long(5001L, 10000L)) { messageId, differentMessageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    repository.fetchReceiptsResult = Result.success(emptyList())
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val initialListSize = viewModel.listData.value.size

                    // Emit receipt event with different messageId
                    val receipt = createMockReceipt(
                        senderUid = "some_user",
                        messageId = differentMessageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(receipt)
                    advanceUntilIdle()

                    // Verify receipt was NOT added
                    viewModel.listData.value.size shouldBe initialListSize
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 10: Existing receipt should be updated, not duplicated") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 20)) { messageId, senderUid ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    // Pre-populate with existing receipt
                    val existingReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 500L,
                        readAt = 0L
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val initialListSize = viewModel.listData.value.size

                    // Emit receipt event with same sender (update)
                    val updatedReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 500L,
                        readAt = 1000L
                    )
                    receiptEventsFlow.emit(updatedReceipt)
                    advanceUntilIdle()

                    // Verify list size unchanged (updated, not added)
                    viewModel.listData.value.size shouldBe initialListSize
                    
                    viewModel.stopTestListener()
                }
            }
        }
    }

    // ========================================
    // Property 11: Receipt Update Timestamp Preservation
    // ========================================

    context("Property 11: Receipt Update Timestamp Preservation") {

        /**
         * **Property 11: Receipt Update Timestamp Preservation**
         * 
         * *For any* receipt update where the new receipt has a timestamp value of 0, 
         * the existing timestamp SHALL be preserved. Only non-zero timestamps from 
         * the new receipt SHALL overwrite existing values.
         * 
         * **Validates: Requirements 12.4**
         */

        test("Property 11: Zero deliveredAt in update should preserve existing deliveredAt") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, Long.MAX_VALUE / 2)) { messageId, existingDeliveredAt ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val senderUid = "test_sender"
                    // Pre-populate with existing receipt that has deliveredAt
                    val existingReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = existingDeliveredAt,
                        readAt = 0L
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit update with deliveredAt = 0 (should preserve existing)
                    val updateReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 0L,  // Zero - should preserve existing
                        readAt = 2000L
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()

                    // Verify existing deliveredAt was preserved
                    val updatedReceipt = viewModel.listData.value.find { it.sender?.uid == senderUid }
                    updatedReceipt shouldNotBe null
                    updatedReceipt?.deliveredAt shouldBe existingDeliveredAt
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 11: Zero readAt in update should preserve existing readAt") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, Long.MAX_VALUE / 2)) { messageId, existingReadAt ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val senderUid = "test_sender"
                    // Pre-populate with existing receipt that has readAt
                    val existingReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 500L,
                        readAt = existingReadAt
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit update with readAt = 0 (should preserve existing)
                    val updateReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 1000L,
                        readAt = 0L  // Zero - should preserve existing
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()

                    // Verify existing readAt was preserved
                    val updatedReceipt = viewModel.listData.value.find { it.sender?.uid == senderUid }
                    updatedReceipt shouldNotBe null
                    updatedReceipt?.readAt shouldBe existingReadAt
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 11: Non-zero timestamps in update should overwrite existing") {
            checkAll(
                100,
                Arb.long(1L, 10000L),
                Arb.long(1L, Long.MAX_VALUE / 4),
                Arb.long(Long.MAX_VALUE / 4 + 1, Long.MAX_VALUE / 2)
            ) { messageId, existingTimestamp, newTimestamp ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val senderUid = "test_sender"
                    // Pre-populate with existing receipt
                    val existingReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = existingTimestamp,
                        readAt = existingTimestamp
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit update with non-zero timestamps (should overwrite)
                    val updateReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = newTimestamp,
                        readAt = newTimestamp
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()

                    // Verify timestamps were overwritten
                    val updatedReceipt = viewModel.listData.value.find { it.sender?.uid == senderUid }
                    updatedReceipt shouldNotBe null
                    updatedReceipt?.deliveredAt shouldBe newTimestamp
                    updatedReceipt?.readAt shouldBe newTimestamp
                    
                    viewModel.stopTestListener()
                }
            }
        }
    }

    // ========================================
    // Property 12: New Receipt Insertion Position
    // ========================================

    context("Property 12: New Receipt Insertion Position") {

        /**
         * **Property 12: New Receipt Insertion Position**
         * 
         * *For any* new receipt added to the list (sender not already present), 
         * the receipt SHALL be inserted at index 0 (top of the list).
         * 
         * **Validates: Requirements 12.5**
         */

        test("Property 12: New receipt should be inserted at index 0") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 20)) { messageId, newSenderUid ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    // Pre-populate with existing receipts
                    val existingReceipts = listOf(
                        createMockReceipt("existing_user_1", messageId, deliveredAt = 100L),
                        createMockReceipt("existing_user_2", messageId, deliveredAt = 200L)
                    )
                    repository.fetchReceiptsResult = Result.success(existingReceipts)
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit new receipt (new sender)
                    val newReceipt = createMockReceipt(
                        senderUid = newSenderUid,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(newReceipt)
                    advanceUntilIdle()

                    // Verify new receipt is at index 0
                    val listData = viewModel.listData.value
                    listData.isNotEmpty() shouldBe true
                    listData[0].sender?.uid shouldBe newSenderUid
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 12: addReceipt notification should be 0 for new receipts") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 20)) { messageId, newSenderUid ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    repository.fetchReceiptsResult = Result.success(emptyList())
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Emit new receipt
                    val newReceipt = createMockReceipt(
                        senderUid = newSenderUid,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(newReceipt)
                    advanceUntilIdle()

                    // Verify addReceipt notification is 0
                    viewModel.addReceipt.value shouldBe 0
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 12: Multiple new receipts should all be inserted at top") {
            runTest {
                val repository = MockMessageInformationRepository()
                repository.fetchReceiptsResult = Result.success(emptyList())
                val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                val viewModel = TestableMessageInformationViewModel(
                    repository, receiptEventsFlow, enableListeners = true
                )

                val messageId = 12345L
                val message = createMockMessage(
                    messageId = messageId,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Add receipts in sequence
                val senders = listOf("user_1", "user_2", "user_3")
                for (sender in senders) {
                    val receipt = createMockReceipt(
                        senderUid = sender,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(receipt)
                    advanceUntilIdle()
                }

                // Last added should be at index 0
                val listData = viewModel.listData.value
                listData.size shouldBe 3
                listData[0].sender?.uid shouldBe "user_3"
                
                viewModel.stopTestListener()
            }
        }
    }

    // ========================================
    // Property 13: Receipt Creation from USER Message
    // ========================================

    context("Property 13: Receipt Creation from USER Message") {

        /**
         * **Property 13: Receipt Creation from USER Message**
         * 
         * *For any* USER conversation message with deliveredAt > 0, calling setMessage 
         * SHALL create a MessageReceipt with:
         * - sender = message.receiver (as User)
         * - readAt = message.readAt
         * - deliveredAt = message.deliveredAt (or message.readAt if deliveredAt is 0)
         * - messageId = message.id
         * 
         * **Validates: Requirements 13.7**
         */

        test("Property 13: USER message with deliveredAt > 0 should create receipt in list") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(1L, Long.MAX_VALUE / 2)) { messageId, deliveredAt ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val receiverUser = User().apply {
                        uid = "receiver_uid"
                        name = "Receiver"
                    }

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        deliveredAt = deliveredAt,
                        receiverUser = receiverUser
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Verify receipt was created and added to list
                    val listData = viewModel.listData.value
                    listData.size shouldBe 1
                    viewModel.state.value shouldBe MessageInformationUIState.Loaded
                }
            }
        }

        test("Property 13: USER message with deliveredAt = 0 should result in EMPTY state") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        deliveredAt = 0L  // Not delivered
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    // Verify EMPTY state
                    viewModel.state.value shouldBe MessageInformationUIState.Empty
                    viewModel.listData.value.isEmpty() shouldBe true
                }
            }
        }

        test("Property 13: Created receipt should have correct messageId") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        deliveredAt = 1000L
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val receipt = viewModel.listData.value.firstOrNull()
                    receipt shouldNotBe null
                    receipt?.messageId shouldBe messageId
                }
            }
        }

        test("Property 13: Created receipt should have correct readAt from message") {
            checkAll(100, Arb.long(1L, 10000L), Arb.long(0L, Long.MAX_VALUE / 2)) { messageId, readAt ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = false
                    )

                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                        readAt = readAt,
                        deliveredAt = 1000L
                    )

                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val receipt = viewModel.listData.value.firstOrNull()
                    receipt shouldNotBe null
                    receipt?.readAt shouldBe readAt
                }
            }
        }
    }

    // ========================================
    // Property 14: Receipt Sender Matching
    // ========================================

    context("Property 14: Receipt Sender Matching") {

        /**
         * **Property 14: Receipt Sender Matching**
         * 
         * *For any* two MessageReceipt objects, they are considered the same receipt 
         * if and only if their sender UIDs match (case-insensitive comparison).
         * 
         * **Validates: Requirements 12.2, 12.3, 12.4**
         */

        test("Property 14: Receipts with same UID (same case) should be considered same") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 20)) { messageId, senderUid ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    // Pre-populate with existing receipt
                    val existingReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 500L
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val initialSize = viewModel.listData.value.size

                    // Emit receipt with same UID
                    val updateReceipt = createMockReceipt(
                        senderUid = senderUid,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()

                    // Should update, not add (size unchanged)
                    viewModel.listData.value.size shouldBe initialSize
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 14: Receipts with same UID (different case) should be considered same") {
            checkAll(100, Arb.long(1L, 10000L)) { messageId ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    val lowerCaseUid = "testuser"
                    val upperCaseUid = "TESTUSER"
                    
                    // Pre-populate with existing receipt (lowercase)
                    val existingReceipt = createMockReceipt(
                        senderUid = lowerCaseUid,
                        messageId = messageId,
                        deliveredAt = 500L
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val initialSize = viewModel.listData.value.size

                    // Emit receipt with uppercase UID (should match case-insensitively)
                    val updateReceipt = createMockReceipt(
                        senderUid = upperCaseUid,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()

                    // Should update, not add (size unchanged)
                    viewModel.listData.value.size shouldBe initialSize
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 14: Receipts with different UIDs should be considered different") {
            checkAll(100, Arb.long(1L, 10000L), Arb.string(5, 10), Arb.string(11, 20)) { messageId, uid1, uid2 ->
                runTest {
                    val repository = MockMessageInformationRepository()
                    // Pre-populate with existing receipt
                    val existingReceipt = createMockReceipt(
                        senderUid = uid1,
                        messageId = messageId,
                        deliveredAt = 500L
                    )
                    repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                    val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                    val viewModel = TestableMessageInformationViewModel(
                        repository, receiptEventsFlow, enableListeners = true
                    )

                    // Set up GROUP message
                    val message = createMockMessage(
                        messageId = messageId,
                        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                    )
                    viewModel.setMessage(message)
                    advanceUntilIdle()

                    val initialSize = viewModel.listData.value.size

                    // Emit receipt with different UID
                    val newReceipt = createMockReceipt(
                        senderUid = uid2,
                        messageId = messageId,
                        deliveredAt = 1000L
                    )
                    receiptEventsFlow.emit(newReceipt)
                    advanceUntilIdle()

                    // Should add new receipt (size increased)
                    viewModel.listData.value.size shouldBe initialSize + 1
                    
                    viewModel.stopTestListener()
                }
            }
        }

        test("Property 14: Case-insensitive matching should work for mixed case UIDs") {
            runTest {
                val repository = MockMessageInformationRepository()
                val messageId = 12345L
                
                // Pre-populate with mixed case UID
                val existingReceipt = createMockReceipt(
                    senderUid = "TestUser123",
                    messageId = messageId,
                    deliveredAt = 500L,
                    readAt = 0L
                )
                repository.fetchReceiptsResult = Result.success(listOf(existingReceipt))
                val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
                val viewModel = TestableMessageInformationViewModel(
                    repository, receiptEventsFlow, enableListeners = true
                )

                // Set up GROUP message
                val message = createMockMessage(
                    messageId = messageId,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Test various case variations
                val caseVariations = listOf("testuser123", "TESTUSER123", "tEsTuSeR123")
                
                for (variation in caseVariations) {
                    val updateReceipt = createMockReceipt(
                        senderUid = variation,
                        messageId = messageId,
                        deliveredAt = 1000L,
                        readAt = 2000L
                    )
                    receiptEventsFlow.emit(updateReceipt)
                    advanceUntilIdle()
                }

                // Should still have only 1 receipt (all updates, no additions)
                viewModel.listData.value.size shouldBe 1
                
                viewModel.stopTestListener()
            }
        }
    }
})
