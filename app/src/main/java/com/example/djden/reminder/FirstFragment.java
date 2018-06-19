package com.example.djden.reminder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FirstFragment extends Fragment {
    private final static String LOG_TAG = "FirstFragment";

    public static final int REQUEST_ADD_REM = 1;

    SQLiteDatabase db; //Открытие происходит в refreshReminders
    DatabaseHelper dbHelper;

    Cursor remindersCursor;
    SimpleCursorAdapter remindersAdapter;

    ListView remList;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());

        inactiveOldReminders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // действия, совершаемые после нажатия на кнопку
                // Создаем объект Intent для вызова новой Activity
                Intent intent = new Intent(getContext(), ReminderAddActivity.class);
                // запуск activity
                startActivityForResult(intent, REQUEST_ADD_REM);
            }
        });

        remList = (ListView) view.findViewById(R.id.reminder_active_list);
        remList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(LOG_TAG, String.format("onItemClick: %s", id));
                Intent intent = new Intent(getContext(), ReminderShowActivity.class);
                intent.putExtra("reminderID", String.valueOf(id));
                // запуск activity
                startActivity(intent);
            }
        });
        refreshReminders();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_REM && resultCode == getActivity().RESULT_OK) {
            refreshReminders();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.w(LOG_TAG, "onResume");
        refreshReminders();
    }

    protected void refreshReminders() {
        // открываем подключение
        db = dbHelper.getReadableDatabase();
        //получаем данные из бд в виде курсора
        String sql = String.format(
                "select * from v_%s where is_active='T' or is_active='O' order by d_start;",
                DatabaseHelper.TABLE_REMINDERS
        );
        try {
            remindersCursor = db.rawQuery(sql, null);
        } catch (SQLiteException ex) {
            Toast.makeText(getContext(), R.string.db_bad, Toast.LENGTH_LONG).show();
            if (remindersCursor != null) {
                remindersCursor.close();
            }
            return;
        }

        // определяем, какие столбцы из курсора будут выводиться в ListView
        String[] headers = new String[]{
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.REM_COLUMN_D_START_FRMT,
                DatabaseHelper.REM_COLUMN_D_START,
                DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                DatabaseHelper.REM_COLUMN_REPEAT,
                DatabaseHelper.REM_COLUMN_IS_SOUND,
        };

        // создаем адаптер, передаем в него курсор
        remindersAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_reminder,
                remindersCursor,
                headers,
                new int[]{R.id.reminder_id, R.id.reminder_id_is_sound, R.id.reminder_msg, R.id.reminder_d_start},
                0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                ImageView reminderIcon = (ImageView) view.findViewById(R.id.reminder_icon_is_active);
                ImageView reminderIsSound = (ImageView) view.findViewById(R.id.reminder_is_sound);
                Cursor reminderItem = (Cursor) remindersAdapter.getItem(position);
                String isActive = reminderItem.getString(reminderItem.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_ACTIVE));
                String isSound = reminderItem.getString(reminderItem.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_SOUND));

                LinearLayout llIcon = (LinearLayout) view.findViewById(R.id.reminder_liner_layout_icon);
                llIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeIsActiveReminder(v);
                    }
                });

                LinearLayout llIsSound = (LinearLayout) view.findViewById(R.id.reminder_liner_layout_is_sound);
                llIsSound.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.w(LOG_TAG, String.format("onClick"));
                        changeIsSoundReminder(v);
                    }
                });

                reminderIcon.setImageResource(
                        isActive.equalsIgnoreCase("T") || isActive.equalsIgnoreCase("O") ? R.mipmap.checkbox_off : R.mipmap.checkbox_on
                );

                reminderIsSound.setImageResource(
                        isSound.equalsIgnoreCase("T") ? R.drawable.sound_on : R.drawable.sound_off
                );

                LinearLayout linerLayout = (LinearLayout) view.findViewById(R.id.item_reminder_linear_layout);
                if (isActive.equalsIgnoreCase("O")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.overdue_color, null));
                    } else {
                        linerLayout.setBackgroundColor(getResources().getColor(R.color.overdue_color));
                    }
                } else {
                    if (position % 2 != 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            linerLayout.setBackgroundColor(getResources().getColor(R.color.stripeRows, null));
                        } else {
                            linerLayout.setBackgroundColor(getResources().getColor(R.color.stripeRows));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            linerLayout.setBackgroundColor(getResources().getColor(R.color.default_color, null));
                        } else {
                            linerLayout.setBackgroundColor(getResources().getColor(R.color.default_color));
                        }
                    }
                }


                return view;
            }

        };

        remList.setAdapter(remindersAdapter);
    }

    protected void changeIsActiveReminder(View v) {
        TextView tvID = (TextView) v.findViewById(R.id.reminder_id);
        ImageView reminderIcon = (ImageView) v.findViewById(R.id.reminder_icon_is_active);
        String id = tvID.getText().toString();
        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s, %s, %s, %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_ID,
                DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                DatabaseHelper.REM_COLUMN_D_START,
                DatabaseHelper.REM_COLUMN_MSG,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{id});

        if (query.moveToFirst()) {
            ManagementAlarms ma = new ManagementAlarms(getContext());
            String isActive = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_ACTIVE));
            String dStart = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_D_START));
            String msg = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_MSG));
            ContentValues newValues = new ContentValues();
            // Задайте значения для каждой строки.
            newValues.put(
                    DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                    isActive.equalsIgnoreCase("F") ? "T" : "F"
            );
            db.update(DatabaseHelper.TABLE_REMINDERS, newValues, "_id=?", new String[]{id});
            newValues.clear();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                calendar.setTime(dateFormatDB.parse(dStart));
                if (isActive.equalsIgnoreCase("F")) {
                    ma.addAlarm(msg, calendar, tvID.getText().toString());
                } else {
                    ma.cancelAlarm(tvID.getText().toString());
                }
            } catch (ParseException e) {
                Toast.makeText(
                        getContext(),
                        String.format("%s ID=%s", R.string.error_parse, id),
                        Toast.LENGTH_LONG
                ).show();
            }

            reminderIcon.setImageResource(
                    isActive.equalsIgnoreCase("F") ? R.mipmap.checkbox_off : R.mipmap.checkbox_on
            );
        }

        //Log.d(LOG_TAG, String.format("id: %s", id));
    }

    protected void changeIsSoundReminder(View v) {
        TextView tvID = (TextView) v.findViewById(R.id.reminder_id_is_sound);
        ImageView reminderIsSound = (ImageView) v.findViewById(R.id.reminder_is_sound);
        String id = tvID.getText().toString();
        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_IS_SOUND,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{id});

        if (query.moveToFirst()) {
            ManagementAlarms ma = new ManagementAlarms(getContext());
            String isSound = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_SOUND));
            ContentValues newValues = new ContentValues();
            // Задайте значения для каждой строки.
            newValues.put(
                    DatabaseHelper.REM_COLUMN_IS_SOUND,
                    isSound.equalsIgnoreCase("F") ? "T" : "F"
            );
            db.update(DatabaseHelper.TABLE_REMINDERS, newValues, "_id=?", new String[]{id});
            newValues.clear();

            reminderIsSound.setImageResource(
                    isSound.equalsIgnoreCase("F") ? R.drawable.sound_on : R.drawable.sound_off
            );
        }

        Log.w(LOG_TAG, String.format("changeIsSoundReminder id: %s", id));
    }

    protected void inactiveOldReminders() {
        db = dbHelper.getReadableDatabase();
        ContentValues newValues = new ContentValues();
        // Задайте значения для каждой строки.
        newValues.put(
                DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                "O" //Overdue (просроченный запоздаллый)
        );
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        SimpleDateFormat dateFormatDB = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        db.update(
                DatabaseHelper.TABLE_REMINDERS,
                newValues,
                "d_start<? and is_active<>'F'",
                new String[]{dateFormatDB.format(calendar.getTime())}
        );
        newValues.clear();
        Log.w(LOG_TAG, String.format("inactiveOldReminders: %s", calendar.getTime()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (remindersCursor != null) {
            remindersCursor.close();
        }
    }
}
