package com.cometchat.chatuikit.report;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chat.models.FlagDetail;
import com.cometchat.chat.models.FlagReason;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometChatFlagMessageBinding;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CometChatFlagMessage is a custom MaterialCardView component that provides a user interface
 * for reporting/flagging messages in a chat application. It displays flag reasons as chips,
 * allows users to select a reason and add additional remarks, and provides report/cancel actions.
 */
public class CometChatFlagMessage extends MaterialCardView {

    private CometChatFlagMessageBinding binding;

    // Styling attributes
    private @ColorInt int backgroundColor;
    private @Dimension int borderRadius;
    private @ColorInt int titleColor;
    private @ColorInt int subtitleTextColor;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int closeIconColor;
    private @ColorInt int strokeColor;
    private @Dimension int strokeWidth;

    // Chip styling
    private @Dimension int chipCornerRadius;
    private @Dimension int chipStrokeWidth;
    private @StyleRes int chipTextAppearance;

    // Chip states
    private @ColorInt int chipActiveBackgroundColor;
    private @ColorInt int chipInactiveBackgroundColor;
    private @ColorInt int chipActiveTextColor;
    private @ColorInt int chipInactiveTextColor;
    private @ColorInt int chipActiveBorderColor;
    private @ColorInt int chipInactiveBorderColor;

    // Remark field styling
    private @ColorInt int remarkFieldTitleTextColor;
    private @StyleRes int remarkFieldTitleTextAppearance;
    private @ColorInt int remarkFieldHintTextColor;
    private @ColorInt int remarkFieldTextColor;
    private @ColorInt int remarkFieldBackgroundColor;
    private @StyleRes int remarkFieldTextAppearance;

    // Button styling - General
    private @Dimension int buttonCornerRadius;
    private @ColorInt int buttonStrokeColor;
    private @Dimension int buttonStrokeWidth;

    // Report Button - Enabled/Disabled states
    private @ColorInt int reportButtonEnabledBackgroundColor;
    private @ColorInt int reportButtonDisabledBackgroundColor;
    private @ColorInt int reportButtonEnabledTextColor;
    private @ColorInt int reportButtonDisabledTextColor;

    private @ColorInt int cancelButtonEnabledBackgroundColor;
    private @ColorInt int cancelButtonDisabledBackgroundColor;
    private @ColorInt int cancelButtonEnabledTextColor;
    private @ColorInt int cancelButtonDisabledTextColor;

    // Error styling
    private @ColorInt int errorTextColor;

    // Progress indicator styling
    private @ColorInt int progressIndicatorColor;

    // Data
    private List<FlagReason> flagReasons = new ArrayList<>();
    private int selectedChipPosition = -1;
    private boolean isReportButtonEnabled = false;

    // Listeners
    private OnClick onCancelClickListener;
    private OnReportClickListener onReportClickListener;
    private OnClick onCloseClickListener;
    private final Map<String, Integer> flagReasonMap = new HashMap<String, Integer>() {{
        put("spam", R.string.cometchat_flag_reason_spam);
        put("sexual", R.string.cometchat_flag_reason_sexual);
        put("harassment", R.string.cometchat_flag_reason_harassment);
    }};

    /**
     * Creates a new CometChatFlagMessage with default styling.
     *
     * @param context The context to use for the view
     */
    public CometChatFlagMessage(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Creates a new CometChatFlagMessage with attributes from XML.
     *
     * @param context The context to use for the view
     * @param attrs   The attribute set from XML layout
     */
    public CometChatFlagMessage(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.cometChatFlagMessageStyle);
    }

    /**
     * Creates a new CometChatFlagMessage with custom styling.
     *
     * @param context      The context to use for the view
     * @param attrs        The attribute set from XML layout
     * @param defStyleAttr The default style attribute to apply
     */
    public CometChatFlagMessage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Initializes the CometChatFlagMessage component by inflating the layout,
     * loading style attributes, setting up listeners, and configuring initial state.
     *
     * @param context      The context to use for initialization
     * @param attrs        The attribute set from XML layout
     * @param defStyleAttr The default style attribute to apply
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Inflate layout
        binding = CometChatFlagMessageBinding.inflate(LayoutInflater.from(context), this, true);

        // Load attributes
        applyStyleAttributes(attrs, defStyleAttr);

        // Setup listeners
        setupListeners();

