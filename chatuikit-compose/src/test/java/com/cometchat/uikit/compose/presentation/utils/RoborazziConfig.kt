package com.cometchat.uikit.compose.presentation.utils

import com.github.takahirom.roborazzi.RoborazziOptions

/**
 * Shared Roborazzi comparison options for all screenshot tests.
 *
 * Provides tolerance for sub-pixel rendering differences between
 * local development machines and CI environments.
 *
 * - changeThreshold: 1% pixel difference allowed before failing
 */
object RoborazziConfig {

    val compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.01f // 1% accepted difference
    )

    /**
     * Creates RoborazziOptions with the shared compare options.
     */
    fun options() = RoborazziOptions(
        compareOptions = compareOptions
    )
}
