package utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {

    private static final String PREFERENCE_NAME = "UI KIT";

    private static PreferenceUtil INSTANCE=null;

    private static Context context;

    private static SharedPreferences sharedPreferences;

    private PreferenceUtil(Context appContext) {
        context = appContext;
    }

    public static PreferenceUtil getInstance(Context appContext) {

         if (INSTANCE==null)
             INSTANCE=new PreferenceUtil(appContext);

         return INSTANCE;
    }

    public void saveString(String key,String value) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean saveStringWithConfirmation(String key,String value){
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
       return editor.commit();
    }

    public String getString(String key) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
        return sharedPreferences.getString(key, "");
    }

}
