package com.cometchat.chatuikit.shared.views.aiassistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.AIAssistantBaseEvent;
import com.cometchat.chat.models.AIAssistantContentReceivedEvent;
import com.cometchat.chat.models.AIAssistantToolStartedEvent;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.ai.CometChatAIStreamService;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.GrammarLocator;
import com.cometchat.chatuikit.databinding.CometchatStreamBubbleLayoutBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.models.StreamMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Node;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.markwon.utils.Dip;
import io.noties.prism4j.Prism4j;

public class CometChatStreamBubble extends MaterialCardView {
    private static final String TAG = "CometChatStreamBubble";
    private CometchatStreamBubbleLayoutBinding binding;

    private Markwon markwon;
    private MarkwonAdapter adapter;

    private @ColorInt int rootTextColor;
    private @StyleRes int rootTextAppearance;
    private StreamMessage streamMessage;

    private String lastRenderedContent = "";
    private boolean isStreaming = false;
    private final StringBuilder streamingBuilder = new StringBuilder();

    /** Constructor for CometChatStreamBubble
     *
     * @param context The context of the view
     */
    public CometChatStreamBubble(Context context) {
        this(context, null);
    }

    /** Constructor for CometChatStreamBubble
     *
     * @param context The context of the view
     * @param attrs   The AttributeSet from XML
     */
    public CometChatStreamBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAIAssistantBubbleStyle);
    }

    /** Constructor for CometChatStreamBubble
     *
     * @param context      The context of the view
     * @param attrs        The AttributeSet from XML
     * @param defStyleAttr The default style attribute
     */
    public CometChatStreamBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /** Initializes the view, inflates the layout, and sets up Markwon and RecyclerView
     *
     * @param context      The context of the view
     * @param attrs        The AttributeSet from XML
     * @param defStyleAttr The default style attribute
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        binding = CometchatStreamBubbleLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        initMarkwon();
        setupRecyclerView();
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /** Initializes the Markwon instance with necessary plugins and themes */
    private void initMarkwon() {
        Prism4j prism4j = new Prism4j(new GrammarLocator());
        Dip dip = Dip.create(getContext());

        markwon = Markwon.builder(getContext())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TableEntryPlugin.create(builder -> {
                    builder.tableHeaderRowBackgroundColor(CometChatTheme.getBackgroundColor4(getContext()))
                            .tableEvenRowBackgroundColor(CometChatTheme.getBackgroundColor2(getContext()))
                            .tableOddRowBackgroundColor(CometChatTheme.getBackgroundColor2(getContext()))
                            .tableBorderWidth(dip.toPx(1))
                            .tableBorderColor(CometChatTheme.getStrokeColorDefault(getContext()))
                            .tableCellPadding(dip.toPx(8));
                }))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
                        builder.setFactory(Code.class, (configuration, props) ->
                                new CustomInlineCodeSpan(
                                        Utils.getTextSize(getContext(), rootTextAppearance),
                                        CometChatTheme.getNeutralColor800(getContext()),
                                        rootTextColor,
                                        18f
                                ));
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.codeBlockBackgroundColor(CometChatTheme.getBackgroundColor1(getContext())) // code block background color
                                .linkColor(CometChatTheme.getPrimaryColor(getContext()))
                                .isLinkUnderlined(true);
                    }
                })
                .build();
    }
    /** Sets up the RecyclerView with a Markwon Adapter to display markdown content */
    private void setupRecyclerView() {
        adapter = MarkwonAdapter.builder(
                        new MarkwonAdapter.Entry<Node, MarkwonAdapter.Holder>() {
                            @NonNull
                            @Override
                            public MarkwonAdapter.Holder createHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
                                View view = inflater.inflate(R.layout.cometchat_ai_assistant_root_text_view, parent, false);
                                return new MarkwonAdapter.Holder(view);
                            }

                            @Override
                            public void bindHolder(@NonNull Markwon markwon, @NonNull MarkwonAdapter.Holder holder, @NonNull Node node) {
                                TextView textView = holder.itemView.findViewById(R.id.root_text_view);
                                if (rootTextColor != 0)
                                    textView.setTextColor(rootTextColor);
                                if (rootTextAppearance != 0)
                                    textView.setTextAppearance(rootTextAppearance);

                                textView.setLineSpacing(Utils.dpToPx(getContext(), 20), 0);
                                Spanned spanned = markwon.render(node);
                                textView.setText(spanned);
                                textView.setMovementMethod(LinkMovementMethod.getInstance());
                            }
                        })
                .include(FencedCodeBlock.class, new MarkwonAdapter.Entry<FencedCodeBlock, MarkwonAdapter.Holder>() {
                    @NonNull
                    @Override
                    public MarkwonAdapter.Holder createHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
                        View view = inflater.inflate(R.layout.cometchat_ai_assistant_code_block_layout, parent, false);
                        return new MarkwonAdapter.Holder(view);
                    }

                    @Override
                    public void bindHolder(@NonNull Markwon markwon, @NonNull MarkwonAdapter.Holder holder, @NonNull FencedCodeBlock node) {
                        TextView codeTextView = holder.itemView.findViewById(R.id.code_block_text_view);
                        MaterialCardView copyBtn = holder.itemView.findViewById(R.id.copy_button_layout);

                        if (rootTextAppearance != 0)
                            codeTextView.setTextAppearance(rootTextAppearance);

                        Spanned spanned = markwon.render(node);
                        markwon.setParsedMarkdown(codeTextView, spanned);

                        copyBtn.setOnClickListener(v -> copyCode(node.getLiteral()));
                    }
                })
                .include(TableBlock.class,
                        TableEntry.create(builder -> builder
                                .tableLayout(R.layout.cometchat_ai_assistant_table_layout, R.id.table_layout)
                                .textLayoutIsRoot(R.layout.cometchat_ai_assistant_table_entry_cell)))
                .build();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);
    }

    private void copyCode(String code) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Code", code);
        clipboardManager.setPrimaryClip(clipData);
    }

    /** Shows the animation effect while the AI assistant is generating a response */
    private void showAnimatedText() {
        binding.streamShimmerTextView.setVisibility(VISIBLE);
        binding.streamShimmerTextView.startShimmer();
        binding.recyclerView.setVisibility(GONE);
    }

    /** Hides the shimmer animation while the AI assistant is generating a response */
    private void hideAnimatedText() {
        binding.streamShimmerTextView.setVisibility(GONE);
        binding.recyclerView.setVisibility(VISIBLE);
    }

    /** Sets the visibility of the RecyclerView
     *
     * @param visibility The visibility constant (e.g., VISIBLE, GONE)
     */
    private void setRecyclerViewVisibility(int visibility) {
        binding.recyclerView.setVisibility(visibility);
    }

    /** Sets the StreamMessage to be displayed in the bubble
     *
     * @param message The StreamMessage object containing the message details
     */
    public void setStreamMessage(StreamMessage message) {
        if (!message.equals(this.streamMessage)) {
            this.streamMessage = message;
            streamingBuilder.setLength(0);
            renderMarkdown(streamingBuilder.toString());
        }

        binding.errorLayout.setVisibility(message.isStreamingInterrupted() ? VISIBLE : GONE);

        if (message.getText() == null || message.getText().isEmpty()) {
            String text = binding.streamShimmerTextView.getText().toString();
            if (!text.isEmpty() && isStreaming) {
                return;
            } else {
                binding.streamShimmerTextView.setText(getContext().getString(R.string.cometchat_thinking));
                showAnimatedText();
            }
        } else {
            hideAnimatedText();
            streamingBuilder.setLength(0);
            streamingBuilder.append(message.getText());
            renderMarkdown(streamingBuilder.toString());
        }

        if (message.getRunId() > -1 && message.getMetadata() != null) {
            String event = message.getMetadata().optString(UIKitConstants.AIConstants.AI_ASSISTANT_EVENT_TYPE);
            if (!event.isEmpty() && UIKitConstants.AIAssistantEventType.RUN_STARTED.equals(event)) {
                startStreaming(message);
            }
        }
    }

    /** Starts streaming the AI assistant response
     * @param message        The StreamMessage being processed
     */
    private void startStreaming(StreamMessage message) {
        isStreaming = true;
        CometChatAIStreamService.startStreamingForRunId(message.getRunId(), new CometChatAIStreamService.AIStreamListener() {
            @Override
            public void onError(CometChatException exception) {
                handleError(message, exception);
            }

            @Override
            public void onAIAssistantEventReceived(AIAssistantBaseEvent aiAssistantBaseEvent) {
                handleEventStreaming(aiAssistantBaseEvent, message);
            }
        });
    }

    /** Handles the streaming events and updates the UI accordingly
     *
     * @param aiAssistantBaseEvent The event received from the AI assistant
     * @param message              The StreamMessage being processed
     * //@param contentBuilder       The StringBuilder holding the current content
     */
    private void handleEventStreaming(AIAssistantBaseEvent aiAssistantBaseEvent, StreamMessage message) {
        if (UIKitConstants.AIAssistantEventType.TOOL_CALL_START.equals(aiAssistantBaseEvent.getType())) {
            if (aiAssistantBaseEvent instanceof AIAssistantToolStartedEvent) {
                AIAssistantToolStartedEvent toolStartedEvent = (AIAssistantToolStartedEvent) aiAssistantBaseEvent;
                binding.streamShimmerTextView.setText(toolStartedEvent.getExecutionText());
                showAnimatedText();
            }
        } else if (UIKitConstants.AIAssistantEventType.TEXT_MESSAGE_START.equals(aiAssistantBaseEvent.getType())) {
            hideAnimatedText();
        } else if (aiAssistantBaseEvent.getType().equals(CometChatConstants.WSKeys.AI_ASSISTANT_EVENT_RUN_FINISHED)) {
            handleRunFinished(message);
        }

        if (aiAssistantBaseEvent instanceof AIAssistantContentReceivedEvent) {
            AIAssistantContentReceivedEvent contentEvent = (AIAssistantContentReceivedEvent) aiAssistantBaseEvent;
            String delta = contentEvent.getDelta();
            if (delta != null && !delta.isEmpty()) {
                streamingBuilder.append(delta);
                String updatedText = streamingBuilder.toString();
                message.setText(updatedText);
                renderMarkdown(updatedText);
            }
        }
    }

    /** Handles the completion of the AI assistant response streaming
     *
     * @param message The StreamMessage being processed
     */
    private void handleRunFinished(StreamMessage message) {
        isStreaming = false;
        CometChatAIStreamService.stopStreamingForRunId(message.getRunId());
    }

    /** Handles errors during streaming
     *
     * @param message   The StreamMessage being processed
     * @param exception The exception that occurred
     */
    private void handleError(StreamMessage message, CometChatException exception) {
        isStreaming = false;
        message.setStreamingInterrupted(true);
        CometChatAIStreamService.stopStreamingForRunId(message.getRunId());
        binding.streamShimmerTextView.stopShimmer();
        binding.errorLayout.setVisibility(VISIBLE);
        CometChatLogger.e(TAG, exception.getMessage());
    }

    /**
     * Renders the current content as markdown
     */
    private void renderMarkdown(String content) {
        if (binding != null) {
            if (shouldUpdateContent(content)) {
                adapter.setMarkdown(markwon, content);
                adapter.notifyDataSetChanged();
                lastRenderedContent = content;
            }
        }
    }

    private boolean shouldUpdateContent(String newContent) {
        return !newContent.trim().equalsIgnoreCase(lastRenderedContent.trim());
    }

    /** Applies style attributes from XML to the view
     *
     * @param attrs        The AttributeSet from XML
     * @param defStyleAttr The default style attribute
     * @param defStyleRes  The default style resource
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, defStyleRes);
        try {
            @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStyle, 0);
            typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, style);
            extractAttributesAndApplyDefaults(typedArray);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.getMessage());
        }
    }

    /** Extracts attributes from the TypedArray and applies them to the view, setting defaults where necessary
     *
     * @param typedArray The TypedArray containing the style attributes
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextColor, 0));
            setTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextAppearance, 0));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleBackgroundColor, 0));
            setAvatarStyle(typedArray.getResourceId(R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleAvatarStyle, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /** Sets the avatar style for the AI assistant
     *
     * @param style The style resource ID for the avatar
     */
    public void setAvatarStyle(@StyleRes int style) {
        if (style != 0) {
            binding.ivAvatar.setStyle(style);
        }
    }

    /** Sets the text color for the AI assistant message
     *
     * @param color The color integer for the text
     */
    public void setTextColor(int color) {
        this.rootTextColor = color;
        binding.streamShimmerTextView.setTextColor(color);
    }

    /** Sets the text appearance for the AI assistant message
     *
     * @param textAppearance The style resource ID for the text appearance
     */
    public void setTextAppearance(int textAppearance) {
        this.rootTextAppearance = textAppearance;
        binding.streamShimmerTextView.setTextAppearance(textAppearance);
    }

    /** Sets the background color for the AI assistant bubble
     *
     * @param color The color integer for the background
     */
    public void setBackgroundColor(@ColorInt int color) {
        setCardBackgroundColor(color);
    }

    /** Sets the overall style for the AI assistant bubble
     *
     * @param style The style resource ID for the bubble
     */
    public void setStyle(int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAIAssistantBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /** Sets the avatar for the AI assistant
     *
     * @param name The name to display if the avatar image is not available
     * @param url  The URL of the avatar image
     */
    public void setAvatar(String name, String url) {
        binding.ivAvatar.setAvatar(name, url);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
