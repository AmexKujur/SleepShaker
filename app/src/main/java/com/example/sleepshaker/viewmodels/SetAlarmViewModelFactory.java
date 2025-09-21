package com.example.sleepshaker.viewmodels; // Make sure it's in a 'viewmodels' package

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.sleepshaker.AlarmRepository;
import com.example.sleepshaker.AlarmScheduler;

public class SetAlarmViewModelFactory implements ViewModelProvider.Factory {
    private final AlarmRepository repository;
    private final AlarmScheduler scheduler;

    public SetAlarmViewModelFactory(AlarmRepository repository, AlarmScheduler scheduler) {
        this.repository = repository;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SetAlarmViewModel.class)) {
            return (T) new SetAlarmViewModel(repository, scheduler);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}