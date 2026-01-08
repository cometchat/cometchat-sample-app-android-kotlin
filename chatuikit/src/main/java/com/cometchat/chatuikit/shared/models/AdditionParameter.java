package com.cometchat.chatuikit.shared.models;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;

import java.util.List;

public class AdditionParameter {
    private static final String TAG = AdditionParameter.class.getSimpleName();

    private List<CometChatTextFormatter> textFormatters;
    private int videoCallButtonVisibility = View.VISIBLE;
    private int voiceCallButtonVisibility = View.VISIBLE;
    private int imageAttachmentOptionVisibility = View.VISIBLE;
    private int cameraAttachmentOptionVisibility = View.VISIBLE;
    private int videoAttachmentOptionVisibility = View.VISIBLE;
    private int audioAttachmentOptionVisibility = View.VISIBLE;
    private int fileAttachmentOptionVisibility = View.VISIBLE;
    private int pollAttachmentOptionVisibility = View.VISIBLE;
    private int collaborativeDocumentOptionVisibility = View.VISIBLE;
    private int collaborativeWhiteboardOptionVisibility = View.VISIBLE;
    private int attachmentButtonVisibility = View.VISIBLE;
    private int voiceNoteButtonVisibility = View.VISIBLE;
    private int stickersButtonVisibility = View.VISIBLE;

    private int replyInThreadOptionVisibility = View.VISIBLE;
    private int replyToMessageOptionVisibility = View.VISIBLE;
    private int translateMessageOptionVisibility = View.VISIBLE;
    private int copyMessageOptionVisibility = View.VISIBLE;
    private int editMessageOptionVisibility = View.VISIBLE;
    private int shareMessageOptionVisibility = View.VISIBLE;
    private int messagePrivatelyOptionVisibility = View.VISIBLE;
    private int deleteMessageOptionVisibility = View.VISIBLE;
    private int messageInfoOptionVisibility = View.VISIBLE;
    private int groupActionMessageVisibility = View.VISIBLE;
    private int moderationViewVisibility = View.VISIBLE;
    private int flagOptionVisibility = View.VISIBLE;
    private int markUnreadOptionVisibility = View.GONE;

    private OnItemClick<BaseMessage> onMessagePreviewClick;

    private @StyleRes int incomingDeleteBubbleStyle;
    private @StyleRes int outgoingDeleteBubbleStyle;

    private @StyleRes int incomingTextBubbleStyle;
    private @StyleRes int outgoingTextBubbleStyle;

    private @StyleRes int incomingImageBubbleStyle;
    private @StyleRes int outgoingImageBubbleStyle;

    private @StyleRes int incomingFileBubbleStyle;
    private @StyleRes int outgoingFileBubbleStyle;

    private @StyleRes int incomingAudioBubbleStyle;
    private @StyleRes int outgoingAudioBubbleStyle;

    private @StyleRes int incomingVideoBubbleStyle;
    private @StyleRes int outgoingVideoBubbleStyle;

    private @StyleRes int incomingCollaborativeBubbleStyle;
    private @StyleRes int outgoingCollaborativeBubbleStyle;

    private @StyleRes int incomingFormBubbleStyle;
    private @StyleRes int outgoingFormBubbleStyle;

    private @StyleRes int incomingPollBubbleStyle;
    private @StyleRes int outgoingPollBubbleStyle;

    private @StyleRes int incomingSchedulerBubbleStyle;
    private @StyleRes int outgoingSchedulerBubbleStyle;

    private @StyleRes int incomingCardBubbleStyle;
    private @StyleRes int outgoingCardBubbleStyle;

    private @StyleRes int incomingMeetCallBubbleStyle;
    private @StyleRes int outgoingMeetCallBubbleStyle;
    private @StyleRes int callButtonStyle;

    private @StyleRes int actionBubbleStyle;
    private @StyleRes int callActionBubbleStyle;

