package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.enums.GroupAction;
import com.cometchat.sampleapp.java.fcm.databinding.ActivityGroupDetailsBinding;
import com.cometchat.sampleapp.java.fcm.databinding.AddMembersLayoutBinding;
import com.cometchat.sampleapp.java.fcm.databinding.BannedMembersLayoutBinding;
import com.cometchat.sampleapp.java.fcm.databinding.GroupMembersLayoutBinding;
import com.cometchat.sampleapp.java.fcm.databinding.TransferOwnershipLayoutBinding;
import com.cometchat.sampleapp.java.fcm.viewmodels.GroupDetailsViewModel;
import com.google.gson.Gson;

import java.util.Objects;

public class GroupDetailsActivity extends AppCompatActivity {
    private Group group;
    private BaseMessage baseMessage;
    private ActivityGroupDetailsBinding binding;
    private AddMembersLayoutBinding addMembersLayoutBinding;

    private Dialog dialog;
    private AlertDialog.Builder alertDialog;

    private GroupDetailsViewModel viewModel;

    private TextView tvError;
    private ProgressBar progressBar;
    private TextView btnText;
    private CometChatConfirmDialog confirmDialog;

    private GroupMember groupMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyWindowInsets(binding.main);
        adjustWindowSettings();

        group = new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_group)), Group.class);
        baseMessage = new Gson().fromJson(getIntent().getStringExtra(getString(R.string.app_base_message)), BaseMessage.class);
        initViewModel();

        setHeaderData(group);
        if (dialog != null) dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Adjusts the window settings based on the Android version.
     * For Android R and above, sets decor fits system windows to true.
     * For below Android R, sets soft input mode to adjust resize.
     */

    private void adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    /**
     * Applies window insets to the view.
     *
     * @param view The view to apply window insets to.
     */

    private void applyWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
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

    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(GroupDetailsViewModel.class);
        viewModel.getDialogState().observe(this, this::setDialogState);
        viewModel.getBaseMessage().observe(this, this::setDeleteChatVisibility);
        viewModel.getErrorMessage().observe(this, this::setError);
        viewModel.getConfirmDialogState().observe(this, this::setConfirmDialogStateObserver);
        viewModel.getUpdatedGroup().observe(this, this::setHeaderData);
        viewModel.getTransferOwnershipDialogState().observe(this, this::setTransferOwnershipDialogStateObserver);
        viewModel.setGroup(group);
        viewModel.setBaseMessage(baseMessage);
        viewModel.addListeners();
    }

    private void setDeleteChatVisibility(BaseMessage baseMessage) {
        binding.tvDeleteChat.setVisibility(baseMessage != null ? View.VISIBLE : View.GONE);
    }

    private void setHeaderData(Group group) {
        binding.avatar.setAvatar(group.getName(), group.getIcon());
        binding.tvGroupName.setText(group.getName());
        binding.tvMemberCount.setText(group.getMembersCount() > 1 ? group.getMembersCount() + " " + getResources().getString(com.cometchat.chatuikit.R.string.cometchat_members) : group.getMembersCount() + " " + getResources().getString(
            com.cometchat.chatuikit.R.string.cometchat_member));

        binding.main.setBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.ivBack.setImageTintList(ColorStateList.valueOf(CometChatTheme.getIconTintPrimary(this)));
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.viewSeparator.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
        binding.infoMessage.setBackgroundColor(CometChatTheme.getWarningColor(this));
        binding.ivInfo.setImageTintList(ColorStateList.valueOf(CometChatTheme.getTextColorPrimary(this)));
        binding.tvInfoMessage.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.tvGroupName.setTextColor(CometChatTheme.getTextColorPrimary(this));
        binding.tvMemberCount.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.viewMembersCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.viewMembersCard.setStrokeColor(CometChatTheme.getStrokeColorLight(this));
        binding.tvViewMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.tvViewMembers.setCompoundDrawableTintList(ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this)));
        binding.tvViewMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.viewAddMembersCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.viewAddMembersCard.setStrokeColor(CometChatTheme.getStrokeColorLight(this));
        binding.tvAddMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.tvAddMembers.setCompoundDrawableTintList(ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this)));
        binding.tvAddMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.viewBannedMembersCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this));
        binding.viewBannedMembersCard.setStrokeColor(CometChatTheme.getStrokeColorLight(this));
        binding.tvBannedMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.tvBannedMembers.setCompoundDrawableTintList(ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(this)));
        binding.tvBannedMembers.setTextColor(CometChatTheme.getTextColorSecondary(this));
        binding.viewDivider.setBackgroundColor(CometChatTheme.getStrokeColorLight(this));
        TextViewCompat.setCompoundDrawableTintList(binding.tvLeaveGroup, ColorStateList.valueOf(CometChatTheme.getErrorColor(this)));
        binding.tvLeaveGroup.setTextColor(CometChatTheme.getErrorColor(this));
        TextViewCompat.setCompoundDrawableTintList(binding.tvDeleteGroup, ColorStateList.valueOf(CometChatTheme.getErrorColor(this)));
        binding.tvDeleteGroup.setTextColor(CometChatTheme.getErrorColor(this));

        setOptionsVisibility();

        if (group.isJoined()) {
            binding.infoMessage.setVisibility(View.GONE);
        } else {
            binding.infoMessage.setVisibility(View.VISIBLE);
        }

        binding.ivBack.setOnClickListener(view -> finish());

        binding.tvDeleteChat.setOnClickListener(view -> {
            if (baseMessage != null) {
                showAlertDialog(
                    getString(R.string.app_delete_chat_title),
                    getString(R.string.app_delete_chat_subtitle),
                    getString(R.string.app_delete_chat_negative_button),
                    getString(R.string.app_delete_chat_positive_button),
                    false,
                    CometChatTheme.getErrorColor(this),
                    R.drawable.ic_delete,
                    GroupAction.DELETE_CHAT
                );
            }
        });

        binding.tvLeaveGroup.setOnClickListener(view -> {
            if (!CometChatUIKit.getLoggedInUser().getUid().equals(group.getOwner())) {
                showAlertDialog(getString(R.string.app_leave_this_group),
                                getString(R.string.app_leave_this_group_description),
                                getString(R.string.app_btn_cancel),
                                getString(R.string.app_btn_leave),
                                false,
                                CometChatTheme.getErrorColor(this),
                                R.drawable.ic_logout,
                                GroupAction.LEAVE
                );
            } else {
                if (group.getMembersCount() > 2) {
                    showAlertDialog(getResources().getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                                    getString(R.string.app_transfer_ownership_information),
                                    getString(R.string.app_btn_cancel),
                                    getString(R.string.app_btn_continue),
                                    true,
                                    CometChatTheme.getPrimaryColor(this),
                                    0,
                                    GroupAction.SHOW_OWNERSHIP_TRANSFER
                    );
                } else
                    showAlertDialog(getResources().getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                                    getString(R.string.app_transfer_ownership_description),
                                    getString(R.string.app_btn_cancel),
                                    getString(R.string.app_btn_continue),
                                    true,
                                    CometChatTheme.getPrimaryColor(this),
                                    0,
                                    GroupAction.TRANSFER_OWNERSHIP
                    );
            }
        });

        binding.tvDeleteGroup.setOnClickListener(view -> showAlertDialog(getString(R.string.app_delete_and_exit_action),
                                                                          getString(R.string.app_delete_and_exit_description),
                                                                          getString(R.string.app_btn_cancel),
                                                                          getString(R.string.app_btn_delete),
                                                                          false,
                                                                          CometChatTheme.getErrorColor(this),
                                                                          R.drawable.ic_delete,
                                                                          GroupAction.DELETE
        ));
    }

    private void showAlertDialog(
        String title,
        String message,
        String negativeButtonText,
        String positiveButtonText,
        boolean hideIcon,
        @ColorInt int positiveButtonColor,
        @DrawableRes int icon,
        GroupAction groupAction
    ) {
        confirmDialog = new CometChatConfirmDialog(this, com.cometchat.chatuikit.R.style.CometChatConfirmDialogStyle);
        if (icon != 0) confirmDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), icon, null));
        confirmDialog.setConfirmDialogIconTint(CometChatTheme.getErrorColor(this));
        confirmDialog.setHideDialogIcon(hideIcon);
        confirmDialog.setHideIconBackground(hideIcon);
        confirmDialog.setTitleText(title);
        confirmDialog.setSubtitleText(message);
        confirmDialog.setPositiveButtonText(positiveButtonText);
        confirmDialog.setConfirmDialogPositiveButtonBackgroundColor(positiveButtonColor);
        confirmDialog.setNegativeButtonText(negativeButtonText);
        confirmDialog.setOnPositiveButtonClick(v -> {
            if (GroupAction.LEAVE.equals(groupAction)) {
                viewModel.leaveGroup(group);
            } else if (GroupAction.DELETE_CHAT.equals(groupAction)) {
                viewModel.deleteChat();
            } else if (GroupAction.DELETE.equals(groupAction)) {
                viewModel.deleteGroup(group);
            } else if (GroupAction.SHOW_OWNERSHIP_TRANSFER.equals(groupAction)) {
                confirmDialog.dismiss();
                showTransferOwnership();
            } else if (GroupAction.TRANSFER_OWNERSHIP.equals(groupAction)) {
                if (group.getMembersCount() > 2) {
                    confirmDialog.dismiss();
                    viewModel.transferOwnership(groupMember);
                    groupMember = null;
                } else if (group.getMembersCount() == 2) {
                    viewModel.fetchAndTransferOwnerShip();
                } else if (group.getMembersCount() == 1) {
                    confirmDialog.dismiss();
                    binding.tvDeleteGroup.performClick();
                }
            }
        });
        confirmDialog.setOnNegativeButtonClick(v -> confirmDialog.dismiss());
        confirmDialog.setConfirmDialogElevation(0);
        confirmDialog.setCancelable(false);
        confirmDialog.show();
    }

    private void setOptionsVisibility() {
        binding.viewAddMembers.setVisibility(View.GONE);
        binding.viewBannedMembers.setVisibility(View.GONE);
        binding.tvDeleteGroup.setVisibility(View.GONE);
        binding.viewMembers.setVisibility(View.GONE);
        binding.tvLeaveGroup.setVisibility(View.GONE);
        if (group.isJoined()) {
            showGroupMembers();
            switch (group.getScope()) {
                case UIKitConstants.GroupMemberScope.PARTICIPANTS:
                    binding.viewMembers.setVisibility(View.VISIBLE);
                    binding.tvLeaveGroup.setVisibility(View.VISIBLE);
                    break;
                case UIKitConstants.GroupMemberScope.MODERATOR:
                    addMemberSetup();
                    bannedMembersSetup();
                    binding.viewAddMembers.setVisibility(View.GONE);
                    binding.viewBannedMembers.setVisibility(View.GONE);
                    binding.viewMembers.setVisibility(View.VISIBLE);
                    binding.tvLeaveGroup.setVisibility(View.VISIBLE);
                    break;
                case UIKitConstants.GroupMemberScope.ADMIN:
                    addMemberSetup();
                    bannedMembersSetup();
                    binding.viewAddMembers.setVisibility(View.VISIBLE);
                    binding.viewBannedMembers.setVisibility(View.VISIBLE);
                    binding.viewMembers.setVisibility(View.VISIBLE);
                    if (group.getMembersCount() > 1) binding.tvLeaveGroup.setVisibility(View.VISIBLE);
                    binding.tvDeleteGroup.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void bannedMembersSetup() {
        binding.viewBannedMembers.setOnClickListener(view -> showBannedMemberDialog());
    }

    private void showBannedMemberDialog() {
        BannedMembersLayoutBinding bannedMemberLayoutBinding = BannedMembersLayoutBinding.bind(View.inflate(this,
                                                                                                            R.layout.banned_members_layout,
                                                                                                            null
        ));
        bannedMemberLayoutBinding.bannedMembers.setGroup(group);
        alertDialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat);
        Utils.removeParentFromView(bannedMemberLayoutBinding.getRoot());
        alertDialog.setView(bannedMemberLayoutBinding.getRoot());
        dialog = alertDialog.create();
        Utils.setDialogStatusBarColor(dialog, CometChatTheme.getBackgroundColor1(this));
        dialog.show();

        if (dialog != null)
            applyWindowInsets(Objects.requireNonNull(dialog.getWindow()).findViewById(R.id.banned_members_main));

        bannedMemberLayoutBinding.bannedMembers.setOnBackPressListener(() -> dialog.dismiss());
    }

    private void showGroupMembers() {
        binding.viewMembers.setOnClickListener(view -> showGroupMembersDialogView());
    }

    private void showGroupMembersDialogView() {
        GroupMembersLayoutBinding viewMembersLayoutBinding = GroupMembersLayoutBinding.bind(View.inflate(this, R.layout.group_members_layout, null));
        viewMembersLayoutBinding.viewMembers.setGroup(group);
        alertDialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat);
        Utils.removeParentFromView(viewMembersLayoutBinding.getRoot());
        alertDialog.setView(viewMembersLayoutBinding.getRoot());
        dialog = alertDialog.create();
        Utils.setDialogStatusBarColor(dialog, CometChatTheme.getBackgroundColor1(this));
        dialog.show();

        if (dialog != null)
            applyWindowInsets(Objects.requireNonNull(Objects.requireNonNull(dialog.getWindow()).findViewById(R.id.view_members_main)));

        viewMembersLayoutBinding.viewMembers.setOnBackPressListener(() -> dialog.dismiss());
    }

    private void addMemberSetup() {
        binding.viewAddMembers.setOnClickListener(view -> showAddMembers());
    }

    private void showAddMembers() {
        addMembersLayoutBinding = AddMembersLayoutBinding.bind(View.inflate(this, R.layout.add_members_layout, null));
        tvError = addMembersLayoutBinding.tvError;
        progressBar = addMembersLayoutBinding.progress;
        btnText = addMembersLayoutBinding.tvAddMembers;

        addMembersLayoutBinding.addMembersBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(this));
        addMembersLayoutBinding.tvAddMembers.setTextColor(CometChatTheme.getColorWhite(this));
        addMembersLayoutBinding.progress.setIndeterminateTintList(ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this)));

        addMembersLayoutBinding.addMembers.setTitleText(getString(com.cometchat.chatuikit.R.string.cometchat_add_members));
        addMembersLayoutBinding.addMembers.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE);
        addMembersLayoutBinding.addMembers.setSubmitSelectionIconVisibility(View.GONE);
        addMembersLayoutBinding.addMembers.setBackIconVisibility(View.VISIBLE);
        addMembersLayoutBinding.addMembers.setOnItemClick((view, poUser, user) -> addMembersLayoutBinding.addMembers.selectUser(user,
                                                                                                                                UIKitConstants.SelectionMode.MULTIPLE));
        addMembersLayoutBinding.addMembersBtn.setOnClickListener(view -> viewModel.addMembersToGroup(this,
                                                                                                     addMembersLayoutBinding.addMembers.getSelectedUsers()));

        alertDialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat);
        Utils.removeParentFromView(addMembersLayoutBinding.getRoot());
        alertDialog.setView(addMembersLayoutBinding.getRoot());
        dialog = alertDialog.create();
        Utils.setDialogStatusBarColor(dialog, CometChatTheme.getBackgroundColor1(this));
        dialog.show();

        if (dialog != null)
            applyWindowInsets(Objects.requireNonNull(dialog.getWindow()).findViewById(R.id.add_members_main));

        addMembersLayoutBinding.addMembers.setOnBackPressListener(() -> dialog.dismiss());
    }

    private void showTransferOwnership() {
        TransferOwnershipLayoutBinding transferOwnershipLayoutBinding = TransferOwnershipLayoutBinding.bind(View.inflate(this,
                                                                                                                         R.layout.transfer_ownership_layout,
                                                                                                                         null
        ));

        transferOwnershipLayoutBinding.transferOwnershipBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(this));
        transferOwnershipLayoutBinding.tvOwnership.setTextColor(CometChatTheme.getColorWhite(this));
        transferOwnershipLayoutBinding.progress.setIndeterminateTintList(ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this)));

        transferOwnershipLayoutBinding.transferOwnership.setTitleText(getString(R.string.app_transfer_ownership));
        transferOwnershipLayoutBinding.transferOwnership.excludeOwner(true);
        transferOwnershipLayoutBinding.transferOwnership.setGroup(group);
        transferOwnershipLayoutBinding.transferOwnership.setSelectionMode(UIKitConstants.SelectionMode.SINGLE);
        tvError = transferOwnershipLayoutBinding.tvError;
        progressBar = transferOwnershipLayoutBinding.progress;
        btnText = transferOwnershipLayoutBinding.tvOwnership;

        transferOwnershipLayoutBinding.transferOwnership.setSubmitSelectionIconVisibility(View.GONE);
        transferOwnershipLayoutBinding.transferOwnership.setDiscardSelectionIconVisibility(View.GONE);
        transferOwnershipLayoutBinding.transferOwnership.setBackIconVisibility(View.GONE);

        alertDialog = new AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat);
        Utils.removeParentFromView(transferOwnershipLayoutBinding.getRoot());
        alertDialog.setView(transferOwnershipLayoutBinding.getRoot());
        dialog = alertDialog.create();
        Utils.setDialogStatusBarColor(dialog, CometChatTheme.getBackgroundColor1(this));
        dialog.show();

        transferOwnershipLayoutBinding.transferOwnership.setBackIconVisibility(View.VISIBLE);
        transferOwnershipLayoutBinding.transferOwnership.setOnBackPressListener(() -> dialog.dismiss());
        transferOwnershipLayoutBinding.transferOwnershipBtn.setOnClickListener(v -> {
            if (!transferOwnershipLayoutBinding.transferOwnership.getSelectedGroupMembers().isEmpty()) {
                groupMember = transferOwnershipLayoutBinding.transferOwnership.getSelectedGroupMembers().get(0);
                showAlertDialog(getResources().getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                                getString(R.string.app_transfer_ownership_description),
                                getString(R.string.app_btn_cancel),
                                getString(R.string.app_btn_continue),
                                true,
                                CometChatTheme.getPrimaryColor(this),
                                0,
                                GroupAction.TRANSFER_OWNERSHIP
                );
            }
        });
    }

    private void setDialogState(UIKitConstants.DialogState state) {
        tvError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnText.setVisibility(View.GONE);
        switch (state) {
            case INITIATED:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                btnText.setVisibility(View.VISIBLE);
                dialog.dismiss();
                break;
            case FAILURE:
                tvError.setVisibility(View.VISIBLE);
                btnText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setConfirmDialogStateObserver(UIKitConstants.DialogState state) {
        switch (state) {
            case INITIATED:
                confirmDialog.hidePositiveButtonProgressBar(false);
                break;
            case SUCCESS:
                if (confirmDialog != null) confirmDialog.dismiss();
                finish();
                break;
            case FAILURE:
                confirmDialog.dismiss();
                Toast.makeText(this,
                               getResources().getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again),
                               Toast.LENGTH_SHORT
                ).show();
                break;
        }
    }

    private void setTransferOwnershipDialogStateObserver(@NonNull UIKitConstants.DialogState state) {
        switch (state) {
            case INITIATED:
                confirmDialog.hidePositiveButtonProgressBar(false);
                break;
            case SUCCESS:
                if (dialog != null) dialog.dismiss();
                if (confirmDialog != null) {
                    confirmDialog.dismiss();
                    binding.tvLeaveGroup.performClick();
                }
                break;
            case FAILURE:
                confirmDialog.dismiss();
                Toast.makeText(this,
                               getResources().getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again),
                               Toast.LENGTH_SHORT
                ).show();
                break;
        }
    }

    private void setError(String error) {
        if (tvError != null) tvError.setText(error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.removeListeners();
    }
}
