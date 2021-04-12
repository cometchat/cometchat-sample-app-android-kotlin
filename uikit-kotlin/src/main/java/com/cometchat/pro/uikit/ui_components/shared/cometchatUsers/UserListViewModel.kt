package com.cometchat.pro.uikit.ui_components.shared.cometchatUsers

import android.content.Context
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderDecoration

class UserListViewModel(context: Context?, cometChatUserList: CometChatUsers, showHeader: Boolean)  {
    private var context: Context? = null
    private var userListAdapter: CometChatUsersAdapter? = null
    private var userListView: CometChatUsers? = null

    init {
        userListView = cometChatUserList
        this.context = context
        setUserListAdapter(cometChatUserList, showHeader)
    }

    private val adapter: CometChatUsersAdapter
        private get() {
            if (userListAdapter == null) {
                userListAdapter = CometChatUsersAdapter(context!!)
            }
            return userListAdapter!!
        }

    fun add(user: User?) {
        if (userListAdapter != null) userListAdapter!!.add(user!!)
    }

    fun add(index: Int, user: User?) {
        if (userListAdapter != null) userListAdapter!!.add(index, user!!)
    }

    fun update(user: User?) {
        if (userListAdapter != null) userListAdapter!!.updateUser(user!!)
    }

    fun remove(user: User?) {
        if (userListAdapter != null) userListAdapter!!.removeUser(user)
    }

    fun remove(index: Int) {
        if (userListAdapter != null) userListAdapter!!.removeUser(index)
    }

    fun clear() {
        if (userListAdapter != null) userListAdapter!!.clear()
    }

    private fun setUserListAdapter(cometChatUserList: CometChatUsers, showHeader: Boolean) {
        userListAdapter = CometChatUsersAdapter(context!!)
        if (showHeader) {
            val stickyHeaderDecoration = StickyHeaderDecoration(userListAdapter!!)
            cometChatUserList.addItemDecoration(stickyHeaderDecoration, 0)
        }
        cometChatUserList.adapter = userListAdapter
    }

    fun setUsersList(usersList: List<User?>) {
        adapter.updateList(usersList)
    }

    fun update(index: Int, user: User?) {
        if (userListAdapter != null) userListAdapter!!.updateUser(index, user!!)
    }

    fun searchUserList(userList: List<User?>?) {
        if (userListAdapter != null) userListAdapter!!.searchUser(userList!!)
    }

    companion object {
        private const val TAG = "UserListViewModel"
    }
}