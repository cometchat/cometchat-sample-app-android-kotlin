package com.cometchat.sampleapp.java.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.GroupMembersRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.helpers.CometChatHelper;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Repository {
    private static final String TAG = Repository.class.getSimpleName();

    /**
     * Logs in a user to CometChat.
     *
     * @param userId           The UID of the user to log in.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void loginUser(String userId, CometChat.CallbackListener<User> callbackListener) {
        CometChatUIKit.login(userId, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (callbackListener != null) {
                    callbackListener.onSuccess(user);
                }
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) {
                    callbackListener.onError(e);
                }
            }
        });
    }

    /**
     * Registers a user with CometChat.
     *
     * @param user             The user object to be registered.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void registerUser(User user, CometChat.CallbackListener<User> callbackListener) {
        CometChatUIKit.createUser(user, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (callbackListener != null) {
                    callbackListener.onSuccess(user);
                }
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) {
                    callbackListener.onError(e);
                }
            }
        });
    }

    /**
     * Creates a new group.
     *
     * @param group            The group object to be created.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void createGroup(Group group, CometChat.CallbackListener<Group> callbackListener) {
        CometChat.createGroup(group, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                CometChatUIKitHelper.onGroupCreated(group);
                if (callbackListener != null) callbackListener.onSuccess(group);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Adds members to a group.
     *
     * @param group            The group to which members will be added.
     * @param groupMembers     List of group members to be added.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void addMembersToGroup(Group group,
                                         List<GroupMember> groupMembers,
                                         CometChat.CallbackListener<HashMap<String, String>> callbackListener) {
        CometChat.addMembersToGroup(group.getGuid(), groupMembers, null, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> successMap) {
                int i = 0;
                for (Map.Entry<String, String> entry : successMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().startsWith(AppConstants.SuccessConstants.ALREADY_MEMBER)) {
                        groupMembers.removeIf(groupMember -> groupMember.getUid().equals(entry.getKey()));
                    }
                    if (AppConstants.SuccessConstants.SUCCESS.equals(entry.getValue())) i++;
                }
                group.setMembersCount(group.getMembersCount() + i);
                List<User> members = new ArrayList<>(groupMembers);
                List<Action> actions = new ArrayList<>();
                for (User user : members) {
                    Action action = Utils.getGroupActionMessage(user, group, group, group.getGuid());
                    action.setAction(CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED);
                    actions.add(action);
                }
                CometChatUIKitHelper.onGroupMemberAdded(actions, members, group, CometChatUIKit.getLoggedInUser());
                if (callbackListener != null) callbackListener.onSuccess(successMap);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Leaves the group.
     *
     * @param group            The group to leave.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void leaveGroup(Group group, CometChat.CallbackListener<String> callbackListener) {
        CometChat.leaveGroup(group.getGuid(), new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                group.setHasJoined(false);
                group.setMembersCount(group.getMembersCount() - 1);
                Action action = Utils.getGroupActionMessage(CometChatUIKit.getLoggedInUser(), group, group, group.getGuid());
                action.setAction(CometChatConstants.ActionKeys.ACTION_LEFT);
                CometChatUIKitHelper.onGroupLeft(action, CometChatUIKit.getLoggedInUser(), group);

                if (callbackListener != null) callbackListener.onSuccess(successMessage);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Deletes a group.
     *
     * @param group            The group to delete.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void deleteGroup(Group group, CometChat.CallbackListener<String> callbackListener) {
        CometChat.deleteGroup(group.getGuid(), new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                CometChatUIKitHelper.onGroupDeleted(group);
                if (callbackListener != null) callbackListener.onSuccess(successMessage);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Joins a group.
     *
     * @param guid             The group ID.
     * @param type             The group type.
     * @param password         The group password (if required).
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void joinGroup(String guid, String type, String password, CometChat.CallbackListener<Group> callbackListener) {
        CometChat.joinGroup(guid, type, password, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                group.setHasJoined(true);
                group.setScope(CometChatConstants.SCOPE_PARTICIPANT);
                CometChatUIKitHelper.onGroupMemberJoined(CometChatUIKit.getLoggedInUser(), group);
                if (callbackListener != null) callbackListener.onSuccess(group);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Unbans a group member.
     *
     * @param group            The group from which the member is unbanned.
     * @param groupMember      The member to unban.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void unBanGroupMember(Group group, GroupMember groupMember, @Nullable CometChat.CallbackListener<Group> callbackListener) {
        CometChat.unbanGroupMember(groupMember.getUid(), group.getGuid(), new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                Action action = Utils.getGroupActionMessage(groupMember, group, group, group.getGuid());
                action.setAction(CometChatConstants.ActionKeys.ACTION_UNBANNED);

                CometChatUIKitHelper.onGroupMemberUnbanned(action, groupMember, CometChatUIKit.getLoggedInUser(), group);
                if (callbackListener != null) callbackListener.onSuccess(group);
            }

            @Override
            public void onError(CometChatException exception) {
                if (callbackListener != null) callbackListener.onError(exception);
            }
        });
    }

    /**
     * Fetches group members.
     *
     * @param group            The group whose members are being fetched.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void fetchGroupMembers(Group group, CometChat.CallbackListener<List<GroupMember>> callbackListener) {
        GroupMembersRequest.GroupMembersRequestBuilder groupMembersRequestBuilder = new GroupMembersRequest.GroupMembersRequestBuilder(group.getGuid()).setLimit(
            30);
        GroupMembersRequest groupMembersRequest = groupMembersRequestBuilder.build();
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                groupMembers.removeIf(groupMember -> groupMember.getUid().equals(CometChatUIKit.getLoggedInUser().getUid()));
                if (callbackListener != null) callbackListener.onSuccess(groupMembers);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Transfers group ownership to another member.
     *
     * @param group            The group whose ownership is being transferred.
     * @param groupMember      The new owner.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void transferOwnership(Group group, GroupMember groupMember, CometChat.CallbackListener<String> callbackListener) {
        if (groupMember == null) return;
        CometChat.transferGroupOwnership(group.getGuid(), groupMember.getUid(), new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                group.setOwner(groupMember.getUid());
                group.setScope(UIKitConstants.GroupMemberScope.ADMIN);
                CometChatUIKitHelper.onOwnershipChanged(group, groupMember);
                if (callbackListener != null) callbackListener.onSuccess(s);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Unblocks a user.
     *
     * @param user             The user to unblock.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void unblockUser(User user, CometChat.CallbackListener<HashMap<String, String>> callbackListener) {
        CometChat.unblockUsers(Collections.singletonList(user.getUid()), new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> resultMap) {
                if (resultMap != null && AppConstants.SuccessConstants.SUCCESS.equalsIgnoreCase(resultMap.get(user.getUid()))) {
                    user.setBlockedByMe(false);
                    CometChatUIKitHelper.onUserUnblocked(user);
                }
                if (callbackListener != null) callbackListener.onSuccess(resultMap);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Blocks a user.
     *
     * @param user             The user to block.
     * @param callbackListener The callback to receive success or error updates.
     */
    public static void blockUser(@NonNull User user, CometChat.CallbackListener<HashMap<String, String>> callbackListener) {
        CometChat.blockUsers(Collections.singletonList(user.getUid()), new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> resultMap) {
                if (resultMap != null && AppConstants.SuccessConstants.SUCCESS.equalsIgnoreCase(resultMap.get(user.getUid()))) {
                    user.setBlockedByMe(true);
                    CometChatUIKitHelper.onUserBlocked(user);
                }
                if (callbackListener != null) callbackListener.onSuccess(resultMap);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    public static void rejectCall(Call call, CometChat.CallbackListener<Call> callbackListener) {
        CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_REJECTED, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                CometChatUIKitHelper.onCallRejected(call);
                if (callbackListener != null) callbackListener.onSuccess(call);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatUIKitHelper.onCallRejected(call);
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    public static void acceptCall(Call call, CometChat.CallbackListener<Call> callbackListener) {
        CometChat.acceptCall(call.getSessionId(), new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                CometChatUIKitHelper.onCallAccepted(call);
                if (callbackListener != null) callbackListener.onSuccess(call);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatUIKitHelper.onCallRejected(call);
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    public static void rejectCallWithBusyStatus(Call call, CometChat.CallbackListener<Call> callbackListener) {
        CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_BUSY, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                CometChatUIKitHelper.onCallRejected(call);
                if (callbackListener != null) callbackListener.onSuccess(call);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatUIKitHelper.onCallRejected(call);
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    public static void deleteChat(String uid, BaseMessage baseMessage, String conversationType, CometChat.CallbackListener<String> callbackListener) {
        CometChat.deleteConversation(uid, conversationType, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (baseMessage != null)
                    CometChatUIKitHelper.onConversationDeleted(CometChatHelper.getConversationFromMessage(baseMessage));
                else
                    CometChatUIKitHelper.onConversationDeleted(new Conversation("", CometChatConstants.CONVERSATION_TYPE_USER));
                callbackListener.onSuccess(s);
            }

            @Override
            public void onError(CometChatException e) {
                callbackListener.onError(e);
            }
        });
    }

    public static void initiateCall(Call call, CometChat.CallbackListener<Call> callbackListener) {
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                callbackListener.onSuccess(call);
            }

            @Override
            public void onError(CometChatException e) {
                callbackListener.onError(e);
            }
        });
    }

    public static void fetchMessageInformation(long id, CometChat.CallbackListener<BaseMessage> callbackListener) {
        CometChat.getMessageDetails(id, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage message) {
                if (callbackListener != null) callbackListener.onSuccess(message);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    public static void fetchSampleUsers(@NonNull CometChat.CallbackListener<List<User>> listener) {
        Request request = new Request.Builder().url(AppConstants.JSONConstants.SAMPLE_APP_USERS_URL).method("GET", null).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Utils.runOnMainThread(() -> listener.onError(new CometChatException("ERROR", e.getMessage())));
            }

            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<User> userList = processSampleUserList(response.body().string());
                        Utils.runOnMainThread(() -> listener.onSuccess(userList));
                    } catch (IOException e) {
                        Utils.runOnMainThread(() -> listener.onError(new CometChatException("ERROR", e.getMessage())));
                    }
                } else {
                    Utils.runOnMainThread(() -> listener.onError(new CometChatException("ERROR", String.valueOf(response.code()))));
                }
            }
        });
    }

    private static List<User> processSampleUserList(String jsonString) {
        List<User> users = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(AppConstants.JSONConstants.KEY_USER);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = new User();
                user.setUid(userJson.getString(AppConstants.JSONConstants.UID));
                user.setName(userJson.getString(AppConstants.JSONConstants.NAME));
                user.setAvatar(userJson.getString(AppConstants.JSONConstants.AVATAR));
                users.add(user);
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return users;
    }

    public static void getUser(String uid, CometChat.CallbackListener<User> listener) {
        CometChat.getUser(uid, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                listener.onSuccess(user);
            }

            @Override
            public void onError(CometChatException e) {
                listener.onError(e);
            }
        });
    }

    public static void logout(CometChat.CallbackListener<String> listener) {
        CometChat.logout(new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                listener.onSuccess(s);
            }

            @Override
            public void onError(CometChatException e) {
                listener.onError(e);
            }
        });
    }

    public static void getGroup(String id, CometChat.CallbackListener<Group> listener) {
        CometChat.getGroup(id, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                listener.onSuccess(group);
            }

            @Override
            public void onError(CometChatException e) {
                listener.onError(e);
            }
        });
    }
}
