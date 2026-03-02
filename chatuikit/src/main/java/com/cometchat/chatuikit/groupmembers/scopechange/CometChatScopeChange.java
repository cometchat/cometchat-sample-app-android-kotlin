package com.cometchat.chatuikit.groupmembers.scopechange;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatScopeChangeLayoutBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.Arrays;

public class CometChatScopeChange extends MaterialCardView {
    private boolean isDetachedFromWindow;
    private static final String TAG = CometChatScopeChange.class.getSimpleName();
    /**
     * Default role titles used in the layout.
     */
    private final String admin = "Admin";
    private final String moderator = "Moderator";
    private final String participant = "Participant";
    /**
     * View binding for the scope change layout.
     */
    private CometchatScopeChangeLayoutBinding binding;
    /**
     * Adapter for displaying available scopes.
     */
    private ScopeAdapter scopeAdapter;
    /**
     * ViewModel to manage scope change data.
     */
    private ScopeChangeViewModel viewModel;
    /**
     * LifecycleOwner for observer management.
     */
    private LifecycleOwner lifecycleOwner;
    /**
     * Click listeners for negative and positive button actions.
     */
    private OnClick onNegativeButtonClick;
    private OnClick onPositiveButtonClick;
    /**
     * Callback for when the scope change action is successful.
     */
    private CometChat.CallbackListener<GroupMember> scopeChangeCallback;

    /**
     * Text appearances and colors for title and subtitle.
     */
    private @StyleRes int titleTextAppearance;

    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int titleColor;
    private @ColorInt int subtitleColor;

    /**
     * Icon for the scope and its tint color.
     */
    private Drawable scopeIcon;

    private @ColorInt int iconTint;

    /**
     * Background color and dimensions for the layout.
     */
    private @ColorInt int backgroundColor;

    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;

    /**
     * Text appearance and colors for scope items.
     */
    private @ColorInt int itemTextColor;

    private @ColorInt int disableItemTextColor;
    private @StyleRes int itemTextAppearance;

    /**
     * Radio button tints for items.
     */
    private @ColorInt int itemRadioButtonTint;

    private @ColorInt int itemDisableRadioButtonTint;

    /**
     * Color for the drag handle of draggable items.
     */
    private @ColorInt int dragHandleColor;

    /**
     * Colors and text appearances for positive and negative buttons.
     */
    private @ColorInt int positiveButtonTextColor;

    private @ColorInt int negativeButtonTextColor;
    private @StyleRes int buttonTextAppearance;
    private @ColorInt int positiveButtonBackgroundColor;
    private @ColorInt int positiveButtonDisabledBackgroundColor;
    private @ColorInt int negativeButtonBackgroundColor;

    /**
     * General style attribute for the layout.
     */
    private @StyleRes int style;

    /**
     * Constructor for initializing the CometChatScopeChange view.
     *
     * @param context The context associated with the view.
     */
    public CometChatScopeChange(Context context) {
        this(context, null);
    }

