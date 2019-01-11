package com.example.android.productivityladder.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.productivityladder.data.WorkContract.ReminderEntry;
import com.example.android.productivityladder.data.WorkContract.TaskEntry;

/**
 * {@link ContentProvider} for ProductivityLadder app.
 */
public class WorkProvider extends ContentProvider {

    /** URI matcher code for the content URI for the reminders table */
    private static final int REMINDERS = 1;

    /** URI matcher code for the content URI for a single reminder in the reminders table */
    private static final int REMINDER_ID = 2;

    /** URI matcher code for the content URI for the tasks table */
    private static final int TASKS = 3;

    /** URI matcher code for the content URI for a task in the tasks table */
    private static final int TASK_ID = 4;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(WorkContract.CONTENT_AUTHORITY, WorkContract.PATH_REMINDERS, REMINDERS);
        sUriMatcher.addURI(WorkContract.CONTENT_AUTHORITY, WorkContract.PATH_REMINDERS + "/#", REMINDER_ID);
        sUriMatcher.addURI(WorkContract.CONTENT_AUTHORITY, WorkContract.PATH_TASKS, TASKS);
        sUriMatcher.addURI(WorkContract.CONTENT_AUTHORITY, WorkContract.PATH_TASKS + "/#", TASK_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = WorkProvider.class.getSimpleName();

    private WorkDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new WorkDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case REMINDERS:
                cursor = database.query(ReminderEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REMINDER_ID:
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ReminderEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TASKS:
                cursor = database.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor, so we know what content URI the Cursor was created for
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return insertReminder(uri, contentValues);
            case TASKS:
                return insertTask(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        //return null;
    }

    /**
     * Insert a reminder into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertReminder(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(ReminderEntry.COLUMN_REMINDER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Reminder requires a name");
        }

        Integer hours = values.getAsInteger(ReminderEntry.COLUMN_TIME_HOURS);
        if (hours == null) {
            throw new IllegalArgumentException("Reminder requires setting time");
        }

        Integer minutes = values.getAsInteger(ReminderEntry.COLUMN_TIME_MINUTES);
        if (minutes == null) {
            throw new IllegalArgumentException("Reminder requires setting time");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new reminder with the given values
        long id = database.insert(ReminderEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertTask(Uri uri, ContentValues values) {

        // Check that the title is not null
        String title = values.getAsString(TaskEntry.COLUMN_TASK_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Task title required");
        }

        Integer status = values.getAsInteger(TaskEntry.COLUMN_TASK_STATUS);
        if (status == null || !TaskEntry.isValidStatus(status)) {
            throw new IllegalArgumentException("Task requires valid status");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new task with the given values
        long id = database.insert(TaskEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return updateReminder(uri, contentValues, selection, selectionArgs);
            case REMINDER_ID:
                // For the REMINDER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateReminder(uri, contentValues, selection, selectionArgs);
            case TASKS:
                return updateTask(uri, contentValues, selection, selectionArgs);
            case TASK_ID:
                // For the TASK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTask(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateReminder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ReminderEntry#COLUMN_TASK_TITLE} key is present,
        // check that the name value is not null.
        if (values.containsKey(ReminderEntry.COLUMN_REMINDER_NAME)) {
            String name = values.getAsString(ReminderEntry.COLUMN_REMINDER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Reminder requires a name");
            }
        }

       // No need to check the details, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ReminderEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }


        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    private int updateTask(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link TaskEntry#COLUMN_TASK_TITLE} key is present,
        // check that the title value is not null.
        if (values.containsKey(TaskEntry.COLUMN_TASK_TITLE)) {
            String title = values.getAsString(TaskEntry.COLUMN_TASK_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Task title required");
            }
        }

        // If the {@link TaskEntry#COLUMN_TASK_STATUS} key is present,
        // check that the status value is valid.
        if (values.containsKey(TaskEntry.COLUMN_TASK_STATUS)) {
            Integer status = values.getAsInteger(TaskEntry.COLUMN_TASK_STATUS);
            if (status == null || !TaskEntry.isValidStatus(status)) {
                throw new IllegalArgumentException("Task re");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }


        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REMINDERS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REMINDER_ID:
                // Delete a single row given by the ID in the URI
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                // Delete a single row given by the ID in the URI
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return ReminderEntry.CONTENT_LIST_TYPE;
            case REMINDER_ID:
                return ReminderEntry.CONTENT_ITEM_TYPE;
            case TASKS:
                return TaskEntry.CONTENT_LIST_TYPE;
            case TASK_ID:
                return TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}