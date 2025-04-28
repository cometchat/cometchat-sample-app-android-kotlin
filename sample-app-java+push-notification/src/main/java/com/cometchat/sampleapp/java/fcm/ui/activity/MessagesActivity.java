package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.keyboard_utils.KeyBoardUtils;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.databinding.ActivityMessagesBinding;
import com.cometchat.sampleapp.java.fcm.databinding.OverflowMenuLayoutBinding;
import com.cometchat.sampleapp.java.fcm.utils.MyApplication;
import com.cometchat.sampleapp.java.fcm.viewmodels.MessagesViewModel;
import com.google.gson.Gson;

public class MessagesActivity extends AppCompatActivity {

    private User user;
    private Group group;
    private BaseMessage baseMessage;
    private MessagesViewModel viewModel;
    private ActivityMessagesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create an instance of the MessagesViewModel
        viewModel = new ViewModelProvider.NewInstanceFactory().create(MessagesViewModel.class);

        // Deserialize the user and group data from the Intent
        user = new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_user)), User.class);
        group = new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_group)), Group.class);
        MyApplication.currentOpenChatId = group != null ? group.getGuid() : user != null ? user.getUid() : null;

        // Set the user and group in the ViewModel
        viewModel.setUser(user);
        viewModel.setGroup(group);

        // Add listeners for ViewModel updates
        viewModel.addListener();
        viewModel.getUpdatedGroup().observe(this, this::updateGroupJoinedStatus);
        viewModel.getBaseMessage().observe(this, this::setBaseMessage);
        viewModel.getUpdateUser().observe(this, this::updateUserBlockStatus);
        viewModel.openUserChat().observe(this, this::openUserChat);
        viewModel.getIsExitActivity().observe(this, this::exitActivity);
        viewModel.getUnblockButtonState().observe(this, this::setUnblockButtonState);

        // Initialize UI components
        addViews();
        setOverFlowMenu();

        // Set click listener for the unblock button
        binding.unblockBtn.setOnClickListener(view -> viewModel.unblockUser());

        binding.messageList.getMentionsFormatter().setOnMentionClick((context, user) -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra(context.getString(R.string.app_user), new Gson().toJson(user));
            context.startActivity(intent);
        });

        binding.messageList.setOnThreadRepliesClick((context, baseMessage, cometchatMessageTemplate) -> {
            Intent intent = new Intent(context, ThreadMessageActivity.class);
            intent.putExtra(getString(R.string.app_message_id), baseMessage.getId());
            context.startActivity(intent);
        });

    }

    /**
     * Updates the UI based on the group's joined status.
     *
     * @param group The updated group object.
     */
    private void updateGroupJoinedStatus(Group group) {
        if (!group.isJoined()) {
            binding.unblockBtn.setVisibility(View.GONE);
            binding.messageComposer.setVisibility(View.GONE);
            binding.infoLayout.setVisibility(View.VISIBLE);
        } else {
            binding.unblockBtn.setVisibility(View.GONE);
            binding.messageComposer.setVisibility(View.VISIBLE);
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
            binding.messageComposer.setVisibility(View.GONE);
            binding.unblockLayout.setVisibility(View.VISIBLE);
        } else {
            binding.messageComposer.setVisibility(View.VISIBLE);
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
        KeyBoardUtils.setKeyboardVisibilityListener(this, binding.getRoot(), keyboardVisible -> {
            if (binding.messageComposer.getMessageInput().getComposeBox().isFocused() && keyboardVisible) {
                if (binding.messageList.atBottom()) {
                    binding.messageList.scrollToBottom();
                }
            }
        });

        // Set user or group data to the message header and composer
        if (user != null) {
            binding.messageHeader.setUser(user);
            binding.messageList.setUser(user);
            binding.messageComposer.setUser(user);
            updateUserBlockStatus(user);
        } else if (group != null) {
            binding.messageHeader.setGroup(group);
            binding.messageList.setGroup(group);
            binding.messageComposer.setGroup(group);
            updateGroupJoinedStatus(group);
        }

        // Set up back button behavior
        binding.messageHeader.setOnBackButtonPressed(() -> {
            Utils.hideKeyBoard(this, binding.getRoot());
            finish();
        });
    }

    /**
     * Configures the overflow menu for additional actions.
     */
    private void setOverFlowMenu() {
        binding.messageHeader.setAuxiliaryButtonView((context, user, group) -> {
            LinearLayout linearLayout = new LinearLayout(context);
            View view = ChatConfigurator.getDataSource().getAuxiliaryHeaderMenu(context, user, group, binding.messageHeader.getAdditionParameter());

            OverflowMenuLayoutBinding overflowMenuLayoutBinding = OverflowMenuLayoutBinding.inflate(getLayoutInflater());
            overflowMenuLayoutBinding.ivMenu.setImageResource(R.drawable.ic_info);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);

            if ((group != null && group.isJoined()) || (user != null && !Utils.isBlocked(user))) {
                if (view != null) {
                    linearLayout.addView(view);
                }
                linearLayout.addView(overflowMenuLayoutBinding.getRoot());
            }

            overflowMenuLayoutBinding.ivMenu.setOnClickListener(view1 -> openDetailScreen(group));
            return linearLayout;
        });
    }

    /**
     * Opens the detail screen for the selected user or group.
     */
    private void openDetailScreen(Group group) {
        Intent intent = null;
        if (user != null) {
            intent = new Intent(this, UserDetailsActivity.class);
            intent.putExtra(getString(R.string.app_user), new Gson().toJson(user));
            intent.putExtra(getString(R.string.app_base_message), new Gson().toJson(binding.messageList.getViewModel().getLastMessage()));
        } else if (group != null) {
            intent = new Intent(this, GroupDetailsActivity.class);
            intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
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
