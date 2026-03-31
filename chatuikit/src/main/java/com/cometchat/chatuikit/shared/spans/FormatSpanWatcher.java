package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.text.Editable;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.Spanned;

import androidx.annotation.Nullable;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import java.util.EnumSet;
import java.util.Set;

/**
 * FormatSpanWatcher manages format span boundaries during text changes.
 * <p>
 * This class implements {@link SpanWatcher} to automatically handle span extension,
 * shrinking, and splitting when text is inserted or deleted within formatted regions.
 * It should be attached to an {@link Editable} to enable automatic span management.
 * </p>
 * <p>
 * Key behaviors:
 * <ul>
 *   <li><b>Span Extension:</b> When text is inserted within a formatted region,
 *       the span is extended to include the new text.</li>
 *   <li><b>Span Shrinking:</b> When text is deleted from a formatted region,
 *       the span boundaries shrink accordingly. If all content is deleted,
 *       the span is removed.</li>
 *   <li><b>Span Splitting:</b> When a newline is inserted within a span,
 *       the span is split into two spans maintaining the format.</li>
 *   <li><b>Pending Formats:</b> When a format is toggled with no selection,
 *       it is stored as a "pending format" and applied to newly typed text.</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * <pre>
 * Editable editable = editText.getText();
 * FormatSpanWatcher watcher = new FormatSpanWatcher();
 * editable.setSpan(watcher, 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
 * </pre>
 * </p>
 * <p>
 * Validates: Requirements 3.6, 4.5, 9.1, 9.2, 9.3, 9.5
 * </p>
 */
public class FormatSpanWatcher implements SpanWatcher {

    /**
     * Flag to prevent recursive span modifications.
     */
    private boolean isModifying = false;

    /**
     * Pending formats to apply to newly typed text.
     * <p>
     * When a format button is clicked with no text selected, the format is added
     * to this set. When the user types new text, these formats are applied to
     * the new characters.
     * </p>
     */
    private final Set<FormatType> pendingFormats = EnumSet.noneOf(FormatType.class);

    /**
     * Explicitly disabled formats.
     * <p>
     * When a user clicks a format button to turn OFF a format while the cursor
     * is inside formatted text, the format is added to this set. This prevents
     * the format from being re-detected from the existing span and shown as active.
     * </p>
     * <p>
     * This set is cleared when the cursor moves to a new position.
     * </p>
     */
    private final Set<FormatType> explicitlyDisabledFormats = EnumSet.noneOf(FormatType.class);

    /**
     * Context for creating theme-aware spans.
     * <p>
     * Used when creating InlineCodeFormatSpan and other spans that need
     * access to theme colors.
     * </p>
     */
    @Nullable
    private Context context;

    /**
     * Creates a new FormatSpanWatcher.
     */
    public FormatSpanWatcher() {
        // Default constructor
        this.context = null;
    }

    /**
     * Creates a new FormatSpanWatcher with context for theme-aware spans.
     *
     * @param context The context used for creating theme-aware spans like InlineCodeFormatSpan.
     */
    public FormatSpanWatcher(@Nullable Context context) {
        this.context = context;
    }

    /**
     * Sets the context for creating theme-aware spans.
     *
     * @param context The context to use.
     */
    public void setContext(@Nullable Context context) {
        this.context = context;
    }

    /**
     * Gets the current pending formats.
     * <p>
     * Pending formats are formats that will be applied to newly typed text.
     * </p>
     *
     * @return A copy of the pending formats set.
     */
    public Set<FormatType> getPendingFormats() {
        return EnumSet.copyOf(pendingFormats);
    }

    /**
     * Toggles a pending format.
     * <p>
     * If the format is already pending, it is removed and added to explicitly disabled.
     * If the format is explicitly disabled, it is removed from disabled and added to pending.
     * Otherwise, it is added to pending.
     * This is used when a format button is clicked with no text selected.
     * </p>
     *
     * @param formatType The format type to toggle.
     * @return true if the format is now pending (active), false if it was removed/disabled.
     */
    public boolean togglePendingFormat(FormatType formatType) {
        if (formatType == null) {
            return false;
        }
        if (pendingFormats.contains(formatType)) {
            // Was pending (active) - now disable it
            pendingFormats.remove(formatType);
            explicitlyDisabledFormats.add(formatType);
            return false;
        } else if (explicitlyDisabledFormats.contains(formatType)) {
            // Was explicitly disabled - now enable it
            explicitlyDisabledFormats.remove(formatType);
            pendingFormats.add(formatType);
            return true;
        } else {
            // Not in either set - add to pending (enable it)
            pendingFormats.add(formatType);
            return true;
        }
    }

    /**
     * Checks if a format is pending.
     *
     * @param formatType The format type to check.
     * @return true if the format is pending, false otherwise.
     */
    public boolean isPendingFormat(FormatType formatType) {
        return formatType != null && pendingFormats.contains(formatType);
    }

    /**
     * Checks if a format is explicitly disabled.
     * <p>
     * A format is explicitly disabled when the user clicks the format button
     * to turn it off while the cursor is inside formatted text.
     * </p>
     *
     * @param formatType The format type to check.
     * @return true if the format is explicitly disabled, false otherwise.
     */
    public boolean isExplicitlyDisabled(FormatType formatType) {
        return formatType != null && explicitlyDisabledFormats.contains(formatType);
    }

    /**
     * Gets the explicitly disabled formats.
     *
     * @return A copy of the explicitly disabled formats set.
     */
    public Set<FormatType> getExplicitlyDisabledFormats() {
        return EnumSet.copyOf(explicitlyDisabledFormats);
    }

    /**
     * Clears all pending formats and explicitly disabled formats.
     * <p>
     * This should be called when the cursor moves to a new position,
     * as the pending/disabled state is only relevant at the original position.
     * </p>
     */
    public void clearPendingFormats() {
        pendingFormats.clear();
        explicitlyDisabledFormats.clear();
    }

