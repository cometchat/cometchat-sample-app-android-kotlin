package com.cometchat.chatuikit.shared.formatters;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.GroupMembersRequest;
import com.cometchat.chat.core.UsersRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.style.PromptTextStyle;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.spans.NonEditableSpan;
import com.cometchat.chatuikit.shared.spans.OnTagClick;
import com.cometchat.chatuikit.shared.spans.TagSpan;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class CometChatMentionsFormatter extends CometChatTextFormatter {
    private static final String TAG = CometChatMentionsFormatter.class.getSimpleName();
    private final int requestLimit = 10;
    private final List<SuggestionItem> suggestionItemList;
    private final String defaultRegex = "<" + getTrackingCharacter() + "uid:(.*?)>";
    private String groupId;
    private UsersRequest.UsersRequestBuilder usersRequestBuilder;
    private GroupMembersRequest.GroupMembersRequestBuilder groupMembersRequestBuilder;
    private Function1<Group, GroupMembersRequest.GroupMembersRequestBuilder> groupMembersRequestBuilderCallback;
    private UIKitConstants.MentionsType mentionType = UIKitConstants.MentionsType.USERS_AND_GROUP_MEMBERS;
    private UIKitConstants.MentionsVisibility mentionsVisibility = UIKitConstants.MentionsVisibility.BOTH;
    private int mentionLimit = 10;
    private GroupMembersRequest groupMembersRequest;
    private UsersRequest usersRequest;
    private PromptTextStyle selfTagStyle, tagStyle, outgoingBubbleTagStyle, outgoingBubbleSelfTagStyle, incomingBubbleSelfTagStyle, incomingBubbleTagStyle, conversationSelfTagStyle, conversationTagStyle;
    private @StyleRes int messageComposerMentionTextStyle, incomingBubbleMentionTextStyle, outgoingBubbleMentionTextStyle, conversationsMentionTextStyle;
    private OnTagClick<User> onTagClick;
    private Pattern pattern;

    public CometChatMentionsFormatter(Context context) {
        super('@');
        this.suggestionItemList = new ArrayList<>();
        init(context, defaultRegex);
    }

    private void init(Context context, @NonNull String regexPattern) {
        this.pattern = Pattern.compile(regexPattern);
        setOutgoingBubbleMentionTextStyle(context, R.style.CometChatOutgoingBubbleMentionsStyle);
        setIncomingBubbleMentionTextStyle(context, R.style.CometChatIncomingBubbleMentionsStyle);
        setMessageComposerMentionTextStyle(context, R.style.CometChatMessageComposerMentionsStyle);
        setConversationsMentionTextStyle(context, R.style.CometChatConversationsMentionsStyle);
    }

    public void setOutgoingBubbleMentionTextStyle(Context context, @StyleRes int style) {
        if (style != 0) {
            this.outgoingBubbleMentionTextStyle = style;
            setIncomingBubbleTagStyle(getPromptTextStyle(context, style));
            setIncomingBubbleSelfTagStyle(getSelfPromptTextStyle(context, style));
        }
    }

    public void setIncomingBubbleMentionTextStyle(Context context, @StyleRes int style) {
        if (style != 0) {
            this.incomingBubbleMentionTextStyle = style;
            setOutgoingBubbleTagStyle(getPromptTextStyle(context, style));
            setOutgoingBubbleSelfTagStyle(getSelfPromptTextStyle(context, style));
        }
    }

    public void setMessageComposerMentionTextStyle(Context context, @StyleRes int style) {
        if (style != 0) {
            this.messageComposerMentionTextStyle = style;
            setTagStyle(getPromptTextStyle(context, style));
            setSelfTagStyle(getSelfPromptTextStyle(context, style));
        }
    }

    public void setConversationsMentionTextStyle(Context context, @StyleRes int style) {
        this.conversationsMentionTextStyle = style;
        setConversationTagStyle(getPromptTextStyle(context, style));
        setConversationSelfTagStyle(getSelfPromptTextStyle(context, style));
    }

    private void setIncomingBubbleTagStyle(PromptTextStyle incomingBubbleTagStyle) {
        this.incomingBubbleTagStyle = incomingBubbleTagStyle;
    }

    private PromptTextStyle getPromptTextStyle(Context context, @StyleRes int style) {
        PromptTextStyle promptTextStyle = new PromptTextStyle();
        if (context != null && style != 0) {
            TypedArray typedArray = context
                .getTheme()
                .obtainStyledAttributes(null, R.styleable.CometChatMentionStyle, R.attr.cometchatMentionsStyle, style);
            try {
                promptTextStyle.setTextAppearance(Utils.getTypefaceFromTextAppearance(context,
                                                                                      typedArray.getResourceId(R.styleable.CometChatMentionStyle_cometchatMentionTextAppearance,
                                                                                                               0)));
                promptTextStyle.setTextSize(Utils.getTextSize(context,
                                                              typedArray.getResourceId(R.styleable.CometChatMentionStyle_cometchatMentionTextAppearance,
                                                                                       0)));
                promptTextStyle.setColor(typedArray.getColor(R.styleable.CometChatMentionStyle_cometchatMentionTextColor, 0));
                promptTextStyle.setBackgroundColor(typedArray.getColor(R.styleable.CometChatMentionStyle_cometchatMentionBackgroundColor, 0));
            } finally {
                typedArray.recycle();
            }
        }
        return promptTextStyle;
    }

    private void setIncomingBubbleSelfTagStyle(PromptTextStyle incomingBubbleSelfTagStyle) {
        this.incomingBubbleSelfTagStyle = incomingBubbleSelfTagStyle;
    }

    private PromptTextStyle getSelfPromptTextStyle(Context context, @StyleRes int style) {
        PromptTextStyle promptTextStyle = new PromptTextStyle();
        if (context != null && style != 0) {
            TypedArray typedArray = context
                .getTheme()
                .obtainStyledAttributes(null, R.styleable.CometChatMentionStyle, R.attr.cometchatMentionsStyle, style);
            try {
                promptTextStyle.setTextAppearance(Utils.getTypefaceFromTextAppearance(context,
                                                                                      typedArray.getResourceId(R.styleable.CometChatMentionStyle_cometchatSelfMentionTextAppearance,
                                                                                                               0)));
                promptTextStyle.setColor(typedArray.getColor(R.styleable.CometChatMentionStyle_cometchatSelfMentionTextColor, 0));
                promptTextStyle.setTextSize(Utils.getTextSize(context,
                                                              typedArray.getResourceId(R.styleable.CometChatMentionStyle_cometchatSelfMentionTextAppearance,
                                                                                       0)));
                promptTextStyle.setBackgroundColor(typedArray.getColor(R.styleable.CometChatMentionStyle_cometchatSelfMentionBackgroundColor, 0));
            } finally {
                typedArray.recycle();
            }
        }
        return promptTextStyle;
    }

    private void setOutgoingBubbleTagStyle(PromptTextStyle outgoingBubbleTagStyle) {
        this.outgoingBubbleTagStyle = outgoingBubbleTagStyle;
    }

    private void setOutgoingBubbleSelfTagStyle(PromptTextStyle outgoingBubbleSelfTagStyle) {
        this.outgoingBubbleSelfTagStyle = outgoingBubbleSelfTagStyle;
    }

    private void setTagStyle(PromptTextStyle promptTextStyle) {
        this.tagStyle = promptTextStyle;
    }

    private void setSelfTagStyle(PromptTextStyle promptTextStyle) {
        this.selfTagStyle = promptTextStyle;
    }

    private void setConversationTagStyle(PromptTextStyle promptTextStyle) {
        this.conversationTagStyle = promptTextStyle;
    }

    private void setConversationSelfTagStyle(PromptTextStyle promptTextStyle) {
        this.conversationSelfTagStyle = promptTextStyle;
    }

    public CometChatMentionsFormatter(Context context, char trackingCharacter) {
        super(trackingCharacter);
        this.suggestionItemList = new ArrayList<>();
        init(context, defaultRegex);
    }

    public CometChatMentionsFormatter(Context context, char trackingCharacter, String regexPattern) {
        super(trackingCharacter);
        this.suggestionItemList = new ArrayList<>();
        init(context, regexPattern);
    }

    public void setMentionLimit(int limit) {
        mentionLimit = limit;
    }

    public void setMentionsVisibility(@Nonnull UIKitConstants.MentionsVisibility mentionsVisibility) {
        this.mentionsVisibility = mentionsVisibility;
    }

    public void setGroupMembersRequestBuilder(@Nonnull Function1<Group, GroupMembersRequest.GroupMembersRequestBuilder> groupMembersRequestBuilderCallBack) {
        this.groupMembersRequestBuilderCallback = groupMembersRequestBuilderCallBack;
        if (getGroup() != null)
            this.groupMembersRequestBuilder = groupMembersRequestBuilderCallBack.apply(getGroup());
        initializeGroupMemberRequestBuilder();
    }

    public void setUsersRequestBuilder(@Nonnull UsersRequest.UsersRequestBuilder usersRequestBuilder) {
        this.usersRequestBuilder = usersRequestBuilder;
    }

    public void setMentionsType(@Nonnull UIKitConstants.MentionsType mentionsType) {
        this.mentionType = mentionsType;
    }

    public void setOnMentionClick(OnTagClick<User> onTagClick) {
        this.onTagClick = onTagClick;
    }

    public @StyleRes int getMessageComposerMentionTextStyle() {
        return messageComposerMentionTextStyle;
    }

    public @StyleRes int getIncomingBubbleMentionTextStyle() {
        return incomingBubbleMentionTextStyle;
    }

    public @StyleRes int getOutgoingBubbleMentionTextStyle() {
        return outgoingBubbleMentionTextStyle;
    }

    public @StyleRes int getConversationsMentionTextStyle() {
        return conversationsMentionTextStyle;
    }

    @Override
    public void search(@Nonnull Context context, @Nullable String queryString) {
        setShowLoadingIndicator(suggestionItemList.isEmpty() && queryString != null && queryString.isEmpty());
        if (getSelectedList().size() < mentionLimit) {
            if (queryString != null) {
                searchMentions(queryString);
            } else {
                suggestionItemList.clear();
                setSuggestionItemList(suggestionItemList);
            }
        } else {
            suggestionItemList.clear();
            setSuggestionItemList(suggestionItemList);
        }
    }

    public void searchMentions(String queryString) {
        switch (mentionType) {
            case USERS:
                searchUser(queryString);
                break;
            case USERS_AND_GROUP_MEMBERS:
                if (getGroup() != null) searchGroupMember(queryString);
                else searchUser(queryString);
                break;
            default:
                break;
        }
    }

    public void searchUser(String queryString) {
        initializeUserRequestBuilder();
        suggestionItemList.clear();
        usersRequest = usersRequestBuilder.setSearchKeyword(queryString).build();
        fetchUsers();
    }

    public void searchGroupMember(String queryString) {
        initializeGroupMemberRequestBuilder();
        groupMembersRequest = groupMembersRequestBuilder.setSearchKeyword(queryString).build();
        suggestionItemList.clear();
        fetchGroupMembers();
    }

    private void initializeUserRequestBuilder() {
        if (usersRequestBuilder == null)
            usersRequestBuilder = new UsersRequest.UsersRequestBuilder().setLimit(requestLimit);
    }

    public void fetchUsers() {
        if (usersRequest == null) return;
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                List<SuggestionItem> suggestionItems = new ArrayList<>();
                for (User user : users) {
                    suggestionItems.add(new SuggestionItem(user.getUid(),
                                                           user.getName(),
                                                           user.getAvatar(),
                                                           user.getStatus(),
                                                           getTrackingCharacter() + user.getName(),
                                                           "<" + getTrackingCharacter() + "uid:" + user.getUid() + ">",
                                                           user.toJson(),
                                                           tagStyle));
                }
                suggestionItemList.addAll(suggestionItems);
                setSuggestionItemList(suggestionItemList);
            }

            @Override
            public void onError(CometChatException e) {
                suggestionItemList.clear();
                setSuggestionItemList(suggestionItemList);
            }
        });
    }

    public void fetchGroupMembers() {
        if (groupMembersRequest == null) return;
        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {

                List<SuggestionItem> suggestionItems = new ArrayList<>();
                for (GroupMember user : groupMembers) {
                    suggestionItems.add(new SuggestionItem(user.getUid(),
                                                           user.getName(),
                                                           user.getAvatar(),
                                                           user.getStatus(),
                                                           getTrackingCharacter() + user.getName(),
                                                           "<" + getTrackingCharacter() + "uid:" + user.getUid() + ">",
                                                           user.toJson(),
                                                           user
                                                               .getUid()
                                                               .equals(CometChatUIKit
                                                                           .getLoggedInUser()
                                                                           .getUid()) && selfTagStyle != null ? selfTagStyle : tagStyle));
                }

                suggestionItemList.addAll(suggestionItems);
                setSuggestionItemList(suggestionItemList);
            }

            @Override
            public void onError(CometChatException e) {
                suggestionItemList.clear();
                setSuggestionItemList(suggestionItemList);
            }
        });
    }

    /**
     * @param suggestionItem
     * @param user
     * @param group
     */
    @Override
    public void onItemClick(@Nonnull Context context, @NonNull SuggestionItem suggestionItem, @Nullable User user, @Nullable Group group) {
    }

    /**
     * @param baseMessage
     */
    @Override
    public void handlePreMessageSend(@Nonnull Context context, @NonNull BaseMessage baseMessage) {
        baseMessage.setMentionedUsers(getMentionUsers());
    }

    public List<User> getMentionUsers() {
        List<User> users = new ArrayList<>();
        for (SuggestionItem suggestionItem : getSelectedList()) {
            users.add(User.fromJson(suggestionItem.getData().toString()));
        }
        return users;
    }

    @Override
    public void onScrollToBottom() {
        if (groupMembersRequestBuilder != null) fetchGroupMembers();
        else if (usersRequestBuilder != null) fetchUsers();
    }

    @Nullable
    @Override
    public SpannableStringBuilder prepareLeftMessageBubbleSpan(@NonNull Context context,
                                                               @NonNull BaseMessage baseMessage,
                                                               SpannableStringBuilder spannable) {
        if (context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, spannable);
        }
        return getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.LEFT, spannable);
    }

    @Nullable
    @Override
    public SpannableStringBuilder prepareRightMessageBubbleSpan(@NonNull Context context,
                                                                @NonNull BaseMessage baseMessage,
                                                                SpannableStringBuilder spannable) {
        if (context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.LEFT, spannable);
        }
        return getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, spannable);
    }

    @Nullable
    @Override
    public SpannableStringBuilder prepareComposerSpan(@NonNull Context context, @NonNull BaseMessage baseMessage, SpannableStringBuilder spannable) {
        return getComposerSpan(baseMessage, spannable);
    }

    @Nullable
    @Override
    public SpannableStringBuilder prepareConversationSpan(@NonNull Context context,
                                                          @NonNull BaseMessage baseMessage,
                                                          SpannableStringBuilder spannable) {
        return getConversationSpan(baseMessage, spannable);
    }

    @Override
    public void observeSelectionList(@Nonnull Context context, @NonNull List<SuggestionItem> selectedSuggestionItemList) {
        setDisableSuggestions(getSelectedList().size() >= mentionLimit);
        setInfoVisibility(getSelectedList().size() >= mentionLimit);
        setInfoText(getSelectedList().size() >= mentionLimit ? context
            .getResources()
            .getString(R.string.cometchat_you_can_mention_up_to) + " " + mentionLimit + " " + context
            .getResources()
            .getString(R.string.cometchat_time_at_a_time) : "");
    }

    @Override
    public void setGroup(Group group) {
        if (group != null) {
            super.setGroup(group);
            groupId = group.getGuid();
            if (groupMembersRequestBuilderCallback != null)
                this.groupMembersRequestBuilder = groupMembersRequestBuilderCallback.apply(group);
        } else {
            super.setGroup(null);
            groupMembersRequestBuilder = null;
        }
        initializeGroupMemberRequestBuilder();
    }

    private void initializeGroupMemberRequestBuilder() {
        if (groupMembersRequestBuilder == null)
            groupMembersRequestBuilder = new GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(requestLimit);
    }

    public void setUser(User user) {
        super.setUser(user);
    }

    @Override
    public boolean getDisableSuggestions() {
        boolean flag = getSelectedList().size() < mentionLimit;
        if (mentionsVisibility == UIKitConstants.MentionsVisibility.BOTH && flag) {
            return false;
        } else {
            if (mentionsVisibility == UIKitConstants.MentionsVisibility.USERS_CONVERSATION_ONLY && this.getUser() != null && flag) {
                return false;
            } else return mentionsVisibility != UIKitConstants.MentionsVisibility.GROUP_CONVERSATION_ONLY || this.getGroup() == null || !flag;
        }
    }

    private SpannableStringBuilder getConversationSpan(BaseMessage baseMessage, SpannableStringBuilder spannableStringBuilder) {

        if (spannableStringBuilder != null && baseMessage != null) {
            Matcher matcher = pattern.matcher(spannableStringBuilder.toString());

            Map<Integer, TagSpan> userSpanMap = new LinkedHashMap<>(); // Used to store UserSpans and their starting
            // indices
            int offset = 0;

            while (matcher.find()) {
                String userId = matcher.group(1);
                for (User user : baseMessage.getMentionedUsers()) {
                    if (user.getUid().equals(userId)) {
                        int startIdx = matcher.start() - offset;
                        String mentionText = getTrackingCharacter() + user.getName();
                        offset += matcher.group().length() - mentionText.length();

                        SuggestionItem suggestionItem = new SuggestionItem(user.getUid(),
                                                                           user.getName(),
                                                                           user.getAvatar(),
                                                                           user.getStatus(),
                                                                           getTrackingCharacter() + user.getName(),
                                                                           "<" + getTrackingCharacter() + "uid:" + user.getUid() + ">",
                                                                           user.toJson(),
                                                                           user
                                                                               .getUid()
                                                                               .equals(CometChatUIKit
                                                                                           .getLoggedInUser()
                                                                                           .getUid()) ? conversationSelfTagStyle : conversationTagStyle);

                        spannableStringBuilder.replace(startIdx, startIdx + matcher.group().length(), mentionText);

                        TagSpan userSpan = new TagSpan(getId(), mentionText, suggestionItem, (context1, user1) -> {
                            if (onTagClick != null) onTagClick.onClick(context1, user1);
                        });
                        userSpanMap.put(startIdx, userSpan);
                        break;
                    }
                }
            }

            for (Map.Entry<Integer, TagSpan> entry : userSpanMap.entrySet()) {
                Integer startIdx = entry.getKey();
                TagSpan userSpan = entry.getValue();
                spannableStringBuilder.setSpan(userSpan, startIdx, startIdx + userSpan.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableStringBuilder;
    }

    private SpannableStringBuilder getComposerSpan(BaseMessage baseMessage, SpannableStringBuilder spannableStringBuilder) {
        if (spannableStringBuilder != null && baseMessage != null) {
            Matcher matcher = pattern.matcher(spannableStringBuilder.toString());

            Map<Integer, NonEditableSpan> userSpanMap = new LinkedHashMap<>(); // Used to store UserSpans and their
            // starting indices
            int offset = 0;

            while (matcher.find()) {
                String userId = matcher.group(1);
                for (User user : baseMessage.getMentionedUsers()) {
                    if (user.getUid().equals(userId)) {
                        int startIdx = matcher.start() - offset;
                        String mentionText = getTrackingCharacter() + user.getName();
                        offset += matcher.group().length() - mentionText.length();

                        SuggestionItem suggestionItem = new SuggestionItem(user.getUid(),
                                                                           user.getName(),
                                                                           user.getAvatar(),
                                                                           user.getStatus(),
                                                                           getTrackingCharacter() + user.getName(),
                                                                           "<" + getTrackingCharacter() + "uid:" + user.getUid() + ">",
                                                                           user.toJson(),
                                                                           user
                                                                               .getUid()
                                                                               .equals(CometChatUIKit
                                                                                           .getLoggedInUser()
                                                                                           .getUid()) && selfTagStyle != null ? selfTagStyle : tagStyle);

                        spannableStringBuilder.replace(startIdx, startIdx + matcher.group().length(), mentionText);
                        NonEditableSpan span = new NonEditableSpan(getId(), mentionText, suggestionItem);
                        userSpanMap.put(startIdx, span);
                        break;
                    }
                }
            }

            for (Map.Entry<Integer, NonEditableSpan> entry : userSpanMap.entrySet()) {
                Integer startIdx = entry.getKey();
                NonEditableSpan userSpan = entry.getValue();
                spannableStringBuilder.setSpan(userSpan, startIdx, startIdx + userSpan.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableStringBuilder;
    }

    private SpannableStringBuilder getBubbleSpan(BaseMessage baseMessage,
                                                 UIKitConstants.MessageBubbleAlignment alignment,
                                                 SpannableStringBuilder spannableStringBuilder) {

        if (spannableStringBuilder != null && baseMessage != null) {
            Matcher matcher = pattern.matcher(spannableStringBuilder.toString());

            Map<Integer, TagSpan> userSpanMap = new LinkedHashMap<>(); // Used to store UserSpans and their starting
            // indices
            int offset = 0;

            while (matcher.find()) {
                String userId = matcher.group(1);
                for (User user : baseMessage.getMentionedUsers()) {
                    if (user.getUid().equals(userId)) {
                        int startIdx = matcher.start() - offset;
                        String mentionText = getTrackingCharacter() + user.getName();
                        offset += matcher.group().length() - mentionText.length();

                        SuggestionItem suggestionItem = new SuggestionItem(user.getUid(),
                                                                           user.getName(),
                                                                           user.getAvatar(),
                                                                           user.getStatus(),
                                                                           getTrackingCharacter() + user.getName(),
                                                                           "<" + getTrackingCharacter() + "uid:" + user.getUid() + ">",
                                                                           user.toJson(),
                                                                           getMentionStyleForAppropriateBubble(user, alignment));

                        spannableStringBuilder.replace(startIdx, startIdx + matcher.group().length(), mentionText);

                        TagSpan userSpan = new TagSpan(getId(), mentionText, suggestionItem, (context1, user1) -> {
                            if (onTagClick != null) onTagClick.onClick(context1, user1);
                        });

                        userSpanMap.put(startIdx, userSpan);

                        break;
                    }
                }
            }

            for (Map.Entry<Integer, TagSpan> entry : userSpanMap.entrySet()) {
                Integer startIdx = entry.getKey();
                TagSpan userSpan = entry.getValue();
                spannableStringBuilder.setSpan(userSpan, startIdx, startIdx + userSpan.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableStringBuilder;
    }

    private PromptTextStyle getMentionStyleForAppropriateBubble(User user, UIKitConstants.MessageBubbleAlignment alignment) {
        if (UIKitConstants.MessageBubbleAlignment.LEFT.equals(alignment)) {
            if (CometChatUIKit.getLoggedInUser().getUid().equals(user.getUid()))
                return outgoingBubbleSelfTagStyle;
            else return outgoingBubbleTagStyle;
        } else {
            if (CometChatUIKit.getLoggedInUser().getUid().equals(user.getUid()))
                return incomingBubbleSelfTagStyle;
            else return incomingBubbleTagStyle;
        }
    }

}
