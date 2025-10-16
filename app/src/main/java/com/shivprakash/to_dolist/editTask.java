package com.shivprakash.to_dolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class editTask extends AppCompatActivity {

    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private Spinner categorySpinner;
    private Spinner prioritySpinner;
    private EditText notesEditText;
    private TextView taskTitle;
    private TaskDBHelper dbHelper;
    private Calendar selectedDateTime;
    private String originalTaskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        originalTaskName = intent.getStringExtra("task");

        taskTitle = findViewById(R.id.text_view_task);
        selectedDateTextView = findViewById(R.id.selected_date_text_view);
        selectedTimeTextView = findViewById(R.id.selected_time_text_view);
        categorySpinner = findViewById(R.id.category_spinner);
        prioritySpinner = findViewById(R.id.priority_spinner);
        notesEditText = findViewById(R.id.notes_edit_text);
        Button selectDateButton = findViewById(R.id.button_select_due_date);
        Button selectTimeButton = findViewById(R.id.button_select_due_time);
        Button saveButton = findViewById(R.id.button_add_task);

        dbHelper = new TaskDBHelper(this);
        selectedDateTime = Calendar.getInstance();

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories_array,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.priorities_array,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        if (originalTaskName != null) {
            taskTitle.setText(getString(R.string.task_label_with_name, originalTaskName));
            loadExistingTask();
        }

        updateDateAndTimeTextViews();

        selectDateButton.setOnClickListener(view -> showDatePickerDialog());
        selectTimeButton.setOnClickListener(view -> showTimePickerDialog());
        saveButton.setOnClickListener(view -> updateTask());
    }

    private void loadExistingTask() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                null,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?",
                new String[]{originalTaskName},
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_CATEGORY));
            String priority = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_PRIORITY));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NOTES));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            String dueTime = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_TIME));

            notesEditText.setText(notes);
            setSpinnerSelection(categorySpinner, category);
            setSpinnerSelection(prioritySpinner, priority);
            parseExistingDateTime(dueDate, dueTime);
        }
        cursor.close();
        db.close();
    }

    private void parseExistingDateTime(String dueDate, String dueTime) {
        if (dueDate != null && dueDate.contains("/")) {
            String[] dateParts = dueDate.split("/");
            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
            }
        }
        if (dueTime != null && dueTime.contains(":")) {
            String[] timeParts = dueTime.split(":");
            if (timeParts.length >= 2) {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateDateAndTimeTextViews() {
        String dateString = String.format(Locale.getDefault(), "%02d/%02d/%d",
                selectedDateTime.get(Calendar.DAY_OF_MONTH),
                selectedDateTime.get(Calendar.MONTH) + 1,
                selectedDateTime.get(Calendar.YEAR));
        selectedDateTextView.setText(dateString);

        String timeString = String.format(Locale.getDefault(), "%02d:%02d",
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE));
        selectedTimeTextView.setText(timeString);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateAndTimeTextViews();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (TimePicker view, int hourOfDay, int minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    updateDateAndTimeTextViews();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void updateTask() {
        if (originalTaskName == null) {
            finish();
            return;
        }
        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        String dueDate = selectedDateTextView.getText().toString().trim();
        String dueTime = selectedTimeTextView.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_CATEGORY, category);
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, priority);
        values.put(TaskContract.TaskEntry.COLUMN_NOTES, notes);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, dueDate);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_TIME, dueTime);
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 0);

        int rowsUpdated = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?",
                new String[]{originalTaskName}
        );
        db.close();

        if (rowsUpdated > 0) {
            Toast.makeText(this, R.string.task_updated_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error_update_task, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
