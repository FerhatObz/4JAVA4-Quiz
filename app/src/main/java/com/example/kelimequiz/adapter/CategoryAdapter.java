package com.example.kelimequiz.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kelimequiz.CategoryDetailActivity;
import com.example.kelimequiz.R;
import com.example.kelimequiz.model.Category;
import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final ArrayList<Category> categoryList;
    private final Context context;
    private final OnCategoryLongClickListener longClickListener;

    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(Category category, int position);
    }

    public CategoryAdapter(Context context, ArrayList<Category> categoryList, OnCategoryLongClickListener longClickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());
        
        int wordCount = category.getWordList() != null ? category.getWordList().size() : 0;
        holder.tvWordCount.setText(wordCount + " Kelime");

        // Tıklama olayı -> Kategori detayını açar
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryDetailActivity.class);
                intent.putExtra("category_id", category.getId());
                context.startActivity(intent);
            }
        });

        // Uzun basma olayı -> Düzenle/Sil/Sırala menüsünü açar
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onCategoryLongClick(category, holder.getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvWordCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvWordCount = itemView.findViewById(R.id.tvWordCount);
        }
    }
}
