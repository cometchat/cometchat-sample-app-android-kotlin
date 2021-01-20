package utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.uikit.reaction.model.Reaction
import com.cometchat.pro.uikit.sticker.model.Sticker
import constant.StringContract
import listeners.ExtensionResponseListener
import org.json.JSONException
import org.json.JSONObject
import screen.CometChatWebViewActivity
import java.util.*
import kotlin.collections.HashMap

public class Extensions {

    companion object{
        private const val TAG = "Extensions"

        fun extensionCheck(baseMessage: BaseMessage): HashMap<String, JSONObject>? {
            val metadata = baseMessage.metadata
            val extensionMap = HashMap<String, JSONObject>()
            try {
                return if (metadata != null) {
                    val injectedObject = metadata.getJSONObject("@injected")
                    if (injectedObject != null && injectedObject.has("extensions")) {
                        val extensionsObject = injectedObject.getJSONObject("extensions")
                        if (extensionsObject != null && extensionsObject.has("link-preview")) {
                            val linkPreviewObject = extensionsObject.getJSONObject("link-preview")
                            val linkPreview = linkPreviewObject.getJSONArray("links")
                            if (linkPreview.length() > 0) {
                                extensionMap["linkPreview"] = linkPreview.getJSONObject(0)
                            }
                        }
                        if (extensionsObject != null && extensionsObject.has("smart-reply")) {
                            extensionMap["smartReply"] = extensionsObject.getJSONObject("smart-reply")
                        }
                        if (extensionsObject != null && extensionsObject.has("thumbnail-generation")){
                            extensionMap["thumbnailGeneration"] = extensionsObject.getJSONObject("thumbnail-generation")
                        }
                        if (extensionsObject != null && extensionsObject.has("reactions")){
                            extensionMap["reactions"] = extensionsObject.getJSONObject("reactions")
                        }
                        if (extensionsObject != null && extensionsObject.has("profanity-filter")){
                            extensionMap["profanityFilter"] = extensionsObject.getJSONObject("profanity-filter")
                        }
                        if (extensionsObject != null && extensionsObject.has("whiteboard")){
                            extensionMap["whiteboard"] = extensionsObject.getJSONObject("whiteboard")
                        }
                        if (extensionsObject != null && extensionsObject.has("document")){
                            extensionMap["document"] = extensionsObject.getJSONObject("document")
                        }
                        if (extensionsObject != null && extensionsObject.has("data-masking")) {
                            extensionMap["dataMasking"] = extensionsObject.getJSONObject("data-masking")
                        }
                    }
                    extensionMap
                } else null
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "isLinkPreview: " + e.message)
            }
            return null
        }

