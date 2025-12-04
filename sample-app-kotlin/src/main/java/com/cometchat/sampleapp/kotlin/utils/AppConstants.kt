package com.cometchat.sampleapp.kotlin.utils

class AppConstants {

    /** Error messages used throughout the application.  */
    object ErrorConstants {
        const val USER_NOT_FOUND: String = "ERR_UID_NOT_FOUND"
    }

    /** Success messages or constants used throughout the application.  */
    object SuccessConstants {
        const val SUCCESS: String = "success"
        const val ALREADY_MEMBER: String = "Member already has the same scope participant"
    }

    /**
     * Constants used for the app.
     */
    object JSONConstants {
        const val SAMPLE_APP_USERS_URL: String = "https://assets.cometchat.io/sampleapp/sampledata.json"
        const val KEY_USER: String = "users"
        const val UID: String = "uid"
        const val NAME: String = "name"
        const val AVATAR: String = "avatar"
        const val RAW_JSON: String = "raw_json"
        const val REPLY_COUNT: String = "reply_count"
    }
}