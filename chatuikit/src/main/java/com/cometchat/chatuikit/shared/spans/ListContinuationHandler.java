package com.cometchat.chatuikit.shared.spans;

import android.text.Editable;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ListContinuationHandler manages list and blockquote continuation behavior.
 * <p>
 * This class handles the Enter key behavior for lists and blockquotes:
 * <ul>
 *   <li><b>Bullet Lists:</b> Creates new bullet item on Enter, exits list on Enter with empty item</li>
 *   <li><b>Numbered Lists:</b> Creates new numbered item with incremented number, exits on empty item</li>
 *   <li><b>Blockquotes:</b> Continues blockquote on single Enter, exits on double Enter</li>
 * </ul>
 * </p>
 * <p>
 * The handler also manages list renumbering when items are added, deleted, or reordered.
 * </p>
 * <p>
 * Validates: Requirements 6.3, 6.4, 7.3, 7.4, 7.5, 8.4, 8.5
 * </p>
 */
public class ListContinuationHandler {

    /**
     * Result of handling an Enter key press.
     */
    public enum EnterKeyResult {
        /**
         * A new list item was created.
         */
        NEW_ITEM_CREATED,
        
        /**
         * List mode was exited (empty item).
         */
        LIST_EXITED,
        
        /**
         * Blockquote was continued.
         */
        BLOCKQUOTE_CONTINUED,
        
        /**
         * Blockquote mode was exited (double Enter).
         */
        BLOCKQUOTE_EXITED,
        
        /**
         * No list/blockquote handling was needed.
         */
        NOT_HANDLED
    }

    /**
     * Creates a new ListContinuationHandler.
     */
    public ListContinuationHandler() {
        // Default constructor
    }

    /**
     * Handles Enter key press for list and blockquote continuation.
     * <p>
     * This method should be called when the user presses Enter within text
     * that may contain list or blockquote formatting.
     * </p>
     *
     * @param editable      The editable text.
     * @param cursorPosition The cursor position where Enter was pressed.
     * @return The result of handling the Enter key.
     */
    @NonNull
    public EnterKeyResult handleEnterKey(@Nullable Editable editable, int cursorPosition) {
        if (editable == null || cursorPosition < 0 || cursorPosition > editable.length()) {
            return EnterKeyResult.NOT_HANDLED;
        }

        // Check for bullet list at cursor position
        BulletListFormatSpan[] bulletSpans = editable.getSpans(
                cursorPosition, cursorPosition, BulletListFormatSpan.class);
        if (bulletSpans != null && bulletSpans.length > 0) {
            return handleBulletListEnter(editable, cursorPosition, bulletSpans[0]);
        }

        // Check for numbered list at cursor position
        NumberedListFormatSpan[] numberedSpans = editable.getSpans(
                cursorPosition, cursorPosition, NumberedListFormatSpan.class);
        if (numberedSpans != null && numberedSpans.length > 0) {
            return handleNumberedListEnter(editable, cursorPosition, numberedSpans[0]);
        }

        // Check for blockquote at cursor position
        BlockquoteFormatSpan[] blockquoteSpans = editable.getSpans(
                cursorPosition, cursorPosition, BlockquoteFormatSpan.class);
        if (blockquoteSpans != null && blockquoteSpans.length > 0) {
            return handleBlockquoteEnter(editable, cursorPosition, blockquoteSpans[0]);
        }

        return EnterKeyResult.NOT_HANDLED;
    }

