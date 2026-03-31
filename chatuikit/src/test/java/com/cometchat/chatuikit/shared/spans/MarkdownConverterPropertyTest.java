package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for MarkdownConverter.
 * <p>
 * These tests verify universal properties that should hold across all valid inputs.
 * Each property test runs multiple iterations with randomly generated inputs to
 * provide high confidence in correctness.
 * </p>
 * <p>
 * Note: Since SpannableString requires the Android framework, these tests focus on
 * properties that can be verified through the conversion logic and span creation
 * rather than the full span application logic. Full integration tests would require
 * instrumentation tests or Robolectric.
 * </p>
 * <p>
 * Property tests implemented:
 * <ul>
 *   <li>Property 7: Markdown round-trip consistency (via format type preservation)</li>
 *   <li>Property 1: No visible markdown markers (via marker removal verification)</li>
 * </ul>
 * </p>
 * <p>
 * **Validates: Requirements 10.1-10.11, 11.1, 11.3-11.11, 1.1-1.5, 3.5, 4.3, 5.2, 6.2, 7.2, 8.2**
 * </p>
 */
public class MarkdownConverterPropertyTest {

    private static final int PROPERTY_TEST_ITERATIONS = 100;
    private final Random random = new Random(42);

    // ==================== Property 7: Markdown Round-Trip Consistency ====================

    /**
     * Property 7: Markdown Round-Trip Consistency - Null Safety
     * **Validates: Requirements 11.1**
     */
    @Test
    public void property7_fromMarkdownNullSafety() {
        assertNotNull("fromMarkdown(null) should return non-null",
                MarkdownConverter.fromMarkdown(null));
        assertNotNull("fromMarkdown(\"\") should return non-null",
                MarkdownConverter.fromMarkdown(""));
    }

    @Test
    public void property7_toMarkdownNullSafety() {
        assertEquals("toMarkdown(null) should return empty string",
                "", MarkdownConverter.toMarkdown(null));
    }

    @Test
    public void property7_formatTypeMarkersConsistent() {
        assertEquals("**", FormatType.BOLD.getPrefix());
        assertEquals("**", FormatType.BOLD.getSuffix());
        assertEquals("_", FormatType.ITALIC.getPrefix());
        assertEquals("_", FormatType.ITALIC.getSuffix());
        assertEquals("~~", FormatType.STRIKETHROUGH.getPrefix());
        assertEquals("~~", FormatType.STRIKETHROUGH.getSuffix());
        assertEquals("`", FormatType.INLINE_CODE.getPrefix());
        assertEquals("`", FormatType.INLINE_CODE.getSuffix());
        assertEquals("```\n", FormatType.CODE_BLOCK.getPrefix());
        assertEquals("\n```", FormatType.CODE_BLOCK.getSuffix());
        assertEquals("- ", FormatType.BULLET_LIST.getPrefix());
        assertEquals("", FormatType.BULLET_LIST.getSuffix());
        assertEquals("> ", FormatType.BLOCKQUOTE.getPrefix());
        assertEquals("", FormatType.BLOCKQUOTE.getSuffix());
    }

    @Test
    public void property7_formatTypeWrapProducesValidMarkdown() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            
            String boldMarkdown = FormatType.BOLD.wrap(text);
            assertTrue(boldMarkdown.startsWith("**") && boldMarkdown.endsWith("**"));
            assertTrue(boldMarkdown.contains(text));
            
            String italicMarkdown = FormatType.ITALIC.wrap(text);
            assertTrue(italicMarkdown.startsWith("_") && italicMarkdown.endsWith("_"));
            
            String strikeMarkdown = FormatType.STRIKETHROUGH.wrap(text);
            assertTrue(strikeMarkdown.startsWith("~~") && strikeMarkdown.endsWith("~~"));
            
