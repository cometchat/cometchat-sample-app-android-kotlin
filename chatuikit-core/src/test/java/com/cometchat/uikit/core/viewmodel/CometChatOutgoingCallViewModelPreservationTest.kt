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
 * Preservation Property Test for CometChatOutgoingCallViewModel.
 * 
 * **Property 2: Preservation** - Existing Call Event Handling
 * 
 * This test validates that existing call event handling behavior is preserved:
 * - `onOutgoingCallRejected` continues to emit `rejectedCall`
 * - `onOutgoingCallAccepted` continues to emit `acceptedCall` and disable end call button
 * 
 * **CRITICAL**: These tests MUST PASS on unfixed code to establish baseline behavior.
 * After the fix is implemented, these tests MUST STILL PASS to confirm no regression.
 * 
 * **Validates: Requirements 3.1, 3.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatOutgoingCallViewModelPreservationTest : FunSpec({

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
     * call events that would normally come from the SDK.
     */
    class TestableOutgoingCallViewModel : CometChatOutgoingCallViewModel(enableListeners = false) {
        
        /**
         * Simulates the `onOutgoingCallRejected` SDK callback.
         * 
         * In the real implementation, this is called by the CometChat SDK
         * when the recipient explicitly rejects the outgoing call.
         * 
         * This behavior MUST be preserved after the fix.
         */
        fun simulateOnOutgoingCallRejected(call: Call) {
            // Use reflection to call the private handleOutgoingCallRejected method
            val method = this::class.java.superclass.getDeclaredMethod(
                "handleOutgoingCallRejected",
                Call::class.java
            )
            method.isAccessible = true
            method.invoke(this, call)
        }
        
        /**
         * Simulates the `onOutgoingCallAccepted` SDK callback.
         * 
         * In the real implementation, this is called by the CometChat SDK
         * when the recipient accepts the outgoing call.
         * 
         * This behavior MUST be preserved after the fix.
         */
        fun simulateOnOutgoingCallAccepted(call: Call) {
            // Use reflection to call the private handleOutgoingCallAccepted method
            val method = this::class.java.superclass.getDeclaredMethod(
                "handleOutgoingCallAccepted",
                Call::class.java
            )
            method.isAccessible = true
            method.invoke(this, call)
        }
        
        /**
         * Exposes the current rejectedCall value for testing.
         */
        fun getRejectedCallValue(): Call? = rejectedCall.value
        
        /**
         * Exposes the current acceptedCall value for testing.
         */
        fun getAcceptedCallValue(): Call? = acceptedCall.value
        
        /**
         * Exposes the current call value for testing.
         */
        fun getCallValue(): Call? = call.value
        
        /**
         * Exposes the current endCallButtonEnabled value for testing.
         */
        fun getEndCallButtonEnabledValue(): Boolean = endCallButtonEnabled.value
    }

    /**
     * Helper function to create a test call with a specific session ID.
     */
    fun createTestCall(
        sessionId: String,
        receiverUid: String = "test_receiver",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        callStatus: String = CometChatConstants.CALL_STATUS_INITIATED
    ): Call {
        val call = Call(receiverUid, receiverType, CometChatConstants.CALL_TYPE_AUDIO)
        call.sessionId = sessionId
        call.callStatus = callStatus
        
        val initiator = User().apply { uid = "test_initiator" }
        call.callInitiator = initiator
        
        return call
    }

    // ========================================
    // Preservation Tests - onOutgoingCallRejected
    // ========================================

    context("Preservation: onOutgoingCallRejected emits rejectedCall") {

        /**
         * **Property 2: Preservation Test - Explicit Rejection**
         * 
         * WHEN `onOutgoingCallRejected` is triggered with a call
         * THEN `rejectedCall` StateFlow SHOULD emit the call
         * 
         * **EXPECTED OUTCOME**: This test PASSES on unfixed code (existing behavior).
         * This test MUST STILL PASS after the fix (preservation).
         * 
         * **Validates: Requirement 3.2**
         */
        test("rejectedCall should emit when onOutgoingCallRejected is triggered") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                // Set up the current outgoing call
                val sessionId = "rejection_session_123"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Verify the call is set
                viewModel.getCallValue() shouldNotBe null
                viewModel.getCallValue()?.sessionId shouldBe sessionId
                
                // Initially, rejectedCall should be null
                viewModel.getRejectedCallValue() shouldBe null
                
                // Simulate onOutgoingCallRejected
                val rejectedCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_REJECTED
                )
                viewModel.simulateOnOutgoingCallRejected(rejectedCall)
                
                advanceUntilIdle()
                
                // PRESERVATION: rejectedCall should emit the rejected call
                viewModel.getRejectedCallValue() shouldNotBe null
                viewModel.getRejectedCallValue()?.sessionId shouldBe sessionId
            }
        }

        /**
         * **Property-Based Test: Preservation - Explicit Rejection**
         * 
         * For ANY session ID, when `onOutgoingCallRejected` is triggered,
         * `rejectedCall` SHOULD emit the call.
         * 
         * **EXPECTED OUTCOME**: This test PASSES on unfixed code.
         * 
         * **Validates: Requirement 3.2**
         */
        test("property: rejectedCall should emit for any onOutgoingCallRejected event") {
            runTest {
                checkAll(Arb.string(minSize = 1, maxSize = 50)) { sessionId ->
                    val viewModel = TestableOutgoingCallViewModel()
                    
                    // Set up the current outgoing call with the generated session ID
                    val currentCall = createTestCall(sessionId = sessionId)
                    viewModel.setCall(currentCall)
                    
                    advanceUntilIdle()
                    
                    // Simulate onOutgoingCallRejected
                    val rejectedCall = createTestCall(
                        sessionId = sessionId,
                        callStatus = CometChatConstants.CALL_STATUS_REJECTED
                    )
                    viewModel.simulateOnOutgoingCallRejected(rejectedCall)
                    
                    advanceUntilIdle()
                    
                    // PRESERVATION: rejectedCall should emit
                    viewModel.getRejectedCallValue() shouldNotBe null
                    viewModel.getRejectedCallValue()?.sessionId shouldBe sessionId
                }
            }
        }
    }

    // ========================================
    // Preservation Tests - onOutgoingCallAccepted
    // ========================================

    context("Preservation: onOutgoingCallAccepted emits acceptedCall and disables end button") {

        /**
         * **Property 2: Preservation Test - Call Acceptance**
         * 
         * WHEN `onOutgoingCallAccepted` is triggered with a call
         * THEN `acceptedCall` StateFlow SHOULD emit the call
         * AND `endCallButtonEnabled` SHOULD be set to false
         * 
         * **EXPECTED OUTCOME**: This test PASSES on unfixed code (existing behavior).
         * This test MUST STILL PASS after the fix (preservation).
         * 
         * **Validates: Requirement 3.1**
         */
        test("acceptedCall should emit and endCallButtonEnabled should be false when onOutgoingCallAccepted is triggered") {
            runTest {
                val viewModel = TestableOutgoingCallViewModel()
                
                // Set up the current outgoing call
                val sessionId = "acceptance_session_456"
                val currentCall = createTestCall(sessionId = sessionId)
                viewModel.setCall(currentCall)
                
                advanceUntilIdle()
                
                // Verify the call is set
                viewModel.getCallValue() shouldNotBe null
                viewModel.getCallValue()?.sessionId shouldBe sessionId
                
                // Initially, acceptedCall should be null and endCallButtonEnabled should be true
                viewModel.getAcceptedCallValue() shouldBe null
                viewModel.getEndCallButtonEnabledValue() shouldBe true
                
                // Simulate onOutgoingCallAccepted
                val acceptedCall = createTestCall(
                    sessionId = sessionId,
                    callStatus = CometChatConstants.CALL_STATUS_ONGOING
                )
                viewModel.simulateOnOutgoingCallAccepted(acceptedCall)
                
                advanceUntilIdle()
                
                // PRESERVATION: acceptedCall should emit the accepted call
                viewModel.getAcceptedCallValue() shouldNotBe null
                viewModel.getAcceptedCallValue()?.sessionId shouldBe sessionId
                
                // PRESERVATION: endCallButtonEnabled should be false
                viewModel.getEndCallButtonEnabledValue() shouldBe false
            }
        }

        /**
         * **Property-Based Test: Preservation - Call Acceptance**
         * 
         * For ANY session ID, when `onOutgoingCallAccepted` is triggered,
         * `acceptedCall` SHOULD emit and `endCallButtonEnabled` SHOULD be false.
         * 
         * **EXPECTED OUTCOME**: This test PASSES on unfixed code.
         * 
         * **Validates: Requirement 3.1**
         */
        test("property: acceptedCall should emit and endCallButtonEnabled should be false for any onOutgoingCallAccepted event") {
            runTest {
                checkAll(Arb.string(minSize = 1, maxSize = 50)) { sessionId ->
                    val viewModel = TestableOutgoingCallViewModel()
                    
                    // Set up the current outgoing call with the generated session ID
                    val currentCall = createTestCall(sessionId = sessionId)
                    viewModel.setCall(currentCall)
                    
                    advanceUntilIdle()
                    
                    // Simulate onOutgoingCallAccepted
                    val acceptedCall = createTestCall(
                        sessionId = sessionId,
                        callStatus = CometChatConstants.CALL_STATUS_ONGOING
                    )
                    viewModel.simulateOnOutgoingCallAccepted(acceptedCall)
                    
                    advanceUntilIdle()
                    
                    // PRESERVATION: acceptedCall should emit
                    viewModel.getAcceptedCallValue() shouldNotBe null
                    viewModel.getAcceptedCallValue()?.sessionId shouldBe sessionId
                    
                    // PRESERVATION: endCallButtonEnabled should be false
                    viewModel.getEndCallButtonEnabledValue() shouldBe false
                }
            }
        }
    }

})
