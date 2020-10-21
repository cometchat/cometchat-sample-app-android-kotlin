package screen.threadconversation;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.TypingIndicator;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.ComposeBox.ComposeBox;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.SmartReplyList;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import adapter.ThreadAdapter;
import constant.StringContract;
import listeners.ComposeActionListener;
import listeners.MessageActionCloseListener;
import listeners.OnItemClickListener;
import listeners.OnMessageLongClick;
import listeners.StickyHeaderDecoration;
import screen.CometChatForwardMessageScreenActivity;
import screen.CometChatGroupDetailScreenActivity;
import screen.CometChatMessageInfoScreenActivity;
import screen.CometChatUserDetailScreenActivity;
import screen.messagelist.MessageActionFragment;
import utils.CallUtils;
import utils.Extensions;
import utils.FontUtils;
import utils.KeyBoardUtils;
import utils.MediaUtils;
import utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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


public class CometChatThreadMessageScreen extends Fragment implements View.OnClickListener,
        OnMessageLongClick, MessageActionCloseListener {

    private static final String TAG = "CometChatThreadScreen";
    private static final int LIMIT = 30;
    private RelativeLayout bottomLayout;
    private String name = "";
    private String conversationName = "";
    private MessagesRequest messagesRequest;    //Used to fetch messages.
    private ComposeBox composeBox;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFileNameWithPath;
    private RecyclerView rvChatListView;    //Used to display list of messages.
    private ThreadAdapter messageAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SmartReplyList rvSmartReply;
    private ShimmerFrameLayout messageShimmer;
    /**
     * <b>Avatar</b> is a UI Kit Component which is used to display user and group avatars.
     */
    private TextView tvName;
    private TextView tvTypingIndicator;
    private Avatar senderAvatar;
    private TextView senderName;
    private TextView tvSentAt;
    private String Id;
    private Context context;
    private LinearLayout blockUserLayout;
    private TextView blockedUserName;
    private StickyHeaderDecoration stickyHeaderDecoration;
    private String avatarUrl;
    private Toolbar toolbar;
    private boolean isBlockedByMe;
    private String loggedInUserScope;
    private RelativeLayout editMessageLayout;
    private TextView tvMessageTitle;
    private TextView tvMessageSubTitle;
    private RelativeLayout replyMessageLayout;
    private TextView replyTitle;
    private TextView replyMessage;
    private ImageView replyMedia;
    private ImageView replyClose;
    private BaseMessage baseMessage;
    private List<BaseMessage> baseMessages = new ArrayList<>();
    private List<BaseMessage> messageList = new ArrayList<>();
    private boolean isEdit;
    private boolean isReply;
    private Timer timer = new Timer();
    private Timer typingTimer = new Timer();
    private View view;
    private boolean isNoMoreMessages;
    private FontUtils fontUtils;
    private User loggedInUser = CometChat.getLoggedInUser();
    String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isInProgress;
    private boolean isSmartReplyClicked;
    private RelativeLayout onGoingCallView;
    private TextView onGoingCallTxt;
    private ImageView onGoingCallClose;
    public int count = 0;
    private long messageSentAt;
    private String messageType;
    private String message;
    private String messageFileName;
    private int messageSize;
    private String messageMimeType;
    private String messageExtension;
    private int parentId;
    private String type;
    private String groupOwnerId;

    private TextView textMessage;
    private ImageView imageMessage;
    private VideoView videoMessage;
    private RelativeLayout fileMessage;
    private RelativeLayout locationMessage;
    private View pollMessage;

    private TextView pollQuestionTv;
    private LinearLayout pollOptionsLL;

    private ImageView mapView;
    private TextView addressView;
    private TextView fileName;
    private TextView fileSize;
    private TextView fileExtension;
    private TextView sentAt;
    private int replyCount;
    private TextView tvReplyCount;
    private NestedScrollView nestedScrollView;
    private LinearLayout noReplyMessages;
    private ImageView ivForwardMessage;
    private boolean isParent = true;
    private ImageView ivMoreOption;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private String parentMessageCategory;
    private double LATITUDE;
    private double LONGITUDE;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double parentMessageLatitude, parentMessageLongitude;
    private String pollQuestion,pollOptions;
    private ArrayList<String> pollResult;
    private TextView totalCount;
    private int voteCount;
    public CometChatThreadMessageScreen() {
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
            parentId = getArguments().getInt(StringContract.IntentStrings.PARENT_ID,0);
            replyCount = getArguments().getInt(StringContract.IntentStrings.REPLY_COUNT,0);
            type = getArguments().getString(StringContract.IntentStrings.TYPE);
            Id = getArguments().getString(StringContract.IntentStrings.ID);
            avatarUrl = getArguments().getString(StringContract.IntentStrings.AVATAR);
            name = getArguments().getString(StringContract.IntentStrings.NAME);
            conversationName = getArguments().getString(StringContract.IntentStrings.CONVERSATION_NAME);
            messageType = getArguments().getString(StringContract.IntentStrings.MESSAGE_TYPE);
            messageSentAt = getArguments().getLong(StringContract.IntentStrings.SENTAT);
            parentMessageCategory = getArguments().getString(StringContract.IntentStrings.MESSAGE_CATEGORY);
            if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                message = getArguments().getString(StringContract.IntentStrings.TEXTMESSAGE);
            } else if (messageType.equals(StringContract.IntentStrings.LOCATION)) {
                parentMessageLatitude = getArguments().getDouble(StringContract.IntentStrings.LOCATION_LATITUDE);
                parentMessageLongitude = getArguments().getDouble(StringContract.IntentStrings.LOCATION_LONGITUDE);

            } else if (messageType.equals(StringContract.IntentStrings.POLLS)) {
                pollQuestion = getArguments().getString(StringContract.IntentStrings.POLL_QUESTION);
                pollOptions = getArguments().getString(StringContract.IntentStrings.POLL_OPTION);
                pollResult = getArguments().getStringArrayList(StringContract.IntentStrings.POLL_RESULT);
                voteCount = getArguments().getInt(StringContract.IntentStrings.POLL_VOTE_COUNT);
            } else {
                message = getArguments().getString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL);
                messageFileName = getArguments().getString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME);
                messageExtension = getArguments().getString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION);
                messageSize = getArguments().getInt(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0);
                messageMimeType = getArguments().getString(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_thread_message, container, false);
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
        nestedScrollView = view.findViewById(R.id.nested_scrollview);
        noReplyMessages = view.findViewById(R.id.no_reply_layout);
        ivMoreOption = view.findViewById(R.id.ic_more_option);
        ivMoreOption.setOnClickListener(this);
        ivForwardMessage = view.findViewById(R.id.ic_forward_option);
        ivForwardMessage.setOnClickListener(this);
        textMessage = view.findViewById(R.id.tv_textMessage);
        imageMessage = view.findViewById(R.id.iv_imageMessage);
        videoMessage = view.findViewById(R.id.vv_videoMessage);
        fileMessage = view.findViewById(R.id.rl_fileMessage);
        locationMessage = view.findViewById(R.id.rl_locationMessage);
        mapView = view.findViewById(R.id.iv_mapView);
        addressView = view.findViewById(R.id.tv_address);
        fileName = view.findViewById(R.id.tvFileName);
        fileSize = view.findViewById(R.id.tvFileSize);
        fileExtension = view.findViewById(R.id.tvFileExtension);

        pollMessage = view.findViewById(R.id.poll_message);
        pollQuestionTv = view.findViewById(R.id.tv_question);
        pollOptionsLL = view.findViewById(R.id.options_group);
        totalCount = view.findViewById(R.id.total_votes);

        if (messageType.equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            imageMessage.setVisibility(View.VISIBLE);
            Glide.with(context).load(message).into(imageMessage);
        } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
            videoMessage.setVisibility(VISIBLE);
            MediaController mediacontroller = new MediaController(getContext());
            mediacontroller.setAnchorView(videoMessage);
            videoMessage.setMediaController(mediacontroller);
            videoMessage.setVideoURI(Uri.parse(message));
        } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_FILE) ||
                messageType.equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
            fileMessage.setVisibility(VISIBLE);
            if (messageFileName!=null)
                fileName.setText(messageFileName);
            if (messageExtension!=null)
                fileExtension.setText(messageExtension);

            fileSize.setText(Utils.getFileSize(messageSize));
        } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            textMessage.setVisibility(View.VISIBLE);
            textMessage.setText(message);
        } else if (messageType.equals(StringContract.IntentStrings.LOCATION)) {
            initLocation();
            locationMessage.setVisibility(VISIBLE);
            addressView.setText(Utils.getAddress(context, parentMessageLatitude, parentMessageLongitude));
            String mapUrl = StringContract.MapUrl.MAPS_URL +parentMessageLatitude+","+parentMessageLongitude+"&key="+ StringContract.MapUrl.MAP_ACCESS_KEY;
            Glide.with(context)
                    .load(mapUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mapView);
        } else if (messageType.equals(StringContract.IntentStrings.POLLS)) {
            ivForwardMessage.setVisibility(GONE);
            TextView threadReplyCount = view.findViewById(R.id.thread_reply_count);
            threadReplyCount.setVisibility(GONE);
            pollMessage.setVisibility(VISIBLE);
            totalCount.setText(voteCount+" Votes");
            pollQuestionTv.setText(pollQuestion);
            try {
                JSONObject options = new JSONObject(pollOptions);
                ArrayList<String> voterInfo = pollResult;
                for (int k = 0; k < options.length(); k++) {
                    LinearLayout linearLayout = new LinearLayout(context);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout
                            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayout.setPadding(8,8,8,8);
                    linearLayout.setBackground(context.getResources()
                            .getDrawable(R.drawable.cc_message_bubble_right));
                    linearLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources()
                            .getColor(R.color.textColorWhite)));
                    layoutParams.bottomMargin = (int) Utils.dpToPx(context, 8);
                    linearLayout.setLayoutParams(layoutParams);

                    TextView textViewPercentage = new TextView(context);
                    TextView textViewOption = new TextView(context);
                    textViewPercentage.setPadding(16, 4, 0, 4);
                    textViewOption.setPadding(16, 4, 0, 4);
                    textViewOption.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
                    textViewPercentage.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);

                    textViewPercentage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
                    textViewOption.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

                    String optionStr = options.getString(String.valueOf(k + 1));
                    if (voteCount>0) {
                        int percentage = Math.round((Integer.parseInt(voterInfo.get(k)) * 100) /
                                voteCount);
                        if (percentage > 0)
                            textViewPercentage.setText(percentage + "% ");
                    }
                    textViewOption.setText(optionStr);
                    int finalK = k;
                    if (pollOptionsLL.getChildCount()!=options.length()) {
                        linearLayout.addView(textViewPercentage);
                        linearLayout.addView(textViewOption);
                        pollOptionsLL.addView(linearLayout);
                    }
                    textViewOption.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("vote",finalK+1);
                                jsonObject.put("id",baseMessage.getId());
                                CometChat.callExtension("polls", "POST", "/v1/vote",
                                        jsonObject,new CometChat.CallbackListener<JSONObject>() {
                                            @Override
                                            public void onSuccess(JSONObject jsonObject) {
                                                // Voted successfully
                                                Log.e(TAG, "onSuccess: "+jsonObject.toString());
                                                Toast.makeText(context,"Voted Success",Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onError(CometChatException e) {
                                                // Some error occured
                                                Log.e(TAG, "onErrorExtension: "+e.getMessage()+"\n"+e.getCode());
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "onError: "+e.getMessage());
                            }
                        }
                    });
                 }
            } catch (Exception e) {
                Log.e(TAG, "setPollsData: "+e.getMessage());
            }
        }
        bottomLayout = view.findViewById(R.id.bottom_layout);
        composeBox = view.findViewById(R.id.message_box);
        messageShimmer = view.findViewById(R.id.shimmer_layout);
        composeBox = view.findViewById(R.id.message_box);
        composeBox.usedIn(CometChatThreadMessageActivity.class.getName());
        composeBox.isPollVisible = false;
        composeBox.ivMic.setVisibility(GONE);
        composeBox.ivSend.setVisibility(VISIBLE);
        setComposeBoxListener();

        rvSmartReply = view.findViewById(R.id.rv_smartReply);


        editMessageLayout = view.findViewById(R.id.editMessageLayout);
        tvMessageTitle = view.findViewById(R.id.tv_message_layout_title);
        tvMessageSubTitle = view.findViewById(R.id.tv_message_layout_subtitle);
        ImageView ivMessageClose = view.findViewById(R.id.iv_message_close);
        ivMessageClose.setOnClickListener(this);

        replyMessageLayout = view.findViewById(R.id.replyMessageLayout);
        replyTitle = view.findViewById(R.id.tv_reply_layout_title);
        replyMessage = view.findViewById(R.id.tv_reply_layout_subtitle);
        replyMedia = view.findViewById(R.id.iv_reply_media);
        replyClose = view.findViewById(R.id.iv_reply_close);
        replyClose.setOnClickListener(this);

        senderAvatar = view.findViewById(R.id.av_sender);
        setAvatar();
        senderName = view.findViewById(R.id.tv_sender_name);
        senderName.setText(name);
        sentAt = view.findViewById(R.id.tv_message_time);
        sentAt.setText(String.format(getString(R.string.sentAtTxt),Utils.getMessageDate(messageSentAt)));
        tvReplyCount = view.findViewById(R.id.thread_reply_count);
        rvChatListView = view.findViewById(R.id.rv_message_list);
        if (parentMessageCategory.equals(CometChatConstants.CATEGORY_CUSTOM))
            ivMoreOption.setVisibility(GONE);
        if (replyCount>0) {
            tvReplyCount.setText(replyCount + " Replies");
            noReplyMessages.setVisibility(GONE);
        }
        else {
            noReplyMessages.setVisibility(VISIBLE);
        }

        MaterialButton unblockUserBtn = view.findViewById(R.id.btn_unblock_user);
        unblockUserBtn.setOnClickListener(this);
        blockedUserName = view.findViewById(R.id.tv_blocked_user_name);
        blockUserLayout = view.findViewById(R.id.blocked_user_layout);
        tvName = view.findViewById(R.id.tv_name);
        tvTypingIndicator = view.findViewById(R.id.tv_typing);
        toolbar = view.findViewById(R.id.chatList_toolbar);
        toolbar.setOnClickListener(this);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tvName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        tvName.setText(String.format(getString(R.string.thread_in_name),conversationName));
        setAvatar();
        rvChatListView.setLayoutManager(linearLayoutManager);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Utils.isDarkMode(context)) {
            ivMoreOption.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivForwardMessage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            bottomLayout.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            toolbar.setBackgroundColor(getResources().getColor(R.color.grey));
            editMessageLayout.setBackground(getResources().getDrawable(R.drawable.left_border_dark));
            replyMessageLayout.setBackground(getResources().getDrawable(R.drawable.left_border_dark));
            composeBox.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            rvChatListView.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            tvName.setTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            ivMoreOption.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            ivForwardMessage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
            bottomLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            toolbar.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            editMessageLayout.setBackground(getResources().getDrawable(R.drawable.left_border));
            replyMessageLayout.setBackground(getResources().getDrawable(R.drawable.left_border));
            composeBox.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            rvChatListView.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            tvName.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }

        KeyBoardUtils.setKeyboardVisibilityListener(getActivity(),(View) rvChatListView.getParent(), keyboardVisible -> {
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
                    rvSmartReply.setVisibility(GONE);
                    sendMessage(var);
                }
            }
        });

        //Check Ongoing Call
        onGoingCallView = view.findViewById(R.id.ongoing_call_view);
        onGoingCallClose = view.findViewById(R.id.close_ongoing_view);
        onGoingCallTxt = view.findViewById(R.id.ongoing_call);
        checkOnGoingCall();
    }

    private void checkOnGoingCall() {
            if(CometChat.getActiveCall()!=null && CometChat.getActiveCall().getCallStatus().equals(CometChatConstants.CALL_STATUS_ONGOING) && CometChat.getActiveCall().getSessionId()!=null) {
                if(onGoingCallView!=null)
                    onGoingCallView.setVisibility(View.VISIBLE);
                if(onGoingCallTxt!=null) {
                    onGoingCallTxt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onGoingCallView.setVisibility(View.GONE);
                            CallUtils.joinOnGoingCall(getContext());
                        }
                    });
                }
                if(onGoingCallClose!=null) {
                    onGoingCallClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onGoingCallView.setVisibility(GONE);
                        }
                    });
                }
            } else if (CometChat.getActiveCall()!=null){
                if (onGoingCallView!=null)
                    onGoingCallView.setVisibility(GONE);
                Log.e(TAG, "checkOnGoingCall: "+CometChat.getActiveCall().toString());
            }
    }

    private void setComposeBoxListener() {

        composeBox.setComposeBoxListener(new ComposeActionListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0) {
                    sendTypingIndicator(false);
                } else {
                    sendTypingIndicator(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (typingTimer == null) {
                    typingTimer = new Timer();
                }
                endTypingTimer();
            }

            @Override
            public void onAudioActionClicked() {
                if (Utils.hasPermissions(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.openAudio(getActivity()),StringContract.RequestCode.AUDIO);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},StringContract.RequestCode.AUDIO);
                }
            }

            @Override
            public void onCameraActionClicked() {
                if (Utils.hasPermissions(getContext(), CAMERA_PERMISSION)) {
                    startActivityForResult(MediaUtils.openCamera(getContext()), StringContract.RequestCode.CAMERA);
                } else {
                    requestPermissions(CAMERA_PERMISSION, StringContract.RequestCode.CAMERA);
                }
            }


            @Override
            public void onGalleryActionClicked() {
                if (Utils.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.openGallery(getActivity()), StringContract.RequestCode.GALLERY);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, StringContract.RequestCode.GALLERY);
                }
            }

            @Override
            public void onFileActionClicked() {
                if (Utils.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(MediaUtils.getFileIntent(StringContract.IntentStrings.EXTRA_MIME_DOC), StringContract.RequestCode.FILE);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, StringContract.RequestCode.FILE);
                }
            }

            @Override
            public void onSendActionClicked(EditText editText) {
                String message = editText.getText().toString().trim();
                editText.setText("");
                editText.setHint(getString(R.string.message));
                if (isEdit) {
                    if (isParent)
                        editThread(message);
                    else {
                        editMessage(baseMessage, message);
                    }
                    editMessageLayout.setVisibility(GONE);
                } else if(isReply){
                    replyMessage(baseMessage,message);
                    replyMessageLayout.setVisibility(GONE);
                } else if (!message.isEmpty())
                    sendMessage(message);
            }

            @Override
            public void onVoiceNoteComplete(String string) {
                if (string != null) {
                    File audioFile = new File(string);
                    sendMediaMessage(audioFile, CometChatConstants.MESSAGE_TYPE_AUDIO);
                }
            }
            @Override
            public void onLocationActionClicked() {
                if (Utils.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    initLocation();

                    boolean provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!provider) {
                        turnOnLocation();
                    }
                    else {
                        getLocation();
                    }
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, StringContract.RequestCode.LOCATION);
                }
            }
        });
    }
    private void getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                {
                    double lon = location.getLongitude();
                    double lat = location.getLatitude();

                    JSONObject customData = new JSONObject();
                    try {
                        customData.put("latitude", lat);
                        customData.put("longitude", lon);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    initAlert(customData);
                }
            }
        });
    }

    private void initAlert(JSONObject customData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(context).inflate(R.layout.map_share_layout,null);
        builder.setView(view);
        try {
            LATITUDE = customData.getDouble("latitude");
            LONGITUDE = customData.getDouble("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView address = view.findViewById(R.id.address);
        address.setText("Address: "+Utils.getAddress(context,LATITUDE,LONGITUDE));
        ImageView mapView = view.findViewById(R.id.map_vw);
        String mapUrl = StringContract.MapUrl.MAPS_URL +LATITUDE+","+LONGITUDE+"&key="+
                StringContract.MapUrl.MAP_ACCESS_KEY;
        Glide.with(this)
                .load(mapUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mapView);

        builder.setPositiveButton(getString(R.string.share), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendCustomMessage("LOCATION", customData);
            }
        }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void sendCustomMessage(String customType, JSONObject customData) {
        CustomMessage customMessage;

        if (type.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
            customMessage = new CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_USER, customType, customData);
        else
            customMessage = new CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_GROUP, customType, customData);

        customMessage.setParentMessageId(parentId);
        CometChat.sendCustomMessage(customMessage, new CometChat.CallbackListener<CustomMessage>() {
            @Override
            public void onSuccess(CustomMessage customMessage) {
                noReplyMessages.setVisibility(GONE);
                if (messageAdapter != null) {
                    messageAdapter.addMessage(customMessage);
                    setReply();
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

    private void turnOnLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Turn on GPS");
        builder.setPositiveButton("ON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), StringContract.RequestCode.LOCATION);
            }
        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        locationManager = (LocationManager) Objects.requireNonNull(getContext()).getSystemService(Context.LOCATION_SERVICE);
        if (Utils.hasPermissions(context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, StringContract.RequestCode.LOCATION);
        }
    }

    private void editThread(String editMessage) {
        isEdit = false;

        TextMessage textmessage;
        if (type.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
            textmessage = new TextMessage(Id, editMessage, CometChatConstants.RECEIVER_TYPE_USER);
        else
            textmessage = new TextMessage(Id, editMessage, CometChatConstants.RECEIVER_TYPE_GROUP);
        sendTypingIndicator(true);
        textmessage.setId(parentId);
        CometChat.editMessage(textmessage, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
                textMessage.setText(((TextMessage)baseMessage).getText());
                message = ((TextMessage) baseMessage).getText();
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: " + e.getMessage());
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
            case StringContract.RequestCode.LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { }
                else
                    showSnackBar(view.findViewById(R.id.message_box), getResources().getString(R.string.grant_location_permission));
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
                blockUserLayout.setVisibility(GONE);
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
            messagesRequest = new MessagesRequest.MessagesRequestBuilder().setLimit(LIMIT).setParentMessageId(parentId).hideMessagesFromBlockedUsers(true).build();
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
        messageShimmer.setVisibility(GONE);
    }


    private List<BaseMessage> filterBaseMessages(List<BaseMessage> baseMessages) {
        List<BaseMessage> tempList = new ArrayList<>();
        for(BaseMessage baseMessage : baseMessages)
        {
            Log.e(TAG, "filterBaseMessages: "+baseMessage.getSentAt());
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

        HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
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
            rvSmartReply.setVisibility(GONE);
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
            messageAdapter = new ThreadAdapter(getActivity(), messageList, type);
            rvChatListView.setAdapter(messageAdapter);
            messageAdapter.notifyDataSetChanged();
        } else {
            messageAdapter.updateList(messageList);

        }
        if (!isBlockedByMe && rvSmartReply.getAdapter().getItemCount()==0&&rvSmartReply.getVisibility() == GONE) {
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
        if (typingTimer!=null) {
            typingTimer.schedule(new TimerTask() {
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
            case StringContract.RequestCode.AUDIO:
                if (data!=null) {
                    File file = MediaUtils.getRealPath(getContext(),data.getData());
                    ContentResolver cr = getActivity().getContentResolver();
                    sendMediaMessage(file,CometChatConstants.MESSAGE_TYPE_AUDIO);
                }
                break;
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
            case StringContract.RequestCode.LOCATION:
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    Toast.makeText(getContext(), "Gps enabled",Toast.LENGTH_SHORT).show();
                    getLocation();
                }
                else {
                    Toast.makeText(getContext(), "Gps disabled",Toast.LENGTH_SHORT).show();
                }
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

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("path", file.getAbsolutePath());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mediaMessage.setMetadata(jsonObject);
        mediaMessage.setParentMessageId(parentId);
        CometChat.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage) {
                noReplyMessages.setVisibility(GONE);
                Log.d(TAG, "sendMediaMessage onSuccess: " + mediaMessage.toString());
                if (messageAdapter != null) {
                    setReply();
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
                        rvSmartReply.setVisibility(GONE);
                        toolbar.setSelected(false);
                        blockedUserName.setText("You've blocked " + user.getName());
                        blockUserLayout.setVisibility(View.VISIBLE);
                    } else {
                        isBlockedByMe = false;
                        blockUserLayout.setVisibility(GONE);
                    }
                    tvName.setText(String.format(getString(R.string.thread_in_name),user.getName()));
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
            senderAvatar.setAvatar(avatarUrl);
        else {
            senderAvatar.setInitials(name);
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
                    loggedInUserScope = group.getScope();
                    groupOwnerId = group.getOwner();

                    tvName.setText(String.format(getString(R.string.thread_in_name),group.getName()));
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

        textMessage.setParentMessageId(parentId);
        sendTypingIndicator(true);

        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                noReplyMessages.setVisibility(GONE);
                isSmartReplyClicked=false;
                if (messageAdapter != null) {
                    setReply();
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
        isParent = true;
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

    /**
     * This method is used to send reply message by link previous message with new message.
     * @param baseMessage is a linked message
     * @param message is a String. It will be new message sent as reply.
     */
    private void replyMessage(BaseMessage baseMessage, String message) {
        isReply = false;
        try {
            TextMessage textMessage;
            if (type.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER))
                textMessage = new TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_USER);
            else
                textMessage = new TextMessage(Id, message, CometChatConstants.RECEIVER_TYPE_GROUP);
            JSONObject jsonObject = new JSONObject();
            JSONObject replyObject = new JSONObject();
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                replyObject.put("type",CometChatConstants.MESSAGE_TYPE_TEXT);
                replyObject.put("message", ((TextMessage) baseMessage).getText());
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                replyObject.put("type",CometChatConstants.MESSAGE_TYPE_IMAGE);
                replyObject.put("message", "image");
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                replyObject.put("type",CometChatConstants.MESSAGE_TYPE_VIDEO);
                replyObject.put("message", "video");
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                replyObject.put("type",CometChatConstants.MESSAGE_TYPE_FILE);
                replyObject.put("message", "file");
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_AUDIO);
                replyObject.put("message", "audio");
            }
            replyObject.put("name",baseMessage.getSender().getName());
            replyObject.put("avatar",baseMessage.getSender().getAvatar());
            jsonObject.put("reply",replyObject);
            textMessage.setParentMessageId(parentId);
            textMessage.setMetadata(jsonObject);
            sendTypingIndicator(true);
            CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
                @Override
                public void onSuccess(TextMessage textMessage) {
                    if (messageAdapter != null) {
                        MediaUtils.playSendSound(context,R.raw.outgoing_message);
                        messageAdapter.addMessage(textMessage);
                        scrollToBottom();
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    Log.e(TAG, "onError: "+e.getMessage());
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "replyMessage: "+e.getMessage());
        }
    }

    private void scrollToBottom() {
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            rvChatListView.scrollToPosition(messageAdapter.getItemCount() - 1);
            final int scrollViewHeight = nestedScrollView.getHeight();
            if (scrollViewHeight > 0) {
                final View lastView = nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1);
                final int lastViewBottom = lastView.getBottom() + nestedScrollView.getPaddingBottom();
                final int deltaScrollY = lastViewBottom - scrollViewHeight - nestedScrollView.getScrollY();
                /* If you want to see the scroll animation, call this. */
                nestedScrollView.smoothScrollBy(0, deltaScrollY);
            }
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
            public void onCustomMessageReceived(CustomMessage message) {
                Log.d(TAG, "onCustomMessageReceived: "+ message.toString());
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
            if (Id != null && Id.equalsIgnoreCase(typingIndicator.getSender().getUid())) {
                if (typingIndicator.getMetadata() == null)
                    typingIndicator(typingIndicator, isShow);
            }
        } else {
            if (Id != null && Id.equalsIgnoreCase(typingIndicator.getReceiverId()))
                typingIndicator(typingIndicator, isShow);
        }
    }

    private void onMessageReceived(BaseMessage message) {
        MediaUtils.playSendSound(context,R.raw.incoming_message);
        if (message.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            if (Id != null && Id.equalsIgnoreCase(message.getSender().getUid())) {
                if (message.getParentMessageId()==parentId)
                    setMessage(message);
            } else if(Id != null && Id.equalsIgnoreCase(message.getReceiverUid()) && message.getSender().getUid().equalsIgnoreCase(loggedInUser.getUid())) {
                if (message.getParentMessageId()==parentId)
                    setMessage(message);
            }
        } else {
            if (Id != null && Id.equalsIgnoreCase(message.getReceiverUid())) {
                if (message.getParentMessageId()==parentId)
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
       setReply();
       noReplyMessages.setVisibility(GONE);
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

    private void setReply() {
        replyCount = replyCount+1;
        if (replyCount==1)
            tvReplyCount.setText(replyCount+" Reply");
        else
            tvReplyCount.setText(replyCount+" Replies");
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
                    tvTypingIndicator.setText("is Typing...");
                else
                    tvTypingIndicator.setText(typingIndicator.getSender().getName() + " is Typing...");
            } else {
                tvTypingIndicator.setVisibility(GONE);
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
        if (messageAdapter!=null)
            messageAdapter.stopPlayingAudio();
        removeMessageListener();
        sendTypingIndicator(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        messageAdapter = null;
        messagesRequest = null;
        checkOnGoingCall();
        fetchMessage();
        isNoMoreMessages = false;
        addMessageListener();

        if (type != null) {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                new Thread(this::getUser).start();
            } else {
                new Thread(this::getGroup).start();
            }
        }
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
        if (id == R.id.iv_close_message_action) {
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
        }
        else if (id == R.id.ic_more_option) {
            MessageActionFragment messageActionFragment = new MessageActionFragment();
            Bundle bundle = new Bundle();
            if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT))
                bundle.putBoolean("copyVisible",true);
            else
                bundle.putBoolean("copyVisible",false);

            bundle.putBoolean("forwardVisible",true);
            if (name.equals(loggedInUser.getName()) && messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                bundle.putBoolean("editVisible",true);
            } else {
                bundle.putBoolean("editVisible",false);
            }

            bundle.putString("type", CometChatThreadMessageActivity.class.getName());
            messageActionFragment.setArguments(bundle);
            showBottomSheet(messageActionFragment);
        }
        else if (id == R.id.ic_forward_option) {
            isParent = true;
            startForwardThreadActivity();
        }
        else if (id == R.id.iv_message_close) {
            if (messageAdapter != null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            isEdit = false;
            baseMessage = null;
            editMessageLayout.setVisibility(GONE);
        }
        else if (id == R.id.iv_reply_close) {
            if (messageAdapter!=null) {
                messageAdapter.clearLongClickSelectedItem();
                messageAdapter.notifyDataSetChanged();
            }
            isReply = false;
            baseMessage = null;
            replyMessageLayout.setVisibility(GONE);
        }

        else if (id == R.id.btn_unblock_user) {
            unblockUser();
        }
        else if (id == R.id.chatList_toolbar) {
            if (type.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                Intent intent = new Intent(getContext(), CometChatUserDetailScreenActivity.class);
                intent.putExtra(StringContract.IntentStrings.UID, Id);
                intent.putExtra(StringContract.IntentStrings.NAME, conversationName);
                intent.putExtra(StringContract.IntentStrings.AVATAR, avatarUrl);
                intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, isBlockedByMe);
                intent.putExtra(StringContract.IntentStrings.TYPE, type);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), CometChatGroupDetailScreenActivity.class);
                intent.putExtra(StringContract.IntentStrings.GUID, Id);
                intent.putExtra(StringContract.IntentStrings.NAME, conversationName);
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
        isReply = false;
        isEdit = false;
        isParent = false;
        MessageActionFragment messageActionFragment = new MessageActionFragment();
        replyMessageLayout.setVisibility(GONE);
        editMessageLayout.setVisibility(GONE);
        boolean copyVisible = true;
        boolean threadVisible = true;
        boolean replyVisible = false;
        boolean editVisible = true;
        boolean deleteVisible = true;
        boolean forwardVisible = true;
        boolean mapVisible = true;
        List<BaseMessage> textMessageList = new ArrayList<>();
        List<BaseMessage> mediaMessageList = new ArrayList<>();
        List<BaseMessage> locationMessageList = new ArrayList<>();
        for (BaseMessage baseMessage : baseMessagesList) {
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                textMessageList.add(baseMessage);
            }
            else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO) ){
                mediaMessageList.add(baseMessage);
            }
            else {
                locationMessageList.add(baseMessage);
            }
        }
        if (textMessageList.size() == 1) {
            BaseMessage basemessage = textMessageList.get(0);
            if (basemessage != null && basemessage.getSender() != null) {
                if (!(basemessage instanceof Action) && basemessage.getDeletedAt() == 0) {
                    baseMessage = basemessage;
                    threadVisible = false;
                    mapVisible = false;
                    if (basemessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        deleteVisible = true;
                        editVisible = true;
                        forwardVisible = true;
                    } else {
                        editVisible = false;
                        forwardVisible = true;
                        if (loggedInUserScope!=null && (loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN) || loggedInUserScope.equals(CometChatConstants.SCOPE_MODERATOR))) {
                            deleteVisible = true;
                        } else {
                            deleteVisible = false;
                        }
                    }
                }
            }
        }

        if (mediaMessageList.size() == 1) {
            BaseMessage basemessage = mediaMessageList.get(0);
            if (basemessage != null && basemessage.getSender() != null) {
                if (!(basemessage instanceof Action) && basemessage.getDeletedAt() == 0) {
                    baseMessage = basemessage;
                    copyVisible = false;
                    threadVisible = false;
                    mapVisible = false;
                    if (basemessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        deleteVisible = true;
                        editVisible = false;
                        forwardVisible = true;
                    } else {
                        if (loggedInUserScope!=null && (loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN) || loggedInUserScope.equals(CometChatConstants.SCOPE_MODERATOR))){
                            deleteVisible = true;
                        } else {
                            deleteVisible = false;
                        }
                        forwardVisible = true;
                        editVisible = false;
                    }
                }
            }
        }
        if (locationMessageList.size() == 1){
            BaseMessage basemessage = locationMessageList.get(0);
            if (basemessage != null && basemessage.getSender() != null) {
                if (!(basemessage instanceof Action) && basemessage.getDeletedAt() == 0) {
                    baseMessage = basemessage;
                    threadVisible = false;
                    copyVisible = false;
                    replyVisible = false;
                    forwardVisible = true;
                    if (basemessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                        mapVisible = true;
                        deleteVisible = true;
                        editVisible = false;
                    } else {
                        if (loggedInUserScope!=null && (loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN) || loggedInUserScope.equals(CometChatConstants.SCOPE_MODERATOR))){
                            deleteVisible = true;
                        } else {
                            deleteVisible = false;
                        }
                        mapVisible = true;
                        editVisible = false;
                    }
                }
            }
        }
        baseMessages = baseMessagesList;
        Bundle bundle = new Bundle();
        bundle.putBoolean("copyVisible",copyVisible);
        bundle.putBoolean("threadVisible",threadVisible);
        bundle.putBoolean("editVisible",editVisible);
        bundle.putBoolean("deleteVisible",deleteVisible);
        bundle.putBoolean("replyVisible",replyVisible);
        bundle.putBoolean("forwardVisible",forwardVisible);
        bundle.putBoolean("mapVisible",mapVisible);
        if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP) &&
                baseMessage.getSender().getUid().equals(loggedInUser.getUid()))
            bundle.putBoolean("messageInfoVisible",true);
        bundle.putString("type", CometChatThreadMessageActivity.class.getName());
        messageActionFragment.setArguments(bundle);
        showBottomSheet(messageActionFragment);
    }

    private void showBottomSheet(MessageActionFragment messageActionFragment) {
        messageActionFragment.show(getFragmentManager(),messageActionFragment.getTag());
        messageActionFragment.setMessageActionListener(new MessageActionFragment.MessageActionListener() {
            @Override
            public void onThreadMessageClick() {

            }

            @Override
            public void onEditMessageClick() {
                if (isParent)
                    editParentMessage();
                else
                    editThreadMessage();
            }

            @Override
            public void onReplyMessageClick() {

            }

            @Override
            public void onForwardMessageClick() {
                if (isParent)
                    startForwardThreadActivity();
                else
                    startForwardMessageActivity();
            }

            @Override
            public void onDeleteMessageClick() {
                deleteMessage(baseMessage);
                if (messageAdapter != null) {
                    messageAdapter.clearLongClickSelectedItem();
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCopyMessageClick() {
                String copyMessage = "";
                if (isParent) {
                    copyMessage = message;
                }
                for (BaseMessage bMessage : baseMessages) {
                    if (bMessage.getDeletedAt() == 0 && bMessage instanceof TextMessage) {
                        copyMessage = copyMessage + "[" + Utils.getLastMessageDate(bMessage.getSentAt()) + "] " + bMessage.getSender().getName() + ": " + ((TextMessage) bMessage).getText();
                    }
                }
                Log.e(TAG, "onCopy: " + message);
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("ThreadMessageAdapter", copyMessage);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, getResources().getString(R.string.text_copied_clipboard), Toast.LENGTH_LONG).show();
                isParent = true;
                if (messageAdapter != null) {
                    messageAdapter.clearLongClickSelectedItem();
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onShareMessageClick() {
                shareMessage();
            }

            @Override
            public void onMessageInfoClick() {
                Intent intent = new Intent(context, CometChatMessageInfoScreenActivity.class);
                if (isParent){
                }
                else {
                    intent.putExtra(StringContract.IntentStrings.ID, baseMessage.getId());
                    intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE, baseMessage.getType());
                    intent.putExtra(StringContract.IntentStrings.SENTAT, baseMessage.getSentAt());
                    if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                        intent.putExtra(StringContract.IntentStrings.TEXTMESSAGE,
                                Extensions.checkProfanityMessage(baseMessage));
                    } else if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_CUSTOM)) {
                        intent.putExtra(StringContract.IntentStrings.CUSTOM_MESSAGE,
                                ((CustomMessage) baseMessage).getCustomData().toString());
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE,
                                CometChatConstants.CATEGORY_CUSTOM);
                    } else {
                        boolean isImageNotSafe = Extensions.getImageModeration(context, baseMessage);
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL,
                                ((MediaMessage) baseMessage).getAttachment().getFileUrl());
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,
                                ((MediaMessage) baseMessage).getAttachment().getFileName());
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,
                                ((MediaMessage) baseMessage).getAttachment().getFileSize());
                        intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,
                                ((MediaMessage) baseMessage).getAttachment().getFileExtension());
                        intent.putExtra(StringContract.IntentStrings.IMAGE_MODERATION, isImageNotSafe);
                    }
                }
                context.startActivity(intent);
            }
        });
    }

    private void editParentMessage() {
        if (message!=null&&messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            isEdit = true;
            isReply = false;
            tvMessageTitle.setText(getResources().getString(R.string.edit_message));
            tvMessageSubTitle.setText(message);
            composeBox.ivSend.setVisibility(View.VISIBLE);
            editMessageLayout.setVisibility(View.VISIBLE);
            composeBox.etComposeBox.setText(message);
        }
    }

    private void shareMessage() {
        if (baseMessage!=null && baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TITLE,getResources().getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, ((TextMessage)baseMessage).getText());
            shareIntent.setType("text/plain");
            Intent intent = Intent.createChooser(shareIntent, getResources().getString(R.string.share_message));
            startActivity(intent);
        } else if (baseMessage!=null && baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            String mediaName = ((MediaMessage)baseMessage).getAttachment().getFileName();
            Glide.with(context).asBitmap().load(((MediaMessage)baseMessage).getAttachment().getFileUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), resource, mediaName, null);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                    shareIntent.setType(((MediaMessage)baseMessage).getAttachment().getFileMimeType());
                    Intent intent = Intent.createChooser(shareIntent, getResources().getString(R.string.share_message));
                    startActivity(intent);
                }
            });
        }
    }

    private void editThreadMessage() {
        if (baseMessage!=null&&baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
            isEdit = true;
            isReply = false;
            tvMessageTitle.setText(getResources().getString(R.string.edit_message));
            tvMessageSubTitle.setText(((TextMessage) baseMessage).getText());
            composeBox.ivSend.setVisibility(View.VISIBLE);
            editMessageLayout.setVisibility(View.VISIBLE);
            composeBox.etComposeBox.setText(((TextMessage) baseMessage).getText());
            if (messageAdapter != null) {
                messageAdapter.setSelectedMessage(baseMessage.getId());
                messageAdapter.notifyDataSetChanged();
            }
        }
    }

    private void startForwardThreadActivity() {
        Intent intent = new Intent(getContext(), CometChatForwardMessageScreenActivity.class);
        if (parentMessageCategory.equals(CometChatConstants.CATEGORY_MESSAGE)) {
            intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_MESSAGE);
            intent.putExtra(StringContract.IntentStrings.TYPE, messageType);
            if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, message);
                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT);
            } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                    messageType.equals(CometChatConstants.MESSAGE_TYPE_AUDIO) ||
                    messageType.equals(CometChatConstants.MESSAGE_TYPE_VIDEO) ||
                    messageType.equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, message);
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, message);
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, messageMimeType);
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, messageExtension);
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, messageSize);
            }
        } else {
            intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY,CometChatConstants.CATEGORY_CUSTOM);
            intent.putExtra(StringContract.IntentStrings.TYPE, StringContract.IntentStrings.LOCATION);
            try {
                intent.putExtra(StringContract.IntentStrings.LOCATION_LATITUDE,parentMessageLatitude);
                intent.putExtra(StringContract.IntentStrings.LOCATION_LONGITUDE,parentMessageLongitude);
            } catch (Exception e) {
                Log.e(TAG, "startForwardMessageActivityError: "+e.getMessage());
            }
        }
        startActivity(intent);
    }

    private void startForwardMessageActivity() {
        Intent intent = new Intent(getContext(), CometChatForwardMessageScreenActivity.class);
        if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_MESSAGE)) {
            intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY,CometChatConstants.CATEGORY_MESSAGE);
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, ((TextMessage) baseMessage).getText());
                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT);
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO) ||
                    baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, ((MediaMessage) baseMessage).getAttachment().getFileName());
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL, ((MediaMessage) baseMessage).getAttachment().getFileUrl());
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, ((MediaMessage) baseMessage).getAttachment().getFileMimeType());
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, ((MediaMessage) baseMessage).getAttachment().getFileExtension());
                intent.putExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, ((MediaMessage) baseMessage).getAttachment().getFileSize());
                intent.putExtra(StringContract.IntentStrings.TYPE, baseMessage.getType());
            }
        } else {
            intent.putExtra(StringContract.IntentStrings.MESSAGE_CATEGORY,CometChatConstants.CATEGORY_CUSTOM);
            intent.putExtra(StringContract.IntentStrings.TYPE, StringContract.IntentStrings.LOCATION);
            try {
                intent.putExtra(StringContract.IntentStrings.LOCATION_LATITUDE,
                        ((CustomMessage)baseMessage).getCustomData().getDouble("latitude"));
                intent.putExtra(StringContract.IntentStrings.LOCATION_LONGITUDE,
                        ((CustomMessage)baseMessage).getCustomData().getDouble("longitude"));
            } catch (Exception e) {
                Log.e(TAG, "startForwardMessageActivityError: "+e.getMessage());
            }
        }
        startActivity(intent);
    }


    private void replyMessage() {
        if (baseMessage != null) {
            isReply = true;
            replyTitle.setText(baseMessage.getSender().getName());
            replyMedia.setVisibility(View.VISIBLE);
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                replyMessage.setText(((TextMessage) baseMessage).getText());
                replyMedia.setVisibility(GONE);
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                replyMessage.setText(getResources().getString(R.string.shared_a_image));
                Glide.with(context).load(((MediaMessage) baseMessage).getAttachment().getFileUrl()).into(replyMedia);
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
                String messageStr = String.format(getResources().getString(R.string.shared_a_audio),
                        Utils.getFileSize(((MediaMessage) baseMessage).getAttachment().getFileSize()));
                replyMessage.setText(messageStr);
                replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_music_24dp, 0, 0, 0);
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                replyMessage.setText(getResources().getString(R.string.shared_a_video));
                Glide.with(context).load(((MediaMessage) baseMessage).getAttachment().getFileUrl()).into(replyMedia);
            } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                String messageStr = String.format(getResources().getString(R.string.shared_a_file),
                        Utils.getFileSize(((MediaMessage) baseMessage).getAttachment().getFileSize()));
                replyMessage.setText(messageStr);
                replyMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_drive_file_black_24dp, 0, 0, 0);
            }
            composeBox.ivSend.setVisibility(View.VISIBLE);
            replyMessageLayout.setVisibility(View.VISIBLE);
            if (messageAdapter != null) {
                messageAdapter.setSelectedMessage(baseMessage.getId());
                messageAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        if (messageAdapter!=null)
            messageAdapter.clearLongClickSelectedItem();
        dialog.dismiss();
    }
}
