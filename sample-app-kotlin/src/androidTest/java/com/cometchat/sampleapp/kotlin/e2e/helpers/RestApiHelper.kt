package com.cometchat.sampleapp.kotlin.e2e.helpers

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * REST API driver for single-emulator real-time E2E tests.
 *
 * The app under test stays logged in as the "primary" user (e.g. `dhruv`) with a live
 * WebSocket connection. This helper performs "other user" actions (send, react, delete,
 * mark-read, thread reply, …) over the CometChat REST API on behalf of a partner user
 * (e.g. `d1`). The server pushes the resulting event to the app's WebSocket, so the UI
 * updates in real time — giving true real-time verification on ONE emulator (no second
 * device, no login switching, no WebSocket disconnect).
 *
 * Auth model (verified against CometChat REST docs):
 *   - Header `apikey`     : a fullAccess REST API Key from the dashboard (NOT the app's
 *                           auth-only Auth Key — that one cannot perform REST writes).
 *   - Header `onBehalfOf` : the UID the action is performed as.
 *   No per-user auth tokens are needed.
 *
 * Base URL format (verified): https://{appId}.api-{region}.cometchat.io/v3
 *
 * The REST key can be overridden per-run:
 *   -Pandroid.testInstrumentationRunnerArguments.restApiKey=YOUR_REST_KEY
 */
object RestApiHelper {

    /** fullAccess REST API Key — single source of truth in [E2ETestConfig]. */
    val restApiKey: String get() = E2ETestConfig.REST_API_KEY

    private val JSON = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    // ─── URL building ────────────────────────────────────────────────────────────

    /** Builds a v3 URL, encoding each path segment (handles emoji reactions safely). */
    private fun url(vararg segments: String): String {
        val builder = "https://${E2ETestHelper.appId}.api-${E2ETestHelper.region}.cometchat.io/v3"
            .toHttpUrl().newBuilder()
        segments.forEach { builder.addPathSegment(it) }
        return builder.build().toString()
    }

    // ─── Low-level request execution ──────────────────────────────────────────────

    private fun newRequest(urlStr: String, onBehalfOf: String): Request.Builder =
        Request.Builder()
            .url(urlStr)
            .addHeader("apikey", restApiKey)
            .addHeader("onBehalfOf", onBehalfOf)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

