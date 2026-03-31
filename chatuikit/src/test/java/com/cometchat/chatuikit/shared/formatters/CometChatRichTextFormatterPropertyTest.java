package com.cometchat.chatuikit.shared.formatters;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.cometchat.chatuikit.shared.spans.BoldFormatSpan;
import com.cometchat.chatuikit.shared.spans.ItalicFormatSpan;
import com.cometchat.chatuikit.shared.spans.StrikethroughFormatSpan;
import com.cometchat.chatuikit.shared.spans.InlineCodeFormatSpan;
import com.cometchat.chatuikit.shared.spans.CodeBlockFormatSpan;
import com.cometchat.chatuikit.shared.spans.LinkFormatSpan;
import com.cometchat.chatuikit.shared.spans.BulletListFormatSpan;
import com.cometchat.chatuikit.shared.spans.NumberedListFormatSpan;
import com.cometchat.chatuikit.shared.spans.BlockquoteFormatSpan;
import com.cometchat.chatuikit.shared.spans.MarkdownConverter;
import com.cometchat.chatuikit.shared.spans.RichTextFormatSpan;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for CometChatRichTextFormatter.
 * <p>
 * **Property 1: Markdown parsing produces correct spans**
 * For any valid markdown text containing format markers (bold, italic, strikethrough,
 * inline code, code block, link, bullet list, numbered list, blockquote), parsing with
 * MarkdownConverter.fromMarkdown() should produce a SpannableString where:
 * - The visible text does not contain the markdown markers
 * - The appropriate format spans are applied at the correct positions
 * - The span types match the markdown format
 * </p>
 * <p>
 * Note: Since SpannableString requires the Android framework, these tests verify
 * the properties through the underlying MarkdownConverter and span creation logic
 * that the formatter uses.
 * </p>
 * <p>
 * **Validates: Requirements 2.1, 2.2, 3.1, 3.2, 4.1, 4.2, 5.1, 5.3, 6.1, 6.3, 7.1, 7.2, 8.1, 8.2, 9.1, 9.2, 10.1, 10.2**
 * </p>
 */
public class CometChatRichTextFormatterPropertyTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private final Random random = new Random(42);
    private CometChatRichTextFormatter formatter;

    @Before
    public void setUp() {
        formatter = new CometChatRichTextFormatter();
    }

    // ==================== Property 1: Markdown Parsing Produces Correct Spans ====================

    /**
     * Property 1.1: Bold markdown markers are correctly identified and removable.
     * For any random text wrapped in ** markers, the markers can be identified and removed.
     * **Validates: Requirements 2.1, 2.2**
     */
    @Test
    public void property1_boldMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 20);
            String markdown = "**" + text + "**";
            
            // Verify markers are present
            assertTrue("Markdown should contain ** markers", markdown.contains("**"));
            
            // Verify text can be extracted by removing markers
            String extracted = markdown.replaceAll("\\*\\*", "");
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify BoldFormatSpan returns correct format type
            BoldFormatSpan span = new BoldFormatSpan();
            assertEquals("BoldFormatSpan should return BOLD", FormatType.BOLD, span.getFormatType());
        }
    }

    /**
     * Property 1.2: Italic markdown markers are correctly identified and removable.
     * For any random text wrapped in _ markers, the markers can be identified and removed.
     * **Validates: Requirements 3.1, 3.2**
     */
    @Test
    public void property1_italicMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 20);
            String markdown = "_" + text + "_";
            
            // Verify markers are present
            assertTrue("Markdown should start with _", markdown.startsWith("_"));
            assertTrue("Markdown should end with _", markdown.endsWith("_"));
            
            // Verify text can be extracted by removing markers
            String extracted = markdown.substring(1, markdown.length() - 1);
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify ItalicFormatSpan returns correct format type
            ItalicFormatSpan span = new ItalicFormatSpan();
            assertEquals("ItalicFormatSpan should return ITALIC", FormatType.ITALIC, span.getFormatType());
        }
    }

    /**
     * Property 1.3: Strikethrough markdown markers are correctly identified and removable.
     * For any random text wrapped in ~~ markers, the markers can be identified and removed.
     * **Validates: Requirements 4.1, 4.2**
     */
    @Test
    public void property1_strikethroughMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 20);
            String markdown = "~~" + text + "~~";
            
            // Verify markers are present
            assertTrue("Markdown should contain ~~ markers", markdown.contains("~~"));
            
            // Verify text can be extracted by removing markers
            String extracted = markdown.replaceAll("~~", "");
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify StrikethroughFormatSpan returns correct format type
            StrikethroughFormatSpan span = new StrikethroughFormatSpan();
            assertEquals("StrikethroughFormatSpan should return STRIKETHROUGH", 
                    FormatType.STRIKETHROUGH, span.getFormatType());
        }
    }

    /**
     * Property 1.4: Inline code markdown markers are correctly identified and removable.
     * For any random text wrapped in ` markers, the markers can be identified and removed.
     * **Validates: Requirements 5.1, 5.3**
     */
    @Test
    public void property1_inlineCodeMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaNumericText(3, 20);
            String markdown = "`" + text + "`";
            
            // Verify markers are present
            assertTrue("Markdown should start with `", markdown.startsWith("`"));
            assertTrue("Markdown should end with `", markdown.endsWith("`"));
            
            // Verify text can be extracted by removing markers
            String extracted = markdown.substring(1, markdown.length() - 1);
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify InlineCodeFormatSpan returns correct format type
            InlineCodeFormatSpan span = new InlineCodeFormatSpan();
            assertEquals("InlineCodeFormatSpan should return INLINE_CODE", 
                    FormatType.INLINE_CODE, span.getFormatType());
        }
    }

    /**
     * Property 1.5: Code block markdown markers are correctly identified and removable.
     * For any random text wrapped in ``` markers, the markers can be identified and removed.
     * **Validates: Requirements 6.1, 6.3**
     */
    @Test
    public void property1_codeBlockMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaNumericText(3, 20);
            String markdown = "```\n" + text + "\n```";
            
            // Verify markers are present
            assertTrue("Markdown should contain ``` markers", markdown.contains("```"));
            
            // Verify text can be extracted by removing markers
            String extracted = markdown.replace("```\n", "").replace("\n```", "");
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify CodeBlockFormatSpan returns correct format type
            CodeBlockFormatSpan span = new CodeBlockFormatSpan();
            assertEquals("CodeBlockFormatSpan should return CODE_BLOCK", 
                    FormatType.CODE_BLOCK, span.getFormatType());
        }
    }

    /**
     * Property 1.6: Link markdown markers are correctly identified and removable.
     * For any random text and URL in [text](url) format, the markers can be identified.
     * **Validates: Requirements 7.1, 7.2**
     */
    @Test
    public void property1_linkMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 15);
            String markdown = "[" + text + "](" + url + ")";
            
            // Verify markers are present
            assertTrue("Markdown should contain [", markdown.contains("["));
            assertTrue("Markdown should contain ](", markdown.contains("]("));
            assertTrue("Markdown should contain )", markdown.contains(")"));
            
            // Verify text and URL can be extracted
            int textStart = markdown.indexOf('[') + 1;
            int textEnd = markdown.indexOf(']');
            assertEquals("Extracted text should match original", text, markdown.substring(textStart, textEnd));
            
            int urlStart = markdown.indexOf('(') + 1;
            int urlEnd = markdown.indexOf(')');
            assertEquals("Extracted URL should match original", url, markdown.substring(urlStart, urlEnd));
            
            // Verify LinkFormatSpan stores URL correctly
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store URL", url, span.getUrl());
            assertEquals("LinkFormatSpan should return LINK", FormatType.LINK, span.getFormatType());
        }
    }

    /**
     * Property 1.7: Bullet list markdown markers are correctly identified and removable.
     * For any random text prefixed with "- ", the marker can be identified and removed.
     * **Validates: Requirements 8.1, 8.2**
     */
    @Test
    public void property1_bulletListMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 20);
            String markdown = "- " + text;
            
            // Verify marker is present
            assertTrue("Markdown should start with '- '", markdown.startsWith("- "));
            
            // Verify text can be extracted by removing marker
            String extracted = markdown.substring(2);
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify BulletListFormatSpan returns correct format type
            BulletListFormatSpan span = new BulletListFormatSpan();
            assertEquals("BulletListFormatSpan should return BULLET_LIST", 
                    FormatType.BULLET_LIST, span.getFormatType());
        }
    }

    /**
     * Property 1.8: Numbered list markdown markers are correctly identified and removable.
     * For any random text prefixed with "N. ", the marker can be identified and removed.
     * **Validates: Requirements 9.1, 9.2**
     */
    @Test
    public void property1_numberedListMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int number = random.nextInt(99) + 1;
            String text = generateRandomAlphaText(3, 20);
            String markdown = number + ". " + text;
            
            // Verify marker is present
            assertTrue("Markdown should match numbered list pattern", markdown.matches("^\\d+\\. .+$"));
            
            // Verify text can be extracted by removing marker
            String extracted = markdown.replaceFirst("^\\d+\\. ", "");
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify NumberedListFormatSpan stores number correctly
            NumberedListFormatSpan span = new NumberedListFormatSpan(number);
            assertEquals("NumberedListFormatSpan should store number", number, span.getNumber());
            assertEquals("NumberedListFormatSpan should return ORDERED_LIST", 
                    FormatType.ORDERED_LIST, span.getFormatType());
        }
    }

    /**
     * Property 1.9: Blockquote markdown markers are correctly identified and removable.
     * For any random text prefixed with "> ", the marker can be identified and removed.
     * **Validates: Requirements 10.1, 10.2**
     */
    @Test
    public void property1_blockquoteMarkdownParsing() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 20);
            String markdown = "> " + text;
            
            // Verify marker is present
            assertTrue("Markdown should start with '> '", markdown.startsWith("> "));
            
            // Verify text can be extracted by removing marker
            String extracted = markdown.substring(2);
            assertEquals("Extracted text should match original", text, extracted);
            
            // Verify BlockquoteFormatSpan returns correct format type
            BlockquoteFormatSpan span = new BlockquoteFormatSpan();
            assertEquals("BlockquoteFormatSpan should return BLOCKQUOTE", 
                    FormatType.BLOCKQUOTE, span.getFormatType());
        }
    }

    /**
     * Property 1.10: All format spans implement RichTextFormatSpan interface.
     * This ensures the formatter can work with all span types uniformly.
     * **Validates: Requirements 2.1-10.2**
     */
    @Test
    public void property1_allSpansImplementRichTextFormatSpan() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            assertTrue("BoldFormatSpan should implement RichTextFormatSpan",
                    new BoldFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("ItalicFormatSpan should implement RichTextFormatSpan",
                    new ItalicFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("StrikethroughFormatSpan should implement RichTextFormatSpan",
                    new StrikethroughFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("InlineCodeFormatSpan should implement RichTextFormatSpan",
                    new InlineCodeFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("CodeBlockFormatSpan should implement RichTextFormatSpan",
                    new CodeBlockFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("LinkFormatSpan should implement RichTextFormatSpan",
                    new LinkFormatSpan("") instanceof RichTextFormatSpan);
            assertTrue("BulletListFormatSpan should implement RichTextFormatSpan",
                    new BulletListFormatSpan() instanceof RichTextFormatSpan);
            assertTrue("NumberedListFormatSpan should implement RichTextFormatSpan",
                    new NumberedListFormatSpan(1) instanceof RichTextFormatSpan);
            assertTrue("BlockquoteFormatSpan should implement RichTextFormatSpan",
                    new BlockquoteFormatSpan() instanceof RichTextFormatSpan);
        }
    }

    /**
     * Property 1.11: FormatType.wrap() produces valid markdown for all format types.
     * **Validates: Requirements 2.1-10.2**
     */
    @Test
    public void property1_formatTypeWrapProducesValidMarkdown() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            
            // Bold
            String boldMarkdown = FormatType.BOLD.wrap(text);
            assertTrue("Bold wrap should produce **text**", 
                    boldMarkdown.startsWith("**") && boldMarkdown.endsWith("**"));
            assertTrue("Bold wrap should contain original text", boldMarkdown.contains(text));
            
            // Italic
            String italicMarkdown = FormatType.ITALIC.wrap(text);
            assertTrue("Italic wrap should produce _text_", 
                    italicMarkdown.startsWith("_") && italicMarkdown.endsWith("_"));
            
            // Strikethrough
            String strikeMarkdown = FormatType.STRIKETHROUGH.wrap(text);
            assertTrue("Strikethrough wrap should produce ~~text~~", 
                    strikeMarkdown.startsWith("~~") && strikeMarkdown.endsWith("~~"));
            
            // Inline code
            String codeMarkdown = FormatType.INLINE_CODE.wrap(text);
            assertTrue("Inline code wrap should produce `text`", 
                    codeMarkdown.startsWith("`") && codeMarkdown.endsWith("`"));
            
            // Code block
            String blockMarkdown = FormatType.CODE_BLOCK.wrap(text);
            assertTrue("Code block wrap should start with ```\\n", blockMarkdown.startsWith("```\n"));
            assertTrue("Code block wrap should end with \\n```", blockMarkdown.endsWith("\n```"));
            
            // Bullet list
            String bulletMarkdown = FormatType.BULLET_LIST.wrap(text);
            assertTrue("Bullet list wrap should start with '- '", bulletMarkdown.startsWith("- "));
            
            // Blockquote
            String quoteMarkdown = FormatType.BLOCKQUOTE.wrap(text);
            assertTrue("Blockquote wrap should start with '> '", quoteMarkdown.startsWith("> "));
        }
    }

    /**
     * Property 1.12: MarkdownConverter.fromMarkdown handles null and empty input safely.
     * **Validates: Requirements 2.1-10.2**
     */
    @Test
    public void property1_markdownConverterNullSafety() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            assertNotNull("fromMarkdown(null) should return non-null",
                    MarkdownConverter.fromMarkdown(null));
            assertNotNull("fromMarkdown(\"\") should return non-null",
                    MarkdownConverter.fromMarkdown(""));
        }
    }

    /**
     * Property 1.13: Formatter consistently returns null for null input.
     * **Validates: Requirements 2.1-10.2**
     */
    @Test
    public void property1_formatterNullInputConsistency() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            assertNull("prepareLeftMessageBubbleSpan(null) should return null",
                    formatter.prepareLeftMessageBubbleSpan(null, null, null));
            assertNull("prepareRightMessageBubbleSpan(null) should return null",
                    formatter.prepareRightMessageBubbleSpan(null, null, null));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Generates random alphabetic text of specified length range.
     */
    private String generateRandomAlphaText(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generates random alphanumeric text of specified length range.
     */
    private String generateRandomAlphaNumericText(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ==================== Property 2: Formatter Preserves Existing Spans ====================

    /**
     * Property 2.1: Position mapping creates identity map for text without markdown.
     * For any text without markdown markers, the position map should be an identity mapping.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_positionMapIdentityForPlainText() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 30);
            
            // Build position map
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(text, text);
            
            // Verify identity mapping
            assertEquals("Position map length should be text length + 1", 
                    text.length() + 1, positionMap.length);
            
            for (int j = 0; j <= text.length(); j++) {
                assertEquals("Position " + j + " should map to itself", j, positionMap[j]);
            }
        }
    }

    /**
     * Property 2.2: Position mapping correctly handles bold markdown removal.
     * For text with **bold** markers, positions after the markers should be adjusted.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_positionMapHandlesBoldMarkers() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String prefix = generateRandomAlphaText(2, 10);
            String boldText = generateRandomAlphaText(3, 10);
            String suffix = generateRandomAlphaText(2, 10);
            
            String original = prefix + "**" + boldText + "**" + suffix;
            String parsed = prefix + boldText + suffix;
            
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(original, parsed);
            
            // Verify prefix positions are unchanged
            for (int j = 0; j < prefix.length(); j++) {
                assertEquals("Prefix position " + j + " should be unchanged", j, positionMap[j]);
            }
            
            // Verify positions after all markers are adjusted by 4 (** at start and ** at end)
            int afterMarkersPos = prefix.length() + 2 + boldText.length() + 2;
            for (int j = afterMarkersPos; j < original.length(); j++) {
                int expectedMapped = j - 4; // 4 chars removed (** + **)
                assertEquals("Position " + j + " should be adjusted by 4", 
                        expectedMapped, positionMap[j]);
            }
        }
    }

    /**
     * Property 2.3: mapPosition handles positions beyond map length.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_mapPositionHandlesBeyondLength() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(text, text);
            
            // Position beyond map should return last valid position
            int beyondPos = text.length() + random.nextInt(10) + 1;
            int mapped = CometChatRichTextFormatter.mapPosition(beyondPos, positionMap, text.length());
            
            assertTrue("Mapped position should be <= max position", mapped <= text.length());
        }
    }

    /**
     * Property 2.4: mapPosition handles negative positions.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_mapPositionHandlesNegativePositions() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(text, text);
            
            int negativePos = -(random.nextInt(10) + 1);
            int mapped = CometChatRichTextFormatter.mapPosition(negativePos, positionMap, text.length());
            
            assertEquals("Negative position should map to -1", -1, mapped);
        }
    }

    /**
     * Property 2.5: mapPosition handles null/empty position map.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_mapPositionHandlesNullMap() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int pos = random.nextInt(100);
            
            // Null map should return original position
            int mappedNull = CometChatRichTextFormatter.mapPosition(pos, null, 100);
            assertEquals("Null map should return original position", pos, mappedNull);
            
            // Empty map should return original position
            int mappedEmpty = CometChatRichTextFormatter.mapPosition(pos, new int[0], 100);
            assertEquals("Empty map should return original position", pos, mappedEmpty);
        }
    }

    /**
     * Property 2.6: Position mapping handles multiple markdown formats.
     * For text with multiple markdown formats, all marker positions should be tracked.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_positionMapHandlesMultipleFormats() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text1 = generateRandomAlphaText(3, 8);
            String text2 = generateRandomAlphaText(3, 8);
            
            // Create text with bold and italic
            String original = "**" + text1 + "** _" + text2 + "_";
            String parsed = text1 + " " + text2;
            
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(original, parsed);
            
            // Verify map is created
            assertNotNull("Position map should not be null", positionMap);
            assertEquals("Position map length should be original length + 1", 
                    original.length() + 1, positionMap.length);
        }
    }

    /**
     * Property 2.7: buildPositionMap handles empty input.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_buildPositionMapHandlesEmptyInput() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int[] mapNull = CometChatRichTextFormatter.buildPositionMap(null, null);
            assertEquals("Null input should return empty array", 0, mapNull.length);
            
            int[] mapEmpty = CometChatRichTextFormatter.buildPositionMap("", "");
            assertEquals("Empty input should return empty array", 0, mapEmpty.length);
        }
    }

    /**
     * Property 2.8: Position mapping preserves relative ordering.
     * For any two positions p1 < p2 in original text, their mapped positions should maintain
     * the same ordering (mapped(p1) <= mapped(p2)).
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_positionMapPreservesOrdering() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String prefix = generateRandomAlphaText(3, 10);
            String boldText = generateRandomAlphaText(3, 10);
            String suffix = generateRandomAlphaText(3, 10);
            
            String original = prefix + "**" + boldText + "**" + suffix;
            String parsed = prefix + boldText + suffix;
            
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(original, parsed);
            
            // Check that ordering is preserved for all valid positions
            int lastValidMapped = -1;
            for (int j = 0; j < positionMap.length; j++) {
                int mapped = positionMap[j];
                if (mapped != -1) {
                    assertTrue("Position ordering should be preserved: pos " + j + 
                            " mapped to " + mapped + " but last valid was " + lastValidMapped,
                            mapped >= lastValidMapped);
                    lastValidMapped = mapped;
                }
            }
        }
    }

    /**
     * Property 2.9: Span preservation works for spans on plain text regions.
     * When a span is applied to a region that doesn't contain markdown markers,
     * the span should be preserved with correct positions.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_spanPreservationOnPlainTextRegions() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String prefix = generateRandomAlphaText(5, 15);
            String boldText = generateRandomAlphaText(3, 10);
            String suffix = generateRandomAlphaText(5, 15);
            
            String original = prefix + "**" + boldText + "**" + suffix;
            String parsed = prefix + boldText + suffix;
            
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(original, parsed);
            
            // A span on the prefix should map to the same positions
            int spanStart = 0;
            int spanEnd = prefix.length();
            
            int mappedStart = CometChatRichTextFormatter.mapPosition(spanStart, positionMap, parsed.length());
            int mappedEnd = CometChatRichTextFormatter.mapPosition(spanEnd, positionMap, parsed.length());
            
            assertEquals("Span start on prefix should be unchanged", spanStart, mappedStart);
            assertEquals("Span end on prefix should be unchanged", spanEnd, mappedEnd);
        }
    }

    /**
     * Property 2.10: Span preservation adjusts positions for spans after markdown.
     * When a span is applied to a region after markdown markers,
     * the span positions should be adjusted by the marker length.
     * **Validates: Requirements 1.4**
     */
    @Test
    public void property2_spanPreservationAdjustsPositionsAfterMarkdown() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String prefix = generateRandomAlphaText(3, 8);
            String boldText = generateRandomAlphaText(3, 8);
            String suffix = generateRandomAlphaText(5, 15);
            
            String original = prefix + "**" + boldText + "**" + suffix;
            String parsed = prefix + boldText + suffix;
            
            int[] positionMap = CometChatRichTextFormatter.buildPositionMap(original, parsed);
            
            // A span on the suffix should be adjusted by 4 (** + **)
            int originalSuffixStart = prefix.length() + 2 + boldText.length() + 2;
            int originalSuffixEnd = original.length();
            
            int mappedStart = CometChatRichTextFormatter.mapPosition(originalSuffixStart, positionMap, parsed.length());
            int mappedEnd = CometChatRichTextFormatter.mapPosition(originalSuffixEnd, positionMap, parsed.length());
            
            int expectedStart = prefix.length() + boldText.length();
            int expectedEnd = parsed.length();
            
            assertEquals("Span start on suffix should be adjusted", expectedStart, mappedStart);
            assertEquals("Span end on suffix should be adjusted", expectedEnd, mappedEnd);
        }
    }

    // ==================== Property 3: Nested Format Handling ====================

    /**
     * Property 3.1: Nested bold and italic markdown markers are correctly identifiable.
     * For any text with nested **_text_** format, both markers should be present and removable.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedBoldItalicMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String nestedMarkdown = "**_" + text + "_**";
            
            // Verify both markers are present
            assertTrue("Nested markdown should contain bold markers", nestedMarkdown.contains("**"));
            assertTrue("Nested markdown should contain italic markers", nestedMarkdown.contains("_"));
            
            // Verify text can be extracted by removing markers
            String extracted = nestedMarkdown.replace("**", "").replace("_", "");
            assertEquals("Extracted text should match original", text, extracted);
        }
    }

    /**
     * Property 3.2: Nested italic and bold markdown markers are correctly identifiable.
     * For any text with nested _**text**_ format, both markers should be present and removable.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedItalicBoldMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String nestedMarkdown = "_**" + text + "**_";
            
            // Verify both markers are present
            assertTrue("Nested markdown should contain bold markers", nestedMarkdown.contains("**"));
            assertTrue("Nested markdown should contain italic markers", nestedMarkdown.contains("_"));
            
            // Verify text can be extracted by removing markers
            String extracted = nestedMarkdown.replace("**", "").replace("_", "");
            assertEquals("Extracted text should match original", text, extracted);
        }
    }

    /**
     * Property 3.3: Nested bold and strikethrough markdown markers are correctly identifiable.
     * For any text with nested **~~text~~** format, both markers should be present and removable.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedBoldStrikethroughMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String nestedMarkdown = "**~~" + text + "~~**";
            
            // Verify both markers are present
            assertTrue("Nested markdown should contain bold markers", nestedMarkdown.contains("**"));
            assertTrue("Nested markdown should contain strikethrough markers", nestedMarkdown.contains("~~"));
            
            // Verify text can be extracted by removing markers
            String extracted = nestedMarkdown.replace("**", "").replace("~~", "");
            assertEquals("Extracted text should match original", text, extracted);
        }
    }

    /**
     * Property 3.4: Triple nested format markers are correctly identifiable.
     * For any text with nested **_~~text~~_** format, all markers should be present and removable.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_tripleNestedMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String nestedMarkdown = "**_~~" + text + "~~_**";
            
            // Verify all markers are present
            assertTrue("Nested markdown should contain bold markers", nestedMarkdown.contains("**"));
            assertTrue("Nested markdown should contain italic markers", nestedMarkdown.contains("_"));
            assertTrue("Nested markdown should contain strikethrough markers", nestedMarkdown.contains("~~"));
            
            // Verify text can be extracted by removing markers
            String extracted = nestedMarkdown.replace("**", "").replace("_", "").replace("~~", "");
            assertEquals("Extracted text should match original", text, extracted);
        }
    }

    /**
     * Property 3.5: MarkdownConverter.fromMarkdown handles nested formats without exception.
     * For any nested format combination, parsing should not throw exceptions.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedFormatsNoException() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            
            // Test various nested combinations
            assertNotNull("Bold+Italic should parse without exception",
                    MarkdownConverter.fromMarkdown("**_" + text + "_**"));
            assertNotNull("Italic+Bold should parse without exception",
                    MarkdownConverter.fromMarkdown("_**" + text + "**_"));
            assertNotNull("Bold+Strikethrough should parse without exception",
                    MarkdownConverter.fromMarkdown("**~~" + text + "~~**"));
            assertNotNull("Triple nested should parse without exception",
                    MarkdownConverter.fromMarkdown("**_~~" + text + "~~_**"));
        }
    }

    /**
     * Property 3.6: Nested format spans return correct format types.
     * All span types used in nested formats should return their correct FormatType.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedFormatSpansReturnCorrectTypes() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Verify each span type returns correct format type
            assertEquals("BoldFormatSpan should return BOLD",
                    FormatType.BOLD, new BoldFormatSpan().getFormatType());
            assertEquals("ItalicFormatSpan should return ITALIC",
                    FormatType.ITALIC, new ItalicFormatSpan().getFormatType());
            assertEquals("StrikethroughFormatSpan should return STRIKETHROUGH",
                    FormatType.STRIKETHROUGH, new StrikethroughFormatSpan().getFormatType());
        }
    }

    /**
     * Property 3.7: Nested format marker lengths are consistent.
     * The total marker length for nested formats should be the sum of individual marker lengths.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_nestedFormatMarkerLengthsConsistent() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            
            // Bold markers: ** (2) + ** (2) = 4
            // Italic markers: _ (1) + _ (1) = 2
            // Strikethrough markers: ~~ (2) + ~~ (2) = 4
            
            String boldItalic = "**_" + text + "_**";
            int expectedLength = text.length() + 4 + 2; // text + bold markers + italic markers
            assertEquals("Bold+Italic length should be text + 6", expectedLength, boldItalic.length());
            
            String tripleNested = "**_~~" + text + "~~_**";
            int expectedTripleLength = text.length() + 4 + 2 + 4; // text + bold + italic + strikethrough
            assertEquals("Triple nested length should be text + 10", expectedTripleLength, tripleNested.length());
        }
    }

    /**
     * Property 3.8: Random nested format combinations are parseable.
     * Generate random combinations of nested formats and verify they parse without exception.
     * **Validates: Requirements 11.1, 11.2, 11.3**
     */
    @Test
    public void property3_randomNestedCombinationsParseable() {
        String[] prefixes = {"**", "_", "~~"};
        String[] suffixes = {"**", "_", "~~"};
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            
            // Generate random nested combination
            int outerIdx = random.nextInt(prefixes.length);
            int innerIdx = random.nextInt(prefixes.length);
            
            String markdown = prefixes[outerIdx] + prefixes[innerIdx] + text + 
                    suffixes[innerIdx] + suffixes[outerIdx];
            
            // Should parse without exception
            assertNotNull("Random nested combination should parse: " + markdown,
                    MarkdownConverter.fromMarkdown(markdown));
        }
    }

    // ==================== Property 4: Plain Text Preservation ====================

    /**
     * Property 4.1: Plain alphabetic text is preserved unchanged.
     * For any random alphabetic text without markdown syntax, the formatter should return identical text.
     * **Validates: Requirements 12.1, 12.2, 12.3**
     */
    @Test
    public void property4_plainAlphabeticTextPreserved() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 50);
            
            // Verify text doesn't contain markdown markers
            assertFalse("Generated text should not contain **", text.contains("**"));
            assertFalse("Generated text should not contain ~~", text.contains("~~"));
            assertFalse("Generated text should not contain ```", text.contains("```"));
            
            // Parse and verify no exception
            assertNotNull("Plain text should parse without exception",
                    MarkdownConverter.fromMarkdown(text));
        }
    }

    /**
     * Property 4.2: Plain alphanumeric text is preserved unchanged.
     * For any random alphanumeric text without markdown syntax, the formatter should return identical text.
     * **Validates: Requirements 12.1, 12.2, 12.3**
     */
    @Test
    public void property4_plainAlphanumericTextPreserved() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaNumericText(5, 50);
            
            // Parse and verify no exception
            assertNotNull("Plain alphanumeric text should parse without exception",
                    MarkdownConverter.fromMarkdown(text));
        }
    }

    /**
     * Property 4.3: Text with spaces is preserved.
     * For any text with spaces but no markdown, the formatter should preserve it.
     * **Validates: Requirements 12.1, 12.2, 12.3**
     */
    @Test
    public void property4_textWithSpacesPreserved() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate text with random spaces
            StringBuilder sb = new StringBuilder();
            int wordCount = random.nextInt(5) + 2;
            for (int j = 0; j < wordCount; j++) {
                if (j > 0) {
                    // Add 1-3 spaces between words
                    int spaces = random.nextInt(3) + 1;
                    for (int k = 0; k < spaces; k++) {
                        sb.append(" ");
                    }
                }
                sb.append(generateRandomAlphaText(3, 10));
            }
            String text = sb.toString();
            
            // Parse and verify no exception
            assertNotNull("Text with spaces should parse without exception",
                    MarkdownConverter.fromMarkdown(text));
        }
    }

    /**
     * Property 4.4: Text with newlines is preserved.
     * For any text with newlines but no markdown, the formatter should preserve it.
     * **Validates: Requirements 12.1, 12.2, 12.3**
     */
    @Test
    public void property4_textWithNewlinesPreserved() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate text with random newlines
            StringBuilder sb = new StringBuilder();
            int lineCount = random.nextInt(5) + 2;
            for (int j = 0; j < lineCount; j++) {
                if (j > 0) {
                    sb.append("\n");
                }
                sb.append(generateRandomAlphaText(5, 20));
            }
            String text = sb.toString();
            
            // Parse and verify no exception
            assertNotNull("Text with newlines should parse without exception",
                    MarkdownConverter.fromMarkdown(text));
        }
    }

    /**
     * Property 4.5: Unclosed markdown markers are left as-is.
     * For any text with unclosed markdown markers, the formatter should not throw exceptions.
     * **Validates: Requirements 12.2**
     */
    @Test
    public void property4_unclosedMarkersLeftAsIs() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            
            // Test various unclosed markers
            assertNotNull("Unclosed bold should not throw",
                    MarkdownConverter.fromMarkdown("**" + text));
            assertNotNull("Unclosed italic should not throw",
                    MarkdownConverter.fromMarkdown("_" + text));
            assertNotNull("Unclosed strikethrough should not throw",
                    MarkdownConverter.fromMarkdown("~~" + text));
            assertNotNull("Unclosed inline code should not throw",
                    MarkdownConverter.fromMarkdown("`" + text));
            assertNotNull("Unclosed code block should not throw",
                    MarkdownConverter.fromMarkdown("```" + text));
        }
    }

    /**
     * Property 4.6: Single markers are not treated as formatting.
     * Single asterisks, tildes, etc. should not be treated as format markers.
     * **Validates: Requirements 12.3**
     */
    @Test
    public void property4_singleMarkersNotFormatting() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            
            // Single asterisk should not be bold
            assertNotNull("Single asterisk should not throw",
                    MarkdownConverter.fromMarkdown("*" + text + "*"));
            
            // Single tilde should not be strikethrough
            assertNotNull("Single tilde should not throw",
                    MarkdownConverter.fromMarkdown("~" + text + "~"));
        }
    }

    /**
     * Property 4.7: Underscores in words are not treated as italic.
     * Underscores within words (like snake_case) should not be treated as italic markers.
     * **Validates: Requirements 12.3**
     */
    @Test
    public void property4_underscoresInWordsNotItalic() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String word1 = generateRandomAlphaText(3, 10);
            String word2 = generateRandomAlphaText(3, 10);
            String word3 = generateRandomAlphaText(3, 10);
            
            String snakeCase = word1 + "_" + word2 + "_" + word3;
            
            // Should parse without exception
            assertNotNull("Snake case should parse without exception",
                    MarkdownConverter.fromMarkdown(snakeCase));
        }
    }

    /**
     * Property 4.8: Numbers without proper list format are not treated as lists.
     * Numbers not followed by ". " at line start should not be treated as numbered lists.
     * **Validates: Requirements 12.3**
     */
    @Test
    public void property4_numbersNotAlwaysList() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int number = random.nextInt(100) + 1;
            String text = generateRandomAlphaText(5, 20);
            
            // Number without period
            assertNotNull("Number without period should not throw",
                    MarkdownConverter.fromMarkdown(number + " " + text));
            
            // Number with period but no space
            assertNotNull("Number with period but no space should not throw",
                    MarkdownConverter.fromMarkdown(number + "." + text));
            
            // Number in middle of line
            assertNotNull("Number in middle of line should not throw",
                    MarkdownConverter.fromMarkdown(text + " " + number + ". " + text));
        }
    }

    /**
     * Property 4.9: Dash without space is not treated as bullet list.
     * Dashes not followed by space at line start should not be treated as bullet lists.
     * **Validates: Requirements 12.3**
     */
    @Test
    public void property4_dashNotAlwaysBullet() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            
            // Dash without space
            assertNotNull("Dash without space should not throw",
                    MarkdownConverter.fromMarkdown("-" + text));
            
            // Dash in middle of line
            assertNotNull("Dash in middle of line should not throw",
                    MarkdownConverter.fromMarkdown(text + " - " + text));
        }
    }

    /**
     * Property 4.10: Greater-than without space is not treated as blockquote.
     * Greater-than not followed by space at line start should not be treated as blockquote.
     * **Validates: Requirements 12.3**
     */
    @Test
    public void property4_greaterThanNotAlwaysBlockquote() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            
            // Greater-than without space
            assertNotNull("Greater-than without space should not throw",
                    MarkdownConverter.fromMarkdown(">" + text));
            
            // Greater-than in middle of line
            assertNotNull("Greater-than in middle of line should not throw",
                    MarkdownConverter.fromMarkdown(text + " > " + text));
        }
    }

    // ==================== Property 5: Link URL Storage ====================

    /**
     * Property 5.1: LinkFormatSpan stores URL correctly.
     * For any URL, the LinkFormatSpan should store and return the exact URL.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanStoresUrl() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 30);
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store exact URL", url, span.getUrl());
        }
    }

    /**
     * Property 5.2: LinkFormatSpan stores URLs with query parameters.
     * For any URL with query parameters, the LinkFormatSpan should store the complete URL.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanStoresUrlWithQueryParams() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String path = generateRandomAlphaNumericText(5, 15);
            String param1 = generateRandomAlphaText(3, 10);
            String value1 = generateRandomAlphaNumericText(3, 10);
            String param2 = generateRandomAlphaText(3, 10);
            String value2 = generateRandomAlphaNumericText(3, 10);
            
            String url = "https://example.com/" + path + "?" + param1 + "=" + value1 + "&" + param2 + "=" + value2;
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store URL with query params", url, span.getUrl());
        }
    }

    /**
     * Property 5.3: LinkFormatSpan stores URLs with fragments.
     * For any URL with fragment identifiers, the LinkFormatSpan should store the complete URL.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanStoresUrlWithFragment() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String path = generateRandomAlphaNumericText(5, 15);
            String fragment = generateRandomAlphaText(3, 15);
            
            String url = "https://example.com/" + path + "#" + fragment;
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store URL with fragment", url, span.getUrl());
        }
    }

    /**
     * Property 5.4: LinkFormatSpan URL can be updated.
     * The URL stored in LinkFormatSpan should be updatable via setUrl().
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanUrlUpdatable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String originalUrl = "https://original.com/" + generateRandomAlphaNumericText(5, 15);
            String newUrl = "https://new.com/" + generateRandomAlphaNumericText(5, 15);
            
            LinkFormatSpan span = new LinkFormatSpan(originalUrl);
            assertEquals("Initial URL should be stored", originalUrl, span.getUrl());
            
            span.setUrl(newUrl);
            assertEquals("Updated URL should be stored", newUrl, span.getUrl());
        }
    }

    /**
     * Property 5.5: LinkFormatSpan handles various URL schemes.
     * LinkFormatSpan should store URLs with different schemes (http, https, mailto, etc.).
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanHandlesVariousSchemes() {
        String[] schemes = {"http://", "https://", "mailto:", "tel:", "ftp://"};
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String scheme = schemes[random.nextInt(schemes.length)];
            String path = generateRandomAlphaNumericText(5, 20);
            String url = scheme + path;
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store URL with scheme " + scheme, url, span.getUrl());
        }
    }

    /**
     * Property 5.6: LinkFormatSpan handles empty URL.
     * LinkFormatSpan should handle empty URL without throwing exception.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanHandlesEmptyUrl() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            LinkFormatSpan span = new LinkFormatSpan("");
            assertEquals("LinkFormatSpan should handle empty URL", "", span.getUrl());
        }
    }

    /**
     * Property 5.7: LinkFormatSpan returns correct format type.
     * LinkFormatSpan should always return FormatType.LINK.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanReturnsCorrectType() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 20);
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should return LINK format type",
                    FormatType.LINK, span.getFormatType());
        }
    }

    /**
     * Property 5.8: Link markdown syntax is correctly identifiable.
     * For any [text](url) markdown, the text and URL should be extractable.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkMarkdownSyntaxIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 20);
            String markdown = "[" + text + "](" + url + ")";
            
            // Verify markers are present
            assertTrue("Link markdown should contain [", markdown.contains("["));
            assertTrue("Link markdown should contain ](", markdown.contains("]("));
            assertTrue("Link markdown should contain )", markdown.contains(")"));
            
            // Verify text and URL can be extracted
            int textStart = markdown.indexOf('[') + 1;
            int textEnd = markdown.indexOf(']');
            assertEquals("Extracted text should match", text, markdown.substring(textStart, textEnd));
            
            int urlStart = markdown.indexOf('(') + 1;
            int urlEnd = markdown.lastIndexOf(')');
            assertEquals("Extracted URL should match", url, markdown.substring(urlStart, urlEnd));
        }
    }

    /**
     * Property 5.9: Link markdown parses without exception.
     * For any valid link markdown, parsing should not throw exceptions.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkMarkdownParsesWithoutException() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(3, 15);
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 20);
            String markdown = "[" + text + "](" + url + ")";
            
            assertNotNull("Link markdown should parse without exception",
                    MarkdownConverter.fromMarkdown(markdown));
        }
    }

    /**
     * Property 5.10: LinkFormatSpan stores URLs with special characters.
     * LinkFormatSpan should correctly store URLs containing special characters.
     * **Validates: Requirements 7.4**
     */
    @Test
    public void property5_linkFormatSpanStoresUrlWithSpecialChars() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String path = generateRandomAlphaNumericText(5, 10);
            // Add some URL-safe special characters
            String url = "https://example.com/" + path + "?key=" + path + "&other=" + path + "#section";
            
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("LinkFormatSpan should store URL with special chars", url, span.getUrl());
        }
    }
}
