package com.example.android.productivityladder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android.productivityladder.data.WorkContract;
import com.example.android.productivityladder.data.WorkDbHelper;
import java.util.Calendar;
import static android.content.ContentValues.TAG;

public class AlarmService extends IntentService {

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";

    private IntentFilter matcher;

    public AlarmService() {
        super(TAG);
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
        //matcher.addAction(CANCEL_ALL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String notificationId = intent.getStringExtra("notificationId");

        if (matcher.matchAction(action)) {
            execute(action, notificationId);
        }
    }

    private void execute(String action, String notificationId) {

        WorkDbHelper mDbHelper = new WorkDbHelper(AlarmService.this);
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Cursor cursor = null;
        try {
            cursor = database.query(WorkContract.ReminderEntry.TABLE_NAME, null, "_id = ?",
                    new String[]{notificationId}, null, null, null);
        } catch(Exception e) {

        }

        if(CREATE.equals(action)) {
            if (cursor.moveToFirst()) {
                Intent i = new Intent(this, AlarmReceiver.class);
                i.putExtra("id", Long.parseLong(notificationId));
                i.putExtra("name", cursor.getString(cursor.getColumnIndex(WorkContract.ReminderEntry.COLUMN_REMINDER_NAME)));
                i.putExtra("details", cursor.getString(cursor.getColumnIndex(WorkContract.ReminderEntry.COLUMN_REMINDER_DETAILS)));

                PendingIntent pi = PendingIntent.getBroadcast(this, Integer.parseInt(notificationId),
                        i, PendingIntent.FLAG_UPDATE_CURRENT);
                int hours = cursor.getInt(cursor.getColumnIndex(WorkContract.ReminderEntry.COLUMN_TIME_HOURS));
                int minutes = cursor.getInt(cursor.getColumnIndex(WorkContract.ReminderEntry.COLUMN_TIME_MINUTES));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);
                //long time = calendar.getTimeInMillis() - System.currentTimeMillis();
                am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            }
        } else if (CANCEL.equals(action)) {
            Intent i = new Intent(this, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, Integer.parseInt(notificationId),
                    i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(pi);
        }
        cursor.close();
        mDbHelper.close();
    }
}