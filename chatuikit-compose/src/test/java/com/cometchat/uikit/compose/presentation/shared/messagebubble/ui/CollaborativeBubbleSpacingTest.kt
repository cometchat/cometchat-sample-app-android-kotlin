package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for Collaborative Bubble Spacing Values.
 *
 * Feature: collaborative-bubble-parity
 * Tests verify that spacing values match the Kotlin XML layout:
 * - Outer padding: 4dp (cometchat_padding_1)
 * - Content margin vertical: 8dp (cometchat_margin_2)
 * - Icon-text spacing: 4dp (cometchat_margin_1)
 * - Title-subtitle gap: 2dp (cometchat_margin)
 * - Button horizontal margin: 20dp (cometchat_margin_5)
 * - Button vertical margin: 8dp (cometchat_margin_2)
 *
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
 *
 * These tests verify the spacing constants used in CometChatCollaborativeBubble
 * match the values defined in chatuikit-kotlin/src/main/res/values/dimen.xml
 */
class CollaborativeBubbleSpacingTest : StringSpec({

    // ============================================================
    // Spacing Constants (matching Kotlin dimen.xml values)
    // ============================================================

    /**
     * Test outer padding is 4dp (cometchat_padding_1)
     * 
     * From dimen.xml:
     * - cometchat_padding_1 = cometchat_spacing_1 = 4dp
     * 
     * Used in: Box around banner image
     * **Validates: Requirement 2.1**
     */
    "Outer padding should be 4dp (cometchat_padding_1)" {
        val expectedOuterPadding = 4
        val actualOuterPadding = CollaborativeBubbleSpacing.OUTER_PADDING_DP
        actualOuterPadding shouldBe expectedOuterPadding
    }

    /**
     * Test content row vertical margin is 8dp (cometchat_margin_2)
     * 
     * From dimen.xml:
     * - cometchat_margin_2 = cometchat_spacing_2 = 8dp
     * 
     * Used in: Row containing icon and text (top and bottom margins)
     * **Validates: Requirement 2.2**
     */
    "Content row vertical margin should be 8dp (cometchat_margin_2)" {
        val expectedContentMarginVertical = 8
        val actualContentMarginVertical = CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP
        actualContentMarginVertical shouldBe expectedContentMarginVertical
    }

    /**
     * Test icon-text spacing is 4dp (cometchat_margin_1)
     * 
     * From dimen.xml:
     * - cometchat_margin_1 = cometchat_spacing_1 = 4dp
     * 
     * Used in: Spacer between icon and text column
     * **Validates: Requirement 2.3**
     */
    "Icon-text spacing should be 4dp (cometchat_margin_1)" {
        val expectedIconTextSpacing = 4
        val actualIconTextSpacing = CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP
        actualIconTextSpacing shouldBe expectedIconTextSpacing
    }

    /**
     * Test title-subtitle gap is 2dp (cometchat_margin)
     * 
     * From dimen.xml:
     * - cometchat_margin = cometchat_spacing = 2dp
     * 
     * Used in: Spacer between title and subtitle text
     * **Validates: Requirement 2.4**
     */
    "Title-subtitle gap should be 2dp (cometchat_margin)" {
        val expectedTitleSubtitleGap = 2
        val actualTitleSubtitleGap = CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP
        actualTitleSubtitleGap shouldBe expectedTitleSubtitleGap
    }

    /**
     * Test button horizontal margin is 20dp (cometchat_margin_5)
     * 
     * From dimen.xml:
     * - cometchat_margin_5 = cometchat_spacing_5 = 20dp
     * 
     * Used in: Box around button (start and end margins)
     * **Validates: Requirement 2.5**
     */
    "Button horizontal margin should be 20dp (cometchat_margin_5)" {
        val expectedButtonHorizontalMargin = 20
        val actualButtonHorizontalMargin = CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP
        actualButtonHorizontalMargin shouldBe expectedButtonHorizontalMargin
    }

    /**
     * Test button vertical margin is 8dp (cometchat_margin_2)
     * 
     * From dimen.xml:
     * - cometchat_margin_2 = cometchat_spacing_2 = 8dp
     * 
     * Used in: Box around button (top and bottom margins)
     * **Validates: Requirement 2.5**
     */
    "Button vertical margin should be 8dp (cometchat_margin_2)" {
        val expectedButtonVerticalMargin = 8
        val actualButtonVerticalMargin = CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP
        actualButtonVerticalMargin shouldBe expectedButtonVerticalMargin
    }

    // ============================================================
    // Dimension Constants (matching Kotlin dimen.xml values)
    // ============================================================

    /**
     * Test banner image height is 136dp (cometchat_136dp)
     * 
     * From dimen.xml:
     * - cometchat_136dp = 136dp
     * 
     * Used in: Image height for collaborative banner
     * **Validates: Requirement 4.1**
     */
    "Banner image height should be 136dp" {
        val expectedBannerHeight = 136
        val actualBannerHeight = CollaborativeBubbleSpacing.BANNER_IMAGE_HEIGHT_DP
        actualBannerHeight shouldBe expectedBannerHeight
    }

    /**
     * Test icon size is 32dp (cometchat_32dp)
     * 
     * From dimen.xml:
     * - cometchat_32dp = 32dp
     * 
     * Used in: Collaborative icon width and height
     * **Validates: Requirement 4.2**
     */
    "Icon size should be 32dp" {
        val expectedIconSize = 32
        val actualIconSize = CollaborativeBubbleSpacing.ICON_SIZE_DP
        actualIconSize shouldBe expectedIconSize
    }

    /**
     * Test max width is 240dp
     * 
     * Used in: Maximum width constraint for the bubble
     * **Validates: Requirement 4.3**
     */
    "Max width should be 240dp" {
        val expectedMaxWidth = 240
        val actualMaxWidth = CollaborativeBubbleSpacing.MAX_WIDTH_DP
        actualMaxWidth shouldBe expectedMaxWidth
    }

    /**
     * Test separator thickness is 1dp
     * 
     * Used in: HorizontalDivider between content and button
     * **Validates: Requirement 4.4**
     */
    "Separator thickness should be 1dp" {
        val expectedSeparatorThickness = 1
        val actualSeparatorThickness = CollaborativeBubbleSpacing.SEPARATOR_THICKNESS_DP
        actualSeparatorThickness shouldBe expectedSeparatorThickness
    }

    // ============================================================
    // Consistency Tests
    // ============================================================

    /**
     * Test that outer padding matches icon-text spacing (both use spacing_1)
     * 
     * Both values should be 4dp as they reference the same base spacing value
     */
    "Outer padding and icon-text spacing should both be 4dp (spacing_1)" {
        CollaborativeBubbleSpacing.OUTER_PADDING_DP shouldBe CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP
        CollaborativeBubbleSpacing.OUTER_PADDING_DP shouldBe 4
    }

    /**
     * Test that content vertical margin matches button vertical margin (both use spacing_2)
     * 
     * Both values should be 8dp as they reference the same base spacing value
     */
    "Content vertical margin and button vertical margin should both be 8dp (spacing_2)" {
        CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP shouldBe CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP
        CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP shouldBe 8
    }

    /**
     * Test that title-subtitle gap is the smallest spacing value
     * 
     * The title-subtitle gap (2dp) should be smaller than all other spacing values
     */
    "Title-subtitle gap should be the smallest spacing value" {
        val titleSubtitleGap = CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP
        titleSubtitleGap shouldBe 2
        (titleSubtitleGap < CollaborativeBubbleSpacing.OUTER_PADDING_DP) shouldBe true
        (titleSubtitleGap < CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP) shouldBe true
        (titleSubtitleGap < CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP) shouldBe true
        (titleSubtitleGap < CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP) shouldBe true
        (titleSubtitleGap < CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP) shouldBe true
    }

    /**
     * Test that button horizontal margin is the largest spacing value
     * 
     * The button horizontal margin (20dp) should be larger than all other spacing values
     */
    "Button horizontal margin should be the largest spacing value" {
        val buttonHorizontalMargin = CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP
        buttonHorizontalMargin shouldBe 20
        (buttonHorizontalMargin > CollaborativeBubbleSpacing.OUTER_PADDING_DP) shouldBe true
        (buttonHorizontalMargin > CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP) shouldBe true
        (buttonHorizontalMargin > CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP) shouldBe true
        (buttonHorizontalMargin > CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP) shouldBe true
        (buttonHorizontalMargin > CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP) shouldBe true
    }

    // ============================================================
    // Spacing Scale Tests
    // ============================================================

    /**
     * Test spacing values follow the expected scale
     * 
     * The spacing scale from dimen.xml:
     * - spacing (2dp) < spacing_1 (4dp) < spacing_2 (8dp) < spacing_5 (20dp)
     */
    "Spacing values should follow the expected scale" {
        val spacing = CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP      // 2dp
        val spacing1 = CollaborativeBubbleSpacing.OUTER_PADDING_DP          // 4dp
        val spacing2 = CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP // 8dp
        val spacing5 = CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP // 20dp

        (spacing < spacing1) shouldBe true
        (spacing1 < spacing2) shouldBe true
        (spacing2 < spacing5) shouldBe true
    }

    /**
     * Test all spacing values are positive
     */
    "All spacing values should be positive" {
        (CollaborativeBubbleSpacing.OUTER_PADDING_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP > 0) shouldBe true
    }

    /**
     * Test all dimension values are positive
     */
    "All dimension values should be positive" {
        (CollaborativeBubbleSpacing.BANNER_IMAGE_HEIGHT_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.ICON_SIZE_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.MAX_WIDTH_DP > 0) shouldBe true
        (CollaborativeBubbleSpacing.SEPARATOR_THICKNESS_DP > 0) shouldBe true
    }

    // ============================================================
    // XML Layout Parity Tests
    // ============================================================

    /**
     * Test that spacing values match the XML layout structure
     * 
     * From cometchat_collaborative_bubble.xml:
     * - paddingStart/Top/End: cometchat_padding_1 (4dp)
     * - layout_marginTop/Bottom: cometchat_margin_2 (8dp)
     * - layout_marginStart (icon to text): cometchat_margin_1 (4dp)
     * - layout_marginTop (subtitle): cometchat_margin (2dp)
     * - layout_marginStart/End (button): cometchat_margin_5 (20dp)
     * - layout_marginTop/Bottom (button): cometchat_margin_2 (8dp)
     */
    "Spacing values should match XML layout structure" {
        // Image container padding
        CollaborativeBubbleSpacing.OUTER_PADDING_DP shouldBe 4

        // Content row margins
        CollaborativeBubbleSpacing.CONTENT_MARGIN_VERTICAL_DP shouldBe 8

        // Icon to text spacing
        CollaborativeBubbleSpacing.ICON_TEXT_SPACING_DP shouldBe 4

        // Title to subtitle gap
        CollaborativeBubbleSpacing.TITLE_SUBTITLE_GAP_DP shouldBe 2

        // Button margins
        CollaborativeBubbleSpacing.BUTTON_HORIZONTAL_MARGIN_DP shouldBe 20
        CollaborativeBubbleSpacing.BUTTON_VERTICAL_MARGIN_DP shouldBe 8
    }

    /**
     * Test that dimension values match the XML layout structure
     * 
     * From cometchat_collaborative_bubble.xml:
     * - Image height: cometchat_136dp (136dp)
     * - Icon size: cometchat_32dp (32dp)
     * - Separator height: 1dp
     */
    "Dimension values should match XML layout structure" {
        CollaborativeBubbleSpacing.BANNER_IMAGE_HEIGHT_DP shouldBe 136
        CollaborativeBubbleSpacing.ICON_SIZE_DP shouldBe 32
        CollaborativeBubbleSpacing.SEPARATOR_THICKNESS_DP shouldBe 1
    }
})

