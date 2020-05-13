package com.example.studybuddy.sbj;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.studybuddy.R;

public class FlashCard implements Parcelable {
    private String question="";
    private String ans="";
    private String questionUrl="";
    private String ansUrl="";
    private int questionColor= R.color.color1;
    private int ansColor=R.color.color2;
    private boolean backSide=false;

    public FlashCard(){}
    public FlashCard(Parcel in)
    {
        this.question=in.readString();
        this.ans=in.readString();
        this.questionUrl=in.readString();
        this.ansUrl=in.readString();
        this.questionColor=in.readInt();
        this.ansColor=in.readInt();
    }

    public static final Creator<FlashCard> CREATOR = new Creator<FlashCard>() {
        @Override
        public FlashCard createFromParcel(Parcel in) {

            return new FlashCard(in);
        }

        @Override
        public FlashCard[] newArray(int size) {
            return new FlashCard[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(ans);
        dest.writeString(questionUrl);
        dest.writeString(ansUrl);
        dest.writeInt(questionColor);
        dest.writeInt(ansColor);
    }

    public String getAns() {
        return ans;
    }

    public String getQuestion() {
        return question;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isBackSide() {
        return backSide;
    }

    public int getAnsColor() {
        return ansColor;
    }

    public int getQuestionColor() {
        return questionColor;
    }

    public void setQuestionColor(int questionColor) {
        this.questionColor = questionColor;
    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public void setAnsColor(int ansColor) {
        this.ansColor = ansColor;
    }

    public String getAnsUrl() {
        return ansUrl;
    }

    public void setAnsUrl(String ansUrl) {
        this.ansUrl = ansUrl;
    }

    public void setBackSide(boolean backSide) {
        this.backSide = backSide;
    }
}
