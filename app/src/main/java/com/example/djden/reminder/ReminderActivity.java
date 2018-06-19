package com.example.djden.reminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private final static String LOG_TAG = "ReminderActivity";

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    String reminderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                //| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        Log.w(LOG_TAG, String.format("onCreate action: %s", getIntent().getAction()));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            reminderID = getIntent().getAction();
            String message = extras.getString("message");

            TextView tvMessage = (TextView) findViewById(R.id.reminder_message);
            tvMessage.setText(message);

            inactiveReminder();
            attention();

            Log.w(LOG_TAG, String.format("onCreate - msg: %s; reminderID: %s;", message, reminderID));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                ManagementSoundVibrate.getInstance(getApplicationContext()).stop();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public void onClickStop(View view) {
        //Log.w(LOG_TAG, "onClickStop");
        ManagementSoundVibrate.getInstance(getApplicationContext()).stop();
        finish();
    }

    public void onClickPostpone(View view) {
        //Log.w(LOG_TAG, "onClickPostpone");
        ManagementSoundVibrate.getInstance(getApplicationContext()).stop();
        postpone();
        finish();
    }

    protected void postpone() {
        SQLiteDatabase db;
        DatabaseHelper dbHelper;

        dbHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{reminderID});

        if (query.moveToFirst()) {
            ContentValues newValues = new ContentValues();
            String msg = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_MSG));

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:00", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "dd-MM-yyyy HH:mm", Locale.getDefault());

            calendar.add(Calendar.MINUTE, 15);

            newValues.clear();
            newValues.put(DatabaseHelper.REM_COLUMN_D_START, dateFormatDB.format(calendar.getTime()));
            newValues.put(DatabaseHelper.REM_COLUMN_D_START_FRMT, dateFormat.format(calendar.getTime()));
            newValues.put(DatabaseHelper.REM_COLUMN_IS_ACTIVE, "T");
            db.update(DatabaseHelper.TABLE_REMINDERS, newValues, "_id=?", new String[]{reminderID});
            newValues.clear();

            ManagementAlarms ma = new ManagementAlarms(getApplicationContext());
            ma.addAlarm(msg, calendar, reminderID);

            Log.w(LOG_TAG, calendar.getTime().toString());
        }

        db.close();
        query.close();
    }

    public void attention() {

        SQLiteDatabase db;
        DatabaseHelper dbHelper;

        dbHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_IS_SOUND,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{reminderID});

        if (query.moveToFirst()) {
            boolean isSound = query.getString(
                    query.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_SOUND)
            ).equals("T");
            ManagementSoundVibrate.getInstance(getApplicationContext()).start(isSound);
        } else {
            ManagementSoundVibrate.getInstance(getApplicationContext()).start(false);
        }
    }

    protected void inactiveReminder() {
        SQLiteDatabase db;
        DatabaseHelper dbHelper;

        dbHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s, %s, %s, %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_REPEAT,
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.REM_COLUMN_D_START,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{reminderID});

        if (query.moveToFirst()) {
            ContentValues newValues = new ContentValues();
            String repeat = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_REPEAT));
            String dStart = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_D_START));
            String msg = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_MSG));

            if (repeat.equals("NONE")) { // Если нет повтора то закрывем

                Button btnPostpone = (Button) findViewById(R.id.reminder_postpone);
                btnPostpone.setVisibility(View.VISIBLE);

                // Задайте значения для каждой строки.
                newValues.put(
                        DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                        "F"
                );
                db.update(DatabaseHelper.TABLE_REMINDERS, newValues, "_id=?", new String[]{reminderID});
                newValues.clear();
            } else {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "dd-MM-yyyy HH:mm", Locale.getDefault());

                try {
                    calendar.setTime(dateFormatDB.parse(dStart));
                    int repTime = getTimeRepeat(repeat, calendar);
                    if (repTime != -1) {
                        newValues.clear();
                        newValues.put(DatabaseHelper.REM_COLUMN_D_START, dateFormatDB.format(calendar.getTime()));
                        newValues.put(DatabaseHelper.REM_COLUMN_D_START_FRMT, dateFormat.format(calendar.getTime()));

                        db.update(DatabaseHelper.TABLE_REMINDERS, newValues, "_id=?", new String[]{reminderID});
                        newValues.clear();

                        ManagementAlarms ma = new ManagementAlarms(getApplicationContext());
                        ma.addAlarm(msg, calendar, reminderID);
                    }
                    Log.w(LOG_TAG, calendar.getTime().toString());


                } catch (ParseException e) {
                    Log.e(LOG_TAG, String.format("Error parse inactiveReminder: %s", e.getMessage()));
                }

            }

            Log.d(LOG_TAG, String.format("id: %s", reminderID));
        }

        db.close();
        query.close();
    }

    protected int getTimeRepeat(String repeat, Calendar calendar) {
        int result = -1;
        switch (repeat) {
            case "HOUR":
                calendar.add(Calendar.HOUR, 1);
                result = 0;
                break;
            case "DAY":
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                result = 0;
                break;
            case "WEEK":
                calendar.add(Calendar.WEEK_OF_MONTH, 1);
                result = 0;
                break;
            case "MONTH":
                calendar.add(Calendar.MONTH, 1);
                result = 0;
                break;
            case "MONTH_LAST_DAY":
                calendar.add(Calendar.MONTH, 1);
                int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                Log.d(LOG_TAG, String.format("lastDay: %s", lastDay));
                calendar.set(Calendar.DAY_OF_MONTH, lastDay);
                result = 0;
                break;
        }
        return result;
    }

    protected void sendNotify(String message) {
        Intent notificationIntent = new Intent(getApplicationContext(), ReminderActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // до версии Android 8.0 API 26
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(contentIntent)
                // обязательные настройки
                .setSmallIcon(R.mipmap.icon)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("Напоминание")
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText(message) // Текст уведомления
                // необязательные настройки
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.icon)) // большая
                // картинка
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker(String.format("Ticker: %s", message))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Альтернативный вариант
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}

