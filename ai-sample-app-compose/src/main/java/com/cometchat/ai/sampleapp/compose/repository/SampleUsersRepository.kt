package com.cometchat.ai.sampleapp.compose.repository

import com.cometchat.ai.sampleapp.compose.utils.AppConstants
import com.cometchat.chat.models.User
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Repository for fetching sample users from the CometChat sample users API.
 * Used to display pre-configured test users on the login screen.
 */
object SampleUsersRepository {

    private val client = OkHttpClient()

    /**
     * Fetches sample users synchronously. Call from a background thread / coroutine on IO.
     * Returns an empty list on any failure.
     */
    fun fetchSampleUsers(): List<User> {
        return try {
            val request = Request.Builder()
                .url(AppConstants.SAMPLE_USERS_URL)
                .get()
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) parse(body) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parse(json: String): List<User> {
        val out = mutableListOf<User>()
        try {
            val array = JSONObject(json).getJSONArray("users")
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                out += User().apply {
                    uid = o.getString("uid")
                    name = o.getString("name")
                    avatar = if (o.has("avatar")) o.optString("avatar", "") else ""
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return out
    }
}
