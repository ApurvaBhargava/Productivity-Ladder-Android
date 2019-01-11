package com.example.android.productivityladder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("id", 0);
        String name = intent.getStringExtra("name");
        String details = intent.getStringExtra("details");

        PendingIntent pi = PendingIntent.getActivity(context, (int)id, new Intent(), 0);

        Notification n = new Notification.Builder(context)
                .setContentTitle(name)
                .setContentText(details)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int)id, n);
    }

}