package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier

/**
 * Bug Condition Exploration Test for Action Bubble Style Fix.
 *
 * This test verifies that `CometChatActionBubbleStyle` incorrectly contains
 * directional methods (`incoming()`, `outgoing()`) and a `resolve()` method,
 * as well as unnecessary properties that are inappropriate for center-aligned
 * action bubbles (system messages like "User joined the group").
 *
 * **Bug Context:**
 * Action bubbles are centered system messages that have no directional variants.
 * The class was likely created by copying from a directional bubble style class
 * (like `CometChatTextBubbleStyle`) without removing the directional-specific
 * methods and properties.
 *
 * **Formal Bug Condition:**
 * ```
 * FUNCTION isBugCondition(styleClass)
 *   RETURN styleClass.hasMethod("incoming")
 *          OR styleClass.hasMethod("outgoing")
 *          OR styleClass.hasMethod("resolve")
 *          OR styleClass.hasProperty("senderNameTextAppearance")
 *          OR styleClass.hasProperty("senderNameTextColor")
 *          OR styleClass.hasProperty("threadIndicatorTextAppearance")
 *          OR styleClass.hasProperty("threadIndicatorTextColor")
 *          OR styleClass.hasProperty("threadIndicatorIconTint")
 *          OR styleClass.hasProperty("timestampTextAppearance")
 *          OR styleClass.hasProperty("timestampTextColor")
 * END FUNCTION
 * ```
 *
 * **EXPECTED OUTCOME ON UNFIXED CODE:** Test FAILS
 * - The class contains `incoming()`, `outgoing()`, `resolve()` methods
 * - The class contains unnecessary properties like `senderNameTextAppearance`
 *
 * **EXPECTED OUTCOME ON FIXED CODE:** Test PASSES
 * - The class only has `default()` factory method
 * - The class only has action-bubble-specific properties
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 *
 * Feature: action-bubble-style-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ActionBubbleStyleExplorationTest {

    private val styleClass = CometChatActionBubbleStyle::class.java
    private val companionClass = CometChatActionBubbleStyle.Companion::class.java

    /**
     * Helper to get all method names from the companion object.
     */
    private fun getCompanionMethodNames(): List<String> {
        return companionClass.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) }
            .map { it.name }
    }

    /**
     * Helper to get all method names from the instance (non-static).
     */
    private fun getInstanceMethodNames(): List<String> {
        return styleClass.declaredMethods
            .filter { !Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
            .map { it.name }
    }

    /**
     * Helper to get all field names (properties) from the data class.
     */
    private fun getPropertyNames(): List<String> {
        return styleClass.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .map { it.name }
    }

    // ==================== Directional Method Tests ====================

    /**
     * Property 1: Fault Condition - No incoming() Method
     *
     * Action bubbles are centered system messages without incoming/outgoing variants.
     * The class SHALL NOT provide an `incoming()` factory method.
     *
     * **Validates: Requirements 1.1, 2.2**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have incoming() method`() {
        val companionMethods = getCompanionMethodNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: incoming(context) method
        // - Expected: No incoming() method (action bubbles are centered, not directional)
        //
        // Counterexample on failure:
        // - Found method: incoming(context: Context): CometChatActionBubbleStyle
        // - This incorrectly implies action bubbles have incoming variants
        assertFalse(
            "CometChatActionBubbleStyle should NOT have incoming() method - " +
                "action bubbles are centered system messages without directional variants. " +
                "Found companion methods: $companionMethods",
            companionMethods.contains("incoming")
        )
    }

    /**
     * Property 1: Fault Condition - No outgoing() Method
     *
     * Action bubbles are centered system messages without incoming/outgoing variants.
     * The class SHALL NOT provide an `outgoing()` factory method.
     *
     * **Validates: Requirements 1.2, 2.2**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have outgoing() method`() {
        val companionMethods = getCompanionMethodNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: outgoing(context) method
        // - Expected: No outgoing() method (action bubbles are centered, not directional)
        //
        // Counterexample on failure:
        // - Found method: outgoing(context: Context): CometChatActionBubbleStyle
        // - This incorrectly implies action bubbles have outgoing variants
        assertFalse(
            "CometChatActionBubbleStyle should NOT have outgoing() method - " +
                "action bubbles are centered system messages without directional variants. " +
                "Found companion methods: $companionMethods",
            companionMethods.contains("outgoing")
        )
    }

    /**
     * Property 1: Fault Condition - No resolve() Method
     *
     * Action bubble styling comes from MessageListStyle, not MessageBubbleStyle.
     * The class SHALL NOT provide a `resolve()` method for merging with MessageBubbleStyle.
     *
     * **Validates: Requirements 1.4, 2.3**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have resolve() method`() {
        val instanceMethods = getInstanceMethodNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: resolve(messageBubbleStyle) method
        // - Expected: No resolve() method (action bubble styling doesn't derive from MessageBubbleStyle)
        //
        // Counterexample on failure:
        // - Found method: resolve(messageBubbleStyle: CometChatMessageBubbleStyle): CometChatActionBubbleStyle
        // - This incorrectly merges properties with MessageBubbleStyle
        assertFalse(
            "CometChatActionBubbleStyle should NOT have resolve() method - " +
                "action bubble styling comes from MessageListStyle, not MessageBubbleStyle. " +
                "Found instance methods: $instanceMethods",
            instanceMethods.contains("resolve")
        )
    }

    // ==================== Unnecessary Property Tests ====================

    /**
     * Property 1: Fault Condition - No senderNameTextAppearance Property
     *
     * Action bubbles don't display sender names (they are system messages).
     * The class SHALL NOT include `senderNameTextAppearance` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have senderNameTextAppearance property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: senderNameTextAppearance property
        // - Expected: No senderNameTextAppearance (action bubbles don't display sender names)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have senderNameTextAppearance property - " +
                "action bubbles don't display sender names. Found properties: $properties",
            properties.contains("senderNameTextAppearance")
        )
    }

    /**
     * Property 1: Fault Condition - No senderNameTextColor Property
     *
     * Action bubbles don't display sender names (they are system messages).
     * The class SHALL NOT include `senderNameTextColor` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have senderNameTextColor property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: senderNameTextColor property
        // - Expected: No senderNameTextColor (action bubbles don't display sender names)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have senderNameTextColor property - " +
                "action bubbles don't display sender names. Found properties: $properties",
            properties.contains("senderNameTextColor")
        )
    }

    /**
     * Property 1: Fault Condition - No threadIndicatorTextAppearance Property
     *
     * Action bubbles don't display thread indicators (they are system messages).
     * The class SHALL NOT include `threadIndicatorTextAppearance` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have threadIndicatorTextAppearance property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: threadIndicatorTextAppearance property
        // - Expected: No threadIndicatorTextAppearance (action bubbles don't display thread indicators)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have threadIndicatorTextAppearance property - " +
                "action bubbles don't display thread indicators. Found properties: $properties",
            properties.contains("threadIndicatorTextAppearance")
        )
    }

    /**
     * Property 1: Fault Condition - No threadIndicatorTextColor Property
     *
     * Action bubbles don't display thread indicators (they are system messages).
     * The class SHALL NOT include `threadIndicatorTextColor` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have threadIndicatorTextColor property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: threadIndicatorTextColor property
        // - Expected: No threadIndicatorTextColor (action bubbles don't display thread indicators)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have threadIndicatorTextColor property - " +
                "action bubbles don't display thread indicators. Found properties: $properties",
            properties.contains("threadIndicatorTextColor")
        )
    }

    /**
     * Property 1: Fault Condition - No threadIndicatorIconTint Property
     *
     * Action bubbles don't display thread indicators (they are system messages).
     * The class SHALL NOT include `threadIndicatorIconTint` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have threadIndicatorIconTint property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: threadIndicatorIconTint property
        // - Expected: No threadIndicatorIconTint (action bubbles don't display thread indicators)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have threadIndicatorIconTint property - " +
                "action bubbles don't display thread indicators. Found properties: $properties",
            properties.contains("threadIndicatorIconTint")
        )
    }

    /**
     * Property 1: Fault Condition - No timestampTextAppearance Property
     *
     * Action bubbles don't display timestamps (they are system messages).
     * The class SHALL NOT include `timestampTextAppearance` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have timestampTextAppearance property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: timestampTextAppearance property
        // - Expected: No timestampTextAppearance (action bubbles don't display timestamps)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have timestampTextAppearance property - " +
                "action bubbles don't display timestamps. Found properties: $properties",
            properties.contains("timestampTextAppearance")
        )
    }

    /**
     * Property 1: Fault Condition - No timestampTextColor Property
     *
     * Action bubbles don't display timestamps (they are system messages).
     * The class SHALL NOT include `timestampTextColor` property.
     *
     * **Validates: Requirements 1.5, 2.4**
     */
    @Test
    fun `CometChatActionBubbleStyle should NOT have timestampTextColor property`() {
        val properties = getPropertyNames()

        // This assertion will FAIL on unfixed code because:
        // - Current implementation has: timestampTextColor property
        // - Expected: No timestampTextColor (action bubbles don't display timestamps)
        assertFalse(
            "CometChatActionBubbleStyle should NOT have timestampTextColor property - " +
                "action bubbles don't display timestamps. Found properties: $properties",
            properties.contains("timestampTextColor")
        )
    }

    // ==================== Expected API Tests ====================

    /**
     * Property 1: Expected Behavior - Should have default() Method
     *
     * Action bubbles should only have a `default()` factory method that returns
     * a style with theme-appropriate default values.
     *
     * **Validates: Requirements 2.1**
     */
    @Test
    fun `CometChatActionBubbleStyle should have default() method`() {
        val companionMethods = getCompanionMethodNames()

        // This should always pass - default() is the expected factory method
        assertTrue(
            "CometChatActionBubbleStyle should have default() method. " +
                "Found companion methods: $companionMethods",
            companionMethods.contains("default")
        )
    }

    /**
     * Property 1: Expected Behavior - Should have fromTypedArray() Method
     *
     * The class should provide `fromTypedArray()` for extracting styles from XML attributes.
     *
     * **Validates: Requirements 3.2**
     */
    @Test
    fun `CometChatActionBubbleStyle should have fromTypedArray() method`() {
        val companionMethods = getCompanionMethodNames()

        // This should always pass - fromTypedArray() is needed for XML attribute extraction
        assertTrue(
            "CometChatActionBubbleStyle should have fromTypedArray() method. " +
                "Found companion methods: $companionMethods",
            companionMethods.contains("fromTypedArray")
        )
    }

    // ==================== Expected Properties Tests ====================

    /**
     * Property 2: Preservation - Should have action-bubble-specific properties
     *
     * The class should include only properties relevant to action bubbles:
     * textColor, textAppearance, backgroundColor, backgroundDrawable,
     * cornerRadius, strokeWidth, strokeColor.
     *
     * **Validates: Requirements 2.4, 3.1**
     */
    @Test
    fun `CometChatActionBubbleStyle should have textColor property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have textColor property. Found: $properties",
            properties.contains("textColor")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have textAppearance property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have textAppearance property. Found: $properties",
            properties.contains("textAppearance")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have backgroundColor property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have backgroundColor property. Found: $properties",
            properties.contains("backgroundColor")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have backgroundDrawable property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have backgroundDrawable property. Found: $properties",
            properties.contains("backgroundDrawable")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have cornerRadius property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have cornerRadius property. Found: $properties",
            properties.contains("cornerRadius")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have strokeWidth property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have strokeWidth property. Found: $properties",
            properties.contains("strokeWidth")
        )
    }

    @Test
    fun `CometChatActionBubbleStyle should have strokeColor property`() {
        val properties = getPropertyNames()
        assertTrue(
            "CometChatActionBubbleStyle should have strokeColor property. Found: $properties",
            properties.contains("strokeColor")
        )
    }
}
