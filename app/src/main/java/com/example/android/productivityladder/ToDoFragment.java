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
import android.util.Log;

import com.example.android.productivityladder.data.WorkContract.TaskEntry;

public class ToDoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 1;
    TaskCursorAdapter mTaskCursorAdapter;

    public ToDoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        // Find the ListView which will be populated with the task data
        ListView taskListView = (ListView) view.findViewById(R.id.list_todo);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = view.findViewById(R.id.empty_view);
        taskListView.setEmptyView(emptyView);
        mTaskCursorAdapter = new TaskCursorAdapter(this.getActivity(), null);
        // Attach the adapter to the ListView.
        taskListView.setAdapter(mTaskCursorAdapter);
        // Setup item click listener
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(ToDoFragment.this.getActivity(), EditTaskActivity.class);
                // Form the content URI that represents the specific task that was clicked on,
                // by appending the "id" (passes as input to this method) onto the {@link TaskEntry#CONTENT_URI}
                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);
                //Set the URI on the data field of the intent
                intent.setData(currentTaskUri);
                //Launch the {@link EditorActivity} to display the data for the current task.
                startActivity(intent);
            }
        });
        // Kick off the loader
        getActivity().getLoaderManager().initLoader(TASK_LOADER, null, this);

        return view;
    }

    /**
     * Helper method to insert hardcoded task data into the database. For debugging purposes only.
     */
    private void insertTask() {
        // Create a ContentValues object where column names are the keys,
        // and task's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_TITLE, "Sample task title");
        values.put(TaskEntry.COLUMN_TASK_DETAILS, "Details of task");
        values.put(TaskEntry.COLUMN_TASK_STATUS, TaskEntry.STATUS_NOT_STARTED);

        Uri newUri = getActivity().getContentResolver().insert(TaskEntry.CONTENT_URI, values);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.menu_task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertTask();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllTasks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selectionArgs[] = {"0"};
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_TITLE,
                TaskEntry.COLUMN_TASK_DETAILS};
        return new CursorLoader(this.getActivity(), TaskEntry.CONTENT_URI, projection,
                TaskEntry.COLUMN_TASK_STATUS + " = ?", selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTaskCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all tasks in the database.
     */
    private void deleteAllTasks() {
        int rowsDeleted = getActivity().getContentResolver().delete(TaskEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from database");
    }
}