package com.cometchat.chatuikit.compactmessagecomposer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.ai.CometChatAIStreamService;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SingleLineComposerViewModel manages the state and business logic for
 * CometChatSingleLineComposer.
 * <p>
 * This ViewModel handles:
 * <ul>
 *     <li>Text content state</li>
 *     <li>Edit/Reply mode state</li>
 *     <li>Toolbar visibility state</li>
 *     <li>Active text formats</li>
 *     <li>Typing indicator management</li>
 *     <li>Message sending logic (text, media, custom)</li>
 *     <li>Panel show/hide events</li>
 * </ul>
 * </p>
 *
 * @see CometChatCompactMessageComposer
 * @see ComposerState
 */
public class CompactMessageComposerViewModel extends ViewModel {
    private static final String TAG = CompactMessageComposerViewModel.class.getSimpleName();

    // Listener tag for events
    public String LISTENERS_TAG;

    // LiveData for UI state
    private final MutableLiveData<ComposerState> composerState = new MutableLiveData<>(new ComposerState());
    private final MutableLiveData<String> textContent = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> toolbarVisible = new MutableLiveData<>(false);
    private final MutableLiveData<Set<FormatType>> activeFormats = new MutableLiveData<>(EnumSet.noneOf(FormatType.class));
    private final MutableLiveData<TextMessage> editingMessage = new MutableLiveData<>(null);
    private final MutableLiveData<BaseMessage> replyingToMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sendButtonActive = new MutableLiveData<>(false);
    
    // LiveData for message operations (following MessageComposerViewModel pattern)
    private final MutableLiveData<BaseMessage> sentMessage = new MutableLiveData<>();
    private final MutableLiveData<CometChatException> exception = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> processEdit = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> processQuote = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> successEdit = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> successQuote = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, String>> mutableHashMap = new MutableLiveData<>();
    
    // LiveData for panel management (following MessageComposerViewModel pattern)
    private final MutableLiveData<Void> closeBottomPanel = new MutableLiveData<>();
    private final MutableLiveData<Void> closeTopPanel = new MutableLiveData<>();
    private final MutableLiveData<Function1<Context, View>> showTopPanel = new MutableLiveData<>();
    private final MutableLiveData<Function1<Context, View>> showBottomPanel = new MutableLiveData<>();
    private final MutableLiveData<String> composeText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAIAssistantGenerating = new MutableLiveData<>(false);

    // Message context
    private User user;
    private Group group;
    private String id;
    private String type;
    private long parentMessageId = -1;
    private HashMap<String, String> idMap = new HashMap<>();
    private Void aVoid;

    // Typing indicator management
    private Timer typingTimer;
    private boolean isTyping = false;
    private long typingDebounceMs = 1000;
    private boolean disableTypingEvents = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Default constructor.
     */
    public CompactMessageComposerViewModel() {
        // Initialize with default state
    }

    // ==================== LiveData Getters ====================

    /**
     * Returns the LiveData for the complete composer state.
     *
     * @return LiveData containing the ComposerState.
     */
    public LiveData<ComposerState> getComposerState() {
        return composerState;
    }

    /**
     * Returns the LiveData for text content.
     *
     * @return LiveData containing the text content.
     */
    public LiveData<String> getTextContent() {
        return textContent;
    }

    /**
     * Returns the LiveData for toolbar visibility.
     *
     * @return LiveData containing the toolbar visibility state.
     */
    public LiveData<Boolean> getToolbarVisible() {
        return toolbarVisible;
    }

    /**
     * Returns the LiveData for active formats.
     *
     * @return LiveData containing the set of active FormatType values.
     */
    public LiveData<Set<FormatType>> getActiveFormats() {
        return activeFormats;
    }

    /**
     * Returns the LiveData for the message being edited.
     *
     * @return LiveData containing the TextMessage being edited, or null.
     */
    public LiveData<TextMessage> getEditingMessage() {
        return editingMessage;
    }

    /**
     * Returns the LiveData for the message being replied to.
     *
     * @return LiveData containing the BaseMessage being replied to, or null.
     */
    public LiveData<BaseMessage> getReplyingToMessage() {
        return replyingToMessage;
    }

    /**
     * Returns the LiveData for send button active state.
     *
     * @return LiveData containing the send button active state.
     */
    public LiveData<Boolean> getSendButtonActive() {
        return sendButtonActive;
    }

