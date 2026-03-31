package com.cometchat.chatuikit.compactmessagecomposer;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

import java.util.EnumSet;
import java.util.Set;

/**
 * ComposerState represents the internal state of the CometChatSingleLineComposer.
 * <p>
 * This class encapsulates all mutable state for the composer including:
 * <ul>
 *     <li>Text content</li>
 *     <li>Toolbar visibility</li>
 *     <li>Active text formats</li>
 *     <li>Edit/Reply mode</li>
 *     <li>Voice recording state</li>
 *     <li>Mentions state</li>
 * </ul>
 * </p>
 * <p>
 * The state is managed by {@link CompactMessageComposerViewModel} and observed
 * by the UI layer via LiveData.
 * </p>
 *
 * @see CompactMessageComposerViewModel
 * @see CometChatCompactMessageComposer
 */
public class ComposerState {

    /**
     * The current text content in the input field.
     */
    private String text = "";

    /**
     * Whether the rich text toolbar is currently visible.
     */
    private boolean isToolbarVisible = false;

    /**
     * The set of currently active text formats at the cursor position.
     */
    private Set<FormatType> activeFormats = EnumSet.noneOf(FormatType.class);

    /**
     * The message being edited, or null if not in edit mode.
     */
    private TextMessage editingMessage = null;

    /**
     * The message being replied to, or null if not in reply mode.
     */
    private BaseMessage replyingToMessage = null;

    /**
     * Whether voice recording is currently active.
     */
    private boolean isRecording = false;

    /**
     * The current recording duration in milliseconds.
     */
    private long recordingDuration = 0;

    /**
     * Whether the mentions suggestion list is currently visible.
     */
    private boolean showMentionsList = false;

    /**
     * Whether the mention limit banner is currently visible.
     */
    private boolean showMentionLimitBanner = false;

    /**
     * The current count of mentions in the text.
     */
    private int mentionCount = 0;

    /**
     * Whether the inline audio recorder is currently visible.
     */
    private boolean isInlineRecordingMode = false;

    /**
     * Default constructor initializing state with default values.
     */
    public ComposerState() {
        // Use default values
    }

    /**
     * Copy constructor for creating a new state from an existing one.
     *
     * @param other The state to copy from.
     */
    public ComposerState(ComposerState other) {
        this.text = other.text;
        this.isToolbarVisible = other.isToolbarVisible;
        this.activeFormats = EnumSet.copyOf(other.activeFormats.isEmpty() ? 
                EnumSet.noneOf(FormatType.class) : other.activeFormats);
        this.editingMessage = other.editingMessage;
        this.replyingToMessage = other.replyingToMessage;
        this.isRecording = other.isRecording;
        this.recordingDuration = other.recordingDuration;
        this.showMentionsList = other.showMentionsList;
        this.showMentionLimitBanner = other.showMentionLimitBanner;
        this.mentionCount = other.mentionCount;
        this.isInlineRecordingMode = other.isInlineRecordingMode;
    }

    // ==================== Helper Methods ====================

