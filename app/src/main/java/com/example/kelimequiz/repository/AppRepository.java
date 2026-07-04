package com.example.kelimequiz.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.kelimequiz.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AppRepository {
    private static AppRepository instance;
    private final Context context;
    private AppData appData;
    private static final String PREFS_NAME = "KelimeQuizPrefs2";
    private static final String KEY_APP_DATA = "app_data_v2";

    private AppRepository(Context context) {
        this.context = context.getApplicationContext();
        load();
    }

    public static synchronized AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context);
        }
        return instance;
    }

    public AppData getAppData() {
        return appData;
    }

    public void save() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(appData);
        editor.putString(KEY_APP_DATA, json);
        editor.apply();
    }

    private void load() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_APP_DATA, null);
        if (json != null) {
            try {
                appData = new Gson().fromJson(json, AppData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (appData == null) {
            appData = new AppData();
        }
        if (appData.getCategories() == null) appData.setCategories(new ArrayList<>());
        if (appData.getSentences() == null) appData.setSentences(new ArrayList<>());

        migrateOldData();

        // Nesnelerin kendi kendilerini iyileştirmesi (Auto-heal)
        for (Category cat : appData.getCategories()) {
            if (cat.getId() == null) cat.setId(java.util.UUID.randomUUID().toString());
            if (cat.getWordList() == null) cat.setWordList(new ArrayList<>());
            for (Word w : cat.getWordList()) {
                if (w.getId() == null) w.setId(java.util.UUID.randomUUID().toString());
                if (w.getDateCreated() == 0) w.setDateCreated(System.currentTimeMillis());
            }
        }
        for (Sentence s : appData.getSentences()) {
            if (s.getId() == null) s.setId(java.util.UUID.randomUUID().toString());
        }

        // İlk kurulumda 25 temel kelimeyi ekle
        if (appData.getCategories().isEmpty()) {
            Category defaultCategory = new Category("Temel Kelimeler");
            String[][] defaultWords = {
                {"Hello", "Merhaba"},
                {"Goodbye", "Hoşça kal"},
                {"Apple", "Elma"},
                {"Book", "Kitap"},
                {"Water", "Su"},
                {"Computer", "Bilgisayar"},
                {"School", "Okul"},
                {"Teacher", "Öğretmen"},
                {"Student", "Öğrenci"},
                {"House", "Ev"},
                {"Family", "Aile"},
                {"Friend", "Arkadaş"},
                {"Morning", "Sabah"},
                {"Night", "Gece"},
                {"Food", "Yemek"},
                {"Car", "Araba"},
                {"Phone", "Telefon"},
                {"Time", "Zaman"},
                {"Money", "Para"},
                {"City", "Şehir"},
                {"Love", "Sevgi"},
                {"Happy", "Mutlu"},
                {"Beautiful", "Güzel"},
                {"Work", "Çalışmak"},
                {"Learn", "Öğrenmek"}
            };
            for (String[] pair : defaultWords) {
                defaultCategory.getWordList().add(new Word(pair[0], pair[1]));
            }
            appData.getCategories().add(defaultCategory);
            save();
        }
    }

    private void migrateOldData() {
        SharedPreferences oldPrefs = context.getSharedPreferences("KelimeQuizPrefs", Context.MODE_PRIVATE);
        if (oldPrefs.contains("word_list")) {
            String oldJson = oldPrefs.getString("word_list", null);
            if (oldJson != null) {
                try {
                    // Eski v1 verisini yükle
                    Type type = new TypeToken<ArrayList<Word>>() {}.getType();
                    ArrayList<Word> oldWords = new Gson().fromJson(oldJson, type);
                    if (oldWords != null && !oldWords.isEmpty()) {
                        Category defaultCategory = new Category("Genel");
                        for (Word w : oldWords) {
                            if (w.getId() == null) w.setId(java.util.UUID.randomUUID().toString());
                            defaultCategory.getWordList().add(w);
                        }
                        appData.getCategories().add(defaultCategory);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Çakışmayı önlemek için eski veriyi temizle
            oldPrefs.edit().remove("word_list").apply();
            save();
        }
    }

    public void clearAll() {
        appData = new AppData();
        appData.setCategories(new ArrayList<>());
        appData.setSentences(new ArrayList<>());
        Category defaultCategory = new Category("Temel Kelimeler");
        String[][] defaultWords = {
            {"Hello", "Merhaba"},
            {"Goodbye", "Hoşça kal"},
            {"Apple", "Elma"},
            {"Book", "Kitap"},
            {"Water", "Su"},
            {"Computer", "Bilgisayar"},
            {"School", "Okul"},
            {"Teacher", "Öğretmen"},
            {"Student", "Öğrenci"},
            {"House", "Ev"},
            {"Family", "Aile"},
            {"Friend", "Arkadaş"},
            {"Morning", "Sabah"},
            {"Night", "Gece"},
            {"Food", "Yemek"},
            {"Car", "Araba"},
            {"Phone", "Telefon"},
            {"Time", "Zaman"},
            {"Money", "Para"},
            {"City", "Şehir"},
            {"Love", "Sevgi"},
            {"Happy", "Mutlu"},
            {"Beautiful", "Güzel"},
            {"Work", "Çalışmak"},
            {"Learn", "Öğrenmek"}
        };
        for (String[] pair : defaultWords) {
            defaultCategory.getWordList().add(new Word(pair[0], pair[1]));
        }
        appData.getCategories().add(defaultCategory);
        save();
    }

    public boolean importJson(String jsonStr) {
        try {
            AppData imported = new Gson().fromJson(jsonStr, AppData.class);
            if (imported != null) {
                if (imported.getCategories() == null) imported.setCategories(new ArrayList<>());
                if (imported.getSentences() == null) imported.setSentences(new ArrayList<>());
                appData = imported;
                save();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String exportJson() {
        return new Gson().toJson(appData);
    }
}
