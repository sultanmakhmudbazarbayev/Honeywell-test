package com.honeywell.bccrfid;

import android.content.Intent;
import android.os.Bundle;

import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.bccrfid.scan.BarcodeManager;
import com.honeywell.bccrfid.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class BarcodeBTActivity extends BaseActivity implements BarcodeReader.BarcodeListener {
    private BarcodeManager mBarcodeManager;
    private String mMacAddr = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        initScan();
    }

    private void initScan() {
        mBarcodeManager = new BarcodeManager(this);
        mBarcodeManager.setBarcodeListener(this);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        LogUtils.e("---onFailureEvent---    " + barcodeFailureEvent.toString());
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        LogUtils.e("---onBarcodeEvent---    " + barcodeReadEvent.getBarcodeData());
        final String result = barcodeReadEvent.getBarcodeData();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has(ReaderBTControl.KEY_MAC_ADDR)) {
                mMacAddr = jsonObject.getString(ReaderBTControl.KEY_MAC_ADDR);
            } else if (jsonObject.has("mac")) {
                mMacAddr = jsonObject.getString("mac");
            } else if (jsonObject.has("macaddr")) {
                mMacAddr = jsonObject.getString("macaddr");
            } else {
                showToast(R.string.toast_parse_data_fail);
                return;
            }

            showToast(getString(R.string.toast_parse_data_successfully) + mMacAddr);
            Intent intent = new Intent();
            intent.putExtra(ReaderBTControl.KEY_MAC_ADDR, mMacAddr);
            setResult(RESULT_OK, intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(R.string.toast_parse_data_fail);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBarcodeManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBarcodeManager.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBarcodeManager.onStop();
    }
}
