package com.honeywell.bccrfid.test;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class DevOptionActivity extends ListActivity {
    private static final String TAG = "DevOptionActivity";
    private int titles[] = {R.string.debug_mode, R.string.get_charge_cycle_title, R.string.beeper_test_title, R.string.carrier_test_title, R.string.rfid_fw_update_title};

    private boolean init = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> strs = new ArrayList<>();

        for (int title : titles) {
            strs.add(getString(title));
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, strs);
        setListAdapter(arrayAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init = false;
            }
        }, 1000);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent;

        if (init) {
            return;
        }

        switch (titles[position]) {
            case R.string.debug_mode:
                App.getInstance().setDebugMode(!App.getInstance().debugMode);
                Toast.makeText(DevOptionActivity.this, "Debug mode: " + App.getInstance().debugMode, Toast.LENGTH_SHORT).show();
            case R.string.get_charge_cycle_title:
                App app = App.getInstance();

                if (app.checkIsRFIDReady()) {
                    int chargeCycle = app.rfidMgr.getBatteryChargeCycle();
                    Toast.makeText(DevOptionActivity.this, "Battery charge cycle: " + chargeCycle, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.string.beeper_test_title:
                intent = new Intent(DevOptionActivity.this, BeeperTestActivity.class);
                startActivity(intent);
                break;
            case R.string.carrier_test_title:
                intent = new Intent(DevOptionActivity.this, CarrierTestActivity.class);
                startActivity(intent);
                break;
            case R.string.rfid_fw_update_title:
                intent = new Intent(DevOptionActivity.this, RfidFwUpdateActivity.class);
                startActivity(intent);
                break;
        }
    }

}
