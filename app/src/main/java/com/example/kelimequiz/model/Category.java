package com.example.kelimequiz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Category implements Serializable {
    private String id;
    private String name;
    private ArrayList<Word> wordList;
    private long dateCreated;
    
    // Geleceğe hazırlık: Alt kategoriler ve açıklama
    private String description;
    private ArrayList<String> subCategoryIds;

    public Category() {
        this.id = UUID.randomUUID().toString();
        this.wordList = new ArrayList<>();
        this.dateCreated = System.currentTimeMillis();
    }

    public Category(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.wordList = new ArrayList<>();
        this.dateCreated = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArrayList<Word> getWordList() {
        if (wordList == null) wordList = new ArrayList<>();
        return wordList;
    }
    public void setWordList(ArrayList<Word> wordList) { this.wordList = wordList; }

    public long getDateCreated() { return dateCreated; }
    public void setDateCreated(long dateCreated) { this.dateCreated = dateCreated; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ArrayList<String> getSubCategoryIds() { return subCategoryIds; }
    public void setSubCategoryIds(ArrayList<String> subCategoryIds) { this.subCategoryIds = subCategoryIds; }
}
