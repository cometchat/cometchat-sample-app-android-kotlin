package utils

import android.content.Context
import android.content.SharedPreferences

public class PreferenceUtil {
    private var sharedPreference: SharedPreferences? = null
    constructor(appContext: Context) {
        context = appContext
    }

    companion object {
        private val PREFERENCE_NAME = "UI KIT"

        private var INSTANCE: PreferenceUtil? = null



        private var context: Context? = null

        fun getInstance(appContext: Context?): PreferenceUtil? {
            if (INSTANCE == null) INSTANCE = PreferenceUtil(appContext!!)
            return INSTANCE
        }



    }
    fun saveStringWithConfirmation(key: String?, value: String?): Boolean {

        sharedPreference = context!!.getSharedPreferences(PREFERENCE_NAME, 0)
        var sharedPreferences = sharedPreference!!
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        return editor.commit()
    }
    fun saveString(key: String?, value: String?) {
        sharedPreference = context!!.getSharedPreferences(PREFERENCE_NAME, 0)
        var sharedPreferences = sharedPreference!!
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
    fun getString(key: String?): String? {
        sharedPreference = context!!.getSharedPreferences(PREFERENCE_NAME, 0)
        var sharedPreferences = sharedPreference!!
        return sharedPreferences.getString(key, "")
    }

}