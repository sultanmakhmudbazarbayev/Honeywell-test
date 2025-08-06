package com.honeywell.bccrfid.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.bccrfid.BaseActivity;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.utils.Log;
import com.honeywell.rfidservice.ConnectionState;
import com.honeywell.rfidservice.RfidFirmwareUpdateListener;
import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.utils.FileUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class RfidFwUpdateActivity extends BaseActivity {
    private static final String TAG = "RfidFwUpdateActivity";
    private static final int READ_REQUEST_CODE = 56983;

    private TextView mTvFilePath;
    private TextView mTvStatus;
    private ProgressBar mProgressBar;
    private Button mBtnUpdate;
    private String mFilePath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rfid_fw_update_activity);
        initToolBar();

        mTvFilePath = findViewById(R.id.tv_file_path);
        mTvStatus = findViewById(R.id.tv_status);
        mProgressBar = findViewById(R.id.progess_bar);
        mBtnUpdate = findViewById(R.id.update_button);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getTitle());
        //toolbar.setNavigationIcon(R.drawable.position_left);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBtnUpdate.isEnabled()) {
            RfidManager.getInstance(this).disconnect();
            super.onBackPressed();
        } else {

        }
    }

    public void clickBtnSelectFile(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void clickBtnUpdate(View v) {
        if (mFilePath == null) {
            return;
        }

        RfidManager rfidMgr = RfidManager.getInstance(this);

        if (!rfidMgr.otaFileChecksum(mFilePath)) {
            Toast.makeText(this, R.string.toast_illegal, Toast.LENGTH_LONG).show();
            return;
        }

        if (rfidMgr.getConnectionState() != ConnectionState.STATE_CONNECTED) {
            Toast.makeText(this, getString(R.string.toast_error1), Toast.LENGTH_SHORT).show();
            return;
        }

        setButton(false);
        mTvStatus.setText("Updating ...");
        mProgressBar.setProgress(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RfidManager.getInstance(RfidFwUpdateActivity.this).firmwareUpdate(mFilePath, mListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setButton(final boolean enable) {
        if (enable) {
            mBtnUpdate.setText(R.string.update);
            mBtnUpdate.setEnabled(true);
        } else {
            mBtnUpdate.setText(R.string.updating);
            mBtnUpdate.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.getPath());
                String path = FileUtil.getFilePath(this, uri);
                Log.i(TAG, "Selected file: " + path);

                if (path != null) {
                    mFilePath = path;
                    mTvFilePath.setText(path);
                    return;
                }
            }

            mTvFilePath.setText(R.string.failed_to_load_file);
        }
    }

    private RfidFirmwareUpdateListener mListener = new RfidFirmwareUpdateListener() {

        @Override
        public void onProgressChanged(final int progress) {
            Log.i(TAG, "progress = " + progress);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(progress);
                    mTvStatus.setTextColor(Color.BLACK);
                    mTvStatus.setText("Updating " + progress + "%");
                }
            });
        }

        @Override
        public void onUpdateFinished() {
            Log.i(TAG, "onUpdateFinished()");

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            setButton(true);
                            mProgressBar.setProgress(0);
                            mTvStatus.setText("Update finished.");
                            mTvStatus.setTextColor(Color.GREEN);
                        }
                    });
        }

        @Override
        public void onUpdateFailed() {
            Log.i(TAG, "onUpdateFailed()");

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            setButton(true);
                            mProgressBar.setProgress(0);
                            mTvStatus.setText("Failed to update!");
                            mTvStatus.setTextColor(Color.RED);
                        }
                    });
        }
    };
}
