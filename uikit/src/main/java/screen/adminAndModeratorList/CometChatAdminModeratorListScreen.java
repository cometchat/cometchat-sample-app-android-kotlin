package screen.adminAndModeratorList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupMembersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupMemberAdapter;
import constant.StringContract;
import listeners.ClickListener;
import listeners.RecyclerTouchListener;
import screen.groupmemberlist.CometChatGroupMemberListScreenActivity;
import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - CometChatAdminListScreen.class is a screen used to display List of admin's of a particular
 * group. It also helps to perform action like remove as admin, add as admin on group members.
 * <p>
 * Created on - 20th December 2019
 * <p>
 * Modified on  - 16th January 2020
 */

public class CometChatAdminModeratorListScreen extends Fragment {

    private RecyclerView adminList;

    private String ownerId;

    private boolean showModerators;

    private String guid;    //It is guid of group whose members are been fetched.

    private GroupMembersRequest groupMembersRequest;    //Used to fetch group members list.

    private ArrayList<GroupMember> members = new ArrayList<>();

    private GroupMemberAdapter adapter;

    private String TAG = "CometChatAdminListScreen";

    private String loggedInUserScope;

    private User loggedInUser = CometChat.getLoggedInUser();

    private FontUtils fontUtils;

    private TextView addAs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            fontUtils=FontUtils.getInstance(getActivity());

