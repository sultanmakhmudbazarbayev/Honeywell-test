package com.honeywell.bccrfid.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.test.BeeperTestActivity;
import com.honeywell.bccrfid.test.CarrierTestActivity;
import com.honeywell.bccrfid.utils.FileUtil;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class GeneralInfoFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    public static final String KEY_MODULE_TEMP = "module_temperature";
    public static final String KEY_MODULE_VERSION = "module_version";
    public static final String KEY_FIRMWARE_UPDATE = "firmware_update";
    public static final String KEY_BEEPER_TEST = "beeper_test";
    public static final String KEY_CARRIER_TEST = "carrier_test";
    public static final String KEY_BATTERY_LEVEL = "battery_level";
    public static final String KEY_BATTERY_TEMP = "battery_temperature";
    public static final String KEY_BLE_MODULE_VERSION = "ble_module_version";

    private Preference mModuleTempPreference;
    private Preference mModuleVersionPreference;
    private Preference mFirmwareUpdatePreference;
    private Preference mBeeperTestPreference;
    private Preference mCarrierTestPreference;
    private Preference mBatteryLevelPreference;
    private Preference mBatteryTempPreference;
    private Preference mBLEModuleVersionPreference;
    private App mApp;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.general_info);
        mApp = (App) getActivity().getApplication();
        initPreference();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        caller.setPreferenceScreen(pref);
        return true;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void initPreference() {
        mModuleTempPreference = (Preference) findPreference(KEY_MODULE_TEMP);
        mModuleTempPreference.setOnPreferenceClickListener(this);

        mModuleVersionPreference = (Preference) findPreference(KEY_MODULE_VERSION);
        mModuleVersionPreference.setOnPreferenceClickListener(this);

        mFirmwareUpdatePreference = (Preference) findPreference(KEY_FIRMWARE_UPDATE);
        mFirmwareUpdatePreference.setOnPreferenceClickListener(this);

        mBeeperTestPreference = (Preference) findPreference(KEY_BEEPER_TEST);
        mBeeperTestPreference.setOnPreferenceClickListener(this);

        mCarrierTestPreference = (Preference) findPreference(KEY_CARRIER_TEST);
        mCarrierTestPreference.setOnPreferenceClickListener(this);

        mBatteryLevelPreference = (Preference) findPreference(KEY_BATTERY_LEVEL);
        mBatteryLevelPreference.setOnPreferenceClickListener(this);

        mBatteryTempPreference = (Preference) findPreference(KEY_BATTERY_TEMP);
        mBatteryTempPreference.setOnPreferenceClickListener(this);

        mBLEModuleVersionPreference = (Preference) findPreference(KEY_BLE_MODULE_VERSION);
        mBLEModuleVersionPreference.setOnPreferenceClickListener(this);

        removePreference(KEY_FIRMWARE_UPDATE);
        removePreference(KEY_BEEPER_TEST);
        removePreference(KEY_CARRIER_TEST);
    }

    private void updateState() {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mModuleTempPreference) {
            try {
                if (mApp.checkIsRFIDReady()) {
                    float temp = mApp.rfidReader.getTemperature();
                    mModuleTempPreference.setSummary(temp + " ℃");
                    Toast.makeText(getActivity(), getString(R.string.toast_get_temp_successfully),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.toast_get_temp_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (preference == mModuleVersionPreference) {
            try {
                if (mApp.checkIsRFIDReady()) {
                    String hardwareVersion = mApp.rfidReader.getHardwareVersion();
                    String softwareVersion = mApp.rfidReader.getSoftwareVersion();
                    mModuleVersionPreference.setSummary(String.format(getString(R.string.version_result), hardwareVersion, softwareVersion));
                }
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.toast_get_module_version_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (preference == mBeeperTestPreference) {
            Intent intent = new Intent(getActivity(), BeeperTestActivity.class);
            startActivity(intent);
        }
        if (preference == mCarrierTestPreference) {
            Intent intent = new Intent(getActivity(), CarrierTestActivity.class);
            startActivity(intent);
        }
        if (preference == mBatteryLevelPreference) {
            if (mApp.checkIsRFIDReady()) {
                String level = mApp.rfidMgr.getBatteryLevel();
                mBatteryLevelPreference.setSummary(level + "%");
            }
        }
        if (preference == mBatteryTempPreference) {
            if (mApp.checkIsRFIDReady()) {
                float temp = mApp.rfidMgr.getBatteryTemperature();
                mBatteryTempPreference.setSummary(temp + " ℃");
            }
        }
        if (preference == mBLEModuleVersionPreference) {
            if (mApp.checkIsRFIDReady()) {
                String softwareVersion = (String) mApp.rfidMgr.getBluetoothModuleSwVersion();
                String hardwareVersion = (String) mApp.rfidMgr.getBluetoothModuleHwVersion();
                mBLEModuleVersionPreference.setSummary(String.format(getString(R.string.version_result), hardwareVersion, softwareVersion));
            }
        }
        return true;
    }

    protected void removePreference(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }
}
