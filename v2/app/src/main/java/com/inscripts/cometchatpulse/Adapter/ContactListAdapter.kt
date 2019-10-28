package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Helpers.ChildClickListener
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.Helpers.OnUserClick
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewHolder.ContactViewHolder
import com.inscripts.cometchatpulse.databinding.ContactItemBinding
import com.inscripts.cometchatpulse.ViewModel.UserViewModel


class ContactListAdapter(val context: Context?, private val isBlockedList:Boolean,val listener:OnClickEvent?=null,
                         val countListener:OnUserClick?=null) : androidx.recyclerview.widget.RecyclerView.Adapter<ContactViewHolder>() {

    private var unReadCountMap: MutableMap<String, Int> = mutableMapOf()
    private var userList: MutableMap<String,User> = mutableMapOf()

    private var  onClickEvent:OnClickEvent?=listener

    private var onUserClick:OnUserClick?=countListener

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ContactViewHolder {

        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val binding:ContactItemBinding= DataBindingUtil.inflate(layoutInflater,R.layout.contact_item,p0,false)

        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(contactViewHolder: ContactViewHolder, p1: Int) {

        val user= userList.values.toMutableList()[p1]

        contactViewHolder.binding.user=user
        contactViewHolder.binding.isBlockedList=isBlockedList
        contactViewHolder.binding.textviewSingleChatUnreadCount.background=CommonUtil.setDrawable(StringContract.Color.primaryColor,40f)

        if (!isBlockedList) {

              contactViewHolder.binding.root.setOnClickListener{
                  onUserClick!!.onItemClick(it,user)

              }

            if (unReadCountMap.containsKey(user.uid)){

                contactViewHolder.binding.textviewSingleChatUnreadCount.visibility= View.VISIBLE
                contactViewHolder.binding.textviewSingleChatUnreadCount.text= unReadCountMap.get(user.uid).toString()
            }
            else{
                contactViewHolder.binding.textviewSingleChatUnreadCount.visibility=View.INVISIBLE
            }
        }

        contactViewHolder.binding.textviewUserName.typeface=StringContract.Font.name
        contactViewHolder.binding.textviewUserStatus.typeface=StringContract.Font.status
        contactViewHolder.binding.executePendingBindings()
        contactViewHolder.binding.root.setTag(R.string.user,user)
        contactViewHolder.binding.executePendingBindings()

         if (isBlockedList) {
             contactViewHolder.binding.root.setOnClickListener {
                 onClickEvent?.onClickRl(it, user)
             }
         }
    }

    internal fun setUser(users: MutableMap<String,User>) {
        this.userList = users
        notifyDataSetChanged()
    }

    fun removeUser(it: User?) {
        userList.remove(it?.uid)
        notifyDataSetChanged()
    }

    fun setUnreadCount(it: MutableMap<String, Int>) {
        unReadCountMap= it
        notifyDataSetChanged()
    }

    fun setFilter(it: MutableMap<String, User>) {
        userList.clear()
        userList=it
        notifyDataSetChanged()
    }


}