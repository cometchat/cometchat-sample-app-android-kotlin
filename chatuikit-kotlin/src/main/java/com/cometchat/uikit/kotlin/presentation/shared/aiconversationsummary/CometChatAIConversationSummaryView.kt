package com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiConversationSummaryBinding
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.ClickListener
import com.cometchat.uikit.kotlin.shared.resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * CometChatAIConversationSummaryView is a customizable UI component
 * designed to display an AI-generated conversation summary in a card view.
 *
 * Unlike the ConversationStarterView which displays multiple clickable options,
 * this view displays a single summary text that provides an overview of the
 * conversation history. It is typically shown at the top of the message list
 * when there are many unread messages.
 *
 * This view allows developers to customize various attributes like colors, dimensions,
 * text appearances, corner radius, stroke width, and other visual elements. It also supports
 * setting custom layouts for error and loading states, along with a shimmer effect to indicate loading.
 *
 * Key features:
 * - Displays a single summary text (not a list of options)
 * - Has a close/dismiss button to hide the summary
 * - Supports loading state with shimmer effect
 * - Supports error state with customizable error message
 *
 * Usage example:
 * ```kotlin
 * val view = CometChatAIConversationSummaryView(context)
 * view.setSummary("This conversation discussed project deadlines and team assignments.")
 * view.setOnCloseClickListener { view ->
 *     // Handle close click - dismiss the summary
 * }
 * ```
 *
 * @see AIConversationSummaryAdapter
 */
class CometChatAIConversationSummaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatAIConversationSummaryStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatAIConversationSummaryView::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatAiConversationSummaryBinding

    // Adapter
    private val aiConversationSummaryAdapter: AIConversationSummaryAdapter

    // Maximum height for the view in pixels
    @Dimension private var maxHeight: Int = 0

    // Click listener for summary items
    private var onItemClickListener: OnItemClickListener? = null

    // Click listener for close icon
    private var onCloseClickListener: OnCloseClickListener? = null

    // Style attributes for the component's main background
    @ColorInt private var viewBackgroundColor: Int = 0
    private var backgroundDrawable: Drawable? = null
    @Dimension private var viewCornerRadius: Int = 0
    @Dimension private var viewStrokeWidth: Int = 0
    @ColorInt private var viewStrokeColor: Int = 0

    // Style attributes for title
    @ColorInt private var titleTextColor: Int = 0
    @StyleRes private var titleTextAppearance: Int = 0

    // Style attributes for close icon
    private var closeIcon: Drawable? = null
    @ColorInt private var closeIconTint: Int = 0

    // Style attributes for individual items in the list
    @ColorInt private var itemBackgroundColor: Int = 0
    private var itemBackgroundDrawable: Drawable? = null
    @Dimension private var itemCornerRadius: Int = 0
    @Dimension private var itemStrokeWidth: Int = 0
    @ColorInt private var itemStrokeColor: Int = 0
    @ColorInt private var itemTextColor: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0

    // Style attributes for error state text
    @ColorInt private var errorStateTextColor: Int = 0
    @StyleRes private var errorStateTextAppearance: Int = 0

    // Resource ID for custom error view layout
    @LayoutRes private var errorViewLayout: Int = 0

    // Resource ID for custom loading view layout
    @LayoutRes private var loadingViewLayout: Int = 0

    // The style resource ID for the view
    @StyleRes private var viewStyle: Int = 0

    init {
        binding = CometchatAiConversationSummaryBinding.inflate(
            LayoutInflater.from(context), this, true
        )
        Utils.initMaterialCard(this)

        aiConversationSummaryAdapter = AIConversationSummaryAdapter()
        binding.recyclerView.adapter = aiConversationSummaryAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        setupRecyclerViewListeners()
        setupCloseIconListener()
        applyStyleAttributes(attrs, defStyleAttr)
    }


    /**
     * Sets up RecyclerView listeners for item clicks.
     */
    private fun setupRecyclerViewListeners() {
        binding.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(context, binding.recyclerView, object : ClickListener {
                override fun onClick(view: View, position: Int) {
                    val summary = view.getTag(R.string.cometchat_reply_lowercase) as? String
                    summary?.let { onItemClickListener?.onClick(view, it, position) }
                }

                override fun onLongClick(view: View, position: Int) {
                    // Long click not used for conversation summary
                }
            })
        )
    }

    /**
     * Sets up the close icon click listener.
     */
    private fun setupCloseIconListener() {
        binding.ivClose.setOnClickListener { view ->
            onCloseClickListener?.onClick(view)
        }
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val directAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAIConversationSummary, defStyleAttr, 0
        )

        @StyleRes val styleResId = directAttributes.getResourceId(
            R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStyle, 0
        )
        directAttributes.recycle()

        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAIConversationSummary, defStyleAttr, styleResId
        )

        extractAttributesAndApplyDefaults(typedArray)
    }

    /**
     * Extracts style attributes and applies defaults.
     */
    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            // Title styles
            setTitleTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextAppearance,
                    0
                )
            )
            setTitleTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )

            // Close icon styles
            setCloseIcon(
                typedArray.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconDrawable
                )
            )
            setCloseIconTint(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCloseIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                )
            )

            // Container styles
            setBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                )
            )
            setBackgroundDrawable(
                typedArray.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryBackgroundDrawable
                )
            )
            setCornerRadius(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryCornerRadius,
                    0
                )
            )
            setStrokeWidth(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeWidth,
                    0
                )
            )
            setStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryStrokeColor,
                    0
                )
            )

            // Item styles
            setItemBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundColor,
                    0
                )
            )
            setItemBackgroundDrawable(
                typedArray.getDrawable(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemBackgroundDrawable
                )
            )
            setItemCornerRadius(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemCornerRadius,
                    0
                )
            )
            setItemStrokeWidth(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeWidth,
                    0
                )
            )
            setItemStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemStrokeColor,
                    0
                )
            )
            setItemTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextAppearance,
                    0
                )
            )
            setItemTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryItemTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                )
            )

            // Error state styles
            setErrorStateTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextAppearance,
                    0
                )
            )
            setErrorStateTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIConversationSummary_cometchatAIConversationSummaryErrorStateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                )
            )
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
            if (measuredHeight > maxHeight) {
                val measureMode = MeasureSpec.getMode(heightMeasureSpec)
                val newHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode)
                super.onMeasure(widthMeasureSpec, newHeightSpec)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    // ==================== Public API ====================

    /**
     * Sets the summary text to be displayed.
     *
     * This method displays the summary in the RecyclerView and hides
     * the loading and error views.
     *
     * @param summary The conversation summary text to display.
     */
    fun setSummary(summary: String?) {
        if (!summary.isNullOrEmpty()) {
            binding.recyclerView.isVisible = true
            aiConversationSummaryAdapter.setList(listOf(summary))
            stopShimmer()
            binding.loadingViewLayout.isVisible = false
            binding.errorViewLayout.isVisible = false
        }
    }

    /**
     * Displays the error view and hides other content views.
     */
    fun showErrorView() {
        binding.errorViewLayout.isVisible = true
        binding.loadingViewLayout.isVisible = false
        binding.recyclerView.isVisible = false
        stopShimmer()
    }

    /**
     * Displays the loading view with a shimmer effect.
     */
    fun showLoadingView() {
        binding.errorViewLayout.isVisible = false
        binding.loadingViewLayout.isVisible = true
        setShimmerVisibility(View.VISIBLE)
        binding.recyclerView.isVisible = false
    }

    /**
     * Replaces the current view in the loadedViewParentLayout with the provided custom view.
     *
     * @param view The custom view to display in place of the default loaded view.
     */
    fun setCustomView(view: View?) {
        if (view != null) {
            binding.loadedViewParentLayout.removeAllViews()
            binding.loadingViewLayout.isVisible = false
            binding.loadedViewParentLayout.addView(view)
        }
    }

    /**
     * Sets the error message text to display in the error view.
     *
     * @param errorText Text to display for error.
     */
    fun setErrorStateText(errorText: String?) {
        if (!errorText.isNullOrEmpty()) {
            binding.tvError.text = errorText
        }
    }


    // ==================== Style Setters ====================

    /**
     * Sets the background color for the view.
     */
    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        this.viewBackgroundColor = backgroundColor
        super.setCardBackgroundColor(backgroundColor)
    }

    /**
     * Sets a drawable as the background for the view.
     */
    @Suppress("DEPRECATION")
    override fun setBackgroundDrawable(backgroundDrawable: Drawable?) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable
            super.setBackgroundDrawable(backgroundDrawable)
        }
    }

    /**
     * Sets the corner radius for the view's card.
     */
    fun setCornerRadius(@Dimension cornerRadius: Int) {
        this.viewCornerRadius = cornerRadius
        super.setRadius(cornerRadius.toFloat())
    }

    /**
     * Sets the stroke width for the view's border.
     */
    override fun setStrokeWidth(@Dimension strokeWidth: Int) {
        this.viewStrokeWidth = strokeWidth
        super.setStrokeWidth(strokeWidth)
    }

    /**
     * Sets the color of the view's border.
     */
    override fun setStrokeColor(@ColorInt strokeColor: Int) {
        this.viewStrokeColor = strokeColor
        super.setStrokeColor(strokeColor)
    }

    /**
     * Sets the text color for the title.
     */
    fun setTitleTextColor(@ColorInt titleTextColor: Int) {
        this.titleTextColor = titleTextColor
        binding.title.setTextColor(titleTextColor)
    }

    /**
     * Sets the text appearance for the title.
     */
    fun setTitleTextAppearance(@StyleRes titleTextAppearance: Int) {
        this.titleTextAppearance = titleTextAppearance
        if (titleTextAppearance != 0) {
            binding.title.setTextAppearance(titleTextAppearance)
        }
    }

    /**
     * Sets the close icon drawable.
     */
    fun setCloseIcon(closeIcon: Drawable?) {
        this.closeIcon = closeIcon
        closeIcon?.let { binding.ivClose.setImageDrawable(it) }
    }

    /**
     * Sets the tint color for the close icon.
     */
    fun setCloseIconTint(@ColorInt closeIconTint: Int) {
        this.closeIconTint = closeIconTint
        binding.ivClose.setColorFilter(closeIconTint)
    }

    /**
     * Sets the background color for each item in the list.
     */
    fun setItemBackgroundColor(@ColorInt itemBackgroundColor: Int) {
        this.itemBackgroundColor = itemBackgroundColor
        aiConversationSummaryAdapter.setItemBackgroundColor(itemBackgroundColor)
    }

    /**
     * Sets a custom drawable as the background for each item in the list.
     */
    fun setItemBackgroundDrawable(itemBackgroundDrawable: Drawable?) {
        this.itemBackgroundDrawable = itemBackgroundDrawable
        aiConversationSummaryAdapter.setItemBackgroundDrawable(itemBackgroundDrawable)
    }

    /**
     * Sets the corner radius for each item in the list.
     */
    fun setItemCornerRadius(@Dimension itemCornerRadius: Int) {
        this.itemCornerRadius = itemCornerRadius
        aiConversationSummaryAdapter.setItemCornerRadius(itemCornerRadius)
    }

    /**
     * Sets the width of the stroke for each item in the list.
     */
    fun setItemStrokeWidth(@Dimension itemStrokeWidth: Int) {
        this.itemStrokeWidth = itemStrokeWidth
        aiConversationSummaryAdapter.setItemStrokeWidth(itemStrokeWidth)
    }

    /**
     * Sets the color of the stroke for each item in the list.
     */
    fun setItemStrokeColor(@ColorInt itemStrokeColor: Int) {
        this.itemStrokeColor = itemStrokeColor
        aiConversationSummaryAdapter.setItemStrokeColor(itemStrokeColor)
    }

    /**
     * Sets the text color for each item in the list.
     */
    fun setItemTextColor(@ColorInt itemTextColor: Int) {
        this.itemTextColor = itemTextColor
        aiConversationSummaryAdapter.setItemTextColor(itemTextColor)
    }

    /**
     * Sets the text appearance for each item in the list.
     */
    fun setItemTextAppearance(@StyleRes itemTextAppearance: Int) {
        this.itemTextAppearance = itemTextAppearance
        aiConversationSummaryAdapter.setItemTextAppearance(itemTextAppearance)
    }

    /**
     * Sets the text color of the error state.
     */
    fun setErrorStateTextColor(@ColorInt color: Int) {
        this.errorStateTextColor = color
        binding.tvError.setTextColor(color)
    }

    /**
     * Sets the appearance of the error state text.
     */
    fun setErrorStateTextAppearance(@StyleRes errorStateTextAppearance: Int) {
        this.errorStateTextAppearance = errorStateTextAppearance
        if (errorStateTextAppearance != 0) {
            binding.tvError.setTextAppearance(errorStateTextAppearance)
        }
    }

    /**
     * Sets the layout resource to use for the error view.
     */
    fun setErrorViewLayout(@LayoutRes errorViewLayout: Int) {
        if (errorViewLayout != 0) {
            this.errorViewLayout = errorViewLayout
            binding.errorViewLayout.removeAllViews()
            val view = View.inflate(context, errorViewLayout, null)
            binding.errorViewLayout.addView(view)
        }
    }

    /**
     * Sets the layout resource to use for the loading view.
     */
    fun setLoadingViewLayout(@LayoutRes loadingViewLayout: Int) {
        if (loadingViewLayout != 0) {
            this.loadingViewLayout = loadingViewLayout
            binding.loadingViewLayout.removeAllViews()
            val view = View.inflate(context, loadingViewLayout, null)
            binding.loadingViewLayout.addView(view)
        }
    }

    /**
     * Sets the style of the AIConversationSummary from a specific style resource.
     */
    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            this.viewStyle = style
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatAIConversationSummary
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    /**
     * Applies a typed style object to the AI conversation summary view.
     *
     * This method allows programmatic styling without XML resources by accepting
     * a [CometChatAIConversationSummaryStyle] object containing all style properties.
     *
     * @param style The [CometChatAIConversationSummaryStyle] to apply
     */
    fun setStyle(style: CometChatAIConversationSummaryStyle) {
        // Title styling
        setTitleTextColor(style.titleTextColor)
        if (style.titleTextAppearance != 0) {
            setTitleTextAppearance(style.titleTextAppearance)
        }

        // Close icon styling
        setCloseIconTint(style.closeIconTint)
        style.closeIconDrawable?.let { setCloseIcon(it) }

        // Container styling
        setBackgroundColor(style.backgroundColor)
        style.backgroundDrawable?.let { setBackgroundDrawable(it) }
        setCornerRadius(style.cornerRadius)
        setStrokeWidth(style.strokeWidth)
        setStrokeColor(style.strokeColor)

        // Item styling
        if (style.itemBackgroundColor != 0) {
            setItemBackgroundColor(style.itemBackgroundColor)
        }
        style.itemBackgroundDrawable?.let { setItemBackgroundDrawable(it) }
        setItemCornerRadius(style.itemCornerRadius)
        setItemStrokeWidth(style.itemStrokeWidth)
        setItemStrokeColor(style.itemStrokeColor)
        setItemTextColor(style.itemTextColor)
        if (style.itemTextAppearance != 0) {
            setItemTextAppearance(style.itemTextAppearance)
        }

        // Error state styling
        setErrorStateTextColor(style.errorStateTextColor)
        if (style.errorStateTextAppearance != 0) {
            setErrorStateTextAppearance(style.errorStateTextAppearance)
        }
    }

    /**
     * Limits the view's height to maxHeight if specified.
     *
     * @param maxHeightInDp Maximum height in dp for the view.
     */
    fun setMaxHeight(maxHeightInDp: Int) {
        if (maxHeightInDp > 0) {
            this.maxHeight = Utils.convertDpToPx(context, maxHeightInDp)
            requestLayout()
        }
    }

    /**
     * Sets the click listener to handle item selection events in the RecyclerView.
     */
    fun setOnItemClickListener(onClick: OnItemClickListener?) {
        this.onItemClickListener = onClick
    }

    /**
     * Sets the click listener for the close icon.
     */
    fun setOnCloseClickListener(onCloseClick: OnCloseClickListener?) {
        this.onCloseClickListener = onCloseClick
    }


    // ==================== Getters ====================

    /**
     * Gets the background color applied to the component.
     */
    @ColorInt
    fun getBackgroundColor(): Int = viewBackgroundColor

    /**
     * Retrieves the background drawable for the component.
     */
    fun getBackgroundDrawable(): Drawable? = backgroundDrawable

    /**
     * Gets the corner radius for the component.
     */
    @Dimension
    fun getCornerRadius(): Int = viewCornerRadius

    /**
     * Retrieves the stroke width for the component's border.
     */
    @Dimension
    override fun getStrokeWidth(): Int = viewStrokeWidth

    /**
     * Gets the color of the stroke around the component.
     */
    @ColorInt
    override fun getStrokeColor(): Int = viewStrokeColor

    /**
     * Gets the text color for the title.
     */
    @ColorInt
    fun getTitleTextColor(): Int = titleTextColor

    /**
     * Gets the text appearance resource ID for the title.
     */
    @StyleRes
    fun getTitleTextAppearance(): Int = titleTextAppearance

    /**
     * Gets the close icon drawable.
     */
    fun getCloseIcon(): Drawable? = closeIcon

    /**
     * Gets the tint color for the close icon.
     */
    @ColorInt
    fun getCloseIconTint(): Int = closeIconTint

    /**
     * Retrieves the background color for each item in the list.
     */
    @ColorInt
    fun getItemBackgroundColor(): Int = itemBackgroundColor

    /**
     * Gets the background drawable for each item in the list.
     */
    fun getItemBackgroundDrawable(): Drawable? = itemBackgroundDrawable

    /**
     * Retrieves the corner radius for each item in the list.
     */
    @Dimension
    fun getItemCornerRadius(): Int = itemCornerRadius

    /**
     * Gets the stroke width for each item in the list.
     */
    @Dimension
    fun getItemStrokeWidth(): Int = itemStrokeWidth

    /**
     * Retrieves the color of the stroke for each item in the list.
     */
    @ColorInt
    fun getItemStrokeColor(): Int = itemStrokeColor

    /**
     * Gets the text color for each item in the list.
     */
    @ColorInt
    fun getItemTextColor(): Int = itemTextColor

    /**
     * Retrieves the text appearance style resource for each item.
     */
    @StyleRes
    fun getItemTextAppearance(): Int = itemTextAppearance

    /**
     * Gets the text color for error state messages.
     */
    @ColorInt
    fun getErrorStateTextColor(): Int = errorStateTextColor

    /**
     * Retrieves the text appearance style for error state messages.
     */
    @StyleRes
    fun getErrorStateTextAppearance(): Int = errorStateTextAppearance

    /**
     * Gets the layout resource for the error view.
     */
    @LayoutRes
    fun getErrorViewLayout(): Int = errorViewLayout

    /**
     * Retrieves the layout resource for the loading view.
     */
    @LayoutRes
    fun getLoadingViewLayout(): Int = loadingViewLayout

    /**
     * Gets the style resource associated with the component.
     */
    @StyleRes
    fun getStyle(): Int = viewStyle

    /**
     * Retrieves the maximum height for the component.
     */
    @Dimension
    fun getMaxHeight(): Int = maxHeight

    /**
     * Retrieves the OnItemClickListener for click events on the component.
     */
    fun getOnItemClickListener(): OnItemClickListener? = onItemClickListener

    /**
     * Retrieves the OnCloseClickListener for close icon click events.
     */
    fun getOnCloseClickListener(): OnCloseClickListener? = onCloseClickListener

    /**
     * Provides access to the binding instance associated with this component.
     */
    fun getBinding(): CometchatAiConversationSummaryBinding = binding

    /**
     * Provides the adapter for managing conversation summary items.
     */
    fun getAIConversationSummaryAdapter(): AIConversationSummaryAdapter = aiConversationSummaryAdapter

    // ==================== Private Helper Methods ====================

    /**
     * Stops the shimmer loading effect.
     */
    private fun stopShimmer() {
        if (loadingViewLayout == 0) {
            binding.shimmerEffectFrame.stopShimmer()
        }
    }

    /**
     * Sets the visibility of the shimmer effect.
     */
    private fun setShimmerVisibility(visibility: Int) {
        if (loadingViewLayout == 0) {
            if (visibility == View.GONE) {
                binding.shimmerEffectFrame.stopShimmer()
            } else {
                val adapter = CometChatShimmerAdapter(
                    4, R.layout.cometchat_ai_conversation_summary_shimmer
                )
                binding.shimmerRecyclerview.adapter = adapter
                binding.shimmerEffectFrame.setShimmer(
                    CometChatShimmerUtils.getCometChatShimmerConfig(context)
                )
                binding.shimmerEffectFrame.startShimmer()
            }
            binding.shimmerParentLayout.isVisible = visibility == View.VISIBLE
        }
    }

    /**
     * Interface defining a click event callback for item selection.
     */
    fun interface OnItemClickListener {
        /**
         * Called when an item is clicked.
         *
         * @param view The view that was clicked.
         * @param summary The summary text.
         * @param position Position of the clicked item.
         */
        fun onClick(view: View, summary: String, position: Int)
    }

    /**
     * Interface defining a click event callback for close icon.
     */
    fun interface OnCloseClickListener {
        /**
         * Called when the close icon is clicked.
         *
         * @param view The view that was clicked.
         */
        fun onClick(view: View)
    }
}
