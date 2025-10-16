package com.shivprakash.to_dolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements Filterable {

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onCheckboxClick(int position);
    }

    private final Context context;
    private List<MainActivity.Data> taskList;
    private List<MainActivity.Data> originalList;
    private OnItemClickListener listener;

    public TaskAdapter(Context context, List<MainActivity.Data> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.originalList = new ArrayList<>(this.taskList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_card_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        MainActivity.Data currentItem = taskList.get(position);
        holder.name.setText(currentItem.getName());
        holder.date.setText(context.getString(R.string.task_date_label, safeText(currentItem.getDate())));
        holder.time.setText(context.getString(R.string.task_time_label, safeText(currentItem.getTime())));
        holder.category.setText(context.getString(R.string.task_category_label, safeText(currentItem.getCategory())));
        holder.priority.setText(context.getString(R.string.task_priority_label, safeText(currentItem.getPriority())));
        holder.notes.setText(context.getString(R.string.task_notes_label, safeText(currentItem.getNotes())));
        holder.completed.setOnCheckedChangeListener(null);
        holder.completed.setChecked(currentItem.isCompleted());
        holder.completed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && isChecked) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onCheckboxClick(adapterPosition);
                }
            }
        });
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onEditClick(adapterPosition);
                }
            }
        });
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void setData(List<MainActivity.Data> data) {
        taskList = data != null ? data : new ArrayList<>();
        originalList = new ArrayList<>(taskList);
        notifyDataSetChanged();
    }

    public MainActivity.Data getItem(int position) {
        if (position < 0 || position >= taskList.size()) {
            return null;
        }
        return taskList.get(position);
    }

    public void removeTaskAt(int position) {
        if (position < 0 || position >= taskList.size()) {
            return;
        }
        MainActivity.Data removed = taskList.remove(position);
        if (originalList != null) {
            originalList.remove(removed);
        }
        notifyItemRemoved(position);
    }

    @Override
    public Filter getFilter() {
        if (originalList == null) {
            originalList = new ArrayList<>(taskList);
        }
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MainActivity.Data> filtered = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(originalList);
                } else {
                    String query = constraint.toString().toLowerCase(Locale.getDefault());
                    for (MainActivity.Data task : originalList) {
                        String name = safeText(task.getName());
                        String notes = safeText(task.getNotes());
                        if (name.toLowerCase(Locale.getDefault()).contains(query) ||
                                notes.toLowerCase(Locale.getDefault()).contains(query)) {
                            filtered.add(task);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                taskList = results.values != null ? (List<MainActivity.Data>) results.values : new ArrayList<>();
                notifyDataSetChanged();
            }
        };
    }

    public void sortByPriority() {
        Map<String, Integer> rank = new HashMap<>();
        rank.put("High", 3);
        rank.put("Medium", 2);
        rank.put("Low", 1);
        Collections.sort(taskList, (a, b) -> {
            Integer rankA = rank.getOrDefault(a.getPriority(), 0);
            Integer rankB = rank.getOrDefault(b.getPriority(), 0);
            return Integer.compare(rankB, rankA);
        });
        notifyDataSetChanged();
    }

    public void sortByDue() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Collections.sort(taskList, (a, b) -> {
            Date dateA = parseDueDate(formatter, a.getDate(), a.getTime());
            Date dateB = parseDueDate(formatter, b.getDate(), b.getTime());
            if (dateA == null && dateB == null) {
                return 0;
            } else if (dateA == null) {
                return 1;
            } else if (dateB == null) {
                return -1;
            }
            return dateA.compareTo(dateB);
        });
        notifyDataSetChanged();
    }

    public void filterToday() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = dateFormatter.format(new Date());
        List<MainActivity.Data> filtered = new ArrayList<>();
        List<MainActivity.Data> source = originalList != null ? originalList : taskList;
        for (MainActivity.Data task : source) {
            if (today.equals(safeText(task.getDate()))) {
                filtered.add(task);
            }
        }
        taskList = filtered;
        notifyDataSetChanged();
    }

    public void clearFilter() {
        if (originalList != null) {
            taskList = new ArrayList<>(originalList);
            notifyDataSetChanged();
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private Date parseDueDate(SimpleDateFormat formatter, String date, String time) {
        try {
            String dueDate = safeText(date);
            String dueTime = safeText(time);
            if (dueDate.isEmpty() && dueTime.isEmpty()) {
                return null;
            }
            return formatter.parse(String.format(Locale.getDefault(), "%s %s", dueDate, dueTime));
        } catch (ParseException e) {
            return null;
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView date;
        final TextView time;
        final TextView category;
        final TextView priority;
        final TextView notes;
        final CheckBox completed;
        final Button editButton;
        final Button deleteButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            date = itemView.findViewById(R.id.text_date);
            time = itemView.findViewById(R.id.text_time);
            category = itemView.findViewById(R.id.text_category);
            priority = itemView.findViewById(R.id.text_priority);
            notes = itemView.findViewById(R.id.text_notes);
            completed = itemView.findViewById(R.id.check_box);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
