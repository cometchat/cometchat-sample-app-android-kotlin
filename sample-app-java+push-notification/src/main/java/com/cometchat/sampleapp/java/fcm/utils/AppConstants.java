package com.cometchat.sampleapp.java.fcm.utils;

public class AppConstants {

    public static Boolean isQRScanned = false;
    /**
     * Error messages used throughout the application.
     */
    public static class ErrorConstants {
        public static final String USER_NOT_FOUND = "ERR_UID_NOT_FOUND";
    }

    /**
     * Success messages or constants used throughout the application.
     */
    public static class SuccessConstants {
        public static final String SUCCESS = "success";
        public static final String ALREADY_MEMBER = "Member already has the same scope participant";
    }

    /**
     * Constants used for the app.
     */
    public static class JSONConstants {
        public static final String SAMPLE_APP_USERS_URL = "https://assets.cometchat.io/sampleapp/sampledata.json";
        public static final String KEY_USER = "users";
        public static final String UID = "uid";
        public static final String NAME = "name";
        public static final String AVATAR = "avatar";
        public static final String RAW_JSON = "rawJson";
        public static final String REPLY_COUNT = "reply_count";
    }

    /**
     * Constants used for FCM.
     */
    public static class FCMConstants {
        public static final String PROVIDER_ID = "Android-CometChat-Team-Messenger";
        public static final String KEY_UID = "UID";
        public static final String KEY_DATA = "FCM_DATA";
        public static final String REPLY_FROM_NOTIFICATION = "key_text_reply";
        public static final String KEY_NOTIFICATION_ID = "FCM_NOTIFICATION_ID";
        public static final String KEY_CLICKED_NOTIFICATION_ID = "FCM_CLICKED_NOTIFICATION_ID";
        public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "CometChatTeamMessenger"; // This is the ID for the default notification channel.
        public static final String CALL_NOTIFICATION_CHANNEL_ID = "Call"; // This is the message-specific notification channel ID.
        public static final String MESSAGE_NOTIFICATION_CHANNEL_ID = "Message"; // This is the message-specific notification channel ID.
        public static final String MESSAGE_NOTIFICATION_CHANNEL_NAME = "Message notification"; // This is the name of the message notification channel.
        public static final String GROUP_KEY = "CometChatNotificationGroup"; // This is the name of the message notification channel.
        public static final int NOTIFICATION_GROUP_SUMMARY_ID = 2000; // This ID is used for notification grouping.
        public static final String KEY_NOTIFICATION_SUMMARY_ID = "FCM_NOTIFICATION_SUMMARY_ID";
        public static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
        public static final String NOTIFICATION_TYPE_MESSAGE = "NOTIFICATION_TYPE_MESSAGE";
        public static final String NOTIFICATION_TYPE_CALL = "NOTIFICATION_TYPE_CALL";
        public static final String NOTIFICATION_PAYLOAD = "NOTIFICATION_DATA";
        public static final String NOTIFICATION_REPLY_ACTION = "NOTIFICATION_REPLY_ACTION";
        public static final String NOTIFICATION_KEY_REPLY_ACTION = "Reply";
    }
}
