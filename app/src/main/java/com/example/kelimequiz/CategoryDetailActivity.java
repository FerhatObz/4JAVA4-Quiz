package com.example.kelimequiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.adapter.WordAdapter;
import com.example.kelimequiz.model.Category;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

public class CategoryDetailActivity extends AppCompatActivity implements WordAdapter.OnWordLongClickListener {

    private String categoryId;
    private Category category;
    private AppRepository repository;

    private RecyclerView rvWords;
    private WordAdapter adapter;
    private ArrayList<Word> displayedWords;

    private TextView tvCategoryTitle, tvEmptyWordState;
    private EditText etSearchWord;
    private ImageButton btnBack;
    private MaterialButton btnFlashcards, btnQuiz;
    private FloatingActionButton fabAddWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        repository = AppRepository.getInstance(this);
        categoryId = getIntent().getStringExtra("category_id");

        // Kategoriyi id ile bul
        findCategory();

        if (category == null) {
            Toast.makeText(this, "Kategori bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Arayüz elemanlarını bağla
        rvWords = findViewById(R.id.rvWords);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvEmptyWordState = findViewById(R.id.tvEmptyWordState);
        etSearchWord = findViewById(R.id.etSearchWord);
        btnBack = findViewById(R.id.btnBack);
        btnFlashcards = findViewById(R.id.btnFlashcards);
        btnQuiz = findViewById(R.id.btnQuiz);
        fabAddWord = findViewById(R.id.fabAddWord);

        tvCategoryTitle.setText(category.getName());

        displayedWords = new ArrayList<>();
        rvWords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WordAdapter(this, displayedWords, this);
        rvWords.setAdapter(adapter);

        applyPressAnimation(btnFlashcards);
        applyPressAnimation(btnQuiz);

        // Geri Butonu
        btnBack.setOnClickListener(v -> closeActivityWithTransition());

        // Yeni Kelime Ekleme (FAB)
        fabAddWord.setOnClickListener(v -> showAddWordDialog(null, -1));

        // Çalışma ve Quiz Modları Geçişleri
        btnFlashcards.setOnClickListener(v -> {
            if (category.getWordList().isEmpty()) {
                Toast.makeText(this, "Lütfen önce kelime ekleyin!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CategoryDetailActivity.this, FlashcardActivity.class);
                intent.putExtra("category_id", category.getId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btnQuiz.setOnClickListener(v -> {
            if (category.getWordList().isEmpty()) {
                Toast.makeText(this, "Lütfen önce kelime ekleyin!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CategoryDetailActivity.this, QuizActivity.class);
                intent.putExtra("category_id", category.getId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Arama/Filtreleme
        etSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWords(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        findCategory();
        filterWords(etSearchWord.getText().toString());
    }

    private void findCategory() {
        category = null;
        for (Category cat : repository.getAppData().getCategories()) {
            if (cat.getId().equals(categoryId)) {
                category = cat;
                break;
            }
        }
    }

    private void filterWords(String query) {
        displayedWords.clear();
        ArrayList<Word> allWords = category.getWordList();
        
        if (query.trim().isEmpty()) {
            displayedWords.addAll(allWords);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Word w : allWords) {
                if (w.getEnglish().toLowerCase().contains(lowerQuery) || 
                    w.getTurkish().toLowerCase().contains(lowerQuery)) {
                    displayedWords.add(w);
                }
            }
        }
        
        adapter.notifyDataSetChanged();

        if (displayedWords.isEmpty()) {
            tvEmptyWordState.setVisibility(View.VISIBLE);
            rvWords.setVisibility(View.GONE);
        } else {
            tvEmptyWordState.setVisibility(View.GONE);
            rvWords.setVisibility(View.VISIBLE);
        }
    }

    private void applyPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
            }
            return false;
        });
    }

    private void showAddWordDialog(final Word editWord, final int editPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_word, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogWordTitle);
        final EditText etDialogWordEng = dialogView.findViewById(R.id.etDialogWordEng);
        final EditText etDialogWordTur = dialogView.findViewById(R.id.etDialogWordTur);

        if (editWord != null) {
            tvTitle.setText("Kelimeyi Düzenle");
            etDialogWordEng.setText(editWord.getEnglish());
            etDialogWordTur.setText(editWord.getTurkish());
        }

        builder.setPositiveButton(editWord != null ? "Kaydet" : "Ekle", (dialog, which) -> {
            String eng = etDialogWordEng.getText().toString().trim();
            String tur = etDialogWordTur.getText().toString().trim();
            
            if (eng.isEmpty() || tur.isEmpty()) {
                Toast.makeText(CategoryDetailActivity.this, "İki alan da doldurulmalıdır!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editWord != null) {
                editWord.setEnglish(eng);
                editWord.setTurkish(tur);
                Toast.makeText(CategoryDetailActivity.this, "Kelime güncellendi", Toast.LENGTH_SHORT).show();
            } else {
                Word newWord = new Word(eng, tur);
                category.getWordList().add(0, newWord);
                Toast.makeText(CategoryDetailActivity.this, "Kelime eklendi", Toast.LENGTH_SHORT).show();
            }
            
            repository.save();
            filterWords(etSearchWord.getText().toString());
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    // Kelime üzerine uzun basınca açılacak işlem menüsü
    @Override
    public void onWordLongClick(Word word, int position) {
        String[] options = {
            word.isFavorite() ? "Favorilerden Çıkar" : "Favorilere Ekle",
            word.isLearned() ? "Öğrenilmedi Olarak İşaretle" : "Öğrenildi Olarak İşaretle",
            "Kelimeyi Düzenle",
            "Kelimeyi Sil"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kelime İşlemleri");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Favori
                    word.setFavorite(!word.isFavorite());
                    repository.save();
                    adapter.notifyItemChanged(position);
                    break;
                case 1: // Öğrenildi
                    word.setLearned(!word.isLearned());
                    repository.save();
                    adapter.notifyItemChanged(position);
                    break;
                case 2: // Düzenle
                    showAddWordDialog(word, position);
                    break;
                case 3: // Sil (Undo destekli)
                    deleteWord(word, position);
                    break;
            }
        });
        builder.show();
    }

    // Geri Al (Undo) destekli kelime silme fonksiyonu
    private void deleteWord(final Word word, final int position) {
        final int originalIndex = category.getWordList().indexOf(word);
        category.getWordList().remove(word);
        repository.save();
        filterWords(etSearchWord.getText().toString());

        Snackbar.make(findViewById(android.R.id.content), "Kelime silindi", Snackbar.LENGTH_LONG)
            .setAction("Geri Al", v -> {
                category.getWordList().add(originalIndex, word);
                repository.save();
                filterWords(etSearchWord.getText().toString());
            })
            .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
            .show();
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
