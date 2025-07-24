package com.cometchat.chatuikit.extensions.polls;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatCreatePollBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;

import java.util.List;

public class CometChatCreatePoll extends MaterialCardView {
    private static final String TAG = CometChatCreatePoll.class.getSimpleName();
    private final int progressVisibility = View.GONE;
    /**
     * UI components and their states.
     */
    private CometchatCreatePollBinding binding;
    private PollOptionsAdapter adapter;
    /**
     * Initializes an ItemTouchHelper to handle drag-and-drop functionality in the
     * RecyclerView.
     */
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

        /**
         * Defines the movement directions allowed for dragging and swiping.
         *
         * @param recyclerView
         *            The RecyclerView to which the ItemTouchHelper is attached.
         * @param viewHolder
         *            The ViewHolder which is being interacted with by the user.
         * @return The movement flags specifying the allowed drag and swipe directions.
         */
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Allow dragging items up and down only, no swipe action.
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        /**
         * Called when an item is dragged to a new position.
         *
         * @param recyclerView
         *            The RecyclerView to which the ItemTouchHelper is attached.
         * @param viewHolder
         *            The ViewHolder being dragged.
         * @param target
         *            The ViewHolder that is the target of the drag event.
         * @return True if the item was moved to the new adapter position, false
         *         otherwise.
         */
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            // Prevent moving if either of the positions is associated with an empty
            // option.
            if (adapter.getOptionsArrayList().get(fromPosition).isEmpty() || adapter.getOptionsArrayList().get(toPosition).isEmpty()) {
                return false;
            }

