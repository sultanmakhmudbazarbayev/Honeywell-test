package com.honeywell.bccrfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.bccrfid.test.RfidFwUpdateActivity;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.rfidservice.ConnectionState;
import com.honeywell.rfidservice.EventListener;
import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.TriggerMode;
import com.honeywell.rfidservice.rfid.RfidReader;
import com.honeywell.rfidservice.utils.GpsUtil;
import com.onsemi.fota.PeripheralTableActivity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class ReaderBTControl extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String TAG = "ReaderBTControl";
    private Switch mBTSwitch;
    private TextView mBTSeartTv;
    private Button mAccessReadBtn;
    private View mView;
    private Switch mAutoConnectSwitch;

    private Context mContext;
    private App mApp;

    private MyHandler mMyHandler;
    private ProgressDialog mProgressDialog;
    private static final int REQUEST_ENABLE_BT = 1;

    public static final int MSG_FINDING_BLE_SERVICES = 0;
    public static final int MSG_CREATING_READER = 1;
    public static final int MSG_CREATE_READER_SUCCESSFULLY = 2;
    public static final int MSG_CREATE_READER_FAILED = 3;
    public static final int MSG_BT_SCAN_TIMEOUT = 100;
    public static final int MSG_ON_BLE_DEV_FOUND = 101;
    public static final int MSG_DEALY_CREATE_READER = 102;

    public static final String KEY_MAC_ADDR = "mac_addr";
    private static final String SP_NAME = "HoneywellRFID";
    private static final String SP_KEY_AUTO_CONNECT = "auto_connect";
    private static final int NORMAL_BT_SEARCH_TIME = 10000;
    private static final int AUTO_CONNECT_BT_SEARCH_TIME = 3000;

    private RfidManager mRfidMgr;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mBleScanDevices = App.getInstance().bleScanDevices;
    private Map<String, Integer> mBleScanDevicesRssi = App.getInstance().bleScanDevicesRssi;
    private ListView mLvBleScan;
    private BluetoothDeviceListAdapter mBleScanListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_control_main);
        initToolBar();
        init();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getTitle());
        toolbar.setNavigationIcon(R.drawable.position_left);
        //toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.topbar_more));
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bt_control, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.rfid_firmware_update).setVisible(mApp.enableRfidFwUpdate);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.barcode_way:
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, getString(R.string.toast_bt_is_closed), Toast.LENGTH_SHORT).show();
                } else if (mAutoConnectSwitch.isChecked()) {
                    Toast.makeText(this, getString(R.string.toast_autoconnect_should_close), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, BarcodeBTActivity.class);
                    startActivityForResult(intent, 1);

                    if (!mBluetoothAdapter.isDiscovering()) {
                        startSearch();
                    }
                }
                break;
            case R.id.ble_fota_update:
                if (isConnected()) {
                    mRfidMgr.setAutoReconnect(false);
                    mRfidMgr.enableBluetoothFota();

                    if (getSelectedDev() != null) {
                        Intent in = new Intent(ReaderBTControl.this, PeripheralTableActivity.class);
                        Log.i(TAG, "Selected OTA MAC: " + getSelectedDev());
                        in.putExtra("mac", getSelectedDev().getAddress());
                        startActivity(in);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_error1), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rfid_firmware_update:
                if (isConnected()) {
                    mRfidMgr.setAutoReconnect(false);

                    if (getSelectedDev() != null) {
                        Intent in = new Intent(ReaderBTControl.this, RfidFwUpdateActivity.class);
                        startActivity(in);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_error1), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        stopSearch();

        if (resultCode == Activity.RESULT_OK) {
            final String mac = data.getStringExtra(KEY_MAC_ADDR);
            disconnect();
            BluetoothDevice btDevice = null;

            try {
                btDevice = mBluetoothAdapter.getRemoteDevice(mac);
            } catch (java.lang.IllegalArgumentException e) {
                Log.e(TAG, mac + " is not a valid Bluetooth address");
                e.printStackTrace();
                return;
            }

            setSelectedDev(btDevice);

            if (!mBleScanDevices.contains(btDevice)) {
                mBleScanDevices.add(btDevice);
            }

            connect(mac);

            mMyHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAccessReadBtn.performClick();
                }
            }, 1500);
        }
    }

    private void init() {
        mContext = this;
        mApp = (App) getApplication();
        mRfidMgr = mApp.getRfidManager();

        mView = findViewById(R.id.group);
        mBTSwitch = (Switch) findViewById(R.id.bt_switch);
        mAutoConnectSwitch = (Switch) findViewById(R.id.auto_connect_switch);
        mBTSeartTv = findViewById(R.id.btn_search);
        mLvBleScan = (ListView) findViewById(R.id.bt_list);
        mAccessReadBtn = (Button) findViewById(R.id.access_read);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBTSeartTv.setOnClickListener(this);
        mAccessReadBtn.setOnClickListener(this);
        mBTSwitch.setChecked(mBluetoothAdapter.isEnabled());
        mBTSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        SharedPreferences sp = this.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        boolean autoConnectState = sp.getBoolean(SP_KEY_AUTO_CONNECT, false);
        mAutoConnectSwitch.setChecked(autoConnectState);
        mAutoConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = ReaderBTControl.this.getSharedPreferences(SP_NAME,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SP_KEY_AUTO_CONNECT, isChecked);
                editor.commit();
            }
        });

        mBleScanListAdapter = new BluetoothDeviceListAdapter(this, mBleScanDevices);
        mLvBleScan.setAdapter(mBleScanListAdapter);
        mLvBleScan.setOnItemClickListener(this);
        mLvBleScan.setOnItemLongClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading_text));
        mMyHandler = new MyHandler(this);
        setVisibility();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        registerReceiver(mBTReceiver, filter);
    }

    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case MSG_FINDING_BLE_SERVICES:
                mProgressDialog.setTitle(getString(R.string.finding_ble_service));
                mProgressDialog.show();
                break;
            case MSG_CREATING_READER:
                mProgressDialog.setTitle(getString(R.string.creating_reader));
                mProgressDialog.show();
                break;
            case MSG_CREATE_READER_SUCCESSFULLY:
                mProgressDialog.dismiss();
                Toast.makeText(ReaderBTControl.this, getString(R.string.toast_create_reader_successfully), Toast.LENGTH_SHORT).show();
                break;
            case MSG_CREATE_READER_FAILED:
                mProgressDialog.dismiss();
                Toast.makeText(ReaderBTControl.this, getString(R.string.toast_create_reader_failed), Toast.LENGTH_SHORT).show();
                break;
            case MSG_BT_SCAN_TIMEOUT:
                searchTimeout();
                break;
            case MSG_ON_BLE_DEV_FOUND:
                int rssi = msg.arg1;
                BluetoothDevice device = (BluetoothDevice) msg.obj;

                if (!mBleScanDevices.contains(device)) {
                    mBleScanDevices.add(device);
                    mBleScanListAdapter.notifyDataSetChanged();
                }

                mBleScanDevicesRssi.put(device.getAddress(), rssi);
                long cur = System.currentTimeMillis();

                if (cur - mPrevListUpdateTime > 250) {
                    mBleScanListAdapter.notifyDataSetChanged();
                    mPrevListUpdateTime = cur;
                }
                break;
            case MSG_DEALY_CREATE_READER:
                readyForCreateReader();
                break;
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference ref;

        private MyHandler(ReaderBTControl act) {
            ref = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (ref.get() != null) {
                ((ReaderBTControl) ref.get()).handleMessage(msg);
            }
        }
    }

    private void readyForCreateReader() {
        String v = null;

        for (int i = 0; i < 3; i++) {
            v = mApp.rfidMgr.getBluetoothModuleSwVersion();
            Log.i(TAG, "Bluetooth firmware version: " + v);

            if (v != null && !v.isEmpty()) {
                if (v.compareTo("2.0.0") <= 0) {
                    showToast("Bluetooth firmware (" + v + ") is too old,\n\nPlease upgrade to new version.", true);
                    break;
                }
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mAccessReadBtn.setEnabled(true);
    }

    /**
     * Listen RFID device event.
     */
    private EventListener mEventListner = new EventListener() {
        @Override
        public void onDeviceConnected(Object data) {
            Log.i(TAG, "onDeviceConnected: " + data);
//            mAccessReadBtn.setEnabled(true);
            mBleScanListAdapter.notifyDataSetChanged();
            mMyHandler.sendEmptyMessageDelayed(MSG_DEALY_CREATE_READER, 1000);
        }

        @Override
        public void onDeviceDisconnected(Object data) {
            Log.i(TAG, "onDeviceDisconnected: " + data);
            mAccessReadBtn.setEnabled(false);
            mBleScanListAdapter.notifyDataSetChanged();
            mMyHandler.removeMessages(MSG_DEALY_CREATE_READER);
        }

        @Override
        public void onReaderCreated(boolean success, RfidReader reader) {
            if (success) {
                mApp.rfidReader = reader;
                mMyHandler.sendEmptyMessage(MSG_CREATE_READER_SUCCESSFULLY);
                Intent intent = new Intent(ReaderBTControl.this, ReaderMain.class);
                startActivity(intent);
            } else {
                mMyHandler.sendEmptyMessage(MSG_CREATE_READER_FAILED);
            }
        }

        @Override
        public void onRfidTriggered(boolean trigger) {
        }

        @Override
        public void onTriggerModeSwitched(TriggerMode curMode) {
        }
    };

    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
            } else {
                stopSearch();
                mBluetoothAdapter.disable();
                mAccessReadBtn.setEnabled(false);
            }
        }
    };

    private void setVisibility() {
        if (mBluetoothAdapter.isEnabled()) {
            mView.setVisibility(View.VISIBLE);
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                onBtStateChange(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0));
            }
        }
    };

    private void onBtStateChange(int state) {
        mBleScanListAdapter.clear();

        if (state == BluetoothAdapter.STATE_ON) {
            mBTSwitch.setOnCheckedChangeListener(null);
            mBTSwitch.setChecked(true);
            mBTSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
            setVisibility();
        } else if (state == BluetoothAdapter.STATE_OFF) {
            mBTSwitch.setOnCheckedChangeListener(null);
            mBTSwitch.setChecked(false);
            mBTSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
            setVisibility();

            mAccessReadBtn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                startSearch();
                break;
            case R.id.access_read:
                onClickCreateReader();
                break;
            default:
                break;
        }
    }

    private void onClickCreateReader() {
//                if (Common.DEVICE_AUTHENTICATION && RfidManager.getInstance(this).getState() != RfidManager.STATE_READY) {
//                    Toast.makeText(this, getString(R.string.dev_not_certified), Toast.LENGTH_SHORT).show();
//                } else
        if (isConnected()) {
            mRfidMgr.createReader();
            mMyHandler.sendEmptyMessage(MSG_CREATING_READER);
        } else {
            Toast.makeText(this, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private long mPrevListUpdateTime = 0;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null) {
                Message msg = mMyHandler.obtainMessage(MSG_ON_BLE_DEV_FOUND);
                msg.arg1 = rssi;
                msg.obj = device;
                mMyHandler.sendMessage(msg);
            }
        }
    };

    private void startSearch() {
        if (!GpsUtil.isOpen(this)) {
            DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case Dialog.BUTTON_POSITIVE:
                            GpsUtil.openGPS(ReaderBTControl.this);
                            break;
                        case Dialog.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_enable_location);
            builder.setPositiveButton(R.string.ok, dialogOnclicListener);
            builder.setNegativeButton(R.string.cancel, dialogOnclicListener);
            builder.create().show();
            return;
        }

        setSelectedDev(null);
        mBleScanDevices.clear();
        mBleScanDevicesRssi.clear();
        mBleScanListAdapter.clear();
        disconnect();

        boolean ret = mBluetoothAdapter.startLeScan(mLeScanCallback);
        mBTSeartTv.setText(getString(R.string.searching));
        mBTSeartTv.setEnabled(false);

        mMyHandler.sendEmptyMessageDelayed(MSG_BT_SCAN_TIMEOUT, mAutoConnectSwitch.isChecked() ? AUTO_CONNECT_BT_SEARCH_TIME : NORMAL_BT_SEARCH_TIME);
    }

    private void searchTimeout() {
        stopSearch();

        if (mAutoConnectSwitch.isChecked()) {
            autoConnect();
        }
    }

    private void autoConnect() {
        if (mBleScanDevices.size() > 0) {
            Set<Map.Entry<String, Integer>> set = mBleScanDevicesRssi.entrySet();
            int min = Integer.MIN_VALUE;
            String mac = null;

            for (Map.Entry<String, Integer> entry : set) {
                if (entry.getValue() > min) {
                    min = entry.getValue();
                    mac = entry.getKey();
                }
            }

            for (BluetoothDevice dev : mBleScanDevices) {
                if (dev.getAddress().equals(mac)) {
                    setSelectedDev(dev);
                    connect(mac);
                    mBleScanListAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    private void stopSearch() {
        mMyHandler.removeMessages(MSG_BT_SCAN_TIMEOUT);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mBTSeartTv.setText(getString(R.string.search));
        mBTSeartTv.setEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtils.e("onItemClick");
        ListView lv = (ListView) parent;
        final BluetoothDevice device = (BluetoothDevice) lv.getAdapter()
                .getItem(position);
        stopSearch();

        if (device.getName().contains("FOTA")) {
            setSelectedDev(device);
            DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case Dialog.BUTTON_POSITIVE:
                            mRfidMgr.setAutoReconnect(false);

                            Intent in = new Intent(ReaderBTControl.this, PeripheralTableActivity.class);
                            Log.i(TAG, "Selected OTA MAC: " + getSelectedDev());
                            in.putExtra("mac", getSelectedDev().getAddress());
                            startActivity(in);
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.ble_fota_update_try_again);
            builder.setPositiveButton(R.string.ok, dialogOnclicListener);
            builder.setNegativeButton(R.string.cancel, dialogOnclicListener);
            builder.create().show();
            return;
        }

        if (!isConnected() || device != getSelectedDev()) {
            disconnect();

            connect(device.getAddress());
            setSelectedDev(device);
            view.setBackgroundColor(Color.YELLOW);
            mBleScanListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = mBleScanDevices.get(position);

        if (isConnected() && device.getAddress().equals(getSelectedDev().getAddress())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_title_disconnect);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.dialog_text_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            disconnect();
                            dialog.cancel();
                        }
                    });
            builder.setNegativeButton(R.string.dialog_text_no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        mRfidMgr.addEventListener(mEventListner);
        mAccessReadBtn.setEnabled(isConnected());
        mBleScanListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRfidMgr.removeEventListener(mEventListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBTReceiver);
        mMyHandler.removeCallbacksAndMessages(null);
    }

    public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
        private int colorDefault;
        private int colorConnected;
        private int colorDisconnected;

        public BluetoothDeviceListAdapter(Context context, List<BluetoothDevice> list) {
            super(context, 0, list);
            colorDefault = getResources().getColor(android.R.color.background_light);
            colorConnected = context.getResources().getColor(R.color.list_item_bg1);
            colorDisconnected = context.getResources().getColor(R.color.list_item_bg2);
        }

        class ViewHolder {
            TextView tvName;
            TextView tvAddress;
            TextView tvRssi;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder vh;

            if (convertView == null) {
                v = LayoutInflater.from(mContext).inflate(
                        R.layout.listview_item, parent, false);
                vh = new ViewHolder();
                vh.tvName = (TextView) v.findViewById(android.R.id.text1);
                vh.tvAddress = (TextView) v.findViewById(android.R.id.text2);
                vh.tvRssi = (TextView) v.findViewById(R.id.rssi);
                v.setTag(vh);
            } else {
                v = convertView;
                vh = (ViewHolder) v.getTag();
            }

            BluetoothDevice device = getItem(position);
            vh.tvName.setText(device.getName() != null ? device.getName() : device.getAddress());
            vh.tvRssi.setText(getString(R.string.tv_rssi) + mBleScanDevicesRssi.get(device.getAddress()));
            vh.tvAddress.setText(device.getAddress());
            v.setBackgroundColor(colorDefault);

            if (device.equals(getSelectedDev())) {
                switch (mRfidMgr.getConnectionState()) {
                    case STATE_CONNECTED:
                        vh.tvAddress.setText(getString(R.string.state_connected));
                        v.setBackgroundColor(colorConnected);
                        break;
                    case STATE_CONNECTING:
                        vh.tvAddress.setText(getString(R.string.state_connecting));
                        v.setBackgroundColor(colorDisconnected);
                        break;
                    case STATE_DISCONNECTED:
                        vh.tvAddress.setText(device.getAddress());
                        v.setBackgroundColor(colorDisconnected);
                        break;
                    default:
                        break;
                }
            }

            return v;
        }
    }

    private ConnectionState getCnntState() {
        return mRfidMgr.getConnectionState();
    }

    private boolean isConnected() {
        return getCnntState() == ConnectionState.STATE_CONNECTED;
    }

    private void connect(String mac) {
        mRfidMgr.setAutoReconnect(true);
        mRfidMgr.connect(mac);
    }

    private void disconnect() {
        mAccessReadBtn.setEnabled(false);
        mRfidMgr.disconnect();
    }

    private BluetoothDevice getSelectedDev() {
        return App.getInstance().selectedBleDev;
    }

    private void setSelectedDev(BluetoothDevice dev) {
        App.getInstance().selectedBleDev = dev;
    }
}
