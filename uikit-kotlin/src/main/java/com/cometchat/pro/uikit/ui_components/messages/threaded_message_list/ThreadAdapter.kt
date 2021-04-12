package com.cometchat.pro.uikit.ui_components.messages.threaded_message_list

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.*
import com.cometchat.pro.uikit.ui_components.messages.extensions.ExtensionResponseListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import org.json.JSONException
import org.json.JSONObject
import com.cometchat.pro.uikit.ui_components.messages.extensions.message_reaction.CometChatReactionInfoActivity
import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

class ThreadAdapter(context: Context, messageList: List<BaseMessage>, type: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private val TAG = "MessageAdapter"
        private val IMAGE_MESSAGE = 11
        private val VIDEO_MESSAGE = 12
        private val AUDIO_MESSAGE = 13
        private val TEXT_MESSAGE = 14
        private val REPLY_TEXT_MESSAGE = 15
        private val FILE_MESSAGE = 16
        private val LINK_MESSAGE = 17
        private val DELETE_MESSAGE = 18
        private val CUSTOM_MESSAGE = 19
        private val LOCATION_CUSTOM_MESSAGE = 20
        var LATITUDE = 0.0
        var LONGITUDE = 0.0
    }

    private val messageList: MutableList<BaseMessage> = ArrayList()


    var context: Context

    private val loggedInUser = CometChat.getLoggedInUser()

    private var isLongClickEnabled = false

    private val selectedItemList: MutableList<Int> = ArrayList()

    var longselectedItemList: MutableList<BaseMessage> = ArrayList()

    private var fontUtils: FontUtils? = null

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private var messagePosition = 0

    private var messageLongClick: OnMessageLongClick? = null



    private val isSent = false

    private var isTextMessageClick = false

    private var isImageMessageClick = false

    private var isLocationMessageClick = false

    init {
        this.context = context
//        this.longselectedItemList = messageList
        setMessageList(messageList)
        try {
            messageLongClick = context as CometChatThreadMessageListActivity
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fontUtils = FontUtils.getInstance(context)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewTypes(position)
    }

    private fun setMessageList(messageList: List<BaseMessage>) {
        this.messageList.addAll(0, messageList)
        notifyItemRangeInserted(0, messageList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view: View
        return when (i) {
            DELETE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageItemBinding: ThreadMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_item, parent, false)
                threadMessageItemBinding.root.tag = DELETE_MESSAGE
                DeleteMessageViewHolder(threadMessageItemBinding)
            }
            TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageItemBinding: ThreadMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_item, parent, false)
                threadMessageItemBinding.root.tag = TEXT_MESSAGE
                TextMessageViewHolder(threadMessageItemBinding)
            }
            REPLY_TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageItemBinding: ThreadMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_item, parent, false)
                threadMessageItemBinding.root.tag = REPLY_TEXT_MESSAGE
                TextMessageViewHolder(threadMessageItemBinding)
            }
            LINK_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageLinkItemBinding: ThreadMessageLinkItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_link_item, parent, false)
                threadMessageLinkItemBinding.root.tag = LINK_MESSAGE
                LinkMessageViewHolder(threadMessageLinkItemBinding)
            }
            AUDIO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageAudioLayoutBinding: ThreadMessageAudioItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_audio_item, parent, false)
                threadMessageAudioLayoutBinding.root.tag = AUDIO_MESSAGE
                AudioMessageViewHolder(threadMessageAudioLayoutBinding)
            }
            IMAGE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageImageItemBinding: ThreadMessageImageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_image_item, parent, false)
                threadMessageImageItemBinding.root.tag = IMAGE_MESSAGE
                ImageMessageViewHolder(threadMessageImageItemBinding)
            }
            VIDEO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageVideoItemBinding: ThreadMessageVideoItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_video_item, parent, false)
                threadMessageVideoItemBinding.root.tag = VIDEO_MESSAGE
                VideoMessageViewHolder(threadMessageVideoItemBinding)
            }
            FILE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageFileItemBinding: ThreadMessageFileItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_file_item, parent, false)
                threadMessageFileItemBinding.root.tag = FILE_MESSAGE
                FileMessageViewHolder(threadMessageFileItemBinding)
            }
            CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageItemBinding: ThreadMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_item, parent, false)
                threadMessageItemBinding.root.tag = TEXT_MESSAGE
                CustomMessageViewHolder(threadMessageItemBinding)
            }
            LOCATION_CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadLocationMessageItemBinding: ThreadLocationMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_location_message_item, parent, false)
                threadLocationMessageItemBinding.root.tag = LOCATION_CUSTOM_MESSAGE
                LocationMessageViewHolder(threadLocationMessageItemBinding)
            }
            else -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val threadMessageItemBinding : ThreadMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.thread_message_item, parent, false)
                threadMessageItemBinding.root.tag = -1
                TextMessageViewHolder(threadMessageItemBinding)
            }
        }
    }


    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        when (viewHolder.getItemViewType()) {
            DELETE_MESSAGE -> setDeleteData(viewHolder as DeleteMessageViewHolder, i)
            TEXT_MESSAGE, REPLY_TEXT_MESSAGE -> setTextData(viewHolder as TextMessageViewHolder, i)
            LINK_MESSAGE -> setLinkData(viewHolder as LinkMessageViewHolder, i)
            IMAGE_MESSAGE -> setImageData(viewHolder as ImageMessageViewHolder, i)
            AUDIO_MESSAGE -> setAudioData(viewHolder as AudioMessageViewHolder, i)
            VIDEO_MESSAGE -> setVideoData(viewHolder as VideoMessageViewHolder, i)
            FILE_MESSAGE -> setFileData(viewHolder as FileMessageViewHolder, i)
            CUSTOM_MESSAGE -> setCustomData(viewHolder as CustomMessageViewHolder, i)
            LOCATION_CUSTOM_MESSAGE -> setLocationData(viewHolder as LocationMessageViewHolder, i)
        }
    }

    private fun setLocationData(viewHolder: LocationMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]
        viewHolder.view.tvUser.setVisibility(View.VISIBLE)
        viewHolder.view.ivUser.setVisibility(View.VISIBLE)
        setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
        viewHolder.view.tvUser.setText(baseMessage.sender.name)
        setLocationData(baseMessage, viewHolder.view.tvPlaceName, viewHolder.view.ivMap)
        viewHolder.view.senderLocationTxt.setText(String.format(context!!.getString(R.string.shared_location), baseMessage.sender.name))
        viewHolder.view.navigateBtn.setOnClickListener(View.OnClickListener {
            try {
                val latitude = (baseMessage as CustomMessage).customData.getDouble("latitude")
                val longitude = baseMessage.customData.getDouble("longitude")
                val label = Utils.getAddress(context, latitude, longitude)
                val uriBegin = "geo:$latitude,$longitude"
                val encodedQuery = Uri.encode(label)
                val uriString = "$uriBegin?q=$encodedQuery&z=16"
                val uri = Uri.parse(uriString)
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                //                    mapIntent.setPackage("com.google.android.apps.maps");
                context!!.startActivity(mapIntent)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        showMessageTime(viewHolder, baseMessage)
        if (messageList[messageList.size - 1] == baseMessage) {
            selectedItemList.add(baseMessage.id)
        }
        if (selectedItemList.contains(baseMessage.id)) viewHolder.view.txtTime.setVisibility(View.VISIBLE) else viewHolder.view.txtTime.setVisibility(View.GONE)

        viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
            if (isLongClickEnabled && !isImageMessageClick) {
                setLongClickSelectedItem(baseMessage)
                messageLongClick!!.setLongMessageClick(longselectedItemList)
            } else {
                setSelectedMessage(baseMessage.id)
            }
            notifyDataSetChanged()
        })
        viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
            if (!isImageMessageClick && !isTextMessageClick) {
                isLongClickEnabled = true
                isLocationMessageClick = true
                setLongClickSelectedItem(baseMessage)
                messageLongClick!!.setLongMessageClick(longselectedItemList)
                notifyDataSetChanged()
            }
            true
        })
        viewHolder.view.reactionsLayout.visibility = View.GONE
        setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
    }

    private fun setLocationData(baseMessage: BaseMessage, tvAddress: TextView, ivMap: ImageView) {
        try {
            LATITUDE = (baseMessage as CustomMessage).customData.getDouble("latitude")
            LONGITUDE = baseMessage.customData.getDouble("longitude")
            tvAddress.text = Utils.getAddress(context, LATITUDE, LONGITUDE)
            val mapUrl = UIKitConstants.MapUrl.MAPS_URL + LATITUDE + "," + LONGITUDE + "&key=" + UIKitConstants.MapUrl.MAP_ACCESS_KEY
            Glide.with(context!!)
                    .load(mapUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setCustomData(viewHolder: CustomMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null) {
            setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
            viewHolder.view.tvUser.setText(baseMessage.sender.name)
            viewHolder.view.goTxtMessage.setText(context!!.resources.getString(R.string.custom_message))
            viewHolder.view.goTxtMessage.setTypeface(fontUtils!!.getTypeFace(FontUtils.robotoLight))
            viewHolder.view.goTxtMessage.setTextColor(context!!.resources.getColor(R.color.primaryTextColor))
            showMessageTime(viewHolder, baseMessage)
            if (messageList[messageList.size - 1] == baseMessage) {
                selectedItemList.add(baseMessage.id)
            }
            if (selectedItemList.contains(baseMessage.id)) viewHolder.view.txtTime.setVisibility(View.VISIBLE) else viewHolder.view.txtTime.setVisibility(View.GONE)
            viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
                setSelectedMessage(baseMessage.id)
                notifyDataSetChanged()
            })
            viewHolder.itemView.setTag(R.string.message, baseMessage)
        }
    }

    private fun setFileData(viewHolder: FileMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]

        if (baseMessage != null && baseMessage.deletedAt == 0L) {
            setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
            viewHolder.view.tvUser.setText(baseMessage.sender.name)
            viewHolder.view.tvFileName.setText((baseMessage as MediaMessage).attachment.fileName)
            viewHolder.view.tvFileExtension.setText(baseMessage.attachment.fileExtension)
            val fileSize = baseMessage.attachment.fileSize
            viewHolder.view.tvFileSize.setText(Utils.getFileSize(fileSize))
            showMessageTime(viewHolder, baseMessage)

//              if (selectedItemList.contains(baseMessage.getId()))
//                  viewHolder.txtTime.setVisibility(View.VISIBLE);
//              else
//                  viewHolder.txtTime.setVisibility(View.GONE);
            viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
//                  if (isLongClickEnabled && !isTextMessageClick) {
//                          setLongClickSelectedItem(baseMessage);
//                  }
//                  else {
                setSelectedMessage(baseMessage.getId())
                //                  }
                notifyDataSetChanged()
            })
            viewHolder.view.tvFileName.setOnClickListener(View.OnClickListener { view: View? -> MediaUtils.openFile(baseMessage.attachment.fileUrl, context!!) })
            viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            })
            viewHolder.view.reactionsLayout.visibility = View.GONE
            setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
        }
    }

    private fun setVideoData(viewHolder: VideoMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]

        setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
        viewHolder.view.tvUser.setText(baseMessage.sender.name)

        if ((baseMessage as MediaMessage).attachment != null) Glide.with(context!!).load(baseMessage.attachment.fileUrl).into(viewHolder.view.goVideoMessage)

        showMessageTime(viewHolder, baseMessage)
