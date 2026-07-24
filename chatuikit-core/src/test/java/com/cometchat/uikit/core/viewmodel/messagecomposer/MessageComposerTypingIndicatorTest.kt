package com.cometchat.uikit.core.viewmodel.messagecomposer

import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * ENG-37022 — typing indicator session tests for [CometChatMessageComposerViewModel].
 *
 * The bug: startTyping fired on every keystroke and endTyping only fired when the
 * input became empty, so pausing mid-text left the receiver's indicator stuck until
 * app restart. The fix makes the session stateful: startTyping is sent once per
 * session and endTyping is sent on debounce expiry, explicit end, or message send.
 *
 * End-on-send is intentionally NOT a ViewModel concern (matching legacy, where the
 * post-send input clear triggers the watcher): the kotlin View's TextWatcher fires
 * endTyping on the programmatic clear, and the Compose composer calls endTyping in
 * handleSend because programmatic clears never fire onValueChange there.
 *
 * Categories:
 * A. Session start — single startTyping per session, re-start after end
 * B. Debounced end — endTyping fires after inactivity, keystrokes reset the timer
 * C. Explicit end — immediate end, no-op without an active session
 * D. Guards — disableTypingEvents, blocked user
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageComposerTypingIndicatorTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageComposerTypingIndicatorTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val debounceMs = UIKitConstants.UIKitUtilityConstants.TYPING_INDICATOR_DEBOUNCER.toLong()

    lateinit var sendTextMessageUseCase: SendTextMessageUseCase
    lateinit var sendMediaMessageUseCase: SendMediaMessageUseCase
    lateinit var sendCustomMessageUseCase: SendCustomMessageUseCase
    lateinit var editMessageUseCase: EditMessageUseCase
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    fun createViewModel(): CometChatMessageComposerViewModel = CometChatMessageComposerViewModel(
        sendTextMessageUseCase = sendTextMessageUseCase,
        sendMediaMessageUseCase = sendMediaMessageUseCase,
        sendCustomMessageUseCase = sendCustomMessageUseCase,
        editMessageUseCase = editMessageUseCase,
        enableListeners = false
    )

    fun mockUser(blockedByMe: Boolean = false, hasBlockedMe: Boolean = false): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn("user-1")
        whenever(user.isBlockedByMe).thenReturn(blockedByMe)
        whenever(user.isHasBlockedMe).thenReturn(hasBlockedMe)
        return user
    }

    fun verifyStartTyping(times: Int) =
        cometChatMock.verify({ CometChat.startTyping(any()) }, Mockito.times(times))

    fun verifyEndTyping(times: Int) =
        cometChatMock.verify({ CometChat.endTyping(any()) }, Mockito.times(times))

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        sendTextMessageUseCase = mock()
        sendMediaMessageUseCase = mock()
        sendCustomMessageUseCase = mock()
        editMessageUseCase = mock()
    }

    afterTest {
        Dispatchers.resetMain()
        cometChatMock.close()
        logMock.close()
    }

    // ==================== A. Session start ====================

    test("startTyping sends a single startTyping event per session across repeated keystrokes") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            repeat(5) {
                viewModel.startTyping()
                advanceTimeBy(debounceMs / 2)
            }

            verifyStartTyping(1)
            verifyEndTyping(0)
        }
    }

    test("startTyping after an ended session starts a new session") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            viewModel.startTyping()
            viewModel.endTyping()
            viewModel.startTyping()

            verifyStartTyping(2)
            verifyEndTyping(1)
        }
    }

    // ==================== B. Debounced end ====================

    test("endTyping fires automatically after the debounce period of inactivity") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            viewModel.startTyping()
            verifyEndTyping(0)

            advanceTimeBy(debounceMs + 1)

            verifyStartTyping(1)
            verifyEndTyping(1)
        }
    }

    test("each keystroke resets the debounce timer so no endTyping fires while typing continues") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            repeat(4) {
                viewModel.startTyping()
                advanceTimeBy(debounceMs - 100)
                verifyEndTyping(0)
            }

            advanceTimeBy(200)
            verifyStartTyping(1)
            verifyEndTyping(1)
        }
    }

    // ==================== C. Explicit end ====================

    test("explicit endTyping sends immediately and cancels the pending debounce") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            viewModel.startTyping()
            viewModel.endTyping()
            verifyEndTyping(1)

            advanceTimeBy(debounceMs * 2)
            verifyEndTyping(1)
        }
    }

    test("endTyping without an active session sends nothing") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())

            viewModel.endTyping()

            verifyStartTyping(0)
            verifyEndTyping(0)
        }
    }

    // ==================== D. Guards ====================

    test("no typing events are sent when disableTypingEvents is true") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser())
            viewModel.disableTypingEvents = true

            viewModel.startTyping()
            advanceTimeBy(debounceMs * 2)
            viewModel.endTyping()

            verifyStartTyping(0)
            verifyEndTyping(0)
        }
    }

    test("no typing events are sent when the user is blocked") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setUser(mockUser(blockedByMe = true))

            viewModel.startTyping()
            advanceTimeBy(debounceMs * 2)
            viewModel.endTyping()

            verifyStartTyping(0)
            verifyEndTyping(0)
        }
    }

})
