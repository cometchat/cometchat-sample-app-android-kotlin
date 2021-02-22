package com.cometchat.pro.uikit.ui_components.shared.cometchatGroups

import android.content.Context
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import java.util.*

class CometChatGroupsViewModel(context: Context?, cometChatGroups: CometChatGroups) {
    private var context: Context? = null
    private val groupsRequest: GroupsRequest? = null
    private var cometChatGroupsAdapter: CometChatGroupsAdapter? = null
    private val groupList: List<User> = ArrayList()
    private val groupHashMap = HashMap<String, Group>()
    private var groupsView: CometChatGroups? = null

    init {
        groupsView = cometChatGroups
        this.context = context
        setGroupListAdapter(cometChatGroups)
    }

    private val adapter: CometChatGroupsAdapter
        private get() {
            if (cometChatGroupsAdapter == null) {
                cometChatGroupsAdapter = CometChatGroupsAdapter(context!!)
            }
            return cometChatGroupsAdapter!!
        }

    private fun setGroupListAdapter(cometChatGroups: CometChatGroups) {
        cometChatGroupsAdapter = CometChatGroupsAdapter(context!!)
        cometChatGroups.adapter = cometChatGroupsAdapter
    }

    fun setGroupList(groupList: List<Group>?) {
        if (cometChatGroupsAdapter != null) {
            if (groupList != null && groupList.size != 0) cometChatGroupsAdapter!!.updateGroupList(groupList!!)
        }
    }

    fun remove(group: Group?) {
        if (cometChatGroupsAdapter != null) cometChatGroupsAdapter!!.removeGroup(group)
    }

    fun update(group: Group?) {
        if (cometChatGroupsAdapter != null) cometChatGroupsAdapter!!.updateGroup(group)
    }

    fun add(group: Group?) {
        if (cometChatGroupsAdapter != null) cometChatGroupsAdapter!!.add(group)
    }

    fun searchGroupList(groups: List<Group?>?) {
        if (cometChatGroupsAdapter != null) cometChatGroupsAdapter!!.searchGroup(groups!!)
    }

    fun clear() {
        if (cometChatGroupsAdapter != null) cometChatGroupsAdapter!!.clear()
    }

    companion object {
        private const val TAG = "GroupListViewModel"
    }
}