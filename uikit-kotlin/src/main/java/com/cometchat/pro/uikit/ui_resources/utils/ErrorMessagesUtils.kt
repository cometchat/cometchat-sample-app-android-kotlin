package com.cometchat.pro.uikit.ui_resources.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.constants.CometChatConstants.Errors.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class ErrorMessagesUtils {
    companion object {
        public fun cometChatErrorMessage(context: Context?, e : String?) {
            when (e) {
//                SDK errors
                ERROR_INIT_NOT_CALLED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INIT_NOT_CALLED), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_PASSWORD_MISSING -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_PASSWORD_MISSING_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_LIMIT_EXCEEDED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_LIMIT_EXCEEDED_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_USER_NOT_LOGGED_IN -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_USER_NOT_LOGGED_IN_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_GUID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_GUID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }

                ERROR_INVALID_UID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_UID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_BLANK_UID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_BLANK_UID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_UID_WITH_SPACE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_UID_WITH_SPACE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_DEFAULT_MESSAGE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_DEFAULT_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_CALL_NOT_INITIATED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_CALL_NOT_INITIATED_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_CALL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_CALL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_CALL_SESSION_MISMATCH -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_CALL_SESSION_MISMATCH_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INIT_NOT_CALLED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INIT_NOT_CALLED_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_UID_GUID_NOT_SPECIFIED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_UID_GUID_NOT_SPECIFIED_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INTERNET_UNAVAILABLE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INTERNET_UNAVAILABLE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_REQUEST_IN_PROGRESS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_REQUEST_IN_PROGRESS_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }

                ERROR_FILTERS_MISSING -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_FILTERS_MISSING_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_BLANK_AUTHTOKEN -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_BLANK_AUTHTOKEN_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EXTENSION_DISABLED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_DISABLED_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_MESSAGEID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_MESSAGEID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_MESSAGE_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_MESSAGE_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_LIST_EMPTY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_LIST_EMPTY_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_UPDATESONLY_WITHOUT_UPDATEDAFTER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_UPDATESONLY_WITHOUT_UPDATEDAFTER_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_JSON_EXCEPTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_JSON_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_LOGOUT_FAIL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_LOGOUT_FAIL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_APPID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_APPID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_NON_POSITIVE_LIMIT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_NON_POSITIVE_LIMIT_MESSSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_REGION_MISSING -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_REGION_MISSING_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_APP_SETTINGS_NULL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_APP_SETTING_NULL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_API_KEY_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_API_KEY_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_SENDING_MESSAGE_TYPE_MESSAGE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_SENDING_MESSAGE_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_MESSAGE_TEXT_EMPTY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_MESSAGE_TEXT_EMPTY_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_FILE_OBJECT_INVALID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_FILE_OBJECT_INVALID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_FILE_URL_EMPTY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_FILE_URL_EMPTY_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_CUSTOM_DATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_CUSTOM_DATA_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_GROUP_NAME -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_GROUP_NAME_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_GROUP_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_GROUP_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_LOGIN_IN_PROGRESS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_LOGIN_IN_PROGRESS_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_MESSAGE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_MESSAGE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_GROUP -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_GROUP_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_CALL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_CALL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_CALL_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_CALL_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_RECEIVER_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_RECEIVER_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_SESSION_ID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_SESSION_ID_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_ACTIVITY_NULL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_ACTIVITY_NULL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_VIEW_NULL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_VIEW_NULL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }

                ERROR_INVALID_FCM_TOKEN -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FCM_TOKEN_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_GROUP_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_GROUP_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_CALL_IN_PROGRESS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_CALL_IN_PROGRESS_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INCORRECT_INITIATOR -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INCORRECT_INITIATOR_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_USER_NAME -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_USER_NAME_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_USER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_USER_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_GROUP_NAME -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_GROUP_NAME_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_TIMESTAMP -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_TIMESTAMP_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_CATEGORY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_CATEGORY_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_ICON -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_ICON_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_DESCRIPTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_DESCRIPTION_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_METADATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_METADATA_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EMPTY_SCOPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EMPTY_SCOPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_SCOPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_SCOPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_CONVERSATION_WITH-> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_CONVERSATION_WITH_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_CONVERSATION_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_CONVERSATION_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_MEDIA_MESSAGE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_MEDIA_MESSAGE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_ATTACHMENT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_ATTACHMENT_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_FILE_NAME -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FILE_NAME_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_FILE_EXTENSION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FILE_EXTENSION_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_FILE_MIME_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FILE_MIME_TYPE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_FILE_URL -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FILE_URL_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_CONVERSATION_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_CONVERSATION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_SETTINGS_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_SETTINGS_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }

                ERROR_INVALID_FEATURE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_FEATURE_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_INVALID_EXTENSION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_INVALID_EXTENSION_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_FEATURE_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_FEATURE_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }
                ERROR_EXTENSION_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
                }

