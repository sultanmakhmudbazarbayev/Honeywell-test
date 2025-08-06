package com.honeywell.bccrfid.fragment.settings;

import android.os.Bundle;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.utils.Log;
import com.honeywell.rfidservice.rfid.AntennaPower;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class AntePowerFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AntePowerFragment";
    public static final String KEY_ANTE1_READ_POWER = "read_power_ante1";
    public static final String KEY_ANTE1_WRITE_POWER = "write_power_ante1";
    private ListPreference mAnte1ReadPowerListPreference;
    private ListPreference mAnte1WritePowerListPreference;
    private App mApp;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.ante_power_settings);
        mApp = (App) getActivity().getApplication();
        initPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSettings();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadSettings();
        }
    }

    private void loadSettings() {
        if (mApp.checkIsRFIDReady()) {
            try {
                AntennaPower[] ap = mApp.rfidReader.getAntennaPower();

                if(ap == null){
                    return;
                }

                Log.d(TAG, "ap size:" + ap.length);

                for (int i = 0; i < ap.length; i++) {
                    Log.d(TAG, "ap num read:" + ap[i].getReadPower() + "     ap num write:" + ap[i].getWritePower());

                    if (i == 0) {
                        mAnte1ReadPowerListPreference.setValue(String.valueOf(ap[i].getReadPower()));
                        mAnte1ReadPowerListPreference.setSummary(String.valueOf(ap[i].getReadPower()));
                        mAnte1WritePowerListPreference.setValue(String.valueOf(ap[i].getWritePower()));
                        mAnte1WritePowerListPreference.setSummary(String.valueOf(ap[i].getWritePower()));
                    }
                }

                Toast.makeText(getActivity(), getString(R.string.toast_get_power_successfullly),
                        Toast.LENGTH_SHORT).show();
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.toast_get_power_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initPreference() {
        mAnte1ReadPowerListPreference = (ListPreference) findPreference(KEY_ANTE1_READ_POWER);
        mAnte1WritePowerListPreference = (ListPreference) findPreference(KEY_ANTE1_WRITE_POWER);
        mAnte1ReadPowerListPreference.setOnPreferenceChangeListener(this);
        mAnte1WritePowerListPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAnte1ReadPowerListPreference) {
            String val = (String) newValue;
            int antNum = 1;
            AntennaPower[] ap = new AntennaPower[antNum];
            if (mApp.checkIsRFIDReady()) {
                try {
                    for (int i = 0; i < antNum; i++) {
                        ap[i] = new AntennaPower(i + 1, Integer.valueOf(val), Integer.valueOf(mAnte1WritePowerListPreference.getValue()));
                    }
                    mApp.rfidReader.setAntennaPower(ap);
                    mAnte1ReadPowerListPreference.setSummary(val);
                    Toast.makeText(getActivity(), getString(R.string.toast_set_power_successfullly),
                            Toast.LENGTH_SHORT).show();
                } catch (RfidReaderException e) {
                    Toast.makeText(getActivity(), getString(R.string.toast_set_power_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (preference == mAnte1WritePowerListPreference) {
            String val = (String) newValue;
            int antNum = 1;
            AntennaPower[] ap = new AntennaPower[antNum];
            if (mApp.checkIsRFIDReady()) {
                try {
                    for (int i = 0; i < antNum; i++) {
                        ap[i] = new AntennaPower(i + 1, Integer.valueOf(mAnte1ReadPowerListPreference.getValue()), Integer.valueOf(val));
                    }
                    mApp.rfidReader.setAntennaPower(ap);
                    mAnte1WritePowerListPreference.setSummary(val);
                    Toast.makeText(getActivity(), getString(R.string.toast_set_power_successfullly),
                            Toast.LENGTH_SHORT).show();
                } catch (RfidReaderException e) {
                    Toast.makeText(getActivity(), getString(R.string.toast_set_power_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }
}
