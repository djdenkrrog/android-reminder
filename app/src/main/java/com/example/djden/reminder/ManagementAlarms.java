package com.example.djden.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by djden on 12.01.18.
 */

public class ManagementAlarms {

    protected final String LOG_TAG = "ManagementAlarms";

    SQLiteDatabase db;
    DatabaseHelper dbHelper;

    protected Context context = null;

    ManagementAlarms(Context context) {
        this.context = context;

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    public void addAlarm(String msg, Calendar date, String remID) {
        Intent intent = new Intent(context, RemindersActivity.class);
        intent.putExtra("message", msg);
        intent.setAction(remID);//Для того чтоб Интенты отличались
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                Integer.valueOf(remID),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pi);

        Log.w(LOG_TAG, new Throwable().getStackTrace()[0].getMethodName());
    }

    public void cancelAlarm(String remID) {
        Intent intent = new Intent(context, ReminderActivity.class);
        intent.setAction(remID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                Integer.valueOf(remID),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pi);

        Log.w(LOG_TAG, new Throwable().getStackTrace()[0].getMethodName());
    }

}
