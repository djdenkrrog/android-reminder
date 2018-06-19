package com.example.djden.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Сервис для переинициализации всех напоминаний например после перезагрузки
 */
public class ReinitRemindersService extends Service {
    private final static String LOG_TAG = "ReinitRemindersService";

    public ReinitRemindersService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(LOG_TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(LOG_TAG, "onStartCommand");
        reinitReminders();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void reinitReminders() {
        SQLiteDatabase db;
        DatabaseHelper dbHelper;

        dbHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s, %s, %s from %s where %s=?;",
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.REM_COLUMN_D_START,
                DatabaseHelper.TABLE_REMINDERS,
                DatabaseHelper.REM_COLUMN_IS_ACTIVE
        ), new String[]{"T"});

        if (query.moveToFirst()) {
            String msg;
            String remID;
            String dStart;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            do {
                msg = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_MSG));
                remID = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_ID));
                dStart = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_D_START));

                try {
                    calendar.setTime(dateFormatDB.parse(dStart));
                    addAlarm(msg, calendar, remID);
                } catch (ParseException e) {
                    Log.w(LOG_TAG, String.format("Error parse date: %s; dateStr: %s", e.getMessage(), dStart));
                }
            } while (query.moveToNext());
        }

        db.close();
        query.close();
        stopSelf();
    }

    protected void addAlarm(String msg, Calendar date, String remID) {
        Intent intent = new Intent(getApplicationContext(), RemindersActivity.class);
        intent.putExtra("message", msg);
        intent.setAction(remID);//Для того чтоб Интенты отличались
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(),
                Integer.valueOf(remID),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pi);

        Log.w(LOG_TAG, String.format("addAlarm: remID: %s, msg: %s, date: %s", remID, msg, date.getTime().toString()));
    }
}
