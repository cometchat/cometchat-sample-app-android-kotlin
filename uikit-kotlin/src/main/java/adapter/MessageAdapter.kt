package adapter

import adapter.MessageAdapter.DateItemHolder
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.text.Spannable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.EmojiSpan
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.Avatar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.*
import constant.StringContract
import listeners.StickyHeaderAdapter
import org.json.JSONException
import screen.messagelist.CometChatMessageListActivity
import screen.threadconversation.CometChatThreadMessageActivity
import utils.*
import java.util.*

/**
 * Purpose - MessageAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of messages. It helps to organize the messages based on its type i.e TextMessage,
 * MediaMessage, Actions. It also helps to manage whether message is sent or recieved and organizes
 * view based on it. It is single adapter used to manage group messages and user messages.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class MessageAdapter(context: Context, messageList: List<BaseMessage>, type: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderAdapter<DateItemHolder?> {
    private val messageList: MutableList<BaseMessage> = ArrayList()
    var context: Context
    private val loggedInUser = CometChat.getLoggedInUser()
    private var isLongClickEnabled = false
    private val selectedItemList: MutableList<Int> = ArrayList()
    var longselectedItemList: MutableList<BaseMessage> = ArrayList()
    private val fontUtils: FontUtils
    private var mediaPlayer: MediaPlayer? = null
    private var messagePosition = 0
    private var messageLongClick: OnMessageLongClick? = null
    private var isUserDetailVisible = false
    private val TAG = "MessageAdapter"
    private val isSent = false
    private var isTextMessageClick = false
    private var isImageMessageClick = false
    private var isLocationMessageClick = false

    companion object {
        private const val RIGHT_IMAGE_MESSAGE = 56
        private const val LEFT_IMAGE_MESSAGE = 89
        private const val RIGHT_VIDEO_MESSAGE = 78
        private const val LEFT_VIDEO_MESSAGE = 87
        private const val RIGHT_AUDIO_MESSAGE = 39
        private const val LEFT_AUDIO_MESSAGE = 93
        private const val CALL_MESSAGE = 234
        private const val LEFT_TEXT_MESSAGE = 1
        private const val RIGHT_TEXT_MESSAGE = 2
        private const val RIGHT_FILE_MESSAGE = 23
        private const val LEFT_FILE_MESSAGE = 25
        private const val ACTION_MESSAGE = 99
        private const val RIGHT_LINK_MESSAGE = 12
        private const val LEFT_LINK_MESSAGE = 13
        private const val LEFT_DELETE_MESSAGE = 551
        private const val RIGHT_DELETE_MESSAGE = 552
        private const val RIGHT_CUSTOM_MESSAGE = 432
        private const val LEFT_CUSTOM_MESSAGE = 431
        private const val RIGHT_REPLY_TEXT_MESSAGE = 987
        private const val LEFT_REPLY_TEXT_MESSAGE = 789
        private const val LEFT_LOCATION_CUSTOM_MESSAGE = 31
        private const val RIGHT_LOCATION_CUSTOM_MESSAGE = 32
        private const val RIGHT_STICKER_MESSAGE = 21
        private const val LEFT_STICKER_MESSAGE = 22

        var LATITUDE = 0.0
        var LONGITUDE = 0.0

    }
    /**
     * It is used to initialize the adapter wherever we needed. It has parameter like messageList
     * which contains list of messages and it will be used in adapter and paramter type is a String
     * whose values will be either CometChatConstants.RECEIVER_TYPE_USER
     * CometChatConstants.RECEIVER_TYPE_GROUP.
     *
     *
     * @param context is a object of Context.
     * @param messageList is a list of messages used in this adapter.
     * @param type is a String which identifies whether it is a user messages or a group messages.
     */
    init {
        setMessageList(messageList)
        this.context = context
        try {
            messageLongClick = context as CometChatMessageListActivity
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (null == mediaPlayer) {
            mediaPlayer = MediaPlayer()
        }
        fontUtils = FontUtils.getInstance(context)
    }

    /**
     * This method is used to return the different view types to adapter based on item position.
     * It uses getItemViewTypes() method to identify the view type of item.
     * @see MessageAdapter.getItemViewTypes
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     * @see MessageAdapter.onCreateViewHolder
     */
    override fun getItemViewType(position: Int): Int {
        return getItemViewTypes(position)
    }

    private fun setMessageList(messageList: List<BaseMessage>) {
        this.messageList.addAll(0, messageList)
        notifyItemRangeInserted(0, messageList.size)
    }

    /**
     * This method is used to inflate the view for item based on its viewtype.
     * It helps to differentiate view for different type of messages.
     * Based on view type it returns various ViewHolder
     * Ex :- For MediaMessage it will return ImageMessageViewHolder,
     * For TextMessage it will return TextMessageViewHolder,etc.
     *
     * @param parent is a object of ViewGroup.
     * @param i is viewType based on it various view will be inflated by adapter for various type
     * of messages.
     * @return It return different ViewHolder for different viewType.
     */
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view: View
        return when (i) {
            LEFT_DELETE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val deleteMessageItemBinding: LeftDeleteMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_delete_message_item, parent, false)
                deleteMessageItemBinding.root.tag = LEFT_DELETE_MESSAGE
                LeftDeleteMessageViewHolder(deleteMessageItemBinding)
            }
            RIGHT_DELETE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val deleteMessageItemBinding: RightDeleteMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_delete_message_item, parent, false)
                deleteMessageItemBinding.root.tag = RIGHT_DELETE_MESSAGE
                RightDeleteMessageViewHolder(deleteMessageItemBinding)
            }
            LEFT_TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: LeftMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_message_item, parent, false)
                textMessageItemBinding.root.tag = LEFT_TEXT_MESSAGE
                LeftTextMessageViewHolder(textMessageItemBinding)
            }
            RIGHT_TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: RightMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_message_item, parent, false)
                textMessageItemBinding.root.tag = RIGHT_TEXT_MESSAGE
                RightTextMessageViewHolder(textMessageItemBinding)
            }
            LEFT_REPLY_TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: LeftMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_message_item, parent, false)
                textMessageItemBinding.root.tag = LEFT_REPLY_TEXT_MESSAGE
                LeftTextMessageViewHolder(textMessageItemBinding)
            }
            RIGHT_REPLY_TEXT_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: RightMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_message_item, parent, false)
                textMessageItemBinding.root.tag = RIGHT_REPLY_TEXT_MESSAGE
                RightTextMessageViewHolder(textMessageItemBinding)
            }
            RIGHT_LINK_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val linkMessageItemBinding: MessageRightLinkItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_right_link_item, parent, false)
                linkMessageItemBinding.root.tag = RIGHT_LINK_MESSAGE
                RightLinkMessageViewHolder(linkMessageItemBinding)
            }
            LEFT_LINK_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val linkMessageItemBinding: MessageLeftLinkItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_left_link_item, parent, false)
                linkMessageItemBinding.root.tag = LEFT_LINK_MESSAGE
                LeftLinkMessageViewHolder(linkMessageItemBinding)
            }
            RIGHT_AUDIO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val audioMessageItemBinding: CometchatAudioLayoutRightBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_audio_layout_right, parent, false)
                audioMessageItemBinding.root.tag = RIGHT_AUDIO_MESSAGE
                RightAudioMessageViewHolder(audioMessageItemBinding)
            }
            LEFT_AUDIO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val audioMessageItemBinding: CometchatAudioLayoutLeftBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_audio_layout_left, parent, false)
                audioMessageItemBinding.root.tag = LEFT_AUDIO_MESSAGE
                LeftAudioMessageViewHolder(audioMessageItemBinding)
            }
            LEFT_IMAGE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val imageMessageItemBinding: MessageLeftListImageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_left_list_image_item, parent, false)
                imageMessageItemBinding.root.tag = LEFT_IMAGE_MESSAGE
                LeftImageMessageViewHolder(imageMessageItemBinding)
            }
            RIGHT_IMAGE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val imageMessageItemBinding: MessageRightListImageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_right_list_image_item, parent, false)
                imageMessageItemBinding.root.tag = RIGHT_IMAGE_MESSAGE
                RightImageMessageViewHolder(imageMessageItemBinding)
            }
            LEFT_VIDEO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val videoMessageItemBinding: MessageLeftListVideoItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_left_list_video_item, parent, false)
                videoMessageItemBinding.root.tag = LEFT_VIDEO_MESSAGE
                LeftVideoMessageViewHolder(videoMessageItemBinding)
            }
            RIGHT_VIDEO_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val videoMessageItemBinding: MessageRightListVideoItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_right_list_video_item, parent, false)
                videoMessageItemBinding.root.tag = RIGHT_VIDEO_MESSAGE
                RightVideoMessageViewHolder(videoMessageItemBinding)
            }
            RIGHT_FILE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val fileMessageItemBinding: CometchatRightFileMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_right_file_message, parent, false)
                fileMessageItemBinding.root.tag = RIGHT_FILE_MESSAGE
                RightFileMessageViewHolder(fileMessageItemBinding)
            }
            LEFT_FILE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val fileMessageItemBinding: CometchatLeftFileMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_left_file_message, parent, false)
                fileMessageItemBinding.root.tag = LEFT_FILE_MESSAGE
                LeftFileMessageViewHolder(fileMessageItemBinding)
            }
            ACTION_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val actionMessageItemBinding: CometchatActionMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_action_message, parent, false)
                actionMessageItemBinding.root.tag = ACTION_MESSAGE
                ActionMessageViewHolder(actionMessageItemBinding)
            }
            CALL_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val callMessageItemBinding: CometchatActionMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_action_message, parent, false)
                callMessageItemBinding.root.tag = CALL_MESSAGE
                ActionMessageViewHolder(callMessageItemBinding)
            }
            RIGHT_CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val customMessageItemBinding: RightCustomMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_custom_message_item, parent, false)
                customMessageItemBinding.root.tag = RIGHT_CUSTOM_MESSAGE
                RightCustomMessageViewHolder(customMessageItemBinding)
            }
            LEFT_CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val customMessageItemBinding: LeftCustomMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_custom_message_item, parent, false)
                customMessageItemBinding.root.tag = LEFT_CUSTOM_MESSAGE
                LeftCustomMessageViewHolder(customMessageItemBinding)
            }
            RIGHT_LOCATION_CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val locationCustomMessageItemBinding: RightLocationMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_location_message_item, parent, false)
                locationCustomMessageItemBinding.root.tag = RIGHT_LOCATION_CUSTOM_MESSAGE
                RightLocationMessageViewHolder(locationCustomMessageItemBinding)
            }
            LEFT_LOCATION_CUSTOM_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val locationCustomMessageItemBinding: LeftLocationMessageItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_location_message_item, parent, false)
                locationCustomMessageItemBinding.root.tag = LEFT_LOCATION_CUSTOM_MESSAGE
                LeftLocationMessageViewHolder(locationCustomMessageItemBinding)
            }
            LEFT_STICKER_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val messageStickerItemBinding: MessageLeftStickerItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_left_sticker_item, parent, false)
                messageStickerItemBinding.root.tag = LEFT_STICKER_MESSAGE
                LeftStickerMessageViewHolder(messageStickerItemBinding)
            }
            RIGHT_STICKER_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val messageStickerItemBinding: MessageRightStickerItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.message_right_sticker_item, parent, false)
                messageStickerItemBinding.root.tag = RIGHT_STICKER_MESSAGE
                RightStickerMessageViewHolder(messageStickerItemBinding)
            }
            else -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val actionMessageItemBinding : CometchatActionMessageBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_action_message, parent, false)
                actionMessageItemBinding.root.tag = -1
                ActionMessageViewHolder(actionMessageItemBinding)
            }
        }
    }

    /**
     * This method is used to bind the various ViewHolder content with their respective view types.
     * Here different methods are being called for different view type and in each method different
     * ViewHolder are been passed as parameter along with position of item.
     *
     * Ex :- For TextMessage `setTextData((TextMessageViewHolder)viewHolder,i)` is been called.
     * where **viewHolder** is casted as **TextMessageViewHolder** and **i** is position of item.
     *
     * @see MessageAdapter.setTextData
     * @see MessageAdapter.setImageData
     * @see MessageAdapter.setFileData
     * @see MessageAdapter.setActionData
     * @param viewHolder is a object of RecyclerViewHolder.
     * @param i is position of item in recyclerView.
     */
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        var nextMessage: BaseMessage? = null
        var prevMessage: BaseMessage? = null
        var isNextMessage = false
        var isPreviousMessage = false
        var isPrevActionMessage = false
        if (i + 1 < messageList.size) {
            if (messageList[i + 1].sender != null) nextMessage = messageList[i + 1]
        }
        if (i - 1 >= 0) {
            if (messageList[i - 1].sender != null) prevMessage = messageList[i - 1]
        }
        isPrevActionMessage = prevMessage != null && (prevMessage.category == CometChatConstants.CATEGORY_ACTION || prevMessage.category == CometChatConstants.CATEGORY_CALL)
        isNextMessage = nextMessage != null && baseMessage.sender.uid == nextMessage.sender.uid
        isPreviousMessage = prevMessage != null && baseMessage.sender.uid == prevMessage.sender.uid
        if (!isPreviousMessage && isNextMessage) {
            isUserDetailVisible = true
        }
        if (isPreviousMessage && isNextMessage) {
            isUserDetailVisible = false
        } else if (!isNextMessage && !isPreviousMessage) {
            isUserDetailVisible = true
        } else if (!isNextMessage) {
            isUserDetailVisible = false
        }
        if (isPrevActionMessage) {
            isUserDetailVisible = true
        }
        when (viewHolder.itemViewType) {
            LEFT_DELETE_MESSAGE -> setDeleteData(viewHolder as LeftDeleteMessageViewHolder, i)
            RIGHT_DELETE_MESSAGE -> setDeleteData(viewHolder as RightDeleteMessageViewHolder, i)
            LEFT_TEXT_MESSAGE -> setTextData(viewHolder as LeftTextMessageViewHolder, i)
            LEFT_REPLY_TEXT_MESSAGE -> setTextData(viewHolder as LeftTextMessageViewHolder, i)
            RIGHT_TEXT_MESSAGE -> setTextData(viewHolder as RightTextMessageViewHolder, i)
            RIGHT_REPLY_TEXT_MESSAGE -> setTextData(viewHolder as RightTextMessageViewHolder, i)
            LEFT_LINK_MESSAGE -> setLinkData(viewHolder as LeftLinkMessageViewHolder, i)
            RIGHT_LINK_MESSAGE -> setLinkData(viewHolder as RightLinkMessageViewHolder, i)
            LEFT_IMAGE_MESSAGE -> setImageData(viewHolder as LeftImageMessageViewHolder, i)
            RIGHT_IMAGE_MESSAGE -> setImageData(viewHolder as RightImageMessageViewHolder, i)
            LEFT_AUDIO_MESSAGE -> setAudioData(viewHolder as LeftAudioMessageViewHolder, i)
            RIGHT_AUDIO_MESSAGE -> setAudioData(viewHolder as RightAudioMessageViewHolder, i)
            LEFT_VIDEO_MESSAGE -> setVideoData(viewHolder as LeftVideoMessageViewHolder, i)
            RIGHT_VIDEO_MESSAGE -> setVideoData(viewHolder as RightVideoMessageViewHolder, i)
            LEFT_FILE_MESSAGE -> setFileData(viewHolder as LeftFileMessageViewHolder, i)
            RIGHT_FILE_MESSAGE -> setFileData(viewHolder as RightFileMessageViewHolder, i)
            ACTION_MESSAGE, CALL_MESSAGE -> setActionData(viewHolder as ActionMessageViewHolder, i)
            LEFT_CUSTOM_MESSAGE -> setCustomData(viewHolder as LeftCustomMessageViewHolder, i)
            RIGHT_CUSTOM_MESSAGE -> setCustomData(viewHolder as RightCustomMessageViewHolder, i)
            LEFT_LOCATION_CUSTOM_MESSAGE -> setLocationData(viewHolder as LeftLocationMessageViewHolder, i)
            RIGHT_LOCATION_CUSTOM_MESSAGE -> setLocationData(viewHolder as RightLocationMessageViewHolder, i)
            LEFT_STICKER_MESSAGE -> setStickerData(viewHolder as LeftStickerMessageViewHolder, i)
            RIGHT_STICKER_MESSAGE -> setStickerData(viewHolder as RightStickerMessageViewHolder, i)
        }
    }

    private fun setStickerData(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null && baseMessage.deletedAt == 0L){
//            var viewHolder : RecyclerView.ViewHolder
            if (viewHolder is LeftStickerMessageViewHolder){
                if (baseMessage.sender.uid != loggedInUser.uid) {
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        viewHolder.view.tvUser.setVisibility(View.GONE)
                        viewHolder.view.ivUser.setVisibility(View.GONE)
                    } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        if (isUserDetailVisible) {
                            viewHolder.view.tvUser.setVisibility(View.VISIBLE)
                            viewHolder.view.ivUser.setVisibility(View.VISIBLE)
                        } else {
                            viewHolder.view.tvUser.setVisibility(View.GONE)
                            viewHolder.view.ivUser.setVisibility(View.INVISIBLE)
                        }
                        setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                        viewHolder.view.tvUser.setText(baseMessage.sender.name)
                    }
                }
                viewHolder.view.stickerView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_defaulf_image))
                try {
                    Glide.with(context).load((baseMessage as CustomMessage).customData.getString("url")).into(viewHolder.view.stickerView)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener {
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, StringContract.IntentStrings.STICKERS)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    try {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as CustomMessage).customData.getString("name"))
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.customData.getString("url"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                }



                showMessageTime(viewHolder, baseMessage)
