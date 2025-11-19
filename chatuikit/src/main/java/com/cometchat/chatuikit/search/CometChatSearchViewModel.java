package com.cometchat.chatuikit.search;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.ConversationsRequest;
import com.cometchat.chat.core.MessagesRequest;
import com.cometchat.chat.enums.AttachmentType;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chatuikit.shared.constants.SearchScope;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import java.util.ArrayList;
import java.util.List;

public class CometChatSearchViewModel extends ViewModel {
    private static final String TAG = CometChatSearchViewModel.class.getSimpleName();
    public static int DEFAULT_LIMIT = 15;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    private final MutableLiveData<CometChatException> cometchatException;

    private final MutableLiveData<UIKitConstants.States> states;
    private final MutableLiveData<Boolean> isSearching;
    private final List<Conversation> conversationList = new ArrayList<>();
    private final List<BaseMessage> messageList;
    private final MutableLiveData<List<Conversation>> mutableConversationList;
    private final MutableLiveData<List<BaseMessage>> mutableMessageList;
    private final MutableLiveData<Boolean> mutableHasMorePreviousMessages;
    private final MutableLiveData<Boolean> mutableHasMoreConversations;
    private final MutableLiveData<Integer> mutableMessagesRangeChanged;
    private final MutableLiveData<Integer> mutableConversationsRangeChanged;
    private List<SearchScope> searchScopes;

    private MessagesRequest messagesRequest;
    private ConversationsRequest conversationsRequest;

    private List<UIKitConstants.SearchFilter> previousFilter;

    private ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder;
    private MessagesRequest.MessagesRequestBuilder messagesRequestBuilder;

    private boolean hasMorePreviousMessages = true;
    private boolean hasMoreConversation = true;
    private String uid;
    private String guid;

    // Request state tracking
    private boolean isConversationRequestPending = false;
    private boolean isMessageRequestPending = false;
    private boolean conversationRequestFailed = false;
    private boolean messageRequestFailed = false;

    public CometChatSearchViewModel() {
        states = new MutableLiveData<>();
        mutableConversationList = new MutableLiveData<>();
        mutableMessageList = new MutableLiveData<>();
        isSearching = new MutableLiveData<>();
        mutableHasMorePreviousMessages = new MutableLiveData<>();
        mutableHasMoreConversations = new MutableLiveData<>();
        mutableMessagesRangeChanged = new MutableLiveData<>();
        messageList = new ArrayList<>();
        mutableConversationsRangeChanged = new MutableLiveData<>();
        cometchatException = new MutableLiveData<>();
    }

    /**
     * Resets the request state tracking variables
     */
    private void resetRequestStates() {
        isConversationRequestPending = false;
        isMessageRequestPending = false;
        conversationRequestFailed = false;
        messageRequestFailed = false;
    }

    /**
     * Checks if both requests have completed (either succeeded or failed)
     */
    private boolean areAllRequestsCompleted() {
        return !isConversationRequestPending && !isMessageRequestPending;
    }

    /**
     * Determines and sets the final state based on request results
     */
    private void updateFinalStateIfCompleted() {
        if (!areAllRequestsCompleted()) {
            return;
        }

        if (conversationRequestFailed && messageRequestFailed) {
            states.setValue(UIKitConstants.States.ERROR);
        } else if (conversationList.isEmpty() && messageList.isEmpty()) {
            states.setValue(UIKitConstants.States.EMPTY);
        } else {
            states.setValue(UIKitConstants.States.NON_EMPTY);
        }

        isSearching.setValue(false);
    }

    public void searchConversationsAndMessages(String searchText, List<UIKitConstants.SearchFilter> filters) {
        if (searchText.isEmpty() && noFilters(filters)) {
            return;
        }

        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }

        // Store the search context for validation
        final String currentSearchText = searchText;
        final List<UIKitConstants.SearchFilter> currentFilters = new ArrayList<>(filters);

        debounceRunnable = () -> {
            if (searchScopes == null) searchScopes = List.of(SearchScope.MESSAGES, SearchScope.CONVERSATIONS);

            states.setValue(UIKitConstants.States.LOADING);
            if (previousFilter != null || !searchText.isEmpty()) {
                messagesRequest = null;
                conversationsRequest = null;
                clear();
            }
            isSearching.setValue(true);
            UIKitConstants.SearchMode searchMode = getSearchMode(currentSearchText, currentFilters, searchScopes);
            performSearch(currentSearchText, currentFilters, searchMode);
        };

        boolean isFilterChange = previousFilter != null && !previousFilter.equals(filters);

