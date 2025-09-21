package com.example.sleepshaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "!!! ALARM HAS BEEN TRIGGERED !!!");
        // Start the ringtone service
        Intent serviceIntent = new Intent(context, RingtonePlayingService.class);
        context.startService(serviceIntent);

        // Start the DismissActivity
        Intent activityIntent = new Intent(context, DismissActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Pass along the extras from the AlarmScheduler
        if (intent.getExtras() != null) {
            activityIntent.putExtras(intent.getExtras());
        }

        context.startActivity(activityIntent);
    }
}