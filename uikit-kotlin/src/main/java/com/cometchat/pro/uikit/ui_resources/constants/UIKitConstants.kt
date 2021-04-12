package com.cometchat.pro.uikit.ui_resources.constants

import com.cometchat.pro.constants.CometChatConstants
import java.util.*

class UIKitConstants {
    object Sounds {
        var enableMessageSounds = true

        var enableCallSounds = true
    }

    object AppInfo {
        var AUTH_KEY = "";
    }
    object IntentStrings {
        const val INTENT_MEDIA_MESSAGE = "intent_media_message"
        const val IMAGE_TYPE = "image/*"
        const val UID = "uid"
        const val AVATAR = "avatar"
        const val STATUS = "status"
        const val NAME = "name"
        const val TYPE = "type"
        const val GUID = "guid"
        const val tabBar = "tabBar"
        @JvmField
        val EXTRA_MIME_DOC = arrayOf("text/plane", "text/html", "application/pdf", "application/msword", "application/vnd.ms.excel", "application/mspowerpoint", "application/zip")
        const val MEMBER_COUNT = "member_count"
        const val GROUP_MEMBER = "group_members"
        const val GROUP_NAME = "group_name"
        const val MEMBER_SCOPE = "member_scope"
        const val GROUP_OWNER = "group_owner"
        const val ID = "id"
        const val IS_ADD_MEMBER = "is_add_member"
        const val IS_BLOCKED_BY_ME = "is_blocked_by_me"
        const val SESSION_ID = "sessionId"
        const val INCOMING = "incoming"
        const val FROM_CALL_LIST = "from_call_list"
        const val JOIN_ONGOING = "join_ongoing_call"
        const val MESSAGE_TYPE_IMAGE_NAME = "file_name"
        const val MESSAGE_TYPE_IMAGE_URL = "file_url"
        const val MESSAGE_TYPE_IMAGE_MIME_TYPE = "file_mime"
        const val MESSAGE_TYPE_IMAGE_EXTENSION = "file_extension"
        const val MESSAGE_TYPE_IMAGE_SIZE = "file_size"
        const val SHOW_MODERATORLIST = "is_moderator"
        const val GROUP_DESC = "group_description"
        const val GROUP_PASSWORD = "group_password"
        const val GROUP_TYPE = "group_type"

        const val MESSAGE_CATEGORY = "message_category"
        const val MESSAGE_TYPE = "message_type"
        const val TEXTMESSAGE = "text_message"
        const val SENTAT = "sent_at"

        const val LOCATION = "LOCATION"
        const val CUSTOM_MESSAGE = "custom_message"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"

        const val PARENT_ID = "parent_id"
        const val REPLY_COUNT = "reply_count"
        const val CONVERSATION_NAME = "conversation_name"

        const val STICKERS = "Sticker"
        const val REACTION_INFO = "reaction_info"

        const val WHITEBOARD = "extension_whiteboard"
        const val WRITEBOARD = "extension_document"
        const val URL = "url"

        const val MEETING = "meeting"

        const val IMAGE_MODERATION = "image_moderation"

        const val POLLS = "extension_poll"
        const val POLL_QUESTION = "poll_question"
        const val POLL_OPTION = "poll_option"
        const val POLL_RESULT = "poll_result"
        const val POLL_ID = "poll_id"
        const val POLL_VOTE_COUNT = "poll_vote_count"
    }

    object Tab {
        const val Conversation = "conversations"
        const val User = "users"
        const val Group = "groups"
    }

    object RequestCode {
        const val GALLERY = 1
        const val CAMERA = 2
        const val FILE = 25
        const val BLOCK_USER = 7
        const val DELETE_GROUP = 8
        const val AUDIO = 3
        const val READ_STORAGE = 1
        const val RECORD = 3
        const val LOCATION = 14
    }

    object MapUrl {
        const val MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=380x220&markers=color:red|"
        const val MAP_ACCESS_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXX"
    }

    object MessageRequest {

