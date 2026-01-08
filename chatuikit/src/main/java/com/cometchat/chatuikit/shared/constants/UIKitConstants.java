package com.cometchat.chatuikit.shared.constants;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.enums.ModerationStatus;

public final class UIKitConstants {
    private static final String TAG = UIKitConstants.class.getSimpleName();

    private UIKitConstants() {
    }

    public static final class MessageHeaderMenuOptions {
        public static final String SEARCH = "search";
        public static final String CONVERSATION_SUMMARY = "conversation_summary";
        public static final String DETAILS = "details";
    }

    public enum SearchMode {
        MESSAGES, CONVERSATIONS, BOTH, NONE
    }

    public enum MentionsType {
        USERS, USERS_AND_GROUP_MEMBERS
    }

    public enum CallWorkFlow {
        MEETING, DEFAULT
    }

    public enum MentionsVisibility {
        USERS_CONVERSATION_ONLY, GROUP_CONVERSATION_ONLY, BOTH
    }

    public enum FormattingType {
        MESSAGE_BUBBLE, MESSAGE_COMPOSER, CONVERSATIONS
    }

    public enum SelectionMode {
        NONE, SINGLE, MULTIPLE
    }

    public enum MessageListAlignment {
        LEFT_ALIGNED, STANDARD
    }

    public enum MessageBubbleAlignment {
        RIGHT, LEFT, CENTER
    }

    public enum TimeStampAlignment {
        TOP, BOTTOM
    }

    public enum AuxiliaryButtonAlignment {
        LEFT, RIGHT
    }

    public enum States {
        LOADING, LOADED, ERROR, EMPTY, NON_EMPTY, INITIAL
    }

    public enum ContactsVisibilityMode {
        USER, GROUP, USER_AND_GROUP
    }

    public enum TimeFormat {
        TWELVE_HOUR, TWENTY_FOUR_HOUR
    }

    public enum DateTimeMode {
        DATE, TIME, DATE_TIME
    }

    public enum DeleteState {
        INITIATED_DELETE, SUCCESS_DELETE, FAILURE_DELETE
    }

    public enum FlagMessageState {
        INITIATED_FLAG, SUCCESS_FLAG, FAILURE_FLAG
    }

    public enum DialogState {
        INITIATED, SUCCESS, FAILURE
    }

    public enum CustomUIPosition {
        COMPOSER_TOP, COMPOSER_BOTTOM, MESSAGE_LIST_TOP, MESSAGE_LIST_BOTTOM
    }

    public enum SearchFilter {
        MESSAGES("messages"),
        CONVERSATIONS("conversations"),
        UNREAD("unread"),
        GROUPS("groups"),
        PHOTOS("photos"),
        VIDEOS("videos"),
        LINKS("links"),
        DOCUMENTS("files"),
        AUDIO("audio");

        private final String value;

        SearchFilter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public static final class SharedPreferencesKeys {
        public static final String CALL = "initiated_call";
        public static final String CALL_MESSAGE = "call_message";
    }

    public static final class ViewTag {
        public static final String INTERNAL_HEADER_VIEW = "internal_header_view";
        public static final String INTERNAL_STATUS_INFO_VIEW = "internal_status_info_view";
        public static final String INTERNAL_THREAD_VIEW = "internal_thread_view";
        public static final String INTERNAL_LEADING_VIEW = "internal_leading_view";
        public static final String INTERNAL_BOTTOM_VIEW = "internal_bottom_view";
    }

    public static final class IntentStrings {

        public static final String UID = "uid";

        public static final String NAME = "name";

