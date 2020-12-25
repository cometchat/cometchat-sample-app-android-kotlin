package viewmodel

import adapter.GroupListAdapter
import android.content.Context
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatGroupList
import java.util.*

class GroupListViewModel(context: Context?, cometChatGroupList: CometChatGroupList) {
    private var context: Context? = null
    private val groupsRequest: GroupsRequest? = null
    private var groupListAdapter: GroupListAdapter? = null
    private val groupList: List<User> = ArrayList()
    private val groupHashMap = HashMap<String, Group>()
    private var groupListView: CometChatGroupList? = null

    init {
        groupListView = cometChatGroupList
        this.context = context
        setGroupListAdapter(cometChatGroupList)
    }

    private val adapter: GroupListAdapter
        private get() {
            if (groupListAdapter == null) {
                groupListAdapter = GroupListAdapter(context!!)
            }
            return groupListAdapter!!
        }

    private fun setGroupListAdapter(cometChatGroupList: CometChatGroupList) {
        groupListAdapter = GroupListAdapter(context!!)
        cometChatGroupList.adapter = groupListAdapter
    }

    fun setGroupList(groupList: List<Group>?) {
        if (groupListAdapter != null) {
            if (groupList != null && groupList.size != 0) groupListAdapter!!.updateGroupList(groupList!!)
        }
    }

    fun remove(group: Group?) {
        if (groupListAdapter != null) groupListAdapter!!.removeGroup(group)
    }

    fun update(group: Group?) {
        if (groupListAdapter != null) groupListAdapter!!.updateGroup(group)
    }

    fun add(group: Group?) {
        if (groupListAdapter != null) groupListAdapter!!.add(group)
    }

    fun searchGroupList(groups: List<Group?>?) {
        if (groupListAdapter != null) groupListAdapter!!.searchGroup(groups!!)
    }

    fun clear() {
        if (groupListAdapter != null) groupListAdapter!!.clear()
    }

    companion object {
        private const val TAG = "GroupListViewModel"
    }
}