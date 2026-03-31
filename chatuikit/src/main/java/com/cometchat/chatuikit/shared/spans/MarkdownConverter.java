package com.cometchat.chatuikit.shared.spans;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MarkdownConverter handles bidirectional conversion between spannable text and markdown.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Convert spannable text with format spans to markdown string (toMarkdown)</li>
 *   <li>Parse markdown string and create spannable with format spans (fromMarkdown)</li>
 * </ul>
 * </p>
 * <p>
 * The toMarkdown method processes spans in order and generates appropriate markdown markers.
 * It handles nested spans by proper marker ordering to ensure valid markdown output.
 * </p>
 * <p>
 * Validates: Requirements 10.1-10.11, 11.1, 11.3-11.11
 * </p>
 */
public class MarkdownConverter {

    private MarkdownConverter() {
        // Private constructor to prevent instantiation
    }

    // ==================== toMarkdown Implementation ====================

    /**
     * Converts spannable text with format spans to markdown string.
     * <p>
     * Processes spans in order and generates appropriate markdown markers.
     * Handles nested spans by proper marker ordering.
     * </p>
     * <p>
     * Conversion rules:
     * <ul>
     *   <li>BoldFormatSpan → *text*</li>
     *   <li>ItalicFormatSpan → _text_</li>
     *   <li>StrikethroughFormatSpan → ~~text~~</li>
     *   <li>InlineCodeFormatSpan → `text`</li>
     *   <li>CodeBlockFormatSpan → ```\ntext\n```</li>
     *   <li>LinkFormatSpan → [text](url)</li>
     *   <li>BulletListFormatSpan → - text</li>
     *   <li>NumberedListFormatSpan → N. text</li>
     *   <li>BlockquoteFormatSpan → > text</li>
     * </ul>
     * </p>
     *
     * @param spannable The spannable text to convert.
     * @return The markdown string representation.
     */
    @NonNull
    public static String toMarkdown(@Nullable Spannable spannable) {
        if (spannable == null || spannable.length() == 0) {
            return "";
        }

        String text = spannable.toString();
        
        // Get all RichTextFormatSpan instances
        RichTextFormatSpan[] spans = spannable.getSpans(0, spannable.length(), RichTextFormatSpan.class);
        
        if (spans == null || spans.length == 0) {
            return text;
        }

        // Process line by line to handle list formats correctly
        String result = buildMarkdownLineByLine(text, spannable, spans);
        
        // Strip zero-width space placeholders used for immediate visual feedback
        result = result.replace("\u200B", "");
        
        return result;
    }
    
    /**
     * Builds markdown by processing text line by line.
     * This ensures list markers are added at the start of each line,
     * and inline formats are properly nested within list items.
     * Code blocks are handled as block-level formats, wrapping all their
     * content lines in a single ``` pair.
     * When code block + blockquote are combined, the blockquote is the outer
     * wrapper and the code block fence is nested inside it.
     */
    @NonNull
    private static String buildMarkdownLineByLine(@NonNull String text, 
                                                   @NonNull Spannable spannable,
                                                   @NonNull RichTextFormatSpan[] allSpans) {
        StringBuilder result = new StringBuilder();
        int textLength = text.length();
        int lineStart = 0;
        boolean insideCodeBlock = false;
        boolean insideBlockquoteCodeBlock = false;
        
        while (lineStart < textLength) {
            // Find the end of the current line
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd == -1) {
                lineEnd = textLength;
            }
            
            // Check if this line is inside a code block span
            boolean lineInCodeBlock = isLineInCodeBlock(spannable, allSpans, lineStart, lineEnd);
            // Check what block-level formats are on this line
            boolean lineHasListFormat = hasListFormatOnLine(spannable, allSpans, lineStart, lineEnd);
            boolean lineHasBlockquote = hasBlockquoteOnLine(spannable, allSpans, lineStart, lineEnd);
            
            // Determine if this line should be inside a code block fence:
            // - Pure code block (no other block format) → inside fence
            // - Code block + list → list is INSIDE code block → inside fence (list markers as raw text)
            // - Code block + blockquote → blockquote is OUTSIDE, code fence INSIDE blockquote
            boolean shouldBeInCodeFence = lineInCodeBlock && !lineHasBlockquote;
            boolean shouldBeInBlockquoteCodeFence = lineInCodeBlock && lineHasBlockquote;
            
            // Handle transition: leaving blockquote+code block region
            if (!shouldBeInBlockquoteCodeFence && insideBlockquoteCodeBlock) {
                result.append("> ```");
                result.append('\n');
                insideBlockquoteCodeBlock = false;
            }
            
            // Emit opening ``` when entering a code block fence region
            if (shouldBeInCodeFence && !insideCodeBlock) {
                result.append("```");
                insideCodeBlock = true;
            }
            
            // Emit closing ``` when leaving a code block fence region
            if (!shouldBeInCodeFence && insideCodeBlock) {
                result.append("```");
                result.append('\n');
                insideCodeBlock = false;
            }
            
            // Handle transition: entering blockquote+code block region
            if (shouldBeInBlockquoteCodeFence && !insideBlockquoteCodeBlock) {
                result.append("> ```");
                insideBlockquoteCodeBlock = true;
            }
            
            if (insideCodeBlock) {
                // Inside code block fence: emit raw text with list markers as plain text
                if (lineHasListFormat) {
                    // Emit list marker + content as raw text inside code block
                    String listMarker = getListMarkerForLine(spannable, allSpans, lineStart, lineEnd);
                    int contentStart = lineStart;
                    if (listMarker != null) {
                        result.append(listMarker);
                        // Skip leading placeholder spaces
                        while (contentStart < lineEnd && text.charAt(contentStart) == ' ') {
                            contentStart++;
                        }
                    }
                    result.append(text, contentStart, lineEnd);
                } else {
                    // Pure code block line: emit raw text
                    result.append(text, lineStart, lineEnd);
                }
            } else if (insideBlockquoteCodeBlock) {
                // Code block + blockquote: blockquote prefix with raw code content
                int contentStart = lineStart;
                // Skip leading placeholder spaces from blockquote span
                while (contentStart < lineEnd && text.charAt(contentStart) == ' ') {
                    contentStart++;
                }
                result.append("> ").append(text, contentStart, lineEnd);
            } else {
                // Process this line normally (no code block, or just blockquote/list)
                String lineMarkdown = processLine(text, spannable, allSpans, lineStart, lineEnd);
                result.append(lineMarkdown);
            }
            
            // Add newline if not at end of text
            if (lineEnd < textLength) {
                // Check if we're about to leave a code block — if so, close the
                // fence on the same line as the content (no trailing newline before ```)
                if (insideCodeBlock) {
                    int nextLineStart = lineEnd + 1;
                    int nextLineEnd = text.indexOf('\n', nextLineStart);
                    if (nextLineEnd == -1) nextLineEnd = textLength;
                    boolean nextLineInCodeBlock = isLineInCodeBlock(spannable, allSpans, nextLineStart, nextLineEnd);
                    boolean nextLineHasBlockquote = hasBlockquoteOnLine(spannable, allSpans, nextLineStart, nextLineEnd);
                    boolean nextInFence = nextLineInCodeBlock && !nextLineHasBlockquote;
                    if (!nextInFence) {
                        // Next line exits the code block — close fence
                        result.append("```");
                        result.append('\n');
                        insideCodeBlock = false;
                        lineStart = nextLineStart;
                        continue;
                    }
                }
                result.append('\n');
                lineStart = lineEnd + 1;
            } else {
                break;
            }
        }
        
        // Close code block if text ends inside one
        if (insideCodeBlock) {
            result.append("```");
        }
        
        // Close blockquote code block if text ends inside one
        if (insideBlockquoteCodeBlock) {
            result.append("> ```");
        }
        
