package com.cometchat.sampleapp.compose.push.utils

object AppConstants {
    object FCMConstants {
        const val NOTIFICATION_TYPE = "notification_type"
        const val NOTIFICATION_TYPE_MESSAGE = "message"
        const val KEY_UID = "uid"
        const val KEY_GUID = "guid"

        /** Id of the tapped message, scrolled to and highlighted once the conversation opens. */
        const val KEY_MESSAGE_ID = "message_id"

        /** Set when the tapped message is a thread reply — routes to the thread instead. */
        const val KEY_PARENT_MESSAGE_ID = "parent_message_id"
    }
}
