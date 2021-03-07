package com.cometchat.pro.uikit.ui_components.messages.extensions

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.CometChat.isExtensionEnabled
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.uikit.ui_components.messages.extensions.collaborative.CometChatCollaborativeActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.ReactionUtils
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.model.Sticker
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import org.json.JSONException
import org.json.JSONObject

public class Extensions {

    companion object{
        private const val TAG = "Extensions"
        var isEnabled :Boolean = true

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
                        if (extensionsObject != null && extensionsObject.has("image-moderation")) {
                            extensionMap["imageModeration"] = extensionsObject.getJSONObject("image-moderation")
                        }
                        if (extensionsObject != null && extensionsObject.has("polls")) {
                            extensionMap["polls"] = extensionsObject.getJSONObject("polls")
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
                var reaction: Reaction = feelReactionList[j]
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
                            val intent = Intent(context, CometChatCollaborativeActivity::class.java)
                            intent.putExtra(UIKitConstants.IntentStrings.URL, boardUrl)
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
                        val intent = Intent(context, CometChatCollaborativeActivity::class.java)
                        intent.putExtra(UIKitConstants.IntentStrings.URL, boardUrl)
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

        fun getImageModeration(context: Context?, baseMessage: BaseMessage?): Boolean {
            var result = false
            try {
                val extensionList = extensionCheck(baseMessage!!)
                if (extensionList != null && extensionList.containsKey("imageModeration")) {
                    val imageModeration = extensionList["imageModeration"]
                    val confidence = imageModeration!!.getInt("confidence")
                    result = confidence > 50
                }
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, "Error:" + e.message, Toast.LENGTH_LONG).show()
            }
            return result
        }

        fun getVoteCount(baseMessage: BaseMessage): Int {
            var voteCount = 0
            val result: JSONObject = getPollsResult(baseMessage)
            try {
                if (result.has("total")) {
                    voteCount = result.getInt("total")
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "getVoteCount: " + e.message)
            }
            Log.e(TAG, "getVoteCount: $voteCount")
            return voteCount
        }

        private fun getPollsResult(baseMessage: BaseMessage): JSONObject {
            var result = JSONObject()
            val extensionList = extensionCheck(baseMessage)
            if (extensionList != null) {
                try {
                    if (extensionList.containsKey("polls")) {
                        val polls = extensionList["polls"]
                        if (polls!!.has("results")) {
                            result = polls.getJSONObject("results")
                        }
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "getPollsResult: " + e.message)
                }
            }
            return result
        }

        fun getVoterInfo(baseMessage: BaseMessage, length: Int): ArrayList<String>? {
            val votes = ArrayList<String>()
            val result = getPollsResult(baseMessage)
            try {
                if (result.has("options")) {
                    val options = result.getJSONObject("options")
                    for (k in 0 until length) {
                        val optionK = options.getJSONObject((k + 1).toString())
                        votes.add(optionK.getString("count"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "getVoterInfo:Error: " + e.message)
            }
            return votes
        }

        fun userVotedOn(baseMessage: BaseMessage, totalOptions: Int, uid: String?): Int {
            var result = 0
            val resultJson = getPollsResult(baseMessage)
            try {
                if (resultJson.has("options")) {
                    val options = resultJson.getJSONObject("options")
                    for (k in 0 until totalOptions) {
                        val option = options.getJSONObject((k + 1).toString())
                        if (option.has("voters") && option["voters"] is JSONObject) {
                            val voterList = option.getJSONObject("voters")
                            if (voterList.has(uid)) {
                                result = k + 1
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "userVotedOn: " + e.message)
            }
            return result
        }

//        fun checkExtensionEnabled(extensionId: String, extensionResponseListener: ExtensionResponseListener<Any>) {
//            isExtensionEnabled(extensionId, object : CallbackListener<Boolean>() {
//                override fun onSuccess(p0: Boolean?) {
//                    Log.e(TAG, "onSuccess:checkExtensionEnabled " + extensionId + " " + p0)
//                    extensionResponseListener.onResponseSuccess(p0)
//                }
//
//                override fun onError(p0: CometChatException?) {
//                    extensionResponseListener.onResponseFailed(p0)
//                }
//
//            })
//        }

        fun checkExtensionEnabled(extensionId: String) :Boolean {
            isExtensionEnabled(extensionId, object : CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean) {

                    Log.e(TAG, "onSuccess: " + extensionId + " - " + p0)
                    isEnabled = p0
                }

                override fun onError(p0: CometChatException?) {
                    Log.e(TAG, "onError: "+p0?.message)
                }

            })


            return isEnabled

        }

    }


}