package com.cometchat.sampleapp.java.ui.activity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cometchat.calls.constants.CometChatCallsConstants;
import com.cometchat.calls.model.CallLog;
import com.cometchat.calls.model.CallUser;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.calls.utils.CallUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityCallDetailsBinding;
import com.cometchat.sampleapp.java.ui.adapters.CallDetailsTabFragmentAdapter;
import com.cometchat.sampleapp.java.ui.fragments.CallDetailsTabHistoryFragment;
import com.cometchat.sampleapp.java.ui.fragments.CallDetailsTabParticipantsFragment;
import com.cometchat.sampleapp.java.ui.fragments.CallDetailsTabRecordingFragment;
import com.cometchat.sampleapp.java.viewmodels.CallDetailsViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

public class CallDetailsActivity extends AppCompatActivity {
    private ActivityCallDetailsBinding binding;
    private CallDetailsViewModel viewModel;
    private CallLog callLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyWindowInsets();
        adjustWindowSettings();

        callLog = new Gson().fromJson(getIntent().getStringExtra("callLog"), CallLog.class);
        callLog.setInitiator(new Gson().fromJson(getIntent().getStringExtra("initiator"), CallUser.class));
        callLog.setReceiver(new Gson().fromJson(getIntent().getStringExtra("receiver"), CallUser.class));

        initViewModel();

        initTabFragment();

        initClickListeners();

