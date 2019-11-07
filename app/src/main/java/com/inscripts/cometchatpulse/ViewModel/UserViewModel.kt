package com.inscripts.cometchatpulse.ViewModel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.User
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.Repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    var unReadCount: MutableLiveData<MutableMap<String, Int>> = MutableLiveData()

    private val userRepository: UserRepository = UserRepository()

    var userList: MutableLiveData<MutableMap<String,User>>

    var filterUserList: MutableLiveData<MutableMap<String,User>>

    var blockedUserList:MutableLiveData<MutableMap<String,User>> = MutableLiveData()

    val user:MutableLiveData<User>

    var blockedUser:MutableLiveData<User> =MutableLiveData()

    init {

        userList = userRepository.usersList
        user=userRepository.user
        blockedUser=userRepository.blockedUser
        filterUserList=userRepository.filterUsersList
    }

    fun fetchUser(LIMIT: Int,shimmer: ShimmerFrameLayout?){
        userRepository.fetchUsers(LIMIT,shimmer)

    }

    override fun onCleared() {
        super.onCleared()
        userRepository.clearjob()
    }

    fun addPresenceListener(listener: String) {
        userRepository.addPresenceListener(listener)
    }

    fun removeUserListener(listener: String) {
        userRepository.removeUserListener(listener)
    }

    fun initCall(context: Context, user: User) {
        val call= Call(user.uid,CometChatConstants.RECEIVER_TYPE_USER,CometChatConstants.CALL_TYPE_VIDEO)
        userRepository.initCall(context,call)
    }

    fun getBlockedUserList(LIMIT: Int) {
       blockedUserList= userRepository.getBlockedUser(LIMIT)
    }

    fun unBlockUser(user: User) {
       userRepository.unBlockUser(user)
    }

    fun searchUser(userName: String) {
        userRepository.searchUser(userName)
    }

    fun fetchUnreadCountForUser() {
        unReadCount=userRepository.getUnreadCount()
    }

    fun addMembertoGroup(activity: Activity,guidList: MutableSet<String>, guid: String) {
        userRepository.addMember(activity,guidList,guid)
    }


}

