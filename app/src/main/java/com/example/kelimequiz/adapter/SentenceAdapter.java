package com.example.kelimequiz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.R;
import com.example.kelimequiz.model.Sentence;
import java.util.ArrayList;

public class SentenceAdapter extends RecyclerView.Adapter<SentenceAdapter.ViewHolder> {

    private final ArrayList<Sentence> sentenceList;
    private final Context context;
    private final OnSentenceLongClickListener longClickListener;

    public interface OnSentenceLongClickListener {
        void onSentenceLongClick(Sentence sentence, int position);
    }

    public SentenceAdapter(Context context, ArrayList<Sentence> sentenceList, OnSentenceLongClickListener longClickListener) {
        this.context = context;
        this.sentenceList = sentenceList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_sentence, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sentence sentence = sentenceList.get(position);
        holder.tvSentenceEnglish.setText(sentence.getEnglish());
        holder.tvSentenceTurkish.setText(sentence.getTurkish());

        // Varsayılan olarak Türkçe karşılık gizli olsun
        holder.tvSentenceTurkish.setVisibility(View.GONE);

        // Karta tıklandığında 3D Flip/Açılma Efekti (Türkçe anlamı aç/kapat)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Şık bir Y-ekseni dönüşü ile Türkçe anlamı görünür kıl
                holder.itemView.animate()
                    .rotationY(90f)
                    .setDuration(120)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            boolean isVisible = holder.tvSentenceTurkish.getVisibility() == View.VISIBLE;
                            holder.tvSentenceTurkish.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                            
                            holder.itemView.setRotationY(-90f);
                            holder.itemView.animate()
                                .rotationY(0f)
                                .setDuration(120)
                                .start();
                        }
                    })
                    .start();
            }
        });

        // Uzun basınca Silme eylemi tetiklensin
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onSentenceLongClick(sentence, holder.getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return sentenceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSentenceEnglish, tvSentenceTurkish;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSentenceEnglish = itemView.findViewById(R.id.tvSentenceEnglish);
            tvSentenceTurkish = itemView.findViewById(R.id.tvSentenceTurkish);
        }
    }
}
