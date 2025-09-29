package com.example.sleepshaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sleepshaker.customviews.LightSensorView;
import com.example.sleepshaker.customviews.ShakeProgressView;
import com.example.sleepshaker.customviews.StepCounterView;

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
    private LinearLayout stepChallengeLayout;
    //private TextView stepCounterTextView;
    private Sensor stepCounterSensor;
    private int initialSteps = -1; // -1 indicates we haven't received a value yet
    private static final int TARGET_STEPS = 20;
    private int currentAlarmId = -1;
    private String currentChallengeType;
    private String currentAlarmMessage;
    private ShakeProgressView customShakeProgressView;
    private LightSensorView customLightSensorView;
    private StepCounterView customStepCounterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss);
        currentAlarmId = getIntent().getIntExtra("ALARM_ITEM_ID", -1);
        currentChallengeType = getIntent().getStringExtra("CHALLENGE_TYPE");
        currentAlarmMessage = getIntent().getStringExtra("ALARM_MESSAGE");

        Log.d("DismissActivity", "Started for Alarm ID: " + currentAlarmId);
        // Initialize all layout containers
        shakeChallengeLayout = findViewById(R.id.shakeChallengeLayout);
        mathChallengeLayout = findViewById(R.id.mathChallengeLayout);
        luxChallengeLayout = findViewById(R.id.luxChallengeLayout);
        stepChallengeLayout = findViewById(R.id.stepChallengeLayout);
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
            case "STEP":
                setupStepChallenge();
                break;
            case "SHAKE":
            default:
                setupShakeChallenge();
                break;
        }
    }

    private void setupShakeChallenge() {
        shakeChallengeLayout.setVisibility(View.VISIBLE);
        //shakeProgressBar = findViewById(R.id.shakeProgressBar);
        customShakeProgressView = findViewById(R.id.customShakeProgressView);
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
        customLightSensorView = findViewById(R.id.customLightSensorView);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(this, "Light sensor not available. Dismissing alarm.", Toast.LENGTH_LONG).show();
            dismissAlarm();
            return;
        }
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void setupStepChallenge() {
        stepChallengeLayout.setVisibility(View.VISIBLE);
        //stepCounterTextView = findViewById(R.id.customStepCounterView);
        customStepCounterView = findViewById(R.id.customStepCounterView);
        customStepCounterView.setTargetSteps(TARGET_STEPS);
        //stepCounterTextView.setText("0 / " + TARGET_STEPS + " steps");
        if (customStepCounterView != null) {
            customStepCounterView.setTargetSteps(TARGET_STEPS);
        } else {
            Log.e("DismissActivity", "customStepCounterView not found in layout");
            Toast.makeText(this, "Step counter not available.", Toast.LENGTH_LONG).show();
            dismissAlarm();
            return;
        }

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounterSensor == null) {
            Toast.makeText(this, "Step sensor not available.", Toast.LENGTH_LONG).show();
            dismissAlarm();
            return;
        }
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepEvent(event);
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
            //shakeProgress += SHAKE_PROGRESS_INCREMENT;
           // shakeProgressBar.setProgress(shakeProgress);
            //if (shakeProgress >= 100) {
            //    dismissAlarm();
            shakeProgress += SHAKE_PROGRESS_INCREMENT;
            customShakeProgressView.setProgress(shakeProgress);
            customShakeProgressView.addShakeEffect(); // Add visual shake effect
            if (shakeProgress >= 100) {
                dismissAlarm();

            }
        }
    }

    private void handleLightEvent(SensorEvent event) {
        float luxValue = event.values[0];
        customLightSensorView.updateLux(luxValue);
        if (luxValue > LUX_THRESHOLD) {
            customLightSensorView.addSuccessAnimation();
            dismissAlarm();
        }
    }
        private void handleStepEvent(SensorEvent event) {
            int totalSteps = (int) event.values[0];

            // If this is the first event, record the initial step count
            if (initialSteps == -1) {
                initialSteps = totalSteps;
            }

            // Calculate steps taken since the alarm started
            int stepsTaken = totalSteps - initialSteps;

            // Update the UI
            if (stepsTaken >= 0) {
                customStepCounterView.updateSteps(stepsTaken);
            }

            // Check if the user has reached the target
            if (stepsTaken >= TARGET_STEPS) {
                dismissAlarm();
            }
        }

    private void dismissAlarm() {
        Log.d("DismissActivity", "Dismissing alarm ID: " + currentAlarmId);

        // Stop the specific ringtone service for this alarm
        Intent stopRingtoneIntent = new Intent(this, RingtonePlayingService.class);
        stopRingtoneIntent.putExtra("ALARM_ID", currentAlarmId);
        stopRingtoneIntent.setAction("STOP_ALARM");
        startService(stopRingtoneIntent);

        // Also try the old way as backup
        stopService(new Intent(this, RingtonePlayingService.class));

        Toast.makeText(this, "Alarm " + currentAlarmId + " Dismissed!", Toast.LENGTH_SHORT).show();
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
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please complete the challenge to dismiss the alarm", Toast.LENGTH_SHORT).show();
    }
}