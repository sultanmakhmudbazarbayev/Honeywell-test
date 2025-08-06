package com.honeywell.bccrfid.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.fragment.BaseFragment;
import com.honeywell.bccrfid.utils.Log;
import com.honeywell.rfidservice.rfid.Region;
import com.honeywell.rfidservice.rfid.RfidReaderException;

import java.util.Arrays;
import java.util.HashMap;

import androidx.appcompat.widget.AppCompatSpinner;

import static com.honeywell.rfidservice.rfid.Region.Unknown;

public class RegionFreqFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "RegionFreqFragment";

    private OnFragmentInteractionListener mListener;
    private ListView mFreListView;
    private AppCompatSpinner mAreaSpinner;

    private App mApp;
    private View mView;
    private FreqAdapter mFreAdapter;
    private HashMap<Integer, Boolean> mCheckStatus = new HashMap<Integer, Boolean>();

    private Region mDeviceRegion;
    private int[] mDeviceFreqTable;
    boolean mSpinnerFirstSelect = true;

    public RegionFreqFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_region_freq, container, false);
        init();
        return mView;
    }

    private void init() {
        mApp = App.getInstance();
        mFreAdapter = new FreqAdapter(getActivity(), R.layout.listview_fre);
        initView();
    }

    private void initView() {
        mFreListView = mView.findViewById(R.id.list_fre);
        mAreaSpinner = mView.findViewById(R.id.spinner_area);
        mAreaSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
        mFreListView.setAdapter(mFreAdapter);

        loadSettings();
    }

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mSpinnerFirstSelect) {
                mSpinnerFirstSelect = false;
                return;
            }

            String selected = mAreaSpinner.getSelectedItem().toString();
            Region r = Region.get(selected);
            Log.i(TAG, "========> On Area Spinner Item Selected : " + selected + ", [" + r + "]");

            if (r == Unknown) {
                showToast(R.string.toast_invalid_region_name);
                return;
            }

            try {
                if (mApp.checkIsRFIDReady()) {
                    mApp.rfidReader.setRegion(r);
                    mDeviceRegion = r;
                    updateFrequencies();
                    showToast(getString(R.string.toast_set_region_successfully));
                }
            } catch (RfidReaderException e) {
                showToast(getString(R.string.toast_set_region_failed) + e.getMessage());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private void loadSettings() {
        if (mApp.checkIsRFIDReady()) {
            try {
                mDeviceRegion = mApp.rfidReader.getRegion();

                for (int i = 0; i < mAreaSpinner.getAdapter().getCount(); i++) {
                    String item = mAreaSpinner.getAdapter().getItem(i).toString();

                    if (item.equals(mDeviceRegion.getName())) {
                        mAreaSpinner.setOnItemSelectedListener(null);
                        mAreaSpinner.setSelection(i);
                        mAreaSpinner.setOnItemSelectedListener(mOnItemSelectedListener);

                        updateFrequencies();
                        return;
                    }
                }
            } catch (RfidReaderException e) {
                showToast(getString(R.string.toast_get_region_failed) + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View v) {
    }

    private void updateFrequencies() {
        try {
            if (mApp.checkIsRFIDReady()) {
                mDeviceFreqTable = mApp.rfidReader.getFreqHopTable();
                Arrays.sort(mDeviceFreqTable);
                mFreAdapter.notifyDataSetChanged();
            }
        } catch (RfidReaderException e) {
            showToast(getString(R.string.toast_view_freq_failed) + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class FreqAdapter extends ArrayAdapter<Integer> {
        int resId;

        public FreqAdapter(Context context, int resourceId) {
            super(context, resourceId);
            resId = resourceId;
        }

        @Override
        public int getCount() {
            if (mDeviceFreqTable == null) {
                return 0;
            }

            return mDeviceFreqTable.length;
        }

        public Integer getItem(int position) {
            if (mDeviceFreqTable == null || position >= mDeviceFreqTable.length) {
                return 0;
            }

            return mDeviceFreqTable[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            View v;

            if (convertView == null) {
                v = LayoutInflater.from(getActivity()).inflate(resId, parent, false);
                vh = new ViewHolder();
                vh.freCkb = v.findViewById(R.id.list_item_fre_ckb);
                v.setTag(vh);
            } else {
                v = convertView;
                vh = (ViewHolder) v.getTag();
            }

            vh.freCkb.setChecked(true);
            vh.freCkb.setText(String.valueOf(getItem(position)));
            vh.freCkb.setEnabled(false);
            vh.freCkb.setClickable(false);
            return v;
        }

        class ViewHolder {
            CheckBox freCkb;
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

    private void showToast(int resId) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String s) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        }
    }
}