        handleArguments();
    }

    private void handleArguments() {

        if (getArguments() != null) {
            guid = getArguments().getString(StringContract.IntentStrings.GUID);
            loggedInUserScope = getArguments().getString(StringContract.IntentStrings.MEMBER_SCOPE);
            ownerId = getArguments().getString(StringContract.IntentStrings.GROUP_OWNER);
            showModerators = getArguments().getBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comet_chat_admin_list_screen, container, false);
        adminList = view.findViewById(R.id.adminList);
        setHasOptionsMenu(true);
        RelativeLayout rlAddMember = view.findViewById(R.id.rl_add_Admin);
        addAs = view.findViewById(R.id.add_as_tv);
        MaterialToolbar toolbar = view.findViewById(R.id.admin_toolbar);
        setToolbar(toolbar);
        if (showModerators) {
            toolbar.setTitle(getResources().getString(R.string.moderators));
            addAs.setText(getResources().getString(R.string.add_as_moderator));
        } else {
            toolbar.setTitle(getResources().getString(R.string.administrators));
            addAs.setText(getResources().getString(R.string.add_as_admin));
        }
        adapter = new GroupMemberAdapter(getContext(), members, null);
        adminList.setAdapter(adapter);
        if (loggedInUserScope != null && loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN)) {
            rlAddMember.setVisibility(View.VISIBLE);
        }
        if (showModerators){
            getModeratorList(guid);
        }
        else {
            getAdminList(guid);
        }
        rlAddMember.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), CometChatGroupMemberListScreenActivity.class);
            intent.putExtra(StringContract.IntentStrings.GUID, guid);
            intent.putExtra(StringContract.IntentStrings.SHOW_MODERATORLIST,showModerators);
            startActivity(intent);
        });
        adminList.addOnItemTouchListener(new RecyclerTouchListener(getContext(), adminList, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                GroupMember groupMember = (GroupMember) var1.getTag(R.string.user);
                if (showModerators) {
                    if (loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN) && !groupMember.getUid().equals(loggedInUser.getUid())) {
                        if (getActivity() != null) {
                            MaterialAlertDialogBuilder alert_dialog = new MaterialAlertDialogBuilder(getActivity());
                            alert_dialog.setTitle(getResources().getString(R.string.remove));
                            alert_dialog.setMessage(String.format(getResources().getString(R.string.remove_from_moderator), groupMember.getName()));
                            alert_dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateMemberScope(groupMember, var1);
                                }
                            });
                            alert_dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alert_dialog.create();
                            alert_dialog.show();
                        }
                    } else {
                        String message;
                        if (groupMember.getUid().equals(loggedInUser.getUid()))
                            message = getResources().getString(R.string.you_cannot_perform_action);
                        else
                            message = getResources().getString(R.string.only_admin_removes_moderator);

                        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    if (ownerId != null && loggedInUser.getUid().equals(ownerId) && loggedInUserScope.equals(CometChatConstants.SCOPE_ADMIN) && !groupMember.getUid().equals(loggedInUser.getUid())) {
                        if (getActivity() != null) {
                            MaterialAlertDialogBuilder alert_dialog = new MaterialAlertDialogBuilder(getActivity());
                            alert_dialog.setTitle(getResources().getString(R.string.remove));
                            alert_dialog.setMessage(String.format(getResources().getString(R.string.remove_from_admin), groupMember.getName()));
                            alert_dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateMemberScope(groupMember, var1);
                                }
                            });
                            alert_dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alert_dialog.create();
                            alert_dialog.show();
                        }
                    }  else {
                        String message;
                        if (groupMember.getUid().equals(loggedInUser.getUid()))
                            message = getResources().getString(R.string.you_cannot_perform_action);
                        else
                            message = getResources().getString(R.string.only_group_owner_removes_admin);

                        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }));
        return view;
    }

    private void setToolbar(MaterialToolbar toolbar) {
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar).setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        }
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getActivity() != null)
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMemberScope(GroupMember groupMember, View view) {

        CometChat.updateGroupMemberScope(groupMember.getUid(), guid, CometChatConstants.SCOPE_PARTICIPANT,
                new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (adapter != null)
                            adapter.removeGroupMember(groupMember);
                        if (showModerators) {
                            Snackbar.make(view, String.format(getResources().getString(R.string.removed_from_moderator), groupMember.getName()),
                                    Snackbar.LENGTH_LONG).show();
                        }
                        else {
                            Snackbar.make(view, String.format(getResources().getString(R.string.removed_from_admin), groupMember.getName()),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(CometChatException e) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.update_group_member_error), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onError: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * This method is used to fetch Admin List.
     *
     * @param groupId is a unique id of group. It is used to fetch admin list of particular group.
     */
    private void getAdminList(String groupId) {
        if (groupMembersRequest == null) {
            groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(100).build();
        }
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                ArrayList<GroupMember> memberList = new ArrayList<>();
                for (GroupMember groupMember : groupMembers) {

                    if (groupMember.getScope().equals(CometChatConstants.SCOPE_ADMIN)) {
                        memberList.add(groupMember);
                    }
                }
                adapter.addAll(memberList);
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(adminList, getResources().getString(R.string.admin_list_retrieve_error), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());

            }
        });
    }
    /**
     * This method is used to fetch Moderator List.
     *
     * @param groupId is a unique id of group. It is used to fetch moderator list of particular group.
     */
    private void getModeratorList(String groupId) {
        if (groupMembersRequest == null) {
            groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(100).build();
        }
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                ArrayList<GroupMember> memberList = new ArrayList<>();
                for (GroupMember groupMember : groupMembers) {

                    if (groupMember.getScope().equals(CometChatConstants.SCOPE_MODERATOR)) {
                        memberList.add(groupMember);
                    }
                }
                adapter.addAll(memberList);
            }

            @Override
            public void onError(CometChatException e) {
                Snackbar.make(adminList, getResources().getString(R.string.modertor_list_retrieve_error), Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onError: " + e.getMessage());

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        groupMembersRequest = null;
        if (guid != null) {
            if (showModerators) {
                getModeratorList(guid);
            } else {
                getAdminList(guid);
            }
        }
        addGroupListener();

    }

    @Override
    public void onPause() {
        super.onPause();
        CometChat.removeGroupListener(TAG);
    }

    private void addGroupListener() {
        CometChat.addGroupListener(TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                    updateGroupMember(leftUser,true,null);
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                    updateGroupMember(kickedUser,true,null);
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
                    if (action.getNewScope().equals(CometChatConstants.SCOPE_ADMIN))
                        updateGroupMember(updatedUser,false,action);
                    else if (action.getOldScope().equals(CometChatConstants.SCOPE_ADMIN))
                        updateGroupMember(updatedUser,true,null);
            }
        });
    }

    private void updateGroupMember(User user, boolean isRemove,Action action) {
        if (adapter != null) {
            if (isRemove)
                adapter.removeGroupMember(Utils.UserToGroupMember(user, false, CometChatConstants.SCOPE_PARTICIPANT));
            else
                adapter.addGroupMember(Utils.UserToGroupMember(user, true, action.getNewScope()));
        }
    }
}
