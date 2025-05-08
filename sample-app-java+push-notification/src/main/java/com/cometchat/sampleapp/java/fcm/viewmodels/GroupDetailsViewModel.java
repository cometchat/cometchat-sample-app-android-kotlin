package com.cometchat.sampleapp.java.fcm.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ViewModel for managing the state and data of a specific group in the chat
 * application.
 */
public class GroupDetailsViewModel extends ViewModel {
    private final String GROUP_LISTENER_ID;

    private final MutableLiveData<UIKitConstants.DialogState> dialogState;
    private final MutableLiveData<UIKitConstants.DialogState> confirmDialogState;
    @NonNull
    private final MutableLiveData<UIKitConstants.DialogState> transferOwnershipDialogState;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Group> updatedGroup;
    private Group group;

    /**
     * Initializes the GroupDetailsViewModel with LiveData objects.
     */
    public GroupDetailsViewModel() {
        dialogState = new MutableLiveData<>();
        confirmDialogState = new MutableLiveData<>();
        transferOwnershipDialogState = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        updatedGroup = new MutableLiveData<>();
        GROUP_LISTENER_ID = System.currentTimeMillis() + "_" + this.getClass().getSimpleName();
    }

    /**
     * Gets the LiveData for the dialog state related to group operations.
     *
     * @return MutableLiveData object containing the dialog state.
     */
    public MutableLiveData<UIKitConstants.DialogState> getDialogState() {
        return dialogState;
    }

    /**
     * Gets the LiveData for error messages that occur during group operations.
     *
     * @return MutableLiveData object containing error messages.
     */
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the LiveData for the updated group details.
     *
     * @return MutableLiveData object containing the updated Group.
     */
    public MutableLiveData<Group> getUpdatedGroup() {
        return updatedGroup;
    }

    /**
     * Gets the LiveData for the confirmation dialog state.
     *
     * @return MutableLiveData object containing the confirmation dialog state.
     */
    public MutableLiveData<UIKitConstants.DialogState> getConfirmDialogState() {
        return confirmDialogState;
    }

    /**
     * Gets the LiveData for the transfer ownership dialog state.
     *
     * @return MutableLiveData object containing the transfer ownership dialog
     * state.
     */
    @NonNull
    public MutableLiveData<UIKitConstants.DialogState> getTransferOwnershipDialogState() {
        return transferOwnershipDialogState;
    }

    /**
     * Sets the group for which details are being managed.
     *
     * @param group The Group to be set.
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Adds listeners for group-related events.
     */
    public void addListeners() {
        CometChat.addGroupListener(GROUP_LISTENER_ID, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User user, Group group_) {
                super.onGroupMemberJoined(action, user, group);
                if (group_.getGuid().equals(group.getGuid())) {
                    group.setMembersCount(group_.getMembersCount());
                    group.setUpdatedAt(group_.getUpdatedAt());
                    updatedGroup.setValue(group);
                }
            }

            @Override
            public void onGroupMemberLeft(Action action, User user, @NonNull Group group_) {
                if (group_.getGuid().equals(group.getGuid())) {
                    group.setMembersCount(group_.getMembersCount());
                    group.setUpdatedAt(group_.getUpdatedAt());
                    updatedGroup.setValue(group);
                }
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group group_) {
                super.onGroupMemberKicked(action, kickedUser, kickedBy, group_);
                if (group_.getGuid().equals(group.getGuid())) {
                    group.setMembersCount(group_.getMembersCount());
                    group.setUpdatedAt(group_.getUpdatedAt());

                    if (kickedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                        group.setHasJoined(false);
                    }
                    updatedGroup.setValue(group);
                }
            }

            @Override
            public void onGroupMemberBanned(Action action, User bannedUser, User bannedBy, Group group_) {
                super.onGroupMemberBanned(action, bannedUser, bannedBy, group_);
                if (group_.getGuid().equals(group.getGuid())) {
                    group.setMembersCount(group_.getMembersCount());
                    group.setUpdatedAt(group_.getUpdatedAt());
                    if (bannedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid()))
                        group.setHasJoined(false);
                    updatedGroup.setValue(group);
                }
            }

            @Override
            public void onGroupMemberScopeChanged(
                Action action, User updatedBy, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group_
            ) {
                super.onGroupMemberScopeChanged(action, updatedBy, updatedUser, scopeChangedTo, scopeChangedFrom, group_);
                if (group_.getGuid().equals(group.getGuid())) {
                    if (updatedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                        group.setScope(scopeChangedTo);
                        updatedGroup.setValue(group);
                    }
                }
            }

