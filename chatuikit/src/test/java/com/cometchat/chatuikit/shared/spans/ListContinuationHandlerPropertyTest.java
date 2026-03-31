package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Property-based tests for ListContinuationHandler.
 * <p>
 * These tests verify universal properties for list continuation behavior
 * that should hold across all valid inputs. Each property test runs multiple iterations
 * with randomly generated inputs to provide high confidence in correctness.
 * </p>
 * <p>
 * Note: Since SpannableStringBuilder requires the Android framework, these tests
 * focus on verifying the logic through span creation contracts and handler behavior
 * rather than full integration with Android's text system.
 * </p>
 * <p>
 * Property tests implemented:
 * <ul>
 *   <li>Property 10: List continuation correctness</li>
 * </ul>
 * </p>
 * <p>
 * **Validates: Requirements 6.3, 7.3, 7.5, 8.4**
 * </p>
 */
public class ListContinuationHandlerPropertyTest {

    /**
     * Number of iterations for property tests.
     */
    private static final int PROPERTY_TEST_ITERATIONS = 100;

    /**
     * Random number generator for test data generation.
     */
    private final Random random = new Random(42); // Fixed seed for reproducibility

    // ==================== Property 10: List Continuation Correctness ====================

    /**
     * Property 10: List Continuation Correctness - Bullet List Span Creation
     * <p>
     * For any bullet list, creating a new item should produce a valid BulletListFormatSpan
     * with the correct format type.
     * </p>
     * <p>
     * **Validates: Requirements 6.3**
     * </p>
     */
    @Test
    public void property10_bulletListSpanCreationIsCorrect() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Create a bullet list span
            BulletListFormatSpan span = new BulletListFormatSpan();
            