//                Api Errors
                UIKitConstants.Errors.AUTH_ERR_EMPTY_APPID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_empty_appid), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_INVALID_APPID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_invalid_appid), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_EMPTY_APIKEY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_empty_apikey), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_APIKEY_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_apikey_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_NO_ACCESS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_no_access), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_EMPTY_AUTH_TOKEN -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_empty_auth_token), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.AUTH_ERR_AUTH_TOKEN_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.auth_err_auth_token_not_found), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_APIKEY_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_apikey_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_APIKEY_NO_SELF_ACTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_apikey_no_self_action), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_AUTH_TOKEN_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_auth_token_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_AUTH_TOKEN_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_auth_token_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_AUTH_TOKENS_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_auth_tokens_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_AUTHTOKEN_UNAVAILABLE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_authtoken_unavailable), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_AUTHTOKEN_NOT_ACCESSIBLE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_authtoken_not_accessible), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_PLAN_RESTRICTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_plan_restriction), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_SUBSCRIPTION_EXPIRED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_subscription_expired), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_PLAN_QUOTA_RESTRICTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_plan_quota_restriction), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_ROLE_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_role_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_ROLE_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_role_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_ROLE_DELETE_DENIED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_role_delete_denied), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_UID_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_uid_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_UID_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_uid_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }

//                UIKitConstants.Errors.ERR_BOT_NOT_FOUND -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_guid_not_found), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_BOT_ALREADY_EXISTS -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }

                UIKitConstants.Errors.ERR_GUID_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_guid_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_GROUP_PASS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_group_pass), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_NOT_A_MEMBER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_not_a_member), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_WRONG_GROUP_PASS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_wrong_group_pass), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_ALREADY_JOINED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_already_joined), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NOT_JOINED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_not_joined), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_JOIN_NOT_ALLOWED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_join_not_allowed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MEMBER_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_member_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_NO_VACANCY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_no_vacancy), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_SAME_SCOPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_same_scope), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MEMBER_SCOPE_CHANGE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_member_scope_change_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_NOT_A_BANNED_USER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_not_a_banned_user), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BANNED_GROUPMEMBER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_banned_groupmember), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_ALREADY_BANNED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_already_banned), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MEMBER_BAN_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_member_ban_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MEMBER_UNBAN_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_member_unban_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NO_CLEARANCE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_no_clearance), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NO_ADMIN_SCOPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_no_admin_scope), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NO_MODERATOR_SCOPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_no_moderator_scope), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NO_SCOPE_CLEARANCE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_no_scope_clearance), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_GROUP_NO_SELF_ACTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_group_no_self_action), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_EMPTY_RECEIVER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_receiver), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_RECEIVER_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_receiver_type), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CONVERSATION_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_conversation_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CONVERSATION_NOT_ACCESSIBLE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_conversation_not_accessible), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_USER_MESSAGE_DELETE_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_user_message_delete_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MESSAGE_ID_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_message_id_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_MESSAGE_DATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_message_data), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_MESSAGE_TEXT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_message_text), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_MESSAGE_TEXT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_message_text), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_MESSAGE_CATEGORY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_message_category), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_MESSAGE_CATEGORY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_message_category), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_MESSAGE_TYPE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_message_type), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_MESSAGE_FILE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_message_file), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MESSAGE_NOT_A_SENDER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_message_not_a_sender), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MESSAGE_NO_ACCESS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_message_no_access), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MESSAGE_ACTION_NOT_ALLOWED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_message_action_not_allowed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_CUSTOM_DATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_custom_data), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_MEDIA_MESSAGE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_media_message), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_CUSTOM_DATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_custom_data), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_INVALID_METADATA -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_metadata), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_WRONG_MESSAGE_THREAD -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_wrong_message_thread), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MESSAGE_THREAD_NESTING -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_message_thread_nesting), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_WRONG_MESSAGE_THREAD_CATEGORY -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_wrong_message_thread_category), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_CALLING_SELF -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_calling_self), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_BUSY_SELF -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_busy_self), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_BUSY_GROUP -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_busy_group), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_BUSY_USER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_busy_user), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EMPTY_CALL_SESSION_ID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_empty_call_session_id), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_SESSION_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_session_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_TERMINATED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_terminated), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_GROUP_ALREADY_JOINED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_group_already_joined), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_GROUP_ALREADY_LEFT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_group_already_left), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_INVALID_INIT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_invalid_init), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_USER_ALREADY_JOINED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_user_already_joined), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_GROUP_INVALID_STATUS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_group_invalid_status), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_ONGOING_TO_INVALID -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_ongoing_to_invalid), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_NOT_A_PART -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_not_a_part), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_EMPTY_JOINED_AT -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_empty_joined_at), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_CALL_NOT_STARTED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_call_not_started), UIKitConstants.ErrorTypes.ERROR)
                }

