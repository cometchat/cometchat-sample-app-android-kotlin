package com.cometchat.chatuikit.shared.views.optionsheet.messageoptionsheet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMessageOptionSheetBinding;
import com.cometchat.chatuikit.databinding.CometchatQuickReactionViewBinding;
import com.cometchat.chatuikit.shared.interfaces.EmojiPickerClickListener;
import com.cometchat.chatuikit.shared.interfaces.MessageOptionClickListener;
import com.cometchat.chatuikit.shared.interfaces.ReactionClickListener;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetAdapter;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetMenuItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CometChatMessageOptionSheet extends MaterialCardView {
    private static final String TAG = CometChatMessageOptionSheet.class.getSimpleName();
    private final int emojiPickerIconViewId = View.generateViewId();
    private final List<String> quickReactionItems = new ArrayList<>();
    private final List<OptionSheetMenuItem> messageOptionItems = new ArrayList<>();
    private CometchatMessageOptionSheetBinding binding;
    private boolean hideEmojiPicker;
    private OptionSheetAdapter adapter;
    private Drawable emojiPickerIcon;
    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int iconTint;
    private @ColorInt int strokeColor;
    private @Dimension int strokeWidth;
    private @Dimension int cornerRadius;

    private EmojiPickerClickListener emojiPickerClickListener;
    private MessageOptionClickListener messageOptionClickListener;
    private ReactionClickListener reactionClickListener;


    /**
     * Custom view for displaying message options in the CometChat application.
     *
     * @param context the context of the application
     */
    public CometChatMessageOptionSheet(Context context) {
        this(context, null);
    }

    /**
     * Constructor that inflates the view with XML attributes.
     *
     * @param context the context of the application
     * @param attrs   the attributes set in XML
     */
    public CometChatMessageOptionSheet(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMessageOptionSheetStyle);
    }

    /**
     * Constructor that applies the default style attributes.
     *
     * @param context      the context of the application
     * @param attrs        the attributes set in XML
     * @param defStyleAttr the default style attribute to apply
     */
    public CometChatMessageOptionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates the layout and initializes the view components.
     *
     * @param attrs        the attributes set in XML
     * @param defStyleAttr the default style attribute to apply
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatMessageOptionSheetBinding.inflate(LayoutInflater.from(getContext()), this, true);
        initRecyclerView();
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Initializes the RecyclerView for displaying options.
     */
    private void initRecyclerView() {
        adapter = new OptionSheetAdapter(getContext(), messageOptionItems);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.scheduleLayoutAnimation();
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.cometchat_color_background4, getContext().getTheme()));
                OptionSheetMenuItem item = (OptionSheetMenuItem) view.getTag(R.string.cometchat_action_item);
                if (messageOptionClickListener != null) {
                    messageOptionClickListener.onMessageOptionClick(item);
                }
            }
        }));
    }

    /**
     * Applies the style attributes from XML to the view.
     *
     * @param attrs        the attributes set in XML
     * @param defStyleAttr the default style attribute to apply
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageOptionSheet, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageOptionSheet, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts attributes from a TypedArray and applies default values if
     * necessary.
     *
     * @param typedArray the TypedArray containing the attributes
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetTitleTextAppearance, 0);
            titleColor = typedArray.getColor(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetTitleColor, 0);
            iconTint = typedArray.getColor(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetIconTint, 0);
            backgroundColor = typedArray.getColor(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetBackgroundColor, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetStrokeColor, 0);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetStrokeWidth, 0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetCornerRadius, 0);
            emojiPickerIcon = typedArray.getDrawable(R.styleable.CometChatMessageOptionSheet_cometchatMessageOptionSheetEmojiPickerIcon);
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Updates the UI with the extracted style attributes.
     */
    private void updateUI() {
        setTitleTextAppearance(titleTextAppearance);
        setTitleColor(titleColor);
        setIconTint(iconTint);
        setBackgroundColor(backgroundColor);
        setStrokeColor(strokeColor);
        setStrokeWidth(strokeWidth);
        setCornerRadius(cornerRadius);
        hideEmojiPicker(hideEmojiPicker);
        disableReactions(false);
    }

    /**
     * Sets the stroke color for the message option sheet.
     *
     * @param strokeColor the stroke color as an integer
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        if (binding != null) {
            binding.viewBottomSheet.setStrokeColor(strokeColor);
        }
    }

    /**
     * Gets the stroke color of the message option sheet.
     *
     * @return the stroke color as an integer
     */
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Sets the visibility of the emoji picker.
     *
     * @param hideEmojiPicker true to hide the emoji picker, false to show
     */
    public void hideEmojiPicker(boolean hideEmojiPicker) {
        this.hideEmojiPicker = hideEmojiPicker;
        View parentView = binding.viewReactions.findViewById(emojiPickerIconViewId);
        if (parentView != null) {
            parentView.setVisibility(hideEmojiPicker ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Enables or disables the visibility of reactions.
     *
     * @param disableReaction true to hide reactions, false to show
     */
    public void disableReactions(boolean disableReaction) {
        if (disableReaction) {
            binding.horizontalContainerReactions.setVisibility(View.GONE);
            hideDivider(true);
        } else {
            binding.horizontalContainerReactions.setVisibility(View.VISIBLE);
            hideDivider(false);
        }
    }

    /**
     * Hides or shows the divider based on the provided boolean.
     *
     * @param hide true to hide the divider, false to show
     */
    private void hideDivider(boolean hide) {
        if (binding != null) {
            binding.viewDivider.setVisibility(hide ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Sets the style of the message option sheet.
     *
     * @param style the resource ID of the style to apply
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMessageOptionSheet);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the corner radius of the message option sheet.
     *
     * @return the corner radius in pixels
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the message option sheet.
     *
     * @param cornerRadius the corner radius in pixels
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
    }

    /**
     * Gets the background color of the message option sheet.
     *
     * @return the background color as an integer
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the message option sheet.
     *
     * @param backgroundColor the background color as an integer
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        if (binding != null) {
            binding.viewBottomSheet.setCardBackgroundColor(backgroundColor);
        }
    }

    /**
     * Gets the tint color for the icon.
     *
     * @return the icon tint color as an integer
     */
    public @ColorInt int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the tint color for the icon.
     *
     * @param iconTint the icon tint color as an integer
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
        if (adapter != null) {
            adapter.setIconTint(iconTint);
        }
    }

    /**
     * Gets the color of the title text.
     *
     * @return the title color as an integer
     */
    public @ColorInt int getTitleColor() {
        return titleColor;
    }

    /**
     * Sets the color of the title text.
     *
     * @param titleColor the title color as an integer
     */
    public void setTitleColor(@ColorInt int titleColor) {
        this.titleColor = titleColor;
        if (adapter != null) {
            adapter.setTextColor(titleColor);
        }
    }

    /**
     * Gets the text appearance style resource for the title.
     *
     * @return the title text appearance style resource ID
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style resource for the title.
     *
     * @param titleTextAppearance the title text appearance style resource ID
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        if (adapter != null) {
            adapter.setTextAppearance(titleTextAppearance);
        }
    }

    /**
     * Adds an option item to the menu.
     *
     * @param actionItem the option item to add
     */
    public void addOptionItem(OptionSheetMenuItem actionItem) {
        if (adapter != null) {
            adapter.addOptionItem(actionItem);
        }
    }

    /**
     * Updates an existing option item in the menu.
     *
     * @param actionItem the option item to update
     */
    public void updateOptionItem(OptionSheetMenuItem actionItem) {
        if (adapter != null) {
            adapter.updateOptionItem(actionItem);
        }
    }    /**
     * Called when the view is attached to a window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        configureReactions();
    }

    /**
     * Gets the list of message option items.
     *
     * @return the list of message option items
     */
    public List<OptionSheetMenuItem> getMessageOptionItems() {
        return messageOptionItems;
    }

    /**
     * Sets the list of message option items.
     *
     * @param messageOptionItems the list of message option items to set
     */
    public void setMessageOptionItems(List<OptionSheetMenuItem> messageOptionItems) {
        this.messageOptionItems.clear();
        this.messageOptionItems.addAll(messageOptionItems);
    }

    /**
     * Gets the drawable icon for the emoji picker.
     *
     * @return the emoji picker icon drawable
     */
    public Drawable getEmojiPickerIcon() {
        return emojiPickerIcon;
    }

    /**
     * Sets the drawable icon for the emoji picker.
     *
     * @param emojiPickerIcon the emoji picker icon drawable
     */
    public void setEmojiPickerIcon(Drawable emojiPickerIcon) {
        if (emojiPickerIcon != null) {
            this.emojiPickerIcon = emojiPickerIcon;
            View parentView = binding.viewReactions.findViewById(emojiPickerIconViewId);
            CometchatQuickReactionViewBinding reactionView = CometchatQuickReactionViewBinding.bind(parentView);
            reactionView.tvReaction.setBackground(emojiPickerIcon);
        }
    }

    /**
     * Checks whether the emoji picker is hidden.
     *
     * @return true if the emoji picker is hidden, false otherwise
     */
    public boolean hideEmojiPicker() {
        return hideEmojiPicker;
    }

    /**
     * Sets the list of reactions that will be quickly accessible for the user.
     *
     * @param quickReactions The list of reactions to display for quick access.
     */
    public void setQuickReactions(List<String> quickReactions) {
        this.quickReactionItems.clear();
        this.quickReactionItems.addAll(quickReactions);
    }

    /**
     * Gets the listener for emoji picker clicks.
     *
     * @return the emoji picker click listener
     */
    public EmojiPickerClickListener getEmojiPickerClickListener() {
        return emojiPickerClickListener;
    }

    /**
     * Sets the listener for emoji picker clicks.
     *
     * @param emojiPickerClickListener the emoji picker click listener
     */
    public void setEmojiPickerClickListener(EmojiPickerClickListener emojiPickerClickListener) {
        this.emojiPickerClickListener = emojiPickerClickListener;
    }

    /**
     * Gets the listener for message option clicks.
     *
     * @return the message option click listener
     */
    public MessageOptionClickListener getMessageOptionClickListener() {
        return messageOptionClickListener;
    }

    /**
     * Sets the listener for message option clicks.
     *
     * @param messageOptionClickListener the message option click listener
     */
    public void setMessageOptionClickListener(MessageOptionClickListener messageOptionClickListener) {
        this.messageOptionClickListener = messageOptionClickListener;
    }

    /**
     * Gets the listener for reaction clicks.
     *
     * @return the reaction click listener
     */
    public ReactionClickListener getReactionClickListener() {
        return reactionClickListener;
    }

    /**
     * Sets the listener for reaction clicks.
     *
     * @param reactionClickListener the reaction click listener
     */
    public void setReactionClickListener(ReactionClickListener reactionClickListener) {
        this.reactionClickListener = reactionClickListener;
    }




    /**
     * Configures the quick reactions by either fetching default reactions or using
     * provided quick reactions. Each reaction is represented as a chip in the view.
     */
    private void configureReactions() {
        List<String> randomQuickReactions = quickReactionItems.isEmpty()
            ? Arrays.asList(Utils.getDefaultReactionsList())
            : quickReactionItems;

        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_margin_2);
        int cardRadius = getContext().getResources().getDimensionPixelSize(R.dimen.cometchat_radius_max);
        int textColor = getContext().getResources().getColor(R.color.cometchat_color_icon_white, getContext().getTheme());
        int textAppearance = CometChatTheme.getTextAppearanceHeading1Regular(getContext());
        int backgroundColor = CometChatTheme.getBackgroundColor3(getContext());

        LinearLayout.LayoutParams layoutParams;

        // Add reaction chips
        for (int i = 0; i < randomQuickReactions.size(); i++) {
            layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.rightMargin = margin;
            if (i == 0) {
                layoutParams.leftMargin = 0;
            } else {
                layoutParams.leftMargin = margin;
            }
            layoutParams.topMargin = 0;
            layoutParams.bottomMargin = 0;

            addReactionChip(randomQuickReactions.get(i), layoutParams, backgroundColor, cardRadius, textColor);
        }

        layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.leftMargin = margin;
        layoutParams.rightMargin = 0;
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;
        // Add reaction icon chip
        addReactionIconChip(layoutParams, backgroundColor, cardRadius);
    }


    /**
     * Adds a reaction chip to the view.
     *
     * @param reaction        the reaction to add
     * @param layoutParams    the layout parameters for the chip
     * @param backgroundColor the background color of the chip
     * @param cardRadius      the corner radius of the chip
     * @param textColor       the text color of the chip
     */
    private void addReactionChip(String reaction,
                                 LinearLayout.LayoutParams layoutParams,
                                 int backgroundColor,
                                 int cardRadius,
                                 int textColor) {
        CometchatQuickReactionViewBinding reactionChipBinding = CometchatQuickReactionViewBinding.inflate(LayoutInflater.from(getContext()));

        Utils.initMaterialCard(reactionChipBinding.cardReactionChip);
        reactionChipBinding.cardReactionChip.setCardBackgroundColor(backgroundColor);
        reactionChipBinding.cardReactionChip.setRadius(cardRadius);

        reactionChipBinding.tvReaction.setText(reaction);
        reactionChipBinding.tvReaction.setTextColor(textColor);
//        reactionChipBinding.tvReaction.setTextAppearance(textAppearance);

        binding.viewReactions.addView(reactionChipBinding.getRoot());
        reactionChipBinding.cardReactionChip.setLayoutParams(layoutParams);
        reactionChipBinding.tvReaction.setOnClickListener(view -> reactionClickListener.onReactionClick(null, reaction));
    }


    private void addReactionIconChip(LinearLayout.LayoutParams layoutParams, int backgroundColor, int cardRadius) {
        CometchatQuickReactionViewBinding addReactionChipBinding = CometchatQuickReactionViewBinding.inflate(LayoutInflater.from(getContext()));
        Utils.initMaterialCard(addReactionChipBinding.cardReactionChip);
        addReactionChipBinding.getRoot().setId(emojiPickerIconViewId);
        addReactionChipBinding.cardReactionChip.setCardBackgroundColor(backgroundColor);
        addReactionChipBinding.cardReactionChip.setRadius(cardRadius);


        addReactionChipBinding.tvReaction.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_add_reaction, null));

        binding.viewReactions.addView(addReactionChipBinding.getRoot());
        addReactionChipBinding.cardReactionChip.setLayoutParams(layoutParams);
        addReactionChipBinding.tvReaction.setOnClickListener(view -> emojiPickerClickListener.onEmojiPickerClick());
    }


    /**
     * Gets the stroke width of the message option sheet.
     *
     * @return the stroke width in pixels
     */
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }


    /**
     * Sets the stroke width for the message option sheet.
     *
     * @param strokeWidth the stroke width in pixels
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        if (binding != null) {
            binding.viewBottomSheet.setStrokeWidth(strokeWidth);
        }
    }


}
