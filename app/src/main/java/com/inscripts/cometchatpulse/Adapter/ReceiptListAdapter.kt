package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.Helpers.OnUserClick
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.Utils.DateUtil
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.receipt_item.view.*
import kotlinx.android.synthetic.main.recent_item.view.*


class ReceiptListAdapter(val context: Context, var receiptList: MutableMap<String,MessageReceipt>,val listener:OnUserClick?=null) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return receiptList.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.receipt_item, parent, false))
    }

    fun updateReciept(messageReceipt: MessageReceipt) {
        Log.e("adapter", messageReceipt.sender.uid)
        receiptList.put(messageReceipt.sender.uid,messageReceipt)
        notifyDataSetChanged()
    }
    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageReceipt = ArrayList<MessageReceipt>(receiptList.values).get(position)
        Log.e("Reciept",messageReceipt.toString())
        holder.itemView.user_name.text = messageReceipt.sender.name
        if (messageReceipt.sender.avatar!=null) {
            Glide.with(context).load(messageReceipt.sender.avatar).into(holder.itemView.user_image)
        }
        else
        {
            holder.itemView.user_image.background = context.getDrawable(R.drawable.images_iconusers)
        }
        if (messageReceipt.readAt>0)
        {
            holder.itemView.readAt.text = DateUtil.getTimeStringFromTimestamp(messageReceipt.readAt,"dd MMMM - hh:mm");
            holder.itemView.readAt.visibility = View.VISIBLE
        }
        else
        {
            holder.itemView.readAt.visibility = View.GONE
        }
        if (messageReceipt.deliveredAt>0)
        {
            holder.itemView.deliveredAt.text = DateUtil.getTimeStringFromTimestamp(messageReceipt.deliveredAt,"dd MMMM - hh:mm")
            holder.itemView.deliveredAt.visibility = View.VISIBLE
        }
        else
        {
            holder.itemView.deliveredAt.visibility = View.GONE
        }
    }

}
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        // Holds the TextView that will add each animal to
//        val tvUserName = view.textviewUserName;
//        val tvLastMessage = view.textviewLastMessage
//        val imageViewUser = view.imageViewUserAvatar;
//        val unreadCount = view.textviewChatUnreadCount
//    }
