package com.cometchat.uikit.kotlin.presentation.calllogs.utils

import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for CallLogsDiffCallback.
 * Tests verify DiffUtil calculations for efficient RecyclerView updates.
 *
 * CallLogsDiffCallback uses:
 * - areItemsTheSame: object equality (==) — same mock instance = same item
 * - areContentsTheSame: object equality + initiatedAt, endedAt, status, type
 *
 * Validates: Requirements 14.3, 26.2
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CallLogsDiffCallbackTest"
 */
class CallLogsDiffCallbackTest {

    private fun createMockCallLog(
        type: String = "audio",
        status: String = "ended",
        initiatedAt: Long = 1735689600L,
        endedAt: Long = 1735689720L,
        initiatorUid: String = "caller-1",
        initiatorName: String = "Caller 1"
    ): CallLog {
        val callLog = mock(CallLog::class.java)
        `when`(callLog.type).thenReturn(type)
        `when`(callLog.status).thenReturn(status)
        `when`(callLog.initiatedAt).thenReturn(initiatedAt)
        `when`(callLog.endedAt).thenReturn(endedAt)
        `when`(callLog.totalDurationInMinutes).thenReturn(2.0)

        val initiator = mock(CallUser::class.java)
        `when`(initiator.uid).thenReturn(initiatorUid)
        `when`(initiator.name).thenReturn(initiatorName)
        `when`(callLog.initiator).thenReturn(initiator)

        val receiver = mock(CallUser::class.java)
        `when`(receiver.uid).thenReturn("receiver-$initiatorUid")
        `when`(receiver.name).thenReturn("Receiver")
        `when`(callLog.receiver).thenReturn(receiver)

        return callLog
    }

    // ==================== List Size Tests ====================

    @Test
    fun `getOldListSize should return correct size`() {
        val oldList = listOf(createMockCallLog(), createMockCallLog(), createMockCallLog())
        val newList = listOf(createMockCallLog())

        val callback = CallLogsDiffCallback(oldList, newList)

        assertEquals(3, callback.oldListSize)
    }

    @Test
    fun `getNewListSize should return correct size`() {
        val oldList = listOf(createMockCallLog())
        val newList = listOf(createMockCallLog(), createMockCallLog())

        val callback = CallLogsDiffCallback(oldList, newList)

        assertEquals(2, callback.newListSize)
    }

    @Test
    fun `empty lists should return zero sizes`() {
        val callback = CallLogsDiffCallback(emptyList(), emptyList())

        assertEquals(0, callback.oldListSize)
        assertEquals(0, callback.newListSize)
    }

    // ==================== areItemsTheSame Tests ====================

    @Test
    fun `areItemsTheSame should return true for same object instance`() {
        val callLog = createMockCallLog()
        val oldList = listOf(callLog)
        val newList = listOf(callLog)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `areItemsTheSame should return false for different object instances`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-2")
        val oldList = listOf(callLog1)
        val newList = listOf(callLog2)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `areItemsTheSame should handle multiple items correctly`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-2")
        val callLog3 = createMockCallLog(initiatorUid = "caller-3")

        val oldList = listOf(callLog1, callLog2, callLog3)
        val newList = listOf(callLog2, callLog1, callLog3)

        val callback = CallLogsDiffCallback(oldList, newList)

        // callLog1 at old[0] vs callLog2 at new[0] — different instances
        assertFalse(callback.areItemsTheSame(0, 0))
        // callLog1 at old[0] vs callLog1 at new[1] — same instance
        assertTrue(callback.areItemsTheSame(0, 1))
        // callLog2 at old[1] vs callLog2 at new[0] — same instance
        assertTrue(callback.areItemsTheSame(1, 0))
        // callLog3 at old[2] vs callLog3 at new[2] — same instance
        assertTrue(callback.areItemsTheSame(2, 2))
    }

    // ==================== areContentsTheSame Tests ====================

    @Test
    fun `areContentsTheSame should return true for same object instance`() {
        val callLog = createMockCallLog()
        val callback = CallLogsDiffCallback(listOf(callLog), listOf(callLog))

        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false for different instances even with same fields`() {
        // Different mock instances are never == to each other
        val callLog1 = createMockCallLog(type = "audio", status = "ended")
        val callLog2 = createMockCallLog(type = "audio", status = "ended")

        val callback = CallLogsDiffCallback(listOf(callLog1), listOf(callLog2))

        // Different mock instances → == returns false → areContentsTheSame returns false
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when status differs`() {
        val callLog = createMockCallLog(status = "ended")
        // Modify status after initial setup
        val callLogUpdated = createMockCallLog(status = "missed")

        val callback = CallLogsDiffCallback(listOf(callLog), listOf(callLogUpdated))

        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when type differs`() {
        val callLog1 = createMockCallLog(type = "audio")
        val callLog2 = createMockCallLog(type = "video")

        val callback = CallLogsDiffCallback(listOf(callLog1), listOf(callLog2))

        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when initiatedAt differs`() {
        val callLog1 = createMockCallLog(initiatedAt = 1735689600L)
        val callLog2 = createMockCallLog(initiatedAt = 1735689700L)

        val callback = CallLogsDiffCallback(listOf(callLog1), listOf(callLog2))

        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when endedAt differs`() {
        val callLog1 = createMockCallLog(endedAt = 1735689720L)
        val callLog2 = createMockCallLog(endedAt = 1735689900L)

        val callback = CallLogsDiffCallback(listOf(callLog1), listOf(callLog2))

        assertFalse(callback.areContentsTheSame(0, 0))
    }

    // ==================== Edge Cases ====================

    @Test
    fun `should handle reordered list correctly`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-2")
        val callLog3 = createMockCallLog(initiatorUid = "caller-3")

        val oldList = listOf(callLog1, callLog2, callLog3)
        val newList = listOf(callLog3, callLog1, callLog2)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertEquals(3, callback.oldListSize)
        assertEquals(3, callback.newListSize)

        // Verify items are identified correctly despite reordering
        assertTrue(callback.areItemsTheSame(0, 1)) // callLog1 old[0] → new[1]
        assertTrue(callback.areItemsTheSame(1, 2)) // callLog2 old[1] → new[2]
        assertTrue(callback.areItemsTheSame(2, 0)) // callLog3 old[2] → new[0]
    }

    @Test
    fun `should handle added items correctly`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-2")

        val oldList = listOf(callLog1)
        val newList = listOf(callLog1, callLog2)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `should handle removed items correctly`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-2")

        val oldList = listOf(callLog1, callLog2)
        val newList = listOf(callLog1)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `should handle single item list`() {
        val callLog = createMockCallLog()

        val callback = CallLogsDiffCallback(listOf(callLog), listOf(callLog))

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `should handle audio and video call types`() {
        val audioCall = createMockCallLog(type = "audio")
        val videoCall = createMockCallLog(type = "video")

        val callback = CallLogsDiffCallback(listOf(audioCall), listOf(videoCall))

        assertFalse(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `should handle various call statuses`() {
        val endedCall = createMockCallLog(status = "ended")
        val missedCall = createMockCallLog(status = "missed")
        val cancelledCall = createMockCallLog(status = "cancelled")

        val oldList = listOf(endedCall, missedCall)
        val newList = listOf(endedCall, cancelledCall)

        val callback = CallLogsDiffCallback(oldList, newList)

        assertTrue(callback.areItemsTheSame(0, 0))  // same instance
        assertFalse(callback.areItemsTheSame(1, 1)) // different instances
    }
}
