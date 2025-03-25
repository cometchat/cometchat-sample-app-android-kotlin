package com.cometchat.chatuikit.messageinformation;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMessageInformationBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.messagereceipt.Receipt;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.List;

/**
 * {@code CometChatMessageInformation} is a custom implementation of
 * {@link BottomSheetDialogFragment} that displays detailed information about a
 * message in a bottom sheet format.
 *
 * <p>
 * This class allows developers to show message-specific actions or details in a
 * modal bottom sheet, following the Material Design guidelines. The view can be
 * customized with various setters for different UI components, such as message
 * text, reactions, and more.
 *
 * <h3>Customization:</h3>
 * <p>
 * The bottom sheet can be customized with attributes like background color,
 * stroke width, and corner radius etc.
 *
 * <h3>See Also:</h3>
 * <p>
 * {@link BottomSheetDialogFragment}
 */
public class CometChatMessageInformation extends BottomSheetDialogFragment {
    private static final String TAG = CometChatMessageInformation.class.getSimpleName();

    private Context context;
    private CometchatMessageInformationBinding binding;

    private BaseMessage message;
    private String toolBarTitleText;
    private String conversationType;
    private boolean hideToolBar = false;
    private Function2<Context, BaseMessage, View> bubbleView;
    private BottomSheetListener bottomSheetListener;

    private MessageInformationAdapter adapter;
    private CometChatMessageTemplate template;
    private MessageInformationViewModel messageInformationViewModel;

    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleTextColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int backgroundHighlightColor;
    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @StyleRes int itemNameTextAppearance;
    private @ColorInt int itemNameTextColor;
    private @StyleRes int itemReadTextAppearance;
    private @ColorInt int itemReadTextColor;
    private @StyleRes int itemReadDateTextAppearance;
    private @ColorInt int itemReadDateTextColor;
    private @StyleRes int itemDeliveredTextAppearance;
    private @ColorInt int itemDeliveredTextColor;
    private @StyleRes int itemDeliveredDateTextAppearance;
    private @ColorInt int itemDeliveredDateTextColor;
    private @StyleRes int itemAvatarStyle;
    private @StyleRes int messageReceipt;

    /**
     * Default constructor for {@code CometChatMessageInformation}.
     *
     * <p>
     * This constructor is used to initialize an instance of
     * {@code CometChatMessageInformation}, which is a custom bottom sheet dialog
     * fragment that displays message details.
     */
    public CometChatMessageInformation() {
    }

    /**
     * Initializes the {@code CometChatMessageInformation} with the provided context
     * and message.
     *
     * <p>
     * This method sets up the necessary UI components and inflates the layout for
     * displaying detailed message information. It also configures the RecyclerView,
     * sets up the view model, and applies custom style attributes.
     *
     * @param context The {@link Context} in which this bottom sheet is shown.
     * @param message The {@link BaseMessage} object that contains the message details.
     */
    public void init(@NonNull Context context, @NonNull BaseMessage message) {
        this.context = context;
        this.message = message;
        conversationType = message
            .getReceiverType()
            .equals(UIKitConstants.ConversationType.USERS) ? UIKitConstants.ConversationType.USERS : UIKitConstants.ConversationType.GROUPS;

        // Inflate the view
        binding = CometchatMessageInformationBinding.inflate(LayoutInflater.from(context));

        // Set up RecyclerView
        adapter = new MessageInformationAdapter(context, message);
        binding.messageInfoRecyclerViewGroup.setAdapter(adapter);

        // Set up the view model
        initViewModel();

        // Customize the UI with attributes
        applyStyleAttributes();

        // Set the message receipt information if conversation type is user
        if (conversationType.equals(UIKitConstants.ConversationType.USERS)) {
            binding.messageReceiptRead.setMessageReceipt(Receipt.READ);
            binding.messageReceiptDelivered.setMessageReceipt(Receipt.DELIVERED);

            if (message.getReadAt() != 0) {
                binding.tvReadReceiptTimeStampUser.setText(Utils.getDateTimeMessageInformation(message.getReadAt() * 1000));
            }

            if (message.getDeliveredAt() != 0) {
                binding.tvDeliveredReceiptTimeStampUser.setText(Utils.getDateTimeMessageInformation(message.getDeliveredAt() * 1000));
            }
        }
    }

