package com.cometchat.chatuikit.compactmessagecomposer;

/**
 * EnterKeyBehavior is an enumeration that defines the behavior of the Enter key
 * in the CometChatSingleLineComposer.
 * <p>
 * This enum allows developers to configure whether pressing the Enter key
 * should send the message or insert a new line character in the input field.
 * </p>
 *
 * @see CometChatCompactMessageComposer
 */
public enum EnterKeyBehavior {
    /**
     * When the Enter key is pressed, the message will be sent immediately.
     * <p>
     * This is useful for quick messaging scenarios where users expect
     * the Enter key to submit their message, similar to many chat applications.
     * </p>
     */
    SEND_MESSAGE,

    /**
     * When the Enter key is pressed, a new line character will be inserted.
     * <p>
     * This is useful when users need to compose multi-line messages
     * and prefer to use a dedicated send button to submit their message.
     * </p>
     */
    NEW_LINE
}
