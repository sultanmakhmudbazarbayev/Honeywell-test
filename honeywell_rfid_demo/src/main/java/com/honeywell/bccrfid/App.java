package com.honeywell.bccrfid;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.rfidservice.ConnectionState;
import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.TriggerMode;
import com.honeywell.rfidservice.rfid.RfidReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {
    private static final String TAG = "App";

    public String[] Coname;
    public String[] tagListTitles;
    public RfidReader rfidReader;
    public SharedPrefManager mSharedPrefManager;

    //BT control
    public List<BluetoothDevice> bleScanDevices = new ArrayList<>();
    public Map<String, Integer> bleScanDevicesRssi = new HashMap<>();
    public BluetoothDevice selectedBleDev;

    public List<Map<String, ?>> ListMs = Collections.synchronizedList(new ArrayList<Map<String, ?>>());

    //Write Tag Fragment
    public String mSelectedEpc;

    //Locate Tag
    public boolean mIsFindingGood = false;

    public float batteryTemperature = 0;
    public int batteryChargeCycle = 0;

    private static App mInstance;
    public RfidManager rfidMgr;
    public boolean enableRfidFwUpdate;
    public boolean debugMode;

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        rfidMgr = RfidManager.getInstance(this);

        Coname = getResources().getStringArray(R.array.coname_texts);
        tagListTitles = getResources().getStringArray(R.array.coname_texts);
        LogUtils.e("App onCreate");
        setListTitle();
        LogUtils.e(BuildConfig.UI_STYLE + ":" + BuildConfig.FLAVOR);
        mSharedPrefManager = SharedPrefManager.getInstance(this);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mSharedPrefManager.loadDefaultSettings();
                boolean debug = mSharedPrefManager.getBoolean(getString(R.string.debug_mode), false);
                setDebugMode(debug);
            }
        });
    }

    public boolean isRFIDReady() {
        return rfidMgr.readerAvailable() && rfidMgr.getConnectionState() == ConnectionState.STATE_CONNECTED && rfidMgr.getTriggerMode() == TriggerMode.RFID;
    }

    public boolean isReady() {
        return rfidMgr.readerAvailable() && rfidMgr.getConnectionState() == ConnectionState.STATE_CONNECTED;
    }

    public boolean isBatteryTemperatureTooHigh() {
        return batteryTemperature >= 60;
    }

    public boolean checkIsRFIDReady() {
        if (rfidMgr.getConnectionState() != ConnectionState.STATE_CONNECTED) {
            Toast.makeText(this, getString(R.string.toast_error1), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!rfidMgr.readerAvailable()) {
            Toast.makeText(this, getString(R.string.toast_error2), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rfidMgr.getTriggerMode() != TriggerMode.RFID) {
            Toast.makeText(this, getString(R.string.toast_error3), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean checkIsReady() {
        if (rfidMgr.getConnectionState() != ConnectionState.STATE_CONNECTED) {
            Toast.makeText(this, getString(R.string.toast_error1), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!rfidMgr.readerAvailable()) {
            Toast.makeText(this, getString(R.string.toast_error2), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setListTitle() {
        Map<String, String> h = new HashMap<String, String>();

        for (int i = 0; i < Coname.length; i++) {
            h.put(Coname[i], tagListTitles[i]);
        }

        if (ListMs.size() == 0) {
            ListMs.add(h);
        } else {
            ListMs.set(0, h);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        tagListTitles = getResources().getStringArray(R.array.coname_texts);
        setListTitle();
    }

    public RfidManager getRfidManager() {
        return rfidMgr;
    }

    public void setDebugMode(boolean en) {
        debugMode = en;
        mSharedPrefManager.putBoolean(getString(R.string.debug_mode), en);
        com.honeywell.bccrfid.utils.Log.level = en ? Log.VERBOSE : Log.INFO;

        try {
            Method method = rfidMgr.getClass().getDeclaredMethod("setDebugMode", boolean.class);
            method.setAccessible(true);
            method.invoke(rfidMgr, en);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
