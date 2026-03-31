package com.cometchat.chatuikit.shared.views.richtexttoolbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatRichTextToolbarBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * CometChatRichTextToolbar is a custom view that provides a toolbar for rich text formatting.
 * <p>
 * The toolbar includes buttons for:
 * <ul>
 *     <li>Bold</li>
 *     <li>Italic</li>
 *     <li>Strikethrough</li>
 *     <li>Inline Code</li>
 *     <li>Code Block</li>
 *     <li>Link</li>
 *     <li>Bullet List</li>
 *     <li>Numbered List</li>
 *     <li>Blockquote</li>
 * </ul>
 * </p>
 * <p>
 * The toolbar supports active state highlighting for format buttons and provides
 * callbacks when format buttons are clicked.
 * </p>
 *
 * @see FormatType
 * @see RichTextFormatterManager
 */
public class CometChatRichTextToolbar extends MaterialCardView {

    private CometchatRichTextToolbarBinding binding;
    private Set<FormatType> activeFormats = EnumSet.noneOf(FormatType.class);
    private OnFormatClickListener onFormatClickListener;

    // Style properties
    private @ColorInt int backgroundColor;
    private @ColorInt int iconTint;
    private @ColorInt int activeIconTint;
    private @ColorInt int activeIconBackgroundColor;
    private @ColorInt int separatorColor;
    private @ColorInt int disabledIconTint;

    /**
     * Compatibility rules for format types.
     * Maps each format type to the set of formats it disables when active.
     */
    private static final Map<FormatType, Set<FormatType>> INCOMPATIBLE_FORMATS;

    /**
     * Formats that should be auto-deselected when a given format is activated.
     * Unlike INCOMPATIBLE_FORMATS (which grays out buttons), these cause the
     * conflicting format to be removed from activeFormats when the key format is toggled on.
     */
    private static final Map<FormatType, Set<FormatType>> AUTO_DESELECT_FORMATS;

    static {
        Map<FormatType, Set<FormatType>> map = new EnumMap<>(FormatType.class);

        // Bold, Italic, Underline, Strike, Inline Code, Link - compatible with everything
        // (Code block remains selectable; inline formatting simply won't render inside code blocks)
        Set<FormatType> disablesNothing = EnumSet.noneOf(FormatType.class);
        map.put(FormatType.BOLD, disablesNothing);
        map.put(FormatType.ITALIC, disablesNothing);
        map.put(FormatType.UNDERLINE, disablesNothing);
        map.put(FormatType.STRIKETHROUGH, disablesNothing);
        map.put(FormatType.INLINE_CODE, disablesNothing);
        map.put(FormatType.LINK, disablesNothing);

        // Blockquote - compatible with everything, disables nothing
        map.put(FormatType.BLOCKQUOTE, EnumSet.noneOf(FormatType.class));

        // Bullet List - compatible with everything except Code Block (handled via auto-deselect)
        map.put(FormatType.BULLET_LIST, EnumSet.noneOf(FormatType.class));

        // Numbered List - compatible with everything except Code Block (handled via auto-deselect)
        map.put(FormatType.ORDERED_LIST, EnumSet.noneOf(FormatType.class));

        // Code Block - disables inline formatting and link; compatible with lists and blockquote
        map.put(FormatType.CODE_BLOCK, EnumSet.of(
                FormatType.BOLD,
                FormatType.ITALIC,
                FormatType.UNDERLINE,
                FormatType.STRIKETHROUGH,
                FormatType.INLINE_CODE,
                FormatType.LINK
        ));

        INCOMPATIBLE_FORMATS = Collections.unmodifiableMap(map);

        // Auto-deselect rules: selecting a list deselects code block, and vice versa
        Map<FormatType, Set<FormatType>> deselectMap = new EnumMap<>(FormatType.class);
        deselectMap.put(FormatType.BULLET_LIST, EnumSet.of(FormatType.CODE_BLOCK, FormatType.ORDERED_LIST));
        deselectMap.put(FormatType.ORDERED_LIST, EnumSet.of(FormatType.CODE_BLOCK, FormatType.BULLET_LIST));
        deselectMap.put(FormatType.BLOCKQUOTE, EnumSet.of(FormatType.CODE_BLOCK));
        deselectMap.put(FormatType.CODE_BLOCK, EnumSet.of(
                FormatType.BULLET_LIST, FormatType.ORDERED_LIST, FormatType.BLOCKQUOTE,
                FormatType.BOLD, FormatType.ITALIC, FormatType.UNDERLINE,
                FormatType.STRIKETHROUGH, FormatType.INLINE_CODE, FormatType.LINK
        ));
        AUTO_DESELECT_FORMATS = Collections.unmodifiableMap(deselectMap);
    }

