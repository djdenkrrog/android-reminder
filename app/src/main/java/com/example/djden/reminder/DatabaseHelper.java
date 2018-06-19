package com.example.djden.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int SCHEMA = 1; // версия базы данных
    private final static String TAG = "DatabaseHelper";
    public static final String DATABASE_NAME = "appReminder.db"; // название бд

    static final String TABLE_REMINDERS = "reminders"; // название таблицы в бд
    // названия столбцов
    static final String REM_COLUMN_ID = "_id";
    static final String REM_COLUMN_MSG = "message";
    static final String REM_COLUMN_D_START = "d_start";
    static final String REM_COLUMN_D_START_FRMT = "d_start_frmt";
    static final String REM_COLUMN_REPEAT = "repeat";
    static final String REM_COLUMN_IS_SOUND = "is_sound";
    static final String REM_COLUMN_IS_ACTIVE = "is_active";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
        ContentValues newValues = new ContentValues();

        /*Tables create*/
        //TABLE_WALLETS
        db.execSQL("CREATE TABLE " + TABLE_REMINDERS
                + " ("
                + REM_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + REM_COLUMN_MSG + " TEXT,"
                + REM_COLUMN_D_START + " DATETIME, "
                + REM_COLUMN_D_START_FRMT + " TEXT, "
                + REM_COLUMN_REPEAT + " TEXT DEFAULT 'NONE', "
                + REM_COLUMN_IS_SOUND + " TEXT DEFAULT 'T', "
                + REM_COLUMN_IS_ACTIVE + " TEXT DEFAULT 'T' "
                + ");");

        createView(db);
    }

    protected void createView(SQLiteDatabase db) {
        db.execSQL(String.format("DROP VIEW IF EXISTS v_%s", TABLE_REMINDERS));

        /*Views create*/
        db.execSQL(String.format(
                "CREATE VIEW v_%s AS SELECT t.* FROM %s t;",
                TABLE_REMINDERS,
                TABLE_REMINDERS
        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "oldVersion: " + oldVersion + "; newVersion: " + newVersion);

        if (newVersion == 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);

            onCreate(db);
        }
        createView(db);
    }
}
