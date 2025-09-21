package com.example.sleepshaker; // Use your actual package name

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetAlarmActivity extends AppCompatActivity {

    // Nested static class for dismiss options
    public static class DismissOption {
        public final String label;
        public final int iconResId;
        public final String typeIdentifier;

        public DismissOption(String label, int iconResId, String typeIdentifier) {
            this.label = label;
            this.iconResId = iconResId;
            this.typeIdentifier = typeIdentifier;
        }
    }

    // Private fields for the activity
    private SetAlarmViewModel viewModel;
    private NumberPicker hourPicker, minutePicker;
    private RadioGroup amPmGroup;
    private SwitchMaterial dailySwitch;
    private List<Chip> dayChips;
    private boolean isDailySwitchUpdatingChips = false;
    private boolean isChipUpdatingDailySwitch = false;
    private RadioGroup dismissOptionsRadioGroup;
    private String selectedDismissOptionType = "SHAKE";
    private final List<DismissOption> dismissOptionsList = Arrays.asList(
            new DismissOption("Shake Phone", R.drawable.ic_vibration, "SHAKE"),
            new DismissOption("Math Quiz", android.R.drawable.ic_dialog_info, "MATH"),
            new DismissOption("Turn On Lights", R.drawable.ic_lightbulb, "LUX_CHALLENGE")
    );
    private AlarmDao alarmDao;
    private MaterialButton saveAlarmButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        // Initialize ViewModel and DAO
        AlarmScheduler alarmScheduler = new AlarmScheduler(this);
        SetAlarmViewModelFactory factory = new SetAlarmViewModelFactory(alarmScheduler);
        viewModel = new ViewModelProvider(this, factory).get(SetAlarmViewModel.class);
        alarmDao = AppDatabase.getInstance(getApplicationContext()).alarmDao();

        initializeViews();

        // Setup UI components and listeners
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        setupTimePickers();
        setupDaySelectionLogic();
        setupDismissOptions();
        saveAlarmButton.setOnClickListener(v -> saveAlarm());
    }

    private void initializeViews() {
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmGroup = findViewById(R.id.amPmGroup);
        dailySwitch = findViewById(R.id.dailySwitch);
        dismissOptionsRadioGroup = findViewById(R.id.dismissOptionsRadioGroup);
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

    private void setupDismissOptions() {
        LayoutInflater inflater = LayoutInflater.from(this);
        dismissOptionsRadioGroup.removeAllViews();
        int firstRadioButtonId = -1;

        for (int i = 0; i < dismissOptionsList.size(); i++) {
            DismissOption option = dismissOptionsList.get(i);
            LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.dismiss_option_item, dismissOptionsRadioGroup, false);

            ImageView iconView = itemView.findViewById(R.id.optionIcon);
            TextView labelView = itemView.findViewById(R.id.optionLabel);
            RadioButton radioButton = itemView.findViewById(R.id.optionRadioButton);

            iconView.setImageResource(option.iconResId);
            labelView.setText(option.label);
            radioButton.setId(View.generateViewId());
            radioButton.setTag(option.typeIdentifier);

            if (i == 0) {
                firstRadioButtonId = radioButton.getId();
                selectedDismissOptionType = option.typeIdentifier;
            }
            itemView.setOnClickListener(v -> {
                if (!radioButton.isChecked()) {
                    dismissOptionsRadioGroup.check(radioButton.getId());
                }
            });
            dismissOptionsRadioGroup.addView(itemView);
        }

        if (firstRadioButtonId != -1) {
            dismissOptionsRadioGroup.check(firstRadioButtonId);
        }

        dismissOptionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            selectedDismissOptionType = (checkedRadioButton != null) ? (String) checkedRadioButton.getTag() : "SHAKE";
            Log.d("SetAlarmActivity", "Dismiss option selected: " + selectedDismissOptionType);
        });
    }

    private void saveAlarm() {
        Calendar baseCalendar = Calendar.getInstance();
        int selectedHour12 = hourPicker.getValue();
        int selectedMinute = minutePicker.getValue();
        boolean isPm = amPmGroup.getCheckedRadioButtonId() == R.id.pmButton;

        int hour24 = selectedHour12;
        if (isPm && selectedHour12 < 12) {
            hour24 = selectedHour12 + 12;
        } else if (!isPm && selectedHour12 == 12) { // 12 AM is 00:00
            hour24 = 0;
        }

        baseCalendar.set(Calendar.HOUR_OF_DAY, hour24);
        baseCalendar.set(Calendar.MINUTE, selectedMinute);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);

        Set<Integer> selectedDays = new HashSet<>();
        if (dayChips.get(0).isChecked()) selectedDays.add(Calendar.SUNDAY);
        if (dayChips.get(1).isChecked()) selectedDays.add(Calendar.MONDAY);
        if (dayChips.get(2).isChecked()) selectedDays.add(Calendar.TUESDAY);
        if (dayChips.get(3).isChecked()) selectedDays.add(Calendar.WEDNESDAY);
        if (dayChips.get(4).isChecked()) selectedDays.add(Calendar.THURSDAY);
        if (dayChips.get(5).isChecked()) selectedDays.add(Calendar.FRIDAY);
        if (dayChips.get(6).isChecked()) selectedDays.add(Calendar.SATURDAY);

        Set<Integer> finalRepeatDays = selectedDays.isEmpty() ? null : selectedDays;
        Calendar alarmTimeCalendar = calculateNextAlarmTime((Calendar) baseCalendar.clone(), finalRepeatDays);

        String message = String.format(Locale.getDefault(), "Alarm for %02d:%02d %s", selectedHour12, selectedMinute, isPm ? "PM" : "AM");

        AlarmItem alarmItem = new AlarmItem(
                alarmTimeCalendar.getTimeInMillis(),
                hour24,
                selectedMinute,
                message,
                true,
                finalRepeatDays,
                selectedDismissOptionType
        );

        executor.execute(() -> {
            long newId = alarmDao.insertAndGetId(alarmItem);
            AlarmItem scheduledAlarmItem = new AlarmItem(alarmItem, (int) newId);
            Log.d("SetAlarmActivity", "Saving AlarmItem with new ID: " + scheduledAlarmItem.id);
            viewModel.schedule(scheduledAlarmItem);
            runOnUiThread(this::finish);
        });
    }

    private Calendar calculateNextAlarmTime(Calendar calendar, Set<Integer> targetDays) {
        long now = System.currentTimeMillis();

        if (targetDays == null || targetDays.isEmpty()) {
            if (calendar.getTimeInMillis() < now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar;
        }

        while (true) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (targetDays.contains(dayOfWeek) && calendar.getTimeInMillis() >= now) {
                return calendar;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor to prevent resource leaks
        if (executor != null) {
            executor.shutdown();
        }
    }
}