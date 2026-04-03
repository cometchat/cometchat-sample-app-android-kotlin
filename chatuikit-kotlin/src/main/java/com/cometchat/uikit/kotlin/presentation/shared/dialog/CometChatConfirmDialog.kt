package com.cometchat.uikit.kotlin.presentation.shared.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * CometChatConfirmDialog is a customizable confirmation dialog used for
 * actions like delete confirmation, logout confirmation, etc.
 */
class CometChatConfirmDialog(
    context: Context,
    @StyleRes private val themeResId: Int = 0
) : Dialog(context, themeResId) {

    companion object {
        private val TAG = CometChatConfirmDialog::class.java.simpleName
    }

    // Views
    private lateinit var parentCard: MaterialCardView
    private lateinit var cardDialogIcon: MaterialCardView
    private lateinit var ivDialogIcon: ImageView
    private lateinit var tvDialogTitle: TextView
    private lateinit var tvDialogSubtitle: TextView
    private lateinit var btnPositive: MaterialCardView
    private lateinit var btnNegative: MaterialCardView
    private lateinit var tvPositiveButton: TextView
    private lateinit var tvNegativeButton: TextView
    private lateinit var progressPositive: ProgressBar
    private lateinit var progressNegative: ProgressBar

    // Visibility flags
    private var hideTitle = false
    private var hideSubtitle = false
    private var hideDialogIcon = false
    private var hideIconBackground = false
    private var hidePositiveButton = false
    private var hideNegativeButton = false
    private var hideNegativeButtonProgressBar = true
    private var hidePositiveButtonProgressBar = true

    // Text content
    private var titleText: String? = null
    private var subtitleText: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null

    // Click listeners
    private var onPositiveButtonClick: View.OnClickListener? = null
    private var onNegativeButtonClick: View.OnClickListener? = null

    // Style properties
    @ColorInt private var confirmDialogStrokeColor: Int = 0
    @Dimension private var confirmDialogStrokeWidth: Int = 0
    @Dimension private var confirmDialogCornerRadius: Int = 0
    @Dimension private var confirmDialogElevation: Int = 0
    @ColorInt private var confirmDialogBackgroundColor: Int = 0

    private var confirmDialogIcon: Drawable? = null
    @ColorInt private var confirmDialogIconBackgroundColor: Int = 0
    @ColorInt private var confirmDialogIconTint: Int = 0
    @StyleRes private var confirmDialogTitleTextAppearance: Int = 0
    @ColorInt private var confirmDialogTitleTextColor: Int = 0
    @StyleRes private var confirmDialogSubtitleTextAppearance: Int = 0
    @ColorInt private var confirmDialogSubtitleTextColor: Int = 0

    @StyleRes private var confirmDialogPositiveButtonTextAppearance: Int = 0
    @ColorInt private var confirmDialogPositiveButtonTextColor: Int = 0
    @ColorInt private var confirmDialogPositiveButtonBackgroundColor: Int = 0
    @Dimension private var confirmDialogPositiveButtonStrokeWidth: Int = 0
    @ColorInt private var confirmDialogPositiveButtonStrokeColor: Int = 0
    @Dimension private var confirmDialogPositiveButtonRadius: Int = 0

    @StyleRes private var confirmDialogNegativeButtonTextAppearance: Int = 0
    @ColorInt private var confirmDialogNegativeButtonTextColor: Int = 0
    @ColorInt private var confirmDialogNegativeButtonBackgroundColor: Int = 0
    @Dimension private var confirmDialogNegativeButtonStrokeWidth: Int = 0
    @ColorInt private var confirmDialogNegativeButtonStrokeColor: Int = 0
    @Dimension private var confirmDialogNegativeButtonRadius: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cometchat_confirm_dialog)
        initViews()
        applyDefaultValues()
        initClickListener()
        updateUI()
        updateContent()
    }

    private fun initViews() {
        parentCard = findViewById(R.id.parent_card)
        cardDialogIcon = findViewById(R.id.card_dialog_icon)
        ivDialogIcon = findViewById(R.id.iv_dialog_icon)
        tvDialogTitle = findViewById(R.id.tv_dialog_title)
        tvDialogSubtitle = findViewById(R.id.tv_dialog_subtitle)
        btnPositive = findViewById(R.id.btn_positive)
        btnNegative = findViewById(R.id.btn_negative)
        tvPositiveButton = findViewById(R.id.tv_positive_button)
        tvNegativeButton = findViewById(R.id.tv_negative_button)
        progressPositive = findViewById(R.id.progress_bar_positive_button)
        progressNegative = findViewById(R.id.progress_bar_negative_button)
        
        // Initialize MaterialCardViews to remove default Material theme styling
        Utils.initMaterialCard(btnNegative)
        Utils.initMaterialCard(btnPositive)
    }

    private fun initClickListener() {
        btnPositive.setOnClickListener { v ->
            onPositiveButtonClick?.onClick(v)
        }
        btnNegative.setOnClickListener { v ->
            onNegativeButtonClick?.onClick(v)
        }
    }

    private fun applyDefaultValues() {
        configureDialogWindow()

        confirmDialogStrokeColor = CometChatTheme.getStrokeColorLight(context)
        confirmDialogBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        confirmDialogIconBackgroundColor = CometChatTheme.getBackgroundColor2(context)

        confirmDialogTitleTextColor = CometChatTheme.getTextColorPrimary(context)
        confirmDialogSubtitleTextColor = CometChatTheme.getTextColorSecondary(context)

        confirmDialogNegativeButtonTextColor = CometChatTheme.getTextColorPrimary(context)
        confirmDialogNegativeButtonBackgroundColor = CometChatTheme.getTextColorWhite(context)
        confirmDialogNegativeButtonStrokeColor = CometChatTheme.getStrokeColorDark(context)

        confirmDialogPositiveButtonTextColor = CometChatTheme.getColorWhite(context)
        confirmDialogPositiveButtonBackgroundColor = CometChatTheme.getErrorColor(context)

        // Apply theme attributes from the style resource
        val typedArray = if (themeResId != 0) {
            context.obtainStyledAttributes(themeResId, R.styleable.CometChatConfirmDialog)
        } else {
            context.obtainStyledAttributes(R.styleable.CometChatConfirmDialog)
        }
        applyCustomAttributes(typedArray)
    }

    private fun configureDialogWindow() {
        window?.let { w ->
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            w.setBackgroundDrawableResource(android.R.color.transparent)
            val padding = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin_4)
            w.decorView.setPadding(padding, 0, padding, 0)
            w.setGravity(Gravity.CENTER)
        }
    }


    fun setStyle(@StyleRes styleResId: Int) {
        val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatConfirmDialog)
        applyCustomAttributes(typedArray)
    }

    /**
     * Applies a typed style object to the dialog.
     *
     * @param style The CometChatConfirmDialogStyle to apply
     */
    fun setStyle(style: CometChatConfirmDialogStyle) {
        // Container styling
        confirmDialogStrokeColor = style.strokeColor
        confirmDialogStrokeWidth = style.strokeWidth
        confirmDialogCornerRadius = style.cornerRadius
        confirmDialogElevation = style.elevation
        confirmDialogBackgroundColor = style.backgroundColor

        // Icon styling
        confirmDialogIcon = style.icon
        confirmDialogIconTint = style.iconTint
        confirmDialogIconBackgroundColor = style.iconBackgroundColor

        // Title styling
        confirmDialogTitleTextAppearance = style.titleTextAppearance
        confirmDialogTitleTextColor = style.titleTextColor

        // Subtitle styling
        confirmDialogSubtitleTextAppearance = style.subtitleTextAppearance
        confirmDialogSubtitleTextColor = style.subtitleTextColor

        // Positive button styling
        confirmDialogPositiveButtonTextAppearance = style.positiveButtonTextAppearance
        confirmDialogPositiveButtonTextColor = style.positiveButtonTextColor
        confirmDialogPositiveButtonBackgroundColor = style.positiveButtonBackgroundColor
        confirmDialogPositiveButtonStrokeWidth = style.positiveButtonStrokeWidth
        confirmDialogPositiveButtonStrokeColor = style.positiveButtonStrokeColor
        confirmDialogPositiveButtonRadius = style.positiveButtonRadius

        // Negative button styling
        confirmDialogNegativeButtonTextAppearance = style.negativeButtonTextAppearance
        confirmDialogNegativeButtonTextColor = style.negativeButtonTextColor
        confirmDialogNegativeButtonBackgroundColor = style.negativeButtonBackgroundColor
        confirmDialogNegativeButtonStrokeWidth = style.negativeButtonStrokeWidth
        confirmDialogNegativeButtonStrokeColor = style.negativeButtonStrokeColor
        confirmDialogNegativeButtonRadius = style.negativeButtonRadius

        updateUI()
    }

    private fun applyCustomAttributes(typedArray: TypedArray) {
        try {
            confirmDialogStrokeColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogStrokeColor,
                confirmDialogStrokeColor
            )
            confirmDialogStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogStrokeWidth,
                confirmDialogStrokeWidth
            )
            confirmDialogCornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogCornerRadius,
                confirmDialogCornerRadius
            )
            confirmDialogElevation = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogElevation,
                confirmDialogElevation
            )
            confirmDialogBackgroundColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogBackgroundColor,
                confirmDialogBackgroundColor
            )
            confirmDialogIcon = typedArray.getDrawable(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIcon
            ) ?: confirmDialogIcon
            confirmDialogIconBackgroundColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIconBackgroundColor,
                confirmDialogIconBackgroundColor
            )
            confirmDialogIconTint = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogIconTint,
                confirmDialogIconTint
            )
            confirmDialogTitleTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogTitleTextAppearance,
                confirmDialogTitleTextAppearance
            )
            confirmDialogTitleTextColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogTitleTextColor,
                confirmDialogTitleTextColor
            )
            confirmDialogSubtitleTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogSubtitleTextAppearance,
                confirmDialogSubtitleTextAppearance
            )
            confirmDialogSubtitleTextColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogSubtitleTextColor,
                confirmDialogSubtitleTextColor
            )
            confirmDialogPositiveButtonTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonTextAppearance,
                confirmDialogPositiveButtonTextAppearance
            )
            confirmDialogPositiveButtonTextColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonTextColor,
                confirmDialogPositiveButtonTextColor
            )
            confirmDialogPositiveButtonBackgroundColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonBackgroundColor,
                confirmDialogPositiveButtonBackgroundColor
            )
            confirmDialogPositiveButtonStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonStrokeWidth,
                confirmDialogPositiveButtonStrokeWidth
            )
            confirmDialogPositiveButtonStrokeColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonStrokeColor,
                confirmDialogPositiveButtonStrokeColor
            )
            confirmDialogPositiveButtonRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogPositiveButtonRadius,
                confirmDialogPositiveButtonRadius
            )
            confirmDialogNegativeButtonTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonTextAppearance,
                confirmDialogNegativeButtonTextAppearance
            )
            confirmDialogNegativeButtonTextColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonTextColor,
                confirmDialogNegativeButtonTextColor
            )
            confirmDialogNegativeButtonBackgroundColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonBackgroundColor,
                confirmDialogNegativeButtonBackgroundColor
            )
            confirmDialogNegativeButtonStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonStrokeWidth,
                confirmDialogNegativeButtonStrokeWidth
            )
            confirmDialogNegativeButtonStrokeColor = typedArray.getColor(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonStrokeColor,
                confirmDialogNegativeButtonStrokeColor
            )
            confirmDialogNegativeButtonRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatConfirmDialog_cometchatConfirmDialogNegativeButtonRadius,
                confirmDialogNegativeButtonRadius
            )
            updateUI()
        } finally {
            typedArray.recycle()
        }
    }

    private fun updateUI() {
        if (!::parentCard.isInitialized) return

        // Parent card styling
        parentCard.strokeColor = confirmDialogStrokeColor
        parentCard.strokeWidth = confirmDialogStrokeWidth
        parentCard.radius = confirmDialogCornerRadius.toFloat()
        parentCard.cardElevation = confirmDialogElevation.toFloat()
        parentCard.setCardBackgroundColor(confirmDialogBackgroundColor)

        // Icon styling
        confirmDialogIcon?.let { ivDialogIcon.setImageDrawable(it) }
        if (confirmDialogIconTint != 0) {
            ivDialogIcon.setColorFilter(confirmDialogIconTint)
        }
        cardDialogIcon.setCardBackgroundColor(confirmDialogIconBackgroundColor)
        cardDialogIcon.strokeWidth = 0
        cardDialogIcon.cardElevation = 0f

        // Title styling
        tvDialogTitle.setTextColor(confirmDialogTitleTextColor)
        if (confirmDialogTitleTextAppearance != 0) {
            tvDialogTitle.setTextAppearance(confirmDialogTitleTextAppearance)
        }

        // Subtitle styling
        tvDialogSubtitle.setTextColor(confirmDialogSubtitleTextColor)
        if (confirmDialogSubtitleTextAppearance != 0) {
            tvDialogSubtitle.setTextAppearance(confirmDialogSubtitleTextAppearance)
        }

        // Positive button styling
        tvPositiveButton.setTextColor(confirmDialogPositiveButtonTextColor)
        if (confirmDialogPositiveButtonTextAppearance != 0) {
            tvPositiveButton.setTextAppearance(confirmDialogPositiveButtonTextAppearance)
        }
        btnPositive.setCardBackgroundColor(confirmDialogPositiveButtonBackgroundColor)
        btnPositive.strokeWidth = confirmDialogPositiveButtonStrokeWidth
        btnPositive.strokeColor = confirmDialogPositiveButtonStrokeColor
        btnPositive.radius = confirmDialogPositiveButtonRadius.toFloat()

        // Negative button styling
        tvNegativeButton.setTextColor(confirmDialogNegativeButtonTextColor)
        if (confirmDialogNegativeButtonTextAppearance != 0) {
            tvNegativeButton.setTextAppearance(confirmDialogNegativeButtonTextAppearance)
        }
        btnNegative.setCardBackgroundColor(confirmDialogNegativeButtonBackgroundColor)
        btnNegative.strokeWidth = confirmDialogNegativeButtonStrokeWidth
        btnNegative.strokeColor = confirmDialogNegativeButtonStrokeColor
        btnNegative.radius = confirmDialogNegativeButtonRadius.toFloat()
    }

    private fun updateContent() {
        if (!::tvDialogTitle.isInitialized) return

        // Title
        tvDialogTitle.visibility = if (hideTitle) View.GONE else View.VISIBLE
        titleText?.let { tvDialogTitle.text = it }

        // Subtitle
        tvDialogSubtitle.visibility = if (hideSubtitle) View.GONE else View.VISIBLE
        subtitleText?.let { tvDialogSubtitle.text = it }

        // Icon
        cardDialogIcon.visibility = if (hideDialogIcon) View.GONE else View.VISIBLE

        // Positive button
        btnPositive.visibility = if (hidePositiveButton) View.GONE else View.VISIBLE
        positiveButtonText?.let { tvPositiveButton.text = it }
        progressPositive.visibility = if (hidePositiveButtonProgressBar) View.GONE else View.VISIBLE
        tvPositiveButton.visibility = if (hidePositiveButtonProgressBar) View.VISIBLE else View.GONE

        // Negative button
        btnNegative.visibility = if (hideNegativeButton) View.GONE else View.VISIBLE
        negativeButtonText?.let { tvNegativeButton.text = it }
        progressNegative.visibility = if (hideNegativeButtonProgressBar) View.GONE else View.VISIBLE
        tvNegativeButton.visibility = if (hideNegativeButtonProgressBar) View.VISIBLE else View.GONE
    }

    // Public setters
    fun setTitleText(text: String) {
        this.titleText = text
        if (::tvDialogTitle.isInitialized) {
            tvDialogTitle.text = text
        }
    }

    fun setSubtitleText(text: String) {
        this.subtitleText = text
        if (::tvDialogSubtitle.isInitialized) {
            tvDialogSubtitle.text = text
        }
    }

    fun setPositiveButtonText(text: String) {
        this.positiveButtonText = text
        if (::tvPositiveButton.isInitialized) {
            tvPositiveButton.text = text
        }
    }

    fun setNegativeButtonText(text: String) {
        this.negativeButtonText = text
        if (::tvNegativeButton.isInitialized) {
            tvNegativeButton.text = text
        }
    }

    fun setOnPositiveButtonClick(listener: View.OnClickListener?) {
        this.onPositiveButtonClick = listener
    }

    fun setOnNegativeButtonClick(listener: View.OnClickListener?) {
        this.onNegativeButtonClick = listener
    }

    fun hideTitle(hide: Boolean) {
        this.hideTitle = hide
        if (::tvDialogTitle.isInitialized) {
            tvDialogTitle.visibility = if (hide) View.GONE else View.VISIBLE
        }
    }

    fun hideSubtitle(hide: Boolean) {
        this.hideSubtitle = hide
        if (::tvDialogSubtitle.isInitialized) {
            tvDialogSubtitle.visibility = if (hide) View.GONE else View.VISIBLE
        }
    }

    fun hideDialogIcon(hide: Boolean) {
        this.hideDialogIcon = hide
        if (::cardDialogIcon.isInitialized) {
            cardDialogIcon.visibility = if (hide) View.GONE else View.VISIBLE
        }
    }

    fun hidePositiveButton(hide: Boolean) {
        this.hidePositiveButton = hide
        if (::btnPositive.isInitialized) {
            btnPositive.visibility = if (hide) View.GONE else View.VISIBLE
        }
    }

    fun hideNegativeButton(hide: Boolean) {
        this.hideNegativeButton = hide
        if (::btnNegative.isInitialized) {
            btnNegative.visibility = if (hide) View.GONE else View.VISIBLE
        }
    }

    fun showPositiveButtonProgress(show: Boolean) {
        this.hidePositiveButtonProgressBar = !show
        if (::progressPositive.isInitialized) {
            progressPositive.visibility = if (show) View.VISIBLE else View.GONE
            tvPositiveButton.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    fun showNegativeButtonProgress(show: Boolean) {
        this.hideNegativeButtonProgressBar = !show
        if (::progressNegative.isInitialized) {
            progressNegative.visibility = if (show) View.VISIBLE else View.GONE
            tvNegativeButton.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    fun setConfirmDialogIcon(icon: Drawable?) {
        this.confirmDialogIcon = icon
        if (::ivDialogIcon.isInitialized) {
            icon?.let { ivDialogIcon.setImageDrawable(it) }
        }
    }

    fun setConfirmDialogIconTint(@ColorInt color: Int) {
        this.confirmDialogIconTint = color
        if (::ivDialogIcon.isInitialized) {
            ivDialogIcon.setColorFilter(color)
        }
    }

    fun setConfirmDialogIconBackgroundColor(@ColorInt color: Int) {
        this.confirmDialogIconBackgroundColor = color
        if (::cardDialogIcon.isInitialized) {
            cardDialogIcon.setCardBackgroundColor(color)
        }
    }

    fun setConfirmDialogElevation(@Dimension elevation: Int) {
        this.confirmDialogElevation = elevation
        if (::parentCard.isInitialized) {
            parentCard.cardElevation = elevation.toFloat()
        }
    }
    
    fun setConfirmDialogBackgroundColor(@ColorInt color: Int) {
        this.confirmDialogBackgroundColor = color
        if (::parentCard.isInitialized) {
            parentCard.setCardBackgroundColor(color)
        }
    }
    
    fun setConfirmDialogCornerRadius(@Dimension radius: Int) {
        this.confirmDialogCornerRadius = radius
        if (::parentCard.isInitialized) {
            parentCard.radius = radius.toFloat()
        }
    }
    
    fun setConfirmDialogStrokeColor(@ColorInt color: Int) {
        this.confirmDialogStrokeColor = color
        if (::parentCard.isInitialized) {
            parentCard.strokeColor = color
        }
    }
    
    fun setConfirmDialogStrokeWidth(@Dimension width: Int) {
        this.confirmDialogStrokeWidth = width
        if (::parentCard.isInitialized) {
            parentCard.strokeWidth = width
        }
    }
    
    fun setTitleTextColor(@ColorInt color: Int) {
        this.confirmDialogTitleTextColor = color
        if (::tvDialogTitle.isInitialized) {
            tvDialogTitle.setTextColor(color)
        }
    }
    
    fun setSubtitleTextColor(@ColorInt color: Int) {
        this.confirmDialogSubtitleTextColor = color
        if (::tvDialogSubtitle.isInitialized) {
            tvDialogSubtitle.setTextColor(color)
        }
    }
    
    fun setPositiveButtonBackgroundColor(@ColorInt color: Int) {
        this.confirmDialogPositiveButtonBackgroundColor = color
        if (::btnPositive.isInitialized) {
            btnPositive.setCardBackgroundColor(color)
        }
    }
    
    fun setPositiveButtonStrokeColor(@ColorInt color: Int) {
        this.confirmDialogPositiveButtonStrokeColor = color
        if (::btnPositive.isInitialized) {
            btnPositive.strokeColor = color
        }
    }
    
    fun setPositiveButtonStrokeWidth(@Dimension width: Int) {
        this.confirmDialogPositiveButtonStrokeWidth = width
        if (::btnPositive.isInitialized) {
            btnPositive.strokeWidth = width
        }
    }
    
    fun setPositiveButtonCornerRadius(@Dimension radius: Int) {
        this.confirmDialogPositiveButtonRadius = radius
        if (::btnPositive.isInitialized) {
            btnPositive.radius = radius.toFloat()
        }
    }
    
    fun setNegativeButtonBackgroundColor(@ColorInt color: Int) {
        this.confirmDialogNegativeButtonBackgroundColor = color
        if (::btnNegative.isInitialized) {
            btnNegative.setCardBackgroundColor(color)
        }
    }
    
    fun setNegativeButtonStrokeColor(@ColorInt color: Int) {
        this.confirmDialogNegativeButtonStrokeColor = color
        if (::btnNegative.isInitialized) {
            btnNegative.strokeColor = color
        }
    }
    
    fun setNegativeButtonStrokeWidth(@Dimension width: Int) {
        this.confirmDialogNegativeButtonStrokeWidth = width
        if (::btnNegative.isInitialized) {
            btnNegative.strokeWidth = width
        }
    }
    
    fun setNegativeButtonCornerRadius(@Dimension radius: Int) {
        this.confirmDialogNegativeButtonRadius = radius
        if (::btnNegative.isInitialized) {
            btnNegative.radius = radius.toFloat()
        }
    }

    fun hideIconBackground(hide: Boolean) {
        this.hideIconBackground = hide
        if (::cardDialogIcon.isInitialized) {
            cardDialogIcon.setCardBackgroundColor(
                if (hide) android.graphics.Color.TRANSPARENT else confirmDialogIconBackgroundColor
            )
        }
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        this.confirmDialogTitleTextAppearance = appearance
        if (::tvDialogTitle.isInitialized && appearance != 0) {
            tvDialogTitle.setTextAppearance(appearance)
        }
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        this.confirmDialogSubtitleTextAppearance = appearance
        if (::tvDialogSubtitle.isInitialized && appearance != 0) {
            tvDialogSubtitle.setTextAppearance(appearance)
        }
    }

    fun setPositiveButtonTextColor(@ColorInt color: Int) {
        this.confirmDialogPositiveButtonTextColor = color
        if (::tvPositiveButton.isInitialized) {
            tvPositiveButton.setTextColor(color)
        }
    }

    fun setNegativeButtonTextColor(@ColorInt color: Int) {
        this.confirmDialogNegativeButtonTextColor = color
        if (::tvNegativeButton.isInitialized) {
            tvNegativeButton.setTextColor(color)
        }
    }

    fun setPositiveButtonTextAppearance(@StyleRes appearance: Int) {
        this.confirmDialogPositiveButtonTextAppearance = appearance
        if (::tvPositiveButton.isInitialized && appearance != 0) {
            tvPositiveButton.setTextAppearance(appearance)
        }
    }

    fun setNegativeButtonTextAppearance(@StyleRes appearance: Int) {
        this.confirmDialogNegativeButtonTextAppearance = appearance
        if (::tvNegativeButton.isInitialized && appearance != 0) {
            tvNegativeButton.setTextAppearance(appearance)
        }
    }
}