    /**
     * Determines whether the send button should be active.
     * <p>
     * The send button is active when the text contains non-whitespace characters,
     * excluding zero-width spaces used as placeholders for code formatting.
     * </p>
     *
     * @return true if the send button should be active, false otherwise.
     */
    public boolean isSendButtonActive() {
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
     * Checks if the composer is currently in edit mode.
     *
     * @return true if editing a message, false otherwise.
     */
    public boolean isEditing() {
        return editingMessage != null;
    }

    /**
     * Checks if the composer is currently in reply mode.
     *
     * @return true if replying to a message, false otherwise.
     */
    public boolean isReplying() {
        return replyingToMessage != null;
    }

    /**
     * Resets all state to default values.
     * <p>
     * This method clears all state including text, edit/reply mode,
     * recording state, and mentions state.
     * </p>
     */
    public void reset() {
        text = "";
        isToolbarVisible = false;
        activeFormats = EnumSet.noneOf(FormatType.class);
        editingMessage = null;
        replyingToMessage = null;
        isRecording = false;
        recordingDuration = 0;
        showMentionsList = false;
        showMentionLimitBanner = false;
        mentionCount = 0;
        isInlineRecordingMode = false;
    }

    // ==================== Getters ====================

    /**
     * Returns the current text content.
     *
     * @return The text content.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns whether the toolbar is visible.
     *
     * @return true if toolbar is visible, false otherwise.
     */
    public boolean isToolbarVisible() {
        return isToolbarVisible;
    }

    /**
     * Returns the set of active formats at the cursor position.
     *
     * @return The set of active FormatType values.
     */
    public Set<FormatType> getActiveFormats() {
        return activeFormats;
    }

    /**
     * Returns the message being edited.
     *
     * @return The TextMessage being edited, or null if not in edit mode.
     */
    public TextMessage getEditingMessage() {
        return editingMessage;
    }

    /**
     * Returns the message being replied to.
     *
     * @return The BaseMessage being replied to, or null if not in reply mode.
     */
    public BaseMessage getReplyingToMessage() {
        return replyingToMessage;
    }

    /**
     * Returns whether voice recording is active.
     *
     * @return true if recording, false otherwise.
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Returns the current recording duration in milliseconds.
     *
     * @return The recording duration.
     */
    public long getRecordingDuration() {
        return recordingDuration;
    }

    /**
     * Returns whether the mentions suggestion list is visible.
     *
     * @return true if mentions list is visible, false otherwise.
     */
    public boolean isShowMentionsList() {
        return showMentionsList;
    }

    /**
     * Returns whether the mention limit banner is visible.
     *
     * @return true if banner is visible, false otherwise.
     */
    public boolean isShowMentionLimitBanner() {
        return showMentionLimitBanner;
    }

    /**
     * Returns the current mention count.
     *
     * @return The number of mentions in the text.
     */
    public int getMentionCount() {
        return mentionCount;
    }

    /**
     * Returns whether the inline audio recorder is currently visible.
     *
     * @return true if inline recording mode is active, false otherwise.
     */
    public boolean isInlineRecordingMode() {
        return isInlineRecordingMode;
    }

    // ==================== Setters ====================

    /**
     * Sets the text content.
     *
     * @param text The text content to set.
     */
    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    /**
     * Sets the toolbar visibility.
     *
     * @param toolbarVisible true to show toolbar, false to hide.
     */
    public void setToolbarVisible(boolean toolbarVisible) {
        isToolbarVisible = toolbarVisible;
    }

    /**
     * Sets the active formats.
     *
     * @param activeFormats The set of active FormatType values.
     */
    public void setActiveFormats(Set<FormatType> activeFormats) {
        this.activeFormats = activeFormats != null ? 
                EnumSet.copyOf(activeFormats.isEmpty() ? EnumSet.noneOf(FormatType.class) : activeFormats) : 
                EnumSet.noneOf(FormatType.class);
    }

    /**
     * Sets the message being edited.
     *
     * @param editingMessage The TextMessage to edit, or null to exit edit mode.
     */
    public void setEditingMessage(TextMessage editingMessage) {
        this.editingMessage = editingMessage;
    }

    /**
     * Sets the message being replied to.
     *
     * @param replyingToMessage The BaseMessage to reply to, or null to exit reply mode.
     */
    public void setReplyingToMessage(BaseMessage replyingToMessage) {
        this.replyingToMessage = replyingToMessage;
    }

    /**
     * Sets the recording state.
     *
     * @param recording true if recording, false otherwise.
     */
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    /**
     * Sets the recording duration.
     *
     * @param recordingDuration The recording duration in milliseconds.
     */
    public void setRecordingDuration(long recordingDuration) {
        this.recordingDuration = recordingDuration;
    }

    /**
     * Sets the mentions list visibility.
     *
     * @param showMentionsList true to show mentions list, false to hide.
     */
    public void setShowMentionsList(boolean showMentionsList) {
        this.showMentionsList = showMentionsList;
    }

    /**
     * Sets the mention limit banner visibility.
     *
     * @param showMentionLimitBanner true to show banner, false to hide.
     */
    public void setShowMentionLimitBanner(boolean showMentionLimitBanner) {
        this.showMentionLimitBanner = showMentionLimitBanner;
    }

    /**
     * Sets the mention count.
     *
     * @param mentionCount The number of mentions.
     */
    public void setMentionCount(int mentionCount) {
        this.mentionCount = mentionCount;
    }

    /**
     * Sets the inline recording mode state.
     *
     * @param inlineRecordingMode true to enable inline recording mode, false to disable.
     */
    public void setInlineRecordingMode(boolean inlineRecordingMode) {
        this.isInlineRecordingMode = inlineRecordingMode;
    }
}