    private @StyleRes int aiConversationStarterStyle;
    private @StyleRes int aiSmartRepliesStyle;
    private @StyleRes int aiConversationSummaryStyle;

    private Drawable inactiveStickerIcon;
    private @ColorInt int inactiveAuxiliaryIconTint;
    private Drawable activeStickerIcon;
    private @ColorInt int activeAuxiliaryIconTint;
    private @StyleRes int aiAssistantMessageBubbleStyle;
    private @StyleRes int outgoingMessagePreviewStyle;
    private @StyleRes int incomingMessagePreviewStyle;

    public @StyleRes int getIncomingTextBubbleStyle() {
        return incomingTextBubbleStyle;
    }

    public void setIncomingTextBubbleStyle(@StyleRes int incomingTextBubbleStyle) {
        this.incomingTextBubbleStyle = incomingTextBubbleStyle;
    }

    public @StyleRes int getOutgoingTextBubbleStyle() {
        return outgoingTextBubbleStyle;
    }

    public void setOutgoingTextBubbleStyle(@StyleRes int outgoingTextBubbleStyle) {
        this.outgoingTextBubbleStyle = outgoingTextBubbleStyle;
    }

    public @StyleRes int getIncomingImageBubbleStyle() {
        return incomingImageBubbleStyle;
    }

    public void setIncomingImageBubbleStyle(@StyleRes int incomingImageBubbleStyle) {
        this.incomingImageBubbleStyle = incomingImageBubbleStyle;
    }

    public @StyleRes int getOutgoingImageBubbleStyle() {
        return outgoingImageBubbleStyle;
    }

    public void setOutgoingImageBubbleStyle(@StyleRes int outgoingImageBubbleStyle) {
        this.outgoingImageBubbleStyle = outgoingImageBubbleStyle;
    }

    public @StyleRes int getIncomingFileBubbleStyle() {
        return incomingFileBubbleStyle;
    }

    public void setIncomingFileBubbleStyle(@StyleRes int incomingFileBubbleStyle) {
        this.incomingFileBubbleStyle = incomingFileBubbleStyle;
    }

    public @StyleRes int getOutgoingFileBubbleStyle() {
        return outgoingFileBubbleStyle;
    }

    public void setOutgoingFileBubbleStyle(@StyleRes int outgoingFileBubbleStyle) {
        this.outgoingFileBubbleStyle = outgoingFileBubbleStyle;
    }

    public @StyleRes int getIncomingAudioBubbleStyle() {
        return incomingAudioBubbleStyle;
    }

    public void setIncomingAudioBubbleStyle(@StyleRes int incomingAudioBubbleStyle) {
        this.incomingAudioBubbleStyle = incomingAudioBubbleStyle;
    }

    public @StyleRes int getOutgoingAudioBubbleStyle() {
        return outgoingAudioBubbleStyle;
    }

    public void setOutgoingAudioBubbleStyle(@StyleRes int outgoingAudioBubbleStyle) {
        this.outgoingAudioBubbleStyle = outgoingAudioBubbleStyle;
    }

    public @StyleRes int getIncomingVideoBubbleStyle() {
        return incomingVideoBubbleStyle;
    }

    public void setIncomingVideoBubbleStyle(@StyleRes int incomingVideoBubbleStyle) {
        this.incomingVideoBubbleStyle = incomingVideoBubbleStyle;
    }

    public @StyleRes int getOutgoingVideoBubbleStyle() {
        return outgoingVideoBubbleStyle;
    }

    public void setOutgoingVideoBubbleStyle(@StyleRes int outgoingVideoBubbleStyle) {
        this.outgoingVideoBubbleStyle = outgoingVideoBubbleStyle;
    }

    public @StyleRes int getIncomingCollaborativeBubbleStyle() {
        return incomingCollaborativeBubbleStyle;
    }

    public void setIncomingCollaborativeBubbleStyle(@StyleRes int incomingCollaborativeBubbleStyle) {
        this.incomingCollaborativeBubbleStyle = incomingCollaborativeBubbleStyle;
    }

