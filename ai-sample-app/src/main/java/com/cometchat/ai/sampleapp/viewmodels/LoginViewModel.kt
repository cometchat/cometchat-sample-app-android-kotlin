package com.cometchat.ai.sampleapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.ai.sampleapp.data.repository.Repository
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit

class LoginViewModel : ViewModel() {
    val loginStatus: MutableLiveData<Boolean> = MutableLiveData()
    val selectedUser: MutableLiveData<User?> = MutableLiveData()
    val users: MutableLiveData<List<User>> = MutableLiveData()
    private val onError = MutableLiveData<CometChatException>()

    fun onError(): MutableLiveData<CometChatException> {
        return onError
    }

    fun checkUserIsNotLoggedIn() {
        if (CometChatUIKit.getLoggedInUser() != null) {
            loginStatus.setValue(true) // User is logged in
        } else {
            loginStatus.setValue(false) // User is not logged in
        }
    }

    val sampleUsers: Unit
        get() {
            Repository.fetchSampleUsers(object : CometChat.CallbackListener<List<User>>() {
                override fun onSuccess(mUsers: List<User>) {
                    users.value = mUsers
                }

                override fun onError(e: CometChatException) {
                    users.value = ArrayList()
                }
            })
        }

    fun selectedUser(user: User?) {
        selectedUser.value = user
    }

    fun login(uid: String) {
        Repository.loginUser(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User?) {
                loginStatus.value = true
            }

            override fun onError(e: CometChatException) {
                loginStatus.value = false
                onError.value = e
            }
        })
    }
}
