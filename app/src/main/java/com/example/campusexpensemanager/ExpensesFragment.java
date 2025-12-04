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
import com.example.campusexpensemanager.AddTransactionActivity;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.TransactionAdapter;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.Transaction;
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
        View view = inflater.inflate(R.layout.fragment_expenses, container, false); // Đảm bảo đúng tên file XML

        db = new DatabaseHelper(getContext());

        // 1. Ánh xạ ID từ fragment_expenses.xml
        recyclerView = view.findViewById(R.id.recyclerExpenses);
        txtTotal = view.findViewById(R.id.txtTotalExpenseValue);
        txtCount = view.findViewById(R.id.txtTransactionCount);
        txtAvg = view.findViewById(R.id.txtAvgExpense);
        Button btnAdd = view.findViewById(R.id.btnAddExpense);

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), transaction -> {
            // Khi Click vào 1 dòng -> Chuyển sang Sửa
            Intent intent = new Intent(getContext(), AddTransactionActivity.class);
            Intent transactionData = intent.putExtra("transaction_data", transaction);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // 3. Sự kiện nút Thêm
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddTransactionActivity.class);
            startActivity(intent);
        });

        loadData();
        return view;
    }

    // Load lại dữ liệu khi quay lại màn hình này
    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Transaction> list = db.getAllTransactions();
        adapter.updateData(list);

        // Logic Tính toán Thống kê
        double total = 0;
        for (Transaction t : list) total += t.getAmount();

        int count = list.size();
        double avg = (count > 0) ? total / count : 0;

        DecimalFormat df = new DecimalFormat("#,### đ");
        txtTotal.setText(df.format(total));
        txtCount.setText(String.valueOf(count));
        txtAvg.setText(df.format(avg));

        // Ẩn/Hiện layout trống
        View emptyState = getView().findViewById(R.id.layoutEmptyState);
        if (emptyState != null) {
            emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}