package com.example.kelimequiz;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.adapter.SentenceAdapter;
import com.example.kelimequiz.model.Sentence;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import android.widget.ProgressBar;
import androidx.cardview.widget.CardView;



public class CommonSentencesActivity extends AppCompatActivity implements SentenceAdapter.OnSentenceLongClickListener {

    private RecyclerView rvSentences;
    private SentenceAdapter adapter;
    private ArrayList<Sentence> sentenceList;
    private AppRepository repository;

    private TextView tvEmptySentenceState;
    private FloatingActionButton fabAddSentence;
    private MaterialButton btnStudySentences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_sentences);

        repository = AppRepository.getInstance(this);
        sentenceList = repository.getAppData().getSentences();

        rvSentences = findViewById(R.id.rvSentences);
        tvEmptySentenceState = findViewById(R.id.tvEmptySentenceState);
        fabAddSentence = findViewById(R.id.fabAddSentence);
        btnStudySentences = findViewById(R.id.btnStudySentences);

        // Geri Dönüş
        findViewById(R.id.btnBack).setOnClickListener(v -> closeActivityWithTransition());

        rvSentences.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SentenceAdapter(this, sentenceList, this);
        rvSentences.setAdapter(adapter);

        checkEmptyState();

        fabAddSentence.setOnClickListener(v -> showAddSentenceDialog());

        // Cümle kartlarını çalış (Rastgele Karıştırma)
        btnStudySentences.setOnClickListener(v -> {
            if (sentenceList.isEmpty()) {
                Toast.makeText(this, "Önce cümle eklemelisiniz!", Toast.LENGTH_SHORT).show();
                return;
            }
            showSentencesFlashcards();
        });
    }

    private void checkEmptyState() {
        if (sentenceList.isEmpty()) {
            tvEmptySentenceState.setVisibility(View.VISIBLE);
            rvSentences.setVisibility(View.GONE);
            btnStudySentences.setVisibility(View.GONE);
        } else {
            tvEmptySentenceState.setVisibility(View.GONE);
            rvSentences.setVisibility(View.VISIBLE);
            btnStudySentences.setVisibility(View.VISIBLE);
        }
    }

    private void showAddSentenceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_sentence, null);
        builder.setView(dialogView);

        final EditText etDialogSentenceEng = dialogView.findViewById(R.id.etDialogSentenceEng);
        final EditText etDialogSentenceTur = dialogView.findViewById(R.id.etDialogSentenceTur);

        builder.setPositiveButton("Ekle", (dialog, which) -> {
            String eng = etDialogSentenceEng.getText().toString().trim();
            String tur = etDialogSentenceTur.getText().toString().trim();

            if (eng.isEmpty() || tur.isEmpty()) {
                Toast.makeText(CommonSentencesActivity.this, "İki alan da doldurulmalıdır!", Toast.LENGTH_SHORT).show();
                return;
            }

            Sentence s = new Sentence(eng, tur);
            sentenceList.add(0, s);
            repository.save();
            adapter.notifyDataSetChanged();
            checkEmptyState();
            Toast.makeText(CommonSentencesActivity.this, "Cümle eklendi", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    // Cümle kartları üzerinde rastgele çalışma modu (Açılan dialog içinde)
    private void showSentencesFlashcards() {
        final ArrayList<Sentence> shuffledList = new ArrayList<>(sentenceList);
        Collections.shuffle(shuffledList);

        final int[] index = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_flashcard, null); // reusing flashcard layout
        builder.setView(dialogView);

        TextView tvProgress = dialogView.findViewById(R.id.tvProgress);
        ProgressBar pbProgress = dialogView.findViewById(R.id.pbProgress);
        TextView tvWord = dialogView.findViewById(R.id.tvWord);
        TextView tvLanguageLabel = dialogView.findViewById(R.id.tvLanguageLabel);
        CardView card = dialogView.findViewById(R.id.cardFlashcard);
        
        // Hide unused back button in dialog
        dialogView.findViewById(R.id.btnBack).setVisibility(View.GONE);

        pbProgress.setMax(shuffledList.size());
        
        // UI helper class
        class FlashcardState {
            boolean isEng = true;
            void update() {
                Sentence s = shuffledList.get(index[0]);
                tvWord.setText(isEng ? s.getEnglish() : s.getTurkish());
                tvLanguageLabel.setText(isEng ? "İngilizce Cümle" : "Türkçe Karşılığı");
                tvProgress.setText((index[0] + 1) + " / " + shuffledList.size());
                pbProgress.setProgress(index[0] + 1);
            }
        }
        
        final FlashcardState state = new FlashcardState();
        state.update();

        card.setOnClickListener(v -> {
            card.animate().rotationY(90f).setDuration(120).withEndAction(() -> {
                state.isEng = !state.isEng;
                card.setRotationY(-90f);
                state.update();
                card.animate().rotationY(0f).setDuration(120).start();
            }).start();
        });

        builder.setPositiveButton("Sıradaki", null); // overridden later to handle loop
        builder.setNegativeButton("Kapat", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Overriding Positive Button to prevent dialog auto-closing
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            index[0]++;
            if (index[0] < shuffledList.size()) {
                state.isEng = true;
                state.update();
            } else {
                Toast.makeText(this, "Tüm cümle kartları tamamlandı!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onSentenceLongClick(Sentence sentence, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cümleyi Sil");
        builder.setMessage("Bu cümleyi silmek istediğinize emin misiniz?");
        builder.setPositiveButton("Evet, Sil", (dialog, which) -> {
            sentenceList.remove(sentence);
            repository.save();
            adapter.notifyDataSetChanged();
            checkEmptyState();
            Toast.makeText(CommonSentencesActivity.this, "Cümle silindi", Toast.LENGTH_SHORT).show();
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
