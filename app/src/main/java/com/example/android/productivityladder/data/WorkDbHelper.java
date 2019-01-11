package com.example.android.productivityladder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.productivityladder.data.WorkContract.ReminderEntry;
import com.example.android.productivityladder.data.WorkContract.TaskEntry;

public class WorkDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "work_manager.db";
    private static final int DATABASE_VERSION = 4;

    public WorkDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_REMINDERS_TABLE = "CREATE TABLE " + ReminderEntry.TABLE_NAME + "("
                + ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReminderEntry.COLUMN_REMINDER_NAME + " TEXT NOT NULL, "
                + ReminderEntry.COLUMN_REMINDER_DETAILS + " TEXT, "
                + ReminderEntry.COLUMN_TIME_HOURS + " INTEGER NOT NULL, "
                + ReminderEntry.COLUMN_TIME_MINUTES + " INTEGER NOT NULL);";

        String SQL_CREATE_TASKS_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + "("
                + TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TaskEntry.COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                + TaskEntry.COLUMN_TASK_DETAILS + " TEXT, "
                + TaskEntry.COLUMN_TASK_STATUS + " INTEGER NOT NULL);";

        db.execSQL(SQL_CREATE_REMINDERS_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS '" + ReminderEntry.TABLE_NAME + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TaskEntry.TABLE_NAME + "'");
        onCreate(db);
    }
}