    /**
     * Constructor for initializing the CometChatScopeChange view with attributes.
     *
     * @param context The context associated with the view.
     * @param attrs   The attributes defined for the view.
     */
    public CometChatScopeChange(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatScopeChangeStyle);
    }

    /**
     * Constructor for initializing the CometChatScopeChange view with attributes
     * and a style.
     *
     * @param context      The context associated with the view.
     * @param attrs        The attributes defined for the view.
     * @param defStyleAttr The default style attribute for the view.
     */
    public CometChatScopeChange(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view, sets up the layout binding, ViewModel, and click
     * listeners for the buttons.
     *
     * @param attrs        The attribute set containing the XML attributes for the view.
     * @param defStyleAttr The default style attribute to apply to this view.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatScopeChangeLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        viewModel = new ViewModelProvider.NewInstanceFactory().create(ScopeChangeViewModel.class);
        scopeAdapter = new ScopeAdapter(getContext(), null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(scopeAdapter);
        applyStyleAttributes(attrs, defStyleAttr, 0);
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner != null) {
            attachObservers();
        }
        binding.cometchatScopeChangeCancelButton.setOnClickListener(v -> {
            if (onNegativeButtonClick != null) {
                onNegativeButtonClick.onClick();
            }
        });

        binding.cometchatScopeChangeSaveButton.setOnClickListener(v -> {
            if (onPositiveButtonClick != null) {
                onPositiveButtonClick.onClick();
            } else {
                viewModel.changeScope(scopeAdapter.getSelectedRole());
            }
        });

        scopeAdapter.setOnScopeSelectionChangeListener(isSameAsOriginal -> {
            setPositiveButtonEnabled(!isSameAsOriginal);
        });
    }

    public void attachObservers() {
        viewModel.getStates().observe(lifecycleOwner, this::setDialogState);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatScopeChange, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatScopeChange_cometchatScopeChangeStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatScopeChange, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Sets the dialog state based on the provided state.
     *
     * @param state The state of the dialog.
     */
    public void setDialogState(UIKitConstants.DialogState state) {
        switch (state) {
            case INITIATED:
                binding.cometchatScopeChangeSaveText.setVisibility(GONE);
                binding.cometchatScopeChangeSaveProgress.setVisibility(VISIBLE);
                break;
            case SUCCESS:
            case FAILURE:
                binding.cometchatScopeChangeSaveText.setVisibility(VISIBLE);
                binding.cometchatScopeChangeSaveProgress.setVisibility(GONE);
                break;
        }
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatScopeChange_cometchatScopeChangeTitleTextAppearance, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatScopeChange_cometchatScopeChangeSubtitleTextAppearance, 0));
            setTitleColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeTitleColor,
                                              CometChatTheme.getTextColorPrimary(getContext())));
            setSubtitleColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeSubtitleColor,
                                                 CometChatTheme.getTextColorSecondary(getContext())));
            setScopeIcon(typedArray.getDrawable(R.styleable.CometChatScopeChange_cometchatScopeChangeIcon));
            setIconTint(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeIconTint,
                                            CometChatTheme.getPrimaryColor(getContext())));
            setCardBackgroundColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeBackgroundColor,
                                                       CometChatTheme.getBackgroundColor1(getContext())));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatScopeChange_cometchatScopeChangeCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatScopeChange_cometchatScopeChangeStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeStrokeColor,
                                               CometChatTheme.getStrokeColorLight(getContext())));
            setItemTextColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeItemTextColor,
                                                 CometChatTheme.getTextColorPrimary(getContext())));
            setItemTextAppearance(typedArray.getResourceId(R.styleable.CometChatScopeChange_cometchatScopeChangeItemTextAppearance, 0));
            setItemRadioButtonTint(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeItemRadioButtonTint,
                                                       CometChatTheme.getPrimaryColor(getContext())));
            setItemDisableRadioButtonTint(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeItemDisableRadioButtonTint,
                                                              CometChatTheme.getStrokeColorDefault(getContext())));
            setDisableItemTextColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeDisableItemTextColor,
                                                        CometChatTheme.getTextColorSecondary(getContext())));
            setDragHandleColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeDragHandleColor,
                                                   CometChatTheme.getNeutralColor500(getContext())));
            setPositiveButtonTextColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangePositiveButtonTextColor,
                                                           CometChatTheme.getColorWhite(getContext())));
            setNegativeButtonTextColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeNegativeButtonTextColor,
                                                           CometChatTheme.getPrimaryColor(getContext())));
            setButtonTextAppearance(typedArray.getResourceId(R.styleable.CometChatScopeChange_cometchatScopeChangeButtonTextAppearance, 0));
            setPositiveButtonBackgroundColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangePositiveButtonBackgroundColor,
                                                                 CometChatTheme.getPrimaryColor(getContext())));
            setPositiveButtonDisabledBackgroundColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangePositiveButtonDisabledBackgroundColor,
                                                                 CometChatTheme.getNeutralColor300(getContext())));
            setNegativeButtonBackgroundColor(typedArray.getColor(R.styleable.CometChatScopeChange_cometchatScopeChangeNegativeButtonBackgroundColor,
                                                                 CometChatTheme.getBackgroundColor1(getContext())));
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Gets the stroke color as a ColorStateList.
     *
     * @return A ColorStateList representing the stroke color, or null if not set.
     */
    @Nullable
    @Override
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Sets the role data for the group and member.
     *
     * @param group       The group for which the role change is taking place.
     * @param groupMember The member for whom the role is being changed.
     */
    public void setRoleData(Group group, GroupMember groupMember) {
        viewModel.setGroupData(group, groupMember);
        scopeAdapter.setGroupData(new ArrayList<>(Arrays.asList(admin, moderator, participant)), groupMember, group);
    }

    /**
     * Gets the ScopeAdapter instance.
     *
     * @return The current ScopeAdapter instance.
     */
    public ScopeAdapter getScopeAdapter() {
        return scopeAdapter;
    }

    /**
     * Gets the ScopeChangeViewModel instance.
     *
     * @return The current ScopeChangeViewModel instance.
     */
    public ScopeChangeViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the OnClick listener for the negative button.
     *
     * @return The OnClick listener for the negative button.
     */
    public OnClick getOnNegativeButtonClick() {
        return onNegativeButtonClick;
    }

    /**
     * Sets the click listener for the negative button.
     *
     * @param onNegativeButtonClick The click listener for the negative button.
     */
    public void setOnNegativeButtonClick(OnClick onNegativeButtonClick) {
        this.onNegativeButtonClick = onNegativeButtonClick;
    }

    /**
     * Gets the OnClick listener for the positive button.
     *
     * @return The OnClick listener for the positive button.
     */
    public OnClick getOnPositiveButtonClick() {
        return onPositiveButtonClick;
    }

    /**
     * Sets the click listener for the positive button.
     *
     * @param onPositiveButtonClick The click listener for the positive button.
     */
    public void setOnPositiveButtonClick(OnClick onPositiveButtonClick) {
        this.onPositiveButtonClick = onPositiveButtonClick;
    }    @Override
    public void setCardBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Gets the admin role string.
     *
     * @return The string representing the admin role.
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * Gets the moderator role string.
     *
     * @return The string representing the moderator role.
     */
    public String getModerator() {
        return moderator;
    }

    /**
     * Gets the participant role string.
     *
     * @return The string representing the participant role.
     */
    public String getParticipant() {
        return participant;
    }    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Gets the scope change callback listener.
     *
     * @return The CallbackListener for scope changes.
     */
    public CometChat.CallbackListener<GroupMember> getScopeChangeCallback() {
        return scopeChangeCallback;
    }

    /**
     * Sets the scope change callback.
     *
     * @param scopeChangeCallback The callback listener for scope changes.
     */
    public void setScopeChangeCallback(CometChat.CallbackListener<GroupMember> scopeChangeCallback) {
        if (scopeChangeCallback != null) {
            this.scopeChangeCallback = scopeChangeCallback;
            viewModel.setScopeChangeCallback(scopeChangeCallback);
        }
    }

    /**
     * Gets the title text appearance style.
     *
     * @return The style resource ID for the title text appearance.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the title text appearance.
     *
     * @param titleTextAppearance The style resource for the title text appearance.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.cometchatScopeChangeTitle.setTextAppearance(titleTextAppearance);
    }

    /**
     * Gets the subtitle text appearance style.
     *
     * @return The style resource ID for the subtitle text appearance.
     */
    public int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the subtitle text appearance.
     *
     * @param subtitleTextAppearance The style resource for the subtitle text appearance.
     */
    public void setSubtitleTextAppearance(@StyleRes int subtitleTextAppearance) {
        this.subtitleTextAppearance = subtitleTextAppearance;
        binding.cometchatScopeChangeSubtitle.setTextAppearance(subtitleTextAppearance);
    }

    /**
     * Gets the title color.
     *
     * @return The color of the title text.
     */
    public int getTitleColor() {
        return titleColor;
    }

    /**
     * Sets the title color.
     *
     * @param titleColor The color of the title text.
     */
    public void setTitleColor(@ColorInt int titleColor) {
        this.titleColor = titleColor;
        binding.cometchatScopeChangeTitle.setTextColor(titleColor);
    }

    /**
     * Gets the subtitle color.
     *
     * @return The color of the subtitle text.
     */
    public int getSubtitleColor() {
        return subtitleColor;
    }

    /**
     * Sets the subtitle color.
     *
     * @param subtitleColor The color of the subtitle text.
     */
    public void setSubtitleColor(@ColorInt int subtitleColor) {
        this.subtitleColor = subtitleColor;
        binding.cometchatScopeChangeSubtitle.setTextColor(subtitleColor);
    }

    /**
     * Gets the scope icon drawable.
     *
     * @return The drawable used for the scope icon.
     */
    public Drawable getScopeIcon() {
        return scopeIcon;
    }

    /**
     * Sets the icon for the scope.
     *
     * @param scopeIcon The drawable for the scope icon.
     */
    public void setScopeIcon(Drawable scopeIcon) {
        this.scopeIcon = scopeIcon;
        binding.ivScopeChange.setImageDrawable(scopeIcon);
    }

    /**
     * Gets the icon tint color.
     *
     * @return The tint color applied to the icon.
     */
    public int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the tint for the icon.
     *
     * @param iconTint The color tint for the icon.
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
        binding.ivScopeChange.setColorFilter(iconTint);
    }

    /**
     * Gets the background color of the card.
     *
     * @return The background color of the card.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Gets the corner radius of the card.
     *
     * @return The corner radius of the card.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius of the card.
     *
     * @param cornerRadius The corner radius value in pixels.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0)
            .build();
        super.setShapeAppearanceModel(shapeAppearanceModel);
    }

    /**
     * Gets the text color for the item.
     *
     * @return The color of the item text.
     */
    public int getItemTextColor() {
        return itemTextColor;
    }

    /**
     * Sets the text color for the item.
     *
     * @param itemTextColor The color of the item text.
     */
    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
        scopeAdapter.setItemTextColor(itemTextColor);
    }

    /**
     * Gets the text color for the disabled item.
     *
     * @return The color of the disabled item text.
     */
    public int getDisableItemTextColor() {
        return disableItemTextColor;
    }

    /**
     * Sets the disabled item text color.
     *
     * @param disableItemTextColor The color of the disabled item text.
     */
    public void setDisableItemTextColor(int disableItemTextColor) {
        this.disableItemTextColor = disableItemTextColor;
        scopeAdapter.setDisableItemTextColor(disableItemTextColor);
    }

    /**
     * Gets the text appearance style for the item.
     *
     * @return The style resource ID for the item text appearance.
     */
    public int getItemTextAppearance() {
        return itemTextAppearance;
    }

    /**
     * Sets the text appearance for the item.
     *
     * @param itemTextAppearance The style resource for the item text appearance.
     */
    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
        scopeAdapter.setItemTextAppearance(itemTextAppearance);
    }

    /**
     * Gets the tint color for the item radio button.
     *
     * @return The tint color for the item radio button.
     */
    public int getItemRadioButtonTint() {
        return itemRadioButtonTint;
    }

    /**
     * Sets the tint for the item radio button.
     *
     * @param itemRadioButtonTint The color tint for the item radio button.
     */
    public void setItemRadioButtonTint(@ColorInt int itemRadioButtonTint) {
        this.itemRadioButtonTint = itemRadioButtonTint;
        scopeAdapter.setRadioButtonTint(itemRadioButtonTint);
    }

    /**
     * Gets the tint color for the disabled item radio button.
     *
     * @return The tint color for the disabled item radio button.
     */
    public int getItemDisableRadioButtonTint() {
        return itemDisableRadioButtonTint;
    }

    /**
     * Sets the tint for the disabled item radio button.
     *
     * @param itemDisableRadioButtonTint The color tint for the disabled item radio button.
     */
    public void setItemDisableRadioButtonTint(@ColorInt int itemDisableRadioButtonTint) {
        this.itemDisableRadioButtonTint = itemDisableRadioButtonTint;
        scopeAdapter.setDisableRadioButtonTint(itemDisableRadioButtonTint);
    }

    /**
     * Gets the drag handle color.
     *
     * @return The color of the drag handle.
     */
    public int getDragHandleColor() {
        return dragHandleColor;
    }

    /**
     * Sets the color for the drag handle.
     *
     * @param dragHandleColor The color of the drag handle.
     */
    public void setDragHandleColor(@ColorInt int dragHandleColor) {
        this.dragHandleColor = dragHandleColor;
        binding.cometchatScopeChangeDragHandle.setCardBackgroundColor(dragHandleColor);
    }

    /**
     * Gets the text color for the positive button.
     *
     * @return The color of the positive button text.
     */
    public int getPositiveButtonTextColor() {
        return positiveButtonTextColor;
    }

    /**
     * Sets the positive button text color.
     *
     * @param positiveButtonTextColor The color of the positive button text.
     */
    public void setPositiveButtonTextColor(@ColorInt int positiveButtonTextColor) {
        this.positiveButtonTextColor = positiveButtonTextColor;
        binding.cometchatScopeChangeSaveText.setTextColor(positiveButtonTextColor);
    }

    /**
     * Gets the text color for the negative button.
     *
     * @return The color of the negative button text.
     */
    public int getNegativeButtonTextColor() {
        return negativeButtonTextColor;
    }

    /**
     * Sets the negative button text color.
     *
     * @param negativeButtonTextColor The color of the negative button text.
     */
    public void setNegativeButtonTextColor(@ColorInt int negativeButtonTextColor) {
        this.negativeButtonTextColor = negativeButtonTextColor;
        binding.cometchatScopeChangeCancelText.setTextColor(negativeButtonTextColor);
    }

    /**
     * Gets the button text appearance style.
     *
     * @return The style resource ID for the button text appearance.
     */
    public int getButtonTextAppearance() {
        return buttonTextAppearance;
    }    /**
     * Gets the stroke width of the card.
     *
     * @return The stroke width of the card.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the text appearance for buttons.
     *
     * @param buttonTextAppearance The style resource for button text appearance.
     */
    public void setButtonTextAppearance(@StyleRes int buttonTextAppearance) {
        this.buttonTextAppearance = buttonTextAppearance;
        binding.cometchatScopeChangeCancelText.setTextAppearance(buttonTextAppearance);
        binding.cometchatScopeChangeSaveText.setTextAppearance(buttonTextAppearance);
    }

    /**
     * Gets the background color for the positive button.
     *
     * @return The background color of the positive button.
     */
    public int getPositiveButtonBackgroundColor() {
        return positiveButtonBackgroundColor;
    }

    /**
     * Sets the background color for the positive button.
     *
     * @param positiveButtonBackgroundColor The background color of the positive button.
     */
    public void setPositiveButtonBackgroundColor(@ColorInt int positiveButtonBackgroundColor) {
        this.positiveButtonBackgroundColor = positiveButtonBackgroundColor;
        binding.cometchatScopeChangeSaveButton.setCardBackgroundColor(positiveButtonBackgroundColor);
    }

    public void setPositiveButtonDisabledBackgroundColor(@ColorInt int color) {
        this.positiveButtonDisabledBackgroundColor = color;
    }

    /**
     * Gets the background color for the negative button.
     *
     * @return The background color of the negative button.
     */
    public int getNegativeButtonBackgroundColor() {
        return negativeButtonBackgroundColor;
    }

    /**
     * Sets the background color for the negative button.
     *
     * @param negativeButtonBackgroundColor The background color of the negative button.
     */
    public void setNegativeButtonBackgroundColor(@ColorInt int negativeButtonBackgroundColor) {
        this.negativeButtonBackgroundColor = negativeButtonBackgroundColor;
        binding.cometchatScopeChangeCancelButton.setCardBackgroundColor(negativeButtonBackgroundColor);
    }

    /**
     * Enables or disables the positive (save) button.
     *
     * @param enabled true to enable the button, false to disable it.
     */
    public void setPositiveButtonEnabled(boolean enabled) {
        binding.cometchatScopeChangeSaveButton.setEnabled(enabled);
        binding.cometchatScopeChangeSaveButton.setCardBackgroundColor(
                enabled ? positiveButtonBackgroundColor : positiveButtonDisabledBackgroundColor
        );
    }

    /**
     * Gets the style attribute.
     *
     * @return The style attribute.
     */
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatScopeChange);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            attachObservers();
            isDetachedFromWindow = false;
        }
    }

    /**
     * Called when the view is detached from a window. Removes observers.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposeObservers();
        isDetachedFromWindow = true;
    }

    public void disposeObservers() {
        if (lifecycleOwner != null && viewModel != null) {
            viewModel.getStates().removeObservers(lifecycleOwner);
        }
    }
}