        binding.parentLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.toolbarBackIcon.setColorFilter(CometChatTheme.getIconTintPrimary(this));
        binding.toolbarTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.toolbarDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
        binding.messageHeaderDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
        binding.infoLayout.setBackgroundColor(CometChatTheme.getBackgroundColor2(this));
        binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.tvInfoCallDuration.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.infoLayoutDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentLayout, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                );

                return insets;
            }
        });
    }

    /**
     * This method is used to set the window settings for the activity.
     * It sets the soft input mode to adjust the resize of the window.
     */

    private void adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void initViewModel() {
        viewModel = new CallDetailsViewModel();
        viewModel.setCallLog(callLog);

        viewModel.getCallLog().observe(this, callLog -> {
            this.callLog = callLog;
            updateInfoView();
        });

        viewModel.getCallDuration().observe(this, callDuration -> {
            binding.tvInfoCallDuration.setText(callDuration);
        });

        viewModel.getReceiverUser().observe(this, user -> {
            binding.messageHeader.setVideoCallButtonVisibility(user.isHasBlockedMe() || user.isBlockedByMe() ? View.GONE : View.VISIBLE);
            binding.messageHeader.setVoiceCallButtonVisibility(user.isHasBlockedMe() || user.isBlockedByMe() ? View.GONE : View.VISIBLE);
            binding.messageHeader.setUser(user);
            binding.messageHeader.setUserStatusVisibility(View.GONE);
            binding.messageHeader.setBackIconVisibility(View.GONE);
        });
    }

    private void initTabFragment() {
        CallDetailsTabFragmentAdapter adapter = new CallDetailsTabFragmentAdapter(this);
        adapter.addFragment(new CallDetailsTabParticipantsFragment(), getString(R.string.app_call_details_participants), callLog);
        adapter.addFragment(new CallDetailsTabRecordingFragment(), getString(R.string.app_call_details_recordings), callLog);
        adapter.addFragment(new CallDetailsTabHistoryFragment(), getString(R.string.app_call_details_history), callLog);
        binding.viewPager.setAdapter(adapter);

        binding.tabLayout.setSelectedTabIndicatorColor(CometChatTheme.getTextColorHighlight(this));

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout,
                              binding.viewPager,
                              (tab, position) -> tab.setText(capitalizeFirstLetter(adapter.getTabTitle(position)))
        ).attach();

        styleTabs(binding.tabLayout);
    }

    private void initClickListeners() {
        binding.toolbarBackIcon.setOnClickListener(view -> finish());
    }

    private void updateInfoView() {
        boolean isLoggedInUser = CallUtils.isLoggedInUser((CallUser) callLog.getInitiator());
        boolean isMissedOrUnanswered = callLog.getStatus().equals(CometChatCallsConstants.CALL_STATUS_UNANSWERED) || callLog.getStatus().equals(
            CometChatCallsConstants.CALL_STATUS_MISSED);
        binding.tvInfoDate.setDateText(Utils.callLogsTimeStamp(callLog.getInitiatedAt(), null));
        if (callLog.getType().equals(CometChatCallsConstants.CALL_TYPE_AUDIO) || callLog
            .getType()
            .equals(CometChatCallsConstants.CALL_TYPE_VIDEO) || callLog.getType().equals(
            CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO)) {
            if (isLoggedInUser) {
                binding.tvInfoTitle.setText(R.string.app_call_outgoing);
                binding.tvInfoTitle.setTextAppearance(CometChatTheme.getTextAppearanceHeading4Medium(this));
                binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
                setupCallIcon(binding.ivInfoIcon,
                              AppCompatResources.getDrawable(this, com.cometchat.chatuikit.R.drawable.cometchat_ic_outgoing_call),
                              CometChatTheme.getSuccessColor(this)
                );
            } else if (isMissedOrUnanswered) {
                binding.tvInfoTitle.setText(R.string.app_call_missed);
                binding.tvInfoTitle.setTextAppearance(CometChatTheme.getTextAppearanceHeading4Medium(this));
                binding.tvInfoTitle.setTextColor(CometChatTheme.getErrorColor(this));
                setupCallIcon(binding.ivInfoIcon,
                              AppCompatResources.getDrawable(this, com.cometchat.chatuikit.R.drawable.cometchat_ic_missed_call),
                              CometChatTheme.getErrorColor(this)
                );
            } else {
                binding.tvInfoTitle.setText(R.string.app_call_incoming);
                binding.tvInfoTitle.setTextAppearance(CometChatTheme.getTextAppearanceHeading4Medium(this));
                binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
                setupCallIcon(binding.ivInfoIcon,
                              AppCompatResources.getDrawable(this, com.cometchat.chatuikit.R.drawable.cometchat_ic_incoming_call),
                              CometChatTheme.getSuccessColor(this)
                );
            }
        }
    }

    /**
     * Capitalizes only the first letter of a string.
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Styles tabs to set active and inactive text color programmatically.
     */
    private void styleTabs(TabLayout tabLayout) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView tabTextView = new TextView(this);
                tabTextView.setText(tab.getText());
                tabTextView.setGravity(Gravity.CENTER);
                tab.setCustomView(tabTextView);

                // Set initial colors
                setTabTextStyle(tabTextView, i == tabLayout.getSelectedTabPosition());
            }
        }

        // Listen for tab selection changes to update styles
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabTextView = (TextView) tab.getCustomView();
                if (tabTextView != null) {
                    setTabTextStyle(tabTextView, true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabTextView = (TextView) tab.getCustomView();
                if (tabTextView != null) {
                    setTabTextStyle(tabTextView, false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Handle reselection if needed
            }
        });
    }

    private void setupCallIcon(ImageView imageView, Drawable icon, @ColorInt int iconTint) {
        imageView.setBackground(icon);
        imageView.setBackgroundTintList(ColorStateList.valueOf(iconTint));
    }

    private void setTabTextStyle(TextView tabTextView, boolean isActive) {
        tabTextView.setTextAppearance(CometChatTheme.getTextAppearanceHeading4Medium(this));
        tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
        if (isActive) {
            tabTextView.setTextColor(CometChatTheme.getTextColorHighlight(this));
        } else {
            tabTextView.setTextColor(CometChatTheme.getTextColorSecondary(this));
        }
    }

    public CallDetailsViewModel getViewModel() {
        return viewModel;
    }

    public ActivityCallDetailsBinding getBinding() {
        return binding;
    }
}
