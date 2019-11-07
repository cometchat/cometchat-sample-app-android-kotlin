package com.inscripts.cometchatpulse.Helpers

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Utils.CommonUtil

class CometChatAudioHelper(private val context: Context) {

    private val incomingAudioHelper: IncomingAudioHelper

    private val outgoingAudioHelper: OutgoingAudioHelper

    private val soundPool: SoundPool

    private val disconnectedSoundId: Int

    init {
        this.incomingAudioHelper = IncomingAudioHelper(context)
        this.outgoingAudioHelper = OutgoingAudioHelper(context)
        this.soundPool = SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0)
        this.disconnectedSoundId = this.soundPool.load(context, R.raw.beep2, 1)
    }

    fun initAudio() {
        val audioManager = CommonUtil.getAudioManager(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    fun startIncomingAudio(ringtone: Uri, isVibarte: Boolean) {
        val audioManager = CommonUtil.getAudioManager(context)
        val speaker = !audioManager.isWiredHeadsetOn() && !audioManager.isBluetoothScoOn()

        audioManager.setMode(AudioManager.MODE_RINGTONE)
        audioManager.setMicrophoneMute(false)
        audioManager.setSpeakerphoneOn(speaker)

        incomingAudioHelper.start(ringtone, isVibarte)
    }

    fun startOutgoingAudio(type: OutgoingAudioHelper.Type) {
        val audioManager = CommonUtil.getAudioManager(context)
        audioManager.setMicrophoneMute(false)

        if (type === OutgoingAudioHelper.Type.IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(false)
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)

        outgoingAudioHelper.start(type)
    }

    fun silenceIncomingRinger() {
        incomingAudioHelper.stop()
    }

    fun startCall(preserveSpeakerphone: Boolean) {
        val audioManager = CommonUtil.getAudioManager(context)

        incomingAudioHelper.stop()
        outgoingAudioHelper.stop()

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)

        if (!preserveSpeakerphone) {
            audioManager.setSpeakerphoneOn(false)
        }

    }

    fun stop(playDisconnected: Boolean) {
        val audioManager = CommonUtil.getAudioManager(context)
        audioManager.setSpeakerphoneOn(false)
        audioManager.setMicrophoneMute(false)
        audioManager.setMode(AudioManager.MODE_NORMAL)
        audioManager.abandonAudioFocus(null)
        incomingAudioHelper.stop()
        outgoingAudioHelper.stop()

        if (playDisconnected) {
            soundPool.play(disconnectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }

        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false)
            audioManager.stopBluetoothSco()
        }


    }

    companion object {

        private val TAG = "CometChatAudioHelper"
    }


}