    public @StyleRes int getOutgoingCollaborativeBubbleStyle() {
        return outgoingCollaborativeBubbleStyle;
    }

    public void setOutgoingCollaborativeBubbleStyle(@StyleRes int outgoingCollaborativeBubbleStyle) {
        this.outgoingCollaborativeBubbleStyle = outgoingCollaborativeBubbleStyle;
    }

    public @StyleRes int getIncomingFormBubbleStyle() {
        return incomingFormBubbleStyle;
    }

    public void setIncomingFormBubbleStyle(@StyleRes int incomingFormBubbleStyle) {
        this.incomingFormBubbleStyle = incomingFormBubbleStyle;
    }

    public @StyleRes int getOutgoingFormBubbleStyle() {
        return outgoingFormBubbleStyle;
    }

    public void setOutgoingFormBubbleStyle(@StyleRes int outgoingFormBubbleStyle) {
        this.outgoingFormBubbleStyle = outgoingFormBubbleStyle;
    }

    public @StyleRes int getIncomingPollBubbleStyle() {
        return incomingPollBubbleStyle;
    }

    public void setIncomingPollBubbleStyle(@StyleRes int incomingPollBubbleStyle) {
        this.incomingPollBubbleStyle = incomingPollBubbleStyle;
    }

    public @StyleRes int getOutgoingPollBubbleStyle() {
        return outgoingPollBubbleStyle;
    }

    public void setOutgoingPollBubbleStyle(@StyleRes int outgoingPollBubbleStyle) {
        this.outgoingPollBubbleStyle = outgoingPollBubbleStyle;
    }

    public @StyleRes int getIncomingSchedulerBubbleStyle() {
        return incomingSchedulerBubbleStyle;
    }

    public void setIncomingSchedulerBubbleStyle(@StyleRes int incomingSchedulerBubbleStyle) {
        this.incomingSchedulerBubbleStyle = incomingSchedulerBubbleStyle;
    }

    public @StyleRes int getOutgoingSchedulerBubbleStyle() {
        return outgoingSchedulerBubbleStyle;
    }

    public void setOutgoingSchedulerBubbleStyle(@StyleRes int outgoingSchedulerBubbleStyle) {
        this.outgoingSchedulerBubbleStyle = outgoingSchedulerBubbleStyle;
    }

    public @StyleRes int getIncomingCardBubbleStyle() {
        return incomingCardBubbleStyle;
    }

    public void setIncomingCardBubbleStyle(@StyleRes int incomingCardBubbleStyle) {
        this.incomingCardBubbleStyle = incomingCardBubbleStyle;
    }

    public @StyleRes int getOutgoingCardBubbleStyle() {
        return outgoingCardBubbleStyle;
    }

    public void setOutgoingCardBubbleStyle(@StyleRes int outgoingCardBubbleStyle) {
        this.outgoingCardBubbleStyle = outgoingCardBubbleStyle;
    }

    public @StyleRes int getIncomingDeleteBubbleStyle() {
        return incomingDeleteBubbleStyle;
    }

    public void setIncomingDeleteBubbleStyle(@StyleRes int incomingDeleteBubbleStyle) {
        this.incomingDeleteBubbleStyle = incomingDeleteBubbleStyle;
    }

    public @StyleRes int getOutgoingDeleteBubbleStyle() {
        return outgoingDeleteBubbleStyle;
    }

    public void setOutgoingDeleteBubbleStyle(@StyleRes int outgoingDeleteBubbleStyle) {
        this.outgoingDeleteBubbleStyle = outgoingDeleteBubbleStyle;
    }

    public @StyleRes int getActionBubbleStyle() {
        return actionBubbleStyle;
    }

    public void setActionBubbleStyle(@StyleRes int actionBubbleStyle) {
        this.actionBubbleStyle = actionBubbleStyle;
    }

