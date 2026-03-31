package com.cometchat.chatuikit.search;

import com.cometchat.chat.core.ConversationsRequest;
import com.cometchat.chat.core.MessagesRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatSearchBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.SearchScope;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatRichTextFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.OnBackPress;
import com.cometchat.chatuikit.shared.interfaces.OnEmpty;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnLoad;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderDecoration;
import com.cometchat.chatuikit.shared.viewholders.ConversationsSearchViewHolderListener;
import com.cometchat.chatuikit.shared.viewholders.MessagesSearchViewHolderListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CometChatSearch extends MaterialCardView {
    private static final String TAG = CometChatSearch.class.getSimpleName();

    private boolean isDetachedFromWindow;

    private static final UIKitConstants.SearchFilter[] MESSAGE_FILTERS = {
            UIKitConstants.SearchFilter.AUDIO,
            UIKitConstants.SearchFilter.DOCUMENTS,
            UIKitConstants.SearchFilter.PHOTOS,
            UIKitConstants.SearchFilter.VIDEOS,
            UIKitConstants.SearchFilter.LINKS
    };

    private static final UIKitConstants.SearchFilter[] CONVERSATION_FILTERS = {
            UIKitConstants.SearchFilter.GROUPS,
            UIKitConstants.SearchFilter.UNREAD
    };

    private String uid;
    private String guid;

    private final List<UIKitConstants.SearchFilter> selectedFilterChip = new ArrayList<>();
    private final List<CometChatTextFormatter> textFormatters = new ArrayList<>();

    private CometchatSearchBinding binding;
    private OnBackPress onBackListener;
    private OnItemClick<Conversation> onConversationClickedListener;
    private OnItemClick<BaseMessage> onMessageClickedListener;
    private List<UIKitConstants.SearchFilter> searchFilters;
    private CometChatSearchViewModel cometChatSearchViewModel;
    private LifecycleOwner lifecycleOwner;
    private CometChatSearchConversationsAdapter cometChatSearchConversationsAdapter;
    private CometChatSearchMessageListAdapter cometChatSearchMessageAdapter;
    private List<SearchScope> searchScopes = new ArrayList<>();

    private LinearLayoutManager conversationLayoutManager;
    private LinearLayoutManager messageLayoutManager;

    private CometChatMentionsFormatter cometchatMentionsFormatter;

    private boolean isChipClickLocked = false;

    private boolean isConversationListEmpty = true;
    private boolean isMessageListEmpty = true;
    private boolean isSearchInProgress = false;
    private boolean hasMorePreviousMessages;
    private boolean hasMorePreviousConversations;

    private int errorStateVisibility = View.VISIBLE;
    private int loadingStateVisibility = View.VISIBLE;
    private int emptyStateVisibility = View.VISIBLE;
    private int initialStateVisibility = View.VISIBLE;

    private View customInitialView = null;
    private View customEmptyView = null;
    private View customErrorView = null;
    private View customLoadingView = null;

    private @ColorInt int backgroundColor;

    // Search Bar Styling
    private @ColorInt int searchBarBackgroundColor;
    private @Dimension int searchBarStrokeWidth;
    private @ColorInt int searchBarStrokeColor;
    private @Dimension int searchBarCornerRadius;
    private @ColorInt int searchBarTextColor;
    private @StyleRes int searchBarTextAppearance;
    private @ColorInt int searchBarHintTextColor;

    // Search Bar Icons
    private Drawable backIcon;
    private @ColorInt int backIconTint;
    private Drawable clearIcon;
    private @ColorInt int clearIconTint;
    private Drawable searchIcon;
    private @ColorInt int searchIconTint;

    // Filter Chip Styling
    private @ColorInt int filterChipBackgroundColor;
    private @ColorInt int filterChipSelectedBackgroundColor;
    private @ColorInt int filterChipTextColor;
    private @ColorInt int filterChipSelectedTextColor;
    private @StyleRes int filterChipTextAppearance;
    private @ColorInt int filterChipStrokeColor;
    private @ColorInt int filterChipSelectedStrokeColor;
    private @Dimension int filterChipStrokeWidth;
    private @Dimension int filterChipCornerRadius;

    // Section Header Styling
    private @ColorInt int sectionHeaderTextColor;
    private @StyleRes int sectionHeaderTextAppearance;
    private @ColorInt int sectionHeaderBackgroundColor;

    // Conversation Item Styling
    private @ColorInt int conversationItemBackgroundColor;
    private @ColorInt int conversationTitleTextColor;
    private @StyleRes int conversationTitleTextAppearance;
    private @ColorInt int conversationSubtitleTextColor;
    private @StyleRes int conversationSubtitleTextAppearance;
    private @ColorInt int conversationTimestampTextColor;
    private @StyleRes int conversationTimestampTextAppearance;

    // Message Item Styling
    private @ColorInt int messageItemBackgroundColor;
    private @ColorInt int messageTitleTextColor;
    private @StyleRes int messageTitleTextAppearance;
    private @ColorInt int messageSubtitleTextColor;
    private @StyleRes int messageSubtitleTextAppearance;
    private @ColorInt int messageTimestampTextColor;
    private @StyleRes int messageTimestampTextAppearance;
    private @ColorInt int messageLinkTextColor;
    private @StyleRes int messageLinkTextAppearance;
    private Drawable messageThreadIcon;

    // Date Separator Styling
    private @StyleRes int dateSeparatorTextAppearance;
    private @ColorInt int dateSeparatorBackgroundColor;
    private @ColorInt int dateSeparatorTextColor;

    // Avatar and Badge Styles
    private @StyleRes int avatarStyle;
    private @StyleRes int badgeStyle;

    // Empty State Styling
    private @ColorInt int emptyStateTextColor;
    private @StyleRes int emptyStateTextAppearance;
    private @ColorInt int emptyStateSubtitleTextColor;
    private @StyleRes int emptyStateSubtitleTextAppearance;
    private Drawable emptyStateIcon;
    private @ColorInt int emptyStateIconTint;

    // Initial State Styling
    private @ColorInt int initialStateTextColor;
    private @StyleRes int initialStateTextAppearance;
    private @ColorInt int initialStateSubtitleTextColor;
    private @StyleRes int initialStateSubtitleTextAppearance;
    private Drawable initialStateIcon;
    private @ColorInt int initialStateIconTint;

    // Error State Styling
    private @ColorInt int errorStateTextColor;
    private @StyleRes int errorStateTextAppearance;
    private @ColorInt int errorStateSubtitleTextColor;
    private @StyleRes int errorStateSubtitleTextAppearance;
    private Drawable errorStateIcon;
    private @ColorInt int errorStateIconTint;

    // See more button styling
    private @ColorInt int seeMoreTextColor;
    private @StyleRes int seeMoreTextAppearance;
    private @StyleRes int messageTimestampDateStyle;

    /** Observer for conversation list changes */
    Observer<List<Conversation>> listObserver = new Observer<List<Conversation>>() {
        @Override
        public void onChanged(List<Conversation> conversations) {
            if (onLoadConversations != null) onLoadConversations.onLoad(conversations);
            isConversationListEmpty = conversations.isEmpty();
            cometChatSearchConversationsAdapter.setList(conversations);
        }
    };

    /** Observer for message list changes */
    Observer<List<BaseMessage>> messageListObserver = new Observer<List<BaseMessage>>() {
        @Override
        public void onChanged(List<BaseMessage> messageList) {
            if (onLoadMessages != null) onLoadMessages.onLoad(messageList);
            isMessageListEmpty = messageList.isEmpty();
            cometChatSearchMessageAdapter.setMessages(messageList);
        }
    };

    /** Observer for state changes */
    Observer<UIKitConstants.States> stateChangeObserver = states -> {
        switch (states) {
            case LOADING:
                handleLoadingState();
                break;
            case LOADED:
                handleLoadedState();
                break;
            case NON_EMPTY:
                handleNonEmptyState();
                break;
            case ERROR:
                handleErrorState();
                break;
            case EMPTY:
                handleEmptyState();
                break;
            case INITIAL:
                handleInitialState();
                break;
            default:
                break;
        }
    };
    private OnError onError;
    private OnLoad<BaseMessage> onLoadMessages;
    private OnLoad<Conversation> onLoadConversations;
    private OnEmpty onEmpty;
    private String mentionAllLabelId;

    private String mentionAllLabel;

    /** Constructor for CometChatSearch */
    public CometChatSearch(Context context) {
        this(context, null);
    }

    /** Constructor for CometChatSearch with attributes */
    public CometChatSearch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatSearchStyle);
    }

    /** Constructor for CometChatSearch with attributes and style */
    public CometChatSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /** Inflates and initializes the view */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatSearchBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        init();
        getDefaultMentionsFormatter();
        applyStyleAttributes(attrs, defStyleAttr);
        setupFilterChips();
    }

    /** Initializes the view components and state */
    private void init() {
        initClickListeners();
        setupRecyclerViews();
        initViewModel();
        initSearchBar();
        if (isConversationListEmpty && isMessageListEmpty) handleInitialState();
    }

    /** Initializes the search bar with listeners */
    private void initSearchBar() {
        binding.searchInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!textView.getText().toString().isEmpty()) {
                    fetchBasedOnSelection();
                }
                return true;
            }
            return false;
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty() && !hasActiveFilters()) {
                    hideAllStates();
                    handleInitialState();
                } else {
                    if (isSearchInProgress) return;
                    fetchBasedOnSelection();
                }
            }
        });
    }

    /** Sets up the RecyclerViews for conversations and messages */
    private void setupRecyclerViews() {
        setupConversationsRecyclerView();
        setupMessagesRecyclerView();

        binding.nestedScrollView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (isViewVisible(binding.recyclerviewConversations) && isRecyclerViewAtBottom(binding.recyclerviewConversations, v)) {
                String searchText = binding.searchInput.getEditableText().toString();
                if (searchText.isEmpty() || hasSelectedConversationFilters()) {
                    if (shouldFetchConversations()) {
                        fetchMore(UIKitConstants.SearchMode.CONVERSATIONS);
                    }
                }
            }

            if (isViewVisible(binding.recyclerviewMessages) && isRecyclerViewAtBottom(binding.recyclerviewMessages, v)) {
                String searchText = binding.searchInput.getEditableText().toString();
                if (searchText.isEmpty() || hasSelectedMessageFilters()) {
                    if (shouldFetchMessages()) {
                        fetchMore(UIKitConstants.SearchMode.MESSAGES);
                    }
                }
            }
        });
    }

    /** Determines if more messages should be fetched */
    private boolean shouldFetchMessages() {
        int lastVisible = messageLayoutManager.findLastVisibleItemPosition();
        int totalCount = binding.recyclerviewMessages.getAdapter() != null ? binding.recyclerviewMessages.getAdapter().getItemCount() : 0;
        boolean isLastItemVisible = lastVisible >= totalCount - 1;
        boolean isRecyclerViewAtEnd = !binding.recyclerviewMessages.canScrollVertically(1);
        return hasMorePreviousMessages && (isLastItemVisible || isRecyclerViewAtEnd);
    }

    /** Determines if more conversations should be fetched */
    private boolean shouldFetchConversations() {
        int lastVisible = conversationLayoutManager.findLastVisibleItemPosition();
        int totalCount = binding.recyclerviewConversations.getAdapter() != null ? binding.recyclerviewConversations.getAdapter().getItemCount() : 0;
        boolean isLastItemVisible = lastVisible >= totalCount - 1;
        boolean isRecyclerViewAtEnd = !binding.recyclerviewConversations.canScrollVertically(1);
        return hasMorePreviousConversations && (isLastItemVisible || isRecyclerViewAtEnd);
    }

    /** Checks if a view is visible */
    private boolean isViewVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    /** Checks if the RecyclerView is at the bottom of the NestedScrollView */
    private boolean isRecyclerViewAtBottom(RecyclerView recyclerView, NestedScrollView scrollView) {
        int[] rvLocation = new int[2];
        int[] svLocation = new int[2];
        recyclerView.getLocationOnScreen(rvLocation);
        scrollView.getLocationOnScreen(svLocation);
        int rvBottom = rvLocation[1] + recyclerView.getHeight();
        int svBottom = svLocation[1] + scrollView.getHeight();
        return rvBottom <= svBottom + 10; // 10px tolerance
    }

    /** Sets up the messages RecyclerView */
    private void setupMessagesRecyclerView() {
        messageLayoutManager = new LinearLayoutManager(getContext());
        cometChatSearchMessageAdapter = new CometChatSearchMessageListAdapter(getContext());
        binding.recyclerviewMessages.setAdapter(cometChatSearchMessageAdapter);
        binding.recyclerviewMessages.setLayoutManager(messageLayoutManager);
        binding.recyclerviewMessages.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        StickyHeaderDecoration stickyHeaderDecoration = new StickyHeaderDecoration(cometChatSearchMessageAdapter);
        binding.recyclerviewMessages.addItemDecoration(stickyHeaderDecoration, 0);
    }

    /** Sets up the conversations RecyclerView */
    private void setupConversationsRecyclerView() {
        conversationLayoutManager = new LinearLayoutManager(getContext());
        cometChatSearchConversationsAdapter = new CometChatSearchConversationsAdapter(getContext());
        binding.recyclerviewConversations.setAdapter(cometChatSearchConversationsAdapter);
        binding.recyclerviewConversations.setLayoutManager(conversationLayoutManager);
        binding.recyclerviewConversations.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
    }

    /** Initializes the ViewModel and sets up observers */
    private void initViewModel() {
        cometChatSearchViewModel = new ViewModelProvider.NewInstanceFactory().create(CometChatSearchViewModel.class);
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner == null) return;
        attachObservers();
    }

    public void attachObservers() {
        cometChatSearchViewModel.getMutableLiveDataIsSearching().observe(lifecycleOwner, this::searchInProgress);
        cometChatSearchViewModel.getCometChatException().observe(lifecycleOwner, this::throwError);
        cometChatSearchViewModel.getMutableMessagesRangeChanged().observe(lifecycleOwner, this::notifyRangeChanged);
        cometChatSearchViewModel.getMutableConversationsRangeChanged().observe(lifecycleOwner, this::notifyConversationsRangeChanged);
        cometChatSearchViewModel.getMutableHasMorePreviousMessages().observe(lifecycleOwner, this::hasMorePreviousMessages);
        cometChatSearchViewModel.getMutableHasMorePreviousConversations().observe(lifecycleOwner, this::hasMorePreviousConversations);
        cometChatSearchViewModel.getMutableConversationList().observe(lifecycleOwner, listObserver);
        cometChatSearchViewModel.getMutableMessageList().observe(lifecycleOwner, messageListObserver);
        cometChatSearchViewModel.getStates().observe(lifecycleOwner, stateChangeObserver);
    }

    /**
     * Throws a CometChatException and handles the error by invoking the provided
     * onError callback.
     *
     * @param cometchatException The CometChatException to be thrown and handled.
     */
    public void throwError(CometChatException cometchatException) {
        if (onError != null) onError.onError(cometchatException);
    }

    /**
     * Notifies the adapter of a range of new conversations that have been inserted into the
     * data set.
     *
     * @param newCount the number of new conversations added to the adapter
     */
    private void notifyConversationsRangeChanged(int newCount) {
        cometChatSearchConversationsAdapter.notifyItemRangeInserted(cometChatSearchConversationsAdapter.getItemCount(), newCount);
    }

    /**
     * Updates the flag indicating whether there are more previous conversations
     * available for pagination.
     *
     * @param aBoolean true if there are more previous conversations, false otherwise
     */
    private void hasMorePreviousConversations(Boolean aBoolean) {
        this.hasMorePreviousConversations = aBoolean;
    }

    /**
     * Updates the flag indicating whether a search operation is currently in progress.
     *
     * @param aBoolean true if a search is in progress, false otherwise
     */
    private void searchInProgress(Boolean aBoolean) {
        isSearchInProgress = aBoolean;
    }

    /**
     * Updates the flag indicating whether there are more previous messages
     * available for pagination.
     *
     * @param aBoolean true if there are more previous messages, false otherwise
     */
    private void hasMorePreviousMessages(Boolean aBoolean) {
        this.hasMorePreviousMessages = aBoolean;
    }

    /**
     * Notifies the adapter of a range of new items that have been inserted into the
     * data set.
     *
     * @param newItemsCount the number of new items added to the adapter
     */
    public void notifyRangeChanged(int newItemsCount) {
        cometChatSearchMessageAdapter.notifyItemRangeInserted(cometChatSearchMessageAdapter.getItemCount(), newItemsCount);
    }

    /** Handles the initial state of the search view */
    private void handleInitialState() {
        if (initialStateVisibility == VISIBLE) {
            if (customInitialView != null) {
                Utils.handleView(binding.initialStateView, customInitialView, true);
            } else {
                hideAllStates();
                binding.initialStateView.setVisibility(VISIBLE);
            }
        } else binding.initialStateView.setVisibility(GONE);
    }

    /** Handles the visibility of RecyclerViews based on search results and filters */
    private void handleRecyclerViews() {
        if (!hasActiveFilters() && getCurrentSearchText().toString().isEmpty()) {
            hideAllStates();
            handleInitialState();
            return;
        }

        String searchText = binding.searchInput.getEditableText().toString();
        boolean hasFilterChipsOnly = hasActiveFilters() && searchText.isEmpty();

        if (!isMessageListEmpty && (hasActiveFilters() || !searchText.isEmpty())) {
            setMessageListRecyclerViewVisibility(VISIBLE);
            if (!searchText.isEmpty() && !hasActiveFilters() && hasMorePreviousMessages)
                binding.messagesSeeMore.setVisibility(VISIBLE);
            else binding.messagesSeeMore.setVisibility(GONE);

            if (hasFilterChipsOnly) {
                binding.messagesSectionHeader.setVisibility(GONE);
            } else {
                binding.messagesSectionHeader.setVisibility(VISIBLE);
            }
        } else {
            setMessageListRecyclerViewVisibility(GONE);
            binding.messagesSeeMore.setVisibility(GONE);
            binding.messagesSectionHeader.setVisibility(GONE);
        }

        if (!isConversationListEmpty && (hasActiveFilters() || !searchText.isEmpty())) {
            setConversationsRecyclerViewVisibility(VISIBLE);
            if (!searchText.isEmpty() && !hasActiveFilters() && hasMorePreviousMessages)
                binding.conversationsSeeMore.setVisibility(VISIBLE);
            else binding.conversationsSeeMore.setVisibility(GONE);

            if (hasFilterChipsOnly) {
                binding.conversationsSectionHeader.setVisibility(GONE);
            } else {
                binding.conversationsSectionHeader.setVisibility(VISIBLE);
            }
        } else {
            setConversationsRecyclerViewVisibility(GONE);
            binding.conversationsSeeMore.setVisibility(GONE);
            binding.conversationsSectionHeader.setVisibility(GONE);
        }
    }

    /** Sets the visibility of the conversations RecyclerView */
    private void setConversationsRecyclerViewVisibility(int visibility) {
        binding.recyclerviewConversations.setVisibility(visibility);
    }

    /** Sets the visibility of the message list RecyclerView */
    private void setMessageListRecyclerViewVisibility(int visibility) {
        binding.recyclerviewMessages.setVisibility(visibility);
    }

    /** Hides all state views */
    private void hideAllStates() {
        setConversationsRecyclerViewVisibility(GONE);
        setMessageListRecyclerViewVisibility(GONE);
        hideShimmer();
        binding.initialStateView.setVisibility(GONE);
        binding.emptyStateView.setVisibility(GONE);
        binding.errorStateView.setVisibility(GONE);
        binding.loadingStateView.setVisibility(GONE);
        binding.conversationsSeeMore.setVisibility(GONE);
        binding.messagesSeeMore.setVisibility(GONE);
    }

    /** Hides the shimmer loading effect */
    private void handleNonEmptyState() {
        hideAllStates();
        handleRecyclerViews();
    }

    /** Hides the shimmer loading effect */
    private void handleLoadedState() {
        hideAllStates();
    }

    /** Hides the shimmer loading effect */
    private void handleLoadingState() {
        if (loadingStateVisibility == VISIBLE) {
            if (customLoadingView != null) {
                Utils.handleView(binding.loadingStateView, customLoadingView, true);
            } else {
                hideAllStates();
                binding.loadingStateView.setVisibility(VISIBLE);
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.search_shimmer);
                binding.shimmerRecyclerview.setAdapter(adapter);
                binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerEffectFrame.startShimmer();
            }
        } else {
            binding.loadingStateView.setVisibility(GONE);
        }
    }

    /** Handles the error state of the search view */
    private void handleErrorState() {
        if (errorStateVisibility == VISIBLE) {
            if (customErrorView != null) {
                Utils.handleView(binding.errorStateView, customErrorView, true);
            } else {
                hideAllStates();
                binding.errorStateView.setVisibility(VISIBLE);
            }
        } else {
            setErrorStateVisibility(View.GONE);
        }
    }

    /** Handles the empty state of the search view */
    private void handleEmptyState() {
        if (onEmpty != null) onEmpty.onEmpty();
        if (emptyStateVisibility == VISIBLE) {
            if (customEmptyView != null) {
                binding.emptyStateView.removeAllViews();
                binding.emptyStateView.addView(customEmptyView);
                binding.emptyStateView.setVisibility(View.VISIBLE);
            } else {
                hideAllStates();
                binding.emptyStateView.setVisibility(VISIBLE);
            }
        } else {
            setEmptyStateVisibility(View.GONE);
        }
    }

    /** Initializes click listeners for various UI components */
    private void initClickListeners() {
        binding.ivBack.setOnClickListener(view -> {
            if (onBackListener != null) {
                onBackListener.onBack();
            }
        });
        binding.recyclerviewMessages.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerviewMessages, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                BaseMessage message = cometChatSearchMessageAdapter.getItemByPosition(position);
                if (onMessageClickedListener != null) {
                    onMessageClickedListener.click(view, position, message);
                }
            }
        }));
        binding.recyclerviewConversations.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerviewConversations, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = cometChatSearchConversationsAdapter.getItemByPosition(position);
                if (onConversationClickedListener != null) {
                    onConversationClickedListener.click(view, position, conversation);
                }
            }
        }));
        binding.ivClear.setOnClickListener(view -> {
            if (!binding.searchInput.getEditableText().toString().isEmpty()) {
                binding.searchInput.setText("");
            }
        });
        binding.conversationsSeeMore.setOnClickListener(v -> fetchMore(UIKitConstants.SearchMode.CONVERSATIONS));
        binding.messagesSeeMore.setOnClickListener(v -> fetchMore(UIKitConstants.SearchMode.MESSAGES));
    }

    /** Applies style attributes from XML to the view */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatSearch, defStyleAttr, 0);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchStyle, 0);
        typedArray = styleResId != 0
                ? getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatSearch)
                : null;
        extractAttributesAndApplyDefaults(typedArray);
    }

    /** Sets the style of the CometChatSearch
     * @param styleResId The resource ID of the style to be applied
     */
    public void setStyle (@StyleRes int styleResId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatSearch);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /** Extracts attributes from the TypedArray and applies default values */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            backgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            searchBarBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBarBackgroundColor, CometChatTheme.getBackgroundColor3(getContext()));
            searchBarStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatSearch_cometchatSearchBarStrokeWidth, 0);
            searchBarStrokeColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBarStrokeColor, CometChatTheme.getStrokeColorDark(getContext()));
            searchBarCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatSearch_cometchatSearchBarCornerRadius, 0);
            searchBarTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBarTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            searchBarTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchBarTextAppearance, 0);
            searchBarHintTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBarHintTextColor, CometChatTheme.getTextColorTertiary(getContext()));

            backIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchBackIcon);
            backIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchBackIconTint, CometChatTheme.getIconTintPrimary(getContext()));
            clearIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchClearIcon);
            clearIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchClearIconTint, CometChatTheme.getIconTintSecondary(getContext()));
            searchIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchIcon);
            searchIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchIconTint, CometChatTheme.getIconTintSecondary(getContext()));

            filterChipBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipBackgroundColor, CometChatTheme.getBackgroundColor3(getContext()));
            filterChipSelectedBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedBackgroundColor, CometChatTheme.getSecondaryButtonBackgroundColor(getContext()));
            filterChipTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            filterChipSelectedTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedTextColor, CometChatTheme.getTextColorWhite(getContext()));
            filterChipTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchFilterChipTextAppearance, 0);
            filterChipStrokeColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipStrokeColor, CometChatTheme.getColorTransparent(getContext()));
            filterChipSelectedStrokeColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchFilterChipSelectedStrokeColor, CometChatTheme.getColorTransparent(getContext()));
            filterChipStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatSearch_cometchatSearchFilterChipStrokeWidth, 0);
            filterChipCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatSearch_cometchatSearchFilterChipCornerRadius, 0);

            sectionHeaderTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchSectionHeaderTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            sectionHeaderTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchSectionHeaderTextAppearance, 0);
            sectionHeaderBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchSectionHeaderBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));

            conversationItemBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchConversationItemBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            conversationTitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchConversationTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            conversationTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchConversationTitleTextAppearance, 0);
            conversationSubtitleTextColor =  typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchConversationSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            conversationSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchConversationSubtitleTextAppearance, 0);
            conversationTimestampTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchConversationTimestampTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            conversationTimestampTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchConversationTimestampTextAppearance, 0);

            messageItemBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchMessageItemBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            messageTitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchMessageTitleTextColor,  CometChatTheme.getTextColorPrimary(getContext()));
            messageTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchMessageTitleTextAppearance, 0);
            messageSubtitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchMessageSubtitleTextColor,  CometChatTheme.getTextColorSecondary(getContext()));
            messageSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchMessageSubtitleTextAppearance, 0);
            messageTimestampTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchMessageTimestampTextColor,  CometChatTheme.getTextColorSecondary(getContext()));
            messageTimestampTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchMessageTimestampTextAppearance, 0);
            messageLinkTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchMessageLinkTextColor,  CometChatTheme.getInfoColor(getContext()));
            messageLinkTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchMessageLinkTextAppearance, 0);
            messageThreadIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometChatSearchMessageThreadIcon);

            avatarStyle = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchAvatarStyle, 0);
            badgeStyle = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchBadgeStyle, 0);

            emptyStateTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchEmptyStateTextColor,  CometChatTheme.getTextColorPrimary(getContext()));
            emptyStateTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchEmptyStateTextAppearance, 0);
            emptyStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchEmptyStateSubtitleTextColor,  CometChatTheme.getTextColorSecondary(getContext()));
            emptyStateSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchEmptyStateSubtitleTextAppearance, 0);
            emptyStateIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchEmptyStateIcon);
            emptyStateIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchEmptyStateIconTint,  CometChatTheme.getIconTintSecondary(getContext()));

            initialStateTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchInitialStateTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            initialStateTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchInitialStateTextAppearance, 0);
            initialStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchInitialStateSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            initialStateSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchInitialStateSubtitleTextAppearance, 0);
            initialStateIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchInitialStateIcon);
            initialStateIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchInitialStateIconTint, CometChatTheme.getTextColorSecondary(getContext()));

            errorStateTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchErrorStateTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            errorStateTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchErrorStateTextAppearance, 0);
            errorStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchErrorStateSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            errorStateSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchErrorStateSubtitleTextAppearance, 0);
            errorStateIcon = typedArray.getDrawable(R.styleable.CometChatSearch_cometchatSearchErrorStateIcon);
            errorStateIconTint = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchErrorStateIconTint, CometChatTheme.getIconTintSecondary(getContext()));

            seeMoreTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchSeeMoreTextColor, CometChatTheme.getPrimaryButtonTextColor(getContext()));
            seeMoreTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchSeeMoreTextAppearance, 0);

            dateSeparatorTextColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchDateSeparatorTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            dateSeparatorTextAppearance = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchDateSeparatorTextAppearance, 0);
            dateSeparatorBackgroundColor = typedArray.getColor(R.styleable.CometChatSearch_cometchatSearchDateSeparatorBackgroundColor, CometChatTheme.getColorTransparent(getContext()));

            messageTimestampDateStyle = typedArray.getResourceId(R.styleable.CometChatSearch_cometchatSearchMessageDateStyle, 0);

            this.setPadding(12, 12, 12, 12);
            applyDefaults();
        } finally {
            typedArray.recycle();
        }
    }

    /** Applies default styles to the view components */
    private void applyDefaults() {
        setBackgroundColor(backgroundColor);

        setSearchBarBackgroundColor(searchBarBackgroundColor);
        setSearchBarStrokeWidth(searchBarStrokeWidth);
        setSearchBarStrokeColor(searchBarStrokeColor);
        setSearchBarCornerRadius(searchBarCornerRadius);
        setSearchBarTextColor(searchBarTextColor);
        setSearchBarTextAppearance(searchBarTextAppearance);
        setSearchBarHintTextColor(searchBarHintTextColor);

        setBackIcon(backIcon);
        setBackIconTint(backIconTint);
        setClearIcon(clearIcon);
        setClearIconTint(clearIconTint);
        setSearchIcon(searchIcon);
        setSearchIconTint(searchIconTint);

        setFilterChipBackgroundColor(filterChipBackgroundColor);
        setFilterChipSelectedBackgroundColor(filterChipSelectedBackgroundColor);
        setFilterChipTextColor(filterChipTextColor);
        setFilterChipSelectedTextColor(filterChipSelectedTextColor);
        setFilterChipTextAppearance(filterChipTextAppearance);
        setFilterChipStrokeColor(filterChipStrokeColor);
        setFilterChipSelectedStrokeColor(filterChipSelectedStrokeColor);
        setFilterChipStrokeWidth(filterChipStrokeWidth);
        setFilterChipCornerRadius(filterChipCornerRadius);

        setSectionHeaderTextColor(sectionHeaderTextColor);
        setSectionHeaderTextAppearance(sectionHeaderTextAppearance);
        setSectionHeaderBackgroundColor(sectionHeaderBackgroundColor);

        setConversationItemBackgroundColor(conversationItemBackgroundColor);
        setConversationTitleTextColor(conversationTitleTextColor);
        setConversationTitleTextAppearance(conversationTitleTextAppearance);
        setConversationSubtitleTextColor(conversationSubtitleTextColor);
        setConversationSubtitleTextAppearance(conversationSubtitleTextAppearance);
        setConversationTimestampTextColor(conversationTimestampTextColor);
        setConversationTimestampTextAppearance(conversationTimestampTextAppearance);

        setMessageItemBackgroundColor(messageItemBackgroundColor);
        setMessageTitleTextColor(messageTitleTextColor);
        setMessageTitleTextAppearance(messageTitleTextAppearance);
        setMessageSubtitleTextColor(messageSubtitleTextColor);
        setMessageSubtitleTextAppearance(messageSubtitleTextAppearance);
        setMessageTimestampTextColor(messageTimestampTextColor);
        setMessageTimestampTextAppearance(messageTimestampTextAppearance);
        setMessageLinkTextAppearance(messageLinkTextAppearance);
        setMessageLinkTextColor(messageLinkTextColor);
        setMessageThreadIcon(messageThreadIcon);

        setAvatarStyle(avatarStyle);
        setBadgeStyle(badgeStyle);

        setEmptyStateTextColor(emptyStateTextColor);
        setEmptyStateTextAppearance(emptyStateTextAppearance);
        setEmptyStateSubtitleTextColor(emptyStateSubtitleTextColor);
        setEmptyStateSubtitleTextAppearance(emptyStateSubtitleTextAppearance);
        setEmptyStateIcon(emptyStateIcon);
        setEmptyStateIconTint(emptyStateIconTint);

        setInitialStateTextColor(initialStateTextColor);
        setInitialStateTextAppearance(initialStateTextAppearance);
        setInitialStateSubtitleTextColor(initialStateSubtitleTextColor);
        setInitialStateSubtitleTextAppearance(initialStateSubtitleTextAppearance);
        setInitialStateIcon(initialStateIcon);
        setInitialStateIconTint(initialStateIconTint);

        setErrorStateTextColor(errorStateTextColor);
        setErrorStateTextAppearance(errorStateTextAppearance);
        setErrorStateSubtitleTextColor(errorStateSubtitleTextColor);
        setErrorStateSubtitleTextAppearance(errorStateSubtitleTextAppearance);
        setErrorStateIcon(errorStateIcon);
        setErrorStateIconTint(errorStateIconTint);

        setSeeMoreTextColor(seeMoreTextColor);
        setSeeMoreTextAppearance(seeMoreTextAppearance);

        setDateSeparatorTextColor(dateSeparatorTextColor);
        setDateSeparatorTextAppearance(dateSeparatorTextAppearance);
        setDateSeparatorBackgroundColor(dateSeparatorBackgroundColor);
    }

    private void setMessageThreadIcon(Drawable threadIcon) {
        this.messageThreadIcon = threadIcon;
        cometChatSearchMessageAdapter.setMessageThreadIcon(threadIcon);
    }

    /**
     * Sets the initial search filter to be selected when the component launches.
     * This method should be called before the component is displayed to the user.
     *
     * @param initialSearchFilter The SearchFilter to be initially selected. If null, no filter will be pre-selected.
     */
    public void setInitialSearchFilter(UIKitConstants.SearchFilter initialSearchFilter) {
        if (initialSearchFilter == null) {
            return;
        }

        if (searchFilters == null) {
            searchFilters = getDefaultSearchFilters();
        }

        if (!searchFilters.contains(initialSearchFilter)) {
            return;
        }

        clearSelectedFilters();
        toggleFilter(initialSearchFilter);

        if (binding != null) {
            refreshChipSelectionState();
            fetchBasedOnSelection();
        }
    }

    /**
     * Sets the back button listener.
     * @param listener The listener to be invoked when the back button is pressed.
     */
    public void setOnBackPressListener(OnBackPress listener) {
        this.onBackListener = listener;
    }

    /**
     * Sets the conversation clicked listener.
     * @param listener The listener to be invoked when a conversation is clicked.
     */
    public void setOnConversationClicked(OnItemClick<Conversation> listener) {
        this.onConversationClickedListener = listener;
    }

    /**
     * Sets the message clicked listener.
     * @param listener The listener to be invoked when a message is clicked.
     */
    public void setOnMessageClicked(OnItemClick<BaseMessage> listener) {
        this.onMessageClickedListener = listener;
    }

    /** * Returns the current list of search filters.
     * If no filters are set, it returns a default list of filters.
     *
     * @return List of SearchFilter enums representing the active search filters.
     */
    public List<UIKitConstants.SearchFilter> getSearchFilters() {
        return searchFilters;
    }

    /** * Sets the search filters to be used in the search functionality.
     * If no filters are provided, it defaults to a predefined set of filters.
     *
     * @param filters List of SearchFilter enums representing the desired search filters.
     */
    public void setSearchFilters(List<UIKitConstants.SearchFilter> filters) {
        this.searchFilters = filters;
        setupFilterChips();
    }

    /*** Sets a custom view for conversation items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for conversation items.
     */
    public void setConversationItemView(ConversationsSearchViewHolderListener viewHolderListener) {
        cometChatSearchConversationsAdapter.setConversationItemView(viewHolderListener);
    }

    /*** Sets a custom view for text message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for text message items.
     */
    public void setImageMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setImageMessageItemView(viewHolderListener);
    }

    /*** Sets a custom view for audio message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for audio message items.
     */
    public void setAudioMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setAudioMessageItemView(viewHolderListener);
    }

    /*** Sets a custom view for video message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for video message items.
     */
    public void setVideoMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setVideoMessageItemView(viewHolderListener);
    }

    /*** Sets a custom view for document message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for document message items.
     */
    public void setDocumentMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setDocumentMessageItemView(viewHolderListener);
    }

    /*** Sets a custom view for link message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for link message items.
     */
    public void setLinkMessageItemView(MessagesSearchViewHolderListener<TextMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setLinkMessageItemView(viewHolderListener);
    }

    /*** Sets a custom view for text message items in the search results.
     *
     * @param viewHolderListener The listener that provides the custom view for text message items.
     */
    public void setTextMessageItemView(MessagesSearchViewHolderListener<TextMessage> viewHolderListener) {
        cometChatSearchMessageAdapter.setTextMessageItemView(viewHolderListener);
    }

    /** Hides the shimmer loading effect */
    private void hideShimmer() {
        binding.shimmerEffectFrame.stopShimmer();
        binding.loadingStateView.setVisibility(View.GONE);
    }

    /** Sets the visibility of the loading state view */
    private void setLoadingStateVisibility(int visibility) {
        loadingStateVisibility = visibility;
    }

    /** Sets the visibility of the initial state view */
    private void setInitialStateVisibility(int visibility) {
        this.initialStateVisibility = visibility;
        binding.initialStateView.setVisibility(visibility);
    }

    /** Sets the visibility of the empty state view */
    public void setEmptyStateVisibility(int visibility) {
        this.emptyStateVisibility = visibility;
    }

    /** Sets the visibility of the error state view */
    public void setErrorStateVisibility(int visibility) {
        this.errorStateVisibility = visibility;
    }

    /** Sets a custom view to be displayed initially.
     *
     * @param initialView The custom view to display for the empty state.
     */
    public void setInitialView(@LayoutRes int initialView) {
        if (initialView != 0) {
            try {
                customInitialView = View.inflate(getContext(), initialView, null);
            } catch (Exception e) {
                customInitialView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /** * Sets a custom view to be displayed when the search results are empty.
     *
     * @param emptyView The custom view to display for the empty state.
     */
    public void setEmptyView(@LayoutRes int emptyView) {
        if (emptyView != 0) {
            try {
                customEmptyView = View.inflate(getContext(), emptyView, null);
            } catch (Exception e) {
                customEmptyView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /** * Sets a custom view to be displayed during the loading state.
     *
     * @param loadingView The custom view to display for the loading state.
     */
    public void setLoadingView(View loadingView) {
        this.customLoadingView = loadingView;
    }

    /** * Sets a custom view to be displayed during the error state.
     *
     * @param errorView The custom view to display for the error state.
     */
    public void setErrorView(View errorView) {
        this.customErrorView = errorView;
    }

    /**
     * Retrieves the default mentions formatter from the available text formatters
     * and adds it to the list. This method searches through the available text
     * formatters and assigns the first instance of CometChatMentionsFormatter found
     * to cometchatMentionsFormatter.
     */
    private void getDefaultMentionsFormatter() {
        for (CometChatTextFormatter textFormatter : CometChatUIKit.getDataSource().getTextFormatters(getContext(), new AdditionParameter())) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                cometchatMentionsFormatter.setMentionAllLabel(mentionAllLabelId, mentionAllLabel);
            } else if (textFormatter instanceof CometChatRichTextFormatter) {
                this.textFormatters.add(textFormatter);
            }
        }
        this.textFormatters.add(cometchatMentionsFormatter);
        processFormatters();
    }

    /**
     * Returns the current search scopes.
     *
     * @return List of SearchScope enums indicating where to search (e.g., CONVERSATIONS, MESSAGES).
     */
    public List<SearchScope> getSearchIn() {
        return searchScopes;
    }

    /**
     * Sets the scopes to search in.
     *
     * @param scopes List of SearchScope enums indicating where to search (e.g., CONVERSATIONS, MESSAGES).
     */
    public void setSearchIn(List<SearchScope> scopes) {
        this.searchScopes = scopes;
        cometChatSearchViewModel.setSearchScopes(scopes);
    }

    /**
     * Sets whether to hide the group type in the search results.
     *
     * @param hide true to hide the group type, false to show it.
     */
    public void setHideGroupType(boolean hide) {
        cometChatSearchConversationsAdapter.setHideGroupType(hide);
    }

    /**
     * Sets whether to hide the user status in the search results.
     *
     * @param bool true to hide the user status, false to show it.
     */
    public void setHideUserStatus(boolean bool) {
        cometChatSearchConversationsAdapter.setHideUserStatus(bool);
    }

    /**
     * Sets the list of text formatters to use for formatting messages.
     *
     * @param cometchatTextFormatters the list of text formatters to apply.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.textFormatters.addAll(cometchatTextFormatters);
            processFormatters();
        }
    }

    /**
     * Processes the current list of text formatters and updates the conversations
     * adapter.
     */
    private void processFormatters() {
        cometChatSearchConversationsAdapter.setTextFormatters(textFormatters);
        cometChatSearchMessageAdapter.setTextFormatters(textFormatters);
    }

    /** * Sets a custom date-time formatter for displaying timestamps.
     *
     * @param dateTimeFormatter The DateTimeFormatterCallback implementation to format date and time.
     */
    public void setDateTimeFormatter(@NonNull DateTimeFormatterCallback dateTimeFormatter) {
         cometChatSearchConversationsAdapter.setDateTimeFormatter(dateTimeFormatter);
    }

    /** * Returns the user ID (UID) for user-specific searches.
     *
     * @return The unique identifier of the user.
     */
    public String getUid() {
        return uid;
    }

    /** * Sets the user ID (UID) for user-specific searches.
     *
     * @param uid The unique identifier of the user.
     */
    public void setUid(String uid) {
        this.uid = uid;
        cometChatSearchViewModel.setUid(uid);
        cometChatSearchMessageAdapter.setUid(uid);
    }

    /** * Returns the group ID (GUID) for group-specific searches.
     *
     * @return The unique identifier of the group.
     */
    public String getGuid() {
        return guid;
    }

    /** * Sets the group ID (GUID) for group-specific searches.
     *
     * @param guid The unique identifier of the group.
     */
    public void setGuid(String guid) {
        this.guid = guid;
        cometChatSearchViewModel.setGuid(guid);
        cometChatSearchMessageAdapter.setGuid(guid);
    }

    /** * Sets the ConversationsRequestBuilder for fetching conversations.
     *
     * @param conversationsRequestBuilder The ConversationsRequestBuilder instance to use.
     */
    public void setConversationsRequestBuilder(ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder) {
        cometChatSearchViewModel.setConversationsRequestBuilder(conversationsRequestBuilder);
    }

    /** * Sets the MessagesRequestBuilder for fetching messages.
     *
     * @param builder The MessagesRequestBuilder instance to use.
     */
    public void setMessagesRequestBuilder(MessagesRequest.MessagesRequestBuilder builder) {
        cometChatSearchViewModel.setMessagesRequestBuilder(builder);
    }

    /**
     * Retrieves the error callback.
     *
     * @return An instance of {@link OnError} that handles error events.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets the callback for handling errors in the message list.
     *
     * @param onError The OnError object representing the error callback.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    /**
     * Retrieves the callback for handling data loading events.
     *
     * @return An instance of {@link OnLoad} for handling the loading of {@link BaseMessage} objects.
     */
    public OnLoad<BaseMessage> getOnLoadMessages() {
        return onLoadMessages;
    }

    /**
     * Sets the callback for handling data loading events.
     *
     * @param onLoad An instance of {@link OnLoad} that is triggered when messages are being loaded.
     */
    public void setOnLoadMessages(OnLoad<BaseMessage> onLoad) {
        this.onLoadMessages = onLoad;
    }

    /**
     * Retrieves the callback for handling data loading events.
     *
     * @return An instance of {@link OnLoad} for handling the loading of {@link BaseMessage} objects.
     */
    public OnLoad<Conversation> getOnLoadConversations() {
        return onLoadConversations;
    }

    /**
     * Sets the callback for handling data loading events.
     *
     * @param onLoad An instance of {@link OnLoad} that is triggered when messages are being loaded.
     */
    public void setOnLoadConversations(OnLoad<Conversation> onLoad) {
        this.onLoadConversations = onLoad;
    }

    /**
     * Retrieves the callback for handling empty state events.
     *
     * @return An instance of {@link OnEmpty} triggered when no messages are available.
     */
    public OnEmpty getOnEmpty() {
        return onEmpty;
    }

    /**
     * Sets the callback for handling empty state events.
     *
     * @param onEmpty An instance of {@link OnEmpty} that is triggered when there are no messages available.
     */
    public void setOnEmpty(OnEmpty onEmpty) {
        this.onEmpty = onEmpty;
    }

    /**
     * Returns the default search filters used when no custom filters are provided.
     *
     * @return List of default SearchFilter enums.
     */
    private List<UIKitConstants.SearchFilter> getDefaultSearchFilters() {
        return Arrays.asList(
                UIKitConstants.SearchFilter.UNREAD,
                UIKitConstants.SearchFilter.GROUPS,
                UIKitConstants.SearchFilter.PHOTOS,
                UIKitConstants.SearchFilter.VIDEOS,
                UIKitConstants.SearchFilter.LINKS,
                UIKitConstants.SearchFilter.DOCUMENTS,
                UIKitConstants.SearchFilter.AUDIO
        );
    }

    /**
     * Sets up the filter chips in the UI based on the provided search filters.
     * If no filters are provided, it defaults to a predefined set of filters.
     */
    private void setupFilterChips() {
        binding.chipGroup.removeAllViews();
        if (searchFilters == null) {
            searchFilters = getDefaultSearchFilters();
        }

        for (int i = 0; i < searchFilters.size(); i++) {
            MaterialCardView chipCard = new MaterialCardView(getContext());
            Utils.initMaterialCard(chipCard);

            LinearLayout chipContainer = new LinearLayout(getContext());
            chipContainer.setOrientation(LinearLayout.HORIZONTAL);
            chipContainer.setGravity(Gravity.CENTER);
            chipContainer.setPadding(
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_3),
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2),
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_3),
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_padding_2)
            );

            ImageView chipIconView = new ImageView(getContext());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_14dp),
                getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_14dp)
            );
            iconParams.setMarginEnd(getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_1));
            chipIconView.setLayoutParams(iconParams);
            chipIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            TextView chipTextView = new TextView(getContext());
            chipTextView.setText(searchFilters.get(i).toString().toLowerCase());
            chipTextView.setGravity(Gravity.CENTER);
            chipTextView.setMaxLines(1);
            chipTextView.setEllipsize(TextUtils.TruncateAt.END);

            createFilterChips(chipIconView, chipTextView, searchFilters.get(i));

            final int position = i;
            boolean isSelected = selectedFilterChip.contains(searchFilters.get(i));

            applyChipStyling(chipCard, chipIconView, chipTextView, isSelected);

            chipCard.setOnClickListener(v -> {
                if (isChipClickLocked) return;
                isChipClickLocked = true;
                handleFilterClick(position);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isChipClickLocked = false;
                }, 50);
            });
            chipContainer.addView(chipIconView);
            chipContainer.addView(chipTextView);

            chipCard.addView(chipContainer);

            MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMarginEnd(getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2));
            layoutParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2);
            chipCard.setLayoutParams(layoutParams);
            binding.chipGroup.addView(chipCard);
            chipCard.setTag(isSelected);
        }
    }

    /**
     * Creates filter chips based on the provided search filter type.
     *
     * @param imageView   The ImageView representing the chip's icon.
     * @param textView    The TextView representing the chip's text.
     * @param searchFilter The type of search filter to create the chip for.
     */
    private void createFilterChips(ImageView imageView, TextView textView, UIKitConstants.SearchFilter searchFilter) {
        switch (searchFilter) {
            case UNREAD:
                setChipIcon(imageView, R.drawable.cometchat_ic_unread_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_unread));
                break;
            case GROUPS:
                setChipIcon(imageView, R.drawable.cometchat_ic_group_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_groups));
                break;
            case PHOTOS:
                setChipIcon(imageView, R.drawable.cometchat_ic_photo_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_photos));
                break;
            case VIDEOS:
                setChipIcon(imageView, R.drawable.cometchat_ic_video_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_videos));
                break;
            case LINKS:
                setChipIcon(imageView, R.drawable.cometchat_ic_link_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_links));
                break;
            case DOCUMENTS:
                setChipIcon(imageView, R.drawable.cometchat_ic_document_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_documents));
                break;
            case AUDIO:
                setChipIcon(imageView, R.drawable.cometchat_ic_audio_outlined);
                setChipText(textView, getContext().getString(R.string.cometchat_audio));
                break;
            default:
        }
    }

    /**
     * Sets the icon for a filter chip.
     *
     * @param iconView The ImageView representing the chip's icon.
     * @param iconRes  The resource ID of the icon to be displayed on the chip.
     */
    private void setChipIcon(ImageView iconView, @DrawableRes int iconRes) {
        if (iconRes != 0) {
            iconView.setImageResource(iconRes);
        }
    }

    /**
     * Sets the text for a filter chip.
     *
     * @param textView The TextView representing the chip's text.
     * @param text     The text to be displayed on the chip.
     */
    private void setChipText(TextView textView, String text) {
        textView.setText(text);
    }

    /**
     * Updates the appearance of a filter chip based on its selection state.
     *
     * @param chipCard     The MaterialCardView representing the chip.
     * @param chipIconView The ImageView representing the chip's icon.
     * @param chipTextView The TextView representing the chip's text.
     * @param isSelected   true if the chip is selected, false otherwise.
     */
    private void applyChipStyling(MaterialCardView chipCard, ImageView chipIconView, TextView chipTextView, boolean isSelected) {
        chipTextView.setTextColor(isSelected ? filterChipSelectedTextColor : filterChipTextColor);
        if (filterChipTextAppearance != 0) {
            chipTextView.setTextAppearance(filterChipTextAppearance);
        }

        chipIconView.setColorFilter(isSelected ? filterChipSelectedTextColor : filterChipTextColor);

        chipCard.setCardBackgroundColor(isSelected ? filterChipSelectedBackgroundColor : filterChipBackgroundColor);
        chipCard.setStrokeColor(isSelected ? filterChipSelectedStrokeColor : filterChipStrokeColor);
        chipCard.setStrokeWidth(filterChipStrokeWidth);
        chipCard.setRadius(filterChipCornerRadius);
    }

    /**
     * Handles the click event on a filter chip.
     *
     * @param selectedPosition The position of the clicked filter chip.
     */
    private void handleFilterClick(int selectedPosition) {
        UIKitConstants.SearchFilter clicked = searchFilters.get(selectedPosition);
        toggleFilter(clicked);
        refreshChipSelectionState();
        fetchBasedOnSelection();
    }

    /**
     * Checks if there are any active/selected filters
     * @return true if any filters are currently selected, false otherwise
     */
    private boolean hasActiveFilters() {
        return !selectedFilterChip.isEmpty();
    }

    /**
     * Checks if a specific filter is currently selected.
     *
     * @param filter The filter to check
     * @return true if the filter is selected, false otherwise
     */
    private boolean isFilterSelected(UIKitConstants.SearchFilter filter) {
        return filter != null && selectedFilterChip.contains(filter);
    }

    /**
     * Safely adds a filter to the selection without duplicates.
     *
     * @param filter The filter to add
     */
    private void addSelectedFilter(UIKitConstants.SearchFilter filter) {
        if (filter == null || selectedFilterChip.contains(filter)) {
            return;
        }
        selectedFilterChip.add(filter);
    }

    /**
     * Safely removes a filter from the selection.
     *
     * @param filter The filter to remove
     */
    private void removeSelectedFilter(UIKitConstants.SearchFilter filter) {
        if (filter != null) {
            selectedFilterChip.remove(filter);
        }
    }

    /**
     * Clears all selected filters.
     */
    private void clearSelectedFilters() {
        selectedFilterChip.clear();
    }

    /**
     * Gets a copy of the currently selected filters.
     *
     * @return A new list containing the selected filters
     */
    private List<UIKitConstants.SearchFilter> getSelectedFilters() {
        return new ArrayList<>(selectedFilterChip);
    }

    /**
     * Fetches search results based on the current search text and selected filters.
     * Implements a debounce mechanism to optimize search requests.
     */
    private void fetchBasedOnSelection() {
        String searchText = getCurrentSearchText().toString();
        List<UIKitConstants.SearchFilter> selectedFilters = getSelectedFilters();

        if (searchText.isEmpty() && !hasActiveFilters()) {
            handleInitialState();
            return;
        }
        cometChatSearchViewModel.searchConversationsAndMessages(searchText, selectedFilters);
    }

    /**
     * Fetches more search results based on the specified search mode.
     *
     * @param searchMode The mode of search, either CONVERSATIONS or MESSAGES.
     */
    private void fetchMore(UIKitConstants.SearchMode searchMode) {
        if (searchMode == UIKitConstants.SearchMode.CONVERSATIONS) {
            cometChatSearchViewModel.fetchConversations();
        } else if (searchMode == UIKitConstants.SearchMode.MESSAGES) {
            cometChatSearchViewModel.fetchMessages();
        }
    }

    /**
     * Checks if any filters from the specified group are selected.
     *
     * @param filters The group of filters to check.
     * @return true if any filter from the group is selected, false otherwise.
     */
    private boolean hasSelectedFiltersFromGroup(UIKitConstants.SearchFilter... filters) {
        for (UIKitConstants.SearchFilter filter : filters) {
            if (selectedFilterChip.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the current search text from the input field.
     *
     * @return The current search text as an Editable object.
     */
    private Editable getCurrentSearchText() {
        return binding.searchInput.getText();
    }

    /**
     * Toggles the selection state of a given search filter.
     *
     * @param filter The search filter to toggle.
     */
    private void toggleFilter(UIKitConstants.SearchFilter filter) {
        if (filter == UIKitConstants.SearchFilter.PHOTOS || filter == UIKitConstants.SearchFilter.VIDEOS) {
            UIKitConstants.SearchFilter[] mediaFilters = {UIKitConstants.SearchFilter.PHOTOS, UIKitConstants.SearchFilter.VIDEOS};
            handleGroupedFilterToggle(filter, isFilterSelected(filter), mediaFilters, this::hasSelectedMediaFilters);
            return;
        }

        if (filter == UIKitConstants.SearchFilter.DOCUMENTS || filter == UIKitConstants.SearchFilter.AUDIO) {
            UIKitConstants.SearchFilter[] documentFilters = {UIKitConstants.SearchFilter.DOCUMENTS, UIKitConstants.SearchFilter.AUDIO};
            handleGroupedFilterToggle(filter, isFilterSelected(filter), documentFilters, this::hasSelectedDocumentFilters);
            return;
        }

        if (filter == UIKitConstants.SearchFilter.UNREAD || filter == UIKitConstants.SearchFilter.GROUPS) {
            UIKitConstants.SearchFilter[] conversationFilters = {UIKitConstants.SearchFilter.UNREAD, UIKitConstants.SearchFilter.GROUPS};
            handleGroupedFilterToggle(filter, isFilterSelected(filter), conversationFilters, this::hasSelectedConversationFilters);
            return;
        }

        // Handle individual filters
        if (isFilterSelected(filter)) removeSelectedFilter(filter);
        else addSelectedFilter(filter);
    }

    /**
     * Handles toggle logic for grouped filters (like PHOTOS+VIDEOS, AUDIO+DOCUMENTS, etc.)
     *
     * @param filter The filter being toggled
     * @param isSelected Whether the filter is currently selected
     * @param groupFilters Array of filters that belong to the same group
     * @param hasGroupSelection Method reference to check if any filter in this group is selected
     */
    private void handleGroupedFilterToggle(UIKitConstants.SearchFilter filter, boolean isSelected,
                                          UIKitConstants.SearchFilter[] groupFilters,
                                          java.util.function.Supplier<Boolean> hasGroupSelection) {
        if (filter == null || groupFilters == null || hasGroupSelection == null) {
            return;
        }

        // Clear other groups if no filter from this group is currently selected
        if (!hasGroupSelection.get()) {
            clearSelectedFilters();
        }

        // Toggle the specific filter
        if (isSelected) {
            removeSelectedFilter(filter);
        } else {
            addSelectedFilter(filter);
        }

        // If any filter from this group is selected, remove filters from other groups
        if (hasGroupSelection.get()) {
            removeFiltersNotInGroup(groupFilters);
        }
    }

    /**
     * Removes all selected filters that are not in the specified group
     *
     * @param groupFilters Array of filters that should remain selected
     */
    private void removeFiltersNotInGroup(UIKitConstants.SearchFilter... groupFilters) {
        List<UIKitConstants.SearchFilter> groupList = Arrays.asList(groupFilters);

        for (UIKitConstants.SearchFilter f : getSelectedFilters()) {
            if (!groupList.contains(f)) {
                removeSelectedFilter(f);
            }
        }
    }

    /**
     * Refreshes the selection state of all filter chips based on the current active filters.
     * Updates the visibility and appearance of each chip accordingly.
     */
    private void refreshChipSelectionState() {
        boolean hasSelectedFilters = hasActiveFilters();

        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            UIKitConstants.SearchFilter currentFilter = searchFilters.get(i);
            boolean selected = isFilterSelected(currentFilter);
            View chipView = binding.chipGroup.getChildAt(i);

            if (hasSelectedFilters) {
                chipView.setVisibility(shouldShowFilterInCurrentSelection(currentFilter) ? View.VISIBLE : View.GONE);
            } else {
                chipView.setVisibility(View.VISIBLE);
            }

            updateChipAppearance(i, selected);
            chipView.setTag(selected);
        }
    }

    /**
     * Determines if a filter should be visible based on the current selection group
     */
    private boolean shouldShowFilterInCurrentSelection(UIKitConstants.SearchFilter filter) {
        // If the filter is already selected, it should be visible
        if (isFilterSelected(filter)) {
            return true;
        }

        // Check if any filter from the same group is selected
        if (isConversationFilter(filter)) {
            return hasSelectedConversationFilters();
        } else if (isMediaFilter(filter)) {
            return hasSelectedMediaFilters();
        } else if (isDocumentFilter(filter)) {
            return hasSelectedDocumentFilters();
        } else if (filter == UIKitConstants.SearchFilter.LINKS) {
            return hasSelectedLinkFilter();
        }

        return false;
    }

    /** Checks if any message filters are selected */
    private boolean hasSelectedMessageFilters() {
        return hasSelectedFiltersFromGroup(MESSAGE_FILTERS);
    }

    /** Checks if any conversation filters are selected */
    private boolean hasSelectedConversationFilters() {
        return hasSelectedFiltersFromGroup(CONVERSATION_FILTERS);
    }

    /** Checks if the filter is a conversation filter */
    private boolean isConversationFilter(UIKitConstants.SearchFilter filter) {
        return filter == UIKitConstants.SearchFilter.UNREAD || filter == UIKitConstants.SearchFilter.GROUPS;
    }

    /** Checks if the filter is a media filter */
    private boolean isMediaFilter(UIKitConstants.SearchFilter filter) {
        return filter == UIKitConstants.SearchFilter.PHOTOS || filter == UIKitConstants.SearchFilter.VIDEOS;
    }

    /** Checks if the filter is a document filter */
    private boolean isDocumentFilter(UIKitConstants.SearchFilter filter) {
        return filter == UIKitConstants.SearchFilter.DOCUMENTS || filter == UIKitConstants.SearchFilter.AUDIO;
    }

    /** Checks if any media filters are selected */
    private boolean hasSelectedMediaFilters() {
        return hasSelectedFiltersFromGroup(UIKitConstants.SearchFilter.PHOTOS, UIKitConstants.SearchFilter.VIDEOS);
    }

    /** Checks if any document filters are selected */
    private boolean hasSelectedDocumentFilters() {
        return hasSelectedFiltersFromGroup(UIKitConstants.SearchFilter.AUDIO, UIKitConstants.SearchFilter.DOCUMENTS);
    }

    /** Checks if any link filters are selected */
    private boolean hasSelectedLinkFilter() {
        return hasSelectedFiltersFromGroup(UIKitConstants.SearchFilter.LINKS);
    }

    /**
     * Updates the appearance of a filter chip at the specified position based on its selection state.
     *
     * @param position   The position of the chip in the chip group.
     * @param isSelected true if the chip is selected, false otherwise.
     */
    private void updateChipAppearance(int position, boolean isSelected) {
        View child = binding.chipGroup.getChildAt(position);
        MaterialCardView card = (MaterialCardView) child;
        View container = card.getChildAt(0);
        LinearLayout ll = (LinearLayout) container;
        ImageView icon = (ImageView) ll.getChildAt(0);
        TextView text = (TextView) ll.getChildAt(1);
        applyChipStyling(card, icon, text, isSelected);
    }

    /** Gets the background color of the search component */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /** Sets the background color of the search component
     * @param backgroundColor The desired background color as an integer.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        binding.parentLayout.setBackgroundColor(backgroundColor);
    }

    /** Gets the background color of the search bar */
    public int getSearchBarBackgroundColor() {
        return searchBarBackgroundColor;
    }

    /** Sets the background color of the search bar
     * @param searchBarBackgroundColor The desired background color of the search bar as an integer.
     */
    public void setSearchBarBackgroundColor(@ColorInt int searchBarBackgroundColor) {
        this.searchBarBackgroundColor = searchBarBackgroundColor;
        binding.searchBox.setBackgroundColor(searchBarBackgroundColor);
    }

    /** Gets the stroke width of the search bar */
    public int getSearchBarStrokeWidth() {
        return searchBarStrokeWidth;
    }

    /** Sets the stroke width of the search bar
     * @param searchBarStrokeWidth The desired stroke width of the search bar as an integer.
     */
    public void setSearchBarStrokeWidth(@Dimension int searchBarStrokeWidth) {
        this.searchBarStrokeWidth = searchBarStrokeWidth;
         binding.searchBoxCard.setStrokeWidth(searchBarStrokeWidth);
    }

    /** Gets the stroke color of the search bar */
    public int getSearchBarStrokeColor() {
        return searchBarStrokeColor;
    }

    /** Sets the stroke color of the search bar
     * @param searchBarStrokeColor The desired stroke color of the search bar as an integer.
     */
    public void setSearchBarStrokeColor(@ColorInt int searchBarStrokeColor) {
        this.searchBarStrokeColor = searchBarStrokeColor;
        binding.searchBoxCard.setStrokeColor(searchBarStrokeColor);
    }

    /** Gets the corner radius of the search bar */
    public int getSearchBarCornerRadius() {
        return searchBarCornerRadius;
    }

    /** Sets the corner radius of the search bar
     * @param searchBarCornerRadius The desired corner radius of the search bar as an integer.
     */
    public void setSearchBarCornerRadius(@Dimension int searchBarCornerRadius) {
        this.searchBarCornerRadius = searchBarCornerRadius;
        binding.searchBoxCard.setRadius(searchBarCornerRadius);
    }

    /** Gets the text color of the search bar */
    public int getSearchBarTextColor() {
        return searchBarTextColor;
    }

    /** Sets the text color of the search bar
     * @param searchBarTextColor The desired text color of the search bar as an integer.
     */
    public void setSearchBarTextColor(@ColorInt int searchBarTextColor) {
        this.searchBarTextColor = searchBarTextColor;
        binding.searchInput.setTextColor(searchBarTextColor);
    }

    /** Gets the text appearance of the search bar */
    public int getSearchBarTextAppearance() {
        return searchBarTextAppearance;
    }

    /** Sets the text appearance of the search bar
     * @param searchBarTextAppearance The desired text appearance style resource for the search bar.
     */
    public void setSearchBarTextAppearance(@StyleRes int searchBarTextAppearance) {
        this.searchBarTextAppearance = searchBarTextAppearance;
        binding.searchInput.setTextAppearance(searchBarTextAppearance);
    }

    /** Gets the hint text color of the search bar */
    public int getSearchBarHintTextColor() {
        return searchBarHintTextColor;
    }

    /** Sets the hint text color of the search bar
     * @param searchBarHintTextColor The desired hint text color of the search bar as an integer.
     */
    public void setSearchBarHintTextColor(@ColorInt int searchBarHintTextColor) {
        this.searchBarHintTextColor = searchBarHintTextColor;
        binding.searchInput.setHintTextColor(searchBarHintTextColor);
    }

    /** Gets the back icon drawable */
    public Drawable getBackIcon() {
        return backIcon;
    }

    /** Sets the back icon drawable
     * @param backIcon The Drawable to be used as the back icon.
     */
    public void setBackIcon(Drawable backIcon) {
        this.backIcon = backIcon;
        binding.ivBack.setImageDrawable(backIcon);
    }

    /** Gets the back icon tint color */
    public int getBackIconTint() {
        return backIconTint;
    }

    /** Sets the back icon tint color
     * @param backIconTint The desired tint color for the back icon as an integer.
     */
    public void setBackIconTint(@ColorInt int backIconTint) {
        this.backIconTint = backIconTint;
        binding.ivBack.setImageTintList(ColorStateList.valueOf(backIconTint));
    }

    /** Gets the clear icon drawable */
    public Drawable getClearIcon() {
        return clearIcon;
    }

    /** Sets the clear icon drawable
     * @param clearIcon The Drawable to be used as the clear icon.
     */
    public void setClearIcon(Drawable clearIcon) {
        this.clearIcon = clearIcon;
        binding.ivClear.setImageDrawable(clearIcon);
    }

    /** Gets the clear icon tint color */
    public int getClearIconTint() {
        return clearIconTint;
    }

    /** Sets the clear icon tint color
     * @param clearIconTint The desired tint color for the clear icon as an integer.
     */
    public void setClearIconTint(@ColorInt int clearIconTint) {
        this.clearIconTint = clearIconTint;
        binding.ivClear.setImageTintList(ColorStateList.valueOf(clearIconTint));
    }

    /** Gets the search icon drawable */
    public Drawable getSearchIcon() {
        return searchIcon;
    }

    /** Sets the search icon drawable
     * @param searchIcon The Drawable to be used as the search icon.
     */
    public void setSearchIcon(Drawable searchIcon) {
        this.searchIcon = searchIcon;
//         binding.ivSearch.setImageDrawable(searchIcon);
    }

    /** Gets the search icon tint color */
    public int getSearchIconTint() {
        return searchIconTint;
    }

    /** Sets the search icon tint color
     * @param searchIconTint The desired tint color for the search icon as an integer.
     */
    public void setSearchIconTint(@ColorInt int searchIconTint) {
        this.searchIconTint = searchIconTint;
        // binding.ivSearch.setImageTintList(ColorStateList.valueOf(searchIconTint));
    }

    /** Gets the filter chip background color */
    public int getFilterChipBackgroundColor() {
        return filterChipBackgroundColor;
    }

    /** Sets the filter chip background color
     * @param filterChipBackgroundColor The desired background color for filter chips as an integer.
     */
    public void setFilterChipBackgroundColor(@ColorInt int filterChipBackgroundColor) {
        this.filterChipBackgroundColor = filterChipBackgroundColor;
    }

    /** Gets the filter chip selected background color */
    public int getFilterChipSelectedBackgroundColor() {
        return filterChipSelectedBackgroundColor;
    }

    /** Sets the filter chip selected background color
     * @param filterChipSelectedBackgroundColor The desired selected background color for filter chips as an integer.
     */
    public void setFilterChipSelectedBackgroundColor(@ColorInt int filterChipSelectedBackgroundColor) {
        this.filterChipSelectedBackgroundColor = filterChipSelectedBackgroundColor;
    }

    /** Gets the filter chip text color */
    public int getFilterChipTextColor() {
        return filterChipTextColor;
    }

    /** Sets the filter chip text color
     * @param filterChipTextColor The desired text color for filter chips as an integer.
     */
    public void setFilterChipTextColor(@ColorInt int filterChipTextColor) {
        this.filterChipTextColor = filterChipTextColor;
    }

    /** Gets the filter chip selected text color */
    public int getFilterChipSelectedTextColor() {
        return filterChipSelectedTextColor;
    }

    /** Sets the filter chip selected text color
     * @param filterChipSelectedTextColor The desired selected text color for filter chips as an integer.
     */
    public void setFilterChipSelectedTextColor(@ColorInt int filterChipSelectedTextColor) {
        this.filterChipSelectedTextColor = filterChipSelectedTextColor;
    }

    /** Gets the filter chip text appearance style resource */
    public int getFilterChipTextAppearance() {
        return filterChipTextAppearance;
    }

    /** Sets the filter chip text appearance style resource
     * @param filterChipTextAppearance The desired text appearance style resource for filter chips.
     */
    public void setFilterChipTextAppearance(@StyleRes int filterChipTextAppearance) {
        this.filterChipTextAppearance = filterChipTextAppearance;
    }

    /** Gets the filter chip stroke color */
    public int getFilterChipStrokeColor() {
        return filterChipStrokeColor;
    }

    /** Sets the filter chip stroke color
     * @param filterChipStrokeColor The desired stroke color for filter chips as an integer.
     */
    public void setFilterChipStrokeColor(@ColorInt int filterChipStrokeColor) {
        this.filterChipStrokeColor = filterChipStrokeColor;
    }

    /** Gets the filter chip selected stroke color */
    public int getFilterChipSelectedStrokeColor() {
        return filterChipSelectedStrokeColor;
    }

    /** Sets the filter chip selected stroke color
     * @param filterChipSelectedStrokeColor The desired selected stroke color for filter chips as an integer.
     */
    public void setFilterChipSelectedStrokeColor(@ColorInt int filterChipSelectedStrokeColor) {
        this.filterChipSelectedStrokeColor = filterChipSelectedStrokeColor;
    }

    /** Gets the filter chip stroke width */
    public int getFilterChipStrokeWidth() {
        return filterChipStrokeWidth;
    }

    /** Sets the filter chip stroke width
     * @param filterChipStrokeWidth The desired stroke width for filter chips as an integer.
     */
    public void setFilterChipStrokeWidth(@Dimension int filterChipStrokeWidth) {
        this.filterChipStrokeWidth = filterChipStrokeWidth;
    }

    /** Gets the filter chip corner radius */
    public int getFilterChipCornerRadius() {
        return filterChipCornerRadius;
    }

    /** Sets the filter chip corner radius
     * @param filterChipCornerRadius The desired corner radius for filter chips as an integer.
     */
    public void setFilterChipCornerRadius(@Dimension int filterChipCornerRadius) {
        this.filterChipCornerRadius = filterChipCornerRadius;
    }

    /** Gets the section header text color */
    public int getSectionHeaderTextColor() {
        return sectionHeaderTextColor;
    }

    /** Sets the section header text color
     * @param sectionHeaderTextColor The desired text color for section headers as an integer.
     */
    public void setSectionHeaderTextColor(@ColorInt int sectionHeaderTextColor) {
        this.sectionHeaderTextColor = sectionHeaderTextColor;
        binding.messagesSectionHeader.setTextColor(sectionHeaderTextColor);
        binding.conversationsSectionHeader.setTextColor(sectionHeaderTextColor);
    }

    /** Gets the section header text appearance style resource */
    public int getSectionHeaderTextAppearance() {
        return sectionHeaderTextAppearance;
    }

    /** Sets the section header text appearance style resource
     * @param sectionHeaderTextAppearance The desired text appearance style resource for section headers.
     */
    public void setSectionHeaderTextAppearance(@StyleRes int sectionHeaderTextAppearance) {
        this.sectionHeaderTextAppearance = sectionHeaderTextAppearance;
        binding.messagesSectionHeader.setTextAppearance(sectionHeaderTextAppearance);
        binding.conversationsSectionHeader.setTextAppearance(sectionHeaderTextAppearance);
    }

    /** Gets the section header background color */
    public int getSectionHeaderBackgroundColor() {
        return sectionHeaderBackgroundColor;
    }

    /** Sets the section header background color
     * @param sectionHeaderBackgroundColor The desired background color for section headers as an integer.
     */
    public void setSectionHeaderBackgroundColor(@ColorInt int sectionHeaderBackgroundColor) {
        this.sectionHeaderBackgroundColor = sectionHeaderBackgroundColor;
        binding.messagesSectionHeader.setBackgroundColor(sectionHeaderBackgroundColor);
        binding.conversationsSectionHeader.setBackgroundColor(sectionHeaderBackgroundColor);
    }

    /** Gets the conversation item background color */
    public int getConversationItemBackgroundColor() {
        return conversationItemBackgroundColor;
    }

    /** Sets the conversation item background color
     * @param conversationItemBackgroundColor The desired background color for conversation items as an integer.
     */
    public void setConversationItemBackgroundColor(@ColorInt int conversationItemBackgroundColor) {
        this.conversationItemBackgroundColor = conversationItemBackgroundColor;
        cometChatSearchConversationsAdapter.setConversationItemBackgroundColor(conversationItemBackgroundColor);
    }

    /** Gets the conversation title text color */
    public int getConversationTitleTextColor() {
        return conversationTitleTextColor;
    }

    /** Sets the conversation title text color
     * @param conversationTitleTextColor The desired text color for conversation titles as an integer.
     */
    public void setConversationTitleTextColor(@ColorInt int conversationTitleTextColor) {
        this.conversationTitleTextColor = conversationTitleTextColor;
        cometChatSearchConversationsAdapter.setConversationTitleTextColor(conversationTitleTextColor);
    }

    /** Gets the conversation title text appearance style resource */
    public int getConversationTitleTextAppearance() {
        return conversationTitleTextAppearance;
    }

    /** Sets the conversation title text appearance style resource
     * @param conversationTitleTextAppearance The desired text appearance style resource for conversation titles.
     */
    public void setConversationTitleTextAppearance(@StyleRes int conversationTitleTextAppearance) {
        this.conversationTitleTextAppearance = conversationTitleTextAppearance;
        cometChatSearchConversationsAdapter.setConversationTitleTextAppearance(conversationTitleTextAppearance);
    }

    /** Gets the conversation subtitle text color */
    public int getConversationSubtitleTextColor() {
        return conversationSubtitleTextColor;
    }

    /** Sets the conversation subtitle text color
     * @param conversationSubtitleTextColor The desired text color for conversation subtitles as an integer.
     */
    public void setConversationSubtitleTextColor(@ColorInt int conversationSubtitleTextColor) {
        this.conversationSubtitleTextColor = conversationSubtitleTextColor;
        cometChatSearchConversationsAdapter.setConversationSubtitleTextColor(conversationSubtitleTextColor);
    }

    /** Gets the conversation subtitle text appearance style resource */
    public int getConversationSubtitleTextAppearance() {
        return conversationSubtitleTextAppearance;
    }

    /** Sets the conversation subtitle text appearance style resource
     * @param conversationSubtitleTextAppearance The desired text appearance style resource for conversation subtitles.
     */
    public void setConversationSubtitleTextAppearance(@StyleRes int conversationSubtitleTextAppearance) {
        this.conversationSubtitleTextAppearance = conversationSubtitleTextAppearance;
        cometChatSearchConversationsAdapter.setConversationSubtitleTextAppearance(conversationSubtitleTextAppearance);
    }

    /** Gets the conversation timestamp text color */
    public int getConversationTimestampTextColor() {
        return conversationTimestampTextColor;
    }

    /** Sets the conversation timestamp text color
     * @param conversationTimestampTextColor The desired text color for conversation timestamps as an integer.
     */
    public void setConversationTimestampTextColor(@ColorInt int conversationTimestampTextColor) {
        this.conversationTimestampTextColor = conversationTimestampTextColor;
        cometChatSearchConversationsAdapter.setConversationTimestampTextColor(conversationTimestampTextColor);
    }

    /** Gets the conversation timestamp text appearance style resource */
    public int getConversationTimestampTextAppearance() {
        return conversationTimestampTextAppearance;
    }

    /** Sets the conversation timestamp text appearance style resource
     * @param conversationTimestampTextAppearance The desired text appearance style resource for conversation timestamps.
     */
    public void setConversationTimestampTextAppearance(@StyleRes int conversationTimestampTextAppearance) {
        this.conversationTimestampTextAppearance = conversationTimestampTextAppearance;
        cometChatSearchConversationsAdapter.setConversationTimestampTextAppearance(conversationTimestampTextAppearance);
    }

    /** Gets the message item background color */
    public int getMessageItemBackgroundColor() {
        return messageItemBackgroundColor;
    }

    /** Sets the message item background color
     * @param messageItemBackgroundColor The desired background color for message items as an integer.
     */
    public void setMessageItemBackgroundColor(@ColorInt int messageItemBackgroundColor) {
        this.messageItemBackgroundColor = messageItemBackgroundColor;
        cometChatSearchMessageAdapter.setItemBackgroundColor(messageItemBackgroundColor);
    }

    /** Gets the message title text color */
    public int getMessageTitleTextColor() {
        return messageTitleTextColor;
    }

    /** Sets the message title text color
     * @param messageTitleTextColor The desired text color for message titles as an integer.
     */
    public void setMessageTitleTextColor(@ColorInt int messageTitleTextColor) {
        this.messageTitleTextColor = messageTitleTextColor;
        cometChatSearchMessageAdapter.setMessageTitleTextColor(messageTitleTextColor);
    }

    /** Gets the message title text appearance style resource */
    public int getMessageTitleTextAppearance() {
        return messageTitleTextAppearance;
    }

    /** Sets the message title text appearance style resource
     * @param messageTitleTextAppearance The desired text appearance style resource for message titles.
     */
    public void setMessageTitleTextAppearance(@StyleRes int messageTitleTextAppearance) {
        this.messageTitleTextAppearance = messageTitleTextAppearance;
        cometChatSearchMessageAdapter.setMessageTitleTextAppearance(messageTitleTextAppearance);
    }

    /** Gets the message subtitle text color */
    public int getMessageSubtitleTextColor() {
        return messageSubtitleTextColor;
    }

    /** Sets the message subtitle text color
     * @param messageSubtitleTextColor The desired text color for message subtitles as an integer.
     */
    public void setMessageSubtitleTextColor(@ColorInt int messageSubtitleTextColor) {
        this.messageSubtitleTextColor = messageSubtitleTextColor;
        cometChatSearchMessageAdapter.setMessageSubtitleTextColor(messageSubtitleTextColor);
    }

    /** Gets the message subtitle text appearance style resource */
    public int getMessageSubtitleTextAppearance() {
        return messageSubtitleTextAppearance;
    }

    /** Sets the message subtitle text appearance style resource
     * @param messageSubtitleTextAppearance The desired text appearance style resource for message subtitles.
     */
    public void setMessageSubtitleTextAppearance(@StyleRes int messageSubtitleTextAppearance) {
        this.messageSubtitleTextAppearance = messageSubtitleTextAppearance;
        cometChatSearchMessageAdapter.setMessageSubtitleTextAppearance(messageSubtitleTextAppearance);
    }

    /** Gets the message timestamp text color */
    public int getMessageTimestampTextColor() {
        return messageTimestampTextColor;
    }

    /** Sets the message timestamp text color
     * @param messageTimestampTextColor The desired text color for message timestamps as an integer.
     */
    public void setMessageTimestampTextColor(@ColorInt int messageTimestampTextColor) {
        this.messageTimestampTextColor = messageTimestampTextColor;
        cometChatSearchMessageAdapter.setMessageTimestampTextColor(messageTimestampTextColor);
    }

    /** Gets the message timestamp text appearance style resource */
    public int getMessageTimestampTextAppearance() {
        return messageTimestampTextAppearance;
    }

    /** Sets the message timestamp text appearance style resource
     * @param messageTimestampTextAppearance The desired text appearance style resource for message timestamps.
     */
    public void setMessageTimestampTextAppearance(@StyleRes int messageTimestampTextAppearance) {
        this.messageTimestampTextAppearance = messageTimestampTextAppearance;
        cometChatSearchMessageAdapter.setMessageTimestampTextAppearance(messageTimestampTextAppearance);
    }

    /**
     * Returns the text color for message links.
     *
     * @return The color integer representing the text color for message links.
     */
    public @ColorInt int getMessageLinkTextColor() {
        return messageLinkTextColor;
    }

    /**
     * Sets the text color for message links.
     *
     * @param messageLinkTextColor The color integer to set as the text color for message links.
     */
    public void setMessageLinkTextColor(@ColorInt int messageLinkTextColor) {
        this.messageLinkTextColor = messageLinkTextColor;
        cometChatSearchMessageAdapter.setMessageLinkTextColor(messageLinkTextColor);
    }

    /**
     * Returns the text appearance resource ID for message links.
     *
     * @return The style resource ID representing the text appearance for message links.
     */
    public @StyleRes int getMessageLinkTextAppearance() {
        return messageLinkTextAppearance;
    }

    /**
     * Sets the text appearance for message links.
     *
     * @param messageLinkTextAppearance The style resource ID to set as the text appearance for message links.
     */
    public void setMessageLinkTextAppearance(@StyleRes int messageLinkTextAppearance) {
        this.messageLinkTextAppearance = messageLinkTextAppearance;
        cometChatSearchMessageAdapter.setMessageLinkTextAppearance(messageLinkTextAppearance);
    }

    /**
     * Returns the avatar style resource ID.
     *
     * @return The style resource ID representing the avatar style.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the avatar style resource ID.
     *
     * @param avatarStyle The style resource ID to set as the avatar style.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        cometChatSearchConversationsAdapter.setAvatarStyle(avatarStyle);
    }

    /**
     * Returns the badge style resource ID.
     *
     * @return The style resource ID representing the badge style.
     */
    public @StyleRes int getBadgeStyle() {
        return badgeStyle;
    }

    /**
     * Sets the badge style resource ID.
     *
     * @param badgeStyle The style resource ID to set as the badge style.
     */
    public void setBadgeStyle(@StyleRes int badgeStyle) {
        this.badgeStyle = badgeStyle;
        cometChatSearchConversationsAdapter.setBadgeStyle(badgeStyle);
    }

    /**
     * Returns the text color for initial state title.
     *
     * @return The color integer representing the text color for initial state title.
     */
    private @ColorInt int getInitialStateTextColor() {
        return initialStateTextColor;
    }

    /**
     * Sets the text color for initial state title.
     *
     * @param initialStateTextColor The color integer to set as the text color for initial state title.
     */
    private void setInitialStateTextColor(@ColorInt int initialStateTextColor) {
        this.initialStateTextColor = initialStateTextColor;
        binding.tvInitialStateTitle.setTextColor(initialStateTextColor);
    }

    /**
     * Returns the text appearance resource ID for initial state title.
     *
     * @return The style resource ID representing the text appearance for initial state title.
     */
    private @StyleRes int getInitialStateTextAppearance() {
        return initialStateTextAppearance;
    }

    /**
     * Sets the text appearance for initial state title.
     *
     * @param initialStateTextAppearance The style resource ID to set as the text appearance for initial state title.
     */
    private void setInitialStateTextAppearance(@StyleRes int initialStateTextAppearance) {
        this.initialStateTextAppearance = initialStateTextAppearance;
        binding.tvInitialStateTitle.setTextAppearance(initialStateTextAppearance);
    }

    /**
     * Returns the text color for initial state subtitle.
     *
     * @return The color integer representing the text color for initial state subtitle.
     */
    private @ColorInt int getInitialStateSubtitleTextColor() {
        return initialStateSubtitleTextColor;
    }

    /**
     * Sets the text color for initial state subtitle.
     *
     * @param initialStateSubtitleTextColor The color integer to set as the text color for initial state subtitle.
     */
    private void setInitialStateSubtitleTextColor(@ColorInt int initialStateSubtitleTextColor) {
        this.initialStateSubtitleTextColor = initialStateSubtitleTextColor;
        binding.tvInitialStateSubtitle.setTextColor(initialStateSubtitleTextColor);
    }

    /**
     * Returns the drawable for initial state icon.
     *
     * @return The drawable representing the initial state icon.
     */
    private Drawable getInitialStateIcon() {
        return initialStateIcon;
    }

    /**
     * Sets the drawable for initial state icon.
     *
     * @param initialStateIcon The drawable to set as the initial state icon.
     */
    private void setInitialStateIcon(Drawable initialStateIcon) {
        this.initialStateIcon = initialStateIcon;
        binding.ivInitialState.setImageDrawable(initialStateIcon);
    }

    /**
     * Returns the tint color for initial state icon.
     *
     * @return The color integer representing the tint color for initial state icon.
     */
    private @ColorInt int getInitialStateIconTint() {
        return initialStateIconTint;
    }

    /**
     * Sets the tint color for initial state icon.
     *
     * @param initialStateIconTint The color integer to set as the tint color for initial state icon.
     */
    private void setInitialStateIconTint(@ColorInt int initialStateIconTint) {
        this.initialStateIconTint = initialStateIconTint;
        binding.ivInitialState.setImageTintList(ColorStateList.valueOf(initialStateIconTint));
    }

    /**
     * Returns the text appearance resource ID for initial state subtitle.
     *
     * @return The style resource ID representing the text appearance for initial state subtitle.
     */
    private @StyleRes int getInitialStateSubtitleTextAppearance() {
        return initialStateSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance for initial state subtitle.
     *
     * @param initialStateSubtitleTextAppearance The style resource ID to set as the text appearance for initial state subtitle.
     */
    private void setInitialStateSubtitleTextAppearance(@StyleRes int initialStateSubtitleTextAppearance) {
        this.initialStateSubtitleTextAppearance = initialStateSubtitleTextAppearance;
        binding.tvInitialStateSubtitle.setTextAppearance(initialStateSubtitleTextAppearance);
    }

    /**
     * Returns the text color for empty state title.
     *
     * @return The color integer representing the text color for empty state title.
     */
    public @ColorInt int getEmptyStateTextColor() {
        return emptyStateTextColor;
    }

    /**
     * Sets the text color for empty state title.
     *
     * @param emptyStateTextColor The color integer to set as the text color for empty state title.
     */
    public void setEmptyStateTextColor(@ColorInt int emptyStateTextColor) {
        this.emptyStateTextColor = emptyStateTextColor;
        binding.tvEmptyStateTitle.setTextColor(emptyStateTextColor);
    }

    /**
     * Returns the text appearance resource ID for empty state title.
     *
     * @return The style resource ID representing the text appearance for empty state title.
     */
    public @StyleRes int getEmptyStateTextAppearance() {
        return emptyStateTextAppearance;
    }

    /**
     * Sets the text appearance for empty state title.
     *
     * @param emptyStateTextAppearance The style resource ID to set as the text appearance for empty state title.
     */
    public void setEmptyStateTextAppearance(@StyleRes int emptyStateTextAppearance) {
        this.emptyStateTextAppearance = emptyStateTextAppearance;
        binding.tvEmptyStateTitle.setTextAppearance(emptyStateTextAppearance);
    }

    /**
     * Returns the text color for empty state subtitle.
     *
     * @return The color integer representing the text color for empty state subtitle.
     */
    public @ColorInt int getEmptyStateSubtitleTextColor() {
        return emptyStateSubtitleTextColor;
    }

    /**
     * Sets the text color for empty state subtitle.
     *
     * @param emptyStateSubtitleTextColor The color integer to set as the text color for empty state subtitle.
     */
    public void setEmptyStateSubtitleTextColor(@ColorInt int emptyStateSubtitleTextColor) {
        this.emptyStateSubtitleTextColor = emptyStateSubtitleTextColor;
        binding.tvEmptyStateSubtitle.setTextColor(emptyStateSubtitleTextColor);
    }

    /**
     * Returns the text appearance resource ID for empty state subtitle.
     *
     * @return The style resource ID representing the text appearance for empty state subtitle.
     */
    public @StyleRes int getEmptyStateSubtitleTextAppearance() {
        return emptyStateSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance for empty state subtitle.
     *
     * @param emptyStateSubtitleTextAppearance The style resource ID to set as the text appearance for empty state subtitle.
     */
    public void setEmptyStateSubtitleTextAppearance(@StyleRes int emptyStateSubtitleTextAppearance) {
        this.emptyStateSubtitleTextAppearance = emptyStateSubtitleTextAppearance;
        binding.tvEmptyStateSubtitle.setTextAppearance(emptyStateSubtitleTextAppearance);
    }

    /**
     * Returns the drawable for empty state icon.
     *
     * @return The drawable representing the empty state icon.
     */
    public Drawable getEmptyStateIcon() {
        return emptyStateIcon;
    }

    /**
     * Sets the drawable for empty state icon.
     *
     * @param emptyStateIcon The drawable to set as the empty state icon.
     */
    public void setEmptyStateIcon(Drawable emptyStateIcon) {
        this.emptyStateIcon = emptyStateIcon;
        binding.ivEmptyState.setImageDrawable(emptyStateIcon);
    }

    /**
     * Returns the tint color for empty state icon.
     *
     * @return The color integer representing the tint color for empty state icon.
     */
    public @ColorInt int getEmptyStateIconTint() {
        return emptyStateIconTint;
    }

    /**
     * Sets the tint color for empty state icon.
     *
     * @param emptyStateIconTint The color integer to set as the tint color for empty state icon.
     */
    public void setEmptyStateIconTint(@ColorInt int emptyStateIconTint) {
        this.emptyStateIconTint = emptyStateIconTint;
    }

    /**
     * Returns the text color for error state title.
     *
     * @return The color integer representing the text color for error state title.
     */
    public @ColorInt int getErrorStateTextColor() {
        return errorStateTextColor;
    }

    /**
     * Sets the text color for error state title.
     *
     * @param errorStateTextColor The color integer to set as the text color for error state title.
     */
    public void setErrorStateTextColor(@ColorInt int errorStateTextColor) {
        this.errorStateTextColor = errorStateTextColor;
    }

    /**
     * Returns the text appearance resource ID for error state title.
     *
     * @return The style resource ID representing the text appearance for error state title.
     */
    public @StyleRes int getErrorStateTextAppearance() {
        return errorStateTextAppearance;
    }

    /**
     * Sets the text appearance for error state title.
     *
     * @param errorStateTextAppearance The style resource ID to set as the text appearance for error state title.
     */
    public void setErrorStateTextAppearance(@StyleRes int errorStateTextAppearance) {
        this.errorStateTextAppearance = errorStateTextAppearance;
    }

    /**
     * Returns the text color for error state subtitle.
     *
     * @return The color integer representing the text color for error state subtitle.
     */
    public @ColorInt int getErrorStateSubtitleTextColor() {
        return errorStateSubtitleTextColor;
    }

    /**
     * Sets the text color for error state subtitle.
     *
     * @param errorStateSubtitleTextColor The color integer to set as the text color for error state subtitle.
     */
    public void setErrorStateSubtitleTextColor(@ColorInt int errorStateSubtitleTextColor) {
        this.errorStateSubtitleTextColor = errorStateSubtitleTextColor;
    }

    /**
     * Returns the text appearance resource ID for error state subtitle.
     *
     * @return The style resource ID representing the text appearance for error state subtitle.
     */
    public @StyleRes int getErrorStateSubtitleTextAppearance() {
        return errorStateSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance for error state subtitle.
     *
     * @param errorStateSubtitleTextAppearance The style resource ID to set as the text appearance for error state subtitle.
     */
    public void setErrorStateSubtitleTextAppearance(@StyleRes int errorStateSubtitleTextAppearance) {
        this.errorStateSubtitleTextAppearance = errorStateSubtitleTextAppearance;
    }

    /**
     * Returns the drawable for error state icon.
     *
     * @return The drawable representing the error state icon.
     */
    public Drawable getErrorStateIcon() {
        return errorStateIcon;
    }

    /**
     * Sets the drawable for error state icon.
     *
     * @param errorStateIcon The drawable to set as the error state icon.
     */
    public void setErrorStateIcon(Drawable errorStateIcon) {
        this.errorStateIcon = errorStateIcon;
    }

    /**
     * Returns the tint color for error state icon.
     *
     * @return The color integer representing the tint color for error state icon.
     */
    public @ColorInt int getErrorStateIconTint() {
        return errorStateIconTint;
    }

    /**
     * Sets the tint color for error state icon.
     *
     * @param errorStateIconTint The color integer to set as the tint color for error state icon.
     */
    public void setErrorStateIconTint(@ColorInt int errorStateIconTint) {
        this.errorStateIconTint = errorStateIconTint;
    }

    /*** Returns the text color for the "See More" option.
     *
     * @return The color integer representing the text color for the "See More" option.
     */
    public @ColorInt int getSeeMoreTextColor() {
        return seeMoreTextColor;
    }

    /*** Sets the text color for the "See More" option.
     *
     * @param seeMoreTextColor The color integer to set as the text color for the "See More" option.
     */
    public void setSeeMoreTextColor(@ColorInt int seeMoreTextColor) {
        this.seeMoreTextColor = seeMoreTextColor;
        binding.messagesSeeMore.setTextColor(seeMoreTextColor);
        binding.conversationsSeeMore.setTextColor(seeMoreTextColor);
    }

    /*** Sets the text appearance for the "See More" option.
     *
     * @param seeMoreTextAppearance The style resource ID to set as the text appearance for the "See More" option.
     */
    public void setSeeMoreTextAppearance(@StyleRes int seeMoreTextAppearance) {
        this.seeMoreTextAppearance = seeMoreTextAppearance;
        binding.messagesSeeMore.setTextAppearance(seeMoreTextAppearance);
        binding.conversationsSeeMore.setTextAppearance(seeMoreTextAppearance);
    }

    /*** Returns the text color for date separators.
     *
     * @return The color integer representing the text color for date separators.
     */
    public @ColorInt int getDateSeparatorTextColor() {
        return dateSeparatorTextColor;
    }

    /*** Sets the text color for date separators.
     *
     * @param dateSeparatorTextColor The color integer to set as the text color for date separators.
     */
    public void setDateSeparatorTextColor(@ColorInt int dateSeparatorTextColor) {
        this.dateSeparatorTextColor = dateSeparatorTextColor;
        cometChatSearchMessageAdapter.setDateSeparatorTextColor(dateSeparatorTextColor);
    }

    /*** Returns the background color for date separators.
     *
     * @return The color integer representing the background color for date separators.
     */
    public @ColorInt int getDateSeparatorBackgroundColor() {
        return dateSeparatorBackgroundColor;
    }

    /*** Sets the background color for date separators.
     *
     * @param dateSeparatorBackgroundColor The color integer to set as the background color for date separators.
     */
    public void setDateSeparatorBackgroundColor(@ColorInt int dateSeparatorBackgroundColor) {
        this.dateSeparatorBackgroundColor = dateSeparatorBackgroundColor;
        cometChatSearchMessageAdapter.setDateSeparatorBackgroundColor(dateSeparatorBackgroundColor);
    }

    /*** Returns the text appearance resource ID for date separators.
     *
     * @return The style resource ID representing the text appearance for date separators.
     */
    public @StyleRes int getDateSeparatorTextAppearance() {
        return dateSeparatorTextAppearance;
    }

    /*** Sets the text appearance for date separators.
     *
     * @param dateSeparatorTextAppearance The style resource ID to set as the text appearance for date separators.
     */
    public void setDateSeparatorTextAppearance(@StyleRes int dateSeparatorTextAppearance) {
        this.dateSeparatorTextAppearance = dateSeparatorTextAppearance;
        cometChatSearchMessageAdapter.setDateSeparatorTextAppearance(dateSeparatorTextAppearance);
    }

    /*** Sets the date format for message timestamps.
     *
     * @param messageDateFormat The SimpleDateFormat object to set as the date format for message timestamps.
     */
    public void setMessageTimestampDateFormat(SimpleDateFormat messageDateFormat) {
        cometChatSearchMessageAdapter.setMessageTimestampDateFormat(messageDateFormat);
    }

    /*** Sets the date format for conversation timestamps.
     *
     * @param conversationDateFormat The SimpleDateFormat object to set as the date format for conversation timestamps.
     */
    public void setConversationTimestampDateFormat(SimpleDateFormat conversationDateFormat) {
        cometChatSearchConversationsAdapter.setConversationTimestampDateFormat(conversationDateFormat);
    }

    /*** Returns the date style resource ID for message timestamps.
     *
     * @return The style resource ID representing the date style for message timestamps.
     */
    public @StyleRes int getMessageTimestampDateStyle() {
        return messageTimestampDateStyle;
    }

    public void setHintText(String string) {
        binding.searchInput.setHint(string);
    }

    /**
     * Sets a custom label for the "mention all" feature for a specific ID.
     *
     * @param id The unique identifier (such as a group or user ID) for which the mention all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     *
     * If either parameter is null or empty, or if the mentions formatter is not initialized, this method does nothing.
     */
    public void setMentionAllLabelId(String id, String mentionAllLabel) {
        if (id != null && !id.isEmpty() && mentionAllLabel != null && !mentionAllLabel.isEmpty()) {
            if (cometchatMentionsFormatter != null) {
                cometchatMentionsFormatter.setMentionAllLabel(id, mentionAllLabel);
            }
            this.mentionAllLabelId = id;
            this.mentionAllLabel = mentionAllLabel;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            attachObservers();
            isDetachedFromWindow = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        disposeObservers();
        isDetachedFromWindow = true;
        super.onDetachedFromWindow();
    }

    public void disposeObservers() {
        if (cometChatSearchViewModel != null) {
            cometChatSearchViewModel.getMutableLiveDataIsSearching().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getCometChatException().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableMessagesRangeChanged().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableConversationsRangeChanged().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableHasMorePreviousMessages().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableHasMorePreviousConversations().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableConversationList().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getMutableMessageList().removeObservers(lifecycleOwner);
            cometChatSearchViewModel.getStates().removeObservers(lifecycleOwner);
        }
    }
}