        return result.toString();
    }
    
    /**
     * Checks if a line has a list format (numbered or bullet) span.
     */
    private static boolean hasListFormatOnLine(@NonNull Spannable spannable,
                                                @NonNull RichTextFormatSpan[] allSpans,
                                                int lineStart, int lineEnd) {
        for (RichTextFormatSpan span : allSpans) {
            FormatType ft = span.getFormatType();
            if (ft == FormatType.BULLET_LIST || ft == FormatType.ORDERED_LIST) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                if (spanStart <= lineStart && spanEnd >= lineEnd) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a line has a blockquote span.
     * Uses overlap detection rather than full containment, because block-level
     * spans may not start at exactly the line boundary (e.g., due to a leading
     * zero-width space placeholder character).
     */
    private static boolean hasBlockquoteOnLine(@NonNull Spannable spannable,
                                                @NonNull RichTextFormatSpan[] allSpans,
                                                int lineStart, int lineEnd) {
        for (RichTextFormatSpan span : allSpans) {
            if (span.getFormatType() == FormatType.BLOCKQUOTE) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                // Use overlap check: span overlaps with line if it starts before line ends
                // and ends after line starts
                if (spanStart < lineEnd && spanEnd > lineStart) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a line falls within a CODE_BLOCK span.
     *
     * @param spannable The spannable text.
     * @param allSpans  All format spans.
     * @param lineStart Start index of the line.
     * @param lineEnd   End index of the line.
     * @return true if the line is inside a code block span.
     */
    private static boolean isLineInCodeBlock(@NonNull Spannable spannable,
                                              @NonNull RichTextFormatSpan[] allSpans,
                                              int lineStart, int lineEnd) {
        for (RichTextFormatSpan span : allSpans) {
            if (span.getFormatType() == FormatType.CODE_BLOCK) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                // Use overlap check: the line is in the code block if the span
                // overlaps with the line range. This handles cases where the span
                // starts mid-line (e.g., after a zero-width space placeholder).
                if (lineStart == lineEnd) {
                    // Empty line: check if position is within the span
                    if (spanStart <= lineStart && spanEnd >= lineEnd && spanStart < spanEnd) {
                        return true;
                    }
                } else {
                    if (spanStart < lineEnd && spanEnd > lineStart) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Processes a single line and converts it to markdown.
     * Handles list markers separately from inline formatting.
     */
    @NonNull
    private static String processLine(@NonNull String text,
                                       @NonNull Spannable spannable,
                                       @NonNull RichTextFormatSpan[] allSpans,
                                       int lineStart,
                                       int lineEnd) {
        StringBuilder lineResult = new StringBuilder();
        
        // Check for list format on this line
        String listMarker = getListMarkerForLine(spannable, allSpans, lineStart, lineEnd);
        
        // Determine the effective content start for this line.
        // List spans use a placeholder space at the start of the line for rendering.
        // We need to skip this placeholder so it doesn't appear in the markdown output.
        int contentStart = lineStart;
        if (listMarker != null) {
            lineResult.append(listMarker);
            
            // Skip leading placeholder space(s) that are part of the list span rendering
            while (contentStart < lineEnd && text.charAt(contentStart) == ' ') {
                contentStart++;
            }
        }
        
        // Get inline format spans for this line (excluding list formats)
        List<SpanInfo> inlineSpanInfoList = collectInlineSpansForLine(spannable, allSpans, contentStart, lineEnd);
        
        // Build markdown for inline formats
        String contentText = text.substring(contentStart, lineEnd);
        String inlineMarkdown = buildInlineMarkdown(contentText, inlineSpanInfoList, spannable, contentStart);
        lineResult.append(inlineMarkdown);
        
        return lineResult.toString();
    }
    
    /**
     * Gets the list marker for a line if it has a list format span.
     * Returns null if no list format.
     */
    @Nullable
    private static String getListMarkerForLine(@NonNull Spannable spannable,
                                                @NonNull RichTextFormatSpan[] allSpans,
                                                int lineStart,
                                                int lineEnd) {
        // Check for blockquote on this line (used as outer wrapper)
        boolean hasBlockquote = false;
        for (RichTextFormatSpan span : allSpans) {
            if (span.getFormatType() == FormatType.BLOCKQUOTE) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                // Use overlap check (consistent with hasBlockquoteOnLine)
                if (spanStart < lineEnd && spanEnd > lineStart) {
                    hasBlockquote = true;
                    break;
                }
            }
        }

        // Check for bullet list
        for (RichTextFormatSpan span : allSpans) {
            if (span.getFormatType() == FormatType.BULLET_LIST) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                // Check if this span covers this line
                if (spanStart <= lineStart && spanEnd >= lineEnd) {
                    return hasBlockquote ? "> - " : "- ";
                }
            }
        }
        
        // Check for numbered list
        for (RichTextFormatSpan span : allSpans) {
            if (span.getFormatType() == FormatType.ORDERED_LIST) {
                int spanStart = spannable.getSpanStart(span);
                int spanEnd = spannable.getSpanEnd(span);
                // Check if this span covers this line
                if (spanStart <= lineStart && spanEnd >= lineEnd) {
                    int number = 1;
                    if (span instanceof NumberedListFormatSpan) {
                        number = ((NumberedListFormatSpan) span).getNumber();
                    }
                    return hasBlockquote ? "> " + number + ". " : number + ". ";
                }
            }
        }
        
        // Blockquote only (no list)
        if (hasBlockquote) {
            return "> ";
        }
        
        return null;
    }
    
    /**
     * Collects inline format spans for a specific line.
     * Excludes list formats (BULLET_LIST, ORDERED_LIST, BLOCKQUOTE).
     */
    @NonNull
    private static List<SpanInfo> collectInlineSpansForLine(@NonNull Spannable spannable,
                                                              @NonNull RichTextFormatSpan[] allSpans,
                                                              int lineStart,
                                                              int lineEnd) {
        List<SpanInfo> spanInfoList = new ArrayList<>();
        String text = spannable.toString();
        
        for (RichTextFormatSpan span : allSpans) {
            FormatType formatType = span.getFormatType();
            
            // Skip list formats and code blocks - they're handled separately as block-level
            if (formatType == FormatType.BULLET_LIST || 
                formatType == FormatType.ORDERED_LIST ||
                formatType == FormatType.BLOCKQUOTE ||
                formatType == FormatType.CODE_BLOCK) {
                continue;
            }
            
            int spanStart = spannable.getSpanStart(span);
            int spanEnd = spannable.getSpanEnd(span);
            
            // Check if span overlaps with this line
            if (spanEnd <= lineStart || spanStart >= lineEnd) {
                continue;
            }
            
            // Clip span to line boundaries
            int clippedStart = Math.max(spanStart, lineStart);
            int clippedEnd = Math.min(spanEnd, lineEnd);
            
            if (clippedStart < clippedEnd) {
                // Skip text style spans that cover only emoji content
                if (RichTextSpanManager.isTextStyleFormat(formatType) &&
                    isEntirelyEmoji(text, clippedStart, clippedEnd)) {
                    continue;
                }
                
                // Add start and end markers relative to line start
                int relativeStart = clippedStart - lineStart;
                int relativeEnd = clippedEnd - lineStart;
                spanInfoList.add(new SpanInfo(relativeStart, relativeEnd, span, true));
                spanInfoList.add(new SpanInfo(relativeStart, relativeEnd, span, false));
            }
        }
        
        // Sort span info for proper marker ordering
        Collections.sort(spanInfoList, new SpanInfoComparator());
        
        return spanInfoList;
    }
    
    /**
     * Builds markdown for inline formats within a line.
     * <p>
     * For LINK spans, consecutive whitespace in the link text is normalized
     * to a single space (similar to Slack's behavior).
     * </p>
     */
    @NonNull
    private static String buildInlineMarkdown(@NonNull String lineText,
                                               @NonNull List<SpanInfo> spanInfoList,
                                               @NonNull Spannable spannable,
                                               int lineStartInText) {
        if (spanInfoList.isEmpty()) {
            return lineText;
        }
        
        StringBuilder result = new StringBuilder();
        int currentPos = 0;
        
        // Track active link spans to normalize whitespace inside links
        int linkNestingLevel = 0;
        
        for (SpanInfo info : spanInfoList) {
            int position = info.isStart() ? info.getStart() : info.getEnd();
            
            // Append text before this marker
            if (position > currentPos) {
                String textSegment = lineText.substring(currentPos, position);
                // If inside a link, normalize consecutive whitespace to single space
                if (linkNestingLevel > 0) {
                    textSegment = normalizeWhitespace(textSegment);
                }
                result.append(textSegment);
                currentPos = position;
            }
            
            // Track link span nesting
            if (info.getSpan().getFormatType() == FormatType.LINK) {
                if (info.isStart()) {
                    linkNestingLevel++;
                } else {
                    linkNestingLevel--;
                }
            }
            
            // Insert the appropriate marker
            String marker = getInlineMarker(info, spannable);
            result.append(marker);
        }
        
        // Append remaining text
        if (currentPos < lineText.length()) {
            result.append(lineText.substring(currentPos));
        }
        
        return result.toString();
    }
    
    /**
     * Normalizes consecutive whitespace characters to a single space.
     * <p>
     * This is used for link text to match Slack's behavior where multiple
     * spaces in link text are collapsed to a single space when the message
     * is sent.
     * </p>
     *
     * @param text The text to normalize.
     * @return The text with consecutive whitespace normalized to single spaces.
     */
    @NonNull
    private static String normalizeWhitespace(@NonNull String text) {
        if (text.isEmpty()) {
            return text;
        }
        // Replace consecutive whitespace with a single space
        return text.replaceAll("\\s+", " ");
    }
    
    /**
     * Gets the markdown marker for an inline format span.
     */
    @NonNull
    private static String getInlineMarker(@NonNull SpanInfo info, @NonNull Spannable spannable) {
        RichTextFormatSpan span = info.getSpan();
        FormatType formatType = span.getFormatType();
        boolean isStart = info.isStart();
        
        switch (formatType) {
            case BOLD:
                return "**";
            case ITALIC:
                return "_";
            case STRIKETHROUGH:
                return "~~";
            case UNDERLINE:
                return isStart ? "<u>" : "</u>";
            case INLINE_CODE:
                return "`";
            case CODE_BLOCK:
                return isStart ? "```\n" : "\n```";
            case LINK:
                if (isStart) {
                    return "[";
                } else {
                    String url = "";
                    if (span instanceof LinkFormatSpan) {
                        url = ((LinkFormatSpan) span).getUrl();
                    }
                    return "](" + url + ")";
                }
            default:
                return "";
        }
    }

    /**
     * Collects span information and sorts by position.
     * <p>
     * Creates SpanInfo objects for each span's start and end positions,
     * then sorts them for proper marker insertion order.
     * </p>
     *
     * @param spannable The spannable text.
     * @param spans     The array of spans to process.
     * @return Sorted list of SpanInfo objects.
     */
    @NonNull
    private static List<SpanInfo> collectAndSortSpans(@NonNull Spannable spannable, 
                                                       @NonNull RichTextFormatSpan[] spans) {
        List<SpanInfo> spanInfoList = new ArrayList<>();

        for (RichTextFormatSpan span : spans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            
            // Skip invalid spans
            if (start < 0 || end < 0 || start >= end) {
                continue;
            }

            // Add start marker info
            spanInfoList.add(new SpanInfo(start, end, span, true));
            // Add end marker info
            spanInfoList.add(new SpanInfo(start, end, span, false));
        }

        // Sort span info for proper marker ordering
        Collections.sort(spanInfoList, new SpanInfoComparator());

        return spanInfoList;
    }

    /**
     * Builds the markdown string by inserting markers at span boundaries.
     * <p>
     * For line-based formats (BLOCKQUOTE), markers are inserted at the start
     * of each line within the span, not just at the span boundaries.
     * </p>
     *
     * @param text         The original text.
     * @param spanInfoList The sorted list of span info.
     * @param spannable    The original spannable for accessing span data.
     * @return The markdown string.
     */
    @NonNull
    private static String buildMarkdownString(@NonNull String text, 
                                               @NonNull List<SpanInfo> spanInfoList,
                                               @NonNull Spannable spannable) {
        StringBuilder result = new StringBuilder();
        int currentPos = 0;

        for (SpanInfo info : spanInfoList) {
            int position = info.isStart() ? info.getStart() : info.getEnd();

            // Append text before this marker
            if (position > currentPos) {
                result.append(text.substring(currentPos, position));
                currentPos = position;
            }

            // Insert the appropriate marker
            String marker = getMarker(info, spannable);
            result.append(marker);
            
            // For line-based formats like BLOCKQUOTE, we need to add markers
            // at the start of each line within the span, not just at boundaries
            if (info.isStart() && isLineBasedFormat(info.getSpan().getFormatType())) {
                // Process the content within this span and add markers after each newline
                int spanEnd = info.getEnd();
                String spanContent = text.substring(position, spanEnd);
                String lineMarker = getLineMarker(info.getSpan().getFormatType());
                
                // Replace newlines with newline + marker for internal lines
                StringBuilder processedContent = new StringBuilder();
                for (int i = 0; i < spanContent.length(); i++) {
                    char c = spanContent.charAt(i);
                    processedContent.append(c);
                    // Add marker after newline if there's more content
                    if (c == '\n' && i < spanContent.length() - 1) {
                        processedContent.append(lineMarker);
                    }
                }
                result.append(processedContent);
                currentPos = spanEnd;
            }
        }

        // Append remaining text
        if (currentPos < text.length()) {
            result.append(text.substring(currentPos));
        }

        return result.toString();
    }
    
    /**
     * Checks if a format type is line-based (requires markers at start of each line).
     *
     * @param formatType The format type to check.
     * @return true if the format requires markers at the start of each line.
     */
    private static boolean isLineBasedFormat(FormatType formatType) {
        return formatType == FormatType.BLOCKQUOTE;
    }
    
    /**
     * Gets the line marker for a line-based format type.
     *
     * @param formatType The format type.
     * @return The marker to insert at the start of each line.
     */
    @NonNull
    private static String getLineMarker(FormatType formatType) {
        if (formatType == FormatType.BLOCKQUOTE) {
            return "> ";
        }
        return "";
    }

    /**
     * Gets the markdown marker for a span info.
     *
     * @param info      The span info.
     * @param spannable The spannable for accessing span data.
     * @return The markdown marker string.
     */
    @NonNull
    private static String getMarker(@NonNull SpanInfo info, @NonNull Spannable spannable) {
        RichTextFormatSpan span = info.getSpan();
        FormatType formatType = span.getFormatType();
        boolean isStart = info.isStart();

        switch (formatType) {
            case BOLD:
                return "**";
            case ITALIC:
                return "_";
            case STRIKETHROUGH:
                return "~~";
            case UNDERLINE:
                return isStart ? "<u>" : "</u>";
            case INLINE_CODE:
                return "`";
            case CODE_BLOCK:
                return isStart ? "```\n" : "\n```";
            case LINK:
                if (isStart) {
                    return "[";
                } else {
                    // Get URL from LinkFormatSpan
                    String url = "";
                    if (span instanceof LinkFormatSpan) {
                        url = ((LinkFormatSpan) span).getUrl();
                    }
                    return "](" + url + ")";
                }
            case BULLET_LIST:
                // Bullet list marker only at start of line
                return isStart ? "- " : "";
            case ORDERED_LIST:
                if (isStart) {
                    // Get number from NumberedListFormatSpan
                    int number = 1;
                    if (span instanceof NumberedListFormatSpan) {
                        number = ((NumberedListFormatSpan) span).getNumber();
                    }
                    return number + ". ";
                }
                return "";
            case BLOCKQUOTE:
                // Blockquote marker only at start of line
                return isStart ? "> " : "";
            default:
                return "";
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Helper class for span processing during markdown conversion.
     * <p>
     * Stores information about a span's position and whether it represents
     * a start or end marker.
     * </p>
     */
    private static class SpanInfo {
        private final int start;
        private final int end;
        private final RichTextFormatSpan span;
        private final boolean isStart;

        /**
         * Creates a new SpanInfo.
         *
         * @param start   The start position of the span.
         * @param end     The end position of the span.
         * @param span    The span object.
         * @param isStart Whether this represents the start marker.
         */
        SpanInfo(int start, int end, RichTextFormatSpan span, boolean isStart) {
            this.start = start;
            this.end = end;
            this.span = span;
            this.isStart = isStart;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }

        RichTextFormatSpan getSpan() {
            return span;
        }

        boolean isStart() {
            return isStart;
        }

        /**
         * Gets the position this info represents (start or end).
         *
         * @return The position.
         */
        int getPosition() {
            return isStart ? start : end;
        }

        /**
         * Gets the span length.
         *
         * @return The length of the span.
         */
        int getLength() {
            return end - start;
        }
    }

    /**
     * Comparator for sorting SpanInfo objects.
     * <p>
     * Sorting rules:
     * <ol>
     *   <li>Sort by position (ascending)</li>
     *   <li>At same position: end markers before start markers (for proper nesting)</li>
     *   <li>For start markers at same position: list formats first, then inline formats</li>
     *   <li>For end markers at same position: longer spans first (outer closes first)</li>
     *   <li>For start markers at same position: shorter spans first (inner opens first)</li>
     * </ol>
     * </p>
     */
    private static class SpanInfoComparator implements Comparator<SpanInfo> {
        
        /**
         * Returns a priority index for inline format types.
         * Opening markers are ordered by this priority (ascending).
         * Closing markers are ordered in reverse (descending) for proper LIFO nesting.
         * e.g., open: **_~~<u>  close: </u>~~_**
         */
        private int getFormatPriority(FormatType formatType) {
            switch (formatType) {
                case BOLD: return 0;
                case ITALIC: return 1;
                case STRIKETHROUGH: return 2;
                case UNDERLINE: return 3;
                case INLINE_CODE: return 4;
                case CODE_BLOCK: return 5;
                case LINK: return 6;
                default: return 10;
            }
        }
        
        @Override
        public int compare(SpanInfo a, SpanInfo b) {
            int posA = a.getPosition();
            int posB = b.getPosition();

            // First, sort by position
            if (posA != posB) {
                return Integer.compare(posA, posB);
            }

            // At same position: end markers come before start markers
            // This ensures proper nesting: close inner spans before opening new ones
            if (a.isStart() != b.isStart()) {
                return a.isStart() ? 1 : -1; // End markers (-1) before start markers (1)
            }

            // Both are start markers or both are end markers at same position
            if (a.isStart()) {
                // For start markers: list formats must come FIRST
                boolean aIsList = isListFormat(a.getSpan().getFormatType());
                boolean bIsList = isListFormat(b.getSpan().getFormatType());
                
                if (aIsList != bIsList) {
                    return aIsList ? -1 : 1;
                }
                
                // For start markers with different lengths: shorter spans first (inner)
                int lengthCompare = Integer.compare(a.getLength(), b.getLength());
                if (lengthCompare != 0) {
                    return lengthCompare;
                }
                
                // Same length: use format priority (ascending for open markers)
                return Integer.compare(
                    getFormatPriority(a.getSpan().getFormatType()),
                    getFormatPriority(b.getSpan().getFormatType()));
            } else {
                // For end markers with different lengths: longer spans first (outer closes first)
                int lengthCompare = Integer.compare(b.getLength(), a.getLength());
                if (lengthCompare != 0) {
                    return lengthCompare;
                }
                
                // Same length: reverse format priority (descending for close markers)
                // This ensures LIFO: if open is **_~~<u> then close is </u>~~_**
                return Integer.compare(
                    getFormatPriority(b.getSpan().getFormatType()),
                    getFormatPriority(a.getSpan().getFormatType()));
            }
        }
        
        /**
         * Checks if a format type is a list format that should come first.
         *
         * @param formatType The format type to check.
         * @return true if the format is a list format (BULLET_LIST, ORDERED_LIST, BLOCKQUOTE).
         */
        private boolean isListFormat(FormatType formatType) {
            return formatType == FormatType.BULLET_LIST || 
                   formatType == FormatType.ORDERED_LIST ||
                   formatType == FormatType.BLOCKQUOTE;
        }
    }

    // ==================== fromMarkdown Implementation ====================

    // Regex patterns for markdown parsing
    // Code block pattern must be processed first to avoid conflicts with inline code
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```\\n?([\\s\\S]*?)\\n?```");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<![\\*_])_([^_]+)_(?![\\*_])");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(.+?)~~");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("<u>(.+?)</u>");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern BULLET_PATTERN = Pattern.compile("^- (.*)$|^-$", Pattern.MULTILINE);
    private static final Pattern NUMBERED_PATTERN = Pattern.compile("^(\\d+)\\. (.*)$|^(\\d+)\\.$", Pattern.MULTILINE);
    // Blockquote pattern: matches "> " followed by any content, OR bare ">" (empty blockquote line)
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^> ?(.*)$", Pattern.MULTILINE);
    // Blockquote-wrapped code block: > ``` ... > ```
    // Handles content lines prefixed with "> " and possible empty lines
    private static final Pattern BLOCKQUOTE_CODE_BLOCK_PATTERN = Pattern.compile(
            "^> ```\\n((?:(?:> .*|)\\n)*?)> ```$", Pattern.MULTILINE);

    /**
     * Parses markdown string and creates spannable with format spans.
     * <p>
     * Uses regex patterns to detect markdown syntax and apply spans.
     * Removes markdown markers from visible text.
     * </p>
     * <p>
     * Parsing order is important:
     * <ol>
     *   <li>Code blocks (to avoid parsing markdown inside code)</li>
     *   <li>Inline code (to avoid parsing markdown inside code)</li>
     *   <li>Links (to avoid parsing markdown inside link text)</li>
     *   <li>Text styles (bold, italic, strikethrough)</li>
     *   <li>Block formats (bullet list, numbered list, blockquote)</li>
     * </ol>
     * </p>
     * <p>
     * Validates: Requirements 11.1, 11.3-11.11
     * </p>
     *
     * @param markdown The markdown string to parse.
     * @return SpannableString with visual formatting applied and no visible markdown markers.
     */
    @NonNull
    public static SpannableString fromMarkdown(@Nullable String markdown) {
        return fromMarkdown(markdown, null, false);
    }

    /**
     * Parses markdown string and creates spannable with format spans, with context for theming.
     * <p>
     * This overload allows specifying context and bubble alignment for proper styling
     * of inline code spans in message bubbles.
     * </p>
     *
     * @param markdown       The markdown string to parse.
     * @param context        The context for accessing theme colors (may be null).
     * @param isSenderBubble True if this is for a sender (right) bubble, false for receiver (left).
     * @return SpannableString with visual formatting applied and no visible markdown markers.
     */
    @NonNull
    public static SpannableString fromMarkdown(@Nullable String markdown, @Nullable android.content.Context context, boolean isSenderBubble) {
        if (markdown == null || markdown.isEmpty()) {
            return new SpannableString("");
        }

        // Collect all format regions to apply
        List<FormatRegion> regions = new ArrayList<>();
        
        // Working copy of the text that we'll modify as we remove markers
        StringBuilder workingText = new StringBuilder(markdown);
        
        // Process in order: blockquote code blocks first, then code blocks, then inline code, then other formats
        // This prevents parsing markdown syntax inside code
        
        // 0. Parse blockquote-wrapped code blocks (> ``` ... > ```)
        parseBlockquoteCodeBlocks(workingText, regions);
        
        // 1. Parse code blocks (```text```)
        parseCodeBlocks(workingText, regions);
        
        // 2. Parse inline code (`text`)
        parseInlineCode(workingText, regions);
        
        // 3. Parse links [text](url)
        parseLinks(workingText, regions);
        
        // 4. Parse text styles (bold, italic, strikethrough)
        parseBold(workingText, regions);
        parseItalic(workingText, regions);
        parseStrikethrough(workingText, regions);
        parseUnderline(workingText, regions);
        
        // 5. Parse block formats
        // Blockquote must be parsed BEFORE lists so that nested formats like
        // "> - item" have the "> " marker stripped first, allowing the bullet/numbered
        // list parser to detect "- item" in the cleaned text.
        parseBlockquote(workingText, regions);
        parseBulletList(workingText, regions);
        parseNumberedList(workingText, regions);
        
        // Create spannable from the cleaned text
        SpannableStringBuilder builder = new SpannableStringBuilder(workingText.toString());
        
        // Apply all format spans with context for proper styling
        for (FormatRegion region : regions) {
            applySpanForRegion(builder, region, context, isSenderBubble);
        }
        
        return new SpannableString(builder);
    }

    /**
     * Parses blockquote-wrapped code blocks (> ``` ... > ```) and removes markers.
     * Creates both BLOCKQUOTE and CODE_BLOCK regions for the content.
     */
    private static void parseBlockquoteCodeBlocks(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = BLOCKQUOTE_CODE_BLOCK_PATTERN.matcher(text);

        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String innerBlock = matcher.group(1); // the "> line\n> line\n" content

            if (innerBlock == null) {
                innerBlock = "";
            }

            // Strip "> " prefix from each inner line to get raw content
            StringBuilder contentBuilder = new StringBuilder();
            String[] lines = innerBlock.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("> ")) {
                    contentBuilder.append(line.substring(2));
                } else if (line.equals(">")) {
                    // empty blockquote line
                } else {
                    contentBuilder.append(line);
                }
                if (i < lines.length - 1) {
                    contentBuilder.append('\n');
                }
            }
            String content = contentBuilder.toString();
            // Remove trailing newline if present
            if (content.endsWith("\n")) {
                content = content.substring(0, content.length() - 1);
            }

            // Replace the entire match with just the content
            text.delete(matchStart, matchEnd);
            text.insert(matchStart, content);

            int deletedLength = (matchEnd - matchStart) - content.length();
            adjustRegionPositions(regions, matchStart, deletedLength);

            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();

            if (contentEnd > contentStart) {
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.CODE_BLOCK, null));
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.BLOCKQUOTE, null));
            }

            matcher.reset(text);
        }
    }

    /**
     * Parses code blocks (```text```) and removes markers.
     * <p>
     * This method iteratively finds and processes code blocks. After each code block
     * is processed (markers removed), the matcher is reset to search the modified text
     * from the beginning. Since we're always searching the current state of the text,
     * no offset adjustment is needed for match positions.
     * </p>
     */
    private static void parseCodeBlocks(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(text);
        
        while (matcher.find()) {
            // Match positions are in the current (possibly modified) text
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null) {
                content = "";
            }
            
            // Calculate the length of markers to remove
            // Opening: ``` or ```\n (3 or 4 chars)
            // Closing: ``` or \n``` (3 or 4 chars)
            String fullMatch = text.substring(matchStart, matchEnd);
            int openingMarkerLen = fullMatch.startsWith("```\n") ? 4 : 3;
            int closingMarkerLen = fullMatch.endsWith("\n```") ? 4 : 3;
            
            // Remove closing marker first (to preserve positions for opening marker removal)
            int closingStart = matchEnd - closingMarkerLen;
            text.delete(closingStart, matchEnd);
            
            // Remove opening marker
            text.delete(matchStart, matchStart + openingMarkerLen);
            
            // Calculate new positions after marker removal
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            // Add region for the content
            if (contentEnd > contentStart) {
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.CODE_BLOCK, null));
            }
            
            // Reset matcher with updated text to find next code block
            // Since we modified the text, we need to search from the beginning again
            matcher.reset(text);
        }
    }

    /**
     * Parses inline code (`text`) and removes markers.
     */
    private static void parseInlineCode(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = INLINE_CODE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            // Match positions are in the current (possibly modified) text
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null || content.isEmpty()) {
                continue;
            }
            
            // Check if this position is inside a code block region
            if (isInsideCodeBlock(matchStart, regions)) {
                continue;
            }
            
            // Remove closing backtick
            text.deleteCharAt(matchEnd - 1);
            // Remove opening backtick
            text.deleteCharAt(matchStart);
            
            // Calculate new positions after marker removal
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            // Add region for the content
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.INLINE_CODE, null));
            
            // Reset matcher with updated text
            matcher.reset(text);
        }
    }

    /**
     * Parses links [text](url) and removes markers.
     */
    private static void parseLinks(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = LINK_PATTERN.matcher(text);
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String linkText = matcher.group(1);
            String url = matcher.group(2);
            
            if (linkText == null || linkText.isEmpty()) {
                continue;
            }
            
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            // Calculate how much text is being removed
            int removedLength = (matchEnd - matchStart) - linkText.length();
            
            // Replace the entire [text](url) with just text
            text.delete(matchStart, matchEnd);
            text.insert(matchStart, linkText);
            
            // Adjust previously recorded regions
            // The deletion effectively removes removedLength chars starting after the link text
            adjustRegionPositions(regions, matchStart + linkText.length(), removedLength);
            
            int contentStart = matchStart;
            int contentEnd = matchStart + linkText.length();
            
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.LINK, url));
            
            matcher.reset(text);
        }
    }

    /**
     * Parses bold (**text**) and removes markers.
     */
    private static void parseBold(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = BOLD_PATTERN.matcher(text);
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null || content.isEmpty()) {
                continue;
            }
            
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            if (matchStart < 0 || matchEnd > text.length() || matchStart + 2 > text.length() || matchEnd - 2 < 0) {
                continue;
            }
            
            // Remove closing ** first (higher position, 2 chars)
            text.delete(matchEnd - 2, matchEnd);
            adjustRegionPositions(regions, matchEnd - 2, 2);
            // Remove opening ** (2 chars)
            text.delete(matchStart, matchStart + 2);
            adjustRegionPositions(regions, matchStart, 2);
            
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.BOLD, null));
            
            matcher.reset(text);
        }
    }

    /**
     * Parses italic (_text_) and removes markers.
     */
    private static void parseItalic(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = ITALIC_PATTERN.matcher(text);
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null || content.isEmpty()) {
                continue;
            }
            
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            if (matchStart < 0 || matchEnd > text.length() || matchEnd - 1 < 0) {
                continue;
            }
            
            // Remove closing _ first (higher position)
            text.deleteCharAt(matchEnd - 1);
            adjustRegionPositions(regions, matchEnd - 1, 1);
            // Remove opening _
            text.deleteCharAt(matchStart);
            adjustRegionPositions(regions, matchStart, 1);
            
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.ITALIC, null));
            
            matcher.reset(text);
        }
    }

    /**
     * Parses strikethrough (~~text~~) and removes markers.
     */
    private static void parseStrikethrough(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = STRIKETHROUGH_PATTERN.matcher(text);
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null || content.isEmpty()) {
                continue;
            }
            
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            if (matchStart < 0 || matchEnd > text.length() || matchStart + 2 > text.length() || matchEnd - 2 < 0) {
                continue;
            }
            
            // Remove closing ~~ first (higher position)
            text.delete(matchEnd - 2, matchEnd);
            adjustRegionPositions(regions, matchEnd - 2, 2);
            // Remove opening ~~
            text.delete(matchStart, matchStart + 2);
            adjustRegionPositions(regions, matchStart, 2);
            
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.STRIKETHROUGH, null));
            
            matcher.reset(text);
        }
    }

    /**
     * Parses underline (&lt;u&gt;text&lt;/u&gt;) and removes markers.
     */
    private static void parseUnderline(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = UNDERLINE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null || content.isEmpty()) {
                continue;
            }
            
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            // <u> is 3 chars, </u> is 4 chars
            if (matchStart < 0 || matchEnd > text.length() || matchStart + 3 > text.length() || matchEnd - 4 < 0) {
                continue;
            }
            
            // Remove closing </u> (4 chars) first (higher position)
            text.delete(matchEnd - 4, matchEnd);
            adjustRegionPositions(regions, matchEnd - 4, 4);
            // Remove opening <u> (3 chars)
            text.delete(matchStart, matchStart + 3);
            adjustRegionPositions(regions, matchStart, 3);
            
            // Calculate new positions after marker removal
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            // Add region for the content
            regions.add(new FormatRegion(contentStart, contentEnd, FormatType.UNDERLINE, null));
            
            // Reset matcher with updated text
            matcher.reset(text);
        }
    }

    /**
     * Parses bullet list (- text) and removes markers.
     */
    private static void parseBulletList(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = BULLET_PATTERN.matcher(text);
        
        while (matcher.find()) {
            // Match positions are in the current (possibly modified) text
            int matchStart = matcher.start();
            String content = matcher.group(1);
            
            // Check if this position is inside a code region
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            // Determine marker length:
            // "- content" or "- " → marker is "- " (2 chars)
            // bare "-" (trimmed empty line) → marker is "-" (1 char)
            int markerLength;
            if (content != null) {
                // Matched "^- (.*)$" — marker is "- " (2 chars)
                markerLength = 2;
            } else {
                // Matched "^-$" — bare dash, marker is "-" (1 char), content is empty
                markerLength = 1;
                content = "";
            }
            
            boolean isEmpty = content.isEmpty();
            
            // Replace marker with a space placeholder for empty lines so the
            // BulletListFormatSpan has a character to draw its bullet against.
            // For non-empty lines just delete the marker as before.
            if (isEmpty) {
                text.replace(matchStart, matchStart + markerLength, " ");
                // Net change: removed markerLength chars, inserted 1 char
                int netDeleted = markerLength - 1;
                if (netDeleted > 0) {
                    adjustRegionPositions(regions, matchStart, netDeleted);
                }
                int contentStart = matchStart;
                int contentEnd = matchStart + 1; // the space placeholder
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.BULLET_LIST, null));
            } else {
                text.delete(matchStart, matchStart + markerLength);
                adjustRegionPositions(regions, matchStart, markerLength);
                int contentStart = matchStart;
                int contentEnd = matchStart + content.length();
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.BULLET_LIST, null));
            }
            
            // Reset matcher with updated text
            matcher.reset(text);
        }
    }

    /**
     * Parses numbered list (N. text) and removes markers.
     */
    private static void parseNumberedList(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = NUMBERED_PATTERN.matcher(text);
        
        while (matcher.find()) {
            // Match positions are in the current (possibly modified) text
            int matchStart = matcher.start();
            String numberStr = matcher.group(1);
            String content = matcher.group(2);
            
            // Handle bare "N." (trimmed empty line) — group(3) has the number
            if (numberStr == null) {
                numberStr = matcher.group(3);
            }
            
            if (numberStr == null) {
                continue;
            }
            
            // Check if this position is inside a code region
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            int number;
            try {
                number = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                number = 1;
            }
            
            // Determine marker length:
            // "N. content" or "N. " → marker is "N. " (numberStr.length + 2)
            // bare "N." (trimmed) → marker is "N." (numberStr.length + 1)
            int markerLength;
            if (content != null) {
                markerLength = numberStr.length() + 2; // number + ". "
            } else {
                markerLength = numberStr.length() + 1; // number + "."
                content = "";
            }
            
            boolean isEmpty = content.isEmpty();
            
            // Replace marker with a space placeholder for empty lines so the
            // NumberedListFormatSpan has a character to draw its number against.
            // For non-empty lines just delete the marker as before.
            if (isEmpty) {
                text.replace(matchStart, matchStart + markerLength, " ");
                int netDeleted = markerLength - 1;
                if (netDeleted > 0) {
                    adjustRegionPositions(regions, matchStart, netDeleted);
                }
                int contentStart = matchStart;
                int contentEnd = matchStart + 1; // the space placeholder
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.ORDERED_LIST, number));
            } else {
                text.delete(matchStart, matchStart + markerLength);
                adjustRegionPositions(regions, matchStart, markerLength);
                int contentStart = matchStart;
                int contentEnd = matchStart + content.length();
                regions.add(new FormatRegion(contentStart, contentEnd, FormatType.ORDERED_LIST, number));
            }
            
            // Reset matcher with updated text
            matcher.reset(text);
        }
    }

    /**
     * Parses blockquote (> text) and removes markers.
     * Handles both content lines and empty lines within blockquotes.
     */
    private static void parseBlockquote(StringBuilder text, List<FormatRegion> regions) {
        Matcher matcher = BLOCKQUOTE_PATTERN.matcher(text);
        
        // First pass: strip "> " or ">" markers and collect per-line positions
        // We track both content ranges and empty-line positions so the merge
        // can bridge across empty blockquote lines.
        List<int[]> lineRanges = new ArrayList<>(); // [start, end] after marker removal; start==end for empty lines
        
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            String content = matcher.group(1);
            
            if (content == null) {
                content = "";
            }
            
            // Check if this position is inside a code region
            if (isInsideCodeRegion(matchStart, regions)) {
                continue;
            }
            
            // Calculate marker length: full match length minus captured content length
            // For "> text" → marker is "> " (2 chars)
            // For ">"     → marker is ">"  (1 char)
            int markerLength = (matchEnd - matchStart) - content.length();
            text.delete(matchStart, matchStart + markerLength);
            
            adjustRegionPositions(regions, matchStart, markerLength);
            
            int contentStart = matchStart;
            int contentEnd = matchStart + content.length();
            
            // Always add the range, even for empty lines (contentStart == contentEnd).
            // Empty lines act as bridges so consecutive blockquote regions merge properly.
            lineRanges.add(new int[]{contentStart, contentEnd});
            
            matcher.reset(text);
        }
        
        // Second pass: merge consecutive blockquote line ranges into continuous regions.
        // Two ranges are consecutive if the start of the next range is at most mergedEnd + 1
        // (accounting for the newline character between lines).
        if (!lineRanges.isEmpty()) {
            int mergedStart = lineRanges.get(0)[0];
            int mergedEnd = lineRanges.get(0)[1];
            // For empty first line, track position so next line can bridge
            int lastLinePos = Math.max(mergedEnd, lineRanges.get(0)[0]);
            
            for (int i = 1; i < lineRanges.size(); i++) {
                int[] range = lineRanges.get(i);
                // Check if this line is consecutive: its start is at most 1 past the
                // furthest position we've seen (content end or empty-line position)
                if (range[0] <= lastLinePos + 1) {
                    // Extend mergedEnd if this range has content
                    if (range[1] > mergedEnd) {
                        mergedEnd = range[1];
                    }
                    // Always advance lastLinePos to bridge empty lines
                    lastLinePos = Math.max(lastLinePos, Math.max(range[0], range[1]));
                } else {
                    // Gap found — emit the merged region and start a new one
                    if (mergedEnd > mergedStart) {
                        regions.add(new FormatRegion(mergedStart, mergedEnd, FormatType.BLOCKQUOTE, null));
                    }
                    mergedStart = range[0];
                    mergedEnd = range[1];
                    lastLinePos = Math.max(range[0], range[1]);
                }
            }
            // Emit the last merged region (only if it has content)
            if (mergedEnd > mergedStart) {
                regions.add(new FormatRegion(mergedStart, mergedEnd, FormatType.BLOCKQUOTE, null));
            }
        }
    }

    /**
     * Checks if a position is inside a code block region.
     */
    private static boolean isInsideCodeBlock(int position, List<FormatRegion> regions) {
        for (FormatRegion region : regions) {
            if (region.formatType == FormatType.CODE_BLOCK &&
                position >= region.start && position < region.end) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a position is inside any code region (code block or inline code).
     */
    private static boolean isInsideCodeRegion(int position, List<FormatRegion> regions) {
        for (FormatRegion region : regions) {
            if (region.formatType == FormatType.CODE_BLOCK &&
                position >= region.start && position < region.end) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the appropriate span for a format region.
     */
    private static void applySpanForRegion(SpannableStringBuilder builder, FormatRegion region) {
        applySpanForRegion(builder, region, null, false);
    }

    /**
     * Applies the appropriate span for a format region with context for theming.
     */
    private static void applySpanForRegion(SpannableStringBuilder builder, FormatRegion region, 
                                           @Nullable android.content.Context context, boolean isSenderBubble) {
        if (region.start < 0 || region.end > builder.length() || region.start >= region.end) {
            return;
        }
        
        // For code blocks, we need to apply multiple spans:
        // 1. CodeBlockFormatSpan for background
        // 2. ForegroundColorSpan for text color (dark text on light background)
        // 3. TypefaceSpan for monospace font
        if (region.formatType == FormatType.CODE_BLOCK) {
            applyCodeBlockSpans(builder, region, context, isSenderBubble);
            return;
        }
        
        // For inline code, we also need text color and monospace font
        if (region.formatType == FormatType.INLINE_CODE) {
            applyInlineCodeSpans(builder, region, context, isSenderBubble);
            return;
        }
        
        Object span = createSpanForFormatType(region.formatType, region.metadata, context, isSenderBubble);
        if (span != null) {
            builder.setSpan(span, region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Applies spans for code block formatting in message bubbles.
     * Uses purple/violet background with white text for sender bubbles,
     * and white background with dark text for receiver bubbles.
     */
    private static void applyCodeBlockSpans(SpannableStringBuilder builder, FormatRegion region, android.content.Context context, boolean isSenderBubble) {
        int backgroundColor;
        if (isSenderBubble) {
            backgroundColor = CometChatTheme.getExtendedPrimaryColor700(context);
        } else {
            backgroundColor = CometChatTheme.getBackgroundColor3(context);
        }

        CodeBlockFormatSpan codeBlockSpan = new CodeBlockFormatSpan(backgroundColor, 0, 0, 16f);
        builder.setSpan(codeBlockSpan, region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // No ForegroundColorSpan — text color is inherited from the TextView's
        // text color, which is set by the text bubble (sender/receiver aware).
        
        // Apply monospace font
        android.text.style.TypefaceSpan typefaceSpan = 
            new android.text.style.TypefaceSpan("monospace");
        builder.setSpan(typefaceSpan, region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Applies spans for inline code formatting.
     * Includes background, text color, and monospace font.
     */
    private static void applyInlineCodeSpans(SpannableStringBuilder builder, FormatRegion region) {
        applyInlineCodeSpans(builder, region, null, false);
    }

    /**
     * Applies spans for inline code formatting with context for theming.
     * Includes background, text color, and monospace font.
     * <p>
     * Styling differs based on bubble alignment:
     * <ul>
     *   <li>Receiver (left) bubble: Light gray background with primary (purple) text</li>
     *   <li>Sender (right) bubble: Semi-transparent dark background with white text</li>
     * </ul>
     * </p>
     */
    private static void applyInlineCodeSpans(SpannableStringBuilder builder, FormatRegion region,
                                              @Nullable android.content.Context context, boolean isSenderBubble) {
        // In message bubbles (context != null), use regular spans instead of
        // ReplacementSpan to avoid fragmentation when text style spans (bold,
        // italic, etc.) overlap the inline code range. ReplacementSpan gets
        // split at every overlapping span boundary, causing the background to
        // render as separate boxes.
        if (context != null) {
            int backgroundColor;
            if (isSenderBubble) {
                backgroundColor = CometChatTheme.getExtendedPrimaryColor700(context);
            } else {
                backgroundColor = CometChatTheme.getBackgroundColor3(context);
            }
            builder.setSpan(new android.text.style.BackgroundColorSpan(backgroundColor),
                    region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!isSenderBubble) {
                int textColor = CometChatTheme.getPrimaryColor(context);
                builder.setSpan(new android.text.style.ForegroundColorSpan(textColor),
                        region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            builder.setSpan(new android.text.style.TypefaceSpan("monospace"),
                    region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // In composer or when no context, use InlineCodeFormatSpan for
            // rounded background rendering
            InlineCodeFormatSpan inlineCodeSpan = new InlineCodeFormatSpan();
            builder.setSpan(inlineCodeSpan, region.start, region.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Creates the appropriate span object for a format type.
     */
    @Nullable
    private static Object createSpanForFormatType(FormatType formatType, @Nullable Object metadata) {
        return createSpanForFormatType(formatType, metadata, null, false);
    }

    @Nullable
    private static Object createSpanForFormatType(FormatType formatType, @Nullable Object metadata,
                                                   @Nullable android.content.Context context,
                                                   boolean isSenderBubble) {
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
                return new InlineCodeFormatSpan();
            case CODE_BLOCK:
                return new CodeBlockFormatSpan();
            case LINK:
                String url = metadata instanceof String ? (String) metadata : "";
                return new LinkFormatSpan(url);
            case BULLET_LIST:
                return new BulletListFormatSpan();
            case ORDERED_LIST:
                int number = metadata instanceof Integer ? (Integer) metadata : 1;
                return new NumberedListFormatSpan(number);
            case BLOCKQUOTE:
                return context != null ? new BlockquoteFormatSpan(context, isSenderBubble) : new BlockquoteFormatSpan();
            default:
                return null;
        }
    }

    /**
     * Helper class to track format regions during parsing.
     */
    private static class FormatRegion {
        int start;
        int end;
        final FormatType formatType;
        final Object metadata;

        FormatRegion(int start, int end, FormatType formatType, @Nullable Object metadata) {
            this.start = start;
            this.end = end;
            this.formatType = formatType;
            this.metadata = metadata;
        }
    }
    
    /**
     * Adjusts all previously recorded region positions after a text deletion.
     * When text is deleted at a position, all regions that start or end after
     * that position need to be shifted back by the deletion length.
     *
     * @param regions      The list of regions to adjust.
     * @param deletionPos  The position where text was deleted.
     * @param deletionLen  The number of characters deleted.
     */
    private static void adjustRegionPositions(List<FormatRegion> regions, int deletionPos, int deletionLen) {
        for (FormatRegion region : regions) {
            if (region.start >= deletionPos + deletionLen) {
                // Region is entirely after the deletion — shift both start and end
                region.start -= deletionLen;
                region.end -= deletionLen;
            } else if (region.start >= deletionPos) {
                // Region starts inside the deleted range — clamp start to deletion pos
                region.start = deletionPos;
                region.end -= deletionLen;
            } else if (region.end > deletionPos) {
                // Region spans across the deletion — only shift end
                region.end -= deletionLen;
            }
            // If region.end <= deletionPos, no adjustment needed
        }
    }

    /**
     * Checks if the text in the given range consists entirely of emoji characters.
     *
     * @param text  The full text string.
     * @param start The start index (inclusive).
     * @param end   The end index (exclusive).
     * @return true if the range contains only emoji characters (and whitespace).
     */
    private static boolean isEntirelyEmoji(String text, int start, int end) {
        if (text == null || start >= end || start < 0 || end > text.length()) {
            return false;
        }
        int i = start;
        boolean hasEmoji = false;
        while (i < end) {
            int codePoint = Character.codePointAt(text, i);
            int charCount = Character.charCount(codePoint);
            if (RichTextSpanManager.isEmojiCodePoint(codePoint) ||
                RichTextSpanManager.isEmojiModifierOrJoiner(codePoint)) {
                hasEmoji = true;
                i += charCount;
            } else if (Character.isWhitespace(codePoint)) {
                i += charCount;
            } else {
                return false;
            }
        }
        return hasEmoji;
    }

    // ==================== toHtml Implementation ====================

    /**
     * Converts markdown string to HTML for use in Android notifications.
     * <p>
     * Android notifications support basic HTML formatting via {@code Html.fromHtml()}.
     * This method converts markdown syntax to the corresponding HTML tags.
     * </p>
     * <p>
     * Supported conversions:
     * <ul>
     *   <li>*bold* → &lt;b&gt;bold&lt;/b&gt;</li>
     *   <li>_italic_ → &lt;i&gt;italic&lt;/i&gt;</li>
     *   <li>~~strikethrough~~ → &lt;s&gt;strikethrough&lt;/s&gt; (or &lt;strike&gt; for older APIs)</li>
     *   <li>`inline code` → &lt;tt&gt;inline code&lt;/tt&gt;</li>
     *   <li>```code block``` → &lt;tt&gt;code block&lt;/tt&gt;</li>
     *   <li>[text](url) → &lt;a href="url"&gt;text&lt;/a&gt;</li>
     *   <li>&lt;u&gt;underline&lt;/u&gt; → &lt;u&gt;underline&lt;/u&gt; (passed through)</li>
     *   <li>- bullet item → • bullet item</li>
     *   <li>1. numbered item → 1. numbered item</li>
     *   <li>&gt; blockquote → blockquote (prefix removed)</li>
     * </ul>
     * </p>
     * <p>
     * Note: Android notification HTML support is limited. Complex formatting like
     * nested lists or code blocks with syntax highlighting are simplified.
     * </p>
     *
     * @param markdown The markdown string to convert.
     * @return HTML string suitable for use with {@code Html.fromHtml()}.
     */
    @NonNull
    public static String toHtml(@Nullable String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String result = markdown;

        // Process code blocks first (to avoid processing markdown inside code)
        // ```code``` → <tt>code</tt>
        result = processCodeBlocksToHtml(result);

        // Process inline code
        // `code` → <tt>code</tt>
        result = processInlineCodeToHtml(result);

        // Process links
        // [text](url) → <a href="url">text</a>
        result = processLinksToHtml(result);

        // Process bold
        // **text** → <b>text</b>
        result = result.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");

        // Process italic
        // _text_ → <i>text</i>
        result = result.replaceAll("(?<![\\*_])_([^_]+)_(?![\\*_])", "<i>$1</i>");

        // Process strikethrough
        // ~~text~~ → <s>text</s>
        result = result.replaceAll("~~(.+?)~~", "<s>$1</s>");

        // Process bullet lists
        // - item → • item
        result = result.replaceAll("(?m)^- (.+)$", "• $1");

        // Process numbered lists (keep as-is, just clean up)
        // Already in "1. item" format which is readable

        // Process blockquotes
        // > text → text (remove prefix, optionally add styling)
        result = result.replaceAll("(?m)^> ?(.*)$", "$1");

        // Clean up any remaining zero-width spaces
        result = result.replace("\u200B", "");

        return result;
    }

    /**
     * Processes code blocks in markdown and converts to HTML.
     * Handles both ```code``` and ```\ncode\n``` formats.
     */
    @NonNull
    private static String processCodeBlocksToHtml(@NonNull String text) {
        // Match code blocks: ```content``` or ```\ncontent\n```
        Pattern pattern = Pattern.compile("```\\n?([\\s\\S]*?)\\n?```");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String content = matcher.group(1);
            if (content == null) {
                content = "";
            }
            // Escape HTML entities in code content
            content = escapeHtml(content);
            // Replace with <tt> tag (monospace in notifications)
            matcher.appendReplacement(result, "<tt>" + Matcher.quoteReplacement(content) + "</tt>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Processes inline code in markdown and converts to HTML.
     */
    @NonNull
    private static String processInlineCodeToHtml(@NonNull String text) {
        // Match inline code: `content`
        Pattern pattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String content = matcher.group(1);
            if (content == null) {
                content = "";
            }
            // Escape HTML entities in code content
            content = escapeHtml(content);
            // Replace with <tt> tag
            matcher.appendReplacement(result, "<tt>" + Matcher.quoteReplacement(content) + "</tt>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Processes links in markdown and converts to HTML.
     */
    @NonNull
    private static String processLinksToHtml(@NonNull String text) {
        // Match links: [text](url)
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String linkText = matcher.group(1);
            String url = matcher.group(2);
            if (linkText == null) {
                linkText = "";
            }
            if (url == null) {
                url = "";
            }
            // Escape HTML entities in link text
            linkText = escapeHtml(linkText);
            // URL should be properly encoded already
            matcher.appendReplacement(result, "<a href=\"" + Matcher.quoteReplacement(url) + "\">" + Matcher.quoteReplacement(linkText) + "</a>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Escapes HTML special characters in text.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
    @NonNull
    private static String escapeHtml(@NonNull String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Converts markdown string to a plain text representation suitable for notifications.
     * <p>
     * This method strips all markdown formatting markers and returns clean, readable text.
     * Use this when HTML formatting is not supported or desired.
     * </p>
     * <p>
     * Conversions:
     * <ul>
     *   <li>*bold* → bold</li>
     *   <li>_italic_ → italic</li>
     *   <li>~~strikethrough~~ → strikethrough</li>
     *   <li>`inline code` → inline code</li>
     *   <li>```code block``` → code block</li>
     *   <li>[text](url) → text</li>
     *   <li>&lt;u&gt;underline&lt;/u&gt; → underline</li>
     *   <li>- bullet item → • bullet item</li>
     *   <li>1. numbered item → 1. numbered item</li>
     *   <li>&gt; blockquote → blockquote</li>
     * </ul>
     * </p>
     *
     * @param markdown The markdown string to convert.
     * @return Plain text string with formatting markers removed.
     */
    @NonNull
    public static String toPlainText(@Nullable String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String result = markdown;

        // Process code blocks first
        // ```code``` → code
        result = result.replaceAll("```\\n?([\\s\\S]*?)\\n?```", "$1");

        // Process inline code
        // `code` → code
        result = result.replaceAll("`([^`]+)`", "$1");

        // Process links
        // [text](url) → text
        result = result.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        // Process bold
        // **text** → text
        result = result.replaceAll("\\*\\*(.+?)\\*\\*", "$1");

        // Process italic
        // _text_ → text
        result = result.replaceAll("(?<![\\*_])_([^_]+)_(?![\\*_])", "$1");

        // Process strikethrough
        // ~~text~~ → text
        result = result.replaceAll("~~(.+?)~~", "$1");

        // Process underline
        // <u>text</u> → text
        result = result.replaceAll("<u>(.+?)</u>", "$1");

        // Process bullet lists
        // - item → • item
        result = result.replaceAll("(?m)^- (.+)$", "• $1");

        // Process blockquotes
        // > text → text
        result = result.replaceAll("(?m)^> ?(.*)$", "$1");

        // Clean up any remaining zero-width spaces
        result = result.replace("\u200B", "");

        return result;
    }
}
