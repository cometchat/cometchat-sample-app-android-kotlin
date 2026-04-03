package com.cometchat.uikit.compose.presentation.emojikeyboard.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents a single emoji with its unicode string and associated keywords.
 *
 * @param emoji The unicode character string for this emoji (e.g., "😀")
 * @param keywords List of keywords associated with this emoji (e.g., ["smile", "happy"])
 */
@Immutable
data class Emoji(
    @SerializedName("emoji")
    @Expose
    val emoji: String,

    @SerializedName("keywords")
    @Expose
    val keywords: List<String> = emptyList()
) {
    init {
        require(emoji.isNotEmpty()) { "Emoji unicode string must not be empty" }
    }
}

/**
 * Represents a category of emojis with an id, display name, icon symbol, and list of emojis.
 *
 * @param id Unique identifier for this category (e.g., "people", "animals_and_nature")
 * @param name Display name for this category (e.g., "Smileys & People")
 * @param symbol Drawable resource ID used as a fallback icon for the category tab
 * @param emojis List of emojis belonging to this category
 */
@Immutable
data class EmojiCategory(
    @SerializedName("id")
    @Expose
    val id: String,

    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("symbol")
    @Expose
    @DrawableRes
    val symbol: Int = 0,

    @SerializedName("emojis")
    @Expose
    val emojis: List<Emoji>
) {
    init {
        require(id.isNotEmpty()) { "EmojiCategory id must not be empty" }
        require(name.isNotEmpty()) { "EmojiCategory name must not be empty" }
        require(emojis.isNotEmpty()) { "EmojiCategory emojis list must not be empty" }
    }
}
