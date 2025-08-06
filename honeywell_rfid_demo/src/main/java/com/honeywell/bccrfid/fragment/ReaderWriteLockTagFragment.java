package com.honeywell.bccrfid.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.fragment.writelock.LockFragment;
import com.honeywell.bccrfid.fragment.writelock.WriteFragment;
import com.honeywell.bccrfid.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReaderWriteLockTagFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReaderWriteLockTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderWriteLockTagFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View mView;
    private Toolbar mToolbar;
    private TabLayout mTab;
    private ViewPager mViewPager;

    private List<String> mTitle;
    private List<Fragment> mFragment;

    private WriteFragment mWriteFragment;
    private LockFragment mLockFragment;

    public ReaderWriteLockTagFragment() {
        // Required empty public constructor
    }

    public static ReaderWriteLockTagFragment newInstance() {
        ReaderWriteLockTagFragment readerWriteLockTagFragment = new ReaderWriteLockTagFragment();
        return readerWriteLockTagFragment;
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
    public static ReaderWriteLockTagFragment newInstance(String param1, String param2) {
        ReaderWriteLockTagFragment fragment = new ReaderWriteLockTagFragment();
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
        mView = inflater.inflate(R.layout.fragment_reader_write_lock_tag, container, false);
        initToolBar();
        init(mView);
        return mView;
    }

    private void initToolBar() {
        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        //mToolbar.setNavigationIcon(R.drawable.position_left);
        mToolbar.setTitle(getString(R.string.write_lock_tag_text));
    }

    private void init(View parent) {
        initTab();
    }

    private void initTab() {
        mTab = (TabLayout) mView.findViewById(R.id.tab);
        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
        mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LogUtils.e("onTabSelected:" + tab.getPosition());
                if (tab.getPosition() == 0) {
                    if (mWriteFragment != null && mWriteFragment.isAdded()
                            && mLockFragment != null && mLockFragment.isAdded()) {
                        mWriteFragment.updatePasswordLayout(mLockFragment.getPwdState(),mLockFragment.getPwd());
                    }
                } else if (tab.getPosition() == 1) {
                    if (mWriteFragment != null && mWriteFragment.isAdded()
                            && mLockFragment != null && mLockFragment.isAdded()) {
                        mLockFragment.updatePasswordLayout(mWriteFragment.getPwdState(),mWriteFragment.getPwd());
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager = (ViewPager) mView.findViewById(R.id.view_pager);
        mTitle = new ArrayList<>();
        mTitle.add(getString(R.string.write_tag));
        mTitle.add(getString(R.string.lock_tag));
        mFragment = new ArrayList<>();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mWriteFragment = new WriteFragment();
                mLockFragment = new LockFragment();
                mFragment.add(mWriteFragment);
                mFragment.add(mLockFragment);

                mViewPager.setAdapter(new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
                    @Override
                    public Fragment getItem(int i) {
                        return mFragment.get(i);
                    }

                    @Override
                    public int getCount() {
                        return mFragment.size();
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        return mTitle.get(position);
                    }
                });
                mViewPager.setOffscreenPageLimit(2);
                mTab.setupWithViewPager(mViewPager);
            }
        });
    }

    private void updateSelectedEpc() {
        if (mWriteFragment != null && mWriteFragment.isAdded()) {
            mWriteFragment.updateSelectedEpc();
        }
        if (mLockFragment != null && mLockFragment.isAdded()) {
            mLockFragment.updateSelectedEpc();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {

        } else {
            updateSelectedEpc();
        }
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
