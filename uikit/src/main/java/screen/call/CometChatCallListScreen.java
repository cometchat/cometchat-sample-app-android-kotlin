package screen.call;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.ConversationsRequest;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatCallList;
import com.cometchat.pro.uikit.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import adapter.ConversationListAdapter;
import adapter.TabAdapter;
import listeners.ClickListener;
import listeners.OnItemClickListener;
import listeners.RecyclerTouchListener;
import screen.CometChatCallActivity;
import screen.CometChatUserCallListScreenActivity;
import utils.FontUtils;
import utils.Utils;

/**
 * * Purpose - CometChatCallList class is a activity used to display list of calls recieved to user and perform certain action on click of item.
 *              It also consist of two tabs <b>All</b> and <b>Missed Call</b>.
 *
 *   Created on - 23rd March 2020
 *
 *   Modified on  - 24th March 2020
 *
**/

public class CometChatCallListScreen extends Fragment {

    private CometChatCallList rvCallList;

    private MessagesRequest messageRequest;    //Uses to fetch Conversations.

    private static OnItemClickListener events;

    private TextView tvTitle;

    private ShimmerFrameLayout conversationShimmer;

    private static final String TAG = "CallList";

    private View view;

    private List<Call> callList = new ArrayList<>();

    private TabAdapter tabAdapter;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private ImageView phoneAddIv;

    public CometChatCallListScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.call_screen, container, false);
        tvTitle = view.findViewById(R.id.tv_title);
        phoneAddIv = view.findViewById(R.id.add_phone_iv);
        phoneAddIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserListScreen();
            }
        });
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        if (getActivity() != null) {
            tabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
            tabAdapter.addFragment(new AllCall(), getContext().getResources().getString(R.string.all));
            tabAdapter.addFragment(new MissedCall(), getContext().getResources().getString(R.string.missed));
            viewPager.setAdapter(tabAdapter);
        }
        tabLayout.setupWithViewPager(viewPager);
        checkDarkMode();
        return view;
    }

    private void checkDarkMode() {
        if(Utils.isDarkMode(getContext())) {
            tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite));
            tabLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
            tabLayout.setTabTextColors(getResources().getColor(R.color.textColorWhite),getResources().getColor(R.color.light_grey));
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor));
            tabLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor),getResources().getColor(R.color.textColorWhite));
        }

    }

    private void openUserListScreen() {
        Intent intent = new Intent(getContext(),CometChatUserCallListScreenActivity.class);
        startActivity(intent);
    }

}
