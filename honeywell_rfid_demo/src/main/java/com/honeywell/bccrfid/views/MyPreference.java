package com.honeywell.bccrfid.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.honeywell.bccrfid.utils.LogUtils;

public class MyPreference extends Preference {
    private PreferenceViewHolder mView;
    private OnBindViewListener mOnBindViewListener;

    public MyPreference(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        LogUtils.e("onBindViewHolder1111111111");
        mView = holder;
        if (mOnBindViewListener != null) {
            mOnBindViewListener.onBindViewCalled(mView);
        }

    }

    public PreferenceViewHolder getView() {
        return mView;
    }

    public static interface OnBindViewListener {
        void onBindViewCalled(PreferenceViewHolder view);
    }
    public void setOnBindViewListener(
            OnBindViewListener onBindViewListener) {
        mOnBindViewListener = onBindViewListener;
    }
}
