package com.inscripts.cometchatpulse.ViewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
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

    private val userRepository: UserRepository

    val userList: MutableLiveData<MutableMap<String,User>>

    val user:MutableLiveData<User>


    init {
        userRepository = UserRepository()
        userList = userRepository.usersList
        user=userRepository.user
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
}

