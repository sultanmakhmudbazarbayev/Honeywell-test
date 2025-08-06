package com.honeywell.bccrfid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefManager {
    private static SharedPrefManager mManager = null;
    private App mApp;
    SharedPreferences mSP;

    private SharedPrefManager(App app) {
        mApp = app;
        mSP = PreferenceManager.getDefaultSharedPreferences(mApp);
    }

    public static SharedPrefManager getInstance(Context context) {
        if (mManager == null) {
            synchronized (SharedPrefManager.class) {
                if (mManager == null) {
                    mManager = new SharedPrefManager((App) context.getApplicationContext());
                }
            }
        }
        return mManager;

    }

    public void loadDefaultSettings() {
        SharedPreferences.Editor editor = mSP.edit();
        if (mSP.getBoolean(Const.SP_FIRST_INIT, true)) {
            editor.putBoolean(Const.SP_KEY_AUTO_CONNECT, false);
            //common settings
            editor.putString(Const.SP_KEY_SINGLE_READ_DURATION, Const.DEF_SINGLE_INVENTORY_DURATION);
            editor.putString(Const.SP_KEY_SINGLE_READ_VACANCY, Const.DEF_SINGLE_INVENTORY_VACANCY);
            editor.putString(Const.SP_KEY_SCAN_MODE, Const.SCAN_MODE_NORMAL);
            editor.putBoolean(Const.SP_KEY_PDA_SCAN_SOUND, false);
            editor.putBoolean(Const.SP_KEY_RFID_SCAN_SOUND, false);
            //addition
            editor.putString(Const.SP_KEY_ADDITION_TAG_DATA_TYPE, "-1");
            //fast mode
            editor.putString(Const.SP_KEY_PAUSE_PERCENTAGE, "0");
            editor.putBoolean(Const.SP_KEY_ITEM_COUNT, true);
            editor.putBoolean(Const.SP_KEY_ITEM_RSSI, false);
            editor.putBoolean(Const.SP_KEY_ITEM_ANT, false);
            editor.putBoolean(Const.SP_KEY_ITEM_FREQ, false);
            editor.putBoolean(Const.SP_KEY_ITEM_TIME, false);
            editor.putBoolean(Const.SP_KEY_ITEM_RFU, false);
            editor.putBoolean(Const.SP_KEY_ITEM_PRO, false);
            editor.putBoolean(Const.SP_KEY_ITEM_DATA, false);

            editor.putBoolean(Const.SP_FIRST_INIT, false);
            editor.commit();
        }
    }

    public int getInt(String key, int def) {
        return mSP.getInt(key, def);
    }

    public String getString(String key, String def) {
        return mSP.getString(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return mSP.getBoolean(key, def);
    }

    public void putInt(String key, int val) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public void putString(String key, String val) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public void putBoolean(String key, boolean val) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }
}
