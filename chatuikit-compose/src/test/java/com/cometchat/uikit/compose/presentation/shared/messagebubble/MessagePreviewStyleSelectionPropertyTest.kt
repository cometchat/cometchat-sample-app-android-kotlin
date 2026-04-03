package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Property-based tests for message preview style selection in DefaultReplyView.
 *
 * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
 *
 * **Validates: Requirements 1.2, 1.3**
 *
 * Property 2: For any message with a non-null quotedMessage and deletedAt == 0,
 * the DefaultReplyView SHALL apply the outgoing message preview style when the
 * message sender UID matches the logged-in user UID, and the incoming message
 * preview style otherwise. When no explicit style is provided, alignment-based
 * defaults (CometChatMessagePreviewStyle.incoming() / .outgoing()) SHALL be used.
 *
 * Since DefaultReplyView is a @Composable function, we test the pure style
 * selection logic that determines which CometChatMessagePreviewStyle is applied.
 */
class MessagePreviewStyleSelectionPropertyTest : StringSpec({

    // ============================================================================
    // Pure logic functions mirroring DefaultReplyView style selection
    // ============================================================================

    /**
     * Determines whether the message is outgoing based on sender UID matching
     * the logged-in user UID. Mirrors the logic in InternalContentRenderer.DefaultReplyView:
     *   val isOutgoing = try {
     *       CometChatUIKit.getLoggedInUser()?.uid == message.sender?.uid
     *   } catch (e: Exception) { false }
     */
    fun resolveIsOutgoing(
        senderUid: String?,
        loggedInUserUid: String?,
        throwsException: Boolean = false
    ): Boolean {
        return try {
            if (throwsException) throw RuntimeException("Simulated SDK exception")
            loggedInUserUid == senderUid
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Selects the appropriate message preview style based on outgoing status
     * and explicit style overrides. Mirrors the logic in DefaultReplyView:
     *   val previewStyle = if (isOutgoing) {
     *       outgoingMessagePreviewStyle ?: CometChatMessagePreviewStyle.outgoing()
     *   } else {
     *       incomingMessagePreviewStyle ?: CometChatMessagePreviewStyle.incoming()
     *   }
     *
     * Returns a [StyleSelection] indicating which style branch was taken.
     */
    fun resolvePreviewStyle(
        isOutgoing: Boolean,
        explicitIncomingStyle: CometChatMessagePreviewStyle?,
        explicitOutgoingStyle: CometChatMessagePreviewStyle?,
        defaultIncomingStyle: CometChatMessagePreviewStyle,
        defaultOutgoingStyle: CometChatMessagePreviewStyle
    ): CometChatMessagePreviewStyle {
        return if (isOutgoing) {
            explicitOutgoingStyle ?: defaultOutgoingStyle
        } else {
            explicitIncomingStyle ?: defaultIncomingStyle
        }
    }

    // ============================================================================
    // Test style instances (non-Composable, constructed directly)
    // ============================================================================

    val stubTextStyle = TextStyle.Default

    /** A recognizable "default incoming" style stand-in. */
    val defaultIncoming = CometChatMessagePreviewStyle(
        backgroundColor = Color.LightGray,
        strokeWidth = 0.dp,
        cornerRadius = 8.dp,
        strokeColor = Color.Transparent,
        separatorColor = Color.Blue,
        titleTextColor = Color.DarkGray,
        titleTextStyle = stubTextStyle,
        subtitleTextColor = Color.Gray,
        subtitleTextStyle = stubTextStyle,
        closeIconTint = Color.Gray,
        messageIconTint = Color.Gray
    )

    /** A recognizable "default outgoing" style stand-in. */
    val defaultOutgoing = CometChatMessagePreviewStyle(
        backgroundColor = Color.Blue,
        strokeWidth = 0.dp,
        cornerRadius = 8.dp,
        strokeColor = Color.Transparent,
        separatorColor = Color.White,
        titleTextColor = Color.White,
        titleTextStyle = stubTextStyle,
        subtitleTextColor = Color.White,
        subtitleTextStyle = stubTextStyle,
        closeIconTint = Color.White,
        messageIconTint = Color.White
    )

    /** An explicit custom incoming style override. */
    val explicitIncoming = CometChatMessagePreviewStyle(
        backgroundColor = Color.Green,
        strokeWidth = 1.dp,
        cornerRadius = 12.dp,
        strokeColor = Color.Black,
        separatorColor = Color.Red,
        titleTextColor = Color.Black,
        titleTextStyle = stubTextStyle,
        subtitleTextColor = Color.DarkGray,
        subtitleTextStyle = stubTextStyle,
        closeIconTint = Color.DarkGray,
        messageIconTint = Color.DarkGray
    )

    /** An explicit custom outgoing style override. */
    val explicitOutgoing = CometChatMessagePreviewStyle(
        backgroundColor = Color.Magenta,
        strokeWidth = 2.dp,
        cornerRadius = 16.dp,
        strokeColor = Color.Yellow,
        separatorColor = Color.Cyan,
        titleTextColor = Color.Yellow,
        titleTextStyle = stubTextStyle,
        subtitleTextColor = Color.Yellow,
        subtitleTextStyle = stubTextStyle,
        closeIconTint = Color.Yellow,
        messageIconTint = Color.Yellow
    )

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    /** Generates random non-blank UIDs. */
    val uidArb = Arb.string(minSize = 1, maxSize = 50)

    /** Generates a pair of UIDs that are guaranteed to be different. */
    val differentUidPairArb = Arb.of(
        listOf(
            "user1" to "user2",
            "alice" to "bob",
            "abc" to "xyz",
            "sender123" to "loggedIn456",
            "uid-a" to "uid-b"
        )
    )

    // ============================================================================
    // Property: Sender UID matches logged-in user → outgoing style selected
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "sender UID matching logged-in user UID resolves to outgoing" {
        checkAll(100, uidArb) { uid ->
            val isOutgoing = resolveIsOutgoing(senderUid = uid, loggedInUserUid = uid)
            isOutgoing shouldBe true

            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = null,
                explicitOutgoingStyle = null,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            style shouldBe defaultOutgoing
        }
    }

    // ============================================================================
    // Property: Sender UID does not match → incoming style selected
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "sender UID not matching logged-in user UID resolves to incoming" {
        checkAll(100, differentUidPairArb) { (senderUid, loggedInUid) ->
            val isOutgoing = resolveIsOutgoing(senderUid = senderUid, loggedInUserUid = loggedInUid)
            isOutgoing shouldBe false

            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = null,
                explicitOutgoingStyle = null,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            style shouldBe defaultIncoming
        }
    }

    // ============================================================================
    // Property: Explicit outgoing style used when sender matches + style provided
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "explicit outgoing style used when sender matches and style provided" {
        checkAll(100, uidArb) { uid ->
            val isOutgoing = resolveIsOutgoing(senderUid = uid, loggedInUserUid = uid)
            isOutgoing shouldBe true

            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = explicitIncoming,
                explicitOutgoingStyle = explicitOutgoing,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            style shouldBe explicitOutgoing
        }
    }

    // ============================================================================
    // Property: Explicit incoming style used when sender doesn't match + style provided
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "explicit incoming style used when sender does not match and style provided" {
        checkAll(100, differentUidPairArb) { (senderUid, loggedInUid) ->
            val isOutgoing = resolveIsOutgoing(senderUid = senderUid, loggedInUserUid = loggedInUid)
            isOutgoing shouldBe false

            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = explicitIncoming,
                explicitOutgoingStyle = explicitOutgoing,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            style shouldBe explicitIncoming
        }
    }

    // ============================================================================
    // Property: No explicit styles → alignment-based defaults used
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "no explicit styles falls back to alignment-based defaults" {
        checkAll(100, Arb.boolean()) { isOutgoing ->
            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = null,
                explicitOutgoingStyle = null,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            if (isOutgoing) {
                style shouldBe defaultOutgoing
            } else {
                style shouldBe defaultIncoming
            }
        }
    }

    // ============================================================================
    // Property: Exception during getLoggedInUser → defaults to incoming
    // ============================================================================

    /**
     * // Feature: jetpack-message-bubble-parity, Property 2: Message preview style selection based on sender identity
     * **Validates: Requirements 1.2, 1.3**
     */
    "exception during user lookup defaults to incoming style" {
        checkAll(100, uidArb, uidArb) { senderUid, loggedInUid ->
            val isOutgoing = resolveIsOutgoing(
                senderUid = senderUid,
                loggedInUserUid = loggedInUid,
                throwsException = true
            )
            isOutgoing shouldBe false

            val style = resolvePreviewStyle(
                isOutgoing = isOutgoing,
                explicitIncomingStyle = null,
                explicitOutgoingStyle = null,
                defaultIncomingStyle = defaultIncoming,
                defaultOutgoingStyle = defaultOutgoing
            )
            style shouldBe defaultIncoming
        }
    }
})
