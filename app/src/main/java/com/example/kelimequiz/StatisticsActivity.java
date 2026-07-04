package com.example.kelimequiz;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kelimequiz.model.Category;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvStatCategories, tvStatWords, tvStatSentences;
    private TextView tvStatFavorites, tvStatLearned, tvStatToday;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        repository = AppRepository.getInstance(this);

        tvStatCategories = findViewById(R.id.tvStatCategories);
        tvStatWords = findViewById(R.id.tvStatWords);
        tvStatSentences = findViewById(R.id.tvStatSentences);
        tvStatFavorites = findViewById(R.id.tvStatFavorites);
        tvStatLearned = findViewById(R.id.tvStatLearned);
        tvStatToday = findViewById(R.id.tvStatToday);

        // Geri Dönüş
        findViewById(R.id.btnBack).setOnClickListener(v -> closeActivityWithTransition());

        calculateStatistics();
    }

    private void calculateStatistics() {
        ArrayList<Category> categories = repository.getAppData().getCategories();
        
        int totalCategories = categories.size();
        int totalWords = 0;
        int totalFavorites = 0;
        int totalLearned = 0;
        int totalToday = 0;

        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);

        for (Category cat : categories) {
            if (cat.getWordList() != null) {
                totalWords += cat.getWordList().size();
                for (Word w : cat.getWordList()) {
                    if (w.isFavorite()) totalFavorites++;
                    if (w.isLearned()) totalLearned++;
                    if (w.getDateCreated() > oneDayAgo) totalToday++;
                }
            }
        }

        int totalSentences = repository.getAppData().getSentences() != null ? repository.getAppData().getSentences().size() : 0;

        tvStatCategories.setText(String.valueOf(totalCategories));
        tvStatWords.setText(String.valueOf(totalWords));
        tvStatSentences.setText(String.valueOf(totalSentences));
        tvStatFavorites.setText(String.valueOf(totalFavorites));
        tvStatLearned.setText(String.valueOf(totalLearned));
        tvStatToday.setText(String.valueOf(totalToday));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void closeActivityWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
