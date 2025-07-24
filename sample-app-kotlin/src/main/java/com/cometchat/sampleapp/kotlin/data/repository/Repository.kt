package com.cometchat.sampleapp.kotlin.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupMembersRequest.GroupMembersRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.utils.AppConstants
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object Repository {
    private val TAG: String = Repository::class.java.simpleName

    /**
     * Logs in a user to CometChat.
     *
     * @param userId
     * The UID of the user to log in.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun loginUser(
        userId: String?, callbackListener: CometChat.CallbackListener<User>
    ) {
        CometChatUIKit.login(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                callbackListener.onSuccess(user)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Registers a user with CometChat.
     *
     * @param user
     * The user object to be registered.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun registerUser(
        user: User, callbackListener: CometChat.CallbackListener<User>
    ) {
        CometChatUIKit.createUser(user, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                callbackListener.onSuccess(user)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Creates a new group.
     *
     * @param group
     * The group object to be created.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun createGroup(
        group: Group, callbackListener: CometChat.CallbackListener<Group>
    ) {
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                CometChatUIKitHelper.onGroupCreated(group)
                callbackListener.onSuccess(group)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Adds members to a group.
     *
     * @param group
     * The group to which members will be added.
     * @param groupMembers
     * List of group members to be added.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun addMembersToGroup(
        group: Group, groupMembers: MutableList<GroupMember>, callbackListener: CometChat.CallbackListener<HashMap<String, String>>
    ) {
        CometChat.addMembersToGroup(group.guid, groupMembers, null, object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(successMap: HashMap<String, String>) {
                var i = 0
                for ((key, value) in successMap) {
                    if (value.startsWith(AppConstants.SuccessConstants.ALREADY_MEMBER)) {
                        groupMembers.removeIf { groupMember: GroupMember -> groupMember.uid == key }
                    }
                    if (AppConstants.SuccessConstants.SUCCESS == value) i++
                }
                group.membersCount += i
                val members: List<User> = ArrayList<User>(groupMembers)
                val actions: MutableList<Action> = ArrayList()
                for (user in members) {
                    val action = Utils.getGroupActionMessage(user, group, group, group.guid)
                    action.action = CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED
                    actions.add(action)
                }
                CometChatUIKitHelper.onGroupMemberAdded(
                    actions, members, group, CometChatUIKit.getLoggedInUser()
                )
                callbackListener.onSuccess(successMap)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Leaves the group.
     *
     * @param group
     * The group to leave.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun leaveGroup(
        group: Group, callbackListener: CometChat.CallbackListener<String>
    ) {
        CometChat.leaveGroup(group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                group.setHasJoined(false)
                group.membersCount -= 1
                val action = Utils.getGroupActionMessage(
                    CometChatUIKit.getLoggedInUser(), group, group, group.guid
                )
                action.action = CometChatConstants.ActionKeys.ACTION_LEFT
                CometChatUIKitHelper.onGroupLeft(action, CometChatUIKit.getLoggedInUser(), group)

                callbackListener.onSuccess(successMessage)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Deletes a group.
     *
     * @param group
     * The group to delete.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun deleteGroup(
        group: Group, callbackListener: CometChat.CallbackListener<String>
    ) {
        CometChat.deleteGroup(group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                CometChatUIKitHelper.onGroupDeleted(group)
                callbackListener.onSuccess(successMessage)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Joins a group.
     *
     * @param guid
     * The group ID.
     * @param type
     * The group type.
     * @param password
     * The group password (if required).
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun joinGroup(
        guid: String, type: String, password: String?, callbackListener: CometChat.CallbackListener<Group>
    ) {
        CometChat.joinGroup(guid, type, password, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                group.setHasJoined(true)
                group.scope = CometChatConstants.SCOPE_PARTICIPANT
                CometChatUIKitHelper.onGroupMemberJoined(CometChatUIKit.getLoggedInUser(), group)
                callbackListener.onSuccess(group)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Unbans a group member.
     *
     * @param group
     * The group from which the member is unbanned.
     * @param groupMember
     * The member to unban.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun unBanGroupMember(
        group: Group, groupMember: GroupMember, callbackListener: CometChat.CallbackListener<Group>
    ) {
        CometChat.unbanGroupMember(groupMember.uid, group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                val action = Utils.getGroupActionMessage(groupMember, group, group, group.guid)
                action.action = CometChatConstants.ActionKeys.ACTION_UNBANNED

                CometChatUIKitHelper.onGroupMemberUnbanned(
                    action, groupMember, CometChatUIKit.getLoggedInUser(), group
                )
                callbackListener.onSuccess(group)
            }

            override fun onError(exception: CometChatException) {
                callbackListener.onError(exception)
            }
        })
    }

    /**
     * Fetches group members.
     *
     * @param group
     * The group whose members are being fetched.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun fetchGroupMembers(
        group: Group, callbackListener: CometChat.CallbackListener<List<GroupMember>>
    ) {
        val groupMembersRequestBuilder = GroupMembersRequestBuilder(group.guid).setLimit(30)
        val groupMembersRequest = groupMembersRequestBuilder.build()
        groupMembersRequest.fetchNext(object : CometChat.CallbackListener<MutableList<GroupMember>>() {
            override fun onSuccess(groupMembers: MutableList<GroupMember>) {
                groupMembers.removeIf { groupMember: GroupMember -> groupMember.uid == CometChatUIKit.getLoggedInUser().uid }
                callbackListener.onSuccess(groupMembers)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Transfers group ownership to another member.
     *
     * @param group
     * The group whose ownership is being transferred.
     * @param groupMember
     * The new owner.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun transferOwnership(
        group: Group, groupMember: GroupMember, callbackListener: CometChat.CallbackListener<String>
    ) {
        CometChat.transferGroupOwnership(group.guid, groupMember.uid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                group.owner = groupMember.uid
                group.scope = UIKitConstants.GroupMemberScope.ADMIN
                CometChatUIKitHelper.onOwnershipChanged(group, groupMember)
                callbackListener.onSuccess(s)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Unblocks a user.
     *
     * @param user
     * The user to unblock.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun unblockUser(
        user: User, callbackListener: CometChat.CallbackListener<HashMap<String, String>>
    ) {
        CometChat.unblockUsers(listOf(user.uid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(resultMap: HashMap<String, String>) {
                if (AppConstants.SuccessConstants.SUCCESS.equals(
                        resultMap[user.uid], ignoreCase = true
                    )
                ) {
                    user.isBlockedByMe = false
                    CometChatUIKitHelper.onUserUnblocked(user)
                }
                callbackListener.onSuccess(resultMap)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    /**
     * Blocks a user.
     *
     * @param user
     * The user to block.
     * @param callbackListener
     * The callback to receive success or error updates.
     */
    fun blockUser(
        user: User, callbackListener: CometChat.CallbackListener<HashMap<String, String>>
    ) {
        CometChat.blockUsers(listOf(user.uid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(resultMap: HashMap<String, String>) {
                if (AppConstants.SuccessConstants.SUCCESS.equals(
                        resultMap[user.uid], ignoreCase = true
                    )
                ) {
                    user.isBlockedByMe = true
                    CometChatUIKitHelper.onUserBlocked(user)
                }
                callbackListener.onSuccess(resultMap)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    fun rejectCall(
        call: Call, callbackListener: CometChat.CallbackListener<Call>
    ) {
        CometChat.rejectCall(call.sessionId, CometChatConstants.CALL_STATUS_REJECTED, object : CometChat.CallbackListener<Call?>() {
            override fun onSuccess(call: Call?) {
                CometChatUIKitHelper.onCallRejected(call)
                callbackListener.onSuccess(call)
            }

            override fun onError(e: CometChatException) {
                CometChatUIKitHelper.onCallRejected(call)
                callbackListener.onError(e)
            }
        })
    }

    fun acceptCall(
        call: Call, callbackListener: CometChat.CallbackListener<Call>
    ) {
        CometChat.acceptCall(call.sessionId, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                CometChatUIKitHelper.onCallAccepted(call)
                callbackListener.onSuccess(call)
            }

            override fun onError(e: CometChatException) {
                CometChatUIKitHelper.onCallRejected(call)
                callbackListener.onError(e)
            }
        })
    }

    fun rejectCallWithBusyStatus(
        call: Call, callbackListener: CometChat.CallbackListener<Call>?
    ) {
        CometChat.rejectCall(call.sessionId, CometChatConstants.CALL_STATUS_BUSY, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call?) {
                CometChatUIKitHelper.onCallRejected(call)
                callbackListener?.onSuccess(call)
            }

            override fun onError(e: CometChatException) {
                CometChatUIKitHelper.onCallRejected(call)
                callbackListener?.onError(e)
            }
        })
    }

    fun deleteChat(
        uid: String, baseMessage: BaseMessage?, conversationType: String, callbackListener: CometChat.CallbackListener<String>
    ) {
        CometChat.deleteConversation(uid, conversationType, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                if (baseMessage != null) CometChatUIKitHelper.onConversationDeleted(CometChatHelper.getConversationFromMessage(baseMessage))
                else CometChatUIKitHelper.onConversationDeleted(Conversation("", CometChatConstants.CONVERSATION_TYPE_USER))
                callbackListener.onSuccess(s)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    fun initiateCall(
        call: Call, callbackListener: CometChat.CallbackListener<Call>
    ) {
        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                callbackListener.onSuccess(call)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    fun fetchMessageInformation(
        id: Long, callbackListener: CometChat.CallbackListener<BaseMessage>
    ) {
        CometChat.getMessageDetails(id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                callbackListener.onSuccess(message)
            }

            override fun onError(e: CometChatException) {
                callbackListener.onError(e)
            }
        })
    }

    fun fetchSampleUsers(listener: CometChat.CallbackListener<List<User>>) {
        val request: Request = Request.Builder().url(AppConstants.JSONConstants.SAMPLE_APP_USERS_URL).method("GET", null).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(
                call: okhttp3.Call, e: IOException
            ) {
                Utils.runOnMainThread {
                    listener.onError(
                        CometChatException("ERROR", e.message)
                    )
                }
            }

            override fun onResponse(
                call: okhttp3.Call, response: Response
            ) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val userList = processSampleUserList(
                            response.body!!.string()
                        )
                        Utils.runOnMainThread {
                            listener.onSuccess(
                                userList
                            )
                        }
                    } catch (e: IOException) {
                        Utils.runOnMainThread {
                            listener.onError(
                                CometChatException("ERROR", e.message)
                            )
                        }
                    }
                } else {
                    Utils.runOnMainThread {
                        listener.onError(
                            CometChatException(
                                "ERROR", response.code.toString()
                            )
                        )
                    }
                }
            }
        })
    }

    private fun processSampleUserList(jsonString: String): List<User> {
        val users: MutableList<User> = ArrayList()
        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray(AppConstants.JSONConstants.KEY_USER)
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = User()
                user.uid = userJson.getString(AppConstants.JSONConstants.UID)
                user.name = userJson.getString(AppConstants.JSONConstants.NAME)
                user.avatar = userJson.getString(AppConstants.JSONConstants.AVATAR)
                users.add(user)
            }
        } catch (e: Exception) {
            CometChatLogger.e(TAG, e.toString())
        }
        return users
    }

    fun getUser(
        uid: String, listener: CometChat.CallbackListener<User>
    ) {
        CometChat.getUser(uid, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                listener.onSuccess(user)
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
            }
        })
    }

    fun logout(listener: CometChat.CallbackListener<String>) {
        CometChat.logout(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                listener.onSuccess(s)
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
            }
        })
    }

    fun getGroup(
        id: String, listener: CometChat.CallbackListener<Group>
    ) {
        CometChat.getGroup(id, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                listener.onSuccess(group)
            }

            override fun onError(e: CometChatException) {
                listener.onError(e)
            }
        })
    }
}
