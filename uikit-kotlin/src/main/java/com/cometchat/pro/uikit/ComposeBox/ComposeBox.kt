package com.cometchat.pro.uikit.ComposeBox

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.uikit.AudioVisualizer.AudioRecordView
import com.cometchat.pro.uikit.ComposeBox.CometChatEditText.OnEditTextMediaListener
import com.cometchat.pro.uikit.ComposeBox.ComposeBoxActionFragment.ComposeBoxActionListener
import com.cometchat.pro.uikit.R
import constant.StringContract
import listeners.ComposeActionListener
import utils.Utils
import java.io.File
import java.io.IOException
import java.util.*

class ComposeBox : RelativeLayout, View.OnClickListener {
    public var btnLiveReaction: ImageView? = null
    private var audioRecordView: AudioRecordView? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var timerRunnable: Runnable? = null
    private val seekHandler = Handler(Looper.getMainLooper())
    private var timer: Timer? = Timer()
    private var audioFileNameWithPath: String? = null
    private var isOpen = false
    private var isRecording = false
    private var isPlaying = false
    private var voiceMessage = false
    var ivAudio: ImageView? = null
    var ivCamera: ImageView? = null
    var ivGallery: ImageView? = null
    var ivFile: ImageView? = null
    var ivSend: ImageView? = null
    var ivArrow: ImageView? = null
    var ivMic: ImageView? = null
    var ivDelete: ImageView? = null
    private var voiceSeekbar: SeekBar? = null
    private var recordTime: Chronometer? = null
    var etComposeBox: CometChatEditText? = null
    private var composeBox: RelativeLayout? = null
    private var flBox: RelativeLayout? = null
    private var voiceMessageLayout: RelativeLayout? = null
    private var rlActionContainer: RelativeLayout? = null
    private val hasFocus = false
    private lateinit var composeActionListener: ComposeActionListener
    private var c: Context? = null
    private var color = 0
    private var composeBoxActionFragment: ComposeBoxActionFragment? = null
    private val bundle = Bundle()

    var isGalleryVisible = true
    var isAudioVisible = true
    var isCameraVisible = true
    var isFileVisible = true
    var isLocationVisible = true
    var isStickerVisible = true
    var isWhiteBoardVisible = true
    var isWriteBoardVisible = true
    var isStartVideoCall = true

