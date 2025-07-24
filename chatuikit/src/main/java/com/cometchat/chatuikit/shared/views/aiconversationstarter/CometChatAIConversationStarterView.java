package com.cometchat.chatuikit.shared.views.aiconversationstarter;

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
import com.cometchat.chatuikit.databinding.CometchatAiConversationStarterBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import javax.annotation.Nullable;

/**
 * The CometChatAIConversationStarterView is a customizable UI component
 * designed to display a list of conversation starter options in a RecyclerView,
 * with built-in error and loading states. This view allows developers to
 * customize various attributes like colors, dimensions, text appearances,
 * corner radius, stroke width, and other visual elements. It also supports
 * setting custom layouts for error and loading states, along with a shimmer
 * effect to indicate loading.
 *
 * <p>
 * This view is typically used in messaging or chat applications to help users
 * quickly start conversations with pre-defined responses.
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * CometChatAIConversationStarterView view = new CometChatAIConversationStarterView(context);
 * view.setReplyList(Arrays.asList("Hello!", "How are you?", "Let's chat!"));
 * view.setOnClick((reply, position) -> {
 * 	// Handle the click event
 * });
 * </pre>
 */
public class CometChatAIConversationStarterView extends MaterialCardView {
    private static final String TAG = CometChatAIConversationStarterView.class.getSimpleName();

    /**
     * Binding for view inflation.
     */
    private CometchatAiConversationStarterBinding binding;

    /**
     * User ID or group ID associated with the component.
     */
    private String uid;

    /**
     * Maximum height for the view in pixels.
     */
    private int maxHeight = 0;

    /**
     * Click listener for reply items.
     */
    private OnClick onClick;

    /**
     * Adapter for managing conversation starter replies.
     */
    private ConversationStarterAdapter conversationStarterAdapter;

    // Style attributes for the component's main background
    private @ColorInt int backgroundColor;
    private Drawable backgroundDrawable;
    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;

    // Style attributes for individual items in the list
    private @ColorInt int itemBackgroundColor;
    private Drawable itemBackgroundDrawable;
    private @Dimension int itemCornerRadius;
    private @Dimension int itemStrokeWidth;
    private @ColorInt int itemStrokeColor;
    private @ColorInt int itemTextColor;
    private @StyleRes int itemTextAppearance;

    // Style attributes for error state text
    private @ColorInt int errorStateTextColor;
    private @StyleRes int errorStateTextAppearance;

    /**
     * Resource ID for custom error view layout.
     */
    private @LayoutRes int errorViewLayout;

    /**
     * Resource ID for custom loading view layout.
     */
    private @LayoutRes int loadingViewLayout;

    /**
     * The style resource ID for the view.
     */
    private @StyleRes int style;

    public CometChatAIConversationStarterView(Context context) {
        this(context, null);
    }

