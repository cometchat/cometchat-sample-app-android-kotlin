package viewmodel

import adapter.UserListAdapter
import android.content.Context
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatUserList
import listeners.StickyHeaderDecoration

class UserListViewModel(context: Context?, cometChatUserList: CometChatUserList, showHeader: Boolean)  {
    private var context: Context? = null
    private var userListAdapter: UserListAdapter? = null
    private var userListView: CometChatUserList? = null

    init {
        userListView = cometChatUserList
        this.context = context
        setUserListAdapter(cometChatUserList, showHeader)
    }

    private val adapter: UserListAdapter
        private get() {
            if (userListAdapter == null) {
                userListAdapter = UserListAdapter(context!!)
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

    private fun setUserListAdapter(cometChatUserList: CometChatUserList, showHeader: Boolean) {
        userListAdapter = UserListAdapter(context!!)
        if (showHeader) {
            val stickyHeaderDecoration = StickyHeaderDecoration(userListAdapter!!)
            cometChatUserList.addItemDecoration(stickyHeaderDecoration, 0)
        }
        cometChatUserList.adapter = userListAdapter
    }

    fun setUsersList(usersList: List<User?>) {
        adapter.updateList(usersList!!)
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