    /**
     * Initializes the {@code MessageInformationViewModel} and sets up observers for
     * various live data components, such as message receipt updates, state changes,
     * and exceptions.
     *
     * <p>
     * This method also binds the ViewModel to the message object and listens for
     * changes to update the UI dynamically based on the message information.
     */
    private void initViewModel() {
        messageInformationViewModel = new ViewModelProvider.NewInstanceFactory().create(MessageInformationViewModel.class);
        messageInformationViewModel.addListener();
        messageInformationViewModel.getLiveListData().observe((LifecycleOwner) context, this::setList);
        messageInformationViewModel.updateReceipt().observe((LifecycleOwner) context, this::notifyUpdateReceipt);
        messageInformationViewModel.addReceipt().observe((LifecycleOwner) context, this::notifyAddReceipt);
        messageInformationViewModel.exceptionMutableLiveData().observe((LifecycleOwner) context, this::showError);
        messageInformationViewModel.clearList().observe((LifecycleOwner) context, this::clear);
        messageInformationViewModel.getState().observe((LifecycleOwner) context, this::stateChangeObserver);
        messageInformationViewModel.setMessage(message);
    }

    /**
     * Applies the default style attributes for {@code CometChatMessageInformation}.
     *
     * <p>
     * This method is responsible for retrieving the default attributes defined in
     * the XML styleable and applying them to the view, including background color,
     * corner radius, and text appearance.
     */
    private void applyStyleAttributes() {
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.CometChatMessageInformation);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Sets the list of message receipts in the adapter if the provided list is not
     * null or empty.
     *
     * @param messageReceipts The list of message receipts to set.
     */
    private void setList(List<MessageReceipt> messageReceipts) {
        if (messageReceipts != null && !messageReceipts.isEmpty()) {
            adapter.setMessageReceipts(messageReceipts);
        }
    }

    /**
     * Notifies the adapter to update a receipt at the specified index.
     *
     * @param index The index of the receipt to update.
     */
    private void notifyUpdateReceipt(int index) {
        adapter.notifyItemChanged(index);
    }

    /**
     * Notifies the adapter to insert a new receipt at the specified position.
     *
     * @param integer The position where the new receipt should be inserted.
     */
    private void notifyAddReceipt(Integer integer) {
        adapter.notifyItemInserted(integer);
    }

    /**
     * Displays an error state when a CometChatException is encountered.
     *
     * @param exception The exception to handle.
     */
    private void showError(CometChatException exception) {
        if (exception != null) {
            handleErrorState();
        }
    }

    /**
     * Clears the adapter's data and refreshes the view.
     *
     * @param unused Unused parameter.
     */
    public void clear(Void unused) {
        adapter.notifyDataSetChanged();
    }

    /**
     * Observes state changes and triggers the appropriate handler.
     *
     * @param states The current state of the component.
     */
    private void stateChangeObserver(UIKitConstants.States states) {
        if (UIKitConstants.States.LOADING.equals(states)) {
            handleLoadingState();
        } else if (UIKitConstants.States.LOADED.equals(states)) {
            handleLoadedState();
        } else if (UIKitConstants.States.EMPTY.equals(states)) {
            handleEmptyState();
        }
    }

    /**
     * Extracts the style attributes from the given {@link TypedArray} and applies
     * default values if the attributes are not defined.
     *
     * <p>
     * This method is responsible for retrieving custom attributes like title text
     * appearance, background color, corner radius, and more, then applying these
     * values to the UI elements.
     *
     * @param typedArray The {@link TypedArray} containing the style attributes to be
     *                   extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationTitleTextAppearance, 0);
            titleTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationTitleTextColor, 0);
            backgroundColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationBackgroundColor, 0);
            backgroundHighlightColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationBackgroundHighlightColor,
                                                           0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageInformation_cometchatMessageInformationCornerRadius, 0);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageInformation_cometchatMessageInformationStrokeWidth, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationStrokeColor, 0);
            itemNameTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemNameTextAppearance,
                                                              0);
            itemNameTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemNameTextColor, 0);
            itemReadTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadTextAppearance,
                                                              0);
            itemReadTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadTextColor, 0);
            itemReadDateTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadDateTextAppearance,
                                                                  0);
            itemReadDateTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadDateTextColor, 0);
            itemDeliveredTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredTextAppearance,
                                                                   0);
            itemDeliveredTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredTextColor,
                                                         0);
            itemDeliveredDateTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredDateTextAppearance,
                                                                       0);
            itemDeliveredDateTextColor = typedArray.getColor(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredDateTextColor,
                                                             0);
            itemAvatarStyle = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationItemAvatarStyle, 0);
            messageReceipt = typedArray.getResourceId(R.styleable.CometChatMessageInformation_cometchatMessageInformationMessageReceiptStyle, 0);
            // Apply default styles
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Handles the error state by invoking the empty state handler.
     */
    private void handleErrorState() {
        handleEmptyState();
    }

