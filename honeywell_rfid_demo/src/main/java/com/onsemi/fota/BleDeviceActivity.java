package com.onsemi.fota;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.bccrfid.R;
import com.honeywell.rfidservice.utils.HashUtil;
import com.onsemi.ble.UpdateControllerListener;
import com.onsemi.protocol.update.FotaController;
import com.onsemi.protocol.update.FotaException;
import com.onsemi.protocol.update.FotaFirmwareFile;
import com.onsemi.protocol.update.FotaOptions;
import com.onsemi.protocol.update.FotaUpdateStep;
import com.onsemi.protocol.utility.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;

import static com.onsemi.fota.FotaState.Idle;
import static com.onsemi.fota.FotaState.Ready;

/**
 * Activity to connect the selected device, select a file and start the update
 */
public class BleDeviceActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 56983;
    private static final String TAG  = "BleDeviceActivity";
    private static final int HASH_LENGTH_I = 40;
    private static final long HASH_LENGTH_L = 40;

    private String imagePath;
    private FotaFirmwareFile firmwareFile;
    private FotaController controller;
    private Timer throughputTimer;
    private int lastProgress;
    private int currentProgress;

    private String getFotaHash = "";
    private String calculateFotaHash = "";
    private boolean isLegal = true;

    private FotaPeripheralListener listener = new FotaPeripheralListener() {
        @Override public void onRssiChanged(int rssi) { }
        @Override public void onDisconnected(boolean fromHost) {}

        @Override
        public void onStateChanged(final FotaState oldState, final FotaState newState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateButtons();
                    updateTextViews();

                    if (mConnecting) {
                        if (newState == Ready) {
                            closeWaitDialog();
                        } else if (newState == Idle) {
                            mWaitDialogTimerHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mConnecting) {
                                        connect();
                                    }
                                }
                            }, 1000);
                        }
                    }
                }
            });
        }
    };

    private UpdateControllerListener updateControllerListener = new UpdateControllerListener() {
        @Override
        public void onProgressChanged(final int progress , final int total, final String step) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(progress == 0) {
                        updateStatusTextView.setText(step);
                    }
                    if(total == 0) {
                        progressBar.setProgress(0);
                    }
                    else {
                        synchronized (throughputTimer) {
                            currentProgress = progress;
                        }
                        progressBar.setProgress((int)Math.round((double)100 / total * progress));
                    }
                }
            });
        }

        @Override
        public void onCompleted(final int status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStatusTextView.setText("Update finished with code: " + status + " (" + statusToString(status) +")");
                }
            });
        }
    };

    TextView stateTextView;
    TextView filePathTextView;
    TextView bootloaderVersionTextView;
    TextView fotaVersionTextView;
    TextView appVersionTextView;

    TextView nameTextView;
    TextView addressTextView;
    TextView fotaVersionImgTextView;
    TextView appVersionImgTextView;

    TextView updateStatusTextView;
    ProgressBar progressBar;

    TextView connectButton;
    TextView selectFileButton;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new FotaController();

        setContentView(R.layout.onsemi_activity_device);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        stateTextView = findViewById(R.id.state_value_text_view);
        nameTextView = findViewById(R.id.name_textview);
        addressTextView = findViewById(R.id.address_textview);

        bootloaderVersionTextView = findViewById(R.id.bootlader_version_textview);
        fotaVersionTextView = findViewById(R.id.fota_version_textview);
        appVersionTextView = findViewById(R.id.app_version_textview);

        fotaVersionImgTextView = findViewById(R.id.fota_img_version_textview);
        appVersionImgTextView = findViewById(R.id.app_img_version_textview);

        filePathTextView = findViewById(R.id.file_value_text_view);
        updateStatusTextView = findViewById(R.id.update_status_text_view);

        progressBar = findViewById(R.id.progess_bar);

        connectButton = findViewById(R.id.connect_button);
        selectFileButton = findViewById(R.id.select_file_button);
        updateButton = findViewById(R.id.update_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        final Activity context = this;
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // it would be "*/*".
                intent.setType("*/*");

                startActivityForResult(intent, READ_REQUEST_CODE);

            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonsEnabled(false);
                if(firmwareFile == null) {
                    updateStatusTextView.setText("No firmware file selected");
                    setButtonsEnabled(true);
                    return;
                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runUpdate();
                    }
                });
                thread.start();
            }
        });

    }

    private void connect() {
        setButtonsEnabled(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();
                    if(selected == null) {
                        return;
                    }
                    if(selected.getFotaState() == Idle) {
                        if (!mConnecting) {
                            mConnecting = true;
                            mWaitDialogTimerHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showWaitDialog();
                                }
                            });
                        }

                        selected.establish();
                    }
                    else {
                        selected.teardown();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateButtons();
        updateTextViews();
    }

    /**
     * Update the button text
     */
    private void updateButtons() {
        FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();
        boolean enabled = false;
        if(selected == null) {
            setButtonsEnabled(false);
            return;
        }

        if(controller.getUpdateStatus() != FotaUpdateStep.Idle && controller.getUpdateStatus() != FotaUpdateStep.Finished) {
            setButtonsEnabled(false);
            return;
        }

        if(selected.getFotaState() == FotaState.Idle ) {
            enabled = true;
            connectButton.setText("Connect");
        }

        if(selected.getFotaState() ==  FotaState.Ready) {
            enabled = true;
            connectButton.setText("Disconnect");
        }
        setButtonsEnabled(enabled);
    }

    /**
     * Enables all buttons
     * @param enabled Enable if true, disable otherwise
     */
    private void setButtonsEnabled(boolean enabled) {
        connectButton.setEnabled(enabled);
        selectFileButton.setEnabled(enabled);

        if (isLegal) {
            updateButton.setEnabled(enabled);
        } else {
            updateButton.setEnabled(isLegal);
        }

    }

    /**
     * Update all text views according the current state
     */
    private void updateTextViews() {
        FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();

        if(imagePath != null) {
            filePathTextView.setText(imagePath);
        }

        if(firmwareFile != null) {
            if(firmwareFile.getFotaImage().getVersion() != null) {
                fotaVersionImgTextView.setText(firmwareFile.getFotaImage().getVersion().toString());
            }
            if(firmwareFile.getAppImage().getVersion() != null) {
                appVersionImgTextView.setText(firmwareFile.getAppImage().getVersion().toString());
            }
        }
        else {
            fotaVersionImgTextView.setText("");
            appVersionImgTextView.setText("");
        }

        if(selected == null) {
            stateTextView.setText("Idle");
            return;
        }

        stateTextView.setText(selected.getFotaState().toString());

        nameTextView.setText(selected.getName());
        addressTextView.setText(selected.getAddress());
        if(selected.getBootloaderVersion() != null) {
            bootloaderVersionTextView.setText(selected.getBootloaderVersion().toString());
        }

        if(selected.getBleStackVersion() != null) {
            fotaVersionTextView.setText(selected.getBleStackVersion().toString());
        }
        if(selected.getApplicationVersion() != null) {
            appVersionTextView.setText(selected.getApplicationVersion().toString());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        final FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();
        if(selected != null) {
            selected.addListener(listener);
        }
    }



    @Override
    protected void onPause(){
//        FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();
//        if(selected != null) {
//            selected.removeListener(listener);
//            try {
//                selected.teardown();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        closeWaitDialogTimer();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                imagePath = getFileName(uri);
                InputStream inputStreamHash = null;
                try {
                    ContentResolver cr = getBaseContext().getContentResolver();
                    InputStream inputStream = cr.openInputStream(uri);
                    inputStreamHash = cr.openInputStream(uri);
                    byte[] bytes = new byte[HASH_LENGTH_I];
                    int length = inputStream.read(bytes, 0, 40);
                    getFotaHash = new String(bytes, 0, length);
                    inputStreamHash.skip(HASH_LENGTH_L);
                    calculateFotaHash = calculateHashCode(inputStreamHash);
                    if (!getFotaHash.equals(calculateFotaHash)) {
                        updateStatusTextView.setText("Invalid Fota file");
                        isLegal = false;
                        Toast.makeText(this,R.string.toast_illegal,Toast.LENGTH_LONG).show();
                    } else {
                        updateStatusTextView.clearComposingText();
                        isLegal = true;
                    }
                    firmwareFile = new FotaFirmwareFile(inputStream);
                }
                catch (FotaException e) {
                    Log.e(TAG, "Failed to read fota file: " + e.getMessage());
                    updateStatusTextView.setText("Invalid Fota file");
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Failed to open fota file: " + e.getMessage());
                } finally {
                    if (inputStreamHash != null) {
                        try {
                            inputStreamHash.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                updateTextViews();
            }
        }
    }

    private String calculateHashCode(InputStream inputStream) {
        FileInputStream fis = (FileInputStream) inputStream;
        DigestInputStream digestInputStream = null;
        byte[] rb = null;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA1");
            digestInputStream = new DigestInputStream(fis, sha);
            byte[] buffer = new byte[4096];
            while (digestInputStream.read(buffer) > 0) {
                sha = digestInputStream.getMessageDigest();
            }
            rb = sha.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d(TAG, "calculateHashCode NoSuchAlgorithmException =" + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "calculateHashCode IOException =" + e);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rb.length; i++) {
            String a = Integer.toHexString(0XFF & rb[i]);
            if (a.length() < 2) {
                a = '0' + a;
            }
            sb.append(a);
        }

        String s = sb.toString().toUpperCase() + "&honRFID";
        s = HashUtil.getSha1String(s.getBytes());
        return s.toUpperCase();
    }

    /**
     * Start the update procedure
     */
    private void runUpdate() {
        FotaPeripheral selected = PeripheralTableActivity.PeripheralManager.getSelected();
        if(selected == null) {
            return;
        }

        try {
            throughputTimer = new Timer();
            throughputTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int progress;
                    synchronized (throughputTimer) {
                        progress = currentProgress - lastProgress;
                        lastProgress = currentProgress;
                        if(currentProgress == 0) {
                            return;
                        }
                    }
                    if(progress == 0) {
                        return;
                    }
                    final double throughput = (double)progress / 1024;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateStatusTextView.setText(controller.getUpdateStatus() + " - " + String.format( "%.2f", throughput ) + "kB/s");
                        }
                    });
                }
            }, 1000, 1000);
            FotaOptions options = new FotaOptions();
            options.setFile(firmwareFile);
            controller.addListener(updateControllerListener);
            selected.update(controller, options);
        } catch (final Exception e) {
            Log.e(TAG,"update failed" + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStatusTextView.setText("update failed: " + e.getMessage());
                }
            });
        }
        finally {
            controller.removeListener(updateControllerListener);
            throughputTimer.cancel();
            throughputTimer = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateButtons();
                }
            });
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String statusToString(int status) {
        switch(status) {
            case 0:
                return "Success";
            case 1:
                return "Download rejected due to incompatible Device ID";
            case 2:
                return "Download rejected due to incompatible Build ID (only for application sub-images)";
            case 3:
                return "Download rejected due to image size too large or small";
            case 4:
                return "Download failed due to flash storage error";
            case 5:
                return "Download failed due to invalid signature";
            case 6:
                return "Download rejected due to invalid start address";
            default:
                return "General Error";
        }
    }

    private static final int MAX_CONN_RETRY_COUNT = 5;
    private int mConnectRetryCount = 0;
    private boolean mConnecting;
    private ProgressDialog mWaitDialog;
    private Handler mWaitDialogTimerHandler = new Handler();

    private Runnable mWaitDialogRunnable = new Runnable() {
        @Override
        public void run() {
            closeWaitDialog();
        }
    };

    private void closeWaitDialogTimer() {
        if (mWaitDialogTimerHandler != null) {
            mWaitDialogTimerHandler.removeCallbacks(mWaitDialogRunnable);
        }
    }

    private void startWaitDialogTimer() {
        closeWaitDialogTimer();
        mWaitDialogTimerHandler.postDelayed(mWaitDialogRunnable, 10 * 1000);
    }

    private void closeWaitDialog() {
        mConnecting = false;
        mConnectRetryCount = 0;
        closeWaitDialogTimer();

        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
            mWaitDialog = null;
        }
    }

    private void showWaitDialog() {
        mConnectRetryCount = 0;
        mWaitDialog = ProgressDialog.show(this, "Connecting to FOTA service", null);
        startWaitDialogTimer();
    }
}
