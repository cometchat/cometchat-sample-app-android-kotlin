package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.androiduikit.databinding.FragmentConversationListBinding
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.ConversationsRequest
import com.cometchat.pro.core.ConversationsRequest.ConversationsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitContracts
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener

class ConversationListViewFragment : Fragment() {
    var conversationBinding: FragmentConversationListBinding? = null
    var conversationlist = ObservableArrayList<Conversation>()
    var conversationsRequest: ConversationsRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        conversationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversation_list, container, false)
        conversations
        conversationBinding!!.setConversationList(conversationlist)
        conversationBinding!!.cometchatConversationList.setItemClickListener(object : OnItemClickListener<Conversation>() {
            override fun OnItemClick(t: Any, position: Int) {
                val conversation = t as Conversation
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitContracts.IntentStrings.TYPE, conversation.conversationType)
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                    intent.putExtra(UIKitContracts.IntentStrings.NAME, (conversation.conversationWith as Group).name)
                    intent.putExtra(UIKitContracts.IntentStrings.GUID, (conversation.conversationWith as Group).guid)
                    intent.putExtra(UIKitContracts.IntentStrings.GROUP_OWNER, (conversation.conversationWith as Group).owner)
                    intent.putExtra(UIKitContracts.IntentStrings.AVATAR, (conversation.conversationWith as Group).icon)
                    intent.putExtra(UIKitContracts.IntentStrings.GROUP_TYPE, (conversation.conversationWith as Group).groupType)
                    intent.putExtra(UIKitContracts.IntentStrings.MEMBER_COUNT, (conversation.conversationWith as Group).membersCount)
                    intent.putExtra(UIKitContracts.IntentStrings.GROUP_DESC, (conversation.conversationWith as Group).description)
                    intent.putExtra(UIKitContracts.IntentStrings.GROUP_PASSWORD, (conversation.conversationWith as Group).password)
                } else {
                    intent.putExtra(UIKitContracts.IntentStrings.NAME, (conversation.conversationWith as User).name)
                    intent.putExtra(UIKitContracts.IntentStrings.UID, (conversation.conversationWith as User).uid)
                    intent.putExtra(UIKitContracts.IntentStrings.AVATAR, (conversation.conversationWith as User).avatar)
                    intent.putExtra(UIKitContracts.IntentStrings.STATUS, (conversation.conversationWith as User).status)
                }
                startActivity(intent)
            }
        })
        return conversationBinding!!.getRoot()
    }

    private val conversations: Unit
        private get() {
            if (conversationsRequest == null) {
                conversationsRequest = ConversationsRequestBuilder().setLimit(30).build()
            }
            conversationsRequest!!.fetchNext(object : CallbackListener<List<Conversation?>?>() {
                override fun onSuccess(conversations: List<Conversation?>?) {
                    conversationBinding!!.contactShimmer.stopShimmer()
                    conversationBinding!!.contactShimmer.visibility = View.GONE
                    conversationlist.addAll(conversations!!)
                }

                override fun onError(e: CometChatException) {
                    Log.e("onError: ", e.message)
                }
            })
        }
}