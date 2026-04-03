package com.cometchat.uikit.kotlin.presentation.emojikeyboard.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents a single emoji with its unicode string and associated keywords.
 *
 * @param emoji The unicode character string for this emoji (e.g., "😀")
 * @param keywords List of keywords associated with this emoji (e.g., ["smile", "happy"])
 */
data class Emoji(
    @SerializedName("emoji")
    @Expose
    val emoji: String,

    @SerializedName("keywords")
    @Expose
    val keywords: List<String> = emptyList()
)