//        if (selectedItemList.contains(baseMessage.getId()))
                //        if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.setVisibility(View.VISIBLE)
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);


                //        else
//            viewHolder.txtTime.setVisibility(View.GONE);
                viewHolder.view.stickerView.setOnLongClickListener(OnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                })


            }
            else{
                viewHolder as RightStickerMessageViewHolder
                viewHolder.view.stickerView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_defaulf_image))
                try {
                    Glide.with(context).load((baseMessage as CustomMessage).customData.getString("url")).into(viewHolder.view.stickerView)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener {
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, StringContract.IntentStrings.STICKERS)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    try {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as CustomMessage).customData.getString("name"))
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.customData.getString("url"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                }



                showMessageTime(viewHolder, baseMessage)
//        if (selectedItemList.contains(baseMessage.getId()))
                //        if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.setVisibility(View.VISIBLE)
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);


                //        else
//            viewHolder.txtTime.setVisibility(View.GONE);
                viewHolder.view.stickerView.setOnLongClickListener(OnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                })
            }
        }
    }

    private fun setLocationData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null && baseMessage.deletedAt == 0L) {
            var viewHolder : RecyclerView.ViewHolder
            if (view is LeftLocationMessageViewHolder){
                viewHolder = view as LeftLocationMessageViewHolder
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP){
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.setText(baseMessage.sender.name)
                }
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    try {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, StringContract.IntentStrings.LOCATION)
                        intent.putExtra(StringContract.IntentStrings.LOCATION_LATITUDE,
                                (baseMessage as CustomMessage).customData.getDouble("latitude"))
                        intent.putExtra(StringContract.IntentStrings.LOCATION_LONGITUDE,
                                baseMessage.customData.getDouble("longitude"))
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "startThreadActivityError: " + e.message)
                    }
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                })

                setLocationData(baseMessage, viewHolder.view.tvPlaceName, viewHolder.view.ivMap)
                viewHolder.view.senderLocationTxt.setText(String.format(context.getString(R.string.shared_location), baseMessage.sender.name))
                viewHolder.view.navigateBtn.setOnClickListener(View.OnClickListener {
                    try {
                        val latitude = (baseMessage as CustomMessage).customData.getDouble("latitude")
                        val longitude = (baseMessage as CustomMessage).customData.getDouble("longitude")
                        val label = Utils.getAddress(context, latitude, longitude)
                        val uriBegin = "geo:$latitude,$longitude"
                        val encodedQuery = Uri.encode(label)
                        val uriString = "$uriBegin?q=$encodedQuery&z=16"
                        val uri = Uri.parse(uriString)
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        //                    mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                })
                showMessageTime(viewHolder, baseMessage)
                viewHolder.view.txtTime.setVisibility(View.VISIBLE);
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
            }
            else {
                viewHolder = view as RightLocationMessageViewHolder
//                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
//                    viewHolder.view.tvUser.visibility = View.GONE
//                    viewHolder.view.ivUser.visibility = View.GONE
//                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP){
//                    if (isUserDetailVisible) {
//                        viewHolder.view.tvUser.visibility = View.VISIBLE
//                        viewHolder.view.ivUser.visibility = View.VISIBLE
//                    } else {
//                        viewHolder.view.tvUser.visibility = View.GONE
//                        viewHolder.view.ivUser.visibility = View.INVISIBLE
//                    }
//                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
//                    viewHolder.view.tvUser.setText(baseMessage.sender.name)
//                }

                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    try {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, StringContract.IntentStrings.LOCATION)
                        intent.putExtra(StringContract.IntentStrings.LOCATION_LATITUDE,
                                (baseMessage as CustomMessage).customData.getDouble("latitude"))
                        intent.putExtra(StringContract.IntentStrings.LOCATION_LONGITUDE,
                                baseMessage.customData.getDouble("longitude"))
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "startThreadActivityError: " + e.message)
                    }
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                })
                setLocationData(baseMessage, viewHolder.view.tvPlaceName, viewHolder.view.ivMap)
                viewHolder.view.senderLocationTxt.setText(String.format(context.getString(R.string.shared_location), baseMessage.sender.name))
                viewHolder.view.navigateBtn.setOnClickListener(View.OnClickListener {
                    try {
                        val latitude = (baseMessage as CustomMessage).customData.getDouble("latitude")
                        val longitude = (baseMessage as CustomMessage).customData.getDouble("longitude")
                        val label = Utils.getAddress(context, latitude, longitude)
                        val uriBegin = "geo:$latitude,$longitude"
                        val encodedQuery = Uri.encode(label)
                        val uriString = "$uriBegin?q=$encodedQuery&z=16"
                        val uri = Uri.parse(uriString)
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        //                    mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                })
                showMessageTime(viewHolder, baseMessage)
                viewHolder.view.txtTime.setVisibility(View.VISIBLE);
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
            }
        }
    }
    private fun setLocationData(baseMessage: BaseMessage, tvAddress: TextView, ivMap: ImageView) {
        try {
            LATITUDE = (baseMessage as CustomMessage).customData.getDouble("latitude")
            LONGITUDE = baseMessage.customData.getDouble("longitude")
            tvAddress.text = Utils.getAddress(context, LATITUDE, LONGITUDE)
            val mapUrl = StringContract.MapUrl.MAPS_URL + LATITUDE + "," + LONGITUDE + "&key=" + StringContract.MapUrl.MAP_ACCESS_KEY
            Glide.with(context)
                    .load(mapUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setAudioData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null && baseMessage.deletedAt == 0L) {
            var viewHolder : RecyclerView.ViewHolder
            if (view is LeftAudioMessageViewHolder) {
                viewHolder = view as LeftAudioMessageViewHolder;
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.text = baseMessage.sender.name
                }
                showMessageTime(viewHolder, baseMessage)
//                    if (selectedItemList.contains(baseMessage.id))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                    else viewHolder.view.txtTime.visibility = View.GONE

                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                viewHolder.view.audiolengthTv.text = Utils.getFileSize((baseMessage as MediaMessage).attachment.fileSize)
                viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                viewHolder.view.playBtn.setOnClickListener {
                    //                    MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context);
                    mediaPlayer!!.reset()
                    if (messagePosition != i) {
                        notifyItemChanged(messagePosition)
                        messagePosition = i
                    }
                    try {
                        mediaPlayer!!.setDataSource(baseMessage.attachment.fileUrl)
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.setOnCompletionListener { (viewHolder as LeftAudioMessageViewHolder).view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp) }
                    } catch (e: Exception) {
                        Log.e(TAG, "MediaPlayerError: " + e.message)
                    }
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.start()
                        (viewHolder as LeftAudioMessageViewHolder).view.playBtn.setImageResource(R.drawable.ic_pause_24dp)
                    } else {
                        mediaPlayer!!.pause()
                        (viewHolder as LeftAudioMessageViewHolder).view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    }
                }
                viewHolder.view.cvMessageContainer.setOnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
            } else{
                viewHolder = view as RightAudioMessageViewHolder
                showMessageTime(viewHolder, baseMessage)
//                if (selectedItemList.contains(baseMessage.id))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                viewHolder.view.audiolengthTv.text = Utils.getFileSize((baseMessage as MediaMessage).attachment.fileSize)
                viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                viewHolder.view.playBtn.setOnClickListener {
                    //                    MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context);
                    mediaPlayer!!.reset()
                    if (messagePosition != i) {
                        notifyItemChanged(messagePosition)
                        messagePosition = i
                    }
                    try {
                        mediaPlayer!!.setDataSource(baseMessage.attachment.fileUrl)
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.setOnCompletionListener { (viewHolder as RightAudioMessageViewHolder).view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp) }
                    } catch (e: Exception) {
                        Log.e(TAG, "MediaPlayerError: " + e.message)
                    }
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.start()
                        viewHolder.view.playBtn.setImageResource(R.drawable.ic_pause_24dp)
                    } else {
                        mediaPlayer!!.pause()
                        viewHolder.view.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    }
                }
                viewHolder.view.cvMessageContainer.setOnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
            }

        }
    }

    fun stopPlayingAudio() {
        if (mediaPlayer != null) mediaPlayer!!.stop()
    }

    /**
     * This method is called whenever viewType of item is file. It is used to bind FileMessageViewHolder
     * contents with MediaMessage at a given position.
     * It shows FileName, FileType, FileSize.
     *
     * @param viewHolder is a object of FileMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     *
     * @see BaseMessage
     */
    private fun setFileData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null && baseMessage.deletedAt == 0L) {
            var viewHolder : RecyclerView.ViewHolder
            if (view is LeftFileMessageViewHolder) {
                viewHolder = view as LeftFileMessageViewHolder
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.text = baseMessage.sender.name
                }
                viewHolder.view.tvFileName.text = (baseMessage as MediaMessage).attachment.fileName
                viewHolder.view.tvFileExtension.text = baseMessage.attachment.fileExtension
                val fileSize = baseMessage.attachment.fileSize
                viewHolder.view.tvFileSize.text = Utils.getFileSize(fileSize)
                showMessageTime(viewHolder, baseMessage)
//                if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                if (baseMessage.getReplyCount() != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                  intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, baseMessage.attachment.fileName)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                viewHolder.view.cvMessageContainer.setOnClickListener { view: View? ->
                    //                  if (isLongClickEnabled && !isTextMessageClick) {
//                          setLongClickSelectedItem(baseMessage);
//                  }
//                  else {
                    setSelectedMessage(baseMessage.getId())
                    //                  }
                    notifyDataSetChanged()
                }
                viewHolder.view.tvFileName.setOnClickListener { view: View? -> MediaUtils.openFile(baseMessage.attachment.fileUrl, context) }
                viewHolder.view.cvMessageContainer.setOnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
            } else {
                viewHolder = view as RightFileMessageViewHolder
                viewHolder.view.tvFileName.text = (baseMessage as MediaMessage).attachment.fileName
                viewHolder.view.tvFileExtension.text = baseMessage.attachment.fileExtension
                val fileSize = baseMessage.attachment.fileSize
                viewHolder.view.tvFileSize.text = Utils.getFileSize(fileSize)
                showMessageTime(viewHolder, baseMessage)
//                if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                if (baseMessage.getReplyCount() != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                  intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, baseMessage.attachment.fileName)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                viewHolder.view.cvMessageContainer.setOnClickListener { view: View? ->
                    //                  if (isLongClickEnabled && !isTextMessageClick) {
//                          setLongClickSelectedItem(baseMessage);
//                  }
//                  else {
                    setSelectedMessage(baseMessage.getId())
                    //                  }
                    notifyDataSetChanged()
                }
                viewHolder.view.tvFileName.setOnClickListener { view: View? -> MediaUtils.openFile(baseMessage.attachment.fileUrl, context) }
                viewHolder.view.cvMessageContainer.setOnLongClickListener {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
            }
        }
    }

    /**
     * This method is called whenever viewType of item is media. It is used to bind ImageMessageViewHolder
     * contents with MediaMessage at a given position.
     * It loads image of MediaMessage using its url.
     *
     * @param viewHolder is a object of ImageMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     *
     * @see BaseMessage
     */
    private fun setImageData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        Log.d(TAG, "setImageData: imgBasemsg " + baseMessage)
        var viewHolder : RecyclerView.ViewHolder
        if (view is LeftImageMessageViewHolder) {
            viewHolder = view as LeftImageMessageViewHolder
            if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                viewHolder.view.tvUser.visibility = View.GONE
                viewHolder.view.ivUser.visibility = View.GONE
            } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (isUserDetailVisible) {
                    viewHolder.view.tvUser.visibility = View.VISIBLE
                    viewHolder.view.ivUser.visibility = View.VISIBLE
                } else {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.INVISIBLE
                }
                setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                viewHolder.view.tvUser.text = baseMessage.sender.name
            }
            viewHolder.view.goImgMessage.setImageDrawable(context!!.resources.getDrawable(R.drawable.ic_defaulf_image))
            var thumbnailUrl = Extensions.getThumbnailGeneration(context, baseMessage)
            if (thumbnailUrl != null)
                Glide.with(context).load(thumbnailUrl).into(viewHolder.view.goImgMessage)
            else {
                if ((baseMessage as MediaMessage).attachment != null)
                    Glide.with(context).load(baseMessage.attachment.fileUrl).into(viewHolder.view.goImgMessage)}
            showMessageTime(viewHolder, baseMessage)
