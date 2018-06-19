package com.example.djden.reminder;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class RemindersActivity extends AppCompatActivity {
    private final static String LOG_TAG = "RemindersActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        Log.w(LOG_TAG, "onCreate");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.reminder_tab_panel);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                Log.w(LOG_TAG, String.format("Tab: %s", tab.getPosition()));
                Fragment fragment = null;
                ImageView imgCornerRight = (ImageView) findViewById(R.id.reminder_corner_right);
                ImageView imgCornerLeft = (ImageView) findViewById(R.id.reminder_corner_left);
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FirstFragment();
                        imgCornerRight.setVisibility(View.VISIBLE);
                        imgCornerLeft.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        fragment = new SecondFragment();
                        imgCornerRight.setVisibility(View.INVISIBLE);
                        imgCornerLeft.setVisibility(View.VISIBLE);
                        break;
                }
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.reminder_frame_layout, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Fragment fragment = new FirstFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.reminder_frame_layout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        startReminder();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG_TAG, "onStart");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w(LOG_TAG, String.format("onNewIntent action: %s", intent.getAction()));
        this.setIntent(intent);
        startReminder();
    }

    protected void startReminder() {
        String reminderID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String message = extras.getString("message");
            reminderID = intent.getAction();

            if (isActiveReminder(reminderID)) {
                Intent reminderIntent = new Intent(getApplicationContext(), ReminderActivity.class);
                reminderIntent.putExtra("message", message);
                reminderIntent.setAction(reminderID);//Для того чтоб Интенты отличались
                reminderIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivity(reminderIntent);

                if (isLockScreen()) {
                    finish();
                }
            }


            Log.w(LOG_TAG, String.format("startReminder - message: %s; reminderID: %s;", message, reminderID));
        }
    }

    protected boolean isLockScreen() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }

    protected boolean isActiveReminder(String reminderID) {
        SQLiteDatabase db;
        DatabaseHelper dbHelper;

        dbHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд в виде курсора
        Cursor query = db.rawQuery(String.format(
                "select %s from %s where _id=?;",
                DatabaseHelper.REM_COLUMN_IS_ACTIVE,
                DatabaseHelper.TABLE_REMINDERS
        ), new String[]{reminderID});

        String isActive = "F";
        if (query.moveToFirst()) {
            isActive = query.getString(query.getColumnIndex(DatabaseHelper.REM_COLUMN_IS_ACTIVE));
            Log.w(LOG_TAG, String.format("isActiveReminder - isActive: %s; reminderID: %s;", isActive, reminderID));
        }

        db.close();
        query.close();
        Log.w(LOG_TAG, String.format("isActiveReminder - isActive: %s; reminderID: %s;", isActive, reminderID));
        return isActive.equals("T");
    }
}
