package com.cometchat.chatuikit.shared.views.messageinput;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMessageInputLayoutBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

public class CometChatMessageInput extends MaterialCardView {
    private static final String TAG = CometChatMessageInput.class.getSimpleName();
    private CometchatMessageInputLayoutBinding binding;
    private CometChatTextWatcher textWatcher;

    private @ColorInt int messageInputBackgroundColor;
    private @Dimension int messageInputStrokeWidth;
    private @ColorInt int messageInputStrokeColor;
    private @Dimension int messageInputCornerRadius;
    private @StyleRes int messageInputTextAppearance;
    private @ColorInt int messageInputTextColor;
    private @ColorInt int messageInputSeparatorColor;
    private @ColorInt int messageInputPlaceHolderTextColor;
    private int auxiliaryButtonVisibility;

    /**
     * Constructs a new instance of CometChatMessageInput.
     *
     * @param context The context in which the message input is created.
     */
    public CometChatMessageInput(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new instance of CometChatMessageInput.
     *
     * @param context The context in which the message input is created.
     * @param attrs   The attribute set for the message input.
     */
    public CometChatMessageInput(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMessageInputStyle);
    }

    /**
     * Constructs a new instance of CometChatMessageInput.
     *
     * @param context      The context in which the message input is created.
     * @param attrs        The attribute set for the message input.
     * @param defStyleAttr The default style attribute for the message input.
     */
    public CometChatMessageInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatMessageInputLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.cometchatComposeBox.setTextWatcher(textWatcher);
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageInput, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatMessageInput_cometchatMessageInputStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageInput, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            messageInputBackgroundColor = typedArray.getColor(R.styleable.CometChatMessageInput_cometchatMessageInputBackgroundColor, 0);
            messageInputSeparatorColor = typedArray.getColor(R.styleable.CometChatMessageInput_cometchatMessageInputSeparatorColor, 0);
            messageInputStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageInput_cometchatMessageInputStrokeWidth, 0);
            messageInputStrokeColor = typedArray.getColor(R.styleable.CometChatMessageInput_cometchatMessageInputStrokeColor, 0);
            messageInputCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageInput_cometchatMessageInputCornerRadius, 0);
            messageInputTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInput_cometchatMessageInputTextAppearance, 0);
            messageInputTextColor = typedArray.getColor(R.styleable.CometChatMessageInput_cometchatMessageInputTextColor, 0);
            messageInputPlaceHolderTextColor = typedArray.getColor(R.styleable.CometChatMessageInput_cometchatMessageInputPlaceHolderTextColor, 0);
            // Apply default styles
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the default values .
     */
    private void applyDefault() {
        setMessageInputBackgroundColor(messageInputBackgroundColor);
        setMessageInputStrokeWidth(messageInputStrokeWidth);
        setMessageInputStrokeColor(messageInputStrokeColor);
        setMessageInputCornerRadius(messageInputCornerRadius);
        setMessageInputTextAppearance(messageInputTextAppearance);
        setMessageInputTextColor(messageInputTextColor);
        setMessageInputPlaceHolderTextColor(messageInputPlaceHolderTextColor);
        setSeparatorColor(messageInputSeparatorColor);
    }

    /**
     * Sets the color of the separator line between the compose box and auxiliary
     * buttons.
     *
     * @param color The color to set for the separator line.
     */
    public void setSeparatorColor(@ColorInt int color) {
        if (color != 0) {
            binding.divider.setBackgroundColor(color);
        }
    }

    /**
     * Sets the style for the CometChatMessageInput view by applying a style
     * resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMessageInput);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the background color of the message input.
     *
     * @return The background color of the message input, represented as a color
     * integer.
     */
    public @ColorInt int getMessageInputBackgroundColor() {
        return messageInputBackgroundColor;
    }

    /**
     * Sets the background color of the message input.
     *
     * @param messageInputBackgroundColor The background color to set for the message input.
     */
    public void setMessageInputBackgroundColor(@ColorInt int messageInputBackgroundColor) {
        this.messageInputBackgroundColor = messageInputBackgroundColor;
        setCardBackgroundColor(messageInputBackgroundColor);
    }

    /**
     * Returns the stroke width of the message input.
     *
     * @return The stroke width of the message input, represented as a dimension
     * integer.
     */
    public @Dimension int getMessageInputStrokeWidth() {
        return messageInputStrokeWidth;
    }

    /**
     * Sets the stroke width of the message input.
     *
     * @param messageInputStrokeWidth The stroke width to set for the message input.
     */
    public void setMessageInputStrokeWidth(@Dimension int messageInputStrokeWidth) {
        this.messageInputStrokeWidth = messageInputStrokeWidth;
        setStrokeWidth(messageInputStrokeWidth);
    }

    /**
     * Returns the stroke color of the message input.
     *
     * @return The stroke color of the message input, represented as a color
     * integer.
     */
    public @ColorInt int getMessageInputStrokeColor() {
        return messageInputStrokeColor;
    }

    /**
     * Sets the stroke color of the message input.
     *
     * @param messageInputStrokeColor The stroke color to set for the message input.
     */
    public void setMessageInputStrokeColor(@ColorInt int messageInputStrokeColor) {
        this.messageInputStrokeColor = messageInputStrokeColor;
        setStrokeColor(messageInputStrokeColor);
    }

    /**
     * Returns the corner radius of the message input.
     *
     * @return The corner radius of the message input, represented as a dimension
     * integer.
     */
    public @Dimension int getMessageInputCornerRadius() {
        return messageInputCornerRadius;
    }

    /**
     * Sets the corner radius of the message input.
     *
     * @param messageInputCornerRadius The corner radius to set for the message input.
     */
    public void setMessageInputCornerRadius(@Dimension int messageInputCornerRadius) {
        this.messageInputCornerRadius = messageInputCornerRadius;
        setRadius(messageInputCornerRadius);
    }

    /**
     * Returns the text style of the compose box.
     *
     * @return The text style of the compose box, represented as a style resource
     * integer.
     */
    public @StyleRes int getMessageInputTextAppearance() {
        return messageInputTextAppearance;
    }

    /**
     * Sets the text style of the compose box.
     *
     * @param messageInputTextAppearance The text style to set for the compose box.
     */
    public void setMessageInputTextAppearance(@StyleRes int messageInputTextAppearance) {
        this.messageInputTextAppearance = messageInputTextAppearance;
        binding.cometchatComposeBox.setTextAppearance(messageInputTextAppearance);
    }

    /**
     * Returns the text color of the compose box.
     *
     * @return The text color of the compose box, represented as a color integer.
     */
    public @ColorInt int getMessageInputTextColor() {
        return messageInputTextColor;
    }

    /**
     * Sets the text color of the compose box.
     *
     * @param messageInputTextColor The text color to set for the compose box.
     */
    public void setMessageInputTextColor(@ColorInt int messageInputTextColor) {
        this.messageInputTextColor = messageInputTextColor;
        binding.cometchatComposeBox.setTextColor(messageInputTextColor);
    }

    /**
     * Returns the placeholder text color of the compose box.
     *
     * @return The placeholder text color of the compose box, represented as a color
     * integer.
     */
    public @ColorInt int getMessageInputPlaceHolderTextColor() {
        return messageInputPlaceHolderTextColor;
    }

    /**
     * Sets the placeholder text color of the compose box.
     *
     * @param messageInputPlaceHolderTextColor The placeholder text color to set for the compose box.
     */
    public void setMessageInputPlaceHolderTextColor(@ColorInt int messageInputPlaceHolderTextColor) {
        this.messageInputPlaceHolderTextColor = messageInputPlaceHolderTextColor;
        binding.cometchatComposeBox.setHintTextColor(messageInputPlaceHolderTextColor);
    }

    /**
     * Removes the text watcher from the compose box if it is currently set. This
     * prevents further callbacks to the text watcher when the text is modified.
     *
     * <p>
     * If no text watcher is set, this method does nothing.
     */
    public void removeTextWatcher() {
        if (textWatcher != null) {
            binding.cometchatComposeBox.removeTextChangedListener(null);
        }
    }

    /**
     * Returns the current selection start position within the compose box.
     *
     * @return The start position of the selection, or -1 if there is no selection.
     */
    public int getSelectionStart() {
        return binding.cometchatComposeBox.getSelectionStart();
    }

    /**
     * Returns the current selection end position within the compose box.
     *
     * @return The end position of the selection, or -1 if there is no selection.
     */
    public int getSelectionEnd() {
        return binding.cometchatComposeBox.getSelectionEnd();
    }

    /**
     * Requests focus for the compose box, allowing it to receive input.
     */
    public void requestEditBoxFocus() {
        binding.cometchatComposeBox.requestFocus();
    }

    /**
     * Sets the selection in the compose box to the specified index.
     *
     * @param index The index at which to set the selection.
     */
    public void setTextSelection(int index) {
        binding.cometchatComposeBox.setSelection(index);
    }

    /**
     * Sets the visibility of the separator line between the compose box and
     * auxiliary buttons.
     *
     * @param visibility The visibility to set for the separator line.
     */
    public void setSeparatorVisibility(int visibility) {
        binding.divider.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the auxiliary button view.
     *
     * @param visibility The visibility to set for the auxiliary button view.
     */
    public void setAuxiliaryButtonVisibility(int visibility) {
        this.auxiliaryButtonVisibility = visibility;
        binding.auxiliaryView.setVisibility(visibility);
    }

    /**
     * Sets the alignment of the auxiliary button view within the message input.
     *
     * @param alignment The alignment to set for the auxiliary button view.
     */
    public void setAuxiliaryButtonAlignment(UIKitConstants.AuxiliaryButtonAlignment alignment) {
        if (alignment != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.auxiliaryView.getLayoutParams();
            if (UIKitConstants.AuxiliaryButtonAlignment.LEFT.equals(alignment)) {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            } else if (UIKitConstants.AuxiliaryButtonAlignment.RIGHT.equals(alignment)) {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            binding.auxiliaryView.setLayoutParams(params);
        }
    }

    /**
     * Sets the maximum number of lines for the compose box.
     *
     * @param value The maximum number of lines to set.
     */
    public void setMaxLine(int value) {
        if (value > 0) {
            binding.cometchatComposeBox.setMaxLines(value);
        }
    }

    /**
     * Sets a spannable string as the text in the compose box.
     *
     * @param text The spannable string to set in the compose box. If null, no action
     *             is taken.
     */
    public void setSpannableText(SpannableString text) {
        if (text != null) {
            binding.cometchatComposeBox.setText(text);
        }
    }

    /**
     * Retrieves the current text from the compose box as a string.
     *
     * @return The text currently in the compose box, converted to a string.
     */
    public String getText() {
        return binding.cometchatComposeBox.getText() + "";
    }

    /**
     * Sets the text of the compose box.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        if (text != null) {
            binding.cometchatComposeBox.setText(text);
        }
    }

    /**
     * Retrieves the current text from the compose box as an editable object.
     *
     * @return The editable text currently in the compose box.
     */
    public Editable getEditableText() {
        return binding.cometchatComposeBox.getText();
    }

    /**
     * Returns the secondary button view of the message input.
     *
     * @return The secondary button view.
     */
    public LinearLayout getSecondaryButtonView() {
        return binding.secondaryView;
    }

    /**
     * Sets the secondary button view for the message input.
     *
     * @param view The secondary button view to set.
     */
    public void setSecondaryButtonView(View view) {
        if (view != null) {
            Utils.handleView(binding.secondaryView, view, false);
        }
    }

    /**
     * Returns the primary button view of the message input.
     *
     * @return The primary button view.
     */
    public LinearLayout getPrimaryButtonView() {
        return binding.primaryView;
    }

    /**
     * Sets the primary button view for the message input.
     *
     * @param view The primary button view to set.
     */
    public void setPrimaryButtonView(View view) {
        if (view != null) {
            Utils.handleView(binding.primaryView, view, false);
        }
    }

    /**
     * Returns the auxiliary button view of the message input.
     *
     * @return The auxiliary button view.
     */
    @NonNull
    public RelativeLayout getAuxiliaryButtonView() {
        return binding.auxiliaryView;
    }

    /**
     * Sets the auxiliary button view for the message input.
     *
     * @param view The auxiliary button view to set.
     */
    public void setAuxiliaryButtonView(View view) {
        Utils.handleView(binding.auxiliaryView, view, false);
    }

    /**
     * Returns the bottom container of the message input.
     *
     * @return The bottom container view.
     */
    public LinearLayout getBottomContainer() {
        return binding.bottomContainer;
    }

    /**
     * Sets the listener for text changes in the compose box.
     *
     * @param textWatcher The listener to set.
     */
    public void setOnTextChangedListener(CometChatTextWatcher textWatcher) {
        if (textWatcher != null) {
            this.textWatcher = textWatcher;
            binding.cometchatComposeBox.setTextWatcher(textWatcher);
        }
    }

    /**
     * Returns the parent view of the message input.
     *
     * @return The parent view.
     */
    @NonNull
    public LinearLayout getView() {
        return binding.parent;
    }

    /**
     * Sets the text selection position in the compose box.
     *
     * @param spanEnd The index position where the selection should end. If the index is
     *                out of bounds, it may throw an exception.
     */
    public void setSelection(int spanEnd) {
        binding.cometchatComposeBox.setSelection(spanEnd);
    }

    /**
     * Retrieves the compose box view for direct access to its properties and
     * methods.
     *
     * @return The {@link CometChatEditText} instance representing the compose box.
     */
    public CometChatEditText getComposeBox() {
        return binding.cometchatComposeBox;
    }

    /**
     * Sets the placeholder text for the compose box.
     *
     * @param text The placeholder text to set.
     */
    public void setPlaceHolderText(String text) {
        binding.cometchatComposeBox.setHint(text);
    }
}
