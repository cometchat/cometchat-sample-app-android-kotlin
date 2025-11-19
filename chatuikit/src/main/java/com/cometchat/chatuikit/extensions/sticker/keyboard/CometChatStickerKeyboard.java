package com.cometchat.chatuikit.extensions.sticker.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatStickerViewBinding;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.extensions.sticker.keyboard.adapter.StickerTabAdapter;
import com.cometchat.chatuikit.extensions.sticker.keyboard.listener.StickerClickListener;
import com.cometchat.chatuikit.extensions.sticker.keyboard.model.Sticker;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.reaction.ExtensionResponseListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CometChatStickerKeyboard extends MaterialCardView implements StickerClickListener {
    private static final String TAG = CometChatStickerKeyboard.class.getSimpleName();
    private static final String STICKER_LIST_CONSTANT = "stickerList";
    private CometchatStickerViewBinding binding;
    private StickerTabAdapter adapter;
    private HashMap<String, List<Sticker>> stickerMap = new HashMap<>();
    private StickerClickListener stickerClickListener;
    private View customEmptyView = null;
    private View customErrorView = null;
    private View customLoadingView = null;
    private @ColorInt int backgroundColor;
    private Drawable backgroundDrawable;
    private @ColorInt int separatorColor;
    private @StyleRes int emptyStateTitleTextAppearance;
    private @StyleRes int emptyStateSubtitleTextAppearance;
    private @StyleRes int errorStateTextAppearance;
    private @ColorInt int errorStateTextColor;
    private @ColorInt int emptyStateTitleTextColor;
    private @ColorInt int emptyStateSubtitleTextColor;
    private @StyleRes int style;
    private String errorStateText;
    private String emptyStateTitleText;
    private String emptyStateSubtitleText;

    /**
     * Constructs a new CometChatStickerKeyboard with the specified context.
     *
     * @param context The Context in which this keyboard is being used.
     */
    public CometChatStickerKeyboard(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatStickerKeyboard with the specified context and
     * attribute set.
     *
     * @param context The Context in which this keyboard is being used.
     * @param attrs   The AttributeSet containing custom attributes, or null if no
     *                attributes are specified.
     */
    public CometChatStickerKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatStickerKeyboardStyle);
    }

    /**
     * Constructs a new CometChatStickerKeyboard with the specified context,
     * attribute set, and default style attribute.
     *
     * @param context      The Context in which this keyboard is being used.
     * @param attrs        The AttributeSet containing custom attributes, or null if no
     *                     attributes are specified.
     * @param defStyleAttr The default style to apply to this keyboard. If 0, no style will
     *                     be applied.
     */
    public CometChatStickerKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViewComponent(attrs, defStyleAttr);
    }

    /**
     * Initializes the UI components and sets up event listeners for the
     * CometChatStickerKeyboard.
     *
     * @param attributeSet The AttributeSet containing custom attributes, or null if no
     *                     attributes are specified.
     * @param defStyleAttr The default style attribute to apply to this view.
     */
    private void initViewComponent(AttributeSet attributeSet, int defStyleAttr) {
        // Inflate the sticker view layout and bind it to this view
        binding = CometchatStickerViewBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Initialize the material card style for this view
        Utils.initMaterialCard(this);

        // Create an adapter for managing sticker tabs, using the fragment manager from
        // the context
        try {
            Activity activity = Utils.getActivity(getContext());
            if (Utils.isActivityUsable(activity) && activity instanceof FragmentActivity) {
                adapter = new StickerTabAdapter(((FragmentActivity) activity).getSupportFragmentManager());
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }

        // Disable ripple effect on tab layout
        binding.tabLayout.setTabRippleColor(null);

        // Add a listener to handle tab selection events
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab selected
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselected
            }
        });

        // Set an OnClickListener for the retry button to fetch stickers when clicked
        binding.retryBtn.setOnClickListener(view -> {
            fetchStickers();
        });

        // Apply style attributes to the view
        applyStyleAttributes(attributeSet, defStyleAttr, 0);
    }

    /**
     * Fetches stickers from the server and updates the UI based on the response
     * state. Sets the state to LOADING while the request is being made, and updates
     * the state based on the response:
     *
     * <ul>
     * <li>LOADED: when stickers are successfully fetched.
     * <li>NON_EMPTY: when the fetched stickers list is not empty.
     * <li>EMPTY: when the fetched stickers list is empty.
     * <li>ERROR: when the request fails.
     * </ul>
     */
    private void fetchStickers() {
        setState(UIKitConstants.States.LOADING);
        Extensions.fetchStickers(new ExtensionResponseListener() {
            @Override
            public void OnResponseSuccess(Object var) {
                setState(UIKitConstants.States.LOADED);
                JSONObject stickersJSON = (JSONObject) var;
                HashMap<String, List<Sticker>> stringListHashMap = Extensions.extractStickersFromJSON(stickersJSON);
                if (!stringListHashMap.isEmpty()) {
                    setState(UIKitConstants.States.NON_EMPTY);
                    setData(Extensions.extractStickersFromJSON(stickersJSON));
                } else {
                    setState(UIKitConstants.States.EMPTY);
                }
            }

            @Override
            public void OnResponseFailed(CometChatException e) {
                setState(UIKitConstants.States.ERROR);
            }
        });
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatStickerKeyboard, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatStickerKeyboard, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    public void setState(@NonNull UIKitConstants.States states) {
        // Handle different states
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
                // Handle unexpected states if necessary
                break;
        }
    }

    /**
     * Sets the data for stickers and updates the view with fragments for each
     * sticker group.
     *
     * @param stickers A HashMap where the key is a String identifier and the value is a
     *                 list of Stickers.
     */
    public void setData(HashMap<String, List<Sticker>> stickers) {
        this.stickerMap = stickers;
        if (adapter != null) {
            for (String str : stickerMap.keySet()) {
                Bundle bundle = new Bundle();
                StickerFragment stickersFragment = new StickerFragment();
                bundle.putParcelableArrayList(STICKER_LIST_CONSTANT, (ArrayList<? extends Parcelable>) stickerMap.get(str));
                stickersFragment.setArguments(bundle);
                stickersFragment.setStickerClickListener(stickerClickListener);

                ArrayList<Sticker> stickerList = new ArrayList<>(stickerMap.get(str));
                bundle.putParcelableArrayList(STICKER_LIST_CONSTANT, stickerList);
                adapter.addFragment(stickersFragment, str, stickerList.get(0).getUrl());
            }
            binding.viewPager.setAdapter(adapter);
            binding.tabLayout.setupWithViewPager(binding.viewPager);

            for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(createTabItemView(adapter.getPageIcon(i)));
                }
            }
        }
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setCardBackgroundColor(typedArray.getColor(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardBackgroundColor,
                                                       CometChatTheme.getBackgroundColor1(getContext())));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardBackgroundDrawable));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardSeparatorColor,
                                                  CometChatTheme.getStrokeColorDefault(getContext())));

            setEmptyStateTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateTitleTextAppearance,
                                                                      0));
            setEmptyStateSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateSubtitleTextAppearance,
                                                                         0));
            setEmptyStateTitleTextColor(typedArray.getColor(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateTitleTextColor,
                                                            CometChatTheme.getTextColorPrimary(getContext())));
            setEmptyStateSubtitleTextColor(typedArray.getColor(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardEmptyStateSubtitleTextColor,
                                                               CometChatTheme.getTextColorSecondary(getContext())));

            setErrorStateTextAppearance(typedArray.getResourceId(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardErrorStateTextAppearance,
                                                                 0));
            setErrorStateTextColor(typedArray.getColor(R.styleable.CometChatStickerKeyboard_cometchatStickerKeyboardErrorStateTextColor,
                                                       CometChatTheme.getTextColorSecondary(getContext())));

        } finally {
            typedArray.recycle();
        }
    }

    // Helper method to handle the LOADING state
    private void handleLoadingState() {
        if (customLoadingView != null) {
            binding.stickerCustomLayout.setVisibility(View.VISIBLE);
            binding.stickerCustomLayout.removeAllViews();
            binding.stickerCustomLayout.addView(customLoadingView);
        } else {
            // Show shimmer
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(20, R.layout.cometchat_sticker_keyboard_shimmer);
            binding.rvShimmerEffect.setAdapter(adapter);
            binding.rvShimmerEffect.setLayoutManager(new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL, false));
            binding.shimmerEffectFrame.setVisibility(View.VISIBLE);
            binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            binding.shimmerEffectFrame.startShimmer();
        }
    }

    // Helper method to handle the LOADED state
    private void handleLoadedState() {
        hideShimmer();
        hideAllStates();
        binding.stickersView.setVisibility(View.VISIBLE);
    }

    // Helper method to handle the ERROR state
    private void handleErrorState() {
        if (customErrorView != null) {
            binding.stickerCustomLayout.setVisibility(View.VISIBLE);
            binding.stickerCustomLayout.removeAllViews();
            binding.stickerCustomLayout.addView(customErrorView);
        } else {
            hideShimmer();
            hideAllStates();
            // Show Error State
            binding.tvErrorStickerTitle.setText(errorStateText == null ? getResources().getString(R.string.cometchat_looks_like_something_went_wrong_n_please_try_again) : errorStateText);
            binding.errorStickerLayout.setVisibility(View.VISIBLE);
        }
    }

    // Helper method to handle the EMPTY state
    private void handleEmptyState() {
        if (customEmptyView != null) {
            binding.stickerCustomLayout.setVisibility(View.VISIBLE);
            binding.stickerCustomLayout.removeAllViews();
            binding.stickerCustomLayout.addView(customEmptyView);
        } else {
            hideShimmer();
            hideErrorState();
            binding.tvEmptyStickerTitle.setText(emptyStateTitleText == null ? getResources().getString(R.string.cometchat_no_stickers_available) : emptyStateTitleText);
            binding.tvEmptyStickerSubtitle.setText(emptyStateSubtitleText == null ? getResources().getString(R.string.cometchat_you_don_t_have_any_stickers_yet) : emptyStateSubtitleText);
            binding.emptyStickerLayout.setVisibility(View.VISIBLE);
        }
        binding.stickersView.setVisibility(View.GONE);
    }

    // Helper method to handle the NON_EMPTY state
    private void handleNonEmptyState() {
        hideShimmer();
        hideAllStates();
        binding.stickersView.setVisibility(View.VISIBLE);
    }

    /**
     * Creates a tab item view with an image loaded from the specified URI.
     *
     * @param imgUri The URI of the image to load into the ImageView.
     * @return A View containing an ImageView with the loaded image.
     */
    private View createTabItemView(String imgUri) {
        ImageView imageView = new ImageView(getContext());
        LayoutParams params = new LayoutParams(72, 72);
        imageView.setLayoutParams(params);
        try {
            Glide.with(getContext()).load(imgUri).placeholder(R.drawable.cometchat_progress_drawable).into(imageView);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return imageView;
    }

    /**
     * Sets the background color of the card.
     *
     * @param backgroundColor The color to set as the card's background.
     */
    @Override
    public void setCardBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    // Utility method to hide shimmer effect
    private void hideShimmer() {
        binding.shimmerEffectFrame.setVisibility(View.GONE);
        binding.shimmerEffectFrame.stopShimmer();
    }

    // Utility method to hide all states except the message list
    private void hideAllStates() {
        binding.stickerCustomLayout.setVisibility(View.GONE);
        binding.errorStickerLayout.setVisibility(View.GONE);
        binding.emptyStickerLayout.setVisibility(View.GONE);
    }

    // Utility method to hide the error state only
    private void hideErrorState() {
        binding.errorStickerLayout.setVisibility(View.GONE);
    }

    /**
     * Sets a custom empty state view to be displayed when no stickers are
     * available.
     *
     * @param id The resource ID of the layout to inflate as the empty state view.
     *           If 0, no view is set.
     */
    public void setEmptyStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customEmptyView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customEmptyView = null; // Handle exception and set to null if it fails
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * setErrorStateView is method allows you to set layout, show when there is a
     * error if Group want to set Error layout other wise it will load default
     * layout
     */
    public void setErrorStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customErrorView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customErrorView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets a custom loading state view to be displayed while stickers are being
     * fetched.
     *
     * @param id The resource ID of the layout to inflate as the loading state
     *           view. If 0, no view is set.
     */
    public void setLoadingStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customLoadingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customLoadingView = null; // Handle exception and set to null if it fails
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    // Getters for testing or direct access if needed
    public StickerTabAdapter getAdapter() {
        return adapter;
    }

    public HashMap<String, List<Sticker>> getStickerMap() {
        return stickerMap;
    }

    public StickerClickListener getStickerClickListener() {
        return stickerClickListener;
    }

    /**
     * Sets the sticker click listener for this keyboard.
     *
     * @param stickerClickListener The listener to be notified when a sticker is clicked.
     */
    public void setStickerClickListener(StickerClickListener stickerClickListener) {
        this.stickerClickListener = stickerClickListener;
    }

    public View getCustomEmptyView() {
        return customEmptyView;
    }

    public View getCustomErrorView() {
        return customErrorView;
    }

    public View getCustomLoadingView() {
        return customLoadingView;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable of the card.
     *
     * @param backgroundDrawable The Drawable to set as the background.
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    public int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Sets the color of the separator line in the view.
     *
     * @param separatorColor The color to set for the separator line.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.separator.setBackgroundColor(separatorColor);
    }

    public int getEmptyStateTitleTextAppearance() {
        return emptyStateTitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the empty state title.
     *
     * @param emptyStateTitleTextAppearance The resource ID of the style to apply to the title.
     */
    public void setEmptyStateTitleTextAppearance(@StyleRes int emptyStateTitleTextAppearance) {
        this.emptyStateTitleTextAppearance = emptyStateTitleTextAppearance;
        binding.tvEmptyStickerTitle.setTextAppearance(emptyStateTitleTextAppearance);
    }

    public int getEmptyStateSubtitleTextAppearance() {
        return emptyStateSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the empty state subtitle.
     *
     * @param emptyStateSubtitleTextAppearance The resource ID of the style to apply to the subtitle.
     */
    public void setEmptyStateSubtitleTextAppearance(@StyleRes int emptyStateSubtitleTextAppearance) {
        this.emptyStateSubtitleTextAppearance = emptyStateSubtitleTextAppearance;
        binding.tvEmptyStickerSubtitle.setTextAppearance(emptyStateSubtitleTextAppearance);
    }

    public int getErrorStateTextAppearance() {
        return errorStateTextAppearance;
    }

    /**
     * Sets the text appearance style for the error state.
     *
     * @param errorStateTextAppearance The resource ID of the style to apply.
     */
    public void setErrorStateTextAppearance(@StyleRes int errorStateTextAppearance) {
        this.errorStateTextAppearance = errorStateTextAppearance;
        binding.tvErrorStickerTitle.setTextAppearance(errorStateTextAppearance);
    }

    public int getErrorStateTextColor() {
        return errorStateTextColor;
    }

    /**
     * Sets the text color for the error state.
     *
     * @param errorStateTextColor The color to set for the error state text.
     */
    public void setErrorStateTextColor(@ColorInt int errorStateTextColor) {
        this.errorStateTextColor = errorStateTextColor;
        binding.tvErrorStickerTitle.setTextColor(errorStateTextColor);
    }

    public int getEmptyStateTitleTextColor() {
        return emptyStateTitleTextColor;
    }

    /**
     * Sets the text color for the empty state title.
     *
     * @param emptyStateTitleTextColor The color to set for the empty state title text.
     */
    public void setEmptyStateTitleTextColor(@ColorInt int emptyStateTitleTextColor) {
        this.emptyStateTitleTextColor = emptyStateTitleTextColor;
        binding.tvEmptyStickerTitle.setTextColor(emptyStateTitleTextColor);
    }

    public int getEmptyStateSubtitleTextColor() {
        return emptyStateSubtitleTextColor;
    }

    /**
     * Sets the text color for the empty state subtitle.
     *
     * @param emptyStateSubtitleTextColor The color to set for the empty state subtitle text.
     */
    public void setEmptyStateSubtitleTextColor(@ColorInt int emptyStateSubtitleTextColor) {
        this.emptyStateSubtitleTextColor = emptyStateSubtitleTextColor;
        binding.tvEmptyStickerSubtitle.setTextColor(emptyStateSubtitleTextColor);
    }

    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatStickerKeyboard);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public String getErrorStateText() {
        return errorStateText;
    }

    /**
     * Sets the text to display in the error state.
     *
     * @param errorStateText The text to display when the error state is active.
     */
    public void setErrorStateText(@NonNull String errorStateText) {
        this.errorStateText = errorStateText;
        binding.tvErrorStickerTitle.setText(errorStateText);
    }

    public String getEmptyStateTitleText() {
        return emptyStateTitleText;
    }

    /**
     * Sets the title text for the empty state.
     *
     * @param emptyStateTitleText The text to display as the title when the empty state is active.
     */
    public void setEmptyStateTitleText(@NonNull String emptyStateTitleText) {
        this.emptyStateTitleText = emptyStateTitleText;
        binding.tvEmptyStickerTitle.setText(emptyStateTitleText);
    }

    public String getEmptyStateSubtitleText() {
        return emptyStateSubtitleText;
    }

    /**
     * Sets the subtitle text for the empty state.
     *
     * @param emptyStateSubtitleText The text to display as the subtitle when the empty state is
     *                               active.
     */
    public void setEmptyStateSubtitleText(@NonNull String emptyStateSubtitleText) {
        this.emptyStateSubtitleText = emptyStateSubtitleText;
        binding.tvEmptyStickerSubtitle.setText(emptyStateSubtitleText);
    }

    public CometchatStickerViewBinding getBinding() {
        return binding;
    }

    @Override
    public void onClickListener(Sticker sticker) {
        stickerClickListener.onClickListener(sticker);
    }
}
