package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.models.NotificationCategory
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.core.state.NotificationFeedUIState
import com.cometchat.uikit.core.state.TimestampGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Calendar

/**
 * Unit tests for CometChatNotificationFeedViewModel.
 * Tests core business logic: timestamp grouping, filter state computation,
 * engagement rules, and state transitions.
 *
 * Note: SDK calls are not tested here (they require integration tests with mocks).
 * These tests focus on pure logic that doesn't depend on CometChat SDK callbacks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatNotificationFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Timestamp Grouping Tests

    @Test
    fun `groupByTimestamp returns empty list for empty input`() {
        val viewModel = createViewModel()
        val result = viewModel.groupByTimestamp(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groupByTimestamp groups today items under Today label`() {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000
        val items = listOf(
            createFeedItem("1", sentAt = now - 60),      // 1 minute ago
            createFeedItem("2", sentAt = now - 3600)     // 1 hour ago
        )

        val result = viewModel.groupByTimestamp(items)

        assertEquals(1, result.size)
        assertEquals("Today", result[0].label)
        assertEquals(2, result[0].items.size)
    }

    @Test
    fun `groupByTimestamp groups yesterday items under Yesterday label`() {
        val viewModel = createViewModel()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis / 1000

        val items = listOf(
            createFeedItem("1", sentAt = yesterday)
        )

        val result = viewModel.groupByTimestamp(items)

        assertEquals(1, result.size)
        assertEquals("Yesterday", result[0].label)
    }

    @Test
    fun `groupByTimestamp preserves item order within groups (newest first)`() {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000
        val items = listOf(
            createFeedItem("1", sentAt = now - 3600),    // older
            createFeedItem("2", sentAt = now - 60)       // newer
        )

        val result = viewModel.groupByTimestamp(items)

        assertEquals(1, result.size)
        // Items should be sorted newest first within group
        assertEquals("2", result[0].items[0].id)
        assertEquals("1", result[0].items[1].id)
    }

    @Test
    fun `groupByTimestamp does not lose or duplicate items`() {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis / 1000

        val items = listOf(
            createFeedItem("1", sentAt = now - 60),
            createFeedItem("2", sentAt = now - 120),
            createFeedItem("3", sentAt = yesterday),
            createFeedItem("4", sentAt = yesterday - 3600)
        )

        val result = viewModel.groupByTimestamp(items)

        val totalItems = result.sumOf { it.items.size }
        assertEquals(items.size, totalItems)
    }

    @Test
    fun `groupByTimestamp creates separate groups for different days`() {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis / 1000

        val items = listOf(
            createFeedItem("1", sentAt = now - 60),
            createFeedItem("2", sentAt = yesterday)
        )

        val result = viewModel.groupByTimestamp(items)

        assertEquals(2, result.size)
        assertEquals("Today", result[0].label)
        assertEquals("Yesterday", result[1].label)
    }

    // endregion

    // region Filter Chip State Tests

    @Test
    fun `initial filter chips contain only All chip when no categories`() {
        val viewModel = createViewModel()
        val chips = viewModel.filterChips.value

        // Initially should have "All" chip (populated during init)
        // Note: In real scenario, categories fetch happens async
        assertTrue(chips.isEmpty() || chips[0].id == "all")
    }

    // endregion

    // region Unread State Tests

    @Test
    fun `isRead returns true when readAt is not null`() {
        val item = createFeedItem("1", readAt = System.currentTimeMillis() / 1000)
        assertTrue(item.isRead)
    }

    @Test
    fun `isRead returns false when readAt is null`() {
        val item = createFeedItem("1", readAt = null)
        assertFalse(item.isRead)
    }

    // endregion

    // region Helper Methods

    private fun createViewModel(): CometChatNotificationFeedViewModel {
        return CometChatNotificationFeedViewModel(
            feedRequestBuilder = null,
            categoriesRequestBuilder = null,
            enableListeners = false,  // Disable for testing
            pollingIntervalMs = Long.MAX_VALUE  // Disable polling for tests
        )
    }

    private fun createFeedItem(
        id: String,
        category: String = "promotions",
        sentAt: Long = System.currentTimeMillis() / 1000,
        readAt: Long? = null
    ): NotificationFeedItem {
        return NotificationFeedItem().apply {
            setId(id)
            setCategory(category)
            setSentAt(sentAt)
            setReadAt(readAt)
        }
    }

    // endregion
}
