package com.cometchat.chatuikit.shared.views.richtexttoolbar;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RichTextFormatterManager is a utility class that handles markdown format application,
 * removal, and detection for rich text formatting in the message composer.
 * <p>
 * This class provides static methods to:
 * <ul>
 *     <li>Apply formatting to selected text or insert markers at cursor position</li>
 *     <li>Toggle formatting on/off for selected text</li>
 *     <li>Remove formatting from selected text</li>
 *     <li>Detect which formats are active at a given cursor position</li>
 * </ul>
 * </p>
 * <p>
 * The class uses regex patterns to detect formatted regions in text and supports
 * all format types defined in {@link FormatType}.
 * </p>
 *
 * @see FormatType
 * @see com.cometchat.chatuikit.shared.formatters.richtext.FormattedResult
 */
public class RichTextFormatterManager {

    /**
     * Regex pattern for detecting bold text wrapped with ** markers.
     * Matches: **text**
     */
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");

    /**
     * Regex pattern for detecting italic text wrapped with _ markers.
     * Matches: _text_
     */
    private static final Pattern ITALIC_PATTERN = Pattern.compile("_(.+?)_");

    /**
     * Regex pattern for detecting strikethrough text wrapped with ~~ markers.
     * Matches: ~~text~~
     */
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(.+?)~~");

    /**
     * Regex pattern for detecting inline code wrapped with single backtick markers.
     * Matches: `text`
     */
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`(.+?)`");

    /**
     * Regex pattern for detecting code blocks wrapped with triple backticks.
     * Matches: ```text``` (with optional newlines)
     */
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```\\n?(.+?)\\n?```", Pattern.DOTALL);

