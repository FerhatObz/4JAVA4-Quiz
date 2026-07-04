package com.example.kelimequiz.model;

import java.io.Serializable;
import java.util.UUID;

public class Word implements Serializable {
    private String id;
    private String english;
    private String turkish;
    private boolean isFavorite;
    private boolean isLearned;
    private long dateCreated;

    // Geleceğe hazırlık alanları (boş / varsayılan bırakıldı)
    private String audioPath;
    private String imagePath;
    private String wordLevel; // A1, A2 vb.
    private int studyInterval; // Spaced Repetition için
    private long nextReviewDate;

    public Word() {
        this.id = UUID.randomUUID().toString();
        this.dateCreated = System.currentTimeMillis();
    }

    public Word(String english, String turkish) {
        this.id = UUID.randomUUID().toString();
        this.english = english;
        this.turkish = turkish;
        this.isFavorite = false;
        this.isLearned = false;
        this.dateCreated = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getTurkish() { return turkish; }
    public void setTurkish(String turkish) { this.turkish = turkish; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { isLearned = learned; }

    public long getDateCreated() { return dateCreated; }
    public void setDateCreated(long dateCreated) { this.dateCreated = dateCreated; }

    // Gelecek alanları getter/setterları
    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getWordLevel() { return wordLevel; }
    public void setWordLevel(String wordLevel) { this.wordLevel = wordLevel; }
    public int getStudyInterval() { return studyInterval; }
    public void setStudyInterval(int studyInterval) { this.studyInterval = studyInterval; }
    public long getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }
}
