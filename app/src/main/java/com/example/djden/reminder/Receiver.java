package com.example.djden.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by djden on 10.01.18.
 */

public class Receiver extends BroadcastReceiver {

    private final static String LOG_TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        Bundle extras = intent.getExtras();
        if (extras != null) {
//            String message = extras.getString("message");
//            Log.w(LOG_TAG, String.format("message: %s", message));
        }
        Log.w(LOG_TAG, "onReceive");
        Log.w(LOG_TAG, String.format("millis: %s", cal.getTimeInMillis()));
        Log.w(LOG_TAG, "action = " + intent.getAction());
        Log.w(LOG_TAG, "message = " + intent.getStringExtra("message"));

        Intent i = new Intent(context, ReminderActivity.class);
        i.putExtra("message", intent.getStringExtra("message"));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        System.out.print("message = " + intent.getStringExtra("message"));
    }
}
