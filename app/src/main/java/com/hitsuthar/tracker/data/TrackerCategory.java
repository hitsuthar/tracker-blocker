package com.hitsuthar.tracker.data;

import android.content.Context;

import com.hitsuthar.tracker.R;

import java.util.ArrayList;
import java.util.List;

public class TrackerCategory {
    public static final String UNCATEGORISED = "Uncategorised";
    public String category;
    public Long lastSeen;
    private boolean uncertain = false;
    private List<Tracker> children;

    TrackerCategory(String category, long lastSeen) {
        this.category = category;
        this.lastSeen = lastSeen;
    }

    public boolean isUncertain() {
        return uncertain;
    }

    public void setUncertain(boolean uncertain) {
        this.uncertain = uncertain;
    }

    public String getDisplayName(Context c) {
        switch (category) {
            case "Advertising":
                return c.getString(R.string.tracker_advertising);
            case "Analytics":
                return c.getString(R.string.tracker_analytics);
            case "Content":
                return c.getString(R.string.tracker_content);
            case "Cryptomining":
                return c.getString(R.string.tracker_cryptomining);
            case "FingerprintingGeneral":
            case "FingerprintingInvasive":
                return c.getString(R.string.tracker_fingerprinting);
            case "Social":
                return c.getString(R.string.tracker_social);
            case "Email":
            case "EmailStrict":
                return c.getString(R.string.tracker_email);
            case UNCATEGORISED:
            default:
                return c.getString(R.string.tracker_uncategorised);
        }
    }

    public List<Tracker> getChildren() {
        if (this.children == null)
            this.children = new ArrayList<>();

        return this.children;
    }

    public String getCategoryName() {
        return category;
    }
}
