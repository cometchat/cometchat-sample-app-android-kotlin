package com.cometchat.uikit.kotlin.shared.formatters

import android.content.Context
import android.text.SpannableStringBuilder
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Utility class for formatting text with multiple formatters.
 * Applies a list of formatters sequentially to a given text.
 */
object FormatterUtils {
    private const val TAG = "FormatterUtils"

    /**
     * Gets formatted text by applying a list of formatters to the input text.
     * Returns a SpannableStringBuilder with styling applied for mentions and other formatted elements.
     *
     * @param context The Android context
     * @param baseMessage The base message to format
     * @param formattingType The type of formatting to apply
     * @param alignment The message bubble alignment
     * @param text The original text to format
     * @param formatters The list of formatters to apply sequentially
     * @return The formatted CharSequence after applying all formatters
     */
    fun getFormattedText(
        context: Context,
        baseMessage: BaseMessage,
        formattingType: UIKitConstants.FormattingType,
        alignment: UIKitConstants.MessageBubbleAlignment,
        text: String,
        formatters: List<CometChatTextFormatter>
    ): CharSequence {
        if (text.isEmpty()) return text

        android.util.Log.d(TAG, "getFormattedText: text='$text', messageId=${baseMessage.id}, muid=${baseMessage.muid}")
        android.util.Log.d(TAG, "getFormattedText: mentionedUsers=${baseMessage.mentionedUsers?.map { it.uid }}")
        android.util.Log.d(TAG, "getFormattedText: formatters count=${formatters.size}")

        var spannable = SpannableStringBuilder(text)

        for (formatter in formatters) {
            android.util.Log.d(TAG, "getFormattedText: applying formatter ${formatter.javaClass.simpleName}")
            val result = formatter.prepareMessageString(
                context,
                baseMessage,
                spannable,
                alignment,
                formattingType
            )
            if (result != null) {
                android.util.Log.d(TAG, "getFormattedText: formatter result='${result}'")
                spannable = result
            }
        }

        android.util.Log.d(TAG, "getFormattedText: final result='$spannable'")
        return spannable
    }

    /**
     * Gets the plain text from a message.
     *
     * @param message The message to extract text from
     * @return The plain text content
     */
    fun getMessageText(message: BaseMessage): String {
        return when (message) {
            is TextMessage -> message.text ?: ""
            else -> ""
        }
    }
}
