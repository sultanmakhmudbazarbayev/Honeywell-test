package com.honeywell.bccrfid.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.BaseActivity;
import com.honeywell.bccrfid.Const;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.ReaderReadAdapter;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.bccrfid.utils.MathHelper;
import com.honeywell.bccrfid.utils.SpUtil;
import com.honeywell.bccrfid.utils.StrUtil;
import com.honeywell.rfidservice.EventListener;
import com.honeywell.rfidservice.TriggerMode;
import com.honeywell.rfidservice.rfid.OnTagReadListener;
import com.honeywell.rfidservice.rfid.RfidReader;
import com.honeywell.rfidservice.rfid.TagAdditionData;
import com.honeywell.rfidservice.rfid.TagReadData;
import com.honeywell.rfidservice.rfid.TagReadOption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReaderInventoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderInventoryFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "ReaderInventoryFragment";

    private boolean mIsResumed;
    private boolean mIsShow = true;
    private OnFragmentInteractionListener mListener;
    private SoundPool mSoundPool;
    private int mSoundId;
    private App mApp;

    private RecyclerView mRecyclerView;
    private ReaderReadAdapter mReaderReadAdapter;
    private View mView;
    private Toolbar mToolbar;
    private RelativeLayout mAnimLayout;
    private TextView mRssiTv;
    private ProgressBar mRssiProgressBar;
    private TextView mOnceCntTv, mOnceNumTv, mOnceTimeTv, mTotalCntTv, mTotalNumTv, mTotalTimeTv, mAverageSpeedTv;
    private TextView mOnceCntTitleTv;
    private Button mBeginReadSingleBtn, mStopReadSingleBtn;

    private HandlerThread mReadHandlerThread = new HandlerThread("ReadHandler");
    private Handler mReadHandler;
    private Handler mUiHandler;

    /*
     * 这两个变量的意义在于：
     * 例如stopReadBtn点了以后，这时候会stopBeeper(Cmd Thread里面),但是存在这之后再update了一次ui，
     * 可能在最后的时间节点，又set了一次Beeper(即使是再post到Cmd Thread也会多执行一次)，导致实际盘存停止了，beeper还在响
     */
    private boolean mIsReading = false;//用于控制是否进行盘存
    private boolean mIsReading_fast = false;

    public List<Map<String, ?>> mOldList = Collections.synchronizedList(new ArrayList<Map<String, ?>>());

    private long mReadBeginTime = 0;
    private int mTotalNum; //总标签次数
    private int mTotalNum_previous = 0;
    private int mFastScanUpdateUICnts = 0;
    private long mTimeRecordPerAboutSecond = 0;
    private NormalModeDashBoard mNormalModeInfo = new NormalModeDashBoard();

    private static final int MSG_UPDATE_UI_NORMAL_MODE = 0;
    private static final int MSG_UPDATE_UI_FAST_MODE = 1;
    private static final int FAST_MODE_UPDATE_UI_GAP = 200;

    private Map<String, TagInfo> mTagMap = Collections.synchronizedMap(new LinkedHashMap<String, TagInfo>());
    private TagReadOption mTagReadOption = new TagReadOption();

    private class NormalModeDashBoard {
        private int onceCount; //单次盘存标签个数
        private int onceNum; //单次盘存标签次数
        private int totalCount; //总标签个数
        private long onceTime; //单次盘存用时
        private long totalTime; //总用时，单位为ms
        private int numPerSecond; //每秒多少次,平均值算出来的
    }

    public ReaderInventoryFragment() {
        // Required empty public constructor
    }

    public static ReaderInventoryFragment newInstance() {
        return new ReaderInventoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_reader_inventory, container, false);
        initToolBar();
        init(mView);
        return mView;
    }

    private void initToolBar() {
        mToolbar = mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.inventory_tag_text));
        mToolbar.setNavigationIcon(R.drawable.position_left);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }


    private void init(View parent) {
        mApp = App.getInstance();

        initHandler();
        mOnceCntTitleTv = parent.findViewById(R.id.once_cnt_title);
        mOnceCntTv = parent.findViewById(R.id.once_cnt_result);
        mOnceNumTv = parent.findViewById(R.id.once_num_result);
        mOnceTimeTv = parent.findViewById(R.id.once_time_result);
        mTotalCntTv = parent.findViewById(R.id.total_cnt_result);
        mTotalNumTv = parent.findViewById(R.id.total_num_result);
        mTotalTimeTv = parent.findViewById(R.id.total_time_result);
        mAverageSpeedTv = parent.findViewById(R.id.average_speed_result);
        mBeginReadSingleBtn = parent.findViewById(R.id.begin_read_single);
        mBeginReadSingleBtn.setOnClickListener(this);
        mStopReadSingleBtn = parent.findViewById(R.id.stop_read_single);
        mStopReadSingleBtn.setOnClickListener(this);
        mRssiProgressBar = parent.findViewById(R.id.finding_good_progressbar);
        mAnimLayout = parent.findViewById(R.id.frame_anim_viewgroup);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        ViewGroup.LayoutParams layoutParams = mAnimLayout.getLayoutParams();
        layoutParams.width = screenWidth;

        mRssiTv = parent.findViewById(R.id.tv_rssi);

        mRecyclerView = parent.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mReaderReadAdapter = new ReaderReadAdapter(mApp, mApp.ListMs);
        mReaderReadAdapter.updateShowItems();
        mRecyclerView.setAdapter(mReaderReadAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(false);
    }

    private void addListTitle() {
        if (mApp.ListMs.size() == 0) {
            Map<String, String> h = new HashMap<>();
            String[] Coname = mApp.Coname;
            for (int i = 0; i < Coname.length; i++) {
                h.put(Coname[i], mApp.tagListTitles[i]);
            }
            mApp.ListMs.add(h);
        }
    }

    private class MyDiffCallback extends DiffUtil.Callback {
        private List<Map<String, ?>> current;
        private List<Map<String, ?>> next;
        Map<String, ?> currentItem;
        Map<String, ?> nextItem;

        public MyDiffCallback(List<Map<String, ?>> current, List<Map<String, ?>> next) {
            this.current = current;
            this.next = next;
        }

        @Override
        public int getOldListSize() {
            return current.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            LogUtils.e("oldItemPosition:" + oldItemPosition + "     newItemPosition:" + newItemPosition);
            currentItem = current.get(oldItemPosition);
            nextItem = next.get(newItemPosition);
            return currentItem.get(mApp.Coname[1]).equals(nextItem.get(mApp.Coname[1]));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            currentItem = current.get(oldItemPosition);
            nextItem = next.get(newItemPosition);
            LogUtils.e("currentItem.get(mApp.Coname[2]):" + currentItem.get(mApp.Coname[2]));
            LogUtils.e("nextItem.get(mApp.Coname[2]):" + nextItem.get(mApp.Coname[2]));
            return currentItem.get(mApp.Coname[2]).equals(nextItem.get(mApp.Coname[2])) &&
                    currentItem.get(mApp.Coname[5]).equals(nextItem.get(mApp.Coname[5]));
        }
    }

    private void updateRecyclerView() {
        if (mOldList != null) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(mOldList, mApp.ListMs), true);
            diffResult.dispatchUpdatesTo(mReaderReadAdapter);
        }
        mOldList.clear();
        for (int i = 0; i < mApp.ListMs.size(); i++) {
            Map<String, ?> m = (Map<String, ?>) ((HashMap<String, String>) mApp.ListMs.get(i)).clone();
            mOldList.add(m);
        }
    }

    private void updateNormalModeDashBoard() {
        NormalModeDashBoard info = mNormalModeInfo;
        mOnceCntTv.setText(info.onceCount + "");
        mOnceNumTv.setText(info.onceNum + "");
        mOnceTimeTv.setText(info.onceTime + "ms");

        if (info.totalTime > 500) {
            mTotalCntTv.setText(info.totalCount + "");
            mTotalNumTv.setText(mTotalNum + "");
            mTotalTimeTv.setText(info.totalTime + "ms");
            mAverageSpeedTv.setText(info.numPerSecond + "pcs/s");

            if (mIsReading && isSoundOn() && info.onceNum > 0) {
                mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
            }
        }
    }

    private void updateFastModeDashBoard() {
        long cur = System.currentTimeMillis();
        int onceNum = mTotalNum - mTotalNum_previous; //单次盘存标签次数
        long onceTime = 0; //单次盘存用时
        long totalTime = cur - mReadBeginTime; //总用时，单位为ms
        int totalCount = mTagMap.size(); // 总标签个数
        int numPerSecond_avg = (int) (mTotalNum * 1000 / totalTime); //每秒多少次，总时间平均
        int numPerSecond = 0; //快速模式下实时计算的每秒多少次

        if (mReadBeginTime > 0) {
            if (totalTime > 500) {
                mTotalCntTv.setText(totalCount + "");
                mTotalNumTv.setText(mTotalNum + "");
                mTotalTimeTv.setText(totalTime + "ms");
                mAverageSpeedTv.setText(numPerSecond_avg + "pcs/s");
            }

            //实时的数据统计区间要搞大一点，暂定1s，如果是200ms，波动很大
            if (mFastScanUpdateUICnts % 5 == 0) {
                mTotalNum_previous = mTotalNum;

                if (mTimeRecordPerAboutSecond > 0) {
                    onceTime = cur - mTimeRecordPerAboutSecond;
                    numPerSecond = (int) MathHelper.div(onceNum * 1000, onceTime, 5);

                    if (mFastScanUpdateUICnts != 0) {
                        mOnceCntTv.setText(numPerSecond + "");
                        mOnceNumTv.setText(onceNum + "");
                        mOnceTimeTv.setText(onceTime + "ms");
                    }

                    //update Handler Beeper
                    if (onceNum > 0) {
                        setHandlerBeeper(true, (byte) (onceNum / 20 + 1), (byte) 0x05);

                        if (!mIsReading_fast) {
                            stopHandleBeeper();
                        }
                    } else {
                        stopHandleBeeper();
                    }
                }

                mTimeRecordPerAboutSecond = cur;
            }

            //可以控制下响的频率
            if (mFastScanUpdateUICnts % 2 == 0 && isSoundOn() && onceNum > 0) {
                mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
            }

            mFastScanUpdateUICnts++;
        }
    }

    private void updateFrameAnimation() {
        if (mApp.mIsFindingGood && mApp.ListMs.size() == 2) {
            mRssiTv.setText("Rssi:" + mApp.ListMs.get(1).get(mApp.Coname[5]));

            int progress = calcProgress();
            LogUtils.e("progress:" + progress);
            if (progress >= 100) {
                mRssiProgressBar.setProgress(100);
            } else {
                mRssiProgressBar.setProgress(progress);
            }
        }
    }

    private int calcProgress() {
        //rssi 一般在 -30 到 -70之间波动
        int progress = 0;
        int rssi = Integer.valueOf(mApp.ListMs.get(1).get(mApp.Coname[5]).toString());
        if (rssi < 0 && rssi > -70) {
            double mulFactor = MathHelper.div(100, 40, 5);
            progress = (int) ((rssi + 70) * mulFactor);
        } else {
            progress = 0;
        }
        return progress;
    }

    private void initHandler() {
        mUiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LogUtils.e("mUiHandler  handleMessage msg.what:" + msg.what);

                switch (msg.what) {
                    //普通模式统计信息
                    case MSG_UPDATE_UI_NORMAL_MODE:
                        updateRecyclerView();
                        updateNormalModeDashBoard();
                        updateFrameAnimation();
                        break;
                    //高速模式统计信息
                    case MSG_UPDATE_UI_FAST_MODE:
                        if (isFastMode()) {
                            LogUtils.e("potter", "notifyDataSetChanged begin");
                            updateRecyclerView();
                            LogUtils.e("potter", "notifyDataSetChanged end");
                            updateFastModeDashBoard();
                            updateFrameAnimation();
                        }

                        if (mIsReading_fast) {
                            Message message = Message.obtain();
                            message.what = MSG_UPDATE_UI_FAST_MODE;
                            sendMessageDelayed(message, FAST_MODE_UPDATE_UI_GAP);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        Thread syncReadThread = new Thread(mSyncReadRunnable);
        syncReadThread.start();

        mReadHandlerThread.start();
        mReadHandler = new Handler(mReadHandlerThread.getLooper());
    }

    private SyncReadRunnable mSyncReadRunnable = new SyncReadRunnable();

    private class SyncReadRunnable implements Runnable {
        private boolean mRun = true;

        void release() {
            mRun = false;
        }

        @Override
        public void run() {
            while (mRun) {
                if (mIsReading && mApp.isRFIDReady()) {
                    NormalModeDashBoard info = mNormalModeInfo;
                    long start = System.currentTimeMillis();
                    String additionDataType = SpUtil.getString(Const.SP_KEY_ADDITION_TAG_DATA_TYPE, "None");
                    Log.d(TAG, "additionDataType=" + additionDataType);
                    int timeout = Integer.valueOf(SpUtil.getString(Const.SP_KEY_SINGLE_READ_DURATION, Const.DEF_SINGLE_INVENTORY_DURATION));
                    // TagReadData[] trds = getReader().syncRead(timeout);
                    TagReadData[] trds = getReader().syncRead(TagAdditionData.get(additionDataType), timeout);
                    long cur = System.currentTimeMillis();
                    info.onceTime = cur - start;
                    info.totalTime = cur - mReadBeginTime;

                    //耗时操作在前面，多加一次判断为了避免mIsReading标志位设置的时候还在走上面的代码，多算了一次数据
                    if (mIsReading && mApp.isRFIDReady()) {
                        int onceNums = 0;

                        for (TagReadData trd : trds) {
                            onceNums += trd.getReadCount();
                        }

                        mTotalNum += onceNums;
                        info.onceNum = onceNums;
                        info.numPerSecond = (int) (mTotalNum * 1000 / info.totalTime);
                        info.onceCount = trds.length;

                        Message msg = Message.obtain();
                        msg.what = MSG_UPDATE_UI_NORMAL_MODE;
                        mUiHandler.removeMessages(MSG_UPDATE_UI_NORMAL_MODE);
                        mUiHandler.sendMessage(msg);
                        updateList(trds);

                        info.totalCount = mTagMap.size();

                        //update Handler Beeper
                        if (onceNums > 0) {
                            setHandlerBeeper(true, (byte) (onceNums / 20 + 1), (byte) 0x05);

                            if (!mIsReading) {
                                stopHandleBeeper();
                            }
                        } else {
                            stopHandleBeeper();
                        }

                        try {
                            Thread.sleep(Integer.valueOf(SpUtil.getString(Const.SP_KEY_SINGLE_READ_VACANCY, Const.DEF_SINGLE_INVENTORY_VACANCY)));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        continue;
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateList(TagReadData[] trds) {
        /*
        逻辑如下：
        有新标签：List要更新，一条数据是一个Map
        没有新标签：旧的标签的读取出来的次数，RSSI和频率需要更新
         */
        for (TagReadData trd : trds) {
            updateList(trd);
        }
    }

    private class TagInfo {
        TagReadData d;
        int readCount;

        TagInfo(TagReadData trd) {
            d = trd;
        }
    }

    private SimpleDateFormat mTagTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private void updateList(TagReadData trd) {
        String[] keys = mApp.Coname;
        int aDataLen = trd.getAdditionData() != null ? trd.getAdditionData().length : 0;
        String epc = trd.getEpcHexStr();

        if (!mTagMap.containsKey(epc)) {
            TagInfo ati = new TagInfo(trd);

            if (mApp.mIsFindingGood) {
                if (epc.equals(mApp.mSelectedEpc)) {
                    mTagMap.put(epc, ati);
                }
            } else {
                mTagMap.put(epc, ati);
            }

            // list
            Map<String, String> m = new HashMap<>();
            m.put("ecp_org", epc);
            m.put(keys[0], String.valueOf(mTagMap.size()));
            m.put(keys[1], epc.length() < 24 ? String.format("%-24s", epc) : epc);
            m.put(keys[2], String.valueOf(trd.getReadCount()));
            m.put(keys[3], String.valueOf(trd.getAntenna()));
            m.put(keys[4], "gen2");
            m.put(keys[5], String.valueOf(trd.getRssi()));
            m.put(keys[6], String.valueOf(trd.getFrequency()));
            m.put(keys[8], mTagTimeFormat.format(trd.getTime()));

            if (aDataLen > 0) {
                m.put(keys[7], StrUtil.toHexString(trd.getAdditionData(), 0, aDataLen));
            } else {
                m.put(keys[7], "                 ");
            }

            if (mApp.mIsFindingGood) {
                if (mApp.mSelectedEpc.equals(m.get(keys[1]))) {
                    mApp.ListMs.add(m);
                }
            } else {
                mApp.ListMs.add(m);
            }
        } else {
            TagInfo atf = mTagMap.get(trd.getEpcHexStr());
            atf.readCount += trd.getReadCount();

            for (int k = 0; k < mApp.ListMs.size(); k++) {
                @SuppressWarnings("unchecked")
                Map<String, String> m = (Map<String, String>) mApp.ListMs.get(k);

                if (epc.equals(m.get("ecp_org"))) {
                    m.put(keys[2], String.valueOf(atf.readCount));
                    m.put(keys[5], String.valueOf(trd.getRssi()));
                    m.put(keys[6], String.valueOf(trd.getFrequency()));

                    if (aDataLen > 0) {
                        m.put(keys[7], StrUtil.toHexString(trd.getAdditionData(), 0, aDataLen));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.begin_read_single:
                beginRead();
                break;
            case R.id.stop_read_single:
                stopRead();
                break;
            default:
                break;
        }
    }

    private OnTagReadListener dataListener = new OnTagReadListener() {
        @Override
        public void onTagRead(final TagReadData[] t) {
            mTotalNum += t.length;
            updateList(t);
        }
    };

    private void setQuickModeParams() {
        mTagReadOption.setReadCount(SpUtil.getBoolean(Const.SP_KEY_ITEM_COUNT, false));
        mTagReadOption.setRssi(SpUtil.getBoolean(Const.SP_KEY_ITEM_RSSI, false));
        mTagReadOption.setAntennaId(SpUtil.getBoolean(Const.SP_KEY_ITEM_ANT, false));
        mTagReadOption.setFrequency(SpUtil.getBoolean(Const.SP_KEY_ITEM_FREQ, false));
        mTagReadOption.setTimestamp(SpUtil.getBoolean(Const.SP_KEY_ITEM_TIME, false));
        mTagReadOption.setProtocol(SpUtil.getBoolean(Const.SP_KEY_ITEM_PRO, false));
        mTagReadOption.setData(SpUtil.getBoolean(Const.SP_KEY_ITEM_DATA, false));
        mTagReadOption.setStopPercent(Integer.valueOf(SpUtil.getString(Const.SP_KEY_PAUSE_PERCENTAGE, "0")));
    }

    private void beginRead() {
        if (mApp.isBatteryTemperatureTooHigh()) {
            BaseActivity act = (BaseActivity) getActivity();

            if (act != null) {
                act.showToast(R.string.toast_temp_too_high);
            }

            return;
        }

        try {
            if (mApp.checkIsRFIDReady()) {
                beginReadUI();
                clearDataAndResetParams();
                mReadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        beginReadInternal();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void beginReadUI() {
        mBeginReadSingleBtn.setVisibility(View.GONE);
        mStopReadSingleBtn.setVisibility(View.VISIBLE);

        if (!isFastMode()) {
            mOnceCntTitleTv.setText(R.string.info_once_cnt);
        } else {
            mOnceCntTitleTv.setText(R.string.info_real_speed);
        }

        mOnceCntTv.setText("0");
        mOnceNumTv.setText("0");
        mOnceTimeTv.setText("0ms");
        mTotalCntTv.setText("0");
        mTotalNumTv.setText("0");
        mTotalTimeTv.setText("0ms");
        mAverageSpeedTv.setText("0pcs/s");

        if (mApp.mIsFindingGood) {
            mAnimLayout.setVisibility(View.VISIBLE);
        }
    }

    private void beginReadInternal() {
        mApp.rfidMgr.setLEDBlink(true);

        if (!isFastMode()) {
            mIsReading = true;
        } else {
            mIsReading_fast = true;

            setQuickModeParams();

            String additionDataType = SpUtil.getString(Const.SP_KEY_ADDITION_TAG_DATA_TYPE, "None");
            RfidReader reader = getReader();
            reader.setOnTagReadListener(dataListener);
            reader.read(TagAdditionData.get(additionDataType), mTagReadOption);

            //每200ms发一次，用于更新高速模式的ui
            Message msg = Message.obtain();
            msg.what = MSG_UPDATE_UI_FAST_MODE;
            mUiHandler.sendMessage(msg);
        }
    }

    private void setHandlerBeeper(boolean switchByte, byte fre, byte strength) {
        if (mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_RFID_SCAN_SOUND, false)) {
            mApp.rfidMgr.setBeeper(switchByte, fre, strength);
        }
    }

    private void stopHandleBeeper() {
        if (mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_RFID_SCAN_SOUND, false)) {
            mApp.rfidMgr.setBeeper(false, 0x05, 0x05);
        }
    }

    private void stopReadOnModeChange() {
        Log.i(TAG, "stopReadOnModeChange()");

        try {
            stopReadUI();
            if (mApp.isReady()) {
                mReadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopReadInternal();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRead() {
        try {
            stopReadUI();
            if (mApp.isRFIDReady()) {
                mReadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopReadInternal();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopReadUI() {
        mBeginReadSingleBtn.setVisibility(View.VISIBLE);
        mStopReadSingleBtn.setVisibility(View.GONE);
        mAnimLayout.setVisibility(View.GONE);
    }

    private void stopReadInternal() {
        Log.i(TAG, "stopReadInternal()");

        if (!isFastMode()) {
            mIsReading = false;
        } else {
            mIsReading_fast = false;
            RfidReader reader = getReader();
            reader.stopRead();
            reader.removeOnTagReadListener(dataListener);
            /*用removeMessages来终止循环其实有risk，如果正好跑在这个msg里面，remove执行完结束后，又postDelay一个msg出来，循环就没有停止
            其实是因为removeMessages和handleMessage没在一个线程，要是在同一个线程，保证了先后顺序，其实就可以保证循环能停下来了。
            这里我们后面改成用mIsReading_fast来控制是否sendMessageDelay
             */
            //mUiHandler.removeMessages(MSG_UPDATE_UI_FAST_MODE);
        }
        mApp.rfidMgr.setLEDBlink(false);
        stopHandleBeeper();
    }

    private void clearDataAndResetParams() {
        mTagMap.clear();
        mApp.ListMs.clear();
        mReadBeginTime = System.currentTimeMillis();
        mTotalNum = 0;
        mTotalNum_previous = 0;
        mFastScanUpdateUICnts = 0;
        mTimeRecordPerAboutSecond = 0;
        addListTitle();
        updateRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsResumed = true;
        mApp.getRfidManager().addEventListener(mEventListner);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        mSoundId = mSoundPool.load(getActivity(), R.raw.beep, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.i(TAG, "onLoadComplete    soundPool=" + soundPool + "id=" + sampleId + "    status=" + status);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsResumed = false;
        mApp.getRfidManager().addEventListener(mEventListner);

        mSoundPool.release();
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.e(" InventoryFrag onStop");
        stopRead();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mIsShow = !hidden;

        if (hidden) {
            stopRead();
        } else {
            /*
             * 这里用notifyDataSetChanged保证去Setting里面修改完return item后每条信息能刷新到，尤其是标题栏
             */
            //updateRecyclerView();
            mReaderReadAdapter.updateShowItems();
            mReaderReadAdapter.notifyDataSetChanged();
        }
    }

    //定义前台状态为resumed且没有hide
    private boolean isForeGround() {
        return mIsResumed && mIsShow;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopRead();
        mListener = null;
        mReadHandlerThread.quit();
        mSyncReadRunnable.release();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Listen RFID device event.
     */
    private EventListener mEventListner = new EventListener() {
        @Override
        public void onDeviceConnected(Object data) {
            Log.i(TAG, "onDeviceConnected: " + data);
        }

        @Override
        public void onDeviceDisconnected(Object data) {
            Log.i(TAG, "onDeviceDisconnected: " + data);
            mIsReading = false;
            mIsReading_fast = false;
            stopReadUI();
        }

        @Override
        public void onReaderCreated(boolean success, RfidReader reader) {
        }

        @Override
        public void onRfidTriggered(boolean trigger) {
            if (isForeGround()) {
                Log.i(TAG, "onRfidTriggered(" + trigger + ")");

                if (trigger) {
                    /*
                     * 防止PDA上已经开始Inventory了，用户又在手柄上按了下trigger
                     * Fix QINGCS-21
                     * Press reader button after click start in fast mode, the speed isn't normal
                     */
                    if (mIsReading_fast || mIsReading) {
                        stopRead();
                    }
                    beginRead();
                } else {
                    stopRead();
                }
            } else {
                Log.i(TAG, "onRfidTriggered(" + trigger + "), not in the foreground.");
            }
        }

        @Override
        public void onTriggerModeSwitched(TriggerMode currentMode) {
            Log.i(TAG, "onTriggerModeSwitched(" + currentMode + ")");
            if (currentMode == TriggerMode.BARCODE_SCAN) {
                /*
                 * 因为从RFID mode切换到Scanner mode的时候，current mode已经变更为scanner mode了，
                 * 所以stopRead()方法是不通用的，所以要修改下写法
                 */
                stopReadOnModeChange();
            }
        }
    };

    private RfidReader getReader() {
        return mApp.rfidReader;
    }

    private boolean isFastMode() {
        return SpUtil.getString(Const.SP_KEY_SCAN_MODE, Const.SCAN_MODE_NORMAL).equals(Const.SCAN_MODE_FAST);
    }

    private boolean isSoundOn() {
        return SpUtil.getBoolean(Const.SP_KEY_PDA_SCAN_SOUND, Const.DEF_SCAN_SOUND_STATE);
    }
}
