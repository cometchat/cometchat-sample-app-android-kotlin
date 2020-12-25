package helper

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import com.cometchat.pro.uikit.R
import utils.Utils

/**
 * CometChatAudioHelper class is used to manage the audio tone and ringtone for incoming and outgoing
 * calls.
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */
class CometChatAudioHelper(private val context: Context) {

    private val incomingAudioHelper: IncomingAudioHelper
    private val outgoingAudioHelper: OutgoingAudioHelper
    private val soundPool: SoundPool
    private val disconnectedSoundId: Int

    companion object {
        private const val TAG = "CometChatAudioHelper"
    }

    init {
        incomingAudioHelper = IncomingAudioHelper(context)
        outgoingAudioHelper = OutgoingAudioHelper(context)
        soundPool = SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0)
        disconnectedSoundId = soundPool.load(context, R.raw.beep2, 1)
    }
    fun initAudio() {
        val audioManager = Utils.getAudioManager(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            audioManager!!.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        } else {
            audioManager!!.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    fun startIncomingAudio(ringtone: Uri?, isVibarte: Boolean) {
        val audioManager = Utils.getAudioManager(context)
        val speaker = !audioManager!!.isWiredHeadsetOn && !audioManager!!.isBluetoothScoOn
        audioManager.mode = AudioManager.MODE_RINGTONE
        audioManager.isMicrophoneMute = false
        audioManager.isSpeakerphoneOn = speaker
        incomingAudioHelper.start(ringtone, isVibarte)
    }

    fun startOutgoingAudio(type: OutgoingAudioHelper.Type) {
        val audioManager = Utils.getAudioManager(context)
        audioManager!!.isMicrophoneMute = false
        if (type == OutgoingAudioHelper.Type.IN_COMMUNICATION) {
            audioManager.isSpeakerphoneOn = false
        }
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        outgoingAudioHelper.start(type)
    }

    fun silenceIncomingRinger() {
        incomingAudioHelper.stop()
    }

    fun startCall(preserveSpeakerphone: Boolean) {
        val audioManager = Utils.getAudioManager(context)
        incomingAudioHelper.stop()
        outgoingAudioHelper.stop()
        audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
        if (!preserveSpeakerphone) {
            audioManager.isSpeakerphoneOn = false
        }
    }

    fun stop(playDisconnected: Boolean) {
        val audioManager = Utils.getAudioManager(context)
        audioManager!!.isSpeakerphoneOn = false
        audioManager.isMicrophoneMute = false
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.abandonAudioFocus(null)
        incomingAudioHelper.stop()
        outgoingAudioHelper.stop()
        if (playDisconnected) {
            soundPool.play(disconnectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }
    }
}