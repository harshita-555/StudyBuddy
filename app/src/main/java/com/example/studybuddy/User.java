package com.example.studybuddy;

public class User {

    private String fullName =" ~~";
    private String occupation;
    private String userId ;
    private String image="";
    private String thumbImg="";



    public void User()
    {}

    public String getImage() {
        return image;
    }

    public String getThumbImg() {
        return thumbImg;
    }

    public String getFullName() {
        return fullName;
    }

    public String getOccupation(){
        return occupation;
    }

    public String getUserId() {
        return userId;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }
}
