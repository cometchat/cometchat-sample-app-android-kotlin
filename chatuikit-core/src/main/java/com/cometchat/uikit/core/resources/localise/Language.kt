package com.cometchat.uikit.core.resources.localise

import androidx.annotation.StringDef

/**
 * Language codes supported by CometChat UIKit.
 * Use these constants with [CometChatLocalize.setLocale] to set the app language.
 */
object Language {
    const val ENGLISH = "en"
    const val SPANISH = "es"
    const val FRENCH = "fr"
    const val GERMAN = "de"
    const val PORTUGUESE = "pt"
    const val ITALIAN = "it"
    const val RUSSIAN = "ru"
    const val CHINESE = "zh"
    const val JAPANESE = "ja"
    const val KOREAN = "ko"
    const val ARABIC = "ar"
    const val HINDI = "hi"
    const val TURKISH = "tr"
    const val DUTCH = "nl"
    const val POLISH = "pl"
    const val SWEDISH = "sv"
    const val HUNGARIAN = "hu"
    const val MALAY = "ms"
    const val LITHUANIAN = "lt"

    /**
     * Annotation for language code validation.
     */
    @StringDef(
        ENGLISH, SPANISH, FRENCH, GERMAN, PORTUGUESE, ITALIAN,
        RUSSIAN, CHINESE, JAPANESE, KOREAN, ARABIC, HINDI,
        TURKISH, DUTCH, POLISH, SWEDISH, HUNGARIAN, MALAY, LITHUANIAN
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Code
}
