package com.example.sleepshaker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class RingtonePlayingService extends Service {
    private Ringtone ringtone;
    public static final int NOTIFICATION_ID = 1;
    private int currentAlarmId = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int alarmId = intent.getIntExtra("ALARM_ID", -1);

            // Handle STOP_ALARM action
            if ("STOP_ALARM".equals(action)) {
                Log.d("RingtoneService", "Received STOP_ALARM for ID: " + alarmId);
                if (alarmId == currentAlarmId || alarmId == -1) {
                    // Stop this specific alarm or stop all if ID is -1
                    stopSelf();
                }
                return START_NOT_STICKY;
            }

            // Handle starting ringtone
            currentAlarmId = alarmId;
            Log.d("RingtoneService", "Starting ringtone for alarm ID: " + alarmId);
        }

        // --- Create Intent to open DismissActivity when notification is tapped ---
        Intent activityIntent = new Intent(this, DismissActivity.class);
        if (intent != null && intent.getExtras() != null) {
            activityIntent.putExtras(intent.getExtras()); // Forward extras
        }
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // --- Create "Dismiss" action for the notification button ---
        Intent dismissIntent = new Intent(this, AlarmReceiver.class);
        dismissIntent.setAction(AlarmReceiver.ACTION_DISMISS);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // --- Build the notification ---
        Notification notification = new NotificationCompat.Builder(this, MainActivity.ALARM_CHANNEL_ID)
                .setContentTitle("WAKE UP!")
                .setContentText("Alarm " + currentAlarmId + " - Tap to dismiss or select a challenge.")
                .setSmallIcon(R.drawable.ic_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setContentIntent(contentPendingIntent) // Set the main tap action
                .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        // --- Play the ringtone (only if not already playing) ---
        if (ringtone == null || !ringtone.isPlaying()) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            this.ringtone = RingtoneManager.getRingtone(this, alarmUri);
            if (ringtone != null) {
                ringtone.play();
                Log.d("RingtoneService", "Ringtone started playing for alarm " + currentAlarmId);
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("RingtoneService", "Service destroying - stopping ringtone for alarm " + currentAlarmId);
        if (ringtone != null) {
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
        }
        stopForeground(true);
        super.onDestroy();
    }
}