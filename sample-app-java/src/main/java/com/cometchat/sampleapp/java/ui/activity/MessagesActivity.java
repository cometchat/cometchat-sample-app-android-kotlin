package com.cometchat.sampleapp.java.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityMessagesBinding;
import com.cometchat.sampleapp.java.databinding.OverflowMenuLayoutBinding;
import com.cometchat.sampleapp.java.utils.AppConstants;
import com.cometchat.sampleapp.java.utils.MyApplication;
import com.cometchat.sampleapp.java.viewmodels.MessagesViewModel;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = "MessagesActivity";

    private User user;
    private Group group;
    private BaseMessage baseMessage;
    private BaseMessage goToMessage;
    private MessagesViewModel viewModel;
    private ActivityMessagesBinding binding;

    private final ActivityResultLauncher<Intent> searchActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean shouldNavigateToDifferentChat = data.getBooleanExtra("navigateToDifferentChat", false);

                    if (shouldNavigateToDifferentChat) {
                        handleDifferentChatNavigation(data);
                    } else {
                        handleSameChatNavigation(data);
                    }
                }
            }
    );

    private void handleDifferentChatNavigation(Intent data) {
        String selectedUserJson = data.getStringExtra(getString(R.string.app_user));
        String selectedGroupJson = data.getStringExtra(getString(R.string.app_group));
        String goToMessageJson = data.getStringExtra(getString(R.string.app_go_to_message));
        String parentMessageJson = data.getStringExtra(getString(R.string.app_base_message));

        Intent intent;
        if (parentMessageJson != null) {
            intent = new Intent(this, ThreadMessageActivity.class);
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessageJson);
            if (selectedUserJson != null) {
                intent.putExtra(getString(R.string.app_user), selectedUserJson);
            }
            if (selectedGroupJson != null) {
                intent.putExtra(getString(R.string.app_group), selectedGroupJson);
            }
        } else {
            intent = new Intent(this, MessagesActivity.class);
            if (selectedUserJson != null) {
                intent.putExtra(getString(R.string.app_user), selectedUserJson);
            }
            if (selectedGroupJson != null) {
                intent.putExtra(getString(R.string.app_group), selectedGroupJson);
            }
            if (goToMessageJson != null) {
                intent.putExtra(getString(R.string.app_go_to_message), goToMessageJson);
            }
        }
        startActivity(intent);
        finish();
    }

    private void handleSameChatNavigation(Intent data) {
        String goToMessageJson = data.getStringExtra(getString(R.string.app_go_to_message));
        if (goToMessageJson != null) {
            try {
                this.goToMessage = BaseMessage.processMessage(new JSONObject(goToMessageJson));
                addViews();
            } catch (JSONException e) {
                CometChatLogger.e(TAG, "Error processing goto message: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        adjustWindowSettings();
        applyWindowInsets();
        setUpTheme();
        initViewModel();

        MyApplication.currentOpenChatId = group != null ? group.getGuid() : user != null ? user.getUid() : null;

        addViews();
        initClickListeners();

        if (!Utils.isAgentChat(user)) {
            setUpMessageHeaderMenu();
        }
    }

    private void extractIntentData() {
        group = new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_group)), Group.class);
        try {
            String rawGoToMessage = getIntent().getStringExtra(getString(R.string.app_go_to_message));
            String userJson = getIntent().getStringExtra(getString(R.string.app_user));

            if (rawGoToMessage != null) {
                goToMessage = BaseMessage.processMessage(new JSONObject(rawGoToMessage));
            }

            if (userJson != null) {
                user = User.fromJson(userJson);
            }
        } catch (JSONException e) {
            CometChatLogger.e(TAG, e.getMessage());
        }

    }

    private void initClickListeners() {
        binding.unblockBtn.setOnClickListener(view -> viewModel.unblockUser());

        binding.messageList.getMentionsFormatter().setOnMentionClick((context, user) -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra(context.getString(R.string.app_user), new Gson().toJson(user));
            context.startActivity(intent);
        });

        binding.messageList.setOnThreadRepliesClick((context, baseMessage, cometchatMessageTemplate) -> {
            Intent intent = new Intent(context, ThreadMessageActivity.class);
            if (user != null) {
                intent.putExtra(context.getString(R.string.app_user), user.toJson().toString());
                intent.putExtra("isBlockedByMe", user.isBlockedByMe());
            }
            if (group != null) {
                intent.putExtra(context.getString(R.string.app_group), new Gson().toJson(group));
            }
            intent.putExtra(AppConstants.JSONConstants.REPLY_COUNT, baseMessage.getReplyCount());
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, baseMessage.getRawMessage().toString());
            context.startActivity(intent);
        });
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(MessagesViewModel.class);
        viewModel.setUser(user);
        viewModel.setGroup(group);

        viewModel.addListener();
        viewModel.getUpdatedGroup().observe(this, this::updateGroupJoinedStatus);
        viewModel.getBaseMessage().observe(this, this::setBaseMessage);
        viewModel.getUpdateUser().observe(this, this::updateUserBlockStatus);
        viewModel.openUserChat().observe(this, this::openUserChat);
        viewModel.getIsExitActivity().observe(this, this::exitActivity);
        viewModel.getUnblockButtonState().observe(this, this::setUnblockButtonState);
    }

    private void setUpMessageHeaderMenu() {
        List<CometChatPopupMenu.MenuItem> options = getHeaderMenuOptions();
        binding.messageHeader.setOptions(options);
        binding.messageHeader.setPopupMenuStyle(R.style.CustomHeaderPopUpMenuStyle);
    }

    private List<CometChatPopupMenu.MenuItem> getHeaderMenuOptions() {
        List<CometChatPopupMenu.MenuItem> options = new ArrayList<>();

        options.add(new CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.SEARCH,
                getString(com.cometchat.chatuikit.R.string.cometchat_menu_search),
                AppCompatResources.getDrawable(this, com.cometchat.chatuikit.R.drawable.cometchat_ic_search),
                null,
                this::navigateToSearchActivity
        ));

        options.add(new CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.CONVERSATION_SUMMARY,
                getString(com.cometchat.chatuikit.R.string.cometchat_menu_conversation_summary),
                AppCompatResources.getDrawable(this, com.cometchat.chatuikit.R.drawable.cometchat_ic_menu_conversation_summary),
                null,
                this::generateConversationSummary
        ));

        options.add(new CometChatPopupMenu.MenuItem(
                UIKitConstants.MessageHeaderMenuOptions.DETAILS,
                getString(com.cometchat.chatuikit.R.string.cometchat_details),
                AppCompatResources.getDrawable(this, R.drawable.ic_info),
                null,
                this::openDetailScreen
        ));

        return options;
    }

    private void generateConversationSummary() {
        binding.messageList.generateConversationSummary();
    }

    private void navigateToSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        if (user != null) {
            intent.putExtra(getString(R.string.app_user), user.toJson().toString());
        } else {
            intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
        }
        searchActivityLauncher.launch(intent);
    }


    /**
     * Sets the window settings for the activity.
     */

    private void adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    /**
     * Applies window insets to the parent view to handle system UI visibility.
     */

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent_view), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                Insets nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                boolean isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                int bottomInset = Math.max(ime.bottom, nav.bottom);

                if (isImeVisible && binding.singleLineComposer.isFocused() && binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom();
                }

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        bottomInset
                );

                return insets;
            }
        });
    }

    private void setUpTheme() {
        binding.parentView.setBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.infoLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.separator.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
        binding.infoText.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.unblockTitle.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this));
        binding.unblockBtn.setStrokeColor(CometChatTheme.getStrokeColorDark(this));
        binding.progress.setIndeterminateTintList(ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this)));
    }

    /**
     * Updates the UI based on the group's joined status.
     *
     * @param group The updated group object.
     */
    private void updateGroupJoinedStatus(Group group) {
        if (!group.isJoined()) {
            binding.unblockBtn.setVisibility(View.GONE);
            binding.singleLineComposer.setVisibility(View.GONE);
            binding.infoLayout.setVisibility(View.VISIBLE);
        } else {
            binding.unblockBtn.setVisibility(View.GONE);
            binding.singleLineComposer.setVisibility(View.VISIBLE);
            binding.infoLayout.setVisibility(View.GONE);
        }
    }

    private void setBaseMessage(BaseMessage baseMessage) {
        this.baseMessage = baseMessage;
    }

    /**
     * Updates the UI based on the user's block status.
     *
     * @param user The updated user object.
     */
    private void updateUserBlockStatus(User user) {
        if (user.isBlockedByMe()) {
            binding.singleLineComposer.setVisibility(View.GONE);
            binding.unblockLayout.setVisibility(View.VISIBLE);
        } else {
            binding.singleLineComposer.setVisibility(View.VISIBLE);
            binding.unblockLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Opens the chat interface for the specified user.
     *
     * @param user The user object representing the chat participant. Must not be null.
     */
    private void openUserChat(User user) {
        if (user != null) {
            Intent intent = new Intent(this, MessagesActivity.class);
            intent.putExtra(getString(R.string.app_user), new Gson().toJson(user));
            startActivity(intent);
        }
    }

    /**
     * Exits the activity if the exit flag is true.
     *
     * @param exit Indicates whether to exit the activity.
     */
    private void exitActivity(boolean exit) {
        if (exit) {
            finish();
        }
    }

    /**
     * Updates the visibility of the unblock button based on the dialog state.
     *
     * @param dialogState The current state of the unblock dialog.
     */
    private void setUnblockButtonState(UIKitConstants.DialogState dialogState) {
        if (dialogState == UIKitConstants.DialogState.INITIATED) {
            binding.unblockText.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        } else if (dialogState == UIKitConstants.DialogState.SUCCESS || dialogState == UIKitConstants.DialogState.FAILURE) {
            binding.unblockText.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

    /**
     * Initializes UI components and sets up the keyboard visibility listener.
     */
    private void addViews() {
        if (goToMessage != null) {
            binding.messageList.gotoMessage(goToMessage.getId());
        }
        if (user != null) {
            binding.messageHeader.setUser(user);
            binding.messageList.setUser(user);
            binding.singleLineComposer.setUser(user);
            updateUserBlockStatus(user);
        } else if (group != null) {
            binding.messageHeader.setGroup(group);
            binding.messageList.setGroup(group);
            binding.singleLineComposer.setGroup(group);
            updateGroupJoinedStatus(group);
        }

        binding.messageList.setStartFromUnreadMessages(true);
        binding.messageList.setMarkAsUnreadOptionVisibility(View.VISIBLE);

        binding.messageHeader.setOnBackButtonPressed(() -> {
            Utils.hideKeyBoard(this, binding.getRoot());
            finish();
        });
    }

    /**
     * Configures the overflow menu for additional actions.
     */
    private void setOverFlowMenu() {
        binding.messageHeader.setTrailingView((context, user, group) -> {
            LinearLayout linearLayout = new LinearLayout(context);

            OverflowMenuLayoutBinding overflowMenuLayoutBinding = OverflowMenuLayoutBinding.inflate(getLayoutInflater());
            overflowMenuLayoutBinding.ivMenu.setImageResource(R.drawable.ic_info);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);

            if ((group != null && group.isJoined()) || (user != null && !Utils.isBlocked(user))) {
                linearLayout.addView(overflowMenuLayoutBinding.getRoot());
            }

            overflowMenuLayoutBinding.ivMenu.setOnClickListener(view1 -> openDetailScreen());
            return linearLayout;
        });
    }

    /**
     * Opens the detail screen for the selected user or group.
     */
    private void openDetailScreen() {
        Intent intent = null;
        if (user != null) {
            intent = new Intent(this, UserDetailsActivity.class);
            intent.putExtra(getString(R.string.app_user), new Gson().toJson(user));
            intent.putExtra(getString(R.string.app_base_message), new Gson().toJson(binding.messageList.getViewModel().getLastMessage()));
        } else if (group != null) {
            intent = new Intent(this, GroupDetailsActivity.class);
            intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
            intent.putExtra(getString(R.string.app_base_message), new Gson().toJson(binding.messageList.getViewModel().getLastMessage()));
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener from the ViewModel to prevent memory leaks
        viewModel.removeListener();
        MyApplication.currentOpenChatId = null;
    }
}