        // Initially disable report button
        updateReportButtonState(false);
    }

    /**
     * Applies styling attributes from the provided attribute set and default style.
     * This method loads both XML attributes and style resource attributes.
     *
     * @param attrs        The attribute set from XML layout
     * @param defStyleAttr The default style attribute to apply
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CometChatFlagMessage, defStyleAttr, 0);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometChatFlagMessageStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatFlagMessage, defStyleAttr, styleResId);
        try {
            loadAttributesFromTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Apply style from style resource
     *
     * @param styleResId Style resource ID
     */
    public void setFlagMessageStyle(@StyleRes int styleResId) {
        if (styleResId == -1) return;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatFlagMessage);
        try {
            loadAttributesFromTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    private void loadAttributesFromTypedArray(TypedArray typedArray) {
        // Dialog styling
        backgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
        titleColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageTitleColor, CometChatTheme.getTextColorPrimary(getContext()));
        titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometchatFlagMessageTitleAppearance, 0);
        subtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometchatFlagMessageSubtitleTextAppearance, 0);
        subtitleTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageSubtitleColor, CometChatTheme.getTextColorSecondary(getContext()));
        closeIconColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageCloseIconColor, CometChatTheme.getIconTintPrimary(getContext()));
        Drawable closeIcon = typedArray.getDrawable(R.styleable.CometChatFlagMessage_cometchatFlagMessageCloseIcon);
        if (closeIcon != null) {
            setCloseIcon(closeIcon);
        }
        strokeColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageStrokeColor, CometChatTheme.getStrokeColorDefault(getContext()));
        borderRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageBorderRadius, 0);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageStrokeWidth, 0);

        // Chip styling
        chipCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipCornerRadius, 0);
        chipStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipStrokeWidth, 0);
        chipTextAppearance = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipTextAppearance, 0);

        // Chip states
        chipActiveBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveBackgroundColor, CometChatTheme.getExtendedPrimaryColor100(getContext()));
        chipInactiveBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
        chipActiveTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveTextColor, CometChatTheme.getTextColorHighlight(getContext()));
        chipInactiveTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveTextColor, CometChatTheme.getTextColorPrimary(getContext()));
        chipActiveBorderColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipActiveBorderColor, CometChatTheme.getExtendedPrimaryColor200(getContext()));
        chipInactiveBorderColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageChipInactiveBorderColor, CometChatTheme.getStrokeColorDefault(getContext()));

        // Remark field
        remarkFieldTitleTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
        remarkFieldHintTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldHintTextColor, CometChatTheme.getTextColorTertiary(getContext()));
        remarkFieldTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTextColor, CometChatTheme.getTextColorPrimary(getContext()));
        remarkFieldBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldBackgroundColor, CometChatTheme.getBackgroundColor2(getContext()));
        remarkFieldTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTitleTextAppearance, 0);
        remarkFieldTextAppearance = typedArray.getResourceId(R.styleable.CometChatFlagMessage_cometchatFlagMessageRemarkFieldTextAppearance, 0);

        // Button styling - General
        buttonStrokeColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonStrokeColor, CometChatTheme.getStrokeColorDark(getContext()));
        buttonCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonCornerRadius, 0);
        buttonStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatFlagMessage_cometchatFlagMessageButtonStrokeWidth, 0);

        // Report Button - Enabled/Disabled
        reportButtonEnabledBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonEnabledBackgroundColor, CometChatTheme.getPrimaryButtonBackgroundColor(getContext()));
        reportButtonDisabledBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonDisabledBackgroundColor, CometChatTheme.getBackgroundColor4(getContext()));
        reportButtonEnabledTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonEnabledTextColor, CometChatTheme.getColorWhite(getContext()));
        reportButtonDisabledTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageReportButtonDisabledTextColor, CometChatTheme.getColorWhite(getContext()));

        // Cancel Button - Enabled/Disabled
        cancelButtonEnabledBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonEnabledBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
        cancelButtonDisabledBackgroundColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonDisabledBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
        cancelButtonEnabledTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonEnabledTextColor, CometChatTheme.getTextColorPrimary(getContext()));
        cancelButtonDisabledTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageCancelButtonDisabledTextColor, CometChatTheme.getTextColorPrimary(getContext()));

        // Error styling
        errorTextColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageErrorTextColor, CometChatTheme.getErrorColor(getContext()));

        // Progress indicator
        progressIndicatorColor = typedArray.getColor(R.styleable.CometChatFlagMessage_cometchatFlagMessageProgressIndicatorColor, Color.BLUE);
        applyDefault();
    }

    private void setCloseIcon(Drawable closeIcon) {
        binding.ivClose.setImageDrawable(closeIcon);
    }

    private void applyDefault() {
        setFlagMessageBackgroundColor(backgroundColor);
        setFlagMessageTitleColor(titleColor);
        setFlagMessageSubtitleColor(subtitleTextColor);
        setFlagMessageTitleTextAppearance(titleTextAppearance);
        setFlagMessageSubtitleTextAppearance(subtitleTextAppearance);
        setFlagMessageCloseIconColor(closeIconColor);
        setFlagMessageStrokeColor(strokeColor);
        setFlagMessageBorderRadius(borderRadius);
        setFlagMessageStrokeWidth(strokeWidth);

        setFlagMessageChipCornerRadius(chipCornerRadius);
        setFlagMessageChipStrokeWidth(chipStrokeWidth);
        setChipTextAppearance(chipTextAppearance);

        setFlagMessageChipActiveBackgroundColor(chipActiveBackgroundColor);
        setFlagMessageChipInactiveBackgroundColor(chipInactiveBackgroundColor);
        setFlagMessageChipActiveTextColor(chipActiveTextColor);
        setFlagMessageChipInactiveTextColor(chipInactiveTextColor);
        setFlagMessageChipActiveBorderColor(chipActiveBorderColor);
        setFlagMessageChipInactiveBorderColor(chipInactiveBorderColor);

        setFlagMessageRemarkFieldTitleTextColor(remarkFieldTitleTextColor);
        setFlagMessageRemarkFieldTitleTextAppearance(remarkFieldTitleTextAppearance);
        setFlagMessageRemarkFieldHintTextColor(remarkFieldHintTextColor);
        setFlagMessageRemarkFieldTextColor(remarkFieldTextColor);
        setFlagMessageRemarkFieldBackgroundColor(remarkFieldBackgroundColor);
        setFlagMessageRemarkFieldTextAppearance(remarkFieldTextAppearance);

        // Apply button background colors FIRST
        setFlagMessageReportButtonEnabledBackgroundColor(reportButtonEnabledBackgroundColor);
        setFlagMessageReportButtonDisabledBackgroundColor(reportButtonDisabledBackgroundColor);
        setFlagMessageReportButtonEnabledTextColor(reportButtonEnabledTextColor);
        setFlagMessageReportButtonDisabledTextColor(reportButtonDisabledTextColor);
        setFlagMessageCancelButtonEnabledBackgroundColor(cancelButtonEnabledBackgroundColor);
        setFlagMessageCancelButtonDisabledBackgroundColor(cancelButtonDisabledBackgroundColor);
        setFlagMessageCancelButtonEnabledTextColor(cancelButtonEnabledTextColor);
        setFlagMessageCancelButtonDisabledTextColor(cancelButtonDisabledTextColor);

        // Apply general button styling LAST so it doesn't get overwritten
        setFlagMessageButtonCornerRadius(buttonCornerRadius);
        setFlagMessageButtonStrokeColor(buttonStrokeColor);
        setFlagMessageButtonStrokeWidth(buttonStrokeWidth);

        setFlagMessageErrorTextColor(errorTextColor);
        setFlagMessageProgressIndicatorColor(progressIndicatorColor);
    }

    private void setFlagMessageSubtitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.subtitleTextAppearance = titleTextAppearance;
        if (titleTextAppearance != -1) {
            binding.tvDescription.setTextAppearance(titleTextAppearance);
        }
    }

    private void setFlagMessageSubtitleColor(@ColorInt int titleColor) {
        this.subtitleTextColor = titleColor;
        binding.tvDescription.setTextColor(titleColor);
    }

    private void setChipTextAppearance(@StyleRes int chipTextAppearance) {
        this.chipTextAppearance = chipTextAppearance;
    }

    private void setFlagMessageTitleTextAppearance(int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        if (titleTextAppearance != -1) {
            binding.tvTitle.setTextAppearance(titleTextAppearance);
        }
    }

    public @StyleRes int getFlagMessageTitleTextAppearance() {
        return titleTextAppearance;
    }

    private void setFlagMessageRemarkFieldTitleTextAppearance(int remarkFieldHintTextColor) {
        this.remarkFieldTitleTextAppearance = remarkFieldHintTextColor;
        if (remarkFieldHintTextColor != -1) {
            binding.tvReasonLabel.setTextAppearance(remarkFieldHintTextColor);
        }
    }

    private void setFlagMessageRemarkFieldTitleTextColor(int remarkFieldHintTextColor) {
        this.remarkFieldTitleTextColor = remarkFieldHintTextColor;
        binding.tvReasonLabel.setTextColor(remarkFieldHintTextColor);
    }

    private void setupListeners() {
        binding.ivClose.setOnClickListener(v -> {
            if (onCancelClickListener != null) {
                onCancelClickListener.onClick();
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (onCancelClickListener != null) {
                onCancelClickListener.onClick();
            }
        });

        binding.btnReport.setOnClickListener(v -> {
            if (isReportButtonEnabled && onReportClickListener != null) {
                FlagReason selectedReason = selectedChipPosition >= 0 ? flagReasons.get(selectedChipPosition) : null;

                FlagDetail flagDetail = new FlagDetail();
                if (selectedReason != null) {
                    flagDetail.setReasonId(selectedReason.getId());
                    flagDetail.setRemark(getRemarkText());
                }

                onReportClickListener.onReportClick(flagDetail);
            }
        });
    }

    private MaterialCardView createChipView(FlagReason flagReason, int position) {
        MaterialCardView chipCard = new MaterialCardView(getContext());
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginPx = Utils.convertDpToPx(getContext(), 6);
        params.setMargins(0, 0, marginPx, marginPx);
        chipCard.setLayoutParams(params);
        chipCard.setCardElevation(0);

        TextView chipText = new TextView(getContext());
        chipText.setText(getLocalizedFlagReasonName(flagReason));
        chipText.setTextColor(chipInactiveTextColor);
        chipText.setTextAppearance(chipTextAppearance);
        chipText.setPadding(
                Utils.convertDpToPx(getContext(), 12),
                Utils.convertDpToPx(getContext(), 4),
                Utils.convertDpToPx(getContext(), 12),
                Utils.convertDpToPx(getContext(), 4)
        );

        chipCard.addView(chipText);
        chipCard.setRippleColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        updateChipStyle(chipCard, chipText, false);

        chipCard.setOnClickListener(v -> {
            if (selectedChipPosition >= 0 && selectedChipPosition < binding.flexboxChips.getChildCount()) {
                MaterialCardView prevChip = (MaterialCardView) binding.flexboxChips.getChildAt(selectedChipPosition);
                TextView prevText = (TextView) prevChip.getChildAt(0);
                updateChipStyle(prevChip, prevText, false);
            }
            selectedChipPosition = position;
            updateChipStyle(chipCard, chipText, true);
            updateReportButtonState(true);
        });

        return chipCard;
    }

    /**
     * Get localized flag reason name based on ID
     *
     * @param flagReason The FlagReason object.
     * @return Localized string.
     */
    private String getLocalizedFlagReasonName(FlagReason flagReason) {
        if (flagReason == null || flagReason.getId() == null) {
            return "";
        }

        String reasonId = flagReason.getId().toLowerCase();
        Integer resourceId = flagReasonMap.get(reasonId);

        if (resourceId != null) {
            try {
                return getContext().getString(resourceId);
            } catch (Exception e) {
                return flagReason.getName() != null ? flagReason.getName() : "";
            }
        } else {
            return flagReason.getName() != null ? flagReason.getName() : "";
        }
    }

    /**
     * Sets a custom mapping of flag reason IDs to localization string resource IDs.
     * @param localizationIdMap A map where keys are flag reason IDs and values are string resource IDs.
     */
    public void setLocalizationIdMap(Map<String, Integer> localizationIdMap) {
        flagReasonMap.putAll(localizationIdMap);
    }

    private void updateChipStyle(MaterialCardView chipCard, TextView chipText, boolean isActive) {
        if (isActive) {
            chipCard.setCardBackgroundColor(chipActiveBackgroundColor);
            chipCard.setStrokeColor(chipActiveBorderColor);
            chipText.setTextColor(chipActiveTextColor);
        } else {
            chipCard.setCardBackgroundColor(chipInactiveBackgroundColor);
            chipCard.setStrokeColor(chipInactiveBorderColor);
            chipText.setTextColor(chipInactiveTextColor);
        }
        chipCard.setStrokeWidth(chipStrokeWidth);
        chipCard.setRadius(chipCornerRadius);
    }

    private void updateReportButtonState(boolean enabled) {
        isReportButtonEnabled = enabled;
        binding.btnReport.setEnabled(enabled);

        if (enabled) {
            binding.btnReport.setCardBackgroundColor(reportButtonEnabledBackgroundColor);
            binding.tvReport.setTextColor(reportButtonEnabledTextColor);
            binding.btnReport.setAlpha(1.0f);
        } else {
            binding.btnReport.setCardBackgroundColor(reportButtonDisabledBackgroundColor);
            binding.tvReport.setTextColor(reportButtonDisabledTextColor);
            binding.btnReport.setAlpha(0.6f);
        }

        binding.btnReport.setStrokeColor(buttonStrokeColor);
        binding.btnReport.setStrokeWidth(buttonStrokeWidth);
        binding.btnReport.setRadius(buttonCornerRadius);
    }

    private void refreshChips() {
        for (int i = 0; i < binding.flexboxChips.getChildCount(); i++) {
            MaterialCardView chip = (MaterialCardView) binding.flexboxChips.getChildAt(i);
            TextView text = (TextView) chip.getChildAt(0);
            updateChipStyle(chip, text, i == selectedChipPosition);
        }
    }

    /**
     * Sets the list of flag reasons that users can select from.
     * This method clears any existing selection and recreates the chip UI elements.
     *
     * @param flagReasons List of FlagReason objects to display as selectable chips.
     *                    If null, an empty list will be used.
     */
    public void setFlagReasons(List<FlagReason> flagReasons) {
        this.flagReasons = flagReasons != null ? flagReasons : new ArrayList<>();
        binding.flexboxChips.removeAllViews();
        selectedChipPosition = -1;
        updateReportButtonState(false);

        for (int i = 0; i < this.flagReasons.size(); i++) {
            MaterialCardView chipView = createChipView(this.flagReasons.get(i), i);
            binding.flexboxChips.addView(chipView);
        }
    }

    /**
     * Gets the current list of flag reasons.
     *
     * @return The list of FlagReason objects currently available for selection
     */
    public List<FlagReason> getFlagReasons() {
        return flagReasons;
    }

    /**
     * Gets the currently selected flag reason.
     *
     * @return The selected FlagReason object, or null if none is selected
     */
    public FlagReason getSelectedFlagReason() {
        return selectedChipPosition >= 0 && selectedChipPosition < flagReasons.size()
                ? flagReasons.get(selectedChipPosition)
                : null;
    }

    /**
     * Clears the current chip selection and disables the report button.
     * This resets the UI to its initial state with no reason selected.
     */
    public void clearSelection() {
        if (selectedChipPosition >= 0 && selectedChipPosition < binding.flexboxChips.getChildCount()) {
            MaterialCardView chip = (MaterialCardView) binding.flexboxChips.getChildAt(selectedChipPosition);
            TextView text = (TextView) chip.getChildAt(0);
            updateChipStyle(chip, text, false);
        }
        selectedChipPosition = -1;
        updateReportButtonState(false);
    }

    /**
     * Sets the title text displayed at the top of the flag message dialog.
     *
     * @param title The title text to display
     */
    public void setTitle(String title) {
        binding.tvTitle.setText(title);
    }

    /**
     * Gets the current title text.
     *
     * @return The title text currently displayed
     */
    public String getTitle() {
        return binding.tvTitle.getText().toString();
    }

    /**
     * Sets the description text displayed below the title.
     *
     * @param description The description text to display
     */
    public void setDescription(String description) {
        binding.tvDescription.setText(description);
    }

    /**
     * Gets the current description text.
     *
     * @return The description text currently displayed
     */
    public String getDescription() {
        return binding.tvDescription.getText().toString();
    }

    /**
     * Sets the hint text for the remark input field.
     *
     * @param hint The hint text to display in the remark field
     */
    public void setRemarkHint(String hint) {
        binding.etRemark.setHint(hint);
    }

    /**
     * Gets the current text entered in the remark field.
     *
     * @return The remark text entered by the user, or empty string if none
     */
    public String getRemarkText() {
        return binding.etRemark.getText() != null ? binding.etRemark.getText().toString() : "";
    }

    /**
     * Sets the text for the cancel button.
     *
     * @param text The text to display on the cancel button
     */
    public void setCancelButtonText(String text) {
        binding.tvCancel.setText(text);
    }

    /**
     * Gets the current cancel button text.
     *
     * @return The text currently displayed on the cancel button
     */
    public String getCancelButtonText() {
        return binding.tvCancel.getText().toString();
    }

    /**
     * Sets the text for the report button.
     *
     * @param text The text to display on the report button
     */
    public void setReportButtonText(String text) {
        binding.tvReport.setText(text);
    }

    /**
     * Gets the current report button text.
     *
     * @return The text currently displayed on the report button
     */
    public String getReportButtonText() {
        return binding.tvReport.getText().toString();
    }

    /**
     * Sets the background color of the flag message dialog.
     *
     * @param color The background color to apply
     */
    public void setFlagMessageBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        binding.rootCard.setCardBackgroundColor(color);
    }

    /**
     * Gets the current background color of the flag message dialog.
     *
     * @return The current background color
     */
    public @ColorInt int getFlagMessageBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the border radius of the flag message dialog.
     *
     * @param radius The border radius in pixels
     */
    public void setFlagMessageBorderRadius(@Dimension int radius) {
        this.borderRadius = radius;
        setRadius(radius);
    }

    /**
     * Gets the current border radius of the flag message dialog.
     *
     * @return The current border radius in pixels
     */
    public @Dimension int getFlagMessageBorderRadius() {
        return borderRadius;
    }

    /**
     * Sets the stroke color of the flag message dialog border.
     *
     * @param color The stroke color to apply
     */
    public void setFlagMessageStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        setStrokeColor(color);
    }

    /**
     * Gets the current stroke color of the flag message dialog border.
     *
     * @return The current stroke color
     */
    public @ColorInt int getFlagMessageStrokeColor() {
        return strokeColor;
    }

    /**
     * Sets the stroke width of the flag message dialog border.
     *
     * @param width The stroke width in pixels
     */
    public void setFlagMessageStrokeWidth(@Dimension int width) {
        this.strokeWidth = width;
        setStrokeWidth((int) width);
    }

    /**
     * Gets the current stroke width of the flag message dialog border.
     *
     * @return The current stroke width in pixels
     */
    public @Dimension int getFlagMessageStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the text color of the flag message dialog title.
     *
     * @param color The color to apply to the title text
     */
    public void setFlagMessageTitleColor(@ColorInt int color) {
        this.titleColor = color;
        binding.tvTitle.setTextColor(color);
    }

    /**
     * Gets the current title text color.
     *
     * @return The current title text color
     */
    public @ColorInt int getFlagMessageTitleColor() {
        return titleColor;
    }

    /**
     * Sets the color of the close icon.
     *
     * @param color The color to apply to the close icon
     */
    public void setFlagMessageCloseIconColor(@ColorInt int color) {
        this.closeIconColor = color;
        binding.ivClose.setColorFilter(color);
    }

    /**
     * Gets the current close icon color.
     *
     * @return The current close icon color
     */
    public @ColorInt int getFlagMessageCloseIconColor() {
        return closeIconColor;
    }

    /**
     * Sets the corner radius for flag reason chips.
     *
     * @param radius The corner radius in pixels
     */
    public void setFlagMessageChipCornerRadius(@Dimension int radius) {
        this.chipCornerRadius = radius;
    }

    /**
     * Gets the current chip corner radius.
     *
     * @return The current chip corner radius in pixels
     */
    public @Dimension int getFlagMessageChipCornerRadius() {
        return chipCornerRadius;
    }

    /**
     * Sets the stroke width for flag reason chips.
     *
     * @param width The stroke width in pixels
     */
    public void setFlagMessageChipStrokeWidth(@Dimension int width) {
        this.chipStrokeWidth = width;
    }

    /**
     * Gets the current chip stroke width.
     *
     * @return The current chip stroke width in pixels
     */
    public @Dimension int getFlagMessageChipStrokeWidth() {
        return chipStrokeWidth;
    }

    /**
     * Sets the background color for active/selected chips.
     *
     * @param color The background color to apply to active chips
     */
    public void setFlagMessageChipActiveBackgroundColor(@ColorInt int color) {
        this.chipActiveBackgroundColor = color;
    }

    /**
     * Gets the current active chip background color.
     *
     * @return The current active chip background color
     */
    public @ColorInt int getFlagMessageChipActiveBackgroundColor() {
        return chipActiveBackgroundColor;
    }

    /**
     * Sets the text color for active/selected chips.
     *
     * @param color The text color to apply to active chips
     */
    public void setFlagMessageChipActiveTextColor(@ColorInt int color) {
        this.chipActiveTextColor = color;
    }

    /**
     * Gets the current active chip text color.
     *
     * @return The current active chip text color
     */
    public @ColorInt int getFlagMessageChipActiveTextColor() {
        return chipActiveTextColor;
    }

    /**
     * Sets the border color for active/selected chips.
     *
     * @param color The border color to apply to active chips
     */
    public void setFlagMessageChipActiveBorderColor(@ColorInt int color) {
        this.chipActiveBorderColor = color;
    }

    /**
     * Gets the current active chip border color.
     *
     * @return The current active chip border color
     */
    public @ColorInt int getFlagMessageChipActiveBorderColor() {
        return chipActiveBorderColor;
    }

    /**
     * Sets the background color for inactive/unselected chips.
     *
     * @param color The background color to apply to inactive chips
     */
    public void setFlagMessageChipInactiveBackgroundColor(@ColorInt int color) {
        this.chipInactiveBackgroundColor = color;
    }

    /**
     * Gets the current inactive chip background color.
     *
     * @return The current inactive chip background color
     */
    public @ColorInt int getFlagMessageChipInactiveBackgroundColor() {
        return chipInactiveBackgroundColor;
    }

    /**
     * Sets the text color for inactive/unselected chips.
     *
     * @param color The text color to apply to inactive chips
     */
    public void setFlagMessageChipInactiveTextColor(@ColorInt int color) {
        this.chipInactiveTextColor = color;
    }

    /**
     * Gets the current inactive chip text color.
     *
     * @return The current inactive chip text color
     */
    public @ColorInt int getFlagMessageChipInactiveTextColor() {
        return chipInactiveTextColor;
    }

    /**
     * Sets the border color for inactive/unselected chips.
     *
     * @param color The border color to apply to inactive chips
     */
    public void setFlagMessageChipInactiveBorderColor(@ColorInt int color) {
        this.chipInactiveBorderColor = color;
    }

    /**
     * Gets the current inactive chip border color.
     *
     * @return The current inactive chip border color
     */
    public @ColorInt int getFlagMessageChipInactiveBorderColor() {
        return chipInactiveBorderColor;
    }

    /**
     * Sets the hint text color for the remark input field.
     *
     * @param color The color to apply to the hint text
     */
    public void setFlagMessageRemarkFieldHintTextColor(@ColorInt int color) {
        this.remarkFieldHintTextColor = color;
        binding.etRemark.setHintTextColor(color);
    }

    /**
     * Gets the current remark field hint text color.
     *
     * @return The current remark field hint text color
     */
    public @ColorInt int getFlagMessageRemarkFieldHintTextColor() {
        return remarkFieldHintTextColor;
    }

    /**
     * Sets the text color for the remark input field.
     *
     * @param color The color to apply to the input text
     */
    public void setFlagMessageRemarkFieldTextColor(@ColorInt int color) {
        this.remarkFieldTextColor = color;
        binding.etRemark.setTextColor(color);
    }

    /**
     * Gets the current remark field text color.
     *
     * @return The current remark field text color
     */
    public @ColorInt int getFlagMessageRemarkFieldTextColor() {
        return remarkFieldTextColor;
    }

    /**
     * Sets the background color for the remark input field container.
     *
     * @param color The background color to apply to the remark field
     */
    public void setFlagMessageRemarkFieldBackgroundColor(@ColorInt int color) {
        this.remarkFieldBackgroundColor = color;
        binding.tilLayout.setCardBackgroundColor(color);
    }

    /**
     * Gets the current remark field background color.
     *
     * @return The current remark field background color
     */
    public @ColorInt int getFlagMessageRemarkFieldBackgroundColor() {
        return remarkFieldBackgroundColor;
    }

    /**
     * Sets the text appearance for the remark input field.
     *
     * @param textAppearance The text appearance style resource to apply
     */
    public void setFlagMessageRemarkFieldTextAppearance(@StyleRes int textAppearance) {
        this.remarkFieldTextAppearance = textAppearance;
        if (textAppearance != -1) {
            binding.etRemark.setTextAppearance(textAppearance);
        }
    }

    /**
     * Gets the current remark field text appearance.
     *
     * @return The current remark field text appearance style resource
     */
    public @StyleRes int getFlagMessageRemarkFieldTextAppearance() {
        return remarkFieldTextAppearance;
    }

    /**
     * Sets the corner radius for both report and cancel buttons.
     *
     * @param radius The corner radius in pixels to apply to both buttons
     */
    public void setFlagMessageButtonCornerRadius(@Dimension int radius) {
        this.buttonCornerRadius = radius;
        binding.btnCancel.setRadius(buttonCornerRadius);
    }

    /**
     * Gets the current button corner radius.
     *
     * @return The current button corner radius in pixels
     */
    public @Dimension int getFlagMessageButtonCornerRadius() {
        return buttonCornerRadius;
    }

    /**
     * Sets the stroke color for both report and cancel buttons.
     *
     * @param color The stroke color to apply to both buttons
     */
    public void setFlagMessageButtonStrokeColor(@ColorInt int color) {
        this.buttonStrokeColor = color;
        binding.btnCancel.setStrokeColor(buttonStrokeColor);
    }

    /**
     * Gets the current button stroke color.
     *
     * @return The current button stroke color
     */
    public @ColorInt int getFlagMessageButtonStrokeColor() {
        return buttonStrokeColor;
    }

    /**
     * Sets the stroke width for both report and cancel buttons.
     *
     * @param width The stroke width in pixels to apply to both buttons
     */
    public void setFlagMessageButtonStrokeWidth(@Dimension int width) {
        this.buttonStrokeWidth = width;
        binding.btnCancel.setStrokeWidth(buttonStrokeWidth);
    }

    /**
     * Gets the current button stroke width.
     *
     * @return The current button stroke width in pixels
     */
    public @Dimension int getFlagMessageButtonStrokeWidth() {
        return buttonStrokeWidth;
    }

    public void setFlagMessageReportButtonEnabledBackgroundColor(@ColorInt int color) {
        this.reportButtonEnabledBackgroundColor = color;
    }

    /**
     * Gets the current enabled report button background color.
     *
     * @return The current enabled report button background color
     */
    public @ColorInt int getFlagMessageReportButtonEnabledBackgroundColor() {
        return reportButtonEnabledBackgroundColor;
    }

    /**
     * Sets the text color for the report button when enabled.
     *
     * @param color The text color to apply when report button is enabled
     */
    public void setFlagMessageReportButtonEnabledTextColor(@ColorInt int color) {
        this.reportButtonEnabledTextColor = color;
        binding.tvReport.setTextColor(color);
    }

    /**
     * Gets the current enabled report button text color.
     *
     * @return The current enabled report button text color
     */
    public @ColorInt int getFlagMessageReportButtonEnabledTextColor() {
        return reportButtonEnabledTextColor;
    }

    /**
     * Sets the background color for the report button when disabled.
     * Applies immediately if the button is currently disabled.
     *
     * @param color The background color to apply when report button is disabled
     */
    public void setFlagMessageReportButtonDisabledBackgroundColor(@ColorInt int color) {
        this.reportButtonDisabledBackgroundColor = color;
    }

    /**
     * Gets the current disabled report button background color.
     *
     * @return The current disabled report button background color
     */
    public @ColorInt int getFlagMessageReportButtonDisabledBackgroundColor() {
        return reportButtonDisabledBackgroundColor;
    }

    /**
     * Sets the text color for the report button when disabled.
     * Applies immediately if the button is currently disabled.
     *
     * @param color The text color to apply when report button is disabled
     */
    public void setFlagMessageReportButtonDisabledTextColor(@ColorInt int color) {
        this.reportButtonDisabledTextColor = color;
    }

    /**
     * Gets the current disabled report button text color.
     *
     * @return The current disabled report button text color
     */
    public @ColorInt int getFlagMessageReportButtonDisabledTextColor() {
        return reportButtonDisabledTextColor;
    }

    /**
     * Sets the background color for the cancel button when enabled.
     * Applies immediately if the button is currently enabled.
     *
     * @param color The background color to apply when cancel button is enabled
     */
    public void setFlagMessageCancelButtonEnabledBackgroundColor(@ColorInt int color) {
        this.cancelButtonEnabledBackgroundColor = color;
        binding.btnCancel.setCardBackgroundColor(cancelButtonEnabledBackgroundColor);
    }

    /**
     * Gets the current enabled cancel button background color.
     *
     * @return The current enabled cancel button background color
     */
    public @ColorInt int getFlagMessageCancelButtonEnabledBackgroundColor() {
        return cancelButtonEnabledBackgroundColor;
    }

    /**
     * Sets the text color for the cancel button when enabled.
     * Applies immediately if the button is currently enabled.
     *
     * @param color The text color to apply when cancel button is enabled
     */
    public void setFlagMessageCancelButtonEnabledTextColor(@ColorInt int color) {
        this.cancelButtonEnabledTextColor = color;
        binding.tvCancel.setTextColor(cancelButtonEnabledTextColor);
    }

    /**
     * Gets the current enabled cancel button text color.
     *
     * @return The current enabled cancel button text color
     */
    public @ColorInt int getFlagMessageCancelButtonEnabledTextColor() {
        return cancelButtonEnabledTextColor;
    }

    /**
     * Sets the background color for the cancel button when disabled.
     * Applies immediately if the button is currently disabled.
     *
     * @param color The background color to apply when cancel button is disabled
     */
    public void setFlagMessageCancelButtonDisabledBackgroundColor(@ColorInt int color) {
        this.cancelButtonDisabledBackgroundColor = color;
        binding.btnCancel.setCardBackgroundColor(cancelButtonDisabledBackgroundColor);
    }

    /**
     * Gets the current disabled cancel button background color.
     *
     * @return The current disabled cancel button background color
     */
    public @ColorInt int getFlagMessageCancelButtonDisabledBackgroundColor() {
        return cancelButtonDisabledBackgroundColor;
    }

    /**
     * Sets the text color for the cancel button when disabled.
     * Applies immediately if the button is currently disabled.
     *
     * @param color The text color to apply when cancel button is disabled
     */
    public void setFlagMessageCancelButtonDisabledTextColor(@ColorInt int color) {
        this.cancelButtonDisabledTextColor = color;
        binding.tvCancel.setTextColor(cancelButtonDisabledTextColor);
    }

    /**
     * Gets the current disabled cancel button text color.
     *
     * @return The current disabled cancel button text color
     */
    public @ColorInt int getFlagMessageCancelButtonDisabledTextColor() {
        return cancelButtonDisabledTextColor;
    }

    /**
     * Sets the text color for the error message displayed during flag operation failures.
     *
     * @param color The color to apply to the error message text
     */
    public void setFlagMessageErrorTextColor(@ColorInt int color) {
        this.errorTextColor = color;
        binding.tvErrorMessage.setTextColor(color);
    }

    /**
     * Gets the current error message text color.
     *
     * @return The current error message text color
     */
    public @ColorInt int getFlagMessageErrorTextColor() {
        return errorTextColor;
    }

    /**
     * Sets the color of the progress indicator shown on the report button.
     *
     * @param color The color to apply to the progress indicator
     */
    public void setFlagMessageProgressIndicatorColor(@ColorInt int color) {
        this.progressIndicatorColor = color;
    }

    /**
     * Gets the current progress indicator color for the report button.
     *
     * @return The current progress indicator color
     */
    public @ColorInt int getFlagMessageProgressIndicatorColor() {
        return progressIndicatorColor;
    }

    /**
     * Controls the visibility of the progress bar on the report button.
     * When showing progress, hides the button text and shows progress indicator.
     * When hiding progress, shows the button text and hides progress indicator.
     *
     * @param visible true to hide progress bar and show button text,
     *                false to show progress bar and hide button text
     */
    public void hidePositiveButtonProgressBar(boolean visible) {
        if (visible) {
            binding.progressBarPositiveButton.setVisibility(GONE);
            binding.tvReport.setVisibility(VISIBLE);
        } else {
            binding.progressBarPositiveButton.setVisibility(VISIBLE);
            binding.tvReport.setVisibility(GONE);
        }
    }

    /**
     * Shows the error message view to indicate that an error occurred during flag operation.
     * This makes the error message visible to the user.
     */
    public void onFlagMessageError() {
        binding.tvErrorMessage.setVisibility(VISIBLE);
    }

    public void setFlagRemarkInputFieldVisibility(int flagRemarkInputFieldVisibility) {
        binding.tvReasonLabel.setVisibility(flagRemarkInputFieldVisibility);
        binding.tvReasonOptional.setVisibility(flagRemarkInputFieldVisibility);
        binding.tilLayout.setVisibility(flagRemarkInputFieldVisibility);
    }

    /**
     * Interface for handling report button click events.
     * Provides callback when user clicks the report button with selected flag details.
     */
    public interface OnReportClickListener {
        /**
         * Called when the report button is clicked.
         *
         * @param flagDetail The flag detail containing selected reason ID and remark text
         */
        void onReportClick(FlagDetail flagDetail);
    }

    /**
     * Sets the listener for cancel button clicks.
     * This listener is also triggered by the close icon click.
     *
     * @param listener The callback to be invoked when cancel button is clicked
     */
    public void setOnCancelClickListener(OnClick listener) {
        this.onCancelClickListener = listener;
    }

    /**
     * Sets the listener for report button clicks.
     * The listener receives a FlagDetail object with the selected reason and remark.
     *
     * @param clickListener The callback to be invoked when report button is clicked
     */
    public void setOnReportClickListener(OnReportClickListener clickListener) {
        this.onReportClickListener = clickListener;
    }

    /**
     * Gets the current close button click listener.
     *
     * @return The current close button click listener, or null if none set
     */
    public OnClick getOnCloseClickListener() {
        return onCloseClickListener;
    }

    /**
     * Sets the listener for close button clicks.
     * This is separate from the cancel button listener and specifically handles the close icon.
     *
     * @param listener The callback to be invoked when close button is clicked
     */
    public void setOnCloseButtonClickListener(OnClick listener) {
        this.onCloseClickListener = listener;
    }
}