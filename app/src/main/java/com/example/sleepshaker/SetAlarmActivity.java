package com.example.sleepshaker.viewmodels;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sleepshaker.viewmodels.SetAlarmViewModel;
import com.example.sleepshaker.viewmodels.SetAlarmViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetAlarmActivity extends AppCompatActivity {

    // Views from your activity_set_alarm.xml
    private NumberPicker hourPicker, minutePicker;
    private RadioGroup amPmGroup;
    private SwitchMaterial dailySwitch;
    private ChipGroup dayChipGroup;
    private List<Chip> dayChips;
    private MaterialButton saveAlarmButton;

    private SetAlarmViewModel viewModel;
    private boolean isDailySwitchUpdatingChips = false;
    private boolean isChipUpdatingDailySwitch = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        // Initialize ViewModel
        AlarmRepository repository = new AlarmRepository(getApplication());
        AlarmScheduler scheduler = new AlarmScheduler(this);
        SetAlarmViewModelFactory factory = new SetAlarmViewModelFactory(repository, scheduler);
        viewModel = new ViewModelProvider(this, factory).get(SetAlarmViewModel.class);

        initializeViews();

        // Setup UI components and listeners
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        setupTimePickers();
        setupDaySelectionLogic();
        saveAlarmButton.setOnClickListener(v -> saveAlarm());
    }

    private void initializeViews() {
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmGroup = findViewById(R.id.amPmGroup);
        dailySwitch = findViewById(R.id.dailySwitch);
        dayChipGroup = findViewById(R.id.dayChipGroup);
        saveAlarmButton = findViewById(R.id.saveAlarmButton);
        dayChips = Arrays.asList(
                findViewById(R.id.chipSunday), findViewById(R.id.chipMonday),
                findViewById(R.id.chipTuesday), findViewById(R.id.chipWednesday),
                findViewById(R.id.chipThursday), findViewById(R.id.chipFriday),
                findViewById(R.id.chipSaturday)
        );
    }

    private void setupTimePickers() {
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));

        Calendar calendar = Calendar.getInstance();
        int currentHour24 = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        int currentHour12 = (currentHour24 == 0 || currentHour24 == 12) ? 12 : currentHour24 % 12;
        hourPicker.setValue(currentHour12);
        minutePicker.setValue(currentMinute);

        if (currentHour24 < 12) {
            amPmGroup.check(R.id.amButton);
        } else {
            amPmGroup.check(R.id.pmButton);
        }
    }

    private void setupDaySelectionLogic() {
        dailySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isDailySwitchUpdatingChips) return;
            isChipUpdatingDailySwitch = true;
            for (Chip chip : dayChips) {
                chip.setChecked(isChecked);
            }
            isChipUpdatingDailySwitch = false;
        });

        for (Chip chip : dayChips) {
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChipUpdatingDailySwitch) return;
                isDailySwitchUpdatingChips = true;
                boolean allChecked = true;
                for (Chip innerChip : dayChips) {
                    if (!innerChip.isChecked()) {
                        allChecked = false;
                        break;
                    }
                }
                dailySwitch.setChecked(allChecked);
                isDailySwitchUpdatingChips = false;
            });
        }
    }

    private void saveAlarm() {
        // Reads values from the NumberPickers
        int hour12 = hourPicker.getValue();
        int minute = minutePicker.getValue();
        boolean isPm = amPmGroup.getCheckedRadioButtonId() == R.id.pmButton;

        int hour24 = hour12;
        if (isPm && hour12 < 12) {
            hour24 = hour12 + 12;
        } else if (!isPm && hour12 == 12) { // 12 AM is 00:00
            hour24 = 0;
        }

        Set<Integer> selectedDays = new HashSet<>();
        // ... Logic to populate selectedDays from chips ...

        boolean isDailyChecked = dailySwitch.isChecked();
        boolean isRecurring = !selectedDays.isEmpty() || isDailyChecked;

        Calendar calendar = calculateNextAlarmTime(hour24, minute, isRecurring, isDailyChecked, selectedDays);

        AlarmItem alarmItem = new AlarmItem();
        alarmItem.timeInMillis = calendar.getTimeInMillis();
        alarmItem.hour = hour24;
        alarmItem.minute = minute;
        // ... Populate other fields for the AlarmItem ...

        executor.execute(() -> {
            long id = viewModel.insert(alarmItem);
            alarmItem.id = (int) id;
            viewModel.schedule(alarmItem);
        });

        Toast.makeText(this, "Alarm saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private Calendar calculateNextAlarmTime(int hour, int minute, boolean isRecurring, boolean isDaily, Set<Integer> selectedDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (!isRecurring) {
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar;
        }

        if (isDaily) {
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar;
        }

        for (int i = 0; i < 8; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (selectedDays.contains(dayOfWeek) && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                return calendar;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}