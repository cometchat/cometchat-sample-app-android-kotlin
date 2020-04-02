package screen.messagelist;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupMembersRequest;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.ComposeBox;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.TypingIndicator;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.SmartReplyList;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapter.MessageAdapter;
import constant.StringContract;
import listeners.OnItemClickListener;
import listeners.StickyHeaderDecoration;
import screen.CometChatForwardMessageScreenActivity;
import screen.CometChatGroupDetailScreenActivity;
import screen.CometChatUserDetailScreenActivity;
import utils.FontUtils;
import utils.FooterDecoration;
import utils.MediaUtils;
import utils.KeyBoardUtils;
import utils.Utils;
import utils.ZoomIv;

/**
 * Purpose - CometChatMessageScreen class is a fragment used to display list of messages and perform certain action on click of message.
 * It also provide search bar to perform search operation on the list of messages. User can send text,images,video and file as messages
 * to each other and in groups. User can also perform actions like edit message,delete message and forward messages to other user and groups.
 *
 * @see CometChat
 * @see User
 * @see Group
 * @see TextMessage
 * @see MediaMessage
 * <p>
 * Created on - 20th December 2019
 * <p>
 * Modified on  - 16th January 2020
 */


public class CometChatMessageScreen extends Fragment implements View.OnClickListener, CometChatMessageListActivity.OnMessageLongClick {

    private static FooterDecoration footerDecoration;
    private static final String TAG = "CometChatMessageScreen";

    private static final int LIMIT = 30;

    private String name = "";

    private String status = "";

    private MessagesRequest messagesRequest;    //Used to fetch messages.

    private RelativeLayout composeBox;

    private EditText etComposeBox;

    private ImageView ivSend;

    private ImageView ivArrow;

    private ImageView ivCamera;

    private ImageView ivGallery;

    private ImageView ivFile;

    private RelativeLayout rlContainer;

    private boolean isOpen;

    private RecyclerView rvChatListView;    //Used to display list of messages.

    private MessageAdapter messageAdapter;

    private LinearLayoutManager linearLayoutManager;

    private SmartReplyList rvSmartReply;

    private ShimmerFrameLayout messageShimmer;

    /**
     * <b>Avatar</b> is a UI Kit Component which is used to display user and group avatars.
     */
    private Avatar userAvatar;

    private TextView tvName;

    private TextView tvStatus;

    private String Id;

    private Context context;

    private LinearLayout blockUserLayout;

    private TextView blockedUserName;

    private StickyHeaderDecoration stickyHeaderDecoration;

    private String avatarUrl;

    private Toolbar toolbar;

    private String type;

    private boolean isBlockedByMe;

    private String loggedInUserScope;

    private RelativeLayout rlMessageAction;

    private ImageView ivCloseMessageAction;

    private ImageView ivCopyMessageAction;

    private TextView tvEditMessage;

    private TextView tvDeleteMessage;

    private TextView tvForwardMessage;

    private RelativeLayout editMessageLayout;

    private TextView tvMessageTitle;


    private TextView tvMessageSubTitle;

    private BaseMessage baseMessage;

    private List<BaseMessage> baseMessages = new ArrayList<>();

    private List<BaseMessage> messageList = new ArrayList<>();

    private boolean isEdit;

    private String groupOwnerId;

    private String memberNames;

    private Timer timer = new Timer();

    private View view;

    private boolean isNoMoreMessages;

    private FontUtils fontUtils;

    private User loggedInUser = CometChat.getLoggedInUser();

    String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean isInProgress;

    private boolean isSmartReplyClicked;


