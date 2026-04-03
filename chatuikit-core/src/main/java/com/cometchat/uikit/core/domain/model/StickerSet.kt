package com.cometchat.uikit.core.domain.model

/**
 * Data class representing a collection of stickers grouped under a common name.
 *
 * A sticker set contains multiple stickers and uses the first sticker's URL as
 * the icon for the tab bar in the sticker keyboard.
 *
 * ## Usage
 *
 * ```kotlin
 * // Create from a map entry (common when parsing API response)
 * val stickerMap = mapOf("Emotions" to listOf(sticker1, sticker2))
 * val stickerSet = StickerSet.fromMapEntry(stickerMap.entries.first())
 *
 * // Access properties
 * println(stickerSet.name)      // "Emotions"
 * println(stickerSet.iconUrl)   // URL of first sticker
 * println(stickerSet.stickers)  // List of stickers
 * ```
 *
 * @property name The name/identifier of the sticker set
 * @property stickers The list of stickers in this set
 * @property iconUrl The URL to use as the tab icon (derived from first sticker's URL)
 */
data class StickerSet(
    /** The name/identifier of the sticker set */
    val name: String,
    /** The list of stickers in this set */
    val stickers: List<Sticker>,
    /** The URL to use as the tab icon (derived from first sticker's URL) */
    val iconUrl: String
) {
    companion object {
        /**
         * Creates a StickerSet from a map entry.
         *
         * This factory method is useful when parsing the sticker API response,
         * which typically returns stickers grouped by set name in a map.
         *
         * @param entry A map entry where key is the set name and value is the list of stickers
         * @return A new StickerSet with iconUrl derived from the first sticker's URL
         */
        fun fromMapEntry(entry: Map.Entry<String, List<Sticker>>): StickerSet {
            return StickerSet(
                name = entry.key,
                stickers = entry.value,
                iconUrl = entry.value.firstOrNull()?.url ?: ""
            )
        }
    }
}
