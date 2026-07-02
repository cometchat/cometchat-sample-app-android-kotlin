package com.cometchat.uikit.compose.presentation.shared.messagebubble.aiassistantbubble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.AIAssistantBaseEvent
import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatAIStreamService
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.StreamMessage
import com.google.android.material.card.MaterialCardView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.table.TableEntry
import io.noties.markwon.recycler.table.TableEntryPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.markwon.utils.Dip
import io.noties.prism4j.Prism4j
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Node

/**
 * Composable that renders AI assistant messages with Markwon-based markdown.
 *
 * Supports two modes:
 * - **Streaming mode**: When [streamMessage] is provided, shows shimmer/thinking state,
 *   streams content in real-time via [CometChatAIStreamService], and handles interrupted state.
 * - **Static mode**: When [aiAssistantMessage] is provided, renders the final markdown
 *   content directly without streaming.
 *
 * Both modes share the same Markdown rendering pipeline and styling.
 *
 * @param modifier Modifier for the composable
 * @param streamMessage Optional StreamMessage for streaming mode
 * @param aiAssistantMessage Optional AIAssistantMessage for static mode
 * @param style Style configuration for the bubble
 * @param aiStreamService Optional AI stream service instance for streaming lifecycle management
 */
@Composable
fun CometChatAIAssistantBubble(
    modifier: Modifier = Modifier,
    streamMessage: StreamMessage? = null,
    aiAssistantMessage: AIAssistantMessage? = null,
    style: CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle.incoming(),
    aiStreamService: CometChatAIStreamService? = CometChatAIStreamService.getInstance()
) {
    when {
        streamMessage != null -> StreamingMode(
            message = streamMessage,
            style = style,
            aiStreamService = aiStreamService,
            modifier = modifier
        )
        aiAssistantMessage != null -> StaticMode(
            message = aiAssistantMessage,
            style = style,
            modifier = modifier
        )
    }
}

// ── Streaming Mode ──────────────────────────────────────────────────────

/**
 * Represents an ordered block in the streaming content.
 * Blocks are appended in arrival order to preserve the sequential rendering
 * of interleaved text and card content from the AI agent.
 */
private sealed class StreamedBlock {
    /** A text block whose markdown content grows as deltas arrive. */
    data class Text(val id: String, var markdown: String) : StreamedBlock()
    /** A card block in loading state (placeholder). */
    data class CardLoading(val cardId: String, val label: String) : StreamedBlock()
    /** A card block that has been fully received and is ready to render. */
    data class CardRendered(val cardId: String, val cardJson: String) : StreamedBlock()
}

