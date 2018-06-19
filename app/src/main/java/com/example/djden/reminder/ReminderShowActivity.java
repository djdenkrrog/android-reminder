package com.example.djden.reminder;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class ReminderShowActivity extends AppCompatActivity {
    private final static String LOG_TAG = "ReminderShowActivity";

    SQLiteDatabase db;
    DatabaseHelper dbHelper;

    String reminderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_show);

        dbHelper = new DatabaseHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            reminderID = extras.getString("reminderID");
            setDateToField();

            Log.w(LOG_TAG, String.format("reminderID: %s;", reminderID));
        }
    }

    protected void setDateToField() {
        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s, %s, %s, %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.REM_COLUMN_D_START_FRMT,
                DatabaseHelper.REM_COLUMN_REPEAT,
                DatabaseHelper.REM_COLUMN_IS_SOUND,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{reminderID});

        if (query.moveToFirst()) {
            String msg = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_MSG));
            String dStart = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_D_START_FRMT));
            String repeat = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_REPEAT));
            String isSound = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_SOUND));

            EditText etMsg = (EditText) findViewById(R.id.reminder_show_msg);
            TextView date = (TextView) findViewById(R.id.reminder_show_date);
            TextView time = (TextView) findViewById(R.id.reminder_show_time);
            CheckBox chk = (CheckBox) findViewById(R.id.reminder_show_is_sound_check);

            etMsg.setText(msg);
            etMsg.setInputType(InputType.TYPE_NULL);
            date.setText(dStart.substring(0, 10).trim());
            time.setText(dStart.substring(11, 16).trim());
            chk.setChecked(isSound.equalsIgnoreCase("T"));
            chk.setButtonDrawable(chk.isChecked() ? R.mipmap.checkbox_on : R.mipmap.checkbox_off);

            //repeat
            TextView tv = (TextView) findViewById(R.id.reminder_show_repeat);
            tv.setText(repeat);
            if (!repeat.equals("NONE")) {
                TextView tvTxt = (TextView) findViewById(R.id.reminder_show_repeat_txt);
                for (int i = 0; i < RepeatDialog.listRepeats.length; i++) {
                    if (RepeatDialog.listRepeats[i].equals(repeat)) {
                        tvTxt.setText(getString(RepeatDialog.listRepeatsRes[i]));
                    }
                }
            }
        }
    }

    public void onClickClose(View view) {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}