    /**
     * Sets the pending formats from a set.
     * <p>
     * This also clears the explicitly disabled formats.
     * </p>
     *
     * @param formats The formats to set as pending.
     */
    public void setPendingFormats(Set<FormatType> formats) {
        pendingFormats.clear();
        explicitlyDisabledFormats.clear();
        if (formats != null) {
            pendingFormats.addAll(formats);
        }
    }

    /**
     * Marks a format as explicitly disabled.
     * <p>
     * This is used when the user clicks a format button to turn off a format
     * while the cursor is inside formatted text. The format will not be shown
     * as active even though a span exists at the cursor position.
     * </p>
     * <p>
     * For CODE_BLOCK format, this also changes the span flags from INCLUSIVE_INCLUSIVE
     * to EXCLUSIVE_EXCLUSIVE to prevent the span from auto-extending when the user
     * types at the boundary. This is necessary because Android's automatic span
     * extension happens before our handleTextInserted method is called.
     * </p>
     *
     * @param formatType The format type to disable.
     */
    public void disableFormat(FormatType formatType) {
        if (formatType != null) {
            pendingFormats.remove(formatType);
            explicitlyDisabledFormats.add(formatType);
        }
    }

    /**
     * Marks a format as explicitly disabled and updates span flags to prevent auto-extension.
     * <p>
     * This is the preferred method to call when disabling a format, as it also
     * updates the span flags for CODE_BLOCK spans to prevent Android's automatic
     * span extension.
     * </p>
     *
     * @param editable   The editable text containing the spans.
     * @param formatType The format type to disable.
     * @param cursorPos  The current cursor position.
     */
    public void disableFormatWithSpanUpdate(Editable editable, FormatType formatType, int cursorPos) {
        if (formatType == null) {
            return;
        }
        
        pendingFormats.remove(formatType);
        explicitlyDisabledFormats.add(formatType);
        
        // Change span flags to EXCLUSIVE_EXCLUSIVE to prevent auto-extension when
        // new text is typed at the span boundary. This applies to ALL format types
        // (inline styles like BOLD/ITALIC/UNDERLINE/STRIKETHROUGH as well as
        // block-level formats like CODE_BLOCK/BLOCKQUOTE/INLINE_CODE).
        // Without this, Android's SpannableStringBuilder auto-extends
        // INCLUSIVE_INCLUSIVE spans to cover newly inserted text, which causes
        // issues like mention formatting being broken when mentions are inserted
        // after disabling inline text style formats.
        if (editable != null) {
            updateBlockSpanFlagsAtPosition(editable, cursorPos, false, formatType);
        }
    }