//            if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.view.txtTime.visibility = View.VISIBLE
//            else viewHolder.view.txtTime.visibility = View.GONE
            //

            if (baseMessage.getReplyCount() != 0) {
                viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
            } else {
                viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                viewHolder.view.threadReplyCount.setVisibility(View.GONE)
            }
            viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                } else {
                    if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                }
                context.startActivity(intent)
            })
            viewHolder.view.cvImageMessageContainer.setOnClickListener { view: View? ->
                displayImage(baseMessage)
                setSelectedMessage(baseMessage.getId())
                notifyDataSetChanged()
            }
            viewHolder.view.cvImageMessageContainer.setOnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            }
        } else {
            viewHolder = view as RightImageMessageViewHolder
            viewHolder.view.goImgMessage.setImageDrawable(context.resources.getDrawable(R.drawable.ic_defaulf_image))
            var thumbnailUrl = Extensions.getThumbnailGeneration(context, baseMessage)
            if (thumbnailUrl != null)
                Glide.with(context).load(thumbnailUrl).into(viewHolder.view.goImgMessage)
            else if ((baseMessage as MediaMessage).attachment != null) Glide.with(context).load(baseMessage.attachment.fileUrl).into(viewHolder.view.goImgMessage)
            showMessageTime(viewHolder, baseMessage)
//            if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.view.txtTime.visibility = View.VISIBLE
//            else viewHolder.view.txtTime.visibility = View.GONE
            //
            if (baseMessage.getReplyCount() != 0) {
                viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
            } else {
                viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                viewHolder.view.threadReplyCount.setVisibility(View.GONE)
            }
            viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                } else {
                    if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                }
                context.startActivity(intent)
            })
            viewHolder.view.cvImageMessageContainer.setOnClickListener { view: View? ->
                displayImage(baseMessage)
                setSelectedMessage(baseMessage.getId())
                notifyDataSetChanged()
            }
            viewHolder.view.cvImageMessageContainer.setOnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            }
        }
    }

    private fun displayImage(baseMessage: BaseMessage) {
        val imageDialog = Dialog(context)
        val messageVw = LayoutInflater.from(context).inflate(R.layout.image_dialog_view, null)
        val imageView: ZoomIv = messageVw.findViewById(R.id.imageView)
        Glide.with(context).asBitmap().load((baseMessage as MediaMessage).attachment.fileUrl).into(object : SimpleTarget<Bitmap?>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                imageView.setImageBitmap(resource)
            }
        })
        imageDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageDialog.setContentView(messageVw)
        imageDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        imageDialog.show()
    }

    private fun setVideoData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        var viewHolder : RecyclerView.ViewHolder
        if (view is LeftVideoMessageViewHolder) {
            viewHolder = view as LeftVideoMessageViewHolder
            if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                viewHolder.view.tvUser.visibility = View.GONE
                viewHolder.view.ivUser.visibility = View.GONE
            } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (isUserDetailVisible) {
                    viewHolder.view.tvUser.visibility = View.VISIBLE
                    viewHolder.view.ivUser.visibility = View.VISIBLE
                } else {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.INVISIBLE
                }
                setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                viewHolder.view.tvUser.text = baseMessage.sender.name
            }
            if ((baseMessage as MediaMessage).attachment != null) Glide.with(context).load(baseMessage.attachment.fileUrl).into(viewHolder.view.goVideoMessage)
            showMessageTime(viewHolder, baseMessage)