    public @StyleRes int getIncomingMeetCallBubbleStyle() {
        return incomingMeetCallBubbleStyle;
    }

    public void setIncomingMeetCallBubbleStyle(@StyleRes int incomingMeetCallBubbleStyle) {
        this.incomingMeetCallBubbleStyle = incomingMeetCallBubbleStyle;
    }

    public @StyleRes int getCallButtonStyle() {
        return callButtonStyle;
    }

    public void setCallButtonStyle(@StyleRes int callButtonStyle) {
        this.callButtonStyle = callButtonStyle;
    }

    public @StyleRes int getOutgoingMeetCallBubbleStyle() {
        return outgoingMeetCallBubbleStyle;
    }

    public void setOutgoingMeetCallBubbleStyle(@StyleRes int outgoingMeetCallBubbleStyle) {
        this.outgoingMeetCallBubbleStyle = outgoingMeetCallBubbleStyle;
    }

    public Drawable getInactiveStickerIcon() {
        return inactiveStickerIcon;
    }

    public void setInactiveStickerIcon(Drawable inactiveStickerIcon) {
        this.inactiveStickerIcon = inactiveStickerIcon;
    }

    public @ColorInt int getInactiveAuxiliaryIconTint() {
        return inactiveAuxiliaryIconTint;
    }

    public void setInactiveAuxiliaryIconTint(@ColorInt int inactiveAuxiliaryIconTint) {
        this.inactiveAuxiliaryIconTint = inactiveAuxiliaryIconTint;
    }

    public @StyleRes int getCallActionBubbleStyle() {
        return callActionBubbleStyle;
    }

    public void setCallActionBubbleStyle(@StyleRes int callActionBubbleStyle) {
        this.callActionBubbleStyle = callActionBubbleStyle;
    }

    public Drawable getActiveStickerIcon() {
        return activeStickerIcon;
    }

    public void setActiveStickerIcon(Drawable activeStickerIcon) {
        this.activeStickerIcon = activeStickerIcon;
    }

    public @ColorInt int getActiveAuxiliaryIconTint() {
        return activeAuxiliaryIconTint;
    }

    public void setActiveAuxiliaryIconTint(@ColorInt int activeAuxiliaryIconTint) {
        this.activeAuxiliaryIconTint = activeAuxiliaryIconTint;
    }

    public @StyleRes int getAiConversationStarterStyle() {
        return aiConversationStarterStyle;
    }

    public void setAiConversationStarterStyle(@StyleRes int aiConversationStarterStyle) {
        this.aiConversationStarterStyle = aiConversationStarterStyle;
    }

    public @StyleRes int getAiSmartRepliesStyle() {
        return aiSmartRepliesStyle;
    }

    public void setAiSmartRepliesStyle(@StyleRes int aiSmartRepliesStyle) {
        this.aiSmartRepliesStyle = aiSmartRepliesStyle;
    }

    public @StyleRes int getAiConversationSummaryStyle() {
        return aiConversationSummaryStyle;
    }

    public void setAiConversationSummaryStyle(@StyleRes int aiConversationSummaryStyle) {
        this.aiConversationSummaryStyle = aiConversationSummaryStyle;
    }

    public List<CometChatTextFormatter> getTextFormatters() {
        return textFormatters;
    }

    public void setTextFormatters(List<CometChatTextFormatter> textFormatters) {
        this.textFormatters = textFormatters;
    }

    public int getVideoCallButtonVisibility() {
        return videoCallButtonVisibility;
    }

    public void setVideoCallButtonVisibility(int videoCallButtonVisibility) {
        this.videoCallButtonVisibility = videoCallButtonVisibility;
    }

    public int getVoiceCallButtonVisibility() {
        return voiceCallButtonVisibility;
    }

    public void setVoiceCallButtonVisibility(int voiceCallButtonVisibility) {
        this.voiceCallButtonVisibility = voiceCallButtonVisibility;
    }

