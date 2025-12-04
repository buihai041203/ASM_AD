package com.example.campusexpensemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.TransactionAdapter;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.Transaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {

    private TextView txtExpense, txtBudget, txtRemaining, txtEmptyHistory;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        db = new DatabaseHelper(getContext());

        // 1. Ánh xạ View
        txtExpense = view.findViewById(R.id.txtOverviewExpense);
        txtBudget = view.findViewById(R.id.txtOverviewBudget);
        txtRemaining = view.findViewById(R.id.txtOverviewRemaining);
        txtEmptyHistory = view.findViewById(R.id.txtEmptyHistory);
        recyclerView = view.findViewById(R.id.recyclerOverviewHistory);

        // 2. Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // QUAN TRỌNG: Truyền null vào listener để tắt tính năng click
        adapter = new TransactionAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(adapter);

        loadOverviewData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOverviewData(); // Load lại dữ liệu khi quay lại tab này
    }

    private void loadOverviewData() {
        if (db == null) return;

        // --- PHẦN 1: THỐNG KÊ (STATS) ---
        double totalExpense = db.getTotalExpense();
        double totalBudget = db.getTotalBudget();
        double remaining = totalBudget - totalExpense;

        DecimalFormat df = new DecimalFormat("#,### đ");
        if (txtExpense != null) txtExpense.setText(df.format(totalExpense));
        if (txtBudget != null) txtBudget.setText(df.format(totalBudget));
        if (txtRemaining != null) {
            txtRemaining.setText(df.format(remaining));
            // Đổi màu: Đỏ nếu âm, Xanh nếu dương
            txtRemaining.setTextColor(remaining < 0 ? Color.parseColor("#EF4444") : Color.parseColor("#10B981"));
        }

        // --- PHẦN 2: LỊCH SỬ (LIST) ---
        List<Transaction> list = db.getAllTransactions();

        // Nếu muốn chỉ hiện 5 giao dịch gần nhất cho gọn (Optional):
        // if (list.size() > 5) list = list.subList(0, 5);

        adapter.updateData(list);

        // Ẩn hiện thông báo nếu danh sách trống
        if (list.isEmpty()) {
            txtEmptyHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyHistory.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}