//            if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.view.txtTime.visibility = View.VISIBLE
//            else viewHolder.view.txtTime.visibility = View.GONE
            //
            if (baseMessage.getReplyCount() != 0) {
                viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
            } else {
                viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                viewHolder.view.threadReplyCount.setVisibility(View.GONE)
            }
            viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, baseMessage.attachment.fileName)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                } else {
                    if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                }
                context.startActivity(intent)
            })
            viewHolder.view.cvImageMessageContainer.setOnClickListener { view: View? ->
                setSelectedMessage(baseMessage.getId())
                notifyDataSetChanged()
            }
            viewHolder.view.cvImageMessageContainer.setOnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            }
            viewHolder.view.playBtn.setOnClickListener { MediaUtils.openFile(baseMessage.attachment.fileUrl, context) }
        } else {
            viewHolder = view as RightVideoMessageViewHolder
            if ((baseMessage as MediaMessage).attachment != null) Glide.with(context).load(baseMessage.attachment.fileUrl).into(viewHolder.view.goVideoMessage)
            showMessageTime(viewHolder, baseMessage)
//            if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.view.txtTime.visibility = View.VISIBLE
//            else viewHolder.view.txtTime.visibility = View.GONE
            //
            if (baseMessage.getReplyCount() != 0) {
                viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
            } else {
                viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                viewHolder.view.threadReplyCount.setVisibility(View.GONE)
            }
            viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, baseMessage.attachment.fileName)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                } else {
                    if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                }
                context.startActivity(intent)
            })
            viewHolder.view.cvImageMessageContainer.setOnClickListener { view: View? ->
                setSelectedMessage(baseMessage.getId())
                notifyDataSetChanged()
            }
            viewHolder.view.cvImageMessageContainer.setOnLongClickListener {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true
                    setLongClickSelectedItem(baseMessage)
                    messageLongClick!!.setLongMessageClick(longselectedItemList)
                    notifyDataSetChanged()
                }
                true
            }
            viewHolder.view.playBtn.setOnClickListener { MediaUtils.openFile(baseMessage.attachment.fileUrl, context) }
        }
    }

    private fun setDeleteData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        var viewHolder: RecyclerView.ViewHolder
        if (view is LeftDeleteMessageViewHolder) {
            viewHolder = view as LeftDeleteMessageViewHolder
            if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                viewHolder.view.tvUser.visibility = View.GONE
                viewHolder.view.ivUser.visibility = View.GONE
            } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                if (isUserDetailVisible) {
                    viewHolder.view.tvUser.visibility = View.VISIBLE
                    viewHolder.view.ivUser.visibility = View.VISIBLE
                } else {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.INVISIBLE
                }
                setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                viewHolder.view.tvUser.text = baseMessage.sender.name
            }
            viewHolder.view.goTxtMessage.setTypeface(null, Typeface.ITALIC)

            showMessageTime(viewHolder, baseMessage)
            if (selectedItemList.contains(baseMessage.id)) viewHolder.view.txtTime.visibility = View.VISIBLE else viewHolder.view.txtTime.visibility = View.GONE
        } else {
            viewHolder = view as RightDeleteMessageViewHolder
            viewHolder.view.goTxtMessage.setTypeface(null, Typeface.ITALIC)
            showMessageTime(viewHolder, baseMessage)
            if (selectedItemList.contains(baseMessage.id)) viewHolder.view.txtTime.visibility = View.VISIBLE else viewHolder.view.txtTime.visibility = View.GONE
        }
    }

    /**
     * This method is called whenever viewType of item is Action. It is used to bind
     * ActionMessageViewHolder contents with Action at a given position. It shows action message
     * or call status based on message category
     *
     * @param viewHolder is a object of ActionMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see Action
     *
     * @see Call
     *
     * @see BaseMessage
     */
    private fun setActionData(view: ActionMessageViewHolder, i: Int) {
        val baseMessage = messageList[i]
        val viewHolder = view as ActionMessageViewHolder
        if (Utils.isDarkMode(context)) viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.textColorWhite)) else viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
        viewHolder.view.goTxtMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        if (baseMessage is Action) viewHolder.view.goTxtMessage.text = baseMessage.message else if (baseMessage is Call) {
            val call = baseMessage
            if (call.callStatus == CometChatConstants.CALL_STATUS_INITIATED) viewHolder.view.goTxtMessage.text = call.sender.name + " " + call.callStatus + " " + call.type + " " + context.resources.getString(R.string.call).toLowerCase() else viewHolder.view.goTxtMessage.text = context.resources.getString(R.string.call) + " " + baseMessage.callStatus
        }
    }

    /**
     * This method is used to show message time below message whenever we click on message.
     * Since we have different ViewHolder, we have to pass **txtTime** of each viewHolder to
     * `setStatusIcon(RecyclerView.ViewHolder viewHolder,BaseMessage baseMessage)`
     * along with baseMessage.
     * @see MessageAdapter.setStatusIcon
     * @param viewHolder is object of ViewHolder.
     * @param baseMessage is a object of BaseMessage.
     *
     * @see BaseMessage
     */
    private fun showMessageTime(viewHolder: RecyclerView.ViewHolder, baseMessage: BaseMessage) {
        if (viewHolder is LeftTextMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftLinkMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftImageMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftFileMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftAudioMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftVideoMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftLocationMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is LeftCustomMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        }


        else if (viewHolder is RightTextMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightLinkMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightImageMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightFileMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightAudioMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightVideoMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightLocationMessageViewHolder){
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        } else if (viewHolder is RightCustomMessageViewHolder) {
            setStatusIcon(viewHolder.view.txtTime, baseMessage)
        }
    }

    /**
     * This method is used set message time i.e sentAt, deliveredAt & readAt in **txtTime**.
     * If sender of baseMessage is user then for user side messages it will show readAt, deliveredAt
     * time along with respective icon. For reciever side message it will show only deliveredAt time
     *
     * @param txtTime is a object of TextView which will show time.
     * @param baseMessage is a object of BaseMessage used to identify baseMessage sender.
     * @see BaseMessage
     */
    private fun setStatusIcon(txtTime: TextView, baseMessage: BaseMessage) {
        if (baseMessage.sender.uid == loggedInUser.uid) {
            if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                if (baseMessage.readAt != 0L) {
                    txtTime.text = Utils.getHeaderDate(baseMessage.readAt * 1000)
                    txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_double_tick, 0)
                    txtTime.compoundDrawablePadding = 10
                } else if (baseMessage.deliveredAt != 0L) {
                    txtTime.text = Utils.getHeaderDate(baseMessage.deliveredAt * 1000)
                    txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_black_24dp, 0)
                    txtTime.compoundDrawablePadding = 10
                } else {
                    txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
                    txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0)
                    txtTime.compoundDrawablePadding = 10
                }
            } else {
                txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
            }
        }
        else {
            txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
        }
    }

    /**
     * This method is called whenever viewType of item is TextMessage. It is used to bind
     * TextMessageViewHolder content with TextMessage at given position.
     * It shows text of a message if deletedAt = 0 else it shows "message deleted"
     *
     * @param viewHolder is a object of TextMessageViewHolder.
     * @param i is postion of item in recyclerView.
     * @see TextMessage
     *
     * @see BaseMessage
     */
    private fun setTextData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null) {
            var viewHolder: RecyclerView.ViewHolder
            if (view is LeftTextMessageViewHolder) {
                val viewHolder = view as LeftTextMessageViewHolder
                viewHolder.view.textMessage = baseMessage as TextMessage
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.text = baseMessage.sender.name
                }

                viewHolder.view.goTxtMessage.text = (baseMessage as TextMessage).text.trim { it <= ' ' }
                viewHolder.view.goTxtMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoRegular)
                if (baseMessage.getSender().uid == loggedInUser.uid) viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.textColorWhite)) else viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
                showMessageTime(viewHolder, baseMessage)
