package com.example.todolist;

public class Task {
    private String name;
    private String time;
    private boolean isCompleted;
    private String date; // Thêm thuộc tính date

    public Task(String name, String time, String date) {
        this.name = name;
        this.time = time;
        this.date = date;
        this.isCompleted = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
