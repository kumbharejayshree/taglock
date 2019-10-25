package com.tagloy.taglock.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {

    public static void setValueString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setValueBoolean(Context context, String key, Boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setValueInt(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getValueInt(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

    public static String getValueString(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static String getString(Context context, String key) throws NullPointerException{
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static boolean getValueBoolean(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static void removeStringValue(Context context,String key){
        SharedPreferences prefs = context.getSharedPreferences(AppConfig.TAGLOCK_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }
}