        if (!searchText.isEmpty() && !isFilterChange) {
            debounceHandler.postDelayed(debounceRunnable, 450);
        } else {
            debounceHandler.post(debounceRunnable);
        }
    }

    public void performSearch(String searchText, List<UIKitConstants.SearchFilter> filters, UIKitConstants.SearchMode searchMode) {
        resetRequestStates();
        switch (searchMode) {
            case BOTH:
                initializeConversationRequestBuilder(searchText.trim(), filters);
                initializeMessageRequestBuilder(searchText.trim(), filters);
                handleConversationAndMessagesSearch();
                break;
            case CONVERSATIONS:
                initializeConversationRequestBuilder(searchText.trim(), filters);
                handleConversationsSearch();
                break;
            case MESSAGES:
                initializeMessageRequestBuilder(searchText.trim(), filters);
                handleMessagesSearch();
                break;
            case NONE:
            default:
                clear();
                states.setValue(UIKitConstants.States.EMPTY);
                isSearching.setValue(false);
                break;
        }
    }

    private void handleMessagesSearch() {
        clearConversations();
        isMessageRequestPending = true;
        fetchMessages();
    }

    private void handleConversationsSearch() {
        clearMessages();
        isConversationRequestPending = true;
        fetchConversations();
    }

    private void handleConversationAndMessagesSearch() {
        isConversationRequestPending = true;
        isMessageRequestPending = true;
        fetchConversations();
        fetchMessages();
    }

    private UIKitConstants.SearchMode getSearchMode(String searchText, List<UIKitConstants.SearchFilter> filters, List<SearchScope> scopes) {
        List<String> messageTypes = getMessageTypesFromFilters(filters);
        List<String> conversationTypes = getConversationTypesFromFilters(filters);

        boolean hasConversationFilters = !conversationTypes.isEmpty();
        boolean hasMessageFilters = !messageTypes.isEmpty();
        boolean hasSearchText = searchText != null && !searchText.isEmpty();

        boolean scopeConversation = scopes.contains(SearchScope.CONVERSATIONS);
        boolean scopeMessage = scopes.contains(SearchScope.MESSAGES);

        if (hasSearchText && !hasConversationFilters && !hasMessageFilters) {
            if (scopeConversation && scopeMessage && (guid == null && uid == null))
                return UIKitConstants.SearchMode.BOTH;
            else if (scopeConversation && (guid == null && uid == null))
                return UIKitConstants.SearchMode.CONVERSATIONS;
            else if (guid != null || uid != null)
                return UIKitConstants.SearchMode.MESSAGES;
            else if (scopeMessage)
                return UIKitConstants.SearchMode.MESSAGES;
        } else if (!hasSearchText && hasConversationFilters && !hasMessageFilters) {
            return UIKitConstants.SearchMode.CONVERSATIONS;
        } else if (!hasSearchText && !hasConversationFilters && hasMessageFilters) {
            return UIKitConstants.SearchMode.MESSAGES;
        } else if (hasSearchText && hasConversationFilters && !hasMessageFilters) {
            return UIKitConstants.SearchMode.CONVERSATIONS;
        } else if (hasSearchText && !hasConversationFilters) {
            return UIKitConstants.SearchMode.MESSAGES;
        }

        return UIKitConstants.SearchMode.NONE;
    }

    private boolean noFilters(List<UIKitConstants.SearchFilter> filters) {
        return filters == null || filters.isEmpty();
    }

    private void initializeMessageRequestBuilder(String searchText, List<UIKitConstants.SearchFilter> filters) {
        if (messagesRequestBuilder == null) {
            messagesRequestBuilder = new MessagesRequest.MessagesRequestBuilder()
                    .setTypes(List.of(UIKitConstants.MessageType.AUDIO,
                            UIKitConstants.MessageType.FILE,
                            UIKitConstants.MessageType.IMAGE,
                            UIKitConstants.MessageType.VIDEO,
                            UIKitConstants.MessageType.TEXT))
                    .setLimit(DEFAULT_LIMIT)
                    .hideDeletedMessages(true);
        }

        configureMessageRequestForSearch(searchText, filters);

        messagesRequest = messagesRequestBuilder.build();
    }

    private void configureMessageRequestForSearch(String searchText, List<UIKitConstants.SearchFilter> filters) {
        int limit = filters.isEmpty() && !searchText.isEmpty() ? 3 : DEFAULT_LIMIT;
        messagesRequestBuilder.setLimit(limit);

        if (uid != null && !uid.isEmpty()) {
            messagesRequestBuilder.setUID(uid);
        }

        if (guid != null && !guid.isEmpty()) {
            messagesRequestBuilder.setGUID(guid);
        }

        if (!searchText.isEmpty() && searchScopes != null && searchScopes.contains(SearchScope.MESSAGES)) {
            messagesRequestBuilder.setSearchKeyword(searchText);
        } else {
            messagesRequestBuilder.setSearchKeyword(null);
        }

        applyMessageFilters(messagesRequestBuilder, filters);
    }


    private void initializeConversationRequestBuilder(String searchText, List<UIKitConstants.SearchFilter> filters) {
        if (conversationsRequestBuilder == null) {
            conversationsRequestBuilder = new ConversationsRequest.ConversationsRequestBuilder()
                    .setLimit(DEFAULT_LIMIT);
        }

        configureConversationRequestForSearch(searchText, filters);

        conversationsRequest = conversationsRequestBuilder.build();
    }

    private void configureConversationRequestForSearch(String searchText, List<UIKitConstants.SearchFilter> filters) {
        int limit = filters.isEmpty() && !searchText.isEmpty() ? 3 : DEFAULT_LIMIT;
        conversationsRequestBuilder.setLimit(limit);

        if (!searchText.isEmpty() && searchScopes != null && searchScopes.contains(SearchScope.CONVERSATIONS)) {
            conversationsRequestBuilder.setSearchKeyword(searchText);
        } else {
            conversationsRequestBuilder.setSearchKeyword(null);
        }

        applyConversationFilters(conversationsRequestBuilder, filters);
    }

    private List<String> getConversationTypesFromFilters(List<UIKitConstants.SearchFilter> filters) {
        List<String> conversationTypes = new ArrayList<>();
        for (UIKitConstants.SearchFilter filter : filters) {
            switch (filter) {
                case UNREAD:
                    conversationTypes.add(UIKitConstants.SearchFilter.UNREAD.getValue());
                    break;
                case GROUPS:
                    conversationTypes.add(UIKitConstants.SearchFilter.GROUPS.getValue());
                    break;
                default:
                    break;
            }
        }
        return conversationTypes;
    }

    private List<String> getMessageTypesFromFilters(List<UIKitConstants.SearchFilter> filters) {
        List<String> messageTypes = new ArrayList<>();
        for (UIKitConstants.SearchFilter filter : filters) {
            switch (filter) {
                case PHOTOS:
                    messageTypes.add(UIKitConstants.MessageType.IMAGE);
                    break;
                case VIDEOS:
                    messageTypes.add(UIKitConstants.MessageType.VIDEO);
                    break;
                case DOCUMENTS:
                    messageTypes.add(UIKitConstants.MessageType.FILE);
                    break;
                case AUDIO:
                    messageTypes.add(UIKitConstants.MessageType.AUDIO);
                    break;
                case LINKS:
                    messageTypes.add(UIKitConstants.MessageType.TEXT);
                    break;
                default:
                    break;
            }
        }
        return messageTypes;
    }

    private void applyConversationFilters(ConversationsRequest.ConversationsRequestBuilder builder, List<UIKitConstants.SearchFilter> filters) {
        previousFilter = filters;
        builder.setUnread(filters.contains(UIKitConstants.SearchFilter.UNREAD));

        if (filters.contains(UIKitConstants.SearchFilter.GROUPS)) {
            builder.setConversationType(CometChatConstants.CONVERSATION_TYPE_GROUP);
        } else {
            builder.setConversationType(null);
        }
    }

    private void applyMessageFilters(MessagesRequest.MessagesRequestBuilder builder, List<UIKitConstants.SearchFilter> filters) {
        previousFilter = filters;
        ArrayList<AttachmentType> attachmentTypes = new ArrayList<>();
        if (filters.contains(UIKitConstants.SearchFilter.PHOTOS))
            attachmentTypes.add(AttachmentType.IMAGE);
        if (filters.contains(UIKitConstants.SearchFilter.VIDEOS))
            attachmentTypes.add(AttachmentType.VIDEO);
        if (filters.contains(UIKitConstants.SearchFilter.DOCUMENTS))
            attachmentTypes.add(AttachmentType.FILE);
        if (filters.contains(UIKitConstants.SearchFilter.AUDIO))
            attachmentTypes.add(AttachmentType.AUDIO);
        if (!attachmentTypes.isEmpty()) {
            builder.setAttachmentTypes(attachmentTypes);
            builder.hasLinks(false);
        } else {
            builder.setAttachmentTypes(null);
        }

        if (filters.contains(UIKitConstants.SearchFilter.LINKS)) {
            builder.setAttachmentTypes(null);
            builder.hasLinks(true);
        } else {
            builder.hasLinks(false);
        }

        List<String> filteredMessageTypes = getMessageTypesFromFilters(filters);
        if (!filteredMessageTypes.isEmpty()) {
            builder.setTypes(filteredMessageTypes);
        } else {
            builder.setTypes(null);
        }
    }

    public void fetchMessages() {
        if (messagesRequest != null) {
            messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
                @Override public void onSuccess(List<BaseMessage> messages) {
                    hasMorePreviousMessages = !messages.isEmpty();
                    if (messageList.isEmpty()) {
                        for (int i = messages.size() - 1; i >= 0; i--) {
                            messageList.add(messages.get(i));
                        }
                        mutableMessageList.setValue(messageList);
                    } else {
                        for (int i = messages.size() - 1; i >= 0; i--) {
                            messageList.add(messages.get(i));
                        }
                        mutableMessagesRangeChanged.setValue(messages.size());
                    }
                    mutableHasMorePreviousMessages.setValue(hasMorePreviousMessages);

                    isMessageRequestPending = false;
                    messageRequestFailed = false;
                    updateFinalStateIfCompleted();
                }
                @Override public void onError(CometChatException e) {
                    isMessageRequestPending = false;
                    messageRequestFailed = true;
                    cometchatException.setValue(e);
                    updateFinalStateIfCompleted();
                }
            });
        }
    }

    public void fetchConversations() {
        if (conversationsRequest != null) {
            conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
                @Override public void onSuccess(List<Conversation> conversations) {
                    hasMoreConversation = !conversations.isEmpty();
                    if (conversationList.isEmpty()) {
                        conversationList.addAll(conversations);
                        mutableConversationList.setValue(conversationList);
                    } else {
                        conversationList.addAll(conversations);
                        mutableConversationsRangeChanged.setValue(conversations.size());
                    }
                    mutableHasMoreConversations.setValue(hasMoreConversation);

                    isConversationRequestPending = false;
                    conversationRequestFailed = false;
                    updateFinalStateIfCompleted();
                }
                @Override public void onError(CometChatException e) {
                    isConversationRequestPending = false;
                    conversationRequestFailed = true;
                    cometchatException.setValue(e);
                    updateFinalStateIfCompleted();
                }
            });
        }
    }

    public void setConversationsRequestBuilder(ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder) {
        if (conversationsRequestBuilder != null) {
            this.conversationsRequestBuilder = conversationsRequestBuilder;
        }
    }

    public void setMessagesRequestBuilder(MessagesRequest.MessagesRequestBuilder builder) {
        if (builder != null) {
            this.messagesRequestBuilder = builder;
        }
    }

    public void setSearchScopes(List<SearchScope> searchScopes) {
        if (searchScopes != null) {
            this.searchScopes = searchScopes;
        }
    }

    private UIKitConstants.States checkIsConversationListEmpty(List<Conversation> conversations) {
        if (conversations.isEmpty()) return UIKitConstants.States.EMPTY;
        return UIKitConstants.States.NON_EMPTY;
    }

    public UIKitConstants.States checkIsMessageListEmpty(List<BaseMessage> baseMessageList) {
        if (baseMessageList.isEmpty()) return UIKitConstants.States.EMPTY;
        return UIKitConstants.States.NON_EMPTY;
    }

    private void clearConversations() {
        conversationList.clear();
        mutableConversationList.setValue(conversationList);
    }

    private void clearMessages() {
        messageList.clear();
        mutableMessageList.setValue(messageList);
    }

    public void clear() {
        clearConversations();
        clearMessages();
    }

    public MutableLiveData<List<Conversation>> getMutableConversationList() {
        return mutableConversationList;
    }

    public MutableLiveData<UIKitConstants.States> getStates() {
        return states;
    }

    public LiveData<List<BaseMessage>> getMutableMessageList() {
        return mutableMessageList;
    }

    public MutableLiveData<Boolean> getMutableLiveDataIsSearching() {
        return isSearching;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public LiveData<Boolean> getMutableHasMorePreviousMessages() {
        return mutableHasMorePreviousMessages;
    }

    public LiveData<Boolean> getMutableHasMorePreviousConversations() {
        return mutableHasMoreConversations;
    }

    public MutableLiveData<Integer> getMutableMessagesRangeChanged() {
        return mutableMessagesRangeChanged;
    }

    public MutableLiveData<Integer> getMutableConversationsRangeChanged() {
        return mutableConversationsRangeChanged;
    }

    public MutableLiveData<CometChatException> getCometChatException() {
        return cometchatException;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up pending callbacks to prevent memory leaks
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
            debounceRunnable = null;
        }
    }
}
