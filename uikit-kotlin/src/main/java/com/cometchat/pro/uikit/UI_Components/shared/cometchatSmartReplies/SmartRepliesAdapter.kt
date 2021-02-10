package com.cometchat.pro.uikit.ui_components.shared.cometchatSmartReplies

import com.cometchat.pro.uikit.ui_components.shared.cometchatSmartReplies.SmartRepliesAdapter.SmartReplyViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.R
import com.google.android.material.chip.Chip
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
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
class SmartRepliesAdapter(context: Context) : RecyclerView.Adapter<SmartReplyViewHolder>() {
    private var context: Context
    private var replyArrayList: List<String> = ArrayList()
    private var fontUtils: FontUtils

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context is a object of Context.
     * @param replyArrayList is a list of users used in this adapter.
     */
    init {
        this.replyArrayList = replyArrayList
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): SmartReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cometchat_smartreply_item, parent, false)
        return SmartReplyViewHolder(view)
    }

    /**
     * This method is used to bind the UserViewHolder contents with user at given
     * position. It set username userAvatar in respective UserViewHolder content.
     *
     * @param smartReplyViewHolder is a object of UserViewHolder.
     * @param i is a position of item in recyclerView.
     * @see User
     */
    override fun onBindViewHolder(smartReplyViewHolder: SmartReplyViewHolder, i: Int) {
        val reply = replyArrayList[i]
        smartReplyViewHolder.cReply.text = reply
        smartReplyViewHolder.itemView.setTag(R.string.replyTxt, reply)
    }

    override fun getItemCount(): Int {
        return replyArrayList.size
    }

    fun updateList(replies: List<String>) {
        replyArrayList = replies
        notifyDataSetChanged()
    }

    inner class SmartReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cReply: Chip

        init {
            cReply = view.findViewById(R.id.replyText)
        }
    }

    companion object {
        private const val TAG = "SmartReplyListAdapter"
    }
}