            // Move the item in the adapter
            adapter.moveItem(fromPosition, toPosition);
            return true;
        }

        /**
         * Determines whether long-press drag is enabled for this RecyclerView.
         *
         * @return True to enable long-press drag, false otherwise.
         */
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        /**
         * Called when an item is swiped away. Not implemented as swipe actions are not
         * enabled.
         *
         * @param viewHolder
         *            The ViewHolder being swiped.
         * @param direction
         *            The direction in which the ViewHolder is swiped.
         */
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // No action needed for swipe
        }
    });
    private OnSubmitClickListener onSubmitClickListener;
    private OnClickListener backClickListener;
    private MutableLiveData<Boolean> isOptionsFilled;
    /**
     * Visibility states for various UI components.
     */
    private int sendButtonTextVisibility = View.VISIBLE;
    private int errorStateVisibility = View.GONE;
    /**
     * Enable or disable states for interactive elements.
     */
    private boolean sendButtonEnabled = true;
    /**
     * General poll appearance attributes.
     */
    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleTextColor;
    private @ColorInt int backgroundColor;
    private Drawable backgroundDrawable;
    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    /**
     * Poll option appearance attributes.
     */
    private @StyleRes int optionTitleTextAppearance;
    private @ColorInt int optionTitleTextColor;
    private @StyleRes int optionTextAppearance;
    private @ColorInt int optionTextColor;
    private @ColorInt int optionHintColor;
    private @Dimension int optionCornerRadius;
    private @Dimension int optionStrokeWidth;
    private @ColorInt int optionStrokeColor;
    /**
     * Poll question appearance attributes.
     */
    private @StyleRes int questionTitleTexAppearance;
    private @ColorInt int questionTitleTextColor;
    private @StyleRes int questionTextAppearance;
    private @ColorInt int questionTextColor;
    private @ColorInt int questionHintColor;
    private @Dimension int questionCornerRadius;
    private @Dimension int questionStrokeWidth;
    private @ColorInt int questionStrokeColor;
    /**
     * Icons and their properties.
     */
    private Drawable dragIcon;
    private @ColorInt int dragIconTint;
    private Drawable backIcon;
    private @ColorInt int backIconTint;
    /**
     * Separator and error text appearance attributes.
     */
    private @ColorInt int separatorColor;
    private @ColorInt int errorTextColor;
    private @StyleRes int errorTextAppearance;
    /**
     * Submit button appearance attributes.
     */
    private @ColorInt int submitButtonBackgroundColor;
    private @ColorInt int disableSubmitButtonBackgroundColor;
    /**
     * A TextWatcher that listens for changes in the text of an EditText and updates
     * the send button state accordingly.
     */
    TextWatcher textWatcher = new TextWatcher() {

        /**
         * Called before the text in the EditText is changed.
         *
         * @param charSequence
         *            The text before any change is made.
         * @param i
         *            The start position of the text being changed.
         * @param i1
         *            The number of characters that are about to be replaced.
         * @param i2
         *            The number of characters that will be added.
         */
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // No action needed before the text changes
        }

        /**
         * Called when the text in the EditText is being changed.
         *
         * @param charSequence
         *            The text currently in the EditText.
         * @param i
         *            The start position of the text being changed.
         * @param i1
         *            The number of characters that were replaced.
         * @param i2
         *            The number of characters that were added.
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Update the state of the send button when the text changes
            setSendButtonState();
        }

        /**
         * Called after the text in the EditText has been changed.
         *
         * @param editable
         *            The final text after the change.
         */
        @Override
        public void afterTextChanged(Editable editable) {
            // No action needed after the text changes
        }
    };
    private @Dimension int submitButtonCornerRadius;
    private @Dimension int submitButtonStrokeWidth;
    private @ColorInt int submitButtonStrokeColor;
    private @ColorInt int submitButtonTextColor;
    private @StyleRes int submitButtonTextAppearance;
    /**
     * Progress indicator attributes.
     */
    private @ColorInt int progressIndeterminateTint;
    /**
     * Toolbar visibility attribute.
     */
    private boolean hideToolBar;
    /**
     * Overall style attribute for the poll view.
     */
    private @StyleRes int style;

    /**
     * Constructor to initialize CometChatCreatePoll with the given context.
     *
     * @param context The context in which the view is running, used to access resources
     *                and theme attributes.
     */
    public CometChatCreatePoll(Context context) {
        this(context, null);
    }

    /**
     * Constructor to initialize CometChatCreatePoll with the given context and
     * attribute set.
     *
     * @param context The context in which the view is running, used to access resources
     *                and theme attributes.
     * @param attrs   A collection of attributes, as found associated with a tag in the
     *                XML that is inflating the view.
     */
    public CometChatCreatePoll(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatCreatePollStyle);
    }

    /**
     * Constructor to initialize CometChatCreatePoll with the given context,
     * attribute set, and default style attribute.
     *
     * @param context      The context in which the view is running, used to access resources
     *                     and theme attributes.
     * @param attrs        A collection of attributes, as found associated with a tag in the
     *                     XML that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view.
     */
    public CometChatCreatePoll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    /**
     * Initializes the CometChatCreatePoll view and sets up its components,
     * listeners, and styles.
     *
     * @param attributeSet The attribute set containing XML attributes for the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view.
     * @param defStyleRes  A resource identifier of a style resource that provides default
     *                     values for the view, used only if defStyleAttr is 0 or cannot be
     *                     found in the theme.
     */
    public void init(AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        // Inflate the layout and bind it to the view
        binding = CometchatCreatePollBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Set up RecyclerView with the PollOptionsAdapter
        isOptionsFilled = new MutableLiveData<>(false);
        adapter = new PollOptionsAdapter(isOptionsFilled, binding.rvOptions);
        binding.rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOptions.setAdapter(adapter);
        itemTouchHelper.attachToRecyclerView(binding.rvOptions);

        // Initialize MaterialCard views
        Utils.initMaterialCard(binding.pollParentCard);
        Utils.initMaterialCard(binding.questionCard);
        Utils.initMaterialCard(binding.submitBtn);

        // Set initial visibility and state for various UI components
        setSendButtonTextVisibility(sendButtonTextVisibility);
        setProgressVisibility(progressVisibility);
        setSendButtonEnabled(sendButtonEnabled);

        // Observe isOptionsFilled LiveData to update the send button state
        isOptionsFilled.observe((LifecycleOwner) getContext(), aBoolean -> setSendButtonState());

        // Add a text watcher to the question EditText field
        binding.etQuestion.addTextChangedListener(textWatcher);
        binding.etQuestion.requestFocus();

        // Apply style attributes to customize the UI
        applyStyleAttributes(attributeSet, defStyleAttr);
    }

    /**
     * Updates the state of the send button based on the input in the question
     * EditText and whether the options are filled.
     *
     * <p>
     * The send button is enabled only if the question EditText is not empty and the
     * options are filled. If the conditions are not met, the button is disabled and
     * its background color is set to indicate it cannot be pressed.
     */
    private void setSendButtonState() {
        if (binding.etQuestion.getText().toString().trim().isEmpty()) {
            binding.submitBtn.setEnabled(false);
            binding.submitBtn.setCardBackgroundColor(disableSubmitButtonBackgroundColor);
        } else {
            if (Boolean.TRUE.equals(isOptionsFilled.getValue())) {
                binding.submitBtn.setEnabled(true);
                binding.submitBtn.setCardBackgroundColor(submitButtonBackgroundColor);
            } else {
                binding.submitBtn.setEnabled(false);
                binding.submitBtn.setCardBackgroundColor(disableSubmitButtonBackgroundColor);
            }
        }
    }

    /**
     * Extracts and applies style attributes defined in XML for the
     * CometChatCreatePoll view.
     *
     * <p>
     * This method retrieves custom attributes such as title text appearance,
     * background color, and corner radius from the given {@link AttributeSet}. It
     * applies default values for any attributes that are not explicitly defined.
     *
     * @param attributeSet The {@link AttributeSet} containing the style attributes to be
     *                     extracted.
     * @param defAttr      The default style attribute to apply if no custom attributes are
     *                     specified.
     */
    private void applyStyleAttributes(AttributeSet attributeSet, int defAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.CometChatCreatePoll, defAttr, 0);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.CometChatCreatePoll, defAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts style attributes from the provided {@link TypedArray} and applies
     * default values to the corresponding member variables.
     *
     * <p>
     * This method retrieves various visual attributes such as text appearances,
     * colors, dimensions, and drawables for the CometChatCreatePoll view. If an
     * attribute is not defined in the XML, a default value is applied (usually 0).
     *
     * @param typedArray The {@link TypedArray} containing the style attributes to be
     *                   extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollTitleTextAppearance, 0);
            titleTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollTitleTextColor,
                                                 CometChatTheme.getTextColorPrimary(getContext()));
            backgroundColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollBackgroundColor,
                                                  CometChatTheme.getBackgroundColor1(getContext()));
            backgroundDrawable = typedArray.getDrawable(R.styleable.CometChatCreatePoll_cometchatCreatePollBackgroundDrawable);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollStrokeWidth, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollStrokeColor,
                                              CometChatTheme.getStrokeColorLight(getContext()));

            optionTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTitleTextAppearance, 0);
            optionTitleTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTitleTextColor,
                                                       CometChatTheme.getTextColorPrimary(getContext()));
            optionTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTextAppearance, 0);
            optionTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionTextColor,
                                                  CometChatTheme.getTextColorPrimary(getContext()));
            optionHintColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionHintColor,
                                                  CometChatTheme.getTextColorTertiary(getContext()));
            optionCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionCornerRadius, 0);
            optionStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionStrokeWidth, 0);
            optionStrokeColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollOptionStrokeColor,
                                                    CometChatTheme.getStrokeColorLight(getContext()));

            dragIcon = typedArray.getDrawable(R.styleable.CometChatCreatePoll_cometchatCreatePollDragIcon);
            dragIconTint = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollDragIconTint,
                                               CometChatTheme.getIconTintSecondary(getContext()));
            questionTitleTexAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTitleTextAppearance, 0);
            questionTitleTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTitleTextColor,
                                                         CometChatTheme.getTextColorPrimary(getContext()));
            questionTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTextAppearance, 0);
            questionTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionTextColor,
                                                    CometChatTheme.getTextColorPrimary(getContext()));
            questionHintColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionHintColor,
                                                    CometChatTheme.getTextColorTertiary(getContext()));
            questionCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionCornerRadius, 0);
            questionStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionStrokeWidth, 0);
            questionStrokeColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollQuestionStrokeColor,
                                                      CometChatTheme.getStrokeColorLight(getContext()));

            separatorColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollSeparatorColor,
                                                 CometChatTheme.getStrokeColorDefault(getContext()));
            submitButtonBackgroundColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonBackgroundColor,
                                                              CometChatTheme.getPrimaryColor(getContext()));
            disableSubmitButtonBackgroundColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollDisabledSubmitButtonBackgroundColor,
                                                                     CometChatTheme.getBackgroundColor4(getContext()));
            submitButtonCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonCornerRadius,
                                                                        0);
            submitButtonStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonStrokeWidth, 0);
            submitButtonStrokeColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonStrokeColor, 0);
            submitButtonTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonTextColor,
                                                        CometChatTheme.getColorWhite(getContext()));
            submitButtonTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollSubmitButtonTextAppearance, 0);
            progressIndeterminateTint = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollProgressIndeterminateTint,
                                                            CometChatTheme.getIconTintSecondary(getContext()));
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatCreatePoll_cometchatCreatePollCornerRadius, 0);

            errorTextColor = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollErrorTextColor,
                                                 CometChatTheme.getErrorColor(getContext()));
            errorTextAppearance = typedArray.getResourceId(R.styleable.CometChatCreatePoll_cometchatCreatePollErrorTextAppearance, 0);

            backIcon = typedArray.getDrawable(R.styleable.CometChatCreatePoll_cometchatCreatePollBackIcon);
            backIconTint = typedArray.getColor(R.styleable.CometChatCreatePoll_cometchatCreatePollBackIconTint,
                                               CometChatTheme.getIconTintPrimary(getContext()));

            applyStyle();

        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted style attributes to the CometChatCreatePoll view.
     *
     * <p>
     * This method sets various visual properties such as text appearances, colors,
     * dimensions, and drawables based on the previously extracted style attributes.
     * It is called after extracting the attributes from the {@link TypedArray} to
     * ensure that the view reflects the intended design specified in the XML.
     */
    private void applyStyle() {
        // Apply title style attributes
        setTitleTextAppearance(titleTextAppearance);
        setTitleTextColor(titleTextColor);
        setBackgroundColor(backgroundColor);
        setBackgroundDrawable(backgroundDrawable);
        setStrokeWidth(strokeWidth);
        setStrokeColor(strokeColor);

        // Apply option style attributes
        setOptionTitleTextAppearance(optionTitleTextAppearance);
        setOptionTitleTextColor(optionTitleTextColor);
        setOptionTextAppearance(optionTextAppearance);
        setOptionTextColor(optionTextColor);
        setOptionHintColor(optionHintColor);
        setOptionCornerRadius(optionCornerRadius);
        setOptionStrokeWidth(optionStrokeWidth);
        setOptionStrokeColor(optionStrokeColor);

        // Set drag icon and tint
        setDragIcon(dragIcon);
        setDragIconTint(dragIconTint);

        // Apply question style attributes
        setQuestionTitleTexAppearance(questionTitleTexAppearance);
        setQuestionTitleTextColor(questionTitleTextColor);
        setQuestionTextAppearance(questionTextAppearance);
        setQuestionTextColor(questionTextColor);
        setQuestionHintColor(questionHintColor);
        setQuestionCornerRadius(questionCornerRadius);
        setQuestionStrokeWidth(questionStrokeWidth);
        setQuestionStrokeColor(questionStrokeColor);

        // Set separator color
        setSeparatorColor(separatorColor);

        // Apply submit button style attributes
        setSubmitButtonBackgroundColor(submitButtonBackgroundColor);
        setDisableSubmitButtonBackgroundColor(disableSubmitButtonBackgroundColor);
        setSubmitButtonCornerRadius(submitButtonCornerRadius);
        setSubmitButtonStrokeWidth(submitButtonStrokeWidth);
        setSubmitButtonStrokeColor(submitButtonStrokeColor);
        setSubmitButtonTextColor(submitButtonTextColor);
        setSubmitButtonTextAppearance(submitButtonTextAppearance);
        setProgressIndeterminateTint(progressIndeterminateTint);
        setCornerRadius(cornerRadius);
        setHideToolBar(hideToolBar);

        // Apply error style attributes
        setErrorTextColor(errorTextColor);
        setErrorTextAppearance(errorTextAppearance);

        // Set back icon and its tint
        setBackIcon(backIcon);
        setBackIconTint(backIconTint);

        // Update the state of the send button
        setSendButtonState();
    }

    /**
     * Sets the stroke color for the poll parent card.
     *
     * <p>
     * This method updates the color of the stroke applied to the border of the
     * poll's parent card, enabling visual customization to match the application's
     * theme.
     *
     * @param strokeColor The color to set for the stroke of the poll parent card.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        binding.pollParentCard.setStrokeColor(strokeColor);
    }

    @Nullable
    @Override
    public ColorStateList getStrokeColorStateList() {
        return super.getStrokeColorStateList();
    }

    /**
     * Sets the background color for the poll options.
     *
     * <p>
     * This method updates the background color of the option cards in the poll,
     * allowing for visual customization that enhances the overall design and user
     * experience.
     *
     * @param backgroundColor The color to set as the background for the poll options.
     */
    public void setOptionBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        adapter.setOptionBackgroundColor(backgroundColor);
    }

    // Getters for testing or direct access if needed
    public int getSendButtonTextVisibility() {
        return sendButtonTextVisibility;
    }

    /**
     * Sets the visibility of the send button text.
     *
     * <p>
     * This method controls the visibility of the text displayed on the send button.
     * It allows for customization of the button's appearance based on the current
     * state of the UI, such as when waiting for a response or displaying an error
     * message.
     *
     * @param visibility The visibility state to set for the send button text. It should be
     *                   one of the constants from the View class (e.g., View.VISIBLE,
     *                   View.GONE).
     */
    public void setSendButtonTextVisibility(int visibility) {
        this.sendButtonTextVisibility = visibility;
        binding.tvSend.setVisibility(visibility);
    }

    public int getProgressVisibility() {
        return progressVisibility;
    }

    /**
     * Sets the visibility of the progress indicator.
     *
     * <p>
     * This method controls the visibility of the progress indicator displayed in
     * the UI. When the progress indicator is visible, the send button text is
     * hidden, and the send button is disabled. When the progress indicator is
     * hidden, the send button text is shown, and the send button is enabled.
     *
     * @param visibility The visibility state to set for the progress indicator. It should
     *                   be one of the constants from the View class (e.g., View.VISIBLE,
     *                   View.GONE).
     */
    public void setProgressVisibility(int visibility) {
        binding.progress.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            binding.tvSend.setVisibility(View.GONE);
            setSendButtonEnabled(false);
        } else {
            binding.tvSend.setVisibility(View.VISIBLE);
            setSendButtonEnabled(true);
        }
    }

    public int getErrorStateVisibility() {
        return errorStateVisibility;
    }

    /**
     * Sets the visibility of the error state UI element.
     *
     * <p>
     * This method updates the visibility of the error text view, allowing it to be
     * shown or hidden based on the current state of the UI. It can be used to
     * inform the user of any errors or validation issues that need to be addressed.
     *
     * @param errorStateVisibility The visibility state to set for the error text view (e.g.,
     *                             {@link View#VISIBLE}, {@link View#GONE}, or
     *                             {@link View#INVISIBLE}).
     */
    public void setErrorStateVisibility(int errorStateVisibility) {
        this.errorStateVisibility = errorStateVisibility;
        binding.tvError.setVisibility(errorStateVisibility);
    }

    public boolean isSendButtonEnabled() {
        return sendButtonEnabled;
    }    /**
     * Sets the background drawable for the poll UI elements.
     *
     * <p>
     * This method applies a drawable background to the poll's UI elements. If the
     * provided drawable is not null, it updates the background of the adapter
     * options as well.
     *
     * @param backgroundDrawable The drawable to set as the background. Can be null to clear the
     *                           background.
     */
    @Override
    public void setBackground(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            adapter.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Enables or disables the send button.
     *
     * <p>
     * This method updates the enabled state of the send button, allowing for
     * control over whether the button can be interacted with by the user. Disabling
     * the button is useful when waiting for an operation to complete or validating
     * input.
     *
     * @param enabled True to enable the send button; false to disable it.
     */
    private void setSendButtonEnabled(boolean enabled) {
        this.sendButtonEnabled = enabled;
        binding.submitBtn.setEnabled(enabled);
    }    /**
     * Sets the stroke width for the poll parent card.
     *
     * <p>
     * This method updates the stroke width applied to the border of the poll's
     * parent card, allowing for customization of the card's appearance.
     *
     * @param strokeWidth The width of the stroke in pixels.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        binding.pollParentCard.setStrokeWidth(strokeWidth);
    }

    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title.
     *
     * <p>
     * This method applies a specified text appearance style resource to the title
     * text view. It allows customization of font attributes to enhance the visual
     * hierarchy of the UI.
     *
     * @param titleTextAppearance The style resource to apply to the title text view.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.tvTitle.setTextAppearance(titleTextAppearance);
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title displayed in the UI.
     *
     * <p>
     * This method updates the text color of the title text view. The color can be
     * customized to fit the design of the application and enhance readability.
     *
     * @param titleTextColor The color to set for the title text view.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.tvTitle.setTextColor(titleTextColor);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the poll parent card and updates the options
     * background color.
     *
     * <p>
     * This method applies a background color to the poll's parent card view and
     * ensures that the options in the adapter are also updated with the same
     * background color.
     *
     * @param backgroundColor The color to set as the background for the poll parent card.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        binding.pollParentCard.setCardBackgroundColor(backgroundColor);
        adapter.setOptionBackgroundColor(backgroundColor);
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the poll parent card.
     *
     * <p>
     * This method updates the corner radius of the poll parent card, allowing for
     * visual customization that enhances the overall aesthetics and design
     * consistency of the poll interface.
     *
     * @param cornerRadius The radius to set for the corners of the poll parent card.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        binding.pollParentCard.setRadius(cornerRadius);
    }

    public int getOptionTitleTextAppearance() {
        return optionTitleTextAppearance;
    }

    /**
     * Sets the text appearance for the option title.
     *
     * <p>
     * This method applies a specified text appearance style to the title of the
     * options, allowing for customization of font size, style, and other text
     * attributes to enhance the readability and aesthetic of the poll options.
     *
     * @param optionTitleTextAppearance The style resource ID to set for the option title text appearance.
     */
    public void setOptionTitleTextAppearance(@StyleRes int optionTitleTextAppearance) {
        this.optionTitleTextAppearance = optionTitleTextAppearance;
        binding.tvOptions.setTextAppearance(optionTitleTextAppearance);
    }

    public int getOptionTitleTextColor() {
        return optionTitleTextColor;
    }

    /**
     * Sets the text color for the option title.
     *
     * <p>
     * This method updates the text color of the option title, allowing for visual
     * customization that enhances readability and ensures consistency with the
     * overall theme of the poll interface.
     *
     * @param optionTitleTextColor The color to set for the text of the option title.
     */
    public void setOptionTitleTextColor(@ColorInt int optionTitleTextColor) {
        this.optionTitleTextColor = optionTitleTextColor;
        binding.tvOptions.setTextColor(optionTitleTextColor);
    }

    public int getOptionTextAppearance() {
        return optionTextAppearance;
    }

    /**
     * Sets the text appearance style for the poll options.
     *
     * <p>
     * This method applies a specified text appearance style resource to the options
     * in the poll. It allows customization of font size, style, and other text
     * attributes for better readability and design consistency.
     *
     * @param optionTextAppearance The style resource to apply to the poll option text views.
     */
    public void setOptionTextAppearance(@StyleRes int optionTextAppearance) {
        this.optionTextAppearance = optionTextAppearance;
        adapter.setOptionTextAppearance(optionTextAppearance);
    }

    public int getOptionTextColor() {
        return optionTextColor;
    }

    /**
     * Sets the text color for the poll options.
     *
     * <p>
     * This method updates the text color of the options in the poll, allowing for
     * customization to enhance readability and visual appeal.
     *
     * @param optionTextColor The color to set for the text of the poll options.
     */
    public void setOptionTextColor(@ColorInt int optionTextColor) {
        this.optionTextColor = optionTextColor;
        adapter.setOptionTextColor(optionTextColor);
    }

    public int getOptionHintColor() {
        return optionHintColor;
    }

    /**
     * Sets the hint color for the option text fields in the poll.
     *
     * <p>
     * This method updates the color of the hint text displayed in the option text
     * fields, providing a visual cue to users on what information is expected.
     *
     * @param optionHintColor The color to set for the hint text of the option fields.
     */
    public void setOptionHintColor(@ColorInt int optionHintColor) {
        this.optionHintColor = optionHintColor;
        adapter.setOptionHintColor(optionHintColor);
    }

    public int getOptionCornerRadius() {
        return optionCornerRadius;
    }

    /**
     * Sets the corner radius for the option cards in the poll.
     *
     * <p>
     * This method defines the curvature of the corners of the option cards,
     * allowing for a more modern and visually appealing design.
     *
     * @param optionCornerRadius The radius in pixels for the corners of the option cards.
     */
    public void setOptionCornerRadius(@Dimension int optionCornerRadius) {
        this.optionCornerRadius = optionCornerRadius;
        adapter.setOptionCornerRadius(optionCornerRadius);
    }

    public int getOptionStrokeWidth() {
        return optionStrokeWidth;
    }

    /**
     * Sets the stroke width for the option cards in the poll.
     *
     * <p>
     * This method updates the stroke width applied to the border of the option
     * cards, allowing for customization of their appearance in the UI.
     *
     * @param optionStrokeWidth The width of the stroke in pixels for the option cards.
     */
    public void setOptionStrokeWidth(@Dimension int optionStrokeWidth) {
        this.optionStrokeWidth = optionStrokeWidth;
        adapter.setOptionStrokeWidth(optionStrokeWidth);
    }

    public int getOptionStrokeColor() {
        return optionStrokeColor;
    }

    /**
     * Sets the stroke color for the poll options.
     *
     * <p>
     * This method updates the stroke color applied to the borders of the option
     * cards, enabling customization for visual appeal and coherence with the
     * application's theme.
     *
     * @param optionStrokeColor The color to set for the stroke of the poll option cards.
     */
    public void setOptionStrokeColor(@ColorInt int optionStrokeColor) {
        this.optionStrokeColor = optionStrokeColor;
        adapter.setOptionStrokeColor(optionStrokeColor);
    }

    public int getQuestionTitleTexAppearance() {
        return questionTitleTexAppearance;
    }

    /**
     * Sets the text appearance for the question title.
     *
     * <p>
     * This method applies a specified text appearance style to the question title,
     * allowing for customization of font size, style, and other text attributes to
     * enhance the visibility and aesthetics of the poll question.
     *
     * @param questionTitleTexAppearance The style resource ID to set for the question title text
     *                                   appearance.
     */
    public void setQuestionTitleTexAppearance(@StyleRes int questionTitleTexAppearance) {
        this.questionTitleTexAppearance = questionTitleTexAppearance;
        binding.tvQuestion.setTextAppearance(questionTitleTexAppearance);
    }

    public int getQuestionTitleTextColor() {
        return questionTitleTextColor;
    }

    /**
     * Sets the text color for the question title.
     *
     * <p>
     * This method updates the text color of the question title, allowing for
     * customization that enhances its visibility and ensures a cohesive design
     * within the poll interface.
     *
     * @param questionTitleTextColor The color to set for the text of the question title.
     */
    public void setQuestionTitleTextColor(@ColorInt int questionTitleTextColor) {
        this.questionTitleTextColor = questionTitleTextColor;
        binding.tvQuestion.setTextColor(questionTitleTextColor);
    }

    public int getQuestionTextAppearance() {
        return questionTextAppearance;
    }

    /**
     * Sets the text appearance style for the question input field.
     *
     * <p>
     * This method applies a specified text appearance style resource to the
     * question input field, allowing for customization of font size, style, and
     * other text attributes to improve readability and user experience.
     *
     * @param questionTextAppearance The style resource to apply to the question text view.
     */
    public void setQuestionTextAppearance(@StyleRes int questionTextAppearance) {
        this.questionTextAppearance = questionTextAppearance;
        binding.etQuestion.setTextAppearance(questionTextAppearance);
    }

    public int getQuestionTextColor() {
        return questionTextColor;
    }

    /**
     * Sets the text color for the question input field.
     *
     * <p>
     * This method updates the text color of the question input field, enhancing
     * visibility and consistency with the application's theme.
     *
     * @param questionTextColor The color to set for the text in the question input field.
     */
    public void setQuestionTextColor(@ColorInt int questionTextColor) {
        this.questionTextColor = questionTextColor;
        binding.etQuestion.setTextColor(questionTextColor);
    }

    public int getQuestionHintColor() {
        return questionHintColor;
    }

    /**
     * Sets the hint color for the question input field.
     *
     * <p>
     * This method updates the color of the hint text displayed in the question
     * input field, providing a visual cue to users about the expected input.
     *
     * @param questionHintColor The color to set for the hint text in the question input field.
     */
    public void setQuestionHintColor(@ColorInt int questionHintColor) {
        this.questionHintColor = questionHintColor;
        binding.etQuestion.setHintTextColor(questionHintColor);
    }

    public int getQuestionCornerRadius() {
        return questionCornerRadius;
    }

    /**
     * Sets the corner radius for the question card.
     *
     * <p>
     * This method updates the corner radius of the question card, allowing for
     * visual customization to enhance the overall aesthetics of the poll interface.
     *
     * @param questionCornerRadius The radius to set for the corners of the question card.
     */
    public void setQuestionCornerRadius(@Dimension int questionCornerRadius) {
        this.questionCornerRadius = questionCornerRadius;
        binding.questionCard.setRadius(questionCornerRadius);
    }

    public int getQuestionStrokeWidth() {
        return questionStrokeWidth;
    }

    /**
     * Sets the stroke width for the question card.
     *
     * <p>
     * This method updates the stroke width applied to the border of the question
     * card, enabling customization for visual clarity and appeal within the poll
     * UI.
     *
     * @param questionStrokeWidth The width to set for the stroke of the question card.
     */
    public void setQuestionStrokeWidth(@Dimension int questionStrokeWidth) {
        this.questionStrokeWidth = questionStrokeWidth;
        binding.questionCard.setStrokeWidth(questionStrokeWidth);
    }

    public int getQuestionStrokeColor() {
        return questionStrokeColor;
    }

    /**
     * Sets the stroke color for the question card.
     *
     * <p>
     * This method updates the stroke color applied to the borders of the question
     * card, allowing for a cohesive look and feel that matches the application's
     * design.
     *
     * @param questionStrokeColor The color to set for the stroke of the question card.
     */
    public void setQuestionStrokeColor(@ColorInt int questionStrokeColor) {
        this.questionStrokeColor = questionStrokeColor;
        binding.questionCard.setStrokeColor(questionStrokeColor);
    }

    public Drawable getDragIcon() {
        return dragIcon;
    }

    /**
     * Sets the drag icon for the poll options.
     *
     * <p>
     * This method defines the icon used for dragging the poll options within the
     * RecyclerView, enhancing the user interface's interactivity and usability.
     *
     * @param dragIcon The drawable resource to be used as the drag icon.
     */
    public void setDragIcon(Drawable dragIcon) {
        this.dragIcon = dragIcon;
        adapter.setDragIcon(dragIcon);
    }

    public int getDragIconTint() {
        return dragIconTint;
    }

    /**
     * Sets the tint color for the drag icon.
     *
     * <p>
     * This method applies a color tint to the drag icon, allowing for customization
     * to match the application's design language.
     *
     * @param color The color to tint the drag icon.
     */
    public void setDragIconTint(@ColorInt int color) {
        this.dragIconTint = color;
        adapter.setDragIconTint(color);
    }

    public int getSeparatorColor() {
        return separatorColor;
    }    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the color of the separator between UI elements.
     *
     * <p>
     * This method updates the background color of the separator, allowing for
     * customization that can improve the visual structure of the poll interface.
     *
     * @param separatorColor The color to set for the separator.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.separator.setBackgroundColor(separatorColor);
    }

    public int getErrorTextColor() {
        return errorTextColor;
    }

    /**
     * Sets the text color for the error message displayed in the UI.
     *
     * <p>
     * This method updates the text color of the error text view to visually
     * indicate errors to the user. The provided color will be applied immediately.
     *
     * @param errorTextColor The color to set for the error text view.
     */
    public void setErrorTextColor(@ColorInt int errorTextColor) {
        this.errorTextColor = errorTextColor;
        binding.tvError.setTextColor(errorTextColor);
    }

    public int getErrorTextAppearance() {
        return errorTextAppearance;
    }

    /**
     * Sets the text appearance style for the error message.
     *
     * <p>
     * This method applies a specified text appearance style resource to the error
     * text view. It allows for customization of font size, style, and other text
     * attributes to ensure the error message is clearly visible to the user.
     *
     * @param errorTextAppearance The style resource to apply to the error text view.
     */
    public void setErrorTextAppearance(@StyleRes int errorTextAppearance) {
        this.errorTextAppearance = errorTextAppearance;
        binding.tvError.setTextAppearance(errorTextAppearance);
    }

    public Drawable getBackIcon() {
        return backIcon;
    }

    /**
     * Sets the back icon drawable for the back button.
     *
     * <p>
     * This method updates the back icon displayed in the UI with the provided
     * drawable. It also stores the drawable for future reference.
     *
     * @param backIcon The drawable to set as the back icon.
     */
    public void setBackIcon(Drawable backIcon) {
        this.backIcon = backIcon;
        binding.imgBack.setImageDrawable(backIcon);
    }

    public int getBackIconTint() {
        return backIconTint;
    }

    /**
     * Sets the tint color for the back icon.
     *
     * <p>
     * This method updates the color filter applied to the back icon, allowing for
     * customization of its appearance to fit the theme of the application.
     *
     * @param backIconTint The color to apply as a tint to the back icon.
     */
    public void setBackIconTint(@ColorInt int backIconTint) {
        this.backIconTint = backIconTint;
        binding.imgBack.setColorFilter(backIconTint);
    }

    public int getSubmitButtonBackgroundColor() {
        return submitButtonBackgroundColor;
    }

    /**
     * Sets the background color for the submit button.
     *
     * <p>
     * This method updates the background color of the submit button, enhancing its
     * visibility and coherence with the overall theme of the poll interface.
     *
     * @param submitButtonBackgroundColor The color to set as the background for the submit button.
     */
    public void setSubmitButtonBackgroundColor(@ColorInt int submitButtonBackgroundColor) {
        this.submitButtonBackgroundColor = submitButtonBackgroundColor;
        binding.submitBtn.setCardBackgroundColor(submitButtonBackgroundColor);
    }

    public int getDisableSubmitButtonBackgroundColor() {
        return disableSubmitButtonBackgroundColor;
    }

    /**
     * Sets the background color for the disabled state of the submit button.
     *
     * <p>
     * This method updates the background color of the submit button when it is in a
     * disabled state, allowing for visual feedback to the user that the button
     * cannot be interacted with.
     *
     * @param disableSubmitButtonBackgroundColor The color to set for the disabled submit button background.
     */
    public void setDisableSubmitButtonBackgroundColor(@ColorInt int disableSubmitButtonBackgroundColor) {
        this.disableSubmitButtonBackgroundColor = disableSubmitButtonBackgroundColor;
    }

    public int getSubmitButtonCornerRadius() {
        return submitButtonCornerRadius;
    }

    /**
     * Sets the corner radius for the submit button.
     *
     * <p>
     * This method updates the corner radius of the submit button, allowing for
     * design consistency with the poll's visual theme.
     *
     * @param submitButtonCornerRadius The radius to set for the corners of the submit button.
     */
    public void setSubmitButtonCornerRadius(@Dimension int submitButtonCornerRadius) {
        this.submitButtonCornerRadius = submitButtonCornerRadius;
        binding.submitBtn.setRadius(submitButtonCornerRadius);
    }

    public int getSubmitButtonStrokeWidth() {
        return submitButtonStrokeWidth;
    }

    /**
     * Sets the stroke width for the submit button.
     *
     * <p>
     * This method updates the stroke width applied to the border of the submit
     * button, enabling visual customization that enhances its appearance and
     * usability.
     *
     * @param submitButtonStrokeWidth The width to set for the stroke of the submit button.
     */
    public void setSubmitButtonStrokeWidth(@Dimension int submitButtonStrokeWidth) {
        this.submitButtonStrokeWidth = submitButtonStrokeWidth;
        binding.submitBtn.setStrokeWidth(submitButtonStrokeWidth);
    }

    public int getSubmitButtonStrokeColor() {
        return submitButtonStrokeColor;
    }

    /**
     * Sets the stroke color for the submit button.
     *
     * <p>
     * This method updates the stroke color applied to the borders of the submit
     * button, allowing for a cohesive look and feel that matches the application's
     * design.
     *
     * @param submitButtonStrokeColor The color to set for the stroke of the submit button.
     */
    public void setSubmitButtonStrokeColor(@ColorInt int submitButtonStrokeColor) {
        this.submitButtonStrokeColor = submitButtonStrokeColor;
        binding.submitBtn.setStrokeColor(submitButtonStrokeColor);
    }

    public int getSubmitButtonTextColor() {
        return submitButtonTextColor;
    }

    /**
     * Sets the text color for the submit button.
     *
     * <p>
     * This method updates the text color of the submit button, allowing for
     * customization that enhances its visibility and overall aesthetics within the
     * poll interface.
     *
     * @param submitButtonTextColor The color to set for the text of the submit button.
     */
    public void setSubmitButtonTextColor(@ColorInt int submitButtonTextColor) {
        this.submitButtonTextColor = submitButtonTextColor;
        binding.tvSend.setTextColor(submitButtonTextColor);
    }

    public int getSubmitButtonTextAppearance() {
        return submitButtonTextAppearance;
    }

    /**
     * Sets the text appearance for the submit button.
     *
     * <p>
     * This method applies a specified text appearance style to the submit button's
     * text, enabling developers to customize the font size, style, and other text
     * attributes for better integration with the overall UI design.
     *
     * @param submitButtonTextAppearance The style resource ID to set for the submit button's text
     *                                   appearance.
     */
    public void setSubmitButtonTextAppearance(@StyleRes int submitButtonTextAppearance) {
        this.submitButtonTextAppearance = submitButtonTextAppearance;
        binding.tvSend.setTextAppearance(submitButtonTextAppearance);
    }

    public int getProgressIndeterminateTint() {
        return progressIndeterminateTint;
    }

    /**
     * Sets the tint color for the indeterminate progress indicator.
     *
     * <p>
     * This method updates the tint of the progress indicator when it is in an
     * indeterminate state, allowing for better visibility and alignment with the
     * application's theme.
     *
     * @param progressIndeterminateTint The color to set as the tint for the indeterminate progress
     *                                  indicator.
     */
    public void setProgressIndeterminateTint(@ColorInt int progressIndeterminateTint) {
        this.progressIndeterminateTint = progressIndeterminateTint;
        binding.progress.setIndeterminateTintList(ColorStateList.valueOf(progressIndeterminateTint));
    }

    public boolean isHideToolBar() {
        return hideToolBar;
    }

    /**
     * Sets the visibility of the toolbar elements based on the provided flag.
     *
     * <p>
     * This method controls whether the toolbar, including the title, separator, and
     * back icon, should be displayed or hidden. Setting the flag to true hides
     * these elements, while setting it to false shows them.
     *
     * @param hideToolBar True to hide the toolbar elements; false to show them.
     */
    public void setHideToolBar(boolean hideToolBar) {
        this.hideToolBar = hideToolBar;
        binding.tvTitle.setVisibility(hideToolBar ? View.GONE : View.VISIBLE);
        binding.separator.setVisibility(hideToolBar ? View.GONE : View.VISIBLE);
        binding.imgBack.setVisibility(hideToolBar ? View.GONE : View.VISIBLE);
    }

    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the CometChatCreatePoll view.
     *
     * @param style The style resource ID to be applied to the view.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().obtainStyledAttributes(style, R.styleable.CometChatCreatePoll);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public OnSubmitClickListener getOnSubmitClickListener() {
        return onSubmitClickListener;
    }

    /**
     * Sets a listener for submit button click events.
     *
     * <p>
     * This method registers a listener that will be notified when the submit button
     * is clicked. It processes the question and options, sending them to the
     * listener when the button is clicked.
     *
     * @param onSubmitClickListener The listener to be notified of submit button clicks. If null, no
     *                              action is taken.
     */
    public void setOnSubmitClickListener(OnSubmitClickListener onSubmitClickListener) {
        if (onSubmitClickListener != null) {
            this.onSubmitClickListener = onSubmitClickListener;
            binding.submitBtn.setOnClickListener(v -> {
                String question = binding.etQuestion.getText().toString();
                List<String> options = adapter.getOptionsArrayList();
                JSONArray processedOptions = new JSONArray();
                for (int i = 0; i < options.size(); i++) {
                    if (!options.get(i).isEmpty()) {
                        try {
                            processedOptions.put(i, options.get(i).trim());
                        } catch (Exception e) {
                            CometChatLogger.e(TAG, e.toString());
                        }
                    }
                }
                onSubmitClickListener.onSubmitClick(question.trim(), processedOptions);
            });
        }
    }

    public OnClickListener getBackClickListener() {
        return backClickListener;
    }

    /**
     * Sets a listener for back button click events.
     *
     * <p>
     * This method registers a listener that will be notified when the back button
     * is clicked, allowing for custom handling of back navigation or functionality.
     *
     * @param backClickListener The listener to be notified of back button clicks. If null, no
     *                          action is taken.
     */
    public void setBackClickListener(OnClickListener backClickListener) {
        if (backClickListener != null) {
            this.backClickListener = backClickListener;
            binding.imgBack.setOnClickListener(backClickListener);
        }
    }

    /**
     * An interface for handling submit button click events.
     *
     * <p>
     * This interface defines a single method that is called when the submit button
     * is clicked. Implementing classes should provide the logic for handling the
     * submitted question and its options.
     */
    public interface OnSubmitClickListener {

        /**
         * Called when the submit button is clicked.
         *
         * @param question The question text that has been entered by the user.
         * @param options  A JSONArray containing the options provided by the user for the
         *                 poll.
         */
        void onSubmitClick(String question, JSONArray options);
    }






}
