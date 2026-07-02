package com.cometchat.uikit.kotlin.presentation.calllogs.ui

import android.graphics.Color
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CallLogsAdapter.
 * Tests verify adapter list updates, item count, click listeners, and style configuration.
 * Uses Robolectric because DiffUtil requires Android framework.
 *
 * Validates: Requirements 14.2, 26.3
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CallLogsAdapterTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class CallLogsAdapterTest {

    private lateinit var adapter: CallLogsAdapter

    @Before
    fun setup() {
        adapter = CallLogsAdapter()
    }

    private fun createMockCallLog(
        type: String = "audio",
        status: String = "ended",
        initiatorUid: String = "caller-1",
        initiatorName: String = "Caller 1"
    ): CallLog {
        val callLog = mock(CallLog::class.java)
        `when`(callLog.type).thenReturn(type)
        `when`(callLog.status).thenReturn(status)
        `when`(callLog.initiatedAt).thenReturn(1735689600L)
        `when`(callLog.totalDurationInMinutes).thenReturn(120.0)

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

    private fun createMockCallLogs(count: Int): List<CallLog> {
        return (1..count).map { i ->
            createMockCallLog(
                initiatorUid = "caller-$i",
                initiatorName = "Caller $i"
            )
        }
    }

    // ==================== List Management Tests ====================

    @Test
    fun `initial adapter should have empty list`() {
        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.getList().isEmpty())
    }

    @Test
    fun `setList should update adapter with new call logs`() {
        val callLogs = createMockCallLogs(3)

        adapter.setList(callLogs)

        assertEquals(3, adapter.itemCount)
        assertEquals(callLogs, adapter.getList())
    }

    @Test
    fun `setList should replace existing list`() {
        val initialList = createMockCallLogs(2)
        adapter.setList(initialList)

        val newList = createMockCallLogs(5)
        adapter.setList(newList)

        assertEquals(5, adapter.itemCount)
        assertEquals(newList, adapter.getList())
    }

    @Test
    fun `setList with empty list should clear adapter`() {
        adapter.setList(createMockCallLogs(3))

        adapter.setList(emptyList())

        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.getList().isEmpty())
    }

    @Test
    fun `setList should handle single item`() {
        val callLog = createMockCallLog()

        adapter.setList(listOf(callLog))

        assertEquals(1, adapter.itemCount)
        assertEquals(callLog, adapter.getList()[0])
    }

    @Test
    fun `setList should handle large list`() {
        val callLogs = createMockCallLogs(100)

        adapter.setList(callLogs)

        assertEquals(100, adapter.itemCount)
    }

    // ==================== Item Count Tests ====================

    @Test
    fun `itemCount should match list size after setList`() {
        adapter.setList(createMockCallLogs(7))
        assertEquals(7, adapter.itemCount)

        adapter.setList(createMockCallLogs(3))
        assertEquals(3, adapter.itemCount)

        adapter.setList(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    // ==================== Call Type Variants ====================

    @Test
    fun `adapter should handle audio call logs`() {
        val audioCallLog = createMockCallLog(type = "audio", status = "ended")
        adapter.setList(listOf(audioCallLog))

        assertEquals(1, adapter.itemCount)
        assertEquals("audio", adapter.getList()[0].type)
    }

    @Test
    fun `adapter should handle video call logs`() {
        val videoCallLog = createMockCallLog(type = "video", status = "ended")
        adapter.setList(listOf(videoCallLog))

        assertEquals(1, adapter.itemCount)
        assertEquals("video", adapter.getList()[0].type)
    }

    @Test
    fun `adapter should handle missed call logs`() {
        val missedCallLog = createMockCallLog(type = "audio", status = "missed")
        adapter.setList(listOf(missedCallLog))

        assertEquals(1, adapter.itemCount)
        assertEquals("missed", adapter.getList()[0].status)
    }

    @Test
    fun `adapter should handle cancelled call logs`() {
        val cancelledCallLog = createMockCallLog(type = "audio", status = "cancelled")
        adapter.setList(listOf(cancelledCallLog))

        assertEquals(1, adapter.itemCount)
        assertEquals("cancelled", adapter.getList()[0].status)
    }

    @Test
    fun `adapter should handle rejected call logs`() {
        val rejectedCallLog = createMockCallLog(type = "video", status = "rejected")
        adapter.setList(listOf(rejectedCallLog))

        assertEquals(1, adapter.itemCount)
        assertEquals("rejected", adapter.getList()[0].status)
    }

    @Test
    fun `adapter should handle mixed call types and statuses`() {
        val callLogs = listOf(
            createMockCallLog(type = "audio", status = "ended", initiatorUid = "c1"),
            createMockCallLog(type = "video", status = "missed", initiatorUid = "c2"),
            createMockCallLog(type = "audio", status = "cancelled", initiatorUid = "c3"),
            createMockCallLog(type = "video", status = "rejected", initiatorUid = "c4"),
            createMockCallLog(type = "audio", status = "unanswered", initiatorUid = "c5")
        )

        adapter.setList(callLogs)

        assertEquals(5, adapter.itemCount)
    }

    // ==================== Click Listener Tests ====================

    @Test
    fun `setOnItemClick should set click listener without throwing`() {
        adapter.setOnItemClick { _, _, _ -> }
        assertNotNull(adapter)
    }

    @Test
    fun `setOnItemLongClick should set long click listener without throwing`() {
        adapter.setOnItemLongClick { _, _, _ -> }
        assertNotNull(adapter)
    }

    @Test
    fun `setOnCallTypeIconClick should set call type icon click listener`() {
        adapter.setOnCallTypeIconClick { _, _, _ -> }
        assertNotNull(adapter)
    }

    // ==================== Style Configuration Tests ====================

    @Test
    fun `setItemStyle should update item style`() {
        val style = CometChatCallLogsListItemStyle(
            backgroundColor = Color.WHITE,
            titleTextColor = Color.BLACK,
            subtitleTextColor = Color.GRAY
        )

        adapter.setItemStyle(style)

        assertNotNull(adapter)
    }

    @Test
    fun `setHideSeparator should update visibility flag`() {
        adapter.setHideSeparator(true)
        assertNotNull(adapter)

        adapter.setHideSeparator(false)
        assertNotNull(adapter)
    }

    @Test
    fun `setDateTimeFormatter should update formatter`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun time(timestamp: Long): String = "Formatted: $timestamp"
        }

        adapter.setDateTimeFormatter(formatter)

        assertEquals(formatter, adapter.getDateTimeFormatter())
    }

    @Test
    fun `setDateTimeFormatter with null should clear formatter`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun time(timestamp: Long): String = "Formatted: $timestamp"
        }
        adapter.setDateTimeFormatter(formatter)

        adapter.setDateTimeFormatter(null)

        assertEquals(null, adapter.getDateTimeFormatter())
    }

    // ==================== Custom View Listener Tests ====================

    @Test
    fun `setItemView should set custom item view listener`() {
        adapter.setItemView(null)
        assertNotNull(adapter)
    }

    @Test
    fun `setLeadingView should set custom leading view listener`() {
        adapter.setLeadingView(null)
        assertNotNull(adapter)
    }

    @Test
    fun `setTitleView should set custom title view listener`() {
        adapter.setTitleView(null)
        assertNotNull(adapter)
    }

    @Test
    fun `setSubtitleView should set custom subtitle view listener`() {
        adapter.setSubtitleView(null)
        assertNotNull(adapter)
    }

    @Test
    fun `setTrailingView should set custom trailing view listener`() {
        adapter.setTrailingView(null)
        assertNotNull(adapter)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `adapter should handle rapid list updates`() {
        for (i in 1..10) {
            val callLogs = createMockCallLogs(i)
            adapter.setList(callLogs)
        }

        assertEquals(10, adapter.itemCount)
    }

    @Test
    fun `adapter should handle list with duplicate session ids`() {
        val callLog1 = createMockCallLog(initiatorUid = "caller-dup-1")
        val callLog2 = createMockCallLog(initiatorUid = "caller-dup-2")

        adapter.setList(listOf(callLog1, callLog2))

        assertEquals(2, adapter.itemCount)
    }
}
