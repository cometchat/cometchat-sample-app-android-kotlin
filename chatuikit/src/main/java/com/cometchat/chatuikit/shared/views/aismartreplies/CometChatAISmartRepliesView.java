package com.cometchat.chatuikit.shared.views.aismartreplies;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAiSmartRepliesBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Custom view for displaying AI-generated smart replies in a chat application.
 * This view extends {@link MaterialCardView} to provide a styled card interface
 * for displaying a list of replies, along with error and loading states.
 *
 * <p>
 * The view manages its own layout, including a RecyclerView for replies, a
 * close icon for dismissing the view, and various customizable attributes such
 * as background color, corner radius, and text appearance.
 *
 * <p>
 * Users can set up click listeners for reply items and the close icon to handle
 * user interactions.
 *
 * <p>
 * This view supports dynamic updates, allowing the display of replies, loading
 * states, and error messages based on the state of the chat.
 */
public class CometChatAISmartRepliesView extends MaterialCardView {
    private static final String TAG = CometChatAISmartRepliesView.class.getSimpleName();

    /**
     * Binding instance for {@link CometchatAiSmartRepliesBinding} used to manage
     * view bindings.
     */
    private CometchatAiSmartRepliesBinding binding;

    /**
     * User ID or group ID associated with the component.
     */
    private String uid;

    /**
     * Maximum height of the component in pixels.
     */
    private @Dimension int maxHeight = 0;

    /**
     * Listener for handling click events on the component.
     */
    private OnClick onClick;

    /**
     * Listener for handling close icon click events.
     */
    private OnCLoseIconClick onCloseIconClick;

    /**
     * Adapter instance for managing smart replies in the component.
     */
    private AISmartRepliesAdapter AISmartRepliesAdapter;

    /**
     * Background color of the component.
     */
    private @ColorInt int backgroundColor;

    /**
     * Background drawable for the component.
     */
    private Drawable backgroundDrawable;

    /**
     * Corner radius of the component in pixels.
     */
    private @Dimension int cornerRadius;

    /**
     * Stroke width of the component's border in pixels.
     */
    private @Dimension int strokeWidth;

    /**
     * Color of the component's stroke.
     */
    private @ColorInt int strokeColor;

    /**
     * Text color for the title in the component.
     */
    private @ColorInt int titleTextColor;

    /**
     * Text appearance resource ID for styling the title text.
     */
    private @StyleRes int titleTextAppearance;

    /**
     * Drawable resource for the close icon.
     */
    private Drawable closeIcon;

    /**
     * Tint color applied to the close icon.
     */
    private @ColorInt int closeIconTint;

    /**
     * Background color for each item in the smart replies list.
     */
    private @ColorInt int itemBackgroundColor;

    /**
     * Background drawable for each item in the smart replies list.
     */
    private Drawable itemBackgroundDrawable;

    /**
     * Corner radius for each item in the smart replies list in pixels.
     */
    private @Dimension int itemCornerRadius;

    /**
     * Stroke width for each item in the smart replies list in pixels.
     */
    private @Dimension int itemStrokeWidth;

    /**
     * Stroke color for each item in the smart replies list.
     */
    private @ColorInt int itemStrokeColor;

    /**
     * Text color for each item in the smart replies list.
     */
    private @ColorInt int itemTextColor;

    /**
     * Text appearance resource ID for styling the text of each item.
     */
    private @StyleRes int itemTextAppearance;

    /**
     * Text color for error state messages.
     */
    private @ColorInt int errorStateTextColor;

    /**
     * Text appearance resource ID for styling error state messages.
     */
    private @StyleRes int errorStateTextAppearance;

    /**
     * Style resource ID for customizing the component's appearance.
     */
    private @StyleRes int style;

    /**
     * Layout resource ID for the error view.
     */
    private @LayoutRes int errorViewLayout;

    /**
     * Layout resource ID for the loading view.
     */
    private @LayoutRes int loadingViewLayout;

