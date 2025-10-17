package com.cometchat.ai.sampleapp.data.repository

import com.cometchat.ai.sampleapp.utils.AppConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.resources.utils.Utils
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object Repository {
    private val TAG: String = Repository::class.java.simpleName

    /**
     * Logs in a user to CometChat.
     *
     * @param userId
     * The UID of the user to log in.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun loginUser(
        userId: String?, callbackListener: CometChat.CallbackListener<User>
    ) {
        CometChatUIKit.login(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                callbackListener.onSuccess(user)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    fun logout(listener: CometChat.CallbackListener<String>) {
        CometChat.logout(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                listener.onSuccess(s)
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
            }
        })
    }

    fun fetchSampleUsers(listener: CometChat.CallbackListener<List<User>>) {
        val request: Request = Request.Builder().url(AppConstants.JSONConstants.SAMPLE_APP_USERS_URL).method("GET", null).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(
                call: okhttp3.Call, e: IOException
            ) {
                Utils.runOnMainThread {
                    listener.onError(
                        CometChatException("ERROR", e.message)
                    )
                }
            }

            override fun onResponse(
                call: okhttp3.Call, response: Response
            ) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val userList = processSampleUserList(
                            response.body()!!.string()
                        )
                        Utils.runOnMainThread {
                            listener.onSuccess(
                                userList
                            )
                        }
                    } catch (e: IOException) {
                        Utils.runOnMainThread {
                            listener.onError(
                                CometChatException("ERROR", e.message)
                            )
                        }
                    }
                } else {
                    Utils.runOnMainThread {
                        listener.onError(
                            CometChatException(
                                "ERROR", response.code().toString()
                            )
                        )
                    }
                }
            }
        })
    }

    private fun processSampleUserList(jsonString: String): List<User> {
        val users: MutableList<User> = ArrayList()
        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray(AppConstants.JSONConstants.KEY_USER)
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = User()
                user.uid = userJson.getString(AppConstants.JSONConstants.UID)
                user.name = userJson.getString(AppConstants.JSONConstants.NAME)
                user.avatar = userJson.getString(AppConstants.JSONConstants.AVATAR)
                users.add(user)
            }
        } catch (e: Exception) {
            CometChatLogger.e(TAG, e.toString())
        }
        return users
    }
}
