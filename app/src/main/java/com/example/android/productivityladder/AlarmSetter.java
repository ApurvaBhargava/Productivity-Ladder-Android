package com.example.android.productivityladder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            Intent service = new Intent(context, AlarmService.class);
            service.setAction(AlarmService.CREATE);
            context.startService(service);
        }
    }

}