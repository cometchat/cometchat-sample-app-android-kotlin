package com.inscripts.cometchatpulse.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.ChatActivity
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.Helpers.OnUserClick
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.Utils.MediaUtil
import kotlinx.android.synthetic.main.recent_item.view.*





class RecentListAdapter(val context: Context, var conversations: MutableList<Conversation>,val listener:OnUserClick?=null)
       : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    private var userClick: OnUserClick? =listener

    override fun getItemCount(): Int {
        return conversations.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recent_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.unreadCount.background= CommonUtil.setDrawable(StringContract.Color.primaryColor,40f)

        val conversation = conversations.get(position)
        if (conversation.conversationType.equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
            val user = conversation.conversationWith as User
            holder.tvUserName.text = user.name
            if (user.avatar == null) {
                val default: Drawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_outline_account_circle_24px)!!
                default.setColorFilter(StringContract.Color.primaryDarkColor, PorterDuff.Mode.SRC_ATOP)
                holder.imageViewUser.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(), default))
            }else {
                Glide.with(context).load(user.avatar).into(holder.imageViewUser)
            }
        }
        else {
            val group = conversation.conversationWith as Group
            holder.tvUserName.text = group.name
            if (group.icon == null) {
                val default: Drawable = ContextCompat.getDrawable(CometChatPro.applicationContext(), R.drawable.ic_group_default)!!
                default.setColorFilter(StringContract.Color.primaryDarkColor, PorterDuff.Mode.SRC_ATOP)
                holder.imageViewUser.setImageBitmap(MediaUtil.getPlaceholderImage(CometChatPro.applicationContext(), default))

            } else {
                Glide.with(context).load(group.icon).into(holder.imageViewUser);
            }
        }
        when (conversation.lastMessage)
        {
            is TextMessage -> {
                val textMessage = conversation.lastMessage as TextMessage
                holder.tvLastMessage.text = textMessage.text
            }
            is MediaMessage -> {
                holder.tvLastMessage.text = "Media Message"
            }
            is Action -> {
                val action = conversation.lastMessage as Action
                holder.tvLastMessage.text = action.message
            }
            is Call -> {
                val call = conversation.lastMessage as Call
                holder.tvLastMessage.text = "Call "+call.action
            }
            is CustomMessage -> {
                val custom = conversation.lastMessage as CustomMessage
                holder.tvLastMessage.text = custom.type
            }
        }
        if(conversation.unreadMessageCount>0)
        {
            holder.unreadCount.visibility = View.VISIBLE
            holder.unreadCount.text = "" + conversation.unreadMessageCount
        }
        else
        {
            holder.unreadCount.visibility = View.GONE
        }
        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                conversation.unreadMessageCount = 0;
                notifyDataSetChanged()
                if (conversation.conversationType.equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                    var user = conversation.conversationWith as User
                    p0?.let { userClick?.onItemClick(it,user) }

//                    var intent : Intent = Intent(context,ChatActivity::class.java)
//                    intent.putExtra(StringContract.IntentString.RECIVER_TYPE,CometChatConstants.CONVERSATION_TYPE_USER)
//                    intent.putExtra(StringContract.IntentString.USER_ID, user.uid)
//                    intent.putExtra(StringContract.IntentString.USER_NAME, user.name)
//                    intent.putExtra(StringContract.IntentString.USER_AVATAR, user.avatar)
//                    intent.putExtra(StringContract.IntentString.USER_STATUS, user.status)
//                    intent.putExtra(StringContract.IntentString.LAST_ACTIVE, user.lastActiveAt)
//                    context.startActivity(intent)
//                    val oneToOneFragment = OneToOneFragment().apply {
//                        arguments = Bundle().apply {
//                            putString(StringContract.IntentString.USER_ID, user.uid)
//                            putString(StringContract.IntentString.USER_NAME, user.name)
//                            putString(StringContract.IntentString.USER_AVATAR, user.avatar)
//                            putString(StringContract.IntentString.USER_STATUS, user.status)
//                            putLong(StringContract.IntentString.LAST_ACTIVE, user.lastActiveAt)
//
//                        }
//                    }
//                    var cont = context as FragmentActivity
//                    cont.supportFragmentManager.beginTransaction()
//                            .replace(R.id.main_frame, oneToOneFragment).addToBackStack(null).commit()
                }
                else
                {
                    var group = conversation.conversationWith as Group
                    p0?.let { userClick?.onItemClick(it,group) }
//                    var intent : Intent = Intent(context,ChatActivity::class.java)
//                    intent.putExtra(StringContract.IntentString.RECIVER_TYPE,CometChatConstants.CONVERSATION_TYPE_GROUP)
//                    intent.putExtra(StringContract.IntentString.GROUP_ID, group.guid)
//                    intent.putExtra(StringContract.IntentString.GROUP_NAME, group.name)
//                    intent.putExtra(StringContract.IntentString.GROUP_ICON, group.icon)
//                    intent.putExtra(StringContract.IntentString.GROUP_OWNER, group.owner)
//                    intent.putExtra(StringContract.IntentString.GROUP_DESCRIPTION, group.description)
//                    intent.putExtra(StringContract.IntentString.USER_SCOPE,group.scope)
//                    (context as Activity).finish()
////                    context.startActivity(intent)
//                    val groupFragment = GroupFragment().apply {
//                        arguments = Bundle().apply {
//                            putString(StringContract.IntentString.GROUP_ID, group.guid)
//                            putString(StringContract.IntentString.GROUP_NAME, group.name)
//                            putString(StringContract.IntentString.GROUP_ICON, group.icon)
//                            putString(StringContract.IntentString.GROUP_OWNER, group.owner)
//                            putString(StringContract.IntentString.GROUP_DESCRIPTION, group.description)
//                            putString(StringContract.IntentString.USER_SCOPE,group.scope)
//                        }
//                    }
//                    var cont = context as FragmentActivity
//                    cont.supportFragmentManager.beginTransaction()
//                            .replace(R.id.main_frame, groupFragment).addToBackStack(null).commit()
                }
            }
        })
    }

    fun updateConversation(newconversation: Conversation)
    {
        if (conversations.contains(newconversation))
        {
            var oldConversation = conversations.get(conversations.indexOf(newconversation))
            conversations.remove(oldConversation)
            newconversation.unreadMessageCount = oldConversation.unreadMessageCount+1
            conversations.add(0,newconversation)
        }
        notifyDataSetChanged()
    }
    fun refreshData(conversationsList: List<Conversation>)
    {
        conversations.addAll(conversationsList)
        notifyDataSetChanged()
    }

    fun setConversation(it: MutableList<Conversation>) {
        for (i in it.indices) {
            if (conversations.contains(it[i])) {
                val index = conversations.indexOf(it[i])
                conversations.remove(conversations.get(index))
                conversations.add(index, it[i])
            } else {
                conversations.add(it[i])
            }
        }
        notifyDataSetChanged()
    }

    fun setConversationFilter(list: MutableList<Conversation>) {
        conversations.clear()
        conversations.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        conversations.clear()
        notifyDataSetChanged()
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvUserName = view.textviewUserName;
    val tvLastMessage = view.textviewLastMessage
    val imageViewUser = view.imageViewUserAvatar;
    val unreadCount = view.textviewChatUnreadCount
}
