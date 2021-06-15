package com.cometchat.pro.uikit.ui_components.shared.cometchatConversations

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.CometchatConversationListItemBinding
import com.cometchat.pro.uikit.ui_components.shared.cometchatConversations.CometChatConversationsAdapter.ConversationViewHolder
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings

/**
 * Purpose - ConversationListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of conversations. It helps to organize the list data in recyclerView.
 * It also help to perform search operation on list of conversation.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */
class CometChatConversationsAdapter(context: Context?) : RecyclerView.Adapter<ConversationViewHolder>(), Filterable {
    private var unreadCountEnabled: Boolean = false
    private var userPresenceEnabled: Boolean = false
    private var context: Context? = null
    /**
     * ConversationListAdapter maintains two arrayList i.e conversationList and filterConversationList.
     * conversationList is a original list and it will not get modified while filterConversationList
     * will get modified as per search filter. In case if search field is empty then to retrieve
     * original list we set filerConversationList = conversationList.
     * Here filterConversationList will be main list for this adapter.
     */
    private var conversationList: MutableList<Conversation> = mutableListOf()
    private var filterConversationList: MutableList<Conversation>? = mutableListOf()
    private var fontUtils: FontUtils

    /**
     * It is constructor which takes conversationList as parameter and bind it with conversationList
     * and filterConversationList in adapter.
     *
     * @param context is a object of Context.
     * @param conversationList is list of conversations used in this adapter.
     */
    init {
        updateList(conversationList)
        filterConversationList = conversationList
        this.context = context
        fontUtils = FontUtils.getInstance(context)
        FeatureRestriction.isUnreadCountEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                unreadCountEnabled = p0
                Log.e("unreadCountEnabled", "onSuccess: unreadCountEnabled"+unreadCountEnabled )
            }
        })
        FeatureRestriction.isUserPresenceEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                userPresenceEnabled = p0
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val conversationListRowBinding: CometchatConversationListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_conversation_list_item, parent, false)
        return ConversationViewHolder(conversationListRowBinding)
    }

    /**
     * This method is used to bind the ConversationViewHolder contents with conversation at given
     * position. It set avatar, name, lastMessage, unreadMessageCount and messageTime of conversation
     * in a respective ConversationViewHolder content. It checks whether conversation type is user
     * or group and set name and avatar as accordingly. It also checks whether last message is text, media
     * or file and modify txtUserMessage view accordingly.
     *
     * @param conversationViewHolder is a object of ConversationViewHolder.
     * @param position is a position of item in recyclerView.
     *
     * @see Conversation
     */
    override fun onBindViewHolder(conversationViewHolder: ConversationViewHolder, position: Int) {
        val conversation = filterConversationList!![position]
        var avatar: String? = null
        var name: String? = null
        var status: String? = null
        var lastMessageText: String? = null
        val baseMessage = conversation.lastMessage

        conversationViewHolder.conversationListRowBinding.conversation = conversation
        conversationViewHolder.conversationListRowBinding.executePendingBindings()
        var type: String? = null
        var category: String? = null
        if (baseMessage != null) {
            type = baseMessage.type
            category = baseMessage.category
            setStatusIcon(conversationViewHolder.conversationListRowBinding.messageTime, baseMessage)
            conversationViewHolder.conversationListRowBinding.messageTime.visibility = View.VISIBLE
            conversationViewHolder.conversationListRowBinding.messageTime.text = Utils.getLastMessageDate(baseMessage.sentAt)
            if (baseMessage.deletedAt > 0L) {
                if (FeatureRestriction.isHideDeletedMessagesEnabled())
                    lastMessageText = ""
            } else lastMessageText = Utils.getLastMessage(context!!, baseMessage)
        } else {
            lastMessageText = context!!.resources.getString(R.string.tap_to_start_conversation)
            conversationViewHolder.conversationListRowBinding.txtUserMessage.marqueeRepeatLimit = 100
            conversationViewHolder.conversationListRowBinding.txtUserMessage.setHorizontallyScrolling(true)
            conversationViewHolder.conversationListRowBinding.txtUserMessage.isSingleLine = true
            conversationViewHolder.conversationListRowBinding.messageTime.visibility = View.GONE
        }
        conversationViewHolder.conversationListRowBinding.txtUserMessage.text = lastMessageText
        conversationViewHolder.conversationListRowBinding.txtUserMessage.typeface = fontUtils.getTypeFace(FontUtils.robotoRegular)
        conversationViewHolder.conversationListRowBinding.txtUserName.typeface = fontUtils.getTypeFace(FontUtils.robotoMedium)
        conversationViewHolder.conversationListRowBinding.messageTime.typeface = fontUtils.getTypeFace(FontUtils.robotoRegular)
        if (conversation.conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
            name = (conversation.conversationWith as User).name
            avatar = (conversation.conversationWith as User).avatar
            status = (conversation.conversationWith as User).status
            if (status == CometChatConstants.USER_STATUS_ONLINE) {
                if (userPresenceEnabled) {
                    conversationViewHolder.conversationListRowBinding.userStatus.visibility = View.VISIBLE
                    conversationViewHolder.conversationListRowBinding.userStatus.setUserStatus(status)
                }
            } else conversationViewHolder.conversationListRowBinding.userStatus.visibility = View.GONE
        } else {
            name = (conversation.conversationWith as Group).name
            avatar = (conversation.conversationWith as Group).icon
            conversationViewHolder.conversationListRowBinding.userStatus.visibility = View.GONE
        }
        if (!unreadCountEnabled) {
            conversationViewHolder.conversationListRowBinding.messageCount.visibility = View.GONE
            conversationViewHolder.conversationListRowBinding.messageCount.setCount(0)
        } else
            conversationViewHolder.conversationListRowBinding.messageCount.setCount(conversation.unreadMessageCount)
        conversationViewHolder.conversationListRowBinding.txtUserName.text = name
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            conversationViewHolder.conversationListRowBinding.avUser.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimary, context!!.theme))
            conversationViewHolder.conversationListRowBinding.avUser.setBackgroundColor(Color.parseColor(UIKitSettings.color))
            conversationViewHolder.conversationListRowBinding.messageCount.setCountBackground(Color.parseColor(UIKitSettings.color))
