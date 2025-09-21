package com.example.sleepshaker;

import androidx.lifecycle.ViewModel;

public class SetAlarmViewModel extends ViewModel {
    private final AlarmScheduler alarmScheduler;

    public SetAlarmViewModel(AlarmScheduler alarmScheduler) {
        this.alarmScheduler = alarmScheduler;
    }

    public void schedule(AlarmItem alarmItem) {
        alarmScheduler.schedule(alarmItem);
    }
}