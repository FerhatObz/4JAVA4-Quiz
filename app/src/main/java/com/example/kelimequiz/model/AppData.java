package com.example.kelimequiz.model;

import java.io.Serializable;
import java.util.ArrayList;

public class AppData implements Serializable {
    private ArrayList<Category> categories;
    private ArrayList<Sentence> sentences;
    private boolean isDarkMode;

    public AppData() {
        this.categories = new ArrayList<>();
        this.sentences = new ArrayList<>();
        this.isDarkMode = false;
    }

    public ArrayList<Category> getCategories() {
        if (categories == null) categories = new ArrayList<>();
        return categories;
    }
    public void setCategories(ArrayList<Category> categories) { this.categories = categories; }

    public ArrayList<Sentence> getSentences() {
        if (sentences == null) sentences = new ArrayList<>();
        return sentences;
    }
    public void setSentences(ArrayList<Sentence> sentences) { this.sentences = sentences; }

    public boolean isDarkMode() { return isDarkMode; }
    public void setDarkMode(boolean darkMode) { isDarkMode = darkMode; }
}
