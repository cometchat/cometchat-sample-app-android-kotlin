package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for Video Bubble Play Overlay Presence.
 *
 * Feature: video-audio-bubbles
 * Properties tested:
 * - Property 3: Play Overlay Presence in Grid
 *
 * **Validates: Requirements 2.6**
 *
 * Tests that each visible video thumbnail in the grid has exactly one play icon
 * overlay rendered on top of it, EXCEPT when the "+N" overlay is shown on the
 * 4th item for grids with 5+ videos.
 *
 * Grid Layout Rules:
 * - 1 video: Single video with play overlay
 * - 2 videos: 2 videos side by side, both with play overlay
 * - 3-4 videos: 2x2 grid, all with play overlay
 * - 5+ videos: 2x2 grid, items 1-3 with play overlay, item 4 with "+N" overlay (no play)
 *
 * The play overlay visibility is determined by the `showMoreOverlay` parameter
 * in the GridVideoItem composable. When `showMoreOverlay` is true, the play
 * overlay is hidden and replaced with the "+N" overlay.
 */
class VideoBubblePlayOverlayPropertyTest : StringSpec({

    /**
     * Property 3: Play Overlay Presence in Grid
     *
     * *For any* grid of video thumbnails, each visible thumbnail SHALL have
     * exactly one play icon overlay rendered on top of it, EXCEPT when the
     * "+N" overlay is displayed on the 4th item.
     *
     * **Validates: Requirements 2.6**
     */
    "Property 3: All items should show play overlay for grids with 1-4 videos" {
        // Test with attachment counts from 1 to 4
        val attachmentCountArb = Arb.int(1..4)

        checkAll(attachmentCountArb) { totalCount ->
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // All visible items should have play overlay
            playOverlayVisibility.forEach { (index, showsPlayOverlay) ->
                showsPlayOverlay shouldBe true
            }
            
            // Number of items with play overlay should equal total count
            playOverlayVisibility.count { it.value } shouldBe totalCount
        }
    }

    "Property 3: Items 1-3 should show play overlay for grids with 5+ videos" {
        // Test with attachment counts > 4
        val attachmentCountArb = Arb.int(5..1000)

        checkAll(100, attachmentCountArb) { totalCount ->
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // Items at indices 0, 1, 2 should have play overlay
            playOverlayVisibility[0] shouldBe true
            playOverlayVisibility[1] shouldBe true
            playOverlayVisibility[2] shouldBe true
        }
    }

    "Property 3: Item 4 should NOT show play overlay for grids with 5+ videos" {
        // Test with attachment counts > 4
        val attachmentCountArb = Arb.int(5..1000)

        checkAll(100, attachmentCountArb) { totalCount ->
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // Item at index 3 (4th item) should NOT have play overlay
            // because it shows the "+N" overlay instead
            playOverlayVisibility[3] shouldBe false
        }
    }

    "Property 3: Exactly 3 items should show play overlay for grids with 5+ videos" {
        // Test with attachment counts > 4
        val attachmentCountArb = Arb.int(5..1000)

        checkAll(100, attachmentCountArb) { totalCount ->
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // Exactly 3 items should have play overlay (indices 0, 1, 2)
            playOverlayVisibility.count { it.value } shouldBe 3
        }
    }

    "Property 3: Single video should always show play overlay" {
        val playOverlayVisibility = calculatePlayOverlayVisibility(1)
        
        playOverlayVisibility.size shouldBe 1
        playOverlayVisibility[0] shouldBe true
    }

    "Property 3: Two videos should both show play overlay" {
        val playOverlayVisibility = calculatePlayOverlayVisibility(2)
        
        playOverlayVisibility.size shouldBe 2
        playOverlayVisibility[0] shouldBe true
        playOverlayVisibility[1] shouldBe true
    }

    "Property 3: Three videos should all show play overlay" {
        val playOverlayVisibility = calculatePlayOverlayVisibility(3)
        
        playOverlayVisibility.size shouldBe 3
        playOverlayVisibility[0] shouldBe true
        playOverlayVisibility[1] shouldBe true
        playOverlayVisibility[2] shouldBe true
    }

    "Property 3: Four videos should all show play overlay" {
        val playOverlayVisibility = calculatePlayOverlayVisibility(4)
        
        playOverlayVisibility.size shouldBe 4
        playOverlayVisibility[0] shouldBe true
        playOverlayVisibility[1] shouldBe true
        playOverlayVisibility[2] shouldBe true
        playOverlayVisibility[3] shouldBe true
    }

    "Property 3: Boundary test - transition from 4 to 5 videos" {
        // At exactly 4 videos, all items show play overlay
        val visibility4 = calculatePlayOverlayVisibility(4)
        visibility4.size shouldBe 4
        visibility4.all { it.value } shouldBe true
        
        // At exactly 5 videos, 4th item shows "+N" overlay instead of play
        val visibility5 = calculatePlayOverlayVisibility(5)
        visibility5.size shouldBe 4
        visibility5[0] shouldBe true
        visibility5[1] shouldBe true
        visibility5[2] shouldBe true
        visibility5[3] shouldBe false // Shows "+1" overlay instead
    }

    "Property 3: Play overlay count should be consistent with grid config" {
        // For any attachment count, verify play overlay count matches expected
        val attachmentCountArb = Arb.int(1..1000)

        checkAll(200, attachmentCountArb) { totalCount ->
            val gridConfig = calculateVideoGridLayout(totalCount)
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            val expectedPlayOverlayCount = if (gridConfig.showMore) {
                // When showMore is true, 4th item doesn't show play overlay
                3
            } else {
                // When showMore is false, all visible items show play overlay
                minOf(totalCount, 4)
            }
            
            playOverlayVisibility.count { it.value } shouldBe expectedPlayOverlayCount
        }
    }

    "Property 3: Play overlay visibility is mutually exclusive with more overlay" {
        // For any grid with 5+ videos, the 4th item should show either
        // play overlay OR more overlay, never both
        val attachmentCountArb = Arb.int(5..1000)

        checkAll(100, attachmentCountArb) { totalCount ->
            val gridConfig = calculateVideoGridLayout(totalCount)
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // 4th item (index 3) should have showMore = true and play overlay = false
            gridConfig.showMore shouldBe true
            playOverlayVisibility[3] shouldBe false
        }
    }

    "Property 3: Total visible items should be min(totalCount, 4)" {
        val attachmentCountArb = Arb.int(1..1000)

        checkAll(200, attachmentCountArb) { totalCount ->
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // Number of visible items in grid is capped at 4
            playOverlayVisibility.size shouldBe minOf(totalCount, 4)
        }
    }

    "Property 3: Play overlay visibility follows showMoreOverlay logic" {
        // This test verifies the exact logic used in GridVideoItem:
        // Play overlay is shown when showMoreOverlay is false
        val attachmentCountArb = Arb.int(1..1000)

        checkAll(200, attachmentCountArb) { totalCount ->
            val gridConfig = calculateVideoGridLayout(totalCount)
            val playOverlayVisibility = calculatePlayOverlayVisibility(totalCount)
            
            // For each visible item, verify play overlay matches !showMoreOverlay
            playOverlayVisibility.forEach { (index, showsPlayOverlay) ->
                val isLastVisibleItem = index == 3
                val showMoreOverlay = isLastVisibleItem && gridConfig.showMore
                
                // Play overlay should be shown when showMoreOverlay is false
                showsPlayOverlay shouldBe !showMoreOverlay
            }
        }
    }
})

/**
 * Calculates play overlay visibility for each visible item in the video grid.
 *
 * This function mirrors the logic in [GridVideoItem] composable where:
 * - Play overlay is shown when `showMoreOverlay` is false
 * - The 4th item (index 3) has `showMoreOverlay = true` when there are 5+ videos
 *
 * @param totalAttachments The total number of video attachments
 * @return A map of item index to play overlay visibility (true = shows play overlay)
 */
private fun calculatePlayOverlayVisibility(totalAttachments: Int): Map<Int, Boolean> {
    val gridConfig = calculateVideoGridLayout(totalAttachments)
    val visibleCount = minOf(totalAttachments, 4)
    
    return (0 until visibleCount).associate { index ->
        val isLastVisibleItem = index == 3
        val showMoreOverlay = isLastVisibleItem && gridConfig.showMore
        
        // Play overlay is shown when showMoreOverlay is false
        // This matches the logic in GridVideoItem: if (!showMoreOverlay) { PlayButtonOverlay(...) }
        index to !showMoreOverlay
    }
}
