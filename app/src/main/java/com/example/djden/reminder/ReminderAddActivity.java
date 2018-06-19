package com.example.djden.reminder;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderAddActivity extends AppCompatActivity
        implements TimePickerFragment.OnEnableBtnSave, DatePickerFragment.OnEnableBtnSave {
    private final static String LOG_TAG = "ReminderAddActivity";

    SQLiteDatabase db;
    DatabaseHelper dbHelper;

    float initialX, initialY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        dbHelper = new DatabaseHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();

                EditText edtText = (EditText) findViewById(R.id.reminder_add_msg);
                int[] edtLoc = new int[2];
                edtText.getLocationOnScreen(edtLoc);

                float edtW = edtText.getWidth();
                float edtH = edtText.getHeight();

                Log.w(LOG_TAG, String.format("dispatchTouchEvent: x:%s, y:%s, w:%s, h:%s",
                        edtLoc[0], edtLoc[1], edtW, edtH));

                Log.d(LOG_TAG, String.format("Action was DOWN: initialX: %s, initialY: %s", initialX, initialY));

                if ((initialX >= edtLoc[0] && initialX <= (edtLoc[0] + edtW)) &&
                        (initialY >= edtLoc[1] && initialY <= (edtLoc[1] + edtH))) {
                    edtText.setEnabled(true);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public void onClickClearMsg(View view) {
        EditText etMsg = (EditText) findViewById(R.id.reminder_add_msg);
        etMsg.setEnabled(false);
        etMsg.setText(null);
    }

    public void onClickClearDateTime(View view) {
        TextView date = (TextView) findViewById(R.id.reminder_add_date);
        TextView time = (TextView) findViewById(R.id.reminder_add_time);

        date.setText(getResources().getString(R.string.date));
        time.setText(getResources().getString(R.string.time));

        Button btnSave = (Button) findViewById(R.id.reminder_add_save);
        btnSave.setEnabled(false);

    }

    public void onClickClearRepeat(View view) {
        TextView tv = (TextView) findViewById(R.id.reminder_add_repeat);
        tv.setText("NONE");

        TextView tvTxt = (TextView) findViewById(R.id.reminder_add_repeat_txt);
        tvTxt.setText(getString(R.string.repeat_none));
    }

    public void onClickCheckIsSound(View view) {
        CheckBox chk = (CheckBox) findViewById(R.id.reminder_add_is_sound_check);
        chk.setChecked(chk.isChecked());
        if (chk.isChecked()) {
            chk.setButtonDrawable(R.mipmap.checkbox_on);
        } else {
            chk.setButtonDrawable(R.mipmap.checkbox_off);
        }

    }

    public void onClickSave(View view) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy HH:mm", Locale.getDefault());

        TextView date = (TextView) findViewById(R.id.reminder_add_date);
        TextView time = (TextView) findViewById(R.id.reminder_add_time);
        String dateTime = String.format(
                "%s %s",
                date.getText().toString(),
                time.getText().toString()
        );

        calendar.setTime(dateFormat.parse(dateTime));
        Log.w(LOG_TAG, calendar.getTime().toString());

        EditText etMsg = (EditText) findViewById(R.id.reminder_add_msg);
        String msg = etMsg.getText().toString();
        msg = msg.isEmpty() ? getResources().getString(R.string.new_reminder) : msg;

        TextView tvRepeat = (TextView) findViewById(R.id.reminder_add_repeat);
        String repeat = tvRepeat.getText().toString();

        CheckBox chk = (CheckBox) findViewById(R.id.reminder_add_is_sound_check);

        ContentValues newValues = new ContentValues();
        // Задайте значения для каждой строки.
        newValues.put(DatabaseHelper.REM_COLUMN_MSG, msg);
        newValues.put(DatabaseHelper.REM_COLUMN_D_START, dateFormatDB.format(calendar.getTime()));
        newValues.put(DatabaseHelper.REM_COLUMN_D_START_FRMT, dateFormat.format(calendar.getTime()));
        newValues.put(DatabaseHelper.REM_COLUMN_IS_SOUND, chk.isChecked() ? "T" : "F");
        if (!repeat.equals("NONE")) {
            newValues.put(DatabaseHelper.REM_COLUMN_REPEAT, repeat);
        }
        db.insert(DatabaseHelper.TABLE_REMINDERS, null, newValues);

        Cursor query = db.rawQuery(String.format(
                "select max(_id) as maxID  from %s ;",
                DatabaseHelper.TABLE_REMINDERS
        ), null);

        if (query.moveToFirst()) {
            String id;
            id = query.getString(query.getColumnIndex("maxID"));
            ManagementAlarms ma = new ManagementAlarms(getApplicationContext());
            ma.addAlarm(msg, calendar, id);
        }

        db.close();
        setResult(RESULT_OK, null);
        finish();
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public void onClickSelectRepeat(View v) {
        RepeatDialog rd = new RepeatDialog();
        rd.show(getFragmentManager(), "repeatDialog");
    }

    public void showDatePickerDialog(View v) {
        //Скрываем клавиатуру
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        //Скрываем клавиатуру
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onEnableBtnSave() {
        TextView date = (TextView) findViewById(R.id.reminder_add_date);
        TextView time = (TextView) findViewById(R.id.reminder_add_time);

        Button btnSave = (Button) findViewById(R.id.reminder_add_save);

        String dateTime = String.format(
                "%s %s",
                date.getText().toString().trim(),
                time.getText().toString().trim()
        );

        Pattern p = Pattern.compile("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}");
        Matcher m = p.matcher(dateTime);
        boolean b = m.matches() && isCorrectDate(dateTime);

        Log.d(LOG_TAG, String.format("DateTime %s", dateTime));
        if (b) {
            btnSave.setEnabled(true);
        } else {
            btnSave.setEnabled(false);
        }
    }

    public boolean isCorrectDate(String dateTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime((new SimpleDateFormat("dd-MM-yyyy HH:mm")).parse(dateTime));
        } catch (ParseException e) {
            return false;
        }
        long seconds = (calendar.getTime().getTime() - new Date().getTime()) / 1000;
        return BuildConfig.DEBUG || seconds >= 300; //Больше 5 минут
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}