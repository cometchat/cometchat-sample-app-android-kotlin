package com.cometchat.sampleapp.kotlin.repository

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
 * Matches master-app-kotlin2 SampleUsersRepository pattern.
 */
object SampleUsersRepository {

    private const val SAMPLE_USERS_URL = "https://assets.cometchat.io/sampleapp/sampledata.json"

    private val client = OkHttpClient()

    /**
     * Fetches sample users from the CometChat sample users API.
     * @param onResult Callback with the list of users. Returns empty list on failure.
     */
    fun fetchSampleUsers(onResult: (List<User>) -> Unit) {
        val request = Request.Builder()
            .url(SAMPLE_USERS_URL)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Return empty list on network failure
                onResult(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val users = parseSampleUsers(body)
                        onResult(users)
                    } else {
                        onResult(emptyList())
                    }
                } catch (e: Exception) {
                    onResult(emptyList())
                }
            }
        })
    }

    /**
     * Parses the JSON response into a list of User objects.
     * Expected JSON format:
     * {
     *   "users": [
     *     { "uid": "...", "name": "...", "avatar": "..." }
     *   ]
     * }
     */
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
                    avatar = userJson.optString("avatar", null)
                }
                users.add(user)
            }
        } catch (e: Exception) {
            // Return empty list on parse failure
            return emptyList()
        }
        return users
    }
}