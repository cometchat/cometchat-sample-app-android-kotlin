package com.cometchat.uikit.kotlin.presentation.shared.toolbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * CometChatToolbar is a reusable toolbar component that displays a title,
 * navigation icon, and optional action buttons. Supports selection mode
 * with count display.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.shared.toolbar.CometChatToolbar
 *     android:id="@+id/toolbar"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatToolbarTitle="Conversations"
 *     app:cometchatToolbarBackIconVisibility="visible" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val toolbar = CometChatToolbar(context)
 * toolbar.setTitle("Conversations")
 * toolbar.setOnBackPress { finish() }
 * ```
 */
class CometChatToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatToolbar::class.java.simpleName
        private const val DEFAULT_HEIGHT_DP = 56
    }

    // Views
    private val backIconView: ImageView
    private val discardIconView: ImageView  // Placed after back icon, before title/selection count
    private val titleTextView: TextView
    private val selectionCountTextView: TextView
    private val actionsContainer: LinearLayout
    private val submitIconView: ImageView
    private val separatorView: View

    // State
    private var isSelectionMode: Boolean = false
    private var selectionCount: Int = 0
    private var savedBackIconVisibility: Int = View.GONE  // Save back icon visibility before selection mode

    // Callbacks
    private var onBackPress: (() -> Unit)? = null
    private var onDiscardSelection: (() -> Unit)? = null
    private var onSubmitSelection: (() -> Unit)? = null

    // Style properties
    @ColorInt private var toolbarBackgroundColor: Int = 0
    @ColorInt private var titleTextColor: Int = 0
    @StyleRes private var titleTextAppearance: Int = 0
    @ColorInt private var backIconTint: Int = 0
    @DrawableRes private var backIconRes: Int = 0
    @Dimension private var toolbarHeight: Int = 0
    private var showSeparator: Boolean = true
    @ColorInt private var separatorColor: Int = 0
    @Dimension private var separatorHeight: Int = 0
    @ColorInt private var discardIconTint: Int = 0
    @DrawableRes private var discardIconRes: Int = 0
    @ColorInt private var submitIconTint: Int = 0
    @DrawableRes private var submitIconRes: Int = 0
    @ColorInt private var selectionCountTextColor: Int = 0
    @StyleRes private var selectionCountTextAppearance: Int = 0

    init {
        orientation = VERTICAL
        
        // Create main toolbar container
        val toolbarContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                Utils.convertDpToPx(context, DEFAULT_HEIGHT_DP)
            )
            // Match reference chatuikit padding: paddingStart/End = 16dp, paddingTop/Bottom = 8dp
            setPadding(
                Utils.convertDpToPx(context, 16),
                Utils.convertDpToPx(context, 8),
                Utils.convertDpToPx(context, 16),
                Utils.convertDpToPx(context, 8)
            )
        }

        // Back icon
        backIconView = ImageView(context).apply {
            layoutParams = (LayoutParams(
                Utils.convertDpToPx(context, 32),
                Utils.convertDpToPx(context, 32)
            ) as LinearLayout.LayoutParams).apply {
                marginEnd = Utils.convertDpToPx(context, 8) // matches cometchat_margin_2
            }
            scaleType = ImageView.ScaleType.CENTER
            // No extra padding needed - toolbar container has padding
            contentDescription = context.getString(R.string.cometchat_navigate_back)
            setOnClickListener { onBackPress?.invoke() }
            visibility = GONE
        }
        toolbarContainer.addView(backIconView)

        // Discard icon (for selection mode) - placed after back icon, before title/selection count
        // This matches the Java chatuikit layout structure
        discardIconView = ImageView(context).apply {
            layoutParams = (LayoutParams(
                Utils.convertDpToPx(context, 40),
                Utils.convertDpToPx(context, 40)
            ) as LinearLayout.LayoutParams).apply {
                marginEnd = Utils.convertDpToPx(context, 8) // matches cometchat_margin_2
            }
            scaleType = ImageView.ScaleType.CENTER
            setPadding(
                Utils.convertDpToPx(context, 8),
                Utils.convertDpToPx(context, 8),
                Utils.convertDpToPx(context, 8),
                Utils.convertDpToPx(context, 8)
            )
            contentDescription = context.getString(R.string.cometchat_discard_selection)
            setOnClickListener { onDiscardSelection?.invoke() }
            visibility = View.GONE
        }
        toolbarContainer.addView(discardIconView)

        // Title text view
        titleTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            // No left padding - back icon has marginEnd when visible
            // Right padding for spacing from action buttons
            setPadding(0, 0, Utils.convertDpToPx(context, 16), 0)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        toolbarContainer.addView(titleTextView)

        // Selection count text view (hidden by default)
        // Uses minimal left padding to match Java layout where selection count
        // appears right after the discard icon with only marginEnd spacing
        selectionCountTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(Utils.convertDpToPx(context, 4), 0, Utils.convertDpToPx(context, 16), 0)
            maxLines = 1
            visibility = GONE
        }
        toolbarContainer.addView(selectionCountTextView)

        // Actions container
        actionsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        // Submit icon (for selection mode)
        submitIconView = ImageView(context).apply {
            layoutParams = LayoutParams(
                Utils.convertDpToPx(context, 48),
                Utils.convertDpToPx(context, 48)
            )
            scaleType = ImageView.ScaleType.CENTER
            setPadding(
                Utils.convertDpToPx(context, 16),
                Utils.convertDpToPx(context, 16),
                Utils.convertDpToPx(context, 16),
                Utils.convertDpToPx(context, 16)
            )
            contentDescription = context.getString(R.string.cometchat_submit_selection)
            setOnClickListener { onSubmitSelection?.invoke() }
            visibility = GONE
        }
        actionsContainer.addView(submitIconView)

        toolbarContainer.addView(actionsContainer)
        addView(toolbarContainer)

        // Separator
        separatorView = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                Utils.convertDpToPx(context, 1)
            )
        }
        addView(separatorView)

        // Set default values
        setDefaultValues()
        
        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun setDefaultValues() {
        toolbarBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        titleTextColor = CometChatTheme.getTextColorPrimary(context)
        backIconTint = CometChatTheme.getIconTintPrimary(context)
        backIconRes = R.drawable.cometchat_ic_back
        separatorColor = CometChatTheme.getStrokeColorLight(context)
        separatorHeight = Utils.convertDpToPx(context, 1)
        discardIconTint = CometChatTheme.getIconTintPrimary(context)
        discardIconRes = R.drawable.cometchat_ic_close
        submitIconTint = CometChatTheme.getPrimaryColor(context)
        submitIconRes = R.drawable.cometchat_ic_check
        selectionCountTextColor = CometChatTheme.getTextColorPrimary(context)
        
        applyDefaultStyles()
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatToolbar, defStyleAttr, 0
        )
        extractAttributesAndApplyDefaults(typedArray)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray?) {
        typedArray ?: return
        try {
            toolbarBackgroundColor = typedArray.getColor(
                R.styleable.CometChatToolbar_cometchatToolbarBackgroundColor,
                toolbarBackgroundColor
            )
            titleTextColor = typedArray.getColor(
                R.styleable.CometChatToolbar_cometchatToolbarTitleTextColor,
                titleTextColor
            )
            titleTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatToolbar_cometchatToolbarTitleTextAppearance,
                titleTextAppearance
            )
            backIconTint = typedArray.getColor(
                R.styleable.CometChatToolbar_cometchatToolbarBackIconTint,
                backIconTint
            )
            backIconRes = typedArray.getResourceId(
                R.styleable.CometChatToolbar_cometchatToolbarBackIcon,
                backIconRes
            )
            showSeparator = typedArray.getBoolean(
                R.styleable.CometChatToolbar_cometchatToolbarShowSeparator,
                showSeparator
            )
            separatorColor = typedArray.getColor(
                R.styleable.CometChatToolbar_cometchatToolbarSeparatorColor,
                separatorColor
            )
            
            val title = typedArray.getString(R.styleable.CometChatToolbar_cometchatToolbarTitle)
            if (!title.isNullOrEmpty()) {
                setTitle(title)
            }
            
            val backIconVisibility = typedArray.getInt(
                R.styleable.CometChatToolbar_cometchatToolbarBackIconVisibility,
                View.GONE
            )
            backIconView.visibility = backIconVisibility
            
            applyDefaultStyles()
        } finally {
            typedArray.recycle()
        }
    }

    private fun applyDefaultStyles() {
        setBackgroundColor(toolbarBackgroundColor)
        
        // Apply text appearance first, then text color (text appearance may override color)
        if (titleTextAppearance != 0) {
            titleTextView.setTextAppearance(titleTextAppearance)
        }
        titleTextView.setTextColor(titleTextColor)
        
        backIconView.setImageResource(backIconRes)
        backIconView.setColorFilter(backIconTint)
        
        separatorView.setBackgroundColor(separatorColor)
        separatorView.visibility = if (showSeparator) View.VISIBLE else View.GONE
        
        discardIconView.setImageResource(discardIconRes)
        discardIconView.setColorFilter(discardIconTint)
        
        submitIconView.setImageResource(submitIconRes)
        submitIconView.setColorFilter(submitIconTint)
        
        // Apply text appearance first, then text color (text appearance may override color)
        if (selectionCountTextAppearance != 0) {
            selectionCountTextView.setTextAppearance(selectionCountTextAppearance)
        }
        selectionCountTextView.setTextColor(selectionCountTextColor)
    }


    // Public API

    /**
     * Sets the toolbar title.
     */
    fun setTitle(title: String) {
        titleTextView.text = title
    }

    /**
     * Gets the current toolbar title.
     */
    fun getTitle(): String = titleTextView.text.toString()

    /**
     * Sets the title text color.
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        titleTextColor = color
        titleTextView.setTextColor(color)
    }

    /**
     * Sets the title text appearance.
     */
    fun setTitleTextAppearance(@StyleRes textAppearance: Int) {
        titleTextAppearance = textAppearance
        if (textAppearance != 0) {
            titleTextView.setTextAppearance(textAppearance)
        }
    }

    /**
     * Sets the back icon visibility.
     */
    fun setBackIconVisibility(visibility: Int) {
        backIconView.visibility = visibility
        // Also update saved visibility so it's restored correctly after selection mode
        savedBackIconVisibility = visibility
    }

    /**
     * Sets the back icon drawable.
     */
    fun setBackIcon(@DrawableRes iconRes: Int) {
        backIconRes = iconRes
        backIconView.setImageResource(iconRes)
    }

    /**
     * Sets the back icon drawable.
     */
    fun setBackIcon(drawable: Drawable?) {
        backIconView.setImageDrawable(drawable)
    }

    /**
     * Sets the back icon tint color.
     */
    fun setBackIconTint(@ColorInt color: Int) {
        backIconTint = color
        backIconView.setColorFilter(color)
    }

    /**
     * Sets the callback for back button press.
     */
    fun setOnBackPress(callback: (() -> Unit)?) {
        onBackPress = callback
    }

    /**
     * Sets the callback for discard selection action.
     */
    fun setOnDiscardSelection(callback: (() -> Unit)?) {
        onDiscardSelection = callback
    }

    /**
     * Sets the callback for submit selection action.
     */
    fun setOnSubmitSelection(callback: (() -> Unit)?) {
        onSubmitSelection = callback
    }

    /**
     * Enables or disables selection mode.
     * In selection mode, the title is replaced with selection count,
     * and discard/submit icons are shown.
     */
    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        updateSelectionModeUI()
    }

    /**
     * Sets the selection count to display in selection mode.
     */
    fun setSelectionCount(count: Int) {
        selectionCount = count
        updateSelectionCountText()
    }

    /**
     * Shows or hides the separator line at the bottom of the toolbar.
     */
    fun setShowSeparator(show: Boolean) {
        showSeparator = show
        separatorView.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Sets the separator color.
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        separatorColor = color
        separatorView.setBackgroundColor(color)
    }

    /**
     * Sets the toolbar background color.
     */
    override fun setBackgroundColor(@ColorInt color: Int) {
        toolbarBackgroundColor = color
        super.setBackgroundColor(color)
    }

    /**
     * Adds a custom action view to the toolbar.
     */
    fun addActionView(view: View) {
        actionsContainer.addView(view, 0)
    }

    /**
     * Removes all custom action views from the toolbar.
     */
    fun clearActionViews() {
        // Keep discard and submit icons, remove others
        val childCount = actionsContainer.childCount
        for (i in childCount - 1 downTo 0) {
            val child = actionsContainer.getChildAt(i)
            if (child != discardIconView && child != submitIconView) {
                actionsContainer.removeViewAt(i)
            }
        }
    }

    /**
     * Applies a style to the toolbar.
     */
    fun setStyle(style: CometChatToolbarStyle) {
        if (style.backgroundColor != 0) setBackgroundColor(style.backgroundColor)
        // Apply text appearance first, then text color (text appearance may override color)
        if (style.titleTextAppearance != 0) setTitleTextAppearance(style.titleTextAppearance)
        if (style.titleTextColor != 0) setTitleTextColor(style.titleTextColor)
        if (style.navigationIconTint != 0) setBackIconTint(style.navigationIconTint)
        if (style.navigationIcon != 0) setBackIcon(style.navigationIcon)
        setShowSeparator(style.showSeparator)
        if (style.separatorColor != 0) setSeparatorColor(style.separatorColor)
        if (style.discardIconTint != 0) {
            discardIconTint = style.discardIconTint
            discardIconView.setColorFilter(style.discardIconTint)
        }
        if (style.discardIcon != 0) {
            discardIconRes = style.discardIcon
            discardIconView.setImageResource(style.discardIcon)
        }
        if (style.submitIconTint != 0) {
            submitIconTint = style.submitIconTint
            submitIconView.setColorFilter(style.submitIconTint)
        }
        if (style.submitIcon != 0) {
            submitIconRes = style.submitIcon
            submitIconView.setImageResource(style.submitIcon)
        }
        if (style.selectionCountTextAppearance != 0) {
            selectionCountTextAppearance = style.selectionCountTextAppearance
            selectionCountTextView.setTextAppearance(style.selectionCountTextAppearance)
        }
        if (style.selectionCountTextColor != 0) {
            selectionCountTextColor = style.selectionCountTextColor
            selectionCountTextView.setTextColor(style.selectionCountTextColor)
        }
    }

    /**
     * Sets the discard icon drawable.
     */
    fun setDiscardIcon(drawable: Drawable?) {
        discardIconView.setImageDrawable(drawable)
    }

    /**
     * Sets the discard icon resource.
     */
    fun setDiscardIcon(@DrawableRes iconRes: Int) {
        discardIconRes = iconRes
        discardIconView.setImageResource(iconRes)
    }

    /**
     * Sets the discard icon tint color.
     */
    fun setDiscardIconTint(@ColorInt color: Int) {
        discardIconTint = color
        discardIconView.setColorFilter(color)
    }

    /**
     * Sets the submit icon drawable.
     */
    fun setSubmitIcon(drawable: Drawable?) {
        submitIconView.setImageDrawable(drawable)
    }

    /**
     * Sets the submit icon resource.
     */
    fun setSubmitIcon(@DrawableRes iconRes: Int) {
        submitIconRes = iconRes
        submitIconView.setImageResource(iconRes)
    }

    /**
     * Sets the submit icon tint color.
     */
    fun setSubmitIconTint(@ColorInt color: Int) {
        submitIconTint = color
        submitIconView.setColorFilter(color)
    }

    /**
     * Sets the selection count text color.
     */
    fun setSelectionCountTextColor(@ColorInt color: Int) {
        selectionCountTextColor = color
        selectionCountTextView.setTextColor(color)
    }

    /**
     * Sets the selection count text appearance.
     */
    fun setSelectionCountTextAppearance(@StyleRes textAppearance: Int) {
        selectionCountTextAppearance = textAppearance
        if (textAppearance != 0) {
            selectionCountTextView.setTextAppearance(textAppearance)
        }
    }

    // Private helpers

    private fun updateSelectionModeUI() {
        if (isSelectionMode) {
            // Save current back icon visibility before hiding it
            savedBackIconVisibility = backIconView.visibility
            titleTextView.visibility = GONE
            selectionCountTextView.visibility = VISIBLE
            discardIconView.visibility = VISIBLE
            submitIconView.visibility = VISIBLE
            backIconView.visibility = GONE
            updateSelectionCountText()
        } else {
            titleTextView.visibility = VISIBLE
            selectionCountTextView.visibility = GONE
            discardIconView.visibility = GONE
            submitIconView.visibility = GONE
            // Restore back icon visibility to its previous state
            backIconView.visibility = savedBackIconVisibility
        }
    }

    private fun updateSelectionCountText() {
        // Match Java implementation: show just the count number
        selectionCountTextView.text = selectionCount.toString()
    }
}
