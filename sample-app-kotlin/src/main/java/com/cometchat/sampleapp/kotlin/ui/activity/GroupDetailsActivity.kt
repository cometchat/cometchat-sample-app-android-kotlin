package com.cometchat.sampleapp.kotlin.ui.activity

import android.app.Dialog
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
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.data.enums.GroupAction
import com.cometchat.sampleapp.kotlin.databinding.ActivityGroupDetailsBinding
import com.cometchat.sampleapp.kotlin.databinding.AddMembersLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.BannedMembersLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.GroupMembersLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.TransferOwnershipLayoutBinding
import com.cometchat.sampleapp.kotlin.viewmodels.GroupDetailsViewModel
import com.google.gson.Gson

class GroupDetailsActivity : AppCompatActivity() {
    private lateinit var group: Group
    private lateinit var binding: ActivityGroupDetailsBinding
    private lateinit var addMembersLayoutBinding: AddMembersLayoutBinding
    private lateinit var dialog: Dialog
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var viewModel: GroupDetailsViewModel
    private var tvError: TextView? = null
    private var progressBar: ProgressBar? = null
    private var btnText: TextView? = null
    private var confirmDialog: CometChatConfirmDialog? = null
    private var groupMember: GroupMember? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupDetailsBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        group = Gson().fromJson(
            intent.getStringExtra(getString(R.string.app_group)), Group::class.java
        )

        initViewModel()

        setHeaderData(group)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(
            GroupDetailsViewModel::class.java
        )

        viewModel.dialogState.observe(
            this
        ) { state: DialogState -> this.setDialogState(state) }

        viewModel.errorMessage.observe(this) { error: String? ->
            if (error != null) {
                this.setError(error)
            }
        }

        viewModel.confirmDialogState.observe(
            this
        ) { state: DialogState -> this.setConfirmDialogStateObserver(state) }

        viewModel.updatedGroup.observe(
            this
        ) { group: Group? ->
            if (group != null) {
                this.setHeaderData(group)
            }
        }

        viewModel.transferOwnershipDialogState.observe(
            this
        ) { state: DialogState -> this.setTransferOwnershipDialogStateObserver(state) }
        viewModel.setGroup(group)