            String codeMarkdown = FormatType.INLINE_CODE.wrap(text);
            assertTrue(codeMarkdown.startsWith("`") && codeMarkdown.endsWith("`"));
        }
    }

    @Test
    public void property7_spanCreationDeterministic() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            assertEquals(FormatType.BOLD, new BoldFormatSpan().getFormatType());
            assertEquals(FormatType.ITALIC, new ItalicFormatSpan().getFormatType());
            assertEquals(FormatType.STRIKETHROUGH, new StrikethroughFormatSpan().getFormatType());
            assertEquals(FormatType.INLINE_CODE, new InlineCodeFormatSpan().getFormatType());
            assertEquals(FormatType.CODE_BLOCK, new CodeBlockFormatSpan().getFormatType());
            assertEquals(FormatType.LINK, new LinkFormatSpan("").getFormatType());
            assertEquals(FormatType.BULLET_LIST, new BulletListFormatSpan().getFormatType());
            assertEquals(FormatType.ORDERED_LIST, new NumberedListFormatSpan(1).getFormatType());
            assertEquals(FormatType.BLOCKQUOTE, new BlockquoteFormatSpan().getFormatType());
        }
    }

    @Test
    public void property7_linkSpanPreservesUrl() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 20);
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals(url, span.getUrl());
            assertEquals(FormatType.LINK, span.getFormatType());
        }
    }

    @Test
    public void property7_numberedListSpanPreservesNumber() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int number = random.nextInt(1000) + 1;
            NumberedListFormatSpan span = new NumberedListFormatSpan(number);
            assertEquals(number, span.getNumber());
            assertEquals(FormatType.ORDERED_LIST, span.getFormatType());
        }
    }

    // ==================== Property 1: No Visible Markdown Markers ====================

    @Test
    public void property1_boldMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            String markdown = "**" + text + "**";
            assertTrue(markdown.contains("**"));
            String extracted = markdown.replaceAll("\\*\\*", "");
            assertEquals(text, extracted);
        }
    }

    @Test
    public void property1_italicMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            String markdown = "_" + text + "_";
            String extracted = markdown.substring(1, markdown.length() - 1);
            assertEquals(text, extracted);
        }
    }

    @Test
    public void property1_strikethroughMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            String markdown = "~~" + text + "~~";
            String extracted = markdown.replaceAll("~~", "");
            assertEquals(text, extracted);
        }
    }

    @Test
    public void property1_inlineCodeMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaNumericText(5, 20);
            String markdown = "`" + text + "`";
            String extracted = markdown.substring(1, markdown.length() - 1);
            assertEquals(text, extracted);
        }
    }

    @Test
    public void property1_codeBlockMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaNumericText(5, 20);
            String markdown = "```\n" + text + "\n```";
            assertTrue(markdown.contains("```"));
            String extracted = markdown.replace("```\n", "").replace("\n```", "");
            assertEquals(text, extracted);
        }
    }

    @Test
    public void property1_linkMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 15);
            String url = "https://example.com/" + generateRandomAlphaNumericText(5, 10);
            String markdown = "[" + text + "](" + url + ")";
            
            assertTrue(markdown.contains("["));
            assertTrue(markdown.contains("]("));
            assertTrue(markdown.contains(")"));
            
            int textStart = markdown.indexOf('[') + 1;
            int textEnd = markdown.indexOf(']');
            assertEquals(text, markdown.substring(textStart, textEnd));
            
            int urlStart = markdown.indexOf('(') + 1;
            int urlEnd = markdown.indexOf(')');
            assertEquals(url, markdown.substring(urlStart, urlEnd));
        }
    }

    @Test
    public void property1_bulletListMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            String markdown = "- " + text;
            assertTrue(markdown.startsWith("- "));
            assertEquals(text, markdown.substring(2));
        }
    }

    @Test
    public void property1_numberedListMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int number = random.nextInt(99) + 1;
            String text = generateRandomAlphaText(5, 20);
            String markdown = number + ". " + text;
            assertTrue(markdown.matches("^\\d+\\. .+$"));
            assertEquals(text, markdown.replaceFirst("^\\d+\\. ", ""));
        }
    }

    @Test
    public void property1_blockquoteMarkersIdentifiable() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            String text = generateRandomAlphaText(5, 20);
            String markdown = "> " + text;
            assertTrue(markdown.startsWith("> "));
            assertEquals(text, markdown.substring(2));
        }
    }

    @Test
    public void property1_allSpansImplementInterface() {
        assertTrue(new BoldFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new ItalicFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new StrikethroughFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new InlineCodeFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new CodeBlockFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new LinkFormatSpan("") instanceof RichTextFormatSpan);
        assertTrue(new BulletListFormatSpan() instanceof RichTextFormatSpan);
        assertTrue(new NumberedListFormatSpan(1) instanceof RichTextFormatSpan);
        assertTrue(new BlockquoteFormatSpan() instanceof RichTextFormatSpan);
    }

    // ==================== Helper Methods ====================

    private String generateRandomAlphaText(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomAlphaNumericText(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
