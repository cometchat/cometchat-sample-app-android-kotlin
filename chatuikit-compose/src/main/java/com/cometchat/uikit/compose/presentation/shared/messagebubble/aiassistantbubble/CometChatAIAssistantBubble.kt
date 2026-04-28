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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

@Composable
private fun StreamingMode(
    message: StreamMessage,
    style: CometChatAIAssistantBubbleStyle,
    aiStreamService: CometChatAIStreamService?,
    modifier: Modifier
) {
    val runId = message.runId
    val context = LocalContext.current

    // Observe accumulated text from the stream service
    val accumulatedText by aiStreamService?.accumulatedText(runId)
        ?.collectAsState() ?: remember { mutableStateOf("") }

    // Lifecycle: start/stop streaming
    DisposableEffect(runId, aiStreamService) {
        val listener = object : CometChatAIStreamService.AIStreamListener {
            override fun onAIAssistantEventReceived(event: AIAssistantBaseEvent) {
                // Events handled by the service; bubble just observes accumulatedText
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

    // Determine display state based on metadata and accumulated text
    // Use message.text as fallback — the ViewModel updates it directly from accumulated content
    val hasRunStarted = message.metadata?.optString(
        UIKitConstants.AIConstants.AI_ASSISTANT_EVENT_TYPE
    ) == UIKitConstants.AIAssistantEventType.RUN_STARTED
    val displayText = accumulatedText.ifEmpty { message.text ?: "" }

    BubbleContainer(style = style, modifier = modifier) {
        when {
            message.isStreamingInterrupted -> {
                // Show accumulated text + error indicator
                if (displayText.isNotEmpty()) {
                    MarkdownContent(text = displayText, style = style)
                }
                ErrorIndicator(style = style)
            }
            !hasRunStarted && displayText.isEmpty() -> {
                // Thinking state: shimmer animation
                ShimmerThinkingText(style = style)
            }
            else -> {
                // Streaming or completed: render markdown
                MarkdownContent(text = displayText, style = style)
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
    val text = message.text ?: ""
    BubbleContainer(style = style, modifier = modifier) {
        MarkdownContent(text = text, style = style)
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

    // Matches the Kotlin UIKit XML layout:
    // - Outer LinearLayout has paddingVertical=8dp (cometchat_padding_2)
    // - Inner content LinearLayout has marginEnd=40dp
    Column(
        modifier = modifier
            .clip(shape)
            .background(style.backgroundColor, shape)
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(style.strokeWidth, style.strokeColor, shape)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp)
            .padding(end = 40.dp)
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
