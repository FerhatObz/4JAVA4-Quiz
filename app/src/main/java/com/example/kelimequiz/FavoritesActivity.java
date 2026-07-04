package com.example.kelimequiz;

import android.os.Bundle;
import android.view.View;
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
import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity implements WordAdapter.OnWordLongClickListener {

    private RecyclerView rvFavorites;
    private WordAdapter adapter;
    private ArrayList<Word> favoriteList;
    private AppRepository repository;
    private TextView tvEmptyFavoritesState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        repository = AppRepository.getInstance(this);

        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmptyFavoritesState = findViewById(R.id.tvEmptyFavoritesState);

        // Geri Dönüş
        findViewById(R.id.btnBack).setOnClickListener(v -> closeActivityWithTransition());

        favoriteList = new ArrayList<>();
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        
        // WordAdapter'ı favori listesiyle bağla. Yıldız tıklanınca favorilerden çıkarılmasını tetikleyecek
        adapter = new WordAdapter(this, favoriteList, this);
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList.clear();
        for (Category cat : repository.getAppData().getCategories()) {
            for (Word w : cat.getWordList()) {
                if (w.isFavorite()) {
                    favoriteList.add(w);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (favoriteList.isEmpty()) {
            tvEmptyFavoritesState.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmptyFavoritesState.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWordLongClick(Word word, int position) {
        // Uzun basınca favoriden kaldırma onayı sun
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Favorilerden Çıkar");
        builder.setMessage("\"" + word.getEnglish() + "\" kelimesini favorilerden çıkarmak istediğinize emin misiniz?");
        builder.setPositiveButton("Evet, Çıkar", (dialog, which) -> {
            word.setFavorite(false);
            repository.save();
            loadFavorites();
            Toast.makeText(FavoritesActivity.this, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("İptal", null);
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
