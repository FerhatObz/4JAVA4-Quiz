package com.example.kelimequiz;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kelimequiz.model.Category;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;

public class QuizActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvQuizProgress, tvQuestionWord;
    
    private MaterialCardView cardOptionA, cardOptionB, cardOptionC, cardOptionD;
    private TextView tvOptionA, tvOptionB, tvOptionC, tvOptionD;

    private String categoryId;
    private Category category;
    private AppRepository repository;

    private ArrayList<Word> quizWords;
    private int totalQuestions = 0;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private Word currentCorrectWord;

    private static final String[] FALLBACK_WORDS = {
        "Merhaba", "Hoşça kal", "Elma", "Kitap", "Su", "Bilgisayar", "Okul", "Öğretmen",
        "Öğrenci", "Ev", "Aile", "Arkadaş", "Sabah", "Gece", "Yemek", "Araba", "Telefon",
        "Zaman", "Para", "Şehir", "Sevgi", "Mutlu", "Güzel", "Çalışmak", "Öğrenmek"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

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
            Toast.makeText(this, "Test yapılacak kelime bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        tvQuizProgress = findViewById(R.id.tvQuizProgress);
        tvQuestionWord = findViewById(R.id.tvQuestionWord);
        
        cardOptionA = findViewById(R.id.cardOptionA);
        cardOptionB = findViewById(R.id.cardOptionB);
        cardOptionC = findViewById(R.id.cardOptionC);
        cardOptionD = findViewById(R.id.cardOptionD);

        tvOptionA = findViewById(R.id.tvOptionA);
        tvOptionB = findViewById(R.id.tvOptionB);
        tvOptionC = findViewById(R.id.tvOptionC);
        tvOptionD = findViewById(R.id.tvOptionD);

        btnBack.setOnClickListener(v -> closeActivityWithTransition());

        // Kullanıcıya kaç soru çözmek istediğini sor
        showQuestionCountSelection();
    }

    private void showQuestionCountSelection() {
        int listSize = category.getWordList().size();
        ArrayList<String> options = new ArrayList<>();
        if (listSize >= 10) options.add("10 Soru");
        if (listSize >= 20) options.add("20 Soru");
        if (listSize >= 50) options.add("50 Soru");
        options.add("Tüm Kelimeler (" + listSize + " adet)");

        final String[] items = options.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Soru Sayısını Seçin");
        builder.setCancelable(false);
        builder.setItems(items, (dialog, which) -> {
            String selected = items[which];
            if (selected.startsWith("10")) totalQuestions = 10;
            else if (selected.startsWith("20")) totalQuestions = 20;
            else if (selected.startsWith("50")) totalQuestions = 50;
            else totalQuestions = listSize;

            startQuiz();
        });
        builder.show();
    }

    private void startQuiz() {
        quizWords = new ArrayList<>(category.getWordList());
        Collections.shuffle(quizWords);
        
        // Eğer seçilen soru sayısı toplam boyuttan büyükse sınırla
        if (totalQuestions > quizWords.size()) {
            totalQuestions = quizWords.size();
        }

        // Seçilen miktarda kelimeyi tut, gerisini kaldır
        while (quizWords.size() > totalQuestions) {
            quizWords.remove(quizWords.size() - 1);
        }

        currentQuestionIndex = 0;
        correctAnswers = 0;
        loadNextQuestion();
    }

    private void loadNextQuestion() {
        // Buton renklerini ve tıklanabilirliklerini sıfırla
        resetButtons();

        if (currentQuestionIndex >= totalQuestions) {
            showQuizResults();
            return;
        }

        tvQuizProgress.setText("Soru: " + (currentQuestionIndex + 1) + " / " + totalQuestions);
        currentCorrectWord = quizWords.get(currentQuestionIndex);
        tvQuestionWord.setText(currentCorrectWord.getEnglish());

        // Şıkları oluştur
        ArrayList<String> choices = new ArrayList<>();
        choices.add(currentCorrectWord.getTurkish()); // Doğru Şık

        // Yanlış şıkları üret (kullanıcının kendi kelime havuzundan)
        ArrayList<Word> pool = new ArrayList<>();
        // Öncelikle tüm kategorilerdeki tüm kelimeleri havuza topla
        for (Category cat : repository.getAppData().getCategories()) {
            if (cat.getWordList() != null) {
                pool.addAll(cat.getWordList());
            }
        }
        
        Collections.shuffle(pool);
        for (Word w : pool) {
            if (choices.size() == 4) break;
            if (!w.getTurkish().toLowerCase().trim().equals(currentCorrectWord.getTurkish().toLowerCase().trim()) 
                && !choices.contains(w.getTurkish())) {
                choices.add(w.getTurkish());
            }
        }

        // Eğer kelime havuzu 4'ten az ise varsayılan 25 kelimeden yanlış şık üret
        if (choices.size() < 4) {
            ArrayList<String> fallbackPool = new ArrayList<>();
            for (String s : FALLBACK_WORDS) {
                if (!s.toLowerCase().trim().equals(currentCorrectWord.getTurkish().toLowerCase().trim()) 
                    && !choices.contains(s)) {
                    fallbackPool.add(s);
                }
            }
            Collections.shuffle(fallbackPool);
            while (choices.size() < 4 && !fallbackPool.isEmpty()) {
                choices.add(fallbackPool.remove(0));
            }
        }

        // Şıkları karıştır (Doğru cevap hep aynı buton olmasın)
        Collections.shuffle(choices);

        tvOptionA.setText(choices.get(0));
        tvOptionB.setText(choices.get(1));
        tvOptionC.setText(choices.get(2));
        tvOptionD.setText(choices.get(3));

        // Tıklama dinleyicilerini ata
        setOptionClickListener(cardOptionA, tvOptionA);
        setOptionClickListener(cardOptionB, tvOptionB);
        setOptionClickListener(cardOptionC, tvOptionC);
        setOptionClickListener(cardOptionD, tvOptionD);
    }

    private void setOptionClickListener(MaterialCardView card, TextView textView) {
        card.setOnClickListener(v -> {
            disableButtons();
            String selectedText = textView.getText().toString();
            boolean isCorrect = selectedText.equals(currentCorrectWord.getTurkish());

            if (isCorrect) {
                correctAnswers++;
                card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
            } else {
                card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_light)));
                // Doğru şıkkı yeşille göster
                highlightCorrectOption();
            }

            // 1.5 saniye bekle ve sonraki soruya geç
            new Handler().postDelayed(() -> {
                currentQuestionIndex++;
                loadNextQuestion();
            }, 1500);
        });
    }

    private void highlightCorrectOption() {
        String correctAns = currentCorrectWord.getTurkish();
        if (tvOptionA.getText().toString().equals(correctAns)) {
            cardOptionA.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
        } else if (tvOptionB.getText().toString().equals(correctAns)) {
            cardOptionB.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
        } else if (tvOptionC.getText().toString().equals(correctAns)) {
            cardOptionC.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
        } else if (tvOptionD.getText().toString().equals(correctAns)) {
            cardOptionD.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
        }
    }

    private void disableButtons() {
        cardOptionA.setClickable(false);
        cardOptionB.setClickable(false);
        cardOptionC.setClickable(false);
        cardOptionD.setClickable(false);
    }

    private void resetButtons() {
        cardOptionA.setClickable(true);
        cardOptionB.setClickable(true);
        cardOptionC.setClickable(true);
        cardOptionD.setClickable(true);

        int defaultCardColor = getResources().getColor(R.color.card_background);
        cardOptionA.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(defaultCardColor));
        cardOptionB.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(defaultCardColor));
        cardOptionC.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(defaultCardColor));
        cardOptionD.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(defaultCardColor));
    }

    private void showQuizResults() {
        int wrongAnswers = totalQuestions - correctAnswers;
        double scorePercent = ((double) correctAnswers / totalQuestions) * 100.0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Tamamlandı!");
        builder.setMessage("Doğru Cevap: " + correctAnswers + "\n" +
                "Yanlış Cevap: " + wrongAnswers + "\n" +
                "Başarı Oranı: %" + String.format("%.1f", scorePercent));
        builder.setCancelable(false);
        builder.setPositiveButton("Kapat", (dialog, which) -> closeActivityWithTransition());
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
