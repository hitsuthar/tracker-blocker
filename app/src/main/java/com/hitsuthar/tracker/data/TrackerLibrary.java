package com.hitsuthar.tracker.data;

import androidx.annotation.NonNull;

import java.util.Objects;

public class TrackerLibrary implements Comparable<TrackerLibrary> {
    private final String name;
    private final String web;
    private final int id;
    private final String sign;

    public TrackerLibrary(@NonNull String name, String web, Integer id, String sign) {
        name = name.replaceAll("[°²?µ]", "").trim();
        this.name = name;
        this.web = web;
        this.id = id;
        this.sign = sign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackerLibrary tracker = (TrackerLibrary) o;
        return Objects.equals(name, tracker.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public int compareTo(TrackerLibrary t) {
        return name.compareTo(t.name);
    }
}
