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

        txtExpense = view.findViewById(R.id.txtOverviewExpense);
        txtBudget = view.findViewById(R.id.txtOverviewBudget);
        txtRemaining = view.findViewById(R.id.txtOverviewRemaining);
        txtEmptyHistory = view.findViewById(R.id.txtEmptyHistory);
        recyclerView = view.findViewById(R.id.recyclerOverviewHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Màn hình Overview chỉ xem, không click -> truyền listener là null
        adapter = new TransactionAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(adapter);

        loadOverviewData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOverviewData();
    }

    private void loadOverviewData() {
        double totalExpense = db.getTotalExpense();
        double totalBudget = db.getTotalBudget();
        double remaining = totalBudget - totalExpense;

        DecimalFormat df = new DecimalFormat("#,### đ");
        if (txtExpense != null) txtExpense.setText(df.format(totalExpense));
        if (txtBudget != null) txtBudget.setText(df.format(totalBudget));
        if (txtRemaining != null) {
            txtRemaining.setText(df.format(remaining));
            txtRemaining.setTextColor(remaining < 0 ? Color.RED : Color.parseColor("#10B981"));
        }

        List<Transaction> list = db.getAllTransactions();
        adapter.updateData(list);

        if (txtEmptyHistory != null && recyclerView != null) {
            if (list.isEmpty()) {
                txtEmptyHistory.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                txtEmptyHistory.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}