    constructor(context: Context) : super(context) {
        initViewComponent(context, null, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewComponent(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewComponent(context, attrs, defStyleAttr, -1)
    }

    private fun initViewComponent(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        var view = View.inflate(context, R.layout.layout_compose_box, null)

        var a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.ComposeBox, 0, 0)
        color = a.getColor(R.styleable.ComposeBox_color, resources.getColor(R.color.colorPrimary))
        addView(view)

        this.c = context
        var viewGroup = view.parent as ViewGroup
        viewGroup.clipChildren = false
        mediaPlayer = MediaPlayer()
        var audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.isMusicActive) {
            audioManager.requestAudioFocus({ focusChange ->
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    stopRecording(true)
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        btnLiveReaction = findViewById(R.id.btn_live_reaction)
        composeBox = findViewById(R.id.message_box)
        flBox = findViewById(R.id.flBox)
        ivMic = findViewById(R.id.ivMic)
        ivDelete = findViewById(R.id.ivDelete)
        audioRecordView = findViewById(R.id.record_audio_visualizer)
        voiceMessageLayout = findViewById(R.id.voiceMessageLayout)
        recordTime = findViewById(R.id.record_time)
        voiceSeekbar = findViewById(R.id.voice_message_seekbar)
        ivCamera = findViewById(R.id.ivCamera)
        ivGallery = findViewById(R.id.ivImage)
        ivAudio = findViewById(R.id.ivAudio)
        ivFile = findViewById(R.id.ivFile)
        ivSend = findViewById(R.id.ivSend)
        ivArrow = findViewById(R.id.ivArrow)
        etComposeBox = findViewById(R.id.etComposeBox)
        rlActionContainer = findViewById(R.id.rlActionContainers)

        ivArrow!!.setImageTintList(ColorStateList.valueOf(color))
        ivCamera!!.setImageTintList(ColorStateList.valueOf(color))
        ivGallery!!.setImageTintList(ColorStateList.valueOf(color))
        ivFile!!.setImageTintList(ColorStateList.valueOf(color))
        ivSend!!.setImageTintList(ColorStateList.valueOf(color))

        ivAudio!!.setOnClickListener(this)
        ivArrow!!.setOnClickListener(this)
        ivSend!!.setOnClickListener(this)
        ivDelete!!.setOnClickListener(this)
        ivFile!!.setOnClickListener(this)
        ivMic!!.setOnClickListener(this)
        ivGallery!!.setOnClickListener(this)
        ivCamera!!.setOnClickListener(this)


//        etComposeBox.setOnFocusChangeListener((view1, b) -> {
//             this.hasFocus=b;
//            if (b){
//                rlActionContainer.setVisibility(GONE);
//                ivArrow.setVisibility(VISIBLE);
//            }else {
//                rlActionContainer.setVisibility(VISIBLE);
//                ivArrow.setVisibility(GONE);
//            }
//        });


        composeBoxActionFragment = ComposeBoxActionFragment()
        composeBoxActionFragment!!.setComposeBoxActionListener(object : ComposeBoxActionListener {
            override fun onGalleryClick() {
                composeActionListener.onGalleryActionClicked()
            }

            override fun onCameraClick() {
                composeActionListener.onCameraActionClicked()
            }

            override fun onFileClick() {
                composeActionListener.onFileActionClicked()
            }

            override fun onAudioClick() {
                composeActionListener.onAudioActionClicked()
            }

            override fun onLocationClick() {
                composeActionListener.onLocationActionClicked()
            }

            override fun onStickerClick() {
                composeActionListener.onStickerActionClicked()
            }

            override fun onWhiteBoardClick() {
                composeActionListener.onWhiteBoardClicked()
            }

            override fun onWriteBoardClick() {
                composeActionListener.onWriteBoardClicked()
            }

            override fun onStartCallClick() {
                composeActionListener.onStartCallClicked()
            }
        })

        etComposeBox!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (composeActionListener != null) {
                    composeActionListener.beforeTextChanged(charSequence, i, i1, i2)
                }
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (composeActionListener != null) {
                    composeActionListener.onTextChanged(charSequence, i, i1, i2)
                }
            }

            override fun afterTextChanged(editable: Editable) {
                if (composeActionListener != null) {
                    composeActionListener.afterTextChanged(editable)
                }
            }
        })

        etComposeBox!!.setMediaSelected(object : OnEditTextMediaListener {
            override fun OnMediaSelected(i: InputContentInfoCompat?) {
                composeActionListener.onEditTextMediaSelected(i)
            }
        })
        if (Utils.isDarkMode(context)) {
            composeBox!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            ivAudio!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            ivMic!!.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_white_24dp))
            flBox!!.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            etComposeBox!!.setTextColor(resources.getColor(R.color.textColorWhite))
            ivArrow!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            ivSend!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            ivCamera!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            ivGallery!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
            ivFile!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.textColorWhite)))
        } else {
            composeBox!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            ivAudio!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
            ivMic!!.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_grey_24dp))
            etComposeBox!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            ivSend!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
            flBox!!.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.grey)))
            ivArrow!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.grey)))
            ivCamera!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
            ivFile!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
            ivFile!!.setImageTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
        }
        a.recycle()
    }

    fun setText(text: String?) {
        etComposeBox!!.setText(text)
    }

    fun setColor(color: Int) {
        ivSend!!.imageTintList = ColorStateList.valueOf(color)
        ivCamera!!.imageTintList = ColorStateList.valueOf(color)
        ivGallery!!.imageTintList = ColorStateList.valueOf(color)
        ivFile!!.imageTintList = ColorStateList.valueOf(color)
        ivArrow!!.imageTintList = ColorStateList.valueOf(color)
    }

    fun setComposeBoxListener(composeActionListener: ComposeActionListener) {
        this.composeActionListener = composeActionListener
        this.composeActionListener.getCameraActionView(ivCamera!!)
        this.composeActionListener.getGalleryActionView(ivGallery!!)
        this.composeActionListener.getFileActionView(ivFile!!)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.ivDelete) {
            stopRecording(true)
            stopPlayingAudio()
            voiceMessageLayout!!.visibility = View.GONE
            etComposeBox!!.visibility = View.VISIBLE
            ivArrow!!.visibility = View.VISIBLE
            ivMic!!.visibility = View.VISIBLE
            ivMic!!.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_grey_24dp))
            isPlaying = false
            isRecording = false
            voiceMessage = false
            ivDelete!!.visibility = View.GONE
            ivSend!!.visibility = View.GONE
        }
        if (view.id == R.id.ivCamera) {
//            composeActionListener!!.onCameraActionClicked(ivCamera)
        }
        if (view.id == R.id.ivImage) {
//            composeActionListener!!.onGalleryActionClicked(ivGallery)
        }
        if (view.id == R.id.ivSend) {
            if (!voiceMessage) {
                composeActionListener.onSendActionClicked(etComposeBox)
            } else {
                composeActionListener.onVoiceNoteComplete(audioFileNameWithPath)
                audioFileNameWithPath = ""
                voiceMessageLayout!!.visibility = View.GONE
                etComposeBox!!.visibility = View.VISIBLE
                ivDelete!!.visibility = GONE
                ivSend!!.visibility = View.GONE
                ivArrow!!.visibility = View.VISIBLE
                ivMic!!.visibility = View.VISIBLE
                isRecording = false
                isPlaying = false
                voiceMessage = false
                ivMic!!.setImageResource(R.drawable.ic_mic_grey_24dp)
            }
        }
        if (view.id == R.id.ivAudio) {
//            composeActionListener!!.onAudioActionClicked(ivAudio)
        }
        if (view.id == R.id.ivFile) {
//            composeActionListener!!.onFileActionClicked(ivFile)
        }
        if (view.id == R.id.ivArrow) {
//            if (isOpen) {
//                ivArrow!!.rotation = 0f
//                isOpen = false
//                val leftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.animate_left_slide)
//                rlActionContainer!!.startAnimation(leftAnim)
//                rlActionContainer!!.visibility = View.GONE
//            } else {
//                ivArrow!!.rotation = 45f
//                isOpen = true
//                val rightAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.animate_right_slide)
//                rlActionContainer!!.startAnimation(rightAnimate)
//                rlActionContainer!!.visibility = View.VISIBLE
//            }

            var fm = (context as AppCompatActivity).supportFragmentManager
            bundle.putBoolean("isGalleryVisible", isGalleryVisible)
            bundle.putBoolean("isCameraVisible", isCameraVisible)
            bundle.putBoolean("isFileVisible", isFileVisible)
            bundle.putBoolean("isAudioVisible", isAudioVisible)
            bundle.putBoolean("isLocationVisible", isLocationVisible)
            bundle.putBoolean("isStartVideoCall", isStartVideoCall)
            if (CometChat.isExtensionEnabled("stickers"))
                bundle.putBoolean("isStickerVisible", isStickerVisible)
            if (CometChat.isExtensionEnabled("whiteboard"))
                bundle.putBoolean("isWhiteBoardVisible", isWhiteBoardVisible)
            if (CometChat.isExtensionEnabled("document"))
                bundle.putBoolean("isWriteBoardVisible", isWriteBoardVisible)
            composeBoxActionFragment!!.arguments = bundle
            composeBoxActionFragment!!.show(fm, composeBoxActionFragment!!.tag)
        }
        if (view.id == R.id.ivMic) {
            if (Utils.hasPermissions(context, *arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
//            if (isOpen) {
//                ivArrow!!.rotation = 0f
//                isOpen = false
//                val leftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.animate_left_slide)
//                rlActionContainer!!.startAnimation(leftAnim)
//                rlActionContainer!!.visibility = View.GONE
//            }
                if (!isRecording) {
                    startRecord()
                    ivMic!!.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_24dp))
                    isRecording = true
                    isPlaying = false
                } else {
                    if (isRecording && !isPlaying) {
                        isPlaying = true
                        stopRecording(false)
                        recordTime!!.stop()
                    }
                    ivMic!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_24dp))
                    audioRecordView!!.visibility = View.GONE
                    ivSend!!.visibility = View.VISIBLE
                    ivDelete!!.visibility = View.VISIBLE
                    voiceSeekbar!!.visibility = View.VISIBLE
                    voiceMessage = true
                    if (audioFileNameWithPath != null) startPlayingAudio(audioFileNameWithPath!!) else Toast.makeText(getContext(), "No File Found. Please", Toast.LENGTH_LONG).show()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    (context as Activity).requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            StringContract.RequestCode.RECORD)
                }
            }
        }
    }

    fun usedIn(className: String?) {
        bundle.putString("type", className)
    }

    fun startRecord() {
        etComposeBox!!.visibility = View.GONE
        recordTime!!.base = SystemClock.elapsedRealtime()
        recordTime!!.start()
        ivArrow!!.visibility = View.GONE
        voiceSeekbar!!.visibility = View.GONE
        voiceMessageLayout!!.visibility = View.VISIBLE
        audioRecordView!!.recreate()
        audioRecordView!!.visibility = View.VISIBLE
        startRecording()
    }


    private fun startPlayingAudio(path: String) {
        try {
            if (timerRunnable != null) {
                seekHandler.removeCallbacks(timerRunnable)
                timerRunnable = null
            }
            mediaPlayer!!.reset()
            if (Utils.hasPermissions(context, *arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                mediaPlayer!!.setDataSource(path)
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
            } else {
                (context as Activity?)!!.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        StringContract.RequestCode.READ_STORAGE)
            }
            val duration = mediaPlayer!!.duration
            voiceSeekbar!!.max = duration
            recordTime!!.base = SystemClock.elapsedRealtime()
            recordTime!!.start()
            timerRunnable = object : Runnable {
                override fun run() {
                    var pos = mediaPlayer!!.currentPosition
                    voiceSeekbar!!.progress = pos
                    if (mediaPlayer!!.isPlaying && pos < duration) {
//                        audioLength.setText(Utils.convertTimeStampToDurationTime(player.getCurrentPosition()));
                        seekHandler.postDelayed(this, 100)
                    } else {
                        seekHandler
                                .removeCallbacks(timerRunnable)
                        timerRunnable = null
                    }
                }
            }
            seekHandler.postDelayed(timerRunnable, 100)
            mediaPlayer!!.setOnCompletionListener { mp: MediaPlayer ->
                seekHandler
                        .removeCallbacks(timerRunnable)
                timerRunnable = null
                mp.stop()
                recordTime!!.stop()
                //                audioLength.setText(Utils.convertTimeStampToDurationTime(duration));
                voiceSeekbar!!.progress = 0
            }
        } catch (e: Exception) {
            Log.e("playAudioError: ", e.message)
            stopPlayingAudio()
        }
    }

    private fun stopPlayingAudio() {
        if (mediaPlayer != null) mediaPlayer!!.stop()
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            audioFileNameWithPath = Utils.getOutputMediaFile(getContext())
            mediaRecorder!!.setOutputFile(audioFileNameWithPath)
            try {
                mediaRecorder!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    var currentMaxAmp = 0
                    try {
                        currentMaxAmp = if (mediaRecorder != null) mediaRecorder!!.maxAmplitude else 0
                        audioRecordView!!.update(currentMaxAmp)
                        if (mediaRecorder == null) timer = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, 0, 100)
            mediaRecorder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(isCancel: Boolean) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()
                mediaRecorder!!.release()
                mediaRecorder = null
                if (isCancel) {
                    File(audioFileNameWithPath).delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}