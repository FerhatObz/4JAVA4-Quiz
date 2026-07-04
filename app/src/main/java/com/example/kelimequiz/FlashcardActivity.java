package com.example.kelimequiz;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.kelimequiz.model.Category;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Collections;

public class FlashcardActivity extends AppCompatActivity {

    private TextView tvProgress, tvWord, tvLanguageLabel;
    private TextView tvCorrectIndicator, tvIncorrectIndicator;
    private ProgressBar pbProgress;
    private CardView cardFlashcard;
    private ImageButton btnBack;
    private MaterialButton btnReshuffle;

    private String categoryId;
    private Category category;
    private AppRepository repository;

    private ArrayList<Word> quizList;
    private int currentIndex = 0;
    private int learnedCount = 0;
    private int skippedCount = 0;
    private boolean isShowingEnglish = true;
    private Word currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        repository = AppRepository.getInstance(this);
        categoryId = getIntent().getStringExtra("category_id");

        // Kategoriyi bul
        for (Category cat : repository.getAppData().getCategories()) {
            if (cat.getId().equals(categoryId)) {
                category = cat;
                break;
            }
        }

        if (category == null || category.getWordList().isEmpty()) {
            Toast.makeText(this, "Çalışılacak kelime bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvProgress = findViewById(R.id.tvProgress);
        tvWord = findViewById(R.id.tvWord);
        tvLanguageLabel = findViewById(R.id.tvLanguageLabel);
        tvCorrectIndicator = findViewById(R.id.tvCorrectIndicator);
        tvIncorrectIndicator = findViewById(R.id.tvIncorrectIndicator);
        pbProgress = findViewById(R.id.pbProgress);
        cardFlashcard = findViewById(R.id.cardFlashcard);
        btnBack = findViewById(R.id.btnBack);
        btnReshuffle = findViewById(R.id.btnReshuffle);

        // Kamera mesafesini ayarla (3D rotasyon bozulmasın diye)
        float scale = getResources().getDisplayMetrics().density;
        cardFlashcard.setCameraDistance(8000 * scale);

        setupSession();

        setupSwipeAndClick();

        btnBack.setOnClickListener(v -> closeActivityWithTransition());
        
        btnReshuffle.setOnClickListener(v -> {
            setupSession();
            Toast.makeText(this, "Kartlar yeniden karıştırıldı!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSession() {
        quizList = new ArrayList<>(category.getWordList());
        // Kaliteli rastgele sıralama algoritması (Collections.shuffle)
        Collections.shuffle(quizList);
        currentIndex = 0;
        learnedCount = 0;
        skippedCount = 0;
        
        pbProgress.setMax(quizList.size());
        loadWord(currentIndex);
    }

    private void loadWord(int index) {
        currentWord = quizList.get(index);
        tvWord.setText(currentWord.getEnglish());
        tvLanguageLabel.setText("İngilizce");
        isShowingEnglish = true;
        tvProgress.setText((index + 1) + " / " + quizList.size());
        pbProgress.setProgress(index + 1);
    }

    private void flipCard() {
        cardFlashcard.animate()
            .rotationY(isShowingEnglish ? 90f : -90f)
            .setDuration(150)
            .withEndAction(() -> {
                if (isShowingEnglish) {
                    tvWord.setText(currentWord.getTurkish());
                    tvLanguageLabel.setText("Türkçe");
                } else {
                    tvWord.setText(currentWord.getEnglish());
                    tvLanguageLabel.setText("İngilizce");
                }
                isShowingEnglish = !isShowingEnglish;

                cardFlashcard.setRotationY(isShowingEnglish ? -90f : 90f);
                cardFlashcard.animate()
                    .rotationY(0f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }

    private void setupSwipeAndClick() {
        cardFlashcard.setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0f;
            private float startY = 0f;
            private static final int SWIPE_THRESHOLD = 250;
            private boolean isMoved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isMoved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX;
                        float dy = event.getRawY() - startY;

                        if (Math.abs(dx) > 15 || Math.abs(dy) > 15) {
                            isMoved = true;
                        }

                        if (isMoved) {
                            v.setTranslationX(dx);
                            v.setRotation(dx * 0.04f);

                            // Tinder tarzı görsel geribildirim göstergeleri
                            float alpha = Math.min(Math.abs(dx) / (SWIPE_THRESHOLD * 0.8f), 1.0f);
                            if (dx > 0) {
                                tvCorrectIndicator.setAlpha(alpha);
                                tvIncorrectIndicator.setAlpha(0f);
                            } else {
                                tvIncorrectIndicator.setAlpha(alpha);
                                tvCorrectIndicator.setAlpha(0f);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        float deltaX = event.getRawX() - startX;
                        float deltaY = event.getRawY() - startY;

                        tvCorrectIndicator.animate().alpha(0f).setDuration(200).start();
                        tvIncorrectIndicator.animate().alpha(0f).setDuration(200).start();

                        if (isMoved && Math.abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0) {
                                handleAnswer(true); // Öğrendim
                            } else {
                                handleAnswer(false); // Bilemedim
                            }
                        } else {
                            v.animate()
                                .translationX(0f)
                                .rotation(0f)
                                .setDuration(250)
                                .start();

                            if (Math.abs(deltaX) < 15 && Math.abs(deltaY) < 15) {
                                flipCard();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void handleAnswer(boolean isLearned) {
        float targetX = isLearned ? 1200f : -1200f;
        float targetRotation = isLearned ? 35f : -35f;

        cardFlashcard.animate()
            .translationX(targetX)
            .rotation(targetRotation)
            .alpha(0f)
            .setDuration(200)
            .withEndAction(() -> {
                if (isLearned) {
                    learnedCount++;
                    // Öğrenildi olarak veritabanına kaydet
                    currentWord.setLearned(true);
                    repository.save();
                } else {
                    skippedCount++;
                }

                currentIndex++;
                if (currentIndex < quizList.size()) {
                    loadWord(currentIndex);

                    cardFlashcard.setTranslationX(isLearned ? -1200f : 1200f);
                    cardFlashcard.setRotation(isLearned ? -35f : 35f);
                    cardFlashcard.setAlpha(0f);

                    cardFlashcard.animate()
                        .translationX(0f)
                        .rotation(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                } else {
                    showResultDialog();
                }
            })
            .start();
    }

    private void showResultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Çalışma Bitti!");
        builder.setMessage("Kategori: " + category.getName() + "\n\n" +
                "Öğrenilen Kelime: " + learnedCount + "\n" +
                "Tekrar Edilecek: " + skippedCount);
        builder.setCancelable(false);
        builder.setPositiveButton("Tamam", (dialog, which) -> closeActivityWithTransition());
        builder.show();
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
