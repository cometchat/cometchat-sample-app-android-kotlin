package com.cometchat.uikit.kotlin.presentation.conversations

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatConversationsListItemsBinding
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.utils.captureWithPopups
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatConversations.
 *
 * Captures golden images for ALL visual states of the CometChatConversations component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * This test uses the same approach as real-world usage: inflate CometChatConversations,
 * set it as the activity's content view, and inject a ViewModel with mock data.
 * The activity handles layout naturally, ensuring proper rendering of all
 * decorations (separators, badges, etc.).
 *
 * This file combines:
 *   - Static screenshot tests (UI states, visibility, custom views, style, content, dark theme)
 *   - Interaction-based tests (Espresso click/scroll for selection mode, toolbar)
 *   - Popup menu tests (Espresso longClick + captureWithPopups for popup rendering)
 *
 * Each test method produces one golden PNG in src/test/snapshots/conversations/.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatConversationsScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatConversationsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatConversationsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/conversations"
        )
    )

    // ==================== Constants ====================

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars"

        // Fixed epoch timestamp (Jan 1, 2025 UTC) — deterministic across runs (for popup tests)
        private const val FIXED_TIMESTAMP = 1735689600L
    }

    // ==================== Timestamp Helpers ====================

    private val NOW: Long get() = System.currentTimeMillis() / 1000
    private val FIVE_MINUTES_AGO: Long get() = NOW - (5 * 60)
    private val TWO_HOURS_AGO: Long get() = NOW - (2 * 60 * 60)
    private val YESTERDAY: Long get() = NOW - (24 * 60 * 60)
    private val TWO_DAYS_AGO: Long get() = NOW - (2 * 24 * 60 * 60)
    private val FIVE_DAYS_AGO: Long get() = NOW - (5 * 24 * 60 * 60)

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load conversations")
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 2: Popup Menu (Espresso longClick + captureWithPopups) ====================

    /**
     * Long-press a user conversation → default popup menu appears with "Delete" option.
     */
    @Test
    fun longPressShowsDefaultPopupMenu() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            createConversationsView(activity).apply {
                setViewModel(createViewModel(popupSharedConversations))
            }
        }
    }

    /**
     * Long-press with custom options (Pin, Mute, Archive, Delete).
     */
    @Test
    fun longPressShowsCustomPopupMenu() {
        launchAndCapturePopup(itemPosition = 1) { activity ->
            createConversationsView(activity).apply {
                setViewModel(createViewModel(popupSharedConversations))
                setOptions { _, _ ->
                    listOf(
                        CometChatPopupMenu.MenuItem("pin", "Pin Chat", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("mute", "Mute Notifications", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("archive", "Archive", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("delete", "Delete", null, null, 0, 0, 0, 0, null)
                    )
                }
            }
        }
    }

    /**
     * Long-press on a group conversation → popup menu appears.
     */
    @Test
    fun longPressGroupConversationShowsPopup() {
        launchAndCapturePopup(itemPosition = 5) { activity ->
            createConversationsView(activity).apply {
                setViewModel(createViewModel(popupSharedConversations))
            }
        }
    }

    /**
     * Dark theme variant — default popup.
     */
    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun longPressShowsDefaultPopupMenuDark() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            createConversationsView(activity).apply {
                setViewModel(createViewModel(popupSharedConversations))
            }
        }
    }

    // ==================== Section 3: Selection Mode Interactions (Espresso click) ====================

    /**
     * Single selection mode: tap an item → shows selection checkbox on that item.
     * Toolbar should change to show selection count.
     */
    @Test
    fun singleSelectionAfterTap() {
        launchInteractWithConfig(
            conversations = createInteractionUserConversations(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            1, click()
                        )
                    )
            }
        )
    }

    /**
     * Multiple selection mode: tap multiple items → shows checkboxes on all selected.
     * Toolbar should show count of selected items.
     */
    @Test
    fun multipleSelectionAfterTaps() {
        launchInteractWithConfig(
            conversations = createInteractionUserConversations(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            2, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            4, click()
                        )
                    )
            }
        )
    }

    /**
     * Multiple selection → deselect one item → verify visual state updates.
     */
    @Test
    fun selectionThenDeselect() {
        launchInteractWithConfig(
            conversations = createInteractionUserConversations(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                for (i in 0..2) {
                    onView(withId(R.id.recyclerview_conversations_list))
                        .perform(
                            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                i, click()
                            )
                        )
                    ShadowLooper.idleMainLooper()
                }

                // Deselect item 1
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            1, click()
                        )
                    )
            }
        )
    }

    /**
     * High unread counts → tap items in selection mode.
     * Verifies unread badges don't interfere with selection checkboxes.
     */
    @Test
    fun highUnreadWithSelection() {
        launchInteractWithConfig(
            conversations = createInteractionUserConversationsWithUnread(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            3, click()
                        )
                    )
            }
        )
    }

    /**
     * Mixed user + group conversations → select both user and group conversations.
     */
    @Test
    fun selectMixedConversations() {
        launchInteractWithConfig(
            conversations = createInteractionMixedConversations(6),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            3, click()
                        )
                    )
            }
        )
    }

    // ==================== Section 4: Scroll States (Espresso scroll) ====================

    /**
     * Scroll to the bottom of a long list (25 conversations).
     */
    @Test
    fun scrollToBottom() {
        launchInteract(
            conversations = createLargeConversationList(25),
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(24)
                    )
            }
        )
    }

    /**
     * Scroll to middle of a long list (25 conversations).
     */
    @Test
    fun scrollToMiddle() {
        launchInteract(
            conversations = createLargeConversationList(25),
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(12)
                    )
            }
        )
    }

    // ==================== Section 5: Toolbar Interactions (Espresso) ====================

    /**
     * Selection mode active → tap discard (X) button in toolbar → selection cleared.
     */
    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        launchInteractWithConfig(
            conversations = createInteractionUserConversations(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_conversations_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            2, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                // Click the cross/discard button in the toolbar to clear selection
                onView(withContentDescription("Discard selection"))
                    .perform(click())
            }
        )
    }

    // ==================== Section 6: Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setToolbarVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setSearchBoxVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setUserStatusVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoGroupType() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealGroupConversations(5))
            view.setViewModel(vm)
            view.setGroupTypeVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoReceipts() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setReceiptsVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setBackIconVisibility(View.VISIBLE)
            view
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customLoading = TextView(activity).apply {
                text = "Custom Loading..."
                textSize = 18f
                setTextColor(Color.DKGRAY)
                setPadding(32, 64, 32, 64)
            }
            view.setLoadingView(customLoading)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customEmpty = TextView(activity).apply {
                text = "No conversations yet!\nStart chatting now."
                textSize = 16f
                setTextColor(Color.GRAY)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setEmptyView(customEmpty)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customError = TextView(activity).apply {
                text = "Oops! Something went wrong.\nPlease try again later."
                textSize = 16f
                setTextColor(Color.RED)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setErrorView(customError)
            val vm = createErrorViewModel("CUSTOM_ERR", "Custom error")
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customLeadingView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            view.setLeadingView(object : ConversationsViewHolderListener() {
                override fun createView(context: Context, binding: CometchatConversationsListItemsBinding): View {
                    return TextView(context).apply {
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#6851D6"))
                        setPadding(24, 24, 24, 24)
                        gravity = android.view.Gravity.CENTER
                    }
                }
                override fun bindView(context: Context, createdView: View, conversation: Conversation, typingIndicator: TypingIndicator?, holder: RecyclerView.ViewHolder, conversationList: List<Conversation>, position: Int) {
                    val name = when (val entity = conversation.conversationWith) {
                        is User -> entity.name
                        is Group -> entity.name
                        else -> "?"
                    }
                    (createdView as TextView).text = name.first().uppercase()
                }
            })
            view
        }
    }

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            view.setTitleView(object : ConversationsViewHolderListener() {
                override fun createView(context: Context, binding: CometchatConversationsListItemsBinding): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.parseColor("#6851D6"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                }
                override fun bindView(context: Context, createdView: View, conversation: Conversation, typingIndicator: TypingIndicator?, holder: RecyclerView.ViewHolder, conversationList: List<Conversation>, position: Int) {
                    val name = when (val entity = conversation.conversationWith) {
                        is User -> entity.name
                        is Group -> entity.name
                        else -> "Unknown"
                    }
                    (createdView as TextView).text = "★ $name"
                }
            })
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            view.setSubtitleView(object : ConversationsViewHolderListener() {
                override fun createView(context: Context, binding: CometchatConversationsListItemsBinding): View {
                    return TextView(context).apply {
                        textSize = 13f
                        setTextColor(Color.parseColor("#FF6600"))
                    }
                }
                override fun bindView(context: Context, createdView: View, conversation: Conversation, typingIndicator: TypingIndicator?, holder: RecyclerView.ViewHolder, conversationList: List<Conversation>, position: Int) {
                    (createdView as TextView).text = "🔔 Custom notification message"
                }
            })
            view
        }
    }

    @Test
    fun customTrailingView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            view.setTrailingView(object : ConversationsViewHolderListener() {
                override fun createView(context: Context, binding: CometchatConversationsListItemsBinding): View {
                    return TextView(context).apply {
                        textSize = 12f
                        setTextColor(Color.parseColor("#6851D6"))
                        setPadding(8, 4, 8, 4)
                    }
                }
                override fun bindView(context: Context, createdView: View, conversation: Conversation, typingIndicator: TypingIndicator?, holder: RecyclerView.ViewHolder, conversationList: List<Conversation>, position: Int) {
                    (createdView as TextView).text = "📌 Pinned"
                }
            })
            view
        }
    }

    @Test
    fun customItemView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            view.setItemView(object : ConversationsViewHolderListener() {
                override fun createView(context: Context, binding: CometchatConversationsListItemsBinding): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.DKGRAY)
                        setPadding(48, 32, 48, 32)
                    }
                }
                override fun bindView(context: Context, createdView: View, conversation: Conversation, typingIndicator: TypingIndicator?, holder: RecyclerView.ViewHolder, conversationList: List<Conversation>, position: Int) {
                    val name = when (val entity = conversation.conversationWith) {
                        is User -> entity.name
                        is Group -> entity.name
                        else -> "Unknown"
                    }
                    (createdView as TextView).text = "⭐ $name — Custom Item View"
                }
            })
            view
        }
    }

    @Test
    fun overflowMenu() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(3))
            view.setViewModel(vm)
            val menuButton = TextView(activity).apply {
                text = "⋮"
                textSize = 24f
                setTextColor(Color.DKGRAY)
                setPadding(16, 8, 16, 8)
            }
            view.setOverflowMenu(menuButton)
            view
        }
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun toolbarCustomTitle() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setTitle("My Chats")
            view
        }
    }

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            val customStyle = CometChatConversationsStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
            view
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            val customStyle = CometChatConversationsStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                titleTextColor = Color.WHITE,
                separatorColor = Color.parseColor("#333333")
            )
            view.setStyle(customStyle)
            view.setTitleTextColor(Color.WHITE)
            view
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentHighUnreadCounts() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = createRealUserConversationsWithUnread(5)
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentGroupsOnly() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealGroupConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentUsersOnly() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentLargeList() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealMixedConversations(20))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun datePattern() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = listOf(
                createRealConversation("conv_1", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("Just now message", NOW), 0),
                createRealConversation("conv_2", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("5 minutes ago", FIVE_MINUTES_AGO), 0),
                createRealConversation("conv_3", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_OFFLINE),
                    createRealTextMessage("2 hours ago", TWO_HOURS_AGO), 0),
                createRealConversation("conv_4", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u4", "Black Widow", "$AVATAR_BASE_URL/blackwidow.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("Yesterday message", YESTERDAY), 0),
                createRealConversation("conv_5", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u5", "Thor", "$AVATAR_BASE_URL/thor.png", CometChatConstants.USER_STATUS_OFFLINE),
                    createRealTextMessage("5 days ago", FIVE_DAYS_AGO), 0)
            )
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun readReceiptsVariation() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = listOf(
                // Sent only
                createRealConversation("conv_r1", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("Sent message", NOW, deliveredAt = 0, readAt = 0), 0),
                // Delivered
                createRealConversation("conv_r2", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("Delivered message", FIVE_MINUTES_AGO, deliveredAt = FIVE_MINUTES_AGO, readAt = 0), 0),
                // Read
                createRealConversation("conv_r3", CometChatConstants.CONVERSATION_TYPE_USER,
                    createRealUser("u3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_ONLINE),
                    createRealTextMessage("Read message", TWO_HOURS_AGO, deliveredAt = TWO_HOURS_AGO, readAt = TWO_HOURS_AGO), 0),
                // Sent only (group)
                createRealConversation("conv_r4", CometChatConstants.CONVERSATION_TYPE_GROUP,
                    createRealGroup("g1", "The Avengers", "$AVATAR_BASE_URL/avengers.png", CometChatConstants.GROUP_TYPE_PUBLIC),
                    createRealTextMessage("Group sent", YESTERDAY, deliveredAt = 0, readAt = 0), 2),
                // Delivered (group)
                createRealConversation("conv_r5", CometChatConstants.CONVERSATION_TYPE_GROUP,
                    createRealGroup("g2", "Design Team", "$AVATAR_BASE_URL/designteam.png", CometChatConstants.GROUP_TYPE_PRIVATE),
                    createRealTextMessage("Group delivered", TWO_DAYS_AGO, deliveredAt = TWO_DAYS_AGO, readAt = 0), 0)
            )
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun mentionsInConversations() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = listOf(
                createRealConversation("conv_m1", CometChatConstants.CONVERSATION_TYPE_GROUP,
                    createRealGroup("g1", "The Avengers", "$AVATAR_BASE_URL/avengers.png", CometChatConstants.GROUP_TYPE_PUBLIC),
                    createRealTextMessage("@Iron Man can you review the PR?", NOW), 1),
                createRealConversation("conv_m2", CometChatConstants.CONVERSATION_TYPE_GROUP,
                    createRealGroup("g2", "Developers Hub", "$AVATAR_BASE_URL/developershub.png", CometChatConstants.GROUP_TYPE_PRIVATE),
                    createRealTextMessage("@Captain America @Spiderman meeting at 3 PM", FIVE_MINUTES_AGO), 3),
                createRealConversation("conv_m3", CometChatConstants.CONVERSATION_TYPE_GROUP,
                    createRealGroup("g3", "Design Team", "$AVATAR_BASE_URL/designteam.png", CometChatConstants.GROUP_TYPE_PUBLIC),
                    createRealTextMessage("@all Please check the new designs", TWO_HOURS_AGO), 5)
            )
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun contentHighUnreadCountsDark() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = createRealUserConversationsWithUnread(5)
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun visibilityNoSeparatorsDark() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createRealUserConversations(5))
            view.setViewModel(vm)
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    /**
     * Launches an ActivityScenario, inflates CometChatConversations,
     * configures it via the provided block, and captures a screenshot.
     */
    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatConversations
    ) {
        launchAndCaptureWithPostAction(configure = configure, postAction = null)
    }

    /**
     * Launches an ActivityScenario, inflates CometChatConversations,
     * configures it, idles the looper (so ViewModel flows are collected),
     * then runs a post-action (e.g., selection) before capturing.
     *
     * Uses the same approach as real-world usage:
     *   activity.setContentView(CometChatConversations(this))
     *
     * The activity handles layout naturally, ensuring proper rendering of all
     * decorations and view hierarchies.
     */
    private fun launchAndCaptureWithPostAction(
        configure: (ComponentActivity) -> CometChatConversations,
        postAction: ((CometChatConversations) -> Unit)? = null
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            // Set content view just like real usage: activity.setContentView(CometChatConversations(this))
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            ShadowLooper.idleMainLooper()

            postAction?.invoke(view)
            ShadowLooper.idleMainLooper()
        }

        // Let the looper process all pending messages (layout, draw, ViewModel emissions)
        ShadowLooper.idleMainLooper()

        // Capture from the decor view to include all drawn decorations
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Popup Capture (captureWithPopups) ====================

    /**
     * Launches the activity, populates CometChatConversations, performs a long-click on
     * the RecyclerView item at [itemPosition], and captures the full decor view so the
     * PopupWindow is included via [activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())].
     */
    private fun launchAndCapturePopup(
        itemPosition: Int,
        configure: (ComponentActivity) -> CometChatConversations
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = configure(activity)
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ShadowLooper.idleMainLooper()

        // Long-press the target item using Espresso
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    itemPosition, longClick()
                )
            )

        ShadowLooper.idleMainLooper()

        // Capture entire window (root view + PopupWindow) using composite capture
        scenario.onActivity { activity ->
            activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Capture (Espresso + captureRoboImage) ====================

    /**
     * Simple interaction: load conversations, perform Espresso interaction, capture.
     */
    private fun launchInteract(
        conversations: List<Conversation>,
        interaction: () -> Unit
    ) {
        launchInteractWithConfig(
            conversations = conversations,
            configure = null,
            interaction = interaction
        )
    }

    /**
     * Full interaction flow:
     * 1. Launch Activity
     * 2. Create CometChatConversations with ViewModel
     * 3. Apply optional configuration (selection mode, etc.)
     * 4. Set content view (just like real usage)
     * 5. Idle looper (ViewModel flows collected, adapter populated)
     * 6. Perform Espresso interaction (click, longClick, scroll)
     * 7. Idle looper again (UI updates from interaction)
     * 8. Capture screenshot of the decor view
     */
    private fun launchInteractWithConfig(
        conversations: List<Conversation>,
        configure: ((CometChatConversations) -> Unit)? = null,
        interaction: () -> Unit,
        postInteraction: ((CometChatConversations) -> Unit)? = null
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)

        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatConversations(activity)
            val vm = createViewModel(conversations)
            view.setViewModel(vm)

            configure?.invoke(view)

            // Set content view just like real usage
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ShadowLooper.idleMainLooper()

        // Perform Espresso interaction (outside onActivity for proper Espresso sync)
        interaction()
        ShadowLooper.idleMainLooper()

        scenario.onActivity { activity ->
            postInteraction?.invoke(
                activity.findViewById<ViewGroup>(android.R.id.content)
                    .getChildAt(0) as CometChatConversations
            )
            ShadowLooper.idleMainLooper()

            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }

        scenario.close()
    }

    // ==================== Avatar Fix ====================

    private fun fixAvatarCircularRendering(root: View) {
        if (root is CometChatAvatar) {
            root.radius = Float.MAX_VALUE
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                fixAvatarCircularRendering(root.getChildAt(i))
            }
        }
    }

    // ==================== View Factory ====================

    private fun createConversationsView(activity: ComponentActivity): CometChatConversations {
        return CometChatConversations(activity)
    }

    // ==================== Data Factories (Static Tests) ====================

    private fun createRealUser(
        uid: String,
        name: String,
        avatar: String,
        status: String
    ): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.avatar = avatar
            this.status = status
        }
    }

    private fun createRealGroup(
        guid: String,
        name: String,
        icon: String,
        groupType: String,
        membersCount: Int = 5
    ): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
            this.icon = icon
            this.groupType = groupType
            this.membersCount = membersCount
        }
    }

    private fun createRealTextMessage(
        text: String,
        sentAt: Long,
        deliveredAt: Long = 0,
        readAt: Long = 0,
        sender: User? = null,
        id: Long = 1L
    ): TextMessage {
        return TextMessage("receiver_1", text, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sentAt = sentAt
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.sender = sender ?: createRealUser(
                "sender_1",
                "Iron Man",
                "$AVATAR_BASE_URL/ironman.png",
                CometChatConstants.USER_STATUS_ONLINE
            )
        }
    }

    private fun createRealConversation(
        conversationId: String,
        type: String,
        conversationWith: Any,
        lastMessage: TextMessage?,
        unreadCount: Int = 0
    ): Conversation {
        return Conversation(conversationId, type).apply {
            this.conversationWith = conversationWith as com.cometchat.chat.models.AppEntity
            this.lastMessage = lastMessage
            this.unreadMessageCount = unreadCount
        }
    }

    private fun createRealUserConversations(count: Int): List<Conversation> {
        val users = listOf(
            createRealUser("user_1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("user_3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_4", "Black Widow", "$AVATAR_BASE_URL/blackwidow.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("user_5", "Thor", "$AVATAR_BASE_URL/thor.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_6", "Hulk", "$AVATAR_BASE_URL/hulk.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_7", "Hawkeye", "$AVATAR_BASE_URL/hawkeye.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("user_8", "Black Panther", "$AVATAR_BASE_URL/blackpanther.png", CometChatConstants.USER_STATUS_ONLINE)
        )

        val messages = listOf(
            "Hey, are we meeting today?",
            "Sure, let me check my schedule",
            "The build passed ✅",
            "Shared a photo",
            "Let's catch up tomorrow",
            "On my way!",
            "Got it, thanks!",
            "Sounds good 👍"
        )

        val timestamps = listOf(NOW, FIVE_MINUTES_AGO, TWO_HOURS_AGO, YESTERDAY, TWO_DAYS_AGO, FIVE_DAYS_AGO)

        return (0 until count.coerceAtMost(users.size)).map { i ->
            val user = users[i]
            val timestamp = timestamps[i % timestamps.size]
            val deliveredAt = if (i % 3 == 0) timestamp else 0L
            val readAt = if (i % 5 == 0) timestamp else 0L

            createRealConversation(
                conversationId = "conv_user_${user.uid}",
                type = CometChatConstants.CONVERSATION_TYPE_USER,
                conversationWith = user,
                lastMessage = createRealTextMessage(
                    text = messages[i % messages.size],
                    sentAt = timestamp,
                    deliveredAt = deliveredAt,
                    readAt = readAt,
                    sender = user,
                    id = (i + 1).toLong()
                ),
                unreadCount = 0
            )
        }
    }

    private fun createRealUserConversationsWithUnread(count: Int): List<Conversation> {
        val users = listOf(
            createRealUser("user_1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("user_4", "Black Widow", "$AVATAR_BASE_URL/blackwidow.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("user_5", "Thor", "$AVATAR_BASE_URL/thor.png", CometChatConstants.USER_STATUS_OFFLINE)
        )

        val messages = listOf(
            "We need to talk about the mission",
            "Assemble! 🦸",
            "I can do this all day",
            "With great power comes great responsibility",
            "Mission complete ✓"
        )

        val timestamps = listOf(NOW, FIVE_MINUTES_AGO, TWO_HOURS_AGO, YESTERDAY, TWO_DAYS_AGO)
        val unreadCounts = listOf(10, 20, 30, 40, 50)

        return (0 until count.coerceAtMost(users.size)).map { i ->
            val user = users[i]
            val timestamp = timestamps[i]

            createRealConversation(
                conversationId = "conv_user_${user.uid}",
                type = CometChatConstants.CONVERSATION_TYPE_USER,
                conversationWith = user,
                lastMessage = createRealTextMessage(
                    text = messages[i],
                    sentAt = timestamp,
                    deliveredAt = timestamp,
                    readAt = 0,
                    sender = user,
                    id = (i + 1).toLong()
                ),
                unreadCount = unreadCounts[i]
            )
        }
    }

    private fun createRealGroupConversations(count: Int): List<Conversation> {
        val groups = listOf(
            createRealGroup("group_1", "The Avengers", "$AVATAR_BASE_URL/avengers.png", CometChatConstants.GROUP_TYPE_PUBLIC, 12),
            createRealGroup("group_2", "Justice League", "$AVATAR_BASE_URL/justiceleague.png", CometChatConstants.GROUP_TYPE_PRIVATE, 8),
            createRealGroup("group_3", "Design Team", "$AVATAR_BASE_URL/designteam.png", CometChatConstants.GROUP_TYPE_PASSWORD, 5),
            createRealGroup("group_4", "Developers Hub", "$AVATAR_BASE_URL/developershub.png", CometChatConstants.GROUP_TYPE_PUBLIC, 25),
            createRealGroup("group_5", "S.H.I.E.L.D.", "$AVATAR_BASE_URL/shield.png", CometChatConstants.GROUP_TYPE_PRIVATE, 15),
            createRealGroup("group_6", "X-Men", "$AVATAR_BASE_URL/xmen.png", CometChatConstants.GROUP_TYPE_PUBLIC, 30),
            createRealGroup("group_7", "Guardians", "$AVATAR_BASE_URL/guardians.png", CometChatConstants.GROUP_TYPE_PASSWORD, 7),
            createRealGroup("group_8", "Fantastic Four", "$AVATAR_BASE_URL/fantasticfour.png", CometChatConstants.GROUP_TYPE_PRIVATE, 4)
        )

        val messages = listOf(
            "Avengers assemble!",
            "New mockups ready for review",
            "Sprint planning at 3 PM",
            "The build passed ✅",
            "Can you review my PR?",
            "Meeting in 5 minutes",
            "Let me check",
            "Talk soon!"
        )

        val timestamps = listOf(NOW, FIVE_MINUTES_AGO, TWO_HOURS_AGO, YESTERDAY, TWO_DAYS_AGO, FIVE_DAYS_AGO)

        return (0 until count.coerceAtMost(groups.size)).map { i ->
            val group = groups[i]
            val timestamp = timestamps[i % timestamps.size]
            val deliveredAt = if (i % 2 == 0) timestamp else 0L
            val readAt = if (i % 4 == 0) timestamp else 0L

            createRealConversation(
                conversationId = "conv_group_${group.guid}",
                type = CometChatConstants.CONVERSATION_TYPE_GROUP,
                conversationWith = group,
                lastMessage = createRealTextMessage(
                    text = messages[i % messages.size],
                    sentAt = timestamp,
                    deliveredAt = deliveredAt,
                    readAt = readAt,
                    id = (i + 100).toLong()
                ),
                unreadCount = 0
            )
        }
    }

    private fun createRealMixedConversations(count: Int): List<Conversation> {
        val userCount = count / 2
        val groupCount = count - userCount
        return createRealUserConversations(userCount) + createRealGroupConversations(groupCount)
    }

    // ==================== Data Factories (Interaction Tests) ====================

    private fun createInteractionUserConversations(count: Int): List<Conversation> {
        val users = listOf(
            createRealUser("u1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("u3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u4", "Black Widow", "$AVATAR_BASE_URL/blackwidow.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("u5", "Thor", "$AVATAR_BASE_URL/thor.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u6", "Hulk", "$AVATAR_BASE_URL/hulk.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u7", "Hawkeye", "$AVATAR_BASE_URL/hawkeye.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("u8", "Black Panther", "$AVATAR_BASE_URL/blackpanther.png", CometChatConstants.USER_STATUS_ONLINE)
        )
        val messages = listOf(
            "Hey, are we meeting today?", "Sure, let me check",
            "The build passed ✅", "Shared a photo",
            "Let's catch up tomorrow", "On my way!",
            "Got it, thanks!", "Sounds good 👍"
        )
        val timestamps = listOf(NOW, FIVE_MINUTES_AGO, TWO_HOURS_AGO, YESTERDAY, TWO_DAYS_AGO)

        return (0 until count.coerceAtMost(users.size)).map { i ->
            createRealConversation(
                "conv_${users[i].uid}", CometChatConstants.CONVERSATION_TYPE_USER,
                users[i],
                createRealTextMessage(messages[i % messages.size], timestamps[i % timestamps.size], id = (i + 1).toLong()),
                0
            )
        }
    }

    private fun createInteractionUserConversationsWithUnread(count: Int): List<Conversation> {
        val users = listOf(
            createRealUser("u1", "Iron Man", "$AVATAR_BASE_URL/ironman.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u2", "Captain America", "$AVATAR_BASE_URL/captainamerica.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u3", "Spiderman", "$AVATAR_BASE_URL/spiderman.png", CometChatConstants.USER_STATUS_OFFLINE),
            createRealUser("u4", "Black Widow", "$AVATAR_BASE_URL/blackwidow.png", CometChatConstants.USER_STATUS_ONLINE),
            createRealUser("u5", "Thor", "$AVATAR_BASE_URL/thor.png", CometChatConstants.USER_STATUS_OFFLINE)
        )
        val unreadCounts = listOf(10, 25, 99, 150, 999)

        return (0 until count.coerceAtMost(users.size)).map { i ->
            createRealConversation(
                "conv_${users[i].uid}", CometChatConstants.CONVERSATION_TYPE_USER,
                users[i],
                createRealTextMessage("Unread message $i", NOW - (i * 300L), id = (i + 1).toLong()),
                unreadCounts[i]
            )
        }
    }

    private fun createInteractionMixedConversations(count: Int): List<Conversation> {
        val userConvs = createInteractionUserConversations(count / 2)
        val groups = listOf(
            createRealGroup("g1", "The Avengers", "$AVATAR_BASE_URL/avengers.png", CometChatConstants.GROUP_TYPE_PUBLIC),
            createRealGroup("g2", "Design Team", "$AVATAR_BASE_URL/designteam.png", CometChatConstants.GROUP_TYPE_PRIVATE),
            createRealGroup("g3", "Developers Hub", "$AVATAR_BASE_URL/developershub.png", CometChatConstants.GROUP_TYPE_PASSWORD)
        )
        val groupConvs = (0 until (count - count / 2).coerceAtMost(groups.size)).map { i ->
            createRealConversation(
                "conv_${groups[i].guid}", CometChatConstants.CONVERSATION_TYPE_GROUP,
                groups[i],
                createRealTextMessage("Group message $i", NOW - (i * 600L), id = (i + 100).toLong()),
                if (i % 2 == 0) 3 else 0
            )
        }
        return userConvs + groupConvs
    }

    /**
     * Creates a large list of 20+ conversations for scroll testing.
     */
    private fun createLargeConversationList(count: Int): List<Conversation> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "Winter Soldier", "War Machine",
            "Loki", "Gamora", "Star-Lord", "Groot", "Rocket",
            "Nebula", "Drax", "Mantis", "Shuri", "Okoye",
            "Valkyrie", "Korg", "Wong", "Nick Fury", "Maria Hill"
        )
        val messages = listOf(
            "Hey, are we meeting today?", "Sure, let me check my schedule",
            "The build passed ✅", "Can you review my PR?",
            "Let's catch up tomorrow", "On my way!",
            "Got it, thanks!", "Sounds good 👍",
            "New mockups ready", "Sprint planning at 3 PM",
            "Deployed to staging", "Bug fix merged",
            "Need your input on this", "Meeting in 5 minutes",
            "Great work on that feature!", "Let me know when you're free"
        )
        val statuses = listOf(CometChatConstants.USER_STATUS_ONLINE, CometChatConstants.USER_STATUS_OFFLINE)

        return (0 until count.coerceAtMost(names.size)).map { i ->
            val user = createRealUser(
                "user_$i",
                names[i],
                "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png",
                statuses[i % statuses.size]
            )
            val timestamp = NOW - (i * 1800L)
            createRealConversation(
                "conv_user_$i", CometChatConstants.CONVERSATION_TYPE_USER,
                user,
                createRealTextMessage(messages[i % messages.size], timestamp, id = (i + 1).toLong()),
                if (i % 4 == 0) (i + 1) else 0
            )
        }
    }

    // ==================== Data Factories (Popup Tests - Deterministic) ====================

    private data class PopupUserData(
        val id: String,
        val name: String,
        val messageText: String,
        val unreadCount: Int,
        val status: String
    )

    /**
     * Single shared conversation list used by popup test cases.
     * Fixed timestamps, names, and messages — no randomness → pixel-stable screenshots.
     */
    private val popupSharedConversations: List<Conversation> by lazy {
        createPopupDeterministicConversations()
    }

    private fun createPopupDeterministicConversations(): List<Conversation> {
        // 5 user conversations
        val users = listOf(
            PopupUserData("user_0", "Iron Man", "Hey!", 3, CometChatConstants.USER_STATUS_ONLINE),
            PopupUserData("user_1", "Captain America", "Sure thing", 0, CometChatConstants.USER_STATUS_OFFLINE),
            PopupUserData("user_2", "Spiderman", "Build passed ✅", 0, CometChatConstants.USER_STATUS_ONLINE),
            PopupUserData("user_3", "Black Widow", "On my way", 0, CometChatConstants.USER_STATUS_OFFLINE),
            PopupUserData("user_4", "Thor", "Sounds good 👍", 0, CometChatConstants.USER_STATUS_ONLINE)
        ).mapIndexed { i, data ->
            val user = User().apply {
                uid = data.id
                name = data.name
                avatar = "$AVATAR_BASE_URL/${data.name.lowercase().replace(" ", "")}.png"
                status = data.status
            }
            val message = TextMessage("receiver", data.messageText, CometChatConstants.RECEIVER_TYPE_USER).apply {
                id = (i + 1).toLong()
                sentAt = FIXED_TIMESTAMP - (i * 300L)
                sender = user
            }
            Conversation("conv_$i", CometChatConstants.CONVERSATION_TYPE_USER).apply {
                conversationWith = user
                lastMessage = message
                unreadMessageCount = data.unreadCount
            }
        }

        // 3 group conversations
        val groups = listOf(
            Triple("group_0", "The Avengers", 5),
            Triple("group_1", "Design Team", 0),
            Triple("group_2", "Developers Hub", 0)
        ).mapIndexed { i, (guid, name, unread) ->
            val group = Group().apply {
                this.guid = guid
                this.name = name
                icon = "$AVATAR_BASE_URL/${name.lowercase().replace(" ", "")}.png"
                groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            }
            val message = TextMessage("receiver", "Group message $i", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
                id = (i + 100).toLong()
                sentAt = FIXED_TIMESTAMP - ((i + 5) * 300L)
            }
            Conversation("conv_group_$i", CometChatConstants.CONVERSATION_TYPE_GROUP).apply {
                conversationWith = group
                lastMessage = message
                unreadMessageCount = unread
            }
        }

        return users + groups
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest) =
                Result.success(conversations)
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest) =
                Result.failure<List<Conversation>>(CometChatException(code, message))
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in Loading state by using a repository
     * that never completes (suspends indefinitely).
     */
    private fun createLoadingViewModel(): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }
}
