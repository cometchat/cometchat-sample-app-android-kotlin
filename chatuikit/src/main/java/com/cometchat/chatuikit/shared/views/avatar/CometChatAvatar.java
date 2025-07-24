package com.cometchat.chatuikit.shared.views.avatar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAvatarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * The CometChatAvatar class is a custom view in Android that represents an
 * avatar, typically used for displaying user profile pictures. It extends the
 * MaterialCardView class and provides various customization options such as
 * setting the image, text, background color, border color, and border width.
 * The avatar can be set with an image URL or a drawable, and if no image is
 * available, it can display a two-letter initial based on the user's name. The
 * class uses the Glide library for loading images asynchronously. It also
 * supports applying styles to the avatar using an AvatarStyle object. Created
 * on: 03 September 2024 Modified on: 10 September 2024
 */
@SuppressWarnings("unused")
public class CometChatAvatar extends MaterialCardView {
    private static final String TAG = CometChatAvatar.class.getSimpleName();

    private CometchatAvatarBinding binding;

    // Properties for the functionality of the avatar
    private String text;
    private String avatarUrl;
    private Drawable placeHolderDrawable;

    // Style properties for customizing the avatar view
    private @ColorInt int avatarBackgroundColor;
    private @ColorInt int avatarStrokeColor;
    private @Dimension float avatarStrokeWidth;
    private @Dimension float avatarStrokeRadius;
    private @StyleRes int avatarPlaceHolderTextAppearance;
    private @ColorInt int avatarPlaceHolderTextColor;
    private @StyleRes int style;

    /**
     * Constructor for creating a CometChatAvatar instance.
     *
     * @param context The context of the current state of the application.
     */
    public CometChatAvatar(Context context) {
        this(context, null);
    }

    /**
     * Constructor for creating a CometChatAvatar instance with an attribute set.
     *
     * @param context The context of the current state of the application.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CometChatAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAvatarStyle);
    }

    /**
     * Constructor for creating a CometChatAvatar instance with an attribute set and
     * default style.
     *
     * @param context      The context of the current state of the application.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    public CometChatAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Avoid initializing the view in edit mode
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
        // Inflate the layout for this view
        binding = CometchatAvatarBinding.inflate(LayoutInflater.from(getContext()), this, true);
        // Reset the card view to default values
        Utils.initMaterialCard(this);
        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAvatar, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatAvatar_cometchatAvatarStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAvatar, defStyleAttr, styleResId);
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
            avatarBackgroundColor = typedArray.getColor(R.styleable.CometChatAvatar_cometchatAvatarBackgroundColor,
                                                        CometChatTheme.getExtendedPrimaryColor500(getContext()));
            avatarStrokeColor = typedArray.getColor(R.styleable.CometChatAvatar_cometchatAvatarStrokeColor, avatarStrokeColor);
            avatarStrokeWidth = typedArray.getDimension(R.styleable.CometChatAvatar_cometchatAvatarStrokeWidth, avatarStrokeWidth);
            avatarStrokeRadius = typedArray.getDimension(R.styleable.CometChatAvatar_cometchatAvatarStrokeRadius, avatarStrokeRadius);
            avatarPlaceHolderTextAppearance = typedArray.getResourceId(R.styleable.CometChatAvatar_cometchatAvatarPlaceHolderTextAppearance,
                                                                       avatarPlaceHolderTextAppearance);
            avatarPlaceHolderTextColor = typedArray.getColor(R.styleable.CometChatAvatar_cometchatAvatarPlaceHolderTextColor,
                                                             CometChatTheme.getPrimaryButtonIconTint(getContext()));
            // Apply default styles
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the avatar's views.
     */
    private void applyDefault() {
        setAvatarBackgroundColor(avatarBackgroundColor);
        setAvatarStrokeColor(avatarStrokeColor);
        setAvatarStrokeWidth(avatarStrokeWidth);
        setAvatarStrokeRadius(avatarStrokeRadius);
        setAvatarPlaceHolderTextAppearance(avatarPlaceHolderTextAppearance);
        setAvatarPlaceHolderTextColor(avatarPlaceHolderTextColor);
    }

    /**
     * Gets the name currently set in the avatar.
     *
     * @return The name currently displayed in the avatar.
     */
    public String getName() {
        return text;
    }

