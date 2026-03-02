package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.interfaces.OnItemClickListener;
import com.cometchat.sampleapp.java.fcm.databinding.ActivityHomeBinding;
import com.cometchat.sampleapp.java.fcm.fcm.FCMMessageDTO;
import com.cometchat.sampleapp.java.fcm.ui.fragments.CallsFragment;
import com.cometchat.sampleapp.java.fcm.ui.fragments.ChatsFragment;
import com.cometchat.sampleapp.java.fcm.ui.fragments.GroupsFragment;
import com.cometchat.sampleapp.java.fcm.ui.fragments.UsersFragment;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;
import com.cometchat.sampleapp.java.fcm.utils.AppUtils;
import com.cometchat.sampleapp.java.fcm.voip.CometChatVoIP;
import com.cometchat.sampleapp.java.fcm.voip.interfaces.VoIPPermissionListener;
import com.cometchat.sampleapp.java.fcm.voip.model.CometChatVoIPError;
import com.cometchat.sampleapp.java.fcm.voip.utils.CometChatVoIPConstant;
import com.google.gson.Gson;
import me.leolin.shortcutbadger.ShortcutBadger;

public class HomeActivity extends AppCompatActivity implements OnItemClickListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String SELECTED_FRAGMENT_KEY = "selected_fragment";
    private ActivityHomeBinding binding;
    private int currentFragment = R.id.nav_chats; // Default to the Chats fragment

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_FRAGMENT_KEY, currentFragment); // Save the selected fragment ID
    }

    @Override
    public void onItemClick() {
        Intent intent = new Intent(this, NewChatActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Restore the current fragment if available, otherwise load the default
        // (ChatsFragment)
        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY, R.id.nav_chats);
        }

        // Set the selected item in the bottom navigation to match the current fragment
        binding.bottomNavigationView.setSelectedItemId(currentFragment);

        AppUtils.requestNotificationPermission(this);

        configureBottomNavigation();
        configureVoIP();
        handleDeepLinking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShortcutBadger.removeCount(this);
    }

    /**
     * Configures the bottom navigation view and its item selection listener.
     * Updates the displayed fragment based on user selection.
     */
    /**
     * Configures the bottom navigation view and its item selection listener.
     * Updates the displayed fragment based on user selection.
     */
    private void configureBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (currentFragment == item.getItemId()) {
                return true; // No action needed if the fragment is already selected
            }
            currentFragment = item.getItemId();
            loadFragment(getFragment(currentFragment));
            return true;
        });

        // Create a ColorStateList for icon and text color based on the checked state
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{CometChatTheme.getIconTintHighlight(this), CometChatTheme.getIconTintSecondary(this)}
        );

        binding.bottomNavigationView.setItemIconTintList(colorStateList);
        binding.bottomNavigationView.setItemTextColor(colorStateList);
    }

    private void configureVoIP() {
        CometChatVoIP.init(this, getApplicationInfo().loadLabel(getPackageManager()).toString());
        launchVoIP();
    }

    private void handleDeepLinking() {
        String notificationType = getIntent().getStringExtra(AppConstants.FCMConstants.NOTIFICATION_TYPE);
        String notificationPayload = getIntent().getStringExtra(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD);
        if (notificationType == null || notificationPayload == null) {
            loadFragment(getFragment(currentFragment));
        } else {
            if (AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE.equals(notificationType)) {
                FCMMessageDTO fcmMessageDTO = new Gson().fromJson(notificationPayload, FCMMessageDTO.class);
                if (fcmMessageDTO != null) {
                    if ("chat".equalsIgnoreCase(fcmMessageDTO.getType())) {
                        Fragment chatFragment = getFragment(currentFragment);
                        Bundle args = new Bundle();
                        args.putString(AppConstants.FCMConstants.NOTIFICATION_TYPE, notificationType);
                        args.putString(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD, notificationPayload);
                        chatFragment.setArguments(args);
                        loadFragment(chatFragment);
                        currentFragment = R.id.nav_chats;
                        binding.bottomNavigationView.setSelectedItemId(currentFragment);
                    }
                }
            }
        }
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to be loaded.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    /**
     * Returns the appropriate fragment based on the selected menu itemId.
     *
     * @param itemId The selected menu itemId ID.
     * @return The corresponding fragment, or null if no match is found.
     */
    private static Fragment getFragment(int itemId) {
        Fragment selectedFragment;
        if (itemId == R.id.nav_chats) {
            selectedFragment = new ChatsFragment();
        } else if (itemId == R.id.nav_calls) {
            selectedFragment = new CallsFragment();
        } else if (itemId == R.id.nav_users) {
            selectedFragment = new UsersFragment();
        } else if (itemId == R.id.nav_groups) {
            selectedFragment = new GroupsFragment();
        } else {
            selectedFragment = new ChatsFragment();
        }
        return selectedFragment;
    }

    private void launchVoIP() {
        if (!CometChatVoIP.hasReadPhoneStatePermission(this)) {
            CometChatVoIP.requestReadPhoneStatePermission(this, CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE);
            return;
        }

        if (!CometChatVoIP.hasManageOwnCallsPermission(this)) {
            CometChatVoIP.requestManageOwnCallsPermission(this, CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS);
            return;
        }

        if (!CometChatVoIP.hasAnswerPhoneCallsPermission(this)) {
            CometChatVoIP.requestAnswerPhoneCallsPermission(this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS);
            return;
        }

        if (CometChatVoIP.hasReadPhoneStatePermission(this) && CometChatVoIP.hasManageOwnCallsPermission(this) && CometChatVoIP.hasAnswerPhoneCallsPermission(
            this)) {
            CometChatVoIP.hasEnabledPhoneAccountForVoIP(this, new VoIPPermissionListener() {
                @Override
                public void onPermissionsGranted() {
                    CometChatLogger.e(TAG, "VoIP Permissions granted");
                }

                @Override
                public void onPermissionsDenied(CometChatVoIPError error) {
                    CometChatLogger.e(TAG, "VoIP Permissions denied: " + error.getMessage());
                    CometChatVoIP.alertDialogForVoIP(HomeActivity.this);
                }
            });
        } else {
            CometChatLogger.e(TAG, "All VoIP Permissions denied.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppUtils.PushNotificationPermissionCode:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CometChatLogger.e(TAG, "Push Notification permission granted.");
                    CometChatVoIP.requestPhoneStatePermissions(this, CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE);
                } else {
                    CometChatLogger.e(TAG, "Push Notification permission denied.");
                }
                break;
            case CometChatVoIPConstant.PermissionCode.READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CometChatLogger.e(TAG, "Read Phone State permission granted.");
                    if (CometChatVoIP.hasManageOwnCallsPermission(this)) {
                        CometChatVoIP.requestAnswerPhoneCallsPermissions(this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS);
                    } else {
                        CometChatVoIP.requestManageOwnCallsPermissions(this, CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS);
                    }
                } else {
                    CometChatLogger.e(TAG, "Read Phone State permission denied.");
                }
                break;
            case CometChatVoIPConstant.PermissionCode.MANAGE_OWN_CALLS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CometChatLogger.e(TAG, "Manage Own Calls permission granted.");
                    CometChatVoIP.requestAnswerPhoneCallsPermissions(this, CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS);
                } else {
                    CometChatLogger.e(TAG, "Manage Own Calls permission denied.");
                }
                break;
            case CometChatVoIPConstant.PermissionCode.ANSWER_PHONE_CALLS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CometChatLogger.e(TAG, "Answer Phone Calls permission granted.");
                    launchVoIP();
                } else {
                    CometChatLogger.e(TAG, "Answer Phone Calls permission denied.");
                }
                break;
        }
    }
}
