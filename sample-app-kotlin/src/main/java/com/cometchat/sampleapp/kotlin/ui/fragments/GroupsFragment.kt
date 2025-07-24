package com.cometchat.sampleapp.kotlin.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.models.Group
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.interfaces.OnItemClick
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.CreateGroupLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.FragmentGroupsBinding
import com.cometchat.sampleapp.kotlin.databinding.JoinPasswordGroupLayoutBinding
import com.cometchat.sampleapp.kotlin.databinding.OverflowMenuLayoutBinding
import com.cometchat.sampleapp.kotlin.ui.activity.MessagesActivity
import com.cometchat.sampleapp.kotlin.viewmodels.GroupsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicReference

/**
 * A fragment that displays a list of groups and allows users to join or create
 * new groups.
 */
class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private lateinit var viewModel: GroupsViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var tvError: TextView? = null
    private var tvButtonText: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider.NewInstanceFactory().create(
            GroupsViewModel::class.java
        )
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Observe the ViewModel for various states and update UI accordingly
        viewModel.joinedGroup.observe(
            viewLifecycleOwner
        ) { group: Group -> this.openGroupChat(group) }
        viewModel.createdGroup.observe(
            viewLifecycleOwner
        ) { group: Group -> this.openGroupChat(group) }
        viewModel.error.observe(viewLifecycleOwner) { message: String? ->
            if (message != null) {
                this.setErrorMessage(message)
            }
        }
        viewModel.dialogState.observe(
            viewLifecycleOwner
        ) { status: DialogState -> this.setDialogState(status) }

        binding.group.onItemClick = object : OnItemClick<Group> {
            override fun click(view: View, position: Int, group: Group) {
                if (group.isJoined) {
                    openGroupChat(group)
                } else {
                    if (group.groupType.equals(
                            UIKitConstants.GroupType.PUBLIC, ignoreCase = true
                        )
                    ) {
                        viewModel.joinPasswordGroup(group, "")
                    } else if (group.groupType.equals(
                            UIKitConstants.GroupType.PASSWORD, ignoreCase = true
                        )
                    ) {
                        openJoinPasswordGroupDialog(group)
                    }
                }
            }
        }
    }

    /**
     * Opens the chat interface for the specified group.
     *
     * @param group
     * The group to open the chat for.
     */
    private fun openGroupChat(group: Group) {
        if (group.isJoined) {
            if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) bottomSheetDialog!!.dismiss()
            val intent = Intent(requireActivity(), MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
            startActivity(intent)
        }
    }

    /** Sets up the overflow menu for creating a new group.  */
    private fun setOverFlowMenu() {
        val overflowMenuLayoutBinding = OverflowMenuLayoutBinding.inflate(
            layoutInflater
        )
        overflowMenuLayoutBinding.ivMenu.imageTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(context))
        overflowMenuLayoutBinding.ivMenu.setOnClickListener {
            if (bottomSheetDialog == null) {
                bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
            }
            openCreateGroupDialog()
        }
        binding.group.overflowMenu = overflowMenuLayoutBinding.root
    }

    /** Opens a dialog for creating a new group.  */
    private fun openCreateGroupDialog() {
        val createGroupLayoutBinding = CreateGroupLayoutBinding.inflate(
            layoutInflater
        )
        tvError = createGroupLayoutBinding.tvError
        tvButtonText = createGroupLayoutBinding.createGroupBtnText
        progressBar = createGroupLayoutBinding.createGroupProgress
        val shapeAppearanceModel = ShapeAppearanceModel().toBuilder().setTopLeftCorner(
            CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_radius_4).toFloat()
        ).setTopRightCorner(
            CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_radius_4).toFloat()
        ).setBottomLeftCorner(CornerFamily.ROUNDED, 0f).setBottomRightCorner(CornerFamily.ROUNDED, 0f).build()
        createGroupLayoutBinding.createGroupCard.shapeAppearanceModel = shapeAppearanceModel
        val groupType = AtomicReference(UIKitConstants.GroupType.PUBLIC)


        createGroupLayoutBinding.createGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(context))
        createGroupLayoutBinding.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(context))
        createGroupLayoutBinding.ivScopeChange.imageTintList = ColorStateList.valueOf(CometChatTheme.getIconTintHighlight(context))
        createGroupLayoutBinding.ivScopeChangeCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        createGroupLayoutBinding.cometchatScopeChangeTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
        createGroupLayoutBinding.scopeType.setTextColor(CometChatTheme.getTextColorPrimary(context))

        val selectedColor = CometChatTheme.getPrimaryColor(context)
        val unselectedColor = CometChatTheme.getTextColorSecondary(context)
        updateRadioButtonTextColors(createGroupLayoutBinding, selectedColor, unselectedColor)
        createGroupLayoutBinding.tvName.setTextColor(CometChatTheme.getTextColorPrimary(context))
        createGroupLayoutBinding.groupNameCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        createGroupLayoutBinding.etName.setTextColor(CometChatTheme.getTextColorPrimary(context))
        createGroupLayoutBinding.etName.setHintTextColor(CometChatTheme.getTextColorTertiary(context))
        createGroupLayoutBinding.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(context))
        createGroupLayoutBinding.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        createGroupLayoutBinding.groupPasswordCard.setStrokeColor(
            ColorStateList.valueOf(
                CometChatTheme.getStrokeColorLight(
                    context
                )
            )
        )
        createGroupLayoutBinding.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(context))
        createGroupLayoutBinding.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(context))
        createGroupLayoutBinding.tvError.setTextColor(CometChatTheme.getErrorColor(context))
        createGroupLayoutBinding.createGroupBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(context))
        createGroupLayoutBinding.createGroupBtnText.setTextColor(CometChatTheme.getColorWhite(context))
        createGroupLayoutBinding.createGroupProgress.indeterminateTintList = ColorStateList.valueOf(
            CometChatTheme.getIconTintSecondary(
                context
            )
        )



        createGroupLayoutBinding.toggle.setOnCheckedChangeListener { radio: RadioGroup?, i: Int ->
            when (i) {
                R.id.radio_public -> {
                    groupType.set(UIKitConstants.GroupType.PUBLIC)
                    hidePasswordField(createGroupLayoutBinding)
                    scrollCreateGroupToBottom(createGroupLayoutBinding, 0)
                }

                R.id.radio_private -> {
                    groupType.set(UIKitConstants.GroupType.PRIVATE)
                    hidePasswordField(createGroupLayoutBinding)
                    scrollCreateGroupToBottom(createGroupLayoutBinding, 0)
                }

                R.id.radio_password -> {
                    groupType.set(UIKitConstants.GroupType.PASSWORD)
                    showPasswordField(createGroupLayoutBinding)
                    scrollCreateGroupToBottom(createGroupLayoutBinding, 0)
                }
            }
        }
        createGroupLayoutBinding.createGroupBtn.setOnClickListener { view: View? ->
            val group = Group()
            group.guid = System.currentTimeMillis().toString()
            group.name = createGroupLayoutBinding.etName.text.toString().trim { it <= ' ' }
            group.groupType = groupType.get()
            group.password = createGroupLayoutBinding.etPassword.text.toString().trim { it <= ' ' }
            viewModel.createGroup(group)
        }
        Utils.showBottomSheet(
            context, bottomSheetDialog!!, true, false, createGroupLayoutBinding.root
        )
        bottomSheetDialog!!.behavior.peekHeight = 1000

        bottomSheetDialog!!.show()

        bottomSheetDialog!!.setOnDismissListener {
            bottomSheetDialog = null
        }

        createGroupLayoutBinding.etName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollCreateGroupToBottom(createGroupLayoutBinding, 200)
            }
        }
        createGroupLayoutBinding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollCreateGroupToBottom(createGroupLayoutBinding, 200)
            }
        }
    }

    /**
     * Updates the text color of radio buttons in the group type toggle.
     *
     * @param binding         The create group layout binding
     * @param selectedColor   The color to use for the selected state
     * @param unselectedColor The color to use for the unselected state
     */
    private fun updateRadioButtonTextColors(binding: CreateGroupLayoutBinding, selectedColor: Int, unselectedColor: Int) {
        // Create a color state list for the radio buttons
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // checked state
            intArrayOf(-android.R.attr.state_checked) // unchecked state
        )

        val colors = intArrayOf(
            selectedColor,
            unselectedColor
        )

        val colorStateList = ColorStateList(states, colors)

        // Apply the color state list to all radio buttons
        binding.radioPublic.setTextColor(colorStateList)
        binding.radioPrivate.setTextColor(colorStateList)
        binding.radioPassword.setTextColor(colorStateList)
    }


    /**
     * Hides the password field in the create group dialog.
     *
     * @param createGroupLayoutBinding
     * The binding object for the create group layout.
     */
    private fun hidePasswordField(createGroupLayoutBinding: CreateGroupLayoutBinding) {
        createGroupLayoutBinding.groupPasswordCard.visibility = View.GONE
        createGroupLayoutBinding.etPassword.visibility = View.GONE
        createGroupLayoutBinding.tvPassword.visibility = View.GONE
    }

    /**
     * Shows the password field in the create group dialog.
     *
     * @param createGroupLayoutBinding
     * The binding object for the create group layout.
     */
    private fun showPasswordField(createGroupLayoutBinding: CreateGroupLayoutBinding) {
        createGroupLayoutBinding.groupPasswordCard.visibility = View.VISIBLE
        createGroupLayoutBinding.etPassword.visibility = View.VISIBLE
        createGroupLayoutBinding.tvPassword.visibility = View.VISIBLE
    }

    /**
     * Opens a dialog for joining a password-protected group.
     *
     * @param group
     * The group that requires a password to join.
     */
    private fun openJoinPasswordGroupDialog(group: Group) {
        val joinPasswordGroupLayoutBinding = JoinPasswordGroupLayoutBinding.inflate(layoutInflater)
        tvError = joinPasswordGroupLayoutBinding.tvError
        tvButtonText = joinPasswordGroupLayoutBinding.joinGroupBtnText
        progressBar = joinPasswordGroupLayoutBinding.joinGroupProgress
        val shapeAppearanceModel = ShapeAppearanceModel().toBuilder().setTopLeftCorner(
            CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_radius_4).toFloat()
        ).setTopRightCorner(
            CornerFamily.ROUNDED, resources.getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_radius_4).toFloat()
        ).setBottomLeftCorner(CornerFamily.ROUNDED, 0f).setBottomRightCorner(CornerFamily.ROUNDED, 0f).build()
        joinPasswordGroupLayoutBinding.joinGroupCard.shapeAppearanceModel = shapeAppearanceModel
        joinPasswordGroupLayoutBinding.groupAvatar.setAvatar(group.name, group.icon)
        joinPasswordGroupLayoutBinding.tvGroupName.text = group.name
        joinPasswordGroupLayoutBinding.tvMemberCount.text =
            if (group.membersCount > 1) (group.membersCount.toString() + " " + resources.getString(com.cometchat.chatuikit.R.string.cometchat_members))
            else (group.membersCount.toString() + " " + resources.getString(com.cometchat.chatuikit.R.string.cometchat_member))

        joinPasswordGroupLayoutBinding.dragHandle.setCardBackgroundColor(CometChatTheme.getNeutralColor500(context))
        joinPasswordGroupLayoutBinding.joinGroupCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(context))
        joinPasswordGroupLayoutBinding.tvJoinGroupTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
        joinPasswordGroupLayoutBinding.tvGroupName.setTextColor(CometChatTheme.getTextColorPrimary(context))
        joinPasswordGroupLayoutBinding.tvMemberCount.setTextColor(CometChatTheme.getTextColorSecondary(context))
        joinPasswordGroupLayoutBinding.tvPassword.setTextColor(CometChatTheme.getTextColorPrimary(context))
        joinPasswordGroupLayoutBinding.groupPasswordCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor2(context))
        joinPasswordGroupLayoutBinding.groupPasswordCard.setStrokeColor(
            ColorStateList.valueOf(
                CometChatTheme.getStrokeColorLight(
                    context
                )
            )
        )
        joinPasswordGroupLayoutBinding.etPassword.setTextColor(CometChatTheme.getTextColorPrimary(context))
        joinPasswordGroupLayoutBinding.etPassword.setHintTextColor(CometChatTheme.getTextColorTertiary(context))
        joinPasswordGroupLayoutBinding.tvError.setTextColor(CometChatTheme.getErrorColor(context))
        joinPasswordGroupLayoutBinding.joinButton.setCardBackgroundColor(CometChatTheme.getPrimaryColor(context))
        joinPasswordGroupLayoutBinding.joinGroupBtnText.setTextColor(CometChatTheme.getColorWhite(context))
        joinPasswordGroupLayoutBinding.joinGroupProgress.indeterminateTintList = ColorStateList.valueOf(
            CometChatTheme.getIconTintSecondary(
                context
            )
        )
        joinPasswordGroupLayoutBinding.joinButton.setOnClickListener { view: View? ->
            viewModel.joinPasswordGroup(group, joinPasswordGroupLayoutBinding.etPassword.text.toString().trim { it <= ' ' })
        }
        if (bottomSheetDialog == null) {
            bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        }

        Utils.showBottomSheet(
            context, bottomSheetDialog!!, true, false, joinPasswordGroupLayoutBinding.root
        )
        bottomSheetDialog!!.show()
        bottomSheetDialog!!.setOnDismissListener {
            bottomSheetDialog = null
        }
    }

    /**
     * Updates the dialog state based on the provided status.
     *
     * @param status
     * The dialog state to update.
     */
    private fun setDialogState(status: DialogState) {
        if (tvError == null || tvButtonText == null || progressBar == null) return
        when (status) {
            DialogState.INITIATED -> {
                tvError!!.visibility = View.GONE
                tvButtonText!!.visibility = View.GONE
                progressBar!!.visibility = View.VISIBLE
            }

            DialogState.SUCCESS -> if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
                bottomSheetDialog!!.dismiss()
            }

            DialogState.FAILURE -> {
                tvError!!.visibility = View.VISIBLE
                tvButtonText!!.visibility = View.VISIBLE
                progressBar!!.visibility = View.GONE
            }
        }
    }

    /**
     * Sets the error message displayed in the UI.
     *
     * @param message
     * The error message to display.
     */
    private fun setErrorMessage(message: String) {
        if (tvError != null) tvError!!.text = message
    }

    private fun scrollCreateGroupToBottom(createGroupLayoutBinding: CreateGroupLayoutBinding, delay: Int) {
        createGroupLayoutBinding.createGroupScrollLayout.postDelayed({
            createGroupLayoutBinding.createGroupScrollLayout.scrollTo(
                0,
                createGroupLayoutBinding.createGroupScrollLayout.height
            )
        }, delay.toLong())
    }
}
