package constant;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.uikit.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringContract {

    public static class AppInfo {
        public static String API_KEY = "";

        public static String APP_ID = "";
    }
    public static class IntentStrings {

        public static final String IMAGE_TYPE = "image/*";

        public static final String UID="uid";

        public static final String AVATAR="avatar";

        public static final String STATUS="status";

        public static final String NAME = "name";

        public static final String TYPE = "type";

        public static final String GUID = "guid";

        public static final String tabBar = "tabBar";

        public static final String[] EXTRA_MIME_DOC=new String[]{"text/plane","text/html","application/pdf","application/msword","application/vnd.ms.excel", "application/mspowerpoint","application/zip"};

        public static final String MEMBER_COUNT = "member_count";

        public static final String GROUP_MEMBER = "group_members";

        public static final String GROUP_NAME = "group_name";

        public static final String MEMBER_SCOPE = "member_scope";

        public static final String GROUP_OWNER = "group_owner";

        public static final String ID = "id";

        public static final String IS_ADD_MEMBER ="is_add_member" ;

        public static final String IS_BLOCKED_BY_ME = "is_blocked_by_me";

        public static final String SESSION_ID = "sessionId";

        public static final String INCOMING = "incoming";

        public static final String FROM_CALL_LIST = "from_call_list" ;

        public static final String JOIN_ONGOING = "join_ongoing_call";

        public static final String MESSAGE_TYPE_IMAGE_NAME = "file_name" ;

        public static final String MESSAGE_TYPE_IMAGE_URL = "file_url" ;

        public static final String MESSAGE_TYPE_IMAGE_MIME_TYPE = "file_mime";

        public static final String MESSAGE_TYPE_IMAGE_EXTENSION = "file_extension";

        public static final String MESSAGE_TYPE_IMAGE_SIZE = "file_size";

        public static final String SHOW_MODERATORLIST = "is_moderator";

        public static final String GROUP_DESC = "group_description";

        public static final String GROUP_PASSWORD = "group_password" ;

        public static final String GROUP_TYPE = "group_type" ;

        public static final String TEXTMESSAGE = "text_message";

        public static final String SENTAT = "sent_at";

        public static final String MESSAGE_TYPE = "message_type" ;

        public static final String PARENT_ID = "parent_id";

        public static final String REPLY_COUNT = "reply_count";

        public static final String CONVERSATION_NAME = "conversation_name";

        public static final String INTENT_MEDIA_MESSAGE = "intent_media_message" ;

        public static final String IMAGE_MODERATION = "image_moderation" ;

        public static final String CUSTOM_MESSAGE = "custom_message" ;

        public static final String LOCATION = "LOCATION" ;

        public static final String LOCATION_LATITUDE = "latitude";

        public static final String LOCATION_LONGITUDE = "longitude" ;

        public static final String MESSAGE_CATEGORY = "message_category";

        public static final String PARENT_BASEMESSAGE = "parent_baseMessage";

        public static final String POLL_VOTE_COUNT = "poll_vote_count";

        public static final String POLLS = "extension_poll";

        public static final String TRANSFER_OWNERSHIP = "transfer_ownership";

        public static final String STICKERS = "Sticker";

        public static final String REACTION_INFO = "reaction_info";

        public static String POLL_QUESTION = "poll_question";

        public static String POLL_OPTION = "poll_option";

        public static String POLL_RESULT = "poll_result";

        public static String POLL_ID = "poll_id";

        public static final String MEDIA_SIZE = "media_size" ;
    }

    public static class Tab {
        public static final String Conversation = "conversations";

        public static final String User = "users";

        public static final String Group = "groups";
    }
    public static class RequestCode{

        public static final int GALLERY=1;

        public static final int CAMERA = 2;

        public static final int FILE = 25;

        public static final int BLOCK_USER = 7;

        public static final int DELETE_GROUP = 8;

        public static final int AUDIO = 3;

        public static final int READ_STORAGE = 001;

        public static final int RECORD = 003;

        public static final int LOCATION = 14;
    }
    public static class MapUrl{

        public static final String MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=380x220&markers=color:red|";

        public static final String MAP_ACCESS_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    }


    public static class MessageRequest {

        public static List<String> messageTypesForUser = new ArrayList<>(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //Custom Messages
                StringContract.IntentStrings.LOCATION,
                StringContract.IntentStrings.POLLS,
                StringContract.IntentStrings.STICKERS
        )) ;
        public static List<String> messageTypesForGroup = new ArrayList<>(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //For Group Actions
                CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
                //Custom Messages
                StringContract.IntentStrings.LOCATION,
                StringContract.IntentStrings.POLLS,
                StringContract.IntentStrings.STICKERS
        ));

        public static List<String> messageCategoriesForGroup = new ArrayList<>(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL,
                CometChatConstants.CATEGORY_ACTION));


        public static List<String> messageCategoriesForUser = new ArrayList<>(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL));
    }
}
