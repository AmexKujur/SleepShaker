package com.example.sleepshaker; // Use your actual package name

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmScheduler {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(AlarmItem alarmItem) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ITEM_ID", alarmItem.id);
        intent.putExtra("CHALLENGE_TYPE", alarmItem.dismissMethod);

        Log.d("AlarmScheduler", "Scheduling alarm ID " + alarmItem.id + " with CHALLENGE_TYPE: " + alarmItem.dismissMethod);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.id, // Unique request code for each alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Ensure alarm time is in the future
        if (alarmItem.timeInMillis < System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "Attempted to schedule alarm ID " + alarmItem.id + " in the past. Skipping.");
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmItem.timeInMillis, pendingIntent);
                } else {
                    // Handle case where permission is not granted, e.g., show a dialog or notification
                    Log.e("AlarmScheduler", "Cannot schedule exact alarms for ID " + alarmItem.id + ". Permission not granted.");
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmItem.timeInMillis, pendingIntent);
            }
            Log.d("AlarmScheduler", "Alarm ID " + alarmItem.id + " scheduled successfully for " + alarmItem.timeInMillis);
        } catch (SecurityException e) {
            Log.e("AlarmScheduler", "SecurityException: Cannot schedule exact alarms for ID " + alarmItem.id, e);
        }
    }

    public void cancel(AlarmItem alarmItem) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("AlarmScheduler", "Cancelled alarm ID " + alarmItem.id);
    }
}