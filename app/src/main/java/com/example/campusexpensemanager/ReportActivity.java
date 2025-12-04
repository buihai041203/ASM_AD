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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnGenerate;
    private TextView txtTotalExpense, txtBalance;
    private RecyclerView recyclerCategory, recyclerTransaction;
    private DatabaseHelper db;
    private DecimalFormat moneyFormat = new DecimalFormat("#,### đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DatabaseHelper(this);

        // 1. Ánh xạ View
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnGenerate = findViewById(R.id.btnGenerateReport);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        txtBalance = findViewById(R.id.txtBalance);
        recyclerCategory = findViewById(R.id.recyclerCategoryReport);
        recyclerTransaction = findViewById(R.id.recyclerTransactionReport);

        // 2. Setup RecyclerView
        recyclerCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransaction.setLayoutManager(new LinearLayoutManager(this));

        // 3. Setup Spinner & Load Data
        setupSpinners();
        loadReport(); // Load lần đầu khi mở màn hình

        // 4. Sự kiện nút Xem
        btnGenerate.setOnClickListener(v -> loadReport());
    }

    private void setupSpinners() {
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) months[i] = i + 1;

        // Tạo danh sách năm động (từ năm hiện tại - 2 đến + 2)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Integer> years = new ArrayList<>();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            years.add(i);
        }

        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));

        // Mặc định chọn tháng/năm hiện tại
        Calendar c = Calendar.getInstance();
        spinnerMonth.setSelection(c.get(Calendar.MONTH)); // Index tháng bắt đầu từ 0
        spinnerYear.setSelection(2); // Chọn năm hiện tại (vị trí index 2 trong list 5 năm)
    }

    private void loadReport() {
        int selectedMonth = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
        int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        // Key lọc: YYYY-MM (Ví dụ: 2025-12)
        String filterKey = String.format("%d-%02d", selectedYear, selectedMonth);

        // --- BƯỚC 1: Lấy dữ liệu từ DatabaseHelper mới ---
        List<Transaction> allTransactions = db.getAllTransactions();

        // --- BƯỚC 2: Xử lý lọc dữ liệu (Filter) ---
        List<Transaction> filteredList = new ArrayList<>();
        double totalExpense = 0;
        Map<String, Double> categoryMap = new HashMap<>();

        for (Transaction t : allTransactions) {
            // Kiểm tra ngày giao dịch có thuộc tháng đang chọn không
            if (t.getDate().startsWith(filterKey)) {
                filteredList.add(t);
                totalExpense += t.getAmount();

                // Gom nhóm số tiền theo danh mục
                double currentAmount = categoryMap.getOrDefault(t.getCategory(), 0.0);
                categoryMap.put(t.getCategory(), currentAmount + t.getAmount());
            }
        }

        // --- BƯỚC 3: Hiển thị Tổng chi & Số dư ---
        txtTotalExpense.setText("Tổng chi tiêu: " + moneyFormat.format(totalExpense));

        // Sử dụng hàm getBudgetByMonth đã có trong DatabaseHelper
        double budget = db.getBudgetByMonth(filterKey);
        double balance = budget - totalExpense;
        txtBalance.setText("Số dư: " + moneyFormat.format(balance));

        // --- BƯỚC 4: Hiển thị List Giao dịch (Chi tiết) ---
        // Truyền null cho listener vì ở màn hình báo cáo không cần click sửa xóa
        TransactionAdapter tranAdapter = new TransactionAdapter(filteredList, null);
        recyclerTransaction.setAdapter(tranAdapter);

        // --- BƯỚC 5: Hiển thị List Danh mục (Tổng hợp) ---
        List<CategoryReport> categoryReports = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            categoryReports.add(new CategoryReport(entry.getKey(), entry.getValue()));
        }

        CategoryReportAdapter catAdapter = new CategoryReportAdapter(categoryReports);
        recyclerCategory.setAdapter(catAdapter);
    }
}