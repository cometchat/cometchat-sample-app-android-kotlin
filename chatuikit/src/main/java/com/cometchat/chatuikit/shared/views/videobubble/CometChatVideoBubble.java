package com.cometchat.chatuikit.shared.views.videobubble;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

/**
 * Represents a custom video bubble view that extends the MaterialCardView.
 *
 * <p>
 * The CometChatVideoBubble is used to display a video thumbnail with a play
 * button overlay.
 */
public class CometChatVideoBubble extends MaterialCardView {
    private static final String TAG = CometChatVideoBubble.class.getSimpleName();
    private LinearLayout parentLayout;
    private MaterialCardView videoCard;
    private FrameLayout playButtonLayout;
    private ImageView videoPlayImageView;
    private ImageView videoPlayImageBackground;
    private ImageView shapeableImageView;
    private String videoUrl;
    private OnClick onClick;
    private @ColorInt int playIconTint;
    private @ColorInt int playButtonBackgroundColor;
    private @StyleRes int style;
    private @Dimension int videoCardRadius;
    private @ColorInt int videoCardStrokeColor;
    private @Dimension int videoCardStrokeWidth;
    private ProgressBar progressBar;
    private File file;

    /**
     * Constructs a new CometChatVideoBubble with the provided context.
     *
     * @param context The context used for inflating the view.
     */
    public CometChatVideoBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatVideoBubble with the provided context and attribute
     * set.
     *
     * @param context The context used for inflating the view.
     * @param attrs   The attribute set used for custom styling.
     */
    public CometChatVideoBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatVideoBubbleStyle);
    }

    /**
     * Constructs a new CometChatVideoBubble with the provided context, attribute
     * set, and default style attribute.
     *
     * @param context      The context used for inflating the view.
     * @param attrs        The attribute set used for custom styling.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatVideoBubble(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Inflate the view layout and initialize UI components.
     *
     * @param attributeSet The attributes for customizing the view.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attributeSet, int defStyleAttr) {
        View view1 = View.inflate(getContext(), R.layout.cometchat_video_bubble, null);
        parentLayout = view1.findViewById(R.id.parent);
        shapeableImageView = view1.findViewById(R.id.video);
        playButtonLayout = view1.findViewById(R.id.play_button_layout);
        videoPlayImageView = view1.findViewById(R.id.play_btn);
        videoPlayImageBackground = view1.findViewById(R.id.play_btn_bg);
        videoCard = view1.findViewById(R.id.video_view_container_card);
        progressBar = view1.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        playButtonLayout.setVisibility(View.GONE);
        Utils.initMaterialCard(videoCard);
        Utils.initMaterialCard(this);
        addView(view1);
        playButtonLayout.setOnClickListener(view -> invokeClick());
        shapeableImageView.setOnClickListener(v -> invokeClick());
        shapeableImageView.setOnLongClickListener(v -> {
            Utils.performAdapterClick(v);
            return false;
        });
        applyStyleAttributes(attributeSet, defStyleAttr);
    }

    private void invokeClick() {
        if (onClick != null) onClick.onClick();
        else openMediaViewActivity();
    }

    /**
     * Apply custom style attributes to the view.
     *
     * @param attrs        The attribute set to apply.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        // Obtain styled attributes
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatVideoBubble, defStyleAttr, 0);
        int styleResId = typedArray.getResourceId(R.styleable.CometChatVideoBubble_cometchatVideoBubbleStyle, 0);
        // Apply default style if defined
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatVideoBubble, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(typedArray);
    }

    private void openMediaViewActivity() {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            MediaUtils.openMediaInPlayer(getContext(), videoUrl, "video/*");
        } else {
            MediaUtils.openFile(getContext(), file);
        }
    }

    /**
     * Extract attributes from the typed array and apply them to the view.
     *
     * @param typedArray The array of attributes to extract and apply.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setVideoCardRadius((int) typedArray.getDimension(R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoCornerRadius, 0));
            setVideoCardStrokeColor((int) typedArray.getDimension(R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoStrokeColor, 0));
            setVideoCardStrokeWidth((int) typedArray.getDimension(R.styleable.CometChatVideoBubble_cometchatVideoBubbleVideoStrokeWidth, 0));
            setPlayIconBackgroundColor(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubblePlayIconBackgroundColor, 0));
            setPlayIconTint(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubblePlayIconTint,
                                                CometChatTheme.getColorWhite(getContext())));
            setProgressIndeterminateTint(typedArray.getColor(R.styleable.CometChatVideoBubble_cometchatVideoBubbleProgressIndeterminateTint,
                                                             CometChatTheme.getIconTintSecondary(getContext())));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the background color for the play button.
     *
     * @param color The background color to be applied.
     */
    public void setPlayIconBackgroundColor(@ColorInt int color) {
        if (color != 0) {
            this.playButtonBackgroundColor = color;
            videoPlayImageBackground.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * Sets the tint color for the progress bar.
     *
     * @param progressIndeterminateTint The color value to be set.
     */
    public void setProgressIndeterminateTint(@ColorInt int progressIndeterminateTint) {
        progressBar.getIndeterminateDrawable().setColorFilter(progressIndeterminateTint, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the video URL and placeholder image for the video thumbnail.
     *
     * @param videoUrl The URL of the video.
     */
    public void setVideoUrl(File file, String videoUrl) {
        this.videoUrl = videoUrl;
        this.file = file;
        loadBitmapIntoImageView(file, videoUrl);
    }

    private void loadBitmapIntoImageView(File file, String url) {
        RequestBuilder<Bitmap> builder = Glide.with(getContext()).asBitmap();
        builder
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(0)
            .skipMemoryCache(false)
            .load(file != null && file.exists() ? file : url)
            .addListener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object o, @NonNull Target<Bitmap> target, boolean b) {
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Bitmap bitmap,
                                               @NonNull Object o,
                                               Target<Bitmap> target,
                                               @NonNull DataSource dataSource,
                                               boolean b) {
                    progressBar.setVisibility(View.GONE);
                    playButtonLayout.setVisibility(View.VISIBLE);
                    return false;
                }
            })
            .into(shapeableImageView);
    }

    /**
     * Sets the thumbnail URL and placeholder image for the video thumbnail.
     *
     * @param thumbnailUrl     The URL of the thumbnail image.
     * @param placeHolderImage The placeholder image resource ID.
     */
    public void setThumbnailUrl(String thumbnailUrl, @DrawableRes int placeHolderImage) {
        loadBitmapIntoImageView(null, thumbnailUrl);
    }

    /**
     * Sets the play icon resource for the play button.
     *
     * @param playIcon The play icon resource ID.
     */
    public void setPlayIcon(@DrawableRes int playIcon) {
        if (playIcon != 0) videoPlayImageView.setImageResource(playIcon);
    }

    public LinearLayout getView() {
        return parentLayout;
    }

    public ImageView getVideoImageView() {
        return shapeableImageView;
    }

    public ImageView getPlayButtonImageView() {
        return videoPlayImageView;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the onClick listener for the play button.
     *
     * @param onClick The onClick listener to be set.
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    // Getters for testing or direct access if needed

    public @ColorInt int getPlayIconTint() {
        return playIconTint;
    }

    /**
     * Sets the tint color for the play button icon.
     *
     * @param color The tint color to be applied.
     */
    public void setPlayIconTint(@ColorInt int color) {
        if (color != 0) {
            this.playIconTint = color;
            videoPlayImageView.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    public @Dimension int getVideoCardRadius() {
        return videoCardRadius;
    }

    /**
     * Sets the corner radius for the video card.
     *
     * @param radius The corner radius to be set.
     */
    public void setVideoCardRadius(@Dimension int radius) {
        this.videoCardRadius = radius;
        videoCard.setRadius(radius);
    }

    public @ColorInt int getVideoCardStrokeColor() {
        return videoCardStrokeColor;
    }

    /**
     * Sets the stroke color for the video card.
     *
     * @param color The stroke color to be set.
     */
    public void setVideoCardStrokeColor(@ColorInt int color) {
        this.videoCardStrokeColor = color;
        videoCard.setStrokeColor(color);
    }

    public @Dimension int getVideoCardStrokeWidth() {
        return videoCardStrokeWidth;
    }

    /**
     * Sets the stroke width for the video card.
     *
     * @param width The stroke width to be set.
     */
    public void setVideoCardStrokeWidth(@Dimension int width) {
        this.videoCardStrokeWidth = width;
        videoCard.setStrokeWidth(width);
    }

    public int getPlayButtonBackgroundColor() {
        return playButtonBackgroundColor;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public int getStyle() {
        return style;
    }

    /**
     * Set the style for the file bubble.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        this.style = style;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatVideoBubble);
        extractAttributesAndApplyDefaults(typedArray);
    }
}
