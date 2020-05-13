package com.example.studybuddy.sbj;


import java.util.ArrayList;

public class Deck {
    String title="";
    ArrayList<FlashCard>  deck;
    int count=0;

    public Deck(){}
    public Deck(String title)
    {
        this.title =title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeck(ArrayList<FlashCard> deck1) {
        deck=new ArrayList<FlashCard>();
        for(int i=0;i<deck1.size();i++) deck.add(deck1.get(i));
        this.count=deck1.size();
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<FlashCard> getDeck() {
        return deck;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}