package com.cometchat.chatuikit.shared.spans;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import java.util.EnumSet;
import java.util.Set;

/**
 * RichTextSpanManager handles span-based rich text formatting for WYSIWYG editing.
 * <p>
 * Unlike RichTextFormatterManager which inserts markdown markers, this class
 * works purely with Android spans for visual formatting. No markdown syntax
 * is inserted into the text - formatting is tracked via span objects.
 * </p>
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Apply formatting spans to text ranges</li>
 *   <li>Remove formatting spans from text ranges</li>
 *   <li>Toggle formatting on/off for text ranges</li>
 *   <li>Detect active formats at cursor positions</li>
 * </ul>
 * </p>
 * <p>
 * <b>Nested Format Handling:</b>
 * <ul>
 *   <li>Text styles (bold, italic, strikethrough) can be combined on the same text</li>
 *   <li>Code formats (inline code, code block) are exclusive - they remove text styles when applied</li>
 *   <li>Removing one format preserves other formats on the same text</li>
 * </ul>
 * </p>
 * <p>
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 9.4, 12.4, 13.1, 13.2, 13.3, 13.4, 13.5
 * </p>
 */
public class RichTextSpanManager {

    /**
     * Set of text style format types that can be combined.
     */
    private static final Set<FormatType> TEXT_STYLE_FORMATS = EnumSet.of(
            FormatType.BOLD,
            FormatType.ITALIC,
            FormatType.STRIKETHROUGH,
            FormatType.UNDERLINE
    );

    /**
     * Set of code format types that are exclusive (cannot be combined with text styles).
     */
    private static final Set<FormatType> CODE_FORMATS = EnumSet.of(
            FormatType.CODE_BLOCK
    );

    private RichTextSpanManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if the given format type is a text style (bold, italic, strikethrough).
     *
     * @param formatType The format type to check.
     * @return true if the format is a text style, false otherwise.
     */
    public static boolean isTextStyleFormat(FormatType formatType) {
        return formatType != null && TEXT_STYLE_FORMATS.contains(formatType);
    }

    /**
     * Checks if the given format type is a code format (inline code, code block).
     *
     * @param formatType The format type to check.
     * @return true if the format is a code format, false otherwise.
     */
    public static boolean isCodeFormat(FormatType formatType) {
        return formatType != null && CODE_FORMATS.contains(formatType);
    }

    /**
     * Applies a format to the specified range using spans only.
     * <p>
     * No markdown markers are inserted into the text. The format is applied
     * by creating an appropriate span and setting it on the editable.
     * </p>
     * <p>
     * <b>Nested Format Handling:</b>
     * <ul>
     *   <li>Text styles (bold, italic, strikethrough) can be combined freely</li>
     *   <li>Code formats (inline code, code block) are exclusive - applying them
     *       removes any existing text styles from the range</li>
     * </ul>
     * </p>
     * <p>
     * <b>Numbered List Handling:</b>
     * When applying ORDERED_LIST format, the list is automatically renumbered
     * to ensure sequential numbering across all list items.
     * </p>
     * <p>
     * <b>Code Block Handling:</b>
     * When applying CODE_BLOCK format:
     * <ul>
     *   <li>Leading whitespace is trimmed from the selected text</li>
     *   <li>Newlines are inserted before/after if not already present to create block separation</li>
     * </ul>
     * </p>
     * <p>
     * Edge cases handled:
     * <ul>
     *   <li>Empty selection (start == end): No span is applied</li>
     *   <li>Invalid range (start > end): Range is swapped</li>
     *   <li>Out of bounds: Range is clamped to valid bounds</li>
     *   <li>Null editable: Returns without modification</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 13.1, 13.2, 13.4, 13.5
     * </p>
     *
     * @param editable   The editable text to apply formatting to.
     * @param start      The start index of the range to format.
     * @param end        The end index of the range to format.
     * @param formatType The type of format to apply.
     */
    public static void applyFormat(Editable editable, int start, int end, FormatType formatType) {
        applyFormat(editable, start, end, formatType, null);
    }

