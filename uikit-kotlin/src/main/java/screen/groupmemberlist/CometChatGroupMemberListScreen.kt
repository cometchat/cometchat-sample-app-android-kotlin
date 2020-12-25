package screen.groupmemberlist

import adapter.GroupMemberAdapter
import android.content.Context
import android.content.DialogInterface
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.GroupMembersRequest
import com.cometchat.pro.core.GroupMembersRequest.GroupMembersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import constant.StringContract
import listeners.ClickListener
import listeners.RecyclerTouchListener
import utils.FontUtils
import utils.Utils
import java.util.*

/**
 * Purpose - CometChatGroupMemberListScreen.class is used to make another admin to other group members.
 * It fetches the list of group member and on click on any group member it changes its scope to admin.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */
class CometChatGroupMemberListScreen : Fragment() {
    private var groupMemberListAdapter: GroupMemberAdapter? = null
    private var groupMembersRequest: GroupMembersRequest? = null
    private var showModerators = false
    private var rvUserList: RecyclerView? = null
    private var etSearch: EditText? = null
    private var clearSearch: ImageView? = null
    private var guid: String? = null
    private var c: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            guid = arguments!!.getString(StringContract.IntentStrings.GUID)
            showModerators = arguments!!.getBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_comet_chat_add_member_screen, container, false)
        rvUserList = view.findViewById(R.id.rv_user_list)
        etSearch = view.findViewById(R.id.search_bar)
        clearSearch = view.findViewById(R.id.clear_search)
        val toolbar: MaterialToolbar = view.findViewById(R.id.add_member_toolbar)
        setToolbar(toolbar)
        etSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0) clearSearch!!.setVisibility(View.VISIBLE)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        etSearch!!.setOnEditorActionListener(OnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                searchUser(textView.text.toString())
                clearSearch!!.setVisibility(View.VISIBLE)
                return@OnEditorActionListener true
            }
            false
        })
        clearSearch!!.setOnClickListener(View.OnClickListener { view1: View? ->
            etSearch!!.setText("")
            clearSearch!!.setVisibility(View.GONE)
            searchUser(etSearch!!.getText().toString())
            if (activity != null) {
                val inputMethodManager = (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                inputMethodManager.hideSoftInputFromWindow(etSearch!!.getWindowToken(), 0)
            }
        })
        rvUserList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchGroupMembers()
                }
            }
        })

        // On click of any group member item in rvUserList, It shows dialog with positive and negative button. On click of positive button it changes scope of group member
        rvUserList!!.addOnItemTouchListener(RecyclerTouchListener(getContext(), rvUserList!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val groupMember = var1.getTag(R.string.user) as GroupMember
                if (showModerators) {
                    if (activity != null) {
                        val alert_dialog = MaterialAlertDialogBuilder(activity)
                        alert_dialog.setTitle(resources.getString(R.string.make_moderator))
                        alert_dialog.setMessage(String.format(resources.getString(R.string.make_moderator_question), groupMember.name))
                        alert_dialog.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface: DialogInterface?, i: Int -> updateAsModeratorScope(groupMember) }
                        alert_dialog.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                        alert_dialog.create()
                        alert_dialog.show()
                    }
                } else {
                    if (activity != null) {
                        val alert_dialog = MaterialAlertDialogBuilder(activity)
                        alert_dialog.setTitle(resources.getString(R.string.make_admin))
                        alert_dialog.setMessage(String.format(resources.getString(R.string.make_admin_question), groupMember.name))
                        alert_dialog.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface: DialogInterface?, i: Int -> updateAsAdminScope(groupMember) }
                        alert_dialog.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                        alert_dialog.create()
                        alert_dialog.show()
                    }
                }
            }
        }))
        fetchGroupMembers()
        return view
    }

    private fun setToolbar(toolbar: MaterialToolbar) {
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar)!!.typeface = FontUtils.getInstance(activity).getTypeFace(FontUtils.robotoMedium)
        }
        if (activity != null) {
            (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
            (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun updateAsAdminScope(groupMember: GroupMember) {
        CometChat.updateGroupMemberScope(groupMember.uid, guid!!, CometChatConstants.SCOPE_ADMIN, object : CallbackListener<String>() {
            override fun onSuccess(s: String) {
                Log.d(TAG, "onSuccess: $s")
                groupMemberListAdapter!!.removeGroupMember(groupMember)
                Snackbar.make(rvUserList!!, String.format(resources.getString(R.string.user_is_admin), groupMember.name), Snackbar.LENGTH_LONG).show()
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                Snackbar.make(rvUserList!!, String.format(resources.getString(R.string.update_scope_error), groupMember.name), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAsModeratorScope(groupMember: GroupMember) {
        CometChat.updateGroupMemberScope(groupMember.uid, guid!!, CometChatConstants.SCOPE_MODERATOR, object : CallbackListener<String>() {
            override fun onSuccess(s: String) {
                Log.d(TAG, "onSuccess: $s")
                groupMemberListAdapter!!.removeGroupMember(groupMember)
                Snackbar.make(rvUserList!!, String.format(resources.getString(R.string.user_is_moderator), groupMember.name), Snackbar.LENGTH_LONG).show()
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                Snackbar.make(rvUserList!!, String.format(resources.getString(R.string.update_scope_error), groupMember.name), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * This method is used to fetch list of group members.
     *
     * @see GroupMembersRequest
     */
    private fun fetchGroupMembers() {
        if (groupMembersRequest == null) {
            groupMembersRequest = GroupMembersRequestBuilder(guid).setLimit(10).build()
        }
        groupMembersRequest!!.fetchNext(object : CallbackListener<List<GroupMember>>() {
            override fun onSuccess(users: List<GroupMember>) {
                if (users.size > 0) {
                    val filterlist: MutableList<GroupMember> = ArrayList()
                    for (gmember in users) {
                        if (showModerators) {
                            if (gmember.scope == CometChatConstants.SCOPE_PARTICIPANT) {
                                filterlist.add(gmember)
                            }
                        } else {
                            if (gmember.scope == CometChatConstants.SCOPE_PARTICIPANT || gmember.scope == CometChatConstants.SCOPE_MODERATOR) {
                                filterlist.add(gmember)
                            }
                        }
                    }
                    setAdapter(filterlist)
                }
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                Snackbar.make(rvUserList!!, resources.getString(R.string.group_member_list_error), Snackbar.LENGTH_LONG).show()
                //                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
    }

    /**
     * This method is used to perform search operation on list of group members.
     *
     * @param s is a String which is used to search group members.
     *
     * @see GroupMembersRequest
     */
    private fun searchUser(s: String) {
        val groupMembersRequest = GroupMembersRequestBuilder(guid).setSearchKeyword(s).setLimit(10).build()
        groupMembersRequest.fetchNext(object : CallbackListener<List<GroupMember>>() {
            override fun onSuccess(groupMembers: List<GroupMember>) {
                if (groupMemberListAdapter != null) {
                    val filterlist: MutableList<GroupMember> = ArrayList()
                    for (gmember in groupMembers) {
                        if (gmember.scope == CometChatConstants.SCOPE_PARTICIPANT) {
                            filterlist.add(gmember)
                        }
                    }
                    groupMemberListAdapter!!.searchGroupMembers(filterlist)
                }
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    /**
     * This method is used to set Adapter for groupMemberList.
     * @param groupMembers
     */
    private fun setAdapter(groupMembers: List<GroupMember>) {
        if (groupMemberListAdapter == null) {
            groupMemberListAdapter = GroupMemberAdapter(getContext()!!, groupMembers, null)
            rvUserList!!.adapter = groupMemberListAdapter
        } else {
            groupMemberListAdapter!!.updateGroupMembers(groupMembers)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onAttach(context: Context) {
        this.c = context
        super.onAttach(context)
    }

    companion object {
        private const val TAG = "CometChatGroupMember"
    }
}