package com.cometchat.chatuikit.shared.resources.localise;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * The CometChatLocalize class provides utility methods for managing
 * localization in an Android application.
 *
 * <p>
 * It allows setting the locale and retrieving the locale country code for an
 * activity.
 */
public class CometChatLocalize {
    private static final String TAG = CometChatLocalize.class.getSimpleName();
    private static Locale locale;

    /**
     * Sets the locale for the specified activity.
     *
     * @param context  the context to set the locale
     * @param language the language code representing the desired locale (e.g., "en" for
     *                 English, "fr" for French)
     */
    public static void setLocale(Context context, @Language.Code String language) {
        locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static Locale getDefault() {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Locale.setDefault(locale);
        return Locale.getDefault();
    }

    /**
     * Retrieves the locale country code for the specified activity.
     *
     * @param context the context to retrieve the locale country code
     * @return the country code of the current locale in the specified activity
     */
    public static String getLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.locale.getCountry();
    }
}
