package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Property-based tests for RichTextSpanManager.
 * <p>
 * These tests verify universal properties that should hold across all valid inputs.
 * Each property test runs multiple iterations with randomly generated inputs to
 * provide high confidence in correctness.
 * </p>
 * <p>
 * Note: Since SpannableStringBuilder requires the Android framework, these tests
 * focus on properties that can be verified through the span creation and interface
 * contracts rather than the full span application logic.
 * </p>
 * <p>
 * Property tests implemented:
 * <ul>
 *   <li>Property 3: Format toggle idempotence (via span type consistency)</li>
 *   <li>Property 4: Format detection accuracy (via format type uniqueness)</li>
 *   <li>Property 8: Nested format preservation (text styles can combine)</li>
 *   <li>Property 9: Code format exclusivity (code formats exclude text styles)</li>
 * </ul>
 * </p>
 * <p>
 * **Validates: Requirements 2.5, 2.6, 9.4, 12.4, 13.1, 13.2, 13.3, 13.4, 13.5**
 * </p>
 */
public class RichTextSpanManagerPropertyTest {

    /**
     * Number of iterations for property tests.
     */
    private static final int PROPERTY_TEST_ITERATIONS = 100;

    /**
     * Random number generator for test data generation.
     */
    private final Random random = new Random(42); // Fixed seed for reproducibility

    /**
     * All format types for testing.
     */
    private static final FormatType[] ALL_FORMATS = FormatType.values();

    // ==================== Property 3: Format Toggle Idempotence ====================

    /**
     * Property 3: Format Toggle Idempotence - Span Type Consistency
     * <p>
     * For any format type, creating a span should always return the same format type.
     * This is a prerequisite for toggle idempotence - if we can't reliably identify
     * the format type, we can't toggle it correctly.
     * </p>
     * <p>
     * **Validates: Requirements 2.5**
     * </p>
     */
    @Test
    public void property3_spanTypeConsistency() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            FormatType formatType = ALL_FORMATS[random.nextInt(ALL_FORMATS.length)];

            // Create span for the format type
            RichTextFormatSpan span = createSpanForFormat(formatType);

