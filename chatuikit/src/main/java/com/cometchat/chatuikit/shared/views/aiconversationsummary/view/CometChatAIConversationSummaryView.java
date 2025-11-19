package com.cometchat.chatuikit.shared.views.aiconversationsummary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAiConversationSummaryBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;

/**
 * Custom view for displaying an AI conversation summary using a
 * MaterialCardView.
 *
 * <p>
 * This class extends {@link MaterialCardView} to provide a specialized view for
 * showing a summary of AI conversations. It includes various features such as
 * customizable item appearance, loading and error states, and click listeners
 * for user interactions.
 *
 * <p>
 * Usage of this view includes setting summary data, customizing visual
 * attributes, and handling click events for both the conversation items and the
 * close icon.
 *
 * <p>
 * To use this view in your layout XML, include the following:
 *
 * <pre>
 * &lt;your.package.name.CometChatAIConversationSummaryView
 *     android:id="@+id/ai_conversation_summary_view"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * </pre>
 *
 * <p>
 * This view supports customization through XML attributes and method calls,
 * allowing for flexible integration into different UI designs.
 */
public class CometChatAIConversationSummaryView extends MaterialCardView {
    private static final String TAG = CometChatAIConversationSummaryView.class.getSimpleName();


    private CometchatAiConversationSummaryBinding binding;

    /**
     * Maximum height of the view, can be set to limit its size.
     */
    private int maxHeight = 0;

    /**
     * Click listener for reply items.
     */
    private OnClick onClick;

    /**
     * Click listener for the close icon.
     */
    private OnCloseIconClick onCloseClick;

    /**
     * Adapter for managing the conversation summary items in the RecyclerView.
     */
    private AIConversationSummaryAdapter AIConversationSummaryAdapter;

    /**
     * Background color of the view.
     */
    private @ColorInt int backgroundColor;

    /**
     * Background drawable of the view.
     */
    private Drawable backgroundDrawable;

    /**
     * Corner radius for the view's background.
     */
    private @Dimension int cornerRadius;

    /**
     * Width of the stroke for the view's border.
     */
    private @Dimension int strokeWidth;

    /**
     * Color of the stroke for the view's border.
     */
    private @ColorInt int strokeColor;

    /**
     * Text color for the title of the view.
     */
    private @ColorInt int titleTextColor;

    /**
     * Text appearance resource for the title.
     */
    private @StyleRes int titleTextAppearance;

    /**
     * Drawable for the close icon.
     */
    private Drawable closeIcon;

    /**
     * Tint color for the close icon.
     */
    private @ColorInt int closeIconTint;

    /**
     * Background color for individual items in the summary.
     */
    private @ColorInt int itemBackgroundColor;

    /**
     * Background drawable for individual items in the summary.
     */
    private Drawable itemBackgroundDrawable;

    /**
     * Corner radius for individual item backgrounds.
     */
    private @Dimension int itemCornerRadius;

    /**
     * Width of the stroke for individual item backgrounds.
     */
    private @Dimension int itemStrokeWidth;

    /**
     * Color of the stroke for individual item backgrounds.
     */
    private @ColorInt int itemStrokeColor;

    /**
     * Text color for individual items in the summary.
     */
    private @ColorInt int itemTextColor;

    /**
     * Text appearance resource for individual items in the summary.
     */
    private @StyleRes int itemTextAppearance;

    /**
     * Text color for error state messages.
     */
    private @ColorInt int errorStateTextColor;

    /**
     * Text appearance resource for error state messages.
     */
    private @StyleRes int errorStateTextAppearance;

    /**
     * Style resource for the overall view.
     */
    private @StyleRes int style;

    /**
     * Layout resource for the error view.
     */
    private @LayoutRes int errorViewLayout;

    /**
     * Layout resource for the loading view.
     */
    private @LayoutRes int loadingViewLayout;

    /**
     * Constructor for creating the view programmatically.
     *
     * @param context The context in which the view is being created.
     */
    public CometChatAIConversationSummaryView(Context context) {
        this(context, null);
    }

