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
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group

import constant.StringContract
import listeners.OnItemClickListener
import screen.messagelist.CometChatMessageListActivity

class GroupListViewFragment : Fragment() {

    internal var groupBinding: FragmentGroupListBinding? = null
    internal var grouplist = ObservableArrayList<Group>()
    internal var groupsRequest: GroupsRequest? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        groupBinding = DataBindingUtil.inflate<FragmentGroupListBinding>(inflater, R.layout.fragment_group_list, container, false)
        getGroups()
        groupBinding?.setGroupList(grouplist)
        groupBinding?.cometchatGroupList?.setItemClickListener(object : OnItemClickListener<Group>() {
            override fun OnItemClick(group: Group, position: Int) {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(StringContract.IntentStrings.NAME, group.name)
                intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, group.owner)
                intent.putExtra(StringContract.IntentStrings.GUID, group.guid)
                intent.putExtra(StringContract.IntentStrings.AVATAR, group.icon)
                intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
                startActivity(intent)
            }

            override fun OnItemLongClick(`var`: Group, position: Int) {
                super.OnItemLongClick(`var`, position)
            }
        })
        return groupBinding?.getRoot()
    }

    private fun getGroups() {
        if (groupsRequest == null) {
            groupsRequest = GroupsRequest.GroupsRequestBuilder().setLimit(30).build()
        }
        groupsRequest!!.fetchNext(object : CometChat.CallbackListener<List<Group>>() {
            override fun onSuccess(groups: List<Group>) {
                groupBinding?.contactShimmer?.stopShimmer()
                groupBinding?.contactShimmer?.setVisibility(View.GONE)
                grouplist.addAll(groups)
            }

            override fun onError(e: CometChatException) {
                groupBinding?.contactShimmer?.stopShimmer()
                groupBinding?.contactShimmer?.setVisibility(View.GONE)
                Log.e("onError: ", e.message.toString())
            }
        })
    }
}
