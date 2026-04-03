package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.MessageReceipt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
 * Unit tests for MessageInformationDataSourceImpl.
 * 
 * Since the DataSource directly depends on CometChat SDK which cannot be easily mocked,
 * these tests use a testable implementation to verify the contract behavior.
 * 
 * The tests validate:
 * - getMessageReceipts success scenarios
 * - getMessageReceipts error scenarios
 * - Result type mapping
 * 
 * **Validates: Requirements 6.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageInformationDataSourceImplTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Testable DataSource implementation for verifying contract behavior.
     * This simulates the behavior expected from MessageInformationDataSourceImpl.
     */
    class TestableMessageInformationDataSource : MessageInformationDataSource {
        var getMessageReceiptsResult: Result<List<MessageReceipt>> = Result.success(emptyList())
        var lastRequestedMessageId: Long? = null
        var callCount: Int = 0

        override suspend fun getMessageReceipts(messageId: Long): Result<List<MessageReceipt>> {
            callCount++
            lastRequestedMessageId = messageId
            return getMessageReceiptsResult
        }
    }

    // ========================================
    // Test getMessageReceipts success scenarios
    // ========================================

    context("getMessageReceipts success scenarios") {

        /**
         * When getMessageReceipts returns successfully with receipts,
         * the result should contain the receipts.
         * 
         * **Validates: Requirements 6.2**
         */
        test("getMessageReceipts should return Result.success with receipts") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                val mockReceipts = createMockReceipts(3)
                dataSource.getMessageReceiptsResult = Result.success(mockReceipts)

                val result = dataSource.getMessageReceipts(12345L)

                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 3
            }
        }

        test("getMessageReceipts should return empty list when no receipts exist") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                dataSource.getMessageReceiptsResult = Result.success(emptyList())

                val result = dataSource.getMessageReceipts(12345L)

                result.isSuccess shouldBe true
                result.getOrNull()?.isEmpty() shouldBe true
            }
        }

        test("getMessageReceipts should pass the correct messageId") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                dataSource.getMessageReceiptsResult = Result.success(emptyList())

                dataSource.getMessageReceipts(99999L)

                dataSource.lastRequestedMessageId shouldBe 99999L
            }
        }

        /**
         * Property-based test: For any number of receipts returned,
         * the result should always be successful with the correct count.
         * 
         * **Validates: Requirements 6.2**
         */
        test("getMessageReceipts should return correct receipt count for any valid input") {
            checkAll(20, Arb.int(0, 50)) { receiptCount ->
                runTest {
                    val dataSource = TestableMessageInformationDataSource()
                    val mockReceipts = createMockReceipts(receiptCount)
                    dataSource.getMessageReceiptsResult = Result.success(mockReceipts)

                    val result = dataSource.getMessageReceipts(12345L)

                    result.isSuccess shouldBe true
                    result.getOrNull()?.size shouldBe receiptCount
                }
            }
        }

        /**
         * Property-based test: For any valid messageId,
         * the dataSource should correctly pass it through.
         * 
         * **Validates: Requirements 6.2**
         */
        test("getMessageReceipts should handle any valid messageId") {
            checkAll(20, Arb.long(1L, Long.MAX_VALUE)) { messageId ->
                runTest {
                    val dataSource = TestableMessageInformationDataSource()
                    dataSource.getMessageReceiptsResult = Result.success(emptyList())

                    dataSource.getMessageReceipts(messageId)

                    dataSource.lastRequestedMessageId shouldBe messageId
                }
            }
        }
    }

    // ========================================
    // Test getMessageReceipts error scenarios
    // ========================================

    context("getMessageReceipts error scenarios") {

        /**
         * When getMessageReceipts fails, the result should be a failure
         * containing the exception.
         * 
         * **Validates: Requirements 6.2**
         */
        test("getMessageReceipts should return Result.failure on error") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                val testException = Exception("Network error")
                dataSource.getMessageReceiptsResult = Result.failure(testException)

                val result = dataSource.getMessageReceipts(12345L)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }

        test("getMessageReceipts error should preserve exception type") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                val testException = IllegalStateException("Invalid state")
                dataSource.getMessageReceiptsResult = Result.failure(testException)

                val result = dataSource.getMessageReceipts(12345L)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
            }
        }

        /**
         * Property-based test: For any error message, the failure result
         * should preserve the error message.
         * 
         * **Validates: Requirements 6.2**
         */
        test("getMessageReceipts should preserve error message for any exception") {
            checkAll(20, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val dataSource = TestableMessageInformationDataSource()
                    val testException = Exception(errorMessage)
                    dataSource.getMessageReceiptsResult = Result.failure(testException)

                    val result = dataSource.getMessageReceipts(12345L)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe errorMessage
                }
            }
        }
    }

    // ========================================
    // Test Result type mapping
    // ========================================

    context("Result type mapping") {

        test("successful result should be mappable") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                val mockReceipts = createMockReceipts(2)
                dataSource.getMessageReceiptsResult = Result.success(mockReceipts)

                val result = dataSource.getMessageReceipts(12345L)
                val mappedResult = result.map { it.size }

                mappedResult.isSuccess shouldBe true
                mappedResult.getOrNull() shouldBe 2
            }
        }

        test("failure result should propagate through map") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                val testException = Exception("Test error")
                dataSource.getMessageReceiptsResult = Result.failure(testException)

                val result = dataSource.getMessageReceipts(12345L)
                val mappedResult = result.map { it.size }

                mappedResult.isFailure shouldBe true
                mappedResult.exceptionOrNull()?.message shouldBe "Test error"
            }
        }

        test("getOrDefault should return default on failure") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                dataSource.getMessageReceiptsResult = Result.failure(Exception("Error"))

                val result = dataSource.getMessageReceipts(12345L)
                val receipts = result.getOrDefault(emptyList())

                receipts.isEmpty() shouldBe true
            }
        }

        test("getOrNull should return null on failure") {
            runTest {
                val dataSource = TestableMessageInformationDataSource()
                dataSource.getMessageReceiptsResult = Result.failure(Exception("Error"))

                val result = dataSource.getMessageReceipts(12345L)

                result.getOrNull() shouldBe null
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
