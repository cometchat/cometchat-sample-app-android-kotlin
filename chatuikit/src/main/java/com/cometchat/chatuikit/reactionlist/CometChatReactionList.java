package com.cometchat.chatuikit.reactionlist;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.core.ReactionsRequest;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Reaction;
import com.cometchat.chat.models.ReactionCount;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatReactionListBinding;
import com.cometchat.chatuikit.reactionlist.adapter.ReactedUsersAdapter;
import com.cometchat.chatuikit.reactionlist.adapter.ReactionsHeaderAdapter;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * The CometChatReactionList class is a custom view in Android that represents a
 * list of reactions, typically used for displaying a list of reactions and the
 * users who reacted to a message. It extends the MaterialCardView class and
 * provides various customization options such as setting the background color,
 * stroke color, stroke width, corner radius, and elevation. The reaction list
 * can be set with a list of reactions and users who reacted to a message, and
 * if no reactions are available, it can display an error message. The class
 * uses the MaterialCardView for displaying the reaction list and supports
 * applying styles to the reaction list using a style resource. Created on: 17
 * Sept 2024 Modified on: 12 Oct 2024
 */
public class CometChatReactionList extends MaterialCardView {
    private static final String TAG = CometChatReactionList.class.getSimpleName();

    private CometchatReactionListBinding binding;

    private String errorText;
    private String selectedReaction;
    private BaseMessage baseMessage;

    private boolean isReactedUserListLoading = false;
    private boolean hideSeparator = false;

    private List<ReactionCount> reactionHeaderList = new ArrayList<>();
    private ReactionsHeaderAdapter reactionsHeaderAdapter;
    private ReactedUsersAdapter reactedUsersAdapter;
    private ReactionListViewModel reactionListViewModel;
    private OnClick onEmpty;
    private ReactionsRequest.ReactionsRequestBuilder reactionsRequestBuilder;

    private View errorView;
    private View loadingView;

    private @ColorInt int reactionListBackgroundColor;
    private @ColorInt int reactionListStrokeColor;
    private @ColorInt int reactionListTitleTextColor;
    private @ColorInt int reactionListTabTextColor;
    private @ColorInt int reactionListTabTextActiveColor;
    private @ColorInt int reactionListTabActiveIndicatorColor;
    private @ColorInt int reactionListSubTitleTextColor;
    private @Dimension int reactionListStrokeWidth;
    private @Dimension int reactionListCornerRadius;
    private @StyleRes int reactionListTitleTextAppearance;
    private @StyleRes int reactionListTabTextAppearance;
    private @StyleRes int reactionListTailViewTextAppearance;
    private @StyleRes int reactionListSubTitleTextAppearance;
    private @ColorInt int reactionListErrorTextColor;
    private @ColorInt int reactionListSeparatorColor;
    private @StyleRes int reactionListAvatarStyle;
    private @StyleRes int reactionListErrorTextAppearance;

    /**
     * Constructs a new CometChatReactionList view with no attributes set.
     *
     * @param context The context in which the view is created.
     */
    public CometChatReactionList(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatReactionList view with the specified attributes
     * set.
     *
     * @param context The context in which the view is created.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CometChatReactionList(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatReactionListStyle);
    }

