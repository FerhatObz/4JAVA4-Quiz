package com.example.kelimequiz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.R;
import com.example.kelimequiz.model.Word;
import com.example.kelimequiz.repository.AppRepository;
import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    private final ArrayList<Word> wordList;
    private final Context context;
    private final OnWordLongClickListener longClickListener;

    public interface OnWordLongClickListener {
        void onWordLongClick(Word word, int position);
    }

    public WordAdapter(Context context, ArrayList<Word> wordList, OnWordLongClickListener longClickListener) {
        this.context = context;
        this.wordList = wordList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_word_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.tvWordEnglish.setText(word.getEnglish());
        holder.tvWordTurkish.setText(word.getTurkish());

        // Yıldız (Favori) durumunu görselleştir
        if (word.isFavorite()) {
            holder.imgFav.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.imgFav.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Tik (Öğrenildi) durumunu görselleştir
        if (word.isLearned()) {
            holder.imgLearned.setImageResource(android.R.drawable.checkbox_on_background);
            holder.imgLearned.setAlpha(1.0f);
        } else {
            holder.imgLearned.setImageResource(android.R.drawable.checkbox_off_background);
            holder.imgLearned.setAlpha(0.6f);
        }

        // Favoriye tıklama olayı
        holder.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word.setFavorite(!word.isFavorite());
                AppRepository.getInstance(context).save();
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        // Öğrenildi durumuna tıklama olayı
        holder.imgLearned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word.setLearned(!word.isLearned());
                AppRepository.getInstance(context).save();
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        // Kelime kartına uzun basma -> Düzenleme/Silme menüsü
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onWordLongClick(word, holder.getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWordEnglish, tvWordTurkish;
        ImageView imgFav, imgLearned;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWordEnglish = itemView.findViewById(R.id.tvWordEnglish);
            tvWordTurkish = itemView.findViewById(R.id.tvWordTurkish);
            imgFav = itemView.findViewById(R.id.imgFav);
            imgLearned = itemView.findViewById(R.id.imgLearned);
        }
    }
}
