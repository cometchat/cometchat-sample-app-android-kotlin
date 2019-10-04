package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inscripts.cometchatpulse.Fragment.MemberFragment
import com.inscripts.cometchatpulse.Helpers.OnOptionClickListener
import com.inscripts.cometchatpulse.Pojo.GroupOption
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.ViewHolder.GroupOptionViewHolder
import com.inscripts.cometchatpulse.databinding.GroupOptionItemBinding


class GroupOptionAdapter(val context:Context,val groupOptionList: MutableMap<Int,GroupOption>,
                         val ownerId:String,val guid:String, onOptionClickListener: OnOptionClickListener): RecyclerView.Adapter<GroupOptionViewHolder>() {

    private  var onOptionClickListener:OnOptionClickListener
    init {
        this.onOptionClickListener=onOptionClickListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GroupOptionViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        val binding: GroupOptionItemBinding =DataBindingUtil.inflate(layoutInflater, R.layout.group_option_item,p0,false)

        return GroupOptionViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return groupOptionList.size
    }

    override fun onBindViewHolder(p0: GroupOptionViewHolder, p1: Int) {

        val groupOption=groupOptionList.values.toMutableList()[p1]

        p0.binding.groupOption=groupOption

        p0.binding.tvOption.typeface=StringContract.Font.name


        p0.binding.root.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {

                  onOptionClickListener.OnOptionClick(groupOptionList.keys.toMutableList()[p1])

            }

        })

    }
}