package com.shivprakash.to_dolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ScrollView taskScrollView;
    private ArrayAdapter<String> taskAdapter;
    private TaskDBHelper dbHelper;
    private List<Data> taskData;
    FloatingActionButton fabAddTask;
    RecyclerView recyclerView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskData=new ArrayList<>();
        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        fabAddTask = findViewById(R.id.fab_add_task);
        dbHelper = new TaskDBHelper(this);
        loadTasksFromSQLite(taskData);
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskData);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Intent intent = new Intent(MainActivity.this, editTask.class);
                intent.putExtra("task", taskData.get(position).getName());
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "Edit clicked at position " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(int position) {

                markTaskAsComplete(position);
                taskData.remove(position);
                Toast.makeText(MainActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onCheckboxClick(int position) {

                markTaskAsComplete(position);
                taskData.remove(position);
                Toast.makeText(MainActivity.this, "Task Completed", Toast.LENGTH_SHORT).show();
                adapter.notifyItemRemoved(position);
            }
        });

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

    }
    public void loadTasksFromSQLite(List<Data> data) {
        // Assuming you have a SQLiteOpenHelper instance named dbHelper
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TaskContract.TaskEntry.TABLE_NAME, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK));
            @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            @SuppressLint("Range") String taskTime = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY));
            @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            @SuppressLint("Range") String notes = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NOTES));

            data.add(new Data(taskName,taskDate,taskTime,category,priority,notes));
            //Toast.makeText(this, "added", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
    public void markTaskAsComplete(int position) {
        String task = taskData.get(position).getName();
        //Toast.makeText(this, task, Toast.LENGTH_SHORT).show();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 1);
        db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?", new String[]{task});
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
    public class Data{
        String name;
        String date;
        String time;
        String category;
        String priority;
        String notes;
        Data(String name, String date, String time, String category, String priority, String notes) {
            this.name = name;
            this.date = date;
            this.time = time;
            this.category = category;
            this.priority = priority;
            this.notes = notes;
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
    }


}
@Override
public boolean onCreateOptionsMenu(android.view.Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    android.view.MenuItem searchItem = menu.findItem(R.id.action_search);
    androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
    searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
        @Override public boolean onQueryTextSubmit(String query) { return false; }
        @Override public boolean onQueryTextChange(String newText) {
            // naive: if adapter has filter implement; else refresh list via DB
            if (adapter != null) adapter.getFilter().filter(newText);
            return true;
        }
    });
    return true;
}

@Override
public boolean onOptionsItemSelected(android.view.MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
        startActivity(new android.content.Intent(this, SettingsActivity.class));
        return true;
    } else if (id == R.id.action_reports) {
        startActivity(new android.content.Intent(this, ReportActivity.class));
        return true;
    } else if (id == R.id.action_sort_priority) {
        sortByPriority();
        return true;
    } else if (id == R.id.action_sort_due) {
        sortByDue();
        return true;
    } else if (id == R.id.action_filter_today) {
        filterToday();
        return true;
    } else if (id == R.id.action_filter_all) {
        loadAll();
        return true;
    }
    return super.onOptionsItemSelected(item);
}

private void sortByPriority(){
    // replace with DB ORDER BY if available
    if (adapter != null) adapter.sortByPriority();
}
private void sortByDue(){
    if (adapter != null) adapter.sortByDue();
}
private void filterToday(){
    if (adapter != null) adapter.filterToday();
}
private void loadAll(){
    if (adapter != null) adapter.clearFilter();
}
