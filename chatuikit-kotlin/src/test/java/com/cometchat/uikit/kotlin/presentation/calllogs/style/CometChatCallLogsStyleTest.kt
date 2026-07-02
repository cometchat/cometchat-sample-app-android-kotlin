package com.cometchat.uikit.kotlin.presentation.calllogs.style

import android.graphics.Color
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for CometChatCallLogsStyle and CometChatCallLogsListItemStyle data classes.
 * Verifies defaults, copy, equals/hashCode, and nested style propagation.
 *
 * **Validates: Requirements 14.1, 31.6**
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCallLogsStyleTest"
 */
class CometChatCallLogsStyleTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== CometChatCallLogsStyle: Default Values ====================

    test("default CometChatCallLogsStyle should have zero/null values for all properties") {
        val style = CometChatCallLogsStyle()
        println("    → Default style created")

        // Container styling
        style.backgroundColor shouldBe 0
        style.strokeColor shouldBe Color.TRANSPARENT
        style.strokeWidth shouldBe 0
        style.cornerRadius shouldBe 0

        // Toolbar styling
        style.titleTextColor shouldBe 0
        style.titleTextAppearance shouldBe 0
        style.backIcon shouldBe null
        style.backIconTint shouldBe 0
        style.toolbarSeparatorColor shouldBe 0
        style.toolbarSeparatorHeight shouldBe 1
        style.showToolbarSeparator shouldBe true

        // Empty state styling
        style.emptyStateTitleTextColor shouldBe 0
        style.emptyStateSubtitleTextColor shouldBe 0
        style.emptyStateTitleTextAppearance shouldBe 0
        style.emptyStateSubtitleTextAppearance shouldBe 0
        style.emptyStateIcon shouldBe null
        style.emptyStateIconTint shouldBe 0

        // Error state styling
        style.errorStateTitleTextColor shouldBe 0
        style.errorStateSubtitleTextColor shouldBe 0
        style.errorStateTitleTextAppearance shouldBe 0
        style.errorStateSubtitleTextAppearance shouldBe 0
        style.errorStateIcon shouldBe null
        style.errorStateIconTint shouldBe 0
        style.retryButtonBackgroundColor shouldBe 0
        style.retryButtonTextColor shouldBe 0

        println("    → All default values verified")
    }

    test("default CometChatCallLogsStyle should initialize nested itemStyle") {
        val style = CometChatCallLogsStyle()
        println("    → Checking nested itemStyle")

        style.itemStyle shouldNotBe null
        style.itemStyle.backgroundColor shouldBe 0
        style.itemStyle.titleTextColor shouldBe 0
        style.itemStyle.subtitleTextColor shouldBe 0
        style.itemStyle.separatorColor shouldBe 0
        println("    → Nested itemStyle initialized with defaults")
    }

    // ==================== CometChatCallLogsStyle: Copy ====================

    test("copy should create a new instance with modified properties") {
        val original = CometChatCallLogsStyle()
        val modified = original.copy(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        println("    → Original: bg=${original.backgroundColor}, Modified: bg=${modified.backgroundColor}")

        modified.backgroundColor shouldBe 0xFF000000.toInt()
        modified.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        // Unchanged properties should remain default
        modified.strokeColor shouldBe Color.TRANSPARENT
        modified.strokeWidth shouldBe 0
        modified.cornerRadius shouldBe 0
        modified.backIconTint shouldBe 0
    }

    test("copy should not modify the original instance") {
        val original = CometChatCallLogsStyle(backgroundColor = 0xFFFF0000.toInt())
        val modified = original.copy(backgroundColor = 0xFF00FF00.toInt())
        println("    → Original bg: ${original.backgroundColor}, Modified bg: ${modified.backgroundColor}")

        original.backgroundColor shouldBe 0xFFFF0000.toInt()
        modified.backgroundColor shouldBe 0xFF00FF00.toInt()
    }

    test("copy should preserve unmodified fields") {
        val original = CometChatCallLogsStyle(
            backgroundColor = 0xFFAAAAAA.toInt(),
            titleTextColor = 0xFFBBBBBB.toInt(),
            backIconTint = 0xFFCCCCCC.toInt(),
            emptyStateTitleTextColor = 0xFFDDDDDD.toInt(),
            errorStateTitleTextColor = 0xFFEEEEEE.toInt()
        )
        val modified = original.copy(backgroundColor = 0xFF111111.toInt())
        println("    → Modified only backgroundColor, checking others preserved")

        modified.backgroundColor shouldBe 0xFF111111.toInt()
        modified.titleTextColor shouldBe 0xFFBBBBBB.toInt()
        modified.backIconTint shouldBe 0xFFCCCCCC.toInt()
        modified.emptyStateTitleTextColor shouldBe 0xFFDDDDDD.toInt()
        modified.errorStateTitleTextColor shouldBe 0xFFEEEEEE.toInt()
    }

    // ==================== CometChatCallLogsStyle: Equality ====================

    test("two styles with same values should be equal") {
        val style1 = CometChatCallLogsStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        val style2 = CometChatCallLogsStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldBe style2
    }

    test("two styles with different values should not be equal") {
        val style1 = CometChatCallLogsStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatCallLogsStyle(backgroundColor = 0xFFFFFFFF.toInt())
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    test("equal styles should have same hashCode") {
        val style1 = CometChatCallLogsStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        val style2 = CometChatCallLogsStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        println("    → hashCode1=${style1.hashCode()}, hashCode2=${style2.hashCode()}")
        style1.hashCode() shouldBe style2.hashCode()
    }

    // ==================== CometChatCallLogsStyle: Nested Style Propagation ====================

    test("nested itemStyle should propagate correctly") {
        val customItemStyle = CometChatCallLogsListItemStyle(
            backgroundColor = 0xFFAA0000.toInt(),
            titleTextColor = 0xFFBB0000.toInt(),
            subtitleTextColor = 0xFFCC0000.toInt(),
            separatorColor = 0xFFDD0000.toInt()
        )
        val style = CometChatCallLogsStyle(itemStyle = customItemStyle)
        println("    → itemStyle.backgroundColor: ${style.itemStyle.backgroundColor}")

        style.itemStyle.backgroundColor shouldBe 0xFFAA0000.toInt()
        style.itemStyle.titleTextColor shouldBe 0xFFBB0000.toInt()
        style.itemStyle.subtitleTextColor shouldBe 0xFFCC0000.toInt()
        style.itemStyle.separatorColor shouldBe 0xFFDD0000.toInt()
    }

    test("copy should allow modifying nested itemStyle") {
        val original = CometChatCallLogsStyle(
            itemStyle = CometChatCallLogsListItemStyle(backgroundColor = 0xFFFF0000.toInt())
        )
        val newItemStyle = CometChatCallLogsListItemStyle(backgroundColor = 0xFF0000FF.toInt())
        val copied = original.copy(itemStyle = newItemStyle)
        println("    → Original itemStyle bg: ${original.itemStyle.backgroundColor}, Copied: ${copied.itemStyle.backgroundColor}")

        copied.itemStyle.backgroundColor shouldBe 0xFF0000FF.toInt()
        original.itemStyle.backgroundColor shouldBe 0xFFFF0000.toInt()
    }

    test("equality should consider nested itemStyle") {
        val style1 = CometChatCallLogsStyle(
            itemStyle = CometChatCallLogsListItemStyle(titleTextColor = 0xFFFF0000.toInt())
        )
        val style2 = CometChatCallLogsStyle(
            itemStyle = CometChatCallLogsListItemStyle(titleTextColor = 0xFF0000FF.toInt())
        )
        println("    → style1==style2 with different itemStyle: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    // ==================== CometChatCallLogsListItemStyle: Default Values ====================

    test("default CometChatCallLogsListItemStyle should have zero/default values") {
        val style = CometChatCallLogsListItemStyle()
        println("    → Default list item style created")

        style.backgroundColor shouldBe 0
        style.titleTextColor shouldBe 0
        style.titleTextAppearance shouldBe 0
        style.missedCallTitleColor shouldBe 0
        style.subtitleTextColor shouldBe 0
        style.subtitleTextAppearance shouldBe 0
        style.incomingCallIcon shouldBe 0
        style.incomingCallIconTint shouldBe 0
        style.outgoingCallIcon shouldBe 0
        style.outgoingCallIconTint shouldBe 0
        style.missedCallIcon shouldBe 0
        style.missedCallIconTint shouldBe 0
        style.audioCallIcon shouldBe 0
        style.audioCallIconTint shouldBe 0
        style.videoCallIcon shouldBe 0
        style.videoCallIconTint shouldBe 0
        style.separatorColor shouldBe 0
        style.separatorHeight shouldBe 1
        println("    → All list item default values verified")
    }

    test("default CometChatCallLogsListItemStyle should initialize nested component styles") {
        val style = CometChatCallLogsListItemStyle()
        println("    → Checking nested component styles")

        style.avatarStyle shouldNotBe null
        style.avatarStyle.backgroundColor shouldBe 0
        style.dateStyle shouldNotBe null
        style.dateStyle.textColor shouldBe 0
        println("    → Nested avatarStyle and dateStyle initialized")
    }

    // ==================== CometChatCallLogsListItemStyle: Copy ====================

    test("CometChatCallLogsListItemStyle copy should preserve unmodified fields") {
        val original = CometChatCallLogsListItemStyle(
            backgroundColor = 0xFFAAAAAA.toInt(),
            titleTextColor = 0xFFBBBBBB.toInt(),
            subtitleTextColor = 0xFFCCCCCC.toInt(),
            separatorColor = 0xFFDDDDDD.toInt(),
            missedCallTitleColor = 0xFFEEEEEE.toInt()
        )
        val modified = original.copy(titleTextColor = 0xFF111111.toInt())
        println("    → Modified only titleTextColor, checking others preserved")

        modified.titleTextColor shouldBe 0xFF111111.toInt()
        modified.backgroundColor shouldBe 0xFFAAAAAA.toInt()
        modified.subtitleTextColor shouldBe 0xFFCCCCCC.toInt()
        modified.separatorColor shouldBe 0xFFDDDDDD.toInt()
        modified.missedCallTitleColor shouldBe 0xFFEEEEEE.toInt()
    }

    test("CometChatCallLogsListItemStyle copy should not modify original") {
        val original = CometChatCallLogsListItemStyle(titleTextColor = 0xFFFF0000.toInt())
        val modified = original.copy(titleTextColor = 0xFF00FF00.toInt())
        println("    → Original: ${original.titleTextColor}, Modified: ${modified.titleTextColor}")

        original.titleTextColor shouldBe 0xFFFF0000.toInt()
        modified.titleTextColor shouldBe 0xFF00FF00.toInt()
    }

    // ==================== CometChatCallLogsListItemStyle: Equality ====================

    test("two list item styles with same values should be equal") {
        val style1 = CometChatCallLogsListItemStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt(),
            separatorColor = 0xFFAAAAAA.toInt()
        )
        val style2 = CometChatCallLogsListItemStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt(),
            separatorColor = 0xFFAAAAAA.toInt()
        )
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldBe style2
        style1.hashCode() shouldBe style2.hashCode()
    }

    test("two list item styles with different values should not be equal") {
        val style1 = CometChatCallLogsListItemStyle(titleTextColor = 0xFF000000.toInt())
        val style2 = CometChatCallLogsListItemStyle(titleTextColor = 0xFFFFFFFF.toInt())
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    // ==================== CometChatCallLogsListItemStyle: Nested Style Propagation ====================

    test("nested avatarStyle should propagate correctly in list item style") {
        val customAvatarStyle = CometChatAvatarStyle(
            backgroundColor = 0xFFFF0000.toInt(),
            strokeColor = 0xFF00FF00.toInt(),
            strokeWidth = 3f,
            cornerRadius = 24f
        )
        val style = CometChatCallLogsListItemStyle(avatarStyle = customAvatarStyle)
        println("    → avatarStyle.backgroundColor: ${style.avatarStyle.backgroundColor}")

        style.avatarStyle.backgroundColor shouldBe 0xFFFF0000.toInt()
        style.avatarStyle.strokeColor shouldBe 0xFF00FF00.toInt()
        style.avatarStyle.strokeWidth shouldBe 3f
        style.avatarStyle.cornerRadius shouldBe 24f
    }

    test("nested dateStyle should propagate correctly in list item style") {
        val customDateStyle = CometChatDateStyle(
            textColor = 0xFF0000FF.toInt(),
            backgroundColor = 0xFFFF00FF.toInt(),
            cornerRadius = 8
        )
        val style = CometChatCallLogsListItemStyle(dateStyle = customDateStyle)
        println("    → dateStyle.textColor: ${style.dateStyle.textColor}")

        style.dateStyle.textColor shouldBe 0xFF0000FF.toInt()
        style.dateStyle.backgroundColor shouldBe 0xFFFF00FF.toInt()
        style.dateStyle.cornerRadius shouldBe 8
    }

    test("copy should allow modifying nested avatarStyle in list item style") {
        val original = CometChatCallLogsListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = 0xFFFF0000.toInt())
        )
        val newAvatarStyle = CometChatAvatarStyle(backgroundColor = 0xFF0000FF.toInt())
        val copied = original.copy(avatarStyle = newAvatarStyle)
        println("    → Original avatar bg: ${original.avatarStyle.backgroundColor}, Copied: ${copied.avatarStyle.backgroundColor}")

        copied.avatarStyle.backgroundColor shouldBe 0xFF0000FF.toInt()
        original.avatarStyle.backgroundColor shouldBe 0xFFFF0000.toInt()
    }

    test("equality should consider nested avatarStyle in list item style") {
        val style1 = CometChatCallLogsListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = 0xFFFF0000.toInt())
        )
        val style2 = CometChatCallLogsListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = 0xFF0000FF.toInt())
        )
        println("    → style1==style2 with different avatarStyle: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    // ==================== CometChatCallLogsStyle: All Style Properties ====================

    test("container styling properties should be settable") {
        val style = CometChatCallLogsStyle(
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

    test("toolbar styling properties should be settable") {
        val style = CometChatCallLogsStyle(
            titleTextColor = 0xFFFFFFFF.toInt(),
            titleTextAppearance = 123,
            backIconTint = 0xFF111111.toInt(),
            toolbarSeparatorColor = 0xFF222222.toInt(),
            toolbarSeparatorHeight = 2,
            showToolbarSeparator = false
        )
        println("    → Toolbar: titleColor=${style.titleTextColor}, separatorColor=${style.toolbarSeparatorColor}")

        style.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        style.titleTextAppearance shouldBe 123
        style.backIconTint shouldBe 0xFF111111.toInt()
        style.toolbarSeparatorColor shouldBe 0xFF222222.toInt()
        style.toolbarSeparatorHeight shouldBe 2
        style.showToolbarSeparator shouldBe false
    }

    test("empty state styling properties should be settable") {
        val style = CometChatCallLogsStyle(
            emptyStateTitleTextColor = 0xFFAA0000.toInt(),
            emptyStateSubtitleTextColor = 0xFFBB0000.toInt(),
            emptyStateTitleTextAppearance = 100,
            emptyStateSubtitleTextAppearance = 200,
            emptyStateIconTint = 0xFFCC0000.toInt()
        )
        println("    → Empty state: title=${style.emptyStateTitleTextColor}, subtitle=${style.emptyStateSubtitleTextColor}")

        style.emptyStateTitleTextColor shouldBe 0xFFAA0000.toInt()
        style.emptyStateSubtitleTextColor shouldBe 0xFFBB0000.toInt()
        style.emptyStateTitleTextAppearance shouldBe 100
        style.emptyStateSubtitleTextAppearance shouldBe 200
        style.emptyStateIconTint shouldBe 0xFFCC0000.toInt()
    }

    test("error state styling properties should be settable") {
        val style = CometChatCallLogsStyle(
            errorStateTitleTextColor = 0xFFDD0000.toInt(),
            errorStateSubtitleTextColor = 0xFFEE0000.toInt(),
            errorStateTitleTextAppearance = 300,
            errorStateSubtitleTextAppearance = 400,
            errorStateIconTint = 0xFFFF0000.toInt(),
            retryButtonBackgroundColor = 0xFF00AA00.toInt(),
            retryButtonTextColor = 0xFF00BB00.toInt()
        )
        println("    → Error state: title=${style.errorStateTitleTextColor}, retry=${style.retryButtonBackgroundColor}")

        style.errorStateTitleTextColor shouldBe 0xFFDD0000.toInt()
        style.errorStateSubtitleTextColor shouldBe 0xFFEE0000.toInt()
        style.errorStateTitleTextAppearance shouldBe 300
        style.errorStateSubtitleTextAppearance shouldBe 400
        style.errorStateIconTint shouldBe 0xFFFF0000.toInt()
        style.retryButtonBackgroundColor shouldBe 0xFF00AA00.toInt()
        style.retryButtonTextColor shouldBe 0xFF00BB00.toInt()
    }

    // ==================== CometChatCallLogsListItemStyle: All Properties ====================

    test("call direction icon properties should be settable") {
        val style = CometChatCallLogsListItemStyle(
            incomingCallIcon = 101,
            incomingCallIconTint = 0xFF00FF00.toInt(),
            outgoingCallIcon = 102,
            outgoingCallIconTint = 0xFF0000FF.toInt(),
            missedCallIcon = 103,
            missedCallIconTint = 0xFFFF0000.toInt()
        )
        println("    → Direction icons: incoming=${style.incomingCallIcon}, outgoing=${style.outgoingCallIcon}, missed=${style.missedCallIcon}")

        style.incomingCallIcon shouldBe 101
        style.incomingCallIconTint shouldBe 0xFF00FF00.toInt()
        style.outgoingCallIcon shouldBe 102
        style.outgoingCallIconTint shouldBe 0xFF0000FF.toInt()
        style.missedCallIcon shouldBe 103
        style.missedCallIconTint shouldBe 0xFFFF0000.toInt()
    }

    test("call type icon properties should be settable") {
        val style = CometChatCallLogsListItemStyle(
            audioCallIcon = 201,
            audioCallIconTint = 0xFFAA00AA.toInt(),
            videoCallIcon = 202,
            videoCallIconTint = 0xFFBB00BB.toInt()
        )
        println("    → Type icons: audio=${style.audioCallIcon}, video=${style.videoCallIcon}")

        style.audioCallIcon shouldBe 201
        style.audioCallIconTint shouldBe 0xFFAA00AA.toInt()
        style.videoCallIcon shouldBe 202
        style.videoCallIconTint shouldBe 0xFFBB00BB.toInt()
    }
})