@Composable
private fun StreamingMode(
    message: StreamMessage,
    style: CometChatAIAssistantBubbleStyle,
    aiStreamService: CometChatAIStreamService?,
    modifier: Modifier
) {
    val runId = message.runId

    // Ordered block list — preserves arrival order of text and card content
    val streamedBlocks = remember { mutableStateOf(listOf<StreamedBlock>()) }

    // Lifecycle: start/stop streaming
    DisposableEffect(runId, aiStreamService) {
        val listener = object : CometChatAIStreamService.AIStreamListener {
            override fun onAIAssistantEventReceived(event: AIAssistantBaseEvent) {
                when (event) {
                    is com.cometchat.chat.models.AIAssistantContentReceivedEvent -> {
                        val delta = event.delta ?: ""
                        if (delta.isNotEmpty()) {
                            val blocks = streamedBlocks.value.toMutableList()
                            val lastBlock = blocks.lastOrNull()
                            if (lastBlock is StreamedBlock.Text) {
                                // Append delta to the current text block
                                blocks[blocks.lastIndex] = lastBlock.copy(
                                    markdown = lastBlock.markdown + delta
                                )
                            } else {
                                // Start a new text block (first block, or after a card)
                                blocks.add(StreamedBlock.Text(
                                    id = "text_${blocks.size}",
                                    markdown = delta
                                ))
                            }
                            streamedBlocks.value = blocks
                        }
                    }
                    is com.cometchat.chat.models.AIAssistantCardStartedEvent -> {
                        val blocks = streamedBlocks.value.toMutableList()
                        blocks.add(StreamedBlock.CardLoading(
                            cardId = event.cardId,
                            label = event.executionText ?: "Building card…"
                        ))
                        streamedBlocks.value = blocks
                    }
                    is com.cometchat.chat.models.AIAssistantCardReceivedEvent -> {
                        val cardJson = event.card?.toString() ?: ""
                        if (cardJson.isNotEmpty()) {
                            val blocks = streamedBlocks.value.toMutableList()
                            val index = blocks.indexOfFirst {
                                it is StreamedBlock.CardLoading && it.cardId == event.cardId
                            }
                            if (index >= 0) {
                                blocks[index] = StreamedBlock.CardRendered(
                                    cardId = event.cardId,
                                    cardJson = cardJson
                                )
                            } else {
                                // No placeholder found — append rendered card
                                blocks.add(StreamedBlock.CardRendered(
                                    cardId = event.cardId,
                                    cardJson = cardJson
                                ))
                            }
                            streamedBlocks.value = blocks
                        }
                    }
                    is com.cometchat.chat.models.AIAssistantCardEndedEvent -> {
                        // No-op — the persisted message replaces the streamed bubble
                    }
                    else -> {
                        // Other events (tool start, text message start, etc.) handled below
                    }
                }
            }

            override fun onError(exception: CometChatException) {
                // Error state is reflected via message.isStreamingInterrupted
            }
        }
        aiStreamService?.startStreamingForRunId(runId, listener)
        onDispose {
            aiStreamService?.stopStreamingForRunId(runId, listener)
        }
    }

    // Determine display state
    val hasRunStarted = message.metadata?.optString(
        UIKitConstants.AIConstants.AI_ASSISTANT_EVENT_TYPE
    ) == UIKitConstants.AIAssistantEventType.RUN_STARTED
    val blocks = streamedBlocks.value
    // Fallback: if no blocks yet but message.text has content (rebind case), show it
    val fallbackText = if (blocks.isEmpty()) (message.text ?: "") else ""

    BubbleContainer(style = style, modifier = modifier) {
        Column {
            when {
                message.isStreamingInterrupted -> {
                    // Show all accumulated blocks + error indicator
                    for (block in blocks) {
                        RenderStreamedBlock(block = block, message = message, style = style)
                    }
                    if (blocks.isEmpty() && fallbackText.isNotEmpty()) {
                        MarkdownContent(text = fallbackText, style = style)
                    }
                    ErrorIndicator(style = style)
                }
                !hasRunStarted && blocks.isEmpty() && fallbackText.isEmpty() -> {
                    // Thinking state: shimmer animation
                    ShimmerThinkingText(style = style)
                }
                else -> {
                    // Render blocks sequentially in arrival order
                    if (blocks.isNotEmpty()) {
                        for (block in blocks) {
                            RenderStreamedBlock(block = block, message = message, style = style)
                        }
                    } else if (fallbackText.isNotEmpty()) {
                        // Fallback for rebind (no blocks yet, but text available)
                        MarkdownContent(text = fallbackText, style = style)
                    }
                }
            }
        }
    }
}

/**
 * Renders a single [StreamedBlock] — either markdown text or a card (loading/rendered).
 */
