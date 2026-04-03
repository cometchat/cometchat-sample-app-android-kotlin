package com.cometchat.uikit.kotlin.presentation.report

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.FlagDetail
import com.cometchat.chat.models.FlagReason
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView

/**
 * CometChatFlagMessageDialog is a dialog for reporting/flagging messages.
 * It displays flag reasons as selectable chips and allows users to add remarks.
 *
 * @param context The context in which the dialog should run
 * @param message The message to be flagged
 */
class CometChatFlagMessageDialog(
    context: Context,
    private val message: BaseMessage
) : Dialog(context) {

    companion object {
        private val TAG = CometChatFlagMessageDialog::class.java.simpleName
    }

    // Views
    private lateinit var rootCard: MaterialCardView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivClose: ImageView
    private lateinit var flexboxChips: FlexboxLayout
    private lateinit var tvReasonLabel: TextView
    private lateinit var tvReasonOptional: TextView
    private lateinit var tilLayout: MaterialCardView
    private lateinit var etRemark: EditText
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnCancel: MaterialCardView
    private lateinit var btnReport: MaterialCardView
    private lateinit var tvCancel: TextView
    private lateinit var tvReport: TextView
    private lateinit var progressBarPositiveButton: ProgressBar

    // Data
    private var flagReasons: List<FlagReason> = emptyList()
    private var selectedChipPosition: Int = -1
    private var isReportButtonEnabled: Boolean = false

    // Localization map for flag reasons
    private val flagReasonMap: MutableMap<String, Int> = mutableMapOf(
        "spam" to R.string.cometchat_flag_reason_spam,
        "sexual" to R.string.cometchat_flag_reason_sexual,
        "harassment" to R.string.cometchat_flag_reason_harassment
    )

    // Listeners
    private var onCancelClickListener: OnClick? = null
    private var onReportClickListener: OnReportClickListener? = null
    private var onCloseClickListener: OnClick? = null

    // Custom text properties
    private var customTitle: String? = null
    private var customDescription: String? = null
    private var customRemarkHint: String? = null
    private var customCancelButtonText: String? = null
    private var customReportButtonText: String? = null

    // Style properties - Dialog
    @ColorInt private var backgroundColor: Int = 0
    @Dimension private var borderRadius: Int = 0
    @ColorInt private var titleColor: Int = 0
    @ColorInt private var subtitleTextColor: Int = 0
    @StyleRes private var titleTextAppearance: Int = 0
    @StyleRes private var subtitleTextAppearance: Int = 0
    @ColorInt private var closeIconColor: Int = 0
    @ColorInt private var strokeColor: Int = 0
    @Dimension private var strokeWidth: Int = 0

    // Style properties - Chips
    @Dimension private var chipCornerRadius: Int = 0
    @Dimension private var chipStrokeWidth: Int = 0
    @StyleRes private var chipTextAppearance: Int = 0
    @ColorInt private var chipActiveBackgroundColor: Int = 0
    @ColorInt private var chipInactiveBackgroundColor: Int = 0
    @ColorInt private var chipActiveTextColor: Int = 0
    @ColorInt private var chipInactiveTextColor: Int = 0
    @ColorInt private var chipActiveBorderColor: Int = 0
    @ColorInt private var chipInactiveBorderColor: Int = 0

    // Style properties - Remark field
    @ColorInt private var remarkFieldTitleTextColor: Int = 0
    @StyleRes private var remarkFieldTitleTextAppearance: Int = 0
    @ColorInt private var remarkFieldHintTextColor: Int = 0
    @ColorInt private var remarkFieldTextColor: Int = 0
    @ColorInt private var remarkFieldBackgroundColor: Int = 0
    @StyleRes private var remarkFieldTextAppearance: Int = 0

    // Style properties - Buttons
    @Dimension private var buttonCornerRadius: Int = 0
    @ColorInt private var buttonStrokeColor: Int = 0
    @Dimension private var buttonStrokeWidth: Int = 0
    @ColorInt private var reportButtonEnabledBackgroundColor: Int = 0
    @ColorInt private var reportButtonDisabledBackgroundColor: Int = 0
    @ColorInt private var reportButtonEnabledTextColor: Int = 0
    @ColorInt private var reportButtonDisabledTextColor: Int = 0
    @ColorInt private var cancelButtonEnabledBackgroundColor: Int = 0
    @ColorInt private var cancelButtonDisabledBackgroundColor: Int = 0
    @ColorInt private var cancelButtonEnabledTextColor: Int = 0
    @ColorInt private var cancelButtonDisabledTextColor: Int = 0

    // Style properties - Error and Progress
    @ColorInt private var errorTextColor: Int = 0
    @ColorInt private var progressIndicatorColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cometchat_flag_message_dialog)
        initViews()
        applyDefaultValues()
        setupListeners()
        updateReportButtonState(false)
    }

    private fun initViews() {
        rootCard = findViewById(R.id.root_card)
        tvTitle = findViewById(R.id.tv_title)
        tvDescription = findViewById(R.id.tv_description)
        ivClose = findViewById(R.id.iv_close)
        flexboxChips = findViewById(R.id.flexbox_chips)
        tvReasonLabel = findViewById(R.id.tv_reason_label)
        tvReasonOptional = findViewById(R.id.tv_reason_optional)
        tilLayout = findViewById(R.id.til_layout)
        etRemark = findViewById(R.id.et_remark)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        btnCancel = findViewById(R.id.btn_cancel)
        btnReport = findViewById(R.id.btn_report)
        tvCancel = findViewById(R.id.tv_cancel)
        tvReport = findViewById(R.id.tv_report)
        progressBarPositiveButton = findViewById(R.id.progress_bar_positive_button)

        // Initialize MaterialCardViews
        Utils.initMaterialCard(btnCancel)
        Utils.initMaterialCard(btnReport)
    }

    private fun applyDefaultValues() {
        configureDialogWindow()

        // Dialog styling defaults
        backgroundColor = CometChatTheme.getBackgroundColor1(context)
        borderRadius = context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_4)
        strokeColor = CometChatTheme.getStrokeColorDefault(context)
        strokeWidth = Utils.convertDpToPx(context, 1)
        titleColor = CometChatTheme.getTextColorPrimary(context)
        subtitleTextColor = CometChatTheme.getTextColorSecondary(context)
        closeIconColor = CometChatTheme.getIconTintPrimary(context)

        // Chip styling defaults
        chipCornerRadius = context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max)
        chipStrokeWidth = Utils.convertDpToPx(context, 1)
        chipActiveBackgroundColor = CometChatTheme.getExtendedPrimaryColor100(context)
        chipInactiveBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        chipActiveTextColor = CometChatTheme.getTextColorHighlight(context)
        chipInactiveTextColor = CometChatTheme.getTextColorPrimary(context)
        chipActiveBorderColor = CometChatTheme.getExtendedPrimaryColor200(context)
        chipInactiveBorderColor = CometChatTheme.getStrokeColorDefault(context)

        // Remark field defaults
        remarkFieldTitleTextColor = CometChatTheme.getTextColorPrimary(context)
        remarkFieldHintTextColor = CometChatTheme.getTextColorTertiary(context)
        remarkFieldTextColor = CometChatTheme.getTextColorPrimary(context)
        remarkFieldBackgroundColor = CometChatTheme.getBackgroundColor2(context)

        // Button defaults
        buttonCornerRadius = context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_2)
        buttonStrokeColor = CometChatTheme.getStrokeColorDark(context)
        buttonStrokeWidth = Utils.convertDpToPx(context, 1)
        reportButtonEnabledBackgroundColor = CometChatTheme.getPrimaryButtonBackgroundColor(context)
        reportButtonDisabledBackgroundColor = CometChatTheme.getBackgroundColor4(context)
        reportButtonEnabledTextColor = CometChatTheme.getColorWhite(context)
        reportButtonDisabledTextColor = CometChatTheme.getColorWhite(context)
        cancelButtonEnabledBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        cancelButtonDisabledBackgroundColor = CometChatTheme.getBackgroundColor1(context)
        cancelButtonEnabledTextColor = CometChatTheme.getTextColorPrimary(context)
        cancelButtonDisabledTextColor = CometChatTheme.getTextColorPrimary(context)

        // Error and progress defaults
        errorTextColor = CometChatTheme.getErrorColor(context)
        progressIndicatorColor = Color.BLUE

        applyStyles()
        applyCustomTexts()
    }

    /**
     * Applies custom text values to views if they have been set.
     */
    private fun applyCustomTexts() {
        customTitle?.let { tvTitle.text = it }
        customDescription?.let { tvDescription.text = it }
        customRemarkHint?.let { etRemark.hint = it }
        customCancelButtonText?.let { tvCancel.text = it }
        customReportButtonText?.let { tvReport.text = it }
    }

    private fun configureDialogWindow() {
        window?.let { w ->
            w.setBackgroundDrawableResource(android.R.color.transparent)
            val padding = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin_4)
            w.decorView.setPadding(padding, 0, padding, 0)
            w.setGravity(Gravity.CENTER)
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setupListeners() {
        ivClose.setOnClickListener {
            onCloseClickListener?.onClick() ?: dismiss()
        }

        btnCancel.setOnClickListener {
            onCancelClickListener?.onClick() ?: dismiss()
        }

        btnReport.setOnClickListener {
            if (isReportButtonEnabled && onReportClickListener != null) {
                val selectedReason = if (selectedChipPosition >= 0 && selectedChipPosition < flagReasons.size) {
                    flagReasons[selectedChipPosition]
                } else null

                val flagDetail = FlagDetail().apply {
                    selectedReason?.let {
                        reasonId = it.id
                        remark = getRemarkText()
                    }
                }
                onReportClickListener?.onReportClick(flagDetail)
            }
        }
    }


    private fun applyStyles() {
        if (!::rootCard.isInitialized) return

        // Dialog styling
        rootCard.setCardBackgroundColor(backgroundColor)
        rootCard.radius = borderRadius.toFloat()
        rootCard.strokeColor = strokeColor
        rootCard.strokeWidth = strokeWidth

        // Title styling
        tvTitle.setTextColor(titleColor)
        if (titleTextAppearance != 0) {
            tvTitle.setTextAppearance(titleTextAppearance)
        }

        // Subtitle styling
        tvDescription.setTextColor(subtitleTextColor)
        if (subtitleTextAppearance != 0) {
            tvDescription.setTextAppearance(subtitleTextAppearance)
        }

        // Close icon styling
        ivClose.setColorFilter(closeIconColor)

        // Remark field styling
        tvReasonLabel.setTextColor(remarkFieldTitleTextColor)
        if (remarkFieldTitleTextAppearance != 0) {
            tvReasonLabel.setTextAppearance(remarkFieldTitleTextAppearance)
        }
        etRemark.setHintTextColor(remarkFieldHintTextColor)
        etRemark.setTextColor(remarkFieldTextColor)
        if (remarkFieldTextAppearance != 0) {
            etRemark.setTextAppearance(remarkFieldTextAppearance)
        }
        tilLayout.setCardBackgroundColor(remarkFieldBackgroundColor)

        // Error text styling
        tvErrorMessage.setTextColor(errorTextColor)

        // Cancel button styling
        btnCancel.setCardBackgroundColor(cancelButtonEnabledBackgroundColor)
        btnCancel.strokeColor = buttonStrokeColor
        btnCancel.strokeWidth = buttonStrokeWidth
        btnCancel.radius = buttonCornerRadius.toFloat()
        tvCancel.setTextColor(cancelButtonEnabledTextColor)

        // Report button styling (initial disabled state)
        updateReportButtonState(isReportButtonEnabled)
    }

    private fun createChipView(flagReason: FlagReason, position: Int): MaterialCardView {
        val chipCard = MaterialCardView(context)
        val params = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val marginPx = Utils.convertDpToPx(context, 6f).toInt()
        params.setMargins(0, 0, marginPx, marginPx)
        chipCard.layoutParams = params
        chipCard.cardElevation = 0f

        val chipText = TextView(context)
        chipText.text = getLocalizedFlagReasonName(flagReason)
        chipText.setTextColor(chipInactiveTextColor)
        if (chipTextAppearance != 0) {
            chipText.setTextAppearance(chipTextAppearance)
        }
        val paddingHorizontal = Utils.convertDpToPx(context, 12f).toInt()
        val paddingVertical = Utils.convertDpToPx(context, 4f).toInt()
        chipText.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

        chipCard.addView(chipText)
        chipCard.rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
        updateChipStyle(chipCard, chipText, false)

        chipCard.setOnClickListener {
            // Deselect previous chip
            if (selectedChipPosition >= 0 && selectedChipPosition < flexboxChips.childCount) {
                val prevChip = flexboxChips.getChildAt(selectedChipPosition) as? MaterialCardView
                val prevText = prevChip?.getChildAt(0) as? TextView
                if (prevChip != null && prevText != null) {
                    updateChipStyle(prevChip, prevText, false)
                }
            }
            // Select current chip
            selectedChipPosition = position
            updateChipStyle(chipCard, chipText, true)
            updateReportButtonState(true)
        }

        return chipCard
    }

    private fun getLocalizedFlagReasonName(flagReason: FlagReason): String {
        if (flagReason.id.isNullOrEmpty()) return ""

        val reasonId = flagReason.id.lowercase()
        val resourceId = flagReasonMap[reasonId]

        return if (resourceId != null) {
            try {
                context.getString(resourceId)
            } catch (e: Exception) {
                flagReason.name ?: ""
            }
        } else {
            flagReason.name ?: ""
        }
    }

    private fun updateChipStyle(chipCard: MaterialCardView, chipText: TextView, isActive: Boolean) {
        if (isActive) {
            chipCard.setCardBackgroundColor(chipActiveBackgroundColor)
            chipCard.strokeColor = chipActiveBorderColor
            chipText.setTextColor(chipActiveTextColor)
        } else {
            chipCard.setCardBackgroundColor(chipInactiveBackgroundColor)
            chipCard.strokeColor = chipInactiveBorderColor
            chipText.setTextColor(chipInactiveTextColor)
        }
        chipCard.strokeWidth = chipStrokeWidth
        chipCard.radius = chipCornerRadius.toFloat()
    }

    private fun updateReportButtonState(enabled: Boolean) {
        isReportButtonEnabled = enabled
        btnReport.isEnabled = enabled

        if (enabled) {
            btnReport.setCardBackgroundColor(reportButtonEnabledBackgroundColor)
            tvReport.setTextColor(reportButtonEnabledTextColor)
            btnReport.alpha = 1.0f
        } else {
            btnReport.setCardBackgroundColor(reportButtonDisabledBackgroundColor)
            tvReport.setTextColor(reportButtonDisabledTextColor)
            btnReport.alpha = 0.6f
        }

        btnReport.strokeColor = buttonStrokeColor
        btnReport.strokeWidth = buttonStrokeWidth
        btnReport.radius = buttonCornerRadius.toFloat()
    }

    private fun getRemarkText(): String {
        return etRemark.text?.toString() ?: ""
    }

    // ==================== PUBLIC API ====================

    /**
     * Sets a custom title for the dialog.
     * @param title The custom title text to display
     */
    fun setTitle(title: String) {
        customTitle = title
        if (::tvTitle.isInitialized) {
            tvTitle.text = title
        }
    }

    /**
     * Sets a custom description for the dialog.
     * @param description The custom description text to display
     */
    fun setDescription(description: String) {
        customDescription = description
        if (::tvDescription.isInitialized) {
            tvDescription.text = description
        }
    }

    /**
     * Sets a custom hint for the remark input field.
     * @param hint The custom hint text to display
     */
    fun setRemarkHint(hint: String) {
        customRemarkHint = hint
        if (::etRemark.isInitialized) {
            etRemark.hint = hint
        }
    }

    /**
     * Sets custom text for the cancel button.
     * @param text The custom cancel button text to display
     */
    fun setCancelButtonText(text: String) {
        customCancelButtonText = text
        if (::tvCancel.isInitialized) {
            tvCancel.text = text
        }
    }

    /**
     * Sets custom text for the report button.
     * @param text The custom report button text to display
     */
    fun setReportButtonText(text: String) {
        customReportButtonText = text
        if (::tvReport.isInitialized) {
            tvReport.text = text
        }
    }

    /**
     * Sets the list of flag reasons to display as selectable chips.
     */
    fun setFlagReasons(flagReasons: List<FlagReason>?) {
        this.flagReasons = flagReasons ?: emptyList()
        if (::flexboxChips.isInitialized) {
            flexboxChips.removeAllViews()
            selectedChipPosition = -1
            updateReportButtonState(false)

            this.flagReasons.forEachIndexed { index, reason ->
                val chipView = createChipView(reason, index)
                flexboxChips.addView(chipView)
            }
        }
    }

    /**
     * Sets a custom mapping of flag reason IDs to localization string resource IDs.
     */
    fun setLocalizationIdMap(localizationIdMap: Map<String, Int>?) {
        localizationIdMap?.let { flagReasonMap.putAll(it) }
    }

    /**
     * Sets the visibility of the remark input field.
     */
    fun setFlagRemarkInputFieldVisibility(visibility: Int) {
        if (::tvReasonLabel.isInitialized) {
            tvReasonLabel.visibility = visibility
            tvReasonOptional.visibility = visibility
            tilLayout.visibility = visibility
        }
    }

    /**
     * Applies a style resource to the dialog.
     */
    fun setFlagMessageStyle(@StyleRes styleResId: Int) {
        if (styleResId == -1) return
        val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatFlagMessage)
        try {
            loadAttributesFromTypedArray(typedArray)
        } finally {
            typedArray.recycle()
        }
        applyStyles()
    }


    private fun loadAttributesFromTypedArray(typedArray: TypedArray) {
        // Dialog styling
        backgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageBackgroundColor,
            backgroundColor
        )
        borderRadius = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageBorderRadius,
            borderRadius
        )
        titleColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageTitleColor,
            titleColor
        )
        subtitleTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageSubtitleColor,
            subtitleTextColor
        )
        titleTextAppearance = typedArray.getResourceId(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageTitleAppearance,
            titleTextAppearance
        )
        subtitleTextAppearance = typedArray.getResourceId(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageSubtitleTextAppearance,
            subtitleTextAppearance
        )
        closeIconColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageCloseIconColor,
            closeIconColor
        )
        strokeColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageStrokeColor,
            strokeColor
        )
        strokeWidth = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageStrokeWidth,
            strokeWidth
        )

        // Chip styling
        chipCornerRadius = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipCornerRadius,
            chipCornerRadius
        )
        chipStrokeWidth = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipStrokeWidth,
            chipStrokeWidth
        )
        chipTextAppearance = typedArray.getResourceId(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipTextAppearance,
            chipTextAppearance
        )
        chipActiveBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveBackgroundColor,
            chipActiveBackgroundColor
        )
        chipInactiveBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveBackgroundColor,
            chipInactiveBackgroundColor
        )
        chipActiveTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveTextColor,
            chipActiveTextColor
        )
        chipInactiveTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveTextColor,
            chipInactiveTextColor
        )
        chipActiveBorderColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveBorderColor,
            chipActiveBorderColor
        )
        chipInactiveBorderColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveBorderColor,
            chipInactiveBorderColor
        )

        // Remark field styling
        remarkFieldTitleTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTitleTextColor,
            remarkFieldTitleTextColor
        )
        remarkFieldHintTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldHintTextColor,
            remarkFieldHintTextColor
        )
        remarkFieldTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTextColor,
            remarkFieldTextColor
        )
        remarkFieldBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldBackgroundColor,
            remarkFieldBackgroundColor
        )
        remarkFieldTextAppearance = typedArray.getResourceId(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTextAppearance,
            remarkFieldTextAppearance
        )

        // Button styling
        buttonCornerRadius = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonCornerRadius,
            buttonCornerRadius
        )
        buttonStrokeColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonStrokeColor,
            buttonStrokeColor
        )
        buttonStrokeWidth = typedArray.getDimensionPixelSize(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonStrokeWidth,
            buttonStrokeWidth
        )
        reportButtonEnabledBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonEnabledBackgroundColor,
            reportButtonEnabledBackgroundColor
        )
        reportButtonDisabledBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonDisabledBackgroundColor,
            reportButtonDisabledBackgroundColor
        )
        reportButtonEnabledTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonEnabledTextColor,
            reportButtonEnabledTextColor
        )
        reportButtonDisabledTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonDisabledTextColor,
            reportButtonDisabledTextColor
        )
        cancelButtonEnabledBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonEnabledBackgroundColor,
            cancelButtonEnabledBackgroundColor
        )
        cancelButtonDisabledBackgroundColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonDisabledBackgroundColor,
            cancelButtonDisabledBackgroundColor
        )
        cancelButtonEnabledTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonEnabledTextColor,
            cancelButtonEnabledTextColor
        )
        cancelButtonDisabledTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonDisabledTextColor,
            cancelButtonDisabledTextColor
        )

        // Error and progress styling
        errorTextColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageErrorTextColor,
            errorTextColor
        )
        progressIndicatorColor = typedArray.getColor(
            R.styleable.CometChatFlagMessage_cometchatFlagMessageProgressIndicatorColor,
            progressIndicatorColor
        )
    }

    /**
     * Shows the error message view.
     */
    fun onFlagMessageError() {
        if (::tvErrorMessage.isInitialized) {
            tvErrorMessage.visibility = View.VISIBLE
        }
    }

    /**
     * Controls the visibility of the progress bar on the report button.
     */
    fun hidePositiveButtonProgressBar(hide: Boolean) {
        if (::progressBarPositiveButton.isInitialized && ::tvReport.isInitialized) {
            if (hide) {
                progressBarPositiveButton.visibility = View.GONE
                tvReport.visibility = View.VISIBLE
            } else {
                progressBarPositiveButton.visibility = View.VISIBLE
                tvReport.visibility = View.GONE
            }
        }
    }

    /**
     * Gets the base message being flagged.
     */
    fun getMessage(): BaseMessage = message

    // ==================== LISTENER SETTERS ====================

    /**
     * Sets the listener for report button clicks.
     */
    fun setOnPositiveButtonClickListener(listener: OnReportClickListener?) {
        this.onReportClickListener = listener
    }

    /**
     * Sets the listener for cancel button clicks.
     */
    fun setOnCancelButtonClickListener(listener: OnClick?) {
        this.onCancelClickListener = listener
    }

    /**
     * Sets the listener for close button clicks.
     */
    fun setOnCloseButtonClickListener(listener: OnClick?) {
        this.onCloseClickListener = listener
    }

    /**
     * Interface for handling report button click events.
     */
    fun interface OnReportClickListener {
        fun onReportClick(flagDetail: FlagDetail)
    }
}
