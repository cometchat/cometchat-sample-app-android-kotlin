package com.cometchat.chatuikit.aiassistantchathistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatAiAssistantChatHistoryItemBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.sticker_header.StickyHeaderAdapter;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.cometchat.chatuikit.shared.views.date.Pattern;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AIAssistantChatHistoryAdapter extends RecyclerView.Adapter<AIAssistantChatHistoryAdapter.MessageViewHolder> implements StickyHeaderAdapter<AIAssistantChatHistoryAdapter.DateItemHolder> {
    private List<BaseMessage> messages;
    private final Context context;
    private @ColorInt int itemBackgroundColor;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int itemTextColor;
    private OnItemClick<BaseMessage> onItemClickListener;
    private OnItemLongClick<BaseMessage> onItemLongClickListener;

    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateSeparatorFormat;
    private final DateTimeFormatterCallback dateTimeFormatter;

    private @ColorInt int dateSeparatorTextColor;
    private @StyleRes int dateSeparatorTextAppearance;
    private @ColorInt int dateSeparatorBackgroundColor;

    public AIAssistantChatHistoryAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
        this.dateTimeFormatter = CometChatUIKit.getAuthSettings().getDateTimeFormatterCallback();
        this.timeFormat = new SimpleDateFormat("h:mm a", CometChatLocalize.getDefault());
        this.dateSeparatorFormat = new SimpleDateFormat("MMM dd, yyyy", CometChatLocalize.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatAiAssistantChatHistoryItemBinding binding = CometchatAiAssistantChatHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        BaseMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public BaseMessage getItem(int position) {
        if (position >= 0 && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }

    public void setMessageList(List<BaseMessage> messageList) {
        this.messages = messageList;
        notifyDataSetChanged();
    }

    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
    }

    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
    }

    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
    }

    /**
     * Set the item click listener
     *
     * @param listener The click listener to set
     */
    public void setOnItemClickListener(OnItemClick<BaseMessage> listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Set the item long click listener
     *
     * @param listener The click listener to set
     */
    public void setOnItemLongClickListener(OnItemLongClick<BaseMessage> listener) {
        this.onItemLongClickListener = listener;
    }

    @Override
    public long getHeaderId(int var1) {
        if (messages.size() > var1) {
            BaseMessage baseMessage = messages.get(var1);
            return Long.parseLong(Utils.getDateId(baseMessage.getSentAt() * 1000));
        } else return 0;
    }

    @Override
    public DateItemHolder onCreateHeaderViewHolder(ViewGroup var1) {
        return new DateItemHolder(LayoutInflater.from(var1.getContext()).inflate(R.layout.cometchat_ai_assistant_chat_history_sticky_header, var1, false));
    }

    @Override
    public void onBindHeaderViewHolder(DateItemHolder holder, int var2, long var3) {
        // Check if the provided index is valid for the baseMessageList
        if (messages.size() > var2) {
            BaseMessage baseMessage = messages.get(var2); // Retrieve the message for the header

            if (baseMessage.getSentAt() > 0) {
                holder.txtMessageDate.setDateFormat(dateSeparatorFormat);
                holder.txtMessageDate.setDateTimeFormatterCallback(dateTimeFormatter);
                holder.txtMessageDate.setDate(baseMessage.getSentAt(), Pattern.DAY_DATE);
            } else {
                holder.txtMessageDate.setDateText(context.getString(R.string.cometchat_updating));
            }
            // Apply the style to the date text view
            holder.txtMessageDate.setDateTextColor(dateSeparatorTextColor);
            if (dateSeparatorTextAppearance != 0) holder.txtMessageDate.setDateTextAppearance(dateSeparatorTextAppearance);
            holder.txtMessageDate.setBackgroundColor(dateSeparatorBackgroundColor);
        }
    }

    public void setDateSeparatorTextColor(@ColorInt int dateSeparatorTextColor) {
        this.dateSeparatorTextColor = dateSeparatorTextColor;
    }

    public void setDateSeparatorTextAppearance(@StyleRes int dateSeparatorTextAppearance) {
        this.dateSeparatorTextAppearance = dateSeparatorTextAppearance;
    }

    public void setDateSeparatorBackgroundColor(@ColorInt int backgroundColor3) {
        this.dateSeparatorBackgroundColor = backgroundColor3;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private final CometchatAiAssistantChatHistoryItemBinding binding;

        public MessageViewHolder(CometchatAiAssistantChatHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BaseMessage message) {
            String messageText = "";
            if (message instanceof TextMessage) {
                messageText = ((TextMessage) message).getText();
            }

            binding.tvMessageText.setText(messageText);
            binding.tvMessageText.setTextAppearance(itemTextAppearance);
            binding.tvMessageText.setTextColor(itemTextColor);
            binding.chatHistoryItemParent.setBackgroundColor(itemBackgroundColor);
        }
    }

    /**
     * ViewHolder for displaying date items in chat history.
     */
    public static class DateItemHolder extends RecyclerView.ViewHolder {
        public CometChatDate txtMessageDate; // Text view for displaying the date
        /**
         * Constructs a DateItemHolder with the specified item view.
         *
         * @param itemView The inflated view for the date item.
         */
        DateItemHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageDate = itemView.findViewById(R.id.txt_message_date); // Initialize date text view
        }
    }
}
