package com.example.android.productivityladder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.productivityladder.data.WorkContract.ReminderEntry;

public class ReminderCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ReminderCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ReminderCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item_reminder.xmlinder.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item_reminder, parent, false);
    }

    /**
     * This method binds the reminder data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current reminder can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        // Find the columns of reminder attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_NAME);
        int hoursColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TIME_HOURS);
        int minutesColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TIME_MINUTES);
        // Read the reminder attributes from the Cursor for the current reminder
        String reminderName = cursor.getString(nameColumnIndex);
        String displayHours = cursor.getString(hoursColumnIndex);
        String displayMinutes = cursor.getString(minutesColumnIndex);
        if(Integer.parseInt(displayMinutes) < 10)
            displayMinutes = "0" + displayMinutes;
        String reminderTime = displayHours + ":" + displayMinutes;

        // Update the TextViews with the attributes for the current reminder
        nameTextView.setText(reminderName);
        summaryTextView.setText(reminderTime);
    }
}