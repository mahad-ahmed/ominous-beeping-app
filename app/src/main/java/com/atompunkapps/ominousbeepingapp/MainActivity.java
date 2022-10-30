package com.atompunkapps.ominousbeepingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.2F;
    private static final int SHAKE_MIN_DELAY = 300;
    private static final int SHAKE_TIMEOUT = 2000;

    private static final int DEFAULT_START_DELAY = 1000;
    private static final int DEFAULT_START_DELTA = 150;
    private static final int DEFAULT_MIN_DELAY = 25;

    private static final float G_SQUARED = (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

    private Sensor sensor;
    private SensorManager sensorManager;

    private CameraManager cameraManager = null;
    private String cameraId = null;

    private MediaPlayer mp;
    private long stopTime;

    private final ImageView[] imageViews = new ImageView[4];
//    private final int[] beeps = new int[] {
//            R.drawable.circle_1,
//            R.drawable.circle_2,
//            R.drawable.circle_3,
//            R.drawable.circle_4
//    };
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

    private Monetization monetization;

//    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monetization = new Monetization(this, findViewById(R.id.banner_container));

        imageViews[0] = findViewById(R.id.beep_image_1);
        imageViews[1] = findViewById(R.id.beep_image_2);
        imageViews[2] = findViewById(R.id.beep_image_3);
        imageViews[3] = findViewById(R.id.beep_image_4);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if(result.getData().hasExtra("shake_count_changed")) {
                            shakeCount = PreferenceManager.getDefaultSharedPreferences(this).getInt("shake_count", 4);
                        }
                        if(result.getData().hasExtra("beep_duration_changed")) {
                            beepDuration = PreferenceManager.getDefaultSharedPreferences(this).getInt("beep_duration", 10) * 1000;
                        }
                        if(result.getData().hasExtra("initial_delay_changed")) {
                            initialDelay = PreferenceManager.getDefaultSharedPreferences(this).getInt("initial_delay", DEFAULT_START_DELAY);
                            delay = initialDelay;
                        }
                        if(result.getData().hasExtra("initial_delta_changed")) {
                            initialDelayDelta = PreferenceManager.getDefaultSharedPreferences(this).getInt("initial_delta", DEFAULT_START_DELTA);
                            delayDelta = initialDelayDelta;
                        }
                        if(result.getData().hasExtra("min_delay_changed")) {
                            minDelay = PreferenceManager.getDefaultSharedPreferences(this).getInt("min_delay", DEFAULT_MIN_DELAY);
                        }
                        if(result.getData().hasExtra("torch_blink_changed")) {
                            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("torch_blink", true)) {
                                if(cameraId == null || cameraManager == null) {
                                    initCamera();
                                }
                            }
                            else {
                                // TODO: Check race condition
                                cameraId = null;
                                cameraManager = null;
                            }
                        }
                    }
                }
        );

        findViewById(R.id.options_image).setOnClickListener(v ->
                launcher.launch(new Intent(this, SettingsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP))
        );
//
//        AudienceNetworkAds.initialize(this);
//
//        adView = new AdView(this, SafeStore.PLACEMENT_ID, AdSize.BANNER_HEIGHT_50);
//
//        ((FrameLayout) findViewById(R.id.banner_container)).addView(adView);
//
//        adView.loadAd();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("torch_blink", true)) {
            initCamera();
        }

        shakeCount = preferences.getInt("shake_count", 4);
        beepDuration = preferences.getInt("beep_duration", 10) * 1000;

        initialDelay = preferences.getInt("initial_delay", DEFAULT_START_DELAY);
        initialDelayDelta = preferences.getInt("initial_delta", DEFAULT_START_DELTA);
        minDelay = preferences.getInt("min_delay", DEFAULT_MIN_DELAY);

        delay = initialDelay;
        delayDelta = initialDelayDelta;

        mp = MediaPlayer.create(this, R.raw.beep6);
        mp.setOnCompletionListener(mp -> {
            setFlash(false);
            if(System.currentTimeMillis() > stopTime || forceStop) {
                for(ImageView imageView : imageViews) {
                    imageView.setVisibility(View.INVISIBLE);
                }

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
                for(int i = 0; i < imageViews.length; i++) {
                    imageViews[i].setVisibility(i == beepIndex ? View.VISIBLE : View.INVISIBLE);
                }

                mp.start();
                setFlash(true);
            }
            catch (InterruptedException ignored) { }
        });

        Toast.makeText(this, "Shake the phone " + shakeCount + " times.", Toast.LENGTH_LONG).show();
    }

    private void initCamera() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIdList = cameraManager.getCameraIdList();
                if(cameraIdList.length > 0) {
                    cameraId = cameraIdList[0];
                }
//                mCameraManager.registerTorchCallback();
            }
            catch (CameraAccessException ignored) { }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO: This can be optimised by not having to divide 3 times
        float gX = event.values[0];
        float gY = event.values[1];
        float gZ = event.values[2];

        //  g-force = 1 when no movement
        double gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ) / G_SQUARED);

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
                    imageViews[0].setVisibility(View.VISIBLE);
//                    imageViews.setImageResource(beeps[beepIndex]);
//                    mp.start(); // Move here?
                }

                stopTime = System.currentTimeMillis() + beepDuration;
                mp.start();
                setFlash(true);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @SuppressLint("NewApi")  // mCameraManager should be null if API < 23 so just suppress I guess
    private void setFlash(boolean enabled) {
        if(cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, enabled);
            }
            catch (Exception ignored) { }
        }
    }


    @Override
    protected void onPause() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bg_beeping", false)) {
            if(sensorManager != null) {
                sensorManager.unregisterListener(this);
            }

            forceStop = true;
        }

        setFlash(false);

//        monetization.pauseAds();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bg_beeping", false)) {
            count = 0;
            forceStop = false;
        }

        if(sensorManager != null && sensor != null) {
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

//        monetization.resumeAds();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        monetization.destroyAds();
        super.onDestroy();
    }
}