    public CometChatAIConversationStarterView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAIConversationStarterStyle);
    }

    public CometChatAIConversationStarterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view with attributes from XML or theme, setting up listeners
     * and default values.
     *
     * @param attrs        AttributeSet for view customization from XML.
     * @param defStyleAttr Default style attribute.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatAiConversationStarterBinding.inflate(LayoutInflater.from(getContext()), this, true);
        conversationStarterAdapter = new ConversationStarterAdapter();
        binding.recyclerView.setAdapter(conversationStarterAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String reply = (String) view.getTag(R.string.cometchat_reply_lowercase);
                if (onClick != null) onClick.onClick(uid, reply, position);
            }
        }));

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
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs,
                                                                               R.styleable.CometChatAIConversationStarter,
                                                                               defStyleAttr,
                                                                               defStyleRes
        );
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIConversationStarter, defStyleAttr, style);
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
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterBackgroundColor, 0));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterBackgroundDrawable));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterCornerRadius,
                                                             0
            ));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterStrokeColor, 0));

            setItemBackgroundColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemBackgroundColor,
                                                       CometChatTheme.getBackgroundColor1(getContext()
                                                       )));
            setItemBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemBackgroundDrawable));
            setItemCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemCornerRadius,
                                                                 0
            ));
            setItemStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemStrokeWidth,
                                                                0
            ));
            setItemStrokeColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemStrokeColor, 0));

            setItemTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemTextAppearance,
                                                           0
            ));
            setItemTextColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterItemTextColor,
                                                 CometChatTheme.getTextColorPrimary(getContext()
                                                 )));
            setErrorStateTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterErrorStateTextAppearance,
                                                                 0
            ));
            setErrorStateTextColor(typedArray.getColor(R.styleable.CometChatAIConversationStarter_cometchatAIConversationStarterErrorStateTextColor,
                                                       CometChatTheme.getTextColorSecondary(getContext())
            ));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Replaces the current view in the loadedViewParentLayout with the provided
     * custom view.
     *
     * @param view The custom view to display in place of the default loaded view.
     */
    public void setCustomView(View view) {
        if (view != null) {
            binding.loadedViewParentLayout.removeAllViews();
            binding.loadingViewLayout.setVisibility(GONE);
            binding.loadedViewParentLayout.addView(view);
        }
    }

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
     * Populates the view with a list of replies and updates visibility of loading
     * and error views.
     *
     * @param replies List of reply options.
     */
    public void setReplyList(List<String> replies) {
        if (!replies.isEmpty()) {
            binding.recyclerView.setVisibility(VISIBLE);
            conversationStarterAdapter.setList(replies);
            stopShimmer();
            binding.loadingViewLayout.setVisibility(GONE);
            binding.errorViewLayout.setVisibility(GONE);
        }
    }

    /**
     * Stops the shimmer loading effect.
     */
    private void stopShimmer() {
        if (loadingViewLayout == 0) binding.shimmerEffectFrame.stopShimmer();
    }

    /**
     * Displays the error view and hides other content views.
     */
    public void showErrorView() {
        binding.errorViewLayout.setVisibility(VISIBLE);
        binding.loadingViewLayout.setVisibility(GONE);
        binding.recyclerView.setVisibility(GONE);
        stopShimmer();
    }

    /**
     * Displays the loading view with a shimmer effect.
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
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(1, R.layout.cometchat_ai_conversation_starter_shimmer);
                binding.shimmerRecyclerview.setAdapter(adapter);
                binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerEffectFrame.startShimmer();
            }
            binding.shimmerParentLayout.setVisibility(visibility);
        }
    }

    /**
     * Sets the error message text to display in the error view.
     *
     * @param errorText Text to display for error.
     */
    public void setErrorStateText(String errorText) {
        if (errorText != null && !errorText.isEmpty()) binding.tvError.setText(errorText);
    }

    /**
     * Retrieves the maximum height for the component.
     *
     * @return The maximum height as an integer in pixels.
     */
    @Dimension
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Limits the view’s height to maxHeight if specified, otherwise, it defaults to
     * original dimensions.
     *
     * @param maxHeightInDp Maximum height in dp for the view.
     */
    public void setMaxHeight(int maxHeightInDp) {
        if (maxHeightInDp > 0) this.maxHeight = Utils.convertDpToPx(getContext(), maxHeightInDp);
    }

    /**
     * Provides access to the binding instance associated with this component.
     *
     * @return An instance of {@link CometchatAiConversationStarterBinding}
     * representing the binding.
     */
    @NonNull
    public CometchatAiConversationStarterBinding getBinding() {
        return binding;
    }    /**
     * Sets the stroke width for the view's border.
     *
     * @param strokeWidth Width of the stroke.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Retrieves the {@link OnClick} listener for click events on the component.
     *
     * @return An instance of {@link OnClick}.
     */
    @Nullable
    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the click listener to handle item selection events in the RecyclerView.
     *
     * @param onClick Click listener for handling item clicks.
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    /**
     * Provides the adapter for managing conversation starter items.
     *
     * @return The {@link ConversationStarterAdapter} instance used by this
     * component.
     */
    @NonNull
    public ConversationStarterAdapter getConversationStarterAdapter() {
        return conversationStarterAdapter;
    }

    /**
     * Gets the background color applied to the component.
     *
     * @return The background color as an integer.
     */
    @ColorInt
    public int getBackgroundColor() {
        return backgroundColor;
    }    /**
     * Sets the color of the view's border.
     *
     * @param strokeColor Color of the stroke.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Sets the background color for the view.
     *
     * @param backgroundColor Color for the background.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Retrieves the background drawable for the component.
     *
     * @return A {@link Drawable} representing the background.
     */
    @Nullable
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets a drawable as the background for the view.
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
     * Gets the corner radius for the component.
     *
     * @return The corner radius in pixels.
     */
    @Dimension
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the view's card.
     *
     * @param cornerRadius Radius for rounded corners.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Retrieves the background color for each item in the list.
     *
     * @return The item background color as an integer.
     */
    @ColorInt
    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    /**
     * Sets the background color for each item in the list.
     *
     * @param itemBackgroundColor The color to set as the background for each item.
     */
    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        conversationStarterAdapter.setItemBackgroundColor(itemBackgroundColor);
    }

    /**
     * Gets the background drawable for each item in the list.
     *
     * @return A {@link Drawable} representing the item background.
     */
    @Nullable
    public Drawable getItemBackgroundDrawable() {
        return itemBackgroundDrawable;
    }

    /**
     * Sets a custom drawable as the background for each item in the list.
     *
     * @param itemBackgroundDrawable The drawable to set as the background for each item.
     */
    public void setItemBackgroundDrawable(Drawable itemBackgroundDrawable) {
        this.itemBackgroundDrawable = itemBackgroundDrawable;
        conversationStarterAdapter.setItemBackgroundDrawable(itemBackgroundDrawable);
    }

    /**
     * Retrieves the corner radius for each item in the list.
     *
     * @return The item corner radius in pixels.
     */
    @Dimension
    public int getItemCornerRadius() {
        return itemCornerRadius;
    }

    /**
     * Sets the corner radius for each item in the list, allowing for rounded
     * corners.
     *
     * @param itemCornerRadius The radius for rounding the corners of each item.
     */
    public void setItemCornerRadius(@Dimension int itemCornerRadius) {
        this.itemCornerRadius = itemCornerRadius;
        conversationStarterAdapter.setItemCornerRadius(itemCornerRadius);
    }

    /**
     * Gets the stroke width for each item in the list.
     *
     * @return The item stroke width in pixels.
     */
    @Dimension
    public int getItemStrokeWidth() {
        return itemStrokeWidth;
    }

    /**
     * Sets the width of the stroke for each item in the list.
     *
     * @param itemStrokeWidth The width of the border stroke around each item.
     */
    public void setItemStrokeWidth(@Dimension int itemStrokeWidth) {
        this.itemStrokeWidth = itemStrokeWidth;
        conversationStarterAdapter.setItemStrokeWidth(itemStrokeWidth);
    }

    /**
     * Retrieves the color of the stroke for each item in the list.
     *
     * @return The item stroke color as an integer.
     */
    @ColorInt
    public int getItemStrokeColor() {
        return itemStrokeColor;
    }

    /**
     * Sets the color of the stroke for each item in the list.
     *
     * @param itemStrokeColor The color of the border stroke around each item.
     */
    public void setItemStrokeColor(@ColorInt int itemStrokeColor) {
        this.itemStrokeColor = itemStrokeColor;
        conversationStarterAdapter.setItemStrokeColor(itemStrokeColor);
    }

    /**
     * Gets the text color for each item in the list.
     *
     * @return The item text color as an integer.
     */
    @ColorInt
    public int getItemTextColor() {
        return itemTextColor;
    }

    /**
     * Sets the text color for each item in the list.
     *
     * @param itemTextColor The color to apply to the text within each item.
     */
    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
        conversationStarterAdapter.setItemTextColor(itemTextColor);
    }

    /**
     * Retrieves the text appearance style resource for each item.
     *
     * @return The resource ID of the text appearance style for each item.
     */
    @StyleRes
    public int getItemTextAppearance() {
        return itemTextAppearance;
    }

    /**
     * Sets the text appearance for each item in the list. The appearance should be
     * defined as a style resource for consistent typography and styling across
     * items.
     *
     * @param itemTextAppearance The resource ID of the style to apply to the text within each
     *                           item.
     */
    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
        conversationStarterAdapter.setItemTextAppearance(itemTextAppearance);
    }

    /**
     * Gets the text color for error state messages.
     *
     * @return The error state text color as an integer.
     */
    @ColorInt
    public int getErrorStateTextColor() {
        return errorStateTextColor;
    }

    /**
     * Sets the text color of the error state.
     *
     * @param color Text color for the error state.
     */
    public void setErrorStateTextColor(@ColorInt int color) {
        this.errorStateTextColor = color;
        binding.tvError.setTextColor(color);
    }

    /**
     * Retrieves the text appearance style for error state messages.
     *
     * @return The resource ID of the error state text appearance style.
     */
    @StyleRes
    public int getErrorStateTextAppearance() {
        return errorStateTextAppearance;
    }

    /**
     * Sets the appearance of the error state text.
     *
     * @param errorStateTextAppearance Text appearance for the error state.
     */
    public void setErrorStateTextAppearance(@StyleRes int errorStateTextAppearance) {
        this.errorStateTextAppearance = errorStateTextAppearance;
        binding.tvError.setTextAppearance(errorStateTextAppearance);
    }

    /**
     * Gets the layout resource for the error view.
     *
     * @return The layout resource ID for the error view.
     */
    @LayoutRes
    public int getErrorViewLayout() {
        return errorViewLayout;
    }

    /**
     * Sets the layout resource to use for the error view.
     *
     * @param errorViewLayout Layout resource ID for the error view.
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
     * Retrieves the layout resource for the loading view.
     *
     * @return The layout resource ID for the loading view.
     */
    @LayoutRes
    public int getLoadingViewLayout() {
        return loadingViewLayout;
    }

    /**
     * Sets the layout resource to use for the loading view.
     *
     * @param loadingViewLayout Layout resource ID for the loading view.
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
     * Gets the style resource associated with the component.
     *
     * @return The resource ID of the style.
     */
    @StyleRes
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the AIConversation starter from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAIConversationStarter);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }    /**
     * Retrieves the stroke width for the component's border.
     *
     * @return The stroke width in pixels.
     */
    @Dimension
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
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
     * Interface defining a click event callback for item selection.
     */
    public interface OnClick {
        /**
         * Called when an item is clicked.
         *
         * @param id       The selected reply ID.
         * @param reply    The selected reply text.
         * @param position Position of the clicked item.
         */
        void onClick(String id, String reply, int position);
    }

    /**
     * Gets the color of the stroke around the component.
     *
     * @return The stroke color as an integer.
     */
    @ColorInt
    @Override
    public int getStrokeColor() {
        return strokeColor;
    }








}
