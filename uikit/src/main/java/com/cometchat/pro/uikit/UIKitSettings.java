package com.cometchat.pro.uikit;

import com.cometchat.pro.constants.CometChatConstants;

import constant.StringContract;

public class UIKitSettings {

    public static void setMessagingSoundEnable(boolean isEnable) {
        StringContract.Sounds.enableMessageSounds = isEnable;
    }

    public static void setCallSoundEnable(boolean isEnable) {
        StringContract.Sounds.enableCallSounds = isEnable;
    }

    public static boolean isMessageSoundEnable() {
        return StringContract.Sounds.enableMessageSounds;
    }

    public static boolean isCallSoundEnable() {
        return StringContract.Sounds.enableCallSounds;
    }

    public static void setHyperLinkEmailColor(int color) {
        StringContract.HyperLink.emailColor = color;
    }

    public static void setHyperLinkPhoneColor(int color) {
        StringContract.HyperLink.phoneColor = color;
    }

    public static void setHyperLinkUrlColor(int color) {
        StringContract.HyperLink.urlColor = color;
    }

    public static void showCallNotification(boolean isVisible) {
        if (!isVisible) {
            StringContract.MessageRequest.messageCategoriesForGroup
                    .remove(CometChatConstants.CATEGORY_CALL);
            StringContract.MessageRequest.messageTypesForUser
                    .remove(CometChatConstants.CATEGORY_CALL);

        } else {
            if (!StringContract.MessageRequest.messageCategoriesForGroup
                    .contains(CometChatConstants.CATEGORY_CALL)) {
                StringContract.MessageRequest.messageCategoriesForGroup
                        .add(CometChatConstants.CATEGORY_CALL);
            }
            if (!StringContract.MessageRequest.messageCategoriesForUser
                    .contains(CometChatConstants.CATEGORY_CALL)) {
                StringContract.MessageRequest.messageCategoriesForUser
                        .add(CometChatConstants.CATEGORY_CALL);
            }

        }
    }

    public static void showGroupNotification(boolean isVisible) {
        if (!isVisible) {
            StringContract.MessageRequest.messageTypesForGroup
                    .remove(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER);
        } else {
            if (StringContract.MessageRequest.messageTypesForGroup
                    .contains(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)) {
                StringContract.MessageRequest.messageTypesForGroup
                        .add(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER);
            }
        }
    }
}
