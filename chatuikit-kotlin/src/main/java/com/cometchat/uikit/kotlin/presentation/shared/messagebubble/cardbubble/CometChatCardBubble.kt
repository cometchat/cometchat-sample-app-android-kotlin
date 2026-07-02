package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.cometchat.cards.CometChatCardView
import com.cometchat.cards.actions.CometChatCardActionCallback
import com.cometchat.cards.models.CometChatCardThemeMode
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * First-party card message bubble view for developer cards (category "card").
 *
 * Renders the card payload from [CardMessage.getCard] using the [CometChatCardView]
 * renderer library. The UI Kit performs no transformation on the card JSON.
 *
 * Action callback is set BEFORE assigning the card schema (assigning the schema
 * triggers a render). Actions are forwarded via [CometChatUIEvent.CardActionClicked]
 * on the event bus — the kit implements no action behavior itself.
 *
 * Mirrors the standard bubble container pattern of [CometChatTextBubble] — same
 * receipts, reactions, reply, thread, and container styling.
 */
class CometChatCardBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardView: CometChatCardView? = null

    /**
     * Maximum width (in pixels) for card content rendered inside this bubble.
     * Calculated as ~75% of the screen width to prevent full-width expansion
     * caused by WebView-based CometChatCardView having no intrinsic width.
     */
    private val maxCardWidth: Int by lazy {
        val screenWidth = resources.displayMetrics.widthPixels
        (screenWidth * 0.75).toInt()
    }

    init {
        // Ensure wrap_content layoutParams so handleView() in CometChatMessageBubble
        // doesn't default to MATCH_PARENT when lp is null.
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Binds a [CardMessage] to this bubble view.
     *
     * @param message The CardMessage to render
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     */
    fun setMessage(message: CardMessage, alignment: UIKitConstants.MessageBubbleAlignment) {
        removeAllViews()

        val cardRaw = message.card
        val cardJson = cardRaw?.toString() ?: ""

        if (cardJson.isEmpty()) {
            // Fallback: getFallbackText() -> getText() -> "Card Message"
            val fallbackText = message.fallbackText?.ifEmpty { null }
                ?: message.text?.ifEmpty { null }
                ?: context.getString(R.string.cometchat_message_card)

            val textView = TextView(context).apply {
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = fallbackText
                setTextColor(CometChatTheme.getTextColorPrimary(context))
                textSize = 14f
                val padding = (12 * resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
            }
            addView(textView)
            return
        }

        cardView = CometChatCardView(context).apply {
            layoutParams = LayoutParams(
                maxCardWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setThemeMode(CometChatCardThemeMode.AUTO)
            // Action callback MUST be set BEFORE card schema (assigning schema triggers render)
            setActionCallback(CometChatCardActionCallback { event ->
                CometChatEvents.emitUIEvent(
                    CometChatUIEvent.CardActionClicked(
                        message = message,
                        actionEvent = event
                    )
                )
            })
            setCardSchema(cardJson)
        }
        addView(cardView)
    }
}
