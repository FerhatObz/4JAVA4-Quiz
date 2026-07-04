package com.example.kelimequiz.model;

import java.io.Serializable;
import java.util.UUID;

public class Sentence implements Serializable {
    private String id;
    private String english;
    private String turkish;
    private long dateCreated;

    public Sentence() {
        this.id = UUID.randomUUID().toString();
        this.dateCreated = System.currentTimeMillis();
    }

    public Sentence(String english, String turkish) {
        this.id = UUID.randomUUID().toString();
        this.english = english;
        this.turkish = turkish;
        this.dateCreated = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getTurkish() { return turkish; }
    public void setTurkish(String turkish) { this.turkish = turkish; }

    public long getDateCreated() { return dateCreated; }
    public void setDateCreated(long dateCreated) { this.dateCreated = dateCreated; }
}
