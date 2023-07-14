package com.hitsuthar.tracker.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hitsuthar.tracker.Common;
import com.hitsuthar.tracker.data.InternetBlocklist;
import com.hitsuthar.tracker.data.TrackerCategory;
import com.hitsuthar.tracker.data.TrackerList;

import com.hitsuthar.tracker.R;

import java.util.List;

public class TrackersFragment extends Fragment {
    private static final String ARG_APP_ID = "app-id";
    private static final String ARG_APP_UID = "app-uid";
    private final String TAG = TrackersFragment.class.getSimpleName();
    private TrackerList trackerList;
    private String mAppId;
    private int mAppUid;
    private SwipeRefreshLayout swipeRefresh;
    private TrackersListAdapter adapter;
    private boolean running = false;

    public TrackersFragment() {
    }

    public static TrackersFragment newInstance(String appId, int uid) {
        TrackersFragment fragment = new TrackersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_APP_ID, appId);
        args.putInt(ARG_APP_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAppId = bundle.getString(ARG_APP_ID);
            mAppUid = bundle.getInt(ARG_APP_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trackers, container, false);
        running = true;
        Context c = v.getContext();
        trackerList = TrackerList.getInstance(c);
        RecyclerView recyclerView = v.findViewById(R.id.transmissions_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        adapter = new TrackersListAdapter(getActivity(), recyclerView, mAppUid, mAppId); // Activity is needed here
        recyclerView.setAdapter(adapter);
        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::updateTrackerList);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTrackerList();
    }


    public void updateTrackerList() {
        new AsyncTask<Object, Object, List<TrackerCategory>>() {
            private boolean refreshing = true;
            @Override
            protected void onPreExecute() {
                if (swipeRefresh != null)
                    swipeRefresh.post(() -> {
                        if (refreshing)
                            swipeRefresh.setRefreshing(true);
                    });
            }
            @Override
            protected List<TrackerCategory> doInBackground(Object... arg) {
                Context c = getContext();
                if (c == null)
                    return null;
                return trackerList.getAppTrackers(c, mAppUid);
            }
            @Override
            protected void onPostExecute(List<TrackerCategory> result) {
                if (running) {
                    if (adapter != null)
                        adapter.set(result);

                    if (swipeRefresh != null) {
                        refreshing = false;
                        swipeRefresh.setRefreshing(false);
                    }
                    if (result != null && result.size() == 0)
                        suggestLaunchingApp();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void suggestLaunchingApp() {
        Activity activity = getActivity();
        if (activity == null)
            return;

        SharedPreferences apply = activity.getSharedPreferences("apply", Context.MODE_PRIVATE);
        InternetBlocklist w = InternetBlocklist.getInstance(activity);
        if (!apply.getBoolean(mAppId, true)
                || w.blockedInternet(mAppUid))
            return;
        final Intent launch = Common.getLaunchIntent(activity, mAppId);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//        if (launch != null) {
//            final boolean enabled = prefs.getBoolean("enabled", false);
//            int msg = enabled ? R.string.no_trackers_found_message : R.string.no_trackers_found_message_disabled;
//            Snackbar s = Common.getSnackbar(activity, msg);
//            if (s == null)
//                return;
//
//            s.setAction(enabled ? R.string.no_trackers_found_action : R.string.back, v -> {
//                if (enabled)
//                    activity.startActivity(launch);
//                else
//                    activity.finish();
//            }).show();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }
}
