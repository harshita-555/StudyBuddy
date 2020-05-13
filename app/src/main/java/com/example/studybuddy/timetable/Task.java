package com.example.studybuddy.timetable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {

    String name;
    String message;
    String deadline;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Task()
    {

    }

    public Map toTaskMap() {
        Map todo =  new HashMap<>();
        todo.put("name", name);
        todo.put("message", message);
        todo.put("deadline", deadline);

        return todo;
    }
}
