package com.onsemi.fota;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.honeywell.bccrfid.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Adapter implementation for list view to show the list of visible BLE devices
 */

public class PeripheralAdapter extends BaseAdapter {

    private final Context context;
    private final List<FotaPeripheral> values;
    private final LayoutInflater inflater;
    private final Handler updateRssiHandler = new Handler();
    private final BaseAdapter adapter = this;

    public PeripheralAdapter(Context context, List<FotaPeripheral> peripherals) {
        super();
        this.context = context;
        this.values = peripherals;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updateRssiHandler.post(executeUpdateRssi);
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Object getItem(int i) {
        return values.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.layout_peripheral_entry, parent, false);
        }
        TextView textViewAddress = convertView.findViewById(R.id.peripheral_entry_address);
        TextView textViewName = convertView.findViewById(R.id.peripheral_entry_name);
        TextView textViewRssi = convertView.findViewById(R.id.peripheral_entry_rssi);
        textViewName.setText(values.get(position).getName());
        textViewAddress.setText(values.get(position).getAddress());
        textViewRssi.setText(values.get(position).getRssi() + "dB");

        return convertView;
    }

    /**
     * Update list periodically to show the changing RSSI value
     */
    private Runnable executeUpdateRssi = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            // Repeat this the same runnable code block again another 2 seconds
            // 'this' is referencing the Runnable object
            adapter.notifyDataSetChanged();
            updateRssiHandler.postDelayed(this, 500);
        }
    };

    /**
     * Update the list
     * @param peripherals
     */
    public void update(List<FotaPeripheral> peripherals) {
        List<FotaPeripheral> toRemove = new LinkedList<>();
        // remove entries first
        for(FotaPeripheral p : values) {
            if(!peripherals.contains(p)) {
                toRemove.add(p);
            }
        }

        values.removeAll(toRemove);

        // add entries
        for(FotaPeripheral p : peripherals) {
            if(!values.contains(p)) {
                values.add(p);
            }
        }
        this.notifyDataSetChanged();
    }

}
