package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.cometchat.cards.CometChatCardComposable
import com.cometchat.cards.models.CometChatCardThemeMode
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent

/**
 * A composable that displays a developer card message bubble (category "card").
 *
 * Renders the card payload from [CardMessage.getCard] using [CometChatCardComposable]
 * from the cards renderer library. The UI Kit performs no transformation on the card JSON.
 *
 * Action callback is set up to emit [CometChatUIEvent.CardActionClicked] on the event bus
 * so the app can handle card actions via global subscription.
 *
 * Fallback order when the card payload is empty:
 * getFallbackText() → getText() → "Card Message" string resource.
 *
 * This is a first-party bubble registered in [InternalContentRenderer] — it is NOT
 * piped in via app-extension hooks. It sits in the same standard bubble container as
 * [CometChatTextBubble] with receipts, reactions, reply, thread, and container styling.
 *
 * @param message The [CardMessage] to render
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param onCardAction Optional callback for direct card action handling (developer card prop path)
 */
@Composable
fun CometChatCardBubble(
    message: CardMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    onCardAction: ((CardMessage, Any) -> Unit)? = null
) {
    val cardRaw = message.card
    val cardJson = cardRaw?.toString() ?: ""

    if (cardJson.isEmpty()) {
        // Fallback: getFallbackText() -> getText() -> "Card Message"
        val fallbackText = message.fallbackText?.ifEmpty { null }
            ?: message.text?.ifEmpty { null }
            ?: stringResource(R.string.cometchat_message_card)

        Text(
            text = fallbackText,
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorPrimary,
            modifier = modifier.padding(12.dp)
        )
        return
    }

    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.75).dp

    Box(modifier = modifier.widthIn(max = maxWidth)) {
        CometChatCardComposable(
            cardJson = cardJson,
            themeMode = CometChatCardThemeMode.AUTO,
            onAction = { event ->
                // Fire both channels: prop callback AND event bus (§2.6.1)
                onCardAction?.invoke(message, event)
                CometChatEvents.emitUIEvent(
                    CometChatUIEvent.CardActionClicked(
                        message = message,
                        actionEvent = event
                    )
                )
            }
        )
    }
}
