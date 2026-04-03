package com.cometchat.uikit.core.mentions

import android.content.Context
import android.text.Editable
import android.text.Spannable
import com.cometchat.chat.models.BaseMessage

/**
 * TextFormatterProcessor handles processing of text formatters before sending messages.
 * 
 * This utility processes all registered text formatters to:
 * 1. Call handlePreMessageSend on each formatter
 * 2. Replace spans with their underlying text representation
 * 3. Prepare the final message text for the SDK
 */
object TextFormatterProcessor {

    /**
     * Result of text processing.
     * 
     * @property processedText The text after processing all formatters
     * @property originalText The original text before processing
     */
    data class ProcessingResult(
        val processedText: String,
        val originalText: String
    )

    /**
     * Processes text by replacing span text with underlying text.
     * 
     * This method iterates through all spans in the text and replaces
     * the visible text with the underlying text stored in each span.
     * 
     * @param text The text to process
     * @param spanToUnderlyingMap Map of span positions to underlying text
     * @return ProcessingResult with the processed text
     */
    fun processSpans(
        text: String,
        spanToUnderlyingMap: Map<IntRange, String>
    ): ProcessingResult {
        if (spanToUnderlyingMap.isEmpty()) {
            return ProcessingResult(text, text)
        }

        // Sort spans by position (reverse order for replacement)
        val sortedSpans = spanToUnderlyingMap.entries.sortedByDescending { it.key.first }

        val result = StringBuilder(text)
        for ((range, underlying) in sortedSpans) {
            if (range.first >= 0 && range.last < result.length) {
                result.replace(range.first, range.last + 1, underlying)
            }
        }

        return ProcessingResult(
            processedText = result.toString(),
            originalText = text
        )
    }

    /**
     * Processes text using a list of prompt-to-underlying mappings.
     * 
     * @param text The text to process
     * @param promptToUnderlyingMap Map of prompt text to underlying text
     * @return ProcessingResult with the processed text
     */
    fun processPrompts(
        text: String,
        promptToUnderlyingMap: Map<String, String>
    ): ProcessingResult {
        if (promptToUnderlyingMap.isEmpty()) {
            return ProcessingResult(text, text)
        }

        var result = text
        for ((prompt, underlying) in promptToUnderlyingMap) {
            result = result.replace(prompt, underlying)
        }

        return ProcessingResult(
            processedText = result,
            originalText = text
        )
    }
}

/**
 * Interface for text formatter processing callbacks.
 */
interface TextFormatterProcessorCallback {
    /**
     * Called before processing formatters.
     * 
     * @param text The original text
     */
    fun onBeforeProcessing(text: String)

    /**
     * Called after processing formatters.
     * 
     * @param result The processing result
     */
    fun onAfterProcessing(result: TextFormatterProcessor.ProcessingResult)
}

/**
 * Manager for coordinating text formatter processing.
 * 
 * This class coordinates the processing of multiple text formatters
 * and provides hooks for pre and post processing.
 */
class TextFormatterProcessingManager {
    private var callback: TextFormatterProcessorCallback? = null

    /**
     * Sets the processing callback.
     */
    fun setCallback(callback: TextFormatterProcessorCallback?) {
        this.callback = callback
    }

    /**
     * Processes text with the given prompt-to-underlying mappings.
     * 
     * @param text The text to process
     * @param promptToUnderlyingMap Map of prompt text to underlying text
     * @return The processed text
     */
    fun process(
        text: String,
        promptToUnderlyingMap: Map<String, String>
    ): String {
        callback?.onBeforeProcessing(text)

        val result = TextFormatterProcessor.processPrompts(text, promptToUnderlyingMap)

        callback?.onAfterProcessing(result)

        return result.processedText
    }

    /**
     * Processes text with span position mappings.
     * 
     * @param text The text to process
     * @param spanToUnderlyingMap Map of span positions to underlying text
     * @return The processed text
     */
    fun processWithSpans(
        text: String,
        spanToUnderlyingMap: Map<IntRange, String>
    ): String {
        callback?.onBeforeProcessing(text)

        val result = TextFormatterProcessor.processSpans(text, spanToUnderlyingMap)

        callback?.onAfterProcessing(result)

        return result.processedText
    }
}
