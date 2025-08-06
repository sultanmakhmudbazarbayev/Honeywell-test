package com.honeywell.bccrfid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.bccrfid.utils.DensityUtil;
import com.honeywell.bccrfid.utils.LogUtils;
import com.honeywell.rfidservice.ConnectionState;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity implements GridView.OnItemClickListener {
    private List<FunctionItem> mList = new ArrayList<FunctionItem>();
    private GridView mGridView;
    private CustomAdapter mAdapter;
    private App mApp;
    private long mExitTime;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private String[] mPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<String> mDismissPermissionList = new ArrayList<>();
    private AlertDialog mPermissionDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21 && BuildConfig.UI_STYLE.equals(Const.HONEYWELL_DESIGN_UI)) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();
        initToolBar();
        init();
        requestDynamicPermissions();
    }

    private void requestDynamicPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < mPermissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, mPermissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mDismissPermissionList.add(mPermissions[i]);
                }
            }
            if (mDismissPermissionList.size() > 0) {
                ActivityCompat.requestPermissions(this, mPermissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String mPermissions[], int[] grantResults) {
        boolean hasPermissionDismiss = false;
        if (PERMISSION_REQUEST_CODE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            if (hasPermissionDismiss) {
                //showPermissionDialog();
            } else {
            }
        }
    }

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission_title)
                    .setPositiveButton(R.string.request_permission_positive_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            String mPackName = getPackageName();
                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.request_permission_negative_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.getTitle());
        setSupportActionBar(toolbar);
    }

    private void init() {
        mApp = (App) getApplication();
        mGridView = (GridView) findViewById(R.id.functionset_grid);
        mAdapter = new CustomAdapter(this,
                R.layout.function_item, mList, mGridView);

        String[] convertTexts = getResources().getStringArray(
                R.array.function_texts);
        TypedArray typedArray = getResources().obtainTypedArray(
                R.array.function_icons);
        for (int index = 0; index < typedArray.length(); index++) {
            int resId = typedArray.getResourceId(index, 0);
            FunctionItem mFunction = new FunctionItem(convertTexts[index],
                    resId);
            mList.add(mFunction);
        }
        typedArray.recycle();
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(MainActivity.this, ReaderBTControl.class);
                break;
            case 1:
                if (checkBTIsConnected()) {

                }
                intent = new Intent(MainActivity.this, ReaderMain.class);
                intent.putExtra("index", 0);
                break;
            case 2:
                if (checkBTIsConnected()) {

                }
                intent = new Intent(MainActivity.this, ReaderMain.class);
                intent.putExtra("index", 1);
                break;
            case 3:
                if (checkBTIsConnected()) {

                }
                intent = new Intent(MainActivity.this, ReaderMain.class);
                intent.putExtra("index", 2);
                break;
            case 4:
                if (checkBTIsConnected()) {

                }
                intent = new Intent(MainActivity.this, ReaderMain.class);
                intent.putExtra("index", 3);
                break;
            case 5:
                intent = new Intent(MainActivity.this, ReaderAbout.class);
                break;
            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private boolean checkBTIsConnected() {
        if (mApp.rfidMgr.getConnectionState() != ConnectionState.STATE_CONNECTED) {
            Toast.makeText(this, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    class CustomAdapter extends ArrayAdapter<FunctionItem> {
        private int resourceId;
        GridView gridview;

        public CustomAdapter(Context context, int resource,
                             List<FunctionItem> objects, GridView gridView) {
            super(context, resource, objects);
            resourceId = resource;
            gridview = gridView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String functionName = getItem(position).getFunctionName();
            int pictureSrc = getItem(position).getPictureSrc();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View root = inflater.inflate(resourceId, null);
            TextView tv = (TextView) root.findViewById(R.id.function_tv);
            View v1 = (View) root.findViewById(R.id.item_rel);
            ImageView iv = (ImageView) root
                    .findViewById(R.id.function_iv);
            tv.setText(functionName);
            iv.setImageResource(pictureSrc);
            LogUtils.e("potter", "gridviewheight:" + gridview.getHeight());
            if (BuildConfig.UI_STYLE.equals(Const.MATERIAL_DESIGN_UI)) {
                LayoutParams layoutParams = v1.getLayoutParams();
                layoutParams.height = (int) ((gridview.getHeight() - DensityUtil.dip2px(
                        MainActivity.this, 4.0f)) / 3);
            }
            return root;
        }
    }

    class FunctionItem {
        private String functionName;
        private int pictureSrc;

        public FunctionItem(String functionName, int pictureSrc) {
            this.functionName = functionName;
            this.pictureSrc = pictureSrc;
        }

        public String getFunctionName() {
            return functionName;
        }

        public int getPictureSrc() {
            return pictureSrc;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_back_to_exit), Toast.LENGTH_SHORT)
                        .show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
