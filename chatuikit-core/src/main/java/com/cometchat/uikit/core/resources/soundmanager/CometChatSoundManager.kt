package com.cometchat.uikit.core.resources.soundmanager

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes
import com.cometchat.uikit.core.R

/**
 * The CometChatSoundManager class handles audio-related functionality in the
 * CometChat UIKit.
 *
 * It provides methods for playing different types of sounds and managing audio
 * settings for messages and calls.
 *
 * @param context The context of the application.
 */
class CometChatSoundManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CometChatSoundManager"
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
        private const val VIBRATE_DURATION = 200L
    }

    private val incomingAudioManager: IncomingAudioManager = IncomingAudioManager(context)
    private val outgoingAudioManager: OutgoingAudioManager = OutgoingAudioManager(context)
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    @Suppress("DEPRECATION")
    private val soundPool: SoundPool = SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0)
    private val disconnectedSoundId: Int = soundPool.load(context, R.raw.cometchat_beep2, 1)

    /**
     * Plays the specified sound using the default raw file.
     *
     * @param sound The sound type to be played.
     */
    fun play(sound: Sound) {
        when (sound) {
            Sound.INCOMING_CALL -> {
                val uri = Uri.parse("android.resource://${context.packageName}/${sound.getRawFile()}")
                startIncomingAudio(uri, vibrate = true)
            }
            Sound.OUTGOING_CALL -> {
                startOutgoingAudio(OutgoingAudioManager.Type.IN_COMMUNICATION, sound.getRawFile())
            }
            Sound.INCOMING_MESSAGE,
            Sound.OUTGOING_MESSAGE,
            Sound.INCOMING_MESSAGE_FROM_OTHER -> {
                playMessageSound(sound.getRawFile())
            }
        }
    }

    /**
     * Plays the specified sound with a custom raw file resource.
     * If the raw file is set to 0, it plays the default sound for the specified sound type.
     *
     * @param sound The sound type to be played.
     * @param rawFile The custom raw file resource ID. Pass 0 to use default.
     */
    fun play(sound: Sound, @RawRes rawFile: Int) {
        if (rawFile == 0) {
            play(sound)
            return
        }
        
        when (sound) {
            Sound.INCOMING_CALL -> {
                val uri = Uri.parse("android.resource://${context.packageName}/$rawFile")
                startIncomingAudio(uri, vibrate = true)
            }
            Sound.OUTGOING_CALL -> {
                startOutgoingAudio(OutgoingAudioManager.Type.IN_COMMUNICATION, rawFile)
            }
            Sound.INCOMING_MESSAGE,
            Sound.OUTGOING_MESSAGE,
            Sound.INCOMING_MESSAGE_FROM_OTHER -> {
                playMessageSound(rawFile)
            }
        }
    }

    /**
     * Starts playing incoming audio with optional vibration.
     *
     * @param ringtone The URI of the ringtone to play.
     * @param vibrate Whether to vibrate along with the ringtone.
     */
    private fun startIncomingAudio(ringtone: Uri, vibrate: Boolean) {
        val audioManager = getAudioManager()
        val speaker = !audioManager.isWiredHeadsetOn && !audioManager.isBluetoothScoOn

        audioManager.mode = AudioManager.MODE_RINGTONE
        audioManager.isMicrophoneMute = false
        audioManager.isSpeakerphoneOn = speaker

        incomingAudioManager.start(ringtone, vibrate)
    }

    /**
     * Starts playing outgoing audio.
     *
     * @param type The type of outgoing audio.
     * @param rawId The raw resource ID of the audio file.
     */
    private fun startOutgoingAudio(type: OutgoingAudioManager.Type, @RawRes rawId: Int) {
        val audioManager = getAudioManager()
        audioManager.isMicrophoneMute = false

        if (type == OutgoingAudioManager.Type.IN_COMMUNICATION) {
            audioManager.isSpeakerphoneOn = false
        }

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        outgoingAudioManager.start(type, rawId)
    }

    /**
     * Plays a message notification sound.
     * Vibrates if music is playing or device is in vibrate mode.
     *
     * @param ringId The raw resource ID of the sound to play.
     */
    private fun playMessageSound(@RawRes ringId: Int) {
        val audioManager = getAudioManager()
        
        val shouldVibrate = (audioManager.isMusicActive && 
            audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT) ||
            audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
            
        if (shouldVibrate) {
            vibrate()
        } else {
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT &&
                audioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                playMediaSound(ringId)
            }
        }
    }

    /**
     * Vibrates the device for a short duration.
     */
    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    /**
     * Plays a media sound using MediaPlayer.
     *
     * @param ringId The raw resource ID of the sound to play.
     */
    private fun playMediaSound(@RawRes ringId: Int) {
        try {
            val mediaPlayer = MediaPlayer.create(context, ringId)
            @Suppress("DEPRECATION")
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { mp ->
                mp?.stop()
                mp?.release()
            }
        } catch (e: Exception) {
            // Silently handle any playback errors
        }
    }

    /**
     * Pauses the audio playback and plays a disconnected sound.
     * Resets the audio settings to the normal state.
     */
    fun pause() {
        pauseSilently()
        soundPool.play(disconnectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    /**
     * Pauses the audio playback silently without playing any sound.
     * Resets all audio settings to normal state.
     */
    fun pauseSilently() {
        val audioManager = getAudioManager()
        audioManager.isSpeakerphoneOn = false
        audioManager.isMicrophoneMute = false
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.abandonAudioFocus(null)
        
        incomingAudioManager.stop()
        outgoingAudioManager.stop()
        
        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }
    }

    /**
     * Gets the AudioManager system service.
     *
     * @return The AudioManager instance.
     */
    private fun getAudioManager(): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Releases resources used by the sound manager.
     * Call this when the sound manager is no longer needed.
     */
    fun release() {
        soundPool.release()
        incomingAudioManager.stop()
        outgoingAudioManager.stop()
    }
}
