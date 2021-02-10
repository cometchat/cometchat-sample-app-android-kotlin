package com.cometchat.pro.uikit.ui_components.calls.call_manager.helper

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.io.IOException

/**
 * IncomingAudioHelper class is used to provide audio tone or ringtone when a there is a incoming call.
 * It provides various method which can set the ringtone or vibrate on incoming call
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */
class IncomingAudioHelper(context: Context) {
    private val context: Context
    private val vibrator: Vibrator
    private var player: MediaPlayer? = null


    companion object {
        private const val TAG = "IncomingAudioHelper"
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }

    init {
        this.context = context.applicationContext
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    /**
     * This method the start the ringtones which passed in parameter and also vibrate the phone on
     * incoming call is vibrate parameter passed is true.
     *
     * @param uri is object of Uri, It is a path of ringtone.
     * @param vibrate is a boolean which checks if the method needs to enable vibrate for incoming call or not.
     */
    fun start(uri: Uri?, vibrate: Boolean) {
        val audioManager = Utils.getAudioManager(context)
        if (player != null) player!!.release()
        if (uri != null) player = createPlayer(uri)
        val ringerMode = audioManager!!.ringerMode
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

    /**
     * This method stops the ringtone and cancelss vibration.
     */
    fun stop() {
        if (player != null) {
            Logger.error(TAG, "Stopping ringer")
            player!!.release()
            player = null
        }
        Logger.error(TAG, "Cancelling vibrator")
        vibrator.cancel()
    }

    /**
     * This method is used to check the vibration mode for different android versions.
     * @param context
     * @param player
     * @param ringerMode
     * @param vibrate
     * @return
     */
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
        val audioManager = Utils.getAudioManager(context)
        return vibrate && audioManager!!.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)
    }

    private fun createPlayer(ringtoneUri: Uri): MediaPlayer? {
        return try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setOnErrorListener(MediaPlayerErrorListener())
            mediaPlayer.setDataSource(context, ringtoneUri)
            mediaPlayer.isLooping = true
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)
            mediaPlayer
        } catch (e: IOException) {
            Logger.error(TAG, "Failed to create player for incoming call ringer")
            null
        }
    }

    private inner class MediaPlayerErrorListener : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            Logger.error(TAG, "onError($mp, $what, $extra")
            player = null
            return false
        }
    }
}