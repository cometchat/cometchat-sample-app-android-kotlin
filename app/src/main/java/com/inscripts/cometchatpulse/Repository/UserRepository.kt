package com.inscripts.cometchatpulse.Repository


import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.WorkerThread
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.*
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Utils.CommonUtil



class UserRepository() {

    var usersList = MutableLiveData<MutableMap<String,User>>()

    var user = MutableLiveData<User>()

    var mutableUserList = mutableMapOf<String,User>()

    var userRequest: UsersRequest? = null




    @WorkerThread
    fun getUser(uid: String) {

            CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
                override fun onError(p0: CometChatException?) {

                }

                override fun onSuccess(p0: User?) {
                    user.value = p0
                }

            })
    }

    @WorkerThread
    fun fetchUsers(LIMIT: Int,shimmer: ShimmerFrameLayout?) {

        if (userRequest == null) {

            userRequest = UsersRequest.UsersRequestBuilder().setLimit(LIMIT).build()

            userRequest?.fetchNext(object : CometChat.CallbackListener<List<User>>() {

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(CometChatPro.applicationContext(),p0?.message,Toast.LENGTH_SHORT).show()
                    shimmer?.stopShimmer()
                    shimmer?.visibility=View.GONE
                }

                override fun onSuccess(p0: List<User>?) {
                    if (p0 != null) {
                        Log.d("UsersRequest", " " + p0.size)
                        for (user:User in p0){
                            mutableUserList.put(user.uid,user)
                        }
                        shimmer?.stopShimmer()
                        shimmer?.visibility= View.GONE
                        usersList.value = mutableUserList
                    }
                }


            })


        } else {

            userRequest?.fetchNext(object : CometChat.CallbackListener<List<User>>() {

                override fun onSuccess(p0: List<User>?) {
                        Log.d("UsersRequest", " " + p0?.size)
                     if (p0!=null) {
                         for (user: User in p0) {
                             mutableUserList.put(user.uid, user)
                         }
                         usersList.value = mutableUserList
                     }

                }

                override fun onError(p0: CometChatException?) {
                   Log.d("fetchNext","UsersRequest onError: ${p0?.message}")
                }

            })

        }

    }


    @WorkerThread
    fun addPresenceListener(listener: String) {

            CometChat.addUserListener(listener, object : CometChat.UserListener() {
                override fun onUserOnline(p0: com.cometchat.pro.models.User?) {

                    if (p0 != null) {
                         mutableUserList.put(p0.uid,p0)
                        usersList.value=mutableUserList
                    }
                }

                override fun onUserOffline(p0: com.cometchat.pro.models.User?) {
                    if (p0 != null) {
                        mutableUserList.put(p0.uid,p0)
                        usersList.value=mutableUserList
                    }
                }

            })

    }

    @WorkerThread
    fun clearjob() {

    }

    fun removeUserListener(listener: String) {
        CometChat.removeUserListener(listener)

    }

    fun initCall(context:Context,call: Call) {
        CometChat.initiateCall(call,object :CometChat.CallbackListener<Call>(){
            override fun onSuccess(p0: Call?) {

                CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_USER,context, p0?.callReceiver as User, p0.type, true, p0.sessionId)
            }

            override fun onError(p0: CometChatException?) {

            }

        })
    }


}