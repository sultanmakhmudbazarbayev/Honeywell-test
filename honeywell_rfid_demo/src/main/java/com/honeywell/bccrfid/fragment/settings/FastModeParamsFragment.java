package com.honeywell.bccrfid.fragment.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.chip.Chip;
import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.Const;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.bccrfid.views.MyPreference;

import java.util.ArrayList;
import java.util.List;

public class FastModeParamsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Chip.OnCheckedChangeListener {
    public static final String KEY_PAUSE_PERCENTAGE = "pause_percentage";
    public static final String KEY_FAST_MODE_SEETINGS = "fast_mode_settings";

    private ListPreference mPauserPercentageListPreference;
    private App mApp;
    private PreferenceViewHolder mView;
    private Chip mCountChip, mRssiChip, mAntChip, mFreqChip, mTimeChip, mRfuChip, mProChip, mDataChip;
    private MyPreference mPreference;
    private List<Chip> mChips = new ArrayList<>();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fast_mode_params_settings);
        mApp = (App) getActivity().getApplication();
        initPreference();

    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.e("loadSettings1111111");
        loadSettings();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            LogUtils.e("loadSettings22222");
            loadSettings();
        }
    }

    private void loadSettings() {
        for (int i = 0; i < mChips.size(); i++) {
            mChips.get(i).setChecked(mApp.mSharedPrefManager.getBoolean((String) (mChips.get(i).getTag()), false));
        }
    }

    private void initPreference() {
        mPauserPercentageListPreference = (ListPreference) findPreference(KEY_PAUSE_PERCENTAGE);
        mPauserPercentageListPreference.setOnPreferenceChangeListener(this);
        mPauserPercentageListPreference.setSummary(mPauserPercentageListPreference.getEntry() + "%");
        mPreference = (MyPreference) findPreference(KEY_FAST_MODE_SEETINGS);
        mPreference.setOnBindViewListener(new MyPreference.OnBindViewListener() {
            @Override
            public void onBindViewCalled(PreferenceViewHolder view) {
                mView = view;
                initView(view);
                LogUtils.e("loadSettings onBindViewCalled");
                loadSettings();
            }
        });
    }

    private void initView(PreferenceViewHolder view) {
        mCountChip = (Chip) mView.findViewById(R.id.cp_count);
        mCountChip.setOnCheckedChangeListener(this);
        mRssiChip = (Chip) mView.findViewById(R.id.cp_rssi);
        mRssiChip.setOnCheckedChangeListener(this);
        mAntChip = (Chip) mView.findViewById(R.id.cp_ant);
        mAntChip.setOnCheckedChangeListener(this);
        mFreqChip = (Chip) mView.findViewById(R.id.cp_frequency);
        mFreqChip.setOnCheckedChangeListener(this);
        mTimeChip = (Chip) mView.findViewById(R.id.cp_time);
        mTimeChip.setOnCheckedChangeListener(this);
        mRfuChip = (Chip) mView.findViewById(R.id.cp_rfu);
        mRfuChip.setOnCheckedChangeListener(this);
        mRfuChip.setVisibility(View.GONE);
        mProChip = (Chip) mView.findViewById(R.id.cp_pro);
        mProChip.setOnCheckedChangeListener(this);
        mDataChip = (Chip) mView.findViewById(R.id.cp_data);
        mDataChip.setOnCheckedChangeListener(this);

        mCountChip.setTag(Const.SP_KEY_ITEM_COUNT);
        mRssiChip.setTag(Const.SP_KEY_ITEM_RSSI);
        mAntChip.setTag(Const.SP_KEY_ITEM_ANT);
        mFreqChip.setTag(Const.SP_KEY_ITEM_FREQ);
        mTimeChip.setTag(Const.SP_KEY_ITEM_TIME);
        mRfuChip.setTag(Const.SP_KEY_ITEM_RFU);
        mProChip.setTag(Const.SP_KEY_ITEM_PRO);
        mDataChip.setTag(Const.SP_KEY_ITEM_DATA);

        mChips.clear();
        mChips.add(mCountChip);
        mChips.add(mRssiChip);
        mChips.add(mAntChip);
        mChips.add(mFreqChip);
        mChips.add(mTimeChip);
        mChips.add(mRfuChip);
        mChips.add(mProChip);
        mChips.add(mDataChip);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPauserPercentageListPreference) {
            String val = (String) newValue;
            LogUtils.e("mPauserPercentage:" + val);
            mPauserPercentageListPreference.setValue(val);
            mPauserPercentageListPreference.setSummary(mPauserPercentageListPreference.getEntry() + "%");
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        switch (buttonView.getId()) {
//            case R.id.cp_count:
//                break;
//            case R.id.cp_rssi:
//                break;
//            case R.id.cp_ant:
//                break;
//            case R.id.cp_frequency:
//                break;
//            case R.id.cp_time:
//                break;
//            case R.id.cp_rfu:
//                break;
//            case R.id.cp_pro:
//                break;
//            case R.id.cp_data:
//                break;
//        }
        String key = (String) buttonView.getTag();
        mApp.mSharedPrefManager.putBoolean(key, isChecked);
    }
}
