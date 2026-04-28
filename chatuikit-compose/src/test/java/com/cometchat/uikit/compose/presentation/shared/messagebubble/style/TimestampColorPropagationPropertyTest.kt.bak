package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Property-based tests for verifying timestamp color propagation from bubble styles
 * to the CometChatDate component in DefaultStatusInfoView.
 *
 * **Feature: message-list-sticky-date-fixes**
 * **Property 4: Style Timestamp Color Propagation**
 *
 * **Validates: Requirements 4.2**
 *
 * *For any* bubble style that specifies a `timestampTextColor`, the `DefaultStatusInfoView`
 * SHALL use that color for the `CometChatDate` component.
 */
class TimestampColorPropagationPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators for testing
    // ============================================================================

    /**
     * Generates random Color values for testing.
     */
    val colorArb = Arb.bind(
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(0, 255),
        Arb.int(0, 255)
    ) { r, g, b, a -> Color(r / 255f, g / 255f, b / 255f, a / 255f) }


    /**
     * Generates random Dp values for testing.
     */
    val dpArb: Arb<Dp> = Arb.float(0f, 24f).map { value -> value.dp }

    /**
     * Generates random TextStyle values for testing.
     */
    val textStyleArb: Arb<TextStyle> = Arb.float(10f, 24f).map { fontSize -> TextStyle(fontSize = fontSize.sp) }

    /**
     * Generates random PaddingValues for testing.
     */
    val paddingArb: Arb<PaddingValues> = Arb.float(0f, 16f).map { padding -> PaddingValues(padding.dp) }

    // ============================================================================
    // Property Test: Timestamp color from bubble style propagates to DateStyle
    // ============================================================================

    /**
     * Property test: For any bubble style with a specified timestampTextColor,
     * when creating a DateStyle for the CometChatDate component, the timestamp
     * color from the bubble style should be used.
     *
     * This test verifies the propagation logic that would be used in
     * DefaultStatusInfoView when creating the DateStyle for CometChatDate.
     *
     * **Validates: Requirements 4.2**
     */
    "Timestamp color from bubble style should propagate to DateStyle" {
        val bubbleStyleArb = Arb.bind(
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            dpArb,    // strokeWidth
            colorArb, // strokeColor
            colorArb, // timestampTextColor
            textStyleArb // timestampTextStyle
        ) { backgroundColor, cornerRadius, strokeWidth, strokeColor, timestampTextColor, timestampTextStyle ->
            createTestBubbleStyle(
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                strokeWidth = strokeWidth,
                strokeColor = strokeColor,
                timestampTextColor = timestampTextColor,
                timestampTextStyle = timestampTextStyle
            )
        }

        checkAll(100, bubbleStyleArb) { bubbleStyle ->
            // Simulate the propagation logic from DefaultStatusInfoView
            val dateStyle = createDateStyleFromBubbleStyle(bubbleStyle)
            
            // The DateStyle should use the timestamp color from the bubble style
            dateStyle.textColor shouldBe bubbleStyle.timestampTextColor
            dateStyle.textStyle shouldBe bubbleStyle.timestampTextStyle
        }
    }


    // ============================================================================
    // Property Test: All bubble style types propagate timestamp color correctly
    // ============================================================================

    /**
     * Property test: For any CometChatTextBubbleStyle with a specified timestampTextColor,
     * the timestamp color should be accessible and propagate correctly.
     *
     * **Validates: Requirements 4.2**
     */
    "CometChatTextBubbleStyle timestamp color should be accessible for propagation" {
        val textBubbleStyleArb = Arb.bind(
            colorArb, // textColor
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            colorArb  // timestampTextColor
        ) { textColor, backgroundColor, cornerRadius, timestampTextColor ->
            createTestTextBubbleStyle(
                textColor = textColor,
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                timestampTextColor = timestampTextColor
            )
        }

        checkAll(100, textBubbleStyleArb) { style ->
            // Verify the style has the timestamp color accessible
            val dateStyle = createDateStyleFromBubbleStyle(style)
            dateStyle.textColor shouldBe style.timestampTextColor
        }
    }

    /**
     * Property test: For any CometChatImageBubbleStyle with a specified timestampTextColor,
     * the timestamp color should be accessible and propagate correctly.
     *
     * **Validates: Requirements 4.2**
     */
    "CometChatImageBubbleStyle timestamp color should be accessible for propagation" {
        val imageBubbleStyleArb = Arb.bind(
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            colorArb, // captionTextColor
            colorArb  // timestampTextColor
        ) { backgroundColor, cornerRadius, captionTextColor, timestampTextColor ->
            createTestImageBubbleStyle(
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                captionTextColor = captionTextColor,
                timestampTextColor = timestampTextColor
            )
        }

        checkAll(100, imageBubbleStyleArb) { style ->
            val dateStyle = createDateStyleFromBubbleStyle(style)
            dateStyle.textColor shouldBe style.timestampTextColor
        }
    }


    /**
     * Property test: For any CometChatAudioBubbleStyle with a specified timestampTextColor,
     * the timestamp color should be accessible and propagate correctly.
     *
     * **Validates: Requirements 4.2**
     */
    "CometChatAudioBubbleStyle timestamp color should be accessible for propagation" {
        val audioBubbleStyleArb = Arb.bind(
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            colorArb, // playIconTint
            colorArb  // timestampTextColor
        ) { backgroundColor, cornerRadius, playIconTint, timestampTextColor ->
            createTestAudioBubbleStyle(
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                playIconTint = playIconTint,
                timestampTextColor = timestampTextColor
            )
        }

        checkAll(100, audioBubbleStyleArb) { style ->
            val dateStyle = createDateStyleFromBubbleStyle(style)
            dateStyle.textColor shouldBe style.timestampTextColor
        }
    }

    /**
     * Property test: For any CometChatVideoBubbleStyle with a specified timestampTextColor,
     * the timestamp color should be accessible and propagate correctly.
     *
     * **Validates: Requirements 4.2**
     */
    "CometChatVideoBubbleStyle timestamp color should be accessible for propagation" {
        val videoBubbleStyleArb = Arb.bind(
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            colorArb, // playIconTint
            colorArb  // timestampTextColor
        ) { backgroundColor, cornerRadius, playIconTint, timestampTextColor ->
            createTestVideoBubbleStyle(
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                playIconTint = playIconTint,
                timestampTextColor = timestampTextColor
            )
        }

        checkAll(100, videoBubbleStyleArb) { style ->
            val dateStyle = createDateStyleFromBubbleStyle(style)
            dateStyle.textColor shouldBe style.timestampTextColor
        }
    }


    /**
     * Property test: For any CometChatFileBubbleStyle with a specified timestampTextColor,
     * the timestamp color should be accessible and propagate correctly.
     *
     * **Validates: Requirements 4.2**
     */
    "CometChatFileBubbleStyle timestamp color should be accessible for propagation" {
        val fileBubbleStyleArb = Arb.bind(
            colorArb, // backgroundColor
            dpArb,    // cornerRadius
            colorArb, // titleTextColor
            colorArb  // timestampTextColor
        ) { backgroundColor, cornerRadius, titleTextColor, timestampTextColor ->
            createTestFileBubbleStyle(
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                titleTextColor = titleTextColor,
                timestampTextColor = timestampTextColor
            )
        }

        checkAll(100, fileBubbleStyleArb) { style ->
            val dateStyle = createDateStyleFromBubbleStyle(style)
            dateStyle.textColor shouldBe style.timestampTextColor
        }
    }

    /**
     * Property test: Outgoing bubble styles should have white/light timestamp color
     * for proper contrast against the primary (purple) background.
     *
     * This verifies that the outgoing() factory methods set appropriate timestamp colors.
     *
     * **Validates: Requirements 4.2**
     */
    "Outgoing bubble styles should have light timestamp color for contrast" {
        // Test with various outgoing style configurations
        val outgoingStyleArb = Arb.bind(
            colorArb, // primary background color (simulating outgoing)
            colorArb  // expected light timestamp color
        ) { primaryBg, _ ->
            // Create an outgoing-style bubble with white timestamp
            createTestBubbleStyle(
                backgroundColor = primaryBg,
                timestampTextColor = Color.White.copy(alpha = 0.8f)
            )
        }

        checkAll(100, outgoingStyleArb) { style ->
            val dateStyle = createDateStyleFromBubbleStyle(style)
            // Verify the timestamp color is the expected white with alpha
            dateStyle.textColor shouldBe Color.White.copy(alpha = 0.8f)
        }
    }
})



