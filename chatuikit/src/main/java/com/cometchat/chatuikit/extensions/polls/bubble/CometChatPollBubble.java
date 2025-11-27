package com.cometchat.chatuikit.extensions.polls.bubble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.extensions.polls.OnOptionClick;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

public class CometChatPollBubble extends MaterialCardView {
    private static final String TAG = CometChatPollBubble.class.getSimpleName();

    private TextView question;
    private JSONObject jsonObject;
    private PollAnswerAdapter pollAnswerAdapter;
    private @ColorInt int progressColor;
    private @ColorInt int progressBackgroundColor;
    private Drawable selectedStateDrawable;
    private Drawable unselectedStateDrawable;
    private @ColorInt int voteCountTextColor;
    private @ColorInt int selectedRadioButtonStrokeColor;
    private @ColorInt int selectedIconTint;
    private @Dimension int selectedRadioButtonCornerRadius;
    private @Dimension int selectedRadioButtonStrokeWidth;
    private @ColorInt int unselectedRadioButtonStrokeColor;
    private @ColorInt int unselectedIconTint;
    private @Dimension int unselectedRadioButtonCornerRadius;
    private @Dimension int unselectedRadioButtonStrokeWidth;
    private @StyleRes int optionAvatarStyle;
    private @ColorInt int titleTextColor;
    private @ColorInt int optionTextColor;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int optionTextAppearance;
    private @StyleRes int voteCountTextAppearance;
    private @StyleRes int style;
    private @ColorInt int progressIndeterminateTint;
    /**
     * An implementation of the {@link OnOptionClick} interface that handles the
     * user's selection of a poll option.
     *
     * <p>
     * This anonymous class defines the behavior when a poll option is clicked. It
     * constructs a JSON object representing the user's vote and sends it to the
     * CometChat server via the callExtension method. The poll's ID is retrieved
     * from the custom data of the associated message, and the selected option's
     * position is included in the vote data.
     *
     * <p>
     * Upon successful voting, the response from the server is handled in the
     * {@link CometChat.CallbackListener} methods.
     */
    private OnOptionClick onOptionClick = new OnOptionClick() {
        @Override
        public void onClick(CustomMessage baseMessage, String selectedOption, int position) {
            try {
                String pollsId;
                if (jsonObject.has(ExtensionConstants.ExtensionJSONField.ID))
                    pollsId = jsonObject.getString(ExtensionConstants.ExtensionJSONField.ID);
                else pollsId = baseMessage.getId() + "";
                JSONObject pollsJsonObject = new JSONObject();
                pollsJsonObject.put(ExtensionConstants.ExtensionJSONField.VOTE, position + 1);
                pollsJsonObject.put(ExtensionConstants.ExtensionJSONField.ID, pollsId);
                CometChat.callExtension(ExtensionConstants.POLLS,
                                        ExtensionConstants.ExtensionRequest.POST,
                                        ExtensionConstants.ExtensionUrls.VOTE_POLL,
                                        pollsJsonObject,
                                        new CometChat.CallbackListener<JSONObject>() {
                                            @Override
                                            public void onSuccess(JSONObject jsonObject1) {
                                            }

                                            @Override
                                            public void onError(CometChatException e) {
                                            }
                                        });
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
    };

    /**
     * Creates a new instance of CometChatPollBubble with the specified context.
     *
     * <p>
     * This constructor initializes the poll bubble with default attributes.
     *
     * @param context The context to use for creating the poll bubble.
     */
    public CometChatPollBubble(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Creates a new instance of CometChatPollBubble with the specified context and
     * attributes.
     *
     * <p>
     * This constructor initializes the poll bubble with the given attributes.
     *
     * @param context The context to use for creating the poll bubble.
     * @param attrs   The attribute set containing custom attributes for the poll
     *                bubble.
     */
    public CometChatPollBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatPollBubbleStyle);
    }

    /**
     * Creates a new instance of CometChatPollBubble with the specified context,
     * attributes, and default style.
     *
     * <p>
     * This constructor initializes the poll bubble with the given attributes and
     * style.
     *
     * @param context      The context to use for creating the poll bubble.
     * @param attrs        The attribute set containing custom attributes for the poll
     *                     bubble.
     * @param defStyleAttr The default style attribute to apply.
     */
    public CometChatPollBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Inflates the view and initializes the poll bubble components.
     *
     * <p>
     * This method inflates the layout for the poll bubble and initializes the views
     * for the question and options. It also sets up the RecyclerView for displaying
     * poll options.
     *
     * @param attributeSet The attribute set containing custom attributes for the poll
     *                     bubble.
     * @param defStyleAttr The default style attribute to apply.
     */
    private void inflateAndInitializeView(AttributeSet attributeSet, int defStyleAttr) {
        Utils.initMaterialCard(this);
        View view = View.inflate(getContext(), R.layout.cometchat_message_polls_bubble, null);
        question = view.findViewById(R.id.tv_question);
        RecyclerView optionsRecyclerView = view.findViewById(R.id.rv_options);
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pollAnswerAdapter = new PollAnswerAdapter(onOptionClick);
        optionsRecyclerView.setAdapter(pollAnswerAdapter);
        addView(view);
        applyStyleAttributes(attributeSet, defStyleAttr, 0);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatPollBubble, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatPollBubble, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setProgressColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleProgressColor,
                                                 CometChatTheme.getPrimaryColor(getContext())));
            setProgressBackgroundColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleProgressBackgroundColor,
                                                           CometChatTheme.getExtendedPrimaryColor700(getContext())));
            setSelectedStateDrawable(typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedStateDrawable));
            setUnselectedStateDrawable(typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedStateDrawable));
            setSelectedIconTint(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedIconTint,
                                                    CometChatTheme.getPrimaryColor(getContext())));
            setSelectedRadioButtonStrokeColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeColor,
                                                                  0));
            setVoteCountTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextColor, 0));
            setSelectedRadioButtonCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonCornerRadius,
                                                                                0));
            setSelectedRadioButtonStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeWidth,
                                                                               0));
            setUnselectedRadioButtonStrokeColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeColor,
                                                                    CometChatTheme.getIconTintSecondary(getContext())));
            setUnselectedIconTint(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedIconTint, 0));
            setUnselectedRadioButtonCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonCornerRadius,
                                                                                  0));
            setUnselectedRadioButtonStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeWidth,
                                                                                 0));
            setOptionAvatarStyle(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleOptionAvatarStyle, 0));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextColor, 0));
            setOptionTextColor(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextColor, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextAppearance, 0));
            setOptionTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextAppearance, 0));
            setVoteCountTextAppearance(typedArray.getResourceId(R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextAppearance, 0));
            setProgressIndeterminateTint(typedArray.getColor(R.styleable.CometChatPollBubble_cometchatPollBubbleProgressIndeterminateTint,
                                                             CometChatTheme.getIconTintSecondary(getContext())));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the poll message data to be displayed.
     *
     * <p>
     * This method updates the poll bubble with the information contained in the
     * provided {@link CustomMessage}. It extracts the question and options from the
     * custom data and updates the UI accordingly. It also checks if the logged-in
     * user has voted on the poll and highlights the user's selected option.
     *
     * @param baseMessage The {@link CustomMessage} containing the poll data. If null, no
     *                    action is taken.
     * @throws RuntimeException if there is an error parsing the JSON data.
     */
    public void setMessage(CustomMessage baseMessage) {
        if (baseMessage != null) {
            jsonObject = baseMessage.getCustomData();
            try {
                JSONObject options = jsonObject.getJSONObject(ExtensionConstants.ExtensionJSONField.OPTIONS);
                question.setText(jsonObject.getString(ExtensionConstants.ExtensionJSONField.QUESTION));
                int myVotedPosition = Extensions.userVotedOn(baseMessage, options.length(), CometChatUIKit.getLoggedInUser().getUid());
                pollAnswerAdapter.setMessage(baseMessage);
                pollAnswerAdapter.setMyChosenOptionPosition(myVotedPosition - 1);
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    // Getters for testing or direct access if needed
    public @ColorInt int getProgressColor() {
        return progressColor;
    }

    /**
     * Sets the color of the progress indicator.
     *
     * <p>
     * This method updates the color used for the progress indicator in the poll
     * options.
     *
     * @param progressColor The color to set for the progress indicator, specified as an
     *                      integer resource.
     */
    public void setProgressColor(@ColorInt int progressColor) {
        this.progressColor = progressColor;
        pollAnswerAdapter.setProgressColor(progressColor);
    }

    public @ColorInt int getProgressBackgroundColor() {
        return progressBackgroundColor;
    }

    /**
     * Sets the background color of the progress indicator.
     *
     * <p>
     * This method updates the background color behind the progress indicator in the
     * poll options.
     *
     * @param progressBackgroundColor The color to set for the progress background, specified as an
     *                                integer resource.
     */
    public void setProgressBackgroundColor(@ColorInt int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
        pollAnswerAdapter.setProgressBackgroundColor(progressBackgroundColor);
    }

    public Drawable getSelectedStateDrawable() {
        return selectedStateDrawable;
    }

    /**
     * Sets the drawable for the selected state of an option.
     *
     * <p>
     * This method updates the drawable used to represent a selected option in the
     * poll.
     *
     * @param selectedStateDrawable The drawable to set for the selected state.
     */
    public void setSelectedStateDrawable(Drawable selectedStateDrawable) {
        this.selectedStateDrawable = selectedStateDrawable;
        pollAnswerAdapter.setSelectedStateDrawable(selectedStateDrawable);
    }

    public Drawable getUnselectedStateDrawable() {
        return unselectedStateDrawable;
    }

    /**
     * Sets the drawable for the unselected state of an option.
     *
     * <p>
     * This method updates the drawable used to represent an unselected option in
     * the poll.
     *
     * @param unselectedStateDrawable The drawable to set for the unselected state.
     */
    public void setUnselectedStateDrawable(Drawable unselectedStateDrawable) {
        this.unselectedStateDrawable = unselectedStateDrawable;
        pollAnswerAdapter.setUnselectedStateDrawable(unselectedStateDrawable);
    }

    public @ColorInt int getSelectedIconTint() {
        return selectedIconTint;
    }

    /**
     * Sets the tint color for the selected icon.
     *
     * <p>
     * This method updates the tint color applied to the icon of a selected option
     * in the poll.
     *
     * @param selectedIconTint The color to set for the selected icon, specified as an integer
     *                         resource.
     */
    public void setSelectedIconTint(@ColorInt int selectedIconTint) {
        this.selectedIconTint = selectedIconTint;
        pollAnswerAdapter.setSelectedIconTint(selectedIconTint);
    }

    public @ColorInt int getSelectedRadioButtonStrokeColor() {
        return selectedRadioButtonStrokeColor;
    }

    /**
     * Sets the stroke color for the selected radio button.
     *
     * <p>
     * This method updates the stroke color applied to the radio button of the
     * selected option.
     *
     * @param selectedRadioButtonStrokeColor The color to set for the selected radio button stroke, specified
     *                                       as an integer resource.
     */
    public void setSelectedRadioButtonStrokeColor(@ColorInt int selectedRadioButtonStrokeColor) {
        this.selectedRadioButtonStrokeColor = selectedRadioButtonStrokeColor;
        pollAnswerAdapter.setSelectedRadioButtonStrokeColor(selectedRadioButtonStrokeColor);
    }

    public @ColorInt int getVoteCountTextColor() {
        return voteCountTextColor;
    }

    /**
     * Sets the text color for the vote count display.
     *
     * <p>
     * This method updates the text color used for displaying the number of votes
     * for an option.
     *
     * @param voteCountTextColor The color to set for the vote count text, specified as an integer
     *                           resource.
     */
    public void setVoteCountTextColor(@ColorInt int voteCountTextColor) {
        this.voteCountTextColor = voteCountTextColor;
        pollAnswerAdapter.setVoteCountTextColor(voteCountTextColor);
    }

    public @Dimension int getSelectedRadioButtonCornerRadius() {
        return selectedRadioButtonCornerRadius;
    }

    /**
     * Sets the corner radius for the selected radio button.
     *
     * <p>
     * This method updates the corner radius of the selected radio button's
     * drawable.
     *
     * @param selectedRadioButtonCornerRadius The corner radius to set for the selected radio button.
     */
    public void setSelectedRadioButtonCornerRadius(@Dimension int selectedRadioButtonCornerRadius) {
        this.selectedRadioButtonCornerRadius = selectedRadioButtonCornerRadius;
        pollAnswerAdapter.setSelectedRadioButtonCornerRadius(selectedRadioButtonCornerRadius);
    }

    public @Dimension int getSelectedRadioButtonStrokeWidth() {
        return selectedRadioButtonStrokeWidth;
    }

    /**
     * Sets the stroke width for the selected radio button.
     *
     * <p>
     * This method updates the stroke width of the selected radio button's drawable.
     *
     * @param selectedRadioButtonStrokeWidth The stroke width to set for the selected radio button.
     */
    public void setSelectedRadioButtonStrokeWidth(@Dimension int selectedRadioButtonStrokeWidth) {
        this.selectedRadioButtonStrokeWidth = selectedRadioButtonStrokeWidth;
        pollAnswerAdapter.setSelectedRadioButtonStrokeWidth(selectedRadioButtonStrokeWidth);
    }

    public @StyleRes int getOptionAvatarStyle() {
        return optionAvatarStyle;
    }

    /**
     * Sets the style resource for option avatars.
     *
     * <p>
     * This method updates the style applied to avatars in the poll options.
     *
     * @param optionAvatarStyle The resource ID of the style to apply to option avatars.
     */
    public void setOptionAvatarStyle(@StyleRes int optionAvatarStyle) {
        this.optionAvatarStyle = optionAvatarStyle;
        pollAnswerAdapter.setOptionAvatarStyle(optionAvatarStyle);
    }

    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the poll question.
     *
     * <p>
     * This method updates the text color used for displaying the poll question.
     *
     * @param titleTextColor The color to set for the title text, specified as an integer
     *                       resource.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        question.setTextColor(titleTextColor);
    }

    public @ColorInt int getOptionTextColor() {
        return optionTextColor;
    }

    /**
     * Sets the text color for poll option text.
     *
     * <p>
     * This method updates the text color used for displaying the options in the
     * poll.
     *
     * @param optionTextColor The color to set for the option text, specified as an integer
     *                        resource.
     */
    public void setOptionTextColor(@ColorInt int optionTextColor) {
        this.optionTextColor = optionTextColor;
        pollAnswerAdapter.setOptionTextColor(optionTextColor);
    }

    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance for the poll question.
     *
     * <p>
     * This method updates the text appearance style for displaying the poll
     * question.
     *
     * @param titleTextAppearance The resource ID of the text appearance style to apply to the
     *                            title.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        question.setTextAppearance(titleTextAppearance);
    }

    public @StyleRes int getOptionTextAppearance() {
        return optionTextAppearance;
    }

    /**
     * Sets the text appearance for poll option text.
     *
     * <p>
     * This method updates the text appearance style for displaying the options in
     * the poll.
     *
     * @param optionTextAppearance The resource ID of the text appearance style to apply to the
     *                             options.
     */
    public void setOptionTextAppearance(@StyleRes int optionTextAppearance) {
        this.optionTextAppearance = optionTextAppearance;
        pollAnswerAdapter.setOptionTextAppearance(optionTextAppearance);
    }

    public @StyleRes int getVoteCountTextAppearance() {
        return voteCountTextAppearance;
    }

    /**
     * Sets the text appearance for the vote count display.
     *
     * <p>
     * This method updates the text appearance style for displaying the number of
     * votes for an option.
     *
     * @param voteCountTextAppearance The resource ID of the text appearance style to apply to the vote
     *                                count.
     */
    public void setVoteCountTextAppearance(@StyleRes int voteCountTextAppearance) {
        this.voteCountTextAppearance = voteCountTextAppearance;
        pollAnswerAdapter.setVoteCountTextAppearance(voteCountTextAppearance);
    }

    public @ColorInt int getUnselectedIconTint() {
        return unselectedIconTint;
    }

    /**
     * Sets the tint color for the unselected icon.
     *
     * <p>
     * This method updates the tint color applied to the icon of unselected options
     * in the poll.
     *
     * @param unselectedIconTint The color to set for the unselected icon, specified as an integer
     *                           resource.
     */
    public void setUnselectedIconTint(@ColorInt int unselectedIconTint) {
        this.unselectedIconTint = unselectedIconTint;
        pollAnswerAdapter.setUnselectedIconTint(unselectedIconTint);
    }

    public @ColorInt int getUnselectedRadioButtonStrokeColor() {
        return unselectedRadioButtonStrokeColor;
    }

    /**
     * Sets the stroke color for the unselected radio button.
     *
     * <p>
     * This method updates the stroke color applied to the radio button of
     * unselected options.
     *
     * @param unselectedRadioButtonStrokeColor The color to set for the unselected radio button stroke, specified
     *                                         as an integer resource.
     */
    public void setUnselectedRadioButtonStrokeColor(@ColorInt int unselectedRadioButtonStrokeColor) {
        this.unselectedRadioButtonStrokeColor = unselectedRadioButtonStrokeColor;
        pollAnswerAdapter.setUnselectedRadioButtonStrokeColor(unselectedRadioButtonStrokeColor);
    }

    public @Dimension int getUnselectedRadioButtonCornerRadius() {
        return unselectedRadioButtonCornerRadius;
    }

    /**
     * Sets the corner radius for the unselected radio button.
     *
     * <p>
     * This method updates the corner radius of the unselected radio button's
     * drawable.
     *
     * @param unselectedRadioButtonCornerRadius The corner radius to set for the unselected radio button.
     */
    public void setUnselectedRadioButtonCornerRadius(@Dimension int unselectedRadioButtonCornerRadius) {
        this.unselectedRadioButtonCornerRadius = unselectedRadioButtonCornerRadius;
        pollAnswerAdapter.setUnselectedRadioButtonCornerRadius(unselectedRadioButtonCornerRadius);
    }

    public @Dimension int getUnselectedRadioButtonStrokeWidth() {
        return unselectedRadioButtonStrokeWidth;
    }

    /**
     * Sets the stroke width for the unselected radio button.
     *
     * <p>
     * This method updates the stroke width of the unselected radio button's
     * drawable.
     *
     * @param unselectedRadioButtonStrokeWidth The stroke width to set for the unselected radio button.
     */
    public void setUnselectedRadioButtonStrokeWidth(@Dimension int unselectedRadioButtonStrokeWidth) {
        this.unselectedRadioButtonStrokeWidth = unselectedRadioButtonStrokeWidth;
        pollAnswerAdapter.setUnselectedRadioButtonStrokeWidth(unselectedRadioButtonStrokeWidth);
    }

    public @ColorInt int getProgressIndeterminateTint() {
        return progressIndeterminateTint;
    }

    /**
     * Sets the tint color for the indeterminate progress indicator.
     *
     * <p>
     * This method updates the tint color used for the indeterminate state of the
     * progress indicator.
     *
     * @param color The color to set for the indeterminate progress indicator,
     *              specified as an integer resource.
     */
    public void setProgressIndeterminateTint(@ColorInt int color) {
        this.progressIndeterminateTint = color;
        pollAnswerAdapter.setProgressIndeterminateTint(color);
    }

    public @StyleRes int getStyle() {
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
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatPollBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public OnOptionClick getOnOptionClick() {
        return onOptionClick;
    }

    /**
     * Sets the listener for option click events.
     *
     * <p>
     * This method allows setting a listener to handle click events on poll options.
     *
     * @param onOptionClick The listener to set for option click events.
     */
    public void setOnOptionClick(OnOptionClick onOptionClick) {
        if (onOptionClick != null) {
            this.onOptionClick = onOptionClick;
        }
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
