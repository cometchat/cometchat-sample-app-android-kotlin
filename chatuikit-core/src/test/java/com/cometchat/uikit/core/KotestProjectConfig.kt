package com.cometchat.uikit.core

import io.kotest.core.config.AbstractProjectConfig

/**
 * Kotest project-level configuration.
 *
 * Sets concurrentSpecs = 1 to prevent multiple specs from running in parallel.
 * This avoids "Dispatchers.Main is used concurrently with setting it" errors
 * that occur when one spec's afterSpec (resetMain) races with another spec's
 * beforeSpec (setMain).
 */
class KotestProjectConfig : AbstractProjectConfig() {
    override val concurrentSpecs: Int = 1
}
