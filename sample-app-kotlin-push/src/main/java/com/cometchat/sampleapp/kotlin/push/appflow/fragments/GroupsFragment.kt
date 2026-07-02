package com.cometchat.sampleapp.kotlin.push.appflow.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.Group
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity
import com.cometchat.sampleapp.kotlin.push.appflow.viewmodels.GroupsViewModel
import com.cometchat.sampleapp.kotlin.push.databinding.DialogCreateGroupBinding
import com.cometchat.sampleapp.kotlin.push.databinding.FragmentGroupsBinding
import com.cometchat.sampleapp.kotlin.push.databinding.DialogJoinPasswordGroupBinding
import com.cometchat.sampleapp.kotlin.push.databinding.OverflowMenuLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicReference

/**
 * Fragment displaying the CometChatGroups component.
 * Mirrors the GroupsFragment from master-app-kotlin.
 */
class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: GroupsViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var tvError: TextView? = null
    private var tvButtonText: TextView? = null
    private var progressBar: ProgressBar? = null

    companion object {
        private const val TAG = "GroupsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider.NewInstanceFactory().create(GroupsViewModel::class.java)
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupGroupsList()
        setupOverflowMenu()
    }

    /**
     * Sets up ViewModel observers for group operations.
     */
    private fun setupObservers() {
        viewModel.joinedGroup.observe(viewLifecycleOwner) { group ->
            openGroupChat(group)
        }
        
        viewModel.createdGroup.observe(viewLifecycleOwner) { group ->
            openGroupChat(group)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { message ->
            message?.let { setErrorMessage(it) }
        }
        
        viewModel.dialogState.observe(viewLifecycleOwner) { status ->
            setDialogState(status)
        }
    }

    /**
     * Sets up the CometChatGroups component with click listeners.
     */
    private fun setupGroupsList() {
        binding.group.setOnItemClick { group ->
            handleGroupClick(group)
        }
    }

    /**
     * Handles group item click based on join status and group type.
     */
    private fun handleGroupClick(group: Group) {
        Log.d(TAG, "Group clicked: ${group.name}, isJoined: ${group.isJoined}, type: ${group.groupType}")
        
        when {
            group.isJoined -> {
                openGroupChat(group)
            }
            group.groupType.equals(UIKitConstants.GroupType.PUBLIC, ignoreCase = true) -> {
                viewModel.joinPasswordGroup(group, "")
            }
            group.groupType.equals(UIKitConstants.GroupType.PASSWORD, ignoreCase = true) -> {
                openJoinPasswordGroupDialog(group)
            }
        }
    }

    /**
     * Opens the chat interface for the specified group.
     */
    private fun openGroupChat(group: Group) {
        if (group.isJoined) {
            if (bottomSheetDialog?.isShowing == true) {
                bottomSheetDialog?.dismiss()
            }
            Log.d(TAG, "Navigating to messages with group: ${group.name}")
            val intent = Intent(requireActivity(), MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            startActivity(intent)
        }
    }

    /**
     * Sets up the overflow menu for creating a new group.
     */
    private fun setupOverflowMenu() {
        val ctx = context ?: return
        val overflowMenuLayoutBinding = OverflowMenuLayoutBinding.inflate(layoutInflater)
        overflowMenuLayoutBinding.ivMenu.imageTintList = ColorStateList.valueOf(
            CometChatTheme.getIconTintHighlight(ctx)
        )
        overflowMenuLayoutBinding.ivMenu.setOnClickListener {
            if (bottomSheetDialog == null) {
                bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
            }
            openCreateGroupDialog()
        }
        // TODO: setOverflowMenu not available on CometChatGroups in chatuikit-kotlin
        binding.group.setOverflowMenu(overflowMenuLayoutBinding.root)
    }

    /**
     * Opens a dialog for creating a new group.
     */
    private fun openCreateGroupDialog() {
        val createGroupLayoutBinding = DialogCreateGroupBinding.inflate(layoutInflater)
        tvError = createGroupLayoutBinding.tvError
        tvButtonText = createGroupLayoutBinding.createGroupBtnText
        progressBar = createGroupLayoutBinding.createGroupProgress
        
        val shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        createGroupLayoutBinding.createGroupCard.shapeAppearanceModel = shapeAppearanceModel
        
        val groupType = AtomicReference(UIKitConstants.GroupType.PUBLIC)
        
        // Apply theme colors
        applyCreateGroupThemeColors(createGroupLayoutBinding)
        
        createGroupLayoutBinding.toggle.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            when (checkedId) {
                R.id.radio_public -> {
                    groupType.set(UIKitConstants.GroupType.PUBLIC)
                    hidePasswordField(createGroupLayoutBinding)
                }
                R.id.radio_private -> {
                    groupType.set(UIKitConstants.GroupType.PRIVATE)
                    hidePasswordField(createGroupLayoutBinding)
                }
                R.id.radio_password -> {
                    groupType.set(UIKitConstants.GroupType.PASSWORD)
                    showPasswordField(createGroupLayoutBinding)
                }
            }
        }
        
        createGroupLayoutBinding.createGroupBtn.setOnClickListener {
            val group = Group().apply {
                guid = System.currentTimeMillis().toString()
                name = createGroupLayoutBinding.etName.text.toString().trim()
                this.groupType = groupType.get()
                password = createGroupLayoutBinding.etPassword.text.toString().trim()
            }
            viewModel.createGroup(group)
        }
        
        Utils.showBottomSheet(requireContext(), bottomSheetDialog!!, true, false, createGroupLayoutBinding.root)
        bottomSheetDialog?.behavior?.peekHeight = 1000
        bottomSheetDialog?.show()
        
        bottomSheetDialog?.setOnDismissListener {
            bottomSheetDialog = null
        }
    }

    /**
     * Applies theme colors to create group dialog.
     */
    private fun applyCreateGroupThemeColors(b: DialogCreateGroupBinding) {
        val ctx = requireContext()
        b.createGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(ctx))
        b.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(ctx))
        b.ivScopeChange.imageTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(ctx))
        b.ivScopeChangeCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
        b.cometchatScopeChangeTitle.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.scopeType.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        
        val selectedColor = CometChatTheme.getPrimaryColor(ctx)
        val unselectedColor = CometChatTheme.getTextColorSecondary(ctx)
        updateRadioButtonTextColors(b, selectedColor, unselectedColor)
        
        b.tvName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.groupNameCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
        b.etName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.etName.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
        b.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
        b.groupPasswordCard.setStrokeColor(ColorStateList.valueOf(CometChatTheme.getStrokeColorLight(ctx)))
        b.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
        b.tvError.setTextColor(CometChatTheme.getErrorColor(ctx))
        b.createGroupBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(ctx))
        b.createGroupBtnText.setTextColor(CometChatTheme.getColorWhite(ctx))
        b.createGroupProgress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(ctx))
    }

    /**
     * Updates the text color of radio buttons.
     */
    private fun updateRadioButtonTextColors(b: DialogCreateGroupBinding, selectedColor: Int, unselectedColor: Int) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(selectedColor, unselectedColor)
        val colorStateList = ColorStateList(states, colors)
        
        b.radioPublic.setTextColor(colorStateList)
        b.radioPrivate.setTextColor(colorStateList)
        b.radioPassword.setTextColor(colorStateList)
    }

    private fun hidePasswordField(b: DialogCreateGroupBinding) {
        b.groupPasswordCard.visibility = View.GONE
        b.etPassword.visibility = View.GONE
        b.tvPassword.visibility = View.GONE
    }

    private fun showPasswordField(b: DialogCreateGroupBinding) {
        b.groupPasswordCard.visibility = View.VISIBLE
        b.etPassword.visibility = View.VISIBLE
        b.tvPassword.visibility = View.VISIBLE
    }

    /**
     * Opens a dialog for joining a password-protected group.
     */
    private fun openJoinPasswordGroupDialog(group: Group) {
        val joinPasswordGroupLayoutBinding = DialogJoinPasswordGroupBinding.inflate(layoutInflater)
        tvError = joinPasswordGroupLayoutBinding.tvError
        tvButtonText = joinPasswordGroupLayoutBinding.joinGroupBtnText
        progressBar = joinPasswordGroupLayoutBinding.joinGroupProgress
        
        val shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        joinPasswordGroupLayoutBinding.joinGroupCard.shapeAppearanceModel = shapeAppearanceModel
        joinPasswordGroupLayoutBinding.groupAvatar.setAvatar(group.name, group.icon)
        joinPasswordGroupLayoutBinding.tvGroupName.text = group.name
        joinPasswordGroupLayoutBinding.tvMemberCount.text = if (group.membersCount > 1) {
            "${group.membersCount} ${resources.getString(com.cometchat.uikit.kotlin.R.string.cometchat_members)}"
        } else {
            "${group.membersCount} ${resources.getString(com.cometchat.uikit.kotlin.R.string.cometchat_member)}"
        }
        
        // Apply theme colors
        applyJoinGroupThemeColors(joinPasswordGroupLayoutBinding)
        
        joinPasswordGroupLayoutBinding.joinButton.setOnClickListener {
            viewModel.joinPasswordGroup(group, joinPasswordGroupLayoutBinding.etPassword.text.toString().trim())
        }
        
        if (bottomSheetDialog == null) {
            bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        }
        
        Utils.showBottomSheet(requireContext(), bottomSheetDialog!!, true, false, joinPasswordGroupLayoutBinding.root)
        bottomSheetDialog?.show()
        
        bottomSheetDialog?.setOnDismissListener {
            bottomSheetDialog = null
        }
    }

    /**
     * Applies theme colors to join group dialog.
     */
    private fun applyJoinGroupThemeColors(b: DialogJoinPasswordGroupBinding) {
        val ctx = requireContext()
        b.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(ctx))
        b.joinGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(ctx))
        b.tvJoinGroupTitle.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.tvGroupName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.tvMemberCount.setTextColor(CometChatTheme.getTextColorSecondary(ctx))
        b.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
        b.groupPasswordCard.setStrokeColor(ColorStateList.valueOf(CometChatTheme.getStrokeColorLight(ctx)))
        b.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
        b.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
        b.tvError.setTextColor(CometChatTheme.getErrorColor(ctx))
        b.joinButton.setCardBackgroundColor(CometChatTheme.getPrimaryColor(ctx))
        b.joinGroupBtnText.setTextColor(CometChatTheme.getColorWhite(ctx))
        b.joinGroupProgress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(ctx))
    }

    /**
     * Updates the dialog state based on the provided status.
     */
    private fun setDialogState(status: DialogState) {
        if (tvError == null || tvButtonText == null || progressBar == null) return
        when (status) {
            DialogState.INITIATED -> {
                tvError?.visibility = View.GONE
                tvButtonText?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
            }
            DialogState.SUCCESS -> {
                if (bottomSheetDialog?.isShowing == true) {
                    bottomSheetDialog?.dismiss()
                }
            }
            DialogState.FAILURE -> {
                tvError?.visibility = View.VISIBLE
                tvButtonText?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
        }
    }

    /**
     * Sets the error message displayed in the UI.
     */
    private fun setErrorMessage(message: String) {
        tvError?.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
