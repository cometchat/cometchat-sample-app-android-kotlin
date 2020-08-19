package screen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.SharedMedia.SharedFilesFragment;
import com.cometchat.pro.uikit.SharedMedia.SharedImagesFragment;
import com.cometchat.pro.uikit.SharedMedia.SharedVideosFragment;
import com.cometchat.pro.uikit.SharedMediaView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.CallHistoryAdapter;
import constant.StringContract;
import screen.messagelist.CometChatMessageListActivity;
import utils.FontUtils;
import utils.Utils;

public class CometChatUserDetailScreenActivity extends AppCompatActivity {
    private Avatar userAvatar;

    private TextView userStatus, userName, addBtn;

    private String name;

    private String TAG = "CometChatUserDetailScreenActivity";

    private String avatar;

    private String uid;

    private String guid;

    private String groupName;

    private boolean isAddMember;

    private boolean isAlreadyAdded;

    private TextView tvSendMessage;

    private TextView tvBlockUser;

    private MaterialToolbar toolbar;

    private boolean isBlocked;

    private FontUtils fontUtils;

    private ImageView callBtn;

    private ImageView vidoeCallBtn;

    private LinearLayout historyView;

    private RecyclerView historyRv;

    private CallHistoryAdapter callHistoryAdapter;

    private MessagesRequest messageRequest;

    private SharedMediaView sharedMediaView;

    private boolean inProgress;

    private boolean fromCallList;

    private View divider1,divider2,divider3;

