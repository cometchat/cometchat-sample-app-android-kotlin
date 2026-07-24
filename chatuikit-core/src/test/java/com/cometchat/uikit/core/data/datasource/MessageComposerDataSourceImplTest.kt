package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for MessageComposerDataSource interface contract.
 * Layer 1 — Verifies the DataSource interface behavior using a fake implementation.
 *
 * Since CometChatUIKit and CometChat static methods cannot be mocked in JVM tests
 * (they require Android framework initialization), we test the DataSource CONTRACT
 * using a controllable fake implementation. This validates that:
 * - The interface methods return correct types on success
 * - The interface methods propagate exceptions on failure
 * - The interface methods handle empty/null scenarios correctly
 *
 * The actual CometChatUIKit integration is verified via instrumented tests.
 */
class MessageComposerDataSourceImplTest : FunSpec({

    // ==================== Fake DataSource for Testing ====================

    /**
     * Controllable fake DataSource that allows us to configure success/failure behavior.
     */
    class FakeMessageComposerDataSource(
        private val textMessageResult: Result<TextMessage> = Result.success(mock()),
        private val mediaMessageResult: Result<MediaMessage> = Result.success(mock()),
        private val customMessageResult: Result<CustomMessage> = Result.success(mock()),
        private val editMessageResult: Result<BaseMessage> = Result.success(mock())
    ) : MessageComposerDataSource {

        override suspend fun sendTextMessage(message: TextMessage): TextMessage {
            return textMessageResult.getOrThrow()
        }

        override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage {
            return mediaMessageResult.getOrThrow()
        }

        override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage {
            return customMessageResult.getOrThrow()
        }

        override suspend fun editMessage(message: BaseMessage): BaseMessage {
            return editMessageResult.getOrThrow()
        }
    }

    beforeTest {
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        println()
    }

    // ==================== sendTextMessage ====================

    test("sendTextMessage should return TextMessage on success") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(sentMessage.text).thenReturn("Hello World")

            val dataSource = FakeMessageComposerDataSource(
                textMessageResult = Result.success(sentMessage)
            )

            println("    [SETUP] FakeDataSource configured with success(TextMessage id=101)")
            val result = dataSource.sendTextMessage(mock())

            println("    [DataSource] result: id=${result.id}, text=${result.text}")
            result shouldBe sentMessage
            result.id shouldBe 101
            result.text shouldBe "Hello World"
            println("    ✅ PASSED — sendTextMessage returned correct TextMessage")
        }
    }

    test("sendTextMessage should throw CometChatException on failure") {
        runTest {
            val exception = CometChatException("ERR_SEND", "Network error", "Network error")

            val dataSource = FakeMessageComposerDataSource(
                textMessageResult = Result.failure(exception)
            )

            println("    [SETUP] FakeDataSource configured with failure(ERR_SEND)")
            val thrown = shouldThrow<CometChatException> {
                dataSource.sendTextMessage(mock())
            }
            println("    [DataSource] threw: code=${thrown.code}")
            thrown.code shouldBe "ERR_SEND"
            println("    ✅ PASSED — CometChatException thrown with correct code")
        }
    }

    // ==================== sendMediaMessage ====================

    test("sendMediaMessage should return MediaMessage on success") {
        runTest {
            val sentMessage = mock<MediaMessage>()
            whenever(sentMessage.id).thenReturn(202)

            val dataSource = FakeMessageComposerDataSource(
                mediaMessageResult = Result.success(sentMessage)
            )

            println("    [SETUP] FakeDataSource configured with success(MediaMessage id=202)")
            val result = dataSource.sendMediaMessage(mock())

            println("    [DataSource] result: id=${result.id}")
            result shouldBe sentMessage
            result.id shouldBe 202
            println("    ✅ PASSED — sendMediaMessage returned correct MediaMessage")
        }
    }

    test("sendMediaMessage should throw CometChatException on failure") {
        runTest {
            val exception = CometChatException("ERR_MEDIA", "Upload failed", "Upload failed")

            val dataSource = FakeMessageComposerDataSource(
                mediaMessageResult = Result.failure(exception)
            )

            println("    [SETUP] FakeDataSource configured with failure(ERR_MEDIA)")
            val thrown = shouldThrow<CometChatException> {
                dataSource.sendMediaMessage(mock())
            }
            println("    [DataSource] threw: code=${thrown.code}")
            thrown.code shouldBe "ERR_MEDIA"
            println("    ✅ PASSED — CometChatException thrown with correct code")
        }
    }

    // ==================== sendCustomMessage ====================

    test("sendCustomMessage should return CustomMessage on success") {
        runTest {
            val sentMessage = mock<CustomMessage>()
            whenever(sentMessage.id).thenReturn(303)

            val dataSource = FakeMessageComposerDataSource(
                customMessageResult = Result.success(sentMessage)
            )

            println("    [SETUP] FakeDataSource configured with success(CustomMessage id=303)")
            val result = dataSource.sendCustomMessage(mock())

            println("    [DataSource] result: id=${result.id}")
            result shouldBe sentMessage
            result.id shouldBe 303
            println("    ✅ PASSED — sendCustomMessage returned correct CustomMessage")
        }
    }

    test("sendCustomMessage should throw CometChatException on failure") {
        runTest {
            val exception = CometChatException("ERR_CUSTOM", "Custom send failed", "Custom send failed")

            val dataSource = FakeMessageComposerDataSource(
                customMessageResult = Result.failure(exception)
            )

            println("    [SETUP] FakeDataSource configured with failure(ERR_CUSTOM)")
            val thrown = shouldThrow<CometChatException> {
                dataSource.sendCustomMessage(mock())
            }
            println("    [DataSource] threw: code=${thrown.code}")
            thrown.code shouldBe "ERR_CUSTOM"
            println("    ✅ PASSED — CometChatException thrown with correct code")
        }
    }

    // ==================== editMessage ====================

    test("editMessage should return BaseMessage on success") {
        runTest {
            val editedMessage = mock<BaseMessage>()
            whenever(editedMessage.id).thenReturn(404)

            val dataSource = FakeMessageComposerDataSource(
                editMessageResult = Result.success(editedMessage)
            )

            println("    [SETUP] FakeDataSource configured with success(BaseMessage id=404)")
            val result = dataSource.editMessage(mock())

            println("    [DataSource] result: id=${result.id}")
            result shouldBe editedMessage
            result.id shouldBe 404
            println("    ✅ PASSED — editMessage returned correct BaseMessage")
        }
    }

    test("editMessage should throw CometChatException on failure") {
        runTest {
            val exception = CometChatException("ERR_EDIT", "Edit failed", "Edit failed")

            val dataSource = FakeMessageComposerDataSource(
                editMessageResult = Result.failure(exception)
            )

            println("    [SETUP] FakeDataSource configured with failure(ERR_EDIT)")
            val thrown = shouldThrow<CometChatException> {
                dataSource.editMessage(mock())
            }
            println("    [DataSource] threw: code=${thrown.code}")
            thrown.code shouldBe "ERR_EDIT"
            println("    ✅ PASSED — CometChatException thrown with correct code")
        }
    }
})
