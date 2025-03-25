package com.cometchat.sampleapp.java.ui.customviews;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.data.interfaces.OnBackPress;
import com.cometchat.sampleapp.java.databinding.BannedMemberCustomViewBinding;
import com.cometchat.sampleapp.java.ui.adapters.BannedMembersAdapter;
import com.cometchat.sampleapp.java.viewmodels.BannedMembersViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * CometChatGroupMembers class handles the display and management of group
 * members in the CometChat UI.
 *
 * <p>
 * This class provides functionality to manage and interact with group members,
 * including displaying the list of members, handling clicks, and managing
 * different states (loading, error, empty). It also provides customization
 * options for different UI states and group member interactions.
 */
public class CometChatBannedMembers extends MaterialCardView {
    private static final String TAG = CometChatBannedMembers.class.getSimpleName();

    private BannedMemberCustomViewBinding binding;
    private BannedMembersViewModel viewModel;

    private BannedMembersAdapter groupMembersAdapter;

    private LinearLayoutManager layoutManager;
    private CometChatConfirmDialog confirmDialog;

    // Interaction listeners
    private OnBackPress onBackPress;
    private OnError onError;
    /**
     * Observer for handling exceptions from CometChat operations.
     *
     * <p>
     * When an exception is observed, the onError callback is triggered.
     */
    Observer<CometChatException> exceptionObserver = exception -> {
        if (onError != null) onError.onError(exception);
    };
    // Error visibility
    private boolean hideError;

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     */
    public CometChatBannedMembers(Context context) {
        this(context, null);
    }

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     * @param attrs   The attribute set for the view.
     */
    public CometChatBannedMembers(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context      The context of the activity or fragment.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatBannedMembers(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the CometChatGroupMembers view.
     *
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Initialize the MaterialCardView and inflate the binding for the layout
        Utils.initMaterialCard(this);
        binding = BannedMemberCustomViewBinding.inflate(LayoutInflater.from(getContext()), this, true);

        initRecyclerView();

        // Initialize the ViewModel and observe various live data updates
        initViewModel();

        // Handle click events within the view
        clickEvents();

        super.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(getContext()));
    }

