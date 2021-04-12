package com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.core.MessagesRequest.MessagesRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.adapter.CometChatSharedMediaAdapter
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Purpose - It is a Fragment which is used in SharedMedia to display Images sent or received to a
 * particular receiver.
 *
 * Created On - 20th March 2020
 *
 * Modified On - 23rd March 2020
 *
 */
class CometChatSharedImages : Fragment() {
    private var rvFiles: RecyclerView? = null
    private var adapter: CometChatSharedMediaAdapter? = null
    private var Id: String? = null
    private var type: String? = null
    private val messageList: MutableList<BaseMessage> = ArrayList()
    private var noMedia: LinearLayout? = null
    private var messagesRequest: MessagesRequest? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cometchat_shared_media, container, false)
        rvFiles = view.findViewById(R.id.rvFiles)
        rvFiles!!.setLayoutManager(GridLayoutManager(context, 2))
        noMedia = view.findViewById(R.id.no_media_available)
        Id = this.arguments!!.getString("Id")
        type = this.arguments!!.getString("type")
        fetchMessage()
        rvFiles!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchMessage()
                }
            }
        })
        return view
    }

    /**
     * This method is used to fetch a messages whose type is MESSAGE_TYPE_IMAGE.
     * @see MessagesRequest.fetchNext
     */
    private fun fetchMessage() {
        if (messagesRequest == null) {
            messagesRequest = if (type != null && type == CometChatConstants.RECEIVER_TYPE_USER) MessagesRequestBuilder().setCategories(Arrays.asList(CometChatConstants.CATEGORY_MESSAGE)).setTypes(Arrays.asList(CometChatConstants.MESSAGE_TYPE_IMAGE)).setUID(Id!!).setLimit(30).build() else MessagesRequestBuilder().setCategories(Arrays.asList(CometChatConstants.CATEGORY_MESSAGE)).setTypes(Arrays.asList(CometChatConstants.MESSAGE_TYPE_IMAGE)).setGUID(Id!!).setLimit(30).build()
        }
        messagesRequest!!.fetchPrevious(object : CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessageList: List<BaseMessage>) {
                if (baseMessageList.size != 0) setImageAdapter(baseMessageList)
                checkMediaVisble()
            }

            override fun onError(e: CometChatException) {
                Log.e("onError: ", e.message)
                if (rvFiles != null) ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    /**
     * This method is used to check if the size of messages fetched is greater than 0 or not. If it
     * is 0 than it will show "No media Available" message.
     */
    private fun checkMediaVisble() {
        if (messageList.size != 0) {
            rvFiles!!.visibility = View.VISIBLE
            noMedia!!.visibility = View.GONE
        } else {
            noMedia!!.visibility = View.VISIBLE
            rvFiles!!.visibility = View.GONE
        }
    }

    /**
     * This method is used to setAdapter for Image messages.
     * @param baseMessageList is object of List<BaseMessage> which contains list of messages.
     * @see CometChatSharedMediaAdapter
    </BaseMessage> */
    private fun setImageAdapter(baseMessageList: List<BaseMessage>) {
        val filteredList = removeDeletedMessage(baseMessageList)
        messageList.addAll(filteredList)
        if (adapter == null) {
            adapter = CometChatSharedMediaAdapter(context!!, filteredList)
            rvFiles!!.adapter = adapter
        } else adapter!!.updateMessageList(filteredList)
    }

    /**
     * This method is used to remove deleted messages from message list and return filteredlist.
     * (baseMessage whose deletedAt !=0 must be removed from message list)
     *
     * @param baseMessageList is object of List<BaseMessage>
     * @return filteredMessageList which does not have deleted messages.
     * @see BaseMessage
    </BaseMessage> */
    private fun removeDeletedMessage(baseMessageList: List<BaseMessage>): List<BaseMessage> {
        val resultList: MutableList<BaseMessage> = ArrayList()
        for (baseMessage in baseMessageList) {
            if (baseMessage.deletedAt == 0L) {
                resultList.add(baseMessage)
            }
        }
        return resultList
    }
}