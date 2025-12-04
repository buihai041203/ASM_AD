package com.example.campusexpensemanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    Spinner spinnerMonth, spinnerYear;
    Button btnGenerate;
    TextView txtTotalExpense, txtBalance;

    RecyclerView recyclerCategory, recyclerTransaction;

    DatabaseHelper db;

    DecimalFormat moneyFormat = new DecimalFormat("#,### đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DatabaseHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnGenerate = findViewById(R.id.btnGenerateReport);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        txtBalance = findViewById(R.id.txtBalance);
        recyclerCategory = findViewById(R.id.recyclerCategoryReport);
        recyclerTransaction = findViewById(R.id.recyclerTransactionReport);

        recyclerCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransaction.setLayoutManager(new LinearLayoutManager(this));

        setupSpinners();

        btnGenerate.setOnClickListener(v -> loadReport());
    }

    private void setupSpinners() {
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) {
            months[i] = i + 1;
        }

        Integer[] years = new Integer[]{2023, 2024, 2025, 2026};

        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));

        Calendar c = Calendar.getInstance();
        spinnerMonth.setSelection(c.get(Calendar.MONTH));
    }

    private void loadReport() {
        int month = (int) spinnerMonth.getSelectedItem();
        int year = (int) spinnerYear.getSelectedItem();

        // Tổng chi tiêu
        double totalExpense = db.getTotalExpense(month, year);
        txtTotalExpense.setText("Tổng chi tiêu: " + moneyFormat.format(totalExpense));

        // Lấy chi phí ban đầu
        double initialBudget = db.getInitialBudget();
        double balance = initialBudget - totalExpense;
        txtBalance.setText("Số dư: " + moneyFormat.format(balance));

        // Phân tích theo danh mục
        List<CategoryReport> categoryReports = db.getExpenseByCategory(month, year);
        CategoryReportAdapter catAdapter = new CategoryReportAdapter(categoryReports);
        recyclerCategory.setAdapter(catAdapter);

        // Giao dịch trong tháng
        List<Transaction> transactionList = db.getTransactionsByMonth(month, year);
        TransactionAdapter tranAdapter = new TransactionAdapter(transactionList, null);
        recyclerTransaction.setAdapter(tranAdapter);
    }
}
