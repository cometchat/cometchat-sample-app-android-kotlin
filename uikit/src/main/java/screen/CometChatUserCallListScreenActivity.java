package screen;

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
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.UsersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatUserList;
import com.cometchat.pro.uikit.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import adapter.UserListAdapter;
import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import listeners.StickyHeaderDecoration;
import utils.FontUtils;
import utils.Utils;

/**

* Purpose - CometChatUserCallListScreenActivity class is a activity used to display list of users
 *          and perform call operation on click of item.It also provide search bar to search user
 *          from the list.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
*/

public class CometChatUserCallListScreenActivity extends AppCompatActivity {

    private static final String TAG = "CometChatUserCallList";

    private int LIMIT = 30;

    private UsersRequest usersRequest;    // Use to fetch users

    private CometChatUserList rvUserList;

    private EditText etSearch;    // Use to perform search operation on list of users.

    private ImageView clearSearch;   //Use to clear the search operation performed on list.

    private ShimmerFrameLayout shimmerFrameLayout;

    private TextView title;

    private RelativeLayout rlSearchBox;

    public CometChatUserCallListScreenActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_list_screen);
        title = findViewById(R.id.tv_title);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_24dp));
        imageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        imageView.setClickable(true);
        imageView.setPadding(8,8,8,8);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_START);
        layoutParams.setMargins(16,32,16,16);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        imageView.setLayoutParams(layoutParams);
        addContentView(imageView,layoutParams);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        title.setTypeface(FontUtils.getInstance(this).getTypeFace(FontUtils.robotoMedium));
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.setMargins(16,32,16,48);
        titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(titleLayoutParams);
        title.setText(getResources().getString(R.string.new_call));
        rvUserList = findViewById(R.id.rv_user_list);
        etSearch = findViewById(R.id.search_bar);
        clearSearch = findViewById(R.id.clear_search);
        rlSearchBox=findViewById(R.id.rl_search_box);

        shimmerFrameLayout=findViewById(R.id.shimmer_layout);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==0) {
                    // if etSearch is empty then fetch all users.
                    usersRequest=null;
                    fetchUsers();
                }
                else {
                    // Search users based on text in etSearch field.
                    searchUser(editable.toString());
                }
            }
        });


        etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchUser(textView.getText().toString());
                    clearSearch.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText("");
                clearSearch.setVisibility(View.GONE);
                searchUser(etSearch.getText().toString());
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        });


        // Uses to fetch next list of user if rvUserList (RecyclerView) is scrolled in upward direction.
        rvUserList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers();
                }

            }
        });

        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        rvUserList.setItemClickListener(new OnItemClickListener<User>() {
            @Override
            public void OnItemClick(User var, int position) {
                User user = var;
                initiatecall(user.getUid(), CometChatConstants.RECEIVER_TYPE_USER,CometChatConstants.CALL_TYPE_AUDIO);
            }
        });
        fetchUsers();
    }

    private void stopHideShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        rlSearchBox.setVisibility(View.VISIBLE);
    }

    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users" }
     *
     * @see UsersRequest
     */
    private void fetchUsers() {

        if (usersRequest == null) {
            Log.e(TAG, "newfetchUsers: " );
            usersRequest = new UsersRequest.UsersRequestBuilder().setLimit(30).build();
        }
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.e(TAG, "onfetchSuccess: "+users.size() );
                stopHideShimmer();
                rvUserList.setUserList(users);
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: " + e.getMessage());
                stopHideShimmer();
                Toast.makeText(CometChatUserCallListScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is used to search users present in your App_ID.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users" }
     *
     * @param s is a string used to get users matches with this string.
     * @see UsersRequest
     */
    private void searchUser(String s) {
        UsersRequest usersRequest = new UsersRequest.UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build();
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                rvUserList.searchUserList(users);
            }

            @Override
            public void onError(CometChatException e) {
                if (rvUserList!=null)
                    Snackbar.make(rvUserList, "Error " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void initiatecall(String recieverID,String receiverType,String callType)
    {
        Call call = new Call(recieverID,receiverType,callType);
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Utils.startCallIntent(CometChatUserCallListScreenActivity.this,((User)call.getCallReceiver()),call.getType(),true,call.getSessionId());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
                if (rvUserList!=null)
                    Snackbar.make(rvUserList,getResources().getString(R.string.call_initiate_error)+":"+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