//                if (messageList[messageList.size - 1] == baseMessage) {
//                    selectedItemList.add(baseMessage.getId())
//                }
//                if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else
//                    viewHolder.view.txtTime.visibility = View.GONE
                setColorFilter(baseMessage, viewHolder.view.cvMessageContainer)
                viewHolder.view.rlMessage.setOnClickListener { view: View? ->
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                    } else {
                        setSelectedMessage(baseMessage.getId())
                    }
                    notifyDataSetChanged()
                }
                viewHolder.view.rlMessage.setOnLongClickListener {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true
                        isTextMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)

                if (baseMessage.getMetadata() != null && baseMessage.getMetadata().has("reply")) {
                    try {
                        val metaData = baseMessage.getMetadata().getJSONObject("reply")
                        val messageType = metaData.getString("type")
                        val message = metaData.getString("message")
                        viewHolder.view.replyItem.replyLayout.setVisibility(View.VISIBLE)
                        viewHolder.view.replyItem.replyUser.setText(metaData.getString("name"))
                        if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                            viewHolder.view.replyItem.replyMessage.setText(message)
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                            viewHolder.view.replyItem.replyMessage.setText(context.resources.getString(R.string.shared_a_image))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_a_audio), ""))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_music_24dp, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO) {
                            viewHolder.view.replyItem.replyMessage.setText(context.resources.getString(R.string.shared_a_video))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam_24dp, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_FILE) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_a_file), ""))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_drive_file_black_24dp, 0, 0, 0)
                        } else if (messageType == StringContract.IntentStrings.LOCATION) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_location_address), "").trim { it <= ' ' })
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_near_me_24dp, 0, 0, 0)
                        } else if (messageType == StringContract.IntentStrings.STICKERS) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.getString(R.string.shared_a_sticker)))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.default_sticker, 0, 0, 0)
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "setTextData: " + e.message)
                    }
                }
                if (baseMessage.getReplyCount() != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                    intent.putExtra(StringContract.IntentStrings.TEXTMESSAGE, (baseMessage as TextMessage).text)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                val txtMessage = baseMessage.text.trim { it <= ' ' }
                viewHolder.view.goTxtMessage.textSize = 16f
                var count = 0
                val processed = EmojiCompat.get().process(txtMessage, 0,
                        txtMessage.length - 1, Int.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
                if (processed is Spannable) {
                    val spannable = processed
                    count = spannable.getSpans(0, spannable.length - 1, EmojiSpan::class.java).size
                    if (Utils.removeEmojiAndSymbol(txtMessage)!!.trim().length === 0) {
                        if (count == 1) {
                            viewHolder.view.goTxtMessage.textSize = Utils.dpToPx(context, 32f)
                        } else if (count == 2) {
                            viewHolder.view.goTxtMessage.textSize = Utils.dpToPx(context, 24f)
                        }
                    }
                }
            } else {
                viewHolder = view as RightTextMessageViewHolder
                viewHolder.view.textMessage = baseMessage as TextMessage
                viewHolder.view.goTxtMessage.text = (baseMessage as TextMessage).text.trim { it <= ' ' }
                viewHolder.view.goTxtMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoRegular)
                if (baseMessage.getSender().uid == loggedInUser.uid) viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.textColorWhite)) else viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
                showMessageTime(viewHolder, baseMessage)
