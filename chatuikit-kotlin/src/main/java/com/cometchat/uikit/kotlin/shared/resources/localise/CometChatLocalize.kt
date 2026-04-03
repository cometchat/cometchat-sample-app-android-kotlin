package com.cometchat.uikit.kotlin.shared.resources.localise

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * CometChatLocalize provides utility methods for managing localization
 * in an Android application.
 *
 * It allows setting the locale and retrieving the locale country code.
 */
object CometChatLocalize {
    private var locale: Locale? = null

    /**
     * Sets the locale for the specified context.
     *
     * @param context The context to set the locale
     * @param language The language code representing the desired locale (e.g., "en" for English, "fr" for French)
     */
    fun setLocale(context: Context, language: String) {
        locale = Locale(language)
        Locale.setDefault(locale!!)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Gets the default locale.
     * If no locale has been set, returns the system default locale.
     *
     * @return The current default locale
     */
    fun getDefault(): Locale {
        if (locale == null) {
            locale = Locale.getDefault()
        }
        Locale.setDefault(locale!!)
        return Locale.getDefault()
    }

    /**
     * Retrieves the locale country code for the specified context.
     *
     * @param context The context to retrieve the locale country code
     * @return The country code of the current locale
     */
    fun getLocale(context: Context): String {
        val config: Configuration = context.resources.configuration
        @Suppress("DEPRECATION")
        return config.locale.country
    }
}
