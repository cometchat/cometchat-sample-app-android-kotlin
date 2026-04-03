package com.cometchat.uikit.core.domain.model

/**
 * Data class representing a single sticker.
 *
 * A sticker contains the sticker's name, URL for the image, and the name of the
 * sticker set it belongs to. Stickers can be either static images or animated GIFs.
 *
 * ## Usage
 *
 * ```kotlin
 * val sticker = Sticker(
 *     name = "happy_face",
 *     url = "https://example.com/stickers/happy_face.gif",
 *     setName = "Emotions"
 * )
 *
 * if (sticker.isGif()) {
 *     // Load as animated GIF
 * } else {
 *     // Load as static image
 * }
 * ```
 *
 * @property name The name/identifier of the sticker
 * @property url The URL to the sticker image (can be static image or GIF)
 * @property setName The name of the sticker set this sticker belongs to
 */
data class Sticker(
    /** The name/identifier of the sticker */
    val name: String,
    /** The URL to the sticker image */
    val url: String,
    /** The name of the sticker set this sticker belongs to */
    val setName: String
) {
    /**
     * Checks if the sticker is a GIF animation.
     *
     * @return true if the sticker URL ends with ".gif" (case-insensitive), false otherwise
     */
    fun isGif(): Boolean = url.lowercase().endsWith(".gif")
}