    private List<BaseMessage> callList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_screen);
        fontUtils= FontUtils.getInstance(this);
        initComponent();

    }

    private void initComponent() {

        historyView = findViewById(R.id.history_view);
        historyRv = findViewById(R.id.history_rv);
        userAvatar = findViewById(R.id.iv_user);
        userName = findViewById(R.id.tv_name);
        userStatus = findViewById(R.id.tv_status);
        callBtn = findViewById(R.id.callBtn_iv);
        vidoeCallBtn = findViewById(R.id.video_callBtn_iv);
        addBtn = findViewById(R.id.btn_add);
        tvSendMessage = findViewById(R.id.tv_send_message);
        toolbar= findViewById(R.id.user_detail_toolbar);
        divider1 = findViewById(R.id.divider_1);
        divider2 = findViewById(R.id.divider_2);
        divider3 = findViewById(R.id.divider_3);

        setSupportActionBar(toolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addBtn.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));

        tvBlockUser = findViewById(R.id.tv_blockUser);

        tvBlockUser.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));

        userName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));


        handleIntent();


        sharedMediaView = findViewById(R.id.shared_media_view);
        sharedMediaView.setRecieverId(uid);
        sharedMediaView.setRecieverType(CometChatConstants.RECEIVER_TYPE_USER);
        sharedMediaView.reload();
        
        checkDarkMode();
        addBtn.setOnClickListener(view -> {

            if (guid != null) {
                if (isAddMember) {
                    if (isAlreadyAdded)
                        kickGroupMember();
                    else
                        addMember();
                }
            }
        });

        tvSendMessage.setOnClickListener(view -> {
              if (isAddMember || fromCallList){
                  Intent intent=new Intent(CometChatUserDetailScreenActivity.this, CometChatMessageListActivity.class);
                  intent.putExtra(StringContract.IntentStrings.TYPE,CometChatConstants.RECEIVER_TYPE_USER);
                  intent.putExtra(StringContract.IntentStrings.UID,uid);
                  intent.putExtra(StringContract.IntentStrings.NAME,name);
                  intent.putExtra(StringContract.IntentStrings.AVATAR,avatar);
                  intent.putExtra(StringContract.IntentStrings.STATUS,CometChatConstants.USER_STATUS_OFFLINE);
                  startActivity(intent);
              }else
                  onBackPressed();
        });

        tvBlockUser.setOnClickListener(view -> {
            if (isBlocked)
               unblockUser();
            else
                blockUser();
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOnGoingCall(CometChatConstants.CALL_TYPE_AUDIO);
            }
        });
        vidoeCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOnGoingCall(CometChatConstants.CALL_TYPE_VIDEO);
            }
        });
    }

    private void checkDarkMode() {
        if (Utils.isDarkMode(this)) {
            userName.setTextColor(getResources().getColor(R.color.textColorWhite));
            divider1.setBackgroundColor(getResources().getColor(R.color.grey));
            divider2.setBackgroundColor(getResources().getColor(R.color.grey));
            divider3.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            userName.setTextColor(getResources().getColor(R.color.primaryTextColor));
            divider1.setBackgroundColor(getResources().getColor(R.color.light_grey));
            divider2.setBackgroundColor(getResources().getColor(R.color.light_grey));
            divider3.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }
    }

    private void checkOnGoingCall(String callType) {
        if(CometChat.getActiveCall()!=null && CometChat.getActiveCall().getCallStatus().equals(CometChatConstants.CALL_STATUS_ONGOING) && CometChat.getActiveCall().getSessionId()!=null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.ongoing_call))
                    .setMessage(getResources().getString(R.string.ongoing_call_message))
                    .setPositiveButton(getResources().getString(R.string.join), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.joinOnGoingCall(CometChatUserDetailScreenActivity.this);
                        }
                    }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callBtn.setEnabled(true);
                    vidoeCallBtn.setEnabled(true);
                    dialog.dismiss();
                }
            }).create().show();
        }
        else {
            Utils.initiatecall(CometChatUserDetailScreenActivity.this,uid,CometChatConstants.RECEIVER_TYPE_USER,callType);
        }
    }


    private void handleIntent() {

        if (getIntent().hasExtra(StringContract.IntentStrings.IS_ADD_MEMBER)) {
            isAddMember = getIntent().getBooleanExtra(StringContract.IntentStrings.IS_ADD_MEMBER, false);
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.FROM_CALL_LIST)) {
            fromCallList = getIntent().getBooleanExtra(StringContract.IntentStrings.FROM_CALL_LIST,false);
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME)){
            isBlocked=getIntent().getBooleanExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME,false);
             setBlockUnblock();
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.GUID)) {
            guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.UID)) {
            uid = getIntent().getStringExtra(StringContract.IntentStrings.UID);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.GROUP_NAME)) {
            groupName = getIntent().getStringExtra(StringContract.IntentStrings.GROUP_NAME);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.NAME)) {
            name = getIntent().getStringExtra(StringContract.IntentStrings.NAME);
            userName.setText(name);
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.AVATAR)) {
            avatar = getIntent().getStringExtra(StringContract.IntentStrings.AVATAR);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.STATUS)) {
            String status = getIntent().getStringExtra(StringContract.IntentStrings.STATUS);

            if (status != null && status.equals(CometChatConstants.USER_STATUS_ONLINE))
                userStatus.setTextColor(getResources().getColor(R.color.colorPrimary));

            userStatus.setText(status);
        }

        if (avatar != null && !avatar.isEmpty())
            userAvatar.setAvatar(avatar);
        else {
            if (name != null && !name.isEmpty())
                userAvatar.setInitials(name);
            else
                userAvatar.setInitials("Unknown");
        }

        if (isAddMember) {
            addBtn.setText(String.format(getResources().getString(R.string.add_user_to_group),name,groupName));
            historyView.setVisibility(View.GONE);
        } else {
            fetchCallHistory();
            addBtn.setVisibility(View.GONE);
        }
    }

    private void fetchCallHistory() {
        if (messageRequest==null)
        {
            messageRequest = new MessagesRequest.MessagesRequestBuilder().setUID(uid).setCategory(CometChatConstants.CATEGORY_CALL).setLimit(30).build();
        }
        messageRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List<BaseMessage> messageList) {
                if (messageList.size()!=0) {
                    callList.addAll(messageList);
                    setCallHistoryAdapter(messageList);
                }
                if (callList.size()!=0)
                    historyView.setVisibility(View.VISIBLE);
                else
                    historyView.setVisibility(View.GONE);
            }

            @Override
            public void onError(CometChatException e) {

            }
        });
    }

    private void setCallHistoryAdapter(List<BaseMessage> messageList) {
        if (callHistoryAdapter==null)
        {
            callHistoryAdapter = new CallHistoryAdapter(CometChatUserDetailScreenActivity.this,messageList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
            historyRv.setLayoutManager(linearLayoutManager);
            historyRv.setAdapter(callHistoryAdapter);
        }
        else
            callHistoryAdapter.updateList(messageList);
    }

    private void setBlockUnblock() {
        if (isBlocked) {
            tvBlockUser.setTextColor(getResources().getColor(R.color.online_green));
            tvBlockUser.setText(getResources().getString(R.string.unblock_user));
        } else{
            tvBlockUser.setText(getResources().getString(R.string.block_user));
            tvBlockUser.setTextColor(getResources().getColor(R.color.red));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

         if (item.getItemId()==android.R.id.home){
             onBackPressed();
         }
        return super.onOptionsItemSelected(item);
    }

    private void addMember() {
        List<GroupMember> userList = new ArrayList<>();
        userList.add(new GroupMember(uid, CometChatConstants.SCOPE_PARTICIPANT));
        CometChat.addMembersToGroup(guid, userList, null, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> stringStringHashMap) {
                Log.e(TAG, "onSuccess: " + uid + "Group" + guid);
                if(tvBlockUser!=null)
                    Snackbar.make(tvBlockUser,String.format(getResources().getString(R.string.user_added_to_group),userName.getText().toString(), groupName), Snackbar.LENGTH_LONG).show();
                addBtn.setText(String.format(getResources().getString(R.string.remove_from_group),groupName));
                isAlreadyAdded = true;
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(CometChatUserDetailScreenActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void kickGroupMember() {

        CometChat.kickGroupMember(uid, guid, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (tvBlockUser!=null)
                    Snackbar.make(tvBlockUser, String.format(getResources().getString(R.string.user_removed_from_group),userName.getText().toString(),groupName), Snackbar.LENGTH_LONG).show();
                addBtn.setText(String.format(getResources().getString(R.string.add_in_group),groupName));
                addBtn.setVisibility(View.VISIBLE);
                isAlreadyAdded = false;

            }

            @Override
            public void onError(CometChatException e) {
                if (tvBlockUser!=null)
                    Snackbar.make(tvBlockUser, getResources().getString(R.string.kicked_error), Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void unblockUser() {
        ArrayList<String> uids = new ArrayList<>();
        uids.add(uid);

      CometChat.unblockUsers(uids, new CometChat.CallbackListener<HashMap<String, String>>() {
          @Override
          public void onSuccess(HashMap<String, String> stringStringHashMap) {
              if (tvBlockUser!=null)
                  Snackbar.make(tvBlockUser,String.format(getResources().getString(R.string.user_unblocked),userName.getText().toString()),Snackbar.LENGTH_SHORT).show();
              isBlocked=false;
              setBlockUnblock();
          }

          @Override
          public void onError(CometChatException e) {
              Log.d(TAG, "onError: "+e.getMessage());
              if (tvBlockUser!=null)
                 Snackbar.make(tvBlockUser,getResources().getString(R.string.unblock_user_error),Snackbar.LENGTH_SHORT).show();
          }
      });
    }


    private void blockUser() {

        ArrayList<String> uids = new ArrayList<>();
        uids.add(uid);
        CometChat.blockUsers(uids, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> stringStringHashMap) {
                if (tvBlockUser!=null)
                    Snackbar.make(tvBlockUser,String.format(getResources().getString(R.string.user_is_blocked),userName.getText().toString()),Snackbar.LENGTH_SHORT).show();
                isBlocked=true;
                setBlockUnblock();
            }

            @Override
            public void onError(CometChatException e) {
                if (tvBlockUser!=null)
                    Snackbar.make(tvBlockUser,String.format(getResources().getString(R.string.block_user_error),userName.getText().toString()),Snackbar.LENGTH_SHORT).show();
                Log.d(TAG, "onError: "+e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        groupListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CometChat.removeGroupListener(TAG);
    }

    private void groupListener()
    {
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                updateBtn(joinedUser,R.string.remove_from_group);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                updateBtn(leftUser,R.string.add_in_group);
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                updateBtn(kickedUser,R.string.add_in_group);
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedby, User userAdded, Group addedTo) {
                updateBtn(userAdded,R.string.remove_from_group);
            }
        });
    }

    private void updateBtn(User user, int resource_string) {
        if (user.getUid().equals(uid))
            addBtn.setText(String.format(getResources().getString(resource_string), groupName ));
    }
}
