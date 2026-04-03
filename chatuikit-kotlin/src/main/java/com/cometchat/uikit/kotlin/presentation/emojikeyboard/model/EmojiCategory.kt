package com.cometchat.uikit.kotlin.presentation.emojikeyboard.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents a category of emojis with an id, display name, icon symbol, and list of emojis.
 *
 * @param id Unique identifier for this category (e.g., "people", "animals_and_nature")
 * @param name Display name for this category (e.g., "Smileys & People")
 * @param symbol Drawable resource ID used as a fallback icon for the category tab
 * @param emojis List of emojis belonging to this category
 */
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
    val emojis: List<Emoji> = emptyList()
)