    /**
     * Returns the LiveData for sent messages.
     *
     * @return LiveData containing the sent BaseMessage.
     */
    public MutableLiveData<BaseMessage> sentMessage() {
        return sentMessage;
    }

    /**
     * Returns the LiveData for exceptions.
     *
     * @return LiveData containing any CometChatException that occurred.
     */
    public MutableLiveData<CometChatException> getException() {
        return exception;
    }

    /**
     * Returns the LiveData for edit processing.
     *
     * @return LiveData containing the message being processed for edit.
     */
    public MutableLiveData<BaseMessage> processEdit() {
        return processEdit;
    }

    /**
     * Returns the LiveData for quote processing.
     *
     * @return LiveData containing the message being processed for quote.
     */
    public MutableLiveData<BaseMessage> processQuote() {
        return processQuote;
    }

    /**
     * Returns the LiveData for successful edit.
     *
     * @return LiveData containing the successfully edited message.
     */
    public MutableLiveData<BaseMessage> successEdit() {
        return successEdit;
    }

    /**
     * Returns the LiveData for successful quote.
     *
     * @return LiveData containing the successfully quoted message.
     */
    public MutableLiveData<BaseMessage> successQuote() {
        return successQuote;
    }

    /**
     * Returns the LiveData for closing bottom panel.
     *
     * @return LiveData for close bottom panel event.
     */
    public MutableLiveData<Void> closeBottomPanel() {
        return closeBottomPanel;
    }

    /**
     * Returns the LiveData for closing top panel.
     *
     * @return LiveData for close top panel event.
     */
    public MutableLiveData<Void> closeTopPanel() {
        return closeTopPanel;
    }

    /**
     * Returns the LiveData for showing top panel.
     *
     * @return LiveData containing the view function for top panel.
     */
    public MutableLiveData<Function1<Context, View>> showTopPanel() {
        return showTopPanel;
    }

    /**
     * Returns the LiveData for showing bottom panel.
     *
     * @return LiveData containing the view function for bottom panel.
     */
    public MutableLiveData<Function1<Context, View>> showBottomPanel() {
        return showBottomPanel;
    }

    /**
     * Returns the LiveData for compose text.
     *
     * @return LiveData containing the compose text.
     */
    public MutableLiveData<String> getComposeText() {
        return composeText;
    }

    public MutableLiveData<Boolean> getIsAIAssistantGenerating() {
        return isAIAssistantGenerating;
    }

    /**
     * Returns the ID map for this composer context.
     *
     * @return HashMap containing the ID map.
     */
    public HashMap<String, String> getIdMap() {
        return idMap;
    }

    // ==================== State Setters ====================

    /**
     * Sets the text content and updates related state.
     *
     * @param text The text content to set.
     */
    public void setText(@NonNull String text) {
        textContent.setValue(text);
        sendButtonActive.setValue(hasMeaningfulContent(text));
        
        // Update composer state
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setText(text);
            composerState.setValue(state);
        }
        
