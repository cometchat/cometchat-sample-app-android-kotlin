package com.cometchat.pro.uikit.ui_components.groups.group_list

import android.content.Context
import android.content.Intent
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.GroupsRequest
import com.cometchat.pro.core.GroupsRequest.GroupsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Group
import com.cometchat.pro.uikit.ui_components.shared.cometchatGroups.CometChatGroups
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.groups.create_group.CometChatCreateGroupActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatGroups.CometChatGroupsAdapter
import com.google.android.material.snackbar.Snackbar
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/*

* Purpose - CometChatGroupList class is a fragment used to display list of groups and perform certain action on click of item.
            It also provide search bar to search group from the list.

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/
class CometChatGroupList constructor() : Fragment() {
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
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_cometchat_group_list, container, false)
        val title: TextView = view.findViewById(R.id.tv_title)
        title.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium))
        rvGroups = view.findViewById(R.id.rv_group_list)
        noGroupLayout = view.findViewById(R.id.no_group_layout)
        etSearch = view.findViewById(R.id.search_bar)
        clearSearch = view.findViewById(R.id.clear_search)
        ivCreateGroup = view.findViewById(R.id.create_group)
        if (Utils.isDarkMode(getContext()!!)) {
            title.setTextColor(getResources().getColor(R.color.textColorWhite))
        } else {
            title.setTextColor(getResources().getColor(R.color.primaryTextColor))
        }
        ivCreateGroup!!.setOnClickListener(View.OnClickListener({ view1: View? ->
            val intent: Intent = Intent(getContext(), CometChatCreateGroupActivity::class.java)
            startActivity(intent)
        }))
        etSearch!!.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun afterTextChanged(editable: Editable) {
                if (editable.length == 0) {
                    // if etSearch is empty then fetch all groups.
                    groupsRequest = null
                    rvGroups!!.clear()
                    fetchGroup()
                } else {
                    // Search group based on text in etSearch field.
                    searchGroup(editable.toString())
                }
            }
        })
        etSearch!!.setOnEditorActionListener(object : OnEditorActionListener {
            public override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchGroup(textView.getText().toString())
                    clearSearch!!.setVisibility(View.VISIBLE)
                    return true
                }
                return false
            }
        })
        clearSearch!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                etSearch!!.setText("")
                clearSearch!!.setVisibility(View.GONE)
                searchGroup(etSearch!!.getText().toString())
                val inputMethodManager: InputMethodManager = getContext()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(etSearch!!.getWindowToken(), 0)
            }
        })

        //Uses to fetch next list of group if rvGroupList (RecyclerView) is scrolled in upward direction.
        rvGroups!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            public override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchGroup()
                }
            }
        })

        // Used to trigger event on click of group item in rvGroupList (RecyclerView)
        rvGroups!!.setItemClickListener(object : OnItemClickListener<Group?>() {
            public override fun OnItemClick(t: Any, position: Int) {
                event!!.OnItemClick(t as Group, position)
            }
        })
        return view
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        groupsRequest!!.fetchNext(object : CallbackListener<List<Group>?>() {
            public override fun onSuccess(groups: List<Group>?) {
                rvGroups!!.setGroupList(groups) // sets the groups in rvGroupList i.e CometChatGroupList Component.
                groupList.addAll((groups)!!)
                if (groupList.size == 0) {
                    noGroupLayout!!.setVisibility(View.VISIBLE)
                    rvGroups!!.setVisibility(View.GONE)
                } else {
                    noGroupLayout!!.setVisibility(View.GONE)
                    rvGroups!!.setVisibility(View.VISIBLE)
                }
            }

            public override fun onError(e: CometChatException) {
                if (rvGroups != null) Snackbar.make(rvGroups!!, getResources().getString(R.string.group_list_error), Snackbar.LENGTH_LONG).show()
            }
        })
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
                rvGroups!!.searchGroupList(groups) // sets the groups in rvGroupList i.e CometChatGroupList Component.
            }

            public override fun onError(e: CometChatException) {
                Log.d(TAG, "onError: " + e.message)
            }
        })
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