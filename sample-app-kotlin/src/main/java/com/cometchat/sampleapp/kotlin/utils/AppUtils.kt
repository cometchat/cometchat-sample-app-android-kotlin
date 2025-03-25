package com.cometchat.sampleapp.kotlin.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.CustomToastBinding

object AppUtils {

    fun customToast(
        context: Context?, message: String?, @ColorInt backgroundColor: Int
    ) {
        val binding = CustomToastBinding.inflate(LayoutInflater.from(context))
        binding.tvMsg.text = message
        binding.parentCard.setCardBackgroundColor(backgroundColor)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = binding.root
        toast.show()
    }

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

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 103
                )
            }
        }
    }

    private fun createNotificationChannel(activity: Activity) {
        val name: CharSequence = "Default Channel"
        val description = "Channel for notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("default_channel", name, importance)
        channel.description = description
        val notificationManager = activity.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
        CometChatLogger.e("TAG", "onMessageReceived: 15")
    }

    fun getProgressBar(
        context: Context, size: Int, @ColorInt color: Int? = null
    ): ProgressBar {
        val progressBar = ProgressBar(context)
        val layoutParams = LinearLayout.LayoutParams(size, size)
        layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = layoutParams
        progressBar.indeterminateTintList = ColorStateList.valueOf(
            color ?: CometChatTheme.getTextColorPrimary(context)
        )
        return progressBar
    }

    fun isDarkMode(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

}
