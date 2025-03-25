package com.cometchat.sampleapp.java.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.CustomToastBinding;

public class AppUtils {

    public static void customToast(Context context, String message, @ColorInt int backgroundColor) {
        CustomToastBinding binding = CustomToastBinding.inflate(LayoutInflater.from(context));
        binding.tvMsg.setText(message);
        binding.parentCard.setCardBackgroundColor(backgroundColor);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(binding.getRoot());
        toast.show();
    }

    public static <T> void saveDataInSharedPref(Context context, String key, T value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_pref), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.apply();
    }

    public static <T> T getDataFromSharedPref(Context context, Class<T> type, String key, T defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_pref), MODE_PRIVATE);
        if (type == String.class) {
            return type.cast(sharedPreferences.getString(key, (String) defaultValue));
        } else if (type == Integer.class) {
            return type.cast(sharedPreferences.getInt(key, (Integer) defaultValue));
        } else if (type == Boolean.class) {
            return type.cast(sharedPreferences.getBoolean(key, (Boolean) defaultValue));
        } else if (type == Float.class) {
            return type.cast(sharedPreferences.getFloat(key, (Float) defaultValue));
        } else if (type == Long.class) {
            return type.cast(sharedPreferences.getLong(key, (Long) defaultValue));
        }
        return defaultValue;
    }

    public static void clearSharePref(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_pref), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static ProgressBar getProgressBar(Context context, int size, @ColorInt int color) {
        ProgressBar progressBar = new ProgressBar(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(CometChatTheme.getTextColorPrimary(context)));
        return progressBar;
    }

    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

}