    /**
     * Updates span flags at the cursor position for the given format type.
     * <p>
     * When inclusive is true, uses SPAN_INCLUSIVE_INCLUSIVE (span auto-extends).
     * When inclusive is false, uses SPAN_EXCLUSIVE_EXCLUSIVE (span doesn't auto-extend).
     * </p>
     * <p>
     * This method works for all format types (inline styles like BOLD/ITALIC as well
     * as block-level formats like CODE_BLOCK/BLOCKQUOTE). For CODE_BLOCK, it also
     * updates associated LeadingMarginSpan instances.
     * </p>
     *
     * @param editable     The editable text.
     * @param position     The cursor position.
     * @param inclusive    Whether to use inclusive flags.
     * @param targetFormat The format type whose spans should be updated.
     */
    private void updateBlockSpanFlagsAtPosition(Editable editable, int position, boolean inclusive, FormatType targetFormat) {
        if (editable == null || position < 0 || position > editable.length()) {
            return;
        }
        
        RichTextFormatSpan[] spans = editable.getSpans(0, editable.length(), RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            if (span.getFormatType() != targetFormat) {
                continue;
            }
            
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            
            // Check if cursor is at or near the span boundary
            if (spanStart <= position && position <= spanEnd) {
                int newFlags = inclusive 
                    ? Spanned.SPAN_INCLUSIVE_INCLUSIVE 
                    : Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                
                // Only update if flags are different
                int currentFlags = editable.getSpanFlags(span);
                if ((currentFlags & Spanned.SPAN_INCLUSIVE_INCLUSIVE) != (newFlags & Spanned.SPAN_INCLUSIVE_INCLUSIVE)) {
                    editable.removeSpan(span);
                    editable.setSpan(span, spanStart, spanEnd, newFlags);
                    
                    // Also update associated LeadingMarginSpan for code blocks
                    if (targetFormat == FormatType.CODE_BLOCK) {
                        android.text.style.LeadingMarginSpan.Standard[] marginSpans = 
                            editable.getSpans(spanStart, spanEnd, android.text.style.LeadingMarginSpan.Standard.class);
                        for (android.text.style.LeadingMarginSpan.Standard marginSpan : marginSpans) {
                            if (marginSpan instanceof BlockquoteFormatSpan) {
                                continue; // Don't modify blockquote spans
                            }
                            int marginStart = editable.getSpanStart(marginSpan);
                            int marginEnd = editable.getSpanEnd(marginSpan);
                            if (marginStart == spanStart && marginEnd == spanEnd) {
                                editable.removeSpan(marginSpan);
                                editable.setSpan(marginSpan, marginStart, marginEnd, newFlags);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables a format (removes from explicitly disabled and adds to pending).
     *
     * @param formatType The format type to enable.
     */
    public void enableFormat(FormatType formatType) {
        if (formatType != null) {
            explicitlyDisabledFormats.remove(formatType);
            pendingFormats.add(formatType);
        }
    }

    /**
     * Clears the explicitly disabled state for a format without adding to pending.
     * <p>
     * Use this when a format is being applied directly via a span (not via pending),
     * to ensure the toolbar shows the correct state.
     * </p>
     *
     * @param formatType The format type to clear from explicitly disabled.
     */
    public void clearExplicitlyDisabled(FormatType formatType) {
        if (formatType != null) {
            explicitlyDisabledFormats.remove(formatType);
        }
    }

    /**
     * Enables a format and updates span flags to allow auto-extension.
     * <p>
     * This is the preferred method to call when enabling a format, as it also
     * updates the span flags for CODE_BLOCK spans to allow Android's automatic
     * span extension.
     * </p>
     *
     * @param editable   The editable text containing the spans.
     * @param formatType The format type to enable.
     * @param cursorPos  The current cursor position.
     */
    public void enableFormatWithSpanUpdate(Editable editable, FormatType formatType, int cursorPos) {
        if (formatType == null) {
            return;
        }
        
        explicitlyDisabledFormats.remove(formatType);
        pendingFormats.add(formatType);
        
        // Change span flags to INCLUSIVE_INCLUSIVE to allow auto-extension when
        // new text is typed at the span boundary. This applies to ALL format types
        // to match the symmetric behavior of disableFormatWithSpanUpdate.
        if (editable != null) {
            updateBlockSpanFlagsAtPosition(editable, cursorPos, true, formatType);
        }
    }

    /**
     * Called when a span is added to the text.
     * <p>
     * This method is called by the framework when any span is added.
     * We don't need to do anything special here as we handle span
     * management through text change callbacks.
     * </p>
     *
     * @param text  The text to which the span was added.
     * @param what  The span that was added.
     * @param start The start position of the span.
     * @param end   The end position of the span.
     */
    @Override
    public void onSpanAdded(Spannable text, Object what, int start, int end) {
        // No action needed when spans are added
    }

    /**
     * Called when a span is removed from the text.
     * <p>
     * This method is called by the framework when any span is removed.
     * We don't need to do anything special here.
     * </p>
     *
     * @param text  The text from which the span was removed.
     * @param what  The span that was removed.
     * @param start The start position where the span was.
     * @param end   The end position where the span was.
     */
    @Override
    public void onSpanRemoved(Spannable text, Object what, int start, int end) {
        // No action needed when spans are removed
    }

    /**
     * Called when a span's boundaries change.
     * <p>
     * This method is called by the framework when a span's start or end
     * position changes. We use this to detect when spans need to be
     * split on newline insertion.
     * </p>
     *
     * @param text     The text containing the span.
     * @param what     The span whose boundaries changed.
     * @param ostart   The old start position.
     * @param oend     The old end position.
     * @param nstart   The new start position.
     * @param nend     The new end position.
     */
    @Override
    public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        // Handle span changes if needed
        // The main span management is done in handleTextChanged
    }

    /**
     * Handles text changes to manage span extension, shrinking, and splitting.
     * <p>
     * This method should be called from a TextWatcher's afterTextChanged method
     * to properly manage format spans when text is modified.
     * </p>
     * <p>
     * Also applies pending formats to newly typed text when there are pending
     * formats set (from clicking format buttons with no selection).
     * </p>
     *
     * @param editable The editable text that was changed.
     * @param start    The start position of the change.
     * @param before   The length of text that was removed.
     * @param count    The length of text that was inserted.
     */
    public void handleTextChanged(Editable editable, int start, int before, int count) {
        if (isModifying || editable == null) {
            return;
        }

        isModifying = true;
        try {
            if (count > before) {
                // Text was inserted
                int insertedLength = count - before;
                
                // Check if emoji was inserted inside code block or inline code
                // If so, convert it to shortcode representation (Slack-style)
                int end = start + insertedLength;
                if (end <= editable.length()) {
                    CharSequence insertedText = editable.subSequence(start, end);
                    if (isEntirelyEmoji(insertedText) && isCodeFormatActive(editable, start)) {
                        String shortcodeText = convertEmojiToShortcode(insertedText);
                        editable.replace(start, end, shortcodeText);
                        // Update insertedLength for the new text
                        insertedLength = shortcodeText.length();
                    }
                }
                
                handleTextInserted(editable, start, insertedLength);
                
                // Apply pending formats to newly typed text
                if (!pendingFormats.isEmpty() && insertedLength > 0) {
                    applyPendingFormats(editable, start, start + insertedLength);
                }
            } else if (before > count) {
                // Text was deleted
                int deletedLength = before - count;
                handleTextDeleted(editable, start, deletedLength);
            }
            // If count == before, it's a replacement - spans should adjust automatically
        } finally {
            isModifying = false;
        }
    }

    /**
     * Checks if code format (CODE_BLOCK or INLINE_CODE) is active at the given position.
     * <p>
     * This checks both pending formats and existing spans at the position.
     * </p>
     *
     * @param editable The editable text.
     * @param position The position to check.
     * @return true if code format is active at the position.
     */
    private boolean isCodeFormatActive(Editable editable, int position) {
        // Check pending formats
        if (pendingFormats.contains(FormatType.CODE_BLOCK) || 
            pendingFormats.contains(FormatType.INLINE_CODE)) {
            return true;
        }
        
        // Check existing spans at the position
        RichTextFormatSpan[] spans = editable.getSpans(position, position, RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            FormatType formatType = span.getFormatType();
            if (formatType == FormatType.CODE_BLOCK || formatType == FormatType.INLINE_CODE) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Converts emoji characters to their shortcode representation (Slack-style).
     * <p>
     * For example, 😀 becomes ":grinning:" and 👍 becomes ":thumbsup:".
     * If no shortcode is found, falls back to the emoji character itself
     * (which will be displayed as text in code blocks).
     * </p>
     *
     * @param text The text containing emoji characters.
     * @return The shortcode representation of the emoji.
     */
    private String convertEmojiToShortcode(CharSequence text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        
        String emoji = text.toString();
        String shortcode = EmojiShortcodeMap.getShortcode(emoji);
        
        if (shortcode != null) {
            return ":" + shortcode + ":";
        }
        
        // Fallback: return the emoji as-is (it will be displayed as text in code context)
        return emoji;
    }

    /**
     * Applies pending formats to the specified range.
     * <p>
     * This is called when text is typed and there are pending formats
     * that should be applied to the new text.
     * </p>
     * <p>
     * For ORDERED_LIST format, this method calculates the correct number
     * based on existing numbered list spans before the insertion point.
     * </p>
     * <p>
     * Note: List formats (ORDERED_LIST, BULLET_LIST) are NOT applied to newline
     * characters. They are only applied when actual content is typed on a new line.
     * </p>
     *
     * @param editable The editable text.
     * @param start    The start of the range to format.
     * @param end      The end of the range to format.
     */
    private void applyPendingFormats(Editable editable, int start, int end) {
        if (editable == null || start >= end) {
            return;
        }

        // Check if the inserted text is just a newline - don't apply list formats to newlines
        CharSequence insertedText = editable.subSequence(start, end);
        boolean isOnlyNewline = insertedText.length() == 1 && insertedText.charAt(0) == '\n';
        boolean isInsertedEmoji = isEntirelyEmoji(insertedText);

        for (FormatType formatType : pendingFormats) {
            // Skip list and blockquote formats for newline characters - they should only apply to actual content
            if (isOnlyNewline && (formatType == FormatType.ORDERED_LIST || formatType == FormatType.BULLET_LIST || formatType == FormatType.BLOCKQUOTE)) {
                continue;
            }
            
            // Skip INLINE_CODE for newline characters - inline code doesn't span across lines
            // The format will be re-applied when actual content is typed on the new line
            if (isOnlyNewline && formatType == FormatType.INLINE_CODE) {
                continue;
            }

            // Skip text style formats for emoji characters - emojis should not be formatted
            if (isInsertedEmoji && RichTextSpanManager.isTextStyleFormat(formatType)) {
                continue;
            }
            
            // Check if this range is already covered by an existing span of the same type
            // This can happen when handleTextInserted already extended an existing span
            if (isRangeCoveredByFormat(editable, start, end, formatType)) {
                continue;
            }
            
            if (formatType == FormatType.ORDERED_LIST) {
                // For numbered lists, calculate the correct number based on existing spans
                int nextNumber = calculateNextListNumber(editable, start);
                NumberedListFormatSpan span = new NumberedListFormatSpan(nextNumber);
                // Use INCLUSIVE_INCLUSIVE so the span extends as user types more
                editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (formatType == FormatType.BULLET_LIST) {
                // For bullet lists, also use INCLUSIVE_INCLUSIVE
                BulletListFormatSpan span = new BulletListFormatSpan();
                editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (formatType == FormatType.INLINE_CODE) {
                // For inline code, create a new span for each segment (don't span across newlines)
                // Use INCLUSIVE_INCLUSIVE so the span extends as user types more on the same line
                // Use context for theme-aware styling if available
                InlineCodeFormatSpan span = context != null 
                    ? new InlineCodeFormatSpan(context) 
                    : new InlineCodeFormatSpan();
                editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (formatType == FormatType.BLOCKQUOTE) {
                // For blockquotes, use INCLUSIVE_INCLUSIVE so the span extends as user types more
                BlockquoteFormatSpan span = context != null
                    ? new BlockquoteFormatSpan(context)
                    : new BlockquoteFormatSpan();
                editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                RichTextSpanManager.applyFormat(editable, start, end, formatType, context);
            }
        }
        // Note: We don't clear pending formats here - they remain active
        // until the user clicks the format button again to toggle off
    }
    
    /**
     * Checks if the specified range is fully covered by an existing span of the given format type.
     * <p>
     * This is used to avoid creating duplicate spans when handleTextInserted has already
     * extended an existing span to cover the newly inserted text.
     * </p>
     *
     * @param editable   The editable text.
     * @param start      The start of the range to check.
     * @param end        The end of the range to check.
     * @param formatType The format type to check for.
     * @return true if the range is fully covered by an existing span of the format type.
     */
    private boolean isRangeCoveredByFormat(Editable editable, int start, int end, FormatType formatType) {
        if (editable == null || formatType == null) {
            return false;
        }
        
        RichTextFormatSpan[] spans = editable.getSpans(start, end, RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            if (span.getFormatType() == formatType) {
                int spanStart = editable.getSpanStart(span);
                int spanEnd = editable.getSpanEnd(span);
                // Check if this span fully covers the range
                if (spanStart <= start && spanEnd >= end) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates the next number for a numbered list item.
     * <p>
     * This method finds the NumberedListFormatSpan on the previous line (if any)
     * and returns the next sequential number. If no previous numbered list item
     * exists, returns 1.
     * </p>
     *
     * @param editable The editable text.
     * @param position The position to calculate the next number for.
     * @return The next number for the list (1 if no existing spans found).
     */
    private int calculateNextListNumber(Editable editable, int position) {
        if (editable == null || position <= 0) {
            return 1;
        }

        // Find the start of the current line
        int currentLineStart = position;
        while (currentLineStart > 0 && editable.charAt(currentLineStart - 1) != '\n') {
            currentLineStart--;
        }

        // If we're at the very beginning, this is the first item
        if (currentLineStart == 0) {
            return 1;
        }

        // Find the previous line (the line before the newline that precedes current line)
        // currentLineStart - 1 is the newline character
        int prevLineEnd = currentLineStart - 1;
        int prevLineStart = prevLineEnd;
        while (prevLineStart > 0 && editable.charAt(prevLineStart - 1) != '\n') {
            prevLineStart--;
        }

        // Look for a NumberedListFormatSpan that covers any part of the previous line
        NumberedListFormatSpan[] spans = editable.getSpans(prevLineStart, prevLineEnd + 1, NumberedListFormatSpan.class);
        
        if (spans == null || spans.length == 0) {
            // No numbered list on previous line, start fresh
            return 1;
        }

        // Find the span with the highest number on the previous line
        int maxNumber = 0;
        for (NumberedListFormatSpan span : spans) {
            if (span.getNumber() > maxNumber) {
                maxNumber = span.getNumber();
            }
        }

        return maxNumber + 1;
    }

    /**
     * Handles text insertion within formatted regions.
     * <p>
     * When text is inserted within a span, the span is extended to include
     * the new text. If a newline is inserted, the span may be split.
     * </p>
     * <p>
     * The extension behavior depends on where the insertion occurs:
     * <ul>
     *   <li><b>Inside span:</b> Span automatically extends (Android behavior)</li>
     *   <li><b>At span end:</b> Span is manually extended to include new text,
     *       UNLESS the format is explicitly disabled</li>
     *   <li><b>At span start:</b> Span is not extended (new text is before the format)</li>
     * </ul>
     * </p>
     * <p>
     * Special handling for numbered lists: When a newline is inserted, the span
     * is truncated to end at the newline. This allows new list items to be created
     * with incremented numbers via applyPendingFormats.
     * </p>
     *
     * @param editable       The editable text.
     * @param insertPosition The position where text was inserted.
     * @param insertedLength The length of inserted text.
     */
    private void handleTextInserted(Editable editable, int insertPosition, int insertedLength) {
        // Get the inserted text to check for newlines
        int insertEnd = insertPosition + insertedLength;
        if (insertEnd > editable.length()) {
            insertEnd = editable.length();
        }
        
        CharSequence insertedText = editable.subSequence(insertPosition, insertEnd);
        boolean hasNewline = containsNewline(insertedText);
        boolean isInsertedTextEmoji = isEntirelyEmoji(insertedText);

        // Get all format spans that might be affected
        // We need to check spans before the insertion happened, so we look at the current state
        RichTextFormatSpan[] spans = editable.getSpans(0, editable.length(), RichTextFormatSpan.class);

        for (RichTextFormatSpan span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            FormatType formatType = span.getFormatType();

            // Check if this format is explicitly disabled - if so, don't extend the span
            boolean isFormatDisabled = explicitlyDisabledFormats.contains(formatType);

            // Calculate the original span end before insertion
            // If insertion was inside the span, spanEnd already includes the inserted text
            // If insertion was at the boundary, spanEnd doesn't include it
            
            // Check if insertion was at the end of the span (before insertion)
            // This happens when insertPosition == spanEnd - insertedLength (for inside insertion)
            // or insertPosition == spanEnd (for boundary insertion)
            boolean wasInsideSpan = insertPosition > spanStart && insertPosition <= spanEnd;
            boolean wasAtSpanEnd = insertPosition == spanEnd;

            // Special handling for LINK spans: don't extend when text is typed at the boundary
            // This allows users to type after a link without the text being included in the link
            // Links should only contain the text that was explicitly selected when creating the link
            if (formatType == FormatType.LINK && wasAtSpanEnd) {
                // Don't extend link spans when any text is typed at the end
                // The new text should be outside the link
                continue;
            }

            // Skip extending text style spans for emoji characters
            if (isInsertedTextEmoji && RichTextSpanManager.isTextStyleFormat(formatType)) {
                if (wasInsideSpan && !wasAtSpanEnd) {
                    // Android auto-extended the span to include the emoji - shrink it back
                    // Split the span: [spanStart..insertPosition] and [insertEnd..spanEnd]
                    int spanFlags = editable.getSpanFlags(span);
                    editable.removeSpan(span);
                    if (insertPosition > spanStart) {
                        editable.setSpan(span, spanStart, insertPosition, spanFlags);
                    }
                    if (insertEnd < spanEnd) {
                        RichTextFormatSpan newSpan = RichTextSpanManager.createNewSpanForFormat(formatType);
                        if (newSpan != null) {
                            editable.setSpan(newSpan, insertEnd, spanEnd, spanFlags);
                        }
                    }
                    // Preserve the format as pending so the toolbar stays selected
                    // and the user can continue typing formatted text after the emoji
                    enableFormat(formatType);
                }
                // Don't extend text style spans at boundary for emojis
                continue;
            }
            
            // Special handling for numbered lists: truncate at newline instead of extending
            // This allows each line to have its own span with the correct number
            if (hasNewline && formatType == FormatType.ORDERED_LIST) {
                if (wasInsideSpan) {
                    // Find the newline position and truncate the span there
                    truncateSpanAtNewline(editable, span, insertPosition, insertedLength);
                }
                // Don't extend numbered list spans across newlines
                continue;
            }
            
            // Special handling for bullet lists: truncate at newline instead of extending
            // This allows each line to have its own bullet point
            if (hasNewline && formatType == FormatType.BULLET_LIST) {
                if (wasInsideSpan) {
                    // Find the newline position and truncate the span there
                    truncateSpanAtNewline(editable, span, insertPosition, insertedLength);
                }
                // Don't extend bullet list spans across newlines
                continue;
            }
            
            // Blockquote spans should extend across newlines (like code blocks)
            // They are block-level spans that cover multiple lines as a single unit
            
            if (wasInsideSpan && !wasAtSpanEnd) {
                // Insertion was strictly inside the span
                
                // Safety net: if the format is explicitly disabled, Android may have
                // auto-extended the span (due to INCLUSIVE_INCLUSIVE flags that were set
                // before the format was disabled, or during a replace operation). In this
                // case, shrink the span back so the inserted text is not formatted.
                // This specifically handles the scenario where text is replaced (e.g.,
                // mention insertion via message.replace()) and the span auto-extends
                // to cover the replacement text even though the format was disabled.
                if (isFormatDisabled && RichTextSpanManager.isTextStyleFormat(formatType)) {
                    // Check if the insertion was at what was originally the span boundary
                    // (i.e., the span was auto-extended to include the inserted text)
                    int originalSpanEnd = spanEnd - insertedLength;
                    if (insertPosition >= originalSpanEnd) {
                        // The span was auto-extended — shrink it back
                        int spanFlags = editable.getSpanFlags(span);
                        editable.removeSpan(span);
                        if (originalSpanEnd > spanStart) {
                            editable.setSpan(span, spanStart, originalSpanEnd,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        continue;
                    }
                }
                
                if (hasNewline && shouldSplitOnNewline(span)) {
                    // Split the span at the newline
                    splitSpanAtNewline(editable, span, insertPosition, insertedLength);
                } else {
                    // For non-newline insertions inside the span, Android automatically extends the RichTextFormatSpan
                    // But we need to manually extend associated LeadingMarginSpan for code blocks
                    if (formatType == FormatType.CODE_BLOCK) {
                        // The span was already extended by Android, so spanEnd already includes the inserted text
                        // We need to extend the LeadingMarginSpan to match
                        extendLeadingMarginSpansForCodeBlock(editable, spanStart, spanEnd);
                    }
                }
            } else if (wasAtSpanEnd && spanStart < spanEnd && !isFormatDisabled) {
                // Insertion was at the end boundary of the span
                // We need to manually extend the span to include the new text
                // But only if the span is not empty, we want to extend at boundaries,
                // AND the format is not explicitly disabled
                if (!hasNewline || !shouldSplitOnNewline(span)) {
                    // Extend the span to include the inserted text
                    extendSpanToIncludeInsertedText(editable, span, spanStart, spanEnd, insertedLength);
                } else if (hasNewline && shouldSplitOnNewline(span)) {
                    // First extend, then split
                    extendSpanToIncludeInsertedText(editable, span, spanStart, spanEnd, insertedLength);
                    // Re-fetch span positions after extension
                    int updatedStart = editable.getSpanStart(span);
                    int updatedEnd = editable.getSpanEnd(span);
                    if (updatedStart >= 0 && updatedEnd > updatedStart) {
                        splitSpanAtNewline(editable, span, insertPosition, insertedLength);
                    }
                }
            }
            // If format is disabled and we're at span end, don't extend - new text will be unformatted
        }
    }

    /**
     * Checks if the given text consists entirely of emoji characters.
     *
     * @param text The text to check.
     * @return true if the text is entirely emoji characters (including modifiers and joiners).
     */
    private boolean isEntirelyEmoji(CharSequence text) {
        if (text == null || text.length() == 0) {
            return false;
        }
        String str = text.toString();
        int i = 0;
        boolean hasEmoji = false;
        while (i < str.length()) {
            int codePoint = Character.codePointAt(str, i);
            int charCount = Character.charCount(codePoint);
            if (RichTextSpanManager.isEmojiCodePoint(codePoint) || 
                RichTextSpanManager.isEmojiModifierOrJoiner(codePoint)) {
                hasEmoji = true;
                i += charCount;
            } else if (Character.isWhitespace(codePoint)) {
                // Allow whitespace between emojis
                i += charCount;
            } else {
                return false;
            }
        }
        return hasEmoji;
    }

    /**
     * Truncates a span at a newline character.
     * <p>
     * This is used for numbered lists where each line should have its own span
     * with a different number. When a newline is inserted, the span is truncated
     * to end at the newline position.
     * </p>
     *
     * @param editable       The editable text.
     * @param span           The span to truncate.
     * @param insertPosition The position where the newline was inserted.
     * @param insertedLength The length of inserted text (including newline).
     */
    private void truncateSpanAtNewline(Editable editable, RichTextFormatSpan span,
                                        int insertPosition, int insertedLength) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        int spanFlags = editable.getSpanFlags(span);

        // Find the newline position within the inserted text
        int newlinePos = -1;
        int insertEnd = insertPosition + insertedLength;
        for (int i = insertPosition; i < insertEnd && i < editable.length(); i++) {
            if (editable.charAt(i) == '\n') {
                newlinePos = i;
                break;
            }
        }

        if (newlinePos == -1) {
            return; // No newline found
        }

        // Truncate the span to end at the newline (not including it)
        if (spanStart < newlinePos) {
            editable.removeSpan(span);
            editable.setSpan(span, spanStart, newlinePos, spanFlags);
        } else {
            // Span would be empty, remove it
            editable.removeSpan(span);
        }
    }

    /**
     * Extends a span to include newly inserted text at its boundary.
     * <p>
     * For CODE_BLOCK spans, this also extends any associated LeadingMarginSpan
     * to maintain consistent padding across all lines.
     * </p>
     *
     * @param editable       The editable text.
     * @param span           The span to extend.
     * @param spanStart      The current start of the span.
     * @param spanEnd        The current end of the span (before extension).
     * @param insertedLength The length of inserted text.
     */
    private void extendSpanToIncludeInsertedText(Editable editable, RichTextFormatSpan span,
                                                   int spanStart, int spanEnd, int insertedLength) {
        int spanFlags = editable.getSpanFlags(span);
        int newEnd = spanEnd + insertedLength;
        
        // Clamp to valid bounds
        newEnd = Math.min(newEnd, editable.length());
        
        if (newEnd > spanStart) {
            editable.removeSpan(span);
            editable.setSpan(span, spanStart, newEnd, spanFlags);
            
            // For CODE_BLOCK spans, also extend any associated LeadingMarginSpan
            if (span.getFormatType() == FormatType.CODE_BLOCK) {
                extendLeadingMarginSpans(editable, spanStart, spanEnd, newEnd);
            }
        }
    }
    
    /**
     * Extends LeadingMarginSpan.Standard spans that cover the original range to the new end.
     * <p>
     * This is used to maintain consistent left padding for code blocks when text is inserted.
     * </p>
     *
     * @param editable     The editable text.
     * @param spanStart    The start of the range.
     * @param originalEnd  The original end of the range (before extension).
     * @param newEnd       The new end of the range (after extension).
     */
    private void extendLeadingMarginSpans(Editable editable, int spanStart, int originalEnd, int newEnd) {
        android.text.style.LeadingMarginSpan.Standard[] marginSpans = 
            editable.getSpans(spanStart, originalEnd, android.text.style.LeadingMarginSpan.Standard.class);
        
        for (android.text.style.LeadingMarginSpan.Standard marginSpan : marginSpans) {
            // Skip RichTextFormatSpan instances (list spans, blockquote spans) —
            // they manage their own boundaries and must not be extended by code block logic
            if (marginSpan instanceof RichTextFormatSpan) {
                continue;
            }
            
            int marginStart = editable.getSpanStart(marginSpan);
            int marginEnd = editable.getSpanEnd(marginSpan);
            int marginFlags = editable.getSpanFlags(marginSpan);
            
            // Only extend if this margin span covers the same range as the code block
            if (marginStart == spanStart && marginEnd == originalEnd) {
                editable.removeSpan(marginSpan);
                editable.setSpan(marginSpan, marginStart, newEnd, marginFlags);
            }
        }
    }
    
    /**
     * Extends LeadingMarginSpan.Standard spans to match the current code block span boundaries.
     * <p>
     * This is called when text is inserted inside a code block and Android has already
     * extended the CodeBlockFormatSpan. We need to extend the LeadingMarginSpan to match.
     * </p>
     *
     * @param editable  The editable text.
     * @param spanStart The start of the code block span.
     * @param spanEnd   The current end of the code block span (after Android's automatic extension).
     */
    private void extendLeadingMarginSpansForCodeBlock(Editable editable, int spanStart, int spanEnd) {
        android.text.style.LeadingMarginSpan.Standard[] marginSpans = 
            editable.getSpans(spanStart, spanEnd, android.text.style.LeadingMarginSpan.Standard.class);
        
        for (android.text.style.LeadingMarginSpan.Standard marginSpan : marginSpans) {
            // Skip RichTextFormatSpan instances (list spans, blockquote spans) —
            // they manage their own boundaries and must not be extended by code block logic
            if (marginSpan instanceof RichTextFormatSpan) {
                continue;
            }
            
            int marginStart = editable.getSpanStart(marginSpan);
            int marginEnd = editable.getSpanEnd(marginSpan);
            int marginFlags = editable.getSpanFlags(marginSpan);
            
            // Extend if this margin span starts at the same position but ends before the code block
            if (marginStart == spanStart && marginEnd < spanEnd) {
                editable.removeSpan(marginSpan);
                editable.setSpan(marginSpan, marginStart, spanEnd, marginFlags);
            }
        }
    }

    /**
     * Handles text deletion within formatted regions.
     * <p>
     * When text is deleted from a span, the span boundaries shrink automatically
     * by Android's Spannable system. This method handles the cleanup:
     * <ul>
     *   <li>Removes spans that become empty (start >= end)</li>
     *   <li>Removes spans where all content has been deleted</li>
     * </ul>
     * </p>
     * <p>
     * Note: Android's Spannable automatically adjusts span boundaries when text
     * is deleted. This method primarily handles edge cases and cleanup.
     * </p>
     *
     * @param editable      The editable text.
     * @param deleteStart   The start position of the deletion.
     * @param deletedLength The length of deleted text.
     */
    private void handleTextDeleted(Editable editable, int deleteStart, int deletedLength) {
        // Get all format spans
        RichTextFormatSpan[] spans = editable.getSpans(0, editable.length(), RichTextFormatSpan.class);

        for (RichTextFormatSpan span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            // Check if span is now empty (start >= end) or invalid
            if (spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) {
                editable.removeSpan(span);
            }
        }

        // If all text has been cleared, reset pending/disabled format state
        // so the toolbar deselects and new text starts with no formatting.
        if (editable.length() == 0) {
            clearPendingFormats();
            // Also remove any lingering TypefaceSpan("monospace") left over from
            // code blocks — these are standard Android spans (not RichTextFormatSpan)
            // so they aren't cleaned up by the loop above.
            android.text.style.TypefaceSpan[] typefaceSpans =
                editable.getSpans(0, 0, android.text.style.TypefaceSpan.class);
            for (android.text.style.TypefaceSpan ts : typefaceSpans) {
                if ("monospace".equals(ts.getFamily())) {
                    editable.removeSpan(ts);
                }
            }
        }
    }

    /**
     * Splits a span at a newline character.
     * <p>
     * When a newline is inserted within a span, the span is split into two
     * separate spans, each maintaining the same format type.
     * </p>
     * <p>
     * For inline formats (BOLD, ITALIC, STRIKETHROUGH, UNDERLINE), the format
     * is added to pending formats so it continues on the new line.
     * </p>
     *
     * @param editable       The editable text.
     * @param span           The span to split.
     * @param insertPosition The position where the newline was inserted.
     * @param insertedLength The length of inserted text (including newline).
     */
    private void splitSpanAtNewline(Editable editable, RichTextFormatSpan span, 
                                     int insertPosition, int insertedLength) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        int spanFlags = editable.getSpanFlags(span);
        FormatType formatType = span.getFormatType();

        // Find the newline position within the inserted text
        int newlinePos = -1;
        int insertEnd = insertPosition + insertedLength;
        for (int i = insertPosition; i < insertEnd && i < editable.length(); i++) {
            if (editable.charAt(i) == '\n') {
                newlinePos = i;
                break;
            }
        }

        if (newlinePos == -1) {
            return; // No newline found
        }

        // Remove the original span
        editable.removeSpan(span);

        // Create first span (before newline)
        if (spanStart < newlinePos) {
            RichTextFormatSpan firstSpan = createSpanCopy(span);
            if (firstSpan != null) {
                editable.setSpan(firstSpan, spanStart, newlinePos, spanFlags);
            }
        }

        // Create second span (after newline)
        int afterNewline = newlinePos + 1;
        if (afterNewline < spanEnd) {
            RichTextFormatSpan secondSpan = createSpanCopy(span);
            if (secondSpan != null) {
                editable.setSpan(secondSpan, afterNewline, spanEnd, spanFlags);
            }
        }

        // For inline formats, add to pending formats so they continue on the new line
        // This ensures that when the user types on the new line, the format is applied
        if (shouldContinueFormatOnNewLine(formatType)) {
            pendingFormats.add(formatType);
            explicitlyDisabledFormats.remove(formatType);
        }
    }

    /**
     * Determines if a format should continue on a new line after being split.
     * <p>
     * Inline text styles (bold, italic, strikethrough, underline) should continue
     * on the new line to maintain formatting continuity.
     * </p>
     *
     * @param formatType The format type to check.
     * @return true if the format should continue on the new line, false otherwise.
     */
    private boolean shouldContinueFormatOnNewLine(FormatType formatType) {
        if (formatType == null) {
            return false;
        }
        
        switch (formatType) {
            case BOLD:
            case ITALIC:
            case STRIKETHROUGH:
            case UNDERLINE:
                return true;
            case INLINE_CODE:
            case LINK:
            case CODE_BLOCK:
            case BULLET_LIST:
            case ORDERED_LIST:
            case BLOCKQUOTE:
                return false;
            default:
                return false;
        }
    }

    /**
     * Checks if the given text contains a newline character.
     *
     * @param text The text to check.
     * @return true if the text contains a newline, false otherwise.
     */
    private boolean containsNewline(CharSequence text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a span should be split when a newline is inserted.
     * <p>
     * Inline text styles (bold, italic, strikethrough) should be split on newline.
     * Block-level formats (code block, blockquote, lists) should not be split.
     * </p>
     *
     * @param span The span to check.
     * @return true if the span should be split on newline, false otherwise.
     */
    private boolean shouldSplitOnNewline(RichTextFormatSpan span) {
        if (span == null) {
            return false;
        }
        
        switch (span.getFormatType()) {
            case BOLD:
            case ITALIC:
            case STRIKETHROUGH:
            case UNDERLINE:
            case INLINE_CODE:
            case LINK:
                return true;
            case CODE_BLOCK:
            case BULLET_LIST:
            case ORDERED_LIST:
            case BLOCKQUOTE:
                return false;
            default:
                return false;
        }
    }

    /**
     * Creates a copy of a span with the same format type.
     * <p>
     * This is used when splitting spans to create new instances with
     * the same formatting.
     * </p>
     *
     * @param original The original span to copy.
     * @return A new span instance with the same format type, or null if not supported.
     */
    private RichTextFormatSpan createSpanCopy(RichTextFormatSpan original) {
        if (original == null) {
            return null;
        }

        switch (original.getFormatType()) {
            case BOLD:
                return new BoldFormatSpan();
            case ITALIC:
                return new ItalicFormatSpan();
            case UNDERLINE:
                return new UnderlineFormatSpan();
            case STRIKETHROUGH:
                return new StrikethroughFormatSpan();
            case INLINE_CODE:
                // Use context for theme-aware styling if available
                return context != null 
                    ? new InlineCodeFormatSpan(context) 
                    : new InlineCodeFormatSpan();
            case CODE_BLOCK:
                return context != null ? new CodeBlockFormatSpan(context) : new CodeBlockFormatSpan();
            case LINK:
                if (original instanceof LinkFormatSpan) {
                    return new LinkFormatSpan(((LinkFormatSpan) original).getUrl());
                }
                return new LinkFormatSpan("");
            case BULLET_LIST:
                return new BulletListFormatSpan();
            case ORDERED_LIST:
                if (original instanceof NumberedListFormatSpan) {
                    return new NumberedListFormatSpan(((NumberedListFormatSpan) original).getNumber());
                }
                return new NumberedListFormatSpan(1);
            case BLOCKQUOTE:
                return context != null ? new BlockquoteFormatSpan(context) : new BlockquoteFormatSpan();
            default:
                return null;
        }
    }

    /**
     * Attaches this watcher to an Editable.
     * <p>
     * This is a convenience method to properly attach the watcher to an Editable
     * with the correct span flags.
     * </p>
     *
     * @param editable The editable to attach to.
     */
    public void attachTo(Editable editable) {
        if (editable == null) {
            return;
        }
        // Remove any existing FormatSpanWatcher
        FormatSpanWatcher[] existing = editable.getSpans(0, editable.length(), FormatSpanWatcher.class);
        for (FormatSpanWatcher watcher : existing) {
            editable.removeSpan(watcher);
        }
        // Attach this watcher
        editable.setSpan(this, 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /**
     * Detaches this watcher from an Editable.
     *
     * @param editable The editable to detach from.
     */
    public void detachFrom(Editable editable) {
        if (editable == null) {
            return;
        }
        editable.removeSpan(this);
    }

    /**
     * Extends a span to include newly inserted text.
     * <p>
     * This method can be called to manually extend a span when text is inserted
     * at its boundary. Normally, spans with SPAN_EXCLUSIVE_EXCLUSIVE don't extend
     * when text is inserted at boundaries, so this method provides manual control.
     * </p>
     *
     * @param editable The editable text.
     * @param span     The span to extend.
     * @param newEnd   The new end position for the span.
     */
    public static void extendSpan(Editable editable, RichTextFormatSpan span, int newEnd) {
        if (editable == null || span == null) {
            return;
        }

        int spanStart = editable.getSpanStart(span);
        int spanFlags = editable.getSpanFlags(span);

        if (spanStart < 0 || newEnd <= spanStart || newEnd > editable.length()) {
            return;
        }

        // Remove and re-add with new boundaries
        editable.removeSpan(span);
        editable.setSpan(span, spanStart, newEnd, spanFlags);
    }

    /**
     * Shrinks a span by adjusting its boundaries.
     * <p>
     * This method can be called to manually shrink a span when text is deleted.
     * If the new boundaries would make the span empty, the span is removed.
     * </p>
     *
     * @param editable The editable text.
     * @param span     The span to shrink.
     * @param newStart The new start position for the span.
     * @param newEnd   The new end position for the span.
     */
    public static void shrinkSpan(Editable editable, RichTextFormatSpan span, int newStart, int newEnd) {
        if (editable == null || span == null) {
            return;
        }

        // If span would be empty, remove it
        if (newStart >= newEnd) {
            editable.removeSpan(span);
            return;
        }

        int spanFlags = editable.getSpanFlags(span);

        // Clamp to valid bounds
        newStart = Math.max(0, newStart);
        newEnd = Math.min(editable.length(), newEnd);

        if (newStart >= newEnd) {
            editable.removeSpan(span);
            return;
        }

        // Remove and re-add with new boundaries
        editable.removeSpan(span);
        editable.setSpan(span, newStart, newEnd, spanFlags);
    }
}
