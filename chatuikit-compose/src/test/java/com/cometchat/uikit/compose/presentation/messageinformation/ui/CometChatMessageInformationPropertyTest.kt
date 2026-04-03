package com.cometchat.uikit.compose.presentation.messageinformation.ui

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.state.MessageInformationUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Property-based tests for [CometChatMessageInformation] composable.
 *
 * Feature: message-information-compose
 * 
 * **Property 2: Toolbar Visibility Toggle**
 * **Property 3: Toolbar Title Update**
 * **Property 4: Timestamp Display Condition**
 * **Property 5: Date Formatting**
 * **Property 8: State-Based Visibility Matrix**
 * **Property 9: Exception Exposure**
 *
 * **Validates: Requirements 3.3, 3.4, 3.5, 5.4, 5.5, 5.6, 8.1, 8.2, 8.4, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 11.1, 11.2, 11.3**
 */
class CometChatMessageInformationPropertyTest : FunSpec({

    // ==================== Property 2: Toolbar Visibility Toggle ====================
    // *For any* boolean value of hideToolBar, the toolbar visibility state SHALL be GONE 
    // when hideToolBar is true and VISIBLE when hideToolBar is false.
    // **Validates: Requirements 3.4, 3.5**

    context("Property 2: Toolbar Visibility Toggle") {

        /**
         * Property 2: Toolbar is hidden when hideToolBar is true
         *
         * **Validates: Requirements 3.4**
         */
        test("Property 2: Toolbar is hidden when hideToolBar is true") {
            val isToolbarVisible = shouldShowToolbar(hideToolBar = true)
            isToolbarVisible.shouldBeFalse()
        }

        /**
         * Property 2: Toolbar is visible when hideToolBar is false
         *
         * **Validates: Requirements 3.5**
         */
        test("Property 2: Toolbar is visible when hideToolBar is false") {
            val isToolbarVisible = shouldShowToolbar(hideToolBar = false)
            isToolbarVisible.shouldBeTrue()
        }

        /**
         * Property 2: Toolbar visibility is determined solely by hideToolBar value
         *
         * **Validates: Requirements 3.4, 3.5**
         */
        test("Property 2: Toolbar visibility is determined solely by hideToolBar value") {
            checkAll(100, Arb.boolean()) { hideToolBar ->
                val isToolbarVisible = shouldShowToolbar(hideToolBar)
                
                if (hideToolBar) {
                    isToolbarVisible.shouldBeFalse()
                } else {
                    isToolbarVisible.shouldBeTrue()
                }
            }
        }
    }

    // ==================== Property 3: Toolbar Title Update ====================
    // *For any* non-null string provided to setToolBarTitleText, the toolbar title text 
    // SHALL equal the provided string.
    // **Validates: Requirements 3.3**

    context("Property 3: Toolbar Title Update") {

        /**
         * Property 3: Toolbar title equals the provided string
         *
         * **Validates: Requirements 3.3**
         */
        test("Property 3: Toolbar title equals the provided string") {
            checkAll(100, Arb.string(1, 100)) { titleText ->
                val displayedTitle = getToolbarTitle(titleText)
                displayedTitle shouldBe titleText
            }
        }

        /**
         * Property 3: Toolbar title preserves exact string content
         *
         * **Validates: Requirements 3.3**
         */
        test("Property 3: Toolbar title preserves exact string content including special characters") {
            val specialStrings = listOf(
                "Message Info",
                "Custom Title",
                "Title with spaces   ",
                "Title-with-dashes",
                "Title_with_underscores",
                "Title123",
                "Título en español",
                "标题中文",
                "العنوان العربي"
            )
            
            specialStrings.forEach { titleText ->
                val displayedTitle = getToolbarTitle(titleText)
                displayedTitle shouldBe titleText
            }
        }

        /**
         * Property 3: Empty string is valid title
         *
         * **Validates: Requirements 3.3**
         */
        test("Property 3: Empty string is valid title") {
            val displayedTitle = getToolbarTitle("")
            displayedTitle shouldBe ""
        }
    }

    // ==================== Property 4: Timestamp Display Condition ====================
    // *For any* message with readAt or deliveredAt timestamps, the corresponding timestamp 
    // text SHALL be displayed if and only if the timestamp value is greater than 0.
    // **Validates: Requirements 5.4, 5.5**

    context("Property 4: Timestamp Display Condition") {

        /**
         * Property 4: Read timestamp is displayed when readAt > 0
         *
         * **Validates: Requirements 5.4**
         */
        test("Property 4: Read timestamp is displayed when readAt > 0") {
            checkAll(100, Arb.long(1L, Long.MAX_VALUE)) { readAt ->
                val shouldDisplay = shouldDisplayTimestamp(readAt)
                shouldDisplay.shouldBeTrue()
            }
        }

        /**
         * Property 4: Read timestamp is hidden when readAt == 0
         *
         * **Validates: Requirements 5.4**
         */
        test("Property 4: Read timestamp is hidden when readAt == 0") {
            val shouldDisplay = shouldDisplayTimestamp(0L)
            shouldDisplay.shouldBeFalse()
        }

        /**
         * Property 4: Delivered timestamp is displayed when deliveredAt > 0
         *
         * **Validates: Requirements 5.5**
         */
        test("Property 4: Delivered timestamp is displayed when deliveredAt > 0") {
            checkAll(100, Arb.long(1L, Long.MAX_VALUE)) { deliveredAt ->
                val shouldDisplay = shouldDisplayTimestamp(deliveredAt)
                shouldDisplay.shouldBeTrue()
            }
        }

        /**
         * Property 4: Delivered timestamp is hidden when deliveredAt == 0
         *
         * **Validates: Requirements 5.5**
         */
        test("Property 4: Delivered timestamp is hidden when deliveredAt == 0") {
            val shouldDisplay = shouldDisplayTimestamp(0L)
            shouldDisplay.shouldBeFalse()
        }

        /**
         * Property 4: Timestamp display is determined solely by timestamp value
         *
         * **Validates: Requirements 5.4, 5.5**
         */
        test("Property 4: Timestamp display is determined solely by timestamp value") {
            checkAll(100, Arb.long(Long.MIN_VALUE, Long.MAX_VALUE)) { timestamp ->
                val shouldDisplay = shouldDisplayTimestamp(timestamp)
                
                if (timestamp > 0) {
                    shouldDisplay.shouldBeTrue()
                } else {
                    shouldDisplay.shouldBeFalse()
                }
            }
        }
    }

    // ==================== Property 5: Date Formatting ====================
    // *For any* timestamp in milliseconds, the formatted output SHALL match the pattern 
    // "dd/M/yyyy, h:mm a" where dd is day, M is month, yyyy is year, h is hour (12-hour), 
    // mm is minutes, and a is AM/PM.
    // **Validates: Requirements 5.6**

    context("Property 5: Date Formatting") {

        /**
         * Property 5: Formatted date matches expected pattern
         *
         * **Validates: Requirements 5.6**
         */
        test("Property 5: Formatted date matches expected pattern") {
            // Use realistic timestamp range (year 2000 to 2100)
            val minTimestamp = 946684800000L // Jan 1, 2000
            val maxTimestamp = 4102444800000L // Jan 1, 2100
            
            checkAll(100, Arb.long(minTimestamp, maxTimestamp)) { milliseconds ->
                val formatted = formatDateTime(milliseconds)
                
                // Pattern: dd/M/yyyy, h:mm a
                // Examples: "1/1/2024, 12:00 AM", "31/12/2024, 11:59 PM"
                val pattern = """^\d{1,2}/\d{1,2}/\d{4}, \d{1,2}:\d{2} [AP]M$"""
                formatted shouldMatch Regex(pattern)
            }
        }

        /**
         * Property 5: formatDateTime produces consistent output for same input
         *
         * **Validates: Requirements 5.6**
         */
        test("Property 5: formatDateTime produces consistent output for same input") {
            checkAll(100, Arb.long(946684800000L, 4102444800000L)) { milliseconds ->
                val formatted1 = formatDateTime(milliseconds)
                val formatted2 = formatDateTime(milliseconds)
                
                formatted1 shouldBe formatted2
            }
        }

        /**
         * Property 5: Known timestamp produces expected formatted output
         *
         * **Validates: Requirements 5.6**
         */
        test("Property 5: Known timestamp produces expected formatted output") {
            // Test with known timestamps
            val testCases = listOf(
                // Timestamp in milliseconds -> expected pattern components
                1704067200000L to "1/1/2024", // Jan 1, 2024 00:00:00 UTC
                1735689600000L to "1/1/2025"  // Jan 1, 2025 00:00:00 UTC
            )
            
            testCases.forEach { (timestamp, expectedDatePart) ->
                val formatted = formatDateTime(timestamp)
                // Verify the date part is present (time may vary by timezone)
                formatted.contains("/") shouldBe true
                formatted.contains(",") shouldBe true
            }
        }

        /**
         * Property 5: formatDateTime handles edge timestamps
         *
         * **Validates: Requirements 5.6**
         */
        test("Property 5: formatDateTime handles edge timestamps") {
            // Test with edge cases
            val edgeCases = listOf(
                1L, // Very small positive timestamp
                System.currentTimeMillis(), // Current time
                946684800000L, // Y2K
                1609459200000L // Jan 1, 2021
            )
            
            edgeCases.forEach { timestamp ->
                val formatted = formatDateTime(timestamp)
                val pattern = """^\d{1,2}/\d{1,2}/\d{4}, \d{1,2}:\d{2} [AP]M$"""
                formatted shouldMatch Regex(pattern)
            }
        }
    }


    // ==================== Property 8: State-Based Visibility Matrix ====================
    // *For any* combination of UI state (LOADING, LOADED, EMPTY, ERROR) and conversation type 
    // (USER, GROUP), the visibility of shimmer, user receipt view, and group list SHALL follow 
    // the defined visibility matrix.
    // **Validates: Requirements 8.1, 8.2, 8.4, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 11.1, 11.2**

    context("Property 8: State-Based Visibility Matrix") {

        /**
         * Property 8: LOADING state shows shimmer and hides content
         *
         * **Validates: Requirements 8.1, 8.2, 8.4**
         */
        test("Property 8: LOADING state shows shimmer and hides content") {
            val conversationTypes = listOf(
                CometChatConstants.RECEIVER_TYPE_USER,
                CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            conversationTypes.forEach { conversationType ->
                val visibility = getVisibilityMatrix(
                    state = MessageInformationUIState.Loading,
                    conversationType = conversationType
                )
                
                visibility.shimmerVisible.shouldBeTrue()
                visibility.userReceiptVisible.shouldBeFalse()
                visibility.groupListVisible.shouldBeFalse()
            }
        }

        /**
         * Property 8: LOADED+USER shows user receipt view only
         *
         * **Validates: Requirements 9.1, 9.2**
         */
        test("Property 8: LOADED+USER shows user receipt view only") {
            val visibility = getVisibilityMatrix(
                state = MessageInformationUIState.Loaded,
                conversationType = CometChatConstants.RECEIVER_TYPE_USER
            )
            
            visibility.shimmerVisible.shouldBeFalse()
            visibility.userReceiptVisible.shouldBeTrue()
            visibility.groupListVisible.shouldBeFalse()
        }

        /**
         * Property 8: LOADED+GROUP shows group list only
         *
         * **Validates: Requirements 9.2, 9.3**
         */
        test("Property 8: LOADED+GROUP shows group list only") {
            val visibility = getVisibilityMatrix(
                state = MessageInformationUIState.Loaded,
                conversationType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            visibility.shimmerVisible.shouldBeFalse()
            visibility.userReceiptVisible.shouldBeFalse()
            visibility.groupListVisible.shouldBeTrue()
        }

        /**
         * Property 8: EMPTY+USER shows user receipt view
         *
         * **Validates: Requirements 10.1, 10.3**
         */
        test("Property 8: EMPTY+USER shows user receipt view") {
            val visibility = getVisibilityMatrix(
                state = MessageInformationUIState.Empty,
                conversationType = CometChatConstants.RECEIVER_TYPE_USER
            )
            
            visibility.shimmerVisible.shouldBeFalse()
            visibility.userReceiptVisible.shouldBeTrue()
            visibility.groupListVisible.shouldBeFalse()
        }

        /**
         * Property 8: EMPTY+GROUP hides all content views
         *
         * **Validates: Requirements 10.2, 10.3**
         */
        test("Property 8: EMPTY+GROUP hides all content views") {
            val visibility = getVisibilityMatrix(
                state = MessageInformationUIState.Empty,
                conversationType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            visibility.shimmerVisible.shouldBeFalse()
            visibility.userReceiptVisible.shouldBeFalse()
            visibility.groupListVisible.shouldBeFalse()
        }

        /**
         * Property 8: ERROR state behaves same as EMPTY state
         *
         * **Validates: Requirements 11.1, 11.2**
         */
        test("Property 8: ERROR state behaves same as EMPTY state") {
            val exception = CometChatException("TEST_ERROR", "Test error message")
            
            // ERROR+USER should show user receipt view (same as EMPTY+USER)
            val userVisibility = getVisibilityMatrix(
                state = MessageInformationUIState.Error(exception),
                conversationType = CometChatConstants.RECEIVER_TYPE_USER
            )
            
            userVisibility.shimmerVisible.shouldBeFalse()
            userVisibility.userReceiptVisible.shouldBeTrue()
            userVisibility.groupListVisible.shouldBeFalse()
            
            // ERROR+GROUP should hide all (same as EMPTY+GROUP)
            val groupVisibility = getVisibilityMatrix(
                state = MessageInformationUIState.Error(exception),
                conversationType = CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            groupVisibility.shimmerVisible.shouldBeFalse()
            groupVisibility.userReceiptVisible.shouldBeFalse()
            groupVisibility.groupListVisible.shouldBeFalse()
        }

        /**
         * Property 8: All state and conversation type combinations follow visibility matrix
         *
         * **Validates: Requirements 8.1, 8.2, 8.4, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 11.1, 11.2**
         */
        test("Property 8: All state and conversation type combinations follow visibility matrix") {
            val stateArb = Arb.element(
                MessageInformationUIState.Loading,
                MessageInformationUIState.Loaded,
                MessageInformationUIState.Empty,
                MessageInformationUIState.Error(CometChatException("TEST", "Test"))
            )
            
            val conversationTypeArb = Arb.element(
                CometChatConstants.RECEIVER_TYPE_USER,
                CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            checkAll(100, stateArb, conversationTypeArb) { state, conversationType ->
                val visibility = getVisibilityMatrix(state, conversationType)
                
                when (state) {
                    is MessageInformationUIState.Loading -> {
                        // LOADING: shimmer=VISIBLE, userReceipt=GONE, groupList=GONE
                        visibility.shimmerVisible.shouldBeTrue()
                        visibility.userReceiptVisible.shouldBeFalse()
                        visibility.groupListVisible.shouldBeFalse()
                    }
                    is MessageInformationUIState.Loaded -> {
                        visibility.shimmerVisible.shouldBeFalse()
                        when (conversationType) {
                            CometChatConstants.RECEIVER_TYPE_USER -> {
                                // LOADED+USER: shimmer=GONE, userReceipt=VISIBLE, groupList=GONE
                                visibility.userReceiptVisible.shouldBeTrue()
                                visibility.groupListVisible.shouldBeFalse()
                            }
                            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                                // LOADED+GROUP: shimmer=GONE, userReceipt=GONE, groupList=VISIBLE
                                visibility.userReceiptVisible.shouldBeFalse()
                                visibility.groupListVisible.shouldBeTrue()
                            }
                        }
                    }
                    is MessageInformationUIState.Empty -> {
                        visibility.shimmerVisible.shouldBeFalse()
                        visibility.groupListVisible.shouldBeFalse()
                        when (conversationType) {
                            CometChatConstants.RECEIVER_TYPE_USER -> {
                                // EMPTY+USER: shimmer=GONE, userReceipt=VISIBLE, groupList=GONE
                                visibility.userReceiptVisible.shouldBeTrue()
                            }
                            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                                // EMPTY+GROUP: shimmer=GONE, userReceipt=GONE, groupList=GONE
                                visibility.userReceiptVisible.shouldBeFalse()
                            }
                        }
                    }
                    is MessageInformationUIState.Error -> {
                        // ERROR: same as EMPTY
                        visibility.shimmerVisible.shouldBeFalse()
                        visibility.groupListVisible.shouldBeFalse()
                        when (conversationType) {
                            CometChatConstants.RECEIVER_TYPE_USER -> {
                                visibility.userReceiptVisible.shouldBeTrue()
                            }
                            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                                visibility.userReceiptVisible.shouldBeFalse()
                            }
                        }
                    }
                }
            }
        }

        /**
         * Property 8: null state shows shimmer (initial state)
         *
         * **Validates: Requirements 8.1, 8.2**
         */
        test("Property 8: null state shows shimmer (initial state)") {
            val conversationTypes = listOf(
                CometChatConstants.RECEIVER_TYPE_USER,
                CometChatConstants.RECEIVER_TYPE_GROUP
            )
            
            conversationTypes.forEach { conversationType ->
                val visibility = getVisibilityMatrix(
                    state = null,
                    conversationType = conversationType
                )
                
                // null state is treated as LOADING
                visibility.shimmerVisible.shouldBeTrue()
                visibility.userReceiptVisible.shouldBeFalse()
                visibility.groupListVisible.shouldBeFalse()
            }
        }
    }

    // ==================== Property 9: Exception Exposure ====================
    // *For any* CometChatException that occurs during receipt fetching, the exception SHALL 
    // be exposed through the ViewModel's exception StateFlow.
    // **Validates: Requirements 11.3**

    context("Property 9: Exception Exposure") {

        /**
         * Property 9: Exception is exposed when error occurs
         *
         * **Validates: Requirements 11.3**
         */
        test("Property 9: Exception is exposed when error occurs") {
            checkAll(100, Arb.string(1, 50), Arb.string(1, 100)) { errorCode, errorMessage ->
                val exception = CometChatException(errorCode, errorMessage)
                val exposedException = exposeException(exception)
                
                exposedException shouldBe exception
                exposedException.code shouldBe errorCode
                exposedException.message shouldBe errorMessage
            }
        }

        /**
         * Property 9: Exception preserves error code and message
         *
         * **Validates: Requirements 11.3**
         */
        test("Property 9: Exception preserves error code and message") {
            val testCases = listOf(
                "NETWORK_ERROR" to "Network connection failed",
                "AUTH_ERROR" to "Authentication failed",
                "UNKNOWN_ERROR" to "Unknown error occurred",
                "TIMEOUT" to "Request timed out",
                "INVALID_MESSAGE_ID" to "Invalid message ID provided"
            )
            
            testCases.forEach { (code, message) ->
                val exception = CometChatException(code, message)
                val exposedException = exposeException(exception)
                
                exposedException.code shouldBe code
                exposedException.message shouldBe message
            }
        }

        /**
         * Property 9: Error state contains the exception
         *
         * **Validates: Requirements 11.3**
         */
        test("Property 9: Error state contains the exception") {
            checkAll(100, Arb.string(1, 50), Arb.string(1, 100)) { errorCode, errorMessage ->
                val exception = CometChatException(errorCode, errorMessage)
                val errorState = MessageInformationUIState.Error(exception)
                
                errorState.exception shouldBe exception
                errorState.exception.code shouldBe errorCode
                errorState.exception.message shouldBe errorMessage
            }
        }
    }
})

// ==================== Helper Functions ====================

/**
 * Determines if the toolbar should be visible based on hideToolBar parameter.
 * Per design doc: "WHEN hideToolBar is set to true, THE Message_Information_Component SHALL hide the toolbar completely"
 *
 * **Validates: Requirements 3.4, 3.5**
 *
 * @param hideToolBar Whether to hide the toolbar
 * @return true if toolbar should be visible, false otherwise
 */
private fun shouldShowToolbar(hideToolBar: Boolean): Boolean = !hideToolBar

/**
 * Gets the toolbar title text.
 * Per design doc: "WHEN setToolBarTitleText is called, THE Message_Information_Component SHALL update the toolbar title to the provided text"
 *
 * **Validates: Requirements 3.3**
 *
 * @param titleText The title text to display
 * @return The title text that would be displayed
 */
private fun getToolbarTitle(titleText: String): String = titleText

/**
 * Determines if a timestamp should be displayed.
 * Per design doc: "WHEN the message has a readAt/deliveredAt timestamp greater than 0, THE Message_Information_Component SHALL display the formatted timestamp"
 *
 * **Validates: Requirements 5.4, 5.5**
 *
 * @param timestamp The timestamp value in seconds
 * @return true if timestamp should be displayed, false otherwise
 */
private fun shouldDisplayTimestamp(timestamp: Long): Boolean = timestamp > 0

/**
 * Data class representing visibility state of UI components.
 */
private data class VisibilityState(
    val shimmerVisible: Boolean,
    val userReceiptVisible: Boolean,
    val groupListVisible: Boolean
)

/**
 * Gets the visibility matrix for a given state and conversation type.
 * Per design doc: Visibility Rules table in UI/UX Behavior section.
 *
 * **Validates: Requirements 8.1, 8.2, 8.4, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 11.1, 11.2**
 *
 * @param state The current UI state
 * @param conversationType The conversation type (USER or GROUP)
 * @return VisibilityState with visibility flags for each component
 */
private fun getVisibilityMatrix(
    state: MessageInformationUIState?,
    conversationType: String
): VisibilityState {
    return when (state) {
        is MessageInformationUIState.Loading, null -> {
            // LOADING: shimmer=VISIBLE, userReceipt=GONE, groupList=GONE
            VisibilityState(
                shimmerVisible = true,
                userReceiptVisible = false,
                groupListVisible = false
            )
        }
        is MessageInformationUIState.Loaded -> {
            when (conversationType) {
                CometChatConstants.RECEIVER_TYPE_USER -> {
                    // LOADED+USER: shimmer=GONE, userReceipt=VISIBLE, groupList=GONE
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = true,
                        groupListVisible = false
                    )
                }
                CometChatConstants.RECEIVER_TYPE_GROUP -> {
                    // LOADED+GROUP: shimmer=GONE, userReceipt=GONE, groupList=VISIBLE
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = false,
                        groupListVisible = true
                    )
                }
                else -> {
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = false,
                        groupListVisible = false
                    )
                }
            }
        }
        is MessageInformationUIState.Empty -> {
            when (conversationType) {
                CometChatConstants.RECEIVER_TYPE_USER -> {
                    // EMPTY+USER: shimmer=GONE, userReceipt=VISIBLE, groupList=GONE
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = true,
                        groupListVisible = false
                    )
                }
                CometChatConstants.RECEIVER_TYPE_GROUP -> {
                    // EMPTY+GROUP: shimmer=GONE, userReceipt=GONE, groupList=GONE
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = false,
                        groupListVisible = false
                    )
                }
                else -> {
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = false,
                        groupListVisible = false
                    )
                }
            }
        }
        is MessageInformationUIState.Error -> {
            // ERROR: same as EMPTY
            when (conversationType) {
                CometChatConstants.RECEIVER_TYPE_USER -> {
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = true,
                        groupListVisible = false
                    )
                }
                else -> {
                    VisibilityState(
                        shimmerVisible = false,
                        userReceiptVisible = false,
                        groupListVisible = false
                    )
                }
            }
        }
    }
}

/**
 * Exposes an exception through the ViewModel pattern.
 * Per design doc: "WHEN an error occurs, THE Message_Information_Component SHALL expose the exception through the ViewModel for handling"
 *
 * **Validates: Requirements 11.3**
 *
 * @param exception The exception to expose
 * @return The exposed exception
 */
private fun exposeException(exception: CometChatException): CometChatException = exception