        public static final String[] EXTRA_MIME_DOC = new String[]{"text/plane", "image/*", "video/*", "text/html", "application/pdf", "application/msword", "application/vnd.ms.excel", "application/mspowerpoint", "application/docs", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip"};

        public static final String SENT_AT = "sent_at";

        public static final String MESSAGE_TYPE = "message_type";

        public static final String INTENT_MEDIA_MESSAGE = "intent_media_message";

        public static final String URL = "url";
        public static final String TITLE = "title";

        public static final String MEDIA_SIZE = "media_size";

        public static final String STORE_INSTANCE = "store_instance";

        public static final String PATH = "path";
    }

    public static final class JSONKeys {
        public static final String METADATA = "metadata";
        public static final String CUSTOM_DATA = "customData";

        public static final String INFO_TEXT = "infoText";
    }

    public static final class MimeType {

        public static final String VIDEO = "video";
        public static final String OCTET_STREAM = "application/octet-stream";
        public static final String AUDIO = "audio/mpeg";
        public static final String PDF = "pdf";
        public static final String ZIP = "zip";
        public static final String IMAGE = "image";
        public static final String CSV = "csv";
        public static final String RTF = "text/rtf";
        public static final String DOC = "doc";
        public static final String XLS = "xls";
        public static final String PPT = "ppt";
        public static final String TEXT = "text";
        public static final String LINK = "link";

        // Text types
        public static final String MIME_CSV = "text/comma-separated-values";
        public static final String MIME_RTF = "text/rtf";
        public static final String MIME_DOC = "application/msword";
        public static final String MIME_XLS = "application/vnd.ms-excel";
        public static final String MIME_PPT = "application/vnd.ms-powerpoint";
        public static final String MIME_PDF = "application/pdf";

        // Compressed types
        public static final String MIME_ZIP = "application/zip";
        public static final String MIME_ODP = "application/vnd.oasis.opendocument.presentation";
        public static final String MIME_ODS = "application/vnd.oasis.opendocument.spreadsheet";
        public static final String MIME_ODT = "application/vnd.oasis.opendocument.text";

        // Video types
        public static final String MIME_MP4_VIDEO = "video/mp4";

        // Audio types
        public static final String MIME_MP3_AUDIO = "audio/mp3";
        public static final String MIME_MPEG_AUDIO = "audio/mpeg";

        // Image types
        public static final String MIME_JPEG_IMAGE = "image/jpeg";
        public static final String MIME_PNG_IMAGE = "image/png";

        // Default (unknown)
        public static final String MIME_UNKNOWN = "unknown";
    }

    public static final class MapId {
        public static String PARENT_MESSAGE_ID = "parentMessageID";

        public static String RECEIVER_ID = "receiverID";

        public static String RECEIVER_TYPE = "receiverType";
    }

    public static final class CallOption {
        public static final String PARTICIPANTS = "participants";
        public static final String RECORDING = "recording";
        public static final String CALL_HISTORY = "callHistory";
    }

    public static final class GroupMemberOption {
        public static final String KICK = "kick";
        public static final String BAN = "ban";
        public static final String UNBAN = "unban";
        public static final String CHANGE_SCOPE = "changeScope";
    }

    public static final class UserStatus {
        public static final String ONLINE = CometChatConstants.USER_STATUS_ONLINE;
        public static final String OFFLINE = CometChatConstants.USER_STATUS_OFFLINE;
    }

    public static final class ConversationOption {
        public static final String DELETE = "delete";
    }

    public static final class ConversationType {
        public static final String USERS = CometChatConstants.RECEIVER_TYPE_USER;
        public static final String GROUPS = CometChatConstants.RECEIVER_TYPE_GROUP;
        public static final String BOTH = "both";
    }

    public static final class GroupType {
        public static final String PRIVATE = CometChatConstants.GROUP_TYPE_PRIVATE;
        public static final String PASSWORD = CometChatConstants.GROUP_TYPE_PASSWORD;
        public static final String PUBLIC = CometChatConstants.GROUP_TYPE_PUBLIC;
    }

