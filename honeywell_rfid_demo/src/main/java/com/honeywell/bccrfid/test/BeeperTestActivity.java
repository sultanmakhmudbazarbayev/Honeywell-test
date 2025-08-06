package com.honeywell.bccrfid.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.BaseActivity;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class BeeperTestActivity extends BaseActivity {
    private TextInputEditText mBeeperFreqEv;
    private TextInputEditText mBeeperStrengthEv;
    private TextInputEditText mCMDSendGapEv;
    private Button mAutoEnableBeeperBtn;
    private Button mManualEnableBeeperBtn;
    private Button mAutoDisableBeeperBtn;
    private TextView mRecordTv;
    private final static int CMD_GAP_RANDOM_MULTI = 3;
    private Handler mHandler;
    private LimitQueue<String> mCMDRecordQueue = new LimitQueue<String>(30);
    private App mApp;


    private boolean mIsAuoEnabled = false;

    private byte[] mHoneyCMD;
    boolean mSwitchByte;
    byte mFreq;
    byte mStrength;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beeper_test_main);
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
        mApp = (App) getApplication();
        mBeeperFreqEv = (TextInputEditText) findViewById(R.id.edtv_freq);
        mBeeperStrengthEv = (TextInputEditText) findViewById(R.id.edtv_strength);
        mCMDSendGapEv = (TextInputEditText) findViewById(R.id.edtv_cmd_gap);
        mAutoEnableBeeperBtn = (Button) findViewById(R.id.cmd_enable_beeper_auto);
        mManualEnableBeeperBtn = (Button) findViewById(R.id.cmd_enable_beeper_manual);
        mAutoDisableBeeperBtn = (Button) findViewById(R.id.cmd_disable_beeper);
        mRecordTv = (TextView) findViewById(R.id.cmd_record);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        mSwitchByte = true;
                        mFreq = getFreq();
                        mStrength = getStrength();
                        setBeeper();
                        Message message = Message.obtain();
                        message.what = 1;
                        this.sendMessageDelayed(message, (long) (randonGap(Double.valueOf(mCMDSendGapEv.getText().toString()))));
                        break;
                }
            }
        };
    }

    private double randonGap(double time) {
        return time + Math.random() * (CMD_GAP_RANDOM_MULTI - 1) * time;
    }

    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.cmd_enable_beeper_auto:
                    if (mCMDSendGapEv.getText().toString().length() == 0) {
                        Toast.makeText(BeeperTestActivity.this, "input gap pls!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mIsAuoEnabled) {
                        Toast.makeText(this, "Auto enable is running!", Toast.LENGTH_SHORT).show();
                    } else {
                        mHandler.sendEmptyMessage(1);
                        mIsAuoEnabled = true;
                    }
                    break;
                case R.id.cmd_enable_beeper_manual:
                    mSwitchByte = true;
                    mFreq = getFreq();
                    mStrength = getStrength();
                    setBeeper();
                    break;
                case R.id.cmd_disable_beeper:
                    mHandler.removeCallbacksAndMessages(null);
                    mIsAuoEnabled = false;
                    mSwitchByte = false;
                    mFreq = (byte) 0x00;
                    mStrength = (byte) 0x00;
                    setBeeper();
                    break;
                case R.id.cmd_clear_record:
                    mCMDRecordQueue.clear();
                    StringBuffer record = new StringBuffer();
                    for (int i = mCMDRecordQueue.size() - 1; i >= 0; i--) {
                        record = record.append(mCMDRecordQueue.get(i));
                    }
                    mRecordTv.setText(record);
                    break;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Input correct value!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setBeeper() {
//        updateRecordText();
        if (mApp.checkIsRFIDReady()) {
            mApp.rfidMgr.setBeeper(mSwitchByte, mFreq, mStrength);
        }
    }

    private byte getFreq() {
        String mFreq = mBeeperFreqEv.getText().toString();
        if (mFreq.length() > 0) {
            return (byte) (Integer.valueOf(mFreq) & 0xFF);
        }
        return (byte) (((int) (Math.random() * 11)) & 0xFF);
    }

    private byte getStrength() {
        String mStrength = mBeeperStrengthEv.getText().toString();
        if (mStrength.length() > 0) {
            return (byte) (Integer.valueOf(mStrength) & 0xFF);
        }
        return (byte) (((int) (Math.random() * 11)) & 0xFF);
    }

    private void getGap() {

    }

    private void updateRecordText() {
        String result = "";
        for (int i = 0; i < mHoneyCMD.length; i++) {
            //result=result+Integer.toHexString(characteristic.getValue()[i]);
            result = result + String.format("%02x", mHoneyCMD[i]) + " ";
        }
        LogUtils.e("potter", "mHoneyCMD:" + result);

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateStr = dateformat.format(System.currentTimeMillis());
        try {
            mCMDRecordQueue.offer(dateStr + "------" + result + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String record = "";
        StringBuffer record = new StringBuffer();
        for (int i = mCMDRecordQueue.size() - 1; i >= 0; i--) {
            record = record.append(mCMDRecordQueue.get(i));
        }
        mRecordTv.setText(record);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class LimitQueue<E> {

        private int limit;

        private LinkedList<E> queue = new LinkedList<E>();

        public LimitQueue(int limit) {
            this.limit = limit;
        }

        public void offer(E e) {
            if (queue.size() >= limit) {
                queue.poll();
            }
            queue.offer(e);
        }

        public E get(int position) {
            return queue.get(position);
        }

        public E getLast() {
            return queue.getLast();
        }

        public E getFirst() {
            return queue.getFirst();
        }

        public int getLimit() {
            return limit;
        }

        public int size() {
            return queue.size();
        }

        public void clear() {
            queue = new LinkedList<E>();
        }
    }
}
