package com.cometchat.uikit.core.viewmodel.messagecomposer

import android.util.Log
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.state.MessageComposerUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Full chain integration test for MessageComposer.
 * Uses a FAKE DataSource → real Repository → real UseCases → real ViewModel.
 * Only the DataSource layer is faked — everything else is real.
 *
 * Validates the complete data flow from DataSource through to ViewModel state changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageComposerFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    lateinit var logMock: MockedStatic<Log>

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }
    afterTest {
        Dispatchers.resetMain()
        logMock.close()
        println()
    }

    fun buildViewModel(dataSource: MessageComposerDataSource): CometChatMessageComposerViewModel {
        println("    [WIRING] Building full chain:")
        println("    [WIRING]   DataSource (custom impl) → MessageComposerRepositoryImpl → UseCases → ViewModel")

        val repository = MessageComposerRepositoryImpl(dataSource)
        println("    [WIRING]   ✓ Repository created with custom DataSource")

        val sendTextMessageUseCase = SendTextMessageUseCase(repository)
        val sendMediaMessageUseCase = SendMediaMessageUseCase(repository)
        val sendCustomMessageUseCase = SendCustomMessageUseCase(repository)
        val editMessageUseCase = EditMessageUseCase(repository)
        println("    [WIRING]   ✓ 4 UseCases created (SendText, SendMedia, SendCustom, Edit) with Repository")

        val viewModel = CometChatMessageComposerViewModel(
            sendTextMessageUseCase = sendTextMessageUseCase,
            sendMediaMessageUseCase = sendMediaMessageUseCase,
            sendCustomMessageUseCase = sendCustomMessageUseCase,
            editMessageUseCase = editMessageUseCase,
            enableListeners = false
        )
        println("    [WIRING]   ✓ ViewModel created (enableListeners=false)")
        return viewModel
    }

    // ==================== Send Text Message Full Chain ====================

    test("full chain: sendTextMessage success → ViewModel shows Idle after Success") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(sentMessage.text).thenReturn("Hello World")
            println("    [SETUP] Created mock TextMessage(id=101, text='Hello World')")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage {
                    println("    [DataSource] sendTextMessage() called → returning sentMessage(id=101)")
                    return sentMessage
                }
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
                override suspend fun editMessage(message: BaseMessage): BaseMessage = mock()
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            println("    [CHAIN]  ViewModel.sendTextMessage('Hello World')")
            println("    [CHAIN]    → SendTextMessageUseCase.invoke(textMessage)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.sendTextMessage(textMessage)")
            println("    [CHAIN]        → DataSource.sendTextMessage(textMessage) → returned id=101")
            println("    [CHAIN]      ← Repository wraps in Result.success(TextMessage)")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → Idle + sentMessage updated")
            viewModel.sendTextMessage("Hello World")
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sentMessage
            viewModel.sentMessage.value?.id shouldBe 101

            println("    [VERIFY] State is Idle after success ✓")
            println("    [VERIFY] sentMessage.id = 101 ✓")
            println("    ✅ PASSED — sendTextMessage flowed: ViewModel → UseCase → Repo → DataSource → back up")
        }
    }

    test("full chain: sendTextMessage failure → ViewModel shows Error state") {
        runTest {
            println("    [SETUP] DataSource will throw CometChatException(code=ERR_NETWORK, message=Network unavailable)")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage {
                    println("    [DataSource] sendTextMessage() called → THROWING CometChatException(ERR_NETWORK)")
                    throw CometChatException("ERR_NETWORK", "Network unavailable", "Network unavailable")
                }
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
                override suspend fun editMessage(message: BaseMessage): BaseMessage = mock()
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            println("    [CHAIN]  ViewModel.sendTextMessage('Hello')")
            println("    [CHAIN]    → SendTextMessageUseCase.invoke(textMessage)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.sendTextMessage(textMessage)")
            println("    [CHAIN]        → DataSource.sendTextMessage(textMessage) → THREW CometChatException")
            println("    [CHAIN]      ← Repository catches exception → wraps in Result.failure(exception)")
            println("    [CHAIN]    ← UseCase passes Result.failure through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.failure → transitions to Error state")
            viewModel.sendTextMessage("Hello")
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
            (viewModel.uiState.value as MessageComposerUIState.Error).exception.code shouldBe "ERR_NETWORK"

            println("    [VERIFY] State is Error with code=ERR_NETWORK ✓")
            println("    ✅ PASSED — Exception propagated: DataSource threw → Repo wrapped → UseCase passed → ViewModel Error")
        }
    }

    // ==================== Send Custom Message Full Chain ====================

    test("full chain: sendCustomMessage success → ViewModel shows Idle") {
        runTest {
            val sentMessage = mock<CustomMessage>()
            whenever(sentMessage.id).thenReturn(303)
            println("    [SETUP] Created mock CustomMessage(id=303)")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage = mock()
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage {
                    println("    [DataSource] sendCustomMessage() called → returning sentMessage(id=303)")
                    return sentMessage
                }
                override suspend fun editMessage(message: BaseMessage): BaseMessage = mock()
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            val customMessage = mock<CustomMessage>()
            println("    [CHAIN]  ViewModel.sendCustomMessage(customMessage)")
            println("    [CHAIN]    → SendCustomMessageUseCase.invoke(customMessage)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.sendCustomMessage(customMessage)")
            println("    [CHAIN]        → DataSource.sendCustomMessage(customMessage) → returned id=303")
            println("    [CHAIN]      ← Repository wraps in Result.success(CustomMessage)")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → Idle + sentMessage updated")
            viewModel.sendCustomMessage(customMessage)
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sentMessage

            println("    [VERIFY] State is Idle after success ✓")
            println("    [VERIFY] sentMessage.id = 303 ✓")
            println("    ✅ PASSED — sendCustomMessage flowed: ViewModel → UseCase → Repo → DataSource → back up")
        }
    }

    // ==================== Edit Message Full Chain ====================

    test("full chain: editMessage success → ViewModel clears edit state and shows Idle") {
        runTest {
            val editedMessage = mock<BaseMessage>()
            whenever(editedMessage.id).thenReturn(404)
            println("    [SETUP] Created mock editedMessage(id=404)")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage = mock()
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
                override suspend fun editMessage(message: BaseMessage): BaseMessage {
                    println("    [DataSource] editMessage() called → returning editedMessage(id=404)")
                    return editedMessage
                }
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(404)
            whenever(originalMessage.receiverUid).thenReturn("user-1")
            whenever(originalMessage.receiverType).thenReturn("user")

            viewModel.setEditMessage(originalMessage)
            println("    [SETUP] setEditMessage(id=404) → state: ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()

            println("    [CHAIN]  ViewModel.editMessage('Updated content')")
            println("    [CHAIN]    → EditMessageUseCase.invoke(textMessage)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.editMessage(textMessage)")
            println("    [CHAIN]        → DataSource.editMessage(textMessage) → returned id=404")
            println("    [CHAIN]      ← Repository wraps in Result.success(BaseMessage)")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → clears editMessage, transitions to Idle")
            viewModel.editMessage("Updated content")
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            println("    [ViewModel] editMessage = ${viewModel.editMessage.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.editMessage.value shouldBe null

            println("    [VERIFY] State is Idle after edit success ✓")
            println("    [VERIFY] editMessage cleared to null ✓")
            println("    ✅ PASSED — editMessage flowed: ViewModel → UseCase → Repo → DataSource → back up, edit state cleared")
        }
    }

    test("full chain: editMessage failure → ViewModel shows Error state") {
        runTest {
            println("    [SETUP] DataSource will throw CometChatException(code=ERR_PERMISSION, message=Cannot edit)")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage = mock()
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
                override suspend fun editMessage(message: BaseMessage): BaseMessage {
                    println("    [DataSource] editMessage() called → THROWING CometChatException(ERR_PERMISSION)")
                    throw CometChatException("ERR_PERMISSION", "Cannot edit", "Cannot edit")
                }
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(404)
            whenever(originalMessage.receiverUid).thenReturn("user-1")
            whenever(originalMessage.receiverType).thenReturn("user")

            viewModel.setEditMessage(originalMessage)
            println("    [SETUP] setEditMessage(id=404) → state: Editing")

            println("    [CHAIN]  ViewModel.editMessage('Updated content')")
            println("    [CHAIN]    → EditMessageUseCase.invoke(textMessage)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.editMessage(textMessage)")
            println("    [CHAIN]        → DataSource.editMessage(textMessage) → THREW CometChatException")
            println("    [CHAIN]      ← Repository catches exception → wraps in Result.failure(exception)")
            println("    [CHAIN]    ← UseCase passes Result.failure through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.failure → transitions to Error state")
            viewModel.editMessage("Updated content")
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
            (viewModel.uiState.value as MessageComposerUIState.Error).exception.code shouldBe "ERR_PERMISSION"

            println("    [VERIFY] State is Error with code=ERR_PERMISSION ✓")
            println("    ✅ PASSED — editMessage failure propagated through entire chain")
        }
    }

    // ==================== Reply + Send Full Chain ====================

    test("full chain: sendTextMessage with reply clears replyMessage on success") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(505)
            println("    [SETUP] Created mock sentMessage(id=505)")

            val dataSource = object : MessageComposerDataSource {
                override suspend fun sendTextMessage(message: TextMessage): TextMessage {
                    println("    [DataSource] sendTextMessage() called → returning sentMessage(id=505)")
                    return sentMessage
                }
                override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
                override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
                override suspend fun editMessage(message: BaseMessage): BaseMessage = mock()
            }

            val viewModel = buildViewModel(dataSource)
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            println("    [SETUP] User set: uid=user-1")

            val replyMessage = mock<BaseMessage>()
            whenever(replyMessage.id).thenReturn(100)
            viewModel.setReplyMessage(replyMessage)
            viewModel.replyMessage.value shouldBe replyMessage
            println("    [SETUP] setReplyMessage(id=100) → state: ${viewModel.uiState.value}")

            println("    [CHAIN]  ViewModel.sendTextMessage('Reply text') with replyMessage set")
            println("    [CHAIN]    → SendTextMessageUseCase.invoke(textMessage with parentMessageId)")
            println("    [CHAIN]      → MessageComposerRepositoryImpl.sendTextMessage(textMessage)")
            println("    [CHAIN]        → DataSource.sendTextMessage(textMessage) → returned id=505")
            println("    [CHAIN]      ← Repository wraps in Result.success(TextMessage)")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → clears replyMessage, transitions to Idle")
            viewModel.sendTextMessage("Reply text")
            advanceUntilIdle()

            println("    [ViewModel] uiState = ${viewModel.uiState.value}")
            println("    [ViewModel] replyMessage = ${viewModel.replyMessage.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.replyMessage.value shouldBe null
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle

            println("    [VERIFY] replyMessage cleared to null after send ✓")
            println("    [VERIFY] State is Idle ✓")
            println("    ✅ PASSED — Reply+send flowed through chain, replyMessage cleared on success")
        }
    }
})
