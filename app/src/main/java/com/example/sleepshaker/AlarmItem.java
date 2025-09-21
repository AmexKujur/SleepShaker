package com.example.sleepshaker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Set;

@Entity(tableName = "alarms")
@TypeConverters(DaysSetConverter.class)
public class AlarmItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timeInMillis;
    public int hour;
    public int minute;
    public String message; // Changed from 'label'
    public boolean isEnabled;
    public Set<Integer> repeatDays; // Changed from 'repeatingDays'
    public String dismissMethod;

    // Default constructor for Room
    public AlarmItem() {}

    // A more complete constructor
    public AlarmItem(long timeInMillis, int hour, int minute, String message, boolean isEnabled, Set<Integer> repeatDays, String dismissMethod) {
        this.timeInMillis = timeInMillis;
        this.hour = hour;
        this.minute = minute;
        this.message = message;
        this.isEnabled = isEnabled;
        this.repeatDays = repeatDays;
        this.dismissMethod = dismissMethod;
    }

    // Copy constructor to help with setting the ID after insertion
    public AlarmItem(AlarmItem other, int newId) {
        this.id = newId;
        this.timeInMillis = other.timeInMillis;
        this.hour = other.hour;
        this.minute = other.minute;
        this.message = other.message;
        this.isEnabled = other.isEnabled;
        this.repeatDays = other.repeatDays;
        this.dismissMethod = other.dismissMethod;
    }
}