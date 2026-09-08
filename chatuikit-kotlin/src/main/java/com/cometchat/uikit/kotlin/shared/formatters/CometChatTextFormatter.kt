package com.cometchat.uikit.kotlin.shared.formatters

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.lifecycle.MutableLiveData
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import org.json.JSONObject

/**
 * An abstract base class to format text in the CometChat module.
 * It provides fundamental methods to handle, format, and track changes of the text.
 * All concrete text formatter classes should extend this class and implement its abstract methods.
 */
abstract class CometChatTextFormatter(
    /**
     * The character for which suggestion search will be triggered.
     */
    private val trackingCharacter: Char
) : Formatter {
    companion object {
        private val TAG = CometChatTextFormatter::class.java.simpleName
    }

    private val suggestionItemList = MutableLiveData<List<SuggestionItem>>(emptyList())
    private val tagInfoMessage = MutableLiveData<String>("")
    private val tagInfoVisible = MutableLiveData<Boolean>(false)
    private val showLoadingIndicator = MutableLiveData<Boolean>(false)
    private val selectedSuggestionItemList = mutableListOf<SuggestionItem>()
    private var disableSuggestions = false
    private var user: User? = null
    private var group: Group? = null

    /**
     * To set the flag if the suggestions should be disabled or not.
     */
    protected fun setDisableSuggestions(disable: Boolean) {
        disableSuggestions = disable
    }

    /**
     * To set the list of suggestion items.
     */
    fun setSuggestionItemList(items: List<SuggestionItem>) {
        android.util.Log.d("MentionDebug", "[$TAG] setSuggestionItemList() - posting ${items.size} items to LiveData")
        android.util.Log.d("MentionDebug", "[$TAG] setSuggestionItemList() - items: ${items.map { it.name }}")
        suggestionItemList.postValue(items)
    }

    /**
     * To show or hide the loading indicator.
     */
    fun setShowLoadingIndicator(show: Boolean) {
        android.util.Log.d("MentionDebug", "[$TAG] setShowLoadingIndicator($show)")
        showLoadingIndicator.postValue(show)
    }

    /**
     * This method is used to search for suggestions based on the passed queryString.
     */
    abstract fun search(context: Context, queryString: String?)

    /**
     * This method is called when an item on the suggestion list is clicked.
     */
    open fun onItemClick(context: Context, suggestionItem: SuggestionItem, user: User?, group: Group?) {}

    /**
     * Method called before the message is sent.
     */
    open fun handlePreMessageSend(context: Context, baseMessage: BaseMessage) {}

    /**
     * This method is called whenever the user scrolls to the bottom of the suggestion list.
     */
    abstract fun onScrollToBottom()

    /**
     * Method to prepare the text of the message.
     */
    fun prepareMessageString(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder,
        messageBubbleAlignment: UIKitConstants.MessageBubbleAlignment,
        formattingType: UIKitConstants.FormattingType
    ): SpannableStringBuilder? {
        return when (formattingType) {
            UIKitConstants.FormattingType.MESSAGE_BUBBLE -> {
                if (messageBubbleAlignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
                    prepareRightMessageBubbleSpan(context, baseMessage, spannable)
                } else {
                    prepareLeftMessageBubbleSpan(context, baseMessage, spannable)
                }
            }
            UIKitConstants.FormattingType.CONVERSATIONS -> {
                prepareConversationSpan(context, baseMessage, spannable)
            }
            else -> {
                prepareComposerSpan(context, baseMessage, spannable)
            }
        }
    }

    /**
     * Method to prepare the text to be shown in the left message bubble.
     */
    open fun prepareLeftMessageBubbleSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = spannable

    /**
     * Method to prepare the text to be shown in the right message bubble.
     */
    open fun prepareRightMessageBubbleSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = spannable

    /**
     * Method to prepare the text to be shown in the composer.
     */
    open fun prepareComposerSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = spannable

    /**
     * Method to prepare the text to be shown in the conversation view.
     */
    open fun prepareConversationSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = spannable

    /**
     * Display span for the reply/edit PREVIEW panels — a read-only surface, distinct from the live
     * composer input. Defaults to [prepareComposerSpan] (so existing formatters are unaffected), but
     * a formatter whose composer span keeps a token intact for the input (e.g. a colour token) should
     * override this to strip the token and apply its style, so the preview shows styled text rather
     * than the raw token.
     */
    open fun preparePreviewSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = prepareComposerSpan(context, baseMessage, spannable)

    /**
     * Reverse of the `prepare*Span` rendering: strip THIS formatter's display markup out of [text],
     * returning the storable token form that goes on the wire (e.g. `"{color:#f00}hi{/color}"` from a
     * coloured span). Default is identity — mentions/URLs already keep their token in the text, so they
     * need no reverse step.
     *
     * A custom (Approach-2) formatter that renders its own inline style overrides this so its style
     * survives send: the token stays in the message text and re-renders via `prepare*Span` on every
     * surface. Call it when reading composer text whose display form differs from the stored token
     * (e.g. before send, or when populating the composer to edit an existing message).
     */
    open fun getOriginalText(text: String): String = text

    /**
     * Optional live-composer rendering: style THIS formatter's tokens directly on the editable input
     * (e.g. hide `{color:#…}`/`{/color}` markers with a zero-width span and colour the inner text).
     * The composer calls this on every text change for each formatter, after its built-in rich-text
     * and mention styling.
     *
     * Default is a no-op — the token then shows as plain source in the composer and only renders via
     * `prepare*Span` once sent. Apply spans only (do NOT delete the marker characters), so the token
     * stays in the [editable] and still goes on the wire. Implementations should remove their own
     * previously-applied spans first so repeated calls are idempotent.
     */
    open fun applyComposerSpans(editable: android.text.Editable) {}

    open fun observeSelectionList(context: Context, selectedSuggestionItemList: List<SuggestionItem>) {}

    /**
     * To set the selected list of suggestions.
     */
    fun setSelectedList(context: Context, selectedItems: List<SuggestionItem>) {
        selectedSuggestionItemList.clear()
        selectedSuggestionItemList.addAll(selectedItems)
        observeSelectionList(context, selectedItems)
    }

    /**
     * To set the tag information message.
     */
    fun setInfoText(message: String) {
        tagInfoMessage.postValue(message)
    }

    /**
     * To show or hide the tag information visibility.
     */
    fun setInfoVisibility(visible: Boolean) {
        tagInfoVisible.postValue(visible)
    }

    /**
     * Sets the group for this formatter.
     */
    open fun setGroup(group: Group?) {
        this.group = group
    }

    /**
     * Sets the user for this formatter.
     */
    open fun setUser(user: User?) {
        this.user = user
    }

    fun getSelectedList(): List<SuggestionItem> = selectedSuggestionItemList.toList()

    fun getSuggestionItemList(): MutableLiveData<List<SuggestionItem>> = suggestionItemList

    fun getTagInfoMessage(): MutableLiveData<String> = tagInfoMessage

    fun getTagInfoVisibility(): MutableLiveData<Boolean> = tagInfoVisible

    fun getShowLoadingIndicator(): MutableLiveData<Boolean> = showLoadingIndicator

    open fun getDisableSuggestions(): Boolean = disableSuggestions

    fun getTrackingCharacter(): Char = trackingCharacter

    fun getId(): Char = trackingCharacter

    fun getUser(): User? = user

    fun getGroup(): Group? = group
}

/**
 * Represents a suggestion item in the suggestion list.
 */
data class SuggestionItem(
    val id: String,
    val name: String,
    val leadingIconUrl: String? = null,
    val status: String? = null,
    val promptText: String,
    val underlyingText: String,
    val data: JSONObject? = null,
    val promptTextStyle: PromptTextStyle? = null,
    val leadingIcon: Int = 0,
    val hideLeadingIcon: Boolean = false,
    val leadingIconStyle: Int = 0
) {
    fun getPromptTextAppearance(): PromptTextStyle? = promptTextStyle
}
