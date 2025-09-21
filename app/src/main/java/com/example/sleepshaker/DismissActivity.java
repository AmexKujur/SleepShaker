package com.example.sleepshaker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class DismissActivity extends AppCompatActivity implements SensorEventListener {

    // Shared components
    private SensorManager sensorManager;

    // Shake Challenge components
    private LinearLayout shakeChallengeLayout;
    private ProgressBar shakeProgressBar;
    private Sensor accelerometer;
    private float acceleration, currentAcceleration, lastAcceleration;
    private int shakeProgress = 0;
    private static final int SHAKE_SENSITIVITY = 15;
    private static final int SHAKE_PROGRESS_INCREMENT = 10;

    // Math Challenge components
    private LinearLayout mathChallengeLayout;
    private TextView mathQuestionText;
    private EditText mathAnswerInput;
    private Button mathSubmitButton;
    private int mathAnswer;

    // Light Sensor (LUX) Challenge components
    private LinearLayout luxChallengeLayout;
    private Sensor lightSensor;
    private static final float LUX_THRESHOLD = 500; // Threshold for a "bright" light

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss);

        // Initialize all layout containers
        shakeChallengeLayout = findViewById(R.id.shakeChallengeLayout);
        mathChallengeLayout = findViewById(R.id.mathChallengeLayout);
        luxChallengeLayout = findViewById(R.id.luxChallengeLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        String challengeType = getIntent().getStringExtra("CHALLENGE_TYPE");
        if (challengeType == null) {
            challengeType = "SHAKE"; // Default fallback
        }

        // Activate the correct challenge
        switch (challengeType) {
            case "MATH":
                setupMathChallenge();
                break;
            case "LUX_CHALLENGE":
                setupLuxChallenge();
                break;
            case "SHAKE":
            default:
                setupShakeChallenge();
                break;
        }
    }

    private void setupShakeChallenge() {
        shakeChallengeLayout.setVisibility(View.VISIBLE);
        shakeProgressBar = findViewById(R.id.shakeProgressBar);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void setupMathChallenge() {
        mathChallengeLayout.setVisibility(View.VISIBLE);
        mathQuestionText = findViewById(R.id.mathQuestionText);
        mathAnswerInput = findViewById(R.id.mathAnswerInput);
        mathSubmitButton = findViewById(R.id.mathSubmitButton);

        Random random = new Random();
        int num1 = random.nextInt(50) + 10; // Random number between 10 and 59
        int num2 = random.nextInt(50) + 10;
        mathAnswer = num1 + num2;

        mathQuestionText.setText(String.format("%d + %d = ?", num1, num2));

        mathSubmitButton.setOnClickListener(v -> {
            String answerString = mathAnswerInput.getText().toString();
            if (!answerString.isEmpty()) {
                int userAnswer = Integer.parseInt(answerString);
                if (userAnswer == mathAnswer) {
                    dismissAlarm();
                } else {
                    Toast.makeText(this, "Wrong answer, try again!", Toast.LENGTH_SHORT).show();
                    mathAnswerInput.setText("");
                }
            }
        });
    }

    private void setupLuxChallenge() {
        luxChallengeLayout.setVisibility(View.VISIBLE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(this, "Light sensor not available. Dismissing alarm.", Toast.LENGTH_LONG).show();
            dismissAlarm();
            return;
        }
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleShakeEvent(event);
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            handleLightEvent(event);
        }
    }

    private void handleShakeEvent(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        if (acceleration > SHAKE_SENSITIVITY) {
            shakeProgress += SHAKE_PROGRESS_INCREMENT;
            shakeProgressBar.setProgress(shakeProgress);
            if (shakeProgress >= 100) {
                dismissAlarm();
            }
        }
    }

    private void handleLightEvent(SensorEvent event) {
        float luxValue = event.values[0];
        if (luxValue > LUX_THRESHOLD) {
            dismissAlarm();
        }
    }

    private void dismissAlarm() {
        stopService(new Intent(this, RingtonePlayingService.class));
        Toast.makeText(this, "Alarm Dismissed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}