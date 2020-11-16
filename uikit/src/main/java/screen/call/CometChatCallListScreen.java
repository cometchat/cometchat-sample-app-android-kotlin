package screen.call;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.uikit.CometChatCallList;
import com.cometchat.pro.uikit.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import adapter.TabAdapter;
import listeners.OnItemClickListener;
import screen.CometChatUserCallListScreenActivity;
import com.cometchat.pro.uikit.Settings.UISettings;
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
        if (UISettings.getColor()!=null) {
            phoneAddIv.setImageTintList(ColorStateList.valueOf(Color.parseColor(UISettings.getColor())));
            Drawable wrappedDrawable = DrawableCompat.wrap(getResources().
                    getDrawable(R.drawable.tab_layout_background_active));
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UISettings.getColor()));
            tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).view.setBackground(wrappedDrawable);
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor(UISettings.getColor()));
        } else {
            tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).
                    view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (UISettings.getColor()!=null) {
                    Drawable wrappedDrawable = DrawableCompat.wrap(getResources().
                            getDrawable(R.drawable.tab_layout_background_active));
                    DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UISettings.getColor()));
                    tab.view.setBackground(wrappedDrawable);
                }
                else
                    tab.view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