    /**
     * Applies a format to the specified range in the editable text with context for theme colors.
     * <p>
     * This overload accepts a context parameter to enable theme-aware styling for
     * code blocks and other spans that need to access theme colors.
     * </p>
     *
     * @param editable   The editable text to apply formatting to.
     * @param start      The start index of the range to format.
     * @param end        The end index of the range to format.
     * @param formatType The type of format to apply.
     * @param context    The context for accessing theme colors (can be null).
     */
    public static void applyFormat(Editable editable, int start, int end, FormatType formatType, 
                                   android.content.Context context) {
        if (editable == null || formatType == null) {
            return;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        // Don't apply span for empty selection
        if (normalizedStart >= normalizedEnd) {
            return;
        }

        // Code format exclusivity: If applying a code format, remove text styles first
        if (isCodeFormat(formatType)) {
            removeTextStylesFromRange(editable, normalizedStart, normalizedEnd);
        }

        // If applying a text style to code-formatted text, don't apply it
        if (isTextStyleFormat(formatType) && hasCodeFormatInRange(editable, normalizedStart, normalizedEnd)) {
            return;
        }

        // For list formats (bullet and numbered), apply span per line
        if (formatType == FormatType.ORDERED_LIST || formatType == FormatType.BULLET_LIST) {
            applyListFormatPerLine(editable, normalizedStart, normalizedEnd, formatType);
            return;
        }

        // For code blocks, apply special handling for block-level formatting
        if (formatType == FormatType.CODE_BLOCK) {
            applyCodeBlockFormat(editable, normalizedStart, normalizedEnd, context);
            return;
        }

        // For text style formats, skip emoji characters and apply spans only to non-emoji segments
        if (isTextStyleFormat(formatType)) {
            applyFormatSkippingEmojis(editable, normalizedStart, normalizedEnd, formatType);
            return;
        }

        // For blockquotes, use INCLUSIVE_INCLUSIVE so the span extends across newlines
        // (block-level format like code block, not per-line like lists)
        if (formatType == FormatType.BLOCKQUOTE) {
            RichTextFormatSpan span = createSpanForFormat(formatType);
            if (span != null) {
                editable.setSpan(span, normalizedStart, normalizedEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            return;
        }

        // Create and apply the appropriate span for non-list, non-code-block formats
        RichTextFormatSpan span = createSpanForFormat(formatType, context);
        if (span != null) {
            editable.setSpan(span, normalizedStart, normalizedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Applies text style formatting to a range while skipping emoji characters.
     * <p>
     * Scans the range for emoji characters and applies formatting spans only to
     * contiguous non-emoji text segments. This prevents formatting markers from
     * wrapping emojis in the markdown output.
     * </p>
     *
     * @param editable   The editable text.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The text style format to apply.
     */
    private static void applyFormatSkippingEmojis(Editable editable, int start, int end, FormatType formatType) {
        String text = editable.toString();
        int segmentStart = -1;

        int i = start;
        while (i < end) {
            int codePoint = Character.codePointAt(text, i);
            int charCount = Character.charCount(codePoint);

            if (isEmojiCodePoint(codePoint)) {
                // If we have a non-emoji segment accumulated, apply the span
                if (segmentStart >= 0) {
                    RichTextFormatSpan span = createSpanForFormat(formatType);
                    if (span != null) {
                        editable.setSpan(span, segmentStart, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    segmentStart = -1;
                }
                
                i += charCount;
                while (i < end) {
                    int nextCp = Character.codePointAt(text, i);
                    if (isEmojiModifierOrJoiner(nextCp)) {
                        i += Character.charCount(nextCp);
                    } else {
                        break;
                    }
                }
            } else {
                if (segmentStart < 0) {
                    segmentStart = i;
                }
                i += charCount;
            }
        }

        // Apply span to the last non-emoji segment
        if (segmentStart >= 0) {
            RichTextFormatSpan span = createSpanForFormat(formatType);
            if (span != null) {
                editable.setSpan(span, segmentStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Checks if a Unicode code point is an emoji character.
     *
     * @param codePoint The Unicode code point to check.
     * @return true if the code point is an emoji.
     */
    public static boolean isEmojiCodePoint(int codePoint) {
        // Variation Selector-16 (emoji presentation)
        if (codePoint == 0xFE0F) return true;
        // Emoticons
        if (codePoint >= 0x1F600 && codePoint <= 0x1F64F) return true;
        // Miscellaneous Symbols and Pictographs
        if (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) return true;
        // Transport and Map Symbols
        if (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) return true;
        // Supplemental Symbols and Pictographs
        if (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) return true;
        // Symbols and Pictographs Extended-A
        if (codePoint >= 0x1FA00 && codePoint <= 0x1FA6F) return true;
        // Symbols and Pictographs Extended-B
        if (codePoint >= 0x1FA70 && codePoint <= 0x1FAFF) return true;
        // Dingbats
        if (codePoint >= 0x2702 && codePoint <= 0x27B0) return true;
        // Miscellaneous Symbols
        if (codePoint >= 0x2600 && codePoint <= 0x26FF) return true;
        // CJK Symbols (some used as emoji)
        if (codePoint >= 0x2300 && codePoint <= 0x23FF) return true;
        // Enclosed Alphanumeric Supplement (circled numbers, etc.)
        if (codePoint >= 0x1F100 && codePoint <= 0x1F1FF) return true;
        // Regional Indicator Symbols (flags)
        if (codePoint >= 0x1F1E0 && codePoint <= 0x1F1FF) return true;
        // Keycap sequences base characters
        if (codePoint == 0x23 || codePoint == 0x2A || (codePoint >= 0x30 && codePoint <= 0x39)) {
            // These are only emoji when followed by 0xFE0F + 0x20E3, handled by modifier check
            return false;
        }
        // Mahjong Tiles, Domino Tiles, Playing Cards
        if (codePoint >= 0x1F000 && codePoint <= 0x1F02F) return true;
        if (codePoint >= 0x1F0A0 && codePoint <= 0x1F0FF) return true;
        // Zero Width Joiner (used in emoji sequences)
        if (codePoint == 0x200D) return true;
        // Combining Enclosing Keycap
        if (codePoint == 0x20E3) return true;
        // Tags block (used in flag sequences)
        if (codePoint >= 0xE0020 && codePoint <= 0xE007F) return true;
        // Skin tone modifiers
        if (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF) return true;
        // Additional common emoji
        if (codePoint == 0x200D || codePoint == 0x2640 || codePoint == 0x2642 ||
            codePoint == 0x2695 || codePoint == 0x2696 || codePoint == 0x2708 ||
            codePoint == 0x2764 || codePoint == 0x2763) return true;
        // Arrows used as emoji
        if (codePoint >= 0x2194 && codePoint <= 0x21AA) return true;
        // Copyright, Registered, TM
        if (codePoint == 0xA9 || codePoint == 0xAE) return true;

        return false;
    }

    /**
     * Checks if a code point is an emoji modifier, variation selector, or joiner
     * that continues an emoji sequence.
     *
     * @param codePoint The Unicode code point to check.
     * @return true if the code point is part of an emoji sequence continuation.
     */
    public static boolean isEmojiModifierOrJoiner(int codePoint) {
        // Variation Selector-16 (emoji presentation)
        if (codePoint == 0xFE0F) return true;
        // Variation Selector-15 (text presentation)
        if (codePoint == 0xFE0E) return true;
        // Zero Width Joiner
        if (codePoint == 0x200D) return true;
        // Combining Enclosing Keycap
        if (codePoint == 0x20E3) return true;
        // Skin tone modifiers
        if (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF) return true;
        // Tags block (used in flag sequences)
        if (codePoint >= 0xE0020 && codePoint <= 0xE007F) return true;
        // Tag Cancel
        if (codePoint == 0xE007F) return true;
        // Regional Indicator Symbols (second flag character)
        if (codePoint >= 0x1F1E0 && codePoint <= 0x1F1FF) return true;
        // If it's an emoji itself (for ZWJ sequences like 👨‍👩‍👧‍👦)
        if (isEmojiCodePoint(codePoint)) return true;

        return false;
    }

    /**
     * Applies code block formatting with proper block-level handling.
     * <p>
     * This method:
     * <ul>
     *   <li>Trims leading whitespace from the selected text</li>
     *   <li>Inserts blank lines (two newlines) before/after for visual block separation</li>
     *   <li>Applies CodeBlockFormatSpan and LeadingMarginSpan</li>
     * </ul>
     * </p>
     *
     * @param editable The editable text.
     * @param start    The start index of the selection.
     * @param end      The end index of the selection.
     * @param context  The context for accessing theme colors (can be null).
     */
    private static void applyCodeBlockFormat(Editable editable, int start, int end, 
                                              android.content.Context context) {
        String text = editable.toString();
        
        // Step 1: Trim leading whitespace from the selected text
        int trimmedStart = start;
        while (trimmedStart < end && Character.isWhitespace(text.charAt(trimmedStart)) 
               && text.charAt(trimmedStart) != '\n') {
            trimmedStart++;
        }
        
        // If we trimmed any leading whitespace, delete it
        if (trimmedStart > start) {
            editable.delete(start, trimmedStart);
            // Adjust end position after deletion
            end -= (trimmedStart - start);
            // Update text reference after modification
            text = editable.toString();
        }
        
        // Step 2: Check if we need to insert newlines for block separation
        // We want a blank line (two newlines) before and after for visual separation
        
        // Check what's before the selection
        boolean needNewlineBefore = false;
        int newlinesBeforeCount = 0;
        if (start > 0) {
            // Count existing newlines before
            int checkPos = start - 1;
            while (checkPos >= 0 && text.charAt(checkPos) == '\n') {
                newlinesBeforeCount++;
                checkPos--;
            }
            // We want at least 2 newlines (blank line) before, unless at start of text
            if (checkPos >= 0) { // There's non-newline content before
                needNewlineBefore = newlinesBeforeCount < 2;
            }
        }
        
        // Check what's after the selection
        boolean needNewlineAfter = false;
        int newlinesAfterCount = 0;
        if (end < text.length()) {
            // Count existing newlines after
            int checkPos = end;
            while (checkPos < text.length() && text.charAt(checkPos) == '\n') {
                newlinesAfterCount++;
                checkPos++;
            }
            // We want at least 2 newlines (blank line) after, unless at end of text
            if (checkPos < text.length()) { // There's non-newline content after
                needNewlineAfter = newlinesAfterCount < 2;
            }
        }
        
        // Insert newlines after first (to preserve start position)
        if (needNewlineAfter) {
            int newlinesToAdd = 2 - newlinesAfterCount;
            StringBuilder newlines = new StringBuilder();
            for (int i = 0; i < newlinesToAdd; i++) {
                newlines.append("\n");
            }
            editable.insert(end, newlines.toString());
            // Don't adjust end - the code block shouldn't include the trailing newlines
        }
        
        // Insert newlines before
        if (needNewlineBefore) {
            int newlinesToAdd = 2 - newlinesBeforeCount;
            StringBuilder newlines = new StringBuilder();
            for (int i = 0; i < newlinesToAdd; i++) {
                newlines.append("\n");
            }
            editable.insert(start, newlines.toString());
            start += newlinesToAdd; // Adjust start to after the newlines
            end += newlinesToAdd;   // Adjust end due to insertion
        }
        
        // Step 3: Apply the code block span (also handles leading margin via LeadingMarginSpan)
        CodeBlockFormatSpan codeBlockSpan = context != null 
            ? new CodeBlockFormatSpan(context) 
            : new CodeBlockFormatSpan();
        editable.setSpan(codeBlockSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        
        // Step 4: Apply monospace font
        android.text.style.TypefaceSpan typefaceSpan = 
            new android.text.style.TypefaceSpan("monospace");
        editable.setSpan(typefaceSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /**
     * Applies list format (bullet or numbered) to each line in the range.
     * <p>
     * For numbered lists, each line gets a span with an incrementing number.
     * For bullet lists, each line gets its own bullet span.
     * </p>
     *
     * @param editable   The editable text.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The list format type (ORDERED_LIST or BULLET_LIST).
     */
    private static void applyListFormatPerLine(Editable editable, int start, int end, FormatType formatType) {
        String text = editable.toString();
        int lineNumber = 1;
        int lineStart = start;

        // Find the start of the first line (go back to beginning of line if needed)
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        
        // Process each line in the range
        while (lineStart < end && lineStart < text.length()) {
            // Find end of current line
            int lineEnd = lineStart;
            while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
                lineEnd++;
            }
            
            // Only apply span if line has content and overlaps with selection
            if (lineEnd > lineStart && lineStart < end && lineEnd > start) {
                // Calculate the actual span range (intersection with selection)
                int spanStart = Math.max(lineStart, start);
                int spanEnd = Math.min(lineEnd, end);
                
                // Apply span to the full line content
                spanStart = lineStart;
                spanEnd = lineEnd;
                
                if (spanEnd > spanStart) {
                    if (formatType == FormatType.ORDERED_LIST) {
                        NumberedListFormatSpan span = new NumberedListFormatSpan(lineNumber);
                        editable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        lineNumber++;
                    } else if (formatType == FormatType.BULLET_LIST) {
                        BulletListFormatSpan span = new BulletListFormatSpan();
                        editable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            
            // Move to next line
            lineStart = lineEnd + 1;
        }
    }

    /**
     * Removes all text style formats (bold, italic, strikethrough) from the specified range.
     * <p>
     * This is used when applying code formats to ensure code format exclusivity.
     * </p>
     * <p>
     * Validates: Requirements 13.5
     * </p>
     *
     * @param editable The editable text to remove text styles from.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     */
    private static void removeTextStylesFromRange(Editable editable, int start, int end) {
        for (FormatType textStyle : TEXT_STYLE_FORMATS) {
            removeFormat(editable, start, end, textStyle);
        }
    }

    /**
     * Checks if any code format (inline code or code block) exists in the specified range.
     *
     * @param editable The editable text to check.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     * @return true if any code format exists in the range, false otherwise.
     */
    public static boolean hasCodeFormatInRange(Editable editable, int start, int end) {
        if (editable == null) {
            return false;
        }

        RichTextFormatSpan[] spans = editable.getSpans(start, end, RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            if (isCodeFormat(span.getFormatType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a format from the specified range.
     * <p>
     * Finds and removes all spans of the specified format type that overlap
     * with the given range. Handles partial overlap cases by splitting spans
     * or adjusting their boundaries.
     * </p>
     * <p>
     * <b>Nested Format Preservation:</b>
     * When removing a format, other formats on the same text are preserved.
     * For example, removing bold from bold-italic text leaves the italic intact.
     * </p>
     * <p>
     * Validates: Requirements 13.3
     * </p>
     *
     * @param editable   The editable text to remove formatting from.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The type of format to remove.
     */
    public static void removeFormat(Editable editable, int start, int end, FormatType formatType) {
        removeFormat(editable, start, end, formatType, null);
    }

    /**
     * Removes a specific format from the given range, with optional context for
     * creating theme-aware replacement spans when the removal is partial.
     */
    public static void removeFormat(Editable editable, int start, int end, FormatType formatType,
                                     android.content.Context context) {
        if (editable == null || formatType == null) {
            return;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        // For block-level formats (CODE_BLOCK, BLOCKQUOTE), expand the removal
        // range to full line boundaries so split spans always start/end at line
        // edges. This ensures that selecting part of a line inside a code block
        // and toggling off removes the format from the entire line.
        boolean isBlockFormat = (formatType == FormatType.CODE_BLOCK
                || formatType == FormatType.BLOCKQUOTE);
        if (isBlockFormat) {
            String text = editable.toString();
            // Expand start to beginning of line
            while (normalizedStart > 0 && text.charAt(normalizedStart - 1) != '\n') {
                normalizedStart--;
            }
            // Expand end to end of line (include trailing newline if present)
            while (normalizedEnd < text.length() && text.charAt(normalizedEnd) != '\n') {
                normalizedEnd++;
            }
            if (normalizedEnd < text.length() && text.charAt(normalizedEnd) == '\n') {
                normalizedEnd++; // include the newline so the split span starts on the next line
            }
        }

        // Get all RichTextFormatSpan instances that overlap with the range
        RichTextFormatSpan[] spans = editable.getSpans(normalizedStart, normalizedEnd, RichTextFormatSpan.class);

        for (RichTextFormatSpan span : spans) {
            if (span.getFormatType() != formatType) {
                continue;
            }

            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            // Remove the original span
            editable.removeSpan(span);
            
            // For code blocks, also remove associated TypefaceSpan (monospace)
            if (formatType == FormatType.CODE_BLOCK) {
                android.text.style.TypefaceSpan[] typefaceSpans = 
                    editable.getSpans(spanStart, spanEnd, android.text.style.TypefaceSpan.class);
                for (android.text.style.TypefaceSpan ts : typefaceSpans) {
                    if ("monospace".equals(ts.getFamily())) {
                        editable.removeSpan(ts);
                    }
                }
                // Also remove any legacy LeadingMarginSpan.Standard from before
                // CodeBlockFormatSpan implemented LeadingMarginSpan
                android.text.style.LeadingMarginSpan.Standard[] marginSpans = 
                    editable.getSpans(spanStart, spanEnd, android.text.style.LeadingMarginSpan.Standard.class);
                for (android.text.style.LeadingMarginSpan.Standard marginSpan : marginSpans) {
                    if (marginSpan instanceof BlockquoteFormatSpan) {
                        continue; // Don't remove blockquote spans
                    }
                    editable.removeSpan(marginSpan);
                }
            }

            // Handle partial overlaps by creating new spans for non-overlapping parts
            // Case 1: Span extends before the removal range
            if (spanStart < normalizedStart) {
                RichTextFormatSpan newSpan = createSpanForFormat(formatType, context);
                if (newSpan != null) {
                    editable.setSpan(newSpan, spanStart, normalizedStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (formatType == FormatType.CODE_BLOCK) {
                        editable.setSpan(new android.text.style.TypefaceSpan("monospace"),
                                spanStart, normalizedStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            // Case 2: Span extends after the removal range
            if (spanEnd > normalizedEnd) {
                RichTextFormatSpan newSpan = createSpanForFormat(formatType, context);
                if (newSpan != null) {
                    editable.setSpan(newSpan, normalizedEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (formatType == FormatType.CODE_BLOCK) {
                        editable.setSpan(new android.text.style.TypefaceSpan("monospace"),
                                normalizedEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }

    /**
     * Removes a specific format from the range while preserving all other formats.
     * <p>
     * This method is specifically designed for nested format handling where
     * removing one format should not affect other formats on the same text.
     * </p>
     * <p>
     * Validates: Requirements 13.3
     * </p>
     *
     * @param editable   The editable text to modify.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The specific format type to remove.
     */
    public static void removeFormatPreservingOthers(Editable editable, int start, int end, FormatType formatType) {
        // This is essentially the same as removeFormat since removeFormat already
        // only removes spans of the specified type, preserving others
        removeFormat(editable, start, end, formatType);
    }

    /**
     * Toggles a format on/off for the specified range.
     * <p>
     * If the format exists at the selection (any part of the range has the format),
     * it is removed. Otherwise, the format is applied to the entire range.
     * </p>
     * <p>
     * <b>Code Format Exclusivity:</b>
     * When toggling on a code format, text styles are removed from the range.
     * When toggling on a text style, it is not applied if code format exists.
     * </p>
     * <p>
     * Validates: Requirements 13.5
     * </p>
     *
     * @param editable   The editable text to toggle formatting on.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The type of format to toggle.
     */
    public static void toggleFormat(Editable editable, int start, int end, FormatType formatType) {
        toggleFormat(editable, start, end, formatType, null);
    }

    /**
     * Toggles a format on the specified range with context for theme colors.
     * <p>
     * If the format exists in the range, it is removed.
     * If the format does not exist, it is applied.
     * </p>
     *
     * @param editable   The editable text.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The format type to toggle.
     * @param context    The context for accessing theme colors (can be null).
     */
    public static void toggleFormat(Editable editable, int start, int end, FormatType formatType,
                                    android.content.Context context) {
        if (editable == null || formatType == null) {
            return;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        boolean hasFormat = hasFormatInRange(editable, normalizedStart, normalizedEnd, formatType);

        // Check if format exists in the range
        if (hasFormat) {
            removeFormat(editable, normalizedStart, normalizedEnd, formatType, context);
        } else {
            applyFormat(editable, normalizedStart, normalizedEnd, formatType, context);
        }
    }

    /**
     * Applies multiple formats to the specified range.
     * <p>
     * This method allows applying multiple text styles (bold, italic, strikethrough)
     * to the same range in a single call. Code formats cannot be combined with
     * text styles due to code format exclusivity.
     * </p>
     * <p>
     * Validates: Requirements 13.1, 13.2, 13.4
     * </p>
     *
     * @param editable    The editable text to apply formatting to.
     * @param start       The start index of the range to format.
     * @param end         The end index of the range to format.
     * @param formatTypes The set of format types to apply.
     */
    public static void applyMultipleFormats(Editable editable, int start, int end, Set<FormatType> formatTypes) {
        if (editable == null || formatTypes == null || formatTypes.isEmpty()) {
            return;
        }

        // Check if any code format is being applied
        boolean hasCodeFormat = formatTypes.stream().anyMatch(RichTextSpanManager::isCodeFormat);

        if (hasCodeFormat) {
            // If code format is being applied, only apply the code format (ignore text styles)
            for (FormatType formatType : formatTypes) {
                if (isCodeFormat(formatType)) {
                    applyFormat(editable, start, end, formatType);
                    break; // Only apply one code format
                }
            }
        } else {
            // Apply all text styles
            for (FormatType formatType : formatTypes) {
                if (isTextStyleFormat(formatType)) {
                    applyFormat(editable, start, end, formatType);
                }
            }
        }
    }

    /**
     * Gets all active text style formats in the specified range.
     * <p>
     * Returns only text style formats (bold, italic, strikethrough) that are
     * active in the range.
     * </p>
     *
     * @param editable The editable text to check.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     * @return A set of active text style format types in the range.
     */
    @NonNull
    public static Set<FormatType> getTextStylesInRange(Editable editable, int start, int end) {
        Set<FormatType> textStyles = EnumSet.noneOf(FormatType.class);

        if (editable == null) {
            return textStyles;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        RichTextFormatSpan[] spans = editable.getSpans(normalizedStart, normalizedEnd, RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            FormatType formatType = span.getFormatType();
            if (isTextStyleFormat(formatType)) {
                textStyles.add(formatType);
            }
        }

        return textStyles;
    }

    /**
     * Detects which formats are active at the cursor position by examining spans.
     * <p>
     * Returns a set of all format types that have spans covering the cursor position.
     * For a span to be considered active at a position, the position must be within
     * the span's range (spanStart <= position < spanEnd).
     * </p>
     *
     * @param spannable      The spannable text to examine.
     * @param cursorPosition The cursor position to check.
     * @return A set of active format types at the cursor position.
     */
    @NonNull
    public static Set<FormatType> detectActiveFormats(Spannable spannable, int cursorPosition) {
        Set<FormatType> activeFormats = EnumSet.noneOf(FormatType.class);

        if (spannable == null || cursorPosition < 0 || cursorPosition > spannable.length()) {
            return activeFormats;
        }

        // Get all RichTextFormatSpan instances at the cursor position
        // We need to check spans that contain the cursor position
        RichTextFormatSpan[] spans = spannable.getSpans(0, spannable.length(), RichTextFormatSpan.class);

        for (RichTextFormatSpan span : spans) {
            int spanStart = spannable.getSpanStart(span);
            int spanEnd = spannable.getSpanEnd(span);

            // Check if cursor is within the span
            // For cursor at position P, span is active if spanStart <= P < spanEnd
            // Special case: if cursor is at end of text and span ends there, include it
            if (spanStart <= cursorPosition && cursorPosition <= spanEnd) {
                // Only include if cursor is strictly inside, or at the end of a non-empty span
                if (cursorPosition < spanEnd || (cursorPosition == spanEnd && spanStart < spanEnd)) {
                    activeFormats.add(span.getFormatType());
                }
            }
        }

        return activeFormats;
    }

    /**
     * Checks if a format exists anywhere in the specified range.
     *
     * @param editable   The editable text to check.
     * @param start      The start index of the range.
     * @param end        The end index of the range.
     * @param formatType The format type to check for.
     * @return true if the format exists in the range, false otherwise.
     */
    public static boolean hasFormatInRange(Editable editable, int start, int end, FormatType formatType) {
        if (editable == null || formatType == null) {
            return false;
        }

        RichTextFormatSpan[] spans = editable.getSpans(start, end, RichTextFormatSpan.class);
        for (RichTextFormatSpan span : spans) {
            if (span.getFormatType() == formatType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a span instance for the given format type.
     *
     * @param formatType The format type to create a span for.
     * @return A new span instance, or null if the format type is not supported.
     */
    private static RichTextFormatSpan createSpanForFormat(FormatType formatType) {
        return createSpanForFormat(formatType, null);
    }

    /**
     * Creates a new span instance for the given format type.
     * Package-visible for use by FormatSpanWatcher when splitting spans around emojis.
     *
     * @param formatType The format type to create a span for.
     * @return A new span instance, or null if the format type is not supported.
     */
    public static RichTextFormatSpan createNewSpanForFormat(FormatType formatType) {
        return createSpanForFormat(formatType, null);
    }

    /**
     * Creates a span instance for the given format type with optional context.
     * <p>
     * When context is provided, spans like INLINE_CODE and CODE_BLOCK will use
     * theme colors for proper styling in the composer.
     * </p>
     *
     * @param formatType The format type to create a span for.
     * @param context    Optional context for theme-aware styling.
     * @return A new span instance, or null if the format type is not supported.
     */
    private static RichTextFormatSpan createSpanForFormat(FormatType formatType, android.content.Context context) {
        if (formatType == null) {
            return null;
        }

        switch (formatType) {
            case BOLD:
                return new BoldFormatSpan();
            case ITALIC:
                return new ItalicFormatSpan();
            case STRIKETHROUGH:
                return new StrikethroughFormatSpan();
            case UNDERLINE:
                return new UnderlineFormatSpan();
            case INLINE_CODE:
                return context != null ? new InlineCodeFormatSpan(context) : new InlineCodeFormatSpan();
            case CODE_BLOCK:
                return context != null ? new CodeBlockFormatSpan(context) : new CodeBlockFormatSpan();
            case LINK:
                // Link requires URL, return a span with empty URL
                return new LinkFormatSpan("");
            case BULLET_LIST:
                return new BulletListFormatSpan();
            case ORDERED_LIST:
                // Numbered list requires a number, default to 1
                return new NumberedListFormatSpan(1);
            case BLOCKQUOTE:
                return context != null ? new BlockquoteFormatSpan(context) : new BlockquoteFormatSpan();
            default:
                return null;
        }
    }

    /**
     * Creates a link span with the specified URL.
     * <p>
     * This is a convenience method for creating link spans with a URL.
     * </p>
     *
     * @param editable The editable text to apply the link to.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     * @param url      The URL for the link.
     */
    public static void applyLinkFormat(Editable editable, int start, int end, String url) {
        if (editable == null || url == null) {
            return;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        // Don't apply span for empty selection
        if (normalizedStart >= normalizedEnd) {
            return;
        }

        LinkFormatSpan span = new LinkFormatSpan(url);
        editable.setSpan(span, normalizedStart, normalizedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Gets the URL from a LinkFormatSpan in the given range.
     *
     * @param editable The editable text to search.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     * @return The URL string, or null if no link span is found.
     */
    @Nullable
    public static String getLinkUrl(Editable editable, int start, int end) {
        if (editable == null) {
            return null;
        }
        LinkFormatSpan[] spans = editable.getSpans(start, end, LinkFormatSpan.class);
        if (spans != null && spans.length > 0) {
            return spans[0].getUrl();
        }
        return null;
    }

    /**
     * Creates a numbered list span with the specified starting number.
     * <p>
     * This method applies numbered list format to each line in the range,
     * with incrementing numbers starting from the specified number.
     * </p>
     *
     * @param editable The editable text to apply the format to.
     * @param start    The start index of the range.
     * @param end      The end index of the range.
     * @param number   The starting number for the list items.
     */
    public static void applyNumberedListFormat(Editable editable, int start, int end, int number) {
        if (editable == null) {
            return;
        }

        // Normalize range
        int normalizedStart = Math.min(start, end);
        int normalizedEnd = Math.max(start, end);

        // Clamp to valid bounds
        normalizedStart = Math.max(0, normalizedStart);
        normalizedEnd = Math.min(editable.length(), normalizedEnd);

        // Don't apply span for empty selection
        if (normalizedStart >= normalizedEnd) {
            return;
        }

        // Apply numbered list format per line
        String text = editable.toString();
        int lineNumber = number;
        int lineStart = normalizedStart;
        
        // Find the start of the first line
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        
        // Process each line in the range
        while (lineStart < normalizedEnd && lineStart < text.length()) {
            // Find end of current line
            int lineEnd = lineStart;
            while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
                lineEnd++;
            }
            
            // Only apply span if line has content
            if (lineEnd > lineStart) {
                NumberedListFormatSpan span = new NumberedListFormatSpan(lineNumber);
                editable.setSpan(span, lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lineNumber++;
            }
            
            // Move to next line
            lineStart = lineEnd + 1;
        }
    }
}
