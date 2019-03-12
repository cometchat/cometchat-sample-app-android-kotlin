package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Helpers.ChildClickListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.ViewHolder.ContactViewHolder
import com.inscripts.cometchatpulse.databinding.ContactItemBinding
import com.inscripts.cometchatpulse.ViewModel.UserViewModel


class ContactListAdapter(val context: Context?) : RecyclerView.Adapter<ContactViewHolder>() {

    private var userList: MutableMap<String,User> = mutableMapOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ContactViewHolder {

        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val binding:ContactItemBinding= DataBindingUtil.inflate(layoutInflater,R.layout.contact_item,p0,false)

        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(contactViewHolder: ContactViewHolder, p1: Int) {

        val user=userList.values.toMutableList().get(p1)

        contactViewHolder.binding.user=user
        contactViewHolder.binding.childClick=context as ChildClickListener
        contactViewHolder.binding.textviewUserName.typeface=StringContract.Font.name
        contactViewHolder.binding.textviewUserStatus.typeface=StringContract.Font.status
        contactViewHolder.binding.executePendingBindings()
        contactViewHolder.binding.root.setTag(R.string.user,user)
        contactViewHolder.binding.executePendingBindings()

    }

    internal fun setUser(users: MutableMap<String,User>) {
        this.userList = users
        notifyDataSetChanged()

    }



}