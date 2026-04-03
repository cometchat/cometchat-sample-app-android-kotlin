package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Bug Condition Exploration Test for CometChatOutgoingCallViewModel.
 * 
 * **Property 1: Fault Condition** - onCallEndedMessageReceived Not Handled
 * 
 * This test validates that when `onCallEndedMessageReceived` is triggered with a call
 * whose session ID matches the current outgoing call, the `rejectedCall` StateFlow
 * should emit the call to trigger screen dismissal.
 * 
 * **CRITICAL**: This test is EXPECTED TO FAIL on unfixed code - failure confirms the bug exists.
 * 
 * **Bug Description**: The `CometChatOutgoingCallViewModel` only listens for 
 * `onOutgoingCallAccepted` and `onOutgoingCallRejected` but NOT `onCallEndedMessageReceived`.
 * When a call times out, is busy, or ends due to system termination, 
 * `onCallEndedMessageReceived` is fired but not handled, leaving the screen visible.
 * 
 * **Validates: Requirements 1.2, 1.3, 1.4, 2.2, 2.3, 2.4**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatOutgoingCallViewModelBugTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Testable ViewModel that exposes the ability to simulate SDK call events.
     * 
     * This extends the real ViewModel and provides methods to simulate
     * `onCallEndedMessageReceived` events that would normally come from the SDK.
     */
    class TestableOutgoingCallViewModel : CometChatOutgoingCallViewModel(enableListeners = false) {
        
        /**
         * Simulates the `onCallEndedMessageReceived` SDK callback.
         * 
         * In the real implementation, this would be called by the CometChat SDK
         * when a call ends due to timeout, busy status, or system termination.
         * 
         * The bug is that this callback is NOT implemented in the ViewModel,
         * so calling this method should have NO EFFECT on unfixed code.
         * 
         * After the fix, this should emit the call through `rejectedCall` StateFlow
         * if the session ID matches the current outgoing call.
         */
        fun simulateOnCallEndedMessageReceived(call: Call) {
            // This simulates what the SDK would do - call the listener method
            // The ViewModel should handle this by emitting rejectedCall
            // 
            // On UNFIXED code: This does nothing because the handler doesn't exist
            // On FIXED code: This should call handleCallEndedMessageReceived(call)
            //
            // We need to directly invoke the handler that SHOULD exist
            // Since it doesn't exist in unfixed code, we use reflection to check
            // Note: The method is in the superclass CometChatOutgoingCallViewModel
            try {
                val method = CometChatOutgoingCallViewModel::class.java.getDeclaredMethod(
                    "handleCallEndedMessageReceived",
                    Call::class.java
                )
                method.isAccessible = true
                method.invoke(this, call)
            } catch (e: NoSuchMethodException) {
                // Method doesn't exist - this is the BUG!
                // The handler is missing, so rejectedCall will NOT be emitted
            }
        }
        
        /**
         * Exposes the current rejectedCall value for testing.
         */
        fun getRejectedCallValue(): Call? = rejectedCall.value
        
        /**
         * Exposes the current call value for testing.
         */
        fun getCallValue(): Call? = call.value
    }

    /**
     * Helper function to create a test call with a specific session ID.
     */
    fun createTestCall(
        sessionId: String,
        receiverUid: String = "test_receiver",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        callStatus: String = CometChatConstants.CALL_STATUS_ENDED
    ): Call {
        val call = Call(receiverUid, receiverType, CometChatConstants.CALL_TYPE_AUDIO)
        call.sessionId = sessionId
        call.callStatus = callStatus
        
        val initiator = User().apply { uid = "test_initiator" }
        call.callInitiator = initiator
        
        return call
    }

    // ========================================
    // Bug Condition Exploration Tests
    // ========================================

    context("Bug Condition: onCallEndedMessageReceived with matching session ID") {

        /**
         * **Property 1: Fault Condition Test**
         * 
         * WHEN `onCallEndedMessageReceived` is triggered with a call whose session ID
         * matches the current outgoing call's session ID
         * THEN `rejectedCall` StateFlow SHOULD emit the call
         * 
         * **EXPECTED OUTCOME**: This test FAILS on unfixed code because the
         * `handleCallEndedMessageReceived` method does not exist.
         * 
         * **Validates: Requirements 1.2, 1.3, 1.4, 2.2, 2.3, 2.4**
         */
        test("rejectedCall should emit when onCallEndedMessageReceived is triggered with matching session ID") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                // Set up the current outgoing call
                val sessionId = "test_session_123"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Verify the call is set
                viewModel.getCallValue() shouldNotBe null
                viewModel.getCallValue()?.sessionId shouldBe sessionId
                
                // Initially, rejectedCall should be null
                viewModel.getRejectedCallValue() shouldBe null
                
                // Simulate onCallEndedMessageReceived with matching session ID
                val endedCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_ENDED
                )
                viewModel.simulateOnCallEndedMessageReceived(endedCall)
                
                advanceUntilIdle()
                
                // BUG: On unfixed code, rejectedCall will still be null
                // because handleCallEndedMessageReceived doesn't exist
                // 
                // EXPECTED (after fix): rejectedCall should emit the ended call
                viewModel.getRejectedCallValue() shouldNotBe null
                viewModel.getRejectedCallValue()?.sessionId shouldBe sessionId
            }
        }

        /**
         * **Property-Based Test: Fault Condition**
         * 
         * For ANY session ID, when `onCallEndedMessageReceived` is triggered with
         * a call that has a matching session ID, `rejectedCall` SHOULD emit.
         * 
         * **EXPECTED OUTCOME**: This test FAILS on unfixed code.
         * 
         * **Validates: Requirements 2.2, 2.3, 2.4**
         */
        test("property: rejectedCall should emit for any matching session ID") {
            runTest {
                checkAll(Arb.string(minSize = 1, maxSize = 50)) { sessionId ->
                    val viewModel = TestableOutgoingCallViewModel()
                    
                    // Set up the current outgoing call with the generated session ID
                    val currentCall = createTestCall(sessionId = sessionId)
                    viewModel.setCall(currentCall)
                    
                    advanceUntilIdle()
                    
                    // Simulate onCallEndedMessageReceived with matching session ID
                    val endedCall = createTestCall(
                        sessionId = sessionId,
                        callStatus = CometChatConstants.CALL_STATUS_ENDED
                    )
                    viewModel.simulateOnCallEndedMessageReceived(endedCall)
                    
                    advanceUntilIdle()
                    
                    // BUG: On unfixed code, this assertion will FAIL
                    viewModel.getRejectedCallValue() shouldNotBe null
                    viewModel.getRejectedCallValue()?.sessionId shouldBe sessionId
                }
            }
        }
    }

    context("Bug Condition: Call timeout scenario") {

        /**
         * **Timeout Scenario Test**
         * 
         * Simulates the real-world scenario where a call times out because
         * the recipient doesn't answer within the timeout period.
         * 
         * **EXPECTED OUTCOME**: This test FAILS on unfixed code.
         * 
         * **Validates: Requirements 1.2, 2.2**
         */
        test("rejectedCall should emit when call times out (CALL_STATUS_UNANSWERED)") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                val sessionId = "timeout_session_456"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Simulate timeout - SDK sends onCallEndedMessageReceived with unanswered status
                val timedOutCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_UNANSWERED
                )
                viewModel.simulateOnCallEndedMessageReceived(timedOutCall)
                
                advanceUntilIdle()
                
                // BUG: On unfixed code, rejectedCall will be null
                viewModel.getRejectedCallValue() shouldNotBe null
            }
        }
    }

    context("Bug Condition: Busy scenario") {

        /**
         * **Busy Scenario Test**
         * 
         * Simulates the real-world scenario where the recipient is busy
         * (already on another call).
         * 
         * **EXPECTED OUTCOME**: This test FAILS on unfixed code.
         * 
         * **Validates: Requirements 1.3, 2.3**
         */
        test("rejectedCall should emit when recipient is busy (CALL_STATUS_BUSY)") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                val sessionId = "busy_session_789"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Simulate busy - SDK sends onCallEndedMessageReceived with busy status
                val busyCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_BUSY
                )
                viewModel.simulateOnCallEndedMessageReceived(busyCall)
                
                advanceUntilIdle()
                
                // BUG: On unfixed code, rejectedCall will be null
                viewModel.getRejectedCallValue() shouldNotBe null
            }
        }
    }

    context("Bug Condition: System termination scenario") {

        /**
         * **System Termination Scenario Test**
         * 
         * Simulates the real-world scenario where the call is terminated
         * by the system or due to network issues.
         * 
         * **EXPECTED OUTCOME**: This test FAILS on unfixed code.
         * 
         * **Validates: Requirements 1.3, 1.4, 2.3, 2.4**
         */
        test("rejectedCall should emit when call is terminated by system (CALL_STATUS_ENDED)") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                val sessionId = "terminated_session_101"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Simulate system termination - SDK sends onCallEndedMessageReceived
                val terminatedCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_ENDED
                )
                viewModel.simulateOnCallEndedMessageReceived(terminatedCall)
                
                advanceUntilIdle()
                
                // BUG: On unfixed code, rejectedCall will be null
                viewModel.getRejectedCallValue() shouldNotBe null
            }
        }
    }
})
