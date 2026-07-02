package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for MessageComposer use cases:
 * - SendTextMessageUseCase
 * - SendMediaMessageUseCase
 * - SendCustomMessageUseCase
 * - EditMessageUseCase
 *
 * Layer 3 — Use cases are thin delegation layers.
 * Mocks the MessageComposerRepository interface, verifies forwarding.
 */
class MessageComposerUseCasesTest : FunSpec({

    // ==================== SendTextMessageUseCase ====================

    context("SendTextMessageUseCase") {
        lateinit var repository: MessageComposerRepository
        lateinit var useCase: SendTextMessageUseCase

        beforeTest {
            repository = mock()
            useCase = SendTextMessageUseCase(repository)
            println("\n  🧪 ${it.name.testName}")
            println("  ─────────────────────────────────────────────────")
        }

        afterTest {
            println()
        }

        test("invoke should delegate to repository.sendTextMessage with same message") {
            runTest {
                val message = mock<TextMessage>()
                val sentMessage = mock<TextMessage>()
                whenever(sentMessage.id).thenReturn(101)
                whenever(repository.sendTextMessage(message)).thenReturn(Result.success(sentMessage))

                println("    [SETUP] Repository mocked to return success(TextMessage id=101)")
                val result = useCase(message)

                println("    [UseCase] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe sentMessage
                verify(repository).sendTextMessage(message)
                println("    ✅ PASSED — UseCase delegated to repository and returned success")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val message = mock<TextMessage>()
                val exception = CometChatException("ERR_SEND", "Failed", "Failed")
                whenever(repository.sendTextMessage(message)).thenReturn(Result.failure(exception))

                println("    [SETUP] Repository mocked to return failure(ERR_SEND)")
                val result = useCase(message)

                println("    [UseCase] result.isFailure=${result.isFailure}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — Failure propagated unchanged")
            }
        }
    }

    // ==================== SendMediaMessageUseCase ====================

    context("SendMediaMessageUseCase") {
        lateinit var repository: MessageComposerRepository
        lateinit var useCase: SendMediaMessageUseCase

        beforeTest {
            repository = mock()
            useCase = SendMediaMessageUseCase(repository)
            println("\n  🧪 ${it.name.testName}")
            println("  ─────────────────────────────────────────────────")
        }

        afterTest {
            println()
        }

        test("invoke should delegate to repository.sendMediaMessage with same message") {
            runTest {
                val message = mock<MediaMessage>()
                val sentMessage = mock<MediaMessage>()
                whenever(sentMessage.id).thenReturn(202)
                whenever(repository.sendMediaMessage(message)).thenReturn(Result.success(sentMessage))

                println("    [SETUP] Repository mocked to return success(MediaMessage id=202)")
                val result = useCase(message)

                println("    [UseCase] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe sentMessage
                verify(repository).sendMediaMessage(message)
                println("    ✅ PASSED — UseCase delegated to repository and returned success")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val message = mock<MediaMessage>()
                val exception = CometChatException("ERR_MEDIA", "Upload failed", "Upload failed")
                whenever(repository.sendMediaMessage(message)).thenReturn(Result.failure(exception))

                println("    [SETUP] Repository mocked to return failure(ERR_MEDIA)")
                val result = useCase(message)

                println("    [UseCase] result.isFailure=${result.isFailure}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — Failure propagated unchanged")
            }
        }
    }

    // ==================== SendCustomMessageUseCase ====================

    context("SendCustomMessageUseCase") {
        lateinit var repository: MessageComposerRepository
        lateinit var useCase: SendCustomMessageUseCase

        beforeTest {
            repository = mock()
            useCase = SendCustomMessageUseCase(repository)
            println("\n  🧪 ${it.name.testName}")
            println("  ─────────────────────────────────────────────────")
        }

        afterTest {
            println()
        }

        test("invoke should delegate to repository.sendCustomMessage with same message") {
            runTest {
                val message = mock<CustomMessage>()
                val sentMessage = mock<CustomMessage>()
                whenever(sentMessage.id).thenReturn(303)
                whenever(repository.sendCustomMessage(message)).thenReturn(Result.success(sentMessage))

                println("    [SETUP] Repository mocked to return success(CustomMessage id=303)")
                val result = useCase(message)

                println("    [UseCase] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe sentMessage
                verify(repository).sendCustomMessage(message)
                println("    ✅ PASSED — UseCase delegated to repository and returned success")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val message = mock<CustomMessage>()
                val exception = CometChatException("ERR_CUSTOM", "Custom failed", "Custom failed")
                whenever(repository.sendCustomMessage(message)).thenReturn(Result.failure(exception))

                println("    [SETUP] Repository mocked to return failure(ERR_CUSTOM)")
                val result = useCase(message)

                println("    [UseCase] result.isFailure=${result.isFailure}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — Failure propagated unchanged")
            }
        }
    }

    // ==================== EditMessageUseCase ====================

    context("EditMessageUseCase") {
        lateinit var repository: MessageComposerRepository
        lateinit var useCase: EditMessageUseCase

        beforeTest {
            repository = mock()
            useCase = EditMessageUseCase(repository)
            println("\n  🧪 ${it.name.testName}")
            println("  ─────────────────────────────────────────────────")
        }

        afterTest {
            println()
        }

        test("invoke should delegate to repository.editMessage with same message") {
            runTest {
                val message = mock<TextMessage>()
                val editedMessage = mock<BaseMessage>()
                whenever(editedMessage.id).thenReturn(404)
                whenever(repository.editMessage(message)).thenReturn(Result.success(editedMessage))

                println("    [SETUP] Repository mocked to return success(BaseMessage id=404)")
                val result = useCase(message)

                println("    [UseCase] result.isSuccess=${result.isSuccess}, id=${result.getOrNull()?.id}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe editedMessage
                verify(repository).editMessage(message)
                println("    ✅ PASSED — UseCase delegated to repository and returned success")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val message = mock<TextMessage>()
                val exception = CometChatException("ERR_EDIT", "Edit failed", "Edit failed")
                whenever(repository.editMessage(message)).thenReturn(Result.failure(exception))

                println("    [SETUP] Repository mocked to return failure(ERR_EDIT)")
                val result = useCase(message)

                println("    [UseCase] result.isFailure=${result.isFailure}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — Failure propagated unchanged")
            }
        }
    }
})
