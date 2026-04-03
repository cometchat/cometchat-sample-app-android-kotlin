package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble

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
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.gridlayout.widget.GridLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import java.io.File

/**
 * A custom view that displays a video bubble for messaging applications.
 *
 * This view supports:
 * - Single video display with thumbnail and play button overlay
 * - Multiple videos in a grid layout (1, 2, 2x2, or 2x2 with "+N" overlay)
 * - Caption text support
 * - Loading state with progress indicator
 * - Customizable styling (corner radius, stroke, colors, play icon)
 *
 * Grid Layout Rules:
 * - 1 video: Full width single video
 * - 2 videos: 2 columns side by side
 * - 3-4 videos: 2x2 grid
 * - 5+ videos: 2x2 grid with "+N" overlay on 4th item
 */
class CometChatVideoBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatVideoBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatVideoBubble::class.java.simpleName
        private const val MAX_VISIBLE_ITEMS = 4
        private const val GRID_SPACING_DP = 2
    }

    // Views from XML layout
    private lateinit var videoContainerCard: MaterialCardView
    private lateinit var parentLayout: LinearLayout
    private lateinit var videoImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var playButtonLayout: FrameLayout
    private lateinit var playButtonBg: ImageView
    private lateinit var playButton: ImageView

    // Programmatic views for grid layout and caption (not in XML)
    private lateinit var rootLayout: LinearLayout
    private lateinit var gridContainer: FrameLayout
    private lateinit var gridLayout: GridLayout
    private lateinit var captionTextView: TextView

    // State
    private var mediaMessage: MediaMessage? = null
    private var attachments: List<Attachment> = emptyList()
    private var onClick: OnClick? = null
    private var onVideoClick: ((Int, Attachment) -> Unit)? = null
    private var onMoreClick: ((List<Attachment>) -> Unit)? = null
    private var videoUrl: String? = null
    private var file: File? = null

    // Single style object - NO individual style properties
    // Using nullable to handle parent class initialization calling setBackgroundDrawable before init
    private var style: CometChatVideoBubbleStyle? = null

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        
        // Set fixed dimensions to match Java reference: 240dp × 144dp (cometchat_video_bubble_layout_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            resources.getDimensionPixelSize(R.dimen.cometchat_144dp)
        )
        
        // Create root layout to hold both grid and single video views
        rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Create grid container for multiple videos (programmatic, not in XML)
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
        rootLayout.addView(gridContainer)

        // Inflate XML layout for single video
        val view = LayoutInflater.from(context).inflate(R.layout.cometchat_video_bubble, null)
        
        // Bind views from XML
        videoContainerCard = view.findViewById(R.id.video_view_container_card)
        parentLayout = view.findViewById(R.id.parent)
        videoImageView = view.findViewById(R.id.video)
        progressBar = view.findViewById(R.id.progress_bar)
        playButtonLayout = view.findViewById(R.id.play_button_layout)
        playButtonBg = view.findViewById(R.id.play_btn_bg)
        playButton = view.findViewById(R.id.play_btn)
        
        // Initialize video container card
        Utils.initMaterialCard(videoContainerCard)
        
        // Set initial visibility states
        progressBar.visibility = View.VISIBLE
        playButtonLayout.visibility = View.GONE
        
        // Wrap the inflated view in a container with proper layout params
        // Height matches Java reference: 144dp (cometchat_video_bubble_layout_container.xml)
        val singleVideoWrapper = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_144dp)
            )
        }
        singleVideoWrapper.addView(view)
        rootLayout.addView(singleVideoWrapper)

        // Create caption text view (programmatic, not in XML)
        captionTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                    resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
                )
            }
            visibility = View.GONE
            setTextIsSelectable(true)
        }
        rootLayout.addView(captionTextView)
        
        addView(rootLayout)

        // Set up click listeners
        playButtonLayout.setOnClickListener { invokeClick() }
        videoImageView.setOnClickListener { invokeClick() }
        videoImageView.setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr, 0)
        applyDefaultStyles()
    }

    private fun invokeClick() {
        if (onClick != null) {
            onClick?.onClick()
        } else if (attachments.isNotEmpty() && onVideoClick != null) {
            onVideoClick?.invoke(0, attachments[0])
        } else {
            openMediaViewActivity()
        }
    }

    private fun applyDefaultStyles() {
        // Default styles are now handled by the style object
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatVideoBubble, defStyleAttr, defStyleRes
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatVideoBubble_cometchatVideoBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatVideoBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatVideoBubbleStyle.fromTypedArray(context, typedArray)
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

        // Video thumbnail styling
        if (currentStyle.videoCornerRadius != 0f) applyVideoCornerRadius(currentStyle.videoCornerRadius)
        if (currentStyle.videoStrokeWidth != 0f) applyVideoStrokeWidth(currentStyle.videoStrokeWidth)
        if (currentStyle.videoStrokeColor != 0) applyVideoStrokeColor(currentStyle.videoStrokeColor)

        // Play button styling
        if (currentStyle.playIconTint != 0) applyPlayIconTint(currentStyle.playIconTint)
        if (currentStyle.playIconBackgroundColor != 0) applyPlayIconBackgroundColor(currentStyle.playIconBackgroundColor)

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
        setCardBackgroundColor(color)
    }

    private fun applyCornerRadius(radius: Float) {
        setRadius(radius)
    }

    private fun applyStrokeWidth(width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    private fun applyBackgroundDrawable(drawable: Drawable) {
        super.setBackgroundDrawable(drawable)
    }

    private fun applyVideoCornerRadius(radius: Float) {
        videoContainerCard.radius = radius
    }

    private fun applyVideoStrokeWidth(width: Float) {
        videoContainerCard.strokeWidth = width.toInt()
    }

    private fun applyVideoStrokeColor(@ColorInt color: Int) {
        videoContainerCard.strokeColor = color
    }

    private fun applyPlayIconTint(@ColorInt color: Int) {
        playButton.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun applyPlayIconBackgroundColor(@ColorInt color: Int) {
        playButtonBg.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
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
        progressBar.indeterminateDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the media message to display.
     * Automatically handles single or multiple attachments.
     *
     * @param mediaMessage The MediaMessage containing video attachment(s)
     */
    fun setMessage(mediaMessage: MediaMessage) {
        setMessage(mediaMessage, null)
    }

    /**
     * Sets the media message with an optional local file.
     *
     * @param mediaMessage The MediaMessage containing video attachment(s)
     * @param localFile Optional local file for the video thumbnail
     */
    fun setMessage(mediaMessage: MediaMessage, localFile: File?) {
        this.mediaMessage = mediaMessage
        
        // Check for multiple attachments in metadata
        val multipleAttachments = extractAttachmentsFromMetadata(mediaMessage)
        
        if (multipleAttachments != null && multipleAttachments.size > 1) {
            // Multiple videos - show grid
            setAttachments(multipleAttachments)
        } else {
            // Single video
            val attachment = mediaMessage.attachment
            if (attachment != null) {
                attachments = listOf(attachment)
            }
            showSingleVideo(localFile, attachment)
        }
        
        // Set caption from message
        setCaption(mediaMessage.caption)
    }

    /**
     * Sets multiple attachments to display in a grid layout.
     *
     * @param attachments List of Attachment objects to display
     */
    fun setAttachments(attachments: List<Attachment>) {
        this.attachments = attachments
        
        if (attachments.size == 1) {
            showSingleVideo(null, attachments[0])
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
    // Single Video Display
    // ========================================

    private fun showSingleVideo(localFile: File?, attachment: Attachment?) {
        gridContainer.visibility = View.GONE
        (videoContainerCard.parent?.parent as? View)?.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        playButtonLayout.visibility = View.GONE

        val url = attachment?.fileUrl ?: ""
        this.videoUrl = url
        this.file = localFile

        // Try to get local file from metadata if not provided (for in-progress uploads)
        val effectiveLocalFile = localFile ?: mediaMessage?.let { getLocalFileFromMetadata(it) }

        // Priority: 1. Local file (provided or from metadata), 2. Thumbnail URL from metadata, 3. Video file URL
        if (effectiveLocalFile != null && effectiveLocalFile.exists()) {
            loadThumbnailFromFile(effectiveLocalFile, videoImageView, progressBar, playButtonLayout)
        } else {
            // Try to get thumbnail URL from metadata first
            val thumbnailUrl = mediaMessage?.let { extractThumbnailUrlFromMetadata(it) }
            if (!thumbnailUrl.isNullOrEmpty()) {
                loadThumbnailFromUrl(thumbnailUrl, videoImageView, progressBar, playButtonLayout)
            } else if (url.isNotEmpty()) {
                loadThumbnailFromUrl(url, videoImageView, progressBar, playButtonLayout)
            } else {
                progressBar.visibility = View.GONE
                playButtonLayout.visibility = View.VISIBLE
                videoImageView.setImageResource(R.drawable.cometchat_image_placeholder)
            }
        }
    }

    /**
     * Sets the video URL and loads the thumbnail.
     *
     * @param file Optional local file for the video
     * @param videoUrl The URL of the video
     */
    fun setVideoUrl(file: File?, videoUrl: String) {
        this.videoUrl = videoUrl
        this.file = file
        loadThumbnailFromFileOrUrl(file, videoUrl)
    }

    private fun loadThumbnailFromFileOrUrl(file: File?, url: String) {
        progressBar.visibility = View.VISIBLE
        playButtonLayout.visibility = View.GONE
        
        if (file != null && file.exists()) {
            loadThumbnailFromFile(file, videoImageView, progressBar, playButtonLayout)
        } else if (url.isNotEmpty()) {
            loadThumbnailFromUrl(url, videoImageView, progressBar, playButtonLayout)
        } else {
            progressBar.visibility = View.GONE
            playButtonLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Sets the thumbnail URL and placeholder image for the video thumbnail.
     *
     * @param thumbnailUrl The URL of the thumbnail image
     * @param placeHolderImage The placeholder image resource ID
     */
    fun setThumbnailUrl(thumbnailUrl: String, @DrawableRes placeHolderImage: Int) {
        loadThumbnailFromUrl(thumbnailUrl, videoImageView, progressBar, playButtonLayout)
    }

    /**
     * Sets the thumbnail image from a URL.
     */
    fun setVideoThumbnail(url: String) {
        if (url.isNotEmpty()) {
            loadThumbnailFromUrl(url, videoImageView, progressBar, playButtonLayout)
        }
    }

    /**
     * Sets the play icon resource for the play button.
     *
     * @param playIcon The play icon resource ID
     */
    fun setPlayIcon(@DrawableRes playIcon: Int) {
        if (playIcon != 0) {
            playButton.setImageResource(playIcon)
        }
    }

    // ========================================
    // Grid Layout Display
    // ========================================

    private fun showGridLayout(attachments: List<Attachment>) {
        (videoContainerCard.parent?.parent as? View)?.visibility = View.GONE
        gridContainer.visibility = View.VISIBLE
        gridLayout.removeAllViews()

        val count = attachments.size
        val visibleCount = minOf(count, MAX_VISIBLE_ITEMS)
        val columns = if (count == 1) 1 else 2

        gridLayout.columnCount = columns
        gridLayout.rowCount = (visibleCount + columns - 1) / columns

        val itemSize = calculateGridItemSize(columns)
        val currentStyle = style ?: CometChatVideoBubbleStyle()

        for (i in 0 until visibleCount) {
            val itemView = createGridItem(attachments[i], i, itemSize)
            
            // Add "+N" overlay on last item if more exist
            if (i == MAX_VISIBLE_ITEMS - 1 && count > MAX_VISIBLE_ITEMS) {
                addMoreOverlay(itemView, count - MAX_VISIBLE_ITEMS)
            }

            val spacing = currentStyle.gridSpacing.toInt()
            val params = GridLayout.LayoutParams().apply {
                width = itemSize
                height = itemSize
                setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2)
            }
            itemView.layoutParams = params
            gridLayout.addView(itemView)
        }
    }

    private fun calculateGridItemSize(columns: Int): Int {
        val currentStyle = style ?: CometChatVideoBubbleStyle()
        val availableWidth = currentStyle.maxGridWidth.toInt() - (currentStyle.gridSpacing.toInt() * (columns + 1))
        return availableWidth / columns
    }

    private fun createGridItem(attachment: Attachment, index: Int, size: Int): FrameLayout {
        val container = FrameLayout(context)
        val currentStyle = style ?: CometChatVideoBubbleStyle()

        val videoCard = MaterialCardView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            radius = currentStyle.videoCornerRadius
        }
        Utils.initMaterialCard(videoCard)

        val thumbnailView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = context.getString(R.string.cometchat_video)
        }

        val gridProgressBar = ProgressBar(context).apply {
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

        // Create play overlay for grid item
        val playOverlay = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        val playIconBg = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.cometchat_32dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_32dp)
            ).apply {
                gravity = Gravity.CENTER
            }
            setImageResource(R.drawable.cometchat_play_background_icon)
            if (currentStyle.playIconBackgroundColor != 0) {
                setColorFilter(currentStyle.playIconBackgroundColor, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        val playIcon = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.cometchat_20dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_20dp)
            ).apply {
                gravity = Gravity.CENTER
            }
            setImageResource(R.drawable.cometchat_play_icon)
            if (currentStyle.playIconTint != 0) {
                setColorFilter(currentStyle.playIconTint, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            contentDescription = context.getString(R.string.cometchat_play)
        }

        playOverlay.addView(playIconBg)
        playOverlay.addView(playIcon)

        val videoFrame = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        videoFrame.addView(thumbnailView)
        videoFrame.addView(gridProgressBar)
        videoFrame.addView(playOverlay)
        videoCard.addView(videoFrame)
        container.addView(videoCard)

        // Load thumbnail
        val url = attachment.fileUrl ?: ""
        if (url.isNotEmpty()) {
            loadThumbnailFromUrl(url, thumbnailView, gridProgressBar, playOverlay)
        } else {
            gridProgressBar.visibility = View.GONE
            playOverlay.visibility = View.VISIBLE
            thumbnailView.setImageResource(R.drawable.cometchat_image_placeholder)
        }

        // Set click listener
        container.setOnClickListener {
            onVideoClick?.invoke(index, attachment)
        }

        container.setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        return container
    }

    private fun addMoreOverlay(itemView: FrameLayout, moreCount: Int) {
        val currentStyle = style ?: CometChatVideoBubbleStyle()
        val overlay = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(currentStyle.moreOverlayBackgroundColor)
        }

        val moreText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            text = "+$moreCount"
            setTextColor(currentStyle.moreOverlayTextColor)
            if (currentStyle.moreOverlayTextAppearance != 0) {
                setTextAppearance(currentStyle.moreOverlayTextAppearance)
            } else {
                textSize = 18f
            }
        }

        overlay.addView(moreText)
        itemView.addView(overlay)

        // Update click listener to show all videos
        itemView.setOnClickListener {
            onMoreClick?.invoke(attachments)
        }
    }

    // ========================================
    // Thumbnail Loading with Glide
    // ========================================

    private fun loadThumbnailFromUrl(url: String, imageView: ImageView, progressBar: ProgressBar, playOverlay: View) {
        Log.d(TAG, "Loading video thumbnail from URL: $url")
        Glide.with(imageView)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createThumbnailListener(progressBar, playOverlay))
            .into(imageView)
    }

    private fun loadThumbnailFromFile(file: File, imageView: ImageView, progressBar: ProgressBar, playOverlay: View) {
        Log.d(TAG, "Loading video thumbnail from file: ${file.absolutePath}")
        Glide.with(imageView)
            .asBitmap()
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.cometchat_image_placeholder)
            .error(R.drawable.cometchat_image_placeholder)
            .skipMemoryCache(false)
            .addListener(createThumbnailListener(progressBar, playOverlay))
            .into(imageView)
    }

    private fun createThumbnailListener(progressBar: ProgressBar, playOverlay: View): RequestListener<Bitmap> {
        return object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                Log.e(TAG, "Video thumbnail load failed for URL: $model", e)
                progressBar.visibility = View.GONE
                playOverlay.visibility = View.VISIBLE
                return false
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                Log.d(TAG, "Video thumbnail loaded successfully: ${resource.width}x${resource.height} from $model")
                progressBar.visibility = View.GONE
                playOverlay.visibility = View.VISIBLE
                return false
            }
        }
    }

    // ========================================
    // Caption
    // ========================================

    /**
     * Sets the caption text for the video bubble.
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
        if (mediaMessage == null && attachments.isEmpty() && videoUrl.isNullOrEmpty()) {
            Log.e(TAG, "No media to display")
            return
        }

        val url = videoUrl ?: attachments.firstOrNull()?.fileUrl ?: ""
        val mimeType = attachments.firstOrNull()?.fileMimeType ?: "video/*"
        
        if (url.isNotEmpty()) {
            // Open video in external player (matches Java MediaUtils.openMediaInPlayer)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(android.net.Uri.parse(url), mimeType)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } else if (file != null && file!!.exists()) {
            com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils.openFile(context, file!!)
        }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatVideoBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatVideoBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatVideoBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getVideoCornerRadius(): Float = style?.videoCornerRadius ?: 0f
    fun getPlayIconTint(): Int = style?.playIconTint ?: 0
    fun getPlayIconBackgroundColor(): Int = style?.playIconBackgroundColor ?: 0
    fun getProgressIndeterminateTint(): Int = style?.progressIndeterminateTint ?: 0
    fun getCaptionTextColor(): Int = style?.captionTextColor ?: 0
    fun getCaptionTextAppearance(): Int = style?.captionTextAppearance ?: 0
    fun getMoreOverlayBackgroundColor(): Int = style?.moreOverlayBackgroundColor ?: 0
    fun getMoreOverlayTextColor(): Int = style?.moreOverlayTextColor ?: 0
    fun getMoreOverlayTextAppearance(): Int = style?.moreOverlayTextAppearance ?: 0
    fun getGridSpacing(): Float = style?.gridSpacing ?: 0f
    fun getMaxGridWidth(): Float = style?.maxGridWidth ?: 0f
    fun getBackgroundColor(): Int = style?.backgroundColor ?: 0
    fun getBackgroundDrawable(): Drawable? = style?.backgroundDrawable
    fun getVideoUrl(): String? = videoUrl

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setVideoCornerRadius(@Dimension radius: Float) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(videoCornerRadius = radius)
        applyVideoCornerRadius(radius)
    }

    fun setPlayIconTint(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(playIconTint = color)
        applyPlayIconTint(color)
    }

    fun setPlayIconBackgroundColor(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(playIconBackgroundColor = color)
        applyPlayIconBackgroundColor(color)
    }

    fun setProgressIndeterminateTint(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(progressIndeterminateTint = color)
        applyProgressIndeterminateTint(color)
    }

    fun setCaptionTextColor(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(captionTextColor = color)
        applyCaptionTextColor(color)
    }

    fun setCaptionTextAppearance(@StyleRes appearance: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(captionTextAppearance = appearance)
        applyCaptionTextAppearance(appearance)
    }

    fun setMoreOverlayBackgroundColor(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(moreOverlayBackgroundColor = color)
    }

    fun setMoreOverlayTextColor(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(moreOverlayTextColor = color)
    }

    fun setMoreOverlayTextAppearance(@StyleRes appearance: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(moreOverlayTextAppearance = appearance)
    }

    fun setGridSpacing(@Dimension spacing: Float) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(gridSpacing = spacing)
    }

    fun setMaxGridWidth(@Dimension width: Float) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(maxGridWidth = width)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = (style ?: CometChatVideoBubbleStyle()).copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {
        // Guard against calls during parent class initialization before style is initialized
        if (style == null) {
            super.setBackgroundDrawable(drawable)
            return
        }
        style = (style ?: CometChatVideoBubbleStyle()).copy(backgroundDrawable = drawable)
        drawable?.let { applyBackgroundDrawable(it) }
    }

    // ========================================
    // Click Listeners
    // ========================================

    fun setOnClick(onClick: OnClick?) {
        this.onClick = onClick
    }

    fun getOnClick(): OnClick? = onClick

    fun setOnVideoClickListener(listener: ((Int, Attachment) -> Unit)?) {
        this.onVideoClick = listener
    }

    fun setOnMoreClickListener(listener: ((List<Attachment>) -> Unit)?) {
        this.onMoreClick = listener
    }

    // ========================================
    // View Accessors
    // ========================================

    fun getVideoImageView(): ImageView = videoImageView

    fun getPlayButtonImageView(): ImageView = playButton

    fun getProgressBar(): ProgressBar = progressBar

    fun getVideoContainerCard(): MaterialCardView = videoContainerCard

    fun getCaptionTextView(): TextView = captionTextView

    fun getGridLayout(): GridLayout = gridLayout

    fun getAttachments(): List<Attachment> = attachments

    fun getMediaMessage(): MediaMessage? = mediaMessage

    fun getPlayOverlay(): FrameLayout = playButtonLayout

    fun getView(): LinearLayout = parentLayout

    // ========================================
    // Lifecycle
    // ========================================

    /**
     * Cancels any ongoing thumbnail loads. Call this when the view is recycled.
     */
    fun cancelThumbnailLoads() {
        try {
            Glide.with(context).clear(videoImageView)
            // Clear grid thumbnails
            for (i in 0 until gridLayout.childCount) {
                val child = gridLayout.getChildAt(i)
                if (child is ViewGroup) {
                    clearThumbnailsInViewGroup(child)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling thumbnail loads: ${e.message}")
        }
    }

    private fun clearThumbnailsInViewGroup(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ImageView) {
                try {
                    Glide.with(context).clear(child)
                } catch (e: Exception) {
                    // Ignore
                }
            } else if (child is ViewGroup) {
                clearThumbnailsInViewGroup(child)
            }
        }
    }
}