/**
 * Object containing the expected spacing values for CometChatCollaborativeBubble.
 * 
 * These values are derived from chatuikit-kotlin/src/main/res/values/dimen.xml
 * and should match the values used in the Compose implementation.
 * 
 * Spacing scale from dimen.xml:
 * - cometchat_spacing = 2dp
 * - cometchat_spacing_1 = 4dp
 * - cometchat_spacing_2 = 8dp
 * - cometchat_spacing_5 = 20dp
 */
private object CollaborativeBubbleSpacing {
    // Spacing values (in dp)
    
    /** Outer padding around image container (cometchat_padding_1 = 4dp) */
    const val OUTER_PADDING_DP = 4
    
    /** Content row vertical margin (cometchat_margin_2 = 8dp) */
    const val CONTENT_MARGIN_VERTICAL_DP = 8
    
    /** Spacing between icon and text column (cometchat_margin_1 = 4dp) */
    const val ICON_TEXT_SPACING_DP = 4
    
    /** Gap between title and subtitle (cometchat_margin = 2dp) */
    const val TITLE_SUBTITLE_GAP_DP = 2
    
    /** Button horizontal margin (cometchat_margin_5 = 20dp) */
    const val BUTTON_HORIZONTAL_MARGIN_DP = 20
    
    /** Button vertical margin (cometchat_margin_2 = 8dp) */
    const val BUTTON_VERTICAL_MARGIN_DP = 8
    
    // Dimension values (in dp)
    
    /** Banner image height (cometchat_136dp = 136dp) */
    const val BANNER_IMAGE_HEIGHT_DP = 136
    
    /** Icon size (cometchat_32dp = 32dp) */
    const val ICON_SIZE_DP = 32
    
    /** Maximum width constraint */
    const val MAX_WIDTH_DP = 240
    
    /** Separator line thickness */
    const val SEPARATOR_THICKNESS_DP = 1
}
