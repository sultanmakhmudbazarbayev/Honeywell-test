package com.honeywell.bccrfid.fragment.writelock;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.fragment.BaseFragment;
import com.honeywell.rfidservice.rfid.Gen2.LockBank;
import com.honeywell.rfidservice.rfid.Gen2.LockType;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LockFragment extends BaseFragment implements View.OnClickListener {
    private static final int CMD_BEGIN = 0;
    private static final int CMD_END = 1;
    private static final int UPDATE_UI = 2;

    private static final int CMD_READ = 0;
    private static final int CMD_WRITE = 1;
    private static final int CMD_LOCK = 2;
    private static final int CMD_KILL = 3;
    private static final String DEFAULT_ACCESS_PWD = "00000000";
    
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
    private AppCompatSpinner mLockBankSpinner;
    private AppCompatSpinner mLockTypeSpinner;
    private TextInputEditText mPwdEdtv;
    private AppCompatCheckBox mPasswordChkBox;
    private Button mLockBtn;
    private Button mKillBtn;

    private Handler mMainHandler;
    private ProgressDialog mProgressDialog;
    private HandlerThread mCMDThread = new HandlerThread("cmd");
    private Handler mCmdHandler;

    public LockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LockFragment newInstance(String param1, String param2) {
        LockFragment fragment = new LockFragment();
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
        mView = inflater.inflate(R.layout.fragment_lock, container, false);
        init();
        return mView;
    }

    private void init() {
        mApp = (App) getActivity().getApplication();
        initView();
        initHandler();
    }

    private void initView() {
        mSelectedEpcEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_tag_epc);
        mLockBankSpinner = (AppCompatSpinner) mView.findViewById(R.id.spinner_lock_bank);
        mLockTypeSpinner = (AppCompatSpinner) mView.findViewById(R.id.spinner_lock_type);
        mPwdEdtv = (TextInputEditText) mView.findViewById(R.id.edtv_pwd);
        mPasswordChkBox = (AppCompatCheckBox) mView.findViewById(R.id.chkbox_pwd);
        mLockBtn = (Button) mView.findViewById(R.id.btn_lock);
        mKillBtn = (Button) mView.findViewById(R.id.btn_kill);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.loading_text));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(getString(R.string.cmd_executing));

        updateSelectedEpc();
        mLockBtn.setOnClickListener(this);
        mKillBtn.setOnClickListener(this);
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
                        break;
                }
            }
        };
        mCMDThread.start();
        mCmdHandler = new Handler(mCMDThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_LOCK:
                        mMainHandler.sendEmptyMessage(CMD_BEGIN);
                        doLock();
                        mMainHandler.sendEmptyMessage(CMD_END);
                        break;
                    case CMD_KILL:
                        mMainHandler.sendEmptyMessage(CMD_BEGIN);
                        doKill();
                        mMainHandler.sendEmptyMessage(CMD_END);
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_lock:
                mCmdHandler.sendEmptyMessage(CMD_LOCK);
                break;
            case R.id.btn_kill:
                mCmdHandler.sendEmptyMessage(CMD_KILL);
                break;
        }
    }

    private void doLock() {
        if (mApp.checkIsRFIDReady()) {
            String epc = mSelectedEpcEdtv.getText().toString();
            LockBank lockBank = LockBank.get(mLockBankSpinner.getSelectedItem().toString());
            LockType lockType = LockType.get(mLockTypeSpinner.getSelectedItem().toString());
            String pwd = mPasswordChkBox.isChecked() ? mPwdEdtv.getText().toString() : DEFAULT_ACCESS_PWD;

            try {
                mApp.rfidReader.lockTag(
                        epc,
                        lockBank,
                        lockType,
                        pwd);
                Toast.makeText(getActivity(), getString(R.string.lock_successfully),
                        Toast.LENGTH_SHORT).show();
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.lock_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doKill() {
        if (mApp.checkIsRFIDReady()) {
            String epc = mSelectedEpcEdtv.getText().toString();
            String pwd = mPwdEdtv.getText().toString();

            try {
                mApp.rfidReader.killTag(epc, pwd);
                Toast.makeText(getActivity(), getString(R.string.kill_successfully),
                        Toast.LENGTH_SHORT).show();
            } catch (RfidReaderException e) {
                Toast.makeText(getActivity(), getString(R.string.kill_failed) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
