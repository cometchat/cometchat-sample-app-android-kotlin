package com.cometchat.uikit.core.utils

import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Shared pin/save rules used by the message list, the thread header and the Pinned/Saved panels.
 *
 * Two problems every surface hit independently:
 *
 * 1. **Attribute loss on replace.** Only pin/save events carry an authoritative pin/save state. Any
 *    other payload that replaces a loaded message — an edit (REST response or realtime frame), a
 *    moderation verdict, a reaction update, a reconnect catch-up — describes the content, not the
 *    viewer's pin/save state: `pinnedAt` is often omitted and `savedAt` (per-viewer) almost always
 *    is. Swapping such a payload in wholesale made the 📌/🔖 indicator vanish until the next fetch.
 *    [carryPinSaveForward] keeps the loaded state on the incoming copy. Pin/save events must NOT go
 *    through it — there the incoming (possibly cleared) state is the truth.
 *
 * 2. **Limit errors rendered as "Something went wrong".** The limit-specific toast used to require
 *    the server to include `errorParams["limit"]`; when it didn't, the user saw a generic failure and
 *    reasonably assumed a transient error and retried. [classifyFailure] recognises the code on its
 *    own and resolves the cap the same way the React kit does — structured `errorParams`, then the
 *    error text ("limit of N"), then the app settings the SDK already caches — and only then gives
 *    up on the number (the kit still shows a limit-specific message).
 */
object PinSaveUtils {

    private const val TAG = "PinSaveUtils"

    /**
     * Returns [incoming] with the loaded pin/save state carried over when [incoming] lacks it.
     *
     * "Lacks" means `!isPinned()` / `!isSaved()`: an absent key parses to 0, and a non-pin/save
     * payload can't tell "omitted" from "cleared", so the loaded state wins (mirrors the React kit's
     * `carryPinSaveForward`). Returns [incoming] itself when nothing needs carrying; otherwise a
     * [BaseMessage.clone] of it, so the SDK-owned object handed to other listeners is not mutated.
     *
     * @param existing the copy currently held by the caller (may be `null` / a different message)
     * @param incoming the replacement payload
     */
    fun carryPinSaveForward(existing: BaseMessage?, incoming: BaseMessage): BaseMessage {
        if (existing == null || existing.id != incoming.id) return incoming
        val carryPin = existing.isPinned && !incoming.isPinned
        val carrySave = existing.isSaved && !incoming.isSaved
        if (!carryPin && !carrySave) return incoming

        val merged = incoming.clone()
        if (carryPin) {
            merged.pinnedAt = existing.pinnedAt
            merged.pinnedBy = existing.pinnedBy
        }
        if (carrySave) {
            merged.savedAt = existing.savedAt
        }
        return merged
    }

    /** Which cap a limit error refers to. */
    enum class LimitScope { PINNED_MESSAGES, SAVED_MESSAGES }

    /** A classified pin/save failure, ready to be turned into a toast by the kit. */
    sealed class Failure {
        /** The cap was hit. [limit] is the cap when it could be resolved, else `null`. */
        data class LimitReached(val scope: LimitScope, val limit: Int?) : Failure()

        /** RBAC/SBAC denial — the shared "you don't have permission" copy. */
        object PermissionDenied : Failure()

        /** Anything else; [code] is logged so QA builds reveal the real server code. */
        data class Other(val code: String?) : Failure()
    }

    /**
     * Classifies a failed pin/unpin/save/unsave call.
     *
     * The limit branches never require `errorParams["limit"]`: the cap is read from the error when
     * present, otherwise from the SDK's cached app settings, otherwise reported as `null`.
     */
    fun classifyFailure(e: CometChatException?): Failure = when (e?.code) {
        UIKitConstants.PinSaveErrorCodes.PINNED_MESSAGES_LIMIT_EXCEEDED ->
            Failure.LimitReached(
                LimitScope.PINNED_MESSAGES,
                limitFromError(e) ?: limitFromErrorText(e) ?: pinnedMessagesLimit()
            )

        UIKitConstants.PinSaveErrorCodes.SAVED_MESSAGES_LIMIT_EXCEEDED ->
            Failure.LimitReached(
                LimitScope.SAVED_MESSAGES,
                limitFromError(e) ?: limitFromErrorText(e) ?: savedMessagesLimit()
            )

        UIKitConstants.PinSaveErrorCodes.PERMISSION_DENIED -> Failure.PermissionDenied

        else -> {
            Log.w(TAG, "pin/save action failed: code=${e?.code} message=${e?.message} details=${e?.details}")
            Failure.Other(e?.code)
        }
    }

    /** The structured cap in `errorParams`, when the server included one. */
    fun limitFromError(e: CometChatException?): Int? =
        (e?.errorParams?.get(UIKitConstants.PinSaveErrorCodes.PARAM_LIMIT) as? Number)
            ?.toInt()
            ?.takeIf { it > 0 }

    /**
     * The cap as phrased in the error text — same heuristic as the React kit's
     * `readLimitFromErrorText`: "limit of N" is unambiguous; a bare trailing number is only
     * accepted when it looks like a cap (1..[MAX_PLAUSIBLE_LIMIT]), so a message id or
     * timestamp at the end of a sentence is never repeated back as the limit.
     */
    fun limitFromErrorText(e: CometChatException?): Int? {
        val texts = listOfNotNull(e?.message, e?.details).filter { it.isNotBlank() }
        for (text in texts) {
            PHRASED_LIMIT.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        }
        for (text in texts) {
            val value = TRAILING_NUMBER.find(text.trim())?.groupValues?.get(1)?.toIntOrNull() ?: continue
            if (value in 1..MAX_PLAUSIBLE_LIMIT) return value
        }
        return null
    }

    private val PHRASED_LIMIT = Regex("""limit of\s+(\d+)""", RegexOption.IGNORE_CASE)
    private val TRAILING_NUMBER = Regex("""(\d+)\s*\.?\s*$""")
    private const val MAX_PLAUSIBLE_LIMIT = 1000

    /** Max pinned messages per conversation from app settings, or `null` when not served. */
    fun pinnedMessagesLimit(): Int? =
        runCatching { CometChat.getPinMessageLimit() }.getOrNull()?.takeIf { it > 0 }

    /** Max saved messages per user from app settings, or `null` when not served. */
    fun savedMessagesLimit(): Int? =
        runCatching { CometChat.getSaveMessageLimit() }.getOrNull()?.takeIf { it > 0 }
}
