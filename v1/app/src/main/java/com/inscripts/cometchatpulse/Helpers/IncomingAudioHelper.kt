package com.inscripts.cometchatpulse.Helpers

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import com.cometchat.pro.helpers.Logger
import com.inscripts.cometchatpulse.Utils.CommonUtil
import java.io.IOException


class IncomingAudioHelper  constructor(context: Context) {

    private val context: Context
    private val vibrator: Vibrator

    private var player: MediaPlayer? = null

    init {
        this.context = context.applicationContext
        this.vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun start(uri: Uri?, vibrate: Boolean) {
        val audioManager = CommonUtil.getAudioManager(context)

        if (player != null) player!!.release()
        if (uri != null) player = createPlayer(uri)

        val ringerMode = audioManager.getRingerMode()

        if (shouldVibrate(context, player, ringerMode, vibrate)) {
            Logger.error(TAG, "Starting vibration")
            vibrator.vibrate(VIBRATE_PATTERN, 1)
        }

        if (player != null && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                if (!player!!.isPlaying) {
                    player!!.prepare()
                    player!!.start()
                    Logger.error(TAG, "Playing ringtone now...")
                } else {
                    Logger.error(TAG, "Ringtone is already playing, declining to restart.")
                }
            } catch (e: IllegalStateException) {
                Logger.error(TAG, e.message)
                player = null
            } catch (e: IOException) {
                Logger.error(TAG, e.message)
                player = null
            }

        } else {
            Logger.error(TAG, "Not ringing, mode: $ringerMode")
        }
    }

    fun stop() {
        if (player != null) {
            Logger.error(TAG, "Stopping ringer")
            player!!.release()
            player = null
        }

        Logger.error(TAG, "Cancelling vibrator")
        vibrator.cancel()
    }

    private fun shouldVibrate(context: Context, player: MediaPlayer?, ringerMode: Int, vibrate: Boolean): Boolean {
        if (player == null) {
            return true
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            shouldVibrateNew(context, ringerMode, vibrate)
        } else {
            shouldVibrateOld(context, vibrate)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun shouldVibrateNew(context: Context, ringerMode: Int, vibrate: Boolean): Boolean {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator == null || !vibrator.hasVibrator()) {
            return false
        }

        return if (vibrate) {
            ringerMode != AudioManager.RINGER_MODE_SILENT
        } else {
            ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }
    }

    private fun shouldVibrateOld(context: Context, vibrate: Boolean): Boolean {
        val audioManager = CommonUtil.getAudioManager(context)
        return vibrate && audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)
    }

    private fun createPlayer(ringtoneUri: Uri): MediaPlayer? {
        try {
            val mediaPlayer = MediaPlayer()

            mediaPlayer.setOnErrorListener(MediaPlayerErrorListener())
            mediaPlayer.setDataSource(context, ringtoneUri)
            mediaPlayer.isLooping = true
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)

            return mediaPlayer
        } catch (e: IOException) {
            Logger.error(TAG, "Failed to create player for incoming call ringer")
            return null
        }

    }


    private inner class MediaPlayerErrorListener : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            Logger.error(TAG, "onError($mp, $what, $extra")
            player = null
            return false
        }
    }

    companion object {

        private val TAG = "IncomingAudioHelper"

        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }

}
