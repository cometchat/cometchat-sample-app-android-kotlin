package com.cometchat.uikit.core.resources.localise

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import java.util.Locale

/**
 * The CometChatLocalize class provides utility methods for managing
 * localization in an Android application.
 *
 * It allows setting the locale and retrieving the locale country code for an
 * activity or context.
 */
object CometChatLocalize {
    private const val TAG = "CometChatLocalize"
    private var locale: Locale? = null

    /**
     * Sets the locale for the specified context.
     *
     * @param context The context to set the locale for.
     * @param language The language code representing the desired locale (e.g., "en" for
     *                 English, "fr" for French). Use [Language.Code] constants.
     */
    @JvmStatic
    fun setLocale(context: Context, @Language.Code language: String) {
        locale = Locale(language)
        Locale.setDefault(locale!!)
        
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /**
     * Gets the default locale.
     * If no locale has been set, returns the system default locale.
     *
     * @return The current default locale.
     */
    @JvmStatic
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
     * @param context The context to retrieve the locale country code from.
     * @return The country code of the current locale in the specified context.
     */
    @JvmStatic
    fun getLocale(context: Context): String {
        val config: Configuration = context.resources.configuration
        return config.locales[0].country
    }

    /**
     * Retrieves the locale language code for the specified context.
     *
     * @param context The context to retrieve the locale language code from.
     * @return The language code of the current locale in the specified context.
     */
    @JvmStatic
    fun getLanguage(context: Context): String {
        val config: Configuration = context.resources.configuration
        return config.locales[0].language
    }

    /**
     * Checks if the current locale is a right-to-left language.
     *
     * @param context The context to check.
     * @return True if the current locale is RTL, false otherwise.
     */
    @JvmStatic
    fun isRtl(context: Context): Boolean {
        val config: Configuration = context.resources.configuration
        return config.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    /**
     * Resets the locale to the system default.
     */
    @JvmStatic
    fun resetToDefault() {
        locale = null
    }

    /**
     * Creates a localized context with the specified language.
     * Useful for creating contexts with different locales without affecting the app globally.
     *
     * @param context The base context.
     * @param language The language code for the new locale.
     * @return A new context with the specified locale.
     */
    @JvmStatic
    fun createLocalizedContext(context: Context, @Language.Code language: String): Context {
        val newLocale = Locale(language)
        val config = Configuration(context.resources.configuration)
        config.setLocale(newLocale)
        config.setLayoutDirection(newLocale)
        return context.createConfigurationContext(config)
    }
}
