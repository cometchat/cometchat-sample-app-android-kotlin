package com.cometchat.sampleapp.kotlin.ui.groups

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.BannedGroupMembersRequest
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityGroupDetailsBinding
import com.cometchat.sampleapp.kotlin.databinding.DialogAddMembersBinding
import com.cometchat.sampleapp.kotlin.databinding.DialogBannedMembersBinding
import com.cometchat.sampleapp.kotlin.databinding.DialogGroupMembersBinding
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.launch

/**
 * Activity for displaying group details.
 *
 * This activity displays detailed information about a group:
 * - Group avatar, name, and member count
 * - Three action cards: View Members, Add Members, Banned Members
 * - Leave group functionality (for non-owners)
 * - Delete & Exit functionality (for owners)
 * - Delete chat option
 *
 * ## Features:
 * - Display group information
 * - Action cards for member management (visibility based on role)
 * - Leave group option for regular members
 * - Delete & Exit option for owners
 * - Delete chat option (when lastMessage exists)
 * - Info banner for "no longer part of this group"
 *
 * ## Usage:
 * ```kotlin
 * GroupDetailsActivity.start(context, group)
 * GroupDetailsActivity.start(context, group, lastMessage)
 * ```
 *
 * Matches master-app-kotlin2 UI parity.
 * Validates: Requirements 7.4, 7.5, 7.6, 11.1, 11.2, 11.3, 11.4, 11.6
 */
class GroupDetailsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_GROUP_ID = "extra_group_id"
        private const val EXTRA_HAS_LAST_MESSAGE = "extra_has_last_message"

        /**
         * Starts GroupDetailsActivity with a Group object.
         *
         * @param context The context to start the activity from
         * @param group The group to display details for
         * @param lastMessage Optional last message (determines delete chat visibility)
         */
        fun start(context: Context, group: Group, lastMessage: BaseMessage? = null) {
            val intent = Intent(context, GroupDetailsActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, group.guid)
                putExtra(EXTRA_HAS_LAST_MESSAGE, lastMessage != null)
            }
            context.startActivity(intent)
        }

        /**
         * Starts GroupDetailsActivity with a group ID.
         *
         * @param context The context to start the activity from
         * @param groupId The GUID of the group to display
         * @param hasLastMessage Whether there's a last message (determines delete chat visibility)
         */
        fun startWithGroupId(context: Context, groupId: String, hasLastMessage: Boolean = false) {
            val intent = Intent(context, GroupDetailsActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
                putExtra(EXTRA_HAS_LAST_MESSAGE, hasLastMessage)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityGroupDetailsBinding
    private var group: Group? = null
    private var isOwner: Boolean = false
    private var isAdmin: Boolean = false
    private var isModerator: Boolean = false
    private var hasLastMessage: Boolean = false
    private var confirmDialog: CometChatConfirmDialog? = null
    private var dialog: Dialog? = null
    private var viewModel: GroupDetailsViewModel? = null
    
    // Dialog views for state management
    private var tvError: TextView? = null
    private var progressBar: ProgressBar? = null
    private var btnText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hasLastMessage = intent.getBooleanExtra(EXTRA_HAS_LAST_MESSAGE, false)

        initViewModel()
        applyWindowInsets()
        setupToolbar()
        setupClickListeners()
        loadGroupData()
    }

    /**
     * Initializes the ViewModel and observes its state.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[GroupDetailsViewModel::class.java]
        
        viewModel?.dialogState?.observe(this) { state ->
            setDialogState(state)
        }
        
        viewModel?.errorMessage?.observe(this) { error ->
            error?.let { setError(it) }
        }
        
        viewModel?.updatedGroup?.observe(this) { updatedGroup ->
            updatedGroup?.let {
                group = it
                setupGroupDetails()
                updateUIBasedOnRole()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel?.removeListeners()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Sets up click listeners for action cards and options.
     */
    private fun setupClickListeners() {
        // View Members card
        binding.viewMembersCard.setOnClickListener {
            showGroupMembersDialog()
        }

        // Add Members card
        binding.viewAddMembersCard.setOnClickListener {
            showAddMembersDialog()
        }

        // Banned Members card
        binding.viewBannedMembersCard.setOnClickListener {
            showBannedMembersDialog()
        }

        // Leave Group option (for non-owners)
        binding.leaveGroupLay.setOnClickListener {
            showLeaveGroupConfirmation()
        }

        // Delete Chat option
        binding.tvDeleteChat.setOnClickListener {
            showDeleteChatConfirmation()
        }

        // Delete & Exit option (for owners)
        binding.deleteGroupLay.setOnClickListener {
            showDeleteGroupConfirmation()
        }
    }

    /**
     * Shows the group members dialog.
     */
    private fun showGroupMembersDialog() {
        group?.let { g ->
            val dialogBinding = DialogGroupMembersBinding.inflate(layoutInflater)
            dialogBinding.viewMembers.setGroup(g)
            
            val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
            alertDialog.setView(dialogBinding.root)
            dialog = alertDialog.create()
            dialog?.show()
            
            dialogBinding.viewMembers.setOnBackPress { dialog?.dismiss() }
        }
    }

    /**
     * Shows the add members dialog.
     */
    private fun showAddMembersDialog() {
        group?.let { g ->
            val dialogBinding = DialogAddMembersBinding.inflate(layoutInflater)
            
            dialogBinding.addMembersBtn.setCardBackgroundColor(CometChatTheme.getPrimaryColor(this))
            dialogBinding.tvAddMembers.setTextColor(CometChatTheme.getColorWhite(this))
            dialogBinding.progress.indeterminateTintList = ColorStateList.valueOf(CometChatTheme.getIconTintSecondary(this))
            
            tvError = dialogBinding.tvError
            progressBar = dialogBinding.progress
            btnText = dialogBinding.tvAddMembers
            
            val addMembersTitle = getString(R.string.add_members)
            
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
                    "${selectedUsers.size} ${getString(R.string.selected)}"
                }
                dialogBinding.addMembers.setTitle(title)
            }
            
            dialogBinding.addMembersBtn.setOnClickListener {
                val selectedUsers = dialogBinding.addMembers.getSelectedUsers()
                if (selectedUsers.isNotEmpty()) {
                    viewModel?.setGroup(g)
                    viewModel?.addMembersToGroup(this, selectedUsers)
                }
            }
            
            val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
            alertDialog.setView(dialogBinding.root)
            dialog = alertDialog.create()
            dialog?.show()
            
            dialogBinding.addMembers.setOnBackPress { dialog?.dismiss() }
        }
    }

    /**
     * Shows the banned members dialog.
     */
    private fun showBannedMembersDialog() {
        group?.let { g ->
            val dialogBinding = DialogBannedMembersBinding.inflate(layoutInflater)
            
            val alertDialog = AlertDialog.Builder(this, androidx.appcompat.R.style.AlertDialog_AppCompat)
            alertDialog.setView(dialogBinding.root)
            dialog = alertDialog.create()
            dialog?.show()
            
            dialogBinding.ivBack.setOnClickListener { dialog?.dismiss() }
            
            // Show loading
            dialogBinding.progressBar.visibility = View.VISIBLE
            dialogBinding.emptyLayout.visibility = View.GONE
            dialogBinding.recyclerBannedMembers.visibility = View.GONE
            
            // Fetch banned members
            fetchBannedMembers(g, dialogBinding)
        }
    }

    /**
     * Fetches banned members for the group.
     */
    private fun fetchBannedMembers(group: Group, dialogBinding: DialogBannedMembersBinding) {
        val request = BannedGroupMembersRequest.BannedGroupMembersRequestBuilder(group.guid)
            .setLimit(30)
            .build()
        
        request.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
            override fun onSuccess(members: List<GroupMember>?) {
                dialogBinding.progressBar.visibility = View.GONE
                
                if (members.isNullOrEmpty()) {
                    dialogBinding.emptyLayout.visibility = View.VISIBLE
                    dialogBinding.recyclerBannedMembers.visibility = View.GONE
                } else {
                    dialogBinding.emptyLayout.visibility = View.GONE
                    dialogBinding.recyclerBannedMembers.visibility = View.VISIBLE
                    
                    // Set up RecyclerView with banned members
                    val adapter = BannedMembersAdapter(members.toMutableList()) { member ->
                        showUnbanConfirmation(member, group, dialogBinding)
                    }
                    dialogBinding.recyclerBannedMembers.layoutManager = LinearLayoutManager(this@GroupDetailsActivity)
                    dialogBinding.recyclerBannedMembers.adapter = adapter
                }
            }
            
            override fun onError(e: CometChatException?) {
                dialogBinding.progressBar.visibility = View.GONE
                dialogBinding.emptyLayout.visibility = View.VISIBLE
            }
        })
    }

    /**
     * Shows confirmation dialog for unbanning a member.
     */
    private fun showUnbanConfirmation(member: GroupMember, group: Group, dialogBinding: DialogBannedMembersBinding) {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setTitleText("Unban ${member.name}")
            setSubtitleText("Are you sure you want to unban ${member.name}?")
            setPositiveButtonText("Unban")
            setPositiveButtonBackgroundColor(CometChatTheme.getPrimaryColor(this@GroupDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                unbanMember(member, group, dialogBinding)
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Unbans a member from the group.
     */
    private fun unbanMember(member: GroupMember, group: Group, dialogBinding: DialogBannedMembersBinding) {
        CometChat.unbanGroupMember(member.uid, group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                confirmDialog?.dismiss()
                // Refresh the banned members list
                fetchBannedMembers(group, dialogBinding)
            }
            
            override fun onError(e: CometChatException?) {
                confirmDialog?.dismiss()
            }
        })
    }

    /**
     * Sets the dialog state based on ViewModel state.
     */
    private fun setDialogState(state: DialogState) {
        tvError?.visibility = View.GONE
        progressBar?.visibility = View.GONE
        btnText?.visibility = View.GONE
        
        when (state) {
            DialogState.INITIATED -> progressBar?.visibility = View.VISIBLE
            DialogState.SUCCESS -> {
                btnText?.visibility = View.VISIBLE
                dialog?.dismiss()
                // Refresh group data
                loadGroupData()
            }
            DialogState.FAILURE -> {
                tvError?.visibility = View.VISIBLE
                btnText?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Sets the error message in the dialog.
     */
    private fun setError(error: String) {
        tvError?.text = error
    }

    /**
     * Loads the group data from CometChat.
     */
    private fun loadGroupData() {
        val groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        if (groupId == null) {
            finish()
            return
        }

        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                group = fetchedGroup
                viewModel?.setGroup(fetchedGroup)
                viewModel?.addListeners()
                checkUserRole()
                setupGroupDetails()
                updateUIBasedOnRole()
            }

            override fun onError(e: CometChatException) {
                finish()
            }
        })
    }

    /**
     * Checks the current user's role in the group.
     */
    private fun checkUserRole() {
        group?.let { g ->
            val loggedInUser = CometChat.getLoggedInUser()
            isOwner = g.owner == loggedInUser?.uid
            
            // Check scope for admin/moderator
            when (g.scope) {
                UIKitConstants.GroupMemberScope.ADMIN -> {
                    isAdmin = true
                    isModerator = false
                }
                UIKitConstants.GroupMemberScope.MODERATOR -> {
                    isAdmin = false
                    isModerator = true
                }
                else -> {
                    isAdmin = false
                    isModerator = false
                }
            }
            
            // Owner is always admin
            if (isOwner) {
                isAdmin = true
            }
        }
    }

    /**
     * Sets up the group details header.
     */
    private fun setupGroupDetails() {
        group?.let { g ->
            // Group Avatar
            binding.groupAvatar.setAvatar(g.name, g.icon)

            // Group Name
            binding.groupName.text = g.name

            // Member count (matching master-app-kotlin2 format)
            val memberText = if (g.membersCount > 1) {
                "${g.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_members)}"
            } else {
                "${g.membersCount} ${getString(com.cometchat.uikit.kotlin.R.string.cometchat_member)}"
            }
            binding.memberCount.text = memberText

            // Show info banner if user is no longer part of the group
            binding.infoMessage.visibility = if (!g.isJoined) View.VISIBLE else View.GONE
        }
    }

    /**
     * Updates UI visibility based on user role.
     * Matches master-app-kotlin2 visibility logic exactly.
     */
    private fun updateUIBasedOnRole() {
        group?.let { g ->
            // Hide all options by default
            binding.viewAddMembers.visibility = View.GONE
            binding.viewBannedMembers.visibility = View.GONE
            binding.deleteGroupLay.visibility = View.GONE
            binding.viewMembers.visibility = View.GONE
            binding.leaveGroupLay.visibility = View.GONE
            
            // Delete chat visibility based on lastMessage
            binding.tvDeleteChat.visibility = if (hasLastMessage) View.VISIBLE else View.GONE

            if (g.isJoined) {
                when (g.scope) {
                    UIKitConstants.GroupMemberScope.PARTICIPANTS -> {
                        // Regular members can view members and leave
                        binding.viewMembers.visibility = View.VISIBLE
                        binding.leaveGroupLay.visibility = View.VISIBLE
                    }
                    UIKitConstants.GroupMemberScope.MODERATOR -> {
                        // Moderators can view members, view banned members, and leave
                        binding.viewMembers.visibility = View.VISIBLE
                        binding.viewBannedMembers.visibility = View.VISIBLE
                        binding.leaveGroupLay.visibility = View.VISIBLE
                    }
                    UIKitConstants.GroupMemberScope.ADMIN -> {
                        // Admins can do everything
                        binding.viewAddMembers.visibility = View.VISIBLE
                        binding.viewBannedMembers.visibility = View.VISIBLE
                        binding.viewMembers.visibility = View.VISIBLE
                        
                        // Show leave if not owner, or delete & exit if owner
                        if (isOwner) {
                            binding.deleteGroupLay.visibility = View.VISIBLE
                            // Owner can only leave if there are other members
                            if (g.membersCount > 1) {
                                binding.leaveGroupLay.visibility = View.VISIBLE
                            }
                        } else {
                            binding.leaveGroupLay.visibility = View.VISIBLE
                            binding.deleteGroupLay.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /**
     * Shows a confirmation dialog before leaving the group.
     */
    private fun showLeaveGroupConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_close, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setTitleText(getString(R.string.leave_group))
            setSubtitleText(getString(R.string.leave_group_confirmation))
            setPositiveButtonText(getString(R.string.leave))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                leaveGroup()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Leaves the current group.
     */
    private fun leaveGroup() {
        group?.let { g ->
            CometChat.leaveGroup(g.guid, object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    confirmDialog?.dismiss()
                    finish()
                }

                override fun onError(e: CometChatException) {
                    confirmDialog?.dismiss()
                }
            })
        }
    }

    /**
     * Shows a confirmation dialog before deleting the chat.
     */
    private fun showDeleteChatConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setTitleText(getString(R.string.delete_chat_title))
            setSubtitleText(getString(R.string.delete_chat_message))
            setPositiveButtonText(getString(R.string.delete))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                deleteChat()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Deletes the chat with the current group.
     */
    private fun deleteChat() {
        group?.let { g ->
            CometChat.deleteConversation(
                g.guid,
                CometChatConstants.CONVERSATION_TYPE_GROUP,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(result: String) {
                        confirmDialog?.dismiss()
                        finish()
                    }

                    override fun onError(e: CometChatException) {
                        confirmDialog?.dismiss()
                    }
                }
            )
        }
    }

    /**
     * Shows a confirmation dialog before deleting the group.
     */
    private fun showDeleteGroupConfirmation() {
        confirmDialog = CometChatConfirmDialog(this, com.cometchat.uikit.kotlin.R.style.CometChatConfirmDialogStyle)
        confirmDialog?.apply {
            setConfirmDialogIcon(androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, 
                com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_delete, 
                null
            ))
            setConfirmDialogIconTint(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setTitleText(getString(R.string.delete_and_exit))
            setSubtitleText(getString(R.string.delete_group_confirmation))
            setPositiveButtonText(getString(R.string.delete))
            setPositiveButtonBackgroundColor(CometChatTheme.getErrorColor(this@GroupDetailsActivity))
            setNegativeButtonText(getString(R.string.cancel))
            setOnPositiveButtonClick {
                showPositiveButtonProgress(true)
                deleteGroup()
            }
            setOnNegativeButtonClick { dismiss() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    /**
     * Deletes the current group.
     */
    private fun deleteGroup() {
        group?.let { g ->
            CometChat.deleteGroup(g.guid, object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    confirmDialog?.dismiss()
                    finish()
                }

                override fun onError(e: CometChatException) {
                    confirmDialog?.dismiss()
                }
            })
        }
    }
}
