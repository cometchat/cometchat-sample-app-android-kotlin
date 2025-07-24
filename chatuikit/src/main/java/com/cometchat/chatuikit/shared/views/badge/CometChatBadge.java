package com.cometchat.chatuikit.shared.views.badge;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * CometChatBadge is a custom view that represents a badge with a count
 * displayed inside a MaterialCardView. It provides methods to customize the
 * appearance of the badge, such as setting the count, text size, text color,
 * background color, corner radius, stroke color, and more.
 *
 * <p>
 * Created on: 06 September 2024 Modified on: 10 September 2024
 */
@SuppressWarnings("unused")
public class CometChatBadge extends MaterialCardView {
    private static final String TAG = CometChatBadge.class.getSimpleName();

    private TextView tvMessageCount;
    private int count = 0;

    private @ColorInt int badgeTextColor;
    private @StyleRes int badgeTextAppearance;
    private @Dimension float badgeCornerRadius;
    private @ColorInt int badgeBackgroundColor;
    private @Dimension float badgeStrokeWidth;
    private @ColorInt int badgeStrokeColor;

    /**
     * Constructs a new CometChatBadge with the given context.
     *
     * @param context The context of the view.
     */
    public CometChatBadge(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatBadge with the given context and attribute set.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set for the view.
     */
    public CometChatBadge(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatBadgeStyle);
    }

