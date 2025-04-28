package com.cometchat.chatuikit.extensions;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.extensions.sticker.keyboard.model.Sticker;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.views.reaction.ExtensionResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Extensions {
    public static final String linkPreview = "link-preview";
    public static final String smartReply = "smart-reply";
    public static final String messageTranslation = "message-translation";
    public static final String profanityFilter = "profanity-filter";
    public static final String imageModeration = "image-moderation";
    public static final String thumbnailGeneration = "thumbnail-generation";
    public static final String sentimentalAnalysis = "sentiment-analysis";
    public static final String polls = "polls";
    public static final String reactions = "reactions";
    public static final String whiteboard = "whiteboard";
    public static final String document = "document";
    public static final String dataMasking = "data-masking";
    public static final String stickers = "stickers";
    public static final String saveMessage = "save-message";
    public static final String pinMessage = "pin-message";
    public static final String voiceTranscription = "voice-transcription";
    public static final String richMedia = "rich-media";
    public static final String malwareScanner = "virus-malware-scanner";
    public static final String mentions = "mentions";
    private static final String TAG = Extensions.class.getSimpleName();

    public static boolean isImageModerated(Context context, BaseMessage baseMessage) {
        boolean result = false;
        try {
            HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
            if (extensionList != null && extensionList.containsKey("imageModeration")) {
                JSONObject imageModeration = extensionList.get("imageModeration");
                if (imageModeration != null && imageModeration.has("unsafe")) {
                    String unsafe = imageModeration.getString("unsafe");
                    result = unsafe.equals("yes");
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return result;
    }

    public static HashMap<String, JSONObject> extensionCheck(BaseMessage baseMessage) {
        JSONObject metadata = baseMessage.getMetadata();
        HashMap<String, JSONObject> extensionMap = new HashMap<>();
        try {
            if (metadata != null) {
                JSONObject injectedObject = metadata.getJSONObject("@injected");
                if (injectedObject.has("extensions")) {
                    JSONObject extensionsObject = injectedObject.getJSONObject("extensions");
                    if (extensionsObject.has(linkPreview)) {
                        JSONObject linkPreviewObject = extensionsObject.getJSONObject(linkPreview);
                        JSONArray linkPreview = linkPreviewObject.getJSONArray("links");
                        if (linkPreview.length() > 0) {
                            extensionMap.put("linkPreview", linkPreview.getJSONObject(0));
                        }
                    }
                    if (extensionsObject.has(smartReply)) {
                        extensionMap.put("smartReply", extensionsObject.getJSONObject(smartReply));
                    }
                    if (extensionsObject.has(messageTranslation)) {
                        extensionMap.put("messageTranslation", extensionsObject.getJSONObject(messageTranslation));
                    }
                    if (extensionsObject.has(profanityFilter)) {
                        extensionMap.put("profanityFilter", extensionsObject.getJSONObject(profanityFilter));
                    }
                    if (extensionsObject.has(imageModeration)) {
                        extensionMap.put("imageModeration", extensionsObject.getJSONObject(imageModeration));
                    }
                    if (extensionsObject.has(thumbnailGeneration)) {
                        extensionMap.put("thumbnailGeneration", extensionsObject.getJSONObject(thumbnailGeneration));
                    }
                    if (extensionsObject.has(sentimentalAnalysis)) {
                        extensionMap.put("sentimentAnalysis", extensionsObject.getJSONObject(sentimentalAnalysis));
                    }
                    if (extensionsObject.has(polls)) {
                        extensionMap.put("polls", extensionsObject.getJSONObject(polls));
                    }
                    if (extensionsObject.has(reactions)) {
                        if (extensionsObject.get(reactions) instanceof JSONObject)
                            extensionMap.put("reactions", extensionsObject.getJSONObject(reactions));
                    }
                    if (extensionsObject.has(whiteboard)) {
                        extensionMap.put("whiteboard", extensionsObject.getJSONObject(whiteboard));
                    }
                    if (extensionsObject.has(document)) {
                        extensionMap.put("document", extensionsObject.getJSONObject(document));
                    }
                    if (extensionsObject.has(dataMasking)) {
                        extensionMap.put("dataMasking", extensionsObject.getJSONObject(dataMasking));
                    }
                }
                return extensionMap;
            } else return null;
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * Below method is used to get a thumbnail generated by thumbnail generation
     * extension. It takes baseMessage as a parameter and check metadata of it. It
     * check whether it contains Thumbnail Generation metadata or not if yes then it
     * will take 'url_small' from its response and return it as result;
     *
     * @param baseMessage object of BaseMessage
     * @return is a String which contains url_small
     */
    public static String getThumbnailUrl(BaseMessage baseMessage) {
        String resultUrl = null;
        try {
            HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
            if (extensionList != null && extensionList.containsKey("thumbnailGeneration")) {
                JSONObject thumbnailGeneration = extensionList.get("thumbnailGeneration");
                if (thumbnailGeneration != null) {
                    resultUrl = thumbnailGeneration.getString("url_medium");
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return resultUrl;
    }

    public static List<String> getSmartReplyList(@NonNull BaseMessage baseMessage) {
        HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
        List<String> replyList = new ArrayList<>();
        if (extensionList != null && extensionList.containsKey("smartReply")) {
            JSONObject replyObject = extensionList.get("smartReply");
            if (replyObject != null) {
                try {
                    replyList.add(replyObject.getString("reply_positive"));
                    replyList.add(replyObject.getString("reply_neutral"));
                    replyList.add(replyObject.getString("reply_negative"));
                } catch (Exception e) {
                    CometChatLogger.e(TAG, e.toString());
                }
            }
        }
        return replyList;
    }

    public static boolean checkSentiment(BaseMessage baseMessage) {
        boolean result = false;
        HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
        try {
            if (extensionList != null && extensionList.containsKey("sentimentAnalysis")) {
                JSONObject sentimentAnalysis = extensionList.get("sentimentAnalysis");
                if (sentimentAnalysis != null) {
                    String str = sentimentAnalysis.getString("sentiment");
                    result = str.equals("negative");
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return result;
    }

    /**
     * Below method checks whether baseMessage contains profanity or not using
     * Profanity Filter extension. If yes then it will return clean message else the
     * orignal message will be return.
     *
     * @param baseMessage is object of BaseMessage
     * @return a String value.
     */
    public static String checkProfanityMessage(Context context, BaseMessage baseMessage) {
        String result = ((TextMessage) baseMessage).getText();
        HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (extensionList != null) {
            try {
                if (extensionList.containsKey("profanityFilter")) {
                    JSONObject profanityFilter = extensionList.get("profanityFilter");
                    if (profanityFilter != null) {
                        String profanity = profanityFilter.getString("profanity");
                        String cleanMessage = profanityFilter.getString("message_clean");
                        if (profanity.equals("no")) result = ((TextMessage) baseMessage).getText();
                        else result = cleanMessage;
                    }
                } else {
                    result = ((TextMessage) baseMessage).getText().trim();
                }
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
        return result;
    }

    public static String checkDataMasking(Context context, BaseMessage baseMessage) {
        String result = ((TextMessage) baseMessage).getText();
        String sensitiveData;
        String messageMasked;
        HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (extensionList != null) {
            try {
                if (extensionList.containsKey("dataMasking")) {
                    JSONObject dataMasking = extensionList.get("dataMasking");
                    if (dataMasking != null) {
                        JSONObject dataObject = dataMasking.getJSONObject("data");
                        if (dataObject.has("sensitive_data") && dataObject.has("message_masked")) {
                            sensitiveData = dataObject.getString("sensitive_data");
                            messageMasked = dataObject.getString("message_masked");
                            if (sensitiveData.equals("no"))
                                result = ((TextMessage) baseMessage).getText();
                            else result = messageMasked;
                        } else if (dataObject.has("action") && dataObject.has("message")) {
                            result = dataObject.getString("message");
                        }
                    }
                } else {
                    result = ((TextMessage) baseMessage).getText();
                }
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
        return result;
    }

    public static int userVotedOn(BaseMessage baseMessage, int totalOptions, String loggedInUserId) {
        int result = 0;
        JSONObject resultJson = getPollsResult(baseMessage);
        try {
            if (resultJson.has("options")) {
                JSONObject options = resultJson.getJSONObject("options");
                for (int k = 0; k < totalOptions; k++) {
                    JSONObject option = options.getJSONObject(String.valueOf(k + 1));
                    if (option.has("voters") && option.get("voters") instanceof JSONObject) {
                        JSONObject voterList = option.getJSONObject("voters");
                        if (voterList.has(loggedInUserId)) {
                            result = k + 1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return result;
    }

    public static JSONObject getPollsResult(BaseMessage baseMessage) {
        JSONObject result = new JSONObject();
        HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (extensionList != null) {
            try {
                if (extensionList.containsKey("polls")) {
                    JSONObject polls = extensionList.get("polls");
                    if (polls != null && polls.has("results")) {
                        result = polls.getJSONObject("results");
                    }
                }
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
        return result;
    }

    public static int getVoteCount(BaseMessage baseMessage) {
        int voteCount = 0;
        JSONObject result = getPollsResult(baseMessage);
        try {

            if (result.has("total")) {
                voteCount = result.getInt("total");
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return voteCount;
    }

    @NonNull
    public static ArrayList<String> getVoterInfo(BaseMessage baseMessage, int totalOptions) {
        ArrayList<String> votes = new ArrayList<>();
        JSONObject result = getPollsResult(baseMessage);
        try {
            if (result.has("options")) {
                JSONObject options = result.getJSONObject("options");
                for (int k = 0; k < totalOptions; k++) {
                    JSONObject optionK = options.getJSONObject(String.valueOf(k + 1));
                    votes.add(optionK.getString("count"));
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return votes;
    }

    public static void fetchStickers(ExtensionResponseListener extensionResponseListener) {
        if (CometChat.isExtensionEnabled(stickers)) {
            CometChat.callExtension("stickers", "GET", "/v1/fetch", null, new CometChat.CallbackListener<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    extensionResponseListener.OnResponseSuccess(jsonObject);
                }

                @Override
                public void onError(CometChatException e) {
                    extensionResponseListener.OnResponseFailed(e);
                }
            });
        } else {
            extensionResponseListener.OnResponseFailed(new CometChatException("ERR_EXTENSION_NOT_ENABLED",
                                                                              "Enable the extension from CometChat Pro dashboard",
                                                                              "stickers"));
        }
    }

    public static HashMap<String, List<Sticker>> extractStickersFromJSON(JSONObject jsonObject) {
        List<Sticker> stickers = new ArrayList<>();
        if (jsonObject != null) {
            try {
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONArray defaultStickersArray = dataObject.getJSONArray("defaultStickers");
                Log.d(TAG, "getStickersList: defaultStickersArray " + defaultStickersArray.length());
                for (int i = 0; i < defaultStickersArray.length(); i++) {
                    JSONObject stickerObject = defaultStickersArray.getJSONObject(i);
                    String stickerOrder = stickerObject.getString("stickerOrder");
                    String stickerSetId = stickerObject.getString("stickerSetId");
                    String stickerUrl = stickerObject.getString("stickerUrl");
                    String stickerSetName = stickerObject.getString("stickerSetName");
                    String stickerName = stickerObject.getString("stickerName");
                    Sticker sticker = new Sticker(stickerName, stickerUrl, stickerSetName);
                    stickers.add(sticker);
                }
                if (dataObject.has("customStickers")) {
                    JSONArray customSticker = dataObject.getJSONArray("customStickers");
                    Log.d(TAG, "getStickersList: customStickersArray " + customSticker);
                    for (int i = 0; i < customSticker.length(); i++) {
                        JSONObject stickerObject = customSticker.getJSONObject(i);
                        String stickerOrder = stickerObject.getString("stickerOrder");
                        String stickerSetId = stickerObject.getString("stickerSetId");
                        String stickerUrl = stickerObject.getString("stickerUrl");
                        String stickerSetName = stickerObject.getString("stickerSetName");
                        String stickerName = stickerObject.getString("stickerName");
                        Sticker sticker = new Sticker(stickerName, stickerUrl, stickerSetName);
                        stickers.add(sticker);
                    }
                }

            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }

        HashMap<String, List<Sticker>> stickerMap = new HashMap<>();
        for (int i = 0; i < stickers.size(); i++) {
            String setName = stickers.get(i).getSetName();
            stickerMap.computeIfAbsent(setName, k -> new ArrayList<>()).add(stickers.get(i));
        }

        return stickerMap;
    }

    public static HashMap<String, String> getReactionsOnMessage(BaseMessage baseMessage) {
        HashMap<String, String> result = new HashMap<>();
        HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (extensionList != null) {
            try {
                if (extensionList.containsKey("reactions")) {
                    JSONObject data = extensionList.get("reactions");
                    if (data != null) {
                        Iterator<String> keys = data.keys();
                        while (keys.hasNext()) {
                            String keyValue = keys.next();
                            JSONObject react = data.getJSONObject(keyValue);
                            String reactCount = react.length() + "";
                            result.put(keyValue, reactCount);
                        }
                    }
                }
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
        return result;
    }

    public static void callWriteBoardExtension(String receiverId, String receiverType, ExtensionResponseListener extensionResponseListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("receiver", receiverId);
            jsonObject.put("receiverType", receiverType);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        CometChat.callExtension("document", "POST", "/v1/create", jsonObject, new CometChat.CallbackListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                extensionResponseListener.OnResponseSuccess(jsonObject);
            }

            @Override
            public void onError(CometChatException e) {
                extensionResponseListener.OnResponseFailed(e);
            }
        });
    }

    public static void callWhiteBoardExtension(String receiverId, String receiverType, ExtensionResponseListener extensionResponseListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("receiver", receiverId);
            jsonObject.put("receiverType", receiverType);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        CometChat.callExtension("whiteboard", "POST", "/v1/create", jsonObject, new CometChat.CallbackListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                extensionResponseListener.OnResponseSuccess(jsonObject);
            }

            @Override
            public void onError(CometChatException e) {
                extensionResponseListener.OnResponseFailed(e);
            }
        });
    }

    public static String getWhiteBoardUrl(BaseMessage baseMessage) {
        String boardUrl = "";
        HashMap<String, JSONObject> extensionCheck = extensionCheck(baseMessage);
        if (extensionCheck != null && extensionCheck.containsKey("whiteboard")) {
            JSONObject whiteBoardData = extensionCheck.get("whiteboard");
            if (whiteBoardData != null && whiteBoardData.has("board_url")) {
                try {
                    boardUrl = whiteBoardData.getString("board_url");
                    String userName = CometChatUIKit.getLoggedInUser().getName().replace("//s+", "_");
                    boardUrl = boardUrl + "&username=" + userName;
                } catch (Exception e) {
                    CometChatLogger.e(TAG, e.toString());
                }
            }
        }
        return boardUrl;
    }

    public static String getWriteBoardUrl(BaseMessage baseMessage) {
        String boardUrl = "";
        HashMap<String, JSONObject> extensionCheck = extensionCheck(baseMessage);
        if (extensionCheck != null && extensionCheck.containsKey("document")) {
            JSONObject whiteBoardData = extensionCheck.get("document");
            if (whiteBoardData != null && whiteBoardData.has("document_url")) {
                try {
                    boardUrl = whiteBoardData.getString("document_url");
                } catch (Exception e) {
                    CometChatLogger.e(TAG, e.toString());
                }
            }
        }
        return boardUrl;
    }

    public static String getTranslatedMessage(BaseMessage baseMessage) {
        String translatedMessage = ((TextMessage) baseMessage).getText();
        try {
            if (baseMessage.getMetadata() != null) {
                JSONObject metadataObject = baseMessage.getMetadata();
                if (metadataObject.has("values")) {
                    JSONObject valueObject = metadataObject.getJSONArray("values").getJSONObject(0);
                    if (valueObject.has("data")) {
                        JSONObject dataObject = valueObject.getJSONObject("data");
                        if (dataObject.has("translations")) {
                            JSONArray translations = dataObject.getJSONArray("translations");
                            if (translations.length() > 0) {
                                JSONObject jsonObject = translations.getJSONObject(0);
                                translatedMessage = jsonObject.getString("message_translated");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return translatedMessage;
    }

    public static List<String> translateSmartReplyMessage(BaseMessage baseMessage,
                                                          JSONObject replyObject,
                                                          ExtensionResponseListener extensionResponseListener) {
        List<String> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < replyObject.length(); i++) {
                String localeLanguage = CometChatLocalize.getDefault().getLanguage();
                JSONObject body = new JSONObject();
                JSONArray languages = new JSONArray();
                languages.put(localeLanguage);
                body.put("msgId", baseMessage.getId());
                body.put("languages", languages);
                if (i == 0) body.put("text", replyObject.getString("reply_positive"));
                else if (i == 1) body.put("text", replyObject.getString("reply_neutral"));
                else body.put("text", replyObject.getString("reply_negative"));

                final String str = body.getString("text");
                CometChat.callExtension("message-translation", "POST", "/v2/translate", body, new CometChat.CallbackListener<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        if (Extensions.isMessageTranslated(jsonObject, str)) {
                            String translatedMessage = Extensions.getTextFromTranslatedMessage(jsonObject, str);
                            resultList.add(translatedMessage);
                        }
                    }

                    @Override
                    public void onError(CometChatException e) {
                    }
                });
            }
            extensionResponseListener.OnResponseSuccess(resultList);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
            extensionResponseListener.OnResponseFailed(new CometChatException("ERR_TRANSLATION_FAILED", "Not able to translate smart reply"));
        }
        return resultList;
    }

    public static boolean isMessageTranslated(JSONObject jsonObject, String txtMessage) {
        boolean result = false;
        String translatedText = null;
        try {
            JSONObject metadataObject = jsonObject;
            if (metadataObject.has("data")) {
                JSONObject dataObject = metadataObject.getJSONObject("data");
                if (dataObject.has("translations")) {
                    JSONArray translations = dataObject.getJSONArray("translations");
                    if (translations.length() > 0) {
                        JSONObject translationsJSONObject = translations.getJSONObject(0);
                        String language = translationsJSONObject.getString("language_translated");
                        String localLanguage = CometChatLocalize.getDefault().getLanguage();
                        String translatedMessage = translationsJSONObject.getString("message_translated");
                        result = language.equalsIgnoreCase(localLanguage) && !txtMessage.equalsIgnoreCase(translatedMessage);
                    }
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }

        return result;
    }

    public static String getTextFromTranslatedMessage(JSONObject jsonObject, String originalString) {
        String result = null;
        try {
            JSONObject metadataObject = jsonObject;
            if (metadataObject.has("data")) {
                JSONObject dataObject = metadataObject.getJSONObject("data");
                if (dataObject.has("translations")) {
                    JSONArray translations = dataObject.getJSONArray("translations");
                    if (translations.length() > 0) {
                        JSONObject translationsJSONObject = translations.getJSONObject(0);
                        String language = translationsJSONObject.getString("language_translated");
                        String localLanguage = CometChatLocalize.getDefault().getLanguage();
                        String translatedMessage = translationsJSONObject.getString("message_translated");
                        if (language.equalsIgnoreCase(localLanguage) && !originalString.equalsIgnoreCase(translatedMessage)) {
                            result = translatedMessage;
                        }
                    }
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return result;
    }

}
