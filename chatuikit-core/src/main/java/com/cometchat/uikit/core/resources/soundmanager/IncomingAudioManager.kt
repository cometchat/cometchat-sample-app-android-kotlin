package com.cometchat.uikit.core.resources.soundmanager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages incoming audio playback for calls.
 * Handles ringtone playback and vibration for incoming calls.
 *
 * @param context The application context.
 */
class IncomingAudioManager(private val context: Context) {
    
    companion object {
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isVibrating = false
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Starts playing the incoming audio.
     *
     * @param ringtoneUri The URI of the ringtone to play. If null, uses default ringtone.
     * @param vibrate Whether to vibrate along with the ringtone.
     */
    fun start(ringtoneUri: Uri?, vibrate: Boolean) {
        stop()
        
        val uri = ringtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // If custom ringtone fails, try default
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, defaultUri)
                    
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setAudioAttributes(audioAttributes)
                    
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                // Silently fail if even default ringtone fails
            }
        }
        
        if (vibrate) {
            startVibration()
        }
    }

    /**
     * Starts the vibration pattern.
     */
    private fun startVibration() {
        if (!isVibrating) {
            isVibrating = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(VIBRATE_PATTERN, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATE_PATTERN, 0)
            }
        }
    }

    /**
     * Stops the incoming audio and vibration.
     */
    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore errors during stop
        }
        mediaPlayer = null
        
        if (isVibrating) {
            vibrator.cancel()
            isVibrating = false
        }
    }
}
