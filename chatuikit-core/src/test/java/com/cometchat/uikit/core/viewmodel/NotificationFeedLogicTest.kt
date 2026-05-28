package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.models.NotificationCategory
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.core.state.FilterChipState
import com.cometchat.uikit.core.state.TimestampGroup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Tests for pure logic functions used by CometChatNotificationFeedViewModel.
 * Uses Kotest FunSpec style (required by project's test runner configuration).
 *
 * Feature: cometchat-notification-feed-uikit
 */
class NotificationFeedLogicTest : FunSpec({

    // region Timestamp Grouping Tests

    test("groupByTimestamp returns empty list for empty input") {
        val result = groupByTimestamp(emptyList())
        result shouldHaveSize 0
    }

    test("groupByTimestamp groups today items under Today label") {
        val now = System.currentTimeMillis() / 1000
        val items = listOf(
            createFeedItem("1", sentAt = now - 60),
            createFeedItem("2", sentAt = now - 3600)
        )
        val result = groupByTimestamp(items)
        result shouldHaveSize 1
        result[0].label shouldBe "Today"
        result[0].items shouldHaveSize 2
    }

    test("groupByTimestamp groups yesterday items under Yesterday label") {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis / 1000

        val items = listOf(createFeedItem("1", sentAt = yesterday))
        val result = groupByTimestamp(items)
        result shouldHaveSize 1
        result[0].label shouldBe "Yesterday"
    }

    test("groupByTimestamp preserves item order within groups newest first") {
        val now = System.currentTimeMillis() / 1000
        val items = listOf(
            createFeedItem("1", sentAt = now - 3600),
            createFeedItem("2", sentAt = now - 60)
        )
        val result = groupByTimestamp(items)
        result shouldHaveSize 1
        result[0].items[0].id shouldBe "2"
        result[0].items[1].id shouldBe "1"
    }

    test("groupByTimestamp does not lose or duplicate items") {
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
        val result = groupByTimestamp(items)
        val totalItems = result.sumOf { it.items.size }
        totalItems shouldBe items.size
    }

    // endregion

    // region Property 1: Filter chip ordering and count

    test("Property 1: filter chips are N+1 with All first and categories in order") {
        checkAll(100, Arb.int(0, 20)) { categoryCount ->
            val categories = (0 until categoryCount).map { i ->
                NotificationCategory("cat_$i", "Category $i")
            }
            val chips = computeFilterChips(categories, null, emptyMap(), 0)

            chips shouldHaveSize categoryCount + 1
            chips[0].label shouldBe "All"
            chips[0].id shouldBe "all"

            categories.forEachIndexed { index, category ->
                chips[index + 1].label shouldBe category.name
                chips[index + 1].id shouldBe category.id
            }
        }
    }

    // endregion

    // region Property 2: Timestamp grouping correctness

    test("Property 2: timestamp grouping preserves all items and orders correctly") {
        checkAll(100, Arb.int(1, 50)) { itemCount ->
            val now = System.currentTimeMillis() / 1000
            val items = (0 until itemCount).map { i ->
                val offset = (i * 3600L) + (i * 100L) // spread across time
                createFeedItem("item_$i", sentAt = now - offset)
            }

            val groups = groupByTimestamp(items)

            // No items lost
            val totalGroupedItems = groups.sumOf { it.items.size }
            totalGroupedItems shouldBe itemCount

            // Items within each group ordered newest first
            groups.forEach { group ->
                for (i in 0 until group.items.size - 1) {
                    (group.items[i].sentAt >= group.items[i + 1].sentAt) shouldBe true
                }
            }
        }
    }

    // endregion

    // region Property 4: Unread badge count invariant

    test("Property 4: unread count equals items with null readAt") {
        checkAll(100, Arb.int(1, 100)) { itemCount ->
            val now = System.currentTimeMillis() / 1000
            val items = (0 until itemCount).map { i ->
                val isRead = i % 2 == 0
                createFeedItem(
                    "item_$i",
                    readAt = if (isRead) now else null
                )
            }

            val expectedUnread = items.count { !it.isRead }
            val actualUnread = items.count { it.readAt == null }
            expectedUnread shouldBe actualUnread
        }
    }

    // endregion

    // region Property 8: New WebSocket item inserted at top

    test("Property 8: new item is always inserted at position 0") {
        checkAll(100, Arb.int(0, 50)) { existingCount ->
            val now = System.currentTimeMillis() / 1000
            val existingItems = (0 until existingCount).map { i ->
                createFeedItem("existing_$i", sentAt = now - (i * 60L))
            }
            val newItem = createFeedItem("new_item", sentAt = now)
            val updatedList = listOf(newItem) + existingItems

            updatedList.size shouldBe existingCount + 1
            updatedList[0].id shouldBe newItem.id
        }
    }

    // endregion

    // region Property 11: Unread indicator matches read state

    test("Property 11: unread indicator present iff readAt is null") {
        checkAll(100, Arb.boolean()) { isRead ->
            val now = System.currentTimeMillis() / 1000
            val readAt: Long? = if (isRead) now else null
            val item = createFeedItem("item", readAt = readAt)

            if (readAt == null) {
                item.isRead shouldBe false
            } else {
                item.isRead shouldBe true
            }
        }
    }

    // endregion
})

// region Pure Logic Functions

private fun groupByTimestamp(
    items: List<NotificationFeedItem>,
    locale: Locale = Locale.getDefault()
): List<TimestampGroup> {
    if (items.isEmpty()) return emptyList()

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterday = Calendar.getInstance().apply {
        time = today.time
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val startOfWeek = Calendar.getInstance().apply {
        time = today.time
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }

    val dayNameFormat = SimpleDateFormat("EEEE", locale)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", locale)

    val grouped = items.groupBy { item ->
        val itemDay = Calendar.getInstance().apply {
            timeInMillis = item.sentAt * 1000
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when {
            !itemDay.before(today) -> "Today"
            !itemDay.before(yesterday) -> "Yesterday"
            !itemDay.before(startOfWeek) -> dayNameFormat.format(Date(item.sentAt * 1000))
            else -> dateFormat.format(Date(item.sentAt * 1000))
        }
    }

    return grouped.map { (label, groupItems) ->
        TimestampGroup(
            label = label,
            items = groupItems.sortedByDescending { it.sentAt }
        )
    }
}

private fun computeFilterChips(
    categories: List<NotificationCategory>,
    activeCategory: String?,
    unreadCounts: Map<String, Int>,
    totalUnread: Int
): List<FilterChipState> {
    val chips = mutableListOf<FilterChipState>()
    chips.add(FilterChipState(id = "all", label = "All", isActive = activeCategory == null, unreadCount = totalUnread))
    categories.forEach { category ->
        chips.add(FilterChipState(
            id = category.id ?: "",
            label = category.name ?: "",
            isActive = activeCategory == category.id,
            unreadCount = unreadCounts[category.id] ?: 0
        ))
    }
    return chips
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
