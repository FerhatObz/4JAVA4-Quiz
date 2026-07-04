package com.example.kelimequiz;

import androidx.appcompat.app.AppCompatDelegate;

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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.adapter.CategoryAdapter;
import com.example.kelimequiz.model.Category;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryLongClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private ArrayList<Category> displayedCategories;
    private AppRepository repository;

    private EditText etSearch;
    private TextView tvEmptyState, tvSentencesCount;
    private CardView cardSentences, cardStats;
    private ImageButton btnFavorites, btnSettings;
    private MaterialButton btnCreateCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
                // Kaydedilmiş tema durumunu yükle ve uygula
        repository = AppRepository.getInstance(this);
        if (repository.getAppData().isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = AppRepository.getInstance(this);

        // Arayüz elemanlarını bağla
        rvCategories = findViewById(R.id.rvCategories);
        etSearch = findViewById(R.id.etSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvSentencesCount = findViewById(R.id.tvSentencesCount);
        cardSentences = findViewById(R.id.cardSentences);
        cardStats = findViewById(R.id.cardStats);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnSettings = findViewById(R.id.btnSettings);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);

        displayedCategories = new ArrayList<>();
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, displayedCategories, this);
        rvCategories.setAdapter(adapter);

        // Mikro animasyonları butonlara uygula
        applyPressAnimation(btnCreateCategory);
        applyPressAnimation(cardSentences);
        applyPressAnimation(cardStats);

        // Kelime/Cümle Ekleme Olayları
        btnCreateCategory.setOnClickListener(v -> showAddCategoryDialog());
        cardSentences.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CommonSentencesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        cardStats.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Arama/Filtreleme dinleyicisi
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verileri tekrar yükle ve listeyi güncelle
        refreshData();
    }

    private void refreshData() {
        // Cümle sayısını güncelle
        int sCount = repository.getAppData().getSentences() != null ? repository.getAppData().getSentences().size() : 0;
        tvSentencesCount.setText(sCount + " Cümle");

        filterCategories(etSearch.getText().toString());
    }

    private void filterCategories(String query) {
        displayedCategories.clear();
        ArrayList<Category> allCategories = repository.getAppData().getCategories();
        
        if (query.trim().isEmpty()) {
            displayedCategories.addAll(allCategories);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Category cat : allCategories) {
                // Kategori adı eşleşiyor mu?
                boolean matchesCategory = cat.getName().toLowerCase().contains(lowerQuery);
                // Kategori içindeki kelimeler eşleşiyor mu?
                boolean matchesWord = false;
                for (Word w : cat.getWordList()) {
                    if (w.getEnglish().toLowerCase().contains(lowerQuery) || 
                        w.getTurkish().toLowerCase().contains(lowerQuery)) {
                        matchesWord = true;
                        break;
                    }
                }
                if (matchesCategory || matchesWord) {
                    displayedCategories.add(cat);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Boş durum kontrolü
        if (displayedCategories.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
        }
    }

    // Buton basılma (mikro) animasyonu
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

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        final EditText etDialogCatName = dialogView.findViewById(R.id.etDialogCatName);

        builder.setPositiveButton("Oluştur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String catName = etDialogCatName.getText().toString().trim();
                if (catName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Kategori adı boş olamaz!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Yeni kategoriyi kaydet
                Category newCat = new Category(catName);
                repository.getAppData().getCategories().add(0, newCat);
                repository.save();
                refreshData();
                Toast.makeText(MainActivity.this, "Kategori oluşturuldu", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    // Kategoriye uzun basınca açılacak işlem penceresi
    @Override
    public void onCategoryLongClick(Category category, int position) {
        String[] options = {"Yeniden Adlandır", "Yukarı Taşı", "Aşağı Taşı", "Kategoriyi Sil"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kategori İşlemleri");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Yeniden adlandır
                    showRenameCategoryDialog(category);
                    break;
                case 1: // Yukarı taşı
                    moveCategory(position, true);
                    break;
                case 2: // Aşağı taşı
                    moveCategory(position, false);
                    break;
                case 3: // Sil
                    showDeleteCategoryConfirmation(category, position);
                    break;
            }
        });
        builder.show();
    }

    private void showRenameCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.etDialogCatName); // placeholder reuse
        final EditText etDialogCatName = dialogView.findViewById(R.id.etDialogCatName);
        etDialogCatName.setText(category.getName());

        builder.setPositiveButton("Güncelle", (dialog, which) -> {
            String newName = etDialogCatName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Kategori adı boş olamaz!", Toast.LENGTH_SHORT).show();
                return;
            }
            category.setName(newName);
            repository.save();
            refreshData();
            Toast.makeText(MainActivity.this, "Kategori güncellendi", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void moveCategory(int position, boolean moveUp) {
        ArrayList<Category> categories = repository.getAppData().getCategories();
        int targetPosition = moveUp ? position - 1 : position + 1;
        
        if (targetPosition < 0 || targetPosition >= categories.size()) {
            Toast.makeText(this, "Bu yönde sıralama yapılamaz!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Yer değiştir (Swap)
        Category temp = categories.get(position);
        categories.set(position, categories.get(targetPosition));
        categories.set(targetPosition, temp);
        
        repository.save();
        refreshData();
    }

    private void showDeleteCategoryConfirmation(Category category, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kategoriyi Sil");
        builder.setMessage("\"" + category.getName() + "\" kategorisini ve içindeki TÜM kelimeleri silmek istediğinize emin misiniz?");
        builder.setPositiveButton("Evet, Sil", (dialog, which) -> {
            repository.getAppData().getCategories().remove(category);
            repository.save();
            refreshData();
            Toast.makeText(MainActivity.this, "Kategori silindi", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }
}