    /**
     * Handles Enter key press within a bullet list.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If current line is empty, exit list mode</li>
     *   <li>Otherwise, create a new bullet item on the next line</li>
     * </ul>
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param span           The bullet list span.
     * @return The result of handling.
     */
    @NonNull
    private EnterKeyResult handleBulletListEnter(@NonNull Editable editable, 
                                                   int cursorPosition,
                                                   @NonNull BulletListFormatSpan span) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);

        if (spanStart < 0 || spanEnd < 0) {
            return EnterKeyResult.NOT_HANDLED;
        }

        // Find the current line boundaries
        int lineStart = findLineStart(editable, cursorPosition);
        int lineEnd = findLineEnd(editable, cursorPosition);

        // Check if current line is empty (only contains the span, no actual text)
        String lineContent = getLineContent(editable, lineStart, lineEnd);
        
        if (isLineEmpty(lineContent)) {
            // Exit list mode - remove the span from this line
            exitBulletList(editable, span, lineStart, lineEnd);
            return EnterKeyResult.LIST_EXITED;
        } else {
            // Create new bullet item
            createNewBulletItem(editable, cursorPosition, span);
            return EnterKeyResult.NEW_ITEM_CREATED;
        }
    }

    /**
     * Handles Enter key press within a numbered list.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If current line is empty, exit list mode</li>
     *   <li>Otherwise, create a new numbered item with incremented number</li>
     * </ul>
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param span           The numbered list span.
     * @return The result of handling.
     */
    @NonNull
    private EnterKeyResult handleNumberedListEnter(@NonNull Editable editable,
                                                     int cursorPosition,
                                                     @NonNull NumberedListFormatSpan span) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);

        if (spanStart < 0 || spanEnd < 0) {
            return EnterKeyResult.NOT_HANDLED;
        }

        // Find the current line boundaries
        int lineStart = findLineStart(editable, cursorPosition);
        int lineEnd = findLineEnd(editable, cursorPosition);

        // Check if current line is empty
        String lineContent = getLineContent(editable, lineStart, lineEnd);
        
        if (isLineEmpty(lineContent)) {
            // Exit list mode - remove the span from this line
            exitNumberedList(editable, span, lineStart, lineEnd);
            return EnterKeyResult.LIST_EXITED;
        } else {
            // Create new numbered item with incremented number
            createNewNumberedItem(editable, cursorPosition, span);
            return EnterKeyResult.NEW_ITEM_CREATED;
        }
    }

    /**
     * Handles Enter key press within a blockquote.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If previous character is newline (double Enter), exit blockquote mode</li>
     *   <li>Otherwise, continue blockquote on the next line</li>
     * </ul>
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param span           The blockquote span.
     * @return The result of handling.
     */
    @NonNull
    private EnterKeyResult handleBlockquoteEnter(@NonNull Editable editable,
                                                   int cursorPosition,
                                                   @NonNull BlockquoteFormatSpan span) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);

        if (spanStart < 0 || spanEnd < 0) {
            return EnterKeyResult.NOT_HANDLED;
        }

        // Check for double Enter (previous character is newline and current line is empty)
        boolean isDoubleEnter = isDoubleEnter(editable, cursorPosition);
        
        if (isDoubleEnter) {
            // Exit blockquote mode
            exitBlockquote(editable, span, cursorPosition);
            return EnterKeyResult.BLOCKQUOTE_EXITED;
        } else {
            // Continue blockquote
            continueBlockquote(editable, cursorPosition, span);
            return EnterKeyResult.BLOCKQUOTE_CONTINUED;
        }
    }

    // ==================== Bullet List Operations ====================

    /**
     * Creates a new bullet item after the current position.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param currentSpan    The current bullet list span.
     */
    private void createNewBulletItem(@NonNull Editable editable, 
                                      int cursorPosition,
                                      @NonNull BulletListFormatSpan currentSpan) {
        // Insert newline at cursor position
        editable.insert(cursorPosition, "\n");
        
        // Create new bullet span for the new line
        int newLineStart = cursorPosition + 1;
        int newLineEnd = findLineEnd(editable, newLineStart);
        
        if (newLineEnd < newLineStart) {
            newLineEnd = newLineStart;
        }
        
        // Create a new bullet span for the new line
        BulletListFormatSpan newSpan = new BulletListFormatSpan();
        newSpan.setBulletColor(currentSpan.getBulletColor());
        newSpan.setBulletRadius(currentSpan.getBulletRadius());
        newSpan.setGapWidth(currentSpan.getGapWidth());
        
        // Apply the span to the new line
        if (newLineStart <= editable.length()) {
            // If the new line is empty, we still need to set the span
            int spanEnd = Math.max(newLineStart, Math.min(newLineEnd, editable.length()));
            if (spanEnd > newLineStart) {
                editable.setSpan(newSpan, newLineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                // For empty new line, set span with zero length that will expand
                editable.setSpan(newSpan, newLineStart, newLineStart, Spanned.SPAN_MARK_MARK);
            }
        }
    }

    /**
     * Exits bullet list mode by removing the span from the current line.
     *
     * @param editable  The editable text.
     * @param span      The bullet list span to remove.
     * @param lineStart The start of the current line.
     * @param lineEnd   The end of the current line.
     */
    private void exitBulletList(@NonNull Editable editable,
                                 @NonNull BulletListFormatSpan span,
                                 int lineStart, int lineEnd) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        
        // Remove the span
        editable.removeSpan(span);
        
        // If the span covered more than just this line, we need to re-apply it
        // to the parts before and after this line
        if (spanStart < lineStart) {
            // Re-apply span to content before this line
            BulletListFormatSpan beforeSpan = new BulletListFormatSpan();
            beforeSpan.setBulletColor(span.getBulletColor());
            beforeSpan.setBulletRadius(span.getBulletRadius());
            beforeSpan.setGapWidth(span.getGapWidth());
            editable.setSpan(beforeSpan, spanStart, lineStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        
        if (spanEnd > lineEnd + 1 && lineEnd + 1 < editable.length()) {
            // Re-apply span to content after this line
            BulletListFormatSpan afterSpan = new BulletListFormatSpan();
            afterSpan.setBulletColor(span.getBulletColor());
            afterSpan.setBulletRadius(span.getBulletRadius());
            afterSpan.setGapWidth(span.getGapWidth());
            int afterStart = Math.min(lineEnd + 1, editable.length());
            int afterEnd = Math.min(spanEnd, editable.length());
            if (afterStart < afterEnd) {
                editable.setSpan(afterSpan, afterStart, afterEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }

    // ==================== Numbered List Operations ====================

    /**
     * Creates a new numbered item after the current position with incremented number.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param currentSpan    The current numbered list span.
     */
    private void createNewNumberedItem(@NonNull Editable editable,
                                        int cursorPosition,
                                        @NonNull NumberedListFormatSpan currentSpan) {
        // Insert newline at cursor position
        editable.insert(cursorPosition, "\n");
        
        // Create new numbered span for the new line with incremented number
        int newLineStart = cursorPosition + 1;
        int newLineEnd = findLineEnd(editable, newLineStart);
        
        if (newLineEnd < newLineStart) {
            newLineEnd = newLineStart;
        }
        
        // Create a new numbered span with incremented number
        int nextNumber = currentSpan.getNumber() + 1;
        NumberedListFormatSpan newSpan = new NumberedListFormatSpan(nextNumber);
        newSpan.setTextColor(currentSpan.getTextColor());
        newSpan.setGapWidth(currentSpan.getGapWidth());
        
        // Apply the span to the new line
        if (newLineStart <= editable.length()) {
            int spanEnd = Math.max(newLineStart, Math.min(newLineEnd, editable.length()));
            if (spanEnd > newLineStart) {
                editable.setSpan(newSpan, newLineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                editable.setSpan(newSpan, newLineStart, newLineStart, Spanned.SPAN_MARK_MARK);
            }
        }
    }

    /**
     * Exits numbered list mode by removing the span from the current line.
     *
     * @param editable  The editable text.
     * @param span      The numbered list span to remove.
     * @param lineStart The start of the current line.
     * @param lineEnd   The end of the current line.
     */
    private void exitNumberedList(@NonNull Editable editable,
                                   @NonNull NumberedListFormatSpan span,
                                   int lineStart, int lineEnd) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        
        // Remove the span
        editable.removeSpan(span);
        
        // If the span covered more than just this line, we need to re-apply it
        if (spanStart < lineStart) {
            NumberedListFormatSpan beforeSpan = new NumberedListFormatSpan(span.getNumber());
            beforeSpan.setTextColor(span.getTextColor());
            beforeSpan.setGapWidth(span.getGapWidth());
            editable.setSpan(beforeSpan, spanStart, lineStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        
        if (spanEnd > lineEnd + 1 && lineEnd + 1 < editable.length()) {
            int afterStart = Math.min(lineEnd + 1, editable.length());
            int afterEnd = Math.min(spanEnd, editable.length());
            if (afterStart < afterEnd) {
                // Note: The number should be recalculated based on position
                NumberedListFormatSpan afterSpan = new NumberedListFormatSpan(span.getNumber());
                afterSpan.setTextColor(span.getTextColor());
                afterSpan.setGapWidth(span.getGapWidth());
                editable.setSpan(afterSpan, afterStart, afterEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        
        // Renumber remaining items
        renumberList(editable);
    }

    // ==================== Blockquote Operations ====================

    /**
     * Continues the blockquote on the next line.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @param currentSpan    The current blockquote span.
     */
    private void continueBlockquote(@NonNull Editable editable,
                                     int cursorPosition,
                                     @NonNull BlockquoteFormatSpan currentSpan) {
        // Insert newline at cursor position
        editable.insert(cursorPosition, "\n");
        
        // The blockquote span should automatically extend to include the new line
        // if it's set with SPAN_INCLUSIVE_INCLUSIVE flags
        int spanEnd = editable.getSpanEnd(currentSpan);
        int newPosition = cursorPosition + 1;
        
        // If the span didn't extend, manually extend it
        if (spanEnd < newPosition) {
            int spanStart = editable.getSpanStart(currentSpan);
            int spanFlags = editable.getSpanFlags(currentSpan);
            editable.removeSpan(currentSpan);
            editable.setSpan(currentSpan, spanStart, newPosition, spanFlags);
        }
    }

    /**
     * Exits blockquote mode on double Enter.
     *
     * @param editable       The editable text.
     * @param span           The blockquote span.
     * @param cursorPosition The cursor position.
     */
    private void exitBlockquote(@NonNull Editable editable,
                                 @NonNull BlockquoteFormatSpan span,
                                 int cursorPosition) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        
        // Find the position of the previous newline (the first Enter)
        int prevNewline = cursorPosition - 1;
        while (prevNewline > spanStart && editable.charAt(prevNewline) != '\n') {
            prevNewline--;
        }
        
        // Remove the span
        editable.removeSpan(span);
        
        // Re-apply span to content before the double Enter
        if (prevNewline > spanStart) {
            BlockquoteFormatSpan beforeSpan = new BlockquoteFormatSpan();
            beforeSpan.setStripeColor(span.getStripeColor());
            beforeSpan.setStripeWidth(span.getStripeWidth());
            beforeSpan.setGapWidth(span.getGapWidth());
            beforeSpan.setBackgroundColor(span.getBackgroundColor());
            editable.setSpan(beforeSpan, spanStart, prevNewline, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        
        // Delete the empty line (the newline that triggered the exit)
        // This removes the extra newline from the double Enter
        if (cursorPosition > 0 && cursorPosition <= editable.length()) {
            // Find the start of the empty line
            int emptyLineStart = prevNewline;
            if (emptyLineStart >= 0 && emptyLineStart < editable.length()) {
                // Delete from the newline to the cursor
                int deleteStart = Math.max(0, emptyLineStart);
                int deleteEnd = Math.min(cursorPosition, editable.length());
                if (deleteEnd > deleteStart) {
                    editable.delete(deleteStart, deleteEnd);
                }
            }
        }
    }

    // ==================== List Renumbering ====================

    /**
     * Renumbers all numbered list items in the editable.
     * <p>
     * This method should be called after items are added, deleted, or reordered
     * to ensure sequential numbering.
     * </p>
     *
     * @param editable The editable text.
     */
    public void renumberList(@Nullable Editable editable) {
        if (editable == null) {
            return;
        }

        // Get all numbered list spans
        NumberedListFormatSpan[] spans = editable.getSpans(
                0, editable.length(), NumberedListFormatSpan.class);
        
        if (spans == null || spans.length == 0) {
            return;
        }

        // Sort spans by their start position
        java.util.Arrays.sort(spans, (a, b) -> {
            int startA = editable.getSpanStart(a);
            int startB = editable.getSpanStart(b);
            return Integer.compare(startA, startB);
        });

        // Group spans by contiguous list sections
        // A new list starts when there's a gap between spans
        int currentNumber = 1;
        int previousEnd = -1;
        
        for (NumberedListFormatSpan span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            
            if (spanStart < 0 || spanEnd < 0) {
                continue;
            }
            
            // Check if this is a new list (gap from previous span)
            if (previousEnd >= 0 && spanStart > previousEnd + 1) {
                // Check if there's non-list content between spans
                boolean hasGap = false;
                for (int i = previousEnd; i < spanStart; i++) {
                    if (i < editable.length() && editable.charAt(i) != '\n') {
                        hasGap = true;
                        break;
                    }
                }
                if (hasGap) {
                    currentNumber = 1; // Reset numbering for new list
                }
            }
            
            // Update the span's number if different
            if (span.getNumber() != currentNumber) {
                span.setNumber(currentNumber);
            }
            
            currentNumber++;
            previousEnd = spanEnd;
        }
    }

    /**
     * Renumbers a specific range of numbered list items.
     *
     * @param editable   The editable text.
     * @param startIndex The start index of the range.
     * @param endIndex   The end index of the range.
     * @param startNumber The starting number for renumbering.
     */
    public void renumberListRange(@Nullable Editable editable, 
                                   int startIndex, int endIndex, int startNumber) {
        if (editable == null || startIndex < 0 || endIndex > editable.length()) {
            return;
        }

        NumberedListFormatSpan[] spans = editable.getSpans(
                startIndex, endIndex, NumberedListFormatSpan.class);
        
        if (spans == null || spans.length == 0) {
            return;
        }

        // Sort spans by position
        java.util.Arrays.sort(spans, (a, b) -> {
            int startA = editable.getSpanStart(a);
            int startB = editable.getSpanStart(b);
            return Integer.compare(startA, startB);
        });

        int currentNumber = startNumber;
        for (NumberedListFormatSpan span : spans) {
            span.setNumber(currentNumber);
            currentNumber++;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Finds the start of the line containing the given position.
     *
     * @param editable The editable text.
     * @param position The position within the line.
     * @return The start index of the line.
     */
    private int findLineStart(@NonNull CharSequence editable, int position) {
        if (position <= 0) {
            return 0;
        }
        
        int lineStart = position - 1;
        while (lineStart > 0 && editable.charAt(lineStart) != '\n') {
            lineStart--;
        }
        
        // If we found a newline, the line starts after it
        if (lineStart > 0 || (lineStart == 0 && editable.charAt(0) == '\n')) {
            lineStart++;
        }
        
        return Math.max(0, lineStart);
    }

    /**
     * Finds the end of the line containing the given position.
     *
     * @param editable The editable text.
     * @param position The position within the line.
     * @return The end index of the line (exclusive, points to newline or end of text).
     */
    private int findLineEnd(@NonNull CharSequence editable, int position) {
        int length = editable.length();
        if (position >= length) {
            return length;
        }
        
        int lineEnd = position;
        while (lineEnd < length && editable.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        
        return lineEnd;
    }

    /**
     * Gets the content of a line.
     *
     * @param editable  The editable text.
     * @param lineStart The start of the line.
     * @param lineEnd   The end of the line.
     * @return The line content as a string.
     */
    @NonNull
    private String getLineContent(@NonNull CharSequence editable, int lineStart, int lineEnd) {
        if (lineStart >= lineEnd || lineStart < 0 || lineEnd > editable.length()) {
            return "";
        }
        return editable.subSequence(lineStart, lineEnd).toString();
    }

    /**
     * Checks if a line is empty (contains only whitespace).
     *
     * @param lineContent The line content.
     * @return true if the line is empty or contains only whitespace.
     */
    private boolean isLineEmpty(@Nullable String lineContent) {
        return lineContent == null || lineContent.trim().isEmpty();
    }

    /**
     * Checks if the current position represents a double Enter (empty line after newline).
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @return true if this is a double Enter situation.
     */
    private boolean isDoubleEnter(@NonNull CharSequence editable, int cursorPosition) {
        if (cursorPosition <= 0) {
            return false;
        }
        
        // Check if previous character is newline
        if (editable.charAt(cursorPosition - 1) != '\n') {
            return false;
        }
        
        // Check if current line is empty
        int lineStart = cursorPosition;
        int lineEnd = findLineEnd(editable, cursorPosition);
        
        String lineContent = "";
        if (lineEnd > lineStart && lineStart < editable.length()) {
            lineContent = editable.subSequence(lineStart, Math.min(lineEnd, editable.length())).toString();
        }
        
        return isLineEmpty(lineContent);
    }

    /**
     * Gets the next number for a numbered list item.
     * <p>
     * This method finds the highest number in the current list and returns
     * the next sequential number.
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @return The next number for the list.
     */
    public int getNextListNumber(@Nullable Editable editable, int cursorPosition) {
        if (editable == null) {
            return 1;
        }

        // Find the line containing the cursor
        int lineStart = findLineStart(editable, cursorPosition);
        
        // Get all numbered list spans before this position
        NumberedListFormatSpan[] spans = editable.getSpans(0, lineStart, NumberedListFormatSpan.class);
        
        if (spans == null || spans.length == 0) {
            return 1;
        }

        // Find the highest number
        int maxNumber = 0;
        for (NumberedListFormatSpan span : spans) {
            if (span.getNumber() > maxNumber) {
                maxNumber = span.getNumber();
            }
        }

        return maxNumber + 1;
    }

    /**
     * Checks if the cursor is at the start of a list item.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @return true if cursor is at the start of a list item.
     */
    public boolean isAtListItemStart(@Nullable Editable editable, int cursorPosition) {
        if (editable == null || cursorPosition < 0) {
            return false;
        }

        int lineStart = findLineStart(editable, cursorPosition);
        return cursorPosition == lineStart;
    }

    /**
     * Checks if the cursor is within a list.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @return true if cursor is within a bullet or numbered list.
     */
    public boolean isInList(@Nullable Editable editable, int cursorPosition) {
        if (editable == null || cursorPosition < 0 || cursorPosition > editable.length()) {
            return false;
        }

        BulletListFormatSpan[] bulletSpans = editable.getSpans(
                cursorPosition, cursorPosition, BulletListFormatSpan.class);
        if (bulletSpans != null && bulletSpans.length > 0) {
            return true;
        }

        NumberedListFormatSpan[] numberedSpans = editable.getSpans(
                cursorPosition, cursorPosition, NumberedListFormatSpan.class);
        return numberedSpans != null && numberedSpans.length > 0;
    }

    /**
     * Checks if the cursor is within a blockquote.
     *
     * @param editable       The editable text.
     * @param cursorPosition The cursor position.
     * @return true if cursor is within a blockquote.
     */
    public boolean isInBlockquote(@Nullable Editable editable, int cursorPosition) {
        if (editable == null || cursorPosition < 0 || cursorPosition > editable.length()) {
            return false;
        }

        BlockquoteFormatSpan[] spans = editable.getSpans(
                cursorPosition, cursorPosition, BlockquoteFormatSpan.class);
        return spans != null && spans.length > 0;
    }
}
