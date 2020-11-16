package com.cometchat.pro.uikit.Settings;

import com.cometchat.pro.uikit.R;

public class UISettings {

    public UISettings() {}


    //style
    protected static String color = "#03A9F4";
    //BottomBar
    protected static boolean showUsersBB = true;
    protected static boolean showGroupsBB = true;
    protected static boolean showChatsBB = true;
    protected static boolean showCallsBB = true;
    protected static boolean showUserSettingsBB = true;
    protected static String groupListing = "all";
    protected static String userListing = "all_users";
    //main
    protected static boolean enableSendingMessage = true;
    protected static boolean showReadDeliveryReceipts = true;
    protected static boolean sendEmojisLargeSize = true;
    protected static boolean sendEmojis = true;
    protected static boolean sendVoiceNotes = true;
    protected static boolean sendFiles = true;
    protected static boolean sendPolls = true;
    protected static boolean sendPhotosVideo = true;
    protected static boolean enableThreadedReplies = true;
    protected static boolean enableReplyToMessage = true;
    protected static boolean enableShareCopyForward = true;
    protected static boolean enableDeleteMessage = true;
    protected static boolean enableEditingMessage = true;
    protected static boolean shareLocation = true;
    protected static boolean sendStickers = true;
    protected static boolean blockUser = true;
    protected static boolean showTypingIndicators = true;
    protected static boolean viewSharedMedia = true;
    protected static boolean showUserPresence = true;
    protected static boolean allowPromoteDemoteMembers = true;
    protected static boolean allowBanKickMembers = true;
    protected static boolean allowAddMembers = true;
    protected static boolean allowModeratorToDeleteMessages;
    protected static boolean allowDeleteGroups = true;
    protected static boolean viewGroupMember = true;
    protected static boolean joinOrLeaveGroup = true;
    protected static boolean groupCreate = true;
    protected static boolean hideJoinLeaveNotifications = false;
    protected static boolean enableVideoCalling = true;
    protected static boolean enableVoiceCalling = true;
    protected static boolean enableMessageSounds = true;
    protected static boolean enableCallSounds = true;
    protected static boolean hideGroupNotification = false;
    protected static boolean hideCallNotification = false;
    protected static int emailColor = R.color.primaryTextColor;
    protected static int phoneColor = R.color.purple;
    protected static int urlColor = R.color.dark_blue;

    public static boolean isGroupNotificationHidden() {
        return hideGroupNotification;
    }

    public static boolean isCallNotificationHidden() {
        return hideCallNotification;
    }

    public static String getColor() {
        return color;
    }


    public static boolean isShowUsersBB() {
        return showUsersBB;
    }


    public static boolean isShowGroupsBB() {
        return showGroupsBB;
    }

    public static boolean isShowChatsBB() {
        return showChatsBB;
    }

    public static boolean isShowCallsBB() {
        return showCallsBB;
    }

    public static boolean isShowUserSettingsBB() {
        return showUserSettingsBB;
    }

    public static String getGroupListing() {
        return groupListing;
    }

    public static String getUserListing() {
        return userListing;
    }

    public static boolean isEnableSendingMessage() {
        return enableSendingMessage;
    }

    public static boolean isShowReadDeliveryReceipts() {
        return showReadDeliveryReceipts;
    }

    public static boolean isSendEmojisLargeSize() {
        return sendEmojisLargeSize;
    }

    public static boolean isSendEmojis() {
        return sendEmojis;
    }

    public static boolean isSendVoiceNotes() {
        return sendVoiceNotes;
    }

    public static boolean isSendFiles() {
        return sendFiles;
    }

    public static boolean isSendPolls() {
        return sendPolls;
    }

    public static boolean isStickerVisible() { return sendStickers; }

    public static boolean isSendPhotosVideo() {
        return sendPhotosVideo;
    }

    public static boolean isEnableThreadedReplies() {
        return enableThreadedReplies;
    }

    public static boolean isEnableReplyToMessage() {
        return enableReplyToMessage;
    }

    public static boolean isEnableShareCopyForward() {
        return enableShareCopyForward;
    }

    public static boolean isEnableDeleteMessage() {
        return enableDeleteMessage;
    }

    public static boolean isEnableEditingMessage() {
        return enableEditingMessage;
    }

    public static boolean isShareLocation() {
        return shareLocation;
    }

    public static boolean isBlockUser() {
        return blockUser;
    }

    public static boolean isShowTypingIndicators() {
        return showTypingIndicators;
    }

    public static boolean isViewSharedMedia() {
        return viewSharedMedia;
    }

    public static boolean isShowUserPresence() {
        return showUserPresence;
    }

    public static boolean isAllowPromoteDemoteMembers() {
        return allowPromoteDemoteMembers;
    }

    public static boolean isAllowBanKickMembers() {
        return allowBanKickMembers;
    }

    public static boolean isAllowAddMembers() {
        return allowAddMembers;
    }

    public static boolean isAllowModeratorToDeleteMessages() {
        return allowModeratorToDeleteMessages;
    }

    public static boolean isAllowDeleteGroups() {
        return allowDeleteGroups;
    }

    public static boolean isViewGroupMember() {
        return viewGroupMember;
    }

    public static boolean isJoinOrLeaveGroup() {
        return joinOrLeaveGroup;
    }

    public static boolean isGroupCreate() {
        return groupCreate;
    }

    public static boolean isHideJoinLeaveNotifications() {
        return hideJoinLeaveNotifications;
    }

    public static boolean isEnableVideoCalling() {
        return enableVideoCalling;
    }

    public static boolean isEnableVoiceCalling() {
        return enableVoiceCalling;
    }

    public static boolean isEnableMessageSounds() {
        return enableMessageSounds;
    }

    public static boolean isEnableCallSounds() {
        return enableCallSounds;
    }

    public static int getEmailColor() {
        return emailColor;
    }

    public static int getPhoneColor() {
        return phoneColor;
    }

    public static int getUrlColor() {
        return urlColor;
    }
}
