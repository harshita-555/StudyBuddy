package com.example.studybuddy.sbj;

public class mediaStore {

    private boolean isVideo;
    private String mediaUrl;

    public mediaStore(){}
    public mediaStore(boolean val,String s){
        isVideo=val;
        mediaUrl=s;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}
