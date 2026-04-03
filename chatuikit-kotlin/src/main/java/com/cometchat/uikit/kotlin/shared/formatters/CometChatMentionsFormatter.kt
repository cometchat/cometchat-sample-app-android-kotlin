package com.cometchat.uikit.kotlin.shared.formatters

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan
import com.cometchat.uikit.kotlin.shared.spans.OnTagClick
import com.cometchat.uikit.kotlin.shared.spans.TagSpan
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * CometChatMentionsFormatter handles @mention formatting in messages.
 *
 * This formatter provides comprehensive mention functionality including:
 * - Suggestion search for users and group members
 * - Mention text styling in composer, message bubbles, and conversation previews
 * - Support for "Mention All" (@all) in group chats
 * - Configurable mention limits and visibility
 * - Click handling for mentions in message bubbles
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val mentionsFormatter = CometChatMentionsFormatter(context)
 *
 * // Configure mention behavior
 * mentionsFormatter.setMentionLimit(10)
 * mentionsFormatter.setMentionsType(UIKitConstants.MentionsType.USERS_AND_GROUP_MEMBERS)
 *
 * // Set click handlers
 * mentionsFormatter.setOnMentionClick { context, user ->
 *     showUserProfile(user)
 * }
 * ```
 *
 * ## Style Configuration
 *
 * ### Using Style Resources (Recommended)
 * ```kotlin
 * mentionsFormatter.setMessageComposerMentionTextStyle(context, R.style.CometChatMessageComposerMentionsStyle)
 * mentionsFormatter.setIncomingBubbleMentionTextStyle(context, R.style.CometChatIncomingBubbleMentionsStyle)
 * mentionsFormatter.setOutgoingBubbleMentionTextStyle(context, R.style.CometChatOutgoingBubbleMentionsStyle)
 * ```
 *
 * ### Using CometChatMentionStyle
 * ```kotlin
 * val mentionStyle = CometChatMentionStyle(
 *     mentionTextColor = Color.BLUE,
 *     mentionBackgroundColor = Color.LTGRAY
 * )
 * mentionsFormatter.setMentionStyle(mentionStyle)
 * ```
 *
 * ## Mention Format
 *
 * Mentions are stored in the following format:
 * - User mentions: `<@uid:userId>` → displayed as `@userName`
 * - Mention all: `<@all:all>` → displayed as `@Notify All`
 *
 * @param context Android context for resource access
 * @param trackingCharacter Character that triggers mention detection (default: '@')
 *
 * @see CometChatTextFormatter Base class for text formatters
 * @see PromptTextStyle Style configuration for mention text
 * @see SuggestionItem Data model for suggestion items
 */
open class CometChatMentionsFormatter(
    private val context: Context,
    trackingCharacter: Char = '@'
) : CometChatTextFormatter(trackingCharacter) {

    companion object {
        private val TAG = CometChatMentionsFormatter::class.java.simpleName
        private const val REQUEST_LIMIT = 10
    }

    private val localSuggestionItemList = mutableListOf<SuggestionItem>()
    private val defaultRegex = "<${getTrackingCharacter()}uid:(.*?)>"

    private var groupId: String? = null
    private var usersRequestBuilder: UsersRequest.UsersRequestBuilder? = null
    private var groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null
    private var groupMembersRequestBuilderCallback: ((Group) -> GroupMembersRequest.GroupMembersRequestBuilder)? = null

    private var mentionType = UIKitConstants.MentionsType.USERS_AND_GROUP_MEMBERS
    private var mentionsVisibility = UIKitConstants.MentionsVisibility.BOTH
    private var localMentionLimit = 10

    private var groupMembersRequest: GroupMembersRequest? = null
    private var usersRequest: UsersRequest? = null

    // Style properties
    private var selfTagStyle: PromptTextStyle? = null
    private var tagStyle: PromptTextStyle? = null
    private var outgoingBubbleTagStyle: PromptTextStyle? = null
    private var outgoingBubbleSelfTagStyle: PromptTextStyle? = null
    private var incomingBubbleSelfTagStyle: PromptTextStyle? = null
    private var incomingBubbleTagStyle: PromptTextStyle? = null
    private var conversationSelfTagStyle: PromptTextStyle? = null
    private var conversationTagStyle: PromptTextStyle? = null

    @StyleRes private var messageComposerMentionTextStyle: Int = 0
    @StyleRes private var incomingBubbleMentionTextStyle: Int = 0
    @StyleRes private var outgoingBubbleMentionTextStyle: Int = 0
    @StyleRes private var conversationsMentionTextStyle: Int = 0

    private var onTagClick: ((Context, User) -> Unit)? = null
    private var mentionAllClick: (() -> Unit)? = null

    private var pattern: Pattern
    private var mentionAllPattern: Pattern

    private var disableMentionAll = false
    private var mentionAllId = "all"
    private var mentionAllLabelText: String
    private var mentionAllInfoText: String

    init {
        mentionAllLabelText = context.getString(R.string.cometchat_notify_all)
        mentionAllInfoText = context.getString(R.string.cometchat_notify_everyone_in_this_group)

        // Initialize styles from @StyleRes resources (matching chatuikit Java implementation)
        setOutgoingBubbleMentionTextStyle(context, R.style.CometChatOutgoingBubbleMentionsStyle)
        setIncomingBubbleMentionTextStyle(context, R.style.CometChatIncomingBubbleMentionsStyle)
        setMessageComposerMentionTextStyle(context, R.style.CometChatMessageComposerMentionsStyle)
        setConversationsMentionTextStyle(context, R.style.CometChatConversationsMentionsStyle)

        pattern = Pattern.compile(defaultRegex)
        mentionAllPattern = Pattern.compile(generateMentionAllRegexPattern(mentionAllId))
    }

    constructor(context: Context, trackingCharacter: Char, regexPattern: String) : this(context, trackingCharacter) {
        pattern = Pattern.compile(regexPattern)
    }

    // ==================== @StyleRes Setter Methods ====================

    /**
     * Sets the mention text style for outgoing message bubbles from a style resource.
     * This extracts colors and text appearance from the XML style definition.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID (e.g., R.style.CometChatOutgoingBubbleMentionsStyle)
     */
    fun setOutgoingBubbleMentionTextStyle(context: Context, @StyleRes style: Int) {
        if (style != 0) {
            outgoingBubbleMentionTextStyle = style
            // Note: In Java chatuikit, outgoing style sets incoming bubble styles (swapped for RTL support)
            incomingBubbleTagStyle = getPromptTextStyleFromResource(context, style)
            incomingBubbleSelfTagStyle = getSelfPromptTextStyleFromResource(context, style)
        }
    }

    /**
     * Sets the mention text style for incoming message bubbles from a style resource.
     * This extracts colors and text appearance from the XML style definition.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID (e.g., R.style.CometChatIncomingBubbleMentionsStyle)
     */
    fun setIncomingBubbleMentionTextStyle(context: Context, @StyleRes style: Int) {
        if (style != 0) {
            incomingBubbleMentionTextStyle = style
            // Note: In Java chatuikit, incoming style sets outgoing bubble styles (swapped for RTL support)
            outgoingBubbleTagStyle = getPromptTextStyleFromResource(context, style)
            outgoingBubbleSelfTagStyle = getSelfPromptTextStyleFromResource(context, style)
        }
    }

    /**
     * Sets the mention text style for the message composer from a style resource.
     * This extracts colors and text appearance from the XML style definition.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID (e.g., R.style.CometChatMessageComposerMentionsStyle)
     */
    fun setMessageComposerMentionTextStyle(context: Context, @StyleRes style: Int) {
        if (style != 0) {
            messageComposerMentionTextStyle = style
            tagStyle = getPromptTextStyleFromResource(context, style)
            selfTagStyle = getSelfPromptTextStyleFromResource(context, style)
        }
    }

    /**
     * Sets the mention text style for conversation list previews from a style resource.
     * This extracts colors and text appearance from the XML style definition.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID (e.g., R.style.CometChatConversationsMentionsStyle)
     */
    fun setConversationsMentionTextStyle(context: Context, @StyleRes style: Int) {
        if (style != 0) {
            conversationsMentionTextStyle = style
            conversationTagStyle = getPromptTextStyleFromResource(context, style)
            conversationSelfTagStyle = getSelfPromptTextStyleFromResource(context, style)
        }
    }

    // ==================== @StyleRes Getter Methods ====================

    fun getMessageComposerMentionTextStyle(): Int = messageComposerMentionTextStyle
    fun getIncomingBubbleMentionTextStyle(): Int = incomingBubbleMentionTextStyle
    fun getOutgoingBubbleMentionTextStyle(): Int = outgoingBubbleMentionTextStyle
    fun getConversationsMentionTextStyle(): Int = conversationsMentionTextStyle

    // ==================== Helper Methods for @StyleRes ====================

    /**
     * Extracts a PromptTextStyle from a style resource for regular mentions.
     * Reads cometchatMentionTextAppearance, cometchatMentionTextColor, and cometchatMentionBackgroundColor.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID
     * @return A PromptTextStyle configured from the style resource
     */
    private fun getPromptTextStyleFromResource(context: Context, @StyleRes style: Int): PromptTextStyle {
        val promptTextStyle = PromptTextStyle()
        if (style != 0) {
            val typedArray: TypedArray = context.theme.obtainStyledAttributes(
                null,
                R.styleable.CometChatMentionStyle,
                R.attr.cometchatMentionsStyle,
                style
            )
            try {
                val textAppearanceResId = typedArray.getResourceId(
                    R.styleable.CometChatMentionStyle_cometchatMentionTextAppearance, 0
                )
                promptTextStyle.setTextAppearance(getTypefaceFromTextAppearance(context, textAppearanceResId))
                promptTextStyle.setTextSize(getTextSizeFromTextAppearance(context, textAppearanceResId))
                promptTextStyle.setColor(
                    typedArray.getColor(
                        R.styleable.CometChatMentionStyle_cometchatMentionTextColor,
                        CometChatTheme.getPrimaryColor(context)
                    )
                )
                promptTextStyle.setBackgroundColor(
                    typedArray.getColor(
                        R.styleable.CometChatMentionStyle_cometchatMentionBackgroundColor,
                        0
                    )
                )
            } finally {
                typedArray.recycle()
            }
        }
        return promptTextStyle
    }

    /**
     * Extracts a PromptTextStyle from a style resource for self mentions.
     * Reads cometchatSelfMentionTextAppearance, cometchatSelfMentionTextColor, and cometchatSelfMentionBackgroundColor.
     *
     * @param context The context to resolve resources
     * @param style The style resource ID
     * @return A PromptTextStyle configured from the style resource for self mentions
     */
    private fun getSelfPromptTextStyleFromResource(context: Context, @StyleRes style: Int): PromptTextStyle {
        val promptTextStyle = PromptTextStyle()
        if (style != 0) {
            val typedArray: TypedArray = context.theme.obtainStyledAttributes(
                null,
                R.styleable.CometChatMentionStyle,
                R.attr.cometchatMentionsStyle,
                style
            )
            try {
                val textAppearanceResId = typedArray.getResourceId(
                    R.styleable.CometChatMentionStyle_cometchatSelfMentionTextAppearance, 0
                )
                promptTextStyle.setTextAppearance(getTypefaceFromTextAppearance(context, textAppearanceResId))
                promptTextStyle.setTextSize(getTextSizeFromTextAppearance(context, textAppearanceResId))
                promptTextStyle.setColor(
                    typedArray.getColor(
                        R.styleable.CometChatMentionStyle_cometchatSelfMentionTextColor,
                        0
                    )
                )
                promptTextStyle.setBackgroundColor(
                    typedArray.getColor(
                        R.styleable.CometChatMentionStyle_cometchatSelfMentionBackgroundColor,
                        0
                    )
                )
            } finally {
                typedArray.recycle()
            }
        }
        return promptTextStyle
    }

    /**
     * Extracts a Typeface from a text appearance style resource.
     *
     * @param context The context to resolve resources
     * @param textAppearanceResId The text appearance style resource ID
     * @return The Typeface extracted from the text appearance, or default if not found
     */
    private fun getTypefaceFromTextAppearance(context: Context, @StyleRes textAppearanceResId: Int): Typeface? {
        if (textAppearanceResId == 0) return null

        var fontFamily: String? = null
        var textStyle = Typeface.NORMAL

        val typedArray = context.obtainStyledAttributes(
            textAppearanceResId,
            androidx.appcompat.R.styleable.TextAppearance
        )
        try {
            fontFamily = typedArray.getString(androidx.appcompat.R.styleable.TextAppearance_fontFamily)
            if (fontFamily == null) {
                fontFamily = typedArray.getString(androidx.appcompat.R.styleable.TextAppearance_android_fontFamily)
            }
            textStyle = typedArray.getInt(
                androidx.appcompat.R.styleable.TextAppearance_android_textStyle,
                textStyle
            )
        } finally {
            typedArray.recycle()
        }

        return if (fontFamily != null) {
            Typeface.create(fontFamily, textStyle)
        } else {
            Typeface.defaultFromStyle(textStyle)
        }
    }

    /**
     * Extracts the text size from a text appearance style resource.
     *
     * @param context The context to resolve resources
     * @param textAppearanceResId The text appearance style resource ID
     * @return The text size in pixels, or -1 if not found
     */
    @Dimension
    private fun getTextSizeFromTextAppearance(context: Context, @StyleRes textAppearanceResId: Int): Int {
        if (textAppearanceResId == 0) return -1

        val typedArray = context.obtainStyledAttributes(
            textAppearanceResId,
            androidx.appcompat.R.styleable.TextAppearance
        )
        return try {
            typedArray.getDimensionPixelSize(
                androidx.appcompat.R.styleable.TextAppearance_android_textSize,
                -1
            )
        } finally {
            typedArray.recycle()
        }
    }

    // ==================== Deprecated Init Method ====================

    @Deprecated("Use @StyleRes setter methods instead", ReplaceWith("setOutgoingBubbleMentionTextStyle(context, style)"))
    private fun initDefaultStyles() {
        val defaultColor = CometChatTheme.getPrimaryColor(context)
        val defaultStyle = PromptTextStyle().setColor(defaultColor)
        tagStyle = defaultStyle
        selfTagStyle = defaultStyle
        outgoingBubbleTagStyle = defaultStyle
        outgoingBubbleSelfTagStyle = defaultStyle
        incomingBubbleTagStyle = defaultStyle
        incomingBubbleSelfTagStyle = defaultStyle
        conversationTagStyle = defaultStyle
        conversationSelfTagStyle = defaultStyle
    }

    private fun generateMentionAllRegexPattern(mentionAllId: String): String {
        return "<${getTrackingCharacter()}all:$mentionAllId>"
    }

    // Style setters
    fun setMentionLimit(limit: Int) { localMentionLimit = limit }
    fun setMentionsVisibility(visibility: UIKitConstants.MentionsVisibility) { mentionsVisibility = visibility }
    fun setMentionsType(type: UIKitConstants.MentionsType) { mentionType = type }
    fun setOnMentionClick(click: (Context, User) -> Unit) { onTagClick = click }
    fun setOnMentionAllClick(click: () -> Unit) { mentionAllClick = click }
    fun setDisableMentionAll(disable: Boolean) { disableMentionAll = disable }
    fun setOutgoingBubbleSelfTagStyle(style: PromptTextStyle?) { outgoingBubbleSelfTagStyle = style }
    fun setOutgoingBubbleTagStyle(style: PromptTextStyle?) { outgoingBubbleTagStyle = style }
    fun setIncomingBubbleSelfTagStyle(style: PromptTextStyle?) { incomingBubbleSelfTagStyle = style }
    fun setIncomingBubbleTagStyle(style: PromptTextStyle?) { incomingBubbleTagStyle = style }
    fun setSelfTagStyle(style: PromptTextStyle?) { selfTagStyle = style }
    fun setTagStyle(style: PromptTextStyle?) { tagStyle = style }
    fun setConversationSelfTagStyle(style: PromptTextStyle?) { conversationSelfTagStyle = style }
    fun setConversationTagStyle(style: PromptTextStyle?) { conversationTagStyle = style }

    /**
     * Sets the mention style for all contexts (bubbles, conversations, composer).
     */
    fun setMentionStyle(style: CometChatMentionStyle) {
        val promptStyle = style.toPromptTextStyle()
        val selfPromptStyle = style.toSelfPromptTextStyle()

        tagStyle = promptStyle
        selfTagStyle = selfPromptStyle
        outgoingBubbleTagStyle = promptStyle
        outgoingBubbleSelfTagStyle = selfPromptStyle
        incomingBubbleTagStyle = promptStyle
        incomingBubbleSelfTagStyle = selfPromptStyle
        conversationTagStyle = promptStyle
        conversationSelfTagStyle = selfPromptStyle
    }

    /**
     * Sets the mention style specifically for conversation list previews.
     */
    fun setConversationsMentionStyle(style: CometChatMentionStyle) {
        conversationTagStyle = style.toPromptTextStyle()
        conversationSelfTagStyle = style.toSelfPromptTextStyle()
    }

    fun setMentionAllLabel(labelId: String, labelText: String) {
        if (labelId.isNotEmpty() && labelText.isNotEmpty()) {
            mentionAllLabelText = labelText
            mentionAllId = labelId
            mentionAllPattern = Pattern.compile(generateMentionAllRegexPattern(mentionAllId))
        }
    }

    fun setMentionAllInfoText(infoText: String) { mentionAllInfoText = infoText }

    fun setGroupMembersRequestBuilder(callback: (Group) -> GroupMembersRequest.GroupMembersRequestBuilder) {
        groupMembersRequestBuilderCallback = callback
        getGroup()?.let { groupMembersRequestBuilder = callback(it) }
        initializeGroupMemberRequestBuilder()
    }

    fun setUsersRequestBuilder(builder: UsersRequest.UsersRequestBuilder) { usersRequestBuilder = builder }

    private fun initializeGroupMemberRequestBuilder() {
        if (groupMembersRequestBuilder == null && groupId != null) {
            groupMembersRequestBuilder = GroupMembersRequest.GroupMembersRequestBuilder(groupId)
                .setLimit(REQUEST_LIMIT)
        }
    }

    private fun initializeUserRequestBuilder() {
        if (usersRequestBuilder == null) {
            usersRequestBuilder = UsersRequest.UsersRequestBuilder().setLimit(REQUEST_LIMIT)
        }
    }

    override fun search(context: Context, queryString: String?) {
        // Match Java: show shimmer only when list is empty AND query is empty (just typed @)
        setShowLoadingIndicator(localSuggestionItemList.isEmpty() && queryString != null && queryString.isEmpty())
        if (getSelectedList().size < localMentionLimit) {
            if (queryString != null) {
                searchMentions(queryString)
            } else {
                localSuggestionItemList.clear()
                setSuggestionItemList(emptyList())
            }
        } else {
            localSuggestionItemList.clear()
            setSuggestionItemList(emptyList())
        }
    }

    fun searchMentions(queryString: String) {
        when (mentionType) {
            UIKitConstants.MentionsType.USERS -> searchUser(queryString)
            UIKitConstants.MentionsType.USERS_AND_GROUP_MEMBERS -> {
                if (getGroup() != null) searchGroupMember(queryString) else searchUser(queryString)
            }
        }
    }

    fun searchUser(queryString: String) {
        // Always create a fresh request builder for new searches to avoid pagination issues
        val builder = usersRequestBuilder?.setSearchKeyword(queryString) 
            ?: UsersRequest.UsersRequestBuilder().setLimit(REQUEST_LIMIT).setSearchKeyword(queryString)
        usersRequest = builder.build()
        fetchUsers()
    }

    fun searchGroupMember(queryString: String) {
        // Always create a fresh request builder for new searches to avoid pagination issues
        val builder = if (groupMembersRequestBuilder != null) {
            groupMembersRequestBuilder?.setSearchKeyword(queryString)
        } else if (groupId != null) {
            GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(REQUEST_LIMIT).setSearchKeyword(queryString)
        } else {
            null
        }
        groupMembersRequest = builder?.build()
        fetchGroupMembers()
    }

    fun fetchUsers() {
        usersRequest?.fetchNext(object : CometChat.CallbackListener<List<User>>() {
            override fun onSuccess(users: List<User>) {
                val suggestions = users.map { user ->
                    SuggestionItem(
                        id = user.uid,
                        name = user.name,
                        leadingIconUrl = user.avatar,
                        status = user.status,
                        promptText = "${getTrackingCharacter()}${user.name}",
                        underlyingText = "<${getTrackingCharacter()}uid:${user.uid}>",
                        data = try { user.toJson() } catch (e: Exception) { null },
                        promptTextStyle = tagStyle
                    )
                }
                localSuggestionItemList.clear()
                localSuggestionItemList.addAll(suggestions)
                setSuggestionItemList(localSuggestionItemList.toList())
            }

            override fun onError(e: CometChatException) {
                localSuggestionItemList.clear()
                setSuggestionItemList(emptyList())
            }
        })
    }

    fun fetchGroupMembers() {
        groupMembersRequest?.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
            override fun onSuccess(groupMembers: List<GroupMember>) {
                val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
                val suggestions = mutableListOf<SuggestionItem>()

                for (member in groupMembers) {
                    val isSelf = member.uid == loggedInUser?.uid
                    suggestions.add(SuggestionItem(
                        id = member.uid,
                        name = member.name,
                        leadingIconUrl = member.avatar,
                        status = member.status,
                        promptText = "${getTrackingCharacter()}${member.name}",
                        underlyingText = "<${getTrackingCharacter()}uid:${member.uid}>",
                        data = try { member.toJson() } catch (e: Exception) { null },
                        promptTextStyle = if (isSelf && selfTagStyle != null) selfTagStyle else tagStyle
                    ))
                }

                // Add mention all option for groups
                if (!disableMentionAll && getGroup() != null) {
                    val metaData = JSONObject()
                    try { metaData.put("infoText", mentionAllInfoText) } catch (e: JSONException) { }

                    val searchKeyword = groupMembersRequest?.searchKeyword ?: ""

                    if (mentionAllId.lowercase().contains(searchKeyword.lowercase())) {
                        val group = getGroup()
                        val iconUrl = when {
                            group?.icon?.isNotEmpty() == true -> group.icon
                            group?.name?.isNotEmpty() == true -> group.name
                            else -> ""
                        }
                        suggestions.add(0, SuggestionItem(
                            id = mentionAllId,
                            name = "${getTrackingCharacter()}$mentionAllLabelText",
                            leadingIconUrl = iconUrl,
                            status = null,
                            promptText = "${getTrackingCharacter()}$mentionAllLabelText",
                            underlyingText = "<${getTrackingCharacter()}all:$mentionAllId>",
                            data = metaData,
                            promptTextStyle = selfTagStyle
                        ))
                    }
                }

                localSuggestionItemList.clear()
                localSuggestionItemList.addAll(suggestions)
                setSuggestionItemList(localSuggestionItemList.toList())
            }

            override fun onError(e: CometChatException) {
                localSuggestionItemList.clear()
                setSuggestionItemList(emptyList())
            }
        })
    }

    override fun onItemClick(context: Context, suggestionItem: SuggestionItem, user: User?, group: Group?) { }

    override fun handlePreMessageSend(context: Context, baseMessage: BaseMessage) {
        baseMessage.mentionedUsers = getMentionUsers()
    }

    fun getMentionUsers(): List<User> {
        return getSelectedList().mapNotNull { item ->
            try {
                item.data?.let { User.fromJson(it.toString()) }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onScrollToBottom() {
        if (groupMembersRequestBuilder != null) fetchGroupMembers()
        else if (usersRequestBuilder != null) fetchUsers()
    }

    override fun observeSelectionList(context: Context, selectedSuggestionItemList: List<SuggestionItem>) {
        setDisableSuggestions(getSelectedList().size >= localMentionLimit)
        setInfoVisibility(getSelectedList().size >= localMentionLimit)
        setInfoText(if (getSelectedList().size >= localMentionLimit) 
            "You can mention up to $localMentionLimit times at a time" else "")
    }

    override fun setGroup(group: Group?) {
        if (group != null) {
            super.setGroup(group)
            groupId = group.guid
            groupMembersRequestBuilderCallback?.let { groupMembersRequestBuilder = it(group) }
        } else {
            super.setGroup(null)
            groupMembersRequestBuilder = null
        }
        initializeGroupMemberRequestBuilder()
    }

    override fun getDisableSuggestions(): Boolean {
        val flag = getSelectedList().size < localMentionLimit
        return when (mentionsVisibility) {
            UIKitConstants.MentionsVisibility.BOTH -> !flag
            UIKitConstants.MentionsVisibility.USERS_CONVERSATION_ONLY -> !(getUser() != null && flag)
            UIKitConstants.MentionsVisibility.GROUP_CONVERSATION_ONLY -> !(getGroup() == null || !flag)
        }
    }

    override fun prepareLeftMessageBubbleSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? {
        return if (context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, spannable)
        } else {
            getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.LEFT, spannable)
        }
    }

    override fun prepareRightMessageBubbleSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? {
        return if (context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.LEFT, spannable)
        } else {
            getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, spannable)
        }
    }

    override fun prepareComposerSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = getComposerSpan(baseMessage, spannable)

    override fun prepareConversationSpan(
        context: Context,
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? = getConversationSpan(baseMessage, spannable)

    private fun getConversationSpan(
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder {
        val originalText = spannable.toString()
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<MentionReplacement>()

        // Process mention all
        if (!disableMentionAll) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                replacements.add(MentionReplacement(
                    mentionAllMatcher.start(), mentionAllMatcher.end(),
                    "${getTrackingCharacter()}$mentionAllLabelText", conversationSelfTagStyle, true
                ))
            }
        }

        // Process user mentions
        val mentionedUsers = baseMessage.mentionedUsers
        if (mentionedUsers != null && mentionedUsers.isNotEmpty()) {
            val matcher = pattern.matcher(originalText)
            while (matcher.find()) {
                val userId = matcher.group(1)
                val user = mentionedUsers.find { it.uid == userId }
                if (user != null) {
                    val isSelf = user.uid == loggedInUser?.uid
                    replacements.add(MentionReplacement(
                        matcher.start(), matcher.end(),
                        "${getTrackingCharacter()}${user.name}",
                        if (isSelf) conversationSelfTagStyle else conversationTagStyle, isSelf
                    ))
                }
            }
        }

        return buildStyledSpannable(spannable, replacements)
    }

    private fun getComposerSpan(
        baseMessage: BaseMessage,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder {
        val originalText = spannable.toString()
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<MentionReplacement>()

        // Process mention all
        if (!disableMentionAll && getGroup() != null) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                val matchedText = mentionAllMatcher.group()
                replacements.add(MentionReplacement(
                    start = mentionAllMatcher.start(),
                    end = mentionAllMatcher.end(),
                    replacement = "${getTrackingCharacter()}$mentionAllId",
                    style = selfTagStyle,
                    isSelf = true,
                    user = null,
                    underlyingText = matchedText,
                    isMentionAll = true
                ))
            }
        }

        // Process user mentions
        val mentionedUsers = baseMessage.mentionedUsers
        if (mentionedUsers != null && mentionedUsers.isNotEmpty()) {
            val matcher = pattern.matcher(originalText)
            while (matcher.find()) {
                val userId = matcher.group(1)
                val matchedText = matcher.group()
                val user = mentionedUsers.find { it.uid == userId }
                if (user != null) {
                    val isSelf = user.uid == loggedInUser?.uid
                    val styleToUse = if (isSelf && selfTagStyle != null) selfTagStyle else tagStyle
                    replacements.add(MentionReplacement(
                        start = matcher.start(),
                        end = matcher.end(),
                        replacement = "${getTrackingCharacter()}${user.name}",
                        style = styleToUse,
                        isSelf = isSelf,
                        user = user,
                        underlyingText = matchedText,
                        isMentionAll = false
                    ))
                }
            }
        }

        return buildStyledSpannable(spannable, replacements)
    }

    private fun getBubbleSpan(
        baseMessage: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder {
        val originalText = spannable.toString()
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<BubbleMentionReplacement>()

        android.util.Log.d(TAG, "getBubbleSpan: originalText='$originalText', messageId=${baseMessage.id}, muid=${baseMessage.muid}")
        android.util.Log.d(TAG, "getBubbleSpan: mentionedUsers=${baseMessage.mentionedUsers?.map { it.uid }}")

        // Process mention all
        if (!disableMentionAll) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                val metaData = JSONObject()
                try { metaData.put("infoText", mentionAllInfoText) } catch (e: JSONException) { }
                
                replacements.add(BubbleMentionReplacement(
                    start = mentionAllMatcher.start(),
                    end = mentionAllMatcher.end(),
                    replacement = "${getTrackingCharacter()}$mentionAllLabelText",
                    style = getMentionAllStyleForBubble(alignment),
                    isMentionAll = true,
                    user = null,
                    data = metaData
                ))
            }
        }

        // Process user mentions
        val mentionedUsers = baseMessage.mentionedUsers
        android.util.Log.d(TAG, "getBubbleSpan: mentionedUsers isNull=${mentionedUsers == null}, isEmpty=${mentionedUsers?.isEmpty()}")
        if (mentionedUsers != null && mentionedUsers.isNotEmpty()) {
            val matcher = pattern.matcher(originalText)
            android.util.Log.d(TAG, "getBubbleSpan: pattern=$pattern, looking for matches in '$originalText'")
            while (matcher.find()) {
                val userId = matcher.group(1)
                android.util.Log.d(TAG, "getBubbleSpan: found match for userId='$userId' at ${matcher.start()}-${matcher.end()}")
                val user = mentionedUsers.find { it.uid == userId }
                if (user != null) {
                    android.util.Log.d(TAG, "getBubbleSpan: found user ${user.name} for userId=$userId")
                    val isSelf = user.uid == loggedInUser?.uid
                    replacements.add(BubbleMentionReplacement(
                        start = matcher.start(),
                        end = matcher.end(),
                        replacement = "${getTrackingCharacter()}${user.name}",
                        style = getMentionStyleForBubble(isSelf, alignment),
                        isMentionAll = false,
                        user = user,
                        data = try { user.toJson() } catch (e: Exception) { null }
                    ))
                } else {
                    android.util.Log.d(TAG, "getBubbleSpan: user NOT found for userId=$userId in mentionedUsers")
                }
            }
        }

        android.util.Log.d(TAG, "getBubbleSpan: total replacements=${replacements.size}")
        return buildBubbleStyledSpannable(spannable, replacements)
    }

    /**
     * Builds a styled SpannableStringBuilder for message bubbles using TagSpan (clickable).
     * TagSpan allows click handling for mentions in message bubbles.
     */
    private fun buildBubbleStyledSpannable(
        original: SpannableStringBuilder,
        replacements: List<BubbleMentionReplacement>
    ): SpannableStringBuilder {
        if (replacements.isEmpty()) return original

        val originalText = original.toString()
        val result = SpannableStringBuilder()
        val sortedReplacements = replacements.sortedBy { it.start }
        var lastEnd = 0

        for (replacement in sortedReplacements) {
            if (replacement.start >= lastEnd && replacement.end <= originalText.length) {
                // Append text before this replacement
                if (replacement.start > lastEnd) {
                    result.append(originalText.substring(lastEnd, replacement.start))
                }

                // Record start position for styling
                val styleStart = result.length

                // Append replacement text
                result.append(replacement.replacement)

                // Record end position for styling
                val styleEnd = result.length

                // Apply styles using TagSpan for clickable mentions in bubbles
                replacement.style?.let { style ->
                    val suggestionItem = SuggestionItem(
                        id = if (replacement.isMentionAll) mentionAllId else (replacement.user?.uid ?: ""),
                        name = replacement.replacement,
                        leadingIconUrl = replacement.user?.avatar,
                        status = replacement.user?.status,
                        promptText = replacement.replacement,
                        underlyingText = "",
                        data = replacement.data,
                        promptTextStyle = style
                    )
                    
                    // Create click handler based on mention type
                    val clickHandler: OnTagClick<User>? = if (replacement.isMentionAll) {
                        OnTagClick { _, _ -> mentionAllClick?.invoke() }
                    } else {
                        onTagClick?.let { callback ->
                            OnTagClick { ctx, user -> callback(ctx, user) }
                        }
                    }
                    
                    result.setSpan(
                        TagSpan(getTrackingCharacter(), replacement.replacement, suggestionItem, clickHandler),
                        styleStart, styleEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                lastEnd = replacement.end
            }
        }

        // Append remaining text
        if (lastEnd < originalText.length) {
            result.append(originalText.substring(lastEnd))
        }

        return result
    }

    /**
     * Builds a styled SpannableStringBuilder by applying replacements and styles.
     * Uses NonEditableSpan for mentions which automatically applies 20% alpha to background colors,
     * matching the chatuikit Java implementation.
     * Creates SuggestionItem with user data and underlyingText for proper encoding on send.
     * 
     * Also applies ForegroundColorSpan and BackgroundColorSpan for EditText compatibility,
     * since ClickableSpan.updateDrawState() requires a MovementMethod which interferes with editing.
     */
    private fun buildStyledSpannable(
        original: SpannableStringBuilder,
        replacements: List<MentionReplacement>
    ): SpannableStringBuilder {
        if (replacements.isEmpty()) return original

        val originalText = original.toString()
        val result = SpannableStringBuilder()
        val sortedReplacements = replacements.sortedBy { it.start }
        var lastEnd = 0

        for (replacement in sortedReplacements) {
            if (replacement.start >= lastEnd && replacement.end <= originalText.length) {
                // Append text before this replacement
                if (replacement.start > lastEnd) {
                    result.append(originalText.substring(lastEnd, replacement.start))
                }

                // Record start position for styling
                val styleStart = result.length

                // Append replacement text
                result.append(replacement.replacement)

                // Record end position for styling
                val styleEnd = result.length

                // Create SuggestionItem with user data for proper encoding on send
                val suggestionItem = SuggestionItem(
                    id = if (replacement.isMentionAll) mentionAllId else (replacement.user?.uid ?: ""),
                    name = replacement.replacement,
                    leadingIconUrl = replacement.user?.avatar,
                    status = replacement.user?.status,
                    promptText = replacement.replacement,
                    underlyingText = replacement.underlyingText,
                    data = try { replacement.user?.toJson() } catch (e: Exception) { null },
                    promptTextStyle = replacement.style
                )

                // Use NonEditableSpan with SuggestionItem for comprehensive styling and data
                result.setSpan(
                    NonEditableSpan(getTrackingCharacter(), replacement.replacement, suggestionItem),
                    styleStart, styleEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                // Apply ForegroundColorSpan and BackgroundColorSpan for EditText compatibility
                // ClickableSpan.updateDrawState() requires MovementMethod which interferes with editing
                replacement.style?.let { style ->
                    if (style.getColor() != 0) {
                        result.setSpan(
                            android.text.style.ForegroundColorSpan(style.getColor()),
                            styleStart, styleEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    if (style.getBackgroundColor() != 0) {
                        // Apply 20% alpha to background color (alpha = 51)
                        val bgColorWithAlpha = (51 shl 24) or (style.getBackgroundColor() and 0x00FFFFFF)
                        result.setSpan(
                            android.text.style.BackgroundColorSpan(bgColorWithAlpha),
                            styleStart, styleEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                lastEnd = replacement.end
            }
        }

        // Append remaining text
        if (lastEnd < originalText.length) {
            result.append(originalText.substring(lastEnd))
        }

        return result
    }

    private fun getMentionStyleForBubble(
        isSelf: Boolean,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): PromptTextStyle? = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> 
            if (isSelf) outgoingBubbleSelfTagStyle else outgoingBubbleTagStyle
        else -> 
            if (isSelf) incomingBubbleSelfTagStyle else incomingBubbleTagStyle
    }

    private fun getMentionAllStyleForBubble(alignment: UIKitConstants.MessageBubbleAlignment): PromptTextStyle? =
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> outgoingBubbleSelfTagStyle
            else -> incomingBubbleSelfTagStyle
        }

    /**
     * Data class representing a mention replacement with position, text, style, and user data.
     * Includes underlyingText for encoding back to <@uid:userId> on send.
     */
    private data class MentionReplacement(
        val start: Int,
        val end: Int,
        val replacement: String,
        val style: PromptTextStyle?,
        val isSelf: Boolean,
        val user: User? = null,
        val underlyingText: String = "",
        val isMentionAll: Boolean = false
    )

    /**
     * Data class representing a mention replacement for message bubbles with click support.
     */
    private data class BubbleMentionReplacement(
        val start: Int,
        val end: Int,
        val replacement: String,
        val style: PromptTextStyle?,
        val isMentionAll: Boolean,
        val user: User?,
        val data: JSONObject?
    )
}
