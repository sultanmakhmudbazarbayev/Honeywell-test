package com.honeywell.bccrfid.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.navigation.NavigationView;
import com.honeywell.bccrfid.R;
import com.honeywell.bccrfid.fragment.settings.AdditionFragment;
import com.honeywell.bccrfid.fragment.settings.AntePowerFragment;
import com.honeywell.bccrfid.fragment.settings.CommonSettingsFragment;
import com.honeywell.bccrfid.fragment.settings.FastModeParamsFragment;
import com.honeywell.bccrfid.fragment.settings.GPIOFragment;
import com.honeywell.bccrfid.fragment.settings.Gen2OptionFragment;
import com.honeywell.bccrfid.fragment.settings.GeneralInfoFragment;
import com.honeywell.bccrfid.fragment.settings.InventoryFragment;
import com.honeywell.bccrfid.fragment.settings.InventoryParamsFragment;
import com.honeywell.bccrfid.fragment.settings.OtherParamsFragment;
import com.honeywell.bccrfid.fragment.settings.RegionFreqFragment;
import com.honeywell.bccrfid.test.DevOptionActivity;
import com.honeywell.bccrfid.utils.LogUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReaderSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReaderSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderSettingsFragment extends BaseFragment {
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
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private Handler mUIHandler;

    private AdditionFragment mAdditionFragment;
    private AntePowerFragment mAntePowerFragment;
    private CommonSettingsFragment mCommonSettingsFragment;
    private Gen2OptionFragment mGen2OptionFragment;
    private GPIOFragment mGPIOFragment;
    private InventoryFragment mInventoryFragment;
    private InventoryParamsFragment mInventoryParamsFragment;
    private OtherParamsFragment mOtherParamsFragment;
    private RegionFreqFragment mRegionFreqFragment;
    private FastModeParamsFragment mFastModeParamsFragment;
    private GeneralInfoFragment mGeneralInfoFragment;

    private static final int TYPE_ADDITION = 0;
    private static final int TYPE_ANTEPOWER = 1;
    private static final int TYPE_COMMONSETTINGS = 2;
    private static final int TYPE_GEN2OPTION = 3;
    private static final int TYPE_GPIO = 4;
    private static final int TYPE_INVENTORY = 5;
    private static final int TYPE_INVENTORYPARAMS = 6;
    private static final int TYPE_OTHERPARAMS = 7;
    private static final int TYPE_REGIION_FREQ = 8;
    private static final int TYPE_FASTMODEPARAMS = 9;
    private static final int TYPE_GENERAL_INFO = 10;
    private static final int TYPE_FCC_REGIION_FREQ = 11;

    public ReaderSettingsFragment() {
        // Required empty public constructor
    }

    public static ReaderSettingsFragment newInstance() {
        ReaderSettingsFragment readerSettingsFragment = new ReaderSettingsFragment();
        return readerSettingsFragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReaderSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReaderSettingsFragment newInstance(String param1, String param2) {
        ReaderSettingsFragment fragment = new ReaderSettingsFragment();
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
        mView = inflater.inflate(R.layout.fragment_reader_settings, container, false);
        initToolBar();
        init();

        View v = mView.findViewById(R.id.btn_dev);
        v.setOnClickListener(new View.OnClickListener() {
            int checking = 0;
            long prev = 0;

            @Override
            public void onClick(View view) {
                long cur = System.currentTimeMillis();

                if (cur - prev > 1000) {
                    checking = 0;
                } else if (++checking >= 20) {
                    checking = 0;
                    Intent intent = new Intent(getActivity(), DevOptionActivity.class);
                    getActivity().startActivity(intent);
                }

                prev = cur;
            }
        });
        return mView;
    }

    private void init() {
        mFragmentManager = getChildFragmentManager();
        mUIHandler = new Handler();
        initView();
        loadSettings();
        loadFragment(TYPE_COMMONSETTINGS);

        mNavigationView.setCheckedItem(R.id.common_settings);
    }

    private void initView() {
        mNavigationView = (NavigationView) mView.findViewById(R.id.slide_navigation);
        mDrawerLayout = (DrawerLayout) mView.findViewById(R.id.drawer_main);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                LogUtils.e("onNavigationItemSelected:" + menuItem.getItemId());
                switch (menuItem.getItemId()) {
                    case R.id.common_settings:
                        loadFragment(TYPE_COMMONSETTINGS);
                        break;
                    case R.id.inventory_params:
                        loadFragment(TYPE_INVENTORYPARAMS);
                        break;
                    case R.id.antenna_power:
                        loadFragment(TYPE_ANTEPOWER);
                        break;
                    case R.id.region_freq:
                        loadFragment(TYPE_REGIION_FREQ);
                        break;
                    case R.id.inventory:
                        loadFragment(TYPE_INVENTORY);
                        break;
                    case R.id.addtion:
                        loadFragment(TYPE_ADDITION);
                        break;
                    case R.id.gen2_option:
                        loadFragment(TYPE_GEN2OPTION);
                        break;
                    case R.id.gpio:
                        loadFragment(TYPE_GPIO);
                        break;
                    case R.id.other_params:
                        loadFragment(TYPE_OTHERPARAMS);
                        break;
                    case R.id.fast_mode_params:
                        loadFragment(TYPE_FASTMODEPARAMS);
                        break;
                    case R.id.general_information:
                        loadFragment(TYPE_GENERAL_INFO);
                        break;
                    case R.id.fcc_region_freq:
                        loadFragment(TYPE_FCC_REGIION_FREQ);
                        break;
                }

                final String title = menuItem.getTitle().toString();
                mUIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mToolbar.setTitle(title);
                        mDrawerLayout.closeDrawer(mNavigationView, false);
                    }
                }, 300);
                return true;
            }
        });
    }

    private void hideAllTabs(FragmentTransaction fragTran) {
        if (mAdditionFragment != null) {
            fragTran.hide(mAdditionFragment);
        }
        if (mAntePowerFragment != null) {
            fragTran.hide(mAntePowerFragment);
        }
        if (mCommonSettingsFragment != null) {
            fragTran.hide(mCommonSettingsFragment);
        }
        if (mGen2OptionFragment != null) {
            fragTran.hide(mGen2OptionFragment);
        }
        if (mGPIOFragment != null) {
            fragTran.hide(mGPIOFragment);
        }
        if (mInventoryFragment != null) {
            fragTran.hide(mInventoryFragment);
        }
        if (mInventoryParamsFragment != null) {
            fragTran.hide(mInventoryParamsFragment);
        }
        if (mOtherParamsFragment != null) {
            fragTran.hide(mOtherParamsFragment);
        }
        if (mRegionFreqFragment != null) {
            fragTran.hide(mRegionFreqFragment);
        }
        if (mFastModeParamsFragment != null) {
            fragTran.hide(mFastModeParamsFragment);
        }
        if (mGeneralInfoFragment != null) {
            fragTran.hide(mGeneralInfoFragment);
        }
    }


    private void loadSettings() {

    }

    private void loadFragment(int index) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        hideAllTabs(fragmentTransaction);
        switch (index) {
            case TYPE_ADDITION:
                if (mAdditionFragment == null) {
                    mAdditionFragment = new AdditionFragment();
                    fragmentTransaction.add(R.id.main, mAdditionFragment);
                } else {
                    fragmentTransaction.show(mAdditionFragment);
                }
                break;
            case TYPE_ANTEPOWER:
                if (mAntePowerFragment == null) {
                    mAntePowerFragment = new AntePowerFragment();
                    fragmentTransaction.add(R.id.main, mAntePowerFragment);
                } else {
                    fragmentTransaction.show(mAntePowerFragment);
                }
                break;
            case TYPE_COMMONSETTINGS:
                if (mCommonSettingsFragment == null) {
                    mCommonSettingsFragment = new CommonSettingsFragment();
                    fragmentTransaction.add(R.id.main, mCommonSettingsFragment);
                } else {
                    fragmentTransaction.show(mCommonSettingsFragment);
                }
                break;
            case TYPE_GEN2OPTION:
                if (mGen2OptionFragment == null) {
                    mGen2OptionFragment = new Gen2OptionFragment();
                    fragmentTransaction.add(R.id.main, mGen2OptionFragment);
                } else {
                    fragmentTransaction.show(mGen2OptionFragment);
                }
                break;
            case TYPE_GPIO:
                if (mGPIOFragment == null) {
                    mGPIOFragment = new GPIOFragment();
                    fragmentTransaction.add(R.id.main, mGPIOFragment);
                } else {
                    fragmentTransaction.show(mGPIOFragment);
                }
                break;
            case TYPE_INVENTORY:
                if (mInventoryFragment == null) {
                    mInventoryFragment = new InventoryFragment();
                    fragmentTransaction.add(R.id.main, mInventoryFragment);
                } else {
                    fragmentTransaction.show(mInventoryFragment);
                }
                break;
            case TYPE_INVENTORYPARAMS:
                if (mInventoryParamsFragment == null) {
                    mInventoryParamsFragment = new InventoryParamsFragment();
                    fragmentTransaction.add(R.id.main, mInventoryParamsFragment);
                } else {
                    fragmentTransaction.show(mInventoryParamsFragment);
                }
                break;
            case TYPE_OTHERPARAMS:
                if (mOtherParamsFragment == null) {
                    mOtherParamsFragment = new OtherParamsFragment();
                    fragmentTransaction.add(R.id.main, mOtherParamsFragment);
                } else {
                    fragmentTransaction.show(mOtherParamsFragment);
                }
                break;
            case TYPE_REGIION_FREQ:
                if (mRegionFreqFragment == null) {
                    mRegionFreqFragment = new RegionFreqFragment();
                    fragmentTransaction.add(R.id.main, mRegionFreqFragment);
                } else {
                    fragmentTransaction.show(mRegionFreqFragment);
                }
                break;
            case TYPE_FASTMODEPARAMS:
                if (mFastModeParamsFragment == null) {
                    mFastModeParamsFragment = new FastModeParamsFragment();
                    fragmentTransaction.add(R.id.main, mFastModeParamsFragment);
                } else {
                    fragmentTransaction.show(mFastModeParamsFragment);
                }
                break;
            case TYPE_GENERAL_INFO:
                if (mGeneralInfoFragment == null) {
                    mGeneralInfoFragment = new GeneralInfoFragment();
                    fragmentTransaction.add(R.id.main, mGeneralInfoFragment);
                } else {
                    fragmentTransaction.show(mGeneralInfoFragment);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void initToolBar() {
        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        //mToolbar.setNavigationIcon(R.drawable.position_left);
        //mToolbar.setTitle(getString(R.string.settings_text));
        mToolbar.setTitle(getString(R.string.common_settings));
        mToolbar.setNavigationIcon(R.drawable.toolbar_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
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
