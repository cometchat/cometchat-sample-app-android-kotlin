package com.cometchat.uikit.compose.presentation.shared.formatters

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Utility class for formatting text with multiple formatters.
 * Applies a list of formatters sequentially to a given text.
 */
object FormatterUtils {
    private const val TAG = "FormatterUtils"

    /**
     * Gets formatted text by applying a list of formatters to the input text.
     * Returns an AnnotatedString with styling applied for mentions and other formatted elements.
     *
     * @param context The Android context
     * @param baseMessage The base message to format
     * @param formattingType The type of formatting to apply
     * @param alignment The message bubble alignment
     * @param text The original text to format
     * @param formatters The list of formatters to apply sequentially
     * @return The formatted AnnotatedString after applying all formatters
     */
    fun getFormattedText(
        context: Context,
        baseMessage: BaseMessage,
        formattingType: UIKitConstants.FormattingType,
        alignment: UIKitConstants.MessageBubbleAlignment,
        text: String,
        formatters: List<CometChatTextFormatter>
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString(text)

        android.util.Log.d(TAG, "getFormattedText: text='$text', messageId=${baseMessage.id}, muid=${baseMessage.muid}")
        android.util.Log.d(TAG, "getFormattedText: mentionedUsers=${baseMessage.mentionedUsers?.map { it.uid }}")
        android.util.Log.d(TAG, "getFormattedText: formatters count=${formatters.size}")

        var formattedText: AnnotatedString = AnnotatedString(text)
        for (textFormatter in formatters) {
            android.util.Log.d(TAG, "getFormattedText: applying formatter ${textFormatter.javaClass.simpleName}")
            formattedText = textFormatter.prepareMessageString(
                context,
                baseMessage,
                formattedText,
                alignment,
                formattingType
            )
            android.util.Log.d(TAG, "getFormattedText: formatter result='${formattedText.text}'")
        }

        android.util.Log.d(TAG, "getFormattedText: final result='${formattedText.text}'")
        return formattedText
    }
}
