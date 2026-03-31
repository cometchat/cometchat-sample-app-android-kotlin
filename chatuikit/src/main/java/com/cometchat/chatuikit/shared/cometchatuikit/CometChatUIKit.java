package com.cometchat.chatuikit.shared.cometchatuikit;

import static com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper.onMessageSent;
import static com.cometchat.chatuikit.shared.resources.utils.Utils.isCallingAvailable;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cometchat.chat.core.AppSettings;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.ConversationUpdateSettings;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.FlagReason;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.BuildConfig;
import com.cometchat.chatuikit.ai.AIExtensionDataSource;
import com.cometchat.chatuikit.ai.DefaultAIFeature;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.extensions.DefaultExtensions;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;   
import com.cometchat.chatuikit.shared.framework.ExtensionsDataSource;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.localise.Language;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.reaction.emojikeyboard.EmojiKeyboardUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The CometChatUIKit class is a utility class that provides various methods for
 * initializing and interacting with the CometChat SDK. It includes methods for
 * initialization, authentication, login, logout, user management, and sending
 * messages.
 */
public final class CometChatUIKit {
    private static final String TAG = CometChatUIKit.class.getSimpleName();

    private static List<FlagReason> flagReasons;
    private static UIKitSettings authenticationSettings;
    private static String successMessage;

    private CometChatUIKit() {
    }

