package com.hitsuthar.tracker.details;

import static com.hitsuthar.tracker.data.TrackerList.findTracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;

import com.caverock.androidsvg.RenderOptions;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

import com.hitsuthar.tracker.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Map;

import eu.faircode.netguard.DatabaseHelper;

public class CountriesFragment extends Fragment {
    private static final String ARG_APP_UID = "app-uid";
    private final String TAG = CountriesFragment.class.getSimpleName();
    private int mAppUid;

    public CountriesFragment() {
    }

    public static CountriesFragment newInstance(int uid) {
        CountriesFragment fragment = new CountriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_APP_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_countries, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public synchronized Map<String, Integer> getHostCountriesCount(int uid) {
        Map<String, Integer> countryToCount = new ArrayMap<>();
        Context context = getContext();
        if (context == null)
            return countryToCount;
        DatabaseHelper dh = DatabaseHelper.getInstance(getContext());
        try (Cursor cursor = dh.getHosts(uid)) {
            InputStream database = context.getAssets().open("GeoLite2-Country.mmdb");
            DatabaseReader reader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();

            if (cursor.moveToFirst()) {
                do {
                    String host = cursor.getString(cursor.getColumnIndexOrThrow("daddr"));
                    if (findTracker(host) == null)
                        continue;
                    InetAddress ipAddress = InetAddress.getByName(host);
                    CountryResponse response = reader.country(ipAddress);
                    Country country = response.getCountry();
                    String code = country.getIsoCode();
                    if (code == null)
                        continue;
                    String countryNames = String.valueOf(country.getNames());
                    Integer count = countryToCount.get(code);
                    if (count == null) {
                        countryToCount.put(code, 1);
                    } else {
                        countryToCount.put(code, count + 1);
                    }
                } while (cursor.moveToNext());
            }
        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
        }
        return countryToCount;
    }
    @Override
    public void onViewCreated(@NonNull final View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        Bundle arguments = getArguments();
        assert arguments != null;
        mAppUid = arguments.getInt(ARG_APP_UID);
        ProgressBar pbLoading = v.findViewById(R.id.pbLoading);
        ImageView mv = v.findViewById(R.id.svgView);
        TextView txtFailure = v.findViewById(R.id.txtFailure);
        TextView countryNames = v.findViewById(R.id.countryName);
        new Thread(() -> {
            showCountriesMap(pbLoading, mv, txtFailure, countryNames);
        }).start();
    }


    private void showCountriesMap(ProgressBar pbLoading, ImageView mv, TextView txtFailure, TextView countryNames) {
        try {
            SVG svg = SVG.getFromAsset(requireContext().getAssets(), "world.svg");
            Map<String, Integer> hostCountriesCount = getHostCountriesCount(mAppUid);
            final RenderOptions renderOptions = new RenderOptions();
            String countries = TextUtils.join(",#", hostCountriesCount.keySet());
            renderOptions.css(String.format("#%s { fill: #F2B8B5; }", countries.toUpperCase()));
            mv.post(() -> {
                Picture picture = svg.renderToPicture(renderOptions);
                mv.setImageDrawable(new PictureDrawable(picture));
                pbLoading.setVisibility(View.GONE);
                countryNames.setText(TextUtils.join(", ", hostCountriesCount.keySet()));
                countryNames.setVisibility(View.VISIBLE);

            });
        } catch (IllegalStateException | IOException | SVGParseException e) {
            e.printStackTrace();
            mv.post(() -> {
                mv.setVisibility(View.GONE);
                txtFailure.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
            });
        }
    }
}
