package com.hitsuthar.tracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class Common {
    @Nullable
    public static String fetch(String url) {
        try {
            StringBuilder html = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setConnectTimeout(5000);
            BufferedReader in;
            if ("gzip".equals(conn.getContentEncoding()))
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
            else
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;
            while ((str = in.readLine()) != null)
                html.append(str);
            in.close();
            return html.toString();
        } catch (IOException | OutOfMemoryError e) {
            return null;
        }
    }
    public static Intent adSettings() {
        Intent intent = new Intent();
        return intent.setComponent(new ComponentName("com.google.android.gms", "com.google.android.gms.ads.settings.AdsSettingsActivity"));
    }
    public static Intent browse(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
    public static boolean hasAdSettings(Context c) {
        return isCallable(c, adSettings());
    }

    public static Intent emailIntent(@Nullable String email, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        if (email != null) {
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        }
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        return i;
    }

    public static boolean isCallable(Context c, Intent intent) {
        List<ResolveInfo> list = c.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    public static String getAppName(PackageManager pm, int uid) {
        if (uid == 0)
            return "System";

        String[] packages = pm.getPackagesForUid(uid);
        if (packages != null && packages.length > 0)
            return packages[0];

        return "Unknown";
    }
    public static Intent getLaunchIntent(Activity activity, String appId) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(appId);
        return intent == null ||
                intent.resolveActivity(activity.getPackageManager()) == null ? null : intent;
    }
    @Nullable
    public static Snackbar getSnackbar(Activity activity, int msg) {
        View v = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if (v == null)
            return null;
        Snackbar s = Snackbar.make(v,
                msg,
                Snackbar.LENGTH_LONG);
        s.setActionTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        return s;
    }
    public static int dayOfYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
    static Set<String> intToStringSet(Set<Integer> ints) {
        Set<String> strings = new HashSet<>();

        for (Integer _int : ints) {
            strings.add(String.valueOf(_int));
        }
        return strings;
    }
}
