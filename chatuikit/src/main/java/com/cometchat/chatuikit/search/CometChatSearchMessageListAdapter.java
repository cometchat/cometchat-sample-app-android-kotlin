package com.cometchat.chatuikit.search;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cometchat.chat.models.AppEntity;
import com.cometchat.chat.models.Attachment;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemAudioBinding;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemDocumentBinding;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemImageBinding;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemLinkBinding;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemTextBinding;
import com.cometchat.chatuikit.databinding.CometchatSearchMessageItemVideoBinding;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderAdapter;
import com.cometchat.chatuikit.shared.utils.SubtitleView;
import com.cometchat.chatuikit.shared.viewholders.MessagesSearchViewHolderListener;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.cometchat.chatuikit.shared.views.date.Pattern;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CometChatSearchMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderAdapter<CometChatSearchMessageListAdapter.DateItemHolder> {
    private static final int IMAGE_MESSAGE = 1;
    private static final int LINK_MESSAGE = 2;
    private static final int AUDIO_MESSAGE = 3;
    private static final int VIDEO_MESSAGE = 4;
    private static final int FILE_MESSAGE = 5;
    private static final int TEXT_MESSAGE = 6;
    private static final int IGNORE_MESSAGE = 0;
    private static final String TAG = CometChatSearchMessageListAdapter.class.getSimpleName();
    private final Context context;
    private List<BaseMessage> messagesList = new ArrayList<>();
    private String uid;
    private String guid;

    private List<CometChatTextFormatter> textFormatters;

    // ViewHolder listeners
    private MessagesSearchViewHolderListener<TextMessage> textItemViewHolder;
    private MessagesSearchViewHolderListener<TextMessage> linkItemViewHolder;
    private MessagesSearchViewHolderListener<MediaMessage> videoItemViewHolder;
    private MessagesSearchViewHolderListener<MediaMessage> imageItemViewHolder;
    private MessagesSearchViewHolderListener<MediaMessage> audioItemViewHolder;
    private MessagesSearchViewHolderListener<MediaMessage> documentItemViewHolder;

    private final SimpleDateFormat dateSeparatorFormat;
    private final DateTimeFormatterCallback dateTimeFormatter;
    private SimpleDateFormat messageTimestampDateFormat;

    private @ColorInt int dateSeparatorTextColor;
    private @StyleRes int dateSeparatorTextAppearance;
    private @ColorInt int dateSeparatorBackgroundColor;

    private @ColorInt int messageItemBackgroundColor;
    private @ColorInt int messageTitleTextColor;
    private @StyleRes int messageTitleTextAppearance;
    private @ColorInt int messageSubtitleTextColor;
    private @StyleRes int messageSubtitleTextAppearance;
    private @ColorInt int messageTimestampTextColor;
    private @StyleRes int messageTimestampTextAppearance;
    private @ColorInt int messageLinkTextColor;
    private @StyleRes int messageLinkTextAppearance;
    private Drawable messageThreadIcon;

    public CometChatSearchMessageListAdapter(Context context) {
        this.context = context;
        this.dateSeparatorFormat = new SimpleDateFormat("MMM, yyyy", CometChatLocalize.getDefault());
        this.dateTimeFormatter = CometChatUIKit.getAuthSettings().getDateTimeFormatterCallback();
        textFormatters = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE_MESSAGE) {
            return new SearchImageViewHolder(getView(parent, R.layout.cometchat_search_message_item_image));
        } else if (viewType == VIDEO_MESSAGE) {
            return new SearchVideoViewHolder(getView(parent, R.layout.cometchat_search_message_item_video));
        } else if (viewType == FILE_MESSAGE) {
            return new SearchDocumentViewHolder(getView(parent, R.layout.cometchat_search_message_item_document));
        } else if (viewType == LINK_MESSAGE) {
            return new SearchLinkViewHolder(getView(parent, R.layout.cometchat_search_message_item_link));
        } else if (viewType == AUDIO_MESSAGE) {
            return new SearchAudioViewHolder(getView(parent, R.layout.cometchat_search_message_item_audio));
        } else if (viewType == TEXT_MESSAGE) {
            return new SearchTextViewHolder(getView(parent, R.layout.cometchat_search_message_item_text));
        } else {
            return new EmptyRowHolder(getView(parent, R.layout.cometchat_empty_view_holder_row));
        }
    }

    private View getView(ViewGroup parent, int view) {
        return LayoutInflater.from(parent.getContext()).inflate(view, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage baseMessage = messagesList.get(position);
        if (holder instanceof SearchImageViewHolder) {
            ((SearchImageViewHolder) holder).bindView((MediaMessage) baseMessage, position);
        } else if (holder instanceof SearchVideoViewHolder) {
            ((SearchVideoViewHolder) holder).bindView((MediaMessage) baseMessage, position);
        } else if (holder instanceof SearchLinkViewHolder) {
            ((SearchLinkViewHolder) holder).bindView((TextMessage) baseMessage, position);
        } else if (holder instanceof SearchDocumentViewHolder) {
            ((SearchDocumentViewHolder) holder).bindView((MediaMessage) baseMessage, position);
        } else if (holder instanceof SearchAudioViewHolder) {
            ((SearchAudioViewHolder) holder).bindView((MediaMessage) baseMessage, position);
        } else if (holder instanceof SearchTextViewHolder) {
            ((SearchTextViewHolder) holder).bindView((TextMessage) baseMessage, position);
        } else {
            Log.e("", "onBindViewHolder: Unknown ViewHolder");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    private int getItemViewTypes(int position) {
        BaseMessage message = messagesList.get(position);
        if (!message.getCategory().equals(UIKitConstants.MessageCategory.MESSAGE)){
            return IGNORE_MESSAGE;
        }

        switch (message.getType()) {
            case UIKitConstants.MessageType.IMAGE:
                return IMAGE_MESSAGE;
            case UIKitConstants.MessageType.VIDEO:
                return VIDEO_MESSAGE;
            case UIKitConstants.MessageType.FILE:
                return FILE_MESSAGE;
            case UIKitConstants.MessageType.TEXT:
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    if (text != null && !text.isEmpty()) {
                        if (hasLinkInMessage(text)) return LINK_MESSAGE;
                        else return TEXT_MESSAGE;
                    }
                }
            case UIKitConstants.MessageType.AUDIO:
                return AUDIO_MESSAGE;
            default:
                return IGNORE_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public void setMessages(List<BaseMessage> messagesList) {
        this.messagesList = messagesList;
        notifyDataSetChanged();
    }

    private void loadThumbnail(String url, ImageView ivThumbnail) {
        try {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.cometchat_image_placeholder)
                    .error(R.drawable.cometchat_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(ivThumbnail);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.getMessage());
        }
    }

    private boolean hasLinkInMessage(String message) {
        String urlPattern = "((http|https|ftp|ftps)://)?([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/\\S*)?";
        return message.matches(".*" + urlPattern + ".*");
    }

    private void bindTailView(CometChatDate date, BaseMessage baseMessage, int textColor, int textAppearance) {
        long timestamp = baseMessage.getUpdatedAt() * 1000;
        String formattedDate;
        if (messageTimestampDateFormat != null) {
            formattedDate = messageTimestampDateFormat.format(new java.util.Date(timestamp));
        } else {
            formattedDate = new SimpleDateFormat("dd MMM, yyyy", CometChatLocalize.getDefault()).format(new java.util.Date(timestamp));
        }
        date.setDateText(formattedDate);
        date.setDateTextColor(textColor);
        if (textAppearance != 0) date.setDateTextAppearance(textAppearance);
    }

    private void bindSubtitleView(SubtitleView messageSubtitle, TextMessage textMessage) {
        messageSubtitle.showTypingIndicator(false);
        messageSubtitle.showSubtitleViewContainer(true);
        messageSubtitle.hideMessageReceiptIcon(true);
        messageSubtitle.showMessageTypeIconView(false);
        if (uid != null || guid != null) {
            messageSubtitle.setSenderNameText("");
        } else {
            String sender = !Objects.equals(textMessage.getSender().getUid(), CometChatUIKit.getLoggedInUser().getUid()) ? textMessage.getSender().getName() : context.getString(R.string.cometchat_you);
            messageSubtitle.setSenderNameText(sender + ": ");
        }
        SpannableString spannableString = SpannableString.valueOf(FormatterUtils.getFormattedText(context,
                textMessage,
                UIKitConstants.FormattingType.CONVERSATIONS,
                null,
                textMessage.getText(),
                textFormatters != null && !textFormatters.isEmpty() ? textFormatters : new ArrayList<>()));
        messageSubtitle.setLastMessageText(spannableString);

        if (messageSubtitleTextAppearance != 0) messageSubtitle.setLastMessageTextAppearance(messageSubtitleTextAppearance);
        if (messageSubtitleTextColor != 0) messageSubtitle.setLastMessageTextColor(messageSubtitleTextColor);
    }

    public BaseMessage getItemByPosition(int position) {
        if (messagesList == null || position < 0 || position >= messagesList.size()) return null;
        return messagesList.get(position);
    }

    private void setTextAndStyle(TextView textView, String text, int color, int textAppearance) {
        textView.setText(text);
        if (color != 0) textView.setTextColor(color);
        if (textAppearance != 0) textView.setTextAppearance(textAppearance);
    }

    private String getConversationTitle(BaseMessage baseMessage) {
        if (uid != null || guid != null) {
            String senderUid = baseMessage.getSender().getUid();
            if (Objects.equals(senderUid, CometChatUIKit.getLoggedInUser().getUid())) {
                return context.getString(R.string.cometchat_you);
            }
            return baseMessage.getSender().getName();
        }
        String title;
        AppEntity appEntity = baseMessage.getReceiver();
        if (appEntity instanceof Group) {
            title = ((Group) appEntity).getName();
        } else {
            title = baseMessage.getSender().getName();
        }
        return title;
    }

    public void setItemBackgroundColor(@ColorInt int messageItemBackgroundColor) {
        this.messageItemBackgroundColor = messageItemBackgroundColor;
    }

    public void setMessageTitleTextColor(@ColorInt int messageTitleTextColor) {
        this.messageTitleTextColor = messageTitleTextColor;
    }

    public void setMessageTitleTextAppearance(@StyleRes int messageTitleTextAppearance) {
        this.messageTitleTextAppearance = messageTitleTextAppearance;
    }

    public void setMessageSubtitleTextColor(@ColorInt int messageSubtitleTextColor) {
        this.messageSubtitleTextColor = messageSubtitleTextColor;
    }

    public void setMessageSubtitleTextAppearance(@StyleRes int messageSubtitleTextAppearance) {
        this.messageSubtitleTextAppearance = messageSubtitleTextAppearance;
    }

    public void setMessageTimestampTextColor(@ColorInt int messageTimestampTextColor) {
        this.messageTimestampTextColor = messageTimestampTextColor;
    }

    public void setMessageTimestampTextAppearance(@StyleRes int messageTimestampTextAppearance) {
        this.messageTimestampTextAppearance = messageTimestampTextAppearance;
    }

    public void setMessageLinkTextColor(@ColorInt int messageLinkTextColor) {
        this.messageLinkTextColor = messageLinkTextColor;
    }

    public void setMessageLinkTextAppearance(@StyleRes int messageLinkTextAppearance) {
        this.messageLinkTextAppearance = messageLinkTextAppearance;
    }

    public void setImageMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        this.imageItemViewHolder = viewHolderListener;
    }

    public void setAudioMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        this.audioItemViewHolder = viewHolderListener;
    }

    public void setVideoMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        this.videoItemViewHolder = viewHolderListener;
    }

    public void setDocumentMessageItemView(MessagesSearchViewHolderListener<MediaMessage> viewHolderListener) {
        this.documentItemViewHolder = viewHolderListener;
    }

    public void setLinkMessageItemView(MessagesSearchViewHolderListener<TextMessage> viewHolderListener) {
        this.linkItemViewHolder = viewHolderListener;
    }

    public void setTextMessageItemView(MessagesSearchViewHolderListener<TextMessage> viewHolderListener) {
        this.textItemViewHolder = viewHolderListener;
    }

    public void setDateSeparatorTextColor(int dateSeparatorTextColor) {
        this.dateSeparatorTextColor = dateSeparatorTextColor;
    }

    public void setDateSeparatorBackgroundColor(int dateSeparatorBackgroundColor) {
        this.dateSeparatorBackgroundColor = dateSeparatorBackgroundColor;
    }

    public void setDateSeparatorTextAppearance(int dateSeparatorTextAppearance) {
        this.dateSeparatorTextAppearance = dateSeparatorTextAppearance;
    }

    public void setMessageTimestampDateFormat(SimpleDateFormat messageDateFormat) {
        this.messageTimestampDateFormat = messageDateFormat;
    }

    public void setTextFormatters(List<CometChatTextFormatter> textFormatters) {
        this.textFormatters = textFormatters;
    }

    public void setMessageThreadIcon(Drawable threadIcon) {
        this.messageThreadIcon = threadIcon;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    private class SearchImageViewHolder extends RecyclerView.ViewHolder {
        private View customView;
        private final CometchatSearchMessageItemImageBinding binding;


        public SearchImageViewHolder(View itemView) {
            super(itemView);
            binding = CometchatSearchMessageItemImageBinding.bind(itemView);

            if (imageItemViewHolder != null) {
                customView = imageItemViewHolder.createView(context, itemView);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(MediaMessage mediaMessage, int position) {
            if (imageItemViewHolder != null) {
                imageItemViewHolder.bindView(context, customView, mediaMessage, this, messagesList, position);
            } else {
                if (messageItemBackgroundColor != 0) {
                    binding.parentLayout.setBackgroundColor(messageItemBackgroundColor);
                }
                if (mediaMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }
                String title = getConversationTitle(mediaMessage);
                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                String fileName = mediaMessage.getAttachment().getFileName() != null ? mediaMessage.getAttachment().getFileName() : context.getString(R.string.cometchat_message_image);
                setTextAndStyle(binding.tvSubtitleView, fileName, messageSubtitleTextColor, messageSubtitleTextAppearance);
                String imageUrl = mediaMessage.getAttachment().getFileUrl();
                if (context != null) {
                    loadThumbnail(imageUrl, binding.ivThumbnail);
                }
            }
        }
    }

    private class SearchVideoViewHolder extends RecyclerView.ViewHolder {
        private View customView;
        private final CometchatSearchMessageItemVideoBinding binding;

        public SearchVideoViewHolder(View itemView) {
            super(itemView);
            binding = CometchatSearchMessageItemVideoBinding.bind(itemView);

            if (videoItemViewHolder != null) {
                customView = videoItemViewHolder.createView(context, itemView);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(MediaMessage mediaMessage, int position) {
            if (videoItemViewHolder != null) {
                videoItemViewHolder.bindView(context, customView, mediaMessage, this, messagesList, position);
            } else {
                if (messageItemBackgroundColor != 0) {
                    binding.parentLayout.setBackgroundColor(messageItemBackgroundColor);
                }
                if (mediaMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }
                String title = getConversationTitle(mediaMessage);
                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                String fileName = mediaMessage.getAttachment().getFileName() != null ? mediaMessage.getAttachment().getFileName() : context.getString(R.string.cometchat_message_video);
                setTextAndStyle(binding.tvSubtitleView, fileName, messageSubtitleTextColor, messageSubtitleTextAppearance);
                String imageUrl = mediaMessage.getAttachment().getFileUrl();
                if (context != null) {
                    loadThumbnail(imageUrl, binding.videoThumbnail);
                    binding.videoThumbnail.setVisibility(VISIBLE);
                    binding.playButtonLayout.setVisibility(VISIBLE);
                }
            }
        }
    }

    private class SearchLinkViewHolder extends RecyclerView.ViewHolder {
        private View customView;
        private final CometchatSearchMessageItemLinkBinding binding;

        public SearchLinkViewHolder(View itemView) {
            super(itemView);
            binding = CometchatSearchMessageItemLinkBinding.bind(itemView);

            if (linkItemViewHolder != null) {
                customView = linkItemViewHolder.createView(context, itemView);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(TextMessage textMessage, int position) {
            if (linkItemViewHolder != null) {
                linkItemViewHolder.bindView(context, customView, textMessage, this, messagesList, position);
            } else {
                if (messageItemBackgroundColor != 0) {
                    binding.parentLayout.setBackgroundColor(messageItemBackgroundColor);
                }
                String title = getConversationTitle(textMessage);
                HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(textMessage);
                if (extensionList != null) {
                    if (extensionList.containsKey(ExtensionConstants.ExtensionJSONField.LINK_PREVIEW)) {
                        JSONObject linkPreviewJsonObject = extensionList.get(ExtensionConstants.ExtensionJSONField.LINK_PREVIEW);
                        try {
                            if (linkPreviewJsonObject != null) {
                                String text = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.URL);
                                String favIconUrl = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.FAV_ICON);
                                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                                setTextAndStyle(binding.tvSubtitleView, text, messageLinkTextColor, messageLinkTextAppearance);
                                loadThumbnail(favIconUrl, binding.messageIvLinkThumbnail);
                            }
                        } catch (Exception e) {
                            CometChatLogger.e(TAG, e.toString());
                        }
                    } else  {
                        setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                        setTextAndStyle(binding.tvSubtitleView, textMessage.getText(), messageLinkTextColor, messageLinkTextAppearance);
                        binding.messageIvLinkThumbnail.setImageResource(R.drawable.cometchat_image_placeholder);
                    }
                } else {
                    setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                    setTextAndStyle(binding.tvSubtitleView, textMessage.getText(), messageLinkTextColor, messageLinkTextAppearance);
                    binding.messageIvLinkThumbnail.setImageResource(R.drawable.cometchat_image_placeholder);
                }

                if (textMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }

                // Bind tail view
                bindTailView(binding.date, textMessage, messageTimestampTextColor, messageTimestampTextAppearance);
            }
        }
    }

    private class SearchDocumentViewHolder extends RecyclerView.ViewHolder {
        private View customView;
        private final CometchatSearchMessageItemDocumentBinding binding;

        public SearchDocumentViewHolder(View itemView) {
            super(itemView);
            binding = CometchatSearchMessageItemDocumentBinding.bind(itemView);

            if (documentItemViewHolder != null) {
                customView = documentItemViewHolder.createView(context, itemView);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(MediaMessage mediaMessage, int position) {
            if (documentItemViewHolder != null) {
                documentItemViewHolder.bindView(context, customView, mediaMessage, this, messagesList, position);
            } else {
                // Set title
                String title = getConversationTitle(mediaMessage);
                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                Attachment attachment = mediaMessage.getAttachment();
                // Set subtitle
                String fileName = mediaMessage.getAttachment().getFileName() != null  ? mediaMessage.getAttachment().getFileName() : context.getString(R.string.cometchat_message_document);
                setTextAndStyle(binding.tvSubtitleView, fileName, messageSubtitleTextColor, messageSubtitleTextAppearance);
                if (attachment.getFileExtension() != null) {
                    if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.VIDEO)) {
                        setFileIcon(R.drawable.cometchat_video_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.OCTET_STREAM)) {
                        if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.DOC)) {
                            setFileIcon(R.drawable.cometchat_word_file_icon);
                        } else if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.PPT)) {
                            setFileIcon(R.drawable.cometchat_ppt_file_icon);
                        } else if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.XLS)) {
                            setFileIcon(R.drawable.cometchat_xlsx_file_icon);
                        }
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.PDF)) {
                        setFileIcon(R.drawable.cometchat_pdf_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.ZIP)) {
                        setFileIcon(R.drawable.cometchat_zip_file_icon);
                    } else if (attachment.getFileUrl().contains(UIKitConstants.MimeType.CSV)) {
                        setFileIcon(R.drawable.cometchat_text_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.AUDIO)) {
                        setFileIcon(R.drawable.cometchat_audio_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.IMAGE)) {
                        setFileIcon(R.drawable.cometchat_image_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.TEXT)) {
                        setFileIcon(R.drawable.cometchat_text_file_icon);
                    } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.LINK)) {
                        setFileIcon(R.drawable.cometchat_link_file_icon);
                    } else {
                        setFileIcon(R.drawable.cometchat_unknown_file_icon);
                    }
                }

                if (mediaMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }
                // Set tail view
                bindTailView(binding.date, mediaMessage, messageTimestampTextColor, messageTimestampTextAppearance);
            }
        }

        public void setFileIcon(@DrawableRes int image) {
            if (image != 0) {
                binding.messageIvDocumentThumbnail.setImageResource(image);
            }
        }
    }

    private class SearchAudioViewHolder extends RecyclerView.ViewHolder {
        private View customView;
        private final CometchatSearchMessageItemAudioBinding binding;

        public SearchAudioViewHolder(View itemView) {
            super(itemView);
            binding = CometchatSearchMessageItemAudioBinding.bind(itemView);

            if (audioItemViewHolder != null) {
                customView = audioItemViewHolder.createView(context, itemView);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(MediaMessage audioMessage, int position) {
            if (audioItemViewHolder != null) {
                audioItemViewHolder.bindView(context, customView, audioMessage, this, messagesList, position);
            } else {
                String title = getConversationTitle(audioMessage);
                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);
                String fileName = audioMessage.getAttachment().getFileName() != null ? audioMessage.getAttachment().getFileName() : context.getString(R.string.cometchat_message_audio);
                setTextAndStyle(binding.tvSubtitleView, fileName, messageSubtitleTextColor, messageSubtitleTextAppearance);
                if (audioMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }
                // Bind tail view
                bindTailView(binding.date, audioMessage, messageTimestampTextColor, messageTimestampTextAppearance);
            }
        }
    }

    private class SearchTextViewHolder extends RecyclerView.ViewHolder {
        private final CometchatSearchMessageItemTextBinding binding;
        private View customView;

        public SearchTextViewHolder(View view) {
            super(view);
            binding = CometchatSearchMessageItemTextBinding.bind(view);

            if (textItemViewHolder != null) {
                customView = textItemViewHolder.createView(context, view);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            }
        }

        public void bindView(TextMessage textMessage, int position) {
            if (textItemViewHolder != null) {
                textItemViewHolder.bindView(context, customView, textMessage, this, messagesList, position);
            } else {
                if (messageItemBackgroundColor != 0) {
                    binding.parentLayout.setBackgroundColor(messageItemBackgroundColor);
                }
                String title = getConversationTitle(textMessage);
                if (textMessage.getParentMessageId() > 0) {
                    binding.icThreadMessage.setImageDrawable(messageThreadIcon);
                    binding.icThreadMessage.setVisibility(VISIBLE);
                } else {
                    binding.icThreadMessage.setVisibility(GONE);
                }
                // Title
                setTextAndStyle(binding.tvMessageTitle, title, messageTitleTextColor, messageTitleTextAppearance);

                // Subtitle
                bindSubtitleView(binding.messageSubtitle, textMessage);

                // Bind tail view
                bindTailView(binding.date, textMessage, messageTimestampTextColor, messageTimestampTextAppearance);
            }
        }
    }

    @Override
    public long getHeaderId(int var1) {
        if (messagesList.size() > var1 && IGNORE_MESSAGE != getItemViewTypes(var1)) {
            BaseMessage baseMessage = messagesList.get(var1);
            return Utils.getMonthId(baseMessage.getSentAt() * 1000);
        } else return 0;
    }

    @Override
    public CometChatSearchMessageListAdapter.DateItemHolder onCreateHeaderViewHolder(ViewGroup var1) {
        return new CometChatSearchMessageListAdapter.DateItemHolder(LayoutInflater.from(var1.getContext()).inflate(R.layout.cometchat_search_sticky_header, var1, false));
    }

    @Override
    public void onBindHeaderViewHolder(DateItemHolder holder, int position, long var3) {
        holder.bindDate(position);
    }

    /**
     * ViewHolder for displaying date items in chat history.
     */
    public class DateItemHolder extends RecyclerView.ViewHolder {
        public CometChatDate txtMessageDate; // Text view for displaying the date
        public LinearLayout parent;
        /**
         * Constructs a DateItemHolder with the specified item view.
         *
         * @param itemView The inflated view for the date item.
         */
        DateItemHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageDate = itemView.findViewById(R.id.txt_message_date);
            parent = itemView.findViewById(R.id.sticky_header_parent_layout);
        }

        public void bindDate(int position) {
            if (messagesList.size() > position) {
                BaseMessage baseMessage = messagesList.get(position);
                if (baseMessage.getSentAt() > 0 && UIKitConstants.MessageCategory.MESSAGE.equalsIgnoreCase(baseMessage.getCategory())) {
                    parent.setVisibility(VISIBLE);
                    parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    txtMessageDate.setDateFormat(dateSeparatorFormat);
                    txtMessageDate.setDateTimeFormatterCallback(dateTimeFormatter);
                    txtMessageDate.setDate(baseMessage.getSentAt(), Pattern.DAY_DATE);
                    txtMessageDate.setBackgroundColor(dateSeparatorBackgroundColor);
                    txtMessageDate.setDateTextColor(dateSeparatorTextColor);
                    if (dateSeparatorTextAppearance != 0) txtMessageDate.setDateTextAppearance(dateSeparatorTextAppearance);
                } else {
                    parent.setVisibility(GONE);
                    parent.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                }
            } else {
                parent.setVisibility(GONE);
                parent.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
        }
    }

    /**
     * ViewHolder for empty rows in the RecyclerView.
     */
    public static class EmptyRowHolder extends RecyclerView.ViewHolder {
        public EmptyRowHolder(View inflate) {
            super(inflate);
        }
    }
}