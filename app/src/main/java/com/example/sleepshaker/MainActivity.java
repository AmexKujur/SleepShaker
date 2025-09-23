package com.example.sleepshaker;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sleepshaker.viewmodels.MainViewModel;
import com.example.sleepshaker.viewmodels.MainViewModelFactory;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private AlarmAdapter alarmAdapter;
    private Button setAlarmButton;
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    ALARM_CHANNEL_ID,
                    "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Channel for the alarm service notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        // 1. Setup ViewModel FIRST
        AlarmRepository repository = new AlarmRepository(getApplication());
        MainViewModelFactory factory = new MainViewModelFactory(repository, getApplication());
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        // 2. Setup RecyclerView
        RecyclerView alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // The adapter can now safely use mainViewModel because it is initialized
        alarmAdapter = new AlarmAdapter(
                alarmItem -> mainViewModel.updateAlarm(alarmItem),
                alarmItem -> mainViewModel.deleteAlarm(alarmItem)
        );
        alarmsRecyclerView.setAdapter(alarmAdapter);

        // 3. ADDED: Request permission for exact alarms (fixes alarms not ringing)
        requestExactAlarmPermission();

        // 4. Observe LiveData
        mainViewModel.getAllAlarms().observe(this, alarms -> {
            if (alarms != null) {
                // ADDED: Log to check if data is being received
                Log.d("MainActivity", "Alarm list updated. Number of alarms: " + alarms.size());
                alarmAdapter.submitList(alarms);
            }
        });

        // 5. Setup Button
        setAlarmButton = findViewById(R.id.setAlarmButton);
        setAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetAlarmActivity.class);
            startActivity(intent);
        });
    }

    // ADDED: Method to handle the permission request
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    public static final String ALARM_CHANNEL_ID = "ALARM_SERVICE_CHANNEL";


}