package com.inscripts.cometchatpulse.Adapter


import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.pro.models.GroupMember
import com.inscripts.cometchatpulse.Fragment.BanMemberFragment
import com.inscripts.cometchatpulse.Fragment.MemberFragment
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.ViewHolder.GroupMemberHolder
import com.inscripts.cometchatpulse.databinding.ContactItemBinding
import com.inscripts.cometchatpulse.databinding.GroupMemberItemBinding

class MemberListAdapter(val context: Context?,val ownerId:String,
                        val resId:Int,val listener: OnClickEvent): androidx.recyclerview.widget.RecyclerView.Adapter<GroupMemberHolder>() {

    private var groupMemberList:MutableMap<String,GroupMember> = mutableMapOf()

    private  var onClickevent:OnClickEvent = listener

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GroupMemberHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(p0.context)
        val binding: GroupMemberItemBinding = DataBindingUtil.inflate(layoutInflater,resId,p0,false)

        return GroupMemberHolder(binding)
    }

    override fun getItemCount(): Int {
     return groupMemberList.size
    }

    override fun onBindViewHolder(p0: GroupMemberHolder, p1: Int) {

       val groupMember:GroupMember=groupMemberList.values.toMutableList()[p1]

        p0.binding.member=groupMember

        p0.binding.textviewUserName.typeface=StringContract.Font.name

        p0.binding.textviewUserStatus.typeface=StringContract.Font.status

        p0.binding.root.setTag(R.string.user,groupMember)

        p0.binding.executePendingBindings()

        if (groupMember.uid.equals(ownerId))
            p0.binding.textviewUserName.text=context?.getString(R.string.you)


        p0.itemView.setOnClickListener {
            onClickevent.onClickRl(p0.itemView,groupMember)
        }

    }

    internal fun setMemberList(memberlist: MutableMap<String,GroupMember>) {
       this.groupMemberList=memberlist
        notifyDataSetChanged()

    }



}