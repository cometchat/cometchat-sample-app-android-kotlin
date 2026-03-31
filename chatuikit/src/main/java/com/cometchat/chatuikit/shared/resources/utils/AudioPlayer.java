package com.cometchat.chatuikit.shared.resources.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.cometchat.chatuikit.logger.CometChatLogger;

public class AudioPlayer {
    private static final String TAG = AudioPlayer.class.getSimpleName();

    private static AudioPlayer instance;
    private final MediaPlayer mediaPlayer;
    private boolean isPrepared;
    private boolean isPaused;
    private MediaPlayer.OnCompletionListener completionListener;
    private Runnable onPlaybackStartListener;
    private Context context;

    private AudioPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    public void setAudioUrl(String url, MediaPlayer.OnPreparedListener preparedListener, MediaPlayer.OnCompletionListener completionListener) {
        try {
            reset();
            this.completionListener = completionListener;
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                if (preparedListener != null) preparedListener.onPrepared(mediaPlayer);
            });
            mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                if (completionListener != null) completionListener.onCompletion(mediaPlayer);
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
    }

    public void reset() {
        mediaPlayer.reset();
        if (completionListener != null)
            completionListener.onCompletion(mediaPlayer);
        isPrepared = false;
        isPaused = false;
    }

    public void start() {
        if (!mediaPlayer.isPlaying() && isPrepared) {
            mediaPlayer.start();
            if (onPlaybackStartListener != null) {
                onPlaybackStartListener.run();
            }
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            isPrepared = false;
            isPaused = false;
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying() && isPrepared) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    public void resume() {
        if (isPaused && isPrepared) {
            mediaPlayer.start();
            isPaused = false;
            if (onPlaybackStartListener != null) {
                onPlaybackStartListener.run();
            }
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Sets a listener to be notified when playback starts.
     * Used by inline audio recorder to stop its own playback.
     */
    public void setOnPlaybackStartListener(Runnable listener) {
        this.onPlaybackStartListener = listener;
    }

    /**
     * Sets the context for MediaPlayer operations.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Plays audio from a local file path.
     * Used when the file is already downloaded (e.g., from local path in metadata).
     */
    public void playFromLocalFile(String filePath, MediaPlayer.OnPreparedListener preparedListener, MediaPlayer.OnCompletionListener completionListener) {
        try {
            reset();
            this.completionListener = completionListener;
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                if (preparedListener != null) preparedListener.onPrepared(mediaPlayer);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (completionListener != null) completionListener.onCompletion(mp);
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error playing from local file: " + e.toString());
        }
    }
}
