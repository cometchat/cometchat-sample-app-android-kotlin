package com.cometchat.chatuikit.extensions.messagetranslation;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageOption;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MessageTranslationDecorator extends DataSourceDecorator {
    private static final String TAG = MessageTranslationDecorator.class.getSimpleName();

    public MessageTranslationDecorator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<CometChatMessageOption> getTextMessageOptions(Context context,
                                                              BaseMessage baseMessage,
                                                              Group group,
                                                              AdditionParameter additionParameter) {
        List<CometChatMessageOption> optionList = super.getTextMessageOptions(context, baseMessage, group, additionParameter);
        if (baseMessage != null && baseMessage.getDeletedAt() == 0) {
            if (additionParameter.getTranslateMessageOptionVisibility() == View.VISIBLE) optionList.add(new CometChatMessageOption(
                "MessageTranslationDecorator",
                context.getString(R.string.cometchat_translate),
                R.drawable.cometchat_ic_translate,
                () -> {
                    translateMessage(context, baseMessage, additionParameter);
                }));
        }
        return optionList;
    }

    private void translateMessage(@NonNull Context context, BaseMessage baseMessage, AdditionParameter additionParameter) {
        try {
            String localeLanguage = CometChatLocalize.getDefault().getLanguage();
            JSONObject body = new JSONObject();
            JSONArray languages = new JSONArray();
            languages.put(localeLanguage);
            body.put("msgId", baseMessage.getId());
            body.put("languages", languages);
            String originalText = String.valueOf(FormatterUtils.getFormattedText(context,
                                                                                 baseMessage,
                                                                                 UIKitConstants.FormattingType.MESSAGE_COMPOSER,
                                                                                 UIKitConstants.MessageBubbleAlignment.LEFT,
                                                                                 ((TextMessage) baseMessage).getText(),
                                                                                 ChatConfigurator
                                                                                     .getDataSource()
                                                                                     .getTextFormatters(context, additionParameter)));
            body.put("text", originalText);
            CometChat.callExtension("message-translation", "POST", "/v2/translate", body, new CometChat.CallbackListener<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    String translatedText = Extensions.getTextFromTranslatedMessage(jsonObject, originalText);
                    if (translatedText != null) {
                        if (baseMessage.getMetadata() != null) {
                            JSONObject meta = baseMessage.getMetadata();
                            try {
                                meta.put(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED, translatedText);
                                baseMessage.setMetadata(meta);
                                CometChatUIKitHelper.onMessageEdited(baseMessage, MessageStatus.SUCCESS);
                            } catch (Exception e) {
                                CometChatLogger.e(TAG, e.toString());
                            }
                        } else {
                            JSONObject meta = new JSONObject();
                            try {
                                meta.put(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED, translatedText);
                                baseMessage.setMetadata(jsonObject);
                                CometChatUIKitHelper.onMessageEdited(baseMessage, MessageStatus.SUCCESS);
                            } catch (JSONException e) {
                                CometChatLogger.e(TAG, e.toString());
                            }
                        }
                    } else {
                        showError(context, context.getString(R.string.cometchat_translation_error));
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    showError(context, context.getString(R.string.cometchat_something_went_wrong));
                }
            });
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
    }

    public void showError(Context context, String message) {
        String errorMessage = message;
        Utils.showToast(context, errorMessage, CometChatTheme.getWarningColor(context));
    }

    @Override
    public String getId() {
        return MessageTranslationDecorator.class.getSimpleName();
    }
}
