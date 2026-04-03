package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject

/**
 * Interface for handling poll option click events.
 */
fun interface OnOptionClick {
    /**
     * Called when a poll option is clicked.
     *
     * @param baseMessage The CustomMessage containing the poll
     * @param selectedOption The text of the selected option
     * @param position The 0-indexed position of the clicked option
     */
    fun onClick(baseMessage: CustomMessage, selectedOption: String, position: Int)
}

/**
 * A custom view that displays an interactive poll message bubble.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 * The UI matches the reference implementation in chatuikit (Java).
 *
 * Layout structure:
 * - Question text at top
 * - RecyclerView with poll options
 * - Each option row: RadioButton → OptionText → VoterAvatars+Count
 * - Progress bar below each option
 *
 * Features:
 * - Display poll question as the title
 * - Display all poll options in a RecyclerView
 * - Show unselected radio buttons when user hasn't voted
 * - Show progress bars with vote percentages when votes exist
 * - Display voter avatars and count for each option
 * - Highlight user's selected option with distinct styling
 * - Handle click listener for voting
 *
 * Example usage:
 * ```kotlin
 * val pollBubble = CometChatPollBubble(context)
 * pollBubble.setMessage(customMessage)
 * pollBubble.setOnOptionClick { message, option, position ->
 *     // Handle vote for option at position
 * }
 * ```
 */
class CometChatPollBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatPollBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val question: TextView
    private val pollAnswerAdapter: PollAnswerAdapter

    // Single style object - nullable during initialization to handle parent constructor calls
    private var style: CometChatPollBubbleStyle? = null

    // State
    private var jsonObject: org.json.JSONObject? = null

    private var onOptionClick: OnOptionClick? = null

    init {
        Utils.initMaterialCard(this)
        
        // Set fixed width to match Java reference: 240dp (cometchat_poll_bubble_layout_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            LayoutParams.WRAP_CONTENT
        )
        
        LayoutInflater.from(context).inflate(R.layout.cometchat_message_polls_bubble, this, true)
        question = findViewById(R.id.tv_question)
        val optionsRecyclerView: RecyclerView = findViewById(R.id.rv_options)
        optionsRecyclerView.layoutManager = LinearLayoutManager(context)
        pollAnswerAdapter = PollAnswerAdapter { baseMessage, selectedOption, position ->
            // Call external listener if set
            onOptionClick?.onClick(baseMessage, selectedOption, position)
            
            // Default vote behavior: submit vote to CometChat
            submitVote(baseMessage, position)
        }
        optionsRecyclerView.adapter = pollAnswerAdapter

        // Set long click listener to propagate to parent for message actions
        setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }

        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Extracts style attributes from XML and applies them.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatPollBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatPollBubble_cometchatPollBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatPollBubble, defStyleAttr, styleResId
        )
        extractAttributesAndApplyDefaults(typedArray)
    }

    /**
     * Extracts attributes from TypedArray and applies defaults.
     */
    private fun extractAttributesAndApplyDefaults(typedArray: android.content.res.TypedArray) {
        try {
            setProgressColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressColor,
                    com.cometchat.uikit.kotlin.theme.CometChatTheme.getPrimaryColor(context)
                )
            )
            setProgressBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressBackgroundColor,
                    com.cometchat.uikit.kotlin.theme.CometChatTheme.getExtendedPrimaryColor700(context)
                )
            )
            setSelectedStateDrawable(
                typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedStateDrawable)
            )
            setUnselectedStateDrawable(
                typedArray.getDrawable(R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedStateDrawable)
            )
            setSelectedIconTint(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedIconTint,
                    com.cometchat.uikit.kotlin.theme.CometChatTheme.getPrimaryColor(context)
                )
            )
            setSelectedRadioButtonStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeColor,
                    0
                )
            )
            setVoteCountTextColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextColor,
                    0
                )
            )
            setSelectedRadioButtonCornerRadius(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonCornerRadius,
                    0
                )
            )
            setSelectedRadioButtonStrokeWidth(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleSelectedRadioButtonStrokeWidth,
                    0
                )
            )
            setUnselectedRadioButtonStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeColor,
                    com.cometchat.uikit.kotlin.theme.CometChatTheme.getIconTintSecondary(context)
                )
            )
            setUnselectedIconTint(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedIconTint,
                    0
                )
            )
            setUnselectedRadioButtonCornerRadius(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonCornerRadius,
                    0
                )
            )
            setUnselectedRadioButtonStrokeWidth(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleUnselectedRadioButtonStrokeWidth,
                    0
                )
            )
            setOptionAvatarStyle(
                typedArray.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionAvatarStyle,
                    0
                )
            )
            setTitleTextColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextColor,
                    0
                )
            )
            setOptionTextColor(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextColor,
                    0
                )
            )
            setTitleTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleTitleTextAppearance,
                    0
                )
            )
            setOptionTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleOptionTextAppearance,
                    0
                )
            )
            setVoteCountTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleVoteCountTextAppearance,
                    0
                )
            )
            setProgressIndeterminateTint(
                typedArray.getColor(
                    R.styleable.CometChatPollBubble_cometchatPollBubbleProgressIndeterminateTint,
                    com.cometchat.uikit.kotlin.theme.CometChatTheme.getIconTintSecondary(context)
                )
            )
        } finally {
            typedArray.recycle()
        }
    }

    // ========================================
    // Public Content Methods
    // ========================================

    /**
     * Sets the poll message data to be displayed.
     *
     * This method updates the poll bubble with the information contained in the
     * provided CustomMessage. It extracts the question and options from the
     * custom data and updates the UI accordingly.
     *
     * @param baseMessage The CustomMessage containing the poll data
     */
    fun setMessage(baseMessage: CustomMessage?) {
        if (baseMessage != null) {
            jsonObject = baseMessage.customData
            try {
                val options = jsonObject?.getJSONObject("options")
                question.text = jsonObject?.getString("question") ?: ""
                val myVotedPosition = userVotedOn(baseMessage, options?.length() ?: 0)
                pollAnswerAdapter.setMessage(baseMessage)
                pollAnswerAdapter.setMyChosenOptionPosition(myVotedPosition - 1)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Determines which option the current user voted on.
     *
     * @param message The poll message
     * @param optionsCount The number of options
     * @return The 1-indexed position of the voted option, or 0 if not voted
     */
    private fun userVotedOn(message: CustomMessage, optionsCount: Int): Int {
        try {
            // Get logged-in user UID
            val loggedInUserId = try {
                CometChatUIKit.getLoggedInUser()?.uid
            } catch (e: Exception) {
                null
            } ?: return 0
            
            val metadata = message.metadata ?: return 0
            if (!metadata.has("@injected")) return 0

            val injected = metadata.getJSONObject("@injected")
            if (!injected.has("extensions")) return 0

            val extensions = injected.getJSONObject("extensions")
            if (!extensions.has("polls")) return 0

            val polls = extensions.getJSONObject("polls")
            if (!polls.has("results")) return 0

            val results = polls.getJSONObject("results")
            if (!results.has("options")) return 0

            val options = results.getJSONObject("options")

            // Check each option for the current user's vote
            for (i in 1..optionsCount) {
                val optionKey = i.toString()
                if (options.has(optionKey)) {
                    val option = options.getJSONObject(optionKey)
                    if (option.has("voters") && option.get("voters") is JSONObject) {
                        val voters = option.getJSONObject("voters")
                        if (voters.has(loggedInUserId)) {
                            return i
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user vote: ${e.message}")
        }
        return 0
    }

    /**
     * Submits a vote to the CometChat polls extension.
     *
     * @param message The poll message
     * @param position The 0-indexed position of the selected option
     */
    private fun submitVote(message: CustomMessage, position: Int) {
        try {
            // Extract poll ID from customData, fallback to message ID
            val pollId = message.customData?.optString("id")?.takeIf { it.isNotEmpty() }
                ?: message.id.toString()
            
            // Construct vote payload - API expects 1-indexed position
            val votePayload = JSONObject().apply {
                put("vote", position + 1)
                put("id", pollId)
            }
            
            // Submit vote via CometChat extension API
            CometChat.callExtension(
                "polls",
                "POST",
                "/v2/vote",
                votePayload,
                object : CometChat.CallbackListener<JSONObject>() {
                    override fun onSuccess(response: JSONObject?) {
                        Log.d(TAG, "Vote submitted successfully")
                    }
                    
                    override fun onError(e: CometChatException?) {
                        Log.e(TAG, "Failed to submit vote: ${e?.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting vote: ${e.message}")
        }
    }

    fun getJsonObject(): org.json.JSONObject? = jsonObject

    fun getOnOptionClick(): OnOptionClick? = onOptionClick

    /**
     * Sets the listener for option click events.
     */
    fun setOnOptionClick(listener: OnOptionClick?) {
        if (listener != null) {
            this.onOptionClick = listener
        }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatPollBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatPollBubble
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    private fun applyStyle() {
        val currentStyle = style ?: return

        // Title styling
        if (currentStyle.titleTextColor != 0) question.setTextColor(currentStyle.titleTextColor)
        if (currentStyle.titleTextAppearance != 0) question.setTextAppearance(currentStyle.titleTextAppearance)

        // Pass style properties to adapter
        pollAnswerAdapter.setProgressColor(currentStyle.progressColor)
        pollAnswerAdapter.setProgressBackgroundColor(currentStyle.progressBackgroundColor)
        pollAnswerAdapter.setSelectedStateDrawable(currentStyle.selectedStateDrawable)
        pollAnswerAdapter.setUnselectedStateDrawable(currentStyle.unselectedStateDrawable)
        pollAnswerAdapter.setSelectedIconTint(currentStyle.selectedIconTint)
        pollAnswerAdapter.setSelectedRadioButtonStrokeColor(currentStyle.selectedRadioButtonStrokeColor)
        pollAnswerAdapter.setSelectedRadioButtonCornerRadius(currentStyle.selectedRadioButtonCornerRadius)
        pollAnswerAdapter.setSelectedRadioButtonStrokeWidth(currentStyle.selectedRadioButtonStrokeWidth)
        pollAnswerAdapter.setUnselectedIconTint(currentStyle.unselectedIconTint)
        pollAnswerAdapter.setUnselectedRadioButtonStrokeColor(currentStyle.unselectedRadioButtonStrokeColor)
        pollAnswerAdapter.setUnselectedRadioButtonCornerRadius(currentStyle.unselectedRadioButtonCornerRadius)
        pollAnswerAdapter.setUnselectedRadioButtonStrokeWidth(currentStyle.unselectedRadioButtonStrokeWidth)
        pollAnswerAdapter.setOptionTextColor(currentStyle.optionTextColor)
        pollAnswerAdapter.setOptionTextAppearance(currentStyle.optionTextAppearance)
        pollAnswerAdapter.setVoteCountTextColor(currentStyle.voteCountTextColor)
        pollAnswerAdapter.setVoteCountTextAppearance(currentStyle.voteCountTextAppearance)
        pollAnswerAdapter.setOptionAvatarStyle(currentStyle.optionAvatarStyle)
        pollAnswerAdapter.setProgressIndeterminateTint(currentStyle.progressIndeterminateTint)
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getProgressColor(): Int = style?.progressColor ?: 0
    fun getProgressBackgroundColor(): Int = style?.progressBackgroundColor ?: 0
    fun getSelectedStateDrawable(): Drawable? = style?.selectedStateDrawable
    fun getUnselectedStateDrawable(): Drawable? = style?.unselectedStateDrawable
    fun getVoteCountTextColor(): Int = style?.voteCountTextColor ?: 0
    fun getSelectedRadioButtonStrokeColor(): Int = style?.selectedRadioButtonStrokeColor ?: 0
    fun getSelectedIconTint(): Int = style?.selectedIconTint ?: 0
    fun getSelectedRadioButtonCornerRadius(): Int = style?.selectedRadioButtonCornerRadius ?: 0
    fun getSelectedRadioButtonStrokeWidth(): Int = style?.selectedRadioButtonStrokeWidth ?: 0
    fun getUnselectedRadioButtonStrokeColor(): Int = style?.unselectedRadioButtonStrokeColor ?: 0
    fun getUnselectedIconTint(): Int = style?.unselectedIconTint ?: 0
    fun getUnselectedRadioButtonCornerRadius(): Int = style?.unselectedRadioButtonCornerRadius ?: 0
    fun getUnselectedRadioButtonStrokeWidth(): Int = style?.unselectedRadioButtonStrokeWidth ?: 0
    fun getOptionAvatarStyle(): Int = style?.optionAvatarStyle ?: 0
    fun getTitleTextColor(): Int = style?.titleTextColor ?: 0
    fun getOptionTextColor(): Int = style?.optionTextColor ?: 0
    fun getTitleTextAppearance(): Int = style?.titleTextAppearance ?: 0
    fun getOptionTextAppearance(): Int = style?.optionTextAppearance ?: 0
    fun getVoteCountTextAppearance(): Int = style?.voteCountTextAppearance ?: 0
    fun getProgressIndeterminateTint(): Int = style?.progressIndeterminateTint ?: 0

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setProgressColor(@ColorInt color: Int) {
        style = style?.copy(progressColor = color) ?: CometChatPollBubbleStyle(progressColor = color)
        pollAnswerAdapter.setProgressColor(color)
    }

    fun setProgressBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(progressBackgroundColor = color) ?: CometChatPollBubbleStyle(progressBackgroundColor = color)
        pollAnswerAdapter.setProgressBackgroundColor(color)
    }

    fun setSelectedStateDrawable(drawable: Drawable?) {
        style = style?.copy(selectedStateDrawable = drawable) ?: CometChatPollBubbleStyle(selectedStateDrawable = drawable)
        pollAnswerAdapter.setSelectedStateDrawable(drawable)
    }

    fun setUnselectedStateDrawable(drawable: Drawable?) {
        style = style?.copy(unselectedStateDrawable = drawable) ?: CometChatPollBubbleStyle(unselectedStateDrawable = drawable)
        pollAnswerAdapter.setUnselectedStateDrawable(drawable)
    }

    fun setSelectedIconTint(@ColorInt color: Int) {
        style = style?.copy(selectedIconTint = color) ?: CometChatPollBubbleStyle(selectedIconTint = color)
        pollAnswerAdapter.setSelectedIconTint(color)
    }

    fun setSelectedRadioButtonStrokeColor(@ColorInt color: Int) {
        style = style?.copy(selectedRadioButtonStrokeColor = color) ?: CometChatPollBubbleStyle(selectedRadioButtonStrokeColor = color)
        pollAnswerAdapter.setSelectedRadioButtonStrokeColor(color)
    }

    fun setVoteCountTextColor(@ColorInt color: Int) {
        style = style?.copy(voteCountTextColor = color) ?: CometChatPollBubbleStyle(voteCountTextColor = color)
        pollAnswerAdapter.setVoteCountTextColor(color)
    }

    fun setSelectedRadioButtonCornerRadius(@Dimension radius: Int) {
        style = style?.copy(selectedRadioButtonCornerRadius = radius) ?: CometChatPollBubbleStyle(selectedRadioButtonCornerRadius = radius)
        pollAnswerAdapter.setSelectedRadioButtonCornerRadius(radius)
    }

    fun setSelectedRadioButtonStrokeWidth(@Dimension width: Int) {
        style = style?.copy(selectedRadioButtonStrokeWidth = width) ?: CometChatPollBubbleStyle(selectedRadioButtonStrokeWidth = width)
        pollAnswerAdapter.setSelectedRadioButtonStrokeWidth(width)
    }

    fun setUnselectedRadioButtonStrokeColor(@ColorInt color: Int) {
        style = style?.copy(unselectedRadioButtonStrokeColor = color) ?: CometChatPollBubbleStyle(unselectedRadioButtonStrokeColor = color)
        pollAnswerAdapter.setUnselectedRadioButtonStrokeColor(color)
    }

    fun setUnselectedIconTint(@ColorInt color: Int) {
        style = style?.copy(unselectedIconTint = color) ?: CometChatPollBubbleStyle(unselectedIconTint = color)
        pollAnswerAdapter.setUnselectedIconTint(color)
    }

    fun setUnselectedRadioButtonCornerRadius(@Dimension radius: Int) {
        style = style?.copy(unselectedRadioButtonCornerRadius = radius) ?: CometChatPollBubbleStyle(unselectedRadioButtonCornerRadius = radius)
        pollAnswerAdapter.setUnselectedRadioButtonCornerRadius(radius)
    }

    fun setUnselectedRadioButtonStrokeWidth(@Dimension width: Int) {
        style = style?.copy(unselectedRadioButtonStrokeWidth = width) ?: CometChatPollBubbleStyle(unselectedRadioButtonStrokeWidth = width)
        pollAnswerAdapter.setUnselectedRadioButtonStrokeWidth(width)
    }

    fun setOptionAvatarStyle(@StyleRes style: Int) {
        this.style = this.style?.copy(optionAvatarStyle = style) ?: CometChatPollBubbleStyle(optionAvatarStyle = style)
        pollAnswerAdapter.setOptionAvatarStyle(style)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style?.copy(titleTextColor = color) ?: CometChatPollBubbleStyle(titleTextColor = color)
        if (color != 0) question.setTextColor(color)
    }

    fun setOptionTextColor(@ColorInt color: Int) {
        style = style?.copy(optionTextColor = color) ?: CometChatPollBubbleStyle(optionTextColor = color)
        pollAnswerAdapter.setOptionTextColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(titleTextAppearance = appearance) ?: CometChatPollBubbleStyle(titleTextAppearance = appearance)
        if (appearance != 0) question.setTextAppearance(appearance)
    }

    fun setOptionTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(optionTextAppearance = appearance) ?: CometChatPollBubbleStyle(optionTextAppearance = appearance)
        pollAnswerAdapter.setOptionTextAppearance(appearance)
    }

    fun setVoteCountTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(voteCountTextAppearance = appearance) ?: CometChatPollBubbleStyle(voteCountTextAppearance = appearance)
        pollAnswerAdapter.setVoteCountTextAppearance(appearance)
    }

    fun setProgressIndeterminateTint(@ColorInt color: Int) {
        style = style?.copy(progressIndeterminateTint = color) ?: CometChatPollBubbleStyle(progressIndeterminateTint = color)
        pollAnswerAdapter.setProgressIndeterminateTint(color)
    }

    companion object {
        private val TAG = CometChatPollBubble::class.java.simpleName
    }
}