    public static final class GroupMemberScope {
        public static final String ADMIN = CometChatConstants.SCOPE_ADMIN;
        public static final String MODERATOR = CometChatConstants.SCOPE_MODERATOR;
        public static final String PARTICIPANTS = CometChatConstants.SCOPE_PARTICIPANT;
    }

    public static final class MessageCategory {
        public static final String MESSAGE = CometChatConstants.CATEGORY_MESSAGE;
        public static final String CUSTOM = CometChatConstants.CATEGORY_CUSTOM;
        public static final String INTERACTIVE = CometChatConstants.CATEGORY_INTERACTIVE;
        public static final String ACTION = CometChatConstants.CATEGORY_ACTION;
        public static final String CALL = CometChatConstants.CATEGORY_CALL;
        public static final String STREAM = "stream_message";
    }

    public static final class MessageType {
        public static final String TEXT = CometChatConstants.MESSAGE_TYPE_TEXT;
        public static final String FILE = CometChatConstants.MESSAGE_TYPE_FILE;
        public static final String IMAGE = CometChatConstants.MESSAGE_TYPE_IMAGE;
        public static final String AUDIO = CometChatConstants.MESSAGE_TYPE_AUDIO;
        public static final String VIDEO = CometChatConstants.MESSAGE_TYPE_VIDEO;
        public static final String STREAM = "ai_assistant_stream";
        public static final String SCHEDULER = "scheduler";
        public static final String MEETING = "meeting";
        public static final String FORM = "form";
        public static final String CUSTOM = "custom";
        public static final String CUSTOM_INTERACTIVE = "customInteractive";
        public static final String CARD = "card";
    }

    public static final class MessageTemplateId {
        public static final String TEXT = CometChatConstants.CATEGORY_MESSAGE + "_" + CometChatConstants.MESSAGE_TYPE_TEXT;
        public static final String FILE = CometChatConstants.CATEGORY_MESSAGE + "_" + CometChatConstants.MESSAGE_TYPE_FILE;
        public static final String IMAGE = CometChatConstants.CATEGORY_MESSAGE + "_" + CometChatConstants.MESSAGE_TYPE_IMAGE;
        public static final String AUDIO = CometChatConstants.CATEGORY_MESSAGE + "_" + CometChatConstants.MESSAGE_TYPE_AUDIO;
        public static final String VIDEO = CometChatConstants.CATEGORY_MESSAGE + "_" + CometChatConstants.MESSAGE_TYPE_VIDEO;
        public static final String GROUP_ACTION = CometChatConstants.CATEGORY_ACTION + "_" + CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER;
        public static final String FORM = CometChatConstants.CATEGORY_INTERACTIVE + "_" + MessageType.FORM;
        public static final String SCHEDULER = CometChatConstants.CATEGORY_INTERACTIVE + "_" + MessageType.SCHEDULER;
        public static final String CARD = CometChatConstants.CATEGORY_INTERACTIVE + "_" + MessageType.CARD;
        public static final String ASSISTANT = "agentic" + "_" + "assistant";
        public static final String CUSTOM_INTERACTIVE = CometChatConstants.CATEGORY_INTERACTIVE + "_" + MessageType.CUSTOM_INTERACTIVE;
        public static final String EXTENSION_POLL = "extension_poll";
        public static final String EXTENSION_STICKER = "extension_sticker";
        public static final String EXTENSION_DOCUMENT = "extension_document";
        public static final String EXTENSION_WHITEBOARD = "extension_whiteboard";
        public static final String EXTENSION_MEETING = "meeting";
        public static final String EXTENSION_LOCATION = "location";
    }

    public static final class CallStatusConstants {
        public static final String INITIATED = CometChatConstants.CALL_STATUS_INITIATED;
        public static final String ONGOING = CometChatConstants.CALL_STATUS_ONGOING;
        public static final String REJECTED = CometChatConstants.CALL_STATUS_REJECTED;
        public static final String CANCELLED = CometChatConstants.CALL_STATUS_CANCELLED;
        public static final String BUSY = CometChatConstants.CALL_STATUS_BUSY;
        public static final String UNANSWERED = CometChatConstants.CALL_STATUS_UNANSWERED;
        public static final String ENDED = CometChatConstants.CALL_STATUS_ENDED;
    }

