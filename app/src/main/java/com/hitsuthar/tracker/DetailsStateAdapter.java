package com.hitsuthar.tracker;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hitsuthar.tracker.details.ActionsFragment;
import com.hitsuthar.tracker.details.CountriesFragment;
import com.hitsuthar.tracker.details.TrackersFragment;

public class DetailsStateAdapter extends FragmentStateAdapter {
    @StringRes
    private static int[] TAB_TITLES = {
            R.string.tab_trackers,
            R.string.tab_countries,
    };
    private final String TAG = DetailsStateAdapter.class.getSimpleName();
    private final int mUid;

    private final TrackersFragment fTrackers;
    private final ActionsFragment fActions;
    private CountriesFragment fCountries;

    public DetailsStateAdapter(FragmentActivity fa, String appId, String appName, int uid) {
        super(fa);
        mUid = uid;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            TAB_TITLES = new int[]{
                    R.string.tab_trackers,
            };
        } else {
            TAB_TITLES = new int[]{
                    R.string.tab_trackers,
                    R.string.tab_countries,
            };
        }
        fTrackers = TrackersFragment.newInstance(appId, uid);
        fActions = ActionsFragment.newInstance(appId, appName);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            switch (position) {
                case 0:
                    return fTrackers;
            }
        } else {
            switch (position) {
                case 0:
                    return fTrackers;
                case 1:
                    if (fCountries == null)
                        fCountries = CountriesFragment.newInstance(mUid);
                    return fCountries;
            }
        }
        return fTrackers;
    }
    public int getPageTitle(final int position) {
        return TAB_TITLES[position];
    }
    @Override
    public int getItemCount() {
        return TAB_TITLES.length;
    }
    void updateTrackerLists() {
        if (fTrackers != null)
            fTrackers.updateTrackerList();
    }
}