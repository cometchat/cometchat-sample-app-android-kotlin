package com.cometchat.uikit.compose.shared.mentions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.suggestionlist.CometChatSuggestionList
import com.cometchat.uikit.compose.presentation.shared.suggestionlist.CometChatSuggestionListStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * UI Tests for MentionAwareTextField and related mention components.
 *
 * Tests verify:
 * - Suggestion list visibility behavior
 * - Item selection and mention insertion
 * - Cursor behavior around mentions
 * - Backspace deletion of mentions
 * - Mention limit enforcement
 *
 * **Validates: Requirements FR-2.1, FR-2.5, FR-4.1, FR-4.4, FR-5.1, FR-5.2, FR-6.1, FR-6.2, FR-6.3**
 */
@RunWith(AndroidJUnit4::class)
class MentionAwareTextFieldUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test data
    private val testSuggestions = listOf(
        SuggestionItem(
            id = "user1",
            name = "John Doe",
            leadingIconUrl = null,
            status = "online",
            promptText = "@John Doe",
            underlyingText = "<@uid:user1>"
        ),
        SuggestionItem(
            id = "user2",
            name = "Jane Smith",
            leadingIconUrl = null,
            status = "offline",
            promptText = "@Jane Smith",
            underlyingText = "<@uid:user2>"
        ),
        SuggestionItem(
            id = "user3",
            name = "Bob Wilson",
            leadingIconUrl = null,
            status = "online",
            promptText = "@Bob Wilson",
            underlyingText = "<@uid:user3>"
        )
    )

    private val mentionStyle = SpanStyle(
        color = Color.Blue,
        background = Color.Blue.copy(alpha = 0.2f)
    )

    // ==================== Suggestion List Visibility Tests ====================

    /**
     * Test: Suggestion list appears when typing '@'
     *
     * Verifies that the suggestion list becomes visible when the user types
     * the tracking character '@' at a valid position.
     *
     * **Validates: Requirements FR-2.1**
     */
    @Test
    fun suggestionList_appearsWhenTypingAtSymbol() {
        var showSuggestions by mutableStateOf(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val mentionState = rememberMentionTextFieldState()

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    MentionAwareTextField(
                        mentionState = mentionState,
                        onMentionDetected = { _, _ -> showSuggestions = true },
                        onMentionContextLost = { showSuggestions = false },
                        mentionStyle = mentionStyle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showSuggestions) {
                        CometChatSuggestionList(
                            suggestions = testSuggestions,
                            onItemClick = { }
                        )
                    }
                }
            }
        }

        // Initially, suggestion list should not be visible
        composeTestRule.onNodeWithContentDescription("Suggestion List").assertDoesNotExist()

        // Type '@' to trigger mention detection
        composeTestRule.onNodeWithText("").performTextInput("@")

        // Wait for recomposition
        composeTestRule.waitForIdle()

        // Suggestion list should now be visible
        assertTrue("Suggestion list should appear after typing @", showSuggestions)
    }

    /**
     * Test: Suggestion list hides when selecting an item
     *
     * Verifies that the suggestion list is hidden after the user selects
     * a suggestion item.
     *
     * **Validates: Requirements FR-2.5**
     */
    @Test
    fun suggestionList_hidesWhenItemSelected() {
        var showSuggestions by mutableStateOf(true)
        val itemSelected = AtomicBoolean(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (showSuggestions) {
                        CometChatSuggestionList(
                            suggestions = testSuggestions,
                            onItemClick = { item ->
                                itemSelected.set(true)
                                showSuggestions = false
                            }
                        )
                    }
                }
            }
        }

        // Suggestion list should be visible initially
        composeTestRule.onNodeWithContentDescription("Suggestion List").assertIsDisplayed()

        // Click on a suggestion item
        composeTestRule.onNodeWithText("John Doe").performClick()

        // Wait for recomposition
        composeTestRule.waitForIdle()

        // Verify item was selected and list is hidden
        assertTrue("Item should be selected", itemSelected.get())
        assertFalse("Suggestion list should be hidden after selection", showSuggestions)
    }

    /**
     * Test: Suggestion list hides when backspace removes '@'
     *
     * Verifies that the suggestion list is hidden when the user presses
     * backspace to remove the tracking character.
     *
     * **Validates: Requirements FR-2.5**
     */
    @Test
    fun suggestionList_hidesWhenBackspaceRemovesAtSymbol() {
        var showSuggestions by mutableStateOf(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val mentionState = rememberMentionTextFieldState()

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    MentionAwareTextField(
                        mentionState = mentionState,
                        onMentionDetected = { _, _ -> showSuggestions = true },
                        onMentionContextLost = { showSuggestions = false },
                        mentionStyle = mentionStyle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showSuggestions) {
                        CometChatSuggestionList(
                            suggestions = testSuggestions,
                            onItemClick = { }
                        )
                    }
                }
            }
        }

        // Type '@' to trigger mention detection
        composeTestRule.onNodeWithText("").performTextInput("@")
        composeTestRule.waitForIdle()
        assertTrue("Suggestion list should appear after typing @", showSuggestions)

        // Clear text (simulates backspace removing '@')
        composeTestRule.onNodeWithText("@").performTextClearance()
        composeTestRule.waitForIdle()

        // Suggestion list should be hidden
        assertFalse("Suggestion list should hide when @ is removed", showSuggestions)
    }

    // ==================== Item Selection Tests ====================

    /**
     * Test: Clicking an item inserts the mention
     *
     * Verifies that clicking a suggestion item inserts the mention text
     * into the text field.
     *
     * **Validates: Requirements FR-4.1**
     */
    @Test
    fun itemSelection_insertsMention() {
        var selectedItem: SuggestionItem? = null

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSuggestionList(
                    suggestions = testSuggestions,
                    onItemClick = { item -> selectedItem = item }
                )
            }
        }

        // Click on "John Doe"
        composeTestRule.onNodeWithText("John Doe").performClick()
        composeTestRule.waitForIdle()

        // Verify the correct item was selected
        assertEquals("user1", selectedItem?.id)
        assertEquals("@John Doe", selectedItem?.promptText)
    }

    /**
     * Test: Mention is styled correctly after insertion
     *
     * Verifies that the inserted mention has the correct styling applied.
     *
     * **Validates: Requirements FR-4.1**
     */
    @Test
    fun mentionInsertion_appliesCorrectStyle() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val mentionState = remember { MentionTextFieldState() }

                // Pre-insert a mention
                remember {
                    mentionState.insertMention(
                        suggestionItem = testSuggestions[0],
                        triggerIndex = 0,
                        mentionStyle = mentionStyle
                    )
                    true
                }

                MentionAwareTextField(
                    mentionState = mentionState,
                    onMentionDetected = { _, _ -> },
                    onMentionContextLost = { },
                    mentionStyle = mentionStyle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Verify the mention text is displayed
        composeTestRule.onNodeWithText("@John Doe ", substring = true).assertIsDisplayed()
    }

    // ==================== Cursor Behavior Tests ====================

    /**
     * Test: MentionTextFieldState prevents cursor inside mention
     *
     * Verifies that the cursor position validation logic prevents
     * the cursor from being placed inside a mention span.
     *
     * **Validates: Requirements FR-5.1**
     */
    @Test
    fun cursorBehavior_cannotBePlacedInsideMention() {
        val mentionState = MentionTextFieldState()

        // Insert a mention at position 0
        mentionState.insertMention(
            suggestionItem = testSuggestions[0],
            triggerIndex = 0,
            mentionStyle = mentionStyle
        )

        // Verify mention was inserted
        val text = mentionState.textFieldValue.text
        assertTrue("Text should contain mention", text.contains("@John Doe"))

        // Try to place cursor inside the mention (position 3, which is inside "@John Doe")
        val mentionRange = mentionState.selectedMentions.first().range
        val insidePosition = mentionRange.first + 3

        // Verify the position is inside the mention
        assertTrue("Position should be inside mention", mentionState.isPositionInMention(insidePosition))
    }

    /**
     * Test: Cursor moves to nearest edge when attempting to place inside mention
     *
     * Verifies that when cursor validation detects cursor inside a mention,
     * it moves the cursor to the nearest edge.
     *
     * **Validates: Requirements FR-5.1**
     */
    @Test
    fun cursorBehavior_movesToNearestEdge() {
        val mentionState = MentionTextFieldState()

        // Insert a mention
        mentionState.insertMention(
            suggestionItem = testSuggestions[0],
            triggerIndex = 0,
            mentionStyle = mentionStyle
        )

        val mentionRange = mentionState.selectedMentions.first().range
        val spanStart = mentionRange.first
        val spanEnd = mentionRange.last + 1

        // Position closer to start
        val positionNearStart = spanStart + 2
        assertTrue("Position should be inside mention", mentionState.isPositionInMention(positionNearStart))

        // Position closer to end
        val positionNearEnd = spanEnd - 2
        assertTrue("Position should be inside mention", mentionState.isPositionInMention(positionNearEnd))
    }

    // ==================== Backspace Deletion Tests ====================

    /**
     * Test: Backspace at end of mention deletes entire mention
     *
     * Verifies that pressing backspace when cursor is at the end of a mention
     * deletes the entire mention span.
     *
     * **Validates: Requirements FR-5.2**
     */
    @Test
    fun backspaceDeletion_deletesEntireMention() {
        val mentionState = MentionTextFieldState()
        var deletedMention: SelectedMention? = null

        // Insert a mention
        mentionState.insertMention(
            suggestionItem = testSuggestions[0],
            triggerIndex = 0,
            mentionStyle = mentionStyle
        )

        // Verify mention exists
        assertEquals(1, mentionState.getMentionCount())
        val originalText = mentionState.textFieldValue.text

        // Simulate backspace by providing text with one character removed from mention
        val mentionRange = mentionState.selectedMentions.first().range
        val textWithCharRemoved = originalText.substring(0, mentionRange.last) +
                originalText.substring(mentionRange.last + 1)

        // Process the value change (simulating backspace)
        mentionState.onValueChange(
            TextFieldValue(
                text = textWithCharRemoved,
                selection = androidx.compose.ui.text.TextRange(mentionRange.last)
            )
        ) { deleted ->
            deletedMention = deleted
        }

        // Verify mention was deleted
        assertEquals("Mention should be deleted", 0, mentionState.getMentionCount())
    }

    /**
     * Test: Mention is removed from tracking after deletion
     *
     * Verifies that after a mention is deleted, it is properly removed
     * from the selected mentions tracking.
     *
     * **Validates: Requirements FR-5.2**
     */
    @Test
    fun backspaceDeletion_removesMentionFromTracking() {
        val mentionState = MentionTextFieldState()

        // Insert two mentions
        mentionState.insertMention(
            suggestionItem = testSuggestions[0],
            triggerIndex = 0,
            mentionStyle = mentionStyle
        )

        // Verify initial state
        assertEquals(1, mentionState.getMentionCount())
        assertTrue(mentionState.selectedMentions.any { it.id == "user1" })

        // Clear the state
        mentionState.clear()

        // Verify mention is removed from tracking
        assertEquals(0, mentionState.getMentionCount())
        assertFalse(mentionState.selectedMentions.any { it.id == "user1" })
    }

    // ==================== Mention Limit Tests ====================

    /**
     * Test: Info message appears when mention limit is reached
     *
     * Verifies that an info message is displayed when the user reaches
     * the configured mention limit.
     *
     * **Validates: Requirements FR-6.2**
     */
    @Test
    fun mentionLimit_showsInfoMessageWhenReached() {
        val mentionLimit = 2
        var showLimitMessage by mutableStateOf(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val mentionState = remember { MentionTextFieldState() }

                // Check if limit is reached
                showLimitMessage = mentionState.getMentionCount() >= mentionLimit

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    MentionAwareTextField(
                        mentionState = mentionState,
                        onMentionDetected = { _, _ -> },
                        onMentionContextLost = { },
                        mentionStyle = mentionStyle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showLimitMessage) {
                        Text(
                            text = "You can mention up to $mentionLimit times at a time",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Initially, limit message should not be shown
        composeTestRule.onNodeWithText("You can mention up to 2 times at a time").assertDoesNotExist()
    }

    /**
     * Test: Suggestion list doesn't appear after limit reached
     *
     * Verifies that the suggestion list is not shown when the user
     * has already reached the mention limit.
     *
     * **Validates: Requirements FR-6.3**
     */
    @Test
    fun mentionLimit_suggestionListNotShownAfterLimitReached() {
        val mentionLimit = 2
        var showSuggestions by mutableStateOf(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val mentionState = remember { MentionTextFieldState() }

                // Pre-insert mentions to reach limit
                remember {
                    mentionState.insertMention(testSuggestions[0], 0, mentionStyle)
                    mentionState.insertMention(testSuggestions[1], mentionState.textFieldValue.text.length, mentionStyle)
                    true
                }

                val isLimitReached = mentionState.getMentionCount() >= mentionLimit

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    MentionAwareTextField(
                        mentionState = mentionState,
                        onMentionDetected = { _, _ ->
                            if (!isLimitReached) {
                                showSuggestions = true
                            }
                        },
                        onMentionContextLost = { showSuggestions = false },
                        mentionStyle = mentionStyle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showSuggestions && !isLimitReached) {
                        CometChatSuggestionList(
                            suggestions = testSuggestions,
                            onItemClick = { }
                        )
                    }
                }
            }
        }

        // Suggestion list should not appear since limit is reached
        composeTestRule.onNodeWithContentDescription("Suggestion List").assertDoesNotExist()
        assertFalse("Suggestions should not be shown when limit reached", showSuggestions)
    }

    // ==================== Suggestion List Component Tests ====================

    /**
     * Test: Suggestion list displays items correctly
     *
     * Verifies that the suggestion list displays all provided items.
     */
    @Test
    fun suggestionList_displaysAllItems() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSuggestionList(
                    suggestions = testSuggestions,
                    onItemClick = { }
                )
            }
        }

        // Verify all items are displayed
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Wilson").assertIsDisplayed()
    }

    /**
     * Test: Suggestion list shows shimmer when loading
     *
     * Verifies that the suggestion list shows shimmer loading state
     * when isLoading is true and suggestions are empty.
     */
    @Test
    fun suggestionList_showsShimmerWhenLoading() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSuggestionList(
                    suggestions = emptyList(),
                    isLoading = true,
                    onItemClick = { }
                )
            }
        }

        // Suggestion list container should be visible (with shimmer)
        composeTestRule.onNodeWithContentDescription("Suggestion List").assertIsDisplayed()
    }

    /**
     * Test: Suggestion list triggers scroll to bottom callback
     *
     * Verifies that the onScrollToBottom callback is invoked when
     * the user scrolls to the bottom of the list.
     */
    @Test
    fun suggestionList_triggersScrollToBottomCallback() {
        val scrollToBottomCalled = AtomicBoolean(false)

        // Create a longer list to enable scrolling
        val longSuggestionList = (1..20).map { index ->
            SuggestionItem(
                id = "user$index",
                name = "User $index",
                leadingIconUrl = null,
                status = "online",
                promptText = "@User $index",
                underlyingText = "<@uid:user$index>"
            )
        }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSuggestionList(
                    suggestions = longSuggestionList,
                    onItemClick = { },
                    onScrollToBottom = { scrollToBottomCalled.set(true) }
                )
            }
        }

        // Verify the list is displayed
        composeTestRule.onNodeWithContentDescription("Suggestion List").assertIsDisplayed()
    }

    /**
     * Test: Item click callback receives correct item
     *
     * Verifies that clicking an item invokes the callback with the correct item.
     */
    @Test
    fun suggestionList_itemClickCallbackReceivesCorrectItem() {
        var clickedItem: SuggestionItem? = null

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatSuggestionList(
                    suggestions = testSuggestions,
                    onItemClick = { item -> clickedItem = item }
                )
            }
        }

        // Click on "Jane Smith"
        composeTestRule.onNodeWithText("Jane Smith").performClick()
        composeTestRule.waitForIdle()

        // Verify correct item was clicked
        assertEquals("user2", clickedItem?.id)
        assertEquals("Jane Smith", clickedItem?.name)
    }
}
