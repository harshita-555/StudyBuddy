package com.example.studybuddy.group;

import java.util.ArrayList;

public class Group {

    private String type;
    private String name;
    private String image="";
    private ArrayList<String> members;
    private ArrayList<String> seenBy;
    private boolean seen;




    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }



}
