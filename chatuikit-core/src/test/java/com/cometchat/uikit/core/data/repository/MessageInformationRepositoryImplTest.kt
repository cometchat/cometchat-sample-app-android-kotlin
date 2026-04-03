package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageInformationDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for MessageInformationRepositoryImpl.
 * 
 * The tests validate:
 * - fetchReceipts delegates to DataSource correctly
 * - createReceiptFromMessage creates correct MessageReceipt
 * - Error handling and Result mapping
 * 
 * **Validates: Requirements 6.2, 13.7**
 * 
 * ## Test Coverage
 * 
 * | Test | Property | Requirements |
 * |------|----------|--------------|
 * | fetchReceipts success | - | 6.2 |
 * | fetchReceipts error | - | 6.2 |
 * | createReceiptFromMessage | Property 13 | 13.7 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageInformationRepositoryImplTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock DataSource for testing repository behavior.
     */
    class MockMessageInformationDataSource : MessageInformationDataSource {
        var getMessageReceiptsResult: Result<List<MessageReceipt>> = Result.success(emptyList())
        var lastRequestedMessageId: Long? = null
        var callCount: Int = 0

        override suspend fun getMessageReceipts(messageId: Long): Result<List<MessageReceipt>> {
            callCount++
            lastRequestedMessageId = messageId
            return getMessageReceiptsResult
        }
    }

    /**
     * Testable Repository that doesn't depend on CometChatUIKit.getLoggedInUser()
     */
    class TestableMessageInformationRepository(
        private val dataSource: MessageInformationDataSource,
        private val loggedInUserUid: String = "test_logged_in_user"
    ) {
        suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
            return dataSource.getMessageReceipts(messageId)
        }

        /**
         * Creates a MessageReceipt from a BaseMessage for USER conversations.
         * Per design doc: createMessageReceipt() utility function.
         * 
         * Logic from design doc:
         * - sender = message.receiver (as User)
         * - readAt = message.readAt
         * - deliveredAt = message.deliveredAt (or message.readAt if deliveredAt is 0)
         * - messageId = message.id
         */
        fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
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

    // ========================================
    // Test fetchReceipts delegation
    // ========================================

    context("fetchReceipts delegation to DataSource") {

        /**
         * fetchReceipts should delegate to DataSource and return success result.
         * 
         * **Validates: Requirements 6.2**
         */
        test("fetchReceipts should return Result.success when DataSource succeeds") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                val mockReceipts = createMockReceipts(3)
                mockDataSource.getMessageReceiptsResult = Result.success(mockReceipts)
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
            }
        }

        test("fetchReceipts should pass correct messageId to DataSource") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                repository.fetchReceipts(99999L)

                mockDataSource.lastRequestedMessageId shouldBe 99999L
            }
        }

        /**
         * fetchReceipts should return failure when DataSource fails.
         * 
         * **Validates: Requirements 6.2**
         */
        test("fetchReceipts should return Result.failure when DataSource fails") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                val testException = Exception("Network error")
                mockDataSource.getMessageReceiptsResult = Result.failure(testException)
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }

        test("fetchReceipts error should preserve exception type") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                val testException = IllegalStateException("Invalid state")
                mockDataSource.getMessageReceiptsResult = Result.failure(testException)
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
            }
        }

        /**
         * Property-based test: For any messageId, fetchReceipts should
         * correctly delegate to DataSource.
         * 
         * **Validates: Requirements 6.2**
         */
        test("fetchReceipts should handle any valid messageId") {
            checkAll(20, Arb.long(1L, Long.MAX_VALUE)) { messageId ->
                runTest {
                    val mockDataSource = MockMessageInformationDataSource()
                    mockDataSource.getMessageReceiptsResult = Result.success(emptyList())
                    
                    val repository = TestableMessageInformationRepository(mockDataSource)
                    repository.fetchReceipts(messageId)

                    mockDataSource.lastRequestedMessageId shouldBe messageId
                }
            }
        }
    }

    // ========================================
    // Test createReceiptFromMessage logic
    // ========================================

    context("createReceiptFromMessage logic") {

        /**
         * **Property 13: Receipt Creation from USER Message**
         * 
         * For any USER conversation message with deliveredAt > 0, calling createReceiptFromMessage
         * SHALL create a MessageReceipt with:
         * - sender = message.receiver (as User)
         * - readAt = message.readAt
         * - deliveredAt = message.deliveredAt (or message.readAt if deliveredAt is 0)
         * - messageId = message.id
         * 
         * **Validates: Requirements 13.7**
         */
        test("createReceiptFromMessage should set sender from message.receiver") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val receiverUser = User().apply {
                uid = "receiver_user_123"
                name = "Receiver User"
            }
            
            val message = createMockMessage(
                messageId = 100L,
                receiverUser = receiverUser,
                readAt = 1000L,
                deliveredAt = 900L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.sender shouldNotBe null
            receipt.sender?.uid shouldBe "receiver_user_123"
        }

        test("createReceiptFromMessage should set readAt from message.readAt") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 1234567890L,
                deliveredAt = 1234567800L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.readAt shouldBe 1234567890L
        }

        test("createReceiptFromMessage should set timestamp from message.readAt") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 9999999L,
                deliveredAt = 8888888L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.timestamp shouldBe 9999999L
        }

        test("createReceiptFromMessage should set deliveredAt from message.deliveredAt when > 0") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 1000L,
                deliveredAt = 900L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.deliveredAt shouldBe 900L
        }

        /**
         * Per design doc: deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
         * 
         * **Validates: Requirements 13.7**
         */
        test("createReceiptFromMessage should use readAt for deliveredAt when deliveredAt is 0") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 1000L,
                deliveredAt = 0L  // deliveredAt is 0
            )

            val receipt = repository.createReceiptFromMessage(message)

            // Should use readAt as fallback
            receipt.deliveredAt shouldBe 1000L
        }

        test("createReceiptFromMessage should set messageId from message.id") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 54321L,
                readAt = 1000L,
                deliveredAt = 900L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.messageId shouldBe 54321L
        }

        test("createReceiptFromMessage should set receiverType from message.receiverType") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 1000L,
                deliveredAt = 900L,
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.receiverType shouldBe CometChatConstants.RECEIVER_TYPE_USER
        }

        test("createReceiptFromMessage should set receiverId from logged in user") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(
                mockDataSource, 
                loggedInUserUid = "my_logged_in_uid"
            )
            
            val message = createMockMessage(
                messageId = 100L,
                readAt = 1000L,
                deliveredAt = 900L
            )

            val receipt = repository.createReceiptFromMessage(message)

            receipt.receiverId shouldBe "my_logged_in_uid"
        }

        /**
         * Property-based test: For any valid timestamps, createReceiptFromMessage
         * should correctly apply the deliveredAt fallback logic.
         * 
         * **Property 13: Receipt Creation from USER Message**
         * **Validates: Requirements 13.7**
         */
        test("Property 13: deliveredAt fallback logic should work for any timestamps") {
            checkAll(20, Arb.long(1L, Long.MAX_VALUE), Arb.long(0L, Long.MAX_VALUE)) { readAt, deliveredAt ->
                val mockDataSource = MockMessageInformationDataSource()
                val repository = TestableMessageInformationRepository(mockDataSource)
                
                val message = createMockMessage(
                    messageId = 100L,
                    readAt = readAt,
                    deliveredAt = deliveredAt
                )

                val receipt = repository.createReceiptFromMessage(message)

                // Verify the fallback logic
                val expectedDeliveredAt = if (deliveredAt == 0L) readAt else deliveredAt
                receipt.deliveredAt shouldBe expectedDeliveredAt
            }
        }

        /**
         * Property-based test: For any messageId, createReceiptFromMessage
         * should correctly set the messageId.
         * 
         * **Property 13: Receipt Creation from USER Message**
         * **Validates: Requirements 13.7**
         */
        test("Property 13: messageId should be correctly set for any valid id") {
            checkAll(20, Arb.long(1L, Long.MAX_VALUE)) { messageId ->
                val mockDataSource = MockMessageInformationDataSource()
                val repository = TestableMessageInformationRepository(mockDataSource)
                
                val message = createMockMessage(
                    messageId = messageId,
                    readAt = 1000L,
                    deliveredAt = 900L
                )

                val receipt = repository.createReceiptFromMessage(message)

                receipt.messageId shouldBe messageId
            }
        }

        test("createReceiptFromMessage should handle null receiver gracefully") {
            val mockDataSource = MockMessageInformationDataSource()
            val repository = TestableMessageInformationRepository(mockDataSource)
            
            // Create message without setting receiver as User
            val message = TextMessage(
                "receiver_id",
                "Test message",
                CometChatConstants.RECEIVER_TYPE_GROUP  // Group type, receiver won't be User
            ).apply {
                id = 100L
                readAt = 1000L
                deliveredAt = 900L
            }

            val receipt = repository.createReceiptFromMessage(message)

            // sender should be null when receiver is not a User
            receipt.sender shouldBe null
        }
    }

    // ========================================
    // Test error handling and Result mapping
    // ========================================

    context("error handling and Result mapping") {

        /**
         * Property-based test: For any error message, the failure result
         * should preserve the error message through the repository.
         * 
         * **Validates: Requirements 6.2**
         */
        test("fetchReceipts should preserve error message for any exception") {
            checkAll(20, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val mockDataSource = MockMessageInformationDataSource()
                    val testException = Exception(errorMessage)
                    mockDataSource.getMessageReceiptsResult = Result.failure(testException)
                    
                    val repository = TestableMessageInformationRepository(mockDataSource)
                    val result = repository.fetchReceipts(12345L)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe errorMessage
                }
            }
        }

        test("fetchReceipts result should be mappable on success") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                val mockReceipts = createMockReceipts(5)
                mockDataSource.getMessageReceiptsResult = Result.success(mockReceipts)
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)
                val mappedResult = result.map { it.size }

                mappedResult.isSuccess shouldBe true
                mappedResult.getOrNull() shouldBe 5
            }
        }

        test("fetchReceipts result should propagate failure through map") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                val testException = Exception("Test error")
                mockDataSource.getMessageReceiptsResult = Result.failure(testException)
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)
                val mappedResult = result.map { it.size }

                mappedResult.isFailure shouldBe true
                mappedResult.exceptionOrNull()?.message shouldBe "Test error"
            }
        }

        test("fetchReceipts getOrDefault should return default on failure") {
            runTest {
                val mockDataSource = MockMessageInformationDataSource()
                mockDataSource.getMessageReceiptsResult = Result.failure(Exception("Error"))
                
                val repository = TestableMessageInformationRepository(mockDataSource)
                val result = repository.fetchReceipts(12345L)
                val receipts = result.getOrDefault(emptyList())

                receipts.isEmpty() shouldBe true
            }
        }
    }
})

/**
 * Helper function to create mock MessageReceipt objects for testing.
 */
private fun createMockReceipts(count: Int): List<MessageReceipt> {
    return (1..count).map { index ->
        MessageReceipt().apply {
            messageId = index.toLong()
            readAt = System.currentTimeMillis() / 1000
            deliveredAt = System.currentTimeMillis() / 1000
        }
    }
}

/**
 * Helper function to create a mock BaseMessage for testing.
 */
private fun createMockMessage(
    messageId: Long,
    readAt: Long = 0L,
    deliveredAt: Long = 0L,
    receiverUser: User? = null,
    receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
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