@Composable
private fun RenderStreamedBlock(
    block: StreamedBlock,
    message: StreamMessage,
    style: CometChatAIAssistantBubbleStyle
) {
    when (block) {
        is StreamedBlock.Text -> {
            if (block.markdown.isNotEmpty()) {
                MarkdownContent(text = block.markdown, style = style)
            }
        }
        is StreamedBlock.CardLoading -> {
            Text(
                text = block.label,
                style = CometChatTheme.typography.bodyRegular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        is StreamedBlock.CardRendered -> {
            // Center the card horizontally and cap it at ~65% of screen width
            // (Kotlin's `maxCardViewWidth`) so it stays narrower than the bubble.
            val cardMaxWidth = (LocalConfiguration.current.screenWidthDp * 0.65f).dp
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.widthIn(max = cardMaxWidth)) {
                    com.cometchat.cards.CometChatCardComposable(
                        cardJson = block.cardJson,
                        themeMode = com.cometchat.cards.models.CometChatCardThemeMode.AUTO,
                        onAction = { event ->
                            com.cometchat.uikit.core.events.CometChatEvents.emitUIEvent(
                                com.cometchat.uikit.core.events.CometChatUIEvent.CardActionClicked(
                                    message = message,
                                    actionEvent = event
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

// ── Static Mode ─────────────────────────────────────────────────────────

@Composable
private fun StaticMode(
    message: AIAssistantMessage,
    style: CometChatAIAssistantBubbleStyle,
    modifier: Modifier
) {
    val elements = message.elements
    if (elements.isNullOrEmpty()) {
        // Fallback: render getText() as markdown (existing behavior, regression guard)
        val text = message.text ?: ""
        BubbleContainer(style = style, modifier = modifier) {
            MarkdownContent(text = text, style = style)
        }
    } else {
        // Walk elements in array order and render per type
        BubbleContainer(style = style, modifier = modifier) {
            Column {
                for (element in elements) {
                    when (element.type) {
                        "text" -> {
                            // Render text element as markdown (same as existing text rendering)
                            val textValue = element.data?.toString() ?: ""
                            if (textValue.isNotEmpty()) {
                                MarkdownContent(text = textValue, style = style)
                            }
                        }
                        "card" -> {
                            // Render card element via the cards renderer
                            val cardData = element.data
                            val cardObj = when (cardData) {
                                is org.json.JSONObject -> cardData.optJSONObject("card")
                                is Map<*, *> -> {
                                    val cardMap = cardData["card"]
                                    if (cardMap != null) org.json.JSONObject(cardMap.toString()) else null
                                }
                                else -> null
                            }
                            val cardJson = cardObj?.toString() ?: ""
                            if (cardJson.isNotEmpty()) {
                                // Center the card horizontally and cap it at ~65% of screen
                                // width (Kotlin's `maxCardViewWidth`) so it stays narrower
                                // than the bubble with breathing room on both sides.
                                val cardMaxWidth = (LocalConfiguration.current.screenWidthDp * 0.65f).dp
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.widthIn(max = cardMaxWidth)) {
                                        com.cometchat.cards.CometChatCardComposable(
                                            cardJson = cardJson,
                                            themeMode = com.cometchat.cards.models.CometChatCardThemeMode.AUTO,
                                            onAction = { event ->
                                                com.cometchat.uikit.core.events.CometChatEvents.emitUIEvent(
                                                    com.cometchat.uikit.core.events.CometChatUIEvent.CardActionClicked(
                                                        message = message,
                                                        actionEvent = event
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        // Unknown element types — skip silently
                    }
                }
            }
        }
    }
}

// ── Bubble Container ────────────────────────────────────────────────────

@Composable
private fun BubbleContainer(
    style: CometChatAIAssistantBubbleStyle,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(style.cornerRadius)

    // Cap the bubble at ~70% of screen width to match the Kotlin UIKit (`maxCardWidth`),
    // so the AI bubble covers the same area in both SDKs instead of sizing to content.
    val maxBubbleWidth = (LocalConfiguration.current.screenWidthDp * 0.70f).dp

    // Symmetric content padding so the text/card never hugs the bubble edge — matches the
    // standard bubble content inset (12dp horizontal / 8dp vertical) used by the other
    // Kotlin/Compose bubbles. This matters in group agent chats where the wrapper draws a
    // filled background; in 1:1 chats the wrapper is transparent so the inset is invisible.
    Column(
        modifier = modifier
            .widthIn(max = maxBubbleWidth)
            .clip(shape)
            .background(style.backgroundColor, shape)
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(style.strokeWidth, style.strokeColor, shape)
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = dimensionResource(R.dimen.cometchat_padding_3),
                vertical = dimensionResource(R.dimen.cometchat_padding_2)
            )
    ) {
        content()
    }
}

// ── Shimmer "Thinking" Text ─────────────────────────────────────────────

@Composable
private fun ShimmerThinkingText(style: CometChatAIAssistantBubbleStyle) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Text(
        text = stringResource(R.string.cometchat_thinking),
        color = if (style.shimmerTextColor != Color.Unspecified) {
            style.shimmerTextColor
        } else {
            style.textColor
        },
        style = style.shimmerTextStyle,
        modifier = Modifier.alpha(alpha)
    )
}

// ── Error Indicator ─────────────────────────────────────────────────────

@Composable
private fun ErrorIndicator(style: CometChatAIAssistantBubbleStyle) {
    val errorBgColor = if (style.errorBackgroundColor != Color.Unspecified) {
        style.errorBackgroundColor
    } else {
        Color(0xFFF9EAEF)
    }
    val errorTextColor = if (style.errorTextColor != Color.Unspecified) {
        style.errorTextColor
    } else {
        Color(0xFFD32F2F)
    }
    val errorIconTint = if (style.errorIconTint != Color.Unspecified) {
        style.errorIconTint
    } else {
        Color(0xFFD32F2F)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(errorBgColor)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_alert_circle),
                contentDescription = null,
                tint = errorIconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.cometchat_something_went_wrong_try_again),
                color = errorTextColor,
                style = style.errorTextStyle
            )
        }
    }



}

// ── Markdown Content (AndroidView + Markwon) ────────────────────────────

@Composable
private fun MarkdownContent(
    text: String,
    style: CometChatAIAssistantBubbleStyle
) {
    val context = LocalContext.current
    // Ensure the context carries a Material Components theme for MaterialCardView inflation
    // in code block layouts. Use the Activity context which should have the app's theme.
    val themedContext = remember(context) {
        // Walk up to find the Activity context which has the full theme
        var ctx = context
        while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) {
            ctx = ctx.baseContext
        }
        // If the Activity context has Material theme, use it; otherwise overlay Material Components
        if (isMaterialTheme(ctx)) ctx
        else android.view.ContextThemeWrapper(ctx, com.google.android.material.R.style.Theme_MaterialComponents_DayNight)
    }
    val textColorArgb = style.textColor.toArgb()

    // Capture Compose theme colors for use in the AndroidView factory
    val colorScheme = CometChatTheme.colorScheme
    val themeColors = remember(colorScheme) {
        MarkwonThemeColors(
            backgroundColor4 = colorScheme.backgroundColor4.toArgb(),
            backgroundColor2 = colorScheme.backgroundColor2.toArgb(),
            backgroundColor1 = colorScheme.backgroundColor1.toArgb(),
            strokeColorDefault = colorScheme.strokeColorDefault.toArgb(),
            neutralColor800 = colorScheme.neutralColor800.toArgb(),
            primaryColor = colorScheme.primary.toArgb()
        )
    }

    // Remember the Markwon instance and adapter to avoid recreating on every recomposition
    val markwonState = remember(themedContext, textColorArgb, themeColors) {
        createMarkwonState(themedContext, textColorArgb, themeColors)
    }

    AndroidView(
        factory = { _ ->
            RecyclerView(themedContext).apply {
                layoutManager = LinearLayoutManager(themedContext)
                adapter = markwonState.adapter
                itemAnimator = null
                overScrollMode = View.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
            }
        },
        update = { _ ->
            if (text.isNotEmpty()) {
                markwonState.adapter.setMarkdown(markwonState.markwon, text)
                markwonState.adapter.notifyDataSetChanged()
            }
        }
    )
}

// ── Markwon Setup ───────────────────────────────────────────────────────

/**
 * Theme colors captured from the Compose theme for use in Markwon configuration.
 */
private data class MarkwonThemeColors(
    val backgroundColor4: Int,
    val backgroundColor2: Int,
    val backgroundColor1: Int,
    val strokeColorDefault: Int,
    val neutralColor800: Int,
    val primaryColor: Int
)

/**
 * Holds the Markwon instance and its associated adapter.
 */
private class MarkwonState(
    val markwon: Markwon,
    val adapter: MarkwonAdapter
)

/**
 * Creates a [MarkwonState] with the Markwon instance configured with all
 * required plugins (HTML, tables, strikethrough, syntax highlighting,
 * custom inline code spans) and a [MarkwonAdapter] for RecyclerView rendering.
 *
 * @param context Android context for Markwon initialization
 * @param textColor The ARGB text color for rendered content
 * @param themeColors Theme colors captured from the Compose theme
 */
private fun createMarkwonState(
    context: Context,
    textColor: Int,
    themeColors: MarkwonThemeColors
): MarkwonState {
    val prism4j = Prism4j(GrammarLocator())
    val dip = Dip.create(context)

    val markwon = Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(TableEntryPlugin.create { builder ->
            builder.tableHeaderRowBackgroundColor(themeColors.backgroundColor4)
                .tableEvenRowBackgroundColor(themeColors.backgroundColor2)
                .tableOddRowBackgroundColor(themeColors.backgroundColor2)
                .tableBorderWidth(dip.toPx(1))
                .tableBorderColor(themeColors.strokeColorDefault)
                .tableCellPadding(dip.toPx(8))
        })
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                builder.setFactory(Code::class.java) { _, _ ->
                    CustomInlineCodeSpan(
                        getTextSizeFromAppearance(context),
                        themeColors.neutralColor800,
                        textColor,
                        18f
                    )
                }
            }

            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.codeBlockBackgroundColor(themeColors.backgroundColor1)
                    .linkColor(themeColors.primaryColor)
                    .isLinkUnderlined(true)
            }
        })
        .build()

    val adapter = MarkwonAdapter.builder(
        object : MarkwonAdapter.Entry<Node, MarkwonAdapter.Holder>() {
            override fun createHolder(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): MarkwonAdapter.Holder {
                val view = inflater.inflate(
                    R.layout.cometchat_ai_assistant_root_text_view, parent, false
                )
                return MarkwonAdapter.Holder(view)
            }

            override fun bindHolder(
                markwon: Markwon,
                holder: MarkwonAdapter.Holder,
                node: Node
            ) {
                val textView = holder.itemView.findViewById<TextView>(R.id.root_text_view)
                if (textColor != 0) textView.setTextColor(textColor)
                // Use natural line height (multiplier=1) with small extra spacing.
                // Previous value (extra=20dp, multiplier=0) collapsed line height to just 20dp,
                // causing excessive gaps between list items and bullet points.
                textView.setLineSpacing(
                    convertDpToPx(textView.context, 4).toFloat(), 1f
                )
                val spanned = markwon.render(node)
                textView.text = spanned
                textView.movementMethod = LinkMovementMethod.getInstance()
            }
        })
        .include(
            FencedCodeBlock::class.java,
            object : MarkwonAdapter.Entry<FencedCodeBlock, MarkwonAdapter.Holder>() {
                override fun createHolder(
                    inflater: LayoutInflater,
                    parent: ViewGroup
                ): MarkwonAdapter.Holder {
                    val view = inflater.inflate(
                        R.layout.cometchat_ai_assistant_code_block_layout, parent, false
                    )
                    // Programmatically apply styling from Compose theme colors since
                    // ?attr/cometchatStrokeColorDefault may not resolve in the Compose context.
                    (view as? MaterialCardView)?.apply {
                        strokeColor = themeColors.strokeColorDefault
                        strokeWidth = dip.toPx(1)
                        radius = dip.toPx(12).toFloat()
                        setCardBackgroundColor(themeColors.backgroundColor1)
                    }
                    // Style the copy button
                    view.findViewById<MaterialCardView>(R.id.copy_button_layout)?.apply {
                        setCardBackgroundColor(themeColors.neutralColor800)
                    }
                    return MarkwonAdapter.Holder(view)
                }

                override fun bindHolder(
                    markwon: Markwon,
                    holder: MarkwonAdapter.Holder,
                    node: FencedCodeBlock
                ) {
                    val codeTextView =
                        holder.itemView.findViewById<TextView>(R.id.code_block_text_view)
                    val copyBtn =
                        holder.itemView.findViewById<MaterialCardView>(R.id.copy_button_layout)

                    val spanned = markwon.render(node)
                    markwon.setParsedMarkdown(codeTextView, spanned)

                    copyBtn.setOnClickListener { copyCode(codeTextView.context, node.literal) }
                }
            })
        .include(
            TableBlock::class.java,
            TableEntry.create { builder ->
                builder.tableLayout(
                    R.layout.cometchat_ai_assistant_table_layout,
                    R.id.table_layout
                ).textLayoutIsRoot(R.layout.cometchat_ai_assistant_table_entry_cell)
            })
        .build()

    return MarkwonState(markwon, adapter)
}

// ── Utility Functions ───────────────────────────────────────────────────

/**
 * Extracts a reasonable text size in pixels for inline code spans.
 * Uses 14sp as the default body text size.
 */
private fun getTextSizeFromAppearance(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics
    ).toInt()
}

/**
 * Converts dp to pixels.
 */
private fun convertDpToPx(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

/**
 * Copies code text to the clipboard.
 */
private fun copyCode(context: Context, code: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clipData = ClipData.newPlainText("Code", code)
    clipboardManager?.setPrimaryClip(clipData)
}

/**
 * Checks whether the given [context] already carries a Material Components theme.
 * Returns `true` if the theme resolves `colorPrimaryVariant` (a Material attribute),
 * meaning MaterialCardView can be inflated safely.
 */
private fun isMaterialTheme(context: Context): Boolean {
    val a = context.obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorPrimaryVariant))
    val hasMaterial = a.hasValue(0)
    a.recycle()
    return hasMaterial
}
