package screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupMembersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.SharedMediaView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupMemberAdapter;
import constant.StringContract;
import listeners.ClickListener;
import listeners.RecyclerTouchListener;
import screen.addmember.CometChatAddMemberScreenActivity;
import screen.adminlist.CometChatAdminListScreenActivity;
import screen.unified.CometChatUnified;
import utils.FontUtils;
import utils.Utils;

import static utils.Utils.UserToGroupMember;


public class CometChatGroupDetailScreenActivity extends AppCompatActivity {

    private String TAG = "CometChatGroupDetail";

    private Avatar groupIcon;

    private String ownerId;

    private TextView tvGroupName;

    private TextView tvAdminCount;

    private ArrayList<String> groupMemberUids = new ArrayList<>();

    private RecyclerView rvMemberList;

    private String guid, gName;

    private GroupMembersRequest groupMembersRequest;

    private GroupMemberAdapter groupMemberAdapter;

    private int adminCount;

    String[] s = new String[0];

    private RelativeLayout rlAddMemberView;

    private RelativeLayout rlAdminListView;

    private String loggedInUserScope;

    private GroupMember groupMember;

    private TextView tvDelete;

    private TextView tvLoadMore;

    private List<GroupMember> groupMembers = new ArrayList<>();

    private static int LIMIT = 30;

    private User loggedInUser = CometChat.getLoggedInUser();

    private FontUtils fontUtils;

    private SharedMediaView sharedMediaView;

    private ImageView videoCallBtn;

    private ImageView callBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_chat_group_detail_screen);
        fontUtils= FontUtils.getInstance(this);
        initComponent();

    }

    private void initComponent() {

        groupIcon = findViewById(R.id.iv_group);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvAdminCount = findViewById(R.id.tv_admin_count);
        rvMemberList = findViewById(R.id.member_list);
        tvLoadMore = findViewById(R.id.tv_load_more);
        tvLoadMore.setText(String.format(getResources().getString(R.string.load_more_members),LIMIT));
        TextView tvAddMember = findViewById(R.id.tv_add_member);
        callBtn = findViewById(R.id.callBtn_iv);
        videoCallBtn = findViewById(R.id.video_callBtn_iv);
        rlAddMemberView = findViewById(R.id.rl_add_member);
        rlAddMemberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMembers();
            }
        });

        rlAdminListView = findViewById(R.id.rlAdminView);
        rlAdminListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAdminListScreen();
            }
        });
        tvDelete = findViewById(R.id.tv_delete);
        TextView tvExit = findViewById(R.id.tv_exit);
        MaterialToolbar toolbar = findViewById(R.id.groupDetailToolbar);

        tvDelete.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        tvExit.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        tvAddMember.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvMemberList.setLayoutManager(linearLayoutManager);
