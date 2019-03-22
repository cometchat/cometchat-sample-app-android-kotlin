package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.inscripts.cometchatpulse.Pojo.GroupOption
import com.inscripts.cometchatpulse.databinding.SampleUserItemBinding



class AutoCompleteAdapter(context: Context, val resource: Int,var userlist:MutableList<GroupOption>):
        ArrayAdapter<GroupOption>(context,resource,userlist) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val binding:SampleUserItemBinding =DataBindingUtil.inflate(inflater,resource,parent,false)

          binding.groupOption=userlist[position]
        binding.executePendingBindings()


        return binding.root


    }

    override fun getCount(): Int {
        return userlist.size
    }

    override fun getItem(position: Int): GroupOption? {
        return userlist[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}