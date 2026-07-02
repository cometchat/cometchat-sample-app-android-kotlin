package com.cometchat.uikit.kotlin.presentation.messagelist.style

import com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter.CometChatAIConversationStarterStyle
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryStyle
import com.cometchat.uikit.kotlin.presentation.shared.aismartreplies.CometChatAISmartRepliesStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.kotlin.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.kotlin.presentation.reactionlist.style.CometChatReactionListStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for CometChatMessageListStyle data class.
 *
 * Verifies:
 * - Default values (all zero/null for unset properties)
 * - copy() creates independent instances with overridden values
 * - equals()/hashCode() structural equality
 * - Nested style propagation (12 nested component styles)
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageListStyleTest"
 */
class CometChatMessageListStyleTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== Default Values ====================

    test("default CometChatMessageListStyle should have zero/null values for container properties") {
        val style = CometChatMessageListStyle()
        println("    → Default style created")

        // Container styling
        style.backgroundColor shouldBe 0
        style.strokeColor shouldBe 0
        style.strokeWidth shouldBe 0
        style.cornerRadius shouldBe 0
        style.backgroundDrawable shouldBe null
        println("    → Container defaults verified: all zero/null")
    }

    test("default CometChatMessageListStyle should have zero values for error state properties") {
        val style = CometChatMessageListStyle()

        style.errorStateTitleTextColor shouldBe 0
        style.errorStateTitleTextAppearance shouldBe 0
        style.errorStateSubtitleTextColor shouldBe 0
        style.errorStateSubtitleTextAppearance shouldBe 0
        println("    → Error state defaults verified: all zero")
    }

    test("default CometChatMessageListStyle should have zero values for empty chat greeting properties") {
        val style = CometChatMessageListStyle()

        style.emptyChatGreetingTitleTextColor shouldBe 0
        style.emptyChatGreetingTitleTextAppearance shouldBe 0
        style.emptyChatGreetingSubtitleTextColor shouldBe 0
        style.emptyChatGreetingSubtitleTextAppearance shouldBe 0
        println("    → Empty chat greeting defaults verified: all zero")
    }

    test("default CometChatMessageListStyle should have zero/null values for AI suggested message properties") {
        val style = CometChatMessageListStyle()

        style.aiAssistantSuggestedMessageTextColor shouldBe 0
        style.aiAssistantSuggestedMessageTextAppearance shouldBe 0
        style.aiAssistantSuggestedMessageCornerRadius shouldBe 0
        style.aiAssistantSuggestedMessageStrokeWidth shouldBe 0
        style.aiAssistantSuggestedMessageStrokeColor shouldBe 0
        style.aiAssistantSuggestedMessageBackgroundColor shouldBe 0
        style.aiAssistantSuggestedMessageEndIcon shouldBe null
        style.aiAssistantSuggestedMessageEndIconTint shouldBe 0
        println("    → AI suggested message defaults verified: all zero/null")
    }

    test("default CometChatMessageListStyle should have null nested component styles") {
        val style = CometChatMessageListStyle()

        style.incomingMessageBubbleStyle.shouldBeNull()
        style.outgoingMessageBubbleStyle.shouldBeNull()
        style.actionBubbleStyle.shouldBeNull()
        style.callActionBubbleStyle.shouldBeNull()
        style.dateSeparatorStyle.shouldBeNull()
        style.deleteDialogStyle.shouldBeNull()
        style.messageInformationStyle.shouldBeNull()
        style.messageOptionSheetStyle.shouldBeNull()
        style.reactionListStyle.shouldBeNull()
        style.aiSmartRepliesStyle.shouldBeNull()
        style.aiConversationStarterStyle.shouldBeNull()
        style.aiConversationSummaryStyle.shouldBeNull()
        println("    → All 12 nested component styles are null by default")
    }

    // ==================== Copy ====================

    test("copy should create a new instance with modified container properties") {
        val original = CometChatMessageListStyle()
        val modified = original.copy(
            backgroundColor = 0xFF000000.toInt(),
            strokeColor = 0xFF333333.toInt(),
            strokeWidth = 2,
            cornerRadius = 16
        )
        println("    → Original bg=${original.backgroundColor}, Modified bg=${modified.backgroundColor}")

        modified.backgroundColor shouldBe 0xFF000000.toInt()
        modified.strokeColor shouldBe 0xFF333333.toInt()
        modified.strokeWidth shouldBe 2
        modified.cornerRadius shouldBe 16
        // Unchanged properties remain default
        modified.errorStateTitleTextColor shouldBe 0
        modified.emptyChatGreetingTitleTextColor shouldBe 0
    }

    test("copy should not modify the original instance") {
        val original = CometChatMessageListStyle(backgroundColor = 0xFFFF0000.toInt())
        val modified = original.copy(backgroundColor = 0xFF00FF00.toInt())
        println("    → Original bg: ${original.backgroundColor}, Modified bg: ${modified.backgroundColor}")

        original.backgroundColor shouldBe 0xFFFF0000.toInt()
        modified.backgroundColor shouldBe 0xFF00FF00.toInt()
    }

    test("copy should preserve unmodified fields") {
        val original = CometChatMessageListStyle(
            backgroundColor = 0xFFAAAAAA.toInt(),
            strokeColor = 0xFFBBBBBB.toInt(),
            errorStateTitleTextColor = 0xFFCCCCCC.toInt(),
            emptyChatGreetingTitleTextColor = 0xFFDDDDDD.toInt(),
            aiAssistantSuggestedMessageTextColor = 0xFFEEEEEE.toInt()
        )
        val modified = original.copy(backgroundColor = 0xFF111111.toInt())
        println("    → Modified only backgroundColor, checking others preserved")

        modified.backgroundColor shouldBe 0xFF111111.toInt()
        modified.strokeColor shouldBe 0xFFBBBBBB.toInt()
        modified.errorStateTitleTextColor shouldBe 0xFFCCCCCC.toInt()
        modified.emptyChatGreetingTitleTextColor shouldBe 0xFFDDDDDD.toInt()
        modified.aiAssistantSuggestedMessageTextColor shouldBe 0xFFEEEEEE.toInt()
    }

    // ==================== Equality ====================

    test("two styles with same values should be equal") {
        val style1 = CometChatMessageListStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeColor = 0xFF333333.toInt(),
            errorStateTitleTextColor = 0xFFFF0000.toInt()
        )
        val style2 = CometChatMessageListStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeColor = 0xFF333333.toInt(),
            errorStateTitleTextColor = 0xFFFF0000.toInt()
        )
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldBe style2
    }

    test("two styles with different values should not be equal") {
        val style1 = CometChatMessageListStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatMessageListStyle(backgroundColor = 0xFFFFFFFF.toInt())
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    test("equal styles should have same hashCode") {
        val style1 = CometChatMessageListStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2,
            cornerRadius = 8
        )
        val style2 = CometChatMessageListStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2,
            cornerRadius = 8
        )
        println("    → hashCode1=${style1.hashCode()}, hashCode2=${style2.hashCode()}")
        style1.hashCode() shouldBe style2.hashCode()
    }

    test("different styles should have different hashCode (high probability)") {
        val style1 = CometChatMessageListStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatMessageListStyle(backgroundColor = 0xFFFFFFFF.toInt())
        println("    → hashCode1=${style1.hashCode()}, hashCode2=${style2.hashCode()}")
        style1.hashCode() shouldNotBe style2.hashCode()
    }

    // ==================== Nested Style Propagation ====================

    test("nested incomingMessageBubbleStyle should propagate correctly") {
        val bubbleStyle = CometChatMessageBubbleStyle(
            backgroundColor = 0xFFEEEEEE.toInt(),
            cornerRadius = 12f
        )
        val style = CometChatMessageListStyle(incomingMessageBubbleStyle = bubbleStyle)
        println("    → incomingBubble.backgroundColor: ${style.incomingMessageBubbleStyle?.backgroundColor}")

        style.incomingMessageBubbleStyle.shouldNotBeNull()
        style.incomingMessageBubbleStyle!!.backgroundColor shouldBe 0xFFEEEEEE.toInt()
        style.incomingMessageBubbleStyle!!.cornerRadius shouldBe 12f
    }

    test("nested outgoingMessageBubbleStyle should propagate correctly") {
        val bubbleStyle = CometChatMessageBubbleStyle(
            backgroundColor = 0xFF6851D6.toInt(),
            cornerRadius = 16f
        )
        val style = CometChatMessageListStyle(outgoingMessageBubbleStyle = bubbleStyle)
        println("    → outgoingBubble.backgroundColor: ${style.outgoingMessageBubbleStyle?.backgroundColor}")

        style.outgoingMessageBubbleStyle.shouldNotBeNull()
        style.outgoingMessageBubbleStyle!!.backgroundColor shouldBe 0xFF6851D6.toInt()
        style.outgoingMessageBubbleStyle!!.cornerRadius shouldBe 16f
    }

    test("nested actionBubbleStyle should propagate correctly") {
        val actionStyle = CometChatActionBubbleStyle(
            backgroundColor = 0xFFF0F0F0.toInt(),
            textColor = 0xFF666666.toInt()
        )
        val style = CometChatMessageListStyle(actionBubbleStyle = actionStyle)
        println("    → actionBubble.backgroundColor: ${style.actionBubbleStyle?.backgroundColor}")

        style.actionBubbleStyle.shouldNotBeNull()
        style.actionBubbleStyle!!.backgroundColor shouldBe 0xFFF0F0F0.toInt()
        style.actionBubbleStyle!!.textColor shouldBe 0xFF666666.toInt()
    }

    test("nested dateSeparatorStyle should propagate correctly") {
        val dateStyle = CometChatDateStyle(
            textColor = 0xFF999999.toInt(),
            backgroundColor = 0xFFE0E0E0.toInt(),
            cornerRadius = 8
        )
        val style = CometChatMessageListStyle(dateSeparatorStyle = dateStyle)
        println("    → dateSeparator.textColor: ${style.dateSeparatorStyle?.textColor}")

        style.dateSeparatorStyle.shouldNotBeNull()
        style.dateSeparatorStyle!!.textColor shouldBe 0xFF999999.toInt()
        style.dateSeparatorStyle!!.backgroundColor shouldBe 0xFFE0E0E0.toInt()
        style.dateSeparatorStyle!!.cornerRadius shouldBe 8
    }

    test("nested messageOptionSheetStyle should propagate correctly") {
        val popupStyle = CometChatPopupMenuStyle(
            backgroundColor = 0xFFFFFFFF.toInt(),
            cornerRadius = 12
        )
        val style = CometChatMessageListStyle(messageOptionSheetStyle = popupStyle)
        println("    → messageOptionSheet.backgroundColor: ${style.messageOptionSheetStyle?.backgroundColor}")

        style.messageOptionSheetStyle.shouldNotBeNull()
        style.messageOptionSheetStyle!!.backgroundColor shouldBe 0xFFFFFFFF.toInt()
        style.messageOptionSheetStyle!!.cornerRadius shouldBe 12
    }

    test("copy should allow modifying nested styles") {
        val original = CometChatMessageListStyle(
            incomingMessageBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFFFF0000.toInt())
        )
        val newBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFF0000FF.toInt())
        val copied = original.copy(incomingMessageBubbleStyle = newBubbleStyle)
        println("    → Original bubble bg: ${original.incomingMessageBubbleStyle?.backgroundColor}, Copied: ${copied.incomingMessageBubbleStyle?.backgroundColor}")

        copied.incomingMessageBubbleStyle!!.backgroundColor shouldBe 0xFF0000FF.toInt()
        original.incomingMessageBubbleStyle!!.backgroundColor shouldBe 0xFFFF0000.toInt()
    }

    test("equality should consider nested styles") {
        val style1 = CometChatMessageListStyle(
            incomingMessageBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFFFF0000.toInt())
        )
        val style2 = CometChatMessageListStyle(
            incomingMessageBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFF0000FF.toInt())
        )
        println("    → style1==style2 with different nested style: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    test("equality should match when nested styles are identical") {
        val bubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFFAA0000.toInt())
        val style1 = CometChatMessageListStyle(incomingMessageBubbleStyle = bubbleStyle)
        val style2 = CometChatMessageListStyle(incomingMessageBubbleStyle = bubbleStyle)
        println("    → style1==style2 with same nested style: ${style1 == style2}")
        style1 shouldBe style2
    }

    // ==================== All Container Properties Settable ====================

    test("container styling properties should be settable") {
        val style = CometChatMessageListStyle(
            backgroundColor = 0xFF1A1A2E.toInt(),
            strokeColor = 0xFF333333.toInt(),
            strokeWidth = 2,
            cornerRadius = 16
        )
        println("    → Container: bg=${style.backgroundColor}, stroke=${style.strokeColor}")

        style.backgroundColor shouldBe 0xFF1A1A2E.toInt()
        style.strokeColor shouldBe 0xFF333333.toInt()
        style.strokeWidth shouldBe 2
        style.cornerRadius shouldBe 16
    }

    test("error state styling properties should be settable") {
        val style = CometChatMessageListStyle(
            errorStateTitleTextColor = 0xFFDD0000.toInt(),
            errorStateTitleTextAppearance = 300,
            errorStateSubtitleTextColor = 0xFFEE0000.toInt(),
            errorStateSubtitleTextAppearance = 400
        )
        println("    → Error state: title=${style.errorStateTitleTextColor}, subtitle=${style.errorStateSubtitleTextColor}")

        style.errorStateTitleTextColor shouldBe 0xFFDD0000.toInt()
        style.errorStateTitleTextAppearance shouldBe 300
        style.errorStateSubtitleTextColor shouldBe 0xFFEE0000.toInt()
        style.errorStateSubtitleTextAppearance shouldBe 400
    }

    test("empty chat greeting styling properties should be settable") {
        val style = CometChatMessageListStyle(
            emptyChatGreetingTitleTextColor = 0xFFAA0000.toInt(),
            emptyChatGreetingTitleTextAppearance = 100,
            emptyChatGreetingSubtitleTextColor = 0xFFBB0000.toInt(),
            emptyChatGreetingSubtitleTextAppearance = 200
        )
        println("    → Empty greeting: title=${style.emptyChatGreetingTitleTextColor}, subtitle=${style.emptyChatGreetingSubtitleTextColor}")

        style.emptyChatGreetingTitleTextColor shouldBe 0xFFAA0000.toInt()
        style.emptyChatGreetingTitleTextAppearance shouldBe 100
        style.emptyChatGreetingSubtitleTextColor shouldBe 0xFFBB0000.toInt()
        style.emptyChatGreetingSubtitleTextAppearance shouldBe 200
    }

    test("AI assistant suggested message styling properties should be settable") {
        val style = CometChatMessageListStyle(
            aiAssistantSuggestedMessageTextColor = 0xFF111111.toInt(),
            aiAssistantSuggestedMessageTextAppearance = 500,
            aiAssistantSuggestedMessageCornerRadius = 12,
            aiAssistantSuggestedMessageStrokeWidth = 1,
            aiAssistantSuggestedMessageStrokeColor = 0xFF222222.toInt(),
            aiAssistantSuggestedMessageBackgroundColor = 0xFF333333.toInt(),
            aiAssistantSuggestedMessageEndIconTint = 0xFF444444.toInt()
        )
        println("    → AI suggested: textColor=${style.aiAssistantSuggestedMessageTextColor}, bg=${style.aiAssistantSuggestedMessageBackgroundColor}")

        style.aiAssistantSuggestedMessageTextColor shouldBe 0xFF111111.toInt()
        style.aiAssistantSuggestedMessageTextAppearance shouldBe 500
        style.aiAssistantSuggestedMessageCornerRadius shouldBe 12
        style.aiAssistantSuggestedMessageStrokeWidth shouldBe 1
        style.aiAssistantSuggestedMessageStrokeColor shouldBe 0xFF222222.toInt()
        style.aiAssistantSuggestedMessageBackgroundColor shouldBe 0xFF333333.toInt()
        style.aiAssistantSuggestedMessageEndIconTint shouldBe 0xFF444444.toInt()
    }

    // ==================== All 12 Nested Styles Can Be Set ====================

    test("all 12 nested component styles can be set simultaneously") {
        val style = CometChatMessageListStyle(
            incomingMessageBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFF110000.toInt()),
            outgoingMessageBubbleStyle = CometChatMessageBubbleStyle(backgroundColor = 0xFF220000.toInt()),
            actionBubbleStyle = CometChatActionBubbleStyle(backgroundColor = 0xFF330000.toInt()),
            callActionBubbleStyle = CometChatCallActionBubbleStyle(backgroundColor = 0xFF440000.toInt()),
            dateSeparatorStyle = CometChatDateStyle(textColor = 0xFF550000.toInt()),
            deleteDialogStyle = CometChatConfirmDialogStyle(backgroundColor = 0xFF660000.toInt()),
            messageInformationStyle = CometChatMessageInformationStyle(backgroundColor = 0xFF770000.toInt()),
            messageOptionSheetStyle = CometChatPopupMenuStyle(backgroundColor = 0xFF880000.toInt()),
            reactionListStyle = CometChatReactionListStyle(backgroundColor = 0xFF990000.toInt()),
            aiSmartRepliesStyle = CometChatAISmartRepliesStyle(backgroundColor = 0xFFAA0000.toInt()),
            aiConversationStarterStyle = CometChatAIConversationStarterStyle(backgroundColor = 0xFFBB0000.toInt()),
            aiConversationSummaryStyle = CometChatAIConversationSummaryStyle(backgroundColor = 0xFFCC0000.toInt())
        )
        println("    → All 12 nested styles set")

        style.incomingMessageBubbleStyle.shouldNotBeNull()
        style.outgoingMessageBubbleStyle.shouldNotBeNull()
        style.actionBubbleStyle.shouldNotBeNull()
        style.callActionBubbleStyle.shouldNotBeNull()
        style.dateSeparatorStyle.shouldNotBeNull()
        style.deleteDialogStyle.shouldNotBeNull()
        style.messageInformationStyle.shouldNotBeNull()
        style.messageOptionSheetStyle.shouldNotBeNull()
        style.reactionListStyle.shouldNotBeNull()
        style.aiSmartRepliesStyle.shouldNotBeNull()
        style.aiConversationStarterStyle.shouldNotBeNull()
        style.aiConversationSummaryStyle.shouldNotBeNull()

        style.incomingMessageBubbleStyle!!.backgroundColor shouldBe 0xFF110000.toInt()
        style.outgoingMessageBubbleStyle!!.backgroundColor shouldBe 0xFF220000.toInt()
        style.actionBubbleStyle!!.backgroundColor shouldBe 0xFF330000.toInt()
        style.callActionBubbleStyle!!.backgroundColor shouldBe 0xFF440000.toInt()
        style.dateSeparatorStyle!!.textColor shouldBe 0xFF550000.toInt()
        style.deleteDialogStyle!!.backgroundColor shouldBe 0xFF660000.toInt()
        style.messageInformationStyle!!.backgroundColor shouldBe 0xFF770000.toInt()
        style.messageOptionSheetStyle!!.backgroundColor shouldBe 0xFF880000.toInt()
        style.reactionListStyle!!.backgroundColor shouldBe 0xFF990000.toInt()
        style.aiSmartRepliesStyle!!.backgroundColor shouldBe 0xFFAA0000.toInt()
        style.aiConversationStarterStyle!!.backgroundColor shouldBe 0xFFBB0000.toInt()
        style.aiConversationSummaryStyle!!.backgroundColor shouldBe 0xFFCC0000.toInt()
        println("    ✅ All 12 nested styles verified with correct values")
    }
})
