package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Implementation of StickerDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching and parsing.
 * This is the default implementation used for remote data operations.
 */
class StickerDataSourceImpl : StickerDataSource {

    companion object {
        private const val EXTENSION_STICKERS = "stickers"
        private const val EXTENSION_GET = "GET"
        private const val EXTENSION_FETCH_PATH = "/v1/fetch"
    }

    /**
     * Fetches stickers from CometChat Extensions API.
     *
     * @return Result containing list of StickerSet objects or error
     */
    override suspend fun fetchStickers(): Result<List<StickerSet>> =
        suspendCancellableCoroutine { continuation ->
            if (!CometChat.isExtensionEnabled(EXTENSION_STICKERS)) {
                continuation.resume(
                    Result.failure(
                        CometChatException(
                            "ERR_EXTENSION_NOT_ENABLED",
                            "Enable the stickers extension from CometChat Pro dashboard",
                            EXTENSION_STICKERS
                        )
                    )
                )
                return@suspendCancellableCoroutine
            }

            CometChat.callExtension(
                EXTENSION_STICKERS,
                EXTENSION_GET,
                EXTENSION_FETCH_PATH,
                null,
                object : CometChat.CallbackListener<JSONObject>() {
                    override fun onSuccess(jsonObject: JSONObject?) {
                        val stickerSets = parseStickerSets(jsonObject)
                        continuation.resume(Result.success(stickerSets))
                    }

                    override fun onError(exception: CometChatException) {
                        continuation.resume(Result.failure(exception))
                    }
                }
            )
        }

    /**
     * Parses the JSON response from the stickers API into StickerSet objects.
     *
     * JSON structure:
     * ```json
     * {
     *   "data": {
     *     "defaultStickers": [
     *       {
     *         "stickerOrder": "1",
     *         "stickerSetId": "set1",
     *         "stickerUrl": "https://...",
     *         "stickerSetName": "Emotions",
     *         "stickerName": "happy"
     *       }
     *     ],
     *     "customStickers": [...]
     *   }
     * }
     * ```
     *
     * @param jsonObject The JSON response from the API
     * @return List of StickerSet objects grouped by set name
     */
    private fun parseStickerSets(jsonObject: JSONObject?): List<StickerSet> {
        if (jsonObject == null) return emptyList()

        val stickers = mutableListOf<Sticker>()

        try {
            val dataObject = jsonObject.getJSONObject("data")

            // Parse default stickers
            if (dataObject.has("defaultStickers")) {
                val defaultStickersArray = dataObject.getJSONArray("defaultStickers")
                for (i in 0 until defaultStickersArray.length()) {
                    val stickerObject = defaultStickersArray.getJSONObject(i)
                    val sticker = parseSticker(stickerObject)
                    stickers.add(sticker)
                }
            }

            // Parse custom stickers
            if (dataObject.has("customStickers")) {
                val customStickersArray = dataObject.getJSONArray("customStickers")
                for (i in 0 until customStickersArray.length()) {
                    val stickerObject = customStickersArray.getJSONObject(i)
                    val sticker = parseSticker(stickerObject)
                    stickers.add(sticker)
                }
            }
        } catch (e: Exception) {
            // Return empty list on parse error
            return emptyList()
        }

        // Group stickers by set name and convert to StickerSet objects
        return stickers
            .groupBy { it.setName }
            .map { entry -> StickerSet.fromMapEntry(entry) }
    }

    /**
     * Parses a single sticker from JSON.
     *
     * @param stickerObject The JSON object representing a sticker
     * @return A Sticker object
     */
    private fun parseSticker(stickerObject: JSONObject): Sticker {
        return Sticker(
            name = stickerObject.optString("stickerName", ""),
            url = stickerObject.optString("stickerUrl", ""),
            setName = stickerObject.optString("stickerSetName", "")
        )
    }
}
