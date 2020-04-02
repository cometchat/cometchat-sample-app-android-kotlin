package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.androiduikit.databinding.FragmentConversationListBinding
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.ConversationsRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User

import constant.StringContract
import listeners.OnItemClickListener
import screen.messagelist.CometChatMessageListActivity

class ConversationListViewFragment : Fragment() {

    var conversationBinding: FragmentConversationListBinding? = null
    internal var conversationlist = ObservableArrayList<Conversation>()
    internal var conversationsRequest: ConversationsRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        conversationBinding = DataBindingUtil.inflate<FragmentConversationListBinding>(inflater, R.layout.fragment_conversation_list, container, false)
        getConversations()
        conversationBinding?.setConversationList(conversationlist)
        conversationBinding?.cometchatConversationList?.setItemClickListener(object : OnItemClickListener<Conversation>() {
            override fun OnItemClick(conversation: Conversation, position: Int) {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(StringContract.IntentStrings.TYPE, conversation.conversationType)
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                    intent.putExtra(StringContract.IntentStrings.NAME, (conversation.conversationWith as Group).name)
                    intent.putExtra(StringContract.IntentStrings.GUID, (conversation.conversationWith as Group).guid)
                    intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, (conversation.conversationWith as Group).owner)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, (conversation.conversationWith as Group).icon)

                } else {
                    intent.putExtra(StringContract.IntentStrings.NAME, (conversation.conversationWith as User).name)
                    intent.putExtra(StringContract.IntentStrings.UID, (conversation.conversationWith as User).uid)
                    intent.putExtra(StringContract.IntentStrings.AVATAR, (conversation.conversationWith as User).avatar)
                    intent.putExtra(StringContract.IntentStrings.STATUS, (conversation.conversationWith as User).status)
                }
                startActivity(intent)
            }

            override fun OnItemLongClick(`var`: Conversation, position: Int) {
                super.OnItemLongClick(`var`, position)
            }
        })
        return conversationBinding?.getRoot()
    }

    private fun getConversations() {
        if (conversationsRequest == null) {
            conversationsRequest = ConversationsRequest.ConversationsRequestBuilder().setLimit(30).build()
        }
        conversationsRequest!!.fetchNext(object : CometChat.CallbackListener<List<Conversation>>() {
            override fun onSuccess(conversations: List<Conversation>) {
                conversationBinding?.contactShimmer?.stopShimmer()
                conversationBinding?.contactShimmer?.setVisibility(View.GONE)
                conversationlist.addAll(conversations)
            }

            override fun onError(e: CometChatException) {
                Log.e("onError: ", e.message)
            }
        })
    }
}