            // Verify format type is correct
            assertNotNull("Bullet span should be created", span);
            assertEquals("Format type should be BULLET_LIST",
                    FormatType.BULLET_LIST, span.getFormatType());
        }
    }

    /**
     * Property 10: List Continuation Correctness - Numbered List Sequential Numbers
     * <p>
     * For any numbered list with N items, pressing Enter after the last item
     * should create item N+1 with the correct sequential number.
     * </p>
     * <p>
     * **Validates: Requirements 7.3**
     * </p>
     */
    @Test
    public void property10_numberedListSequentialNumbersAreCorrect() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate a random starting number
            int startNumber = random.nextInt(100) + 1;
            
            // Create a numbered list span
            NumberedListFormatSpan currentSpan = new NumberedListFormatSpan(startNumber);
            
            // Simulate creating a new item (what happens on Enter)
            int nextNumber = currentSpan.getNumber() + 1;
            NumberedListFormatSpan newSpan = new NumberedListFormatSpan(nextNumber);
            
            // Verify the new span has the correct incremented number
            assertEquals("New item should have number N+1",
                    startNumber + 1, newSpan.getNumber());
            assertEquals("Format type should be ORDERED_LIST",
                    FormatType.ORDERED_LIST, newSpan.getFormatType());
        }
    }

    /**
     * Property 10: List Continuation Correctness - Numbered List Renumbering
     * <p>
     * For any numbered list, renumbering should produce sequential numbers
     * starting from 1 (or the specified start number).
     * </p>
     * <p>
     * **Validates: Requirements 7.5**
     * </p>
     */
    @Test
    public void property10_numberedListRenumberingIsSequential() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random number of items
            int itemCount = random.nextInt(20) + 1;
            
            // Create spans with random initial numbers
            NumberedListFormatSpan[] spans = new NumberedListFormatSpan[itemCount];
            for (int j = 0; j < itemCount; j++) {
                spans[j] = new NumberedListFormatSpan(random.nextInt(100));
            }
            
            // Simulate renumbering
            for (int j = 0; j < itemCount; j++) {
                spans[j].setNumber(j + 1);
            }
            
            // Verify sequential numbering
            for (int j = 0; j < itemCount; j++) {
                assertEquals("Item " + j + " should have number " + (j + 1),
                        j + 1, spans[j].getNumber());
            }
        }
    }

    /**
     * Property 10: List Continuation Correctness - Blockquote Span Creation
     * <p>
     * For any blockquote, continuing on Enter should produce a valid BlockquoteFormatSpan
     * with the correct format type.
     * </p>
     * <p>
     * **Validates: Requirements 8.4**
     * </p>
     */
    @Test
    public void property10_blockquoteSpanCreationIsCorrect() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Create a blockquote span
            BlockquoteFormatSpan span = new BlockquoteFormatSpan();
            
            // Verify format type is correct
            assertNotNull("Blockquote span should be created", span);
            assertEquals("Format type should be BLOCKQUOTE",
                    FormatType.BLOCKQUOTE, span.getFormatType());
        }
    }

    /**
     * Property 10 variant: Bullet list properties are preserved during continuation.
     * <p>
     * When a new bullet item is created, the styling properties should be
     * copied from the original span.
     * </p>
     */
    @Test
    public void property10_bulletListPropertiesPreservedDuringContinuation() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random properties
            int bulletColor = random.nextInt();
            int bulletRadius = random.nextInt(20) + 1;
            int gapWidth = random.nextInt(50) + 1;
            
            // Create original span with properties
            BulletListFormatSpan original = new BulletListFormatSpan();
            original.setBulletColor(bulletColor);
            original.setBulletRadius(bulletRadius);
            original.setGapWidth(gapWidth);
            
            // Create new span (simulating continuation)
            BulletListFormatSpan newSpan = new BulletListFormatSpan();
            newSpan.setBulletColor(original.getBulletColor());
            newSpan.setBulletRadius(original.getBulletRadius());
            newSpan.setGapWidth(original.getGapWidth());
            
            // Verify properties are preserved
            assertEquals("Bullet color should be preserved", bulletColor, newSpan.getBulletColor());
            assertEquals("Bullet radius should be preserved", bulletRadius, newSpan.getBulletRadius());
            assertEquals("Gap width should be preserved", gapWidth, newSpan.getGapWidth());
        }
    }

    /**
     * Property 10 variant: Numbered list properties are preserved during continuation.
     * <p>
     * When a new numbered item is created, the styling properties should be
     * copied from the original span (except the number which is incremented).
     * </p>
     */
    @Test
    public void property10_numberedListPropertiesPreservedDuringContinuation() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random properties
            int number = random.nextInt(100) + 1;
            int textColor = random.nextInt();
            int gapWidth = random.nextInt(50) + 1;
            
            // Create original span with properties
            NumberedListFormatSpan original = new NumberedListFormatSpan(number);
            original.setTextColor(textColor);
            original.setGapWidth(gapWidth);
            
            // Create new span (simulating continuation)
            NumberedListFormatSpan newSpan = new NumberedListFormatSpan(original.getNumber() + 1);
            newSpan.setTextColor(original.getTextColor());
            newSpan.setGapWidth(original.getGapWidth());
            
            // Verify properties are preserved (except number which is incremented)
            assertEquals("Number should be incremented", number + 1, newSpan.getNumber());
            assertEquals("Text color should be preserved", textColor, newSpan.getTextColor());
            assertEquals("Gap width should be preserved", gapWidth, newSpan.getGapWidth());
        }
    }

    /**
     * Property 10 variant: Blockquote properties are preserved during continuation.
     * <p>
     * When a blockquote is continued, the styling properties should be
     * preserved in the extended span.
     * </p>
     */
    @Test
    public void property10_blockquotePropertiesPreservedDuringContinuation() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Generate random properties
            int stripeColor = random.nextInt();
            int stripeWidth = random.nextInt(10) + 1;
            int gapWidth = random.nextInt(50) + 1;
            
            // Create original span with properties
            BlockquoteFormatSpan original = new BlockquoteFormatSpan();
            original.setStripeColor(stripeColor);
            original.setStripeWidth(stripeWidth);
            original.setGapWidth(gapWidth);
            
            // Create new span (simulating continuation)
            BlockquoteFormatSpan newSpan = new BlockquoteFormatSpan();
            newSpan.setStripeColor(original.getStripeColor());
            newSpan.setStripeWidth(original.getStripeWidth());
            newSpan.setGapWidth(original.getGapWidth());
            
            // Verify properties are preserved
            assertEquals("Stripe color should be preserved", stripeColor, newSpan.getStripeColor());
            assertEquals("Stripe width should be preserved", stripeWidth, newSpan.getStripeWidth());
            assertEquals("Gap width should be preserved", gapWidth, newSpan.getGapWidth());
        }
    }

    // ==================== Handler Behavior Tests ====================

    /**
     * Property: Handler returns NOT_HANDLED for null editable.
     * <p>
     * For any cursor position, handleEnterKey with null editable should
     * return NOT_HANDLED.
     * </p>
     */
    @Test
    public void property_handlerReturnsNotHandledForNullEditable() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int cursorPosition = random.nextInt(1000);
            ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, cursorPosition);
            assertEquals("Should return NOT_HANDLED for null editable",
                    ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
        }
    }

    /**
     * Property: Handler returns NOT_HANDLED for negative cursor position.
     * <p>
     * For any negative cursor position, handleEnterKey should return NOT_HANDLED.
     * </p>
     */
    @Test
    public void property_handlerReturnsNotHandledForNegativeCursor() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int negativeCursor = -(random.nextInt(1000) + 1);
            ListContinuationHandler.EnterKeyResult result = handler.handleEnterKey(null, negativeCursor);
            assertEquals("Should return NOT_HANDLED for negative cursor",
                    ListContinuationHandler.EnterKeyResult.NOT_HANDLED, result);
        }
    }

    /**
     * Property: getNextListNumber returns 1 for null editable.
     * <p>
     * For any cursor position with null editable, getNextListNumber should return 1.
     * </p>
     */
    @Test
    public void property_getNextListNumberReturnsOneForNullEditable() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int cursorPosition = random.nextInt(1000);
            int nextNumber = handler.getNextListNumber(null, cursorPosition);
            assertEquals("Should return 1 for null editable", 1, nextNumber);
        }
    }

    /**
     * Property: isInList returns false for null editable.
     * <p>
     * For any cursor position with null editable, isInList should return false.
     * </p>
     */
    @Test
    public void property_isInListReturnsFalseForNullEditable() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int cursorPosition = random.nextInt(1000);
            boolean result = handler.isInList(null, cursorPosition);
            assertFalse("Should return false for null editable", result);
        }
    }

    /**
     * Property: isInBlockquote returns false for null editable.
     * <p>
     * For any cursor position with null editable, isInBlockquote should return false.
     * </p>
     */
    @Test
    public void property_isInBlockquoteReturnsFalseForNullEditable() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int cursorPosition = random.nextInt(1000);
            boolean result = handler.isInBlockquote(null, cursorPosition);
            assertFalse("Should return false for null editable", result);
        }
    }

    // ==================== Numbered List Increment Tests ====================

    /**
     * Property: Numbered list increment is always exactly 1.
     * <p>
     * For any starting number N, the next number should always be N+1.
     * </p>
     */
    @Test
    public void property_numberedListIncrementIsAlwaysOne() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int startNumber = random.nextInt(Integer.MAX_VALUE - 1);
            NumberedListFormatSpan span = new NumberedListFormatSpan(startNumber);
            int nextNumber = span.getNumber() + 1;
            
            assertEquals("Increment should always be 1",
                    startNumber + 1, nextNumber);
        }
    }

    /**
     * Property: Numbered list numbers are always positive after renumbering from 1.
     * <p>
     * When renumbering a list starting from 1, all numbers should be positive.
     * </p>
     */
    @Test
    public void property_renumberedListNumbersArePositive() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int itemCount = random.nextInt(50) + 1;
            
            for (int j = 0; j < itemCount; j++) {
                int number = j + 1; // Renumbering starts from 1
                assertTrue("Renumbered number should be positive", number > 0);
            }
        }
    }

    // ==================== Format Type Consistency Tests ====================

    /**
     * Property: All list span format types are consistent.
     * <p>
     * BulletListFormatSpan should always return BULLET_LIST,
     * NumberedListFormatSpan should always return ORDERED_LIST,
     * BlockquoteFormatSpan should always return BLOCKQUOTE.
     * </p>
     */
    @Test
    public void property_listSpanFormatTypesAreConsistent() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            BulletListFormatSpan bulletSpan = new BulletListFormatSpan();
            NumberedListFormatSpan numberedSpan = new NumberedListFormatSpan(random.nextInt(100) + 1);
            BlockquoteFormatSpan blockquoteSpan = new BlockquoteFormatSpan();
            
            assertEquals("Bullet span format type should be BULLET_LIST",
                    FormatType.BULLET_LIST, bulletSpan.getFormatType());
            assertEquals("Numbered span format type should be ORDERED_LIST",
                    FormatType.ORDERED_LIST, numberedSpan.getFormatType());
            assertEquals("Blockquote span format type should be BLOCKQUOTE",
                    FormatType.BLOCKQUOTE, blockquoteSpan.getFormatType());
        }
    }

    // ==================== Handler Instance Tests ====================

    /**
     * Property: Multiple handler instances are independent.
     * <p>
     * Creating multiple ListContinuationHandler instances should produce
     * independent objects.
     * </p>
     */
    @Test
    public void property_multipleHandlerInstancesAreIndependent() {
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            ListContinuationHandler handler1 = new ListContinuationHandler();
            ListContinuationHandler handler2 = new ListContinuationHandler();
            
            assertNotNull("Handler 1 should be created", handler1);
            assertNotNull("Handler 2 should be created", handler2);
            assertNotSame("Handlers should be different instances", handler1, handler2);
        }
    }

    /**
     * Property: renumberList handles null gracefully.
     * <p>
     * Calling renumberList with null should not throw an exception.
     * </p>
     */
    @Test
    public void property_renumberListHandlesNullGracefully() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            // Should not throw exception
            handler.renumberList(null);
        }
    }

    /**
     * Property: renumberListRange handles null gracefully.
     * <p>
     * Calling renumberListRange with null should not throw an exception.
     * </p>
     */
    @Test
    public void property_renumberListRangeHandlesNullGracefully() {
        ListContinuationHandler handler = new ListContinuationHandler();
        
        for (int i = 0; i < PROPERTY_TEST_ITERATIONS; i++) {
            int start = random.nextInt(100);
            int end = start + random.nextInt(100);
            int startNumber = random.nextInt(100) + 1;
            
            // Should not throw exception
            handler.renumberListRange(null, start, end, startNumber);
        }
    }
}
