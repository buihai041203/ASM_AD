package com.example.campusexpensemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {

    private List<CategoryReport> list;

    public CategoryReportAdapter(List<CategoryReport> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout đơn giản có sẵn của Android (chỉ có 1 TextView)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryReport item = list.get(position);
        DecimalFormat df = new DecimalFormat("#,### đ");

        // Hiển thị dạng: "Ăn uống: 500,000 đ"
        String displayText = item.getCategoryName() + ": " + df.format(item.getTotalAmount());
        holder.textView.setText(displayText);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ TextView có ID là text1 trong layout simple_list_item_1
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}