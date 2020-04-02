package helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.pro.helpers.Logger;

import java.io.IOException;

import utils.Utils;

/**
 * IncomingAudioHelper class is used to provide audio tone or ringtone when a there is a incoming call.
 * It provides various method which can set the ringtone or vibrate on incoming call
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */
public class IncomingAudioHelper {
    private static final String TAG = "IncomingAudioHelper";

    private static final long[] VIBRATE_PATTERN = {0, 1000,1000};

    private final Context context;
    private final Vibrator vibrator;

    private MediaPlayer player;

    IncomingAudioHelper(Context context) {
        this.context  = context.getApplicationContext();
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * This method the start the ringtones which passed in parameter and also vibrate the phone on
     * incoming call is vibrate parameter passed is true.
     *
     * @param uri is object of Uri, It is a path of ringtone.
     * @param vibrate is a boolean which checks if the method needs to enable vibrate for incoming call or not.
     */
    public void start(@Nullable Uri uri, boolean vibrate) {
        AudioManager audioManager = Utils.getAudioManager(context);

        if (player != null) player.release();
        if (uri != null)    player = createPlayer(uri);

        int ringerMode = audioManager.getRingerMode();

        if (shouldVibrate(context, player, ringerMode, vibrate)) {
            Logger.error(TAG, "Starting vibration");
            vibrator.vibrate(VIBRATE_PATTERN, 1);
        }

        if (player != null && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                if (!player.isPlaying()) {
                    player.prepare();
                    player.start();
                    Logger.error(TAG, "Playing ringtone now...");
                } else {
                    Logger.error(TAG, "Ringtone is already playing, declining to restart.");
                }
            } catch (IllegalStateException | IOException e) {
                Logger.error(TAG, e.getMessage());
                player = null;
            }
        } else {
            Logger.error(TAG, "Not ringing, mode: " + ringerMode);
        }
    }

    /**
     * This method stops the ringtone and cancelss vibration.
     */
    public void stop() {
        if (player != null) {
            Logger.error(TAG, "Stopping ringer");
            player.release();
            player = null;
        }

        Logger.error(TAG, "Cancelling vibrator");
        vibrator.cancel();
    }

    /**
     * This method is used to check the vibration mode for different android versions.
     * @param context
     * @param player
     * @param ringerMode
     * @param vibrate
     * @return
     */
    private boolean shouldVibrate(Context context, MediaPlayer player, int ringerMode, boolean vibrate) {
        if (player == null) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return shouldVibrateNew(context, ringerMode, vibrate);
        } else {
            return shouldVibrateOld(context, vibrate);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean shouldVibrateNew(Context context, int ringerMode, boolean vibrate) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null || !vibrator.hasVibrator()) {
            return false;
        }

        if (vibrate) {
            return ringerMode != AudioManager.RINGER_MODE_SILENT;
        } else {
            return ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        }
    }

    private boolean shouldVibrateOld(Context context, boolean vibrate) {
        AudioManager audioManager = Utils.getAudioManager(context);
        return vibrate && audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER);
    }

    private MediaPlayer createPlayer(@NonNull Uri ringtoneUri) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
            mediaPlayer.setDataSource(context, ringtoneUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);

            return mediaPlayer;
        } catch (IOException e) {
            Logger.error(TAG, "Failed to create player for incoming call ringer");
            return null;
        }
    }


    private class MediaPlayerErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Logger.error(TAG, "onError(" + mp + ", " + what + ", " + extra);
            player = null;
            return false;
        }
    }
}
