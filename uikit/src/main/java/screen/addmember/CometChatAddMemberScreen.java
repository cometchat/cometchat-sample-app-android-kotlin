package screen.addmember;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.UsersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.UserListAdapter;
import constant.StringContract;
import listeners.ClickListener;
import listeners.RecyclerTouchListener;
import listeners.StickyHeaderDecoration;
import screen.CometChatUserDetailScreenActivity;
import utils.FontUtils;
import utils.Utils;

public class CometChatAddMemberScreen extends Fragment {
    private static final String TAG = "CometChatAddMember";

    private UserListAdapter userListAdapter;

    private UsersRequest usersRequest;

    private RecyclerView rvUserList;

    private EditText etSearch;

    private ImageView clearSearch;

    private String guid;

    private ArrayList<String> groupMembersUids = new ArrayList<>();

    private String groupName;

    private FontUtils fontUtils;

    private MaterialToolbar toolbar;

    List<GroupMember> groupMembers = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleArguments();
        fontUtils=FontUtils.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comet_chat_add_member_screen, container, false);

        init(view);
        return view;
    }
     
    public void init(View view)
    {
        // Inflate the layout
        setHasOptionsMenu(true);
        rvUserList = view.findViewById(R.id.rv_user_list);
        etSearch = view.findViewById(R.id.search_bar);

        toolbar = view.findViewById(R.id.add_member_toolbar);

        setToolbar(toolbar);

        checkDarkMode();

        clearSearch = view.findViewById(R.id.clear_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0)
                    clearSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH)
                {
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
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                if (inputMethodManager!=null)
                inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(),0);
            }
        });

        rvUserList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers();
                }
            }
        });


        rvUserList.addOnItemTouchListener(new RecyclerTouchListener(getContext(), rvUserList, new ClickListener() {

            @Override
            public void onClick(View var1, int var2) {
                User user=(User)var1.getTag(R.string.user);

                if (getActivity()!=null) {
                    Intent intent = new Intent(getActivity(), CometChatUserDetailScreenActivity.class);
                    intent.putExtra(StringContract.IntentStrings.UID, user.getUid());
                    intent.putExtra(StringContract.IntentStrings.NAME, user.getName());
                    intent.putExtra(StringContract.IntentStrings.AVATAR, user.getAvatar());
                    intent.putExtra(StringContract.IntentStrings.STATUS, user.getStatus());
                    intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, user.isBlockedByMe());
                    intent.putExtra(StringContract.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP);
                    intent.putExtra(StringContract.IntentStrings.GUID, guid);
                    intent.putExtra(StringContract.IntentStrings.IS_ADD_MEMBER, true);
                    intent.putExtra(StringContract.IntentStrings.GROUP_NAME, groupName);
                    getActivity().finish();

                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View var1, int var2) {

            }
        }));
    }

    private void checkDarkMode() {
        if(Utils.isDarkMode(getContext())) {
            toolbar.setTitleTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            toolbar.setTitleTextColor(getResources().getColor(R.color.primaryTextColor));
        }
    }

    private void setToolbar(MaterialToolbar toolbar) {
        if (Utils.changeToolbarFont(toolbar)!=null){
            Utils.changeToolbarFont(toolbar).setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        }
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void handleArguments() {
        if (getArguments() != null) {
            guid = getArguments().getString(StringContract.IntentStrings.GUID);
            groupName = getArguments().getString(StringContract.IntentStrings.GROUP_NAME);
            groupMembersUids = getArguments().getStringArrayList(StringContract.IntentStrings.GROUP_MEMBER);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

           if(item.getItemId()==android.R.id.home){
               if (getActivity()!=null)
              getActivity().onBackPressed();
           }

        return super.onOptionsItemSelected(item);
    }

    private void fetchUsers() {
        if (usersRequest == null) {
            int LIMIT = 30;
            usersRequest = new UsersRequest.UsersRequestBuilder().setLimit(LIMIT).build();
        }

        makeUserRequest(usersRequest);
    }

    private void searchUser(String s) {
        UsersRequest usersRequest = new UsersRequest.UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build();
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (userListAdapter!=null)
                    userListAdapter.searchUser(users);
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: "+e.getMessage());
            }
        });
    }
    private void makeUserRequest(UsersRequest usersRequest) {
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users.size() > 0) {
                    ArrayList<User> userArrayList = new ArrayList<>();
                    for (User user: users)
                    {
                        if (!groupMembersUids.contains(user.getUid()))
                        {
                            userArrayList.add(user);
                        }
                    }
                    setAdapter(userArrayList);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
            }
        });
    }

    private void setAdapter(List<User> users) {
        if (userListAdapter==null){
            userListAdapter=new UserListAdapter(getContext(),Utils.userSort(users));
            StickyHeaderDecoration stickyHeaderDecoration = new StickyHeaderDecoration(userListAdapter);
            rvUserList.addItemDecoration(stickyHeaderDecoration, 0);
            rvUserList.setAdapter(userListAdapter);
        }else {
            userListAdapter.updateList(Utils.userSort(users));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        usersRequest=null;
        userListAdapter=null;
        fetchUsers();
    }




    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }


}
