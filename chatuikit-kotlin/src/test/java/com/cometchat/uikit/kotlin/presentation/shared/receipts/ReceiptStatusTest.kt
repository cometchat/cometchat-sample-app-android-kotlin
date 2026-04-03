package com.cometchat.uikit.kotlin.presentation.shared.receipts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ReceiptStatus enum.
 */
class ReceiptStatusTest {

    @Test
    fun `ReceiptStatus should have IN_PROGRESS value`() {
        val status = ReceiptStatus.IN_PROGRESS
        assertNotNull(status)
        assertEquals("IN_PROGRESS", status.name)
    }

    @Test
    fun `ReceiptStatus should have SENT value`() {
        val status = ReceiptStatus.SENT
        assertNotNull(status)
        assertEquals("SENT", status.name)
    }

    @Test
    fun `ReceiptStatus should have DELIVERED value`() {
        val status = ReceiptStatus.DELIVERED
        assertNotNull(status)
        assertEquals("DELIVERED", status.name)
    }

    @Test
    fun `ReceiptStatus should have READ value`() {
        val status = ReceiptStatus.READ
        assertNotNull(status)
        assertEquals("READ", status.name)
    }

    @Test
    fun `ReceiptStatus should have ERROR value`() {
        val status = ReceiptStatus.ERROR
        assertNotNull(status)
        assertEquals("ERROR", status.name)
    }

    @Test
    fun `ReceiptStatus should have exactly 5 values`() {
        val values = ReceiptStatus.entries
        assertEquals(5, values.size)
    }

    @Test
    fun `ReceiptStatus values should be in correct order`() {
        val values = ReceiptStatus.entries
        assertEquals(ReceiptStatus.IN_PROGRESS, values[0])
        assertEquals(ReceiptStatus.SENT, values[1])
        assertEquals(ReceiptStatus.DELIVERED, values[2])
        assertEquals(ReceiptStatus.READ, values[3])
        assertEquals(ReceiptStatus.ERROR, values[4])
    }

    @Test
    fun `ReceiptStatus valueOf should return correct enum`() {
        assertEquals(ReceiptStatus.IN_PROGRESS, ReceiptStatus.valueOf("IN_PROGRESS"))
        assertEquals(ReceiptStatus.SENT, ReceiptStatus.valueOf("SENT"))
        assertEquals(ReceiptStatus.DELIVERED, ReceiptStatus.valueOf("DELIVERED"))
        assertEquals(ReceiptStatus.READ, ReceiptStatus.valueOf("READ"))
        assertEquals(ReceiptStatus.ERROR, ReceiptStatus.valueOf("ERROR"))
    }

    @Test
    fun `ReceiptStatus ordinal values should be sequential`() {
        assertEquals(0, ReceiptStatus.IN_PROGRESS.ordinal)
        assertEquals(1, ReceiptStatus.SENT.ordinal)
        assertEquals(2, ReceiptStatus.DELIVERED.ordinal)
        assertEquals(3, ReceiptStatus.READ.ordinal)
        assertEquals(4, ReceiptStatus.ERROR.ordinal)
    }

    @Test
    fun `ReceiptStatus entries should contain all statuses`() {
        val entries = ReceiptStatus.entries
        assertTrue(entries.contains(ReceiptStatus.IN_PROGRESS))
        assertTrue(entries.contains(ReceiptStatus.SENT))
        assertTrue(entries.contains(ReceiptStatus.DELIVERED))
        assertTrue(entries.contains(ReceiptStatus.READ))
        assertTrue(entries.contains(ReceiptStatus.ERROR))
    }

    @Test
    fun `ReceiptStatus progression should follow message lifecycle`() {
        // Verify the logical progression of message status
        assertTrue(ReceiptStatus.IN_PROGRESS.ordinal < ReceiptStatus.SENT.ordinal)
        assertTrue(ReceiptStatus.SENT.ordinal < ReceiptStatus.DELIVERED.ordinal)
        assertTrue(ReceiptStatus.DELIVERED.ordinal < ReceiptStatus.READ.ordinal)
    }
}
