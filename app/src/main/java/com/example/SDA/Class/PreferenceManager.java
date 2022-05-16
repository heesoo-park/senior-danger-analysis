package com.example.SDA.Class;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    public static final String PREFERENCES_NAME = "pref";
    public static final String NAME = "name";
    public static final String ID_TOKEN = "id_token";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";
    public static final String CARE_ID = "care_id";
    public static final String CARE_ID_TOKEN = "care_id_token";
    private static final String DEFAULT_VALUE_STRING = null;

    private static SharedPreferences getPreferences(Context context) {
         return context.getSharedPreferences(PREFERENCES_NAME, context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        String value = prefs.getString(key, DEFAULT_VALUE_STRING);
        return value;
    }

    public static void removeKey(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();
    }

    public static void clear(Context context) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
