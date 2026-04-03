package com.cometchat.uikit.kotlin.presentation.groupmembers.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatGroupMembersViewModelFactory
import com.cometchat.uikit.core.state.DialogState
import com.cometchat.uikit.core.state.GroupMembersEvent
import com.cometchat.uikit.core.state.GroupMembersUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatGroupMembersBinding
import com.cometchat.uikit.kotlin.presentation.groupmembers.adapter.GroupMembersAdapter
import com.cometchat.uikit.kotlin.presentation.groupmembers.scopechange.CometChatScopeChange
import com.cometchat.uikit.kotlin.presentation.groupmembers.utils.GroupMembersUtils
import com.cometchat.uikit.kotlin.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.kotlin.presentation.groupmembers.utils.GroupMembersViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatGroupMembers is a custom Android View that displays a list of group members
 * with support for search, selection, member actions (kick/ban/scope change), and styling.
 *
 * Uses the shared [CometChatGroupMembersViewModel] from chatuikit-core for business logic.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.groupmembers.ui.CometChatGroupMembers
 *     android:id="@+id/groupMembers"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * // Basic usage
 * val groupMembers = findViewById<CometChatGroupMembers>(R.id.groupMembers)
 * groupMembers.setGroup(myGroup)
 *
 * // With callbacks
 * groupMembers.setOnItemClick { member -> viewMemberProfile(member) }
 * groupMembers.setOnError { exception -> showError(exception) }
 *
 * // With selection mode
 * groupMembers.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
 * groupMembers.setOnSelection { selectedMembers -> handleSelection(selectedMembers) }
 *
 * // Disable specific actions
 * groupMembers.setKickMemberOptionVisibility(View.GONE)
 * groupMembers.setBanMemberOptionVisibility(View.GONE)
 *
 * // Custom styling
 * groupMembers.setStyle(CometChatGroupMembersStyle.default(context))
 * groupMembers.setTitleTextColor(Color.BLACK)
 * ```
 *
 * Migration from Java implementation:
 * - LiveData → StateFlow: State is now collected via coroutines instead of LiveData observers
 * - Java → Kotlin: All callbacks use Kotlin lambdas instead of Java interfaces
 * - ViewModel is shared with the Compose implementation via chatuikit-core
 * - Style is now a data class ([CometChatGroupMembersStyle]) instead of individual setter methods
 *
 * @see CometChatGroupMembersViewModel
 * @see CometChatGroupMembersStyle
 * @see GroupMembersAdapter
 */
class CometChatGroupMembers @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatGroupMembersStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatGroupMembers::class.java.simpleName
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    // View Binding
    private val binding: CometchatGroupMembersBinding

    // ViewModel
    private var viewModel: CometChatGroupMembersViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Adapter
    private val membersAdapter: GroupMembersAdapter

    // Lifecycle
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewScope: CoroutineScope? = null

    // Group
    private var group: Group? = null
    private var groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null
    private var searchRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null

    // Selection
    private val selectedMembers = HashMap<GroupMember, Boolean>()
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
    private var isFurtherSelectionEnabled = true

    // Callbacks
    private var onItemClick: ((GroupMember) -> Unit)? = null
    private var onItemLongClick: ((GroupMember) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null
    private var onSelection: ((List<GroupMember>) -> Unit)? = null

    // Custom view listeners
    private var itemViewListener: GroupMembersViewHolderListener? = null
    private var leadingViewListener: GroupMembersViewHolderListener? = null
    private var titleViewListener: GroupMembersViewHolderListener? = null
    private var subtitleViewListener: GroupMembersViewHolderListener? = null
    private var trailingViewListener: GroupMembersViewHolderListener? = null

    // Custom state views
    private var customEmptyView: View? = null
    private var customErrorView: View? = null
    private var customLoadingView: View? = null

    // Menu options
    private var options: ((Context, GroupMember, Group) -> List<CometChatPopupMenu.MenuItem>)? = null
    private var addOptions: ((Context, GroupMember, Group) -> List<CometChatPopupMenu.MenuItem>)? = null

    // Popup menu
    private val popupMenu: CometChatPopupMenu

    // Confirm dialog
    private var confirmDialog: CometChatConfirmDialog? = null

    // Feature toggles
    private var kickMemberOptionVisibility = View.VISIBLE
    private var banMemberOptionVisibility = View.VISIBLE
    private var scopeChangeOptionVisibility = View.VISIBLE

    // Visibility controls
    private var toolbarVisibility = View.VISIBLE
    private var backIconVisibility = View.GONE
    private var searchBoxVisibility = View.VISIBLE
    private var separatorVisibility = View.GONE
    private var errorStateVisibility = View.VISIBLE
    private var loadingStateVisibility = View.VISIBLE
    private var emptyStateVisibility = View.VISIBLE
    private var userStatusVisibility = View.VISIBLE

    // Style
    private var style: CometChatGroupMembersStyle = CometChatGroupMembersStyle()
    @StyleRes private var currentStyleResId: Int = 0

    // Search debounce
    private var searchJob: Job? = null

    init {
        // Inflate layout
        binding = CometchatGroupMembersBinding.inflate(
            LayoutInflater.from(context), this, true
        )

        // Reset card view defaults
        Utils.initMaterialCard(this)

        // Initialize components
        popupMenu = CometChatPopupMenu(context, 0)
        membersAdapter = GroupMembersAdapter(context)

        // Setup RecyclerView
        setupRecyclerView()

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()

        // Initialize ViewModel
        initViewModel()
    }

    // ==================== Initialization ====================

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerviewGroupMembers.layoutManager = layoutManager
        binding.recyclerviewGroupMembers.adapter = membersAdapter

        // Disable change animations for smoother updates
        val animator = binding.recyclerviewGroupMembers.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Pagination scroll listener
        binding.recyclerviewGroupMembers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel?.fetchGroupMembers()
                }
            }
        })
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatGroupMembers, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatGroupMembers_cometchatGroupMembersStyle, 0
        )
        typedArray.recycle()

        currentStyleResId = styleResId

        typedArray = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatGroupMembers, defStyleAttr, styleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatGroupMembers, defStyleAttr, 0
            )
        }

        style = CometChatGroupMembersStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    private fun applyStyle() {
        // Container
        setCardBackgroundColor(style.backgroundColor)
        if (style.strokeWidth > 0) {
            strokeWidth = style.strokeWidth
            strokeColor = style.strokeColor
        }
        if (style.cornerRadius > 0) {
            radius = style.cornerRadius.toFloat()
        }
        binding.parentLayout.setBackgroundColor(style.backgroundColor)

        // Toolbar
        binding.toolbar.setBackgroundColor(style.backgroundColor)
        if (style.titleTextAppearance != 0) {
            binding.toolbar.setTitleTextAppearance(style.titleTextAppearance)
        }
        if (style.titleTextColor != 0) {
            binding.toolbar.setTitleTextColor(style.titleTextColor)
        }
        style.backIcon?.let { binding.toolbar.setBackIcon(it) }
        if (style.backIconTint != 0) {
            binding.toolbar.setBackIconTint(style.backIconTint)
        }
        binding.toolbar.setBackIconVisibility(backIconVisibility)

        // Selection icons
        style.discardSelectionIcon?.let { binding.toolbar.setDiscardIcon(it) }
        style.submitSelectionIcon?.let { binding.toolbar.setSubmitIcon(it) }
        if (style.discardSelectionIconTint != 0) {
            binding.toolbar.setDiscardIconTint(style.discardSelectionIconTint)
        }
        if (style.submitSelectionIconTint != 0) {
            binding.toolbar.setSubmitIconTint(style.submitSelectionIconTint)
        }
        
        // Selection count styling - matches Java: uses item title styling
        if (style.itemTitleTextAppearance != 0) {
            binding.toolbar.setSelectionCountTextAppearance(style.itemTitleTextAppearance)
        }
        if (style.itemTitleTextColor != 0) {
            binding.toolbar.setSelectionCountTextColor(style.itemTitleTextColor)
        }

        // Search box
        if (style.searchBackgroundColor != 0) {
            binding.searchBox.setCardBackgroundColor(style.searchBackgroundColor)
        }
        if (style.searchTextColor != 0) {
            binding.searchBox.setSearchInputTextColor(style.searchTextColor)
        }
        if (style.searchTextAppearance != 0) {
            binding.searchBox.setSearchInputTextAppearance(style.searchTextAppearance)
        }
        if (style.searchPlaceholderColor != 0) {
            binding.searchBox.setSearchInputPlaceHolderTextColor(style.searchPlaceholderColor)
        }
        style.searchStartIcon?.let { binding.searchBox.setSearchInputStartIcon(it) }
        if (style.searchStartIconTint != 0) {
            binding.searchBox.setSearchInputStartIconTint(style.searchStartIconTint)
        }
        style.searchEndIcon?.let { binding.searchBox.setSearchInputEndIcon(it) }
        if (style.searchEndIconTint != 0) {
            binding.searchBox.setSearchInputEndIconTint(style.searchEndIconTint)
        }
        if (style.searchCornerRadius > 0) {
            binding.searchBox.radius = style.searchCornerRadius.toFloat()
        }
        if (style.searchStrokeWidth > 0) {
            binding.searchBox.strokeWidth = style.searchStrokeWidth
            binding.searchBox.strokeColor = style.searchStrokeColor
        }

        // Empty state
        if (style.emptyStateTitleTextColor != 0) {
            binding.tvEmptyTitle.setTextColor(style.emptyStateTitleTextColor)
        }
        if (style.emptyStateSubtitleTextColor != 0) {
            binding.tvEmptySubtitle.setTextColor(style.emptyStateSubtitleTextColor)
        }
        if (style.emptyStateTitleTextAppearance != 0) {
            binding.tvEmptyTitle.setTextAppearance(style.emptyStateTitleTextAppearance)
        }
        if (style.emptyStateSubtitleTextAppearance != 0) {
            binding.tvEmptySubtitle.setTextAppearance(style.emptyStateSubtitleTextAppearance)
        }

        // Error state
        if (style.errorStateTitleTextColor != 0) {
            binding.tvErrorTitle.setTextColor(style.errorStateTitleTextColor)
        }
        if (style.errorStateSubtitleTextColor != 0) {
            binding.tvErrorSubtitle.setTextColor(style.errorStateSubtitleTextColor)
        }
        if (style.errorStateTitleTextAppearance != 0) {
            binding.tvErrorTitle.setTextAppearance(style.errorStateTitleTextAppearance)
        }
        if (style.errorStateSubtitleTextAppearance != 0) {
            binding.tvErrorSubtitle.setTextAppearance(style.errorStateSubtitleTextAppearance)
        }

        // Update adapter style
        membersAdapter.setStyle(style)
    }

    private fun setupClickListeners() {
        // Adapter item click
        membersAdapter.setOnItemClick { view, position, member ->
            if (onItemClick != null) {
                onItemClick?.invoke(member)
            } else {
                if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                    selectGroupMember(member, selectionMode)
                }
            }
        }

        // Adapter item long click — shows popup menu, matching original Java implementation
        membersAdapter.setOnItemLongClick { view, position, member ->
            if (onItemLongClick != null) {
                onItemLongClick?.invoke(member)
            } else {
                group?.let { grp ->
                    preparePopupMenu(member, grp)
                    val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
                    // Show popup only if the logged-in user has permission to act on this member
                    // Matching the original Java implementation's scope checks
                    if (loggedInUser != null) {
                        val isOwner = loggedInUser.uid == grp.owner
                        val groupScope = grp.scope ?: ""
                        val memberScope = member.scope ?: ""
                        val shouldShow = when {
                            isOwner -> member.uid != loggedInUser.uid
                            CometChatConstants.SCOPE_ADMIN.equals(groupScope, ignoreCase = true) ->
                                CometChatConstants.SCOPE_PARTICIPANT.equals(memberScope, ignoreCase = true) ||
                                        CometChatConstants.SCOPE_MODERATOR.equals(memberScope, ignoreCase = true)
                            CometChatConstants.SCOPE_MODERATOR.equals(groupScope, ignoreCase = true) ->
                                CometChatConstants.SCOPE_PARTICIPANT.equals(memberScope, ignoreCase = true)
                            else -> false
                        }
                        if (shouldShow) {
                            popupMenu.show(view)
                        }
                    }
                }
            }
        }

        // Toolbar callbacks
        binding.toolbar.setOnBackPress {
            onBackPress?.invoke()
        }
        binding.toolbar.setOnDiscardSelection {
            clearSelection()
        }
        binding.toolbar.setOnSubmitSelection {
            onSelection?.invoke(getSelectedMembers())
        }

        // Retry button
        binding.btnRetry.setOnClickListener {
            viewModel?.refreshList()
        }

        // Search box
        binding.searchBox.addOnSearchListener(CometChatSearchBox.OnSearch { state, text ->
            when (state) {
                CometChatSearchBox.SearchState.TEXT_CHANGE -> {
                    searchJob?.cancel()
                    searchJob = viewScope?.launch {
                        delay(SEARCH_DEBOUNCE_MS)
                        viewModel?.searchGroupMembers(text.ifEmpty { null })
                    }
                }
                CometChatSearchBox.SearchState.CLEAR -> {
                    searchJob?.cancel()
                    viewModel?.searchGroupMembers(null)
                }
            }
        })
    }

    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        if (!isExternalViewModel && viewModel == null) {
            val factory = CometChatGroupMembersViewModelFactory(groupMembersRequestBuilder)
            viewModel = ViewModelProvider(
                lifecycleOwner as androidx.lifecycle.ViewModelStoreOwner,
                factory
            )[CometChatGroupMembersViewModel::class.java]
        }

        observeViewModel()
    }

    // ==================== ViewModel Observation ====================

    private fun observeViewModel() {
        val vm = viewModel ?: return

        viewScope?.cancel()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Observe members list
        viewScope?.launch {
            vm.members.collectLatest { members ->
                membersAdapter.updateMembers(members)
            }
        }

        // Observe UI state
        viewScope?.launch {
            vm.uiState.collectLatest { state ->
                handleStateChange(state)
            }
        }

        // Observe dialog state
        viewScope?.launch {
            vm.dialogState.collectLatest { state ->
                handleDialogState(state)
            }
        }

        // Observe events for adapter notifications
        viewScope?.launch {
            vm.events.collectLatest { event ->
                handleEvent(event)
            }
        }
    }

    // ==================== State Handling ====================

    private fun handleStateChange(state: GroupMembersUIState) {
        hideAllStates()
        when (state) {
            is GroupMembersUIState.Loading -> handleLoadingState()
            is GroupMembersUIState.Content -> handleContentState()
            is GroupMembersUIState.Empty -> handleEmptyState()
            is GroupMembersUIState.Error -> handleErrorState(state.exception)
        }
    }

    private fun hideAllStates() {
        binding.recyclerviewGroupMembers.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE
        binding.errorStateView.visibility = View.GONE
        binding.shimmerParentLayout.visibility = View.GONE
        binding.customLayout.visibility = View.GONE
    }

    private fun handleLoadingState() {
        if (loadingStateVisibility == View.VISIBLE) {
            if (customLoadingView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customLoadingView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.shimmerParentLayout.visibility = View.VISIBLE
                setupShimmer()
            }
        }
        announceForAccessibility(context.getString(R.string.cometchat_members))
    }

    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(30, R.layout.cometchat_group_member_shimmer)
        binding.shimmerRecyclerview.adapter = shimmerAdapter
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
    }

    private fun handleContentState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.recyclerviewGroupMembers.visibility = View.VISIBLE
    }

    private fun handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer()
        if (emptyStateVisibility == View.VISIBLE) {
            if (customEmptyView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customEmptyView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateView.visibility = View.VISIBLE
            }
        }
    }

    private fun handleErrorState(exception: CometChatException? = null) {
        binding.shimmerEffectFrame.stopShimmer()
        if (errorStateVisibility == View.VISIBLE) {
            if (customErrorView != null) {
                binding.customLayout.removeAllViews()
                binding.customLayout.addView(customErrorView)
                binding.customLayout.visibility = View.VISIBLE
            } else {
                binding.errorStateView.visibility = View.VISIBLE
            }
        }
        exception?.let { onError?.invoke(it) }
    }

    private fun handleEvent(event: GroupMembersEvent) {
        when (event) {
            is GroupMembersEvent.MemberInsertedAtTop -> {
                binding.recyclerviewGroupMembers.scrollToPosition(0)
            }
            is GroupMembersEvent.MemberKicked,
            is GroupMembersEvent.MemberBanned -> {
                // Already handled by list update from ViewModel
            }
            else -> { /* MemberUpdated, MemberRemoved, etc. handled by DiffUtil */ }
        }
    }

    // ==================== Dialog Handling ====================

    private fun handleDialogState(state: DialogState) {
        confirmDialog?.dismiss()
        confirmDialog = null

        when (state) {
            is DialogState.Hidden -> { /* No dialog */ }
            is DialogState.ConfirmKick -> showKickConfirmDialog(state.member)
            is DialogState.ConfirmBan -> showBanConfirmDialog(state.member)
            is DialogState.SelectScope -> showScopeChangeDialog(state.member, state.currentScope)
        }
    }

    private fun showKickConfirmDialog(member: GroupMember) {
        confirmDialog = CometChatConfirmDialog(context, R.style.CometChatConfirmDialogStyle).apply {
            setConfirmDialogIcon(
                ResourcesCompat.getDrawable(resources, R.drawable.cometchat_ic_delete, null)
            )
            setTitleText(
                context.getString(R.string.cometchat_kick) + " " + member.name + " ?"
            )
            setSubtitleText(
                "Are You sure you want to " + context.getString(R.string.cometchat_kick).lowercase() + " " + member.name + "?"
            )
            setPositiveButtonText(context.getString(R.string.cometchat_yes))
            setNegativeButtonText(context.getString(R.string.cometchat_no))
            setOnPositiveButtonClick { viewModel?.kickMember(member) }
            setOnNegativeButtonClick { viewModel?.dismissDialog() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    private fun showBanConfirmDialog(member: GroupMember) {
        confirmDialog = CometChatConfirmDialog(context, R.style.CometChatConfirmDialogStyle).apply {
            setConfirmDialogIcon(
                ResourcesCompat.getDrawable(resources, R.drawable.cometchat_ic_delete, null)
            )
            setTitleText(
                context.getString(R.string.cometchat_ban) + " " + member.name + " ?"
            )
            setSubtitleText(
                "Are You sure you want to " + context.getString(R.string.cometchat_ban).lowercase() + " " + member.name + "?"
            )
            setPositiveButtonText(context.getString(R.string.cometchat_yes))
            setNegativeButtonText(context.getString(R.string.cometchat_no))
            setOnPositiveButtonClick { viewModel?.banMember(member) }
            setOnNegativeButtonClick { viewModel?.dismissDialog() }
            setConfirmDialogElevation(0)
            setCancelable(false)
            show()
        }
    }

    private fun showScopeChangeDialog(member: GroupMember, currentScope: String) {
        val grp = group ?: return
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(context)

        val cometchatScopeChange = CometChatScopeChange(context)
        cometchatScopeChange.setRoleData(grp, member)
        cometchatScopeChange.setOnPositiveButtonClick {
            val selectedRole = cometchatScopeChange.getScopeAdapter().getSelectedRole()
            if (selectedRole != null) {
                bottomSheetDialog.dismiss()
                viewModel?.changeMemberScope(member, selectedRole.lowercase())
            }
        }
        cometchatScopeChange.setOnNegativeButtonClick {
            bottomSheetDialog.dismiss()
            viewModel?.dismissDialog()
        }

        bottomSheetDialog.setOnDismissListener { viewModel?.dismissDialog() }
        Utils.showBottomSheet(context, bottomSheetDialog, true, false, cometchatScopeChange)
    }

    // ==================== Popup Menu ====================

    private fun preparePopupMenu(member: GroupMember, group: Group) {
        val menuItems = mutableListOf<CometChatPopupMenu.MenuItem>()

        if (options != null) {
            menuItems.addAll(options!!.invoke(context, member, group))
        } else {
            // Build default options based on permissions — matching Java MembersUtils pattern (no icons)
            val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
            val loggedInScope = getLoggedInUserScope(group)

            menuItems.addAll(
                GroupMembersUtils.getDefaultGroupMemberOptions(
                    context = context,
                    groupMember = member,
                    group = group,
                    loggedInUserId = loggedInUser?.uid ?: "",
                    loggedInUserScope = loggedInScope,
                    disableKick = kickMemberOptionVisibility != View.VISIBLE,
                    disableBan = banMemberOptionVisibility != View.VISIBLE,
                    disableChangeScope = scopeChangeOptionVisibility != View.VISIBLE
                )
            )

            // Additional options
            addOptions?.let { menuItems.addAll(it.invoke(context, member, group)) }
        }

        if (menuItems.isEmpty()) return

        popupMenu.setMenuItems(menuItems)
        popupMenu.setOnMenuItemClickListener { id, _ ->
            handleDefaultMenuClick(id, member)
            popupMenu.dismiss()
        }
    }

    private fun handleDefaultMenuClick(id: String, member: GroupMember) {
        when {
            id.equals(UIKitConstants.GroupMemberOption.KICK, ignoreCase = true) -> {
                viewModel?.showKickConfirmation(member)
            }
            id.equals(UIKitConstants.GroupMemberOption.BAN, ignoreCase = true) -> {
                viewModel?.showBanConfirmation(member)
            }
            id.equals(UIKitConstants.GroupMemberOption.CHANGE_SCOPE, ignoreCase = true) -> {
                viewModel?.showScopeSelection(member)
            }
        }
    }

    // ==================== Permission Helpers ====================

    private fun getLoggedInUserScope(group: Group): String {
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
            ?: return CometChatConstants.SCOPE_PARTICIPANT

        // Check if logged-in user is the owner
        if (loggedInUser.uid == group.owner) {
            return CometChatConstants.SCOPE_ADMIN // Owner has highest privileges
        }

        // Find logged-in user in members list
        val members = viewModel?.getItems() ?: emptyList()
        val loggedInMember = members.find { it.uid == loggedInUser.uid }
        return loggedInMember?.scope ?: CometChatConstants.SCOPE_PARTICIPANT
    }

    // ==================== Selection ====================

    /**
     * Selects or deselects a group member based on the current selection mode.
     *
     * In SINGLE mode, clears previous selection and selects the given member.
     * In MULTIPLE mode, toggles the member's selection state.
     *
     * @param member The group member to select/deselect
     * @param mode The selection mode to apply
     */
    fun selectGroupMember(member: GroupMember, mode: UIKitConstants.SelectionMode?) {
        if (mode == null) return
        selectionMode = mode

        when (mode) {
            UIKitConstants.SelectionMode.SINGLE -> {
                selectedMembers.clear()
                selectedMembers[member] = true
                binding.toolbar.setSelectionMode(true)
                binding.toolbar.setSelectionCount(1)
                membersAdapter.selectMembers(selectedMembers)
            }
            UIKitConstants.SelectionMode.MULTIPLE -> {
                if (selectedMembers.containsKey(member)) {
                    selectedMembers.remove(member)
                } else if (isFurtherSelectionEnabled) {
                    selectedMembers[member] = true
                }
                if (selectedMembers.isEmpty()) {
                    binding.toolbar.setSelectionMode(false)
                } else {
                    binding.toolbar.setSelectionMode(true)
                    binding.toolbar.setSelectionCount(selectedMembers.size)
                }
                membersAdapter.selectMembers(selectedMembers)
            }
            UIKitConstants.SelectionMode.NONE -> { /* Do nothing */ }
        }
    }

    /**
     * Returns the list of currently selected group members.
     *
     * @return List of selected [GroupMember] objects
     */
    fun getSelectedMembers(): List<GroupMember> = selectedMembers.keys.toList()

    /**
     * Clears all member selections and resets the toolbar selection UI.
     */
    fun clearSelection() {
        selectedMembers.clear()
        selectionMode = UIKitConstants.SelectionMode.NONE
        binding.toolbar.setSelectionMode(false)
        binding.toolbar.setSelectionCount(0)
        membersAdapter.setSelectionMode(UIKitConstants.SelectionMode.NONE)
    }

    // ==================== Public API - Core ====================

    /**
     * Sets the group for which to display members.
     * This triggers fetching of group members.
     */
    fun setGroup(group: Group) {
        this.group = group
        membersAdapter.setGroup(group)
        viewModel?.setGroup(group)
        viewModel?.fetchGroupMembers()
    }

    /**
     * Gets the current group.
     */
    fun getGroup(): Group? = group

    /**
     * Sets a custom request builder for fetching members.
     */
    fun setGroupMembersRequestBuilder(builder: GroupMembersRequest.GroupMembersRequestBuilder) {
        this.groupMembersRequestBuilder = builder
        viewModel?.setGroupMembersRequestBuilder(builder)
    }

    /**
     * Sets a custom request builder for searching members.
     */
    fun setSearchRequestBuilder(builder: GroupMembersRequest.GroupMembersRequestBuilder) {
        this.searchRequestBuilder = builder
        viewModel?.setSearchRequestBuilder(builder)
    }

    /**
     * Sets the selection mode.
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        selectedMembers.clear()
        membersAdapter.selectMembers(selectedMembers)
        selectionMode = mode

        when (mode) {
            UIKitConstants.SelectionMode.SINGLE,
            UIKitConstants.SelectionMode.MULTIPLE -> {
                isFurtherSelectionEnabled = true
                membersAdapter.setSelectionMode(mode)
                binding.toolbar.setSelectionMode(true)
                binding.toolbar.setSelectionCount(0)
            }
            UIKitConstants.SelectionMode.NONE -> {
                isFurtherSelectionEnabled = false
                membersAdapter.setSelectionMode(UIKitConstants.SelectionMode.NONE)
                binding.toolbar.setSelectionMode(false)
            }
        }
    }

    /**
     * Gets the current selection mode.
     */
    fun getSelectionMode(): UIKitConstants.SelectionMode = selectionMode

    // ==================== Public API - Style ====================

    /**
     * Sets the style programmatically.
     */
    fun setStyle(style: CometChatGroupMembersStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            currentStyleResId = styleRes
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatGroupMembers
            )
            style = CometChatGroupMembersStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }

    /**
     * Gets the current style.
     */
    fun getStyle(): CometChatGroupMembersStyle = style

    /**
     * Refreshes the style from the current theme.
     */
    fun refreshStyle() {
        val typedArray = if (currentStyleResId != 0) {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatGroupMembers,
                R.attr.cometchatGroupMembersStyle, currentStyleResId
            )
        } else {
            context.theme.obtainStyledAttributes(
                null, R.styleable.CometChatGroupMembers,
                R.attr.cometchatGroupMembersStyle, 0
            )
        }
        style = CometChatGroupMembersStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    // ==================== Public API - Visibility Controls ====================

    /**
     * Sets the visibility of the toolbar.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setToolbarVisibility(visibility: Int) {
        toolbarVisibility = visibility
        binding.toolbar.visibility = visibility
    }

    /**
     * Sets the visibility of the back icon in the toolbar.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setBackIconVisibility(visibility: Int) {
        backIconVisibility = visibility
        binding.toolbar.setBackIconVisibility(visibility)
    }

    /**
     * Sets the visibility of the search box.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setSearchBoxVisibility(visibility: Int) {
        searchBoxVisibility = visibility
        binding.searchBoxLayout.visibility = visibility
    }

    /**
     * Sets the visibility of list item separators.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setSeparatorVisibility(visibility: Int) {
        separatorVisibility = visibility
        membersAdapter.setHideSeparator(visibility != View.VISIBLE)
    }

    /**
     * Sets the visibility of user online/offline status indicators.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setUserStatusVisibility(visibility: Int) {
        userStatusVisibility = visibility
        membersAdapter.setHideUserStatus(visibility != View.VISIBLE)
    }

    /**
     * Sets the visibility of the error state view.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setErrorStateVisibility(visibility: Int) {
        errorStateVisibility = visibility
    }

    /**
     * Gets the current error state visibility.
     *
     * @return The error state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getErrorStateVisibility(): Int = errorStateVisibility

    /**
     * Sets the visibility of the loading state view.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setLoadingStateVisibility(visibility: Int) {
        loadingStateVisibility = visibility
    }

    /**
     * Gets the current loading state visibility.
     *
     * @return The loading state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getLoadingStateVisibility(): Int = loadingStateVisibility

    /**
     * Sets the visibility of the empty state view.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setEmptyStateVisibility(visibility: Int) {
        emptyStateVisibility = visibility
    }

    /**
     * Gets the current empty state visibility.
     *
     * @return The empty state visibility (View.VISIBLE, View.INVISIBLE, or View.GONE)
     */
    fun getEmptyStateVisibility(): Int = emptyStateVisibility

    /**
     * Hides or shows the loading state.
     * When hidden, the loading shimmer will not be displayed during initial fetch.
     *
     * @param hide true to hide the loading state, false to show it
     */
    fun setHideLoadingState(hide: Boolean) {
        loadingStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the empty state.
     * When hidden, the empty state view will not be displayed when there are no group members.
     *
     * @param hide true to hide the empty state, false to show it
     */
    fun setHideEmptyState(hide: Boolean) {
        emptyStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the error state.
     * When hidden, the error state view will not be displayed when an error occurs.
     *
     * @param hide true to hide the error state, false to show it
     */
    fun setHideErrorState(hide: Boolean) {
        errorStateVisibility = if (hide) View.GONE else View.VISIBLE
    }

    // ==================== Public API - Feature Toggles ====================

    /**
     * Sets the visibility of the kick member option in the overflow menu.
     *
     * @param visibility [View.VISIBLE] to show, [View.GONE] to hide
     */
    fun setKickMemberOptionVisibility(visibility: Int) {
        kickMemberOptionVisibility = visibility
    }

    /** Returns the current visibility of the kick member option. */
    fun getKickMemberOptionVisibility(): Int = kickMemberOptionVisibility

    /**
     * Sets the visibility of the ban member option in the overflow menu.
     *
     * @param visibility [View.VISIBLE] to show, [View.GONE] to hide
     */
    fun setBanMemberOptionVisibility(visibility: Int) {
        banMemberOptionVisibility = visibility
    }

    /** Returns the current visibility of the ban member option. */
    fun getBanMemberOptionVisibility(): Int = banMemberOptionVisibility

    /**
     * Sets the visibility of the scope change option in the overflow menu.
     *
     * @param visibility [View.VISIBLE] to show, [View.GONE] to hide
     */
    fun setScopeChangeOptionVisibility(visibility: Int) {
        scopeChangeOptionVisibility = visibility
    }

    /** Returns the current visibility of the scope change option. */
    fun getScopeChangeOptionVisibility(): Int = scopeChangeOptionVisibility

    // ==================== Public API - Custom Views ====================

    /**
     * Sets a custom view listener for the entire list item.
     * When set, replaces the default item layout entirely.
     *
     * @param listener The [GroupMembersViewHolderListener] for creating and binding custom item views, or null to use default
     */
    fun setItemView(listener: GroupMembersViewHolderListener?) {
        itemViewListener = listener
        membersAdapter.setItemView(listener)
    }

    /**
     * Sets a custom view listener for the leading section (avatar area).
     *
     * @param listener The [GroupMembersViewHolderListener] for creating and binding custom leading views, or null to use default
     */
    fun setLeadingView(listener: GroupMembersViewHolderListener?) {
        leadingViewListener = listener
        membersAdapter.setLeadingView(listener)
    }

    /**
     * Sets a custom view listener for the title section (member name).
     *
     * @param listener The [GroupMembersViewHolderListener] for creating and binding custom title views, or null to use default
     */
    fun setTitleView(listener: GroupMembersViewHolderListener?) {
        titleViewListener = listener
        membersAdapter.setTitleView(listener)
    }

    /**
     * Sets a custom view listener for the subtitle section (member scope).
     *
     * @param listener The [GroupMembersViewHolderListener] for creating and binding custom subtitle views, or null to use default
     */
    fun setSubtitleView(listener: GroupMembersViewHolderListener?) {
        subtitleViewListener = listener
        membersAdapter.setSubtitleView(listener)
    }

    /**
     * Sets a custom view listener for the trailing section (overflow menu area).
     *
     * @param listener The [GroupMembersViewHolderListener] for creating and binding custom trailing views, or null to use default
     */
    fun setTrailingView(listener: GroupMembersViewHolderListener?) {
        trailingViewListener = listener
        membersAdapter.setTrailingView(listener)
    }

    /**
     * Sets a custom view for the empty state.
     *
     * @param view The custom empty state view, or null to use default
     */
    fun setEmptyView(view: View?) {
        customEmptyView = view
    }

    /**
     * Sets a custom view for the error state.
     *
     * @param view The custom error state view, or null to use default
     */
    fun setErrorView(view: View?) {
        customErrorView = view
    }

    /**
     * Sets a custom view for the loading state.
     *
     * @param view The custom loading state view, or null to use default
     */
    fun setLoadingView(view: View?) {
        customLoadingView = view
    }

    /**
     * Sets a custom overflow menu view in the toolbar.
     *
     * @param view The custom overflow menu view, or null to remove
     */
    fun setOverflowMenu(view: View?) {
        binding.toolbar.clearActionViews()
        if (view != null) {
            binding.toolbar.addActionView(view)
        }
    }

    // ==================== Public API - Callbacks ====================

    /**
     * Sets the callback for member item clicks.
     *
     * @param callback Invoked with the clicked [GroupMember]
     */
    fun setOnItemClick(callback: (GroupMember) -> Unit) {
        onItemClick = callback
    }

    /** Returns the current item click callback. */
    fun getOnItemClick(): ((GroupMember) -> Unit)? = onItemClick

    /**
     * Sets the callback for member item long-clicks.
     *
     * @param callback Invoked with the long-clicked [GroupMember]
     */
    fun setOnItemLongClick(callback: (GroupMember) -> Unit) {
        onItemLongClick = callback
    }

    /** Returns the current item long-click callback. */
    fun getOnItemLongClick(): ((GroupMember) -> Unit)? = onItemLongClick

    /**
     * Sets the callback for error events.
     *
     * @param callback Invoked with the [CometChatException] that occurred
     */
    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
    }

    /** Returns the current error callback. */
    fun getOnError(): ((CometChatException) -> Unit)? = onError

    /**
     * Sets the callback for back button press.
     *
     * @param callback Invoked when the back button is pressed
     */
    fun setOnBackPress(callback: () -> Unit) {
        onBackPress = callback
    }

    /** Returns the current back press callback. */
    fun getOnBackPress(): (() -> Unit)? = onBackPress

    /**
     * Sets the callback for selection submission.
     *
     * @param callback Invoked with the list of selected [GroupMember] objects
     */
    fun setOnSelection(callback: (List<GroupMember>) -> Unit) {
        onSelection = callback
    }

    /** Returns the current selection callback. */
    fun getOnSelection(): ((List<GroupMember>) -> Unit)? = onSelection

    // ==================== Public API - Menu Options ====================

    /**
     * Sets a custom options provider that replaces the default overflow menu items.
     *
     * @param callback Function that returns a list of [CometChatPopupMenu.MenuItem] for the given member and group
     */
    fun setOptions(callback: (Context, GroupMember, Group) -> List<CometChatPopupMenu.MenuItem>) {
        options = callback
    }

    /**
     * Sets an additional options provider that appends items to the default overflow menu.
     *
     * @param callback Function that returns additional [CometChatPopupMenu.MenuItem] items
     */
    fun addOptions(callback: (Context, GroupMember, Group) -> List<CometChatPopupMenu.MenuItem>) {
        addOptions = callback
    }

    // ==================== Public API - ViewModel ====================

    /**
     * Sets an external ViewModel instance. When set, the component uses this ViewModel
     * instead of creating its own. Pass null to revert to internal ViewModel creation.
     *
     * @param viewModel The external [CometChatGroupMembersViewModel], or null to use internal
     */
    fun setViewModel(viewModel: CometChatGroupMembersViewModel?) {
        this.viewModel = viewModel
        this.isExternalViewModel = viewModel != null

        if (lifecycleOwner != null) {
            if (viewModel != null) {
                observeViewModel()
                // Re-set group if already configured
                group?.let { viewModel.setGroup(it) }
            } else {
                isExternalViewModel = false
                initViewModel()
            }
        }
    }

    /** Returns the current ViewModel instance. */
    fun getViewModel(): CometChatGroupMembersViewModel? = viewModel

    // ==================== Public API - Misc ====================

    /**
     * Sets the toolbar title text.
     *
     * @param title The title string to display
     */
    fun setTitle(title: String) {
        binding.toolbar.setTitle(title)
    }

    /**
     * Sets the search box placeholder text.
     *
     * @param placeholder The placeholder string to display when search is empty
     */
    fun setSearchPlaceholderText(placeholder: String) {
        binding.searchBox.setSearchPlaceholderText(placeholder)
    }

    /**
     * Sets the search box text programmatically.
     *
     * @param keyword The search keyword to set
     */
    fun setSearchKeyword(keyword: String) {
        binding.searchBox.setSearchInputText(keyword)
    }

    /**
     * Sets whether to exclude the group owner from the members list.
     *
     * @param exclude true to exclude the owner
     */
    fun excludeOwner(exclude: Boolean) {
        viewModel?.setExcludeOwner(exclude)
    }

    /** Returns the internal RecyclerView. */
    fun getRecyclerView(): RecyclerView = binding.recyclerviewGroupMembers

    /** Returns the internal [GroupMembersAdapter]. */
    fun getAdapter(): GroupMembersAdapter = membersAdapter

    /** Returns the ViewBinding for advanced customization. */
    fun getBinding(): CometchatGroupMembersBinding = binding

    /**
     * Sets whether further member selection is allowed.
     * Useful for limiting the number of selectable members.
     *
     * @param enabled true to allow further selections
     */
    fun setFurtherSelectionEnabled(enabled: Boolean) {
        isFurtherSelectionEnabled = enabled
    }

    /** Returns whether further selection is currently enabled. */
    fun isFurtherSelectionEnabled(): Boolean = isFurtherSelectionEnabled

    // ==================== Public API - Toolbar Styling ====================

    /** Returns the current toolbar title text color. */
    fun getTitleTextColor(): Int = style.titleTextColor

    /**
     * Sets the toolbar title text color.
     *
     * @param color The color int to apply
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        binding.toolbar.setTitleTextColor(color)
    }

    /** Returns the current toolbar title text appearance resource ID. */
    fun getTitleTextAppearance(): Int = style.titleTextAppearance

    /**
     * Sets the toolbar title text appearance.
     *
     * @param appearance The style resource ID for text appearance
     */
    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) binding.toolbar.setTitleTextAppearance(appearance)
    }

    /** Returns the current back icon drawable. */
    fun getBackIcon(): Drawable? = style.backIcon

    /**
     * Sets the toolbar back icon drawable.
     *
     * @param icon The drawable to use as back icon
     */
    fun setBackIcon(icon: Drawable?) {
        style = style.copy(backIcon = icon)
        icon?.let { binding.toolbar.setBackIcon(it) }
    }

    /** Returns the current back icon tint color. */
    fun getBackIconTint(): Int = style.backIconTint

    /**
     * Sets the toolbar back icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setBackIconTint(@ColorInt tint: Int) {
        style = style.copy(backIconTint = tint)
        binding.toolbar.setBackIconTint(tint)
    }

    // ==================== Public API - Search Styling ====================

    /** Returns the current search input background color. */
    fun getSearchInputBackgroundColor(): Int = style.searchBackgroundColor

    /**
     * Sets the search input background color.
     *
     * @param color The color int to apply
     */
    fun setSearchInputBackgroundColor(@ColorInt color: Int) {
        style = style.copy(searchBackgroundColor = color)
        binding.searchBox.setCardBackgroundColor(color)
    }

    /** Returns the current search input text color. */
    fun getSearchInputTextColor(): Int = style.searchTextColor

    /**
     * Sets the search input text color.
     *
     * @param color The color int to apply
     */
    fun setSearchInputTextColor(@ColorInt color: Int) {
        style = style.copy(searchTextColor = color)
        binding.searchBox.setSearchInputTextColor(color)
    }

    /** Returns the current search input placeholder text color. */
    fun getSearchInputPlaceHolderTextColor(): Int = style.searchPlaceholderColor

    /**
     * Sets the search input placeholder text color.
     *
     * @param color The color int to apply
     */
    fun setSearchInputPlaceHolderTextColor(@ColorInt color: Int) {
        style = style.copy(searchPlaceholderColor = color)
        binding.searchBox.setSearchInputPlaceHolderTextColor(color)
    }

    /** Returns the current search input corner radius in pixels. */
    fun getSearchInputCornerRadius(): Int = style.searchCornerRadius

    /**
     * Sets the search input corner radius.
     *
     * @param radius The corner radius in pixels
     */
    fun setSearchInputCornerRadius(@Dimension radius: Int) {
        style = style.copy(searchCornerRadius = radius)
        binding.searchBox.radius = radius.toFloat()
    }

    /** Returns the current search input stroke width in pixels. */
    fun getSearchInputStrokeWidth(): Int = style.searchStrokeWidth

    /**
     * Sets the search input stroke width.
     *
     * @param width The stroke width in pixels
     */
    fun setSearchInputStrokeWidth(@Dimension width: Int) {
        style = style.copy(searchStrokeWidth = width)
        binding.searchBox.strokeWidth = width
    }

    /** Returns the current search input stroke color. */
    fun getSearchInputStrokeColor(): Int = style.searchStrokeColor

    /**
     * Sets the search input stroke color.
     *
     * @param color The color int to apply
     */
    fun setSearchInputStrokeColor(@ColorInt color: Int) {
        style = style.copy(searchStrokeColor = color)
        binding.searchBox.strokeColor = color
    }

    // ==================== Public API - Item Styling ====================

    /** Returns the current item title text color. */
    fun getItemTitleTextColor(): Int = style.itemTitleTextColor

    /**
     * Sets the item title text color.
     *
     * @param color The color int to apply
     */
    fun setItemTitleTextColor(@ColorInt color: Int) {
        style = style.copy(itemTitleTextColor = color)
        membersAdapter.setStyle(style)
    }

    /** Returns the current separator color. */
    fun getSeparatorColor(): Int = style.separatorColor

    /**
     * Sets the list item separator color.
     *
     * @param color The color int to apply
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        membersAdapter.setStyle(style)
    }

    /**
     * Sets the list item separator height.
     *
     * @param height The height in pixels
     */
    fun setSeparatorHeight(@Dimension height: Int) {
        membersAdapter.setSeparatorHeight(height)
    }

    /** Returns the current corner radius in pixels. */
    fun getCornerRadius(): Int = style.cornerRadius

    /**
     * Sets the container corner radius.
     *
     * @param radius The corner radius in pixels
     */
    fun setCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        this.radius = radius.toFloat()
    }

    // ==================== Public API - Checkbox Styling ====================

    /**
     * Sets the selection checkbox stroke width.
     *
     * @param width The stroke width in pixels
     */
    fun setCheckBoxStrokeWidth(@Dimension width: Int) {
        membersAdapter.setCheckBoxStrokeWidth(width)
    }

    /**
     * Sets the selection checkbox corner radius.
     *
     * @param radius The corner radius in pixels
     */
    fun setCheckBoxCornerRadius(@Dimension radius: Int) {
        membersAdapter.setCheckBoxCornerRadius(radius)
    }

    /**
     * Sets the selection checkbox stroke color.
     *
     * @param color The color int to apply
     */
    fun setCheckBoxStrokeColor(@ColorInt color: Int) {
        membersAdapter.setCheckBoxStrokeColor(color)
    }

    /**
     * Sets the selection checkbox background color (unchecked state).
     *
     * @param color The color int to apply
     */
    fun setCheckBoxBackgroundColor(@ColorInt color: Int) {
        membersAdapter.setCheckBoxBackgroundColor(color)
    }

    /**
     * Sets the selection checkbox background color (checked state).
     *
     * @param color The color int to apply
     */
    fun setCheckBoxCheckedBackgroundColor(@ColorInt color: Int) {
        membersAdapter.setCheckBoxCheckedBackgroundColor(color)
    }

    // ==================== Public API - Selection Icon Styling ====================

    /**
     * Sets the discard selection icon drawable.
     *
     * @param icon The drawable to use as discard selection icon
     */
    fun setDiscardSelectionIcon(icon: Drawable?) {
        style = style.copy(discardSelectionIcon = icon)
        icon?.let { binding.toolbar.setDiscardIcon(it) }
    }

    /**
     * Sets the discard selection icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setDiscardSelectionIconTint(@ColorInt tint: Int) {
        style = style.copy(discardSelectionIconTint = tint)
        binding.toolbar.setDiscardIconTint(tint)
    }

    /**
     * Sets the submit selection icon drawable.
     *
     * @param icon The drawable to use as submit selection icon
     */
    fun setSubmitSelectionIcon(icon: Drawable?) {
        style = style.copy(submitSelectionIcon = icon)
        icon?.let { binding.toolbar.setSubmitIcon(it) }
    }

    /**
     * Sets the submit selection icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setSubmitSelectionIconTint(@ColorInt tint: Int) {
        style = style.copy(submitSelectionIconTint = tint)
        binding.toolbar.setSubmitIconTint(tint)
    }

    /**
     * Sets the checkbox select icon drawable (checkmark icon).
     *
     * @param icon The drawable to use as select icon
     */
    fun setSelectIcon(icon: Drawable?) {
        style = style.copy(checkBoxSelectIcon = icon)
        membersAdapter.setCheckBoxSelectIcon(icon)
    }

    /**
     * Sets the checkbox select icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setSelectIconTint(@ColorInt tint: Int) {
        style = style.copy(checkBoxSelectIconTint = tint)
        membersAdapter.setCheckBoxSelectIconTint(tint)
    }

    // ==================== Public API - Search Input Advanced Styling ====================

    /**
     * Sets the search input text appearance.
     *
     * @param appearance The style resource ID for text appearance
     */
    fun setSearchInputTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(searchTextAppearance = appearance)
        if (appearance != 0) binding.searchBox.setSearchInputTextAppearance(appearance)
    }

    /**
     * Sets the search input placeholder text appearance.
     *
     * @param appearance The style resource ID for text appearance
     */
    fun setSearchInputPlaceHolderTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) binding.searchBox.setSearchInputPlaceHolderTextAppearance(appearance)
    }

    /**
     * Sets the search input start icon drawable.
     *
     * @param icon The drawable to use as search icon
     */
    fun setSearchInputIcon(icon: Drawable?) {
        style = style.copy(searchStartIcon = icon)
        icon?.let { binding.searchBox.setSearchInputStartIcon(it) }
    }

    /**
     * Sets the search input start icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setSearchInputIconTint(@ColorInt tint: Int) {
        style = style.copy(searchStartIconTint = tint)
        binding.searchBox.setSearchInputStartIconTint(tint)
    }

    /**
     * Sets the search input end icon drawable.
     *
     * @param icon The drawable to use as end/clear icon
     */
    fun setSearchInputEndIcon(icon: Drawable?) {
        style = style.copy(searchEndIcon = icon)
        icon?.let { binding.searchBox.setSearchInputEndIcon(it) }
    }

    /**
     * Sets the search input end icon tint color.
     *
     * @param tint The color int to apply
     */
    fun setSearchInputEndIconTint(@ColorInt tint: Int) {
        style = style.copy(searchEndIconTint = tint)
        binding.searchBox.setSearchInputEndIconTint(tint)
    }

    // ==================== Public API - List Item Advanced Styling ====================

    /**
     * Sets the avatar style for list items.
     *
     * @param avatarStyle The avatar style to apply
     */
    fun setAvatarStyle(avatarStyle: com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle) {
        style = style.copy(avatarStyle = avatarStyle)
        membersAdapter.setAvatarStyle(avatarStyle)
    }

    /**
     * Sets the item title text appearance.
     *
     * @param appearance The style resource ID for text appearance
     */
    fun setItemTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemTitleTextAppearance = appearance)
        membersAdapter.setItemTitleTextAppearance(appearance)
    }

    /**
     * Sets the status indicator style for list items.
     *
     * @param statusIndicatorStyle The status indicator style to apply
     */
    fun setStatusIndicatorStyle(statusIndicatorStyle: com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle) {
        style = style.copy(statusIndicatorStyle = statusIndicatorStyle)
        membersAdapter.setStatusIndicatorStyle(statusIndicatorStyle)
    }

    // ==================== Public API - Empty/Error State Styling ====================

    /**
     * Sets the empty state title text color.
     *
     * @param color The color int to apply
     */
    fun setEmptyStateTitleTextColor(@ColorInt color: Int) {
        style = style.copy(emptyStateTitleTextColor = color)
        binding.tvEmptyTitle.setTextColor(color)
    }

    /**
     * Sets the empty state subtitle text color.
     *
     * @param color The color int to apply
     */
    fun setEmptyStateSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(emptyStateSubtitleTextColor = color)
        binding.tvEmptySubtitle.setTextColor(color)
    }

    /**
     * Sets the error state title text color.
     *
     * @param color The color int to apply
     */
    fun setErrorStateTitleTextColor(@ColorInt color: Int) {
        style = style.copy(errorStateTitleTextColor = color)
        binding.tvErrorTitle.setTextColor(color)
    }

    /**
     * Sets the error state subtitle text color.
     *
     * @param color The color int to apply
     */
    fun setErrorStateSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(errorStateSubtitleTextColor = color)
        binding.tvErrorSubtitle.setTextColor(color)
    }

    // ==================== Lifecycle ====================

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        searchJob?.cancel()
        viewScope?.cancel()
        viewScope = null
        confirmDialog?.dismiss()
        confirmDialog = null
    }
}