    private fun execute(request: Request): JSONObject {
        var attempt = 0
        var netRetry = 0
        while (true) {
            val resp = try {
                client.newCall(request).execute()
            } catch (e: java.io.IOException) {
                // Transient network blip (e.g. SocketTimeoutException on a busy emulator) — retry.
                if (netRetry < 3) { netRetry++; Thread.sleep(2000); continue }
                throw e
            }
            val text = resp.body?.string().orEmpty()
            val code = resp.code
            resp.close()

            // Rate limited (1000 req/min per app): back off and retry, honoring the hint.
            if (code == 429 && attempt < 5) {
                attempt++
                val secs = Regex("retry after (\\d+)").find(text)
                    ?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 5L
                Thread.sleep((secs + 2L) * 1000L)
                continue
            }

            if (code !in 200..299) {
                throw RuntimeException("REST ${request.method} ${request.url} -> HTTP $code: $text")
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        }
    }

    private fun post(urlStr: String, onBehalfOf: String, body: JSONObject?): JSONObject {
        val reqBody = (body?.toString() ?: "{}").toRequestBody(JSON)
        return execute(newRequest(urlStr, onBehalfOf).post(reqBody).build())
    }

    private fun put(urlStr: String, onBehalfOf: String, body: JSONObject): JSONObject =
        execute(newRequest(urlStr, onBehalfOf).put(body.toString().toRequestBody(JSON)).build())

    private fun delete(urlStr: String, onBehalfOf: String): JSONObject =
        execute(newRequest(urlStr, onBehalfOf).delete().build())

    private fun deleteWithBody(urlStr: String, onBehalfOf: String, body: JSONObject): JSONObject =
        execute(newRequest(urlStr, onBehalfOf).delete(body.toString().toRequestBody(JSON)).build())

    private fun get(urlStr: String, onBehalfOf: String): JSONObject =
        execute(newRequest(urlStr, onBehalfOf).get().build())

    // ─── Messages ─────────────────────────────────────────────────────────────────

    /**
     * Sends a text message on behalf of [sender] to [receiver].
     * @return the new message id (as a String, e.g. "355796").
     */
    fun sendMessage(
        sender: String,
        receiver: String,
        text: String,
        receiverType: String = "user"
    ): String {
        val body = JSONObject().apply {
            put("receiver", receiver)
            put("receiverType", receiverType)
            put("category", "message")
            put("type", "text")
            put("data", JSONObject().put("text", text))
        }
        return post(url("messages"), sender, body).getJSONObject("data").get("id").toString()
    }

    /**
     * Sends a media message (image/file/audio/video) on behalf of [sender] using a hosted URL.
     * Avoids the flaky DocumentsUI/photo-picker flow entirely.
     * @return the new message id.
     */
    fun sendMediaMessage(
        sender: String,
        receiver: String,
        fileUrl: String,
        type: String = "image",
        name: String = "test_image.jpg",
        mimeType: String = "image/jpeg",
        receiverType: String = "user"
    ): String {
        val attachment = JSONObject().apply {
            put("url", fileUrl)
            put("name", name)
            put("mimeType", mimeType)
            put("extension", name.substringAfterLast('.', ""))
            put("size", "2048")
        }
        val body = JSONObject().apply {
            put("receiver", receiver)
            put("receiverType", receiverType)
            put("category", "message")
            put("type", type)
            put("data", JSONObject().put("attachments", JSONArray().put(attachment)))
        }
        return post(url("messages"), sender, body).getJSONObject("data").get("id").toString()
    }

    /**
     * Sends ONE media message carrying MULTIPLE attachments on behalf of [sender] — the
     * receive-side shape of the multi-attachment feature (ENG-36737). All attachments share
     * the message [type]/[mimeType]; the UIKit groups them into a single per-type bubble.
     * @return the new message id.
     */
    fun sendMultiAttachmentMessage(
        sender: String,
        receiver: String,
        fileUrl: String,
        names: List<String>,
        type: String = "file",
        mimeType: String = "application/pdf",
        receiverType: String = "user"
    ): String {
        val attachments = JSONArray()
        names.forEach { name ->
            attachments.put(JSONObject().apply {
                put("url", fileUrl)
                put("name", name)
                put("mimeType", mimeType)
                put("extension", name.substringAfterLast('.', ""))
                put("size", "2048")
            })
        }
        val body = JSONObject().apply {
            put("receiver", receiver)
            put("receiverType", receiverType)
            put("category", "message")
            put("type", type)
            put("data", JSONObject().put("attachments", attachments))
        }
        return post(url("messages"), sender, body).getJSONObject("data").get("id").toString()
    }

    /** Edits a text message on behalf of [sender]. */
    fun editMessage(sender: String, messageId: String, newText: String) {
        val body = JSONObject().put("data", JSONObject().put("text", newText))
        put(url("messages", messageId), sender, body)
    }

    /** Deletes a message on behalf of [sender]. */
    fun deleteMessage(sender: String, messageId: String) {
        delete(url("messages", messageId), sender)
    }

    /**
     * Fetches a single message on behalf of [sender]. Useful for verifying server-side
     * receipt state (deliveredAt / readAt), which is NOT exposed in the UI a11y tree.
     */
    fun getMessage(sender: String, messageId: String): JSONObject =
        get(url("messages", messageId), sender).getJSONObject("data")

    /** Returns true once [messageId] has a non-null deliveredAt (delivered receipt). */
    fun isDelivered(sender: String, messageId: String): Boolean =
        getMessage(sender, messageId).has("deliveredAt")

    /** Returns true once [messageId] has a non-null readAt (read receipt). */
    fun isRead(sender: String, messageId: String): Boolean =
        getMessage(sender, messageId).has("readAt")

    // ─── Reactions ──────────────────────────────────────────────────────────────

    /** Adds [reaction] (emoji) to [messageId] on behalf of [sender]. */
    fun addReaction(sender: String, messageId: String, reaction: String) {
        post(url("messages", messageId, "reactions", reaction), sender, null)
    }

    /** Removes [reaction] from [messageId] on behalf of [sender]. */
    fun removeReaction(sender: String, messageId: String, reaction: String) {
        delete(url("messages", messageId, "reactions", reaction), sender)
    }

    // ─── Block / unblock ──────────────────────────────────────────────────────────

    /** [blocker] blocks [blockedUid] (POST /users/{blocker}/blockedusers). */
    fun blockUser(blocker: String, blockedUid: String) {
        post(url("users", blocker, "blockedusers"), blocker, JSONObject().put("blockedUids", JSONArray().put(blockedUid)))
    }

    /** [blocker] unblocks [blockedUid] (DELETE /users/{blocker}/blockedusers with body). */
    fun unblockUser(blocker: String, blockedUid: String) {
        deleteWithBody(url("users", blocker, "blockedusers"), blocker, JSONObject().put("blockedUids", JSONArray().put(blockedUid)))
    }

    // ─── Group admin (ban / scope) ──────────────────────────────────────────────────

    /** Bans [uid] from group [guid] on behalf of [onBehalfOf] (POST members with usersToBan). */
    fun banGroupMember(onBehalfOf: String, guid: String, uid: String) {
        post(url("groups", guid, "members"), onBehalfOf, JSONObject().put("usersToBan", JSONArray().put(uid)))
    }

    /** Changes [uid]'s scope (admin/moderator/participant) in group [guid] on behalf of [onBehalfOf]. */
    fun changeMemberScope(onBehalfOf: String, guid: String, uid: String, scope: String) {
        put(url("groups", guid, "members", uid), onBehalfOf, JSONObject().put("scope", scope))
    }

    // ─── Threads ──────────────────────────────────────────────────────────────────

    /** Sends a threaded reply to [parentMessageId] on behalf of [sender]. @return reply id. */
    fun sendThreadReply(
        sender: String,
        parentMessageId: String,
        receiver: String,
        text: String,
        receiverType: String = "user"
    ): String {
        val body = JSONObject().apply {
            put("receiver", receiver)
            put("receiverType", receiverType)
            put("category", "message")
            put("type", "text")
            put("data", JSONObject().put("text", text))
        }
        return post(url("messages", parentMessageId, "thread"), sender, body)
            .getJSONObject("data").get("id").toString()
    }

    // ─── Receipts ──────────────────────────────────────────────────────────────────

    /**
     * Marks the conversation as read up to [messageId], on behalf of [onBehalfOf]
     * (the receiver of the messages), for the 1:1 conversation with [withUid] (the sender).
     * This produces a read receipt that the sender's app receives in real time.
     */
    fun markUserConversationRead(onBehalfOf: String, withUid: String, messageId: String) {
        val body = JSONObject().put("messageId", messageId.toLong())
        post(url("users", withUid, "conversation", "read"), onBehalfOf, body)
    }

    // ─── Users ──────────────────────────────────────────────────────────────────

    /** Returns the display name of [uid] (used to assert sender names in group chats). */
    fun getUserName(uid: String, onBehalfOf: String = uid): String =
        get(url("users", uid), onBehalfOf).getJSONObject("data").getString("name")

    // ─── Groups ─────────────────────────────────────────────────────────────────

    /**
     * Creates a group owned by [owner]. Throws if it already exists.
     * Pass [password] for a password-protected group (type = "password").
     */
    fun createGroup(
        owner: String,
        guid: String,
        name: String,
        type: String = "public",
        password: String? = null
    ) {
        val body = JSONObject().apply {
            put("guid", guid)
            put("name", name)
            put("type", type)
            put("owner", owner)
            if (password != null) put("password", password)
        }
        post(url("groups"), owner, body)
    }

    /** Creates the group if it does not already exist (swallows "already exists" errors). */
    fun createGroupIfAbsent(owner: String, guid: String, name: String, type: String = "public") {
        try {
            createGroup(owner, guid, name, type)
        } catch (_: Exception) { /* already exists or transient — ignore */ }
    }

    /**
     * Adds [uid] to [guid] as a participant on behalf of [onBehalfOf].
     * Use onBehalfOf = uid for a self-join (generates a "joined" action message);
     * use onBehalfOf = an admin to add someone.
     */
    fun addGroupMember(onBehalfOf: String, guid: String, uid: String) {
        val body = JSONObject().put("participants", JSONArray().put(uid))
        post(url("groups", guid, "members"), onBehalfOf, body)
    }

    /** Deletes [guid] on behalf of [onBehalfOf] (must be owner/admin). Throws on failure. */
    fun deleteGroup(guid: String, onBehalfOf: String) {
        delete(url("groups", guid), onBehalfOf)
    }

    /** Best-effort group delete for teardown — ignores errors. */
    fun deleteGroupQuietly(guid: String, onBehalfOf: String) {
        try {
            deleteGroup(guid, onBehalfOf)
        } catch (_: Exception) { /* cleanup — ignore */ }
    }

    /** Best-effort add — ignores "already a member" / transient errors. */
    fun ensureGroupMember(onBehalfOf: String, guid: String, uid: String) {
        try {
            addGroupMember(onBehalfOf, guid, uid)
        } catch (_: Exception) { /* already a member — ignore */ }
    }

    /**
     * Removes [uid] from [guid] on behalf of [onBehalfOf].
     * Use onBehalfOf = uid for a self-leave (generates a "left" action message);
     * use onBehalfOf = an admin to kick.
     */
    fun removeGroupMember(onBehalfOf: String, guid: String, uid: String) {
        delete(url("groups", guid, "members", uid), onBehalfOf)
    }

    /** Best-effort remove — ignores "not a member" / transient errors. */
    fun ensureNotGroupMember(onBehalfOf: String, guid: String, uid: String) {
        try {
            removeGroupMember(onBehalfOf, guid, uid)
        } catch (_: Exception) { /* not a member — ignore */ }
    }
}
