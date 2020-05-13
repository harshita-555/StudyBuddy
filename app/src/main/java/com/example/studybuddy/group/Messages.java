package com.example.studybuddy.group;

public class Messages {

    private String content;
    private String type;
    private long time;
    private  String from;

    public Messages()
    {

    }
    public Messages(String content,String type,long time ,String from)
    {
        this.content=content;
        this.from=from;
        this.time=time;
        this.type=type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
