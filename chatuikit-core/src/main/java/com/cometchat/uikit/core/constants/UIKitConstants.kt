package com.cometchat.uikit.core.constants

/**
 * UIKit constants and enums for configuration and settings.
 */
object UIKitConstants {

    /**
     * Enum defining the scope for search operations.
     */
    enum class SearchScope(val value: String) {
        /**
         * Search within conversations.
         */
        CONVERSATIONS("conversations"),

        /**
         * Search within messages.
         */
        MESSAGES("messages");

        override fun toString(): String = value
    }

    /**
     * Defines the status values for a message.
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.LOCAL_VARIABLE)
    annotation class MessageStatus {
        companion object {
            /**
             * The message is in progress.
             */
            const val IN_PROGRESS = 0

            /**
             * The message was successfully sent.
             */
            const val SUCCESS = 1

            /**
             * An error occurred while sending the message.
             */
            const val ERROR = -1
        }
    }

    /**
     * Message header menu options.
     */
    object MessageHeaderMenuOptions {
        const val SEARCH = "search"
        const val CONVERSATION_SUMMARY = "conversation_summary"
        const val DETAILS = "details"
    }

    /**
     * Enum defining search modes.
     */
    enum class SearchMode {
        MESSAGES, CONVERSATIONS, BOTH, NONE
    }

    /**
     * Enum defining mentions types.
     */
    enum class MentionsType {
        USERS, USERS_AND_GROUP_MEMBERS
    }

    /**
     * Enum defining call workflows.
     */
    enum class CallWorkFlow {
        MEETING, DEFAULT
    }

    /**
     * Enum defining mentions visibility.
     */
    enum class MentionsVisibility {
        USERS_CONVERSATION_ONLY, GROUP_CONVERSATION_ONLY, BOTH
    }

    /**
     * Enum defining formatting types.
     */
    enum class FormattingType {
        MESSAGE_BUBBLE, MESSAGE_COMPOSER, CONVERSATIONS
    }

    /**
     * Enum defining selection modes.
     */
    enum class SelectionMode {
        NONE, SINGLE, MULTIPLE
    }

    /**
     * Controls the overall alignment of messages in the list.
     */
    enum class MessageListAlignment {
        /**
         * Standard alignment: outgoing messages on right, incoming on left.
         */
        STANDARD,

        /**
         * All messages aligned to the left.
         */
        LEFT_ALIGNED
    }

    /**
     * Enum defining message bubble alignments.
     */
    enum class MessageBubbleAlignment {
        RIGHT, LEFT, CENTER
    }

    /**
     * Controls where the timestamp is displayed in message bubbles.
     */
    enum class TimeStampAlignment {
        /**
         * Display timestamp in the header view alongside sender name.
         */
        TOP,

        /**
         * Display timestamp in the status info view alongside receipt indicator.
         */
        BOTTOM
    }

    /**
     * Enum defining auxiliary button alignment.
     */
    enum class AuxiliaryButtonAlignment {
        LEFT, RIGHT
    }

    /**
     * Enum defining states.
     */
    enum class States {
        LOADING, LOADED, ERROR, EMPTY, NON_EMPTY, INITIAL
    }

    /**
     * Enum defining contacts visibility mode.
     */
    enum class ContactsVisibilityMode {
        USER, GROUP, USER_AND_GROUP
    }

    /**
     * Enum defining time formats.
     */
    enum class TimeFormat {
        TWELVE_HOUR, TWENTY_FOUR_HOUR
    }

    /**
     * Enum defining date-time modes.
     */
    enum class DateTimeMode {
        DATE, TIME, DATE_TIME
    }

    /**
     * Enum defining delete states.
     */
    enum class DeleteState {
        INITIATED_DELETE, SUCCESS_DELETE, FAILURE_DELETE
    }

    /**
     * Enum defining flag message states.
     */
    enum class FlagMessageState {
        INITIATED_FLAG, SUCCESS_FLAG, FAILURE_FLAG
    }

    /**
     * Enum defining dialog states.
     */
    enum class DialogState {
        INITIATED, SUCCESS, FAILURE
    }

    /**
     * Enum defining custom UI positions.
     */
    enum class CustomUIPosition {
        COMPOSER_TOP, COMPOSER_BOTTOM, MESSAGE_LIST_TOP, MESSAGE_LIST_BOTTOM
    }

    /**
     * Enum defining search filters.
     */
    enum class SearchFilter(val value: String) {
        MESSAGES("messages"),
        CONVERSATIONS("conversations"),
        UNREAD("unread"),
        GROUPS("groups"),
        PHOTOS("photos"),
        VIDEOS("videos"),
        LINKS("links"),
        DOCUMENTS("files"),
        AUDIO("audio");

        override fun toString(): String = value
    }

    /**
     * Shared preferences keys.
     */
    object SharedPreferencesKeys {
        const val CALL = "initiated_call"
        const val CALL_MESSAGE = "call_message"
    }

    /**
     * View tags.
     */
    object ViewTag {
        const val INTERNAL_HEADER_VIEW = "internal_header_view"
        const val INTERNAL_STATUS_INFO_VIEW = "internal_status_info_view"
        const val INTERNAL_THREAD_VIEW = "internal_thread_view"
        const val INTERNAL_LEADING_VIEW = "internal_leading_view"
        const val INTERNAL_BOTTOM_VIEW = "internal_bottom_view"
    }

    /**
     * Intent string constants.
     */
    object IntentStrings {
        const val UID = "uid"
        const val NAME = "name"
        val EXTRA_MIME_DOC = arrayOf(
            "text/plane",
            "image/*",
            "video/*",
            "text/html",
            "application/pdf",
            "application/msword",
            "application/vnd.ms.excel",
            "application/mspowerpoint",
            "application/docs",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip"
        )
        const val SENT_AT = "sent_at"
        const val MESSAGE_TYPE = "message_type"
        const val INTENT_MEDIA_MESSAGE = "intent_media_message"
        const val URL = "url"
        const val TITLE = "title"
        const val MEDIA_SIZE = "media_size"
        const val STORE_INSTANCE = "store_instance"
        const val PATH = "path"
    }

    /**
     * JSON keys.
     */
    object JSONKeys {
        const val METADATA = "metadata"
        const val CUSTOM_DATA = "customData"
        const val INFO_TEXT = "infoText"
        const val INJECTED = "@injected"
        const val EXTENSIONS = "extensions"
        const val LINK_PREVIEW = "link-preview"
        const val LINKS = "links"
    }

    /**
     * MIME types.
     */
    object MimeType {
        const val VIDEO = "video"
        const val OCTET_STREAM = "application/octet-stream"
        const val AUDIO = "audio/mpeg"
        const val PDF = "pdf"
        const val ZIP = "zip"
        const val IMAGE = "image"
        const val CSV = "csv"
        const val RTF = "text/rtf"
        const val DOC = "doc"
        const val XLS = "xls"
        const val PPT = "ppt"
        const val TEXT = "text"
        const val LINK = "link"
        const val GIF_EXTENSION = ".gif"

        // Text types
        const val MIME_CSV = "text/comma-separated-values"
        const val MIME_RTF = "text/rtf"
        const val MIME_DOC = "application/msword"
        const val MIME_XLS = "application/vnd.ms-excel"
        const val MIME_PPT = "application/vnd.ms-powerpoint"
        const val MIME_PDF = "application/pdf"

        // Compressed types
        const val MIME_ZIP = "application/zip"
        const val MIME_ODP = "application/vnd.oasis.opendocument.presentation"
        const val MIME_ODS = "application/vnd.oasis.opendocument.spreadsheet"
        const val MIME_ODT = "application/vnd.oasis.opendocument.text"

        // Video types
        const val MIME_MP4_VIDEO = "video/mp4"

        // Audio types
        const val MIME_MP3_AUDIO = "audio/mp3"
        const val MIME_MPEG_AUDIO = "audio/mpeg"

        // Image types
        const val MIME_JPEG_IMAGE = "image/jpeg"
        const val MIME_PNG_IMAGE = "image/png"

        // Default (unknown)
        const val MIME_UNKNOWN = "unknown"
    }

    /**
     * Map IDs.
     */
    object MapId {
        const val PARENT_MESSAGE_ID = "parentMessageID"
        const val RECEIVER_ID = "receiverID"
        const val RECEIVER_TYPE = "receiverType"
    }

    /**
     * Call options.
     */
    object CallOption {
        const val PARTICIPANTS = "participants"
        const val RECORDING = "recording"
        const val CALL_HISTORY = "callHistory"
    }

    /**
     * Group member options.
     */
    object GroupMemberOption {
        const val KICK = "kick"
        const val BAN = "ban"
        const val UNBAN = "unban"
        const val CHANGE_SCOPE = "changeScope"
    }

    /**
     * User status constants.
     */
    object UserStatus {
        const val ONLINE = "online"
        const val OFFLINE = "offline"
    }

    /**
     * Conversation options.
     */
    object ConversationOption {
        const val DELETE = "delete"
    }

    /**
     * Conversation types.
     */
    object ConversationType {
        const val USERS = "user"
        const val GROUPS = "group"
        const val BOTH = "both"
    }

    /**
     * Group types.
     */
    object GroupType {
        const val PRIVATE = "private"
        const val PASSWORD = "password"
        const val PUBLIC = "public"
    }

    /**
     * Group member scopes.
     */
    object GroupMemberScope {
        const val ADMIN = "admin"
        const val MODERATOR = "moderator"
        const val PARTICIPANTS = "participant"
    }

    /**
     * Message categories.
     */
    object MessageCategory {
        const val MESSAGE = "message"
        const val CUSTOM = "custom"
        const val INTERACTIVE = "interactive"
        const val ACTION = "action"
        const val CALL = "call"
        const val STREAM = "stream_message"
    }

    /**
     * Message types.
     */
    object MessageType {
        const val TEXT = "text"
        const val FILE = "file"
        const val IMAGE = "image"
        const val AUDIO = "audio"
        const val VIDEO = "video"
        const val STREAM = "ai_assistant_stream"
        const val MEETING = "meeting"
        const val CUSTOM = "custom"
        const val EXTENSION_POLL = "extension_poll"
        const val EXTENSION_STICKER = "extension_sticker"
        const val EXTENSION_DOCUMENT = "extension_document"
        const val EXTENSION_WHITEBOARD = "extension_whiteboard"
        const val EXTENSION_MEETING = "meeting"
    }

    /**
     * Message template IDs.
     */
    object MessageTemplateId {
        const val TEXT = "message_text"
        const val FILE = "message_file"
        const val IMAGE = "message_image"
        const val AUDIO = "message_audio"
        const val VIDEO = "message_video"
        const val GROUP_ACTION = "action_group_member"
        const val FORM = "interactive_form"
        const val SCHEDULER = "interactive_scheduler"
        const val CARD = "interactive_card"
        const val ASSISTANT = "agentic_assistant"
        const val CUSTOM_INTERACTIVE = "interactive_customInteractive"
        const val EXTENSION_POLL = "extension_poll"
        const val EXTENSION_STICKER = "extension_sticker"
        const val EXTENSION_DOCUMENT = "extension_document"
        const val EXTENSION_WHITEBOARD = "extension_whiteboard"
        const val EXTENSION_MEETING = "meeting"
        const val EXTENSION_LOCATION = "location"
    }

    /**
     * Call status constants.
     */
    object CallStatusConstants {
        const val INITIATED = "initiated"
        const val ONGOING = "ongoing"
        const val REJECTED = "rejected"
        const val CANCELLED = "cancelled"
        const val BUSY = "busy"
        const val UNANSWERED = "unanswered"
        const val ENDED = "ended"
    }

    /**
     * Composer actions.
     */
    object ComposerAction {
        const val CAMERA = "camera"
        const val IMAGE = "image"
        const val VIDEO = "video"
        const val AUDIO = "audio"
        const val DOCUMENT = "document"
    }

    /**
     * Receiver types.
     */
    object ReceiverType {
        const val USER = "user"
        const val GROUP = "group"
    }

    /**
     * Message options.
     */
    object MessageOption {
        const val EDIT = "edit"
        const val DELETE = "delete"
        const val REPLY = "reply"
        const val FORWARD = "forward"
        const val REPLY_PRIVATELY = "reply_privately"
        const val MESSAGE_PRIVATELY = "message_privately"
        const val COPY = "copy"
        const val TRANSLATE = "translate"
        const val MESSAGE_INFORMATION = "message_information"
        const val SHARE = "share"
        const val REPLY_IN_THREAD = "reply_in_thread"
        const val REPLY_TO_MESSAGE = "reply_to_message"
        const val REPORT = "report"
        const val MARK_AS_UNREAD = "mark_as_unread"
        const val REACT = "react"
    }

    /**
     * File operations.
     */
    object Files {
        const val OPEN = "open"
        const val SHARE = "share"
    }

    /**
     * Scheduler constants.
     */
    object SchedulerConstants {
        const val AVAILABLE = "available"
        const val OCCUPIED = "occupied"
    }

    /**
     * UI elements types.
     */
    object UIElementsType {
        const val UI_ELEMENT_TEXT_INPUT = "textInput"
        const val UI_ELEMENT_BUTTON = "button"
        const val UI_ELEMENT_CHECKBOX = "checkbox"
        const val UI_ELEMENT_SPINNER = "dropdown"
        const val UI_ELEMENT_LABEL = "label"
        const val UI_ELEMENT_RADIO_BUTTON = "radio"
        const val UI_ELEMENT_SINGLE_SELECT = "singleSelect"
        const val UI_ELEMENT_DATE_TIME = "dateTime"
    }

    /**
     * Calling JSON constants.
     */
    object CallingJSONConstants {
        const val CALL_TYPE = "callType"
        const val CALL_SESSION_ID = "sessionID"
    }

    /**
     * Moderation constants.
     */
    object ModerationConstants {
        const val UNMODERATED = "unmoderated"
        const val PENDING = "pending"
        const val APPROVED = "approved"
        const val DISAPPROVED = "disapproved"
    }

    /**
     * AI Assistant event types.
     */
    object AIAssistantEventType {
        const val RUN_STARTED = "run_started"
        const val RUN_FINISHED = "run_finished"
        const val TOOL_CALL_START = "tool_call_started"
        const val TOOL_CALL_END = "tool_call_ended"
        const val TEXT_MESSAGE_START = "text_message_start"
        const val TEXT_MESSAGE_END = "text_message_end"
    }

    /**
     * AI constants.
     */
    object AIConstants {
        const val AGENTIC_USER = "@agentic"
        const val AI_ASSISTANT_EVENT_TYPE = "ai_assistant_event_type"
    }

    /**
     * AI Assistant JSON constants.
     */
    object AIAssistantJsonConstants {
        const val SUGGESTED_MESSAGES = "suggestedMessages"
        const val GREETING_MESSAGE = "greetingMessage"
        const val INTRODUCTORY_MESSAGE = "introductoryMessage"
    }

    /**
     * Utility constants for UIKit operations.
     */
    object UIKitUtilityConstants {
        /**
         * Composer search query debounce interval in milliseconds.
         */
        const val COMPOSER_SEARCH_QUERY_INTERVAL = 500

        /**
         * Composer operation debounce interval in milliseconds.
         */
        const val COMPOSER_OPERATION_INTERVAL = 200

        /**
         * Typing indicator debounce interval in milliseconds.
         */
        const val TYPING_INDICATOR_DEBOUNCER = 1000
    }
}