        viewModel.addListeners()
    }

    private fun setHeaderData(group: Group) {
        binding.avatar.setAvatar(group.name, group.icon)
        binding.tvGroupName.text = group.name
        binding.tvMemberCount.text = if (group.membersCount > 1) group.membersCount.toString() + " " + resources.getString(
            com.cometchat.chatuikit.R.string.cometchat_members
        ) else group.membersCount.toString() + " " + resources.getString(com.cometchat.chatuikit.R.string.cometchat_member)
        setOptionsVisibility()

        if (group.isJoined) {
            binding.infoMessage.visibility = View.GONE
        } else {
            binding.infoMessage.visibility = View.VISIBLE
        }

        binding.ivBack.setOnClickListener { view: View? -> finish() }

        binding.leaveGroupLay.setOnClickListener { view: View? ->
            if (CometChatUIKit.getLoggedInUser().uid != group.owner) {
                showAlertDialog(
                    getString(R.string.app_leave_this_group),
                    getString(R.string.app_leave_this_group_description),
                    getString(R.string.app_btn_cancel),
                    getString(R.string.app_btn_leave),
                    false,
                    CometChatTheme.getErrorColor(this),
                    R.drawable.ic_logout,
                    GroupAction.LEAVE
                )
            } else {
                if (group.membersCount > 2) {
                    showAlertDialog(
                        resources.getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                        getString(R.string.app_transfer_ownership_information),
                        getString(R.string.app_btn_cancel),
                        getString(R.string.app_btn_continue),
                        true,
                        CometChatTheme.getPrimaryColor(this),
                        0,
                        GroupAction.SHOW_OWNERSHIP_TRANSFER
                    )
                } else
                    showAlertDialog(
                        resources.getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                        getString(R.string.app_transfer_ownership_description),
                        getString(R.string.app_btn_cancel),
                        getString(R.string.app_btn_continue),
                        true,
                        CometChatTheme.getPrimaryColor(this),
                        0,
                        GroupAction.TRANSFER_OWNERSHIP
                    )
            }
        }

        binding.deleteGroupLay.setOnClickListener { view: View? ->
            showAlertDialog(
                getString(R.string.app_delete_and_exit_action),
                getString(R.string.app_delete_and_exit_description),
                getString(R.string.app_btn_cancel),
                getString(R.string.app_btn_delete),
                false,
                CometChatTheme.getErrorColor(this),
                R.drawable.ic_delete,
                GroupAction.DELETE
            )
        }
    }

    private fun showAlertDialog(
        title: String,
        message: String,
        negativeButtonText: String,
        positiveButtonText: String,
        hideIcon: Boolean,
        @ColorInt positiveButtonColor: Int,
        @DrawableRes icon: Int,
        groupAction: GroupAction
    ) {
        confirmDialog = CometChatConfirmDialog(
            this, com.cometchat.chatuikit.R.style.CometChatConfirmDialogStyle
        )
        if (icon != 0) confirmDialog!!.confirmDialogIcon = ResourcesCompat.getDrawable(
            resources, icon, null
        )
        confirmDialog!!.setConfirmDialogIconTint(CometChatTheme.getErrorColor(this))
        confirmDialog!!.isHideDialogIcon = hideIcon
        confirmDialog!!.isHideIconBackground = hideIcon
        confirmDialog!!.titleText = title
        confirmDialog!!.subtitleText = message
        confirmDialog!!.positiveButtonText = positiveButtonText
        confirmDialog!!.confirmDialogPositiveButtonBackgroundColor = positiveButtonColor
        confirmDialog!!.negativeButtonText = negativeButtonText
        confirmDialog!!.onPositiveButtonClick = View.OnClickListener { v: View? ->
            if (GroupAction.LEAVE == groupAction) {
                viewModel.leaveGroup(group)
            } else if (GroupAction.DELETE == groupAction) {
                viewModel.deleteGroup(group)
            } else if (GroupAction.SHOW_OWNERSHIP_TRANSFER == groupAction) {
                showTransferOwnership()
            } else if (GroupAction.TRANSFER_OWNERSHIP == groupAction) {
                if (group.membersCount > 2) {
                    confirmDialog!!.dismiss()
                    viewModel.transferOwnership(groupMember!!)
                    groupMember = null
                } else if (group.membersCount == 2) {
                    viewModel.fetchAndTransferOwnerShip()
                } else if (group.membersCount == 1) {
                    confirmDialog!!.dismiss()
                    binding.deleteGroupLay.performClick()
                }
            }
        }
        confirmDialog!!.onNegativeButtonClick = View.OnClickListener { v: View? -> confirmDialog!!.dismiss() }
        confirmDialog!!.confirmDialogElevation = 0
        confirmDialog!!.setCancelable(false)
        confirmDialog!!.show()
    }

    private fun setOptionsVisibility() {
        binding.viewAddMembers.visibility = View.GONE
        binding.viewBannedMembers.visibility = View.GONE
        binding.deleteGroupLay.visibility = View.GONE
        binding.viewMembers.visibility = View.GONE
        binding.leaveGroupLay.visibility = View.GONE
        if (group.isJoined) {
            showGroupMembers()
            when (group.scope) {
                UIKitConstants.GroupMemberScope.PARTICIPANTS -> {
                    binding.viewMembers.visibility = View.VISIBLE
                    binding.leaveGroupLay.visibility = View.VISIBLE
                }

                UIKitConstants.GroupMemberScope.MODERATOR -> {
                    addMemberSetup()
                    bannedMembersSetup()
                    binding.viewAddMembers.visibility = View.GONE
                    binding.viewBannedMembers.visibility = View.GONE
                    binding.viewMembers.visibility = View.VISIBLE
                    binding.leaveGroupLay.visibility = View.VISIBLE
                }

                UIKitConstants.GroupMemberScope.ADMIN -> {
                    addMemberSetup()
                    bannedMembersSetup()
                    binding.viewAddMembers.visibility = View.VISIBLE
                    binding.viewBannedMembers.visibility = View.VISIBLE
                    binding.viewMembers.visibility = View.VISIBLE
                    if (group.membersCount > 1) binding.leaveGroupLay.visibility = View.VISIBLE
                    binding.deleteGroupLay.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun bannedMembersSetup() {
        binding.viewBannedMembers.setOnClickListener { view: View? -> showBannedMemberDialog() }
    }

    private fun showBannedMemberDialog() {
        val bannedMemberLayoutBinding = BannedMembersLayoutBinding.bind(
            View.inflate(
                this, R.layout.banned_members_layout, null
            )
        )
        bannedMemberLayoutBinding.bannedMembers.setGroup(group)
        alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        Utils.removeParentFromView(bannedMemberLayoutBinding.root)
        alertDialog.setView(bannedMemberLayoutBinding.root)
        dialog = alertDialog.create()
        Utils.setDialogStatusBarColor(
            dialog, CometChatTheme.getBackgroundColor1(
                this
            )
        )
        dialog.show()
        bannedMemberLayoutBinding.bannedMembers.setOnBackPressListener { dialog.dismiss() }
    }

    private fun showGroupMembers() {
        binding.viewMembers.setOnClickListener { view: View? -> showGroupMembersDialogView() }
    }

    private fun showGroupMembersDialogView() {
        val viewMembersLayoutBinding = GroupMembersLayoutBinding.bind(
            View.inflate(
                this, R.layout.group_members_layout, null
            )
        )
        viewMembersLayoutBinding.viewMembers.setGroup(group)
        alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        Utils.removeParentFromView(viewMembersLayoutBinding.root)
        alertDialog.setView(viewMembersLayoutBinding.root)
        dialog = alertDialog.create()
        Utils.setDialogStatusBarColor(
            dialog, CometChatTheme.getBackgroundColor1(
                this
            )
        )
        dialog.show()
        viewMembersLayoutBinding.viewMembers.setOnBackPressListener { dialog.dismiss() }
    }

    private fun addMemberSetup() {
        binding.viewAddMembers.setOnClickListener { view: View? -> showAddMembers() }
    }

    private fun showAddMembers() {
        addMembersLayoutBinding = AddMembersLayoutBinding.bind(
            View.inflate(
                this, R.layout.add_members_layout, null
            )
        )
        tvError = addMembersLayoutBinding.tvError
        progressBar = addMembersLayoutBinding.progress
        btnText = addMembersLayoutBinding.tvAddMembers

        addMembersLayoutBinding.addMembers.setTitleText(getString(com.cometchat.chatuikit.R.string.cometchat_add_members))
        addMembersLayoutBinding.addMembers.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
        addMembersLayoutBinding.addMembers.setSubmitSelectionIconVisibility(View.GONE)
        addMembersLayoutBinding.addMembers.backIconVisibility = View.VISIBLE
        addMembersLayoutBinding.addMembers.setOnItemClick { view, position, user ->
            addMembersLayoutBinding.addMembers.selectUser(user, UIKitConstants.SelectionMode.MULTIPLE)
        }


        addMembersLayoutBinding.addMembersBtn.setOnClickListener {
            viewModel.addMembersToGroup(this,
                addMembersLayoutBinding.addMembers.selectedUsers
            )
        }

        alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        Utils.removeParentFromView(
            addMembersLayoutBinding.root
        )
        alertDialog.setView(addMembersLayoutBinding.root)
        dialog = alertDialog.create()
        Utils.setDialogStatusBarColor(
            dialog, CometChatTheme.getBackgroundColor1(
                this
            )
        )
        dialog.show()
        addMembersLayoutBinding.addMembers.setOnBackPressListener { dialog.dismiss() }
    }

    private fun showTransferOwnership() {
        val transferOwnershipLayoutBinding = TransferOwnershipLayoutBinding.bind(
            View.inflate(
                this, R.layout.transfer_ownership_layout, null
            )
        )
        transferOwnershipLayoutBinding.transferOwnership.setTitleText(getString(R.string.app_transfer_ownership))
        transferOwnershipLayoutBinding.transferOwnership.excludeOwner(true)
        transferOwnershipLayoutBinding.transferOwnership.setGroup(group)
        transferOwnershipLayoutBinding.transferOwnership.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
        tvError = transferOwnershipLayoutBinding.tvError
        progressBar = transferOwnershipLayoutBinding.progress
        btnText = transferOwnershipLayoutBinding.tvOwnership

        transferOwnershipLayoutBinding.transferOwnership.setSubmitSelectionIconVisibility(View.GONE)
        transferOwnershipLayoutBinding.transferOwnership.setDiscardSelectionIconVisibility(View.GONE)
        transferOwnershipLayoutBinding.transferOwnership.setBackIconVisibility(View.GONE)

        alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
        Utils.removeParentFromView(transferOwnershipLayoutBinding.root)
        alertDialog.setView(transferOwnershipLayoutBinding.root)
        dialog = alertDialog.create()
        Utils.setDialogStatusBarColor(
            dialog, CometChatTheme.getBackgroundColor1(
                this
            )
        )
        dialog.show()

        transferOwnershipLayoutBinding.transferOwnership.setBackIconVisibility(View.VISIBLE)
        transferOwnershipLayoutBinding.transferOwnership.setOnBackPressListener { dialog.dismiss() }
        transferOwnershipLayoutBinding.transferOwnershipBtn.setOnClickListener {
            if (transferOwnershipLayoutBinding.transferOwnership.selectedGroupMembers.isNotEmpty()) {
                groupMember = transferOwnershipLayoutBinding.transferOwnership.selectedGroupMembers[0]
                showAlertDialog(
                    resources.getString(com.cometchat.chatuikit.R.string.cometchat_transfer_ownership),
                    getString(R.string.app_transfer_ownership_description),
                    getString(R.string.app_btn_cancel),
                    getString(R.string.app_btn_continue),
                    true,
                    CometChatTheme.getPrimaryColor(this),
                    0,
                    GroupAction.TRANSFER_OWNERSHIP
                )
            }
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
                dialog.dismiss()
            }

            DialogState.FAILURE -> {
                tvError?.visibility = View.VISIBLE
                btnText?.visibility = View.VISIBLE
            }
        }
    }

    private fun setConfirmDialogStateObserver(state: DialogState) {
        when (state) {
            DialogState.INITIATED -> confirmDialog?.hidePositiveButtonProgressBar(false)

            DialogState.SUCCESS -> {
                confirmDialog?.dismiss()
                finish()
            }

            DialogState.FAILURE -> {
                confirmDialog?.dismiss()
                Toast.makeText(
                    this, resources.getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setTransferOwnershipDialogStateObserver(state: DialogState) {
        when (state) {
            DialogState.INITIATED -> confirmDialog!!.hidePositiveButtonProgressBar(false)
            DialogState.SUCCESS -> if (confirmDialog != null) {
                dialog.dismiss()
                confirmDialog!!.dismiss()
                binding.leaveGroupLay.performClick()
            }

            DialogState.FAILURE -> {
                confirmDialog!!.dismiss()
                Toast.makeText(
                    this, resources.getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setError(error: String) {
        tvError?.text = error
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
}
