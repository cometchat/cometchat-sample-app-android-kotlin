package helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;

import com.cometchat.pro.uikit.R;

import utils.Utils;

/**
 * CometChatAudioHelper class is used to manage the audio tone and ringtone for incoming and outgoing
 * calls.
 *
 * Created at: 29th March 2020
 *
 * Modified at 29th March 2020
 */
public class CometChatAudioHelper {
    private static final String TAG = "CometChatAudioHelper";

    private Context context;

    private IncomingAudioHelper incomingAudioHelper;

    private OutgoingAudioHelper outgoingAudioHelper;

    private final SoundPool soundPool;

    private final int  disconnectedSoundId;

    public CometChatAudioHelper(Context context) {
        this.context = context;
        this.incomingAudioHelper=new IncomingAudioHelper(context);
        this.outgoingAudioHelper=new OutgoingAudioHelper(context);
        this.soundPool=new SoundPool(1,AudioManager.STREAM_VOICE_CALL,0);
        this.disconnectedSoundId=this.soundPool.load(context, R.raw.beep2,1);
    }

    public void initAudio(){
        AudioManager audioManager = Utils.getAudioManager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    public void startIncomingAudio(Uri ringtone, boolean isVibarte)
    {
        AudioManager audioManager = Utils.getAudioManager(context);
        boolean      speaker      = !audioManager.isWiredHeadsetOn() && !audioManager.isBluetoothScoOn();

        audioManager.setMode(AudioManager.MODE_RINGTONE);
        audioManager.setMicrophoneMute(false);
        audioManager.setSpeakerphoneOn(speaker);

        incomingAudioHelper.start(ringtone, isVibarte);
    }

    public void startOutgoingAudio(OutgoingAudioHelper.Type type) {
        AudioManager audioManager = Utils.getAudioManager(context);
        audioManager.setMicrophoneMute(false);

        if (type == OutgoingAudioHelper.Type.IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(false);
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        outgoingAudioHelper.start(type);
    }

    public void silenceIncomingRinger() {
        incomingAudioHelper.stop();
    }

    public void startCall(boolean preserveSpeakerphone) {
        AudioManager audioManager = Utils.getAudioManager(context);

        incomingAudioHelper.stop();
        outgoingAudioHelper.stop();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (!preserveSpeakerphone) {
            audioManager.setSpeakerphoneOn(false);
        }

    }

    public void stop(boolean playDisconnected) {
        AudioManager audioManager = Utils.getAudioManager(context);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMicrophoneMute(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.abandonAudioFocus(null);
        incomingAudioHelper.stop();
        outgoingAudioHelper.stop();

        if (playDisconnected) {
            soundPool.play(disconnectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }


    }

}
