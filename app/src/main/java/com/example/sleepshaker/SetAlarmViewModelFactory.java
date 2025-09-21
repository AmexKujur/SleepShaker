package com.example.sleepshaker;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SetAlarmViewModelFactory implements ViewModelProvider.Factory {
    private final AlarmScheduler alarmScheduler;

    public SetAlarmViewModelFactory(AlarmScheduler alarmScheduler) {
        this.alarmScheduler = alarmScheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SetAlarmViewModel.class)) {
            return (T) new SetAlarmViewModel(alarmScheduler);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}