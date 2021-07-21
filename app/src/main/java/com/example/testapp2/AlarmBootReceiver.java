package com.example.testapp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            HelperCode.initializeAlarmManager(context);
            HelperCode.setTestAlarm(context);
            Log.w("Stuff", "Got reboot message!");
        }
    }
}
