package com.hitsuthar.tracker.analysis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.hitsuthar.tracker.data.TrackerLibrary;
import com.hitsuthar.tracker.R;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.faircode.netguard.Rule;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DuplicateEntryNameException;
import lanchon.multidexlib2.DuplicateTypeException;
import lanchon.multidexlib2.EmptyMultiDexContainerException;
import lanchon.multidexlib2.MultiDexDetectedException;
import lanchon.multidexlib2.MultiDexIO;

public class TrackerLibraryAnalyser {
    private static final int EXODUS_DATABASE_VERSION = 423;
    private final Context mContext;
    public TrackerLibraryAnalyser(Context mContext) {
        this.mContext = mContext;
        int current = getPrefs().getInt("version", Integer.MIN_VALUE);
        if (current < EXODUS_DATABASE_VERSION)
            getPrefs().edit().clear().putInt("version", EXODUS_DATABASE_VERSION).apply();
    }

    @NonNull
    private static Set<TrackerLibrary> findTrackers(Context c, String apk) throws IOException, RuntimeException {
        DexFile dx = MultiDexIO.readDexFile(true, new File(apk), new BasicDexFileNamer(), null, null);
        String[] Sign = c.getResources().getStringArray(R.array.trackers);
        String[] Names = c.getResources().getStringArray(R.array.tname);
        String[] Web = c.getResources().getStringArray(R.array.tweb);
        Set<TrackerLibrary> trackers = new HashSet<>();
        for (ClassDef classDef : dx.getClasses()) {
            String className = classDef.getType();
            className = className.replace('/', '.');
            className = className.substring(1, className.length() - 1);

            if (className.length() > 8) {
                if (className.contains(".")) {
                    for (int Signz = 0; Signz < Sign.length; Signz++) {
                        if (className.contains(Sign[Signz])) {
                            if (Names[Signz].startsWith("µ?")) // exclude "good" trackers
                                continue;
                            trackers.add(new TrackerLibrary(Names[Signz], Web[Signz], Signz, Sign[Signz]));
                            break;
                        }
                    }
                }
            }
        }

        return trackers;
    }

    public String analyse(String mAppId) throws AnalysisException {
        String trackerString;

        try {
            Set<TrackerLibrary> trackers;
            PackageInfo pkg = mContext.getPackageManager().getPackageInfo(mAppId, 0);

            SharedPreferences prefs = getPrefs();
            int analysedCode = prefs.getInt("versioncode_" + mAppId, Integer.MIN_VALUE);

            if (pkg.versionCode > analysedCode) {
                String apk = pkg.applicationInfo.publicSourceDir;
                trackers = findTrackers(mContext, apk);

                final List<TrackerLibrary> sortedTrackers = new ArrayList<>(trackers);
                Collections.sort(sortedTrackers);

                if (sortedTrackers.size() > 0)
                    trackerString = "\n• " + TextUtils.join("\n• ", sortedTrackers);
                else
                    trackerString = mContext.getString(R.string.none);
                prefs.edit()
                        .putInt("versioncode_" + mAppId, pkg.versionCode)
                        .putString("trackers_" + mAppId, trackerString)
                        .apply();
            } else
                trackerString = prefs.getString("trackers_" + mAppId, null);
        } catch (Throwable e) {
            if (e instanceof EmptyMultiDexContainerException
                    || e instanceof MultiDexDetectedException
                    || e instanceof DuplicateTypeException
                    || e instanceof DuplicateEntryNameException
                    || e instanceof PackageManager.NameNotFoundException
                    || Rule.isSystem(mAppId, mContext))
                throw new AnalysisException(mContext.getString(R.string.tracking_detection_failed));
            else if (e instanceof OutOfMemoryError)
                throw new AnalysisException(mContext.getString(R.string.tracking_detection_failed_ram));
            else
                throw new AnalysisException(mContext.getString(R.string.tracking_detection_failed_report));
        }
        return trackerString;
    }
    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences("library_analysis", Context.MODE_PRIVATE);
    }
}
