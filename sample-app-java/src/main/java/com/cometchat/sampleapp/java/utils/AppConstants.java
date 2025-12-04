package com.cometchat.sampleapp.java.utils;

public class AppConstants {

    /**
     * Error messages used throughout the application.
     */
    public static class ErrorMessages {
        public static final String USER_NOT_FOUND = "ERR_UID_NOT_FOUND";
    }

    /**
     * Success messages or constants used throughout the application.
     */
    public static class SuccessConstants {
        public static final String SUCCESS = "success";
        public static final String ALREADY_MEMBER = "Member already has the same scope participant";
    }

    public static class JSONConstants {
        public static final String SAMPLE_APP_USERS_URL = "https://assets.cometchat.io/sampleapp/sampledata.json";
        public static final String KEY_USER = "users";
        public static final String UID = "uid";
        public static final String NAME = "name";
        public static final String AVATAR = "avatar";
        public static final String RAW_JSON = "raw_json";
        public static final String REPLY_COUNT = "reply_count";
    }
}
