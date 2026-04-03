package com.cometchat.uikit.core.utils

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton manager for all audio bubble playback states.
 * Ensures only one audio plays at a time, state survives LazyColumn/RecyclerView recycling.
 */
object AudioBubbleStateManager {
    private const val TAG = "AudioBubbleStateMgr"
    private val states = ConcurrentHashMap<Int, AudioBubblePlaybackState>()

    fun getOrCreate(id: Int, audioUrl: String?, localPath: String?): AudioBubblePlaybackState {
        return states.getOrPut(id) { AudioBubblePlaybackState(id = id, audioUrl = audioUrl, localPath = localPath) }
    }

    fun pauseAllExcept(excludeId: Int) {
        states.values.toList().forEach { state ->
            if (state.id != excludeId && state.playState == PlayState.PLAYING) state.pause()
        }
    }

    fun clearAll() {
        Log.d(TAG, "Clearing all audio bubble states (${states.size} entries)")
        states.values.toList().forEach { it.release() }
        states.clear()
    }

    fun remove(id: Int) { states.remove(id)?.release() }
}
