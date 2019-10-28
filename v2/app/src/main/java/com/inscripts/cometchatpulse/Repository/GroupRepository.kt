package com.inscripts.cometchatpulse.Repository


import android.app.ProgressDialog
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import androidx.fragment.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.Toast
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
import java.lang.Exception
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.core.MessagesRequest






class GroupRepository {

    var groupList: MutableLiveData<MutableMap<String,Group>> = MutableLiveData()

    var filterGroupList: MutableLiveData<MutableMap<String,Group>> = MutableLiveData()

    var group: MutableLiveData<Group> = MutableLiveData()

    var groupListMutable = mutableMapOf<String,Group>()

    var groupFilterList = mutableMapOf<String,Group>()

    var groupMemberList = mutableMapOf<String,GroupMember>()

    var banGroupMemberList= mutableMapOf<String,GroupMember>()

    var groupMemberLiveData:MutableLiveData<MutableMap<String,GroupMember>> = MutableLiveData()

    var banMemberLiveData:MutableLiveData<MutableMap<String,GroupMember>> = MutableLiveData()

    var unreadCountGroup:MutableLiveData<MutableMap<String,Int>> = MutableLiveData()

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

                           if (p0!=null) {
                               for (group: Group in p0) {
                                   groupListMutable[group.guid] = group
                               }
                               groupList.value=groupListMutable
                           }

                        groupList.value = groupListMutable
                        shimmerFrameLayout?.stopShimmer()
                        shimmerFrameLayout?.visibility=View.GONE

                    }

                    override fun onError(p0: CometChatException?) {
                        Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                    }

                })
            } else {
                groupRequest!!.fetchNext(object : CometChat.CallbackListener<List<Group>>() {
                    override fun onSuccess(p0: List<Group>?) {
                        if (p0!=null){
                            for (group:Group in p0){
                                groupListMutable[group.guid]=group
                            }
                            groupList.value=groupListMutable
                        }
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
                banGroupMemberList.remove(uid)
                banMemberLiveData.value=banGroupMemberList
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

                     if (p0!=null){
                         for (groupMember in p0){
                             banGroupMemberList.put(groupMember.uid,groupMember)
                         }

                         banMemberLiveData.value=banGroupMemberList
                     }


                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }
        else{
            groupBanMemberRequest?.fetchNext(object :CometChat.CallbackListener<List<GroupMember>>(){
                override fun onSuccess(p0: List<GroupMember>?) {
                    if (p0!=null){
                        for (groupMember in p0){
                            banGroupMemberList.put(groupMember.uid,groupMember)
                        }
                        banMemberLiveData.value=banGroupMemberList
                    }
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

                       if (p0!=null) {

                           for (groupMember in p0) {
                              groupMemberList.put(groupMember.uid,groupMember)
                           }

                           groupMemberLiveData.value=groupMemberList
                       }


                }

                override fun onError(p0: CometChatException?) {

                }

            })
        }
        else{
            groupMemberRequest?.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
                override fun onSuccess(p0: List<GroupMember>?) {

                    if (p0!=null) {
                        for (groupMember in p0) {
                            groupMemberList.put(groupMember.uid,groupMember)
                        }
                        groupMemberLiveData.value=groupMemberList
                    }
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

            CometChat.joinGroup(group.guid,group.groupType,group.password,object :CometChat.CallbackListener<Group>(){
                override fun onSuccess(p0: Group?) {
                    progressDialog?.dismiss()
                    p0?.let { groupJoin.onJoined(it,resId) }
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

                  if (p0!=null) {
                      groupListMutable.put(p0.guid, p0)
                      groupList.value = groupListMutable
                  }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    @WorkerThread
    fun banMember(uid: String, guid: String) {
        CometChat.banGroupMember(uid,guid,object :CometChat.CallbackListener<String>(){

            override fun onSuccess(p0: String?) {
                groupMemberList.remove(uid)
                groupMemberLiveData.value=groupMemberList
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

                groupMemberList.remove(uid)
                groupMemberLiveData.value=groupMemberList

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

               val  member = groupMemberList[uid]
                 member?.scope=scope
                if (member!=null) {
                    groupMemberList.put(member.uid,member)
                    groupMemberLiveData.value = groupMemberList
                }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
            }

        })
    }


    fun searchGroup(s: String) {

        groupFilterList.clear()
            val groupSearchRequest = GroupsRequest.GroupsRequestBuilder().setSearchKeyWord(s).setLimit(100).build()

            groupSearchRequest?.fetchNext(object : CometChat.CallbackListener<List<Group>>() {

                override fun onSuccess(p0: List<Group>?) {
                     if (p0!=null) {
                         for (group: Group in p0) {
                          groupFilterList.put(group.guid,group)
                         }
                         filterGroupList.value=groupFilterList
                     }
                }

                override fun onError(p0: CometChatException?) {
                  Log.d("","onError: groupSearchRequest ${p0?.message}")
                }

            })
    }

    fun fetchUnreadCount() {
        CometChat.getUnreadMessageCountForAllGroups(object : CometChat.CallbackListener<HashMap<String, Int>>(){
            override fun onSuccess(p0: HashMap<String, Int>?) {
                unreadCountGroup.value=p0
            }

            override fun onError(p0: CometChatException?) {
               Log.d("fetchUnreadCount","onError: ${p0?.message}")
            }

        })
    }


    interface onGroupJoin{
        fun onJoined(group: Group,resId: Int)
    }
}