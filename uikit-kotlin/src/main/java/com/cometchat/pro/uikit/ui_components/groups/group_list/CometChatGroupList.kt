package com.cometchat.pro.uikit.ui_components.groups.group_list

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.core.GroupsRequest.GroupsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.groups.create_group.CometChatCreateGroupActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatGroups.CometChatGroups
import com.cometchat.pro.uikit.ui_components.shared.cometchatGroups.CometChatGroupsAdapter
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.cometchat.pro.uikit.ui_settings.enum.GroupMode
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.*

/*

* Purpose - CometChatGroupList class is a fragment used to display list of groups and perform certain action on click of item.
            It also provide search bar to search group from the list.

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/
class CometChatGroupList constructor() : Fragment() {
    private var isCreateGroupVisible: Boolean = true
    private var isTitleVisible: Boolean = true
    private var rvGroups //Uses to display list of groups.
            : CometChatGroups? = null
    private var cometChatGroupsAdapter: CometChatGroupsAdapter? = null
    private var groupsRequest //Uses to fetch Groups.
            : GroupsRequest? = null
    private var etSearch //Uses to perform search operations on groups.
            : EditText? = null
    private var clearSearch: ImageView? = null
    private var ivCreateGroup: ImageView? = null
    private var noGroupLayout: LinearLayout? = null
    private val groupList: MutableList<Group> = ArrayList()
    private var conversationShimmer: ShimmerFrameLayout? = null
    private var tvTitle: TextView? = null
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_cometchat_group_list, container, false)

        rvGroups = view.findViewById(R.id.rv_group_list)
        noGroupLayout = view.findViewById(R.id.no_group_layout)
        etSearch = view.findViewById(R.id.search_bar)
        clearSearch = view.findViewById(R.id.clear_search)
        conversationShimmer = view.findViewById(R.id.shimmer_layout)
        tvTitle = view.findViewById(R.id.tv_title)
        tvTitle?.typeface = FontUtils.getInstance(activity).getTypeFace(FontUtils.robotoMedium)

        val fragment = fragmentManager?.findFragmentByTag("startChat")
        if (fragment != null && fragment.isVisible) {
            Log.e(TAG, "onCreateView: group "+ fragment.toString())
            tvTitle?.visibility = View.GONE
        }

        FeatureRestriction.isGroupSearchEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0) {
                    etSearch?.visibility = View.GONE
                    clearSearch?.visibility = View.GONE
                }
            }
        })
        ivCreateGroup = view.findViewById(R.id.create_group)
        ivCreateGroup?.imageTintList = ColorStateList.valueOf(Color.parseColor(UIKitSettings.color))
        if (FeatureRestriction.isGroupCreationEnabled()) ivCreateGroup?.visibility = View.VISIBLE else ivCreateGroup?.visibility = View.GONE
        if (Utils.isDarkMode(context!!)) {
            tvTitle?.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            tvTitle?.setTextColor(resources.getColor(R.color.primaryTextColor))
        }
        ivCreateGroup?.setOnClickListener(View.OnClickListener { view1: View? ->
            val intent: Intent = Intent(context, CometChatCreateGroupActivity::class.java)
            startActivity(intent)
        })
        etSearch?.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    // if etSearch is empty then fetch all groups.
                    groupsRequest = null
                    rvGroups?.clear()
                    fetchGroup()
                } else {
                    // Search group based on text in etSearch field.
                    searchGroup(editable.toString())
                }
            }
        })
        etSearch?.setOnEditorActionListener(object : OnEditorActionListener {
            public override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent?): Boolean {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchGroup(textView.text.toString())
                    clearSearch?.visibility = View.VISIBLE
                    return true
                }
                return false
            }
        })
        clearSearch?.setOnClickListener {
            etSearch?.setText("")
            clearSearch?.visibility = View.GONE
            searchGroup(etSearch?.text.toString())
            val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch?.windowToken, 0)
        }

        //Uses to fetch next list of group if rvGroupList (RecyclerView) is scrolled in upward direction.
        rvGroups?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            public override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchGroup()
                }
            }
        })

        // Used to trigger event on click of group item in rvGroupList (RecyclerView)
        rvGroups?.setItemClickListener(object : OnItemClickListener<Group?>() {
            public override fun OnItemClick(t: Any, position: Int) {
                event?.OnItemClick(t as Group, position)
            }
        })
        return view
    }

    var itemTouchHelper = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            var position = viewHolder.adapterPosition
            when (direction) {
                ItemTouchHelper.LEFT -> {
//                    onChildDraw()
                }
            }
        }

    }
    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isTitleVisible)
            tvTitle?.visibility = View.VISIBLE
        else tvTitle?.visibility = View.GONE

        if (isCreateGroupVisible)
            ivCreateGroup?.visibility = View.VISIBLE
        else ivCreateGroup?.visibility = View.GONE
    }

    /**
     * This method is used to retrieve list of groups present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-groups-retrieve-groups" ">&quot;https://prodocs.cometchat.com/docs/android-groups-retrieve-groups&quot;
     *
     * @see GroupsRequest
     */
    private fun fetchGroup() {
        if (groupsRequest == null) {
            groupsRequest = GroupsRequestBuilder().setLimit(30).build()
        }
        groupsRequest?.fetchNext(object : CallbackListener<List<Group>?>() {
            public override fun onSuccess(groups: List<Group>?) {
                stopHideShimmer()
                val filteredList: MutableList<Group> = filterGroup(groups as MutableList<Group>)
                rvGroups?.setGroupList(groups) // sets the groups in rvGroupList i.e CometChatGroupList Component.
                groupList.addAll((groups))
                if (groupList.size == 0) {
                    noGroupLayout?.visibility = View.VISIBLE
                    rvGroups?.visibility = View.GONE
                } else {
                    noGroupLayout?.visibility = View.GONE
                    rvGroups?.visibility = View.VISIBLE
                }
            }

            public override fun onError(e: CometChatException) {
                stopHideShimmer()
                if (rvGroups != null)
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    private fun filterGroup(groups: MutableList<Group>): MutableList<Group> {
        val resultList: MutableList<Group> = ArrayList()
        for (group in groups) {
            if (group.isJoined) {
                resultList.add(group)
            }
            if (UIKitSettings.groupInMode == GroupMode.PUBLIC_GROUP &&
                    group.groupType.equals(CometChatConstants.GROUP_TYPE_PUBLIC, ignoreCase = true)) {
                resultList.add(group)
            } else if (UIKitSettings.groupInMode == GroupMode.PASSWORD_GROUP &&
                    group.groupType.equals(CometChatConstants.GROUP_TYPE_PASSWORD, ignoreCase = true)) {
                resultList.add(group)
            }  else if (UIKitSettings.groupInMode == GroupMode.ALL_GROUP) {
                resultList.add(group)
            }
        }
        return resultList
    }

    /**
     * This method is used to search groups present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-groups-retrieve-groups" ">&quot;https://prodocs.cometchat.com/docs/android-groups-retrieve-groups&quot;
     *
     * @param s is a string used to get groups matches with this string.
     * @see GroupsRequest
     */
    private fun searchGroup(s: String) {
        val groupsRequest: GroupsRequest = GroupsRequestBuilder().setSearchKeyWord(s).setLimit(100).build()
        groupsRequest.fetchNext(object : CallbackListener<List<Group?>?>() {
            public override fun onSuccess(groups: List<Group?>?) {
                rvGroups?.searchGroupList(groups) // sets the groups in rvGroupList i.e CometChatGroupList Component.
            }

            public override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
            }
        })
    }

    /**
     * This method is used to hide shimmer effect if the list is loaded.
     */
    private fun stopHideShimmer() {
        conversationShimmer?.stopShimmer()
        conversationShimmer?.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        groupsRequest = null
        cometChatGroupsAdapter = null
        fetchGroup()
    }

    fun setTitleVisible(isVisible: Boolean) {
        isTitleVisible = isVisible
    }

    fun setGroupCreateVisible(isVisible: Boolean) {
        isCreateGroupVisible = isVisible
    }

    companion object {
        private var event: OnItemClickListener<Any>? = null
        private val TAG: String = "CometChatGroupListScreen"

        /**
         *
         * @param groupItemClickListener An object of `OnItemClickListener<T>` abstract class helps to initialize with events
         * to perform onItemClick & onItemLongClick.
         * @see OnItemClickListener
         */
        @JvmStatic
        fun setItemClickListener(groupItemClickListener: OnItemClickListener<Any>) {
            event = groupItemClickListener
        }
    }
}