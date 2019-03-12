package com.inscripts.cometchatpulse.ViewModel

import android.app.Application
import android.app.ProgressDialog
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.widget.RelativeLayout
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.GroupMember
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.Repository.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GroupViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository: GroupRepository

    private val messageRepository:MessageRepository

    val groupList:MutableLiveData<MutableList<Group>>

    val groupMemberList:MutableLiveData<MutableList<GroupMember>>



    init {
        groupRepository= GroupRepository()
        messageRepository= MessageRepository()
        groupList=groupRepository.groupList
        groupMemberList=groupRepository.groupMemberLiveData

    }

    fun fetchGroups(LIMIT:Int,shimmerFrameLayout: ShimmerFrameLayout?){
        groupRepository.fetchGroup(LIMIT,shimmerFrameLayout)
    }

    override fun onCleared() {
        super.onCleared()
        groupRepository.clearjob()

    }

    fun createGroup(context: Context, group: Group) {
        groupRepository.createGroup(context, group)
    }

    fun fetchGroupMemeber(LIMIT: Int,guid: String) {
        groupRepository.getGroupMember(guid,LIMIT)
    }

    fun joinGroup(group: Group, progressDialog: ProgressDialog?,resId:Int,context: Context) {
        groupRepository.joinGroup(group,progressDialog,resId,context)
    }

    fun initCall(context: Context?, guid: String, receiver_type: String, call_type: String) {
        val call= Call(guid,receiver_type,call_type)
        context?.let { messageRepository.initiateCall(call, it) }
    }

    fun leaveGroup(guid: String,activity: FragmentActivity?) {
        groupRepository.leaveGroup(guid,activity)
    }

    fun deleteGroup(groupId: String,context: Context) {
        groupRepository.deleteGroup(groupId,context)
    }

    fun addCallListener(applicationContext: Context, calL_EVENT_LISTENER: String, relative: RelativeLayout?) {
         messageRepository.addCallListener(applicationContext,calL_EVENT_LISTENER,relative)
    }


}