package com.hitsuthar.tracker.data;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class Tracker {
    private final Set<String> hosts = new HashSet<>();
    public String name;
    public String category;
    public Long lastSeen;
    public String country;
    public Tracker(String name, String category, long lastSeen) {
        this.name = name;
        this.category = category;
        this.lastSeen = lastSeen;
    }
    public Tracker(String name, String category) {
        this.name = name;
        this.category = category;
    }
    @Override
    @NonNull
    public String toString() {
        if (this.name == null)
            return "NO_TRACKER";

        return getName();
    }
    public String getName() {
        if (name.equals("Alphabet"))
            return "Google";

        if (name.equals("Adobe Systems"))
            return "Adobe";

        return name;
    }
    public String getCategory() {
        if (category == null || category.equals("null")) return null;
        return category;
    }
    void addHost(String host) {
        this.hosts.add(host);
    }

    public Set<String> getHosts() {
        return hosts;
    }
}
