package com.example.sleepshaker.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.sleepshaker.AlarmItem;
import com.example.sleepshaker.AlarmRepository;
import com.example.sleepshaker.AlarmScheduler;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final AlarmRepository repository;
    private final LiveData<List<AlarmItem>> allAlarms;
    private final AlarmScheduler scheduler;

    public MainViewModel(@NonNull AlarmRepository repository, @NonNull Application application) {
        super(application);
        this.repository = repository;
        this.allAlarms = repository.getAllAlarms();
        this.scheduler = new AlarmScheduler(application);
    }

    public LiveData<List<AlarmItem>> getAllAlarms() {
        return allAlarms;
    }

    public void updateAlarm(AlarmItem alarmItem) {
        repository.update(alarmItem);
        if (alarmItem.isEnabled) {
            scheduler.schedule(alarmItem);
        } else {
            scheduler.cancel(alarmItem);
        }
    }

    public void deleteAlarm(AlarmItem alarmItem) {
        repository.delete(alarmItem);
        scheduler.cancel(alarmItem);
    }
}