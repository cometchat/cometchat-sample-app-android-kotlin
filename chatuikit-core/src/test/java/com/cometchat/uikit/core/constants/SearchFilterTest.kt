package com.cometchat.uikit.core.constants

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for SearchFilter enum.
 *
 * Tests verify that:
 * - isMessageFilter() returns true for PHOTOS, VIDEOS, DOCUMENTS, LINKS, AUDIO
 * - isMessageFilter() returns false for GROUPS, UNREAD
 * - isConversationFilter() returns true for GROUPS, UNREAD
 * - isConversationFilter() returns false for PHOTOS, VIDEOS, DOCUMENTS, LINKS, AUDIO
 *
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8**
 */
class SearchFilterTest : FunSpec({

    context("isMessageFilter()") {

        test("PHOTOS should be a message filter") {
            SearchFilter.PHOTOS.isMessageFilter() shouldBe true
        }

        test("VIDEOS should be a message filter") {
            SearchFilter.VIDEOS.isMessageFilter() shouldBe true
        }

        test("DOCUMENTS should be a message filter") {
            SearchFilter.DOCUMENTS.isMessageFilter() shouldBe true
        }

        test("LINKS should be a message filter") {
            SearchFilter.LINKS.isMessageFilter() shouldBe true
        }

        test("AUDIO should be a message filter") {
            SearchFilter.AUDIO.isMessageFilter() shouldBe true
        }

        test("GROUPS should NOT be a message filter") {
            SearchFilter.GROUPS.isMessageFilter() shouldBe false
        }

        test("UNREAD should NOT be a message filter") {
            SearchFilter.UNREAD.isMessageFilter() shouldBe false
        }
    }

    context("isConversationFilter()") {

        test("GROUPS should be a conversation filter") {
            SearchFilter.GROUPS.isConversationFilter() shouldBe true
        }

        test("UNREAD should be a conversation filter") {
            SearchFilter.UNREAD.isConversationFilter() shouldBe true
        }

        test("PHOTOS should NOT be a conversation filter") {
            SearchFilter.PHOTOS.isConversationFilter() shouldBe false
        }

        test("VIDEOS should NOT be a conversation filter") {
            SearchFilter.VIDEOS.isConversationFilter() shouldBe false
        }

        test("DOCUMENTS should NOT be a conversation filter") {
            SearchFilter.DOCUMENTS.isConversationFilter() shouldBe false
        }

        test("LINKS should NOT be a conversation filter") {
            SearchFilter.LINKS.isConversationFilter() shouldBe false
        }

        test("AUDIO should NOT be a conversation filter") {
            SearchFilter.AUDIO.isConversationFilter() shouldBe false
        }
    }

    context("Filter categories are mutually exclusive") {

        test("all message filters should not be conversation filters") {
            val messageFilters = listOf(
                SearchFilter.PHOTOS,
                SearchFilter.VIDEOS,
                SearchFilter.DOCUMENTS,
                SearchFilter.LINKS,
                SearchFilter.AUDIO
            )

            messageFilters.forEach { filter ->
                filter.isMessageFilter() shouldBe true
                filter.isConversationFilter() shouldBe false
            }
        }

        test("all conversation filters should not be message filters") {
            val conversationFilters = listOf(
                SearchFilter.GROUPS,
                SearchFilter.UNREAD
            )

            conversationFilters.forEach { filter ->
                filter.isConversationFilter() shouldBe true
                filter.isMessageFilter() shouldBe false
            }
        }

        test("every SearchFilter should be either a message filter or conversation filter") {
            SearchFilter.entries.forEach { filter ->
                val isMessage = filter.isMessageFilter()
                val isConversation = filter.isConversationFilter()

                // Each filter should be exactly one type (XOR)
                (isMessage xor isConversation) shouldBe true
            }
        }
    }

    context("Filter values") {

        test("PHOTOS should have value 'photos'") {
            SearchFilter.PHOTOS.value shouldBe "photos"
        }

        test("VIDEOS should have value 'videos'") {
            SearchFilter.VIDEOS.value shouldBe "videos"
        }

        test("DOCUMENTS should have value 'documents'") {
            SearchFilter.DOCUMENTS.value shouldBe "documents"
        }

        test("LINKS should have value 'links'") {
            SearchFilter.LINKS.value shouldBe "links"
        }

        test("AUDIO should have value 'audio'") {
            SearchFilter.AUDIO.value shouldBe "audio"
        }

        test("GROUPS should have value 'groups'") {
            SearchFilter.GROUPS.value shouldBe "groups"
        }

        test("UNREAD should have value 'unread'") {
            SearchFilter.UNREAD.value shouldBe "unread"
        }
    }
})
