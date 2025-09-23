package com.example.sleepshaker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class RingtonePlayingService extends Service {
    private Ringtone ringtone;
    public static final int NOTIFICATION_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // --- Create Intent to open DismissActivity when notification is tapped ---
        Intent activityIntent = new Intent(this, DismissActivity.class);
        if (intent.getExtras() != null) {
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
                .setContentText("Tap to dismiss or select a challenge.")
                .setSmallIcon(R.drawable.ic_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setContentIntent(contentPendingIntent) // Set the main tap action
                .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        // --- Play the ringtone ---
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        this.ringtone = RingtoneManager.getRingtone(this, alarmUri);
        ringtone.play();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null) {
            ringtone.stop();
        }
        stopForeground(true);
    }
}