//        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//


        //        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//
        viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
            setSelectedMessage(baseMessage.getId())
            notifyDataSetChanged()
        })
        viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
            if (!isLongClickEnabled && !isTextMessageClick) {
                isImageMessageClick = true
                setLongClickSelectedItem(baseMessage)
                messageLongClick!!.setLongMessageClick(longselectedItemList)
                notifyDataSetChanged()
            }
            true
        })
        viewHolder.view.reactionsLayout.visibility = View.GONE
        setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
        viewHolder.view.playBtn.setOnClickListener(View.OnClickListener { MediaUtils.openFile((baseMessage as MediaMessage).attachment.fileUrl, context!!) })
    }

    private fun setAudioData(viewHolder: AudioMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null && baseMessage.deletedAt == 0L) {
            viewHolder.view.playBtn.imageTintList = ColorStateList.valueOf(context.resources.getColor(R.color.textColorWhite))
            setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
            viewHolder.view.tvUser.text = baseMessage.sender.name
            showMessageTime(viewHolder, baseMessage)
            //            if (selectedItemList.contains(baseMessage.getId()))
//                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
            viewHolder.view.audiolengthTv.setText(Utils.getFileSize((baseMessage as MediaMessage).attachment.fileSize))
            viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            viewHolder.view.playBtn.setOnClickListener(View.OnClickListener { //                    MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context);
                mediaPlayer.reset()
                if (messagePosition != i) {
                    notifyItemChanged(messagePosition)
                    messagePosition = i
                }
                try {
                    mediaPlayer.setDataSource(baseMessage.attachment.fileUrl)
                    mediaPlayer.prepare()
                    mediaPlayer.setOnCompletionListener { viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp) }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "MediaPlayerError: " + e.message)
                }
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    viewHolder.view.playBtn.setImageResource(R.drawable.ic_pause_24dp)
                } else {
                    mediaPlayer.pause()
                    viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                }
            })
            viewHolder.view.cvMessageContainer.setOnLongClickListener(OnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            })
            viewHolder.view.reactionsLayout.visibility = View.GONE
            setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
        }
    }


    private fun setImageData(viewHolder: ImageMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]

        setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
        viewHolder.view.tvUser.text = baseMessage.sender.name

        viewHolder.view.goImgMessage.setImageDrawable(context.resources.getDrawable(R.drawable.ic_defaulf_image))
        val isImageNotSafe = Extensions.getImageModeration(context, baseMessage)
        val thumbnailUrl = Extensions.getThumbnailGeneration(context, baseMessage)
        if (thumbnailUrl != null) {
//            Glide.with(context).asBitmap().load(thumbnailUrl).into(viewHolder.view.goImgMessage)
            if ((baseMessage as MediaMessage).attachment.fileExtension.equals(".gif", ignoreCase = true)) {
                setImageDrawable(viewHolder, thumbnailUrl, true, false)
            } else {
                setImageDrawable(viewHolder, thumbnailUrl, false, isImageNotSafe)
            }
        }
        else {
//            if ((baseMessage as MediaMessage).attachment != null)
//                Glide.with(context).asBitmap().load(baseMessage.attachment.fileUrl).into(viewHolder.view.goImgMessage)
            if ((baseMessage as MediaMessage).attachment.fileExtension.equals(".gif", ignoreCase = true))
                setImageDrawable(viewHolder, baseMessage.attachment.fileUrl, true, false)
            else
                setImageDrawable(viewHolder, baseMessage.attachment.fileUrl, false, isImageNotSafe)
        }
        if (isImageNotSafe) {
            viewHolder.view.sensitiveLayout.setVisibility(View.VISIBLE)
        } else {
            viewHolder.view.sensitiveLayout.setVisibility(View.GONE)
        }
