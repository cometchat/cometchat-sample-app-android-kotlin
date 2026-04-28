package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatRichTextFormatter
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.SearchFilter
import com.cometchat.uikit.core.constants.SearchScope
import com.cometchat.uikit.core.factory.CometChatSearchViewModelFactory
import com.cometchat.uikit.core.state.SearchUIState
import com.cometchat.uikit.core.viewmodel.CometChatSearchViewModel

/**
 * Default search filters in the order matching the Kotlin implementation:
 * Unread, Groups, Photos, Videos, Links, Documents, Audio
 */
private val defaultSearchFilters = listOf(
    SearchFilter.UNREAD,
    SearchFilter.GROUPS,
    SearchFilter.PHOTOS,
    SearchFilter.VIDEOS,
    SearchFilter.LINKS,
    SearchFilter.DOCUMENTS,
    SearchFilter.AUDIO
)

/**
 * CometChatSearch is a composable that provides search functionality across
 * conversations and messages with support for filter chips, debounced search,
 * pagination, and multiple view states.
 */
@Composable
fun CometChatSearch(
    modifier: Modifier = Modifier,
    viewModel: CometChatSearchViewModel? = null,
    style: CometChatSearchStyle = CometChatSearchStyle.default(),
    searchScopes: List<SearchScope> = listOf(SearchScope.MESSAGES, SearchScope.CONVERSATIONS),
    uid: String? = null,
    guid: String? = null,
    searchFilters: List<SearchFilter> = defaultSearchFilters,
    hideSearchBar: Boolean = false,
    hideFilterChips: Boolean = false,
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    hideInitialState: Boolean = false,
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    initialView: (@Composable () -> Unit)? = null,
    conversationItemView: (@Composable (Conversation) -> Unit)? = null,
    conversationLeadingView: (@Composable (Conversation) -> Unit)? = null,
    conversationTitleView: (@Composable (Conversation) -> Unit)? = null,
    conversationSubtitleView: (@Composable (Conversation) -> Unit)? = null,
    conversationTrailingView: (@Composable (Conversation) -> Unit)? = null,
    textMessageItemView: (@Composable (TextMessage) -> Unit)? = null,
    imageMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    videoMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    audioMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    documentMessageItemView: (@Composable (MediaMessage) -> Unit)? = null,
    linkMessageItemView: (@Composable (TextMessage) -> Unit)? = null,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    onBackPress: (() -> Unit)? = null,
    onConversationClick: ((Conversation) -> Unit)? = null,
    onMessageClick: ((BaseMessage) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onLoadConversations: ((List<Conversation>) -> Unit)? = null,
    onLoadMessages: ((List<BaseMessage>) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Initialize default text formatters if none provided (matching reference Java behavior)
    val effectiveTextFormatters = remember(textFormatters) {
        if (textFormatters.isEmpty()) {
            try {
                val richTextFormatter = CometChatRichTextFormatter()
                val mentionsFormatter = CometChatMentionsFormatter(context)
                listOf<CometChatTextFormatter>(richTextFormatter, mentionsFormatter)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            textFormatters
        }
    }

    val searchViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CometChatSearchViewModelFactory()
    )

    var searchText by rememberSaveable { mutableStateOf("") }

    val uiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val conversations by searchViewModel.conversations.collectAsStateWithLifecycle()
    val messages by searchViewModel.messages.collectAsStateWithLifecycle()
    val hasMoreConversations by searchViewModel.hasMoreConversations.collectAsStateWithLifecycle()
    val hasMoreMessages by searchViewModel.hasMoreMessages.collectAsStateWithLifecycle()
    val selectedFilters by searchViewModel.selectedFilters.collectAsStateWithLifecycle()
    val visibleFilters by searchViewModel.visibleFilters.collectAsStateWithLifecycle()

    LaunchedEffect(searchScopes) {
        searchViewModel.setSearchScopes(searchScopes)
    }

    LaunchedEffect(uid) {
        searchViewModel.setUid(uid)
    }

    LaunchedEffect(guid) {
        searchViewModel.setGuid(guid)
    }

    LaunchedEffect(uiState, conversations, messages) {
        when (uiState) {
            is SearchUIState.Content -> {
                onLoadConversations?.invoke(conversations)
                onLoadMessages?.invoke(messages)
            }
            is SearchUIState.Empty -> onEmpty?.invoke()
            is SearchUIState.Error -> {
                val error = (uiState as SearchUIState.Error).exception
                onError?.invoke(error)
            }
            else -> { }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
    ) {
        if (!hideSearchBar) {
            SearchBar(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    searchViewModel.searchConversationsAndMessages(newText, selectedFilters)
                },
                style = style,
                placeholder = context.getString(R.string.cometchat_search),
                onBackPress = onBackPress,
                onSearch = { query ->
                    searchViewModel.searchConversationsAndMessages(query, selectedFilters)
                },
                onClear = {
                    searchText = ""
                    searchViewModel.searchConversationsAndMessages("", selectedFilters)
                }
            )
        }

        if (!hideFilterChips && searchFilters.isNotEmpty()) {
            // Filter the user-specified searchFilters list by what's visible,
            // preserving the user-specified order (not enum declaration order)
            val orderedVisibleFilters = searchFilters.filter { it in visibleFilters }
            SearchFilterChips(
                filters = orderedVisibleFilters,
                selectedFilters = selectedFilters,
                onFilterToggle = { filter ->
                    searchViewModel.toggleFilter(filter)
                },
                style = style
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiState) {
                is SearchUIState.Initial -> {
                    if (!hideInitialState) {
                        initialView?.invoke() ?: CometChatEmptyState(
                            style = style.initialStateStyle,
                            title = context.getString(R.string.cometchat_search_initial_title),
                            subtitle = context.getString(R.string.cometchat_search_initial_subtitle)
                        )
                    }
                }

                is SearchUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: CometChatLoadingState(
                            style = style.loadingStateStyle
                        )
                    }
                }

                is SearchUIState.Empty -> {
                    if (!hideEmptyState) {
                        emptyView?.invoke() ?: CometChatEmptyState(
                            style = style.emptyStateStyle,
                            title = context.getString(R.string.cometchat_search_empty_title),
                            subtitle = context.getString(R.string.cometchat_search_empty_subtitle)
                        )
                    }
                }

                is SearchUIState.Error -> {
                    if (!hideErrorState) {
                        errorView?.invoke {
                            searchViewModel.searchConversationsAndMessages(searchText, selectedFilters)
                        } ?: CometChatErrorState(
                            style = style.errorStateStyle,
                            title = context.getString(R.string.cometchat_error_conversations_title),
                            subtitle = context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                            onRetry = {
                                searchViewModel.searchConversationsAndMessages(searchText, selectedFilters)
                            }
                        )
                    }
                }

                is SearchUIState.Content -> {
                    // Match reference: hide section headers when only filter chips are active (no search text)
                    val hasFilterChipsOnly = selectedFilters.isNotEmpty() && searchText.isEmpty()

                    SearchListContent(
                        conversations = conversations,
                        messages = messages,
                        hasMoreConversations = hasMoreConversations,
                        hasMoreMessages = hasMoreMessages,
                        onConversationClick = { conversation ->
                            onConversationClick?.invoke(conversation)
                        },
                        onMessageClick = { message ->
                            onMessageClick?.invoke(message)
                        },
                        onLoadMoreConversations = {
                            searchViewModel.fetchMoreConversations()
                        },
                        onLoadMoreMessages = {
                            searchViewModel.fetchMoreMessages()
                        },
                        style = style,
                        showSectionHeaders = !hasFilterChipsOnly,
                        // Match Java reference: show See More when search text is not empty AND no active filters
                        showSeeMore = searchText.isNotEmpty() && selectedFilters.isEmpty(),
                        conversationItemView = conversationItemView,
                        conversationLeadingView = conversationLeadingView,
                        conversationTitleView = conversationTitleView,
                        conversationSubtitleView = conversationSubtitleView,
                        conversationTrailingView = conversationTrailingView,
                        textFormatters = effectiveTextFormatters,
                        uid = uid,
                        guid = guid,
                        textMessageItemView = textMessageItemView,
                        imageMessageItemView = imageMessageItemView,
                        videoMessageItemView = videoMessageItemView,
                        audioMessageItemView = audioMessageItemView,
                        documentMessageItemView = documentMessageItemView,
                        linkMessageItemView = linkMessageItemView
                    )
                }
            }
        }
    }
}


