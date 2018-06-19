package com.example.djden.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {
    private final static String LOG_TAG = "BootBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(LOG_TAG, "onReceive");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            context.startService(new Intent(context, ReinitRemindersService.class));
            Log.w(LOG_TAG, "BOOT_COMPLETED");
        }
    }
}
