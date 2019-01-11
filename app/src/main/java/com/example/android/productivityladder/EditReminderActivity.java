package com.example.android.productivityladder;

import android.content.ContentUris;
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
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.android.productivityladder.data.WorkContract.ReminderEntry;
import static com.example.android.productivityladder.AlarmService.CANCEL;
import static com.example.android.productivityladder.AlarmService.CREATE;

public class EditReminderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the reminder data loader */
    private static final int EXISTING_REMINDER_LOADER = 0;
    /** Content URI for the existing reminder (null if it's a new reminder) */
    private Uri mCurrentReminderUri;

    /** EditText field to enter the reminder's name */
    private EditText mNameEditText;

    /** EditText field to enter the reminder's details */
    private EditText mDetailsEditText;

    /** EditText field to enter the reminder's time */
    private TimePicker mReminderTimePicker;

    /** Boolean flag that keeps track of whether the reminder has been edited (true) or not (false) */
    private boolean mReminderHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mReminderHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mReminderHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);
        // Examine the intent that was used to launch this activity
        // in order to figure out if we're creating a new reminder or editing an existing one
        Intent intent = getIntent();
        mCurrentReminderUri = intent.getData();
        //If the intent DOES NOT contain a reminder content URI, then we know that we are creating a new reminder
        if(mCurrentReminderUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_reminder));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a reminder that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        else {
            setTitle(getString(R.string.editor_activity_title_edit_reminder));
        }
        // Initialize a loader to read the reminder data from the database
        // and display the current values in the editor
        getSupportLoaderManager().initLoader(EXISTING_REMINDER_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_reminder_name);
        mDetailsEditText = (EditText) findViewById(R.id.edit_reminder_details);
        mReminderTimePicker = (TimePicker) findViewById(R.id.edit_time);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDetailsEditText.setOnTouchListener(mTouchListener);
        mReminderTimePicker.setOnTouchListener(mTouchListener);

    }


    /**
     * Get user input from editor and save new reminder into database.
     */
    private void saveReminder() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String detailsString = mDetailsEditText.getText().toString().trim();
        Integer hoursInteger = mReminderTimePicker.getCurrentHour();
        Integer minutesInteger = mReminderTimePicker.getCurrentMinute();

        // Check if this is supposed to be a new reminder
        // and check if all the fields in the editor are blank
        if (mCurrentReminderUri == null && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(detailsString)) {
            // Since no fields were modified, we can return early without creating a new reminder.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and reminder attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_REMINDER_NAME, nameString);
        values.put(ReminderEntry.COLUMN_REMINDER_DETAILS, detailsString);
        values.put(ReminderEntry.COLUMN_TIME_HOURS, hoursInteger);
        values.put(ReminderEntry.COLUMN_TIME_MINUTES, minutesInteger);

        // Determine if this is a new or existing reminder by checking if mCurrentReminderUri is null or not
        if (mCurrentReminderUri == null) {
            // This is a NEW reminder, so insert a new reminder into the provider,
            // returning the content URI for the new reminder.
            Uri newUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_reminder_successful),
                        Toast.LENGTH_SHORT).show();
                Intent sendIntent = new Intent(EditReminderActivity.this, AlarmService.class);
                sendIntent.setAction(CREATE);
                sendIntent.putExtra("notificationId", Long.toString(ContentUris.parseId(newUri)));
                startService(sendIntent);
            }
        } else {
            // Otherwise this is an EXISTING reminder, so update the reminder with content URI: mCurrentReminderUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentReminderUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentReminderUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
            Intent sendIntent = new Intent(EditReminderActivity.this, AlarmService.class);
            sendIntent.setAction(CREATE);
            sendIntent.putExtra("notificationId", Long.toString(ContentUris.parseId(mCurrentReminderUri)));
            startService(sendIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_reminder, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new reminder, hide the "Delete" menu item.
        if (mCurrentReminderUri == null) {
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
                // Save reminder to database
                saveReminder();
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
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mReminderHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditReminderActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditReminderActivity.this);
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
        // Since the editor shows all reminder attributes, define a projection that contains
        // all columns from the reminder table
        String[] projection = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_REMINDER_NAME,
                ReminderEntry.COLUMN_REMINDER_DETAILS,
                ReminderEntry.COLUMN_TIME_MINUTES,
                ReminderEntry.COLUMN_TIME_HOURS };
        if(mCurrentReminderUri!=null)
        // This loader will execute the ContentProvider's query method on a background thread
        return new android.support.v4.content.CursorLoader(this,   // Parent activity context
                mCurrentReminderUri,         // Query the content URI for the current reminder
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
            // Find the columns of reminder attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_NAME);
            int detailsColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_DETAILS);
            int hoursColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TIME_HOURS);
            int minutesColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_TIME_MINUTES);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String details = cursor.getString(detailsColumnIndex);
            Integer hours = cursor.getInt(hoursColumnIndex);
            Integer minutes = cursor.getInt(minutesColumnIndex);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDetailsEditText.setText(details);
            mReminderTimePicker.setCurrentHour(hours);
            mReminderTimePicker.setCurrentMinute(minutes);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDetailsEditText.setText("");
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the reminder hasn't changed, continue with handling back button press
        if (!mReminderHasChanged) {
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
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }});
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this reminder.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the reminder.
                deleteReminder();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteReminder() {
        // Only perform the delete if this is an existing reminder.
        if (mCurrentReminderUri != null) {
            long id = ContentUris.parseId(mCurrentReminderUri);
            // Call the ContentResolver to delete the reminder at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentReminderUri
            // content URI already identifies the reminder that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentReminderUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_successful),
                        Toast.LENGTH_SHORT).show();
                Intent sendIntent = new Intent(EditReminderActivity.this, AlarmService.class);
                sendIntent.setAction(CANCEL);
                sendIntent.putExtra("notificationId", Long.toString(id));
                startService(sendIntent);
            }
        }
        // Close the activity
        finish();
    }

}