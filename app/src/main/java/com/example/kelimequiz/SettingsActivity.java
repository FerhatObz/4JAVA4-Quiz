package com.example.kelimequiz;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.kelimequiz.repository.AppRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDarkMode;
    private MaterialButton btnExport, btnImport, btnClearAll;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = AppRepository.getInstance(this);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);
        btnClearAll = findViewById(R.id.btnClearAll);

        // Geri Dönüş
        findViewById(R.id.btnBack).setOnClickListener(v -> closeActivityWithTransition());

        // Tema durumunu yükle
        switchDarkMode.setChecked(repository.getAppData().isDarkMode());
        
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.getAppData().setDarkMode(isChecked);
            repository.save();

            // Temayı uygula
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Verileri JSON olarak Dışa Aktar (Clipboard'a kopyalar ve Paylaşım menüsünü açar)
        btnExport.setOnClickListener(v -> exportData());

        // Verileri JSON olarak İçe Aktar (Clipboard'dan yapıştırılan veriyi okur)
        btnImport.setOnClickListener(v -> showImportDialog());

        // Tüm verileri sil (Sıfırla)
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void exportData() {
        String json = repository.exportJson();

        // Clipboard'a Kopyala
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("DilKartlarimYedek", json);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Yedek kopyalandı! Paylaşabilirsiniz.", Toast.LENGTH_SHORT).show();

        // Paylaşım menüsünü tetikle
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dil Öğrenme Kartları Yedeği");
        shareIntent.putExtra(Intent.EXTRA_TEXT, json);
        startActivity(Intent.createChooser(shareIntent, "Yedeği Paylaş"));
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yedeği Geri Yükle");
        builder.setMessage("Lütfen panodan kopyaladığınız veya paylaştığınız yedek JSON metnini aşağıdaki alana yapıştırın. Bu işlem mevcut tüm verileri silecektir.");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null); // Reuse category input layout for simple text field
        builder.setView(dialogView);

        final EditText etDialogJson = dialogView.findViewById(R.id.etDialogCatName);
        etDialogJson.setHint("JSON metnini buraya yapıştırın...");

        builder.setPositiveButton("İçe Aktar", (dialog, which) -> {
            String jsonStr = etDialogJson.getText().toString().trim();
            if (jsonStr.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Metin alanı boş bırakılamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = repository.importJson(jsonStr);
            if (success) {
                Toast.makeText(SettingsActivity.this, "Veriler başarıyla içe aktarıldı ve güncellendi!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Hata! Geçersiz yedek formatı.", Toast.LENGTH_LONG).show();
            }
        });
        
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void showClearAllConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("TÜM VERİLERİ SİL");
        builder.setMessage("Kategorileriniz, kelimeleriniz ve cümleleriniz dahil tüm uygulama verileri kalıcı olarak silinecektir. Bu işlem geri alınamaz! Emin misiniz?");
        
        builder.setPositiveButton("Evet, Her Şeyi Sil", (dialog, which) -> {
            repository.clearAll();
            Toast.makeText(SettingsActivity.this, "Tüm uygulama verileri sıfırlandı.", Toast.LENGTH_SHORT).show();
            // Uygulamayı yenilemek için ana sayfaya dön
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
