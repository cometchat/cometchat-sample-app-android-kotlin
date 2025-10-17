package com.cometchat.chatuikit.shared.utils;

import android.util.Log;

import com.cometchat.chat.enums.ModerationStatus;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.messagereceipt.Receipt;

import org.json.JSONObject;

public class MessageReceiptUtils {
    private static final String TAG = MessageReceiptUtils.class.getSimpleName();


    public static com.cometchat.chatuikit.shared.views.messagereceipt.Receipt MessageReceipt(BaseMessage baseMessage) {
        if (baseMessage != null) {
            if (baseMessage.getMetadata() != null) {
                {
                    JSONObject jsonObject = baseMessage.getMetadata();
                    try {
                        String exception = jsonObject.getString("error");
                        if (!exception.isEmpty()) return Receipt.ERROR;
                        else return getReceipt(baseMessage);
                    } catch (Exception e) {
                        return getReceipt(baseMessage);
                    }
                }
            } else {
                return getReceipt(baseMessage);
            }
        } else return Receipt.ERROR;
    }

    private static Receipt getReceipt(BaseMessage baseMessage) {
        ModerationStatus moderationStatus = Utils.getModerationStatus(baseMessage);
        if (UIKitConstants.ModerationConstants.DISAPPROVED.equals(moderationStatus)) {
            return Receipt.ERROR;
        } else if (baseMessage.getId() == 0) return Receipt.IN_PROGRESS;
        else if (baseMessage.getReadAt() != 0) return Receipt.READ;
        else if (baseMessage.getDeliveredAt() != 0) return Receipt.DELIVERED;
        else if (baseMessage.getSentAt() > 0 || UIKitConstants.ModerationConstants.PENDING.equals(moderationStatus)) return Receipt.SENT;
        else return Receipt.IN_PROGRESS;
    }

    public static boolean hideReceipt(BaseMessage baseMessage) {
        if (baseMessage != null && baseMessage.getDeletedAt() == 0) {
            User user = CometChatUIKit.getLoggedInUser();
            if (user != null) {
                if ((baseMessage.getCategory().equals(UIKitConstants.MessageCategory.MESSAGE) || baseMessage
                    .getCategory()
                    .equals(UIKitConstants.MessageCategory.INTERACTIVE)) && baseMessage.getSender() != null) {
                    return !baseMessage.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid());
                } else if (baseMessage.getMetadata() != null && baseMessage
                    .getMetadata()
                    .has("incrementUnreadCount") && baseMessage.getSender() != null) {
                    return !baseMessage.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid());
                }
            }
        }
        return true;
    }
}