//                if (messageList[messageList.size - 1] == baseMessage) {
//                    selectedItemList.add(baseMessage.getId())
//                }
//                if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                setColorFilter(baseMessage, viewHolder.view.cvMessageContainer)
                viewHolder.view.rlMessage.setOnClickListener { view: View? ->
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                    } else {
                        setSelectedMessage(baseMessage.getId())
                    }
                    notifyDataSetChanged()
                }
                viewHolder.view.rlMessage.setOnLongClickListener {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true
                        isTextMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)

                if (baseMessage.getMetadata() != null && baseMessage.getMetadata().has("reply")) {
                    try {
                        val metaData = baseMessage.getMetadata().getJSONObject("reply")
                        val messageType = metaData.getString("type")
                        val message = metaData.getString("message")
                        viewHolder.view.replyItem.replyLayout.setVisibility(View.VISIBLE)
                        viewHolder.view.replyItem.replyUser.setText(metaData.getString("name"))
                        if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                            viewHolder.view.replyItem.replyMessage.setText(message)
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                            viewHolder.view.replyItem.replyMessage.setText(context.resources.getString(R.string.shared_a_image))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_a_audio), ""))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_music_24dp, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO) {
                            viewHolder.view.replyItem.replyMessage.setText(context.resources.getString(R.string.shared_a_video))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam_24dp, 0, 0, 0)
                        } else if (messageType == CometChatConstants.MESSAGE_TYPE_FILE) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_a_file), ""))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_drive_file_black_24dp, 0, 0, 0)
                        } else if (messageType == StringContract.IntentStrings.LOCATION) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.resources.getString(R.string.shared_location_address), "").trim { it <= ' ' })
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_near_me_24dp, 0, 0, 0)
                        } else if (messageType == StringContract.IntentStrings.STICKERS) {
                            viewHolder.view.replyItem.replyMessage.setText(String.format(context.getString(R.string.shared_a_sticker)))
                            viewHolder.view.replyItem.replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.default_sticker, 0, 0, 0)
                        }

                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "setTextData: " + e.message)
                    }
                }
                if (baseMessage.getReplyCount() != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.getReplyCount().toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.getSender().avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.getReplyCount())
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.getId())
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType())
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt())
                    intent.putExtra(StringContract.IntentStrings.TEXTMESSAGE, baseMessage.text)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.getCategory())
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getReceiverType())
                    if (baseMessage.getReceiverType() == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.getReceiverUid())
                    } else {
                        if (baseMessage.getReceiverUid() == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getSender().uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.getReceiverUid())
                    }
                    context.startActivity(intent)
                })
                val txtMessage = baseMessage.text.trim { it <= ' ' }
                viewHolder.view.goTxtMessage.textSize = 16f
                var count = 0
                val processed = EmojiCompat.get().process(txtMessage, 0,
                        txtMessage.length - 1, Int.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
                if (processed is Spannable) {
                    val spannable = processed
                    count = spannable.getSpans(0, spannable.length - 1, EmojiSpan::class.java).size
                    if (Utils.removeEmojiAndSymbol(txtMessage)!!.trim().length === 0) {
                        if (count == 1) {
                            viewHolder.view.goTxtMessage.textSize = Utils.dpToPx(context, 32f)
                        } else if (count == 2) {
                            viewHolder.view.goTxtMessage.textSize = Utils.dpToPx(context, 24f)
                        }
                    }
                }
            }
        }
    }

    private fun setCustomData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        if (baseMessage != null) {
            var viewHolder: RecyclerView.ViewHolder
            if (view is LeftCustomMessageViewHolder) {
                viewHolder = view as LeftCustomMessageViewHolder
                viewHolder.view.customMessage = baseMessage as CustomMessage
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.text = baseMessage.sender.name
                }

                viewHolder.view.goTxtMessage.text = context.resources.getString(R.string.custom_message)
                viewHolder.view.goTxtMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoLight)
                if (baseMessage.sender.uid == loggedInUser.uid) viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.textColorWhite)) else viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
                showMessageTime(viewHolder, baseMessage)
//                if (messageList[messageList.size - 1] == baseMessage) {
//                    selectedItemList.add(baseMessage.id)
//                }
//                if (selectedItemList.contains(baseMessage.id))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                viewHolder.view.rlMessage.setOnClickListener { view: View? ->
                    setSelectedMessage(baseMessage.id)
                    notifyDataSetChanged()
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)
            } else {
                viewHolder = view as RightCustomMessageViewHolder
                viewHolder.view.customMessage = baseMessage as CustomMessage
                viewHolder.view.goTxtMessage.text = context.resources.getString(R.string.custom_message)
                viewHolder.view.goTxtMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoLight)
                if (baseMessage.sender.uid == loggedInUser.uid) viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.textColorWhite)) else viewHolder.view.goTxtMessage.setTextColor(context.resources.getColor(R.color.primaryTextColor))
                showMessageTime(viewHolder, baseMessage)
                if (messageList[messageList.size - 1] == baseMessage) {
                    selectedItemList.add(baseMessage.id)
                }
                if (selectedItemList.contains(baseMessage.id)) viewHolder.view.txtTime.visibility = View.VISIBLE else viewHolder.view.txtTime.visibility = View.GONE
                viewHolder.view.rlMessage.setOnClickListener { view: View? ->
                    setSelectedMessage(baseMessage.id)
                    notifyDataSetChanged()
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)
            }
        }
    }

    private fun setColorFilter(baseMessage: BaseMessage, view: View) {
        if (!longselectedItemList.contains(baseMessage)) {
            if (baseMessage.sender == CometChat.getLoggedInUser()) view.background.setColorFilter(context.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP) else view.background.setColorFilter(context.resources.getColor(R.color.message_bubble_grey), PorterDuff.Mode.SRC_ATOP)
        } else {
            if (baseMessage.sender == CometChat.getLoggedInUser()) view.background.setColorFilter(context.resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP) else view.background.setColorFilter(context.resources.getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun setLinkData(view: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageList[i]
        var url: String? = null
        if (baseMessage != null) {
            var viewHolder: RecyclerView.ViewHolder
            if (view is LeftLinkMessageViewHolder) {
                viewHolder = view as LeftLinkMessageViewHolder
                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    viewHolder.view.tvUser.visibility = View.GONE
                    viewHolder.view.ivUser.visibility = View.GONE
                } else if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    if (isUserDetailVisible) {
                        viewHolder.view.tvUser.visibility = View.VISIBLE
                        viewHolder.view.ivUser.visibility = View.VISIBLE
                    } else {
                        viewHolder.view.tvUser.visibility = View.GONE
                        viewHolder.view.ivUser.visibility = View.INVISIBLE
                    }
                    setAvatar(viewHolder.view.ivUser, baseMessage.sender.avatar, baseMessage.sender.name)
                    viewHolder.view.tvUser.text = baseMessage.sender.name
                }

                if (baseMessage.deletedAt == 0L) {
                    val extensionList = Extensions.extensionCheck(baseMessage)
                    if (extensionList != null) {
                        if (extensionList.containsKey("linkPreview")) {
                            val linkPreviewJsonObject = extensionList["linkPreview"]
                            try {
                                val description = linkPreviewJsonObject!!.getString("description")
                                val image = linkPreviewJsonObject.getString("image")
                                val title = linkPreviewJsonObject.getString("title")
                                url = linkPreviewJsonObject.getString("url")
                                Log.e("setLinkData: ", baseMessage.toString() + "\n\n" + url + "\n" + description + "\n" + image)
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
                                if (baseMessage.text == url || baseMessage.text == "$url/") {
                                    viewHolder.view.message.visibility = View.GONE
                                } else {
                                    viewHolder.view.message.visibility = View.VISIBLE
                                }
                                viewHolder.view.message.text = messageStr
                            } catch (e: Exception) {
                                Log.e("setLinkData: ", e.message)
                            }
                        }
                    }
                }
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT) intent.putExtra(StringContract.IntentStrings.TEXTMESSAGE, (baseMessage as TextMessage).text) else {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    }
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                })

                showMessageTime(viewHolder, baseMessage)
                val finalUrl = url
                viewHolder.view.visitLink.setOnClickListener {
                    if (finalUrl != null) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(finalUrl)
                        context.startActivity(intent)
                    }
                }
