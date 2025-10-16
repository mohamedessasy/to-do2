package com.shivprakash.to_dolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskDBHelper dbHelper;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new TaskDBHelper(this);
        adapter = new TaskAdapter(this, new ArrayList<>());

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Data task = adapter.getItem(position);
                if (task == null) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, editTask.class);
                intent.putExtra("task", task.getName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                if (removeTask(position)) {
                    Toast.makeText(MainActivity.this, R.string.task_deleted_message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCheckboxClick(int position) {
                if (removeTask(position)) {
                    Toast.makeText(MainActivity.this, R.string.task_completed_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void refreshTasks() {
        List<Data> tasks = new ArrayList<>();
        loadTasksFromSQLite(tasks);
        adapter.setData(tasks);
    }

    private boolean removeTask(int position) {
        Data task = adapter.getItem(position);
        if (task == null) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?",
                new String[]{task.getName()});
        db.close();
        adapter.removeTaskAt(position);
        return true;
    }

    public void loadTasksFromSQLite(@NonNull List<Data> data) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE_NAME, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK));
            @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            @SuppressLint("Range") String taskTime = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY));
            @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            @SuppressLint("Range") String notes = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NOTES));
            @SuppressLint("Range") int completed = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_COMPLETED));

            data.add(new Data(taskName, taskDate, taskTime, category, priority, notes, completed == 1));
        }

        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_reports) {
            startActivity(new Intent(this, ReportActivity.class));
            return true;
        } else if (id == R.id.action_sort_priority) {
            adapter.sortByPriority();
            return true;
        } else if (id == R.id.action_sort_due) {
            adapter.sortByDue();
            return true;
        } else if (id == R.id.action_filter_today) {
            adapter.filterToday();
            return true;
        } else if (id == R.id.action_filter_all) {
            adapter.clearFilter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class Data {
        private final String name;
        private final String date;
        private final String time;
        private final String category;
        private final String priority;
        private final String notes;
        private final boolean completed;

        Data(String name, String date, String time, String category, String priority, String notes, boolean completed) {
            this.name = name;
            this.date = date;
            this.time = time;
            this.category = category;
            this.priority = priority;
            this.notes = notes;
            this.completed = completed;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public String getNotes() {
            return notes;
        }

        public boolean isCompleted() {
            return completed;
        }
    }
}
