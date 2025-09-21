package com.example.sleepshaker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DismissActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration, currentAcceleration, lastAcceleration;
    private static final int SHAKE_THRESHOLD = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss); // Ensure you have this layout file

        String challengeType = getIntent().getStringExtra("CHALLENGE_TYPE");
        if (challengeType == null) {
            challengeType = "SHAKE"; // Default to shake if something goes wrong
        }

        TextView challengeInstructions = findViewById(R.id.challengeInstructions); // ID from your XML

        switch (challengeType) {
            case "SHAKE":
                challengeInstructions.setText("Shake your phone to dismiss the alarm!");
                setupShakeDetector();
                break;
            case "MATH":
                challengeInstructions.setText("Solve the math problem to dismiss!");
                // TODO: Implement Math challenge UI and logic
                // For now, we'll just let it be dismissed easily
                // dismissAlarm();
                break;
            case "LUX_CHALLENGE":
                challengeInstructions.setText("Turn on the lights to dismiss!");
                // TODO: Implement Light Sensor challenge logic
                break;
            default:
                // Default behavior if challenge type is unknown
                dismissAlarm();
                break;
        }
    }

    private void setupShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        if (acceleration > SHAKE_THRESHOLD) {
            dismissAlarm();
        }
    }

    private void dismissAlarm() {
        // Stop the ringtone
        stopService(new Intent(this, RingtonePlayingService.class));
        Toast.makeText(this, "Alarm Dismissed!", Toast.LENGTH_SHORT).show();

        // Unregister listener to save battery
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        finish(); // Close this activity
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}