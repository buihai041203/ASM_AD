package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExpensesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private DatabaseHelper db;
    private TextView txtTotal, txtCount, txtAvg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        db = new DatabaseHelper(getContext());

        // 1. Ánh xạ View
        recyclerView = view.findViewById(R.id.recyclerExpenses);
        txtTotal = view.findViewById(R.id.txtTotalExpenseValue);
        txtCount = view.findViewById(R.id.txtTransactionCount);
        txtAvg = view.findViewById(R.id.txtAvgExpense);
        Button btnAdd = view.findViewById(R.id.btnAddExpense); // Đảm bảo ID này đúng trong XML

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- LOGIC QUAN TRỌNG: CLICK VÀO DÒNG ĐỂ SỬA ---
        adapter = new TransactionAdapter(new ArrayList<>(), transaction -> {
            // Khi click vào 1 dòng -> Chuyển sang EditActivity
            Intent intent = new Intent(getContext(), EditActivity.class);
            // Gửi dữ liệu object sang để bên kia hiển thị lại
            intent.putExtra("transaction_data", transaction);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // --- LOGIC QUAN TRỌNG: CLICK NÚT THÊM ---
        btnAdd.setOnClickListener(v -> {
            // Khi click nút Thêm -> Chuyển sang AddTransactionActivity
            Intent intent = new Intent(getContext(), AddTransactionActivity.class);
            startActivity(intent);
        });

        loadData();
        return view;
    }

    // Load lại dữ liệu khi quay lại màn hình này (Ví dụ sau khi thêm/sửa xong)
    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (db == null) return;

        List<Transaction> list = db.getAllTransactions();
        adapter.updateData(list);

        // Logic Tính toán Thống kê
        double total = 0;
        for (Transaction t : list) total += t.getAmount();

        int count = list.size();
        double avg = (count > 0) ? total / count : 0;

        DecimalFormat df = new DecimalFormat("#,### đ");

        if (txtTotal != null) txtTotal.setText(df.format(total));
        if (txtCount != null) txtCount.setText(String.valueOf(count));
        if (txtAvg != null) txtAvg.setText(df.format(avg));

        // Ẩn/Hiện layout trống (nếu có id layoutEmptyState trong xml)
        View emptyState = getView().findViewById(R.id.layoutEmptyState);
        if (emptyState != null) {
            emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}