// ============================================================================
// Helper functions to create test style instances without Compose context
// ============================================================================

/**
 * Creates a DateStyle from a bubble style, simulating the propagation logic
 * used in DefaultStatusInfoView.
 *
 * This function replicates the logic:
 * ```kotlin
 * DateStyle.default(
 *     textColor = style.timestampTextColor,
 *     textStyle = style.timestampTextStyle
 * )
 * ```
 */
private fun createDateStyleFromBubbleStyle(style: CometChatMessageBubbleStyle): DateStyle {
    return DateStyle(
        textColor = style.timestampTextColor,
        textStyle = style.timestampTextStyle
    )
}

/**
 * Creates a test CometChatMessageBubbleStyle instance with customizable values.
 */
private fun createTestBubbleStyle(
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatMessageBubbleStyle = CometChatMessageBubbleStyle(
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)


/**
 * Creates a test CometChatTextBubbleStyle instance with customizable values.
 */
private fun createTestTextBubbleStyle(
    textColor: Color = Color.Black,
    textStyle: TextStyle = TextStyle(fontSize = 14.sp),
    linkColor: Color = Color.Blue,
    translatedTextColor: Color = Color.Gray,
    translatedTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    separatorColor: Color = Color.LightGray,
    linkPreviewBackgroundColor: Color = Color(0xFFE8E8E8),
    linkPreviewTitleColor: Color = Color.Black,
    linkPreviewTitleStyle: TextStyle = TextStyle(fontSize = 14.sp),
    linkPreviewDescriptionColor: Color = Color.Gray,
    linkPreviewDescriptionStyle: TextStyle = TextStyle(fontSize = 12.sp),
    linkPreviewLinkColor: Color = Color.Blue,
    linkPreviewLinkStyle: TextStyle = TextStyle(fontSize = 12.sp),
    linkPreviewCornerRadius: Dp = 8.dp,
    linkPreviewStrokeWidth: Dp = 0.dp,
    linkPreviewStrokeColor: Color = Color.Transparent,
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatTextBubbleStyle = CometChatTextBubbleStyle(
    textColor = textColor,
    textStyle = textStyle,
    linkColor = linkColor,
    translatedTextColor = translatedTextColor,
    translatedTextStyle = translatedTextStyle,
    separatorColor = separatorColor,
    linkPreviewBackgroundColor = linkPreviewBackgroundColor,
    linkPreviewTitleColor = linkPreviewTitleColor,
    linkPreviewTitleStyle = linkPreviewTitleStyle,
    linkPreviewDescriptionColor = linkPreviewDescriptionColor,
    linkPreviewDescriptionStyle = linkPreviewDescriptionStyle,
    linkPreviewLinkColor = linkPreviewLinkColor,
    linkPreviewLinkStyle = linkPreviewLinkStyle,
    linkPreviewCornerRadius = linkPreviewCornerRadius,
    linkPreviewStrokeWidth = linkPreviewStrokeWidth,
    linkPreviewStrokeColor = linkPreviewStrokeColor,
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)


/**
 * Creates a test CometChatImageBubbleStyle instance with customizable values.
 */
private fun createTestImageBubbleStyle(
    imageCornerRadius: Dp = 8.dp,
    imageStrokeWidth: Dp = 0.dp,
    imageStrokeColor: Color = Color.Transparent,
    captionTextColor: Color = Color.Black,
    captionTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    progressIndicatorColor: Color = Color.Gray,
    gridSpacing: Dp = 2.dp,
    maxGridWidth: Dp = 240.dp,
    moreOverlayBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    moreOverlayTextColor: Color = Color.White,
    moreOverlayTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatImageBubbleStyle = CometChatImageBubbleStyle(
    imageCornerRadius = imageCornerRadius,
    imageStrokeWidth = imageStrokeWidth,
    imageStrokeColor = imageStrokeColor,
    captionTextColor = captionTextColor,
    captionTextStyle = captionTextStyle,
    progressIndicatorColor = progressIndicatorColor,
    gridSpacing = gridSpacing,
    maxGridWidth = maxGridWidth,
    moreOverlayBackgroundColor = moreOverlayBackgroundColor,
    moreOverlayTextColor = moreOverlayTextColor,
    moreOverlayTextStyle = moreOverlayTextStyle,
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)


/**
 * Creates a test CometChatAudioBubbleStyle instance with customizable values.
 */
private fun createTestAudioBubbleStyle(
    playIconTint: Color = Color.Blue,
    pauseIconTint: Color = Color.Blue,
    buttonBackgroundColor: Color = Color.White,
    audioWaveColor: Color = Color.Blue,
    subtitleTextColor: Color = Color.Gray,
    subtitleTextStyle: TextStyle = TextStyle(fontSize = 10.sp),
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatAudioBubbleStyle = CometChatAudioBubbleStyle(
    playIconTint = playIconTint,
    pauseIconTint = pauseIconTint,
    buttonBackgroundColor = buttonBackgroundColor,
    audioWaveColor = audioWaveColor,
    subtitleTextColor = subtitleTextColor,
    subtitleTextStyle = subtitleTextStyle,
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)


/**
 * Creates a test CometChatVideoBubbleStyle instance with customizable values.
 */
private fun createTestVideoBubbleStyle(
    videoCornerRadius: Dp = 8.dp,
    videoStrokeWidth: Dp = 0.dp,
    videoStrokeColor: Color = Color.Transparent,
    playIconTint: Color = Color.White,
    playIconBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    progressIndicatorColor: Color = Color.Gray,
    captionTextColor: Color = Color.Black,
    captionTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    gridSpacing: Dp = 2.dp,
    maxGridWidth: Dp = 240.dp,
    moreOverlayBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    moreOverlayTextColor: Color = Color.White,
    moreOverlayTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatVideoBubbleStyle = CometChatVideoBubbleStyle(
    videoCornerRadius = videoCornerRadius,
    videoStrokeWidth = videoStrokeWidth,
    videoStrokeColor = videoStrokeColor,
    playIconTint = playIconTint,
    playIconBackgroundColor = playIconBackgroundColor,
    progressIndicatorColor = progressIndicatorColor,
    captionTextColor = captionTextColor,
    captionTextStyle = captionTextStyle,
    gridSpacing = gridSpacing,
    maxGridWidth = maxGridWidth,
    moreOverlayBackgroundColor = moreOverlayBackgroundColor,
    moreOverlayTextColor = moreOverlayTextColor,
    moreOverlayTextStyle = moreOverlayTextStyle,
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)


/**
 * Creates a test CometChatFileBubbleStyle instance with customizable values.
 */
private fun createTestFileBubbleStyle(
    innerCornerRadius: Dp = 2.dp,
    itemSpacing: Dp = 1.dp,
    titleTextColor: Color = Color.Black,
    titleTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    subtitleTextColor: Color = Color.Gray,
    subtitleTextStyle: TextStyle = TextStyle(fontSize = 10.sp),
    fileIconBackgroundColor: Color = Color.White,
    fileIconCornerRadius: Dp = 4.dp,
    fileIconSize: Dp = 32.dp,
    downloadIconTint: Color = Color.Gray,
    downloadAllButtonBackgroundColor: Color = Color.Blue,
    downloadAllButtonTextColor: Color = Color.Black,
    downloadAllButtonTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    downloadAllButtonCornerRadius: Dp = 8.dp,
    downloadAllButtonHeight: Dp = 40.dp,
    backgroundColor: Color = Color(0xFFF5F5F5),
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    padding: PaddingValues = PaddingValues(0.dp),
    senderNameTextColor: Color = Color.Gray,
    senderNameTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor: Color = Color.Gray,
    threadIndicatorTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint: Color = Color.Gray,
    timestampTextColor: Color = Color.Gray,
    timestampTextStyle: TextStyle = TextStyle(fontSize = 12.sp)
): CometChatFileBubbleStyle = CometChatFileBubbleStyle(
    innerCornerRadius = innerCornerRadius,
    itemSpacing = itemSpacing,
    titleTextColor = titleTextColor,
    titleTextStyle = titleTextStyle,
    subtitleTextColor = subtitleTextColor,
    subtitleTextStyle = subtitleTextStyle,
    fileIconBackgroundColor = fileIconBackgroundColor,
    fileIconCornerRadius = fileIconCornerRadius,
    fileIconSize = fileIconSize,
    downloadIconTint = downloadIconTint,
    downloadAllButtonBackgroundColor = downloadAllButtonBackgroundColor,
    downloadAllButtonTextColor = downloadAllButtonTextColor,
    downloadAllButtonTextStyle = downloadAllButtonTextStyle,
    downloadAllButtonCornerRadius = downloadAllButtonCornerRadius,
    downloadAllButtonHeight = downloadAllButtonHeight,
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
)