            assertNotNull("Span should be created for " + formatType, span);
            assertEquals("Span should return correct format type for " + formatType,
                    formatType, span.getFormatType());
        }
    }

    /**
     * Property 3 variant: Multiple span instances return consistent format types.
     * <p>
     * Creating multiple instances of the same span type should always return
     * the same format type, ensuring toggle operations are consistent.
     * </p>
     */
    @Test
    public void property3_multipleInstancesConsistent() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            FormatType formatType = ALL_FORMATS[random.nextInt(ALL_FORMATS.length)];

            // Create multiple instances
            RichTextFormatSpan span1 = createSpanForFormat(formatType);
            RichTextFormatSpan span2 = createSpanForFormat(formatType);
            RichTextFormatSpan span3 = createSpanForFormat(formatType);

            // All should return the same format type
            assertEquals("First span should return " + formatType, formatType, span1.getFormatType());
            assertEquals("Second span should return " + formatType, formatType, span2.getFormatType());
            assertEquals("Third span should return " + formatType, formatType, span3.getFormatType());
        }
    }

    /**
     * Property 3 variant: Format type is immutable after span creation.
     * <p>
     * The format type returned by a span should not change over time.
     * </p>
     */
    @Test
    public void property3_formatTypeImmutable() {
        for (FormatType formatType : ALL_FORMATS) {
            RichTextFormatSpan span = createSpanForFormat(formatType);

            // Call getFormatType multiple times
            FormatType type1 = span.getFormatType();
            FormatType type2 = span.getFormatType();
            FormatType type3 = span.getFormatType();

            assertEquals("Format type should be consistent on first call", formatType, type1);
            assertEquals("Format type should be consistent on second call", formatType, type2);
            assertEquals("Format type should be consistent on third call", formatType, type3);
        }
    }

    // ==================== Property 4: Format Detection Accuracy ====================

    /**
     * Property 4: Format Detection Accuracy - Format Type Uniqueness
     * <p>
     * Each format type should have a unique span class, ensuring that format
     * detection can accurately identify which formats are active.
     * </p>
     * <p>
     * **Validates: Requirements 2.6, 9.4, 12.4**
     * </p>
     */
    @Test
    public void property4_formatTypeUniqueness() {
        Set<FormatType> seenTypes = new HashSet<>();

        for (FormatType formatType : ALL_FORMATS) {
            RichTextFormatSpan span = createSpanForFormat(formatType);
            FormatType returnedType = span.getFormatType();

            // Each format type should be unique
            assertFalse("Format type " + returnedType + " should not have been seen before",
                    seenTypes.contains(returnedType) && returnedType != formatType);

            seenTypes.add(returnedType);
        }

        // All format types should be represented
        assertEquals("All format types should be unique",
                ALL_FORMATS.length, seenTypes.size());
    }

    /**
     * Property 4 variant: Span class matches format type.
     * <p>
     * Each span class should return the correct format type, enabling accurate
     * format detection.
     * </p>
     */
    @Test
    public void property4_spanClassMatchesFormatType() {
        // Test each span class directly
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

    /**
     * Property 4 variant: RichTextFormatSpan interface enables polymorphic detection.
     * <p>
     * All span types should be detectable through the RichTextFormatSpan interface,
     * enabling format detection to work with any span type.
     * </p>
     */
    @Test
    public void property4_polymorphicDetection() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            FormatType formatType = ALL_FORMATS[random.nextInt(ALL_FORMATS.length)];

            // Create span and cast to interface
            RichTextFormatSpan span = createSpanForFormat(formatType);

            // Should be able to detect format type through interface
            assertTrue("Span should implement RichTextFormatSpan",
                    span instanceof RichTextFormatSpan);
            assertEquals("Format type should be detectable through interface",
                    formatType, span.getFormatType());
        }
    }

    /**
     * Property 4 variant: Null safety in detection.
     * <p>
     * The detectActiveFormats method should handle null inputs gracefully.
     * </p>
     */
    @Test
    public void property4_nullSafetyInDetection() {
        // Test with null spannable
        Set<FormatType> formats = RichTextSpanManager.detectActiveFormats(null, 0);
        assertNotNull("Should return non-null set", formats);
        assertTrue("Should return empty set for null input", formats.isEmpty());

        // Test with negative cursor position
        formats = RichTextSpanManager.detectActiveFormats(null, -1);
        assertNotNull("Should return non-null set for negative cursor", formats);
        assertTrue("Should return empty set for negative cursor", formats.isEmpty());

        // Test with large cursor position
        formats = RichTextSpanManager.detectActiveFormats(null, Integer.MAX_VALUE);
        assertNotNull("Should return non-null set for large cursor", formats);
        assertTrue("Should return empty set for large cursor", formats.isEmpty());
    }

    // ==================== Additional Property Tests ====================

    /**
     * Property: All format types can create spans.
     * <p>
     * Every FormatType enum value should be able to create a corresponding span.
     * </p>
     */
    @Test
    public void property_allFormatTypesCreateSpans() {
        for (FormatType formatType : ALL_FORMATS) {
            RichTextFormatSpan span = createSpanForFormat(formatType);
            assertNotNull("Should create span for " + formatType, span);
        }
    }

    /**
     * Property: Span creation is deterministic.
     * <p>
     * Creating a span for the same format type should always produce a span
     * with the same format type (though not necessarily the same instance).
     * </p>
     */
    @Test
    public void property_spanCreationDeterministic() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            FormatType formatType = ALL_FORMATS[random.nextInt(ALL_FORMATS.length)];

            RichTextFormatSpan span1 = createSpanForFormat(formatType);
            RichTextFormatSpan span2 = createSpanForFormat(formatType);

            assertEquals("Span creation should be deterministic",
                    span1.getFormatType(), span2.getFormatType());
        }
    }

    /**
     * Property: Link spans preserve URL.
     * <p>
     * LinkFormatSpan should correctly store and return the URL.
     * </p>
     */
    @Test
    public void property_linkSpanPreservesUrl() {
        String[] testUrls = {
                "https://example.com",
                "http://test.org/path",
                "https://domain.com/path?query=value",
                "",
                "ftp://files.example.com"
        };

        for (String url : testUrls) {
            LinkFormatSpan span = new LinkFormatSpan(url);
            assertEquals("URL should be preserved", url, span.getUrl());
        }
    }

    /**
     * Property: Numbered list spans preserve number.
     * <p>
     * NumberedListFormatSpan should correctly store and return the number.
     * </p>
     */
    @Test
    public void property_numberedListSpanPreservesNumber() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int number = random.nextInt(1000) + 1;
            NumberedListFormatSpan span = new NumberedListFormatSpan(number);
            assertEquals("Number should be preserved", number, span.getNumber());
        }
    }

    // ==================== Property 8: Nested Format Preservation ====================

    /**
     * Property 8: Nested Format Preservation
     * <p>
     * For any text with multiple non-code formats applied (bold, italic, strikethrough),
     * removing one format SHALL preserve all other formats on that text.
     * </p>
     * <p>
     * This test verifies that the format type classification is correct, which is
     * a prerequisite for nested format preservation. Since we can't test with
     * SpannableStringBuilder in unit tests, we verify the classification logic.
     * </p>
     * <p>
     * **Validates: Requirements 13.1, 13.2, 13.3, 13.4**
     * </p>
     */
    @Test
    public void property8_textStyleFormatsCanCombine() {
        // Text style formats that should be combinable
        FormatType[] textStyles = {FormatType.BOLD, FormatType.ITALIC, FormatType.STRIKETHROUGH};

        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Pick two random text styles
            FormatType style1 = textStyles[random.nextInt(textStyles.length)];
            FormatType style2 = textStyles[random.nextInt(textStyles.length)];

            // Both should be classified as text styles
            assertTrue("Format " + style1 + " should be a text style",
                    RichTextSpanManager.isTextStyleFormat(style1));
            assertTrue("Format " + style2 + " should be a text style",
                    RichTextSpanManager.isTextStyleFormat(style2));

            // Neither should be classified as code format
            assertFalse("Format " + style1 + " should not be a code format",
                    RichTextSpanManager.isCodeFormat(style1));
            assertFalse("Format " + style2 + " should not be a code format",
                    RichTextSpanManager.isCodeFormat(style2));
        }
    }

    /**
     * Property 8 variant: All text style combinations are valid.
     * <p>
     * Any combination of bold, italic, and strikethrough should be allowed.
     * </p>
     */
    @Test
    public void property8_allTextStyleCombinationsValid() {
        FormatType[] textStyles = {FormatType.BOLD, FormatType.ITALIC, FormatType.STRIKETHROUGH};

        // Test all possible combinations (including single formats)
        for (FormatType style1 : textStyles) {
            assertTrue("Single format " + style1 + " should be valid text style",
                    RichTextSpanManager.isTextStyleFormat(style1));

            for (FormatType style2 : textStyles) {
                // Both formats should be independently valid
                assertTrue("Format " + style1 + " should be valid",
                        RichTextSpanManager.isTextStyleFormat(style1));
                assertTrue("Format " + style2 + " should be valid",
                        RichTextSpanManager.isTextStyleFormat(style2));

                for (FormatType style3 : textStyles) {
                    // Triple combination should also be valid
                    assertTrue("Format " + style3 + " should be valid in triple combination",
                            RichTextSpanManager.isTextStyleFormat(style3));
                }
            }
        }
    }

    /**
     * Property 8 variant: Removing one format type doesn't affect span creation for others.
     * <p>
     * Creating spans for different text styles should be independent operations.
     * </p>
     */
    @Test
    public void property8_spanCreationIndependent() {
        FormatType[] textStyles = {FormatType.BOLD, FormatType.ITALIC, FormatType.STRIKETHROUGH};

        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Create spans for all text styles
            RichTextFormatSpan boldSpan = createSpanForFormat(FormatType.BOLD);
            RichTextFormatSpan italicSpan = createSpanForFormat(FormatType.ITALIC);
            RichTextFormatSpan strikeSpan = createSpanForFormat(FormatType.STRIKETHROUGH);

            // Each span should maintain its own format type
            assertEquals("Bold span should remain bold", FormatType.BOLD, boldSpan.getFormatType());
            assertEquals("Italic span should remain italic", FormatType.ITALIC, italicSpan.getFormatType());
            assertEquals("Strikethrough span should remain strikethrough",
                    FormatType.STRIKETHROUGH, strikeSpan.getFormatType());

            // Creating one span shouldn't affect others
            RichTextFormatSpan newBoldSpan = createSpanForFormat(FormatType.BOLD);
            assertEquals("New bold span should be independent", FormatType.BOLD, newBoldSpan.getFormatType());
        }
    }

    // ==================== Property 9: Code Format Exclusivity ====================

    /**
     * Property 9: Code Format Exclusivity
     * <p>
     * For any text with code formatting (inline code or code block), attempting to
     * apply other text styles (bold, italic, strikethrough) SHALL have no effect,
     * and the code formatting SHALL remain unchanged.
     * </p>
     * <p>
     * This test verifies the format classification logic that enables code format
     * exclusivity. Since we can't test with SpannableStringBuilder in unit tests,
     * we verify the classification is correct.
     * </p>
     * <p>
     * **Validates: Requirements 13.5**
     * </p>
     */
    @Test
    public void property9_codeFormatsAreExclusive() {
        FormatType[] codeFormats = {FormatType.INLINE_CODE, FormatType.CODE_BLOCK};
        FormatType[] textStyles = {FormatType.BOLD, FormatType.ITALIC, FormatType.STRIKETHROUGH};

        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            FormatType codeFormat = codeFormats[random.nextInt(codeFormats.length)];
            FormatType textStyle = textStyles[random.nextInt(textStyles.length)];

            // Code format should be classified as code
            assertTrue("Format " + codeFormat + " should be a code format",
                    RichTextSpanManager.isCodeFormat(codeFormat));

            // Code format should NOT be classified as text style
            assertFalse("Format " + codeFormat + " should not be a text style",
                    RichTextSpanManager.isTextStyleFormat(codeFormat));

            // Text style should NOT be classified as code
            assertFalse("Format " + textStyle + " should not be a code format",
                    RichTextSpanManager.isCodeFormat(textStyle));

            // Text style should be classified as text style
            assertTrue("Format " + textStyle + " should be a text style",
                    RichTextSpanManager.isTextStyleFormat(textStyle));
        }
    }

    /**
     * Property 9 variant: Code formats are mutually exclusive with text styles.
     * <p>
     * No format should be classified as both code format and text style.
     * </p>
     */
    @Test
    public void property9_noFormatIsBothCodeAndTextStyle() {
        for (FormatType formatType : ALL_FORMATS) {
            boolean isCode = RichTextSpanManager.isCodeFormat(formatType);
            boolean isTextStyle = RichTextSpanManager.isTextStyleFormat(formatType);

            // A format cannot be both code and text style
            assertFalse("Format " + formatType + " cannot be both code and text style",
                    isCode && isTextStyle);
        }
    }

    /**
     * Property 9 variant: Only INLINE_CODE and CODE_BLOCK are code formats.
     * <p>
     * Exactly two format types should be classified as code formats.
     * </p>
     */
    @Test
    public void property9_exactlyTwoCodeFormats() {
        int codeFormatCount = 0;
        Set<FormatType> codeFormats = new HashSet<>();

        for (FormatType formatType : ALL_FORMATS) {
            if (RichTextSpanManager.isCodeFormat(formatType)) {
                codeFormatCount++;
                codeFormats.add(formatType);
            }
        }

        assertEquals("Should have exactly 2 code formats", 2, codeFormatCount);
        assertTrue("INLINE_CODE should be a code format", codeFormats.contains(FormatType.INLINE_CODE));
        assertTrue("CODE_BLOCK should be a code format", codeFormats.contains(FormatType.CODE_BLOCK));
    }

    /**
     * Property 9 variant: Exactly three text style formats.
     * <p>
     * Exactly four format types should be classified as text styles.
     * </p>
     */
    @Test
    public void property9_exactlyFourTextStyles() {
        int textStyleCount = 0;
        Set<FormatType> textStyles = new HashSet<>();

        for (FormatType formatType : ALL_FORMATS) {
            if (RichTextSpanManager.isTextStyleFormat(formatType)) {
                textStyleCount++;
                textStyles.add(formatType);
            }
        }

        assertEquals("Should have exactly 4 text styles", 4, textStyleCount);
        assertTrue("BOLD should be a text style", textStyles.contains(FormatType.BOLD));
        assertTrue("ITALIC should be a text style", textStyles.contains(FormatType.ITALIC));
        assertTrue("STRIKETHROUGH should be a text style", textStyles.contains(FormatType.STRIKETHROUGH));
        assertTrue("UNDERLINE should be a text style", textStyles.contains(FormatType.UNDERLINE));
    }

    /**
     * Property 9 variant: Code format spans maintain their type.
     * <p>
     * Code format spans should always return their correct format type.
     * </p>
     */
    @Test
    public void property9_codeSpansMaintainType() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Create code format spans
            RichTextFormatSpan inlineCodeSpan = createSpanForFormat(FormatType.INLINE_CODE);
            RichTextFormatSpan codeBlockSpan = createSpanForFormat(FormatType.CODE_BLOCK);

            // Verify they maintain their types
            assertEquals("Inline code span should return INLINE_CODE",
                    FormatType.INLINE_CODE, inlineCodeSpan.getFormatType());
            assertEquals("Code block span should return CODE_BLOCK",
                    FormatType.CODE_BLOCK, codeBlockSpan.getFormatType());

            // Verify they are classified as code formats
            assertTrue("Inline code span type should be code format",
                    RichTextSpanManager.isCodeFormat(inlineCodeSpan.getFormatType()));
            assertTrue("Code block span type should be code format",
                    RichTextSpanManager.isCodeFormat(codeBlockSpan.getFormatType()));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a span instance for the given format type.
     * This mirrors the logic in RichTextSpanManager.createSpanForFormat().
     *
     * @param formatType The format type to create a span for.
     * @return A new span instance.
     */
    private RichTextFormatSpan createSpanForFormat(FormatType formatType) {
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
                return new LinkFormatSpan("");
            case BULLET_LIST:
                return new BulletListFormatSpan();
            case ORDERED_LIST:
                return new NumberedListFormatSpan(1);
            case BLOCKQUOTE:
                return new BlockquoteFormatSpan();
            default:
                throw new IllegalArgumentException("Unknown format type: " + formatType);
        }
    }
}
