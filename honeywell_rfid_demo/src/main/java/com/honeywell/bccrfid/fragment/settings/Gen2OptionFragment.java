package com.honeywell.bccrfid.fragment.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.rfidservice.rfid.Gen2.Session;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import static com.honeywell.rfidservice.rfid.Gen2.Session.Session0;
import static com.honeywell.rfidservice.rfid.Gen2.Session.Session1;
import static com.honeywell.rfidservice.rfid.Gen2.Session.Session2;
import static com.honeywell.rfidservice.rfid.Gen2.Session.Session3;

public class Gen2OptionFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    public static final String KEY_SESSION = "session_mode";
    private ListPreference mSessionPreference;
    private App mApp;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.gen2_option_settings);
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
                Session s = mApp.rfidReader.getSession();
                switch (s) {
                    case Session0:
                        mSessionPreference.setValue("0");
                        mSessionPreference.setSummary(mSessionPreference.getEntry());
                        break;
                    case Session1:
                        mSessionPreference.setValue("1");
                        mSessionPreference.setSummary(mSessionPreference.getEntry());
                        break;
                    case Session2:
                        mSessionPreference.setValue("2");
                        mSessionPreference.setSummary(mSessionPreference.getEntry());
                        break;
                    case Session3:
                        mSessionPreference.setValue("3");
                        mSessionPreference.setSummary(mSessionPreference.getEntry());
                        break;
                }
                Toast.makeText(getActivity(), getString(R.string.toast_get_session_mode_successfully),
                        Toast.LENGTH_SHORT).show();
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.toast_get_session_mode_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void initPreference() {
        mSessionPreference = (ListPreference) findPreference(KEY_SESSION);
        mSessionPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSessionPreference) {
            String val = (String) newValue;
            LogUtils.e("mSessionPreference:" + val);
            if (mApp.checkIsRFIDReady()) {
                try {
                    Session s = null;

                    switch (Integer.valueOf(val)) {
                        case 0:
                            s = Session0;
                            break;
                        case 1:
                            s = Session1;
                            break;
                        case 2:
                            s = Session2;
                            break;
                        case 3:
                            s = Session3;
                            break;
                    }
                    mApp.rfidReader.setSession(s);
                    mSessionPreference.setValue(val);
                    mSessionPreference.setSummary(mSessionPreference.getEntry());
                    Toast.makeText(getActivity(), getString(R.string.set_session_mode_successfully),
                            Toast.LENGTH_SHORT).show();
                } catch (RfidReaderException e) {
                    Toast.makeText(getActivity(), getString(R.string.set_session_mode_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
        return true;
    }
}
