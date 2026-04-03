package com.cometchat.sampleapp.kotlin.ui.groups

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.Group
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.CreateGroupLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.FragmentGroupsBinding
import com.cometchat.sampleapp.kotlin.databinding.JoinPasswordGroupLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.OverflowMenuLayoutBinding
import com.cometchat.sampleapp.kotlin.ui.messages.MessagesActivity
import com.cometchat.uikit.core.constants.UIKitConstants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * Fragment displaying the list of groups (Groups tab).
 *
 * This fragment uses the CometChatGroups component from chatuikit-kotlin
 * to display all available groups. It handles:
 * - Displaying group list with avatars, names, and member counts
 * - Search functionality to filter groups
 * - Navigation to message screen on group tap
 * - Create group functionality via overflow menu
 * - Join password-protected groups
 * - Loading and empty states
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Uses GroupsViewModel for join/create group operations
 * - Delegates list display to CometChatGroups component
 *
 * @see com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroups
 */
class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GroupsViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null

    // Dialog UI references
    private var tvError: TextView? = null
    private var tvButtonText: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GroupsViewModel::class.java]
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupGroupsList()
        setOverFlowMenu()
    }

    /**
     * Observes ViewModel LiveData for join/create group operations.
     */
    private fun observeViewModel() {
        viewModel.joinedGroup.observe(viewLifecycleOwner) { group ->
            openGroupChat(group)
        }
        viewModel.createdGroup.observe(viewLifecycleOwner) { group ->
            openGroupChat(group)
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            setErrorMessage(message)
        }
        viewModel.dialogState.observe(viewLifecycleOwner) { state ->
            setDialogState(state)
        }
    }

    /**
     * Sets up the CometChatGroups component with click handlers.
     */
    private fun setupGroupsList() {
        binding.groupsList.setOnItemClick { group ->
            // Handle group item click based on join status
            if (group.isJoined) {
                openGroupChat(group)
            } else {
                when {
                    group.groupType.equals(UIKitConstants.GroupType.PUBLIC, ignoreCase = true) -> {
                        viewModel.joinPasswordGroup(group, "")
                    }
                    group.groupType.equals(UIKitConstants.GroupType.PASSWORD, ignoreCase = true) -> {
                        openJoinPasswordGroupDialog(group)
                    }
                }
            }
        }
    }

    /**
     * Opens the chat interface for the specified group.
     */
    private fun openGroupChat(group: Group) {
        if (group.isJoined) {
            bottomSheetDialog?.takeIf { it.isShowing }?.dismiss()
            MessagesActivity.start(requireContext(), group = group)
        }
    }

    /**
     * Sets the error message displayed in the dialog.
     */
    private fun setErrorMessage(message: String?) {
        tvError?.text = message ?: getString(R.string.app_something_went_wrong)
    }

    /**
     * Updates the dialog state based on the operation status.
     */
    private fun setDialogState(status: UIKitConstants.DialogState) {
        if (tvError == null || tvButtonText == null || progressBar == null) return
        
        when (status) {
            UIKitConstants.DialogState.INITIATED -> {
                tvError?.visibility = View.GONE
                tvButtonText?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
            }
            UIKitConstants.DialogState.SUCCESS -> {
                bottomSheetDialog?.takeIf { it.isShowing }?.dismiss()
            }
            UIKitConstants.DialogState.FAILURE -> {
                tvError?.visibility = View.VISIBLE
                tvButtonText?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
        }
    }

    /**
     * Opens a dialog for joining a password-protected group.
     */
    private fun openJoinPasswordGroupDialog(group: Group) {
        val joinBinding = JoinPasswordGroupLayoutBinding.inflate(layoutInflater)
        
        tvError = joinBinding.tvError
        tvButtonText = joinBinding.joinGroupBtnText
        progressBar = joinBinding.joinGroupProgress

        // Set corner radius for bottom sheet
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        joinBinding.joinGroupCard.shapeAppearanceModel = shapeAppearanceModel

        // Set group info
        joinBinding.groupAvatar.setAvatar(group.name, group.icon)
        joinBinding.tvGroupName.text = group.name
        val memberText = if (group.membersCount > 1) {
            "${group.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_members)}"
        } else {
            "${group.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_member)}"
        }
        joinBinding.tvMemberCount.text = memberText

        // Apply theme colors
        context?.let { ctx ->
            joinBinding.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(ctx))
            joinBinding.joinGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(ctx))
            joinBinding.tvJoinGroupTitle.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            joinBinding.tvGroupName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            joinBinding.tvMemberCount.setTextColor(CometChatTheme.getTextColorSecondary(ctx))
            joinBinding.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            joinBinding.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            joinBinding.groupPasswordCard.strokeColor = CometChatTheme.getStrokeColorLight(ctx)
            joinBinding.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            joinBinding.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
            joinBinding.tvError.setTextColor(CometChatTheme.getErrorColor(ctx))
            joinBinding.joinButton.setCardBackgroundColor(CometChatTheme.getPrimaryColor(ctx))
            joinBinding.joinGroupBtnText.setTextColor(CometChatTheme.getColorWhite(ctx))
            joinBinding.joinGroupProgress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(ctx))
        }

        // Join button click
        joinBinding.joinButton.setOnClickListener {
            viewModel.joinPasswordGroup(group, joinBinding.etPassword.text.toString().trim())
        }

        if (bottomSheetDialog == null) {
            bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        }

        bottomSheetDialog?.let { dialog ->
            Utils.showBottomSheet(requireContext(), dialog, true, false, joinBinding.root)
            dialog.show()
            dialog.setOnDismissListener { bottomSheetDialog = null }
        }
    }

    /**
     * Sets up the overflow menu for creating a new group.
     */
    private fun setOverFlowMenu() {
        val overflowBinding = OverflowMenuLayoutBinding.inflate(layoutInflater)
        
        context?.let { ctx ->
            overflowBinding.ivMenu.imageTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(ctx))
        }
        
        overflowBinding.ivMenu.setOnClickListener {
            if (bottomSheetDialog == null) {
                bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
            }
            openCreateGroupDialog()
        }
        
        binding.groupsList.setOverflowMenu(overflowBinding.root)
    }

    /**
     * Opens a dialog for creating a new group.
     */
    private fun openCreateGroupDialog() {
        val createBinding = CreateGroupLayoutBinding.inflate(layoutInflater)
        
        tvError = createBinding.tvError
        tvButtonText = createBinding.createGroupBtnText
        progressBar = createBinding.createGroupProgress

        // Set corner radius for bottom sheet
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_4).toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        createBinding.createGroupCard.shapeAppearanceModel = shapeAppearanceModel

        var groupType = UIKitConstants.GroupType.PUBLIC

        // Apply theme colors
        context?.let { ctx ->
            createBinding.createGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(ctx))
            createBinding.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(ctx))
            createBinding.ivScopeChange.imageTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(ctx))
            createBinding.ivScopeChangeCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            createBinding.cometchatScopeChangeTitle.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            createBinding.scopeType.setTextColor(CometChatTheme.getTextColorPrimary(ctx))

            val selectedColor = CometChatTheme.getPrimaryColor(ctx)
            val unselectedColor = CometChatTheme.getTextColorSecondary(ctx)
            updateRadioButtonTextColors(createBinding, selectedColor, unselectedColor)

            createBinding.tvName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            createBinding.groupNameCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            createBinding.etName.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            createBinding.etName.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
            createBinding.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            createBinding.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(ctx))
            createBinding.groupPasswordCard.strokeColor = CometChatTheme.getStrokeColorLight(ctx)
            createBinding.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(ctx))
            createBinding.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(ctx))
            createBinding.tvError.setTextColor(CometChatTheme.getErrorColor(ctx))
            createBinding.createGroupBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(ctx))
            createBinding.createGroupBtnText.setTextColor(CometChatTheme.getColorWhite(ctx))
            createBinding.createGroupProgress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(ctx))
        }

        // Radio group listener for group type selection
        createBinding.toggle.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_public -> {
                    groupType = UIKitConstants.GroupType.PUBLIC
                    hidePasswordField(createBinding)
                    scrollCreateGroupToBottom(createBinding, 0)
                }
                R.id.radio_private -> {
                    groupType = UIKitConstants.GroupType.PRIVATE
                    hidePasswordField(createBinding)
                    scrollCreateGroupToBottom(createBinding, 0)
                }
                R.id.radio_password -> {
                    groupType = UIKitConstants.GroupType.PASSWORD
                    showPasswordField(createBinding)
                    scrollCreateGroupToBottom(createBinding, 0)
                }
            }
        }

        // Create button click
        createBinding.createGroupBtn.setOnClickListener {
            val group = Group().apply {
                guid = System.currentTimeMillis().toString()
                name = createBinding.etName.text.toString().trim()
                setGroupType(groupType)
                password = createBinding.etPassword.text.toString().trim()
            }
            viewModel.createGroup(group)
        }

        Utils.showBottomSheet(requireContext(), bottomSheetDialog!!, true, false, createBinding.root)
        bottomSheetDialog?.behavior?.peekHeight = 1000
        bottomSheetDialog?.show()
        bottomSheetDialog?.setOnDismissListener { bottomSheetDialog = null }

        // Focus listeners for scrolling
        createBinding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollCreateGroupToBottom(createBinding, 200)
        }
        createBinding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollCreateGroupToBottom(createBinding, 200)
        }
    }

    /**
     * Updates the text color of radio buttons in the group type toggle.
     */
    private fun updateRadioButtonTextColors(
        binding: CreateGroupLayoutBinding,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(selectedColor, unselectedColor)
        val colorStateList = ColorStateList(states, colors)

        binding.radioPublic.setTextColor(colorStateList)
        binding.radioPrivate.setTextColor(colorStateList)
        binding.radioPassword.setTextColor(colorStateList)
    }

    /**
     * Hides the password field in the create group dialog.
     */
    private fun hidePasswordField(binding: CreateGroupLayoutBinding) {
        binding.groupPasswordCard.visibility = View.GONE
        binding.etPassword.visibility = View.GONE
        binding.tvPassword.visibility = View.GONE
    }

    /**
     * Shows the password field in the create group dialog.
     */
    private fun showPasswordField(binding: CreateGroupLayoutBinding) {
        binding.groupPasswordCard.visibility = View.VISIBLE
        binding.etPassword.visibility = View.VISIBLE
        binding.tvPassword.visibility = View.VISIBLE
    }

    /**
     * Scrolls the create group dialog to the bottom.
     */
    private fun scrollCreateGroupToBottom(binding: CreateGroupLayoutBinding, delay: Long) {
        binding.createGroupScrollLayout.postDelayed({
            binding.createGroupScrollLayout.scrollTo(0, binding.createGroupScrollLayout.height)
        }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        _binding = null
    }
}
