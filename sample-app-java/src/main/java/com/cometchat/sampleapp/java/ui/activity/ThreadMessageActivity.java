package com.cometchat.sampleapp.java.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityThreadMessageBinding;
import com.cometchat.sampleapp.java.utils.AppConstants;
import com.cometchat.sampleapp.java.viewmodels.ThreadMessageViewModel;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

public class ThreadMessageActivity extends AppCompatActivity {
    private ActivityThreadMessageBinding binding;
    private User user;
    private Group group;
    private BaseMessage goToMessage;
    private boolean isBlockedByMe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = ActivityThreadMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpTheme();
        adjustWindowSettings();
        applyWindowInsets();

        ThreadMessageViewModel viewModel = new ViewModelProvider.NewInstanceFactory().create(ThreadMessageViewModel.class);
        String goToMessageJson = getIntent().getStringExtra(getString(R.string.app_go_to_message));
        String rawMessage = getIntent().getStringExtra(AppConstants.JSONConstants.RAW_JSON);
        int replyCount = getIntent().getIntExtra(AppConstants.JSONConstants.REPLY_COUNT, 0);
        String userJson = getIntent().getStringExtra(getString(R.string.app_user));

        try {
            isBlockedByMe = getIntent().getBooleanExtra("isBlockedByMe", false);
            if (goToMessageJson != null) {
                goToMessage = BaseMessage.processMessage(new JSONObject(goToMessageJson));
            }
            if (rawMessage != null) {
                BaseMessage parentMessage = BaseMessage.processMessage(new JSONObject(rawMessage));
                parentMessage.setReplyCount(replyCount);
                viewModel.setParentMessage(parentMessage);
            }
            if (userJson != null) {
                user = User.fromJson(userJson);
            }
        } catch (JSONException e) {
            CometChatLogger.e(ThreadMessageActivity.class.getSimpleName(), e.getMessage());
        }

        viewModel.addUserListener();
        viewModel.getParentMessage().observe(this, this::setParentMessage);
        viewModel.getUserBlockStatus().observe(this, this::setUserBlockedStatus);
        viewModel.getUnblockButtonState().observe(this, this::setUnblockButtonState);

        if (user != null) {
            viewModel.setUser(user);
        }

        binding.unblockBtn.setOnClickListener(view -> viewModel.unblockUser());
        initClickListeners();
        setupUI();
    }

    private void setUpTheme() {
        binding.backIcon.setColorFilter(CometChatTheme.getIconTintPrimary(this));
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.tvSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.unblockText.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.unblockBtn.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(this));
        binding.unblockBtn.setStrokeColor(CometChatTheme.getStrokeColorDark(this));
        binding.progress.setIndeterminateTintList(ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this)));
    }

    private void adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.parentView, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                Insets nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                int bottomInset = Math.max(ime.bottom, nav.bottom);
                boolean isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

                if (binding.singleLineComposer.isFocused() && isImeVisible) {
                    if (binding.messageList.atBottom()) {
                        binding.messageList.scrollToBottom();
                    }
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

    private void setParentMessage(BaseMessage parentMessage) {
        if (UIKitConstants.ReceiverType.USER.equalsIgnoreCase(parentMessage.getReceiverType())) {
            user = parentMessage
                .getSender()
                .getUid()
                .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) ? (User) parentMessage.getReceiver() : parentMessage.getSender();
        } else if (UIKitConstants.ReceiverType.GROUP.equalsIgnoreCase(parentMessage.getReceiverType())) {
            group = (Group) parentMessage.getReceiver();
        }

        if (goToMessage != null) binding.messageList.gotoMessage(goToMessage.getId());
        binding.messageList.setParentMessage(parentMessage.getId());
        binding.singleLineComposer.setParentMessageId(parentMessage.getId());
        binding.threadHeader.setParentMessage(parentMessage);
        binding.threadHeader.setReactionVisibility(View.GONE);
        binding.tvSubtitle.setText(user != null ? user.getName() : group != null ? group.getName() : "");
        binding.tvSubtitle.setVisibility(binding.tvSubtitle.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);

        if (user != null) {
            binding.messageList.setUser(user);
            binding.singleLineComposer.setUser(user);
        } else if (group != null) {
            binding.messageList.setGroup(group);
            binding.singleLineComposer.setGroup(group);
        }
    }

    private void setUserBlockedStatus(User user) {
        if (this.user != null && this.user.getUid().equals(user.getUid())) {
            isBlockedByMe = user.isBlockedByMe();
            updateUserBlockStatus();
        }
    }

    private void initClickListeners() {
        binding.messageList.getMentionsFormatter().setOnMentionClick((context, user) -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra(context.getString(R.string.app_user), new Gson().toJson(user));
            context.startActivity(intent);
        });

        binding.threadHeader.getCometchatMentionsFormatter().setOnMentionClick((context, user) -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra(context.getString(R.string.app_user), new Gson().toJson(user));
            context.startActivity(intent);
        });
    }

    private void updateUserBlockStatus() {
        if (isBlockedByMe) {
            binding.singleLineComposer.setVisibility(View.GONE);
            binding.unblockLayout.setVisibility(View.VISIBLE);
        } else {
            binding.singleLineComposer.setVisibility(View.VISIBLE);
            binding.unblockLayout.setVisibility(View.GONE);
        }
    }

    private void setUnblockButtonState(UIKitConstants.DialogState dialogState) {
        if (dialogState == UIKitConstants.DialogState.INITIATED) {
            binding.unblockText.setVisibility(View.GONE);
            binding.progress.setVisibility(View.VISIBLE);
        } else if (dialogState == UIKitConstants.DialogState.SUCCESS || dialogState == UIKitConstants.DialogState.FAILURE) {
            binding.unblockText.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

    private void setupUI() {
        // Set up back button behavior
        binding.backIcon.setOnClickListener((v) -> {
            Utils.hideKeyBoard(this, binding.getRoot());
            finish();
        });

        // Get the screen height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // Calculate 25% of the screen height
        int requiredHeight = (int) (screenHeight * 0.35);
        binding.threadHeader.setMaxHeight(requiredHeight);

        updateUserBlockStatus();
    }
}