    /**
     * Sets the name to be displayed in the avatar.
     *
     * @param name The name to be displayed.
     */
    public void setName(String name) {
        String text = "";
        if (name != null && !name.isEmpty()) {
            String[] nameParts = name.trim().split("\\s+");

            if (containsOnlyEmojis(name)) {
                // If the name contains only emojis, show the first emoji
                text = getFirstCodePoint(name);
            } else if (nameParts.length >= 2) {
                // Get the first character from the first two parts of the name
                text = getFirstCodePoint(nameParts[0]) + getFirstCodePoint(nameParts[1]);
            } else {
                // Get the first two characters from the first part of the name
                text = getFirstCodePoint(nameParts[0]) + getNextCodePoint(nameParts[0], 1);
            }
        }
        setIvAvatarVisibility(View.INVISIBLE);
        binding.tvAvatar.setText(text.toUpperCase());
        setTvAvatarVisibility(View.VISIBLE);
    }

    /**
     * Sets the avatar image using the provided URL.
     *
     * @param name      The name to be displayed.
     * @param avatarUrl A non-null String representing the URL of the avatar image.
     */
    public void setAvatar(@NonNull String name, String avatarUrl) {
        setName(name);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            setAvatar(avatarUrl);
        }
    }

    private boolean containsOnlyEmojis(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        // Iterate through the string and check if all code points are emojis
        int length = input.length();
        for (int i = 0; i < length; ) {
            int codePoint = input.codePointAt(i);
            if (!isEmoji(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }

    private String getFirstCodePoint(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int firstCodePoint = input.codePointAt(0);
        return new String(Character.toChars(firstCodePoint));
    }

    private String getNextCodePoint(String input, int count) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int codePointIndex = 0;
        for (int i = 0; i < count; i++) {
            codePointIndex = input.offsetByCodePoints(codePointIndex, 1);
            if (codePointIndex >= input.length()) {
                break;
            }
        }
        if (codePointIndex < input.length()) {
            int nextCodePoint = input.codePointAt(codePointIndex);
            return new String(Character.toChars(nextCodePoint));
        }
        return "";
    }

    private void setTvAvatarVisibility(int visibility) {
        binding.tvAvatar.setVisibility(visibility);
    }

    private void setIvAvatarVisibility(int visibility) {
        binding.ivAvatar.setVisibility(visibility);
    }

    /**
     * Checks if a given context is valid for Glide image loading.
     *
     * @param context The context to check.
     * @return True if the context is valid, false otherwise.
     */
    private static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    /**
     * Sets the image and visibility for the CometChatAvatar view. Loads the image
     * using Glide if an avatar URL is provided, otherwise displays the text
     * placeholder.
     */
    private void setValues() {
        try {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                if (getContext() != null) {
                    Glide.with(getContext()).load(avatarUrl).placeholder(placeHolderDrawable).addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    @Nullable Object model,
                                                    @NonNull Target<Drawable> target,
                                                    boolean isFirstResource) {
                            setTvAvatarVisibility(View.VISIBLE);
                            setIvAvatarVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource,
                                                       @NonNull Object model,
                                                       Target<Drawable> target,
                                                       @NonNull DataSource dataSource,
                                                       boolean isFirstResource) {
                            setTvAvatarVisibility(View.GONE);
                            setIvAvatarVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(binding.ivAvatar);
                }
                binding.innerViewLayout.setBackgroundColor(getContext().getColor(android.R.color.transparent));
                applyDefault();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        invalidate();
    }

    private boolean isEmoji(int codePoint) {
        // Check if the code point is within the emoji ranges
        return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
            (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols and Pictographs
            (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport and Map
            (codePoint >= 0x2600 && codePoint <= 0x26FF) ||   // Misc symbols
            (codePoint >= 0x2700 && codePoint <= 0x27BF) ||   // Dingbats
            (codePoint >= 0xFE00 && codePoint <= 0xFE0F) ||   // Variation Selectors
            (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) || // Supplemental Symbols and Pictographs
            (codePoint >= 0x1FA70 && codePoint <= 0x1FAFF);   // Symbols and Pictographs Extended-A
    }

    /**
     * Sets the style for the CometChatAvatar view by applying a style resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAvatar);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Sets the avatar image using the provided URL.
     *
     * @param placeHolderDrawable The Placeholder Drawable to be displayed.
     * @param avatarUrl           A non-null String representing the URL of the avatar image.
     */
    public void setAvatar(@NonNull String avatarUrl, Drawable placeHolderDrawable) {
        this.placeHolderDrawable = placeHolderDrawable;
        this.avatarUrl = avatarUrl;
        if (isValidContextForGlide(getContext())) {
            setValues();
        }
    }

    /**
     * Gets the URL of the avatar image currently set.
     *
     * @return The URL of the avatar image.
     */
    public String getAvatar() {
        return avatarUrl;
    }

    /**
     * Sets the avatar image using the provided URL.
     *
     * @param avatarUrl A non-null String representing the URL of the avatar image.
     */
    public void setAvatar(@NonNull String avatarUrl) {
        this.avatarUrl = avatarUrl;
        if (isValidContextForGlide(getContext())) setValues();
    }

    /**
     * Sets the image to be displayed in the CometChatAvatar.
     *
     * @param drawable The Drawable to be displayed.
     */
    public void setAvatar(@NonNull Drawable drawable) {
        binding.ivAvatar.setImageDrawable(drawable);
        binding.ivAvatar.setVisibility(View.VISIBLE);
        binding.tvAvatar.setVisibility(View.GONE);
    }

    /**
     * Sets a drawable placeholder to be displayed while loading the avatar image.
     *
     * @param placeholderDrawable The drawable to be set as the placeholder.
     */
    public void setPlaceholder(Drawable placeholderDrawable) {
        this.placeHolderDrawable = placeholderDrawable;
    }

    /**
     * Gets the background color of the avatar.
     *
     * @return The background color of the avatar.
     */
    public @ColorInt int getAvatarBackgroundColor() {
        return avatarBackgroundColor;
    }

    /**
     * Sets the background color of the avatar.
     *
     * @param avatarBackgroundColor The color to be set as the avatar's background.
     */
    public void setAvatarBackgroundColor(@ColorInt int avatarBackgroundColor) {
        this.avatarBackgroundColor = avatarBackgroundColor;
        binding.innerViewLayout.setBackgroundColor(avatarBackgroundColor);
    }

    /**
     * Gets the stroke color of the avatar's border.
     *
     * @return The stroke color of the avatar's border.
     */
    public @ColorInt int getAvatarStrokeColor() {
        return avatarStrokeColor;
    }

    /**
     * Sets the stroke color of the avatar's border.
     *
     * @param avatarStrokeColor The color to be set as the avatar's stroke color.
     */
    public void setAvatarStrokeColor(@ColorInt int avatarStrokeColor) {
        this.avatarStrokeColor = avatarStrokeColor;
        binding.cardView.setStrokeColor(avatarStrokeColor);
    }

    /**
     * Gets the width of the avatar's border.
     *
     * @return The width of the avatar's border.
     */
    public @Dimension float getAvatarStrokeWidth() {
        return avatarStrokeWidth;
    }

    /**
     * Sets the width of the avatar's border.
     *
     * @param avatarStrokeWidth The width to be set for the avatar's stroke.
     */
    public void setAvatarStrokeWidth(@Dimension float avatarStrokeWidth) {
        this.avatarStrokeWidth = avatarStrokeWidth;
        binding.cardView.setStrokeWidth((int) avatarStrokeWidth);
    }

    /**
     * Gets the radius of the avatar's corners.
     *
     * @return The radius of the avatar's corners.
     */
    public @Dimension float getAvatarStrokeRadius() {
        return avatarStrokeRadius;
    }

    /**
     * Sets the radius of the avatar's corners.
     *
     * @param avatarStrokeRadius The radius to be set for the avatar's corners.
     */
    public void setAvatarStrokeRadius(@Dimension float avatarStrokeRadius) {
        this.avatarStrokeRadius = avatarStrokeRadius;
        binding.cardView.setRadius(avatarStrokeRadius);
    }

    /**
     * Gets the text appearance currently applied to the placeholder.
     *
     * @return The text appearance resource currently applied.
     */
    public @StyleRes int getAvatarPlaceHolderTextAppearance() {
        return avatarPlaceHolderTextAppearance;
    }

    /**
     * Sets the text appearance for the placeholder when no image is available.
     *
     * @param avatarPlaceHolderTextAppearance The text appearance resource to be applied.
     */
    public void setAvatarPlaceHolderTextAppearance(@StyleRes int avatarPlaceHolderTextAppearance) {
        this.avatarPlaceHolderTextAppearance = avatarPlaceHolderTextAppearance;
        binding.tvAvatar.setTextAppearance(avatarPlaceHolderTextAppearance);
    }

    /**
     * Gets the text color currently applied to the placeholder.
     *
     * @return The text color currently applied to the placeholder.
     */
    public @ColorInt int getAvatarPlaceHolderTextColor() {
        return avatarPlaceHolderTextColor;
    }

    /**
     * Sets the text color for the placeholder when no image is available.
     *
     * @param avatarPlaceHolderTextColor The color to be set for the placeholder text.
     */
    public void setAvatarPlaceHolderTextColor(@ColorInt int avatarPlaceHolderTextColor) {
        this.avatarPlaceHolderTextColor = avatarPlaceHolderTextColor;
        binding.tvAvatar.setTextColor(avatarPlaceHolderTextColor);
    }
}