/**
 * Internal stateless composable for preview purposes.
 * This allows previewing the UI without ViewModel dependencies.
 */
@Composable
private fun CometChatSearchContent(
    modifier: Modifier = Modifier,
    style: CometChatSearchStyle = CometChatSearchStyle.default(),
    searchFilters: List<SearchFilter> = defaultSearchFilters,
    selectedFilters: Set<SearchFilter> = emptySet(),
    searchText: String = "",
    uiState: SearchUIState = SearchUIState.Initial,
    hideSearchBar: Boolean = false,
    hideFilterChips: Boolean = false,
    onSearchTextChange: (String) -> Unit = {},
    onFilterToggle: (SearchFilter) -> Unit = {},
    onBackPress: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
    ) {
        if (!hideSearchBar) {
            SearchBar(
                value = searchText,
                onValueChange = onSearchTextChange,
                style = style,
                placeholder = "Search",
                onBackPress = onBackPress,
                onSearch = {},
                onClear = { onSearchTextChange("") }
            )
        }

        if (!hideFilterChips && searchFilters.isNotEmpty()) {
            SearchFilterChips(
                filters = searchFilters,
                selectedFilters = selectedFilters,
                onFilterToggle = onFilterToggle,
                style = style
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiState) {
                is SearchUIState.Initial -> {
                    CometChatEmptyState(
                        style = style.initialStateStyle,
                        title = "Start Your Search",
                        subtitle = "Search for conversations or messages by typing a keyword above."
                    )
                }
                is SearchUIState.Loading -> {
                    CometChatLoadingState(style = style.loadingStateStyle)
                }
                is SearchUIState.Empty -> {
                    CometChatEmptyState(
                        style = style.emptyStateStyle,
                        title = "No Results Found",
                        subtitle = "Try searching with different keywords."
                    )
                }
                is SearchUIState.Error -> {
                    CometChatErrorState(
                        style = style.errorStateStyle,
                        title = "Something went wrong",
                        subtitle = "Please try again.",
                        onRetry = {}
                    )
                }
                is SearchUIState.Content -> {
                    // Content would show conversations/messages list
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "CometChatSearch - Initial State")
@Composable
private fun CometChatSearchInitialPreview() {
    CometChatTheme {
        CometChatSearchContent(
            uiState = SearchUIState.Initial
        )
    }
}

@Preview(showBackground = true, name = "CometChatSearch - Loading State")
@Composable
private fun CometChatSearchLoadingPreview() {
    CometChatTheme {
        CometChatSearchContent(
            uiState = SearchUIState.Loading
        )
    }
}

@Preview(showBackground = true, name = "CometChatSearch - Empty State")
@Composable
private fun CometChatSearchEmptyPreview() {
    CometChatTheme {
        CometChatSearchContent(
            searchText = "xyz",
            uiState = SearchUIState.Empty
        )
    }
}

@Preview(showBackground = true, name = "CometChatSearch - With Selected Filters")
@Composable
private fun CometChatSearchWithFiltersPreview() {
    CometChatTheme {
        CometChatSearchContent(
            selectedFilters = setOf(SearchFilter.UNREAD),
            uiState = SearchUIState.Initial
        )
    }
}