    /**
     * Constructs a new CometChatReactionList view with the specified attributes
     * set.
     *
     * @param context      The context in which the view is created.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    public CometChatReactionList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates the layout and initializes the view.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);

        // Inflate the layout for this view
        binding = CometchatReactionListBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Init RecyclerViews
        initRecyclerViews();

        // Init view model
        initViewModel();

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Configures the RecyclerViews for the CometChatReactionList view.
     */
    private void initRecyclerViews() {
        reactedUsersAdapter = new ReactedUsersAdapter(getContext());
        reactionsHeaderAdapter = new ReactionsHeaderAdapter(getContext());

        // For users
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUsers.setAdapter(reactedUsersAdapter);
        reactedUsersAdapter.setReactedUsersAdapterEventListener((baseMessage, reactionList, position) -> {
            final String loggedInUserUID = CometChatUIKit.getLoggedInUser().getUid();
            final Reaction reaction = reactionList.get(position);
            if (reaction.getReactedBy().getUid().equals(loggedInUserUID)) {
                reactionListViewModel.removeReaction(baseMessage, reaction.getReaction());
            }
        });
        binding.recyclerViewUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (!isReactedUserListLoading) {
                        reactionListViewModel.getReactedUsersList(null, null);
                    }
                }
            }
        });

        // For emojis
        binding.recyclerViewEmojis.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewEmojis.setAdapter(reactionsHeaderAdapter);
        reactionsHeaderAdapter.setEmojiAdapterEventListener(emoji -> reactionListViewModel.setSelectedReactionLiveData(emoji));
    }

    /**
     * Configures the RecyclerViews and the ViewModel for the CometChatReactionList
     * view.
     */
    private void initViewModel() {
        reactionListViewModel = new ReactionListViewModel();
        reactionListViewModel.setContext(getContext());

        // Reset the data
        reactionListViewModel.clearReactionRequestHashMap();
        reactionListViewModel.clearReactedUserCacheHashMap();

        reactionListViewModel.getLoadingStateLiveData().observe((LifecycleOwner) getContext(), this::stateChangeObserver);
        reactionListViewModel.getBaseMessageLiveData().observe((LifecycleOwner) getContext(), this::setBaseMessage);
        reactionListViewModel.getReactionHeaderLiveData().observe((LifecycleOwner) getContext(), this::setReactionHeaderList);
        reactionListViewModel.getSelectedReactionLiveData().observe((LifecycleOwner) getContext(), this::updatedSelectedReaction);
        reactionListViewModel.getActiveTabIndexLiveData().observe((LifecycleOwner) getContext(), this::setActiveTab);
        reactionListViewModel.getReactedUsersLiveData().observe((LifecycleOwner) getContext(), this::setReactedUsersList);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatReactionList, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatReactionList, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Returns the request builder for fetching reactions.
     */
    private void stateChangeObserver(UIKitConstants.States loadingState) {
        if (loadingState == UIKitConstants.States.LOADING) {
            if (getLoadingView() != null) {
                binding.layoutLoadingState.removeAllViews();
                Utils.removeParentFromView(getLoadingView());
                binding.layoutLoadingState.addView(getLoadingView());
            } else {
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(10, R.layout.shimmer_cometchat_reaction_list_items);
                binding.shimmerParentLayout.shimmerRecyclerviewReactionList.setAdapter(adapter);
                binding.shimmerParentLayout.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerParentLayout.shimmerEffectFrame.startShimmer();
            }
            binding.layoutLoadingState.setVisibility(View.VISIBLE);
            binding.recyclerViewUsers.setVisibility(GONE);
        } else if (loadingState == UIKitConstants.States.LOADED) {
            if (getLoadingView() == null) {
                binding.shimmerParentLayout.shimmerEffectFrame.stopShimmer();
            }
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.recyclerViewUsers.setVisibility(VISIBLE);
        } else {
            if (getErrorView() != null) {
                binding.layoutErrorState.removeAllViews();
                Utils.removeParentFromView(getErrorView());
                binding.layoutErrorState.addView(getErrorView());
            } else {
                if (getErrorText() != null) {
                    setErrorText(getErrorText());
                } else {
                    setErrorText(getContext().getString(R.string.cometchat_reaction_list_error));
                }
            }
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.recyclerViewUsers.setVisibility(GONE);
            binding.layoutErrorState.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the list of reaction headers for the reaction list.
     *
     * @param reactionHeaderList The list of ReactionCount objects.
     */
    public void setReactionHeaderList(List<ReactionCount> reactionHeaderList) {
        this.reactionHeaderList = reactionHeaderList;
        if (reactionListViewModel != null)
            reactionListViewModel.setSelectedReactionLiveData(selectedReaction);
    }

    /**
     * Updates the selected reaction and handles the loader state.
     *
     * @param selectedReaction The newly selected reaction string.
     */
    public void updatedSelectedReaction(String selectedReaction) {
        this.selectedReaction = selectedReaction;
        if (reactionListViewModel != null) {
            reactionListViewModel.setActiveTabIndexLiveData(0);
        }
    }

    /**
     * Sets the active tab in the reaction header.
     *
     * @param activeTab The index of the active tab.
     */
    public void setActiveTab(int activeTab) {
        if (reactionsHeaderAdapter != null) {
            if (reactionHeaderList.isEmpty()) {
                reactedUsersAdapter.setReactedUsersList(new ArrayList<>());
                if (onEmpty != null) {
                    onEmpty.onClick();
                }
            }
            reactionsHeaderAdapter.setActiveTab(activeTab);
            reactionsHeaderAdapter.updateReactionHeaderList(reactionHeaderList);
            if (reactionListViewModel != null) {
                reactionListViewModel.getReactedUsersList(activeTab == 0 ? getContext().getString(R.string.cometchat_all) : this.selectedReaction,
                                                          getReactionsRequestBuilder());
            }
        }
    }

    /**
     * Sets the list of reactions for the reaction list.
     *
     * @param reactions The list of Reaction objects.
     */
    public void setReactedUsersList(List<Reaction> reactions) {
        if (reactions != null) {
            if (reactedUsersAdapter != null) {
                reactedUsersAdapter.setReactedUsersList(reactions);
            }
        }
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            reactionListBackgroundColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListBackgroundColor,
                                                              reactionListBackgroundColor);
            reactionListStrokeColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListStrokeColor,
                                                          reactionListStrokeColor);
            reactionListTitleTextColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListTitleTextColor,
                                                             reactionListTitleTextColor);
            reactionListTabTextColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListTabTextColor,
                                                           reactionListTabTextColor);
            reactionListTabTextActiveColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListTabTextActiveColor,
                                                                 reactionListTabTextActiveColor);
            reactionListTabActiveIndicatorColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListTabActiveIndicatorColor,
                                                                      reactionListTabActiveIndicatorColor);
            reactionListSubTitleTextColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListSubTitleTextColor,
                                                                reactionListSubTitleTextColor);
            reactionListErrorTextColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListErrorTextColor,
                                                             reactionListErrorTextColor);
            reactionListAvatarStyle = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListAvatarStyle,
                                                               reactionListAvatarStyle);
            reactionListStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatReactionList_cometchatReactionListStrokeWidth,
                                                                       reactionListStrokeWidth);
            reactionListCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatReactionList_cometchatReactionListCornerRadius,
                                                                        reactionListCornerRadius);
            reactionListTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListTitleTextAppearance,
                                                                       reactionListTitleTextAppearance);
            reactionListTabTextAppearance = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListTabTextAppearance,
                                                                     reactionListTabTextAppearance);
            reactionListTailViewTextAppearance = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListTailViewTextAppearance,
                                                                          reactionListTailViewTextAppearance);
            reactionListSubTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListSubTitleTextAppearance,
                                                                          reactionListSubTitleTextAppearance);
            reactionListErrorTextAppearance = typedArray.getResourceId(R.styleable.CometChatReactionList_cometchatReactionListErrorTextApAppearance,
                                                                       reactionListErrorTextAppearance);
            reactionListSeparatorColor = typedArray.getColor(R.styleable.CometChatReactionList_cometchatReactionListSeparatorColor, 0);

            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Returns the loading view in the reaction list.
     *
     * @return The loading view object.
     */
    public View getLoadingView() {
        return loadingView;
    }

    /**
     * Returns the error view in the reaction list.
     *
     * @return The error view object.
     */
    public View getErrorView() {
        return errorView;
    }

    /**
     * Sets the error view for the reaction list.
     *
     * @param errorView The view to display when there is an error.
     */
    public void setErrorView(View errorView) {
        this.errorView = errorView;
    }

    /**
     * Returns the error text in the reaction list.
     *
     * @return The error text string.
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * Sets the error text for the reaction list. This text will be displayed when
     * an error occurs.
     *
     * @param errorText The error message string.
     */
    public void setErrorText(String errorText) {
        this.errorText = errorText;
        binding.tvErrorState.setText(errorText);
    }

    /**
     * Returns the request builder for fetching reactions.
     *
     * @return The ReactionsRequest.ReactionsRequestBuilder object.
     */
    public ReactionsRequest.ReactionsRequestBuilder getReactionsRequestBuilder() {
        return reactionsRequestBuilder;
    }

    /**
     * Applies the extracted or default values to the avatar's views.
     */
    private void updateUI() {
        setReactionListBackgroundColor(reactionListBackgroundColor);
        setReactionListStrokeColor(reactionListStrokeColor);
        setReactionListTitleTextColor(reactionListTitleTextColor);
        setReactionListTabTextColor(reactionListTabTextColor);
        setReactionListTabTextActiveColor(reactionListTabTextActiveColor);
        setReactionListTabActiveIndicatorColor(reactionListTabActiveIndicatorColor);
        setReactionListSubTitleTextColor(reactionListSubTitleTextColor);
        setReactionListErrorTextColor(reactionListErrorTextColor);
        setReactionListAvatarStyle(reactionListAvatarStyle);
        setReactionListStrokeWidth(reactionListStrokeWidth);
        setReactionListCornerRadius(reactionListCornerRadius);
        setReactionListTitleTextAppearance(reactionListTitleTextAppearance);
        setReactionListTabTextAppearance(reactionListTabTextAppearance);
        setReactionListTailViewTextAppearance(reactionListTailViewTextAppearance);
        setReactionListSubTitleTextAppearance(reactionListSubTitleTextAppearance);
        setReactionListErrorTextAppearance(reactionListErrorTextAppearance);
        setReactionListSeparatorColor(reactionListSeparatorColor);
    }

    /**
     * Sets the request builder for fetching reactions.
     *
     * @param reactionsRequestBuilder The ReactionsRequest.ReactionsRequestBuilder object.
     */
    public void setReactionsRequestBuilder(ReactionsRequest.ReactionsRequestBuilder reactionsRequestBuilder) {
        if (reactionsRequestBuilder != null)
            this.reactionsRequestBuilder = reactionsRequestBuilder;
    }

    /**
     * Sets the loading view for the reaction list.
     *
     * @param loadingView The view to display when reactions are loading.
     */
    public void setLoadingView(View loadingView) {
        this.loadingView = loadingView;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (reactedUsersAdapter != null) {
            reactedUsersAdapter.setBaseMessage(baseMessage);
        }
        if (reactionListViewModel != null) {
            reactionListViewModel.addListener();
            String newBaseMessageString = new Gson().toJson(baseMessage);
            BaseMessage newBaseMessage = new Gson().fromJson(newBaseMessageString, BaseMessage.class);
            reactionListViewModel.setBaseMessageLiveData(newBaseMessage);
            reactionListViewModel.setReactionHeaderLiveData(baseMessage.getReactions());
        }
    }

    /**
     * Sets the style for the CometChatReactionList view by applying a style
     * resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatReactionList);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the subtitle text style for the reaction list.
     *
     * @return The resource ID of the subtitle text style.
     */
    public @ColorInt int getReactionListSubTitleTextAppearance() {
        return reactionListSubTitleTextAppearance;
    }

    /**
     * Sets the subtitle text style for the reaction list.
     *
     * @param reactionListSubTitleTextAppearance The resource ID of the subtitle text style.
     */
    public void setReactionListSubTitleTextAppearance(@StyleRes int reactionListSubTitleTextAppearance) {
        this.reactionListSubTitleTextAppearance = reactionListSubTitleTextAppearance;
        reactedUsersAdapter.setSubTitleTextAppearance(reactionListSubTitleTextAppearance);
    }

    /**
     * Returns the tail view text style for the reaction list.
     *
     * @return The resource ID of the tail view text style.
     */
    public @StyleRes int getReactionListTailViewTextAppearance() {
        return reactionListTailViewTextAppearance;
    }

    /**
     * Sets the tail view text style for the reaction list.
     *
     * @param reactionListTailViewTextAppearance The resource ID of the tail view text style.
     */
    public void setReactionListTailViewTextAppearance(@StyleRes int reactionListTailViewTextAppearance) {
        this.reactionListTailViewTextAppearance = reactionListTailViewTextAppearance;
        reactedUsersAdapter.setTailViewTextAppearance(reactionListTailViewTextAppearance);
    }

    /**
     * Returns the tab text style for the reaction list.
     *
     * @return The resource ID of the tab text style.
     */
    public @StyleRes int getReactionListTabTextAppearance() {
        return reactionListTabTextAppearance;
    }

    /**
     * Sets the tab text style for the reaction list.
     *
     * @param reactionListTabTextAppearance The resource ID of the tab text style.
     */
    public void setReactionListTabTextAppearance(@StyleRes int reactionListTabTextAppearance) {
        this.reactionListTabTextAppearance = reactionListTabTextAppearance;
        reactionsHeaderAdapter.setTextAppearance(reactionListTabTextAppearance);
    }

    /**
     * Returns the title text style for the reaction list.
     *
     * @return The resource ID of the title text style.
     */
    public @StyleRes int getReactionListTitleTextAppearance() {
        return reactionListTitleTextAppearance;
    }

    /**
     * Sets the title text style for the reaction list.
     *
     * @param reactionListTitleTextAppearance The resource ID of the title text style.
     */
    public void setReactionListTitleTextAppearance(@StyleRes int reactionListTitleTextAppearance) {
        this.reactionListTitleTextAppearance = reactionListTitleTextAppearance;
        reactedUsersAdapter.setTitleTextAppearance(reactionListTitleTextAppearance);
    }

    /**
     * Returns the corner radius for the reaction list. This defines the radius of
     * the top-left and top-right corners of the reaction list card.
     *
     * @return The dimension of the corner radius in pixels.
     */
    public @Dimension int getReactionListCornerRadius() {
        return reactionListCornerRadius;
    }

    /**
     * Sets the corner radius for the reaction list. It applies the radius to the
     * top-left and top-right corners, leaving the bottom corners square.
     *
     * @param reactionListCornerRadius The dimension of the corner radius in pixels.
     */
    public void setReactionListCornerRadius(@Dimension int reactionListCornerRadius) {
        this.reactionListCornerRadius = reactionListCornerRadius;
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, reactionListCornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, reactionListCornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0)
            .build();
        binding.cardReactionList.setShapeAppearanceModel(shapeAppearanceModel);
    }

    /**
     * Returns the stroke width for the reaction list. This defines the width of the
     * border stroke around the reaction list card.
     *
     * @return The dimension of the stroke width in pixels.
     */
    public @Dimension int getReactionListStrokeWidth() {
        return reactionListStrokeWidth;
    }

    /**
     * Sets the stroke width for the reaction list. This defines the width of the
     * border around the reaction list card.
     *
     * @param reactionListStrokeWidth The dimension of the stroke width in pixels.
     */
    public void setReactionListStrokeWidth(@Dimension int reactionListStrokeWidth) {
        this.reactionListStrokeWidth = reactionListStrokeWidth;
        binding.cardReactionList.setStrokeWidth(reactionListStrokeWidth);
    }

    /**
     * Retrieves the style resource ID for the error text appearance in the reaction
     * list.
     *
     * @return An integer representing the style resource ID for the reaction list
     * error text appearance.
     */
    public @StyleRes int getReactionListErrorAppearance() {
        return reactionListErrorTextAppearance;
    }

    /**
     * Sets the style resource ID for the error text appearance in the reaction
     * list.
     *
     * @param reactionListErrorTextAppearance The style resource ID to be set for the error text appearance.
     *                                        Must be a valid style resource annotated with @StyleRes.
     */
    public void setReactionListErrorAppearance(@StyleRes int reactionListErrorTextAppearance) {
        this.reactionListErrorTextAppearance = reactionListErrorTextAppearance;
        if (reactionListErrorTextAppearance != 0) {
            binding.tvErrorState.setTextAppearance(reactionListErrorTextAppearance);
        }
    }

    /**
     * Returns the subtitle text color for the reaction list.
     *
     * @return The color integer of the subtitle text.
     */
    public @ColorInt int getReactionListSubTitleTextColor() {
        return reactionListSubTitleTextColor;
    }

    /**
     * Sets the subtitle text color for the reaction list.
     *
     * @param reactionListSubTitleTextColor The color integer for the subtitle text.
     */
    public void setReactionListSubTitleTextColor(@ColorInt int reactionListSubTitleTextColor) {
        this.reactionListSubTitleTextColor = reactionListSubTitleTextColor;
        reactedUsersAdapter.setSubTitleTextColor(reactionListSubTitleTextColor);
    }

    /**
     * Returns the active tab indicator color for the reaction list.
     *
     * @return The color integer of the active tab indicator.
     */
    public @ColorInt int getReactionListTabActiveIndicatorColor() {
        return reactionListTabActiveIndicatorColor;
    }

    /**
     * Sets the active tab indicator color for the reaction list.
     *
     * @param reactionListTabActiveIndicatorColor The color integer for the active tab indicator.
     */
    public void setReactionListTabActiveIndicatorColor(@ColorInt int reactionListTabActiveIndicatorColor) {
        this.reactionListTabActiveIndicatorColor = reactionListTabActiveIndicatorColor;
        reactionsHeaderAdapter.setTabActiveIndicatorColor(reactionListTabActiveIndicatorColor);
    }

    /**
     * Returns the active tab text color for the reaction list.
     *
     * @return The color integer of the active tab text.
     */
    public @ColorInt int getReactionListTabTextActiveColor() {
        return reactionListTabTextActiveColor;
    }

    /**
     * Sets the active tab text color for the reaction list.
     *
     * @param reactionListTabTextActiveColor The color integer for the active tab text.
     */
    public void setReactionListTabTextActiveColor(@ColorInt int reactionListTabTextActiveColor) {
        this.reactionListTabTextActiveColor = reactionListTabTextActiveColor;
        reactionsHeaderAdapter.setTextActiveColor(reactionListTabTextActiveColor);
    }

    /**
     * Returns the tab text color for the reaction list.
     *
     * @return The color integer of the tab text.
     */
    public @ColorInt int getReactionListTabTextColor() {
        return reactionListTabTextColor;
    }

    /**
     * Sets the tab text color for the reaction list.
     *
     * @param reactionListTabTextColor The color integer for the tab text.
     */
    public void setReactionListTabTextColor(@ColorInt int reactionListTabTextColor) {
        this.reactionListTabTextColor = reactionListTabTextColor;
        reactionsHeaderAdapter.setTextColor(reactionListTabTextColor);
    }

    /**
     * Returns the title text color for the reaction list.
     *
     * @return The color integer of the title text.
     */
    public @ColorInt int getReactionListTitleTextColor() {
        return reactionListTitleTextColor;
    }

    /**
     * Sets the title text color for the reaction list.
     *
     * @param reactionListTitleTextColor The color integer for the title text.
     */
    public void setReactionListTitleTextColor(@ColorInt int reactionListTitleTextColor) {
        this.reactionListTitleTextColor = reactionListTitleTextColor;
        reactedUsersAdapter.setTitleTextColor(reactionListTitleTextColor);
    }

    /**
     * Returns the stroke color for the reaction list.
     *
     * @return The color integer of the stroke.
     */
    public @ColorInt int getReactionListStrokeColor() {
        return reactionListStrokeColor;
    }

    /**
     * Sets the stroke color for the reaction list.
     *
     * @param reactionListStrokeColor The color integer for the stroke.
     */
    public void setReactionListStrokeColor(@ColorInt int reactionListStrokeColor) {
        this.reactionListStrokeColor = reactionListStrokeColor;
        binding.cardReactionList.setStrokeColor(reactionListStrokeColor);
    }

    /**
     * Returns the background color for the reaction list.
     *
     * @return The color integer of the background.
     */
    public @ColorInt int getReactionListBackgroundColor() {
        return reactionListBackgroundColor;
    }

    /**
     * Sets the background color for the reaction list.
     *
     * @param reactionListBackgroundColor The color integer for the background.
     */
    public void setReactionListBackgroundColor(@ColorInt int reactionListBackgroundColor) {
        this.reactionListBackgroundColor = reactionListBackgroundColor;
        setCardBackgroundColor(getContext().getResources().getColor(R.color.cometchat_color_transparent, getContext().getTheme()));
        binding.cardReactionList.setCardBackgroundColor(reactionListBackgroundColor);
    }

    /**
     * Sets the item click listener for the ReactedUsersAdapter.
     *
     * @param onReactionListItemClick The item click listener to set.
     */
    public void setOnReactionListItemClick(OnReactionListItemClick onReactionListItemClick) {
        reactedUsersAdapter.setOnReactionListItemClick(onReactionListItemClick);
    }

    /**
     * Returns the error text color for the reaction list.
     *
     * @return The color integer of the error text.
     */
    public @ColorInt int getReactionListErrorTextColor() {
        return reactionListErrorTextColor;
    }

    /**
     * Sets the error text color for the reaction list. This color will be applied
     * to the error text view.
     *
     * @param reactionListErrorTextColor The color integer for the error text.
     */
    public void setReactionListErrorTextColor(@ColorInt int reactionListErrorTextColor) {
        this.reactionListErrorTextColor = reactionListErrorTextColor;
        binding.tvErrorState.setTextColor(reactionListErrorTextColor);
    }

    /**
     * Gets the color of the reaction list separator.
     *
     * @return The color used for the reaction list separator.
     */
    public @ColorInt int getReactionListSeparatorColor() {
        return reactionListSeparatorColor;
    }

    /**
     * Sets the color of the reaction list separator.
     *
     * @param reactionListSeparatorColor The new color for the reaction list separator. Must be a valid
     *                                   color integer.
     */
    public void setReactionListSeparatorColor(@ColorInt int reactionListSeparatorColor) {
        this.reactionListSeparatorColor = reactionListSeparatorColor;
        binding.viewSeparator.setBackgroundColor(CometChatTheme.getStrokeColorDefault(getContext()));
    }

    /**
     * Checks if the separator is hidden.
     *
     * @return {@code true} if the separator is hidden, {@code false} otherwise.
     */
    public boolean hideSeparator() {
        return hideSeparator;
    }

    /**
     * Sets whether the separator should be hidden or visible.
     *
     * @param hideSeparator A boolean indicating if the separator should be hidden. Pass
     *                      {@code true} to hide the separator, or {@code false} to show it.
     */
    public void hideSeparator(boolean hideSeparator) {
        this.hideSeparator = hideSeparator;
        binding.viewSeparator.setVisibility(hideSeparator ? View.GONE : View.VISIBLE);
    }

    /**
     * Returns the avatar style for the reaction list.
     *
     * @return The resource ID of the avatar style.
     */
    public @StyleRes int getReactionListAvatarStyle() {
        return reactionListAvatarStyle;
    }

    /**
     * Sets the avatar style for the reaction list. This style will be applied to
     * the avatars in the reaction list.
     *
     * @param reactionListAvatarStyle The resource ID for the avatar style.
     */
    public void setReactionListAvatarStyle(@StyleRes int reactionListAvatarStyle) {
        this.reactionListAvatarStyle = reactionListAvatarStyle;
        reactedUsersAdapter.setAvatarStyle(reactionListAvatarStyle);
    }

    /**
     * Returns the base message of the reaction list.
     *
     * @return The BaseMessage object.
     */
    public BaseMessage getBaseMessage() {
        return baseMessage;
    }

    /**
     * Sets the base message for the reaction list.
     *
     * @param baseMessage The BaseMessage object to be set.
     */
    public void setBaseMessage(@NonNull BaseMessage baseMessage) {
        this.baseMessage = baseMessage;
    }

    /**
     * Sets the loading state for the reacted users list.
     *
     * @param isReactedUserListLoading A boolean indicating whether the reacted user list is loading.
     */
    public void isReactedUserListLoading(boolean isReactedUserListLoading) {
        this.isReactedUserListLoading = isReactedUserListLoading;
    }

    /**
     * Returns the ViewModel for the CometChatReactionList view.
     *
     * @return The ReactionListViewModel object.
     */
    public ReactionListViewModel getReactionListViewModel() {
        return reactionListViewModel;
    }

    /**
     * Sets the ViewModel for the CometChatReactionList view.
     *
     * @param reactionListViewModel The ReactionListViewModel object.
     */
    public void setReactionListViewModel(ReactionListViewModel reactionListViewModel) {
        this.reactionListViewModel = reactionListViewModel;
    }

    /**
     * Returns the listener for the empty state of the reaction list.
     *
     * @return The OnClick listener object.
     */
    public OnClick getOnEmpty() {
        return onEmpty;
    }

    /**
     * Sets the listener for the empty state of the reaction list.
     *
     * @param onEmpty The OnClick listener object.
     */
    public void setOnEmpty(OnClick onEmpty) {
        this.onEmpty = onEmpty;
    }

    /**
     * Returns the selected reaction in the reaction list.
     *
     * @return The selected reaction string.
     */
    public String getSelectedReaction() {
        return selectedReaction;
    }

    /**
     * Sets the selected reaction in the reaction list.
     *
     * @param selectedReaction The selected reaction string.
     */
    public void setSelectedReaction(String selectedReaction) {
        this.selectedReaction = selectedReaction;
    }

    /**
     * Returns the text appearance for the error text in the reaction list.
     */
    public int getReactionListErrorTextAppearance() {
        return reactionListErrorTextAppearance;
    }

    /**
     * Sets the text appearance for the error text in the reaction list.
     */
    public void setReactionListErrorTextAppearance(int reactionListErrorTextAppearance) {
        this.reactionListErrorTextAppearance = reactionListErrorTextAppearance;
    }
}