    public static final class ComposerAction {
        public static final String CAMERA = "camera";
        public static final String IMAGE = "image";
        public static final String VIDEO = "video";
        public static final String AUDIO = "audio";
        public static final String DOCUMENT = "document";
    }

    public static final class ReceiverType {
        public static final String USER = CometChatConstants.RECEIVER_TYPE_USER;
        public static final String GROUP = CometChatConstants.RECEIVER_TYPE_GROUP;
    }

    public static final class MessageOption {
        public static final String EDIT = "edit";
        public static final String DELETE = "delete";
        public static final String REPLY = "reply";
        public static final String FORWARD = "forward";
        public static final String REPLY_PRIVATELY = "reply_privately";
        public static final String MESSAGE_PRIVATELY = "message_privately";
        public static final String COPY = "copy";
        public static final String TRANSLATE = "translate";
        public static final String MESSAGE_INFORMATION = "message_information";
        public static final String SHARE = "share";
        public static final String REPLY_IN_THREAD = "reply_in_thread";
        public static final String REPLY_TO_MESSAGE = "reply_to_message";
        public static final String REPORT = "report";
        public static final String MARK_UNREAD = "mark_unread";
    }

    public static final class files {
        public static final String OPEN = "open";
        public static final String SHARE = "share";
    }

    public static final class SchedulerConstants {
        public static final String AVAILABLE = "available";
        public static final String OCCUPIED = "occupied";
    }

    public static final class UIElementsType {
        public static final String UI_ELEMENT_TEXT_INPUT = "textInput";
        public static final String UI_ELEMENT_BUTTON = "button";
        public static final String UI_ELEMENT_CHECKBOX = "checkbox";
        public static final String UI_ELEMENT_SPINNER = "dropdown";
        public static final String UI_ELEMENT_LABEL = "label";
        public static final String UI_ELEMENT_RADIO_BUTTON = "radio";
        public static final String UI_ELEMENT_SINGLE_SELECT = "singleSelect";
        public static final String UI_ELEMENT_DATE_TIME = "dateTime";
    }

    public static final class CallingJSONConstants {
        public static final String CALL_TYPE = "callType";
        public static final String CALL_SESSION_ID = "sessionID";
    }

    public static final class ModerationConstants {
        public static final ModerationStatus UNMODERATED = ModerationStatus.UNMODERATED;
        public static final ModerationStatus PENDING = ModerationStatus.PENDING;
        public static final ModerationStatus APPROVED = ModerationStatus.APPROVED;
        public static final ModerationStatus DISAPPROVED = ModerationStatus.DISAPPROVED;
    }

    public static final class AIAssistantEventType {
        public static final String RUN_STARTED = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_RUN_STARTED;
        public static final String RUN_FINISHED = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_RUN_FINISHED;
        public static final String TOOL_CALL_START = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_TOOL_CALL_STARTED;
        public static final String TOOL_CALL_END = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_TOOL_CALL_ENDED;
        public static final String TEXT_MESSAGE_START = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_TEXT_MESSAGE_START;
        public static final String TEXT_MESSAGE_END = CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_TEXT_MESSAGE_END;
    }

    public static final class AIConstants {
        public static final String AGENTIC_USER = "@agentic";
        public static final String AI_ASSISTANT_EVENT_TYPE = "ai_assistant_event_type";
    }

    public static final class AIAssistantJsonConstants {
        public static final String SUGGESTED_MESSAGES = "suggestedMessages";
        public static final String GREETING_MESSAGE = "greetingMessage";
        public static final String INTRODUCTORY_MESSAGE = "introductoryMessage";
    }
}
