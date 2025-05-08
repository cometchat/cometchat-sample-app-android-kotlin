package com.cometchat.sampleapp.java.ui.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.keyboard_utils.KeyBoardUtils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityThreadMessageBinding;
import com.cometchat.sampleapp.java.viewmodels.ThreadMessageViewModel;

public class ThreadMessageActivity extends AppCompatActivity {
    private ActivityThreadMessageBinding binding;
    private User user;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = ActivityThreadMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create an instance of the MessagesViewModel
        ThreadMessageViewModel viewModel = new ViewModelProvider.NewInstanceFactory().create(ThreadMessageViewModel.class);
        viewModel.fetchMessageDetails(getIntent().getIntExtra(getString(R.string.app_message_id), -1));

        viewModel.addUserListener();
        viewModel.getParentMessage().observe(this, this::setParentMessage);
        viewModel.getUserBlockStatus().observe(this, this::updateUserBlockStatus);
        viewModel.getUnblockButtonState().observe(this, this::setUnblockButtonState);
        viewModel.setUser(user);

        binding.unblockBtn.setOnClickListener(view -> viewModel.unblockUser());

        setupUI();
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

        KeyBoardUtils.setKeyboardVisibilityListener(this, binding.getRoot(), keyboardVisible -> {
            if (binding.messageComposer.getMessageInput().getComposeBox().isFocused() && keyboardVisible) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom();
                }
            }
        });

        binding.messageList.setParentMessage(parentMessage.getId());
        binding.messageComposer.setParentMessageId(parentMessage.getId());
        binding.threadHeader.setParentMessage(parentMessage);
        binding.tvSubtitle.setText(user != null ? user.getName() : group != null ? group.getName() : "");
        binding.tvSubtitle.setVisibility(binding.tvSubtitle.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);
        // Set user or group data to the message header and composer
        if (user != null) {
            binding.messageList.setUser(user);
            binding.messageComposer.setUser(user);
            updateUserBlockStatus(user);
        } else if (group != null) {
            binding.messageList.setGroup(group);
            binding.messageComposer.setGroup(group);
        }
    }

    private void updateUserBlockStatus(User user) {
        if (user.isBlockedByMe()) {
            binding.messageComposer.setVisibility(View.GONE);
            binding.unblockLayout.setVisibility(View.VISIBLE);
        } else {
            binding.messageComposer.setVisibility(View.VISIBLE);
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
}
