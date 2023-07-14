package com.hitsuthar.tracker.details;

import static com.hitsuthar.tracker.Common.emailIntent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hitsuthar.tracker.Common;
import com.hitsuthar.tracker.DetailsActivity;
import com.hitsuthar.tracker.data.PlayStore;

import com.hitsuthar.tracker.R;

import java.util.Objects;

import eu.faircode.netguard.Util;


public class ActionsFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_APP_ID = "app-id";
    private static final String ARG_APP_NAME = "app-name";
    private final String TAG = ActionsFragment.class.getSimpleName();
    private String appId;
    private String appName;

    public ActionsFragment() {
    }

    public static ActionsFragment newInstance(String appId, String appName) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_APP_ID, appId);
        args.putString(ARG_APP_NAME, appName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actions, container, false);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull final View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Bundle arguments = getArguments();
        assert arguments != null;
        appId = arguments.getString(ARG_APP_ID);
        appName = arguments.getString(ARG_APP_NAME);

        v.findViewById(R.id.btnTrackers).setOnClickListener(this);
        v.findViewById(R.id.btnAdSettings).setOnClickListener(this);
        v.findViewById(R.id.btnReqData).setOnClickListener(this);
        v.findViewById(R.id.btnReqDeletion).setOnClickListener(this);
        v.findViewById(R.id.btnContactDev).setOnClickListener(this);
        v.findViewById(R.id.btnContactGoogle).setOnClickListener(this);
        v.findViewById(R.id.btnContactOfficials).setOnClickListener(this);

        if (!Common.hasAdSettings(getContext()))
            v.findViewById(R.id.adsettings_card).setVisibility(View.GONE);

        if (Util.isPlayStoreInstall(getContext())) {
            v.findViewById(R.id.tracker_card).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnTrackers) {
            Activity a = getActivity();
            if (a == null)
                return;
            TabLayout tabs = a.findViewById(R.id.tabs);
            Objects.requireNonNull(tabs.getTabAt(0)).select();
        } else if (id == R.id.btnAdSettings) {
            if (Common.hasAdSettings(getContext())) {
                startActivity(Common.adSettings());
            } else {
                View vx = getView();
                if (vx != null)
                    Snackbar.make(vx, R.string.play_services_required, Snackbar.LENGTH_LONG).show();
            }
        } else if (id == R.id.btnReqData || id == R.id.btnReqDeletion || id == R.id.btnContactDev) {
            if (DetailsActivity.app != null && DetailsActivity.app.developerMail != null) {
                String mail = DetailsActivity.app.developerMail;
                contactDeveloper(v, mail);
                return;
            }

            if (Util.isFDroidInstall()) {
                contactDeveloper(v, null);
                return;
            }

            Context c = getContext();
            if (c == null)
                return;
            AlertDialog.Builder builder = new AlertDialog.Builder(c)
                    .setTitle(R.string.external_servers)
                    .setMessage(R.string.confirm_google_info)
                    .setPositiveButton(R.string.yes, (dialog, id2) -> {
                        new Thread(() -> {
                            DetailsActivity.app = PlayStore.getInfo(appId);

                            Activity a = getActivity();
                            if (a == null)
                                return;

                            a.runOnUiThread(() -> {
                                if (isAdded()) {
                                    String mail = null;

                                    if (DetailsActivity.app != null && DetailsActivity.app.developerMail != null)
                                        mail = DetailsActivity.app.developerMail;

                                    contactDeveloper(v, mail);
                                }
                            });
                        }).start();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.no, (dialog, id2) -> {
                        contactDeveloper(v, null);
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false); // avoid back button
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else if (id == R.id.btnContactGoogle) {
            sendEmail(getString(R.string.google_dpo_mail), null, null);
        } else if (id == R.id.btnContactOfficials) {
            Intent browserIntent = Common.browse(getString(R.string.dpas_overview_url));
            startActivity(browserIntent);
        }
    }

    private void contactDeveloper(View v, String mail) {
        String subject = null, body = null;
        if (v.getId() == R.id.btnReqData) {
            subject = getString(R.string.subject_request_data);
            body = getString(R.string.body_request_data, appName, appId);
        }

        if (v.getId() == R.id.btnReqDeletion) {
            subject = getString(R.string.subject_request_data);
            body = getString(R.string.body_delete_data, appName, appId);
        }

        sendEmail(mail, subject, body);
    }


    public void sendEmail(@Nullable String email, String subject, String body) {
        Intent i = emailIntent(email, subject, body);
        try {
            startActivity(Intent.createChooser(i, "Send email via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            View v = getView();
            if (v != null)
                Snackbar.make(v, R.string.no_mail_service, Snackbar.LENGTH_LONG).show();
        }
    }
}
