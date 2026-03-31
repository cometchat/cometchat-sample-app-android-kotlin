package com.cometchat.chatuikit.shared.views.richtexttoolbar;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for FormatType enum.
 */
public class FormatTypeTest {

    // ==================== BOLD Format Tests ====================

    @Test
    public void boldWrapProducesCorrectOutput() {
        String text = "Hello";
        String wrapped = FormatType.BOLD.wrap(text);
        assertEquals("**Hello**", wrapped);
    }

    @Test
    public void boldUnwrapProducesCorrectOutput() {
        String wrapped = "**Hello**";
        String unwrapped = FormatType.BOLD.unwrap(wrapped);
        assertEquals("Hello", unwrapped);
    }

    @Test
    public void boldRoundTrip() {
        String text = "Test message";
        String wrapped = FormatType.BOLD.wrap(text);
        String unwrapped = FormatType.BOLD.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void boldIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.BOLD.isWrapped("**text**"));
        assertFalse(FormatType.BOLD.isWrapped("text"));
        assertFalse(FormatType.BOLD.isWrapped("*text*"));
    }

    // ==================== ITALIC Format Tests ====================

    @Test
    public void italicWrapProducesCorrectOutput() {
        String text = "Hello";
        String wrapped = FormatType.ITALIC.wrap(text);
        assertEquals("_Hello_", wrapped);
    }

    @Test
    public void italicUnwrapProducesCorrectOutput() {
        String wrapped = "_Hello_";
        String unwrapped = FormatType.ITALIC.unwrap(wrapped);
        assertEquals("Hello", unwrapped);
    }

    @Test
    public void italicRoundTrip() {
        String text = "Test message";
        String wrapped = FormatType.ITALIC.wrap(text);
        String unwrapped = FormatType.ITALIC.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void italicIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.ITALIC.isWrapped("_text_"));
        assertFalse(FormatType.ITALIC.isWrapped("text"));
    }

    // ==================== STRIKETHROUGH Format Tests ====================

    @Test
    public void strikethroughWrapProducesCorrectOutput() {
        String text = "Hello";
        String wrapped = FormatType.STRIKETHROUGH.wrap(text);
        assertEquals("~~Hello~~", wrapped);
    }

    @Test
    public void strikethroughUnwrapProducesCorrectOutput() {
        String wrapped = "~~Hello~~";
        String unwrapped = FormatType.STRIKETHROUGH.unwrap(wrapped);
        assertEquals("Hello", unwrapped);
    }

    @Test
    public void strikethroughRoundTrip() {
        String text = "Test message";
        String wrapped = FormatType.STRIKETHROUGH.wrap(text);
        String unwrapped = FormatType.STRIKETHROUGH.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void strikethroughIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.STRIKETHROUGH.isWrapped("~~text~~"));
        assertFalse(FormatType.STRIKETHROUGH.isWrapped("text"));
        assertFalse(FormatType.STRIKETHROUGH.isWrapped("~text~"));
    }

    // ==================== INLINE_CODE Format Tests ====================

    @Test
    public void inlineCodeWrapProducesCorrectOutput() {
        String text = "code";
        String wrapped = FormatType.INLINE_CODE.wrap(text);
        assertEquals("`code`", wrapped);
    }

    @Test
    public void inlineCodeUnwrapProducesCorrectOutput() {
        String wrapped = "`code`";
        String unwrapped = FormatType.INLINE_CODE.unwrap(wrapped);
        assertEquals("code", unwrapped);
    }

    @Test
    public void inlineCodeRoundTrip() {
        String text = "console.log()";
        String wrapped = FormatType.INLINE_CODE.wrap(text);
        String unwrapped = FormatType.INLINE_CODE.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void inlineCodeIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.INLINE_CODE.isWrapped("`text`"));
        assertFalse(FormatType.INLINE_CODE.isWrapped("text"));
    }

    // ==================== CODE_BLOCK Format Tests ====================

    @Test
    public void codeBlockWrapProducesCorrectOutput() {
        String text = "code";
        String wrapped = FormatType.CODE_BLOCK.wrap(text);
        assertEquals("```\ncode\n```", wrapped);
    }

    @Test
    public void codeBlockUnwrapProducesCorrectOutput() {
        String wrapped = "```\ncode\n```";
        String unwrapped = FormatType.CODE_BLOCK.unwrap(wrapped);
        assertEquals("code", unwrapped);
    }

    @Test
    public void codeBlockRoundTrip() {
        String text = "function test() {\n  return true;\n}";
        String wrapped = FormatType.CODE_BLOCK.wrap(text);
        String unwrapped = FormatType.CODE_BLOCK.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void codeBlockIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.CODE_BLOCK.isWrapped("```\ntext\n```"));
        assertFalse(FormatType.CODE_BLOCK.isWrapped("text"));
        assertFalse(FormatType.CODE_BLOCK.isWrapped("`text`"));
    }

    // ==================== LINK Format Tests ====================

    @Test
    public void linkWrapProducesCorrectOutput() {
        String text = "Click here";
        String wrapped = FormatType.LINK.wrap(text);
        assertEquals("[Click here](url)", wrapped);
    }

    @Test
    public void linkUnwrapProducesCorrectOutput() {
        String wrapped = "[Click here](url)";
        String unwrapped = FormatType.LINK.unwrap(wrapped);
        assertEquals("Click here", unwrapped);
    }

    @Test
    public void linkRoundTrip() {
        String text = "Link text";
        String wrapped = FormatType.LINK.wrap(text);
        String unwrapped = FormatType.LINK.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void linkIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.LINK.isWrapped("[text](url)"));
        assertFalse(FormatType.LINK.isWrapped("text"));
    }

    // ==================== BULLET_LIST Format Tests ====================

    @Test
    public void bulletListWrapProducesCorrectOutput() {
        String text = "Item";
        String wrapped = FormatType.BULLET_LIST.wrap(text);
        assertEquals("- Item", wrapped);
    }

    @Test
    public void bulletListUnwrapProducesCorrectOutput() {
        String wrapped = "- Item";
        String unwrapped = FormatType.BULLET_LIST.unwrap(wrapped);
        assertEquals("Item", unwrapped);
    }

    @Test
    public void bulletListRoundTrip() {
        String text = "List item";
        String wrapped = FormatType.BULLET_LIST.wrap(text);
        String unwrapped = FormatType.BULLET_LIST.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void bulletListIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.BULLET_LIST.isWrapped("- text"));
        assertFalse(FormatType.BULLET_LIST.isWrapped("text"));
    }

    // ==================== ORDERED_LIST Format Tests ====================

    @Test
    public void orderedListWrapProducesCorrectOutput() {
        String text = "Item";
        String wrapped = FormatType.ORDERED_LIST.wrap(text);
        assertEquals("1. Item", wrapped);
    }

    @Test
    public void orderedListUnwrapProducesCorrectOutput() {
        String wrapped = "1. Item";
        String unwrapped = FormatType.ORDERED_LIST.unwrap(wrapped);
        assertEquals("Item", unwrapped);
    }

    @Test
    public void orderedListRoundTrip() {
        String text = "List item";
        String wrapped = FormatType.ORDERED_LIST.wrap(text);
        String unwrapped = FormatType.ORDERED_LIST.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void orderedListIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.ORDERED_LIST.isWrapped("1. text"));
        assertFalse(FormatType.ORDERED_LIST.isWrapped("text"));
    }

    // ==================== BLOCKQUOTE Format Tests ====================

    @Test
    public void blockquoteWrapProducesCorrectOutput() {
        String text = "Quote";
        String wrapped = FormatType.BLOCKQUOTE.wrap(text);
        assertEquals("> Quote", wrapped);
    }

    @Test
    public void blockquoteUnwrapProducesCorrectOutput() {
        String wrapped = "> Quote";
        String unwrapped = FormatType.BLOCKQUOTE.unwrap(wrapped);
        assertEquals("Quote", unwrapped);
    }

    @Test
    public void blockquoteRoundTrip() {
        String text = "Quoted text";
        String wrapped = FormatType.BLOCKQUOTE.wrap(text);
        String unwrapped = FormatType.BLOCKQUOTE.unwrap(wrapped);
        assertEquals(text, unwrapped);
    }

    @Test
    public void blockquoteIsWrappedDetectsWrappedText() {
        assertTrue(FormatType.BLOCKQUOTE.isWrapped("> text"));
        assertFalse(FormatType.BLOCKQUOTE.isWrapped("text"));
    }

    // ==================== Empty String Tests ====================

    @Test
    public void allFormatsHandleEmptyString() {
        for (FormatType format : FormatType.values()) {
            String wrapped = format.wrap("");
            String unwrapped = format.unwrap(wrapped);
            assertEquals("Empty string round-trip should work for " + format, "", unwrapped);
        }
    }

    // ==================== Unwrap on Non-Wrapped Text Tests ====================

    @Test
    public void unwrapOnNonWrappedTextReturnsOriginal() {
        String plainText = "plain text";
        for (FormatType format : FormatType.values()) {
            if (!format.isWrapped(plainText)) {
                String result = format.unwrap(plainText);
                assertEquals("Unwrap on non-wrapped text should return original for " + format, plainText, result);
            }
        }
    }

    // ==================== Wrap Length Tests ====================

    @Test
    public void wrapLengthIsCorrect() {
        String text = "test";
        for (FormatType format : FormatType.values()) {
            String wrapped = format.wrap(text);
            int expectedLength = text.length() + format.getPrefix().length() + format.getSuffix().length();
            assertEquals("Wrapped length should be correct for " + format, expectedLength, wrapped.length());
        }
    }

    // ==================== Special Characters Tests ====================

    @Test
    public void formatsHandleSpecialCharacters() {
        String[] specialTexts = {
                "Hello World!",
                "Test@#$%",
                "Unicode: 你好",
                "Emoji: 😀",
                "Newline:\ntext",
                "Tab:\ttext"
        };

        for (String text : specialTexts) {
            for (FormatType format : FormatType.values()) {
                String wrapped = format.wrap(text);
                String unwrapped = format.unwrap(wrapped);
                assertEquals("Special characters should be preserved for " + format + " with text: " + text, text, unwrapped);
            }
        }
    }

    // ==================== Toggle Idempotence Tests ====================

    @Test
    public void toggleIdempotence() {
        String text = "Test";
        for (FormatType format : FormatType.values()) {
            // First toggle: wrap
            String afterFirstToggle = format.wrap(text);
            assertTrue("After first toggle, should be wrapped for " + format, format.isWrapped(afterFirstToggle));

            // Second toggle: unwrap
            String afterSecondToggle = format.unwrap(afterFirstToggle);
            assertEquals("After second toggle, should be original for " + format, text, afterSecondToggle);
        }
    }

    @Test
    public void toggleIdempotenceMultipleCycles() {
        String text = "Test";
        for (FormatType format : FormatType.values()) {
            String current = text;

            // Toggle 1: wrap
            current = format.wrap(current);
            assertTrue("After toggle 1, should be wrapped", format.isWrapped(current));

            // Toggle 2: unwrap
            current = format.unwrap(current);
            assertEquals("After toggle 2, should be original", text, current);

            // Toggle 3: wrap again
            current = format.wrap(current);
            assertTrue("After toggle 3, should be wrapped again", format.isWrapped(current));

            // Toggle 4: unwrap again
            current = format.unwrap(current);
            assertEquals("After toggle 4, should be original again", text, current);
        }
    }

    // ==================== Prefix/Suffix Tests ====================

    @Test
    public void boldPrefixAndSuffix() {
        assertEquals("**", FormatType.BOLD.getPrefix());
        assertEquals("**", FormatType.BOLD.getSuffix());
    }

    @Test
    public void italicPrefixAndSuffix() {
        assertEquals("_", FormatType.ITALIC.getPrefix());
        assertEquals("_", FormatType.ITALIC.getSuffix());
    }

    @Test
    public void strikethroughPrefixAndSuffix() {
        assertEquals("~~", FormatType.STRIKETHROUGH.getPrefix());
        assertEquals("~~", FormatType.STRIKETHROUGH.getSuffix());
    }

    @Test
    public void inlineCodePrefixAndSuffix() {
        assertEquals("`", FormatType.INLINE_CODE.getPrefix());
        assertEquals("`", FormatType.INLINE_CODE.getSuffix());
    }

    @Test
    public void codeBlockPrefixAndSuffix() {
        assertEquals("```\n", FormatType.CODE_BLOCK.getPrefix());
        assertEquals("\n```", FormatType.CODE_BLOCK.getSuffix());
    }

    @Test
    public void linkPrefixAndSuffix() {
        assertEquals("[", FormatType.LINK.getPrefix());
        assertEquals("](url)", FormatType.LINK.getSuffix());
    }

    @Test
    public void bulletListPrefixAndSuffix() {
        assertEquals("- ", FormatType.BULLET_LIST.getPrefix());
        assertEquals("", FormatType.BULLET_LIST.getSuffix());
    }

    @Test
    public void orderedListPrefixAndSuffix() {
        assertEquals("1. ", FormatType.ORDERED_LIST.getPrefix());
        assertEquals("", FormatType.ORDERED_LIST.getSuffix());
    }

    @Test
    public void blockquotePrefixAndSuffix() {
        assertEquals("> ", FormatType.BLOCKQUOTE.getPrefix());
        assertEquals("", FormatType.BLOCKQUOTE.getSuffix());
    }
}
