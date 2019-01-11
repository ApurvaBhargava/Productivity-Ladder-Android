package com.example.android.productivityladder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import com.example.android.productivityladder.data.WorkContract.ReminderEntry;
import static com.example.android.productivityladder.AlarmService.CREATE;

public class ReminderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REMINDER_LOADER = 0;
    ReminderCursorAdapter mReminderCursorAdapter;

    public ReminderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        // Find the ListView which will be populated with the reminder data
        ListView reminderListView = (ListView) view.findViewById(R.id.list_reminder);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = view.findViewById(R.id.empty_view);
        reminderListView.setEmptyView(emptyView);
        mReminderCursorAdapter = new ReminderCursorAdapter(this.getActivity(), null);
        // Attach the adapter to the ListView.
        reminderListView.setAdapter(mReminderCursorAdapter);
        // Setup item click listener
        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(ReminderFragment.this.getActivity(), EditReminderActivity.class);
                // Form the content URI that represents the specific reminder that was clicked on,
                // by appending the "id" (passes as input to this method) onto the {@link ReminderEntry#CONTENT_URI}
                Uri currentReminderUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, id);
                //Set the URI on the data field of the intent
                intent.setData(currentReminderUri);
                //Launch the {@link EditorActivity} to display the data for the current reminder.
                startActivity(intent);
            }
        });
        // Kick off the loader
        getActivity().getLoaderManager().initLoader(REMINDER_LOADER, null, this);

        return view;
    }

    /**
     * Helper method to insert hardcoded reminder data into the database. For debugging purposes only.
     */
    private void insertReminder() {
        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_REMINDER_NAME, "Sleep");
        values.put(ReminderEntry.COLUMN_REMINDER_DETAILS, "Meditate and sleep");
        values.put(ReminderEntry.COLUMN_TIME_HOURS, new Integer(23));
        values.put(ReminderEntry.COLUMN_TIME_MINUTES, new Integer(30));

        // Insert a new row for reminder into the provider using the ContentResolver.
        // Use the {@link ReminderEntry#CONTENT_URI} to indicate that we want to insert
        // into the reminders database table.
        // Receive the new content URI that will allow us to access reminder's data in the future.
        Uri newUri = getActivity().getContentResolver().insert(ReminderEntry.CONTENT_URI, values);
        Intent sendIntent = new Intent(ReminderFragment.this.getActivity(), AlarmService.class);
        sendIntent.setAction(CREATE);
        sendIntent.putExtra("notificationId", Long.toString(ContentUris.parseId(newUri)));
        startService(sendIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.menu_reminder, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertReminder();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            /*
            case R.id.action_delete_all_entries:
                deleteAllReminders();
                return true;
            */
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_REMINDER_NAME,
                ReminderEntry.COLUMN_REMINDER_DETAILS,
                ReminderEntry.COLUMN_TIME_HOURS,
                ReminderEntry.COLUMN_TIME_MINUTES};
        return new CursorLoader(this.getActivity(), ReminderEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mReminderCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReminderCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all reminders in the database.
     */
    /*
    private void deleteAllReminders() {

        int rowsDeleted = getActivity().getContentResolver().delete(ReminderEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " row(s) deleted from database");
    }
    */

    private void startService(Intent sendIntent) {
    }
}