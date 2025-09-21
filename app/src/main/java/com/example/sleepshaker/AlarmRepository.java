package com.example.sleepshaker;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {

    private final AlarmDao alarmDao;
    private final LiveData<List<AlarmItem>> allAlarms;
    private final ExecutorService executorService;

    public AlarmRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        alarmDao = database.alarmDao();
        allAlarms = alarmDao.getAllAlarms();
        executorService = Executors.newSingleThreadExecutor();
    }

    // ADD THIS METHOD
    public long insertAndGetId(AlarmItem alarmItem) {
        // This is a synchronous call, so it must be run on a background thread
        // by the ViewModel that calls it.
        return alarmDao.insert(alarmItem);
    }

    public void update(AlarmItem alarmItem) {
        executorService.execute(() -> alarmDao.update(alarmItem));
    }



    public void delete(AlarmItem alarmItem) {
        executorService.execute(() -> alarmDao.delete(alarmItem));
    }

    public LiveData<List<AlarmItem>> getAllAlarms() {
        return allAlarms;
    }
}