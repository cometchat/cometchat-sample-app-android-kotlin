package screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.CometChatGroupList;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.models.Group;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import adapter.GroupListAdapter;
import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import screen.creategroup.CometChatCreateGroupScreenActivity;
import utils.FontUtils;
import utils.Utils;

/*

* Purpose - CometChatGroupList class is a fragment used to display list of groups and perform certain action on click of item.
            It also provide search bar to search group from the list.

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/

public class CometChatGroupListScreen extends Fragment  {

    private static OnItemClickListener event;

    private CometChatGroupList rvGroupList;   //Uses to display list of groups.

    private GroupListAdapter groupListAdapter;

    private GroupsRequest groupsRequest;    //Uses to fetch Groups.

    private EditText etSearch;    //Uses to perform search operations on groups.

    private ImageView clearSearch;

    private ImageView ivCreateGroup;

    private LinearLayout noGroupLayout;

    private List<Group> groupList = new ArrayList<>();

    private static final String TAG = "CometChatGroupListScreen";

    public CometChatGroupListScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_group_list_screen, container, false);
        TextView title = view.findViewById(R.id.tv_title);
        title.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));
        rvGroupList=view.findViewById(R.id.rv_group_list);
        noGroupLayout = view.findViewById(R.id.no_group_layout);
        etSearch = view.findViewById(R.id.search_bar);
        clearSearch = view.findViewById(R.id.clear_search);

        ivCreateGroup = view.findViewById(R.id.create_group);

        if(Utils.isDarkMode(getContext())) {
            title.setTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            title.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }

        ivCreateGroup.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), CometChatCreateGroupScreenActivity.class);
            startActivity(intent);
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==0) {
                    // if etSearch is empty then fetch all groups.
                    groupsRequest=null;
                    rvGroupList.clear();
                    fetchGroup();
                }
                else {
                    // Search group based on text in etSearch field.
                    searchGroup(editable.toString());
                }
            }
        });
        etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH)
                {
                    searchGroup(textView.getText().toString());
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
                searchGroup(etSearch.getText().toString());
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(),0);
            }
        });

        //Uses to fetch next list of group if rvGroupList (RecyclerView) is scrolled in upward direction.
        rvGroupList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    fetchGroup();
                }

            }
        });

        // Used to trigger event on click of group item in rvGroupList (RecyclerView)
        rvGroupList.setItemClickListener(new OnItemClickListener<Group>() {
            @Override
            public void OnItemClick(Group group, int position) {
                if (event!=null)
                    event.OnItemClick(group,position);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     *  This method is used to retrieve list of groups present in your App_ID.
     *  For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-groups-retrieve-groups" }
     *
     * @see GroupsRequest
     */
    private void fetchGroup(){
        if (groupsRequest==null){
            groupsRequest=new GroupsRequest.GroupsRequestBuilder().setLimit(30).build();
        }
        groupsRequest.fetchNext(new CometChat.CallbackListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                rvGroupList.setGroupList(groups); // sets the groups in rvGroupList i.e CometChatGroupList Component.
                groupList.addAll(groups);
                if (groupList.size()==0) {
                    noGroupLayout.setVisibility(View.VISIBLE);
                    rvGroupList.setVisibility(View.GONE);
                } else {
                    noGroupLayout.setVisibility(View.GONE);
                    rvGroupList.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onError(CometChatException e) {
                if (rvGroupList!=null)
                    Snackbar.make(rvGroupList,getResources().getString(R.string.group_list_error),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     *  This method is used to search groups present in your App_ID.
     *  For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-groups-retrieve-groups" }
     *
     * @param s is a string used to get groups matches with this string.
     * @see GroupsRequest
     */
    private void searchGroup(String s)
    {
        GroupsRequest groupsRequest = new GroupsRequest.GroupsRequestBuilder().setSearchKeyWord(s).setLimit(100).build();
        groupsRequest.fetchNext(new CometChat.CallbackListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                rvGroupList.searchGroupList(groups); // sets the groups in rvGroupList i.e CometChatGroupList Component.
            }


            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: "+e.getMessage());
            }
        });
    }


    /**
     *
     * @param groupItemClickListener An object of <code>OnItemClickListener&lt;T&gt;</code> abstract class helps to initialize with events
     *                               to perform onItemClick & onItemLongClick.
     * @see OnItemClickListener
     */
    public static void setItemClickListener(@NonNull OnItemClickListener<Group> groupItemClickListener){
        event=groupItemClickListener;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        groupsRequest=null;
        groupListAdapter=null;
        fetchGroup();
    }
}
