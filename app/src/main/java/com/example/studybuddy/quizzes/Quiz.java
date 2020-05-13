package com.example.studybuddy.quizzes;


import java.io.Serializable;
import java.util.ArrayList;

public class Quiz implements Serializable {
    String Name;
    ArrayList<Question> Questions;
    Long time;
    Long timeStamp;
    Long attemptTill;
    Boolean attemptOnce;
    Boolean isAttempted = false;

    public void setAttempted(Boolean attempted) {
        isAttempted = attempted;
    }

    public Boolean getAttempted() {
        return isAttempted;
    }

    public Quiz() {
    }

    public Long getTime() {
        return time;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getAttemptTill() {
        return attemptTill;
    }

    public void setAttemptTill(Long attemptTill) {
        this.attemptTill = attemptTill;
    }

    public Boolean getAttemptOnce() {
        return attemptOnce;
    }

    public void setAttemptOnce(Boolean attemptOnce) {
        this.attemptOnce = attemptOnce;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<Question> getQuestions() {
        return Questions;
    }

    public void setQuestions(ArrayList<Question> Questions) {
        this.Questions = Questions;
    }

    public void setName(String name) {
        Name = name;
    }

}
