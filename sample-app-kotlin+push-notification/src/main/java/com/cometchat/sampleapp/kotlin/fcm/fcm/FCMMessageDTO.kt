package com.cometchat.sampleapp.kotlin.fcm.fcm

import com.google.gson.annotations.SerializedName

class FCMMessageDTO {
    // Getters and Setters
    @SerializedName("conversationId")
    var conversationId: String? = null

    @SerializedName("sender")
    var sender: String? = null

    @SerializedName("receiver")
    var receiver: String? = null

    @SerializedName("receiverName")
    var receiverName: String? = null

    @SerializedName("receiverType")
    var receiverType: String? = null

    @SerializedName("receiverAvatar")
    var receiverAvatar: String? = null

    @SerializedName("tag")
    var tag: String? = null

    @SerializedName("body")
    var text: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("senderAvatar")
    var senderAvatar: String? = null

    @SerializedName("senderName")
    var senderName: String? = null

    @SerializedName("unreadMessageCount")
    var unreadMessageCount: String? = null
}
