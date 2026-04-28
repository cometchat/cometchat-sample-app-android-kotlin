package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter

private const val LOAD_MORE_THRESHOLD = 5

/**
 * A composable that displays the search results content with conversations and messages sections.
 */
@Composable
fun SearchListContent(
    conversations: List<Conversation>,
    messages: List<BaseMessage>,
    hasMoreConversations: Boolean,
    hasMoreMessages: Boolean,
    onConversationClick: (Conversation) -> Unit,
    onMessageClick: (BaseMessage) -> Unit,
    onLoadMoreConversations: () -> Unit,
    onLoadMoreMessages: () -> Unit,
    modifier: Modifier = Modifier,
    style: CometChatSearchStyle = CometChatSearchStyle.default(),
    showSectionHeaders: Boolean = true,
    showSeeMore: Boolean = false,
    conversationItemView: (@Composable (Conversation) -> Unit)? = null,
    conversationLeadingView: (@Composable (Conversation) -> Unit)? = null,
    conversationTitleView: (@Composable (Conversation) -> Unit)? = null,
    conversationSubtitleView: (@Composable (Conversation) -> Unit)? = null,
    conversationTrailingView: (@Composable (Conversation) -> Unit)? = null,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    uid: String? = null,
    guid: String? = null,
    textMessageItemView: (@Composable (TextMessage) -> Unit)? = null,
    imageMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    videoMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    audioMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    documentMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    linkMessageItemView: (@Composable (TextMessage) -> Unit)? = null
) {
    val listState = rememberLazyListState()

    val totalItems = conversations.size + messages.size +
            (if (conversations.isNotEmpty()) 1 else 0) +
            (if (messages.isNotEmpty()) 1 else 0)

    val shouldLoadMore by remember {
        derivedStateOf {
            // Only auto-paginate when See More is NOT shown (filter-based search with limit=15)
            // When showSeeMore is true (text-only search with limit=3), pagination is manual via See More click
            if (showSeeMore) {
                false
            } else {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= totalItems - LOAD_MORE_THRESHOLD
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            if (hasMoreMessages) {
                onLoadMoreMessages()
            }
            if (hasMoreConversations) {
                onLoadMoreConversations()
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .semantics {
                contentDescription = "Search results: ${conversations.size} conversations, ${messages.size} messages"
                collectionInfo = CollectionInfo(
                    rowCount = totalItems,
                    columnCount = 1
                )
            }
    ) {
        if (conversations.isNotEmpty()) {
            if (showSectionHeaders) {
                item(key = "conversations_header") {
                    SectionHeader(
                        title = "Conversations",
                        style = style
                    )
                }
            }

            items(
                items = conversations,
                key = { conversation -> "conversation_${conversation.conversationId}" }
            ) { conversation ->
                if (conversationItemView != null) {
                    conversationItemView(conversation)
                } else {
                    SearchConversationItem(
                        conversation = conversation,
                        onClick = onConversationClick,
                        style = style.conversationItemStyle,
                        textFormatters = textFormatters,
                        leadingView = conversationLeadingView,
                        titleView = conversationTitleView,
                        subtitleView = conversationSubtitleView,
                        trailingView = conversationTrailingView
                    )
                }
            }

            // See More for conversations (matching Java reference)
            if (showSeeMore && hasMoreConversations) {
                item(key = "conversations_see_more") {
                    SeeMoreText(
                        onClick = onLoadMoreConversations,
                        style = style
                    )
                }
            }
        }

        if (messages.isNotEmpty()) {
            if (showSectionHeaders) {
                item(key = "messages_header") {
                    SectionHeader(
                        title = "Messages",
                        style = style
                    )
                }
            }

            items(
                items = messages,
                key = { message -> "message_${message.id}" }
            ) { message ->
                SearchMessageItem(
                    message = message,
                    onClick = onMessageClick,
                    style = style.messageItemStyle,
                    textFormatters = textFormatters,
                    uid = uid,
                    guid = guid,
                    textMessageView = textMessageItemView,
                    imageMessageView = imageMessageItemView,
                    videoMessageView = videoMessageItemView,
                    audioMessageView = audioMessageItemView,
                    documentMessageView = documentMessageItemView,
                    linkMessageView = linkMessageItemView
                )
            }

            // See More for messages (matching Java reference)
            if (showSeeMore && hasMoreMessages) {
                item(key = "messages_see_more") {
                    SeeMoreText(
                        onClick = onLoadMoreMessages,
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
private fun SeeMoreText(
    onClick: () -> Unit,
    style: CometChatSearchStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "See More",
            color = style.seeMoreTextColor,
            style = style.seeMoreTextStyle,
            modifier = Modifier
                .clickable { onClick() }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    style: CometChatSearchStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.sectionHeaderBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = style.sectionHeaderTextColor,
            style = style.sectionHeaderTextStyle
        )
    }
}


