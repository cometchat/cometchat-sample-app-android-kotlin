package com.cometchat.chatuikit.shared.views.imagebubble;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cometchat.chat.models.Attachment;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.Collections;

/**
 * Custom view that displays an image bubble for messaging applications.
 *
 * <p>
 * This view allows displaying images with optional captions and handles image
 * loading asynchronously using Glide. It also supports styling options for
 * customization.
 */
public class CometChatImageBubble extends MaterialCardView {
    private static final String TAG = CometChatImageBubble.class.getSimpleName();
    private Context context;
    private ImageView shapeableImageView;
    private MaterialCardView imageViewContainerCard;
    private ProgressBar progressBar;
    private TextView textView;
    private OnClick onClick;
    private String imageUrl;
    private @ColorInt int backgroundColor;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @Dimension int cornerRadius;
    private Drawable backgroundDrawable = null;
    private @StyleRes int style;
    private @ColorInt int progressIndeterminateTint;
    private MediaMessage mediaMessage;

    /**
     * Constructs a new instance of CometChatImageBubble.
     *
     * @param context The context in which the view is created.
     */
    public CometChatImageBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new instance of CometChatImageBubble.
     *
     * @param context The context in which the view is created.
     * @param attrs   The attribute set used to inflate the view.
     */
    public CometChatImageBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatImageBubbleStyle);
    }

    /**
     * Constructs a new instance of CometChatImageBubble with style.
     *
     * @param context      The context in which the view is created.
     * @param attrs        The attribute set used to inflate the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatImageBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    /**
     * Initializes the view by inflating the layout and setting up initial
     * properties.
     *
     * @param context      The context in which the view is created.
     * @param attrs        The attribute set used to inflate the view.
     * @param defStyleAttr The default style attribute.
     */
    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        // Inflate and set up the view
        View view = View.inflate(context, R.layout.cometchat_image_bubble, null);
        shapeableImageView = view.findViewById(R.id.image);
        shapeableImageView.setTransitionName(getContext().getString(R.string.cometchat_shared_image_transition, 0));
        imageViewContainerCard = view.findViewById(R.id.image_view_container_card);
        progressBar = view.findViewById(R.id.loader_icon);
        textView = view.findViewById(R.id.caption);
        textView.setVisibility(GONE);
        Utils.initMaterialCard(imageViewContainerCard);
        Utils.initMaterialCard(this);
        // Add the inflated view to this view
        addView(view);

        // Set up click listeners
        shapeableImageView.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.onClick();
            } else {
                openMediaViewActivity();
            }
        });

        shapeableImageView.setOnLongClickListener(v -> {
            Utils.performAdapterClick(v);
            return false;
        });

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Opens the media view activity to display the image in full screen.
     */
    private void openMediaViewActivity() {
        if (mediaMessage == null && (imageUrl == null || imageUrl.isEmpty())) {
            CometChatLogger.e(TAG, "MediaMessage is null and imageUrl is empty");
            return;
        }
        Utils.openImageViewer(shapeableImageView,
                              Collections.singletonList(imageUrl),
                              Collections.singletonList(mediaMessage.getAttachment().getFileMimeType()),
                              Collections.singletonList(mediaMessage.getAttachment().getFileName()));
    }

    /**
     * Applies style attributes from XML or default styles.
     *
     * @param attrs        The attribute set containing style attributes.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatImageBubble, defStyleAttr, defStyleRes);
        int styleResId = typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatImageBubble, defStyleRes, styleResId);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts style attributes and applies default values.
     *
     * @param typedArray The typed array containing style attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setCornerRadius((int) typedArray.getDimension(R.styleable.CometChatImageBubble_cometchatImageBubbleImageCornerRadius, 0));
            setStrokeColor((int) typedArray.getDimension(R.styleable.CometChatImageBubble_cometchatImageBubbleImageStrokeColor, 0));
            setStrokeWidth((int) typedArray.getDimension(R.styleable.CometChatImageBubble_cometchatImageBubbleImageStrokeWidth, 0));
            setCaptionTextAppearance(typedArray.getResourceId(R.styleable.CometChatImageBubble_cometchatImageBubbleCaptionTextAppearance, 0));
            setCaptionTextColor(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleCaptionTextColor, 0));
            setProgressIndeterminateTint(typedArray.getColor(R.styleable.CometChatImageBubble_cometchatImageBubbleProgressIndeterminateTint, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the stroke color for the image view container card.
     *
     * @param color The color of the stroke to be set.
     */
    @Override
    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        this.imageViewContainerCard.setStrokeColor(ColorStateList.valueOf(strokeColor));
        super.setStrokeColor(ColorStateList.valueOf(strokeColor));
    }

    /**
     * Sets the text appearance for the caption.
     *
     * @param appearance The text appearance style resource.
     */
    public void setCaptionTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            textView.setTextAppearance(appearance);
        }
    }

    /**
     * Sets the text color for the caption.
     *
     * @param color The color value to be set.
     */
    public void setCaptionTextColor(@ColorInt int color) {
        if (color != 0) {
            textView.setTextColor(color);
        }
    }

    @Override
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    private ActivityOptionsCompat getActivityOption(View targetView) {
        return ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, targetView, targetView.getTransitionName());
    }

    /**
     * Sets the caption for the image bubble.
     *
     * @param caption The caption text to be set.
     */
    public void setCaption(String caption) {
        if (caption != null && !caption.isEmpty()) {
            textView.setVisibility(VISIBLE);
            textView.setText(caption);
        } else textView.setVisibility(GONE);
    }

    /**
     * Sets the caption for the image bubble.
     *
     * @param caption The caption text to be set.
     */
    public void setCaption(SpannableString caption) {
        if (caption != null) {
            textView.setVisibility(VISIBLE);
            textView.setText(caption);
        } else textView.setVisibility(GONE);
    }

    /**
     * Sets the thumbnail of the image using a URL. The thumbnail image is loaded
     * asynchronously using the Glide library.
     *
     * @param url The URL of the thumbnail image to be displayed.
     */
    public void setImageThumbnail(String url) {
        if (url != null && !url.isEmpty()) {
            glideLoadImageFromUrl(url);
        }
    }

    /**
     * Loads an image from the specified URL into the ImageView using Glide.
     *
     * <p>
     * This method delegates the loading task to
     * {@link #loadBitmapIntoImageView(File, String)} with the provided URL and a
     * null file parameter. It sets up the image loading process to handle the image
     * fetched from the internet.
     *
     * @param url The URL of the image to be loaded.
     */
    private void glideLoadImageFromUrl(String url) {
        loadBitmapIntoImageView(null, url);
    }

    /**
     * Loads a bitmap image into the ImageView from either a file or a URL.
     *
     * <p>
     * This method uses Glide to load a bitmap image from the specified file or URL
     * into the associated ImageView. It handles caching, placeholder, and
     * visibility of the progress bar during the loading process.
     *
     * @param file The File object representing the image file, or null to load from
     *             a URL.
     * @param url  The URL of the image to be loaded, or null to load from a file.
     */
    private void loadBitmapIntoImageView(File file, String url) {
        RequestBuilder<Bitmap> builder = Glide.with(context).asBitmap();
        builder
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(0)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .load(file != null && file.exists() ? file : url)
            .addListener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object o, @NonNull Target<Bitmap> target, boolean b) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Bitmap bitmap,
                                               @NonNull Object o,
                                               Target<Bitmap> target,
                                               @NonNull DataSource dataSource,
                                               boolean b) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .into(shapeableImageView);
    }

    public ImageView getShapeableImageView() {
        return shapeableImageView;
    }

    public MaterialCardView getImageViewContainerCard() {
        return imageViewContainerCard;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTextView() {
        return textView;
    }

    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the click listener for the image bubble.
     *
     * @param onClick The OnClick listener to be set.
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the image view container card.
     *
     * @param color The color to be set as the background color.
     */
    @Override
    public void setBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        this.imageViewContainerCard.setCardBackgroundColor(backgroundColor);
        super.setBackgroundColor(backgroundColor);
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the image view container card.
     *
     * @param radius The radius of the corners to be set.
     */
    public void setCornerRadius(@Dimension int radius) {
        this.cornerRadius = radius;
        this.imageViewContainerCard.setRadius(cornerRadius);
        super.setRadius(cornerRadius);
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable for the image view container card.
     *
     * @param drawable The drawable to be set as the background.
     */
    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        this.backgroundDrawable = drawable;
        if (imageViewContainerCard != null)
            this.imageViewContainerCard.setBackground(backgroundDrawable);
        super.setBackgroundDrawable(backgroundDrawable);
    }

    public @ColorInt int getProgressIndeterminateTint() {
        return progressIndeterminateTint;
    }

    /**
     * Sets the tint color for the progress bar.
     *
     * @param progressIndeterminateTint The color value to be set.
     */
    public void setProgressIndeterminateTint(int progressIndeterminateTint) {
        this.progressIndeterminateTint = progressIndeterminateTint;
        progressBar.getIndeterminateDrawable().setColorFilter(progressIndeterminateTint, android.graphics.PorterDuff.Mode.SRC_IN);
    }    /**
     * Sets the stroke width for the image view container card.
     *
     * @param width The width of the stroke to be set.
     */
    @Override
    public void setStrokeWidth(@Dimension int width) {
        this.strokeWidth = width;
        this.imageViewContainerCard.setStrokeWidth(strokeWidth);
    }

    public int getStyle() {
        return style;
    }

    /**
     * Sets the style for the image bubble from a style resource.
     *
     * @param styleResId The style resource ID.
     */
    public void setStyle(@StyleRes int styleResId) {
        this.style = styleResId;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatImageBubble);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Method to set MediaMessage
     *
     * @param mediaMessage
     */

    public void setMessage(MediaMessage mediaMessage) {
        this.mediaMessage = mediaMessage;
        setMessage(mediaMessage, null);
    }

    /**
     * Method to set MediaMessage and File
     *
     * @param mediaMessage
     * @param file
     */

    public void setMessage(MediaMessage mediaMessage, File file) {
        this.mediaMessage = mediaMessage;
        Attachment attachment = mediaMessage.getAttachment();
        setImageUrl(file,
                    attachment != null ? attachment.getFileUrl() : "",
                    attachment != null ? attachment.getFileExtension().equalsIgnoreCase("gif") : Utils.isGifFile(file));
    }

    /**
     * Sets the image URL, placeholder image, and whether the image is a GIF. The
     * image is loaded asynchronously using the Glide library.
     *
     * @param file  The file containing the image to be displayed.
     * @param url   The URL of the image to be displayed.
     * @param isGif Determines if the image is a GIF or not.
     */
    public void setImageUrl(File file, String url, boolean isGif) {
        this.imageUrl = url;
        if (file != null && file.exists()) {
            if (!isGif) {
                glideLoadImageFromFile(file);
            } else {
                glideLoadGIFFromFile(file);
            }
        } else {
            setImageUrl(url, isGif);
        }
    }

    /**
     * Loads an image from the specified file into the ImageView using Glide.
     *
     * <p>
     * This method delegates the loading task to
     * {@link #loadBitmapIntoImageView(File, String)} with the provided file and a
     * null URL parameter. It sets up the image loading process to handle the image
     * retrieved from local storage.
     *
     * @param file The File object representing the image file to be loaded.
     */
    private void glideLoadImageFromFile(File file) {
        loadBitmapIntoImageView(file, null);
    }

    /**
     * Loads a GIF image from the specified file into the ImageView using Glide.
     *
     * <p>
     * This method delegates the loading task to
     * {@link #loadGifInToImageView(File, String)} with the provided file and a null
     * URL parameter. It sets up the GIF loading process to handle the GIF retrieved
     * from local storage.
     *
     * @param file The File object representing the GIF file to be loaded.
     */
    private void glideLoadGIFFromFile(File file) {
        loadGifInToImageView(file, null);
    }

    /**
     * Sets the image URL, placeholder image, and whether the image is a GIF. The
     * image is loaded asynchronously using the Glide library.
     *
     * @param url   The URL of the image to be displayed.
     * @param isGif Determines if the image is a GIF or not.
     */
    public void setImageUrl(String url, boolean isGif) {
        this.imageUrl = url;
        if (!isGif) {
            glideLoadImageFromUrl(url);
        } else {
            glideLoadGIFFromUrl(url);
        }
    }

    /**
     * Loads a GIF image into the ImageView from either a file or a URL.
     *
     * <p>
     * This method uses Glide to load a GIF image from the specified file or URL
     * into the associated ImageView. It handles caching, placeholder, and
     * visibility of the progress bar during the loading process.
     *
     * @param file     The File object representing the GIF file, or null to load from a
     *                 URL.
     * @param imageUrl The URL of the GIF to be loaded, or null to load from a file.
     */
    private void loadGifInToImageView(File file, String imageUrl) {
        RequestBuilder<GifDrawable> gifDrawableRequestBuilder = Glide.with(context).asGif();
        gifDrawableRequestBuilder
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(0)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .load(file != null && file.exists() ? file : imageUrl)
            .addListener(new RequestListener<GifDrawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<GifDrawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull GifDrawable resource,
                                               @NonNull Object model,
                                               Target<GifDrawable> target,
                                               @NonNull DataSource dataSource,
                                               boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .into(shapeableImageView);
    }

    /**
     * Loads a GIF image from the specified URL into the ImageView using Glide.
     *
     * <p>
     * This method delegates the loading task to
     * {@link #loadGifInToImageView(File, String)} with the provided URL and a null
     * file parameter. It sets up the GIF loading process to handle the GIF fetched
     * from the internet.
     *
     * @param url The URL of the GIF to be loaded.
     */
    private void glideLoadGIFFromUrl(String url) {
        loadGifInToImageView(null, url);
    }


    // Getters for testing or direct access if needed



    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }


}
