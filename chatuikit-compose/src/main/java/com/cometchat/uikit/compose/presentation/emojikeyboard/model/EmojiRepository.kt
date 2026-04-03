package com.cometchat.uikit.compose.presentation.emojikeyboard.model

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Singleton that loads and caches emoji data from the bundled `emoji.json` asset.
 * Direct port of `EmojiKeyboardUtils` from the non-Compose chatuikit module.
 */
object EmojiRepository {

    private const val TAG = "EmojiRepository"

    @Volatile
    private var emojiCategories: List<EmojiCategory>? = null

    /**
     * Parses `emoji.json` from assets on a background thread and caches the result.
     * Skips re-parsing if data is already cached.
     *
     * @param context Android context used to access the assets directory
     */
    fun loadAndSaveEmojis(context: Context) {
        if (emojiCategories == null || emojiCategories!!.isEmpty()) {
            Thread {
                val categories = mutableListOf<EmojiCategory>()
                try {
                    val jsonArray = JSONObject(loadJSONFromAsset(context)).getJSONArray("emojiCategory")
                    for (i in 0 until jsonArray.length()) {
                        val category = Gson().fromJson(
                            jsonArray.getJSONObject(i).toString(),
                            EmojiCategory::class.java
                        )
                        categories.add(category)
                    }
                    emojiCategories = categories
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }.start()
        }
    }

    /**
     * Returns the cached list of emoji categories, or null if not yet loaded.
     */
    fun getEmojiCategories(): List<EmojiCategory>? = emojiCategories

    private fun loadJSONFromAsset(context: Context): String? {
        var json: String? = null
        var inputStream: java.io.InputStream? = null
        try {
            inputStream = context.assets.open("emoji.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            var bytesRead = 0
            while (bytesRead < size) {
                val result = inputStream.read(buffer, bytesRead, size - bytesRead)
                if (result == -1) break
                bytesRead += result
            }
            json = String(buffer, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: e.toString())
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: e.toString())
            }
        }
        return json
    }
}
