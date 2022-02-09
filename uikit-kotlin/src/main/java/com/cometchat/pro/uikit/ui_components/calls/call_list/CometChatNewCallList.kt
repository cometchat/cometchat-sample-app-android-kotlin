package com.cometchat.pro.uikit.ui_components.calls.call_list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.core.UsersRequest.UsersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsers
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.facebook.shimmer.ShimmerFrameLayout

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
 */ /**
 *
 * Purpose - CometChatUserCallListScreenActivity class is a activity used to display list of users
 * and perform call operation on click of item.It also provide search bar to search user
 * from the list.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */
class CometChatNewCallList constructor() : AppCompatActivity() {
    private val LIMIT: Int = 30
    private var usersRequest // Use to fetch users
            : UsersRequest? = null
    private var rvUserList: CometChatUsers? = null
    private var etSearch // Use to perform search operation on list of users.
            : EditText? = null
    private var clearSearch //Use to clear the search operation performed on list.
            : ImageView? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null
    private var title: TextView? = null
    private var rlSearchBox: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_cometchat_userlist)
        title = findViewById(R.id.tv_title)
        val imageView: ImageView = ImageView(this)
        imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_back))
        if (UIKitSettings.color != null) {
            window.statusBarColor = Color.parseColor(UIKitSettings.color)
            imageView.imageTintList = ColorStateList.valueOf(
                    Color.parseColor(UIKitSettings.color))
        } else imageView.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))

        imageView.isClickable = true
        imageView.setPadding(8, 8, 8, 8)
        val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_START)
        layoutParams.setMargins(16, 32, 16, 16)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        imageView.layoutParams = layoutParams
        addContentView(imageView, layoutParams)
        imageView.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                onBackPressed()
            }
        })
        title!!.setTypeface(FontUtils.getInstance(this).getTypeFace(FontUtils.robotoMedium))
        val titleLayoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        titleLayoutParams.setMargins(16, 32, 16, 48)
        titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        title!!.setLayoutParams(titleLayoutParams)
        title!!.setText(getResources().getString(R.string.new_call))
        rvUserList = findViewById(R.id.rv_user_list)
        etSearch = findViewById(R.id.search_bar)
        clearSearch = findViewById(R.id.clear_search)
        rlSearchBox = findViewById(R.id.rl_search_box)
        shimmerFrameLayout = findViewById(R.id.shimmer_layout)
        etSearch!!.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun afterTextChanged(editable: Editable) {
                if (editable.length == 0) {
                    // if etSearch is empty then fetch all users.
                    usersRequest = null
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
                val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        rvUserList!!.setItemClickListener(object : OnItemClickListener<User?>() {
            public override fun OnItemClick(t: Any, position: Int) {
                val user: User = t as User
                initiatecall(user.getUid(), CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO)
            }
        })
        fetchUsers()
    }

    private fun stopHideShimmer() {
        shimmerFrameLayout!!.stopShimmer()
        shimmerFrameLayout!!.setVisibility(View.GONE)
        title!!.setVisibility(View.VISIBLE)
        rlSearchBox!!.setVisibility(View.VISIBLE)
    }

    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-users-retrieve-users.section-retrieve-list-of-users" ">&quot;https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users&quot;
     *
     * @see UsersRequest
     */
    private fun fetchUsers() {
        if (usersRequest == null) {
            Log.v(TAG, "newfetchUsers: ")
            usersRequest = UsersRequestBuilder().setLimit(30).build()
        }
        usersRequest!!.fetchNext(object : CallbackListener<List<User?>>() {
            public override fun onSuccess(users: List<User?>) {
                Log.v(TAG, "onfetchSuccess: " + users.size)
                stopHideShimmer()
                rvUserList!!.setUserList(users)
            }

            public override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                stopHideShimmer()
                ErrorMessagesUtils.cometChatErrorMessage(this@CometChatNewCallList, e.code)
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
                rvUserList!!.searchUserList(users)
            }

            public override fun onError(e: CometChatException) {
                if (rvUserList != null)
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatNewCallList, e.code)
            }
        })
    }

    fun initiatecall(recieverID: String?, receiverType: String?, callType: String?) {
        val call: Call = Call((recieverID)!!, receiverType, callType)
        CometChat.initiateCall(call, object : CallbackListener<Call>() {
            public override fun onSuccess(call: Call) {
                Utils.startCallIntent(this@CometChatNewCallList, (call.callReceiver as User), call.type, true, call.sessionId)
            }

            public override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                if (rvUserList != null)
                    ErrorMessagesUtils.cometChatErrorMessage(this@CometChatNewCallList, e.code)
            }
        })
    }

    companion object {
        private val TAG: String = "CometChatUserCallList"
    }
}