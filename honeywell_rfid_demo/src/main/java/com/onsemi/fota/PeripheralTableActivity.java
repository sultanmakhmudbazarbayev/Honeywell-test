package com.onsemi.fota;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.honeywell.bccrfid.R;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity of the app is used to show a list of visible BLE devices
 */
public class PeripheralTableActivity extends AppCompatActivity {
    public static FotaPeripheralManager PeripheralManager;
    ListView peripheralsListView;
    Button enableBluetoothButton;
    View bluetoothOffLayout;

    PeripheralAdapter adapter;
    private String mTargetMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetMac = getIntent().getStringExtra("mac");
        startLoopTargetTimer();
        setContentView(R.layout.onsemi_activity_main);

        if(savedInstanceState == null)
        {
            // create the peripheral manager to start the scan an get the list of devices
            PeripheralManager = new FotaPeripheralManagerImpl(this);
            PeripheralManager.addListener(new FotaPeripheralManagerListener() {
                @Override
                public void selectedChanged(FotaPeripheral peripheral) { }
                @Override
                public void selectedChanging(FotaPeripheral peripheral) { }
                @Override
                public void onPeripheralsListUpdated() {
                    updatePeripheralsList();
                }
                @Override
                public void onBluetoothEnabled() {
                    PeripheralManager.startScan();
                    updateViewVisibility();
                }
                @Override
                public void onBluetoothDisabled() {
                    PeripheralManager.stopScan();
                    enableBluetoothButton.setText("Enable");
                    updateViewVisibility();
                }
            });
        }

        bluetoothOffLayout = findViewById(R.id.layout_bluetooth_info);

        peripheralsListView = (ListView)findViewById(R.id.listview_devices);
        adapter = new PeripheralAdapter(this, PeripheralManager.peripherals());
        peripheralsListView.setAdapter(adapter);
        final Intent intent = new Intent(this, BleDeviceActivity.class);
        peripheralsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PeripheralManager.setSelected((FotaPeripheral) adapter.getItem(i));
                startActivity(intent);
            }
        });

        enableBluetoothButton = (Button)findViewById(R.id.button_enable_bluetooth);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter.getDefaultAdapter().enable();
                enableBluetoothButton.setText("Enabling");
            }
        });

        updateViewVisibility();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start the scan when the activity is resumed
        PeripheralManager.clearPeripherals();
        PeripheralManager.startScan();
    }

    @Override
    protected void onPause() {
        // stop the scan when the activity is paused
        PeripheralManager.stopScan();
        super.onPause();
    }

    void updatePeripheralsList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.update(PeripheralManager.peripherals());
            }
        });
    }


    /**
     * Show either the Bluetooth disabled message or the device list
     */
    void updateViewVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(PeripheralManager.isBluetoothDisabled()) {
                    bluetoothOffLayout.setVisibility(View.VISIBLE);
                    peripheralsListView.setVisibility(View.GONE);
                }
                else if(PeripheralManager.isBluetoothEnabled()) {
                    bluetoothOffLayout.setVisibility(View.GONE);
                    peripheralsListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean checkTarget() {
        for (FotaPeripheral fp : PeripheralManager.peripherals()) {
            if (fp.getAddress().equals(mTargetMac) && fp.getName().equals("IH25 FOTA")) {
                final Intent intent = new Intent(this, BleDeviceActivity.class);
                PeripheralManager.setSelected(fp);
                startActivity(intent);
                finish();
                return true;
            }
        }

        return false;
    }

    private void startLoopTargetTimer() {
        mHandler.sendEmptyMessageDelayed(MSG_LOOP_TARGET, 500);
    }

    private MyHandler mHandler = new MyHandler(this);
    private static final int MSG_LOOP_TARGET = 100;

    private static class MyHandler<T> extends Handler {
        private WeakReference<T> wr;

        private MyHandler(T act) {
            wr = new WeakReference<T>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PeripheralTableActivity act = (PeripheralTableActivity) wr.get();

            if (act == null) {
                return;
            }

            switch (msg.what) {
                case MSG_LOOP_TARGET:
                    if (!act.checkTarget()) {
                        act.startLoopTargetTimer();
                    }
                    break;
            }
        }
    }
}
