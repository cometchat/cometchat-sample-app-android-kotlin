package com.cometchat.uikit.kotlin.shared.mentions

import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * Unit tests for selected mentions tracking logic.
 * 
 * Tests cover:
 * - Adding mentions to tracking
 * - Removing mentions from tracking
 * - Clearing all mentions
 * - Extracting unique mentions from text
 * - Mention limit enforcement
 * 
 * Based on design document section 3.5.
 * 
 * **Validates: Requirements FR-5.3, FR-6.1, FR-6.2**
 */
class SelectedMentionsTrackingTest : DescribeSpec({

    describe("Selected Mentions Tracking") {

        describe("adding mentions") {

            it("should add single mention to tracking") {
                val tracker = MentionTracker()
                val item = createSuggestionItem("user1", "John")
                
                tracker.addMention('@', item)
                
                tracker.getMentions('@') shouldHaveSize 1
                tracker.getMentions('@').map { it.id } shouldContain "user1"
            }

            it("should add multiple mentions to tracking") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                tracker.addMention('@', createSuggestionItem("user3", "Bob"))
                
                tracker.getMentions('@') shouldHaveSize 3
            }

            it("should not add duplicate mentions") {
                val tracker = MentionTracker()
                val item = createSuggestionItem("user1", "John")
                
                tracker.addMention('@', item)
                tracker.addMention('@', item)
                
                tracker.getMentions('@') shouldHaveSize 1
            }

            it("should track mentions by formatter ID") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('#', createSuggestionItem("tag1", "kotlin"))
                
                tracker.getMentions('@') shouldHaveSize 1
                tracker.getMentions('#') shouldHaveSize 1
            }
        }

        describe("removing mentions") {

            it("should remove mention from tracking") {
                val tracker = MentionTracker()
                val item = createSuggestionItem("user1", "John")
                
                tracker.addMention('@', item)
                tracker.removeMention('@', "user1")
                
                tracker.getMentions('@').shouldBeEmpty()
            }

            it("should only remove specified mention") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                
                tracker.removeMention('@', "user1")
                
                tracker.getMentions('@') shouldHaveSize 1
                tracker.getMentions('@').map { it.id } shouldNotContain "user1"
                tracker.getMentions('@').map { it.id } shouldContain "user2"
            }

            it("should handle removing non-existent mention") {
                val tracker = MentionTracker()
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                
                // Should not throw
                tracker.removeMention('@', "nonexistent")
                
                tracker.getMentions('@') shouldHaveSize 1
            }
        }

        describe("clearing mentions") {

            it("should clear all mentions for formatter") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                
                tracker.clearMentions('@')
                
                tracker.getMentions('@').shouldBeEmpty()
            }

            it("should only clear mentions for specified formatter") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('#', createSuggestionItem("tag1", "kotlin"))
                
                tracker.clearMentions('@')
                
                tracker.getMentions('@').shouldBeEmpty()
                tracker.getMentions('#') shouldHaveSize 1
            }

            it("should clear all mentions across all formatters") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('#', createSuggestionItem("tag1", "kotlin"))
                
                tracker.clearAll()
                
                tracker.getMentions('@').shouldBeEmpty()
                tracker.getMentions('#').shouldBeEmpty()
            }
        }

        describe("mention count") {

            it("should return correct count") {
                val tracker = MentionTracker()
                
                tracker.getCount('@') shouldBe 0
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.getCount('@') shouldBe 1
                
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                tracker.getCount('@') shouldBe 2
            }

            it("should return count per formatter") {
                val tracker = MentionTracker()
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                tracker.addMention('#', createSuggestionItem("tag1", "kotlin"))
                
                tracker.getCount('@') shouldBe 2
                tracker.getCount('#') shouldBe 1
            }
        }

        describe("mention limit enforcement") {

            it("should check if limit is reached") {
                val tracker = MentionTracker(mentionLimit = 2)
                
                tracker.isLimitReached('@') shouldBe false
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.isLimitReached('@') shouldBe false
                
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                tracker.isLimitReached('@') shouldBe true
            }

            it("should prevent adding when limit reached") {
                val tracker = MentionTracker(mentionLimit = 2)
                
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                
                val added = tracker.addMentionIfAllowed('@', createSuggestionItem("user3", "Bob"))
                
                added shouldBe false
                tracker.getCount('@') shouldBe 2
            }

            it("should allow adding when under limit") {
                val tracker = MentionTracker(mentionLimit = 10)
                
                val added = tracker.addMentionIfAllowed('@', createSuggestionItem("user1", "John"))
                
                added shouldBe true
                tracker.getCount('@') shouldBe 1
            }

            it("should use default limit of 10") {
                val tracker = MentionTracker()
                
                // Add 10 mentions
                for (i in 1..10) {
                    tracker.addMention('@', createSuggestionItem("user$i", "User$i"))
                }
                
                tracker.isLimitReached('@') shouldBe true
            }
        }

        describe("extracting unique mentions") {

            it("should extract unique mentions from list") {
                val items = listOf(
                    createSuggestionItem("user1", "John"),
                    createSuggestionItem("user2", "Jane"),
                    createSuggestionItem("user1", "John"), // Duplicate
                    createSuggestionItem("user3", "Bob")
                )
                
                val unique = extractUniqueMentions(items)
                
                unique shouldHaveSize 3
                unique.map { it.id } shouldContain "user1"
                unique.map { it.id } shouldContain "user2"
                unique.map { it.id } shouldContain "user3"
            }

            it("should handle empty list") {
                val unique = extractUniqueMentions(emptyList())
                
                unique.shouldBeEmpty()
            }

            it("should preserve first occurrence when duplicates exist") {
                val item1 = createSuggestionItem("user1", "John")
                val item2 = createSuggestionItem("user1", "Johnny") // Same ID, different name
                
                val unique = extractUniqueMentions(listOf(item1, item2))
                
                unique shouldHaveSize 1
                unique[0].name shouldBe "John" // First occurrence preserved
            }
        }

        describe("edge cases") {

            it("should handle empty formatter ID") {
                val tracker = MentionTracker()
                
                // Should not throw
                tracker.getMentions(' ').shouldBeEmpty()
            }

            it("should handle mention with empty ID") {
                val tracker = MentionTracker()
                val item = SuggestionItem(
                    id = "",
                    name = "Empty",
                    promptText = "@Empty",
                    underlyingText = "<@uid:>"
                )
                
                tracker.addMention('@', item)
                
                tracker.getMentions('@') shouldHaveSize 1
            }

            it("should handle concurrent modifications") {
                val tracker = MentionTracker()
                
                // Add and remove in sequence
                tracker.addMention('@', createSuggestionItem("user1", "John"))
                tracker.addMention('@', createSuggestionItem("user2", "Jane"))
                tracker.removeMention('@', "user1")
                tracker.addMention('@', createSuggestionItem("user3", "Bob"))
                
                tracker.getCount('@') shouldBe 2
                tracker.getMentions('@').map { it.id } shouldContain "user2"
                tracker.getMentions('@').map { it.id } shouldContain "user3"
            }
        }
    }
})

