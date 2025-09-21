package com.example.sleepshaker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    long insertAndGetId(AlarmItem alarm);

    @Update
    void update(AlarmItem alarm);

    @Delete
    void delete(AlarmItem alarm);

    @Query("SELECT * FROM alarms ORDER BY hour, minute ASC")
    LiveData<List<AlarmItem>> getAllAlarms();
}