    public int getImageAttachmentOptionVisibility() {
        return imageAttachmentOptionVisibility;
    }

    public void setImageAttachmentOptionVisibility(int imageAttachmentOptionVisibility) {
        this.imageAttachmentOptionVisibility = imageAttachmentOptionVisibility;
    }

    public int getCameraAttachmentOptionVisibility() {
        return cameraAttachmentOptionVisibility;
    }

    public void setCameraAttachmentOptionVisibility(int cameraAttachmentOptionVisibility) {
        this.cameraAttachmentOptionVisibility = cameraAttachmentOptionVisibility;
    }

    public int getVideoAttachmentOptionVisibility() {
        return videoAttachmentOptionVisibility;
    }

    public void setVideoAttachmentOptionVisibility(int videoAttachmentOptionVisibility) {
        this.videoAttachmentOptionVisibility = videoAttachmentOptionVisibility;
    }

    public int getAudioAttachmentOptionVisibility() {
        return audioAttachmentOptionVisibility;
    }

    public void setAudioAttachmentOptionVisibility(int audioAttachmentOptionVisibility) {
        this.audioAttachmentOptionVisibility = audioAttachmentOptionVisibility;
    }

    public int getFileAttachmentOptionVisibility() {
        return fileAttachmentOptionVisibility;
    }

    public void setFileAttachmentOptionVisibility(int fileAttachmentOptionVisibility) {
        this.fileAttachmentOptionVisibility = fileAttachmentOptionVisibility;
    }

    public int getPollAttachmentOptionVisibility() {
        return pollAttachmentOptionVisibility;
    }

    public void setPollAttachmentOptionVisibility(int pollAttachmentOptionVisibility) {
        this.pollAttachmentOptionVisibility = pollAttachmentOptionVisibility;
    }

    public int getCollaborativeDocumentOptionVisibility() {
        return collaborativeDocumentOptionVisibility;
    }

    public void setCollaborativeDocumentOptionVisibility(int collaborativeDocumentOptionVisibility) {
        this.collaborativeDocumentOptionVisibility = collaborativeDocumentOptionVisibility;
    }

    public int getCollaborativeWhiteboardOptionVisibility() {
        return collaborativeWhiteboardOptionVisibility;
    }

    public void setCollaborativeWhiteboardOptionVisibility(int collaborativeWhiteboardOptionVisibility) {
        this.collaborativeWhiteboardOptionVisibility = collaborativeWhiteboardOptionVisibility;
    }

    public int getAttachmentButtonVisibility() {
        return attachmentButtonVisibility;
    }

    public void setAttachmentButtonVisibility(int attachmentButtonVisibility) {
        this.attachmentButtonVisibility = attachmentButtonVisibility;
    }

    public int getVoiceNoteButtonVisibility() {
        return voiceNoteButtonVisibility;
    }

    public void setVoiceNoteButtonVisibility(int voiceNoteButtonVisibility) {
        this.voiceNoteButtonVisibility = voiceNoteButtonVisibility;
    }

    public int getStickersButtonVisibility() {
        return stickersButtonVisibility;
    }

    public void setStickersButtonVisibility(int stickersButtonVisibility) {
        this.stickersButtonVisibility = stickersButtonVisibility;
    }

    public int getReplyInThreadOptionVisibility() {
        return replyInThreadOptionVisibility;
    }

    public void setReplyInThreadOptionVisibility(int replyInThreadOptionVisibility) {
        this.replyInThreadOptionVisibility = replyInThreadOptionVisibility;
    }

    public int getTranslateMessageOptionVisibility() {
        return translateMessageOptionVisibility;
    }

    public void setTranslateMessageOptionVisibility(int translateMessageOptionVisibility) {
        this.translateMessageOptionVisibility = translateMessageOptionVisibility;
    }

    public int getCopyMessageOptionVisibility() {
        return copyMessageOptionVisibility;
    }