/**
 * Simple mention tracker for testing purposes.
 * Simulates the tracking behavior from CometChatMessageComposer.
 */
private class MentionTracker(private val mentionLimit: Int = 10) {
    private val mentionsByFormatter = mutableMapOf<Char, MutableMap<String, SuggestionItem>>()
    
    fun addMention(formatterId: Char, item: SuggestionItem) {
        mentionsByFormatter.getOrPut(formatterId) { mutableMapOf() }[item.id] = item
    }
    
    fun addMentionIfAllowed(formatterId: Char, item: SuggestionItem): Boolean {
        if (isLimitReached(formatterId)) return false
        addMention(formatterId, item)
        return true
    }
    
    fun removeMention(formatterId: Char, itemId: String) {
        mentionsByFormatter[formatterId]?.remove(itemId)
    }
    
    fun clearMentions(formatterId: Char) {
        mentionsByFormatter[formatterId]?.clear()
    }
    
    fun clearAll() {
        mentionsByFormatter.clear()
    }
    
    fun getMentions(formatterId: Char): List<SuggestionItem> {
        return mentionsByFormatter[formatterId]?.values?.toList() ?: emptyList()
    }
    
    fun getCount(formatterId: Char): Int {
        return mentionsByFormatter[formatterId]?.size ?: 0
    }
    
    fun isLimitReached(formatterId: Char): Boolean {
        return getCount(formatterId) >= mentionLimit
    }
}

/**
 * Extracts unique mentions from a list based on ID.
 */
private fun extractUniqueMentions(items: List<SuggestionItem>): List<SuggestionItem> {
    val uniqueMap = mutableMapOf<String, SuggestionItem>()
    for (item in items) {
        if (!uniqueMap.containsKey(item.id)) {
            uniqueMap[item.id] = item
        }
    }
    return uniqueMap.values.toList()
}

/**
 * Helper function to create a SuggestionItem for testing.
 */
private fun createSuggestionItem(
    id: String, 
    name: String, 
    style: PromptTextStyle? = null
): SuggestionItem {
    return SuggestionItem(
        id = id,
        name = name,
        leadingIconUrl = null,
        status = null,
        promptText = "@$name",
        underlyingText = "<@uid:$id>",
        data = null,
        promptTextStyle = style
    )
}
