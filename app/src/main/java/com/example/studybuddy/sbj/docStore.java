package com.example.studybuddy.sbj;

public class docStore {
    private String docType="";
    private String docUrl;
    private String docName="UNTITLED";
    private Long docSize;

    public docStore()
    {

    }

    public Long getDocSize() {
        return docSize;
    }

    public void setDocSize(Long docSize) {
        this.docSize = docSize;
    }

    public String getDocName() {
        return docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