    /**
     * Constructor for inflating the view from XML with attributes.
     *
     * @param context The context in which the view is being created.
     * @param attrs   The attribute set containing custom attributes.
     */
    public CometChatAIConversationSummaryView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAIConversationSummaryStyle);
    }

    /**
     * Constructor for inflating the view from XML with attributes and a default
     * style.
     *
     * @param context      The context in which the view is being created.
     * @param attrs        The attribute set containing custom attributes.
     * @param defStyleAttr The default style attribute to apply.
     */
    public CometChatAIConversationSummaryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view by inflating the layout, setting up the RecyclerView,
     * and applying style attributes from XML.
     *
     * @param attrs        The attribute set containing customization for this view.
     * @param defStyleAttr The default style attribute to apply to this view.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatAiConversationSummaryBinding.inflate(LayoutInflater.from(getContext()), this, true);
        AIConversationSummaryAdapter = new AIConversationSummaryAdapter();
        binding.recyclerView.setAdapter(AIConversationSummaryAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String reply = (String) view.getTag(R.string.cometchat_reply_lowercase);
                if (onClick != null) onClick.onClick(view, reply, position);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        binding.ivClose.setOnClickListener(view -> {
            if (onCloseClick != null) onCloseClick.onClick(view);
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
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.CometChatAIConversationSummary,
            defStyleAttr,
            defStyleRes
        );
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIConversationSummary, defStyleAttr, style);
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
            setTitleTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextAppearance,
                0
            ));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextColor,
                                                  CometChatTheme.getTextColorPrimary(getContext())));
            setCloseIcon(typedArray.getDrawable(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconDrawable));
            setCloseIconTint(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconTint,
                                                 CometChatTheme.getIconTintPrimary(getContext())));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundColor,
                                                   CometChatTheme.getBackgroundColor1(getContext())));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundDrawable));
            setCornerRadius(typedArray.getDimensionPixelSize(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCornerRadius,
                0
            ));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeColor, 0));
            setItemBackgroundColor(typedArray.getColor(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundColor,
                0
            ));
            setItemBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundDrawable));
            setItemCornerRadius(typedArray.getDimensionPixelSize(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemCornerRadius,
                0
            ));
            setItemStrokeWidth(typedArray.getDimensionPixelSize(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeWidth,
                0
            ));
            setItemStrokeColor(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeColor, 0));
            setItemTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextAppearance,
                0
            ));
            setItemTextColor(typedArray.getColor(R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextColor,
                                                 CometChatTheme.getTextColorPrimary(getContext())));
            setErrorStateTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextAppearance,
                0
            ));
            setErrorStateTextColor(typedArray.getColor(
                R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextColor,
                CometChatTheme.getTextColorSecondary(getContext())
            ));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets a custom view to be displayed in the loaded view parent layout.
     *
     * @param view The custom view to be added. If null, no action is taken.
     */
    public void setCustomView(View view) {
        if (view != null) {
            binding.loadedViewParentLayout.removeAllViews();
            binding.loadingViewLayout.setVisibility(GONE);
            binding.loadedViewParentLayout.addView(view);
        }
    }

    /**
     * Measures the view's dimensions, respecting the max height if specified.
     *
     * @param widthMeasureSpec  The width requirements as determined by the parent.
     * @param heightMeasureSpec The height requirements as determined by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
            if (measuredHeight > maxHeight) {
                int measureMode = MeasureSpec.getMode(heightMeasureSpec);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Sets the summary text to be displayed in the recycler view.
     *
     * @param summary The summary text to display. If null or empty, no action is taken.
     */
    public void setSummary(String summary) {
        if (summary != null && !summary.isEmpty()) {
            binding.recyclerView.setVisibility(VISIBLE);
            AIConversationSummaryAdapter.setList(Collections.singletonList(summary));
            stopShimmer();
            binding.loadingViewLayout.setVisibility(GONE);
            binding.errorViewLayout.setVisibility(GONE);
        }
    }

    /**
     * Stops the shimmer effect if it is currently running.
     */
    private void stopShimmer() {
        if (loadingViewLayout == 0) {
            binding.shimmerEffectFrame.stopShimmer();
        }
    }

    /**
     * Sets the click listener for the close icon.
     *
     * @param onCloseClick The click listener to set for the close icon. If null, no action
     *                         is taken.
     */
    public void setOnCloseClick(OnCloseIconClick onCloseClick) {
        if (onCloseClick != null) this.onCloseClick = onCloseClick;
    }

    /**
     * Displays the error view, hiding other views.
     */
    public void showErrorView() {
        binding.errorViewLayout.setVisibility(VISIBLE);
        binding.loadingViewLayout.setVisibility(GONE);
        binding.recyclerView.setVisibility(GONE);
        stopShimmer();
    }

    /**
     * Displays the loading view, hiding other views.
     */
    public void showLoadingView() {
        binding.errorViewLayout.setVisibility(GONE);
        binding.loadingViewLayout.setVisibility(VISIBLE);
        setShimmerVisibility(VISIBLE);
        binding.recyclerView.setVisibility(GONE);
    }

    /**
     * Sets the visibility of the shimmer effect, which is used to display a loading
     * state.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setShimmerVisibility(int visibility) {
        if (loadingViewLayout == 0) {
            if (visibility == View.GONE) {
                binding.shimmerEffectFrame.stopShimmer();
            } else {
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(4, R.layout.cometchat_ai_conversation_summary_shimmer);
                binding.shimmerRecyclerview.setAdapter(adapter);
                binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerEffectFrame.startShimmer();
            }
            binding.shimmerParentLayout.setVisibility(visibility);
        }
    }

    /**
     * Sets the error state text to be displayed.
     *
     * @param errorText The error text to display. If null or empty, no action is taken.
     */
    public void setErrorStateText(String errorText) {
        if (errorText != null && !errorText.isEmpty()) binding.tvError.setText(errorText);
    }

    /**
     * Gets the binding for this view.
     *
     * @return The binding object for this view.
     */
    public CometchatAiConversationSummaryBinding getBinding() {
        return binding;
    }

    /**
     * Gets the maximum height set for this view.
     *
     * @return The maximum height in pixels.
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Sets the maximum height for the view in dp.
     *
     * @param maxHeightInDp The maximum height in density-independent pixels.
     */
    public void setMaxHeight(int maxHeightInDp) {
        if (maxHeightInDp > 0) {
            this.maxHeight = Utils.convertDpToPx(getContext(), maxHeightInDp);
        }
    }

    /**
     * Gets the click listener for the main view.
     *
     * @return The OnClick listener instance, or null if not set.
     */
    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the click listener for the main view.
     *
     * @param onClick The click listener to set. If null, no action is taken.
     */
    public void setOnClick(OnClick onClick) {
        if (onClick != null) this.onClick = onClick;
    }

    /**
     * Gets the adapter for the AI conversation summary.
     *
     * @return The AIConversationSummaryAdapter instance.
     */
    public AIConversationSummaryAdapter getAIConversationSummaryAdapter() {
        return AIConversationSummaryAdapter;
    }

    /**
     * Gets the background color of this view.
     *
     * @return The background color in ARGB format.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }    /**
     * Sets the stroke width for the view.
     *
     * @param strokeWidth The stroke width in pixels.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Sets the background color of the view.
     *
     * @param backgroundColor The color to set as the background color.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Gets the background drawable of this view.
     *
     * @return The background drawable, or null if not set.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable for the view.
     *
     * @param backgroundDrawable The drawable to set as the background.
     */
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Gets the corner radius of this view.
     *
     * @return The corner radius in pixels.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the view.
     *
     * @param cornerRadius The corner radius in pixels.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }    /**
     * Sets the stroke color for the view.
     *
     * @param strokeColor The color to set as the stroke color.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Gets the text color for the title.
     *
     * @return The title text color in ARGB format.
     */
    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title.
     *
     * @param titleTextColor The color to set for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.title.setTextColor(titleTextColor);
    }

    /**
     * Gets the text appearance resource ID for the title.
     *
     * @return The title text appearance resource ID.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance for the title.
     *
     * @param titleTextAppearance The resource ID of the text appearance style.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.title.setTextAppearance(titleTextAppearance);
    }

    /**
     * Gets the drawable for the close icon.
     *
     * @return The close icon drawable, or null if not set.
     */
    public Drawable getCloseIcon() {
        return closeIcon;
    }

    /**
     * Sets the close icon drawable.
     *
     * @param closeIcon The drawable to set as the close icon.
     */
    public void setCloseIcon(Drawable closeIcon) {
        this.closeIcon = closeIcon;
        binding.ivClose.setImageDrawable(closeIcon);
    }

    /**
     * Gets the tint color for the close icon.
     *
     * @return The close icon tint color in ARGB format.
     */
    public int getCloseIconTint() {
        return closeIconTint;
    }

    /**
     * Sets the tint color for the close icon.
     *
     * @param closeIconTint The tint color to apply to the close icon.
     */
    public void setCloseIconTint(@ColorInt int closeIconTint) {
        this.closeIconTint = closeIconTint;
        binding.ivClose.setColorFilter(closeIconTint);
    }

    /**
     * Gets the background color for items in the summary.
     *
     * @return The item background color in ARGB format.
     */
    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the background color for items in the adapter.
     *
     * @param itemBackgroundColor The color to set for item backgrounds.
     */
    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        AIConversationSummaryAdapter.setItemBackgroundColor(itemBackgroundColor);
    }

    /**
     * Gets the background drawable for items in the summary.
     *
     * @return The item background drawable, or null if not set.
     */
    public Drawable getItemBackgroundDrawable() {
        return itemBackgroundDrawable;
    }

    /**
     * Sets the background drawable for items in the adapter.
     *
     * @param itemBackgroundDrawable The drawable to set for item backgrounds.
     */
    public void setItemBackgroundDrawable(Drawable itemBackgroundDrawable) {
        this.itemBackgroundDrawable = itemBackgroundDrawable;
        AIConversationSummaryAdapter.setItemBackgroundDrawable(itemBackgroundDrawable);
    }

    /**
     * Gets the corner radius for items in the summary.
     *
     * @return The item corner radius in pixels.
     */
    public int getItemCornerRadius() {
        return itemCornerRadius;
    }

    /**
     * Sets the corner radius for items in the adapter.
     *
     * @param itemCornerRadius The corner radius in pixels.
     */
    public void setItemCornerRadius(@Dimension int itemCornerRadius) {
        this.itemCornerRadius = itemCornerRadius;
        AIConversationSummaryAdapter.setItemCornerRadius(itemCornerRadius);
    }

    /**
     * Gets the stroke width for items in the summary.
     *
     * @return The item stroke width in pixels.
     */
    public int getItemStrokeWidth() {
        return itemStrokeWidth;
    }

    /**
     * Sets the stroke width for items in the adapter.
     *
     * @param itemStrokeWidth The stroke width in pixels.
     */
    public void setItemStrokeWidth(@Dimension int itemStrokeWidth) {
        this.itemStrokeWidth = itemStrokeWidth;
        AIConversationSummaryAdapter.setItemStrokeWidth(itemStrokeWidth);
    }

    /**
     * Gets the stroke color for items in the summary.
     *
     * @return The item stroke color in ARGB format.
     */
    public int getItemStrokeColor() {
        return itemStrokeColor;
    }

    /**
     * Sets the stroke color for items in the adapter.
     *
     * @param itemStrokeColor The color to set as the stroke color for items.
     */
    public void setItemStrokeColor(@ColorInt int itemStrokeColor) {
        this.itemStrokeColor = itemStrokeColor;
        AIConversationSummaryAdapter.setItemStrokeColor(itemStrokeColor);
    }

    /**
     * Gets the text color for items in the summary.
     *
     * @return The item text color in ARGB format.
     */
    public int getItemTextColor() {
        return itemTextColor;
    }

    /**
     * Sets the text color for items in the adapter.
     *
     * @param itemTextColor The color to set for item text.
     */
    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
        AIConversationSummaryAdapter.setItemTextColor(itemTextColor);
    }

    /**
     * Gets the text appearance resource ID for items in the summary.
     *
     * @return The item text appearance resource ID.
     */
    public int getItemTextAppearance() {
        return itemTextAppearance;
    }

    /**
     * Sets the text appearance for items in the adapter.
     *
     * @param itemTextAppearance The resource ID of the text appearance style.
     */
    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
        AIConversationSummaryAdapter.setItemTextAppearance(itemTextAppearance);
    }

    /**
     * Gets the text color for the error state.
     *
     * @return The error state text color in ARGB format.
     */
    public int getErrorStateTextColor() {
        return errorStateTextColor;
    }

    /**
     * Sets the text color for the error state.
     *
     * @param color The color to set for the error state text.
     */
    public void setErrorStateTextColor(@ColorInt int color) {
        this.errorStateTextColor = color;
        binding.tvError.setTextColor(color);
    }

    /**
     * Gets the text appearance resource ID for the error state.
     *
     * @return The error state text appearance resource ID.
     */
    public int getErrorStateTextAppearance() {
        return errorStateTextAppearance;
    }

    /**
     * Sets the text appearance for the error state.
     *
     * @param errorStateTextAppearance The resource ID of the text appearance style for the error state.
     */
    public void setErrorStateTextAppearance(@StyleRes int errorStateTextAppearance) {
        this.errorStateTextAppearance = errorStateTextAppearance;
        binding.tvError.setTextAppearance(errorStateTextAppearance);
    }

    /**
     * Gets the style resource ID for this view.
     *
     * @return The style resource ID.
     */
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the AI conversation summary from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAIConversationSummary);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the layout resource ID for the error view.
     *
     * @return The error view layout resource ID.
     */
    public int getErrorViewLayout() {
        return errorViewLayout;
    }

    /**
     * Sets the layout resource for the error view.
     *
     * @param errorViewLayout The layout resource ID to set for the error view.
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
     * Gets the layout resource ID for the loading view.
     *
     * @return The loading view layout resource ID.
     */
    public int getLoadingViewLayout() {
        return loadingViewLayout;
    }    /**
     * Gets the stroke width of this view.
     *
     * @return The stroke width in pixels.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the layout resource for the loading view.
     *
     * @param loadingViewLayout The layout resource ID to set for the loading view.
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
     * Interface for handling click events on the main view.
     */
    public interface OnClick {
        /**
         * Called when the view is clicked.
         *
         * @param view     The view that was clicked.
         * @param reply    The reply string associated with the click.
         * @param position The position of the item clicked.
         */
        void onClick(View view, String reply, int position);
    }

    /**
     * Interface for handling click events on the close icon.
     */
    public interface OnCloseIconClick {
        /**
         * Called when the close icon is clicked.
         *
         * @param view The view that was clicked.
         */
        void onClick(View view);
    }



    /**
     * Gets the stroke color of this view.
     *
     * @return The stroke color in ARGB format.
     */
    @Override
    public int getStrokeColor() {
        return strokeColor;
    }






}
