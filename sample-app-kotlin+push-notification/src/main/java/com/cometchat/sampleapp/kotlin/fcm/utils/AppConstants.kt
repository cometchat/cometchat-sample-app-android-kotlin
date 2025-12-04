package com.cometchat.sampleapp.kotlin.fcm.utils

class AppConstants {

    companion object {
        var IS_QR_SCANNED: Boolean = false
    }

    /** Error messages used throughout the application.  */
    object ErrorConstants {
        const val USER_NOT_FOUND: String = "ERR_UID_NOT_FOUND"
    }

    /** Success messages or constants used throughout the application.  */
    object SuccessConstants {
        const val SUCCESS: String = "success"
        const val SUCCESS_ALREADY_MEMBER: String = "Member already has the same scope participant"
    }

    object JSONConstants {
        const val SAMPLE_APP_USERS_URL: String = "https://assets.cometchat.io/sampleapp/sampledata.json"
        const val KEY_USER: String = "users"
        const val UID: String = "uid"
        const val NAME: String = "name"
        const val AVATAR: String = "avatar"
        const val RAW_JSON: String = "raw_json"
        const val REPLY_COUNT: String = "reply_count"
    }

    object FCMConstants {
        const val PROVIDER_ID: String = "Android-CometChat-Team-Messenger"
        const val KEY_UID: String = "UID"
        const val KEY_DATA: String = "FCM_DATA"
        const val REPLY_FROM_NOTIFICATION: String = "key_text_reply"
        const val KEY_NOTIFICATION_ID: String = "FCM_NOTIFICATION_ID"
        const val KEY_CLICKED_NOTIFICATION_ID: String = "FCM_CLICKED_NOTIFICATION_ID"
        const val DEFAULT_NOTIFICATION_CHANNEL_ID: String = "CometChatTeamMessenger" // This is the ID for the default notification channel.
        const val MESSAGE_NOTIFICATION_CHANNEL_ID: String = "Message" // This is the message-specific notification channel ID.
        const val MESSAGE_NOTIFICATION_CHANNEL_NAME: String = "Message notification" // This is the name of the message notification channel.
        const val GROUP_KEY: String = "CometChatNotificationGroup" // This is the name of the message notification channel.
        const val NOTIFICATION_GROUP_SUMMARY_ID: Int = 2000 // This ID is used for notification grouping.
        const val KEY_NOTIFICATION_SUMMARY_ID: String = "FCM_NOTIFICATION_SUMMARY_ID"
        const val NOTIFICATION_TYPE: String = "NOTIFICATION_TYPE"
        const val NOTIFICATION_TYPE_MESSAGE: String = "NOTIFICATION_TYPE_MESSAGE"
        const val NOTIFICATION_PAYLOAD: String = "NOTIFICATION_DATA"
        const val NOTIFICATION_REPLY_ACTION: String = "NOTIFICATION_REPLY_ACTION"
        const val NOTIFICATION_KEY_REPLY_ACTION: String = "Reply"
    }
}