//                if (selectedItemList.contains(baseMessage.id))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                //            if (i < selectedItems.length && selectedItems[i] == 0) {
//                viewHolder.view.txtTime.setVisibility(View.GONE);
//            } else
//                viewHolder.view.txtTime.setVisibility(View.VISIBLE);
                viewHolder.view.cvLinkMessageContainer.setOnClickListener { view: View? ->
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                    } else {
                        setSelectedMessage(baseMessage.id)
                    }
                    notifyDataSetChanged()
                }
                viewHolder.view.cvLinkMessageContainer.setOnLongClickListener {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true
                        isTextMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)
            } else {
                viewHolder = view as RightLinkMessageViewHolder
                if (baseMessage.deletedAt == 0L) {
                    val extensionList = Extensions.extensionCheck(baseMessage)
                    if (extensionList != null) {
                        if (extensionList.containsKey("linkPreview")) {
                            val linkPreviewJsonObject = extensionList["linkPreview"]
                            try {
                                val description = linkPreviewJsonObject!!.getString("description")
                                val image = linkPreviewJsonObject.getString("image")
                                val title = linkPreviewJsonObject.getString("title")
                                url = linkPreviewJsonObject.getString("url")
                                Log.e("setLinkData: ", baseMessage.toString() + "\n\n" + url + "\n" + description + "\n" + image)
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
                                if (baseMessage.text == url || baseMessage.text == "$url/") {
                                    viewHolder.view.message.visibility = View.GONE
                                } else {
                                    viewHolder.view.message.visibility = View.VISIBLE
                                }
                                viewHolder.view.message.text = messageStr
                            } catch (e: Exception) {
                                Log.e("setLinkData: ", e.message)
                            }
                        }
                    }
                }
                if (baseMessage.replyCount != 0) {
                    viewHolder.view.threadReplyCount.setVisibility(View.VISIBLE)
                    viewHolder.view.threadReplyCount.setText(baseMessage.replyCount.toString() + " Replies")
                } else {
                    viewHolder.view.replyAvatarLayout.setVisibility(View.GONE)
                    viewHolder.view.threadReplyCount.setVisibility(View.GONE)
                }
                viewHolder.view.threadReplyCount.setOnClickListener(View.OnClickListener { view: View? ->
                    val intent = Intent(context, CometChatThreadMessageActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                    intent.putExtra(StringContract.IntentStrings.NAME, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, baseMessage.sender.avatar)
                    intent.putExtra(StringContract.IntentStrings.REPLY_COUNT, baseMessage.replyCount)
                    intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.name)
                    intent.putExtra(StringContract.IntentStrings.PARENT_ID, baseMessage.id)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.type)
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, baseMessage.category)
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.sentAt)
                    if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT) intent.putExtra(StringContract.IntentStrings.TEXTMESSAGE, (baseMessage as TextMessage).text) else {
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, baseMessage.attachment.fileExtension)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, baseMessage.attachment.fileUrl)
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, baseMessage.attachment.fileSize)
                    }
                    intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.receiverType)
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                        intent.putExtra(StringContract.IntentStrings.GUID, baseMessage.receiverUid)
                    } else {
                        if (baseMessage.receiverUid == loggedInUser.uid) intent.putExtra(StringContract.IntentStrings.UID, baseMessage.sender.uid) else intent.putExtra(StringContract.IntentStrings.UID, baseMessage.receiverUid)
                    }
                    context.startActivity(intent)
                })
                showMessageTime(viewHolder, baseMessage)
                val finalUrl = url
                viewHolder.view.visitLink.setOnClickListener {
                    if (finalUrl != null) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(finalUrl)
                        context.startActivity(intent)
                    }
                }
//                if (selectedItemList.contains(baseMessage.id))
                viewHolder.view.txtTime.visibility = View.VISIBLE
//                else viewHolder.view.txtTime.visibility = View.GONE
                //            if (i < selectedItems.length && selectedItems[i] == 0) {
