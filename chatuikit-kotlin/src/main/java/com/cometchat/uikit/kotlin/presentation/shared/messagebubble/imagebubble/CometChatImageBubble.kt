package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.gridlayout.widget.GridLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import java.io.File

/**
 * A custom view that displays an image bubble for messaging applications.
 *
 * This view supports:
 * - Single image display with optional caption
 * - Multiple images in a grid layout (1, 2, 2x2, or 2x2 with "+N" overlay)
 * - GIF support
 * - Loading state with progress indicator
 * - Customizable styling (corner radius, stroke, colors)
 *
 * Grid Layout Rules:
 * - 1 image: Full width single image
 * - 2 images: 2 columns side by side
 * - 3-4 images: 2x2 grid
 * - 5+ images: 2x2 grid with "+N" overlay on 4th item
 */
class CometChatImageBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatImageBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatImageBubble::class.java.simpleName
        private const val MAX_VISIBLE_ITEMS = 4
        private const val GRID_SPACING_DP = 2
    }

    // Views from XML layout
    private lateinit var parentLayout: LinearLayout
    private lateinit var singleImageContainer: MaterialCardView
    private lateinit var singleImageView: ShapeableImageView
    private lateinit var singleProgressBar: ProgressBar
    private lateinit var captionTextView: TextView

    // Programmatic views for grid layout (not in XML)
    private lateinit var gridContainer: FrameLayout
    private lateinit var gridLayout: GridLayout

    // State
    private var mediaMessage: MediaMessage? = null
    private var attachments: List<Attachment> = emptyList()
    private var onClick: OnClick? = null
    private var onImageClick: ((Int, Attachment) -> Unit)? = null
    private var onMoreClick: ((List<Attachment>) -> Unit)? = null

    // Single style object - NO individual style properties
    // Using nullable to handle parent class initialization calling setBackgroundDrawable before init
    private var style: CometChatImageBubbleStyle? = null

    // Non-style properties (grid layout specific)
    @ColorInt private var moreOverlayBackgroundColor: Int = 0
    @ColorInt private var moreOverlayTextColor: Int = 0
    @StyleRes private var moreOverlayTextAppearance: Int = 0
    @Dimension private var gridSpacing: Int = 0
    @Dimension private var maxGridWidth: Int = 0

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        
        // Inflate XML layout
        LayoutInflater.from(context).inflate(R.layout.cometchat_image_bubble, this, true)
        
        // Bind views from XML
        parentLayout = findViewById(R.id.parent)
        singleImageContainer = findViewById(R.id.image_view_container_card)
        singleImageView = findViewById(R.id.image)
        singleProgressBar = findViewById(R.id.loader_icon)
        captionTextView = findViewById(R.id.caption)
        
        // Create grid container for multiple images (programmatic, not in XML)
        gridContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }

        gridLayout = GridLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        gridContainer.addView(gridLayout)
        
        // Add grid container to parent layout at the beginning
        parentLayout.addView(gridContainer, 0)

        // Set up click listeners
        singleImageView.setOnClickListener {
            if (onClick != null) {
                onClick?.onClick()
            } else {
                openMediaViewActivity()
            }
        }

        singleImageView.setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr, 0)
        applyDefaultStyles()
    }

    private fun applyDefaultStyles() {
        gridSpacing = Utils.convertDpToPx(context, GRID_SPACING_DP)
        maxGridWidth = resources.getDimensionPixelSize(R.dimen.cometchat_240dp)
        moreOverlayBackgroundColor = 0x99000000.toInt() // Semi-transparent black
        moreOverlayTextColor = CometChatTheme.getColorWhite(context)
        
        // Set fixed dimensions to match Java reference: 240dp × 232dp (cometchat_image_bubble_layout_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            resources.getDimensionPixelSize(R.dimen.cometchat_232dp)
        )
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatImageBubble, defStyleAttr, defStyleRes
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatImageBubble_cometchatImageBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatImageBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatImageBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        val currentStyle = style ?: return
        // Bubble container styling
        if (currentStyle.cornerRadius != 0f) applyCornerRadius(currentStyle.cornerRadius)
        if (currentStyle.strokeWidth != 0f) applyStrokeWidth(currentStyle.strokeWidth)
        if (currentStyle.strokeColor != 0) applyStrokeColor(currentStyle.strokeColor)
        currentStyle.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Image styling
        if (currentStyle.imageCornerRadius != 0f) applyImageCornerRadius(currentStyle.imageCornerRadius)
        if (currentStyle.imageStrokeWidth != 0f) applyImageStrokeWidth(currentStyle.imageStrokeWidth)
        if (currentStyle.imageStrokeColor != 0) applyImageStrokeColor(currentStyle.imageStrokeColor)

        // Caption styling
        if (currentStyle.captionTextColor != 0) applyCaptionTextColor(currentStyle.captionTextColor)
        if (currentStyle.captionTextAppearance != 0) applyCaptionTextAppearance(currentStyle.captionTextAppearance)

        // Progress indicator
        if (currentStyle.progressIndeterminateTint != 0) applyProgressIndeterminateTint(currentStyle.progressIndeterminateTint)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyBackgroundColor(@ColorInt color: Int) {
        super.setCardBackgroundColor(color)
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        super.setRadius(radius)
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        super.setStrokeWidth(width.toInt())
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        super.setStrokeColor(color)
    }

    private fun applyBackgroundDrawable(drawable: Drawable) {
        super.setBackgroundDrawable(drawable)
    }

    private fun applyImageCornerRadius(@Dimension radius: Float) {
        singleImageContainer.radius = radius
    }

    private fun applyImageStrokeWidth(@Dimension width: Float) {
        singleImageContainer.strokeWidth = width.toInt()
    }

    private fun applyImageStrokeColor(@ColorInt color: Int) {
        singleImageContainer.strokeColor = color
    }

    private fun applyCaptionTextColor(@ColorInt color: Int) {
        captionTextView.setTextColor(color)
    }

    private fun applyCaptionTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            captionTextView.setTextAppearance(appearance)
        }
    }

    private fun applyProgressIndeterminateTint(@ColorInt color: Int) {
        singleProgressBar.indeterminateDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the media message to display.
     * Automatically handles single or multiple attachments.
     *
     * @param mediaMessage The MediaMessage containing image attachment(s)
     */
    fun setMessage(mediaMessage: MediaMessage) {
        setMessage(mediaMessage, null)
    }

    /**
     * Sets the media message with an optional local file.
     *
     * @param mediaMessage The MediaMessage containing image attachment(s)
     * @param localFile Optional local file for the image
     */
    fun setMessage(mediaMessage: MediaMessage, localFile: File?) {
        this.mediaMessage = mediaMessage
        
        // Check for multiple attachments in metadata
        val multipleAttachments = extractAttachmentsFromMetadata(mediaMessage)
        
        if (multipleAttachments != null && multipleAttachments.size > 1) {
            // Multiple images - show grid
            setAttachments(multipleAttachments)
        } else {
            // Single image
            val attachment = mediaMessage.attachment
            if (attachment != null) {
                attachments = listOf(attachment)
            }
            showSingleImage(localFile, attachment)
        }
    }

    /**
     * Sets multiple attachments to display in a grid layout.
     *
     * @param attachments List of Attachment objects to display
     */
    fun setAttachments(attachments: List<Attachment>) {
        this.attachments = attachments
        
        if (attachments.size == 1) {
            showSingleImage(null, attachments[0])
        } else {
            showGridLayout(attachments)
        }
    }

    /**
     * Extracts multiple attachments from message metadata if available.
     */
    private fun extractAttachmentsFromMetadata(message: MediaMessage): List<Attachment>? {
        return try {
            val metadata = message.metadata ?: return null
            if (metadata.has("attachments")) {
                val attachmentsArray = metadata.getJSONArray("attachments")
                val result = mutableListOf<Attachment>()
                for (i in 0 until attachmentsArray.length()) {
                    val attachmentJson = attachmentsArray.getJSONObject(i)
                    val attachment = Attachment().apply {
                        fileUrl = attachmentJson.optString("url", "")
                        fileName = attachmentJson.optString("fileName", "")
                        fileExtension = attachmentJson.optString("extension", "")
                        fileMimeType = attachmentJson.optString("mimeType", "")
                        fileSize = attachmentJson.optLong("size", 0).toInt()
                    }
                    result.add(attachment)
                }
                if (result.isNotEmpty()) result else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting attachments from metadata: ${e.message}")
            null
        }
    }

    /**
     * Extracts the thumbnail URL from the message metadata if available.
     * Looks for the thumbnail-generation extension at:
     * metadata.@injected.extensions.thumbnail-generation.url_medium
     *
     * @param message The MediaMessage to extract thumbnail URL from
     * @return The thumbnail URL if found, null otherwise
     */
    private fun extractThumbnailUrlFromMetadata(message: MediaMessage): String? {
        return try {
            val metadata = message.metadata ?: return null
            val injected = metadata.optJSONObject("@injected") ?: return null
            val extensions = injected.optJSONObject("extensions") ?: return null
            val thumbnailGeneration = extensions.optJSONObject("thumbnail-generation") ?: return null
            val urlMedium = thumbnailGeneration.optString("url_medium", null)
            if (urlMedium.isNullOrEmpty()) null else urlMedium
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting thumbnail URL from metadata: ${e.message}")
            null
        }
    }

    /**
     * Extracts the local file path from the message metadata if available.
     * The local file path is stored in metadata.path during upload.
     *
     * @param message The MediaMessage to extract local file from
     * @return The local File if found and exists, null otherwise
     */
    private fun getLocalFileFromMetadata(message: MediaMessage): File? {
        return try {
            val metadata = message.metadata ?: return null
            val path = metadata.optString("path", null)
            if (path.isNullOrEmpty()) return null
            val file = File(path)
            if (file.exists()) file else null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting local file from metadata: ${e.message}")
            null
        }
    }

    // ========================================
    // Single Image Display
    // ========================================

    private fun showSingleImage(localFile: File?, attachment: Attachment?) {
        gridContainer.visibility = View.GONE
        singleImageContainer.visibility = View.VISIBLE
        singleProgressBar.visibility = View.VISIBLE

        val url = attachment?.fileUrl ?: ""
        
        // Try to get local file from metadata if not provided (for in-progress uploads)
        val effectiveLocalFile = localFile ?: mediaMessage?.let { getLocalFileFromMetadata(it) }
        
        val isGif = attachment?.fileExtension?.equals("gif", ignoreCase = true) == true ||
                    Utils.isGifFile(effectiveLocalFile)

        // Priority: 1. Local file (provided or from metadata), 2. Thumbnail URL from metadata, 3. Image file URL
        if (effectiveLocalFile != null && effectiveLocalFile.exists()) {
            if (isGif) {
                loadGifFromFile(effectiveLocalFile, singleImageView, singleProgressBar)
            } else {
                loadBitmapFromFile(effectiveLocalFile, singleImageView, singleProgressBar)
            }
        } else {
            // Try to get thumbnail URL from metadata first
            val thumbnailUrl = mediaMessage?.let { extractThumbnailUrlFromMetadata(it) }
            if (!thumbnailUrl.isNullOrEmpty()) {
                // Use thumbnail URL from metadata (handles both images and videos with generated thumbnails)
                if (isGif) {
                    loadGifFromUrl(thumbnailUrl, singleImageView, singleProgressBar)
                } else {
                    loadBitmapFromUrl(thumbnailUrl, singleImageView, singleProgressBar)
                }
            } else if (url.isNotEmpty()) {
                // Fall back to image file URL
                if (isGif) {
                    loadGifFromUrl(url, singleImageView, singleProgressBar)
                } else {
                    loadBitmapFromUrl(url, singleImageView, singleProgressBar)
                }
            } else {
                singleProgressBar.visibility = View.GONE
                singleImageView.setImageResource(R.drawable.cometchat_image_placeholder)
            }
        }
    }

    /**
     * Sets the thumbnail image from a URL.
     */
    fun setImageThumbnail(url: String) {
        if (url.isNotEmpty()) {
            loadBitmapFromUrl(url, singleImageView, singleProgressBar)
        }
    }

    // ========================================
    // Grid Layout Display
    // ========================================

    private fun showGridLayout(attachments: List<Attachment>) {
        singleImageContainer.visibility = View.GONE
        gridContainer.visibility = View.VISIBLE
        gridLayout.removeAllViews()

        val count = attachments.size
        val visibleCount = minOf(count, MAX_VISIBLE_ITEMS)
        val columns = if (count == 1) 1 else 2

        gridLayout.columnCount = columns
        gridLayout.rowCount = (visibleCount + columns - 1) / columns

        val itemSize = calculateGridItemSize(columns)

        for (i in 0 until visibleCount) {
            val itemView = createGridItem(attachments[i], i, itemSize)
            
            // Add "+N" overlay on last item if more exist
            if (i == MAX_VISIBLE_ITEMS - 1 && count > MAX_VISIBLE_ITEMS) {
                addMoreOverlay(itemView, count - MAX_VISIBLE_ITEMS)
            }

            val params = GridLayout.LayoutParams().apply {
                width = itemSize
                height = itemSize
                setMargins(gridSpacing / 2, gridSpacing / 2, gridSpacing / 2, gridSpacing / 2)
            }
            itemView.layoutParams = params
            gridLayout.addView(itemView)
        }
    }

    private fun calculateGridItemSize(columns: Int): Int {
        val availableWidth = maxGridWidth - (gridSpacing * (columns + 1))
        return availableWidth / columns
    }

    private fun createGridItem(attachment: Attachment, index: Int, size: Int): FrameLayout {
        val container = FrameLayout(context)
        val currentStyle = style ?: CometChatImageBubbleStyle()

        val imageCard = MaterialCardView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            radius = currentStyle.imageCornerRadius
        }
        Utils.initMaterialCard(imageCard)

        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val progressBar = ProgressBar(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.cometchat_24dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_24dp)
            ).apply {
                gravity = Gravity.CENTER
            }
            if (currentStyle.progressIndeterminateTint != 0) {
                indeterminateDrawable.setColorFilter(currentStyle.progressIndeterminateTint, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        val imageFrame = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        imageFrame.addView(imageView)
        imageFrame.addView(progressBar)
        imageCard.addView(imageFrame)
        container.addView(imageCard)

        // Load image
        val url = attachment.fileUrl ?: ""
        val isGif = attachment.fileExtension?.equals("gif", ignoreCase = true) == true

        if (url.isNotEmpty()) {
            if (isGif) {
                loadGifFromUrl(url, imageView, progressBar)
            } else {
                loadBitmapFromUrl(url, imageView, progressBar)
            }
        } else {
            progressBar.visibility = View.GONE
            imageView.setImageResource(R.drawable.cometchat_image_placeholder)
        }

        // Set click listener
        container.setOnClickListener {
            onImageClick?.invoke(index, attachment)
        }

        container.setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        return container
    }

    private fun addMoreOverlay(itemView: FrameLayout, moreCount: Int) {
        val overlay = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(moreOverlayBackgroundColor)
        }

        val moreText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            text = "+$moreCount"
            setTextColor(moreOverlayTextColor)
            if (moreOverlayTextAppearance != 0) {
                setTextAppearance(moreOverlayTextAppearance)
            } else {
                textSize = 18f
            }
        }

        overlay.addView(moreText)
        itemView.addView(overlay)

        // Update click listener to show all images
        itemView.setOnClickListener {
            onMoreClick?.invoke(attachments)
        }
    }

    // ========================================
    // Image Loading with Glide
    // ========================================

    private fun loadBitmapFromUrl(url: String, imageView: ImageView, progressBar: ProgressBar) {
        Log.d(TAG, "Loading bitmap from URL: $url")
        Glide.with(imageView)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createBitmapListener(progressBar))
            .into(imageView)
    }

    private fun loadBitmapFromFile(file: File, imageView: ImageView, progressBar: ProgressBar) {
        Log.d(TAG, "Loading bitmap from file: ${file.absolutePath}")
        Glide.with(imageView)
            .asBitmap()
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createBitmapListener(progressBar))
            .into(imageView)
    }

    private fun loadGifFromUrl(url: String, imageView: ImageView, progressBar: ProgressBar) {
        Glide.with(context)
            .asGif()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createGifListener(progressBar))
            .into(imageView)
    }

    private fun loadGifFromFile(file: File, imageView: ImageView, progressBar: ProgressBar) {
        Glide.with(context)
            .asGif()
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createGifListener(progressBar))
            .into(imageView)
    }

    private fun createBitmapListener(progressBar: ProgressBar): RequestListener<Bitmap> {
        return object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                Log.e(TAG, "Image load failed for URL: $model", e)
                progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                Log.d(TAG, "Image loaded successfully: ${resource.width}x${resource.height} from $model")
                progressBar.visibility = View.GONE
                return false
            }
        }
    }

    private fun createGifListener(progressBar: ProgressBar): RequestListener<GifDrawable> {
        return object : RequestListener<GifDrawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<GifDrawable>,
                isFirstResource: Boolean
            ): Boolean {
                Log.e(TAG, "GIF load failed for URL: $model", e)
                progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: GifDrawable,
                model: Any,
                target: Target<GifDrawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                Log.d(TAG, "GIF loaded successfully from $model")
                progressBar.visibility = View.GONE
                return false
            }
        }
    }

    // ========================================
    // Caption
    // ========================================

    /**
     * Sets the caption text for the image bubble.
     */
    fun setCaption(caption: String?) {
        if (!caption.isNullOrEmpty()) {
            captionTextView.visibility = View.VISIBLE
            captionTextView.text = caption
        } else {
            captionTextView.visibility = View.GONE
        }
    }

    /**
     * Sets the caption text using a SpannableString.
     */
    fun setCaption(caption: SpannableString?) {
        if (caption != null) {
            captionTextView.visibility = View.VISIBLE
            captionTextView.text = caption
        } else {
            captionTextView.visibility = View.GONE
        }
    }

    // ========================================
    // Media Viewer
    // ========================================

    private fun openMediaViewActivity() {
        if (mediaMessage == null && attachments.isEmpty()) {
            Log.e(TAG, "No media to display")
            return
        }

        val urls = attachments.map { it.fileUrl ?: "" }
        val mimeTypes = attachments.map { it.fileMimeType ?: "" }
        val fileNames = attachments.map { it.fileName ?: "" }

        Utils.openImageViewer(singleImageView, urls, mimeTypes, fileNames)
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatImageBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatImageBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatImageBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getImageCornerRadius(): Float = style?.imageCornerRadius ?: 0f
    fun getImageStrokeColor(): Int = style?.imageStrokeColor ?: 0
    fun getImageStrokeWidth(): Float = style?.imageStrokeWidth ?: 0f
    fun getCaptionTextColor(): Int = style?.captionTextColor ?: 0
    fun getCaptionTextAppearance(): Int = style?.captionTextAppearance ?: 0
    fun getProgressIndeterminateTint(): Int = style?.progressIndeterminateTint ?: 0
    fun getBubbleBackgroundColor(): Int = style?.backgroundColor ?: 0
    fun getBubbleCornerRadius(): Float = style?.cornerRadius ?: 0f
    fun getBubbleStrokeWidth(): Float = style?.strokeWidth ?: 0f
    fun getBubbleStrokeColor(): Int = style?.strokeColor ?: 0
    fun getBackgroundDrawable(): Drawable? = style?.backgroundDrawable

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setImageCornerRadius(@Dimension radius: Float) {
        style = (style ?: CometChatImageBubbleStyle()).copy(imageCornerRadius = radius)
        applyImageCornerRadius(radius)
    }

    fun setImageStrokeColor(@ColorInt color: Int) {
        style = (style ?: CometChatImageBubbleStyle()).copy(imageStrokeColor = color)
        applyImageStrokeColor(color)
    }

    fun setImageStrokeWidth(@Dimension width: Float) {
        style = (style ?: CometChatImageBubbleStyle()).copy(imageStrokeWidth = width)
        applyImageStrokeWidth(width)
    }

    fun setCaptionTextColor(@ColorInt color: Int) {
        style = (style ?: CometChatImageBubbleStyle()).copy(captionTextColor = color)
        applyCaptionTextColor(color)
    }

    fun setCaptionTextAppearance(@StyleRes appearance: Int) {
        style = (style ?: CometChatImageBubbleStyle()).copy(captionTextAppearance = appearance)
        applyCaptionTextAppearance(appearance)
    }

    fun setProgressIndeterminateTint(@ColorInt color: Int) {
        style = (style ?: CometChatImageBubbleStyle()).copy(progressIndeterminateTint = color)
        applyProgressIndeterminateTint(color)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = (style ?: CometChatImageBubbleStyle()).copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {
        // Guard against calls during parent class initialization before style is initialized
        if (style == null) {
            super.setBackgroundDrawable(drawable)
            return
        }
        style = (style ?: CometChatImageBubbleStyle()).copy(backgroundDrawable = drawable)
        drawable?.let { applyBackgroundDrawable(it) }
    }

    fun getBackgroundColor(): Int = style?.backgroundColor ?: 0

    fun setMoreOverlayBackgroundColor(@ColorInt color: Int) {
        this.moreOverlayBackgroundColor = color
    }

    fun getMoreOverlayBackgroundColor(): Int = moreOverlayBackgroundColor

    fun setMoreOverlayTextColor(@ColorInt color: Int) {
        this.moreOverlayTextColor = color
    }

    fun getMoreOverlayTextColor(): Int = moreOverlayTextColor

    fun setMoreOverlayTextAppearance(@StyleRes appearance: Int) {
        this.moreOverlayTextAppearance = appearance
    }

    fun getMoreOverlayTextAppearance(): Int = moreOverlayTextAppearance

    fun setGridSpacing(@Dimension spacing: Int) {
        this.gridSpacing = spacing
    }

    fun getGridSpacing(): Int = gridSpacing

    fun setMaxGridWidth(@Dimension width: Int) {
        this.maxGridWidth = width
    }

    fun getMaxGridWidth(): Int = maxGridWidth

    // ========================================
    // Click Listeners
    // ========================================

    fun setOnClick(onClick: OnClick?) {
        this.onClick = onClick
    }

    fun getOnClick(): OnClick? = onClick

    fun setOnImageClickListener(listener: ((Int, Attachment) -> Unit)?) {
        this.onImageClick = listener
    }

    fun setOnMoreClickListener(listener: ((List<Attachment>) -> Unit)?) {
        this.onMoreClick = listener
    }

    // ========================================
    // View Accessors
    // ========================================

    fun getSingleImageView(): ImageView = singleImageView

    fun getSingleImageContainer(): MaterialCardView = singleImageContainer

    fun getProgressBar(): ProgressBar = singleProgressBar

    fun getCaptionTextView(): TextView = captionTextView

    fun getGridLayout(): GridLayout = gridLayout

    fun getAttachments(): List<Attachment> = attachments

    fun getMediaMessage(): MediaMessage? = mediaMessage

    // ========================================
    // Lifecycle
    // ========================================

    /**
     * Cancels any ongoing image loads. Call this when the view is recycled.
     */
    fun cancelImageLoads() {
        try {
            Glide.with(context).clear(singleImageView)
            // Clear grid images
            for (i in 0 until gridLayout.childCount) {
                val child = gridLayout.getChildAt(i)
                if (child is ViewGroup) {
                    clearImagesInViewGroup(child)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling image loads: ${e.message}")
        }
    }

    private fun clearImagesInViewGroup(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ImageView) {
                try {
                    Glide.with(context).clear(child)
                } catch (e: Exception) {
                    // Ignore
                }
            } else if (child is ViewGroup) {
                clearImagesInViewGroup(child)
            }
        }
    }
}
