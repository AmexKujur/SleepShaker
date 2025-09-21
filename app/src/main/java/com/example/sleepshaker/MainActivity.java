package com.example.sleepshaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // <-- Correct import
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sleepshaker.viewmodels.MainViewModel;
import com.example.sleepshaker.viewmodels.MainViewModelFactory;
import com.example.sleepshaker.viewmodels.SetAlarmActivity;


public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private AlarmAdapter alarmAdapter;
    private Button setAlarmButton; // <-- Correct type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup RecyclerView
        RecyclerView alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmAdapter = new AlarmAdapter(
                alarmItem -> mainViewModel.updateAlarm(alarmItem),
                alarmItem -> mainViewModel.deleteAlarm(alarmItem)
        );
        alarmsRecyclerView.setAdapter(alarmAdapter);

        // Setup ViewModel
        AlarmRepository repository = new AlarmRepository(getApplication());
        MainViewModelFactory factory = new MainViewModelFactory(repository, getApplication());
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        // Observe LiveData
        mainViewModel.getAllAlarms().observe(this, alarms -> {
            if (alarms != null) {
                alarmAdapter.submitList(alarms);
            }
        });

        // Setup Button
        setAlarmButton = findViewById(R.id.setAlarmButton);
        setAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetAlarmActivity.class);
            startActivity(intent);
        });
    }
}