        // Handle typing indicator
        handleTypingIndicator(text);
    }

    /**
     * Checks if the text has meaningful content for sending.
     * <p>
     * This method strips zero-width spaces (used as placeholders for code formatting)
     * and other invisible characters before checking if the text is empty.
     * </p>
     *
     * @param text The text to check.
     * @return true if the text has meaningful content, false otherwise.
     */
    private boolean hasMeaningfulContent(String text) {
        if (text == null) {
            return false;
        }
        // Remove zero-width spaces (U+200B) used as placeholders for code formatting
        String stripped = text
                .replace("\u200B", "")  // Zero-width space
                .replace("\u200C", "")  // Zero-width non-joiner
                .replace("\u200D", "")  // Zero-width joiner
                .replace("\uFEFF", ""); // Zero-width no-break space (BOM)
        return !stripped.trim().isEmpty();
    }

    /**
     * Sets the toolbar visibility.
     *
     * @param visible true to show toolbar, false to hide.
     */
    public void setToolbarVisible(boolean visible) {
        toolbarVisible.setValue(visible);
        
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setToolbarVisible(visible);
            composerState.setValue(state);
        }
    }

    /**
     * Sets the active formats.
     *
     * @param formats The set of active FormatType values.
     */
    public void setActiveFormats(@NonNull Set<FormatType> formats) {
        activeFormats.setValue(formats);
        
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setActiveFormats(formats);
            composerState.setValue(state);
        }
    }

    /**
     * Sets the message to edit.
     *
     * @param message The TextMessage to edit, or null to exit edit mode.
     */
    public void setEditMessage(@Nullable TextMessage message) {
        editingMessage.setValue(message);
        
        if (message != null) {
            setText(message.getText());
        }
        
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setEditingMessage(message);
            composerState.setValue(state);
        }
    }

    /**
     * Sets the message to reply to.
     *
     * @param message The BaseMessage to reply to, or null to exit reply mode.
     */
    public void setReplyMessage(@Nullable BaseMessage message) {
        replyingToMessage.setValue(message);
        
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setReplyingToMessage(message);
            composerState.setValue(state);
        }
    }

    /**
     * Cancels edit mode.
     */
    public void cancelEdit() {
        setEditMessage(null);
        setText("");
    }

    /**
     * Cancels reply mode.
     */
    public void cancelReply() {
        setReplyMessage(null);
    }

    /**
     * Sets the inline recording mode state.
     *
     * @param inlineRecordingMode true to enable inline recording mode, false to disable.
     */
    public void setInlineRecordingMode(boolean inlineRecordingMode) {
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.setInlineRecordingMode(inlineRecordingMode);
            composerState.setValue(state);
        }
    }

    /**
     * Resets the composer state to defaults.
     */
    public void reset() {
        setText("");
        setToolbarVisible(false);
        setActiveFormats(EnumSet.noneOf(FormatType.class));
        setEditMessage(null);
        setReplyMessage(null);
        
        ComposerState state = composerState.getValue();
        if (state != null) {
            state.reset();
            composerState.setValue(state);
        }
    }

    // ==================== Message Context ====================

    /**
     * Sets the user for one-on-one messaging.
     *
     * @param user The User object.
     */
    public void setUser(@Nullable User user) {
        if (user != null) {
            this.user = user;
            this.group = null;
            this.id = user.getUid();
            this.type = UIKitConstants.ReceiverType.USER;
            setIdMap();
        }
    }

    /**
     * Sets the group for group messaging.
     *
     * @param group The Group object.
     */
    public void setGroup(@Nullable Group group) {
        if (group != null) {
            this.group = group;
            this.user = null;
            this.id = group.getGuid();
            this.type = UIKitConstants.ReceiverType.GROUP;
            setIdMap();
        }
    }

    /**
     * Sets the parent message ID for threaded replies.
     *
     * @param parentMessageId The parent message ID.
     */
    public void setParentMessageId(long parentMessageId) {
        this.parentMessageId = parentMessageId;
        setIdMap();
    }

    /**
     * Sets up the ID map for event matching.
     */
    private void setIdMap() {
        idMap = new HashMap<>();
        if (parentMessageId > 0) {
            idMap.put(UIKitConstants.MapId.PARENT_MESSAGE_ID, String.valueOf(parentMessageId));
        }
        if (user != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, user.getUid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.USER);
        } else if (group != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, group.getGuid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.GROUP);
        }
        mutableHashMap.setValue(idMap);
    }

    /**
     * Gets the current user.
     *
     * @return The User object, or null.
     */
    @Nullable
    public User getUser() {
        return user;
    }

    /**
     * Gets the current group.
     *
     * @return The Group object, or null.
     */
    @Nullable
    public Group getGroup() {
        return group;
    }

    /**
     * Gets the receiver ID.
     *
     * @return The receiver ID.
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * Gets the receiver type.
     *
     * @return The receiver type.
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Gets the parent message ID.
     *
     * @return The parent message ID.
     */
    public long getParentMessageId() {
        return parentMessageId;
    }

    // ==================== Event Listeners ====================

    /**
     * Adds event listeners for message and UI events.
     */
    public void addListeners() {
        LISTENERS_TAG = System.currentTimeMillis() + "_single_line_composer";
        
        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {
            @Override
            public void ccMessageEdited(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.IN_PROGRESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                    if (baseMessage instanceof TextMessage) {
                        processEdit.setValue(baseMessage);
                    }
                }
            }

            @Override
            public void ccReplyToMessage(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.IN_PROGRESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                    processQuote.setValue(baseMessage);
                } else if (status == MessageStatus.SUCCESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                    successQuote.setValue(baseMessage);
                }
            }
        });

        CometChatUIEvents.addListener(LISTENERS_TAG, new CometChatUIEvents() {
            @Override
            public void showPanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment, Function1<Context, View> view) {
                if (UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM.equals(alignment) && idMap.equals(id)) {
                    showBottomPanel.setValue(view);
                } else if (UIKitConstants.CustomUIPosition.COMPOSER_TOP.equals(alignment) && idMap.equals(id)) {
                    showTopPanel.setValue(view);
                }
            }

            @Override
            public void hidePanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment) {
                if (UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM.equals(alignment) && idMap.equals(id)) {
                    closeBottomPanel.setValue(aVoid);
                } else if (UIKitConstants.CustomUIPosition.COMPOSER_TOP.equals(alignment) && idMap.equals(id)) {
                    closeTopPanel.setValue(aVoid);
                }
            }

            @Override
            public void ccComposeMessage(String userOrGroupId, String text) {
                if (id != null && id.equals(userOrGroupId)) {
                    composeText.setValue(text);
                }
            }
        });

        CometChatAIStreamService.setOnStreamCallBack(new CometChatAIStreamService.OnStreamCallBack() {
            @Override
            public void onStreamCompleted() {
                isAIAssistantGenerating.setValue(false);
            }

            @Override
            public void onStreamInterrupted() {
                isAIAssistantGenerating.setValue(false);
            }
        });
    }

    /**
     * Removes event listeners.
     */
    public void removeListeners() {
        if (LISTENERS_TAG != null) {
            CometChatMessageEvents.removeListener(LISTENERS_TAG);
            CometChatUIEvents.removeListener(LISTENERS_TAG);
        }
    }

    // ==================== Message Sending ====================

    /**
     * Sends a text message.
     *
     * @param text The text content to send.
     */
    public void sendTextMessage(String text) {
        TextMessage textMessage = getTextMessage(text);
        if (textMessage != null) {
            sendTextMessage(textMessage);
        }
    }

    /**
     * Creates a TextMessage object.
     *
     * @param text The text content.
     * @return The TextMessage object, or null if invalid.
     */
    @Nullable
    public TextMessage getTextMessage(String text) {
        if (text != null && !text.isEmpty() && id != null && type != null) {
            TextMessage message = new TextMessage(id, text.trim(), type);
            if (parentMessageId > 0) {
                message.setParentMessageId((int) parentMessageId);
            }
            return message;
        }
        return null;
    }

    /**
     * Sends a TextMessage.
     *
     * @param textMessage The TextMessage to send.
     */
    public void sendTextMessage(TextMessage textMessage) {
        if (textMessage == null) return;
        
        CometChatUIKit.sendTextMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage message) {
                sentMessage.setValue(message);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, "Error sending text message: " + e.getMessage());
                exception.setValue(e);
            }
        });
    }

    /**
     * Sends a media message.
     *
     * @param mediaMessage The MediaMessage to send.
     */
    public void sendMediaMessage(MediaMessage mediaMessage) {
        if (mediaMessage == null) return;
        
        CometChatUIKit.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage message) {
                sentMessage.setValue(message);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, "Error sending media message: " + e.getMessage());
                exception.setValue(e);
            }
        });
    }

    /**
     * Creates a MediaMessage object.
     *
     * @param file        The media file.
     * @param contentType The content type.
     * @return The MediaMessage object, or null if invalid.
     */
    @Nullable
    public MediaMessage getMediaMessage(File file, String contentType) {
        if (file != null && contentType != null && id != null && type != null) {
            MediaMessage mediaMessage = new MediaMessage(id, file, contentType, type);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(UIKitConstants.IntentStrings.PATH, file.getAbsolutePath());
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
            if (parentMessageId > 0) {
                mediaMessage.setParentMessageId((int) parentMessageId);
            }
            mediaMessage.setMetadata(jsonObject);
            return mediaMessage;
        }
        return null;
    }
    
    /**
     * Creates a MediaMessage with waveform amplitude data stored in metadata.
     * Used for audio messages recorded with the inline audio recorder.
     *
     * @param file The media file to send.
     * @param contentType The content type of the media.
     * @param amplitudes The list of amplitude values for waveform visualization.
     * @return The MediaMessage object with waveform data, or null if invalid.
     */
    @Nullable
    public MediaMessage getMediaMessageWithWaveform(File file, String contentType, List<Float> amplitudes) {
        if (file != null && contentType != null && id != null && type != null) {
            MediaMessage mediaMessage = new MediaMessage(id, file, contentType, type);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(UIKitConstants.IntentStrings.PATH, file.getAbsolutePath());
                
                // Store waveform amplitudes as JSON array
                if (amplitudes != null && !amplitudes.isEmpty()) {
                    JSONArray amplitudesArray = new JSONArray();
                    for (Float amplitude : amplitudes) {
                        amplitudesArray.put(amplitude.doubleValue());
                    }
                    jsonObject.put("waveform_amplitudes", amplitudesArray);
                }
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
            if (parentMessageId > 0) {
                mediaMessage.setParentMessageId((int) parentMessageId);
            }
            mediaMessage.setMetadata(jsonObject);
            return mediaMessage;
        }
        return null;
    }

    /**
     * Sends a custom message.
     *
     * @param customMessage The CustomMessage to send.
     */
    public void sendCustomMessage(CustomMessage customMessage) {
        if (customMessage == null) return;
        
        CometChatUIKit.sendCustomMessage(customMessage, new CometChat.CallbackListener<CustomMessage>() {
            @Override
            public void onSuccess(CustomMessage message) {
                sentMessage.setValue(message);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, "Error sending custom message: " + e.getMessage());
                exception.setValue(e);
            }
        });
    }

    /**
     * Edits a text message.
     *
     * @param textMessage The TextMessage to edit.
     */
    public void editMessage(TextMessage textMessage) {
        if (textMessage == null) return;
        
        // Remove translation metadata if present
        if (textMessage.getMetadata() != null) {
            if (textMessage.getMetadata().has(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED)) {
                textMessage.getMetadata().remove(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED);
            }
        }
        
        CometChat.editMessage(textMessage, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage message) {
                CometChatUIKitHelper.onMessageEdited(message, MessageStatus.SUCCESS);
                successEdit.setValue(message);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, "Error editing message: " + e.getMessage());
                exception.setValue(e);
                textMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                CometChatUIKitHelper.onMessageEdited(textMessage, MessageStatus.ERROR);
            }
        });
    }

    /**
     * Notifies about message reply.
     *
     * @param baseMessage The message being replied to.
     */
    public void onMessageReply(BaseMessage baseMessage) {
        CometChatUIKitHelper.onMessageReply(baseMessage, MessageStatus.SUCCESS);
    }

    // ==================== Typing Indicator ====================

    /**
     * Sets the typing debounce duration.
     *
     * @param debounceMs The debounce duration in milliseconds.
     */
    public void setTypingDebounceMs(long debounceMs) {
        this.typingDebounceMs = debounceMs;
    }

    /**
     * Sets whether typing events are disabled.
     *
     * @param disabled true to disable typing events.
     */
    public void setDisableTypingEvents(boolean disabled) {
        this.disableTypingEvents = disabled;
    }

    /**
     * Handles typing indicator logic with debounce.
     *
     * @param text The current text content.
     */
    private void handleTypingIndicator(@NonNull String text) {
        if (disableTypingEvents) {
            return;
        }

        // Cancel existing timer
        if (typingTimer != null) {
            typingTimer.cancel();
            typingTimer = null;
        }

        if (text.isEmpty()) {
            // Stop typing indicator
            if (isTyping) {
                endTyping();
            }
            return;
        }

        // Start typing indicator if not already typing
        if (!isTyping) {
            startTyping();
        }

        // Set timer to stop typing after debounce period
        typingTimer = new Timer();
        typingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> endTyping());
            }
        }, typingDebounceMs);
    }

    /**
     * Starts the typing indicator.
     */
    private void startTyping() {
        isTyping = true;
        if (id != null && type != null) {
            CometChat.startTyping(new TypingIndicator(id, type));
        }
    }

    /**
     * Ends the typing indicator.
     */
    private void endTyping() {
        isTyping = false;
        if (id != null && type != null) {
            CometChat.endTyping(new TypingIndicator(id, type));
        }
    }

    // ==================== Lifecycle ====================

    /**
     * Cancels the typing timer if active.
     * This should be called when the view is detached.
     */
    public void cancelTypingTimer() {
        if (typingTimer != null) {
            typingTimer.cancel();
            typingTimer = null;
        }
        
        // End typing if active
        if (isTyping) {
            endTyping();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTypingTimer();
        removeListeners();
    }
}
