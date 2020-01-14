package com.inscripts.cometchatpulse.ViewModel

import android.app.Application
import android.app.ProgressDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.Context
import androidx.fragment.app.FragmentActivity
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

    val groupList:MutableLiveData<MutableMap<String,Group>>

    val filterGroupList:MutableLiveData<MutableMap<String,Group>>

    val groupMemberList:MutableLiveData<MutableMap<String,GroupMember>>

    val unreadCountForGroup:MutableLiveData<MutableMap<String,Int>>



    init {
        groupRepository= GroupRepository()
        messageRepository= MessageRepository()
        groupList=groupRepository.groupList
        unreadCountForGroup=groupRepository.unreadCountGroup
        groupMemberList=groupRepository.groupMemberLiveData
        filterGroupList=groupRepository.filterGroupList

    }

    fun fetchGroups(LIMIT:Int,shimmerFrameLayout: ShimmerFrameLayout?){
        groupRepository.fetchGroup(LIMIT,shimmerFrameLayout)
    }

    override fun onCleared() {
        super.onCleared()

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

    fun searchGroup(s: String) {
        groupRepository.searchGroup(s)
    }

    fun fetchGroupUnreadCount() {
        groupRepository.fetchUnreadCount()
    }


}