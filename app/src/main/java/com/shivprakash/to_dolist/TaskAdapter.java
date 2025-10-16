package com.shivprakash.to_dolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements Filterable {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final Context mContext;
    private List<MainActivity.Data> mTaskList;
    private OnItemClickListener mListener;
    private List<MainActivity.Data> originalList;

    public TaskAdapter(Context context, List<MainActivity.Data> taskList) {
        mContext = context;
        mTaskList = taskList != null ? taskList : new ArrayList<MainActivity.Data>();
        originalList = new ArrayList<>(mTaskList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.task_card_layout, parent, false);
        return new TaskViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        MainActivity.Data currentItem = mTaskList.get(position);
        holder.mTextViewName.setText(currentItem.getName());
        holder.mTextViewDate.setText("Date: " + (currentItem.getDate()==null?"":currentItem.getDate()));
        holder.mTextViewTime.setText("Time: " + (currentItem.getTime()==null?"":currentItem.getTime()));
        holder.text_category.setText("Category: " + (currentItem.getCategory()==null?"":currentItem.getCategory()));
        holder.text_priority.setText("Priority: " + (currentItem.getPriority()==null?"":currentItem.getPriority()));
        holder.text_notes.setText("Note: " + (currentItem.getNotes()==null?"":currentItem.getNotes()));
    }

    @Override
    public int getItemCount() { return mTaskList != null ? mTaskList.size() : 0; }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewName, mTextViewDate, mTextViewTime, text_category, text_priority, text_notes;
        public TaskViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.text_name);
            mTextViewDate = itemView.findViewById(R.id.text_date);
            mTextViewTime = itemView.findViewById(R.id.text_time);
            text_category = itemView.findViewById(R.id.text_category);
            text_priority = itemView.findViewById(R.id.text_priority);
            text_notes = itemView.findViewById(R.id.text_notes);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) listener.onItemClick(position);
                }
            });
        }
    }

    public void setData(List<MainActivity.Data> data){
        if (data == null) data = new ArrayList<>();
        mTaskList = data;
        originalList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (originalList == null) originalList = new ArrayList<>(mTaskList);
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MainActivity.Data> filtered = new ArrayList<>();
                if (constraint == null || constraint.length()==0) {
                    filtered.addAll(originalList);
                } else {
                    String q = constraint.toString().toLowerCase();
                    for (MainActivity.Data t : originalList){
                        String name = t.getName() == null ? "" : t.getName();
                        String notes = t.getNotes() == null ? "" : t.getNotes();
                        if (name.toLowerCase().contains(q) || notes.toLowerCase().contains(q)){
                            filtered.add(t);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = filtered;
                return fr;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mTaskList = (List<MainActivity.Data>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void sortByPriority(){
        Map<String,Integer> rank = new HashMap<>();
        rank.put("High", 3); rank.put("Medium", 2); rank.put("Low", 1);
        Collections.sort(mTaskList, (a,b) -> {
            Integer ra = rank.getOrDefault(a.getPriority(), 0);
            Integer rb = rank.getOrDefault(b.getPriority(), 0);
            return Integer.compare(rb, ra);
        });
        notifyDataSetChanged();
    }

    public void sortByDue(){
        Collections.sort(mTaskList, (a,b) -> {
            String da = (a.getDate()==null?"":a.getDate()) + " " + (a.getTime()==null?"":a.getTime());
            String db = (b.getDate()==null?"":b.getDate()) + " " + (b.getTime()==null?"":b.getTime());
            return da.compareTo(db);
        });
        notifyDataSetChanged();
    }

    public void filterToday(){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new java.util.Date());
        List<MainActivity.Data> filtered = new ArrayList<>();
        for (MainActivity.Data t : (originalList!=null?originalList:mTaskList)){
            String d = t.getDate();
            if (d != null && d.startsWith(today)) filtered.add(t);
        }
        mTaskList = filtered;
        notifyDataSetChanged();
    }

    public void clearFilter(){
        if (originalList!=null){
            mTaskList = new ArrayList<>(originalList);
            notifyDataSetChanged();
        }
    }
}
