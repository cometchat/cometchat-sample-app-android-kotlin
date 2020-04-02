package helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.cometchat.pro.helpers.Logger;
import com.cometchat.pro.uikit.R;

import java.io.IOException;

/**
 * OutgoingAudioHelper class is used to provide audio tone when a there is a outgoing call.
 * It provides various method which can set the ringtone or vibrate on outgoing call
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */

public class OutgoingAudioHelper {
    private static final String TAG = "OutgoingAudioHelper";

    private Type type;

    public enum Type {
        IN_COMMUNICATION,
        RINGING,
    }

    private final Context context;

    private android.os.Handler handler=new android.os.Handler();

    private MediaPlayer mediaPlayer;


    public OutgoingAudioHelper(@NonNull Context context) {
        this.context = context;

    }

    /**
     * This method is used to start the outgoing calltone.
     * @param type
     */
    public void start(final Type type) {
        int soundId;
        this.type=type;
        if (type == Type.IN_COMMUNICATION || type == Type.RINGING) soundId = R.raw.outgoing_call;
        else throw new IllegalArgumentException("Not a valid sound type");

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        mediaPlayer.setLooping(true);
        String packageName = context.getPackageName();
        Uri dataUri = Uri.parse("android.resource://" + packageName + "/" + soundId);

        try {
            mediaPlayer.setDataSource(context, dataUri);
            mediaPlayer.prepare();

            mediaPlayer.start();

        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    /**
     * This method is used to stop the outgoing calltone.
     */
    public void stop() {
        if (mediaPlayer == null) return;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
