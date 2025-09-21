package com.example.sleepshaker.viewmodels; // Make sure it's in a 'viewmodels' package

import androidx.lifecycle.ViewModel;
import com.example.sleepshaker.AlarmItem;
import com.example.sleepshaker.AlarmRepository;
import com.example.sleepshaker.AlarmScheduler;

public class SetAlarmViewModel extends ViewModel {
    private final AlarmRepository repository;
    private final AlarmScheduler scheduler;

    public SetAlarmViewModel(AlarmRepository repository, AlarmScheduler scheduler) {
        this.repository = repository;
        this.scheduler = scheduler;
    }

    public long insert(AlarmItem alarm) {
        return repository.insertAndGetId(alarm); // Assuming Repository has this method
    }

    public void schedule(AlarmItem alarm) {
        scheduler.schedule(alarm);
    }
}