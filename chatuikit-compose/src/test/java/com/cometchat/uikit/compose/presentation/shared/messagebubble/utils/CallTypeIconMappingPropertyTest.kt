package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import com.cometchat.chat.constants.CometChatConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for Call Type Icon Mapping.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 8: Call Type Icon Mapping
 *
 * **Validates: Requirements 5.1, 9.1-9.5**
 *
 * Tests the [getCallType] function which maps call type, status, and direction
 * to [CallType] enum values for displaying appropriate call icons.
 *
 * Missed Call Logic (per Java reference):
 * - A call is considered "missed" when:
 *   1. Call status is UNANSWERED, AND
 *   2. The logged-in user is NOT the call initiator (they received the call but didn't answer)
 * - If the logged-in user initiated the call and it was unanswered, it's an outgoing call (not missed)
 *
 * The function follows these mapping rules:
 * - VIDEO_MISSED: Video call with UNANSWERED status AND logged-in user is NOT initiator
 * - AUDIO_MISSED: Audio call with UNANSWERED status AND logged-in user is NOT initiator
 * - VIDEO_INCOMING: Video call where initiator is not the logged-in user (non-missed)
 * - AUDIO_INCOMING: Audio call where initiator is not the logged-in user (non-missed)
 * - VIDEO_OUTGOING: Video call where initiator is the logged-in user
 * - AUDIO_OUTGOING: Audio call where initiator is the logged-in user
 */
class CallTypeIconMappingPropertyTest : StringSpec({

    // Test data generators
    val audioCallType = CometChatConstants.CALL_TYPE_AUDIO
    val videoCallType = CometChatConstants.CALL_TYPE_VIDEO
    
    // Only UNANSWERED status triggers missed call styling (per Java reference)
    val missedTriggerStatus = CometChatConstants.CALL_STATUS_UNANSWERED
    
    // Other statuses that don't trigger missed call styling
    val nonMissedStatuses = listOf(
        CometChatConstants.CALL_STATUS_INITIATED,
        CometChatConstants.CALL_STATUS_ONGOING,
        CometChatConstants.CALL_STATUS_ENDED,
        CometChatConstants.CALL_STATUS_CANCELLED,
        CometChatConstants.CALL_STATUS_REJECTED,
        CometChatConstants.CALL_STATUS_BUSY
    )
    
    val callTypeArb = Arb.element(listOf(audioCallType, videoCallType))
    val nonMissedStatusArb = Arb.element(nonMissedStatuses)
    val userIdArb = Arb.string(5..20)

    /**
     * Property 8: Call Type Icon Mapping
     *
     * *For any* Call message, the `getCallType()` function SHALL return the correct
     * `CallType` enum based on call type (audio/video), status (missed/answered),
     * and direction (incoming/outgoing).
     *
     * **Validates: Requirements 5.1, 9.1-9.5**
     */
    "Property 8: getCallType should always return a valid CallType enum for any input" {
        val allStatuses = nonMissedStatuses + missedTriggerStatus
        val statusArb = Arb.element(allStatuses)

        checkAll(200, callTypeArb, statusArb, userIdArb, userIdArb) { callType, status, initiatorId, loggedInUserId ->
            val result = getCallType(callType, status, initiatorId, loggedInUserId)
            
            // Result should always be a valid CallType enum value
            result shouldNotBe null
            CallType.entries.contains(result) shouldBe true
        }
    }

    // ===== MISSED CALL TESTS (per Java reference: UNANSWERED + NOT initiator) =====

    "Property 8: Video calls with UNANSWERED status where user is NOT initiator should return VIDEO_MISSED" {
        checkAll(100, userIdArb) { loggedInUserId ->
            // Different initiator = incoming call that was missed
            val initiatorId = loggedInUserId + "_other"
            val result = getCallType(videoCallType, missedTriggerStatus, initiatorId, loggedInUserId)
            result shouldBe CallType.VIDEO_MISSED
        }
    }

    "Property 8: Audio calls with UNANSWERED status where user is NOT initiator should return AUDIO_MISSED" {
        checkAll(100, userIdArb) { loggedInUserId ->
            // Different initiator = incoming call that was missed
            val initiatorId = loggedInUserId + "_other"
            val result = getCallType(audioCallType, missedTriggerStatus, initiatorId, loggedInUserId)
            result shouldBe CallType.AUDIO_MISSED
        }
    }

    "Property 8: Video calls with UNANSWERED status where user IS initiator should return VIDEO_OUTGOING (not missed)" {
        checkAll(100, userIdArb) { userId ->
            // Same user = outgoing call that wasn't answered by recipient
            val result = getCallType(videoCallType, missedTriggerStatus, userId, userId)
            result shouldBe CallType.VIDEO_OUTGOING
        }
    }

    "Property 8: Audio calls with UNANSWERED status where user IS initiator should return AUDIO_OUTGOING (not missed)" {
        checkAll(100, userIdArb) { userId ->
            // Same user = outgoing call that wasn't answered by recipient
            val result = getCallType(audioCallType, missedTriggerStatus, userId, userId)
            result shouldBe CallType.AUDIO_OUTGOING
        }
    }

    // ===== INCOMING/OUTGOING CALL TESTS =====

    "Property 8: Video calls from other users (incoming) with non-missed status should return VIDEO_INCOMING" {
        checkAll(100, nonMissedStatusArb, userIdArb) { status, loggedInUserId ->
            // Generate a different initiator ID to simulate incoming call
            val initiatorId = loggedInUserId + "_other"
            
            val result = getCallType(videoCallType, status, initiatorId, loggedInUserId)
            result shouldBe CallType.VIDEO_INCOMING
        }
    }

    "Property 8: Audio calls from other users (incoming) with non-missed status should return AUDIO_INCOMING" {
        checkAll(100, nonMissedStatusArb, userIdArb) { status, loggedInUserId ->
            // Generate a different initiator ID to simulate incoming call
            val initiatorId = loggedInUserId + "_other"
            
            val result = getCallType(audioCallType, status, initiatorId, loggedInUserId)
            result shouldBe CallType.AUDIO_INCOMING
        }
    }

    "Property 8: Video calls initiated by logged-in user (outgoing) should return VIDEO_OUTGOING" {
        checkAll(100, nonMissedStatusArb, userIdArb) { status, userId ->
            // Same initiator and logged-in user = outgoing call
            val result = getCallType(videoCallType, status, userId, userId)
            result shouldBe CallType.VIDEO_OUTGOING
        }
    }

    "Property 8: Audio calls initiated by logged-in user (outgoing) should return AUDIO_OUTGOING" {
        checkAll(100, nonMissedStatusArb, userIdArb) { status, userId ->
            // Same initiator and logged-in user = outgoing call
            val result = getCallType(audioCallType, status, userId, userId)
            result shouldBe CallType.AUDIO_OUTGOING
        }
    }

    // ===== SPECIFIC STATUS TESTS =====

    "Property 8: UNANSWERED status with incoming call should be treated as missed" {
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "other", "me")
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "other", "me")
        
        result1 shouldBe CallType.AUDIO_MISSED
        result2 shouldBe CallType.VIDEO_MISSED
    }

    "Property 8: UNANSWERED status with outgoing call should NOT be treated as missed" {
        // When user initiated the call but recipient didn't answer, it's outgoing (not missed)
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "me", "me")
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "me", "me")
        
        result1 shouldBe CallType.AUDIO_OUTGOING
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: CANCELLED status should NOT be treated as missed (shows direction)" {
        // Cancelled calls show direction, not missed styling
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_CANCELLED, "other", "me")
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_CANCELLED, "me", "me")
        
        result1 shouldBe CallType.AUDIO_INCOMING
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: REJECTED status should NOT be treated as missed (shows direction)" {
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_REJECTED, "other", "me")
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_REJECTED, "me", "me")
        
        result1 shouldBe CallType.AUDIO_INCOMING
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: BUSY status should NOT be treated as missed (shows direction)" {
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_BUSY, "other", "me")
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_BUSY, "me", "me")
        
        result1 shouldBe CallType.AUDIO_INCOMING
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: INITIATED status should not be treated as missed" {
        // Incoming audio call
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, "other", "me")
        result1 shouldBe CallType.AUDIO_INCOMING
        
        // Outgoing video call
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, "me", "me")
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: ONGOING status should not be treated as missed" {
        // Incoming video call
        val result1 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_ONGOING, "other", "me")
        result1 shouldBe CallType.VIDEO_INCOMING
        
        // Outgoing audio call
        val result2 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_ONGOING, "me", "me")
        result2 shouldBe CallType.AUDIO_OUTGOING
    }

    "Property 8: ENDED status should not be treated as missed" {
        // Incoming audio call
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_ENDED, "other", "me")
        result1 shouldBe CallType.AUDIO_INCOMING
        
        // Outgoing video call
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_ENDED, "me", "me")
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    // ===== NULL HANDLING TESTS =====

    "Property 8: Null initiator ID should be treated as incoming call" {
        // When initiator is null, it's different from logged-in user, so incoming
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, null, "me")
        result1 shouldBe CallType.AUDIO_INCOMING
        
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, null, "me")
        result2 shouldBe CallType.VIDEO_INCOMING
    }

    "Property 8: Null logged-in user ID should be treated as incoming call" {
        // When logged-in user is null, initiator is different, so incoming
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, "other", null)
        result1 shouldBe CallType.AUDIO_INCOMING
        
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, "other", null)
        result2 shouldBe CallType.VIDEO_INCOMING
    }

    "Property 8: Both null IDs should be treated as outgoing call" {
        // When both are null, they are equal, so outgoing
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, null, null)
        result1 shouldBe CallType.AUDIO_OUTGOING
        
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, null, null)
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: Null initiator with UNANSWERED should be missed (incoming unanswered)" {
        // Null initiator != logged-in user, so it's an incoming missed call
        val result1 = getCallType(audioCallType, CometChatConstants.CALL_STATUS_UNANSWERED, null, "me")
        result1 shouldBe CallType.AUDIO_MISSED
        
        val result2 = getCallType(videoCallType, CometChatConstants.CALL_STATUS_UNANSWERED, null, "me")
        result2 shouldBe CallType.VIDEO_MISSED
    }

    // ===== EDGE CASE TESTS =====

    "Property 8: Unknown call type should default to audio" {
        // When call type is not video, it defaults to audio behavior
        val result1 = getCallType("unknown", CometChatConstants.CALL_STATUS_INITIATED, "other", "me")
        result1 shouldBe CallType.AUDIO_INCOMING
        
        val result2 = getCallType(null, CometChatConstants.CALL_STATUS_INITIATED, "me", "me")
        result2 shouldBe CallType.AUDIO_OUTGOING
    }

    "Property 8: Unknown status should not be treated as missed" {
        // Unknown status is not UNANSWERED, so it's treated as non-missed
        val result1 = getCallType(audioCallType, "unknown", "other", "me")
        result1 shouldBe CallType.AUDIO_INCOMING
        
        val result2 = getCallType(videoCallType, null, "me", "me")
        result2 shouldBe CallType.VIDEO_OUTGOING
    }

    "Property 8: getCallType should be deterministic - same inputs produce same output" {
        val allStatuses = nonMissedStatuses + missedTriggerStatus
        val statusArb = Arb.element(allStatuses)

        checkAll(100, callTypeArb, statusArb, userIdArb, userIdArb) { callType, status, initiatorId, loggedInUserId ->
            val result1 = getCallType(callType, status, initiatorId, loggedInUserId)
            val result2 = getCallType(callType, status, initiatorId, loggedInUserId)
            
            result1 shouldBe result2
        }
    }

    "Property 8: All CallType enum values should be reachable" {
        // Verify that each CallType can be produced by getCallType
        val testCases = mapOf(
            CallType.AUDIO_INCOMING to listOf(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, "other", "me"),
            CallType.AUDIO_OUTGOING to listOf(audioCallType, CometChatConstants.CALL_STATUS_INITIATED, "me", "me"),
            CallType.VIDEO_INCOMING to listOf(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, "other", "me"),
            CallType.VIDEO_OUTGOING to listOf(videoCallType, CometChatConstants.CALL_STATUS_INITIATED, "me", "me"),
            // Missed calls require UNANSWERED status AND different initiator
            CallType.AUDIO_MISSED to listOf(audioCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "other", "me"),
            CallType.VIDEO_MISSED to listOf(videoCallType, CometChatConstants.CALL_STATUS_UNANSWERED, "other", "me")
        )

        testCases.forEach { (expectedType, inputs) ->
            val result = getCallType(inputs[0], inputs[1], inputs[2], inputs[3])
            result shouldBe expectedType
        }
    }

    // ===== HELPER FUNCTION TESTS =====

    "Property 8: isMissedCall helper should correctly identify missed calls" {
        // Test the isMissedCall helper function
        isMissedCall(CallType.AUDIO_MISSED) shouldBe true
        isMissedCall(CallType.VIDEO_MISSED) shouldBe true
        isMissedCall(CallType.AUDIO_INCOMING) shouldBe false
        isMissedCall(CallType.AUDIO_OUTGOING) shouldBe false
        isMissedCall(CallType.VIDEO_INCOMING) shouldBe false
        isMissedCall(CallType.VIDEO_OUTGOING) shouldBe false
    }

    "Property 8: isVideoCall helper should correctly identify video calls" {
        // Test the isVideoCall helper function
        isVideoCall(CallType.VIDEO_INCOMING) shouldBe true
        isVideoCall(CallType.VIDEO_OUTGOING) shouldBe true
        isVideoCall(CallType.VIDEO_MISSED) shouldBe true
        isVideoCall(CallType.AUDIO_INCOMING) shouldBe false
        isVideoCall(CallType.AUDIO_OUTGOING) shouldBe false
        isVideoCall(CallType.AUDIO_MISSED) shouldBe false
    }

    "Property 8: isIncomingCall helper should correctly identify incoming calls" {
        // Test the isIncomingCall helper function
        isIncomingCall(CallType.AUDIO_INCOMING) shouldBe true
        isIncomingCall(CallType.VIDEO_INCOMING) shouldBe true
        isIncomingCall(CallType.AUDIO_OUTGOING) shouldBe false
        isIncomingCall(CallType.VIDEO_OUTGOING) shouldBe false
        isIncomingCall(CallType.AUDIO_MISSED) shouldBe false
        isIncomingCall(CallType.VIDEO_MISSED) shouldBe false
    }

    "Property 8: getCallTypeIcon should return valid resource ID for all call types" {
        // Verify that each CallType has a corresponding icon resource
        CallType.entries.forEach { callType ->
            val iconRes = getCallTypeIcon(callType)
            iconRes shouldNotBe 0
        }
    }

    "Property 8: getCallTypeText should return non-empty string for all call types" {
        // Verify that each CallType has a corresponding display text
        CallType.entries.forEach { callType ->
            val text = getCallTypeText(callType)
            text.isNotEmpty() shouldBe true
        }
    }

    "Property 8: Call type mapping should be consistent with helper functions" {
        checkAll(100, callTypeArb, nonMissedStatusArb, userIdArb) { callType, status, loggedInUserId ->
            val initiatorId = loggedInUserId + "_other" // Different user = incoming
            val result = getCallType(callType, status, initiatorId, loggedInUserId)
            
            // If video call type, result should be identified as video by helper
            val isVideo = callType == videoCallType
            isVideoCall(result) shouldBe isVideo
            
            // Since status is non-missed and initiator is different, should be incoming
            isIncomingCall(result) shouldBe true
            
            // Since status is not UNANSWERED, should not be missed
            isMissedCall(result) shouldBe false
        }
    }

    "Property 8: Only UNANSWERED incoming calls should be identified as missed" {
        // Test that only UNANSWERED status with different initiator produces missed calls
        checkAll(100, callTypeArb, userIdArb) { callType, loggedInUserId ->
            val initiatorId = loggedInUserId + "_other" // Different user = incoming
            val result = getCallType(callType, missedTriggerStatus, initiatorId, loggedInUserId)
            
            // Should be missed
            isMissedCall(result) shouldBe true
            // Missed calls are not classified as incoming (they have their own category)
            isIncomingCall(result) shouldBe false
        }
    }
})
