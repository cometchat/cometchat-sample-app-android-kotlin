package com.cometchat.chatuikit.shared.formatters;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.shared.spans.InlineCodeFormatSpan;
import com.cometchat.chatuikit.shared.spans.MarkdownConverter;
import com.cometchat.chatuikit.shared.spans.RichTextFormatSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Text formatter that parses markdown syntax in message text and applies
 * visual formatting spans for rich text rendering in message bubbles.
 * <p>
 * This formatter uses {@link MarkdownConverter#fromMarkdown(String)} to parse markdown
 * and create spannable text with appropriate format spans.
 * </p>
 * <p>
 * Supported markdown formats:
 * <ul>
 *   <li>Bold: **text**</li>
 *   <li>Italic: _text_</li>
 *   <li>Strikethrough: ~~text~~</li>
 *   <li>Inline code: `text`</li>
 *   <li>Code block: ```text```</li>
 *   <li>Link: [text](url)</li>
 *   <li>Bullet list: - text</li>
 *   <li>Numbered list: N. text</li>
 *   <li>Blockquote: > text</li>
 * </ul>
 * </p>
 * <p>
 * Validates: Requirements 1.1, 1.2, 1.4, 1.5, 2.1-10.2
 * </p>
 */
public class CometChatRichTextFormatter extends CometChatTextFormatter {

    /**
     * Creates a new CometChatRichTextFormatter.
     * Uses '\0' as tracking character since this formatter doesn't use suggestions.
     */
    public CometChatRichTextFormatter() {
        super('\0');
    }

    /**
     * No-op for search since this formatter doesn't use suggestions.
     *
     * @param context     The context in which this method is called.
     * @param queryString The query string (ignored).
     */
    @Override
    public void search(@Nonnull Context context, @Nullable String queryString) {
        // No-op: This formatter doesn't provide suggestions
    }

    /**
     * No-op for scroll since this formatter doesn't use suggestions.
     */
    @Override
    public void onScrollToBottom() {
        // No-op: This formatter doesn't provide suggestions
    }

    /**
     * Prepares the spannable for left (received) message bubbles.
     * Parses markdown and applies format spans while preserving existing spans.
     *
     * @param context     The context in which this method is called.
     * @param baseMessage The message being formatted.
     * @param spannable   The input spannable text.
     * @return SpannableStringBuilder with format spans applied.
     */
    @Nullable
    @Override
    public SpannableStringBuilder prepareLeftMessageBubbleSpan(
            @NonNull Context context,
            @NonNull BaseMessage baseMessage,
            SpannableStringBuilder spannable) {
        return applyMarkdownFormatting(spannable, context, false);
    }

    /**
     * Prepares the spannable for right (sent) message bubbles.
     * Parses markdown and applies format spans while preserving existing spans.
     *
     * @param context     The context in which this method is called.
     * @param baseMessage The message being formatted.
     * @param spannable   The input spannable text.
     * @return SpannableStringBuilder with format spans applied.
     */
    @Nullable
    @Override
    public SpannableStringBuilder prepareRightMessageBubbleSpan(
            @NonNull Context context,
            @NonNull BaseMessage baseMessage,
            SpannableStringBuilder spannable) {
        return applyMarkdownFormatting(spannable, context, true);
    }

    /**
     * Prepares the spannable for conversation list subtitles.
     * <p>
     * Applies markdown formatting and then truncates to the first line of
     * content. This preserves rich text rendering (bold, italic, inline code,
     * etc.) for the first block while avoiding visual artifacts from
     * block-level spans (lists, blockquotes, code blocks) being crammed
     * onto a single line.
     * </p>
     * <p>
     * {@link InlineCodeFormatSpan} (a {@link android.text.style.ReplacementSpan})
     * is replaced with simpler spans that are compatible with single-line
     * ellipsize.
     * </p>
     *
     * @param context     The context in which this method is called.
     * @param baseMessage The message being formatted.
     * @param spannable   The input spannable text.
     * @return SpannableStringBuilder with first-line markdown formatting applied.
     */
    @Nullable
    @Override
    public SpannableStringBuilder prepareConversationSpan(
            @NonNull Context context,
            @NonNull BaseMessage baseMessage,
            SpannableStringBuilder spannable) {
        if (spannable == null || spannable.length() == 0) {
            return spannable;
        }
        String originalText = spannable.toString();
        if (!containsMarkdown(originalText)) {
            return spannable;
        }
        // Apply full markdown formatting (same as message bubbles)
        SpannableStringBuilder result = applyMarkdownFormatting(spannable, context, false);
        if (result == null) {
            return spannable;
        }
        // Truncate to the first line so block-level formats don't collide
        truncateToFirstLine(result);
        // Replace InlineCodeFormatSpan (ReplacementSpan) with simpler spans
        // that are compatible with single-line ellipsize
        replaceInlineCodeSpansForConversation(result);
        return result;
    }

    /**
     * Replaces {@link InlineCodeFormatSpan} instances with simpler character-style
     * spans that work with single-line ellipsize. ReplacementSpan breaks
     * TextView's ellipsize=end behavior on Android.
     */
    private void replaceInlineCodeSpansForConversation(SpannableStringBuilder builder) {
        InlineCodeFormatSpan[] inlineCodeSpans = builder.getSpans(
                0, builder.length(), InlineCodeFormatSpan.class);
        for (InlineCodeFormatSpan span : inlineCodeSpans) {
            int start = builder.getSpanStart(span);
            int end = builder.getSpanEnd(span);
            int flags = builder.getSpanFlags(span);
            int bgColor = span.getBackgroundColor();
            int textColor = span.getTextColor();
            builder.removeSpan(span);
            builder.setSpan(new BackgroundColorSpan(bgColor), start, end, flags);
            builder.setSpan(new ForegroundColorSpan(textColor), start, end, flags);
            builder.setSpan(new TypefaceSpan("monospace"), start, end, flags);
        }
    }

    /**
     * Truncates the spannable to the first line of content.
     * <p>
     * Everything from the first newline onward is deleted. Spans that extend
     * beyond the truncation point are automatically clipped by
     * {@link SpannableStringBuilder#delete}.
     * </p>
     */
    private void truncateToFirstLine(SpannableStringBuilder builder) {
        String text = builder.toString();
        int newlineIndex = text.indexOf('\n');
        if (newlineIndex >= 0 && newlineIndex < builder.length()) {
            builder.delete(newlineIndex, builder.length());
        }
    }


    /**
     * Applies markdown formatting to the input spannable while preserving existing spans.
     * <p>
     * This method:
     * <ol>
     *   <li>Extracts existing spans from the input</li>
     *   <li>Builds a position mapping from original to parsed text</li>
     *   <li>Parses markdown using MarkdownConverter.fromMarkdown()</li>
     *   <li>Copies existing spans to the result with adjusted positions</li>
     * </ol>
     * </p>
     * <p>
     * Validates: Requirement 1.4 (preserves formatting applied by other formatters)
     * </p>
     *
     * @param spannable The input spannable text with potential markdown.
     * @return SpannableStringBuilder with markdown parsed and format spans applied.
     */
    @Nullable
    private SpannableStringBuilder applyMarkdownFormatting(@Nullable SpannableStringBuilder spannable) {
        return applyMarkdownFormatting(spannable, null, false);
    }

    /**
     * Applies markdown formatting to the input spannable while preserving existing spans.
     * <p>
     * This overload allows specifying context and bubble alignment for proper styling
     * of inline code spans in message bubbles.
     * </p>
     *
     * @param spannable      The input spannable text with potential markdown.
     * @param context        The context for accessing theme colors (may be null).
     * @param isSenderBubble True if this is for a sender (right) bubble, false for receiver (left).
     * @return SpannableStringBuilder with markdown parsed and format spans applied.
     */
    @Nullable
    private SpannableStringBuilder applyMarkdownFormatting(@Nullable SpannableStringBuilder spannable,
                                                           @Nullable Context context,
                                                           boolean isSenderBubble) {
        if (spannable == null) {
            return null;
        }

        if (spannable.length() == 0) {
            return spannable;
        }

        String originalText = spannable.toString();
        
        // Check if there's any markdown to parse
        if (!containsMarkdown(originalText)) {
            // No markdown, return the original spannable with existing spans preserved
            return spannable;
        }
        
        // Get existing spans before parsing (to preserve them)
        Object[] existingSpans = spannable.getSpans(0, spannable.length(), Object.class);
        int[] spanStarts = new int[existingSpans.length];
        int[] spanEnds = new int[existingSpans.length];
        int[] spanFlags = new int[existingSpans.length];
        
        for (int i = 0; i < existingSpans.length; i++) {
            spanStarts[i] = spannable.getSpanStart(existingSpans[i]);
            spanEnds[i] = spannable.getSpanEnd(existingSpans[i]);
            spanFlags[i] = spannable.getSpanFlags(existingSpans[i]);
        }

        // Parse markdown and get result with format spans
        SpannableString parsed = MarkdownConverter.fromMarkdown(originalText, context, isSenderBubble);
        SpannableStringBuilder result = new SpannableStringBuilder(parsed);
        String parsedText = result.toString();

        // Build position mapping from original text to parsed text
        int[] positionMap = buildPositionMap(originalText, parsedText);

        // Copy existing non-RichTextFormatSpan spans to the result with adjusted positions
        for (int i = 0; i < existingSpans.length; i++) {
            Object span = existingSpans[i];
            
            // Skip RichTextFormatSpan instances as they will be replaced by parsed ones
            if (span instanceof RichTextFormatSpan) {
                continue;
            }
            
            int originalStart = spanStarts[i];
            int originalEnd = spanEnds[i];
            int flags = spanFlags[i];
            
            // Skip invalid spans
            if (originalStart < 0 || originalEnd < 0 || originalStart >= originalEnd) {
                continue;
            }
            
            // Map positions from original to parsed text
            int newStart = mapPosition(originalStart, positionMap, parsedText.length());
            int newEnd = mapPosition(originalEnd, positionMap, parsedText.length());
            
            // Validate mapped positions
            if (newStart >= 0 && newEnd > newStart && newEnd <= result.length()) {
                try {
                    result.setSpan(span, newStart, newEnd, flags);
                } catch (Exception e) {
                    // Ignore span application errors
                }
            }
        }

        return result;
    }

    /**
     * Checks if the text contains any markdown syntax that needs to be parsed.
     * <p>
     * This is a quick check to avoid unnecessary parsing when there's no markdown.
     * </p>
     *
     * @param text The text to check.
     * @return true if the text contains markdown syntax, false otherwise.
     */
    private boolean containsMarkdown(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Check for common markdown patterns
        // Code blocks: ```
        if (text.contains("```")) {
            return true;
        }
        // Inline code: `text` - use Pattern with DOTALL to handle multiline text
        if (Pattern.compile("`[^`]+`", Pattern.DOTALL).matcher(text).find()) {
            return true;
        }
        // Bold: **text**
        if (text.contains("**")) {
            return true;
        }
        // Italic: _text_ (but not inside words) - use Pattern with DOTALL
        if (Pattern.compile("(?<![\\w])_[^_]+_(?![\\w])", Pattern.DOTALL).matcher(text).find()) {
            return true;
        }
        // Strikethrough: ~~text~~
        if (text.contains("~~")) {
            return true;
        }
        // Underline: <u>text</u>
        if (text.contains("<u>") && text.contains("</u>")) {
            return true;
        }
        // Links: [text](url) - use Pattern with DOTALL
        if (Pattern.compile("\\[[^\\]]+\\]\\([^)]+\\)", Pattern.DOTALL).matcher(text).find()) {
            return true;
        }
        // Bullet list: ^- text
        if (Pattern.compile("^- .+", Pattern.MULTILINE).matcher(text).find()) {
            return true;
        }
        // Numbered list: ^N. text
        if (Pattern.compile("^\\d+\\. .+", Pattern.MULTILINE).matcher(text).find()) {
            return true;
        }
        // Blockquote: ^> text
        if (Pattern.compile("^> .+", Pattern.MULTILINE).matcher(text).find()) {
            return true;
        }
        
        return false;
    }

    /**
     * Builds a position mapping array from original text positions to parsed text positions.
     * <p>
     * The mapping accounts for markdown markers that are removed during parsing.
     * Each index in the returned array corresponds to a position in the original text,
     * and the value at that index is the corresponding position in the parsed text.
     * A value of -1 indicates the position was inside a removed marker.
     * </p>
     *
     * @param originalText The original text with markdown markers.
     * @param parsedText   The parsed text with markers removed.
     * @return Array mapping original positions to parsed positions.
     */
    @VisibleForTesting
    static int[] buildPositionMap(String originalText, String parsedText) {
        if (originalText == null || originalText.isEmpty()) {
            return new int[0];
        }
        
        // If texts are identical, create identity mapping
        if (originalText.equals(parsedText)) {
            int[] map = new int[originalText.length() + 1];
            for (int i = 0; i <= originalText.length(); i++) {
                map[i] = i;
            }
            return map;
        }

        // Find all markdown marker regions in the original text
        List<int[]> markerRegions = findMarkdownMarkerRegions(originalText);
        
        // Build position map
        int[] map = new int[originalText.length() + 1];
        int offset = 0;
        
        for (int i = 0; i <= originalText.length(); i++) {
            // Check if this position is inside a marker region
            boolean insideMarker = false;
            for (int[] region : markerRegions) {
                if (i >= region[0] && i < region[1]) {
                    insideMarker = true;
                    break;
                }
                // Update offset when we pass a marker region
                if (i == region[1]) {
                    offset += region[1] - region[0];
                }
            }
            
            if (insideMarker) {
                map[i] = -1; // Position is inside a removed marker
            } else {
                map[i] = i - offset;
            }
        }
        
        return map;
    }

    /**
     * Finds all markdown marker regions in the text.
     * <p>
     * Returns a list of [start, end] pairs representing the positions of
     * markdown markers that will be removed during parsing.
     * </p>
     *
     * @param text The text to search for markers.
     * @return List of marker regions as [start, end] pairs.
     */
    private static List<int[]> findMarkdownMarkerRegions(String text) {
        List<int[]> regions = new ArrayList<>();
        
        // Code block markers: ```\n and \n```
        addPatternMarkers(regions, text, Pattern.compile("```\\n?"), 0);
        addPatternMarkers(regions, text, Pattern.compile("\\n?```"), 0);
        
        // Inline code markers: ` (but not inside code blocks)
        // We need to be careful not to match backticks inside code blocks
        addInlineCodeMarkers(regions, text);
        
        // Bold markers: **
        addPatternMarkers(regions, text, Pattern.compile("\\*\\*"), 0);
        
        // Italic markers: _ (but not inside words)
        addPatternMarkers(regions, text, Pattern.compile("(?<![\\w])_|_(?![\\w])"), 0);
        
        // Strikethrough markers: ~~
        addPatternMarkers(regions, text, Pattern.compile("~~"), 0);
        
        // Underline markers: <u> and </u>
        addPatternMarkers(regions, text, Pattern.compile("<u>"), 0);
        addPatternMarkers(regions, text, Pattern.compile("</u>"), 0);
        
        // Link markers: [text](url) -> we need to mark [ ] ( url )
        addLinkMarkers(regions, text);
        
        // Bullet list markers: ^- 
        addPatternMarkers(regions, text, Pattern.compile("^- ", Pattern.MULTILINE), 0);
        
        // Numbered list markers: ^N. 
        addPatternMarkers(regions, text, Pattern.compile("^\\d+\\. ", Pattern.MULTILINE), 0);
        
        // Blockquote markers: ^> 
        addPatternMarkers(regions, text, Pattern.compile("^> ", Pattern.MULTILINE), 0);
        
        // Sort regions by start position and merge overlapping ones
        regions.sort((a, b) -> Integer.compare(a[0], b[0]));
        
        return mergeOverlappingRegions(regions);
    }

    /**
     * Adds marker regions for a given pattern.
     */
    private static void addPatternMarkers(List<int[]> regions, String text, Pattern pattern, int group) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            regions.add(new int[]{matcher.start(group), matcher.end(group)});
        }
    }

    /**
     * Adds inline code marker regions, avoiding code blocks.
     */
    private static void addInlineCodeMarkers(List<int[]> regions, String text) {
        // Find code block regions first
        List<int[]> codeBlockRegions = new ArrayList<>();
        Matcher codeBlockMatcher = Pattern.compile("```[\\s\\S]*?```").matcher(text);
        while (codeBlockMatcher.find()) {
            codeBlockRegions.add(new int[]{codeBlockMatcher.start(), codeBlockMatcher.end()});
        }
        
        // Find inline code markers, excluding those inside code blocks
        Matcher inlineCodeMatcher = Pattern.compile("`([^`]+)`").matcher(text);
        while (inlineCodeMatcher.find()) {
            int start = inlineCodeMatcher.start();
            int end = inlineCodeMatcher.end();
            
            // Check if inside a code block
            boolean insideCodeBlock = false;
            for (int[] codeBlock : codeBlockRegions) {
                if (start >= codeBlock[0] && end <= codeBlock[1]) {
                    insideCodeBlock = true;
                    break;
                }
            }
            
            if (!insideCodeBlock) {
                // Add opening backtick
                regions.add(new int[]{start, start + 1});
                // Add closing backtick
                regions.add(new int[]{end - 1, end});
            }
        }
    }

    /**
     * Adds link marker regions for [text](url) syntax.
     */
    private static void addLinkMarkers(List<int[]> regions, String text) {
        Matcher linkMatcher = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)").matcher(text);
        while (linkMatcher.find()) {
            int fullStart = linkMatcher.start();
            int textStart = linkMatcher.start(1);
            int textEnd = linkMatcher.end(1);
            int fullEnd = linkMatcher.end();
            
            // Add [ marker
            regions.add(new int[]{fullStart, textStart});
            // Add ](url) marker
            regions.add(new int[]{textEnd, fullEnd});
        }
    }

    /**
     * Merges overlapping regions in the list.
     */
    private static List<int[]> mergeOverlappingRegions(List<int[]> regions) {
        if (regions.isEmpty()) {
            return regions;
        }
        
        List<int[]> merged = new ArrayList<>();
        int[] current = regions.get(0);
        
        for (int i = 1; i < regions.size(); i++) {
            int[] next = regions.get(i);
            if (next[0] <= current[1]) {
                // Overlapping or adjacent, merge
                current[1] = Math.max(current[1], next[1]);
            } else {
                // Not overlapping, add current and move to next
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        
        return merged;
    }

    /**
     * Maps a position from original text to parsed text using the position map.
     * <p>
     * If the position falls inside a removed marker, finds the nearest valid position.
     * </p>
     *
     * @param originalPos The position in the original text.
     * @param positionMap The position mapping array.
     * @param maxPos      The maximum valid position in parsed text.
     * @return The mapped position in parsed text, or -1 if invalid.
     */
    @VisibleForTesting
    static int mapPosition(int originalPos, int[] positionMap, int maxPos) {
        if (positionMap == null || positionMap.length == 0) {
            return originalPos;
        }
        
        if (originalPos < 0) {
            return -1;
        }
        
        if (originalPos >= positionMap.length) {
            // Position beyond the map, return the last valid position
            return Math.min(positionMap[positionMap.length - 1], maxPos);
        }
        
        int mappedPos = positionMap[originalPos];
        
        // If position is inside a marker (-1), find nearest valid position
        if (mappedPos == -1) {
            // Search forward for a valid position
            for (int i = originalPos + 1; i < positionMap.length; i++) {
                if (positionMap[i] != -1) {
                    return Math.min(positionMap[i], maxPos);
                }
            }
            // Search backward if forward search failed
            for (int i = originalPos - 1; i >= 0; i--) {
                if (positionMap[i] != -1) {
                    return Math.min(positionMap[i], maxPos);
                }
            }
            return -1;
        }
        
        return Math.min(mappedPos, maxPos);
    }
}
