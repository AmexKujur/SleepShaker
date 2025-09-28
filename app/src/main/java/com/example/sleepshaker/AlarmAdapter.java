package com.example.sleepshaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class AlarmAdapter extends ListAdapter<AlarmItem, AlarmAdapter.AlarmViewHolder> {

    private final OnToggleListener toggleListener;
    private final OnDeleteListener deleteListener;

    public AlarmAdapter(OnToggleListener toggleListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.toggleListener = toggleListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_alarm, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem currentAlarm = getItem(position);

        // Format time display with AM/PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, currentAlarm.hour);
        calendar.set(Calendar.MINUTE, currentAlarm.minute);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(calendar.getTime());
        holder.timeTextView.setText(formattedTime);

        // Set alarm label - always show with default format if message is empty
        if (currentAlarm.message != null && !currentAlarm.message.trim().isEmpty()) {
            holder.alarmLabelTextView.setText(currentAlarm.message);
        } else {
            // Show default label like "Alarm for 02:30 PM"
            holder.alarmLabelTextView.setText("Alarm for " + formattedTime);
        }
        holder.alarmLabelTextView.setVisibility(View.VISIBLE);

        // Set days display - always show
        String daysText = formatDays(currentAlarm.repeatDays);
        holder.alarmDaysTextView.setText(daysText);
        holder.alarmDaysTextView.setVisibility(View.VISIBLE);

        // Handle switch
        holder.alarmSwitch.setOnCheckedChangeListener(null);
        holder.alarmSwitch.setChecked(currentAlarm.isEnabled);

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (toggleListener != null) {
                currentAlarm.isEnabled = isChecked;
                toggleListener.onToggle(currentAlarm);
            }
        });

        // Handle delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(currentAlarm);
            }
        });
    }

    private String formatDays(Set<Integer> repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) {
            return "One time";
        }

        if (repeatDays.size() == 7) {
            return "Daily";
        }

        if (repeatDays.size() == 5 &&
                repeatDays.contains(Calendar.MONDAY) &&
                repeatDays.contains(Calendar.TUESDAY) &&
                repeatDays.contains(Calendar.WEDNESDAY) &&
                repeatDays.contains(Calendar.THURSDAY) &&
                repeatDays.contains(Calendar.FRIDAY)) {
            return "Weekdays";
        }

        if (repeatDays.size() == 2 &&
                repeatDays.contains(Calendar.SATURDAY) &&
                repeatDays.contains(Calendar.SUNDAY)) {
            return "Weekends";
        }

        // Show abbreviated day names
        String[] dayAbbrevs = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder days = new StringBuilder();

        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
            if (repeatDays.contains(day)) {
                if (days.length() > 0) {
                    days.append(", ");
                }
                days.append(dayAbbrevs[day - 1]);
            }
        }

        return days.toString();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        SwitchMaterial alarmSwitch;
        TextView alarmLabelTextView;
        TextView alarmDaysTextView;
        ImageView deleteButton;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            alarmLabelTextView = itemView.findViewById(R.id.alarmLabelTextView);
            alarmDaysTextView = itemView.findViewById(R.id.alarmDaysTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnToggleListener {
        void onToggle(AlarmItem alarmItem);
    }

    public interface OnDeleteListener {
        void onDelete(AlarmItem alarmItem);
    }

    private static final DiffUtil.ItemCallback<AlarmItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlarmItem oldItem, @NonNull AlarmItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull AlarmItem oldItem, @NonNull AlarmItem newItem) {
            return oldItem.hour == newItem.hour &&
                    oldItem.minute == newItem.minute &&
                    oldItem.isEnabled == newItem.isEnabled &&
                    (oldItem.message != null ? oldItem.message.equals(newItem.message) : newItem.message == null) &&
                    (oldItem.repeatDays != null ? oldItem.repeatDays.equals(newItem.repeatDays) : newItem.repeatDays == null);
        }
    };
}