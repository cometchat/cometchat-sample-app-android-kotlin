package com.inscripts.cometchatpulse.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.util.LongSparseArray
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.ImageViewActivity
import com.inscripts.cometchatpulse.AsyncTask.DownloadFile
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.CustomView.CircleImageView
import com.inscripts.cometchatpulse.CustomView.StickyHeaderAdapter
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.DateUtil
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.ViewHolder.*
import com.inscripts.cometchatpulse.databinding.*
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.util.*

class OneToOneAdapter(val context: Context, val ownerId: String,
                      val listener: OnClickEvent) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        StickyHeaderAdapter<TextHeaderHolder> {

    private lateinit var viewHolder: RecyclerView.ViewHolder
    private var messagesList: LongSparseArray<BaseMessage> = LongSparseArray()
    private var currentPlayingSong: String? = null
    private var timerRunnable: Runnable? = null
    private val seekHandler = Handler()
    private var onClickEvent: OnClickEvent
    private var currentlyPlayingId = 0L
    var player: MediaPlayer? = null

    private var downloadFile: DownloadFile? = null

    init {
        if (player == null) {
            player = MediaPlayer()
        }
        onClickEvent = listener
        audioDurations = LongSparseArray()
        videoThumbnails = LongSparseArray()
    }

    companion object {
        private lateinit var audioDurations: LongSparseArray<Int>
        private lateinit var videoThumbnails: LongSparseArray<Bitmap>
    }



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        when (p1) {

            StringContract.ViewType.RIGHT_TEXT_MESSAGE -> {
                val binding: RightTextBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_text, p0, false)
                return RightTextMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_TEXT_MESSAGE -> {
                val binding: LeftTextBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_text, p0, false)
                return LeftTextMessageHolder(binding)
            }

            StringContract.ViewType.RIGHT_IMAGE_MESSAGE -> {
                val binding: CcImageVideoLayoutRightBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_right, p0, false)
                return RightImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_IMAGE_MESSAGE -> {
                val binding: CcImageVideoLayoutLeftBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_left, p0, false)
                return LeftImageVideoMessageHolder(binding)

            }

            StringContract.ViewType.RIGHT_VIDEO_MESSAGE -> {

                val binding: CcImageVideoLayoutRightBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_right, p0, false)
                return RightImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_VIDEO_MESSAGE -> {
                val binding: CcImageVideoLayoutLeftBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_left, p0, false)
                return LeftImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.RIGHT_AUDIO_MESSAGE -> {
                val binding: RightAudioBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_audio, p0, false)
                return RightAudioMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_AUDIO_MESSAGE -> {
                val binding:    LeftAudioBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_audio, p0, false)
                return LeftAudioMessageHolder(binding)
            }

            StringContract.ViewType.CALL_MESSAGE -> {

                val binding: ListHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_header, p0, false)
                return TextHeaderHolder(binding)
            }

            StringContract.ViewType.RIGHT_FILE_MESSAGE -> {
                val binding: RightFileBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_file, p0, false)
                return RightFileViewHolder(binding)
            }

            StringContract.ViewType.LEFT_FILE_MESSAGE -> {
                val binding: LeftFileBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_file, p0, false)
                return LeftFileViewHolder(binding)
            }

            StringContract.ViewType.RIGHT_LOCATION_MESSAGE -> {
                val binding: RightLocationBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_location, p0, false)
                return RightLocationViewHolder(binding)
            }

            StringContract.ViewType.LEFT_LOCATION_MESSAGE -> {
                val binding: LeftLocationBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_location, p0, false)
                return LeftLocationViewHolder(binding)
            }

            StringContract.ViewType.RIGHT_TEXT_REPLY_MESSAGE -> {

                val binding: RightReplyBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_reply, p0, false)
                return RightReplyMessageHolder(binding)
            }

            StringContract.ViewType.RIGHT_MEDIA_REPLY_MESSAGE -> {

                val binding: RightReplyBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_reply, p0, false)
                return RightReplyMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_TEXT_REPLY_MESSAGE -> {

                val binding: LeftReplyBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_reply, p0, false)
                return LeftReplyMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_MEDIA_REPLY_MESSAGE -> {

                val binding: LeftReplyBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_reply, p0, false)
                return LeftReplyMessageHolder(binding)
            }

            else -> {
                return viewHolder
            }
        }

    }

    override fun getHeaderId(var1: Int): Long {
        return java.lang.Long.parseLong(DateUtil.getDateId(messagesList.get(messagesList.keyAt(var1))?.getSentAt()!! * 1000))
    }

    override fun onCreateHeaderViewHolder(var1: ViewGroup): TextHeaderHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val binding: ListHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_header, var1, false)
        return TextHeaderHolder(binding)

    }

    override fun getItemId(position: Int): Long {

       val baseMessage=messagesList[messagesList.keyAt(position)]
         if (baseMessage!=null){
             return baseMessage.id.toLong()
         }else{
             return 0L
         }

    }

    override fun onBindHeaderViewHolder(var1: TextHeaderHolder, var2: Int, var3: Long) {
        try {
            val date = Date(messagesList[messagesList.keyAt(var2)]?.sentAt?.times(1000)!!)

            val formattedDate = DateUtil.getCustomizeDate(date.getTime())

            var1.binding.txtMessageDate.setBackground(context.resources.getDrawable(R.drawable.cc_rounded_date_button))

            var1.binding.txtMessageDate.setText(formattedDate)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

    }


    override fun getItemCount(): Int {

        return messagesList.size()
    }

    override fun getItemViewType(position: Int): Int {

        if (messagesList.get(messagesList.keyAt(position))?.category.equals(CometChatConstants.CATEGORY_MESSAGE, ignoreCase = true)) {

            if (ownerId.equals(messagesList.get(messagesList.keyAt(position))?.sender?.uid, ignoreCase = true)) {

                if ((messagesList.get(messagesList.keyAt(position))is TextMessage)
                        && (messagesList.get(messagesList.keyAt(position)) as TextMessage).metadata != null
                        && (messagesList.get(messagesList.keyAt(position)) as TextMessage).metadata.has("reply")) {

                    return StringContract.ViewType.RIGHT_TEXT_REPLY_MESSAGE

                } else if ((messagesList.get(messagesList.keyAt(position)) is MediaMessage)
                        && (messagesList.get(messagesList.keyAt(position)) as MediaMessage).metadata != null
                        && (messagesList.get(messagesList.keyAt(position)) as MediaMessage).metadata.has("reply")) {

                    return StringContract.ViewType.RIGHT_MEDIA_REPLY_MESSAGE

                } else {

                    when (messagesList.get(messagesList.keyAt(position))?.type) {

                        CometChatConstants.MESSAGE_TYPE_TEXT -> {

                            if ((messagesList.get(messagesList.keyAt(position)) as TextMessage).text!=null) {

                                if ((messagesList.get(messagesList.keyAt(position)) as TextMessage).text.equals("custom_location")) {
                                    return StringContract.ViewType.RIGHT_LOCATION_MESSAGE
                                }
                            }

                            return StringContract.ViewType.RIGHT_TEXT_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_IMAGE -> {

                            return StringContract.ViewType.RIGHT_IMAGE_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_AUDIO -> {

                            return StringContract.ViewType.RIGHT_AUDIO_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_VIDEO -> {

                            return StringContract.ViewType.RIGHT_VIDEO_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_FILE -> {

                            return StringContract.ViewType.RIGHT_FILE_MESSAGE
                        }

                    }
                }
            } else {


                if ((messagesList.get(messagesList.keyAt(position)) is TextMessage)
                        && (messagesList.get(messagesList.keyAt(position)) as TextMessage).metadata != null
                        && (messagesList.get(messagesList.keyAt(position)) as TextMessage).metadata.has("reply")) {

                    return StringContract.ViewType.LEFT_TEXT_REPLY_MESSAGE
                } else if ((messagesList.get(messagesList.keyAt(position)) is MediaMessage)
                        && (messagesList.get(messagesList.keyAt(position)) as MediaMessage).metadata != null
                        && (messagesList.get(messagesList.keyAt(position)) as MediaMessage).metadata.has("reply")) {

                    return StringContract.ViewType.LEFT_MEDIA_REPLY_MESSAGE
                } else {
                    when (messagesList.get(messagesList.keyAt(position))?.type) {

                        CometChatConstants.MESSAGE_TYPE_TEXT -> {

                               if ((messagesList.get(messagesList.keyAt(position)) as TextMessage).text!=null){

                                if ((messagesList.get(messagesList.keyAt(position)) as TextMessage).text.equals("custom_location")) {

                                    return StringContract.ViewType.LEFT_LOCATION_MESSAGE
                                }
                            }

                            return StringContract.ViewType.LEFT_TEXT_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_IMAGE -> {

                            return StringContract.ViewType.LEFT_IMAGE_MESSAGE

                        }

                        CometChatConstants.MESSAGE_TYPE_AUDIO -> {

                            return StringContract.ViewType.LEFT_AUDIO_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_VIDEO -> {

                            return StringContract.ViewType.LEFT_VIDEO_MESSAGE
                        }

                        CometChatConstants.MESSAGE_TYPE_FILE -> {

                            return StringContract.ViewType.LEFT_FILE_MESSAGE
                        }

                    }
                }
            }
        } else if (messagesList.get(messagesList.keyAt(position))?.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)) {
            return StringContract.ViewType.CALL_MESSAGE
        }

        return super.getItemViewType(position)
    }

    fun setMessageList(messageList: MutableList<BaseMessage>) {
        for(baseMessage:BaseMessage in messageList){
            this.messagesList.put(baseMessage.id.toLong(),baseMessage)
        }

        notifyDataSetChanged()
    }

    private fun setLongClick(view: View, baseMessage: BaseMessage) {
        var message: Any? = null
        if (baseMessage is TextMessage)
            message = baseMessage

        if (baseMessage is MediaMessage)
            message = baseMessage

        view.setOnLongClickListener {
            message?.let { it1 -> onClickEvent.onClickRl(view, it1) }
            true
        }
    }

    public fun stopPlayer() {
        try {
            if (player != null) {
                player?.stop()
                player = null
            }

        } catch (e: Exception) {

        }
    }


    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val baseMessage = messagesList.get(messagesList.keyAt(p0.adapterPosition))


        val timeStampLong = messagesList.get(messagesList.keyAt(p0.adapterPosition))?.sentAt
        var message: String? = null
        var filePath: String? = null
        var mediaFile: String? = null

        if (baseMessage is MediaMessage) {
            if (baseMessage.metadata != null) {
                try {
                    Log.d("metaAdapter", baseMessage.metadata.toString())

                    filePath = baseMessage.metadata.getString("path")
                    Log.d("OneToOneAdapter", filePath)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            mediaFile = baseMessage.url

        }

        if (baseMessage is Call) {
            val user = baseMessage.callInitiator as User
            val callReceiver = baseMessage.callReceiver as User

            if (user.uid.equals(ownerId, ignoreCase = true)) {
                message = "You Call " + callReceiver.name
            } else {
                message = "You received call from " + user.name
            }
        }

        when (p0.itemViewType) {

            StringContract.ViewType.RIGHT_TEXT_MESSAGE -> {
                val rightTextMessageHolder = p0 as RightTextMessageHolder
                rightTextMessageHolder.binding.message = (baseMessage as TextMessage)
                rightTextMessageHolder.binding.tvMessage.typeface = StringContract.Font.message
                rightTextMessageHolder.binding.timestamp.typeface = StringContract.Font.status
                rightTextMessageHolder.binding.tvMessage.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
                setLongClick(rightTextMessageHolder.binding.root, baseMessage)
                setStatusIcon(rightTextMessageHolder.binding.imgMessageStatus,baseMessage)

                if (baseMessage.deletedAt!=0L){
                    rightTextMessageHolder.binding.tvMessage.text="message deleted"
                    rightTextMessageHolder.binding.tvMessage.setTypeface(null,Typeface.ITALIC)
                    rightTextMessageHolder.binding.tvMessage.setTextColor(context.resources.getColor(R.color.deletedTextColor))
                }
                else {
                    rightTextMessageHolder.binding.tvMessage.text = baseMessage.text
                    rightTextMessageHolder.binding.tvMessage.setTextColor(StringContract.Color.white)
                    rightTextMessageHolder.binding.tvMessage.typeface = StringContract.Font.message
                }

            }

            StringContract.ViewType.LEFT_TEXT_MESSAGE -> {
                val leftTextMessageHolder = p0 as LeftTextMessageHolder
                leftTextMessageHolder.binding.message = (baseMessage as TextMessage)
                leftTextMessageHolder.binding.senderName.typeface = StringContract.Font.status
                leftTextMessageHolder.binding.timestamp.typeface = StringContract.Font.status
                leftTextMessageHolder.binding.tvMessage.background.setColorFilter(StringContract.Color.leftMessageColor,PorterDuff.Mode.SRC_ATOP)

                setLongClick(leftTextMessageHolder.binding.root, baseMessage)

                if (baseMessage.deletedAt!=0L){
                    leftTextMessageHolder.binding.tvMessage.text="message deleted"
                    leftTextMessageHolder.binding.tvMessage.setTextColor(context.resources.getColor(R.color.deletedTextColor))
                    leftTextMessageHolder.binding.tvMessage.setTypeface(null,Typeface.ITALIC)
                }
                else {
                    leftTextMessageHolder.binding.tvMessage.text = baseMessage.text
                    leftTextMessageHolder.binding.tvMessage.setTextColor(StringContract.Color.black)
                    leftTextMessageHolder.binding.tvMessage.typeface = StringContract.Font.message
                }

            }


            StringContract.ViewType.RIGHT_TEXT_REPLY_MESSAGE -> {
                val rightReplyMessageHolder = p0 as RightReplyMessageHolder
                rightReplyMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightReplyMessageHolder.binding.message = baseMessage
                rightReplyMessageHolder.binding.rlMain.background.setColorFilter(StringContract.Color.rightMessageColor,
                        PorterDuff.Mode.SRC_ATOP)
                baseMessage?.let { setStatusIcon(rightReplyMessageHolder.binding.messageStatus, it) }
                if (baseMessage is TextMessage) {
                    rightReplyMessageHolder.binding.txtNewmsg.visibility = View.VISIBLE
                    rightReplyMessageHolder.binding.txtNewmsg.text = baseMessage.text
                    rightReplyMessageHolder.binding.txtNewmsg.typeface = StringContract.Font.message
                    rightReplyMessageHolder.binding.tvNameReply.typeface = StringContract.Font.name
                    rightReplyMessageHolder.binding.ivNewMessage.visibility = View.GONE

                    if (baseMessage.metadata.has("senderUid")
                            && baseMessage.metadata.getString("senderUid").equals(ownerId)) {

                        rightReplyMessageHolder.binding.tvNameReply.text = context.getString(R.string.you)

                    } else {
                        rightReplyMessageHolder.binding.tvNameReply.text = baseMessage.metadata.getString("senderName")
                    }

                    when {
                        baseMessage.metadata.getString("type")
                                .equals(CometChatConstants.MESSAGE_TYPE_TEXT) -> {

                            rightReplyMessageHolder.binding.tvReplyTextMessage.text =
                                    baseMessage.metadata?.getString("text")
                            rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                        }
                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                                baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_VIDEO) -> {

                            rightReplyMessageHolder.binding.ivReplyImage.visibility = View.VISIBLE
                            Glide.with(context).load(baseMessage.metadata.getString("url"))
                                    .into(rightReplyMessageHolder.binding.ivReplyImage)

                            if (baseMessage.metadata.getString("type") == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                                rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.photo)
                            } else {
                                rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.video)
                            }

                        }
                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_AUDIO) -> {

                            rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.audio_message)

                            rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                        }
                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_FILE) -> {

                            rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.file_message)

                            rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                        }
                    }

                }

                baseMessage?.let { setLongClick(rightReplyMessageHolder.binding.root, it) }

            }

            StringContract.ViewType.RIGHT_MEDIA_REPLY_MESSAGE -> {

                val rightReplyMessageHolder = p0 as RightReplyMessageHolder
                rightReplyMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightReplyMessageHolder.binding.message = baseMessage
                rightReplyMessageHolder.binding.rlMain.background.setColorFilter(StringContract.Color.rightMessageColor,
                        PorterDuff.Mode.SRC_ATOP)

                baseMessage?.let { setStatusIcon(rightReplyMessageHolder.binding.messageStatus, it) }
                if (baseMessage is MediaMessage) {


                    if (baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_IMAGE)
                            || baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {

                        rightReplyMessageHolder.binding.ivNewMessage.visibility = View.VISIBLE

                        if (baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                            rightReplyMessageHolder.binding.ivVideoPlay.visibility=View.VISIBLE
                            rightReplyMessageHolder.binding.ivVideoPlay.setOnClickListener {
                               startIntent(baseMessage)
                            }

                        }

                        Glide.with(context).load(baseMessage.url)
                                .into(rightReplyMessageHolder.binding.ivNewMessage)
                    } else if (baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_FILE)) {

                        rightReplyMessageHolder.binding.fileContainer.visibility = View.VISIBLE

                        rightReplyMessageHolder.binding.fileName.text = FileUtil.getFileName(baseMessage.url)
                        rightReplyMessageHolder.binding.fileType.text = FileUtil.getFileExtension(baseMessage.url)

                        rightReplyMessageHolder.binding.fileName.setOnClickListener {

                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(baseMessage.url)))
                        }
                    } else if (baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {

                        rightReplyMessageHolder.binding.audioContainer.visibility = View.VISIBLE
                        rightReplyMessageHolder.binding.audioSeekBar.progress = 0
                        rightReplyMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                        rightReplyMessageHolder.binding.audioLength.typeface = StringContract.Font.status

                        try {

                            if (timeStampLong?.let { audioDurations.get(it) } == null) {
                                player?.reset()
                                try {
                                    player?.setDataSource(filePath)
                                    player?.prepare()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                                val duration = player?.duration
                                timeStampLong?.let { audioDurations.put(it, duration) }
                                rightReplyMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                                { DateUtil.convertTimeStampToDurationTime(it) })

                            } else {
                                val duration = timeStampLong.let { audioDurations.get(it) }
                                rightReplyMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                                { DateUtil.convertTimeStampToDurationTime(it) })

                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                        rightReplyMessageHolder.binding.playButton.setOnClickListener {

                            if (!TextUtils.isEmpty(filePath)) {

                                try {
                                    if (baseMessage.sentAt == currentlyPlayingId) {
                                        currentPlayingSong = ""

                                        try {
                                            if (player?.isPlaying()!!) {
                                                player?.pause()
                                                rightReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow_black)
                                            } else {

                                                player?.getCurrentPosition()?.let { player?.seekTo(it) }
                                                player?.getCurrentPosition()?.let { rightReplyMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                rightReplyMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                                player?.getDuration()?.let { rightReplyMessageHolder.binding.audioSeekBar.setMax(it) }
                                                rightReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                                timerRunnable = object : Runnable {
                                                    override fun run() {

                                                        val pos = player?.getCurrentPosition()
                                                        pos?.let { rightReplyMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                        if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                            rightReplyMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                            seekHandler.postDelayed(this, 250)
                                                        } else {
                                                            seekHandler
                                                                    .removeCallbacks(timerRunnable)
                                                            timerRunnable = null
                                                        }
                                                    }

                                                }
                                                seekHandler.postDelayed(timerRunnable, 100)
                                                notifyDataSetChanged()
                                                player!!.start()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    } else {
                                        rightReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_black_24dp)
                                        player?.let {
                                            timeStampLong?.let { it1 ->
                                                playAudio(filePath, it1, it, rightReplyMessageHolder.binding.playButton,
                                                        rightReplyMessageHolder.binding.audioLength, rightReplyMessageHolder.binding.audioSeekBar)
                                            }
                                        }
                                    }

                                    rightReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                        }
                    }

                    if (baseMessage.metadata.has("senderUid")
                            && baseMessage.metadata.getString("senderUid").equals(ownerId)) {

                        rightReplyMessageHolder.binding.tvNameReply.text = context.getString(R.string.you)
                    } else {
                        rightReplyMessageHolder.binding.tvNameReply.text = baseMessage.metadata.getString("senderName");
                    }

                    if (baseMessage.metadata.getString("type")
                                    .equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {

                        rightReplyMessageHolder.binding.tvReplyTextMessage.text =
                                baseMessage.metadata?.getString("text")

                        rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                            baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {

                        Glide.with(context).load(baseMessage.metadata.getString("url"))
                                .into(rightReplyMessageHolder.binding.ivReplyImage)

                        if (baseMessage.metadata.getString("type") == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                            rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.photo)
                        } else {
                            rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.video)
                        }

                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {

                        rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.audio_message)

                        rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_FILE)) {

                        rightReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.file_message)

                        rightReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                    }
                }

                baseMessage?.let { setLongClick(rightReplyMessageHolder.binding.root, it) }
            }

            StringContract.ViewType.LEFT_TEXT_REPLY_MESSAGE -> {

                val leftReplyMessageHolder = p0 as LeftReplyMessageHolder
                leftReplyMessageHolder.binding.message = baseMessage
                leftReplyMessageHolder.binding.rlMain.background.setColorFilter(StringContract.Color.leftMessageColor,
                        PorterDuff.Mode.SRC_ATOP)

                if (baseMessage is TextMessage) {

                    leftReplyMessageHolder.binding.txtNewmsg.visibility = View.VISIBLE
                    leftReplyMessageHolder.binding.txtNewmsg.text = baseMessage.text
                    leftReplyMessageHolder.binding.txtNewmsg.typeface = StringContract.Font.message
                    leftReplyMessageHolder.binding.ivNewMessage.visibility = View.GONE
                    leftReplyMessageHolder.binding.tvNameReply.visibility = View.VISIBLE

                    if (baseMessage.metadata.has("senderUid")
                            && baseMessage.metadata.getString("senderUid").equals(ownerId)) {

                        leftReplyMessageHolder.binding.tvNameReply.text = context.getString(R.string.you)
                    } else {
                        leftReplyMessageHolder.binding.tvNameReply.text = baseMessage.metadata.getString("senderName");
                    }

                    if (baseMessage.metadata.getString("type")
                                    .equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {

                        leftReplyMessageHolder.binding.tvReplyTextMessage.text = baseMessage.metadata?.getString("text")

                        leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                            baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                        Glide.with(context).load(baseMessage.metadata.getString("url"))
                                .into(leftReplyMessageHolder.binding.ivReplyImage)

                        if (baseMessage.metadata.getString("type") == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                            leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.photo)
                        } else {
                            leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.video)
                        }

                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {

                        leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.audio_message)

                        leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                    } else if (baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_FILE)) {

                        leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.file_message)

                        leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                    }

                }

                baseMessage?.let { setLongClick(leftReplyMessageHolder.binding.root, it) }
            }

            StringContract.ViewType.LEFT_MEDIA_REPLY_MESSAGE -> {

                val leftReplyMessageHolder = p0 as LeftReplyMessageHolder
                leftReplyMessageHolder.binding.message = baseMessage
                leftReplyMessageHolder.binding.rlMain.background.setColorFilter(StringContract.Color.leftMessageColor,
                        PorterDuff.Mode.SRC_ATOP)

                if (baseMessage is MediaMessage) {

                    when {
                        baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_IMAGE)
                                || baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_VIDEO) -> {

                            leftReplyMessageHolder.binding.ivNewMessage.visibility = View.VISIBLE

                            Glide.with(context).load(baseMessage.url)
                                    .into(leftReplyMessageHolder.binding.ivNewMessage)

                            if (baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                                leftReplyMessageHolder.binding.tvReplyTextMessage.visibility = View.VISIBLE;
                                leftReplyMessageHolder.binding.ivVideoPlay.setOnClickListener {
                                    startIntent(baseMessage)
                                }
                            }

                        }
                        baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_FILE) -> {

                            leftReplyMessageHolder.binding.fileContainer.visibility = View.VISIBLE
                            leftReplyMessageHolder.binding.fileName.text = FileUtil.getFileName(baseMessage.url)
                            leftReplyMessageHolder.binding.fileType.text = FileUtil.getFileExtension(baseMessage.url)
                            leftReplyMessageHolder.binding.fileName.setOnClickListener {

                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(baseMessage.url)))
                            }
                        }
                        baseMessage.type.equals(CometChatConstants.MESSAGE_TYPE_AUDIO) -> {

                            leftReplyMessageHolder.binding.audioContainer.visibility = View.VISIBLE
                            leftReplyMessageHolder.binding.audioSeekBar.progress = 0
                            leftReplyMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                            leftReplyMessageHolder.binding.audioLength.typeface = StringContract.Font.status

                            try {

                                if (timeStampLong?.let { audioDurations.get(it) } == null) {
                                    player?.reset()
                                    try {
                                        player?.setDataSource(baseMessage.url)
                                        player?.prepare()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    val duration = player?.duration
                                    timeStampLong?.let { audioDurations.put(it, duration) }
                                    leftReplyMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                                    { DateUtil.convertTimeStampToDurationTime(it) })

                                } else {
                                    val duration = timeStampLong?.let { audioDurations.get(it) }
                                    leftReplyMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                                    { DateUtil.convertTimeStampToDurationTime(it) })

                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            leftReplyMessageHolder.binding.playButton.setOnClickListener {

                                if (!TextUtils.isEmpty(baseMessage.url)) {

                                    try {
                                        if (baseMessage.sentAt == currentlyPlayingId) {
                                            currentPlayingSong = ""

                                            try {
                                                if (player?.isPlaying()!!) {
                                                    player?.pause()
                                                    leftReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow)
                                                } else {

                                                    player?.getCurrentPosition()?.let { player?.seekTo(it) }
                                                    player?.getCurrentPosition()?.let { leftReplyMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                    leftReplyMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                                    player?.getDuration()?.let { leftReplyMessageHolder.binding.audioSeekBar.setMax(it) }
                                                    leftReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                                    timerRunnable = object : Runnable {
                                                        override fun run() {

                                                            val pos = player?.getCurrentPosition()
                                                            pos?.let { leftReplyMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                            if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                                leftReplyMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                                seekHandler.postDelayed(this, 250)
                                                            } else {
                                                                seekHandler.removeCallbacks(timerRunnable)
                                                                timerRunnable = null
                                                            }
                                                        }

                                                    }
                                                    seekHandler.postDelayed(timerRunnable, 100)
                                                    notifyDataSetChanged()
                                                    player!!.start()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                        } else {
                                            leftReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                            player?.let {
                                                timeStampLong?.let { it1 ->
                                                    playAudio(baseMessage.url, it1, it, leftReplyMessageHolder.binding.playButton,
                                                            leftReplyMessageHolder.binding.audioLength,
                                                            leftReplyMessageHolder.binding.audioSeekBar)
                                                }
                                            }
                                        }

                                        leftReplyMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                            }
                        }
                    }

                    if (baseMessage.metadata.has("senderUid")
                            && baseMessage.metadata.getString("senderUid").equals(ownerId)) {

                        leftReplyMessageHolder.binding.tvNameReply.text = context.getString(R.string.you)
                    } else {
                        leftReplyMessageHolder.binding.tvNameReply.text = baseMessage.metadata.getString("senderName");
                    }

                    when {
                        baseMessage.metadata.getString("type")
                                .equals(CometChatConstants.MESSAGE_TYPE_TEXT) -> {

                            leftReplyMessageHolder.binding.tvReplyTextMessage.text =
                                    baseMessage.metadata?.getString("text")
                            leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                        }
                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                                baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_VIDEO) -> {


                            Glide.with(context).load(baseMessage.metadata.getString("url"))
                                    .into(leftReplyMessageHolder.binding.ivReplyImage)

                            if (baseMessage.metadata.getString("type") == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                                leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.photo)
                            } else {
                                leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.video)
                            }

                        }

                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_AUDIO) -> {

                            leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.audio_message)

                            leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE

                        }

                        baseMessage.metadata.getString("type").equals(CometChatConstants.MESSAGE_TYPE_FILE) -> {

                            leftReplyMessageHolder.binding.tvReplyTextMessage.text = context.getString(R.string.file_message)

                            leftReplyMessageHolder.binding.ivReplyImage.visibility = View.GONE
                        }
                    }
                }

                baseMessage?.let { setLongClick(leftReplyMessageHolder.binding.root, it) }
            }

            StringContract.ViewType.LEFT_IMAGE_MESSAGE -> {
                val leftImageVideoMessageHolder = p0 as LeftImageVideoMessageHolder
                leftImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftImageVideoMessageHolder.binding.senderName.typeface = StringContract.Font.status
                leftImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                leftImageVideoMessageHolder.binding.imageMessage.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        startIntent(baseMessage)
                    }

                })

                setLongClick(leftImageVideoMessageHolder.binding.imageMessage, baseMessage)
            }

            StringContract.ViewType.RIGHT_IMAGE_MESSAGE -> {
                val rightImageVideoMessageHolder = p0 as RightImageVideoMessageHolder
                rightImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightImageVideoMessageHolder.binding.imageMessage.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        startIntent(baseMessage)
                    }

                })
                setStatusIcon(rightImageVideoMessageHolder.binding.messageStatus, baseMessage)
                setLongClick(rightImageVideoMessageHolder.binding.imageMessage, baseMessage)

            }

            StringContract.ViewType.RIGHT_LOCATION_MESSAGE -> {
                val rightLocationViewHolder = p0 as RightLocationViewHolder
                rightLocationViewHolder.binding.message = baseMessage as TextMessage
                rightLocationViewHolder.binding.timestamp.typeface = StringContract.Font.status
                rightLocationViewHolder.bindView(p1, messagesList)
                setStatusIcon(rightLocationViewHolder.binding.imgMessageStatus, baseMessage)
            }

            StringContract.ViewType.LEFT_LOCATION_MESSAGE -> {
                val leftLocationViewHolder = p0 as LeftLocationViewHolder
                leftLocationViewHolder.binding.message = baseMessage as TextMessage
                leftLocationViewHolder.binding.timestamp.typeface = StringContract.Font.status
                leftLocationViewHolder.bindView(p1, messagesList)

            }

            StringContract.ViewType.RIGHT_FILE_MESSAGE -> {
                try {

                    val rightFileViewHolder = p0 as RightFileViewHolder
                    rightFileViewHolder.binding.message = baseMessage as MediaMessage
                    rightFileViewHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
                    rightFileViewHolder.binding.fileName.text = baseMessage.attachment.fileName
                    rightFileViewHolder.binding.fileType.text = baseMessage.attachment.fileExtension
                    val finalMediaFile = mediaFile
                    rightFileViewHolder.binding.fileName.typeface = StringContract.Font.name
                    rightFileViewHolder.binding.fileType.typeface = StringContract.Font.name
                    rightFileViewHolder.binding.timeStamp.typeface = StringContract.Font.status
                    rightFileViewHolder.binding.fileName.setOnClickListener(View.OnClickListener {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalMediaFile)))
                    })
                    setStatusIcon(rightFileViewHolder.binding.messageStatus, baseMessage)
                    setLongClick(rightFileViewHolder.binding.root, baseMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            StringContract.ViewType.LEFT_FILE_MESSAGE -> {

                try {
                    val leftFileViewHolder = p0 as LeftFileViewHolder
                    leftFileViewHolder.binding.message = baseMessage as MediaMessage
                    leftFileViewHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.leftMessageColor, PorterDuff.Mode.SRC_ATOP)
                    leftFileViewHolder.binding.fileName.text = baseMessage.attachment.fileName
                    leftFileViewHolder.binding.fileType.text = baseMessage.attachment.fileExtension
                    val finalMediaFile = mediaFile
                    leftFileViewHolder.binding.fileName.typeface = StringContract.Font.name
                    leftFileViewHolder.binding.fileType.typeface = StringContract.Font.name
                    leftFileViewHolder.binding.timeStamp.typeface = StringContract.Font.status
                    leftFileViewHolder.binding.senderName.typeface = StringContract.Font.status
                    leftFileViewHolder.binding.fileName.setOnClickListener(View.OnClickListener {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalMediaFile)))
                    })
                    setLongClick(leftFileViewHolder.binding.root, baseMessage)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            StringContract.ViewType.LEFT_VIDEO_MESSAGE -> {
                val leftImageVideoMessageHolder = p0 as LeftImageVideoMessageHolder
                leftImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status

                val requestOptions = RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_broken_image)
                Glide.with(context)
                        .load(baseMessage.url)
                        .apply(requestOptions)
                        .into(leftImageVideoMessageHolder.binding.imageMessage)

                leftImageVideoMessageHolder.binding.btnPlayVideo.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                     startIntent(baseMessage)
                    }

                })

                setLongClick(leftImageVideoMessageHolder.binding.root, baseMessage)
            }

            StringContract.ViewType.CALL_MESSAGE -> {
                val listHeaderItem = p0 as TextHeaderHolder
                listHeaderItem.binding.txtMessageDate.setTextColor(StringContract.Color.black)

                message = "Call " + (baseMessage as Call).callStatus

                listHeaderItem.binding.txtMessageDate.text = message

            }

            StringContract.ViewType.RIGHT_VIDEO_MESSAGE -> {
                val rightImageVideoMessageHolder = p0 as RightImageVideoMessageHolder
                rightImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status

                val requestOptions = RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_broken_image)
                Glide.with(context)
                        .load(baseMessage.url)
                        .apply(requestOptions)
                        .into(rightImageVideoMessageHolder.binding.imageMessage)

                rightImageVideoMessageHolder.binding.btnPlayVideo.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                      startIntent(baseMessage)
                    }

                })

                setStatusIcon(rightImageVideoMessageHolder.binding.messageStatus, baseMessage)
                setLongClick(rightImageVideoMessageHolder.binding.root, baseMessage)

            }

            StringContract.ViewType.RIGHT_AUDIO_MESSAGE -> {
                val rightAudioMessageHolder = p0 as RightAudioMessageHolder
                rightAudioMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightAudioMessageHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
                rightAudioMessageHolder.binding.audioSeekBar.progress = 0
                rightAudioMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightAudioMessageHolder.binding.audioLength.typeface = StringContract.Font.status
                setLongClick(rightAudioMessageHolder.binding.root, baseMessage)

                setStatusIcon(rightAudioMessageHolder.binding.messageStatus, baseMessage)

                try {

                    if (timeStampLong?.let { audioDurations.get(it) } == null) {
                        player?.reset()
                        try {
                            player?.setDataSource(filePath)
                            player?.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val duration = player?.duration
                        timeStampLong?.let { audioDurations.put(it, duration) }
                        rightAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })

                    } else {
                        val duration = timeStampLong?.let { audioDurations.get(it) }
                        rightAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                rightAudioMessageHolder.binding.playButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {

                        if (!TextUtils.isEmpty(filePath)) {

                            try {
                                if (baseMessage.sentAt == currentlyPlayingId) {
                                    currentPlayingSong = ""

                                    try {
                                        if (player?.isPlaying()!!) {
                                            player?.pause()
                                            rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow)
                                        } else {

                                            player?.currentPosition?.let { player?.seekTo(it) }
                                            player?.currentPosition?.let { rightAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                            rightAudioMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                            player?.getDuration()?.let { rightAudioMessageHolder.binding.audioSeekBar.setMax(it) }
                                            rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                            timerRunnable = object : Runnable {
                                                override fun run() {

                                                    val pos = player?.getCurrentPosition()
                                                    pos?.let { rightAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                    if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                        rightAudioMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                        seekHandler.postDelayed(this, 250)
                                                    } else {
                                                        seekHandler
                                                                .removeCallbacks(timerRunnable)
                                                        timerRunnable = null
                                                    }
                                                }

                                            }
                                            seekHandler.postDelayed(timerRunnable, 100)
                                            notifyDataSetChanged()
                                            player!!.start()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                } else {
                                    rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                    player?.let {
                                        timeStampLong?.let { it1 ->
                                            playAudio(filePath, it1, it, rightAudioMessageHolder.binding.playButton,
                                                    rightAudioMessageHolder.binding.audioLength, rightAudioMessageHolder.binding.audioSeekBar)
                                        }
                                    }
                                }

                                rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                })

                filePath?.let { Log.d("metaData", it) }

            }

            StringContract.ViewType.LEFT_AUDIO_MESSAGE -> {

                val leftAudioMessageHolder = p0 as LeftAudioMessageHolder
                leftAudioMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftAudioMessageHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.leftMessageColor, PorterDuff.Mode.SRC_ATOP)
                leftAudioMessageHolder.binding.audioSeekBar.progress = 0
                leftAudioMessageHolder.binding.audioLength.typeface = StringContract.Font.status
                leftAudioMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                setLongClick(leftAudioMessageHolder.itemView, baseMessage)
                leftAudioMessageHolder.binding.progress.visibility = View.GONE

                val audioPath: String = FileUtil.getPath(context, CometChatConstants.MESSAGE_TYPE_AUDIO) +
                        FileUtil.getFileName(baseMessage.url)

                val audioFile = File(audioPath)

                if (audioFile.exists()) {

                    leftAudioMessageHolder.binding.progress.visibility = View.GONE
                    leftAudioMessageHolder.binding.download.visibility = View.GONE
                    leftAudioMessageHolder.binding.playButton.visibility = View.VISIBLE

                    try {

                        if (timeStampLong?.let { audioDurations.get(it) } == null) {
                            player?.reset()
                            try {
                                player?.setDataSource(audioPath)
                                player?.prepare()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            val duration = player?.duration
                            timeStampLong?.let { audioDurations.put(it, duration) }
                            leftAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                            { DateUtil.convertTimeStampToDurationTime(it) })

                        } else {
                            val duration = timeStampLong?.let { audioDurations.get(it) }
                            leftAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                            { DateUtil.convertTimeStampToDurationTime(it) })

                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {

                    leftAudioMessageHolder.binding.progress.visibility = View.GONE
                    leftAudioMessageHolder.binding.download.visibility = View.VISIBLE
                    leftAudioMessageHolder.binding.playButton.visibility = View.GONE

                }

                leftAudioMessageHolder.binding.download.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {

                        if (downloadFile?.status == AsyncTask.Status.RUNNING) {

                            downloadFile?.cancel(true)
                            leftAudioMessageHolder.binding.download.setImageResource(R.drawable.ic_file_download)
                            leftAudioMessageHolder.binding.progress.visibility = View.GONE
                            leftAudioMessageHolder.binding.playButton.visibility = View.GONE
                        } else {

                            if (CCPermissionHelper.hasPermissions(CometChatPro.applicationContext(), CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)) {
                                if (FileUtil.checkDirExistence(context, CometChatConstants.MESSAGE_TYPE_AUDIO)) {

                                    downloadFile = DownloadFile(CometChatConstants.MESSAGE_TYPE_AUDIO, baseMessage.url,
                                            leftAudioMessageHolder.binding)

                                    downloadFile?.execute()

                                }
                            } else {
                                CCPermissionHelper.requestPermissions(context as Activity,
                                        arrayOf(CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE), StringContract.RequestCode.FILE_WRITE)
                            }
                        }
                    }
                })


                leftAudioMessageHolder.binding.playButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {

                        if (!TextUtils.isEmpty(baseMessage.url)) {

                            try {
                                if (baseMessage.sentAt == currentlyPlayingId) {
                                    currentPlayingSong = ""

                                    try {
                                        if (player?.isPlaying!!) {
                                            player?.pause()
                                            leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow_black)
                                        } else {
                                            //                                                player.setDataSource(message);
                                            //                                                player.prepare();
                                            player?.currentPosition?.let { player?.seekTo(it) }
                                            player?.currentPosition?.let { leftAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                            leftAudioMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                            player?.getDuration()?.let { leftAudioMessageHolder.binding.audioSeekBar.setMax(it) }
                                            leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_black_24dp)
                                            timerRunnable = object : Runnable {
                                                override fun run() {

                                                    val pos = player?.currentPosition
                                                    pos?.let { leftAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                    if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                        leftAudioMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                        seekHandler.postDelayed(this, 250)
                                                    } else {
                                                        seekHandler
                                                                .removeCallbacks(timerRunnable)
                                                        timerRunnable = null
                                                    }
                                                }

                                            }
                                            seekHandler.postDelayed(timerRunnable, 100)
                                            notifyDataSetChanged()
                                            player!!.start()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                } else {
                                    leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_black_24dp)
                                    player?.let {
                                        timeStampLong?.let { it1 ->
                                            playAudio(if (audioFile.exists()) audioPath else baseMessage.url, it1, it, leftAudioMessageHolder.binding.playButton,
                                                    leftAudioMessageHolder.binding.audioLength, leftAudioMessageHolder.binding.audioSeekBar, true)
                                        }
                                    }
                                }

                                leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_black_24dp)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                })

                filePath?.let { Log.d("metaData", it) }

            }

        }
    }

    private fun setStatusIcon(circleImageView: CircleImageView, baseMessage: BaseMessage) {

        if (baseMessage.readAt != 0L) {
            val drawable=ContextCompat.getDrawable(context,R.drawable.ic_double_tick_blue);
            drawable?.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
            circleImageView.setImageDrawable(drawable)
            circleImageView.circleBackgroundColor = context.resources.getColor(android.R.color.transparent)
        }
        else if (baseMessage.deliveredAt != 0L) {
               circleImageView.setImageResource(R.drawable.ic_double_tick)
               circleImageView.circleBackgroundColor = StringContract.Color.primaryColor
        }
        else{
            circleImageView.setImageResource(R.drawable.ic_check_24dp)
            circleImageView.circleBackgroundColor = StringContract.Color.primaryColor
        }
    }


    private fun startIntent(baseMessage: MediaMessage) {
        val imageIntent = Intent(context, ImageViewActivity::class.java)
        imageIntent.putExtra(StringContract.IntentString.FILE_TYPE, baseMessage.type);
        imageIntent.putExtra(StringContract.IntentString.URL, (baseMessage.url))
        context.startActivity(imageIntent)
    }

    fun playAudio(message: String?, sentTimeStamp: Long, player: MediaPlayer,
                  playButton: ImageView, audioLength: TextView, audioSeekBar: SeekBar, isLeft: Boolean = false) {
        try {
            currentPlayingSong = message
            currentlyPlayingId = sentTimeStamp
            if (timerRunnable != null) {
                seekHandler.removeCallbacks(timerRunnable)
                timerRunnable = null
            }
            //            setBtnColor(viewtype, playBtn, false);
            player.reset()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.setDataSource(currentPlayingSong)
            player.prepare()
            player.start()

            val duration = player.duration
            audioSeekBar.max = duration
            timerRunnable = object : Runnable {
                override fun run() {

                    val pos = player.currentPosition
                    audioSeekBar.progress = pos

                    if (player.isPlaying && pos < duration) {
                        audioLength.text = DateUtil.convertTimeStampToDurationTime(player.currentPosition.toLong())
                        seekHandler.postDelayed(this, 250)
                    } else {
                        seekHandler.removeCallbacks(timerRunnable)
                        timerRunnable = null
                    }
                }

            }
            seekHandler.postDelayed(timerRunnable, 100)
            notifyDataSetChanged()

            player.setOnCompletionListener { mp ->
                currentPlayingSong = ""
                currentlyPlayingId = 0L
                //                    setBtnColor(viewtype, playBtn, true);
                seekHandler.removeCallbacks(timerRunnable)
                timerRunnable = null
                mp.stop()
                audioLength.text = DateUtil.convertTimeStampToDurationTime(duration.toLong())
                audioSeekBar.progress = 0
                if (isLeft) {
                    playButton.setImageResource(R.drawable.ic_play_arrow_black)
                } else {
                    playButton.setImageResource(R.drawable.ic_play_arrow)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setDeliveryReceipts(messageReceipt: MessageReceipt) {
        val baseMessage =messagesList.get(messageReceipt.messageId.toLong())
        if (baseMessage!=null) {
            baseMessage.deliveredAt = messageReceipt.timestamp
            for(i in messagesList.size()-1 downTo 0){
                if(messagesList.get(messagesList.keyAt(i))?.deliveredAt!! >0){
                    break
                }else{
                    val message : BaseMessage? = messagesList.get(messagesList.keyAt(i))
                    message?.deliveredAt = messageReceipt.deliveredAt;
                    message?.id?.toLong()?.let { messagesList.put(it, message) }
                }
            }
            notifyDataSetChanged()
        }

    }

    fun setRead(messageReceipt: MessageReceipt) {
        val baseMessage = messagesList.get(messageReceipt.messageId.toLong())
        if (baseMessage != null) {
            for(i in messagesList.size()-1 downTo 0){
                if(messagesList.get(messagesList.keyAt(i))?.readAt!! >0){
                    break
                }else{
                    val message : BaseMessage? = messagesList.get(messagesList.keyAt(i))
                    message?.readAt = messageReceipt.readAt;
                    message?.id?.toLong()?.let { messagesList.put(it, message) }
                }
            }
            notifyDataSetChanged()
        }
    }

    fun setDeletedMessage(deletedMessage: BaseMessage) {
        messagesList.put(deletedMessage.id.toLong(),deletedMessage)
        notifyDataSetChanged()
    }

    fun setEditMessage(editMessage: BaseMessage) {
        messagesList.put(editMessage.id.toLong(),editMessage)
        notifyDataSetChanged()
    }

    fun setFilterList(it: MutableList<BaseMessage>) {
        messagesList.clear()
        for (baseMessage in it){
            messagesList.put(baseMessage.id.toLong(),baseMessage)
        }
        notifyDataSetChanged()
    }

}