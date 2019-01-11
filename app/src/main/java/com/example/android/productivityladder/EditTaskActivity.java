package com.example.android.productivityladder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.productivityladder.data.WorkContract.TaskEntry;

public class EditTaskActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the task data loader */
    private static final int EXISTING_TASK_LOADER = 1;
    /** Content URI for the existing task (null if it's a new task) */
    private Uri mCurrentTaskUri;

    /** EditText field to enter the task's title */
    private EditText mTitleEditText;

    /** EditText field to enter the task's details */
    private EditText mDetailsEditText;

    /** EditText field to enter the task's status */
    private Spinner mStatusSpinner;

    /**
     * Status of the task. The possible valid values are in the WorkContract.java file:
     * {@link TaskEntry#STATUS_NOT_STARTED}, {@link TaskEntry#STATUS_IN_PROGRESS}, or
     * {@link TaskEntry#STATUS_COMPLETED}.
     */
    private int mStatus = TaskEntry.STATUS_NOT_STARTED;

    /** Boolean flag that keeps track of whether the task has been edited (true) or not (false) */
    private boolean mTaskHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mTaskHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTaskHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        // Examine the intent that was used to launch this activity
        // in order to figure out if we're creating a new task or editing an existing one
        Intent intent = getIntent();
        mCurrentTaskUri = intent.getData();
        //If the intent DOES NOT contain a task content URI, then we know that we are creating a new task
        if(mCurrentTaskUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_task));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a task that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        else {
            setTitle(getString(R.string.editor_activity_title_edit_task));
        }
        // Initialize a loader to read the task data from the database
        // and display the current values in the editor
        getSupportLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.edit_task_title);
        mDetailsEditText = (EditText) findViewById(R.id.edit_task_details);
        mStatusSpinner = (Spinner) findViewById(R.id.spinner_status);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mDetailsEditText.setOnTouchListener(mTouchListener);
        mStatusSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the status of the task.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter statusSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mStatusSpinner.setAdapter(statusSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.status_in_progress))) {
                        mStatus = TaskEntry.STATUS_IN_PROGRESS;
                    } else if (selection.equals(getString(R.string.status_completed))) {
                        mStatus = TaskEntry.STATUS_COMPLETED;
                    } else {
                        mStatus = TaskEntry.STATUS_NOT_STARTED;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mStatus = TaskEntry.STATUS_NOT_STARTED;
            }
        });
    }

    /**
     * Get user input from editor and save new task into database.
     */
    private void saveTask() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        String detailsString = mDetailsEditText.getText().toString().trim();
        // Check if this is supposed to be a new task
        // and check if all the fields in the editor are blank
        if (mCurrentTaskUri == null && TextUtils.isEmpty(titleString)
                && TextUtils.isEmpty(detailsString) && mStatus == TaskEntry.STATUS_NOT_STARTED) {
            // Since no fields were modified, we can return early without creating a new task.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and task attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_TITLE, titleString);
        values.put(TaskEntry.COLUMN_TASK_DETAILS, detailsString);
        values.put(TaskEntry.COLUMN_TASK_STATUS, mStatus);

        // Determine if this is a new or existing task by checking if mCurrentTaskUri is null or not
        if (mCurrentTaskUri == null) {
            // This is a NEW task, so insert a new task into the provider,
            // returning the content URI for the new task.
            Uri newUri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING task, so update the task with content URI: mCurrenttaskUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrenttaskUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentTaskUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_task, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new task, hide the "Delete" menu item.
        if (mCurrentTaskUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save task to database
                saveTask();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the task hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mTaskHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditTaskActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditTaskActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all task attributes, define a projection that contains
        // all columns from the task table
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_TITLE,
                TaskEntry.COLUMN_TASK_DETAILS,
                TaskEntry.COLUMN_TASK_STATUS };
        if(mCurrentTaskUri!=null)
            // This loader will execute the ContentProvider's query method on a background thread
            return new android.support.v4.content.CursorLoader(this,   // Parent activity context
                    mCurrentTaskUri,         // Query the content URI for the current task
                    projection,             // Columns to include in the resulting Cursor
                    null,          // No selection clause
                    null,       // No selection arguments
                    null);         // Default sort order
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of task attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
            int detailsColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DETAILS);
            int statusColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_STATUS);
            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String details = cursor.getString(detailsColumnIndex);
            int status = cursor.getInt(statusColumnIndex);
            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mDetailsEditText.setText(details);
            switch (status) {
                case TaskEntry.STATUS_IN_PROGRESS:
                    mStatusSpinner.setSelection(1);
                    break;
                case TaskEntry.STATUS_COMPLETED:
                    mStatusSpinner.setSelection(2);
                    break;
                default:
                    mStatusSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mDetailsEditText.setText("");
        mStatusSpinner.setSelection(0); // Select NOT_STARTED
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the task hasn't changed, continue with handling back button press
        if (!mTaskHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the task.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }});
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this task.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_task);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the task.
                deleteTask();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the task.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTask() {
        // Only perform the delete if this is an existing task.
        if (mCurrentTaskUri != null) {
            // Call the ContentResolver to delete the task at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentTaskUri
            // content URI already identifies the task that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTaskUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

}