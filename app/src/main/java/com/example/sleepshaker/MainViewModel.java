package com.example.sleepshaker; // Use your actual package name

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private final AlarmDao alarmDao;
    private final LiveData<List<AlarmItem>> allAlarms;
    private final ExecutorService executorService;
    private final AlarmScheduler alarmScheduler;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        alarmDao = database.alarmDao();
        allAlarms = alarmDao.getAllAlarms();
        executorService = Executors.newSingleThreadExecutor();
        alarmScheduler = new AlarmScheduler(application);
    }

    public LiveData<List<AlarmItem>> getAllAlarms() {
        return allAlarms;
    }

    public void deleteAlarm(AlarmItem alarmItem) {
        executorService.execute(() -> {
            alarmDao.delete(alarmItem);
            // Also cancel the scheduled alarm in the system
            alarmScheduler.cancel(alarmItem);
        });
    }

    public void updateAlarm(AlarmItem alarmItem) {
        executorService.execute(() -> {
            alarmDao.update(alarmItem);
            // If the alarm is now enabled, schedule it. Otherwise, cancel it.
            if (alarmItem.isEnabled) {
                alarmScheduler.schedule(alarmItem);
            } else {
                alarmScheduler.cancel(alarmItem);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown the executor when the ViewModel is destroyed to prevent memory leaks
        executorService.shutdown();
    }
}