package com.cometchat.sampleapp.kotlin.ui.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.constants.UIKitConstants.States
import com.cometchat.chatuikit.shared.interfaces.OnError
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.data.interfaces.OnBackPress
import com.cometchat.sampleapp.kotlin.databinding.BannedMemberCustomViewBinding
import com.cometchat.sampleapp.kotlin.ui.adapters.BannedMembersAdapter
import com.cometchat.sampleapp.kotlin.viewmodels.BannedMembersViewModel
import com.google.android.material.card.MaterialCardView

/**
 * CometChatGroupMembers class handles the display and management of group
 * members in the CometChat UI.
 *
 *
 *
 * This class provides functionality to manage and interact with group members,
 * including displaying the list of members, handling clicks, and managing
 * different states (loading, error, empty). It also provides customization
 * options for different UI states and group member interactions.
 */
class CometChatBannedMembers @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var binding: BannedMemberCustomViewBinding
    private lateinit var viewModel: BannedMembersViewModel
    private var groupMembersAdapter: BannedMembersAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var confirmDialog: CometChatConfirmDialog? = null
    private var lifecycleOwner: LifecycleOwner? = null

    // Interaction listeners
    private var onBackPress: OnBackPress? = null
    /**
     * Retrieves the current OnError callback instance.
     *
     * @return The OnError callback instance used for handling errors.
     */
    /**
     * Sets the error callback for handling errors.
     *
     * @param onError The error callback to set.
     */
    var onError: OnError? = null

    /**
     * Checks if the error state is hidden.
     *
     * @return `true` if the error state is hidden; `false` otherwise.
     */ // Error visibility
    var isHideError: Boolean = false
        private set

    /**
     * Initializes the CometChatGroupMembers view.
     *
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    private fun inflateAndInitializeView(
        attrs: AttributeSet?, defStyleAttr: Int
    ) { // Initialize the MaterialCardView and inflate the binding for the layout
        Utils.initMaterialCard(this)
        binding = BannedMemberCustomViewBinding.inflate(
            LayoutInflater.from(
                context
            ), this, true
        )

        initRecyclerView() // Initialize the ViewModel and observe various live data updates
        initViewModel() // Handle click events within the view
        clickEvents()
        setUpColors() // Set up the colors for the view based on the CometChat theme
        super.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(context))
    }

    private fun setUpColors() {
        binding.parentLayout.setBackgroundColor(CometChatTheme.getBackgroundColor1(context))
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
        binding.ivBack.setColorFilter(CometChatTheme.getIconTintPrimary(context))
        binding.viewSeparator.setBackgroundColor(CometChatTheme.getStrokeColorLight(context))
        binding.groupMemberSearchCard.setStrokeColor(ColorStateList.valueOf(CometChatTheme.getStrokeColorLight(context)))
        binding.ivSearch.setColorFilter(CometChatTheme.getIconTintSecondary(context))
        binding.ivClear.setColorFilter(CometChatTheme.getIconTintSecondary(context))
        binding.etSearch.setTextColor(CometChatTheme.getTextColorPrimary(context))
        binding.etSearch.setHintTextColor(CometChatTheme.getTextColorSecondary(context))
        binding.tvEmptyGroupMembersTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
        binding.tvEmptyGroupMembersSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(context))
        binding.tvErrorGroupMembersTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
        binding.tvErrorGroupMembersSubtitle.setTextColor(CometChatTheme.getTextColorSecondary(context))
        binding.retryBtn.setTextColor(CometChatTheme.getColorWhite(context))
        binding.retryBtn.backgroundTintList = ColorStateList.valueOf(CometChatTheme.getPrimaryColor(context))
    }

    /**
     * Initializes the ViewModel and sets up observers for LiveData.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(BannedMembersViewModel::class.java)
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return

        viewModel.mutableBannedGroupMembersList.observe(lifecycleOwner!!) { list: List<GroupMember> ->
            this.setGroupMemberList(list)
        }
        viewModel.states.observe(lifecycleOwner!!) { states: States ->
            this.setStateChangeObserver(states)
        }
        viewModel.insertAtTop().observe(lifecycleOwner!!) { position: Int ->
            this.notifyInsertedAt(position)
        }
        viewModel.moveToTop().observe(lifecycleOwner!!) { position: Int ->
            this.notifyItemMovedToTop(position)
        }
        viewModel.updateGroupMember().observe(lifecycleOwner!!) { position: Int ->
            this.notifyItemChanged(position)
        }
        viewModel.removeGroupMember().observe(lifecycleOwner!!) { position: Int ->
            this.notifyItemRemoved(position)
        }
        viewModel.cometChatException.observe(lifecycleOwner!!, exceptionObserver)
        viewModel.dialogStates.observe(lifecycleOwner!!) { state: DialogState ->
            this.setDialogState(state)
        }

        // Set up the back button click event
        binding.ivBack.setOnClickListener { view: View? ->
            if (onBackPress != null) {
                onBackPress!!.onBack()
            }
        }
    }

    /**
     * Initializes the RecyclerView and sets its adapter.
     */
    private fun initRecyclerView() { // Set up the RecyclerView and its adapter
        layoutManager = LinearLayoutManager(context)
        binding.recyclerviewGroupMembersList.layoutManager = layoutManager
        groupMembersAdapter = BannedMembersAdapter(
            context
        )
        binding.recyclerviewGroupMembersList.adapter =
            groupMembersAdapter // Add a scroll listener to the RecyclerView to detect when the user reaches the
        // bottom
        binding.recyclerviewGroupMembersList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView, newState: Int
            ) { // If the RecyclerView cannot scroll down anymore, fetch more group members
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.fetchGroupMember()
                }
            }

            override fun onScrolled(
                recyclerView: RecyclerView, dx: Int, dy: Int
            ) { // Additional actions on scroll can be handled here if needed
            }
        })
    }

    /**
     * Shows a confirmation alert dialog for unbanning a group member.
     *
     * @param groupMember        The group member to unban.
     * @param title              The title of the confirmation dialog.
     * @param message            The message to display in the dialog.
     * @param positiveButtonText The text for the positive button.
     * @param negativeButtonText The text for the negative button.
     */
    private fun showConfirmationAlertDialog(
        groupMember: GroupMember, title: String, message: String, positiveButtonText: String, negativeButtonText: String
    ) {
        confirmDialog = CometChatConfirmDialog(
            context, com.cometchat.chatuikit.R.style.CometChatConfirmDialogStyle
        )
        confirmDialog!!.confirmDialogIcon = ResourcesCompat.getDrawable(
            resources, R.drawable.ic_leave_group, null
        )
        confirmDialog!!.titleText = title
        confirmDialog!!.setConfirmDialogIconTint(CometChatTheme.getErrorColor(context))
        confirmDialog!!.subtitleText = message
        confirmDialog!!.positiveButtonText = positiveButtonText
        confirmDialog!!.negativeButtonText = negativeButtonText
        confirmDialog!!.onPositiveButtonClick = OnClickListener { v: View? ->
            viewModel.unBanGroupMember(groupMember)
        }
        confirmDialog!!.onNegativeButtonClick = OnClickListener { v: View? -> confirmDialog!!.dismiss() }
        confirmDialog!!.confirmDialogElevation = 0
        confirmDialog!!.setCancelable(false)
        confirmDialog!!.show()
    }

    /**
     * Sets the state of the dialog based on the provided DialogState.
     *
     * @param state The state to set for the dialog, which can be SUCCESS, INITIATED,
     * or FAILURE.
     */
    private fun setDialogState(state: DialogState) {
        if (confirmDialog != null && confirmDialog!!.isShowing) {
            when (state) {
                DialogState.SUCCESS -> {
                    confirmDialog!!.dismiss()
                    confirmDialog = null
                }

                DialogState.INITIATED -> confirmDialog!!.hidePositiveButtonProgressBar(false)

                DialogState.FAILURE -> {
                    confirmDialog!!.dismiss()
                    Toast.makeText(
                        context,
                        context.getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dispose()
    }

    /**
     * Disposes of all observers and clears references to prevent memory leaks.
     */
    private fun dispose() {
        viewModel.removeListeners()

        if (lifecycleOwner != null) {
            viewModel.mutableBannedGroupMembersList.removeObservers(lifecycleOwner!!)
            viewModel.states.removeObservers(lifecycleOwner!!)
            viewModel.insertAtTop().removeObservers(lifecycleOwner!!)
            viewModel.moveToTop().removeObservers(lifecycleOwner!!)
            viewModel.updateGroupMember().removeObservers(lifecycleOwner!!)
            viewModel.removeGroupMember().removeObservers(lifecycleOwner!!)
            viewModel.cometChatException.removeObservers(lifecycleOwner!!)
            viewModel.dialogStates.removeObservers(lifecycleOwner!!)
        }
        lifecycleOwner = null
        groupMembersAdapter = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.addListeners()
    }

    /**
     * Sets the group for the CometChatGroupMembers view.
     *
     * @param group The Group object to be set.
     */
    fun setGroup(group: Group?) {
        groupMembersAdapter!!.group = group
        viewModel.setGroup(group)
        viewModel.fetchGroupMember()
    }

    /**
     * Sets up click events and listeners for various UI elements in the view. This
     * includes handling item clicks in the RecyclerView, managing text changes in
     * the search input, and defining actions for the clear button and retry button.
     */
    private fun clickEvents() { // Add a touch listener to the RecyclerView to handle item clicks
        binding.recyclerviewGroupMembersList.addOnItemTouchListener(
            RecyclerTouchListener(context, binding.recyclerviewGroupMembersList, object : ClickListener() {
                override fun onClick(
                    view: View, position: Int
                ) {
                    val groupMember = view.getTag(com.cometchat.chatuikit.R.string.cometchat_member) as GroupMember
                    showConfirmationAlertDialog(
                        groupMember, "Unban " + groupMember.name, "Are you sure you want to unban " + groupMember.name + "?", "Unban", "Cancel"
                    )
                }

                override fun onLongClick(
                    view: View, position: Int
                ) {
                    val groupMember =
                        view.getTag(com.cometchat.chatuikit.R.string.cometchat_member) as GroupMember // Additional actions on long click can be handled here if needed
                }
            })
        ) // Add a text change listener to the search input field
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence, i: Int, i1: Int, i2: Int
            ) { // No action needed before text changes
            }

            override fun onTextChanged(
                charSequence: CharSequence, i: Int, i1: Int, i2: Int
            ) { // Show the clear icon when there is text, and filter the group members
                // based on the search input
                if (charSequence.length != 0) {
                    binding.ivClear.visibility = VISIBLE
                    viewModel.searchBannedGroupMembers(charSequence.toString())
                } else {
                    binding.ivClear.visibility = GONE
                    viewModel.searchBannedGroupMembers(null)
                }
            }

            override fun afterTextChanged(editable: Editable) { // No action needed after text changes
            }
        }) // Set up the clear icon click event to reset the search input
        binding.ivClear.setOnClickListener { view: View? ->
            binding.etSearch.setText("")
            binding.ivClear.visibility = GONE
        } // Set up the retry button click event for retrying to fetch group members
        binding.retryBtn.setOnClickListener { view: View? -> viewModel.fetchGroupMember() }
    }

    /**
     * Observer for handling conversation states. Depending on the state, it
     * triggers appropriate methods to handle each state.
     */
    private fun setStateChangeObserver(states: States) {
        when (states) {
            States.LOADING -> handleLoadingState()
            States.LOADED -> handleLoadedState()
            States.ERROR -> handleErrorState()
            States.EMPTY -> handleEmptyState()
            States.NON_EMPTY -> handleNonEmptyState()

            else -> {}
        }
    }

    /**
     * Handles the loading state by displaying a loading view. If a custom loading
     * view is provided, it shows that; otherwise, it uses a shimmer effect.
     */
    private fun handleLoadingState() {
        if (binding.etSearch.text.toString().trim { it <= ' ' }.isEmpty() && !binding.etSearch.isFocused) {
            val adapter = CometChatShimmerAdapter(
                30, com.cometchat.chatuikit.R.layout.cometchat_group_member_shimmer
            )
            binding.shimmerRecyclerviewGroupMembersList.adapter = adapter
            binding.shimmerParentLayout.visibility = VISIBLE
            binding.shimmerEffectFrame.setShimmer(
                CometChatShimmerUtils.getCometChatShimmerConfig(
                    context
                )
            )
            binding.shimmerEffectFrame.startShimmer()
        }
    }

    /**
     * Handles the loaded state by hiding the shimmer effect and displaying the
     * conversation list.
     */
    private fun handleLoadedState() {
        hideShimmer()
        hideAllStates()
        binding.recyclerviewGroupMembersList.visibility = VISIBLE
    }

    /**
     * Handles the error state by displaying an error message or a custom error view
     * if provided. It also hides other views that are not relevant during the error
     * state.
     */
    private fun handleErrorState() {
        if (viewModel.getGroupMemberArrayList().isEmpty()) {
            hideShimmer()
            hideAllStates()
            binding.errorGroupMembersLayout.visibility = VISIBLE
        }
    }

    /**
     * Handles the empty state by displaying a message indicating there are no
     * conversations. It shows a custom empty view if provided.
     */
    private fun handleEmptyState() {
        hideShimmer()
        hideErrorState()
        binding.emptyGroupMembersLayout.visibility = VISIBLE
        binding.recyclerviewGroupMembersList.visibility = GONE
    }

    /**
     * Handles the non-empty state by ensuring the conversation list is visible.
     */
    private fun handleNonEmptyState() {
        hideShimmer()
        hideAllStates()
        binding.recyclerviewGroupMembersList.visibility = VISIBLE
    }

    /**
     * Hides the shimmer effect and the parent layout containing it.
     */
    private fun hideShimmer() {
        binding.shimmerParentLayout.visibility = GONE
        binding.shimmerEffectFrame.stopShimmer()
    }

    /**
     * Hides all state layouts (custom, error, and empty layouts).
     */
    private fun hideAllStates() {
        binding.groupMembersCustomLayout.visibility = GONE
        binding.errorGroupMembersLayout.visibility = GONE
        binding.emptyGroupMembersLayout.visibility = GONE
    }

    /**
     * Hides the error state layout.
     */
    private fun hideErrorState() {
        binding.errorGroupMembersLayout.visibility = GONE
    }

    /**
     * Hides or shows the error state view.
     *
     * @param hide True to hide the error state view, false to show it.
     */
    fun hideError(hide: Boolean) {
        isHideError = hide
    }

    /**
     * Notifies the adapter that an item has been inserted at the specified position
     * and scrolls to the top of the list.
     *
     * @param position The position at which the item was inserted.
     */
    private fun notifyInsertedAt(position: Int) {
        groupMembersAdapter!!.notifyItemInserted(position)
        scrollToTop()
    }

    /**
     * Notifies the adapter that an item has moved to the top of the list and
     * scrolls to the top.
     *
     * @param position The current position of the item before moving.
     */
    private fun notifyItemMovedToTop(position: Int) {
        groupMembersAdapter!!.notifyItemMoved(position, 0)
        groupMembersAdapter!!.notifyItemChanged(0)
        scrollToTop()
    }

    /**
     * Scrolls the RecyclerView to the top if the first visible item position is
     * less than 5.
     */
    private fun scrollToTop() {
        if (layoutManager!!.findFirstVisibleItemPosition() < 5) layoutManager!!.scrollToPosition(0)
    }

    /**
     * Notifies the adapter that the item at the specified position has changed.
     *
     * @param position The position of the item that has changed.
     */
    private fun notifyItemChanged(position: Int) {
        groupMembersAdapter!!.notifyItemChanged(position)
    }

    /**
     * Notifies the adapter that an item has been removed from the specified
     * position.
     *
     * @param position The position of the item that was removed.
     */
    private fun notifyItemRemoved(position: Int) {
        groupMembersAdapter!!.notifyItemRemoved(position)
    }

    /**
     * Observer for handling exceptions from CometChat operations.
     *
     *
     *
     * When an exception is observed, the onError callback is triggered.
     */
    var exceptionObserver: Observer<CometChatException> = Observer { exception: CometChatException? ->
        if (onError != null) onError!!.onError(exception)
    }
    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context      The context of the activity or fragment.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     * @param attrs   The attribute set for the view.
     */
    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     */
    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    /**
     * Sets a listener to handle back press events.
     *
     * @param onBackPress The listener that will be notified of back press events. If
     * `null` is provided, the current listener remains unchanged.
     */
    fun setOnBackPressListener(onBackPress: OnBackPress?) {
        if (onBackPress != null) this.onBackPress = onBackPress
    }

    /**
     * Sets the visibility of the toolbar.
     *
     * @param visibility The visibility state to set. Must be one of [View.VISIBLE],
     * [View.INVISIBLE], or [View.GONE].
     */
    fun setToolbarVisibility(visibility: Int) {
        binding.toolbar.visibility = visibility
    }

    /**
     * Sets the visibility of the title TextView.
     *
     * @param visibility The visibility state to set. Must be one of [View.VISIBLE],
     * [View.INVISIBLE], or [View.GONE].
     */
    fun setTitleVisibility(visibility: Int) {
        binding.tvTitle.visibility = visibility
    }

    /**
     * Sets the list of group members for the adapter.
     *
     * @param list The list of [GroupMember] objects to be displayed.
     */
    private fun setGroupMemberList(list: List<GroupMember>) {
        groupMembersAdapter!!.setGroupMemberList(list)
    }

    companion object {

        private val TAG: String = CometChatBannedMembers::class.java.simpleName
    }
}
