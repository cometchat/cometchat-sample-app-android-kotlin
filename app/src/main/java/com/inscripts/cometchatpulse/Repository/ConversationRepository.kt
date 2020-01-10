package com.inscripts.cometchatpulse.Repository

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import android.content.Context
import androidx.annotation.WorkerThread
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.*
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.CometChatHelper
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.Fragment.RecentListFragment
import kotlinx.android.synthetic.main.fragment_recent_list.*
import kotlinx.android.synthetic.main.fragment_recent_list.view.*
import java.lang.Exception
import java.util.ArrayList


class ConversationRepository {

    var conversationList:MutableLiveData<MutableList<Conversation>> = MutableLiveData()

    var conversationFilterList:MutableLiveData<MutableList<Conversation>> = MutableLiveData()

    var conversation = MutableLiveData<Conversation>()

    var conversationRequest: ConversationsRequest? = null

    private val TAG = "ConversationRepository"

    @WorkerThread
    fun fetchConversations(LIMIT: Int, shimmer: ShimmerFrameLayout?,isRefresh:Boolean) {

          if(isRefresh){
              conversationRequest=null
          }

        if (conversationRequest==null) {
            conversationRequest = ConversationsRequest.ConversationsRequestBuilder().setLimit(LIMIT).build()
        }
        conversationRequest?.fetchNext(object : CometChat.CallbackListener<List<Conversation>>() {

            override fun onSuccess(p0: List<Conversation>?) {
                Log.d("ConversationsRequest", " " + p0?.size)
//                conversationList.addAll(p0!!)
//                recentListAdapter.notifyDataSetChanged();
                shimmer?.stopShimmer()
                shimmer?.visibility = View.GONE

                if (p0?.size!=0) {
                    conversationList.value = p0 as MutableList<Conversation>?
                }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                Log.d("fetchNext", "ConversationsRequest onError: ${p0?.message}")
                shimmer?.stopShimmer()
                shimmer?.visibility = View.GONE
            }

        })
    }

    fun addMessageListener(tag: String) {
        CometChat.addMessageListener(tag,object :CometChat.MessageListener(){
            override fun onTextMessageReceived(message: TextMessage?) {
                conversation.value = CometChatHelper.getConversationFromMessage(message)

            }

            override fun onMediaMessageReceived(message: MediaMessage?) {
                conversation.value = CometChatHelper.getConversationFromMessage(message)
            }

        })
    }


}