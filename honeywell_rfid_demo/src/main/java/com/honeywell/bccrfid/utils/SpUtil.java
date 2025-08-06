package com.honeywell.bccrfid.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.honeywell.bccrfid.App;

/**
 * Created by yuwan on 18/2/17.
 */

public class SpUtil {
    public static void putBoolean(String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        return sp.getBoolean(key, defValue);
    }

    public static void putInt(String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        sp.edit().putInt(key, value).commit();
    }

    public static int getInt(String key, int defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        return sp.getInt(key, defValue);
    }

    public static void putLong(String key, long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        sp.edit().putLong(key, value).commit();
    }

    public static long getLong(String key, long defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        return sp.getLong(key, defValue);
    }

    public static void putString(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        sp.edit().putString(key, value).commit();
    }

    public static String getString(String key, String defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        return sp.getString(key, defValue);
    }
}
