package com.example.android.productivityladder;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.productivityladder.data.WorkContract.TaskEntry;

public class TaskCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link TaskCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public TaskCursorAdapter(Context context, Cursor c) {
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
        // Inflate a list item view using the layout specified in list_item_task.xmlinder.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false);
    }

    /**
     * This method binds the task data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the title for the current task can be set on the title TextView
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
        TextView titleTextView = (TextView) view.findViewById(R.id.task_title);
        TextView detailsTextView = (TextView) view.findViewById(R.id.task_details);
        // Find the columns of task attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int detailsColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DETAILS);
        // Read the task attributes from the Cursor for the current task
        String taskTitle = cursor.getString(titleColumnIndex);
        String taskDetails = cursor.getString(detailsColumnIndex);

        // If the task details is empty string or null, then use some default text or blank
        if (TextUtils.isEmpty(taskDetails)) {
            taskDetails = context.getString(R.string.unknown_task);
        }
        // Update the TextViews with the attributes for the current task
        titleTextView.setText(taskTitle);
        detailsTextView.setText(taskDetails);
    }
}
