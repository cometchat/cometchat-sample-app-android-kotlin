package com.cometchat.uikit.kotlin.presentation.shared.reaction

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * MaterialCardView-based reaction chip that displays an emoji with an optional count.
 *
 * Direct 1:1 port of `CometChatReaction.java` from the Java chatuikit module.
 */
class CometChatReaction @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvReactionEmoji: TextView
    private val tvReactionCount: TextView
    private val spaceBetween: Space

    private var emoji: String? = null
    private var count: Int = 0
    private var isReactedByMe: Boolean = false

    @StyleRes private var reactionStyle: Int = 0
    @ColorInt private var reactionEmojiTextColor: Int = 0
    @ColorInt private var reactionCountTextColor: Int = 0
    @StyleRes private var reactionEmojiTextAppearance: Int = 0
    @StyleRes private var reactionCountTextAppearance: Int = 0
    @ColorInt private var reactionStrokeColor: Int = 0
    @ColorInt private var reactionBackgroundColor: Int = 0
    @ColorInt private var activeReactionStrokeColor: Int = 0
    @ColorInt private var activeReactionBackgroundColor: Int = 0
    @Dimension private var reactionStrokeWidth: Int = 0
    @Dimension private var reactionCornerRadius: Int = 0
    @Dimension private var reactionElevation: Int = 0
    @Dimension private var activeReactionStrokeWidth: Int = 0

    private var clickListener: OnClickListener? = null
    private var longClickListener: OnLongClickListener? = null

    init {
        Utils.initMaterialCard(this)
        val view = LayoutInflater.from(context).inflate(R.layout.cometchat_reaction_item_layout, this, true)
        tvReactionEmoji = view.findViewById(R.id.tv_reaction_emoji)
        tvReactionCount = view.findViewById(R.id.tv_reaction_count)
        spaceBetween = view.findViewById(R.id.space_between)
        setDefaultValues()
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun setDefaultValues() {
        reactionEmojiTextColor = CometChatTheme.getTextColorPrimary(context)
        reactionStrokeColor = CometChatTheme.getStrokeColorLight(context)
        reactionBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        activeReactionStrokeColor = CometChatTheme.getExtendedPrimaryColor300(context)
        activeReactionBackgroundColor = CometChatTheme.getExtendedPrimaryColor100(context)
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val directAttributes: TypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatReaction, defStyleAttr, 0
        )
        @StyleRes val styleResId = directAttributes.getResourceId(
            R.styleable.CometChatReaction_cometchatReactionStyle, 0
        )
        val resolvedAttributes: TypedArray? = if (styleResId != 0) {
            context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatReaction, defStyleAttr, styleResId
            )
        } else null
        extractAttributesAndApplyDefaults(resolvedAttributes)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray?) {
        if (typedArray == null) return
        try {
            reactionEmojiTextColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatReactionEmojiTextColor, reactionEmojiTextColor
            )
            reactionCountTextColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatReactionCountTextColor, reactionCountTextColor
            )
            reactionStrokeColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatReactionStrokeColor, reactionStrokeColor
            )
            reactionBackgroundColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatReactionBackgroundColor, reactionBackgroundColor
            )
            activeReactionStrokeColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatActiveReactionStrokeColor, activeReactionStrokeColor
            )
            activeReactionBackgroundColor = typedArray.getColor(
                R.styleable.CometChatReaction_cometchatActiveReactionBackgroundColor, activeReactionBackgroundColor
            )
            reactionEmojiTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatReaction_cometchatReactionEmojiTextAppearance, reactionEmojiTextAppearance
            )
            reactionCountTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatReaction_cometchatReactionCountTextAppearance, reactionCountTextAppearance
            )
            reactionStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatReaction_cometchatReactionStrokeWidth, reactionStrokeWidth
            )
            reactionCornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatReaction_cometchatReactionCornerRadius, reactionCornerRadius
            )
            reactionElevation = typedArray.getDimensionPixelSize(
                R.styleable.CometChatReaction_cometchatReactionElevation, reactionElevation
            )
            activeReactionStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatReaction_cometchatActiveReactionStrokeWidth, activeReactionStrokeWidth
            )
            applyDefault()
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyDefault() {
        configureReactionTexts()
        setReactionEmojiTextColor(reactionEmojiTextColor)
        setReactionCountTextColor(reactionEmojiTextColor)
        setReactionStrokeColor(reactionStrokeColor)
        setReactionBackgroundColor(reactionBackgroundColor)
        setActiveReactionStrokeColor(activeReactionStrokeColor)
        setActiveReactionBackgroundColor(activeReactionBackgroundColor)
        setReactionEmojiTextAppearance(reactionEmojiTextAppearance)
        setReactionCountTextAppearance(reactionEmojiTextAppearance)
        setReactionStrokeWidth(reactionStrokeWidth)
        setReactionCornerRadius(reactionCornerRadius)
        setReactionElevation(reactionElevation)
        setActiveReactionStrokeWidth(activeReactionStrokeWidth)
    }

    private fun configureReactionTexts() {
        val currentEmoji = emoji ?: return
        if (currentEmoji == "+") {
            val countText = "+$count"
            tvReactionCount.text = countText
            tvReactionEmoji.visibility = GONE
            spaceBetween.visibility = GONE
            tvReactionCount.visibility = VISIBLE
        } else {
            tvReactionEmoji.text = currentEmoji
            tvReactionEmoji.visibility = VISIBLE
            spaceBetween.visibility = GONE
            tvReactionCount.visibility = GONE
            if (count > 0) {
                tvReactionCount.text = count.toString()
                spaceBetween.visibility = VISIBLE
                tvReactionCount.visibility = VISIBLE
            }
        }
    }

    /**
     * Builds the reaction view with the specified emoji, count, and reaction status.
     */
    fun buildReactionView(
        emoji: String,
        count: Int,
        isReactedByMe: Boolean,
        onClickListener: OnClickListener?,
        onLongClickListener: OnLongClickListener?
    ): View {
        this.emoji = emoji
        this.count = count
        this.isReactedByMe = isReactedByMe
        this.clickListener = onClickListener
        this.longClickListener = onLongClickListener
        setOnClickListener(onClickListener)
        setOnLongClickListener(onLongClickListener)
        applyDefault()
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginStart = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin)
        setLayoutParams(layoutParams)
        return this
    }

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val typedArray = context.theme.obtainStyledAttributes(style, R.styleable.CometChatReaction)
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    fun setReactionEmojiTextColor(@ColorInt color: Int) {
        reactionEmojiTextColor = color
        tvReactionEmoji.setTextColor(color)
    }

    fun setReactionCountTextColor(@ColorInt color: Int) {
        reactionCountTextColor = color
        tvReactionCount.setTextColor(color)
    }

    fun setReactionEmojiTextAppearance(@StyleRes textAppearance: Int) {
        reactionEmojiTextAppearance = textAppearance
        if (textAppearance != 0) tvReactionEmoji.setTextAppearance(textAppearance)
    }

    fun setReactionCountTextAppearance(@StyleRes textAppearance: Int) {
        reactionCountTextAppearance = textAppearance
        if (textAppearance != 0) tvReactionCount.setTextAppearance(textAppearance)
    }

    fun setReactionStrokeColor(@ColorInt color: Int) {
        reactionStrokeColor = color
        if (!isReactedByMe) strokeColor = color
    }

    fun setReactionBackgroundColor(@ColorInt color: Int) {
        reactionBackgroundColor = color
        if (!isReactedByMe) setCardBackgroundColor(color)
    }

    fun setActiveReactionStrokeColor(@ColorInt color: Int) {
        activeReactionStrokeColor = color
        if (isReactedByMe) strokeColor = color
    }

    fun setActiveReactionBackgroundColor(@ColorInt color: Int) {
        activeReactionBackgroundColor = color
        if (isReactedByMe) setCardBackgroundColor(color)
    }

    fun setReactionStrokeWidth(@Dimension width: Int) {
        reactionStrokeWidth = width
        if (!isReactedByMe) strokeWidth = width
    }

    fun setReactionCornerRadius(@Dimension radius: Int) {
        reactionCornerRadius = radius
        this.radius = radius.toFloat()
    }

    fun setReactionElevation(@Dimension elevation: Int) {
        reactionElevation = elevation
        cardElevation = elevation.toFloat()
    }

    fun setActiveReactionStrokeWidth(@Dimension width: Int) {
        activeReactionStrokeWidth = width
        if (isReactedByMe) strokeWidth = width
    }

    fun getEmoji(): String? = emoji
    fun getCount(): Int = count
    fun isReactedByMe(): Boolean = isReactedByMe
    fun getOnClickListener(): OnClickListener? = clickListener
    fun getOnLongClickListener(): OnLongClickListener? = longClickListener
}
