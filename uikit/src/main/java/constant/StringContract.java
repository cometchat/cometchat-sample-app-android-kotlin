package constant;

public class StringContract {

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
    }


}
