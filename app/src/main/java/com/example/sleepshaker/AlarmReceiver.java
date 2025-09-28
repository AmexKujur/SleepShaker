package com.example.sleepshaker;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS = "com.example.sleepshaker.DISMISS_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_DISMISS.equals(action)) {
            // --- Logic for the "Dismiss" button ---
            Log.d("AlarmReceiver", "Dismiss action received.");

            // Stop the ringtone service
            Intent serviceIntent = new Intent(context, RingtonePlayingService.class);
            context.stopService(serviceIntent);

            // Cancel the notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(RingtonePlayingService.NOTIFICATION_ID);

        } else {
            // --- Logic to start the alarm ---
            int alarmId = intent.getIntExtra("ALARM_ITEM_ID", -1);
            String challengeType = intent.getStringExtra("CHALLENGE_TYPE");

            Log.d("AlarmReceiver", "=== ALARM TRIGGERED === ID: " + alarmId + " Challenge: " + challengeType);

            // Start the ringtone service for this specific alarm
            Intent serviceIntent = new Intent(context, RingtonePlayingService.class);
            serviceIntent.putExtra("ALARM_ID", alarmId);
            if (intent.getExtras() != null) {
                serviceIntent.putExtras(intent.getExtras());
            }
            context.startService(serviceIntent);

            // CRITICAL: Directly launch DismissActivity from here
            Intent dismissIntent = new Intent(context, DismissActivity.class);
            dismissIntent.putExtra("ALARM_ITEM_ID", alarmId);
            dismissIntent.putExtra("CHALLENGE_TYPE", challengeType);

            // CRITICAL: These flags ensure separate activity instances
            dismissIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            Log.d("AlarmReceiver", "Starting DismissActivity for alarm ID: " + alarmId);
            context.startActivity(dismissIntent);
        }
    }
}