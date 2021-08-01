package com.atompunkapps.ominousbeepingapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class SettingsActivity extends AppCompatActivity {
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
        private final Intent resultIntent = new Intent();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            setHasOptionsMenu(true);

            if(getActivity() == null) {
                return;
            }

            getActivity().setResult(
                    Activity.RESULT_OK,
                    resultIntent
            );

            //noinspection ConstantConditions
            findPreference("shake_count").setOnPreferenceChangeListener((preference, newValue) -> {
                resultIntent.putExtra("shake_count_changed", true);
                return true;
            });

            //noinspection ConstantConditions
            findPreference("beep_duration").setOnPreferenceChangeListener((preference, newValue) -> {
                resultIntent.putExtra("beep_duration_changed", true);
                return true;
            });

            //noinspection ConstantConditions
            findPreference("rating").setOnPreferenceClickListener(preference -> {
                final ReviewManager manager = ReviewManagerFactory.create(preference.getContext());
                manager.requestReviewFlow().addOnCompleteListener(task -> {
                    FragmentActivity activity = getActivity();
                    if(activity != null && task.isSuccessful()) {
                        manager.launchReviewFlow(activity, task.getResult());
                    }
                });
                return true;
            });

            //noinspection ConstantConditions
            findPreference("initial_delay").setOnPreferenceChangeListener((preference, newValue) -> {
                resultIntent.putExtra("initial_delay_changed", true);
                return true;
            });

            //noinspection ConstantConditions
            findPreference("initial_delta").setOnPreferenceChangeListener((preference, newValue) -> {
                resultIntent.putExtra("initial_delta_changed", true);
                return true;
            });

            //noinspection ConstantConditions
            findPreference("min_delay").setOnPreferenceChangeListener((preference, newValue) -> {
                resultIntent.putExtra("min_delay_changed", true);
                return true;
            });

//            findPreference("bg_beeping").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    resultIntent.putExtra("bg_beeping_changed", true);
//                    return true;
//                }
//            });

            //noinspection ConstantConditions
            findPreference("torch_blink").setOnPreferenceChangeListener((preference, newValue) ->  {
                resultIntent.putExtra("torch_blink_changed", true);
                return true;
            });

            //noinspection ConstantConditions
            findPreference("more_apps").setOnPreferenceClickListener(preference -> {
                try {
                    startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/developer?id=Atompunk+Tech+%26+Utilities")
                            )
                    );
                }
                catch (ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                return true;
            });
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            menu.add(0, 9, 0, "Default").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if(item.getItemId() == 9) {
                ((SeekBarPreference) findPreference("shake_count")).setValue(4);
                findPreference("shake_count").callChangeListener(4);

                ((SeekBarPreference) findPreference("beep_duration")).setValue(10);
                findPreference("beep_duration").callChangeListener(10);

                ((SwitchPreference) findPreference("bg_beeping")).setChecked(false);
                findPreference("bg_beeping").callChangeListener(false);

                ((SeekBarPreference) findPreference("initial_delay")).setValue(1000);
                findPreference("initial_delay").callChangeListener(1000);

                ((SeekBarPreference) findPreference("initial_delta")).setValue(150);
                findPreference("initial_delta").callChangeListener(150);

                ((SeekBarPreference) findPreference("min_delay")).setValue(25);
                findPreference("min_delay").callChangeListener(25);
            }
            return super.onOptionsItemSelected(item);
        }
    }
}