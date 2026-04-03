package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat

/**
 * A custom view that represents a file bubble used for displaying file attachments
 * in a chat interface. It provides several customization options such as file icon,
 * title, subtitle, and download button.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Single file display with file type icon, name, and size
 * - File type detection based on MIME type and extension
 * - Customizable styling via XML attributes or programmatically
 */
class CometChatFileBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var parentLayout: LinearLayout
    private lateinit var horizontalContainer: LinearLayout
    private lateinit var fileIconImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var downloadIconImageView: ImageView

    // Single style object - NO individual style properties
    private var style: CometChatFileBubbleStyle = CometChatFileBubbleStyle()

    // Data fields
    private var titleText: String = ""
    private var subTitleText: String = ""
    private var fileUrl: String = ""
    private var onClick: OnClick? = null
    private var message: MediaMessage? = null

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    /**
     * Initializes the view by inflating the XML layout and binding child views.
     *
     * @param attrs The attribute set for customization.
     * @param defStyleAttr The default style attribute.
     */
    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        
        // Set fixed width to match Java reference: 240dp (cometchat_file_bubble_layout_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            LayoutParams.WRAP_CONTENT
        )
        
        LayoutInflater.from(context).inflate(R.layout.cometchat_file_bubble, this, true)

        // Bind UI elements to their corresponding IDs
        parentLayout = findViewById(R.id.parent)
        horizontalContainer = findViewById(R.id.horizontal_container)
        fileIconImageView = findViewById(R.id.cometchat_file_icon)
        titleTextView = findViewById(R.id.tv_toolbar_title)
        subtitleTextView = findViewById(R.id.tv_subtitle)
        downloadIconImageView = findViewById(R.id.iv_download)

        // Set default visibility
        downloadIconImageView.visibility = View.GONE
        titleTextView.visibility = View.GONE
        subtitleTextView.visibility = View.GONE

        // Set the download icon click event
        downloadIconImageView.setOnClickListener { handleDownloadClick() }

        parentLayout.setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        parentLayout.setOnClickListener { openFile() }

        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Handle the click event for the download icon.
     */
    private fun handleDownloadClick() {
        if (onClick != null) {
            onClick?.onClick()
        } else {
            // Default download behavior
            if (fileUrl.isNotEmpty()) {
                val defaultTitle = titleText.ifEmpty { System.currentTimeMillis().toString() }
                Utils.downloadFile(context, fileUrl, defaultTitle)
            }
        }
    }

    private fun openFile() {
        message?.let { msg ->
            val file = Utils.getFileFromLocalPath(msg)
            if (file != null && file.exists()) {
                MediaUtils.openFile(context, file)
                return
            }
            msg.attachment?.let { attachment ->
                MediaUtils.openMediaInPlayer(context, attachment.fileUrl, attachment.fileMimeType)
            }
        }
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return

        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatFileBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatFileBubble_cometchatFileBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatFileBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatFileBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        // Bubble container styling
        // Background handled by wrapper CometChatMessageBubble - content views are transparent by default
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        if (style.strokeWidth != 0f) applyStrokeWidth(style.strokeWidth)
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
        style.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Title styling
        if (style.titleColor != 0) applyTitleTextColor(style.titleColor)
        if (style.titleTextAppearance != 0) applyTitleTextAppearance(style.titleTextAppearance)

        // Subtitle styling
        if (style.subtitleColor != 0) applySubtitleTextColor(style.subtitleColor)
        if (style.subtitleTextAppearance != 0) applySubtitleTextAppearance(style.subtitleTextAppearance)

        // Download icon styling
        if (style.fileDownloadIconTint != 0) applyDownloadIconTint(style.fileDownloadIconTint)
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

    private fun applyBackgroundDrawable(drawable: android.graphics.drawable.Drawable) {
        super.setBackgroundDrawable(drawable)
    }

    private fun applyTitleTextColor(@ColorInt color: Int) {
        titleTextView.setTextColor(color)
    }

    private fun applyTitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            titleTextView.setTextAppearance(appearance)
        }
    }

    private fun applySubtitleTextColor(@ColorInt color: Int) {
        subtitleTextView.setTextColor(color)
    }

    private fun applySubtitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            subtitleTextView.setTextAppearance(appearance)
        }
    }

    private fun applyDownloadIconTint(@ColorInt color: Int) {
        downloadIconImageView.imageTintList = ColorStateList.valueOf(color)
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the media message for the current view. This method takes a
     * [MediaMessage] and updates the UI to reflect the properties of the
     * message, including its attachment (if available).
     *
     * @param mediaMessage The [MediaMessage] to be set.
     */
    @SuppressLint("SimpleDateFormat")
    fun setMessage(mediaMessage: MediaMessage?) {
        if (mediaMessage == null) return
        this.message = mediaMessage

        val attachment = mediaMessage.attachment
        val dateFormatterPattern = "d MMM, yyyy"

        if (attachment != null) {
            val size = attachment.fileSize

            // Set file icon based on MIME type
            setFileIconFromMimeType(attachment.fileMimeType, attachment.fileUrl)

            // Format subtitle text
            val subTitleText = SimpleDateFormat(dateFormatterPattern).format(mediaMessage.sentAt * 1000) +
                    " • " + Utils.getFileSize(size) +
                    " • " + attachment.fileUrl.substringAfterLast(".")

            // Set file URL and text without automatically showing download icon
            setFileUrlInternal(attachment.fileUrl, attachment.fileName ?: "", subTitleText)

            // Determine download icon visibility based on local file existence
            val shouldShowDownload = shouldShowDownloadIcon(mediaMessage)
            downloadIconImageView.visibility = if (shouldShowDownload) View.VISIBLE else View.GONE
        } else {
            // Handle file from local path - no download needed for local files
            mediaMessage.file?.let { file ->
                val subTitleText = SimpleDateFormat(dateFormatterPattern).format(mediaMessage.sentAt * 1000) +
                        " • " + Utils.getFileSize(file.length().toInt()) +
                        " • " + file.path.substringAfterLast(".")

                setFileIconFromMimeType(Utils.getMimeTypeFromFile(context, file), file.path)
                downloadIconImageView.visibility = View.GONE
                setFileUrlInternal("", file.name, subTitleText)
            }
        }
    }

    /**
     * Determines whether the download icon should be shown for a file message.
     *
     * Matches the Java reference behavior:
     * - If the message has no attachment (local outgoing file) → hide download icon
     * - If the message metadata contains a local "path" and that file exists → hide download icon
     * - Otherwise (remote file not yet downloaded) → show download icon
     *
     * @param mediaMessage The media message to check
     * @return true if the download icon should be shown
     */
    private fun shouldShowDownloadIcon(mediaMessage: MediaMessage): Boolean {
        // No attachment means it's a local outgoing file — no download needed
        val attachment = mediaMessage.attachment ?: return false
        if (attachment.fileUrl.isNullOrEmpty()) return false

        // Check if file already exists locally via metadata "path"
        try {
            val metadata = mediaMessage.metadata
            if (metadata != null && metadata.has(UIKitConstants.IntentStrings.PATH)) {
                val path = metadata.getString(UIKitConstants.IntentStrings.PATH)
                if (!path.isNullOrEmpty()) {
                    val file = File(path)
                    if (file.exists()) return false
                }
            }
        } catch (_: Exception) { }

        return true
    }

    /**
     * Sets the file icon based on MIME type.
     */
    private fun setFileIconFromMimeType(mimeType: String?, fileUrl: String?) {
        val icon = when {
            mimeType == null -> R.drawable.cometchat_unknown_file_icon
            mimeType.contains(UIKitConstants.MimeType.VIDEO) -> R.drawable.cometchat_video_file_icon
            mimeType.contains(UIKitConstants.MimeType.OCTET_STREAM) -> {
                when {
                    fileUrl?.endsWith(UIKitConstants.MimeType.DOC) == true -> R.drawable.cometchat_word_file_icon
                    fileUrl?.endsWith(UIKitConstants.MimeType.PPT) == true -> R.drawable.cometchat_ppt_file_icon
                    fileUrl?.endsWith(UIKitConstants.MimeType.XLS) == true -> R.drawable.cometchat_xlsx_file_icon
                    else -> R.drawable.cometchat_unknown_file_icon
                }
            }
            mimeType.contains(UIKitConstants.MimeType.PDF) -> R.drawable.cometchat_pdf_file_icon
            mimeType.contains(UIKitConstants.MimeType.ZIP) -> R.drawable.cometchat_zip_file_icon
            fileUrl?.contains(UIKitConstants.MimeType.CSV) == true -> R.drawable.cometchat_text_file_icon
            mimeType.contains(UIKitConstants.MimeType.AUDIO) -> R.drawable.cometchat_audio_file_icon
            mimeType.contains(UIKitConstants.MimeType.IMAGE) -> R.drawable.cometchat_image_file_icon
            mimeType.contains(UIKitConstants.MimeType.TEXT) -> R.drawable.cometchat_text_file_icon
            mimeType.contains(UIKitConstants.MimeType.LINK) -> R.drawable.cometchat_link_file_icon
            else -> R.drawable.cometchat_unknown_file_icon
        }
        setFileIcon(icon)
    }

    /**
     * Set the file URL, title, and subtitle for the file bubble.
     * This public method shows the download icon when a valid URL is provided.
     *
     * @param fileUrl The file URL.
     * @param titleText The title text.
     * @param subtitleText The subtitle text.
     */
    fun setFileUrl(fileUrl: String, titleText: String, subtitleText: String) {
        setFileUrlInternal(fileUrl, titleText, subtitleText)
        if (fileUrl.isNotEmpty()) {
            downloadIconImageView.visibility = View.VISIBLE
        }
    }

    /**
     * Internal method to set file URL, title, and subtitle without modifying download icon visibility.
     * Used by setMessage() which handles download icon visibility separately based on local file existence.
     *
     * @param fileUrl The file URL.
     * @param titleText The title text.
     * @param subtitleText The subtitle text.
     */
    private fun setFileUrlInternal(fileUrl: String, titleText: String, subtitleText: String) {
        if (fileUrl.isNotEmpty()) {
            this.fileUrl = fileUrl
        }
        setTitleText(titleText)
        setSubtitleText(subtitleText)
    }

    /**
     * Set the subtitle text for the file bubble.
     *
     * @param text The subtitle text to display.
     */
    fun setSubtitleText(text: String) {
        if (text.isNotEmpty()) {
            subTitleText = text
            subtitleTextView.visibility = View.VISIBLE
            subtitleTextView.text = text
        }
    }

    /**
     * Set the title text for the file bubble.
     *
     * @param text The title text to display.
     */
    fun setTitleText(text: String) {
        if (text.isNotEmpty()) {
            titleTextView.visibility = View.VISIBLE
            titleText = text
            titleTextView.text = text
        }
    }

    /**
     * Set the file icon for the file bubble.
     *
     * @param image The resource ID of the image to set.
     */
    fun setFileIcon(@DrawableRes image: Int) {
        if (image != 0) {
            fileIconImageView.setImageResource(image)
        }
    }

    /**
     * Set the download icon image resource.
     *
     * @param image The resource ID of the download icon image.
     */
    fun setDownloadIcon(@DrawableRes image: Int) {
        if (image != 0) {
            downloadIconImageView.setImageResource(image)
        }
    }

    // ========================================
    // Getters for views
    // ========================================

    fun getView(): LinearLayout = parentLayout

    fun getTitle(): TextView = titleTextView

    fun getSubtitle(): TextView = subtitleTextView

    fun getDownloadImageView(): ImageView = downloadIconImageView

    fun getFileIcon(): ImageView = fileIconImageView

    fun getTitleText(): String = titleText

    fun getSubTitleText(): String = subTitleText

    fun getFileUrl(): String = fileUrl

    fun getOnClick(): OnClick? = onClick

    /**
     * Set a custom click listener for the file bubble.
     *
     * @param onClick The custom OnClick listener.
     */
    fun setOnClick(onClick: OnClick?) {
        this.onClick = onClick
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatFileBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatFileBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatFileBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getBubbleBackgroundColor(): Int = style.backgroundColor
    fun getTitleTextColor(): Int = style.titleColor
    fun getTitleTextAppearance(): Int = style.titleTextAppearance
    fun getSubtitleTextColor(): Int = style.subtitleColor
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance
    fun getDownloadIconTint(): Int = style.fileDownloadIconTint
    fun getBubbleStrokeWidth(): Float = style.strokeWidth
    fun getBubbleStrokeColor(): Int = style.strokeColor
    fun getBubbleCornerRadius(): Float = style.cornerRadius

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius.toFloat())
        applyCornerRadius(radius.toFloat())
    }

    fun setBubbleStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width.toFloat())
        applyStrokeWidth(width.toFloat())
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        applyStrokeColor(color)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleColor = color)
        applyTitleTextColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        applyTitleTextAppearance(appearance)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(subtitleColor = color)
        applySubtitleTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(subtitleTextAppearance = appearance)
        applySubtitleTextAppearance(appearance)
    }

    fun setDownloadIconTint(@ColorInt color: Int) {
        style = style.copy(fileDownloadIconTint = color)
        applyDownloadIconTint(color)
    }

    /**
     * Sets the background color of the file icon container.
     * Currently a no-op as there's no dedicated container in the layout.
     *
     * @param color The background color to set.
     */
    fun setFileIconBackgroundColor(@ColorInt color: Int) {
        // No dedicated file icon container in current layout
    }

    /**
     * Sets multiple file attachments. Currently displays only the first attachment.
     *
     * @param attachments The list of attachments to display.
     */
    fun setFiles(attachments: List<Attachment>) {
        if (attachments.isEmpty()) return
        val attachment = attachments.first()
        setFileIconFromMimeType(attachment.fileMimeType, attachment.fileUrl)
        setFileUrl(attachment.fileUrl, attachment.fileName ?: "", Utils.getFileSize(attachment.fileSize))
    }

    /**
     * Controls visibility of a "Download All" button.
     * Currently a no-op as this feature isn't in the current layout.
     *
     * @param show Whether to show the download all button.
     */
    fun setShowDownloadAllButton(show: Boolean) {
        // Download All button not implemented in current layout
    }

    /**
     * Sets the background color of the "Download All" button.
     * Currently a no-op as this feature isn't in the current layout.
     *
     * @param color The background color to set.
     */
    fun setDownloadAllButtonBackgroundColor(@ColorInt color: Int) {
        // Download All button not implemented in current layout
    }

    /**
     * Sets the text color of the "Download All" button.
     * Currently a no-op as this feature isn't in the current layout.
     *
     * @param color The text color to set.
     */
    fun setDownloadAllButtonTextColor(@ColorInt color: Int) {
        // Download All button not implemented in current layout
    }

    companion object {
        private val TAG = CometChatFileBubble::class.java.simpleName
    }
}
