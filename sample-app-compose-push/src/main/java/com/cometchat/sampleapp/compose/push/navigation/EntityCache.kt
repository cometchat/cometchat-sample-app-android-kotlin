package com.cometchat.sampleapp.compose.push.navigation

import com.cometchat.calls.model.CallLog
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User

/**
 * Simple in-memory cache for entities that cannot be fetched by ID from the SDK.
 * 
 * This is used for Navigation Compose where we cannot pass complex objects directly
 * as route parameters (they must be serializable). Instead, we store the entity
 * in this cache before navigating and retrieve it at the destination.
 * 
 * Note: This cache is cleared when the app process is killed. For production apps,
 * consider using a more robust solution like a ViewModel with SavedStateHandle or
 * a persistent cache.
 */
object EntityCache {
    
    private val callLogCache = mutableMapOf<String, CallLog>()
    private val userCache = mutableMapOf<String, User>()
    private val groupCache = mutableMapOf<String, Group>()
    
    // ── CallLog ─────────────────────────────────────────────────────────

    fun putCallLog(sessionId: String, callLog: CallLog) {
        callLogCache[sessionId] = callLog
    }
    
    fun getCallLog(sessionId: String): CallLog? {
        return callLogCache[sessionId]
    }
    
    fun removeCallLog(sessionId: String) {
        callLogCache.remove(sessionId)
    }
    
    fun clearCallLogs() {
        callLogCache.clear()
    }

    // ── User ────────────────────────────────────────────────────────────

    fun putUser(uid: String, user: User) {
        userCache[uid] = user
    }

    fun getUser(uid: String): User? {
        return userCache[uid]
    }

    fun removeUser(uid: String) {
        userCache.remove(uid)
    }

    // ── Group ───────────────────────────────────────────────────────────

    fun putGroup(guid: String, group: Group) {
        groupCache[guid] = group
    }

    fun getGroup(guid: String): Group? {
        return groupCache[guid]
    }

    fun removeGroup(guid: String) {
        groupCache.remove(guid)
    }
}