    /**
     * Handles the loading state by starting the shimmer effect and hiding other
     * views.
     */
    private void handleLoadingState() {
        CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.shimmer_cometchat_message_information);
        binding.shimmerRecyclerview.setAdapter(adapter);
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context));
        binding.shimmerEffectFrame.startShimmer();
        binding.messageInfoRecyclerViewGroup.setVisibility(View.GONE);
        binding.messageReceiptsUser.setVisibility(View.GONE);
        binding.shimmerEffectFrame.setVisibility(View.VISIBLE);
    }

    /**
     * Handles the loaded state by stopping the shimmer effect and displaying the
     * relevant views.
     */
    private void handleLoadedState() {
        binding.shimmerEffectFrame.stopShimmer();
        binding.messageInfoRecyclerViewGroup.setVisibility(conversationType.equals(UIKitConstants.ConversationType.USERS) ? View.GONE : View.VISIBLE);
        binding.messageReceiptsUser.setVisibility(conversationType.equals(UIKitConstants.ConversationType.USERS) ? View.VISIBLE : View.GONE);
        binding.shimmerEffectFrame.setVisibility(View.GONE);
    }

    /**
     * Handles the empty state by stopping the shimmer effect and updating the
     * visibility of views.
     */
    private void handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer();
        binding.messageInfoRecyclerViewGroup.setVisibility(View.GONE);
        binding.messageReceiptsUser.setVisibility(conversationType.equals(UIKitConstants.ConversationType.USERS) ? View.VISIBLE : View.GONE);
        binding.shimmerEffectFrame.setVisibility(View.GONE);
    }

    /**
     * Updates the UI components with the extracted style attributes.
     *
     * <p>
     * This method applies the various style attributes such as text appearance,
     * background color, and corner radius to the relevant UI components of the
     * message information view.
     */
    private void updateUI() {
        setTitleTextAppearance(titleTextAppearance);
        setTitleTextColor(titleTextColor);
        setBackgroundColor(backgroundColor);
        setBackgroundHighlightColor(backgroundHighlightColor);
        setCornerRadius(cornerRadius);
        setStrokeWidth(strokeWidth);
        setStrokeColor(strokeColor);
        setItemNameTextAppearance(itemNameTextAppearance);
        setItemNameTextColor(itemNameTextColor);
        setItemReadTextAppearance(itemReadTextAppearance);
        setItemReadTextColor(itemReadTextColor);
        setItemReadDateTextAppearance(itemReadDateTextAppearance);
        setItemReadDateTextColor(itemReadDateTextColor);
        setItemDeliveredTextAppearance(itemDeliveredTextAppearance);
        setItemDeliveredTextColor(itemDeliveredTextColor);
        setItemDeliveredDateTextAppearance(itemDeliveredDateTextAppearance);
        setItemDeliveredDateTextColor(itemDeliveredDateTextColor);
        setItemAvatarStyle(itemAvatarStyle);
        setMessageReceipt(messageReceipt);
    }

    /**
     * Sets the stroke color for the layout.
     *
     * @param strokeColor The stroke color to apply.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        binding.parentLayout.setStrokeColor(ColorStateList.valueOf(strokeColor));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = CometchatMessageInformationBinding.inflate(inflater, container, false);
        }
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setFitToContents(false);
                bottomSheet.setBackgroundResource(R.color.cometchat_color_transparent);
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetListener.onDismiss();
                            dismiss();
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (messageInformationViewModel != null) {
            messageInformationViewModel.removeListener();
        }
    }

    /**
     * Sets the custom style for {@code CometChatMessageInformation} using the
     * provided style resource.
     *
     * <p>
     * This method applies the style attributes to customize the appearance of the
     * message information view, such as title text appearance, background color,
     * stroke width, and corner radius.
     *
     * @param style The resource ID of the style to be applied.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = context.obtainStyledAttributes(style, R.styleable.CometChatMessageInformation);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the resource ID for the title text appearance style.
     *
     * @return The resource ID of the title text appearance.
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the title text appearance for the toolbar.
     *
     * @param titleTextAppearance The style resource for title text appearance.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.toolBarTitle.setTextAppearance(titleTextAppearance);
    }

    /**
     * Retrieves the title text color.
     *
     * @return The color of the title text.
     */
    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the title text color for the toolbar.
     *
     * @param titleTextColor The color for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.toolBarTitle.setTextColor(titleTextColor);
    }

    /**
     * Retrieves the background color of the layout.
     *
     * @return The background color of the layout.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the layout.
     *
     * @param backgroundColor The background color to apply.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        binding.parentLayout.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Retrieves the background highlight color.
     *
     * @return The background highlight color.
     */
    public @ColorInt int getBackgroundHighlightColor() {
        return backgroundHighlightColor;
    }

    /**
     * Sets the background highlight color.
     *
     * @param backgroundHighlightColor The highlight color to apply.
     */
    public void setBackgroundHighlightColor(@ColorInt int backgroundHighlightColor) {
        this.backgroundHighlightColor = backgroundHighlightColor;
        binding.messageBubbleView.setBackgroundColor(backgroundHighlightColor);
    }

    /**
     * Retrieves the corner radius of the layout.
     *
     * @return The corner radius value.
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the layout.
     *
     * @param cornerRadius The radius for the corners.
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
        binding.parentLayout.setShapeAppearanceModel(shapeAppearanceModel);
    }

    /**
     * Retrieves the stroke width of the layout.
     *
     * @return The stroke width.
     */
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the stroke width for the layout.
     *
     * @param strokeWidth The stroke width to apply.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        binding.parentLayout.setStrokeWidth(strokeWidth);
    }

    /**
     * Retrieves the stroke color of the layout.
     *
     * @return The stroke color.
     */
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Retrieves the item name text appearance.
     *
     * @return The text appearance for the item name.
     */
    public @StyleRes int getItemNameTextAppearance() {
        return itemNameTextAppearance;
    }

    /**
     * Sets the item name text appearance.
     *
     * @param itemNameTextAppearance The text appearance for the item name.
     */
    public void setItemNameTextAppearance(@StyleRes int itemNameTextAppearance) {
        this.itemNameTextAppearance = itemNameTextAppearance;
        if (conversationType.equals(UIKitConstants.ConversationType.USERS)) {
            binding.tvReadReceiptUser.setTextAppearance(itemNameTextAppearance);
            binding.tvDeliveredReceiptUser.setTextAppearance(itemNameTextAppearance);
        } else {
            adapter.setItemNameTextAppearance(itemNameTextAppearance);
        }
    }

    /**
     * Retrieves the item name text color.
     *
     * @return The text color of the item name.
     */
    public @ColorInt int getItemNameTextColor() {
        return itemNameTextColor;
    }

    /**
     * Sets the item name text color.
     *
     * @param itemNameTextColor The text color for the item name.
     */
    public void setItemNameTextColor(@ColorInt int itemNameTextColor) {
        this.itemNameTextColor = itemNameTextColor;
        if (conversationType.equals(UIKitConstants.ConversationType.USERS)) {
            binding.tvReadReceiptUser.setTextColor(itemNameTextColor);
            binding.tvDeliveredReceiptUser.setTextColor(itemNameTextColor);
        } else {
            adapter.setItemNameTextColor(itemNameTextColor);
        }
    }

    /**
     * Retrieves the item read text appearance.
     *
     * @return The text appearance for the read receipt.
     */
    public @StyleRes int getItemReadTextAppearance() {
        return itemReadTextAppearance;
    }

    /**
     * Sets the item read text appearance.
     *
     * @param itemReadTextAppearance The text appearance for the read receipt.
     */
    public void setItemReadTextAppearance(@StyleRes int itemReadTextAppearance) {
        this.itemReadTextAppearance = itemReadTextAppearance;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemReadTextAppearance(itemReadTextAppearance);
        }
    }

    /**
     * Retrieves the item read text color.
     *
     * @return The text color of the read receipt.
     */
    public @ColorInt int getItemReadTextColor() {
        return itemReadTextColor;
    }

    /**
     * Sets the item read text color.
     *
     * @param itemReadTextColor The text color for the read receipt.
     */
    public void setItemReadTextColor(@ColorInt int itemReadTextColor) {
        this.itemReadTextColor = itemReadTextColor;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemReadTextColor(itemReadTextColor);
        }
    }

    /**
     * Retrieves the item read date text appearance.
     *
     * @return The text appearance for the read date.
     */
    public @StyleRes int getItemReadDateTextAppearance() {
        return itemReadDateTextAppearance;
    }

    /**
     * Sets the item read date text appearance.
     *
     * @param itemReadDateTextAppearance The text appearance for the read date.
     */
    public void setItemReadDateTextAppearance(@StyleRes int itemReadDateTextAppearance) {
        this.itemReadDateTextAppearance = itemReadDateTextAppearance;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemReadDateTextAppearance(itemReadDateTextAppearance);
        }
    }

    /**
     * Retrieves the item read date text color.
     *
     * @return The text color of the read date.
     */
    public @ColorInt int getItemReadDateTextColor() {
        return itemReadDateTextColor;
    }

    /**
     * Sets the item read date text color.
     *
     * @param itemReadDateTextColor The text color for the read date.
     */
    public void setItemReadDateTextColor(@ColorInt int itemReadDateTextColor) {
        this.itemReadDateTextColor = itemReadDateTextColor;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemReadDateTextColor(itemReadDateTextColor);
        }
    }

    /**
     * Retrieves the item delivered text appearance.
     *
     * @return The text appearance for the delivered receipt.
     */
    public @StyleRes int getItemDeliveredTextAppearance() {
        return itemDeliveredTextAppearance;
    }

    /**
     * Sets the item delivered text appearance.
     *
     * @param itemDeliveredTextAppearance The text appearance for the delivered receipt.
     */
    public void setItemDeliveredTextAppearance(@StyleRes int itemDeliveredTextAppearance) {
        this.itemDeliveredTextAppearance = itemDeliveredTextAppearance;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemDeliveredTextAppearance(itemDeliveredTextAppearance);
        }
    }

    /**
     * Retrieves the item delivered text color.
     *
     * @return The text color of the delivered receipt.
     */
    public @ColorInt int getItemDeliveredTextColor() {
        return itemDeliveredTextColor;
    }

    /**
     * Sets the item delivered text color.
     *
     * @param itemDeliveredTextColor The text color for the delivered receipt.
     */
    public void setItemDeliveredTextColor(@ColorInt int itemDeliveredTextColor) {
        this.itemDeliveredTextColor = itemDeliveredTextColor;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemDeliveredTextColor(itemDeliveredTextColor);
        }
    }

    /**
     * Retrieves the item delivered date text appearance.
     *
     * @return The text appearance for the delivered date.
     */
    public @StyleRes int getItemDeliveredDateTextAppearance() {
        return itemDeliveredDateTextAppearance;
    }

    /**
     * Sets the item delivered date text appearance.
     *
     * @param itemDeliveredDateTextAppearance The text appearance for the delivered date.
     */
    public void setItemDeliveredDateTextAppearance(@StyleRes int itemDeliveredDateTextAppearance) {
        this.itemDeliveredDateTextAppearance = itemDeliveredDateTextAppearance;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemDeliveredDateTextAppearance(itemDeliveredDateTextAppearance);
        }
    }

    /**
     * Retrieves the item delivered date text color.
     *
     * @return The text color of the delivered date.
     */
    public @ColorInt int getItemDeliveredDateTextColor() {
        return itemDeliveredDateTextColor;
    }

    /**
     * Sets the item delivered date text color.
     *
     * @param itemDeliveredDateTextColor The text color for the delivered date.
     */
    public void setItemDeliveredDateTextColor(@ColorInt int itemDeliveredDateTextColor) {
        this.itemDeliveredDateTextColor = itemDeliveredDateTextColor;
        if (conversationType.equals(UIKitConstants.ConversationType.GROUPS)) {
            adapter.setItemDeliveredDateTextColor(itemDeliveredDateTextColor);
        }
    }

    /**
     * Retrieves the item avatar style.
     *
     * @return The style resource for the item avatar.
     */
    public @StyleRes int getItemAvatarStyle() {
        return itemAvatarStyle;
    }

    /**
     * Sets the item avatar style.
     *
     * @param itemAvatarStyle The style resource for the item avatar.
     */
    public void setItemAvatarStyle(@StyleRes int itemAvatarStyle) {
        this.itemAvatarStyle = itemAvatarStyle;
        adapter.setItemAvatarStyle(itemAvatarStyle);
    }

    /**
     * Retrieves the message receipt style.
     *
     * @return The style resource for the message receipt.
     */
    public @StyleRes int getMessageReceipt() {
        return messageReceipt;
    }

    /**
     * Sets the message receipt style.
     *
     * @param messageReceipt The style resource for the message receipt.
     */
    public void setMessageReceipt(@StyleRes int messageReceipt) {
        this.messageReceipt = messageReceipt;
        binding.messageReceiptRead.setStyle(messageReceipt);
        binding.messageReceiptDelivered.setStyle(messageReceipt);
    }

    /**
     * Retrieves the message object.
     *
     * @return The {@link BaseMessage} object.
     */
    public BaseMessage getMessage() {
        return message;
    }

    /**
     * Retrieves the toolbar title text.
     *
     * @return The toolbar title text.
     */
    public String getToolBarTitleText() {
        return toolBarTitleText;
    }

    /**
     * Sets the toolbar title text.
     *
     * @param toolBarTitleText The title text to set in the toolbar.
     */
    public void setToolBarTitleText(String toolBarTitleText) {
        this.toolBarTitleText = toolBarTitleText;
        binding.toolBarTitle.setText(toolBarTitleText);
    }

    /**
     * Checks if the toolbar is hidden.
     *
     * @return True if the toolbar is hidden, otherwise false.
     */
    public boolean isHideToolBar() {
        return hideToolBar;
    }

    /**
     * Sets whether the toolbar should be hidden.
     *
     * @param hideToolBar True to hide the toolbar, false to show it.
     */
    public void setHideToolBar(boolean hideToolBar) {
        this.hideToolBar = hideToolBar;
        binding.toolBarView.setVisibility(hideToolBar ? View.GONE : View.VISIBLE);
    }

    /**
     * Sets the bubble view for the message.
     *
     * @param bubbleView A function that creates the message bubble UI.
     */
    public void setBubbleView(@NonNull Function2<Context, BaseMessage, View> bubbleView) {
        this.bubbleView = bubbleView;
        // Set up the message bubble UI
        setBubbleView();
    }

    /**
     * Configures the message bubble view. If the message and template are not null
     * and bubbleView is not initialized, it clears the message bubble container and
     * sets up a new bubble view. If bubbleView is already initialized, it applies
     * the bubble view to the message and adjusts the overlay size to match the
     * parent layout.
     */
    private void setBubbleView() {
        if (message != null && template != null && bubbleView == null) {
            binding.messageBubbleView.removeAllViews();
        } else if (bubbleView != null) {
            binding.messageBubbleView.removeAllViews();
            View view = bubbleView.apply(context, message);
            binding.messageBubbleView.addView(view);
            // Set the size of messageBubbleViewOverlay to match messageBubbleParentLayout
            binding.messageBubbleParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    binding.messageBubbleParentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = binding.messageBubbleParentLayout.getWidth();
                    int height = binding.messageBubbleParentLayout.getHeight();
                    ViewGroup.LayoutParams params = binding.messageBubbleViewOverlay.getLayoutParams();
                    params.width = width;
                    params.height = height;
                    binding.messageBubbleViewOverlay.setLayoutParams(params);
                }
            });
            binding.messageBubbleViewOverlay.setOnClickListener(v -> {
            }); // To block inner child touch
        } else {
            Log.e(TAG, "Error: setBubbleView: missing input parameters BaseMessage and Template or bubbleView");
        }
    }

    /**
     * Retrieves the message template.
     *
     * @return The CometChatMessageTemplate used for displaying the message.
     */
    public CometChatMessageTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the message template for displaying message information.
     *
     * @param template The CometChatMessageTemplate to use for message information.
     */
    public void setTemplate(@Nullable CometChatMessageTemplate template) {
        if (template != null) {
            this.template = template;
        }
    }

    /**
     * Retrieves the BottomSheetListener.
     *
     * @return The BottomSheetListener for the bottom sheet interactions.
     */
    public BottomSheetListener getBottomSheetListener() {
        return bottomSheetListener;
    }

    /**
     * Sets the BottomSheetListener for the bottom sheet interactions.
     *
     * @param bottomSheetListener The listener to handle bottom sheet events.
     */
    public void setBottomSheetListener(BottomSheetListener bottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener;
    }

    /**
     * Interface definition for a callback to be invoked when the bottom sheet is
     * dismissed.
     */
    public interface BottomSheetListener {
        /**
         * Called when the bottom sheet is dismissed.
         */
        void onDismiss();
    }
}