        private fun getSmartReplyList(baseMessage: BaseMessage): List<String>? {
            val extensionList: HashMap<String, JSONObject> = extensionCheck(baseMessage)!!
            if (extensionList != null && extensionList.containsKey("smartReply")) {
                val replyObject = extensionList["smartReply"]
                val replyList: MutableList<String> = ArrayList()
                try {
                    replyList.add(replyObject!!.getString("reply_positive"))
                    replyList.add(replyObject.getString("reply_neutral"))
                    replyList.add(replyObject.getString("reply_negative"))
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "onSuccess: " + e.message)
                }
                return replyList
            }
            return null
        }

        fun checkSmartReply(lastMessage: BaseMessage?): List<String?>? {
            if (lastMessage != null && lastMessage.sender.uid != CometChat.getLoggedInUser().uid) {
                if (lastMessage.metadata != null) {
                    return getSmartReplyList(lastMessage)
                }
            }
            return null
        }

        fun fetchStickers(extensionResponseListener: ExtensionResponseListener<Any>) {
            CometChat.callExtension("stickers", "GET", "/v1/fetch", null, object : CallbackListener<JSONObject>() {
                override fun onSuccess(jsonObject: JSONObject?) {
                    extensionResponseListener.onResponseSuccess(jsonObject)
                }

                override fun onError(e: CometChatException?) {
                    extensionResponseListener.onResponseFailed(e)
                }

            })
        }

        fun extractStickersFromJSON(jsonObject: JSONObject): HashMap<String?, MutableList<Sticker>?> {
            val stickers: MutableList<Sticker> = ArrayList()
            if (jsonObject != null) {
                try {
                    val dataObject: JSONObject = jsonObject.getJSONObject("data")
                    val defaultStickersArray = dataObject.getJSONArray("defaultStickers")
                    Log.d(TAG, "getStickersList: defaultStickersArray " + defaultStickersArray.length())
                    for (i in 0 until defaultStickersArray.length()) {
                        val stickerObject = defaultStickersArray.getJSONObject(i)
                        val stickerOrder = stickerObject.getString("stickerOrder")
                        val stickerSetId = stickerObject.getString("stickerSetId")
                        val stickerUrl = stickerObject.getString("stickerUrl")
                        val stickerSetName = stickerObject.getString("stickerSetName")
                        val stickerName = stickerObject.getString("stickerName")
                        val sticker = Sticker(stickerName, stickerUrl, stickerSetName)
                        stickers.add(sticker)
                    }
                    if (dataObject.has("customStickers")) {
                        val customSticker = dataObject.getJSONArray("customStickers")
                        Log.d(TAG, "getStickersList: customStickersArray $customSticker")
                        for (i in 0 until customSticker.length()) {
                            val stickerObject = customSticker.getJSONObject(i)
                            val stickerOrder = stickerObject.getString("stickerOrder")
                            val stickerSetId = stickerObject.getString("stickerSetId")
                            val stickerUrl = stickerObject.getString("stickerUrl")
                            val stickerSetName = stickerObject.getString("stickerSetName")
                            val stickerName = stickerObject.getString("stickerName")
                            val sticker = Sticker(stickerName, stickerUrl, stickerSetName)
                            stickers.add(sticker)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val stickerMap: HashMap<String?, MutableList<Sticker>?> = HashMap()
            for (i in stickers.indices) {
                if (stickerMap.containsKey(stickers[i].setName)) {
                    stickerMap[stickers[i].setName]!!.add(stickers[i])
                } else {
                    val list: MutableList<Sticker> = ArrayList()
                    list.add(stickers[i])
                    stickerMap[stickers[i].setName] = list
                }
            }
            return stickerMap
        }

        fun getThumbnailGeneration(context: Context, baseMessage: BaseMessage): String? {
            var urlSmall:String? = null
            try {
                val extensionList = extensionCheck(baseMessage)
                if (extensionList != null && extensionList.containsKey("thumbnailGeneration")){
                    val thumbnailGenerationObject = extensionList["thumbnailGeneration"]
                    urlSmall = thumbnailGenerationObject!!.getString("url_small")
                }
            }
            catch (e: Exception)
            {
                Toast.makeText(context, "Error:" + e.message, Toast.LENGTH_LONG).show()
            }
            return urlSmall
        }

        fun getInitialReactions(i: Int): MutableList<Reaction> {
            var resultReaction: MutableList<Reaction> = ArrayList()
            var feelReactionList = ReactionUtils.getFeelList()
            for (j in 0..i){
                var reaction:Reaction = feelReactionList[j]
                resultReaction.add(reaction)
            }
            return resultReaction
        }

        fun getReactionsOnMessage(baseMessage: BaseMessage): HashMap<String, String> {
            val resultReactions: HashMap<String, String> = HashMap()
            try {
                var extensionList = extensionCheck(baseMessage)
                if (extensionList != null && extensionList.containsKey("reactions")) {
                    val reactionObject = extensionList["reactions"]
                    var keys: Iterator<String> = reactionObject!!.keys()
                    while (keys.hasNext()){
                        var keyValue :String = keys.next()
                        var reaction : JSONObject = reactionObject.getJSONObject(keyValue)
                        var reactionCount: String = reaction.length().toString()
                        resultReactions[keyValue] = reactionCount
                        Log.e(TAG, "getReactionsOnMessage: $keyValue=$reactionCount")
                    }
                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            return resultReactions
        }

        fun getReactionsInfo(jsonObject: JSONObject): HashMap<String, List<String>> {
            val result = HashMap<String, List<String>>()
            if (jsonObject != null) {
                try {
                    val injectedObject = jsonObject.getJSONObject("@injected")
                    if (injectedObject != null && injectedObject.has("extensions")) {
                        val extensionsObject = injectedObject.getJSONObject("extensions")
                        if (extensionsObject.has("reactions")) {
                            val data = extensionsObject.getJSONObject("reactions")
                            val keys = data.keys()
                            while (keys.hasNext()) {
                                val reactionUser: MutableList<String> = ArrayList()
                                val keyValue = keys.next() as String
                                val react = data.getJSONObject(keyValue)
                                val uids = react.keys()
                                while (uids.hasNext()) {
                                    val uid = uids.next() as String
                                    val user = react.getJSONObject(uid)
                                    reactionUser.add(user.getString("name"))
                                    Log.e(TAG, "getReactionsOnMessage: " + keyValue + "=" + user.getString("name"))
                                }
                                result[keyValue] = reactionUser
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            return result
        }

        fun getProfanityFilter(baseMessage: BaseMessage): String {
            var result = (baseMessage as TextMessage).text
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                try {
                    if (extensionList.containsKey("profanityFilter")) {
                        val profanityFilter = extensionList["profanityFilter"]
                        val profanity = profanityFilter?.getString("profanity")
                        val cleanMessage = profanityFilter?.getString("message_clean")
                        if (profanity == "no")
                            result = (baseMessage as TextMessage).text
                        else
                            result = cleanMessage
                    } else {
                        result = (baseMessage as TextMessage).text.trim()
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "checkProfanityMessage:Error: " + e.message)
                }
            }
            return result
        }

        fun callExtensions(slug: String, id: String, type: String, extensionResponseListener: ExtensionResponseListener<Any>) {
            var body = JSONObject()
            body.put("receiverType", type)
            body.put("receiver", id)
            CometChat.callExtension(slug, "POST", "/v1/create", body, object : CallbackListener<JSONObject>() {
                override fun onSuccess(p0: JSONObject?) {
                    extensionResponseListener.onResponseSuccess(p0)
                }

                override fun onError(p0: CometChatException?) {
                    extensionResponseListener.onResponseFailed(p0)
                }

            })
        }

        fun openWhiteBoard(baseMessage: BaseMessage, context: Context) {
            var boardUrl = ""
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                try {
                    if (extensionList.containsKey("whiteboard")) {
                        val whiteBoardObject = extensionList["whiteboard"]
                        if (whiteBoardObject != null && whiteBoardObject.has("board_url")) {
                            boardUrl = whiteBoardObject.getString("board_url")
                            var username = CometChat.getLoggedInUser().name.replace(" ", "_")
                            boardUrl = "$boardUrl&username=$username"
                            val intent = Intent(context, CometChatWebViewActivity::class.java)
                            intent.putExtra(StringContract.IntentStrings.URL, boardUrl)
                            context.startActivity(intent)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun getWhiteBoardUrl(baseMessage: BaseMessage): String? {
            var boardUrl = ""
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                if (extensionList.containsKey("whiteboard")) {
                    val whiteBoardObject = extensionList["whiteboard"]
                    if (whiteBoardObject != null && whiteBoardObject.has("board_url")) {
                        try {
                            boardUrl = whiteBoardObject.getString("board_url")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return boardUrl
        }

        fun openWriteBoard(baseMessage: BaseMessage, context: Context) {
            try {
                var boardUrl = ""
                val extensionList = extensionCheck(baseMessage)
                if (extensionList != null && extensionList.containsKey("document")) {
                    val writeBoardObject = extensionList["document"]
                    if (writeBoardObject != null && writeBoardObject.has("document_url")) {
                        boardUrl = writeBoardObject.getString("document_url")
                        val intent = Intent(context, CometChatWebViewActivity::class.java)
                        intent.putExtra(StringContract.IntentStrings.URL, boardUrl)
                        context.startActivity(intent)
                    }
                }
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        fun getWriteBoardUrl(baseMessage: BaseMessage): String? {
            var documentUrl = ""
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                if (extensionList.containsKey("document")) {
                    val writeBoardObject = extensionList["document"]
                    if (writeBoardObject != null && writeBoardObject.has("document_url")) {
                        try {
                            documentUrl = writeBoardObject.getString("document_url")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return documentUrl

        }

        fun checkDataMasking(baseMessage: BaseMessage): String {
            var result = (baseMessage as TextMessage).text
            val sensitiveData: String
            val messageMasked: String
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                try {
                    if (extensionList.containsKey("dataMasking")) {
                        val dataMasking = extensionList["dataMasking"]
                        if (dataMasking != null && dataMasking.has("data")) {
                            val dataObject = dataMasking.getJSONObject("data")
                            if (dataObject.has("sensitive_data") && dataObject.has("message_masked")) {
                                sensitiveData = dataObject.getString("sensitive_data")
                                messageMasked = dataObject.getString("message_masked")
                                result = if (sensitiveData == "no") baseMessage.text else messageMasked
                            } else if (dataObject.has("action") && dataObject.has("message")) {
                                result = dataObject.getString("message")
                            }
                        }
                    } else {
                        result = baseMessage.text
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return result
        }


    }


}