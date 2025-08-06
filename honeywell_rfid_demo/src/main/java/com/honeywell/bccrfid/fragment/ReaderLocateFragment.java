package com.honeywell.bccrfid.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.honeywell.bccrfid.App;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.ReaderReadAdapter;
import com.honeywell.bccrfid.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReaderLocateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReaderLocateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderLocateFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private App mApp;

    private RecyclerView mRecyclerView;
    private ReaderReadAdapter mReaderReadAdapter;
    private View mView;
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private AppCompatCheckBox mChkBoxFindingGood;
    private String mFilterStr = "";

    private List<Map<String, ?>> listAll = Collections.synchronizedList(new ArrayList<Map<String, ?>>());
    private List<Map<String, ?>> listQuery = Collections.synchronizedList(new ArrayList<Map<String, ?>>());

    public ReaderLocateFragment() {
        // Required empty public constructor
    }

    public static ReaderLocateFragment newInstance() {
        ReaderLocateFragment readerLocaleFragment = new ReaderLocateFragment();
        return readerLocaleFragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReaderWriteLockTagFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReaderLocateFragment newInstance(String param1, String param2) {
        ReaderLocateFragment fragment = new ReaderLocateFragment();
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
        mView = inflater.inflate(R.layout.fragment_reader_locate, container, false);
        initToolBar();
        init();
        return mView;
    }


    private void initToolBar() {
        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu_locate_tag);
        //mToolbar.setNavigationIcon(R.drawable.position_left);
        mToolbar.setTitle(getString(R.string.locate_tag_text));
        MenuItem menuItem = mToolbar.getMenu().findItem(R.id.locate_tag_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                LogUtils.e("onQueryTextSubmit:" + s);
                mFilterStr = s;
                doQuery();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                LogUtils.e("onQueryTextChange:" + s);
                LogUtils.e("onQueryTextChange:" + (s == null ? "null" : "not null"));
                LogUtils.e("onQueryTextChange:" + s.equals(""));
                LogUtils.e("onQueryTextChange length:" + s.length());
                mFilterStr = s;
                doQuery();
                return false;
            }
        });
    }

    private void init() {
        LogUtils.e("liuqipeng", "init");
        mApp = (App) getActivity().getApplication();
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mReaderReadAdapter = new ReaderReadAdapter(mApp, listQuery);
        mRecyclerView.setAdapter(mReaderReadAdapter);
        mReaderReadAdapter.notifyDataSetChanged();
        mChkBoxFindingGood = (AppCompatCheckBox) mView.findViewById(R.id.chkbox_finding_good);
        mChkBoxFindingGood.setChecked(mApp.mIsFindingGood);
        mChkBoxFindingGood.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    if (mApp.mSelectedEpc == null || mApp.mSelectedEpc.length() == 0) {
                        mChkBoxFindingGood.setChecked(!isChecked);
                        Toast.makeText(getActivity(), getString(R.string.toast_select_epc), Toast.LENGTH_SHORT).show();
                    }
                }
                mApp.mIsFindingGood = mChkBoxFindingGood.isChecked();
                LogUtils.e("mApp.mIsFindingGood:" + mApp.mIsFindingGood);
            }
        });
        //doQuery();
    }

    private void initList() {
        listAll.clear();
        listAll.addAll(mApp.ListMs);
        listQuery.clear();
        listQuery.addAll(mApp.ListMs);
        mReaderReadAdapter.notifyDataSetChanged();
    }

    private void doQuery() {
        if (mFilterStr != null && !mFilterStr.equals("")) {
            LogUtils.e("doSearch");
            listQuery.clear();
            int num = 0;
            if(listAll.size() == 0){
                return;
            }

            Map<String, String> m = (Map<String, String>) ((HashMap<String, String>) listAll.get(0)).clone();
            listQuery.add(m);
            for (int i = 1; i < listAll.size(); i++) {
                m = (Map<String, String>) ((HashMap<String, String>) listAll.get(i)).clone();
                //if (m.get(mApp.Coname[1]).toLowerCase().contains(mFilterStr.toLowerCase())) {
                if (m.get(mApp.Coname[1]).contains(mFilterStr.toUpperCase())) {
                    m.put(mApp.Coname[0], String.valueOf(++num));
                    listQuery.add(m);
                }
            }

            mReaderReadAdapter.notifyDataSetChanged();
        } else {
            initList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initList();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {

        } else {
            initList();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