    /**
     * Constructs a new CometChatRichTextToolbar with the given context.
     *
     * @param context The context of the view.
     */
    public CometChatRichTextToolbar(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatRichTextToolbar with the given context and attribute set.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set for the view.
     */
    public CometChatRichTextToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatRichTextToolbarStyle);
    }

    /**
     * Constructs a new CometChatRichTextToolbar with the given context, attribute set,
     * and default style attribute.
     *
     * @param context      The context of the view.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatRichTextToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatRichTextToolbarBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setDefaultValues();
        setupFormatButtons();
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Sets the default values for the toolbar.
     */
    private void setDefaultValues() {
        // Unselected icons are grayed out (secondary tint)
        iconTint = CometChatTheme.getIconTintSecondary(getContext());
        // Selected icons are dark/bold (primary text color for high contrast)
        activeIconTint = CometChatTheme.getTextColorPrimary(getContext());
        // Selected button background is background4
        activeIconBackgroundColor = CometChatTheme.getBackgroundColor4(getContext());
        separatorColor = CometChatTheme.getStrokeColorLight(getContext());
        // Toolbar background is background2
        backgroundColor = CometChatTheme.getBackgroundColor2(getContext());
        // Disabled icons have reduced opacity
        disabledIconTint = CometChatTheme.getIconTintTertiary(getContext());
    }

    /**
     * Sets up click listeners for all format buttons.
     */
    private void setupFormatButtons() {
        binding.btnBold.setOnClickListener(v -> onFormatClick(FormatType.BOLD));
        binding.btnItalic.setOnClickListener(v -> onFormatClick(FormatType.ITALIC));
        binding.btnStrikethrough.setOnClickListener(v -> onFormatClick(FormatType.STRIKETHROUGH));
        binding.btnUnderline.setOnClickListener(v -> onFormatClick(FormatType.UNDERLINE));
        binding.btnInlineCode.setOnClickListener(v -> onFormatClick(FormatType.INLINE_CODE));
        binding.btnCodeBlock.setOnClickListener(v -> onFormatClick(FormatType.CODE_BLOCK));
        binding.btnLink.setOnClickListener(v -> onFormatClick(FormatType.LINK));
        binding.btnBulletList.setOnClickListener(v -> onFormatClick(FormatType.BULLET_LIST));
        binding.btnNumberedList.setOnClickListener(v -> onFormatClick(FormatType.ORDERED_LIST));
        binding.btnBlockquote.setOnClickListener(v -> onFormatClick(FormatType.BLOCKQUOTE));
    }

    /**
     * Handles format button click events.
     * Ignores clicks on disabled formats.
     *
     * @param formatType The format type that was clicked.
     */
    private void onFormatClick(FormatType formatType) {
        // Ignore clicks on disabled formats
        if (isFormatDisabled(formatType)) {
            return;
        }
        if (onFormatClickListener != null) {
            onFormatClickListener.onFormatClick(formatType);
        }
    }

    /**
     * Applies the style attributes from XML.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.CometChatRichTextToolbar, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(
                R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarStyle, 0);
        directAttributes = styleResId != 0 ? getContext()
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.CometChatRichTextToolbar, defStyleAttr, styleResId) : null;
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts the attributes and applies the default values.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) {
            applyDefault();
            return;
        }
        try {
            backgroundColor = typedArray.getColor(
                    R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarBackgroundColor, backgroundColor);
            iconTint = typedArray.getColor(
                    R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarIconTint, iconTint);
            activeIconTint = typedArray.getColor(
                    R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarActiveIconTint, activeIconTint);
            activeIconBackgroundColor = typedArray.getColor(
                    R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarActiveIconBackgroundColor, activeIconBackgroundColor);
            separatorColor = typedArray.getColor(
                    R.styleable.CometChatRichTextToolbar_cometchatRichTextToolbarSeparatorColor, separatorColor);
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the toolbar.
     */
    private void applyDefault() {
        setToolbarBackgroundColor(backgroundColor);
        setSeparatorColor(separatorColor);
        updateButtonStates();
    }

    /**
     * Sets the style for the toolbar.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                    style, R.styleable.CometChatRichTextToolbar);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Sets the active formats and updates the button states.
     *
     * @param activeFormats The set of active FormatType values.
     */
    public void setActiveFormats(@NonNull Set<FormatType> activeFormats) {
        this.activeFormats = activeFormats != null ? activeFormats : EnumSet.noneOf(FormatType.class);
        updateButtonStates();
    }

    /**
     * Gets the currently active formats.
     *
     * @return The set of active FormatType values.
     */
    public Set<FormatType> getActiveFormats() {
        return activeFormats;
    }

    /**
     * Computes the set of formats that should be disabled based on currently active formats.
     *
     * @return The set of FormatType values that should be disabled.
     */
    private Set<FormatType> getDisabledFormats() {
        Set<FormatType> disabled = EnumSet.noneOf(FormatType.class);
        for (FormatType activeFormat : activeFormats) {
            Set<FormatType> incompatible = INCOMPATIBLE_FORMATS.get(activeFormat);
            if (incompatible != null) {
                disabled.addAll(incompatible);
            }
        }
        return disabled;
    }

    /**
     * Checks if a format type is currently disabled based on active formats.
     *
     * @param formatType The format type to check.
     * @return true if the format is disabled, false otherwise.
     */
    public boolean isFormatDisabled(FormatType formatType) {
        return getDisabledFormats().contains(formatType);
    }

    /**
     * Toggles a format in the active formats set.
     * When activating a format, auto-deselects any conflicting formats
     * (e.g., selecting a list deselects code block and vice versa).
     *
     * @param formatType The format type to toggle.
     */
    public void toggleFormat(FormatType formatType) {
        boolean wasActive = activeFormats.contains(formatType);
        if (wasActive) {
            activeFormats.remove(formatType);
        } else {
            // Auto-deselect conflicting formats before activating
            Set<FormatType> toDeselect = AUTO_DESELECT_FORMATS.get(formatType);
            if (toDeselect != null) {
                activeFormats.removeAll(toDeselect);
            }
            // Also deselect any formats that become incompatible (disabled) when this format is active
            Set<FormatType> incompatible = INCOMPATIBLE_FORMATS.get(formatType);
            if (incompatible != null) {
                activeFormats.removeAll(incompatible);
            }
            activeFormats.add(formatType);
        }
        updateButtonStatesWithAnnouncement(formatType, !wasActive);
    }

    /**
     * Updates all button states, with accessibility announcement for the toggled format.
     *
     * @param toggledFormat The format that was toggled.
     * @param isNowActive   Whether the toggled format is now active.
     */
    private void updateButtonStatesWithAnnouncement(FormatType toggledFormat, boolean isNowActive) {
        Set<FormatType> disabledFormats = getDisabledFormats();

        updateButtonStateForFormat(binding.cardBold, binding.btnBold, FormatType.BOLD, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardItalic, binding.btnItalic, FormatType.ITALIC, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardStrikethrough, binding.btnStrikethrough, FormatType.STRIKETHROUGH, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardUnderline, binding.btnUnderline, FormatType.UNDERLINE, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardInlineCode, binding.btnInlineCode, FormatType.INLINE_CODE, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardCodeBlock, binding.btnCodeBlock, FormatType.CODE_BLOCK, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardLink, binding.btnLink, FormatType.LINK, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardBulletList, binding.btnBulletList, FormatType.BULLET_LIST, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardNumberedList, binding.btnNumberedList, FormatType.ORDERED_LIST, disabledFormats, toggledFormat);
        updateButtonStateForFormat(binding.cardBlockquote, binding.btnBlockquote, FormatType.BLOCKQUOTE, disabledFormats, toggledFormat);
    }

    /**
     * Updates a button state, announcing if it's the toggled format.
     *
     * @param card            The card view wrapping the button.
     * @param button          The button to update.
     * @param buttonFormat    The format type this button represents.
     * @param disabledFormats The set of currently disabled formats.
     * @param toggledFormat   The format that was toggled.
     */
    private void updateButtonStateForFormat(MaterialCardView card, ImageButton button, FormatType buttonFormat,
                                            Set<FormatType> disabledFormats, FormatType toggledFormat) {
        boolean shouldAnnounce = buttonFormat == toggledFormat;
        updateButtonState(card, button, buttonFormat, disabledFormats, shouldAnnounce);
    }

    /**
     * Updates the visual state of all format buttons based on active formats.
     */
    private void updateButtonStates() {
        Set<FormatType> disabledFormats = getDisabledFormats();

        updateButtonState(binding.cardBold, binding.btnBold, FormatType.BOLD, disabledFormats, false);
        updateButtonState(binding.cardItalic, binding.btnItalic, FormatType.ITALIC, disabledFormats, false);
        updateButtonState(binding.cardStrikethrough, binding.btnStrikethrough, FormatType.STRIKETHROUGH, disabledFormats, false);
        updateButtonState(binding.cardUnderline, binding.btnUnderline, FormatType.UNDERLINE, disabledFormats, false);
        updateButtonState(binding.cardInlineCode, binding.btnInlineCode, FormatType.INLINE_CODE, disabledFormats, false);
        updateButtonState(binding.cardCodeBlock, binding.btnCodeBlock, FormatType.CODE_BLOCK, disabledFormats, false);
        updateButtonState(binding.cardLink, binding.btnLink, FormatType.LINK, disabledFormats, false);
        updateButtonState(binding.cardBulletList, binding.btnBulletList, FormatType.BULLET_LIST, disabledFormats, false);
        updateButtonState(binding.cardNumberedList, binding.btnNumberedList, FormatType.ORDERED_LIST, disabledFormats, false);
        updateButtonState(binding.cardBlockquote, binding.btnBlockquote, FormatType.BLOCKQUOTE, disabledFormats, false);
    }

    /**
     * Updates the visual state of a single format button.
     *
     * @param card            The card view wrapping the button.
     * @param button          The button to update.
     * @param formatType      The format type this button represents.
     * @param disabledFormats The set of currently disabled formats.
     * @param announce        Whether to announce the state change for accessibility.
     */
    private void updateButtonState(MaterialCardView card, ImageButton button, FormatType formatType,
                                   Set<FormatType> disabledFormats, boolean announce) {
        boolean isActive = activeFormats.contains(formatType);
        boolean isDisabled = disabledFormats.contains(formatType);
        boolean wasActive = button.isSelected();
        boolean wasEnabled = button.isEnabled();

        button.setSelected(isActive);
        button.setEnabled(!isDisabled);

        if (isDisabled) {
            // Disabled state - reduced opacity and not clickable
            button.setColorFilter(disabledIconTint, PorterDuff.Mode.SRC_IN);
            card.setCardBackgroundColor(0);
            button.setAlpha(0.4f);
        } else if (isActive) {
            // Active state
            button.setColorFilter(activeIconTint, PorterDuff.Mode.SRC_IN);
            card.setCardBackgroundColor(activeIconBackgroundColor);
            button.setAlpha(1.0f);
        } else {
            // Normal inactive state
            button.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN);
            card.setCardBackgroundColor(0);
            button.setAlpha(1.0f);
        }

        // Announce state change for accessibility (TalkBack)
        if (announce) {
            CharSequence formatName = button.getContentDescription();
            if (formatName != null) {
                if (wasEnabled != !isDisabled) {
                    // Announce enabled/disabled state change
                    String announcement = getContext().getString(
                            isDisabled ? R.string.cometchat_format_disabled : R.string.cometchat_format_enabled,
                            formatName
                    );
                    button.announceForAccessibility(announcement);
                } else if (wasActive != isActive) {
                    // Announce active/inactive state change
                    String announcement = getContext().getString(
                            isActive ? R.string.cometchat_format_enabled : R.string.cometchat_format_disabled,
                            formatName
                    );
                    button.announceForAccessibility(announcement);
                }
            }
        }
    }

    /**
     * Updates the visual state of a single format button with accessibility announcement.
     *
     * @param card       The card view wrapping the button.
     * @param button     The button to update.
     * @param formatType The format type this button represents.
     */
    private void updateButtonStateWithAnnouncement(MaterialCardView card, ImageButton button, FormatType formatType) {
        updateButtonState(card, button, formatType, getDisabledFormats(), true);
    }

    /**
     * Sets the toolbar background color.
     *
     * @param color The background color.
     */
    public void setToolbarBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        binding.toolbarContainer.setBackgroundColor(color);
    }

    /**
     * Sets the icon tint color for inactive buttons.
     *
     * @param color The icon tint color.
     */
    public void setIconTint(@ColorInt int color) {
        this.iconTint = color;
        updateButtonStates();
    }

    /**
     * Sets the icon tint color for active buttons.
     *
     * @param color The active icon tint color.
     */
    public void setActiveIconTint(@ColorInt int color) {
        this.activeIconTint = color;
        updateButtonStates();
    }

    /**
     * Sets the background color for active buttons.
     *
     * @param color The active icon background color.
     */
    public void setActiveIconBackgroundColor(@ColorInt int color) {
        this.activeIconBackgroundColor = color;
        updateButtonStates();
    }

    /**
     * Sets the separator color.
     *
     * @param color The separator color.
     */
    public void setSeparatorColor(@ColorInt int color) {
        this.separatorColor = color;
        binding.separator1.setBackgroundColor(color);
        binding.separator2.setBackgroundColor(color);
    }

    /**
     * Sets the icon tint color for disabled buttons.
     *
     * @param color The disabled icon tint color.
     */
    public void setDisabledIconTint(@ColorInt int color) {
        this.disabledIconTint = color;
        updateButtonStates();
    }

    /**
     * Sets the format click listener.
     *
     * @param listener The listener to set.
     */
    public void setOnFormatClickListener(@Nullable OnFormatClickListener listener) {
        this.onFormatClickListener = listener;
    }

    /**
     * Interface for format button click callbacks.
     */
    public interface OnFormatClickListener {
        /**
         * Called when a format button is clicked.
         *
         * @param formatType The format type that was clicked.
         */
        void onFormatClick(FormatType formatType);
    }
}
