package com.atompunkapps.ominousbeepingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_CODE_SETTINGS = 9;

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.2F;
    private static final int SHAKE_MIN_DELAY = 300;
    private static final int SHAKE_TIMEOUT = 2000;

    private static final int DEFAULT_START_DELAY = 1000;
    private static final int DEFAULT_START_DELTA = 150;
    private static final int DEFAULT_MIN_DELAY = 25;

    private Sensor sensor;
    private SensorManager sensorManager;

    private MediaPlayer mp;
    private long stopTime;

    private ImageView imageView;
    private final int[] beeps = new int[] {
            R.drawable.circle_1,
            R.drawable.circle_2,
            R.drawable.circle_3,
            R.drawable.circle_4
    };
    private int beepIndex = 0;

    private long initialDelay;
    private float initialDelayDelta;
    private int minDelay;

    private long shakeTime;
    private int count;
    private int shakeCount;
    private int beepDuration;
    private long delay;
    private float delayDelta;
    private boolean forceStop = false;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.beep_image);

        findViewById(R.id.options_image).setOnClickListener(v -> startActivityForResult(
                new Intent(MainActivity.this, SettingsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                REQUEST_CODE_SETTINGS
        ));

        AudienceNetworkAds.initialize(this);

        adView = new AdView(this, SafeStore.PLACEMENT_ID, AdSize.BANNER_HEIGHT_50);

        ((FrameLayout) findViewById(R.id.banner_container)).addView(adView);

        adView.loadAd();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        shakeCount = preferences.getInt("shake_count", 4);
        beepDuration = preferences.getInt("beep_duration", 10) * 1000;

        initialDelay = preferences.getInt("initial_delay", DEFAULT_START_DELAY);
        initialDelayDelta = preferences.getInt("initial_delta", DEFAULT_START_DELTA);
        minDelay = preferences.getInt("min_delay", DEFAULT_MIN_DELAY);

        delay = initialDelay;
        delayDelta = initialDelayDelta;

        mp = MediaPlayer.create(this, R.raw.beep6);
        mp.setOnCompletionListener(mp -> {
            if(System.currentTimeMillis() > stopTime || forceStop) {
                imageView.setVisibility(View.GONE);

                delay = initialDelay;
                delayDelta = initialDelayDelta;
                forceStop = false;
                return;
            }
            try {
                Thread.sleep(delay);
                delayDelta = delayDelta * 0.91f;
                delay -= Math.round(delayDelta);
                if(delay < minDelay) {
                    delay = minDelay;
                }

                beepIndex = (beepIndex + 1) % 4;
                imageView.setImageResource(beeps[beepIndex]);

                mp.start();
            }
            catch (InterruptedException ignored) { }
        });

        Toast.makeText(this, "Shake the phone " + shakeCount + " times.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        //  g-force = 1 when no movement
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();

            if (shakeTime + SHAKE_MIN_DELAY > now) {
                return;
            }

            //  Reset the count after timeout
            if (shakeTime + SHAKE_TIMEOUT < now) {
                count = 0;
            }

            shakeTime = now;
            count++;

            if(count >= shakeCount) {
                if(count == shakeCount) {
                    beepIndex = 0;
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(beeps[beepIndex]);
//                    mp.start(); // Move here?
                }

                stopTime = System.currentTimeMillis() + beepDuration;
                mp.start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_SETTINGS && resultCode == SettingsActivity.RESULT_PREFS_CHANGED && data != null) {
            if(data.hasExtra("shake_count_changed")) {
                shakeCount = PreferenceManager.getDefaultSharedPreferences(this).getInt("shake_count", 4);
            }
            if(data.hasExtra("beep_duration_changed")) {
                beepDuration = PreferenceManager.getDefaultSharedPreferences(this).getInt("beep_duration", 10) * 1000;
            }
            if(data.hasExtra("initial_delay_changed")) {
                initialDelay = PreferenceManager.getDefaultSharedPreferences(this).getInt("initial_delay", DEFAULT_START_DELAY);
                delay = initialDelay;
            }
            if(data.hasExtra("initial_delta_changed")) {
                initialDelayDelta = PreferenceManager.getDefaultSharedPreferences(this).getInt("initial_delta", DEFAULT_START_DELTA);
                delayDelta = initialDelayDelta;
            }
            if(data.hasExtra("min_delay_changed")) {
                minDelay = PreferenceManager.getDefaultSharedPreferences(this).getInt("min_delay", DEFAULT_MIN_DELAY);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bg_beeping", false)) {
            if(sensorManager != null) {
                sensorManager.unregisterListener(this);
            }

            forceStop = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bg_beeping", false)) {
            count = 0;
            forceStop = false;
        }

        if(sensorManager != null && sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}