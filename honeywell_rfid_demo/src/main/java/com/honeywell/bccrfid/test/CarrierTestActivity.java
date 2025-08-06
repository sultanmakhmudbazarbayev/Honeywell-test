package com.honeywell.bccrfid.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.BaseActivity;
import com.honeywell.bccrfid.R;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class CarrierTestActivity extends BaseActivity {
    private static final String TAG = "CarrierTestActivity";
    private App mApp;
    private Spinner mSpinnerPwr;
    private Spinner mSpinnerFreq;
    private ArrayList<Integer> mPower;
    private int[] mFreq;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_test_activity);
        mApp = (App) getApplication();
        initToolBar();
        init();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getTitle());
        //toolbar.setNavigationIcon(R.drawable.position_left);
        setSupportActionBar(toolbar);
    }

    private void init() {
        mPower = new ArrayList<>();

        for (int i = 500; i <= 3000; i += 100) {
            mPower.add(i);
        }

        mSpinnerPwr = findViewById(R.id.spinner_pwr);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mPower);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPwr.setAdapter(adapter);
        mSpinnerPwr.setSelection(0);

        ArrayList d = new ArrayList<String>();

        if (mApp.checkIsRFIDReady()) {
            try {
                mFreq = mApp.rfidReader.getFreqHopTable();
                sort(mFreq);

                for (int i = 0; i < mFreq.length; i++) {
                    d.add(String.valueOf(mFreq[i]));
                }
            } catch (RfidReaderException e) {
                e.printStackTrace();
            }
        }

        mSpinnerFreq = findViewById(R.id.spinner_freq);
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, d);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerFreq.setAdapter(adapter);
        mSpinnerFreq.setSelection(0);
    }

    public void clickBtnStart(View v) {
        if (mFreq == null || mFreq.length == 0 || mFreq.length <= mSpinnerFreq.getSelectedItemId()) {
            Toast.makeText(this, "Invalid frequency!", Toast.LENGTH_SHORT).show();
            return;
        }

        int pwr = mPower.get((int) mSpinnerPwr.getSelectedItemId());
        int freq = mFreq[(int) mSpinnerFreq.getSelectedItemId()];

        if (mApp.checkIsRFIDReady()) {
            try {
                Method method = mApp.rfidReader.getClass().getDeclaredMethod("startCarrierTest", int.class, int.class);
                method.setAccessible(true);
                method.invoke(mApp.rfidReader, pwr, freq);
                Toast.makeText(this, "Carrier test started!\nfreq = " + freq, Toast.LENGTH_SHORT).show();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickBtnStop(View v) {
        if (mApp.checkIsRFIDReady()) {
            try {
                Method method = mApp.rfidReader.getClass().getDeclaredMethod("stopCarrierTest");
                method.setAccessible(true);
                method.invoke(mApp.rfidReader);
                Toast.makeText(this, "Carrier test stop!",Toast.LENGTH_SHORT).show();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public int[] sort(int[] array) {
        int tmpIntValue = 0;

        for (int xIndex = 0; xIndex < array.length; xIndex++) {
            for (int yIndex = 0; yIndex < array.length; yIndex++) {
                if (array[xIndex] < array[yIndex]) {
                    tmpIntValue = (Integer) array[xIndex];
                    array[xIndex] = array[yIndex];
                    array[yIndex] = tmpIntValue;
                }
            }
        }

        return array;
    }


    public static String toHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder();
        int c;

        for (byte b : bArray) {
            c = b & 0xff;

            if (c < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(c));
            sb.append(" ");
        }

        return sb.toString();
    }

    public static String toHexString(byte[] bArray, int start, int len) {
        return toHexString(bArray, start, len, " ");
    }

    public static String toHexString(byte[] bArray, int start, int len,
                                     String separator) {
        if (bArray.length < start + len) {
            Log.e(TAG, "toHexString() overflow,    bArray.length="
                    + bArray.length + ", start=" + start + ", len=" + len);
            return null;
        }

        StringBuffer sb = new StringBuffer();
        int c;

        for (int i = start; i < len; i++) {
            c = bArray[i] & 0xff;

            if (c < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(c));

            if (separator != null) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }
}
