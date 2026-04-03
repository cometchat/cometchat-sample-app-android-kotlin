package com.cometchat.uikit.core.resources.soundmanager

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes

/**
 * Manages outgoing audio playback for calls.
 * Handles ringback tone playback for outgoing calls.
 *
 * @param context The application context.
 */
class OutgoingAudioManager(private val context: Context) {
    
    /**
     * Type of outgoing audio.
     */
    enum class Type {
        /**
         * Audio for in-communication mode (during call).
         */
        IN_COMMUNICATION,
        
        /**
         * Audio for ringing mode (before call is answered).
         */
        RINGING
    }

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Starts playing the outgoing audio.
     *
     * @param type The type of outgoing audio.
     * @param rawId The raw resource ID of the audio file to play.
     */
    fun start(type: Type, @RawRes rawId: Int) {
        stop()
        
        try {
            mediaPlayer = MediaPlayer.create(context, rawId)?.apply {
                val usage = when (type) {
                    Type.IN_COMMUNICATION -> AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING
                    Type.RINGING -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
                }
                
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            // Silently handle playback errors
        }
    }

    /**
     * Stops the outgoing audio playback.
     */
    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore errors during stop
        }
        mediaPlayer = null
    }
}
