package com.example.campusexpensemanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener position) {

    }

    // Interface giao tiếp
    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    // Constructor chuẩn
    public TransactionAdapter(List<Transaction> transactionList, OnItemClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    public void updateData(List<Transaction> list) {
        this.transactionList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout có sẵn 2 dòng của Android
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        DecimalFormat formatter = new DecimalFormat("#,### đ");

        // Dòng 1: Tên khoản chi + Số tiền
        holder.text1.setText(transaction.getNote() + " - " + formatter.format(transaction.getAmount()));

        // Dòng 2: Ngày + Danh mục
        holder.text2.setText(transaction.getDate() + " | " + transaction.getCategory());

        // Đổi màu tiền (Ví dụ đơn giản)
        holder.text1.setTextColor(Color.parseColor("#43A047")); // Màu xanh

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}