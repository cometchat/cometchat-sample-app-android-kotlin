package com.cometchat.uikit.compose.presentation.shared.formatters

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.style.PromptTextStyle
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class CometChatMentionsFormatter(
    private val context: Context
) : CometChatTextFormatter('@') {
    
    companion object {
        private const val TAG = "CometChatMentionsFormatter"
    }
    
    private val requestLimit = 10
    private val localSuggestionItemList = mutableStateOf<List<SuggestionItem>>(emptyList())
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
    
    private var selfTagStyle: PromptTextStyle? = null
    private var tagStyle: PromptTextStyle? = null
    private var outgoingBubbleTagStyle: PromptTextStyle? = null
    private var outgoingBubbleSelfTagStyle: PromptTextStyle? = null
    private var incomingBubbleSelfTagStyle: PromptTextStyle? = null
    private var incomingBubbleTagStyle: PromptTextStyle? = null
    private var conversationSelfTagStyle: PromptTextStyle? = null
    private var conversationTagStyle: PromptTextStyle? = null
    
    private var onTagClick: ((Context, User) -> Unit)? = null
    private var mentionAllClick: (() -> Unit)? = null
    
    private var pattern: Pattern = Pattern.compile(defaultRegex)
    private var mentionAllPattern: Pattern
    private var disableMentionAll = false
    private var mentionAllId = "all"
    private var mentionAllLabelText: String
    private var mentionAllInfoText: String

    init {
        // Initialize mention all text from string resources
        mentionAllLabelText = context.getString(R.string.cometchat_notify_all)
        mentionAllInfoText = context.getString(R.string.cometchat_notify_everyone_in_this_group)
        mentionAllPattern = Pattern.compile(generateMentionAllRegexPattern(mentionAllId))
        initDefaultStyles()
    }

    constructor(context: Context, trackingCharacter: Char) : this(context)
    constructor(context: Context, trackingCharacter: Char, regexPattern: String) : this(context) {
        pattern = Pattern.compile(regexPattern)
    }

    private fun initDefaultStyles() {
        // Primary color for incoming bubble mentions (matches Java: cometchatPrimaryColor #6852D6)
        val primaryColor = 0xFF6852D6.toInt()
        // White for outgoing bubble mentions (matches Java: cometchatColorWhite #FFFFFF)
        val whiteColor = 0xFFFFFFFF.toInt()
        // Warning/amber color for self-mentions (matches Java: cometchatWarningColor #FFAB00)
        val warningColor = 0xFFFFAB00.toInt()
        
        // Default/composer styles use primary color
        val defaultStyle = PromptTextStyle()
            .setColor(primaryColor)
            .setBackgroundColor(primaryColor)
        val defaultSelfStyle = PromptTextStyle()
            .setColor(warningColor)
            .setBackgroundColor(warningColor)
        
        tagStyle = defaultStyle
        selfTagStyle = defaultSelfStyle
        
        // Incoming bubble (LEFT alignment in formatter) uses primary color
        val incomingStyle = PromptTextStyle()
            .setColor(primaryColor)
            .setBackgroundColor(primaryColor)
        val incomingSelfStyle = PromptTextStyle()
            .setColor(warningColor)
            .setBackgroundColor(warningColor)
        
        // Note: In getBubbleSpan, LEFT alignment uses outgoingBubble* styles
        outgoingBubbleTagStyle = incomingStyle
        outgoingBubbleSelfTagStyle = incomingSelfStyle
        
        // Outgoing bubble (RIGHT alignment in formatter) uses white color
        val outgoingStyle = PromptTextStyle()
            .setColor(whiteColor)
            .setBackgroundColor(whiteColor)
        val outgoingSelfStyle = PromptTextStyle()
            .setColor(warningColor)
            .setBackgroundColor(warningColor)
        
        // Note: In getBubbleSpan, RIGHT alignment uses incomingBubble* styles
        incomingBubbleTagStyle = outgoingStyle
        incomingBubbleSelfTagStyle = outgoingSelfStyle
        
        // Conversation styles use primary color (same as incoming)
        conversationTagStyle = incomingStyle
        conversationSelfTagStyle = incomingSelfStyle
    }

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
     *
     * This method applies the provided [CometChatMentionStyle] to all internal style properties,
     * converting it to [PromptTextStyle] instances for use with the formatter infrastructure.
     *
     * @param style The CometChatMentionStyle to apply across all mention contexts
     */
    fun setMentionStyle(style: CometChatMentionStyle) {
        val promptStyle = style.toPromptTextStyle()
        val selfPromptStyle = style.toSelfPromptTextStyle()
        
        // Apply to general styles
        tagStyle = promptStyle
        selfTagStyle = selfPromptStyle
        
        // Apply to bubble styles
        outgoingBubbleTagStyle = promptStyle
        outgoingBubbleSelfTagStyle = selfPromptStyle
        incomingBubbleTagStyle = promptStyle
        incomingBubbleSelfTagStyle = selfPromptStyle
        
        // Apply to conversation styles
        conversationTagStyle = promptStyle
        conversationSelfTagStyle = selfPromptStyle
    }

    /**
     * Sets the mention style specifically for incoming (left-aligned) message bubbles.
     *
     * This method applies the provided [CometChatMentionStyle] only to incoming bubble
     * style properties, leaving outgoing bubble, conversation, and composer styles unchanged.
     *
     * @param style The CometChatMentionStyle to apply for incoming message bubbles
     */
    fun setIncomingBubbleMentionStyle(style: CometChatMentionStyle) {
        // Note: In the formatter, LEFT alignment = outgoing style properties (sender's perspective)
        // and RIGHT alignment = incoming style properties. This matches the Java implementation.
        outgoingBubbleTagStyle = style.toPromptTextStyle()
        outgoingBubbleSelfTagStyle = style.toSelfPromptTextStyle()
    }

    /**
     * Sets the mention style specifically for outgoing (right-aligned) message bubbles.
     *
     * This method applies the provided [CometChatMentionStyle] only to outgoing bubble
     * style properties, leaving incoming bubble, conversation, and composer styles unchanged.
     *
     * @param style The CometChatMentionStyle to apply for outgoing message bubbles
     */
    fun setOutgoingBubbleMentionStyle(style: CometChatMentionStyle) {
        // Note: In the formatter, RIGHT alignment = incoming style properties (receiver's perspective)
        // and LEFT alignment = outgoing style properties. This matches the Java implementation.
        incomingBubbleTagStyle = style.toPromptTextStyle()
        incomingBubbleSelfTagStyle = style.toSelfPromptTextStyle()
    }

    /**
     * Sets the mention style specifically for conversation list previews.
     *
     * This method applies the provided [CometChatMentionStyle] only to conversation-related
     * style properties, leaving bubble and composer styles unchanged. Use this when you want
     * to customize how mentions appear in conversation list subtitles without affecting
     * other contexts.
     *
     * @param style The CometChatMentionStyle to apply for conversation previews
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
            groupMembersRequestBuilder = GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(requestLimit)
        }
    }

    private fun initializeUserRequestBuilder() {
        if (usersRequestBuilder == null) {
            usersRequestBuilder = UsersRequest.UsersRequestBuilder().setLimit(requestLimit)
        }
    }

    override fun search(context: Context, queryString: String?) {
        setShowLoadingIndicator(localSuggestionItemList.value.isEmpty() && queryString?.isEmpty() == true)
        if (getSelectedList().size < localMentionLimit) {
            if (queryString != null) {
                searchMentions(queryString)
            } else {
                localSuggestionItemList.value = emptyList()
                setSuggestionItemList(emptyList())
            }
        } else {
            localSuggestionItemList.value = emptyList()
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
        initializeUserRequestBuilder()
        localSuggestionItemList.value = emptyList()
        usersRequest = usersRequestBuilder?.setSearchKeyword(queryString)?.build()
        fetchUsers()
    }

    fun searchGroupMember(queryString: String) {
        initializeGroupMemberRequestBuilder()
        groupMembersRequest = groupMembersRequestBuilder?.setSearchKeyword(queryString)?.build()
        localSuggestionItemList.value = emptyList()
        fetchGroupMembers()
    }

    fun fetchUsers() {
        usersRequest?.fetchNext(object : CometChat.CallbackListener<List<User>>() {
            override fun onSuccess(users: List<User>) {
                val suggestions = users.map { user ->
                    SuggestionItem(
                        id = user.uid, name = user.name, leadingIconUrl = user.avatar,
                        status = user.status, promptText = "${getTrackingCharacter()}${user.name}",
                        underlyingText = "<${getTrackingCharacter()}uid:${user.uid}>",
                        data = try { user.toJson() } catch (e: Exception) { null },
                        promptTextStyle = tagStyle
                    )
                }
                val currentList = localSuggestionItemList.value.toMutableList()
                currentList.addAll(suggestions)
                localSuggestionItemList.value = currentList
                setSuggestionItemList(currentList)
            }
            override fun onError(e: CometChatException) {
                localSuggestionItemList.value = emptyList()
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
                        id = member.uid, name = member.name, leadingIconUrl = member.avatar,
                        status = member.status, promptText = "${getTrackingCharacter()}${member.name}",
                        underlyingText = "<${getTrackingCharacter()}uid:${member.uid}>",
                        data = try { member.toJson() } catch (e: Exception) { null },
                        promptTextStyle = if (isSelf && selfTagStyle != null) selfTagStyle else tagStyle
                    ))
                }
                if (!disableMentionAll && getGroup() != null) {
                    val metaData = JSONObject()
                    try { metaData.put("infoText", mentionAllInfoText) } catch (e: JSONException) { }
                    val containsMentionAll = localSuggestionItemList.value.any { it.id == mentionAllId }
                    val searchKeyword = groupMembersRequest?.searchKeyword ?: ""
                    if (!containsMentionAll && mentionAllId.lowercase().contains(searchKeyword.lowercase())) {
                        val group = getGroup()
                        val iconUrl = when {
                            group?.icon?.isNotEmpty() == true -> group.icon
                            group?.name?.isNotEmpty() == true -> group.name
                            else -> ""
                        }
                        suggestions.add(0, SuggestionItem(
                            id = mentionAllId, name = "${getTrackingCharacter()}$mentionAllLabelText",
                            leadingIconUrl = iconUrl, status = null,
                            promptText = "${getTrackingCharacter()}$mentionAllLabelText",
                            underlyingText = "<${getTrackingCharacter()}all:$mentionAllId>",
                            data = metaData, promptTextStyle = selfTagStyle
                        ))
                    }
                }
                val currentList = localSuggestionItemList.value.toMutableList()
                currentList.addAll(suggestions)
                localSuggestionItemList.value = currentList
                setSuggestionItemList(currentList)
            }
            override fun onError(e: CometChatException) {
                localSuggestionItemList.value = emptyList()
                setSuggestionItemList(emptyList())
            }
        })
    }

    override fun onItemClick(context: Context, suggestionItem: SuggestionItem, user: User?, group: Group?) { }

    override fun handlePreMessageSend(context: Context, baseMessage: BaseMessage) {
        val users = getMentionUsers()
        android.util.Log.d(TAG, "handlePreMessageSend: setting mentionedUsers, count=${users.size}, users=${users.map { it.uid }}")
        baseMessage.mentionedUsers = users
        android.util.Log.d(TAG, "handlePreMessageSend: after setting, mentionedUsers=${baseMessage.mentionedUsers?.map { it.uid }}")
    }

    fun getMentionUsers(): List<User> {
        val selectedList = getSelectedList()
        android.util.Log.d(TAG, "getMentionUsers: selectedList.size=${selectedList.size}")
        return selectedList.mapNotNull { item ->
            android.util.Log.d(TAG, "getMentionUsers: item.id=${item.id}, item.data=${item.data}")
            try {
                val user = item.data?.let { User.fromJson(it.toString()) }
                android.util.Log.d(TAG, "getMentionUsers: parsed user=${user?.uid}, name=${user?.name}")
                user
            } catch (e: Exception) {
                android.util.Log.e(TAG, "getMentionUsers: failed to parse user from item.data", e)
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
        setInfoText(if (getSelectedList().size >= localMentionLimit) "You can mention up to $localMentionLimit times at a time" else "")
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

    override fun prepareLeftMessageBubbleSpan(context: Context, baseMessage: BaseMessage, text: AnnotatedString): AnnotatedString =
        getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.LEFT, text)

    override fun prepareRightMessageBubbleSpan(context: Context, baseMessage: BaseMessage, text: AnnotatedString): AnnotatedString =
        getBubbleSpan(baseMessage, UIKitConstants.MessageBubbleAlignment.RIGHT, text)

    override fun prepareComposerSpan(context: Context, baseMessage: BaseMessage, text: AnnotatedString): AnnotatedString =
        getComposerSpan(baseMessage, text)

    override fun prepareConversationSpan(context: Context, baseMessage: BaseMessage, text: AnnotatedString): AnnotatedString =
        getConversationSpan(baseMessage, text)

    private fun getConversationSpan(baseMessage: BaseMessage, annotatedString: AnnotatedString): AnnotatedString {
        val originalText = annotatedString.text
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<MentionReplacement>()
        
        if (!disableMentionAll) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                replacements.add(MentionReplacement(
                    mentionAllMatcher.start(), mentionAllMatcher.end(),
                    "${getTrackingCharacter()}$mentionAllLabelText", conversationSelfTagStyle, true
                ))
            }
        }
        
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
        return buildStyledString(originalText, replacements)
    }

    private fun getComposerSpan(baseMessage: BaseMessage, annotatedString: AnnotatedString): AnnotatedString {
        val originalText = annotatedString.text
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<MentionReplacement>()
        
        if (!disableMentionAll && getGroup() != null) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                replacements.add(MentionReplacement(
                    mentionAllMatcher.start(), mentionAllMatcher.end(),
                    "${getTrackingCharacter()}$mentionAllId", selfTagStyle, true
                ))
            }
        }
        
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
                        if (isSelf && selfTagStyle != null) selfTagStyle else tagStyle, isSelf
                    ))
                }
            }
        }
        return buildStyledString(originalText, replacements)
    }

    private fun getBubbleSpan(baseMessage: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment, annotatedString: AnnotatedString): AnnotatedString {
        val originalText = annotatedString.text
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val replacements = mutableListOf<MentionReplacement>()
        
        android.util.Log.d(TAG, "getBubbleSpan: originalText='$originalText', messageId=${baseMessage.id}, muid=${baseMessage.muid}")
        android.util.Log.d(TAG, "getBubbleSpan: mentionedUsers=${baseMessage.mentionedUsers?.map { it.uid }}")
        
        if (!disableMentionAll) {
            val mentionAllMatcher = mentionAllPattern.matcher(originalText)
            while (mentionAllMatcher.find()) {
                replacements.add(MentionReplacement(
                    mentionAllMatcher.start(), mentionAllMatcher.end(),
                    "${getTrackingCharacter()}$mentionAllId", getMentionAllStyleForBubble(alignment), true
                ))
            }
        }
        
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
                    replacements.add(MentionReplacement(
                        matcher.start(), matcher.end(),
                        "${getTrackingCharacter()}${user.name}",
                        getMentionStyleForBubble(isSelf, alignment), isSelf
                    ))
                } else {
                    android.util.Log.d(TAG, "getBubbleSpan: user NOT found for userId=$userId in mentionedUsers")
                }
            }
        }
        android.util.Log.d(TAG, "getBubbleSpan: total replacements=${replacements.size}")
        return buildStyledString(originalText, replacements)
    }

    private fun buildStyledString(originalText: String, replacements: List<MentionReplacement>): AnnotatedString {
        if (replacements.isEmpty()) return AnnotatedString(originalText)
        
        // Sort replacements by start position (ascending) to process from left to right
        val sortedReplacements = replacements.sortedBy { it.start }
        
        return buildAnnotatedString {
            var lastEnd = 0
            
            for (replacement in sortedReplacements) {
                // Validate replacement bounds against original text
                if (replacement.start >= lastEnd && replacement.end <= originalText.length) {
                    // Append text before this replacement
                    if (replacement.start > lastEnd) {
                        append(originalText.substring(lastEnd, replacement.start))
                    }
                    
                    // Record the start position in the built string
                    val styleStart = this.length
                    
                    // Append the replacement text
                    append(replacement.replacement)
                    
                    // Record the end position in the built string
                    val styleEnd = this.length
                    
                    // Apply style if available
                    if (replacement.style != null) {
                        addStyle(
                            style = SpanStyle(
                                color = if (replacement.style.getColor() != 0) Color(replacement.style.getColor()) else Color.Unspecified,
                                fontWeight = FontWeight.Medium,
                                background = if (replacement.style.getBackgroundColor() != 0) Color(replacement.style.getBackgroundColor()).copy(alpha = 0.2f) else Color.Unspecified
                            ),
                            start = styleStart,
                            end = styleEnd
                        )
                    }
                    
                    lastEnd = replacement.end
                }
            }
            
            // Append any remaining text after the last replacement
            if (lastEnd < originalText.length) {
                append(originalText.substring(lastEnd))
            }
        }
    }

    private fun getMentionStyleForBubble(isSelf: Boolean, alignment: UIKitConstants.MessageBubbleAlignment): PromptTextStyle? =
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> if (isSelf) outgoingBubbleSelfTagStyle else outgoingBubbleTagStyle
            else -> if (isSelf) incomingBubbleSelfTagStyle else incomingBubbleTagStyle
        }

    private fun getMentionAllStyleForBubble(alignment: UIKitConstants.MessageBubbleAlignment): PromptTextStyle? =
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> outgoingBubbleSelfTagStyle
            else -> incomingBubbleSelfTagStyle
        }

    private fun generateMentionAllRegexPattern(id: String): String = "<${getTrackingCharacter()}all:$id>"

    private data class MentionReplacement(val start: Int, val end: Int, val replacement: String, val style: PromptTextStyle?, val isSelf: Boolean)

    // ==================== Compose Integration Methods ====================

    /**
     * Gets the mention limit for this formatter.
     *
     * @return The maximum number of mentions allowed per message
     */
    fun getMentionLimit(): Int = localMentionLimit

    /**
     * Gets the mention all ID used for @all mentions.
     *
     * @return The mention all ID (default: "all")
     */
    fun getMentionAllId(): String = mentionAllId

    /**
     * Gets the mention all label text.
     *
     * @return The display label for mention all (e.g., "Notify All")
     */
    fun getMentionAllLabelText(): String = mentionAllLabelText

    /**
     * Checks if mention all is disabled.
     *
     * @return true if mention all is disabled, false otherwise
     */
    fun isMentionAllDisabled(): Boolean = disableMentionAll

    /**
     * Converts a [PromptTextStyle] to a Compose [SpanStyle].
     *
     * This method bridges the formatter's internal styling to Compose's AnnotatedString styling,
     * applying the same 20% alpha to background colors as the View-based implementation.
     *
     * @param promptTextStyle The PromptTextStyle to convert
     * @return A SpanStyle with equivalent styling
     */
    fun toSpanStyle(promptTextStyle: PromptTextStyle?): SpanStyle {
        if (promptTextStyle == null) return SpanStyle()
        return SpanStyle(
            color = if (promptTextStyle.getColor() != 0) Color(promptTextStyle.getColor()) else Color.Unspecified,
            fontWeight = FontWeight.Medium,
            background = if (promptTextStyle.getBackgroundColor() != 0) 
                Color(promptTextStyle.getBackgroundColor()).copy(alpha = 0.2f) 
            else Color.Unspecified
        )
    }

    /**
     * Gets the default [SpanStyle] for mentions in the composer.
     *
     * This style is used for regular user mentions (not self-mentions) in the message composer.
     *
     * @return The SpanStyle for composer mentions
     */
    fun getComposerMentionSpanStyle(): SpanStyle = toSpanStyle(tagStyle)

    /**
     * Gets the [SpanStyle] for self-mentions in the composer.
     *
     * This style is used when the logged-in user mentions themselves in the message composer.
     *
     * @return The SpanStyle for self-mentions in the composer
     */
    fun getComposerSelfMentionSpanStyle(): SpanStyle = toSpanStyle(selfTagStyle)

    /**
     * Gets the appropriate [SpanStyle] for a suggestion item based on whether it's a self-mention.
     *
     * @param suggestionItem The suggestion item to get the style for
     * @return The appropriate SpanStyle for the suggestion item
     */
    fun getSpanStyleForSuggestionItem(suggestionItem: SuggestionItem): SpanStyle {
        val loggedInUser = try { CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val isSelf = suggestionItem.id == loggedInUser?.uid || suggestionItem.id == mentionAllId
        return if (isSelf) getComposerSelfMentionSpanStyle() else getComposerMentionSpanStyle()
    }

    /**
     * Checks if the mention limit has been reached.
     *
     * @return true if the number of selected mentions equals or exceeds the limit
     */
    fun isMentionLimitReached(): Boolean = getSelectedList().size >= localMentionLimit

    /**
     * Gets the info message to display when the mention limit is reached.
     *
     * @return The info message string, or empty string if limit not reached
     */
    fun getMentionLimitInfoMessage(): String {
        return if (isMentionLimitReached()) {
            "You can mention up to $localMentionLimit times at a time"
        } else ""
    }

    /**
     * Clears the local suggestion list and resets the formatter state.
     *
     * Call this when the composer is cleared or when starting a new message.
     */
    fun clearSuggestions() {
        localSuggestionItemList.value = emptyList()
        setSuggestionItemList(emptyList())
    }

    /**
     * Gets the current tag style for regular mentions.
     *
     * @return The PromptTextStyle for regular mentions, or null if not set
     */
    fun getTagStyle(): PromptTextStyle? = tagStyle

    /**
     * Gets the current self tag style for self-mentions.
     *
     * @return The PromptTextStyle for self-mentions, or null if not set
     */
    fun getSelfTagStyle(): PromptTextStyle? = selfTagStyle
}
