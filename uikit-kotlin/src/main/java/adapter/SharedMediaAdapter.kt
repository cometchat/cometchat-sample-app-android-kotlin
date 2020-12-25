package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.uikit.R
import utils.Extensions
import utils.FontUtils
import utils.MediaUtils
import java.util.*

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class SharedMediaAdapter(context: Context, messageArrayList: List<BaseMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context
    private val messageArrayList: MutableList<BaseMessage> = ArrayList()
    private var fontUtils: FontUtils

    companion object {
        private const val TAG = "SharedMediaAdapter"
        private const val SHARED_MEDIA_IMAGE = 1
        private const val SHARED_MEDIA_VIDEO = 2
        private const val SHARED_MEDIA_FILE = 3
    }

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context          is a object of Context.
     * @param messageArrayList is a list of messages used in this adapter.
     */
    init {
        setMessageList(messageArrayList)
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    private fun setMessageList(messageArrayList: List<BaseMessage>) {
        this.messageArrayList.addAll(messageArrayList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewTypes(position)
    }

    private fun getItemViewTypes(position: Int): Int {
        val baseMessage = messageArrayList[position]
        if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            return SHARED_MEDIA_IMAGE
        } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_VIDEO) {
            return SHARED_MEDIA_VIDEO
        } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_FILE) {
            return SHARED_MEDIA_FILE
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view: View
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (i) {
            SHARED_MEDIA_IMAGE -> {
                view = layoutInflater.inflate(R.layout.shared_media_image_row, parent, false)
                ImageViewHolder(view)
            }
            SHARED_MEDIA_VIDEO -> {
                view = layoutInflater.inflate(R.layout.shared_media_video_row, parent, false)
                VideoViewHolder(view)
            }
            SHARED_MEDIA_FILE -> {
                view = layoutInflater.inflate(R.layout.shared_media_file_row, parent, false)
                FileViewHolder(view)
            }
            else -> {
                view = layoutInflater.inflate(R.layout.shared_media_image_row, parent, false)
                FileViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val baseMessage = messageArrayList[i]
        if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            setImageData(viewHolder as ImageViewHolder, i)
        } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_FILE) {
            setFileData(viewHolder as FileViewHolder, i)
        } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_VIDEO){
            setVideoData(viewHolder as VideoViewHolder, i)
        }
    }

    private fun setVideoData(viewHolder: VideoViewHolder, i: Int) {
        val message = messageArrayList[i]
//        viewHolder.imageView.setOnClickListener { view: View? -> MediaUtils.openFile((message as MediaMessage).attachment.fileUrl, context) }
        Glide.with(context).load((message as MediaMessage).attachment.fileUrl).into(viewHolder.imageView)
        viewHolder.itemView.setTag(R.string.baseMessage, message)
    }

    private fun setFileData(viewHolder: FileViewHolder, i: Int) {
        val message = messageArrayList[i]
        viewHolder.fileName.text = (message as MediaMessage).attachment.fileName
        viewHolder.fileExtension.text = message.attachment.fileExtension
        viewHolder.itemView.setTag(R.string.baseMessage, message)
//        viewHolder.itemView.setOnClickListener { view: View? -> MediaUtils.openFile(message.attachment.fileUrl, context) }
    }

    private fun setImageData(viewHolder: ImageViewHolder, i: Int) {
        val message = messageArrayList[i]
        var thumbnailUrl = Extensions.getThumbnailGeneration(context, message)
        if (thumbnailUrl != null)
            Glide.with(context).load(thumbnailUrl).into(viewHolder.imageView)
        else {
            Glide.with(context).load((message as MediaMessage).attachment.fileUrl).into(viewHolder.imageView)
        }
        viewHolder.itemView.setTag(R.string.baseMessage, message)
    }

    override fun getItemCount(): Int {
        return messageArrayList.size
    }

    fun updateMessageList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    internal inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.imageView)
        }
    }

    internal inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.video_img)
        }
    }

    internal inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView
        val fileExtension: TextView

        init {
            fileName = itemView.findViewById(R.id.fileName_tv)
            fileExtension = itemView.findViewById(R.id.extension_tv)
        }
    }
}