package com.cometchat.chatuikit.conversations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatConversationsListItemsBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.ConversationTailView;
import com.cometchat.chatuikit.shared.utils.ConversationsUtils;
import com.cometchat.chatuikit.shared.utils.SubtitleView;
import com.cometchat.chatuikit.shared.viewholders.ConversationsViewHolderListener;
import com.cometchat.chatuikit.shared.views.statusindicator.StatusIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NotifyDataSetChanged")
@SuppressWarnings("unused")
public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ConversationsAdapter.class.getSimpleName();
    private final Context context;
    private boolean hideReceipts = false;
    private boolean hideUserStatus = false;
    private boolean hideGroupType = false;
    private List<CometChatTextFormatter> formatters;
    private List<Conversation> conversationsList;
    private SimpleDateFormat dateFormat;
    private DateTimeFormatterCallback dateTimeFormatter;
    private HashMap<Conversation, Boolean> selectedConversation;
    private HashMap<Conversation, TypingIndicator> typingIndicatorHashMap;
    private ConversationsViewHolderListener titleView, leadingView, subtitleViewHolder, trailingViewHolder, itemViewHolder;
    private @ColorInt int conversationsEmptyStateTitleTextColor;
    private @ColorInt int conversationsEmptyStateSubtitleTextColor;
    private @ColorInt int conversationsErrorStateTitleTextColor;
    private @ColorInt int conversationsErrorStateSubtitleTextColor;
    private @ColorInt int conversationsItemTitleTextColor;
    private @ColorInt int conversationsItemSubtitleTextColor;
    private @ColorInt int conversationsItemMessageTypeIconTint;
    private @StyleRes int conversationsEmptyStateTextTitleAppearance;
    private @StyleRes int conversationsEmptyStateTextSubtitleAppearance;
    private @StyleRes int conversationsErrorStateTextTitleAppearance;
    private @StyleRes int conversationsErrorStateTextSubtitleAppearance;
    private @StyleRes int conversationsItemTitleTextAppearance;
    private @StyleRes int conversationsItemSubtitleTextAppearance;
    private @StyleRes int conversationsAvatarStyle;
    private @StyleRes int conversationsStatusIndicatorStyle;
    private @StyleRes int conversationsDateStyle;
    private @StyleRes int conversationsBadgeStyle;
    private @StyleRes int conversationsReceiptStyle;
    private @StyleRes int conversationsTypingIndicatorStyle;
    private @ColorInt int itemSelectedBackgroundColor;
    private @ColorInt int itemBackgroundColor;
    private int checkBoxStrokeWidth;
    private int checkBoxCornerRadius;
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;
    private Drawable checkBoxSelectIcon;
    private @ColorInt int checkBoxSelectIconTint;
    private boolean isSelectionEnabled = false;


    /**
     * Constructor for ConversationsAdapter. Initializes the adapter with the
     * provided context and sets up internal data structures such as the
     * conversation list, selected conversations map, typing indicator map, and
     * additional parameters.
     *
     * @param context the context in which the adapter is operating.
     */
    public ConversationsAdapter(Context context) {
        this.context = context;
        conversationsList = new ArrayList<>();
        selectedConversation = new HashMap<>();
        typingIndicatorHashMap = new HashMap<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatConversationsListItemsBinding binding = CometchatConversationsListItemsBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new ConversationsViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ConversationsViewHolder) holder).bindView(conversationsList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getItemCount() {
        return conversationsList.size();
    }

    /**
     * Sets the list of conversations and refreshes the adapter.
     *
     * @param conversations the list of conversations to be displayed.
     */
    public void setList(List<Conversation> conversations) {
        conversationsList = conversations;
        notifyDataSetChanged();
    }

    /**
     * Retrieves a conversation at a specific position.
     *
     * @param position the position of the conversation.
     * @return the conversation at the specified position, or null if not found.
     */
    public Conversation getConversation(int position) {
        try {
            return conversationsList.get(position);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
            return null;
        }
    }

    /**
     * Adds a list of conversations to the existing list, replacing any existing
     * ones.
     *
     * @param conversations the list of conversations to be added.
     */
    public void addList(List<Conversation> conversations) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversationsList.contains(conversations.get(i))) {
                int index = conversationsList.indexOf(conversations.get(i));
                conversationsList.remove(conversations.get(i));
                conversationsList.add(index, conversations.get(i));
            } else {
                conversationsList.add(conversations.get(i));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Updates an existing conversation in the list and notifies the adapter.
     *
     * @param conversation the conversation to be updated.
     */
    public void updateConversation(Conversation conversation) {
        if (conversationsList.contains(conversation)) {
            conversationsList.set(conversationsList.indexOf(conversation), conversation);
            notifyItemChanged(conversationsList.indexOf(conversation), conversation);
        }
    }

    /**
     * Adds a new conversation to the top of the list and notifies the adapter.
     *
     * @param conversation the conversation to be added.
     */
    public void notifyNewConversationAdded(Conversation conversation) {
        conversationsList.add(0, conversation);
        notifyItemInserted(0);
    }

    /**
     * Moves an existing conversation to the top of the list.
     *
     * @param conversation the conversation to be moved.
     */
    public void notifyMoveConversationToTop(Conversation conversation) {
        if (conversationsList.contains(conversation)) {
            notifyItemMoved(conversationsList.indexOf(conversation), 0);
            conversationsList.remove(conversation);
            conversationsList.add(0, conversation);
        }
    }

    /**
     * Adds a conversation to the list and notifies the adapter.
     *
     * @param conversation the conversation to be added.
     */
    public void add(Conversation conversation) {
        if (!conversationsList.contains(conversation)) {
            conversationsList.add(conversation);
            notifyItemInserted(conversationsList.size());
        }
    }

    /**
     * Moves the conversation to the top and updates the adapter if a new message is
     * added.
     *
     * @param conversation the conversation to be moved.
     */
    public void addMessage(Conversation conversation) {
        if (conversationsList.contains(conversation)) {
            int oldIndex = conversationsList.indexOf(conversation);
            conversationsList.remove(conversation);
            conversationsList.add(0, conversation);
            notifyItemMoved(oldIndex, 0);
        } else {
            conversationsList.add(0, conversation);
            notifyItemInserted(0);
        }
        notifyItemChanged(0);
    }

    /**
     * Adds a conversation at a specific position in the list.
     *
     * @param position     the position at which to add the conversation.
     * @param conversation the conversation to be added.
     */
    public void add(int position, Conversation conversation) {
        if (!conversationsList.contains(conversation)) {
            conversationsList.add(position, conversation);
        }
    }

    /**
     * Removes a conversation from a specific position and notifies the adapter.
     *
     * @param position the position of the conversation to be removed.
     */
    public void remove(int position) {
        conversationsList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Removes a specific conversation and notifies the adapter.
     *
     * @param conversation the conversation to be removed.
     */
    public void remove(Conversation conversation) {
        int position = conversationsList.indexOf(conversation);
        conversationsList.remove(conversation);
        notifyItemRemoved(position);
    }

    /**
     * Updates the typing indicators for conversations and notifies the adapter.
     *
     * @param hashMap the map of conversations and typing indicators.
     */
    public void typing(HashMap<Conversation, TypingIndicator> hashMap) {
        typingIndicatorHashMap = new HashMap<>(hashMap);
        for (Map.Entry<Conversation, TypingIndicator> entry : hashMap.entrySet()) {
            Conversation conversation = entry.getKey();
            int index = conversationsList.indexOf(conversation);
            if (index != -1) {
                notifyItemChanged(index);
            }
        }
    }

    /**
     * Sets the item view for the conversation list and refreshes the adapter.
     *
     * @param listItemView the listener to handle the conversation item view.
     */
    public void setItemView(ConversationsViewHolderListener listItemView) {
        if (listItemView != null) {
            this.itemViewHolder = listItemView;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the subtitle view for the conversation list and refreshes the adapter.
     *
     * @param subtitleViewHolder the listener to handle the subtitle view.
     */
    public void setSubtitleView(@Nullable ConversationsViewHolderListener subtitleViewHolder) {
        if (subtitleViewHolder != null) {
            this.subtitleViewHolder = subtitleViewHolder;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the tail view for the conversation list and refreshes the adapter.
     *
     * @param tailViewHolder the listener to handle the tail view.
     */
    public void setTrailingView(ConversationsViewHolderListener tailViewHolder) {
        if (tailViewHolder != null) {
            this.trailingViewHolder = tailViewHolder;
            notifyDataSetChanged();
        }
    }

    /**
     * Clears the conversation list and refreshes the adapter.
     */
    public void clear() {
        conversationsList.clear();
        notifyDataSetChanged();
    }

    /**
     * Enables or disables the presence of users in the conversation list and
     * refreshes the adapter.
     *
     * @param hideUserStatus true to disable users' presence, false otherwise.
     */
    public void hideUserStatus(boolean hideUserStatus) {
        this.hideUserStatus = hideUserStatus;
        notifyDataSetChanged();
    }

    /**
     * Enables or disables the group type icon in the conversation list and refreshes
     * the adapter.
     *
     * @param hide true to disable the group type icon, false otherwise.
     */
    public void hideGroupType(boolean hide) {
        this.hideGroupType = hide;
        notifyDataSetChanged();
    }

    /**
     * Enables or disables selection and refreshes the view.
     *
     * @param selectionEnabled true to enable selection, false to disable
     */
    public void setSelectionEnabled(boolean selectionEnabled) {
        isSelectionEnabled = selectionEnabled;
        notifyDataSetChanged();
    }

    /**
     * Disables or hides the read receipt in the conversation list and refreshes the
     * adapter.
     *
     * @param hideReceipt true to hide the read receipt, false otherwise.
     */
    public void hideReceipts(boolean hideReceipt) {
        this.hideReceipts = hideReceipt;
        notifyDataSetChanged();
    }

    /**
     * Selects conversations from a hash map and refreshes the adapter.
     *
     * @param hashMap the hash map containing selected conversations.
     */
    public void selectConversation(HashMap<Conversation, Boolean> hashMap) {
        this.selectedConversation = hashMap;
        notifyDataSetChanged();
    }

    /**
     * Retrieves the list of conversations.
     *
     * @return the current list of conversations.
     */
    public List<Conversation> getConversationsList() {
        return conversationsList;
    }

    /**
     * Retrieves the context of the adapter.
     *
     * @return the context associated with the adapter.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Retrieves the typing indicators hash map.
     *
     * @return the hash map containing typing indicators.
     */
    public HashMap<Conversation, TypingIndicator> getTypingIndicatorHashMap() {
        return typingIndicatorHashMap;
    }

    /**
     * Checks if users' presence is disabled in the conversation list.
     *
     * @return true if users' presence is disabled, false otherwise.
     */
    public boolean disableUsersPresence() {
        return hideUserStatus;
    }

    /**
     * Checks if the read receipt is disabled in the conversation list.
     *
     * @return true if read receipt is disabled, false otherwise.
     */
    public boolean hideReceipts() {
        return hideReceipts;
    }


    /**
     * Retrieves the hash map of selected conversations.
     *
     * @return the hash map containing selected conversations.
     */
    public HashMap<Conversation, Boolean> getSelectedConversation() {
        return selectedConversation;
    }


    /**
     * Sets a custom date pattern for conversations and refreshes the adapter.
     *
     * @param dateFormat a function that returns a custom date pattern for a conversation.
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        if (dateFormat != null) {
            this.dateFormat = dateFormat;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the list of text formatters for the conversation view.
     *
     * @param textFormatters A list of {@link CometChatTextFormatter} to format text in
     *                       conversations.
     */
    public void setTextFormatters(List<CometChatTextFormatter> textFormatters) {
        if (textFormatters != null) {
            this.formatters = textFormatters;
            notifyDataSetChanged();
        }
    }

    public void setItemSelectedBackgroundColor(@ColorInt int itemSelectedBackgroundColor) {
        this.itemSelectedBackgroundColor = itemSelectedBackgroundColor;
    }

    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
    }

    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
    }

    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
    }

    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
    }

    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
    }

    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
    }

    public void setCheckBoxSelectIcon(Drawable checkBoxSelectIcon) {
        this.checkBoxSelectIcon = checkBoxSelectIcon;
    }

    public void setCheckBoxSelectIconTint(@ColorInt int checkBoxSelectIconTint) {
        this.checkBoxSelectIconTint = checkBoxSelectIconTint;
    }

    /**
     * Gets the title text color of the empty state in the conversations view.
     *
     * @return The color of the empty state title text.
     */
    public @ColorInt int getConversationsEmptyStateTitleTextColor() {
        return conversationsEmptyStateTitleTextColor;
    }

    /**
     * Sets the title text color of the empty state in the conversations view.
     *
     * @param conversationsEmptyStateTitleTextColor The color to set for the empty state title text.
     */
    public void setConversationsEmptyStateTitleTextColor(@ColorInt int conversationsEmptyStateTitleTextColor) {
        this.conversationsEmptyStateTitleTextColor = conversationsEmptyStateTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the subtitle text color of the empty state in the conversations view.
     *
     * @return The color of the empty state subtitle text.
     */
    public @ColorInt int getConversationsEmptyStateSubtitleTextColor() {
        return conversationsEmptyStateSubtitleTextColor;
    }

    /**
     * Sets the subtitle text color of the empty state in the conversations view.
     *
     * @param conversationsEmptyStateSubtitleTextColor The color to set for the empty state subtitle text.
     */
    public void setConversationsEmptyStateSubtitleTextColor(@ColorInt int conversationsEmptyStateSubtitleTextColor) {
        this.conversationsEmptyStateSubtitleTextColor = conversationsEmptyStateSubtitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the title text color of the error state in the conversations view.
     *
     * @return The color of the error state title text.
     */
    public @ColorInt int getConversationsErrorStateTitleTextColor() {
        return conversationsErrorStateTitleTextColor;
    }

    /**
     * Sets the title text color of the error state in the conversations view.
     *
     * @param conversationsErrorStateTitleTextColor The color to set for the error state title text.
     */
    public void setConversationsErrorStateTitleTextColor(@ColorInt int conversationsErrorStateTitleTextColor) {
        this.conversationsErrorStateTitleTextColor = conversationsErrorStateTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the subtitle text color of the error state in the conversations view.
     *
     * @return The color of the error state subtitle text.
     */
    public @ColorInt int getConversationsErrorStateSubtitleTextColor() {
        return conversationsErrorStateSubtitleTextColor;
    }

    /**
     * Sets the subtitle text color of the error state in the conversations view.
     *
     * @param conversationsErrorStateSubtitleTextColor The color to set for the error state subtitle text.
     */
    public void setConversationsErrorStateSubtitleTextColor(@ColorInt int conversationsErrorStateSubtitleTextColor) {
        this.conversationsErrorStateSubtitleTextColor = conversationsErrorStateSubtitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the title text color of a conversation item.
     *
     * @return The color of the conversation item title text.
     */
    public @ColorInt int getConversationsItemTitleTextColor() {
        return conversationsItemTitleTextColor;
    }

    /**
     * Sets the title text color of a conversation item.
     *
     * @param conversationsItemTitleTextColor The color to set for the conversation item title text.
     */
    public void setConversationsItemTitleTextColor(@ColorInt int conversationsItemTitleTextColor) {
        this.conversationsItemTitleTextColor = conversationsItemTitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the subtitle text color of a conversation item.
     *
     * @return The color of the conversation item subtitle text.
     */
    public @ColorInt int getConversationsItemSubtitleTextColor() {
        return conversationsItemSubtitleTextColor;
    }

    /**
     * Sets the subtitle text color of a conversation item.
     *
     * @param conversationsItemSubtitleTextColor The color to set for the conversation item subtitle text.
     */
    public void setConversationsItemSubtitleTextColor(@ColorInt int conversationsItemSubtitleTextColor) {
        this.conversationsItemSubtitleTextColor = conversationsItemSubtitleTextColor;
        notifyDataSetChanged();
    }

    /**
     * Gets the tint color for the message type icon in a conversation item.
     *
     * @return The tint color of the message type icon.
     */
    public @ColorInt int getConversationsItemMessageTypeIconTint() {
        return conversationsItemMessageTypeIconTint;
    }

    /**
     * Sets the tint color for the message type icon in a conversation item.
     *
     * @param conversationsItemMessageTypeIconTint The tint color to set for the message type icon.
     */
    public void setConversationsItemMessageTypeIconTint(@ColorInt int conversationsItemMessageTypeIconTint) {
        this.conversationsItemMessageTypeIconTint = conversationsItemMessageTypeIconTint;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for the empty state title.
     *
     * @return The style resource for the empty state title text appearance.
     */
    public @StyleRes int getConversationsEmptyStateTextTitleAppearance() {
        return conversationsEmptyStateTextTitleAppearance;
    }

    /**
     * Sets the text appearance style resource for the empty state title.
     *
     * @param conversationsEmptyStateTextTitleAppearance The style resource to set for the empty state title text
     *                                                   appearance.
     */
    public void setConversationsEmptyStateTextTitleAppearance(@StyleRes int conversationsEmptyStateTextTitleAppearance) {
        this.conversationsEmptyStateTextTitleAppearance = conversationsEmptyStateTextTitleAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for the empty state subtitle.
     *
     * @return The style resource for the empty state subtitle text appearance.
     */
    public @StyleRes int getConversationsEmptyStateTextSubtitleAppearance() {
        return conversationsEmptyStateTextSubtitleAppearance;
    }

    /**
     * Sets the text appearance style resource for the empty state subtitle.
     *
     * @param conversationsEmptyStateTextSubtitleAppearance The style resource to set for the empty state subtitle text
     *                                                      appearance.
     */
    public void setConversationsEmptyStateTextSubtitleAppearance(@StyleRes int conversationsEmptyStateTextSubtitleAppearance) {
        this.conversationsEmptyStateTextSubtitleAppearance = conversationsEmptyStateTextSubtitleAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for the error state title.
     *
     * @return The style resource for the error state title text appearance.
     */
    public @StyleRes int getConversationsErrorStateTextTitleAppearance() {
        return conversationsErrorStateTextTitleAppearance;
    }

    /**
     * Sets the text appearance style resource for the error state title.
     *
     * @param conversationsErrorStateTextTitleAppearance The style resource to set for the error state title text
     *                                                   appearance.
     */
    public void setConversationsErrorStateTextTitleAppearance(@StyleRes int conversationsErrorStateTextTitleAppearance) {
        this.conversationsErrorStateTextTitleAppearance = conversationsErrorStateTextTitleAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for the error state subtitle.
     *
     * @return The style resource for the error state subtitle text appearance.
     */
    public @StyleRes int getConversationsErrorStateTextSubtitleAppearance() {
        return conversationsErrorStateTextSubtitleAppearance;
    }

    /**
     * Sets the text appearance style resource for the error state subtitle.
     *
     * @param conversationsErrorStateTextSubtitleAppearance The style resource to set for the error state subtitle text
     *                                                      appearance.
     */
    public void setConversationsErrorStateTextSubtitleAppearance(@StyleRes int conversationsErrorStateTextSubtitleAppearance) {
        this.conversationsErrorStateTextSubtitleAppearance = conversationsErrorStateTextSubtitleAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for a conversation item's title.
     *
     * @return The style resource for the conversation item title text appearance.
     */
    public @StyleRes int getConversationsItemTitleTextAppearance() {
        return conversationsItemTitleTextAppearance;
    }

    /**
     * Sets the text appearance style resource for a conversation item's title.
     *
     * @param conversationsItemTitleTextAppearance The style resource to set for the conversation item title text
     *                                             appearance.
     */
    public void setConversationsItemTitleTextAppearance(@StyleRes int conversationsItemTitleTextAppearance) {
        this.conversationsItemTitleTextAppearance = conversationsItemTitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the text appearance style resource for a conversation item's subtitle.
     *
     * @return The style resource for the conversation item subtitle text
     * appearance.
     */
    public @StyleRes int getConversationsItemSubtitleTextAppearance() {
        return conversationsItemSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance style resource for a conversation item's subtitle.
     *
     * @param conversationsItemSubtitleTextAppearance The style resource to set for the conversation item subtitle text
     *                                                appearance.
     */
    public void setConversationsItemSubtitleTextAppearance(@StyleRes int conversationsItemSubtitleTextAppearance) {
        this.conversationsItemSubtitleTextAppearance = conversationsItemSubtitleTextAppearance;
        notifyDataSetChanged();
    }

    /**
     * Gets the avatar style resource for a conversation item.
     *
     * @return The style resource for the avatar in a conversation item.
     */
    public @StyleRes int getConversationsAvatarStyle() {
        return conversationsAvatarStyle;
    }

    /**
     * Sets the avatar style resource for a conversation item.
     *
     * @param conversationsAvatarStyle The style resource to set for the avatar in a conversation item.
     */
    public void setConversationsAvatarStyle(@StyleRes int conversationsAvatarStyle) {
        this.conversationsAvatarStyle = conversationsAvatarStyle;
        notifyDataSetChanged();
    }

    /**
     * Gets the status indicator style resource for a conversation item.
     *
     * @return The style resource for the status indicator in a conversation item.
     */
    public @StyleRes int getConversationsStatusIndicatorStyle() {
        return conversationsStatusIndicatorStyle;
    }

    /**
     * Sets the status indicator style resource for a conversation item.
     *
     * @param conversationsStatusIndicatorStyle The style resource to set for the status indicator in a
     *                                          conversation item.
     */
    public void setConversationsStatusIndicatorStyle(@StyleRes int conversationsStatusIndicatorStyle) {
        this.conversationsStatusIndicatorStyle = conversationsStatusIndicatorStyle;
        notifyDataSetChanged();
    }

    /**
     * Gets the date style resource for a conversation item.
     *
     * @return The style resource for the date in a conversation item.
     */
    public @StyleRes int getConversationsDateStyle() {
        return conversationsDateStyle;
    }

    /**
     * Sets the date style resource for a conversation item.
     *
     * @param conversationsDateStyle The style resource to set for the date in a conversation item.
     */
    public void setConversationsDateStyle(@StyleRes int conversationsDateStyle) {
        this.conversationsDateStyle = conversationsDateStyle;
        notifyDataSetChanged();
    }

    /**
     * Gets the badge style resource for a conversation item.
     *
     * @return The style resource for the badge in a conversation item.
     */
    public @StyleRes int getConversationsBadgeStyle() {
        return conversationsBadgeStyle;
    }

    /**
     * Sets the badge style resource for a conversation item.
     *
     * @param conversationsBadgeStyle The style resource to set for the badge in a conversation item.
     */
    public void setConversationsBadgeStyle(@StyleRes int conversationsBadgeStyle) {
        this.conversationsBadgeStyle = conversationsBadgeStyle;
        notifyDataSetChanged();
    }

    /**
     * Gets the receipt style resource for a conversation item.
     *
     * @return The style resource for the receipt in a conversation item.
     */
    public @StyleRes int getConversationsReceiptStyle() {
        return conversationsReceiptStyle;
    }

    /**
     * Sets the receipt style resource for a conversation item.
     *
     * @param conversationsReceiptStyle The style resource to set for the receipt in a conversation item.
     */
    public void setConversationsReceiptStyle(@StyleRes int conversationsReceiptStyle) {
        this.conversationsReceiptStyle = conversationsReceiptStyle;
        notifyDataSetChanged();
    }

    /**
     * Gets the typing indicator style resource for a conversation item.
     *
     * @return The style resource for the typing indicator in a conversation item.
     */
    public @StyleRes int getConversationsTypingIndicatorStyle() {
        return conversationsTypingIndicatorStyle;
    }

    /**
     * Sets the typing indicator style resource for a conversation item.
     *
     * @param conversationsTypingIndicatorStyle The style resource to set for the typing indicator in a
     *                                          conversation item.
     */
    public void setConversationsTypingIndicatorStyle(@StyleRes int conversationsTypingIndicatorStyle) {
        this.conversationsTypingIndicatorStyle = conversationsTypingIndicatorStyle;
        notifyDataSetChanged();
    }

    public void setTitleView(ConversationsViewHolderListener titleView) {
        this.titleView = titleView;
        notifyDataSetChanged();
    }

    public void setLeadingView(ConversationsViewHolderListener leadingView) {
        this.leadingView = leadingView;
        notifyDataSetChanged();
    }

    public void setDateTimeFormatter(DateTimeFormatterCallback dateTimeFormatter) {
        if (dateTimeFormatter != null) {
            this.dateTimeFormatter = dateTimeFormatter;
            notifyDataSetChanged();
        }
    }

    /**
     * ViewHolder class for binding and displaying conversation items in a
     * RecyclerView.
     */
    public class ConversationsViewHolder extends RecyclerView.ViewHolder {

        // Binding for conversation list items layout.
        private final CometchatConversationsListItemsBinding binding;

        // Custom or default views for subtitle and tail.
        private SubtitleView subtitleView;
        private ConversationTailView tailView;
        private View customView, customLeadingView, customTitleView, customSubtitleView, customTailView;

        /**
         * Constructor for ConversationsViewHolder. Initializes the custom views or
         * default ones for subtitle and tail sections based on view holders provided.
         *
         * @param itemView The root view of the conversation item layout.
         */
        public ConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatConversationsListItemsBinding.bind(itemView);

            // If a custom view holder is provided, use the custom view.
            if (itemViewHolder != null) {
                customView = itemViewHolder.createView(context, binding);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            } else {
                // Handle subtitle view
                if (leadingView != null) {
                    customLeadingView = leadingView.createView(context, binding);
                    Utils.handleView(binding.conversationLeadingView, customLeadingView, true);
                }
                if (titleView != null) {
                    customTitleView = titleView.createView(context, binding);
                    Utils.handleView(binding.conversationsTitleView, customTitleView, true);
                }
                if (subtitleViewHolder != null) {
                    customSubtitleView = subtitleViewHolder.createView(context, binding);
                    Utils.handleView(binding.subtitleView, customSubtitleView, true);
                } else {
                    subtitleView = ConversationsUtils.getSubtitleViewContainer(context);
                    Utils.handleView(binding.subtitleView, subtitleView, true);
                }

                // Handle tail view
                if (trailingViewHolder != null) {
                    customTailView = trailingViewHolder.createView(context, binding);
                    Utils.handleView(binding.tailView, customTailView, true);
                } else {
                    tailView = ConversationsUtils.getConversationTailViewContainer(context);
                    Utils.handleView(binding.tailView, tailView, true);
                }
            }
        }

        /**
         * Binds a conversation object to the view holder, setting up the avatar,
         * status, name, subtitle, and tail views. Handles custom views if available.
         *
         * @param conversation The conversation object to display.
         * @param position     The position of the item in the list.
         */
        public void bindView(Conversation conversation, int position) {
            // Set avatar style
            binding.conversationsAvatar.setStyle(conversationsAvatarStyle);

            // Set status indicator style
            binding.conversationsStatusAndTypeIndicator.setStyle(conversationsStatusIndicatorStyle);

            // If a custom view holder is provided, bind the custom view.
            if (itemViewHolder != null) {
                itemViewHolder.bindView(context, customView, conversation, this, conversationsList, position);
            } else {
                // Set conversation avatar and title
                String name = ConversationsUtils.getConversationTitle(conversation);
                if (titleView != null) {
                    titleView.bindView(context, customTitleView, conversation, this, conversationsList, position);
                } else {

                    binding.tvConversationsTitle.setText(name);
                    binding.tvConversationsTitle.setTextAppearance(conversationsItemTitleTextAppearance);

                    if (conversationsItemTitleTextColor != 0) {
                        binding.tvConversationsTitle.setTextColor(conversationsItemTitleTextColor);
                    }
                }

                if (leadingView != null) {
                    leadingView.bindView(context, customLeadingView, conversation, this, conversationsList, position);
                } else {
                    binding.conversationsAvatar.setAvatar(name, ConversationsUtils.getConversationAvatar(conversation));
                    // Handle user presence or group type icon based on conversation type
                    if (UIKitConstants.ConversationType.USERS.equalsIgnoreCase(conversation.getConversationType())) {
                        handleUserPresence(conversation);
                    } else if (UIKitConstants.ConversationType.GROUPS.equalsIgnoreCase(conversation.getConversationType())) {
                        handleGroupType(conversation);
                    }
                }

                if (isSelectionEnabled) {
                    Utils.initMaterialCard(binding.checkboxView);
                    binding.ivCheckbox.setImageDrawable(checkBoxSelectIcon);
                    binding.ivCheckbox.setImageTintList(ColorStateList.valueOf(checkBoxSelectIconTint));
                    binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                    binding.checkboxView.setStrokeColor(ColorStateList.valueOf(checkBoxStrokeColor));
                    binding.checkboxView.setRadius(checkBoxCornerRadius);

                    if (!selectedConversation.isEmpty() && selectedConversation.containsKey(conversation)) {
                        binding.ivCheckbox.setVisibility(View.VISIBLE);
                        binding.checkboxView.setStrokeWidth(0);
                        binding.parentLayout.setBackgroundColor(itemSelectedBackgroundColor);
                        binding.checkboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor);
                    } else {
                        binding.ivCheckbox.setVisibility(View.GONE);
                        binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                        binding.parentLayout.setBackgroundColor(itemBackgroundColor);
                        binding.checkboxView.setCardBackgroundColor(checkBoxBackgroundColor);
                    }
                    binding.checkboxView.setVisibility(View.VISIBLE);
                } else {
                    binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                    binding.parentLayout.setBackgroundColor(itemBackgroundColor);
                    binding.checkboxView.setVisibility(View.GONE);
                }

                // Bind subtitle view
                if (subtitleViewHolder != null) {
                    subtitleViewHolder.bindView(context, customSubtitleView, conversation, this, conversationsList, position);
                } else {
                    ConversationsUtils.bindSubtitleView(
                        context,
                        subtitleView,
                        conversation,
                        typingIndicatorHashMap,
                        hideReceipts,
                        formatters,
                        conversationsItemSubtitleTextAppearance,
                        conversationsItemSubtitleTextColor,
                        conversationsItemMessageTypeIconTint,
                        conversationsReceiptStyle,
                        conversationsTypingIndicatorStyle
                    );
                }

                // Bind tail view
                if (trailingViewHolder != null) {
                    trailingViewHolder.bindView(context, customTailView, conversation, this, conversationsList, position);
                } else {
                    ConversationsUtils.bindConversationTailView(
                        tailView,
                        dateFormat,
                        dateTimeFormatter,
                        conversation,
                        conversationsBadgeStyle,
                        conversationsDateStyle
                    );
                }
            }

            // Set tag for the conversation
            itemView.setTag(R.string.cometchat_conversation, conversation);
        }

        /**
         * Handles the display of the user presence indicator for one-on-one
         * conversations.
         *
         * @param conversation The conversation object containing the user information.
         */
        private void handleUserPresence(Conversation conversation) {
            if (((User) conversation.getConversationWith()).getStatus().equalsIgnoreCase(CometChatConstants.USER_STATUS_ONLINE)) {
                if (!Utils.isBlocked(((User) conversation.getConversationWith()))) {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(hideUserStatus ? StatusIndicator.OFFLINE : StatusIndicator.ONLINE);
                } else {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
                }
            } else {
                binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
            }
        }

        /**
         * Handles the display of group type icons (public, private, protected) for
         * group conversations.
         *
         * @param conversation The conversation object containing the group information.
         */
        private void handleGroupType(Conversation conversation) {
            if (!hideGroupType) {
                if (((Group) conversation.getConversationWith()).getGroupType().equals(CometChatConstants.GROUP_TYPE_PASSWORD)) {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PROTECTED_GROUP);
                } else if (((Group) conversation.getConversationWith()).getGroupType().equals(CometChatConstants.GROUP_TYPE_PRIVATE)) {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PRIVATE_GROUP);
                } else {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PUBLIC_GROUP);
                }
            }
        }

        /**
         * Adjusts the layout and icon for selected conversations.
         *
         * @param conversation The conversation object to check for selection.
         */
        private void handleSelectionMode(Conversation conversation) {
            if (!selectedConversation.isEmpty() && selectedConversation.containsKey(conversation)) {
                ConstraintLayout.LayoutParams selectedConversationLayoutParams = new ConstraintLayout.LayoutParams(
                    context
                        .getResources()
                        .getDimensionPixelSize(R.dimen.cometchat_20dp),
                    context
                        .getResources()
                        .getDimensionPixelSize(R.dimen.cometchat_20dp)
                );
                selectedConversationLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                selectedConversationLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                binding.conversationsStatusAndTypeIndicator.setLayoutParams(selectedConversationLayoutParams);
                binding.conversationsStatusAndTypeIndicator.setStatusIndicatorBackgroundImage(ResourcesCompat.getDrawable(
                    context.getResources(),
                    R.drawable.cometchat_ic_circle_check,
                    null
                ));
            }
        }
    }
}
