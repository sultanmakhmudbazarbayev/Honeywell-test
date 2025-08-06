package com.honeywell.bccrfid.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.SwitchPreference;

public class MySwitchPreference extends SwitchPreference {
    public MySwitchPreference(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.switchPreferenceStyle);
    }
}
