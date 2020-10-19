package com.atompunkapps.ominousbeepingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

public class SettingsActivity extends AppCompatActivity {
    static int RESULT_PREFS_CHANGED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Intent resultIntent = new Intent();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            if(getActivity() == null) {
                return;
            }

            getActivity().setResult(
                    RESULT_PREFS_CHANGED,
                    resultIntent
            );

            findPreference("shake_count").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    resultIntent.putExtra("shake_count_changed", true);
                    return true;
                }
            });

            findPreference("beep_duration").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    resultIntent.putExtra("beep_duration_changed", true);
                    return true;
                }
            });

            findPreference("rating").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final ReviewManager manager = ReviewManagerFactory.create(preference.getContext());
                    manager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                        @Override
                        public void onComplete(@NonNull Task<ReviewInfo> task) {
                            FragmentActivity activity = getActivity();
                            if(activity != null && task.isSuccessful()) {
                                manager.launchReviewFlow(activity, task.getResult());
                            }
                        }
                    });
                    return true;
                }
            });

//            findPreference("bg_beeping").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    resultIntent.putExtra("bg_beeping_changed", true);
//                    return true;
//                }
//            });
        }
    }
}