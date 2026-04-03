package com.cometchat.uikit.kotlin.presentation.shared.statusindicator

/**
 * Enum representing various status indicators for users and groups.
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
     * Represents a public group status.
     */
    PUBLIC_GROUP,

    /**
     * Represents a private group status.
     */
    PRIVATE_GROUP,

    /**
     * Represents a protected (password-protected) group status.
     */
    PROTECTED_GROUP
}
