package com.cometchat.chatuikit.shared.views.suggestionlist;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatSuggestionListBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.itemclicklistener.OnItemClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * A custom view that represents a suggestion list using a
 * {@link MaterialCardView}. The {@code
 * CometChatSuggestionList} allows the display of suggestions with customizable
 * attributes such as background color, stroke color, corner radius, and item
 * styles. It also supports avatars and text styling for each suggestion item.
 * This class provides methods to configure the appearance of the list items,
 * handle item clicks, apply style attributes, and manage a shimmer effect
 * during loading.
 */
public class CometChatSuggestionList extends MaterialCardView {
    private static final String TAG = CometChatSuggestionList.class.getSimpleName();
    private CometchatSuggestionListBinding binding;

    private boolean showAvatar = true;

    private @ColorInt int suggestionListBackgroundColor;
    private @ColorInt int suggestionListStrokeColor;
    private @Dimension int suggestionListStrokeWidth;
    private @Dimension int suggestionListCornerRadius;
    private @StyleRes int suggestionListItemAvatarStyle;
    private @StyleRes int suggestionListItemTextAppearance;
    private @ColorInt int suggestionListItemTextColor;
    private @ColorInt int suggestionListItemInfoTextColor;
    private @StyleRes int suggestionListItemInfoTextAppearance;
    private @Dimension int maxHeightLimit;

    private SuggestionListAdapter suggestionListAdapter;
    private OnItemClickListener<SuggestionItem> onItemClickListener;

    /**
     * Constructor to initialize the CometChatSuggestionList with a context.
     *
     * @param context the context in which the view is running
     */
    public CometChatSuggestionList(Context context) {
        this(context, null);
    }

