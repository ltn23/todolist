package com.example.todolist;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private Map<String, List<Task>> tasksByDate = new TreeMap<>();
    private LinearLayout tasksContainer;
    private Button btnHideCompleted;
    private boolean isHideCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasksByDate = new HashMap<>();
        tasksContainer = findViewById(R.id.tasksContainer);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        btnHideCompleted = findViewById(R.id.btnHideCompleted);

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        btnHideCompleted.setOnClickListener(v -> {
            isHideCompleted = !isHideCompleted;
            updateAllRecyclerViews();
            if (isHideCompleted) {
                btnHideCompleted.setText("Show completed");
            } else {
                btnHideCompleted.setText("Hide completed");
            }
        });
    }

    private void refreshTasksContainer() {
        Map<String, List<Task>> sortedTasks = sortTasksByDateAsc(tasksByDate);
        tasksContainer.removeAllViews();
        for (Map.Entry<String, List<Task>> entry : sortedTasks.entrySet()) {
            String date = entry.getKey();
            createRecyclerViewForDate(date);
        }
    }

    private Map<String, List<Task>> sortTasksByDateAsc(Map<String, List<Task>> tasksByDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        List<Map.Entry<String, List<Task>>> entryList = new ArrayList<>(tasksByDate.entrySet());

        entryList.sort((e1, e2) -> {
            try {
                Date date1 = dateFormat.parse(e1.getKey());
                Date date2 = dateFormat.parse(e2.getKey());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        Map<String, List<Task>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<Task>> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText etTaskName = dialogView.findViewById(R.id.etTaskName);
        final DatePicker datePicker = dialogView.findViewById(R.id.datePickerTaskDate);

        builder.setTitle("Add Task")
                .setPositiveButton("Save", (dialog, id) -> {
                    String taskName = etTaskName.getText().toString();
                    if (!taskName.isEmpty()) {
                        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth() + 1;
                        int year = datePicker.getYear();
                        String taskDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year);

                        Task task = new Task(taskName, currentTime, taskDate);

                        addTask(task);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private void addTask(Task task) {
        List<Task> taskList = tasksByDate.get(task.getDate());

        if (taskList == null || taskList.isEmpty()) {
            taskList = new ArrayList<>();
            tasksByDate.put(task.getDate(), taskList);
            createRecyclerViewForDate(task.getDate());
        }

        tasksByDate.get(task.getDate()).add(task);

        refreshTasksContainer();

    }


    private void createRecyclerViewForDate(String date) {
        TextView dateTextView = new TextView(this);
        dateTextView.setText("Tasks for " + date);
        dateTextView.setTextSize(24f);
        dateTextView.setTypeface(null, Typeface.BOLD);
        dateTextView.setTextColor(Color.BLACK);
        dateTextView.setPadding(16, 16, 16, 16);
        tasksContainer.addView(dateTextView);

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(tasksByDate.get(date)));
        tasksContainer.addView(recyclerView);

    }

    private void updateRecyclerViewForDate(String date) {
        int index = findRecyclerViewIndexForDate(date);
        if (index != -1) {
            RecyclerView recyclerView = (RecyclerView) tasksContainer.getChildAt(index);
            TaskAdapter adapter = (TaskAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

    }

    private int findRecyclerViewIndexForDate(String date) {
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View view = tasksContainer.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getText().toString().contains(date)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private void updateAllRecyclerViews() {
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View view = tasksContainer.getChildAt(i);
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                TaskAdapter adapter = (TaskAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setHideCompleted(isHideCompleted);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void moveTaskToNewDate(Task task, String oldDate) {

        List<Task> oldTaskList = tasksByDate.get(oldDate);
        if (oldTaskList != null) {
            oldTaskList.remove(task);
            if (oldTaskList.isEmpty()) {
                tasksByDate.remove(oldDate);
                removeRecyclerViewForDate(oldDate);
            } else {
                updateRecyclerViewForDate(oldDate);
            }
        }

        addTask(task);
        refreshTasksContainer();
    }

    public void removeRecyclerViewForDate(String date) {
        int index = findRecyclerViewIndexForDate(date);

        if (index != -1) {
            tasksContainer.removeViewAt(index - 1);
            tasksContainer.removeViewAt(index - 1);
        }

    }

}
