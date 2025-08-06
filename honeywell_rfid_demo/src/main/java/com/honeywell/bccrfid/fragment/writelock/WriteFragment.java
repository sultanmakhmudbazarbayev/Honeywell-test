package com.honeywell.bccrfid.fragment.writelock;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.fragment.BaseFragment;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WriteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteFragment extends BaseFragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private App mApp;
    private View mView;
    private TextInputEditText mSelectedEpcEdtv;
    private AppCompatSpinner mMemoryBankSpinner;
    private TextInputEditText mStartAddrEdtv;
    private TextInputEditText mBlockCntEdtv;
    private TextInputEditText mDataEdtv;
    private TextInputEditText mPwdEdtv;
    private Button mReadBtn;
    private Button mWriteBtn;
    private TextInputLayout mDataLayout;
    private TextView mIntroTv;
    private AppCompatCheckBox mPasswordChkBox;

    private Handler mMainHandler;
    private ProgressDialog mProgressDialog;
    private HandlerThread mCMDThread = new HandlerThread("cmd");
    private Handler mCmdHandler;

    private static final int RESERVED_MAX_BLOCK = 4;
    private static final int EPC_MAX_BLOCK = 8;
    private static final int TID_MAX_BLOCK = 12;
    private static final int USER_MAX_BLOCK = 32;

    private static final int RESERVED_DEF_START_ADDR = 2;
    private static final int EPC_DEF_START_ADDR = 2;
    private static final int TID_DEF_START_ADDR = 0;
    private static final int USER_DEF_START_ADDR = 0;

    private static final int CMD_BEGIN = 0;
    private static final int CMD_END = 1;
    private static final int UPDATE_UI = 2;

    private static final int CMD_READ = 0;
    private static final int CMD_WRITE = 1;
    private static final int CMD_LOCK = 2;
    private static final int CMD_KILL = 3;
    private static final String DEFAULT_ACCESS_PWD = "00000000";


    public WriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WriteFragment newInstance(String param1, String param2) {
        WriteFragment fragment = new WriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_write, container, false);
        init();
        return mView;
    }

    private void init() {
        mApp = (App) getActivity().getApplication();
        initView();
        initHandler();
    }

    private void initHandler() {
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_BEGIN:
                        mProgressDialog.show();
                        break;
                    case CMD_END:
                        mProgressDialog.dismiss();
                        break;
                    case UPDATE_UI:
                        mDataEdtv.setText(msg.getData().getString("read_data"));
                        break;
                }
            }
        };
        mCMDThread.start();
        mCmdHandler = new Handler(mCMDThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_READ:
                        mMainHandler.sendEmptyMessage(CMD_BEGIN);
                        doRead();
                        mMainHandler.sendEmptyMessage(CMD_END);
                        break;
                    case CMD_WRITE:
                        mMainHandler.sendEmptyMessage(CMD_BEGIN);
                        doWrite();
                        mMainHandler.sendEmptyMessage(CMD_END);
                        break;
                }
            }
        };
    }

    private void initView() {
        mSelectedEpcEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_tag_epc);
        mMemoryBankSpinner = (AppCompatSpinner) mView.findViewById(R.id.spinner_bank);
        mStartAddrEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_start_addr);
        mBlockCntEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_block_cnt);
        mDataEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_data);
        mPwdEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_pwd);
        mDataLayout = (TextInputLayout) mView.findViewById(R.id.data_layout);
        mReadBtn = (Button) mView.findViewById(R.id.btn_read);
        mWriteBtn = (Button) mView.findViewById(R.id.btn_write);
        mIntroTv = (TextView) mView.findViewById(R.id.introduction_tv);
        mPasswordChkBox = (AppCompatCheckBox) mView.findViewById(R.id.chkbox_pwd);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.loading_text));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(getString(R.string.cmd_executing));

        updateSelectedEpc();
        mIntroTv.setText(String.format(getString(R.string.introduction_text), RESERVED_MAX_BLOCK, EPC_MAX_BLOCK, TID_MAX_BLOCK, USER_MAX_BLOCK));
        //mPwdEdtv.setText(DEFAULT_ACCESS_PWD);
        mPwdEdtv.setEnabled(mPasswordChkBox.isChecked());
        mPasswordChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPwdEdtv.setEnabled(mPasswordChkBox.isChecked());
                if (isChecked && mPwdEdtv.getText().length() == 0) {
                    mPwdEdtv.setText(DEFAULT_ACCESS_PWD);
                }
            }
        });

        mStartAddrEdtv.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String beforeStr = s.toString();
                String afterStr = s.toString();
                if (beforeStr.length() > 0) {

                    afterStr = String.valueOf(Integer.parseInt(beforeStr));

                    if (Integer.parseInt(afterStr) > getMaxStartAddr()) {
                        afterStr = String.valueOf(getMaxStartAddr());
                    }
                }

                if (!beforeStr.equals(afterStr)) {
                    mStartAddrEdtv.setText(afterStr);
                }

                String countStr = mBlockCntEdtv.getText().toString();
                if (mBlockCntEdtv.getText().toString().length() > 0) {
                    if (Integer.parseInt(countStr) > getMaxBlockBank()) {
                        mBlockCntEdtv.setText(String.valueOf(getMaxBlockBank()));
                    }
                }

                if (mBlockCntEdtv.getText().toString().length() > 0) {
                    mDataLayout.setCounterMaxLength(Integer.parseInt(mBlockCntEdtv.getText().toString()) * 4);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBlockCntEdtv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String beforeStr = s.toString();
                String afterStr = s.toString();
                if (beforeStr.length() > 0) {

                    afterStr = String.valueOf(Integer.parseInt(beforeStr));

                    if (Integer.parseInt(afterStr) > getMaxBlockBank()) {
                        afterStr = String.valueOf(getMaxBlockBank());
                    }
                }

                if (!beforeStr.equals(afterStr)) {
                    mBlockCntEdtv.setText(afterStr);
                }

                if (mBlockCntEdtv.getText().toString().length() > 0) {
                    mDataLayout.setCounterMaxLength(Integer.parseInt(mBlockCntEdtv.getText().toString()) * 4);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mMemoryBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0:
                        mStartAddrEdtv.setText(String.valueOf(RESERVED_DEF_START_ADDR));
                        mBlockCntEdtv.setText(String.valueOf(RESERVED_MAX_BLOCK - RESERVED_DEF_START_ADDR));
                        break;

                    case 1:
                        mStartAddrEdtv.setText(String.valueOf(EPC_DEF_START_ADDR));
                        mBlockCntEdtv.setText(String.valueOf(EPC_MAX_BLOCK - EPC_DEF_START_ADDR));
                        break;

                    case 2:
                        mStartAddrEdtv.setText(String.valueOf(TID_DEF_START_ADDR));
                        mBlockCntEdtv.setText(String.valueOf(TID_MAX_BLOCK - TID_DEF_START_ADDR));
                        break;

                    case 3:
                        mStartAddrEdtv.setText(String.valueOf(USER_DEF_START_ADDR));
                        mBlockCntEdtv.setText(String.valueOf(USER_MAX_BLOCK - USER_DEF_START_ADDR));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mReadBtn.setOnClickListener(this);
        mWriteBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_read:
                mCmdHandler.sendEmptyMessage(CMD_READ);
                break;
            case R.id.btn_write:
                mCmdHandler.sendEmptyMessage(CMD_WRITE);
                break;
            default:
                break;
        }
    }

    private void doRead() {
        if (mApp.checkIsRFIDReady()) {
            if (checkIsDataLegal()) {
                int position = mMemoryBankSpinner.getSelectedItemPosition();
                String epc = mSelectedEpcEdtv.getText().toString();

                try {
                    int startAddr = Integer.valueOf(mStartAddrEdtv.getText().toString());
                    int blockCnt = Integer.valueOf(mBlockCntEdtv.getText().toString());
                    String pwd = mPasswordChkBox.isChecked() ? mPwdEdtv.getText().toString() : null;
                    String epddata = mApp.rfidReader.readTagData(epc, position, startAddr, blockCnt, pwd);

                    Message msg = Message.obtain();
                    msg.what = UPDATE_UI;
                    Bundle bundle = new Bundle();
                    if (epddata != null) {
                        bundle.putString("read_data", epddata);
                    } else {
                        bundle.putString("read_data", "");
                    }
                    msg.setData(bundle);
                    mMainHandler.sendMessage(msg);
                } catch (RfidReaderException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.read_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.read_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void doWrite() {
        if (mApp.checkIsRFIDReady()) {
            if (checkIsDataLegal()) {
                int position = mMemoryBankSpinner.getSelectedItemPosition();
                String epc = mSelectedEpcEdtv.getText().toString();
                String data = mDataEdtv.getText().toString();
                if (data.length() != mDataLayout.getCounterMaxLength()) {
                    Toast.makeText(getActivity(), getString(R.string.data_length_not_adatped), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int startAddr = Integer.valueOf(mStartAddrEdtv.getText().toString());
                    String pwd = mPasswordChkBox.isChecked() ? mPwdEdtv.getText().toString() : null;
                    mApp.rfidReader.writeTagData(epc, position, startAddr, pwd, data);
                    Toast.makeText(getActivity(), getString(R.string.write_successfully), Toast.LENGTH_SHORT).show();
                } catch (RfidReaderException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.write_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.write_failed) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void updateSelectedEpc() {
        if (mSelectedEpcEdtv != null) {
            mSelectedEpcEdtv.setText(mApp.mSelectedEpc);
        }
    }

    public void updatePasswordLayout(boolean isCheck, String pwd) {
        if (mPasswordChkBox != null) {
            mPasswordChkBox.setChecked(isCheck);
        }
        if (mPwdEdtv != null) {
            mPwdEdtv.setText(pwd);
        }
    }

    public String getPwd() {
        String pwd = DEFAULT_ACCESS_PWD;
        if (mPwdEdtv != null) {
            pwd = mPwdEdtv.getText().toString();
        }
        return pwd;
    }

    public boolean getPwdState() {
        boolean pwdState = false;
        if (mPasswordChkBox != null) {
            pwdState = mPasswordChkBox.isChecked();
        }
        return pwdState;
    }

    private boolean checkIsDataLegal() {
        String startAddr = mStartAddrEdtv.getText().toString();
        String blockCnt = mBlockCntEdtv.getText().toString();
        if (startAddr.length() == 0) {
            Toast.makeText(getActivity(), getString(R.string.toast_start_addr_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (blockCnt.length() == 0) {
            Toast.makeText(getActivity(), getString(R.string.toast_block_cnt_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int getMaxStartAddr() {
        int position = mMemoryBankSpinner.getSelectedItemPosition();
        int max = 0;
        switch (position) {
            case 0:
                max = RESERVED_MAX_BLOCK - 1;
                break;
            case 1:
                max = EPC_MAX_BLOCK - 1;
                break;
            case 2:
                max = TID_MAX_BLOCK - 1;
                break;
            case 3:
                max = USER_MAX_BLOCK - 1;
                break;
            default:
                break;
        }
        return max;
    }

    private int getMaxBlockBank() {
        int position = mMemoryBankSpinner.getSelectedItemPosition();
        int max = 0;
        int startAddr = 0;
        if (mStartAddrEdtv.getText().toString().length() > 0) {
            startAddr = Integer.parseInt(mStartAddrEdtv.getText().toString());
        }
        switch (position) {
            case 0:
                max = RESERVED_MAX_BLOCK - startAddr;
                break;
            case 1:
                max = EPC_MAX_BLOCK - startAddr;
                break;
            case 2:
                max = TID_MAX_BLOCK - startAddr;
                break;
            case 3:
                max = USER_MAX_BLOCK - startAddr;
                break;
            default:
                break;
        }
        return max;
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
        mListener = null;
    }

}