    /**
     * Constructor to initialize the CometChatSuggestionList with a context and
     * attribute set.
     *
     * @param context the context in which the view is running
     * @param attrs   the set of attributes associated with the view
     */
    public CometChatSuggestionList(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatSuggestionListStyle);
    }

    /**
     * Constructor to initialize the CometChatSuggestionList with a context,
     * attribute set, and a default style attribute.
     *
     * @param context      the context in which the view is running
     * @param attrs        the set of attributes associated with the view
     * @param defStyleAttr the default style to apply to this view
     */
    public CometChatSuggestionList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by binding the layout and applying styles.
     *
     * @param attrs        the set of attributes associated with the view
     * @param defStyleAttr the default style to apply to this view
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatSuggestionListBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        init();
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Initializes the suggestion list adapter and configures the recycler view
     * listeners.
     */
    private void init() {
        suggestionListAdapter = new SuggestionListAdapter(getContext(), null);
        binding.recyclerViewSuggestionList.setAdapter(suggestionListAdapter);
        configureRecyclerViewListeners();
    }

    /**
     * Applies the style attributes defined in the XML or by default.
     *
     * @param attrs        the set of attributes associated with the view
     * @param defStyleAttr the default style to apply to this view
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatSuggestionList, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatSuggestionList_cometchatSuggestionListStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatSuggestionList, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Configures the item touch listener and scroll listener for the recycler view.
     */
    private void configureRecyclerViewListeners() {
        binding.recyclerViewSuggestionList.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                                                                                            binding.recyclerViewSuggestionList,
                                                                                            new ClickListener() {
                                                                                                @Override
                                                                                                public void onClick(@NonNull View view,
                                                                                                                    int position) {
                                                                                                    SuggestionItem suggestionItem = (SuggestionItem) view.getTag(
                                                                                                        R.string.cometchat_tag_item);
                                                                                                    if (onItemClickListener != null)
                                                                                                        onItemClickListener.OnItemClick(suggestionItem,
                                                                                                                                        position);
                                                                                                }

                                                                                                @Override
                                                                                                public void onLongClick(View view, int position) {
                                                                                                    SuggestionItem suggestionItem = (SuggestionItem) view.getTag(
                                                                                                        R.string.cometchat_tag_item);
                                                                                                    if (onItemClickListener != null)
                                                                                                        onItemClickListener.OnItemLongClick(
                                                                                                            suggestionItem,
                                                                                                            position);
                                                                                                }
                                                                                            }));

        binding.recyclerViewSuggestionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                recyclerView.canScrollVertically(1);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * Extracts style attributes from the provided TypedArray and applies default
     * values where necessary.
     *
     * @param typedArray the TypedArray containing style attributes
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            suggestionListBackgroundColor = typedArray.getColor(R.styleable.CometChatSuggestionList_cometchatSuggestionListBackgroundColor,
                                                                CometChatTheme.getBackgroundColor1(getContext()));
            suggestionListStrokeColor = typedArray.getColor(R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeColor,
                                                            CometChatTheme.getStrokeColorLight(getContext()));
            suggestionListStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatSuggestionList_cometchatSuggestionListStrokeWidth, 0);
            suggestionListCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatSuggestionList_cometchatSuggestionListCornerRadius, 0);
            suggestionListItemAvatarStyle = typedArray.getResourceId(R.styleable.CometChatSuggestionList_cometchatSuggestionListItemAvatarStyle, 0);
            suggestionListItemTextAppearance = typedArray.getResourceId(R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextAppearance,
                                                                        0);
            suggestionListItemTextColor = typedArray.getColor(R.styleable.CometChatSuggestionList_cometchatSuggestionListItemTextColor,
                                                              CometChatTheme.getTextColorPrimary(getContext()));
            suggestionListItemInfoTextColor = typedArray.getColor(R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextColor,
                                                                    CometChatTheme.getTextColorSecondary(getContext()));
            suggestionListItemInfoTextAppearance = typedArray.getResourceId(R.styleable.CometChatSuggestionList_cometchatSuggestionListItemInfoTextAppearance,
                                                                            0);
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Updates the UI of the suggestion list based on the extracted style
     * attributes.
     */
    private void updateUI() {
        setSuggestionListBackgroundColor(suggestionListBackgroundColor);
        setSuggestionListStrokeColor(suggestionListStrokeColor);
        setSuggestionListStrokeWidth(suggestionListStrokeWidth);
        setSuggestionListCornerRadius(suggestionListCornerRadius);
        setSuggestionListItemAvatarStyle(suggestionListItemAvatarStyle);
        setSuggestionListItemTextAppearance(suggestionListItemTextAppearance);
        setSuggestionListItemTextColor(suggestionListItemTextColor);
        setSuggestionListItemInfoTextColor(suggestionListItemInfoTextColor);
        setSuggestionListItemInfoTextAppearance(suggestionListItemInfoTextAppearance);
    }

    /**
     * Sets the style of the suggestion list using a specified style resource.
     *
     * @param style the style resource to apply to the suggestion list
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatSuggestionList);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the background color of the suggestion list.
     *
     * @return the background color of the suggestion list
     */
    public @ColorInt int getSuggestionListBackgroundColor() {
        return suggestionListBackgroundColor;
    }

    /**
     * Sets the background color of the suggestion list.
     *
     * @param suggestionListBackgroundColor the background color to be set
     */
    public void setSuggestionListBackgroundColor(@ColorInt int suggestionListBackgroundColor) {
        this.suggestionListBackgroundColor = suggestionListBackgroundColor;
        setCardBackgroundColor(suggestionListBackgroundColor);
    }

    /**
     * Returns the stroke color of the suggestion list.
     *
     * @return the stroke color of the suggestion list
     */
    public @ColorInt int getSuggestionListStrokeColor() {
        return suggestionListStrokeColor;
    }

    /**
     * Sets the stroke color of the suggestion list.
     *
     * @param suggestionListStrokeColor the stroke color to be set
     */
    public void setSuggestionListStrokeColor(@ColorInt int suggestionListStrokeColor) {
        this.suggestionListStrokeColor = suggestionListStrokeColor;
        setStrokeColor(suggestionListStrokeColor);
    }

    /**
     * Returns the stroke width of the suggestion list.
     *
     * @return the stroke width of the suggestion list
     */
    public @Dimension int getSuggestionListStrokeWidth() {
        return suggestionListStrokeWidth;
    }

    /**
     * Sets the stroke width of the suggestion list.
     *
     * @param suggestionListStrokeWidth the stroke width to be set
     */
    public void setSuggestionListStrokeWidth(@Dimension int suggestionListStrokeWidth) {
        this.suggestionListStrokeWidth = suggestionListStrokeWidth;
        setStrokeWidth(suggestionListStrokeWidth);
    }

    /**
     * Returns the corner radius of the suggestion list.
     *
     * @return the corner radius of the suggestion list
     */
    public @Dimension int getSuggestionListCornerRadius() {
        return suggestionListCornerRadius;
    }

    /**
     * Sets the corner radius of the suggestion list.
     *
     * @param suggestionListCornerRadius the corner radius to be set
     */
    public void setSuggestionListCornerRadius(@Dimension int suggestionListCornerRadius) {
        this.suggestionListCornerRadius = suggestionListCornerRadius;
        setRadius(suggestionListCornerRadius);
    }

    /**
     * Returns the avatar style of the suggestion list items.
     *
     * @return the avatar style resource of the suggestion list items
     */
    public @StyleRes int getSuggestionListItemAvatarStyle() {
        return suggestionListItemAvatarStyle;
    }

    /**
     * Sets the avatar style of the suggestion list items.
     *
     * @param suggestionListItemAvatarStyle the avatar style resource to be set
     */
    public void setSuggestionListItemAvatarStyle(@StyleRes int suggestionListItemAvatarStyle) {
        this.suggestionListItemAvatarStyle = suggestionListItemAvatarStyle;
        suggestionListAdapter.setSuggestionListItemAvatarStyle(suggestionListItemAvatarStyle);
    }

    /**
     * Returns the text appearance of the suggestion list items.
     *
     * @return the text appearance resource of the suggestion list items
     */
    public @StyleRes int getSuggestionListItemTextAppearance() {
        return suggestionListItemTextAppearance;
    }

    /**
     * Sets the text appearance of the suggestion list items.
     *
     * @param suggestionListItemTextAppearance the text appearance resource to be set
     */
    public void setSuggestionListItemTextAppearance(@StyleRes int suggestionListItemTextAppearance) {
        this.suggestionListItemTextAppearance = suggestionListItemTextAppearance;
        suggestionListAdapter.setSuggestionListItemTextAppearance(suggestionListItemTextAppearance);
    }

    /**
     * Returns the text color of the suggestion list items.
     *
     * @return the text color of the suggestion list items
     */
    public @ColorInt int getSuggestionListItemTextColor() {
        return suggestionListItemTextColor;
    }

    /**
     * Sets the text color of the suggestion list items.
     *
     * @param suggestionListItemTextColor the text color to be set
     */
    public void setSuggestionListItemTextColor(@ColorInt int suggestionListItemTextColor) {
        this.suggestionListItemTextColor = suggestionListItemTextColor;
        suggestionListAdapter.setSuggestionListItemTextColor(suggestionListItemTextColor);
    }

    /**
     * Sets the color resource ID for the suggestion list item info text.
     * @param suggestionListItemInfoTextColor the color resource ID to apply to the suggestion list item info text
     */
    public void setSuggestionListItemInfoTextColor(@ColorInt int suggestionListItemInfoTextColor) {
        this.suggestionListItemInfoTextColor = suggestionListItemInfoTextColor;
        suggestionListAdapter.setSuggestionListItemInfoTextColor(suggestionListItemInfoTextColor);
    }

    /**
     * Sets the style resource ID for the suggestion list item info text appearance.
     *
     * @param suggestionListItemInfoTextAppearance the style resource ID to apply to the suggestion list item info text appearance
     */
    public void setSuggestionListItemInfoTextAppearance(@StyleRes int suggestionListItemInfoTextAppearance) {
        this.suggestionListItemInfoTextAppearance = suggestionListItemInfoTextAppearance;
        suggestionListAdapter.setSuggestionListItemInfoTextAppearance(suggestionListItemInfoTextAppearance);
    }

    /**
     * Returns whether avatars are shown in the suggestion list.
     *
     * @return true if avatars are shown, false otherwise
     */
    public boolean showAvatar() {
        return showAvatar;
    }

    /**
     * Sets whether avatars should be shown in the suggestion list.
     *
     * @param showAvatar true to show avatars, false to hide them
     */
    public void showAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
        suggestionListAdapter.showAvatar(showAvatar);
    }

    /**
     * Sets the maximum height limit for the suggestion list.
     *
     * @param maxHeightLimit the maximum height limit in pixels
     */
    public void setMaxHeightLimit(@Dimension int maxHeightLimit) {
        if (maxHeightLimit > 0) {
            this.maxHeightLimit = maxHeightLimit;
        }
    }

    /**
     * Measures the dimensions of the suggestion list, enforcing the maximum height
     * limit if set.
     *
     * @param widthMeasureSpec  the width measurement specification
     * @param heightMeasureSpec the height measurement specification
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeightLimit != 0 && heightSize > maxHeightLimit) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightLimit, heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Sets the view holder listener for the suggestion list items.
     *
     * @param viewHolderListener the view holder listener to be set
     */
    public void setListItemView(SuggestionListViewHolderListener viewHolderListener) {
        suggestionListAdapter.setListItemView(viewHolderListener);
    }

    /**
     * Updates the suggestion list with a new list of suggestion items.
     *
     * @param suggestionItemList the new list of suggestion items
     */
    public void setList(List<SuggestionItem> suggestionItemList) {
        if (!suggestionItemList.isEmpty()) {
            showShimmer(false);
        }
        suggestionListAdapter.updateList(suggestionItemList);
    }

    /**
     * Shows or hides the shimmer effect on the suggestion list.
     *
     * @param visibility true to show shimmer, false to hide it
     */
    public void showShimmer(boolean visibility) {
        if (visibility) {
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(1, R.layout.shimmer_cometchat_suggestion_list_items);
            binding.shimmerRecyclerViewSuggestion.setAdapter(adapter);
            binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            binding.shimmerEffectFrame.startShimmer();
        } else {
            binding.shimmerEffectFrame.stopShimmer();
        }
        binding.recyclerViewSuggestionList.setVisibility(visibility ? GONE : VISIBLE);
        binding.shimmerEffectFrame.setVisibility(visibility ? VISIBLE : GONE);
    }

    /**
     * Sets the item click listener for the suggestion list items.
     *
     * @param onItemClickListener the item click listener to be set
     */
    public void setItemClickListener(OnItemClickListener<SuggestionItem> onItemClickListener) {
        if (onItemClickListener != null) {
            this.onItemClickListener = onItemClickListener;
        }
    }

    /**
     * Returns the binding object for the suggestion list.
     *
     * @return the binding object for the suggestion list
     */
    public CometchatSuggestionListBinding getBinding() {
        return binding;
    }
}
