package com.shivprakash.to_dolist;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ListView list = findViewById(R.id.report_list);

        TaskDBHelper db = new TaskDBHelper(this);
        List<String> rows = new ArrayList<>();

        long now = System.currentTimeMillis();
        long end = now + 24*60*60*1000;
        try {
            Cursor c = db.getReadableDatabase().query(TaskContract.TaskEntry.TABLE_NAME, null,
                    TaskContract.TaskEntry.COLUMN_PRIORITY + "=?",
                    new String[]{"High"},
                    null,null, TaskContract.TaskEntry.COLUMN_DUE_DATE+" ASC");
            while (c.moveToNext()){
                String t = c.getString(c.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TASK));
                String pr = c.getString(c.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_PRIORITY));
                rows.add("URGENT: " + t + " (" + pr + ")");
            }
            c.close();
        } catch (Exception ignored){}

        try {
            Cursor c2 = db.getReadableDatabase().query(TaskContract.TaskEntry.TABLE_NAME, null,
                    TaskContract.TaskEntry.COLUMN_COMPLETED + "=?",
                    new String[]{"1"}, null,null, null);
            while (c2.moveToNext()){
                String t = c2.getString(c2.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TASK));
                rows.add("DONE: " + t);
            }
            c2.close();
        } catch (Exception ignored){}

        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }
}
