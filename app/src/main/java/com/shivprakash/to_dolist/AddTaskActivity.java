package com.shivprakash.to_dolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
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

public class AddTaskActivity extends AppCompatActivity {

    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private EditText taskEditText;
    private Spinner categorySpinner;
    private Spinner prioritySpinner;
    private EditText notesEditText;
    private TaskDBHelper dbHelper;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        selectedDateTextView = findViewById(R.id.selected_date_text_view);
        selectedTimeTextView = findViewById(R.id.selected_time_text_view);
        taskEditText = findViewById(R.id.task_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        prioritySpinner = findViewById(R.id.priority_spinner);
        notesEditText = findViewById(R.id.notes_edit_text);
        Button selectDateButton = findViewById(R.id.button_select_due_date);
        Button selectTimeButton = findViewById(R.id.button_select_due_time);
        Button addTaskButton = findViewById(R.id.button_add_task);

        selectedDateTime = Calendar.getInstance();
        dbHelper = new TaskDBHelper(this);

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

        updateDateAndTimeTextViews();

        selectDateButton.setOnClickListener(view -> showDatePickerDialog());
        selectTimeButton.setOnClickListener(view -> showTimePickerDialog());
        addTaskButton.setOnClickListener(view -> addTask());
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

    private void addTask() {
        String task = taskEditText.getText().toString().trim();
        if (task.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_task, Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        String dueDate = selectedDateTextView.getText().toString().trim();
        String dueTime = selectedTimeTextView.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK, task);
        values.put(TaskContract.TaskEntry.COLUMN_CATEGORY, category);
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, priority);
        values.put(TaskContract.TaskEntry.COLUMN_NOTES, notes);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, dueDate);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_TIME, dueTime);
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 0);

        long newRowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        db.close();
        if (newRowId == -1) {
            Toast.makeText(this, R.string.error_add_task, Toast.LENGTH_SHORT).show();
            return;
        }

        scheduleReminder(task, notes);
        Toast.makeText(this, R.string.task_added_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleReminder(String title, String text) {
        long triggerAtMillis = selectedDateTime.getTimeInMillis();
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}
