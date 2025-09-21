package com.example.sleepshaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.material.switchmaterial.SwitchMaterial; // Changed import
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class AlarmAdapter extends ListAdapter<AlarmItem, AlarmAdapter.AlarmViewHolder> {

    private final OnToggleListener toggleListener;
    private final OnDeleteListener deleteListener;

    // Constructor
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
        holder.timeTextView.setText(String.format("%02d:%02d", currentAlarm.hour, currentAlarm.minute));

        holder.alarmSwitch.setOnCheckedChangeListener(null);
        holder.alarmSwitch.setChecked(currentAlarm.isEnabled);

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (toggleListener != null) {
                currentAlarm.isEnabled = isChecked; // Update the item's state
                toggleListener.onToggle(currentAlarm);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(currentAlarm);
            }
        });
    }

    // ViewHolder class
    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        SwitchMaterial alarmSwitch; // Changed type to SwitchMaterial
        ImageView deleteButton;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView); // Ensure IDs match list_item_alarm.xml
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // Listener Interfaces
    public interface OnToggleListener {
        void onToggle(AlarmItem alarmItem);
    }

    public interface OnDeleteListener {
        void onDelete(AlarmItem alarmItem);
    }

    // DiffUtil.ItemCallback to calculate list differences
    private static final DiffUtil.ItemCallback<AlarmItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlarmItem oldItem, @NonNull AlarmItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull AlarmItem oldItem, @NonNull AlarmItem newItem) {
            return oldItem.hour == newItem.hour &&
                    oldItem.minute == newItem.minute &&
                    oldItem.isEnabled == newItem.isEnabled;
        }
    };
}