package com.inscripts.cometchatpulse.Repository


import android.app.ProgressDialog
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.support.annotation.WorkerThread
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.core.*
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.GroupMember
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.Activities.CreateGroupActivity
import com.inscripts.cometchatpulse.Activities.GroupDetailActivity
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Fragment.MemberFragment


class GroupRepository {

    var groupList: MutableLiveData<MutableList<Group>> = MutableLiveData()

    var group: MutableLiveData<Group> = MutableLiveData()

    var groupListMutable = mutableListOf<Group>()

    var groupMemberList = mutableListOf<GroupMember>()

    var banGroupMemberList= mutableListOf<GroupMember>()

    var groupMemberLiveData:MutableLiveData<MutableList<GroupMember>> = MutableLiveData()

    var banMemberLiveData:MutableLiveData<MutableList<GroupMember>> = MutableLiveData()

    var groupRequest: GroupsRequest? = null

    var groupMemberRequest: GroupMembersRequest? = null

    var groupBanMemberRequest:BannedGroupMembersRequest?=null

    private lateinit var groupJoin:onGroupJoin


    @WorkerThread
    fun fetchGroup(LIMIT: Int,shimmerFrameLayout: ShimmerFrameLayout?) {

            if (groupRequest == null) {

                groupRequest = GroupsRequest.GroupsRequestBuilder().setLimit(LIMIT).build()

                groupRequest!!.fetchNext(object : CometChat.CallbackListener<List<Group>>() {
                    override fun onSuccess(p0: List<Group>?) {
                        p0?.let { groupListMutable.addAll(it) }
                        groupList.value = groupListMutable

                        shimmerFrameLayout?.stopShimmer()
                        shimmerFrameLayout?.visibility=View.GONE
                    }


                    override fun onError(p0: CometChatException?) {
                        Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
                        shimmerFrameLayout?.stopShimmer()
                        shimmerFrameLayout?.visibility=View.GONE
                    }

                })
            } else {
                groupRequest!!.fetchNext(object : CometChat.CallbackListener<List<Group>>() {
                    override fun onSuccess(p0: List<Group>?) {
                        p0?.let { groupListMutable.addAll(it) }
                        groupList.value = groupListMutable
                    }

                    override fun onError(p0: CometChatException?) {
                       p0?.printStackTrace()
                    }

                })
            }
    }

    @WorkerThread
    fun getGroup(guid: String) {
            CometChat.getGroup(guid, object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(p0: Group?) {

                   Log.d("onSuccess",p0?.toString())

                }

                override fun onError(p0: CometChatException?) {

                }
            })
    }

    @WorkerThread
    fun unBanMember(guid: String,uid: String){

        CometChat.unbanGroupMember(uid,guid,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {

                showToast("Successfully unbanned Member")
            }

            override fun onError(p0: CometChatException?) {
                p0?.message?.let { showToast(it) }
            }

        })
    }


    @WorkerThread
    fun getBannedMember(guid: String,LIMIT: Int){
        if (groupBanMemberRequest==null){

            groupBanMemberRequest=BannedGroupMembersRequest.BannedGroupMembersRequestBuilder(guid).setLimit(LIMIT).build()

            groupBanMemberRequest?.fetchNext(object :CometChat.CallbackListener<List<GroupMember>>(){
                override fun onSuccess(p0: List<GroupMember>?) {

                    p0?.let { banGroupMemberList.addAll(it) }
                    banMemberLiveData.value=banGroupMemberList

                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }
        else{
            groupBanMemberRequest?.fetchNext(object :CometChat.CallbackListener<List<GroupMember>>(){
                override fun onSuccess(p0: List<GroupMember>?) {
                    p0?.let { banGroupMemberList.addAll(it) }
                    banMemberLiveData.value=banGroupMemberList
                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }
    }
    @WorkerThread
    fun getGroupMember(guid: String, LIMIT: Int){

        if (groupMemberRequest==null) {

            groupMemberRequest = GroupMembersRequest.GroupMembersRequestBuilder(guid).setLimit(LIMIT).build()

            groupMemberRequest?.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
                override fun onSuccess(p0: List<GroupMember>?) {

                    p0?.let { groupMemberList.addAll(it) }

                    groupMemberLiveData.value=groupMemberList
                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }
        else{
            groupMemberRequest?.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
                override fun onSuccess(p0: List<GroupMember>?) {

                    p0?.let { groupMemberList.addAll(it) }
                    groupMemberLiveData.value=groupMemberList
                }

                override fun onError(p0: CometChatException?) {
                   p0?.printStackTrace()
                }


            })
        }

    }

    @WorkerThread
    fun clearjob() {

    }

    @WorkerThread
    fun joinGroup(group: Group, progressDialog: ProgressDialog?,resId:Int,context:Context) {

        groupJoin=context as onGroupJoin

            CometChat.joinGroup(group.guid,group.groupType,group.password,object :CometChat.CallbackListener<String>(){
                override fun onSuccess(p0: String?) {
                    progressDialog?.dismiss()
                    group.setHasJoined(true)
                    groupJoin.onJoined(group,resId)
                }

                override fun onError(p0: CometChatException?) {
                    progressDialog?.dismiss()
                    Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
                }

            })
    }
    @WorkerThread
    fun createGroup(context: Context,group: Group) {
        CometChat.createGroup(group,object :CometChat.CallbackListener<Group>(){
            override fun onSuccess(p0: Group?) {

                (context as CreateGroupActivity).finish()
                Toast.makeText(CometChatPro.applicationContext(),p0?.groupType+" group created ",Toast.LENGTH_SHORT).show()
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext()," group creation failed  ",Toast.LENGTH_SHORT).show()
            }

        })
    }
    @WorkerThread
    fun banMember(uid: String, guid: String) {
        CometChat.banGroupMember(uid,guid,object :CometChat.CallbackListener<String>(){

            override fun onSuccess(p0: String?) {
               showToast("Successfully banned Member")
            }
            override fun onError(p0: CometChatException?) {
                showToast(p0?.message.toString())
            }

        })

    }

    fun showToast(msg:String){
        Toast.makeText(CometChatPro.applicationContext(), msg, Toast.LENGTH_SHORT).show()
    }
    @WorkerThread
    fun kickMember(uid: String, guid: String) {
        CometChat.kickGroupMember(uid,guid,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {
                showToast("Successfully kicked Member")
            }

            override fun onError(p0: CometChatException?) {
                p0?.message?.let { showToast(it) }
            }

        })
    }
    @WorkerThread
    fun leaveGroup(guid: String,activity: FragmentActivity?) {
        CometChat.leaveGroup(guid,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {
                showToast("Successfully left group")

                activity?.onBackPressed()
            }

            override fun onError(p0: CometChatException?) {
             Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun deleteGroup(groupId: String,context: Context) {

        CometChat.deleteGroup(groupId,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {
                context.startActivity(Intent(context,MainActivity::class.java))
                (context as GroupDetailActivity).finish()
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun updateScope(fragment:MemberFragment,uid: String, guid: String,scope:String) {

        CometChat.updateGroupMemberScope(uid,guid,scope,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {
              Toast.makeText(CometChatPro.applicationContext(),"Success",Toast.LENGTH_SHORT).show()



            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
            }

        })
    }


    interface onGroupJoin{
        fun onJoined(group: Group,resId: Int)
    }
}