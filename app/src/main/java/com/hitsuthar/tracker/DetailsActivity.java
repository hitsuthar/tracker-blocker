package com.hitsuthar.tracker;

import static com.hitsuthar.tracker.data.InternetBlocklist.SHARED_PREFS_INTERNET_BLOCKLIST_APPS_KEY;
import static com.hitsuthar.tracker.data.TrackerBlocklist.PREF_BLOCKLIST;
import static com.hitsuthar.tracker.data.TrackerBlocklist.SHARED_PREFS_BLOCKLIST_APPS_KEY;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hitsuthar.tracker.data.InternetBlocklist;
import com.hitsuthar.tracker.data.PlayStore;
import com.hitsuthar.tracker.data.Tracker;
import com.hitsuthar.tracker.data.TrackerBlocklist;
import com.hitsuthar.tracker.data.TrackerList;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;


import eu.faircode.netguard.DatabaseHelper;

public class DetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_APP_PACKAGENAME = "INTENT_APP_PACKAGENAME";
    public static final String INTENT_EXTRA_APP_UID = "INTENT_APP_UID";
    public static final String INTENT_EXTRA_APP_NAME = "INTENT_APP_NAME";
    private static final int REQUEST_EXPORT = 10;
    public static PlayStore.AppInfo app = null;
    private final String TAG = DetailsActivity.class.getSimpleName();
    private boolean running = false;
    private Integer appUid;
    private String appPackageName;
    private DetailsStateAdapter detailsStateAdapter;

    public static void savePrefs(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_BLOCKLIST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        TrackerBlocklist b = TrackerBlocklist.getInstance(c);
        Set<Integer> trackerIntSet = b.getBlocklist();
        Set<String> trackerSet = Common.intToStringSet(trackerIntSet);
        editor.putStringSet(SHARED_PREFS_BLOCKLIST_APPS_KEY, trackerSet);
        for (Integer uid : trackerIntSet) {
            Set<String> subset = b.getSubset(uid);
            editor.putStringSet(SHARED_PREFS_BLOCKLIST_APPS_KEY + "_" + uid, subset);
        }
        InternetBlocklist internetBlocklist = InternetBlocklist.getInstance(c);
        Set<String> internetSet = Common.intToStringSet(internetBlocklist.getBlocklist());
        editor.putStringSet(SHARED_PREFS_INTERNET_BLOCKLIST_APPS_KEY, internetSet);

        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_EXPORT) {
            if (resultCode == RESULT_OK && data != null)
                new ExportDatabaseCSVTask(data).execute(); // export to CSV
        } else {
            Log.w(TAG, "Unknown activity result request=" + requestCode);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        running = true;

        Intent intent = getIntent();
        appPackageName = intent.getStringExtra(INTENT_EXTRA_APP_PACKAGENAME);
        appUid = intent.getIntExtra(INTENT_EXTRA_APP_UID, -1);
        String appName = intent.getStringExtra(INTENT_EXTRA_APP_NAME);

        detailsStateAdapter =
                new DetailsStateAdapter(
                        this,
                        appPackageName,
                        appName,
                        appUid);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(detailsStateAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager, (tab, position) ->
                tab.setText(getString(detailsStateAdapter.getPageTitle(position)))
        ).attach();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(getString(R.string.app_info));
        toolbar.setSubtitle(appName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();// Respond to the action bar's Up/Home button
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (itemId == R.id.action_export_csv) {
            startActivityForResult(getIntentCreateExport(), REQUEST_EXPORT);
            return true;
        } else if (itemId == R.id.action_clear) {
            DatabaseHelper dh = DatabaseHelper.getInstance(this);
            dh.clearAccess(appUid, false);
            detailsStateAdapter.updateTrackerLists();
            return true;
        } else if (itemId == R.id.action_launch) {
            Intent launch = Common.getLaunchIntent(this, appPackageName);
            if (launch != null)
                startActivity(launch);
            return true;
        } else if (itemId == R.id.action_uninstall) {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + appPackageName));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent getIntentCreateExport() {
        Intent intent;
        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, appPackageName + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date().getTime()) + ".csv");
        return intent;
    }

    @Override
    public void onPause() {
        super.onPause();
        savePrefs(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app = null;
        running = false;
    }

    class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(DetailsActivity.this);
        TrackerList trackerList;
        Intent data;

        public ExportDatabaseCSVTask(Intent data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.exporting));
            this.dialog.show();
            trackerList = TrackerList.getInstance(DetailsActivity.this);
        }

        protected Boolean doInBackground(final String... args) {
            Uri target = data.getData();
            if (data.hasExtra("org.openintents.extra.DIR_PATH"))
                target = Uri.parse(target + "/" + appPackageName + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date().getTime()) + ".csv");
            Log.i(TAG, "Writing URI=" + target);

            try (OutputStream out = getContentResolver().openOutputStream(target)) {
                csvExport(out);
                return true;
            } catch (Throwable ex) {
                Log.e(TAG, ex + "\n" + Log.getStackTraceString(ex));
                return false;
            }
        }

        private void csvExport(OutputStream out) throws IOException {
            try (CSVWriter csv = new CSVWriter(new OutputStreamWriter(out),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.RFC4180_LINE_END)) {

                try (Cursor data = trackerList.getAppInfo(appUid)) {
                    if (data == null) throw new IOException("Could not read hosts.");

                    List<String> columnNames = new ArrayList<>();
                    Collections.addAll(columnNames, data.getColumnNames());
                    columnNames.add("Tracker Name");
                    columnNames.add("Tracker Category");

                    csv.writeNext(columnNames.toArray(new String[0]));
                    while (data.moveToNext()) {
                        String[] row = new String[data.getColumnNames().length + 2];
                        for (int i = 0; i < data.getColumnNames().length; i++) {
                            row[i] = data.getString(i);
                        }

                        String hostname = data.getString(data.getColumnIndexOrThrow("daddr"));
                        Tracker tracker = TrackerList.findTracker(hostname);
                        if (tracker != null) {
                            row[data.getColumnNames().length] = tracker.getName();
                            row[data.getColumnNames().length + 1] = tracker.getCategory();
                        } else {
                            row[data.getColumnNames().length] = "";
                            row[data.getColumnNames().length + 1] = "";
                        }

                        csv.writeNext(row);
                    }
                }
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (running) {
                if (this.dialog.isShowing())
                    this.dialog.dismiss();

                if (success) {
                    View v = findViewById(R.id.view_pager);
                    Snackbar s = Snackbar.make(v, R.string.exported, Snackbar.LENGTH_LONG);
                    s.show();
                } else {
                    Toast.makeText(DetailsActivity.this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}