    /**
     * Regex pattern for detecting markdown links.
     * Matches: [text](url)
     */
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.+?)\\]\\((.+?)\\)");

    /**
     * Regex pattern for detecting bullet list items.
     * Matches lines starting with "- "
     */
    private static final Pattern BULLET_LIST_PATTERN = Pattern.compile("^- (.+)$", Pattern.MULTILINE);

    /**
     * Regex pattern for detecting ordered list items.
     * Matches lines starting with "1. ", "2. ", etc.
     */
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^\\d+\\. (.+)$", Pattern.MULTILINE);

    /**
     * Regex pattern for detecting blockquote lines.
     * Matches lines starting with "> "
     */
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^> (.+)$", Pattern.MULTILINE);

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private RichTextFormatterManager() {
        // Utility class - prevent instantiation
    }

    /**
     * Applies the specified format to the editable text.
     * <p>
     * If there is no selection (selectionStart == selectionEnd), inserts format markers
     * at the cursor position. If there is a selection, wraps the selected text with
     * the format markers.
     * </p>
     *
     * @param editable       The editable text to format.
     * @param selectionStart The start position of the selection or cursor.
     * @param selectionEnd   The end position of the selection or cursor.
     * @param formatType     The type of format to apply.
     * @return The new cursor position after formatting.
     */
    public static int applyFormat(@NonNull Editable editable, int selectionStart,
                                  int selectionEnd, @NonNull FormatType formatType) {
        if (selectionStart == selectionEnd) {
            return insertMarkersAtCursor(editable, selectionStart, formatType);
        } else {
            return wrapSelection(editable, selectionStart, selectionEnd, formatType);
        }
    }

    /**
     * Toggles the specified format on the selected text.
     * <p>
     * If the selected text is already wrapped with the format markers, removes them.
     * Otherwise, applies the format to the selection.
     * </p>
     *
     * @param editable       The editable text to format.
     * @param selectionStart The start position of the selection.
     * @param selectionEnd   The end position of the selection.
     * @param formatType     The type of format to toggle.
     * @return The new cursor position after toggling.
     */
    public static int toggleFormat(@NonNull Editable editable, int selectionStart,
                                   int selectionEnd, @NonNull FormatType formatType) {
        if (selectionStart == selectionEnd) {
            // No selection - just insert markers
            return insertMarkersAtCursor(editable, selectionStart, formatType);
        }

        String selectedText = editable.subSequence(selectionStart, selectionEnd).toString();

        if (formatType.isWrapped(selectedText)) {
            return removeFormat(editable, selectionStart, selectionEnd, formatType);
        } else {
            return applyFormat(editable, selectionStart, selectionEnd, formatType);
        }
    }

    /**
     * Removes the format markers from the selected text.
     * <p>
     * If the selected text is wrapped with the format markers, removes them and
     * returns the unwrapped text. If not wrapped, returns the original selection end.
     * </p>
     *
     * @param editable       The editable text to modify.
     * @param selectionStart The start position of the selection.
     * @param selectionEnd   The end position of the selection.
     * @param formatType     The type of format to remove.
     * @return The new cursor position after removing the format.
     */
    public static int removeFormat(@NonNull Editable editable, int selectionStart,
                                   int selectionEnd, @NonNull FormatType formatType) {
        if (selectionStart >= selectionEnd || selectionStart < 0 || selectionEnd > editable.length()) {
            return selectionEnd;
        }

        String selectedText = editable.subSequence(selectionStart, selectionEnd).toString();

        if (!formatType.isWrapped(selectedText)) {
            return selectionEnd;
        }

        String unwrappedText = formatType.unwrap(selectedText);
        editable.replace(selectionStart, selectionEnd, unwrappedText);

        // Return cursor position at end of unwrapped text
        return selectionStart + unwrappedText.length();
    }

    /**
     * Detects which formats are active at the given cursor position.
     * <p>
     * Scans the text for all format patterns and checks if the cursor position
     * falls within any formatted region.
     * </p>
     *
     * @param text           The text to analyze.
     * @param cursorPosition The cursor position to check.
     * @return A set of FormatType values that are active at the cursor position.
     */
    @NonNull
    public static Set<FormatType> detectActiveFormats(@NonNull String text, int cursorPosition) {
        Set<FormatType> activeFormats = EnumSet.noneOf(FormatType.class);

        if (text.isEmpty() || cursorPosition < 0 || cursorPosition > text.length()) {
            return activeFormats;
        }

        if (isInsideFormat(text, cursorPosition, BOLD_PATTERN)) {
            activeFormats.add(FormatType.BOLD);
        }
        if (isInsideFormat(text, cursorPosition, ITALIC_PATTERN)) {
            activeFormats.add(FormatType.ITALIC);
        }
        if (isInsideFormat(text, cursorPosition, STRIKETHROUGH_PATTERN)) {
            activeFormats.add(FormatType.STRIKETHROUGH);
        }
        if (isInsideFormat(text, cursorPosition, INLINE_CODE_PATTERN)) {
            activeFormats.add(FormatType.INLINE_CODE);
        }
        if (isInsideFormat(text, cursorPosition, CODE_BLOCK_PATTERN)) {
            activeFormats.add(FormatType.CODE_BLOCK);
        }
        if (isInsideFormat(text, cursorPosition, LINK_PATTERN)) {
            activeFormats.add(FormatType.LINK);
        }
        if (isInsideFormat(text, cursorPosition, BULLET_LIST_PATTERN)) {
            activeFormats.add(FormatType.BULLET_LIST);
        }
        if (isInsideFormat(text, cursorPosition, ORDERED_LIST_PATTERN)) {
            activeFormats.add(FormatType.ORDERED_LIST);
        }
        if (isInsideFormat(text, cursorPosition, BLOCKQUOTE_PATTERN)) {
            activeFormats.add(FormatType.BLOCKQUOTE);
        }

        return activeFormats;
    }

    /**
     * Applies visual spans to the editable text based on markdown syntax.
     * <p>
     * This method scans the text for markdown patterns and applies corresponding
     * Android text spans to render the formatting visually in the input field.
     * For example, text wrapped with ** will appear bold, text wrapped with _ will
     * appear italic, etc.
     * </p>
     * <p>
     * Note: This method removes existing formatting spans before applying new ones
     * to avoid duplicate spans.
     * </p>
     *
     * @param editable The editable text to apply visual formatting to.
     */
    public static void applyVisualFormatting(@NonNull Editable editable) {
        if (editable.length() == 0) {
            return;
        }

        // Remove existing formatting spans to avoid duplicates
        removeAllFormattingSpans(editable);

        String text = editable.toString();

        // Apply bold formatting (**text**)
        applySpanForPattern(editable, text, BOLD_PATTERN, () -> new StyleSpan(Typeface.BOLD));

        // Apply italic formatting (_text_)
        applySpanForPattern(editable, text, ITALIC_PATTERN, () -> new StyleSpan(Typeface.ITALIC));

        // Apply strikethrough formatting (~~text~~)
        applySpanForPattern(editable, text, STRIKETHROUGH_PATTERN, StrikethroughSpan::new);

        // Apply inline code formatting (`text`)
        applySpanForPattern(editable, text, INLINE_CODE_PATTERN, () -> new TypefaceSpan("monospace"));

        // Apply code block formatting (```text```)
        applySpanForPattern(editable, text, CODE_BLOCK_PATTERN, () -> new TypefaceSpan("monospace"));
    }

    /**
     * Removes all formatting spans from the editable text.
     *
     * @param editable The editable text to remove spans from.
     */
    private static void removeAllFormattingSpans(@NonNull Editable editable) {
        // Remove StyleSpans (bold, italic)
        StyleSpan[] styleSpans = editable.getSpans(0, editable.length(), StyleSpan.class);
        for (StyleSpan span : styleSpans) {
            editable.removeSpan(span);
        }

        // Remove StrikethroughSpans
        StrikethroughSpan[] strikethroughSpans = editable.getSpans(0, editable.length(), StrikethroughSpan.class);
        for (StrikethroughSpan span : strikethroughSpans) {
            editable.removeSpan(span);
        }

        // Remove TypefaceSpans (monospace for code)
        TypefaceSpan[] typefaceSpans = editable.getSpans(0, editable.length(), TypefaceSpan.class);
        for (TypefaceSpan span : typefaceSpans) {
            editable.removeSpan(span);
        }
    }

    /**
     * Applies a span to all matches of the given pattern in the text.
     *
     * @param editable     The editable text to apply spans to.
     * @param text         The text string to search for patterns.
     * @param pattern      The regex pattern to match.
     * @param spanSupplier A supplier that creates new span instances.
     */
    private static void applySpanForPattern(@NonNull Editable editable, @NonNull String text,
                                            @NonNull Pattern pattern, @NonNull SpanSupplier spanSupplier) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // Apply span to the entire match (including markers)
            // The visual effect will be applied to the content between markers
            int start = matcher.start();
            int end = matcher.end();

            // For patterns with groups, we want to style the content (group 1)
            // but we need to be careful about the indices
            if (matcher.groupCount() >= 1) {
                int contentStart = matcher.start(1);
                int contentEnd = matcher.end(1);
                if (contentStart >= 0 && contentEnd >= 0 && contentStart < contentEnd) {
                    editable.setSpan(spanSupplier.create(), contentStart, contentEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                editable.setSpan(spanSupplier.create(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Functional interface for creating span instances.
     */
    private interface SpanSupplier {
        Object create();
    }

    /**
     * Inserts format markers at the cursor position.
     * <p>
     * Inserts the prefix and suffix markers at the cursor position and places
     * the cursor between them so the user can type formatted text.
     * </p>
     *
     * @param editable       The editable text to modify.
     * @param cursorPosition The position to insert markers.
     * @param formatType     The type of format to insert.
     * @return The new cursor position (between the markers).
     */
    private static int insertMarkersAtCursor(@NonNull Editable editable, int cursorPosition,
                                             @NonNull FormatType formatType) {
        String prefix = formatType.getPrefix();
        String suffix = formatType.getSuffix();

        // Insert prefix + suffix at cursor position
        String markers = prefix + suffix;
        editable.insert(cursorPosition, markers);

        // Return cursor position between prefix and suffix
        return cursorPosition + prefix.length();
    }

    /**
     * Wraps the selected text with format markers.
     * <p>
     * For line-based formats (BULLET_LIST, ORDERED_LIST, BLOCKQUOTE), applies
     * the prefix to each line in the selection. For other formats, wraps the
     * entire selection with prefix and suffix.
     * </p>
     *
     * @param editable       The editable text to modify.
     * @param selectionStart The start position of the selection.
     * @param selectionEnd   The end position of the selection.
     * @param formatType     The type of format to apply.
     * @return The new cursor position after wrapping.
     */
    private static int wrapSelection(@NonNull Editable editable, int selectionStart,
                                     int selectionEnd, @NonNull FormatType formatType) {
        String selectedText = editable.subSequence(selectionStart, selectionEnd).toString();
        String wrappedText;

        // Handle line-based formats differently
        if (isLineBasedFormat(formatType)) {
            wrappedText = wrapLines(selectedText, formatType);
        } else {
            wrappedText = formatType.wrap(selectedText);
        }

        editable.replace(selectionStart, selectionEnd, wrappedText);

        // Return cursor position at end of wrapped text
        return selectionStart + wrappedText.length();
    }

    /**
     * Checks if the given format type is line-based.
     * <p>
     * Line-based formats apply their prefix to each line rather than wrapping
     * the entire selection.
     * </p>
     *
     * @param formatType The format type to check.
     * @return true if the format is line-based, false otherwise.
     */
    private static boolean isLineBasedFormat(@NonNull FormatType formatType) {
        return formatType == FormatType.BULLET_LIST ||
               formatType == FormatType.ORDERED_LIST ||
               formatType == FormatType.BLOCKQUOTE;
    }

    /**
     * Wraps each line in the text with the format prefix.
     * <p>
     * For ORDERED_LIST, increments the number for each line (1., 2., 3., etc.).
     * For other line-based formats, applies the same prefix to each line.
     * </p>
     *
     * @param text       The text containing lines to wrap.
     * @param formatType The line-based format type to apply.
     * @return The text with each line wrapped.
     */
    private static String wrapLines(@NonNull String text, @NonNull FormatType formatType) {
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                result.append("\n");
            }

            String line = lines[i];
            if (line.isEmpty()) {
                result.append(line);
                continue;
            }

            if (formatType == FormatType.ORDERED_LIST) {
                // Use incrementing numbers for ordered lists
                result.append(i + 1).append(". ").append(line);
            } else {
                result.append(formatType.getPrefix()).append(line);
            }
        }

        return result.toString();
    }

    /**
     * Checks if the cursor position is inside a formatted region.
     * <p>
     * Uses the provided regex pattern to find all formatted regions in the text
     * and checks if the cursor position falls within any of them.
     * </p>
     *
     * @param text           The text to search.
     * @param cursorPosition The cursor position to check.
     * @param pattern        The regex pattern for the format.
     * @return true if the cursor is inside a formatted region, false otherwise.
     */
    private static boolean isInsideFormat(@NonNull String text, int cursorPosition,
                                          @NonNull Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (cursorPosition >= matcher.start() && cursorPosition <= matcher.end()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the regex pattern for the specified format type.
     * <p>
     * This method is useful for external classes that need to detect
     * formatted regions in text.
     * </p>
     *
     * @param formatType The format type to get the pattern for.
     * @return The regex pattern for the format type, or null if not found.
     */
    @NonNull
    public static Pattern getPatternForFormat(@NonNull FormatType formatType) {
        switch (formatType) {
            case BOLD:
                return BOLD_PATTERN;
            case ITALIC:
                return ITALIC_PATTERN;
            case STRIKETHROUGH:
                return STRIKETHROUGH_PATTERN;
            case INLINE_CODE:
                return INLINE_CODE_PATTERN;
            case CODE_BLOCK:
                return CODE_BLOCK_PATTERN;
            case LINK:
                return LINK_PATTERN;
            case BULLET_LIST:
                return BULLET_LIST_PATTERN;
            case ORDERED_LIST:
                return ORDERED_LIST_PATTERN;
            case BLOCKQUOTE:
                return BLOCKQUOTE_PATTERN;
            default:
                // Return a pattern that matches nothing as fallback
                return Pattern.compile("(?!)");
        }
    }
}
