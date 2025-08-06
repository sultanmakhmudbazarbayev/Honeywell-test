package com.honeywell.bccrfid;

import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import com.honeywell.bccrfid.utils.LogUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private String ClsName;
    private boolean showLog = true;
    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClsName = this.getClass().getSimpleName() + ":";
        if (showLog) {
            LogUtils.e(ClsName + "onCreate");
        }
    }
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (showLog) {
            LogUtils.e(ClsName + "onStart");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showLog) {
            LogUtils.e(ClsName + "onResume");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (showLog) {
            LogUtils.e(ClsName + "onStop");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (showLog) {
            LogUtils.e(ClsName + "onDestroy");
        }
    }

    public void showToast(int strId) {
        showToast(strId, false);
    }

    public void showToast(final String s) {
        showToast(s, false);
    }

    public void showToast(int strId, boolean longTime) {
        showToast(getString(strId), longTime);
    }

    public void showToast(final String s, final boolean longTime) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            doShowToast(s, longTime);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    doShowToast(s, longTime);
                }
            });
        }
    }

    private void doShowToast(String s, boolean longTime) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), s,
                    longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
        }

        mToast.show();
    }
}