            @Override
            public void onMemberAddedToGroup(Action action, User user, User user1, Group group_) {
                if (group_.getGuid().equals(group.getGuid())) {
                    group.setMembersCount(group_.getMembersCount());
                    group.setUpdatedAt(group_.getUpdatedAt());
                    updatedGroup.setValue(group);
                }
            }
        });

        CometChatGroupEvents.addGroupListener(GROUP_LISTENER_ID, new CometChatGroupEvents() {
            @Override
            public void ccGroupMemberAdded(
                List<Action> actionMessages, List<User> usersAdded, Group userAddedIn, User addedBy
            ) {
                updatedGroup.setValue(userAddedIn);
            }

            @Override
            public void ccGroupMemberKicked(Action actionMessage, User kickedUser, User kickedBy, @Nullable Group kickedFrom) {
                if (kickedFrom != null) updatedGroup.setValue(kickedFrom);
            }

            @Override
            public void ccGroupMemberBanned(Action actionMessage, User bannedUser, User bannedBy, Group bannedFrom) {
                updatedGroup.setValue(bannedFrom);
            }

            @Override
            public void ccOwnershipChanged(Group mGroup, GroupMember newOwner) {
                if (mGroup != null) updatedGroup.setValue(mGroup);
            }
        });
    }

    /**
     * Removes the added group listeners.
     */
    public void removeListeners() {
        CometChat.removeGroupListener(GROUP_LISTENER_ID);
        CometChatGroupEvents.removeListener(GROUP_LISTENER_ID);
    }

    /**
     * Adds members to the current group.
     *
     * @param users The list of Users to be added to the group.
     */
    public void addMembersToGroup(Context context, List<User> users) {
        dialogState.setValue(UIKitConstants.DialogState.INITIATED);
        List<GroupMember> groupMembers = new ArrayList<>();

        for (User user : users) {
            groupMembers.add(userToGroupMember(user, false, ""));
        }

        Repository.addMembersToGroup(group, groupMembers, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> successMap) {
                dialogState.setValue(UIKitConstants.DialogState.SUCCESS);
            }

            @Override
            public void onError(CometChatException e) {
                dialogState.setValue(UIKitConstants.DialogState.FAILURE);
                handleError(context, e.getMessage());
            }
        });
    }

    /**
     * Converts a User object to a GroupMember object.
     *
     * @param user          The User to be converted.
     * @param isScopeUpdate Indicates if this is a scope update.
     * @param newScope      The new scope if it is a scope update.
     * @return The converted GroupMember.
     */
    public GroupMember userToGroupMember(User user, boolean isScopeUpdate, String newScope) {
        GroupMember groupMember;
        if (isScopeUpdate) groupMember = new GroupMember(user.getUid(), newScope);
        else groupMember = new GroupMember(user.getUid(), CometChatConstants.SCOPE_PARTICIPANT);
        groupMember.setAvatar(user.getAvatar());
        groupMember.setName(user.getName());
        groupMember.setStatus(user.getStatus());
        return groupMember;
    }

    private void handleError(Context context, String error) {
        Matcher matcher = Pattern.compile("UID (\\w+) .*?GUID (\\w+)").matcher(error);

        if (!matcher.find()) {
            postGenericError(context);
            return;
        }

        String uid = matcher.group(1);
        String guid = matcher.group(2);

        if (uid == null || guid == null) {
            postGenericError(context);
            return;
        }

        Repository.getUser(uid, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                fetchGroupAndPostErrorMessage(context, user, guid);
            }

            @Override
            public void onError(CometChatException e) {
                postGenericError(context);
            }
        });
    }

    private void postGenericError(Context context) {
        errorMessage.setValue(context.getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong));
    }

    private void fetchGroupAndPostErrorMessage(Context context, User user, String guid) {
        Repository.getGroup(guid, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                postCompleteError(context, group.getName(), user.getName());
            }

            @Override
            public void onError(CometChatException e) {
                postGenericError(context);
            }
        });
    }

    private void postCompleteError(Context context, String groupName, String userName) {
        String message = groupName != null
            ? context.getString(R.string.participant_scope_with_group, userName, groupName)
            : context.getString(R.string.participant_scope_without_group, userName);

        errorMessage.setValue(message);
    }

    /**
     * Allows a user to leave the specified group.
     *
     * @param group The Group to leave.
     */
    public void leaveGroup(@NonNull Group group) {
        confirmDialogState.setValue(UIKitConstants.DialogState.INITIATED);
        Repository.leaveGroup(group, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                confirmDialogState.setValue(UIKitConstants.DialogState.SUCCESS);
            }

            @Override
            public void onError(CometChatException e) {
                confirmDialogState.setValue(UIKitConstants.DialogState.FAILURE);
            }
        });
    }

    /**
     * Deletes the specified group.
     *
     * @param group The Group to delete.
     */
    public void deleteGroup(Group group) {
        confirmDialogState.setValue(UIKitConstants.DialogState.INITIATED);
        Repository.deleteGroup(group, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                confirmDialogState.setValue(UIKitConstants.DialogState.SUCCESS);
            }

            @Override
            public void onError(CometChatException e) {
                confirmDialogState.setValue(UIKitConstants.DialogState.FAILURE);
            }
        });
    }

    /**
     * Fetches the current group members and initiates the ownership transfer
     * process.
     */
    public void fetchAndTransferOwnerShip() {
        transferOwnershipDialogState.setValue(UIKitConstants.DialogState.INITIATED);
        Repository.fetchGroupMembers(group, new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                if (!groupMembers.isEmpty()) {
                    transferOwnership(groupMembers.get(0));
                } else {
                    transferOwnershipDialogState.setValue(UIKitConstants.DialogState.FAILURE);
                }
            }

            @Override
            public void onError(CometChatException e) {
                transferOwnershipDialogState.setValue(UIKitConstants.DialogState.FAILURE);
            }
        });
    }

    /**
     * Transfers ownership of the group to a new owner.
     *
     * @param newOwner The new GroupMember who will become the owner.
     */
    public void transferOwnership(GroupMember newOwner) {
        transferOwnershipDialogState.setValue(UIKitConstants.DialogState.INITIATED);
        Repository.transferOwnership(group, newOwner, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                transferOwnershipDialogState.setValue(UIKitConstants.DialogState.SUCCESS);
            }

            @Override
            public void onError(CometChatException e) {
                transferOwnershipDialogState.setValue(UIKitConstants.DialogState.FAILURE);
            }
        });
    }
}
