package com.honeywell.bccrfid.scan;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeDeviceConnectionEvent;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.BarcodeReaderInfo;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.List;


public class BarcodeManager implements AidcManager.BarcodeDeviceListener, BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {
    private static final String TAG = BarcodeManager.class.getSimpleName();
    private AidcManager mManager;

    private BarcodeReader mReader;

    private BarcodeReader mBarcodeReader;
    private BarcodeReader mInternalScannerReader;
    private BarcodeReader mRingScannerReader;
    private Context context;
    private BarcodeReader.BarcodeListener barcodeListener;

    public BarcodeManager(Context context) {
        this.context = context;

        try {
            initBarcode();
        } catch (Exception e) {
            Log.w("", e);
        }
    }

    public void setBarcodeListener(BarcodeReader.BarcodeListener listener) {
        this.barcodeListener = listener;
    }


    private void initBarcode() {
        AidcManager.create(context, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                mManager = aidcManager;
                mManager.addBarcodeDeviceListener(BarcodeManager.this);
                initAllBarcodeReaderAndSetDefault();
            }
        });
    }

    private void initAllBarcodeReaderAndSetDefault() {
        List<BarcodeReaderInfo> readerList = mManager.listConnectedBarcodeDevices();

        if (readerList.size() <= 2) { /*Only support one ring scanner!*/
            for (BarcodeReaderInfo reader : readerList) {
                if ("dcs.scanner.imager".equals(reader.getName()))
                    mInternalScannerReader = initBarcodeReader(mInternalScannerReader, reader.getName());
                else
                    mRingScannerReader = initBarcodeReader(mRingScannerReader, reader.getName());
            }
        }

        if (readerList.size() == 2) {
            mBarcodeReader = mRingScannerReader;
        } else if (readerList.size() == 1) {
            mBarcodeReader = mInternalScannerReader;
        } else
            Log.d(TAG, "No reader find");
    }


    private BarcodeReader initBarcodeReader(BarcodeReader mReader, String mReaderName) {
        if (mReader == null) {
            if (mReaderName == null)
                mReader = mManager.createBarcodeReader();
            else
                mReader = mManager.createBarcodeReader(mReaderName);

            try {
                // apply settings
                mReader.setProperty(BarcodeReader.PROPERTY_AZTEC_ENABLED,
                        true);

                // set the trigger mode to client control
                mReader.setProperty(
                        BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);

                mReader.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_SCAN_TO_INTENT, false);
                mReader.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);
                mReader.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_EZ_CONFIG, false);

            } catch (UnsupportedPropertyException e) {
                Toast.makeText(context,
                        "Failed to apply properties", Toast.LENGTH_SHORT)
                        .show();
            }

            // register bar code event listener
            mReader.addBarcodeListener(this);
            mReader.addTriggerListener(this);

            try {
                mReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(context,
                        "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }

        return mReader;
    }

    private void doScan(boolean doScan) {
        try {
            mBarcodeReader.aim(doScan);
            mBarcodeReader.light(doScan);
            mBarcodeReader.decode(doScan);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
    }


    public void onResume() {
        // claim the scanner to gain full control
        if (mRingScannerReader != null) {
            try {
                mRingScannerReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(context, "Scanner unavailable", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        if (mInternalScannerReader != null) {
            try {
                mInternalScannerReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(context, "Scanner unavailable", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onStop() {

        try {
            if (mRingScannerReader != null) {
                // release the scanner claim so we don't get any scanner
                // notifications while paused.
                mRingScannerReader.release();
            }

            if (mInternalScannerReader != null) {
                // release the scanner claim so we don't get any scanner
                // notifications while paused.
                mInternalScannerReader.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (mRingScannerReader != null) {
            mRingScannerReader.removeBarcodeListener(this);
            mRingScannerReader.removeTriggerListener(this);
            mRingScannerReader.close();
            mRingScannerReader = null;
        }

        if (mInternalScannerReader != null) {
            mInternalScannerReader.removeBarcodeListener(this);
            mInternalScannerReader.removeTriggerListener(this);
            mInternalScannerReader.close();
            mInternalScannerReader = null;
        }

        if (mManager != null) {
            mManager.removeBarcodeDeviceListener(this);
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            mManager.close();
        }
    }

    @Override
    public void onBarcodeDeviceConnectionEvent(BarcodeDeviceConnectionEvent barcodeDeviceConnectionEvent) {
        BarcodeReaderInfo readerInfo = barcodeDeviceConnectionEvent.getBarcodeReaderInfo();
        int readerStatus = barcodeDeviceConnectionEvent.getConnectionStatus();

        initAllBarcodeReaderAndSetDefault();

        Log.d(TAG, readerInfo.getName() + " Connection status: " + readerStatus);
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        barcodeListener.onBarcodeEvent(barcodeReadEvent);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        barcodeListener.onFailureEvent(barcodeFailureEvent);
    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {
        doScan(triggerStateChangeEvent.getState());
    }
}