//                UIKitConstants.Errors.ERR_ALREADY_FRIEND -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_NOT_A_FRIEND -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_CANNOT_FORM_SELF_RELATION -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_FAILED_TO_ADD_FRIEND -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }

                UIKitConstants.Errors.ERR_CANNOT_BLOCK_SELF -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_cannot_block_self), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BLOCKED_RECEIVER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_blocked_receiver), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BLOCKED_SENDER -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_blocked_sender), UIKitConstants.ErrorTypes.ERROR)
                }

                UIKitConstants.Errors.ERR_EXTENSION_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_extension_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BLOCKED_BY_EXTENSION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_blocked_by_extension), UIKitConstants.ErrorTypes.ERROR)
                }

//                UIKitConstants.Errors.ERR_WEBHOOK_NOT_FOUND -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_BLOCKED_BY_WEBHOOK -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_TRIGGER_DOES_NOT_EXIST -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }

                UIKitConstants.Errors.ERR_INVALID_API_VERSION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_invalid_api_version), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_API_NOT_FOUND -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_api_not_found), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_MISSION_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_mission_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BAD_REQUEST -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_bad_request), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_OPERATION_FAILED -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_operation_failed), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_EXCEPTION -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_exception), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_TOO_MANY_REQUESTS -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_too_many_requests), UIKitConstants.ErrorTypes.ERROR)
                }
                UIKitConstants.Errors.ERR_BAD_ERROR_RESPONSE -> {
                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.err_bad_error_response), UIKitConstants.ErrorTypes.ERROR)
                }

//                UIKitConstants.Errors.ERR_WS_INIT_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_APP_INIT_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_APP_DESTROY_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_ROLE_CREATION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_ROLE_DELETION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_USER_CREATION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_USER_UPDATION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_GROUP_CREATION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_GROUP_DELETION_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_GROUP_JOIN_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_GROUP_LEAVE_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }
//                UIKitConstants.Errors.ERR_WS_GROUP_MEMBER_MGMT_FAILED -> {
//                    showCometChatErrorDialog(context, context?.resources?.getString(R.string.ERROR_EXTENSION_NOT_FOUND_MESSAGE), UIKitConstants.ErrorTypes.ERROR)
//                }

                else -> {
                    showCometChatErrorDialog(context, e, UIKitConstants.ErrorTypes.ERROR)
                }

            }
        }

        public fun showCometChatErrorDialog (context: Context?, errorMessage : String?, errorType : String) {
            val builder = context?.let { AlertDialog.Builder(it) }
            val dialogView = LayoutInflater.from(context).inflate(R.layout.cometchat_error_message_view, null, false)
            builder?.setView(dialogView)
            dialogView.findViewById<TextView>(R.id.tv_error_message).text = errorMessage
            if (errorType == UIKitConstants.ErrorTypes.ERROR) {
                dialogView.findViewById<LinearLayout>(R.id.ll_background).background = context?.getDrawable(R.color.red_600)
                dialogView.findViewById<ImageView>(R.id.iv_error_icon).setImageResource(R.drawable.error_icon)
            } else if (errorType == UIKitConstants.ErrorTypes.WARNING) {
                dialogView.findViewById<LinearLayout>(R.id.ll_background).background = context?.getDrawable(R.color.orange_200)
                dialogView.findViewById<ImageView>(R.id.iv_error_icon).setImageResource(R.drawable.warning_icon)
            } else if (errorType == UIKitConstants.ErrorTypes.INFO) {
                dialogView.findViewById<LinearLayout>(R.id.ll_background).background = context?.getDrawable(R.color.blue_100)
                dialogView.findViewById<ImageView>(R.id.iv_error_icon).setImageResource(R.drawable.info_icon)
            } else if (errorType == UIKitConstants.ErrorTypes.SUCCESS) {
                dialogView.findViewById<LinearLayout>(R.id.ll_background).background = context?.getDrawable(R.color.green)
                dialogView.findViewById<ImageView>(R.id.iv_error_icon).setImageResource(R.drawable.success_icon)
            }

            val alertDialog = builder?.create()
            alertDialog?.window?.setGravity(Gravity.TOP)
            alertDialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialogView.findViewById<ImageView>(R.id.iv_error_close).setOnClickListener(View.OnClickListener {
                alertDialog?.dismiss()
            })
            alertDialog?.show()
        }

    }

}