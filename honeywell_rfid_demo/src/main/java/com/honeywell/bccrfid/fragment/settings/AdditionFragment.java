package com.honeywell.bccrfid.fragment.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;

public class AdditionFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    public static final String KEY_ADDITION_TAG_DATA_TYPE = "settings_addtion_type";
    private ListPreference mAdditionTagDataTypereference;
    private App mApp;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.addition_settings);
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
        mAdditionTagDataTypereference.setSummary(mAdditionTagDataTypereference.getEntry());
    }

    private void initPreference() {
        mAdditionTagDataTypereference = (ListPreference) findPreference(KEY_ADDITION_TAG_DATA_TYPE);
        mAdditionTagDataTypereference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAdditionTagDataTypereference) {
            mAdditionTagDataTypereference.setValue((String) newValue);
            mAdditionTagDataTypereference.setSummary(mAdditionTagDataTypereference.getEntry());
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
