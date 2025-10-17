package com.cometchat.chatuikit.shared.views.aiassistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.GrammarLocator;
import com.cometchat.chatuikit.databinding.CometchatAiAssistantMessageBubbleLayoutBinding;
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

public class CometChatAIAssistantMessageBubble extends MaterialCardView {
    private static final String TAG = "CometChatAIAssistantMessageBubble";
    private CometchatAiAssistantMessageBubbleLayoutBinding binding;
    private MarkwonAdapter adapter;
    private Markwon markwon;
    private @ColorInt int rootTextColor;
    private @StyleRes int rootTextAppearance;

    public CometChatAIAssistantMessageBubble(Context context) {
        this(context, null);
    }

    public CometChatAIAssistantMessageBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAIAssistantBubbleStyle);
    }

    public CometChatAIAssistantMessageBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatAiAssistantMessageBubbleLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);

        Prism4j prism4j = new Prism4j(new GrammarLocator());
        markwon = Markwon.builder(getContext())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TableEntryPlugin.create(builder -> {
                    final Dip dip = Dip.create(getContext());
                    builder.tableHeaderRowBackgroundColor(CometChatTheme.getBackgroundColor4(getContext()))
                            .tableEvenRowBackgroundColor(CometChatTheme.getBackgroundColor2(getContext()))
                            .tableOddRowBackgroundColor(CometChatTheme.getBackgroundColor2(getContext()))
                            .tableBorderWidth(dip.toPx(1))
                            .tableBorderColor(CometChatTheme.getStrokeColorDark(getContext()))
                            .tableCellPadding(dip.toPx(8));
                }))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))
                .usePlugin(new AbstractMarkwonPlugin() {
                               @Override
                               public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
                                   super.configureSpansFactory(builder);
                                   builder.setFactory(Code.class, (configuration, props) -> new CustomInlineCodeSpan(
                                           Utils.getTextSize(getContext(), rootTextAppearance),
                                           CometChatTheme.getNeutralColor800(getContext()), // background
                                           rootTextColor, // text color
                                           18f // corner radius
                                   ));
                               }

                               @Override
                               public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                   super.configureTheme(builder);
                                   builder.codeBlockBackgroundColor(CometChatTheme.getBackgroundColor1(getContext())) // code block background color
                                           .linkColor(CometChatTheme.getPrimaryColor(getContext()))
                                           .isLinkUnderlined(true);
                               }
                           }
                )
                .build();

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
                .include(TableBlock.class, TableEntry.create(builder -> {
                    builder.tableLayout(R.layout.cometchat_ai_assistant_table_layout, R.id.table_layout)
                            .textLayoutIsRoot(R.layout.cometchat_ai_assistant_table_entry_cell);
                }))
                .build();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatAIAssistantBubble_cometchatAIAssistantBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAIAssistantBubble, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTextColor(typedArray.getColor(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextColor, 0));
            setTextAppearance(typedArray.getResourceId(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleTextAppearance, 0));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatAIAssistantBubble_cometChatAIAssistantBubbleBackgroundColor, 0));
        } finally {
            typedArray.recycle();
        }
    }

    public void setTextColor(int color) {
        this.rootTextColor = color;
    }

    public void setTextAppearance(@StyleRes int textAppearance) {
        rootTextAppearance = textAppearance;
    }

    public void setStyle(int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAIAssistantBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public void setBackgroundColor(@ColorInt int color) {
        setCardBackgroundColor(color);
    }

    private void copyCode(String code) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Code", code);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void setAIAssistantMessage(AIAssistantMessage aiAssistantMessage) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(aiAssistantMessage.getText());
        renderMarkdown(contentBuilder);
    }

    private void renderMarkdown(StringBuilder contentBuilder) {
        if (binding != null) {
            adapter.setMarkdown(markwon, contentBuilder.toString());
            adapter.notifyDataSetChanged();
        }
    }
}
