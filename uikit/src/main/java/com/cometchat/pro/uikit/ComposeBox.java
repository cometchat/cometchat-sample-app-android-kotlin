package com.cometchat.pro.uikit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import constant.StringContract;
import listeners.ComposeActionListener;
import utils.AudioVisualizer.AudioRecordView;
import utils.KeyBoardUtils;
import utils.Utils;

public class ComposeBox extends RelativeLayout implements View.OnClickListener {

    private AudioRecordView audioRecordView;

    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    private Runnable timerRunnable;

    private Handler seekHandler = new Handler(Looper.getMainLooper());

    private Timer timer = new Timer();

    private String audioFileNameWithPath;

    private boolean isOpen,isRecording,isPlaying,voiceMessage;

    public ImageView ivAudio,ivCamera,ivGallery, ivFile,ivSend,ivArrow,ivMic,ivDelete;

    private SeekBar voiceSeekbar;

    private Chronometer recordTime;

    public EditText etComposeBox;

    private RelativeLayout composeBox;

    private RelativeLayout flBox;

    private RelativeLayout voiceMessageLayout;

    private RelativeLayout rlActionContainer;

    private boolean hasFocus;

    private ComposeActionListener composeActionListener;

    private Context context;

    private int color;

    public ComposeBox(Context context) {
        super(context);
        initViewComponent(context,null,-1,-1);
    }

