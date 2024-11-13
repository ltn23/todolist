package com.example.todolist;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private boolean hideCompleted = false;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());
        holder.taskTime.setText(task.getTime());
        holder.taskDate.setText(task.getDate()); // Hiển thị ngày tháng

        holder.taskCheckbox.setOnCheckedChangeListener(null);
        holder.taskCheckbox.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.5f); // Làm mờ task đã hoàn thành
        } else {
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f); // Task chưa hoàn thành rõ ràng
        }

        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            notifyItemChanged(position);
        });

        holder.btnEditTask.setOnClickListener(v -> showEditTaskDialog(holder.itemView, position));
        holder.btnDeleteTask.setOnClickListener(v -> showDeleteConfirmationDialog(holder.itemView, position));

        if (hideCompleted && task.isCompleted()) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        CheckBox taskCheckbox;
        TextView taskName, taskTime, taskDate; // Thêm taskDate ở đây
        ImageButton btnEditTask, btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            taskName = itemView.findViewById(R.id.task_name);
            taskTime = itemView.findViewById(R.id.task_time);
            taskDate = itemView.findViewById(R.id.task_date);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }

    public void setHideCompleted(boolean hideCompleted) {
        this.hideCompleted = hideCompleted;
        notifyDataSetChanged();
    }

    private void showDeleteConfirmationDialog(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    Task task = taskList.get(position);
                    String taskDate = task.getDate();

                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, taskList.size());

                    if (taskList.isEmpty()) {
                        ((MainActivity) view.getContext()).removeRecyclerViewForDate(taskDate);
                    }

                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    private void showEditTaskDialog(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText etTaskName = dialogView.findViewById(R.id.etTaskName);
        final DatePicker datePicker = dialogView.findViewById(R.id.datePickerTaskDate);

        Task currentTask = taskList.get(position);
        etTaskName.setText(currentTask.getName());

        String[] dateParts = currentTask.getDate().split("/");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int year = Integer.parseInt(dateParts[2]);

        datePicker.updateDate(year, month, day);

        builder.setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, id) -> {
                    String newTaskName = etTaskName.getText().toString();
                    int newDay = datePicker.getDayOfMonth();
                    int newMonth = datePicker.getMonth() + 1;
                    int newYear = datePicker.getYear();
                    String newTaskDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", newDay, newMonth, newYear);

                    if (!newTaskName.isEmpty()) {
                        currentTask.setName(newTaskName);
                    }

                    if (!newTaskDate.equals(currentTask.getDate())) {
                        String oldTaskDate = currentTask.getDate();
                        currentTask.setDate(newTaskDate);

                        ((MainActivity) view.getContext()).moveTaskToNewDate(currentTask, oldTaskDate);
                    } else {
                        notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }


}
