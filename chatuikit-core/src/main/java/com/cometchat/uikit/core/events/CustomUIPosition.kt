package com.cometchat.uikit.core.events

/**
 * Enum representing the position of custom UI panels.
 * Maps to the Java UIKitConstants.CustomUIPosition enum for compatibility.
 */
enum class CustomUIPosition {
    /**
     * Position at the top of the message composer.
     */
    COMPOSER_TOP,

    /**
     * Position at the bottom of the message composer.
     */
    COMPOSER_BOTTOM,

    /**
     * Position at the top of the message list.
     */
    MESSAGE_LIST_TOP,

    /**
     * Position at the bottom of the message list.
     */
    MESSAGE_LIST_BOTTOM
}
