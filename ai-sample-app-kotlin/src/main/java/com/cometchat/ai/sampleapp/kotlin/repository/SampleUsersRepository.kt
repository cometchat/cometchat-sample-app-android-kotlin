package com.cometchat.ai.sampleapp.kotlin.repository

import com.cometchat.ai.sampleapp.kotlin.utils.AppConstants
import com.cometchat.chat.models.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Repository for fetching sample users from the CometChat sample users API.
 * Used to display pre-configured test users on the login screen.
 */
object SampleUsersRepository {

    private val client = OkHttpClient()

    /**
     * Fetches sample users from the CometChat sample users API.
     * @param onResult Callback with the list of users. Returns empty list on failure.
     */
    fun fetchSampleUsers(onResult: (List<User>) -> Unit) {
        val request = Request.Builder()
            .url(AppConstants.SAMPLE_USERS_URL)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        onResult(parseSampleUsers(body))
                    } else {
                        onResult(emptyList())
                    }
                } catch (e: Exception) {
                    onResult(emptyList())
                }
            }
        })
    }

    private fun parseSampleUsers(json: String): List<User> {
        val users = mutableListOf<User>()
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("users")
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = User().apply {
                    uid = userJson.getString("uid")
                    name = userJson.getString("name")
                    avatar = if (userJson.has("avatar")) userJson.optString("avatar", "") else ""
                }
                users.add(user)
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return users
    }
}
