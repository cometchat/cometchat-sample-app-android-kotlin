package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for MessageComposerRepositoryImpl.
 * Layer 2 — wraps DataSource calls in Result<T> for proper error handling.
 * Mocks the MessageComposerDataSource interface — no SDK involved.
 *
 * Covers:
 * - sendTextMessage: success wrapping, CometChatException wrapping, general Exception wrapping
 * - sendMediaMessage: success wrapping, failure wrapping
 * - sendCustomMessage: success wrapping, failure wrapping
 * - editMessage: success wrapping, failure wrapping
 */
class MessageComposerRepositoryImplTest : FunSpec({

    lateinit var dataSource: MessageComposerDataSource
    lateinit var repository: MessageComposerRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = MessageComposerRepositoryImpl(dataSource)
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        println()
    }

    // ==================== sendTextMessage ====================

    test("sendTextMessage should delegate to dataSource and wrap in Result.success") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(dataSource.sendTextMessage(inputMessage)).thenReturn(sentMessage)

            println("    [SETUP] DataSource mocked to return TextMessage(id=101)")
            val result = repository.sendTextMessage(inputMessage)

            println("    [Repository] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe sentMessage
            verify(dataSource).sendTextMessage(inputMessage)
            println("    ✅ PASSED — DataSource result wrapped in Result.success")
        }
    }

    test("sendTextMessage should wrap CometChatException in Result.failure") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val exception = CometChatException("ERR_SEND", "Network error", "Network error")
            whenever(dataSource.sendTextMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw CometChatException(ERR_SEND)")
            val result = repository.sendTextMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}, code=${(result.exceptionOrNull() as? CometChatException)?.code}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — CometChatException wrapped in Result.failure")
        }
    }

    test("sendTextMessage should wrap general Exception in Result.failure") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val exception = RuntimeException("Unexpected error")
            whenever(dataSource.sendTextMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw RuntimeException")
            val result = repository.sendTextMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — General exception wrapped in Result.failure")
        }
    }

    // ==================== sendMediaMessage ====================

    test("sendMediaMessage should delegate to dataSource and wrap in Result.success") {
        runTest {
            val inputMessage = mock<MediaMessage>()
            val sentMessage = mock<MediaMessage>()
            whenever(sentMessage.id).thenReturn(202)
            whenever(dataSource.sendMediaMessage(inputMessage)).thenReturn(sentMessage)

            println("    [SETUP] DataSource mocked to return MediaMessage(id=202)")
            val result = repository.sendMediaMessage(inputMessage)

            println("    [Repository] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe sentMessage
            verify(dataSource).sendMediaMessage(inputMessage)
            println("    ✅ PASSED — DataSource result wrapped in Result.success")
        }
    }

    test("sendMediaMessage should wrap CometChatException in Result.failure") {
        runTest {
            val inputMessage = mock<MediaMessage>()
            val exception = CometChatException("ERR_MEDIA", "Upload failed", "Upload failed")
            whenever(dataSource.sendMediaMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw CometChatException(ERR_MEDIA)")
            val result = repository.sendMediaMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — CometChatException wrapped in Result.failure")
        }
    }

    test("sendMediaMessage should wrap general Exception in Result.failure") {
        runTest {
            val inputMessage = mock<MediaMessage>()
            val exception = IllegalStateException("File not found")
            whenever(dataSource.sendMediaMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw IllegalStateException")
            val result = repository.sendMediaMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — General exception wrapped in Result.failure")
        }
    }

    // ==================== sendCustomMessage ====================

    test("sendCustomMessage should delegate to dataSource and wrap in Result.success") {
        runTest {
            val inputMessage = mock<CustomMessage>()
            val sentMessage = mock<CustomMessage>()
            whenever(sentMessage.id).thenReturn(303)
            whenever(dataSource.sendCustomMessage(inputMessage)).thenReturn(sentMessage)

            println("    [SETUP] DataSource mocked to return CustomMessage(id=303)")
            val result = repository.sendCustomMessage(inputMessage)

            println("    [Repository] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe sentMessage
            verify(dataSource).sendCustomMessage(inputMessage)
            println("    ✅ PASSED — DataSource result wrapped in Result.success")
        }
    }

    test("sendCustomMessage should wrap CometChatException in Result.failure") {
        runTest {
            val inputMessage = mock<CustomMessage>()
            val exception = CometChatException("ERR_CUSTOM", "Custom send failed", "Custom send failed")
            whenever(dataSource.sendCustomMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw CometChatException(ERR_CUSTOM)")
            val result = repository.sendCustomMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — CometChatException wrapped in Result.failure")
        }
    }

    // ==================== editMessage ====================

    test("editMessage should delegate to dataSource and wrap in Result.success") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val editedMessage = mock<BaseMessage>()
            whenever(editedMessage.id).thenReturn(404)
            whenever(dataSource.editMessage(inputMessage)).thenReturn(editedMessage)

            println("    [SETUP] DataSource mocked to return BaseMessage(id=404)")
            val result = repository.editMessage(inputMessage)

            println("    [Repository] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe editedMessage
            verify(dataSource).editMessage(inputMessage)
            println("    ✅ PASSED — DataSource result wrapped in Result.success")
        }
    }

    test("editMessage should wrap CometChatException in Result.failure") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val exception = CometChatException("ERR_EDIT", "Edit failed", "Edit failed")
            whenever(dataSource.editMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw CometChatException(ERR_EDIT)")
            val result = repository.editMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — CometChatException wrapped in Result.failure")
        }
    }

    test("editMessage should wrap general Exception in Result.failure") {
        runTest {
            val inputMessage = mock<TextMessage>()
            val exception = RuntimeException("Unexpected edit error")
            whenever(dataSource.editMessage(inputMessage)).thenAnswer { throw exception }

            println("    [SETUP] DataSource will throw RuntimeException")
            val result = repository.editMessage(inputMessage)

            println("    [Repository] result.isFailure=${result.isFailure}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — General exception wrapped in Result.failure")
        }
    }
})