    public ComposeBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewComponent(context,attrs,-1,-1);
    }

    public ComposeBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViewComponent(context,attrs,defStyleAttr,-1);
    }

    private void initViewComponent(Context context,AttributeSet attributeSet,int defStyleAttr,int defStyleRes){

        View view =View.inflate(context, R.layout.layout_compose_box,null);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.ComposeBox, 0, 0);
        color = a.getColor(R.styleable.ComposeBox_color,getResources().getColor(R.color.colorPrimary));
        addView(view);

        this.context=context;

        ViewGroup viewGroup=(ViewGroup)view.getParent();
        viewGroup.setClipChildren(false);

        mediaPlayer = new MediaPlayer();
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.isMusicActive())
        {
            audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopRecording(true);
                    }
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        composeBox=this.findViewById(R.id.message_box);
        flBox=this.findViewById(R.id.flBox);
        ivMic=this.findViewById(R.id.ivMic);
        ivDelete=this.findViewById(R.id.ivDelete);
        audioRecordView=this.findViewById(R.id.record_audio_visualizer);
        voiceMessageLayout=this.findViewById(R.id.voiceMessageLayout);
        recordTime=this.findViewById(R.id.record_time);
        voiceSeekbar=this.findViewById(R.id.voice_message_seekbar);
        ivCamera=this.findViewById(R.id.ivCamera);
        ivGallery=this.findViewById(R.id.ivImage);
        ivAudio = this.findViewById(R.id.ivAudio);
        ivFile =this.findViewById(R.id.ivFile);
        ivSend=this.findViewById(R.id.ivSend);
        ivArrow=this.findViewById(R.id.ivArrow);
        etComposeBox=this.findViewById(R.id.etComposeBox);
        rlActionContainer=this.findViewById(R.id.rlActionContainers);

        ivArrow.setImageTintList(ColorStateList.valueOf(color));
        ivCamera.setImageTintList(ColorStateList.valueOf(color));
        ivGallery.setImageTintList(ColorStateList.valueOf(color));

        ivFile.setImageTintList(ColorStateList.valueOf(color));
        ivSend.setImageTintList(ColorStateList.valueOf(color));

        ivAudio.setOnClickListener(this);
        ivArrow.setOnClickListener(this);
        ivSend.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivFile.setOnClickListener(this);
        ivMic.setOnClickListener(this);
        ivGallery.setOnClickListener(this);
        ivCamera.setOnClickListener(this);


        etComposeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (composeActionListener!=null){
                    composeActionListener.beforeTextChanged(charSequence,i,i1,i2);
                }

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (composeActionListener!=null){
                    composeActionListener.onTextChanged(charSequence,i,i1,i2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (composeActionListener!=null){
                    composeActionListener.afterTextChanged(editable);
                }
            }
        });

        if (Utils.isDarkMode(context)) {
            composeBox.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            ivAudio.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_white_24dp));
            flBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etComposeBox.setTextColor(getResources().getColor(R.color.textColorWhite));
            ivArrow.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivSend.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivCamera.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivGallery.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivFile.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
        } else {
            composeBox.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            ivAudio.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_grey_24dp));
            etComposeBox.setTextColor(getResources().getColor(R.color.primaryTextColor));
            ivSend.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            flBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
            ivArrow.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
            ivCamera.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            ivFile.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            ivFile.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
        a.recycle();
    }
    public void setText(String text)
    {
        etComposeBox.setText(text);
    }
    public void setColor(int color)
    {

        ivSend.setImageTintList(ColorStateList.valueOf(color));
        ivCamera.setImageTintList(ColorStateList.valueOf(color));
        ivGallery.setImageTintList(ColorStateList.valueOf(color));
        ivFile.setImageTintList(ColorStateList.valueOf(color));

        ivArrow.setImageTintList(ColorStateList.valueOf(color));
    }
    public void setComposeBoxListener(ComposeActionListener composeActionListener){
        this.composeActionListener=composeActionListener;

        this.composeActionListener.getCameraActionView(ivCamera);
        this.composeActionListener.getGalleryActionView(ivGallery);
        this.composeActionListener.getFileActionView(ivFile);

    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivDelete) {
            stopRecording(true);
            stopPlayingAudio();
            voiceMessageLayout.setVisibility(GONE);
            etComposeBox.setVisibility(View.VISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
            ivMic.setVisibility(View.VISIBLE);
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_grey_24dp));
            isPlaying = false;
            isRecording = false;
            voiceMessage = false;
            ivDelete.setVisibility(GONE);
            ivSend.setVisibility(View.GONE);
        }
        if (view.getId()==R.id.ivCamera){
           composeActionListener.onCameraActionClicked(ivCamera);
        }
        if (view.getId()==R.id.ivImage){
           composeActionListener.onGalleryActionClicked(ivGallery);
        }
        if (view.getId()==R.id.ivSend){
            if (!voiceMessage) {
                composeActionListener.onSendActionClicked(etComposeBox);
            } else {
                composeActionListener.onVoiceNoteComplete(audioFileNameWithPath);
                audioFileNameWithPath = "";
                voiceMessageLayout.setVisibility(GONE);
                etComposeBox.setVisibility(View.VISIBLE);
                ivSend.setVisibility(GONE);
                ivArrow.setVisibility(View.VISIBLE);
                ivMic.setVisibility(View.VISIBLE);
                isRecording = false;
                isPlaying = false;
                voiceMessage = false;
                ivMic.setImageResource(R.drawable.ic_mic_grey_24dp);
            }

        }
        if(view.getId()==R.id.ivAudio) {
            composeActionListener.onAudioActionClicked(ivAudio);
        }
        if (view.getId()==R.id.ivFile){
           composeActionListener.onFileActionClicked(ivFile);
        }
        if(view.getId()==R.id.ivArrow) {
            if (isOpen) {
               closeActionContainer();
            } else {
                openActionContainer();
            }
        }
        if (view.getId()==R.id.ivMic) {
            if (Utils.hasPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE})) {

                if (isOpen) {
                    closeActionContainer();
                }
                if (!isRecording) {
                    startRecord();
                    ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_24dp));
                    isRecording = true;
                    isPlaying = false;
                } else {
                    if (isRecording && !isPlaying) {
                        isPlaying = true;
                        stopRecording(false);
                        recordTime.stop();
                    }
                    ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                    audioRecordView.setVisibility(GONE);
                    ivSend.setVisibility(View.VISIBLE);
                    ivDelete.setVisibility(View.VISIBLE);
                    voiceSeekbar.setVisibility(View.VISIBLE);
                    voiceMessage = true;
                    if (audioFileNameWithPath != null)
                        startPlayingAudio(audioFileNameWithPath);
                    else
                        Toast.makeText(getContext(), "No File Found. Please", Toast.LENGTH_LONG).show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            StringContract.RequestCode.RECORD);
                }
            }
        }
    }

    public void openActionContainer() {
        ivArrow.setRotation(45f);
        isOpen = true;
        Animation rightAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.animate_right_slide);
        rlActionContainer.startAnimation(rightAnimate);
        rlActionContainer.setVisibility(View.VISIBLE);
    }

    public void closeActionContainer() {
        ivArrow.setRotation(0);
        isOpen = false;
        Animation leftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.animate_left_slide);
        rlActionContainer.startAnimation(leftAnim);
        rlActionContainer.setVisibility(GONE);
    }

    public void startRecord() {
            etComposeBox.setVisibility(GONE);
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            ivArrow.setVisibility(GONE);
            voiceSeekbar.setVisibility(GONE);
            voiceMessageLayout.setVisibility(View.VISIBLE);
            audioRecordView.recreate();
            audioRecordView.setVisibility(View.VISIBLE);
            startRecording();
    }

    private void startPlayingAudio(String path) {
        try {

            if (timerRunnable != null) {
                seekHandler.removeCallbacks(timerRunnable);
                timerRunnable = null;
            }

            mediaPlayer.reset();
            if (Utils.hasPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                ((Activity)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        StringContract.RequestCode.READ_STORAGE);
            }

            final int duration = mediaPlayer.getDuration();
            voiceSeekbar.setMax(duration);
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    int pos = mediaPlayer.getCurrentPosition();
                    voiceSeekbar.setProgress(pos);

                    if (mediaPlayer.isPlaying() && pos < duration) {
//                        audioLength.setText(Utils.convertTimeStampToDurationTime(player.getCurrentPosition()));
                        seekHandler.postDelayed(this, 100);
                    } else {
                        seekHandler
                                .removeCallbacks(timerRunnable);
                        timerRunnable = null;
                    }
                }

            };
            seekHandler.postDelayed(timerRunnable, 100);
            mediaPlayer.setOnCompletionListener(mp -> {
                seekHandler
                        .removeCallbacks(timerRunnable);
                timerRunnable = null;
                mp.stop();
                recordTime.stop();
//                audioLength.setText(Utils.convertTimeStampToDurationTime(duration));
                voiceSeekbar.setProgress(0);
//                playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            });

        } catch (Exception e) {
            Log.e( "playAudioError: ",e.getMessage());
            stopPlayingAudio();;
        }
    }


    private void stopPlayingAudio() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }
    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            audioFileNameWithPath = Utils.getOutputMediaFile(getContext());
            mediaRecorder.setOutputFile(audioFileNameWithPath);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int currentMaxAmp = 0;
                    try {
                        currentMaxAmp = mediaRecorder != null ? mediaRecorder.getMaxAmplitude() : 0;
                        audioRecordView.update(currentMaxAmp);
                        if (mediaRecorder==null)
                            timer = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }}, 0, 100);
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(boolean isCancel) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                if (isCancel) {
                    new File(audioFileNameWithPath).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