        var messageTypesForUser: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //Custom Messages
                IntentStrings.LOCATION,
                IntentStrings.STICKERS,
                IntentStrings.WHITEBOARD,
                IntentStrings.WRITEBOARD,
                IntentStrings.POLLS
        ))
        var messageTypesForGroup: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //For Group Actions
                CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
                //Custom Messages
                IntentStrings.LOCATION,
                IntentStrings.STICKERS,
                IntentStrings.WHITEBOARD,
                IntentStrings.WRITEBOARD,
                IntentStrings.MEETING,
                IntentStrings.POLLS
        ))

        var messageCategoriesForGroup: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL,
                CometChatConstants.CATEGORY_ACTION))


        var messageCategoriesForUser: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL))
    }

    object ErrorTypes {
        const val ERROR = "errorMessage"
        const val WARNING = "warningMessage"
        const val INFO = "infoMessage"
        const val SUCCESS = "successMessage"
    }

    object Errors {
        //AuthError
        const val AUTH_ERR_EMPTY_APPID ="AUTH_ERR_EMPTY_APPID"
        const val AUTH_ERR_INVALID_APPID = "AUTH_ERR_INVALID_APPID"
        const val AUTH_ERR_EMPTY_APIKEY = "AUTH_ERR_EMPTY_APIKEY"
        const val AUTH_ERR_APIKEY_NOT_FOUND = "AUTH_ERR_APIKEY_NOT_FOUND"
        const val AUTH_ERR_NO_ACCESS = "AUTH_ERR_NO_ACCESS"
        const val AUTH_ERR_EMPTY_AUTH_TOKEN = "AUTH_ERR_EMPTY_AUTH_TOKEN"
        const val AUTH_ERR_AUTH_TOKEN_NOT_FOUND = "AUTH_ERR_AUTH_TOKEN_NOT_FOUND"
        //Api related error
        const val ERR_APIKEY_NOT_FOUND = "ERR_APIKEY_NOT_FOUND"
        const val ERR_APIKEY_NO_SELF_ACTION = "ERR_APIKEY_NO_SELF_ACTION"
        //Auth token related error
        const val ERR_AUTH_TOKEN_NOT_FOUND = "ERR_AUTH_TOKEN_NOT_FOUND"
        const val ERR_AUTH_TOKEN_DELETE_FAILED = "ERR_AUTH_TOKEN_DELETE_FAILED"
        const val ERR_AUTH_TOKENS_DELETE_FAILED ="ERR_AUTH_TOKENS_DELETE_FAILED"
        const val ERR_AUTHTOKEN_UNAVAILABLE = "ERR_AUTHTOKEN_UNAVAILABLE"
        const val ERR_AUTHTOKEN_NOT_ACCESSIBLE = "ERR_AUTHTOKEN_NOT_ACCESSIBLE"

        const val ERR_PLAN_RESTRICTION ="ERR_PLAN_RESTRICTION"
        const val ERR_SUBSCRIPTION_EXPIRED = "ERR_SUBSCRIPTION_EXPIRED"
        const val ERR_PLAN_QUOTA_RESTRICTION = "ERR_PLAN_QUOTA_RESTRICTION"

        const val ERR_ROLE_NOT_FOUND = "ERR_ROLE_NOT_FOUND"
        const val ERR_ROLE_DELETE_FAILED = "ERR_ROLE_DELETE_FAILED"
        const val ERR_ROLE_DELETE_DENIED ="ERR_ROLE_DELETE_DENIED"

        const val ERR_UID_NOT_FOUND = "ERR_UID_NOT_FOUND"
        const val ERR_UID_DELETE_FAILED ="ERR_UID_DELETE_FAILED"

        const val ERR_BOT_NOT_FOUND ="ERR_BOT_NOT_FOUND"
        const val ERR_BOT_ALREADY_EXISTS ="ERR_BOT_ALREADY_EXISTS"

        const val ERR_GUID_NOT_FOUND ="ERR_GUID_NOT_FOUND"
        const val ERR_EMPTY_GROUP_PASS ="ERR_EMPTY_GROUP_PASS"
        const val ERR_GROUP_DELETE_FAILED ="ERR_GROUP_DELETE_FAILED"
        const val ERR_NOT_A_MEMBER ="ERR_NOT_A_MEMBER"
        const val ERR_WRONG_GROUP_PASS ="ERR_WRONG_GROUP_PASS"
        const val ERR_ALREADY_JOINED ="ERR_ALREADY_JOINED"
        const val ERR_GROUP_NOT_JOINED ="ERR_GROUP_NOT_JOINED"
        const val ERR_GROUP_JOIN_NOT_ALLOWED ="ERR_GROUP_JOIN_NOT_ALLOWED"
        const val ERR_MEMBER_DELETE_FAILED ="ERR_MEMBER_DELETE_FAILED"
        const val ERR_NO_VACANCY ="ERR_NO_VACANCY"
        const val ERR_SAME_SCOPE ="ERR_SAME_SCOPE"
        const val ERR_MEMBER_SCOPE_CHANGE_FAILED ="ERR_MEMBER_SCOPE_CHANGE_FAILED"
        const val ERR_NOT_A_BANNED_USER ="ERR_NOT_A_BANNED_USER"
        const val ERR_BANNED_GROUPMEMBER = "ERR_BANNED_GROUPMEMBER"
        const val ERR_ALREADY_BANNED ="ERR_ALREADY_BANNED"
        const val ERR_MEMBER_BAN_FAILED ="ERR_MEMBER_BAN_FAILED"
        const val ERR_MEMBER_UNBAN_FAILED = "ERR_MEMBER_UNBAN_FAILED"
        const val ERR_GROUP_NO_CLEARANCE = "ERR_GROUP_NO_CLEARANCE"
        const val ERR_GROUP_NO_ADMIN_SCOPE ="ERR_GROUP_NO_ADMIN_SCOPE"
        const val ERR_GROUP_NO_MODERATOR_SCOPE = "ERR_GROUP_NO_MODERATOR_SCOPE"
        const val ERR_GROUP_NO_SCOPE_CLEARANCE ="ERR_GROUP_NO_SCOPE_CLEARANCE"
        const val ERR_GROUP_NO_SELF_ACTION ="ERR_GROUP_NO_SELF_ACTION"

        const val ERR_EMPTY_RECEIVER ="ERR_EMPTY_RECEIVER"
        const val ERR_INVALID_RECEIVER_TYPE ="ERR_INVALID_RECEIVER_TYPE"
        const val ERR_CONVERSATION_NOT_FOUND ="ERR_CONVERSATION_NOT_FOUND"
        const val ERR_CONVERSATION_NOT_ACCESSIBLE ="ERR_CONVERSATION_NOT_ACCESSIBLE"
        const val ERR_USER_MESSAGE_DELETE_FAILED ="ERR_USER_MESSAGE_DELETE_FAILED"
        const val ERR_MESSAGE_ID_NOT_FOUND ="ERR_MESSAGE_ID_NOT_FOUND"
        const val ERR_INVALID_MESSAGE_DATA ="ERR_INVALID_MESSAGE_DATA"
        const val ERR_EMPTY_MESSAGE_TEXT ="ERR_EMPTY_MESSAGE_TEXT"
        const val ERR_INVALID_MESSAGE_TEXT ="ERR_INVALID_MESSAGE_TEXT"
        const val ERR_EMPTY_MESSAGE_CATEGORY ="ERR_EMPTY_MESSAGE_CATEGORY"
        const val ERR_INVALID_MESSAGE_CATEGORY = "ERR_INVALID_MESSAGE_CATEGORY"
        const val ERR_EMPTY_MESSAGE_TYPE ="ERR_EMPTY_MESSAGE_TYPE"
        const val ERR_EMPTY_MESSAGE_FILE ="ERR_EMPTY_MESSAGE_FILE"
        const val ERR_MESSAGE_NOT_A_SENDER ="ERR_MESSAGE_NOT_A_SENDER"
        const val ERR_MESSAGE_NO_ACCESS ="ERR_MESSAGE_NO_ACCESS"
        const val ERR_MESSAGE_ACTION_NOT_ALLOWED ="ERR_MESSAGE_ACTION_NOT_ALLOWED"
        const val ERR_EMPTY_CUSTOM_DATA ="ERR_EMPTY_CUSTOM_DATA"
        const val ERR_INVALID_MEDIA_MESSAGE ="ERR_INVALID_MEDIA_MESSAGE"
        const val ERR_INVALID_CUSTOM_DATA ="ERR_INVALID_CUSTOM_DATA"
        const val ERR_INVALID_METADATA = "ERR_INVALID_METADATA"
        const val ERR_WRONG_MESSAGE_THREAD ="ERR_WRONG_MESSAGE_THREAD"
        const val ERR_MESSAGE_THREAD_NESTING ="ERR_MESSAGE_THREAD_NESTING"
        const val ERR_WRONG_MESSAGE_THREAD_CATEGORY ="ERR_WRONG_MESSAGE_THREAD_CATEGORY"

        const val ERR_CALLING_SELF ="ERR_CALLING_SELF"
        const val ERR_CALL_BUSY_SELF ="ERR_CALL_BUSY_SELF"
        const val ERR_CALL_BUSY_GROUP="ERR_CALL_BUSY_GROUP"
        const val ERR_CALL_BUSY_USER ="ERR_CALL_BUSY_USER"
        const val ERR_EMPTY_CALL_SESSION_ID ="ERR_EMPTY_CALL_SESSION_ID"
        const val ERR_CALL_SESSION_NOT_FOUND ="ERR_CALL_SESSION_NOT_FOUND"
        const val ERR_CALL_TERMINATED ="ERR_CALL_TERMINATED"
        const val ERR_CALL_GROUP_ALREADY_JOINED ="ERR_CALL_GROUP_ALREADY_JOINED"
        const val ERR_CALL_GROUP_ALREADY_LEFT ="ERR_CALL_GROUP_ALREADY_LEFT"
        const val ERR_CALL_INVALID_INIT ="ERR_CALL_INVALID_INIT"
        const val ERR_CALL_USER_ALREADY_JOINED ="ERR_CALL_USER_ALREADY_JOINED"
        const val ERR_CALL_GROUP_INVALID_STATUS ="ERR_CALL_GROUP_INVALID_STATUS"
        const val ERR_CALL_ONGOING_TO_INVALID ="ERR_CALL_ONGOING_TO_INVALID"
        const val ERR_CALL_NOT_A_PART="ERR_CALL_NOT_A_PART"
        const val ERR_CALL_EMPTY_JOINED_AT ="ERR_CALL_EMPTY_JOINED_AT"
        const val ERR_CALL_NOT_STARTED ="ERR_CALL_NOT_STARTED"

        const val ERR_ALREADY_FRIEND ="ERR_ALREADY_FRIEND"
        const val ERR_NOT_A_FRIEND="ERR_NOT_A_FRIEND"
        const val ERR_CANNOT_FORM_SELF_RELATION ="ERR_CANNOT_FORM_SELF_RELATION"
        const val ERR_FAILED_TO_ADD_FRIEND ="ERR_FAILED_TO_ADD_FRIEND"

        const val ERR_CANNOT_BLOCK_SELF ="ERR_CANNOT_BLOCK_SELF"
        const val ERR_BLOCKED_RECEIVER ="ERR_BLOCKED_RECEIVER"
        const val ERR_BLOCKED_SENDER ="ERR_BLOCKED_SENDER"

        const val ERR_EXTENSION_NOT_FOUND ="ERR_EXTENSION_NOT_FOUND"
        const val ERR_BLOCKED_BY_EXTENSION="ERR_BLOCKED_BY_EXTENSION"

        const val ERR_WEBHOOK_NOT_FOUND ="ERR_WEBHOOK_NOT_FOUND"
        const val ERR_BLOCKED_BY_WEBHOOK ="ERR_BLOCKED_BY_WEBHOOK"
        const val ERR_TRIGGER_DOES_NOT_EXIST ="ERR_TRIGGER_DOES_NOT_EXIST"

        const val ERR_INVALID_API_VERSION ="ERR_INVALID_API_VERSION"
        const val ERR_API_NOT_FOUND ="ERR_API_NOT_FOUND"
        const val ERR_MISSION_FAILED="ERR_MISSION_FAILED"
        const val ERR_BAD_REQUEST ="ERR_BAD_REQUEST"
        const val ERR_OPERATION_FAILED ="ERR_OPERATION_FAILED"
        const val ERR_EXCEPTION ="ERR_EXCEPTION"
        const val ERR_TOO_MANY_REQUESTS ="ERR_TOO_MANY_REQUESTS"
        const val ERR_BAD_ERROR_RESPONSE ="ERR_BAD_ERROR_RESPONSE"

        const val ERR_WS_INIT_FAILED ="ERR_WS_INIT_FAILED"
        const val ERR_WS_APP_INIT_FAILED ="ERR_WS_APP_INIT_FAILED"
        const val ERR_WS_APP_DESTROY_FAILED ="ERR_WS_APP_DESTROY_FAILED"
        const val ERR_WS_ROLE_CREATION_FAILED ="ERR_WS_ROLE_CREATION_FAILED"
        const val ERR_WS_ROLE_DELETION_FAILED ="ERR_WS_ROLE_DELETION_FAILED"
        const val ERR_WS_USER_CREATION_FAILED ="ERR_WS_USER_CREATION_FAILED"
        const val ERR_WS_USER_UPDATION_FAILED ="ERR_WS_USER_UPDATION_FAILED"
        const val ERR_WS_GROUP_CREATION_FAILED ="ERR_WS_GROUP_CREATION_FAILED"
        const val ERR_WS_GROUP_DELETION_FAILED ="ERR_WS_GROUP_DELETION_FAILED"
        const val ERR_WS_GROUP_JOIN_FAILED ="ERR_WS_GROUP_JOIN_FAILED"
        const val ERR_WS_GROUP_LEAVE_FAILED ="ERR_WS_GROUP_LEAVE_FAILED"
        const val ERR_WS_GROUP_MEMBER_MGMT_FAILED ="ERR_WS_GROUP_MEMBER_MGMT_FAILED"
    }
}