package com.example.android.productivityladder.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class WorkContract {

    private WorkContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.productivityladder";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_REMINDERS = "reminders";
    public static final String PATH_TASKS = "tasks";

    /**
     * Inner class that defines constant values for the reminders database table.
     * Each entry in the table represents a single reminder.
     */
    public static final class ReminderEntry implements BaseColumns {

        /** The content URI to access the reminder data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REMINDERS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of reminders.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single reminder.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDERS;

        /** Name of database table for reminders */
        public final static String TABLE_NAME = "reminders";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_REMINDER_NAME = "name";
        public final static String COLUMN_REMINDER_DETAILS = "details";
        public final static String COLUMN_TIME_HOURS = "hours";
        public final static String COLUMN_TIME_MINUTES = "minutes";
    }

    /**
     * Inner class that defines constant values for the tasks database table.
     * Each entry in the table represents a single task.
     */
    public static final class TaskEntry implements BaseColumns {

        /** The content URI to access the task data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TASKS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of tasks.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single task.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;

        /** Name of database table for tasks */
        public final static String TABLE_NAME = "tasks";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TASK_TITLE = "title";
        public final static String COLUMN_TASK_DETAILS = "details";
        public final static String COLUMN_TASK_STATUS = "status";

        public static final int STATUS_NOT_STARTED = 0;
        public static final int STATUS_IN_PROGRESS = 1;
        public static final int STATUS_COMPLETED = 2;

        public static boolean isValidStatus(int status) {
            if (status == STATUS_NOT_STARTED || status == STATUS_IN_PROGRESS || status == STATUS_COMPLETED) {
                return true;
            }
            return false;
        }
    }
}

