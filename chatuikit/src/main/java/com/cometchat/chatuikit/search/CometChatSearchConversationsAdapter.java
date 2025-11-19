package com.cometchat.chatuikit.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.databinding.CometchatSearchConversationItemBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.utils.ConversationTailView;
import com.cometchat.chatuikit.shared.utils.ConversationsUtils;
import com.cometchat.chatuikit.shared.utils.SubtitleView;
import com.cometchat.chatuikit.shared.viewholders.ConversationsSearchViewHolderListener;
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.cometchat.chatuikit.shared.views.date.Pattern;
import com.cometchat.chatuikit.shared.views.statusindicator.CometChatStatusIndicator;
import com.cometchat.chatuikit.shared.views.statusindicator.StatusIndicator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CometChatSearchConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = CometChatSearchConversationsAdapter.class.getSimpleName();
    private final Context context;
    private List<Conversation> conversationList;
    private ConversationsSearchViewHolderListener titleViewHolder, leadingViewHolder, subtitleViewHolder, trailingViewHolder, itemViewHolder;
    private boolean hideUserStatus = false;
    private boolean groupTypeVisibility = false;

    private @ColorInt int conversationItemBackgroundColor;
    private @ColorInt int conversationTitleTextColor;
    private @StyleRes int conversationTitleTextAppearance;
    private @ColorInt int conversationSubtitleTextColor;
    private @StyleRes int conversationSubtitleTextAppearance;
    private @ColorInt int conversationTimestampTextColor;
    private @StyleRes int conversationTimestampTextAppearance;

    private @StyleRes int conversationsAvatarStyle;
    private @StyleRes int conversationsDateStyle;
    private @StyleRes int conversationsBadgeStyle;

    private SimpleDateFormat dateFormat;
    private List<CometChatTextFormatter> formatters;
    private DateTimeFormatterCallback dateTimeFormat;

    public CometChatSearchConversationsAdapter(Context context) {
        this.context = context;
        conversationList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatSearchConversationItemBinding binding = CometchatSearchConversationItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CometChatSearchConversationsViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CometChatSearchConversationsViewHolder) holder).bindView(conversationList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void setList(List<Conversation> conversations) {
        this.conversationList = conversations;
        notifyDataSetChanged();
    }

    public void setConversationItemView(ConversationsSearchViewHolderListener viewHolderListener) {
        this.itemViewHolder = viewHolderListener;
        notifyDataSetChanged();
    }

    public void setConversationItemBackgroundColor(@ColorInt int conversationItemBackgroundColor) {
        this.conversationItemBackgroundColor = conversationItemBackgroundColor;
    }

    public void setConversationTitleTextColor(@ColorInt int conversationTitleTextColor) {
        this.conversationTitleTextColor = conversationTitleTextColor;
    }

    public void setConversationTitleTextAppearance(@StyleRes int conversationTitleTextAppearance) {
        this.conversationTitleTextAppearance = conversationTitleTextAppearance;
    }

    public void setConversationSubtitleTextColor(@ColorInt int conversationSubtitleTextColor) {
        this.conversationSubtitleTextColor = conversationSubtitleTextColor;
    }

    public void setConversationSubtitleTextAppearance(@StyleRes int conversationSubtitleTextAppearance) {
        this.conversationSubtitleTextAppearance = conversationSubtitleTextAppearance;
    }

    public void setConversationTimestampTextColor(@ColorInt int conversationTimestampTextColor) {
        this.conversationTimestampTextColor = conversationTimestampTextColor;
    }

    public void setConversationTimestampTextAppearance(@StyleRes int conversationTimestampTextAppearance) {
        this.conversationTimestampTextAppearance = conversationTimestampTextAppearance;
    }

    public Conversation getItemByPosition(int position) {
        if (position >= 0 && position < conversationList.size()) {
            return conversationList.get(position);
        }
        return null;
    }

    public void setHideUserStatus(boolean bool) {
        this.hideUserStatus = bool;
    }

    public void setConversationTimestampDateFormat(SimpleDateFormat conversationDateFormat) {
        this.dateFormat = conversationDateFormat;
    }

    public void setBadgeStyle(int badgeStyle) {
        this.conversationsBadgeStyle = badgeStyle;
    }

    public void setAvatarStyle(int avatarStyle) {
        this.conversationsAvatarStyle = avatarStyle;
    }

    public void setHideGroupType(boolean hide) {
        this.groupTypeVisibility = hide;
    }

    public void setTextFormatters(List<CometChatTextFormatter> textFormatters) {
        this.formatters = textFormatters;
    }

    public void setDateTimeFormatter(DateTimeFormatterCallback dateTimeFormatter) {
        this.dateTimeFormat = dateTimeFormatter;
    }


    private class CometChatSearchConversationsViewHolder extends RecyclerView.ViewHolder {
        private final CometchatSearchConversationItemBinding binding;
        private SubtitleView subtitleView;
        private ConversationTailView tailView;
        private View customView, customLeadingView, customTitleView, customSubtitleView, customTailView;

        public CometChatSearchConversationsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatSearchConversationItemBinding.bind(itemView);

            if (itemViewHolder != null) {
                customView = itemViewHolder.createView(context, binding);
                binding.parentLayout.removeAllViews();
                binding.parentLayout.addView(customView);
            } else {
                if (leadingViewHolder != null) {
                    customLeadingView = leadingViewHolder.createView(context, binding);
                    Utils.handleView(binding.conversationLeadingView, customLeadingView, true);
                }

                if (titleViewHolder != null) {
                    customTitleView = titleViewHolder.createView(context, binding);
                    Utils.handleView(binding.conversationsTitleView, customTitleView, true);
                }

                if (subtitleViewHolder != null) {
                    customSubtitleView = subtitleViewHolder.createView(context, binding);
                    Utils.handleView(binding.subtitleView, customSubtitleView, true);
                } else {
                    subtitleView = ConversationsUtils.getSubtitleViewContainer(context);
                    Utils.handleView(binding.subtitleView, subtitleView, true);
                }

                if (trailingViewHolder != null) {
                    customTailView = trailingViewHolder.createView(context, binding);
                    Utils.handleView(binding.tailView, customTailView, true);
                } else {
                    tailView = ConversationsUtils.getConversationTailViewContainer(context);
                    Utils.handleView(binding.tailView, tailView, true);
                }
            }
        }

        public void bindView(Conversation conversation, int position) {
            if (itemViewHolder != null) {
                itemViewHolder.bindView(context, customView, conversation, this, conversationList, position);
            } else {
                binding.parentLayout.setBackgroundColor(conversationItemBackgroundColor);
                String name = ConversationsUtils.getConversationTitle(conversation);
                if (titleViewHolder != null) {
                    titleViewHolder.bindView(context, customTitleView, conversation, this, conversationList, position);
                } else {
                    binding.tvConversationsTitle.setText(name);
                    binding.tvConversationsTitle.setTextAppearance(conversationTitleTextAppearance);
                    if (conversationTitleTextColor != 0) {
                        binding.tvConversationsTitle.setTextColor(conversationTitleTextColor);
                    }
                }

                if (leadingViewHolder != null) {
                    leadingViewHolder.bindView(context, customLeadingView, conversation, this, conversationList, position);
                } else {
                    bindLeadingView(binding.conversationsAvatar, binding.conversationsStatusAndTypeIndicator, name, conversation);
                }

                if (subtitleViewHolder != null) {
                    subtitleViewHolder.bindView(context, customSubtitleView, conversation, this, conversationList, position);
                } else {
                    ConversationsUtils.bindSubtitleView(
                            context,
                            subtitleView,
                            conversation,
                            new HashMap<>(),
                            false,
                            formatters,
                            conversationSubtitleTextAppearance,
                            conversationSubtitleTextColor,
                            0,
                            0,
                            0
                    );
                }

                if (trailingViewHolder != null) {
                    trailingViewHolder.bindView(context, customTailView, conversation, this, conversationList, position);
                } else {
                    ConversationsUtils.bindConversationTailView(
                            tailView,
                            dateFormat,
                            dateTimeFormat,
                            conversation,
                            conversationsBadgeStyle,
                            0
                    );
                    tailView.getDate().setDateTextColor(conversationTimestampTextColor);
                    tailView.getDate().setDateTextAppearance(conversationTimestampTextAppearance);
                }
            }
        }
    }


    private void bindTailView(CometChatDate date, BaseMessage baseMessage, int textColor, int textAppearance) {
        date.setDate(baseMessage.getUpdatedAt(), Pattern.DAY_DATE_TIME);
        date.setDateFormat(dateFormat);
        date.setDateTextColor(textColor);
        if (textAppearance != 0) date.setDateTextAppearance(textAppearance);
    }

    private void handleUserPresence(CometChatStatusIndicator conversationsStatusAndTypeIndicator, Conversation conversation) {
        if (((User) conversation.getConversationWith()).getStatus().equalsIgnoreCase(CometChatConstants.USER_STATUS_ONLINE)) {
            if (!Utils.isBlocked(((User) conversation.getConversationWith()))) {
                conversationsStatusAndTypeIndicator.setStatusIndicator(hideUserStatus ? StatusIndicator.OFFLINE : StatusIndicator.ONLINE);
            } else {
                conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
            }
        } else {
            conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
        }
    }

    private void handleGroupType(CometChatStatusIndicator conversationsStatusAndTypeIndicator, Conversation conversation) {
        if (!groupTypeVisibility) {
            if (((Group) conversation.getConversationWith()).getGroupType().equals(CometChatConstants.GROUP_TYPE_PASSWORD)) {
                conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PROTECTED_GROUP);
            } else if (((Group) conversation.getConversationWith()).getGroupType().equals(CometChatConstants.GROUP_TYPE_PRIVATE)) {
                conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PRIVATE_GROUP);
            } else {
                conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PUBLIC_GROUP);
            }
        }
    }

    private void bindLeadingView(CometChatAvatar conversationsAvatar, CometChatStatusIndicator conversationsStatusAndTypeIndicator, String name, Conversation conversation) {
        conversationsAvatar.setAvatar(name, ConversationsUtils.getConversationAvatar(conversation));
        conversationsAvatar.setStyle(conversationsAvatarStyle);
        if (UIKitConstants.ConversationType.USERS.equalsIgnoreCase(conversation.getConversationType())) {
            handleUserPresence(conversationsStatusAndTypeIndicator, conversation);
        } else if (UIKitConstants.ConversationType.GROUPS.equalsIgnoreCase(conversation.getConversationType())) {
            handleGroupType(conversationsStatusAndTypeIndicator, conversation);
        }
    }
}
