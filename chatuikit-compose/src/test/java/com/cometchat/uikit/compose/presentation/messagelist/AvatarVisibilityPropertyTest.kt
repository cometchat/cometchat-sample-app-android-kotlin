package com.cometchat.uikit.compose.presentation.messagelist

import com.cometchat.uikit.compose.presentation.messagelist.ui.shouldShowAvatar
import com.cometchat.uikit.compose.presentation.shared.messagebubble.LeadingViewResolution
import com.cometchat.uikit.compose.presentation.shared.messagebubble.resolveLeadingView
import com.cometchat.uikit.compose.presentation.shared.messagebubble.resolveLeadingViewWithFactory
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for verifying avatar visibility logic in the message list.
 *
 * **Feature: message-list-avatar-parity**
 * **Property 1: Outgoing Messages Never Show Default Avatar**
 *
 * **Validates: Requirements 1.1**
 *
 * For any message with RIGHT alignment (outgoing) and no custom leadingView provider,
 * the resolved leading view SHALL be null when using default avatar visibility settings.
 *
 * This test verifies the [shouldShowAvatar] function correctly implements the avatar
 * visibility rules defined in the design document:
 *
 * ## Decision Table:
 * | Alignment | hideAvatar | isGroupConversation | Show Avatar? |
 * |-----------|------------|---------------------|--------------|
 * | RIGHT     | false      | true                | NO           |
 * | RIGHT     | false      | false               | NO           |
 * | RIGHT     | true       | true                | NO           |
 * | RIGHT     | true       | false               | NO           |
 * | CENTER    | false      | true                | NO           |
 * | CENTER    | false      | false               | NO           |
 * | CENTER    | true       | true                | NO           |
 * | CENTER    | true       | false               | NO           |
 * | LEFT      | false      | true                | YES          |
 * | LEFT      | false      | false               | NO           |
 * | LEFT      | true       | true                | NO           |
 * | LEFT      | true       | false               | NO           |
 */
class AvatarVisibilityPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators for property-based testing
    // ============================================================================

    /**
     * Generates random boolean values for hideAvatar flag.
     */
    val hideAvatarArb = Arb.boolean()

    /**
     * Generates random boolean values for isGroupConversation flag.
     */
    val isGroupConversationArb = Arb.boolean()

    /**
     * Generates random MessageBubbleAlignment values.
     */
    val alignmentArb = Arb.enum<UIKitConstants.MessageBubbleAlignment>()

    /**
     * Generates random boolean values for hasCustomLeadingView flag.
     * Used for Property 2 tests.
     */
    val hasCustomLeadingViewArb = Arb.boolean()

    /**
     * Generates random boolean values for shouldShowDefaultAvatar flag.
     * Used for Property 2 tests.
     */
    val shouldShowDefaultAvatarArb = Arb.boolean()

    // ============================================================================
    // Property 1: Outgoing Messages Never Show Default Avatar
    // ============================================================================

    /**
     * Property test: For any message with RIGHT alignment (outgoing), the avatar
     * should NEVER be shown, regardless of hideAvatar or isGroupConversation values.
     *
     * This property ensures that outgoing messages always hide the default avatar,
     * matching the standard chat pattern where only incoming messages show sender avatars.
     *
     * **Validates: Requirements 1.1**
     */
    "Property 1: Outgoing messages (RIGHT alignment) should never show default avatar" {
        checkAll(100, hideAvatarArb, isGroupConversationArb) { hideAvatar, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                hideAvatar = hideAvatar,
                isGroupConversation = isGroupConversation
            )

            // Outgoing messages should NEVER show avatar regardless of other flags
            result shouldBe false
        }
    }

    /**
     * Property test: For any message with RIGHT alignment, even when hideAvatar is false
     * and isGroupConversation is true (the most permissive settings), the avatar should
     * still not be shown.
     *
     * This is a specific case of Property 1 that explicitly tests the most permissive
     * configuration to ensure outgoing messages never show avatars.
     *
     * **Validates: Requirements 1.1**
     */
    "Property 1 (specific): RIGHT alignment with permissive settings should still hide avatar" {
        // Most permissive settings: hideAvatar=false, isGroupConversation=true
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
            hideAvatar = false,
            isGroupConversation = true
        )

        result shouldBe false
    }

    /**
     * Property test: For any combination of hideAvatar and isGroupConversation,
     * RIGHT-aligned messages should always return false from shouldShowAvatar.
     *
     * This test generates all 4 combinations of the boolean flags and verifies
     * that RIGHT alignment always results in hidden avatar.
     *
     * **Validates: Requirements 1.1**
     */
    "Property 1 (exhaustive): All boolean combinations with RIGHT alignment should hide avatar" {
        // Test all 4 combinations explicitly
        val combinations = listOf(
            Pair(false, false),
            Pair(false, true),
            Pair(true, false),
            Pair(true, true)
        )

        combinations.forEach { (hideAvatar, isGroupConversation) ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                hideAvatar = hideAvatar,
                isGroupConversation = isGroupConversation
            )

            result shouldBe false
        }
    }

    // ============================================================================
    // Additional property tests for completeness (supporting Property 1 context)
    // ============================================================================

    /**
     * Property test: CENTER-aligned messages (action/system messages) should also
     * never show avatars, similar to RIGHT-aligned messages.
     *
     * This test ensures that action messages (like "User joined the group") don't
     * display avatars, which is consistent with the design document.
     *
     * **Validates: Requirements 1.1** (by extension - CENTER alignment rule)
     */
    "CENTER alignment should never show avatar regardless of other flags" {
        checkAll(100, hideAvatarArb, isGroupConversationArb) { hideAvatar, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
                hideAvatar = hideAvatar,
                isGroupConversation = isGroupConversation
            )

            // CENTER-aligned messages should NEVER show avatar
            result shouldBe false
        }
    }

    /**
     * Property test: LEFT-aligned messages (incoming) should only show avatar
     * when hideAvatar is false AND isGroupConversation is true.
     *
     * This test verifies the complete avatar visibility logic for incoming messages,
     * ensuring that avatars are only shown in group conversations when not explicitly hidden.
     *
     * **Validates: Requirements 1.1** (by providing contrast to outgoing behavior)
     */
    "LEFT alignment should show avatar only when hideAvatar=false AND isGroupConversation=true" {
        checkAll(100, hideAvatarArb, isGroupConversationArb) { hideAvatar, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = hideAvatar,
                isGroupConversation = isGroupConversation
            )

            // LEFT-aligned messages show avatar only when:
            // - hideAvatar is false (not explicitly hidden)
            // - AND isGroupConversation is true (group chat context)
            val expected = !hideAvatar && isGroupConversation
            result shouldBe expected
        }
    }

    /**
     * Property test: For any alignment, when hideAvatar is true, the avatar should
     * never be shown. This tests the master override behavior.
     *
     * **Validates: Requirements 1.1** (hideAvatar master override)
     */
    "hideAvatar=true should always hide avatar regardless of alignment or conversation type" {
        checkAll(100, alignmentArb, isGroupConversationArb) { alignment, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // When hideAvatar is true, avatar should NEVER be shown
            result shouldBe false
        }
    }

    /**
     * Property test: The only case where avatar is shown is when:
     * - alignment is LEFT (incoming message)
     * - hideAvatar is false
     * - isGroupConversation is true
     *
     * This test verifies the complete decision table from the design document.
     *
     * **Validates: Requirements 1.1** (complete decision table verification)
     */
    "Avatar should only be shown for LEFT alignment with hideAvatar=false and isGroupConversation=true" {
        checkAll(100, alignmentArb, hideAvatarArb, isGroupConversationArb) { alignment, hideAvatar, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = hideAvatar,
                isGroupConversation = isGroupConversation
            )

            // Avatar is shown ONLY when all three conditions are met:
            // 1. alignment is LEFT
            // 2. hideAvatar is false
            // 3. isGroupConversation is true
            val expected = alignment == UIKitConstants.MessageBubbleAlignment.LEFT &&
                    !hideAvatar &&
                    isGroupConversation

            result shouldBe expected
        }
    }

    // ============================================================================
    // Property 2: Custom LeadingView Providers Override Default Behavior
    // ============================================================================

    /**
     * Property test: For any message with any alignment, when a custom leadingView
     * slot provider is explicitly passed, the custom leading view SHALL be rendered
     * regardless of avatar visibility configuration.
     *
     * This property ensures that developers can always override the default avatar
     * behavior by providing a custom leadingView composable.
     *
     * **Feature: message-list-avatar-parity**
     * **Property 2: Custom LeadingView Providers Override Default Behavior**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 2: Custom leadingView should always be used regardless of shouldShowDefaultAvatar" {
        checkAll(100, shouldShowDefaultAvatarArb) { shouldShowDefaultAvatar ->
            // When a custom leadingView is provided (hasCustomLeadingView = true)
            val result = resolveLeadingView(
                hasCustomLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            // Custom leadingView should ALWAYS be used, regardless of shouldShowDefaultAvatar
            result shouldBe LeadingViewResolution.CustomLeadingView
        }
    }

    /**
     * Property test: When a custom leadingView is provided, the resolution should
     * return CustomLeadingView even when shouldShowDefaultAvatar is false.
     *
     * This is a specific case of Property 2 that explicitly tests the scenario
     * where avatar visibility would normally be hidden (e.g., outgoing messages).
     *
     * **Validates: Requirements 1.2**
     */
    "Property 2 (specific): Custom leadingView with shouldShowDefaultAvatar=false should still use custom view" {
        val result = resolveLeadingView(
            hasCustomLeadingView = true,
            shouldShowDefaultAvatar = false
        )

        result shouldBe LeadingViewResolution.CustomLeadingView
    }

    /**
     * Property test: When a custom leadingView is provided, the resolution should
     * return CustomLeadingView even when shouldShowDefaultAvatar is true.
     *
     * This verifies that custom views take precedence over factory defaults.
     *
     * **Validates: Requirements 1.2**
     */
    "Property 2 (specific): Custom leadingView with shouldShowDefaultAvatar=true should still use custom view" {
        val result = resolveLeadingView(
            hasCustomLeadingView = true,
            shouldShowDefaultAvatar = true
        )

        result shouldBe LeadingViewResolution.CustomLeadingView
    }

    /**
     * Property test: For all combinations of shouldShowDefaultAvatar, when a custom
     * leadingView is provided, the result should always be CustomLeadingView.
     *
     * This exhaustive test verifies both boolean values explicitly.
     *
     * **Validates: Requirements 1.2**
     */
    "Property 2 (exhaustive): All shouldShowDefaultAvatar values with custom leadingView should use custom view" {
        val shouldShowDefaultAvatarValues = listOf(true, false)

        shouldShowDefaultAvatarValues.forEach { shouldShowDefaultAvatar ->
            val result = resolveLeadingView(
                hasCustomLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            result shouldBe LeadingViewResolution.CustomLeadingView
        }
    }

    /**
     * Property test: When no custom leadingView is provided and shouldShowDefaultAvatar
     * is false, the resolution should return NoLeadingView.
     *
     * This verifies the complementary behavior to Property 2 - when there's no custom
     * view and avatar visibility is disabled, no leading view should be shown.
     *
     * **Validates: Requirements 1.1** (by contrast with Property 2)
     */
    "No custom leadingView with shouldShowDefaultAvatar=false should return NoLeadingView" {
        val result = resolveLeadingView(
            hasCustomLeadingView = false,
            shouldShowDefaultAvatar = false
        )

        result shouldBe LeadingViewResolution.NoLeadingView
    }

    /**
     * Property test: When no custom leadingView is provided and shouldShowDefaultAvatar
     * is true, the resolution should return FactoryLeadingView.
     *
     * This verifies that factory defaults are used when no custom view is provided
     * and avatar visibility is enabled.
     *
     * **Validates: Requirements 4.1** (factory integration)
     */
    "No custom leadingView with shouldShowDefaultAvatar=true should return FactoryLeadingView" {
        val result = resolveLeadingView(
            hasCustomLeadingView = false,
            shouldShowDefaultAvatar = true
        )

        result shouldBe LeadingViewResolution.FactoryLeadingView
    }

    /**
     * Property test: The resolveLeadingView function should correctly handle all
     * four combinations of hasCustomLeadingView and shouldShowDefaultAvatar.
     *
     * This comprehensive test verifies the complete decision matrix for leading
     * view resolution.
     *
     * **Validates: Requirements 1.2** (complete resolution logic)
     */
    "resolveLeadingView should handle all combinations correctly" {
        checkAll(100, hasCustomLeadingViewArb, shouldShowDefaultAvatarArb) { hasCustomLeadingView, shouldShowDefaultAvatar ->
            val result = resolveLeadingView(
                hasCustomLeadingView = hasCustomLeadingView,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            val expected = when {
                hasCustomLeadingView -> LeadingViewResolution.CustomLeadingView
                !shouldShowDefaultAvatar -> LeadingViewResolution.NoLeadingView
                else -> LeadingViewResolution.FactoryLeadingView
            }

            result shouldBe expected
        }
    }

    /**
     * Property test: Custom leadingView should override default behavior for any
     * message alignment (LEFT, RIGHT, CENTER).
     *
     * This test combines the alignment generator with the custom leadingView behavior
     * to verify that custom views work correctly regardless of message alignment.
     *
     * **Validates: Requirements 1.2** (alignment independence)
     */
    "Property 2: Custom leadingView should override for any alignment" {
        checkAll(100, alignmentArb, shouldShowDefaultAvatarArb) { alignment, shouldShowDefaultAvatar ->
            // Compute shouldShowDefaultAvatar based on alignment (simulating real usage)
            // For this test, we're verifying that custom leadingView overrides regardless
            val result = resolveLeadingView(
                hasCustomLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            // Custom leadingView should ALWAYS be used, regardless of alignment or shouldShowDefaultAvatar
            result shouldBe LeadingViewResolution.CustomLeadingView
        }
    }

    // ============================================================================
    // Property 3: Custom Factory Leading Views Override Visibility Settings
    // ============================================================================

    /**
     * Generates random boolean values for factoryProvidesLeadingView flag.
     * Used for Property 3 tests.
     */
    val factoryProvidesLeadingViewArb = Arb.boolean()

    /**
     * Property test: For any message where the BubbleFactory returns a non-null
     * leading view, that leading view SHALL be rendered regardless of the
     * shouldShowDefaultAvatar flag.
     *
     * This property ensures that custom factories can always provide their own
     * leading views, overriding the default avatar visibility settings.
     *
     * **Feature: message-list-avatar-parity**
     * **Property 3: Custom Factory Leading Views Override Visibility Settings**
     *
     * **Validates: Requirements 1.3, 4.2**
     */
    "Property 3: Factory-provided leading view should be rendered regardless of shouldShowDefaultAvatar" {
        checkAll(100, shouldShowDefaultAvatarArb) { shouldShowDefaultAvatar ->
            // When factory provides a non-null leading view (factoryProvidesLeadingView = true)
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = false,
                factoryProvidesLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            // Factory-provided leading view should ALWAYS be used, regardless of shouldShowDefaultAvatar
            result shouldBe LeadingViewResolution.FactoryLeadingView
        }
    }

    /**
     * Property test: When a factory provides a non-null leading view, the resolution
     * should return FactoryLeadingView even when shouldShowDefaultAvatar is false.
     *
     * This is a specific case of Property 3 that explicitly tests the scenario
     * where avatar visibility would normally be hidden (e.g., outgoing messages),
     * but the factory explicitly provides a leading view.
     *
     * **Validates: Requirements 1.3, 4.2**
     */
    "Property 3 (specific): Factory leading view with shouldShowDefaultAvatar=false should still use factory view" {
        val result = resolveLeadingViewWithFactory(
            hasCustomLeadingView = false,
            factoryProvidesLeadingView = true,
            shouldShowDefaultAvatar = false
        )

        result shouldBe LeadingViewResolution.FactoryLeadingView
    }

    /**
     * Property test: When a factory provides a non-null leading view, the resolution
     * should return FactoryLeadingView when shouldShowDefaultAvatar is true.
     *
     * This verifies that factory-provided views are used when visibility is enabled.
     *
     * **Validates: Requirements 1.3, 4.2**
     */
    "Property 3 (specific): Factory leading view with shouldShowDefaultAvatar=true should use factory view" {
        val result = resolveLeadingViewWithFactory(
            hasCustomLeadingView = false,
            factoryProvidesLeadingView = true,
            shouldShowDefaultAvatar = true
        )

        result shouldBe LeadingViewResolution.FactoryLeadingView
    }

    /**
     * Property test: For all combinations of shouldShowDefaultAvatar, when a factory
     * provides a non-null leading view, the result should always be FactoryLeadingView.
     *
     * This exhaustive test verifies both boolean values explicitly.
     *
     * **Validates: Requirements 1.3, 4.2**
     */
    "Property 3 (exhaustive): All shouldShowDefaultAvatar values with factory leading view should use factory view" {
        val shouldShowDefaultAvatarValues = listOf(true, false)

        shouldShowDefaultAvatarValues.forEach { shouldShowDefaultAvatar ->
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = false,
                factoryProvidesLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            result shouldBe LeadingViewResolution.FactoryLeadingView
        }
    }

    /**
     * Property test: Custom leadingView should still take precedence over factory
     * leading view, even when factory provides a non-null leading view.
     *
     * This verifies the priority order: custom leadingView > factory leading view.
     *
     * **Validates: Requirements 1.2** (custom view precedence over factory)
     */
    "Property 3: Custom leadingView takes precedence over factory leading view" {
        checkAll(100, factoryProvidesLeadingViewArb, shouldShowDefaultAvatarArb) { factoryProvidesLeadingView, shouldShowDefaultAvatar ->
            // When custom leadingView is provided, it should always take precedence
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = true,
                factoryProvidesLeadingView = factoryProvidesLeadingView,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            // Custom leadingView should ALWAYS be used, regardless of factory or shouldShowDefaultAvatar
            result shouldBe LeadingViewResolution.CustomLeadingView
        }
    }

    /**
     * Property test: When factory does NOT provide a leading view and shouldShowDefaultAvatar
     * is false, the resolution should return NoLeadingView.
     *
     * This verifies that when no custom view is provided and no factory view is available,
     * the shouldShowDefaultAvatar flag is respected.
     *
     * **Validates: Requirements 1.1** (default avatar visibility)
     */
    "Property 3: No factory leading view with shouldShowDefaultAvatar=false should return NoLeadingView" {
        val result = resolveLeadingViewWithFactory(
            hasCustomLeadingView = false,
            factoryProvidesLeadingView = false,
            shouldShowDefaultAvatar = false
        )

        result shouldBe LeadingViewResolution.NoLeadingView
    }

    /**
     * Property test: When factory does NOT provide a leading view and shouldShowDefaultAvatar
     * is true, the resolution should return FactoryLeadingView.
     *
     * This verifies that when no custom view is provided and no factory view is available,
     * but avatar visibility is enabled, the factory's default leading view is used.
     *
     * **Validates: Requirements 4.1** (factory integration)
     */
    "Property 3: No factory leading view with shouldShowDefaultAvatar=true should return FactoryLeadingView" {
        val result = resolveLeadingViewWithFactory(
            hasCustomLeadingView = false,
            factoryProvidesLeadingView = false,
            shouldShowDefaultAvatar = true
        )

        result shouldBe LeadingViewResolution.FactoryLeadingView
    }

    /**
     * Property test: The resolveLeadingViewWithFactory function should correctly handle
     * all eight combinations of hasCustomLeadingView, factoryProvidesLeadingView, and
     * shouldShowDefaultAvatar.
     *
     * This comprehensive test verifies the complete decision matrix for leading
     * view resolution with factory support.
     *
     * **Validates: Requirements 1.2, 1.3, 4.2** (complete resolution logic)
     */
    "resolveLeadingViewWithFactory should handle all combinations correctly" {
        checkAll(100, hasCustomLeadingViewArb, factoryProvidesLeadingViewArb, shouldShowDefaultAvatarArb) { 
            hasCustomLeadingView, factoryProvidesLeadingView, shouldShowDefaultAvatar ->
            
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = hasCustomLeadingView,
                factoryProvidesLeadingView = factoryProvidesLeadingView,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            val expected = when {
                // Priority 1: Custom leadingView always takes precedence
                hasCustomLeadingView -> LeadingViewResolution.CustomLeadingView
                // Priority 2: Factory-provided leading view overrides visibility settings
                factoryProvidesLeadingView -> LeadingViewResolution.FactoryLeadingView
                // Priority 3: Respect shouldShowDefaultAvatar for default behavior
                !shouldShowDefaultAvatar -> LeadingViewResolution.NoLeadingView
                // Priority 4: Use factory's default leading view
                else -> LeadingViewResolution.FactoryLeadingView
            }

            result shouldBe expected
        }
    }

    /**
     * Property test: Factory-provided leading view should override visibility settings
     * for any message alignment (LEFT, RIGHT, CENTER).
     *
     * This test combines the alignment generator with the factory leading view behavior
     * to verify that factory views work correctly regardless of message alignment.
     *
     * **Validates: Requirements 1.3, 4.2** (alignment independence)
     */
    "Property 3: Factory leading view should override visibility for any alignment" {
        checkAll(100, alignmentArb, shouldShowDefaultAvatarArb) { alignment, shouldShowDefaultAvatar ->
            // When factory provides a non-null leading view
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = false,
                factoryProvidesLeadingView = true,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            // Factory-provided leading view should ALWAYS be used, regardless of alignment or shouldShowDefaultAvatar
            result shouldBe LeadingViewResolution.FactoryLeadingView
        }
    }

    /**
     * Property test: Verifies the complete priority order of leading view resolution:
     * 1. Custom leadingView (highest priority)
     * 2. Factory-provided leading view
     * 3. shouldShowDefaultAvatar check
     * 4. Default factory leading view (lowest priority)
     *
     * This test exhaustively verifies all 8 combinations of the three boolean flags.
     *
     * **Validates: Requirements 1.2, 1.3, 4.2** (priority order verification)
     */
    "Property 3 (exhaustive): All boolean combinations should follow priority order" {
        // Test all 8 combinations explicitly
        val combinations = listOf(
            Triple(false, false, false), // No custom, no factory, no show -> NoLeadingView
            Triple(false, false, true),  // No custom, no factory, show -> FactoryLeadingView
            Triple(false, true, false),  // No custom, factory, no show -> FactoryLeadingView (factory overrides)
            Triple(false, true, true),   // No custom, factory, show -> FactoryLeadingView
            Triple(true, false, false),  // Custom, no factory, no show -> CustomLeadingView
            Triple(true, false, true),   // Custom, no factory, show -> CustomLeadingView
            Triple(true, true, false),   // Custom, factory, no show -> CustomLeadingView (custom takes precedence)
            Triple(true, true, true)     // Custom, factory, show -> CustomLeadingView
        )

        combinations.forEach { (hasCustomLeadingView, factoryProvidesLeadingView, shouldShowDefaultAvatar) ->
            val result = resolveLeadingViewWithFactory(
                hasCustomLeadingView = hasCustomLeadingView,
                factoryProvidesLeadingView = factoryProvidesLeadingView,
                shouldShowDefaultAvatar = shouldShowDefaultAvatar
            )

            val expected = when {
                hasCustomLeadingView -> LeadingViewResolution.CustomLeadingView
                factoryProvidesLeadingView -> LeadingViewResolution.FactoryLeadingView
                !shouldShowDefaultAvatar -> LeadingViewResolution.NoLeadingView
                else -> LeadingViewResolution.FactoryLeadingView
            }

            result shouldBe expected
        }
    }

    // ============================================================================
    // Property 4: Avatar Visibility Follows Conversation Type Rules
    // ============================================================================

    /**
     * Property test: For any incoming message (LEFT alignment) with hideAvatar=false:
     * - In a Group conversation (group != null), the avatar SHALL be shown
     * - In a User conversation (user != null, group == null), the avatar SHALL be hidden
     *
     * This property ensures that avatar visibility correctly follows conversation type rules
     * as defined in the design document:
     * - WHILE in a Group_Conversation, WHEN an incoming message is rendered, THE Message_Bubble SHALL show the sender avatar by default
     * - WHILE in a User_Conversation, WHEN an incoming message is rendered, THE Message_Bubble SHALL hide the sender avatar by default
     *
     * **Feature: message-list-avatar-parity**
     * **Property 4: Avatar Visibility Follows Conversation Type Rules**
     *
     * **Validates: Requirements 2.1, 2.2, 3.1, 3.2, 3.3**
     */
    "Property 4: LEFT alignment with hideAvatar=false should show avatar only in group conversations" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // For incoming messages (LEFT alignment) with hideAvatar=false:
            // - Group conversation (isGroupConversation=true) -> show avatar
            // - User conversation (isGroupConversation=false) -> hide avatar
            result shouldBe isGroupConversation
        }
    }

    /**
     * Property test: In a Group conversation (group != null), incoming messages (LEFT alignment)
     * with hideAvatar=false SHALL show the sender avatar.
     *
     * This is a specific case of Property 4 that explicitly tests the group conversation scenario.
     *
     * **Validates: Requirements 2.1, 3.2**
     */
    "Property 4 (specific): Group conversation with LEFT alignment and hideAvatar=false should show avatar" {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = false,
            isGroupConversation = true // Group conversation
        )

        // In group conversations, incoming messages should show avatar
        result shouldBe true
    }

    /**
     * Property test: In a User conversation (user != null, group == null), incoming messages
     * (LEFT alignment) with hideAvatar=false SHALL hide the sender avatar.
     *
     * This is a specific case of Property 4 that explicitly tests the user conversation scenario.
     *
     * **Validates: Requirements 2.2, 3.1**
     */
    "Property 4 (specific): User conversation with LEFT alignment and hideAvatar=false should hide avatar" {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = false,
            isGroupConversation = false // User conversation (1:1 chat)
        )

        // In user conversations, incoming messages should hide avatar
        result shouldBe false
    }

    /**
     * Property test: Conversation type detection should correctly determine avatar visibility.
     * This test simulates the conversation type detection logic:
     * - When group != null -> isGroupConversation = true -> show avatar for incoming
     * - When user != null && group == null -> isGroupConversation = false -> hide avatar for incoming
     *
     * **Validates: Requirements 3.1, 3.2, 3.3**
     */
    "Property 4: Conversation type detection determines avatar visibility for incoming messages" {
        // Simulate conversation type detection scenarios
        data class ConversationContext(
            val hasUser: Boolean,
            val hasGroup: Boolean,
            val expectedIsGroupConversation: Boolean
        )

        val scenarios = listOf(
            // User conversation: user != null, group == null
            ConversationContext(hasUser = true, hasGroup = false, expectedIsGroupConversation = false),
            // Group conversation: group != null (user may or may not be present)
            ConversationContext(hasUser = false, hasGroup = true, expectedIsGroupConversation = true),
            // Both provided: group takes precedence
            ConversationContext(hasUser = true, hasGroup = true, expectedIsGroupConversation = true),
            // Neither provided: defaults to user conversation behavior
            ConversationContext(hasUser = false, hasGroup = false, expectedIsGroupConversation = false)
        )

        scenarios.forEach { (hasUser, hasGroup, expectedIsGroupConversation) ->
            // Simulate conversation type detection: group takes precedence
            val isGroupConversation = hasGroup

            // Verify the detection matches expected
            isGroupConversation shouldBe expectedIsGroupConversation

            // Verify avatar visibility for incoming messages
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // Avatar should be shown only in group conversations
            result shouldBe expectedIsGroupConversation
        }
    }

    /**
     * Property test: For all combinations of conversation type, incoming messages (LEFT alignment)
     * with hideAvatar=false should follow the conversation type rules.
     *
     * This exhaustive test verifies both conversation types explicitly.
     *
     * **Validates: Requirements 2.1, 2.2, 3.1, 3.2, 3.3**
     */
    "Property 4 (exhaustive): All conversation types with LEFT alignment and hideAvatar=false" {
        val conversationTypes = listOf(
            Pair(true, true),   // Group conversation -> show avatar
            Pair(false, false)  // User conversation -> hide avatar
        )

        conversationTypes.forEach { (isGroupConversation, expectedShowAvatar) ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            result shouldBe expectedShowAvatar
        }
    }

    /**
     * Property test: Conversation type rules should NOT apply to outgoing messages (RIGHT alignment).
     * Even in group conversations, outgoing messages should never show avatar.
     *
     * This test verifies that conversation type rules only affect incoming messages.
     *
     * **Validates: Requirements 1.1** (by contrast with Property 4)
     */
    "Property 4 (contrast): Conversation type rules do not apply to outgoing messages" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // Outgoing messages should NEVER show avatar, regardless of conversation type
            result shouldBe false
        }
    }

    /**
     * Property test: Conversation type rules should NOT apply to action messages (CENTER alignment).
     * Even in group conversations, action messages should never show avatar.
     *
     * This test verifies that conversation type rules only affect incoming messages.
     *
     * **Validates: Requirements 1.1** (by contrast with Property 4)
     */
    "Property 4 (contrast): Conversation type rules do not apply to action messages" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // Action messages should NEVER show avatar, regardless of conversation type
            result shouldBe false
        }
    }

    /**
     * Property test: The complete decision table for conversation type avatar rules.
     * This test verifies all combinations of alignment and conversation type with hideAvatar=false.
     *
     * | Alignment | isGroupConversation | Show Avatar? |
     * |-----------|---------------------|--------------|
     * | LEFT      | true                | YES          |
     * | LEFT      | false               | NO           |
     * | RIGHT     | true                | NO           |
     * | RIGHT     | false               | NO           |
     * | CENTER    | true                | NO           |
     * | CENTER    | false               | NO           |
     *
     * **Validates: Requirements 2.1, 2.2, 3.1, 3.2, 3.3**
     */
    "Property 4 (decision table): Complete conversation type avatar visibility matrix" {
        checkAll(100, alignmentArb, isGroupConversationArb) { alignment, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // Avatar is shown ONLY when:
            // 1. alignment is LEFT (incoming message)
            // 2. isGroupConversation is true (group chat)
            val expected = alignment == UIKitConstants.MessageBubbleAlignment.LEFT && isGroupConversation

            result shouldBe expected
        }
    }

    /**
     * Property test: Verifies that the conversation type rules are correctly applied
     * when simulating real-world scenarios with User and Group objects.
     *
     * This test simulates the logic in CometChatMessageList that derives isGroupConversation:
     * - isGroupConversation = (group != null)
     *
     * **Validates: Requirements 3.1, 3.2, 3.3**
     */
    "Property 4: Simulated conversation type detection with User/Group presence" {
        // Simulate different conversation contexts
        data class SimulatedConversation(
            val description: String,
            val groupPresent: Boolean,  // Simulates group != null
            val userPresent: Boolean,   // Simulates user != null
            val expectedAvatarVisibility: Boolean
        )

        val conversations = listOf(
            SimulatedConversation(
                description = "Group conversation (group != null)",
                groupPresent = true,
                userPresent = false,
                expectedAvatarVisibility = true
            ),
            SimulatedConversation(
                description = "User conversation (user != null, group == null)",
                groupPresent = false,
                userPresent = true,
                expectedAvatarVisibility = false
            ),
            SimulatedConversation(
                description = "Both provided (group takes precedence)",
                groupPresent = true,
                userPresent = true,
                expectedAvatarVisibility = true
            ),
            SimulatedConversation(
                description = "Neither provided (defaults to user conversation)",
                groupPresent = false,
                userPresent = false,
                expectedAvatarVisibility = false
            )
        )

        conversations.forEach { conversation ->
            // Derive isGroupConversation as CometChatMessageList does: group != null
            val isGroupConversation = conversation.groupPresent

            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            result shouldBe conversation.expectedAvatarVisibility
        }
    }

    /**
     * Property test: Verifies that hideAvatar=true overrides conversation type rules.
     * Even in group conversations where avatar would normally be shown, hideAvatar=true
     * should hide the avatar.
     *
     * This test ensures Property 5 (hideAvatar precedence) works correctly with
     * conversation type rules.
     *
     * **Validates: Requirements 2.3** (hideAvatar precedence over conversation type)
     */
    "Property 4 (with hideAvatar): hideAvatar=true overrides conversation type rules" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should ALWAYS hide avatar, regardless of conversation type
            result shouldBe false
        }
    }

    // ============================================================================
    // Property 5: HideAvatar Takes Precedence
    // ============================================================================

    /**
     * Property test: For any message and any conversation type, when hideAvatar is true,
     * the avatar SHALL NOT be shown.
     *
     * This property ensures that hideAvatar=true acts as a master override that takes
     * precedence over all other avatar visibility settings, including:
     * - Message alignment (LEFT, RIGHT, CENTER)
     * - Conversation type (User, Group)
     *
     * **Feature: message-list-avatar-parity**
     * **Property 5: HideAvatar Takes Precedence**
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5: hideAvatar=true should hide avatar for any alignment and conversation type" {
        checkAll(100, alignmentArb, isGroupConversationArb) { alignment, isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true is the master override - avatar should NEVER be shown
            result shouldBe false
        }
    }

    /**
     * Property test: hideAvatar=true should hide avatar even in the most permissive
     * configuration (LEFT alignment + group conversation).
     *
     * This is a specific case of Property 5 that explicitly tests the scenario where
     * avatar would normally be shown (incoming message in group conversation), but
     * hideAvatar=true overrides this behavior.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (specific): hideAvatar=true with LEFT alignment and group conversation should hide avatar" {
        // Most permissive settings: LEFT alignment (incoming) + group conversation
        // This is the ONLY case where avatar would normally be shown
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = true,
            isGroupConversation = true
        )

        // hideAvatar=true should override and hide the avatar
        result shouldBe false
    }

    /**
     * Property test: hideAvatar=true should hide avatar for LEFT alignment in user conversations.
     *
     * This is a specific case of Property 5 that tests incoming messages in user conversations.
     * Avatar would already be hidden by default, but hideAvatar=true should still work.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (specific): hideAvatar=true with LEFT alignment and user conversation should hide avatar" {
        val result = shouldShowAvatar(
            alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
            hideAvatar = true,
            isGroupConversation = false
        )

        // hideAvatar=true should hide the avatar
        result shouldBe false
    }

    /**
     * Property test: hideAvatar=true should hide avatar for RIGHT alignment (outgoing messages).
     *
     * This is a specific case of Property 5 that tests outgoing messages.
     * Avatar would already be hidden by default for outgoing messages, but hideAvatar=true
     * should still work correctly.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (specific): hideAvatar=true with RIGHT alignment should hide avatar" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should hide the avatar for outgoing messages
            result shouldBe false
        }
    }

    /**
     * Property test: hideAvatar=true should hide avatar for CENTER alignment (action messages).
     *
     * This is a specific case of Property 5 that tests action/system messages.
     * Avatar would already be hidden by default for action messages, but hideAvatar=true
     * should still work correctly.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (specific): hideAvatar=true with CENTER alignment should hide avatar" {
        checkAll(100, isGroupConversationArb) { isGroupConversation ->
            val result = shouldShowAvatar(
                alignment = UIKitConstants.MessageBubbleAlignment.CENTER,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should hide the avatar for action messages
            result shouldBe false
        }
    }

    /**
     * Property test: For all 12 combinations in the decision table, when hideAvatar=true,
     * the avatar should NEVER be shown.
     *
     * This exhaustive test verifies all combinations of alignment and conversation type
     * with hideAvatar=true.
     *
     * | Alignment | hideAvatar | isGroupConversation | Show Avatar? |
     * |-----------|------------|---------------------|--------------|
     * | RIGHT     | true       | true                | NO           |
     * | RIGHT     | true       | false               | NO           |
     * | CENTER    | true       | true                | NO           |
     * | CENTER    | true       | false               | NO           |
     * | LEFT      | true       | true                | NO           |
     * | LEFT      | true       | false               | NO           |
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (exhaustive): All alignment and conversation type combinations with hideAvatar=true" {
        // Test all 6 combinations explicitly (3 alignments x 2 conversation types)
        val alignments = listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )
        val conversationTypes = listOf(true, false)

        alignments.forEach { alignment ->
            conversationTypes.forEach { isGroupConversation ->
                val result = shouldShowAvatar(
                    alignment = alignment,
                    hideAvatar = true,
                    isGroupConversation = isGroupConversation
                )

                // hideAvatar=true should ALWAYS hide avatar
                result shouldBe false
            }
        }
    }

    /**
     * Property test: Verifies that hideAvatar=true is the first check in the decision logic.
     *
     * The shouldShowAvatar function should check hideAvatar FIRST before checking
     * alignment or conversation type. This ensures consistent behavior regardless
     * of the order of other conditions.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5: hideAvatar check should be evaluated first in decision logic" {
        // Generate all possible combinations
        checkAll(100, alignmentArb, isGroupConversationArb) { alignment, isGroupConversation ->
            // With hideAvatar=true
            val resultWithHideAvatar = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // With hideAvatar=false (for comparison)
            val resultWithoutHideAvatar = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = false,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should ALWAYS return false
            resultWithHideAvatar shouldBe false

            // hideAvatar=false should follow normal rules
            val expectedWithoutHideAvatar = alignment == UIKitConstants.MessageBubbleAlignment.LEFT && isGroupConversation
            resultWithoutHideAvatar shouldBe expectedWithoutHideAvatar
        }
    }

    /**
     * Property test: Verifies the contrast between hideAvatar=true and hideAvatar=false
     * for the case where avatar would normally be shown (LEFT + group conversation).
     *
     * This test explicitly demonstrates the precedence behavior by comparing the two states.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (contrast): hideAvatar=true vs hideAvatar=false for permissive configuration" {
        // Configuration where avatar would normally be shown
        val alignment = UIKitConstants.MessageBubbleAlignment.LEFT
        val isGroupConversation = true

        // With hideAvatar=false, avatar should be shown
        val resultWithoutHideAvatar = shouldShowAvatar(
            alignment = alignment,
            hideAvatar = false,
            isGroupConversation = isGroupConversation
        )
        resultWithoutHideAvatar shouldBe true

        // With hideAvatar=true, avatar should be hidden (precedence)
        val resultWithHideAvatar = shouldShowAvatar(
            alignment = alignment,
            hideAvatar = true,
            isGroupConversation = isGroupConversation
        )
        resultWithHideAvatar shouldBe false
    }

    /**
     * Property test: Verifies that hideAvatar=true correctly overrides the default
     * avatar visibility for all message types in a group conversation.
     *
     * In a group conversation, incoming messages (LEFT) would normally show avatars.
     * This test verifies that hideAvatar=true overrides this for all message types.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5: hideAvatar=true overrides default visibility in group conversations" {
        // In group conversation
        val isGroupConversation = true

        // Test all alignments
        val alignments = listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )

        alignments.forEach { alignment ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should hide avatar for ALL alignments in group conversation
            result shouldBe false
        }
    }

    /**
     * Property test: Verifies that hideAvatar=true correctly overrides the default
     * avatar visibility for all message types in a user conversation.
     *
     * In a user conversation, avatars are already hidden by default for incoming messages.
     * This test verifies that hideAvatar=true maintains this behavior consistently.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5: hideAvatar=true maintains hidden state in user conversations" {
        // In user conversation
        val isGroupConversation = false

        // Test all alignments
        val alignments = listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )

        alignments.forEach { alignment ->
            val result = shouldShowAvatar(
                alignment = alignment,
                hideAvatar = true,
                isGroupConversation = isGroupConversation
            )

            // hideAvatar=true should hide avatar for ALL alignments in user conversation
            result shouldBe false
        }
    }

    /**
     * Property test: Verifies the complete decision table from the design document
     * with focus on hideAvatar=true rows.
     *
     * This test validates all 12 rows of the decision table, with special attention
     * to the 6 rows where hideAvatar=true.
     *
     * **Validates: Requirements 2.3**
     */
    "Property 5 (decision table): Complete hideAvatar precedence verification" {
        // Complete decision table from design document
        data class DecisionTableRow(
            val alignment: UIKitConstants.MessageBubbleAlignment,
            val hideAvatar: Boolean,
            val isGroupConversation: Boolean,
            val expectedShowAvatar: Boolean
        )

        val decisionTable = listOf(
            // hideAvatar=true rows (Property 5 focus)
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.RIGHT, true, true, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.RIGHT, true, false, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.CENTER, true, true, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.CENTER, true, false, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.LEFT, true, true, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.LEFT, true, false, false),
            // hideAvatar=false rows (for completeness)
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.RIGHT, false, true, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.RIGHT, false, false, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.CENTER, false, true, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.CENTER, false, false, false),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.LEFT, false, true, true),
            DecisionTableRow(UIKitConstants.MessageBubbleAlignment.LEFT, false, false, false)
        )

        decisionTable.forEach { row ->
            val result = shouldShowAvatar(
                alignment = row.alignment,
                hideAvatar = row.hideAvatar,
                isGroupConversation = row.isGroupConversation
            )

            result shouldBe row.expectedShowAvatar
        }
    }
})