    /**
     * Initializes the CometChat SDK with the provided authentication settings.
     *
     * @param context          The context of the calling activity or application.
     * @param authSettings     The UIKitSettings object containing the authentication settings.
     * @param callbackListener The callback listener to handle the initialization success or
     *                         failure.
     */
    public static void init(Context context, UIKitSettings authSettings, CometChat.CallbackListener<String> callbackListener) {
        authenticationSettings = authSettings;

        if (!checkAuthSettings(callbackListener)) return;

        AppSettings.AppSettingsBuilder appSettingsBuilder = new AppSettings.AppSettingsBuilder();

        if (authenticationSettings.getRoles() != null && !authenticationSettings.getRoles().isEmpty()) {
            appSettingsBuilder.subscribePresenceForRoles(authenticationSettings.getRoles());
        } else if (authenticationSettings.getSubscriptionType().equals("ALL_USERS")) {
            appSettingsBuilder.subscribePresenceForAllUsers();
        } else if (authenticationSettings.getSubscriptionType().equals("FRIENDS")) {
            appSettingsBuilder.subscribePresenceForFriends();
        }

        appSettingsBuilder.autoEstablishSocketConnection(authenticationSettings.isAutoEstablishSocketConnection());
        appSettingsBuilder.setRegion(authenticationSettings.getRegion());
        appSettingsBuilder.overrideAdminHost(authenticationSettings.getOverrideAdminHost());
        appSettingsBuilder.overrideClientHost(authenticationSettings.getOverrideClientHost());
        AppSettings appSettings = appSettingsBuilder.build();
        CometChat.init(context, authenticationSettings.getAppId(), appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                SDKEventInitializer.addMessageListener();
                EmojiKeyboardUtils.loadAndSaveEmojis(context);
                successMessage = s;
                if (isCallingAvailable()) initiateCallingExtension(context);
                if (CometChatUIKit.getLoggedInUser() != null) {
                    initiateChatExtensions();
                    fetchFlagReasons();
                }

                if (callbackListener != null) callbackListener.onSuccess(successMessage);
                CometChat.setSource("uikit-v5", "android", "java");
            }

            @Override
            public void onError(CometChatException e) {
                if (e != null) if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    private static void fetchFlagReasons() {
        CometChat.getFlagReasons(new CometChat.CallbackListener<List<FlagReason>>() {
            @Override
            public void onSuccess(List<FlagReason> reasons) {
                flagReasons = reasons;
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    public static List<FlagReason> getFlagReasons() {
        return flagReasons != null ? flagReasons : new ArrayList<>();
    }

    /**
     * Checks if the authentication settings are valid and handles the error case.
     *
     * @param onError The callback listener to handle the error case.
     * @return True if the authentication settings are valid, False otherwise.
     */
    private static boolean checkAuthSettings(CometChat.CallbackListener onError) {
        if (authenticationSettings == null) {
            if (onError != null) {
                onError.onError(new CometChatException("ERR", "Authentication null", "Populate authSettings before initializing"));
            }
            return false;
        }

        if (authenticationSettings.getAppId() == null) {
            if (onError != null) {
                onError.onError(new CometChatException("appIdErr", "APP ID null", "Populate authSettings before initializing"));
            }
            return false;
        }
        return true;
    }

    private static void initiateCallingExtension(Context context) {
        if (isCallingAvailable()) new CallingExtension(context).enable();
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return The User object representing the logged-in user, or null if no user
     * is logged in.
     */
    public static User getLoggedInUser() {
        return CometChat.getLoggedInUser();
    }

    private static void initiateChatExtensions() {
        if (authenticationSettings != null) {
            List<AIExtensionDataSource> aiExtensions = authenticationSettings.getAIFeatures() == null ? DefaultAIFeature.get() : authenticationSettings.getAIFeatures();
            if (!aiExtensions.isEmpty()) {
                for (ExtensionsDataSource element : aiExtensions) {
                    element.enable();
                }
            }

            List<ExtensionsDataSource> extensionList = authenticationSettings.getExtensions() == null ? DefaultExtensions.get() : authenticationSettings.getExtensions();
            if (!extensionList.isEmpty()) {
                for (ExtensionsDataSource element : extensionList) {
                    element.enable();
                }
            }
        }
    }

    /**
     * Logs in a user with the specified UID.
     *
     * @param uid              The UID of the user to be logged in.
     * @param callbackListener The callback listener to handle the login success or failure.
     */
    public static void login(String uid, CometChat.CallbackListener<User> callbackListener) {
        if (!checkAuthSettings(callbackListener)) return;
        if (CometChatUIKit.getLoggedInUser() == null || !CometChatUIKit.getLoggedInUser().getUid().equals(uid)) {
            CometChat.login(uid, authenticationSettings.getAuthKey(), new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    initiateChatExtensions();
                    fetchFlagReasons();
                    if (callbackListener != null) {
                        callbackListener.onSuccess(user);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    if (callbackListener != null) {
                        callbackListener.onError(e);
                    }
                }
            });
        } else {
            initiateChatExtensions();
            if (callbackListener != null)
                callbackListener.onSuccess(CometChatUIKit.getLoggedInUser());
        }
    }

    public static boolean isSDKInitialized() {
        return CometChat.isInitialized();
    }

    /**
     * Logs in a user with the provided authentication token.
     *
     * @param authToken        The authentication token for the user.
     * @param callbackListener The callback listener to handle the login result. Must implement
     *                         the {@link CometChat.CallbackListener} interface.
     */
    public static void loginWithAuthToken(String authToken, CometChat.CallbackListener<User> callbackListener) {
        if (!checkAuthSettings(callbackListener)) return;
        if (CometChatUIKit.getLoggedInUser() == null) {
            CometChat.login(authToken, new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    initiateChatExtensions();
                    fetchFlagReasons();
                    if (callbackListener != null) callbackListener.onSuccess(user);
                }

                @Override
                public void onError(CometChatException e) {
                    if (callbackListener != null) callbackListener.onError(e);
                }
            });
        } else {
            initiateChatExtensions();
            if (callbackListener != null)
                callbackListener.onSuccess(CometChatUIKit.getLoggedInUser());
        }
    }

    /**
     * Logs out the currently logged-in user.
     *
     * @param callbackListener The callback listener to handle the logout result. Must implement
     *                         the {@link CometChat.CallbackListener} interface.
     */
    public static void logout(CometChat.CallbackListener<String> callbackListener) {
        CometChat.logout(new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                ChatConfigurator.init();
                if (callbackListener != null) callbackListener.onSuccess(successMessage);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Creates a new user in the CometChat platform.
     *
     * @param user             The user object containing the details of the user to be created.
     * @param callbackListener The callback listener to handle the create user result. Must
     *                         implement the {@link CometChat.CallbackListener} interface.
     * @throws IllegalArgumentException If the provided user object is null.
     */
    public static void createUser(User user, CometChat.CallbackListener<User> callbackListener) {

        if (!checkAuthSettings(callbackListener)) return;

        CometChat.createUser(user, authenticationSettings.getAuthKey(), new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (callbackListener != null) callbackListener.onSuccess(user);
            }

            @Override
            public void onError(CometChatException e) {
                if (callbackListener != null) callbackListener.onError(e);
            }
        });
    }

    /**
     * Sends a custom message.
     *
     * @param customMessage The custom message to be sent.
     */
    public static void sendCustomMessage(CustomMessage customMessage, CometChat.CallbackListener<CustomMessage> messageCallbackListener) {
        if (customMessage == null) return;
        customMessage.setSender(customMessage.getSender() == null ? CometChat.getLoggedInUser() : customMessage.getSender());
        customMessage.setMuid(customMessage.getMuid() == null || customMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : customMessage.getMuid());
        customMessage.setSentAt(customMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : customMessage.getSentAt());
        customMessage.shouldSendNotification(true);

        onMessageSent(customMessage, MessageStatus.IN_PROGRESS);
        CometChat.sendCustomMessage(customMessage, new CometChat.CallbackListener<CustomMessage>() {
            @Override
            public void onSuccess(CustomMessage customMessage) {
                onMessageSent(customMessage, MessageStatus.SUCCESS);
                triggerMessageCallback(messageCallbackListener, customMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                customMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                CometChatUIKitHelper.onMessageSent(customMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    private static void triggerMessageCallback(CometChat.CallbackListener listener, BaseMessage message, CometChatException exception) {
        if (listener != null) {
            if (exception == null) listener.onSuccess(message);
            else listener.onError(exception);
        }
    }

    /**
     * Sends a text message.
     *
     * @param textMessage The text message to be sent.
     */
    public static void sendTextMessage(TextMessage textMessage, CometChat.CallbackListener<TextMessage> messageCallbackListener) {
        if (textMessage == null) return;
        textMessage.setSender(textMessage.getSender() == null ? CometChat.getLoggedInUser() : textMessage.getSender());
        textMessage.setMuid(textMessage.getMuid() == null || textMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : textMessage.getMuid());
        textMessage.setSentAt(textMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : textMessage.getSentAt());

        onMessageSent(textMessage, MessageStatus.IN_PROGRESS);
        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                onMessageSent(textMessage, MessageStatus.SUCCESS);
                triggerMessageCallback(messageCallbackListener, textMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                textMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                CometChatUIKitHelper.onMessageSent(textMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    /**
     * Sends a media message.
     *
     * @param mediaMessage The media message to be sent.
     */
    public static void sendMediaMessage(MediaMessage mediaMessage, CometChat.CallbackListener<MediaMessage> messageCallbackListener) {
        if (mediaMessage == null) return;
        mediaMessage.setSender(mediaMessage.getSender() == null ? CometChat.getLoggedInUser() : mediaMessage.getSender());
        mediaMessage.setMuid(mediaMessage.getMuid() == null || mediaMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : mediaMessage.getMuid());
        mediaMessage.setSentAt(mediaMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : mediaMessage.getSentAt());

        // Cache waveform amplitudes by MUID before stripping from metadata
        // This allows AudioBubble to retrieve them even after we remove from metadata
        com.cometchat.chatuikit.shared.views.waveform.WaveformCache.cacheAndStripFromMetadata(mediaMessage);

        onMessageSent(mediaMessage, MessageStatus.IN_PROGRESS);
        
        CometChat.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage) {
                onMessageSent(mediaMessage, MessageStatus.SUCCESS);
                triggerMessageCallback(messageCallbackListener, mediaMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                mediaMessage.setMetadata(Utils.placeErrorObjectInMetaData(e, mediaMessage.getMetadata()));
                CometChatUIKitHelper.onMessageSent(mediaMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    public static void sendFormMessage(FormMessage formMessage,
                                       boolean disableLocalEvents,
                                       CometChat.CallbackListener<FormMessage> messageCallbackListener) {
        if (formMessage == null) return;
        formMessage.setSender(formMessage.getSender() == null ? CometChat.getLoggedInUser() : formMessage.getSender());
        formMessage.setMuid(formMessage.getMuid() == null || formMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : formMessage.getMuid());
        formMessage.setSentAt(formMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : formMessage.getSentAt());

        if (!disableLocalEvents) onMessageSent(formMessage, MessageStatus.IN_PROGRESS);
        InteractiveMessage interactiveMessage = formMessage.toInteractiveMessage();
        CometChat.sendInteractiveMessage(interactiveMessage, new CometChat.CallbackListener<InteractiveMessage>() {
            @Override
            public void onSuccess(InteractiveMessage interactiveMessage) {
                FormMessage formMessage = FormMessage.fromInteractive(interactiveMessage);
                if (!disableLocalEvents) {
                    CometChatUIKitHelper.onMessageSent(formMessage, MessageStatus.SUCCESS);
                }
                triggerMessageCallback(messageCallbackListener, formMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                formMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                if (!disableLocalEvents) onMessageSent(formMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    public static void sendSchedulerMessage(SchedulerMessage schedulerMessage,
                                            boolean disableLocalEvents,
                                            CometChat.CallbackListener<SchedulerMessage> messageCallbackListener) {
        if (schedulerMessage == null) return;
        schedulerMessage.setSender(schedulerMessage.getSender() == null ? CometChat.getLoggedInUser() : schedulerMessage.getSender());
        schedulerMessage.setMuid(schedulerMessage.getMuid() == null || schedulerMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : schedulerMessage.getMuid());
        schedulerMessage.setSentAt(schedulerMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : schedulerMessage.getSentAt());

        if (!disableLocalEvents) onMessageSent(schedulerMessage, MessageStatus.IN_PROGRESS);
        InteractiveMessage interactiveMessage = schedulerMessage.toInteractiveMessage();
        CometChat.sendInteractiveMessage(interactiveMessage, new CometChat.CallbackListener<InteractiveMessage>() {
            @Override
            public void onSuccess(InteractiveMessage interactiveMessage) {
                SchedulerMessage schedulerMessage = SchedulerMessage.fromInteractive(interactiveMessage);
                if (!disableLocalEvents) {
                    CometChatUIKitHelper.onMessageSent(schedulerMessage, MessageStatus.SUCCESS);
                }
                triggerMessageCallback(messageCallbackListener, schedulerMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                schedulerMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                if (!disableLocalEvents) onMessageSent(schedulerMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    public static void sendCardMessage(CardMessage cardMessage,
                                       boolean disableLocalEvents,
                                       CometChat.CallbackListener<CardMessage> messageCallbackListener) {
        if (cardMessage == null) return;
        cardMessage.setSender(cardMessage.getSender() == null ? CometChat.getLoggedInUser() : cardMessage.getSender());
        cardMessage.setMuid(cardMessage.getMuid() == null || cardMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : cardMessage.getMuid());
        cardMessage.setSentAt(cardMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : cardMessage.getSentAt());
        if (!disableLocalEvents) onMessageSent(cardMessage, MessageStatus.IN_PROGRESS);
        InteractiveMessage interactiveMessage = cardMessage.toInteractiveMessage();
        CometChat.sendInteractiveMessage(interactiveMessage, new CometChat.CallbackListener<InteractiveMessage>() {
            @Override
            public void onSuccess(InteractiveMessage interactiveMessage) {
                CardMessage cardMessage = CardMessage.fromInteractive(interactiveMessage);
                if (!disableLocalEvents) {
                    CometChatUIKitHelper.onMessageSent(cardMessage, MessageStatus.SUCCESS);
                }
                triggerMessageCallback(messageCallbackListener, cardMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                cardMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                if (!disableLocalEvents) onMessageSent(cardMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    public static void sendCustomInteractiveMessage(CustomInteractiveMessage customInteractiveMessage,
                                                    boolean disableLocalEvents,
                                                    CometChat.CallbackListener<CustomInteractiveMessage> messageCallbackListener) {
        if (customInteractiveMessage == null) return;
        customInteractiveMessage.setSender(customInteractiveMessage.getSender() == null ? CometChat.getLoggedInUser() : customInteractiveMessage.getSender());
        customInteractiveMessage.setMuid(customInteractiveMessage.getMuid() == null || customInteractiveMessage
            .getMuid()
            .isEmpty() ? System.currentTimeMillis() + "" : customInteractiveMessage.getMuid());
        customInteractiveMessage.setSentAt(customInteractiveMessage.getSentAt() == 0 ? System.currentTimeMillis() / 1000 : customInteractiveMessage.getSentAt());
        if (!disableLocalEvents) onMessageSent(customInteractiveMessage, MessageStatus.IN_PROGRESS);
        InteractiveMessage interactiveMessage = customInteractiveMessage.toInteractiveMessage();
        CometChat.sendInteractiveMessage(interactiveMessage, new CometChat.CallbackListener<InteractiveMessage>() {
            @Override
            public void onSuccess(InteractiveMessage interactiveMessage) {
                CustomInteractiveMessage customInteractiveMessage = CustomInteractiveMessage.fromInteractive(interactiveMessage);
                if (!disableLocalEvents) {
                    CometChatUIKitHelper.onMessageSent(customInteractiveMessage, MessageStatus.SUCCESS);
                }
                triggerMessageCallback(messageCallbackListener, customInteractiveMessage, null);
            }

            @Override
            public void onError(CometChatException e) {
                customInteractiveMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                if (!disableLocalEvents)
                    onMessageSent(customInteractiveMessage, MessageStatus.ERROR);
                triggerMessageCallback(messageCallbackListener, null, e);
            }
        });
    }

    public static void setLocale(Context context, @Language.Code String language) {
        CometChatLocalize.setLocale(context, language);
    }

    public static DataSource getDataSource() {
        return ChatConfigurator.getDataSource();
    }

    @NonNull
    public static ConversationUpdateSettings getConversationUpdateSettings() {
        return CometChat.getConversationUpdateSettings();
    }

    public static UIKitSettings getAuthSettings() {
        return authenticationSettings;
    }

    public static String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getSDKPackageName() {
        return BuildConfig.LIBRARY_PACKAGE_NAME;
    }
}
