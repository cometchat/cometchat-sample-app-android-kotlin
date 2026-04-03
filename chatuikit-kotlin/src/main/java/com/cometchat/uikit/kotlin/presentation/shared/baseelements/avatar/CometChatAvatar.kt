package com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAvatarBinding
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * CometChatAvatar is a custom view that displays an avatar image or initials.
 * 
 * This component displays a circular avatar with support for:
 * - Loading images from URLs using Glide
 * - Displaying initials when no image is available
 * - Customizable styling (background color, stroke, corner radius)
 * - Support for User and Group objects
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
 *     android:id="@+id/avatar"
 *     android:layout_width="48dp"
 *     android:layout_height="48dp"
 *     app:cometchatAvatarBackgroundColor="@color/primary" />
 * ```
 * 
 * Usage in Kotlin:
 * ```kotlin
 * val avatar = CometChatAvatar(context)
 * avatar.setAvatar("John Doe", "https://example.com/avatar.jpg")
 * ```
 */
@Suppress("unused")
class CometChatAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatAvatarStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatAvatar::class.java.simpleName
    }

    private lateinit var binding: CometchatAvatarBinding

    // Properties for the functionality of the avatar
    private var text: String = ""
    private var avatarUrl: String? = null
    private var placeHolderDrawable: Drawable? = null

    // Single style object - NO individual style properties
    private var style: CometChatAvatarStyle = CometChatAvatarStyle()

    init {
        if (!isInEditMode) {
            inflateAndInitializeView(attrs, defStyleAttr)
        }
    }

    /**
     * Inflates and initializes the view.
     */
    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        binding = CometchatAvatarBinding.inflate(LayoutInflater.from(context), this, true)
        Utils.initMaterialCard(this)
        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAvatar, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatAvatar_cometchatAvatarStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAvatar, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatAvatarStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        if (style.backgroundColor != 0) applyBackgroundColor(style.backgroundColor)
        if (style.strokeColor != 0) applyStrokeColor(style.strokeColor)
        if (style.strokeWidth != 0f) applyStrokeWidth(style.strokeWidth)
        if (style.cornerRadius != 0f) applyCornerRadius(style.cornerRadius)
        if (style.placeHolderTextAppearance != 0) applyPlaceHolderTextAppearance(style.placeHolderTextAppearance)
        if (style.placeHolderTextColor != 0) applyPlaceHolderTextColor(style.placeHolderTextColor)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyBackgroundColor(@ColorInt color: Int) {
        binding.innerViewLayout.setBackgroundColor(color)
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        binding.cardView.strokeColor = color
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        binding.cardView.strokeWidth = width.toInt()
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        binding.cardView.radius = radius
    }

    private fun applyPlaceHolderTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            binding.tvAvatar.setTextAppearance(textAppearance)
        }
    }

    private fun applyPlaceHolderTextColor(@ColorInt color: Int) {
        binding.tvAvatar.setTextColor(color)
    }

    /**
     * Gets the name currently set in the avatar.
     */
    fun getName(): String = text

    /**
     * Sets the name to be displayed as initials in the avatar.
     * 
     * @param name The name to generate initials from
     */
    fun setName(name: String?) {
        // Clear any pending Glide request to prevent callbacks from affecting this view
        if (isValidContextForGlide(context)) {
            Glide.with(context).clear(binding.ivAvatar)
        }
        
        // Reset avatar URL since we're setting name only
        this.avatarUrl = null
        
        var displayText = ""
        if (!name.isNullOrEmpty()) {
            val nameParts = name.trim().split("\\s+".toRegex())

            displayText = when {
                containsOnlyEmojis(name) -> getFirstCodePoint(name)
                nameParts.size >= 2 -> getFirstCodePoint(nameParts[0]) + getFirstCodePoint(nameParts[1])
                else -> getFirstCodePoint(nameParts[0]) + getNextCodePoint(nameParts[0], 1)
            }
        }
        setIvAvatarVisibility(View.INVISIBLE)
        binding.tvAvatar.text = displayText.uppercase()
        setTvAvatarVisibility(View.VISIBLE)
        
        // Restore background color from style
        if (style.backgroundColor != 0) {
            binding.innerViewLayout.setBackgroundColor(style.backgroundColor)
        }
    }

    /**
     * Sets the avatar with name and optional URL.
     * 
     * @param name The name to display as initials
     * @param avatarUrl The URL of the avatar image (optional)
     */
    fun setAvatar(name: String, avatarUrl: String?) {
        setName(name)
        if (!avatarUrl.isNullOrEmpty()) {
            setAvatar(avatarUrl)
        }
    }

    /**
     * Sets the avatar from a User object.
     * 
     * @param user The User object containing name and avatar URL
     */
    fun setAvatar(user: User) {
        setAvatar(user.name ?: "", user.avatar)
    }

    /**
     * Sets the avatar from a Group object.
     * 
     * @param group The Group object containing name and icon URL
     */
    fun setAvatar(group: Group) {
        setAvatar(group.name ?: "", group.icon)
    }

    /**
     * Sets the avatar image from a URL.
     * 
     * @param avatarUrl The URL of the avatar image
     */
    fun setAvatar(avatarUrl: String) {
        this.avatarUrl = avatarUrl
        if (isValidContextForGlide(context)) {
            setValues()
        }
    }

    /**
     * Sets the avatar image from a URL with a placeholder drawable.
     * 
     * @param avatarUrl The URL of the avatar image
     * @param placeHolderDrawable The placeholder drawable to show while loading
     */
    fun setAvatar(avatarUrl: String, placeHolderDrawable: Drawable?) {
        this.placeHolderDrawable = placeHolderDrawable
        this.avatarUrl = avatarUrl
        if (isValidContextForGlide(context)) {
            setValues()
        }
    }

    /**
     * Sets the avatar image from a drawable.
     * 
     * @param drawable The drawable to display
     */
    fun setAvatar(drawable: Drawable) {
        binding.ivAvatar.setImageDrawable(drawable)
        binding.ivAvatar.visibility = View.VISIBLE
        binding.tvAvatar.visibility = View.GONE
    }

    /**
     * Gets the current avatar URL.
     */
    fun getAvatar(): String? = avatarUrl

    /**
     * Sets a placeholder drawable.
     * 
     * @param placeholderDrawable The drawable to use as placeholder
     */
    fun setPlaceholder(placeholderDrawable: Drawable?) {
        this.placeHolderDrawable = placeholderDrawable
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getAvatarBackgroundColor(): Int = style.backgroundColor

    fun getAvatarStrokeColor(): Int = style.strokeColor

    fun getAvatarStrokeWidth(): Float = style.strokeWidth

    fun getAvatarStrokeRadius(): Float = style.cornerRadius

    fun getAvatarPlaceHolderTextAppearance(): Int = style.placeHolderTextAppearance

    fun getAvatarPlaceHolderTextColor(): Int = style.placeHolderTextColor

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setAvatarBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setAvatarStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        applyStrokeColor(color)
    }

    fun setAvatarStrokeWidth(@Dimension width: Float) {
        style = style.copy(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setAvatarStrokeRadius(@Dimension radius: Float) {
        style = style.copy(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setAvatarPlaceHolderTextAppearance(@StyleRes textAppearance: Int) {
        style = style.copy(placeHolderTextAppearance = textAppearance)
        applyPlaceHolderTextAppearance(textAppearance)
    }

    fun setAvatarPlaceHolderTextColor(@ColorInt color: Int) {
        style = style.copy(placeHolderTextColor = color)
        applyPlaceHolderTextColor(color)
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Applies a style resource to the avatar.
     * 
     * @param styleRes The style resource ID
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatAvatar)
            // fromTypedArray handles recycling internally
            setStyle(CometChatAvatarStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Applies a CometChatAvatarStyle to the avatar.
     * 
     * @param style The style object containing all styling properties
     */
    fun setStyle(style: CometChatAvatarStyle) {
        this.style = style
        applyStyle()
    }

    // Private helper methods

    private fun setTvAvatarVisibility(visibility: Int) {
        binding.tvAvatar.visibility = visibility
    }

    private fun setIvAvatarVisibility(visibility: Int) {
        binding.ivAvatar.visibility = visibility
    }

    private fun setValues() {
        try {
            if (!avatarUrl.isNullOrEmpty() && context != null) {
                Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(placeHolderDrawable)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            setTvAvatarVisibility(View.VISIBLE)
                            setIvAvatarVisibility(View.GONE)
                            // Restore background color on failure
                            if (style.backgroundColor != 0) {
                                binding.innerViewLayout.setBackgroundColor(style.backgroundColor)
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            setTvAvatarVisibility(View.GONE)
                            setIvAvatarVisibility(View.VISIBLE)
                            return false
                        }
                    })
                    .into(binding.ivAvatar)
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        invalidate()
    }

    private fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) return false
        if (context is Activity) {
            return !context.isDestroyed && !context.isFinishing
        }
        return true
    }

    private fun containsOnlyEmojis(input: String?): Boolean {
        if (input.isNullOrEmpty()) return false
        var i = 0
        while (i < input.length) {
            val codePoint = input.codePointAt(i)
            if (!isEmoji(codePoint)) return false
            i += Character.charCount(codePoint)
        }
        return true
    }

    private fun getFirstCodePoint(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val firstCodePoint = input.codePointAt(0)
        return String(Character.toChars(firstCodePoint))
    }

    private fun getNextCodePoint(input: String?, count: Int): String {
        if (input.isNullOrEmpty()) return ""
        var codePointIndex = 0
        for (i in 0 until count) {
            codePointIndex = input.offsetByCodePoints(codePointIndex, 1)
            if (codePointIndex >= input.length) break
        }
        return if (codePointIndex < input.length) {
            val nextCodePoint = input.codePointAt(codePointIndex)
            String(Character.toChars(nextCodePoint))
        } else ""
    }

    private fun isEmoji(codePoint: Int): Boolean {
        return (codePoint in 0x1F600..0x1F64F) || // Emoticons
               (codePoint in 0x1F300..0x1F5FF) || // Misc Symbols and Pictographs
               (codePoint in 0x1F680..0x1F6FF) || // Transport and Map
               (codePoint in 0x2600..0x26FF) ||   // Misc symbols
               (codePoint in 0x2700..0x27BF) ||   // Dingbats
               (codePoint in 0xFE00..0xFE0F) ||   // Variation Selectors
               (codePoint in 0x1F900..0x1F9FF) || // Supplemental Symbols and Pictographs
               (codePoint in 0x1FA70..0x1FAFF)    // Symbols and Pictographs Extended-A
    }
}
