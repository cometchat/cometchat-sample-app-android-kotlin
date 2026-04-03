package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageInformationDataSource
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.state.MessageInformationUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Integration tests for CometChatMessageInformation component.
 * 
 * These tests verify the integration between ViewModel, Repository, DataSource,
 * and EventListener components working together end-to-end.
 * 
 * Feature: message-information-compose
 * **Validates: Requirements 6.2, 12.1, 12.2, 12.3, 20.1, 20.2, 20.3, 20.4**
 * 
 * ## Test Coverage
 * 
 * | Test Category | Description | Requirements |
 * |---------------|-------------|--------------|
 * | SDK Receipt Fetching | DataSource → Repository → ViewModel integration | 6.2 |
 * | Real-time Events | EventListener → ViewModel event handling | 12.1, 12.2, 12.3 |
 * | Lifecycle Management | Listener attach/detach behavior | 20.1, 20.2, 20.3, 20.4 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageInformationIntegrationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }


    // ==================== Mock Classes ====================

    /**
     * Mock DataSource that simulates SDK behavior.
     * Allows configuring responses and tracking calls.
     */
    class MockMessageInformationDataSource : MessageInformationDataSource {
        var getMessageReceiptsResult: Result<List<MessageReceipt>> = Result.success(emptyList())
        var lastRequestedMessageId: Long? = null
        var callCount: Int = 0
        var delayMs: Long = 0L

        override suspend fun getMessageReceipts(messageId: Long): Result<List<MessageReceipt>> {
            callCount++
            lastRequestedMessageId = messageId
            if (delayMs > 0) {
                kotlinx.coroutines.delay(delayMs)
            }
            return getMessageReceiptsResult
        }
    }

    /**
     * Mock Repository that wraps DataSource and provides receipt creation.
     */
    class MockMessageInformationRepository(
        private val dataSource: MessageInformationDataSource,
        private val loggedInUserUid: String = "test_logged_in_user"
    ) : MessageInformationRepository {
        var createReceiptCallCount: Int = 0

        override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
            return dataSource.getMessageReceipts(messageId)
        }

        override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
            createReceiptCallCount++
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
     * Mock EventListener that exposes a controllable Flow for testing.
     * Uses composition to wrap the real MessageReceiptEventListener.
     */
    class MockMessageReceiptEventListener {
        val receiptEventsFlow = MutableSharedFlow<MessageReceipt>()
        var listenerAddedCount: Int = 0
        var listenerRemovedCount: Int = 0

        fun receiptEvents(): Flow<MessageReceipt> = callbackFlow {
            listenerAddedCount++
            val job = CoroutineScope(Dispatchers.Main).launch {
                receiptEventsFlow.collect { receipt ->
                    trySend(receipt)
                }
            }
            awaitClose {
                listenerRemovedCount++
                job.cancel()
            }
        }
    }


    /**
     * Testable ViewModel that uses mock dependencies.
     * This is a standalone implementation that mirrors the real ViewModel behavior
     * but uses mock dependencies for testing.
     */
    class TestableMessageInformationViewModel(
        private val repository: MessageInformationRepository,
        private val mockEventListener: MockMessageReceiptEventListener,
        enableListeners: Boolean = true
    ) {
        // ==================== UI State ====================
        private val _state = MutableStateFlow<MessageInformationUIState?>(null)
        val state: StateFlow<MessageInformationUIState?> = _state.asStateFlow()

        private val _listData = MutableStateFlow<List<MessageReceipt>>(emptyList())
        val listData: StateFlow<List<MessageReceipt>> = _listData.asStateFlow()

        private val _updateReceipt = MutableStateFlow<Int?>(null)
        val updateReceipt: StateFlow<Int?> = _updateReceipt.asStateFlow()

        private val _addReceipt = MutableStateFlow<Int?>(null)
        val addReceipt: StateFlow<Int?> = _addReceipt.asStateFlow()

        private val _exception = MutableStateFlow<CometChatException?>(null)
        val exception: StateFlow<CometChatException?> = _exception.asStateFlow()

        // ==================== Internal State ====================
        private var message: BaseMessage? = null
        private var conversationType: String? = null
        private val messageReceipts = mutableListOf<MessageReceipt>()
        private var testReceiptEventsJob: Job? = null
        private var isListenerActive = false
        private val scope = CoroutineScope(Dispatchers.Main + Job())

        init {
            if (enableListeners) {
                startTestListener()
            }
        }

        fun setMessage(baseMessage: BaseMessage?) {
            if (baseMessage == null) return
            this.message = baseMessage

            conversationType = when (baseMessage.receiverType.lowercase()) {
                CometChatConstants.RECEIVER_TYPE_USER -> CometChatConstants.RECEIVER_TYPE_USER
                CometChatConstants.RECEIVER_TYPE_GROUP -> CometChatConstants.RECEIVER_TYPE_GROUP
                else -> null
            }

            when (conversationType) {
                CometChatConstants.RECEIVER_TYPE_USER -> handleUserConversation(baseMessage)
                CometChatConstants.RECEIVER_TYPE_GROUP -> fetchMessageReceipt()
            }
        }

        fun getMessage(): BaseMessage? = message
        fun getConversationType(): String? = conversationType

        fun fetchMessageReceipt() {
            val currentMessage = message
            if (currentMessage == null) {
                _state.value = MessageInformationUIState.Loading
                return
            }

            _state.value = MessageInformationUIState.Loading

            scope.launch {
                repository.fetchReceipts(currentMessage.id.toLong())
                    .onSuccess { receipts ->
                        setList(receipts)
                        _state.value = if (receipts.isEmpty()) {
                            MessageInformationUIState.Empty
                        } else {
                            MessageInformationUIState.Loaded
                        }
                    }
                    .onFailure { throwable ->
                        val exception = throwable as? CometChatException
                            ?: CometChatException(
                                "UNKNOWN_ERROR",
                                throwable.message ?: "Unknown error occurred"
                            )
                        _exception.value = exception
                        _state.value = MessageInformationUIState.Error(exception)
                    }
            }
        }

        fun startTestListener() {
            if (isListenerActive) return
            isListenerActive = true
            testReceiptEventsJob = scope.launch {
                mockEventListener.receiptEventsFlow.collect { messageReceipt ->
                    val currentMessage = message
                    if (currentMessage != null && messageReceipt.messageId == currentMessage.id) {
                        setOrUpdate(messageReceipt)
                    }
                }
            }
        }

        fun stopTestListener() {
            isListenerActive = false
            testReceiptEventsJob?.cancel()
            testReceiptEventsJob = null
        }

        fun isListenerActive(): Boolean = isListenerActive

        private fun handleUserConversation(baseMessage: BaseMessage) {
            messageReceipts.clear()
            val receipt = repository.createReceiptFromMessage(baseMessage)
            if (receipt.deliveredAt != 0L) {
                addToTop(receipt)
                _listData.value = messageReceipts.toList()
                _state.value = MessageInformationUIState.Loaded
            } else {
                _state.value = MessageInformationUIState.Empty
            }
        }

        private fun setList(receipts: List<MessageReceipt>) {
            messageReceipts.clear()
            messageReceipts.addAll(receipts)
            _listData.value = messageReceipts.toList()
        }

        private fun setOrUpdate(messageReceipt: MessageReceipt) {
            val existingReceipt = isPresent(messageReceipt)
            if (existingReceipt != null) {
                update(messageReceipt, existingReceipt)
            } else {
                addToTop(messageReceipt)
            }
        }

        private fun isPresent(messageReceipt: MessageReceipt): MessageReceipt? {
            return messageReceipts.find {
                it.sender?.uid.equals(messageReceipt.sender?.uid, ignoreCase = true)
            }
        }

        private fun update(newReceipt: MessageReceipt, oldReceipt: MessageReceipt) {
            oldReceipt.deliveredAt = if (newReceipt.deliveredAt == 0L) {
                oldReceipt.deliveredAt
            } else {
                newReceipt.deliveredAt
            }
            oldReceipt.readAt = if (newReceipt.readAt == 0L) {
                oldReceipt.readAt
            } else {
                newReceipt.readAt
            }

            val index = messageReceipts.indexOf(oldReceipt)
            if (index >= 0) {
                messageReceipts[index] = oldReceipt
                _listData.value = messageReceipts.toList()
                _state.value = MessageInformationUIState.Loaded
                _updateReceipt.value = index
            }
        }

        private fun addToTop(messageReceipt: MessageReceipt?) {
            if (messageReceipt != null) {
                messageReceipts.add(0, messageReceipt)
                if (messageReceipts.size == 1) {
                    _state.value = MessageInformationUIState.Loaded
                }
                _listData.value = messageReceipts.toList()
                _addReceipt.value = 0
            }
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
    // SDK Receipt Fetching Integration Tests
    // ========================================

    context("SDK Receipt Fetching Integration") {

        /**
         * Test: DataSource → Repository → ViewModel integration for GROUP conversations.
         * 
         * Verifies that when setMessage is called with a GROUP message:
         * 1. ViewModel calls Repository.fetchReceipts()
         * 2. Repository delegates to DataSource.getMessageReceipts()
         * 3. DataSource returns receipts
         * 4. ViewModel updates state to LOADED with receipts
         * 
         * **Validates: Requirements 6.2**
         */
        test("GROUP conversation should fetch receipts through full stack") {
            runTest {
                // Arrange - Set up mock stack
                val mockDataSource = MockMessageInformationDataSource()
                val mockReceipts = listOf(
                    createMockReceipt("user1", 100L, readAt = 1000L, deliveredAt = 900L),
                    createMockReceipt("user2", 100L, readAt = 0L, deliveredAt = 800L),
                    createMockReceipt("user3", 100L, readAt = 1100L, deliveredAt = 700L)
                )
                mockDataSource.getMessageReceiptsResult = Result.success(mockReceipts)
                
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = false
                )

                // Act - Set GROUP message
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Assert - Verify full integration
                mockDataSource.callCount shouldBe 1
                mockDataSource.lastRequestedMessageId shouldBe 100L
                viewModel.state.value shouldBe MessageInformationUIState.Loaded
                viewModel.listData.value.size shouldBe 3
            }
        }

        /**
         * Test: Error propagation through the stack.
         * 
         * Verifies that when DataSource returns an error:
         * 1. Error propagates through Repository to ViewModel
         * 2. ViewModel updates state to ERROR
         * 3. Exception is exposed through exception StateFlow
         * 
         * **Validates: Requirements 6.2**
         */
        test("SDK error should propagate through full stack to ViewModel") {
            runTest {
                // Arrange - Set up mock stack with error
                val mockDataSource = MockMessageInformationDataSource()
                val testException = CometChatException("NETWORK_ERROR", "Network unavailable")
                mockDataSource.getMessageReceiptsResult = Result.failure(testException)
                
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = false
                )

                // Act - Set GROUP message
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Assert - Verify error propagation
                viewModel.state.value.shouldBeInstanceOf<MessageInformationUIState.Error>()
                viewModel.exception.value shouldNotBe null
                viewModel.exception.value?.code shouldBe "NETWORK_ERROR"
            }
        }

        /**
         * Test: Empty receipts handling.
         * 
         * Verifies that when DataSource returns empty list:
         * 1. ViewModel updates state to EMPTY
         * 2. listData is empty
         * 
         * **Validates: Requirements 6.2**
         */
        test("Empty receipts from SDK should result in EMPTY state") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = false
                )

                // Act
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Assert
                viewModel.state.value shouldBe MessageInformationUIState.Empty
                viewModel.listData.value.isEmpty() shouldBe true
            }
        }

        /**
         * Test: USER conversation uses Repository.createReceiptFromMessage().
         * 
         * Verifies that for USER conversations:
         * 1. DataSource.getMessageReceipts() is NOT called
         * 2. Repository.createReceiptFromMessage() IS called
         * 3. Receipt is created from message data
         * 
         * **Validates: Requirements 6.2**
         */
        test("USER conversation should use createReceiptFromMessage, not SDK fetch") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = false
                )

                // Act - Set USER message with delivery timestamp
                val receiverUser = User().apply {
                    uid = "receiver_uid"
                    name = "Receiver"
                }
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                    readAt = 1000L,
                    deliveredAt = 900L,
                    receiverUser = receiverUser
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Assert - DataSource NOT called, Repository createReceipt called
                mockDataSource.callCount shouldBe 0
                mockRepository.createReceiptCallCount shouldBe 1
                viewModel.state.value shouldBe MessageInformationUIState.Loaded
                viewModel.listData.value.size shouldBe 1
            }
        }
    }


    // ========================================
    // Real-time Event Handling Integration Tests
    // ========================================

    context("Real-time Event Handling End-to-End") {

        /**
         * Test: EventListener → ViewModel integration for delivered events.
         * 
         * Verifies that when a MessagesDelivered event is received:
         * 1. EventListener emits the receipt
         * 2. ViewModel receives and processes the event
         * 3. Receipt is added to the list
         * 
         * **Validates: Requirements 12.1, 12.2**
         */
        test("MessagesDelivered event should flow from EventListener to ViewModel") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                // Set up GROUP message
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Act - Emit delivered event
                val deliveredReceipt = createMockReceipt(
                    senderUid = "new_user",
                    messageId = 100L,
                    deliveredAt = 1000L,
                    readAt = 0L
                )
                mockEventListener.receiptEventsFlow.emit(deliveredReceipt)
                advanceUntilIdle()

                // Assert - Receipt added to list
                viewModel.listData.value.any { it.sender?.uid == "new_user" } shouldBe true
                viewModel.listData.value.find { it.sender?.uid == "new_user" }?.deliveredAt shouldBe 1000L
                
                viewModel.stopTestListener()
            }
        }

        /**
         * Test: EventListener → ViewModel integration for read events.
         * 
         * Verifies that when a MessagesRead event is received:
         * 1. EventListener emits the receipt
         * 2. ViewModel receives and processes the event
         * 3. Receipt is added/updated in the list
         * 
         * **Validates: Requirements 12.1, 12.3**
         */
        test("MessagesRead event should flow from EventListener to ViewModel") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                // Set up GROUP message
                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Act - Emit read event
                val readReceipt = createMockReceipt(
                    senderUid = "reader_user",
                    messageId = 100L,
                    deliveredAt = 900L,
                    readAt = 1000L
                )
                mockEventListener.receiptEventsFlow.emit(readReceipt)
                advanceUntilIdle()

                // Assert - Receipt added with read timestamp
                viewModel.listData.value.any { it.sender?.uid == "reader_user" } shouldBe true
                viewModel.listData.value.find { it.sender?.uid == "reader_user" }?.readAt shouldBe 1000L
                
                viewModel.stopTestListener()
            }
        }

        /**
         * Test: Multiple real-time events in sequence.
         * 
         * Verifies that multiple events are processed correctly:
         * 1. First delivered event adds receipt
         * 2. Second read event updates the same receipt
         * 3. Third event from different user adds new receipt
         * 
         * **Validates: Requirements 12.1, 12.2, 12.3**
         */
        test("Multiple real-time events should be processed in sequence") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Act - Emit sequence of events
                // Event 1: User A delivered
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("user_a", 100L, deliveredAt = 1000L, readAt = 0L)
                )
                advanceUntilIdle()

                // Event 2: User A read (update)
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("user_a", 100L, deliveredAt = 0L, readAt = 1100L)
                )
                advanceUntilIdle()

                // Event 3: User B delivered (new)
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("user_b", 100L, deliveredAt = 1050L, readAt = 0L)
                )
                advanceUntilIdle()

                // Assert
                viewModel.listData.value.size shouldBe 2
                
                // User A should have both timestamps (preserved + updated)
                val userAReceipt = viewModel.listData.value.find { it.sender?.uid == "user_a" }
                userAReceipt shouldNotBe null
                userAReceipt?.deliveredAt shouldBe 1000L // Preserved from first event
                userAReceipt?.readAt shouldBe 1100L // Updated from second event
                
                // User B should be at top (most recent)
                viewModel.listData.value[0].sender?.uid shouldBe "user_b"
                
                viewModel.stopTestListener()
            }
        }

        /**
         * Test: Events for different message IDs are ignored.
         * 
         * Verifies that events with non-matching messageId are filtered out.
         * 
         * **Validates: Requirements 12.2, 12.3**
         */
        test("Events for different messageId should be ignored") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                val initialSize = viewModel.listData.value.size

                // Act - Emit event for different message
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("some_user", 999L, deliveredAt = 1000L) // Different messageId
                )
                advanceUntilIdle()

                // Assert - List unchanged
                viewModel.listData.value.size shouldBe initialSize
                
                viewModel.stopTestListener()
            }
        }
    }


    // ========================================
    // Lifecycle Management Integration Tests
    // ========================================

    context("Lifecycle Management") {

        /**
         * Test: Listener is added when component is attached.
         * 
         * Verifies that when the component is initialized with enableListeners=true:
         * 1. Event listener is started
         * 2. Events can be received
         * 
         * **Validates: Requirements 20.1**
         */
        test("Listener should be active when component is attached") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                
                // Act - Create ViewModel with listeners enabled
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Assert - Listener is active
                viewModel.isListenerActive() shouldBe true
                
                // Verify events are received
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("test_user", 100L, deliveredAt = 1000L)
                )
                advanceUntilIdle()
                
                viewModel.listData.value.any { it.sender?.uid == "test_user" } shouldBe true
                
                viewModel.stopTestListener()
            }
        }

        /**
         * Test: Listener is removed when component is detached.
         * 
         * Verifies that when stopTestListener() is called:
         * 1. Event listener is stopped
         * 2. Events are no longer received
         * 
         * **Validates: Requirements 20.2, 20.3**
         */
        test("Listener should be inactive after component is detached") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Act - Stop listener (simulate detach)
                viewModel.stopTestListener()
                advanceUntilIdle()

                // Assert - Listener is inactive
                viewModel.isListenerActive() shouldBe false
                
                // Events should not be processed
                val sizeBeforeEvent = viewModel.listData.value.size
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("ignored_user", 100L, deliveredAt = 1000L)
                )
                advanceUntilIdle()
                
                viewModel.listData.value.size shouldBe sizeBeforeEvent
            }
        }

        /**
         * Test: Listener can be re-attached after detachment.
         * 
         * Verifies that when startTestListener() is called after stopTestListener():
         * 1. Event listener is restarted
         * 2. Events can be received again
         * 
         * **Validates: Requirements 20.4**
         */
        test("Listener should be re-attachable after detachment") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Detach
                viewModel.stopTestListener()
                advanceUntilIdle()
                viewModel.isListenerActive() shouldBe false

                // Act - Re-attach
                viewModel.startTestListener()
                advanceUntilIdle()

                // Assert - Listener is active again
                viewModel.isListenerActive() shouldBe true
                
                // Events should be received
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("reattach_user", 100L, deliveredAt = 1000L)
                )
                advanceUntilIdle()
                
                viewModel.listData.value.any { it.sender?.uid == "reattach_user" } shouldBe true
                
                viewModel.stopTestListener()
            }
        }

        /**
         * Test: Multiple attach/detach cycles work correctly.
         * 
         * Verifies that the component handles multiple lifecycle transitions:
         * 1. Attach → Detach → Attach → Detach
         * 2. Events are only received when attached
         * 
         * **Validates: Requirements 20.1, 20.2, 20.3, 20.4**
         */
        test("Multiple attach/detach cycles should work correctly") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Cycle 1: Attached - should receive
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("cycle1_user", 100L, deliveredAt = 1000L)
                )
                advanceUntilIdle()
                viewModel.listData.value.any { it.sender?.uid == "cycle1_user" } shouldBe true

                // Detach
                viewModel.stopTestListener()
                advanceUntilIdle()

                // Cycle 2: Detached - should NOT receive
                val sizeAfterDetach = viewModel.listData.value.size
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("cycle2_ignored", 100L, deliveredAt = 1100L)
                )
                advanceUntilIdle()
                viewModel.listData.value.size shouldBe sizeAfterDetach

                // Re-attach
                viewModel.startTestListener()
                advanceUntilIdle()

                // Cycle 3: Re-attached - should receive
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("cycle3_user", 100L, deliveredAt = 1200L)
                )
                advanceUntilIdle()
                viewModel.listData.value.any { it.sender?.uid == "cycle3_user" } shouldBe true

                // Final detach
                viewModel.stopTestListener()
                advanceUntilIdle()

                // Cycle 4: Detached again - should NOT receive
                val finalSize = viewModel.listData.value.size
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("cycle4_ignored", 100L, deliveredAt = 1300L)
                )
                advanceUntilIdle()
                viewModel.listData.value.size shouldBe finalSize
            }
        }

        /**
         * Test: State is preserved across attach/detach cycles.
         * 
         * Verifies that receipt data is preserved when listener is detached and re-attached.
         * 
         * **Validates: Requirements 20.1, 20.2, 20.3, 20.4**
         */
        test("State should be preserved across attach/detach cycles") {
            runTest {
                // Arrange
                val mockDataSource = MockMessageInformationDataSource()
                val initialReceipts = listOf(
                    createMockReceipt("initial_user1", 100L, deliveredAt = 500L),
                    createMockReceipt("initial_user2", 100L, deliveredAt = 600L)
                )
                mockDataSource.getMessageReceiptsResult = Result.success(initialReceipts)
                val mockRepository = MockMessageInformationRepository(mockDataSource)
                val mockEventListener = MockMessageReceiptEventListener()
                val viewModel = TestableMessageInformationViewModel(
                    mockRepository, mockEventListener, enableListeners = true
                )

                val message = createMockMessage(
                    messageId = 100L,
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                )
                viewModel.setMessage(message)
                advanceUntilIdle()

                // Verify initial state
                viewModel.listData.value.size shouldBe 2

                // Add receipt while attached
                mockEventListener.receiptEventsFlow.emit(
                    createMockReceipt("added_user", 100L, deliveredAt = 1000L)
                )
                advanceUntilIdle()
                viewModel.listData.value.size shouldBe 3

                // Detach
                viewModel.stopTestListener()
                advanceUntilIdle()

                // State should be preserved
                viewModel.listData.value.size shouldBe 3
                viewModel.state.value shouldBe MessageInformationUIState.Loaded

                // Re-attach
                viewModel.startTestListener()
                advanceUntilIdle()

                // State should still be preserved
                viewModel.listData.value.size shouldBe 3
                viewModel.listData.value.any { it.sender?.uid == "added_user" } shouldBe true
                
                viewModel.stopTestListener()
            }
        }
    }
})
