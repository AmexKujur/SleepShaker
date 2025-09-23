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
            Log.d("AlarmReceiver", "Alarm is triggering, starting RingtonePlayingService...");

            // Start the service. The service will handle the notification, sound, and activity launch.
            Intent serviceIntent = new Intent(context, RingtonePlayingService.class);
            if (intent.getExtras() != null) {
                serviceIntent.putExtras(intent.getExtras()); // Forward extras to the service
            }
            context.startService(serviceIntent);
        }
    }
}