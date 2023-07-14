package com.hitsuthar.tracker.data;

import static com.hitsuthar.tracker.data.TrackerBlocklist.PREF_BLOCKLIST;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class InternetBlocklist {
    public static final String SHARED_PREFS_INTERNET_BLOCKLIST_APPS_KEY = "INTERNET_BLOCKLIST_APPS_KEY";
    private static InternetBlocklist instance;
    private final HashSet<Integer> blockmap = new HashSet<>();

    private InternetBlocklist(Context c) {
        if (c != null) {
            loadSettings(c);
        }
    }
    public static InternetBlocklist getInstance(Context c) {
        if (instance == null)
            instance = new InternetBlocklist(c);
        return instance;
    }
    public void loadSettings(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_BLOCKLIST, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(SHARED_PREFS_INTERNET_BLOCKLIST_APPS_KEY, null);

        if (set != null) {
            blockmap.clear();
            for (String id : set) {
                int uid = Integer.parseInt(id);
                blockmap.add(uid);
            }
        }
    }
    public Set<Integer> getBlocklist() {
        return blockmap;
    }

    public void clear() {
        blockmap.clear();
    }

    public synchronized void block(int uid) {
        blockmap.add(uid);
    }

    public synchronized void unblock(int uid) {
        blockmap.remove(uid);
    }

    public boolean blockedInternet(int uid) {
        return blockmap.contains(uid);
    }
}