//        }
        if (avatar!=null && avatar.isNotEmpty()) {
            conversationViewHolder.conversationListRowBinding.avUser.setAvatar(avatar)
        } else {
            conversationViewHolder.conversationListRowBinding.avUser.setInitials(name)
        }
        if (Utils.isDarkMode(context!!)) {
            conversationViewHolder.conversationListRowBinding.txtUserName.setTextColor(context!!.resources.getColor(R.color.textColorWhite))
            conversationViewHolder.conversationListRowBinding.tvSeprator.setBackgroundColor(context!!.resources.getColor(R.color.grey))
        } else {
            conversationViewHolder.conversationListRowBinding.txtUserName.setTextColor(context!!.resources.getColor(R.color.primaryTextColor))
            conversationViewHolder.conversationListRowBinding.tvSeprator.setBackgroundColor(context!!.resources.getColor(R.color.light_grey))
        }
        conversationViewHolder.conversationListRowBinding.root.setTag(R.string.conversation, conversation)
    }

    private fun setStatusIcon(txtTime: TextView, baseMessage: BaseMessage) {
        FeatureRestriction.isDeliveryReceiptsEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0) {
                    if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_USER && baseMessage.sender.uid == CometChat.getLoggedInUser().uid) {
                        if (baseMessage.readAt != 0L) {
                            txtTime.text = Utils.getLastMessageDate(baseMessage.sentAt)
                            txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_double_tick, 0, 0, 0)
                            txtTime.compoundDrawablePadding = 10
                        } else if (baseMessage.deliveredAt != 0L) {
                            txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
                            txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0)
                            txtTime.compoundDrawablePadding = 10
                        } else {
                            txtTime.text = Utils.getHeaderDate(baseMessage.sentAt * 1000)
                            txtTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_black_24dp, 0, 0, 0)
                            txtTime.compoundDrawablePadding = 10
                        }
                    } else {
                        txtTime.text = Utils.getHeaderDate(baseMessage.sentAt)
                        txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return filterConversationList?.size!!
    }

    /**
     * This method is used to update the filterConversationList with new conversations and avoid
     * duplicates conversations.
     *
     * @param conversations is a list of conversation which will be updated in adapter.
     */
    fun updateList(conversations: List<Conversation>) {
        for (i in conversations.indices) {
            if (filterConversationList!!.contains(conversations[i])) {
                val index = filterConversationList!!.indexOf(conversations[i])
                filterConversationList!!.remove(conversations[i])
                filterConversationList!!.add(index, conversations[i])
            } else {
                filterConversationList!!.add(conversations[i])
            }
        }
        notifyDataSetChanged()
    }

    fun setReadReceipts(readReceipts: MessageReceipt) {
        for (i in 0 until filterConversationList!!.size - 1) {
            val conversation = filterConversationList!![i]
            if (conversation.conversationType == CometChatConstants.RECEIVER_TYPE_USER && readReceipts.sender.uid == (conversation.conversationWith as User).uid) {
                val baseMessage = filterConversationList!![i].lastMessage
                if (baseMessage != null && baseMessage.readAt == 0L) {
                    baseMessage.readAt = readReceipts.readAt
                    val index = filterConversationList!!.indexOf(filterConversationList!![i])
                    filterConversationList!!.removeAt(index)
                    conversation.lastMessage = baseMessage
                    filterConversationList!!.add(index, conversation)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setDeliveredReceipts(deliveryReceipts: MessageReceipt) {
        for (i in 0 until filterConversationList!!.size - 1) {
            val conversation = filterConversationList!![i]
            if (conversation.conversationType == CometChatConstants.RECEIVER_TYPE_USER && deliveryReceipts.sender.uid == (conversation.conversationWith as User).uid) {
                val baseMessage = filterConversationList!![i].lastMessage
                if (baseMessage != null && baseMessage.deliveredAt == 0L) {
                    baseMessage.readAt = deliveryReceipts.deliveredAt
                    val index = filterConversationList!!.indexOf(filterConversationList!![i])
                    filterConversationList!!.removeAt(index)
                    conversation.lastMessage = baseMessage
                    filterConversationList!!.add(index, conversation)
                }
            }
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to remove the conversation from filterConversationList
     *
     * @param conversation is a object of conversation.
     *
     * @see Conversation
     */
    fun remove(conversation: Conversation?) {
        val position = filterConversationList!!.indexOf(conversation)
        filterConversationList!!.remove(conversation)
        notifyItemRemoved(position)
    }

    /**
     * This method is used to update conversation in filterConversationList.
     *
     * @param conversation is an object of Conversation. It is used to update the previous conversation
     * in list
     * @see Conversation
     */
    fun update(conversation: Conversation) {
        if (filterConversationList!!.contains(conversation)) {
            val oldConversation = filterConversationList!![filterConversationList!!.indexOf(conversation)]
            filterConversationList!!.remove(oldConversation)
            var isCustomMessage = false
            if (conversation.lastMessage.metadata != null && conversation.lastMessage.metadata.has("incrementUnreadCount"))
                isCustomMessage = conversation.lastMessage.metadata.getBoolean("incrementUnreadCount")

            if (conversation.lastMessage.editedAt == 0L && conversation.lastMessage.deletedAt == 0L && conversation.lastMessage.category == CometChatConstants.CATEGORY_MESSAGE || isCustomMessage)
                conversation.unreadMessageCount = oldConversation.unreadMessageCount + 1
            else {
                conversation.unreadMessageCount = oldConversation.unreadMessageCount
            }
            filterConversationList!!.add(0, conversation)
        } else {
            filterConversationList!!.add(0, conversation)
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to add conversation in list.
     *
     * @param conversation is an object of Conversation. It will be added to filterConversationList.
     *
     * @see Conversation
     */
    fun add(conversation: Conversation) {
        if (filterConversationList != null) filterConversationList!!.add(conversation)
    }

    /**
     * This method is used to reset the adapter by clearing filterConversationList.
     */
    fun resetAdapterList() {
        filterConversationList!!.clear()
        notifyDataSetChanged()
    }

    /**
     * It is used to perform search operation in filterConversationList. It will check
     * whether searchKeyword is similar to username or group name and modify filterConversationList
     * accordingly. In case if searchKeyword is empty it will set filterConversationList = conversationList
     *
     * @return
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val searchKeyword = charSequence.toString()
                filterConversationList = if (searchKeyword.isEmpty()) {
                    conversationList
                } else {
                    val tempFilter: MutableList<Conversation> = mutableListOf()
                    for (conversation in filterConversationList!!) {
                        if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_USER &&
                                (conversation.conversationWith as User).name.toLowerCase().contains(searchKeyword)) {
                            tempFilter.add(conversation)
                        } else if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP &&
                                (conversation.conversationWith as Group).name.toLowerCase().contains(searchKeyword)) {
                            tempFilter.add(conversation)
                        } else if (conversation.lastMessage != null &&
                                conversation.lastMessage.category == CometChatConstants.CATEGORY_MESSAGE &&
                                conversation.lastMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT &&
                                (conversation.lastMessage as TextMessage).text!=null &&
                                (conversation.lastMessage as TextMessage).text.contains(searchKeyword)) {
                            tempFilter.add(conversation)
                        }
                    }
                    tempFilter
                }
                val filterResults = FilterResults()
                filterResults.values = filterConversationList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterConversationList = filterResults.values as MutableList<Conversation>?
                notifyDataSetChanged()
            }
        }
    }

    inner class ConversationViewHolder(var conversationListRowBinding: CometchatConversationListItemBinding) : RecyclerView.ViewHolder(conversationListRowBinding.root)
}