    /**
     * Constructs a new instance of {@link CometChatAISmartRepliesView} with the
     * specified context.
     *
     * @param context The context associated with the view.
     */
    public CometChatAISmartRepliesView(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new instance of {@link CometChatAISmartRepliesView} with the
     * specified context and attributes.
     *
     * @param context The context associated with the view.
     * @param attrs   The attribute set containing view attributes from XML.
     */
    public CometChatAISmartRepliesView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAISmartRepliesStyle);
    }

    /**
     * Constructs a new instance of {@link CometChatAISmartRepliesView} with the
     * specified context, attributes, and default style attribute.
     *
     * @param context      The context associated with the view.
     * @param attrs        The attribute set containing view attributes from XML.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource for default values.
     */
    public CometChatAISmartRepliesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view with provided attributes and default styling. Sets up
     * the {@link CometchatAiSmartRepliesBinding}, configures the RecyclerView
     * adapter, and adds listeners for item click and close icon click events.
     *
     * @param attrs        The attribute set containing XML attributes to be applied to the
     *                     view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource with default values.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatAiSmartRepliesBinding.inflate(LayoutInflater.from(getContext()), this, true);
        AISmartRepliesAdapter = new AISmartRepliesAdapter();
        binding.recyclerView.setAdapter(AISmartRepliesAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String reply = (String) view.getTag(R.string.cometchat_reply_lowercase);
                if (onClick != null) onClick.onClick(view, uid, reply, position);
            }
        }));
        binding.ivClose.setOnClickListener(view -> {
            if (onCloseIconClick != null) onCloseIconClick.onClick(view);
        });
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAISmartReplies, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAISmartReplies, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesTitleTextAppearance, 0));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesTitleTextColor,
                                                  CometChatTheme.getTextColorPrimary(getContext())));
            setCloseIcon(typedArray.getDrawable(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCloseIconDrawable));
            setCloseIconTint(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCloseIconTint,
                                                 CometChatTheme.getIconTintPrimary(getContext())));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesBackgroundColor,
                                                   CometChatTheme.getBackgroundColor1(getContext())));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesBackgroundDrawable));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesStrokeColor, 0));
            setItemBackgroundColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemBackgroundColor,
                                                       CometChatTheme.getBackgroundColor1(getContext())));
            setItemBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemBackgroundDrawable));
            setItemCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemCornerRadius, 0));
            setItemStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemStrokeWidth, 0));
            setItemStrokeColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemStrokeColor,
                                                   CometChatTheme.getStrokeColorLight(getContext())));
            setItemTextAppearance(typedArray.getResourceId(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemTextAppearance, 0));
            setItemTextColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesItemTextColor,
                                                 CometChatTheme.getTextColorPrimary(getContext())));
            setErrorStateTextAppearance(typedArray.getResourceId(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesErrorStateTextAppearance,
                                                                 0));
            setErrorStateTextColor(typedArray.getColor(R.styleable.CometChatAISmartReplies_cometchatAISmartRepliesErrorStateTextColor,
                                                       CometChatTheme.getTextColorSecondary(getContext())));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets a custom view in place of the default loaded view. Replaces the content
     * of {@code
     * loadedViewParentLayout} with the provided custom view.
     *
     * @param view The custom view to set in the layout.
     */
    public void setCustomView(@NonNull View view) {
        binding.loadedViewParentLayout.removeAllViews();
        binding.loadingViewLayout.setVisibility(GONE);
        binding.loadedViewParentLayout.addView(view);
    }

    /**
     * Measures the view with a maximum height constraint if set. If
     * {@code maxHeight} is greater than zero, the height will not exceed this
     * value.
     *
     * @param widthMeasureSpec  Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight == 0) super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        else {
            int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
            if (measuredHeight > maxHeight) {
                int measureMode = MeasureSpec.getMode(heightMeasureSpec);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Sets the list of replies to display in the smart replies view.
     *
     * @param replies List of replies to display.
     */
    public void setReplyList(@NonNull List<String> replies) {
        if (!replies.isEmpty()) {
            binding.recyclerView.setVisibility(VISIBLE);
            AISmartRepliesAdapter.setList(replies);
            stopShimmer();
            binding.loadingViewLayout.setVisibility(GONE);
            binding.errorViewLayout.setVisibility(GONE);
        }
    }

    /**
     * Stops the shimmer effect.
     */
    private void stopShimmer() {
        if (loadingViewLayout == 0) binding.shimmerEffectFrame.stopShimmer();
    }

    /**
     * Sets a click listener for the close icon.
     *
     * @param onCLoseIconClick Callback to invoke when the close icon is clicked.
     */
    public void setOnCLoseIconClick(@NonNull OnCLoseIconClick onCLoseIconClick) {
        this.onCloseIconClick = onCLoseIconClick;
    }

    /**
     * Displays the error view and hides other views.
     */
    public void showErrorView() {
        binding.errorViewLayout.setVisibility(VISIBLE);
        binding.loadingViewLayout.setVisibility(GONE);
        binding.recyclerView.setVisibility(GONE);
        stopShimmer();
    }

    /**
     * Displays the loading view and starts the shimmer effect.
     */
    public void showLoadingView() {
        binding.errorViewLayout.setVisibility(GONE);
        binding.loadingViewLayout.setVisibility(VISIBLE);
        setShimmerVisibility(VISIBLE);
        binding.recyclerView.setVisibility(GONE);
    }

    /**
     * Sets the visibility of the shimmer effect for the loading state.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setShimmerVisibility(int visibility) {
        if (loadingViewLayout == 0) {
            if (visibility == View.GONE) {
                binding.shimmerEffectFrame.stopShimmer();
            } else {
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(1, R.layout.cometchat_ai_conversation_starter_shimmer);
                binding.shimmerRecyclerview.setAdapter(adapter);
                binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerEffectFrame.startShimmer();
            }
            binding.shimmerParentLayout.setVisibility(visibility);
        }
    }

    /**
     * Sets the error text to display in the error state.
     *
     * @param errorText Text to display in the error view.
     */
    public void setErrorStateText(@NonNull String errorText) {
        if (!errorText.isEmpty()) binding.tvError.setText(errorText);
    }

    /**
     * Returns the binding for this view.
     *
     * @return CometchatAiSmartRepliesBinding instance associated with this view.
     */
    public CometchatAiSmartRepliesBinding getBinding() {
        return binding;
    }

    /**
     * Returns the maximum height for the view in pixels.
     *
     * @return Max height in pixels.
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Sets the maximum height for this view in DP.
     *
     * @param maxHeightInDp The maximum height in DP.
     */
    public void setMaxHeight(int maxHeightInDp) {
        if (maxHeightInDp > 0) this.maxHeight = Utils.convertDpToPx(getContext(), maxHeightInDp);
    }

    /**
     * Returns the click listener for reply items.
     *
     * @return OnClick instance for reply item clicks.
     */
    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets a click listener for reply items.
     *
     * @param onClick Callback to invoke when a reply item is clicked.
     */
    public void setOnClick(@NonNull OnClick onClick) {
        this.onClick = onClick;
    }

    /**
     * Returns the adapter for the AI Smart Replies view.
     *
     * @return AISmartRepliesAdapter instance used by the view.
     */
    public AISmartRepliesAdapter getAISmartRepliesAdapter() {
        return AISmartRepliesAdapter;
    }

    /**
     * Returns the background color for the view.
     *
     * @return Background color as an integer.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }    /**
     * Sets the stroke width for the view.
     *
     * @param strokeWidth Dimension integer for the stroke width in DP.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Sets the background color for the view.
     *
     * @param backgroundColor Color integer for the background.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Returns the background drawable for the view.
     *
     * @return Background drawable.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable for the view.
     *
     * @param backgroundDrawable Drawable for the background.
     */
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Returns the corner radius for the view.
     *
     * @return Corner radius in pixels.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the view.
     *
     * @param cornerRadius Dimension integer for the corner radius in DP.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }    /**
     * Sets the stroke color for the view.
     *
     * @param strokeColor Color integer for the stroke color.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Returns the text color of the title in the view.
     *
     * @return Title text color as an integer.
     */
    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the color of the title text.
     *
     * @param titleTextColor Color integer for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.title.setTextColor(titleTextColor);
    }

    /**
     * Returns the appearance style of the title text.
     *
     * @return Title text appearance style resource ID.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the appearance style for the title text.
     *
     * @param titleTextAppearance Style resource for the title text appearance.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.title.setTextAppearance(titleTextAppearance);
    }

    /**
     * Returns the close icon drawable for the view.
     *
     * @return Close icon drawable.
     */
    public Drawable getCloseIcon() {
        return closeIcon;
    }

    /**
     * Sets the close icon drawable.
     *
     * @param closeIcon Drawable for the close icon.
     */
    public void setCloseIcon(Drawable closeIcon) {
        this.closeIcon = closeIcon;
        binding.ivClose.setImageDrawable(closeIcon);
    }

    /**
     * Returns the tint color for the close icon.
     *
     * @return Close icon tint color as an integer.
     */
    public int getCloseIconTint() {
        return closeIconTint;
    }

    /**
     * Sets the tint color for the close icon.
     *
     * @param closeIconTint Color integer for the close icon tint.
     */
    public void setCloseIconTint(@ColorInt int closeIconTint) {
        this.closeIconTint = closeIconTint;
        binding.ivClose.setColorFilter(closeIconTint);
    }

    /**
     * Returns the background color for reply items.
     *
     * @return Item background color as an integer.
     */
    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the background color for each item in the smart replies.
     *
     * @param itemBackgroundColor Color integer for item background.
     */
    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        AISmartRepliesAdapter.setItemBackgroundColor(itemBackgroundColor);
    }

    /**
     * Returns the background drawable for reply items.
     *
     * @return Background drawable for items.
     */
    public Drawable getItemBackgroundDrawable() {
        return itemBackgroundDrawable;
    }

    /**
     * Sets the background drawable for each item in the smart replies.
     *
     * @param itemBackgroundDrawable Drawable for the item background.
     */
    public void setItemBackgroundDrawable(Drawable itemBackgroundDrawable) {
        this.itemBackgroundDrawable = itemBackgroundDrawable;
        AISmartRepliesAdapter.setItemBackgroundDrawable(itemBackgroundDrawable);
    }

    /**
     * Returns the corner radius for reply items.
     *
     * @return Item corner radius in pixels.
     */
    public int getItemCornerRadius() {
        return itemCornerRadius;
    }

    /**
     * Sets the corner radius for each item in the smart replies.
     *
     * @param itemCornerRadius Dimension integer for item corner radius in DP.
     */
    public void setItemCornerRadius(@Dimension int itemCornerRadius) {
        this.itemCornerRadius = itemCornerRadius;
        AISmartRepliesAdapter.setItemCornerRadius(itemCornerRadius);
    }

    /**
     * Returns the stroke width for reply items.
     *
     * @return Item stroke width in pixels.
     */
    public int getItemStrokeWidth() {
        return itemStrokeWidth;
    }

    /**
     * Sets the stroke width for each item in the smart replies.
     *
     * @param itemStrokeWidth Dimension integer for item stroke width in DP.
     */
    public void setItemStrokeWidth(@Dimension int itemStrokeWidth) {
        this.itemStrokeWidth = itemStrokeWidth;
        AISmartRepliesAdapter.setItemStrokeWidth(itemStrokeWidth);
    }

    /**
     * Returns the stroke color for reply items.
     *
     * @return Item stroke color as an integer.
     */
    public int getItemStrokeColor() {
        return itemStrokeColor;
    }

    /**
     * Sets the stroke color for each item in the smart replies.
     *
     * @param itemStrokeColor Color integer for item stroke color.
     */
    public void setItemStrokeColor(@ColorInt int itemStrokeColor) {
        this.itemStrokeColor = itemStrokeColor;
        AISmartRepliesAdapter.setItemStrokeColor(itemStrokeColor);
    }

    /**
     * Returns the text color for reply items.
     *
     * @return Item text color as an integer.
     */
    public int getItemTextColor() {
        return itemTextColor;
    }

    /**
     * Sets the text color for each item in the smart replies.
     *
     * @param itemTextColor Color integer for item text color.
     */
    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
        AISmartRepliesAdapter.setItemTextColor(itemTextColor);
    }

    /**
     * Returns the text appearance style for reply items.
     *
     * @return Item text appearance style resource ID.
     */
    public int getItemTextAppearance() {
        return itemTextAppearance;
    }

    /**
     * Sets the appearance style for each item's text in the smart replies.
     *
     * @param itemTextAppearance Style resource for item text appearance.
     */
    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
        AISmartRepliesAdapter.setItemTextAppearance(itemTextAppearance);
    }

    /**
     * Returns the error state text color.
     *
     * @return Error state text color as an integer.
     */
    public int getErrorStateTextColor() {
        return errorStateTextColor;
    }

    /**
     * Sets the text color for the error state.
     *
     * @param color Color integer for the error text.
     */
    public void setErrorStateTextColor(@ColorInt int color) {
        this.errorStateTextColor = color;
        binding.tvError.setTextColor(color);
    }

    /**
     * Returns the error state text appearance style.
     *
     * @return Error state text appearance style resource ID.
     */
    public int getErrorStateTextAppearance() {
        return errorStateTextAppearance;
    }

    /**
     * Sets the appearance style for the error state text.
     *
     * @param errorStateTextAppearance Style resource for the error state text appearance.
     */
    public void setErrorStateTextAppearance(@StyleRes int errorStateTextAppearance) {
        this.errorStateTextAppearance = errorStateTextAppearance;
        binding.tvError.setTextAppearance(errorStateTextAppearance);
    }

    /**
     * Returns the general style resource ID for the view.
     *
     * @return Style resource ID.
     */
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the Ai smart replies from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAISmartReplies);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the layout resource ID for the error view.
     *
     * @return Error view layout resource ID.
     */
    public int getErrorViewLayout() {
        return errorViewLayout;
    }

    /**
     * Sets a custom layout for the error view.
     *
     * @param errorViewLayout Layout resource for the error view.
     */
    public void setErrorViewLayout(@LayoutRes int errorViewLayout) {
        if (errorViewLayout != 0) {
            this.errorViewLayout = errorViewLayout;
            binding.errorViewLayout.removeAllViews();
            View view = View.inflate(getContext(), errorViewLayout, null);
            binding.errorViewLayout.addView(view);
        }
    }

    /**
     * Returns the layout resource ID for the loading view.
     *
     * @return Loading view layout resource ID.
     */
    public int getLoadingViewLayout() {
        return loadingViewLayout;
    }    /**
     * Returns the stroke width of the view.
     *
     * @return Stroke width in pixels.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets a custom layout for the loading view.
     *
     * @param loadingViewLayout Layout resource for the loading view.
     */
    public void setLoadingViewLayout(@LayoutRes int loadingViewLayout) {
        if (loadingViewLayout != 0) {
            this.loadingViewLayout = loadingViewLayout;
            binding.loadingViewLayout.removeAllViews();
            View view = View.inflate(getContext(), loadingViewLayout, null);
            binding.loadingViewLayout.addView(view);
        }
    }

    /**
     * Returns the user ID or group ID associated with the component.
     *
     * @return The user ID or group ID.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the user ID or group ID associated with the component.
     *
     * @param uid The user ID or group ID.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Interface for handling click events on reply items.
     */
    public interface OnClick {
        /**
         * Called when a reply item is clicked.
         *
         * @param view     The clicked view.
         * @param reply    The text of the clicked reply.
         * @param position The position of the clicked item.
         */
        void onClick(View view, String id, String reply, int position);
    }

    /**
     * Interface for handling click events on the close icon.
     */
    public interface OnCLoseIconClick {
        /**
         * Called when the close icon is clicked.
         *
         * @param view The clicked close icon view.
         */
        void onClick(View view);
    }    /**
     * Returns the stroke color of the view.
     *
     * @return Stroke color as an integer.
     */
    @Override
    public int getStrokeColor() {
        return strokeColor;
    }










}
