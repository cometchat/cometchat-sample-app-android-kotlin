package helper

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.uikit.R
import java.io.IOException

/**
 * OutgoingAudioHelper class is used to provide audio tone when a there is a outgoing call.
 * It provides various method which can set the ringtone or vibrate on outgoing call
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */
class OutgoingAudioHelper(private val context: Context) {
    private var type: Type? = null

    enum class Type {
        IN_COMMUNICATION, RINGING
    }

    private val handler = Handler()
    private var mediaPlayer: MediaPlayer? = null


    companion object {
        private const val TAG = "OutgoingAudioHelper"
    }

    /**
     * This method is used to start the outgoing calltone.
     * @param type
     */
    fun start(type: Type) {
        val soundId: Int
        this.type = type
        soundId = if (type == Type.IN_COMMUNICATION || type == Type.RINGING) R.raw.outgoing_call else throw IllegalArgumentException("Not a valid sound type")
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

    /**
     * This method is used to stop the outgoing calltone.
     */
    fun stop() {
        if (mediaPlayer == null) return
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
    }
}