//        if (smallUrl != null) {
//            Glide.with(context!!).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).load(smallUrl).into(object : SimpleTarget<Bitmap?>() {
//                fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    if (isImageNotSafe) viewHolder.imageView.setImageBitmap(Utils.blur(context, resource)) else viewHolder.imageView.setImageBitmap(resource)
//                }
//            })
//        } else {
//            Glide.with(context!!).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).load((baseMessage as MediaMessage).attachment.fileUrl).into(object : SimpleTarget<Bitmap?>() {
//                fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    if (isImageNotSafe) viewHolder.imageView.setImageBitmap(Utils.blur(context, resource)) else viewHolder.imageView.setImageBitmap(resource)
//                }
//            })
//        }
//        if (isImageNotSafe) {
//            viewHolder.sensitiveLayout.setVisibility(View.VISIBLE)
//        } else {
//            viewHolder.sensitiveLayout.setVisibility(View.GONE)
//        }
        showMessageTime(viewHolder, baseMessage)
//        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);


        //        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
        viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
            if (isImageNotSafe) {
                val alert = AlertDialog.Builder(context)
                alert.setTitle("Unsafe Content")
                alert.setIcon(R.drawable.ic_hand)
                alert.setMessage("Are you surely want to see this unsafe content")
                alert.setPositiveButton("Yes") { dialog, which -> MediaUtils.openFile((baseMessage as MediaMessage).attachment.fileUrl, context) }
                alert.setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                alert.create().show()
            } else {
            setSelectedMessage(baseMessage.id)
            notifyDataSetChanged()
            MediaUtils.openFile((baseMessage as MediaMessage).attachment.fileUrl, context!!)
            }
        })
        viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
            if (!isLongClickEnabled && !isTextMessageClick) {
                isImageMessageClick = true
                setLongClickSelectedItem(baseMessage)
                messageLongClick!!.setLongMessageClick(longselectedItemList)
                notifyDataSetChanged()
            }
            true
        })
        viewHolder.view.reactionsLayout.visibility = View.GONE
        setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
    }

    private fun setImageDrawable(viewHolder: ImageMessageViewHolder, url: String, gif: Boolean, isImageNotSafe: Boolean) {
        if (gif) {
            Glide.with(context).asGif().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).load(url).into(viewHolder.view.goImgMessage)
        } else {
            Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).load(url).into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            if (isImageNotSafe) viewHolder.view.goImgMessage.setImageBitmap(Utils.blur(context, resource)) else viewHolder.view.goImgMessage.setImageBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
        }
    }

    private fun setLinkData(viewHolder: LinkMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]

        var url: String? = null

        if (baseMessage != null) {
            setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
            viewHolder.view.tvUser.text = baseMessage.sender.name
            if (baseMessage.deletedAt == 0L) {
                val extensionList: HashMap<String, JSONObject> = Extensions.extensionCheck(baseMessage)!!
                if (extensionList != null) {
                    if (extensionList.containsKey("linkPreview")) {
                        val linkPreviewJsonObject = extensionList["linkPreview"]
                        try {
                            val description = linkPreviewJsonObject!!.getString("description")
                            val image = linkPreviewJsonObject.getString("image")
                            val title = linkPreviewJsonObject.getString("title")
                            url = linkPreviewJsonObject.getString("url")
                            Log.e("setLinkData: ", """$baseMessage $url $description $image """.trimIndent())
                            viewHolder.view.linkTitle.text = title
                            viewHolder.view.linkSubtitle.text = description
                            Glide.with(context).load(Uri.parse(image)).timeout(1000).into(viewHolder.view.linkImg)
                            if (url.contains("youtu.be") || url.contains("youtube")) {
                                viewHolder.view.videoLink.visibility = View.VISIBLE
                                viewHolder.view.visitLink.text = context.resources.getString(R.string.view_on_youtube)
                            } else {
                                viewHolder.view.videoLink.visibility = View.GONE
                                viewHolder.view.visitLink.text = context.resources.getString(R.string.visit)
                            }
                            val messageStr = (baseMessage as TextMessage).text
                            if ((baseMessage as TextMessage).text == url || (baseMessage as TextMessage).text == "$url/") {
                                viewHolder.view.message.visibility = View.GONE
                            } else {
                                viewHolder.view.message.visibility = View.VISIBLE
                            }
                            viewHolder.view.message.text = messageStr
                        } catch (e: java.lang.Exception) {
                            Log.e("setLinkData: ", e.message)
                        }
                    }
                }
            }
//            Utils.setHyperLinkSupport(context, viewHolder.message)
            showMessageTime(viewHolder, baseMessage)
            val finalUrl = url
//            viewHolder.linkVisit.setOnClickListener(View.OnClickListener {
//                if (finalUrl != null) {
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(finalUrl)
//                    context!!.startActivity(intent)
//                }
//            })
            //            if (selectedItemList.contains(baseMessage.getId()))
//                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
//            if (i < selectedItems.length && selectedItems[i] == 0) {
//                viewHolder.txtTime.setVisibility(View.GONE);
//            } else
//                viewHolder.txtTime.setVisibility(View.VISIBLE);
            viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener { view: View? ->
                if (isLongClickEnabled && !isImageMessageClick) {
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                } else {
                    setSelectedMessage(baseMessage.id)
                }
                notifyDataSetChanged()
            })
            viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
                if (!isImageMessageClick) {
                    isLongClickEnabled = true
                    isTextMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            })
            viewHolder.view.reactionsLayout.visibility = View.GONE
            setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
            viewHolder.itemView.setTag(R.string.message, baseMessage)
        }
    }

    private fun setTextData(viewHolder: TextMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null) {
//            if (baseMessage.sender.uid != loggedInUser.uid) {
//                val isSentimentNegative: Boolean = Extensions.checkSentiment(baseMessage)
//                if (isSentimentNegative) {
//                    viewHolder.view.goTxtMessage.setVisibility(View.GONE)
//                    viewHolder.sentimentVw.setVisibility(View.VISIBLE)
//                } else {
//                    viewHolder.view.goTxtMessage.setVisibility(View.VISIBLE)
//                    viewHolder.sentimentVw.setVisibility(View.GONE)
//                }
//                viewHolder.viewSentimentMessage.setOnClickListener(View.OnClickListener {
//                    val sentimentAlert = AlertDialog.Builder(context)
//                            .setTitle(context!!.resources.getString(R.string.sentiment_alert))
//                            .setMessage(context!!.resources.getString(R.string.sentiment_alert_message))
//                            .setPositiveButton(context!!.resources.getString(R.string.yes)) { dialog, which ->
//                                viewHolder.view.goTxtMessage.setVisibility(View.VISIBLE)
//                                viewHolder.sentimentVw.setVisibility(View.GONE)
//                            }
//                            .setNegativeButton(context!!.resources.getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
//                    sentimentAlert.create().show()
//                })
//            } else {
//                viewHolder.view.goTxtMessage.setVisibility(View.VISIBLE)
//                viewHolder.sentimentVw.setVisibility(View.GONE)
//            }
            setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
            viewHolder.view.tvUser.text = baseMessage.sender.name
            val txtMessage = (baseMessage as TextMessage).text.trim { it <= ' ' }
            viewHolder.view.goTxtMessage.textSize = 16f
//            var count = 0
//            val processed = EmojiCompat.get().process(txtMessage, 0,
//                    txtMessage.length - 1, Int.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
//            if (processed is Spannable) {
//                val spannable = processed
//                count = spannable.getSpans(0, spannable.length - 1, EmojiSpan::class.java).size
////                if (Utils.removeEmojiAndSymbol(txtMessage).trim().length() === 0) {
////                    if (count == 1) {
////                        viewHolder.view.goTxtMessage.setTextSize(Utils.dpToPx(context, 32f) as Int.toFloat())
////                    } else if (count == 2) {
////                        viewHolder.view.goTxtMessage.setTextSize(Utils.dpToPx(context, 24f) as Int.toFloat())
////                    }
////                }
//            }

            var message = txtMessage
//            if (isExtensionEnabled("profanity-filter"))
//                message = Extensions.getProfanityFilter(baseMessage)
//            if (isExtensionEnabled("data-masking"))
//                message = Extensions.checkDataMasking(baseMessage)

            if (Extensions.checkExtensionEnabled("data-masking")) {
                message = Extensions.checkDataMasking(baseMessage)
            }
            if (Extensions.checkExtensionEnabled("profanity-filter")) {
                message = Extensions.getProfanityFilter(baseMessage)
            }

//

            viewHolder.view.goTxtMessage.text = message
            viewHolder.view.goTxtMessage.typeface = fontUtils!!.getTypeFace(FontUtils.robotoRegular)
            viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
//            Utils.setHyperLinkSupport(context, viewHolder.txtMessage)
            showMessageTime(viewHolder, baseMessage)
            if (messageList[messageList.size - 1] == baseMessage) {
                selectedItemList.add(baseMessage.getId())
            }
            setColorFilter(baseMessage, viewHolder.view.cvMessageContainer)
            viewHolder.view.rlMessage.setOnClickListener(View.OnClickListener {
                if (isLongClickEnabled && !isImageMessageClick) {
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                } else {
                    setSelectedMessage(baseMessage.getId())
                }
                notifyDataSetChanged()
            })
            viewHolder.view.rlMessage.setOnLongClickListener(OnLongClickListener {
                if (!isImageMessageClick) {
                    isLongClickEnabled = true
                    isTextMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            })
            viewHolder.view.reactionsLayout.visibility = View.GONE
            setReactionSupport(baseMessage, viewHolder.view.reactionsLayout)
            viewHolder.itemView.setTag(R.string.message, baseMessage)
        }
    }

    private fun setReactionSupport(baseMessage: BaseMessage, reactionLayout: ChipGroup) {
        val reactionOnMessage = Extensions.getReactionsOnMessage(baseMessage)
        if (reactionOnMessage.size > 0) {
            reactionLayout.visibility = View.VISIBLE
            reactionLayout.removeAllViews()
            for ((k, v) in reactionOnMessage) {
                val chip = Chip(context)
                chip.chipStrokeWidth = 2f
                chip.chipBackgroundColor = ColorStateList.valueOf(context.resources.getColor(android.R.color.transparent))
                chip.chipStrokeColor = ColorStateList.valueOf(context.resources.getColor(R.color.colorPrimaryDark))
                chip.text = k + " " + reactionOnMessage[k]
                reactionLayout.addView(chip)
                chip.setOnLongClickListener {
                    val intent = Intent(context, CometChatReactionInfoActivity::class.java)
                    intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO, baseMessage.metadata.toString())
                    context.startActivity(intent)
                    true
                }
                chip.setOnClickListener {
                    val body = JSONObject()
                    try {
                        body.put("msgId", baseMessage.id)
                        body.put("emoji", k)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    CometChat.callExtension("reactions", "POST", "/v1/react", body,
                            object : CallbackListener<JSONObject?>() {
                                override fun onSuccess(responseObject: JSONObject?) {
                                    // ReactionModel added successfully.
                                }

                                override fun onError(e: CometChatException) {
                                    // Some error occured.
                                }
                            })
                }
            }
        }
    }

    private fun setColorFilter(baseMessage: BaseMessage, view: View) {
        if (!longselectedItemList.contains(baseMessage)) {
            view.background.setColorFilter(context.resources.getColor(R.color.message_bubble_grey), PorterDuff.Mode.SRC_ATOP)
        } else {
            view.background.setColorFilter(context.resources.getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun setDeleteData(viewHolder: DeleteMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]

        setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
        viewHolder.view.tvUser.text = baseMessage.sender.name

        if (baseMessage.deletedAt != 0L) {
            viewHolder.view.goTxtMessage.setText(R.string.this_message_deleted)
            viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.secondaryTextColor))
            viewHolder.view.goTxtMessage.setTypeface(null, Typeface.ITALIC)
        }
        showMessageTime(viewHolder, baseMessage)
    }

    private fun showMessageTime(viewHolder: RecyclerView.ViewHolder, baseMessage: BaseMessage) {
        if (viewHolder is TextMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LinkMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is ImageMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is FileMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        }
    }

    private fun setStatusIcon(txtTime: TextView, baseMessage: BaseMessage) {
        if (baseMessage.sender.uid == loggedInUser.uid) {
            if (baseMessage.readAt != 0L) {
                txtTime.text = Utils.getHeaderDate(baseMessage.readAt * 1000)
                //                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_double_tick, 0);
                txtTime.compoundDrawablePadding = 10
            } else if (baseMessage.deliveredAt != 0L) {
                txtTime.text = Utils.getHeaderDate(baseMessage.deliveredAt * 1000)
                //                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_black_24dp, 0);
                txtTime.compoundDrawablePadding = 10
            } else {
                txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
                //                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                txtTime.compoundDrawablePadding = 10
            }
        } else {
            txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
        }
    }

    fun setSelectedMessage(id: Int?) {
        if (selectedItemList.contains(id)) selectedItemList.remove(id!!) else selectedItemList.add(id!!)
    }
    fun setLongClickSelectedItem(baseMessage: BaseMessage?) {
        if (longselectedItemList.contains(baseMessage)) longselectedItemList.remove(baseMessage) else longselectedItemList.add(baseMessage!!)
    }

    private fun setAvatar(avatar: CometChatAvatar, avatarUrl: String?, name: String?) {
        if (avatarUrl != null && avatarUrl.isNotEmpty()) Glide.with(context).load(avatarUrl).into(avatar) else avatar.setInitials(name!!)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private fun getItemViewTypes(position: Int): Int {
        val baseMessage = messageList[position]
        val extensionList: HashMap<String, JSONObject>? = Extensions.extensionCheck(baseMessage)
        if (baseMessage.deletedAt == 0L) {
            if (baseMessage.category == CometChatConstants.CATEGORY_MESSAGE) {
                return when (baseMessage.type) {
                    CometChatConstants.MESSAGE_TYPE_TEXT -> if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList["linkPreview"] != null) LINK_MESSAGE else if (baseMessage.metadata != null && baseMessage.metadata.has("reply")) REPLY_TEXT_MESSAGE else TEXT_MESSAGE
                    CometChatConstants.MESSAGE_TYPE_AUDIO -> AUDIO_MESSAGE
                    CometChatConstants.MESSAGE_TYPE_IMAGE -> IMAGE_MESSAGE
                    CometChatConstants.MESSAGE_TYPE_VIDEO -> VIDEO_MESSAGE
                    CometChatConstants.MESSAGE_TYPE_FILE -> FILE_MESSAGE
                    else -> -1
                }
            } else {
                if (baseMessage.category == CometChatConstants.CATEGORY_CUSTOM) {
                    return if (baseMessage.type.equals("LOCATION", ignoreCase = true)) LOCATION_CUSTOM_MESSAGE else CUSTOM_MESSAGE
                }
            }
        } else {
            return DELETE_MESSAGE
        }
        return -1
    }

    fun updateList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    fun setDeliveryReceipts(messageReceipt: MessageReceipt) {
        for (i in messageList.indices.reversed()) {
            val baseMessage = messageList[i]
            if (baseMessage.deliveredAt == 0L) {
                val index = messageList.indexOf(baseMessage)
                messageList[index].deliveredAt = messageReceipt.deliveredAt
            }
        }
        notifyDataSetChanged()
    }
    fun setReadReceipts(messageReceipt: MessageReceipt) {
        for (i in messageList.indices.reversed()) {
            val baseMessage = messageList[i]
            if (baseMessage.readAt == 0L) {
                val index = messageList.indexOf(baseMessage)
                messageList[index].readAt = messageReceipt.readAt
            }
        }
        notifyDataSetChanged()
    }

    fun addMessage(baseMessage: BaseMessage?) {
//        if (!messageList.contains(baseMessage)) {
        messageList.add(baseMessage!!)
        selectedItemList.clear()
        //        }
        notifyItemInserted(messageList.size - 1)
    }

    fun setUpdatedMessage(baseMessage: BaseMessage?) {
        if (messageList.contains(baseMessage)) {
            val index = messageList.indexOf(baseMessage)
            messageList.remove(baseMessage)
            messageList.add(index, baseMessage!!)
            notifyItemChanged(index)
        }
    }

    fun clearLongClickSelectedItem() {
        isLongClickEnabled = false
        isTextMessageClick = false
        isImageMessageClick = false
        isLocationMessageClick = false
        longselectedItemList.clear()
        notifyDataSetChanged()
    }

    fun getLastMessage(): BaseMessage? {
        return if (messageList.size > 0) {
            Log.e(TAG, "getLastMessage: " + messageList[messageList.size - 1])
            messageList[messageList.size - 1]
        } else null
    }

    fun stopPlayingAudio() {
        mediaPlayer.stop()
    }

    //delete
    inner class DeleteMessageViewHolder(val view: ThreadMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //text
    inner class TextMessageViewHolder(val view: ThreadMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //link
    inner class LinkMessageViewHolder(val view: ThreadMessageLinkItemBinding) : RecyclerView.ViewHolder(view.root)

    //audio
    inner class AudioMessageViewHolder(val view: ThreadMessageAudioItemBinding) : RecyclerView.ViewHolder(view.root)

    //image
    inner class ImageMessageViewHolder(val view: ThreadMessageImageItemBinding) : RecyclerView.ViewHolder(view.root)

    //video
    inner class VideoMessageViewHolder(val view: ThreadMessageVideoItemBinding) : RecyclerView.ViewHolder(view.root)

    //file
    inner class FileMessageViewHolder(val view: ThreadMessageFileItemBinding) : RecyclerView.ViewHolder(view.root)

    //location
    inner  class LocationMessageViewHolder(val view: ThreadLocationMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //custom
    inner class CustomMessageViewHolder(val view: ThreadMessageItemBinding) : RecyclerView.ViewHolder(view.root)


    interface OnMessageLongClick {
        fun setLongMessageClick(baseMessage: List<BaseMessage>?)
    }
}
