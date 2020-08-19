package screen.groupmemberlist;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupMembersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupMemberAdapter;
import constant.StringContract;
import listeners.ClickListener;
import listeners.RecyclerTouchListener;
import utils.FontUtils;
import utils.Utils;

/**
 * Purpose - CometChatGroupMemberListScreen.class is used to make another admin to other group members.
 * It fetches the list of group member and on click on any group member it changes its scope to admin.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */

public class CometChatGroupMemberListScreen extends Fragment {
    private static final String TAG = "CometChatGroupMember";

    private GroupMemberAdapter groupMemberListAdapter;

    private GroupMembersRequest groupMembersRequest;

    private boolean showModerators;

    private RecyclerView rvUserList;

    private EditText etSearch;

    private ImageView clearSearch;

    private String guid;

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       if (getArguments()!=null) {
           guid = getArguments().getString(StringContract.IntentStrings.GUID);
           showModerators = getArguments().getBoolean(StringContract.IntentStrings.SHOW_MODERATORLIST);
       }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comet_chat_add_member_screen, container, false);

        rvUserList = view.findViewById(R.id.rv_user_list);

        etSearch = view.findViewById(R.id.search_bar);

        clearSearch = view.findViewById(R.id.clear_search);

        MaterialToolbar toolbar = view.findViewById(R.id.add_member_toolbar);
        setToolbar(toolbar);

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


        etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH)
            {
                searchUser(textView.getText().toString());
                clearSearch.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
        clearSearch.setOnClickListener(view1 -> {
            etSearch.setText("");
            clearSearch.setVisibility(View.GONE);
            searchUser(etSearch.getText().toString());
             if (getActivity()!=null) {
                 InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                 // Hide the soft keyboard
                 assert inputMethodManager != null;
                 inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
             }
        });

        rvUserList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    fetchGroupMembers();
                }

            }
        });

        // On click of any group member item in rvUserList, It shows dialog with positive and negative button. On click of positive button it changes scope of group member
        rvUserList.addOnItemTouchListener(new RecyclerTouchListener(getContext(), rvUserList, new ClickListener() {

            @Override
            public void onClick(View var1, int var2) {
                GroupMember groupMember = (GroupMember) var1.getTag(R.string.user);
                if (showModerators){
                    if (getActivity() != null) {
                        MaterialAlertDialogBuilder alert_dialog = new MaterialAlertDialogBuilder(getActivity());
                        alert_dialog.setTitle(getResources().getString(R.string.make_moderator));
                        alert_dialog.setMessage(String.format(getResources().getString(R.string.make_moderator_question), groupMember.getName()));
                        alert_dialog.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> updateAsModeratorScope(groupMember));
                        alert_dialog.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
                        alert_dialog.create();
                        alert_dialog.show();
                    }
                } else {
                    if (getActivity() != null) {
                        MaterialAlertDialogBuilder alert_dialog = new MaterialAlertDialogBuilder(getActivity());
                        alert_dialog.setTitle(getResources().getString(R.string.make_admin));
                        alert_dialog.setMessage(String.format(getResources().getString(R.string.make_admin_question), groupMember.getName()));
                        alert_dialog.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> updateAsAdminScope(groupMember));
                        alert_dialog.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
                        alert_dialog.create();
                        alert_dialog.show();

                    }
                }
            }
        }));

        fetchGroupMembers();

        return view;
    }

    private void setToolbar(MaterialToolbar toolbar) {
        if (Utils.changeToolbarFont(toolbar) != null) {
            Utils.changeToolbarFont(toolbar).setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));
        }
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateAsAdminScope(GroupMember groupMember) {

        CometChat.updateGroupMemberScope(groupMember.getUid(), guid, CometChatConstants.SCOPE_ADMIN, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess: "+s);
                groupMemberListAdapter.removeGroupMember(groupMember);
                Snackbar.make(rvUserList,String.format(getResources().getString(R.string.user_is_admin),groupMember.getName()), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage() );
                Snackbar.make(rvUserList,String.format(getResources().getString(R.string.update_scope_error),groupMember.getName()),Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAsModeratorScope(GroupMember groupMember) {

        CometChat.updateGroupMemberScope(groupMember.getUid(), guid, CometChatConstants.SCOPE_MODERATOR, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess: "+s);
                groupMemberListAdapter.removeGroupMember(groupMember);
                if (rvUserList!=null)
                    Snackbar.make(rvUserList,String.format(getResources().getString(R.string.user_is_moderator),groupMember.getName()), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage() );
                Snackbar.make(rvUserList,String.format(getResources().getString(R.string.update_scope_error),groupMember.getName()),Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is used to fetch list of group members.
     *
     * @see GroupMembersRequest
     */
    private void fetchGroupMembers() {
        if (groupMembersRequest == null) {
            groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(guid).setLimit(10).build();
        }
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> users) {
                if (users.size() > 0) {
                    List<GroupMember> filterlist = new ArrayList<>();
                    for (GroupMember gmember : users) {
                        if (showModerators) {
                            if (gmember.getScope().equals(CometChatConstants.SCOPE_PARTICIPANT)) {
                                filterlist.add(gmember);
                            }
                        } else {
                            if (gmember.getScope().equals(CometChatConstants.SCOPE_PARTICIPANT) || gmember.getScope().equals(CometChatConstants.SCOPE_MODERATOR)) {
                                filterlist.add(gmember);
                            }
                        }
                    }
                    setAdapter(filterlist);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
                Snackbar.make(rvUserList,getResources().getString(R.string.group_member_list_error),Snackbar.LENGTH_LONG).show();
//                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is used to perform search operation on list of group members.
     *
     * @param s is a String which is used to search group members.
     *
     * @see GroupMembersRequest
     */
    private void searchUser(String s)
    {
        GroupMembersRequest groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(guid).setSearchKeyword(s).setLimit(10).build();
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                if (groupMemberListAdapter!=null)
                {
                    List<GroupMember> filterlist = new ArrayList<>();
                    for (GroupMember gmember : groupMembers) {
                        if (gmember.getScope().equals(CometChatConstants.SCOPE_PARTICIPANT))
                        {
                            filterlist.add(gmember);
                        }
                    }
                    groupMemberListAdapter.searchGroupMembers(filterlist);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage() );
            }
        });
    }


    /**
     * This method is used to set Adapter for groupMemberList.
     * @param groupMembers
     */
    private void setAdapter(List<GroupMember> groupMembers) {
        if (groupMemberListAdapter==null){
            groupMemberListAdapter=new GroupMemberAdapter(getContext(),groupMembers,null);
            rvUserList.setAdapter(groupMemberListAdapter);
        }else {
            groupMemberListAdapter.updateGroupMembers(groupMembers);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }
}
