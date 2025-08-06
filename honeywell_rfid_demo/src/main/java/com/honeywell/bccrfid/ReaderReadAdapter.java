package com.honeywell.bccrfid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeywell.bccrfid.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class ReaderReadAdapter extends RecyclerView.Adapter<ReaderReadAdapter.MyViewHolder> {
    private ReaderReadAdapter.MyViewHolder holder;
    private Map<String, String> m;
    private int[] colors;
    private int mSelectedColor;
    private App mApp;
    private List<Map<String, ?>> mShowList = Collections.synchronizedList(new ArrayList<Map<String, ?>>());

    private int mCount;

    public ReaderReadAdapter() {

    }

    public ReaderReadAdapter(App app, List<Map<String, ?>> list) {
        mApp = app;
        mShowList = list;
        colors = new int[]{mApp.getResources().getColor(R.color.tag_item_color1), mApp.getResources().getColor(R.color.tag_item_color2)};
        mSelectedColor = mApp.getResources().getColor(R.color.tag_item_selected);
    }

    private String mScanMode = Const.SCAN_MODE_NORMAL;
    private boolean mItemCount = false;
    private boolean mItemAnt = false;
    private boolean mItemPro = false;
    private boolean mItemRssi = false;
    private boolean mItemFreq = false;
    private boolean mItemData = false;
    private boolean mItemAdditionType = false;
    private boolean mItemTime = false;

    public void updateShowItems(){
        mScanMode = mApp.mSharedPrefManager.getString(Const.SP_KEY_SCAN_MODE, Const.SCAN_MODE_NORMAL);
        mItemCount = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_COUNT, false);
        mItemAnt = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_ANT, false);
        mItemPro = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_PRO, false);
        mItemRssi = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_RSSI, false);
        mItemFreq = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_FREQ, false);
        mItemData = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_DATA, false);
        mItemAdditionType = !mApp.mSharedPrefManager.getString(Const.SP_KEY_ADDITION_TAG_DATA_TYPE, "None").equals("None");
        mItemTime = mApp.mSharedPrefManager.getBoolean(Const.SP_KEY_ITEM_TIME, false);
    }
    @Override
    public ReaderReadAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtils.e("sssssss onCreateViewHolder:" + mCount++);
        holder = new ReaderReadAdapter.MyViewHolder(LayoutInflater.from(
                mApp).inflate(R.layout.listitemview_inv, parent,
                false));
        return holder;
    }

    private Map<String, String> findTargetTag(List<Map<String, ?>> list) {
        for (int i = 0; i < list.size(); i++) {
            if (mApp.mSelectedEpc.equals(list.get(i).get(mApp.Coname[1]))) {
                return (Map<String, String>) list.get(i);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ReaderReadAdapter.MyViewHolder holder, int position) {
        LogUtils.e("sssssss onBindViewHolder");
        if (mShowList == null || mShowList.size() == 0) {
            return;
        }
        m = (Map<String, String>) mShowList
                .get(position);
        holder.item.setBackgroundColor(colors[position % 2]);
        holder.item.setTag(position);
        holder.readsort.setText(m.get(mApp.Coname[0]));
        holder.readepc.setText(m.get(mApp.Coname[1]));
        holder.readcnt.setText(m.get(mApp.Coname[2]));
        holder.readant.setText(m.get(mApp.Coname[3]));
        holder.readpro.setText(m.get(mApp.Coname[4]));
        holder.readrssi.setText(m.get(mApp.Coname[5]));
        holder.readfre.setText(m.get(mApp.Coname[6]));
        holder.reademd.setText(m.get(mApp.Coname[7]));
        holder.readetime.setText(m.get(mApp.Coname[8]));
        if(mScanMode.equals(Const.SCAN_MODE_FAST)){
            holder.readcnt.setVisibility(mItemCount ? View.VISIBLE : View.GONE);
            holder.readant.setVisibility(mItemAnt ? View.VISIBLE : View.GONE);
            holder.readpro.setVisibility(mItemPro ? View.VISIBLE : View.GONE);
            holder.readrssi.setVisibility(mItemRssi ? View.VISIBLE : View.GONE);
            holder.readfre.setVisibility(mItemFreq ? View.VISIBLE : View.GONE);
            boolean showMd = mItemData && mItemAdditionType;
            holder.reademd.setVisibility(showMd ? View.VISIBLE : View.GONE);
            holder.readetime.setVisibility(mItemTime ? View.VISIBLE : View.GONE);
        }else {
            holder.readcnt.setVisibility(View.VISIBLE);
            holder.readant.setVisibility(View.GONE);
            holder.readpro.setVisibility(View.GONE);
            holder.readrssi.setVisibility(View.VISIBLE);
            holder.readfre.setVisibility(View.VISIBLE);
            holder.reademd.setVisibility(View.GONE);
            holder.readetime.setVisibility(View.GONE);
        }
        if (holder.readepc.getText().toString().equals(mApp.mSelectedEpc)) {
            holder.item.setBackgroundColor(mSelectedColor);
        }
    }

    @Override
    public int getItemCount() {
        LogUtils.e("mShowList.size():" + mShowList.size());
        return mShowList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        View item;
        TextView readsort;
        TextView readepc;
        TextView readcnt;
        TextView readant;
        TextView readpro;
        TextView readrssi;
        TextView readfre;
        TextView reademd;
        TextView readetime;
        View.OnClickListener clickListener;

        public MyViewHolder(final View view) {
            super(view);
            item = view;
            readsort = (TextView) view.findViewById(R.id.textView_readsort);
            readepc = (TextView) view.findViewById(R.id.textView_readepc);
            readcnt = (TextView) view.findViewById(R.id.textView_readcnt);
            readant = (TextView) view.findViewById(R.id.textView_readant);
            readpro = (TextView) view.findViewById(R.id.textView_readpro);
            readrssi = (TextView) view.findViewById(R.id.textView_readrssi);
            readfre = (TextView) view.findViewById(R.id.textView_readfre);
            reademd = (TextView) view.findViewById(R.id.textView_reademd);
            readetime = (TextView) view.findViewById(R.id.textView_timestamp);
            if (clickListener == null) {
                clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (Integer) view.getTag();
                        if (position != 0) {
                            mApp.mSelectedEpc = readepc.getText().toString();
                            notifyDataSetChanged();
                        }
                    }
                };
            }
            view.setOnClickListener(clickListener);
        }
    }
}