    public CometChatMessageScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleArguments();
        if (getActivity() != null)
            fontUtils = FontUtils.getInstance(getActivity());
    }

    /**
     * This method is used to handle arguments passed to this fragment.
     */
    private void handleArguments() {
        if (getArguments() != null) {
            Id = getArguments().getString(StringContract.IntentStrings.UID);
            avatarUrl = getArguments().getString(StringContract.IntentStrings.AVATAR);
            status = getArguments().getString(StringContract.IntentStrings.STATUS);
            name = getArguments().getString(StringContract.IntentStrings.NAME);
            type = getArguments().getString(StringContract.IntentStrings.TYPE);
            if (type != null && type.equals(CometChatConstants.RECEIVER_TYPE_GROUP))
                Id = getArguments().getString(StringContract.IntentStrings.GUID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_screen, container, false);

        initViewComponent(view);
        return view;
    }


    /**
     * This is a main method which is used to initialize the view for this fragment.
     *
     * @param view
     */
    private void initViewComponent(View view) {

        setHasOptionsMenu(true);
        composeBox = view.findViewById(R.id.message_box);
        messageShimmer = view.findViewById(R.id.shimmer_layout);
        etComposeBox = view.findViewById(R.id.etComposeBox);
        ivSend = view.findViewById(R.id.ivSend);
        ivSend.setOnClickListener(this);
        ivCamera = view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(this);
        ivGallery = view.findViewById(R.id.ivImage);
        ivGallery.setOnClickListener(this);
        ivFile = view.findViewById(R.id.ivFile);
        ivFile.setOnClickListener(this);
        rlContainer = view.findViewById(R.id.rlActionContainers);
        ivArrow = view.findViewById(R.id.ivArrow);
        ivArrow.setOnClickListener(this);
        setComposeBoxListener();

        rvSmartReply = view.findViewById(R.id.rv_smartReply);
        rlMessageAction = view.findViewById(R.id.message_actions);
        ivCloseMessageAction = view.findViewById(R.id.iv_close_message_action);
        ivCloseMessageAction.setOnClickListener(this);
        ivCopyMessageAction = view.findViewById(R.id.iv_copy_message_action);
        ivCopyMessageAction.setOnClickListener(this);
        tvEditMessage = view.findViewById(R.id.edit_message);
        tvEditMessage.setOnClickListener(this);
        tvDeleteMessage = view.findViewById(R.id.delete_message);
        tvDeleteMessage.setOnClickListener(this);
        tvForwardMessage = view.findViewById(R.id.forward_message);
        tvForwardMessage.setOnClickListener(this);
        editMessageLayout = view.findViewById(R.id.editMessageLayout);
        tvMessageTitle = view.findViewById(R.id.tv_message_layout_title);
        tvMessageSubTitle = view.findViewById(R.id.tv_message_layout_subtitle);
        ImageView ivMessageClose = view.findViewById(R.id.iv_message_close);
        ivMessageClose.setOnClickListener(this);
        rvChatListView = view.findViewById(R.id.rv_message_list);
        MaterialButton unblockUserBtn = view.findViewById(R.id.btn_unblock_user);
        unblockUserBtn.setOnClickListener(this);
        blockedUserName = view.findViewById(R.id.tv_blocked_user_name);
        blockUserLayout = view.findViewById(R.id.blocked_user_layout);
        tvName = view.findViewById(R.id.tv_name);
        tvStatus = view.findViewById(R.id.tv_status);
        userAvatar = view.findViewById(R.id.iv_chat_avatar);
        toolbar = view.findViewById(R.id.chatList_toolbar);
        toolbar.setOnClickListener(this);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tvName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        tvName.setText(name);
        setAvatar();


        rvChatListView.setLayoutManager(linearLayoutManager);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        KeyBoardUtils.setKeyboardVisibilityListener(getActivity(), (View) rvChatListView.getParent(), keyboardVisible -> {
            if (keyboardVisible) {
                scrollToBottom();
            }
        });


        // Uses to fetch next list of messages if rvChatListView (RecyclerView) is scrolled in downward direction.
        rvChatListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                //for toolbar elevation animation i.e stateListAnimator
                toolbar.setSelected(rvChatListView.canScrollVertically(-1));
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                    if (!isNoMoreMessages && !isInProgress) {
                        if (linearLayoutManager.findFirstVisibleItemPosition() == 10 || !rvChatListView.canScrollVertically(-1)) {
                            isInProgress = true;
                            fetchMessage();
                        }
                    }
            }

        });

        rvSmartReply.setItemClickListener(new OnItemClickListener<String>() {
            @Override
            public void OnItemClick(String var, int position) {
                if (!isSmartReplyClicked) {
                    isSmartReplyClicked = true;
                    rvSmartReply.setVisibility(View.GONE);
                    sendMessage(var);
                }
            }
        });




    }

    private void setComposeBoxListener() {

        etComposeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {

                    sendTypingIndicator(false);
                } else {
                    sendTypingIndicator(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (timer == null) {
                    timer = new Timer();
                }
                endTypingTimer();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: ");
        switch (requestCode) {

            case StringContract.RequestCode.CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)

                    startActivityForResult(MediaUtils.openCamera(getActivity()), StringContract.RequestCode.CAMERA);
                else
                    showSnackBar(view.findViewById(R.id.message_box), getResources().getString(R.string.grant_camera_permission));
                break;
            case StringContract.RequestCode.GALLERY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(MediaUtils.openGallery(getActivity()), StringContract.RequestCode.GALLERY);
                else
                    showSnackBar(view.findViewById(R.id.message_box), getResources().getString(R.string.grant_storage_permission));
                break;
            case StringContract.RequestCode.FILE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(MediaUtils.getFileIntent(StringContract.IntentStrings.EXTRA_MIME_DOC), StringContract.RequestCode.FILE);
                else
                    showSnackBar(view.findViewById(R.id.message_box), getResources().getString(R.string.grant_storage_permission));
                break;
        }
    }

    private void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is used to get Group Members and display names of group member.
     *
     * @see GroupMember
     * @see GroupMembersRequest
     */
    private void getMember() {
        GroupMembersRequest groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(Id).setLimit(100).build();

        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> list) {
                String s[] = new String[0];
                if (list != null && list.size() != 0) {
                    s = new String[list.size()];
                    for (int j = 0; j < list.size(); j++) {

                        s[j] = list.get(j).getName();
                    }

                }
                setSubTitle(s);

            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "Group Member list fetching failed with exception: " + e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Incase if user is blocked already, then this method is used to unblock the user .
     *
     * @see CometChat#unblockUsers(List, CometChat.CallbackListener)
     */
    private void unblockUser() {
        ArrayList<String> uids = new ArrayList<>();
        uids.add(Id);
        CometChat.unblockUsers(uids, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> stringStringHashMap) {
                Snackbar.make(rvChatListView,String.format(getResources().getString(R.string.user_unblocked),name),Snackbar.LENGTH_LONG).show();
                blockUserLayout.setVisibility(View.GONE);
                isBlockedByMe = false;
                messagesRequest=null;
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method is used to set GroupMember names as subtitle in toolbar.
     *
     * @param users
     */
    private void setSubTitle(String... users) {
        if (users != null && users.length != 0) {
            StringBuilder stringBuilder = new StringBuilder();

            for (String user : users) {
                stringBuilder.append(user).append(",");
            }

            memberNames = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();

            tvStatus.setText(memberNames);
        }

    }


    /**
     * This method is used to fetch message of users & groups. For user it fetches previous 100 messages at
     * a time and for groups it fetches previous 30 messages. You can change limit of messages by modifying
     * number in <code>setLimit()</code>
     * This method also mark last message as read using markMessageAsRead() present in this class.
     * So all the above messages get marked as read.
     *
     * @see MessagesRequest#fetchPrevious(CometChat.CallbackListener)
     */
    private void fetchMessage() {

        if (messagesRequest == null) {
            if (type != null) {
                if (type.equals(CometChatConstants.RECEIVER_TYPE_USER))
                    messagesRequest = new MessagesRequest.MessagesRequestBuilder().setLimit(LIMIT).setUID(Id).build();
                else
                    messagesRequest = new MessagesRequest.MessagesRequestBuilder().setLimit(LIMIT).setGUID(Id).hideMessagesFromBlockedUsers(true).build();
            }
        }
        messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {

            @Override
            public void onSuccess(List<BaseMessage> baseMessages) {
                isInProgress = false;
                List<BaseMessage> filteredMessageList = filterBaseMessages(baseMessages);
                initMessageAdapter(filteredMessageList);
                if (baseMessages.size() != 0) {
                    stopHideShimmer();
                    BaseMessage baseMessage = baseMessages.get(baseMessages.size() - 1);
                    markMessageAsRead(baseMessage);
                }

                if (baseMessages.size() == 0) {
                    stopHideShimmer();
                    isNoMoreMessages = true;
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });
    }

    private void stopHideShimmer() {
        messageShimmer.stopShimmer();
        messageShimmer.setVisibility(View.GONE);
    }


    private List<BaseMessage> filterBaseMessages(List<BaseMessage> baseMessages) {
        List<BaseMessage> tempList = new ArrayList<>();
        for(BaseMessage baseMessage : baseMessages)
        {
            if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION)) {
                Action action = ((Action)baseMessage);
                if (action.getAction().equals(CometChatConstants.ActionKeys.ACTION_MESSAGE_DELETED) ||
                        action.getAction().equals(CometChatConstants.ActionKeys.ACTION_MESSAGE_EDITED)) {
                }
                else {
                    tempList.add(baseMessage);
                }
            }
            else {
                tempList.add(baseMessage);
            }
        }
        return tempList;
    }

    private void getSmartReplyList(BaseMessage baseMessage) {

        HashMap<String, JSONObject> extensionList = Utils.extensionCheck(baseMessage);
        if (extensionList != null && extensionList.containsKey("smartReply")) {
            rvSmartReply.setVisibility(View.VISIBLE);
            JSONObject replyObject = extensionList.get("smartReply");
            List<String> replyList = new ArrayList<>();
            try {
                replyList.add(replyObject.getString("reply_positive"));
                replyList.add(replyObject.getString("reply_neutral"));
                replyList.add(replyObject.getString("reply_negative"));
            } catch (Exception e) {
                Log.e(TAG, "onSuccess: " + e.getMessage());
            }
            setSmartReplyAdapter(replyList);
        } else {
            rvSmartReply.setVisibility(View.GONE);
        }
    }

    private void setSmartReplyAdapter(List<String> replyList) {
        rvSmartReply.setSmartReplyList(replyList);
        scrollToBottom();
    }


    /**
     * This method is used to initialize the message adapter if it is empty else it helps
     * to update the messagelist in adapter.
     *
     * @param messageList is a list of messages which will be added.
     */
    private void initMessageAdapter(List<BaseMessage> messageList) {
        if (messageAdapter == null) {
            messageAdapter = new MessageAdapter(getActivity(), messageList, type);
            rvChatListView.setAdapter(messageAdapter);
            stickyHeaderDecoration = new StickyHeaderDecoration(messageAdapter);
            rvChatListView.addItemDecoration(stickyHeaderDecoration, 0);
            scrollToBottom();
            messageAdapter.notifyDataSetChanged();
        } else {
            messageAdapter.updateList(messageList);

        }
        if (!isBlockedByMe && rvSmartReply.getAdapter().getItemCount()==0&&rvSmartReply.getVisibility() == View.GONE) {
            BaseMessage lastMessage = messageAdapter.getLastMessage();
            checkSmartReply(lastMessage);
        }
    }

    /**
     * This method is used to send typing indicator to other users and groups.
     *
     * @param isEnd is boolean which is used to differentiate between startTyping & endTyping Indicators.
     * @see CometChat#startTyping(TypingIndicator)
     * @see CometChat#endTyping(TypingIndicator)
     */
    private void sendTypingIndicator(boolean isEnd) {
        if (isEnd) {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                CometChat.endTyping(new TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_USER));
            } else {
                CometChat.endTyping(new TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_GROUP));
            }
        } else {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                CometChat.startTyping(new TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_USER));
            } else {
                CometChat.startTyping(new TypingIndicator(Id, CometChatConstants.RECEIVER_TYPE_GROUP));
            }
        }
    }

    private void endTypingTimer() {
        if (timer!=null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTypingIndicator(true);
                }
            }, 2000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");

        switch (requestCode) {
            case StringContract.RequestCode.GALLERY:
                if (data != null) {

                    File file = MediaUtils.getRealPath(getContext(), data.getData());
                    ContentResolver cr = getActivity().getContentResolver();
                    String mimeType = cr.getType(data.getData());
                    if (mimeType!=null && mimeType.contains("image")) {
                        if (file.exists())
                            sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE);
                        else
                            Snackbar.make(rvChatListView, R.string.file_not_exist, Snackbar.LENGTH_LONG).show();
                    }
                    else {
                        if (file.exists())
                            sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_VIDEO);
                        else
                            Snackbar.make(rvChatListView, R.string.file_not_exist, Snackbar.LENGTH_LONG).show();
                    }
                }

                break;
            case StringContract.RequestCode.CAMERA:
                File file;
                if (Build.VERSION.SDK_INT >= 29) {
                    file = MediaUtils.getRealPath(getContext(), MediaUtils.uri);
                } else {
                    file = new File(MediaUtils.pictureImagePath);
                }
                if (file.exists())
                    sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE);
                else
                    Snackbar.make(rvChatListView,R.string.file_not_exist,Snackbar.LENGTH_LONG).show();

                break;
            case StringContract.RequestCode.FILE:
                if (data != null)
                    sendMediaMessage(MediaUtils.getRealPath(getActivity(), data.getData()), CometChatConstants.MESSAGE_TYPE_FILE);
                break;
            case StringContract.RequestCode.BLOCK_USER:
                name = data.getStringExtra("");
                break;
        }

    }


    /**
     * This method is used to send media messages to other users and group.
     *
     * @param file     is an object of File which is been sent within the message.
     * @param filetype is a string which indicate a type of file been sent within the message.
     * @see CometChat#sendMediaMessage(MediaMessage, CometChat.CallbackListener)
     * @see MediaMessage
     */
    private void sendMediaMessage(File file, String filetype) {
        MediaMessage mediaMessage;

        if (type.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
            mediaMessage = new MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_USER);
        else
            mediaMessage = new MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_GROUP);


        CometChat.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage) {
                Log.d(TAG, "sendMediaMessage onSuccess: " + mediaMessage.toString());
                if (messageAdapter != null) {
                    messageAdapter.addMessage(mediaMessage);
                    scrollToBottom();
                }
            }

            @Override
            public void onError(CometChatException e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method is used to get details of reciever.
     *
     * @see CometChat#getUser(String, CometChat.CallbackListener)
     */
    private void getUser() {

        CometChat.getUser(Id, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {

                if (getActivity() != null) {
                    if (user.isBlockedByMe()) {
                        isBlockedByMe = true;
                        rvSmartReply.setVisibility(View.GONE);
                        toolbar.setSelected(false);
                        blockedUserName.setText("You've blocked " + user.getName());
                        blockUserLayout.setVisibility(View.VISIBLE);
                    } else {
                        isBlockedByMe = false;
                        blockUserLayout.setVisibility(View.GONE);
                        avatarUrl = user.getAvatar();
                        if (user.getStatus().equals(CometChatConstants.USER_STATUS_ONLINE)) {
                            tvStatus.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                        }
                        status = user.getStatus().toString();
                        setAvatar();
                        tvStatus.setText(status);

                    }
                    name = user.getName();
                    tvName.setText(name);
                    Log.d(TAG, "onSuccess: " + user.toString());
                }

            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAvatar() {
        if (avatarUrl != null && !avatarUrl.isEmpty())
            userAvatar.setAvatar(avatarUrl);
        else {
            userAvatar.setInitials(name);
        }
    }

    /**
     * This method is used to get Group Details.
     *
     * @see CometChat#getGroup(String, CometChat.CallbackListener)
     */
    private void getGroup() {

        CometChat.getGroup(Id, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (getActivity() != null) {
                    name = group.getName();
                    avatarUrl = group.getIcon();
                    loggedInUserScope = group.getScope();
                    groupOwnerId = group.getOwner();

                    tvName.setText(name);
                    if (context != null) {
                        userAvatar.setAvatar(getActivity().getResources().getDrawable(R.drawable.ic_account), avatarUrl);
                    }
                    setAvatar();
                }

            }

            @Override
            public void onError(CometChatException e) {

            }
        });
    }

    /**
     * This method is used to send Text Message to other users and groups.
     *
     * @param message is a String which is been sent as message.
     * @see TextMessage
     * @see CometChat#sendMessage(TextMessage, CometChat.CallbackListener)
     */
    private void sendMessage(String message) {
        TextMessage textMessage;
        if (type.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
            textMessage = new TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_USER);
        else
            textMessage = new TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_GROUP);


        sendTypingIndicator(true);

        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                isSmartReplyClicked=false;
                if (messageAdapter != null) {
                    MediaUtils.playSendSound(context,R.raw.outgoing_message);
                    messageAdapter.addMessage(textMessage);
                    scrollToBottom();
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });

    }

    /**
     * This method is used to delete the message.
     *
     * @param baseMessage is an object of BaseMessage which is being used to delete the message.
     * @see BaseMessage
     * @see CometChat#deleteMessage(int, CometChat.CallbackListener)
     */
    private void deleteMessage(BaseMessage baseMessage) {
        CometChat.deleteMessage(baseMessage.getId(), new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
                if (messageAdapter != null)
                    messageAdapter.setUpdatedMessage(baseMessage);
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });
    }

    /**
     * This method is used to edit the message. This methods takes old message and change text of old
     * message with new message i.e String and update it.
     *
     * @param baseMessage is an object of BaseMessage, It is a old message which is going to be edited.
     * @param message     is String, It is a new message which will be replaced with text of old message.
     * @see TextMessage
     * @see BaseMessage
     * @see CometChat#editMessage(BaseMessage, CometChat.CallbackListener)
     */
    private void editMessage(BaseMessage baseMessage, String message) {

        isEdit = false;

        TextMessage textMessage;
        if (baseMessage.getReceiverType().equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
            textMessage = new TextMessage(baseMessage.getReceiverUid(), message, CometChatConstants.RECEIVER_TYPE_USER);
        else
            textMessage = new TextMessage(baseMessage.getReceiverUid(), message, CometChatConstants.RECEIVER_TYPE_GROUP);
        sendTypingIndicator(true);
        textMessage.setId(baseMessage.getId());
        CometChat.editMessage(textMessage, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage message) {
                if (messageAdapter != null) {
                    Log.e(TAG, "onSuccess: " + message.toString());
                    messageAdapter.setUpdatedMessage(message);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });

    }

    private void scrollToBottom() {
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            rvChatListView.scrollToPosition(messageAdapter.getItemCount() - 1);

        }
    }

    /**
     * This method is used to recieve real time group events like onMemberAddedToGroup, onGroupMemberJoined,
     * onGroupMemberKicked, onGroupMemberLeft, onGroupMemberBanned, onGroupMemberUnbanned,
     * onGroupMemberScopeChanged.
     *
     * @see CometChat#addGroupListener(String, CometChat.GroupListener)
     */
    private void addGroupListener() {
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                super.onGroupMemberJoined(action, joinedUser, joinedGroup);
                tvStatus.setText(memberNames + "," + joinedUser.getName());
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                }
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                super.onGroupMemberLeft(action, leftUser, leftGroup);
                Log.d(TAG, "onGroupMemberLeft: " + leftUser.getName());
                if (memberNames != null)
                    tvStatus.setText(memberNames.replace("," + leftUser.getName(), ""));
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    if (linearLayoutManager != null && linearLayoutManager.findLastVisibleItemPosition() < 10)
                        scrollToBottom();
                }
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                super.onGroupMemberKicked(action, kickedUser, kickedBy, kickedFrom);
                Log.d(TAG, "onGroupMemberKicked: " + kickedUser.getName());
                if (kickedUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    if (getActivity() != null)
                        getActivity().finish();
//                        ((Activity) getActivity()).onBackPressed();

                }
                tvStatus.setText(memberNames.replace("," + kickedUser.getName(), ""));
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    scrollToBottom();
                }
            }

            @Override
            public void onGroupMemberBanned(Action action, User bannedUser, User bannedBy, Group bannedFrom) {
                if (bannedUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                        Toast.makeText(getActivity(), "You have been banned", Toast.LENGTH_SHORT).show();
                    }
                }
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    scrollToBottom();
                }

            }

            @Override
            public void onGroupMemberUnbanned(Action action, User unbannedUser, User unbannedBy, Group unbannedFrom) {
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    scrollToBottom();
                }
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    scrollToBottom();
                }
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedby, User userAdded, Group addedTo) {
                tvStatus.setText(memberNames + "," + userAdded.getName());
                if (messageAdapter != null) {
                    messageAdapter.addMessage(action);
                    if (linearLayoutManager != null && linearLayoutManager.findLastVisibleItemPosition() < 10)
                        scrollToBottom();
                }
            }
        });
    }

    /**
     * This method is used to get real time user status i.e user is online or offline.
     *
     * @see CometChat#addUserListener(String, CometChat.UserListener)
     */
    private void addUserListener() {
        if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            CometChat.addUserListener(TAG, new CometChat.UserListener() {
                @Override
                public void onUserOnline(User user) {
                    Log.d(TAG, "onUserOnline: " + user.toString());
                    if (user.getUid().equals(Id)) {
                        tvStatus.setText(user.getStatus());
                        tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }

                @Override
                public void onUserOffline(User user) {
                    Log.d(TAG, "onUserOffline: " + user.toString());
                    if (user.getUid().equals(Id)) {
                        tvStatus.setTextColor(getResources().getColor(android.R.color.black));
                        tvStatus.setText(user.getStatus());
                    }
                }
            });
        }
    }


    /**
     * This method is used to mark users & group message as read.
     *
     * @param baseMessage is object of BaseMessage.class. It is message which is been marked as read.
     */
    private void markMessageAsRead(BaseMessage baseMessage) {
        if (type.equals(CometChatConstants.RECEIVER_TYPE_USER))
            CometChat.markAsRead(baseMessage.getId(), baseMessage.getSender().getUid(), baseMessage.getReceiverType());
        else
            CometChat.markAsRead(baseMessage.getId(), baseMessage.getReceiverUid(), baseMessage.getReceiverType());
    }


    /**
     * This method is used to add message listener to recieve real time messages between users &
     * groups. It also give real time events for typing indicators, edit message, delete message,
     * message being read & delivered.
     *
     * @see CometChat#addMessageListener(String, CometChat.MessageListener)
     */
    private void addMessageListener() {

        CometChat.addMessageListener(TAG, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage message) {
                Log.d(TAG, "onTextMessageReceived: " + message.toString());
                onMessageReceived(message);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                Log.d(TAG, "onMediaMessageReceived: " + message.toString());
                onMessageReceived(message);
            }

            @Override
            public void onTypingStarted(TypingIndicator typingIndicator) {
                Log.e(TAG, "onTypingStarted: " + typingIndicator);
                setTypingIndicator(typingIndicator,true);
            }

            @Override
            public void onTypingEnded(TypingIndicator typingIndicator) {
                Log.d(TAG, "onTypingEnded: " + typingIndicator.toString());
                setTypingIndicator(typingIndicator,false);
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                Log.d(TAG, "onMessagesDelivered: " + messageReceipt.toString());
                setMessageReciept(messageReceipt);

            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                Log.e(TAG, "onMessagesRead: " + messageReceipt.toString());
                setMessageReciept(messageReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage message) {
                Log.d(TAG, "onMessageEdited: " + message.toString());
                updateMessage(message);
            }

            @Override
            public void onMessageDeleted(BaseMessage message) {
                Log.d(TAG, "onMessageDeleted: ");
                updateMessage(message);
            }
        });
    }

    private void setMessageReciept(MessageReceipt messageReceipt) {
        if (messageAdapter != null) {
            if (messageReceipt.getReceivertype().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                if (Id!=null && messageReceipt.getSender().getUid().equals(Id)) {
                    if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_DELIVERED))
                        messageAdapter.setDeliveryReceipts(messageReceipt);
                    else
                        messageAdapter.setReadReceipts(messageReceipt);
                }
            }
        }
    }

    private void setTypingIndicator(TypingIndicator typingIndicator,boolean isShow) {
        if (typingIndicator.getReceiverType().equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER)) {
            Log.e(TAG, "onTypingStarted: " + typingIndicator);
            if (Id != null && Id.equalsIgnoreCase(typingIndicator.getSender().getUid()))
                typingIndicator(typingIndicator, isShow);
        } else {
            if (Id != null && Id.equalsIgnoreCase(typingIndicator.getReceiverId()))
                typingIndicator(typingIndicator, isShow);
        }
    }

    private void onMessageReceived(BaseMessage message) {
        MediaUtils.playSendSound(context,R.raw.incoming_message);
        if (message.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            if (Id != null && Id.equalsIgnoreCase(message.getSender().getUid())) {
                setMessage(message);
            }
        } else {
            if (Id != null && Id.equalsIgnoreCase(message.getReceiverUid())) {
                setMessage(message);
            }
        }
    }

    /**
     * This method is used to update edited message by calling <code>setEditMessage()</code> of adapter
     *
     * @param message is an object of BaseMessage and it will replace with old message.
     * @see BaseMessage
     */
    private void updateMessage(BaseMessage message) {
        messageAdapter.setUpdatedMessage(message);
    }


    /**
     * This method is used to mark message as read before adding them to list. This method helps to
     * add real time message in list.
     *
     * @param message is an object of BaseMessage, It is recieved from message listener.
     * @see BaseMessage
     */
    private void setMessage(BaseMessage message) {
        if (messageAdapter != null) {
            messageAdapter.addMessage(message);
            checkSmartReply(message);
            markMessageAsRead(message);
            if ((messageAdapter.getItemCount() - 1) - ((LinearLayoutManager) rvChatListView.getLayoutManager()).findLastVisibleItemPosition() < 5)
                scrollToBottom();
        } else {
            messageList.add(message);
            initMessageAdapter(messageList);
        }
    }

    private void checkSmartReply(BaseMessage lastMessage) {
        if (lastMessage != null && !lastMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (lastMessage.getMetadata() != null) {
                getSmartReplyList(lastMessage);
            }
        }
    }

    /**
     * This method is used to display typing status to user.
     *
     * @param show is boolean, If it is true then <b>is Typing</b> will be shown to user
     *             If it is false then it will show user status i.e online or offline.
     */
    private void typingIndicator(TypingIndicator typingIndicator, boolean show) {
        if (messageAdapter != null) {
            if (show) {
                if (typingIndicator.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER))
                    tvStatus.setText("is Typing...");
                else
                    tvStatus.setText(typingIndicator.getSender().getName() + " is Typing...");
            } else {
                if (typingIndicator.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER))
                    tvStatus.setText(status);
                else
                    tvStatus.setText(memberNames);
            }

        }
    }

    /**
     * This method is used to remove message listener
     *
     * @see CometChat#removeMessageListener(String)
     */
    private void removeMessageListener() {
        CometChat.removeMessageListener(TAG);
    }

    /**
     * This method is used to remove user presence listener
     *
     * @see CometChat#removeUserListener(String)
     */
    private void removeUserListener() {
        CometChat.removeUserListener(TAG);
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        removeMessageListener();
        removeUserListener();
        removeGroupListener();
        timer = null;
        sendTypingIndicator(true);


    }

    private void removeGroupListener() {
        CometChat.removeGroupListener(TAG);
    }

    @Override
    public void onResume() {
        onCloseAction();
        super.onResume();
        Log.d(TAG, "onResume: ");
        rvChatListView.removeItemDecoration(stickyHeaderDecoration);
        messageAdapter = null;
        messagesRequest = null;
        fetchMessage();
        addMessageListener();

        if (type != null) {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                addUserListener();
                tvStatus.setText(status);
                new Thread(this::getUser).start();
            } else {
                addGroupListener();
                new Thread(this::getGroup).start();
                new Thread(this::getMember).start();
            }
        }
    }

    public void onCloseAction() {
        if (messageAdapter != null)
            messageAdapter.clearLongClickSelectedItem();
        composeBox.setVisibility(View.VISIBLE);
        rlMessageAction.setVisibility(View.GONE);
        userAvatar.setVisibility(View.VISIBLE);
        ivCloseMessageAction.setVisibility(View.GONE);
        ivCopyMessageAction.setVisibility(View.GONE);
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.delete_message) {
            deleteMessage(baseMessage);
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            onCloseAction();
        }
        else if (id == R.id.forward_message) {
            Intent intent = new Intent(getContext(), CometChatForwardMessageScreenActivity.class);
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)){
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, ((TextMessage) baseMessage).getText());
                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT);
            }
            startActivity(intent);
        }
        else if (id == R.id.edit_message) {
            if (baseMessage!=null&&baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                isEdit = true;
                tvMessageTitle.setText(getResources().getString(R.string.edit_message));
                tvMessageSubTitle.setText(((TextMessage) baseMessage).getText());
                etComposeBox.setText(((TextMessage) baseMessage).getText());

                editMessageLayout.setVisibility(View.VISIBLE);
                if (messageAdapter != null) {
                    messageAdapter.setSelectedMessage(baseMessage.getId());
                    messageAdapter.notifyDataSetChanged();
                }
            }
            onCloseAction();
        }
        else if (id == R.id.iv_copy_message_action) {
            String message = "";
            for (BaseMessage bMessage : baseMessages) {
                if (bMessage.getDeletedAt() == 0 && bMessage instanceof TextMessage) {
                    message = message + "[" + Utils.getLastMessageDate(bMessage.getSentAt()) + "] " + bMessage.getSender().getName() + ": " + ((TextMessage) bMessage).getText() + "\n\n";
                }
            }
            Log.e(TAG, "onCopy: " + message);
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("MessageAdapter", message);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(context, getResources().getString(R.string.text_copied_clipboard), Toast.LENGTH_LONG).show();
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            onCloseAction();
        }
        else if (id == R.id.iv_close_message_action) {
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            onCloseAction();
        }
        else if (id == R.id.iv_message_close) {
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            isEdit = false;
            baseMessage = null;
            editMessageLayout.setVisibility(View.GONE);
            etComposeBox.setText("");
        }
        else if(id == R.id.ivCamera) {
            if (Utils.hasPermissions(getContext(), CAMERA_PERMISSION)) {
                startActivityForResult(MediaUtils.openCamera(getContext()), StringContract.RequestCode.CAMERA);
            } else {
                requestPermissions(CAMERA_PERMISSION, StringContract.RequestCode.CAMERA);
            }
        }
        else if (id == R.id.ivImage) {
            if (Utils.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startActivityForResult(MediaUtils.openGallery(getActivity()), StringContract.RequestCode.GALLERY);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, StringContract.RequestCode.GALLERY);
            }
        }
        else if (id == R.id.ivFile) {
            if (Utils.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startActivityForResult(MediaUtils.getFileIntent(StringContract.IntentStrings.EXTRA_MIME_DOC), StringContract.RequestCode.FILE);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, StringContract.RequestCode.FILE);
            }
        }
        else if (id == R.id.ivSend) {
            String message = etComposeBox.getText().toString().trim();
            etComposeBox.setText("");
            if (isEdit) {
                editMessage(baseMessage, message);
                editMessageLayout.setVisibility(View.GONE);
            } else
            if(!message.isEmpty())
                sendMessage(message);
        }
        else if (id == R.id.ivArrow) {
            if (isOpen) {
                ivArrow.setRotation(0);
                isOpen = false;
                Animation leftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.animate_left_slide);
                rlContainer.startAnimation(leftAnim);
                rlContainer.setVisibility(View.GONE);
            } else {
                ivArrow.setRotation(45f);
                isOpen = true;
                Animation rightAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.animate_right_slide);
                rlContainer.startAnimation(rightAnimate);
                rlContainer.setVisibility(View.VISIBLE);
            }
        }
        else if (id == R.id.btn_unblock_user) {
            unblockUser();
        }
        else if (id == R.id.chatList_toolbar) {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                Intent intent = new Intent(getContext(), CometChatUserDetailScreenActivity.class);
                intent.putExtra(StringContract.IntentStrings.UID, Id);
                intent.putExtra(StringContract.IntentStrings.NAME, name);
                intent.putExtra(StringContract.IntentStrings.AVATAR, avatarUrl);
                intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, isBlockedByMe);
                intent.putExtra(StringContract.IntentStrings.STATUS, status);
                intent.putExtra(StringContract.IntentStrings.TYPE, type);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), CometChatGroupDetailScreenActivity.class);
                intent.putExtra(StringContract.IntentStrings.GUID, Id);
                intent.putExtra(StringContract.IntentStrings.NAME, name);
                intent.putExtra(StringContract.IntentStrings.AVATAR, avatarUrl);
                intent.putExtra(StringContract.IntentStrings.TYPE, type);
                intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope);
                intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, groupOwnerId);
                startActivity(intent);
            }
        }
    }

    @Override
    public void setLongMessageClick(List<BaseMessage> baseMessagesList) {
        Log.e(TAG, "setLongMessageClick: " + baseMessagesList);
        List<BaseMessage> textMessageList = new ArrayList<>();
        List<BaseMessage> mediaMessageList = new ArrayList<>();
        for (BaseMessage baseMessage : baseMessagesList) {
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                textMessageList.add(baseMessage);
            } else {
                mediaMessageList.add(baseMessage);
            }
        }
        if (textMessageList.size() == 1) {
            BaseMessage basemessage = textMessageList.get(0);
            if (basemessage != null && basemessage.getSender() != null) {
                if (!(basemessage instanceof Action) && basemessage.getDeletedAt() == 0) {
                    baseMessage = basemessage;
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    ivCloseMessageAction.setVisibility(View.VISIBLE);
                    ivCopyMessageAction.setVisibility(View.VISIBLE);
                    userAvatar.setVisibility(View.GONE);
                    if (basemessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        tvDeleteMessage.setVisibility(View.VISIBLE);
                        tvEditMessage.setVisibility(View.VISIBLE);
                        tvEditMessage.setText(getResources().getString(R.string.edit));
                        tvForwardMessage.setVisibility(View.VISIBLE);
                        rlMessageAction.setVisibility(View.VISIBLE);
                    } else {
                        tvForwardMessage.setVisibility(View.VISIBLE);
                        tvDeleteMessage.setVisibility(View.GONE);
                        tvEditMessage.setVisibility(View.GONE);
                        rlMessageAction.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else if (textMessageList.size() == 0) {
            onCloseAction();
            if (messageAdapter != null)
                messageAdapter.clearLongClickSelectedItem();
        } else {
            ivCopyMessageAction.setVisibility(View.VISIBLE);
            tvEditMessage.setVisibility(View.GONE);
            tvDeleteMessage.setVisibility(View.GONE);
            tvForwardMessage.setVisibility(View.GONE);
        }

        if (mediaMessageList.size() == 1) {
            BaseMessage basemessage = mediaMessageList.get(0);
            if (basemessage != null && basemessage.getSender() != null) {
                if (!(basemessage instanceof Action) && basemessage.getDeletedAt() == 0) {
                    baseMessage = basemessage;
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    ivCloseMessageAction.setVisibility(View.VISIBLE);
                    ivCopyMessageAction.setVisibility(View.GONE);
                    userAvatar.setVisibility(View.GONE);
                    if (basemessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        tvDeleteMessage.setVisibility(View.VISIBLE);
                        tvEditMessage.setVisibility(View.GONE);
                        tvForwardMessage.setVisibility(View.GONE);
                        rlMessageAction.setVisibility(View.VISIBLE);
                    } else {
                        tvForwardMessage.setVisibility(View.GONE);
                        tvEditMessage.setVisibility(View.GONE);
                        tvDeleteMessage.setVisibility(View.GONE);
                        rlMessageAction.setVisibility(View.GONE);
                        ivCloseMessageAction.setVisibility(View.GONE);
                    }
                }
            }
        }
        baseMessages = baseMessagesList;
    }
}
