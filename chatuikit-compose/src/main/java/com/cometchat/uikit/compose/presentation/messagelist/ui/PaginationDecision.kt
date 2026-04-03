package com.cometchat.uikit.compose.presentation.messagelist.ui

/**
 * Represents the result of a pagination decision.
 *
 * This sealed class encapsulates the possible outcomes when evaluating
 * whether pagination should be triggered based on scroll state.
 */
sealed class PaginationDecision {
    /**
     * Indicates that older messages should be fetched.
     * This occurs when the user scrolls near the top of the message list.
     */
    data object FetchOlderMessages : PaginationDecision()
    
    /**
     * Indicates that newer messages should be fetched.
     * This occurs when the user scrolls near the bottom of the message list
     * after having scrolled up.
     */
    data object FetchNewerMessages : PaginationDecision()
    
    /**
     * Indicates that no pagination action should be taken.
     * This occurs when guard conditions prevent fetching or when
     * the scroll position doesn't meet the threshold requirements.
     */
    data object NoAction : PaginationDecision()
}

/**
 * The threshold number of items from the edge at which pagination should trigger.
 * When the user scrolls within this many items of the top or bottom,
 * pagination will be triggered if other conditions are met.
 */
const val PAGINATION_THRESHOLD = 5

/**
 * Determines the pagination action to take based on the current scroll state.
 *
 * This pure function encapsulates the bidirectional pagination decision logic
 * used in the MessageList component. It evaluates the scroll position and
 * various state flags to determine whether to fetch older messages, newer
 * messages, or take no action.
 *
 * With reverseLayout=true in LazyColumn:
 * - Index 0 = newest message (at bottom visually)
 * - Last index = oldest message (at top visually)
 * - Scrolling UP increases lastVisibleIndex toward totalItems-1
 * - Scrolling DOWN decreases firstVisibleIndex toward 0
 *
 * Priority: When both conditions are met simultaneously (near top AND near bottom
 * with few messages), older messages are fetched first.
 *
 * @param totalItems The total number of items in the list
 * @param firstVisibleIndex The index of the first visible item (closest to bottom with reverseLayout=true)
 * @param lastVisibleIndex The index of the last visible item (closest to top with reverseLayout=true)
 * @param hasMoreNewMessages Whether more newer messages are available to fetch
 * @param hasMorePreviousMessages Whether more older messages are available to fetch
 * @param isInProgress Whether a fetch operation is currently in progress
 * @return The pagination decision indicating what action to take
 */
fun determinePaginationAction(
    totalItems: Int,
    firstVisibleIndex: Int,
    lastVisibleIndex: Int,
    hasMoreNewMessages: Boolean,
    hasMorePreviousMessages: Boolean,
    isInProgress: Boolean
): PaginationDecision {
    // Guard: No action if a fetch is already in progress
    if (isInProgress) {
        return PaginationDecision.NoAction
    }
    
    // Guard: No action if the list is empty
    if (totalItems <= 0) {
        return PaginationDecision.NoAction
    }
    
    // Check if near the top (older messages direction)
    // With reverseLayout=true: lastVisibleIndex approaches totalItems-1 when scrolling up
    val nearTop = lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD
    
    // Check if near the bottom (newer messages direction)
    // With reverseLayout=true: firstVisibleIndex approaches 0 when scrolling down
    val nearBottom = firstVisibleIndex <= PAGINATION_THRESHOLD
    
    // Priority: Fetch older messages first when both conditions are met
    return when {
        nearTop && hasMorePreviousMessages -> PaginationDecision.FetchOlderMessages
        nearBottom && hasMoreNewMessages -> PaginationDecision.FetchNewerMessages
        else -> PaginationDecision.NoAction
    }
}

/**
 * Determines if newer message pagination should be triggered.
 *
 * This is a convenience function that specifically checks the conditions
 * for fetching newer messages. It returns true when:
 * - firstVisibleIndex <= PAGINATION_THRESHOLD (near bottom)
 * - hasMoreNewMessages is true
 * - isInProgress is false
 * - totalItems > 0
 *
 * Note: This function does NOT consider the priority rule where older
 * messages take precedence. Use [determinePaginationAction] for the
 * complete decision logic with priority handling.
 *
 * @param totalItems The total number of items in the list
 * @param firstVisibleIndex The index of the first visible item
 * @param hasMoreNewMessages Whether more newer messages are available
 * @param isInProgress Whether a fetch operation is currently in progress
 * @return True if newer message pagination should be triggered
 */
fun shouldFetchNewerMessages(
    totalItems: Int,
    firstVisibleIndex: Int,
    hasMoreNewMessages: Boolean,
    isInProgress: Boolean
): Boolean {
    // Guard conditions
    if (isInProgress) return false
    if (totalItems <= 0) return false
    if (!hasMoreNewMessages) return false
    
    // Threshold condition: near bottom with reverseLayout=true
    return firstVisibleIndex <= PAGINATION_THRESHOLD
}


/**
 * Determines if older message pagination should be triggered.
 *
 * This is a convenience function that specifically checks the conditions
 * for fetching older messages. It returns true when:
 * - lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD (near top)
 * - hasMorePreviousMessages is true
 * - isInProgress is false
 * - totalItems > 0
 *
 * Note: This function does NOT consider the priority rule where older
 * messages take precedence over newer messages. Use [determinePaginationAction]
 * for the complete decision logic with priority handling.
 *
 * @param totalItems The total number of items in the list
 * @param lastVisibleIndex The index of the last visible item (closest to top with reverseLayout=true)
 * @param hasMorePreviousMessages Whether more older messages are available
 * @param isInProgress Whether a fetch operation is currently in progress
 * @return True if older message pagination should be triggered
 */
fun shouldFetchOlderMessages(
    totalItems: Int,
    lastVisibleIndex: Int,
    hasMorePreviousMessages: Boolean,
    isInProgress: Boolean
): Boolean {
    // Guard conditions
    if (isInProgress) return false
    if (totalItems <= 0) return false
    if (!hasMorePreviousMessages) return false

    // Threshold condition: near top with reverseLayout=true
    // lastVisibleIndex approaches totalItems-1 when scrolling up
    return lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD
}

