package com.cometchat.uikit.kotlin.shared.mentions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.cometchat.uikit.core.mentions.MentionDetectionState
import com.cometchat.uikit.core.mentions.MentionDetector
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan

/**
 * TextWatcher that detects mention triggers in an EditText.
 * 
 * This class monitors text changes and cursor position to detect when
 * a user is typing a mention (e.g., @username).
 * 
 * @param editText The EditText to monitor
 * @param textFormatters List of text formatters to check for tracking characters
 * @param onMentionDetected Callback when a mention is detected or cleared
 */
class MentionTextWatcher(
    private val editText: EditText,
    private val textFormatters: List<CometChatTextFormatter>,
    private val onMentionDetected: (MentionDetectionResult) -> Unit
) : TextWatcher {

    companion object {
        private const val TAG = "MentionTextWatcher"
    }

    /**
     * Result of mention detection with formatter information.
     */
    data class MentionDetectionResult(
        val isActive: Boolean,
        val query: String,
        val triggerIndex: Int,
        val cursorPosition: Int,
        val formatter: CometChatTextFormatter?
    ) {
        companion object {
            val INACTIVE = MentionDetectionResult(
                isActive = false,
                query = "",
                triggerIndex = -1,
                cursorPosition = -1,
                formatter = null
            )
        }
    }

    private val detectorMap = mutableMapOf<Char, MentionDetector>()
    private var currentFormatter: CometChatTextFormatter? = null
    private var isProcessing = false
    
    /**
     * External flag to temporarily disable detection during mention insertion.
     * This prevents flicker when a mention is selected and inserted.
     */
    private var detectionEnabled = true

    init {
        android.util.Log.d(TAG, "init: creating detectors for ${textFormatters.size} formatters")
        // Create detectors for each formatter's tracking character
        textFormatters.forEach { formatter ->
            val trackingChar = formatter.getTrackingCharacter()
            android.util.Log.d(TAG, "init: formatter=${formatter.javaClass.simpleName}, trackingChar='$trackingChar'")
            if (!detectorMap.containsKey(trackingChar)) {
                detectorMap[trackingChar] = MentionDetector(trackingChar)
            }
        }
        android.util.Log.d(TAG, "init: created ${detectorMap.size} detectors")
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not used
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Not used
    }

    override fun afterTextChanged(s: Editable?) {
        if (isProcessing || !detectionEnabled) return
        isProcessing = true

        try {
            val text = s?.toString() ?: ""
            val cursorPosition = editText.selectionStart
            
            android.util.Log.d(TAG, "afterTextChanged: text='$text', cursorPosition=$cursorPosition")

            // Check if cursor is inside a NonEditableSpan - if so, don't detect mentions
            val editable = editText.text
            if (editable != null && cursorPosition >= 0) {
                val spansAtCursor = editable.getSpans(cursorPosition, cursorPosition, NonEditableSpan::class.java)
                if (spansAtCursor.isNotEmpty()) {
                    android.util.Log.d(TAG, "afterTextChanged: cursor is inside a span, skipping detection")
                    onMentionDetected(MentionDetectionResult.INACTIVE)
                    return
                }
            }

            // Try to detect mention with each formatter
            var detectionResult = MentionDetectionResult.INACTIVE

            for (formatter in textFormatters) {
                if (formatter.getDisableSuggestions()) {
                    android.util.Log.d(TAG, "afterTextChanged: formatter ${formatter.javaClass.simpleName} has suggestions disabled, skipping")
                    continue
                }

                val trackingChar = formatter.getTrackingCharacter()
                val detector = detectorMap[trackingChar]
                if (detector == null) {
                    android.util.Log.d(TAG, "afterTextChanged: no detector for trackingChar='$trackingChar'")
                    continue
                }

                // Check if the detected trigger index is inside a span
                val result = detector.detectMention(text, cursorPosition)
                android.util.Log.d(TAG, "afterTextChanged: detector result isActive=${result.isActive}, query='${result.query}', triggerIndex=${result.triggerIndex}")
                
                if (result.isActive && editable != null) {
                    // Verify the trigger character is not inside a span
                    val spansAtTrigger = editable.getSpans(result.triggerIndex, result.triggerIndex + 1, NonEditableSpan::class.java)
                    if (spansAtTrigger.isNotEmpty()) {
                        android.util.Log.d(TAG, "afterTextChanged: trigger is inside a span, skipping")
                        continue
                    }
                }
                
                if (result.isActive) {
                    currentFormatter = formatter
                    detectionResult = MentionDetectionResult(
                        isActive = true,
                        query = result.query,
                        triggerIndex = result.triggerIndex,
                        cursorPosition = result.cursorPosition,
                        formatter = formatter
                    )
                    android.util.Log.d(TAG, "afterTextChanged: mention detected! query='${result.query}', formatter=${formatter.javaClass.simpleName}")
                    break
                }
            }

            if (!detectionResult.isActive) {
                currentFormatter = null
                android.util.Log.d(TAG, "afterTextChanged: no mention detected")
            }

            onMentionDetected(detectionResult)
        } finally {
            isProcessing = false
        }
    }

    /**
     * Gets the currently active formatter.
     */
    fun getCurrentFormatter(): CometChatTextFormatter? = currentFormatter

    /**
     * Enables or disables mention detection.
     * 
     * Use this to temporarily disable detection during mention insertion
     * to prevent flicker caused by the TextWatcher triggering again
     * when the mention text is inserted.
     * 
     * @param enabled true to enable detection, false to disable
     */
    fun setDetectionEnabled(enabled: Boolean) {
        detectionEnabled = enabled
        android.util.Log.d(TAG, "setDetectionEnabled: $enabled")
    }

    /**
     * Manually triggers mention detection.
     * Useful when cursor position changes without text change.
     */
    fun checkMention() {
        if (!detectionEnabled) return
        
        val text = editText.text?.toString() ?: ""
        val cursorPosition = editText.selectionStart
        val editable = editText.text

        // Check if cursor is inside a NonEditableSpan
        if (editable != null && cursorPosition >= 0) {
            val spansAtCursor = editable.getSpans(cursorPosition, cursorPosition, NonEditableSpan::class.java)
            if (spansAtCursor.isNotEmpty()) {
                onMentionDetected(MentionDetectionResult.INACTIVE)
                return
            }
        }

        var detectionResult = MentionDetectionResult.INACTIVE

        for (formatter in textFormatters) {
            if (formatter.getDisableSuggestions()) continue

            val trackingChar = formatter.getTrackingCharacter()
            val detector = detectorMap[trackingChar] ?: continue

            val result = detector.detectMention(text, cursorPosition)
            
            // Check if trigger is inside a span
            if (result.isActive && editable != null) {
                val spansAtTrigger = editable.getSpans(result.triggerIndex, result.triggerIndex + 1, NonEditableSpan::class.java)
                if (spansAtTrigger.isNotEmpty()) continue
            }
            
            if (result.isActive) {
                currentFormatter = formatter
                detectionResult = MentionDetectionResult(
                    isActive = true,
                    query = result.query,
                    triggerIndex = result.triggerIndex,
                    cursorPosition = result.cursorPosition,
                    formatter = formatter
                )
                break
            }
        }

        if (!detectionResult.isActive) {
            currentFormatter = null
        }

        onMentionDetected(detectionResult)
    }
}

/**
 * Extension function to add mention detection to an EditText.
 */
fun EditText.addMentionDetection(
    textFormatters: List<CometChatTextFormatter>,
    onMentionDetected: (MentionTextWatcher.MentionDetectionResult) -> Unit
): MentionTextWatcher {
    val watcher = MentionTextWatcher(this, textFormatters, onMentionDetected)
    addTextChangedListener(watcher)
    return watcher
}
