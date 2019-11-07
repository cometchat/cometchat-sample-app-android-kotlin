package com.inscripts.cometchatpulse

import android.graphics.Typeface
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Utils.Appearance

class StringContract {

    class AppDetails {

        companion object {

          const val APP_ID: String = "XXXXXXXXXXXXX"

          const val API_KEY: String = "XXXXXXXXXXXX"

          lateinit var theme: Appearance.AppTheme

        }
    }

    class IntentString {

        companion object {

            const val USER_ID: String = "user_id"

            const val USER_NAME: String = "user_name"

            const val USER_AVATAR: String = "user_avatar"

            const val USER_STATUS: String = "user_status"

            const  val LAST_ACTIVE: String = "last_user"

            const val GROUP_ID: String = "group_id"

            const val GROUP_NAME: String = "group_name"

            const val GROUP_ICON: String = "group_icon"

            const val GROUP_OWNER: String = "group_owner"

            const val IMAGE_TYPE = "image/*"

            const val AUDIO_TYPE = "audio/*"

             val DOCUMENT_TYPE = arrayOf("*/*")

             val EXTRA_MIME_TYPE = arrayOf("image/*", "video/*")

            val EXTRA_MIME_DOC = arrayOf("text/plane", "text/html", "application/pdf", "application/msword",
                    "application/vnd.ms.excel", "application/mspowerpoint", "application/zip")

            const val TITLE: String = "title"

            const val POSITION: String = "position"

            const  val SESSION_ID: String = "session_id"

            const  val OUTGOING: String = "outgoing"

            const val INCOMING: String = "incoming"

            const  val RECIVER_TYPE: String = "receiver_type"

            const  val URL: String = "image"

            const  val FILE_TYPE: String = "file_type"

            const  val ID: String = "id"

            const  val GROUP_DESCRIPTION:String="description"

            const  val USER_SCOPE: String="scope"
            
        }
    }

    class ViewType {

        companion object {

            const val  RIGHT_TEXT_MESSAGE = 334

            const  val LEFT_TEXT_MESSAGE = 734

            const val LEFT_IMAGE_MESSAGE = 528

            const val RIGHT_IMAGE_MESSAGE = 834

            const val LEFT_VIDEO_MESSAGE = 580

            const val RIGHT_VIDEO_MESSAGE = 797

            const val RIGHT_AUDIO_MESSAGE = 70

            const val LEFT_AUDIO_MESSAGE = 79

            const val LEFT_FILE_MESSAGE = 24

            const val RIGHT_FILE_MESSAGE = 55

            const val CALL_MESSAGE = 84

            const  val ACTION_MESSAGE = 99

            const val RIGHT_LOCATION_MESSAGE = 58

            const val LEFT_LOCATION_MESSAGE = 59

            const val RIGHT_MEDIA_REPLY_MESSAGE =345

            const val RIGHT_TEXT_REPLY_MESSAGE=346

            const val LEFT_MEDIA_REPLY_MESSAGE =756

            const val LEFT_TEXT_REPLY_MESSAGE=748

        }
    }


    class Font {


        companion object {

            lateinit var title: Typeface

            lateinit var name: Typeface

            lateinit var status: Typeface

            lateinit var message: Typeface

        }

    }

    class Color {

        companion object {

            var primaryColor: Int = 0

            var primaryDarkColor: Int = 0

            var accentColor: Int = 0

            var rightMessageColor = 0

            var leftMessageColor = 0

            var iconTint = 0

            var white: Int = android.graphics.Color.parseColor("#ffffff")

            var black: Int = android.graphics.Color.parseColor("#000000")

            var grey: Int = android.graphics.Color.parseColor("#CACACC")

            var inactiveColor = android.graphics.Color.parseColor("#9e9e9e")

        }

    }

    class Dimensions {

        companion object {

            var marginStart: Int = 16

            var marginEnd: Int = 16

            var cardViewCorner: Float = 24f

            var cardViewElevation: Float = 8f
        }
    }

    class ListenerName {

        companion object {

            const val MESSAGE_LISTENER = "message_listener"

            const val USER_LISTENER = "user_listener"

            const val GROUP_EVENT_LISTENER = "group_event_listener"

            const  val CALL_EVENT_LISTENER = "call_event_listener"
        }

    }


    class RequestPermission {

        companion object {
            val RECORD_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO,
                    CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

            val CAMERA_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA,
                    CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

            val VIDEO_CALL_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA,
                    CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO)

            val STORAGE_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

//            val LOCATION_PERMISSION= arrayOf(CCPermissionHelper.L)
        }
    }

    class RequestCode {

        companion object {

            const val ADD_GALLERY = 1

            const val ADD_DOCUMENT = 2

            const val ADD_SOUND = 3

            const val TAKE_PHOTO = 5

            const val LOCATION = 15

            const val TAKE_VIDEO = 7

            const val LEFT = 8

            const val RECORD_CODE = 10

            const val VIDEO_CALL = 12

            const val VOICE_CALL = 24

            const val FILE_WRITE =25
        }
    }


}