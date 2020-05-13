package com.example.studybuddy.sbj;

public class noteStore {
    String title;
    String content;

    public noteStore(String title,String content)
    {
        this.title=title;
        this.content=content;
    }
    public noteStore()
    {

    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
