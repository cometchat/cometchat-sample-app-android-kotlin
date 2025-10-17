package com.cometchat.ai.sampleapp.utils

import android.content.Context
import com.cometchat.ai.sampleapp.R

object AppUtils {

    fun <T> saveDataInSharedPref(
        context: Context, key: String?, value: T
    ) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.app_shared_pref), Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        when (value) {
            is String -> {
                editor.putString(key, value as String)
            }

            is Int -> {
                editor.putInt(key, value as Int)
            }

            is Boolean -> {
                editor.putBoolean(key, value as Boolean)
            }

            is Float -> {
                editor.putFloat(key, value as Float)
            }

            is Long -> {
                editor.putLong(key, value as Long)
            }
        }
        editor.apply()
    }

    fun <T> getDataFromSharedPref(
        context: Context, type: Class<T>, keyResId: Int, defaultValue: T?
    ): T? {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.app_shared_pref), Context.MODE_PRIVATE
        )
        val key = context.getString(keyResId)
        return when (type) {
            String::class.java -> sharedPreferences.getString(key, defaultValue as? String) as T?
            Int::class.java -> sharedPreferences.getInt(key, defaultValue as? Int ?: 0) as T?
            Boolean::class.java -> sharedPreferences.getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class.java -> sharedPreferences.getFloat(key, defaultValue as? Float ?: 0f) as T?
            Long::class.java -> sharedPreferences.getLong(key, defaultValue as? Long ?: 0L) as T?
            else -> defaultValue
        }
    }

    fun clearSharePref(context: Context) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.app_shared_pref), Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
