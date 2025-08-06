package com.honeywell.bccrfid.fragment.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.Const;
import com.honeywell.bccrfid.R;

public class CommonSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    public static final String KEY_SINGLE_READ_DURATION = Const.SP_KEY_SINGLE_READ_DURATION;
    public static final String KEY_SINGLE_READ_VACANCY = Const.SP_KEY_SINGLE_READ_VACANCY;
    public static final String KEY_SCAN_MODE = Const.SP_KEY_SCAN_MODE;
    public static final String KEY_PDA_SCAN_SOUND = Const.SP_KEY_PDA_SCAN_SOUND;
    public static final String KEY_RFID_SCAN_SOUND = Const.SP_KEY_RFID_SCAN_SOUND;

    private ListPreference mSingleReadDurationPreference;
    private ListPreference mSingleReadVacancyPreference;
    private ListPreference mScanModePreference;
    private SwitchPreference mPdaScanSoundSwitchPreference;
    private SwitchPreference mRfidScanSoundSwitchPreference;
    private App mApp;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.common_settings);
        mApp = (App) getActivity().getApplication();
        initPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void initPreference() {
        mSingleReadDurationPreference = (ListPreference) findPreference(KEY_SINGLE_READ_DURATION);
        mSingleReadDurationPreference.setOnPreferenceChangeListener(this);

        mSingleReadVacancyPreference = (ListPreference) findPreference(KEY_SINGLE_READ_VACANCY);
        mSingleReadVacancyPreference.setOnPreferenceChangeListener(this);


        mScanModePreference = (ListPreference) findPreference(KEY_SCAN_MODE);
        mScanModePreference.setOnPreferenceChangeListener(this);

        mPdaScanSoundSwitchPreference = (SwitchPreference) findPreference(KEY_PDA_SCAN_SOUND);
        mRfidScanSoundSwitchPreference = (SwitchPreference) findPreference(KEY_RFID_SCAN_SOUND);
    }

    private void updateState() {
        mSingleReadDurationPreference.setSummary(mSingleReadDurationPreference.getEntry());
        mSingleReadVacancyPreference.setSummary(mSingleReadVacancyPreference.getEntry());
        mScanModePreference.setSummary(mScanModePreference.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSingleReadDurationPreference) {
            mSingleReadDurationPreference.setSummary((String) newValue);
        }
        if (preference == mSingleReadVacancyPreference) {
            mSingleReadVacancyPreference.setSummary((String) newValue);
        }
        if (preference == mScanModePreference) {
            mScanModePreference.setValue((String) newValue);
            mScanModePreference.setSummary(mScanModePreference.getEntry());
            return false;
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    protected void removePreference(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }
}
