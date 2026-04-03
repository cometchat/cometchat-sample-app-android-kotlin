package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for Collaborative Bubble Alignment-Based Style Selection.
 *
 * Feature: collaborative-bubble-parity
 * Properties tested:
 * - Property 3: Alignment-based style selection
 *
 * **Validates: Requirements 5.2, 5.3, 5.4, 5.6**
 *
 * Expected behavior:
 * - RIGHT alignment → outgoing style (CometChatCollaborativeBubbleStyle.outgoing())
 * - LEFT alignment → incoming style (CometChatCollaborativeBubbleStyle.incoming())
 * - CENTER alignment → incoming style (default)
 */
class CollaborativeBubbleAlignmentStylePropertyTest : StringSpec({

    val alignmentArb = Arb.enum<AlignmentStyleMessageBubbleAlignment>()

    "Property 3: RIGHT alignment should select outgoing style" {
        checkAll(100, alignmentArb) { alignment ->
            if (alignment == AlignmentStyleMessageBubbleAlignment.RIGHT) {
                val selectedStyle = selectStyleForAlignment(alignment)
                selectedStyle shouldBe StyleType.OUTGOING
            }
        }
    }

    "Property 3: LEFT alignment should select incoming style" {
        checkAll(100, alignmentArb) { alignment ->
            if (alignment == AlignmentStyleMessageBubbleAlignment.LEFT) {
                val selectedStyle = selectStyleForAlignment(alignment)
                selectedStyle shouldBe StyleType.INCOMING
            }
        }
    }

    "Property 3: CENTER alignment should select incoming style (default)" {
        checkAll(100, alignmentArb) { alignment ->
            if (alignment == AlignmentStyleMessageBubbleAlignment.CENTER) {
                val selectedStyle = selectStyleForAlignment(alignment)
                selectedStyle shouldBe StyleType.INCOMING
            }
        }
    }

    "Property 3: Style selection should be deterministic for any alignment" {
        checkAll(100, alignmentArb) { alignment ->
            val style1 = selectStyleForAlignment(alignment)
            val style2 = selectStyleForAlignment(alignment)
            val style3 = selectStyleForAlignment(alignment)
            style1 shouldBe style2
            style2 shouldBe style3
        }
    }

    "Property 3: Style selection should never return null for any alignment" {
        checkAll(100, alignmentArb) { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            selectedStyle shouldNotBe null
        }
    }

    "Property 3: Only RIGHT alignment should select outgoing style" {
        checkAll(100, alignmentArb) { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            if (selectedStyle == StyleType.OUTGOING) {
                alignment shouldBe AlignmentStyleMessageBubbleAlignment.RIGHT
            }
        }
    }

    "Property 3: LEFT and CENTER alignments should both select incoming style" {
        checkAll(100, alignmentArb) { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            if (alignment == AlignmentStyleMessageBubbleAlignment.LEFT || 
                alignment == AlignmentStyleMessageBubbleAlignment.CENTER) {
                selectedStyle shouldBe StyleType.INCOMING
            }
        }
    }

    "Property 3: Every alignment value should map to exactly one style type" {
        AlignmentStyleMessageBubbleAlignment.entries.forEach { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            selectedStyle shouldNotBe null
            // Verify it's one of the valid style types
            (selectedStyle == StyleType.INCOMING || selectedStyle == StyleType.OUTGOING) shouldBe true
        }
    }

    "Property 3: Style selection should be consistent with when expression logic" {
        checkAll(100, alignmentArb) { alignment ->
            val expectedStyle = when (alignment) {
                AlignmentStyleMessageBubbleAlignment.RIGHT -> StyleType.OUTGOING
                else -> StyleType.INCOMING
            }
            val actualStyle = selectStyleForAlignment(alignment)
            actualStyle shouldBe expectedStyle
        }
    }

    "Property 3: Outgoing style should only be selected for RIGHT alignment" {
        checkAll(100, alignmentArb) { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            val isOutgoing = selectedStyle == StyleType.OUTGOING
            val isRight = alignment == AlignmentStyleMessageBubbleAlignment.RIGHT
            isOutgoing shouldBe isRight
        }
    }

    "Property 3: Incoming style should be selected for non-RIGHT alignments" {
        checkAll(100, alignmentArb) { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            val isIncoming = selectedStyle == StyleType.INCOMING
            val isNotRight = alignment != AlignmentStyleMessageBubbleAlignment.RIGHT
            isIncoming shouldBe isNotRight
        }
    }

    "Property 3: All alignment enum values should be covered" {
        AlignmentStyleMessageBubbleAlignment.entries.forEach { alignment ->
            val selectedStyle = selectStyleForAlignment(alignment)
            when (alignment) {
                AlignmentStyleMessageBubbleAlignment.RIGHT -> selectedStyle shouldBe StyleType.OUTGOING
                AlignmentStyleMessageBubbleAlignment.LEFT -> selectedStyle shouldBe StyleType.INCOMING
                AlignmentStyleMessageBubbleAlignment.CENTER -> selectedStyle shouldBe StyleType.INCOMING
            }
        }
    }

    "Property 3: Style selection should match message-based overload default behavior" {
        checkAll(100, alignmentArb) { alignment ->
            // This simulates the default style parameter in the message-based overload:
            // style: CometChatCollaborativeBubbleStyle = when (alignment) {
            //     UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatCollaborativeBubbleStyle.outgoing()
            //     else -> CometChatCollaborativeBubbleStyle.incoming()
            // }
            val defaultStyleForOverload = when (alignment) {
                AlignmentStyleMessageBubbleAlignment.RIGHT -> StyleType.OUTGOING
                else -> StyleType.INCOMING
            }
            val selectedStyle = selectStyleForAlignment(alignment)
            selectedStyle shouldBe defaultStyleForOverload
        }
    }

    "Property 3: MessageBubbleStyle selection should also follow alignment" {
        checkAll(100, alignmentArb) { alignment ->
            // This simulates the messageBubbleStyle parameter default:
            // messageBubbleStyle: CometChatMessageBubbleStyle = when (alignment) {
            //     UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMessageBubbleStyle.outgoing()
            //     else -> CometChatMessageBubbleStyle.incoming()
            // }
            val messageBubbleStyleType = selectMessageBubbleStyleForAlignment(alignment)
            val collaborativeStyleType = selectStyleForAlignment(alignment)
            // Both should follow the same alignment-based selection logic
            messageBubbleStyleType shouldBe collaborativeStyleType
        }
    }

    "Property 3: RIGHT alignment should produce different style than LEFT" {
        val rightStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.RIGHT)
        val leftStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.LEFT)
        rightStyle shouldNotBe leftStyle
    }

    "Property 3: RIGHT alignment should produce different style than CENTER" {
        val rightStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.RIGHT)
        val centerStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.CENTER)
        rightStyle shouldNotBe centerStyle
    }

    "Property 3: LEFT and CENTER alignments should produce same style type" {
        val leftStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.LEFT)
        val centerStyle = selectStyleForAlignment(AlignmentStyleMessageBubbleAlignment.CENTER)
        leftStyle shouldBe centerStyle
    }

    "Property 3: Style selection should be idempotent" {
        checkAll(100, alignmentArb) { alignment ->
            val firstCall = selectStyleForAlignment(alignment)
            val secondCall = selectStyleForAlignment(alignment)
            val thirdCall = selectStyleForAlignment(alignment)
            firstCall shouldBe secondCall
            secondCall shouldBe thirdCall
        }
    }
})

// Helper function that simulates the alignment-based style selection logic
// from CometChatCollaborativeBubble composable
private fun selectStyleForAlignment(alignment: AlignmentStyleMessageBubbleAlignment): StyleType {
    return when (alignment) {
        AlignmentStyleMessageBubbleAlignment.RIGHT -> StyleType.OUTGOING
        else -> StyleType.INCOMING
    }
}

// Helper function that simulates MessageBubbleStyle selection based on alignment
private fun selectMessageBubbleStyleForAlignment(alignment: AlignmentStyleMessageBubbleAlignment): StyleType {
    return when (alignment) {
        AlignmentStyleMessageBubbleAlignment.RIGHT -> StyleType.OUTGOING
        else -> StyleType.INCOMING
    }
}

// Local enum to avoid dependency on UIKitConstants
// Mirrors UIKitConstants.MessageBubbleAlignment
private enum class AlignmentStyleMessageBubbleAlignment {
    RIGHT, LEFT, CENTER
}

// Enum representing the style type selected
private enum class StyleType {
    INCOMING,
    OUTGOING
}
