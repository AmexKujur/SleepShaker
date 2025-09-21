package com.example.sleepshaker;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton; // Changed to MaterialButton
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private AlarmAdapter alarmAdapter;
    private RecyclerView alarmsRecyclerView;
    private MaterialButton setAlarmButton; // Changed to MaterialButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel using the factory
        MainViewModelFactory factory = new MainViewModelFactory((Application) getApplicationContext());
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        // Find views (ensure your IDs match your activity_main.xml)
        alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);
        setAlarmButton = findViewById(R.id.setAlarmButton);

        // Set up RecyclerView
        setupRecyclerView();

        // Observe alarms from ViewModel
        mainViewModel.getAllAlarms().observe(this, new Observer<List<AlarmItem>>() {
            @Override
            public void onChanged(List<AlarmItem> alarms) {
                // ListAdapter will handle the animations and updates
                alarmAdapter.submitList(alarms);
            }
        });

        // Set listener for the "Set Alarm" button
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetAlarmActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        // Initialize adapter and define listener behavior
        alarmAdapter = new AlarmAdapter(
                alarmItem -> mainViewModel.updateAlarm(alarmItem), // Lambda for onToggle
                alarmItem -> mainViewModel.deleteAlarm(alarmItem)  // Lambda for onDelete
        );

        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmsRecyclerView.setAdapter(alarmAdapter);
    }
}