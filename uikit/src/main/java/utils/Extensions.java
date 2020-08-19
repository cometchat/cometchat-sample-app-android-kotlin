package utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.TextMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Extensions {

    private static final String TAG = "Extensions";
    public static boolean getImageModeration(Context context, BaseMessage baseMessage) {
        boolean result = false;
        try {
            HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
            if (extensionList != null && extensionList.containsKey("imageModeration")) {
                JSONObject imageModeration = extensionList.get("imageModeration");
                String confidence = imageModeration.getString("confidence");
                if (Integer.parseInt(confidence) > 50) {
                    result = true;
                } else {
                    result = false;
                }
            }
        }catch (Exception e) {
            Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return result;
    }

    public static String getThumbnailGeneration(Context context, BaseMessage baseMessage) {
        String resultUrl = null;
        try {
            HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
            if (extensionList != null && extensionList.containsKey("thumbnailGeneration")) {
                JSONObject thumbnailGeneration = extensionList.get("thumbnailGeneration");
                resultUrl = thumbnailGeneration.getString("url_small");
            }
        }catch (Exception e) {
            Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return resultUrl;
    }


    public static List<String> getSmartReplyList(BaseMessage baseMessage){

        HashMap<String, JSONObject> extensionList = extensionCheck(baseMessage);
        if (extensionList != null && extensionList.containsKey("smartReply")) {
            JSONObject replyObject = extensionList.get("smartReply");
            List<String> replyList = new ArrayList<>();
            try {
                replyList.add(replyObject.getString("reply_positive"));
                replyList.add(replyObject.getString("reply_neutral"));
                replyList.add(replyObject.getString("reply_negative"));
            } catch (Exception e) {
                Log.e(TAG, "onSuccess: " + e.getMessage());
            }
            return replyList;
        }
        return null;
    }


    public static HashMap<String,JSONObject> extensionCheck(BaseMessage baseMessage)
    {
        JSONObject metadata = baseMessage.getMetadata();
        HashMap<String,JSONObject> extensionMap = new HashMap<>();
        try {
            if (metadata != null) {
                JSONObject injectedObject = metadata.getJSONObject("@injected");
                if (injectedObject != null && injectedObject.has("extensions")) {
                    JSONObject extensionsObject = injectedObject.getJSONObject("extensions");
                    if (extensionsObject != null && extensionsObject.has("link-preview")) {
                        JSONObject linkPreviewObject = extensionsObject.getJSONObject("link-preview");
                        JSONArray linkPreview = linkPreviewObject.getJSONArray("links");
                        if (linkPreview.length() > 0) {
                            extensionMap.put("linkPreview",linkPreview.getJSONObject(0));
                        }

                    }
                    if (extensionsObject !=null && extensionsObject.has("smart-reply")) {
                        extensionMap.put("smartReply",extensionsObject.getJSONObject("smart-reply"));
                    }
                    if (extensionsObject!=null && extensionsObject.has("message-translation")) {
                        extensionMap.put("messageTranslation",extensionsObject.getJSONObject("message-translation"));
                    }
                    if (extensionsObject!=null && extensionsObject.has("profanity-filter")) {
                        extensionMap.put("profanityFilter",extensionsObject.getJSONObject("profanity-filter"));
                    }
                    if (extensionsObject!=null && extensionsObject.has("image-moderation")) {
                        extensionMap.put("imageModeration",extensionsObject.getJSONObject("image-moderation"));
                    }
                    if (extensionsObject!=null && extensionsObject.has("sentiment-analysis")) {
                        extensionMap.put("sentimentAnalysis",extensionsObject.getJSONObject("sentiment-analysis"));
                    }
                }
                return extensionMap;
            }
            else
                return null;
        }  catch (Exception e) {
            Log.e(TAG, "isLinkPreview: "+e.getMessage() );
        }
        return null;
    }

    public static boolean checkSentiment(BaseMessage baseMessage) {
        boolean result = false;
        HashMap<String,JSONObject> extensionList = extensionCheck(baseMessage);
        try {
            if (extensionList.containsKey("sentimentAnalysis")) {
                JSONObject sentimentAnalysis = extensionList.get("sentimentAnalysis");
                String str = sentimentAnalysis.getString("sentiment");
                if (str.equals("negative"))
                    result = true;
                else
                    result = false;
            }
        }catch (Exception e) {
            Log.e(TAG, "checkSentiment: "+e.getMessage());
        }
        return result;
    }

    public static String checkProfanityMessage(BaseMessage baseMessage) {
        String result = ((TextMessage)baseMessage).getText();
        HashMap<String,JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (extensionList!=null) {
            try {
                if (extensionList.containsKey("profanityFilter")) {
                    JSONObject profanityFilter = extensionList.get("profanityFilter");
                    String profanity = profanityFilter.getString("profanity");
                    String cleanMessage = profanityFilter.getString("message_clean");
                    if (profanity.equals("no"))
                        result = ((TextMessage)baseMessage).getText();
                    else
                        result = cleanMessage;
                } else {
                    result = ((TextMessage)baseMessage).getText().trim();
                }
            }catch (Exception e) {
                Log.e(TAG, "checkProfanityMessage:Error: "+e.getMessage() );
            }
        }
        return result;
    }
}
