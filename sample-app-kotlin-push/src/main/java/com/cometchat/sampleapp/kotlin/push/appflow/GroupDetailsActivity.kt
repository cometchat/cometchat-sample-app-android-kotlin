package com.cometchat.sampleapp.kotlin.push.appflow

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.enums.GroupAction
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.DialogState
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.GroupDetailsViewModel
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityGroupDetailsBinding
import com.cometchat.sampleapp.kotlin.push.databinding.DialogAddMembersBinding
import com.cometchat.sampleapp.kotlin.push.databinding.DialogBannedMembersBinding
import com.cometchat.sampleapp.kotlin.push.databinding.DialogGroupMembersBinding
import com.cometchat.sampleapp.kotlin.push.databinding.DialogTransferOwnershipBinding
import com.google.gson.Gson

class GroupDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGroupDetailsBinding
    private lateinit var viewModel: GroupDetailsViewModel
    private lateinit var group: Group
    private var baseMessage: BaseMessage? = null
    private var dialog: Dialog? = null
    private var confirmDialog: CometChatConfirmDialog? = null
    private var groupMember: GroupMember? = null
    
    // Dialog views for state management
    private var tvError: TextView? = null
    private var progressBar: ProgressBar? = null
    private var btnText: TextView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        
        // Extract group from intent
        group = Gson().fromJson(
            intent.getStringExtra(getString(R.string.app_group)),
            Group::class.java
        )
        baseMessage = Gson().fromJson(
            intent.getStringExtra(getString(R.string.app_base_message)),
            BaseMessage::class.java
        )
        
        initViewModel()
        setHeaderData(group)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
    
    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(GroupDetailsViewModel::class.java)
        
        viewModel.dialogState.observe(this) { state -> setDialogState(state) }
        viewModel.errorMessage.observe(this) { error -> error?.let { setError(it) } }
        viewModel.confirmDialogState.observe(this) { state -> setConfirmDialogStateObserver(state) }
        viewModel.updatedGroup.observe(this) { group -> group?.let { setHeaderData(it) } }
        viewModel.transferOwnershipDialogState.observe(this) { state -> setTransferOwnershipDialogStateObserver(state) }
        
        viewModel.setGroup(group)
        viewModel.setBaseMessage(baseMessage)
        viewModel.addListeners()
    }
    
    private fun setHeaderData(group: Group) {
        this.group = group
        
        binding.avatar.setAvatar(group.name, group.icon)
        binding.tvGroupName.text = group.name
        
        val memberText = if (group.membersCount > 1) {
            "${group.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_members)}"
        } else {
            "${group.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_member)}"
        }
        binding.tvMemberCount.text = memberText
        
        // Apply theme colors
        applyThemeColors()
        
        // Set visibility based on join status
        setOptionsVisibility()
        setDeleteChatVisibility()
        
        // Info message visibility
        binding.infoMessage.visibility = if (group.isJoined) View.GONE else View.VISIBLE
        
        // Click listeners
        binding.ivBack.setOnClickListener { finish() }
        
        binding.tvDeleteChat.setOnClickListener {
            showAlertDialog(
                title = getString(R.string.app_delete_chat_title),
                message = getString(R.string.app_delete_chat_subtitle),
                negativeText = getString(R.string.app_delete_chat_negative_button),
                positiveText = getString(R.string.app_delete_chat_positive_button),
                hideIcon = false,
                positiveButtonColor = CometChatTheme.getErrorColor(this),
                icon = com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete,
                groupAction = GroupAction.DELETE_CHAT
            )
        }
        
        binding.leaveGroupLay.setOnClickListener {
            if (CometChatUIKit.getLoggedInUser()?.uid != group.owner) {
                showAlertDialog(
                    title = getString(R.string.app_leave_this_group),
                    message = getString(R.string.app_leave_this_group_description),
                    negativeText = getString(R.string.app_btn_cancel),
                    positiveText = getString(R.string.app_btn_leave),
                    hideIcon = false,
                    positiveButtonColor = CometChatTheme.getErrorColor(this),
                    icon = com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_close,
                    groupAction = GroupAction.LEAVE
                )
            } else {
                if (group.membersCount > 2) {
                    showAlertDialog(
                        title = getString(com.cometchat.uikit.kotlin.R.string.cometchat_transfer_ownership),
                        message = getString(R.string.app_transfer_ownership_information),
                        negativeText = getString(R.string.app_btn_cancel),
                        positiveText = getString(R.string.app_btn_continue),
                        hideIcon = true,
                        positiveButtonColor = CometChatTheme.getPrimaryColor(this),
                        icon = 0,
                        groupAction = GroupAction.SHOW_OWNERSHIP_TRANSFER
                    )
                } else {
                    showAlertDialog(
                        title = getString(com.cometchat.uikit.kotlin.R.string.cometchat_transfer_ownership),
                        message = getString(R.string.app_transfer_ownership_description),
                        negativeText = getString(R.string.app_btn_cancel),
                        positiveText = getString(R.string.app_btn_continue),
                        hideIcon = true,
                        positiveButtonColor = CometChatTheme.getPrimaryColor(this),
                        icon = 0,
                        groupAction = GroupAction.TRANSFER_OWNERSHIP
                    )
                }
            }
        }
        
        binding.deleteGroupLay.setOnClickListener {
            // If owner and there are other members, need to transfer ownership first
            if (CometChatUIKit.getLoggedInUser()?.uid == group.owner && group.membersCount > 1) {
                if (group.membersCount > 2) {
                    showAlertDialog(
                        title = getString(com.cometchat.uikit.kotlin.R.string.cometchat_transfer_ownership),
                        message = getString(R.string.app_transfer_ownership_information),
                        negativeText = getString(R.string.app_btn_cancel),
                        positiveText = getString(R.string.app_btn_continue),
                        hideIcon = true,
                        positiveButtonColor = CometChatTheme.getPrimaryColor(this),
                        icon = 0,
                        groupAction = GroupAction.SHOW_OWNERSHIP_TRANSFER
                    )
                } else {
                    showAlertDialog(
                        title = getString(com.cometchat.uikit.kotlin.R.string.cometchat_transfer_ownership),
                        message = getString(R.string.app_transfer_ownership_description),
                        negativeText = getString(R.string.app_btn_cancel),
                        positiveText = getString(R.string.app_btn_continue),
                        hideIcon = true,
                        positiveButtonColor = CometChatTheme.getPrimaryColor(this),
                        icon = 0,
                        groupAction = GroupAction.TRANSFER_OWNERSHIP
                    )
                }
            } else {
                // Owner with no other members, or not owner - can delete directly
                showAlertDialog(
                    title = getString(R.string.app_delete_and_exit_action),
                    message = getString(R.string.app_delete_and_exit_description),
                    negativeText = getString(R.string.app_btn_cancel),
                    positiveText = getString(R.string.app_btn_delete),
                    hideIcon = false,
                    positiveButtonColor = CometChatTheme.getErrorColor(this),
                    icon = com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete,
                    groupAction = GroupAction.DELETE
                )
            }
        }
    }
    
    private fun applyThemeColors() {
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.tvGroupName.setTextColor(CometChatTheme.getTextColorPrimary(this))
        binding.tvMemberCount.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.tvViewMembers.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.tvAddMembers.setTextColor(CometChatTheme.getTextColorSecondary(this))
        binding.tvBannedMembers.setTextColor(CometChatTheme.getTextColorSecondary(this))
    }
    
    private fun setDeleteChatVisibility() {
        // Always show Delete Chat for joined members regardless of last message
        binding.tvDeleteChat.visibility = if (group.isJoined) View.VISIBLE else View.GONE
    }
    
    private fun setOptionsVisibility() {
        binding.viewAddMembers.visibility = View.GONE
        binding.viewBannedMembers.visibility = View.GONE
        binding.deleteGroupLay.visibility = View.GONE
        binding.viewMembers.visibility = View.GONE
        binding.leaveGroupLay.visibility = View.GONE
        
        if (group.isJoined) {
            setupGroupMembersClick()
            
            when (group.scope) {
                UIKitConstants.GroupMemberScope.PARTICIPANTS -> {
                    binding.viewMembers.visibility = View.VISIBLE
                    binding.leaveGroupLay.visibility = View.VISIBLE
                }
                UIKitConstants.GroupMemberScope.MODERATOR -> {
                    binding.viewMembers.visibility = View.VISIBLE
                    binding.leaveGroupLay.visibility = View.VISIBLE
                }
                UIKitConstants.GroupMemberScope.ADMIN -> {
                    setupAddMembersClick()
                    setupBannedMembersClick()
                    binding.viewAddMembers.visibility = View.VISIBLE
                    binding.viewBannedMembers.visibility = View.VISIBLE
                    binding.viewMembers.visibility = View.VISIBLE
                    if (group.membersCount > 1) binding.leaveGroupLay.visibility = View.VISIBLE
                    binding.deleteGroupLay.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun setupGroupMembersClick() {
        binding.viewMembers.setOnClickListener { showGroupMembersDialog() }
    }
    
    private fun setupAddMembersClick() {
        binding.viewAddMembers.setOnClickListener { showAddMembersDialog() }
    }
    
    private fun setupBannedMembersClick() {
        binding.viewBannedMembers.setOnClickListener { showBannedMembersDialog() }
    }
    
    private fun showGroupMembersDialog() {
        val dialogBinding = DialogGroupMembersBinding.inflate(layoutInflater)
        dialogBinding.viewMembers.setGroup(group)
        
        val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        // TODO: // Utils.removeParentFromView is private in chatuikit-kotlin
        // // Utils.removeParentFromView(dialogBinding.root)
        alertDialog.setView(dialogBinding.root)
        dialog = alertDialog.create()
        // TODO: // Utils.setDialogStatusBarColor doesn't exist in chatuikit-kotlin
        // // Utils.setDialogStatusBarColor(dialog!!, CometChatTheme.getBackgroundColor1(this))
        dialog?.show()
        
        dialogBinding.viewMembers.setOnBackPress { dialog?.dismiss() }
    }
    
    private fun showAddMembersDialog() {
        val dialogBinding = DialogAddMembersBinding.inflate(layoutInflater)
        
        dialogBinding.addMembersBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(this))
        dialogBinding.tvAddMembers.setTextColor(CometChatTheme.getColorWhite(this))
        dialogBinding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
        
        tvError = dialogBinding.tvError
        progressBar = dialogBinding.progress
        btnText = dialogBinding.tvAddMembers
        
        val addMembersTitle = getString(R.string.app_add_members)
        
        // Set initial title to "Add Members"
        dialogBinding.addMembers.setTitle(addMembersTitle)
        
        dialogBinding.addMembers.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
        dialogBinding.addMembers.setBackIconVisibility(View.VISIBLE)
        dialogBinding.addMembers.setOnItemClick { user ->
            dialogBinding.addMembers.selectUser(user, UIKitConstants.SelectionMode.MULTIPLE)
        }
        
        // Update title when selection changes
        dialogBinding.addMembers.setOnSelectionChange { selectedUsers ->
            val title = if (selectedUsers.isEmpty()) {
                addMembersTitle
            } else {
                "${selectedUsers.size} ${getString(R.string.app_selected)}"
            }
            dialogBinding.addMembers.setTitle(title)
        }
        
        dialogBinding.addMembersBtn.setOnClickListener {
            val selectedUsers = dialogBinding.addMembers.getSelectedUsers()
            if (selectedUsers.isNotEmpty()) {
                viewModel.addMembersToGroup(this, selectedUsers)
            }
        }
        
        val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        alertDialog.setView(dialogBinding.root)
        dialog = alertDialog.create()
        dialog?.show()
        
        dialogBinding.addMembers.setOnBackPress { dialog?.dismiss() }
    }
    
    private fun showBannedMembersDialog() {
        val dialogBinding = DialogBannedMembersBinding.inflate(layoutInflater)
        // Note: CometChatBannedMembers is a custom view that needs to be implemented
        // For now, showing a placeholder dialog
        
        val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        // Utils.removeParentFromView(dialogBinding.root)
        alertDialog.setView(dialogBinding.root)
        dialog = alertDialog.create()
        // Utils.setDialogStatusBarColor(dialog!!, CometChatTheme.getBackgroundColor1(this))
        dialog?.show()
    }
    
    private fun showTransferOwnershipDialog() {
        val dialogBinding = DialogTransferOwnershipBinding.inflate(layoutInflater)
        
        dialogBinding.transferOwnershipBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(this))
        dialogBinding.tvOwnership.setTextColor(CometChatTheme.getColorWhite(this))
        dialogBinding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
        
        dialogBinding.transferOwnership.setGroup(group)
        dialogBinding.transferOwnership.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
        dialogBinding.transferOwnership.excludeOwner(true)
        
        tvError = dialogBinding.tvError
        progressBar = dialogBinding.progress
        btnText = dialogBinding.tvOwnership
        
        val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        alertDialog.setView(dialogBinding.root)
        dialog = alertDialog.create()
        dialog?.show()
        
        dialogBinding.transferOwnership.setOnBackPress { dialog?.dismiss() }
        dialogBinding.transferOwnershipBtn.setOnClickListener {
            val selectedMembers = dialogBinding.transferOwnership.getSelectedMembers()
            if (selectedMembers.isNotEmpty()) {
                groupMember = selectedMembers[0]
                showAlertDialog(
                    title = getString(com.cometchat.uikit.kotlin.R.string.cometchat_transfer_ownership),
                    message = getString(R.string.app_transfer_ownership_description),
                    negativeText = getString(R.string.app_btn_cancel),
                    positiveText = getString(R.string.app_btn_continue),
                    hideIcon = true,
                    positiveButtonColor = CometChatTheme.getPrimaryColor(this),
                    icon = 0,
                    groupAction = GroupAction.TRANSFER_OWNERSHIP
                )
            }
        }
    }
    
    private fun showAlertDialog(
        title: String,
        message: String,
        negativeText: String,
        positiveText: String,
        hideIcon: Boolean,
        @ColorInt positiveButtonColor: Int,
        @DrawableRes icon: Int,
        groupAction: GroupAction
    ) {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            if (icon != 0) {
                setConfirmDialogIcon(ResourcesCompat.getDrawable(resources, icon, null))
            }
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            hideDialogIcon(hideIcon)
            // TODO: isHideIconBackground not available in chatuikit-kotlin CometChatConfirmDialog
            // isHideIconBackground = hideIcon
            setTitleText(title)
            setSubtitleText(message)
            setPositiveButtonText(positiveText)
            setPositiveButtonBackgroundColor(positiveButtonColor)
            setNegativeButtonText(negativeText)
            setOnPositiveButtonClick {
                when (groupAction) {
                    GroupAction.LEAVE -> viewModel.leaveGroup(group)
                    GroupAction.DELETE_CHAT -> viewModel.deleteChat()
                    GroupAction.DELETE -> viewModel.deleteGroup(group)
                    GroupAction.SHOW_OWNERSHIP_TRANSFER -> {
                        confirmDialog?.dismiss()
                        showTransferOwnershipDialog()
                    }

                    GroupAction.TRANSFER_OWNERSHIP -> {
                        if (group.membersCount > 2 && groupMember != null) {
                            confirmDialog?.dismiss()
                            viewModel.transferOwnership(groupMember!!)
                            groupMember = null
                        } else if (group.membersCount == 2) {
                            viewModel.fetchAndTransferOwnerShip()
                        } else if (group.membersCount == 1) {
                            confirmDialog?.dismiss()
                            binding.deleteGroupLay.performClick()
                        }
                    }
                }
            }
            setOnNegativeButtonClick { confirmDialog?.dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }
    
    private fun setDialogState(state: DialogState) {
        tvError?.visibility = View.GONE
        progressBar?.visibility = View.GONE
        btnText?.visibility = View.GONE
        
        when (state) {
            DialogState.INITIATED -> progressBar?.visibility = View.VISIBLE
            DialogState.SUCCESS -> {
                btnText?.visibility = View.VISIBLE
                dialog?.dismiss()
            }
            DialogState.FAILURE -> {
                tvError?.visibility = View.VISIBLE
                btnText?.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setConfirmDialogStateObserver(state: DialogState) {
        when (state) {
            DialogState.INITIATED -> confirmDialog?.showPositiveButtonProgress(true)
            DialogState.SUCCESS -> {
                confirmDialog?.dismiss()
                finish()
            }
            DialogState.FAILURE -> {
                confirmDialog?.dismiss()
                Toast.makeText(
                    this,
                    getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setTransferOwnershipDialogStateObserver(state: DialogState) {
        when (state) {
            DialogState.INITIATED -> confirmDialog?.showPositiveButtonProgress(true)
            DialogState.SUCCESS -> {
                dialog?.dismiss()
                confirmDialog?.dismiss()
                binding.leaveGroupLay.performClick()
            }
            DialogState.FAILURE -> {
                confirmDialog?.dismiss()
                Toast.makeText(
                    this,
                    getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setError(error: String) {
        tvError?.text = error
    }
}
