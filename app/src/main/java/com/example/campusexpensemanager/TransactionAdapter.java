package com.example.campusexpensemanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnItemClickListener listener; // Biến lắng nghe sự kiện

    public void updateData(List<Transaction> list) {

    }

    // 1. Tạo Interface (Cái cổng giao tiếp)
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // 2. Hàm để Main cài đặt sự kiện
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransactionAdapter(ArrayList<Transaction> list, List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout có sẵn của Android cho nhanh
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new TransactionViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");

        holder.text1.setText(transaction.getTitle() + " : " + formatter.format(transaction.getAmount()));
        holder.text2.setText(transaction.getTime());

        if (transaction.getAmount() < 0) {
            holder.text1.setTextColor(Color.RED);
        } else {
            holder.text1.setTextColor(Color.parseColor("#43A047"));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public TransactionViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);

            // 3. Bắt sự kiện click vào dòng
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}