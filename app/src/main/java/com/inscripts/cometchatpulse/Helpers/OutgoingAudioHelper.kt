package com.inscripts.cometchatpulse.Helpers

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.R
import java.io.IOException

class OutgoingAudioHelper(private val context: Context) {

    private var type: Type? = null

    private val handler = android.os.Handler()

    private var mediaPlayer: MediaPlayer? = null

    enum class Type {
        IN_COMMUNICATION,
        RINGING
    }

    fun start(type: Type) {
        val soundId: Int
        this.type = type
        if (type == Type.IN_COMMUNICATION || type == Type.RINGING)
            soundId = R.raw.ring
        else
            throw IllegalArgumentException("Not a valid sound type")

        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
        mediaPlayer!!.isLooping = true
        val packageName = context.packageName
        val dataUri = Uri.parse("android.resource://$packageName/$soundId")

        try {
            mediaPlayer!!.setDataSource(context, dataUri)
            mediaPlayer!!.prepare()

            mediaPlayer!!.start()

        } catch (e: IllegalArgumentException) {
            Logger.error(TAG, e.message)
        } catch (e: SecurityException) {
            Logger.error(TAG, e.message)
        } catch (e: IllegalStateException) {
            Logger.error(TAG, e.message)
        } catch (e: IOException) {
            Logger.error(TAG, e.message)
        }

    }

    fun stop() {
        if (mediaPlayer == null) return
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    companion object {

        private val TAG = "OutgoingAudioHelper"
    }
}
