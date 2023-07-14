package com.hitsuthar.tracker.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerBlocklist {
    public static final String SHARED_PREFS_BLOCKLIST_APPS_KEY = "APPS_BLOCKLIST_APPS_KEY";
    final public static String PREF_BLOCKLIST = "blocklist";
    public static String NECESSARY_CATEGORY = "Content";
    private static TrackerBlocklist instance;

    private final Map<Integer, Set<String>> blockmap = new ConcurrentHashMap<>();

    private TrackerBlocklist(Context c) {
        if (c != null)
            loadSettings(c);
    }

    public static TrackerBlocklist getInstance(Context c) {
        if (instance == null)
            instance = new TrackerBlocklist(c);

        return instance;
    }
    public static String getBlockingKey(Tracker t) {
        return t.category + " | " + t.getName();
    }
    public void loadSettings(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_BLOCKLIST, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(SHARED_PREFS_BLOCKLIST_APPS_KEY, null);

        if (set != null) {
            blockmap.clear();
            for (String appUid : set) {
                Set<String> prefset = prefs.getStringSet
                        (SHARED_PREFS_BLOCKLIST_APPS_KEY + "_" + appUid, null);
                Set<String> subset = new HashSet<>();
                if (prefset != null)
                    subset.addAll(prefset);

                if (subset.contains("Uncategorised | Alphabet")) {
                    subset.remove("Uncategorised | Alphabet");
                    subset.add("Uncategorised | Google");
                }
                if (subset.contains("Uncategorised | Adobe Systems")) {
                    subset.remove("Uncategorised | Adobe Systems");
                    subset.add("Uncategorised | Adobe");
                }
                int uid = -1;
                if (StringUtils.isNumeric(appUid))
                    uid = Integer.parseInt(appUid);
                else {
                    try {
                        uid = c.getPackageManager().getApplicationInfo(appUid, 0).uid;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (uid >= 0)
                    blockmap.put(uid, subset);
            }
        }
    }

    public Set<Integer> getBlocklist() {
        return blockmap.keySet();
    }
    public Set<String> getSubset(int uid) {
        return blockmap.get(uid);
    }
    public void clear() {
        blockmap.clear();
    }
    public void clear(int uid) {
        blockmap.remove(uid);
    }
    public synchronized void block(int uid, String t) {
        Set<String> app = blockmap.get(uid);
        if (app == null)
            return;
        app.remove(t);
    }
    public synchronized void unblock(int uid, String t) {
        Set<String> app = blockmap.get(uid);

        if (app == null) {
            app = new HashSet<>();
            blockmap.put(uid, app);
        }
        app.add(t);
    }
    public synchronized void block(int uid, Tracker t) {
        block(uid, getBlockingKey(t));
    }
    public synchronized void unblock(int uid, Tracker t) {
        unblock(uid, getBlockingKey(t));
    }
    public boolean blocked(int uid, String key) {
        Set<String> trackers = this.getSubset(uid);
        if (trackers == null) {
            return true;
        }

        return !trackers.contains(key);
    }
    public boolean blockedTracker(int uid, Tracker t) {
        return blocked(uid, t.category)
                && blocked(uid, getBlockingKey(t));
    }
}
