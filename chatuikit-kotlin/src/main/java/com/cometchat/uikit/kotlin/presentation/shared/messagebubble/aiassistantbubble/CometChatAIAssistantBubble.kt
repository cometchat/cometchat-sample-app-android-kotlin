package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.TypedArray
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.AIAssistantBaseEvent
import com.cometchat.chat.models.AIAssistantContentReceivedEvent
import com.cometchat.chat.models.AIAssistantToolStartedEvent
import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.core.CometChatAIStreamService
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.StreamMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatAiAssistantBubbleLayoutBinding
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.DIMENSION_NOT_SET
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.STYLE_NOT_SET
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
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
 * Custom view that renders AI assistant messages with Markwon-based markdown.
 *
 * Supports two modes:
 * - **Streaming mode**: When a [StreamMessage] is set via [setStreamMessage], shows
 *   shimmer/thinking state, streams content in real-time, and handles errors.
 * - **Static mode**: When an [AIAssistantMessage] is set via [setMessage], renders
 *   the final Markdown content directly without streaming.
 *
 * Both modes share the same Markdown rendering pipeline and styling.
 */
class CometChatAIAssistantBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatAIAssistantBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CometChatAIAssistantBubble"
    }

    // ViewBinding
    private val binding: CometchatAiAssistantBubbleLayoutBinding =
        CometchatAiAssistantBubbleLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    // Markwon rendering
    private lateinit var markwon: Markwon
    private lateinit var adapter: MarkwonAdapter
    private val streamingBuilder = StringBuilder()
    private var lastRenderedContent = ""

    // Styling state
    @ColorInt
    private var rootTextColor: Int = 0
    @StyleRes
    private var rootTextAppearance: Int = 0

    // Streaming state
    private var streamMessage: StreamMessage? = null
    private var isStreaming = false
    private var aiStreamService: CometChatAIStreamService? = null
    private var currentBubbleListener: CometChatAIStreamService.AIStreamListener? = null

    /**
     * Maximum width (in pixels) for the AI bubble content column.
     * Calculated as ~70% of the screen width matching Flutter's behavior.
     */
    private val maxCardWidth: Int by lazy {
        val screenWidth = resources.displayMetrics.widthPixels
        (screenWidth * 0.70).toInt()
    }

    /**
     * Maximum width (in pixels) for card content rendered inside this bubble.
     * Calculated as ~65% of the screen width (narrower than bubble) to provide
     * breathing room between card edges and bubble boundary, matching Flutter.
     */
    private val maxCardViewWidth: Int by lazy {
        val screenWidth = resources.displayMetrics.widthPixels
        (screenWidth * 0.65).toInt()
    }

    init {
        Utils.initMaterialCard(this)
        setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        // Ensure wrap_content layoutParams are set so that handleView() in
        // CometChatMessageBubble doesn't default to MATCH_PARENT when lp is null.
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        initMarkwon()
        setupRecyclerView()
        applyStyleAttributes(attrs, defStyleAttr, 0)
        applyMaxWidth()
    }

    /**
     * Constrains the content column to [maxCardWidth] so that card elements
     * (WebView-based, no intrinsic width) don't cause the bubble to expand
     * to full parent width through the wrap_content measurement chain.
     */
    private fun applyMaxWidth() {
        val contentColumn = binding.contentColumn
        contentColumn.post {
            if (contentColumn.width > maxCardWidth) {
                contentColumn.layoutParams = contentColumn.layoutParams.apply {
                    width = maxCardWidth
                }
                contentColumn.requestLayout()
            }
        }
    }

    // ── Markwon Initialization ──────────────────────────────────────────

    private fun initMarkwon() {
        val prism4j = Prism4j(GrammarLocator())
        val dip = Dip.create(context)

        markwon = Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TableEntryPlugin.create { builder ->
                builder.tableHeaderRowBackgroundColor(CometChatTheme.getBackgroundColor4(context))
                    .tableEvenRowBackgroundColor(CometChatTheme.getBackgroundColor2(context))
                    .tableOddRowBackgroundColor(CometChatTheme.getBackgroundColor2(context))
                    .tableBorderWidth(dip.toPx(1))
                    .tableBorderColor(CometChatTheme.getStrokeColorDefault(context))
                    .tableCellPadding(dip.toPx(8))
            })
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.setFactory(Code::class.java) { _, _ ->
                        CustomInlineCodeSpan(
                            getTextSizeFromAppearance(context, rootTextAppearance),
                            CometChatTheme.getNeutralColor800(context),
                            rootTextColor,
                            18f
                        )
                    }
                }

                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.codeBlockBackgroundColor(CometChatTheme.getBackgroundColor1(context))
                        .linkColor(CometChatTheme.getPrimaryColor(context))
                        .isLinkUnderlined(true)
                }
            })
            .build()
    }

    // ── RecyclerView Setup ──────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = MarkwonAdapter.builder(
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
                    if (rootTextColor != 0) textView.setTextColor(rootTextColor)
                    if (rootTextAppearance != 0) textView.setTextAppearance(rootTextAppearance)
                    textView.setLineSpacing(
                        Utils.convertDpToPx(context, 20).toFloat(), 0f
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

                        if (rootTextAppearance != 0) codeTextView.setTextAppearance(rootTextAppearance)

                        val spanned = markwon.render(node)
                        markwon.setParsedMarkdown(codeTextView, spanned)

                        copyBtn.setOnClickListener { copyCode(node.literal) }
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

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
    }

    // ── Ordered Block Model ────────────────────────────────────────────

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

    /** Ordered list of blocks — preserves arrival order of text and cards. */
    private val streamedBlocks = mutableListOf<StreamedBlock>()

    // ── Stream Message Handling ─────────────────────────────────────────

    /**
     * Sets a [StreamMessage] for streaming mode.
     *
     * Shows shimmer/thinking state initially, then streams content in real-time
     * as events arrive from the AI assistant service.
     *
     * Uses an ordered block list to preserve the sequential rendering of
     * interleaved text and card content from the AI agent.
     */
    fun setStreamMessage(message: StreamMessage) {
        if (message !== this.streamMessage) {
            // Clean up old listener if switching to a different message
            val oldListener = currentBubbleListener
            val oldRunId = this.streamMessage?.runId ?: -1L
            if (oldListener != null && oldRunId > -1) {
                val service = aiStreamService ?: CometChatAIStreamService.getInstance()
                service?.stopStreamingForRunId(oldRunId, oldListener)
                currentBubbleListener = null
            }
            this.streamMessage = message
            streamingBuilder.setLength(0)
            streamedBlocks.clear()
            // Drop any block views left over from a previously bound message on this
            // recycled bubble so incremental rendering starts from a clean container.
            (binding.recyclerView.parent as? android.view.ViewGroup)?.let { p ->
                p.findViewWithTag<android.widget.LinearLayout>("ordered_blocks_container")
                    ?.let { p.removeView(it) }
            }
            renderMarkdown(streamingBuilder.toString())
            lastRenderedContent = ""
            isStreaming = false
        }

        // Error state — when interrupted, hide thinking and show error, then return
        if (message.isStreamingInterrupted) {
            hideAnimatedText()
            if (streamedBlocks.isNotEmpty()) {
                // Show all accumulated blocks above the error
                renderOrderedBlocks(message)
            } else if (!message.text.isNullOrEmpty()) {
                // Fallback: show text directly
                binding.recyclerView.visibility = VISIBLE
                streamingBuilder.setLength(0)
                streamingBuilder.append(message.text)
                renderMarkdown(streamingBuilder.toString())
            } else {
                // No content — hide recycler so error card is the only child,
                // vertically centered with the avatar
                binding.recyclerView.visibility = GONE
            }
            binding.errorCard.visibility = VISIBLE
            return
        }
        binding.errorCard.visibility = GONE

        // Text-based branching
        val messageText = message.text
        if (messageText.isNullOrEmpty() && streamedBlocks.isEmpty()) {
            // No text yet — show shimmer, but guard against overwriting
            // tool execution text (e.g., "Searching...") during active streaming
            val currentShimmerText = binding.streamShimmerTextView.text?.toString() ?: ""
            if (currentShimmerText.isNotEmpty() && isStreaming) {
                return
            } else {
                binding.streamShimmerTextView.text =
                    context.getString(R.string.cometchat_thinking)
                showAnimatedText()
            }
        } else if (streamedBlocks.isNotEmpty()) {
            // Blocks are being managed — render them in order
            hideAnimatedText()
            renderOrderedBlocks(message)
        } else if (!messageText.isNullOrEmpty()) {
            // Text already accumulated (rebind during streaming, no blocks yet) — render directly
            hideAnimatedText()
            streamingBuilder.setLength(0)
            streamingBuilder.append(messageText)
            renderMarkdown(streamingBuilder.toString())
        }

        // Start streaming if metadata indicates RUN_STARTED
        if (message.runId > -1 && message.metadata != null) {
            val event = message.metadata.optString(UIKitConstants.AIConstants.AI_ASSISTANT_EVENT_TYPE)
            if (event.isNotEmpty() && UIKitConstants.AIAssistantEventType.RUN_STARTED == event) {
                startStreaming(message)
            }
        }
    }

    // ── AI Assistant Message (Static) ──────────────────────────────────

    /**
     * Sets an [AIAssistantMessage] for static rendering mode.
     *
     * Renders the final markdown content directly without streaming, shimmer,
     * or error states. This is used for completed AI assistant responses.
     */
    fun setMessage(message: AIAssistantMessage) {
        // Clean up any active streaming state
        val oldListener = currentBubbleListener
        val oldRunId = streamMessage?.runId ?: -1L
        if (oldListener != null && oldRunId > -1) {
            val service = aiStreamService ?: CometChatAIStreamService.getInstance()
            service?.stopStreamingForRunId(oldRunId, oldListener)
            currentBubbleListener = null
        }
        streamMessage = null
        isStreaming = false

        // Hide streaming-specific UI
        binding.errorCard.visibility = GONE
        hideAnimatedText()

        // Check for elements (ordered blocks) — new card message support
        val elements = message.elements
        if (!elements.isNullOrEmpty()) {
            renderElements(message, elements)
        } else {
            // Fallback: render getText() as markdown (existing behavior, regression guard)
            val text = message.text ?: ""
            streamingBuilder.setLength(0)
            streamingBuilder.append(text)
            renderMarkdown(text)
        }
    }

    /**
     * Renders an ordered list of [AIAssistantElement] blocks inline.
     * Text blocks use the existing markdown renderer; card blocks use [CometChatCardView].
     */
    private fun renderElements(message: AIAssistantMessage, elements: List<com.cometchat.chat.models.AIAssistantElement>) {
        // Use the recycler view's parent as a container for mixed content
        // For elements rendering, we hide the recycler and add views to a dynamic container
        binding.recyclerView.visibility = GONE

        // Get or create the elements container (LinearLayout added dynamically)
        val parentLayout = binding.recyclerView.parent as? android.view.ViewGroup ?: return
        // Remove any previous elements container
        val existingContainer = parentLayout.findViewWithTag<android.widget.LinearLayout>("elements_container")
        existingContainer?.let { parentLayout.removeView(it) }
        // Also remove any streaming blocks container from a previous session
        val existingBlocksContainer = parentLayout.findViewWithTag<android.widget.LinearLayout>("ordered_blocks_container")
        existingBlocksContainer?.let { parentLayout.removeView(it) }

        val elementsContainer = android.widget.LinearLayout(context).apply {
            tag = "elements_container"
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                maxCardWidth,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        for (element in elements) {
            when (element.type) {
                "text" -> {
                    val textValue = element.data?.toString() ?: ""
                    if (textValue.isNotEmpty()) {
                        // Create a markdown text block using Markwon
                        val recyclerView = androidx.recyclerview.widget.RecyclerView(context).apply {
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                            isNestedScrollingEnabled = false
                            overScrollMode = android.view.View.OVER_SCROLL_NEVER
                            val markwonAdapter = io.noties.markwon.recycler.MarkwonAdapter.builderTextViewIsRoot(android.R.layout.simple_list_item_1)
                                .build()
                            adapter = markwonAdapter
                            markwonAdapter.setMarkdown(markwon, textValue)
                            markwonAdapter.notifyDataSetChanged()
                        }
                        elementsContainer.addView(recyclerView)
                    }
                }
                "card" -> {
                    val cardData = element.data
                    val cardJson = try {
                        when (cardData) {
                            is org.json.JSONObject -> cardData.optJSONObject("card")?.toString() ?: ""
                            is Map<*, *> -> {
                                val cardMap = cardData["card"]
                                cardMap?.let { org.json.JSONObject(it.toString()).toString() } ?: ""
                            }
                            else -> ""
                        }
                    } catch (e: Exception) { "" }

                    if (cardJson.isNotEmpty()) {
                        // Wrap card in a FrameLayout to constrain its height measurement.
                        // CometChatCardView (WebView-based) can report incorrect heights
                        // when placed directly in a LinearLayout, causing extra empty space.
                        val cardWrapper = android.widget.FrameLayout(context).apply {
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                maxCardViewWidth,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = (8 * resources.displayMetrics.density).toInt()
                                bottomMargin = (8 * resources.displayMetrics.density).toInt()
                                // Center the card horizontally within the bubble content,
                                // since it is narrower than the surrounding text/code-box.
                                gravity = android.view.Gravity.CENTER_HORIZONTAL
                            }
                        }
                        val cardView = com.cometchat.cards.CometChatCardView(context).apply {
                            layoutParams = android.widget.FrameLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setThemeMode(com.cometchat.cards.models.CometChatCardThemeMode.AUTO)
                            // Action callback BEFORE schema (critical — assigning schema triggers render)
                            setActionCallback(com.cometchat.cards.actions.CometChatCardActionCallback { event ->
                                com.cometchat.uikit.core.events.CometChatEvents.emitUIEvent(
                                    com.cometchat.uikit.core.events.CometChatUIEvent.CardActionClicked(
                                        message = message,
                                        actionEvent = event
                                    )
                                )
                            })
                            setCardSchema(cardJson)
                        }
                        cardWrapper.addView(cardView)
                        elementsContainer.addView(cardWrapper)
                    }
                }
                // Unknown element types — skip silently
            }
        }

        // Add the elements container to the parent at the position where recycler was
        val recyclerIndex = parentLayout.indexOfChild(binding.recyclerView)
        parentLayout.addView(elementsContainer, recyclerIndex + 1)
    }

    // ── Streaming ───────────────────────────────────────────────────────

    private fun startStreaming(message: StreamMessage) {
        isStreaming = true
        val service = aiStreamService ?: CometChatAIStreamService.getInstance()
        if (service == null) {
            Log.e(TAG, "startStreaming: no AIStreamService available for runId=${message.runId}")
            return
        }
        // Store the listener reference so we can remove just this listener on RUN_FINISHED,
        // without killing the processing job or the ViewModel's listener.
        val bubbleListener = object : CometChatAIStreamService.AIStreamListener {
            override fun onAIAssistantEventReceived(event: AIAssistantBaseEvent) {
                handleEventStreaming(event, message)
            }

            override fun onError(exception: CometChatException) {
                handleError(message, exception)
            }
        }
        currentBubbleListener = bubbleListener
        service.startStreamingForRunId(message.runId, bubbleListener)
    }

    private fun handleEventStreaming(
        event: AIAssistantBaseEvent,
        message: StreamMessage
    ) {
        when {
            UIKitConstants.AIAssistantEventType.TOOL_CALL_START == event.type -> {
                if (event is AIAssistantToolStartedEvent) {
                    val execText = event.executionText
                    if (!execText.isNullOrEmpty()) {
                        binding.streamShimmerTextView.text = execText
                    }
                    showAnimatedText()
                }
            }
            UIKitConstants.AIAssistantEventType.TEXT_MESSAGE_START == event.type -> {
                hideAnimatedText()
            }
            CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_RUN_FINISHED == event.type -> {
                handleRunFinished(message)
            }
            // Card lifecycle events (streaming) — ordered block approach
            event is com.cometchat.chat.models.AIAssistantCardStartedEvent -> {
                // Append a card loading placeholder as the next block in order
                streamedBlocks.add(StreamedBlock.CardLoading(
                    cardId = event.cardId,
                    label = event.executionText ?: context.getString(R.string.cometchat_thinking)
                ))
                hideAnimatedText()
                renderOrderedBlocks(message)
            }
            event is com.cometchat.chat.models.AIAssistantCardReceivedEvent -> {
                // Replace the loading placeholder with the rendered card at the same position
                val cardJson = event.card?.toString() ?: ""
                if (cardJson.isNotEmpty()) {
                    val index = streamedBlocks.indexOfFirst {
                        it is StreamedBlock.CardLoading && it.cardId == event.cardId
                    }
                    if (index >= 0) {
                        streamedBlocks[index] = StreamedBlock.CardRendered(
                            cardId = event.cardId,
                            cardJson = cardJson
                        )
                    } else {
                        // No placeholder found — append rendered card
                        streamedBlocks.add(StreamedBlock.CardRendered(
                            cardId = event.cardId,
                            cardJson = cardJson
                        ))
                    }
                    renderOrderedBlocks(message)
                }
            }
            event is com.cometchat.chat.models.AIAssistantCardEndedEvent -> {
                // No-op — the persisted message replaces the streamed bubble after run finishes
            }
        }

        // When actual text content arrives, append to or extend the current text block
        if (event is AIAssistantContentReceivedEvent) {
            val delta = event.delta
            if (!delta.isNullOrEmpty()) {
                hideAnimatedText()
                // Append to existing text block or create a new one
                val lastBlock = streamedBlocks.lastOrNull()
                if (lastBlock is StreamedBlock.Text) {
                    streamedBlocks[streamedBlocks.lastIndex] = lastBlock.copy(
                        markdown = lastBlock.markdown + delta
                    )
                } else {
                    streamedBlocks.add(StreamedBlock.Text(
                        id = "text_${streamedBlocks.size}",
                        markdown = delta
                    ))
                }
                // Also update streamingBuilder and message.text for backward compatibility
                streamingBuilder.append(delta)
                message.text = streamingBuilder.toString()
                renderOrderedBlocks(message)
            }
        }
    }

    // ── Ordered Block Rendering ─────────────────────────────────────────

    /**
     * Renders the ordered list of [StreamedBlock]s sequentially in a dynamic
     * LinearLayout container. This preserves the arrival order of interleaved
     * text and card content from the AI agent.
     */
    private fun renderOrderedBlocks(message: StreamMessage) {
        // Hide the recycler view (used for single-text streaming) since we use the container
        binding.recyclerView.visibility = GONE

        val parentLayout = binding.recyclerView.parent as? android.view.ViewGroup ?: return

        // Remove any stale elements container left over from a static render on this recycled bubble
        val staleElementsContainer = parentLayout.findViewWithTag<android.widget.LinearLayout>("elements_container")
        staleElementsContainer?.let { parentLayout.removeView(it) }

        // Get or create the ordered blocks container
        val existingContainer = parentLayout.findViewWithTag<android.widget.LinearLayout>("ordered_blocks_container")
        val blocksContainer: android.widget.LinearLayout
        if (existingContainer != null) {
            blocksContainer = existingContainer
        } else {
            blocksContainer = android.widget.LinearLayout(context).apply {
                tag = "ordered_blocks_container"
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    maxCardWidth,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val recyclerIndex = parentLayout.indexOfChild(binding.recyclerView)
            parentLayout.addView(blocksContainer, recyclerIndex + 1)
        }

        // Reconcile the existing child views with the current blocks instead of tearing
        // everything down and rebuilding it on every delta. Only the changed block (the
        // growing text tail, a newly appended block, or a card placeholder swapped to its
        // rendered form) does any real work; untouched blocks are skipped. This keeps a
        // streaming response O(n) overall instead of O(n^2).
        var childIndex = 0
        for (block in streamedBlocks) {
            // An empty text block renders no view (mirrors the original behavior); skip it
            // without advancing childIndex so block/view positions stay aligned.
            if (block is StreamedBlock.Text && block.markdown.isEmpty()) continue

            val signature = blockSignature(block)
            val existing = blocksContainer.getChildAt(childIndex)

            when {
                // Identical block already rendered at this position — leave it untouched.
                // This is what skips the unchanged prior blocks (the real O(n^2) source):
                // finished text and already-rendered cards are not rebuilt on every delta.
                existing != null && existing.tag == signature -> { /* no-op */ }

                // New or changed block — the growing text tail, a freshly appended block,
                // or a CardLoading -> CardRendered swap. Rebuild only this one view (siblings
                // are left untouched) so each view is constructed exactly as a fresh render
                // would, avoiding any reuse/re-measure quirks of a nested RecyclerView.
                else -> {
                    if (existing != null) blocksContainer.removeViewAt(childIndex)
                    val view = buildBlockView(block, message)
                    view.tag = signature
                    blocksContainer.addView(view, childIndex)
                }
            }
            childIndex++
        }

        // Trim any trailing views left over from a previous, longer render.
        while (blocksContainer.childCount > childIndex) {
            blocksContainer.removeViewAt(blocksContainer.childCount - 1)
        }
    }

    /**
     * Stable identity + content signature for a block, stored as the rendered view's tag.
     * Used to detect whether a view can be reused as-is, updated in place, or rebuilt.
     */
    private fun blockSignature(block: StreamedBlock): String = when (block) {
        is StreamedBlock.Text -> "T|${block.id}|${block.markdown.hashCode()}"
        is StreamedBlock.CardLoading -> "L|${block.cardId}"
        is StreamedBlock.CardRendered -> "R|${block.cardId}|${block.cardJson.hashCode()}"
    }

    /** Builds the view for a single [StreamedBlock]. The caller positions and tags it. */
    private fun buildBlockView(block: StreamedBlock, message: StreamMessage): View = when (block) {
        is StreamedBlock.Text -> {
            androidx.recyclerview.widget.RecyclerView(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                itemAnimator = null
                val markwonAdapter = io.noties.markwon.recycler.MarkwonAdapter.builder(
                    object : io.noties.markwon.recycler.MarkwonAdapter.Entry<org.commonmark.node.Node, io.noties.markwon.recycler.MarkwonAdapter.Holder>() {
                        override fun createHolder(
                            inflater: LayoutInflater,
                            parent: ViewGroup
                        ): io.noties.markwon.recycler.MarkwonAdapter.Holder {
                            val view = inflater.inflate(
                                R.layout.cometchat_ai_assistant_root_text_view, parent, false
                            )
                            return io.noties.markwon.recycler.MarkwonAdapter.Holder(view)
                        }

                        override fun bindHolder(
                            markwon: Markwon,
                            holder: io.noties.markwon.recycler.MarkwonAdapter.Holder,
                            node: org.commonmark.node.Node
                        ) {
                            val textView = holder.itemView.findViewById<TextView>(R.id.root_text_view)
                            if (rootTextColor != 0) textView.setTextColor(rootTextColor)
                            if (rootTextAppearance != 0) textView.setTextAppearance(rootTextAppearance)
                            textView.setLineSpacing(
                                Utils.convertDpToPx(context, 20).toFloat(), 0f
                            )
                            val spanned = markwon.render(node)
                            textView.text = spanned
                            textView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
                        }
                    })
                    .build()
                adapter = markwonAdapter
                markwonAdapter.setMarkdown(markwon, block.markdown)
                markwonAdapter.notifyDataSetChanged()
            }
        }
        is StreamedBlock.CardLoading -> {
            android.widget.TextView(context).apply {
                text = block.label
                setTextColor(CometChatTheme.getTextColorSecondary(context))
                textSize = 13f
                val padding = (12 * resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(CometChatTheme.getBackgroundColor3(context))
                    cornerRadius = 8 * resources.displayMetrics.density
                }
                val lp = android.widget.LinearLayout.LayoutParams(
                    maxCardViewWidth,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = (8 * resources.displayMetrics.density).toInt()
                lp.bottomMargin = (8 * resources.displayMetrics.density).toInt()
                lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = lp
            }
        }
        is StreamedBlock.CardRendered -> {
            com.cometchat.cards.CometChatCardView(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    maxCardViewWidth,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (8 * resources.displayMetrics.density).toInt()
                    bottomMargin = (8 * resources.displayMetrics.density).toInt()
                    // Center the card horizontally within the bubble content.
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                setThemeMode(com.cometchat.cards.models.CometChatCardThemeMode.AUTO)
                // Action callback BEFORE schema
                setActionCallback(com.cometchat.cards.actions.CometChatCardActionCallback { event ->
                    com.cometchat.uikit.core.events.CometChatEvents.emitUIEvent(
                        com.cometchat.uikit.core.events.CometChatUIEvent.CardActionClicked(
                            message = message,
                            actionEvent = event
                        )
                    )
                })
                setCardSchema(block.cardJson)
            }
        }
    }

    private fun handleRunFinished(message: StreamMessage) {
        isStreaming = false
        // Remove only the bubble's own listener — do NOT call the no-arg overload
        // which cancels the processing job and removes ALL listeners (including the ViewModel's).
        val listener = currentBubbleListener
        if (listener != null) {
            val service = aiStreamService ?: CometChatAIStreamService.getInstance()
            service?.stopStreamingForRunId(message.runId, listener)
            currentBubbleListener = null
        }
    }

    private fun handleError(message: StreamMessage, exception: CometChatException) {
        isStreaming = false
        message.isStreamingInterrupted = true
        // Remove only the bubble's own listener
        val listener = currentBubbleListener
        if (listener != null) {
            val service = aiStreamService ?: CometChatAIStreamService.getInstance()
            service?.stopStreamingForRunId(message.runId, listener)
            currentBubbleListener = null
        }
        // Hide thinking/shimmer text
        binding.streamShimmerTextView.visibility = GONE
        binding.streamShimmerTextView.stopShimmer()
        // If no content was accumulated, hide recycler so error card centers with avatar
        if (streamingBuilder.isEmpty()) {
            binding.recyclerView.visibility = GONE
        }
        binding.errorCard.visibility = VISIBLE
        Log.e(TAG, exception.message ?: "Streaming error")
    }

    // ── Markdown Rendering ──────────────────────────────────────────────

    private fun renderMarkdown(content: String) {
        if (shouldUpdateContent(content)) {
            adapter.setMarkdown(markwon, content)
            adapter.notifyDataSetChanged()
            lastRenderedContent = content
        }
    }

    private fun shouldUpdateContent(newContent: String): Boolean {
        return !newContent.trim().equals(lastRenderedContent.trim(), ignoreCase = true)
    }

    private fun copyCode(code: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = ClipData.newPlainText("Code", code)
        clipboardManager?.setPrimaryClip(clipData)
    }

    // ── Shimmer Helpers ─────────────────────────────────────────────────

    private fun showAnimatedText() {
        binding.streamShimmerTextView.visibility = VISIBLE
        binding.streamShimmerTextView.startShimmer()
        binding.recyclerView.visibility = GONE
    }

    private fun hideAnimatedText() {
        binding.streamShimmerTextView.visibility = GONE
        binding.recyclerView.visibility = VISIBLE
    }

    // ── Style Setters ───────────────────────────────────────────────────

    fun setTextColor(@ColorInt color: Int) {
        this.rootTextColor = color
        binding.streamShimmerTextView.setTextColor(color)
    }

    fun setTextAppearance(@StyleRes textAppearance: Int) {
        this.rootTextAppearance = textAppearance
        binding.streamShimmerTextView.setTextAppearance(textAppearance)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    fun setAvatarStyle(@StyleRes style: Int) {
        if (style != 0) {
            binding.ivAvatar.setStyle(style)
        }
    }

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatAIAssistantBubble
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    /**
     * Applies style properties from a [CometChatAIAssistantBubbleStyle] object.
     *
     * Only applies properties that are not set to sentinel values ([STYLE_NOT_SET] / [DIMENSION_NOT_SET]).
     * Container properties are applied to this [MaterialCardView], content text properties to the
     * markdown renderer, shimmer properties to the shimmer text view, and error properties to the
     * error layout children.
     */
    fun setStyle(style: CometChatAIAssistantBubbleStyle) {
        // Container properties (MaterialCardView)
        if (style.backgroundColor != STYLE_NOT_SET) {
            setCardBackgroundColor(style.backgroundColor)
        }
        if (style.cornerRadius != DIMENSION_NOT_SET) {
            radius = style.cornerRadius
        }
        if (style.strokeWidth != DIMENSION_NOT_SET) {
            strokeWidth = style.strokeWidth.toInt()
        }
        if (style.strokeColor != STYLE_NOT_SET) {
            strokeColor = style.strokeColor
        }

        // Content text properties (for markdown-rendered content)
        if (style.textColor != 0) {
            setTextColor(style.textColor)
        }
        if (style.textAppearance != 0) {
            setTextAppearance(style.textAppearance)
        }

        // Shimmer / thinking state properties
        if (style.shimmerTextColor != 0) {
            binding.streamShimmerTextView.setTextColor(style.shimmerTextColor)
        }
        if (style.shimmerTextAppearance != 0) {
            TextViewCompat.setTextAppearance(binding.streamShimmerTextView, style.shimmerTextAppearance)
        }

        // Error state properties
        applyErrorStyle(style)

        // Avatar style
        if (style.avatarStyleRes != 0) {
            setAvatarStyle(style.avatarStyleRes)
        }
    }

    /**
     * Applies error styling properties to the error layout's child views.
     * The error layout contains an [ImageView] (index 0) and a [TextView] (index 1).
     */
    private fun applyErrorStyle(style: CometChatAIAssistantBubbleStyle) {
        val errorLayout = binding.errorLayout

        if (style.errorBackgroundColor != 0) {
            errorLayout.setBackgroundColor(style.errorBackgroundColor)
            (errorLayout.parent as? MaterialCardView)?.setCardBackgroundColor(style.errorBackgroundColor)
        }

        if (style.errorIconTint != 0) {
            val errorIcon = errorLayout.getChildAt(0) as? ImageView
            errorIcon?.setColorFilter(style.errorIconTint)
        }

        val errorTextView = errorLayout.getChildAt(1) as? TextView
        if (errorTextView != null) {
            if (style.errorTextColor != 0) {
                errorTextView.setTextColor(style.errorTextColor)
            }
            if (style.errorTextAppearance != 0) {
                TextViewCompat.setTextAppearance(errorTextView, style.errorTextAppearance)
            }
        }
    }

    fun setAvatar(name: String, url: String?) {
        binding.ivAvatar.setAvatar(name, url)
    }

    fun setAIStreamService(service: CometChatAIStreamService?) {
        this.aiStreamService = service
    }

    // ── Style Attribute Extraction ──────────────────────────────────────

    private fun applyStyleAttributes(
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, defStyleRes
        )
        try {
            @StyleRes val style = typedArray.getResourceId(
                R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStyle, 0
            )
            typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, style
            )
            extractAttributesAndApplyDefaults(typedArray)
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error applying style attributes")
        }
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            setTextColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextColor, 0
                )
            )
            setTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextAppearance, 0
                )
            )
            setBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleBackgroundColor, 0
                )
            )
            setAvatarStyle(
                typedArray.getResourceId(
                    R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleAvatarStyle, 0
                )
            )
        } finally {
            typedArray.recycle()
        }
    }

    // ── Utility ─────────────────────────────────────────────────────────

    /**
     * Extracts the text size in pixels from a text appearance style resource.
     * Returns a reasonable default if the resource is invalid.
     */
    private fun getTextSizeFromAppearance(context: Context, @StyleRes textAppearance: Int): Int {
        if (textAppearance == 0) return 14 // default fallback in sp-equivalent pixels
        return try {
            val attrs = intArrayOf(android.R.attr.textSize)
            val ta = context.obtainStyledAttributes(textAppearance, attrs)
            val size = ta.getDimensionPixelSize(0, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics
            ).toInt())
            ta.recycle()
            size
        } catch (e: Exception) {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics
            ).toInt()
        }
    }
}
