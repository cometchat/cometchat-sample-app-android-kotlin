package com.cometchat.chatuikit.shared.views.optionsheet.attachmentoptionsheet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAttachmentOptionSheetBinding;
import com.cometchat.chatuikit.shared.interfaces.OptionSheetClickListener;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetAdapter;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetMenuItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom view that represents an attachment options sheet for CometChat. It
 * extends MaterialCardView to provide a styled UI for attachment options.
 */
public class CometChatAttachmentOptionSheet extends MaterialCardView {
    private static final String TAG = CometChatAttachmentOptionSheet.class.getSimpleName();
    private final List<OptionSheetMenuItem> attachmentItems = new ArrayList<>();
    private CometchatAttachmentOptionSheetBinding binding;
    private OptionSheetAdapter adapter;
    private OptionSheetClickListener attachmentOptionClickListener;

    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleColor;
    private @ColorInt int iconTint;
    private @ColorInt int backgroundColor;
    private @ColorInt int strokeColor;
    private @Dimension int strokeWidth;
    private @Dimension int cornerRadius;

    /**
     * Constructor for creating the attachment option sheet with a context.
     *
     * @param context The context to use for the view.
     */
    public CometChatAttachmentOptionSheet(Context context) {
        this(context, null);
    }

    /**
     * Constructor for creating the attachment option sheet with a context and
     * attributes.
     *
     * @param context The context to use for the view.
     * @param attrs   The attribute set to use for styling.
     */
    public CometChatAttachmentOptionSheet(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAttachmentOptionSheetStyle);
    }

    /**
     * Constructor for creating the attachment option sheet with a context,
     * attributes, and style.
     *
     * @param context      The context to use for the view.
     * @param attrs        The attribute set to use for styling.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatAttachmentOptionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates the view and initializes the UI components.
     *
     * @param attrs        The attribute set to use for styling.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Reset the card view to default values
        Utils.initMaterialCard(this);

        // Inflate the view
        binding = CometchatAttachmentOptionSheetBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Initialize recycler view
        initRecyclerView();

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Initializes the RecyclerView, sets up the adapter, and adds an item touch
     * listener.
     */
    private void initRecyclerView() {
        adapter = new OptionSheetAdapter(getContext(), attachmentItems);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.scheduleLayoutAnimation();
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.cometchat_color_background4, getContext().getTheme()));
                OptionSheetMenuItem item = (OptionSheetMenuItem) view.getTag(R.string.cometchat_action_item);
                if (attachmentOptionClickListener != null) {
                    attachmentOptionClickListener.onOptionSheetItemClick(item);
                }
            }
        }));
    }

    /**
     * Applies the style attributes from XML to the view.
     *
     * @param attrs        The attribute set to use for styling.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatAttachmentOptionSheet, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetStyle, 0);
        directAttributes = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatAttachmentOptionSheet, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts attributes from the provided TypedArray and applies default values.
     *
     * @param typedArray The TypedArray containing style attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetTitleTextAppearance,
                                                           0);
            titleColor = typedArray.getColor(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetTitleColor,
                                             CometChatTheme.getTextColorPrimary(getContext()));
            iconTint = typedArray.getColor(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetIconTint,
                                           CometChatTheme.getIconTintHighlight(getContext()));
            backgroundColor = typedArray.getColor(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetBackgroundColor,
                                                  CometChatTheme.getBackgroundColor1(getContext()));
            strokeColor = typedArray.getColor(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetStrokeColor,
                                              CometChatTheme.getStrokeColorLight(getContext()));
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetStrokeWidth, 0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatAttachmentOptionSheet_cometchatAttachmentOptionSheetCornerRadius, 0);
            // Call setters
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Updates the UI elements of the attachment option sheet based on the current
     * attributes.
     */
    private void updateUI() {
        setTitleTextAppearance(titleTextAppearance);
        setTitleColor(titleColor);
        setIconTint(iconTint);
        setBackgroundColor(backgroundColor);
        setStrokeColor(strokeColor);
        setStrokeWidth(strokeWidth);
        setCornerRadius(cornerRadius);
    }

    /**
     * Sets the stroke color for the attachment option sheet.
     *
     * @param strokeColor The stroke color to set as an integer.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        if (binding != null) {
            binding.viewBottomSheet.setStrokeColor(strokeColor);
        }
    }

    /**
     * Gets the stroke color of the attachment option sheet.
     *
     * @return The stroke color as an integer.
     */
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Sets the style for the attachment option sheet.
     *
     * @param style The style resource ID to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAttachmentOptionSheet);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Centers the text alignment for the attachment option items.
     */
    public void textAlignCenter() {
        mTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    /**
     * Sets the text alignment for the attachment option items.
     *
     * @param alignment The alignment to apply.
     */
    private void mTextAlignment(int alignment) {
        if (adapter != null) {
            adapter.setItemTextAlignment(alignment);
        }
    }

    /**
     * Aligns the text to the start for the attachment option items.
     */
    public void textAlignStart() {
        mTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
    }

    /**
     * Aligns the text to the end for the attachment option items.
     */
    public void textAlignEnd() {
        mTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
    }

    /**
     * Gets the corner radius of the attachment option sheet.
     *
     * @return The corner radius in pixels.
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }    /**
     * Gets the stroke width of the attachment option sheet.
     *
     * @return The stroke width in pixels.
     */
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the corner radius for the attachment option sheet.
     *
     * @param cornerRadius The corner radius to set in pixels.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        if (binding != null) {
            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0)
                .build();
            binding.viewBottomSheet.setShapeAppearanceModel(shapeAppearanceModel);
        }
    }    /**
     * Sets the stroke width for the attachment option sheet.
     *
     * @param strokeWidth The stroke width to set in pixels.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        if (binding != null) {
            binding.viewBottomSheet.setStrokeWidth(strokeWidth);
        }
    }

    /**
     * Gets the background color of the attachment option sheet.
     *
     * @return The background color as an integer.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the attachment option sheet.
     *
     * @param backgroundColor The background color to set as an integer.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        if (binding != null) {
            binding.viewBottomSheet.setCardBackgroundColor(backgroundColor);
        }
    }

    /**
     * Gets the icon tint color for the attachment option sheet.
     *
     * @return The icon tint color as an integer.
     */
    public @ColorInt int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the icon tint color for the attachment option sheet.
     *
     * @param iconTint The icon tint color to set as an integer.
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
        if (adapter != null) {
            adapter.setIconTint(iconTint);
        }
    }

    /**
     * Gets the title color of the attachment option sheet.
     *
     * @return The title color as an integer.
     */
    public @ColorInt int getTitleColor() {
        return titleColor;
    }

    /**
     * Sets the title color for the attachment option sheet.
     *
     * @param titleColor The title color to set as an integer.
     */
    public void setTitleColor(@ColorInt int titleColor) {
        this.titleColor = titleColor;
        if (adapter != null) {
            adapter.setTextColor(titleColor);
        }
    }

    /**
     * Gets the title text appearance resource ID for the attachment option sheet.
     *
     * @return The title text appearance resource ID.
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the title text appearance for the attachment option sheet.
     *
     * @param titleTextAppearance The title text appearance resource ID to set.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        if (adapter != null) {
            adapter.setTextAppearance(titleTextAppearance);
        }
    }

    /**
     * Adds an attachment option item to the attachment option sheet.
     *
     * @param actionItem The OptionSheetMenuItem to add.
     */
    public void addAttachmentOptionItem(OptionSheetMenuItem actionItem) {
        if (adapter != null) {
            adapter.addOptionItem(actionItem);
        }
    }

    /**
     * Updates an existing attachment option item in the attachment option sheet.
     *
     * @param actionItem The OptionSheetMenuItem to update.
     */
    public void updateAttachmentOptionItem(@NonNull OptionSheetMenuItem actionItem) {
        if (adapter != null) {
            adapter.updateOptionItem(actionItem);
        }
    }

    /**
     * Gets the list of attachment option items.
     *
     * @return A list of OptionSheetMenuItem.
     */
    public List<OptionSheetMenuItem> getAttachmentOptionItems() {
        return attachmentItems;
    }

    /**
     * Sets the attachment option items for the attachment option sheet.
     *
     * @param optionSheetMenuItemList The list of OptionSheetMenuItem to set.
     */
    public void setAttachmentOptionItems(List<OptionSheetMenuItem> optionSheetMenuItemList) {
        this.attachmentItems.clear();
        this.attachmentItems.addAll(optionSheetMenuItemList);
    }

    /**
     * Sets the click listener for attachment option items.
     *
     * @param attachmentOptionClickListener The listener to set for attachment option clicks.
     */
    public void setAttachmentOptionClickListener(OptionSheetClickListener attachmentOptionClickListener) {
        this.attachmentOptionClickListener = attachmentOptionClickListener;
    }




}