//        rvMemberList.setNestedScrollingEnabled(false);

        handleIntent();

        sharedMediaView = findViewById(R.id.shared_media_view);
        sharedMediaView.setRecieverId(guid);
        sharedMediaView.setRecieverType(CometChatConstants.RECEIVER_TYPE_GROUP);
        sharedMediaView.reload();

        rvMemberList.addOnItemTouchListener(new RecyclerTouchListener(this, rvMemberList, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                GroupMember user = (GroupMember) var1.getTag(R.string.user);
                if (loggedInUserScope != null&&loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN)) {
                    groupMember = user;
                    boolean isAdmin =user.getScope().equals(CometChatConstants.SCOPE_ADMIN) ;
                    boolean isSelf = loggedInUser.getUid().equals(user.getUid());
                    boolean isOwner = loggedInUser.getUid().equals(ownerId);
                    if (!isSelf) {
                        if (!isAdmin||isOwner) {
                            registerForContextMenu(rvMemberList);
                            openContextMenu(var1);
                        }
                    }
                }
            }

            @Override
            public void onLongClick(View var1, int var2) {

            }
        }));

        tvLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGroupMembers();
            }
        });
        tvExit.setOnClickListener(view -> createDialog(getResources().getString(R.string.exit_group_title), getResources().getString(R.string.exit_group_message),
                getResources().getString(R.string.exit), getResources().getString(R.string.cancel), R.drawable.ic_exit_to_app));

        callBtn.setOnClickListener(view -> initiateGroupCall(guid,CometChatConstants.RECEIVER_TYPE_GROUP,CometChatConstants.CALL_TYPE_AUDIO) );

        videoCallBtn.setOnClickListener(view -> initiateGroupCall(guid,CometChatConstants.RECEIVER_TYPE_GROUP,CometChatConstants.CALL_TYPE_VIDEO));

        tvDelete.setOnClickListener(view -> createDialog(getResources().getString(R.string.delete_group_title), getResources().getString(R.string.delete_group_message),
                getResources().getString(R.string.delete), getResources().getString(R.string.cancel), R.drawable.ic_delete_24dp));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group_action_menu, menu);

        menu.findItem(R.id.item_make_admin).setVisible(false);

        menu.setHeaderTitle("Group Action");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.item_remove) {
            kickMember();
        }

        return super.onContextItemSelected(item);
    }

    public void initiateGroupCall(String recieverID,String receiverType,String callType)
    {
        Call call = new Call(recieverID,receiverType,callType);
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Utils.startGroupCallIntent(CometChatGroupDetailScreenActivity.this,((Group)call.getCallReceiver()),call.getType(),true,call.getSessionId());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
                if (rvMemberList!=null)
                    Snackbar.make(rvMemberList,getResources().getString(R.string.call_initiate_error)+":"+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method is used to create dialog box on click of events like <b>Delete Group</b> and <b>Exit Group</b>
     * @param title
     * @param message
     * @param positiveText
     * @param negativeText
     * @param drawableRes
     */
    private void createDialog(String title, String message, String positiveText, String negativeText, int drawableRes) {

        MaterialAlertDialogBuilder alert_dialog = new MaterialAlertDialogBuilder(CometChatGroupDetailScreenActivity.this,
                R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered);
        alert_dialog.setTitle(title);
        alert_dialog.setMessage(message);
        alert_dialog.setPositiveButton(positiveText, (dialogInterface, i) -> {

            if (positiveText.equalsIgnoreCase(getResources().getString(R.string.exit)))
                leaveGroup();

            else if (positiveText.equalsIgnoreCase(getResources().getString(R.string.delete))
                    && loggedInUserScope.equalsIgnoreCase(CometChatConstants.SCOPE_ADMIN))
                deleteGroup();

        });

        alert_dialog.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert_dialog.create();
        alert_dialog.show();

    }


    /**
     * This method is used to handle the intent passed to this activity.
     */
    private void handleIntent() {
        if (getIntent().hasExtra(StringContract.IntentStrings.GUID)) {
            guid = getIntent().getStringExtra(StringContract.IntentStrings.GUID);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MEMBER_SCOPE)) {
            loggedInUserScope = getIntent().getStringExtra(StringContract.IntentStrings.MEMBER_SCOPE);
            if (loggedInUserScope != null && loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN)) {
                rlAddMemberView.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
            }

        }
        if (getIntent().hasExtra(StringContract.IntentStrings.NAME)) {
            gName = getIntent().getStringExtra(StringContract.IntentStrings.NAME);
            tvGroupName.setText(gName);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.AVATAR)) {
            String avatar = getIntent().getStringExtra(StringContract.IntentStrings.AVATAR);
            if (avatar != null && !avatar.isEmpty())
                groupIcon.setAvatar(avatar);
            else
                groupIcon.setInitials(gName);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.GROUP_OWNER)) {
            ownerId = getIntent().getStringExtra(StringContract.IntentStrings.GROUP_OWNER);
        }
    }


    /**
     * This method is used whenever user click <b>Administrator</b>. It takes user to
     * <code>CometChatAdminListScreenActivity.class</code>
     *
     * @see CometChatAdminListScreenActivity
     */
    public void openAdminListScreen() {
        Intent intent = new Intent(this, CometChatAdminListScreenActivity.class);
        intent.putExtra(StringContract.IntentStrings.GUID, guid);
        intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, ownerId);
        intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope);
        startActivity(intent);
    }

    /**
     * This method is used whenever user click <b>Add Member</b>. It takes user to
     * <code>CometChatAddMemberScreenActivity.class</code>
     *
     * @see CometChatAddMemberScreenActivity
     */
    public void addMembers() {
        Intent intent = new Intent(this, CometChatAddMemberScreenActivity.class);
        intent.putExtra(StringContract.IntentStrings.GUID, guid);
        intent.putExtra(StringContract.IntentStrings.GROUP_MEMBER, groupMemberUids);
        intent.putExtra(StringContract.IntentStrings.GROUP_NAME, gName);
        intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope);
        intent.putExtra(StringContract.IntentStrings.IS_ADD_MEMBER, true);
        startActivity(intent);
    }

    /**
     * This method is used to delete Group. It is used only if loggedIn user is admin.
     */
    private void deleteGroup() {
        CometChat.deleteGroup(guid, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                launchUnified();
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(rvMemberList, getResources().getString(R.string.group_delete_error), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    private void launchUnified() {
        Intent intent = new Intent(CometChatGroupDetailScreenActivity.this, CometChatUnified.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    /**
     * This method is used to kick group member from the group. It is used only if loggedIn user is admin.
     *
     * @see CometChat#kickGroupMember(String, String, CometChat.CallbackListener)
     */
    private void kickMember() {
        CometChat.kickGroupMember(groupMember.getUid(), guid, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "onSuccess: " + s);
                groupMemberUids.remove(groupMember.getUid());
                groupMemberAdapter.removeGroupMember(groupMember);
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(rvMemberList, String.format(getResources().getString(R.string.cannot_remove_member),groupMember.getName()), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    /**
     * This method is used to get list of group members. It also helps to update other things like
     * Admin count.
     *
     * @see GroupMembersRequest#fetchNext(CometChat.CallbackListener)
     * @see GroupMember
     */
    private void getGroupMembers() {
        if (groupMembersRequest == null) {
            groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(guid).setLimit(LIMIT).build();
        }
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                Log.e(TAG, "onSuccess: " + groupMembers.size());
                if (groupMembers != null && groupMembers.size() != 0) {
                    adminCount = 0;
                    groupMemberUids.clear();
                    s = new String[groupMembers.size()];
                    for (int j = 0; j < groupMembers.size(); j++) {
                        groupMemberUids.add(groupMembers.get(j).getUid());
                        if (groupMembers.get(j).getScope().equals(CometChatConstants.SCOPE_ADMIN)) {
                            adminCount++;
                        }
                        s[j] = groupMembers.get(j).getName();
                    }
                    tvAdminCount.setText(adminCount + "");
                    if (groupMemberAdapter == null) {
                        groupMemberAdapter = new GroupMemberAdapter(CometChatGroupDetailScreenActivity.this, groupMembers, ownerId);
                        rvMemberList.setAdapter(groupMemberAdapter);
                    } else {
                        groupMemberAdapter.addAll(groupMembers);
                    }
                    if (groupMembers.size()<LIMIT)
                    {
                        tvLoadMore.setVisibility(View.GONE);
                    }
                }
                else
                {
                    tvLoadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(rvMemberList, getResources().getString(R.string.group_member_list_error), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }


    /**
     * This method is used to leave the loggedIn User from respective group.
     *
     * @see CometChat#leaveGroup(String, CometChat.CallbackListener)
     */
    private void leaveGroup() {
        CometChat.leaveGroup(guid, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                launchUnified();
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(rlAddMemberView, getResources().getString(R.string.leave_group_error), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    /**
     * This method is used to add group listener in this screen to receive real-time events.
     *
     * @see CometChat#addGroupListener(String, CometChat.GroupListener)
     */
    public void addGroupListener() {
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                Log.e(TAG, "onGroupMemberJoined: " + joinedUser.getUid());
                if (joinedGroup.getGuid().equals(guid))
                    updateGroupMember(joinedUser,false,false,action);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                Log.d(TAG, "onGroupMemberLeft: ");
                if (leftGroup.getGuid().equals(guid))
                    updateGroupMember(leftUser,true,false,action);
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                Log.d(TAG, "onGroupMemberKicked: ");
                if (kickedFrom.getGuid().equals(guid))
                    updateGroupMember(kickedUser,true,false,action);
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
                Log.d(TAG, "onGroupMemberScopeChanged: ");
                if (group.getGuid().equals(guid))
                    updateGroupMember(updatedUser,false,true,action);
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedby, User userAdded, Group addedTo) {
                if (addedTo.getGuid().equals(guid))
                    updateGroupMember(userAdded,false,false,action);
            }
        });
    }

    /**
     * This method is used to update group members from events recieved in real time. It updates or removes
     * group member from list based on parameters passed.
     *
     * @param user is a object of User.
     * @param isRemoved is a boolean which helps to know whether group member needs to be removed from list or not.
     * @param isScopeUpdate is a boolean which helps to know whether group member scope is updated or not.
     * @param action is object of Action.
     *
     * @see Action
     * @see GroupMember
     * @see User
     * @see utils.Utils#UserToGroupMember(User, boolean, String)
     */
    private void updateGroupMember(User user, boolean isRemoved, boolean isScopeUpdate, Action action) {
        if (groupMemberAdapter != null) {
            if (!isRemoved && !isScopeUpdate)
                groupMemberAdapter.addGroupMember(UserToGroupMember(user, false, action.getOldScope()));
            else if (isRemoved && !isScopeUpdate)
                groupMemberAdapter.removeGroupMember(UserToGroupMember(user, false, action.getOldScope()));
            else if (!isRemoved)
            {
                groupMemberAdapter.updateMember(UserToGroupMember(user, true, action.getNewScope()));
                if (action.getNewScope().equals(CometChatConstants.SCOPE_ADMIN)) {
                    adminCount = adminCount + 1;
                    tvAdminCount.setText(String.valueOf(adminCount));
                    if (user.getUid().equals(loggedInUser.getUid())) {
                        rlAddMemberView.setVisibility(View.VISIBLE);
                        loggedInUserScope = CometChatConstants.SCOPE_ADMIN;
                        tvDelete.setVisibility(View.VISIBLE);
                    } else {
                        loggedInUserScope = action.getNewScope();
                        rlAddMemberView.setVisibility(View.GONE);
                        tvDelete.setVisibility(View.GONE);
                    }
                } else if (action.getOldScope().equals(CometChatConstants.SCOPE_ADMIN)) {
                    adminCount = adminCount - 1;
                    tvAdminCount.setText(String.valueOf(adminCount));
                }
            }
        }
    }

    /**
     * This method is used to remove group listener.
     */
    public void removeGroupListener() {
        CometChat.removeGroupListener(TAG);
    }


    @Override
    protected void onResume() {
        super.onResume();
        groupMembersRequest = null;
        if (groupMemberAdapter != null) {
            groupMemberAdapter.resetAdapter();
            groupMemberAdapter = null;

        }
        getGroupMembers();
        addGroupListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        removeGroupListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeGroupListener();
    }
}
