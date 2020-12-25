package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.androiduikit.databinding.FragmentGroupListBinding
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.core.GroupsRequest.GroupsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import constant.StringContract
import listeners.OnItemClickListener
import screen.messagelist.CometChatMessageListActivity

class GroupListViewFragment : Fragment() {
    var groupBinding: FragmentGroupListBinding? = null
    var grouplist = ObservableArrayList<Group>()
    var groupsRequest: GroupsRequest? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        groupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_list, container, false)
        groups
        groupBinding!!.setGroupList(grouplist)
        groupBinding!!.cometchatGroupList.setItemClickListener(object : OnItemClickListener<Group?>() {
            override fun OnItemClick(t: Any, position: Int) {
                val group = t as Group
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(StringContract.IntentStrings.NAME, group.name)
                intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, group.owner)
                intent.putExtra(StringContract.IntentStrings.GUID, group.guid)
                intent.putExtra(StringContract.IntentStrings.AVATAR, group.icon)
                intent.putExtra(StringContract.IntentStrings.GROUP_TYPE, group.groupType)
                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
                intent.putExtra(StringContract.IntentStrings.MEMBER_COUNT, group.membersCount)
                intent.putExtra(StringContract.IntentStrings.GROUP_DESC, group.description)
                intent.putExtra(StringContract.IntentStrings.GROUP_PASSWORD, group.password)
                startActivity(intent)
            }
        })
        return groupBinding!!.getRoot()
    }

    private val groups: Unit
        private get() {
            if (groupsRequest == null) {
                groupsRequest = GroupsRequestBuilder().setLimit(30).build()
            }
            groupsRequest!!.fetchNext(object : CallbackListener<List<Group?>?>() {
                override fun onSuccess(groups: List<Group?>?) {
                    groupBinding!!.contactShimmer.stopShimmer()
                    groupBinding!!.contactShimmer.visibility = View.GONE
                    grouplist.addAll(groups!!)
                }

                override fun onError(e: CometChatException) {
                    groupBinding!!.contactShimmer.stopShimmer()
                    groupBinding!!.contactShimmer.visibility = View.GONE
                    Log.e("onError: ", e.message)
                }
            })
        }
}