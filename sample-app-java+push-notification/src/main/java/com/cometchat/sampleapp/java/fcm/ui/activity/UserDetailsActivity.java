package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.calls.CometChatCallActivity;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.databinding.ActivityUserDetailsBinding;
import com.cometchat.sampleapp.java.fcm.utils.AppUtils;
import com.cometchat.sampleapp.java.fcm.viewmodels.UserDetailsViewModel;
import com.google.gson.Gson;

public class UserDetailsActivity extends AppCompatActivity {

    private ActivityUserDetailsBinding binding;
    private CometChatConfirmDialog alertDialog;
    private UserDetailsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViewModel();

        initClickListeners();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(UserDetailsViewModel.class);
        viewModel.addListeners();
        viewModel.setUser(new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_user)), User.class));
        viewModel.setBaseMessage(new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_base_message)), BaseMessage.class));
        viewModel.getUser().observe(this, this::setUserHeader);
        viewModel.isUserBlockedByMe().observe(this, isUserBlockedByMe());
        viewModel.isUserBlocked().observe(this, blockUserStateObserver());
        viewModel.isUserUnblocked().observe(this, unblockUserStateObserver());
        viewModel.isChatDeleted().observe(this, daleteChatStateObserver());
        viewModel.onCallStart().observe(this, onCallStart());
        viewModel.onCallStartError().observe(this, onCallStartError());
    }

    private void initClickListeners() {
        binding.ivBack.setOnClickListener(v -> finish());

        binding.tvBlock.setOnClickListener(v -> {
            if (binding.tvBlock.getText().toString().equals(getString(R.string.app_block))) {
                blockUser();
            } else {
                unblockUser();
            }
        });

        if (viewModel.getBaseMessage().getValue() == null)
            binding.tvDeleteChat.setVisibility(View.GONE);

        binding.tvDeleteChat.setOnClickListener(v -> deleteChat());

        binding.cardVoiceCall.setOnClickListener(v -> viewModel.startCall(CometChatConstants.CALL_TYPE_AUDIO));

        binding.cardVideoCall.setOnClickListener(v -> viewModel.startCall(CometChatConstants.CALL_TYPE_VIDEO));
    }

    private void setUserHeader(User user) {
        binding.avatar.setAvatar(user.getName(), user.getAvatar());
        binding.tvTitle.setText(user.getName());
        if (!Utils.isBlocked(user)) {
            binding.infoMessage.setVisibility(View.GONE);
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.cardVideoCall.setVisibility(View.VISIBLE);
            binding.cardVoiceCall.setVisibility(View.VISIBLE);
            if (user.getStatus().equals(CometChatConstants.USER_STATUS_ONLINE)) {
                binding.tvSubtitle.setText(getResources().getString(com.cometchat.chatuikit.R.string.cometchat_online));
            } else {
                if (user.getLastActiveAt() == 0) {
                    binding.tvSubtitle.setText(getString(com.cometchat.chatuikit.R.string.cometchat_offline));
                } else {
                    String lastSeen = Utils.getLastSeenTime(this, user.getLastActiveAt());
                    binding.tvSubtitle.setText(lastSeen);
                    binding.tvSubtitle.setSelected(true);
                }
            }
        } else {
            binding.tvSubtitle.setVisibility(View.GONE);
            binding.cardVideoCall.setVisibility(View.GONE);
            binding.cardVoiceCall.setVisibility(View.GONE);
            if (user.isBlockedByMe()) {
                binding.infoMessage.setVisibility(View.VISIBLE);
                binding.tvInfoMessage.setText(String.format("%s %s", getString(R.string.app_you_have_blocked_this_user), user.getName()));
            } else {
                binding.infoMessage.setVisibility(View.VISIBLE);
                binding.tvInfoMessage.setText(String.format("%s %s", user.getName(), getString(R.string.app_has_blocked_you)));
            }
        }
    }

    @NonNull
    private Observer<Boolean> isUserBlockedByMe() {
        return isUserBlockedByMe -> {
            if (isUserBlockedByMe) {
                binding.tvBlock.setText(getString(R.string.app_unblock));
            } else {
                binding.tvBlock.setText(getString(R.string.app_block));
            }
        };
    }

    @NonNull
    private Observer<Boolean> blockUserStateObserver() {
        return isUserBlocked -> {
            if (isUserBlocked) {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_block_user_success), CometChatTheme.getColorBlack(this));
            } else {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_block_user_error), CometChatTheme.getErrorColor(this));
            }
        };
    }

    @NonNull
    private Observer<Boolean> unblockUserStateObserver() {
        return isUserUnblocked -> {
            if (isUserUnblocked) {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_unblock_user_success), CometChatTheme.getColorBlack(this));
            } else {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_unblock_user_error), CometChatTheme.getErrorColor(this));
            }
        };
    }

    @NonNull
    private Observer<Boolean> daleteChatStateObserver() {
        return isChatDeleted -> {
            if (isChatDeleted) {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_delete_chat_success), CometChatTheme.getColorBlack(this));
                finish();
            } else {
                alertDialog.dismiss();
                AppUtils.customToast(this, getString(R.string.app_delete_chat_error), CometChatTheme.getErrorColor(this));
            }
        };
    }

    @NonNull
    private Observer<Call> onCallStart() {
        return call -> {
            CometChatCallActivity.launchOutgoingCallScreen(this, call, null);
        };
    }

    @NonNull
    private Observer<String> onCallStartError() {
        return msg -> AppUtils.customToast(this, getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong), CometChatTheme.getErrorColor(this));
    }

    private void blockUser() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(getResources(), com.cometchat.chatuikit.R.drawable.cometchat_ic_block, null),
            CometChatTheme.getErrorColor(this),
            getString(R.string.app_block_user_title),
            getString(R.string.app_block_user_subtitle),
            getString(R.string.app_block_user_positive_button),
            getString(R.string.app_block_user_negative_button),
            view -> {
                alertDialog.hidePositiveButtonProgressBar(false);
                viewModel.blockUser();
            },
            view -> alertDialog.dismiss(),
            0,
            false
        );
    }

    private void unblockUser() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(getResources(), com.cometchat.chatuikit.R.drawable.cometchat_ic_block, null),
            CometChatTheme.getErrorColor(this),
            getString(R.string.app_unblock_user_title),
            getString(R.string.app_unblock_user_subtitle),
            getString(R.string.app_unblock_user_positive_button),
            getString(R.string.app_unblock_user_negative_button),
            view -> {
                alertDialog.hidePositiveButtonProgressBar(false);
                viewModel.unblockUser();
            },
            view -> alertDialog.dismiss(),
            0,
            false
        );
    }

    private void deleteChat() {
        showCometChatConfirmDialog(
            R.style.ConfirmationDialogStyle,
            ResourcesCompat.getDrawable(getResources(), com.cometchat.chatuikit.R.drawable.cometchat_ic_delete, null),
            CometChatTheme.getErrorColor(this),
            getString(R.string.app_delete_chat_title),
            getString(R.string.app_delete_chat_subtitle),
            getString(R.string.app_delete_chat_positive_button),
            getString(R.string.app_delete_chat_negative_button),
            view -> {
                alertDialog.hidePositiveButtonProgressBar(false);
                viewModel.deleteChat();
            },
            view -> alertDialog.dismiss(),
            0,
            false
        );
    }

    private void showCometChatConfirmDialog(
        @StyleRes int style,
        Drawable icon,
        @ColorInt int iconTint,
        String title,
        String subtitle,
        String positiveButtonText,
        String negativeButtonText,
        View.OnClickListener onPositiveButtonClick,
        View.OnClickListener onNegativeButtonClick,
        int elevation,
        boolean cancelable
    ) {
        alertDialog = new CometChatConfirmDialog(this, style);
        alertDialog.setConfirmDialogIcon(icon);
        alertDialog.setConfirmDialogIconTint(iconTint);
        alertDialog.setTitleText(title);
        alertDialog.setSubtitleText(subtitle);
        alertDialog.setPositiveButtonText(positiveButtonText);
        alertDialog.setNegativeButtonText(negativeButtonText);
        alertDialog.setOnPositiveButtonClick(onPositiveButtonClick);
        alertDialog.setOnNegativeButtonClick(onNegativeButtonClick);
        alertDialog.setConfirmDialogElevation(elevation);
        alertDialog.setCancelable(cancelable);
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.removeListeners();
    }
}