    public void setCopyMessageOptionVisibility(int copyMessageOptionVisibility) {
        this.copyMessageOptionVisibility = copyMessageOptionVisibility;
    }

    public int getEditMessageOptionVisibility() {
        return editMessageOptionVisibility;
    }

    public void setEditMessageOptionVisibility(int editMessageOptionVisibility) {
        this.editMessageOptionVisibility = editMessageOptionVisibility;
    }

    public int getShareMessageOptionVisibility() {
        return shareMessageOptionVisibility;
    }

    public void setShareMessageOptionVisibility(int shareMessageOptionVisibility) {
        this.shareMessageOptionVisibility = shareMessageOptionVisibility;
    }

    public int getMessagePrivatelyOptionVisibility() {
        return messagePrivatelyOptionVisibility;
    }

    public void setMessagePrivatelyOptionVisibility(int messagePrivatelyOptionVisibility) {
        this.messagePrivatelyOptionVisibility = messagePrivatelyOptionVisibility;
    }

    public int getDeleteMessageOptionVisibility() {
        return deleteMessageOptionVisibility;
    }

    public void setDeleteMessageOptionVisibility(int deleteMessageOptionVisibility) {
        this.deleteMessageOptionVisibility = deleteMessageOptionVisibility;
    }

    public int getMessageInfoOptionVisibility() {
        return messageInfoOptionVisibility;
    }

    public void setMessageInfoOptionVisibility(int messageInfoOptionVisibility) {
        this.messageInfoOptionVisibility = messageInfoOptionVisibility;
    }

    public int getGroupActionMessageVisibility() {
        return groupActionMessageVisibility;
    }

    public void setGroupActionMessageVisibility(int groupActionMessageVisibility) {
        this.groupActionMessageVisibility = groupActionMessageVisibility;
    }

    public void setModerationViewVisibility(int visibility) {
        this.moderationViewVisibility = visibility;
    }

    public int getModerationViewVisibility() {
        return moderationViewVisibility;
    }

    public int getAIAssistantMessageBubbleStyle() {
        return aiAssistantMessageBubbleStyle;
    }

    public void setAIAssistantMessageBubbleStyle(int aiAssistantMessageBubbleStyle) {
        this.aiAssistantMessageBubbleStyle = aiAssistantMessageBubbleStyle;
    }

    public int getReplyToMessageOptionVisibility() {
        return replyToMessageOptionVisibility;
    }

    public void setReplyToMessageOptionVisibility(int replyToMessageOptionVisibility) {
        this.replyToMessageOptionVisibility = replyToMessageOptionVisibility;
    }

    public int getIncomingReplyMessagePreviewStyle() {
        return incomingMessagePreviewStyle;
    }

    public void setIncomingReplyMessagePreviewStyle(int resourceId) {
        this.incomingMessagePreviewStyle = resourceId;
    }

    public int getOutgoingReplyMessagePreviewStyle() {
        return outgoingMessagePreviewStyle;
    }

    public void setOutgoingReplyMessagePreviewStyle(int resourceId) {
        this.outgoingMessagePreviewStyle = resourceId;
    }

    public OnItemClick<BaseMessage> getOnMessagePreviewClick() {
        return onMessagePreviewClick;
    }

    public void setOnMessagePreviewClick(OnItemClick<BaseMessage> onMessagePreviewClick) {
        this.onMessagePreviewClick = onMessagePreviewClick;
    }

    public int getFlagOptionVisibility() {
        return flagOptionVisibility;
    }

    public void setFlagOptionVisibility(int flagOptionVisibility) {
        this.flagOptionVisibility = flagOptionVisibility;
    }

    public int getMarkUnreadOptionVisibility() {
        return markUnreadOptionVisibility;
    }

    public void setMarkUnreadOptionVisibility(int markUnreadOptionVisibility) {
        this.markUnreadOptionVisibility = markUnreadOptionVisibility;
    }
}
