package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.mergeWithBase
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Regression tests for receipt style resolution in DefaultStatusInfoView.
 *
 * **Regression: ENG-38382 — the message bubble ignored `messageReceiptStyle`, so the receipt
 * error icon/tint/size could not be customized on the message screen.**
 *
 * Two defects combined to drop the integrator's style:
 * 1. `DefaultStatusInfoView` called `CometChatReceipts(...)` with no `style` argument, so it
 *    always fell back to `CometChatReceiptsStyle.default()`.
 * 2. `mergeWithBase` cannot carry `messageReceiptStyle` onto a per-bubble-type style — those
 *    data classes do not declare the property, so it is structurally always `null` on them.
 *    Whenever a content style was merged in, the base style's value was lost.
 *
 * The fix threads the base style's receipt style to the render site as an explicit
 * `receiptStyle` parameter. These tests pin both the structural limitation that makes the
 * parameter necessary and the resolution chain that consumes it.
 */
class ReceiptStyleResolutionPropertyTest : StringSpec({

    val dpArb: Arb<Dp> = Arb.float(12f, 32f).map { it.dp }

    /**
     * Builds a receipt style without touching @Composable defaults.
     * Painters are irrelevant here — resolution is by reference identity.
     */
    fun receiptStyle(size: Dp, errorTint: Color): CometChatReceiptsStyle = CometChatReceiptsStyle(
        waitIcon = null,
        sentIcon = null,
        deliveredIcon = null,
        readIcon = null,
        errorIcon = null,
        waitIconTint = Color.Gray,
        sentIconTint = Color.Gray,
        deliveredIconTint = Color.Gray,
        readIconTint = Color.Blue,
        errorIconTint = errorTint,
        size = size
    )

    fun baseStyle(receipts: CometChatReceiptsStyle?): CometChatMessageBubbleStyle =
        CometChatMessageBubbleStyle(
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 1.dp,
            strokeColor = Color.DarkGray,
            padding = PaddingValues(8.dp),
            senderNameTextColor = Color.Black,
            senderNameTextStyle = TextStyle(fontSize = 11.sp),
            threadIndicatorTextColor = Color.Blue,
            threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
            threadIndicatorIconTint = Color.Blue,
            timestampTextColor = Color.Gray,
            timestampTextStyle = TextStyle(fontSize = 9.sp),
            messageReceiptStyle = receipts
        )

    fun textContentStyle(): CometChatTextBubbleStyle = CometChatTextBubbleStyle(
        textColor = Color.Black,
        textStyle = TextStyle(fontSize = 14.sp),
        linkColor = Color.Blue,
        translatedTextColor = Color.Gray,
        translatedTextStyle = TextStyle(fontSize = 12.sp),
        separatorColor = Color.LightGray,
        linkPreviewBackgroundColor = Color(0xFFE8E8E8),
        linkPreviewTitleColor = Color.Black,
        linkPreviewTitleStyle = TextStyle(fontSize = 14.sp),
        linkPreviewDescriptionColor = Color.Gray,
        linkPreviewDescriptionStyle = TextStyle(fontSize = 12.sp),
        linkPreviewLinkColor = Color.Blue,
        linkPreviewLinkStyle = TextStyle(fontSize = 12.sp),
        linkPreviewCornerRadius = 8.dp,
        linkPreviewStrokeWidth = 0.dp,
        linkPreviewStrokeColor = Color.Transparent,
        backgroundColor = Color.White,
        cornerRadius = 12.dp,
        strokeWidth = 1.dp,
        strokeColor = Color.DarkGray,
        padding = PaddingValues(8.dp),
        senderNameTextColor = Color.Black,
        senderNameTextStyle = TextStyle(fontSize = 11.sp),
        threadIndicatorTextColor = Color.Blue,
        threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
        threadIndicatorIconTint = Color.Blue,
        timestampTextColor = Color.Gray,
        timestampTextStyle = TextStyle(fontSize = 9.sp)
    )

    /**
     * Models the resolution chain from DefaultStatusInfoView:
     *   style = receiptStyle ?: style.messageReceiptStyle ?: CometChatReceiptsStyle.default()
     *
     * `CometChatReceiptsStyle.default()` is @Composable, so the composable fallback is modelled
     * as `null` — meaning "fall through to the library default".
     */
    fun resolveReceiptStyle(
        receiptStyle: CometChatReceiptsStyle?,
        bubbleStyle: CometChatMessageBubbleStyle
    ): CometChatReceiptsStyle? = receiptStyle ?: bubbleStyle.messageReceiptStyle

    // ========================================================================
    // The structural limitation that makes the receiptStyle parameter necessary
    // ========================================================================

    /**
     * Per-bubble-type styles do not declare `messageReceiptStyle`, so a merged style always
     * reports `null` no matter what the base style carried. This is the reason
     * DefaultStatusInfoView cannot read the receipt style off its `style` parameter alone.
     */
    "mergeWithBase drops messageReceiptStyle from the base style" {
        checkAll(50, dpArb) { size ->
            val base = baseStyle(receiptStyle(size, Color.Magenta))

            val merged = mergeWithBase(textContentStyle(), base)

            base.messageReceiptStyle shouldBe receiptStyle(size, Color.Magenta)
            merged.messageReceiptStyle.shouldBeNull()
        }
    }

    // ========================================================================
    // Resolution chain
    // ========================================================================

    /**
     * The bug: with a content style merged in, reading the receipt style off the effective
     * style yields the library default and the integrator's error icon never appears.
     * Passing the base style's value explicitly restores it.
     */
    "explicit receiptStyle survives a content-style merge" {
        checkAll(50, dpArb) { size ->
            val custom = receiptStyle(size, Color.Magenta)
            val base = baseStyle(custom)
            val effective = mergeWithBase(textContentStyle(), base)

            // Pre-fix behaviour: nothing to resolve from, so the default is used.
            resolveReceiptStyle(receiptStyle = null, bubbleStyle = effective).shouldBeNull()

            // Post-fix behaviour: the caller supplies the base style's receipt style.
            resolveReceiptStyle(
                receiptStyle = base.messageReceiptStyle,
                bubbleStyle = effective
            ) shouldBe custom
        }
    }

    /**
     * With no content style, `effectiveStyle === baseStyle`, so the receipt style is readable
     * from the bubble style itself and must still be honoured.
     */
    "bubble style's messageReceiptStyle is used when no explicit receiptStyle is given" {
        checkAll(50, dpArb) { size ->
            val custom = receiptStyle(size, Color.Magenta)

            resolveReceiptStyle(
                receiptStyle = null,
                bubbleStyle = baseStyle(custom)
            ) shouldBe custom
        }
    }

    /**
     * An explicit receiptStyle wins over the bubble style's own value.
     */
    "explicit receiptStyle takes precedence over the bubble style's value" {
        checkAll(50, dpArb) { size ->
            val fromBase = receiptStyle(size, Color.Magenta)
            val fromBubble = receiptStyle(size, Color.Green)

            resolveReceiptStyle(
                receiptStyle = fromBase,
                bubbleStyle = baseStyle(fromBubble)
            ) shouldBe fromBase
        }
    }

    /**
     * When nobody supplies a receipt style, resolution falls through to the library default.
     */
    "falls through to the library default when no receipt style is configured" {
        resolveReceiptStyle(
            receiptStyle = null,
            bubbleStyle = baseStyle(receipts = null)
        ).shouldBeNull()
    }

    /**
     * The receipt icon size now comes from the style rather than a hardcoded 16.dp modifier.
     * The library default remains 16.dp, so unstyled bubbles are visually unchanged.
     */
    "a custom size is carried through resolution" {
        checkAll(50, dpArb) { size ->
            val resolved = resolveReceiptStyle(
                receiptStyle = null,
                bubbleStyle = baseStyle(receiptStyle(size, Color.Magenta))
            )

            resolved?.size shouldBe size
        }
    }
})
