package com.honeywell.bccrfid;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.honeywell.bccrfid.fragment.BaseFragment;
import com.honeywell.bccrfid.fragment.ReaderInventoryFragment;
import com.honeywell.bccrfid.fragment.ReaderLocateFragment;
import com.honeywell.bccrfid.fragment.ReaderSettingsFragment;
import com.honeywell.bccrfid.fragment.ReaderWriteLockTagFragment;
import com.honeywell.bccrfid.utils.LogUtils;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ReaderMain extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        BaseFragment.OnFragmentInteractionListener {
    private static final String TAG = "ReaderMain";
    private BottomNavigationView mBottomNavigationView;
    private int mSelectedIndex;
    private FragmentManager mFragmentManager;
    private ReaderInventoryFragment mReaderReadFrag;
    private ReaderWriteLockTagFragment mReaderWriteLockTagFrag;
    private ReaderSettingsFragment mReaderSettingsFrag;
    private ReaderLocateFragment mReaderLocaleFrag;
    private boolean mBatteryReminder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBasicHandler = new MyHandler<>(this);
        startLoopTemperatureTimer();
        setContentView(R.layout.reader_main);
        mSelectedIndex = getIntent().getIntExtra("index", 0);
        init(mSelectedIndex);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void init(int index) {
        mFragmentManager = getSupportFragmentManager();
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setSelectedItemId(mBottomNavigationView.getMenu().getItem(index).getItemId());
    }

    private void loadFragment(int index) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        hideAllTabs(fragmentTransaction);
        switch (index) {
            case 0:
                if (mReaderReadFrag == null) {
                    mReaderReadFrag = ReaderInventoryFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_frame, mReaderReadFrag);
                } else {
                    fragmentTransaction.show(mReaderReadFrag);
                }
                break;
            case 1:
                if (mReaderWriteLockTagFrag == null) {
                    mReaderWriteLockTagFrag = ReaderWriteLockTagFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_frame, mReaderWriteLockTagFrag);
                } else {
                    fragmentTransaction.show(mReaderWriteLockTagFrag);
                }
                break;
            case 2:
                if (mReaderLocaleFrag == null) {
                    mReaderLocaleFrag = ReaderLocateFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_frame, mReaderLocaleFrag);
                } else {
                    fragmentTransaction.show(mReaderLocaleFrag);
                }
                break;
            case 3:
                if (mReaderSettingsFrag == null) {
                    mReaderSettingsFrag = ReaderSettingsFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_frame, mReaderSettingsFrag);
                } else {
                    fragmentTransaction.show(mReaderSettingsFrag);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideAllTabs(FragmentTransaction fragTran) {
        if (mReaderReadFrag != null) {
            fragTran.hide(mReaderReadFrag);
        }
        if (mReaderWriteLockTagFrag != null) {
            fragTran.hide(mReaderWriteLockTagFrag);
        }
        if (mReaderSettingsFrag != null) {
            fragTran.hide(mReaderSettingsFrag);
        }
        if (mReaderLocaleFrag != null) {
            fragTran.hide(mReaderLocaleFrag);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        LogUtils.e("onNavigationItemSelected");
        LogUtils.e("mBottomNavigationView.getSelectedItemId():" + mBottomNavigationView.getSelectedItemId());
        LogUtils.e("item.getItemId():" + item.getItemId());
        int lastSelectedItemId = mBottomNavigationView.getSelectedItemId();
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.reader_read_item:
                loadFragment(0);
                break;
            case R.id.reader_writelock_item:
                loadFragment(1);
                break;
            case R.id.reader_locate_item:
                loadFragment(2);
                break;
            case R.id.reader_setting_item:
                loadFragment(3);
                break;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    private void startLoopTemperatureTimer() {
        mBasicHandler.sendEmptyMessageDelayed(MSG_LOOP_TEMP, 60 * 1000);
    }

    private MyHandler mBasicHandler;
    private static final int MSG_LOOP_TEMP = 100;

    private static class MyHandler<T> extends Handler {
        private WeakReference<T> wr;

        private MyHandler(T act) {
            wr = new WeakReference<T>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ReaderMain act = (ReaderMain) wr.get();

            if (act == null) {
                return;
            }

            switch (msg.what) {
                case MSG_LOOP_TEMP:
                    App app = (App) act.getApplication();
                    if (app.checkIsRFIDReady()) {
                        float t = app.rfidMgr.getBatteryTemperature();
                        int chargeCycle = app.rfidMgr.getBatteryChargeCycle();
                        app.batteryTemperature = t;
                        app.batteryChargeCycle = chargeCycle;
                        Log.i(TAG, "Battery temperature: " + t + "    charge cycle: " + chargeCycle);

                        if (chargeCycle > 500 && !act.mBatteryReminder) {
                            act.mBatteryReminder = true;
                            Toast.makeText(act, R.string.toast_battery_charge_cycle_reminder, Toast.LENGTH_LONG).show();
                        }
                    }

                    act.startLoopTemperatureTimer();
                    break;
            }
        }
    }
}
