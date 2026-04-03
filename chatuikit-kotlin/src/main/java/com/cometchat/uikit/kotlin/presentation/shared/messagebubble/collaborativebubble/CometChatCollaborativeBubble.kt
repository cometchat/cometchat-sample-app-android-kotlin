package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject

/**
 * A custom view that displays a collaborative document/whiteboard message bubble.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 * The UI matches the reference implementation in chatuikit (Java).
 *
 * Layout structure:
 * - Image banner at top (136dp height)
 * - Icon + Title + Subtitle row
 * - Separator line
 * - Join/Open button
 *
 * Features:
 * - Display collaborative document/whiteboard links
 * - Banner image for visual appeal
 * - Document icon and title/subtitle
 * - "Join" or "Open" button
 * - Separator line between content and button
 * - Automatic extraction of data from CustomMessage
 *
 * Example usage:
 * ```kotlin
 * val collaborativeBubble = CometChatCollaborativeBubble(context)
 * collaborativeBubble.setMessage(customMessage) // Extracts data automatically
 * // or manually:
 * collaborativeBubble.setTitle("Collaborative Document")
 * collaborativeBubble.setSubTitle("Open to collaborate")
 * collaborativeBubble.setButtonText("Join")
 * collaborativeBubble.setBoardUrl("https://...")
 * ```
 */
class CometChatCollaborativeBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatCollaborativeBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatCollaborativeBubble::class.java.simpleName
        
        // Extension keys for collaborative data
        private const val EXTENSION_WHITEBOARD = "whiteboard"
        private const val EXTENSION_DOCUMENT = "document"
        private const val KEY_BOARD_URL = "board_url"
        private const val KEY_DOCUMENT_URL = "document_url"
    }

    /**
     * Enum representing collaborative document types.
     */
    enum class CollaborativeType {
        DOCUMENT,
        WHITEBOARD
    }

    // Views from XML layout
    private lateinit var imageContainerCard: MaterialCardView
    private lateinit var bannerImageView: ImageView
    private lateinit var iconImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var separatorView: View
    private lateinit var joinButton: TextView

    // Single style object - nullable during initialization to handle parent constructor calls
    private var style: CometChatCollaborativeBubbleStyle? = null

    private var boardUrl: String = ""
    private var titleText: String = ""
    private var onClick: OnClick? = null
    private var customMessage: CustomMessage? = null
    private var collaborativeType: CollaborativeType = CollaborativeType.DOCUMENT

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        
        // Set fixed width to match Java reference: 240dp (cometchat_collaborative_bubble_layout_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            LayoutParams.WRAP_CONTENT
        )
        
        // Inflate the XML layout
        val view = LayoutInflater.from(context).inflate(R.layout.cometchat_collaborative_bubble, this, true)
        
        // Find views
        imageContainerCard = view.findViewById(R.id.bubble_image_container)
        bannerImageView = view.findViewById(R.id.collaborative_bubble_image)
        iconImageView = view.findViewById(R.id.icon)
        titleTextView = view.findViewById(R.id.title)
        subtitleTextView = view.findViewById(R.id.tv_last_message_text)
        separatorView = view.findViewById(R.id.separator)
        joinButton = view.findViewById(R.id.join_button)
        
        // Initialize image container card
        Utils.initMaterialCard(imageContainerCard)
        
        // Set up join button click listener
        joinButton.setOnClickListener {
            onClick?.onClick()
        }

        // Set long click listener to propagate to parent for message actions
        setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }
        
        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Extracts style attributes from XML and applies them.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCollaborativeBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCollaborativeBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatCollaborativeBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        val currentStyle = style ?: return

        // Title styling
        if (currentStyle.titleTextColor != 0) applyTitleTextColor(currentStyle.titleTextColor)
        if (currentStyle.titleTextAppearance != 0) applyTitleTextAppearance(currentStyle.titleTextAppearance)

        // Subtitle styling
        if (currentStyle.subtitleTextColor != 0) applySubtitleTextColor(currentStyle.subtitleTextColor)
        if (currentStyle.subtitleTextAppearance != 0) applySubtitleTextAppearance(currentStyle.subtitleTextAppearance)

        // Icon styling
        currentStyle.iconDrawable?.let { iconImageView.setImageDrawable(it) }
        if (currentStyle.iconTint != 0) applyIconTint(currentStyle.iconTint)

        // Button styling
        if (currentStyle.buttonTextColor != 0) applyButtonTextColor(currentStyle.buttonTextColor)
        if (currentStyle.buttonTextAppearance != 0) applyButtonTextAppearance(currentStyle.buttonTextAppearance)

        // Separator styling
        if (currentStyle.separatorColor != 0) applySeparatorColor(currentStyle.separatorColor)

        // Image container styling
        if (currentStyle.imageStrokeColor != 0) imageContainerCard.strokeColor = currentStyle.imageStrokeColor
        if (currentStyle.imageStrokeWidth != 0f) imageContainerCard.strokeWidth = currentStyle.imageStrokeWidth.toInt()
        if (currentStyle.imageCornerRadius != 0f) imageContainerCard.radius = currentStyle.imageCornerRadius
    }

    // ========================================
    // Public Content Methods
    // ========================================

    /**
     * Sets the message and extracts collaborative data from it.
     *
     * This method automatically extracts:
     * - Board URL from message metadata (whiteboard or document extension)
     * - Title and subtitle from customData
     * - Button text from customData
     * - Collaborative type (document or whiteboard)
     *
     * The data is extracted from the message metadata structure:
     * - Whiteboard: `metadata.@injected.extensions.whiteboard.board_url`
     * - Document: `metadata.@injected.extensions.document.document_url`
     *
     * @param message The CustomMessage containing collaborative data
     */
    fun setMessage(message: CustomMessage) {
        this.customMessage = message
        
        // Extract collaborative data from message
        val extractedData = extractCollaborativeData(message)
        
        // Apply extracted data
        this.collaborativeType = extractedData.type
        setBoardUrl(extractedData.url)
        setTitle(extractedData.title)
        setSubTitle(extractedData.subtitle)
        setButtonText(extractedData.buttonText)
        
        // Update icon based on type
        updateIconForType(extractedData.type)
    }

    /**
     * Gets the current message.
     */
    fun getMessage(): CustomMessage? = customMessage

    /**
     * Gets the collaborative type.
     */
    fun getCollaborativeType(): CollaborativeType = collaborativeType

    /**
     * Sets the collaborative type and updates the icon.
     */
    fun setCollaborativeType(type: CollaborativeType) {
        this.collaborativeType = type
        updateIconForType(type)
    }

    /**
     * Updates the icon based on the collaborative type.
     */
    private fun updateIconForType(type: CollaborativeType) {
        val iconRes = when (type) {
            CollaborativeType.DOCUMENT -> R.drawable.cometchat_ic_collaborative_document
            CollaborativeType.WHITEBOARD -> R.drawable.cometchat_ic_collaborative
        }
        iconImageView.setImageResource(iconRes)
    }

    /**
     * Data class for extracted collaborative data.
     */
    private data class CollaborativeData(
        val title: String,
        val subtitle: String,
        val buttonText: String,
        val url: String,
        val type: CollaborativeType
    )

    /**
     * Extracts collaborative data from a CustomMessage.
     *
     * Checks both the message metadata (for extension data) and customData
     * for collaborative information.
     */
    private fun extractCollaborativeData(message: CustomMessage): CollaborativeData {
        var url = ""
        var type = CollaborativeType.DOCUMENT
        var title = context.getString(R.string.cometchat_collaborative_doc)
        var subtitle = context.getString(R.string.cometchat_open_document_to_edit_content_together)
        var buttonText = context.getString(R.string.cometchat_open_document)

        try {
            // First, try to extract URL from metadata extensions
            val metadata = message.metadata
            if (metadata != null) {
                val extensionData = extractExtensionData(metadata)
                if (extensionData != null) {
                    url = extensionData.first
                    type = extensionData.second
                }
            }

            // Extract additional data from customData
            val customData = message.customData
            if (customData != null) {
                // Title
                if (customData.has("title")) {
                    title = customData.optString("title", title)
                }
                // Subtitle
                if (customData.has("subtitle")) {
                    subtitle = customData.optString("subtitle", subtitle)
                }
                // Button text
                if (customData.has("button_text")) {
                    buttonText = customData.optString("button_text", buttonText)
                }
                // URL fallback from customData
                if (url.isEmpty()) {
                    url = customData.optString("url", 
                          customData.optString("board_url", 
                          customData.optString("document_url", "")))
                }
                // Type from customData if not determined from metadata
                if (customData.has("type")) {
                    val typeStr = customData.optString("type", "")
                    if (typeStr.contains("whiteboard", ignoreCase = true)) {
                        type = CollaborativeType.WHITEBOARD
                    }
                }
            }

            // Determine type from message type if still document
            val messageType = message.type
            if (messageType != null && messageType.contains("whiteboard", ignoreCase = true)) {
                type = CollaborativeType.WHITEBOARD
            }

            // Update title/subtitle/button based on type if using defaults
            if (type == CollaborativeType.WHITEBOARD) {
                if (title == context.getString(R.string.cometchat_collaborative_doc)) {
                    title = context.getString(R.string.cometchat_collaborative_whiteboard)
                }
                if (subtitle == context.getString(R.string.cometchat_open_document_to_edit_content_together)) {
                    subtitle = context.getString(R.string.cometchat_open_whiteboard_to_edit_content_together)
                }
                if (buttonText == context.getString(R.string.cometchat_open_document)) {
                    buttonText = context.getString(R.string.cometchat_open_whiteboard)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting collaborative data: ${e.message}")
        }

        return CollaborativeData(
            title = title,
            subtitle = subtitle,
            buttonText = buttonText,
            url = url,
            type = type
        )
    }

    /**
     * Extracts extension data from message metadata.
     *
     * Looks for whiteboard or document extension data in the structure:
     * `metadata.@injected.extensions.{whiteboard|document}`
     *
     * @return Pair of (url, type) or null if not found
     */
    private fun extractExtensionData(metadata: JSONObject): Pair<String, CollaborativeType>? {
        try {
            if (!metadata.has("@injected")) return null
            
            val injectedObject = metadata.getJSONObject("@injected")
            if (!injectedObject.has("extensions")) return null
            
            val extensionsObject = injectedObject.getJSONObject("extensions")
            
            // Check for whiteboard extension
            if (extensionsObject.has(EXTENSION_WHITEBOARD)) {
                val whiteboardData = extensionsObject.getJSONObject(EXTENSION_WHITEBOARD)
                if (whiteboardData.has(KEY_BOARD_URL)) {
                    val boardUrl = whiteboardData.getString(KEY_BOARD_URL)
                    return Pair(boardUrl, CollaborativeType.WHITEBOARD)
                }
            }
            
            // Check for document extension
            if (extensionsObject.has(EXTENSION_DOCUMENT)) {
                val documentData = extensionsObject.getJSONObject(EXTENSION_DOCUMENT)
                if (documentData.has(KEY_DOCUMENT_URL)) {
                    val documentUrl = documentData.getString(KEY_DOCUMENT_URL)
                    return Pair(documentUrl, CollaborativeType.DOCUMENT)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting extension data: ${e.message}")
        }
        return null
    }

    fun getTitle(): String = titleTextView.text.toString()

    /**
     * Sets the title text.
     */
    fun setTitle(title: String?) {
        if (title != null) {
            this.titleText = title
            titleTextView.text = title
        }
    }

    fun getSubTitle(): String = subtitleTextView.text.toString()

    /**
     * Sets the subtitle text.
     */
    fun setSubTitle(subTitle: String?) {
        subtitleTextView.text = subTitle ?: ""
    }

    fun getButtonText(): String = joinButton.text.toString()

    /**
     * Sets the button text.
     */
    fun setButtonText(buttonText: String?) {
        joinButton.text = buttonText ?: ""
    }

    fun getBoardUrl(): String = boardUrl

    /**
     * Sets the URL for the collaborative board.
     */
    fun setBoardUrl(url: String?) {
        this.boardUrl = url ?: ""
    }

    fun getIcon(): Drawable? = iconImageView.drawable

    /**
     * Sets the icon drawable.
     */
    fun setIcon(drawable: Drawable?) {
        iconImageView.setImageDrawable(drawable)
    }

    /**
     * Sets the icon from a resource.
     */
    fun setIcon(@DrawableRes iconRes: Int) {
        iconImageView.setImageResource(iconRes)
    }

    fun getOnClick(): OnClick? = onClick

    /**
     * Sets the click listener for the join button.
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
    fun setStyle(style: CometChatCollaborativeBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatCollaborativeBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatCollaborativeBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getTitleTextAppearance(): Int = style?.titleTextAppearance ?: 0
    fun getTitleTextColor(): Int = style?.titleTextColor ?: 0
    fun getSubtitleTextAppearance(): Int = style?.subtitleTextAppearance ?: 0
    fun getSubtitleTextColor(): Int = style?.subtitleTextColor ?: 0
    fun getIconTint(): Int = style?.iconTint ?: 0
    fun getIconDrawable(): Drawable? = style?.iconDrawable
    fun getButtonTextAppearance(): Int = style?.buttonTextAppearance ?: 0
    fun getButtonTextColor(): Int = style?.buttonTextColor ?: 0
    fun getSeparatorColor(): Int = style?.separatorColor ?: 0
    fun getImageStrokeColor(): Int = style?.imageStrokeColor ?: 0
    fun getImageStrokeWidth(): Float = style?.imageStrokeWidth ?: 0f
    fun getImageCornerRadius(): Float = style?.imageCornerRadius ?: 0f

    // Wrapper property getters
    fun getBubbleBackgroundColor(): Int = style?.backgroundColor ?: 0
    fun getBubbleCornerRadius(): Float = style?.cornerRadius ?: 0f
    fun getBubbleStrokeWidth(): Float = style?.strokeWidth ?: 0f
    fun getBubbleStrokeColor(): Int = style?.strokeColor ?: 0
    fun getSenderNameTextColor(): Int = style?.senderNameTextColor ?: 0
    fun getSenderNameTextAppearance(): Int = style?.senderNameTextAppearance ?: 0
    fun getThreadIndicatorTextColor(): Int = style?.threadIndicatorTextColor ?: 0
    fun getThreadIndicatorTextAppearance(): Int = style?.threadIndicatorTextAppearance ?: 0
    fun getThreadIndicatorIconTint(): Int = style?.threadIndicatorIconTint ?: 0

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(titleTextAppearance = appearance) ?: CometChatCollaborativeBubbleStyle(titleTextAppearance = appearance)
        applyTitleTextAppearance(appearance)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style?.copy(titleTextColor = color) ?: CometChatCollaborativeBubbleStyle(titleTextColor = color)
        applyTitleTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(subtitleTextAppearance = appearance) ?: CometChatCollaborativeBubbleStyle(subtitleTextAppearance = appearance)
        applySubtitleTextAppearance(appearance)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style?.copy(subtitleTextColor = color) ?: CometChatCollaborativeBubbleStyle(subtitleTextColor = color)
        applySubtitleTextColor(color)
    }

    fun setIconTint(@ColorInt color: Int) {
        style = style?.copy(iconTint = color) ?: CometChatCollaborativeBubbleStyle(iconTint = color)
        applyIconTint(color)
    }

    fun setIconDrawable(drawable: Drawable?) {
        style = style?.copy(iconDrawable = drawable) ?: CometChatCollaborativeBubbleStyle(iconDrawable = drawable)
        drawable?.let { iconImageView.setImageDrawable(it) }
    }

    fun setButtonTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(buttonTextAppearance = appearance) ?: CometChatCollaborativeBubbleStyle(buttonTextAppearance = appearance)
        applyButtonTextAppearance(appearance)
    }

    fun setButtonTextColor(@ColorInt color: Int) {
        style = style?.copy(buttonTextColor = color) ?: CometChatCollaborativeBubbleStyle(buttonTextColor = color)
        applyButtonTextColor(color)
    }

    fun setSeparatorColor(@ColorInt color: Int) {
        style = style?.copy(separatorColor = color) ?: CometChatCollaborativeBubbleStyle(separatorColor = color)
        applySeparatorColor(color)
    }

    fun setImageStrokeColor(@ColorInt color: Int) {
        style = style?.copy(imageStrokeColor = color) ?: CometChatCollaborativeBubbleStyle(imageStrokeColor = color)
        imageContainerCard.strokeColor = color
    }

    fun setImageStrokeWidth(@Dimension width: Float) {
        style = style?.copy(imageStrokeWidth = width) ?: CometChatCollaborativeBubbleStyle(imageStrokeWidth = width)
        imageContainerCard.strokeWidth = width.toInt()
    }

    fun setImageCornerRadius(@Dimension radius: Float) {
        style = style?.copy(imageCornerRadius = radius) ?: CometChatCollaborativeBubbleStyle(imageCornerRadius = radius)
        imageContainerCard.radius = radius
    }

    // Wrapper property setters
    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(backgroundColor = color) ?: CometChatCollaborativeBubbleStyle(backgroundColor = color)
        setCardBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Float) {
        style = style?.copy(cornerRadius = radius) ?: CometChatCollaborativeBubbleStyle(cornerRadius = radius)
        setRadius(radius)
    }

    fun setBubbleStrokeWidth(@Dimension width: Float) {
        style = style?.copy(strokeWidth = width) ?: CometChatCollaborativeBubbleStyle(strokeWidth = width)
        strokeWidth = width.toInt()
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style?.copy(strokeColor = color) ?: CometChatCollaborativeBubbleStyle(strokeColor = color)
        strokeColor = color
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {
        // Guard against calls during parent constructor initialization when style is null
        if (style == null) {
            super.setBackgroundDrawable(drawable)
            return
        }
        style = style?.copy(backgroundDrawable = drawable)
        drawable?.let { super.setBackgroundDrawable(it) }
    }

    fun setSenderNameTextColor(@ColorInt color: Int) {
        style = style?.copy(senderNameTextColor = color) ?: CometChatCollaborativeBubbleStyle(senderNameTextColor = color)
    }

    fun setSenderNameTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(senderNameTextAppearance = appearance) ?: CometChatCollaborativeBubbleStyle(senderNameTextAppearance = appearance)
    }

    fun setThreadIndicatorTextColor(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorTextColor = color) ?: CometChatCollaborativeBubbleStyle(threadIndicatorTextColor = color)
    }

    fun setThreadIndicatorTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(threadIndicatorTextAppearance = appearance) ?: CometChatCollaborativeBubbleStyle(threadIndicatorTextAppearance = appearance)
    }

    fun setThreadIndicatorIconTint(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorIconTint = color) ?: CometChatCollaborativeBubbleStyle(threadIndicatorIconTint = color)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyTitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            titleTextView.setTextAppearance(appearance)
        }
    }

    private fun applyTitleTextColor(@ColorInt color: Int) {
        titleTextView.setTextColor(color)
    }

    private fun applySubtitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            subtitleTextView.setTextAppearance(appearance)
        }
    }

    private fun applySubtitleTextColor(@ColorInt color: Int) {
        subtitleTextView.setTextColor(color)
    }

    private fun applyIconTint(@ColorInt color: Int) {
        iconImageView.imageTintList = ColorStateList.valueOf(color)
    }

    private fun applyButtonTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            joinButton.setTextAppearance(appearance)
        }
    }

    private fun applyButtonTextColor(@ColorInt color: Int) {
        joinButton.setTextColor(color)
    }

    private fun applySeparatorColor(@ColorInt color: Int) {
        separatorView.setBackgroundColor(color)
    }
}
