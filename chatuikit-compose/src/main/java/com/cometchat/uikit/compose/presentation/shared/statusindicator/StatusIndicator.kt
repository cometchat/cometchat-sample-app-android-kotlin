package com.cometchat.uikit.compose.presentation.shared.statusindicator

/**
 * Enum representing various status indicators.
 * Mirrors the View-based StatusIndicator enum.
 */
enum class StatusIndicator {
    /**
     * Represents the online status of a user.
     */
    ONLINE,

    /**
     * Represents the offline status of a user.
     */
    OFFLINE,

    /**
     * Represents a public group (open to all users).
     */
    PUBLIC_GROUP,

    /**
     * Represents a private group (requires invitation).
     */
    PRIVATE_GROUP,

    /**
     * Represents a protected/password group.
     */
    PROTECTED_GROUP
}
