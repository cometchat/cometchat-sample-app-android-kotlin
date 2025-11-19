package com.cometchat.chatuikit.messagecomposer;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.ai.CometChatAIStreamService;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class MessageComposerViewModel extends ViewModel {
    private static final String TAG = MessageComposerViewModel.class.getSimpleName();


    public String LISTENERS_TAG;
    public MutableLiveData<BaseMessage> sentMessage;
    public MutableLiveData<BaseMessage> processEdit;
    public MutableLiveData<BaseMessage> processQuote;
    public MutableLiveData<BaseMessage> successQuote;
    public MutableLiveData<CometChatException> exception;
    public MutableLiveData<BaseMessage> successEdit;
    public MutableLiveData<HashMap<String, String>> mutableHashMap;
    public MutableLiveData<Boolean> isAIAssistantGenerating;
    public User user;
    public Group group;
    public String id;
    public String type;
    public HashMap<String, String> idMap;
    public MutableLiveData<String> composeText;
    public long parentMessageId = -1;
    public MutableLiveData<Void> closeBottomPanel;
    public MutableLiveData<Void> closeTopPanel;
    public MutableLiveData<Function1<Context, View>> showTopPanel;
    public MutableLiveData<Function1<Context, View>> showBottomPanel;
    public Void aVoid;
    private boolean isAgentChat;

    public MessageComposerViewModel() {
        sentMessage = new MutableLiveData<>();
        exception = new MutableLiveData<>();
        processEdit = new MutableLiveData<>();
        processQuote = new MutableLiveData<>();
        successEdit = new MutableLiveData<>();
        successQuote = new MutableLiveData<>();
        mutableHashMap = new MutableLiveData<>();
        closeBottomPanel = new MutableLiveData<>();
        closeTopPanel = new MutableLiveData<>();
        showTopPanel = new MutableLiveData<>();
        showBottomPanel = new MutableLiveData<>();
        composeText = new MutableLiveData<>();
        idMap = new HashMap<>();
        isAIAssistantGenerating = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsAIAssistantGenerating() {
        return isAIAssistantGenerating;
    }

    public MutableLiveData<String> getComposeText() {
        return composeText;
    }

    public MutableLiveData<BaseMessage> sentMessage() {
        return sentMessage;
    }

    public MutableLiveData<BaseMessage> processEdit() {
        return processEdit;
    }

    public MutableLiveData<BaseMessage> processQuote() {
        return processQuote;
    }

    public MutableLiveData<CometChatException> getException() {
        return exception;
    }

    public MutableLiveData<BaseMessage> successEdit() {
        return successEdit;
    }

    public MutableLiveData<BaseMessage> successQuote() {
        return successQuote;
    }

    public MutableLiveData<HashMap<String, String>> getMutableHashMap() {
        return mutableHashMap;
    }

    public MutableLiveData<Void> closeBottomPanel() {
        return closeBottomPanel;
    }

    public MutableLiveData<Void> closeTopPanel() {
        return closeTopPanel;
    }

    public MutableLiveData<Function1<Context, View>> showTopPanel() {
        return showTopPanel;
    }

    public MutableLiveData<Function1<Context, View>> showBottomPanel() {
        return showBottomPanel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(@Nullable User user) {
        if (user != null) {
            this.user = user;
            this.id = user.getUid();
            this.type = UIKitConstants.ReceiverType.USER;
            this.isAgentChat = Utils.isAgentChat(user);
            setIdMap();
        }
    }

    public void setIdMap() {
        if (parentMessageId > 0)
            idMap.put(UIKitConstants.MapId.PARENT_MESSAGE_ID, String.valueOf(parentMessageId));
        if (user != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, user.getUid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.USER);
        } else if (group != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, group.getGuid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.GROUP);
        }
        mutableHashMap.setValue(idMap);
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
            this.id = group.getGuid();
            this.type = UIKitConstants.ReceiverType.GROUP;
            setIdMap();
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public HashMap<String, String> getIdMap() {
        return idMap;
    }

    public void setParentMessageId(long id) {
        this.parentMessageId = id;
        setIdMap();
    }

    public void onMessageReply(BaseMessage baseMessage) {
        CometChatUIKitHelper.onMessageReply(baseMessage, MessageStatus.SUCCESS);
    }

    public void addListeners() {
        LISTENERS_TAG = System.currentTimeMillis() + "_composer";
        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {
            @Override
            public void ccMessageEdited(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.IN_PROGRESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                    if (baseMessage instanceof TextMessage) processEdit.setValue(baseMessage);
                }
            }

            /**
             * Called when a reply to a message is sent/in progress.
             *
             * @param baseMessage The replied message object.
             * @param status      The status of the reply message.
             */
            @Override
            public void ccReplyToMessage(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.IN_PROGRESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                    processQuote.setValue(baseMessage);
                } else {
                    if (status == MessageStatus.SUCCESS && baseMessage != null && idMap.equals(Utils.getIdMap(baseMessage))) {
                        successQuote.setValue(baseMessage);
                    }
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
                if (UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM.equals(alignment) && idMap.equals(id))
                    closeBottomPanel.setValue(aVoid);
                else if (UIKitConstants.CustomUIPosition.COMPOSER_TOP.equals(alignment) && idMap.equals(id))
                    closeTopPanel.setValue(aVoid);
            }

            @Override
            public void ccComposeMessage(String userOrGroupId, String text) {
                if (id.equals(userOrGroupId)) {
                    composeText.setValue(text);
                }
            }
        });
    }

    public void removeListeners() {
        CometChatMessageEvents.removeListener(LISTENERS_TAG);
        CometChatUIEvents.removeListener(LISTENERS_TAG);
    }

    public void sendCustomMessage(CustomMessage baseMessage) {
        CometChatUIKit.sendCustomMessage(baseMessage, new CometChat.CallbackListener<CustomMessage>() {
            @Override
            public void onSuccess(CustomMessage mediaMessage) {
                sentMessage.setValue(mediaMessage);
            }

            @Override
            public void onError(CometChatException e) {
                exception.setValue(e);
            }
        });
    }

    public void sendMediaMessage(MediaMessage mediaMessage) {
        CometChatUIKit.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage) {
                sentMessage.setValue(mediaMessage);
            }

            @Override
            public void onError(CometChatException e) {
                exception.setValue(e);
            }
        });
    }

    public MediaMessage getMediaMessage(File file, String contentType) {
        if (file != null && contentType != null) {
            MediaMessage mediaMessage = new MediaMessage(id, file, contentType, type);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(UIKitConstants.IntentStrings.PATH, file.getAbsolutePath());
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
            if (parentMessageId > -1) mediaMessage.setParentMessageId(parentMessageId);
            mediaMessage.setMetadata(jsonObject);
            return mediaMessage;
        }
        return null;
    }

    public void sendTextMessage(String text) {
        TextMessage textMessage = getTextMessage(text);
        sendTextMessage(textMessage);
    }

    public TextMessage getTextMessage(String text) {
        if (text != null && !text.isEmpty()) {
            TextMessage message = new TextMessage(id, text.trim(), type);
            if (parentMessageId > -1) message.setParentMessageId(parentMessageId);
            return message;
        }
        return null;
    }

    public void sendTextMessage(TextMessage textMessage) {
        CometChatUIKit.sendTextMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                if (isAgentChat) {
                    if (parentMessageId == -1) parentMessageId = textMessage.getId();
                    isAIAssistantGenerating.setValue(true);
                }
                sentMessage.setValue(textMessage);
            }

            @Override
            public void onError(CometChatException e) {
                exception.setValue(e);
            }
        });
    }

    public void editMessage(TextMessage textMessage) {
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
                exception.setValue(e);
                textMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                CometChatUIKitHelper.onMessageEdited(textMessage, MessageStatus.ERROR);
            }
        });
    }
}
