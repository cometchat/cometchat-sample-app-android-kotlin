package com.cometchat.uikit.kotlin.presentation.messagelist

import com.cometchat.uikit.kotlin.presentation.messagelist.adapter.MessageAdapterTest
import com.cometchat.uikit.kotlin.presentation.messagelist.popupmenu.PopupMenuAdapterColorFallbackPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.popupmenu.PopupMenuAdapterIconVisibilityPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListAlignmentPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListCallbackPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListDateTimeFormatPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListQuickReactionsPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListReactionsRequestBuilderPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListStickyDatePropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListStreamingSpeedPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListSwipeToReplyPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageListVisibilityPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.ui.MessageOptionVisibilityPropertyTest
import com.cometchat.uikit.kotlin.presentation.messagelist.utils.MessageOptionsUtilsPropertyTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Message List component (Kotlin/XML) presentation layer.
 *
 * Runs all message-list-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Top-level message list tests
    ActionMessageInteractionBugExplorationTest::class,
    CometChatMessagePopupMenuAlignmentPropertyTest::class,
    CometChatMessagePopupMenuStylePropertyTest::class,
    DismissOnDeletePropertyTest::class,
    MenuItemClickPropertyTest::class,
    OptionClickDelegationPropertyTest::class,
    OptionConversionPropertyTest::class,
    QuickReactionsDefaultsPropertyTest::class,
    QuickReactionsVisibilityPropertyTest::class,
    ReactionClickPropertyTest::class,
    RegularMessageInteractionPreservationTest::class,
    // Adapter tests
    MessageAdapterTest::class,
    // BubbleViewProvider tests
    BubbleViewProviderPropertyTest::class,
    // Popup menu tests
    PopupMenuAdapterColorFallbackPropertyTest::class,
    PopupMenuAdapterIconVisibilityPropertyTest::class,
    // UI tests
    CometChatMessageListAlignmentPropertyTest::class,
    CometChatMessageListCallbackPropertyTest::class,
    CometChatMessageListDateTimeFormatPropertyTest::class,
    CometChatMessageListPropertyTest::class,
    CometChatMessageListQuickReactionsPropertyTest::class,
    CometChatMessageListReactionsRequestBuilderPropertyTest::class,
    CometChatMessageListStickyDatePropertyTest::class,
    CometChatMessageListStreamingSpeedPropertyTest::class,
    CometChatMessageListSwipeToReplyPropertyTest::class,
    CometChatMessageListVisibilityPropertyTest::class,
    MessageOptionVisibilityPropertyTest::class,
    // Utils tests
    MessageOptionsUtilsPropertyTest::class
)
class CometChatMessageListTestSuite