    /**
     * Initializes the RecyclerView and sets its adapter.
     */
    private void initRecyclerView() {
        // Set up the RecyclerView and its adapter
        layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerviewGroupMembersList.setLayoutManager(layoutManager);
        groupMembersAdapter = new BannedMembersAdapter(getContext());
        binding.recyclerviewGroupMembersList.setAdapter(groupMembersAdapter);

        // Add a scroll listener to the RecyclerView to detect when the user reaches the
        // bottom
        binding.recyclerviewGroupMembersList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                // If the RecyclerView cannot scroll down anymore, fetch more group members
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.fetchGroupMember();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // Additional actions on scroll can be handled here if needed
            }
        });
    }

    /**
     * Initializes the ViewModel and sets up observers for LiveData.
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(BannedMembersViewModel.class);
        viewModel.getMutableBannedGroupMembersList().observe((LifecycleOwner) getContext(), this::setGroupMemberList);
        viewModel.getStates().observe((LifecycleOwner) getContext(), this::setStateChangeObserver);
        viewModel.insertAtTop().observe((LifecycleOwner) getContext(), this::notifyInsertedAt);
        viewModel.moveToTop().observe((LifecycleOwner) getContext(), this::notifyItemMovedToTop);
        viewModel.updateGroupMember().observe((LifecycleOwner) getContext(), this::notifyItemChanged);
        viewModel.removeGroupMember().observe((LifecycleOwner) getContext(), this::notifyItemRemoved);
        viewModel.getCometChatException().observe((LifecycleOwner) getContext(), exceptionObserver);
        viewModel.getDialogStates().observe((LifecycleOwner) getContext(), this::setDialogState);

        // Set up the back button click event
        binding.ivBack.setOnClickListener(view -> {
            if (onBackPress != null) {
                onBackPress.onBack();
            }
        });
    }

    /**
     * Sets up click events and listeners for various UI elements in the view. This
     * includes handling item clicks in the RecyclerView, managing text changes in
     * the search input, and defining actions for the clear button and retry button.
     */
    private void clickEvents() {
        // Add a touch listener to the RecyclerView to handle item clicks
        binding.recyclerviewGroupMembersList.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                                                                                              binding.recyclerviewGroupMembersList,
                                                                                              new ClickListener() {
                                                                                                  @Override
                                                                                                  public void onClick(View view, int position) {
                                                                                                      GroupMember groupMember = (GroupMember) view.getTag(
                                                                                                          com.cometchat.chatuikit.R.string.cometchat_member);
                                                                                                      showConfirmationAlertDialog(groupMember,
                                                                                                                                  "Unban " + groupMember.getName(),
                                                                                                                                  "Are you sure you want to unban " + groupMember.getName() + "?",
                                                                                                                                  "Unban",
                                                                                                                                  "Cancel"
                                                                                                      );
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onLongClick(View view, int position) {
                                                                                                      GroupMember groupMember = (GroupMember) view.getTag(
                                                                                                          com.cometchat.chatuikit.R.string.cometchat_member);
                                                                                                      // Additional actions on long click can be handled here if needed
                                                                                                  }
                                                                                              }
        ));

        // Add a text change listener to the search input field
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Show the clear icon when there is text, and filter the group members
                // based on the search input
                if (charSequence.length() != 0) {
                    binding.ivClear.setVisibility(VISIBLE);
                    viewModel.searchBannedGroupMembers(charSequence.toString());
                } else {
                    binding.ivClear.setVisibility(GONE);
                    viewModel.searchBannedGroupMembers(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed after text changes
            }
        });

        // Set up the clear icon click event to reset the search input
        binding.ivClear.setOnClickListener(view -> {
            binding.etSearch.setText("");
            binding.ivClear.setVisibility(GONE);
        });

        // Set up the retry button click event for retrying to fetch group members
        binding.retryBtn.setOnClickListener(view -> viewModel.fetchGroupMember());
    }

    /**
     * Sets the list of group members for the adapter.
     *
     * @param list The list of {@link GroupMember} objects to be displayed.
     */
    private void setGroupMemberList(List<GroupMember> list) {
        groupMembersAdapter.setGroupMemberList(list);
    }

    /**
     * Observer for handling conversation states. Depending on the state, it
     * triggers appropriate methods to handle each state.
     */
    private void setStateChangeObserver(UIKitConstants.States states) {
        switch (states) {
            case LOADING:
                handleLoadingState();
                break;
            case LOADED:
                handleLoadedState();
                break;
            case ERROR:
                handleErrorState();
                break;
            case EMPTY:
                handleEmptyState();
                break;
            case NON_EMPTY:
                handleNonEmptyState();
                break;
            default:
                break;
        }
    }

    /**
     * Notifies the adapter that an item has been inserted at the specified position
     * and scrolls to the top of the list.
     *
     * @param position The position at which the item was inserted.
     */
    private void notifyInsertedAt(int position) {
        groupMembersAdapter.notifyItemInserted(position);
        scrollToTop();
    }

    /**
     * Notifies the adapter that an item has moved to the top of the list and
     * scrolls to the top.
     *
     * @param position The current position of the item before moving.
     */
    private void notifyItemMovedToTop(int position) {
        groupMembersAdapter.notifyItemMoved(position, 0);
        groupMembersAdapter.notifyItemChanged(0);
        scrollToTop();
    }

    /**
     * Notifies the adapter that the item at the specified position has changed.
     *
     * @param position The position of the item that has changed.
     */
    private void notifyItemChanged(int position) {
        groupMembersAdapter.notifyItemChanged(position);
    }

    /**
     * Notifies the adapter that an item has been removed from the specified
     * position.
     *
     * @param position The position of the item that was removed.
     */
    private void notifyItemRemoved(int position) {
        groupMembersAdapter.notifyItemRemoved(position);
    }

    /**
     * Sets the state of the dialog based on the provided DialogState.
     *
     * @param state The state to set for the dialog, which can be SUCCESS, INITIATED,
     *              or FAILURE.
     */
    private void setDialogState(UIKitConstants.DialogState state) {
        if (confirmDialog != null && confirmDialog.isShowing()) {
            switch (state) {
                case SUCCESS:
                    confirmDialog.dismiss();
                    confirmDialog = null;
                    break;
                case INITIATED:
                    confirmDialog.hidePositiveButtonProgressBar(false);
                    break;
                case FAILURE:
                    confirmDialog.dismiss();
                    Toast.makeText(getContext(),
                                   getContext().getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong_please_try_again),
                                   Toast.LENGTH_SHORT
                    ).show();
                    break;
            }
        }
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
    private void showConfirmationAlertDialog(
        GroupMember groupMember, String title, String message, String positiveButtonText, String negativeButtonText
    ) {
        confirmDialog = new CometChatConfirmDialog(getContext(), com.cometchat.chatuikit.R.style.CometChatConfirmDialogStyle);
        confirmDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_leave_group, null));
        confirmDialog.setTitleText(title);
        confirmDialog.setConfirmDialogIconTint(CometChatTheme.getErrorColor(getContext()));
        confirmDialog.setSubtitleText(message);
        confirmDialog.setPositiveButtonText(positiveButtonText);
        confirmDialog.setNegativeButtonText(negativeButtonText);
        confirmDialog.setOnPositiveButtonClick(v -> {
            viewModel.unBanGroupMember(groupMember);
        });
        confirmDialog.setOnNegativeButtonClick(v -> confirmDialog.dismiss());
        confirmDialog.setConfirmDialogElevation(0);
        confirmDialog.setCancelable(false);
        confirmDialog.show();
    }

    /**
     * Handles the loading state by displaying a loading view. If a custom loading
     * view is provided, it shows that; otherwise, it uses a shimmer effect.
     */
    private void handleLoadingState() {
        if (binding.etSearch.getText().toString().trim().isEmpty() && !binding.etSearch.isFocused()) {
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, com.cometchat.chatuikit.R.layout.cometchat_group_member_shimmer);
            binding.shimmerRecyclerviewGroupMembersList.setAdapter(adapter);
            binding.shimmerParentLayout.setVisibility(View.VISIBLE);
            binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            binding.shimmerEffectFrame.startShimmer();
        }
    }

    /**
     * Handles the loaded state by hiding the shimmer effect and displaying the
     * conversation list.
     */
    private void handleLoadedState() {
        hideShimmer();
        hideAllStates();
        binding.recyclerviewGroupMembersList.setVisibility(View.VISIBLE);
    }

    /**
     * Handles the error state by displaying an error message or a custom error view
     * if provided. It also hides other views that are not relevant during the error
     * state.
     */
    private void handleErrorState() {
        if (viewModel.getGroupMemberArrayList().isEmpty()) {
            hideShimmer();
            hideAllStates();
            binding.errorGroupMembersLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the empty state by displaying a message indicating there are no
     * conversations. It shows a custom empty view if provided.
     */
    private void handleEmptyState() {
        hideShimmer();
        hideErrorState();
        binding.emptyGroupMembersLayout.setVisibility(View.VISIBLE);
        binding.recyclerviewGroupMembersList.setVisibility(View.GONE);
    }

    /**
     * Handles the non-empty state by ensuring the conversation list is visible.
     */
    private void handleNonEmptyState() {
        hideShimmer();
        hideAllStates();
        binding.recyclerviewGroupMembersList.setVisibility(View.VISIBLE);
    }

    /**
     * Scrolls the RecyclerView to the top if the first visible item position is
     * less than 5.
     */
    private void scrollToTop() {
        if (layoutManager.findFirstVisibleItemPosition() < 5) layoutManager.scrollToPosition(0);
    }

    /**
     * Hides the shimmer effect and the parent layout containing it.
     */
    private void hideShimmer() {
        binding.shimmerParentLayout.setVisibility(View.GONE);
        binding.shimmerEffectFrame.stopShimmer();
    }

    /**
     * Hides all state layouts (custom, error, and empty layouts).
     */
    private void hideAllStates() {
        binding.groupMembersCustomLayout.setVisibility(View.GONE);
        binding.errorGroupMembersLayout.setVisibility(View.GONE);
        binding.emptyGroupMembersLayout.setVisibility(View.GONE);
    }

    /**
     * Hides the error state layout.
     */
    private void hideErrorState() {
        binding.errorGroupMembersLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewModel.removeListeners();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewModel.addListeners();
    }

    /**
     * Sets the group for the CometChatGroupMembers view.
     *
     * @param group The Group object to be set.
     */
    public void setGroup(Group group) {
        groupMembersAdapter.setGroup(group);
        viewModel.setGroup(group);
        viewModel.fetchGroupMember();
    }

    /**
     * Hides or shows the error state view.
     *
     * @param hide True to hide the error state view, false to show it.
     */
    public void hideError(boolean hide) {
        hideError = hide;
    }

    /**
     * Retrieves the current OnError callback instance.
     *
     * @return The OnError callback instance used for handling errors.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets the error callback for handling errors.
     *
     * @param onError The error callback to set.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    /**
     * Checks if the error state is hidden.
     *
     * @return {@code true} if the error state is hidden; {@code false} otherwise.
     */
    public boolean isHideError() {
        return hideError;
    }

    /**
     * Sets a listener to handle back press events.
     *
     * @param onBackPress The listener that will be notified of back press events. If
     *                    {@code null} is provided, the current listener remains unchanged.
     */
    public void setOnBackPressListener(OnBackPress onBackPress) {
        if (onBackPress != null) this.onBackPress = onBackPress;
    }

    /**
     * Sets the visibility of the toolbar.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setToolbarVisibility(int visibility) {
        binding.toolbar.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the title TextView.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setTitleVisibility(int visibility) {
        binding.tvTitle.setVisibility(visibility);
    }
}
