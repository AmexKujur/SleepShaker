package com.example.sleepshaker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.sleepshaker.viewmodels.SetAlarmViewModel;
import com.example.sleepshaker.viewmodels.SetAlarmViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetAlarmActivity extends AppCompatActivity {

    // --- UPDATED: Views to match the new layout ---
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private RadioGroup amPmGroup;
    private TimePicker timePicker;
    private SwitchMaterial dailySwitch;
    private ChipGroup dayChipGroup;
    private RadioGroup dismissOptionsRadioGroup;
    private MaterialButton saveAlarmButton;
    private Toolbar toolbar;

    private SetAlarmViewModel viewModel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        AlarmRepository repository = new AlarmRepository(getApplication());
        AlarmScheduler scheduler = new AlarmScheduler(this);
        SetAlarmViewModelFactory factory = new SetAlarmViewModelFactory(repository, scheduler);
        viewModel = new ViewModelProvider(this, factory).get(SetAlarmViewModel.class);

        initializeViews();
        setupDaySelectionLogic();
        saveAlarmButton.setOnClickListener(v -> saveAlarm());


        // --- UPDATED: Toolbar back button logic ---
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        // --- UPDATED: Finding the new views by their ID ---
        toolbar = findViewById(R.id.toolbar);
        //timePicker = findViewById(R.id.timePicker);
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmGroup = findViewById(R.id.amPmGroup);
        dailySwitch = findViewById(R.id.dailySwitch);
        dayChipGroup = findViewById(R.id.dayChipGroup);
        dismissOptionsRadioGroup = findViewById(R.id.dismissOptionsRadioGroup);
        saveAlarmButton = findViewById(R.id.saveAlarmButton);
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setFormatter(i -> String.format("%02d", i));
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(i -> String.format("%02d", i));


    }

    private void setupDaySelectionLogic() {
        final boolean[] isUpdating = {false};

        // --- UPDATED: Using the 'dailySwitch' variable ---
        dailySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdating[0]) {
                isUpdating[0] = true;
                for (int i = 0; i < dayChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) dayChipGroup.getChildAt(i);
                    chip.setChecked(isChecked);
                }
                isUpdating[0] = false;
            }
        });

        dayChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!isUpdating[0]) {
                isUpdating[0] = true;
                boolean allChecked = group.getCheckedChipIds().size() == 7;
                dailySwitch.setChecked(allChecked);
                isUpdating[0] = false;
            }
        });
    }

    private void saveAlarm() {
        // --- UPDATED: Get time from the new TimePicker ---
        //int hour = timePicker.getHour(); // 24-hour format
        int hour12 = hourPicker.getValue();
        int minute = minutePicker.getValue();
        boolean isPm = amPmGroup.getCheckedRadioButtonId() == R.id.pmButton;
        int hour24;

        if (hour12 == 12) {
            hour24 = isPm ? 12 : 0; // 12 PM is 12, 12 AM is 0
        } else {
            hour24 = isPm ? hour12 + 12 : hour12; // 1 PM is 13, etc.
        }

        Set<Integer> selectedDays = new HashSet<>();
        for (int id : dayChipGroup.getCheckedChipIds()) {
            Chip chip = dayChipGroup.findViewById(id);
            int dayIndex = dayChipGroup.indexOfChild(chip);
            selectedDays.add(dayIndex + 1); // Calendar.SUNDAY = 1, etc.
        }

        String selectedChallenge;
        int selectedId = dismissOptionsRadioGroup.getCheckedRadioButtonId();
        // --- UPDATED: IDs match the new layout ---
        if (selectedId == R.id.mathOption) {
            selectedChallenge = "MATH";
        } else if (selectedId == R.id.lightOption) {
            selectedChallenge = "LUX_CHALLENGE";
        }else if(selectedId == R.id.stepOption){
            selectedChallenge = "STEP";
        }
        else {
            selectedChallenge = "SHAKE";
        }

        AlarmItem alarmItem = new AlarmItem();
        alarmItem.hour = hour24;
        alarmItem.minute = minute;
        alarmItem.isEnabled = true;
        alarmItem.repeatDays = selectedDays;
        alarmItem.dismissMethod = selectedChallenge;
        alarmItem.message = ""; // Label input was removed from layout

        boolean isRecurring = !selectedDays.isEmpty();
        Calendar alarmTimeCalendar = calculateNextAlarmTime(hour24, minute, isRecurring, selectedDays);
        alarmItem.timeInMillis = alarmTimeCalendar.getTimeInMillis();
        Log.d("AlarmDebug", "Scheduling alarm for: " + alarmTimeCalendar.getTime().toString());
        executor.execute(() -> {
            try {
                long newId = viewModel.insert(alarmItem);
                alarmItem.id = (int) newId;

                if (alarmItem.isEnabled) {
                    viewModel.schedule(alarmItem);
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(SetAlarmActivity.this, "Alarm saved", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                Log.e("SetAlarmActivity", "Error while saving alarm!", e);
            }


        });
    }

    private Calendar calculateNextAlarmTime(int hour, int minute, boolean isRecurring, Set<Integer> selectedDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (!isRecurring) {
            return calendar;
        }

        while (!selectedDays.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar;
    }

    // No need for onDestroy if you are just using a single thread executor like this,
    // as it will be garbage collected with the Activity.
}