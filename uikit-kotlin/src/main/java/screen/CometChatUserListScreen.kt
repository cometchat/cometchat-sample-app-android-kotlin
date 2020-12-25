package screen

import adapter.UserListAdapter
import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.core.UsersRequest.UsersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatUserList
import com.cometchat.pro.uikit.R
import com.facebook.shimmer.ShimmerFrameLayout
import listeners.OnItemClickListener
import utils.FontUtils
import utils.Utils
import java.util.*

/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /*

* Purpose - CometChatUserList class is a fragment used to display list of users and perform certain action on click of item.
            It also provide search bar to search user from the list.

* @author - CometChat

* @version - v1.0

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/
class CometChatUserListScreen constructor() : Fragment() {
    private val LIMIT: Int = 30
    private var c: Context? = null
    private val isSearching: Boolean = false
    private val userListAdapter: UserListAdapter? = null
    private var usersRequest // Use to fetch users
            : UsersRequest? = null
    private var rvUserList // Use to display list of users
            : CometChatUserList? = null
    private var etSearch // Use to perform search operation on list of users.
            : EditText? = null
    private var clearSearch //Use to clear the search operation performed on list.
            : ImageView? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null
    private var title: TextView? = null
    private var rlSearchBox: RelativeLayout? = null
    private var noUserLayout: LinearLayout? = null
    private val userList: MutableList<User> = ArrayList()
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_user_list_screen, container, false)
        title = view.findViewById(R.id.tv_title)
        title!!.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium))
        rvUserList = view.findViewById(R.id.rv_user_list)
        noUserLayout = view.findViewById(R.id.no_user_layout)
        etSearch = view.findViewById(R.id.search_bar)
        clearSearch = view.findViewById(R.id.clear_search)
        rlSearchBox = view.findViewById(R.id.rl_search_box)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)
        if (Utils.isDarkMode(getContext()!!)) {
            title!!.setTextColor(getResources().getColor(R.color.textColorWhite))
        } else {
            title!!.setTextColor(getResources().getColor(R.color.primaryTextColor))
        }
        etSearch!!.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun afterTextChanged(editable: Editable) {
                if (editable.length == 0) {
                    // if etSearch is empty then fetch all users.
                    usersRequest = null
                    rvUserList!!.clear()
                    fetchUsers()
                } else {
                    // Search users based on text in etSearch field.
                    searchUser(editable.toString())
                }
            }
        })
        etSearch!!.setOnEditorActionListener(object : OnEditorActionListener {
            public override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchUser(textView.getText().toString())
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
                searchUser(etSearch!!.getText().toString())
                val inputMethodManager: InputMethodManager = getContext()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(etSearch!!.getWindowToken(), 0)
            }
        })


        // Uses to fetch next list of user if rvUserList (RecyclerView) is scrolled in upward direction.
        rvUserList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            public override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers()
                }
            }
        })

        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        rvUserList!!.setItemClickListener(object : OnItemClickListener<User?>() {
            public override fun OnItemClick(t: Any, position: Int) {
                if (events!=null ) events!!.OnItemClick(t as User, position)
            }
        })
        return view
    }

    private fun stopHideShimmer() {
        shimmerFrameLayout!!.stopShimmer()
        shimmerFrameLayout!!.setVisibility(View.GONE)
        title!!.setVisibility(View.VISIBLE)
        rlSearchBox!!.setVisibility(View.VISIBLE)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUsers()
    }

    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-users-retrieve-users.section-retrieve-list-of-users" ">&quot;https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users&quot;
     *
     * @see UsersRequest
     */
    private fun fetchUsers() {
        if (usersRequest == null) {
            Log.e(TAG, "newfetchUsers: ")
            usersRequest = UsersRequestBuilder().setLimit(30).build()
        }
        usersRequest!!.fetchNext(object : CallbackListener<List<User>>() {
            public override fun onSuccess(users: List<User>) {
                Log.e(TAG, "onfetchSuccess: " + users.size)
                userList.addAll(users)
                stopHideShimmer()
                rvUserList!!.setUserList(users) // set the users to rvUserList i.e CometChatUserList Component.
                if (userList.size == 0) {
                    noUserLayout!!.setVisibility(View.VISIBLE)
                    rvUserList!!.setVisibility(View.GONE)
                } else {
                    rvUserList!!.setVisibility(View.VISIBLE)
                    noUserLayout!!.setVisibility(View.GONE)
                }
            }

            public override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                stopHideShimmer()
                if (getActivity() != null) Toast.makeText(getActivity(), e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * This method is used to search users present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-users-retrieve-users.section-retrieve-list-of-users" ">&quot;https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users&quot;
     *
     * @param s is a string used to get users matches with this string.
     * @see UsersRequest
     */
    private fun searchUser(s: String) {
        val usersRequest: UsersRequest = UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build()
        usersRequest.fetchNext(object : CallbackListener<List<User?>?>() {
            public override fun onSuccess(users: List<User?>?) {
                rvUserList!!.searchUserList(users) // set the users to rvUserList i.e CometChatUserList Component.
            }

            public override fun onError(e: CometChatException) {
                Toast.makeText(context, "Error " + e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onAttach(context: Context) {
        super.onAttach(context)
        this.c = context
    }

    companion object {
        private val TAG: String = "CometChatUserListScreen"
        private var events: OnItemClickListener<Any>? = null

        /**
         *
         * @param onItemClickListener An object of `OnItemClickListener<T>` abstract class helps to initialize with events
         * to perform onItemClick & onItemLongClick,
         * @see OnItemClickListener
         */
        @JvmStatic
        fun setItemClickListener(onItemClickListener: OnItemClickListener<Any>?) {
            events = onItemClickListener
        }
    }
}