    /**
     * Constructs a new CometChatBadge with the given context, attribute set, and
     * default style attribute.
     *
     * @param context      The context of the view.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Initialize the MaterialCardView
        Utils.initMaterialCard(this);
        // Set the layout parameters
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // Create a new TextView to display the message count
        tvMessageCount = new TextView(getContext());
        // Set layout params to wrap the content width (we'll manage centering manually)
        tvMessageCount.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Center the text inside the TextView
        tvMessageCount.setGravity(Gravity.CENTER);
        tvMessageCount.setIncludeFontPadding(false);
        // Set text alignment explicitly to center
        tvMessageCount.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // Set padding (left, top, right, bottom) for the TextView
        tvMessageCount.setPadding(getResources().getDimensionPixelSize(R.dimen.cometchat_4dp),
                                  getResources().getDimensionPixelSize(R.dimen.cometchat_2dp),
                                  getResources().getDimensionPixelSize(R.dimen.cometchat_4dp),
                                  getResources().getDimensionPixelSize(R.dimen.cometchat_2dp));
        // Set minimum width of the TextView for single-digit numbers (e.g., 20dp
        // converted to
        // pixels)
        tvMessageCount.setMinWidth(getResources().getDimensionPixelSize(R.dimen.cometchat_20dp));
        // Add the TextView to the MaterialCardView
        addView(tvMessageCount);
        // Set the default values
        setDefaultValues();
        // Apply the style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Sets the default values for the CometChatBadge.
     */
    private void setDefaultValues() {
        badgeTextColor = CometChatTheme.getPrimaryButtonIconTint(getContext());
        badgeBackgroundColor = CometChatTheme.getPrimaryColor(getContext());
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatBadge, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatBadge_cometchatBadgeStyle, 0);
        directAttributes = styleResId != 0 ? getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatBadge, defStyleAttr, styleResId) : null;
        extractAttributesAndApplyDefaults(directAttributes);
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
            badgeTextAppearance = typedArray.getResourceId(R.styleable.CometChatBadge_cometchatBadgeTextAppearance, badgeTextAppearance);
            badgeTextColor = typedArray.getColor(R.styleable.CometChatBadge_cometchatBadgeTextColor, badgeTextColor);
            badgeCornerRadius = typedArray.getDimension(R.styleable.CometChatBadge_cometchatBadgeCornerRadius, badgeCornerRadius);
            badgeBackgroundColor = typedArray.getColor(R.styleable.CometChatBadge_cometchatBadgeBackgroundColor, badgeBackgroundColor);
            badgeStrokeWidth = typedArray.getDimension(R.styleable.CometChatBadge_cometchatBadgeStrokeWidth, badgeStrokeWidth);
            badgeStrokeColor = typedArray.getColor(R.styleable.CometChatBadge_cometchatBadgeStrokeColor, badgeStrokeColor);
            // Apply default settings
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the CometChatBadge.
     */
    private void applyDefault() {
        setCount(count);
        setBadgeTextAppearance(badgeTextAppearance);
        setBadgeTextColor(badgeTextColor);
        setBadgeCornerRadius(badgeCornerRadius);
        setBadgeBackgroundColor(badgeBackgroundColor);
        setBadgeStrokeWidth(badgeStrokeWidth);
        setBadgeStrokeColor(badgeStrokeColor);
    }

    /**
     * Sets the style for the CometChatBadge view by applying a style resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatBadge);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the count for the badge.
     *
     * @return The count of the badge.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count for the badge.
     *
     * @param count The count to set.
     */
    public void setCount(int count) {
        this.count = count;
        if (count < 999) {
            tvMessageCount.setText(String.valueOf(count));
        } else {
            tvMessageCount.setText(R.string.cometchat_unread_message_count_max);
        }
    }

    /**
     * Gets the count text color.
     *
     * @return The count text color.
     */
    public @ColorInt int getBadgeTextColor() {
        return badgeTextColor;
    }

    /**
     * Sets the count text color.
     *
     * @param badgeTextColor The count text color to set.
     */
    public void setBadgeTextColor(@ColorInt int badgeTextColor) {
        this.badgeTextColor = badgeTextColor;
        tvMessageCount.setTextColor(badgeTextColor);
    }

    /**
     * Gets the count text style.
     *
     * @return The count text style.
     */
    public @StyleRes int getBadgeTextAppearance() {
        return badgeTextAppearance;
    }

    /**
     * Sets the count text style.
     *
     * @param badgeTextAppearance The count text style to set.
     */
    public void setBadgeTextAppearance(@StyleRes int badgeTextAppearance) {
        this.badgeTextAppearance = badgeTextAppearance;
        tvMessageCount.setTextAppearance(badgeTextAppearance);
    }

    /**
     * Gets the badge corner radius.
     *
     * @return The badge corner radius.
     */
    public @Dimension float getBadgeCornerRadius() {
        return badgeCornerRadius;
    }

    /**
     * Sets the badge corner radius.
     *
     * @param badgeCornerRadius The badge corner radius to set.
     */
    public void setBadgeCornerRadius(@Dimension float badgeCornerRadius) {
        this.badgeCornerRadius = badgeCornerRadius;
        setRadius(badgeCornerRadius);
    }

    /**
     * Gets the badge background color.
     *
     * @return The badge background color.
     */
    public @ColorInt int getBadgeBackgroundColor() {
        return badgeBackgroundColor;
    }

    /**
     * Sets the badge background color.
     *
     * @param badgeBackgroundColor The badge background color to set.
     */
    public void setBadgeBackgroundColor(@ColorInt int badgeBackgroundColor) {
        this.badgeBackgroundColor = badgeBackgroundColor;
        setCardBackgroundColor(badgeBackgroundColor);
    }

    /**
     * Gets the badge stroke width.
     *
     * @return The badge stroke width.
     */
    public @Dimension float getBadgeStrokeWidth() {
        return badgeStrokeWidth;
    }

    /**
     * Sets the badge stroke width.
     *
     * @param badgeStrokeWidth The badge stroke width to set.
     */
    public void setBadgeStrokeWidth(@Dimension float badgeStrokeWidth) {
        this.badgeStrokeWidth = badgeStrokeWidth;
        setStrokeWidth((int) badgeStrokeWidth);
    }

    /**
     * Gets the badge stroke color.
     *
     * @return The badge stroke color.
     */
    public @ColorInt int getBadgeStrokeColor() {
        return badgeStrokeColor;
    }

    /**
     * Sets the badge stroke color.
     *
     * @param badgeStrokeColor The badge stroke color to set.
     */
    public void setBadgeStrokeColor(@ColorInt int badgeStrokeColor) {
        this.badgeStrokeColor = badgeStrokeColor;
        setStrokeColor(badgeStrokeColor);
    }
}