//                viewHolder.view.txtTime.setVisibility(View.GONE);
//            } else
//                viewHolder.view.txtTime.setVisibility(View.VISIBLE);
                viewHolder.view.cvLinkMessageContainer.setOnClickListener { view: View? ->
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                    } else {
                        setSelectedMessage(baseMessage.id)
                    }
                    notifyDataSetChanged()
                }
                viewHolder.view.cvLinkMessageContainer.setOnLongClickListener {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true
                        isTextMessageClick = true
                        setLongClickSelectedItem(baseMessage)
                        messageLongClick!!.setLongMessageClick(longselectedItemList)
                        notifyDataSetChanged()
                    }
                    true
                }
                viewHolder.view.root.setTag(R.string.message, baseMessage)
            }
        }
    }

    fun setSelectedMessage(id: Int) {
        if (selectedItemList.contains(id)) selectedItemList.remove(id) else selectedItemList.add(id)
    }

    fun setLongClickSelectedItem(baseMessage: BaseMessage) {
        if (longselectedItemList.contains(baseMessage)) longselectedItemList.remove(baseMessage) else longselectedItemList.add(baseMessage)
    }

    /**
     * This method is used to set avatar of groupMembers to show in groupMessages. If avatar of
     * group member is not available then it calls `setInitials(String name)` to show
     * first two letter of group member name.
     *
     * @param avatar is a object of Avatar
     * @param avatarUrl is a String. It is url of avatar.
     * @param name is a String. It is a name of groupMember.
     * @see Avatar
     */
    private fun setAvatar(avatar: Avatar, avatarUrl: String?, name: String) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) Glide.with(context).load(avatarUrl).into(avatar) else avatar.setInitials(name)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: " + messageList.size)
        return messageList.size
    }

    override fun getHeaderId(var1: Int): Long {
        val baseMessage = messageList[var1]
        return Utils.getDateId(baseMessage.sentAt * 1000)!!.toLong()
    }

    override fun onCreateHeaderViewHolder(var1: ViewGroup?): DateItemHolder? {
        val view = LayoutInflater.from(var1!!.context).inflate(R.layout.cc_message_list_header,
                var1, false)
        return DateItemHolder(view)
    }

    override fun onBindHeaderViewHolder(var1: Any, var2: Int, var3: Long) {
        val baseMessage = messageList[var2]
        val date = Date(baseMessage.sentAt * 1000L)
        val formattedDate = Utils.getDate(date.time)
        var dateItemHolder = var1 as DateItemHolder;
        dateItemHolder.txtMessageDate.background = context.resources.getDrawable(R.drawable.cc_rounded_date_button)
        dateItemHolder.txtMessageDate.text = formattedDate
    }

    /**
     * This method is used to maintain different viewType based on message category and type and
     * returns the different view types to adapter based on it.
     *
     * Ex :- For message with category **CometChatConstants.CATEGORY_MESSAGE** and type
     * **CometChatConstants.MESSAGE_TYPE_TEXT** and message is sent by a **Logged-in user**,
     * It will return **RIGHT_TEXT_MESSAGE**
     *
     *
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     *
     * @see MessageAdapter.onCreateViewHolder
     * @see BaseMessage
     */
    private fun getItemViewTypes(position: Int): Int {
        val baseMessage = messageList[position]
        val extensionList = Extensions.extensionCheck(baseMessage)
        if (baseMessage.deletedAt == 0L) {
            if (baseMessage.category == CometChatConstants.CATEGORY_MESSAGE) {
                return when (baseMessage.type) {
                    CometChatConstants.MESSAGE_TYPE_TEXT -> if (baseMessage.sender.uid == loggedInUser.uid) {
                        if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList["linkPreview"] != null) RIGHT_LINK_MESSAGE
                        else if (baseMessage.metadata != null && baseMessage.metadata.has("reply"))
                            return RIGHT_REPLY_TEXT_MESSAGE
                        else RIGHT_TEXT_MESSAGE

                    } else {
                        if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList["linkPreview"] != null) LEFT_LINK_MESSAGE
                        else if (baseMessage.metadata != null && baseMessage.metadata.has("reply"))
                            return LEFT_REPLY_TEXT_MESSAGE
                        else LEFT_TEXT_MESSAGE
                    }
                    CometChatConstants.MESSAGE_TYPE_AUDIO -> if (baseMessage.sender.uid == loggedInUser.uid) {
                        RIGHT_AUDIO_MESSAGE
                    } else {
                        LEFT_AUDIO_MESSAGE
                    }
                    CometChatConstants.MESSAGE_TYPE_IMAGE -> if (baseMessage.sender.uid == loggedInUser.uid) {
                        RIGHT_IMAGE_MESSAGE
                    } else {
                        LEFT_IMAGE_MESSAGE
                    }
                    CometChatConstants.MESSAGE_TYPE_VIDEO -> if (baseMessage.sender.uid == loggedInUser.uid) {
                        RIGHT_VIDEO_MESSAGE
                    } else {
                        LEFT_VIDEO_MESSAGE
                    }
                    CometChatConstants.MESSAGE_TYPE_FILE -> if (baseMessage.sender.uid == loggedInUser.uid) {
                        RIGHT_FILE_MESSAGE
                    } else {
                        LEFT_FILE_MESSAGE
                    }
                    else -> -1
                }
            } else {
                if (baseMessage.category == CometChatConstants.CATEGORY_ACTION) {
                    return ACTION_MESSAGE
                } else if (baseMessage.category == CometChatConstants.CATEGORY_CALL) {
                    return CALL_MESSAGE
                } else if (baseMessage.category == CometChatConstants.CATEGORY_CUSTOM) {
                    if (baseMessage.sender.uid == loggedInUser.uid) {
                        return if (baseMessage.type.equals(StringContract.IntentStrings.LOCATION, ignoreCase = true))
                            RIGHT_LOCATION_CUSTOM_MESSAGE
                        else if (baseMessage.type.equals(StringContract.IntentStrings.STICKERS, ignoreCase = true))
                            RIGHT_STICKER_MESSAGE
                        else
                            RIGHT_CUSTOM_MESSAGE
                    }
                    else {
                        return if (baseMessage.type.equals(StringContract.IntentStrings.LOCATION, ignoreCase = true))
                            LEFT_LOCATION_CUSTOM_MESSAGE
                        else if (baseMessage.type.equals(StringContract.IntentStrings.STICKERS, ignoreCase = true))
                            LEFT_STICKER_MESSAGE
                        else
                            LEFT_CUSTOM_MESSAGE
                    }
                }
            }
        } else {
            return if (baseMessage.sender.uid == loggedInUser.uid) {
                RIGHT_DELETE_MESSAGE
            } else {
                LEFT_DELETE_MESSAGE
            }
        }
        return -1
    }

    /**
     * This method is used to update message list of adapter.
     *
     * @param baseMessageList is list of baseMessages.
     */
    fun updateList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    /**
     * This method is used to set real time delivery receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
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

    /**
     * This method is used to set real time read receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
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

    /**
     * This method is used to add message in messageList when send by a user or when received in
     * real time.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will added.
     * @see BaseMessage
     */
    fun addMessage(baseMessage: BaseMessage) { //        if (!messageList.contains(baseMessage)) {
        messageList.add(baseMessage)
        selectedItemList.clear()
        //        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to update previous message with new message in messageList of adapter.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will be updated.
     */
    fun setUpdatedMessage(baseMessage: BaseMessage) {
        if (messageList.contains(baseMessage)) {
            val index = messageList.indexOf(baseMessage)
            messageList.remove(baseMessage)
            messageList.add(index, baseMessage)
            notifyItemChanged(index)
        }
    }

    fun resetList() {
        messageList.clear()
        notifyDataSetChanged()
    }

    fun clearLongClickSelectedItem() {
        isLongClickEnabled = false
        isTextMessageClick = false
        isImageMessageClick = false
        longselectedItemList.clear()
        notifyDataSetChanged()
    }

    val lastMessage: BaseMessage?
        get() = if (messageList.size > 0) {
            Log.e(TAG, "getLastMessage: " + messageList[messageList.size - 1])
            messageList[messageList.size - 1]
        } else null

    fun getPosition(baseMessage: BaseMessage?): Int {
        return messageList.indexOf(baseMessage)
    }


    //delete
    inner class LeftDeleteMessageViewHolder(val view: LeftDeleteMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightDeleteMessageViewHolder(val view: RightDeleteMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //text
    inner class LeftTextMessageViewHolder(val view: LeftMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightTextMessageViewHolder(val view: RightMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //
    inner class LeftCustomMessageViewHolder(val view: LeftCustomMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightCustomMessageViewHolder(val view: RightCustomMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //image
    inner class LeftImageMessageViewHolder(val view: MessageLeftListImageItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightImageMessageViewHolder(val view: MessageRightListImageItemBinding) : RecyclerView.ViewHolder(view.root)

    //video
    inner class LeftVideoMessageViewHolder(val view: MessageLeftListVideoItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightVideoMessageViewHolder(val view: MessageRightListVideoItemBinding) : RecyclerView.ViewHolder(view.root)

    //file
    inner class LeftFileMessageViewHolder(val view: CometchatLeftFileMessageBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightFileMessageViewHolder(val view: CometchatRightFileMessageBinding) : RecyclerView.ViewHolder(view.root)

    //audio
    inner class LeftAudioMessageViewHolder(val view: CometchatAudioLayoutLeftBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightAudioMessageViewHolder(val view: CometchatAudioLayoutRightBinding) : RecyclerView.ViewHolder(view.root)

    //link
    inner class LeftLinkMessageViewHolder(val view: MessageLeftLinkItemBinding) : RecyclerView.ViewHolder(view.root)

    inner class RightLinkMessageViewHolder(val view: MessageRightLinkItemBinding) : RecyclerView.ViewHolder(view.root)

    //action
    inner class ActionMessageViewHolder(val view: CometchatActionMessageBinding) : RecyclerView.ViewHolder(view.root)

    //location
    inner class LeftLocationMessageViewHolder(val view: LeftLocationMessageItemBinding): RecyclerView.ViewHolder(view.root)

    inner class RightLocationMessageViewHolder(val view: RightLocationMessageItemBinding) : RecyclerView.ViewHolder(view.root)

    //sticker
    inner class LeftStickerMessageViewHolder(val view: MessageLeftStickerItemBinding): RecyclerView.ViewHolder(view.root)

    inner class RightStickerMessageViewHolder(val view: MessageRightStickerItemBinding): RecyclerView.ViewHolder(view.root)

//    inner class CustomMessageViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
//        val txtMessage: TextView
//        private val cardView: RelativeLayout
//        private val view: View
//        var txtTime: TextView
//        var tvUser: TextView
//        private val imgStatus: ImageView
//        private val type: Int
//        val ivUser: Avatar
//        val rlMessageBubble: RelativeLayout
//
//        init {
//            type = view.tag as Int
//            tvUser = view.findViewById(R.id.tv_user)
//            txtMessage = view.findViewById(R.id.go_txt_message)
//            cardView = view.findViewById(R.id.cv_message_container)
//            txtTime = view.findViewById(R.id.txt_time)
//            imgStatus = view.findViewById(R.id.img_pending)
//            ivUser = view.findViewById(R.id.iv_user)
//            rlMessageBubble = view.findViewById(R.id.rl_message)
//            this.view = view
//        }
//    }

//    inner class AudioMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        //        private TextView audioLength;
////
////        private TextView tvUser;
////
////        private Avatar ivUser;
////
////        private RelativeLayout rlMessageBubble;
////
////        private SeekBar audioSeekBar;
////
////        private ImageView downloadBtn;
////
////        private ImageView playBtn;
////
////        private ProgressBar downloadProgress;
////
////        private TextView txtTime;
////        private int type;
////
////        public AudioMessageViewHolder(@NonNull View itemView) {
////            super(itemView);
////
////            type = (int)itemView.getTag();
////            audioLength = itemView.findViewById(R.id.audioLength);
////            rlMessageBubble = itemView.findViewById(R.id.cv_message_container);
////            tvUser = itemView.findViewById(R.id.tv_user);
////            ivUser = itemView.findViewById(R.id.iv_user);
////            txtTime = itemView.findViewById(R.id.txt_time);
////            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
////            playBtn = itemView.findViewById(R.id.playBtn);
////            downloadBtn = itemView.findViewById(R.id.download);
////            downloadProgress = itemView.findViewById(R.id.progressBar);
////        }
//        var length: TextView
//        var playBtn: ImageView
//        var type: Int
//        var tvUser: TextView
//        var ivUser: Avatar
//        var rlMessageBubble: RelativeLayout
//        var txtTime: TextView
//
//        init {
//            type = itemView.tag as Int
//            length = itemView.findViewById(R.id.audiolength_tv)
//            playBtn = itemView.findViewById(R.id.playBtn)
//            rlMessageBubble = itemView.findViewById(R.id.cv_message_container)
//            tvUser = itemView.findViewById(R.id.tv_user)
//            ivUser = itemView.findViewById(R.id.iv_user)
//            txtTime = itemView.findViewById(R.id.txt_time)
//        }
//    }

//    inner class LinkMessageViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
//        val linkTitle: TextView
//        val linkVisit: TextView
//        val linkSubtitle: TextView
//        val message: TextView
//        val videoLink: ImageView
//        private val cardView: RelativeLayout
//        private val view: View
//        var txtTime: TextView
//        private val imgStatus: ImageView
//        val linkImg: ImageView
//        private val type: Int
//        val tvUser: TextView
//        val ivUser: Avatar
//        val rlMessageBubble: RelativeLayout
//
//        init {
//            type = view.tag as Int
//            tvUser = view.findViewById(R.id.tv_user)
//            linkTitle = view.findViewById(R.id.link_title)
//            linkSubtitle = view.findViewById(R.id.link_subtitle)
//            linkVisit = view.findViewById(R.id.visitLink)
//            linkImg = view.findViewById(R.id.link_img)
//            message = view.findViewById(R.id.message)
//            videoLink = view.findViewById(R.id.videoLink)
//            cardView = view.findViewById(R.id.cv_message_container)
//            txtTime = view.findViewById(R.id.txt_time)
//            imgStatus = view.findViewById(R.id.img_pending)
//            ivUser = view.findViewById(R.id.iv_user)
//            rlMessageBubble = view.findViewById(R.id.rl_message)
//            this.view = view
//        }
//    }

    inner class DateItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtMessageDate: TextView

        init {
            txtMessageDate = itemView.findViewById(R.id.txt_message_date)
        }
    }

    interface OnMessageLongClick {
        fun setLongMessageClick(baseMessage: List<BaseMessage>?)
    }

}